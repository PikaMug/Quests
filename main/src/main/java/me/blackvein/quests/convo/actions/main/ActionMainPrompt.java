package me.blackvein.quests.convo.actions.main;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.blackvein.quests.Quest;
import me.blackvein.quests.QuestMob;
import me.blackvein.quests.Quests;
import me.blackvein.quests.Stage;
import me.blackvein.quests.actions.Action;
import me.blackvein.quests.convo.actions.ActionsEditorNumericPrompt;
import me.blackvein.quests.convo.actions.menu.ActionMenuPrompt;
import me.blackvein.quests.convo.actions.tasks.EffectPrompt;
import me.blackvein.quests.convo.actions.tasks.PlayerPrompt;
import me.blackvein.quests.convo.actions.tasks.TimerPrompt;
import me.blackvein.quests.convo.actions.tasks.WeatherPrompt;
import me.blackvein.quests.convo.generic.ItemStackPrompt;
import me.blackvein.quests.events.editor.actions.ActionsEditorPostOpenNumericPromptEvent;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.ConfigUtil;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;

public class ActionMainPrompt extends ActionsEditorNumericPrompt {
    
    private final Quests plugin;
    
    public ActionMainPrompt(ConversationContext context) {
        super(context);
        this.plugin = (Quests)context.getPlugin();
    }

    private final int size = 10;
    
    public int getSize() {
        return size;
    }
    
    public String getTitle(ConversationContext context) {
        return Lang.get("event") + ": " + context.getSessionData(CK.E_NAME);
    }
    
    public ChatColor getNumberColor(ConversationContext context, int number) {
        switch (number) {
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        case 6:
        case 7:
            if (plugin.getDependencies().getDenizenAPI() == null) {
                return ChatColor.GRAY;
            } else {
                return ChatColor.BLUE;
            }
        case 8:
            return ChatColor.BLUE;
        case 9:
            return ChatColor.GREEN;
        case 10:
            return ChatColor.RED;
        default:
            return null;
        }
    }
    
