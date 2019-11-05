/*******************************************************************************************************
 * Continued by PikaMug (formerly HappyPikachu) with permission from _Blackvein_. All rights reserved.
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
import java.util.Collection;
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

import com.codisimus.plugins.phatloots.PhatLootsAPI;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.util.player.UserManager;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass;

import me.blackvein.quests.actions.Action;
import me.blackvein.quests.actions.ActionFactory;
import me.blackvein.quests.listeners.CmdExecutor;
import me.blackvein.quests.listeners.DungeonsListener;
import me.blackvein.quests.listeners.NpcListener;
import me.blackvein.quests.listeners.PartiesListener;
import me.blackvein.quests.listeners.PlayerListener;
import me.blackvein.quests.prompts.QuestOfferPrompt;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.LocaleQuery;
import me.blackvein.quests.util.MiscUtil;
import me.clip.placeholderapi.PlaceholderAPI;

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
    private LinkedList<Action> events = new LinkedList<Action>();
    private LinkedList<NPC> questNpcs = new LinkedList<NPC>();
    private CommandExecutor cmdExecutor;
    private ConversationFactory conversationFactory;
    private ConversationFactory npcConversationFactory;
    private QuestFactory questFactory;
    private ActionFactory eventFactory;
    private PlayerListener playerListener;
    private NpcListener npcListener;
    private NpcEffectThread effThread;
    private DungeonsListener dungeonsListener;
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
        /****** WARNING: ORDER OF STEPS MATTERS ******/

        // 1 - Intialize variables
        bukkitVersion = Bukkit.getServer().getBukkitVersion().split("-")[0];
        settings = new Settings(this);
        localeQuery = new LocaleQuery(this);
        localeQuery.setBukkitVersion(bukkitVersion);
        playerListener = new PlayerListener(this);
        effThread = new NpcEffectThread(this);
        npcListener = new NpcListener(this);
        dungeonsListener = new DungeonsListener();
        partiesListener = new PartiesListener();
        questFactory = new QuestFactory(this);
        eventFactory = new ActionFactory(this);
        depends = new Dependencies(this);
        lang = new Lang(this);

        // 2 - Load main config
        settings.init();
        
        // 3 - Setup language files
        try {
            setupLang();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        
        // 4 - Load command executor
        cmdExecutor = new CmdExecutor(this);
        
        // 5 - Load soft-depends
        depends.init();
        
        // 6 - Save resources from jar
        saveResourceAs("quests.yml", "quests.yml", false);
        saveResourceAs("actions.yml", "actions.yml", false);
        
        // 7 - Save config with any new options
        getConfig().options().copyDefaults(true);
        saveConfig();
        
        // 8 - Setup commands
        getCommand("quests").setExecutor(cmdExecutor);
        getCommand("questadmin").setExecutor(cmdExecutor);
        getCommand("quest").setExecutor(cmdExecutor);
        
        // 9 - Setup conversation factory after timeout has loaded
        this.conversationFactory = new ConversationFactory(this).withModality(false).withPrefix(new QuestsPrefix())
                .withFirstPrompt(new QuestAcceptPrompt()).withTimeout(settings.getAcceptTimeout())
                .thatExcludesNonPlayersWithMessage("Console may not perform this conversation!")
                .addConversationAbandonedListener(this);
        this.npcConversationFactory = new ConversationFactory(this).withModality(false)
                .withFirstPrompt(new QuestOfferPrompt(this))
                .withTimeout(settings.getAcceptTimeout()).withLocalEcho(false).addConversationAbandonedListener(this);
        
        // 10 - Register listeners
        getServer().getPluginManager().registerEvents(playerListener, this);
        if (depends.getCitizens() != null) {
            getServer().getPluginManager().registerEvents(npcListener, this);
            if (settings.canNpcEffects()) {
                getServer().getScheduler().scheduleSyncRepeatingTask(this, effThread, 20, 20);
            }
        }
        if (depends.getDungeonsApi() != null) {
            getServer().getPluginManager().registerEvents(dungeonsListener, this);
        }
        if (depends.getPartiesApi() != null) {
            getServer().getPluginManager().registerEvents(partiesListener, this);
        }
        
        // 11 - Delay loading of Quests, Actions and modules
        delayLoadQuestInfo(5L);
    }
    
    @Override
    public void onDisable() {
        getLogger().info("Saving Quester data...");
        for (Player p : getServer().getOnlinePlayers()) {
            Quester quester = getQuester(p.getUniqueId());
            quester.saveData();
        }
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
    
    public LinkedList<Action> getActions() {
        return events;
    }
    
    public void setActions(LinkedList<Action> actions) {
        this.events = actions;
    }
    
    /**
     * @deprecated Use getActions()
     */
    public LinkedList<Action> getEvents() {
        return events;
    }
    
    /**
     * @deprecated Use setActions()
     */
    public void setEvents(LinkedList<Action> events) {
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
    
    public ConversationFactory getConversationFactory() {
        return conversationFactory;
    }
    
    public ConversationFactory getNpcConversationFactory() {
        return npcConversationFactory;
    }
    
    public QuestFactory getQuestFactory() {
        return questFactory;
    }
    
    public ActionFactory getEventFactory() {
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

    private class QuestAcceptPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get((Player) context.getForWhom(), "acceptQuest") + "  " + ChatColor.GREEN 
                    + Lang.get("yesWord") + " / " + Lang.get("noWord");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String s) {
            Player player = (Player) context.getForWhom();
            if (s.equalsIgnoreCase(Lang.get(player, "yesWord"))) {
                String questToTake = getQuester(player.getUniqueId()).questToTake;
                try {
                    if (getQuest(questToTake) == null) {
                        getLogger().info(player.getName() + " attempted to take quest \"" + questToTake 
                                + "\" but something went wrong");
                        player.sendMessage(ChatColor.RED 
                                + "Something went wrong! Please report issue to an administrator.");
                    } else {
                        getQuester(player.getUniqueId()).takeQuest(getQuest(questToTake), false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return Prompt.END_OF_CONVERSATION;
            } else if (s.equalsIgnoreCase(Lang.get("noWord"))) {
                player.sendMessage(ChatColor.YELLOW + Lang.get("cancelled"));
                return Prompt.END_OF_CONVERSATION;
            } else {
                String msg = Lang.get("questInvalidChoice");
                msg.replace("<yes>", Lang.get(player, "yesWord"));
                msg.replace("<no>", Lang.get(player, "noWord"));
                player.sendMessage(ChatColor.RED + msg);
                return new QuestAcceptPrompt();
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
            throw new IllegalArgumentException("The embedded resource '" + resourcePath 
                    + "' cannot be found in Quests jar");
        }
        
        String outPath = outputPath.replace('/', File.separatorChar).replace('\\', File.separatorChar);
        File outFile = new File(getDataFolder(), outPath);
        File outDir = new File(outFile.getPath().replace(outFile.getName(), ""));
        
        if (!outDir.exists()) {
            if (!outDir.mkdirs()) {
                getLogger().log(Level.SEVERE, "Failed to make directories for " + outFile.getName() + " (canWrite= " 
                        + outDir.canWrite() + ")");
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
                    getLogger().severe("Unable to copy " + outFile.getName() + " (canWrite= " + outFile.canWrite() 
                            + ")");
                }
            }
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile, ex);
        }
    }

    /**
     * Load quests, actions, and modules after specified delay<p>
     * 
     * At startup, this permits Citizens to fully load first
     */
    private void delayLoadQuestInfo(long ticks) {
        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {

            @Override
            public void run() {
                loadQuests();
                loadActions();
                getLogger().log(Level.INFO, "Loaded " + quests.size() + " Quest(s)"
                        + ", " + events.size() + " Action(s)"
                        + ", " + Lang.size() + " Phrase(s)");
                questers.addAll(getOnlineQuesters());
                if (depends.getCitizens() != null) {
                    if (depends.getCitizens().getNPCRegistry() == null) {
                        getLogger().log(Level.SEVERE, 
                                "Citizens was enabled but NPCRegistry was null. Disabling linkage.");
                        depends.disableCitizens();
                    }
                }
                loadModules();
            }
        }, ticks);
    }
    
    /**
     * Load modules from file
     */
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

    /**
     * Load the specified jar as a module
     * 
     * @param jar A custom reward/requirement/objective jar
     */
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
                        getLogger().warning("Failed to register events for custom objective \"" + name 
                                + "\". Does the objective class listen for events?");
                        ex.printStackTrace();
                    }
                }
            }
            if (count == 0) {
                getLogger().severe("Unable to load module from file: " + jar.getName() 
                        + ", jar file is not a valid module!");
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
        if (!ignoreOverrides) {
            if (quester.getCurrentStage(quest) != null) {
                if (quester.getCurrentStage(quest).objectiveOverride != null) {
                    quester.getPlayer().sendMessage(ChatColor.GREEN + quester.getCurrentStage(quest).objectiveOverride);
                    return;
                }
            }
        }
        if (quester.getQuestData(quest) == null) {
            getLogger().warning("Quest data was null when showing objectives for " + quest.getName());
            return;
        }
        QuestData data = quester.getQuestData(quest);
        Stage stage = quester.getCurrentStage(quest);
        for (ItemStack e : stage.blocksToBreak) {
            for (ItemStack e2 : data.blocksBroken) {
                if (e2.getType().equals(e.getType()) && e2.getDurability() == e.getDurability()) {
                    if (e2.getAmount() < e.getAmount()) {
                        String message = ChatColor.GREEN + Lang.get(quester.getPlayer(), "break") + " <item>" 
                                + ChatColor.GREEN + ": " + e2.getAmount() + "/" + e.getAmount();
                        if (depends.getPlaceholderApi() != null) {
                            message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
                        }
                        if (getSettings().canTranslateItems() && !e.hasItemMeta() 
                                && !e.getItemMeta().hasDisplayName()) {
                            localeQuery.sendMessage(quester.getPlayer(), message, e.getType(), e.getDurability(), null);
                        } else {
                            quester.getPlayer().sendMessage(message.replace("<item>", ItemUtil.getName(e)));
                        }
                    } else {
                        String message = ChatColor.GRAY + Lang.get(quester.getPlayer(), "break") + " <item>" 
                                + ChatColor.GRAY + ": " + e2.getAmount() + "/" + e.getAmount();
                        if (depends.getPlaceholderApi() != null) {
                            message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
                        }
                        if (getSettings().canTranslateItems() && !e.hasItemMeta() 
                                && !e.getItemMeta().hasDisplayName()) {
                            localeQuery.sendMessage(quester.getPlayer(), message, e.getType(), e.getDurability(), null);
                        } else {
                            quester.getPlayer().sendMessage(message.replace("<item>", ItemUtil.getName(e)));
                        }
                    }
                }
            }
        }
        for (ItemStack e : stage.blocksToDamage) {
            for (ItemStack e2 : data.blocksDamaged) {
                if (e2.getType().equals(e.getType()) && e2.getDurability() == e.getDurability()) {
                    if (e2.getAmount() < e.getAmount()) {
                        String message = ChatColor.GREEN + Lang.get(quester.getPlayer(), "damage") + " <item>" 
                                + ChatColor.GREEN + ": " + e2.getAmount() + "/" + e.getAmount();
                        if (depends.getPlaceholderApi() != null) {
                            message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
                        }
                        if (getSettings().canTranslateItems() && !e.hasItemMeta() 
                                && !e.getItemMeta().hasDisplayName()) {
                            localeQuery.sendMessage(quester.getPlayer(), message, e.getType(), e.getDurability(), null);
                        } else {
                            quester.getPlayer().sendMessage(message.replace("<item>", ItemUtil.getName(e)));
                        }
                    } else {
                        String message = ChatColor.GRAY + Lang.get(quester.getPlayer(), "damage") + " <item>" 
                                + ChatColor.GRAY + ": " + e2.getAmount() + "/" + e.getAmount();
                        if (depends.getPlaceholderApi() != null) {
                            message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
                        }
                        if (getSettings().canTranslateItems() && !e.hasItemMeta() 
                                && !e.getItemMeta().hasDisplayName()) {
                            localeQuery.sendMessage(quester.getPlayer(), message, e.getType(), e.getDurability(), null);
                        } else {
                            quester.getPlayer().sendMessage(message.replace("<item>", ItemUtil.getName(e)));
                        }
                    }
                }
            }
        }
        for (ItemStack e : stage.blocksToPlace) {
            for (ItemStack e2 : data.blocksPlaced) {
                if (e2.getType().equals(e.getType()) && e2.getDurability() == e.getDurability()) {
                    if (e2.getAmount() < e.getAmount()) {
                        String message = ChatColor.GREEN + Lang.get(quester.getPlayer(), "place") + " <item>" 
                                + ChatColor.GREEN + ": " + e2.getAmount() + "/" + e.getAmount();
                        if (depends.getPlaceholderApi() != null) {
                            message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
                        }
                        if (getSettings().canTranslateItems() && !e.hasItemMeta() 
                                && !e.getItemMeta().hasDisplayName()) {
                            localeQuery.sendMessage(quester.getPlayer(), message, e.getType(), e.getDurability(), null);
                        } else {
                            quester.getPlayer().sendMessage(message.replace("<item>", ItemUtil.getName(e)));
                        }
                    } else {
                        String message = ChatColor.GRAY + Lang.get(quester.getPlayer(), "place") + " <item>" 
                                + ChatColor.GRAY + ": " + e2.getAmount() + "/" + e.getAmount();
                        if (depends.getPlaceholderApi() != null) {
                            message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
                        }
                        if (getSettings().canTranslateItems() && !e.hasItemMeta() 
                                && !e.getItemMeta().hasDisplayName()) {
                            localeQuery.sendMessage(quester.getPlayer(), message, e.getType(), e.getDurability(), null);
                        } else {
                            quester.getPlayer().sendMessage(message.replace("<item>", ItemUtil.getName(e)));
                        }
                    }
                }
            }
        }
        for (ItemStack e : stage.blocksToUse) {
            for (ItemStack e2 : data.blocksUsed) {
                if (e2.getType().equals(e.getType()) && e2.getDurability() == e.getDurability()) {
                    if (e2.getAmount() < e.getAmount()) {
                        String message = ChatColor.GREEN + Lang.get(quester.getPlayer(), "use") + " <item>" 
                                + ChatColor.GREEN + ": " + e2.getAmount() + "/" + e.getAmount();
                        if (depends.getPlaceholderApi() != null) {
                            message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
                        }
                        if (getSettings().canTranslateItems() && !e.hasItemMeta() 
                                && !e.getItemMeta().hasDisplayName()) {
                            localeQuery.sendMessage(quester.getPlayer(), message, e.getType(), e.getDurability(), null);
                        } else {
                            quester.getPlayer().sendMessage(message.replace("<item>", ItemUtil.getName(e)));
                        }
                    } else {
                        String message = ChatColor.GRAY + Lang.get(quester.getPlayer(), "use") + " <item>" 
                                + ChatColor.GRAY + ": " + e2.getAmount() + "/" + e.getAmount();
                        if (depends.getPlaceholderApi() != null) {
                            message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
                        }
                        if (getSettings().canTranslateItems() && !e.hasItemMeta() 
                                && !e.getItemMeta().hasDisplayName()) {
                            localeQuery.sendMessage(quester.getPlayer(), message, e.getType(), e.getDurability(), null);
                        } else {
                            quester.getPlayer().sendMessage(message.replace("<item>", ItemUtil.getName(e)));
                        }
                    }
                }
            }
        }
        for (ItemStack e : stage.blocksToCut) {
            for (ItemStack e2 : data.blocksCut) {
                if (e2.getType().equals(e.getType()) && e2.getDurability() == e.getDurability()) {
                    if (e2.getAmount() < e.getAmount()) {
                        String message = ChatColor.GREEN + Lang.get(quester.getPlayer(), "cut") + " <item>" 
                                + ChatColor.GREEN + ": " + e2.getAmount() + "/" + e.getAmount();
                        if (depends.getPlaceholderApi() != null) {
                            message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
                        }
                        if (getSettings().canTranslateItems() && !e.hasItemMeta() 
                                && !e.getItemMeta().hasDisplayName()) {
                            localeQuery.sendMessage(quester.getPlayer(), message, e.getType(), e.getDurability(), null);
                        } else {
                            quester.getPlayer().sendMessage(message.replace("<item>", ItemUtil.getName(e)));
                        }
                    } else {
                        String message = ChatColor.GRAY + Lang.get(quester.getPlayer(), "cut") + " <item>" 
                                + ChatColor.GRAY + ": " + e2.getAmount() + "/" + e.getAmount();
                        if (depends.getPlaceholderApi() != null) {
                            message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
                        }
                        if (getSettings().canTranslateItems() && !e.hasItemMeta() 
                                && !e.getItemMeta().hasDisplayName()) {
                            localeQuery.sendMessage(quester.getPlayer(), message, e.getType(), e.getDurability(), null);
                        } else {
                            quester.getPlayer().sendMessage(message.replace("<item>", ItemUtil.getName(e)));
                        }
                    }
                }
            }
        }
        for (ItemStack is : stage.itemsToCraft) {
            int crafted = 0;
            if (data.itemsCrafted.containsKey(is)) {
                crafted = data.itemsCrafted.get(is);
            }
            int amt = is.getAmount();
            if (crafted < amt) {
                String message = ChatColor.GREEN + Lang.get(quester.getPlayer(), "craft") + " <item>" 
                        + ChatColor.GREEN + ": " + crafted + "/" + is.getAmount();
                if (depends.getPlaceholderApi() != null) {
                    message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
                }
                if (getSettings().canTranslateItems() && !is.hasItemMeta() && !is.getItemMeta().hasDisplayName()) {
                    localeQuery.sendMessage(quester.getPlayer(), message, is.getType(), is.getDurability(), 
                            is.getEnchantments());
                } else {
                    quester.getPlayer().sendMessage(message.replace("<item>", ItemUtil.getName(is)));
                }
            } else {
                String message = ChatColor.GRAY + Lang.get(quester.getPlayer(), "craft") + " <item>" 
                        + ChatColor.GRAY + ": " + crafted + "/" + is.getAmount();
                if (depends.getPlaceholderApi() != null) {
                    message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
                }
                if (getSettings().canTranslateItems() && !is.hasItemMeta() && !is.getItemMeta().hasDisplayName()) {
                    localeQuery.sendMessage(quester.getPlayer(), message, is.getType(), is.getDurability(), 
                            is.getEnchantments());
                } else {
                    quester.getPlayer().sendMessage(message.replace("<item>", ItemUtil.getName(is)));
                }
            }
        }
        for (ItemStack is : stage.itemsToSmelt) {
            int smelted = 0;
            if (data.itemsSmelted.containsKey(is)) {
                smelted = data.itemsSmelted.get(is);
            }
            int amt = is.getAmount();
            if (smelted < amt) {
                String message = ChatColor.GREEN + Lang.get(quester.getPlayer(), "smelt") + " <item>" 
                        + ChatColor.GREEN + ": " + smelted + "/" + is.getAmount();
                if (depends.getPlaceholderApi() != null) {
                    message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
                }
                if (getSettings().canTranslateItems() && !is.hasItemMeta() && !is.getItemMeta().hasDisplayName()) {
                    localeQuery.sendMessage(quester.getPlayer(), message, is.getType(), is.getDurability(), 
                            is.getEnchantments());
                } else {
                    quester.getPlayer().sendMessage(message.replace("<item>", ItemUtil.getName(is)));
                }
            } else {
                String message = ChatColor.GRAY + Lang.get(quester.getPlayer(), "smelt") + " <item>" 
                        + ChatColor.GRAY + ": " + smelted + "/" + is.getAmount();
                if (depends.getPlaceholderApi() != null) {
                    message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
                }
                if (getSettings().canTranslateItems() && !is.hasItemMeta() && !is.getItemMeta().hasDisplayName()) {
                    localeQuery.sendMessage(quester.getPlayer(), message, is.getType(), is.getDurability(), 
                            is.getEnchantments());
                } else {
                    quester.getPlayer().sendMessage(message.replace("<item>", ItemUtil.getName(is)));
                }
            }
        }
        Map<Enchantment, Material> set;
        Map<Enchantment, Material> set2;
        Set<Enchantment> enchantSet;
        Set<Enchantment> enchantSet2;
        Collection<Material> matSet;
        Enchantment enchantment = null;
        Enchantment enchantment2 = null;
        Material mat = null;
        int num1;
        int num2;
        for (Entry<Map<Enchantment, Material>, Integer> e : stage.itemsToEnchant.entrySet()) {
            for (Entry<Map<Enchantment, Material>, Integer> e2 : data.itemsEnchanted.entrySet()) {
                set = e2.getKey();
                set2 = e.getKey();
                enchantSet = set.keySet();
                enchantSet2 = set2.keySet();
                for (Object o : enchantSet.toArray()) {
                    enchantment = (Enchantment) o;
                }
                for (Object o : enchantSet2.toArray()) {
                    enchantment2 = (Enchantment) o;
                }
                num1 = e2.getValue();
                num2 = e.getValue();
                matSet = set.values();
                for (Object o : matSet.toArray()) {
                    mat = (Material) o;
                }
                if (enchantment2 == enchantment) {
                    if (num1 < num2) {
                        String message = ChatColor.GREEN + Lang.get(quester.getPlayer(), "enchantItem")
                                + ChatColor.GREEN + ": " + num1 + "/" + num2;
                        if (depends.getPlaceholderApi() != null) {
                            message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
                        }
                        Map<Enchantment, Integer> enchs = new HashMap<Enchantment, Integer>();
                        enchs.put(enchantment, 1);
                        if (getSettings().canTranslateItems()) {
                            localeQuery.sendMessage(quester.getPlayer(), message, mat, (short) 0, enchs);
                        } else {
                            quester.getPlayer().sendMessage(message
                                    .replace("<item>", ItemUtil.getName(new ItemStack(mat)))
                                    .replace("<enchantment>", enchantment.getName()));
                        }
                    } else {
                        String message = ChatColor.GRAY + Lang.get(quester.getPlayer(), "enchantItem")
                                + ChatColor.GRAY + ": " + num1 + "/" + num2;
                        if (depends.getPlaceholderApi() != null) {
                            message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
                        }
                        Map<Enchantment, Integer> enchs = new HashMap<Enchantment, Integer>();
                        enchs.put(enchantment, 1);
                        if (getSettings().canTranslateItems()) {
                            localeQuery.sendMessage(quester.getPlayer(), message, mat, (short) 0, enchs);
                        } else {
                            quester.getPlayer().sendMessage(message
                                    .replace("<item>", ItemUtil.getName(new ItemStack(mat)))
                                    .replace("<enchantment>", enchantment.getName()));
                        }
                    }
                }
            }
        }
        for (ItemStack is : stage.itemsToBrew) {
            int brewed = 0;
            if (data.itemsBrewed.containsKey(is)) {
                brewed = data.itemsBrewed.get(is);
            }
            int amt = is.getAmount();
            if (brewed < amt) {
                String message = ChatColor.GREEN + Lang.get(quester.getPlayer(), "brew") + " <item>" 
                        + ChatColor.GREEN + ": " + brewed + "/" + is.getAmount();
                if (depends.getPlaceholderApi() != null) {
                    message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
                }
                if (getSettings().canTranslateItems()) {
                    if (is.hasItemMeta() && !is.getItemMeta().hasDisplayName()) {
                        // Bukkit version is 1.9+
                        localeQuery.sendMessage(quester.getPlayer(), message, is.getType(), is.getDurability(), 
                                is.getEnchantments(), is.getItemMeta());
                    } else if (Material.getMaterial("LINGERING_POTION") == null && !is.hasItemMeta() ) {
                        // Bukkit version is below 1.9
                        localeQuery.sendMessage(quester.getPlayer(), message, is.getType(), is.getDurability(), 
                                is.getEnchantments());
                    } else {
                        quester.getPlayer().sendMessage(message.replace("<item>", ItemUtil.getName(is)));
                    }
                }
            } else {
                String message = ChatColor.GRAY + Lang.get(quester.getPlayer(), "brew") + " <item>" 
                        + ChatColor.GRAY + ": " + brewed + "/" + is.getAmount();
                if (depends.getPlaceholderApi() != null) {
                    message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
                }
                if (getSettings().canTranslateItems()) {
                    if (is.hasItemMeta() && !is.getItemMeta().hasDisplayName()) {
                        // Bukkit version is 1.9+
                        localeQuery.sendMessage(quester.getPlayer(), message, is.getType(), is.getDurability(), 
                                is.getEnchantments(), is.getItemMeta());
                    } else if (Material.getMaterial("LINGERING_POTION") == null && !is.hasItemMeta() ) {
                        // Bukkit version is below 1.9
                        localeQuery.sendMessage(quester.getPlayer(), message, is.getType(), is.getDurability(), 
                                is.getEnchantments());
                    } else {
                        quester.getPlayer().sendMessage(message.replace("<item>", ItemUtil.getName(is)));
                    }
                }
            }
        }
        if (stage.fishToCatch != null) {
            if (data.getFishCaught() < stage.fishToCatch) {
                String message = ChatColor.GREEN + Lang.get(quester.getPlayer(), "catchFish")
                        + ChatColor.GREEN + ": " + data.getFishCaught() + "/" + stage.fishToCatch;
                if (depends.getPlaceholderApi() != null) {
                    message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
                }
                quester.getPlayer().sendMessage(message);
            } else {
                String message = ChatColor.GRAY + Lang.get(quester.getPlayer(), "catchFish")
                        + ChatColor.GRAY + ": " + data.getFishCaught() + "/" + stage.fishToCatch;
                if (depends.getPlaceholderApi() != null) {
                    message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
                }
                quester.getPlayer().sendMessage(message);
            }
        }
        for (EntityType e : stage.mobsToKill) {
            for (EntityType e2 : data.mobsKilled) {
                if (e == e2) {
                    if (data.mobNumKilled.size() > data.mobsKilled.indexOf(e2) 
                            && stage.mobNumToKill.size() > stage.mobsToKill.indexOf(e)) {
                        if (data.mobNumKilled.get(data.mobsKilled.indexOf(e2)) 
                                < stage.mobNumToKill.get(stage.mobsToKill.indexOf(e))) {
                            if (stage.locationsToKillWithin.isEmpty()) {
                                String message = ChatColor.GREEN + Lang.get(quester.getPlayer(), "kill") + " " 
                                        + ChatColor.AQUA + "<mob>" + ChatColor.GREEN + ": " 
                                        + (data.mobNumKilled.get(data.mobsKilled.indexOf(e2))) 
                                        + "/" + (stage.mobNumToKill.get(stage.mobsToKill.indexOf(e)));
                                if (depends.getPlaceholderApi() != null) {
                                    message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
                                }
                                if (getSettings().canTranslateItems()) {
                                    localeQuery.sendMessage(quester.getPlayer(), message, e, null);
                                } else {
                                    quester.getPlayer().sendMessage(message.replace("<mob>", 
                                            MiscUtil.getProperMobName(e)));
                                }
                            } else {
                                String message = ChatColor.GREEN + Lang.get(quester.getPlayer(), "killAtLocation") + " " 
                                        + ChatColor.AQUA + "<mob>" + ChatColor.GREEN + ": " 
                                        + (data.mobNumKilled.get(data.mobsKilled.indexOf(e2))) 
                                        + "/" + (stage.mobNumToKill.get(stage.mobsToKill.indexOf(e)));
                                message = message.replace("<location>", 
                                        stage.killNames.get(stage.mobsToKill.indexOf(e)));
                                if (depends.getPlaceholderApi() != null) {
                                    message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
                                }
                                if (getSettings().canTranslateItems()) {
                                    localeQuery.sendMessage(quester.getPlayer(), message, e, null);
                                } else {
                                    quester.getPlayer().sendMessage(message.replace("<mob>", 
                                            MiscUtil.getProperMobName(e)));
                                }
                            }
                        } else {
                            if (stage.locationsToKillWithin.isEmpty()) {
                                String message = ChatColor.GRAY + Lang.get(quester.getPlayer(), "kill") + " " 
                                        + ChatColor.AQUA + "<mob>" + ChatColor.GRAY + ": " 
                                        + (data.mobNumKilled.get(data.mobsKilled.indexOf(e2))) 
                                        + "/" + (stage.mobNumToKill.get(stage.mobsToKill.indexOf(e)));
                                if (depends.getPlaceholderApi() != null) {
                                    message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
                                }
                                if (getSettings().canTranslateItems()) {
                                    localeQuery.sendMessage(quester.getPlayer(), message, e, null);
                                } else {
                                    quester.getPlayer().sendMessage(message.replace("<mob>", 
                                            MiscUtil.getProperMobName(e)));
                                }
                            } else {
                                String message = ChatColor.GRAY + Lang.get(quester.getPlayer(), "killAtLocation") + " " 
                                        + ChatColor.AQUA + "<mob>" + ChatColor.GRAY + ": " 
                                        + (data.mobNumKilled.get(data.mobsKilled.indexOf(e2))) 
                                        + "/" + (stage.mobNumToKill.get(stage.mobsToKill.indexOf(e)));
                                message = message.replace("<location>", 
                                        stage.killNames.get(stage.mobsToKill.indexOf(e)));
                                if (depends.getPlaceholderApi() != null) {
                                    message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
                                }
                                if (getSettings().canTranslateItems()) {
                                    localeQuery.sendMessage(quester.getPlayer(), message, e, null);
                                } else {
                                    quester.getPlayer().sendMessage(message.replace("<mob>", 
                                            MiscUtil.getProperMobName(e)));
                                }
                            }
                        }
                    }
                }
            }
        }
        if (stage.playersToKill != null) {
            if (data.getPlayersKilled() < stage.playersToKill) {
                String message = ChatColor.GREEN + Lang.get(quester.getPlayer(), "killPlayer")
                        + ChatColor.GREEN + ": " + data.getPlayersKilled() + "/" + stage.playersToKill;
                if (depends.getPlaceholderApi() != null) {
                    message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
                }
                quester.getPlayer().sendMessage(message);
            } else {
                String message = ChatColor.GRAY + Lang.get(quester.getPlayer(), "killPlayer")
                        + ChatColor.GRAY + ": " + data.getPlayersKilled() + "/" + stage.playersToKill;
                if (depends.getPlaceholderApi() != null) {
                    message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
                }
                quester.getPlayer().sendMessage(message);
            }
        }
        int index = 0;
        for (ItemStack is : stage.itemsToDeliver) {
            int delivered = 0;
            if (data.itemsDelivered.containsKey(is)) {
                delivered = data.itemsDelivered.get(is);
            }
            int amt = is.getAmount();
            Integer npc = stage.itemDeliveryTargets.get(index);
            index++;
            if (delivered < amt) {
                String message = ChatColor.GREEN + Lang.get(quester.getPlayer(), "deliver")
                        + ChatColor.GREEN + ": " + delivered + "/" + is.getAmount();
                message = message.replace("<npc>", getNPCName(npc));
                if (depends.getPlaceholderApi() != null) {
                    message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
                }
                if (getSettings().canTranslateItems() && !is.hasItemMeta() && !is.getItemMeta().hasDisplayName()) {
                    localeQuery.sendMessage(quester.getPlayer(), message, is.getType(), is.getDurability(), 
                            is.getEnchantments());
                } else {
                    quester.getPlayer().sendMessage(message.replace("<item>", ItemUtil.getName(is)));
                }
            } else {
                String message = ChatColor.GREEN + Lang.get(quester.getPlayer(), "deliver")
                        + ChatColor.GREEN + ": " + delivered + "/" + is.getAmount();
                message = message.replace("<npc>", getNPCName(npc));
                if (depends.getPlaceholderApi() != null) {
                    message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
                }
                if (getSettings().canTranslateItems() && !is.hasItemMeta() && !is.getItemMeta().hasDisplayName()) {
                    localeQuery.sendMessage(quester.getPlayer(), message, is.getType(), is.getDurability(), 
                            is.getEnchantments());
                } else {
                    quester.getPlayer().sendMessage(message.replace("<item>", ItemUtil.getName(is)));
                }
            }
        }
        for (Integer n : stage.citizensToInteract) {
            for (Entry<Integer, Boolean> e : data.citizensInteracted.entrySet()) {
                if (e.getKey().equals(n)) {
                    if (e.getValue() == false) {
                        String message = ChatColor.GREEN + Lang.get(quester.getPlayer(), "talkTo");
                        message = message.replace("<npc>", getNPCName(n));
                        if (depends.getPlaceholderApi() != null) {
                            message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
                        }
                        quester.getPlayer().sendMessage(message);
                    } else {
                        String message = ChatColor.GRAY + Lang.get(quester.getPlayer(), "talkTo");
                        message = message.replace("<npc>", getNPCName(n));
                        if (depends.getPlaceholderApi() != null) {
                            message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
                        }
                        quester.getPlayer().sendMessage(message);
                    }
                }
            }
        }
        for (Integer n : stage.citizensToKill) {
            for (Integer n2 : data.citizensKilled) {
                if (n.equals(n2)) {
                    if (data.citizenNumKilled.size() > data.citizensKilled.indexOf(n2) 
                            && stage.citizenNumToKill.size() > stage.citizensToKill.indexOf(n)) {
                        if (data.citizenNumKilled.get(data.citizensKilled.indexOf(n2)) 
                                < stage.citizenNumToKill.get(stage.citizensToKill.indexOf(n))) {
                            String message = ChatColor.GREEN + Lang.get(quester.getPlayer(), "kill") + " " 
                                    + getNPCName(n) + ChatColor.GREEN + " " 
                                    + data.citizenNumKilled.get(stage.citizensToKill.indexOf(n)) + "/" 
                                    + stage.citizenNumToKill.get(stage.citizensToKill.indexOf(n));
                            if (depends.getPlaceholderApi() != null) {
                                message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
                            }
                            quester.getPlayer().sendMessage(message);
                        } else {
                            String message = ChatColor.GRAY + Lang.get(quester.getPlayer(), "kill") + " " 
                                    + getNPCName(n) + ChatColor.GRAY + " " 
                                    + data.citizenNumKilled.get(stage.citizensToKill.indexOf(n)) + "/" 
                                    + stage.citizenNumToKill.get(stage.citizensToKill.indexOf(n));
                            if (depends.getPlaceholderApi() != null) {
                                message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
                            }
                            quester.getPlayer().sendMessage(message);
                        }
                    }
                }
            }
        }
        for (Entry<EntityType, Integer> e : stage.mobsToTame.entrySet()) {
            for (Entry<EntityType, Integer> e2 : data.mobsTamed.entrySet()) {
                if (e.getKey().equals(e2.getKey())) {
                    if (e2.getValue() < e.getValue()) {
                        String message = ChatColor.GREEN + Lang.get(quester.getPlayer(), "tame") + " " + "<mob>" 
                                + ChatColor.GREEN + ": " + e2.getValue() + "/" + e.getValue();
                        if (getSettings().canTranslateItems()) {
                            localeQuery.sendMessage(quester.getPlayer(), message, e.getKey(), null);
                        } else {
                            quester.getPlayer().sendMessage(message.replace("<mob>", 
                                    MiscUtil.getProperMobName(e.getKey())));
                        }
                    } else {
                        String message = ChatColor.GRAY + Lang.get(quester.getPlayer(), "tame") + " " + "<mob>" 
                                + ChatColor.GRAY + ": " + e2.getValue() + "/" + e.getValue();
                        if (getSettings().canTranslateItems()) {
                            localeQuery.sendMessage(quester.getPlayer(), message, e.getKey(), null);
                        } else {
                            quester.getPlayer().sendMessage(message.replace("<mob>", 
                                    MiscUtil.getProperMobName(e.getKey())));
                        }
                    }
                }
            }
        }
        for (Entry<DyeColor, Integer> e : stage.sheepToShear.entrySet()) {
            for (Entry<DyeColor, Integer> e2 : data.sheepSheared.entrySet()) {
                if (e.getKey().equals(e2.getKey())) {
                    if (e2.getValue() < e.getValue()) {
                        String message = ChatColor.GREEN + Lang.get(quester.getPlayer(), "shearSheep") 
                                + ChatColor.GREEN + ": " + e2.getValue() + "/" + e.getValue();
                        message = message.replace("<color>", e.getKey().name().toLowerCase());
                        quester.getPlayer().sendMessage(message);
                    } else {
                        String message = ChatColor.GRAY + Lang.get(quester.getPlayer(), "shearSheep") 
                                + ChatColor.GRAY + ": " + e2.getValue() + "/" + e.getValue();
                        message = message.replace("<color>", e.getKey().name().toLowerCase());
                        quester.getPlayer().sendMessage(message);
                    }
                }
            }
        }
        for (Location l : stage.locationsToReach) {
            for (Location l2 : data.locationsReached) {
                if (l.equals(l2)) {
                    if (!data.hasReached.isEmpty()) {
                        if (data.hasReached.get(data.locationsReached.indexOf(l2)) == false) {
                            String message = ChatColor.GREEN + Lang.get(quester.getPlayer(), "goTo");
                            message = message.replace("<location>", 
                                    stage.locationNames.get(stage.locationsToReach.indexOf(l)));
                            quester.getPlayer().sendMessage(message);
                        } else {
                            String message = ChatColor.GRAY + Lang.get(quester.getPlayer(), "goTo");
                            message = message.replace("<location>", 
                                    stage.locationNames.get(stage.locationsToReach.indexOf(l)));
                            quester.getPlayer().sendMessage(message);
                        }
                    }
                }
            }
        }
        for (String s : stage.passwordDisplays) {
            if (data.passwordsSaid.containsKey(s)) {
                Boolean b = data.passwordsSaid.get(s);
                if (b != null && !b) {
                    quester.getPlayer().sendMessage(ChatColor.GREEN + s);
                } else {
                    quester.getPlayer().sendMessage(ChatColor.GRAY + s);
                }
            }
        }
        for (CustomObjective co : stage.customObjectives) {
            int countsIndex = 0;
            String display = co.getDisplay();
            List<String> unfinished = new LinkedList<String>();
            List<String> finished = new LinkedList<String>();
            for (Entry<String, Integer> entry : data.customObjectiveCounts.entrySet()) {
                if (co.getName().equals(entry.getKey())) {
                    for (Entry<String,Object> prompt : co.getData()) {
                        String replacement = "%" + prompt.getKey() + "%";
                        try {
                            for (Entry<String, Object> e : stage.customObjectiveData) {
                                if (e.getKey().equals(prompt.getKey())) {
                                    if (display.contains(replacement)) {
                                        display = display.replace(replacement, ((String) e.getValue()));
                                    }
                                }
                            }
                        } catch (NullPointerException ne) {
                            getLogger().severe("Unable to fetch display for " + co.getName() + " on " 
                                    + quest.getName());
                            ne.printStackTrace();
                        }
                    }
                    if (entry.getValue() < stage.customObjectiveCounts.get(countsIndex)) {
                        if (co.canShowCount()) {
                            display = display.replace("%count%", entry.getValue() + "/" 
                                    + stage.customObjectiveCounts.get(countsIndex));
                        }
                        unfinished.add(display);
                    } else {
                        if (co.canShowCount()) {
                            display = display.replace("%count%", stage.customObjectiveCounts.get(countsIndex) 
                                    + "/" + stage.customObjectiveCounts.get(countsIndex));
                        }
                        finished.add(display);
                    }
                }
                countsIndex++;
            }
            for (String s : unfinished) {
                quester.getPlayer().sendMessage(ChatColor.GREEN + s);
            }
            for (String s : finished) {
                quester.getPlayer().sendMessage(ChatColor.GRAY + s);
            }
        }
    }
    
    /**
     * Show the player a list of their quests
     * 
     * @deprecated Use #listQuests(Quester)
     * @param player Player to show the list
     * @param page Page to display, with 7 quests per page
     */
    public void listQuests(Player player, int page) {
        listQuests(getQuester(player.getUniqueId()), page);
    }
    
    /**
     * Show the player a list of their quests
     * 
     * @param player Player to show the list
     * @param page Page to display, with 7 quests per page
     */
    public void listQuests(Quester quester, int page) {
        // Although we could copy the quests list to a new object, we instead opt to
        // duplicate code to improve efficiency if ignore-locked-quests is set to 'false'
        int rows = 7;
        Player player = quester.getPlayer();
        if (getSettings().canIgnoreLockedQuests()) {
            LinkedList<Quest> available = new LinkedList<Quest>();
            for (Quest q : quests) {
                if (quester.getCompletedQuests().contains(q.getName()) == false) {
                    if (q.testRequirements(player)) {
                        available.add(q);
                    }
                } else if (q.getPlanner().hasCooldown() && quester.getCooldownDifference(q) < 0) {
                    if (q.testRequirements(player)) {
                        available.add(q);
                    }
                }
            }
            if ((available.size() + rows) <= (page * rows) || available.size() == 0) {
                player.sendMessage(ChatColor.YELLOW + Lang.get(player, "pageNotExist"));
            } else {
                player.sendMessage(ChatColor.GOLD + Lang.get(player, "questListTitle"));
                int fromOrder = (page - 1) * rows;
                List<Quest> subQuests;
                if (available.size() >= (fromOrder + rows)) {
                    subQuests = available.subList((fromOrder), (fromOrder + rows));
                } else {
                    subQuests = available.subList((fromOrder), available.size());
                }
                fromOrder++;
                for (Quest q : subQuests) {
                    player.sendMessage(ChatColor.YELLOW + Integer.toString(fromOrder) + ". " + q.getName());
                    fromOrder++;
                }
                int numPages = (int) Math.ceil(((double) available.size()) / ((double) rows));
                String msg = Lang.get(player, "pageFooter");
                msg = msg.replace("<current>", String.valueOf(page));
                msg = msg.replace("<all>", String.valueOf(numPages));
                player.sendMessage(ChatColor.GOLD + msg);
            }
        } else {
            if ((quests.size() + rows) <= (page * rows) || quests.size() == 0) {
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
    }

    /**
     * Reload quests, actions, config settings, lang and modules, and player data
     */
    public void reloadQuests() {
        for (Quester quester : questers) {
            quester.saveData();
        }
        quests.clear();
        events.clear();
        
        loadQuests();
        loadActions();
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
            quester.loadData();
            for (Quest q : quester.currentQuests.keySet()) {
                quester.checkQuest(q);
            }
        }
    }

    /**
     * Get online Quester from player UUID
     * 
     * @param id Player UUID
     * @return Quester, or null if offline
     */
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

    /**
     * Get online Quester from player name
     * 
     * @param name Player name
     * @return Quester, or null if offline
     */
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

    /**
     * Get a list of all online Questers
     * 
     * @return list of online Questers
     */
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

    /**
     * Load quests from file
     */
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
                    quest.id = questKey;
                    if (config.contains("quests." + questKey + ".name")) {
                        quest.setName(parseString(config.getString("quests." + questKey + ".name"), quest));
                    } else {
                        skipQuestProcess("Quest block \'" + questKey + "\' is missing " + ChatColor.RED + "name:");
                    }
                    if (depends.getCitizens() != null && config.contains("quests." + questKey + ".npc-giver-id")) {
                        if (CitizensAPI.getNPCRegistry().getById(config.getInt("quests." + questKey + ".npc-giver-id")) 
                                != null) {
                            quest.npcStart = CitizensAPI.getNPCRegistry().getById(config.getInt("quests." + questKey 
                                    + ".npc-giver-id"));
                            questNpcs.add(CitizensAPI.getNPCRegistry().getById(config.getInt("quests." + questKey 
                                    + ".npc-giver-id")));
                        } else {
                            skipQuestProcess("npc-giver-id: for Quest " + quest.getName() + " is not a valid NPC id!");
                        }
                    }
                    if (config.contains("quests." + questKey + ".block-start")) {
                        Location location = getLocation(config.getString("quests." + questKey + ".block-start"));
                        if (location != null) {
                            quest.blockStart = location;
                        } else {
                            skipQuestProcess(new String[] { "block-start: for Quest " + quest.getName() 
                                    + " is not in proper location format!", "Proper format is: \"WorldName x y z\"" });
                        }
                    }
                    if (config.contains("quests." + questKey + ".region")) {
                        String region = config.getString("quests." + questKey + ".region");
                        boolean exists = false;
                        for (World world : getServer().getWorlds()) {
                            if (getDependencies().getWorldGuardApi().getRegionManager(world) != null) {
                                if (getDependencies().getWorldGuardApi().getRegionManager(world).hasRegion(region)) {
                                    quest.region = region;
                                    exists = true;
                                    break;
                                }
                            }
                        }
                        if (!exists) {
                            skipQuestProcess("region: for Quest " + quest.getName() 
                                    + " is not a valid WorldGuard region!");
                        }
                    }
                    if (config.contains("quests." + questKey + ".gui-display")) {
                        ItemStack stack = config.getItemStack("quests." + questKey + ".gui-display");
                        if (stack == null) {
                            // Legacy
                            String item = config.getString("quests." + questKey + ".gui-display");
                            try {
                                stack = ItemUtil.readItemStack(item);
                            } catch (Exception e) {
                                skipQuestProcess(item + " inside items: GUI Display in Quest " + quest.getName() 
                                        + "is not properly formatted!");
                            }
                        }
                        if (stack != null) {
                            quest.guiDisplay = stack;
                        }
                    }
                    if (config.contains("quests." + questKey + ".redo-delay")) {
                        // Legacy
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
                    if (config.contains("quests." + questKey + ".action")) {
                        Action act = Action.loadAction(config.getString("quests." + questKey + ".action"), this);
                        if (act != null) {
                            quest.initialAction = act;
                        } else {
                            skipQuestProcess("Initial Action in Quest " + quest.getName() + " failed to load.");
                        }
                    } else if (config.contains("quests." + questKey + ".event")) {
                        Action evt = Action.loadAction(config.getString("quests." + questKey + ".event"), this);
                        if (evt != null) {
                            quest.initialAction = evt;
                        } else {
                            skipQuestProcess("Initial Action in Quest " + quest.getName() + " failed to load.");
                        }
                    }
                    if (config.contains("quests." + questKey + ".requirements")) {
                        loadQuestRequirements(config, questsSection, quest, questKey);
                    }
                    if (config.contains("quests." + questKey + ".planner")) {
                        loadQuestPlanner(config, questsSection, quest, questKey);
                    }
                    if (config.contains("quests." + questKey + ".options")) {
                        loadQuestOptions(config, questsSection, quest, questKey);
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

    @SuppressWarnings("unchecked")
    private void loadQuestRewards(FileConfiguration config, Quest quest, String questKey) throws SkipQuest {
        Rewards rews = quest.getRewards();
        if (config.contains("quests." + questKey + ".rewards.items")) {
            LinkedList<ItemStack> temp = new LinkedList<ItemStack>(); // TODO - should maybe be = rews.getItems() ?
            List<ItemStack> stackList = (List<ItemStack>) config.get("quests." + questKey + ".rewards.items");
            if (Quests.checkList(stackList, ItemStack.class)) {
                for (ItemStack stack : stackList) {
                    if (stack != null) {
                        temp.add(stack);
                    }
                }
            } else {
                // Legacy
                if (Quests.checkList(stackList, String.class)) {
                    List<String> items = config.getStringList("quests." + questKey + ".rewards.items");
                    for (String item : items) {
                        try {
                            ItemStack stack = ItemUtil.readItemStack(item);
                            if (stack != null) {
                                temp.add(stack);
                            }
                        } catch (Exception e) {
                            skipQuestProcess("" + item + " inside items: Reward in Quest " + quest.getName() 
                                    + " is not properly formatted!");
                        }
                    }
                } else {
                    skipQuestProcess("items: Reward in Quest " + quest.getName() + " is not properly formatted!");
                }
            }
            rews.setItems(temp);
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
        if (config.contains("quests." + questKey + ".rewards.commands-override-display")) {
            if (Quests.checkList(config.getList("quests." + questKey + ".rewards.commands-override-display"), 
                    String.class)) {
                rews.setCommandsOverrideDisplay(config.getStringList("quests." + questKey 
                        + ".rewards.commands-override-display"));
            } else {
                skipQuestProcess("commands-override-display: Reward in Quest " + quest.getName() 
                        + " is not a list of strings!");
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
                        if (Quests.checkList(config.getList("quests." + questKey + ".rewards.mcmmo-levels"), 
                                Integer.class)) {
                            for (String skill : config.getStringList("quests." + questKey + ".rewards.mcmmo-skills")) {
                                if (depends.getMcmmoClassic() == null) {
                                    skipQuestProcess("" + skill + " in mcmmo-skills: Reward in Quest " + quest.getName()
                                            + " requires the mcMMO plugin!");
                                } else if (Quests.getMcMMOSkill(skill) == null) {
                                    skipQuestProcess("" + skill + " in mcmmo-skills: Reward in Quest " + quest.getName()
                                            + " is not a valid mcMMO skill name!");
                                }
                            }
                            rews.setMcmmoSkills(config.getStringList("quests." + questKey + ".rewards.mcmmo-skills"));
                            rews.setMcmmoAmounts(config.getIntegerList("quests." + questKey + ".rewards.mcmmo-levels"));
                        } else {
                            skipQuestProcess("mcmmo-levels: Reward in Quest " + quest.getName() 
                                    + " is not a list of numbers!");
                        }
                    } else {
                        skipQuestProcess("Rewards for Quest " + quest.getName() + " is missing mcmmo-levels:");
                    }
                } else {
                    skipQuestProcess("mcmmo-skills: Reward in Quest " + quest.getName() 
                            + " is not a list of mcMMO skill names!");
                }
            }
        }
        if (depends.isPluginAvailable("Heroes")) {
            if (config.contains("quests." + questKey + ".rewards.heroes-exp-classes")) {
                if (Quests.checkList(config.getList("quests." + questKey + ".rewards.heroes-exp-classes"), 
                        String.class)) {
                    if (config.contains("quests." + questKey + ".rewards.heroes-exp-amounts")) {
                        if (Quests.checkList(config.getList("quests." + questKey + ".rewards.heroes-exp-amounts"), 
                                Double.class)) {
                            for (String heroClass : config.getStringList("quests." + questKey 
                                    + ".rewards.heroes-exp-classes")) {
                                if (depends.getHeroes() == null) {
                                    skipQuestProcess("" + heroClass + " in heroes-exp-classes: Reward in Quest " 
                                            + quest.getName() + " requires the Heroes plugin!");
                                } else if (depends.getHeroes().getClassManager().getClass(heroClass) == null) {
                                    skipQuestProcess("" + heroClass + " in heroes-exp-classes: Reward in Quest " 
                                            + quest.getName() + " is not a valid Heroes class name!");
                                }
                            }
                            rews.setHeroesClasses(config.getStringList("quests." + questKey 
                                    + ".rewards.heroes-exp-classes"));
                            rews.setHeroesAmounts(config.getDoubleList("quests." + questKey 
                                    + ".rewards.heroes-exp-amounts"));
                        } else {
                            skipQuestProcess("heroes-exp-amounts: Reward in Quest " + quest.getName() 
                                    + " is not a list of experience amounts (decimal numbers)!");
                        }
                    } else {
                        skipQuestProcess("Rewards for Quest " + quest.getName() + " is missing heroes-exp-amounts:");
                    }
                } else {
                    skipQuestProcess("heroes-exp-classes: Reward in Quest " + quest.getName() 
                            + " is not a list of Heroes classes!");
                }
            }
        }
        if (depends.isPluginAvailable("PhatLoots")) {
            if (config.contains("quests." + questKey + ".rewards.phat-loots")) {
                if (Quests.checkList(config.getList("quests." + questKey + ".rewards.phat-loots"), String.class)) {
                    for (String loot : config.getStringList("quests." + questKey + ".rewards.phat-loots")) {
                        if (depends.getPhatLoots() == null) {
                            skipQuestProcess("" + loot + " in phat-loots: Reward in Quest " + quest.getName() 
                                    + " requires the PhatLoots plugin!");
                        } else if (PhatLootsAPI.getPhatLoot(loot) == null) {
                            skipQuestProcess("" + loot + " in phat-loots: Reward in Quest " + quest.getName() 
                                    + " is not a valid PhatLoot name!");
                        }
                    }
                    rews.setPhatLoots(config.getStringList("quests." + questKey + ".rewards.phat-loots"));
                } else {
                    skipQuestProcess("phat-loots: Reward in Quest " + quest.getName() + " is not a list of PhatLoots!");
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void loadQuestRequirements(FileConfiguration config, ConfigurationSection questsSection, Quest quest, 
            String questKey) throws SkipQuest {
        Requirements reqs = quest.getRequirements();
        if (config.contains("quests." + questKey + ".requirements.fail-requirement-message")) {
            reqs.setFailRequirements(parseString(config.getString("quests." + questKey 
                    + ".requirements.fail-requirement-message"), quest));
        } else {
            skipQuestProcess("Requirements for Quest " + quest.getName() + " is missing fail-requirement-message:");
        }
        if (config.contains("quests." + questKey + ".requirements.items")) {
            List<ItemStack> temp = reqs.getItems(); // TODO - should maybe be = newLinkedList<ItemStack>() ?
            List<ItemStack> stackList = (List<ItemStack>) config.get("quests." + questKey + ".requirements.items");
            if (checkList(stackList, ItemStack.class)) {
                for (ItemStack stack : stackList) {
                    if (stack != null) {
                        temp.add(stack);
                    }
                }
            } else {
                // Legacy
                List<String> items = config.getStringList("quests." + questKey + ".requirements.items");
                if (checkList(items, String.class)) {
                    for (String item : items) {
                        try {
                            ItemStack stack = ItemUtil.readItemStack(item);
                            if (stack != null) {
                                temp.add(stack);
                            }
                        } catch (Exception e) {
                            skipQuestProcess("" + item + " inside items: Requirement in Quest " + quest.getName() 
                                    + " is not properly formatted!");
                        }
                    }
                } else {
                    skipQuestProcess("items: Requirement in Quest " + quest.getName() + " is not properly formatted!");
                }
            }
            if (config.contains("quests." + questKey + ".requirements.remove-items")) {
                if (Quests.checkList(config.getList("quests." + questKey + ".requirements.remove-items"), 
                        Boolean.class)) {
                    reqs.setRemoveItems(config.getBooleanList("quests." + questKey + ".requirements.remove-items"));
                } else {
                    skipQuestProcess("remove-items: Requirement for Quest " + quest.getName() 
                            + " is not a list of true/false values!");
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
                    skipQuestProcess(new String[] { "" + ChatColor.LIGHT_PURPLE + failedQuest 
                            + " inside quests: Requirement for Quest " + quest.getName() 
                            + " is not a valid Quest name!", "Make sure you aren\'t using the colons." });
                }
            } else {
                skipQuestProcess("quest-blocks: Requirement for Quest " + quest.getName() 
                        + " is not a list of Quest names!");
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
                            + quest.getName() + " is not a valid Quest name!", "Make sure you aren\'t using colons." });
                }
            } else {
                skipQuestProcess("quests: Requirement for Quest " + quest.getName() + " is not a list of Quest names!");
            }
        }
        if (config.contains("quests." + questKey + ".requirements.permissions")) {
            if (Quests.checkList(config.getList("quests." + questKey + ".requirements.permissions"), String.class)) {
                reqs.setPermissions(config.getStringList("quests." + questKey + ".requirements.permissions"));
            } else {
                skipQuestProcess("permissions: Requirement for Quest " + quest.getName() 
                        + " is not a list of permissions!");
            }
        }
        if (config.contains("quests." + questKey + ".requirements.mcmmo-skills")) {
            if (Quests.checkList(config.getList("quests." + questKey + ".requirements.mcmmo-skills"), String.class)) {
                if (config.contains("quests." + questKey + ".requirements.mcmmo-amounts")) {
                    if (Quests.checkList(config.getList("quests." + questKey + ".requirements.mcmmo-amounts"), 
                            Integer.class)) {
                        List<String> skills = config.getStringList("quests." + questKey + ".requirements.mcmmo-skills");
                        List<Integer> amounts = config.getIntegerList("quests." + questKey 
                                + ".requirements.mcmmo-amounts");
                        if (skills.size() != amounts.size()) {
                            skipQuestProcess("mcmmo-skills: and mcmmo-amounts: in requirements: for Quest " 
                                    + quest.getName() + " are not the same size!");
                        }
                        reqs.setMcmmoSkills(skills);
                        reqs.setMcmmoAmounts(amounts);
                    } else {
                        skipQuestProcess("mcmmo-amounts: Requirement for Quest " + quest.getName() 
                                + " is not a list of numbers!");
                    }
                } else {
                    skipQuestProcess("Requirements for Quest " + quest.getName() + " is missing mcmmo-amounts:");
                }
            } else {
                skipQuestProcess("mcmmo-skills: Requirement for Quest " + quest.getName() 
                        + " is not a list of skills!");
            }
        }
        if (config.contains("quests." + questKey + ".requirements.heroes-primary-class")) {
            String className = config.getString("quests." + questKey + ".requirements.heroes-primary-class");
            HeroClass hc = depends.getHeroes().getClassManager().getClass(className);
            if (hc != null && hc.isPrimary()) {
                reqs.setHeroesPrimaryClass(hc.getName());
            } else if (hc != null) {
                skipQuestProcess("heroes-primary-class: Requirement for Quest " + quest.getName() 
                        + " is not a primary Heroes class!");
            } else {
                skipQuestProcess("heroes-primary-class: Requirement for Quest " + quest.getName() 
                        + " is not a valid Heroes class!");
            }
        }
        if (config.contains("quests." + questKey + ".requirements.heroes-secondary-class")) {
            String className = config.getString("quests." + questKey + ".requirements.heroes-secondary-class");
            HeroClass hc = depends.getHeroes().getClassManager().getClass(className);
            if (hc != null && hc.isSecondary()) {
                reqs.setHeroesSecondaryClass(hc.getName());
            } else if (hc != null) {
                skipQuestProcess("heroes-secondary-class: Requirement for Quest " + quest.getName() 
                        + " is not a secondary Heroes class!");
            } else {
                skipQuestProcess("heroes-secondary-class: Requirement for Quest " + quest.getName() 
                        + " is not a valid Heroes class!");
            }
        }
    }
    
    private void loadQuestPlanner(FileConfiguration config, ConfigurationSection questsSection, Quest quest, 
            String questKey) throws SkipQuest {
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
    
    private void loadQuestOptions(FileConfiguration config, ConfigurationSection questsSection, Quest quest, 
            String questKey) throws SkipQuest {
        Options opts = quest.getOptions();
        if (config.contains("quests." + questKey + ".options.allow-commands")) {
            opts.setAllowCommands(config.getBoolean("quests." + questKey + ".options.allow-commands"));
        }
        if (config.contains("quests." + questKey + ".options.allow-quitting")) {
            opts.setAllowQuitting(config.getBoolean("quests." + questKey + ".options.allow-quitting"));
        } else if (getConfig().contains("allow-quitting")) {
            // Legacy
            opts.setAllowQuitting(getConfig().getBoolean("allow-quitting"));
        }
        if (config.contains("quests." + questKey + ".options.use-dungeonsxl-plugin")) {
            opts.setUseDungeonsXLPlugin(config.getBoolean("quests." + questKey + ".options.use-dungeonsxl-plugin"));
        }
        if (config.contains("quests." + questKey + ".options.use-parties-plugin")) {
            opts.setUsePartiesPlugin(config.getBoolean("quests." + questKey + ".options.use-parties-plugin"));
        }
        if (config.contains("quests." + questKey + ".options.share-progress-level")) {
            opts.setShareProgressLevel(config.getInt("quests." + questKey + ".options.share-progress-level"));
        }
        if (config.contains("quests." + questKey + ".options.require-same-quest")) {
            opts.setRequireSameQuest(config.getBoolean("quests." + questKey + ".options.require-same-quest"));
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

    @SuppressWarnings({ "unchecked", "unused" })
    private void processStages(Quest quest, FileConfiguration config, String questKey) throws StageFailedException {
        ConfigurationSection questStages = config.getConfigurationSection("quests." + questKey + ".stages.ordered");
        for (String s2 : questStages.getKeys(false)) {
            Stage oStage = new Stage();
            List<String> breakNames = new LinkedList<String>();
            List<Integer> breakAmounts = new LinkedList<Integer>();
            List<Short> breakDurability = new LinkedList<Short>();
            List<String> damageNames = new LinkedList<String>();
            List<Integer> damageAmounts = new LinkedList<Integer>();
            List<Short> damageDurability = new LinkedList<Short>();
            List<String> placeNames = new LinkedList<String>();
            List<Integer> placeAmounts = new LinkedList<Integer>();
            List<Short> placeDurability = new LinkedList<Short>();
            List<String> useNames = new LinkedList<String>();
            List<Integer> useAmounts = new LinkedList<Integer>();
            List<Short> useDurability = new LinkedList<Short>();
            List<String> cutNames = new LinkedList<String>();
            List<Integer> cutAmounts = new LinkedList<Integer>();
            List<Short> cutDurability = new LinkedList<Short>();
            List<EntityType> mobsToKill = new LinkedList<EntityType>();
            List<Integer> mobNumToKill = new LinkedList<Integer>();
            List<Location> locationsToKillWithin = new LinkedList<Location>();
            List<Integer> radiiToKillWithin = new LinkedList<Integer>();
            List<String> areaNames = new LinkedList<String>();
            List<ItemStack> itemsToCraft = new LinkedList<ItemStack>();
            List<ItemStack> itemsToSmelt = new LinkedList<ItemStack>();
            List<Enchantment> enchantments = new LinkedList<Enchantment>();
            List<Material> itemsToEnchant = new LinkedList<Material>();
            List<Integer> amountsToEnchant = new LinkedList<Integer>();
            List<ItemStack> itemsToBrew = new LinkedList<ItemStack>();
            List<Integer> npcIdsToTalkTo = new LinkedList<Integer>();
            List<ItemStack> itemsToDeliver= new LinkedList<ItemStack>();
            List<Integer> itemDeliveryTargetIds = new LinkedList<Integer>();
            List<String> deliveryMessages = new LinkedList<String>();
            List<Integer> npcIdsToKill = new LinkedList<Integer>();
            List<Integer> npcAmountsToKill = new LinkedList<Integer>();
            // Denizen script load
            if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".script-to-run")) {
                if (getDependencies().getDenizenAPI().containsScript(config.getString("quests." + questKey 
                        + ".stages.ordered." + s2 + ".script-to-run"))) {
                    trigger = new DenizenTrigger(this);
                    oStage.script = config.getString("quests." + questKey + ".stages.ordered." + s2 + ".script-to-run");
                } else {
                    stageFailed("script-to-run: in Stage " + s2 + " of Quest " + quest.getName() 
                            + " is not a Denizen script!");
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".break-block-names")) {
                if (checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".break-block-names"), 
                        String.class)) {
                    breakNames = config.getStringList("quests." + questKey + ".stages.ordered." + s2 
                            + ".break-block-names");
                } else {
                    stageFailed("break-block-names: in Stage " + s2 + " of Quest " + quest.getName() 
                            + " is not a list of strings!");
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".break-block-amounts")) {
                    if (checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 
                            + ".break-block-amounts"), Integer.class)) {
                        breakAmounts = config.getIntegerList("quests." + questKey + ".stages.ordered." + s2 
                                + ".break-block-amounts");
                    } else {
                        stageFailed("break-block-amounts: in Stage " + s2 + " of Quest " + quest.getName() 
                                + " is not a list of numbers!");
                    }
                } else {
                    stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing break-block-amounts:");
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".break-block-durability")) {
                    if (checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 
                            + ".break-block-durability"), Integer.class)) {
                        breakDurability = config.getShortList("quests." + questKey + ".stages.ordered." + s2 
                                + ".break-block-durability");
                    } else {
                        stageFailed("break-block-durability: in Stage " + s2 + " of Quest " + quest.getName() 
                                + " is not a list of numbers!");
                    }
                } else {
                    stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing break-block-durability:");
                }
            }
            int breakIndex = 0;
            for (String s : breakNames) {
                ItemStack is;
                if (breakDurability.get(breakIndex) != -1) {
                    is = ItemUtil.processItemStack(s, breakAmounts.get(breakIndex), breakDurability.get(breakIndex));
                } else {
                    // Legacy
                    is = ItemUtil.processItemStack(s, breakAmounts.get(breakIndex), (short) 0);
                }
                if (Material.matchMaterial(s) != null) {
                    oStage.blocksToBreak.add(is);
                } else {
                    stageFailed("" + s + " inside break-block-names: inside Stage " + s2 + " of Quest " 
                            + quest.getName() + " is not a valid item name!");
                }
                breakIndex++;
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".damage-block-names")) {
                if (checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".damage-block-names"), 
                        String.class)) {
                    damageNames = config.getStringList("quests." + questKey + ".stages.ordered." + s2 
                            + ".damage-block-names");
                } else {
                    stageFailed("damage-block-names: in Stage " + s2 + " of Quest " + quest.getName() 
                            + " is not a list of strings!");
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".damage-block-amounts")) {
                    if (checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 
                            + ".damage-block-amounts"), Integer.class)) {
                        damageAmounts = config.getIntegerList("quests." + questKey + ".stages.ordered." + s2 
                                + ".damage-block-amounts");
                    } else {
                        stageFailed("damage-block-amounts: in Stage " + s2 + " of Quest " + quest.getName() 
                                + " is not a list of numbers!");
                    }
                } else {
                    stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing damage-block-amounts:");
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".damage-block-durability")) {
                    if (checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 
                            + ".damage-block-durability"), Integer.class)) {
                        damageDurability = config.getShortList("quests." + questKey + ".stages.ordered." + s2 
                                + ".damage-block-durability");
                    } else {
                        stageFailed("damage-block-durability: in Stage " + s2 + " of Quest " + quest.getName() 
                                + " is not a list of numbers!");
                    }
                } else {
                    stageFailed("Stage " + s2 + " of Quest " + quest.getName() 
                            + " is missing damage-block-durability:");
                }
            }
            int damageIndex = 0;
            for (String s : damageNames) {
                ItemStack is;
                if (damageDurability.get(damageIndex) != -1) {
                    is = ItemUtil.processItemStack(s, damageAmounts.get(damageIndex), 
                            damageDurability.get(damageIndex));
                } else {
                    // Legacy
                    is = ItemUtil.processItemStack(s, damageAmounts.get(damageIndex), (short) 0);
                }
                if (Material.matchMaterial(s) != null) {
                    oStage.blocksToDamage.add(is);
                } else {
                    stageFailed("" + s + " inside damage-block-names: inside Stage " + s2 + " of Quest " 
                            + quest.getName() + " is not a valid item name!");
                }
                damageIndex++;
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".place-block-names")) {
                if (checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".place-block-names"), 
                        String.class)) {
                    placeNames = config.getStringList("quests." + questKey + ".stages.ordered." + s2 
                            + ".place-block-names");
                } else {
                    stageFailed("place-block-names: in Stage " + s2 + " of Quest " + quest.getName() 
                            + " is not a list of strings!");
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".place-block-amounts")) {
                    if (checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 
                            + ".place-block-amounts"), Integer.class)) {
                        placeAmounts = config.getIntegerList("quests." + questKey + ".stages.ordered." + s2 
                                + ".place-block-amounts");
                    } else {
                        stageFailed("place-block-amounts: in Stage " + s2 + " of Quest " + quest.getName() 
                                + " is not a list of numbers!");
                    }
                } else {
                    stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing place-block-amounts:");
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".place-block-durability")) {
                    if (checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 
                            + ".place-block-durability"), Integer.class)) {
                        placeDurability = config.getShortList("quests." + questKey + ".stages.ordered." + s2 
                                + ".place-block-durability");
                    } else {
                        stageFailed("place-block-durability: in Stage " + s2 + " of Quest " + quest.getName() 
                                + " is not a list of numbers!");
                    }
                } else {
                    stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing place-block-durability:");
                }
            }
            int placeIndex = 0;
            for (String s : placeNames) {
                ItemStack is;
                if (placeDurability.get(placeIndex) != -1) {
                    is = ItemUtil.processItemStack(s, placeAmounts.get(placeIndex), placeDurability.get(placeIndex));
                } else {
                    // Legacy
                    is = ItemUtil.processItemStack(s, placeAmounts.get(placeIndex), (short) 0);
                }
                if (Material.matchMaterial(s) != null) {
                    oStage.blocksToPlace.add(is);
                } else {
                    stageFailed("" + s + " inside place-block-names: inside Stage " + s2 + " of Quest " 
                            + quest.getName() + " is not a valid item name!");
                }
                placeIndex++;
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".use-block-names")) {
                if (checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".use-block-names"), 
                        String.class)) {
                    useNames = config.getStringList("quests." + questKey + ".stages.ordered." + s2 
                            + ".use-block-names");
                } else {
                    stageFailed("use-block-names: in Stage " + s2 + " of Quest " + quest.getName() 
                            + " is not a list of strings!");
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".use-block-amounts")) {
                    if (checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".use-block-amounts"),
                            Integer.class)) {
                        useAmounts = config.getIntegerList("quests." + questKey + ".stages.ordered." + s2 
                                + ".use-block-amounts");
                    } else {
                        stageFailed("use-block-amounts: in Stage " + s2 + " of Quest " + quest.getName() 
                                + " is not a list of numbers!");
                    }
                } else {
                    stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing use-block-amounts:");
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".use-block-durability")) {
                    if (checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 
                            + ".use-block-durability"), Integer.class)) {
                        useDurability = config.getShortList("quests." + questKey + ".stages.ordered." + s2 
                                + ".use-block-durability");
                    } else {
                        stageFailed("use-block-durability: in Stage " + s2 + " of Quest " + quest.getName() 
                                + " is not a list of numbers!");
                    }
                } else {
                    stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing use-block-durability:");
                }
            }
            int useIndex = 0;
            for (String s : useNames) {
                ItemStack is;
                if (useDurability.get(useIndex) != -1) {
                    is = ItemUtil.processItemStack(s, useAmounts.get(useIndex), useDurability.get(useIndex));
                } else {
                    // Legacy
                    is = ItemUtil.processItemStack(s, useAmounts.get(useIndex), (short) 0);
                }
                if (Material.matchMaterial(s) != null) {
                    oStage.blocksToUse.add(is);
                } else {
                    stageFailed("" + s + " inside use-block-names: inside Stage " + s2 + " of Quest " + quest.getName()
                            + " is not a valid item name!");
                }
                useIndex++;
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".cut-block-names")) {
                if (checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".cut-block-names"), 
                        String.class)) {
                    cutNames = config.getStringList("quests." + questKey + ".stages.ordered." + s2 
                            + ".cut-block-names");
                } else {
                    stageFailed("cut-block-names: in Stage " + s2 + " of Quest " + quest.getName() 
                            + " is not a list of strings!");
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".cut-block-amounts")) {
                    if (checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".cut-block-amounts"),
                            Integer.class)) {
                        cutAmounts = config.getIntegerList("quests." + questKey + ".stages.ordered." + s2 
                                + ".cut-block-amounts");
                    } else {
                        stageFailed("cut-block-amounts: in Stage " + s2 + " of Quest " + quest.getName() 
                                + " is not a list of numbers!");
                    }
                } else {
                    stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing cut-block-amounts:");
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".cut-block-durability")) {
                    if (checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 
                            + ".cut-block-durability"), Integer.class)) {
                        cutDurability = config.getShortList("quests." + questKey + ".stages.ordered." + s2 
                                + ".cut-block-durability");
                    } else {
                        stageFailed("cut-block-durability: in Stage " + s2 + " of Quest " + quest.getName() 
                                + " is not a list of numbers!");
                    }
                } else {
                    stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing cut-block-durability:");
                }
            }
            int cutIndex = 0;
            for (String s : cutNames) {
                ItemStack is;
                if (cutDurability.get(cutIndex) != -1) {
                    is = ItemUtil.processItemStack(s, cutAmounts.get(cutIndex), cutDurability.get(cutIndex));
                } else {
                    // Legacy
                    is = ItemUtil.processItemStack(s, cutAmounts.get(cutIndex), (short) 0);
                }
                if (Material.matchMaterial(s) != null) {
                    oStage.blocksToCut.add(is);
                } else {
                    stageFailed("" + s + " inside cut-block-names: inside Stage " + s2 + " of Quest " + quest.getName()
                            + " is not a valid item name!");
                }
                cutIndex++;
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".items-to-craft")) {
                itemsToCraft = (List<ItemStack>) config.get("quests." + questKey + ".stages.ordered." + s2 
                        + ".items-to-craft");
                if (checkList(itemsToCraft, ItemStack.class)) {
                    for (ItemStack stack : itemsToCraft) {
                        if (stack != null) {
                            oStage.itemsToCraft.add(stack);
                        } else {
                            stageFailed("" + stack + " inside items-to-craft: inside Stage " + s2 + " of Quest " 
                                    + quest.getName() + " is not formatted properly!");
                        }
                    }
                } else {
                    // Legacy
                    List<String> items = config.getStringList("quests." + questKey + ".stages.ordered." + s2 
                            + ".items-to-craft");
                    if (checkList(items, String.class)) {
                        for (String item : items) {
                            ItemStack is = ItemUtil.readItemStack("" + item);
                            if (is != null) {
                                oStage.itemsToCraft.add(is);
                            } else {
                                stageFailed("" + item + " inside legacy items-to-craft: inside Stage " + s2 
                                        + " of Quest " + quest.getName() + " is not formatted properly!");
                            }
                        }
                    } else {
                        stageFailed("items-to-craft: in Stage " + s2 + " of Quest " + quest.getName() 
                                + " is not formatted properly!");
                    }
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".items-to-smelt")) {
                itemsToSmelt = (List<ItemStack>) config.get("quests." + questKey + ".stages.ordered." + s2 
                        + ".items-to-smelt");
                if (checkList(itemsToSmelt, ItemStack.class)) {
                    for (ItemStack stack : itemsToSmelt) {
                        if (stack != null) {
                            oStage.itemsToSmelt.add(stack);
                        } else {
                            stageFailed("" + stack + " inside items-to-smelt: inside Stage " + s2 + " of Quest " 
                                    + quest.getName() + " is not formatted properly!");
                        }
                    }
                } else {
                    // Legacy
                    List<String> items = config.getStringList("quests." + questKey + ".stages.ordered." + s2 
                            + ".items-to-smelt");
                    if (checkList(items, String.class)) {
                        for (String item : items) {
                            ItemStack is = ItemUtil.readItemStack("" + item);
                            if (is != null) {
                                oStage.itemsToSmelt.add(is);
                            } else {
                                stageFailed("" + item + " inside legacy items-to-smelt: inside Stage " + s2 
                                        + " of Quest " + quest.getName() + " is not formatted properly!");
                            }
                        }
                    } else {
                        stageFailed("items-to-smelt: in Stage " + s2 + " of Quest " + quest.getName() 
                                + " is not formatted properly!");
                    }
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".enchantments")) {
                if (Quests.checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".enchantments"), 
                        String.class)) {
                    for (String enchant : config.getStringList("quests." + questKey + ".stages.ordered." + s2 
                            + ".enchantments")) {
                        Enchantment e = ItemUtil.getEnchantmentFromProperName(enchant);
                        if (e != null) {
                            enchantments.add(e);
                        } else {
                            stageFailed("" + enchant + " inside enchantments: inside Stage " + s2 + " of Quest " 
                                    + quest.getName() + " is not a valid enchantment!");
                        }
                    }
                } else {
                    stageFailed("enchantments: in Stage " + s2 + " of Quest " + quest.getName() 
                            + " is not a list of enchantment names!");
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".enchantment-item-names")) {
                    if (Quests.checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 
                            + ".enchantment-item-names"), String.class)) {
                        for (String item : config.getStringList("quests." + questKey + ".stages.ordered." + s2 
                                + ".enchantment-item-names")) {
                            if (Material.matchMaterial(item) != null) {
                                itemsToEnchant.add(Material.matchMaterial(item));
                            } else {
                                stageFailed("" + item + " inside enchantment-item-names: inside Stage " + s2 
                                        + " of Quest " + quest.getName() + " is not a valid item name!");
                            }
                        }
                    } else {
                        stageFailed("enchantment-item-names: in Stage " + s2 + " of Quest " + quest.getName() 
                                + " is not a valid item name!");
                    }
                } else {
                    stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing enchantment-item-names:");
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".enchantment-amounts")) {
                    if (Quests.checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 
                            + ".enchantment-amounts"), Integer.class)) {
                        amountsToEnchant = config.getIntegerList("quests." + questKey + ".stages.ordered." + s2 
                                + ".enchantment-amounts");
                    } else {
                        stageFailed("enchantment-amounts: in Stage " + s2 + " of Quest " + quest.getName() 
                                + " is not a list of numbers!");
                    }
                } else {
                    stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing enchantment-amounts:");
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".items-to-brew")) {
                itemsToBrew = (List<ItemStack>) config.get("quests." + questKey + ".stages.ordered." + s2 
                        + ".items-to-brew");
                if (checkList(itemsToBrew, ItemStack.class)) {
                    for (ItemStack stack : itemsToBrew) {
                        if (stack != null) {
                            oStage.itemsToBrew.add(stack);
                        } else {
                            stageFailed("" + stack + " inside items-to-brew: inside Stage " + s2 + " of Quest " 
                                    + quest.getName() + " is not formatted properly!");
                        }
                    }
                } else {
                    // Legacy
                    List<String> items = config.getStringList("quests." + questKey + ".stages.ordered." + s2 
                            + ".items-to-brew");
                    if (checkList(items, String.class)) {
                        for (String item : items) {
                            ItemStack is = ItemUtil.readItemStack("" + item);
                            if (is != null) {
                                oStage.itemsToBrew.add(is);
                            } else {
                                stageFailed("" + item + " inside legacy items-to-brew: inside Stage " + s2 
                                        + " of Quest " + quest.getName() + " is not formatted properly!");
                            }
                        }
                    } else {
                        stageFailed("items-to-brew: in Stage " + s2 + " of Quest " + quest.getName() 
                                + " is not formatted properly!");
                    }
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".fish-to-catch")) {
                if (config.getInt("quests." + questKey + ".stages.ordered." + s2 + ".fish-to-catch", -999) != -999) {
                    oStage.fishToCatch = config.getInt("quests." + questKey + ".stages.ordered." + s2 
                            + ".fish-to-catch");
                } else {
                    stageFailed("fish-to-catch: inside Stage " + s2 + " of Quest " + quest.getName() 
                            + " is not a number!");
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".players-to-kill")) {
                if (config.getInt("quests." + questKey + ".stages.ordered." + s2 + ".players-to-kill", -999) != -999) {
                    oStage.playersToKill = config.getInt("quests." + questKey + ".stages.ordered." + s2 
                            + ".players-to-kill");
                } else {
                    stageFailed("players-to-kill: inside Stage " + s2 + " of Quest " + quest.getName() 
                            + " is not a number!");
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".npc-ids-to-talk-to")) {
                if (checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".npc-ids-to-talk-to"), 
                        Integer.class)) {
                    npcIdsToTalkTo = config.getIntegerList("quests." + questKey + ".stages.ordered." + s2 
                            + ".npc-ids-to-talk-to");
                    for (int i : npcIdsToTalkTo) {
                        if (getDependencies().getCitizens() != null) {
                            if (CitizensAPI.getNPCRegistry().getById(i) != null) {
                                questNpcs.add(CitizensAPI.getNPCRegistry().getById(i));
                            } else {
                                stageFailed("" + i + " inside npc-ids-to-talk-to: inside Stage " + s2 + " of Quest " 
                                        + quest.getName() + " is not a valid NPC id!");
                            }
                        } else {
                            stageFailed("Citizens not installed while getting ID " + i 
                                    + " inside npc-ids-to-talk-to: inside Stage " + s2 + " of Quest " 
                                    + quest.getName());
                        }
                        
                    }
                } else {
                    stageFailed("npc-ids-to-talk-to: in Stage " + s2 + " of Quest " + quest.getName() 
                            + " is not a list of numbers!");
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".items-to-deliver")) {
                if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".npc-delivery-ids")) {
                    if (checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".npc-delivery-ids"), 
                            Integer.class)) {
                        if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".delivery-messages")) {
                            itemsToDeliver = (List<ItemStack>) config.get("quests." + questKey + ".stages.ordered." 
                                    + s2 + ".items-to-deliver");
                            itemDeliveryTargetIds = config.getIntegerList("quests." + questKey + ".stages.ordered." 
                                    + s2 + ".npc-delivery-ids");
                            deliveryMessages.addAll(config.getStringList("quests." + questKey + ".stages.ordered." 
                                    + s2 + ".delivery-messages"));
                            int index = 0;
                            if (checkList(itemsToDeliver, ItemStack.class)) {
                                for (ItemStack stack : itemsToDeliver) {
                                    if (stack != null) {
                                        int npcId = itemDeliveryTargetIds.get(index);
                                        index++;
                                        if (stack != null) {
                                            if (getDependencies().getCitizens() != null) {
                                                NPC npc = CitizensAPI.getNPCRegistry().getById(npcId);
                                                if (npc != null) {
                                                    oStage.getItemsToDeliver().add(stack);
                                                    oStage.getItemDeliveryTargets().add(npcId);
                                                    oStage.deliverMessages.addAll(deliveryMessages);
                                                } else {
                                                    stageFailed("Citizens was not installed for ID " + npcId 
                                                            + " inside npc-delivery-ids: inside Stage " + s2 
                                                            + " of Quest " + quest.getName());
                                                }
                                            } else {
                                                stageFailed("" + npcId + " inside npc-delivery-ids: inside Stage " + s2
                                                        + " of Quest " + quest.getName() + " is not a valid NPC id!");
                                            }
                                        } else {
                                            stageFailed("" + stack + " inside items-to-deliver: inside Stage " + s2 
                                                    + " of Quest " + quest.getName() + " is not formatted properly!");
                                        }
                                    }
                                }
                            } else {
                                List<String> items = config.getStringList("quests." + questKey + ".stages.ordered." 
                                        + s2 + ".items-to-deliver");
                                if (checkList(items, String.class)) {
                                    // Legacy
                                    for (String item : items) {
                                        ItemStack is = ItemUtil.readItemStack("" + item);
                                        int npcId = itemDeliveryTargetIds.get(index);
                                        index++;
                                        if (is != null) {
                                            if (getDependencies().getCitizens() != null) {
                                                NPC npc = CitizensAPI.getNPCRegistry().getById(npcId);
                                                if (npc != null) {
                                                    oStage.getItemsToDeliver().add(is);
                                                    oStage.getItemDeliveryTargets().add(npcId);
                                                    oStage.deliverMessages.addAll(deliveryMessages);
                                                } else {
                                                    stageFailed("" + npcId + " inside npc-delivery-ids: inside Stage " 
                                                            + s2 + " of Quest " + quest.getName() 
                                                            + " is not a valid NPC id!");
                                                }
                                            } else {
                                                stageFailed("Citizens was not installed for ID " + npcId 
                                                        + " inside npc-delivery-ids: inside Stage " + s2 
                                                        + " of Quest " + quest.getName());
                                            }
                                        } else {
                                            stageFailed("" + item + " inside items-to-deliver: inside Stage " + s2 
                                                    + " of Quest " + quest.getName() + " is not formatted properly!");
                                        }
                                    }
                                } else {
                                    stageFailed("items-to-deliver: in Stage " + s2 + " of Quest " + quest.getName() 
                                            + " is not formatted properly!");
                                }
                            }
                        }
                    } else {
                        stageFailed("npc-delivery-ids: in Stage " + s2 + " of Quest " + ChatColor.DARK_PURPLE 
                                + quest.getName() + " is not a list of NPC ids!");
                    }
                } else {
                    stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing npc-delivery-ids:");
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".npc-ids-to-kill")) {
                if (checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".npc-ids-to-kill"), 
                        Integer.class)) {
                    if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".npc-kill-amounts")) {
                        if (checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 
                                + ".npc-kill-amounts"), Integer.class)) {
                            npcIdsToKill = config.getIntegerList("quests." + questKey + ".stages.ordered." + s2 
                                    + ".npc-ids-to-kill");
                            npcAmountsToKill = config.getIntegerList("quests." + questKey + ".stages.ordered." + s2 
                                    + ".npc-kill-amounts");
                            for (int i : npcIdsToKill) {
                                if (CitizensAPI.getNPCRegistry().getById(i) != null) {
                                    if (npcAmountsToKill.get(npcIdsToKill.indexOf(i)) > 0) {
                                        oStage.citizensToKill.add(i);
                                        oStage.citizenNumToKill.add(npcAmountsToKill.get(npcIdsToKill.indexOf(i)));
                                        questNpcs.add(CitizensAPI.getNPCRegistry().getById(i));
                                    } else {
                                        stageFailed("" + npcAmountsToKill.get(npcIdsToKill.indexOf(i)) 
                                                + " inside npc-kill-amounts: inside Stage " + s2 + " of Quest " 
                                                + quest.getName() + " is not a positive number!");
                                    }
                                } else {
                                    stageFailed("" + i + " inside npc-ids-to-kill: inside Stage " + s2 + " of Quest " 
                                            + quest.getName() + " is not a valid NPC id!");
                                }
                            }
                        } else {
                            stageFailed("npc-kill-amounts: in Stage " + s2 + " of Quest " + quest.getName() 
                                    + " is not a list of numbers!");
                        }
                    } else {
                        stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing npc-kill-amounts:");
                    }
                } else {
                    stageFailed("npc-ids-to-kill: in Stage " + s2 + " of Quest " + quest.getName()
                            + " is not a list of numbers!");
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".mobs-to-kill")) {
                if (Quests.checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".mobs-to-kill"), 
                        String.class)) {
                    List<String> mobNames = config.getStringList("quests." + questKey + ".stages.ordered." + s2 
                            + ".mobs-to-kill");
                    for (String mob : mobNames) {
                        EntityType type = MiscUtil.getProperMobType(mob);
                        if (type != null) {
                            mobsToKill.add(type);
                        } else {
                            stageFailed("" + mob + " inside mobs-to-kill: inside Stage " + s2 + " of Quest " 
                                    + quest.getName() + " is not a valid mob name!");
                        }
                    }
                } else {
                    stageFailed("mobs-to-kill: in Stage " + s2 + " of Quest " + quest.getName() 
                            + " is not a list of mob names!");
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".mob-amounts")) {
                    if (Quests.checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 
                            + ".mob-amounts"), Integer.class)) {
                        for (int i : config.getIntegerList("quests." + questKey + ".stages.ordered." + s2 
                                + ".mob-amounts")) {
                            mobNumToKill.add(i);
                        }
                    } else {
                        stageFailed("mob-amounts: in Stage " + s2 + " of Quest " + quest.getName() 
                                + " is not a list of numbers!");
                    }
                } else {
                    stageFailed("Stage " + s2 + " of Quest " + ChatColor.DARK_PURPLE + quest.getName() 
                            + " is missing mob-amounts:");
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".locations-to-kill")) {
                if (Quests.checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 
                        + ".locations-to-kill"), String.class)) {
                    List<String> locations = config.getStringList("quests." + questKey + ".stages.ordered." + s2 
                            + ".locations-to-kill");
                    for (String loc : locations) {
                        if (getLocation(loc) != null) {
                            locationsToKillWithin.add(getLocation(loc));
                        } else {
                            stageFailed(new String[] { "" + loc + " inside locations-to-kill: inside Stage " + s2 
                                    + " of Quest " + quest.getName() + " is not in proper location format!", 
                                    "Proper location format is: \"WorldName x y z\"" });
                        }
                    }
                } else {
                    stageFailed("locations-to-kill: in Stage " + s2 + " of Quest " + quest.getName() 
                            + " is not a list of locations!");
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".kill-location-radii")) {
                    if (Quests.checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 
                            + ".kill-location-radii"), Integer.class)) {
                        List<Integer> radii = config.getIntegerList("quests." + questKey + ".stages.ordered." + s2 
                                + ".kill-location-radii");
                        for (int i : radii) {
                            radiiToKillWithin.add(i);
                        }
                    } else {
                        stageFailed("kill-location-radii: in Stage " + s2 + " of Quest " + quest.getName() 
                                + " is not a list of numbers!");
                    }
                } else {
                    stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing kill-location-radii:");
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".kill-location-names")) {
                    if (Quests.checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 
                            + ".kill-location-names"), String.class)) {
                        List<String> locationNames = config.getStringList("quests." + questKey + ".stages.ordered." 
                            + s2 + ".kill-location-names");
                        for (String name : locationNames) {
                            areaNames.add(name);
                        }
                    } else {
                        stageFailed("kill-location-names: in Stage " + s2 + " of Quest " + quest.getName() 
                                + " is not a list of names!");
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
                if (Quests.checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 
                        + ".locations-to-reach"), String.class)) {
                    List<String> locations = config.getStringList("quests." + questKey + ".stages.ordered." + s2 
                            + ".locations-to-reach");
                    for (String loc : locations) {
                        if (getLocation(loc) != null) {
                            oStage.locationsToReach.add(getLocation(loc));
                        } else {
                            stageFailed(new String[] { "" + loc + " inside locations-to-reach inside Stage " + s2 
                                    + " of Quest " + quest.getName() + " is not in proper location format!", 
                                    "Proper location format is: \"WorldName x y z\"" });
                        }
                    }
                } else {
                    stageFailed("locations-to-reach: in Stage " + s2 + " of Quest " + quest.getName() 
                            + " is not a list of locations!");
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".reach-location-radii")) {
                    if (Quests.checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 
                            + ".reach-location-radii"), Integer.class)) {
                        List<Integer> radii = config.getIntegerList("quests." + questKey + ".stages.ordered." + s2 
                                + ".reach-location-radii");
                        for (int i : radii) {
                            oStage.radiiToReachWithin.add(i);
                        }
                    } else {
                        stageFailed("reach-location-radii: in Stage " + s2 + " of Quest " + quest.getName() 
                                + " is not a list of numbers!");
                    }
                } else {
                    stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing reach-location-radii:");
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".reach-location-names")) {
                    if (Quests.checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 
                            + ".reach-location-names"), String.class)) {
                        List<String> locationNames = config.getStringList("quests." + questKey + ".stages.ordered." 
                            + s2 + ".reach-location-names");
                        for (String name : locationNames) {
                            oStage.locationNames.add(name);
                        }
                    } else {
                        stageFailed("reach-location-names: in Stage " + s2 + " of Quest " + quest.getName() 
                                + " is not a list of names!");
                    }
                } else {
                    stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing reach-location-names:");
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".mobs-to-tame")) {
                if (Quests.checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 + ".mobs-to-tame"), 
                        String.class)) {
                    if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".mob-tame-amounts")) {
                        if (Quests.checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 
                                + ".mob-tame-amounts"), Integer.class)) {
                            List<String> mobs = config.getStringList("quests." + questKey + ".stages.ordered." + s2 
                                    + ".mobs-to-tame");
                            List<Integer> mobAmounts = config.getIntegerList("quests." + questKey + ".stages.ordered." 
                                    + s2 + ".mob-tame-amounts");
                            for (String mob : mobs) {
                                if (Tameable.class.isAssignableFrom(EntityType.valueOf(mob.toUpperCase())
                                        .getEntityClass())) {
                                    oStage.mobsToTame.put(EntityType.valueOf(mob.toUpperCase()), 
                                            mobAmounts.get(mobs.indexOf(mob)));
                                } else {
                                    stageFailed("" + mob + " inside mobs-to-tame: inside Stage " + s2 + " of Quest " 
                                            + quest.getName() + " is not a valid tameable mob!");
                                }
                            }
                        } else {
                            stageFailed("mob-tame-amounts: in Stage " + s2 + " of Quest " + quest.getName() 
                                    + " is not a list of numbers!");
                        }
                    } else {
                        stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing mob-tame-amounts:");
                    }
                } else {
                    stageFailed("mobs-to-tame: in Stage " + s2 + " of Quest " + quest.getName() 
                            + " is not a list of mob names!");
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".sheep-to-shear")) {
                if (Quests.checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 
                        + ".sheep-to-shear"), String.class)) {
                    if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".sheep-amounts")) {
                        if (Quests.checkList(config.getList("quests." + questKey + ".stages.ordered." + s2 
                                + ".sheep-amounts"), Integer.class)) {
                            List<String> sheep = config.getStringList("quests." + questKey + ".stages.ordered." + s2 
                                    + ".sheep-to-shear");
                            List<Integer> shearAmounts = config.getIntegerList("quests." + questKey + ".stages.ordered." 
                                    + s2 + ".sheep-amounts");
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
                                    oStage.sheepToShear.put(DyeColor.LIGHT_BLUE, 
                                            shearAmounts.get(sheep.indexOf(color)));
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
                                    oStage.sheepToShear.put(DyeColor.getByColor(Color.SILVER), 
                                            shearAmounts.get(sheep.indexOf(color)));
                                } else if (color.equalsIgnoreCase(Lang.get("COLOR_WHITE"))) {
                                    oStage.sheepToShear.put(DyeColor.WHITE, shearAmounts.get(sheep.indexOf(color)));
                                } else if (color.equalsIgnoreCase(Lang.get("COLOR_YELLOW"))) {
                                    oStage.sheepToShear.put(DyeColor.YELLOW, shearAmounts.get(sheep.indexOf(color)));
                                } else {
                                    stageFailed("" + color + " inside sheep-to-shear: inside Stage " + s2 + " of Quest " 
                                            + quest.getName() + " is not a valid color!");
                                }
                            }
                        } else {
                            stageFailed("sheep-amounts: in Stage " + s2 + " of Quest " + quest.getName() 
                                    + " is not a list of numbers!");
                        }
                    } else {
                        stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing sheep-amounts:");
                    }
                } else {
                    stageFailed("sheep-to-shear: in Stage " + s2 + " of Quest " + quest.getName() 
                            + " is not a list of colors!");
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".password-displays")) {
                List<String> displays = config.getStringList("quests." + questKey + ".stages.ordered." + s2 
                        + ".password-displays");
                if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".password-phrases")) {
                    List<String> phrases = config.getStringList("quests." + questKey + ".stages.ordered." + s2 
                            + ".password-phrases");
                    if (displays.size() == phrases.size()) {
                        for (int passIndex = 0; passIndex < displays.size(); passIndex++) {
                            oStage.passwordDisplays.add(displays.get(passIndex));
                            LinkedList<String> answers = new LinkedList<String>();
                            answers.addAll(Arrays.asList(phrases.get(passIndex).split("\\|")));
                            oStage.passwordPhrases.add(answers);
                        }
                    } else {
                        stageFailed("password-displays and password-phrases in Stage " + s2 + " of Quest " 
                                + quest.getName() + " are not the same size!");
                    }
                } else {
                    stageFailed("Stage " + s2 + " of Quest " + quest.getName() + " is missing password-phrases!");
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".objective-override")) {
                oStage.objectiveOverride = config.getString("quests." + questKey + ".stages.ordered." + s2 
                        + ".objective-override");
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".start-event")) {
                Action evt = Action.loadAction(config.getString("quests." + questKey + ".stages.ordered." + s2 
                        + ".start-event"), this);
                if (evt != null) {
                    oStage.startEvent = evt;
                } else {
                    stageFailed("start-event: in Stage " + s2 + " of Quest " + quest.getName() + " failed to load.");
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".finish-event")) {
                Action evt = Action.loadAction(config.getString("quests." + questKey + ".stages.ordered." + s2 
                        + ".finish-event"), this);
                if (evt != null) {
                    oStage.finishEvent = evt;
                } else {
                    stageFailed("finish-event: in Stage " + s2 + " of Quest " + quest.getName() + " failed to load.");
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".death-event")) {
                Action evt = Action.loadAction(config.getString("quests." + questKey + ".stages.ordered." + s2 
                        + ".death-event"), this);
                if (evt != null) {
                    oStage.deathEvent = evt;
                } else {
                    stageFailed("death-event: in Stage " + s2 + " of Quest " + quest.getName() + " failed to load.");
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".disconnect-event")) {
                Action evt = Action.loadAction(config.getString("quests." + questKey + ".stages.ordered." + s2 
                        + ".disconnect-event"), this);
                if (evt != null) {
                    oStage.disconnectEvent = evt;
                } else {
                    stageFailed("disconnect-event: in Stage " + s2 + " of Quest " + quest.getName() 
                            + " failed to load.");
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".chat-events")) {
                if (config.isList("quests." + questKey + ".stages.ordered." + s2 + ".chat-events")) {
                    if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".chat-event-triggers")) {
                        if (config.isList("quests." + questKey + ".stages.ordered." + s2 + ".chat-event-triggers")) {
                            List<String> chatEvents = config.getStringList("quests." + questKey + ".stages.ordered." 
                                    + s2 + ".chat-events");
                            List<String> chatEventTriggers = config.getStringList("quests." + questKey 
                                    + ".stages.ordered." + s2 + ".chat-event-triggers");
                            boolean loadEventFailed = false;
                            for (int i = 0; i < chatEvents.size(); i++) {
                                Action evt = Action.loadAction(chatEvents.get(i), this);
                                if (evt != null) {
                                    oStage.chatEvents.put(chatEventTriggers.get(i), evt);
                                } else {
                                    loadEventFailed = true;
                                    stageFailed("" + chatEvents.get(i) + " inside of chat-events: in Stage " + s2 
                                            + " of Quest " + quest.getName() + " failed to load.");
                                }
                            }
                            if (loadEventFailed) {
                                break;
                            }
                        } else {
                            stageFailed("chat-event-triggers in Stage " + s2 + " of Quest " + quest.getName() 
                                    + " is not in list format!");
                        }
                    } else {
                        stageFailed("Stage " + s2 + " of Quest " + quest.getName() 
                                + " is missing chat-event-triggers!");
                    }
                } else {
                    stageFailed("chat-events in Stage " + s2 + " of Quest " + quest.getName() 
                            + " is not in list format!");
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".command-events")) {
                if (config.isList("quests." + questKey + ".stages.ordered." + s2 + ".command-events")) {
                    if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".command-event-triggers")) {
                        if (config.isList("quests." + questKey + ".stages.ordered." + s2 + ".command-event-triggers")) {
                            List<String> commandEvents = config.getStringList("quests." + questKey + ".stages.ordered." 
                                    + s2 + ".command-events");
                            List<String> commandEventTriggers = config.getStringList("quests." + questKey 
                                    + ".stages.ordered." + s2 + ".command-event-triggers");
                            boolean loadEventFailed = false;
                            for (int i = 0; i < commandEvents.size(); i++) {
                                Action evt = Action.loadAction(commandEvents.get(i), this);
                                if (evt != null) {
                                    oStage.commandEvents.put(commandEventTriggers.get(i), evt);
                                } else {
                                    loadEventFailed = true;
                                    stageFailed("" + commandEvents.get(i) + " inside of command-events: in Stage " + s2
                                            + " of Quest " + quest.getName() + " failed to load.");
                                }
                            }
                            if (loadEventFailed) {
                                break;
                            }
                        } else {
                            stageFailed("command-event-triggers in Stage " + s2 + " of Quest " + quest.getName() 
                                    + " is not in list format!");
                        }
                    } else {
                        stageFailed("Stage " + s2 + " of Quest " + quest.getName() 
                                + " is missing command-event-triggers!");
                    }
                } else {
                    stageFailed("command-events in Stage " + s2 + " of Quest " + quest.getName() 
                            + " is not in list format!");
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
                oStage.delayMessage = config.getString("quests." + questKey + ".stages.ordered." + s2 
                        + ".delay-message");
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".start-message")) {
                oStage.startMessage = config.getString("quests." + questKey + ".stages.ordered." + s2 
                        + ".start-message");
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".complete-message")) {
                oStage.completeMessage = config.getString("quests." + questKey + ".stages.ordered." + s2 
                        + ".complete-message");
            }
            LinkedList<Integer> ids = new LinkedList<Integer>();
            if (npcIdsToTalkTo != null) {
                ids.addAll(npcIdsToTalkTo);
            }
            oStage.citizensToInteract = ids;
            quest.getStages().add(oStage);
        }
    }
    
    private void loadCustomSections(Quest quest, FileConfiguration config, String questKey)
            throws StageFailedException, SkipQuest {
        ConfigurationSection questStages = config.getConfigurationSection("quests." + questKey + ".stages.ordered");
        for (String s2 : questStages.getKeys(false)) {
            if (quest == null) {
                getLogger().severe("Unable to load custom objectives because quest for " + questKey + " was null");
                return;
            }
            if (quest.getStage(Integer.valueOf(s2) - 1) == null) {
                getLogger().severe("Unable to load custom objectives because stage" + (Integer.valueOf(s2) - 1) 
                        + " for " + quest.getName() + " was null");
                return;
            }
            Stage oStage = quest.getStage(Integer.valueOf(s2) - 1);
            oStage.customObjectives = new LinkedList<>();
            oStage.customObjectiveCounts = new LinkedList<>();
            oStage.customObjectiveData = new LinkedList<>();
            oStage.customObjectiveDisplays = new LinkedList<>();
            if (config.contains("quests." + questKey + ".stages.ordered." + s2 + ".custom-objectives")) {
                ConfigurationSection sec = config.getConfigurationSection("quests." + questKey + ".stages.ordered." 
                        + s2 + ".custom-objectives");
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
                        getLogger().warning("Custom objective \"" + name + "\" for Stage " + s2 + " of Quest \"" 
                                + quest.getName() + "\" could not be found!");
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
                    getLogger().warning("Custom reward \"" + name + "\" for Quest \"" + quest.getName() 
                            + "\" could not be found!");
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
            ConfigurationSection sec = config.getConfigurationSection("quests." + questKey 
                    + ".requirements.custom-requirements");
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
                    getLogger().warning("Custom requirement \"" + name + "\" for Quest \"" + quest.getName() 
                            + "\" could not be found!");
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
     * Add possibilty to use fallbacks for customs maps<p>
     * 
     * Avoid null objects in datamap by initialize the entry value with empty string if no fallback present.
     */
    private static Map<String, Object> populateCustoms(ConfigurationSection sec2, Map<String, Object> datamap) {
        Map<String,Object> data = new HashMap<String,Object>();
        if (sec2 != null) {
            for (String key : datamap.keySet()) {
                data.put(key, sec2.contains(key) ? sec2.get(key) : datamap.get(key) != null 
                        ? datamap.get(key) : new String());
            }
        }
        return data;
    }
    
    /**
     * Add possibilty to use fallbacks for customs entries<p>
     * 
     * Avoid null objects in datamap by initialize the entry value with empty string if no fallback present.
     */
    private static Entry<String, Object> populateCustoms(ConfigurationSection sec2, Entry<String, Object> datamap) {
        String key = null;;
        Object value = null;;
        if (sec2 != null) {
            key = datamap.getKey();
            value = datamap.getValue();
        }
        return new AbstractMap.SimpleEntry<String, Object>(key, sec2.contains(key) ? sec2.get(key) : value != null 
                ? value : new String());
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
    
    /**
     * Load actions from file
     * 
     * @deprecated Use loadActions()
     */
    public void loadEvents() {
        loadActions();
    }
    
    /**
     * Load actions from file
     */
    public void loadActions() {
        YamlConfiguration config = new YamlConfiguration();
        File legacyFile = new File(this.getDataFolder(), "events.yml");
        File actionsFile = new File(this.getDataFolder(), "actions.yml");
        // Using isFile() because exists() and renameTo() can return false positives
        if (legacyFile.isFile()) {
            getLogger().log(Level.INFO, "Renaming legacy events.yml to actions.yml ...");
            try {
                legacyFile.renameTo(actionsFile);
                if (actionsFile.isFile()) {
                    getLogger().log(Level.INFO, "Success! Deleting legacy events.yml ...");
                    legacyFile.delete();
                    getLogger().log(Level.INFO, "Done!");
                }
            } catch (Exception e) {
                getLogger().log(Level.WARNING, "Unable to rename events.yml to actions.yml");
                e.printStackTrace();
            }
        }
        if (actionsFile.length() != 0) {
            try {
                if (actionsFile.isFile()) {
                    config.load(actionsFile);
                } else {
                    config.load(legacyFile);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidConfigurationException e) {
                e.printStackTrace();
            }
            ConfigurationSection sec = config.getConfigurationSection("actions");
            if (sec == null) {
                getLogger().log(Level.INFO,
                        "Could not find section \"actions\" from actions.yml. Trying legacy \"events\"...");
                sec = config.getConfigurationSection("events");
            }
            if (sec != null) {
                for (String s : sec.getKeys(false)) {
                    Action event = Action.loadAction(s, this);
                    if (event != null) {
                        events.add(event);
                    } else {
                        getLogger().log(Level.SEVERE, "Failed to load Action \"" + s + "\". Skipping.");
                    }
                }
            } else {
                getLogger().log(Level.SEVERE, "Could not find beginning section from actions.yml. Skipping.");
            }
        } else {
            getLogger().log(Level.WARNING, "Empty file actions.yml was not loaded.");
        }
    }
    
    public String[] parseStringWithPossibleLineBreaks(String s, Quest quest, Player player) {
        String parsed = parseString(s);
        if (parsed.contains("<npc>")) {
            if (quest.npcStart != null) {
                parsed = parsed.replace("<npc>", quest.npcStart.getName());
            } else {
                Bukkit.getLogger().warning(quest.getName() + " quest uses <npc> tag but doesn't have an NPC start set");
            }
        }
        if (depends.getPlaceholderApi() != null && player != null) {
            parsed = PlaceholderAPI.setPlaceholders(player, parsed);
        }
        return parsed.split("\n");
    }
    
    public static String[] parseStringWithPossibleLineBreaks(String s, Quest quest) {
        String parsed = parseString(s);
        if (parsed.contains("<npc>")) {
            if (quest.npcStart != null) {
                parsed = parsed.replace("<npc>", quest.npcStart.getName());
            } else {
                Bukkit.getLogger().warning(quest.getName() + " quest uses <npc> tag but doesn't have an NPC start set");
            }
        }
        return parsed.split("\n");
    }

    public static String[] parseStringWithPossibleLineBreaks(String s, NPC npc) {
        String parsed = parseString(s);
        if (parsed.contains("<npc>")) {
            parsed = parsed.replace("<npc>", npc.getName());
        }
        return parsed.split("\n");
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
    
    /**
     * Gets living EntityType from name
     * 
     * @deprecated Use MiscUtil.getProperMobType(EntityType)
     * @param mob Name to get type from
     * @return EntityType or null if invalid
     */
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
        if (message.length() > 0) {
            message = message.substring(1, message.length() - 1);
        }
        return message;
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
     * Checks if player can use Quests
     * 
     * @param uuid the entity UUID to be checked
     * @return {@code true} if entity is a Player that has permission
     */
    public boolean canUseQuests(UUID uuid) {
        return !checkQuester(uuid);
    }
    
    /**
     * Checks if player CANNOT use Quests
     * 
     * @param uuid the entity UUID to be checked
     * @return {@code true} if entity has no permission or is not a player
     * @deprecated Use #canUseQuests
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

    /**
     * Checks whether items in a list are instances of a class<p>
     * 
     * Does NOT check whether list objects are null
     * 
     * @param list The list to check objects of
     * @param clazz The class to compare against
     * @return false if list is null or list object does not match
     */
    public static boolean checkList(List<?> list, Class<?> clazz) {
        if (list == null) {
            return false;
        }
        for (Object o : list) {
            if (clazz.isAssignableFrom(o.getClass()) == false) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Get a Quest by name
     * 
     * @param name Name of the quest
     * @return Quest or null if not found
     */
    public Quest getQuest(String name) {
        if (name == null) {
            return null;
        }
        LinkedList<Quest> qs = quests;
        for (Quest q : qs) {
            if (q.getName().equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', name))) {
                return q;
            }
        }
        for (Quest q : qs) {
            if (q.getName().toLowerCase().startsWith(ChatColor.translateAlternateColorCodes('&', name).toLowerCase())) {
                return q;
            }
        }
        for (Quest q : qs) {
            if (q.getName().toLowerCase().contains(ChatColor.translateAlternateColorCodes('&', name).toLowerCase())) {
                return q;
            }
        }
        return null;
    }
    
    /**
     * Get an Action by name
     * 
     * @param name Name of the action
     * @return Action or null if not found
     */
    public Action getAction(String name) {
        if (name == null) {
            return null;
        }
        LinkedList<Action> as = events;
        for (Action a : as) {
            if (a.getName().equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', name))) {
                return a;
            }
        }
        for (Action a : as) {
            if (a.getName().toLowerCase().startsWith(ChatColor.translateAlternateColorCodes('&', name).toLowerCase())) {
                return a;
            }
        }
        for (Action a : as) {
            if (a.getName().toLowerCase().contains(ChatColor.translateAlternateColorCodes('&', name).toLowerCase())) {
                return a;
            }
        }
        return null;
    }

    /**
     * Get an Action by name
     * 
     * @param name Name of the action
     * @return Action or null if not found
     * @deprecated Use getAction()
     */
    public Action getEvent(String name) {
        return getAction(name);
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
    
    /**
     * Checks whether an NPC has a quest that the player may accept
     * @param npc The giver NPC to check
     * @param quester The player to check
     * @return true if at least one quest is available
     */
    public boolean hasQuest(NPC npc, Quester quester) {
        for (Quest q : quests) {
            // Return false for expired quests

            // Return true if not yet completed
            if (q.npcStart != null && quester.completedQuests.contains(q.getName()) == false) {
                if (q.npcStart.getId() == npc.getId()) {
                    boolean ignoreLockedQuests = settings.canIgnoreLockedQuests();
                    if (ignoreLockedQuests == false || ignoreLockedQuests == true 
                            && q.testRequirements(quester) == true) {
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
                    if (ignoreLockedQuests == false || ignoreLockedQuests == true 
                            && q.testRequirements(quester) == true) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public boolean hasCompletedRedoableQuest(NPC npc, Quester quester) {
        for (Quest q : quests) {
            if (q.npcStart != null && quester.completedQuests.contains(q.getName()) == true 
                    && q.getPlanner().getCooldown() > -1) {
                if (q.npcStart.getId() == npc.getId()) {
                    boolean ignoreLockedQuests = settings.canIgnoreLockedQuests();
                    if (ignoreLockedQuests == false || ignoreLockedQuests == true 
                            && q.testRequirements(quester) == true) {
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

    @SuppressWarnings("deprecation")
    public boolean testSecondaryHeroesClass(String secondaryClass, UUID uuid) {
        Hero hero = getHero(uuid);
        return hero.getSecondClass().getName().equalsIgnoreCase(secondaryClass);
    }
}
