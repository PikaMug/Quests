package me.blackvein.quests;

import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.mcMMO;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.citizensnpcs.api.CitizensPlugin;
import net.citizensnpcs.api.npc.NPC;
import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.conversations.*;
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

public class Quests extends JavaPlugin implements ConversationAbandonedListener {

    public static Economy economy = null;
    public static Permission permission = null;
    public static mcMMO mcmmo = null;
    public static boolean snoop = true;
    List<String> questerBlacklist = new LinkedList<String>();
    ConversationFactory conversationFactory;
    QuestFactory questFactory;
    EventFactory eventFactory;
    Vault vault = null;
    public CitizensPlugin citizens;
    PlayerListener pListener;
    NpcListener npcListener;
    public Denizen denizen = null;
    QuestTaskTrigger trigger;
    Map<String, Quester> questers = new HashMap<String, Quester>();
    LinkedList<Quest> quests = new LinkedList<Quest>();
    public LinkedList<Event> events = new LinkedList<Event>();
    LinkedList<NPC> questNPCs = new LinkedList<NPC>();
    boolean allowCommands = true;
    boolean allowCommandsForNpcQuests = false;
    boolean showQuestReqs = true;
    boolean allowQuitting = true;
    boolean debug = false;
    boolean load = false;
    int killDelay = 0;
    int totalQuestPoints = 0;
    public final static Logger log = Logger.getLogger("Minecraft");

    @Override
    public void onEnable() {

        pListener = new PlayerListener(this);
        npcListener = new NpcListener(this);

        this.conversationFactory = new ConversationFactory(this)
                .withModality(false)
                .withPrefix(new QuestsPrefix())
                .withFirstPrompt(new QuestPrompt())
                .withTimeout(20)
                .thatExcludesNonPlayersWithMessage("Console may not perform this conversation!")
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

        if (getServer().getPluginManager().getPlugin("Denizen") != null) {
            denizen = (Denizen) getServer().getPluginManager().getPlugin("Denizen");
        }

        if (getServer().getPluginManager().getPlugin("mcMMO") != null) {
            mcmmo = (mcMMO) getServer().getPluginManager().getPlugin("mcMMO");
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
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        loadConfig();

        if (new File(this.getDataFolder(), "quests.yml").exists() == false) {
            printInfo("[Quests] Quest data not found, writing default to file.");
            FileConfiguration data = new YamlConfiguration();
            try {
                data.load(this.getResource("quests.yml"));
                data.set("events", null);
                data.save(new File(this.getDataFolder(), "quests.yml"));
            } catch (Exception e) {
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
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        getServer().getPluginManager().registerEvents(pListener, this);
        printInfo("[Quests] Enabled.");

        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                loadQuests();
                loadEvents();
                log.log(Level.INFO, "[Quests] " + quests.size() + " Quest(s) loaded.");
                log.log(Level.INFO, "[Quests] " + events.size() + " Event(s) loaded.");
                questers = getOnlineQuesters();
                if(snoop)
                    snoop();
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
    }

    private class QuestPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return ChatColor.YELLOW + "Accept Quest?  " + ChatColor.GREEN + "Yes / No";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String s) {

            Player player = (Player) context.getForWhom();

            if (s.equalsIgnoreCase("Yes")) {

                getQuester(player.getName()).takeQuest(getQuest(getQuester(player.getName()).questToTake));
                return Prompt.END_OF_CONVERSATION;

            } else if (s.equalsIgnoreCase("No")) {

                player.sendMessage(ChatColor.YELLOW + "Cancelled.");
                return Prompt.END_OF_CONVERSATION;

            } else {

                player.sendMessage(ChatColor.RED + "Invalid choice. Type \'Yes\' or \'No\'");
                return new QuestPrompt();

            }


        }
    }

    private class QuestsPrefix implements ConversationPrefix {

        @Override
        public String getPrefix(ConversationContext context) {

            return ChatColor.GREEN + "Quests: " + ChatColor.GRAY;

        }
    }

    public void loadConfig() {

        FileConfiguration config = getConfig();

        allowCommands = config.getBoolean("allow-command-questing");
        allowCommandsForNpcQuests = config.getBoolean("allow-command-quests-with-npcs");
        showQuestReqs = config.getBoolean("show-requirements");
        allowQuitting = config.getBoolean("allow-quitting");
        snoop = config.getBoolean("snoop", true);
        debug = config.getBoolean("debug-mode");
        killDelay = config.getInt("kill-delay");
        for (String s : config.getStringList("quester-blacklist")) {

            questerBlacklist.add(s);

        }

    }

    public void printHelp(Player player) {

        player.sendMessage(ChatColor.GOLD + "- Quests -");
        player.sendMessage(ChatColor.YELLOW + "/quests - Display this help");
        if (player.hasPermission("quests.list")) {
            player.sendMessage(ChatColor.YELLOW + "/quests list <page> - List available Quests");
        }
        if (player.hasPermission("quests.take")) {
            player.sendMessage(ChatColor.YELLOW + "/quests take <quest name> - Accept a Quest");
        }
        if (player.hasPermission("quests.quit")) {
            player.sendMessage(ChatColor.YELLOW + "/quests quit - Quit your current Quest");
        }
        if (player.hasPermission("quests.editor.create")) {
            player.sendMessage(ChatColor.YELLOW + "/quests create - Create a Quest");
        }
        if (player.hasPermission("quests.editor.events")) {
            player.sendMessage(ChatColor.YELLOW + "/quests events - Create/Edit Events");
        }
        if (player.hasPermission("quests.stats")) {
            player.sendMessage(ChatColor.YELLOW + "/quests stats - View your Questing stats");
        }
        if (player.hasPermission("quests.top")) {
            player.sendMessage(ChatColor.YELLOW + "/quests top <number> - View top Questers");
        }
        player.sendMessage(ChatColor.YELLOW + "/quests info - Display plugin information");
        player.sendMessage(" ");
        player.sendMessage(ChatColor.YELLOW + "/quest - Display current Quest objectives");
        if (player.hasPermission("quests.questinfo")) {
            player.sendMessage(ChatColor.YELLOW + "/quest <quest name> - Display Quest information");
        }
        if (player.hasPermission("quests.admin")) {
            player.sendMessage(ChatColor.DARK_RED + "/questadmin " + ChatColor.RED + "- Questadmin help");
        }

    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("quest")) {

            if (cs instanceof Player) {

                if (((Player) cs).hasPermission("quests.quest")) {

                    if (args.length == 0) {

                        if (getQuester(cs.getName()).currentQuest != null) {

                            if (getQuester(cs.getName()).delayStartTime == 0) {
                                cs.sendMessage(ChatColor.GOLD + "---(Objectives)---");
                            }

                            for (String s : getQuester(cs.getName()).getObjectives()) {

                                cs.sendMessage(s);

                            }

                        } else {

                            cs.sendMessage(ChatColor.YELLOW + "You do not currently have an active Quest.");
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

                            Quest quest = null;

                            for (Quest q : quests) {

                                if (q.name.toLowerCase().contains(name)) {
                                    quest = q;
                                    break;
                                }

                            }

                            if (quest != null) {

                                Player player = (Player) cs;
                                Quester quester = getQuester(player.getName());

                                cs.sendMessage(ChatColor.GOLD + "- " + quest.name + " -");
                                cs.sendMessage(" ");
                                if (quest.redoDelay > -1) {

                                    if (quest.redoDelay == 0) {
                                        cs.sendMessage(ChatColor.DARK_AQUA + "Redoable");
                                    } else {
                                        cs.sendMessage(ChatColor.DARK_AQUA + "Redoable every " + ChatColor.AQUA + getTime(quest.redoDelay) + ChatColor.DARK_AQUA + ".");
                                    }

                                }
                                if (quest.npcStart != null) {
                                    cs.sendMessage(ChatColor.YELLOW + "Start: Speak to " + quest.npcStart.getName());
                                } else {
                                    cs.sendMessage(ChatColor.YELLOW + quest.description);
                                }

                                cs.sendMessage(" ");

                                if (showQuestReqs == true) {

                                    cs.sendMessage(ChatColor.GOLD + "Requirements");

                                    if (quest.permissionReqs.isEmpty() == false) {

                                        for (String perm : quest.permissionReqs) {

                                            if (permission.has(player, perm)) {
                                                cs.sendMessage(ChatColor.GREEN + "Permission: " + perm);
                                            } else {
                                                cs.sendMessage(ChatColor.RED + "Permission: " + perm);
                                            }

                                        }

                                    }

                                    if (quest.questPointsReq != 0) {

                                        if (quester.questPoints >= quest.questPointsReq) {
                                            cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.GREEN + quest.questPointsReq + " Quest Points");
                                        } else {
                                            cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.RED + quest.questPointsReq + " Quest Points");
                                        }

                                    }

                                    if (quest.moneyReq != 0) {

                                        if (economy.getBalance(quester.name) >= quest.moneyReq) {
                                            if (quest.moneyReq == 1) {
                                                cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.GREEN + quest.moneyReq + " " + Quests.getCurrency(false));
                                            } else {
                                                cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.GREEN + quest.moneyReq + " " + Quests.getCurrency(true));
                                            }
                                        } else {
                                            if (quest.moneyReq == 1) {
                                                cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.RED + quest.moneyReq + " " + Quests.getCurrency(false));
                                            } else {
                                                cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.RED + quest.moneyReq + " " + Quests.getCurrency(true));
                                            }
                                        }

                                    }

                                    if (quest.itemIds.isEmpty() == false) {

                                        for (int i : quest.itemIds) {

                                            if (hasItem(player, i, quest.itemAmounts.get(quest.itemIds.indexOf(i))) == true) {
                                                cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.GREEN + Quester.prettyItemString(i) + " x " + quest.itemAmounts.get(quest.itemIds.indexOf(i)));
                                            } else {
                                                cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.RED + Quester.prettyItemString(i) + " x " + quest.itemAmounts.get(quest.itemIds.indexOf(i)));
                                            }

                                        }

                                    }

