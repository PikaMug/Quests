package me.blackvein.quests;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.blackvein.quests.exceptions.InvalidStageException;
import me.blackvein.quests.prompts.QuestAcceptPrompt;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;
import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizencore.scripts.ScriptRegistry;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.CitizensPlugin;
import net.citizensnpcs.api.npc.NPC;
import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.conversations.Conversable;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.codisimus.plugins.phatloots.PhatLoots;
import com.codisimus.plugins.phatloots.PhatLootsAPI;
import com.evilmidget38.UUIDFetcher;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.util.player.UserManager;
import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.inventory.meta.ItemMeta;

public class Quests extends JavaPlugin implements ConversationAbandonedListener, ColorUtil {

    public static Economy economy = null;
    public static Permission permission = null;
    public static WorldGuardPlugin worldGuard = null;
    public static mcMMO mcmmo = null;
    public static Heroes heroes = null;
    public static PhatLoots phatLoots = null;
    public static boolean npcEffects = true;
    public static boolean useCompass = true;
    public static boolean ignoreLockedQuests = false;
    public static boolean genFilesOnJoin = true;
    public static int acceptTimeout = 20;
    public static int maxQuests = 0;
    public static String effect = "note";
    public final Map<UUID, Quester> questers = new HashMap<UUID, Quester>();
    public final List<String> questerBlacklist = new LinkedList<String>();
    public final List<CustomRequirement> customRequirements = new LinkedList<CustomRequirement>();
    public final List<CustomReward> customRewards = new LinkedList<CustomReward>();
    public final List<CustomObjective> customObjectives = new LinkedList<CustomObjective>();
    public final LinkedList<Quest> quests = new LinkedList<Quest>();
    public final LinkedList<Event> events = new LinkedList<Event>();
    public final LinkedList<NPC> questNPCs = new LinkedList<NPC>();
    public final LinkedList<Integer> questNPCGUIs = new LinkedList<Integer>();
    public ConversationFactory conversationFactory;
    public ConversationFactory NPCConversationFactory;
    public QuestFactory questFactory;
    public EventFactory eventFactory;
    public Vault vault = null;
    public CitizensPlugin citizens;
    public PlayerListener pListener;
    public NpcListener npcListener;
    public NpcEffectThread effListener;
    public Denizen denizen = null;
    public QuestTaskTrigger trigger;
    public boolean allowCommands = true;
    public boolean allowCommandsForNpcQuests = false;
    public boolean showQuestReqs = true;
    public boolean allowQuitting = true;
    public boolean debug = false;
    public boolean convertData = false;
    public boolean load = false;
    public int killDelay = 0;
    public int totalQuestPoints = 0;
    public Lang lang;
    public HashMap<String, Integer> commands = new HashMap<String, Integer>();
    public HashMap<String, Integer> adminCommands = new HashMap<String, Integer>();
    private static Quests instance = null;

    @SuppressWarnings("serial")
    class StageFailedException extends Exception {
    }

    @SuppressWarnings("serial")
    class SkipQuest extends Exception {
    }

    @Override
    public void onEnable() {

        /*if(getServer().getBukkitVersion().equalsIgnoreCase(validVersion) == false) {

         getLogger().severe("Your current version of CraftBukkit is " + getServer().getBukkitVersion() + ", this version of Quests is built for version " + validVersion);
         getLogger().severe("Disabling...");
         getServer().getPluginManager().disablePlugin(this);
         return;

         }  */
        pListener = new PlayerListener(this);
        effListener = new NpcEffectThread(this);
        npcListener = new NpcListener(this);
        instance = this;

        this.conversationFactory = new ConversationFactory(this)
                .withModality(false)
                .withPrefix(new QuestsPrefix())
                .withFirstPrompt(new QuestPrompt())
                .withTimeout(acceptTimeout)
                .thatExcludesNonPlayersWithMessage("Console may not perform this conversation!")
                .addConversationAbandonedListener(this);

        this.NPCConversationFactory = new ConversationFactory(this)
                .withModality(false)
                .withFirstPrompt(new QuestAcceptPrompt(this))
                .withTimeout(acceptTimeout)
                .withLocalEcho(false)
                .addConversationAbandonedListener(this);

        questFactory = new QuestFactory(this);
        eventFactory = new EventFactory(this);

        linkOtherPlugins();

        defaultConfigFile();

        loadConfig();
        loadModules();

        defaultLangFile();
        defaultQuestsFile();
        defaultEventsFile();
        defaultDataFile();

        loadCommands();

        getServer().getPluginManager().registerEvents(pListener, this);
        if (npcEffects) {
            getServer().getScheduler().scheduleSyncRepeatingTask(this, effListener, 20, 20);
        }

        delayLoadQuestInfo();
    }

    private void defaultLangFile() {
        lang = new Lang(this);
        lang.initPhrases();
        if (new File(this.getDataFolder(), "/lang/en.yml").exists() == false) {
        	getLogger().info("Translation data not found, writing defaults to file.");
            lang.saveNewLang();
        } else {
            lang.loadLang();
        }
    }

