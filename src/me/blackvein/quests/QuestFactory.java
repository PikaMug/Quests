package me.blackvein.quests;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import me.blackvein.quests.prompts.RequirementsPrompt;
import me.blackvein.quests.prompts.RewardsPrompt;
import me.blackvein.quests.prompts.StagesPrompt;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;

public class QuestFactory implements ConversationAbandonedListener {

    public Quests quests;
    Map<Player, Quest> editSessions = new HashMap<Player, Quest>();
    Map<Player, Block> selectedBlockStarts = new HashMap<Player, Block>();
    public Map<Player, Block> selectedKillLocations = new HashMap<Player, Block>();
    public Map<Player, Block> selectedReachLocations = new HashMap<Player, Block>();
    public List<String> names = new LinkedList<String>();
    ConversationFactory convoCreator;
    static final ChatColor BOLD = ChatColor.BOLD;
    static final ChatColor AQUA = ChatColor.AQUA;
    static final ChatColor DARKAQUA = ChatColor.DARK_AQUA;
    static final ChatColor BLUE = ChatColor.BLUE;
    static final ChatColor GOLD = ChatColor.GOLD;
    static final ChatColor PINK = ChatColor.LIGHT_PURPLE;
    static final ChatColor GREEN = ChatColor.GREEN;
    static final ChatColor RED = ChatColor.RED;
    static final ChatColor DARKRED = ChatColor.DARK_RED;
    static final ChatColor YELLOW = ChatColor.YELLOW;
    static final ChatColor RESET = ChatColor.RESET;
    File questsFile;

    @SuppressWarnings("LeakingThisInConstructor")
    public QuestFactory(Quests plugin) {

        quests = plugin;
        questsFile = new File(plugin.getDataFolder(), "quests.yml");

        //Ensure to initialize convoCreator last, to ensure that 'this' is fully initialized before it is passed
        this.convoCreator = new ConversationFactory(plugin)
                .withModality(false)
                .withLocalEcho(false)
                .withPrefix(new QuestCreatorPrefix())
                .withFirstPrompt(new QuestNamePrompt())
                .withTimeout(3600)
                .thatExcludesNonPlayersWithMessage("Console may not perform this operation!")
                .addConversationAbandonedListener(this);

    }

    @Override
    public void conversationAbandoned(ConversationAbandonedEvent abandonedEvent) {

        if (abandonedEvent.getContext().getSessionData("questName") != null) {
            names.remove((String) abandonedEvent.getContext().getSessionData("questName"));
        }

        Player player = (Player) abandonedEvent.getContext().getForWhom();
        selectedBlockStarts.remove(player);
        selectedKillLocations.remove(player);
        selectedReachLocations.remove(player);

    }

    private class QuestCreatorPrefix implements ConversationPrefix {

        @Override
        public String getPrefix(ConversationContext context) {

            return "";

        }
    }

    private class MenuPrompt extends FixedSetPrompt {

        public MenuPrompt() {

            super("1", "2", "3");

        }

        @Override
        public String getPromptText(ConversationContext context) {

            String text =
                    GOLD + "- Quest Editor -\n"
                    + BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Create a Quest\n"
                    + BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Edit a Quest\n"
                    + BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Delete a Quest";

            return text;

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new QuestNamePrompt();
            }

