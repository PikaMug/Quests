/*******************************************************************************************************
 * Continued by FlyingPikachu/HappyPikachu with permission from _Blackvein_. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

package me.blackvein.quests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.ConversationPrefix;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.codisimus.plugins.phatloots.PhatLootsAPI;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.util.player.UserManager;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import me.blackvein.quests.listeners.CmdExecutor;
import me.blackvein.quests.listeners.NpcListener;
import me.blackvein.quests.listeners.PartiesListener;
import me.blackvein.quests.listeners.PlayerListener;
import me.blackvein.quests.prompts.QuestAcceptPrompt;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.LocaleQuery;
import me.blackvein.quests.util.MiscUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import net.aufdemrand.denizencore.scripts.ScriptRegistry;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class Quests extends JavaPlugin implements ConversationAbandonedListener {

	private String bukkitVersion = "0";
	private Dependencies depends;
	private Settings settings;
	private final List<CustomRequirement> customRequirements = new LinkedList<CustomRequirement>();
	private final List<CustomReward> customRewards = new LinkedList<CustomReward>();
	private final List<CustomObjective> customObjectives = new LinkedList<CustomObjective>();
	private LinkedList<Quester> questers = new LinkedList<Quester>();
	private LinkedList<Quest> quests = new LinkedList<Quest>();
	private LinkedList<Event> events = new LinkedList<Event>();
	private LinkedList<NPC> questNpcs = new LinkedList<NPC>();
	private LinkedList<Integer> questNpcGuis = new LinkedList<Integer>();
	private CommandExecutor cmdExecutor;
	private ConversationFactory conversationFactory;
	private ConversationFactory npcConversationFactory;
	private QuestFactory questFactory;
	private EventFactory eventFactory;
	private PlayerListener playerListener;
	private NpcListener npcListener;
	private NpcEffectThread effThread;
	private PartiesListener partiesListener;
	private DenizenTrigger trigger;
	private Lang lang;
	private LocaleQuery localeQuery;

	@SuppressWarnings("serial")
	class StageFailedException extends Exception {
	}
	@SuppressWarnings("serial")
	class SkipQuest extends Exception {
	}

	@Override
	public void onEnable() {
		// ORDER MATTERS
		bukkitVersion = Bukkit.getServer().getBukkitVersion().split("-")[0];
		settings = new Settings(this);
		localeQuery = new LocaleQuery(this);
		localeQuery.setBukkitVersion(bukkitVersion);
		playerListener = new PlayerListener(this);
		effThread = new NpcEffectThread(this);
		npcListener = new NpcListener(this);
		partiesListener = new PartiesListener();
		questFactory = new QuestFactory(this);
		eventFactory = new EventFactory(this);
		depends = new Dependencies(this);
		lang = new Lang(this);

		// 1 - Load main config
		settings.init();
		
		// 2 - Load command executor
		cmdExecutor = new CmdExecutor(this);
		
		// 3 - Load soft-depends
		depends.init();
		
		// 4 - Save resources from jar
		saveResourceAs("quests.yml", "quests.yml", false);
		saveResourceAs("events.yml", "events.yml", false);
		saveResourceAs("data.yml", "data.yml", false);
		
		// 5 - Load other configs
		try {
			setupLang();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		loadData();
		
		// 6 - Save config with any new options
		getConfig().options().copyDefaults(true);
		saveConfig();
		
		// 7 - Setup commands
		getCommand("quests").setExecutor(cmdExecutor);
		getCommand("questadmin").setExecutor(cmdExecutor);
		getCommand("quest").setExecutor(cmdExecutor);
		
		// 8 - Setup conversation factory after timeout has loaded
		this.conversationFactory = new ConversationFactory(this).withModality(false).withPrefix(new QuestsPrefix())
				.withFirstPrompt(new QuestPrompt()).withTimeout(settings.getAcceptTimeout())
				.thatExcludesNonPlayersWithMessage("Console may not perform this conversation!").addConversationAbandonedListener(this);
		this.npcConversationFactory = new ConversationFactory(this).withModality(false).withFirstPrompt(new QuestAcceptPrompt(this))
				.withTimeout(settings.getAcceptTimeout()).withLocalEcho(false).addConversationAbandonedListener(this);
		
		// 9 - Register listeners
		getServer().getPluginManager().registerEvents(playerListener, this);
		if (depends.getCitizens() != null) {
			getServer().getPluginManager().registerEvents(npcListener, this);
			if (settings.canNpcEffects()) {
				getServer().getScheduler().scheduleSyncRepeatingTask(this, effThread, 20, 20);
			}
		}
		if (depends.getPartiesApi() != null) {
			getServer().getPluginManager().registerEvents(partiesListener, this);
		}
		
		// 10 - Delay loading of Quests, Events and modules
		delayLoadQuestInfo();
	}
	
	@Override
	public void onDisable() {
		getLogger().info("Saving Quester data.");
		for (Player p : getServer().getOnlinePlayers()) {
			if (depends.getGpsApi() != null) {
				if (depends.getGpsApi().gpsIsActive(p)) {
					depends.getGpsApi().stopGPS(p);
				}
			}
			Quester quester = getQuester(p.getUniqueId());
			quester.saveData();
		}
		updateData();
	}
	
	public String getDetectedBukkitVersion() {
		return bukkitVersion;
	}
	
	public Dependencies getDependencies() {
		return depends;
	}
	
	public Settings getSettings() {
		return settings;
	}
	
	public List<CustomRequirement> getCustomRequirements() {
		return customRequirements;
	}

	public Optional<CustomRequirement> getCustomRequirement(String class_name) {
		int size=customRequirements.size();
		for(int i1=0;i1<size;i1++) {
			CustomRequirement o1=customRequirements.get(i1);
			if(o1.getClass().getName().equals(class_name)) return Optional.of(o1);
		}
		return Optional.empty();
	}
	
	
	
	public List<CustomReward> getCustomRewards() {
		return customRewards;
	}
	
	public Optional<CustomReward> getCustomReward(String class_name) {
		int size=customRewards.size();
		for(int i1=0;i1<size;i1++) {
			CustomReward o1=customRewards.get(i1);
			if(o1.getClass().getName().equals(class_name)) return Optional.of(o1);
		}
		return Optional.empty();
	}
	
	public List<CustomObjective> getCustomObjectives() {
		return customObjectives;
	}
	
	public Optional<CustomObjective> getCustomObjective(String class_name) {
		int size=customObjectives.size();
		for(int i1=0;i1<size;i1++) {
			CustomObjective o1=customObjectives.get(i1);
			if(o1.getClass().getName().equals(class_name)) return Optional.of(o1);
		}
		return Optional.empty();
	}
	
	public LinkedList<Quest> getQuests() {
		return quests;
	}
	
	public LinkedList<Event> getEvents() {
		return events;
	}
	
	public void setEvents(LinkedList<Event> events) {
		this.events = events;
	}
	
	public LinkedList<Quester> getQuesters() {
		return questers;
	}
	
	public void setQuesters(LinkedList<Quester> questers) {
		this.questers = questers;
	}
	
	public LinkedList<NPC> getQuestNpcs() {
		return questNpcs;
	}
	
	public void setQuestNpcs(LinkedList<NPC> questNpcs) {
		this.questNpcs = questNpcs;
	}
	
	public LinkedList<Integer> getQuestNpcGuis() {
		return questNpcGuis;
	}
	
	public void setQuestNpcGuis(LinkedList<Integer> questNpcGuis) {
		this.questNpcGuis = questNpcGuis;
	}
	
	public ConversationFactory getConversationFactory() {
		return conversationFactory;
	}
	
	public ConversationFactory getNpcConversationFactory() {
		return npcConversationFactory;
	}
	
	public QuestFactory getQuestFactory() {
		return questFactory;
	}
	
	public EventFactory getEventFactory() {
		return eventFactory;
	}
	
	public DenizenTrigger getDenizenTrigger() {
		return trigger;
	}
	
	public Lang getLang() {
		return lang;
	}
	
	public LocaleQuery getLocaleQuery() {
		return localeQuery;
	}

	@Override
	public void conversationAbandoned(ConversationAbandonedEvent abandonedEvent) {
		if (abandonedEvent.gracefulExit() == false) {
			if (abandonedEvent.getContext().getForWhom() != null) {
				try {
					abandonedEvent.getContext().getForWhom().sendRawMessage(ChatColor.YELLOW 
							+ Lang.get((Player) abandonedEvent.getContext().getForWhom(), "questTimeout"));
				} catch (Exception e) {
				}
			}
		}
	}

	private class QuestPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			return ChatColor.YELLOW + Lang.get((Player) context.getForWhom(), "acceptQuest") + "  " + ChatColor.GREEN + Lang.get("yesWord") + " / " + Lang.get("noWord");
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String s) {
			Player player = (Player) context.getForWhom();
			if (s.equalsIgnoreCase(Lang.get(player, "yesWord"))) {
				try {
					getQuester(player.getUniqueId()).takeQuest(getQuest(getQuester(player.getUniqueId()).questToTake), false);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return Prompt.END_OF_CONVERSATION;
			} else if (s.equalsIgnoreCase(Lang.get("noWord"))) {
				player.sendMessage(ChatColor.YELLOW + Lang.get("cancelled"));
				return Prompt.END_OF_CONVERSATION;
			} else {
				player.sendMessage(ChatColor.RED + Lang.get("questInvalidChoice"));
				return new QuestPrompt();
			}
		}
	}

	private class QuestsPrefix implements ConversationPrefix {

		@Override
		public String getPrefix(ConversationContext context) {
			return ChatColor.GRAY.toString();
		}
	}
	
	/**
	 * Transfer language files from jar to disk
	 */
	private void setupLang() throws IOException, URISyntaxException {
		final String path = "lang";
		final File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
		if(jarFile.isFile()) {
			final JarFile jar = new JarFile(jarFile);
			final Enumeration<JarEntry> entries = jar.entries();
			Set<String> results = new HashSet<String>();
			while(entries.hasMoreElements()) {
			    final String name = entries.nextElement().getName();
			    if (name.startsWith(path + "/") && name.contains("strings.yml")) {
			    	results.add(name);
			    }
			}
			for (String resourcePath : results) {
			    saveResourceAs(resourcePath, resourcePath, false);
			    saveResourceAs(resourcePath, resourcePath.replace(".yml", "_new.yml"), true);
			}
			jar.close();
		}
		try {
			lang.loadLang();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Save a Quests plugin resource to a specific path in the filesystem
	 * 
	 * @param resourcePath jar file location starting from resource folder, i.e. "lang/el-GR/strings.yml"
	 * @param outputPath file destination starting from Quests folder, i.e. "lang/el-GR/strings.yml"
	 * @param replace whether or not to replace the destination file
	 */
    public void saveResourceAs(String resourcePath, String outputPath, boolean replace) {
    	if (resourcePath == null || resourcePath.equals("")) {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }

        resourcePath = resourcePath.replace('\\', '/');
        InputStream in = getResource(resourcePath);
        if (in == null) {
            throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found in Quests jar");
        }
        
        String outPath = outputPath.replace('/', File.separatorChar).replace('\\', File.separatorChar);
        File outFile = new File(getDataFolder(), outPath);
        File outDir = new File(outFile.getPath().replace(outFile.getName(), ""));
        
        if (!outDir.exists()) {
            if (!outDir.mkdirs()) {
            	getLogger().log(Level.SEVERE, "Failed to make directories for " + outFile.getName() + " (canWrite= " + outDir.canWrite() + ")");
            }
        }

        try {
            if (!outFile.exists() || replace) {
                OutputStream out = new FileOutputStream(outFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
                if (!outFile.exists()) {
                	getLogger().severe("Unable to copy " + outFile.getName() + " (canWrite= " + outFile.canWrite() + ")");
                }
            }
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile, ex);
        }
    }

	private void delayLoadQuestInfo() {
		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {

			@Override
			public void run() {
				loadQuests();
				loadEvents();
				getLogger().log(Level.INFO, "Loaded " + quests.size() + " Quest(s)"
						+ ", " + events.size() + " Event(s)"
						+ ", " + Lang.size() + " Phrase(s)");
				questers.addAll(getOnlineQuesters());
				loadModules();
			}
		}, 5L);
	}

	public void loadData() {
		YamlConfiguration config = new YamlConfiguration();
		File dataFile = new File(this.getDataFolder(), "data.yml");
		try {
			config.load(dataFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (config.contains("npc-gui")) {
			List<Integer> ids = config.getIntegerList("npc-gui");
			questNpcGuis.clear();
			questNpcGuis.addAll(ids);
		}
	}

	public void loadModules() {
		File f = new File(this.getDataFolder(), "modules");
		if (f.exists() && f.isDirectory()) {
			File[] modules = f.listFiles();
			if (modules != null) {
				for (File module : modules) {
					if (module.isDirectory() == false && module.getName().endsWith(".jar")) {
						loadModule(module);
					}
				}
			}
		} else {
			f.mkdir();
		}
		boolean failedToLoad;
		FileConfiguration config = null;
		File file = new File(this.getDataFolder(), "quests.yml");
		try {
			config = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(file), "UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (config != null) {
			ConfigurationSection questsSection;
			if (config.contains("quests")) {
				questsSection = config.getConfigurationSection("quests");
				for (String questKey : questsSection.getKeys(false)) {
					try { // main "skip quest" try/catch block
						Quest quest = new Quest();
						failedToLoad = false;
						if (config.contains("quests." + questKey + ".name")) {
							quest = getQuest(parseString(config.getString("quests." + questKey + ".name"), quest));
							loadCustomSections(quest, config, questKey);
						} else {
							skipQuestProcess("Quest block \'" + questKey + "\' is missing " + ChatColor.RED + "name:");
						}
						if (failedToLoad == true) {
							getLogger().log(Level.SEVERE, "Failed to load Quest \"" + questKey + "\". Skipping.");
						}
					} catch (SkipQuest ex) {
						continue;
					} catch (StageFailedException ex) {
						continue;
					}
				}
			}
		} else {
			getLogger().severe("Unable to load module data from quests.yml");
		}
	}

	public void loadModule(File jar) {
		try {
			@SuppressWarnings("resource")
			JarFile jarFile = new JarFile(jar);
			Enumeration<JarEntry> e = jarFile.entries();
			URL[] urls = { new URL("jar:file:" + jar.getPath() + "!/") };
			ClassLoader cl = URLClassLoader.newInstance(urls, getClassLoader());
			int count = 0;
			while (e.hasMoreElements()) {
				JarEntry je = (JarEntry) e.nextElement();
				if (je.isDirectory() || !je.getName().endsWith(".class")) {
					continue;
				}
				String className = je.getName().substring(0, je.getName().length() - 6);
				className = className.replace('/', '.');
				Class<?> c = Class.forName(className, true, cl);
				if (CustomRequirement.class.isAssignableFrom(c)) {
					Class<? extends CustomRequirement> requirementClass = c.asSubclass(CustomRequirement.class);
					Constructor<? extends CustomRequirement> cstrctr = requirementClass.getConstructor();
					CustomRequirement requirement = cstrctr.newInstance();
					Optional<CustomRequirement>oo=getCustomRequirement(requirement.getClass().getName());
					if (oo.isPresent()) customRequirements.remove(oo.get());
					customRequirements.add(requirement);
					String name = requirement.getName() == null ? "[" + jar.getName() + "]" : requirement.getName();
					String author = requirement.getAuthor() == null ? "[Unknown]" : requirement.getAuthor();
					count++;
					getLogger().info("Loaded Module: " + name + " by " + author);
				} else if (CustomReward.class.isAssignableFrom(c)) {
					Class<? extends CustomReward> rewardClass = c.asSubclass(CustomReward.class);
					Constructor<? extends CustomReward> cstrctr = rewardClass.getConstructor();
					CustomReward reward = cstrctr.newInstance();
					Optional<CustomReward>oo=getCustomReward(reward.getClass().getName());
					if (oo.isPresent()) customRewards.remove(oo.get());
					customRewards.add(reward);
					String name = reward.getName() == null ? "[" + jar.getName() + "]" : reward.getName();
					String author = reward.getAuthor() == null ? "[Unknown]" : reward.getAuthor();
					count++;
					getLogger().info("Loaded Module: " + name + " by " + author);
				} else if (CustomObjective.class.isAssignableFrom(c)) {
					Class<? extends CustomObjective> objectiveClass = c.asSubclass(CustomObjective.class);
					Constructor<? extends CustomObjective> cstrctr = objectiveClass.getConstructor();
					CustomObjective objective = cstrctr.newInstance();
					Optional<CustomObjective>oo=getCustomObjective(objective.getClass().getName());
					if (oo.isPresent()) {
						HandlerList.unregisterAll(oo.get());
						customObjectives.remove(oo.get());
					}
					customObjectives.add(objective);
					String name = objective.getName() == null ? "[" + jar.getName() + "]" : objective.getName();
					String author = objective.getAuthor() == null ? "[Unknown]" : objective.getAuthor();
					count++;
					getLogger().info("Loaded Module: " + name + " by " + author);
					try {
						getServer().getPluginManager().registerEvents(objective, this);
						getLogger().info("Registered events for custom objective \"" + name + "\"");
					} catch (Exception ex) {
						getLogger().warning("Failed to register events for custom objective \"" + name + "\". Does the objective class listen for events?");
						ex.printStackTrace();
					}
				}
			}
			if (count == 0) {
				getLogger().severe("Unable to load module from file: " + jar.getName() + ", jar file is not a valid module!");
			}
		} catch (Exception e) {
			getLogger().severe("Unable to load module from file: " + jar.getName());
			e.printStackTrace();
		}
	}

	/**
	 * Show all of a player's objectives for the current stage of a quest.<p>
	 * 
	 * Respects PlaceholderAPI and translations, when enabled.
	 * 
	 * @param quest The quest to get current stage objectives of
	 * @param quester The player to show current stage objectives to
	 * @param ignoreOverrides Whether to ignore objective-overrides
	 */
	@SuppressWarnings("deprecation")
	public void showObjectives(Quest quest, Quester quester, boolean ignoreOverrides) {
		for (String obj : quester.getObjectives(quest, false)) {
			if (depends.getPlaceholderApi() != null) {
				obj = PlaceholderAPI.setPlaceholders(quester.getPlayer(), obj);
			}
			try {
				// TODO ensure all applicable strings are translated
				String sbegin = obj.substring(obj.indexOf(ChatColor.AQUA.toString()) + 2);
				String serial = sbegin.substring(0, sbegin.indexOf(ChatColor.GREEN.toString()));
				Stage stage = quester.getCurrentStage(quest);
				if (obj.contains(Lang.get(quester.getPlayer(), "break"))) {
					for (ItemStack is : stage.blocksToBreak) {
						if (Material.matchMaterial(serial) != null) {
							if (Material.matchMaterial(serial).equals(is.getType())) {
								localeQuery.sendMessage(quester.getPlayer(), obj.replace(serial, "<item>"), is.getType(), is.getDurability(), null);
								break;
							}
						}
					}
				} else if (obj.contains(Lang.get(quester.getPlayer(), "damage"))) {
					for (ItemStack is : stage.blocksToDamage) {
						if (Material.matchMaterial(serial) != null) {
							if (Material.matchMaterial(serial).equals(is.getType())) {
								localeQuery.sendMessage(quester.getPlayer(), obj.replace(serial, "<item>"), is.getType(), is.getDurability(), null);
								break;
							}
						}
					}
				} else if (obj.contains(Lang.get(quester.getPlayer(), "place"))) {
					for (ItemStack is : stage.blocksToPlace) {
						if (Material.matchMaterial(serial) != null) {
							if (Material.matchMaterial(serial).equals(is.getType())) {
								localeQuery.sendMessage(quester.getPlayer(), obj.replace(serial, "<item>"), is.getType(), is.getDurability(), null);
								break;
							}
						}
					}
				} else if (obj.contains(Lang.get(quester.getPlayer(), "use"))) {
					for (ItemStack is : stage.blocksToUse) {
						if (Material.matchMaterial(serial) != null) {
							if (Material.matchMaterial(serial).equals(is.getType())) {
								localeQuery.sendMessage(quester.getPlayer(), obj.replace(serial, "<item>"), is.getType(), is.getDurability(), null);
								break;
							}
						}
					}
				} else if (obj.contains(Lang.get(quester.getPlayer(), "cut"))) {
					for (ItemStack is : stage.blocksToCut) {
						if (Material.matchMaterial(serial) != null) {
							if (Material.matchMaterial(serial).equals(is.getType())) {
								localeQuery.sendMessage(quester.getPlayer(), obj.replace(serial, "<item>"), is.getType(), is.getDurability(), null);
								break;
							}
						}
					}
				} else if (obj.contains(Lang.get(quester.getPlayer(), "craft"))) {
					for (ItemStack is : stage.getItemsToCraft()) {
						if (Material.matchMaterial(serial) != null) {
							if (Material.matchMaterial(serial).equals(is.getType())) {
								localeQuery.sendMessage(quester.getPlayer(), obj.replace(serial, "<item>"), is.getType(), is.getDurability(), null);
								break;
							}
						}
					}
				}
				//TODO find a better way to detect a deliver objective
				else if (obj.contains(Lang.get(quester.getPlayer(), "deliver").split(" ")[0])) {
					for (ItemStack is : stage.getItemsToDeliver()) {
						if (Material.matchMaterial(serial) != null) {
							if (Material.matchMaterial(serial).equals(is.getType())) {
								String enchant = "";
								if (!is.getEnchantments().isEmpty()) {
									//TODO parse multiple enchantments?
									localeQuery.sendMessage(quester.getPlayer(), obj.replace(serial, "<item>").replace(enchant, "<enchantment>"),
											is.getType(), is.getDurability(), is.getEnchantments());
									break;
								} else {
									localeQuery.sendMessage(quester.getPlayer(), obj.replace(serial, "<item>"), is.getType(), is.getDurability(), null);
									break;
								}
							}
						}
					}
				} else if (obj.contains(Lang.get(quester.getPlayer(), "kill"))) {
					if (stage.mobsToKill == null || stage.mobsToKill.isEmpty()) {
						// Could be Kill a Player objective
						quester.getPlayer().sendMessage(obj);
						return;
					}
					for (EntityType type : stage.mobsToKill) {
						try {
							EntityType et = EntityType.valueOf(serial.toUpperCase().replace(" ", "_"));
							if (et.equals(type)) {
								//TODO account for extra data like Villager career
								localeQuery.sendMessage(quester.getPlayer(), obj.replace(serial, "<mob>"), type, null);
								break;
							}
						} catch (IllegalArgumentException iae) {
							// Could be Kill a Player objective 
							quester.getPlayer().sendMessage(obj);
						}
						break;
					}
				} else if (obj.contains(Lang.get(quester.getPlayer(), "tame"))) {
					for (Entry<EntityType, Integer> e : stage.mobsToTame.entrySet()) {
						try {
							EntityType type = e.getKey();
							EntityType et = EntityType.valueOf(serial.toUpperCase().replace(" ", "_"));
							if (et.equals(type)) {
								localeQuery.sendMessage(quester.getPlayer(), obj.replace(serial, "<mob>"), type, null);
								break;
							}
						} catch (IllegalArgumentException iae) {
							// Do nothing
						}
					}
				} else {
					quester.getPlayer().sendMessage(obj);
				}
			} catch (IndexOutOfBoundsException e) {
				quester.getPlayer().sendMessage(obj);
			}
		}
	}

	public void listQuests(Player player, int page) {
        int rows = 7;
        if ((quests.size() + rows) < ((page * rows)) || quests.size() == 0) {
            player.sendMessage(ChatColor.YELLOW + Lang.get(player, "pageNotExist"));
        } else {
            player.sendMessage(ChatColor.GOLD + Lang.get(player, "questListTitle"));
            int fromOrder = (page - 1) * rows;
 
            List<Quest> subQuests;
 
            if (quests.size() >= (fromOrder + rows)) {
                subQuests = quests.subList((fromOrder), (fromOrder + rows));
            } else {
                subQuests = quests.subList((fromOrder), quests.size());
            }
 
            fromOrder++;
 
            for (Quest q : subQuests) {
                player.sendMessage(ChatColor.YELLOW + Integer.toString(fromOrder) + ". " + q.getName());
                fromOrder++;
            }
            int numPages = (int) Math.ceil(((double) quests.size()) / ((double) rows));
 
            String msg = Lang.get(player, "pageFooter");
            msg = msg.replace("<current>", String.valueOf(page));
            msg = msg.replace("<all>", String.valueOf(numPages));
            player.sendMessage(ChatColor.GOLD + msg);
        }
	}

	public void reloadQuests() {
		quests.clear();
		events.clear();
		
		loadQuests();
		loadData();
		loadEvents();
		// Reload config from disc in-case a setting was changed
		reloadConfig();
		settings.init();
		Lang.clear();
		try {
			lang.loadLang();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		loadModules();
		for (Quester quester : questers) {
			for (Quest q : quester.currentQuests.keySet()) {
				quester.checkQuest(q);
			}
		}
	}

	public Quester getQuester(UUID id) {
		Quester quester = null;
		for (Quester q: questers) {
			if (q.getUUID().equals(id)) {
				quester = q;
			}
		}
		if (quester == null) {
			quester = new Quester(this);
			quester.setUUID(id);
			if (depends.getCitizens() != null) {
				if (depends.getCitizens().getNPCRegistry().getByUniqueId(id) != null) {
					return quester;
				}
			}
			questers.add(quester);
			if (!quester.loadData()) {
				questers.remove(quester);
			}
		}
		return quester;
	}

	public Quester getQuester(String name) {
		UUID id = null;
		Quester quester = null;
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (p.getName().equalsIgnoreCase(name)) {
				id = p.getUniqueId();
				break;
			}
		}
		if (id != null) {
			quester = getQuester(id);
		}
		return quester;
	}

	public LinkedList<Quester> getOnlineQuesters() {
		LinkedList<Quester> qs = new LinkedList<Quester>();
		for (Player p : getServer().getOnlinePlayers()) {
			Quester quester = new Quester(this);
			quester.setUUID(p.getUniqueId());
			if (quester.loadData() == false) {
				quester.saveData();
			}
			qs.add(quester);
			// Kind of hacky to put this here, works around issues with the compass on fast join
			quester.findCompassTarget();
		}
		return qs;
	}

	public void loadQuests() {
		boolean failedToLoad;
		boolean needsSaving = false;
		FileConfiguration config = null;
		File file = new File(this.getDataFolder(), "quests.yml");
		try {
			config = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(file), "UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (config != null) {
			ConfigurationSection questsSection;
			if (config.contains("quests")) {
				questsSection = config.getConfigurationSection("quests");
			} else {
				questsSection = config.createSection("quests");
				needsSaving = true;
			}
			if (questsSection == null) {
				getLogger().severe("Missing \'quests\' section marker within quests.yml, canceled loading");
				return;
			}
			for (String questKey : questsSection.getKeys(false)) {
				try { // main "skip quest" try/catch block
					Quest quest = new Quest();
					failedToLoad = false;
					if (config.contains("quests." + questKey + ".name")) {
						quest.setName(parseString(config.getString("quests." + questKey + ".name"), quest));
					} else {
						skipQuestProcess("Quest block \'" + questKey + "\' is missing " + ChatColor.RED + "name:");
					}
					if (depends.getCitizens() != null && config.contains("quests." + questKey + ".npc-giver-id")) {
						if (CitizensAPI.getNPCRegistry().getById(config.getInt("quests." + questKey + ".npc-giver-id")) != null) {
							quest.npcStart = CitizensAPI.getNPCRegistry().getById(config.getInt("quests." + questKey + ".npc-giver-id"));
							questNpcs.add(CitizensAPI.getNPCRegistry().getById(config.getInt("quests." + questKey + ".npc-giver-id")));
						} else {
							skipQuestProcess("npc-giver-id: for Quest " + quest.getName() + " is not a valid NPC id!");
						}
					}
					if (config.contains("quests." + questKey + ".block-start")) {
						Location location = getLocation(config.getString("quests." + questKey + ".block-start"));
						if (location != null) {
							quest.blockStart = location;
						} else {
							skipQuestProcess(new String[] { "block-start: for Quest " + quest.getName() + " is not in proper location format!", "Proper location format is: \"WorldName x y z\"" });
						}
					}
					if (config.contains("quests." + questKey + ".region")) {
						String region = config.getString("quests." + questKey + ".region");
						boolean exists = regionFound(quest, region);
						if (!exists) {
							skipQuestProcess("region: for Quest " + quest.getName() + " is not a valid WorldGuard region!");
						}
					}
					if (config.contains("quests." + questKey + ".gui-display")) {
						String item = config.getString("quests." + questKey + ".gui-display");
						try {
							ItemStack stack = ItemUtil.readItemStack(item);
							if (stack != null) {
								quest.guiDisplay = stack;
							}
						} catch (Exception e) {
							this.getLogger().warning(item + " in items: GUI Display in Quest " + quest.getName() + "is not properly formatted!");
						}
					}
					if (config.contains("quests." + questKey + ".redo-delay")) {
						//Legacy
						if (config.getInt("quests." + questKey + ".redo-delay", -999) != -999) {
							quest.getPlanner().setCooldown(config.getInt("quests." + questKey + ".redo-delay") * 1000);
						} else {
							skipQuestProcess("redo-delay: for Quest " + quest.getName() + " is not a number!");
						}
					}
					if (config.contains("quests." + questKey + ".finish-message")) {
						quest.finished = parseString(config.getString("quests." + questKey + ".finish-message"), quest);
					} else {
						skipQuestProcess("Quest " + quest.getName() + " is missing finish-message:");
					}
					if (config.contains("quests." + questKey + ".ask-message")) {
						quest.description = parseString(config.getString("quests." + questKey + ".ask-message"), quest);
					} else {
						skipQuestProcess("Quest " + quest.getName() + " is missing ask-message:");
					}
					if (config.contains("quests." + questKey + ".event")) {
						Event evt = Event.loadEvent(config.getString("quests." + questKey + ".event"), this);
						if (evt != null) {
							quest.initialEvent = evt;
						} else {
							skipQuestProcess("Initial Event in Quest " + quest.getName() + " failed to load.");
						}
					}
					if (config.contains("quests." + questKey + ".requirements")) {
						loadQuestRequirements(config, questsSection, quest, questKey);
					}
					if (config.contains("quests." + questKey + ".planner")) {
						loadQuestPlanner(config, questsSection, quest, questKey);
					}
					quest.plugin = this;
					processStages(quest, config, questKey);
					loadQuestRewards(config, quest, questKey);
					quests.add(quest);
					if (needsSaving) {
						try {
							config.save(file);
						} catch (IOException e) {
							getLogger().log(Level.SEVERE, "Failed to save Quest \"" + questKey + "\"");
							e.printStackTrace();
						}
					}
					if (failedToLoad == true) {
						getLogger().log(Level.SEVERE, "Failed to load Quest \"" + questKey + "\". Skipping.");
					}
				} catch (SkipQuest ex) {
					continue;
				} catch (StageFailedException ex) {
					continue;
				}
			}
		} else {
			getLogger().severe("Unable to load quests.yml");
		}
	}

	private void loadQuestRewards(FileConfiguration config, Quest quest, String questKey) throws SkipQuest {
		Rewards rews = quest.getRewards();
		if (config.contains("quests." + questKey + ".rewards.items")) {
			if (Quests.checkList(config.getList("quests." + questKey + ".rewards.items"), String.class)) {
				LinkedList<ItemStack> temp = new LinkedList<ItemStack>();
				for (String item : config.getStringList("quests." + questKey + ".rewards.items")) {
					try {
						ItemStack stack = ItemUtil.readItemStack(item);
						if (stack != null) {
							temp.add(stack);
						}
					} catch (Exception e) {
						skipQuestProcess("" + item + " in items: Reward in Quest " + quest.getName() + " is not properly formatted!");
					}
				}
				rews.setItems(temp);
			} else {
				skipQuestProcess("items: Reward in Quest " + quest.getName() + " is not a list of strings!");
			}
		}
		if (config.contains("quests." + questKey + ".rewards.money")) {
			if (config.getInt("quests." + questKey + ".rewards.money", -999) != -999) {
				rews.setMoney(config.getInt("quests." + questKey + ".rewards.money"));
			} else {
				skipQuestProcess("money: Reward in Quest " + quest.getName() + " is not a number!");
			}
		}
		if (config.contains("quests." + questKey + ".rewards.exp")) {
			if (config.getInt("quests." + questKey + ".rewards.exp", -999) != -999) {
				rews.setExp(config.getInt("quests." + questKey + ".rewards.exp"));
			} else {
				skipQuestProcess("exp: Reward in Quest " + quest.getName() + " is not a number!");
			}
		}
		if (config.contains("quests." + questKey + ".rewards.commands")) {
			if (Quests.checkList(config.getList("quests." + questKey + ".rewards.commands"), String.class)) {
				
				rews.setCommands(config.getStringList("quests." + questKey + ".rewards.commands"));
			} else {
				skipQuestProcess("commands: Reward in Quest " + quest.getName() + " is not a list of commands!");
			}
		}
		if (config.contains("quests." + questKey + ".rewards.permissions")) {
			if (Quests.checkList(config.getList("quests." + questKey + ".rewards.permissions"), String.class)) {
				rews.setPermissions(config.getStringList("quests." + questKey + ".rewards.permissions"));
			} else {
				skipQuestProcess("permissions: Reward in Quest " + quest.getName() + " is not a list of permissions!");
			}
		}
		if (config.contains("quests." + questKey + ".rewards.quest-points")) {
			if (config.getInt("quests." + questKey + ".rewards.quest-points", -999) != -999) {
				rews.setQuestPoints(config.getInt("quests." + questKey + ".rewards.quest-points"));
			} else {
				skipQuestProcess("quest-points: Reward in Quest " + quest.getName() + " is not a number!");
			}
		}
		if (depends.isPluginAvailable("mcMMO")) {
			if (config.contains("quests." + questKey + ".rewards.mcmmo-skills")) {
				if (Quests.checkList(config.getList("quests." + questKey + ".rewards.mcmmo-skills"), String.class)) {
					if (config.contains("quests." + questKey + ".rewards.mcmmo-levels")) {
						if (Quests.checkList(config.getList("quests." + questKey + ".rewards.mcmmo-levels"), Integer.class)) {
							for (String skill : config.getStringList("quests." + questKey + ".rewards.mcmmo-skills")) {
								if (depends.getMcmmo() == null) {
									skipQuestProcess("" + skill + " in mcmmo-skills: Reward in Quest " + quest.getName() + " requires the mcMMO plugin!");
								} else if (Quests.getMcMMOSkill(skill) == null) {
									skipQuestProcess("" + skill + " in mcmmo-skills: Reward in Quest " + quest.getName() + " is not a valid mcMMO skill name!");
								}
							}
							rews.setMcmmoSkills(config.getStringList("quests." + questKey + ".rewards.mcmmo-skills"));
							rews.setMcmmoAmounts(config.getIntegerList("quests." + questKey + ".rewards.mcmmo-levels"));
						} else {
							skipQuestProcess("mcmmo-levels: Reward in Quest " + quest.getName() + " is not a list of numbers!");
						}
					} else {
						skipQuestProcess("Rewards for Quest " + quest.getName() + " is missing mcmmo-levels:");
					}
				} else {
					skipQuestProcess("mcmmo-skills: Reward in Quest " + quest.getName() + " is not a list of mcMMO skill names!");
				}
			}
		}
		if (depends.isPluginAvailable("Heroes")) {
			if (config.contains("quests." + questKey + ".rewards.heroes-exp-classes")) {
				if (Quests.checkList(config.getList("quests." + questKey + ".rewards.heroes-exp-classes"), String.class)) {
					if (config.contains("quests." + questKey + ".rewards.heroes-exp-amounts")) {
						if (Quests.checkList(config.getList("quests." + questKey + ".rewards.heroes-exp-amounts"), Double.class)) {
							for (String heroClass : config.getStringList("quests." + questKey + ".rewards.heroes-exp-classes")) {
								if (depends.getHeroes() == null) {
									skipQuestProcess("" + heroClass + " in heroes-exp-classes: Reward in Quest " + quest.getName() + " requires the Heroes plugin!");
								} else if (depends.getHeroes().getClassManager().getClass(heroClass) == null) {
									skipQuestProcess("" + heroClass + " in heroes-exp-classes: Reward in Quest " + quest.getName() + " is not a valid Heroes class name!");
								}
							}
							rews.setHeroesClasses(config.getStringList("quests." + questKey + ".rewards.heroes-exp-classes"));
							rews.setHeroesAmounts(config.getDoubleList("quests." + questKey + ".rewards.heroes-exp-amounts"));
						} else {
							skipQuestProcess("heroes-exp-amounts: Reward in Quest " + quest.getName() + " is not a list of experience amounts (decimal numbers)!");
						}
					} else {
						skipQuestProcess("Rewards for Quest " + quest.getName() + " is missing heroes-exp-amounts:");
					}
				} else {
					skipQuestProcess("heroes-exp-classes: Reward in Quest " + quest.getName() + " is not a list of Heroes classes!");
				}
			}
		}
		if (depends.isPluginAvailable("PhatLoots")) {
			if (config.contains("quests." + questKey + ".rewards.phat-loots")) {
				if (Quests.checkList(config.getList("quests." + questKey + ".rewards.phat-loots"), String.class)) {
					for (String loot : config.getStringList("quests." + questKey + ".rewards.phat-loots")) {
						if (depends.getPhatLoots() == null) {
							skipQuestProcess("" + loot + " in phat-loots: Reward in Quest " + quest.getName() + " requires the PhatLoots plugin!");
						} else if (PhatLootsAPI.getPhatLoot(loot) == null) {
							skipQuestProcess("" + loot + " in phat-loots: Reward in Quest " + quest.getName() + " is not a valid PhatLoot name!");
						}
					}
					rews.setPhatLoots(config.getStringList("quests." + questKey + ".rewards.phat-loots"));
				} else {
					skipQuestProcess("phat-loots: Reward in Quest " + quest.getName() + " is not a list of PhatLoots!");
				}
			}
		}
	}

	private void loadQuestRequirements(FileConfiguration config, ConfigurationSection questsSection, Quest quest, String questKey) throws SkipQuest {
		Requirements reqs = quest.getRequirements();
		if (config.contains("quests." + questKey + ".requirements.fail-requirement-message")) {
			reqs.setFailRequirements(parseString(config.getString("quests." + questKey + ".requirements.fail-requirement-message"), quest));
		} else {
			skipQuestProcess("Requirements for Quest " + quest.getName() + " is missing fail-requirement-message:");
		}
		if (config.contains("quests." + questKey + ".requirements.items")) {
			if (Quests.checkList(config.getList("quests." + questKey + ".requirements.items"), String.class)) {
				List<String> itemReqs = config.getStringList("quests." + questKey + ".requirements.items");
				boolean failed = false;
				List<ItemStack> temp = reqs.getItems();
				for (String item : itemReqs) {
					ItemStack stack = ItemUtil.readItemStack(item);
					if (stack != null) {
						temp.add(stack);
					} else {
						failed = true;
						break;
					}
				}
				reqs.setItems(temp);
				if (failed == true) {
					skipQuestProcess("items: Requirement for Quest " + quest.getName() + " is not formatted correctly!");
				}
			} else {
				skipQuestProcess("items: Requirement for Quest " + quest.getName() + " is not formatted correctly!");
			}
			if (config.contains("quests." + questKey + ".requirements.remove-items")) {
				if (Quests.checkList(config.getList("quests." + questKey + ".requirements.remove-items"), Boolean.class)) {
					reqs.setRemoveItems(config.getBooleanList("quests." + questKey + ".requirements.remove-items"));
				} else {
					skipQuestProcess("remove-items: Requirement for Quest " + quest.getName() + " is not a list of true/false values!");
				}
			} else {
				skipQuestProcess("Requirements for Quest " + quest.getName() + " is missing remove-items:");
			}
		}
		if (config.contains("quests." + questKey + ".requirements.money")) {
			if (config.getInt("quests." + questKey + ".requirements.money", -999) != -999) {
				reqs.setMoney(config.getInt("quests." + questKey + ".requirements.money"));
			} else {
				skipQuestProcess("money: Requirement for Quest " + quest.getName() + " is not a number!");
			}
		}
		if (config.contains("quests." + questKey + ".requirements.quest-points")) {
			if (config.getInt("quests." + questKey + ".requirements.quest-points", -999) != -999) {
				reqs.setQuestPoints(config.getInt("quests." + questKey + ".requirements.quest-points"));
			} else {
				skipQuestProcess("quest-points: Requirement for Quest " + quest.getName() + " is not a number!");
			}
		}
		if (config.contains("quests." + questKey + ".requirements.quest-blocks")) {
			if (Quests.checkList(config.getList("quests." + questKey + ".requirements.quest-blocks"), String.class)) {
				List<String> names = config.getStringList("quests." + questKey + ".requirements.quest-blocks");
				boolean failed = false;
				String failedQuest = "NULL";
				List<String> temp = new LinkedList<String>();
				for (String name : names) {
					boolean done = false;
					for (String string : questsSection.getKeys(false)) {
						if (config.getString("quests." + string + ".name").equalsIgnoreCase(name)) {
							temp.add(name);
							done = true;
							break;
						}
					}
					if (!done) {
						failed = true;
						failedQuest = name;
						break;
					}
				}
				reqs.setNeededQuests(temp);
				if (failed) {
					skipQuestProcess(new String[] { "" + ChatColor.LIGHT_PURPLE + failedQuest + " inside quests: Requirement for Quest " 
							+ quest.getName() + " is not a valid Quest name!", "Make sure you aren\'t using the config section identifier." });
				}
			} else {
				skipQuestProcess("quest-blocks: Requirement for Quest " + quest.getName() + " is not a list of Quest names!");
			}
		}
		if (config.contains("quests." + questKey + ".requirements.quests")) {
			if (Quests.checkList(config.getList("quests." + questKey + ".requirements.quests"), String.class)) {
				List<String> names = config.getStringList("quests." + questKey + ".requirements.quests");
				boolean failed = false;
				String failedQuest = "NULL";
				List<String> temp = new LinkedList<String>();
				for (String name : names) {
					boolean done = false;
					for (String string : questsSection.getKeys(false)) {
						if (config.getString("quests." + string + ".name").equalsIgnoreCase(name)) {
							temp.add(name);
							done = true;
							break;
						}
					}
					if (!done) {
						failed = true;
						failedQuest = name;
						break;
					}
				}
				reqs.setNeededQuests(temp);
				if (failed) {
					skipQuestProcess(new String[] { "" + failedQuest + " inside quests: Requirement for Quest "
							+ quest.getName() + " is not a valid Quest name!", "Make sure you aren\'t using the config section identifier." });
				}
			} else {
				skipQuestProcess("quests: Requirement for Quest " + quest.getName() + " is not a list of Quest names!");
			}
		}
		if (config.contains("quests." + questKey + ".requirements.permissions")) {
			if (Quests.checkList(config.getList("quests." + questKey + ".requirements.permissions"), String.class)) {
				reqs.setPermissions(config.getStringList("quests." + questKey + ".requirements.permissions"));
			} else {
				skipQuestProcess("permissions: Requirement for Quest " + quest.getName() + " is not a list of permissions!");
			}
		}
		if (config.contains("quests." + questKey + ".requirements.mcmmo-skills")) {
			if (Quests.checkList(config.getList("quests." + questKey + ".requirements.mcmmo-skills"), String.class)) {
				if (config.contains("quests." + questKey + ".requirements.mcmmo-amounts")) {
					if (Quests.checkList(config.getList("quests." + questKey + ".requirements.mcmmo-amounts"), Integer.class)) {
						List<String> skills = config.getStringList("quests." + questKey + ".requirements.mcmmo-skills");
						List<Integer> amounts = config.getIntegerList("quests." + questKey + ".requirements.mcmmo-amounts");
						if (skills.size() != amounts.size()) {
							skipQuestProcess("mcmmo-skills: and mcmmo-amounts: in requirements: for Quest " + quest.getName() + " are not the same size!");
						}
						reqs.setMcmmoSkills(skills);
						reqs.setMcmmoAmounts(amounts);
					} else {
						skipQuestProcess("mcmmo-amounts: Requirement for Quest " + quest.getName() + " is not a list of numbers!");
					}
				} else {
					skipQuestProcess("Requirements for Quest " + quest.getName() + " is missing mcmmo-amounts:");
				}
			} else {
				skipQuestProcess("mcmmo-skills: Requirement for Quest " + quest.getName() + " is not a list of skills!");
			}
		}
		if (config.contains("quests." + questKey + ".requirements.heroes-primary-class")) {
			String className = config.getString("quests." + questKey + ".requirements.heroes-primary-class");
			HeroClass hc = depends.getHeroes().getClassManager().getClass(className);
			if (hc != null && hc.isPrimary()) {
				reqs.setHeroesPrimaryClass(hc.getName());
			} else if (hc != null) {
				skipQuestProcess("heroes-primary-class: Requirement for Quest " + quest.getName() + " is not a primary Heroes class!");
			} else {
				skipQuestProcess("heroes-primary-class: Requirement for Quest " + quest.getName() + " is not a valid Heroes class!");
			}
		}
		if (config.contains("quests." + questKey + ".requirements.heroes-secondary-class")) {
			String className = config.getString("quests." + questKey + ".requirements.heroes-secondary-class");
			HeroClass hc = depends.getHeroes().getClassManager().getClass(className);
			if (hc != null && hc.isSecondary()) {
				reqs.setHeroesSecondaryClass(hc.getName());
			} else if (hc != null) {
				skipQuestProcess("heroes-secondary-class: Requirement for Quest " + quest.getName() + " is not a secondary Heroes class!");
			} else {
				skipQuestProcess("heroes-secondary-class: Requirement for Quest " + quest.getName() + " is not a valid Heroes class!");
			}
		}
	}
	
	private void loadQuestPlanner(FileConfiguration config, ConfigurationSection questsSection, Quest quest, String questKey) throws SkipQuest {
		Planner pln = quest.getPlanner();
		if (config.contains("quests." + questKey + ".planner.start")) {
			pln.setStart(config.getString("quests." + questKey + ".planner.start"));
		} /*else {
			skipQuestProcess("Planner for Quest " + quest.getName() + " is missing start:");
		}*/
		if (config.contains("quests." + questKey + ".planner.end")) {
			pln.setEnd(config.getString("quests." + questKey + ".planner.end"));
		} /*else {
			skipQuestProcess("Planner for Quest " + quest.getName() + " is missing end:");
		}*/
		if (config.contains("quests." + questKey + ".planner.repeat")) {
			if (config.getInt("quests." + questKey + ".planner.repeat", -999) != -999) {
				pln.setRepeat(config.getInt("quests." + questKey + ".planner.repeat") * 1000);
			} else {
				skipQuestProcess("repeat: for Quest " + quest.getName() + " is not a number!");
			}
		}
		if (config.contains("quests." + questKey + ".planner.cooldown")) {
			if (config.getInt("quests." + questKey + ".planner.cooldown", -999) != -999) {
				pln.setCooldown(config.getInt("quests." + questKey + ".planner.cooldown") * 1000);
			} else {
				skipQuestProcess("cooldown: for Quest " + quest.getName() + " is not a number!");
			}
		}
	}

	private void skipQuestProcess(String[] msgs) throws SkipQuest {
		for (String msg : msgs) {
			if (msg != null) {
				getLogger().severe(msg);
			}
		}
		throw new SkipQuest();
	}

	private void skipQuestProcess(String msg) throws SkipQuest {
		skipQuestProcess(new String[] { msg });
	}



	private boolean regionFound(Quest quest, String region) {
		boolean exists = false;
		for (World world : getServer().getWorlds()) {
			RegionManager rm = depends.getWorldGuardApi().getRegionManager(world);
			if (rm != null) {
				ProtectedRegion pr = rm.getRegion(region);
				if (pr != null) {
					quest.region = region;
					exists = true;
					break;
				}
			}
		}
		return exists;
	}

	private void processStages(Quest quest, FileConfiguration config, String questKey) throws StageFailedException {
		ConfigurationSection questStages = config.getConfigurationSection("quests." + questKey + ".stages.ordered");
		for (String s2 : questStages.getKeys(false)) {
			Stage oStage = new Stage();
			List<String> breaknames = new LinkedList<String>();
			List<Integer> breakamounts = new LinkedList<Integer>();
			List<Short> breakdurability = new LinkedList<Short>();
			List<String> damagenames = new LinkedList<String>();
			List<Integer> damageamounts = new LinkedList<Integer>();
			List<Short> damagedurability = new LinkedList<Short>();
			List<String> placenames = new LinkedList<String>();
			List<Integer> placeamounts = new LinkedList<Integer>();
			List<Short> placedurability = new LinkedList<Short>();
			List<String> usenames = new LinkedList<String>();
			List<Integer> useamounts = new LinkedList<Integer>();
			List<Short> usedurability = new LinkedList<Short>();
			List<String> cutnames = new LinkedList<String>();
			List<Integer> cutamounts = new LinkedList<Integer>();
			List<Short> cutdurability = new LinkedList<Short>();
			List<EntityType> mobsToKill = new LinkedList<EntityType>();
			List<Integer> mobNumToKill = new LinkedList<Integer>();
			List<Location> locationsToKillWithin = new LinkedList<Location>();
			List<Integer> radiiToKillWithin = new LinkedList<Integer>();
			List<String> areaNames = new LinkedList<String>();
			List<String> itemsToCraft = new LinkedList<String>();
			List<Enchantment> enchantments = new LinkedList<Enchantment>();
			List<Material> itemsToEnchant = new LinkedList<Material>();
			List<Integer> amountsToEnchant = new LinkedList<Integer>();
			List<Integer> npcIdsToTalkTo = new LinkedList<Integer>();
			List<String> itemsToDeliver= new LinkedList<String>();
			List<Integer> itemDeliveryTargetIds = new LinkedList<Integer>();
			List<String> deliveryMessages = new LinkedList<String>();
			List<Integer> npcIdsToKill = new LinkedList<Integer>();
			List<Integer> npcAmountsToKill = new LinkedList<Integer>();
			// Denizen script load
			if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".script-to-run")) {
				if (ScriptRegistry.containsScript(config.getString("quests." + questKey + ".stages.ordered." + s2 + ".script-to-run"))) {
					trigger = new DenizenTrigger();
					oStage.script = config.getString("quests." + questKey + ".stages.ordered." + s2 + ".script-to-run");
				} else {
					stageFailed("script-to-run: in Stage " + s2 + " of Quest " + quest.getName() + " is not a Denizen script!");
				}
			}
			if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".break-block-names")) {
				if (checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".break-block-names"), String.class)) {
					breaknames = config.getStringList("quests." + questKey + ".stages.ordered." + s2 + ".break-block-names");
				} else {
					stageFailed("break-block-names: in Stage " + s2 + " of Quest " + quest.getName() + " is not a list of strings!");
				}
				if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".break-block-amounts")) {
					if (checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".break-block-amounts"), Integer.class)) {
						breakamounts = config.getIntegerList("quests." + questKey + ".stages.ordered." + s2 + ".break-block-amounts");
					} else {
						stageFailed("break-block-amounts: in Stage " + s2 + " of Quest " + quest.getName() + " is not a list of numbers!");
					}
				} else {
					stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing break-block-amounts:");
				}
				if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".break-block-durability")) {
					if (checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".break-block-durability"), Integer.class)) {
						breakdurability = config.getShortList("quests." + questKey + ".stages.ordered." + s2 + ".break-block-durability");
					} else {
						stageFailed("break-block-durability: in Stage " + s2 + " of Quest " + quest.getName() + " is not a list of numbers!");
					}
				} else {
					stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing break-block-durability:");
				}
			}
			for (String s : breaknames) {
				ItemStack is;
				if (breakdurability.get(breaknames.indexOf(s)) != -1) {
					is = ItemUtil.processItemStack(s, breakamounts.get(breaknames.indexOf(s)), breakdurability.get(breaknames.indexOf(s)));
				} else {
					// Legacy
					is = ItemUtil.processItemStack(s, breakamounts.get(breaknames.indexOf(s)), (short) 0);
				}
				if (Material.matchMaterial(s) != null) {
					oStage.blocksToBreak.add(is);
				} else {
					stageFailed("" + s + " inside break-block-names: inside Stage " + s2 + " of Quest " + quest.getName() + " is not a valid item name!");
				}
			}
			if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".damage-block-names")) {
				if (checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".damage-block-names"), String.class)) {
					damagenames = config.getStringList("quests." + questKey + ".stages.ordered." + s2 + ".damage-block-names");
				} else {
					stageFailed("damage-block-names: in Stage " + s2 + " of Quest " + quest.getName() + " is not a list of strings!");
				}
				if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".damage-block-amounts")) {
					if (checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".damage-block-amounts"), Integer.class)) {
						damageamounts = config.getIntegerList("quests." + questKey + ".stages.ordered." + s2 + ".damage-block-amounts");
					} else {
						stageFailed("damage-block-amounts: in Stage " + s2 + " of Quest " + quest.getName() + " is not a list of numbers!");
					}
				} else {
					stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing damage-block-amounts:");
				}
				if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".damage-block-durability")) {
					if (checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".damage-block-durability"), Integer.class)) {
						damagedurability = config.getShortList("quests." + questKey + ".stages.ordered." + s2 + ".damage-block-durability");
					} else {
						stageFailed("damage-block-durability: in Stage " + s2 + " of Quest " + quest.getName() + " is not a list of numbers!");
					}
				} else {
					stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing damage-block-durability:");
				}
			}
			for (String s : damagenames) {
				ItemStack is;
				if (damagedurability.get(damagenames.indexOf(s)) != -1) {
					is = ItemUtil.processItemStack(s, damageamounts.get(damagenames.indexOf(s)), damagedurability.get(damagenames.indexOf(s)));
				} else {
					// Legacy
					is = ItemUtil.processItemStack(s, damageamounts.get(damagenames.indexOf(s)), (short) 0);
				}
				if (Material.matchMaterial(s) != null) {
					oStage.blocksToDamage.add(is);
				} else {
					stageFailed("" + s + " inside damage-block-names: inside Stage " + s2 + " of Quest " + quest.getName() + " is not a valid item name!");
				}
			}
			if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".place-block-names")) {
				if (checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".place-block-names"), String.class)) {
					placenames = config.getStringList("quests." + questKey + ".stages.ordered." + s2 + ".place-block-names");
				} else {
					stageFailed("place-block-names: in Stage " + s2 + " of Quest " + quest.getName() + " is not a list of strings!");
				}
				if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".place-block-amounts")) {
					if (checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".place-block-amounts"), Integer.class)) {
						placeamounts = config.getIntegerList("quests." + questKey + ".stages.ordered." + s2 + ".place-block-amounts");
					} else {
						stageFailed("place-block-amounts: in Stage " + s2 + " of Quest " + quest.getName() + " is not a list of numbers!");
					}
				} else {
					stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing place-block-amounts:");
				}
				if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".place-block-durability")) {
					if (checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".place-block-durability"), Integer.class)) {
						placedurability = config.getShortList("quests." + questKey + ".stages.ordered." + s2 + ".place-block-durability");
					} else {
						stageFailed("place-block-durability: in Stage " + s2 + " of Quest " + quest.getName() + " is not a list of numbers!");
					}
				} else {
					stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing place-block-durability:");
				}
			}
			for (String s : placenames) {
				ItemStack is;
				if (placedurability.get(placenames.indexOf(s)) != -1) {
					is = ItemUtil.processItemStack(s, placeamounts.get(placenames.indexOf(s)), placedurability.get(placenames.indexOf(s)));
				} else {
					// Legacy
					is = ItemUtil.processItemStack(s, placeamounts.get(placenames.indexOf(s)), (short) 0);
				}
				if (Material.matchMaterial(s) != null) {
					oStage.blocksToPlace.add(is);
				} else {
					stageFailed("" + s + " inside place-block-names: inside Stage " + s2 + " of Quest " + quest.getName() + " is not a valid item name!");
				}
			}
			if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".use-block-names")) {
				if (checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".use-block-names"), String.class)) {
					usenames = config.getStringList("quests." + questKey + ".stages.ordered." + s2 + ".use-block-names");
				} else {
					stageFailed("use-block-names: in Stage " + s2 + " of Quest " + quest.getName() + " is not a list of strings!");
				}
				if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".use-block-amounts")) {
					if (checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".use-block-amounts"), Integer.class)) {
						useamounts = config.getIntegerList("quests." + questKey + ".stages.ordered." + s2 + ".use-block-amounts");
					} else {
						stageFailed("use-block-amounts: in Stage " + s2 + " of Quest " + quest.getName() + " is not a list of numbers!");
					}
				} else {
					stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing use-block-amounts:");
				}
				if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".use-block-durability")) {
					if (checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".use-block-durability"), Integer.class)) {
						usedurability = config.getShortList("quests." + questKey + ".stages.ordered." + s2 + ".use-block-durability");
					} else {
						stageFailed("use-block-durability: in Stage " + s2 + " of Quest " + quest.getName() + " is not a list of numbers!");
					}
				} else {
					stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing use-block-durability:");
				}
			}
			for (String s : usenames) {
				ItemStack is;
				if (usedurability.get(usenames.indexOf(s)) != -1) {
					is = ItemUtil.processItemStack(s, useamounts.get(usenames.indexOf(s)), usedurability.get(usenames.indexOf(s)));
				} else {
					// Legacy
					is = ItemUtil.processItemStack(s, useamounts.get(usenames.indexOf(s)), (short) 0);
				}
				if (Material.matchMaterial(s) != null) {
					oStage.blocksToUse.add(is);
				} else {
					stageFailed("" + s + " inside use-block-names: inside Stage " + s2 + " of Quest " + quest.getName() + " is not a valid item name!");
				}
			}
			if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".cut-block-names")) {
				if (checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".cut-block-names"), String.class)) {
					cutnames = config.getStringList("quests." + questKey + ".stages.ordered." + s2 + ".cut-block-names");
				} else {
					stageFailed("cut-block-names: in Stage " + s2 + " of Quest " + quest.getName() + " is not a list of strings!");
				}
				if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".cut-block-amounts")) {
					if (checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".cut-block-amounts"), Integer.class)) {
						cutamounts = config.getIntegerList("quests." + questKey + ".stages.ordered." + s2 + ".cut-block-amounts");
					} else {
						stageFailed("cut-block-amounts: in Stage " + s2 + " of Quest " + quest.getName() + " is not a list of numbers!");
					}
				} else {
					stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing cut-block-amounts:");
				}
				if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".cut-block-durability")) {
					if (checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".cut-block-durability"), Integer.class)) {
						cutdurability = config.getShortList("quests." + questKey + ".stages.ordered." + s2 + ".cut-block-durability");
					} else {
						stageFailed("cut-block-durability: in Stage " + s2 + " of Quest " + quest.getName() + " is not a list of numbers!");
					}
				} else {
					stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing cut-block-durability:");
				}
			}
			for (String s : cutnames) {
				ItemStack is;
				if (cutdurability.get(cutnames.indexOf(s)) != -1) {
					is = ItemUtil.processItemStack(s, cutamounts.get(cutnames.indexOf(s)), cutdurability.get(cutnames.indexOf(s)));
				} else {
					// Legacy
					is = ItemUtil.processItemStack(s, cutamounts.get(cutnames.indexOf(s)), (short) 0);
				}
				if (Material.matchMaterial(s) != null) {
					oStage.blocksToCut.add(is);
				} else {
					stageFailed("" + s + " inside cut-block-names: inside Stage " + s2 + " of Quest " + quest.getName() + " is not a valid item name!");
				}
			}
			if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".items-to-craft")) {
				if (checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".items-to-craft"), String.class)) {
					itemsToCraft = config.getStringList("quests." + questKey + ".stages.ordered." + s2 + ".items-to-craft");
					for (String item : itemsToCraft) {
						ItemStack is = ItemUtil.readItemStack("" + item);
						if (is != null) {
							oStage.getItemsToCraft().add(is);
						} else {
							stageFailed("" + item + " inside items-to-craft: inside Stage " + s2 + " of Quest " + quest.getName() + " is not formatted properly!");
						}
					}
				} else {
					stageFailed("items-to-craft: in Stage " + s2 + " of Quest " + quest.getName() + " is not formatted properly!");
				}
			}
			if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".enchantments")) {
				if (Quests.checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".enchantments"), String.class)) {
					for (String enchant : config.getStringList("quests." + questKey + ".stages.ordered." + s2 + ".enchantments")) {
						Enchantment e = Quests.getEnchantment(enchant);
						if (e != null) {
							enchantments.add(e);
						} else {
							stageFailed("" + enchant + " inside enchantments: inside Stage " + s2 + " of Quest " + quest.getName() + " is not a valid enchantment!");
						}
					}
				} else {
					stageFailed("enchantments: in Stage " + s2 + " of Quest " + quest.getName() + " is not a list of enchantment names!");
				}
				if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".enchantment-item-names")) {
					if (Quests.checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".enchantment-item-names"), String.class)) {
						for (String item : config.getStringList("quests." + questKey + ".stages.ordered." + s2 + ".enchantment-item-names")) {
							if (Material.matchMaterial(item) != null) {
								itemsToEnchant.add(Material.matchMaterial(item));
							} else {
								stageFailed("" + item + " inside enchantment-item-names: inside Stage " + s2 + " of Quest " + quest.getName() + " is not a valid item name!");
							}
						}
					} else {
						stageFailed("enchantment-item-names: in Stage " + s2 + " of Quest " + quest.getName() + " is not a valid item name!");
					}
				} else {
					stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing enchantment-item-names:");
				}
				if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".enchantment-amounts")) {
					if (Quests.checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".enchantment-amounts"), Integer.class)) {
						amountsToEnchant = config.getIntegerList("quests." + questKey + ".stages.ordered." + s2 + ".enchantment-amounts");
					} else {
						stageFailed("enchantment-amounts: in Stage " + s2 + " of Quest " + quest.getName() + " is not a list of numbers!");
					}
				} else {
					stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing enchantment-amounts:");
				}
			}
			if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".fish-to-catch")) {
				if (config.getInt("quests." + questKey + ".stages.ordered." + s2 + ".fish-to-catch", -999) != -999) {
					oStage.fishToCatch = config.getInt("quests." + questKey + ".stages.ordered." + s2 + ".fish-to-catch");
				} else {
					stageFailed("fish-to-catch: inside Stage " + s2 + " of Quest " + quest.getName() + " is not a number!");
				}
			}
			if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".players-to-kill")) {
				if (config.getInt("quests." + questKey + ".stages.ordered." + s2 + ".players-to-kill", -999) != -999) {
					oStage.playersToKill = config.getInt("quests." + questKey + ".stages.ordered." + s2 + ".players-to-kill");
				} else {
					stageFailed("players-to-kill: inside Stage " + s2 + " of Quest " + quest.getName() + " is not a number!");
				}
			}
			if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".npc-ids-to-talk-to")) {
				if (checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".npc-ids-to-talk-to"), Integer.class)) {
					npcIdsToTalkTo = config.getIntegerList("quests." + questKey + ".stages.ordered." + s2 + ".npc-ids-to-talk-to");
					for (int i : npcIdsToTalkTo) {
						if (CitizensAPI.getNPCRegistry().getById(i) != null) {
							questNpcs.add(CitizensAPI.getNPCRegistry().getById(i));
						} else {
							stageFailed("" + i + " inside npc-ids-to-talk-to: inside Stage " + s2 + " of Quest " + quest.getName() + " is not a valid NPC id!");
						}
					}
				} else {
					stageFailed("npc-ids-to-talk-to: in Stage " + s2 + " of Quest " + quest.getName() + " is not a list of numbers!");
				}
			}
			if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".items-to-deliver")) {
				if (checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".items-to-deliver"), String.class)) {
					if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".npc-delivery-ids")) {
						if (checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".npc-delivery-ids"), Integer.class)) {
							if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".delivery-messages")) {
								itemsToDeliver = config.getStringList("quests." + questKey + ".stages.ordered." + s2 + ".items-to-deliver");
								itemDeliveryTargetIds = config.getIntegerList("quests." + questKey + ".stages.ordered." + s2 + ".npc-delivery-ids");
								deliveryMessages.addAll(config.getStringList("quests." + questKey + ".stages.ordered." + s2 + ".delivery-messages"));
								int index = 0;
								for (String item : itemsToDeliver) {
									ItemStack is = ItemUtil.readItemStack("" + item);
									int npcId = itemDeliveryTargetIds.get(index);
									index++;
									if (is != null) {
										NPC npc = CitizensAPI.getNPCRegistry().getById(npcId);
										if (npc != null) {
											oStage.getItemsToDeliver().add(is);
											oStage.getItemDeliveryTargets().add(npcId);
											oStage.deliverMessages.addAll(deliveryMessages);
										} else {
											stageFailed("" + npcId + " inside npc-delivery-ids: inside Stage " + s2 + " of Quest " + quest.getName() + " is not a valid NPC id!");
										}
									} else {
										stageFailed("" + item + " inside items-to-deliver: inside Stage " + s2 + " of Quest " + quest.getName() + " is not formatted properly!");
									}
								}
							} else {
								stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing delivery-messages:");
							}
						} else {
							stageFailed("npc-delivery-ids: in Stage " + s2 + " of Quest " + ChatColor.DARK_PURPLE + quest.getName() + " is not a list of NPC ids!");
						}
					} else {
						stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing npc-delivery-ids:");
					}
				} else {
					stageFailed("items-to-deliver: in Stage " + s2 + " of Quest " + quest.getName() + " is not formatted properly!");
				}
			}
			if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".npc-ids-to-kill")) {
				if (checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".npc-ids-to-kill"), Integer.class)) {
					if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".npc-kill-amounts")) {
						if (checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".npc-kill-amounts"), Integer.class)) {
							npcIdsToKill = config.getIntegerList("quests." + questKey + ".stages.ordered." + s2 + ".npc-ids-to-kill");
							npcAmountsToKill = config.getIntegerList("quests." + questKey + ".stages.ordered." + s2 + ".npc-kill-amounts");
							for (int i : npcIdsToKill) {
								if (CitizensAPI.getNPCRegistry().getById(i) != null) {
									if (npcAmountsToKill.get(npcIdsToKill.indexOf(i)) > 0) {
										oStage.citizensToKill.add(i);
										oStage.citizenNumToKill.add(npcAmountsToKill.get(npcIdsToKill.indexOf(i)));
										questNpcs.add(CitizensAPI.getNPCRegistry().getById(i));
									} else {
										stageFailed("" + npcAmountsToKill.get(npcIdsToKill.indexOf(i)) + " inside npc-kill-amounts: inside Stage " + s2 + " of Quest " + quest.getName() + " is not a positive number!");
									}
								} else {
									stageFailed("" + i + " inside npc-ids-to-kill: inside Stage " + s2 + " of Quest " + quest.getName() + " is not a valid NPC id!");
								}
							}
						} else {
							stageFailed("npc-kill-amounts: in Stage " + s2 + " of Quest " + quest.getName() + " is not a list of numbers!");
						}
					} else {
						stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing npc-kill-amounts:");
					}
				} else {
					stageFailed("npc-ids-to-kill: in Stage " + s2 + " of Quest " + quest.getName() + " is not a list of numbers!");
				}
			}
			if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".mobs-to-kill")) {
				if (Quests.checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".mobs-to-kill"), String.class)) {
					List<String> mobNames = config.getStringList("quests." + questKey + ".stages.ordered." + s2 + ".mobs-to-kill");
					for (String mob : mobNames) {
						EntityType type = getMobType(mob);
						if (type != null) {
							mobsToKill.add(type);
						} else {
							stageFailed("" + mob + " inside mobs-to-kill: inside Stage " + s2 + " of Quest " + quest.getName() + " is not a valid mob name!");
						}
					}
				} else {
					stageFailed("mobs-to-kill: in Stage " + s2 + " of Quest " + quest.getName() + " is not a list of mob names!");
				}
				if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".mob-amounts")) {
					if (Quests.checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".mob-amounts"), Integer.class)) {
						for (int i : config.getIntegerList("quests." + questKey + ".stages.ordered." + s2 + ".mob-amounts")) {
							mobNumToKill.add(i);
						}
					} else {
						stageFailed("mob-amounts: in Stage " + s2 + " of Quest " + quest.getName() + " is not a list of numbers!");
					}
				} else {
					stageFailed("Stage " + s2 + " of Quest " + ChatColor.DARK_PURPLE + quest.getName() + " is missing mob-amounts:");
				}
			}
			if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".locations-to-kill")) {
				if (Quests.checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".locations-to-kill"), String.class)) {
					List<String> locations = config.getStringList("quests." + questKey + ".stages.ordered." + s2 + ".locations-to-kill");
					for (String loc : locations) {
						if (getLocation(loc) != null) {
							locationsToKillWithin.add(getLocation(loc));
						} else {
							stageFailed(new String[] { "" + loc + " inside locations-to-kill: inside Stage " + s2 + " of Quest " + quest.getName() + " is not in proper location format!", 
									"Proper location format is: \"WorldName x y z\"" });
						}
					}
				} else {
					stageFailed("locations-to-kill: in Stage " + s2 + " of Quest " + quest.getName() + " is not a list of locations!");
				}
				if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".kill-location-radii")) {
					if (Quests.checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".kill-location-radii"), Integer.class)) {
						List<Integer> radii = config.getIntegerList("quests." + questKey + ".stages.ordered." + s2 + ".kill-location-radii");
						for (int i : radii) {
							radiiToKillWithin.add(i);
						}
					} else {
						stageFailed("kill-location-radii: in Stage " + s2 + " of Quest " + quest.getName() + " is not a list of numbers!");
					}
				} else {
					stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing kill-location-radii:");
				}
				if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".kill-location-names")) {
					if (Quests.checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".kill-location-names"), String.class)) {
						List<String> locationNames = config.getStringList("quests." + questKey + ".stages.ordered." + s2 + ".kill-location-names");
						for (String name : locationNames) {
							areaNames.add(name);
						}
					} else {
						stageFailed("kill-location-names: in Stage " + s2 + " of Quest " + quest.getName() + " is not a list of names!");
					}
				} else {
					stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing kill-location-names:");
				}
			}
			oStage.mobsToKill.addAll(mobsToKill);
			oStage.mobNumToKill.addAll(mobNumToKill);
			oStage.locationsToKillWithin.addAll(locationsToKillWithin);
			oStage.radiiToKillWithin.addAll(radiiToKillWithin);
			oStage.killNames.addAll(areaNames);
			Map<Map<Enchantment, Material>, Integer> enchants = new HashMap<Map<Enchantment, Material>, Integer>();
			for (Enchantment e : enchantments) {
				Map<Enchantment, Material> map = new HashMap<Enchantment, Material>();
				map.put(e, itemsToEnchant.get(enchantments.indexOf(e)));
				enchants.put(map, amountsToEnchant.get(enchantments.indexOf(e)));
			}
			oStage.itemsToEnchant = enchants;
			if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".locations-to-reach")) {
				if (Quests.checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".locations-to-reach"), String.class)) {
					List<String> locations = config.getStringList("quests." + questKey + ".stages.ordered." + s2 + ".locations-to-reach");
					for (String loc : locations) {
						if (getLocation(loc) != null) {
							oStage.locationsToReach.add(getLocation(loc));
						} else {
							stageFailed(new String[] { "" + loc + " inside locations-to-reach inside Stage " + s2 + " of Quest " + quest.getName() + " is not in proper location format!", 
									"Proper location format is: \"WorldName x y z\"" });
						}
					}
				} else {
					stageFailed("locations-to-reach: in Stage " + s2 + " of Quest " + quest.getName() + " is not a list of locations!");
				}
				if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".reach-location-radii")) {
					if (Quests.checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".reach-location-radii"), Integer.class)) {
						List<Integer> radii = config.getIntegerList("quests." + questKey + ".stages.ordered." + s2 + ".reach-location-radii");
						for (int i : radii) {
							oStage.radiiToReachWithin.add(i);
						}
					} else {
						stageFailed("reach-location-radii: in Stage " + s2 + " of Quest " + quest.getName() + " is not a list of numbers!");
					}
				} else {
					stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing reach-location-radii:");
				}
				if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".reach-location-names")) {
					if (Quests.checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".reach-location-names"), String.class)) {
						List<String> locationNames = config.getStringList("quests." + questKey + ".stages.ordered." + s2 + ".reach-location-names");
						for (String name : locationNames) {
							oStage.locationNames.add(name);
						}
					} else {
						stageFailed("reach-location-names: in Stage " + s2 + " of Quest " + quest.getName() + " is not a list of names!");
					}
				} else {
					stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing reach-location-names:");
				}
			}
			if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".mobs-to-tame")) {
				if (Quests.checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".mobs-to-tame"), String.class)) {
					if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".mob-tame-amounts")) {
						if (Quests.checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".mob-tame-amounts"), Integer.class)) {
							List<String> mobs = config.getStringList("quests." + questKey + ".stages.ordered." + s2 + ".mobs-to-tame");
							List<Integer> mobAmounts = config.getIntegerList("quests." + questKey + ".stages.ordered." + s2 + ".mob-tame-amounts");
							for (String mob : mobs) {
								if (Tameable.class.isAssignableFrom(EntityType.valueOf(mob.toUpperCase()).getEntityClass())) {
									oStage.mobsToTame.put(EntityType.valueOf(mob.toUpperCase()), mobAmounts.get(mobs.indexOf(mob)));
								} else {
									stageFailed("" + mob + " inside mobs-to-tame: inside Stage " + s2 + " of Quest " + quest.getName() + " is not a valid tameable mob!");
								}
							}
						} else {
							stageFailed("mob-tame-amounts: in Stage " + s2 + " of Quest " + quest.getName() + " is not a list of numbers!");
						}
					} else {
						stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing mob-tame-amounts:");
					}
				} else {
					stageFailed("mobs-to-tame: in Stage " + s2 + " of Quest " + quest.getName() + " is not a list of mob names!");
				}
			}
			if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".sheep-to-shear")) {
				if (Quests.checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".sheep-to-shear"), String.class)) {
					if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".sheep-amounts")) {
						if (Quests.checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".sheep-amounts"), Integer.class)) {
							List<String> sheep = config.getStringList("quests." + questKey + ".stages.ordered." + s2 + ".sheep-to-shear");
							List<Integer> shearAmounts = config.getIntegerList("quests." + questKey + ".stages.ordered." + s2 + ".sheep-amounts");
							for (String color : sheep) {
								if (color.equalsIgnoreCase(Lang.get("COLOR_BLACK"))) {
									oStage.sheepToShear.put(DyeColor.BLACK, shearAmounts.get(sheep.indexOf(color)));
								} else if (color.equalsIgnoreCase(Lang.get("COLOR_BLUE"))) {
									oStage.sheepToShear.put(DyeColor.BLUE, shearAmounts.get(sheep.indexOf(color)));
								} else if (color.equalsIgnoreCase(Lang.get("COLOR_BROWN"))) {
									oStage.sheepToShear.put(DyeColor.BROWN, shearAmounts.get(sheep.indexOf(color)));
								} else if (color.equalsIgnoreCase(Lang.get("COLOR_CYAN"))) {
									oStage.sheepToShear.put(DyeColor.CYAN, shearAmounts.get(sheep.indexOf(color)));
								} else if (color.equalsIgnoreCase(Lang.get("COLOR_GRAY"))) {
									oStage.sheepToShear.put(DyeColor.GRAY, shearAmounts.get(sheep.indexOf(color)));
								} else if (color.equalsIgnoreCase(Lang.get("COLOR_GREEN"))) {
									oStage.sheepToShear.put(DyeColor.GREEN, shearAmounts.get(sheep.indexOf(color)));
								} else if (color.equalsIgnoreCase(Lang.get("COLOR_LIGHT_BLUE"))) {
									oStage.sheepToShear.put(DyeColor.LIGHT_BLUE, shearAmounts.get(sheep.indexOf(color)));
								} else if (color.equalsIgnoreCase(Lang.get("COLOR_LIME"))) {
									oStage.sheepToShear.put(DyeColor.LIME, shearAmounts.get(sheep.indexOf(color)));
								} else if (color.equalsIgnoreCase(Lang.get("COLOR_MAGENTA"))) {
									oStage.sheepToShear.put(DyeColor.MAGENTA, shearAmounts.get(sheep.indexOf(color)));
								} else if (color.equalsIgnoreCase(Lang.get("COLOR_ORANGE"))) {
									oStage.sheepToShear.put(DyeColor.ORANGE, shearAmounts.get(sheep.indexOf(color)));
								} else if (color.equalsIgnoreCase(Lang.get("COLOR_PINK"))) {
									oStage.sheepToShear.put(DyeColor.PINK, shearAmounts.get(sheep.indexOf(color)));
								} else if (color.equalsIgnoreCase(Lang.get("COLOR_PURPLE"))) {
									oStage.sheepToShear.put(DyeColor.PURPLE, shearAmounts.get(sheep.indexOf(color)));
								} else if (color.equalsIgnoreCase(Lang.get("COLOR_RED"))) {
									oStage.sheepToShear.put(DyeColor.RED, shearAmounts.get(sheep.indexOf(color)));
								} else if (color.equalsIgnoreCase(Lang.get("COLOR_SILVER"))) {
									// 1.13 changed DyeColor.SILVER -> DyeColor.LIGHT_GRAY
									oStage.sheepToShear.put(DyeColor.getByColor(Color.SILVER), shearAmounts.get(sheep.indexOf(color)));
								} else if (color.equalsIgnoreCase(Lang.get("COLOR_WHITE"))) {
									oStage.sheepToShear.put(DyeColor.WHITE, shearAmounts.get(sheep.indexOf(color)));
								} else if (color.equalsIgnoreCase(Lang.get("COLOR_YELLOW"))) {
									oStage.sheepToShear.put(DyeColor.YELLOW, shearAmounts.get(sheep.indexOf(color)));
								} else {
									stageFailed("" + color + " inside sheep-to-shear: inside Stage " + s2 + " of Quest " + quest.getName() + " is not a valid color!");
								}
							}
						} else {
							stageFailed("sheep-amounts: in Stage " + s2 + " of Quest " + quest.getName() + " is not a list of numbers!");
						}
					} else {
						stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing sheep-amounts:");
					}
				} else {
					stageFailed("sheep-to-shear: in Stage " + s2 + " of Quest " + quest.getName() + " is not a list of colors!");
				}
			}
			if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".password-displays")) {
				List<String> displays = config.getStringList("quests." + questKey + ".stages.ordered." + s2 + ".password-displays");
				if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".password-phrases")) {
					List<String> phrases = config.getStringList("quests." + questKey + ".stages.ordered." + s2 + ".password-phrases");
					if (displays.size() == phrases.size()) {
						for (int passIndex = 0; passIndex < displays.size(); passIndex++) {
							oStage.passwordDisplays.add(displays.get(passIndex));
							LinkedList<String> answers = new LinkedList<String>();
							answers.addAll(Arrays.asList(phrases.get(passIndex).split("\\|")));
							oStage.passwordPhrases.add(answers);
						}
					} else {
						stageFailed("password-displays and password-phrases in Stage " + s2 + " of Quest " + quest.getName() + " are not the same size!");
					}
				} else {
					stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing password-phrases!");
				}
			}
			if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".objective-override")) {
				oStage.objectiveOverride = config.getString("quests." + questKey + ".stages.ordered." + s2 + ".objective-override");
			}
			if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".start-event")) {
				Event evt = Event.loadEvent(config.getString("quests." + questKey + ".stages.ordered." + s2 + ".start-event"), this);
				if (evt != null) {
					oStage.startEvent = evt;
				} else {
					stageFailed("start-event: in Stage " + s2 + " of Quest " + quest.getName() + " failed to load.");
				}
			}
			if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".finish-event")) {
				Event evt = Event.loadEvent(config.getString("quests." + questKey + ".stages.ordered." + s2 + ".finish-event"), this);
				if (evt != null) {
					oStage.finishEvent = evt;
				} else {
					stageFailed("finish-event: in Stage " + s2 + " of Quest " + quest.getName() + " failed to load.");
				}
			}
			if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".death-event")) {
				Event evt = Event.loadEvent(config.getString("quests." + questKey + ".stages.ordered." + s2 + ".death-event"), this);
				if (evt != null) {
					oStage.deathEvent = evt;
				} else {
					stageFailed("death-event: in Stage " + s2 + " of Quest " + quest.getName() + " failed to load.");
				}
			}
			if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".disconnect-event")) {
				Event evt = Event.loadEvent(config.getString("quests." + questKey + ".stages.ordered." + s2 + ".disconnect-event"), this);
				if (evt != null) {
					oStage.disconnectEvent = evt;
				} else {
					stageFailed("disconnect-event: in Stage " + s2 + " of Quest " + quest.getName() + " failed to load.");
				}
			}
			if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".chat-events")) {
				if (config.isList("quests." + questKey + ".stages.ordered." + s2 + ".chat-events")) {
					if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".chat-event-triggers")) {
						if (config.isList("quests." + questKey + ".stages.ordered." + s2 + ".chat-event-triggers")) {
							List<String> chatEvents = config.getStringList("quests." + questKey + ".stages.ordered." + s2 + ".chat-events");
							List<String> chatEventTriggers = config.getStringList("quests." + questKey + ".stages.ordered." + s2 + ".chat-event-triggers");
							boolean loadEventFailed = false;
							for (int i = 0; i < chatEvents.size(); i++) {
								Event evt = Event.loadEvent(chatEvents.get(i), this);
								if (evt != null) {
									oStage.chatEvents.put(chatEventTriggers.get(i), evt);
								} else {
									loadEventFailed = true;
									stageFailed("" + chatEvents.get(i) + " inside of chat-events: in Stage " + s2 + " of Quest " + quest.getName() + " failed to load.");
								}
							}
							if (loadEventFailed) {
								break;
							}
						} else {
							stageFailed("chat-event-triggers in Stage " + s2 + " of Quest " + quest.getName() + " is not in list format!");
						}
					} else {
						stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing chat-event-triggers!");
					}
				} else {
					stageFailed("chat-events in Stage " + s2 + " of Quest " + quest.getName() + " is not in list format!");
				}
			}
			if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".command-events")) {
				if (config.isList("quests." + questKey + ".stages.ordered." + s2 + ".command-events")) {
					if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".command-event-triggers")) {
						if (config.isList("quests." + questKey + ".stages.ordered." + s2 + ".command-event-triggers")) {
							List<String> commandEvents = config.getStringList("quests." + questKey + ".stages.ordered." + s2 + ".command-events");
							List<String> commandEventTriggers = config.getStringList("quests." + questKey + ".stages.ordered." + s2 + ".command-event-triggers");
							boolean loadEventFailed = false;
							for (int i = 0; i < commandEvents.size(); i++) {
								Event evt = Event.loadEvent(commandEvents.get(i), this);
								if (evt != null) {
									oStage.commandEvents.put(commandEventTriggers.get(i), evt);
								} else {
									loadEventFailed = true;
									stageFailed("" + commandEvents.get(i) + " inside of command-events: in Stage " + s2 + " of Quest " + quest.getName() + " failed to load.");
								}
							}
							if (loadEventFailed) {
								break;
							}
						} else {
							stageFailed("command-event-triggers in Stage " + s2 + " of Quest " + quest.getName() + " is not in list format!");
						}
					} else {
						stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing command-event-triggers!");
					}
				} else {
					stageFailed("command-events in Stage " + s2 + " of Quest " + quest.getName() + " is not in list format!");
				}
			}
			if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".delay")) {
				if (config.getLong("quests." + questKey + ".stages.ordered." + s2 + ".delay", -999) != -999) {
					oStage.delay = config.getInt("quests." + questKey + ".stages.ordered." + s2 + ".delay") * 1000;
				} else {
					stageFailed("delay: in Stage " + s2 + " of Quest " + quest.getName() + " is not a number!");
				}
			}
			if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".delay-message")) {
				oStage.delayMessage = config.getString("quests." + questKey + ".stages.ordered." + s2 + ".delay-message");
			}
			if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".start-message")) {
				oStage.startMessage = config.getString("quests." + questKey + ".stages.ordered." + s2 + ".start-message");
			}
			if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".complete-message")) {
				oStage.completeMessage = config.getString("quests." + questKey + ".stages.ordered." + s2 + ".complete-message");
			}
			LinkedList<Integer> ids = new LinkedList<Integer>();
			if (npcIdsToTalkTo != null) {
				ids.addAll(npcIdsToTalkTo);
			}
			oStage.citizensToInteract = ids;
			quest.getStages().add(oStage);
		}
	}
	
	private void loadCustomSections(Quest quest, FileConfiguration config, String questKey) throws StageFailedException, SkipQuest {
		ConfigurationSection questStages = config.getConfigurationSection("quests." + questKey + ".stages.ordered");
		for (String s2 : questStages.getKeys(false)) {
			if (quest == null) {
				getLogger().severe("Unable to load custom objectives because quest for " + questKey + " was null");
				return;
			}
			if (quest.getStage(Integer.valueOf(s2) - 1) == null) {
				getLogger().severe("Unable to load custom objectives because stage" + (Integer.valueOf(s2) - 1) + " for " + quest.getName() + " was null");
				return;
			}
			Stage oStage = quest.getStage(Integer.valueOf(s2) - 1);
			oStage.customObjectives=new LinkedList<>();
			oStage.customObjectiveCounts=new LinkedList<>();
			oStage.customObjectiveData=new LinkedList<>();
			oStage.customObjectiveDisplays=new LinkedList<>();
			if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".custom-objectives")) {
				ConfigurationSection sec = config.getConfigurationSection("quests." + questKey + ".stages.ordered." + s2 + ".custom-objectives");
				for (String path : sec.getKeys(false)) {
					String name = sec.getString(path + ".name");
					int count = sec.getInt(path + ".count");
					Optional<CustomObjective> found = Optional.empty();
					for (CustomObjective cr : customObjectives) {
						if (cr.getName().equalsIgnoreCase(name)) {
							found = Optional.of(cr);
							break;
						}
					}
					if (!found.isPresent()) {
						getLogger().warning("Custom objective \"" + name + "\" for Stage " + s2 + " of Quest \"" + quest.getName() + "\" could not be found!");
						continue;
					} else {
						ConfigurationSection sec2 = sec.getConfigurationSection(path + ".data");
						oStage.customObjectives.add(found.get());
							if (count <= 0) {
								oStage.customObjectiveCounts.add(0);
							} else {
								oStage.customObjectiveCounts.add(count);
							}
						for (Entry<String,Object> prompt : found.get().getData()) {
							Entry<String, Object> data = populateCustoms(sec2, prompt);
							oStage.customObjectiveData.add(data);
						}
					}
				}
			}
		}
		Rewards rews = quest.getRewards();
		if (config.contains("quests." + questKey + ".rewards.custom-rewards")) {
			ConfigurationSection sec = config.getConfigurationSection("quests." + questKey + ".rewards.custom-rewards");
			Map<String, Map<String, Object>> temp = new HashMap<String, Map<String, Object>>();
			for (String path : sec.getKeys(false)) {
				String name = sec.getString(path + ".name");
				Optional<CustomReward>found = Optional.empty();
				for (CustomReward cr : customRewards) {
					if (cr.getName().equalsIgnoreCase(name)) {
						found=Optional.of(cr);
						break;
					}
				}
				if (!found.isPresent()) {
					getLogger().warning("Custom reward \"" + name + "\" for Quest \"" + quest.getName() + "\" could not be found!");
					continue;
				} else {
					ConfigurationSection sec2 = sec.getConfigurationSection(path + ".data");
					Map<String, Object> data = populateCustoms(sec2, found.get().getData());
					temp.put(name, data);
				}
			}
			rews.setCustomRewards(temp);
		}
		Requirements reqs = quest.getRequirements();
		if (config.contains("quests." + questKey + ".requirements.custom-requirements")) {
			ConfigurationSection sec = config.getConfigurationSection("quests." + questKey + ".requirements.custom-requirements");
			Map<String, Map<String, Object>> temp = new HashMap<String, Map<String, Object>>();
			for (String path : sec.getKeys(false)) {
				String name = sec.getString(path + ".name");
				Optional<CustomRequirement>found=Optional.empty();
				for (CustomRequirement cr : customRequirements) {
					if (cr.getName().equalsIgnoreCase(name)) {
						found=Optional.of(cr);
						break;
					}
				}
				if (!found.isPresent()) {
					getLogger().warning("Custom requirement \"" + name + "\" for Quest \"" + quest.getName() + "\" could not be found!");
					skipQuestProcess((String) null); // null bc we warn, not severe for this one
				} else {
					ConfigurationSection sec2 = sec.getConfigurationSection(path + ".data");
					Map<String, Object> data = populateCustoms(sec2,found.get().getData());
					temp.put(name, data);
				}
			}
			reqs.setCustomRequirements(temp);
		}
	}
	
	/**
	 * Add possibilty to use fallbacks for customs maps
	 * Avoid null objects in datamap by initialize the entry value with empty string if no fallback present.
	 */
	static Map<String, Object> populateCustoms(ConfigurationSection sec2, Map<String, Object> datamap) {
		Map<String,Object> data = new HashMap<String,Object>();
		if (sec2 != null) {
			for (String key : datamap.keySet()) {
				data.put(key, sec2.contains(key) ? sec2.get(key) : datamap.get(key) != null ? datamap.get(key) : new String());
			}
		}
		return data;
	}
	
	/**
	 * Add possibilty to use fallbacks for customs entries
	 * Avoid null objects in datamap by initialize the entry value with empty string if no fallback present.
	 */
	static Entry<String, Object> populateCustoms(ConfigurationSection sec2, Entry<String, Object> datamap) {
		String key = null;;
		Object value = null;;
		if (sec2 != null) {
			key = datamap.getKey();
			value = datamap.getValue();
		}
		return new AbstractMap.SimpleEntry<String, Object>(key, sec2.contains(key) ? sec2.get(key) : value != null ? value : new String());
	}

	private void stageFailed(String msg) throws StageFailedException {
		stageFailed(new String[] { msg });
	}

	private void stageFailed(String[] msgs) throws StageFailedException {
		for (String msg : msgs) {
			if (msg != null) {
				getLogger().severe(msg);
			}
		}
		throw new StageFailedException();
	}

	public void loadEvents() {
		YamlConfiguration config = new YamlConfiguration();
		File eventsFile = new File(this.getDataFolder(), "events.yml");
		if (eventsFile.length() != 0) {
			try {
				config.load(eventsFile);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
			}
			ConfigurationSection sec = config.getConfigurationSection("events");
			if (sec != null) {
				for (String s : sec.getKeys(false)) {
					Event event = Event.loadEvent(s, this);
					if (event != null) {
						events.add(event);
					} else {
						getLogger().log(Level.SEVERE, "Failed to load Event \"" + s + "\". Skipping.");
					}
				}
			} else {
				getLogger().log(Level.SEVERE, "Could not find section \"events\" from events.yml. Skipping.");
			}
		} else {
			getLogger().log(Level.WARNING, "Empty file events.yml was not loaded.");
		}
	}

	public static String parseString(String s, Quest quest) {
		String parsed = parseString(s);
		if (parsed.contains("<npc>")) {
			if (quest.npcStart != null) {
				parsed = parsed.replace("<npc>", quest.npcStart.getName());
			} else {
				Bukkit.getLogger().warning(quest.getName() + " quest uses <npc> tag but doesn't have an NPC start set");
			}
		}
		return parsed;
	}
	
	public String parseString(String s, Quest quest, Player player) {
		String parsed = parseString(s, quest);
		if (depends.getPlaceholderApi() != null && player != null) {
			parsed = PlaceholderAPI.setPlaceholders(player, parsed);
		}
		return parsed;
	}

	public static String parseString(String s, NPC npc) {
		String parsed = parseString(s);
		if (parsed.contains("<npc>")) {
			parsed = parsed.replace("<npc>", npc.getName());
		}
		return parsed;
	}

	public static String parseString(String s) {
		String parsed = s;
		parsed = parsed.replace("<black>", ChatColor.BLACK.toString());
		parsed = parsed.replace("<darkblue>", ChatColor.DARK_BLUE.toString());
		parsed = parsed.replace("<darkgreen>", ChatColor.DARK_GREEN.toString());
		parsed = parsed.replace("<darkaqua>", ChatColor.DARK_AQUA.toString());
		parsed = parsed.replace("<darkred>", ChatColor.DARK_RED.toString());
		parsed = parsed.replace("<purple>", ChatColor.DARK_PURPLE.toString());
		parsed = parsed.replace("<gold>", ChatColor.GOLD.toString());
		parsed = parsed.replace("<grey>", ChatColor.GRAY.toString());
		parsed = parsed.replace("<gray>", ChatColor.GRAY.toString());
		parsed = parsed.replace("<darkgrey>", ChatColor.DARK_GRAY.toString());
		parsed = parsed.replace("<darkgray>", ChatColor.DARK_GRAY.toString());
		parsed = parsed.replace("<blue>", ChatColor.BLUE.toString());
		parsed = parsed.replace("<green>", ChatColor.GREEN.toString());
		parsed = parsed.replace("<aqua>", ChatColor.AQUA.toString());
		parsed = parsed.replace("<red>", ChatColor.RED.toString());
		parsed = parsed.replace("<pink>", ChatColor.LIGHT_PURPLE.toString());
		parsed = parsed.replace("<yellow>", ChatColor.YELLOW.toString());
		parsed = parsed.replace("<white>", ChatColor.WHITE.toString());
		parsed = parsed.replace("<random>", ChatColor.MAGIC.toString());
		parsed = parsed.replace("<italic>", ChatColor.ITALIC.toString());
		parsed = parsed.replace("<bold>", ChatColor.BOLD.toString());
		parsed = parsed.replace("<underline>", ChatColor.UNDERLINE.toString());
		parsed = parsed.replace("<strike>", ChatColor.STRIKETHROUGH.toString());
		parsed = parsed.replace("<reset>", ChatColor.RESET.toString());
		parsed = parsed.replace("<br>", "\n");
		parsed = ChatColor.translateAlternateColorCodes('&', parsed);
		return parsed;
	}

	public static Location getLocation(String arg) {
		String[] info = arg.split(" ");
		
		if (info.length < 4) {
			return null;
		}
		
		StringBuilder sb = new StringBuilder();
		int index = 0;
		int xIndex = info.length -3;
		int yIndex = info.length -2;
		int zIndex = info.length -1;
		
		while (index < xIndex) {
			String s = info[index];
			if (index == 0) {
				sb.append(s);
			} else {
				sb.append(" " + s);
			}
			index++;
		}
		
		String world = sb.toString();
		
		double x;
		double y;
		double z;
		try {
			x = Double.parseDouble(info[xIndex]);
			y = Double.parseDouble(info[yIndex]);
			z = Double.parseDouble(info[zIndex]);
		} catch (Exception e) {
			Bukkit.getLogger().severe("Please inform Quests developer location was wrong for "
					+ world + " " + info[xIndex] + " " + info[yIndex] + " " + info[zIndex] + " ");
			return null;
		}
		if (Bukkit.getServer().getWorld(world) == null) {
			Bukkit.getLogger().severe("Quests could not locate world " + world + ", is it loaded?");
			return null;
		}
		Location finalLocation = new Location(Bukkit.getServer().getWorld(world), x, y, z);
		return finalLocation;
	}

	public static String getLocationInfo(Location loc) {
		String info = "";
		info += loc.getWorld().getName();
		info += " " + loc.getX();
		info += " " + loc.getY();
		info += " " + loc.getZ();
		return info;
	}

	public static Effect getEffect(String eff) {
		if (eff.equalsIgnoreCase("BLAZE_SHOOT")) {
			return Effect.BLAZE_SHOOT;
		} else if (eff.equalsIgnoreCase("BOW_FIRE")) {
			return Effect.BOW_FIRE;
		} else if (eff.equalsIgnoreCase("CLICK1")) {
			return Effect.CLICK1;
		} else if (eff.equalsIgnoreCase("CLICK2")) {
			return Effect.CLICK2;
		} else if (eff.equalsIgnoreCase("DOOR_TOGGLE")) {
			return Effect.DOOR_TOGGLE;
		} else if (eff.equalsIgnoreCase("EXTINGUISH")) {
			return Effect.EXTINGUISH;
		} else if (eff.equalsIgnoreCase("GHAST_SHOOT")) {
			return Effect.GHAST_SHOOT;
		} else if (eff.equalsIgnoreCase("GHAST_SHRIEK")) {
			return Effect.GHAST_SHRIEK;
		} else if (eff.equalsIgnoreCase("ZOMBIE_CHEW_IRON_DOOR")) {
			return Effect.ZOMBIE_CHEW_IRON_DOOR;
		} else if (eff.equalsIgnoreCase("ZOMBIE_CHEW_WOODEN_DOOR")) {
			return Effect.ZOMBIE_CHEW_WOODEN_DOOR;
		} else if (eff.equalsIgnoreCase("ZOMBIE_DESTROY_DOOR")) {
			return Effect.ZOMBIE_DESTROY_DOOR;
		} else {
			return null;
		}
	}

	public static EntityType getMobType(String mob) {
		return MiscUtil.getProperMobType(mob);
	}

	public static String getTime(long milliseconds) {
		String message = "";
		long days = milliseconds / 86400000;
		long hours = (milliseconds % 86400000) / 3600000;
		long minutes = ((milliseconds % 86400000) % 3600000) / 60000;
		long seconds = (((milliseconds % 86400000) % 3600000) % 60000) / 1000;
		long milliSeconds2 = (((milliseconds % 86400000) % 3600000) % 60000) % 1000;
		if (days > 0L) {
			if (days == 1L) {
				message += " 1 " + Lang.get("timeDay") + ",";
			} else {
				message += " " + days + " " + Lang.get("timeDays") + ",";
			}
		}
		if (hours > 0L) {
			if (hours == 1L) {
				message += " 1 " + Lang.get("timeHour") + ",";
			} else {
				message += " " + hours + " " + Lang.get("timeHours") + ",";
			}
		}
		if (minutes > 0L) {
			if (minutes == 1L) {
				message += " 1 " + Lang.get("timeMinute") + ",";
			} else {
				message += " " + minutes + " " + Lang.get("timeMinutes") + ",";
			}
		}
		if (seconds > 0L) {
			if (seconds == 1L) {
				message += " 1 " + Lang.get("timeSecond") + ",";
			} else {
				message += " " + seconds + " " + Lang.get("timeSeconds") + ",";
			}
		} else {
			if (milliSeconds2 > 0L) {
				if (milliSeconds2 == 1L) {
					message += " 1 " + Lang.get("timeMillisecond") + ",";
				} else {
					message += " " + milliSeconds2 + " " + Lang.get("timeMilliseconds") + ",";
				}
			}
		}
		message = message.substring(1, message.length() - 1);
		return message;
	}
	
	public static PotionEffect getPotionEffect(String type, int duration, int amplifier) {
		PotionEffectType potionType;
		if (type.equalsIgnoreCase("ABSORPTION")) {
			potionType = PotionEffectType.ABSORPTION;
		} else if (type.equalsIgnoreCase("BLINDNESS")) {
			potionType = PotionEffectType.BLINDNESS;
		} else if (type.equalsIgnoreCase("CONFUSION")) {
			potionType = PotionEffectType.CONFUSION;
		} else if (type.equalsIgnoreCase("DAMAGE_RESISTANCE")) {
			potionType = PotionEffectType.DAMAGE_RESISTANCE;
		} else if (type.equalsIgnoreCase("FAST_DIGGING")) {
			potionType = PotionEffectType.FAST_DIGGING;
		} else if (type.equalsIgnoreCase("FIRE_RESISTANCE")) {
			potionType = PotionEffectType.FIRE_RESISTANCE;
		} else if (type.equalsIgnoreCase("HARM")) {
			potionType = PotionEffectType.HARM;
		} else if (type.equalsIgnoreCase("HEAL")) {
			potionType = PotionEffectType.HEAL;
		} else if (type.equalsIgnoreCase("HEALTH_BOOST")) {
			potionType = PotionEffectType.HEALTH_BOOST;
		} else if (type.equalsIgnoreCase("HUNGER")) {
			potionType = PotionEffectType.HUNGER;
		} else if (type.equalsIgnoreCase("INCREASE_DAMAGE")) {
			potionType = PotionEffectType.INCREASE_DAMAGE;
		} else if (type.equalsIgnoreCase("INVISIBILITY")) {
			potionType = PotionEffectType.INVISIBILITY;
		} else if (type.equalsIgnoreCase("JUMP")) {
			potionType = PotionEffectType.JUMP;
		} else if (type.equalsIgnoreCase("NIGHT_VISION")) {
			potionType = PotionEffectType.NIGHT_VISION;
		} else if (type.equalsIgnoreCase("POISON")) {
			potionType = PotionEffectType.POISON;
		} else if (type.equalsIgnoreCase("REGENERATION")) {
			potionType = PotionEffectType.REGENERATION;
		} else if (type.equalsIgnoreCase("SATURATION")) {
			potionType = PotionEffectType.SATURATION;
		} else if (type.equalsIgnoreCase("SLOW")) {
			potionType = PotionEffectType.SLOW;
		} else if (type.equalsIgnoreCase("SLOW_DIGGING")) {
			potionType = PotionEffectType.SLOW_DIGGING;
		} else if (type.equalsIgnoreCase("SPEED")) {
			potionType = PotionEffectType.SPEED;
		} else if (type.equalsIgnoreCase("WATER_BREATHING")) {
			potionType = PotionEffectType.WATER_BREATHING;
		} else if (type.equalsIgnoreCase("WEAKNESS")) {
			potionType = PotionEffectType.WEAKNESS;
		} else if (type.equalsIgnoreCase("WITHER")) {
			potionType = PotionEffectType.WITHER;
		} else {
			return null;
		}
		return new PotionEffect(potionType, duration, amplifier);
	}

	public static SkillType getMcMMOSkill(String s) {
		return SkillType.getSkill(s);
	}
	
	/**
	 * Adds item to player's inventory. If full, item is dropped at player's location.
	 * 
	 * @throws NullPointerException when ItemStack is null
	 */
	public static void addItem(Player p, ItemStack i) throws Exception {
		if (i == null) {
			throw new NullPointerException("Null item while trying to add to inventory of " + p.getName());
		}
		PlayerInventory inv = p.getInventory();
		if (i != null) {
			HashMap<Integer, ItemStack> leftover = inv.addItem(i);
			if (leftover != null) {
				if (leftover.isEmpty() == false) {
					for (ItemStack i2 : leftover.values()) {
						p.getWorld().dropItem(p.getLocation(), i2);
					}
				}
			}
		}
	}

	public String getCurrency(boolean plural) {
		if (depends.getVaultEconomy() == null) {
			return Lang.get("money");
		}
		if (plural) {
			if (depends.getVaultEconomy().currencyNamePlural().trim().isEmpty()) {
				return Lang.get("money");
			} else {
				return depends.getVaultEconomy().currencyNamePlural();
			}
		} else {
			if (depends.getVaultEconomy().currencyNameSingular().trim().isEmpty()) {
				return Lang.get("money");
			} else {
				return depends.getVaultEconomy().currencyNameSingular();
			}
		}
	}

	public static boolean removeItem(Inventory inventory, ItemStack is) {
		int amount = is.getAmount();
		HashMap<Integer, ? extends ItemStack> allItems = inventory.all(is.getType());
		HashMap<Integer, Integer> removeFrom = new HashMap<Integer, Integer>();
		int foundAmount = 0;
		for (Map.Entry<Integer, ? extends ItemStack> item : allItems.entrySet()) {
			if (ItemUtil.compareItems(is, item.getValue(), true) == 0) {
				if (item.getValue().getAmount() >= amount - foundAmount) {
					removeFrom.put(item.getKey(), amount - foundAmount);
					foundAmount = amount;
				} else {
					foundAmount += item.getValue().getAmount();
					removeFrom.put(item.getKey(), item.getValue().getAmount());
				}
				if (foundAmount >= amount) {
					break;
				}
			}
		}
		if (foundAmount == amount) {
			for (Map.Entry<Integer, Integer> toRemove : removeFrom.entrySet()) {
				ItemStack item = inventory.getItem(toRemove.getKey());
				if (item.getAmount() - toRemove.getValue() <= 0) {
					inventory.clear(toRemove.getKey());
				} else {
					item.setAmount(item.getAmount() - toRemove.getValue());
					inventory.setItem(toRemove.getKey(), item);
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Checks if player CANNOT use Quests
	 * 
	 * @param uuid the entity UUID to be checked
	 * @return {@code true} if entity has no permission or is not a player
	 */
	public boolean checkQuester(UUID uuid) {
		if (!(Bukkit.getPlayer(uuid) instanceof Player)) {
			return true;
		}
		Player p = Bukkit.getPlayer(uuid);
		if (p.isOp()) {
			return false;
		}
		try {
			for (PermissionAttachmentInfo pm : p.getEffectivePermissions()) {
				if (pm.getPermission().startsWith("quests")
						|| pm.getPermission().equals("*")
						|| pm.getPermission().equals("*.*")) {
					return false;
				}
			}
		} catch (NullPointerException ne) {
			// User has no permissions
		} catch (ConcurrentModificationException cme) {
			// Bummer. Not much we can do about it
		}
		return true;
	}

	public static boolean checkList(List<?> list, Class<?> clazz) {
		if (list == null) {
			return false;
		}
		for (Object o : list) {
			if (o == null) {
				Bukkit.getLogger().severe("A null " + clazz.getSimpleName() + " value was detected in quests.yml, please correct the file");
				return false;
			}
			if (clazz.isAssignableFrom(o.getClass()) == false) {
				return false;
			}
		}
		return true;
	}

	public Quest getQuest(String name) {
		for (Quest q : quests) {
			if (q.getName().equalsIgnoreCase(name)) {
				return q;
			}
		}
		return null;
	}

	public Event getEvent(String name) {
		for (Event e : events) {
			if (e.getName().equalsIgnoreCase(name)) {
				return e;
			}
		}
		return null;
	}

	public Location getNPCLocation(int id) {
		return depends.getCitizens().getNPCRegistry().getById(id).getStoredLocation();
	}

	public String getNPCName(int id) {
		return depends.getCitizens().getNPCRegistry().getById(id).getName();
	}

	public static int countInv(Inventory inv, Material m, int subtract) {
		int count = 0;
		for (ItemStack i : inv.getContents()) {
			if (i != null) {
				if (i.getType().equals(m)) {
					count += i.getAmount();
				}
			}
		}
		return count - subtract;
	}
	
	@SuppressWarnings("deprecation") // since 1.13
	public static Enchantment getEnchantment(String enchant) {
		String ench = Lang.getKey(enchant.replace(" ", ""));
		ench = ench.replace("ENCHANTMENT_", "");
		Enchantment e = Enchantment.getByName(ench);
		return e != null ? e : getEnchantmentLegacy(ench.replace(" ", ""));
	}

	public static Enchantment getEnchantmentLegacy(String enchant) {
		if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_ARROW_DAMAGE"))) {
			return Enchantment.ARROW_DAMAGE;
		} else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_ARROW_FIRE"))) {
			return Enchantment.ARROW_FIRE;
		} else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_ARROW_INFINITE"))) {
			return Enchantment.ARROW_INFINITE;
		} else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_ARROW_KNOCKBACK"))) {
			return Enchantment.ARROW_KNOCKBACK;
		} else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_DAMAGE_ALL"))) {
			return Enchantment.DAMAGE_ALL;
		} else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_DAMAGE_ARTHROPODS"))) {
			return Enchantment.DAMAGE_ARTHROPODS;
		} else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_DAMAGE_UNDEAD"))) {
			return Enchantment.DAMAGE_UNDEAD;
		} else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_DIG_SPEED"))) {
			return Enchantment.DIG_SPEED;
		} else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_DURABILITY"))) {
			return Enchantment.DURABILITY;
		} else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_FIRE_ASPECT"))) {
			return Enchantment.FIRE_ASPECT;
		} else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_KNOCKBACK"))) {
			return Enchantment.KNOCKBACK;
		} else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_LOOT_BONUS_BLOCKS"))) {
			return Enchantment.LOOT_BONUS_BLOCKS;
		} else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_LOOT_BONUS_MOBS"))) {
			return Enchantment.LOOT_BONUS_MOBS;
		} else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_LUCK"))) {
			return Enchantment.LOOT_BONUS_MOBS;
		} else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_LURE"))) {
			return Enchantment.LOOT_BONUS_MOBS;
		} else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_OXYGEN"))) {
			return Enchantment.OXYGEN;
		} else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_PROTECTION_ENVIRONMENTAL"))) {
			return Enchantment.PROTECTION_ENVIRONMENTAL;
		} else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_PROTECTION_EXPLOSIONS"))) {
			return Enchantment.PROTECTION_EXPLOSIONS;
		} else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_PROTECTION_FALL"))) {
			return Enchantment.PROTECTION_FALL;
		} else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_PROTECTION_FIRE"))) {
			return Enchantment.PROTECTION_FIRE;
		} else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_PROTECTION_PROJECTILE"))) {
			return Enchantment.PROTECTION_PROJECTILE;
		} else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_SILK_TOUCH"))) {
			return Enchantment.SILK_TOUCH;
		} else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_THORNS"))) {
			return Enchantment.THORNS;
		} else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_WATER_WORKER"))) {
			return Enchantment.WATER_WORKER;
		} else {
			return null;
		}
	}

	public static Enchantment getEnchantmentPretty(String enchant) {
		while (Quester.spaceToCapital(enchant) != null) {
			enchant = Quester.spaceToCapital(enchant);
		}
		return getEnchantment(enchant);
	}

	public static DyeColor getDyeColor(String s) {
		String col = Lang.getKey(MiscUtil.getCapitalized(s));
		col = col.replace("COLOR_", "");
		DyeColor color = null;
		try {
			color = DyeColor.valueOf(col);
		} catch (IllegalArgumentException e) {
			// Do nothing
		}
		return color != null ? color : getDyeColorLegacy(s);
	}

	public static DyeColor getDyeColorLegacy(String s) {
		if (s.equalsIgnoreCase("Black") || s.equalsIgnoreCase(Lang.get("COLOR_BLACK"))) {
			return DyeColor.BLACK;
		} else if (s.equalsIgnoreCase("Blue") || s.equalsIgnoreCase(Lang.get("COLOR_BLUE"))) {
			return DyeColor.BLUE;
		} else if (s.equalsIgnoreCase("Brown") || s.equalsIgnoreCase(Lang.get("COLOR_BROWN"))) {
			return DyeColor.BROWN;
		} else if (s.equalsIgnoreCase("Cyan") || s.equalsIgnoreCase(Lang.get("COLOR_CYAN"))) {
			return DyeColor.CYAN;
		} else if (s.equalsIgnoreCase("Gray") || s.equalsIgnoreCase(Lang.get("COLOR_GRAY"))) {
			return DyeColor.GRAY;
		} else if (s.equalsIgnoreCase("Green") || s.equalsIgnoreCase(Lang.get("COLOR_GREEN"))) {
			return DyeColor.GREEN;
		} else if (s.equalsIgnoreCase("LightBlue") || s.equalsIgnoreCase(Lang.get("COLOR_LIGHT_BLUE"))) {
			return DyeColor.LIGHT_BLUE;
		} else if (s.equalsIgnoreCase("Lime") || s.equalsIgnoreCase(Lang.get("COLOR_LIME"))) {
			return DyeColor.LIME;
		} else if (s.equalsIgnoreCase("Magenta") || s.equalsIgnoreCase(Lang.get("COLOR_MAGENTA"))) {
			return DyeColor.MAGENTA;
		} else if (s.equalsIgnoreCase("Orange") || s.equalsIgnoreCase(Lang.get("COLOR_ORAGE"))) {
			return DyeColor.ORANGE;
		} else if (s.equalsIgnoreCase("Pink") || s.equalsIgnoreCase(Lang.get("COLOR_PINK"))) {
			return DyeColor.PINK;
		} else if (s.equalsIgnoreCase("Purple") || s.equalsIgnoreCase(Lang.get("COLOR_PURPLE"))) {
			return DyeColor.PURPLE;
		} else if (s.equalsIgnoreCase("Red") || s.equalsIgnoreCase(Lang.get("COLOR_RED"))) {
			return DyeColor.RED;
		// 1.13 changed DyeColor.SILVER -> DyeColor.LIGHT_GRAY
		} else if (s.equalsIgnoreCase("Silver") || s.equalsIgnoreCase("LightGray") || s.equalsIgnoreCase(Lang.get("COLOR_SILVER"))) {
			return DyeColor.getByColor(Color.SILVER);
		} else if (s.equalsIgnoreCase("White") || s.equalsIgnoreCase(Lang.get("COLOR_WHITE"))) {
			return DyeColor.WHITE;
		} else if (s.equalsIgnoreCase("Yellow") || s.equalsIgnoreCase(Lang.get("COLOR_YELLOW"))) {
			return DyeColor.YELLOW;
		} else {
			return null;
		}
	}

	public static String getDyeString(DyeColor dc) {
		return Lang.get("COLOR_" + dc.name());
	}

	public boolean hasQuest(NPC npc, Quester quester) {
		for (Quest q : quests) {
			if (q.npcStart != null && quester.completedQuests.contains(q.getName()) == false) {
				if (q.npcStart.getId() == npc.getId()) {
					boolean ignoreLockedQuests = settings.canIgnoreLockedQuests();
					if (ignoreLockedQuests == false || ignoreLockedQuests == true && q.testRequirements(quester) == true) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	// Unused internally, left for external use
	public boolean hasCompletedQuest(NPC npc, Quester quester) {
		for (Quest q : quests) {
			if (q.npcStart != null && quester.completedQuests.contains(q.getName()) == true) {
				if (q.npcStart.getId() == npc.getId()) {
					boolean ignoreLockedQuests = settings.canIgnoreLockedQuests();
					if (ignoreLockedQuests == false || ignoreLockedQuests == true && q.testRequirements(quester) == true) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public boolean hasCompletedRedoableQuest(NPC npc, Quester quester) {
		for (Quest q : quests) {
			if (q.npcStart != null && quester.completedQuests.contains(q.getName()) == true && q.getPlanner().getCooldown() > -1) {
				if (q.npcStart.getId() == npc.getId()) {
					boolean ignoreLockedQuests = settings.canIgnoreLockedQuests();
					if (ignoreLockedQuests == false || ignoreLockedQuests == true && q.testRequirements(quester) == true) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static int getMCMMOSkillLevel(SkillType st, String player) {
		McMMOPlayer mPlayer = UserManager.getPlayer(player);
		if (mPlayer == null) {
			return -1;
		}
		return mPlayer.getProfile().getSkillLevel(st);
	}

	public Hero getHero(UUID uuid) {
		Player p = getServer().getPlayer(uuid);
		if (p == null) {
			return null;
		}
		return depends.getHeroes().getCharacterManager().getHero(p);
	}

	public boolean testPrimaryHeroesClass(String primaryClass, UUID uuid) {
		Hero hero = getHero(uuid);
		return hero.getHeroClass().getName().equalsIgnoreCase(primaryClass);
	}

	public boolean testSecondaryHeroesClass(String secondaryClass, UUID uuid) {
		Hero hero = getHero(uuid);
		return hero.getSecondClass().getName().equalsIgnoreCase(secondaryClass);
	}

	public void updateData() {
		YamlConfiguration config = new YamlConfiguration();
		File dataFile = new File(this.getDataFolder(), "data.yml");
		try {
			config.load(dataFile);
			config.set("npc-gui", questNpcGuis);
			config.save(dataFile);
		} catch (Exception e) {
			getLogger().severe("Unable to update data.yml file");
			e.printStackTrace();
		}
	}
	
}