package me.blackvein.quests.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import me.blackvein.quests.Quests;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Lang {

    public static String lang = "en";
    private static final LangToken tokens = new LangToken();

    private static final LinkedHashMap<String, String> langMap = new LinkedHashMap<String, String>();

    private final Quests plugin;

    public Lang(Quests plugin) {
        tokens.initTokens();
        this.plugin = plugin;
    }

    public static String get(String key) {
        return langMap.containsKey(key) ? tokens.convertString(langMap.get(key)) : "NULL";
    }

    public static String getKey(String val) {

        for (Entry<String, String> entry : langMap.entrySet()) {

            if (entry.getValue().equals(val)) {
                return entry.getKey();
            }

        }

        return "NULL";

    }

    public static String getCommandKey(String val) {

        for (Entry<String, String> entry : langMap.entrySet()) {

            if (entry.getValue().equalsIgnoreCase(val) && entry.getKey().toUpperCase().startsWith("COMMAND_")) {
                return entry.getKey();
            }

        }

        return "NULL";

    }

    public static void clearPhrases() {
        langMap.clear();
    }

    public static int getPhrases() {
        return langMap.size();
    }

    public static String getModified(String key, String[] tokens) {

        String orig = langMap.get(key);
        for (int i = 0; i < tokens.length; i++) {

            orig = orig.replaceAll("%" + (i + 1), tokens[i]);

        }

        return orig;

    }

    public void initPhrases() {

		//Quests
        langMap.put("questFailed", "*QUEST FAILED*");
        langMap.put("questMaxAllowed", "You may only have up to <number> Quests.");
        langMap.put("questAlreadyOn", "You are already on that Quest!");
        langMap.put("questTooEarly", "You may not take <quest> again for another <time>.");
        langMap.put("questAlreadyCompleted", "You have already completed <quest>.");
        langMap.put("questInvalidLocation", "You may not take <quest> at this location.");
        langMap.put("questInvalidDeliveryItem", "<item> is not a required item for this quest!");

        langMap.put("questSelectedLocation", "Selected location");

        langMap.put("questDisplayHelp", "- Display this help");
        //Commands
        langMap.put("COMMAND_LIST", "list");
        langMap.put("COMMAND_LIST_HELP", "list [page] - List available Quests");

        langMap.put("COMMAND_TAKE", "take");
        langMap.put("COMMAND_TAKE_HELP", "take [quest name] - Accept a Quest");
        langMap.put("COMMAND_TAKE_USAGE", "Usage: /quests take [quest]");

        langMap.put("COMMAND_QUIT", "quit");
        langMap.put("COMMAND_QUIT_HELP", "quit [quest] - Quit a current Quest");

        langMap.put("COMMAND_EDITOR", "editor");
        langMap.put("COMMAND_EDITOR_HELP", "editor - Create/Edit Quests");

        langMap.put("COMMAND_EVENTS_EDITOR", "events");
        langMap.put("COMMAND_EVENTS_EDITOR_HELP", "events - Create/Edit Events");

        langMap.put("COMMAND_STATS", "stats");
        langMap.put("COMMAND_STATS_HELP", "stats - View your Questing stats");

        langMap.put("COMMAND_TOP", "top");
        langMap.put("COMMAND_TOP_HELP", "top [number] - View top Questers");
        langMap.put("COMMAND_TOP_USAGE", "Usage: /quests top [number]");

        langMap.put("COMMAND_INFO", "info");
        langMap.put("COMMAND_INFO_HELP", "info - Display plugin information");

        langMap.put("COMMAND_JOURNAL", "journal");
        langMap.put("COMMAND_JOURNAL_HELP", "journal - View/Put away your Quest Journal");

        langMap.put("COMMAND_QUEST_HELP", "- Display current Quest objectives");
        langMap.put("COMMAND_QUESTINFO_HELP", "[quest name] - Display Quest information");

        langMap.put("COMMAND_QUESTADMIN_HELP", "- View Questadmin help");

        langMap.put("COMMAND_QUESTADMIN_STATS", "stats");
        langMap.put("COMMAND_QUESTADMIN_STATS_HELP", "stats [player] - View Questing statistics of a player");

        langMap.put("COMMAND_QUESTADMIN_GIVE", "give");
        langMap.put("COMMAND_QUESTADMIN_GIVE_HELP", "give [player] [quest] - Force a player to take a Quest");

        langMap.put("COMMAND_QUESTADMIN_QUIT", "quit");
        langMap.put("COMMAND_QUESTADMIN_QUIT_HELP", "quit [player] [quest] - Force a player to quit their Quest");

        langMap.put("COMMAND_QUESTADMIN_POINTS", "points");
        langMap.put("COMMAND_QUESTADMIN_POINTS_HELP", "points [player] [amount] - Set a players Quest Points");

        langMap.put("COMMAND_QUESTADMIN_TAKEPOINTS", "takepoints");
        langMap.put("COMMAND_QUESTADMIN_TAKEPOINTS_HELP", "takepoints [player] [amount] - Take a players Quest Points");

        langMap.put("COMMAND_QUESTADMIN_GIVEPOINTS", "givepoints");
        langMap.put("COMMAND_QUESTADMIN_GIVEPOINTS_HELP", "givepoints [player] [amount] - Give a player Quest Points");

        langMap.put("COMMAND_QUESTADMIN_POINTSALL", "pointsall");
        langMap.put("COMMAND_QUESTADMIN_POINTSALL_HELP", "pointsall [amount] - Set ALL players' Quest Points");

        langMap.put("COMMAND_QUESTADMIN_FINISH", "finish");
        langMap.put("COMMAND_QUESTADMIN_FINISH_HELP", "finish [player] [quest] - Immediately force Quest completion for a player");

        langMap.put("COMMAND_QUESTADMIN_NEXTSTAGE", "nextstage");
        langMap.put("COMMAND_QUESTADMIN_NEXTSTAGE_HELP", "nextstage [player] [quest] - Immediately force Stage completion for a player");

        langMap.put("COMMAND_QUESTADMIN_SETSTAGE", "setstage");
        langMap.put("COMMAND_QUESTADMIN_SETSTAGE_HELP", "setstage [player] [quest] [stage] - Set the current Stage for a player");
        langMap.put("COMMAND_QUESTADMIN_SETSTAGE_USAGE", "Usage: /questadmin setstage [player] [quest] [stage]");

        langMap.put("COMMAND_QUESTADMIN_PURGE", "purge");
        langMap.put("COMMAND_QUESTADMIN_PURGE_HELP", "purge [player] - Clear all Quests data of a player AND BLACKLISTS THEM");

        langMap.put("COMMAND_QUESTADMIN_RESET", "reset");
        langMap.put("COMMAND_QUESTADMIN_RESET_HELP", "reset [player] - Clear all Quests data of a player");

        langMap.put("COMMAND_QUESTADMIN_REMOVE", "remove");
        langMap.put("COMMAND_QUESTADMIN_REMOVE_HELP", "remove [player] [quest] - Remove a completed Quest from a player");

        langMap.put("COMMAND_QUESTADMIN_TOGGLEGUI", "togglegui");
        langMap.put("COMMAND_QUESTADMIN_TOGGLEGUI_HELP", "togglegui [npc id] - Toggle GUI Quest Display on an NPC");

        langMap.put("COMMAND_QUESTADMIN_RELOAD", "reload");
        langMap.put("COMMAND_QUESTADMIN_RELOAD_HELP", "reload - Reload all Quests");

        //Quest create menu
        langMap.put("questEditorHeader", "Create Quest");
        langMap.put("questEditorCreate", "Create new Quest");
        langMap.put("questEditorEdit", "Edit a Quest");
        langMap.put("questEditorDelete", "Delete Quest");
        langMap.put("questEditorName", "Set name");

        langMap.put("questEditorAskMessage", "Set ask message");
        langMap.put("questEditorFinishMessage", "Set finish message");
        langMap.put("questEditorRedoDelay", "Set redo delay");
        langMap.put("questEditorNPCStart", "Set NPC start");
        langMap.put("questEditorBlockStart", "Set Block start");
        langMap.put("questEditorInitialEvent", "Set initial Event");
        langMap.put("questEditorSetGUI", "Set GUI Item display");
        langMap.put("questEditorReqs", "Edit Requirements");
        langMap.put("questEditorStages", "Edit Stages");
        langMap.put("questEditorRews", "Edit Rewards");

        langMap.put("questEditorEnterQuestName", "Enter Quest name (or 'cancel' to return)");
        langMap.put("questEditorEditEnterQuestName", "Enter Quest name to edit, or 'cancel' to return");
        langMap.put("questEditorEnterAskMessage", "Enter ask message (or 'cancel' to return)");
        langMap.put("questEditorEnterFinishMessage", "Enter finish message (or 'cancel' to return)");
        langMap.put("questEditorEnterRedoDelay", "Enter amount of time (in seconds), 0 to clear the redo delay or -1 to cancel ");
        langMap.put("questEditorEnterNPCStart", "Enter NPC ID, -1 to clear the NPC start or -2 to cancel");
        langMap.put("questEditorEnterBlockStart", "Right-click on a block to use as a start point, then enter 'done' to save, or enter 'clear' to clear the block start, or 'cancel' to return");
        langMap.put("questEditorEnterInitialEvent", "Enter an Event name, or enter 'clear' to clear the initial Event, or 'cancel' to return");

        langMap.put("questRequiredNoneSet", "Required, none set");

        langMap.put("questWGSetRegion", "Set Region");
        langMap.put("questWGNotInstalled", "WorldGuard not installed");
        langMap.put("questWGPrompt", "Enter WorldGuard region, or enter 'clear' to clear the region, or 'cancel' to return.");
        langMap.put("questWGInvalidRegion", "<region> is not a valid WorldGuard region!");
        langMap.put("questWGRegionCleared", "Quest region cleared.");

        langMap.put("questCitNotInstalled", "Citizens not installed");
        langMap.put("questDenNotInstalled", "Denizen not installed");

        langMap.put("questGUIError", "Error: That item is already being used as the GUI Display for the Quest <quest>.");
        langMap.put("questCurrentItem", "Current item:");
        langMap.put("questSetItem", "Set Item");
        langMap.put("questClearItem", "Clear Item");
        langMap.put("questGUICleared", "Quest GUI Item Display cleared.");

        langMap.put("questDeleted", "Quest deleted! Quests and Events have been reloaded.");

        //Quest create menu errors
        langMap.put("questEditorNameExists", "A Quest with that name already exists!");
        langMap.put("questEditorBeingEdited", "Someone is creating/editing a Quest with that name!");
        langMap.put("questEditorInvalidQuestName", "Name may not contain periods or commas!");
        langMap.put("questEditorInvalidEventName", "is not a valid event name!");
        langMap.put("questEditorInvalidNPC", "No NPC exists with that id!");
        langMap.put("questEditorNoStartBlockSelected", "You must select a block first.");
        langMap.put("questEditorPositiveAmount", "Amount must be a positive number.");
        langMap.put("questEditorQuestAsRequirement1", "The following Quests have");
        langMap.put("questEditorQuestAsRequirement2", "as a requirement:");
        langMap.put("questEditorQuestAsRequirement3", "You must modify these Quests so that they do not use it before deleting it.");
        langMap.put("questEditorQuestNotFound", "Quest not found!");

        langMap.put("questEditorEventCleared", "Initial Event cleared.");
        langMap.put("questEditorSave", "Finish and save");

        langMap.put("questEditorNeedAskMessage", "You must set an ask message!");
        langMap.put("questEditorNeedFinishMessage", "You must set a finish message!");
        langMap.put("questEditorNeedStages", "Your Quest has no Stages!");
        langMap.put("questEditorSaved", "Quest saved! (You will need to perform a Quest reload for it to appear)");
        langMap.put("questEditorExited", "Are you sure you want to exit without saving?");
        langMap.put("questEditorDeleted", "Are you sure you want to delete the Quest");

        langMap.put("questEditorNoPermsCreate", "You do not have permission to create Quests.");
        langMap.put("questEditorNoPermsEdit", "You do not have permission to edit Quests.");
        langMap.put("questEditorNoPermsDelete", "You do not have permission to delete Quests.");
        //

        //Stages
        //Menu
        langMap.put("stageEditorEditStage", "Edit Stage");
        langMap.put("stageEditorNewStage", "Add new Stage");
        //create prompt
        langMap.put("stageEditorStages", "Stages");
        langMap.put("stageEditorStage", "Stage");
        langMap.put("stageEditorBreakBlocks", "Break Blocks");
        langMap.put("stageEditorDamageBlocks", "Damage Blocks");
        langMap.put("stageEditorPlaceBlocks", "Place Blocks");
        langMap.put("stageEditorUseBlocks", "Use Blocks");
        langMap.put("stageEditorCutBlocks", "Cut Blocks");
        langMap.put("stageEditorCatchFish", "Catch Fish");
        langMap.put("stageEditorFish", "fish");
        langMap.put("stageEditorKillPlayers", "Kill Players");
        langMap.put("stageEditorPlayers", "players");
        langMap.put("stageEditorEnchantItems", "Enchant Items");
        langMap.put("stageEditorDeliverItems", "Deliver Items");
        langMap.put("stageEditorTalkToNPCs", "Talk to NPCs");
        langMap.put("stageEditorKillNPCs", "Kill NPCs");
        langMap.put("stageEditorKillMobs", "Kill Mobs");
        langMap.put("stageEditorReachLocs", "Reach locations");
        langMap.put("stageEditorReachRadii1", "Reach within");
        langMap.put("stageEditorReachRadii2", "blocks of");
        langMap.put("stageEditorTameMobs", "Tame Mobs");
        langMap.put("stageEditorShearSheep", "Shear Sheep");
        langMap.put("stageEditorEvents", "Events");
        langMap.put("stageEditorStageEvents", "Stage Events");
        langMap.put("stageEditorStartEvent", "Start Event");
        langMap.put("stageEditorStartEventCleared", "Start Event cleared.");
        langMap.put("stageEditorFinishEvent", "Finish Event");
        langMap.put("stageEditorFinishEventCleared", "Finish Event cleared.");
        langMap.put("stageEditorChatEvents", "Chat Events");
        langMap.put("stageEditorChatTrigger", "Chat Trigger");
        langMap.put("stageEditorTriggeredBy", "Triggered by");
        langMap.put("stageEditorChatEventsCleared", "Chat Events cleared.");
        langMap.put("stageEditorDeathEvent", "Death Event");
        langMap.put("stageEditorDeathEventCleared", "Death Event cleared.");
        langMap.put("stageEditorDisconnectEvent", "Disconnect Event");
        langMap.put("stageEditorDisconnectEventCleared", "Disconnect Event cleared.");
        langMap.put("stageEditorDelayMessage", "Delay Message");
        langMap.put("stageEditorDenizenScript", "Denizen Script");
        langMap.put("stageEditorStartMessage", "Start Message");
        langMap.put("stageEditorCompleteMessage", "Complete Message");
        langMap.put("stageEditorDelete", "Delete Stage");

        langMap.put("stageEditorSetBlockNames", "Set block names");
        langMap.put("stageEditorSetBlockAmounts", "Set block amounts");
        langMap.put("stageEditorSetBlockDurability", "Set block durability");
        langMap.put("stageEditorSetDamageAmounts", "Set damage amounts");
        langMap.put("stageEditorSetPlaceAmounts", "Set place amounts");
        langMap.put("stageEditorSetUseAmounts", "Set use amounts");
        langMap.put("stageEditorSetCutAmounts", "Set cut amounts");
        langMap.put("stageEditorSetKillAmounts", "Set kill amounts");
        langMap.put("stageEditorSetEnchantAmounts", "Set enchant amounts");
        langMap.put("stageEditorSetMobAmounts", "Set mob amounts");
        langMap.put("stageEditorSetEnchantments", "Set enchantments");
        langMap.put("stageEditorSetItemNames", "Set item names");
        langMap.put("stageEditorSetKillIds", "Set NPC IDs");
        langMap.put("stageEditorSetMobTypes", "Set mob types");
        langMap.put("stageEditorSetKillLocations", "Set kill locations");
        langMap.put("stageEditorSetKillLocationRadii", "Set kill location radii");
        langMap.put("stageEditorSetKillLocationNames", "Set kill location names");
        langMap.put("stageEditorSetLocations", "Set locations");
        langMap.put("stageEditorSetLocationRadii", "Set location radii");
        langMap.put("stageEditorSetLocationNames", "Set location names");
        langMap.put("stageEditorSetTameAmounts", "Set tame amounts");
        langMap.put("stageEditorSetShearColors", "Set sheep colors");
        langMap.put("stageEditorSetShearAmounts", "Set shear amounts");
        langMap.put("stageEditorPassword", "Password Objectives");
        langMap.put("stageEditorAddPasswordDisplay", "Add password display");
        langMap.put("stageEditorAddPasswordPhrases", "Add password phrase(s)");
        langMap.put("stageEditorNoPasswordDisplays", "No password displays set");
        langMap.put("stageObjectiveOverride", "Objective Display Override");
        langMap.put("stageEditorCustom", "Custom Objectives");
        langMap.put("stageEditorNoModules", "No modules loaded");
        langMap.put("stageEditorModuleNotFound", "Custom objective module not found.");
        langMap.put("stageEditorCustomPrompt", "Enter the name of a custom objective to add, or enter \'clear\' to clear all custom objectives, or \'cancel\' to return.");
        langMap.put("stageEditorCustomAlreadyAdded", "That custom objective has already been added!");
        langMap.put("stageEditorCustomCleared", "Custom objectives cleared.");
        langMap.put("stageEditorCustomDataPrompt", "Enter value for <data>:");

        langMap.put("stageEditorEnterBlockNames", "Enter block names, separating each one by a space, or enter \'cancel\' to return.");
        langMap.put("stageEditorBreakBlocksPrompt", "Enter block amounts (numbers), separating each one by a space, or enter \'cancel\' to return.");
        langMap.put("stageEditorDamageBlocksPrompt", "Enter damage amounts (numbers), separating each one by a space, or enter \'cancel\' to return.");
        langMap.put("stageEditorPlaceBlocksPrompt", "Enter place amounts (numbers), separating each one by a space, or enter \'cancel\' to return.");
        langMap.put("stageEditorUseBlocksPrompt", "Enter use amounts (numbers), separating each one by a space, or enter \'cancel\' to return.");
        langMap.put("stageEditorCutBlocksPrompt", "Enter cut amounts (numbers), separating each one by a space, or enter \'cancel\' to return.");
        langMap.put("stageEditorCatchFishPrompt", "Enter number of fish to catch, or 0 to clear the fish catch objective, or -1 to cancel");
        langMap.put("stageEditorKillPlayerPrompt", "Enter number of players to kill, or 0 to clear the player kill objective, or -1 to cancel");
        langMap.put("stageEditorEnchantTypePrompt", "Enter enchantment names, separating each one by a comma, or enter \'cancel\' to return.");
        langMap.put("stageEditorEnchantAmountsPrompt", "Enter enchant amounts (numbers), separating each one by a space, or enter \'cancel\' to return.");
        langMap.put("stageEditorItemNamesPrompt", "Enter item names, separating each one by a space, or enter \'cancel\' to return.");
        langMap.put("stageEditorNPCPrompt", "Enter NPC ids, separating each one by a space, or enter \'cancel\' to return.");
        langMap.put("stageEditorNPCToTalkToPrompt", "Enter NPC IDs, separating each one by a space, or enter \'clear\' to clear the NPC ID list, or \'cancel\' to return.");
        langMap.put("stageEditorDeliveryMessagesPrompt", "Enter delivery messages, separating each one by a semi-colon or enter \'cancel\' to return.");
        langMap.put("stageEditorKillNPCsPrompt", "Enter kill amounts (numbers), separating each one by a space, or enter \'cancel\' to return.");
        langMap.put("stageEditorMobsPrompt", "Enter mob names separating each one by a space, or enter 'cancel' to return");
        langMap.put("stageEditorMobAmountsPrompt", "Enter mob amounts separating each one by a space, or enter 'cancel' to return");
        langMap.put("stageEditorMobLocationPrompt", "Right-click on a block to select it, then enter 'add' to add it to the kill location list, or enter 'cancel' to return");
        langMap.put("stageEditorMobLocationRadiiPrompt", "Enter kill location radii (number of blocks) separating each one by a space, or enter 'cancel' to return");
        langMap.put("stageEditorMobLocationNamesPrompt", "Enter location names separating each one by a comma, or enter 'cancel' to return");
        langMap.put("stageEditorReachLocationPrompt", "Right-click on a block to select it, then enter 'add' to add it to the reach location list, or enter 'cancel' to return");
        langMap.put("stageEditorReachLocationRadiiPrompt", "Enter reach location radii (number of blocks) separating each one by a space, or enter 'cancel' to return");
        langMap.put("stageEditorReachLocationNamesPrompt", "Enter location names separating each one by a comma, or enter 'cancel' to return");
        langMap.put("stageEditorTameAmountsPrompt", "Enter tame amounts separating each one by a space, or enter 'cancel' to return");
        langMap.put("stageEditorShearColorsPrompt", "Enter sheep colors separating each one by a space, or enter 'cancel' to return");
        langMap.put("stageEditorShearAmountsPrompt", "Enter shear amounts separating each one by a space, or enter 'cancel' to return");
        langMap.put("stageEditorEventsPrompt", "Enter an event name, or enter 'clear' to clear the event, or 'cancel' to return");
        langMap.put("stageEditorChatEventsPrompt", "Enter an event name to add, or enter 'clear' to clear all chat events, or 'cancel' to return");
        langMap.put("stageEditorChatEventsTriggerPromptA", "Enter a chat trigger for");
        langMap.put("stageEditorChatEventsTriggerPromptB", "or enter 'cancel' to return.");
        langMap.put("stageEditorDelayPrompt", "Enter time (in seconds), or enter 'clear' to clear the delay, or 'cancel' to return");
        langMap.put("stageEditorDelayMessagePrompt", "Enter delay message, or enter 'clear' to clear the message, or 'cancel' to return");
        langMap.put("stageEditorScriptPrompt", "Enter script name, or enter 'clear' to clear the script, or 'cancel' to return");
        langMap.put("stageEditorStartMessagePrompt", "Enter start message, or enter 'clear' to clear the message, or 'cancel' to return");
        langMap.put("stageEditorCompleteMessagePrompt", "Enter complete message, or enter 'clear' to clear the message, or 'cancel' to return");
        langMap.put("stageEditorPasswordDisplayPrompt", "Enter a password display, or 'cancel' to return");
        langMap.put("stageEditorPasswordDisplayHint", "(This is the text that will be displayed to the player as their objective)");
        langMap.put("stageEditorPasswordPhrasePrompt", "Enter a password phrase, or 'cancel' to return");
        langMap.put("stageEditorPasswordPhraseHint1", "(This is the text that a player has to say to complete the objective)");
        langMap.put("stageEditorPasswordPhraseHint2", "If you want multiple password phrases, seperate them by a | (pipe)");

        langMap.put("stageEditorObjectiveOverridePrompt", "Enter objective display override, or 'clear' to clear the override, or 'cancel' to return.");
        langMap.put("stageEditorObjectiveOverrideHint", "(The objective display override will show up as the players current objective)");
        langMap.put("stageEditorObjectiveOverrideCleared", "Objective display override cleared.");

        langMap.put("stageEditorDeliveryAddItem", "Add item");
        langMap.put("stageEditorDeliveryNPCs", "Set NPC IDs");
        langMap.put("stageEditorDeliveryMessages", "Set delivery messages");

        langMap.put("stageEditorContainsDuplicates", "List contains duplicates!");
        langMap.put("stageEditorInvalidBlockName", "is not a valid block name!");
        langMap.put("stageEditorInvalidEnchantment", "is not a valid enchantment name!");
        langMap.put("stageEditorInvalidNPC", "is not a valid NPC ID!");
        langMap.put("stageEditorInvalidMob", "is not a valid mob name!");
        langMap.put("stageEditorInvalidItemName", "is not a valid item name!");
        langMap.put("stageEditorInvalidNumber", "is not a number!");
        langMap.put("stageEditorInvalidDye", "is not a valid dye color!");
        langMap.put("stageEditorInvalidEvent", "is not a valid event name!");
        langMap.put("stageEditorDuplicateEvent", "Event is already in the list!");
        langMap.put("stageEditorInvalidDelay", "Delay must be at least one second!");
        langMap.put("stageEditorInvalidScript", "Denizen script not found!");

        langMap.put("stageEditorNoCitizens", "Citizens is not installed!");
        langMap.put("stageEditorNoDenizen", "Denizen is not installed!");

        langMap.put("stageEditorPositiveAmount", "You must enter a positive number!");
        langMap.put("stageEditorNoNumber", "Input was not a number!");
        langMap.put("stageEditorNotGreaterThanZero", "is not greater than 0!");
        langMap.put("stageEditorNotListofNumbers", "Invalid entry, input was not a list of numbers!");
        langMap.put("stageEditorNoDelaySet", "You must set a delay first!");
        langMap.put("stageEditorNoBlockNames", "You must set block names first!");
        langMap.put("stageEditorNoEnchantments", "You must set enchantments first!");
        langMap.put("stageEditorNoItems", "You must add items first!");
        langMap.put("stageEditorNoDeliveryMessage", "You must set at least one delivery message!");
        langMap.put("stageEditorNoNPCs", "You must set NPC IDs first!");
        langMap.put("stageEditorNoMobTypes", "You must set mob types first!");
        langMap.put("stageEditorNoKillLocations", "You must set kill locations first!");
        langMap.put("stageEditorNoBlockSelected", "You must select a block first.");
        langMap.put("stageEditorNoColors", "You must set colors first!");
        langMap.put("stageEditorNoLocations", "You must set locations first!");

        langMap.put("stageEditorNoEnchantmentsSet", "No enchantments set");
        langMap.put("stageEditorNoItemsSet", "No items set");
        langMap.put("stageEditorNoMobTypesSet", "No mob types set");
        langMap.put("stageEditorNoLocationsSet", "No locations set");
        langMap.put("stageEditorNoColorsSet", "No colors set");

        langMap.put("stageEditorListNotSameSize", "The block names list and the amounts list are not the same size!");
        langMap.put("stageEditorEnchantmentNotSameSize", "The enchantments list, the item id list and the enchant amount list are not the same size!");
        langMap.put("stageEditorDeliveriesNotSameSize", "The item list and the NPC list are not equal in size!");
        langMap.put("stageEditorNPCKillsNotSameSize", "The NPC IDs list and the kill amounts list are not the same size!");
        langMap.put("stageEditorAllListsNotSameSize", "All of your lists are not the same size!");
        langMap.put("stageEditorMobTypesNotSameSize", "The mob types list and the mob amounts list are not the same size!");
        langMap.put("stageEditorTameMobsNotSameSize", "The mob types list and the tame amounts list are not the same size!");
        langMap.put("stageEditorShearNotSameSize", "The sheep colors list and the shear amounts list are not the same size!");

        langMap.put("stageEditorMustSetPasswordDisplays", "You must add at least one password display first!");
        langMap.put("stageEditorAddPasswordCleared", "Password Objectives cleared.");
        langMap.put("stageEditorPasswordNotSameSize", "The password display and password phrase lists are not the same size!");

        langMap.put("stageEditorListContainsDuplicates", " List contains duplicates!");

        langMap.put("stageEditorDelayCleared", "Delay cleared.");
        langMap.put("stageEditorDelayMessageCleared", "Delay message cleared.");
        langMap.put("stageEditorDenizenCleared", "Denizen script cleared.");

        langMap.put("stageEditorBreakBlocksCleared", "Break blocks objective cleared.");
        langMap.put("stageEditorDamageBlocksCleared", "Damage blocks objective cleared.");
        langMap.put("stageEditorPlaceBlocksCleared", "Place blocks objective cleared.");
        langMap.put("stageEditorUseBlocksCleared", "Use blocks objective cleared.");
        langMap.put("stageEditorCutBlocksCleared", "Cut blocks objective cleared.");
        langMap.put("stageEditorEnchantmentsCleared", "Enchantment objective cleared.");
        langMap.put("stageEditorDeliveriesCleared", "Delivery objective cleared.");
        langMap.put("stageEditorReachLocationsCleared", "Reach Locations objective cleared.");
        langMap.put("stageEditorKillNPCsCleared", "Kill NPCs objective cleared.");
        langMap.put("stageEditorKillMobsCleared", "Kill Mobs objective cleared.");
        langMap.put("stageEditorTameCleared", "Tame Mobs objective cleared.");
        langMap.put("stageEditorShearCleared", "Shear Sheep objective cleared.");
        langMap.put("stageEditorStartMessageCleared", "Start message cleared.");
        langMap.put("stageEditorCompleteMessageCleared", "Complete message cleared.");

        langMap.put("stageEditorConfirmStageDelete", "Are you sure you want to delete this stage?");
        langMap.put("stageEditorConfirmStageNote", "Any Stages after will be shifted back one spot");
        langMap.put("stageEditorDeleteSucces", "Stage deleted successfully.");

        langMap.put("stageEditorEnchantments", "Enchantments");
        langMap.put("stageEditorNPCNote", "Note: You may specify the name of the NPC with <npc>");
        langMap.put("stageEditorOptional", "Optional");
        langMap.put("stageEditorColors", "Sheep Colors");

        langMap.put("allListsNotSameSize", "All of your lists are not the same size!");
		//Events

        langMap.put("eventEditorCreate", "Create new Event");
        langMap.put("eventEditorEdit", "Edit an Event");
        langMap.put("eventEditorDelete", "Delete an Event");
        langMap.put("eventEditorCreatePermisssions", "You do not have permission to create new Events.");
        langMap.put("eventEditorEditPermisssions", "You do not have permission to edit Events.");
        langMap.put("eventEditorDeletePermisssions", "You do not have permission to delete Events.");

        langMap.put("eventEditorNoneToEdit", "No Events currently exist to be edited!");
        langMap.put("eventEditorNoneToDelete", "No Events currently exist to be deleted!");
        langMap.put("eventEditorNotFound", "Event not found!");
        langMap.put("eventEditorExists", "Event already exists!");
        langMap.put("eventEditorSomeone", "Someone is already creating or editing an Event with that name!");
        langMap.put("eventEditorAlpha", "Name must be alphanumeric!");

        langMap.put("eventEditorErrorReadingFile", "Error reading Events file.");
        langMap.put("eventEditorErrorSaving", "An error occurred while saving.");
        langMap.put("eventEditorDeleted", "Event deleted, Quests and Events reloaded.");
        langMap.put("eventEditorSaved", "Event saved, Quests and Events reloaded.");

        langMap.put("eventEditorEnterEventName", "Enter an Event name, or 'cancel' to return.");
        langMap.put("eventEditorDeletePrompt", "Are you sure you want to delete the Event");
        langMap.put("eventEditorQuitWithoutSaving", "Are you sure you want to quit without saving?");
        langMap.put("eventEditorFinishAndSave", "Are you sure you want to finish and save the Event");
        langMap.put("eventEditorModifiedNote", "Note: You have modified an Event that the following Quests use:");
        langMap.put("eventEditorForcedToQuit", "If you save the Event, anyone who is actively doing any of these Quests will be forced to quit them.");

        langMap.put("eventEditorEventInUse", "The following Quests use the Event");
        langMap.put("eventEditorMustModifyQuests", "eventEditorNotFound");
        langMap.put("eventEditorListSizeMismatch", "The lists are not the same size!");
        langMap.put("eventEditorListDuplicates", "List contains duplicates!");
        langMap.put("eventEditorNotANumberList", "Input was not a list of numbers!");
        langMap.put("eventEditorInvalidEntry", "Invalid entry");

        langMap.put("eventEditorSetName", "Set name");
        langMap.put("eventEditorSetMessage", "Set message");

        langMap.put("eventEditorClearInv", "Clear player inventory");
        langMap.put("eventEditorFailQuest", "Fail the quest");
        langMap.put("eventEditorSetExplosions", "Set explosion locations");
        langMap.put("eventEditorSetLightning", "Set lightning strike locations");
        langMap.put("eventEditorSetEffects", "Set effects");
        langMap.put("eventEditorSetStorm", "Set storm");
        langMap.put("eventEditorSetThunder", "Set thunder");
        langMap.put("eventEditorSetMobSpawns", "Set mob spawns");
        langMap.put("eventEditorSetPotionEffects", "Set potion effects");
        langMap.put("eventEditorSetHunger", "Set player hunger level");
        langMap.put("eventEditorSetSaturation", "Set player saturation level");
        langMap.put("eventEditorSetHealth", "Set player health level");
        langMap.put("eventEditorSetTeleport", "Set player teleport location");
        langMap.put("eventEditorSetCommands", "Set commands to execute");

        langMap.put("eventEditorItems", "Event Items");
        langMap.put("eventEditorSetItems", "Give items");
        langMap.put("eventEditorItemsCleared", "Event items cleared.");
        langMap.put("eventEditorAddItem", "Add item");
        langMap.put("eventEditorSetItemNames", "Set item names");
        langMap.put("eventEditorSetItemAmounts", "Set item amounts");
        langMap.put("eventEditorNoNames", "No names set");
        langMap.put("eventEditorMustSetNames", "You must set item names first!");
        langMap.put("eventEditorInvalidName", "is not a valid item name!");
        langMap.put("eventEditorNotGreaterThanZero", "is not greater than 0!");
        langMap.put("eventEditorNotANumber", "is not a number!");

        langMap.put("eventEditorStorm", "Event Storm");
        langMap.put("eventEditorSetWorld", "Set world");
        langMap.put("eventEditorSetDuration", "Set duration");
        langMap.put("eventEditorNoWorld", "(No world set)");
        langMap.put("eventEditorSetWorldFirst", "You must set a world first!");
        langMap.put("eventEditorInvalidWorld", "is not a valid world name!");
        langMap.put("eventEditorMustSetStormDuration", "You must set a storm duration!");
        langMap.put("eventEditorStormCleared", "Storm data cleared.");
        langMap.put("eventEditorEnterStormWorld", "Enter a world name for the storm to occur in, or enter 'cancel' to return");
        langMap.put("eventEditorEnterDuration", "Enter duration (in seconds)");
        langMap.put("eventEditorAtLeastOneSecond", "Amount must be at least 1 second!");
        langMap.put("eventEditorNotGreaterThanOneSecond", "is not greater than 1 second!");

        langMap.put("eventEditorThunder", "Event Thunder");
        langMap.put("eventEditorMustSetThunderDuration", "You must set a thunder duration!");
        langMap.put("eventEditorThunderCleared", "Thunder data cleared.");
        langMap.put("eventEditorEnterThunderWorld", "Enter a world name for the thunder to occur in, or enter 'cancel' to return");

        langMap.put("eventEditorEffects", "Event Effects");
        langMap.put("eventEditorAddEffect", "Add effect");
        langMap.put("eventEditorAddEffectLocation", "Add effect location");
        langMap.put("eventEditorNoEffects", "No effects set");
        langMap.put("eventEditorMustAddEffects", "You must add effects first!");
        langMap.put("eventEditorInvalidEffect", "is not a valid effect name!");
        langMap.put("eventEditorEffectsCleared", "Event effects cleared.");
        langMap.put("eventEditorEffectLocationPrompt", "Right-click on a block to play an effect at, then enter 'add' to add it to the list, or enter 'cancel' to return");

        langMap.put("eventEditorMobSpawns", "Event Mob Spawns");
        langMap.put("eventEditorAddMobTypes", "Add mob");
        langMap.put("eventEditorNoTypesSet", "(No type set)");
        langMap.put("eventEditorMustSetMobTypesFirst", "You must set the mob type first!");
        langMap.put("eventEditorSetMobAmounts", "Set mob amount");
        langMap.put("eventEditorNoAmountsSet", "(No amounts set)");
        langMap.put("eventEditorMustSetMobAmountsFirst", "You must set mob amount first!");
        langMap.put("eventEditorAddSpawnLocation", "Set spawn location");
        langMap.put("eventEditorMobSpawnsCleared", "Mob spawns cleared.");
        langMap.put("eventEditorMustSetMobLocationFirst", "You must set a spawn-location first!");
        langMap.put("eventEditorInvalidMob", "is not a valid mob name!");
        langMap.put("eventEditorSetMobName", "Set custom name for mob");
        langMap.put("eventEditorSetMobType", "Set mob type");
        langMap.put("eventEditorSetMobItemInHand", "Set item in hand");
        langMap.put("eventEditorSetMobItemInHandDrop", "Set drop chance of item in hand");
        langMap.put("eventEditorSetMobBoots", "Set boots");
        langMap.put("eventEditorSetMobBootsDrop", "Set drop chance of boots");
        langMap.put("eventEditorSetMobLeggings", "Set leggings");
        langMap.put("eventEditorSetMobLeggingsDrop", "Set drop chance of leggings");
        langMap.put("eventEditorSetMobChestPlate", "Set chest plate");
        langMap.put("eventEditorSetMobChestPlateDrop", "Set drop chance of chest plate");
        langMap.put("eventEditorSetMobHelmet", "Set helmet");
        langMap.put("eventEditorSetMobHelmetDrop", "Set drop chance of helmet");
        langMap.put("eventEditorSetMobSpawnLoc", "Right-click on a block to spawn a mob at, then enter 'add' to the confirm it, or enter 'cancel' to return");
        langMap.put("eventEditorSetMobSpawnAmount", "Set the amount of mobs to spawn");
        langMap.put("eventEditorSetDropChance", "Set the drop chance");
        langMap.put("eventEditorInvalidDropChance", "Drop chance has to be between 0.0 and 1.0");

        langMap.put("eventEditorLightningPrompt", "Right-click on a block to spawn a lightning strike at, then enter 'add' to add it to the list, or enter 'clear' to clear the locations list, or 'cancel' to return");

        langMap.put("eventEditorPotionEffects", "Event Potion Effects");
        langMap.put("eventEditorSetPotionEffectTypes", "Set potion effect types");
        langMap.put("eventEditorMustSetPotionTypesFirst", "You must set potion effect types first!");
        langMap.put("eventEditorSetPotionDurations", "Set potion effect durations");
        langMap.put("eventEditorMustSetPotionDurationsFirst", "You must set potion effect durations first!");
        langMap.put("eventEditorMustSetPotionTypesAndDurationsFirst", "You must set potion effect types and durations first!");
        langMap.put("eventEditorNoDurationsSet", "(No durations set)");
        langMap.put("eventEditorSetPotionMagnitudes", "Set potion effect magnitudes");
        langMap.put("eventEditorPotionsCleared", "Potion effects cleared.");
        langMap.put("eventEditorInvalidPotionType", "is not a valid potion effect type!");

        langMap.put("eventEditorEnterNPCId", "Enter NPC ID (or -1 to return)");
        langMap.put("eventEditorNoNPCExists", "No NPC exists with that id!");
        langMap.put("eventEditorExplosionPrompt", "Right-click on a block to spawn an explosion at, then enter 'add' to add it to the list, or enter 'clear' to clear the explosions list, or 'cancel' to return");
        langMap.put("eventEditorSelectBlockFirst", "You must select a block first.");
        langMap.put("eventEditorSetMessagePrompt", "Enter message, or enter \'none\' to delete, (or \'cancel\' to return)");
        langMap.put("eventEditorSetItemNamesPrompt", "Enter item names separating each one by a space, or enter 'cancel' to return.");
        langMap.put("eventEditorSetItemAmountsPrompt", "Enter item amounts (numbers) separating each one by a space, or enter 'cancel' to return.");
        langMap.put("eventEditorSetMobTypesPrompt", "Enter mob name, or enter 'cancel' to return");
        langMap.put("eventEditorSetMobAmountsPrompt", "Enter mob amount, or enter 'cancel' to return");
        langMap.put("eventEditorSetMobNamePrompt", "Set the name for this mob, or enter 'cancel' to return");
        langMap.put("eventEditorSetMobLocationPrompt", "Right-click on a block to select it, then enter 'add' to add it to the mob spawn location list, or enter 'cancel' to return");
        langMap.put("eventEditorSetPotionEffectsPrompt", "Enter potion effect types separating each one by a space, or enter 'cancel' to return");
        langMap.put("eventEditorSetPotionDurationsPrompt", "Enter effect durations (in milliseconds) separating each one by a space, or enter 'cancel' to return");
        langMap.put("eventEditorSetPotionMagnitudesPrompt", "Enter potion effect magnitudes separating each one by a space, or enter 'cancel' to return");
        langMap.put("eventEditorSetHungerPrompt", "Enter hunger level, or -1 to remove it");
        langMap.put("eventEditorHungerLevelAtLeastZero", "Hunger level must be at least 0!");
        langMap.put("eventEditorSetSaturationPrompt", "Enter saturation level, or -1 to remove it");
        langMap.put("eventEditorSaturationLevelAtLeastZero", "Saturation level must be at least 0!");
        langMap.put("eventEditorSetHealthPrompt", "Enter health level, or -1 to remove it");
        langMap.put("eventEditorHealthLevelAtLeastZero", "Health level must be at least 0!");
        langMap.put("eventEditorSetTeleportPrompt", "Right-click on a block to teleport the player to, then enter 'done' to finish, or enter 'clear' to clear the teleport location, or 'cancel' to return");
        langMap.put("eventEditorCommandsNote", "Note: You may use <player> to refer to the player's name.");
        langMap.put("eventEditorSetCommandsPrompt", "Enter commands separating each one by a comma, or enter 'clear' to clear the list, or enter 'cancel' to return.");
                //

        //Requirements Prompt
        langMap.put("reqSetMoney", "Set money requirement");
        langMap.put("reqSetQuestPoints", "Set Quest Points requirement");
        langMap.put("reqSetItem", "Set item requirements");
        langMap.put("reqSetPerms", "Set permission requirements");
        langMap.put("reqSetQuest", "Set Quest requirements");
        langMap.put("reqSetQuestBlocks", "Set Quest blocks");
        langMap.put("reqSetMcMMO", "Set mcMMO requirements");
        langMap.put("reqSetHeroes", "Set Heroes requirements");
        langMap.put("reqSetCustom", "Custom requirements");
        langMap.put("reqSetFail", "Set fail requirements message");
        langMap.put("reqSetSkills", "Set skills");
        langMap.put("reqSetSkillAmounts", "Set skill amounts");
        langMap.put("reqHeroesSetPrimary", "Set Primary Class");
        langMap.put("reqHeroesSetSecondary", "Set Secondary Class");

        langMap.put("reqMoneyPrompt", "Enter amount of <money>, or 0 to clear the money requirement, or -1 to cancel");
        langMap.put("reqQuestPointsPrompt", "Enter amount of Quest Points, or 0 to clear the Quest Point requirement, or -1 to cancel");
        langMap.put("reqQuestPrompt", "Enter a list of Quest names separating each one by a <comma>, or enter \'clear\' to clear the list, or \'cancel\' to return.");
        langMap.put("reqRemoveItemsPrompt", "Enter a list of true/false values, separating each one by a space, or enter \'cancel\' to return.");
        langMap.put("reqPermissionsPrompt", "Enter permission requirements separating each one by a space, or enter \'clear\' to clear the list, or enter \'cancel\' to return.");
        langMap.put("reqCustomPrompt", "Enter the name of a custom requirement to add, or enter \'clear\' to clear all custom requirements, or \'cancel\' to return.");
        langMap.put("reqMcMMOPrompt", "Enter mcMMO skills, separating each one by a space, or enter \'clear\' to clear the list, or \'cancel\' to return.");
        langMap.put("reqMcMMOAmountsPrompt", "Enter mcMMO skill amounts, separating each one by a space, or enter \'clear\' to clear the list, or \'cancel\' to return.");
        langMap.put("reqHeroesPrimaryPrompt", "Enter a Heroes Primary Class name, or enter 'clear' to clear the requirement, or 'cancel' to return.");
        langMap.put("reqHeroesSecondaryPrompt", "Enter a Heroes Secondary Class name, or enter 'clear' to clear the requirement, or 'cancel' to return.");
        langMap.put("reqFailMessagePrompt", "Enter fail requirements message, or enter \'cancel\' to return.");

        langMap.put("reqAddItem", "Add item");
        langMap.put("reqSetRemoveItems", "Set remove items");
        langMap.put("reqNoItemsSet", "No items set");
        langMap.put("reqNoValuesSet", "No values set");

        langMap.put("reqHeroesPrimaryDisplay", "Primary Class:");
        langMap.put("reqHeroesSecondaryDisplay", "Secondary Class:");

        langMap.put("reqGreaterThanZero", "Amount must be greater than 0!");
        langMap.put("reqNotAQuestName", "<quest> is not a Quest name!");
        langMap.put("reqItemCleared", "Item requirements cleared.");
        langMap.put("reqListsNotSameSize", "The items list and remove items list are not the same size!");
        langMap.put("reqTrueFalseError", "<input> is not a true or false value!%br%Example: true false true true");
        langMap.put("reqCustomAlreadyAdded", "That custom requirement has already been added!");
        langMap.put("reqCustomNotFound", "Custom requirement module not found.");
        langMap.put("reqCustomCleared", "Custom requirements cleared.");
        langMap.put("reqMcMMOError", "<input> is not an mcMMO skill name!");
        langMap.put("reqMcMMOCleared", "mcMMO skill requirements cleared.");
        langMap.put("reqMcMMOAmountsCleared", "mcMMO skill amount requirements cleared.");
        langMap.put("reqHeroesNotPrimary", "The <class> class is not primary!");
        langMap.put("reqHeroesPrimaryCleared", "Heroes Primary Class requirement cleared.");
        langMap.put("reqHeroesNotSecondary", "The <class> class is not secondary!");
        langMap.put("reqHeroesSecondaryCleared", "Heroes Secondary Class requirement cleared.");
        langMap.put("reqHeroesClassNotFound", "Class not found!");

        langMap.put("reqNone", "No requirements set");
        langMap.put("reqNotANumber", "<input> is not a number!");
        langMap.put("reqMustAddItem", "You must add at least one item first!");
        langMap.put("reqNoMessage", "You must set a fail requirements message!");
        langMap.put("reqNoMcMMO", "mcMMO not installed");
        langMap.put("reqNoHeroes", "Heroes not installed");
                //

        //Rewards Prompt
        langMap.put("rewSetMoney", "Set money reward");
        langMap.put("rewSetQuestPoints", "Set Quest Points reward");
        langMap.put("rewSetItems", "Set item rewards");
        langMap.put("rewSetExperience", "Set experience reward");
        langMap.put("rewSetCommands", "Set command rewards");
        langMap.put("rewSetPermission", "Set permission rewards");
        langMap.put("rewSetMcMMO", "Set mcMMO skill rewards");
        langMap.put("rewSetHeroes", "Set Heroes experience rewards");
        langMap.put("rewSetPhat", "Set PhatLoot rewards");
        langMap.put("rewSetCustom", "Set Custom Rewards");
        langMap.put("rewSetHeroesClasses", "Set classes");
        langMap.put("rewSetHeroesAmounts", "Set experience amounts");

        langMap.put("rewMoneyPrompt", "Enter amount of <money>, or 0 to clear the money reward, or -1 to cancel");
        langMap.put("rewExperiencePrompt", "Enter amount of experience, or 0 to clear the experience reward, or -1 to cancel");
        langMap.put("rewCommandPrompt", "Enter command rewards separating each one by a <comma>, or enter \'clear\' to clear the list, or enter \'cancel\' to return.");
        langMap.put("rewCommandPromptHint", "Note: You may put <player> to specify the player who completed the Quest. e.g. smite <player>");
        langMap.put("rewPermissionsPrompt", "Enter permission rewards separating each one by a space, or enter \'clear\' to clear the list, or enter \'cancel\' to return.");
        langMap.put("rewQuestPointsPrompt", "Enter amount of Quest Points, or 0 to clear the Quest Point reward, or -1 to cancel");
        langMap.put("rewMcMMOPrompt", "Enter mcMMO skills, separating each one by a space, or enter \'cancel\' to return.");
        langMap.put("rewMcMMOPromptHint", "Note: Typing \'All\' will give levels to all skills.");
        langMap.put("rewHeroesClassesPrompt", "Enter Heroes classes separating each one by a space, or enter 'cancel' to return.");
        langMap.put("rewHeroesExperiencePrompt", "Enter experience amounts (numbers, decimals are allowed) separating each one by a space, or enter 'cancel' to return.");
        langMap.put("rewPhatLootsPrompt", "Enter PhatLoots separating each one by a space, or enter 'clear' to clear the list, or 'cancel' to return.");
        langMap.put("rewCustomRewardPrompt", "Enter the name of a custom reward to add, or enter \'clear\' to clear all custom rewards, or \'cancel\' to return.");

        langMap.put("rewItemsCleared", "Item rewards cleared.");
        langMap.put("rewNoMcMMOSkills", "No skills set");
        langMap.put("rewNoHeroesClasses", "No classes set");
        langMap.put("rewSetMcMMOSkillsFirst", "You must set skills first!");
        langMap.put("rewMcMMOCleared", "mcMMO rewards cleared.");
        langMap.put("rewMcMMOListsNotSameSize", "The skills list and skill amounts list are not the same size!");
        langMap.put("rewSetHeroesClassesFirst", "You must set classes first!");
        langMap.put("rewHeroesCleared", "Heroes rewards cleared.");
        langMap.put("rewHeroesListsNotSameSize", "The classes list and experience amounts list are not the same size!");
        langMap.put("rewHeroesInvalidClass", "<input> is not a valid Heroes class name!");
        langMap.put("rewPhatLootsInvalid", "<input> is not a valid PhatLoot name!");
        langMap.put("rewPhatLootsCleared", "PhatLoots reward cleared.");
        langMap.put("rewCustomAlreadyAdded", "That custom reward has already been added!");
        langMap.put("rewCustomNotFound", "Custom reward module not found.");
        langMap.put("rewCustomCleared", "Custom rewards cleared.");

        langMap.put("rewNoPhat", "PhatLoots not installed");
                //

        //Item Prompt
        langMap.put("itemCreateLoadHand", "Load item in hand");
        langMap.put("itemCreateSetName", "Set name");
        langMap.put("itemCreateSetAmount", "Set amount");
        langMap.put("itemCreateSetDurab", "Set durability");
        langMap.put("itemCreateSetEnchs", "Add/clear enchantments");
        langMap.put("itemCreateSetDisplay", "Set display name");
        langMap.put("itemCreateSetLore", "Set lore");

        langMap.put("itemCreateEnterName", "Enter an item name, or 'cancel' to return.");
        langMap.put("itemCreateEnterAmount", "Enter item amount (max. 64), or 'cancel' to return.");
        langMap.put("itemCreateEnterDurab", "Enter item durability, or 'clear' to clear the data, or 'cancel' to return.");
        langMap.put("itemCreateEnterEnch", "Enter an enchantment name, or 'clear' to clear the enchantments, or 'cancel' to return.");
        langMap.put("itemCreateEnterLevel", "Enter a level (number) for <enchantment>");
        langMap.put("itemCreateEnterDisplay", "Enter item display name, or 'clear' to clear the display name, or 'cancel' to return.");
        langMap.put("itemCreateEnterLore", "Enter item lore, separating each line by a semi-colon ; or 'clear' to clear the lore, or 'cancel' to return.");

        langMap.put("itemCreateLoaded", "Item loaded.");
        langMap.put("itemCreateNoItem", "No item in hand!");
        langMap.put("itemCreateNoName", "You must set a name first!");
        langMap.put("itemCreateInvalidName", "Invalid item name!");
        langMap.put("itemCreateInvalidAmount", "Amount must be between 1-64!");
        langMap.put("itemCreateInvalidDurab", "Invalid item durability!");
        langMap.put("itemCreateInvalidEnch", "Invalid enchantment name!");
        langMap.put("itemCreateInvalidLevel", "Level must be greater than 0!");
        langMap.put("itemCreateInvalidInput", "Invalid input!");
        langMap.put("itemCreateNotNumber", "Input was not a number!");

        langMap.put("itemCreateNoNameAmount", "You must set a name and amount first!");
        langMap.put("itemCreateCriticalError", "A critical error has occurred.");
                //

        //Titles
        langMap.put("questTitle", "-- <quest> --");
        langMap.put("questObjectivesTitle", "---(<quest>)---");
        langMap.put("questCompleteTitle", "**QUEST COMPLETE: <quest>**");
        langMap.put("questRewardsTitle", "Rewards:");

        langMap.put("journalTitle", "Quest Journal");

        langMap.put("questsTitle", "- Quests -");
        langMap.put("questHelpTitle", "- Quests -");
        langMap.put("questListTitle", "- Quests -");
        langMap.put("questNPCListTitle", "- Quests | <npc> -");
        langMap.put("questAdminHelpTitle", "- Questadmin -");
        langMap.put("questEditorTitle", "- Quest Editor -");
        langMap.put("eventEditorTitle", "- Event Editor - ");
        langMap.put("questCreateTitle", "- Create Quest -");
        langMap.put("questEditTitle", "- Edit Quest -");
        langMap.put("questDeleteTitle", "- Delete Quest -");
        langMap.put("requirementsTitle", "- <quest> | Requirements -");
        langMap.put("rewardsTitle", "- <quest> | Rewards -");
        langMap.put("itemRequirementsTitle", "- Item Requirements -");
        langMap.put("itemRewardsTitle", "- Item Rewards -");
        langMap.put("mcMMORequirementsTitle", "- mcMMO Requirements -");
        langMap.put("mcMMORewardsTitle", "- mcMMO Rewards -");
        langMap.put("heroesRequirementsTitle", "- Heroes Requirements -");
        langMap.put("heroesRewardsTitle", "- Heroes Rewards -");
        langMap.put("heroesClassesTitle", "- Heroes Classes -");
        langMap.put("heroesExperienceTitle", "- Heroes Experience -");
        langMap.put("heroesPrimaryTitle", "- Primary Classes -");
        langMap.put("heroesSecondaryTitle", "- Secondary Classes -");
        langMap.put("phatLootsRewardsTitle", "- PhatLoots Rewards -");
        langMap.put("customRequirementsTitle", "- Custom Requirements -");
        langMap.put("customRewardsTitle", "- Custom Rewards -");
        langMap.put("skillListTitle", "- Skill List -");
        langMap.put("eventTitle", "- Event -");
        langMap.put("completedQuestsTitle", "- Completed Quests -");
        langMap.put("topQuestersTitle", "- Top <number> Questers -");
        langMap.put("createItemTitle", "- Create Item -");
        langMap.put("enchantmentsTitle", "- Enchantments -");

        langMap.put("questGUITitle", "- GUI Item Display -");
        langMap.put("questRegionTitle", "- Quest Region -");

        langMap.put("eventEditorGiveItemsTitle", "- Give Items -");
        langMap.put("eventEditorEffectsTitle", "- Effects -");
        langMap.put("eventEditorStormTitle", "- Event Storm -");
        langMap.put("eventEditorThunderTitle", "- Event Thunder -");
        langMap.put("eventEditorMobSpawnsTitle", "- Event Mob Spawns -");
        langMap.put("eventEditorMobsTitle", "- Mobs -");
        langMap.put("eventEditorAddMobTypesTitle", "- Add Mob -");
        langMap.put("eventEditorPotionEffectsTitle", "- Event Potion Effects -");
        langMap.put("eventEditorPotionTypesTitle", "- Event Potion Types -");

        langMap.put("eventEditorWorldsTitle", "- Worlds -");
		//

        //Effects
        langMap.put("effBlazeShoot", "Sound of a Blaze firing");
        langMap.put("effBowFire", "Sound of a bow firing");
        langMap.put("effClick1", "A click sound");
        langMap.put("effClick2", "A different click sound");
        langMap.put("effDoorToggle", "Sound of a door opening or closing");
        langMap.put("effExtinguish", "Sound of fire being extinguished");
        langMap.put("effGhastShoot", "Sound of a Ghast firing");
        langMap.put("effGhastShriek", "Sound of a Ghast shrieking");
        langMap.put("effZombieWood", "Sound of a Zombie chewing an iron door");
        langMap.put("effZombieIron", "Sound of a Zombie chewing a wooden door");
        langMap.put("effEnterName", "Enter an effect name to add it to the list, or enter 'cancel' to return");

		//
        //Inputs
        langMap.put("cmdCancel", "cancel");
        langMap.put("cmdAdd", "add");
        langMap.put("cmdClear", "clear");
        langMap.put("cmdNone", "none");
        langMap.put("cmdDone", "done");
		//

        //User end
        langMap.put("acceptQuest", "Accept Quest?");
        langMap.put("enterAnOption", "Enter an option");
        langMap.put("questAccepted", "Quest accepted: <quest>");
        langMap.put("currentQuest", "Current Quests:");
        langMap.put("noMoreQuest", "No more quests available.");

        //Objectives
        langMap.put("damage", "Damage");
        langMap.put("break", "Break");
        langMap.put("place", "Place");
        langMap.put("use", "Use");
        langMap.put("cut", "Cut");
        langMap.put("catchFish", "Catch Fish");
        langMap.put("enchantItem", "Enchant <item> with <enchantment>");
        langMap.put("kill", "Kill");
        langMap.put("killAtLocation", "Kill <mob> at <location>");
        langMap.put("killPlayer", "Kill a Player");
        langMap.put("deliver", "Deliver <item> to <npc>");
        langMap.put("talkTo", "Talk to <npc>");
        langMap.put("tame", "Tame");
        langMap.put("shearSheep", "Shear <color> sheep");
        langMap.put("goTo", "Go to <location>");

        langMap.put("completed", "Completed");
                    //

        langMap.put("invalidSelection", "Invalid selection!");
        langMap.put("noActiveQuest", "You do not currently have any active Quests.");
        langMap.put("speakTo", "Start: Speak to <npc>");
        langMap.put("mustSpeakTo", "You must speak to <npc> to start this Quest.");
        langMap.put("noCommandStart", "<quest> may not be started via command.");
        langMap.put("permissionDisplay", "Permission:");
        langMap.put("heroesClass", "class");
        langMap.put("mcMMOLevel", "level");
        langMap.put("haveCompleted", "You have completed <quest>");
        langMap.put("cannotComplete", "Cannot complete <quest>");
        langMap.put("questNotFound", "Quest not found.");
        langMap.put("alreadyConversing", "You already are in a conversation!");

        langMap.put("inputNum", "Input must be a number.");
        langMap.put("inputPosNum", "Input must be a positive number.");

        langMap.put("killNotValid", "Kill did not count. You must wait <time> before you can kill <player> again.");
        langMap.put("questModified", "Your active Quest <quest> has been modified. You have been forced to quit the Quest.");
        langMap.put("questNotExist", "Your active Quest <quest> no longer exists. You have been forced to quit the Quest.");
        langMap.put("questInvalidChoice", "Invalid choice. Type \'Yes\' or \'No\'");
        langMap.put("questPointsDisplay", "Quest points:");

        langMap.put("questNoDrop", "You may not drop Quest items.");
        langMap.put("questNoBrew", "You may not brew Quest items.");
        langMap.put("questNoStore", "You may not store Quest items.");
        langMap.put("questNoCraft", "You may not craft Quest items.");
        langMap.put("questNoEquip", "You may not equip Quest items.");
        langMap.put("questNoDispense", "You may not put Quest items in dispensers.");
        langMap.put("questNoEnchant", "You may not enchant Quest items.");
        langMap.put("questNoSmelt", "You may not smelt using Quest items.");

        langMap.put("questInfoNoPerms", "You do not have permission to view a Quest's information.");
        langMap.put("questCmdNoPerms", "You do not have access to that command.");

        langMap.put("pageSelectionNum", "Page selection must be a number.");
        langMap.put("pageSelectionPosNum", "Page selection must be a positive number.");
        langMap.put("questListNoPerms", "You do not have permission to view the Quests list.");

        langMap.put("questTakeNoPerms", "You do not have permission to take Quests via commands.");
        langMap.put("questTakeDisabled", "Taking Quests via commands has been disabled.");

        langMap.put("questQuit", "You have quit <quest>");
        langMap.put("questQuitNoPerms", "You do not have permission to quit Quests.");
        langMap.put("questQuitDisabled", "Quitting Quests has been disabled.");

        langMap.put("questEditorNoPerms", "You do not have permission to use the Quests Editor.");

        langMap.put("eventEditorNoPerms", "You do not have permission to use the Events Editor.");

        langMap.put("questsUnknownCommand", "Unknown Quests command. Type /quests for help.");

        langMap.put("pageNotExist", "Page does not exist.");
        langMap.put("pageFooter", "- Page <current> of <all> -");

        //Admin
        langMap.put("questsReloaded", "Quests reloaded.");
        langMap.put("numQuestsLoaded", "<number> Quests loaded.");
        langMap.put("questForceTake", "<player> has forcibly started the Quest <quest>.");
        langMap.put("questForcedTake", "<player> has forced you to take the Quest <quest>.");
        langMap.put("questForceQuit", "<player> has forcibly quit the Quest <quest>.");
        langMap.put("questForcedQuit", "<player> has forced you to quit the Quest <quest>.");
        langMap.put("questForceFinish", "<player> has forcibly finished their Quest <quest>.");
        langMap.put("questForcedFinish", "<player> has forced you to finish your Quest <quest>.");
        langMap.put("questForceNextStage", "<player> has advanced to the next Stage in the Quest <quest>.");
        langMap.put("questForcedNextStage", "<player> has advanced you to the next Stage in your Quest <quest>.");
        langMap.put("questPurged", "<player> has been purged and blacklisted.");
        langMap.put("questReset", "<player> has been reset.");
        langMap.put("questRemoved", "Quest <quest> has been removed from player <player>'s completed Quests.");
        langMap.put("settingAllQuestPoints", "Setting all players' Quest Points...");
        langMap.put("allQuestPointsSet", "All players' Quest Points have been set to <number>!");

        langMap.put("setQuestPoints", "<player>'s Quest Points have been set to <number>.");
        langMap.put("questPointsSet", "<player> has set your Quest Points to <number>.");
        langMap.put("takeQuestPoints", "Took away <number> Quest Points from <player>.");
        langMap.put("questPointsTaken", "<player> took away <number> Quest Points.");
        langMap.put("giveQuestPoints", "Gave <number> Quest Points from <player>.");
        langMap.put("questPointsGiven", "<player> gave you <number> Quest Points.");

        langMap.put("enableNPCGUI", "<npc> will now provide a GUI Quest Display.");
        langMap.put("disableNPCGUI", "<npc> will no longer provide a GUI Quest Display.");

        langMap.put("invalidNumber", "Invalid number.");
        langMap.put("noCurrentQuest", "<player> does not currently have any active Quests.");
        langMap.put("playerNotFound", "Player not found.");
        langMap.put("invalidStageNum", "Invalid stage number for Quest <quest>");
        langMap.put("errorNPCID", "Error: There is no NPC with ID <number>");
        langMap.put("errorReading", "Error reading <file>, skipping..");
        langMap.put("errorReadingSuppress", "Error reading <file>, suppressing further errors.");
        langMap.put("errorDataFolder", "Error: Unable to read Quests data folder!");
        langMap.put("questsPlayerHasQuestAlready", "<player> is already on the Quest <quest>!");
        langMap.put("questsUnknownAdminCommand", "Unknown Questsadmin command. Type /questsadmin for help.");
        langMap.put("unknownError", "An unknown error occurred. See console output.");

        langMap.put("journalTaken", "You take out your Quest Journal.");
        langMap.put("journalPutAway", "You put away your Quest Journal.");
        langMap.put("journalAlreadyHave", "You already have your Quest Journal out.");
        langMap.put("journalNoRoom", "You have no room in your inventory for your Quest Journal!");
        langMap.put("journalNoQuests", "You have no accepted quests!");
        langMap.put("journalDenied", "You cannot do that with your Quest Journal.");
                    //

                //
        //Enchantments
        langMap.put("ENCHANTMENT_ARROW_DAMAGE", "Power");
        langMap.put("ENCHANTMENT_ARROW_FIRE", "Flame");
        langMap.put("ENCHANTMENT_ARROW_INFINITE", "Infinity");
        langMap.put("ENCHANTMENT_ARROW_KNOCKBACK", "Punch");
        langMap.put("ENCHANTMENT_DAMAGE_ALL", "Sharpness");
        langMap.put("ENCHANTMENT_DAMAGE_ARTHROPODS", "BaneOfArthropods");
        langMap.put("ENCHANTMENT_DEPTH_STRIDER", "DepthStrider");
        langMap.put("ENCHANTMENT_DAMAGE_UNDEAD", "Smite");
        langMap.put("ENCHANTMENT_DIG_SPEED", "Efficiency");
        langMap.put("ENCHANTMENT_DURABILITY", "Unbreaking");
        langMap.put("ENCHANTMENT_FIRE_ASPECT", "FireAspect");
        langMap.put("ENCHANTMENT_FROST_WALKER", "FrostWalker");
        langMap.put("ENCHANTMENT_KNOCKBACK", "Knockback");
        langMap.put("ENCHANTMENT_LOOT_BONUS_BLOCKS", "Fortune");
        langMap.put("ENCHANTMENT_LOOT_BONUS_MOBS", "Looting");
        langMap.put("ENCHANTMENT_LUCK", "LuckOfTheSea");
        langMap.put("ENCHANTMENT_LURE", "Lure");
        langMap.put("ENCHANTMENT_MENDING", "Mending");
        langMap.put("ENCHANTMENT_OXYGEN", "Respiration");
        langMap.put("ENCHANTMENT_PROTECTION_ENVIRONMENTAL", "Protection");
        langMap.put("ENCHANTMENT_PROTECTION_EXPLOSIONS", "BlastProtection");
        langMap.put("ENCHANTMENT_PROTECTION_FALL", "FeatherFalling");
        langMap.put("ENCHANTMENT_PROTECTION_FIRE", "FireProtection");
        langMap.put("ENCHANTMENT_PROTECTION_PROJECTILE", "ProjectileProtection");
        langMap.put("ENCHANTMENT_SILK_TOUCH", "SilkTouch");
        langMap.put("ENCHANTMENT_THORNS", "Thorns");
        langMap.put("ENCHANTMENT_WATER_WORKER", "AquaAffinity");

        //Colors
        langMap.put("COLOR_BLACK", "Black");
        langMap.put("COLOR_BLUE", "Blue");
        langMap.put("COLOR_BROWN", "Brown");
        langMap.put("COLOR_CYAN", "Cyan");
        langMap.put("COLOR_GRAY", "Gray");
        langMap.put("COLOR_GREEN", "Green");
        langMap.put("COLOR_LIGHT_BLUE", "LightBlue");
        langMap.put("COLOR_LIME", "Lime");
        langMap.put("COLOR_MAGENTA", "Magenta");
        langMap.put("COLOR_ORANGE", "Orange");
        langMap.put("COLOR_PINK", "Pink");
        langMap.put("COLOR_PURPLE", "Purple");
        langMap.put("COLOR_RED", "Red");
        langMap.put("COLOR_SILVER", "Silver");
        langMap.put("COLOR_WHITE", "White");
        langMap.put("COLOR_YELLOW", "Yellow");

                //Time
        langMap.put("timeDay", "Day");
        langMap.put("timeDays", "Days");
        langMap.put("timeHour", "Hour");
        langMap.put("timeHours", "Hours");
        langMap.put("timeMinute", "Minute");
        langMap.put("timeMinutes", "Minutes");
        langMap.put("timeSecond", "Second");
        langMap.put("timeSeconds", "Seconds");
        langMap.put("timeMillisecond", "Millisecond");
        langMap.put("timeMilliseconds", "Milliseconds");

        //Misc
        langMap.put("event", "Event");
        langMap.put("delay", "Delay");
        langMap.put("save", "Save");
        langMap.put("exit", "Exit");
        langMap.put("exited", "Exited");
        langMap.put("cancel", "Cancel");
        langMap.put("cancelled", "Cancelled");
        langMap.put("questTimeout", "Cancelled.");
        langMap.put("back", "Back");
        langMap.put("yesWord", "Yes");
        langMap.put("noWord", "No");
        langMap.put("true", "true");
        langMap.put("false", "false");
        langMap.put("clear", "Clear");
        langMap.put("edit", "Edit");
        langMap.put("none", "None");
        langMap.put("done", "Done");
        langMap.put("comma", "comma");
        langMap.put("finish", "Finish");
        langMap.put("quit", "Quit");
        langMap.put("noneSet", "None set");
        langMap.put("noDelaySet", "No delay set");
        langMap.put("noIdsSet", "No IDs set");
        langMap.put("noNamesSet", "No names set");
        langMap.put("worlds", "Worlds");
        langMap.put("mobs", "Mobs");
        langMap.put("points", "points");
        langMap.put("invalidOption", "Invalid option!");
        langMap.put("npcHint", "Note: You can left or right click on NPC's to get their ID.");
        langMap.put("listDuplicate", "List contains duplicates!");
        langMap.put("id", "ID");
        langMap.put("quest", "Quest");
        langMap.put("quests", "Quests");
        langMap.put("createdBy", "Created by");
        langMap.put("questPoints", "Quest Points");
        langMap.put("complete", "Complete");
        langMap.put("redoable", "Redoable");
        langMap.put("usage", "Usage");
        langMap.put("redoableEvery", "Redoable every <time>.");
        langMap.put("requirements", "Requirements");
        langMap.put("money", "Money");
        langMap.put("with", "With");
        langMap.put("to", "to");
        langMap.put("blocksWithin", "within <amount> blocks of");
        langMap.put("valRequired", "Value required");

        langMap.put("enchantedItem", "*Enchanted*");
        langMap.put("experience", "Experience");
		//

        //Error Messages
        langMap.put("questErrorReadingFile", "Error reading Quests file.");
        langMap.put("questSaveError", "An error occurred while saving.");
        langMap.put("questBlacklisted", "You are blacklisted. Contact an admin if this is in error.");
                //

        File file = new File(plugin.getDataFolder(), "/lang/" + lang + ".yml");
        YamlConfiguration langFile = YamlConfiguration.loadConfiguration(file);

        for (Entry<String, Object> e : langFile.getValues(true).entrySet()) {
            langMap.put(e.getKey(), (String) e.getValue());
        }

    }

    public void saveNewLang() {

        FileConfiguration data = new YamlConfiguration();
        File dir = new File(plugin.getDataFolder(), "/lang");
        File file = new File(plugin.getDataFolder(), "/lang/en.yml");

        for (Entry<String, String> e : langMap.entrySet()) {
            data.set(e.getKey(), e.getValue());
        }

        try {

            if (dir.exists() == false || dir.isDirectory() == false) {
                dir.mkdir();
            }

            PrintWriter out = new PrintWriter(file);

            for (String key : data.getKeys(false)) {

                out.println(key + ": \"" + data.getString(key) + "\"");

            }

            out.close();

        } catch (IOException e1) {
            e1.printStackTrace();
        }

    }

    public void loadLang() {

        File langFile = new File(plugin.getDataFolder(), "/lang/" + lang + ".yml");
        boolean newLangs = false;

        if (langFile.exists()) {

            LinkedHashMap<String, String> tempMap = new LinkedHashMap<String, String>();
            LinkedHashMap<String, String> toPut = new LinkedHashMap<String, String>();
            LinkedHashMap<String, String> newMap = new LinkedHashMap<String, String>();
            FileConfiguration config = new YamlConfiguration();

            try {

                config.load(langFile);
                for (String key : config.getKeys(false)) {

                    tempMap.put(key, config.getString(key));

                }

                for (Entry<String, String> entry : langMap.entrySet()) {

                    if (tempMap.containsKey(entry.getKey())) {

                        toPut.put(entry.getKey(), tempMap.get(entry.getKey()));

                    } else {

                        newLangs = true;
                        newMap.put(entry.getKey(), entry.getValue());

                    }

                }

                langMap.putAll(toPut);

                if (newLangs) {

                    File file = new File(plugin.getDataFolder(), "/lang/" + lang + "_new.yml");
                    if (file.exists()) {
                        file.delete();
                    }

                    FileConfiguration config2 = new YamlConfiguration();

                    try {

                        for (Entry<String, String> entry : newMap.entrySet()) {

                            config2.set(entry.getKey(), entry.getValue());

                        }

                        config2.save(file);

                    } catch (Exception e) {
                        plugin.getLogger().severe("An error occurred while trying to dump new language phrases. Operation aborted. Error log:");
                        e.printStackTrace();
                        return;
                    }

                    plugin.getLogger().info("There are new language phrases with the current version. They have been stored in /lang/"+ lang + "_new.yml");

                }

            } catch (Exception e) {
                plugin.getLogger().severe("There was an error reading the language file: /lang/" + lang + ".yml");
                plugin.getLogger().severe("Language loading aborted. Error log:");
                e.printStackTrace();
            }

        } else {

            plugin.getLogger().severe("Attempted to load language file: /lang/" + lang + ".yml but the file was not found. Using default language EN");

        }
    }

    private static class LangToken {

        Map<String, String> tokenMap = new HashMap<String, String>();

        public void initTokens() {

            tokenMap.put("%br%", "\n");
            tokenMap.put("%tab%", "\t");
            tokenMap.put("%rtr%", "\r");
            tokenMap.put("%bold%", ChatColor.BOLD.toString());
            tokenMap.put("%italic%", ChatColor.ITALIC.toString());
            tokenMap.put("%underline%", ChatColor.UNDERLINE.toString());
            tokenMap.put("%strikethrough%", ChatColor.STRIKETHROUGH.toString());
            tokenMap.put("%magic%", ChatColor.MAGIC.toString());
            tokenMap.put("%reset%", ChatColor.RESET.toString());
            tokenMap.put("%white%", ChatColor.WHITE.toString());
            tokenMap.put("%black%", ChatColor.BLACK.toString());
            tokenMap.put("%aqua%", ChatColor.AQUA.toString());
            tokenMap.put("%darkaqua%", ChatColor.DARK_AQUA.toString());
            tokenMap.put("%blue%", ChatColor.BLUE.toString());
            tokenMap.put("%darkblue%", ChatColor.DARK_BLUE.toString());
            tokenMap.put("%gold%", ChatColor.GOLD.toString());
            tokenMap.put("%gray%", ChatColor.GRAY.toString());
            tokenMap.put("%darkgray%", ChatColor.DARK_GRAY.toString());
            tokenMap.put("%pink%", ChatColor.LIGHT_PURPLE.toString());
            tokenMap.put("%purple%", ChatColor.DARK_PURPLE.toString());
            tokenMap.put("%green%", ChatColor.GREEN.toString());
            tokenMap.put("%darkgreen%", ChatColor.DARK_GREEN.toString());
            tokenMap.put("%red%", ChatColor.RED.toString());
            tokenMap.put("%darkred%", ChatColor.DARK_RED.toString());
            tokenMap.put("%yellow%", ChatColor.YELLOW.toString());

        }

        public String convertString(String s) {

            for (String token : tokenMap.keySet()) {
                s = s.replace(token, tokenMap.get(token));
                s = s.replace(token.toUpperCase(), tokenMap.get(token));

            }
            return s;

        }

    }

}