            return null;

        }
    }

    public Prompt returnToMenu() {

        return new CreateMenuPrompt();

    }

    private class CreateMenuPrompt extends FixedSetPrompt {

        public CreateMenuPrompt() {

            super("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11");

        }

        @Override
        public String getPromptText(ConversationContext context) {

            String text =
                    GOLD + "- Quest: " + AQUA + context.getSessionData("questName") + GOLD + " -\n";

            text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set name\n";

            if (context.getSessionData("askMessage") == null) {
                text += BLUE + "" + BOLD + "2" + RESET + RED + " - Set ask message " + DARKRED + "(Required, none set)\n";
            } else {
                text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set ask message (\"" + context.getSessionData("askMessage") + "\")\n";
            }

            if (context.getSessionData("finishMessage") == null) {
                text += BLUE + "" + BOLD + "3" + RESET + RED + " - Set finish message " + DARKRED + "(Required, none set)\n";
            } else {
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Set finish message (\"" + context.getSessionData("finishMessage") + "\")\n";
            }

            if (context.getSessionData("redoDelay") == null) {
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Set redo delay (None set)\n";
            } else {
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Set redo delay (" + Quests.getTime((Long) context.getSessionData("redoDelay")) + ")\n";
            }

            if (context.getSessionData("npcStart") == null && quests.citizens != null) {
                text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - Set NPC start (None set)\n";
            } else if (quests.citizens != null) {
                text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - Set NPC start (" + quests.citizens.getNPCRegistry().getById((Integer) context.getSessionData("npcStart")).getName() + ")\n";
            }

            if (context.getSessionData("blockStart") == null) {

                if (quests.citizens != null) {
                    text += BLUE + "" + BOLD + "6" + RESET + YELLOW + " - Set Block start (None set)\n";
                } else {
                    text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - Set Block start (None set)\n";
                }

            } else {

                if (quests.citizens != null) {
                    Location l = (Location) context.getSessionData("blockStart");
                    text += BLUE + "" + BOLD + "6" + RESET + YELLOW + " - Set Block start (" + l.getWorld().getName() + ", " + l.getBlockX() + ", " + l.getBlockY() + ", " + l.getBlockZ() + ")\n";
                } else {
                    Location l = (Location) context.getSessionData("blockStart");
                    text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - Set Block start (" + l.getWorld().getName() + ", " + l.getBlockX() + ", " + l.getBlockY() + ", " + l.getBlockZ() + ")\n";
                }

            }

            if (quests.citizens != null) {
                text += BLUE + "" + BOLD + "7" + RESET + DARKAQUA + " - Edit Requirements\n";
            } else {
                text += BLUE + "" + BOLD + "6" + RESET + DARKAQUA + " - Edit Requirements\n";
            }

            if (quests.citizens != null) {
                text += BLUE + "" + BOLD + "8" + RESET + PINK + " - Edit Stages\n";
            } else {
                text += BLUE + "" + BOLD + "7" + RESET + PINK + " - Edit Stages\n";
            }

            if (quests.citizens != null) {
                text += BLUE + "" + BOLD + "9" + RESET + GREEN + " - Edit Rewards\n";
            } else {
                text += BLUE + "" + BOLD + "8" + RESET + GREEN + " - Edit Rewards\n";
            }

            if (quests.citizens != null) {
                text += BLUE + "" + BOLD + "10" + RESET + GOLD + " - Save\n";
            } else {
                text += BLUE + "" + BOLD + "9" + RESET + GOLD + " - Save\n";
            }

            if (quests.citizens != null) {
                text += BLUE + "" + BOLD + "11" + RESET + RED + " - Exit\n";
            } else {
                text += BLUE + "" + BOLD + "10" + RESET + RED + " - Exit\n";
            }

            return text;

        }

        @Override
        public Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {

                return new SetNamePrompt();

            } else if (input.equalsIgnoreCase("2")) {

                return new AskMessagePrompt();

            } else if (input.equalsIgnoreCase("3")) {

                return new FinishMessagePrompt();

            } else if (input.equalsIgnoreCase("4")) {

                return new RedoDelayPrompt();

            } else if (input.equalsIgnoreCase("5")) {

                if (quests.citizens != null) {
                    return new SetNpcStartPrompt();
                } else {
                    selectedBlockStarts.put((Player) context.getForWhom(), null);
                    return new BlockStartPrompt();
                }

            } else if (input.equalsIgnoreCase("6")) {

                if (quests.citizens != null) {
                    selectedBlockStarts.put((Player) context.getForWhom(), null);
                    return new BlockStartPrompt();
                } else {
                    return new RequirementsPrompt(quests, QuestFactory.this);
                }

            } else if (input.equalsIgnoreCase("7")) {

                if (quests.citizens != null) {
                    return new RequirementsPrompt(quests, QuestFactory.this);
                } else {
                    return new StagesPrompt(QuestFactory.this);
                }

            } else if (input.equalsIgnoreCase("8")) {

                if (quests.citizens != null) {
                    return new StagesPrompt(QuestFactory.this);
                } else {
                    return new RewardsPrompt(quests, QuestFactory.this);
                }

            } else if (input.equalsIgnoreCase("9")) {

                if (quests.citizens != null) {
                    return new RewardsPrompt(quests, QuestFactory.this);
                } else {
                    return new SavePrompt();
                }

            } else if (input.equalsIgnoreCase("10")) {

                if (quests.citizens != null) {
                    return new SavePrompt();
                } else {
                    return new ExitPrompt();
                }

            } else if (input.equalsIgnoreCase("11")) {

                if (quests.citizens != null) {
                    return new ExitPrompt();
                } else {
                    return new CreateMenuPrompt();
                }

            }

            return null;

        }
    }

    private class SelectEditPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String quests = GOLD + "- Quests -\n";
            for (Quest q : QuestFactory.this.quests.getQuests()) {
                quests += GOLD + "- " + YELLOW + q.getName() + "\n";
            }

            return quests + GOLD + "Enter a Quest to edit, or \"cancel\" to exit.";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("cancel") == false) {

                for (Quest q : QuestFactory.this.quests.getQuests()) {

                    if (q.getName().equalsIgnoreCase(input)) {
                    }

                }

                return new SelectEditPrompt();

            } else {
                return Prompt.END_OF_CONVERSATION;
            }

        }
    }

    private class QuestNamePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String text =
                    AQUA + "Create new Quest " + GOLD + "- Enter a name for the Quest (Or enter \'cancel\' to exit)";

            return text;

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("cancel") == false) {

                for (Quest q : quests.quests) {

                    if (q.name.equalsIgnoreCase(input)) {

                        context.getForWhom().sendRawMessage(ChatColor.RED + "Quest already exists!");
                        return new QuestNamePrompt();

                    }

                }

                if (names.contains(input)) {

                    context.getForWhom().sendRawMessage(ChatColor.RED + "Someone is creating a Quest with that name!");
                    return new QuestNamePrompt();

                }

                if (input.contains(",")) {

                    context.getForWhom().sendRawMessage(ChatColor.RED + "Name may not contain commas!");
                    return new QuestNamePrompt();

                }

                context.setSessionData("questName", input);
                names.add(input);
                return new CreateMenuPrompt();

            } else {

                return Prompt.END_OF_CONVERSATION;

            }

        }
    }

    private class SetNpcStartPrompt extends NumericPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return ChatColor.YELLOW + "Enter NPC ID, or 0 to clear the NPC start, or -1 to cancel";

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {

            if (input.intValue() > 0) {

                if (quests.citizens.getNPCRegistry().getById(input.intValue()) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + "No NPC exists with that id!");
                    return new SetNpcStartPrompt();
                }

                context.setSessionData("npcStart", input.intValue());
                return new CreateMenuPrompt();

            } else if (input.intValue() == 0) {
                context.setSessionData("npcStart", null);
                return new CreateMenuPrompt();
            } else if (input.intValue() == -1) {
                return new CreateMenuPrompt();
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + "No NPC exists with that id!");
                return new SetNpcStartPrompt();
            }

        }
    }

    private class BlockStartPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return ChatColor.YELLOW + "Right-click on a block to use as a start point, then enter \"done\" to save,\n"
                    + "or enter \"clear\" to clear the block start, or \"cancel\" to return";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase("done") || input.equalsIgnoreCase("cancel")) {

                if (input.equalsIgnoreCase("done")) {

                    Block block = selectedBlockStarts.get(player);
                    if (block != null) {
                        Location loc = block.getLocation();
                        context.setSessionData("blockStart", loc);
                        selectedBlockStarts.remove(player);
                    } else {
                        player.sendMessage(ChatColor.RED + "You must select a block first.");
                        return new BlockStartPrompt();
                    }

                } else {
                    selectedBlockStarts.remove(player);
                }


                return new CreateMenuPrompt();

            } else if (input.equalsIgnoreCase("clear")) {

                selectedBlockStarts.remove(player);
                context.setSessionData("blockStart", null);
                return new CreateMenuPrompt();

            }

            return new BlockStartPrompt();

        }
    }

    private class SetNamePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return ChatColor.YELLOW + "Enter Quest name (or \'cancel\' to return)";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("cancel") == false) {

                for (Quest q : quests.quests) {

                    if (q.name.equalsIgnoreCase(input)) {
                        context.getForWhom().sendRawMessage(RED + "A Quest with that name already exists!");
                        return new SetNamePrompt();
                    }

                }

                if (names.contains(input)) {
                    context.getForWhom().sendRawMessage(RED + "Someone is creating/editing a Quest with that name!");
                    return new SetNamePrompt();
                }

                if (input.contains(",")) {

                    context.getForWhom().sendRawMessage(ChatColor.RED + "Name may not contain commas!");
                    return new QuestNamePrompt();

                }

                names.remove((String) context.getSessionData("questName"));
                context.setSessionData("questName", input);
                names.add(input);

            }

            return new CreateMenuPrompt();

        }
    }

    private class AskMessagePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return ChatColor.YELLOW + "Enter ask message (or \'cancel\' to return)";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("cancel") == false) {
                context.setSessionData("askMessage", input);
            }

            return new CreateMenuPrompt();

        }
    }

    private class FinishMessagePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return ChatColor.YELLOW + "Enter finish message (or \'cancel\' to return)";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("cancel") == false) {
                context.setSessionData("finishMessage", input);
            }

            return new CreateMenuPrompt();

        }
    }

    private class RedoDelayPrompt extends NumericPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return ChatColor.YELLOW + "Enter amount of time (in milliseconds), or 0 to clear the redo delay, or -1 to cancel";

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {

            if (input.longValue() < -1) {
                context.getForWhom().sendRawMessage(ChatColor.RED + "Amount must be a positive number.");
            } else if (input.longValue() == 0) {
                context.setSessionData("redoDelay", null);
            } else if (input.longValue() != -1) {
                context.setSessionData("redoDelay", input.longValue());
            }

            return new CreateMenuPrompt();

        }
    }

    private class SavePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String text = GREEN
                    + "1 - Yes\n"
                    + "2 - No";
            return ChatColor.YELLOW + "Finish and save \"" + AQUA + context.getSessionData("questName") + YELLOW + "\"?\n" + text;

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase("Yes")) {

                if (context.getSessionData("askMessage") == null) {
                    context.getForWhom().sendRawMessage(RED + "You must set an ask message!");
                    return new CreateMenuPrompt();
                } else if (context.getSessionData("finishMessage") == null) {
                    context.getForWhom().sendRawMessage(RED + "You must set a finish message!");
                    return new CreateMenuPrompt();
                } else if (StagesPrompt.getStages(context) == 0) {
                    context.getForWhom().sendRawMessage(RED + "Your Quest has no Stages!");
                    return new CreateMenuPrompt();
                }

                FileConfiguration data = new YamlConfiguration();
                try {
                    data.load(new File(quests.getDataFolder(), "quests.yml"));
                    ConfigurationSection questSection = data.getConfigurationSection("quests");

                    int customNum = 1;
                    while (true) {

                        if (questSection.contains("custom" + customNum)) {
                            customNum++;
                        } else {
                            break;
                        }

                    }

                    ConfigurationSection newSection = questSection.createSection("custom" + customNum);
                    saveQuest(context, newSection);
                    data.save(new File(quests.getDataFolder(), "quests.yml"));
                    context.getForWhom().sendRawMessage(BOLD + "Quest saved! (You will need to perform a Quest reload for it to appear)");

                } catch (Exception e) {
                    e.printStackTrace();
                }

                return Prompt.END_OF_CONVERSATION;

            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase("No")) {
                return new CreateMenuPrompt();
            } else {
                return new SavePrompt();
            }

        }
    }

    private class ExitPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String text = GREEN
                    + "1 - Yes\n"
                    + "2 - No";
            return ChatColor.YELLOW + "Are you sure you want to exit without saving?\n" + text;

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase("Yes")) {

                context.getForWhom().sendRawMessage(BOLD + "" + YELLOW + "Exited.");
                return Prompt.END_OF_CONVERSATION;

            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase("No")) {
                return new CreateMenuPrompt();
            } else {
                return new ExitPrompt();
            }

        }
    }

    public static void saveQuest(ConversationContext cc, ConfigurationSection cs) {

        String name = (String) cc.getSessionData("questName");
        String desc = (String) cc.getSessionData("askMessage");
        String finish = (String) cc.getSessionData("finishMessage");
        Long redo = null;
        Integer npcStart = null;
        String blockStart = null;

        Integer moneyReq = null;
        Integer questPointsReq = null;
        LinkedList<Integer> itemIdReqs = null;
        LinkedList<Integer> itemAmountReqs = null;
        LinkedList<Boolean> removeItemReqs = null;
        LinkedList<String> permReqs = null;
        LinkedList<String> questReqs = null;
        String failMessage = null;

        Integer moneyRew = null;
        Integer questPointsRew = null;
        LinkedList<Integer> itemIdRews = null;
        LinkedList<Integer> itemAmountRews = null;
        Integer expRew = null;
        LinkedList<String> commandRews = null;
        LinkedList<String> permRews = null;
        LinkedList<String> mcMMOSkillRews = null;
        LinkedList<Integer> mcMMOSkillAmounts = null;



        if (cc.getSessionData("redoDelay") != null) {
            redo = (Long) cc.getSessionData("redoDelay");
        }

        if (cc.getSessionData("npcStart") != null) {
            npcStart = (Integer) cc.getSessionData("npcStart");
        }

        if (cc.getSessionData("blockStart") != null) {
            blockStart = (String) cc.getSessionData("blockStart");
        }



        if (cc.getSessionData("moneyReq") != null) {
            moneyReq = (Integer) cc.getSessionData("moneyReq");
        }

        if (cc.getSessionData("questPointsReq") != null) {
            questPointsReq = (Integer) cc.getSessionData("questPointsReq");
        }

        if (cc.getSessionData("itemIdReqs") != null) {
            itemIdReqs = (LinkedList<Integer>) cc.getSessionData("itemIdReqs");
            itemAmountReqs = (LinkedList<Integer>) cc.getSessionData("itemAmountReqs");
            removeItemReqs = (LinkedList<Boolean>) cc.getSessionData("removeItemReqs");
        }

        if (cc.getSessionData("permissionReqs") != null) {
            permReqs = (LinkedList<String>) cc.getSessionData("permissionReqs");
        }

        if (cc.getSessionData("questReqs") != null) {
            questReqs = (LinkedList<String>) cc.getSessionData("questReqs");
        }

        if (cc.getSessionData("failMessage") != null) {
            failMessage = (String) cc.getSessionData("failMessage");
        }



        if (cc.getSessionData("moneyRew") != null) {
            moneyRew = (Integer) cc.getSessionData("moneyRew");
        }

        if (cc.getSessionData("questPointsRew") != null) {
            questPointsRew = (Integer) cc.getSessionData("questPointsRew");
        }

        if (cc.getSessionData("itemIdRews") != null) {
            itemIdRews = (LinkedList<Integer>) cc.getSessionData("itemIdRews");
            itemAmountRews = (LinkedList<Integer>) cc.getSessionData("itemAmountRews");
        }

        if (cc.getSessionData("expRew") != null) {
            expRew = (Integer) cc.getSessionData("expRew");
        }

        if (cc.getSessionData("commandRews") != null) {
            commandRews = (LinkedList<String>) cc.getSessionData("commandRews");
        }

        if (cc.getSessionData("permissionRews") != null) {
            permRews = (LinkedList<String>) cc.getSessionData("permissionRews");
        }

        if (cc.getSessionData("mcMMOSkillRews") != null) {
            mcMMOSkillRews = (LinkedList<String>) cc.getSessionData("mcMMOSkillRews");
            mcMMOSkillAmounts = (LinkedList<Integer>) cc.getSessionData("mcMMOSkillAmounts");
        }



        cs.set("name", name);
        cs.set("npc-giver-id", npcStart);
        cs.set("block-start", blockStart);
        cs.set("redo-delay", redo);
        cs.set("ask-message", desc);
        cs.set("finish-message", finish);


        if (moneyReq != null || questPointsReq != null || itemIdReqs != null || permReqs != null || questReqs != null) {

            ConfigurationSection reqs = cs.createSection("requirements");
            reqs.set("item-ids", itemIdReqs);
            reqs.set("item-amounts", itemAmountReqs);
            reqs.set("money", moneyReq);
            reqs.set("quest-points", questPointsReq);
            reqs.set("remove-items", removeItemReqs);
            reqs.set("permissions", permReqs);
            reqs.set("quests", questReqs);
            reqs.set("fail-requirement-message", failMessage);

        }


        ConfigurationSection stages = cs.createSection("stages");
        ConfigurationSection ordered = stages.createSection("ordered");

        String pref;

        LinkedList<Integer> breakIds;
        LinkedList<Integer> breakAmounts;

        LinkedList<Integer> damageIds;
        LinkedList<Integer> damageAmounts;

        LinkedList<Integer> placeIds;
        LinkedList<Integer> placeAmounts;

        LinkedList<Integer> useIds;
        LinkedList<Integer> useAmounts;

        LinkedList<Integer> cutIds;
        LinkedList<Integer> cutAmounts;

        Integer fish;
        Integer players;

        LinkedList<String> enchantments;
        LinkedList<Integer> enchantmentIds;
        LinkedList<Integer> enchantmentAmounts;

        LinkedList<Integer> deliveryItemIds;
        LinkedList<Integer> deliveryItemAmounts;
        LinkedList<Integer> deliveryNPCIds;
        LinkedList<String> deliveryMessages;

        LinkedList<Integer> npcIds;

        LinkedList<String> mobs;
        LinkedList<Integer> mobAmounts;
        LinkedList<String> mobLocs;
        LinkedList<Integer> mobRadii;
        LinkedList<String> mobLocNames;

        LinkedList<String> reachLocs;
        LinkedList<Integer> reachRadii;
        LinkedList<String> reachNames;

        LinkedList<String> tames;
        LinkedList<Integer> tameAmounts;

        LinkedList<String> shearColors;
        LinkedList<Integer> shearAmounts;
        
        LinkedList<Integer> npcKillIds;
        LinkedList<Integer> npcKillAmounts;

        String script;
        String event;
        Long delay;
        String delayMessage;

        for (int i = 1; i <= StagesPrompt.getStages(cc); i++) {

            pref = "stage" + i;
            ConfigurationSection stage = ordered.createSection("\'" + i + "\'");

            breakIds = null;
            breakAmounts = null;

            damageIds = null;
            damageAmounts = null;

            placeIds = null;
            placeAmounts = null;

            useIds = null;
            useAmounts = null;

            cutIds = null;
            cutAmounts = null;

            fish = null;
            players = null;

            enchantments = null;
            enchantmentIds = null;
            enchantmentAmounts = null;

            deliveryItemIds = null;
            deliveryItemAmounts = null;
            deliveryNPCIds = null;
            deliveryMessages = null;

            npcIds = null;

            mobs = null;
            mobAmounts = null;
            mobLocs = null;
            mobRadii = null;
            mobLocNames = null;

            reachLocs = null;
            reachRadii = null;
            reachNames = null;

            tames = null;
            tameAmounts = null;

            shearColors = null;
            shearAmounts = null;
            
            npcKillIds = null;
            npcKillAmounts = null;

            script = null;
            event = null;
            delay = null;
            delayMessage = null;



            if (cc.getSessionData(pref + "breakIds") != null) {
                breakIds = (LinkedList<Integer>) cc.getSessionData(pref + "breakIds");
                breakAmounts = (LinkedList<Integer>) cc.getSessionData(pref + "breakAmounts");
            }

            if (cc.getSessionData(pref + "damageIds") != null) {
                damageIds = (LinkedList<Integer>) cc.getSessionData(pref + "damageIds");
                damageAmounts = (LinkedList<Integer>) cc.getSessionData(pref + "damageAmounts");
            }

            if (cc.getSessionData(pref + "placeIds") != null) {
                placeIds = (LinkedList<Integer>) cc.getSessionData(pref + "placeIds");
                placeAmounts = (LinkedList<Integer>) cc.getSessionData(pref + "placeAmounts");
            }

            if (cc.getSessionData(pref + "useIds") != null) {
                useIds = (LinkedList<Integer>) cc.getSessionData(pref + "useIds");
                useAmounts = (LinkedList<Integer>) cc.getSessionData(pref + "useAmounts");
            }

            if (cc.getSessionData(pref + "cutIds") != null) {
                cutIds = (LinkedList<Integer>) cc.getSessionData(pref + "cutIds");
                cutAmounts = (LinkedList<Integer>) cc.getSessionData(pref + "cutAmounts");
            }

            if (cc.getSessionData(pref + "fish") != null) {
                fish = (Integer) cc.getSessionData(pref + "fish");
            }

            if (cc.getSessionData(pref + "playerKill") != null) {
                players = (Integer) cc.getSessionData(pref + "playerKill");
            }

            if (cc.getSessionData(pref + "enchantTypes") != null) {
                enchantments = (LinkedList<String>) cc.getSessionData(pref + "enchantTypes");
                enchantmentIds = (LinkedList<Integer>) cc.getSessionData(pref + "enchantIds");
                enchantmentAmounts = (LinkedList<Integer>) cc.getSessionData(pref + "enchantAmounts");
            }

            if (cc.getSessionData(pref + "deliveryIds") != null) {
                deliveryItemIds = (LinkedList<Integer>) cc.getSessionData(pref + "deliveryIds");
                deliveryItemAmounts = (LinkedList<Integer>) cc.getSessionData(pref + "deliveryAmounts");
                deliveryNPCIds = (LinkedList<Integer>) cc.getSessionData(pref + "deliveryNPCs");
                deliveryMessages = (LinkedList<String>) cc.getSessionData(pref + "deliveryMessages");
            }

            if (cc.getSessionData(pref + "npcIdsToTalkTo") != null) {
                npcIds = (LinkedList<Integer>) cc.getSessionData(pref + "npcIdsToTalkTo");
            }

            if (cc.getSessionData(pref + "mobTypes") != null) {
                mobs = (LinkedList<String>) cc.getSessionData(pref + "mobTypes");
                mobAmounts = (LinkedList<Integer>) cc.getSessionData(pref + "mobAmounts");
                if (cc.getSessionData(pref + "killLocations") != null) {
                    mobLocs = (LinkedList<String>) cc.getSessionData(pref + "killLocations");
                    mobRadii = (LinkedList<Integer>) cc.getSessionData(pref + "killLocationRadii");
                    mobLocNames = (LinkedList<String>) cc.getSessionData(pref + "killLocationNames");
                }
            }

            if (cc.getSessionData(pref + "tameTypes") != null) {
                tames = (LinkedList<String>) cc.getSessionData(pref + "tameTypes");
                tameAmounts = (LinkedList<Integer>) cc.getSessionData(pref + "tameAmounts");
            }

            if (cc.getSessionData(pref + "shearColors") != null) {
                shearColors = (LinkedList<String>) cc.getSessionData(pref + "shearColors");
                shearAmounts = (LinkedList<Integer>) cc.getSessionData(pref + "shearAmounts");
            }
            
            if (cc.getSessionData(pref + "npcIdsToKill") != null) {
                npcKillIds = (LinkedList<Integer>) cc.getSessionData(pref + "npcIdsToKill");
                npcKillAmounts = (LinkedList<Integer>) cc.getSessionData(pref + "npcAmountsToKill");
            }

            if (cc.getSessionData(pref + "event") != null) {
                event = (String) cc.getSessionData(pref + "event");
            }

            if (cc.getSessionData(pref + "delay") != null) {
                delay = (Long) cc.getSessionData(pref + "delay");
                delayMessage = (String) cc.getSessionData(pref + "delayMessage");
            }

            if (cc.getSessionData(pref + "denizen") != null) {
                script = (String) cc.getSessionData(pref + "denizen");
            }

            stage.set("break-block-ids", breakIds);
            stage.set("break-block-amounts", breakAmounts);
            stage.set("damage-block-ids", damageIds);
            stage.set("damage-block-amounts", damageAmounts);
            stage.set("place-block-ids", placeIds);
            stage.set("place-block-amounts", placeAmounts);
            stage.set("use-block-ids", useIds);
            stage.set("use-block-amounts", useAmounts);
            stage.set("cut-block-ids", cutIds);
            stage.set("cut-block-amounts", cutAmounts);
            stage.set("fish-to-catch", fish);
            stage.set("players-to-kill", players);
            stage.set("enchantments", enchantments);
            stage.set("enchantment-item-ids", enchantmentIds);
            stage.set("enchantment-amounts", enchantmentAmounts);
            stage.set("item-ids-to-deliver", deliveryItemIds);
            stage.set("item-amounts-to-deliver", deliveryItemAmounts);
            stage.set("npc-delivery-ids", deliveryNPCIds);
            stage.set("delivery-messages", deliveryMessages);
            stage.set("npc-ids-to-talk-to", npcIds);
            stage.set("mobs-to-kill", mobs);
            stage.set("mob-amounts", mobAmounts);
            stage.set("locations-to-kill", mobLocs);
            stage.set("kill-location-radii", mobRadii);
            stage.set("kill-location-names", mobLocNames);
            stage.set("locations-to-reach", reachLocs);
            stage.set("reach-location-radii", reachRadii);
            stage.set("reach-location-names", reachNames);
            stage.set("mobs-to-tame", tames);
            stage.set("mob-tame-amounts", tameAmounts);
            stage.set("sheep-to-shear", shearColors);
            stage.set("sheep-amounts", shearAmounts);
            stage.set("npc-ids-to-kill", npcKillIds);
            stage.set("npc-kill-amounts", npcKillAmounts);
            stage.set("script-to-run", script);
            stage.set("event", event);
            stage.set("delay", delay);
            stage.set("delay-message", delayMessage);

        }


        if (moneyRew != null || questPointsRew != null || itemIdRews != null || permRews != null || expRew != null || commandRews != null || mcMMOSkillRews != null) {

            ConfigurationSection rews = cs.createSection("rewards");
            rews.set("item-ids", itemIdRews);
            rews.set("item-amounts", itemAmountRews);
            rews.set("money", moneyRew);
            rews.set("quest-points", questPointsRew);
            rews.set("exp", expRew);
            rews.set("permissions", permRews);
            rews.set("commands", commandRews);
            rews.set("mcmmo-skills", mcMMOSkillRews);
            rews.set("mcmmo-levels", mcMMOSkillAmounts);

        }

    }

    /*public static void loadQuest(ConversationContext cc, Quest q) {

        cc.setSessionData("name", q.name);
        cc.setSessionData("npc-giver-id", q.npcStart);
        cc.setSessionData("block-start", q.blockStart);
        cc.setSessionData("redo-delay", q.redoDelay);
        cc.setSessionData("ask-message", q.description);
        cc.setSessionData("finish-message", q.finished);


        cc.setSessionData(pref + "item-ids", itemIdReqs);
        cc.setSessionData(pref + "item-amounts", itemAmountReqs);
        cc.setSessionData(pref + "money", moneyReq);
        cc.setSessionData(pref + "quest-points", questPointsReq);
        cc.setSessionData(pref + "remove-items", removeItemReqs);
        cc.setSessionData(pref + "permissions", permReqs);
        cc.setSessionData(pref + "quests", questReqs);
        cc.setSessionData(pref + "fail-requirement-message", failMessage);
        cc.setSessionData(pref + "item-ids", itemIdRews);
        cc.setSessionData(pref + "item-amounts", itemAmountRews);
        cc.setSessionData(pref + "money", moneyRew);
        cc.setSessionData(pref + "quest-points", questPointsRew);
        cc.setSessionData(pref + "exp", expRew);
        cc.setSessionData(pref + "permissions", permRews);
        cc.setSessionData(pref + "commands", commandRews);
        cc.setSessionData(pref + "mcmmo-skills", mcMMOSkillRews);
        cc.setSessionData(pref + "mcmmo-levels", mcMMOSkillAmounts);

        String pref;

        for(Stage s : q.stages){

            pref = "stage" + q.stages.indexOf(s);


            cc.setSessionData("breakIds", breakIds);
            cc.setSessionData("break-block-amounts", breakAmounts);
            cc.setSessionData("damage-block-ids", damageIds);
            cc.setSessionData("damage-block-amounts", damageAmounts);
            cc.setSessionData("place-block-ids", placeIds);
            cc.setSessionData("place-block-amounts", placeAmounts);
            cc.setSessionData("use-block-ids", useIds);
            cc.setSessionData("use-block-amounts", useAmounts);
            cc.setSessionData("cut-block-ids", cutIds);
            cc.setSessionData("cut-block-amounts", cutAmounts);
            cc.setSessionData("fish-to-catch", fish);
            cc.setSessionData("players-to-kill", players);
            cc.setSessionData("enchantments", enchantments);
            cc.setSessionData("enchantment-item-ids", enchantmentIds);
            cc.setSessionData("enchantment-amounts", enchantmentAmounts);
            cc.setSessionData("item-ids-to-deliver", deliveryItemIds);
            cc.setSessionData("item-amounts-to-deliver", deliveryItemAmounts);
            cc.setSessionData("npc-delivery-ids", deliveryNPCIds);
            cc.setSessionData("delivery-messages", deliveryMessages);
            cc.setSessionData("npc-ids-to-talk-to", npcIds);
            cc.setSessionData("mobs-to-kill", mobs);
            cc.setSessionData("mob-amounts", mobAmounts);
            cc.setSessionData("locations-to-kill", mobLocs);
            cc.setSessionData("kill-location-radii", mobRadii);
            cc.setSessionData("kill-location-names", mobLocNames);
            cc.setSessionData("locations-to-reach", reachLocs);
            cc.setSessionData("reach-location-radii", reachRadii);
            cc.setSessionData("reach-location-names", reachNames);
            cc.setSessionData("mobs-to-tame", tames);
            cc.setSessionData("mob-tame-amounts", tameAmounts);
            cc.setSessionData("sheep-to-shear", shearColors);
            cc.setSessionData("sheep-amounts", shearAmounts);
            cc.setSessionData("script-to-run", script);
            cc.setSessionData("event", event);
            cc.setSessionData("delay", delay);
            cc.setSessionData("delay-message", delayMessage);

        }



    }*/

}