    private void delayLoadQuestInfo() {
        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                loadQuests();
                loadEvents();
                getLogger().log(Level.INFO, "" + quests.size() + " Quest(s) loaded.");
                getLogger().log(Level.INFO, "" + events.size() + " Event(s) loaded.");
                getLogger().log(Level.INFO, "" + Lang.getPhrases() + " Phrase(s) loaded.");
                questers.putAll(getOnlineQuesters());

                if(convertData) {

                    convertQuesters();

                    FileConfiguration config = getConfig();
                    config.set("convert-data-on-startup", false);

                    try {
                        config.save(new File(Quests.this.getDataFolder(), "config.yml"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 5L);
    }

    private void defaultDataFile() {
        if (new File(this.getDataFolder(), "data.yml").exists() == false) {
        	getLogger().info("Data file not found, writing default to file.");
            FileConfiguration data = new YamlConfiguration();
            data.options().copyHeader(true);
            data.options().copyDefaults(true);
            try {
                data.save(new File(this.getDataFolder(), "data.yml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            loadData();
        }
    }

    @SuppressWarnings("deprecation")
	private void defaultEventsFile() {
        if (new File(this.getDataFolder(), "events.yml").exists() == false) {
        	getLogger().info("Events data not found, writing defaults to file.");
            FileConfiguration data = new YamlConfiguration();
            data.options().copyHeader(true);
            data.options().copyDefaults(true);
            try {
                data.load(this.getResource("events.yml"));
                data.save(new File(this.getDataFolder(), "events.yml"));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("deprecation")
	private void defaultQuestsFile() {
        if (new File(this.getDataFolder(), "quests.yml").exists() == false) {

        	getLogger().info("Quest data not found, writing defaults to file.");
            FileConfiguration data = new YamlConfiguration();
            try {
                data.load(this.getResource("quests.yml"));
                data.set("events", null);
                data.save(new File(this.getDataFolder(), "quests.yml"));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidConfigurationException e) {
                e.printStackTrace();
            }

        }
    }

    @SuppressWarnings("deprecation")
	private void defaultConfigFile() {
        if (new File(this.getDataFolder(), "config.yml").exists() == false) {
        	getLogger().info("Config not found, writing default to file.");
            FileConfiguration config = new YamlConfiguration();
            try {
                config.load(this.getResource("config.yml"));
                config.save(new File(this.getDataFolder(), "config.yml"));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadCommands() {

        // [] - required
        // {} - optional

        commands.put(Lang.get("COMMAND_LIST"), 1); // list {page}
        commands.put(Lang.get("COMMAND_TAKE"), 2); // take [quest]
        commands.put(Lang.get("COMMAND_QUIT"), 2); // quit [quest]
        commands.put(Lang.get("COMMAND_EDITOR"), 1); // editor
        commands.put(Lang.get("COMMAND_EVENTS_EDITOR"), 1); // events
        commands.put(Lang.get("COMMAND_STATS"), 1); // stats
        commands.put(Lang.get("COMMAND_TOP"), 2); // top [number]
        commands.put(Lang.get("COMMAND_INFO"), 1); // info
        commands.put(Lang.get("COMMAND_JOURNAL"), 1); // journal

        adminCommands.put(Lang.get("COMMAND_QUESTADMIN_STATS"), 2); // stats [player]
        adminCommands.put(Lang.get("COMMAND_QUESTADMIN_GIVE"), 3); // give [player] [quest]
        adminCommands.put(Lang.get("COMMAND_QUESTADMIN_QUIT"), 3); // quit [player] [quest]
        adminCommands.put(Lang.get("COMMAND_QUESTADMIN_REMOVE"), 3); // remove [player] [quest]
        adminCommands.put(Lang.get("COMMAND_QUESTADMIN_POINTS"), 3); // points [player] [amount]
        adminCommands.put(Lang.get("COMMAND_QUESTADMIN_TAKEPOINTS"), 3); // takepoints [player] [amount]
        adminCommands.put(Lang.get("COMMAND_QUESTADMIN_GIVEPOINTS"), 3); // givepoints [player] [amount]
        adminCommands.put(Lang.get("COMMAND_QUESTADMIN_POINTSALL"), 2); // pointsall [amount]
        adminCommands.put(Lang.get("COMMAND_QUESTADMIN_FINISH"), 3); // finish [player] [quest]
        adminCommands.put(Lang.get("COMMAND_QUESTADMIN_NEXTSTAGE"), 3); // nextstage [player] [quest]
        adminCommands.put(Lang.get("COMMAND_QUESTADMIN_SETSTAGE"), 4); // setstage [player] [quest] [stage]
        adminCommands.put(Lang.get("COMMAND_QUESTADMIN_PURGE"), 2); // purge [player]
        adminCommands.put(Lang.get("COMMAND_QUESTADMIN_RESET"), 2); // purge [player]
        adminCommands.put(Lang.get("COMMAND_QUESTADMIN_TOGGLEGUI"), 2); // togglegui [npc id]
        adminCommands.put(Lang.get("COMMAND_QUESTADMIN_RELOAD"), 1); // reload

    }

    public String checkCommand(String cmd, String[] args) {

        if(cmd.equalsIgnoreCase("quest") || args.length == 0) {
            return null;
        }

        if(cmd.equalsIgnoreCase("quests")) {

            if(commands.containsKey(args[0].toLowerCase())) {

                int min = commands.get(args[0].toLowerCase());
                if(args.length < min)
                    return getQuestsCommandUsage(args[0]);
                else
                    return null;

            }

            return YELLOW + Lang.get("questsUnknownCommand");

        } else if(cmd.equalsIgnoreCase("questsadmin") || cmd.equalsIgnoreCase("questadmin")) {

            if(adminCommands.containsKey(args[0].toLowerCase())) {

                int min = adminCommands.get(args[0].toLowerCase());
                if(args.length < min)
                    return getQuestadminCommandUsage(args[0]);
                else
                    return null;

            }

            return YELLOW + Lang.get("questsUnknownAdminCommand");
        }

        return "NULL";
    }

    public String getQuestsCommandUsage(String cmd) {

        return RED + Lang.get("usage") + ":" + YELLOW + "/quests " + Lang.get(Lang.getCommandKey(cmd) + "_HELP");

    }

    public String getQuestadminCommandUsage(String cmd) {

        return RED + Lang.get("usage") + ": " + YELLOW + "/questadmin " + Lang.get(Lang.getCommandKey(cmd) + "_HELP");

    }

    private void linkOtherPlugins() {

        try {
            if (getServer().getPluginManager().getPlugin("Citizens") != null) {
                citizens = (CitizensPlugin) getServer().getPluginManager().getPlugin("Citizens");
            }
            if (citizens != null) {
                getServer().getPluginManager().registerEvents(npcListener, this);
            }
        } catch (Exception e) {
        	getLogger().warning("Legacy version of Citizens found. Citizens in Quests not enabled.");
        }

        if (getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            worldGuard = (WorldGuardPlugin) getServer().getPluginManager().getPlugin("WorldGuard");
        }

        if (getServer().getPluginManager().getPlugin("Denizen") != null) {
            denizen = (Denizen) getServer().getPluginManager().getPlugin("Denizen");
        }

        if (getServer().getPluginManager().getPlugin("mcMMO") != null) {
            mcmmo = (mcMMO) getServer().getPluginManager().getPlugin("mcMMO");
        }

        if (getServer().getPluginManager().getPlugin("Heroes") != null) {
            heroes = (Heroes) getServer().getPluginManager().getPlugin("Heroes");
        }

        if (getServer().getPluginManager().getPlugin("PhatLoots") != null) {
            phatLoots = (PhatLoots) getServer().getPluginManager().getPlugin("PhatLoots");
        }

        if (!setupEconomy()) {
        	getLogger().warning("Economy not found.");
        }

        if (!setupPermissions()) {
        	getLogger().warning("Permissions not found.");
        }

        vault = (Vault) getServer().getPluginManager().getPlugin("Vault");
    }

    @Override
    public void onDisable() {

    	getLogger().info("Saving Quester data.");
        for (Player p : getServer().getOnlinePlayers()) {
        	if (!questerBlacklist.contains(p.getUniqueId().toString())) {
        		Quester quester = getQuester(p.getUniqueId());
        		quester.saveData();
        	}
        }
        updateData();

    }

    public LinkedList<Quest> getQuests() {
        return quests;
    }

    @Override
    public void conversationAbandoned(ConversationAbandonedEvent abandonedEvent) {

        if (abandonedEvent.gracefulExit() == false) {

            if (abandonedEvent.getContext().getForWhom() != null) {

                try {
                    abandonedEvent.getContext().getForWhom().sendRawMessage(YELLOW + Lang.get("questTimeout"));
                } catch (Exception e) {
                }

            }

        }

    }

    public static Quests getInstance() {
        return instance;
    }

    private class QuestPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + Lang.get("acceptQuest") + "  " + GREEN + Lang.get("yesWord") + " / " + Lang.get("noWord");

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String s) {

            Player player = (Player) context.getForWhom();

            if (s.equalsIgnoreCase(Lang.get("yesWord"))) {

                try{
                    getQuester(player.getUniqueId()).takeQuest(getQuest(getQuester(player.getUniqueId()).questToTake), false);
                }catch(Exception e) {
                    e.printStackTrace();
                }
                return Prompt.END_OF_CONVERSATION;

            } else if (s.equalsIgnoreCase(Lang.get("noWord"))) {

                player.sendMessage(YELLOW + Lang.get("cancelled"));
                return Prompt.END_OF_CONVERSATION;

            } else {

                player.sendMessage(RED + Lang.get("questInvalidChoice"));
                return new QuestPrompt();

            }

        }
    }

    private class QuestsPrefix implements ConversationPrefix {

        @Override
        public String getPrefix(ConversationContext context) {

            return "" + GRAY;

        }
    }

    public void loadConfig() {
    	
        FileConfiguration config = getConfig();

        Lang.lang = config.getString("language", "en");
        allowCommands = config.getBoolean("allow-command-questing", true);
        allowCommandsForNpcQuests = config.getBoolean("allow-command-quests-with-npcs", false);
        showQuestReqs = config.getBoolean("show-requirements", true);
        allowQuitting = config.getBoolean("allow-quitting", true);
        useCompass = config.getBoolean("use-compass", true);
        genFilesOnJoin = config.getBoolean("generate-files-on-join", true);
        npcEffects = config.getBoolean("show-npc-effects", true);
        effect = config.getString("npc-effect", "note");
        debug = config.getBoolean("debug-mode", false);
        killDelay = config.getInt("kill-delay", 600);
        acceptTimeout = config.getInt("accept-timeout", 20);
        convertData = config.getBoolean("convert-data-on-startup", false);

        if (config.contains("language")) {
            Lang.lang = config.getString("language");
        } else {
            config.set("language", "en");
        }

        if (config.contains("ignore-locked-quests")) {
            ignoreLockedQuests = config.getBoolean("ignore-locked-quests");
        } else {
            config.set("ignore-locked-quests", false);
        }

        if (config.contains("max-quests")) {
            maxQuests = config.getInt("max-quests");
        } else {
            config.set("max-quests", maxQuests);
        }

        for (String s : config.getStringList("quester-blacklist")) {
        	if (!s.equals("UUID")) {
        		questerBlacklist.add(s);
        	}
        }

        try {
            config.save(new File(this.getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

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
            questNPCGUIs.clear();
            questNPCGUIs.addAll(ids);

        }

    }

    public void loadModules() {

        File f = new File(this.getDataFolder(), "modules");
        if (f.exists() && f.isDirectory()) {

            File[] modules = f.listFiles();
            for (File module : modules) {

                if (module.isDirectory() == false && module.getName().endsWith(".jar")) {
                    loadModule(module);
                }

            }

        } else {

            f.mkdir();

        }

    }

    public void loadModule(File jar) {

        try {

            JarFile jarFile = new JarFile(jar);
            Enumeration<JarEntry> e = jarFile.entries();

            URL[] urls = {new URL("jar:file:" + jar.getPath() + "!/")};

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
                    customRequirements.add(requirement);
                    String name = requirement.getName() == null ? "[" + jar.getName() + "]" : requirement.getName();
                    String author = requirement.getAuthor() == null ? "[Unknown]" : requirement.getAuthor();
                    count++;
                    getLogger().info("Loaded Module: " + name + " by " + author);

                } else if (CustomReward.class.isAssignableFrom(c)) {

                    Class<? extends CustomReward> rewardClass = c.asSubclass(CustomReward.class);
                    Constructor<? extends CustomReward> cstrctr = rewardClass.getConstructor();
                    CustomReward reward = cstrctr.newInstance();
                    customRewards.add(reward);
                    String name = reward.getName() == null ? "[" + jar.getName() + "]" : reward.getName();
                    String author = reward.getAuthor() == null ? "[Unknown]" : reward.getAuthor();
                    count++;
                    getLogger().info("Loaded Module: " + name + " by " + author);

                } else if (CustomObjective.class.isAssignableFrom(c)) {

                    Class<? extends CustomObjective> objectiveClass = c.asSubclass(CustomObjective.class);
                    Constructor<? extends CustomObjective> cstrctr = objectiveClass.getConstructor();
                    CustomObjective objective = cstrctr.newInstance();
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
                        if (debug) {
                            getLogger().warning("Error log:");
                            ex.printStackTrace();
                        }
                    }

                }
            }

            if(count == 0) {
                getLogger().severe("Error: Unable to load module from file: " + jar.getName() + ", jar file is not a valid module!");
            }

        } catch (Exception e) {
        	getLogger().severe("Error: Unable to load module from file: " + jar.getName());
            if (debug) {
            	getLogger().severe("Error log:");
                e.printStackTrace();
            }
        }

    }

    public void printHelp(Player player) {

        player.sendMessage(GOLD + Lang.get("questHelpTitle"));
        player.sendMessage(YELLOW + "/quests " + Lang.get("questDisplayHelp"));
        if (player.hasPermission("quests.list")) {
            player.sendMessage(YELLOW + "/quests " + Lang.get("COMMAND_LIST_HELP"));
        }
        if (player.hasPermission("quests.take")) {
            player.sendMessage(YELLOW + "/quests " + Lang.get("COMMAND_TAKE_HELP"));
        }
        if (player.hasPermission("quests.quit")) {
            player.sendMessage(YELLOW + "/quests " + Lang.get("COMMAND_QUIT_HELP"));
        }
        if (player.hasPermission("quests.journal")) {
            player.sendMessage(YELLOW + "/quests " + Lang.get("COMMAND_JOURNAL_HELP"));
        }
        if (player.hasPermission("quests.editor.*") || player.hasPermission("quests.editor.editor")) {
            player.sendMessage(YELLOW + "/quests " + Lang.get("COMMAND_EDITOR_HELP"));
        }
        if (player.hasPermission("quests.editor.*") || player.hasPermission("quests.editor.events.editor")) {
            player.sendMessage(YELLOW + "/quests " + Lang.get("COMMAND_EVENTS_EDITOR_HELP"));
        }
        if (player.hasPermission("quests.stats")) {
            player.sendMessage(YELLOW + "/quests " + Lang.get("COMMAND_STATS_HELP"));
        }
        if (player.hasPermission("quests.top")) {
            player.sendMessage(YELLOW + "/quests " + Lang.get("COMMAND_TOP_HELP"));
        }
        //player.sendMessage(GOLD + "/quests party - Quest Party commands");
        player.sendMessage(YELLOW + "/quests " + Lang.get("COMMAND_INFO_HELP"));
        player.sendMessage(" ");
        player.sendMessage(YELLOW + "/quest " + Lang.get("COMMAND_QUEST_HELP"));
        if (player.hasPermission("quests.questinfo")) {
            player.sendMessage(YELLOW + "/quest " + Lang.get("COMMAND_QUESTINFO_HELP"));
        }

        if (player.hasPermission("quests.admin.*") || player.hasPermission("quests.admin")) {
            player.sendMessage(DARKRED + "/questadmin " + RED + Lang.get("COMMAND_QUESTADMIN_HELP"));
        }

    }

    public void printPartyHelp(Player player) {

        player.sendMessage(PURPLE + "- Quest Parties -");
        player.sendMessage(PINK + "/quests party create - Create new party");
        player.sendMessage(PINK + "/quests party leave - Leave your party");
        player.sendMessage(PINK + "/quests party info - Info about your party");
        player.sendMessage(PURPLE + "- (Leader only) -");
        player.sendMessage(PINK + "/quests party invite <player> - Invite a player to your party");
        player.sendMessage(PINK + "/quests party kick <player> - Kick a member from the party");
        player.sendMessage(PINK + "/quests party setleader <player> - Set a party member as the new leader");

    }

    @Override
    public boolean onCommand(final CommandSender cs, Command cmd, String label, String[] args) {

    	if (cs instanceof Player) {
    		if (checkQuester(((Player)cs).getUniqueId()) == true) {
    			cs.sendMessage(RED + Lang.get("questBlacklisted"));
    			return true;
    		}
    	}

        String error = checkCommand(cmd.getName(), args);

        if(error != null) {
            cs.sendMessage(error);
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("quest")) {

            return questCommandHandler(cs, args);

        } else if (cmd.getName().equalsIgnoreCase("quests")) {

            return questActionsCommandHandler(cs, args);

        } else if (cmd.getName().equalsIgnoreCase("questadmin")) {

            return questAdminCommandHandler(cs, args);

        }

        return false;

    }

    private boolean questAdminCommandHandler(final CommandSender cs, String[] args) {

        if (args.length == 0) {

            adminHelp(cs);
            return true;

        }

        if (args[0].equalsIgnoreCase(Lang.get("COMMAND_QUESTADMIN_RELOAD"))) {

            adminReload(cs);

        } else if (args[0].equalsIgnoreCase(Lang.get("COMMAND_QUESTADMIN_POINTSALL"))) {

            adminPointsAll(cs, args);

        } else if (args[0].equalsIgnoreCase(Lang.get("COMMAND_QUESTADMIN_GIVE"))) {

            adminGive(cs, args);

        } else if (args[0].equalsIgnoreCase(Lang.get("COMMAND_QUESTADMIN_POINTS"))) {

            adminPoints(cs, args);

        } else if (args[0].equalsIgnoreCase(Lang.get("COMMAND_QUESTADMIN_TAKEPOINTS"))) {

            adminTakePoints(cs, args);

        } else if (args[0].equalsIgnoreCase(Lang.get("COMMAND_QUESTADMIN_GIVEPOINTS"))) {

            adminGivePoints(cs, args);

        } else if (args[0].equalsIgnoreCase(Lang.get("COMMAND_QUESTADMIN_TOGGLEGUI"))) {

            adminToggieGUI(cs, args);

        } else if (args[0].equalsIgnoreCase(Lang.get("COMMAND_QUESTADMIN_QUIT"))) {

            adminQuit(cs, args);

        } else if (args[0].equalsIgnoreCase(Lang.get("COMMAND_QUESTADMIN_NEXTSTAGE"))) {

            adminNextStage(cs, args);

        } else if (args[0].equalsIgnoreCase(Lang.get("COMMAND_QUESTADMIN_SETSTAGE"))) {

            adminSetStage(cs, args);

        } else if (args[0].equalsIgnoreCase(Lang.get("COMMAND_QUESTADMIN_FINISH"))) {

            adminFinish(cs, args);

        } else if (args[0].equalsIgnoreCase(Lang.get("COMMAND_QUESTADMIN_PURGE"))) {

            adminPurge(cs, args);

        } else if (args[0].equalsIgnoreCase(Lang.get("COMMAND_QUESTADMIN_RESET"))) {

            adminReset(cs, args);

        } else if (args[0].equalsIgnoreCase(Lang.get("COMMAND_QUESTADMIN_STATS"))) {

            adminStats(cs, args);

        } else if (args[0].equalsIgnoreCase(Lang.get("COMMAND_QUESTADMIN_REMOVE"))) {

            adminRemove(cs, args);

        } else {

            cs.sendMessage(YELLOW + Lang.get("questsUnknownAdminCommand"));

        }

        return true;
    }

    private void adminHelp(final CommandSender cs) {
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin")) {
            printAdminHelp(cs);
        } else {
            cs.sendMessage(RED + Lang.get("questCmdNoPerms"));
        }
    }

    private void adminReload(final CommandSender cs) {
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.reload")) {
            reloadQuests();
            cs.sendMessage(GOLD + Lang.get("questsReloaded"));
            String msg = Lang.get("numQuestsLoaded");
            msg = msg.replaceAll("<number>", PURPLE + String.valueOf(quests.size()) + GOLD);
            cs.sendMessage(GOLD + msg);
        } else {
            cs.sendMessage(RED + Lang.get("questCmdNoPerms"));
        }
    }

    private void adminToggieGUI(final CommandSender cs, String[] args) {
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.togglegui")) {

            try {

                int i = Integer.parseInt(args[1]);
                if (citizens.getNPCRegistry().getById(i) == null) {
                    String msg = Lang.get("errorNPCID");
                    msg = msg.replaceAll("errorNPCID", PURPLE + "" + i + RED);
                    cs.sendMessage(RED + msg);
                } else if (questNPCGUIs.contains(i)) {
                    questNPCGUIs.remove(questNPCGUIs.indexOf(i));
                    updateData();
                    String msg = Lang.get("disableNPCGUI");
                    msg = msg.replaceAll("<npc>", PURPLE + citizens.getNPCRegistry().getById(i).getName() + YELLOW);
                    cs.sendMessage(YELLOW + msg);
                } else {
                    questNPCGUIs.add(i);
                    updateData();
                    String msg = Lang.get("enableNPCGUI");
                    msg = msg.replaceAll("<npc>", PURPLE + citizens.getNPCRegistry().getById(i).getName() + YELLOW);
                    cs.sendMessage(YELLOW + msg);
                }

            } catch (NumberFormatException nfe) {
                cs.sendMessage(RED + Lang.get("inputNum"));
            } catch (Exception ex) {
                ex.printStackTrace();
                cs.sendMessage(RED + Lang.get("unknownError"));
            }

        } else {

            cs.sendMessage(RED + Lang.get("questCmdNoPerms"));

        }
    }

    private void adminGivePoints(final CommandSender cs, String[] args) {
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.givepoints")) {

            Player target = null;

            for (Player p : getServer().getOnlinePlayers()) {

                if (p.getName().equalsIgnoreCase(args[1])) {
                    target = p;
                    break;
                }

            }

            if (target == null) {

                cs.sendMessage(YELLOW + Lang.get("playerNotFound"));

            } else {

                int points;

                try {

                    points = Integer.parseInt(args[2]);

                    Quester quester = getQuester(target.getUniqueId());
                    quester.questPoints += Math.abs(points);

                    String msg1 = Lang.get("giveQuestPoints");
                    msg1 = msg1.replaceAll("<player>", GREEN + target.getName() + GOLD);
                    msg1 = msg1.replaceAll("<number>", PURPLE + "" + points + GOLD);
                    cs.sendMessage(GOLD + msg1);
                    String msg2 = Lang.get("questPointsGiven");
                    msg2 = msg2.replaceAll("<player>", GREEN + cs.getName() + GOLD);
                    msg2 = msg2.replaceAll("<number>", PURPLE + "" + points + GOLD);
                    target.sendMessage(GREEN + msg2);

                    quester.saveData();

                } catch (NumberFormatException e) {

                    cs.sendMessage(YELLOW + Lang.get("inputNum"));

                }

            }

        } else {

            cs.sendMessage(RED + Lang.get("questCmdNoPerms"));

        }
    }

    private void adminTakePoints(final CommandSender cs, String[] args) {

        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.takepoints")) {

            Player target = null;

            for (Player p : getServer().getOnlinePlayers()) {

                if (p.getName().equalsIgnoreCase(args[1])) {
                    target = p;
                    break;
                }

            }

            if (target == null) {

                cs.sendMessage(YELLOW + Lang.get("playerNotFound"));

            } else {

                int points;

                try {

                    points = Integer.parseInt(args[2]);

                } catch (NumberFormatException e) {

                    cs.sendMessage(YELLOW + Lang.get("inputNum"));
                    return;

                }
                Quester quester = getQuester(target.getUniqueId());
                quester.questPoints -= Math.abs(points);

                String msg1 = Lang.get("takeQuestPoints");
                msg1 = msg1.replaceAll("<player>", GREEN + target.getName() + GOLD);
                msg1 = msg1.replaceAll("<number>", PURPLE + "" + points + GOLD);
                cs.sendMessage(GOLD + msg1);
                String msg2 = Lang.get("questPointsTaken");
                msg2 = msg2.replaceAll("<player>", GREEN + cs.getName() + GOLD);
                msg2 = msg2.replaceAll("<number>", PURPLE + "" + points + GOLD);

                target.sendMessage(GREEN + msg2);

                quester.saveData();

            }

        } else {

            cs.sendMessage(RED + Lang.get("questCmdNoPerms"));

        }
    }

    private void adminPoints(final CommandSender cs, String[] args) {
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.points")) {

            Player target = null;

            for (Player p : getServer().getOnlinePlayers()) {

                if (p.getName().equalsIgnoreCase(args[1])) {
                    target = p;
                    break;
                }

            }

            if (target == null) {

                cs.sendMessage(YELLOW + Lang.get("playerNotFound"));

            } else {

                int points;

                try {

                    points = Integer.parseInt(args[2]);

                } catch (NumberFormatException e) {

                    cs.sendMessage(YELLOW + Lang.get("inputNum"));
                    return;

                }

                Quester quester = getQuester(target.getUniqueId());
                quester.questPoints = points;

                String msg1 = Lang.get("setQuestPoints");
                msg1 = msg1.replaceAll("<player>", GREEN + target.getName() + GOLD);
                msg1 = msg1.replaceAll("<number>", PURPLE + "" + points + GOLD);
                cs.sendMessage(GOLD + msg1);
                String msg2 = Lang.get("questPointsSet");
                msg2 = msg2.replaceAll("<player>", GREEN + cs.getName() + GOLD);
                msg2 = msg2.replaceAll("<number>", PURPLE + "" + points + GOLD);
                target.sendMessage(GREEN + msg2);

                quester.saveData();

            }

        } else {

            cs.sendMessage(RED + Lang.get("questCmdNoPerms"));

        }
    }

    private void adminGive(final CommandSender cs, String[] args) {

        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.give")) {

            Player target = null;

            for (Player p : getServer().getOnlinePlayers()) {

                if (p.getName().toLowerCase().contains(args[1].toLowerCase())) {
                    target = p;
                    break;
                }

            }

            if (target == null) {

                cs.sendMessage(YELLOW + Lang.get("playerNotFound"));

            } else {

                Quest questToGive;

                String name = "";

                if (args.length == 3) {
                    name = args[2].toLowerCase();
                } else {

                    for (int i = 2; i < args.length; i++) {

                        int lastIndex = args.length - 1;

                        if (i == lastIndex) {
                        	name = name + args[i].toLowerCase();
                        } else {
                        	name = name + args[i].toLowerCase() + " ";
                        }

                    }

                }

                questToGive = findQuest(name);

                if (questToGive == null) {

                    cs.sendMessage(YELLOW + Lang.get("questNotFound"));

                } else {

                    Quester quester = getQuester(target.getUniqueId());

                    for (Quest q : quester.currentQuests.keySet()) {

                        if(q.getName().equalsIgnoreCase(questToGive.getName())) {

                            String msg = Lang.get("questsPlayerHasQuestAlready");
                            msg = msg.replaceAll("<player>", ITALIC + "" + GREEN + target.getName() + RESET + YELLOW);
                            msg = msg.replaceAll("<quest>", ITALIC + "" + PURPLE + questToGive.getName() + RESET + YELLOW);
                            cs.sendMessage(YELLOW + msg);

                            return;
                        }

                    }

                    quester.hardQuit(questToGive);

                    String msg1 = Lang.get("questForceTake");
                    msg1 = msg1.replaceAll("<player>", GREEN + target.getName() + GOLD);
                    msg1 = msg1.replaceAll("<quest>", PURPLE + questToGive.name + GOLD);
                    cs.sendMessage(GOLD + msg1);
                    String msg2 = Lang.get("questForcedTake");
                    msg2 = msg2.replaceAll("<player>", GREEN + cs.getName() + GOLD);
                    msg2 = msg2.replaceAll("<quest>", PURPLE + questToGive.name + GOLD);
                    target.sendMessage(GREEN + msg2);
                    quester.takeQuest(questToGive, true);

                }

            }

        } else {

            cs.sendMessage(RED + Lang.get("questCmdNoPerms"));

        }
    }

    private void adminPointsAll(final CommandSender cs, String[] args) {
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.points.all")) {

            final int amount;

            try {

                amount = Integer.parseInt(args[1]);

                if (amount < 0) {
                    cs.sendMessage(RED + Lang.get("inputPosNum"));
                    return;
                }

            } catch (NumberFormatException e) {
                cs.sendMessage(RED + Lang.get("inputNum"));
                return;
            }

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {

                    File questerFolder = new File(Quests.this.getDataFolder(), "data");
                    if (questerFolder.exists() && questerFolder.isDirectory()) {

                        FileConfiguration data = new YamlConfiguration();
                        int failCount = 0;
                        boolean suppressed = false;

                        for (File f : questerFolder.listFiles()) {

                            try {

                                data.load(f);
                                data.set("quest-points", amount);
                                data.save(f);

                            } catch (IOException e) {

                                if (failCount < 10) {
                                    String msg = Lang.get("errorReading");
                                    msg = msg.replaceAll("<file>", DARKAQUA + f.getName() + RED);
                                    cs.sendMessage(RED + msg);
                                    failCount++;
                                } else if (suppressed == false) {
                                    String msg = Lang.get("errorReadingSuppress");
                                    msg = msg.replaceAll("<file>", DARKAQUA + f.getName() + RED);
                                    cs.sendMessage(RED + msg);
                                    suppressed = true;
                                }

                            } catch (InvalidConfigurationException e) {

                                if (failCount < 10) {
                                    String msg = Lang.get("errorReading");
                                    msg = msg.replaceAll("<file>", DARKAQUA + f.getName() + RED);
                                    cs.sendMessage(RED + msg);
                                    failCount++;
                                } else if (suppressed == false) {
                                    String msg = Lang.get("errorReadingSuppress");
                                    msg = msg.replaceAll("<file>", DARKAQUA + f.getName() + RED);
                                    cs.sendMessage(RED + msg);
                                    suppressed = true;
                                }

                            }

                        }

                        cs.sendMessage(GREEN + Lang.get("done"));

                        String msg = Lang.get("allQuestPointsSet");
                        msg = msg.replaceAll("<number>", AQUA + "" + amount + GOLD);
                        getServer().broadcastMessage(YELLOW + "" + GOLD + msg);

                    } else {
                        cs.sendMessage(RED + Lang.get("errorDataFolder"));
                    }

                }
            });

            cs.sendMessage(YELLOW + Lang.get("settingAllQuestPoints"));
            for (Quester q : questers.values()) {

                q.questPoints = amount;

            }
            thread.start();

        } else {

            cs.sendMessage(RED + Lang.get("questCmdNoPerms"));

        }
    }

    private void adminFinish(final CommandSender cs, String[] args) {

        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.finish")) {

            Player target = null;

            for (Player p : getServer().getOnlinePlayers()) {

                if (p.getName().toLowerCase().contains(args[1].toLowerCase())) {
                    target = p;
                    break;
                }

            }

            if (target == null) {

                cs.sendMessage(YELLOW + Lang.get("playerNotFound"));

            } else {

                Quester quester = getQuester(target.getUniqueId());
                if (quester.currentQuests.isEmpty()) {

                    String msg = Lang.get("noCurrentQuest");
                    msg = msg.replaceAll("<player>", target.getName());
                    cs.sendMessage(YELLOW + msg);

                } else {

                    Quest found = findQuest(MiscUtil.concatArgArray(args, 2, args.length - 1, ' '));

                    if (found == null) {
                        cs.sendMessage(RED + Lang.get("questNotFound"));
                        return;
                    }

                    String msg1 = Lang.get("questForceFinish");
                    msg1 = msg1.replaceAll("<player>", GREEN + target.getName() + GOLD);
                    msg1 = msg1.replaceAll("<quest>", PURPLE + found.name + GOLD);
                    cs.sendMessage(GOLD + msg1);
                    String msg2 = Lang.get("questForcedFinish");
                    msg2 = msg2.replaceAll("<player>", GREEN + cs.getName() + GOLD);
                    msg2 = msg2.replaceAll("<quest>", PURPLE + found.name + GOLD);
                    target.sendMessage(GREEN + msg2);
                    found.completeQuest(quester);

                    quester.saveData();

                }

            }

        } else {

            cs.sendMessage(RED + Lang.get("questCmdNoPerms"));

        }
    }

    private void adminSetStage(final CommandSender cs, String[] args) {
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.setstage")) {

            Player target = null;

            for (Player p : getServer().getOnlinePlayers()) {

                //To ensure the correct player is selected
                if (p.getName().equalsIgnoreCase(args[1])) {
                    target = p;
                    break;
                }

            }

            if (target == null) {
                //
                for (Player p : getServer().getOnlinePlayers()) {

                    if (p.getName().toLowerCase().contains(args[1].toLowerCase())) {
                        target = p;
                        break;
                    }
                }
            }
            int stage = -1;
            if (args.length > 3) {
                try {
                    stage = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    cs.sendMessage(YELLOW + Lang.get("inputNum"));
                }
            } else {
                cs.sendMessage(YELLOW + Lang.get("COMMAND_QUESTADMIN_SETSTAGE_USAGE"));
                return;
            }

            if (target == null) {

                cs.sendMessage(YELLOW + Lang.get("playerNotFound"));

            } else {

                Quester quester = getQuester(target.getUniqueId());
                if (quester.currentQuests.isEmpty()) {

                    String msg = Lang.get("noCurrentQuest");
                    msg = msg.replaceAll("<player>", target.getName());
                    cs.sendMessage(YELLOW + msg);

                } else {

                    Quest found = findQuest(MiscUtil.concatArgArray(args, 2, args.length - 1, ' '));

                    if (found == null) {
                        cs.sendMessage(RED + Lang.get("questNotFound"));
                        return;
                    }

                    try {
                        found.setStage(quester, stage);
                    } catch (InvalidStageException e) {
                        String msg = Lang.get("invalidStageNum");
                        msg = msg.replaceAll("<quest>", PURPLE + found.name + RED);
                        cs.sendMessage(ChatColor.RED + msg);
                    }

                    quester.saveData();

                }

            }

        } else {

            cs.sendMessage(RED + Lang.get("questCmdNoPerms"));

        }
    }

    private void adminNextStage(final CommandSender cs, String[] args) {

        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.nextstage")) {

            Player target = null;

            for (Player p : getServer().getOnlinePlayers()) {

                if (p.getName().toLowerCase().contains(args[1].toLowerCase())) {
                    target = p;
                    break;
                }

            }

            if (target == null) {

                cs.sendMessage(YELLOW + Lang.get("playerNotFound"));

            } else {

                Quester quester = getQuester(target.getUniqueId());
                if (quester.currentQuests.isEmpty()) {

                    String msg = Lang.get("noCurrentQuest");
                    msg = msg.replaceAll("<player>", target.getName());
                    cs.sendMessage(YELLOW + msg);

                } else {

                    Quest found = findQuest(MiscUtil.concatArgArray(args, 2, args.length - 1, ' '));

                    if (found == null) {
                        cs.sendMessage(RED + Lang.get("questNotFound"));
                        return;
                    }

                    String msg1 = Lang.get("questForceNextStage");
                    msg1 = msg1.replaceAll("<player>", GREEN + target.getName() + GOLD);
                    msg1 = msg1.replaceAll("<quest>", PURPLE + found.name + GOLD);
                    cs.sendMessage(GOLD + msg1);
                    String msg2 = Lang.get("questForcedNextStage");
                    msg2 = msg2.replaceAll("<player>", GREEN + cs.getName() + GOLD);
                    msg2 = msg2.replaceAll("<quest>", PURPLE + found.name + GOLD);
                    target.sendMessage(GREEN + msg2);
                    found.nextStage(quester);

                    quester.saveData();

                }

            }

        } else {

            cs.sendMessage(RED + Lang.get("questCmdNoPerms"));

        }
    }

    private void adminQuit(final CommandSender cs, String[] args) {
try{
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.quit")) {

            Player target = null;

            for (Player p : getServer().getOnlinePlayers()) {

                if (p.getName().toLowerCase().contains(args[1].toLowerCase())) {
                    target = p;
                    break;
                }

            }

            if (target == null) {

                cs.sendMessage(YELLOW + Lang.get("playerNotFound"));

            } else {

                Quester quester = getQuester(target.getUniqueId());
                if (quester.currentQuests.isEmpty()) {

                    String msg = Lang.get("noCurrentQuest");
                    msg = msg.replaceAll("<player>", target.getName());
                    cs.sendMessage(YELLOW + msg);

                } else {

                    Quest found = findQuest(MiscUtil.concatArgArray(args, 2, args.length - 1, ' '));

                    if (found == null) {
                        cs.sendMessage(RED + Lang.get("questNotFound"));
                        return;
                    }

                    quester.hardQuit(found);

                    String msg1 = Lang.get("questForceQuit");
                    msg1 = msg1.replaceAll("<player>", GREEN + target.getName() + GOLD);
                    msg1 = msg1.replaceAll("<quest>", PURPLE + found.name + GOLD);
                    cs.sendMessage(GOLD + msg1);
                    String msg2 = Lang.get("questForcedQuit");
                    msg2 = msg2.replaceAll("<player>", GREEN + cs.getName() + GOLD);
                    msg2 = msg2.replaceAll("<quest>", PURPLE + found.name + GOLD);
                    target.sendMessage(GREEN + msg2);

                    quester.saveData();
                    quester.updateJournal();

                }

            }

        } else {

            cs.sendMessage(RED + Lang.get("questCmdNoPerms"));

        }
}catch(NullPointerException npe) {
	System.out.println("Please report this full error in Github ticket #130");
	npe.printStackTrace();
}
    }
    
    private void adminPurge(final CommandSender cs, String[] args) {

        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.purge")) {

            Quester quester = getQuester(args[1]);
            
            if (quester == null) {
				cs.sendMessage(YELLOW + Lang.get("playerNotFound"));
				return;
			}

            try {
            	quester.hardClear();
            	quester.saveData();
            	quester.updateJournal();
                final File dataFolder = new File(this.getDataFolder(), "data/");
                final File found = new File(dataFolder, quester.id + ".yml");
                found.delete();
                addToBlacklist(quester.id);

                String msg = Lang.get("questPurged");
                if (Bukkit.getOfflinePlayer(quester.id).getName() != null) {
                	msg = msg.replaceAll("<player>", GREEN + Bukkit.getOfflinePlayer(quester.id).getName() + GOLD);
                } else {
                	msg = msg.replaceAll("<player>", GREEN + args[1] + GOLD);
                }
                cs.sendMessage(GOLD + msg);
                cs.sendMessage(PURPLE + " UUID: " + DARKAQUA + quester.id);
            } catch (Exception e) {
            	getLogger().info("Data file does not exist for " + quester.id.toString());
            }

        } else {

            cs.sendMessage(RED + Lang.get("questCmdNoPerms"));

        }
    }

    private void adminReset(final CommandSender cs, String[] args) {

        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.reset")) {

            Quester quester = getQuester(args[1]);

            if (quester == null) {
                cs.sendMessage(YELLOW + Lang.get("playerNotFound"));
                return;
            }
            UUID id = quester.id;
            questers.remove(id);

            try {
                quester.hardClear();
                quester.saveData();
                quester.updateJournal();
                final File dataFolder = new File(this.getDataFolder(), "data/");
                final File found = new File(dataFolder, id + ".yml");
                found.delete();

                String msg = Lang.get("questReset");
                if (Bukkit.getOfflinePlayer(id).getName() != null) {
                    msg = msg.replaceAll("<player>", GREEN + Bukkit.getOfflinePlayer(id).getName() + GOLD);
                } else {
                    msg = msg.replaceAll("<player>", GREEN + args[1] + GOLD);
                }
                cs.sendMessage(GOLD + msg);
                cs.sendMessage(PURPLE + " UUID: " + DARKAQUA + id);


            } catch (Exception e) {
                getLogger().info("Data file does not exist for " + id.toString());
            }

            quester = new Quester(this);
            quester.id = id;
            quester.saveData();
            questers.put(id, quester);

        } else {

            cs.sendMessage(RED + Lang.get("questCmdNoPerms"));

        }
    }
    
    private void adminStats(final CommandSender cs, String[] args) {
    	
        if (cs.hasPermission("quests.admin.*") && cs.hasPermission("quests.admin.stats")) {
        	
            questsStats(cs, args);
        	
        } else {
        	
        	cs.sendMessage(RED + Lang.get("questCmdNoPerms"));
        	
        }
    }

    private void adminRemove(final CommandSender cs, String[] args) {
    	
        if (cs.hasPermission("quests.admin.*") && cs.hasPermission("quests.admin.remove")) {
        	
            Quester quester = getQuester(args[1]);

            if (quester == null) {
				cs.sendMessage(YELLOW + Lang.get("playerNotFound"));
				return;
			}
            
            Quest toRemove = findQuest(MiscUtil.concatArgArray(args, 2, args.length - 1, ' '));
            if (toRemove == null) {
                cs.sendMessage(RED + Lang.get("questNotFound"));
                return;
            }

            String msg = Lang.get("questRemoved");
            if (Bukkit.getOfflinePlayer(quester.id).getName() != null) {
            	msg = msg.replaceAll("<player>", GREEN + Bukkit.getOfflinePlayer(quester.id).getName() + GOLD);
            } else {
            	msg = msg.replaceAll("<player>", GREEN + args[1] + GOLD);
            }
            msg = msg.replaceAll("<quest>", ChatColor.DARK_PURPLE + toRemove.name + ChatColor.AQUA);
            cs.sendMessage(GOLD + msg);
            cs.sendMessage(PURPLE + " UUID: " + DARKAQUA + quester.id.toString());

            quester.hardRemove(toRemove);
            
            quester.saveData();
            quester.updateJournal();

        } else {
        	
        	cs.sendMessage(RED + Lang.get("questCmdNoPerms"));
        	
        }
    }

    private boolean questActionsCommandHandler(final CommandSender cs, String[] args) {

        if (cs instanceof Player) {

            if (args.length == 0) {

                questsHelp(cs);
                return true;

            } else {

                if (args[0].equalsIgnoreCase(Lang.get("COMMAND_LIST"))) {

                    questsList(cs, args);

                } else if (args[0].equalsIgnoreCase(Lang.get("COMMAND_TAKE"))) {

                    questsTake((Player) cs, args);

                } else if (args[0].equalsIgnoreCase(Lang.get("COMMAND_QUIT"))) {

                    questsQuit((Player) cs, args);

                } else if (args[0].equalsIgnoreCase(Lang.get("COMMAND_STATS"))) {

                    questsStats(cs, null);

                } else if (args[0].equalsIgnoreCase(Lang.get("COMMAND_JOURNAL"))) {

                    questsJournal((Player) cs);

                } else if (args[0].equalsIgnoreCase(Lang.get("COMMAND_TOP"))) {

                    questsTop(cs, args);

                } else if (args[0].equalsIgnoreCase(Lang.get("COMMAND_EDITOR"))) {

                    questsEditor(cs);

                } else if (args[0].equalsIgnoreCase(Lang.get("COMMAND_EVENTS_EDITOR"))) {

                    questsEvents(cs);

                } else if (args[0].equalsIgnoreCase(Lang.get("COMMAND_INFO"))) {

                    questsInfo(cs);

                } else {

                    cs.sendMessage(YELLOW + Lang.get("questsUnknownCommand"));
                    return true;

                }

            }

        } else if (args.length == 1) {

            if (args[0].equalsIgnoreCase("convert")) {

                if (cs instanceof ConsoleCommandSender) {

                    convertQuesters();

                } else {
                    cs.sendMessage(YELLOW + Lang.get("questsUnknownCommand"));
                    return true;
                }

            } else {

                cs.sendMessage(YELLOW + "This command may only be performed in-game.");
                return true;

            }

        } else {

            cs.sendMessage(YELLOW + "This command may only be performed in-game.");
            return true;

        }

        return true;
    }

    private boolean questsInfo(final CommandSender cs) {
        cs.sendMessage(GOLD + Lang.get("quests") + " " + this.getDescription().getVersion());
        cs.sendMessage(GOLD + Lang.get("createdBy") + " " + DARKRED + "Blackvein");
        return true;
    }

    private boolean questsEvents(final CommandSender cs) {
        if (cs.hasPermission("quests.editor.*") || cs.hasPermission("quests.editor.events.editor")) {
            eventFactory.convoCreator.buildConversation((Conversable) cs).begin();
        } else {
            cs.sendMessage(RED + Lang.get("eventEditorNoPerms"));
        }
        return true;
    }

    private boolean questsEditor(final CommandSender cs) {
        if (cs.hasPermission("quests.editor.*") || cs.hasPermission("quests.editor.editor")) {
            questFactory.convoCreator.buildConversation((Conversable) cs).begin();
        } else {
            cs.sendMessage(RED + Lang.get("questEditorNoPerms"));
        }
        return true;
    }

    private boolean questsTop(final CommandSender cs, String[] args) {
        if (args.length == 1 || args.length > 2) {

            cs.sendMessage(YELLOW + Lang.get("COMMAND_TOP_USAGE"));

        } else {

            int topNumber;

            try {

                topNumber = Integer.parseInt(args[1]);

            } catch (NumberFormatException e) {

                cs.sendMessage(YELLOW + Lang.get("inputNum"));
                return true;

            }

            if (topNumber < 1) {

                cs.sendMessage(YELLOW + Lang.get("inputPosNum"));
                return true;

            }

            File folder = new File(this.getDataFolder(), "data");
            File[] playerFiles = folder.listFiles();

            Map<String, Integer> questPoints = new HashMap<String, Integer>();

            for (File f : playerFiles) {

                FileConfiguration data = new YamlConfiguration();
                try {

                    data.load(f);

                } catch (IOException e) {

                    e.printStackTrace();

                } catch (InvalidConfigurationException e) {
                    e.printStackTrace();
                }

                String name = f.getName().substring(0, (f.getName().indexOf(".")));
                questPoints.put(name, data.getInt("quest-points"));

            }

            LinkedHashMap<String, Integer> sortedMap = (LinkedHashMap<String, Integer>) Quests.sort(questPoints);

            int numPrinted = 0;

            String msg = Lang.get("topQuestersTitle");
            msg = msg.replaceAll("<number>", PURPLE + "" + topNumber + GOLD);
            cs.sendMessage(GOLD + msg);
            for (String s : sortedMap.keySet()) {

                int i = (Integer) sortedMap.get(s);
                s = s.trim();
                UUID id = UUID.fromString(s);
                s = Bukkit.getOfflinePlayer(id).getName();
                numPrinted++;
                cs.sendMessage(YELLOW + String.valueOf(numPrinted) + ". " + s + " - " + PURPLE + i + YELLOW + " " + Lang.get("questPoints"));

                if (numPrinted == topNumber) {
                    break;
                }

            }

        }

        return true;
    }

    private void questsStats(final CommandSender cs, String[] args) {

    	Quester quester;
    	
    	if (args != null) {
    		
    		quester = getQuester(args[1]);
    		
			if (quester == null) {
				cs.sendMessage(YELLOW + Lang.get("playerNotFound"));
				return;
			} else if (Bukkit.getOfflinePlayer(quester.id).getName() != null) {
				cs.sendMessage(GOLD + "- " + Bukkit.getOfflinePlayer(quester.id).getName() + " -");
			} else {
				cs.sendMessage(GOLD + "- " + args[1] + " -");
			}
    	} else {
    		quester = getQuester(((Player)cs).getUniqueId());
    		cs.sendMessage(GOLD + "- " + ((Player)cs).getName() + " -");
    	}
    	
        cs.sendMessage(YELLOW + Lang.get("questPointsDisplay") + " " + PURPLE + quester.questPoints);
        if (quester.currentQuests.isEmpty()) {
            cs.sendMessage(YELLOW + Lang.get("currentQuest") + " " + PURPLE + Lang.get("none"));
        } else {
           cs.sendMessage(YELLOW + Lang.get("currentQuest"));
            for (Quest q : quester.currentQuests.keySet()) {
                cs.sendMessage(PINK + " - " + PURPLE + q.name);
            }
        }

        String completed;

        if (quester.completedQuests.isEmpty()) {
            completed = PURPLE + Lang.get("none");
        } else {

            completed = PURPLE + "";
            for (String s : quester.completedQuests) {

                completed += s;

                if (quester.amountsCompleted.containsKey(s) && quester.amountsCompleted.get(s) > 1) {
                    completed += PINK + " (x" + quester.amountsCompleted.get(s) + ")";
                }

                if (quester.completedQuests.indexOf(s) < (quester.completedQuests.size() - 1)) {
                    completed += ", ";
                }

            }

        }

        cs.sendMessage(YELLOW + Lang.get("completedQuestsTitle"));
        cs.sendMessage(completed);
    }

    private void questsJournal(final Player player) {

        Quester quester = getQuester(player.getUniqueId());

        if(quester.hasJournal) {

            Inventory inv = player.getInventory();
            ItemStack[] arr = inv.getContents();
            for(int i = 0; i < arr.length; i++) {

                if(arr[i] != null) {
                    if(ItemUtil.isJournal(arr[i])) {
                        inv.setItem(i, null);
                        break;
                    }
                }

            }
            player.sendMessage(YELLOW + Lang.get("journalPutAway"));
            quester.hasJournal = false;

        } else if(player.getItemInHand() == null || player.getItemInHand().getType().equals(Material.AIR)) {

            ItemStack stack = new ItemStack(Material.WRITTEN_BOOK, 1);
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(PINK + Lang.get("journalTitle"));
            stack.setItemMeta(meta);
            player.setItemInHand(stack);
            player.sendMessage(YELLOW + Lang.get("journalTaken"));
            quester.hasJournal = true;
            quester.updateJournal();

        } else {

            Inventory inv = player.getInventory();
            ItemStack[] arr = inv.getContents();
            boolean given = false;
            for(int i = 0; i < arr.length; i++) {

                if(arr[i] == null) {
                    ItemStack stack = new ItemStack(Material.WRITTEN_BOOK, 1);
                    ItemMeta meta = stack.getItemMeta();
                    meta.setDisplayName(PINK + Lang.get("journalTitle"));
                    stack.setItemMeta(meta);
                    inv.setItem(i, stack);
                    player.sendMessage(YELLOW + Lang.get("journalTaken"));
                    given = true;
                    break;
                }

            }

            if(given) {
                quester.hasJournal = true;
                quester.updateJournal();
            }else
                player.sendMessage(YELLOW + Lang.get("journalNoRoom"));

        }
    }

    private boolean questsQuit(final Player player, String[] args) {

        if (allowQuitting == true) {

            if (((Player) player).hasPermission("quests.quit")) {

                if (args.length == 1) {
                    player.sendMessage(RED + Lang.get("COMMAND_QUIT_HELP"));
                    return true;
                }

                Quester quester = getQuester(player.getUniqueId());
                if (quester.currentQuests.isEmpty() == false) {

                    Quest found = findQuest(MiscUtil.concatArgArray(args, 1, args.length - 1, ' '));

                    if (found == null) {
                        player.sendMessage(RED + Lang.get("questNotFound"));
                        return true;
                    }

                    quester.hardQuit(found);

                    String msg = Lang.get("questQuit");
                    msg = msg.replaceAll("<quest>", PURPLE + found.name + YELLOW);
                    player.sendMessage(YELLOW + msg);
                    quester.saveData();
                    quester.loadData();
                    quester.updateJournal();
                    return true;

                } else {

                    player.sendMessage(YELLOW + Lang.get("noActiveQuest"));
                    return true;

                }

            } else {

                player.sendMessage(RED + Lang.get("questQuitNoPerms"));
                return true;
            }

        } else {

            player.sendMessage(YELLOW + Lang.get("questQuitDisabled"));
            return true;

        }
    }

    private void questsTake(final Player player, String[] args) {
        if (allowCommands == true) {

            if (((Player) player).hasPermission("quests.take")) {

                if (args.length == 1) {

                    player.sendMessage(YELLOW + Lang.get("COMMAND_TAKE_USAGE"));

                } else {

                    String name = null;

                    if (args.length == 2) {
                        name = args[1].toLowerCase();
                    } else {

                        boolean first = true;
                        int lastIndex = (args.length - 1);
                        int index = 0;

                        for (String s : args) {

                            if (index != 0) {

                                if (first) {

                                    first = false;
                                    if (args.length > 2) {
                                        name = s.toLowerCase() + " ";
                                    } else {
                                        name = s.toLowerCase();
                                    }

                                } else if (index == lastIndex) {
                                    name = name + s.toLowerCase();
                                } else {
                                    name = name + s.toLowerCase() + " ";
                                }

                            }

                            index++;

                        }

                    }

                    Quest questToFind = findQuest(name);

                    if (questToFind != null) {

                        final Quest q = questToFind;
                        final Quester quester = getQuester(player.getUniqueId());

                        if (quester.currentQuests.size() >= maxQuests && maxQuests > 0) {
                            String msg = Lang.get("questMaxAllowed");
                            msg = msg.replaceAll("<number>", String.valueOf(maxQuests));
                            player.sendMessage(YELLOW + msg);
                        } else if (quester.currentQuests.containsKey(q)) {
                            String msg = Lang.get("questAlreadyOn");
                            player.sendMessage(YELLOW + msg);
                        } else if (quester.completedQuests.contains(q.name) && q.redoDelay < 0) {
                            String msg = Lang.get("questAlreadyCompleted");
                            msg = msg.replaceAll("<quest>", PURPLE + q.name + YELLOW);
                            player.sendMessage(YELLOW + msg);
                        } else if (q.npcStart != null && allowCommandsForNpcQuests == false) {
                            String msg = Lang.get("mustSpeakTo");
                            msg = msg.replaceAll("<npc>", PURPLE + q.npcStart.getName() + YELLOW);
                            player.sendMessage(YELLOW + msg);
                        } else if (q.blockStart != null) {
                            String msg = Lang.get("noCommandStart");
                            msg = msg.replaceAll("<quest>", PURPLE + q.name + YELLOW);
                            player.sendMessage(YELLOW + msg);
                        } else {

                            boolean takeable = true;

                            if (quester.completedQuests.contains(q.name)) {

                                if (quester.getDifference(q) > 0) {
                                    String early = Lang.get("questTooEarly");
                                    early = early.replaceAll("<quest>", ChatColor.AQUA + q.name + ChatColor.YELLOW);
                                    early = early.replaceAll("<time>", ChatColor.DARK_PURPLE + Quests.getTime(quester.getDifference(q)) + ChatColor.YELLOW);
                                    player.sendMessage(YELLOW + early);
                                    takeable = false;
                                }

                            }

                            if (q.region != null) {

                                boolean inRegion = false;
                                Player p = quester.getPlayer();
                                RegionManager rm = worldGuard.getRegionManager(p.getWorld());
                                Iterator<ProtectedRegion> it = rm.getApplicableRegions(p.getLocation()).iterator();
                                while (it.hasNext()) {
                                    ProtectedRegion pr = it.next();
                                    if (pr.getId().equalsIgnoreCase(q.region)) {
                                        inRegion = true;
                                        break;
                                    }
                                }

                                if (inRegion == false) {
                                    String msg = Lang.get("questInvalidLocation");
                                    msg = msg.replaceAll("<quest>", AQUA + q.name + YELLOW);
                                    player.sendMessage(YELLOW + msg);
                                    takeable = false;
                                }

                            }

                            if (takeable == true) {

                                if (player instanceof Conversable) {

                                    if (((Player) player).isConversing() == false) {

                                        quester.questToTake = q.name;

                                        String s
                                                = GOLD + "- " + PURPLE + quester.questToTake + GOLD + " -\n"
                                                + "\n"
                                                + RESET + getQuest(quester.questToTake).description + "\n";

                                        for (String msg : s.split("<br>")) {
                                            player.sendMessage(msg);
                                        }

                                        conversationFactory.buildConversation((Conversable) player).begin();

                                    } else {

                                        player.sendMessage(YELLOW + Lang.get("alreadyConversing"));

                                    }
                                } else {
                                }

                            }

                        }

                    } else {
                        player.sendMessage(YELLOW + Lang.get("questNotFound"));
                    }

                }

            } else {

                player.sendMessage(RED + Lang.get("questTakeNoPerms"));

            }

        } else {

            player.sendMessage(YELLOW + Lang.get("questTakeDisabled"));

        }
    }

    private void questsList(final CommandSender cs, String[] args) {
        if (((Player) cs).hasPermission("quests.list")) {

            if (args.length == 1) {
                listQuests((Player) cs, 1);
            } else if (args.length == 2) {

                int page;

                try {

                    page = Integer.parseInt(args[1]);
                    if (page < 1) {

                        cs.sendMessage(YELLOW + Lang.get("pageSelectionPosNum"));
                        return;

                    }

                } catch (NumberFormatException e) {

                    cs.sendMessage(YELLOW + Lang.get("pageSelectionNum"));
                    return;

                }

                listQuests((Player) cs, page);

            }

        } else {

            cs.sendMessage(RED + Lang.get("questListNoPerms"));

        }
    }

    private void questsHelp(final CommandSender cs) {
        if (((Player) cs).hasPermission("quests.quests")) {

            Player p = (Player) cs;
            printHelp(p);

        } else {

            cs.sendMessage(RED + Lang.get("questCmdNoPerms"));
        }
    }

    private boolean questCommandHandler(final CommandSender cs, String[] args) {

        if (cs instanceof Player) {

            if (((Player) cs).hasPermission("quests.quest")) {

                if (args.length == 0) {

                    showObjectives((Player) cs);

                } else {

                    showQuestDetails(cs, args);

                }

            } else {

                cs.sendMessage(RED + Lang.get("questCmdNoPerms"));
                return true;

            }

        } else {

            cs.sendMessage(YELLOW + "This command may only be performed in-game.");
            return true;

        }

        return true;
    }

    private void showQuestDetails(final CommandSender cs, String[] args) {

        if (((Player) cs).hasPermission("quests.questinfo")) {

            String name = "";

            if (args.length == 1) {
                name = args[0].toLowerCase();
            } else {

                int index = 0;
                for (String s : args) {

                    if (index == (args.length - 1)) {
                        name = name + s.toLowerCase();
                    } else {
                        name = name + s.toLowerCase() + " ";
                    }

                    index++;

                }
            }

            Quest q = findQuest(name);

            if (q != null) {

                Player player = (Player) cs;
                Quester quester = getQuester(player.getUniqueId());

                cs.sendMessage(GOLD + "- " + q.name + " -");
                cs.sendMessage(" ");
                if (q.redoDelay > -1) {

                    if (q.redoDelay == 0) {
                        cs.sendMessage(DARKAQUA + Lang.get("readoable"));
                    } else {
                        String msg = Lang.get("redoableEvery");
                        msg = msg.replaceAll("<time>", AQUA + getTime(q.redoDelay) + DARKAQUA);
                        cs.sendMessage(DARKAQUA + msg);
                    }

                }
                if (q.npcStart != null) {
                    String msg = Lang.get("speakTo");
                    msg = msg.replaceAll("<npc>", q.npcStart.getName());
                    cs.sendMessage(YELLOW + msg);
                } else {
                    cs.sendMessage(YELLOW + q.description);
                }

                cs.sendMessage(" ");

                if (showQuestReqs == true) {

                    cs.sendMessage(GOLD + Lang.get("requirements"));

                    if (q.permissionReqs.isEmpty() == false) {

                        for (String perm : q.permissionReqs) {

                            if (permission.has(player, perm)) {
                                cs.sendMessage(GREEN + Lang.get("permissionDisplay") + " " + perm);
                            } else {
                                cs.sendMessage(RED + Lang.get("permissionDisplay") + " " + perm);
                            }

                        }

                    }

                    if (q.heroesPrimaryClassReq != null) {

                        if (this.testPrimaryHeroesClass(q.heroesPrimaryClassReq, player.getUniqueId())) {
                            cs.sendMessage(BOLD + "" + GREEN + q.heroesPrimaryClassReq + RESET + "" + DARKGREEN + " " + Lang.get("heroesClass"));
                        } else {
                            cs.sendMessage(BOLD + "" + DARKRED + q.heroesPrimaryClassReq + RESET + "" + RED + " " + Lang.get("heroesClass"));
                        }

                    }

                    if (q.heroesSecondaryClassReq != null) {

                        if (this.testSecondaryHeroesClass(q.heroesSecondaryClassReq, player.getUniqueId())) {
                            cs.sendMessage(BOLD + "" + DARKRED + q.heroesSecondaryClassReq + RESET + "" + RED + " " + Lang.get("heroesClass"));
                        } else {
                            cs.sendMessage(BOLD + "" + GREEN + q.heroesSecondaryClassReq + RESET + "" + DARKGREEN + " " + Lang.get("heroesClass"));
                        }

                    }

                    if (q.mcMMOSkillReqs.isEmpty() == false) {

                        for (String skill : q.mcMMOSkillReqs) {

                            int level = Quests.getMCMMOSkillLevel(Quests.getMcMMOSkill(skill), player.getName());
                            int req = q.mcMMOAmountReqs.get(q.mcMMOSkillReqs.indexOf(skill));
                            String skillName = MiscUtil.getCapitalized(skill);

                            if (level >= req) {
                                cs.sendMessage(GREEN + skillName + " " + Lang.get("mcMMOLevel") + " " + req);
                            } else {
                                cs.sendMessage(RED + skillName + " " + Lang.get("mcMMOLevel") + " " + req);
                            }

                        }

                    }

                    if (q.questPointsReq != 0) {

                        if (quester.questPoints >= q.questPointsReq) {
                            cs.sendMessage(GRAY + "- " + GREEN + q.questPointsReq + " " + Lang.get("questPoints"));
                        } else {
                            cs.sendMessage(GRAY + "- " + RED + q.questPointsReq + " " + Lang.get("questPoints"));
                        }

                    }

                    if (q.moneyReq != 0) {

                        if (economy.getBalance(quester.getOfflinePlayer()) >= q.moneyReq) {
                            if (q.moneyReq == 1) {
                                cs.sendMessage(GRAY + "- " + GREEN + q.moneyReq + " " + Quests.getCurrency(false));
                            } else {
                                cs.sendMessage(GRAY + "- " + GREEN + q.moneyReq + " " + Quests.getCurrency(true));
                            }
                        } else {
                            if (q.moneyReq == 1) {
                                cs.sendMessage(GRAY + "- " + RED + q.moneyReq + " " + Quests.getCurrency(false));
                            } else {
                                cs.sendMessage(GRAY + "- " + RED + q.moneyReq + " " + Quests.getCurrency(true));
                            }
                        }

                    }

                    if (q.items.isEmpty() == false) {

                        for (ItemStack is : q.items) {

                            if (hasItem(player, is) == true) {
                                cs.sendMessage(GRAY + "- " + GREEN + ItemUtil.getString(is));
                            } else {
                                cs.sendMessage(GRAY + "- " + RED + ItemUtil.getString(is));
                            }

                        }

                    }

                    if (q.neededQuests.isEmpty() == false) {

                        for (String s : q.neededQuests) {

                            if (quester.completedQuests.contains(s)) {
                                cs.sendMessage(GRAY + "- " + GREEN + Lang.get("complete") + " " + ITALIC + s);
                            } else {
                                cs.sendMessage(GRAY + "- " + RED + Lang.get("complete") + " " + ITALIC + s);
                            }

                        }

                    }

                    if (q.blockQuests.isEmpty() == false) {

                        for (String s : q.blockQuests) {

                            if (quester.completedQuests.contains(s)) {
                                String msg = Lang.get("haveCompleted");
                                msg = msg.replaceAll("<quest>", ITALIC + "" + PURPLE + s + RED);
                                cs.sendMessage(GRAY + "- " + RED + msg);
                            } else {
                                String msg = Lang.get("cannotComplete");
                                msg = msg.replaceAll("<quest>", ITALIC + "" + PURPLE + s + GREEN);
                                cs.sendMessage(GRAY + "- " + GREEN + msg);
                            }

                        }

                    }

                }

            } else {

                cs.sendMessage(YELLOW + Lang.get("questNotFound"));

            }

        } else {

            cs.sendMessage(RED + Lang.get("questInfoNoPerms"));

        }
    }

    private void showObjectives(final Player player) {

        Quester quester = getQuester(player.getUniqueId());
        if (quester.currentQuests.isEmpty() == false) {
            for (Quest q : quester.currentQuests.keySet()) {
                Stage stage = quester.getCurrentStage(q);
                q.updateCompass(quester, stage);

            	try {
                if (getQuester(player.getUniqueId()).getQuestData(q).delayStartTime == 0) {

                    String msg = Lang.get("questObjectivesTitle");
                    msg = msg.replaceAll("<quest>", q.name);
                    player.sendMessage(ChatColor.GOLD + msg);

                    for (String s : getQuester(player.getUniqueId()).getObjectivesReal(q)) {

                        player.sendMessage(s);

                    }

                }
                
            	} catch (Exception e) {
            		//TODO find source of NullPointerException from Github ticket #130
            	}

            }

        } else {

            player.sendMessage(YELLOW + Lang.get("noActiveQuest"));

        }

    }

    public void printAdminHelp(CommandSender cs) {

        cs.sendMessage(RED + Lang.get("questAdminHelpTitle"));
        cs.sendMessage("");
        cs.sendMessage(DARKRED + "/questadmin" + RED + " " + Lang.get("COMMAND_QUESTADMIN_HELP"));
        if(cs.hasPermission("quests.admin.*")){
        	cs.sendMessage(DARKRED + "/questadmin " + RED + Lang.get("COMMAND_QUESTADMIN_STATS_HELP"));
        	cs.sendMessage(DARKRED + "/questadmin " + RED + Lang.get("COMMAND_QUESTADMIN_GIVE_HELP"));
            cs.sendMessage(DARKRED + "/questadmin " + RED + Lang.get("COMMAND_QUESTADMIN_QUIT_HELP"));
            cs.sendMessage(DARKRED + "/questadmin " + RED + Lang.get("COMMAND_QUESTADMIN_POINTS_HELP"));
            cs.sendMessage(DARKRED + "/questadmin " + RED + Lang.get("COMMAND_QUESTADMIN_TAKEPOINTS_HELP"));
            cs.sendMessage(DARKRED + "/questadmin " + RED + Lang.get("COMMAND_QUESTADMIN_GIVEPOINTS_HELP"));
            cs.sendMessage(DARKRED + "/questadmin " + RED + Lang.get("COMMAND_QUESTADMIN_POINTSALL_HELP"));
            cs.sendMessage(DARKRED + "/questadmin " + RED + Lang.get("COMMAND_QUESTADMIN_FINISH_HELP"));
            cs.sendMessage(DARKRED + "/questadmin " + RED + Lang.get("COMMAND_QUESTADMIN_NEXTSTAGE_HELP"));
            cs.sendMessage(DARKRED + "/questadmin " + RED + Lang.get("COMMAND_QUESTADMIN_SETSTAGE_HELP"));
            cs.sendMessage(DARKRED + "/questadmin " + RED + Lang.get("COMMAND_QUESTADMIN_PURGE_HELP"));
            cs.sendMessage(DARKRED + "/questadmin " + RED + Lang.get("COMMAND_QUESTADMIN_RESET_HELP"));
            cs.sendMessage(DARKRED + "/questadmin " + RED + Lang.get("COMMAND_QUESTADMIN_REMOVE_HELP"));
            cs.sendMessage(DARKRED + "/questadmin " + RED + Lang.get("COMMAND_QUESTADMIN_TOGGLEGUI_HELP"));
            cs.sendMessage(DARKRED + "/questadmin " + RED + Lang.get("COMMAND_QUESTADMIN_RELOAD_HELP"));
        } else{
        	if (cs.hasPermission("quests.admin.stats")) {
        		cs.sendMessage(DARKRED + "/questadmin " + RED + Lang.get("COMMAND_QUESTADMIN_STATS_HELP"));
        	}
        	if (cs.hasPermission("quests.admin.give")) {
        		cs.sendMessage(DARKRED + "/questadmin " + RED + Lang.get("COMMAND_QUESTADMIN_GIVE_HELP"));
        	}
        	if (cs.hasPermission("quests.admin.quit")) {
        		cs.sendMessage(DARKRED + "/questadmin " + RED + Lang.get("COMMAND_QUESTADMIN_QUIT_HELP"));
        	}
        	if (cs.hasPermission("quests.admin.points")) {
        		cs.sendMessage(DARKRED + "/questadmin " + RED + Lang.get("COMMAND_QUESTADMIN_POINTS_HELP"));
        	}
        	if (cs.hasPermission("quests.admin.takepoints")) {
        		cs.sendMessage(DARKRED + "/questadmin " + RED + Lang.get("COMMAND_QUESTADMIN_TAKEPOINTS_HELP"));
        	}
        	if (cs.hasPermission("quests.admin.givepoints")) {
        		cs.sendMessage(DARKRED + "/questadmin " + RED + Lang.get("COMMAND_QUESTADMIN_GIVEPOINTS_HELP"));
        	}
        	if (cs.hasPermission("quests.admin.pointsall")) {
        		cs.sendMessage(DARKRED + "/questadmin " + RED + Lang.get("COMMAND_QUESTADMIN_POINTSALL_HELP"));
        	}
        	if (cs.hasPermission("quests.admin.finish")) {
        		cs.sendMessage(DARKRED + "/questadmin " + RED + Lang.get("COMMAND_QUESTADMIN_FINISH_HELP"));
        	}
        	if (cs.hasPermission("quests.admin.nextstage")) {
        		cs.sendMessage(DARKRED + "/questadmin " + RED + Lang.get("COMMAND_QUESTADMIN_NEXTSTAGE_HELP"));
        	}
        	if (cs.hasPermission("quests.admin.setstage")) {
        		cs.sendMessage(DARKRED + "/questadmin " + RED + Lang.get("COMMAND_QUESTADMIN_SETSTAGE_HELP"));
        	}
        	if (cs.hasPermission("quests.admin.purge")) {
        		cs.sendMessage(DARKRED + "/questadmin " + RED + Lang.get("COMMAND_QUESTADMIN_PURGE_HELP"));
        	}
            if (cs.hasPermission("quests.admin.reset")) {
                cs.sendMessage(DARKRED + "/questadmin " + RED + Lang.get("COMMAND_QUESTADMIN_RESET_HELP"));
            }
        	if (cs.hasPermission("quests.admin.remove")) {
        		cs.sendMessage(DARKRED + "/questadmin " + RED + Lang.get("COMMAND_QUESTADMIN_REMOVE_HELP"));
        	}
        	if (citizens != null && cs.hasPermission("quests.admin.togglegui")) {
        		cs.sendMessage(DARKRED + "/questadmin " + RED + Lang.get("COMMAND_QUESTADMIN_TOGGLEGUI_HELP"));
        	}
        	if (cs.hasPermission("quests.admin.reload")) {
        		cs.sendMessage(DARKRED + "/questadmin " + RED + Lang.get("COMMAND_QUESTADMIN_RELOAD_HELP"));
        	}

        }

    }

    public void listQuests(Player player, int page) {

        if (quests.size() < ((page * 8) - 7)) {
            player.sendMessage(YELLOW + Lang.get("pageNotExist"));
        } else {
            player.sendMessage(GOLD + Lang.get("questsTitle"));

            int numOrder = (page - 1) * 8;

            if (numOrder == 0) {
                numOrder = 1;
            }

            List<Quest> subQuests;

            if (numOrder > 1) {
                if (quests.size() >= (numOrder + 7)) {
                    subQuests = quests.subList((numOrder), (numOrder + 7));
                } else {
                    subQuests = quests.subList((numOrder), quests.size());
                }
            } else if (quests.size() >= (numOrder + 7)) {
                subQuests = quests.subList((numOrder - 1), (numOrder + 7));
            } else {
                subQuests = quests.subList((numOrder - 1), quests.size());
            }

            if (numOrder != 1) {
                numOrder++;
            }

            for (Quest q : subQuests) {

                player.sendMessage(YELLOW + Integer.toString(numOrder) + ". " + q.name);
                numOrder++;

            }

            int numPages = quests.size() / 8;
            if ((quests.size() % 8) > 0 || numPages == 0) {
                numPages++;
            }

            String msg = Lang.get("pageFooter");
            msg = msg.replaceAll("<current>", String.valueOf(page));
            msg = msg.replaceAll("<all>", String.valueOf(numPages));
            player.sendMessage(GOLD + msg);

        }

    }

    public void reloadQuests() {

        quests.clear();
        events.clear();
        questerBlacklist.clear();
        loadQuests();
        loadData();
        loadEvents();
        
    	//Reload config from disc in-case a setting was changed
        reloadConfig();
        
        loadConfig();

        Lang.clearPhrases();
        lang.initPhrases();
        lang.loadLang();

        for (Quester quester : questers.values()) {
            for (Quest q : quester.currentQuests.keySet()) {
                quester.checkQuest(q);
            }
        }

    }

    public Quester getQuester(UUID id) {

        Quester quester = null;

        if (questers.containsKey(id)) {
            quester = questers.get(id);
        }

        if (quester == null) {

            if (debug == true && !questerBlacklist.contains(id.toString())) {
                getLogger().log(Level.WARNING, "Quester data for UUID \"" + id.toString() + "\" not stored. Attempting manual data retrieval..");
            }

            quester = new Quester(this);
            quester.id = id;
            if (quester.loadData() == false && !questerBlacklist.contains(id.toString())) {
            	if (citizens != null) {
            		if (citizens.getNPCRegistry().getByUniqueId(id) != null) {
            			return quester;
            		}
            	}
            	getLogger().info("Quester not found for UUID \"" + id.toString() + "\". Consider adding them to the Quester blacklist.");
            } else {
                if (debug == true && !questerBlacklist.contains(id.toString())) {
                    getLogger().log(Level.INFO, "Manual data retrieval succeeded for UUID \"" + id.toString() + "\"");
                }
                questers.put(id, quester);
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
    	
    	try {
    		if (id == null) {
    			id = UUIDFetcher.getUUIDOf(name);
    		}
		} catch (Exception e) {
			//Do nothing
		}
    	
    	if (id != null) {
    		quester = getQuester(id);
    	}
    	
    	return quester;
    }

    public Map<UUID, Quester> getOnlineQuesters() {

        Map<UUID, Quester> qs = new HashMap<UUID, Quester>();

        for (Player p : getServer().getOnlinePlayers()) {

            Quester quester = new Quester(this);
            quester.id = p.getUniqueId();
            if (quester.loadData() == false) {
                quester.saveData();
            }
            qs.put(p.getUniqueId(), quester);
            // Kind of hacky to put this here, works around issues with the compass on fast join
            quester.findCompassTarget();

        }

        return qs;

    }

    private boolean needsSaving = false;
    private String questName = "";
    private Quest quest;

    public void loadQuests() {

        boolean failedToLoad;
        totalQuestPoints = 0;
        needsSaving = false;

        FileConfiguration config = new YamlConfiguration();
        File file = new File(this.getDataFolder(), "quests.yml");

        try {

            config.load(file);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }

        ConfigurationSection questsSection;
        if (config.contains("quests")) {
            questsSection = config.getConfigurationSection("quests");
        } else {
            questsSection = config.createSection("quests");
            needsSaving = true;
        }

        for (String key : questsSection.getKeys(false)) {

            try { // main "skip quest" try/catch block

                questName = key;

                quest = new Quest();
                failedToLoad = false;

                if (config.contains("quests." + questName + ".name")) {
                    // TODO why have a name attr then path key can be guest name?
                    quest.name = parseString(config.getString("quests." + questName + ".name"), quest);
                } else {
                    skipQuestProcess("Quest block \'" + questName + "\' is missing " + RED + "name:");
                }

                if (citizens != null && config.contains("quests." + questName + ".npc-giver-id")) {
                    if (CitizensAPI.getNPCRegistry().getById(config.getInt("quests." + questName + ".npc-giver-id")) != null) {

                        quest.npcStart = CitizensAPI.getNPCRegistry().getById(config.getInt("quests." + questName + ".npc-giver-id"));
                        questNPCs.add(CitizensAPI.getNPCRegistry().getById(config.getInt("quests." + questName + ".npc-giver-id")));

                    } else {
                        skipQuestProcess("npc-giver-id: for Quest " + quest.name + " is not a valid NPC id!");
                    }
                }

                if (config.contains("quests." + questName + ".block-start")) {

                    Location location = getLocation(config.getString("quests." + questName + ".block-start"));
                    if (location != null) {
                        quest.blockStart = location;
                    } else {
                        skipQuestProcess(new String[]{
                            "block-start: for Quest " + quest.name + " is not in proper location format!",
                            "Proper location format is: \"WorldName x y z\""});
                    }

                }

                if (config.contains("quests." + questName + ".region")) {

                    String region = config.getString("quests." + questName + ".region");
                    boolean exists = regionFound(quest, region);

                    if (!exists) {
                        skipQuestProcess("region: for Quest " + quest.name + " is not a valid WorldGuard region!");
                    }

                }

                if (config.contains("quests." + questName + ".gui-display")) {

                    String item = config.getString("quests." + questName + ".gui-display");
                    try {
                    	ItemStack stack = ItemUtil.readItemStack(item);

                    	if (stack != null) {
                    		quest.guiDisplay = stack;
                    	}
                    } catch (Exception e) {
                    	instance.getLogger().warning(item + " in items: GUI Display in Quest " + quest.name + "is not properly formatted!");
                    }

                }

                if (config.contains("quests." + questName + ".redo-delay")) {

                    if (config.getInt("quests." + questName + ".redo-delay", -999) != -999) {
                        quest.redoDelay = config.getInt("quests." + questName + ".redo-delay") * 1000;
                    } else {
                        skipQuestProcess("redo-delay: for Quest " + quest.name + " is not a number!");
                    }

                }

                if (config.contains("quests." + questName + ".finish-message")) {
                    quest.finished = parseString(config.getString("quests." + questName + ".finish-message"), quest);
                } else {
                    skipQuestProcess("Quest " + quest.name + " is missing finish-message:");
                }

                if (config.contains("quests." + questName + ".ask-message")) {
                    quest.description = parseString(config.getString("quests." + questName + ".ask-message"), quest);
                } else {
                    skipQuestProcess("Quest " + quest.name + " is missing ask-message:");
                }

                if (config.contains("quests." + questName + ".event")) {

                    Event evt = Event.loadEvent(config.getString("quests." + questName + ".event"), this);

                    if (evt != null) {
                        quest.initialEvent = evt;
                    } else {
                        skipQuestProcess("Initial Event in Quest " + quest.name + " failed to load.");
                    }

                }

                if (config.contains("quests." + questName + ".requirements")) {

                    loadQuestRequirements(config, questsSection);

                }

                quest.plugin = this;

                processStages(quest, config, questName); // needsSaving may be modified as a side-effect

                loadRewards(config);

                quests.add(quest);

                if (needsSaving) {
                    try {
                        config.save(file);

                    } catch (IOException e) {

                        if (debug == false) {
                            getLogger().log(Level.SEVERE, "Failed to load Quest \"" + questName + "\". Skipping.");
                        } else {
                            getLogger().log(Level.SEVERE, "Failed to load Quest \"" + questName + "\". Error log:");
                            e.printStackTrace();
                        }

                    }
                }

                if (failedToLoad == true) {
                    getLogger().log(Level.SEVERE, "Failed to load Quest \"" + questName + "\". Skipping.");
                }
            } catch (SkipQuest ex) {
                continue;
            } catch (StageFailedException ex) {
                continue;
            }
        }
    }

    private void loadRewards(FileConfiguration config) throws SkipQuest {
        if (config.contains("quests." + questName + ".rewards.items")) {

            if (Quests.checkList(config.getList("quests." + questName + ".rewards.items"), String.class)) {

                for (String item : config.getStringList("quests." + questName + ".rewards.items")) {

                    try {
                        ItemStack stack = ItemUtil.readItemStack(item);
                        if (stack != null) {
                            quest.itemRewards.add(stack);
                        }
                    } catch (Exception e) {
                        skipQuestProcess("" + item + " in items: Reward in Quest " + quest.name + " is not properly formatted!");
                    }

                }

            } else {
                skipQuestProcess("items: Reward in Quest " + quest.name + " is not a list of strings!");
            }

        }

        if (config.contains("quests." + questName + ".rewards.money")) {

            if (config.getInt("quests." + questName + ".rewards.money", -999) != -999) {
                quest.moneyReward = config.getInt("quests." + questName + ".rewards.money");
            } else {
                skipQuestProcess("money: Reward in Quest " + quest.name + " is not a number!");
            }

        }

        if (config.contains("quests." + questName + ".rewards.exp")) {

            if (config.getInt("quests." + questName + ".rewards.exp", -999) != -999) {
                quest.exp = config.getInt("quests." + questName + ".rewards.exp");
            } else {
                skipQuestProcess("exp: Reward in Quest " + quest.name + " is not a number!");
            }

        }

        if (config.contains("quests." + questName + ".rewards.commands")) {

            if (Quests.checkList(config.getList("quests." + questName + ".rewards.commands"), String.class)) {
                quest.commands.clear();
                quest.commands.addAll(config.getStringList("quests." + questName + ".rewards.commands"));
            } else {
                skipQuestProcess("commands: Reward in Quest " + quest.name + " is not a list of commands!");
            }

        }

        if (config.contains("quests." + questName + ".rewards.permissions")) {

            if (Quests.checkList(config.getList("quests." + questName + ".rewards.permissions"), String.class)) {
                quest.permissions.clear();
                quest.permissions.addAll(config.getStringList("quests." + questName + ".rewards.permissions"));
            } else {
                skipQuestProcess("permissions: Reward in Quest " + quest.name + " is not a list of permissions!");
            }

        }

        if (config.contains("quests." + questName + ".rewards.quest-points")) {

            if (config.getInt("quests." + questName + ".rewards.quest-points", -999) != -999) {
                quest.questPoints = config.getInt("quests." + questName + ".rewards.quest-points");
                totalQuestPoints += quest.questPoints;
            } else {
                skipQuestProcess("quest-points: Reward in Quest " + quest.name + " is not a number!");
            }

        }

        if (config.contains("quests." + questName + ".rewards.mcmmo-skills")) {

            if (Quests.checkList(config.getList("quests." + questName + ".rewards.mcmmo-skills"), String.class)) {

                if (config.contains("quests." + questName + ".rewards.mcmmo-levels")) {

                    if (Quests.checkList(config.getList("quests." + questName + ".rewards.mcmmo-levels"), Integer.class)) {

                        for (String skill : config.getStringList("quests." + questName + ".rewards.mcmmo-skills")) {

                            if (Quests.getMcMMOSkill(skill) == null) {
                                skipQuestProcess("" + skill + " in mcmmo-skills: Reward in Quest " + quest.name + " is not a valid mcMMO skill name!");
                            }

                        }

                        quest.mcmmoSkills.clear();
                        quest.mcmmoAmounts.clear();

                        quest.mcmmoSkills.addAll(config.getStringList("quests." + questName + ".rewards.mcmmo-skills"));
                        quest.mcmmoAmounts.addAll(config.getIntegerList("quests." + questName + ".rewards.mcmmo-levels"));

                    } else {
                        skipQuestProcess("mcmmo-levels: Reward in Quest " + quest.name + " is not a list of numbers!");
                    }

                } else {
                    skipQuestProcess("Rewards for Quest " + quest.name + " is missing mcmmo-levels:");
                }

            } else {
                skipQuestProcess("mcmmo-skills: Reward in Quest " + quest.name + " is not a list of mcMMO skill names!");
            }
        }

        if (config.contains("quests." + questName + ".rewards.heroes-exp-classes")) {

            if (Quests.checkList(config.getList("quests." + questName + ".rewards.heroes-exp-classes"), String.class)) {

                if (config.contains("quests." + questName + ".rewards.heroes-exp-amounts")) {

                    if (Quests.checkList(config.getList("quests." + questName + ".rewards.heroes-exp-amounts"), Double.class)) {

                        for (String heroClass : config.getStringList("quests." + questName + ".rewards.heroes-exp-classes")) {

                            if (Quests.heroes.getClassManager().getClass(heroClass) == null) {
                                skipQuestProcess("" + heroClass + " in heroes-exp-classes: Reward in Quest " + quest.name + " is not a valid Heroes class name!");
                            }

                        }

                        quest.heroesClasses.clear();
                        quest.heroesAmounts.clear();

                        quest.heroesClasses.addAll(config.getStringList("quests." + questName + ".rewards.heroes-exp-classes"));
                        quest.heroesAmounts.addAll(config.getDoubleList("quests." + questName + ".rewards.heroes-exp-amounts"));

                    } else {
                        skipQuestProcess("heroes-exp-amounts: Reward in Quest " + quest.name + " is not a list of experience amounts (decimal numbers)!");
                    }

                } else {
                    skipQuestProcess("Rewards for Quest " + quest.name + " is missing heroes-exp-amounts:");
                }

            } else {
                skipQuestProcess("heroes-exp-classes: Reward in Quest " + quest.name + " is not a list of Heroes classes!");
            }
        }

        if (getServer().getPluginManager().getPlugin("PhatLoots") != null) {
        if (config.contains("quests." + questName + ".rewards.phat-loots")) {

            if (Quests.checkList(config.getList("quests." + questName + ".rewards.phat-loots"), String.class)) {

                for (String loot : config.getStringList("quests." + questName + ".rewards.phat-loots")) {

                	if (PhatLootsAPI.getPhatLoot(loot) == null) {

                		skipQuestProcess("" + loot + " in phat-loots: Reward in Quest " + quest.name + " is not a valid PhatLoot name!");

                	}

                }

                quest.phatLootRewards.clear();
                quest.phatLootRewards.addAll(config.getStringList("quests." + questName + ".rewards.phat-loots"));

            } else {
                skipQuestProcess("phat-loots: Reward in Quest " + quest.name + " is not a list of PhatLoots!");
            }
        }
        }

        if (config.contains("quests." + questName + ".rewards.custom-rewards")) {
            populateCustomRewards(config);
        }
    }

    private void loadQuestRequirements(FileConfiguration config, ConfigurationSection questsSection) throws SkipQuest {
        if (config.contains("quests." + questName + ".requirements.fail-requirement-message")) {
            quest.failRequirements = parseString(config.getString("quests." + questName + ".requirements.fail-requirement-message"), quest);
        } else {
            skipQuestProcess("Requirements for Quest " + quest.name + " is missing fail-requirement-message:");
        }

        if (config.contains("quests." + questName + ".requirements.items")) {

            if (Quests.checkList(config.getList("quests." + questName + ".requirements.items"), String.class)) {
                List<String> itemReqs = config.getStringList("quests." + questName + ".requirements.items");
                boolean failed = false;
                for (String item : itemReqs) {

                    ItemStack stack = ItemUtil.readItemStack(item);
                    if (stack != null) {
                        quest.items.add(stack);
                    } else {
                        failed = true;
                        break;
                    }

                }

                if (failed == true) {
                    skipQuestProcess("items: Requirement for Quest " + quest.name + " is not formatted correctly!");
                }

            } else {
                skipQuestProcess("items: Requirement for Quest " + quest.name + " is not formatted correctly!");
            }

            if (config.contains("quests." + questName + ".requirements.remove-items")) {

                if (Quests.checkList(config.getList("quests." + questName + ".requirements.remove-items"), Boolean.class)) {
                    quest.removeItems.clear();
                    quest.removeItems.addAll(config.getBooleanList("quests." + questName + ".requirements.remove-items"));
                } else {
                    skipQuestProcess("remove-items: Requirement for Quest " + quest.name + " is not a list of true/false values!");
                }

            } else {
                skipQuestProcess("Requirements for Quest " + quest.name + " is missing remove-items:");
            }
        }

        if (config.contains("quests." + questName + ".requirements.money")) {

            if (config.getInt("quests." + questName + ".requirements.money", -999) != -999) {
                quest.moneyReq = config.getInt("quests." + questName + ".requirements.money");
            } else {
                skipQuestProcess("money: Requirement for Quest " + quest.name + " is not a number!");
            }

        }

        if (config.contains("quests." + questName + ".requirements.quest-points")) {

            if (config.getInt("quests." + questName + ".requirements.quest-points", -999) != -999) {
                quest.questPointsReq = config.getInt("quests." + questName + ".requirements.quest-points");
            } else {
                skipQuestProcess("quest-points: Requirement for Quest " + quest.name + " is not a number!");
            }

        }

        if (config.contains("quests." + questName + ".requirements.quest-blocks")) {

            if (Quests.checkList(config.getList("quests." + questName + ".requirements.quest-blocks"), String.class)) {

                List<String> names = config.getStringList("quests." + questName + ".requirements.quest-blocks");

                boolean failed = false;
                String failedQuest = "NULL";

                for (String name : names) {

                    boolean done = false;
                    for (String string : questsSection.getKeys(false)) {

                        if (config.getString("quests." + string + ".name").equalsIgnoreCase(name)) {
                            quest.blockQuests.add(name);
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

                if (failed) {
                    skipQuestProcess(new String[]{
                        "" + PINK + failedQuest + " inside quests: Requirement for Quest " + quest.name + " is not a valid Quest name!",
                        "Make sure you are using the Quest name: value, and not the block name."});
                }

            } else {
                skipQuestProcess("quest-blocks: Requirement for Quest " + quest.name + " is not a list of Quest names!");
            }

        }

        if (config.contains("quests." + questName + ".requirements.quests")) {

            if (Quests.checkList(config.getList("quests." + questName + ".requirements.quests"), String.class)) {

                List<String> names = config.getStringList("quests." + questName + ".requirements.quests");

                boolean failed = false;
                String failedQuest = "NULL";

                for (String name : names) {

                    boolean done = false;
                    for (String string : questsSection.getKeys(false)) {

                        if (config.getString("quests." + string + ".name").equalsIgnoreCase(name)) {
                            quest.neededQuests.add(name);
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

                if (failed) {
                    skipQuestProcess(new String[]{
                        "" + failedQuest + " inside quests: Requirement for Quest " + quest.name + " is not a valid Quest name!",
                        "Make sure you are using the Quest name: value, and not the block name."});
                }

            } else {
                skipQuestProcess("quests: Requirement for Quest " + quest.name + " is not a list of Quest names!");
            }

        }

        if (config.contains("quests." + questName + ".requirements.permissions")) {

            if (Quests.checkList(config.getList("quests." + questName + ".requirements.permissions"), String.class)) {
                quest.permissionReqs.clear();
                quest.permissionReqs.addAll(config.getStringList("quests." + questName + ".requirements.permissions"));
            } else {
                skipQuestProcess("permissions: Requirement for Quest " + quest.name + " is not a list of permissions!");
            }

        }

        if (config.contains("quests." + questName + ".requirements.mcmmo-skills")) {

            if (Quests.checkList(config.getList("quests." + questName + ".requirements.mcmmo-skills"), String.class)) {

                if (config.contains("quests." + questName + ".requirements.mcmmo-amounts")) {

                    if (Quests.checkList(config.getList("quests." + questName + ".requirements.mcmmo-amounts"), Integer.class)) {

                        List<String> skills = config.getStringList("quests." + questName + ".requirements.mcmmo-skills");
                        List<Integer> amounts = config.getIntegerList("quests." + questName + ".requirements.mcmmo-amounts");

                        if (skills.size() != amounts.size()) {
                            skipQuestProcess("mcmmo-skills: and mcmmo-amounts: in requirements: for Quest " + quest.name + " are not the same size!");
                        }

                        quest.mcMMOSkillReqs.addAll(skills);
                        quest.mcMMOAmountReqs.addAll(amounts);

                    } else {
                        skipQuestProcess("mcmmo-amounts: Requirement for Quest " + quest.name + " is not a list of numbers!");
                    }

                } else {
                    skipQuestProcess("Requirements for Quest " + quest.name + " is missing mcmmo-amounts:");
                }

            } else {
                skipQuestProcess("mcmmo-skills: Requirement for Quest " + quest.name + " is not a list of skills!");
            }

        }

        if (config.contains("quests." + questName + ".requirements.heroes-primary-class")) {

            String className = config.getString("quests." + questName + ".requirements.heroes-primary-class");
            HeroClass hc = heroes.getClassManager().getClass(className);
            if (hc != null && hc.isPrimary()) {
                quest.heroesPrimaryClassReq = hc.getName();
            } else if (hc != null) {
                skipQuestProcess("heroes-primary-class: Requirement for Quest " + quest.name + " is not a primary Heroes class!");
            } else {
                skipQuestProcess("heroes-primary-class: Requirement for Quest " + quest.name + " is not a valid Heroes class!");
            }

        }

        if (config.contains("quests." + questName + ".requirements.heroes-secondary-class")) {

            String className = config.getString("quests." + questName + ".requirements.heroes-secondary-class");
            HeroClass hc = heroes.getClassManager().getClass(className);
            if (hc != null && hc.isSecondary()) {
                quest.heroesSecondaryClassReq = hc.getName();
            } else if (hc != null) {
                skipQuestProcess("heroes-secondary-class: Requirement for Quest " + quest.name + " is not a secondary Heroes class!");
            } else {
                skipQuestProcess("heroes-secondary-class: Requirement for Quest " + quest.name + " is not a valid Heroes class!");
            }

        }

        if (config.contains("quests." + questName + ".requirements.custom-requirements")) {

            ConfigurationSection sec = config.getConfigurationSection("quests." + questName + ".requirements.custom-requirements");
            for (String path : sec.getKeys(false)) {

                String name = sec.getString(path + ".name");
                boolean found = false;

                for (CustomRequirement cr : customRequirements) {
                    if (cr.getName().equalsIgnoreCase(name)) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                	getLogger().warning("Custom requirement \"" + name + "\" for Quest \"" + quest.name + "\" could not be found!");
                    skipQuestProcess((String) null); // null bc we warn, not severe for this one
                }

                Map<String, Object> data = new HashMap<String, Object>();
                ConfigurationSection sec2 = sec.getConfigurationSection(path + ".data");
                if (sec2 != null) {
                    for (String dataPath : sec2.getKeys(false)) {
                        data.put(dataPath, sec2.get(dataPath));
                    }
                }

                quest.customRequirements.put(name, data);

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
        skipQuestProcess(new String[]{msg});
    }

    private void populateCustomRewards(FileConfiguration config) {
        ConfigurationSection sec = config.getConfigurationSection("quests." + questName + ".rewards.custom-rewards");
        for (String path : sec.getKeys(false)) {

            String name = sec.getString(path + ".name");
            boolean found = false;

            for (CustomReward cr : customRewards) {
                if (cr.getName().equalsIgnoreCase(name)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
            	getLogger().warning("Custom reward \"" + name + "\" for Quest \"" + quest.name + "\" could not be found!");
                continue;
            }

            Map<String, Object> data = new HashMap<String, Object>();
            ConfigurationSection sec2 = sec.getConfigurationSection(path + ".data");
            if (sec2 != null) {
                for (String dataPath : sec2.getKeys(false)) {
                    data.put(dataPath, sec2.get(dataPath));
                }
            }

            quest.customRewards.put(name, data);
        }
    }

    private boolean regionFound(Quest quest, String region) {
        boolean exists = false;

        for (World world : getServer().getWorlds()) {

            RegionManager rm = worldGuard.getRegionManager(world);
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

    private void processStages(Quest quest, FileConfiguration config, String questName) throws StageFailedException {
        int index = 1;

        ConfigurationSection questStages = config.getConfigurationSection("quests." + questName + ".stages.ordered");

        for (String s2 : questStages.getKeys(false)) {

            Stage oStage = new Stage();

            LinkedList<EntityType> mobsToKill = new LinkedList<EntityType>();
            LinkedList<Integer> mobNumToKill = new LinkedList<Integer>();
            LinkedList<Location> locationsToKillWithin = new LinkedList<Location>();
            LinkedList<Integer> radiiToKillWithin = new LinkedList<Integer>();
            LinkedList<String> areaNames = new LinkedList<String>();

            LinkedList<Enchantment> enchantments = new LinkedList<Enchantment>();
            LinkedList<Material> itemsToEnchant = new LinkedList<Material>();
            List<Integer> amountsToEnchant = new LinkedList<Integer>();

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

            //Denizen script load
            if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".script-to-run")) {

                if (ScriptRegistry.containsScript(config.getString("quests." + questName + ".stages.ordered." + s2 + ".script-to-run"))) {
                    trigger = new QuestTaskTrigger();
                    oStage.script = config.getString("quests." + questName + ".stages.ordered." + s2 + ".script-to-run");
                } else {
                    stageFailed("script-to-run: in Stage " + s2 + " of Quest " + quest.name + " is not a Denizen script!");
                }

            }

            if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".break-block-names")) {

                if (checkList(config.getList("quests." + questName + ".stages.ordered." + s2 + ".break-block-names"), String.class)) {
					breaknames = config.getStringList("quests." + questName + ".stages.ordered." + s2 + ".break-block-names");
					
                } else {
                    stageFailed("break-block-names: in Stage " + s2 + " of Quest " + quest.name + " is not a list of strings!");
                }

                if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".break-block-amounts")) {

                    if (checkList(config.getList("quests." + questName + ".stages.ordered." + s2 + ".break-block-amounts"), Integer.class)) {
                        breakamounts = config.getIntegerList("quests." + questName + ".stages.ordered." + s2 + ".break-block-amounts");
                    } else {
                        stageFailed("break-block-amounts: in Stage " + s2 + " of Quest " + quest.name + " is not a list of numbers!");
                    }

                } else {
                    stageFailed("Stage " + s2 + " of Quest " + quest.name + " is missing break-block-amounts:");
                }
                
                if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".break-block-durability")) {

                    if (checkList(config.getList("quests." + questName + ".stages.ordered." + s2 + ".break-block-durability"), Integer.class)) {
                        breakdurability = config.getShortList("quests." + questName + ".stages.ordered." + s2 + ".break-block-durability");
                    } else {
                        stageFailed("break-block-durability: in Stage " + s2 + " of Quest " + quest.name + " is not a list of numbers!");
                    }

                } else {
                    stageFailed("Stage " + s2 + " of Quest " + quest.name + " is missing break-block-durability:");
                }

            }
            
            for (String s : breaknames) {
            	ItemStack is;
            	if (breakdurability.get(breaknames.indexOf(s)) != -1) {
            		is = new ItemStack(Material.matchMaterial(s), breakamounts.get(breaknames.indexOf(s)), breakdurability.get(breaknames.indexOf(s)));
            	} else {
            		//Legacy
            		is = new ItemStack(Material.matchMaterial(s), breakamounts.get(breaknames.indexOf(s)), (short) 0);
            		
            	}
            	if (Material.matchMaterial(s) != null) {
            		oStage.blocksToBreak.add(is);
            	} else {
            		stageFailed("" + s + " inside break-block-names: inside Stage " + s2 + " of Quest " + quest.name + " is not a valid item name!");
            	}
            }

            if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".damage-block-names")) {

                if (checkList(config.getList("quests." + questName + ".stages.ordered." + s2 + ".damage-block-names"), String.class)) {
                    damagenames = config.getStringList("quests." + questName + ".stages.ordered." + s2 + ".damage-block-names");
                } else {
                    stageFailed("damage-block-names: in Stage " + s2 + " of Quest " + quest.name + " is not a list of strings!");
                }

                if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".damage-block-amounts")) {

                    if (checkList(config.getList("quests." + questName + ".stages.ordered." + s2 + ".damage-block-amounts"), Integer.class)) {
                        damageamounts = config.getIntegerList("quests." + questName + ".stages.ordered." + s2 + ".damage-block-amounts");
                    } else {
                        stageFailed("damage-block-amounts: in Stage " + s2 + " of Quest " + quest.name + " is not a list of numbers!");
                    }

                } else {
                    stageFailed("Stage " + s2 + " of Quest " + quest.name + " is missing damage-block-amounts:");
                }
                
                if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".damage-block-durability")) {

                    if (checkList(config.getList("quests." + questName + ".stages.ordered." + s2 + ".damage-block-durability"), Integer.class)) {
                        damagedurability = config.getShortList("quests." + questName + ".stages.ordered." + s2 + ".damage-block-durability");
                    } else {
                        stageFailed("damage-block-durability: in Stage " + s2 + " of Quest " + quest.name + " is not a list of numbers!");
                    }

                } else {
                    stageFailed("Stage " + s2 + " of Quest " + quest.name + " is missing damage-block-durability:");
                }

            }
            
            for (String s : damagenames) {
            	ItemStack is;
            	if (damagedurability.get(damagenames.indexOf(s)) != -1) {
            		is = new ItemStack(Material.matchMaterial(s), damageamounts.get(damagenames.indexOf(s)), damagedurability.get(damagenames.indexOf(s)));
            	} else {
            		//Legacy
            		is = new ItemStack(Material.matchMaterial(s), damageamounts.get(damagenames.indexOf(s)), (short) 0);
            		
            	}
            	if (Material.matchMaterial(s) != null) {
            		oStage.blocksToDamage.add(is);
            	} else {
            		stageFailed("" + s + " inside damage-block-names: inside Stage " + s2 + " of Quest " + quest.name + " is not a valid item name!");
            	}
            }

            if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".place-block-names")) {

                if (checkList(config.getList("quests." + questName + ".stages.ordered." + s2 + ".place-block-names"), String.class)) {
                    placenames = config.getStringList("quests." + questName + ".stages.ordered." + s2 + ".place-block-names");
                } else {
                    stageFailed("place-block-names: in Stage " + s2 + " of Quest " + quest.name + " is not a list of strings!");
                }

                if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".place-block-amounts")) {

                    if (checkList(config.getList("quests." + questName + ".stages.ordered." + s2 + ".place-block-amounts"), Integer.class)) {
                        placeamounts = config.getIntegerList("quests." + questName + ".stages.ordered." + s2 + ".place-block-amounts");
                    } else {
                        stageFailed("place-block-amounts: in Stage " + s2 + " of Quest " + quest.name + " is not a list of numbers!");
                    }

                } else {
                    stageFailed("Stage " + s2 + " of Quest " + quest.name + " is missing place-block-amounts:");
                }
                
                if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".place-block-durability")) {

                    if (checkList(config.getList("quests." + questName + ".stages.ordered." + s2 + ".place-block-durability"), Integer.class)) {
                        placedurability = config.getShortList("quests." + questName + ".stages.ordered." + s2 + ".place-block-durability");
                    } else {
                        stageFailed("place-block-durability: in Stage " + s2 + " of Quest " + quest.name + " is not a list of numbers!");
                    }

                } else {
                    stageFailed("Stage " + s2 + " of Quest " + quest.name + " is missing place-block-durability:");
                }

            }

            for (String s : placenames) {
            	ItemStack is;
            	if (placedurability.get(placenames.indexOf(s)) != -1) {
            		is = new ItemStack(Material.matchMaterial(s), placeamounts.get(placenames.indexOf(s)), placedurability.get(placenames.indexOf(s)));
            	} else {
            		//Legacy
            		is = new ItemStack(Material.matchMaterial(s), placeamounts.get(placenames.indexOf(s)), (short) 0);
            		
            	}
            	if (Material.matchMaterial(s) != null) {
            		oStage.blocksToPlace.add(is);
            	} else {
            		stageFailed("" + s + " inside place-block-names: inside Stage " + s2 + " of Quest " + quest.name + " is not a valid item name!");
            	}
            }

            if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".use-block-names")) {

                if (checkList(config.getList("quests." + questName + ".stages.ordered." + s2 + ".use-block-names"), String.class)) {
                    usenames = config.getStringList("quests." + questName + ".stages.ordered." + s2 + ".use-block-names");
                } else {
                    stageFailed("use-block-names: in Stage " + s2 + " of Quest " + quest.name + " is not a list of strings!");
                }

                if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".use-block-amounts")) {

                    if (checkList(config.getList("quests." + questName + ".stages.ordered." + s2 + ".use-block-amounts"), Integer.class)) {
                        useamounts = config.getIntegerList("quests." + questName + ".stages.ordered." + s2 + ".use-block-amounts");
                    } else {
                        stageFailed("use-block-amounts: in Stage " + s2 + " of Quest " + quest.name + " is not a list of numbers!");
                    }

                } else {
                    stageFailed("Stage " + s2 + " of Quest " + quest.name + " is missing use-block-amounts:");
                }
                
                if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".use-block-durability")) {

                    if (checkList(config.getList("quests." + questName + ".stages.ordered." + s2 + ".use-block-durability"), Integer.class)) {
                        usedurability = config.getShortList("quests." + questName + ".stages.ordered." + s2 + ".use-block-durability");
                    } else {
                        stageFailed("use-block-durability: in Stage " + s2 + " of Quest " + quest.name + " is not a list of numbers!");
                    }

                } else {
                    stageFailed("Stage " + s2 + " of Quest " + quest.name + " is missing use-block-durability:");
                }

            }

            for (String s : usenames) {
            	ItemStack is;
            	if (usedurability.get(usenames.indexOf(s)) != -1) {
            		is = new ItemStack(Material.matchMaterial(s), useamounts.get(usenames.indexOf(s)), usedurability.get(usenames.indexOf(s)));
            	} else {
            		//Legacy
            		is = new ItemStack(Material.matchMaterial(s), useamounts.get(usenames.indexOf(s)), (short) 0);
            		
            	}
            	if (Material.matchMaterial(s) != null) {
            		oStage.blocksToUse.add(is);
            	} else {
            		stageFailed("" + s + " inside use-block-names: inside Stage " + s2 + " of Quest " + quest.name + " is not a valid item name!");
            	}
            }

            if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".cut-block-names")) {

                if (checkList(config.getList("quests." + questName + ".stages.ordered." + s2 + ".cut-block-names"), String.class)) {
                    cutnames = config.getStringList("quests." + questName + ".stages.ordered." + s2 + ".cut-block-names");
                } else {
                    stageFailed("cut-block-names: in Stage " + s2 + " of Quest " + quest.name + " is not a list of strings!");
                }

                if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".cut-block-amounts")) {

                    if (checkList(config.getList("quests." + questName + ".stages.ordered." + s2 + ".cut-block-amounts"), Integer.class)) {
                        cutamounts = config.getIntegerList("quests." + questName + ".stages.ordered." + s2 + ".cut-block-amounts");
                    } else {
                        stageFailed("cut-block-amounts: in Stage " + s2 + " of Quest " + quest.name + " is not a list of numbers!");
                    }

                } else {
                    stageFailed("Stage " + s2 + " of Quest " + quest.name + " is missing cut-block-amounts:");
                }
                
                if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".cut-block-durability")) {

                    if (checkList(config.getList("quests." + questName + ".stages.ordered." + s2 + ".cut-block-durability"), Integer.class)) {
                        cutdurability = config.getShortList("quests." + questName + ".stages.ordered." + s2 + ".cut-block-durability");
                    } else {
                        stageFailed("cut-block-durability: in Stage " + s2 + " of Quest " + quest.name + " is not a list of numbers!");
                    }

                } else {
                    stageFailed("Stage " + s2 + " of Quest " + quest.name + " is missing cut-block-durability:");
                }

            }

            for (String s : cutnames) {
            	ItemStack is;
            	if (cutdurability.get(cutnames.indexOf(s)) != -1) {
            		is = new ItemStack(Material.matchMaterial(s), cutamounts.get(cutnames.indexOf(s)), cutdurability.get(cutnames.indexOf(s)));
            	} else {
            		//Legacy
            		is = new ItemStack(Material.matchMaterial(s), cutamounts.get(cutnames.indexOf(s)), (short) 0);
            		
            	}
            	if (Material.matchMaterial(s) != null) {
            		oStage.blocksToCut.add(is);
            	} else {
            		stageFailed("" + s + " inside cut-block-names: inside Stage " + s2 + " of Quest " + quest.name + " is not a valid item name!");
            	}
            }

            if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".fish-to-catch")) {

                if (config.getInt("quests." + questName + ".stages.ordered." + s2 + ".fish-to-catch", -999) != -999) {
                    oStage.fishToCatch = config.getInt("quests." + questName + ".stages.ordered." + s2 + ".fish-to-catch");
                } else {
                    stageFailed("fish-to-catch: inside Stage " + s2 + " of Quest " + quest.name + " is not a number!");
                }

            }

            if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".players-to-kill")) {

                if (config.getInt("quests." + questName + ".stages.ordered." + s2 + ".players-to-kill", -999) != -999) {
                    oStage.playersToKill = config.getInt("quests." + questName + ".stages.ordered." + s2 + ".players-to-kill");
                } else {
                    stageFailed("players-to-kill: inside Stage " + s2 + " of Quest " + quest.name + " is not a number!");
                }

            }

            if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".enchantments")) {

                if (Quests.checkList(config.getList("quests." + questName + ".stages.ordered." + s2 + ".enchantments"), String.class)) {

                    for (String enchant : config.getStringList("quests." + questName + ".stages.ordered." + s2 + ".enchantments")) {

                        Enchantment e = Quests.getEnchantment(enchant);

                        if (e != null) {

                            enchantments.add(e);

                        } else {

                            stageFailed("" + enchant + " inside enchantments: inside Stage " + s2 + " of Quest " + quest.name + " is not a valid enchantment!");

                        }

                    }

                } else {
                    stageFailed("enchantments: in Stage " + s2 + " of Quest " + quest.name + " is not a list of enchantment names!");
                }

                if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".enchantment-item-names")) {

                    if (Quests.checkList(config.getList("quests." + questName + ".stages.ordered." + s2 + ".enchantment-item-names"), Integer.class)) {

                        for (String item : config.getStringList("quests." + questName + ".stages.ordered." + s2 + ".enchantment-item-names")) {

                            if (Material.matchMaterial(item) != null) {
                                itemsToEnchant.add(Material.matchMaterial(item));
                            } else {
                                stageFailed("" + item + " inside enchantment-item-names: inside Stage " + s2 + " of Quest " + quest.name + " is not a valid item name!");
                            }

                        }

                    } else {

                        stageFailed("enchantment-item-names: in Stage " + s2 + " of Quest " + quest.name + " is not a list of numbers!");

                    }

                } else {
                    stageFailed("Stage " + s2 + " of Quest " + quest.name + " is missing enchantment-item-names:");
                }

                if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".enchantment-amounts")) {

                    if (Quests.checkList(config.getList("quests." + questName + ".stages.ordered." + s2 + ".enchantment-amounts"), Integer.class)) {

                        amountsToEnchant = config.getIntegerList("quests." + questName + ".stages.ordered." + s2 + ".enchantment-amounts");

                    } else {

                        stageFailed("enchantment-amounts: in Stage " + s2 + " of Quest " + quest.name + " is not a list of numbers!");

                    }

                } else {
                    stageFailed("Stage " + s2 + " of Quest " + quest.name + " is missing enchantment-amounts:");
                }

            }

            List<Integer> npcIdsToTalkTo = null;

            if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".npc-ids-to-talk-to")) {

                if (checkList(config.getList("quests." + questName + ".stages.ordered." + s2 + ".npc-ids-to-talk-to"), Integer.class)) {

                    npcIdsToTalkTo = config.getIntegerList("quests." + questName + ".stages.ordered." + s2 + ".npc-ids-to-talk-to");
                    for (int i : npcIdsToTalkTo) {

                        if (CitizensAPI.getNPCRegistry().getById(i) != null) {

                            questNPCs.add(CitizensAPI.getNPCRegistry().getById(i));

                        } else {
                            stageFailed("" + i + " inside npc-ids-to-talk-to: inside Stage " + s2 + " of Quest " + quest.name + " is not a valid NPC id!");
                        }

                    }

                } else {
                    stageFailed("npc-ids-to-talk-to: in Stage " + s2 + " of Quest " + quest.name + " is not a list of numbers!");
                }

            }

            List<String> itemsToDeliver;
            List<Integer> itemDeliveryTargetIds;
            LinkedList<String> deliveryMessages = new LinkedList<String>();

            if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".items-to-deliver")) {

                if (checkList(config.getList("quests." + questName + ".stages.ordered." + s2 + ".items-to-deliver"), String.class)) {

                    if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".npc-delivery-ids")) {

                        if (checkList(config.getList("quests." + questName + ".stages.ordered." + s2 + ".npc-delivery-ids"), Integer.class)) {

                            if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".delivery-messages")) {

                                itemsToDeliver = config.getStringList("quests." + questName + ".stages.ordered." + s2 + ".items-to-deliver");
                                itemDeliveryTargetIds = config.getIntegerList("quests." + questName + ".stages.ordered." + s2 + ".npc-delivery-ids");
                                deliveryMessages.addAll(config.getStringList("quests." + questName + ".stages.ordered." + s2 + ".delivery-messages"));

                                for (String item : itemsToDeliver) {

                                    ItemStack is = ItemUtil.readItemStack("" + item);

                                    if (is != null) {

                                        int npcId = itemDeliveryTargetIds.get(itemsToDeliver.indexOf(item));
                                        NPC npc = CitizensAPI.getNPCRegistry().getById(npcId);

                                        if (npc != null) {

                                            oStage.itemsToDeliver.add(is);
                                            oStage.itemDeliveryTargets.add(npcId);
                                            oStage.deliverMessages = deliveryMessages;

                                        } else {
                                            stageFailed("" + npcId + " inside npc-delivery-ids: inside Stage " + s2 + " of Quest " + quest.name + " is not a valid NPC id!");
                                        }

                                    } else {
                                        stageFailed("" + item + " inside items-to-deliver: inside Stage " + s2 + " of Quest " + quest.name + " is not formatted properly!");
                                    }

                                }

                            } else {
                                stageFailed("Stage " + s2 + " of Quest " + quest.name + " is missing delivery-messages:");
                            }

                        } else {
                            stageFailed("npc-delivery-ids: in Stage " + s2 + " of Quest " + PURPLE + quest.name + " is not a list of NPC ids!");
                        }

                    } else {
                        stageFailed("Stage " + s2 + " of Quest " + quest.name + " is missing npc-delivery-ids:");
                    }

                } else {
                    stageFailed("items-to-deliver: in Stage " + s2 + " of Quest " + quest.name + " is not formatted properly!");
                }

            }

            List<Integer> npcIds;
            List<Integer> npcAmounts;

            if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".npc-ids-to-kill")) {

                if (checkList(config.getList("quests." + questName + ".stages.ordered." + s2 + ".npc-ids-to-kill"), Integer.class)) {

                    if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".npc-kill-amounts")) {

                        if (checkList(config.getList("quests." + questName + ".stages.ordered." + s2 + ".npc-kill-amounts"), Integer.class)) {

                            npcIds = config.getIntegerList("quests." + questName + ".stages.ordered." + s2 + ".npc-ids-to-kill");
                            npcAmounts = config.getIntegerList("quests." + questName + ".stages.ordered." + s2 + ".npc-kill-amounts");
                            for (int i : npcIds) {

                                if (CitizensAPI.getNPCRegistry().getById(i) != null) {

                                    if (npcAmounts.get(npcIds.indexOf(i)) > 0) {
                                        oStage.citizensToKill.add(i);
                                        oStage.citizenNumToKill.add(npcAmounts.get(npcIds.indexOf(i)));
                                        questNPCs.add(CitizensAPI.getNPCRegistry().getById(i));
                                    } else {
                                        stageFailed("" + npcAmounts.get(npcIds.indexOf(i)) + " inside npc-kill-amounts: inside Stage " + s2 + " of Quest " + quest.name + " is not a positive number!");
                                    }

                                } else {
                                    stageFailed("" + i + " inside npc-ids-to-kill: inside Stage " + s2 + " of Quest " + quest.name + " is not a valid NPC id!");
                                }

                            }

                        } else {
                            stageFailed("npc-kill-amounts: in Stage " + s2 + " of Quest " + quest.name + " is not a list of numbers!");
                        }

                    } else {
                        stageFailed("Stage " + s2 + " of Quest " + quest.name + " is missing npc-kill-amounts:");
                    }

                } else {
                    stageFailed("npc-ids-to-kill: in Stage " + s2 + " of Quest " + quest.name + " is not a list of numbers!");
                }

            }

            if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".mobs-to-kill")) {

                if (Quests.checkList(config.getList("quests." + questName + ".stages.ordered." + s2 + ".mobs-to-kill"), String.class)) {

                    List<String> mobNames = config.getStringList("quests." + questName + ".stages.ordered." + s2 + ".mobs-to-kill");
                    for (String mob : mobNames) {

                        EntityType type = getMobType(mob);

                        if (type != null) {

                            mobsToKill.add(type);

                        } else {

                            stageFailed("" + mob + " inside mobs-to-kill: inside Stage " + s2 + " of Quest " + quest.name + " is not a valid mob name!");

                        }

                    }

                } else {
                    stageFailed("mobs-to-kill: in Stage " + s2 + " of Quest " + quest.name + " is not a list of mob names!");
                }

                if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".mob-amounts")) {

                    if (Quests.checkList(config.getList("quests." + questName + ".stages.ordered." + s2 + ".mob-amounts"), Integer.class)) {

                        for (int i : config.getIntegerList("quests." + questName + ".stages.ordered." + s2 + ".mob-amounts")) {

                            mobNumToKill.add(i);

                        }

                    } else {

                        stageFailed("mob-amounts: in Stage " + s2 + " of Quest " + quest.name + " is not a list of numbers!");

                    }

                } else {
                    stageFailed("Stage " + s2 + " of Quest " + PURPLE + quest.name + " is missing mob-amounts:");
                }

            }

            if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".locations-to-kill")) {

                if (Quests.checkList(config.getList("quests." + questName + ".stages.ordered." + s2 + ".locations-to-kill"), String.class)) {

                    List<String> locations = config.getStringList("quests." + questName + ".stages.ordered." + s2 + ".locations-to-kill");

                    for (String loc : locations) {

                        String[] info = loc.split(" ");
                        if (info.length == 4) {
                            double x = 0;
                            double y = 0;
                            double z = 0;
                            try {
                                x = Double.parseDouble(info[1]);
                                y = Double.parseDouble(info[2]);
                                z = Double.parseDouble(info[3]);
                            } catch (NumberFormatException e) {
                                stageFailed(new String[]{
                                    "" + loc + " inside mobs-to-kill: inside Stage " + s2 + " of Quest " + quest.name + " is not in proper location format!",
                                    "Proper location format is: \"WorldName x y z\""});
                            }

                            if (getServer().getWorld(info[0]) != null) {
                                Location finalLocation = new Location(getServer().getWorld(info[0]), x, y, z);
                                locationsToKillWithin.add(finalLocation);
                            } else {
                                stageFailed("" + info[0] + " inside mobs-to-kill: inside Stage " + s2 + " of Quest " + quest.name + " is not a valid world name!");
                            }

                        } else {
                            stageFailed(new String[]{
                                "" + loc + " inside mobs-to-kill: inside Stage " + s2 + " of Quest " + quest.name + " is not in proper location format!",
                                "Proper location format is: \"WorldName x y z\""});
                        }

                    }

                } else {
                    stageFailed("locations-to-kill: in Stage " + s2 + " of Quest " + quest.name + " is not a list of locations!");
                }

                if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".kill-location-radii")) {

                    if (Quests.checkList(config.getList("quests." + questName + ".stages.ordered." + s2 + ".kill-location-radii"), Integer.class)) {

                        List<Integer> radii = config.getIntegerList("quests." + questName + ".stages.ordered." + s2 + ".kill-location-radii");
                        for (int i : radii) {

                            radiiToKillWithin.add(i);

                        }

                    } else {
                        stageFailed("kill-location-radii: in Stage " + s2 + " of Quest " + quest.name + " is not a list of numbers!");
                    }

                } else {
                    stageFailed("Stage " + s2 + " of Quest " + quest.name + " is missing kill-location-radii:");
                }

                if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".kill-location-names")) {

                    if (Quests.checkList(config.getList("quests." + questName + ".stages.ordered." + s2 + ".kill-location-names"), String.class)) {

                        List<String> locationNames = config.getStringList("quests." + questName + ".stages.ordered." + s2 + ".kill-location-names");
                        for (String name : locationNames) {

                            areaNames.add(name);

                        }

                    } else {
                        stageFailed("kill-location-names: in Stage " + s2 + " of Quest " + quest.name + " is not a list of names!");
                    }

                } else {
                    stageFailed("Stage " + s2 + " of Quest " + quest.name + " is missing kill-location-names:");
                }

            }

            oStage.mobsToKill = mobsToKill;
            oStage.mobNumToKill = mobNumToKill;
            oStage.locationsToKillWithin = locationsToKillWithin;
            oStage.radiiToKillWithin = radiiToKillWithin;
            oStage.areaNames = areaNames;

            Map<Map<Enchantment, Material>, Integer> enchants = new HashMap<Map<Enchantment, Material>, Integer>();

            for (Enchantment e : enchantments) {

                Map<Enchantment, Material> map = new HashMap<Enchantment, Material>();
                map.put(e, itemsToEnchant.get(enchantments.indexOf(e)));
                enchants.put(map, amountsToEnchant.get(enchantments.indexOf(e)));

            }

            oStage.itemsToEnchant = enchants;

            if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".locations-to-reach")) {

                if (Quests.checkList(config.getList("quests." + questName + ".stages.ordered." + s2 + ".locations-to-reach"), String.class)) {

                    List<String> locations = config.getStringList("quests." + questName + ".stages.ordered." + s2 + ".locations-to-reach");

                    for (String loc : locations) {

                        String[] info = loc.split(" ");
                        if (info.length == 4) {
                            double x = 0;
                            double y = 0;
                            double z = 0;
                            try {
                                x = Double.parseDouble(info[1]);
                                y = Double.parseDouble(info[2]);
                                z = Double.parseDouble(info[3]);
                            } catch (NumberFormatException e) {
                                stageFailed(new String[]{
                                    "" + loc + " inside locations-to-reach: inside Stage " + s2 + " of Quest " + quest.name + " is not in proper location format!",
                                    "Proper location format is: \"WorldName x y z\""});
                            }

                            if (getServer().getWorld(info[0]) != null) {
                                Location finalLocation = new Location(getServer().getWorld(info[0]), x, y, z);
                                oStage.locationsToReach.add(finalLocation);
                            } else {
                                stageFailed("" + info[0] + " inside locations-to-reach: inside Stage " + s2 + " of Quest " + quest.name + " is not a valid world name!");
                            }

                        } else {
                            stageFailed(new String[]{
                                "" + loc + " inside mobs-to-kill: inside Stage " + s2 + " of Quest " + quest.name + " is not in proper location format!",
                                "Proper location format is: \"WorldName x y z\""});
                        }

                    }

                } else {
                    stageFailed("locations-to-reach: in Stage " + s2 + " of Quest " + quest.name + " is not a list of locations!");
                }

                if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".reach-location-radii")) {

                    if (Quests.checkList(config.getList("quests." + questName + ".stages.ordered." + s2 + ".reach-location-radii"), Integer.class)) {

                        List<Integer> radii = config.getIntegerList("quests." + questName + ".stages.ordered." + s2 + ".reach-location-radii");
                        for (int i : radii) {

                            oStage.radiiToReachWithin.add(i);

                        }

                    } else {
                        stageFailed("reach-location-radii: in Stage " + s2 + " of Quest " + quest.name + " is not a list of numbers!");
                    }

                } else {
                    stageFailed("Stage " + s2 + " of Quest " + quest.name + " is missing reach-location-radii:");
                }

                if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".reach-location-names")) {

                    if (Quests.checkList(config.getList("quests." + questName + ".stages.ordered." + s2 + ".reach-location-names"), String.class)) {

                        List<String> locationNames = config.getStringList("quests." + questName + ".stages.ordered." + s2 + ".reach-location-names");
                        for (String name : locationNames) {

                            oStage.locationNames.add(name);

                        }

                    } else {
                        stageFailed("reach-location-names: in Stage " + s2 + " of Quest " + quest.name + " is not a list of names!");
                    }

                } else {
                    stageFailed("Stage " + s2 + " of Quest " + quest.name + " is missing reach-location-names:");
                }

            }

            if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".mobs-to-tame")) {

                if (Quests.checkList(config.getList("quests." + questName + ".stages.ordered." + s2 + ".mobs-to-tame"), String.class)) {

                    if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".mob-tame-amounts")) {

                        if (Quests.checkList(config.getList("quests." + questName + ".stages.ordered." + s2 + ".mob-tame-amounts"), Integer.class)) {

                            List<String> mobs = config.getStringList("quests." + questName + ".stages.ordered." + s2 + ".mobs-to-tame");
                            List<Integer> mobAmounts = config.getIntegerList("quests." + questName + ".stages.ordered." + s2 + ".mob-tame-amounts");

                            for (String mob : mobs) {

                                if (mob.equalsIgnoreCase("Wolf") || mob.equalsIgnoreCase("Ocelot") || mob.equalsIgnoreCase("Horse")) {

                                    oStage.mobsToTame.put(EntityType.valueOf(mob.toUpperCase()), mobAmounts.get(mobs.indexOf(mob)));

                                } else {
                                    stageFailed("" + mob + " inside mobs-to-tame: inside Stage " + s2 + " of Quest " + quest.name + " is not a valid tameable mob!");
                                }

                            }

                        } else {
                            stageFailed("mob-tame-amounts: in Stage " + s2 + " of Quest " + quest.name + " is not a list of numbers!");
                        }

                    } else {
                        stageFailed("Stage " + s2 + " of Quest " + quest.name + " is missing mob-tame-amounts:");
                    }

                } else {
                    stageFailed("mobs-to-tame: in Stage " + s2 + " of Quest " + quest.name + " is not a list of mob names!");
                }

            }

            if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".sheep-to-shear")) {

                if (Quests.checkList(config.getList("quests." + questName + ".stages.ordered." + s2 + ".sheep-to-shear"), String.class)) {

                    if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".sheep-amounts")) {

                        if (Quests.checkList(config.getList("quests." + questName + ".stages.ordered." + s2 + ".sheep-amounts"), Integer.class)) {

                            List<String> sheep = config.getStringList("quests." + questName + ".stages.ordered." + s2 + ".sheep-to-shear");
                            List<Integer> shearAmounts = config.getIntegerList("quests." + questName + ".stages.ordered." + s2 + ".sheep-amounts");

                            for (String color : sheep) {

                                if (color.equalsIgnoreCase("Black")) {

                                    oStage.sheepToShear.put(DyeColor.BLACK, shearAmounts.get(sheep.indexOf(color)));

                                } else if (color.equalsIgnoreCase("Blue")) {

                                    oStage.sheepToShear.put(DyeColor.BLUE, shearAmounts.get(sheep.indexOf(color)));

                                } else if (color.equalsIgnoreCase("Brown")) {

                                    oStage.sheepToShear.put(DyeColor.BROWN, shearAmounts.get(sheep.indexOf(color)));

                                } else if (color.equalsIgnoreCase("Cyan")) {

                                    oStage.sheepToShear.put(DyeColor.CYAN, shearAmounts.get(sheep.indexOf(color)));

                                } else if (color.equalsIgnoreCase("Gray")) {

                                    oStage.sheepToShear.put(DyeColor.GRAY, shearAmounts.get(sheep.indexOf(color)));

                                } else if (color.equalsIgnoreCase("Green")) {

                                    oStage.sheepToShear.put(DyeColor.GREEN, shearAmounts.get(sheep.indexOf(color)));

                                } else if (color.equalsIgnoreCase("LightBlue")) {

                                    oStage.sheepToShear.put(DyeColor.LIGHT_BLUE, shearAmounts.get(sheep.indexOf(color)));

                                } else if (color.equalsIgnoreCase("Lime")) {

                                    oStage.sheepToShear.put(DyeColor.LIME, shearAmounts.get(sheep.indexOf(color)));

                                } else if (color.equalsIgnoreCase("Magenta")) {

                                    oStage.sheepToShear.put(DyeColor.MAGENTA, shearAmounts.get(sheep.indexOf(color)));

                                } else if (color.equalsIgnoreCase("Orange")) {

                                    oStage.sheepToShear.put(DyeColor.ORANGE, shearAmounts.get(sheep.indexOf(color)));

                                } else if (color.equalsIgnoreCase("Pink")) {

                                    oStage.sheepToShear.put(DyeColor.PINK, shearAmounts.get(sheep.indexOf(color)));

                                } else if (color.equalsIgnoreCase("Purple")) {

                                    oStage.sheepToShear.put(DyeColor.PURPLE, shearAmounts.get(sheep.indexOf(color)));

                                } else if (color.equalsIgnoreCase("Red")) {

                                    oStage.sheepToShear.put(DyeColor.RED, shearAmounts.get(sheep.indexOf(color)));

                                } else if (color.equalsIgnoreCase("Silver")) {

                                    oStage.sheepToShear.put(DyeColor.SILVER, shearAmounts.get(sheep.indexOf(color)));

                                } else if (color.equalsIgnoreCase("White")) {

                                    oStage.sheepToShear.put(DyeColor.WHITE, shearAmounts.get(sheep.indexOf(color)));

                                } else if (color.equalsIgnoreCase("Yellow")) {

                                    oStage.sheepToShear.put(DyeColor.YELLOW, shearAmounts.get(sheep.indexOf(color)));

                                } else {

                                    stageFailed("" + color + " inside sheep-to-shear: inside Stage " + s2 + " of Quest " + quest.name + " is not a valid color!");
                                }

                            }

                        } else {
                            stageFailed("sheep-amounts: in Stage " + s2 + " of Quest " + quest.name + " is not a list of numbers!");
                        }

                    } else {
                        stageFailed("Stage " + s2 + " of Quest " + quest.name + " is missing sheep-amounts:");
                    }

                } else {
                    stageFailed("sheep-to-shear: in Stage " + s2 + " of Quest " + quest.name + " is not a list of colors!");
                }

            }

            if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".password-displays")) {

                List<String> displays = config.getStringList("quests." + questName + ".stages.ordered." + s2 + ".password-displays");

                if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".password-phrases")) {

                    List<String> phrases = config.getStringList("quests." + questName + ".stages.ordered." + s2 + ".password-phrases");
                    if (displays.size() == phrases.size()) {

                        for (int passIndex = 0; passIndex < displays.size(); passIndex++) {

                            oStage.passwordDisplays.add(displays.get(passIndex));
                            LinkedList<String> answers = new LinkedList<String>();
                            answers.addAll(Arrays.asList(phrases.get(passIndex).split("\\|")));
                            oStage.passwordPhrases.add(answers);

                        }

                    } else {
                        stageFailed("password-displays and password-phrases in Stage " + s2 + " of Quest " + quest.name + " are not the same size!");
                    }

                } else {
                    stageFailed("Stage " + s2 + " of Quest " + quest.name + " is missing password-phrases!");
                }

            }

            if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".custom-objectives")) {

                ConfigurationSection sec = config.getConfigurationSection("quests." + questName + ".stages.ordered." + s2 + ".custom-objectives");
                for (String path : sec.getKeys(false)) {

                    String name = sec.getString(path + ".name");
                    int count = sec.getInt(path + ".count");
                    CustomObjective found = null;

                    for (CustomObjective cr : customObjectives) {
                        if (cr.getName().equalsIgnoreCase(name)) {
                            found = cr;
                            break;
                        }
                    }

                    if (found == null) {
                    	getLogger().warning("Custom objective \"" + name + "\" for Stage " + s2 + " of Quest \"" + quest.name + "\" could not be found!");
                        continue;
                    }

                    Map<String, Object> data = new HashMap<String, Object>();
                    ConfigurationSection sec2 = sec.getConfigurationSection(path + ".data");
                    if (sec2 != null) {
                        for (String dataPath : sec2.getKeys(false)) {
                            data.put(dataPath, sec2.get(dataPath));
                        }
                    }

                    oStage.customObjectives.add(found);
                    oStage.customObjectiveCounts.add(count);
                    oStage.customObjectiveData.add(data);
                }

            }

            if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".objective-override")) {

                oStage.objectiveOverride = config.getString("quests." + questName + ".stages.ordered." + s2 + ".objective-override");

            }

            if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".start-event")) {

                Event evt = Event.loadEvent(config.getString("quests." + questName + ".stages.ordered." + s2 + ".start-event"), this);

                if (evt != null) {
                    oStage.startEvent = evt;
                } else {
                    stageFailed("start-event: in Stage " + s2 + " of Quest " + quest.name + " failed to load.");
                }

            }

            if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".finish-event")) {

                Event evt = Event.loadEvent(config.getString("quests." + questName + ".stages.ordered." + s2 + ".finish-event"), this);

                if (evt != null) {
                    oStage.finishEvent = evt;
                } else {
                    stageFailed("finish-event: in Stage " + s2 + " of Quest " + quest.name + " failed to load.");
                }

            }

            //Legacy support
            if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".event")) {

                Event evt = Event.loadEvent(config.getString("quests." + questName + ".stages.ordered." + s2 + ".event"), this);

                if (evt != null) {
                    oStage.finishEvent = evt;
                    getLogger().info("Converting event: in Stage " + s2 + " of Quest " + quest.name + " to finish-event:");
                    String old = config.getString("quests." + questName + ".stages.ordered." + s2 + ".event");
                    config.set("quests." + questName + ".stages.ordered." + s2 + ".finish-event", old);
                    config.set("quests." + questName + ".stages.ordered." + s2 + ".event", null);
                    needsSaving = true;
                } else {
                    stageFailed("event: in Stage " + s2 + " of Quest " + quest.name + " failed to load.");
                }

            }
            //

            if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".death-event")) {

                Event evt = Event.loadEvent(config.getString("quests." + questName + ".stages.ordered." + s2 + ".death-event"), this);

                if (evt != null) {
                    oStage.deathEvent = evt;
                } else {
                    stageFailed("death-event: in Stage " + s2 + " of Quest " + quest.name + " failed to load.");
                }

            }

            if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".disconnect-event")) {

                Event evt = Event.loadEvent(config.getString("quests." + questName + ".stages.ordered." + s2 + ".disconnect-event"), this);

                if (evt != null) {
                    oStage.disconnectEvent = evt;
                } else {
                    stageFailed("disconnect-event: in Stage " + s2 + " of Quest " + quest.name + " failed to load.");
                }

            }

            if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".chat-events")) {

                if (config.isList("quests." + questName + ".stages.ordered." + s2 + ".chat-events")) {

                    if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".chat-event-triggers")) {

                        if (config.isList("quests." + questName + ".stages.ordered." + s2 + ".chat-event-triggers")) {

                            List<String> chatEvents = config.getStringList("quests." + questName + ".stages.ordered." + s2 + ".chat-events");
                            List<String> chatEventTriggers = config.getStringList("quests." + questName + ".stages.ordered." + s2 + ".chat-event-triggers");
                            boolean loadEventFailed = false;

                            for (int i = 0; i < chatEvents.size(); i++) {

                                Event evt = Event.loadEvent(chatEvents.get(i), this);

                                if (evt != null) {
                                    oStage.chatEvents.put(chatEventTriggers.get(i), evt);
                                } else {
                                    loadEventFailed = true;
                                    stageFailed("" + chatEvents.get(i) + " inside of chat-events: in Stage " + s2 + " of Quest " + quest.name + " failed to load.");
                                }

                            }

                            if (loadEventFailed) {
                                break;
                            }

                        } else {
                            stageFailed("chat-event-triggers in Stage " + s2 + " of Quest " + quest.name + " is not in list format!");
                        }

                    } else {
                        stageFailed("Stage " + s2 + " of Quest " + quest.name + " is missing chat-event-triggers!");
                    }

                } else {
                    stageFailed("chat-events in Stage " + s2 + " of Quest " + quest.name + " is not in list format!");
                }

            }

            if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".delay")) {

                if (config.getLong("quests." + questName + ".stages.ordered." + s2 + ".delay", -999) != -999) {
                    oStage.delay = config.getInt("quests." + questName + ".stages.ordered." + s2 + ".delay") * 1000;
                } else {
                    stageFailed("delay: in Stage " + s2 + " of Quest " + quest.name + " is not a number!");
                }

            }

            if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".delay-message")) {

                oStage.delayMessage = config.getString("quests." + questName + ".stages.ordered." + s2 + ".delay-message");

            }

            if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".start-message")) {

                oStage.startMessage = config.getString("quests." + questName + ".stages.ordered." + s2 + ".start-message");

            }

            if (config.contains("quests." + questName + ".stages.ordered." + s2 + ".complete-message")) {

                oStage.completeMessage = config.getString("quests." + questName + ".stages.ordered." + s2 + ".complete-message");

            }

            LinkedList<Integer> ids = new LinkedList<Integer>();
            if (npcIdsToTalkTo != null) {
                ids.addAll(npcIdsToTalkTo);
            }
            oStage.citizensToInteract = ids;

            quest.orderedStages.add(oStage);

        }
    }

    private void stageFailed(String msg) throws StageFailedException {
        stageFailed(new String[]{msg});
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

        try {
            config.load(eventsFile);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }

        ConfigurationSection sec = config.getConfigurationSection("events");
        for (String s : sec.getKeys(false)) {

            Event event = Event.loadEvent(s, this);
            if (event != null) {
                events.add(event);
            } else {
                getLogger().log(Level.SEVERE, "Failed to load Event \"" + s + "\". Skipping.");
            }

        }

    }

    public static String parseString(String s, Quest quest) {

        String parsed = s;

        if (parsed.contains("<npc>")) {
            parsed = parsed.replaceAll("<npc>", quest.npcStart.getName());
        }

        parsed = parsed.replaceAll("<black>", BLACK.toString());
        parsed = parsed.replaceAll("<darkblue>", DARKBLUE.toString());
        parsed = parsed.replaceAll("<darkgreen>", DARKGREEN.toString());
        parsed = parsed.replaceAll("<darkaqua>", DARKAQUA.toString());
        parsed = parsed.replaceAll("<darkred>", DARKRED.toString());
        parsed = parsed.replaceAll("<purple>", PURPLE.toString());
        parsed = parsed.replaceAll("<gold>", GOLD.toString());
        parsed = parsed.replaceAll("<grey>", GRAY.toString());
        parsed = parsed.replaceAll("<gray>", GRAY.toString());
        parsed = parsed.replaceAll("<darkgrey>", DARKGRAY.toString());
        parsed = parsed.replaceAll("<darkgray>", DARKGRAY.toString());
        parsed = parsed.replaceAll("<blue>", BLUE.toString());
        parsed = parsed.replaceAll("<green>", GREEN.toString());
        parsed = parsed.replaceAll("<aqua>", AQUA.toString());
        parsed = parsed.replaceAll("<red>", RED.toString());
        parsed = parsed.replaceAll("<pink>", PINK.toString());
        parsed = parsed.replaceAll("<yellow>", YELLOW.toString());
        parsed = parsed.replaceAll("<white>", WHITE.toString());

        parsed = parsed.replaceAll("<random>", MAGIC.toString());
        parsed = parsed.replaceAll("<italic>", ITALIC.toString());
        parsed = parsed.replaceAll("<bold>", BOLD.toString());
        parsed = parsed.replaceAll("<underline>", UNDERLINE.toString());
        parsed = parsed.replaceAll("<strike>", STRIKETHROUGH.toString());
        parsed = parsed.replaceAll("<reset>", RESET.toString());

        parsed = parsed.replaceAll("<br>", "\n");

        parsed = ChatColor.translateAlternateColorCodes('&', parsed);

        return parsed;
    }

    public static String parseString(String s, NPC npc) {

        String parsed = s;

        if (parsed.contains("<npc>")) {
            parsed = parsed.replaceAll("<npc>", npc.getName());
        }

        parsed = parsed.replaceAll("<black>", BLACK.toString());
        parsed = parsed.replaceAll("<darkblue>", DARKBLUE.toString());
        parsed = parsed.replaceAll("<darkgreen>", DARKGREEN.toString());
        parsed = parsed.replaceAll("<darkaqua>", DARKAQUA.toString());
        parsed = parsed.replaceAll("<darkred>", DARKRED.toString());
        parsed = parsed.replaceAll("<purple>", PURPLE.toString());
        parsed = parsed.replaceAll("<gold>", GOLD.toString());
        parsed = parsed.replaceAll("<grey>", GRAY.toString());
        parsed = parsed.replaceAll("<gray>", GRAY.toString());
        parsed = parsed.replaceAll("<darkgrey>", DARKGRAY.toString());
        parsed = parsed.replaceAll("<darkgray>", DARKGRAY.toString());
        parsed = parsed.replaceAll("<blue>", BLUE.toString());
        parsed = parsed.replaceAll("<green>", GREEN.toString());
        parsed = parsed.replaceAll("<aqua>", AQUA.toString());
        parsed = parsed.replaceAll("<red>", RED.toString());
        parsed = parsed.replaceAll("<pink>", PINK.toString());
        parsed = parsed.replaceAll("<yellow>", YELLOW.toString());
        parsed = parsed.replaceAll("<white>", WHITE.toString());

        parsed = parsed.replaceAll("<random>", MAGIC.toString());
        parsed = parsed.replaceAll("<italic>", ITALIC.toString());
        parsed = parsed.replaceAll("<bold>", BOLD.toString());
        parsed = parsed.replaceAll("<underline>", UNDERLINE.toString());
        parsed = parsed.replaceAll("<strike>", STRIKETHROUGH.toString());
        parsed = parsed.replaceAll("<reset>", RESET.toString());

        parsed = parsed.replaceAll("<br>", "\n");

        parsed = ChatColor.translateAlternateColorCodes('&', parsed);

        return parsed;

    }

    public static String parseString(String s) {

        String parsed = s;

        parsed = parsed.replaceAll("<black>", BLACK.toString());
        parsed = parsed.replaceAll("<darkblue>", DARKBLUE.toString());
        parsed = parsed.replaceAll("<darkgreen>", DARKGREEN.toString());
        parsed = parsed.replaceAll("<darkaqua>", DARKAQUA.toString());
        parsed = parsed.replaceAll("<darkred>", DARKRED.toString());
        parsed = parsed.replaceAll("<purple>", PURPLE.toString());
        parsed = parsed.replaceAll("<gold>", GOLD.toString());
        parsed = parsed.replaceAll("<grey>", GRAY.toString());
        parsed = parsed.replaceAll("<gray>", GRAY.toString());
        parsed = parsed.replaceAll("<darkgrey>", DARKGRAY.toString());
        parsed = parsed.replaceAll("<darkgray>", DARKGRAY.toString());
        parsed = parsed.replaceAll("<blue>", BLUE.toString());
        parsed = parsed.replaceAll("<green>", GREEN.toString());
        parsed = parsed.replaceAll("<aqua>", AQUA.toString());
        parsed = parsed.replaceAll("<red>", RED.toString());
        parsed = parsed.replaceAll("<pink>", PINK.toString());
        parsed = parsed.replaceAll("<yellow>", YELLOW.toString());
        parsed = parsed.replaceAll("<white>", WHITE.toString());

        parsed = parsed.replaceAll("<random>", MAGIC.toString());
        parsed = parsed.replaceAll("<italic>", ITALIC.toString());
        parsed = parsed.replaceAll("<bold>", BOLD.toString());
        parsed = parsed.replaceAll("<underline>", UNDERLINE.toString());
        parsed = parsed.replaceAll("<strike>", STRIKETHROUGH.toString());
        parsed = parsed.replaceAll("<reset>", RESET.toString());
        parsed = ChatColor.translateAlternateColorCodes('&', parsed);

        return parsed;

    }

    private boolean setupEconomy() {
        try {

            RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
            if (economyProvider != null) {
                economy = economyProvider.getProvider();
            }

            return (economy != null);

        } catch (Exception e) {
            return false;
        }
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }

    private static Map<String, Integer> sort(Map<String, Integer> unsortedMap) {

        List<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(unsortedMap.entrySet());

        Collections.sort(list, new Comparator<Entry<String, Integer>>() {
            @Override
            public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
                int i = o1.getValue();
                int i2 = o2.getValue();
                if (i < i2) {
                    return 1;
                } else if (i == i2) {
                    return 0;
                } else {
                    return -1;
                }
            }
        });

        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    public boolean hasItem(Player player, ItemStack is) {

        Inventory inv = player.getInventory();
        int playerAmount = 0;

        for (ItemStack stack : inv.getContents()) {

            if (stack != null) {

                if (ItemUtil.compareItems(is, stack, false) == 0) {
                    playerAmount += stack.getAmount();
                }

            }

        }
        return playerAmount >= is.getAmount();

    }

    public static Location getLocation(String arg) {

        String[] info = arg.split(" ");
        if (info.length != 4) {
            return null;
        }

        double x;
        double y;
        double z;

        try {
            x = Double.parseDouble(info[1]);
            y = Double.parseDouble(info[2]);
            z = Double.parseDouble(info[3]);
        } catch (NumberFormatException e) {
            return null;
        }

        if (Bukkit.getServer().getWorld(info[0]) == null) {
            return null;
        }

        Location finalLocation = new Location(Bukkit.getServer().getWorld(info[0]), x, y, z);

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

        if (days > 0) {

            if (days == 1) {
                message += " 1 " + Lang.get("timeDay") + ",";
            } else {
                message += " " + days + " " + Lang.get("timeDays") + ",";
            }

        }

        if (hours > 0) {

            if (hours == 1) {
                message += " 1 " + Lang.get("timeHour") + ",";
            } else {
                message += " " + hours + " " + Lang.get("timeHours") + ",";
            }

        }

        if (minutes > 0) {

            if (minutes == 1) {
                message += " 1 " + Lang.get("timeMinute") + ",";
            } else {
                message += " " + minutes + " " + Lang.get("timeMinutes") + ",";
            }

        }

        if (seconds > 0) {

            if (seconds == 1) {
                message += " 1 " + Lang.get("timeSecond") + ",";
            } else {
                message += " " + seconds + " " + Lang.get("timeSeconds") + ",";
            }
        } else {
            if (milliSeconds2 > 0) {
                if (milliSeconds2 == 1) {
                    message += " 1 " + Lang.get("timeMillisecond") + ",";
                } else {
                    message += " " + milliSeconds2 + " " + Lang.get("timeMilliseconds") + ",";
                }
            }
        }

        message = message.substring(1, message.length() - 1);

        return message;

    }
    private static final String thou[] = {"", "M", "MM", "MMM"};
    private static final String hund[] = {"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"};
    private static final String ten[] = {"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
    private static final String unit[] = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};

    public static String getNumeral(int i) {

        int th = i / 1000;
        int h = (i / 100) % 10;
        int t = (i / 10) % 10;
        int u = i % 10;

        return thou[th] + hund[h] + ten[t] + unit[u];

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

    public static void addItem(Player p, ItemStack i) {

        PlayerInventory inv = p.getInventory();
        HashMap<Integer, ItemStack> leftover = inv.addItem(i);

        if (leftover != null) {

            if (leftover.isEmpty() == false) {

                for (ItemStack i2 : leftover.values()) {
                    p.getWorld().dropItem(p.getLocation(), i2);
                }

            }

        }

    }

    public static String getCurrency(boolean plural) {

        if (Quests.economy == null) {
            return Lang.get("money");
        }

        if (plural) {
            if (Quests.economy.currencyNamePlural().trim().isEmpty()) {
                return Lang.get("money");
            } else {
                return Quests.economy.currencyNamePlural();
            }
        } else {
            if (Quests.economy.currencyNameSingular().trim().isEmpty()) {
                return Lang.get("money");
            } else {
                return Quests.economy.currencyNameSingular();
            }
        }

    }

    public static boolean removeItem(Inventory inventory, ItemStack is) {

        int amount = is.getAmount();
        HashMap<Integer, ? extends ItemStack> allItems = inventory.all(is);
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

    public boolean checkQuester(UUID uuid) {

        for (String s : questerBlacklist) {

        	try {
        		UUID.fromString(s);
        		return true;
        	} catch (IllegalArgumentException e) {
                getLogger().warning(s + " in config.yml is not a valid UUID for quester-blacklist");
        	}

        }

        return false;

    }

    public static boolean checkList(List<?> list, Class<?> c) {

        if (list == null) {
            return false;
        }

        for (Object o : list) {
        	if (o == null) {
        		Bukkit.getLogger().severe("A null " + c.getSimpleName() + " value was detected in quests.yml, please correct the file");
        		return false;
        	}

            if (c.isAssignableFrom(o.getClass()) == false) {
                return false;
            }

        }

        return true;

    }

    public Quest getQuest(String s) {

        for (Quest q : quests) {
            if (q.name.equalsIgnoreCase(s)) {
                return q;
            }
        }

        return null;
    }

    public Quest findQuest(String s) {

        for (Quest q : quests) {
            if (q.name.equalsIgnoreCase(s)) {
                return q;
            }
        }

        for (Quest q : quests) {
            if (q.name.toLowerCase().contains(s.toLowerCase())) {
                return q;
            }
        }

        return null;

    }

    public Event getEvent(String s) {

        for (Event e : events) {
            if (e.name.equalsIgnoreCase(s)) {
                return e;
            }
        }

        return null;

    }

    public Location getNPCLocation(int id) {

        return citizens.getNPCRegistry().getById(id).getStoredLocation();

    }

    public String getNPCName(int id) {

        return citizens.getNPCRegistry().getById(id).getName();

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

    public static Enchantment getEnchantment(String enchant) {

        String ench = Lang.getKey(enchant);
        ench = ench.replace("ENCHANTMENT_", "");
        Enchantment e = Enchantment.getByName(ench);

        return e != null ? e : getEnchantmentLegacy(ench);
    }

    public static Enchantment getEnchantmentLegacy(String enchant) {

        if (enchant.equalsIgnoreCase("Power")) {

            return Enchantment.ARROW_DAMAGE;

        } else if (enchant.equalsIgnoreCase("Flame")) {

            return Enchantment.ARROW_FIRE;

        } else if (enchant.equalsIgnoreCase("Infinity")) {

            return Enchantment.ARROW_INFINITE;

        } else if (enchant.equalsIgnoreCase("Punch")) {

            return Enchantment.ARROW_KNOCKBACK;

        } else if (enchant.equalsIgnoreCase("Sharpness")) {

            return Enchantment.DAMAGE_ALL;

        } else if (enchant.equalsIgnoreCase("BaneOfArthropods")) {

            return Enchantment.DAMAGE_ARTHROPODS;

        } else if (enchant.equalsIgnoreCase("Smite")) {

            return Enchantment.DAMAGE_UNDEAD;

        } else if (enchant.equalsIgnoreCase("Efficiency")) {

            return Enchantment.DIG_SPEED;

        } else if (enchant.equalsIgnoreCase("Unbreaking")) {

            return Enchantment.DURABILITY;

        } else if (enchant.equalsIgnoreCase("FireAspect")) {

            return Enchantment.FIRE_ASPECT;

        } else if (enchant.equalsIgnoreCase("Knockback")) {

            return Enchantment.KNOCKBACK;

        } else if (enchant.equalsIgnoreCase("Fortune")) {

            return Enchantment.LOOT_BONUS_BLOCKS;

        } else if (enchant.equalsIgnoreCase("Looting")) {

            return Enchantment.LOOT_BONUS_MOBS;

        } else if (enchant.equalsIgnoreCase("LuckOfTheSea")) {

            return Enchantment.LOOT_BONUS_MOBS;

        } else if (enchant.equalsIgnoreCase("Lure")) {

            return Enchantment.LOOT_BONUS_MOBS;

        } else if (enchant.equalsIgnoreCase("Respiration")) {

            return Enchantment.OXYGEN;

        } else if (enchant.equalsIgnoreCase("Protection")) {

            return Enchantment.PROTECTION_ENVIRONMENTAL;

        } else if (enchant.equalsIgnoreCase("BlastProtection")) {

            return Enchantment.PROTECTION_EXPLOSIONS;

        } else if (enchant.equalsIgnoreCase("FeatherFalling")) {

            return Enchantment.PROTECTION_FALL;

        } else if (enchant.equalsIgnoreCase("FireProtection")) {

            return Enchantment.PROTECTION_FIRE;

        } else if (enchant.equalsIgnoreCase("ProjectileProtection")) {

            return Enchantment.PROTECTION_PROJECTILE;

        } else if (enchant.equalsIgnoreCase("SilkTouch")) {

            return Enchantment.SILK_TOUCH;

        } else if (enchant.equalsIgnoreCase("Thorns")) {

            return Enchantment.THORNS;

        } else if (enchant.equalsIgnoreCase("AquaAffinity")) {

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
        	//Do nothing
        }

        return color != null ? color : getDyeColorLegacy(s);

    }

    public static DyeColor getDyeColorLegacy(String s) {

        if (s.equalsIgnoreCase("Black")) {

            return DyeColor.BLACK;

        } else if (s.equalsIgnoreCase("Blue")) {

            return DyeColor.BLUE;

        } else if (s.equalsIgnoreCase("Brown")) {

            return DyeColor.BROWN;

        } else if (s.equalsIgnoreCase("Cyan")) {

            return DyeColor.CYAN;

        } else if (s.equalsIgnoreCase("Gray")) {

            return DyeColor.GRAY;

        } else if (s.equalsIgnoreCase("Green")) {

            return DyeColor.GREEN;

        } else if (s.equalsIgnoreCase("LightBlue")) {

            return DyeColor.LIGHT_BLUE;

        } else if (s.equalsIgnoreCase("Lime")) {

            return DyeColor.LIME;

        } else if (s.equalsIgnoreCase("Magenta")) {

            return DyeColor.MAGENTA;

        } else if (s.equalsIgnoreCase("Orange")) {

            return DyeColor.ORANGE;

        } else if (s.equalsIgnoreCase("Pink")) {

            return DyeColor.PINK;

        } else if (s.equalsIgnoreCase("Purple")) {

            return DyeColor.PURPLE;

        } else if (s.equalsIgnoreCase("Red")) {

            return DyeColor.RED;

        } else if (s.equalsIgnoreCase("Silver")) {

            return DyeColor.SILVER;

        } else if (s.equalsIgnoreCase("White")) {

            return DyeColor.WHITE;

        } else if (s.equalsIgnoreCase("Yellow")) {

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

            if (q.npcStart != null && quester.completedQuests.contains(q.name) == false) {

                if (q.npcStart.getId() == npc.getId()) {

                    if(ignoreLockedQuests == false || ignoreLockedQuests == true && q.testRequirements(quester) == true) {
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

        return heroes.getCharacterManager().getHero(p);

    }

    public boolean testPrimaryHeroesClass(String primaryClass, UUID uuid) {

        Hero hero = getHero(uuid);
        return hero.getHeroClass().getName().equalsIgnoreCase(primaryClass);

    }

    public boolean testSecondaryHeroesClass(String secondaryClass, UUID uuid) {

        Hero hero = getHero(uuid);
        return hero.getHeroClass().getName().equalsIgnoreCase(secondaryClass);

    }

    public void updateData() {

        YamlConfiguration config = new YamlConfiguration();
        File dataFile = new File(this.getDataFolder(), "data.yml");

        try {
            config.load(dataFile);
            config.set("npc-gui", questNPCGUIs);
            config.save(dataFile);
        } catch (Exception e) {
            getLogger().severe("Unable to update data file.");
            if (debug) {
                getLogger().severe("Error log:");
                e.printStackTrace();
            } else {
                getLogger().severe("Enable debug to view the error getLogger()");
            }
        }

    }

    public void convertQuesters() {

        int numQuesters = 0;
        int succeeded = 0;
        int failed = 0;

        final File dataFolder = new File(this.getDataFolder(), "data/");
        final File oldDataFolder = new File(this.getDataFolder(), "data/old/");

        if (oldDataFolder.exists() == false || oldDataFolder.exists() && oldDataFolder.isDirectory() == false) {
            oldDataFolder.mkdir();
        }

        if (dataFolder.exists() && dataFolder.isDirectory()) {

            final File[] files = dataFolder.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return dir.getPath().equals(dataFolder.getPath()) && name.endsWith(".yml");
                }

            });

            numQuesters = files.length;
            if (numQuesters > 0) {

                final ArrayList<String> names = new ArrayList<String>();

                getLogger().info("Gathering Quester information...");
                for (int i = 0; i < numQuesters; i++) {

                    final File file = files[i];
                    final File old = new File(oldDataFolder, file.getName());
                    final String name = file.getName().substring(0, file.getName().length() - 4);
                    final FileConfiguration config = new YamlConfiguration();

                    try {
                        config.load(file);
                        config.save(old);
                        config.set("lastKnownName", name);
                        config.save(file);
                    } catch (Exception e) {
                        failed++;
                    }

                    names.add(name.toLowerCase());
                    succeeded++;

                }

                getLogger().info("Completed: " + succeeded + " Success(es). " + failed + " Failure(s). " + numQuesters + " Total.");
                getLogger().info("Preparing to convert data.");

                Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {

                    @Override
                    public void run() {
                    	getLogger().info("Done. Converting data...");
                        int converted = 0;
                        int failed = 0;

                        final UUIDFetcher fetcher = new UUIDFetcher(names);
                        final Map<String, UUID> idMap;

                        try {

                            idMap = fetcher.call();

                        } catch (Exception ex) {
                        	getLogger().severe("Error retrieving data from Mojang account database. Error log:");
                            Logger.getLogger(Quests.class.getName()).log(Level.SEVERE, null, ex);
                            return;
                        }

                        for (Entry<String, UUID> entry : idMap.entrySet()) {

                            try {

                                final File found = new File(dataFolder, entry.getKey() + ".yml");
                                final File copy = new File(dataFolder, entry.getValue() + ".yml");

                                final FileConfiguration config = new YamlConfiguration();
                                final FileConfiguration newConfig = new YamlConfiguration();

                                config.load(found);

                                if(config.contains("currentQuest")) {

                                    LinkedList<String> currentQuests = new LinkedList<String>();
                                    currentQuests.add(config.getString("currentQuest"));
                                    LinkedList<Integer> currentStages = new LinkedList<Integer>();
                                    currentStages.add(config.getInt("currentStage"));

                                    newConfig.set("currentQuests", currentQuests);
                                    newConfig.set("currentStages", currentStages);
                                    newConfig.set("hasJournal", false);
                                    newConfig.set("lastKnownName", entry.getKey());

                                    ConfigurationSection dataSec = Quester.getLegacyQuestData(config, config.getString("currentQuest"));
                                    newConfig.set("questData", dataSec);

                                }

                                newConfig.save(copy);

                                found.delete();

                                converted++;

                            } catch (Exception ex) {
                                failed++;
                            }

                        }

                        getLogger().info("Conversion completed: " + converted + " Converted. " + failed + " Failed.");
                        getLogger().info("Old data files stored in /Quests/data/old");
                    }

                });

            } else {
            	getLogger().info("No Questers to convert!");
            }

        } else {
        	getLogger().info("Data folder does not exist!");
        }

    }

    public void addToBlacklist(UUID id) {
    	List<String> blacklist = getConfig().getStringList("quester-blacklist");
    	if (!blacklist.contains(id.toString())) {
    		blacklist.add(id.toString());
    		getConfig().set("quester-blacklist", blacklist);
    		saveConfig();
    	}
    	if (!questerBlacklist.contains(id.toString())) {
    		questerBlacklist.add(id.toString());
    	}
    }
}
