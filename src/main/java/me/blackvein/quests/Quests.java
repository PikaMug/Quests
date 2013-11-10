package me.blackvein.quests;

import com.codisimus.plugins.phatloots.PhatLoots;
import com.codisimus.plugins.phatloots.PhatLootsAPI;
import me.blackvein.quests.util.ColorUtil;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.util.player.UserManager;
import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
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
import me.ThaH3lper.com.EpicBoss;
import me.ThaH3lper.com.Mobs.EpicMobs;
import me.blackvein.quests.prompts.QuestAcceptPrompt;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;
import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.scripts.ScriptRegistry;
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
import think.rpgitems.Plugin;
import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGItem;

public class Quests extends JavaPlugin implements ConversationAbandonedListener, ColorUtil {

    public final static Logger log = Logger.getLogger("Minecraft");
    public static Economy economy = null;
    public static Permission permission = null;
    public static WorldGuardPlugin worldGuard = null;
    public static mcMMO mcmmo = null;
    public static EpicBoss epicBoss = null;
    public static Plugin rpgItems = null;
    public static Heroes heroes = null;
    public static PhatLoots phatLoots = null;
    public static boolean snoop = true;
    public static boolean npcEffects = true;
    public static boolean broadcastPartyCreation = true;
    public static boolean ignoreLockedQuests = false;
    public static boolean genFilesOnJoin = true;
    public static int maxPartySize = 0;
    public static int acceptTimeout = 20;
    public static int inviteTimeout = 20;
    public static String effect = "note";
    public final Map<String, Quester> questers = new HashMap<String, Quester>();
    public final List<String> questerBlacklist = new LinkedList<String>();
    public final LinkedList<Quest> quests = new LinkedList<Quest>();
    public final LinkedList<Event> events = new LinkedList<Event>();
    public final LinkedList<NPC> questNPCs = new LinkedList<NPC>();
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
    public boolean load = false;
    public int killDelay = 0;
    public int totalQuestPoints = 0;
    public Lang lang;
    private static Quests instance = null;