    public String getSelectionText(ConversationContext context, int number) {
        switch (number) {
        case 1:
            return ChatColor.YELLOW + Lang.get("eventEditorSetName");
        case 2:
            return ChatColor.GOLD + Lang.get("eventEditorPlayer");
        case 3:
            return ChatColor.GOLD + Lang.get("eventEditorTimer");
        case 4:
            return ChatColor.GOLD + Lang.get("eventEditorEffect");
        case 5:
            return ChatColor.GOLD + Lang.get("eventEditorWeather");
        case 6:
            return ChatColor.YELLOW + Lang.get("eventEditorSetMobSpawns");
        case 7:
            if (plugin.getDependencies().getDenizenAPI() == null) {
                return ChatColor.GRAY + Lang.get("stageEditorDenizenScript");
            } else {
                return ChatColor.YELLOW + Lang.get("stageEditorDenizenScript");
            }
        case 8:
            return ChatColor.YELLOW + Lang.get("eventEditorFailQuest") + ":";
        case 9:
            return ChatColor.GREEN + Lang.get("save");
        case 10:
            return ChatColor.RED + Lang.get("exit");
        default:
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    public String getAdditionalText(ConversationContext context, int number) {
        switch (number) {
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
            return "";
        case 6:
            if (context.getSessionData(CK.E_MOB_TYPES) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                LinkedList<String> types = (LinkedList<String>) context.getSessionData(CK.E_MOB_TYPES);
                String text = "";
                for (String s : types) {
                    QuestMob qm = QuestMob.fromString(s);
                    text += ChatColor.GRAY + "    - " + ChatColor.AQUA + qm.getType().name() 
                            + ((qm.getName() != null) ? ": " + qm.getName() : "") + ChatColor.GRAY + " x " 
                            + ChatColor.DARK_AQUA + qm.getSpawnAmounts() + ChatColor.GRAY + " -> " 
                            + ChatColor.GREEN + ConfigUtil.getLocationInfo(qm.getSpawnLocation()) + "\n";
                }
                return text;
            }
        case 7:
            if (plugin.getDependencies().getDenizenAPI() == null) {
                return ChatColor.GRAY + "(" + Lang.get("notInstalled") + ")";
            } else {
                if (context.getSessionData(CK.E_DENIZEN) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + context.getSessionData(CK.E_DENIZEN) 
                            + ChatColor.GRAY + ")";
                }
            }
        case 8:
            if (context.getSessionData(CK.E_FAIL_QUEST) == null) {
                context.setSessionData(CK.E_FAIL_QUEST, Lang.get("noWord"));
            }
            return "" + ChatColor.AQUA + context.getSessionData(CK.E_FAIL_QUEST);
        case 9:
        case 10:
            return "";
        default:
            return null;
        }
    }

    @Override
    public String getPromptText(ConversationContext context) {
        ActionsEditorPostOpenNumericPromptEvent event = new ActionsEditorPostOpenNumericPromptEvent(context, this);
        plugin.getServer().getPluginManager().callEvent(event);
        
        String text = ChatColor.GOLD + "- " + getTitle(context).replaceFirst(": ", ": " + ChatColor.AQUA) 
                + ChatColor.GOLD + " -\n";
        for (int i = 1; i <= size; i++) {
            text += getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                    + getSelectionText(context, i) + " " + getAdditionalText(context, i) + "\n";
        }
        return text;
    }

    @Override
    public Prompt acceptValidatedInput(ConversationContext context, Number input) {
        switch (input.intValue()) {
        case 1:
            return new ActionNamePrompt();
        case 2:
            return new PlayerPrompt(context);
        case 3:
            return new TimerPrompt();
        case 4:
            return new EffectPrompt(context);
        case 5:
            return new WeatherPrompt(context);
        case 6:
            return new ActionMobPrompt();
        case 7:
            return new ActionDenizenPrompt();
        case 8:
            String s = (String) context.getSessionData(CK.E_FAIL_QUEST);
            if (s.equalsIgnoreCase(Lang.get("yesWord"))) {
                context.setSessionData(CK.E_FAIL_QUEST, Lang.get("noWord"));
            } else {
                context.setSessionData(CK.E_FAIL_QUEST, Lang.get("yesWord"));
            }
            return new ActionMainPrompt(context);
        case 9:
            if (context.getSessionData(CK.E_OLD_EVENT) != null) {
                return new ActionSavePrompt((String) context.getSessionData(CK.E_OLD_EVENT));
            } else {
                return new ActionSavePrompt(null);
            }
        case 10:
            return new ActionExitPrompt();
        default:
            return new ActionMainPrompt(context);
        }
    }
    
    private class ActionNamePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("eventEditorEnterEventName");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                for (Action a : plugin.getActions()) {
                    if (a.getName().equalsIgnoreCase(input)) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorExists"));
                        return new ActionNamePrompt();
                    }
                }
                List<String> actionNames = plugin.getActionFactory().getNamesOfActionsBeingEdited();
                if (actionNames.contains(input)) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorSomeone"));
                    return new ActionNamePrompt();
                }
                if (input.contains(",")) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorInvalidQuestName"));
                    return new ActionNamePrompt();
                }
                actionNames.remove((String) context.getSessionData(CK.E_NAME));
                context.setSessionData(CK.E_NAME, input);
                actionNames.add(input);
                plugin.getActionFactory().setNamesOfActionsBeingEdited(actionNames);
            }
            return new ActionMainPrompt(context);
        }
    }

    private class ActionMobPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.GOLD + Lang.get("eventEditorMobSpawnsTitle") + "\n";
            if (context.getSessionData(CK.E_MOB_TYPES) == null) {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("eventEditorAddMobTypes") + " (" + Lang.get("noneSet") + ")\n";
                text += ChatColor.RED + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("clear") + "\n";
                text += ChatColor.GREEN + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("done");
            } else {
                @SuppressWarnings("unchecked")
                LinkedList<String> types = (LinkedList<String>) context.getSessionData(CK.E_MOB_TYPES);
                for (int i = 0; i < types.size(); i++) {
                    QuestMob qm = QuestMob.fromString(types.get(i));
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + (i + 1) + ChatColor.RESET + ChatColor.YELLOW + " - "
                            + Lang.get("edit") + ": " + ChatColor.AQUA + qm.getType().name() 
                            + ((qm.getName() != null) ? ": " + qm.getName() : "") + ChatColor.GRAY + " x " 
                            + ChatColor.DARK_AQUA + qm.getSpawnAmounts() + ChatColor.GRAY + " -> " + ChatColor.GREEN 
                            + ConfigUtil.getLocationInfo(qm.getSpawnLocation()) + "\n";
                }
                text += ChatColor.BLUE + "" + ChatColor.BOLD + (types.size() + 1) + ChatColor.RESET + ChatColor.YELLOW 
                        + " - " + Lang.get("eventEditorAddMobTypes") + "\n";
                text += ChatColor.RED + "" + ChatColor.BOLD + (types.size() + 2) + ChatColor.RESET + ChatColor.YELLOW 
                        + " - " + Lang.get("clear") + "\n";
                text += ChatColor.GREEN + "" + ChatColor.BOLD + (types.size() + 3) + ChatColor.RESET + ChatColor.YELLOW
                        + " - " + Lang.get("done");
            }
            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (context.getSessionData(CK.E_MOB_TYPES) == null) {
                if (input.equalsIgnoreCase("1")) {
                    return new QuestMobPrompt(0, null);
                } else if (input.equalsIgnoreCase("2")) {
                    context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("eventEditorMobSpawnsCleared"));
                    context.setSessionData(CK.E_MOB_TYPES, null);
                    return new ActionMobPrompt();
                } else if (input.equalsIgnoreCase("3")) {
                    return new ActionMainPrompt(context);
                }
            } else {
                @SuppressWarnings("unchecked")
                LinkedList<String> types = (LinkedList<String>) context.getSessionData(CK.E_MOB_TYPES);
                int inp;
                try {
                    inp = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED 
                            + Lang.get("reqNotANumber").replace("<input>", input));
                    return new ActionMobPrompt();
                }
                if (inp == types.size() + 1) {
                    return new QuestMobPrompt(inp - 1, null);
                } else if (inp == types.size() + 2) {
                    context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("eventEditorMobSpawnsCleared"));
                    context.setSessionData(CK.E_MOB_TYPES, null);
                    return new ActionMobPrompt();
                } else if (inp == types.size() + 3) {
                    return new ActionMainPrompt(context);
                } else if (inp > types.size()) {
                    return new ActionMobPrompt();
                } else {
                    return new QuestMobPrompt(inp - 1, QuestMob.fromString(types.get(inp - 1)));
                }
            }
            return new ActionMobPrompt();
        }
    }

    private class QuestMobPrompt extends StringPrompt {

        private QuestMob questMob;
        private Integer itemIndex = -1;
        private final Integer mobIndex;

        public QuestMobPrompt(int mobIndex, QuestMob questMob) {
            this.questMob = questMob;
            this.mobIndex = mobIndex;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.GOLD + Lang.get("eventEditorAddMobTypesTitle") + "\n";
            if (questMob == null) {
                questMob = new QuestMob();
            }
            // Check/add newly made item
            if (context.getSessionData("newItem") != null) {
                if (itemIndex >= 0) {
                    questMob.getInventory()[itemIndex] = ((ItemStack) context.getSessionData("tempStack"));
                    itemIndex = -1;
                }
                context.setSessionData("newItem", null);
                context.setSessionData("tempStack", null);
            }
            text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("eventEditorSetMobName") + ChatColor.GRAY + " (" 
                    + ((questMob.getName() == null) ? Lang.get("noneSet") : ChatColor.AQUA + questMob.getName()) 
                    + ChatColor.GRAY + ")\n";
            text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("eventEditorSetMobType") + ChatColor.GRAY + " (" 
                    + ((questMob.getType() == null) ? Lang.get("noneSet") : ChatColor.AQUA + questMob.getType().name())
                    + ChatColor.GRAY + ")\n";
            text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("eventEditorAddSpawnLocation") + ChatColor.GRAY + " (" 
                    + ((questMob.getSpawnLocation() == null) ? ChatColor.GRAY + Lang.get("noneSet") : ChatColor.AQUA 
                            + ConfigUtil.getLocationInfo(questMob.getSpawnLocation())) + ChatColor.GRAY + ")\n";
            text += ChatColor.BLUE + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("eventEditorSetMobSpawnAmount") + ChatColor.GRAY + " (" 
                    + ((questMob.getSpawnAmounts() == null) ? ChatColor.GRAY + Lang.get("noneSet") : ChatColor.AQUA 
                            + "" + questMob.getSpawnAmounts()) + ChatColor.GRAY + ")\n";
            text += ChatColor.BLUE + "" + ChatColor.BOLD + "5" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("eventEditorSetMobItemInHand") + ChatColor.GRAY + " (" 
                    + ((questMob.getInventory()[0] == null) ? ChatColor.GRAY + Lang.get("noneSet") : ChatColor.AQUA 
                            + ItemUtil.getDisplayString(questMob.getInventory()[0])) + ChatColor.GRAY + ")\n";
            text += ChatColor.BLUE + "" + ChatColor.BOLD + "6" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("eventEditorSetMobItemInHandDrop") + ChatColor.GRAY + " (" 
                    + ((questMob.getDropChances()[0] == null) ? ChatColor.GRAY + Lang.get("noneSet") : ChatColor.AQUA 
                    + "" + questMob.getDropChances()[0]) + ChatColor.GRAY + ")\n";
            text += ChatColor.BLUE + "" + ChatColor.BOLD + "7" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("eventEditorSetMobBoots") + ChatColor.GRAY + " (" 
                    + ((questMob.getInventory()[1] == null) ? ChatColor.GRAY + Lang.get("noneSet") : ChatColor.AQUA 
                    + ItemUtil.getDisplayString(questMob.getInventory()[1])) + ChatColor.GRAY + ")\n";
            text += ChatColor.BLUE + "" + ChatColor.BOLD + "8" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("eventEditorSetMobBootsDrop") + ChatColor.GRAY + " (" 
                    + ((questMob.getDropChances()[1] == null) ? ChatColor.GRAY + Lang.get("noneSet") : ChatColor.AQUA 
                    + "" + questMob.getDropChances()[1]) + ChatColor.GRAY + ")\n";
            text += ChatColor.BLUE + "" + ChatColor.BOLD + "9" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("eventEditorSetMobLeggings") + ChatColor.GRAY + " (" 
                    + ((questMob.getInventory()[2] == null) ? ChatColor.GRAY + Lang.get("noneSet") : ChatColor.AQUA 
                    + ItemUtil.getDisplayString(questMob.getInventory()[2])) + ChatColor.GRAY + ")\n";
            text += ChatColor.BLUE + "" + ChatColor.BOLD + "10" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("eventEditorSetMobLeggingsDrop") + ChatColor.GRAY + " (" 
                    + ((questMob.getDropChances()[2] == null) ? ChatColor.GRAY + Lang.get("noneSet") : ChatColor.AQUA
                    + "" + questMob.getDropChances()[2]) + ChatColor.GRAY + ")\n";
            text += ChatColor.BLUE + "" + ChatColor.BOLD + "11" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("eventEditorSetMobChestPlate") + ChatColor.GRAY + " (" 
                    + ((questMob.getInventory()[3] == null) ? ChatColor.GRAY + Lang.get("noneSet") : ChatColor.AQUA
                    + ItemUtil.getDisplayString(questMob.getInventory()[3])) + ChatColor.GRAY + ")\n";
            text += ChatColor.BLUE + "" + ChatColor.BOLD + "12" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("eventEditorSetMobChestPlateDrop") + ChatColor.GRAY + " (" 
                    + ((questMob.getDropChances()[3] == null) ? ChatColor.GRAY + Lang.get("noneSet") : ChatColor.AQUA
                    + "" + questMob.getDropChances()[3]) + ChatColor.GRAY + ")\n";
            text += ChatColor.BLUE + "" + ChatColor.BOLD + "13" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("eventEditorSetMobHelmet") + ChatColor.GRAY + " (" 
                    + ((questMob.getInventory()[4] == null) ? ChatColor.GRAY + Lang.get("noneSet") : ChatColor.AQUA
                    + ItemUtil.getDisplayString(questMob.getInventory()[4])) + ChatColor.GRAY + ")\n";
            text += ChatColor.BLUE + "" + ChatColor.BOLD + "14" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("eventEditorSetMobHelmetDrop") + ChatColor.GRAY + " (" 
                    + ((questMob.getDropChances()[4] == null) ? ChatColor.GRAY + Lang.get("noneSet") : ChatColor.AQUA
                    + "" + questMob.getDropChances()[4]) + ChatColor.GRAY + ")\n";
            text += ChatColor.GREEN + "" + ChatColor.BOLD + "15" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("done") + "\n";
            text += ChatColor.RED + "" + ChatColor.BOLD + "16" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("cancel");
            return text;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase("1")) {
                return new MobNamePrompt(mobIndex, questMob);
            } else if (input.equalsIgnoreCase("2")) {
                return new MobTypePrompt(mobIndex, questMob);
            } else if (input.equalsIgnoreCase("3")) {
                Map<UUID, Block> selectedMobLocations = plugin.getActionFactory().getSelectedMobLocations();
                selectedMobLocations.put(((Player) context.getForWhom()).getUniqueId(), null);
                plugin.getActionFactory().setSelectedMobLocations(selectedMobLocations);
                return new MobLocationPrompt(mobIndex, questMob);
            } else if (input.equalsIgnoreCase("4")) {
                return new MobAmountPrompt(mobIndex, questMob);
            } else if (input.equalsIgnoreCase("5")) {
                itemIndex = 0;
                return new ItemStackPrompt(QuestMobPrompt.this);
            } else if (input.equalsIgnoreCase("6")) {
                return new MobDropPrompt(0, mobIndex, questMob);
            } else if (input.equalsIgnoreCase("7")) {
                itemIndex = 1;
                return new ItemStackPrompt(QuestMobPrompt.this);
            } else if (input.equalsIgnoreCase("8")) {
                return new MobDropPrompt(1, mobIndex, questMob);
            } else if (input.equalsIgnoreCase("9")) {
                itemIndex = 2;
                return new ItemStackPrompt(QuestMobPrompt.this);
            } else if (input.equalsIgnoreCase("10")) {
                return new MobDropPrompt(2, mobIndex, questMob);
            } else if (input.equalsIgnoreCase("11")) {
                itemIndex = 3;
                return new ItemStackPrompt(QuestMobPrompt.this);
            } else if (input.equalsIgnoreCase("12")) {
                return new MobDropPrompt(3, mobIndex, questMob);
            } else if (input.equalsIgnoreCase("13")) {
                itemIndex = 4;
                return new ItemStackPrompt(QuestMobPrompt.this);
            } else if (input.equalsIgnoreCase("14")) {
                return new MobDropPrompt(4, mobIndex, questMob);
            } else if (input.equalsIgnoreCase("15")) {
                if (questMob.getType() == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorMustSetMobTypesFirst"));
                    return new QuestMobPrompt(mobIndex, questMob);
                } else if (questMob.getSpawnLocation() == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorMustSetMobLocationFirst"));
                    return new QuestMobPrompt(mobIndex, questMob);
                } else if (questMob.getSpawnAmounts() == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorMustSetMobAmountsFirst"));
                    return new QuestMobPrompt(mobIndex, questMob);
                }
                if (context.getSessionData(CK.E_MOB_TYPES) == null 
                        || ((LinkedList<String>) context.getSessionData(CK.E_MOB_TYPES)).isEmpty()) {
                    LinkedList<String> list = new LinkedList<String>();
                    list.add(questMob.serialize());
                    context.setSessionData(CK.E_MOB_TYPES, list);
                } else {
                    LinkedList<String> list = (LinkedList<String>) context.getSessionData(CK.E_MOB_TYPES);
                    if (mobIndex < list.size()) {
                        list.set(mobIndex, questMob.serialize());
                    } else {
                        list.add(questMob.serialize());
                    }
                    context.setSessionData(CK.E_MOB_TYPES, list);
                }
                return new ActionMobPrompt();
            } else if (input.equalsIgnoreCase("16")) {
                return new ActionMobPrompt();
            } else {
                return new QuestMobPrompt(mobIndex, questMob);
            }
        }
    }

    private class MobNamePrompt extends StringPrompt {

        private final QuestMob questMob;
        private final Integer mobIndex;

        public MobNamePrompt(int mobIndex, QuestMob questMob) {
            this.questMob = questMob;
            this.mobIndex = mobIndex;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.YELLOW + Lang.get("eventEditorSetMobNamePrompt");
            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                return new QuestMobPrompt(mobIndex, questMob);
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                questMob.setName(null);
                return new QuestMobPrompt(mobIndex, questMob);
            } else {
                input = ChatColor.translateAlternateColorCodes('&', input);
                questMob.setName(input);
                return new QuestMobPrompt(mobIndex, questMob);
            }
        }
    }

    private class MobTypePrompt extends StringPrompt {

        private final QuestMob questMob;
        private final Integer mobIndex;

        public MobTypePrompt(int mobIndex, QuestMob questMob) {
            this.questMob = questMob;
            this.mobIndex = mobIndex;
        }

        @Override
        public String getPromptText(ConversationContext arg0) {
            String mobs = ChatColor.LIGHT_PURPLE + Lang.get("eventEditorMobsTitle") + "\n";
            final EntityType[] mobArr = EntityType.values();
            for (int i = 0; i < mobArr.length; i++) {
                final EntityType type = mobArr[i];
                if (type.isAlive() == false) {
                    continue;
                }
                if (i < (mobArr.length - 1)) {
                    mobs += MiscUtil.snakeCaseToUpperCamelCase(mobArr[i].name()) + ", ";
                } else {
                    mobs += MiscUtil.snakeCaseToUpperCamelCase(mobArr[i].name()) + "\n";
                }
            }
            return mobs + ChatColor.YELLOW + Lang.get("eventEditorSetMobTypesPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                if (MiscUtil.getProperMobType(input) != null) {
                    questMob.setType(MiscUtil.getProperMobType(input));
                } else {
                    player.sendMessage(ChatColor.LIGHT_PURPLE + input + " " + ChatColor.RED 
                            + Lang.get("eventEditorInvalidMob"));
                    return new MobTypePrompt(mobIndex, questMob);
                }
            }
            return new QuestMobPrompt(mobIndex, questMob);
        }
    }

    private class MobAmountPrompt extends StringPrompt {

        private final QuestMob questMob;
        private final Integer mobIndex;

        public MobAmountPrompt(int mobIndex, QuestMob questMob) {
            this.questMob = questMob;
            this.mobIndex = mobIndex;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("eventEditorSetMobAmountsPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                try {
                    int i = Integer.parseInt(input);
                    if (i < 1) {
                        player.sendMessage(ChatColor.RED + Lang.get("invalidMinimum").replace("<number>", "1"));
                        return new MobAmountPrompt(mobIndex, questMob);
                    }
                    questMob.setSpawnAmounts(i);
                    return new QuestMobPrompt(mobIndex, questMob);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + Lang.get("reqNotANumber").replace("<input>", input));
                    return new MobAmountPrompt(mobIndex, questMob);
                }
            }
            return new QuestMobPrompt(mobIndex, questMob);
        }
    }

    private class MobLocationPrompt extends StringPrompt {

        private final QuestMob questMob;
        private final Integer mobIndex;

        public MobLocationPrompt(int mobIndex, QuestMob questMob) {
            this.questMob = questMob;
            this.mobIndex = mobIndex;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("eventEditorSetMobLocationPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(Lang.get("cmdAdd"))) {
                Map<UUID, Block> selectedMobLocations = plugin.getActionFactory().getSelectedMobLocations();
                Block block = selectedMobLocations.get(player.getUniqueId());
                if (block != null) {
                    Location loc = block.getLocation();
                    questMob.setSpawnLocation(loc);
                    selectedMobLocations.remove(player.getUniqueId());
                    plugin.getActionFactory().setSelectedMobLocations(selectedMobLocations);
                } else {
                    player.sendMessage(ChatColor.RED + Lang.get("eventEditorSelectBlockFirst"));
                    return new MobLocationPrompt(mobIndex, questMob);
                }
                return new QuestMobPrompt(mobIndex, questMob);
            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                Map<UUID, Block> selectedMobLocations = plugin.getActionFactory().getSelectedMobLocations();
                selectedMobLocations.remove(player.getUniqueId());
                plugin.getActionFactory().setSelectedMobLocations(selectedMobLocations);
                return new QuestMobPrompt(mobIndex, questMob);
            } else {
                return new MobLocationPrompt(mobIndex, questMob);
            }
        }
    }

    private class MobDropPrompt extends StringPrompt {

        private final QuestMob questMob;
        private final Integer mobIndex;
        private final Integer invIndex;

        public MobDropPrompt(int invIndex, int mobIndex, QuestMob questMob) {
            this.questMob = questMob;
            this.mobIndex = mobIndex;
            this.invIndex = invIndex;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.YELLOW + Lang.get("eventEditorSetDropChance");
            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            float chance;
            if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                return new QuestMobPrompt(mobIndex, questMob);
            }
            try {
                chance = Float.parseFloat(input);
            } catch (NumberFormatException e) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidRange")
                        .replace("<least>", "0.0").replace("<greatest>", "1.0"));
                return new MobDropPrompt(invIndex, mobIndex, questMob);
            }
            if (chance > 1 || chance < 0) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidRange")
                        .replace("<least>", "0.0").replace("<greatest>", "1.0"));
                return new MobDropPrompt(invIndex, mobIndex, questMob);
            }
            Float[] temp = questMob.getDropChances();
            temp[invIndex] = chance;
            questMob.setDropChances(temp);
            return new QuestMobPrompt(mobIndex, questMob);
        }
    }
    
    private class ActionDenizenPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.DARK_AQUA + "- " + Lang.get("stageEditorDenizenScript") + " -\n";
            for (String s : plugin.getDependencies().getDenizenAPI().getScriptNames()) {
                text += ChatColor.AQUA + "- " + s + "\n";
            }
            return text + ChatColor.YELLOW + Lang.get("stageEditorScriptPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                if (plugin.getDependencies().getDenizenAPI().containsScript(input)) {
                    context.setSessionData(CK.E_DENIZEN, input.toUpperCase());
                    return new ActionMainPrompt(context);
                } else {
                    player.sendMessage(ChatColor.RED + Lang.get("stageEditorInvalidScript"));
                    return new ActionDenizenPrompt();
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.E_DENIZEN, null);
                player.sendMessage(ChatColor.YELLOW + Lang.get("stageEditorDenizenCleared"));
                return new ActionMainPrompt(context);
            } else {
                return new ActionMainPrompt(context);
            }
        }
    }

    private class ActionSavePrompt extends StringPrompt {

        String modName = null;
        LinkedList<String> modified = new LinkedList<String>();

        public ActionSavePrompt(String modifiedName) {
            if (modifiedName != null) {
                modName = modifiedName;
                for (Quest q : plugin.getQuests()) {
                    for (Stage s : q.getStages()) {
                        if (s.getFinishAction() != null && s.getFinishAction().getName() != null) {
                            if (s.getFinishAction().getName().equalsIgnoreCase(modifiedName)) {
                                modified.add(q.getName());
                                break;
                            }
                        }
                    }
                }
            }
        }

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.YELLOW + Lang.get("questEditorSave") + " \"" + ChatColor.AQUA 
                    + context.getSessionData(CK.E_NAME) + ChatColor.YELLOW + "\"?\n";
            if (modified.isEmpty() == false) {
                text += ChatColor.RED + Lang.get("eventEditorModifiedNote") + "\n";
                for (String s : modified) {
                    text += ChatColor.GRAY + "    - " + ChatColor.DARK_RED + s + "\n";
                }
                text += ChatColor.RED + Lang.get("eventEditorForcedToQuit") + "\n";
            }
            return text + ChatColor.GREEN + "1 - " + Lang.get("yesWord") + "\n" + "2 - " + Lang.get("noWord");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(Lang.get("yesWord"))) {
                plugin.getActionFactory().saveAction(context);
                return new ActionMenuPrompt(context);
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(Lang.get("noWord"))) {
                return new ActionMainPrompt(context);
            } else {
                return new ActionSavePrompt(modName);
            }
        }
    }
    
    private class ActionExitPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.GREEN + "" +  ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.GREEN + " - " 
                    + Lang.get("yesWord") + "\n" + ChatColor.RED + "" +  ChatColor.BOLD + "2" + ChatColor.RESET 
                    + ChatColor.RED + " - " + Lang.get("noWord");
            return ChatColor.YELLOW + Lang.get("confirmDelete") + "\n" + text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(Lang.get("yesWord"))) {
                context.getForWhom().sendRawMessage(ChatColor.BOLD + "" + ChatColor.YELLOW + Lang.get("exited"));
                plugin.getActionFactory().clearData(context);
                return Prompt.END_OF_CONVERSATION;
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(Lang.get("noWord"))) {
                return new ActionMainPrompt(context);
            } else {
                return new ActionExitPrompt();
            }
        }
    }
}