                                    if (quest.neededQuests.isEmpty() == false) {

                                        for (String s : quest.neededQuests) {

                                            if (quester.completedQuests.contains(s)) {
                                                cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.GREEN + "Complete " + ChatColor.DARK_PURPLE + s);
                                            } else {
                                                cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.RED + "Complete " + ChatColor.DARK_PURPLE + s);
                                            }

                                        }

                                    }

                                }

                            } else {

                                cs.sendMessage(ChatColor.YELLOW + "Quest not found.");
                                return true;

                            }

                        } else {

                            cs.sendMessage(ChatColor.RED + "You do not have permission to view a Quest's information.");
                            return true;

                        }

                    }

                } else {

                    cs.sendMessage(ChatColor.RED + "You do not have access to that command.");
                    return true;

                }

            } else {

                cs.sendMessage(ChatColor.YELLOW + "This command may only be performed in-game.");
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

                        cs.sendMessage(ChatColor.RED + "You do not have access to that command.");
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

                                        cs.sendMessage(ChatColor.YELLOW + "Page selection must be a positive number.");
                                        return true;

                                    }

                                } catch (Exception e) {

                                    cs.sendMessage(ChatColor.YELLOW + "Page selection must be a number.");
                                    return true;

                                }

                                listQuests((Player) cs, page);
                                return true;

                            }

                        } else {

                            cs.sendMessage(ChatColor.RED + "You do not have permission to view the Quests list.");
                            return true;

                        }

                    } else if (args[0].equalsIgnoreCase("take")) {

                        if (allowCommands == true) {

                            if (((Player) cs).hasPermission("quests.take")) {

                                if (args.length == 1) {

                                    cs.sendMessage(ChatColor.YELLOW + "Usage: /quests take <quest>");
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

                                    Quest questToFind = null;

                                    for (Quest q : quests) {

                                        if (q.name.toLowerCase().contains(name)) {
                                            questToFind = q;
                                            break;
                                        }

                                    }

                                    if (questToFind != null) {

                                        final Quest quest = questToFind;
                                        final Quester quester = getQuester(cs.getName());

                                        if (quester.currentQuest != null) {
                                            cs.sendMessage(ChatColor.YELLOW + "You may only have one active Quest.");
                                        } else if (quester.completedQuests.contains(quest.name) && quest.redoDelay < 0) {
                                            cs.sendMessage(ChatColor.YELLOW + "You have already completed " + ChatColor.DARK_PURPLE + quest.name + ChatColor.YELLOW + ".");
                                        } else if (quest.npcStart != null && allowCommandsForNpcQuests == false) {
                                            cs.sendMessage(ChatColor.YELLOW + "You must speak to " + ChatColor.DARK_PURPLE + quest.npcStart.getName() + ChatColor.YELLOW + " to start this Quest.");
                                        } else if (quest.blockStart != null) {
                                            cs.sendMessage(ChatColor.DARK_PURPLE + quest.name + ChatColor.YELLOW + " may not be started via command.");
                                        } else {

                                            boolean takeable = true;
                                            if (quester.completedQuests.contains(quest.name)) {

                                                if (quester.getDifference(quest) > 0) {
                                                    cs.sendMessage(ChatColor.YELLOW + "You may not take " + ChatColor.AQUA + quest.name + ChatColor.YELLOW + " again for another " + ChatColor.DARK_PURPLE + getTime(quester.getDifference(quest)) + ChatColor.YELLOW + ".");
                                                    takeable = false;
                                                }

                                            }

                                            if (takeable == true) {

                                                if (cs instanceof Conversable) {

                                                    quester.questToTake = quest.name;

                                                    String s =
                                                            ChatColor.GOLD + "- " + ChatColor.DARK_PURPLE + quester.questToTake + ChatColor.GOLD + " -\n"
                                                            + "\n"
                                                            + ChatColor.RESET + getQuest(quester.questToTake).description + "\n";

                                                    cs.sendMessage(s);
                                                    conversationFactory.buildConversation((Conversable) cs).begin();
                                                    return true;
                                                } else {
                                                    return false;
                                                }

                                            }

                                        }

                                    } else {
                                        cs.sendMessage(ChatColor.YELLOW + "Quest not found.");
                                        return true;
                                    }

                                }

                            } else {

                                cs.sendMessage(ChatColor.RED + "You do not have permission to take Quests via commands.");
                                return true;

                            }

                        } else {

                            cs.sendMessage(ChatColor.YELLOW + "Taking Quests via commands has been disabled.");
                            return true;

                        }

                    } else if (args[0].equalsIgnoreCase("quit")) {

                        if (allowQuitting == true) {

                            if (((Player) cs).hasPermission("quests.quit")) {

                                Quester quester = getQuester(cs.getName());
                                if (quester.currentQuest != null) {

                                    quester.reset();
                                    quester.currentStage = null;
                                    cs.sendMessage(ChatColor.YELLOW + "You have quit " + ChatColor.DARK_PURPLE + quester.currentQuest.name + ChatColor.YELLOW + ".");
                                    quester.currentQuest = null;
                                    return true;

                                } else {

                                    cs.sendMessage(ChatColor.YELLOW + "You do not currently have an active Quest.");
                                    return true;

                                }

                            } else {

                                cs.sendMessage(ChatColor.RED + "You do not have permission to quit Quests.");
                                return true;
                            }

                        } else {

                            cs.sendMessage(ChatColor.YELLOW + "Quitting Quests has been disabled.");
                            return true;

                        }

                    } else if (args[0].equalsIgnoreCase("stats")) {

                        Quester quester = getQuester(cs.getName());
                        cs.sendMessage(ChatColor.GOLD + "- " + cs.getName() + " -");
                        cs.sendMessage(ChatColor.YELLOW + "Quest points: " + ChatColor.DARK_PURPLE + quester.questPoints + "/" + totalQuestPoints);
                        if (quester.currentQuest == null) {
                            cs.sendMessage(ChatColor.YELLOW + "Current Quest: " + ChatColor.DARK_PURPLE + "None");
                        } else {
                            cs.sendMessage(ChatColor.YELLOW + "Current Quest: " + ChatColor.DARK_PURPLE + quester.currentQuest.name);
                        }

                        String completed;

                        if (quester.completedQuests.isEmpty()) {
                            completed = ChatColor.DARK_PURPLE + "None";
                        } else {

                            completed = ChatColor.DARK_PURPLE + "";
                            for (String s : quester.completedQuests) {

                                if (quester.completedQuests.indexOf(s) < (quester.completedQuests.size() - 1)) {
                                    completed = completed + s + ", ";
                                } else {
                                    completed = completed + s;
                                }

                            }

                        }

                        cs.sendMessage(ChatColor.YELLOW + "- Completed Quests -");
                        cs.sendMessage(completed);

                    } else if (args[0].equalsIgnoreCase("top")) {

                        if (args.length == 1 || args.length > 2) {

                            cs.sendMessage(ChatColor.YELLOW + "Usage: /quests top <number>");

                        } else {

                            int topNumber;

                            try {

                                topNumber = Integer.parseInt(args[1]);

                            } catch (Exception e) {

                                cs.sendMessage(ChatColor.YELLOW + "Input must be a number.");
                                return true;

                            }

                            if (topNumber < 1) {

                                cs.sendMessage(ChatColor.YELLOW + "Input must be a positive number.");
                                return true;

                            }

                            File folder = new File(this.getDataFolder(), "data");
                            File[] playerFiles = folder.listFiles();

                            Map<String, Integer> questPoints = new HashMap<String, Integer>();

                            for (File f : playerFiles) {

                                FileConfiguration data = new YamlConfiguration();
                                try {

                                    data.load(f);

                                } catch (Exception e) {

                                    e.printStackTrace();

                                }

                                String name = f.getName().substring(0, (f.getName().indexOf(".")));
                                questPoints.put(name, data.getInt("quest-points"));

                            }

                            LinkedHashMap sortedMap = (LinkedHashMap) Quests.sort(questPoints);

                            int numPrinted = 0;

                            cs.sendMessage(ChatColor.GOLD + "- Top " + ChatColor.DARK_PURPLE + topNumber + ChatColor.GOLD + " Questers -");
                            for (Object o : sortedMap.keySet()) {

                                String s = (String) o;
                                int i = (Integer) sortedMap.get(o);
                                numPrinted++;
                                cs.sendMessage(ChatColor.YELLOW + String.valueOf(numPrinted) + ". " + s + " - " + ChatColor.DARK_PURPLE + i + ChatColor.YELLOW + " Quest points");

                                if (numPrinted == topNumber) {
                                    break;
                                }


                            }


                        }

                        return true;

                    } else if (args[0].equalsIgnoreCase("create")) {

                        if (cs.hasPermission("quests.editor.create")) {
                            questFactory.convoCreator.buildConversation((Conversable) cs).begin();
                        } else {
                            cs.sendMessage(ChatColor.RED + "You do not have permission to create Quests.");
                        }
                        return true;

                    } else if (args[0].equalsIgnoreCase("events")) {

                        if (cs.hasPermission("quests.editor.events")) {
                            eventFactory.convoCreator.buildConversation((Conversable) cs).begin();
                        } else {
                            cs.sendMessage(ChatColor.RED + "You do not have permission to use the Events editor.");
                        }
                        return true;

                    } else if (args[0].equalsIgnoreCase("info")) {

                        cs.sendMessage(ChatColor.GOLD + "Quests " + this.getDescription().getVersion());
                        cs.sendMessage(ChatColor.GOLD + "Made by " + ChatColor.DARK_RED + "Blackvein");
                        return true;

                    } else {

                        cs.sendMessage(ChatColor.YELLOW + "Unknown Quests command. Type /quests for help.");
                        return true;

                    }

                }

            } else {

                cs.sendMessage(ChatColor.YELLOW + "This command may only be performed in-game.");
                return true;

            }

            return true;

        } else if (cmd.getName().equalsIgnoreCase("questadmin")) {

            if (cs instanceof Player || args.length == 1 && args[0].equalsIgnoreCase("reload")) {

                Player player = null;
                if (cs instanceof Player) {
                    player = (Player) cs;
                }

                if (args.length == 0) {

                    if (player.hasPermission("quests.admin")) {
                        printAdminHelp(player);
                    } else {
                        player.sendMessage(ChatColor.RED + "You do not have access to that command.");
                    }

                } else if (args.length == 1) {

                    if (args[0].equalsIgnoreCase("reload")) {

                        if (player == null || player.hasPermission("quests.admin.reload")) {
                            reloadQuests();
                            cs.sendMessage(ChatColor.GOLD + "Quests reloaded.");
                            cs.sendMessage(ChatColor.DARK_PURPLE + String.valueOf(quests.size()) + ChatColor.GOLD + " Quests loaded.");
                        } else {
                            cs.sendMessage(ChatColor.RED + "You do not have access to that command.");
                        }

                    } else {

                        cs.sendMessage(ChatColor.YELLOW + "Unknown Questadmin command. Type /questadmin for help.");

                    }

                } else if (args.length == 2) {

                    if (args[0].equalsIgnoreCase("quit")) {

                        if (player.hasPermission("quests.admin.quit")) {

                            Player target = null;

                            for (Player p : getServer().getOnlinePlayers()) {

                                if (p.getName().toLowerCase().contains(args[1].toLowerCase())) {
                                    target = p;
                                    break;
                                }

                            }

                            if (target == null) {

                                player.sendMessage(ChatColor.YELLOW + "Player not found.");

                            } else {

                                Quester quester = getQuester(target.getName());
                                if (quester.currentQuest == null) {

                                    player.sendMessage(ChatColor.YELLOW + target.getName() + " does not currently have an active Quest.");

                                } else {

                                    quester.reset();
                                    quester.currentStage = null;
                                    player.sendMessage(ChatColor.GREEN + target.getName() + ChatColor.GOLD + " has forcibly quit the Quest " + ChatColor.DARK_PURPLE + quester.currentQuest.name + ChatColor.GOLD + ".");
                                    target.sendMessage(ChatColor.GREEN + player.getName() + ChatColor.GOLD + " has forced you to quit the Quest " + ChatColor.DARK_PURPLE + quester.currentQuest.name + ChatColor.GOLD + ".");
                                    quester.currentQuest = null;

                                    quester.saveData();

                                }

                            }

                        } else {

                            player.sendMessage(ChatColor.RED + "You do not have access to that command.");

                        }

                    } else if (args[0].equalsIgnoreCase("nextstage")) {

                        if (player.hasPermission("quests.admin.nextstage")) {

                            Player target = null;

                            for (Player p : getServer().getOnlinePlayers()) {

                                if (p.getName().toLowerCase().contains(args[1].toLowerCase())) {
                                    target = p;
                                    break;
                                }

                            }

                            if (target == null) {

                                player.sendMessage(ChatColor.YELLOW + "Player not found.");

                            } else {

                                Quester quester = getQuester(target.getName());
                                if (quester.currentQuest == null) {

                                    player.sendMessage(ChatColor.YELLOW + target.getName() + " does not currently have an active Quest.");

                                } else {

                                    player.sendMessage(ChatColor.GREEN + target.getName() + ChatColor.GOLD + " has advanced to the next Stage in the Quest " + ChatColor.DARK_PURPLE + quester.currentQuest.name + ChatColor.GOLD + ".");
                                    target.sendMessage(ChatColor.GREEN + player.getName() + ChatColor.GOLD + " has advanced you to the next Stage in your Quest " + ChatColor.DARK_PURPLE + quester.currentQuest.name + ChatColor.GOLD + ".");
                                    quester.currentQuest.nextStage(quester);

                                    quester.saveData();

                                }

                            }

                        } else {

                            player.sendMessage(ChatColor.RED + "You do not have access to that command.");

                        }

                    } else if (args[0].equalsIgnoreCase("finish")) {

                        if (player.hasPermission("quests.admin.finish")) {

                            Player target = null;

                            for (Player p : getServer().getOnlinePlayers()) {

                                if (p.getName().toLowerCase().contains(args[1].toLowerCase())) {
                                    target = p;
                                    break;
                                }

                            }

                            if (target == null) {

                                player.sendMessage(ChatColor.YELLOW + "Player not found.");

                            } else {

                                Quester quester = getQuester(target.getName());
                                if (quester.currentQuest == null) {

                                    player.sendMessage(ChatColor.YELLOW + target.getName() + " does not currently have an active Quest.");

                                } else {

                                    player.sendMessage(ChatColor.GREEN + target.getName() + ChatColor.GOLD + " has advanced to the next Stage in the Quest " + ChatColor.DARK_PURPLE + quester.currentQuest.name + ChatColor.GOLD + ".");
                                    target.sendMessage(ChatColor.GREEN + player.getName() + ChatColor.GOLD + " has advanced you to the next Stage in your Quest " + ChatColor.DARK_PURPLE + quester.currentQuest.name + ChatColor.GOLD + ".");
                                    quester.currentQuest.completeQuest(quester);

                                    quester.saveData();

                                }

                            }

                        } else {

                            player.sendMessage(ChatColor.RED + "You do not have access to that command.");

                        }

                    }

                } else {

                    if (args[0].equalsIgnoreCase("give")) {

                        if (player.hasPermission("quests.admin.give")) {

                            Player target = null;

                            for (Player p : getServer().getOnlinePlayers()) {

                                if (p.getName().toLowerCase().contains(args[1].toLowerCase())) {
                                    target = p;
                                    break;
                                }

                            }

                            if (target == null) {

                                player.sendMessage(ChatColor.YELLOW + "Player not found.");

                            } else {

                                Quest questToGive = null;

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

                                for (Quest q : quests) {

                                    if (q.name.toLowerCase().contains(name)) {
                                        questToGive = q;
                                        break;
                                    }

                                }

                                if (questToGive == null) {

                                    player.sendMessage(ChatColor.YELLOW + "Quest not found.");

                                } else {

                                    Quester quester = getQuester(target.getName());

                                    quester.reset();

                                    quester.currentQuest = questToGive;
                                    quester.currentStage = questToGive.stages.getFirst();
                                    quester.addEmpties();
                                    player.sendMessage(ChatColor.GREEN + target.getName() + ChatColor.GOLD + " has forcibly started the Quest " + ChatColor.DARK_PURPLE + questToGive.name + ChatColor.GOLD + ".");
                                    target.sendMessage(ChatColor.GREEN + player.getName() + ChatColor.GOLD + " has forced you to take the Quest " + ChatColor.DARK_PURPLE + questToGive.name + ChatColor.GOLD + ".");
                                    target.sendMessage(ChatColor.GOLD + "---(Objectives)---");
                                    for (String s : quester.getObjectives()) {
                                        target.sendMessage(s);
                                    }

                                    quester.saveData();

                                }

                            }

                        } else {

                            player.sendMessage(ChatColor.RED + "You do not have access to that command.");

                        }

                    } else if (args[0].equalsIgnoreCase("points")) {

                        if (player.hasPermission("quests.admin.points")) {

                            Player target = null;

                            for (Player p : getServer().getOnlinePlayers()) {

                                if (p.getName().toLowerCase().contains(args[1].toLowerCase())) {
                                    target = p;
                                    break;
                                }

                            }

                            if (target == null) {

                                player.sendMessage(ChatColor.YELLOW + "Player not found.");

                            } else {

                                int points;

                                try {

                                    points = Integer.parseInt(args[2]);

                                } catch (Exception e) {

                                    player.sendMessage(ChatColor.YELLOW + "Amount must be a number.");
                                    return true;

                                }

                                Quester quester = getQuester(target.getName());
                                quester.questPoints = points;
                                player.sendMessage(ChatColor.GREEN + target.getName() + ChatColor.GOLD + "\'s Quest Points have been set to " + ChatColor.DARK_PURPLE + points + ChatColor.GOLD + ".");
                                target.sendMessage(ChatColor.GREEN + player.getName() + ChatColor.GOLD + " has set your Quest Points to " + ChatColor.DARK_PURPLE + points + ChatColor.GOLD + ".");

                                quester.saveData();

                            }

                        } else {

                            player.sendMessage(ChatColor.RED + "You do not have access to that command.");

                        }

                    }

                }

            } else {

                cs.sendMessage(ChatColor.YELLOW + "This command may only be performed in-game.");

            }

            return true;

        }


        return false;

    }

    public void printAdminHelp(Player player) {

        player.sendMessage(ChatColor.RED + "- " + ChatColor.DARK_RED + "Questadmin" + ChatColor.RED + " -");
        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_RED + "/questadmin" + ChatColor.RED + " - View Questadmin help");
        if (player.hasPermission("quests.admin.give")) {
            player.sendMessage(ChatColor.DARK_RED + "/questadmin give <player> <quest>" + ChatColor.RED + " - Force a player to take a Quest");
        }
        if (player.hasPermission("quests.admin.quit")) {
            player.sendMessage(ChatColor.DARK_RED + "/questadmin quit <player>" + ChatColor.RED + " - Force a player to quit their Quest");
        }
        if (player.hasPermission("quests.admin.points")) {
            player.sendMessage(ChatColor.DARK_RED + "/questadmin points <player> <amount>" + ChatColor.RED + " - Set a players Quest Points");
        }
        if (player.hasPermission("quests.admin.finish")) {
            player.sendMessage(ChatColor.DARK_RED + "/questadmin finish <player>" + ChatColor.RED + " - Immediately force Quest completion for a player");
        }
        if (player.hasPermission("quests.admin.nextstage")) {
            player.sendMessage(ChatColor.DARK_RED + "/questadmin nextstage <player>" + ChatColor.RED + " - Immediately force Stage completion for a player");
        }
        if (player.hasPermission("quests.admin.reload")) {
            player.sendMessage(ChatColor.DARK_RED + "/questadmin reload" + ChatColor.RED + " - Reload all Quests");
        }

    }

    public void listQuests(Player player, int page) {

        if (quests.size() < ((page * 8) - 7)) {
            player.sendMessage(ChatColor.YELLOW + "Page does not exist.");
        } else {
            player.sendMessage(ChatColor.GOLD + "- Quests -");

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
            } else if (quests.size() >= (numOrder + 6)) {
                subQuests = quests.subList((numOrder - 1), (numOrder + 7));
            } else {
                subQuests = quests.subList((numOrder - 1), quests.size());
            }

            if (numOrder != 1) {
                numOrder++;
            }

            for (Quest quest : subQuests) {

                player.sendMessage(ChatColor.YELLOW + Integer.toString(numOrder) + ". " + quest.name);
                numOrder++;

            }

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
                System.out.println("Saving data.");
                quester.saveData();
            } else {
                System.out.println("Loaded data.");
            }
            qs.put(p.getName(), quester);

        }

        return qs;

    }

    public void loadQuests() {

        FileConfiguration config = new YamlConfiguration();
        try {

            config.load(new File(this.getDataFolder(), "quests.yml"));

        } catch (Exception e) {
            e.printStackTrace();
        }

        ConfigurationSection section1 = config.getConfigurationSection("quests");
        boolean failedToLoad = false;
        totalQuestPoints = 0;
        boolean firstStage = true;
        for (String s : section1.getKeys(false)) {

            try {

                Quest quest = new Quest();
                failedToLoad = false;

                if (config.contains("quests." + s + ".name")) {
                    quest.name = parseString(config.getString("quests." + s + ".name"), quest);
                } else {
                    printSevere(ChatColor.GOLD + "[Quests] Quest block \'" + ChatColor.DARK_PURPLE + s + ChatColor.GOLD + "\' is missing " + ChatColor.RED + "name:");
                    continue;
                }

                if (config.contains("quests." + s + ".npc-giver-id")) {

                    if (citizens.getNPCRegistry().getById(config.getInt("quests." + s + ".npc-giver-id")) != null) {

                        quest.npcStart = citizens.getNPCRegistry().getById(config.getInt("quests." + s + ".npc-giver-id"));
                        questNPCs.add(citizens.getNPCRegistry().getById(config.getInt("quests." + s + ".npc-giver-id")));

                    } else {
                        printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "npc-giver-id: " + ChatColor.GOLD + "for Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a valid NPC id!");
                        continue;
                    }

                }

                if (config.contains("quests." + s + ".block-start")) {

                    Location location = getLocation(config.getString("quests." + s + ".block-start"));
                    if (location != null) {
                        quest.blockStart = location;
                    } else {
                        printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "block-start: " + ChatColor.GOLD + "for Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not in proper location format!");
                        printSevere(ChatColor.GOLD + "[Quests] Proper location format is: \"WorldName x y z\"");
                        continue;
                    }

                }

                if (config.contains("quests." + s + ".redo-delay")) {

                    if (config.getInt("quests." + s + ".redo-delay", -999) != -999) {
                        quest.redoDelay = config.getInt("quests." + s + ".redo-delay");
                    } else {
                        printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "redo-delay: " + ChatColor.GOLD + "for Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a number!");
                        continue;
                    }

                }

                if (config.contains("quests." + s + ".finish-message")) {
                    quest.finished = parseString(config.getString("quests." + s + ".finish-message"), quest);
                } else {
                    printSevere(ChatColor.GOLD + "[Quests] Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is missing " + ChatColor.RED + "finish-message:");
                    continue;
                }

                if (config.contains("quests." + s + ".ask-message")) {
                    quest.description = parseString(config.getString("quests." + s + ".ask-message"), quest);
                } else {
                    printSevere(ChatColor.GOLD + "[Quests] Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is missing " + ChatColor.RED + "ask-message:");
                    continue;
                }

                if (config.contains("quests." + s + ".requirements")) {

                    if (config.contains("quests." + s + ".requirements.fail-requirement-message")) {
                        quest.failRequirements = parseString(config.getString("quests." + s + ".requirements.fail-requirement-message"), quest);
                    } else {
                        printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.YELLOW + "Requirements " + ChatColor.GOLD + "for Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is missing " + ChatColor.RED + "fail-requirement-message:");
                        continue;
                    }

                    if (config.contains("quests." + s + ".requirements.item-ids")) {

                        if (Quests.checkList(config.getList("quests." + s + ".requirements.item-ids"), Integer.class)) {
                            quest.itemIds = config.getIntegerList("quests." + s + ".requirements.item-ids");
                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "item-ids: " + ChatColor.YELLOW + "Requirement " + ChatColor.GOLD + "for Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of numbers!");
                            continue;
                        }

                        if (config.contains("quests." + s + ".requirements.item-amounts")) {

                            if (Quests.checkList(config.getList("quests." + s + ".requirements.item-amounts"), Integer.class)) {
                                quest.itemAmounts = config.getIntegerList("quests." + s + ".requirements.item-amounts");
                            } else {
                                printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "item-amounts: " + ChatColor.YELLOW + "Requirement " + ChatColor.GOLD + "for Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of numbers!");
                            }

                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.YELLOW + "Requirements " + ChatColor.GOLD + "for Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is missing " + ChatColor.RED + "item-amounts:");
                            continue;
                        }


                        if (config.contains("quests." + s + ".requirements.remove-items")) {

                            if (Quests.checkList(config.getList("quests." + s + ".requirements.remove-items"), Boolean.class)) {
                                quest.removeItems = config.getBooleanList("quests." + s + ".requirements.remove-items");
                            } else {
                                printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "remove-items: " + ChatColor.YELLOW + "Requirement " + ChatColor.GOLD + "for Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of true/false values!");
                                continue;
                            }

                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.YELLOW + "Requirements " + ChatColor.GOLD + "for Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is missing " + ChatColor.RED + "remove-items:");
                            continue;
                        }
                    }

                    if (config.contains("quests." + s + ".requirements.money")) {

                        if (config.getInt("quests." + s + ".requirements.money", -999) != -999) {
                            quest.moneyReq = config.getInt("quests." + s + ".requirements.money");
                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "money: " + ChatColor.YELLOW + "Requirement " + ChatColor.GOLD + "for Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a number!");
                            continue;
                        }

                    }

                    if (config.contains("quests." + s + ".requirements.quest-points")) {

                        if (config.getInt("quests." + s + ".requirements.quest-points", -999) != -999) {
                            quest.questPointsReq = config.getInt("quests." + s + ".requirements.quest-points");
                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "quest-points: " + ChatColor.YELLOW + "Requirement " + ChatColor.GOLD + "for Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a number!");
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
                                printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.LIGHT_PURPLE + failedQuest + ChatColor.GOLD + " inside " + ChatColor.RED + "quests: " + ChatColor.YELLOW + "Requirement " + ChatColor.GOLD + "for Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a valid Quest name!");
                                printSevere(ChatColor.RED + "Make sure you are using the Quest " + ChatColor.DARK_RED + "name: " + ChatColor.RED + "value, and not the block name.");
                                continue;
                            }

                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "quests: " + ChatColor.YELLOW + "Requirement " + ChatColor.GOLD + "for Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of Quest names!");
                            continue;
                        }

                    }

                    if (config.contains("quests." + s + ".requirements.permissions")) {

                        if (Quests.checkList(config.getList("quests." + s + ".requirements.permissions"), String.class)) {
                            quest.permissionReqs = config.getStringList("quests." + s + ".requirements.permissions");
                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "permissions: " + ChatColor.YELLOW + "Requirement " + ChatColor.GOLD + "for Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of permissions!");
                            continue;
                        }

                    }

                }

                quest.plugin = this;

                ConfigurationSection section2 = config.getConfigurationSection("quests." + s + ".stages.ordered");

                int index = 1;
                boolean stageFailed = false;
                for (String s2 : section2.getKeys(false)) {

                    Stage stage = new Stage();

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

                        if (ScriptRegistry.containsScript("quests." + s + ".stages.ordered." + s2 + ".script-to-run")) {
                            trigger = new QuestTaskTrigger();
                            stage.script = config.getString("quests." + s + ".stages.ordered." + s2 + ".script-to-run");
                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "script-to-run: " + ChatColor.GOLD + "in " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a Denizen script!");
                            stageFailed = true;
                            break;
                        }

                    }

                    //

                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".break-block-ids")) {

                        if (checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".break-block-ids"), Integer.class)) {
                            breakids = config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".break-block-ids");
                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "break-block-ids: " + ChatColor.GOLD + "in " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of numbers!");
                            stageFailed = true;
                            break;
                        }

                        if (config.contains("quests." + s + ".stages.ordered." + s2 + ".break-block-amounts")) {

                            if (checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".break-block-amounts"), Integer.class)) {
                                breakamounts = config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".break-block-amounts");
                            } else {
                                printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "break-block-amounts: " + ChatColor.GOLD + "in " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of numbers!");
                                stageFailed = true;
                                break;
                            }

                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is missing " + ChatColor.RED + "break-block-amounts:");
                            stageFailed = true;
                            break;
                        }

                    }

                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".damage-block-ids")) {

                        if (checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".damage-block-ids"), Integer.class)) {
                            damageids = config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".damage-block-ids");
                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "damage-block-ids: " + ChatColor.GOLD + "in " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of numbers!");
                            stageFailed = true;
                            break;
                        }

                        if (config.contains("quests." + s + ".stages.ordered." + s2 + ".damage-block-amounts")) {

                            if (checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".damage-block-amounts"), Integer.class)) {
                                damageamounts = config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".damage-block-amounts");
                            } else {
                                printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "damage-block-amounts: " + ChatColor.GOLD + "in " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of numbers!");
                                stageFailed = true;
                                break;
                            }

                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is missing " + ChatColor.RED + "damage-block-amounts:");
                            stageFailed = true;
                            break;
                        }

                    }

                    for (int i : damageids) {

                        if (Material.getMaterial(i) != null) {
                            stage.blocksToDamage.put(Material.getMaterial(i), damageamounts.get(damageids.indexOf(i)));
                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + i + ChatColor.GOLD + " inside " + ChatColor.GREEN + "damage-block-ids: " + ChatColor.GOLD + "inside " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a valid item ID!");
                            stageFailed = true;
                            break;
                        }

                    }

                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".place-block-ids")) {

                        if (checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".place-block-ids"), Integer.class)) {
                            placeids = config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".place-block-ids");
                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "place-block-ids: " + ChatColor.GOLD + "in " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of numbers!");
                            stageFailed = true;
                            break;
                        }

                        if (config.contains("quests." + s + ".stages.ordered." + s2 + ".place-block-amounts")) {

                            if (checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".place-block-amounts"), Integer.class)) {
                                placeamounts = config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".place-block-amounts");
                            } else {
                                printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "place-block-amounts: " + ChatColor.GOLD + "in " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of numbers!");
                                stageFailed = true;
                                break;
                            }

                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is missing " + ChatColor.RED + "place-block-amounts:");
                            stageFailed = true;
                            break;
                        }

                    }

                    for (int i : placeids) {

                        if (Material.getMaterial(i) != null) {
                            stage.blocksToPlace.put(Material.getMaterial(i), placeamounts.get(placeids.indexOf(i)));
                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + i + ChatColor.GOLD + " inside " + ChatColor.GREEN + "place-block-ids: " + ChatColor.GOLD + "inside " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a valid item ID!");
                            stageFailed = true;
                            break;
                        }

                    }

                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".use-block-ids")) {

                        if (checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".use-block-ids"), Integer.class)) {
                            useids = config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".use-block-ids");
                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "use-block-ids: " + ChatColor.GOLD + "in " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of numbers!");
                            stageFailed = true;
                            break;
                        }

                        if (config.contains("quests." + s + ".stages.ordered." + s2 + ".use-block-amounts")) {

                            if (checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".use-block-amounts"), Integer.class)) {
                                useamounts = config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".use-block-amounts");
                            } else {
                                printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "use-block-amounts: " + ChatColor.GOLD + "in " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of numbers!");
                                stageFailed = true;
                                break;
                            }

                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is missing " + ChatColor.RED + "use-block-amounts:");
                            stageFailed = true;
                            break;
                        }

                    }

                    for (int i : useids) {

                        if (Material.getMaterial(i) != null) {
                            stage.blocksToUse.put(Material.getMaterial(i), useamounts.get(useids.indexOf(i)));
                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + i + ChatColor.GOLD + " inside " + ChatColor.GREEN + "use-block-ids: " + ChatColor.GOLD + "inside " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a valid item ID!");
                            stageFailed = true;
                            break;
                        }

                    }

                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".cut-block-ids")) {

                        if (checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".cut-block-ids"), Integer.class)) {
                            cutids = config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".cut-block-ids");
                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "cut-block-ids: " + ChatColor.GOLD + "in " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of numbers!");
                            stageFailed = true;
                            break;
                        }

                        if (config.contains("quests." + s + ".stages.ordered." + s2 + ".cut-block-amounts")) {

                            if (checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".cut-block-amounts"), Integer.class)) {
                                cutamounts = config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".cut-block-amounts");
                            } else {
                                printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "cut-block-amounts: " + ChatColor.GOLD + "in " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of numbers!");
                                stageFailed = true;
                                break;
                            }

                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is missing " + ChatColor.RED + "cut-block-amounts:");
                            stageFailed = true;
                            break;
                        }

                    }

                    for (int i : cutids) {

                        if (Material.getMaterial(i) != null) {
                            stage.blocksToCut.put(Material.getMaterial(i), cutamounts.get(cutids.indexOf(i)));
                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + i + ChatColor.GOLD + " inside " + ChatColor.GREEN + "cut-block-ids: " + ChatColor.GOLD + "inside " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a valid item ID!");
                            stageFailed = true;
                            break;
                        }

                    }

                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".fish-to-catch")) {

                        if (config.getInt("quests." + s + ".stages.ordered." + s2 + ".fish-to-catch", -999) != -999) {
                            stage.fishToCatch = config.getInt("quests." + s + ".stages.ordered." + s2 + ".fish-to-catch");
                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "fish-to-catch:" + ChatColor.GOLD + " inside " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a number!");
                            stageFailed = true;
                            break;
                        }

                    }

                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".players-to-kill")) {

                        if (config.getInt("quests." + s + ".stages.ordered." + s2 + ".players-to-kill", -999) != -999) {
                            stage.playersToKill = config.getInt("quests." + s + ".stages.ordered." + s2 + ".players-to-kill");
                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "players-to-kill:" + ChatColor.GOLD + " inside " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a number!");
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

                                    printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + enchant + ChatColor.GOLD + " inside " + ChatColor.GREEN + "enchantments: " + ChatColor.GOLD + "inside " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a valid enchantment!");
                                    stageFailed = true;
                                    break;

                                }

                            }

                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "enchantments: " + ChatColor.GOLD + "in " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of enchantment names!");
                            stageFailed = true;
                            break;
                        }

                        if (config.contains("quests." + s + ".stages.ordered." + s2 + ".enchantment-item-ids")) {

                            if (Quests.checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".enchantment-item-ids"), Integer.class)) {

                                for (int item : config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".enchantment-item-ids")) {

                                    if (Material.getMaterial(item) != null) {
                                        itemsToEnchant.add(Material.getMaterial(item));
                                    } else {
                                        printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + item + ChatColor.GOLD + " inside " + ChatColor.GREEN + "enchantment-item-ids: " + ChatColor.GOLD + "inside " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a valid item id!");
                                        stageFailed = true;
                                        break;
                                    }

                                }

                            } else {

                                printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "enchantment-item-ids: " + ChatColor.GOLD + "in " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of numbers!");
                                stageFailed = true;
                                break;

                            }

                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is missing " + ChatColor.RED + "enchantment-item-ids:");
                            stageFailed = true;
                            break;
                        }


                        if (config.contains("quests." + s + ".stages.ordered." + s2 + ".enchantment-amounts")) {

                            if (Quests.checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".enchantment-amounts"), Integer.class)) {

                                amountsToEnchant = config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".enchantment-amounts");

                            } else {

                                printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "enchantment-amounts: " + ChatColor.GOLD + "in " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of numbers!");
                                stageFailed = true;
                                break;

                            }

                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is missing " + ChatColor.RED + "enchantment-amounts:");
                            stageFailed = true;
                            break;
                        }


                    }

                    List<Integer> npcIdsToTalkTo;
                    LinkedList<NPC> npcsToTalkTo = new LinkedList<NPC>();

                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".npc-ids-to-talk-to")) {

                        if (checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".npc-ids-to-talk-to"), Integer.class)) {

                            npcIdsToTalkTo = config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".npc-ids-to-talk-to");
                            npcsToTalkTo = new LinkedList<NPC>();
                            for (int i : npcIdsToTalkTo) {

                                if (citizens.getNPCRegistry().getById(i) != null) {

                                    npcsToTalkTo.add(citizens.getNPCRegistry().getById(i));
                                    questNPCs.add(citizens.getNPCRegistry().getById(i));

                                } else {
                                    printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + i + ChatColor.GOLD + " inside " + ChatColor.GREEN + "npc-ids-to-talk-to: " + ChatColor.GOLD + "inside " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a valid NPC id!");
                                    stageFailed = true;
                                    break;
                                }

                            }

                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "npc-ids-to-talk-to: " + ChatColor.GOLD + "in " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of numbers!");
                            stageFailed = true;
                            break;
                        }

                    }

                    List<Integer> itemIdsToDeliver;
                    List<Integer> itemAmountsToDeliver;
                    List<Integer> itemDeliveryTargetIds;
                    ArrayList<String> deliveryMessages = new ArrayList();

                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".item-ids-to-deliver")) {

                        if (checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".item-ids-to-deliver"), Integer.class)) {

                            if (config.contains("quests." + s + ".stages.ordered." + s2 + ".item-amounts-to-deliver")) {

                                if (checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".item-amounts-to-deliver"), Integer.class)) {

                                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".npc-delivery-ids")) {

                                        if (checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".npc-delivery-ids"), Integer.class)) {

                                            if (config.contains("quests." + s + ".stages.ordered." + s2 + ".delivery-messages")) {

                                                itemIdsToDeliver = config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".item-ids-to-deliver");
                                                itemAmountsToDeliver = config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".item-amounts-to-deliver");
                                                itemDeliveryTargetIds = config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".npc-delivery-ids");
                                                deliveryMessages.addAll(config.getStringList("quests." + s + ".stages.ordered." + s2 + ".delivery-messages"));

                                                for (int i : itemIdsToDeliver) {

                                                    Material m = Material.getMaterial(i);
                                                    if (m != null) {

                                                        int amt = itemAmountsToDeliver.get(itemIdsToDeliver.indexOf(i));

                                                        if (amt > 0) {

                                                            int npcId = itemDeliveryTargetIds.get(itemIdsToDeliver.indexOf(i));
                                                            NPC npc = citizens.getNPCRegistry().getById(npcId);

                                                            if (npc != null) {

                                                                stage.itemsToDeliver.add(m);
                                                                stage.itemAmountsToDeliver.add(amt);
                                                                stage.itemDeliveryTargets.add(npc);
                                                                stage.deliverMessages = deliveryMessages;

                                                            } else {
                                                                printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + npcId + ChatColor.GOLD + " inside " + ChatColor.GREEN + "npc-delivery-ids: " + ChatColor.GOLD + "inside " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a valid NPC id!");
                                                                stageFailed = true;
                                                                break;
                                                            }

                                                        } else {
                                                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + amt + ChatColor.GOLD + " inside " + ChatColor.GREEN + "item-amounts-to-deliver: " + ChatColor.GOLD + "inside " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a positive amount!");
                                                            stageFailed = true;
                                                            break;
                                                        }

                                                    } else {
                                                        printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + i + ChatColor.GOLD + " inside " + ChatColor.GREEN + "item-ids-to-deliver: " + ChatColor.GOLD + "inside " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a valid item id!");
                                                        stageFailed = true;
                                                        break;
                                                    }

                                                }

                                            } else {
                                                printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is missing " + ChatColor.RED + "delivery-messages:");
                                                stageFailed = true;
                                                break;
                                            }

                                        } else {
                                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "npc-delivery-ids: " + ChatColor.GOLD + "in " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of ids!");
                                            stageFailed = true;
                                            break;
                                        }

                                    } else {
                                        printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is missing " + ChatColor.RED + "npc-delivery-ids:");
                                        stageFailed = true;
                                        break;
                                    }

                                } else {
                                    printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "item-amounts-to-deliver: " + ChatColor.GOLD + "in " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of numbers!");
                                    stageFailed = true;
                                    break;
                                }

                            } else {
                                printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is missing " + ChatColor.RED + "item-amounts-to-deliver:");
                                stageFailed = true;
                                break;
                            }

                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "item-ids-to-deliver: " + ChatColor.GOLD + "in " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of ids!");
                            stageFailed = true;
                            break;
                        }

                    }

                    List<Integer> npcIdsToKill;
                    List<Integer> npcKillAmounts;

                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".npc-ids-to-kill")) {

                        if (checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".npc-ids-to-kill"), Integer.class)) {

                            if (config.contains("quests." + s + ".stages.ordered." + s2 + ".npc-kill-amounts")) {

                                if (checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".npc-kill-amounts"), Integer.class)) {

                                    npcIdsToKill = config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".npc-ids-to-kill");
                                    npcKillAmounts = config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".npc-kill-amounts");
                                    for (int i : npcIdsToKill) {

                                        if (citizens.getNPCRegistry().getById(i) != null) {

                                            if (npcKillAmounts.get(npcIdsToKill.indexOf(i)) > 0) {
                                                stage.citizensToKill.add(citizens.getNPCRegistry().getById(i));
                                                stage.citizenNumToKill.add(npcKillAmounts.get(npcIdsToKill.indexOf(i)));
                                                questNPCs.add(citizens.getNPCRegistry().getById(i));
                                            } else {
                                                printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + npcKillAmounts.get(npcIdsToKill.indexOf(i)) + ChatColor.GOLD + " inside " + ChatColor.GREEN + "npc-kill-amounts: " + ChatColor.GOLD + "inside " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a positive number!");
                                                stageFailed = true;
                                                break;
                                            }

                                        } else {
                                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + i + ChatColor.GOLD + " inside " + ChatColor.GREEN + "npc-ids-to-kill: " + ChatColor.GOLD + "inside " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a valid NPC id!");
                                            stageFailed = true;
                                            break;
                                        }

                                    }

                                } else {
                                    printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "npc-kill-amounts: " + ChatColor.GOLD + "in " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of numbers!");
                                    stageFailed = true;
                                    break;
                                }

                            } else {
                                printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is missing " + ChatColor.RED + "npc-kill-amounts:");
                                stageFailed = true;
                                break;
                            }

                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "npc-ids-to-kill: " + ChatColor.GOLD + "in " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of numbers!");
                            stageFailed = true;
                            break;
                        }

                    }

                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".mobs-to-kill")) {

                        if (Quests.checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".mobs-to-kill"), String.class)) {

                            List<String> mobNames = config.getStringList("quests." + s + ".stages.ordered." + s2 + ".mobs-to-kill");
                            for (String mob : mobNames) {

                                if (mob.equalsIgnoreCase("Blaze")) {

                                    mobsToKill.add(EntityType.BLAZE);

                                } else if (mob.equalsIgnoreCase("CaveSpider")) {

                                    mobsToKill.add(EntityType.CAVE_SPIDER);

                                } else if (mob.equalsIgnoreCase("Chicken")) {

                                    mobsToKill.add(EntityType.CHICKEN);

                                } else if (mob.equalsIgnoreCase("Cow")) {

                                    mobsToKill.add(EntityType.COW);

                                } else if (mob.equalsIgnoreCase("Creeper")) {

                                    mobsToKill.add(EntityType.CREEPER);

                                } else if (mob.equalsIgnoreCase("Enderman")) {

                                    mobsToKill.add(EntityType.ENDERMAN);

                                } else if (mob.equalsIgnoreCase("EnderDragon")) {

                                    mobsToKill.add(EntityType.ENDER_DRAGON);

                                } else if (mob.equalsIgnoreCase("Ghast")) {

                                    mobsToKill.add(EntityType.GHAST);

                                } else if (mob.equalsIgnoreCase("Giant")) {

                                    mobsToKill.add(EntityType.GIANT);

                                } else if (mob.equalsIgnoreCase("IronGolem")) {

                                    mobsToKill.add(EntityType.IRON_GOLEM);

                                } else if (mob.equalsIgnoreCase("MagmaCube")) {

                                    mobsToKill.add(EntityType.MAGMA_CUBE);

                                } else if (mob.equalsIgnoreCase("MushroomCow")) {

                                    mobsToKill.add(EntityType.MUSHROOM_COW);

                                } else if (mob.equalsIgnoreCase("Ocelot")) {

                                    mobsToKill.add(EntityType.OCELOT);

                                } else if (mob.equalsIgnoreCase("Pig")) {

                                    mobsToKill.add(EntityType.PIG);

                                } else if (mob.equalsIgnoreCase("PigZombie")) {

                                    mobsToKill.add(EntityType.PIG_ZOMBIE);

                                } else if (mob.equalsIgnoreCase("Sheep")) {

                                    mobsToKill.add(EntityType.SHEEP);

                                } else if (mob.equalsIgnoreCase("Silverfish")) {

                                    mobsToKill.add(EntityType.SILVERFISH);

                                } else if (mob.equalsIgnoreCase("Skeleton")) {

                                    mobsToKill.add(EntityType.SKELETON);

                                } else if (mob.equalsIgnoreCase("Slime")) {

                                    mobsToKill.add(EntityType.SLIME);

                                } else if (mob.equalsIgnoreCase("Snowman")) {

                                    mobsToKill.add(EntityType.SNOWMAN);

                                } else if (mob.equalsIgnoreCase("Spider")) {

                                    mobsToKill.add(EntityType.SPIDER);

                                } else if (mob.equalsIgnoreCase("Squid")) {

                                    mobsToKill.add(EntityType.SQUID);

                                } else if (mob.equalsIgnoreCase("Villager")) {

                                    mobsToKill.add(EntityType.VILLAGER);

                                } else if (mob.equalsIgnoreCase("Wolf")) {

                                    mobsToKill.add(EntityType.WOLF);

                                } else if (mob.equalsIgnoreCase("Zombie")) {

                                    mobsToKill.add(EntityType.ZOMBIE);

                                } else {

                                    printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + mob + ChatColor.GOLD + " inside " + ChatColor.GREEN + "mobs-to-kill: " + ChatColor.GOLD + "inside " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a valid mob name!");
                                    stageFailed = true;
                                    break;

                                }

                            }

                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "mobs-to-kill: " + ChatColor.GOLD + "in " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of mob names!");
                            stageFailed = true;
                            break;
                        }

                        if (config.contains("quests." + s + ".stages.ordered." + s2 + ".mob-amounts")) {

                            if (Quests.checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".mob-amounts"), Integer.class)) {

                                for (int i : config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".mob-amounts")) {

                                    mobNumToKill.add(i);

                                }

                            } else {

                                printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "mob-amounts: " + ChatColor.GOLD + "in " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of numbers!");
                                stageFailed = true;
                                break;

                            }

                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is missing " + ChatColor.RED + "mob-amounts:");
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
                                    } catch (Exception e) {
                                        printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + loc + ChatColor.GOLD + " inside " + ChatColor.GREEN + "mobs-to-kill: " + ChatColor.GOLD + "inside " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not in proper location format!");
                                        printSevere(ChatColor.GOLD + "[Quests] Proper location format is: \"WorldName x y z\"");
                                        stageFailed = true;
                                        break;
                                    }

                                    if (getServer().getWorld(info[0]) != null) {
                                        Location finalLocation = new Location(getServer().getWorld(info[0]), x, y, z);
                                        locationsToKillWithin.add(finalLocation);
                                    } else {
                                        printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + info[0] + ChatColor.GOLD + " inside " + ChatColor.GREEN + "mobs-to-kill: " + ChatColor.GOLD + "inside " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a valid world name!");
                                        stageFailed = true;
                                        break;
                                    }

                                } else {
                                    printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + loc + ChatColor.GOLD + " inside " + ChatColor.GREEN + "mobs-to-kill: " + ChatColor.GOLD + "inside " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not in proper location format!");
                                    printSevere(ChatColor.GOLD + "[Quests] Proper location format is: \"WorldName x y z\"");
                                    stageFailed = true;
                                    break;
                                }

                            }

                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "locations-to-kill: " + ChatColor.GOLD + "in " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of locations!");
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
                                printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "kill-location-radii: " + ChatColor.GOLD + "in " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of numbers!");
                                stageFailed = true;
                                break;
                            }

                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is missing " + ChatColor.RED + "kill-location-radii:");
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
                                printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "kill-location-names: " + ChatColor.GOLD + "in " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of names!");
                                stageFailed = true;
                                break;
                            }

                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is missing " + ChatColor.RED + "kill-location-names:");
                            stageFailed = true;
                            break;
                        }


                    }

                    stage.mobsToKill = mobsToKill;
                    stage.mobNumToKill = mobNumToKill;
                    stage.locationsToKillWithin = locationsToKillWithin;
                    stage.radiiToKillWithin = radiiToKillWithin;
                    stage.areaNames = areaNames;

                    Map<Map<Enchantment, Material>, Integer> enchants = new HashMap<Map<Enchantment, Material>, Integer>();

                    for (Enchantment e : enchantments) {

                        Map<Enchantment, Material> map = new HashMap<Enchantment, Material>();
                        map.put(e, itemsToEnchant.get(enchantments.indexOf(e)));
                        enchants.put(map, amountsToEnchant.get(enchantments.indexOf(e)));

                    }

                    stage.itemsToEnchant = enchants;

                    Map<Material, Integer> breakMap = new EnumMap<Material, Integer>(Material.class);

                    for (int i : breakids) {

                        breakMap.put(Material.getMaterial(i), breakamounts.get(breakids.indexOf(i)));

                    }

                    stage.blocksToBreak = breakMap;

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
                                    } catch (Exception e) {
                                        printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + loc + ChatColor.GOLD + " inside " + ChatColor.GREEN + "locations-to-reach: " + ChatColor.GOLD + "inside " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not in proper location format!");
                                        printSevere(ChatColor.GOLD + "[Quests] Proper location format is: \"WorldName x y z\"");
                                        stageFailed = true;
                                        break;
                                    }

                                    if (getServer().getWorld(info[0]) != null) {
                                        Location finalLocation = new Location(getServer().getWorld(info[0]), x, y, z);
                                        stage.locationsToReach.add(finalLocation);
                                    } else {
                                        printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + info[0] + ChatColor.GOLD + " inside " + ChatColor.GREEN + "locations-to-reach: " + ChatColor.GOLD + "inside " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a valid world name!");
                                        stageFailed = true;
                                        break;
                                    }

                                } else {
                                    printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + loc + ChatColor.GOLD + " inside " + ChatColor.GREEN + "mobs-to-kill: " + ChatColor.GOLD + "inside " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not in proper location format!");
                                    printSevere(ChatColor.GOLD + "[Quests] Proper location format is: \"WorldName x y z\"");
                                    stageFailed = true;
                                    break;
                                }

                            }

                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "locations-to-reach: " + ChatColor.GOLD + "in " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of locations!");
                            stageFailed = true;
                            break;
                        }

                        if (config.contains("quests." + s + ".stages.ordered." + s2 + ".reach-location-radii")) {

                            if (Quests.checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".reach-location-radii"), Integer.class)) {

                                List<Integer> radii = config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".reach-location-radii");
                                for (int i : radii) {

                                    stage.radiiToReachWithin.add(i);

                                }

                            } else {
                                printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "reach-location-radii: " + ChatColor.GOLD + "in " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of numbers!");
                                stageFailed = true;
                                break;
                            }

                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is missing " + ChatColor.RED + "reach-location-radii:");
                            stageFailed = true;
                            break;
                        }

                        if (config.contains("quests." + s + ".stages.ordered." + s2 + ".reach-location-names")) {

                            if (Quests.checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".reach-location-names"), String.class)) {

                                List<String> locationNames = config.getStringList("quests." + s + ".stages.ordered." + s2 + ".reach-location-names");
                                for (String name : locationNames) {

                                    stage.locationNames.add(name);

                                }

                            } else {
                                printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "reach-location-names: " + ChatColor.GOLD + "in " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of names!");
                                stageFailed = true;
                                break;
                            }

                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is missing " + ChatColor.RED + "reach-location-names:");
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

                                            stage.mobsToTame.put(EntityType.WOLF, mobAmounts.get(mobs.indexOf(mob)));

                                        } else if (mob.equalsIgnoreCase("Ocelot")) {

                                            stage.mobsToTame.put(EntityType.OCELOT, mobAmounts.get(mobs.indexOf(mob)));

                                        } else {
                                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + mob + ChatColor.GOLD + " inside " + ChatColor.GREEN + "mobs-to-tame: " + ChatColor.GOLD + "inside " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a valid tameable mob!");
                                            stageFailed = true;
                                            break;
                                        }

                                    }

                                } else {
                                    printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "mob-tame-amounts: " + ChatColor.GOLD + "in " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of numbers!");
                                    stageFailed = true;
                                    break;
                                }

                            } else {
                                printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is missing " + ChatColor.RED + "mob-tame-amounts:");
                                stageFailed = true;
                                break;
                            }

                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "mobs-to-tame: " + ChatColor.GOLD + "in " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of mob names!");
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

                                            stage.sheepToShear.put(DyeColor.BLACK, shearAmounts.get(sheep.indexOf(color)));

                                        } else if (color.equalsIgnoreCase("Blue")) {

                                            stage.sheepToShear.put(DyeColor.BLUE, shearAmounts.get(sheep.indexOf(color)));

                                        } else if (color.equalsIgnoreCase("Brown")) {

                                            stage.sheepToShear.put(DyeColor.BROWN, shearAmounts.get(sheep.indexOf(color)));

                                        } else if (color.equalsIgnoreCase("Cyan")) {

                                            stage.sheepToShear.put(DyeColor.CYAN, shearAmounts.get(sheep.indexOf(color)));

                                        } else if (color.equalsIgnoreCase("Gray")) {

                                            stage.sheepToShear.put(DyeColor.GRAY, shearAmounts.get(sheep.indexOf(color)));

                                        } else if (color.equalsIgnoreCase("Green")) {

                                            stage.sheepToShear.put(DyeColor.GREEN, shearAmounts.get(sheep.indexOf(color)));

                                        } else if (color.equalsIgnoreCase("LightBlue")) {

                                            stage.sheepToShear.put(DyeColor.LIGHT_BLUE, shearAmounts.get(sheep.indexOf(color)));

                                        } else if (color.equalsIgnoreCase("Lime")) {

                                            stage.sheepToShear.put(DyeColor.LIME, shearAmounts.get(sheep.indexOf(color)));

                                        } else if (color.equalsIgnoreCase("Magenta")) {

                                            stage.sheepToShear.put(DyeColor.MAGENTA, shearAmounts.get(sheep.indexOf(color)));

                                        } else if (color.equalsIgnoreCase("Orange")) {

                                            stage.sheepToShear.put(DyeColor.ORANGE, shearAmounts.get(sheep.indexOf(color)));

                                        } else if (color.equalsIgnoreCase("Pink")) {

                                            stage.sheepToShear.put(DyeColor.PINK, shearAmounts.get(sheep.indexOf(color)));

                                        } else if (color.equalsIgnoreCase("Purple")) {

                                            stage.sheepToShear.put(DyeColor.PURPLE, shearAmounts.get(sheep.indexOf(color)));

                                        } else if (color.equalsIgnoreCase("Red")) {

                                            stage.sheepToShear.put(DyeColor.RED, shearAmounts.get(sheep.indexOf(color)));

                                        } else if (color.equalsIgnoreCase("Silver")) {

                                            stage.sheepToShear.put(DyeColor.SILVER, shearAmounts.get(sheep.indexOf(color)));

                                        } else if (color.equalsIgnoreCase("White")) {

                                            stage.sheepToShear.put(DyeColor.WHITE, shearAmounts.get(sheep.indexOf(color)));

                                        } else if (color.equalsIgnoreCase("Yellow")) {

                                            stage.sheepToShear.put(DyeColor.YELLOW, shearAmounts.get(sheep.indexOf(color)));

                                        } else {

                                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + color + ChatColor.GOLD + " inside " + ChatColor.GREEN + "sheep-to-shear: " + ChatColor.GOLD + "inside " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a valid color!");
                                            stageFailed = true;
                                            break;

                                        }

                                    }

                                } else {
                                    printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "sheep-amounts: " + ChatColor.GOLD + "in " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of numbers!");
                                    stageFailed = true;
                                    break;
                                }

                            } else {
                                printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is missing " + ChatColor.RED + "sheep-amounts:");
                                stageFailed = true;
                                break;
                            }

                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "sheep-to-shear: " + ChatColor.GOLD + "in " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of colors!");
                            stageFailed = true;
                            break;
                        }

                    }

                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".craft-item-ids")) {

                        if (Quests.checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".craft-item-ids"), Integer.class)) {

                            if (config.contains("quests." + s + ".stages.ordered." + s2 + ".craft-item-amounts")) {

                                if (Quests.checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".craft-item-amounts"), Integer.class)) {

                                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".craft-quest-items")) {

                                        if (Quests.checkList(config.getList("quests." + s + ".stages.ordered." + s2 + ".craft-quest-items"), Boolean.class)) {

                                            List<Integer> craftIds = config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".craft-item-ids");
                                            List<Integer> craftAmounts = config.getIntegerList("quests." + s + ".stages.ordered." + s2 + ".craft-item-amounts");
                                            List<Boolean> craftQuestItems = config.getBooleanList("quests." + s + ".stages.ordered." + s2 + ".craft-quest-items");

                                            for (int i : craftIds) {

                                                EnumMap<Material, Integer> map = new EnumMap<Material, Integer>(Material.class);
                                                if (firstStage) {
                                                    quest.questItems.put(Material.getMaterial(i), craftAmounts.get(craftIds.indexOf(i)));
                                                }
                                                map.put(Material.getMaterial(i), craftAmounts.get(craftIds.indexOf(i)));
                                                stage.itemsToCraft.put(map, craftQuestItems.get(craftIds.indexOf(i)));

                                            }

                                        }

                                    } else {
                                        printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is missing " + ChatColor.RED + "craft-quest-items:");
                                        stageFailed = true;
                                        break;
                                    }

                                } else {
                                    printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "craft-item-amounts: " + ChatColor.GOLD + "in " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of numbers!");
                                    stageFailed = true;
                                    break;
                                }

                            } else {
                                printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is missing " + ChatColor.RED + "craft-item-amounts:");
                                stageFailed = true;
                                break;
                            }

                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "craft-item-ids: " + ChatColor.GOLD + "in " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of item ids!");
                            stageFailed = true;
                            break;
                        }

                    }

                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".event")) {

                        Event evt = Event.getEvent(config.getString("quests." + s + ".stages.ordered." + s2 + ".event"), this);

                        if (evt != null) {
                            stage.event = evt;
                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "Event " + ChatColor.GOLD + "in " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " failed to load.");
                            stageFailed = true;
                            break;
                        }

                    }

                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".delay")) {

                        if (config.getLong("quests." + s + ".stages.ordered." + s2 + ".delay", -999) != -999) {
                            stage.delay = config.getLong("quests." + s + ".stages.ordered." + s2 + ".delay");
                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "delay: " + ChatColor.GOLD + "in " + ChatColor.LIGHT_PURPLE + "Stage " + s2 + ChatColor.GOLD + " of Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a number!");
                            stageFailed = true;
                            break;
                        }

                    }

                    if (config.contains("quests." + s + ".stages.ordered." + s2 + ".delay-message")) {

                        stage.delayMessage = config.getString("quests." + s + ".stages.ordered." + s2 + ".delay-message");

                    }

                    stage.citizensToInteract = npcsToTalkTo;

                    if (stageFailed) {
                        break;
                    }
                    quest.stages.add(stage);
                    firstStage = false;

                }

                if (stageFailed) {
                    continue;
                }

                //Load rewards
                if (config.contains("quests." + s + ".rewards.item-ids")) {

                    if (Quests.checkList(config.getList("quests." + s + ".rewards.item-ids"), Integer.class)) {

                        if (config.contains("quests." + s + ".rewards.item-amounts")) {

                            if (Quests.checkList(config.getList("quests." + s + ".rewards.item-amounts"), Integer.class)) {

                                boolean failed = false;
                                for (int i : config.getIntegerList("quests." + s + ".rewards.item-ids")) {

                                    Material m = Material.getMaterial(i);
                                    if (m == null) {
                                        printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + i + ChatColor.GOLD + " in " + ChatColor.GREEN + "item-amounts: " + ChatColor.AQUA + "Reward " + ChatColor.GOLD + "in Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of numbers!");
                                        failed = true;
                                        break;
                                    }
                                    int amnt = config.getIntegerList("quests." + s + ".rewards.item-amounts").get(config.getIntegerList("quests." + s + ".rewards.item-ids").indexOf(i));
                                    ItemStack stack = new ItemStack(m, amnt);
                                    quest.itemRewards.add(stack);
                                    quest.itemRewardAmounts.add(amnt);

                                }

                                if (failed) {
                                    continue;
                                }

                            } else {
                                printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "item-amounts: " + ChatColor.AQUA + "Reward " + ChatColor.GOLD + "in Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of numbers!");
                                continue;
                            }

                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.AQUA + "Rewards " + ChatColor.GOLD + "for Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is missing " + ChatColor.RED + "item-amounts:");
                            continue;
                        }

                    } else {
                        printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "item-ids: " + ChatColor.AQUA + "Reward " + ChatColor.GOLD + "in Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of item ids!");
                        continue;
                    }

                }

                if (config.contains("quests." + s + ".rewards.money")) {

                    if (config.getInt("quests." + s + ".rewards.money", -999) != -999) {
                        quest.moneyReward = config.getInt("quests." + s + ".rewards.money");
                    } else {
                        printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "money: " + ChatColor.AQUA + "Reward " + ChatColor.GOLD + "in Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a number!");
                        continue;
                    }

                }

                if (config.contains("quests." + s + ".rewards.exp")) {

                    if (config.getInt("quests." + s + ".rewards.exp", -999) != -999) {
                        quest.exp = config.getInt("quests." + s + ".rewards.exp");
                    } else {
                        printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "exp: " + ChatColor.AQUA + "Reward " + ChatColor.GOLD + "in Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a number!");
                        continue;
                    }

                }

                if (config.contains("quests." + s + ".rewards.commands")) {

                    if (Quests.checkList(config.getList("quests." + s + ".rewards.commands"), String.class)) {
                        quest.commands = config.getStringList("quests." + s + ".rewards.commands");
                    } else {
                        printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "commands: " + ChatColor.AQUA + "Reward " + ChatColor.GOLD + "in Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of commands!");
                        continue;
                    }

                }

                if (config.contains("quests." + s + ".rewards.permissions")) {

                    if (Quests.checkList(config.getList("quests." + s + ".rewards.permissions"), String.class)) {
                        quest.permissions = config.getStringList("quests." + s + ".rewards.permissions");
                    } else {
                        printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "permissions: " + ChatColor.AQUA + "Reward " + ChatColor.GOLD + "in Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of permissions!");
                        continue;
                    }

                }

                if (config.contains("quests." + s + ".rewards.quest-points")) {

                    if (config.getInt("quests." + s + ".rewards.quest-points", -999) != -999) {
                        quest.questPoints = config.getInt("quests." + s + ".rewards.quest-points");
                        totalQuestPoints += quest.questPoints;
                    } else {
                        printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "quest-points: " + ChatColor.AQUA + "Reward " + ChatColor.GOLD + "in Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a number!");
                        continue;
                    }

                }

                if (config.contains("quests." + s + ".rewards.mcmmo-skills")) {

                    if (Quests.checkList(config.getList("quests." + s + ".rewards.mcmmo-skills"), String.class)) {

                        if (config.contains("quests." + s + ".rewards.mcmmo-levels")) {

                            boolean failed = false;
                            for (String skill : config.getStringList("quests." + s + ".rewards.mcmmo-skills")) {

                                if (Quests.getMcMMOSkill(skill) == null) {
                                    printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + skill + ChatColor.GOLD + " in " + ChatColor.GREEN + "mcmmo-skills: " + ChatColor.AQUA + "Reward " + ChatColor.GOLD + "in Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a valid mcMMO skill name!");
                                    failed = true;
                                    break;
                                }

                            }
                            if (failed) {
                                continue;
                            }

                            quest.mcmmoSkills = config.getStringList("quests." + s + ".rewards.mcmmo-skills");
                            quest.mcmmoAmounts = config.getIntegerList("quests." + s + ".rewards.mcmmo-levels");

                        } else {
                            printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.AQUA + "Rewards " + ChatColor.GOLD + "for Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is missing " + ChatColor.RED + "mcmmo-levels:");
                            continue;
                        }

                    } else {
                        printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "mcmmo-skills: " + ChatColor.AQUA + "Reward " + ChatColor.GOLD + "in Quest " + ChatColor.DARK_PURPLE + quest.name + ChatColor.GOLD + " is not a list of mcMMO skill names!");
                        continue;
                    }
                }

                //
                quests.add(quest);

            } catch (Exception e) {

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
        } catch (Exception e) {
            e.printStackTrace();
        }

        ConfigurationSection sec = config.getConfigurationSection("events");
        for (String s : sec.getKeys(false)) {

            Event event = Event.getEvent(s, this);
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

        parsed = parsed.replaceAll("<black>", ChatColor.BLACK.toString());
        parsed = parsed.replaceAll("<darkblue>", ChatColor.DARK_BLUE.toString());
        parsed = parsed.replaceAll("<darkgreen>", ChatColor.DARK_GREEN.toString());
        parsed = parsed.replaceAll("<darkaqua>", ChatColor.DARK_AQUA.toString());
        parsed = parsed.replaceAll("<darkred>", ChatColor.DARK_RED.toString());
        parsed = parsed.replaceAll("<purple>", ChatColor.DARK_PURPLE.toString());
        parsed = parsed.replaceAll("<gold>", ChatColor.GOLD.toString());
        parsed = parsed.replaceAll("<grey>", ChatColor.GRAY.toString());
        parsed = parsed.replaceAll("<gray>", ChatColor.GRAY.toString());
        parsed = parsed.replaceAll("<darkgrey>", ChatColor.DARK_GRAY.toString());
        parsed = parsed.replaceAll("<darkgray>", ChatColor.DARK_GRAY.toString());
        parsed = parsed.replaceAll("<blue>", ChatColor.BLUE.toString());
        parsed = parsed.replaceAll("<green>", ChatColor.GREEN.toString());
        parsed = parsed.replaceAll("<aqua>", ChatColor.AQUA.toString());
        parsed = parsed.replaceAll("<red>", ChatColor.RED.toString());
        parsed = parsed.replaceAll("<pink>", ChatColor.LIGHT_PURPLE.toString());
        parsed = parsed.replaceAll("<yellow>", ChatColor.YELLOW.toString());
        parsed = parsed.replaceAll("<white>", ChatColor.WHITE.toString());

        parsed = parsed.replaceAll("<random>", ChatColor.MAGIC.toString());
        parsed = parsed.replaceAll("<italic>", ChatColor.ITALIC.toString());
        parsed = parsed.replaceAll("<bold>", ChatColor.BOLD.toString());
        parsed = parsed.replaceAll("<underline>", ChatColor.UNDERLINE.toString());
        parsed = parsed.replaceAll("<strike>", ChatColor.STRIKETHROUGH.toString());
        parsed = parsed.replaceAll("<reset>", ChatColor.RESET.toString());

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

    private static Map sort(Map unsortedMap) {

        List list = new LinkedList(unsortedMap.entrySet());

        Collections.sort(list, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                int i = (Integer) (((Map.Entry) o1).getValue());
                int i2 = (Integer) (((Map.Entry) o2).getValue());
                if (i < i2) {
                    return 1;
                } else if (i == i2) {
                    return 0;
                } else {
                    return -1;
                }


            }
        });

        Map sortedMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    public static void printSevere(String s) {

        s = ChatColor.stripColor(s);
        log.severe(s);

    }

    public static void printWarning(String s) {

        s = ChatColor.stripColor(s);
        log.warning(s);

    }

    public static void printInfo(String s) {

        s = ChatColor.stripColor(s);
        log.info(s);

    }

    public boolean hasItem(Player player, int i, int i2) {

        Inventory inv = player.getInventory();
        Material item = Material.getMaterial(i);
        int playerAmount = 0;

        for (ItemStack stack : inv.getContents()) {

            if (stack != null) {

                if (stack.getType().equals(item) && stack.getDurability() == stack.getDurability()) {
                    playerAmount += stack.getAmount();
                }

            }

        }

        if (playerAmount >= i2) {
            return true;
        } else {
            return false;
        }

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
        } catch (Exception e) {
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
        long days = 0;
        long hours = 0;
        long minutes = 0;
        long seconds = 0;
        if (((Long) milliseconds).compareTo(Long.parseLong("86400000")) > -1) {
            days = (Long) milliseconds / Long.parseLong("86400000");
            milliseconds -= ((Long) milliseconds / Long.parseLong("86400000")) * Long.parseLong("86400000");
        }

        if (((Long) milliseconds).compareTo(Long.parseLong("3600000")) > -1) {
            hours = (Long) milliseconds / Long.parseLong("3600000");
            milliseconds -= ((Long) milliseconds / Long.parseLong("3600000")) * Long.parseLong("3600000");
        }

        if (((Long) milliseconds).compareTo(Long.parseLong("60000")) > -1) {
            minutes = (Long) milliseconds / Long.parseLong("60000");
            milliseconds -= ((Long) milliseconds / Long.parseLong("60000")) * Long.parseLong("60000");
        }

        if (((Long) milliseconds).compareTo(Long.parseLong("1000")) > -1) {
            seconds = (Long) milliseconds / Long.parseLong("1000");
        }


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

    public static boolean removeItem(Inventory inventory, Material type, int amount) {

        HashMap<Integer, ? extends ItemStack> allItems = inventory.all(type);
        HashMap<Integer, Integer> removeFrom = new HashMap<Integer, Integer>();
        int foundAmount = 0;
        for (Map.Entry<Integer, ? extends ItemStack> item : allItems.entrySet()) {

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
            if (name.endsWith(check)) {
                return true;
            } else {
                return false;
            }

        } else if (check.endsWith("*") == false && check.startsWith("*")) {

            check = check.substring(1);
            if (name.startsWith(check)) {
                return true;
            } else {
                return false;
            }

        } else if (check.endsWith("*") && check.startsWith("*")) {

            check = check.substring(1, check.length());
            if (name.contains(check)) {
                return true;
            } else {
                return false;
            }

        } else {

            if (name.equalsIgnoreCase(check)) {
                return true;
            } else {
                return false;
            }

        }

    }

    public static boolean checkList(List<?> list, Class c) {

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

    public Event getEvent(String s) {

        for (Event e : events) {
            if (e.name.equalsIgnoreCase(s)) {
                return e;
            }
        }

        return null;

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
            Connection conn = DriverManager.getConnection(url, "bigal_snooper", "jpuradox");
            Statement statement = conn.createStatement();
            java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
            Timestamp stamp = new Timestamp(date.getTime());
            statement.executeUpdate("DELETE FROM entries " + "WHERE server='" + getServer().getIp() + ":" + ((Integer) getServer().getPort()).toString() + "'");
            String cit = citizens != null ? "true" : "false";
            statement.executeUpdate("INSERT INTO entries " + "VALUES ('" + getServer().getIp() + ":" + ((Integer) getServer().getPort()).toString() + "', " + quests.size() + ", '" + cit + "', '" + stamp.toString() + "')");
        } catch (Exception e) {

        }

    }
}