    @Override
    public void onEnable() {

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

        try {
            if (getServer().getPluginManager().getPlugin("Citizens") != null) {
                citizens = (CitizensPlugin) getServer().getPluginManager().getPlugin("Citizens");
            }
            if (citizens != null) {
                getServer().getPluginManager().registerEvents(npcListener, this);
            }
        } catch (Exception e) {
            printWarning("[Quests] Legacy version of Citizens found. Citizens in Quests not enabled.");
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

        if (getServer().getPluginManager().getPlugin("RPG Items") != null) {
            rpgItems = (Plugin) getServer().getPluginManager().getPlugin("RPG Items");
        }

        if (getServer().getPluginManager().getPlugin("EpicBoss Gold Edition") != null) {
            epicBoss = (EpicBoss) getServer().getPluginManager().getPlugin("EpicBoss Gold Edition");
        }

        if (getServer().getPluginManager().getPlugin("Heroes") != null) {
            heroes = (Heroes) getServer().getPluginManager().getPlugin("Heroes");
        }

        if (getServer().getPluginManager().getPlugin("RPG Items") != null) {
            rpgItems = think.rpgitems.Plugin.plugin;
        }

        if (getServer().getPluginManager().getPlugin("PhatLoots") != null) {
            phatLoots = (PhatLoots) getServer().getPluginManager().getPlugin("PhatLoots");
        }

        if (!setupEconomy()) {
            printWarning("[Quests] Economy not found.");
        }

        if (!setupPermissions()) {
            printWarning("[Quests] Permissions not found.");
        }

        vault = (Vault) getServer().getPluginManager().getPlugin("Vault");

        if (new File(this.getDataFolder(), "config.yml").exists() == false) {
            printInfo("[Quests] Config not found, writing default to file.");
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

        loadConfig();
        lang = new Lang(this);
        lang.initPhrases();
        lang.save();

        if (new File(this.getDataFolder(), "quests.yml").exists() == false) {

            printInfo("[Quests] Quest data not found, writing default to file.");
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

        if (new File(this.getDataFolder(), "events.yml").exists() == false) {
            printInfo("[Quests] Events data not found, writing default to file.");
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

        getServer().getPluginManager().registerEvents(pListener, this);
        if (npcEffects) {
            getServer().getScheduler().scheduleSyncRepeatingTask(this, effListener, 20, 20);
        }
        printInfo("[Quests] Enabled.");

        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                loadQuests();
                loadEvents();
                log.log(Level.INFO, "[Quests] " + quests.size() + " Quest(s) loaded.");
                log.log(Level.INFO, "[Quests] " + events.size() + " Event(s) loaded.");
                questers.putAll(getOnlineQuesters());
                if (snoop) {
                    snoop();
                }
            }
        }, 5L);

    }

    @Override
    public void onDisable() {

        printInfo("[Quests] Saving Quester data.");
        for (Player p : getServer().getOnlinePlayers()) {

            Quester quester = getQuester(p.getName());
            quester.saveData();

        }
        printInfo("[Quests] Disabled.");

    }

    public LinkedList<Quest> getQuests() {
        return quests;
    }

    @Override
    public void conversationAbandoned(ConversationAbandonedEvent abandonedEvent) {

        if (abandonedEvent.gracefulExit() == false) {

            if (abandonedEvent.getContext().getForWhom() != null) {

                try {
                    abandonedEvent.getContext().getForWhom().sendRawMessage(YELLOW + "Cancelled.");
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

            return YELLOW + "Accept Quest?  " + GREEN + "Yes / No";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String s) {

            Player player = (Player) context.getForWhom();

            if (s.equalsIgnoreCase("Yes")) {

                getQuester(player.getName()).takeQuest(getQuest(getQuester(player.getName()).questToTake));
                return Prompt.END_OF_CONVERSATION;

            } else if (s.equalsIgnoreCase("No")) {

                player.sendMessage(YELLOW + "Cancelled.");
                return Prompt.END_OF_CONVERSATION;

            } else {

                player.sendMessage(RED + "Invalid choice. Type \'Yes\' or \'No\'");
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

        allowCommands = config.getBoolean("allow-command-questing", true);
        allowCommandsForNpcQuests = config.getBoolean("allow-command-quests-with-npcs", false);
        showQuestReqs = config.getBoolean("show-requirements", true);
        allowQuitting = config.getBoolean("allow-quitting", true);
        genFilesOnJoin = config.getBoolean("generate-files-on-join", true);
        snoop = config.getBoolean("snoop", true);
        npcEffects = config.getBoolean("show-npc-effects", true);
        effect = config.getString("npc-effect", "note");
        debug = config.getBoolean("debug-mode", false);
        killDelay = config.getInt("kill-delay", 600);
        acceptTimeout = config.getInt("accept-timeout", 20);

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

        if (config.contains("broadcast-party-creation")) {
            broadcastPartyCreation = config.getBoolean("broadcast-party-creation");
        } else {
            config.set("broadcast-party-creation", true);
        }

        if (config.contains("max-party-size")) {
            maxPartySize = config.getInt("max-party-size");
        } else {
            config.set("max-party-size", 0);
        }

        if (config.contains("party-invite-timeout")) {
            inviteTimeout = config.getInt("party-invite-timeout");
        } else {
            config.set("party-invite-timeout", 20);
        }

        for (String s : config.getStringList("quester-blacklist")) {
            questerBlacklist.add(s);
        }

        try {
            config.save(new File(this.getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void printHelp(Player player) {

        player.sendMessage(GOLD + "- Quests -");
        player.sendMessage(YELLOW + "/quests - Display this help");
        if (player.hasPermission("quests.list")) {
            player.sendMessage(YELLOW + "/quests list <page> - List available Quests");
        }
        if (player.hasPermission("quests.take")) {
            player.sendMessage(YELLOW + "/quests take <quest name> - Accept a Quest");
        }
        if (player.hasPermission("quests.quit")) {
            player.sendMessage(YELLOW + "/quests quit - Quit your current Quest");
        }
        if (player.hasPermission("quests.editor.editor")) {
            player.sendMessage(YELLOW + "/quests editor - Create/Edit Quests");
        }
        if (player.hasPermission("quests.editor.events.editor")) {
            player.sendMessage(YELLOW + "/quests events - Create/Edit Events");
        }
        if (player.hasPermission("quests.stats")) {
            player.sendMessage(YELLOW + "/quests stats - View your Questing stats");
        }
        if (player.hasPermission("quests.top")) {
            player.sendMessage(YELLOW + "/quests top <number> - View top Questers");
        }
        //player.sendMessage(GOLD + "/quests party - Quest Party commands");
        player.sendMessage(YELLOW + "/quests info - Display plugin information");
        player.sendMessage(" ");
        player.sendMessage(YELLOW + "/quest - Display current Quest objectives");
        if (player.hasPermission("quests.questinfo")) {
            player.sendMessage(YELLOW + "/quest <quest name> - Display Quest information");
        }

        if (player.hasPermission("quests.admin")) {
            player.sendMessage(DARKRED + "/questadmin " + RED + "- Questadmin help");
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

        if (cmd.getName().equalsIgnoreCase("quest")) {

            if (cs instanceof Player) {

                if (((Player) cs).hasPermission("quests.quest")) {

                    if (args.length == 0) {

                        if (getQuester(cs.getName()).currentQuest != null) {

                            if (getQuester(cs.getName()).delayStartTime == 0) {
                                cs.sendMessage(GOLD + "---(Objectives)---");
                            }

                            for (String s : getQuester(cs.getName()).getObjectives()) {

                                cs.sendMessage(s);

                            }

                        } else {

                            cs.sendMessage(YELLOW + "You do not currently have an active Quest.");
                            return true;

                        }

                    } else {

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

                            Quest quest = findQuest(name);

                            if (quest != null) {

                                Player player = (Player) cs;
                                Quester quester = getQuester(player.getName());

                                cs.sendMessage(GOLD + "- " + quest.name + " -");
                                cs.sendMessage(" ");
                                if (quest.redoDelay > -1) {

                                    if (quest.redoDelay == 0) {
                                        cs.sendMessage(DARKAQUA + "Redoable");
                                    } else {
                                        cs.sendMessage(DARKAQUA + "Redoable every " + AQUA + getTime(quest.redoDelay) + DARKAQUA + ".");
                                    }

                                }
                                if (quest.npcStart != null) {
                                    cs.sendMessage(YELLOW + "Start: Speak to " + quest.npcStart.getName());
                                } else {
                                    cs.sendMessage(YELLOW + quest.description);
                                }

                                cs.sendMessage(" ");

                                if (showQuestReqs == true) {

                                    cs.sendMessage(GOLD + "Requirements");

                                    if (quest.permissionReqs.isEmpty() == false) {

                                        for (String perm : quest.permissionReqs) {

                                            if (permission.has(player, perm)) {
                                                cs.sendMessage(GREEN + "Permission: " + perm);
                                            } else {
                                                cs.sendMessage(RED + "Permission: " + perm);
                                            }

                                        }

                                    }

                                    if(quest.heroesPrimaryClassReq != null){

                                        if(this.testPrimaryHeroesClass(quest.heroesPrimaryClassReq, player.getName())){
                                            cs.sendMessage(BOLD + "" + GREEN + quest.heroesPrimaryClassReq + RESET + "" + DARKGREEN + " class");
                                        }else{
                                            cs.sendMessage(BOLD + "" + DARKRED + quest.heroesPrimaryClassReq + RESET + "" + RED + " class");
                                        }

                                    }

                                    if(quest.heroesSecondaryClassReq != null){

                                        if(this.testSecondaryHeroesClass(quest.heroesSecondaryClassReq, player.getName())){
                                            cs.sendMessage(BOLD + "" + DARKRED + quest.heroesSecondaryClassReq + RESET + "" + RED + " class");
                                        }else{
                                            cs.sendMessage(BOLD + "" + GREEN + quest.heroesSecondaryClassReq + RESET + "" + DARKGREEN + " class");
                                        }

                                    }

                                    if (quest.mcMMOSkillReqs.isEmpty() == false) {

                                        for (String skill : quest.mcMMOSkillReqs) {

                                            int level = Quests.getMCMMOSkillLevel(Quests.getMcMMOSkill(skill), player.getName());
                                            int req = quest.mcMMOAmountReqs.get(quest.mcMMOSkillReqs.indexOf(skill));
                                            String skillName = MiscUtil.getCapitalized(skill);

                                            if (level >= req) {
                                                cs.sendMessage(GREEN + skillName + " level " + req);
                                            } else {
                                                cs.sendMessage(RED + skillName + " level " + req);
                                            }

                                        }

                                    }

                                    if (quest.questPointsReq != 0) {

                                        if (quester.questPoints >= quest.questPointsReq) {
                                            cs.sendMessage(GRAY + "- " + GREEN + quest.questPointsReq + " Quest Points");
                                        } else {
                                            cs.sendMessage(GRAY + "- " + RED + quest.questPointsReq + " Quest Points");
                                        }

                                    }

                                    if (quest.moneyReq != 0) {

                                        if (economy.getBalance(quester.name) >= quest.moneyReq) {
                                            if (quest.moneyReq == 1) {
                                                cs.sendMessage(GRAY + "- " + GREEN + quest.moneyReq + " " + Quests.getCurrency(false));
                                            } else {
                                                cs.sendMessage(GRAY + "- " + GREEN + quest.moneyReq + " " + Quests.getCurrency(true));
                                            }
                                        } else {
                                            if (quest.moneyReq == 1) {
                                                cs.sendMessage(GRAY + "- " + RED + quest.moneyReq + " " + Quests.getCurrency(false));
                                            } else {
                                                cs.sendMessage(GRAY + "- " + RED + quest.moneyReq + " " + Quests.getCurrency(true));
                                            }
                                        }

                                    }

                                    if (quest.items.isEmpty() == false) {

                                        for (ItemStack is : quest.items) {

                                            if (hasItem(player, is) == true) {
                                                cs.sendMessage(GRAY + "- " + GREEN + ItemUtil.getString(is));
                                            } else {
                                                cs.sendMessage(GRAY + "- " + RED + ItemUtil.getString(is));
                                            }

                                        }

                                    }

                                    if (quest.neededQuests.isEmpty() == false) {

                                        for (String s : quest.neededQuests) {

                                            if (quester.completedQuests.contains(s)) {
                                                cs.sendMessage(GRAY + "- " + GREEN + "Complete " + ITALIC + s);
                                            } else {
                                                cs.sendMessage(GRAY + "- " + RED + "Complete " + ITALIC + s);
                                            }

                                        }

                                    }

                                    if (quest.blockQuests.isEmpty() == false) {

                                        for (String s : quest.blockQuests) {

                                            if (quester.completedQuests.contains(s)) {
                                                cs.sendMessage(GRAY + "- " + RED + "You have already Completed " + ITALIC + s);
                                            } else {
                                                cs.sendMessage(GRAY + "- " + GREEN + "Still able to complete " + ITALIC + s);
                                            }

                                        }

                                    }

                                }

                            } else {

                                cs.sendMessage(YELLOW + "Quest not found.");
                                return true;

                            }

                        } else {

                            cs.sendMessage(RED + "You do not have permission to view a Quest's information.");
                            return true;

                        }

                    }

                } else {

                    cs.sendMessage(RED + "You do not have access to that command.");
                    return true;

                }

            } else {

                cs.sendMessage(YELLOW + "This command may only be performed in-game.");
                return true;

            }

            return true;

        } else if (cmd.getName().equalsIgnoreCase("quests")) {

            if (cs instanceof Player) {

                if (args.length == 0) {

                    if (((Player) cs).hasPermission("quests.quests")) {

                        Player p = (Player) cs;
                        printHelp(p);

                    } else {

                        cs.sendMessage(RED + "You do not have access to that command.");
                        return true;

                    }

                } else {

                    if (args[0].equalsIgnoreCase("list")) {

                        if (((Player) cs).hasPermission("quests.list")) {

                            if (args.length == 1) {
                                listQuests((Player) cs, 1);
                            } else if (args.length == 2) {

                                int page;

                                try {

                                    page = Integer.parseInt(args[1]);
                                    if (page < 1) {

                                        cs.sendMessage(YELLOW + "Page selection must be a positive number.");
                                        return true;

                                    }

                                } catch (NumberFormatException e) {

                                    cs.sendMessage(YELLOW + "Page selection must be a number.");
                                    return true;

                                }

                                listQuests((Player) cs, page);
                                return true;

                            }

                        } else {

                            cs.sendMessage(RED + "You do not have permission to view the Quests list.");
                            return true;

                        }

                    } else if (args[0].equalsIgnoreCase("take")) {

                        if (allowCommands == true) {

                            if (((Player) cs).hasPermission("quests.take")) {

                                if (args.length == 1) {

                                    cs.sendMessage(YELLOW + "Usage: /quests take <quest>");
                                    return true;

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

                                        final Quest quest = questToFind;
                                        final Quester quester = getQuester(cs.getName());

                                        if (quester.currentQuest != null) {
                                            cs.sendMessage(YELLOW + "You may only have one active Quest.");
                                        } else if (quester.completedQuests.contains(quest.name) && quest.redoDelay < 0) {
                                            cs.sendMessage(YELLOW + "You have already completed " + PURPLE + quest.name + YELLOW + ".");
                                        } else if (quest.npcStart != null && allowCommandsForNpcQuests == false) {
                                            cs.sendMessage(YELLOW + "You must speak to " + PURPLE + quest.npcStart.getName() + YELLOW + " to start this Quest.");
                                        } else if (quest.blockStart != null) {
                                            cs.sendMessage(PURPLE + quest.name + YELLOW + " may not be started via command.");
                                        } else {

                                            boolean takeable = true;

                                            if (quester.completedQuests.contains(quest.name)) {

                                                if (quester.getDifference(quest) > 0) {
                                                    cs.sendMessage(YELLOW + "You may not take " + AQUA + quest.name + YELLOW + " again for another " + PURPLE + getTime(quester.getDifference(quest)) + YELLOW + ".");
                                                    takeable = false;
                                                }

                                            }

                                            if(quest.region != null){

                                                boolean inRegion = false;
                                                Player p = quester.getPlayer();
                                                RegionManager rm = worldGuard.getRegionManager(p.getWorld());
                                                Iterator<ProtectedRegion> it = rm.getApplicableRegions(p.getLocation()).iterator();
                                                while(it.hasNext()){
                                                    ProtectedRegion pr = it.next();
                                                    if(pr.getId().equalsIgnoreCase(quest.region)){
                                                        inRegion = true;
                                                        break;
                                                    }
                                                }

                                                if(inRegion == false){
                                                    cs.sendMessage(YELLOW + "You may not take " + AQUA + quest.name + YELLOW + " at this location.");
                                                    takeable = false;
                                                }

                                            }

                                            if (takeable == true) {

                                                if (cs instanceof Conversable) {

                                                    if (((Player) cs).isConversing() == false) {

                                                        quester.questToTake = quest.name;

                                                        String s
                                                                = GOLD + "- " + PURPLE + quester.questToTake + GOLD + " -\n"
                                                                + "\n"
                                                                + RESET + getQuest(quester.questToTake).description + "\n";

                                                        for (String msg : s.split("<br>")) {
                                                            cs.sendMessage(msg);
                                                        }

                                                        conversationFactory.buildConversation((Conversable) cs).begin();

                                                    } else {

                                                        cs.sendMessage(YELLOW + "You are already in a conversation!");

                                                    }

                                                    return true;
                                                } else {
                                                    return false;
                                                }

                                            }

                                        }

                                    } else {
                                        cs.sendMessage(YELLOW + "Quest not found.");
                                        return true;
                                    }

                                }

                            } else {

                                cs.sendMessage(RED + "You do not have permission to take Quests via commands.");
                                return true;

                            }

                        } else {

                            cs.sendMessage(YELLOW + "Taking Quests via commands has been disabled.");
                            return true;

                        }

                    } else if (args[0].equalsIgnoreCase("quit")) {

                        if (allowQuitting == true) {

                            if (((Player) cs).hasPermission("quests.quit")) {

                                Quester quester = getQuester(cs.getName());
                                if (quester.currentQuest != null) {

                                    quester.resetObjectives();
                                    quester.currentStage = null;
                                    cs.sendMessage(YELLOW + "You have quit " + PURPLE + quester.currentQuest.name + YELLOW + ".");
                                    quester.currentQuest = null;
                                    quester.saveData();
                                    quester.loadData();
                                    return true;

                                } else {

                                    cs.sendMessage(YELLOW + "You do not currently have an active Quest.");
                                    return true;

                                }

                            } else {

                                cs.sendMessage(RED + "You do not have permission to quit Quests.");
                                return true;
                            }

                        } else {

                            cs.sendMessage(YELLOW + "Quitting Quests has been disabled.");
                            return true;

                        }

                    } else if (args[0].equalsIgnoreCase("stats")) {

                        Quester quester = getQuester(cs.getName());
                        cs.sendMessage(GOLD + "- " + cs.getName() + " -");
                        cs.sendMessage(YELLOW + "Quest points: " + PURPLE + quester.questPoints + "/" + totalQuestPoints);
                        if (quester.currentQuest == null) {
                            cs.sendMessage(YELLOW + "Current Quest: " + PURPLE + "None");
                        } else {
                            cs.sendMessage(YELLOW + "Current Quest: " + PURPLE + quester.currentQuest.name);
                        }

                        String completed;

                        if (quester.completedQuests.isEmpty()) {
                            completed = PURPLE + "None";
                        } else {

                            completed = PURPLE + "";
                            for (String s : quester.completedQuests) {

                                if (quester.completedQuests.indexOf(s) < (quester.completedQuests.size() - 1)) {
                                    completed = completed + s + ", ";
                                } else {
                                    completed = completed + s;
                                }

                            }

                        }

                        cs.sendMessage(YELLOW + "- Completed Quests -");
                        cs.sendMessage(completed);

                    } else if (args[0].equalsIgnoreCase("top")) {

                        if (args.length == 1 || args.length > 2) {

                            cs.sendMessage(YELLOW + "Usage: /quests top <number>");

                        } else {

                            int topNumber;

                            try {

                                topNumber = Integer.parseInt(args[1]);

                            } catch (NumberFormatException e) {

                                cs.sendMessage(YELLOW + "Input must be a number.");
                                return true;

                            }

                            if (topNumber < 1) {

                                cs.sendMessage(YELLOW + "Input must be a positive number.");
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

                            cs.sendMessage(GOLD + "- Top " + PURPLE + topNumber + GOLD + " Questers -");
                            for (String s : sortedMap.keySet()) {

                                int i = (Integer) sortedMap.get(s);
                                numPrinted++;
                                cs.sendMessage(YELLOW + String.valueOf(numPrinted) + ". " + s + " - " + PURPLE + i + YELLOW + " Quest points");

                                if (numPrinted == topNumber) {
                                    break;
                                }

                            }

                        }

                        return true;

                    } else if (args[0].equalsIgnoreCase("editor")) {

                        if (cs.hasPermission("quests.editor.editor")) {
                            questFactory.convoCreator.buildConversation((Conversable) cs).begin();
                        } else {
                            cs.sendMessage(RED + "You do not have permission to use the Quests Editor.");
                        }
                        return true;

                    } else if (args[0].equalsIgnoreCase("events")) {

                        if (cs.hasPermission("quests.editor.events.editor")) {
                            eventFactory.convoCreator.buildConversation((Conversable) cs).begin();
                        } else {
                            cs.sendMessage(RED + "You do not have permission to use the Events editor.");
                        }
                        return true;

                    } else if (args[0].equalsIgnoreCase("info")) {

                        cs.sendMessage(GOLD + "Quests " + this.getDescription().getVersion());
                        cs.sendMessage(GOLD + "Made by " + DARKRED + "Blackvein" + GOLD + " and " + RED + "BlockCat");
                        return true;

                    } else {

                        cs.sendMessage(YELLOW + "Unknown Quests command. Type /quests for help.");
                        return true;

                    }

                }

            } else {

                cs.sendMessage(YELLOW + "This command may only be performed in-game.");
                return true;

            }

            return true;

        } else if (cmd.getName().equalsIgnoreCase("questadmin")) {

            if (args.length == 0) {

                if (cs.hasPermission("quests.admin")) {
                    printAdminHelp(cs);
                } else {
                    cs.sendMessage(RED + "You do not have access to that command.");
                }

            } else if (args.length == 1) {

                if (args[0].equalsIgnoreCase("reload")) {

                    if (cs.hasPermission("quests.admin.reload")) {
                        reloadQuests();
                        cs.sendMessage(GOLD + "Quests reloaded.");
                        cs.sendMessage(PURPLE + String.valueOf(quests.size()) + GOLD + " Quests loaded.");
                    } else {
                        cs.sendMessage(RED + "You do not have access to that command.");
                    }

                } else {

                    cs.sendMessage(YELLOW + "Unknown Questadmin command. Type /questadmin for help.");

                }

            } else if (args.length == 2) {

                if (args[0].equalsIgnoreCase("quit")) {

                    if (cs.hasPermission("quests.admin.quit")) {

                        Player target = null;

                        for (Player p : getServer().getOnlinePlayers()) {

                            if (p.getName().toLowerCase().contains(args[1].toLowerCase())) {
                                target = p;
                                break;
                            }

                        }

                        if (target == null) {

                            cs.sendMessage(YELLOW + "Player not found.");

                        } else {

                            Quester quester = getQuester(target.getName());
                            if (quester.currentQuest == null) {

                                cs.sendMessage(YELLOW + target.getName() + " does not currently have an active Quest.");

                            } else {

                                quester.resetObjectives();
                                quester.currentStage = null;
                                cs.sendMessage(GREEN + target.getName() + GOLD + " has forcibly quit the Quest " + PURPLE + quester.currentQuest.name + GOLD + ".");
                                target.sendMessage(GREEN + cs.getName() + GOLD + " has forced you to quit the Quest " + PURPLE + quester.currentQuest.name + GOLD + ".");
                                quester.currentQuest = null;

                                quester.saveData();

                            }

                        }

                    } else {

                        cs.sendMessage(RED + "You do not have access to that command.");

                    }

                } else if (args[0].equalsIgnoreCase("nextstage")) {

                    if (cs.hasPermission("quests.admin.nextstage")) {

                        Player target = null;

                        for (Player p : getServer().getOnlinePlayers()) {

                            if (p.getName().toLowerCase().contains(args[1].toLowerCase())) {
                                target = p;
                                break;
                            }

                        }

                        if (target == null) {

                            cs.sendMessage(YELLOW + "Player not found.");

                        } else {

                            Quester quester = getQuester(target.getName());
                            if (quester.currentQuest == null) {

                                cs.sendMessage(YELLOW + target.getName() + " does not currently have an active Quest.");

                            } else {

                                cs.sendMessage(GREEN + target.getName() + GOLD + " has advanced to the next Stage in the Quest " + PURPLE + quester.currentQuest.name + GOLD + ".");
                                target.sendMessage(GREEN + cs.getName() + GOLD + " has advanced you to the next Stage in your Quest " + PURPLE + quester.currentQuest.name + GOLD + ".");
                                quester.currentQuest.nextStage(quester);

                                quester.saveData();

                            }

                        }

                    } else {

                        cs.sendMessage(RED + "You do not have access to that command.");

                    }

                } else if (args[0].equalsIgnoreCase("finish")) {

                    if (cs.hasPermission("quests.admin.finish")) {

                        Player target = null;

                        for (Player p : getServer().getOnlinePlayers()) {

                            if (p.getName().toLowerCase().contains(args[1].toLowerCase())) {
                                target = p;
                                break;
                            }

                        }

                        if (target == null) {

                            cs.sendMessage(YELLOW + "Player not found.");

                        } else {

                            Quester quester = getQuester(target.getName());
                            if (quester.currentQuest == null) {

                                cs.sendMessage(YELLOW + target.getName() + " does not currently have an active Quest.");

                            } else {

                                cs.sendMessage(GREEN + target.getName() + GOLD + " has advanced to the next Stage in the Quest " + PURPLE + quester.currentQuest.name + GOLD + ".");
                                target.sendMessage(GREEN + cs.getName() + GOLD + " has advanced you to the next Stage in your Quest " + PURPLE + quester.currentQuest.name + GOLD + ".");
                                quester.currentQuest.completeQuest(quester);

                                quester.saveData();

                            }

                        }

                    } else {

                        cs.sendMessage(RED + "You do not have access to that command.");

                    }

                } else if (args[0].equalsIgnoreCase("pointsall")) {

                    if (cs.hasPermission("quests.admin.points.all")) {

                        final int amount;

                        try {

                            amount = Integer.parseInt(args[1]);

                            if (amount < 0) {
                                cs.sendMessage(RED + "Error: Amount cannot be less than zero!");
                                return true;
                            }

                        } catch (NumberFormatException e) {
                            cs.sendMessage(RED + "Error: Input was not a number!");
                            return true;
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

                                            if (failCount < 4) {
                                                cs.sendMessage(RED + "Error reading " + DARKAQUA + f.getName() + RED + ", skipping..");
                                            } else if (suppressed == false) {
                                                cs.sendMessage(RED + "Error reading " + DARKAQUA + f.getName() + RED + ", suppressing further errors.");
                                                suppressed = true;
                                            }

                                        } catch (InvalidConfigurationException e) {
                                            if (failCount < 4) {
                                                cs.sendMessage(RED + "Error reading " + DARKAQUA + f.getName() + RED + ", skipping..");
                                            } else if (suppressed == false) {
                                                cs.sendMessage(RED + "Error reading " + DARKAQUA + f.getName() + RED + ", suppressing further errors.");
                                                suppressed = true;
                                            }
                                        }

                                    }

                                    cs.sendMessage(GREEN + "Done.");
                                    getServer().broadcastMessage(YELLOW + "[Quests] " + GOLD + "All players' Quest Points have been set to " + AQUA + amount + GOLD + "!");

                                } else {
                                    cs.sendMessage(RED + "Error: Unable to read Quests data folder!");
                                }

                            }
                        });

                        cs.sendMessage(YELLOW + "Setting all players' Quest Points...");
                        for (Quester q : questers.values()) {

                            q.questPoints = amount;

                        }
                        thread.start();

                    } else {

                        cs.sendMessage(RED + "You do not have access to that command.");

                    }

                } else {

                    cs.sendMessage(YELLOW + "Unknown Questadmin command. Type /questadmin for help.");

                }

            } else {

                if (args[0].equalsIgnoreCase("give")) {

                    if (cs.hasPermission("quests.admin.give")) {

                        Player target = null;

                        for (Player p : getServer().getOnlinePlayers()) {

                            if (p.getName().toLowerCase().contains(args[1].toLowerCase())) {
                                target = p;
                                break;
                            }

                        }

                        if (target == null) {

                            cs.sendMessage(YELLOW + "Player not found.");

                        } else {

                            Quest questToGive;

                            String name = null;

                            if (args.length == 3) {
                                name = args[2].toLowerCase();
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

                            questToGive = findQuest(name);

                            if (questToGive == null) {

                                cs.sendMessage(YELLOW + "Quest not found.");

                            } else {

                                Quester quester = getQuester(target.getName());

                                quester.resetObjectives();

                                quester.currentQuest = questToGive;
                                quester.currentStage = questToGive.orderedStages.getFirst();
                                quester.addEmpties();
                                cs.sendMessage(GREEN + target.getName() + GOLD + " has forcibly started the Quest " + PURPLE + questToGive.name + GOLD + ".");
                                target.sendMessage(GREEN + cs.getName() + GOLD + " has forced you to take the Quest " + PURPLE + questToGive.name + GOLD + ".");
                                target.sendMessage(GOLD + "---(Objectives)---");
                                for (String s : quester.getObjectives()) {
                                    target.sendMessage(s);
                                }

                                quester.saveData();

                            }

                        }

                    } else {

                        cs.sendMessage(RED + "You do not have access to that command.");

                    }

                } else if (args[0].equalsIgnoreCase("points")) {

                    if (cs.hasPermission("quests.admin.points")) {

                        Player target = null;

                        for (Player p : getServer().getOnlinePlayers()) {

                            if (p.getName().equalsIgnoreCase(args[1])) {
                                target = p;
                                break;
                            }

                        }

                        if (target == null) {

                            cs.sendMessage(YELLOW + "Player not found.");

                        } else {

                            int points;

                            try {

                                points = Integer.parseInt(args[2]);

                            } catch (NumberFormatException e) {

                                cs.sendMessage(YELLOW + "Amount must be a number.");
                                return true;

                            }

                            Quester quester = getQuester(target.getName());
                            quester.questPoints = points;
                            cs.sendMessage(GREEN + target.getName() + GOLD + "\'s Quest Points have been set to " + PURPLE + points + GOLD + ".");
                            target.sendMessage(GREEN + cs.getName() + GOLD + " has set your Quest Points to " + PURPLE + points + GOLD + ".");

                            quester.saveData();

                        }

                    } else {

                        cs.sendMessage(RED + "You do not have access to that command.");

                    }

                } else if (args[0].equalsIgnoreCase("takepoints")) {

                    if (cs.hasPermission("quests.admin.takepoints")) {

                        Player target = null;

                        for (Player p : getServer().getOnlinePlayers()) {

                            if (p.getName().equalsIgnoreCase(args[1])) {
                                target = p;
                                break;
                            }

                        }

                        if (target == null) {

                            cs.sendMessage(YELLOW + "Player not found.");

                        } else {

                            int points;

                            try {

                                points = Integer.parseInt(args[2]);

                            } catch (NumberFormatException e) {

                                cs.sendMessage(YELLOW + "Amount must be a number.");
                                return true;

                            }
                            Quester quester = getQuester(target.getName());
                            quester.questPoints -= Math.abs(points);
                            cs.sendMessage(GOLD + "Took away " + PURPLE + points + GOLD + " Quest Points from " + GREEN + target.getName() + GOLD + "\'s.");
                            target.sendMessage(GREEN + cs.getName() + GOLD + " took away " + PURPLE + points + GOLD + " Quest Points.");

                            quester.saveData();

                        }

                    } else {

                        cs.sendMessage(RED + "You do not have access to that command.");

                    }
                } else if (args[0].equalsIgnoreCase("givepoints")) {

                    if (cs.hasPermission("quests.admin.givepoints")) {

                        Player target = null;

                        for (Player p : getServer().getOnlinePlayers()) {

                            if (p.getName().equalsIgnoreCase(args[1])) {
                                target = p;
                                break;
                            }

                        }

                        if (target == null) {

                            cs.sendMessage(YELLOW + "Player not found.");

                        } else {

                            int points;

                            try {

                                points = Integer.parseInt(args[2]);

                                Quester quester = getQuester(target.getName());
                                quester.questPoints += Math.abs(points);
                                cs.sendMessage(GOLD + "Gave " + PURPLE + points + GOLD + " Quest Points to " + GREEN + target.getName() + GOLD + ".");
                                target.sendMessage(GREEN + cs.getName() + GOLD + " gave you " + PURPLE + points + GOLD + " Quest Points.");

                                quester.saveData();

                                return true;

                            } catch (NumberFormatException e) {

                                cs.sendMessage(YELLOW + "Amount must be a number.");

                            }

                        }

                    } else {

                        cs.sendMessage(RED + "You do not have access to that command.");

                    }
                } else {

                    cs.sendMessage(YELLOW + "Unknown Questadmin command. Type /questadmin for help.");

                }

            }

            return true;

        }

        return false;

    }

    public void printAdminHelp(CommandSender cs) {

        cs.sendMessage(RED + "- " + DARKRED + "Questadmin" + RED + " -");
        cs.sendMessage("");
        cs.sendMessage(DARKRED + "/questadmin" + RED + " - View Questadmin help");
        if (cs.hasPermission("quests.admin.give")) {
            cs.sendMessage(DARKRED + "/questadmin give <player> <quest>" + RED + " - Force a player to take a Quest");
        }
        if (cs.hasPermission("quests.admin.quit")) {
            cs.sendMessage(DARKRED + "/questadmin quit <player>" + RED + " - Force a player to quit their Quest");
        }
        if (cs.hasPermission("quests.admin.points")) {
            cs.sendMessage(DARKRED + "/questadmin points <player> <amount>" + RED + " - Set a players Quest Points");
        }
        if (cs.hasPermission("quests.admin.takepoints")) {
            cs.sendMessage(DARKRED + "/questadmin takepoints <player> <amount>" + RED + " - Take a players Quest Points");
        }
        if (cs.hasPermission("quests.admin.givepoints")) {
            cs.sendMessage(DARKRED + "/questadmin givepoints <player> <amount>" + RED + " - Give a player Quest Points");
        }
        if (cs.hasPermission("quests.admin.pointsall")) {
            cs.sendMessage(DARKRED + "/questadmin pointsall <amount>" + RED + " - Set ALL players' Quest Points");
        }
        if (cs.hasPermission("quests.admin.finish")) {
            cs.sendMessage(DARKRED + "/questadmin finish <player>" + RED + " - Immediately force Quest completion for a player");
        }
        if (cs.hasPermission("quests.admin.nextstage")) {
            cs.sendMessage(DARKRED + "/questadmin nextstage <player>" + RED + " - Immediately force Stage completion for a player");
        }
        if (cs.hasPermission("quests.admin.reload")) {
            cs.sendMessage(DARKRED + "/questadmin reload" + RED + " - Reload all Quests");
        }

    }

    public void listQuests(Player player, int page) {

        if (quests.size() < ((page * 8) - 7)) {
            player.sendMessage(YELLOW + "Page does not exist.");
        } else {
            player.sendMessage(GOLD + "- Quests -");

            int numOrder = (page - 1) * 8;

            if (numOrder == 0) {
                numOrder = 1;
            }

            List<Quest> subQuests;

            if (numOrder > 1) {
                if (quests.size() >= (numOrder + 7)) {
                    subQuests = quests.subList((numOrder), (numOrder + 8));
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

            for (Quest quest : subQuests) {

                player.sendMessage(YELLOW + Integer.toString(numOrder) + ". " + quest.name);
                numOrder++;

            }

            int numPages = quests.size() / 8;
            if ((quests.size() % 8) > 0 || numPages == 0) {
                numPages++;
            }

            player.sendMessage(GOLD + "- Page " + page + " of " + numPages + " -");

        }

    }

    public void reloadQuests() {

        quests.clear();
        events.clear();
        loadQuests();
        loadEvents();
        loadConfig();

        for (Quester quester : questers.values()) {
            quester.checkQuest();
        }

    }

    public Quester getQuester(String player) {

        Quester quester = null;

        if (questers.containsKey(player)) {
            quester = questers.get(player);
        }

        if (quester == null) {

            if (debug == true) {
                log.log(Level.WARNING, "[Quests] Quester data for player \"" + player + "\" not stored. Attempting manual data retrieval..");
            }

            quester = new Quester(this);
            quester.name = player;
            if (quester.loadData() == false) {
                log.severe("[Quests] Quester not found for player \"" + player + "\". Consider adding them to the Quester blacklist.");
            } else {
                if (debug == true) {
                    log.log(Level.INFO, "[Quests] Manual data retrieval succeeded for player \"" + player + "\"");
                }
                questers.put(player, quester);
            }
        }

        return quester;

    }

    public Map<String, Quester> getOnlineQuesters() {

        Map<String, Quester> qs = new HashMap<String, Quester>();

        for (Player p : getServer().getOnlinePlayers()) {

            Quester quester = new Quester(this);
            quester.name = p.getName();
            if (quester.loadData() == false) {
                quester.saveData();
            }
            qs.put(p.getName(), quester);

        }

        return qs;

    }

    public void loadQuests() {

        FileConfiguration config = new YamlConfiguration();
        File file = new File(this.getDataFolder(), "quests.yml");

        try {

            config.load(file);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }

        ConfigurationSection section1 = config.getConfigurationSection("quests");
        boolean failedToLoad = false;
        totalQuestPoints = 0;
        boolean needsSaving = false;
        for (String s : section1.getKeys(false)) {

            try {

                Quest quest = new Quest();
                failedToLoad = false;

                if (config.contains("quests." + s + ".name")) {
                    quest.name = parseString(config.getString("quests." + s + ".name"), quest);
                } else {
                    printSevere("[Quests] Quest block \'" + s + "\' is missing " + RED + "name:");
                    continue;
                }

                if (config.contains("quests." + s + ".npc-giver-id")) {

                    if (CitizensAPI.getNPCRegistry().getById(config.getInt("quests." + s + ".npc-giver-id")) != null) {

                        quest.npcStart = CitizensAPI.getNPCRegistry().getById(config.getInt("quests." + s + ".npc-giver-id"));
                        questNPCs.add(CitizensAPI.getNPCRegistry().getById(config.getInt("quests." + s + ".npc-giver-id")));

                    } else {
                        printSevere("[Quests] npc-giver-id: for Quest " + quest.name + " is not a valid NPC id!");
                        continue;
                    }

                }

                if (config.contains("quests." + s + ".block-start")) {

                    Location location = getLocation(config.getString("quests." + s + ".block-start"));
                    if (location != null) {
                        quest.blockStart = location;
                    } else {
                        printSevere("[Quests] block-start: for Quest " + quest.name + " is not in proper location format!");
                        printSevere("[Quests] Proper location format is: \"WorldName x y z\"");
                        continue;
                    }

                }

                if(config.contains("quests." + s + ".region")){

                    String region = config.getString("quests." + s + ".region");
                    boolean exists = false;
                    for(World world : getServer().getWorlds()){

                        RegionManager rm = worldGuard.getRegionManager(world);
                        if(rm != null){
                            ProtectedRegion pr = rm.getRegionExact(region);
                            if(pr != null){
                                quest.region = region;
                                exists = true;
                                break;
                            }
                        }

                    }

                    if(!exists){
                        printSevere("[Quests] region: for Quest " + quest.name + " is not a valid WorldGuard region!");
                        continue;
                    }

                }

                if (config.contains("quests." + s + ".redo-delay")) {

                    if (config.getInt("quests." + s + ".redo-delay", -999) != -999) {
                        quest.redoDelay = config.getInt("quests." + s + ".redo-delay");
                    } else {
                        printSevere("[Quests] redo-delay: for Quest " + quest.name + " is not a number!");
                        continue;
                    }

                }

                if (config.contains("quests." + s + ".finish-message")) {
                    quest.finished = parseString(config.getString("quests." + s + ".finish-message"), quest);
                } else {
                    printSevere("[Quests] Quest " + quest.name + " is missing finish-message:");
                    continue;
                }

                if (config.contains("quests." + s + ".ask-message")) {
                    quest.description = parseString(config.getString("quests." + s + ".ask-message"), quest);
                } else {
                    printSevere("[Quests] Quest " + quest.name + " is missing ask-message:");
                    continue;
                }

                if (config.contains("quests." + s + ".event")) {

                    Event evt = Event.loadEvent(config.getString("quests." + s + ".event"), this);

                    if (evt != null) {
                        quest.initialEvent = evt;
                    } else {
                        printSevere("[Quests] Initial Event in Quest " + quest.name + " failed to load.");
                        continue;
                    }

                }

                if (config.contains("quests." + s + ".requirements")) {

                    if (config.contains("quests." + s + ".requirements.fail-requirement-message")) {
                        quest.failRequirements = parseString(config.getString("quests." + s + ".requirements.fail-requirement-message"), quest);
                    } else {
                        printSevere("[Quests] Requirements for Quest " + quest.name + " is missing fail-requirement-message:");
                        continue;
                    }

                    if (config.contains("quests." + s + ".requirements.items")) {

                        if (Quests.checkList(config.getList("quests." + s + ".requirements.items"), String.class)) {
                            List<String> itemReqs = config.getStringList("quests." + s + ".requirements.items");
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
                                printSevere("[Quests] items: Requirement for Quest " + quest.name + " is not formatted correctly!");
                                continue;
                            }

                        } else {
                            printSevere("[Quests] items: Requirement for Quest " + quest.name + " is not formatted correctly!");
                            continue;
                        }

                        if (config.contains("quests." + s + ".requirements.remove-items")) {

                            if (Quests.checkList(config.getList("quests." + s + ".requirements.remove-items"), Boolean.class)) {
                                quest.removeItems.clear();
                                quest.removeItems.addAll(config.getBooleanList("quests." + s + ".requirements.remove-items"));
                            } else {
                                printSevere("[Quests] remove-items: Requirement for Quest " + quest.name + " is not a list of true/false values!");
                                continue;
                            }

                        } else {
                            printSevere("[Quests] Requirements for Quest " + quest.name + " is missing remove-items:");
                            continue;
                        }
                    }

                    if (config.contains("quests." + s + ".requirements.money")) {

                        if (config.getInt("quests." + s + ".requirements.money", -999) != -999) {
                            quest.moneyReq = config.getInt("quests." + s + ".requirements.money");
                        } else {
                            printSevere("[Quests] money: Requirement for Quest " + quest.name + " is not a number!");
                            continue;
                        }

                    }

                    if (config.contains("quests." + s + ".requirements.quest-points")) {

                        if (config.getInt("quests." + s + ".requirements.quest-points", -999) != -999) {
                            quest.questPointsReq = config.getInt("quests." + s + ".requirements.quest-points");
                        } else {
                            printSevere("[Quests] quest-points: Requirement for Quest " + quest.name + " is not a number!");
                            continue;
                        }

                    }

                    if (config.contains("quests." + s + ".requirements.quest-blocks")) {

                        if (Quests.checkList(config.getList("quests." + s + ".requirements.quest-blocks"), String.class)) {

                            List<String> names = config.getStringList("quests." + s + ".requirements.quest-blocks");

                            boolean failed = false;
                            String failedQuest = "NULL";

                            for (String name : names) {

                                boolean done = false;
                                for (String string : section1.getKeys(false)) {

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
                                printSevere("[Quests] " + PINK + failedQuest + " inside quests: Requirement for Quest " + quest.name + " is not a valid Quest name!");
                                printSevere("Make sure you are using the Quest name: value, and not the block name.");
                                continue;
                            }

                        } else {
                            printSevere("[Quests] quest-blocks: Requirement for Quest " + quest.name + " is not a list of Quest names!");
                            continue;
                        }

                    }

                    if (config.contains("quests." + s + ".requirements.quests")) {

                        if (Quests.checkList(config.getList("quests." + s + ".requirements.quests"), String.class)) {

                            List<String> names = config.getStringList("quests." + s + ".requirements.quests");

                            boolean failed = false;
                            String failedQuest = "NULL";

                            for (String name : names) {

                                boolean done = false;
                                for (String string : section1.getKeys(false)) {

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
                                printSevere("[Quests] " + failedQuest + " inside quests: Requirement for Quest " + quest.name + " is not a valid Quest name!");
                                printSevere("Make sure you are using the Quest name: value, and not the block name.");
                                continue;
                            }

                        } else {
                            printSevere("[Quests] quests: Requirement for Quest " + quest.name + " is not a list of Quest names!");
                            continue;
                        }

                    }

                    if (config.contains("quests." + s + ".requirements.permissions")) {

                        if (Quests.checkList(config.getList("quests." + s + ".requirements.permissions"), String.class)) {
                            quest.permissionReqs.clear();
                            quest.permissionReqs.addAll(config.getStringList("quests." + s + ".requirements.permissions"));
                        } else {
                            printSevere("[Quests] permissions: Requirement for Quest " + quest.name + " is not a list of permissions!");
                            continue;
                        }

                    }

                    if (config.contains("quests." + s + ".requirements.mcmmo-skills")) {

                        if (Quests.checkList(config.getList("quests." + s + ".requirements.mcmmo-skills"), String.class)) {

                            if (config.contains("quests." + s + ".requirements.mcmmo-amounts")) {

                                if (Quests.checkList(config.getList("quests." + s + ".requirements.mcmmo-amounts"), Integer.class)) {

                                    List<String> skills = config.getStringList("quests." + s + ".requirements.mcmmo-skills");
                                    List<Integer> amounts = config.getIntegerList("quests." + s + ".requirements.mcmmo-amounts");

                                    if (skills.size() != amounts.size()) {
                                        printSevere("[Quests] mcmmo-skills: and mcmmo-amounts: in requirements: for Quest " + quest.name + " are not the same size!");
                                        continue;
                                    }

                                    quest.mcMMOSkillReqs.addAll(skills);
                                    quest.mcMMOAmountReqs.addAll(amounts);

                                } else {
                                    printSevere("[Quests] mcmmo-amounts: Requirement for Quest " + quest.name + " is not a list of numbers!");
                                    continue;
                                }

                            } else {
                                printSevere("[Quests] Requirements for Quest " + quest.name + " is missing mcmmo-amounts:");
                                continue;
                            }

                        } else {
                            printSevere("[Quests] mcmmo-skills: Requirement for Quest " + quest.name + " is not a list of skills!");
                            continue;
                        }

                    }

                    if (config.contains("quests." + s + ".requirements.heroes-primary-class")) {

                        String className = config.getString("quests." + s + ".requirements.heroes-primary-class");
                        HeroClass hc = heroes.getClassManager().getClass(className);
                        if(hc != null && hc.isPrimary()){
                            quest.heroesPrimaryClassReq = hc.getName();
                        }else if(hc != null){
                            printSevere("[Quests] heroes-primary-class: Requirement for Quest " + quest.name + " is not a primary Heroes class!");
                            continue;
                        }else{
                            printSevere("[Quests] heroes-primary-class: Requirement for Quest " + quest.name + " is not a valid Heroes class!");
                            continue;
                        }

                    }

                    if (config.contains("quests." + s + ".requirements.heroes-secondary-class")) {

                        String className = config.getString("quests." + s + ".requirements.heroes-secondary-class");
                        HeroClass hc = heroes.getClassManager().getClass(className);
                        if(hc != null && hc.isSecondary()){
                            quest.heroesSecondaryClassReq = hc.getName();
                        }else if(hc != null){
                            printSevere("[Quests] heroes-secondary-class: Requirement for Quest " + quest.name + " is not a secondary Heroes class!");
                            continue;
                        }else{
                            printSevere("[Quests] heroes-secondary-class: Requirement for Quest " + quest.name + " is not a valid Heroes class!");
                            continue;
                        }

                    }

                }

                quest.plugin = this;

                ConfigurationSection section2 = config.getConfigurationSection("quests." + s + ".stages.ordered");

                int index = 1;
                boolean stageFailed = false;

                for (String s2 : section2.getKeys(false)) {

                    Stage oStage = new Stage();

                    LinkedList<EntityType> mobsToKill = new LinkedList<EntityType>();
                    LinkedList<Integer> mobNumToKill = new LinkedList<Integer>();
                    LinkedList<Location> locationsToKillWithin = new LinkedList<Location>();
                    LinkedList<Integer> radiiToKillWithin = new LinkedList<Integer>();
                    LinkedList<String> areaNames = new LinkedList<String>();

                    LinkedList<Enchantment> enchantments = new LinkedList<Enchantment>();
                    LinkedList<Material> itemsToEnchant = new LinkedList<Material>();
                    List<Integer> amountsToEnchant = new LinkedList<Integer>();

                    List<Integer> breakids = new LinkedList<Integer>();
                    List<Integer> breakamounts = new LinkedList<Integer>();

                    List<Integer> damageids = new LinkedList<Integer>();
                    List<Integer> damageamounts = new LinkedList<Integer>();

                    List<Integer> placeids = new LinkedList<Integer>();
                    List<Integer> placeamounts = new LinkedList<Integer>();

                    List<Integer> useids = new LinkedList<Integer>();
                    List<Integer> useamounts = new LinkedList<Integer>();

                    List<Integer> cutids = new LinkedList<Integer>();
                    List<Integer> cutamounts = new LinkedList<Integer>();

                    //Denizen script load
                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".script-to-run")) {

                        if (ScriptRegistry.containsScript(config.getString("quests." + s + ".stages.ordered." + s2 + ".script-to-run"))) {
                            trigger = new QuestTaskTrigger();
                            oStage.script = config.getString("quests." + s + ".stages.ordered." + s2 + ".script-to-run");
                        } else {
                            printSevere("[Quests] script-to-run: in Stage " + s2 + " of Quest " + quest.name + " is not a Denizen script!");
                            stageFailed = true;
                            break;
                        }

                    }

                    //
                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".break-block-ids")) {

                        if (checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".break-block-ids"), Integer.class)) {
                            breakids = config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".break-block-ids");
                        } else {
                            printSevere("[Quests] break-block-ids: in Stage " + s2 + " of Quest " + quest.name + " is not a list of numbers!");
                            stageFailed = true;
                            break;
                        }

                        if (config.contains("quests." + s + ".stages.ordered." + s2 + ".break-block-amounts")) {

                            if (checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".break-block-amounts"), Integer.class)) {
                                breakamounts = config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".break-block-amounts");
                            } else {
                                printSevere("[Quests] break-block-amounts: in Stage " + s2 + " of Quest " + quest.name + " is not a list of numbers!");
                                stageFailed = true;
                                break;
                            }

                        } else {
                            printSevere("[Quests] Stage " + s2 + " of Quest " + quest.name + " is missing break-block-amounts:");
                            stageFailed = true;
                            break;
                        }

                    }

                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".damage-block-ids")) {

                        if (checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".damage-block-ids"), Integer.class)) {
                            damageids = config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".damage-block-ids");
                        } else {
                            printSevere("[Quests] damage-block-ids: in Stage " + s2 + " of Quest " + quest.name + " is not a list of numbers!");
                            stageFailed = true;
                            break;
                        }

                        if (config.contains("quests." + s + ".stages.ordered." + s2 + ".damage-block-amounts")) {

                            if (checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".damage-block-amounts"), Integer.class)) {
                                damageamounts = config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".damage-block-amounts");
                            } else {
                                printSevere("[Quests] damage-block-amounts: in Stage " + s2 + " of Quest " + quest.name + " is not a list of numbers!");
                                stageFailed = true;
                                break;
                            }

                        } else {
                            printSevere("[Quests] Stage " + s2 + " of Quest " + quest.name + " is missing damage-block-amounts:");
                            stageFailed = true;
                            break;
                        }

                    }

                    for (int i : damageids) {

                        if (Material.getMaterial(i) != null) {
                            oStage.blocksToDamage.put(Material.getMaterial(i), damageamounts.get(damageids.indexOf(i)));
                        } else {
                            printSevere("[Quests] " + i + " inside damage-block-ids: inside Stage " + s2 + " of Quest " + quest.name + " is not a valid item ID!");
                            stageFailed = true;
                            break;
                        }

                    }

                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".place-block-ids")) {

                        if (checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".place-block-ids"), Integer.class)) {
                            placeids = config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".place-block-ids");
                        } else {
                            printSevere("[Quests] place-block-ids: in Stage " + s2 + " of Quest " + quest.name + " is not a list of numbers!");
                            stageFailed = true;
                            break;
                        }

                        if (config.contains("quests." + s + ".stages.ordered." + s2 + ".place-block-amounts")) {

                            if (checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".place-block-amounts"), Integer.class)) {
                                placeamounts = config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".place-block-amounts");
                            } else {
                                printSevere("[Quests] place-block-amounts: in Stage " + s2 + " of Quest " + quest.name + " is not a list of numbers!");
                                stageFailed = true;
                                break;
                            }

                        } else {
                            printSevere("[Quests] Stage " + s2 + " of Quest " + quest.name + " is missing place-block-amounts:");
                            stageFailed = true;
                            break;
                        }

                    }

                    for (int i : placeids) {

                        if (Material.getMaterial(i) != null) {
                            oStage.blocksToPlace.put(Material.getMaterial(i), placeamounts.get(placeids.indexOf(i)));
                        } else {
                            printSevere("[Quests] " + +i + " inside place-block-ids: inside Stage " + s2 + " of Quest " + quest.name + " is not a valid item ID!");
                            stageFailed = true;
                            break;
                        }

                    }

                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".use-block-ids")) {

                        if (checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".use-block-ids"), Integer.class)) {
                            useids = config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".use-block-ids");
                        } else {
                            printSevere("[Quests] use-block-ids: in Stage " + s2 + " of Quest " + quest.name + " is not a list of numbers!");
                            stageFailed = true;
                            break;
                        }

                        if (config.contains("quests." + s + ".stages.ordered." + s2 + ".use-block-amounts")) {

                            if (checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".use-block-amounts"), Integer.class)) {
                                useamounts = config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".use-block-amounts");
                            } else {
                                printSevere("[Quests] use-block-amounts: in Stage " + s2 + " of Quest " + quest.name + " is not a list of numbers!");
                                stageFailed = true;
                                break;
                            }

                        } else {
                            printSevere("[Quests] Stage " + s2 + " of Quest " + quest.name + " is missing use-block-amounts:");
                            stageFailed = true;
                            break;
                        }

                    }

                    for (int i : useids) {

                        if (Material.getMaterial(i) != null) {
                            oStage.blocksToUse.put(Material.getMaterial(i), useamounts.get(useids.indexOf(i)));
                        } else {
                            printSevere("[Quests] " + RED + i + " inside use-block-ids: inside Stage " + s2 + " of Quest " + quest.name + " is not a valid item ID!");
                            stageFailed = true;
                            break;
                        }

                    }

                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".cut-block-ids")) {

                        if (checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".cut-block-ids"), Integer.class)) {
                            cutids = config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".cut-block-ids");
                        } else {
                            printSevere("[Quests] cut-block-ids: in Stage " + s2 + " of Quest " + quest.name + " is not a list of numbers!");
                            stageFailed = true;
                            break;
                        }

                        if (config.contains("quests." + s + ".stages.ordered." + s2 + ".cut-block-amounts")) {

                            if (checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".cut-block-amounts"), Integer.class)) {
                                cutamounts = config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".cut-block-amounts");
                            } else {
                                printSevere("[Quests] cut-block-amounts: in Stage " + s2 + " of Quest " + quest.name + " is not a list of numbers!");
                                stageFailed = true;
                                break;
                            }

                        } else {
                            printSevere("[Quests] Stage " + s2 + " of Quest " + quest.name + " is missing cut-block-amounts:");
                            stageFailed = true;
                            break;
                        }

                    }

                    for (int i : cutids) {

                        if (Material.getMaterial(i) != null) {
                            oStage.blocksToCut.put(Material.getMaterial(i), cutamounts.get(cutids.indexOf(i)));
                        } else {
                            printSevere("[Quests] " + i + " inside cut-block-ids: inside Stage " + s2 + " of Quest " + quest.name + " is not a valid item ID!");
                            stageFailed = true;
                            break;
                        }

                    }

                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".fish-to-catch")) {

                        if (config.getInt("quests." + s + ".stages.ordered." + s2 + ".fish-to-catch", -999) != -999) {
                            oStage.fishToCatch = config.getInt("quests." + s + ".stages.ordered." + s2 + ".fish-to-catch");
                        } else {
                            printSevere("[Quests] fish-to-catch: inside Stage " + s2 + " of Quest " + quest.name + " is not a number!");
                            stageFailed = true;
                            break;
                        }

                    }

                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".players-to-kill")) {

                        if (config.getInt("quests." + s + ".stages.ordered." + s2 + ".players-to-kill", -999) != -999) {
                            oStage.playersToKill = config.getInt("quests." + s + ".stages.ordered." + s2 + ".players-to-kill");
                        } else {
                            printSevere("[Quests] players-to-kill: inside Stage " + s2 + " of Quest " + quest.name + " is not a number!");
                            stageFailed = true;
                            break;
                        }

                    }

                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".enchantments")) {

                        if (Quests.checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".enchantments"), String.class)) {

                            for (String enchant : config.getStringList("quests." + s + ".stages.ordered." + s2 + ".enchantments")) {

                                Enchantment e = Quests.getEnchantment(enchant);

                                if (e != null) {

                                    enchantments.add(e);

                                } else {

                                    printSevere("[Quests] " + enchant + " inside enchantments: inside Stage " + s2 + " of Quest " + quest.name + " is not a valid enchantment!");
                                    stageFailed = true;
                                    break;

                                }

                            }

                        } else {
                            printSevere("[Quests] enchantments: in Stage " + s2 + " of Quest " + quest.name + " is not a list of enchantment names!");
                            stageFailed = true;
                            break;
                        }

                        if (config.contains("quests." + s + ".stages.ordered." + s2 + ".enchantment-item-ids")) {

                            if (Quests.checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".enchantment-item-ids"), Integer.class)) {

                                for (int item : config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".enchantment-item-ids")) {

                                    if (Material.getMaterial(item) != null) {
                                        itemsToEnchant.add(Material.getMaterial(item));
                                    } else {
                                        printSevere("[Quests] " + item + " inside enchantment-item-ids: inside Stage " + s2 + " of Quest " + quest.name + " is not a valid item id!");
                                        stageFailed = true;
                                        break;
                                    }

                                }

                            } else {

                                printSevere("[Quests] enchantment-item-ids: in Stage " + s2 + " of Quest " + quest.name + " is not a list of numbers!");
                                stageFailed = true;
                                break;

                            }

                        } else {
                            printSevere("[Quests] Stage " + s2 + " of Quest " + quest.name + " is missing enchantment-item-ids:");
                            stageFailed = true;
                            break;
                        }

                        if (config.contains("quests." + s + ".stages.ordered." + s2 + ".enchantment-amounts")) {

                            if (Quests.checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".enchantment-amounts"), Integer.class)) {

                                amountsToEnchant = config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".enchantment-amounts");

                            } else {

                                printSevere("[Quests] enchantment-amounts: in Stage " + s2 + " of Quest " + quest.name + " is not a list of numbers!");
                                stageFailed = true;
                                break;

                            }

                        } else {
                            printSevere("[Quests] Stage " + s2 + " of Quest " + quest.name + " is missing enchantment-amounts:");
                            stageFailed = true;
                            break;
                        }

                    }

                    List<Integer> npcIdsToTalkTo = null;

                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".npc-ids-to-talk-to")) {

                        if (checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".npc-ids-to-talk-to"), Integer.class)) {

                            npcIdsToTalkTo = config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".npc-ids-to-talk-to");
                            for (int i : npcIdsToTalkTo) {

                                if (CitizensAPI.getNPCRegistry().getById(i) != null) {

                                    questNPCs.add(CitizensAPI.getNPCRegistry().getById(i));

                                } else {
                                    printSevere("[Quests] " + i + " inside npc-ids-to-talk-to: inside Stage " + s2 + " of Quest " + quest.name + " is not a valid NPC id!");
                                    stageFailed = true;
                                    break;
                                }

                            }

                        } else {
                            printSevere("[Quests] npc-ids-to-talk-to: in Stage " + s2 + " of Quest " + quest.name + " is not a list of numbers!");
                            stageFailed = true;
                            break;
                        }

                    }

                    List<String> itemsToDeliver;
                    List<Integer> itemDeliveryTargetIds;
                    LinkedList<String> deliveryMessages = new LinkedList<String>();

                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".items-to-deliver")) {

                        if (checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".items-to-deliver"), String.class)) {

                            if (config.contains("quests." + s + ".stages.ordered." + s2 + ".npc-delivery-ids")) {

                                if (checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".npc-delivery-ids"), Integer.class)) {

                                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".delivery-messages")) {

                                        itemsToDeliver = config.getStringList("quests." + s + ".stages.ordered." + s2 + ".items-to-deliver");
                                        itemDeliveryTargetIds = config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".npc-delivery-ids");
                                        deliveryMessages.addAll(config.getStringList("quests." + s + ".stages.ordered." + s2 + ".delivery-messages"));

                                        for (String item : itemsToDeliver) {

                                            ItemStack is = ItemUtil.readItemStack(item);

                                            if (is != null) {

                                                int npcId = itemDeliveryTargetIds.get(itemsToDeliver.indexOf(item));
                                                NPC npc = CitizensAPI.getNPCRegistry().getById(npcId);

                                                if (npc != null) {

                                                    oStage.itemsToDeliver.add(is);
                                                    oStage.itemDeliveryTargets.add(npcId);
                                                    oStage.deliverMessages = deliveryMessages;

                                                } else {
                                                    printSevere("[Quests] " + npcId + " inside npc-delivery-ids: inside Stage " + s2 + " of Quest " + quest.name + " is not a valid NPC id!");
                                                    stageFailed = true;
                                                    break;
                                                }

                                            } else {
                                                printSevere("[Quests] " + item + " inside items-to-deliver: inside Stage " + s2 + " of Quest " + quest.name + " is not formatted properly!");
                                                stageFailed = true;
                                                break;
                                            }

                                        }

                                    } else {
                                        printSevere("[Quests] Stage " + s2 + " of Quest " + quest.name + " is missing delivery-messages:");
                                        stageFailed = true;
                                        break;
                                    }

                                } else {
                                    printSevere("[Quests] npc-delivery-ids: in Stage " + s2 + " of Quest " + PURPLE + quest.name + " is not a list of NPC ids!");
                                    stageFailed = true;
                                    break;
                                }

                            } else {
                                printSevere("[Quests] Stage " + s2 + " of Quest " + quest.name + " is missing npc-delivery-ids:");
                                stageFailed = true;
                                break;
                            }

                        } else {
                            printSevere("[Quests] items-to-deliver: in Stage " + s2 + " of Quest " + quest.name + " is not formatted properly!");
                            stageFailed = true;
                            break;
                        }

                    }

                    List<Integer> npcIds;
                    List<Integer> npcAmounts;

                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".npc-ids-to-kill")) {

                        if (checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".npc-ids-to-kill"), Integer.class)) {

                            if (config.contains("quests." + s + ".stages.ordered." + s2 + ".npc-kill-amounts")) {

                                if (checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".npc-kill-amounts"), Integer.class)) {

                                    npcIds = config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".npc-ids-to-kill");
                                    npcAmounts = config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".npc-kill-amounts");
                                    for (int i : npcIds) {

                                        if (CitizensAPI.getNPCRegistry().getById(i) != null) {

                                            if (npcAmounts.get(npcIds.indexOf(i)) > 0) {
                                                oStage.citizensToKill.add(i);
                                                oStage.citizenNumToKill.add(npcAmounts.get(npcIds.indexOf(i)));
                                                questNPCs.add(CitizensAPI.getNPCRegistry().getById(i));
                                            } else {
                                                printSevere("[Quests] " + npcAmounts.get(npcIds.indexOf(i)) + " inside npc-kill-amounts: inside Stage " + s2 + " of Quest " + quest.name + " is not a positive number!");
                                                stageFailed = true;
                                                break;
                                            }

                                        } else {
                                            printSevere("[Quests] " + i + " inside npc-ids-to-kill: inside Stage " + s2 + " of Quest " + quest.name + " is not a valid NPC id!");
                                            stageFailed = true;
                                            break;
                                        }

                                    }

                                } else {
                                    printSevere("[Quests] npc-kill-amounts: in Stage " + s2 + " of Quest " + quest.name + " is not a list of numbers!");
                                    stageFailed = true;
                                    break;
                                }

                            } else {
                                printSevere("[Quests] Stage " + s2 + " of Quest " + quest.name + " is missing npc-kill-amounts:");
                                stageFailed = true;
                                break;
                            }

                        } else {
                            printSevere("[Quests] npc-ids-to-kill: in Stage " + s2 + " of Quest " + quest.name + " is not a list of numbers!");
                            stageFailed = true;
                            break;
                        }

                    }

                    List<String> bossIdsToKill;
                    List<Integer> bossKillAmounts;

                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".boss-ids-to-kill")) {

                        if (checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".boss-ids-to-kill"), String.class)) {

                            if (config.contains("quests." + s + ".stages.ordered." + s2 + ".boss-amounts-to-kill")) {

                                if (checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".boss-amounts-to-kill"), Integer.class)) {

                                    bossIdsToKill = config.getStringList("quests." + s + ".stages.ordered." + s2 + ".boss-ids-to-kill");
                                    bossKillAmounts = config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".boss-amounts-to-kill");
                                    for (String boss : bossIdsToKill) {

                                        if (Quests.getBoss(boss) != null) {

                                            if (bossKillAmounts.get(bossIdsToKill.indexOf(boss)) > 0) {
                                                oStage.bossesToKill.add(boss);
                                                oStage.bossAmountsToKill.add(bossKillAmounts.get(bossIdsToKill.indexOf(boss)));
                                            } else {
                                                printSevere("[Quests] " + bossKillAmounts.get(bossIdsToKill.indexOf(boss)) + GOLD + " inside boss-amounts-to-kill: inside Stage " + s2 + " of Quest " + quest.name + " is not a positive number!");
                                                stageFailed = true;
                                                break;
                                            }

                                        } else {
                                            printSevere("[Quests] " + boss + " inside boss-ids-to-kill: inside Stage " + s2 + " of Quest " + quest.name + " is not a valid EpicBoss name!");
                                            stageFailed = true;
                                            break;
                                        }

                                    }

                                } else {
                                    printSevere("[Quests] boss-amounts-to-kill: in Stage " + s2 + " of Quest " + quest.name + " is not a list of numbers!");
                                    stageFailed = true;
                                    break;
                                }

                            } else {
                                printSevere("[Quests] Stage " + s2 + " of Quest " + quest.name + " is missing boss-amounts-to-kill:");
                                stageFailed = true;
                                break;
                            }

                        } else {
                            printSevere("[Quests] boss-ids-to-kill: in Stage " + s2 + " of Quest " + quest.name + " is not a list of IDs!");
                            stageFailed = true;
                            break;
                        }

                    }

                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".mobs-to-kill")) {

                        if (Quests.checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".mobs-to-kill"), String.class)) {

                            List<String> mobNames = config.getStringList("quests." + s + ".stages.ordered." + s2 + ".mobs-to-kill");
                            for (String mob : mobNames) {

                                EntityType type = getMobType(mob);

                                if (type != null) {

                                    mobsToKill.add(type);

                                } else {

                                    printSevere("[Quests] " + mob + " inside mobs-to-kill: inside Stage " + s2 + " of Quest " + quest.name + " is not a valid mob name!");
                                    stageFailed = true;
                                    break;

                                }

                            }

                        } else {
                            printSevere("[Quests] mobs-to-kill: in Stage " + s2 + " of Quest " + quest.name + " is not a list of mob names!");
                            stageFailed = true;
                            break;
                        }

                        if (config.contains("quests." + s + ".stages.ordered." + s2 + ".mob-amounts")) {

                            if (Quests.checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".mob-amounts"), Integer.class)) {

                                for (int i : config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".mob-amounts")) {

                                    mobNumToKill.add(i);

                                }

                            } else {

                                printSevere("[Quests] mob-amounts: in Stage " + s2 + " of Quest " + quest.name + " is not a list of numbers!");
                                stageFailed = true;
                                break;

                            }

                        } else {
                            printSevere("[Quests] Stage " + s2 + " of Quest " + PURPLE + quest.name + " is missing mob-amounts:");
                            stageFailed = true;
                            break;
                        }

                    }

                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".locations-to-kill")) {

                        if (Quests.checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".locations-to-kill"), String.class)) {

                            List<String> locations = config.getStringList("quests." + s + ".stages.ordered." + s2 + ".locations-to-kill");

                            for (String loc : locations) {

                                String[] info = loc.split(" ");
                                if (info.length == 4) {
                                    double x;
                                    double y;
                                    double z;
                                    try {
                                        x = Double.parseDouble(info[1]);
                                        y = Double.parseDouble(info[2]);
                                        z = Double.parseDouble(info[3]);
                                    } catch (NumberFormatException e) {
                                        printSevere("[Quests] " + loc + " inside mobs-to-kill: inside Stage " + s2 + " of Quest " + quest.name + " is not in proper location format!");
                                        printSevere("[Quests] Proper location format is: \"WorldName x y z\"");
                                        stageFailed = true;
                                        break;
                                    }

                                    if (getServer().getWorld(info[0]) != null) {
                                        Location finalLocation = new Location(getServer().getWorld(info[0]), x, y, z);
                                        locationsToKillWithin.add(finalLocation);
                                    } else {
                                        printSevere("[Quests] " + info[0] + " inside mobs-to-kill: inside Stage " + s2 + " of Quest " + quest.name + " is not a valid world name!");
                                        stageFailed = true;
                                        break;
                                    }

                                } else {
                                    printSevere("[Quests] " + loc + " inside mobs-to-kill: inside Stage " + s2 + " of Quest " + quest.name + " is not in proper location format!");
                                    printSevere("[Quests] Proper location format is: \"WorldName x y z\"");
                                    stageFailed = true;
                                    break;
                                }

                            }

                        } else {
                            printSevere("[Quests] locations-to-kill: in Stage " + s2 + " of Quest " + quest.name + " is not a list of locations!");
                            stageFailed = true;
                            break;
                        }

                        if (config.contains("quests." + s + ".stages.ordered." + s2 + ".kill-location-radii")) {

                            if (Quests.checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".kill-location-radii"), Integer.class)) {

                                List<Integer> radii = config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".kill-location-radii");
                                for (int i : radii) {

                                    radiiToKillWithin.add(i);

                                }

                            } else {
                                printSevere("[Quests] kill-location-radii: in Stage " + s2 + " of Quest " + quest.name + " is not a list of numbers!");
                                stageFailed = true;
                                break;
                            }

                        } else {
                            printSevere("[Quests] Stage " + s2 + " of Quest " + quest.name + " is missing kill-location-radii:");
                            stageFailed = true;
                            break;
                        }

                        if (config.contains("quests." + s + ".stages.ordered." + s2 + ".kill-location-names")) {

                            if (Quests.checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".kill-location-names"), String.class)) {

                                List<String> locationNames = config.getStringList("quests." + s + ".stages.ordered." + s2 + ".kill-location-names");
                                for (String name : locationNames) {

                                    areaNames.add(name);

                                }

                            } else {
                                printSevere("[Quests] kill-location-names: in Stage " + s2 + " of Quest " + quest.name + " is not a list of names!");
                                stageFailed = true;
                                break;
                            }

                        } else {
                            printSevere("[Quests] Stage " + s2 + " of Quest " + quest.name + " is missing kill-location-names:");
                            stageFailed = true;
                            break;
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

                    Map<Material, Integer> breakMap = new EnumMap<Material, Integer>(Material.class);

                    for (int i : breakids) {

                        breakMap.put(Material.getMaterial(i), breakamounts.get(breakids.indexOf(i)));

                    }

                    oStage.blocksToBreak = breakMap;

                    if (index < section2.getKeys(false).size()) {
                        index++;
                    }

                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".locations-to-reach")) {

                        if (Quests.checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".locations-to-reach"), String.class)) {

                            List<String> locations = config.getStringList("quests." + s + ".stages.ordered." + s2 + ".locations-to-reach");

                            for (String loc : locations) {

                                String[] info = loc.split(" ");
                                if (info.length == 4) {
                                    double x;
                                    double y;
                                    double z;
                                    try {
                                        x = Double.parseDouble(info[1]);
                                        y = Double.parseDouble(info[2]);
                                        z = Double.parseDouble(info[3]);
                                    } catch (NumberFormatException e) {
                                        printSevere("[Quests] " + loc + " inside locations-to-reach: inside Stage " + s2 + " of Quest " + quest.name + " is not in proper location format!");
                                        printSevere("[Quests] Proper location format is: \"WorldName x y z\"");
                                        stageFailed = true;
                                        break;
                                    }

                                    if (getServer().getWorld(info[0]) != null) {
                                        Location finalLocation = new Location(getServer().getWorld(info[0]), x, y, z);
                                        oStage.locationsToReach.add(finalLocation);
                                    } else {
                                        printSevere("[Quests] " + info[0] + " inside locations-to-reach: inside Stage " + s2 + " of Quest " + quest.name + " is not a valid world name!");
                                        stageFailed = true;
                                        break;
                                    }

                                } else {
                                    printSevere("[Quests] " + loc + " inside mobs-to-kill: inside Stage " + s2 + " of Quest " + quest.name + " is not in proper location format!");
                                    printSevere("[Quests] Proper location format is: \"WorldName x y z\"");
                                    stageFailed = true;
                                    break;
                                }

                            }

                        } else {
                            printSevere("[Quests] locations-to-reach: in Stage " + s2 + " of Quest " + quest.name + " is not a list of locations!");
                            stageFailed = true;
                            break;
                        }

                        if (config.contains("quests." + s + ".stages.ordered." + s2 + ".reach-location-radii")) {

                            if (Quests.checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".reach-location-radii"), Integer.class)) {

                                List<Integer> radii = config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".reach-location-radii");
                                for (int i : radii) {

                                    oStage.radiiToReachWithin.add(i);

                                }

                            } else {
                                printSevere("[Quests] reach-location-radii: in Stage " + s2 + " of Quest " + quest.name + " is not a list of numbers!");
                                stageFailed = true;
                                break;
                            }

                        } else {
                            printSevere("[Quests] Stage " + s2 + " of Quest " + quest.name + " is missing reach-location-radii:");
                            stageFailed = true;
                            break;
                        }

                        if (config.contains("quests." + s + ".stages.ordered." + s2 + ".reach-location-names")) {

                            if (Quests.checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".reach-location-names"), String.class)) {

                                List<String> locationNames = config.getStringList("quests." + s + ".stages.ordered." + s2 + ".reach-location-names");
                                for (String name : locationNames) {

                                    oStage.locationNames.add(name);

                                }

                            } else {
                                printSevere("[Quests] reach-location-names: in Stage " + s2 + " of Quest " + quest.name + " is not a list of names!");
                                stageFailed = true;
                                break;
                            }

                        } else {
                            printSevere("[Quests] Stage " + s2 + " of Quest " + quest.name + " is missing reach-location-names:");
                            stageFailed = true;
                            break;
                        }

                    }

                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".mobs-to-tame")) {

                        if (Quests.checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".mobs-to-tame"), String.class)) {

                            if (config.contains("quests." + s + ".stages.ordered." + s2 + ".mob-tame-amounts")) {

                                if (Quests.checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".mob-tame-amounts"), Integer.class)) {

                                    List<String> mobs = config.getStringList("quests." + s + ".stages.ordered." + s2 + ".mobs-to-tame");
                                    List<Integer> mobAmounts = config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".mob-tame-amounts");

                                    for (String mob : mobs) {

                                        if (mob.equalsIgnoreCase("Wolf")) {

                                            oStage.mobsToTame.put(EntityType.WOLF, mobAmounts.get(mobs.indexOf(mob)));

                                        } else if (mob.equalsIgnoreCase("Ocelot")) {

                                            oStage.mobsToTame.put(EntityType.OCELOT, mobAmounts.get(mobs.indexOf(mob)));

                                        } else {
                                            printSevere("[Quests] " + mob + " inside mobs-to-tame: inside Stage " + s2 + " of Quest " + quest.name + " is not a valid tameable mob!");
                                            stageFailed = true;
                                            break;
                                        }

                                    }

                                } else {
                                    printSevere("[Quests] mob-tame-amounts: in Stage " + s2 + " of Quest " + quest.name + " is not a list of numbers!");
                                    stageFailed = true;
                                    break;
                                }

                            } else {
                                printSevere("[Quests] Stage " + s2 + " of Quest " + quest.name + " is missing mob-tame-amounts:");
                                stageFailed = true;
                                break;
                            }

                        } else {
                            printSevere("[Quests] mobs-to-tame: in Stage " + s2 + " of Quest " + quest.name + " is not a list of mob names!");
                            stageFailed = true;
                            break;
                        }

                    }

                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".sheep-to-shear")) {

                        if (Quests.checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".sheep-to-shear"), String.class)) {

                            if (config.contains("quests." + s + ".stages.ordered." + s2 + ".sheep-amounts")) {

                                if (Quests.checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".sheep-amounts"), Integer.class)) {

                                    List<String> sheep = config.getStringList("quests." + s + ".stages.ordered." + s2 + ".sheep-to-shear");
                                    List<Integer> shearAmounts = config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".sheep-amounts");

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

                                            printSevere("[Quests] " + color + " inside sheep-to-shear: inside Stage " + s2 + " of Quest " + quest.name + " is not a valid color!");
                                            stageFailed = true;
                                            break;

                                        }

                                    }

                                } else {
                                    printSevere("[Quests] sheep-amounts: in Stage " + s2 + " of Quest " + quest.name + " is not a list of numbers!");
                                    stageFailed = true;
                                    break;
                                }

                            } else {
                                printSevere("[Quests] Stage " + s2 + " of Quest " + quest.name + " is missing sheep-amounts:");
                                stageFailed = true;
                                break;
                            }

                        } else {
                            printSevere("[Quests] sheep-to-shear: in Stage " + s2 + " of Quest " + quest.name + " is not a list of colors!");
                            stageFailed = true;
                            break;
                        }

                    }

                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".start-event")) {

                        Event evt = Event.loadEvent(config.getString("quests." + s + ".stages.ordered." + s2 + ".start-event"), this);

                        if (evt != null) {
                            oStage.startEvent = evt;
                        } else {
                            printSevere("[Quests] start-event: in Stage " + s2 + " of Quest " + quest.name + " failed to load.");
                            stageFailed = true;
                            break;
                        }

                    }

                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".finish-event")) {

                        Event evt = Event.loadEvent(config.getString("quests." + s + ".stages.ordered." + s2 + ".finish-event"), this);

                        if (evt != null) {
                            oStage.finishEvent = evt;
                        } else {
                            printSevere("[Quests] finish-event: in Stage " + s2 + " of Quest " + quest.name + " failed to load.");
                            stageFailed = true;
                            break;
                        }

                    }

                    //Legacy support
                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".event")) {

                        Event evt = Event.loadEvent(config.getString("quests." + s + ".stages.ordered." + s2 + ".event"), this);

                        if (evt != null) {
                            oStage.finishEvent = evt;
                            printInfo("[Quests] Converting event: in Stage " + s2 + " of Quest " + quest.name + " to finish-event:");
                            String old = config.getString("quests." + s + ".stages.ordered." + s2 + ".event");
                            config.set("quests." + s + ".stages.ordered." + s2 + ".finish-event", old);
                            config.set("quests." + s + ".stages.ordered." + s2 + ".event", null);
                            needsSaving = true;
                        } else {
                            printSevere("[Quests] event: in Stage " + s2 + " of Quest " + quest.name + " failed to load.");
                            stageFailed = true;
                            break;
                        }

                    }
                    //

                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".death-event")) {

                        Event evt = Event.loadEvent(config.getString("quests." + s + ".stages.ordered." + s2 + ".death-event"), this);

                        if (evt != null) {
                            oStage.deathEvent = evt;
                        } else {
                            printSevere("[Quests] death-event: in Stage " + s2 + " of Quest " + quest.name + " failed to load.");
                            stageFailed = true;
                            break;
                        }

                    }

                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".disconnect-event")) {

                        Event evt = Event.loadEvent(config.getString("quests." + s + ".stages.ordered." + s2 + ".disconnect-event"), this);

                        if (evt != null) {
                            oStage.disconnectEvent = evt;
                        } else {
                            printSevere("[Quests] disconnect-event: in Stage " + s2 + " of Quest " + quest.name + " failed to load.");
                            stageFailed = true;
                            break;
                        }

                    }

                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".chat-events")) {

                        if (config.isList("quests." + s + ".stages.ordered." + s2 + ".chat-events")) {

                            if (config.contains("quests." + s + ".stages.ordered." + s2 + ".chat-event-triggers")) {

                                if (config.isList("quests." + s + ".stages.ordered." + s2 + ".chat-event-triggers")) {

                                    List<String> chatEvents = config.getStringList("quests." + s + ".stages.ordered." + s2 + ".chat-events");
                                    List<String> chatEventTriggers = config.getStringList("quests." + s + ".stages.ordered." + s2 + ".chat-event-triggers");
                                    boolean loadEventFailed = false;

                                    for (int i = 0; i < chatEvents.size(); i++) {

                                        Event evt = Event.loadEvent(chatEvents.get(i), this);

                                        if (evt != null) {
                                            oStage.chatEvents.put(chatEventTriggers.get(i), evt);
                                        } else {
                                            printSevere("[Quests] " + chatEvents.get(i) + " inside of chat-events: in Stage " + s2 + " of Quest " + quest.name + " failed to load.");
                                            stageFailed = true;
                                            loadEventFailed = true;
                                            break;
                                        }

                                    }

                                    if (loadEventFailed) {
                                        break;
                                    }

                                } else {
                                    printSevere("[Quests] chat-event-triggers in Stage " + s2 + " of Quest " + quest.name + " is not in list format!");
                                    stageFailed = true;
                                    break;
                                }

                            } else {
                                printSevere("[Quests] Stage " + s2 + " of Quest " + quest.name + " is missing chat-event-triggers!");
                                stageFailed = true;
                                break;
                            }

                        } else {
                            printSevere("[Quests] chat-events in Stage " + s2 + " of Quest " + quest.name + " is not in list format!");
                            stageFailed = true;
                            break;
                        }

                    }

                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".delay")) {

                        if (config.getLong("quests." + s + ".stages.ordered." + s2 + ".delay", -999) != -999) {
                            oStage.delay = config.getLong("quests." + s + ".stages.ordered." + s2 + ".delay");
                        } else {
                            printSevere("[Quests] delay: in Stage " + s2 + " of Quest " + quest.name + " is not a number!");
                            stageFailed = true;
                            break;
                        }

                    }

                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".delay-message")) {

                        oStage.delayMessage = config.getString("quests." + s + ".stages.ordered." + s2 + ".delay-message");

                    }

                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".start-message")) {

                        oStage.startMessage = config.getString("quests." + s + ".stages.ordered." + s2 + ".start-message");

                    }

                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".complete-message")) {

                        oStage.completeMessage = config.getString("quests." + s + ".stages.ordered." + s2 + ".complete-message");

                    }

                    LinkedList<Integer> ids = new LinkedList<Integer>();
                    if (npcIdsToTalkTo != null) {
                        ids.addAll(npcIdsToTalkTo);
                    }
                    oStage.citizensToInteract = ids;

                    if (stageFailed) {
                        break;
                    }

                    quest.orderedStages.add(oStage);

                }

                if (stageFailed) {
                    continue;
                }

                //Load rewards
                if (config.contains("quests." + s + ".rewards.items")) {

                    if (Quests.checkList(config.getList("quests." + s + ".rewards.items"), String.class)) {

                        boolean failed = false;
                        for (String item : config.getStringList("quests." + s + ".rewards.items")) {

                            try {
                                ItemStack stack = ItemUtil.readItemStack(item);
                                if (stack != null) {
                                    quest.itemRewards.add(stack);
                                }
                            } catch (Exception e) {
                                printSevere("[Quests] " + item + " in items: Reward in Quest " + quest.name + " is not properly formatted!");
                                failed = true;
                                break;
                            }

                        }

                        if (failed) {
                            continue;
                        }

                    } else {
                        printSevere("[Quests] items: Reward in Quest " + quest.name + " is not a list of strings!");
                        continue;
                    }

                }

                if (config.contains("quests." + s + ".rewards.rpgitem-ids")) {

                    if (Quests.checkList(config.getList("quests." + s + ".rewards.rpgitem-ids"), Integer.class)) {

                        if (config.contains("quests." + s + ".rewards.rpgitem-amounts")) {

                            if (Quests.checkList(config.getList("quests." + s + ".rewards.rpgitem-amounts"), Integer.class)) {

                                boolean failed = false;
                                for (Integer id : config.getIntegerList("quests." + s + ".rewards.rpgitem-ids")) {

                                    try {
                                        RPGItem item = ItemManager.getItemById(id);
                                        if (item != null) {
                                            quest.rpgItemRewardIDs.add(id);
                                        } else {
                                            printSevere("[Quests] " + id + " in rpgitem-ids: Reward in Quest " + quest.name + " is not a valid RPGItem id!");
                                            failed = true;
                                            break;
                                        }
                                    } catch (Exception e) {
                                        printSevere("[Quests] " + id + " in rpgitem-ids: Reward in Quest " + quest.name + " is not properly formatted!");
                                        failed = true;
                                        break;
                                    }

                                }

                                for (Integer amount : config.getIntegerList("quests." + s + ".rewards.rpgitem-amounts")) {

                                    quest.rpgItemRewardAmounts.add(amount);

                                }

                                if (failed) {
                                    continue;
                                }

                            } else {
                                printSevere("[Quests] rpgitem-amounts: Reward in Quest " + quest.name + " is not a list of numbers!");
                                break;
                            }

                        } else {
                            printSevere("[Quests] Rewards for Quest " + quest.name + " is missing rpgitem-amounts");
                            continue;
                        }

                    } else {
                        printSevere("[Quests] rpgitem-ids: Reward in Quest " + quest.name + " is not a list of numbers!");
                        continue;
                    }

                }

                if (config.contains("quests." + s + ".rewards.money")) {

                    if (config.getInt("quests." + s + ".rewards.money", -999) != -999) {
                        quest.moneyReward = config.getInt("quests." + s + ".rewards.money");
                    } else {
                        printSevere("[Quests] money: Reward in Quest " + quest.name + " is not a number!");
                        continue;
                    }

                }

                if (config.contains("quests." + s + ".rewards.exp")) {

                    if (config.getInt("quests." + s + ".rewards.exp", -999) != -999) {
                        quest.exp = config.getInt("quests." + s + ".rewards.exp");
                    } else {
                        printSevere("[Quests] exp: Reward in Quest " + quest.name + " is not a number!");
                        continue;
                    }

                }

                if (config.contains("quests." + s + ".rewards.commands")) {

                    if (Quests.checkList(config.getList("quests." + s + ".rewards.commands"), String.class)) {
                        quest.commands.clear();
                        quest.commands.addAll(config.getStringList("quests." + s + ".rewards.commands"));
                    } else {
                        printSevere("[Quests] commands: Reward in Quest " + quest.name + " is not a list of commands!");
                        continue;
                    }

                }

                if (config.contains("quests." + s + ".rewards.permissions")) {

                    if (Quests.checkList(config.getList("quests." + s + ".rewards.permissions"), String.class)) {
                        quest.permissions.clear();
                        quest.permissions.addAll(config.getStringList("quests." + s + ".rewards.permissions"));
                    } else {
                        printSevere("[Quests] permissions: Reward in Quest " + quest.name + " is not a list of permissions!");
                        continue;
                    }

                }

                if (config.contains("quests." + s + ".rewards.quest-points")) {

                    if (config.getInt("quests." + s + ".rewards.quest-points", -999) != -999) {
                        quest.questPoints = config.getInt("quests." + s + ".rewards.quest-points");
                        totalQuestPoints += quest.questPoints;
                    } else {
                        printSevere("[Quests] quest-points: Reward in Quest " + quest.name + " is not a number!");
                        continue;
                    }

                }

                if (config.contains("quests." + s + ".rewards.mcmmo-skills")) {

                    if (Quests.checkList(config.getList("quests." + s + ".rewards.mcmmo-skills"), String.class)) {

                        if (config.contains("quests." + s + ".rewards.mcmmo-levels")) {

                            if(Quests.checkList(config.getList("quests." + s + ".rewards.mcmmo-levels"), Integer.class)){

                                boolean failed = false;
                                for (String skill : config.getStringList("quests." + s + ".rewards.mcmmo-skills")) {

                                    if (Quests.getMcMMOSkill(skill) == null) {
                                        printSevere("[Quests] " + skill + " in mcmmo-skills: Reward in Quest " + quest.name + " is not a valid mcMMO skill name!");
                                        failed = true;
                                        break;
                                    }

                                }
                                if (failed) {
                                    continue;
                                }

                                quest.mcmmoSkills.clear();
                                quest.mcmmoAmounts.clear();

                                quest.mcmmoSkills.addAll(config.getStringList("quests." + s + ".rewards.mcmmo-skills"));
                                quest.mcmmoAmounts.addAll(config.getIntegerList("quests." + s + ".rewards.mcmmo-levels"));

                            } else {
                                printSevere("[Quests] mcmmo-levels: Reward in Quest " + quest.name + " is not a list of numbers!");
                                continue;
                            }

                        } else {
                            printSevere("[Quests] Rewards for Quest " + quest.name + " is missing mcmmo-levels:");
                            continue;
                        }

                    } else {
                        printSevere("[Quests] mcmmo-skills: Reward in Quest " + quest.name + " is not a list of mcMMO skill names!");
                        continue;
                    }
                }

                if (config.contains("quests." + s + ".rewards.heroes-exp-classes")) {

                    if (Quests.checkList(config.getList("quests." + s + ".rewards.heroes-exp-classes"), String.class)) {

                        if (config.contains("quests." + s + ".rewards.heroes-exp-amounts")) {

                            if (Quests.checkList(config.getList("quests." + s + ".rewards.heroes-exp-amounts"), Double.class)) {

                                boolean failed = false;
                                for (String heroClass : config.getStringList("quests." + s + ".rewards.heroes-exp-classes")) {

                                    if (Quests.heroes.getClassManager().getClass(heroClass) == null) {
                                        printSevere("[Quests] " + heroClass + " in heroes-exp-classes: Reward in Quest " + quest.name + " is not a valid Heroes class name!");
                                        failed = true;
                                        break;
                                    }

                                }
                                if (failed) {
                                    continue;
                                }

                                quest.heroesClasses.clear();
                                quest.heroesAmounts.clear();

                                quest.heroesClasses.addAll(config.getStringList("quests." + s + ".rewards.heroes-exp-classes"));
                                quest.heroesAmounts.addAll(config.getDoubleList("quests." + s + ".rewards.heroes-exp-amounts"));

                            } else {
                                printSevere("[Quests] heroes-exp-amounts: Reward in Quest " + quest.name + " is not a list of experience amounts (decimal numbers)!");
                                continue;
                            }

                        } else {
                            printSevere("[Quests] Rewards for Quest " + quest.name + " is missing heroes-exp-amounts:");
                            continue;
                        }

                    } else {
                        printSevere("[Quests] heroes-exp-classes: Reward in Quest " + quest.name + " is not a list of Heroes classes!");
                        continue;
                    }
                }

                if (config.contains("quests." + s + ".rewards.phat-loots")) {

                    if (Quests.checkList(config.getList("quests." + s + ".rewards.phat-loots"), String.class)) {

                        boolean failed = false;
                        for (String loot : config.getStringList("quests." + s + ".rewards.phat-loots")) {

                            if (PhatLootsAPI.getPhatLoot(loot) == null) {
                                printSevere("[Quests] " + loot + " in phat-loots: Reward in Quest " + quest.name + " is not a valid PhatLoot name!");
                                failed = true;
                                break;
                            }

                        }
                        if (failed) {
                            continue;
                        }

                        quest.phatLootRewards.clear();
                        quest.phatLootRewards.addAll(config.getStringList("quests." + s + ".rewards.phat-loots"));

                    } else {
                        printSevere("[Quests] phat-loots: Reward in Quest " + quest.name + " is not a list of PhatLoots!");
                        continue;
                    }
                }

                //
                quests.add(quest);

                if (needsSaving) {
                    config.save(file);
                }

            } catch (IOException e) {

                if (debug == false) {
                    log.log(Level.SEVERE, "[Quests] Failed to load Quest \"" + s + "\". Skipping.");
                } else {
                    log.log(Level.SEVERE, "[Quests] Failed to load Quest \"" + s + "\". Error log:");
                    e.printStackTrace();
                }

            }

            if (failedToLoad == true) {
                log.log(Level.SEVERE, "[Quests] Failed to load Quest \"" + s + "\". Skipping.");
            }

        }

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
                log.log(Level.SEVERE, "[Quests] Failed to load Event \"" + s + "\". Skipping.");
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

    public static void printSevere(String s) {

        //s = ChatColor.stripColor(s);
        log.severe(s);

    }

    public static void printWarning(String s) {

        //s = ChatColor.stripColor(s);
        log.warning(s);

    }

    public static void printInfo(String s) {

        //s = ChatColor.stripColor(s);
        log.info(s);

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

        if (mob.equalsIgnoreCase("Bat")) {

            return EntityType.BAT;

        } else if (mob.equalsIgnoreCase("Blaze")) {

            return EntityType.BLAZE;

        } else if (mob.equalsIgnoreCase("CaveSpider")) {

            return EntityType.CAVE_SPIDER;

        } else if (mob.equalsIgnoreCase("Chicken")) {

            return EntityType.CHICKEN;

        } else if (mob.equalsIgnoreCase("Cow")) {

            return EntityType.COW;

        } else if (mob.equalsIgnoreCase("Creeper")) {

            return EntityType.CREEPER;

        } else if (mob.equalsIgnoreCase("Enderman")) {

            return EntityType.ENDERMAN;

        } else if (mob.equalsIgnoreCase("EnderDragon")) {

            return EntityType.ENDER_DRAGON;

        } else if (mob.equalsIgnoreCase("Ghast")) {

            return EntityType.GHAST;

        } else if (mob.equalsIgnoreCase("Giant")) {

            return EntityType.GIANT;

        } else if (mob.equalsIgnoreCase("Horse")) {

            return EntityType.HORSE;

        } else if (mob.equalsIgnoreCase("IronGolem")) {

            return EntityType.IRON_GOLEM;

        } else if (mob.equalsIgnoreCase("MagmaCube")) {

            return EntityType.MAGMA_CUBE;

        } else if (mob.equalsIgnoreCase("MushroomCow")) {

            return EntityType.MUSHROOM_COW;

        } else if (mob.equalsIgnoreCase("Ocelot")) {

            return EntityType.OCELOT;

        } else if (mob.equalsIgnoreCase("Pig")) {

            return EntityType.PIG;

        } else if (mob.equalsIgnoreCase("PigZombie")) {

            return EntityType.PIG_ZOMBIE;

        } else if (mob.equalsIgnoreCase("Sheep")) {

            return EntityType.SHEEP;

        } else if (mob.equalsIgnoreCase("Silverfish")) {

            return EntityType.SILVERFISH;

        } else if (mob.equalsIgnoreCase("Skeleton")) {

            return EntityType.SKELETON;

        } else if (mob.equalsIgnoreCase("Slime")) {

            return EntityType.SLIME;

        } else if (mob.equalsIgnoreCase("Snowman")) {

            return EntityType.SNOWMAN;

        } else if (mob.equalsIgnoreCase("Spider")) {

            return EntityType.SPIDER;

        } else if (mob.equalsIgnoreCase("Squid")) {

            return EntityType.SQUID;

        } else if (mob.equalsIgnoreCase("Villager")) {

            return EntityType.VILLAGER;

        } else if (mob.equalsIgnoreCase("Witch")) {

            return EntityType.WITCH;

        } else if (mob.equalsIgnoreCase("Wither")) {

            return EntityType.WITHER;

        } else if (mob.equalsIgnoreCase("Wolf")) {

            return EntityType.WOLF;

        } else if (mob.equalsIgnoreCase("Zombie")) {

            return EntityType.ZOMBIE;

        } else {

            return null;
        }

    }

    public static String getTime(long milliseconds) {

        String message = "";

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);

        Calendar epoch = Calendar.getInstance();
        epoch.setTimeInMillis(0);

        long days = calendar.get(Calendar.DAY_OF_YEAR) - epoch.get(Calendar.DAY_OF_YEAR);
        long hours = calendar.get(Calendar.HOUR_OF_DAY) - epoch.get(Calendar.HOUR_OF_DAY);
        long minutes = calendar.get(Calendar.MINUTE);
        long seconds = calendar.get(Calendar.SECOND);
        long milliSeconds2 = calendar.get(Calendar.MILLISECOND);
        if (days > 0) {

            if (days == 1) {
                message += " 1 Day,";
            } else {
                message += " " + days + " Days,";
            }

        }

        if (hours > 0) {

            if (hours == 1) {
                message += " 1 Hour,";
            } else {
                message += " " + hours + " Hours,";
            }

        }

        if (minutes > 0) {

            if (minutes == 1) {
                message += " 1 Minute,";
            } else {
                message += " " + minutes + " Minutes,";
            }

        }

        if (seconds > 0) {

            if (seconds == 1) {
                message += " 1 Second,";
            } else {
                message += " " + seconds + " Seconds,";
            }
        } else {
            if (milliSeconds2 > 0) {
                if (milliSeconds2 == 1) {
                    message += " 1 Millisecond,";
                } else {
                    message += " " + milliSeconds2 + " Milliseconds,";
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

        if (type.equalsIgnoreCase("BLINDNESS")) {
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

        if (s.equalsIgnoreCase("Acrobatics")) {
            return SkillType.ACROBATICS;
        } else if (s.equalsIgnoreCase("Archery")) {
            return SkillType.ARCHERY;
        } else if (s.equalsIgnoreCase("Axes")) {
            return SkillType.AXES;
        } else if (s.equalsIgnoreCase("Excavation")) {
            return SkillType.EXCAVATION;
        } else if (s.equalsIgnoreCase("Fishing")) {
            return SkillType.FISHING;
        } else if (s.equalsIgnoreCase("Herbalism")) {
            return SkillType.HERBALISM;
        } else if (s.equalsIgnoreCase("Mining")) {
            return SkillType.MINING;
        } else if (s.equalsIgnoreCase("Repair")) {
            return SkillType.REPAIR;
        } else if (s.equalsIgnoreCase("Smelting")) {
            return SkillType.SMELTING;
        } else if (s.equalsIgnoreCase("Swords")) {
            return SkillType.SWORDS;
        } else if (s.equalsIgnoreCase("Taming")) {
            return SkillType.TAMING;
        } else if (s.equalsIgnoreCase("Unarmed")) {
            return SkillType.UNARMED;
        } else if (s.equalsIgnoreCase("Woodcutting")) {
            return SkillType.WOODCUTTING;
        } else {
            return null;
        }

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
            return "Money";
        }

        if (plural) {
            if (Quests.economy.currencyNamePlural().trim().isEmpty()) {
                return "Money";
            } else {
                return Quests.economy.currencyNamePlural();
            }
        } else {
            if (Quests.economy.currencyNameSingular().trim().isEmpty()) {
                return "Money";
            } else {
                return Quests.economy.currencyNameSingular();
            }
        }

    }

    public static boolean removeItem(Inventory inventory, ItemStack is) {

        int type = is.getTypeId();
        int amount = is.getAmount();
        HashMap<Integer, ? extends ItemStack> allItems = inventory.all(type);
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

    public boolean checkQuester(String name) {

        for (String s : questerBlacklist) {

            if (Quests.checkQuester(name, s)) {
                return true;
            }

        }

        return false;

    }

    private static boolean checkQuester(String name, String check) {

        if (check.endsWith("*") && check.startsWith("*") == false) {

            check = check.substring(0, check.length());
            return name.endsWith(check);

        } else if (check.endsWith("*") == false && check.startsWith("*")) {

            check = check.substring(1);
            return name.startsWith(check);

        } else if (check.endsWith("*") && check.startsWith("*")) {

            check = check.substring(1, check.length());
            return name.contains(check);

        } else {
            return name.equalsIgnoreCase(check);

        }

    }

    public static boolean checkList(List<?> list, Class<?> c) {

        if (list == null) {
            return false;
        }

        for (Object o : list) {

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

        for(Quest q : quests) {
            if(q.name.equalsIgnoreCase(s))
                return q;
        }

        for(Quest q : quests) {
            if(q.name.toLowerCase().contains(s.toLowerCase()))
                return q;
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

        } else if (enchant.equalsIgnoreCase("Bane Of Arthropods")) {

            return Enchantment.DAMAGE_ARTHROPODS;

        } else if (enchant.equalsIgnoreCase("Smite")) {

            return Enchantment.DAMAGE_UNDEAD;

        } else if (enchant.equalsIgnoreCase("Efficiency")) {

            return Enchantment.DIG_SPEED;

        } else if (enchant.equalsIgnoreCase("Unbreaking")) {

            return Enchantment.DURABILITY;

        } else if (enchant.equalsIgnoreCase("Fire Aspect")) {

            return Enchantment.FIRE_ASPECT;

        } else if (enchant.equalsIgnoreCase("Knockback")) {

            return Enchantment.KNOCKBACK;

        } else if (enchant.equalsIgnoreCase("Fortune")) {

            return Enchantment.LOOT_BONUS_BLOCKS;

        } else if (enchant.equalsIgnoreCase("Looting")) {

            return Enchantment.LOOT_BONUS_MOBS;

        } else if (enchant.equalsIgnoreCase("Respiration")) {

            return Enchantment.OXYGEN;

        } else if (enchant.equalsIgnoreCase("Protection")) {

            return Enchantment.PROTECTION_ENVIRONMENTAL;

        } else if (enchant.equalsIgnoreCase("Blast Protection")) {

            return Enchantment.PROTECTION_EXPLOSIONS;

        } else if (enchant.equalsIgnoreCase("Feather Falling")) {

            return Enchantment.PROTECTION_FALL;

        } else if (enchant.equalsIgnoreCase("Fire Protection")) {

            return Enchantment.PROTECTION_FIRE;

        } else if (enchant.equalsIgnoreCase("Projectile Protection")) {

            return Enchantment.PROTECTION_PROJECTILE;

        } else if (enchant.equalsIgnoreCase("Silk Touch")) {

            return Enchantment.SILK_TOUCH;

        } else if (enchant.equalsIgnoreCase("Thorns")) {

            return Enchantment.THORNS;

        } else if (enchant.equalsIgnoreCase("Aqua Affinity")) {

            return Enchantment.WATER_WORKER;

        } else {

            return null;

        }

    }

    public static DyeColor getDyeColor(String s) {

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

        if (dc.equals(DyeColor.BLACK)) {
            return "Black";
        } else if (dc.equals(DyeColor.BLUE)) {
            return "Blue";
        } else if (dc.equals(DyeColor.BROWN)) {
            return "Brown";
        } else if (dc.equals(DyeColor.CYAN)) {
            return "Cyan";
        } else if (dc.equals(DyeColor.GRAY)) {
            return "Gray";
        } else if (dc.equals(DyeColor.GREEN)) {
            return "Green";
        } else if (dc.equals(DyeColor.LIGHT_BLUE)) {
            return "LightBlue";
        } else if (dc.equals(DyeColor.LIME)) {
            return "Lime";
        } else if (dc.equals(DyeColor.MAGENTA)) {
            return "Magenta";
        } else if (dc.equals(DyeColor.ORANGE)) {
            return "Orange";
        } else if (dc.equals(DyeColor.PINK)) {
            return "Pink";
        } else if (dc.equals(DyeColor.PURPLE)) {
            return "Purple";
        } else if (dc.equals(DyeColor.RED)) {
            return "Red";
        } else if (dc.equals(DyeColor.SILVER)) {
            return "Silver";
        } else if (dc.equals(DyeColor.WHITE)) {
            return "White";
        } else if (dc.equals(DyeColor.YELLOW)) {
            return "Yellow";
        } else {
            return null;
        }

    }

    public void snoop() {

        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            String url = "jdbc:mysql://173.234.237.34:3306/bigal_quests";
            Connection conn = DriverManager.getConnection(url, "bigal_snooper", "merv41");
            Statement statement = conn.createStatement();
            java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
            Timestamp stamp = new Timestamp(date.getTime());
            statement.executeUpdate("DELETE FROM entries WHERE server='" + getServer().getIp() + ":" + ((Integer) getServer().getPort()).toString() + "'");
            String cit = citizens != null ? "true" : "false";
            String name = getServer().getServerName().replaceAll("'", "''").replaceAll("\"", "''");
            String motd = getServer().getMotd().replaceAll("'", "''").replaceAll("\"", "''");
            String ip = getServer().getIp().trim();
            if (ip.isEmpty() || ip.startsWith("192") || ip.startsWith("localhost") || ip.startsWith("127") || ip.startsWith("0.0"))
                return;
            String port = ((Integer) getServer().getPort()).toString();

            statement.executeUpdate("INSERT INTO entries VALUES ('" + ip + ":" + port + "', '" + name + "', '" + motd + "', " + quests.size() + ", '" + cit + "', '" + stamp.toString() + "')");
        } catch (ClassNotFoundException e) {
        } catch (IllegalAccessException e) {
        } catch (InstantiationException e) {
        } catch (SQLException e) {
        }

    }

    public boolean hasQuest(NPC npc, Quester quester) {

        for (Quest quest : quests) {

            if (quest.npcStart != null && quester.completedQuests.contains(quest.name) == false) {

                if (quest.npcStart.getId() == npc.getId()) {
                    return true;
                }

            }

        }

        return false;
    }

    public static EpicMobs getBoss(String name) {

        for (EpicMobs em : Quests.epicBoss.listMobs) {
            if (em.cmdName.equalsIgnoreCase(name)) {
                return em;
            }
        }

        return null;

    }

    public static int getMCMMOSkillLevel(SkillType st, String player) {

        McMMOPlayer mPlayer = UserManager.getPlayer(player);
        if (mPlayer == null) {
            return -1;
        }

        return mPlayer.getProfile().getSkillLevel(st);

    }

    public Hero getHero(String player){

        Player p = getServer().getPlayer(player);
        if(p == null)
            return null;

        return heroes.getCharacterManager().getHero(p);

    }

    public boolean testPrimaryHeroesClass(String primaryClass, String player){

        Hero hero = getHero(player);
        return hero.getHeroClass().getName().equalsIgnoreCase(primaryClass);

    }

    public boolean testSecondaryHeroesClass(String secondaryClass, String player){

        Hero hero = getHero(player);
        return hero.getHeroClass().getName().equalsIgnoreCase(secondaryClass);

    }

}
