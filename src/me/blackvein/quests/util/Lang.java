package me.blackvein.quests.util;

import java.util.HashMap;
import java.util.Map;

public class Lang {

    public static String lang = "en";
    private static Map<String, String> en = new HashMap<String, String>();
    private static Map<String, String> fr = new HashMap<String, String>();
    private static Map<String, String> es = new HashMap<String, String>();
    private static Map<String, String> de = new HashMap<String, String>();
    private static Map<String, String> sv = new HashMap<String, String>();
    private static Map<String, String> nl = new HashMap<String, String>();
    private static Map<String, String> pl = new HashMap<String, String>();
    private static Map<String, String> da = new HashMap<String, String>();
    private static Map<String, String> zh = new HashMap<String, String>();
    private static Map<String, String> no = new HashMap<String, String>();

    public static String get(String key){

        if(lang.equals("en"))
            return en.get(key);
        else if(lang.equals("fr"))
            return fr.get(key);
        else if(lang.equals("es"))
            return es.get(key);
        else if(lang.equals("de"))
            return de.get(key);
        else if(lang.equals("sv"))
            return sv.get(key);
        else if(lang.equals("nl"))
            return nl.get(key);
        else if(lang.equals("pl"))
            return pl.get(key);
        else if(lang.equals("da"))
            return da.get(key);
        else if(lang.equals("zh"))
            return zh.get(key);
        else if(lang.equals("no"))
            return no.get(key);
        else
            return en.get(key);

    }

    public static void initPhrases(){

        //English

            //Quests
            en.put("enterQuestName", "Enter Quest name (or \"cancel\" to return)");


            //


            //Events
            en.put("eventEditorTitle", "Event Editor");
            en.put("eventEditorCreate", "Create new Event");
            en.put("eventEditorEdit", "Edit an Event");
            en.put("eventEditorDelete", "Delete an Event");

            en.put("eventEditorNoneToEdit", "No Events currently exist to be edited!");
            en.put("eventEditorNoneToDelete", "No Events currently exist to be deleted!");
            en.put("eventEditorNotFound", "Event not found!");
            en.put("eventEditorExists", "Event already exists!");
            en.put("eventEditorSomeone", "Someone is already creating or editing an Event with that name!");
            en.put("eventEditorAlpha", "Name must be alphanumeric!");

            en.put("eventEditorErrorReadingFile", "Error reading Events file.");
            en.put("eventEditorErrorSaving", "An error occurred while saving.");
            en.put("eventEditorDeleted", "Event deleted, Quests and Events reloaded.");
            en.put("eventEditorSaved", "Event saved, Quests and Events reloaded.");

            en.put("eventEditorEnterEventName", "Enter an Event name, or \"cancel\" to return.");
            en.put("eventEditorDeletePrompt", "Are you sure you want to delete the Event");
            en.put("eventEditorQuitWithoutSaving", "Are you sure you want to quit without saving?");
            en.put("eventEditorFinishAndSave", "Are you sure you want to finish and save the Event");
            en.put("eventEditorModifiedNote", "Note: You have modified an Event that the following Quests use:");
            en.put("eventEditorForcedToQuit", "If you save the Event, anyone who is actively doing any of these Quests will be forced to quit them.");

            en.put("eventEditorEventInUse", "The following Quests use the Event");
            en.put("eventEditorMustModifyQuests", "eventEditorNotFound");
            en.put("eventEditorListSizeMismatch", "The lists are not the same size!");
            en.put("eventEditorListDuplicates", "List contains duplicates!");
            en.put("eventEditorNotANumberList", "Input was not a list of numbers!");
            en.put("eventEditorInvalidEntry", "Invalid entry");

            en.put("eventEditorSetName", "Set name");
            en.put("eventEditorSetMessage", "Set message");



            en.put("eventEditorClearInv", "Clear player inventory");
            en.put("eventEditorSetExplosions", "Set explosion locations");
            en.put("eventEditorSetLightning", "Set lightning strike locations");
            en.put("eventEditorSetEffects", "Set effects");
            en.put("eventEditorSetStorm", "Set storm");
            en.put("eventEditorSetThunder", "Set thunder");
            en.put("eventEditorSetMobSpawns", "Set mob spawns");
            en.put("eventEditorSetPotionEffects", "Set potion effects");
            en.put("eventEditorSetHunger", "Set player hunger level");
            en.put("eventEditorSetSaturation", "Set player saturation level");
            en.put("eventEditorSetHealth", "Set player health level");
            en.put("eventEditorSetTeleport", "Set player teleport location");
            en.put("eventEditorSetCommands", "Set commands to execute");

            en.put("eventEditorItems", "Event Items");
                en.put("eventEditorSetItems", "Give items");
                en.put("eventEditorItemsCleared", "Event items cleared.");
                en.put("eventEditorSetItemIDs", "Set item IDs");
                en.put("eventEditorSetItemAmounts", "Set item amounts");
                en.put("eventEditorNoIDs", "No IDs set");
                en.put("eventEditorMustSetIDs", "You must set item IDs first!");
                en.put("eventEditorInvalidID", "___ is not a valid item ID!");
                en.put("eventEditorNotGreaterThanZero", "___ is not greater than 0!");
                en.put("eventEditorNotANumber", "___ is not a number!");

            en.put("eventEditorStorm", "Event Storm");
                en.put("eventEditorSetWorld", "Set world");
                en.put("eventEditorSetDuration", "Set duration");
                en.put("eventEditorNoWorld", "(No world set)");
                en.put("eventEditorSetWorldFirst", "You must set a world first!");
                en.put("eventEditorInvalidWorld", "___ is not a valid world name!");
                en.put("eventEditorMustSetStormDuration", "You must set a storm duration!");
                en.put("eventEditorStormCleared", "Storm data cleared.");
                en.put("eventEditorEnterStormWorld", "Enter a world name for the storm to occur in, or enter \"cancel\" to return");
                en.put("eventEditorEnterDuration", "Enter duration (in milliseconds)");
                en.put("eventEditorAtLeastOneSecond", "Amount must be at least 1 second! (1000 milliseconds)");
                en.put("eventEditorNotGreaterThanOneSecond", "___ is not greater than 1 second! (1000 milliseconds)");

            en.put("eventEditorThunder", "Event Thunder");
                en.put("eventEditorInvalidWorld", "___ is not a valid world name!");
                en.put("eventEditorMustSetThunderDuration", "You must set a thunder duration!");
                en.put("eventEditorThunderCleared", "Thunder data cleared.");
                en.put("eventEditorEnterThunderWorld", "Enter a world name for the thunder to occur in, or enter \"cancel\" to return");

            en.put("eventEditorEffects", "Event Effects");
                en.put("eventEditorAddEffect", "Add effect");
                en.put("eventEditorAddEffectLocation", "Add effect location");
                en.put("eventEditorNoEffects", "No effects set");
                en.put("eventEditorMustAddEffects", "You must add effects first!");
                en.put("eventEditorInvalidEffect", "___ is not a valid effect name!");
                en.put("eventEditorEffectsCleared", "Event effects cleared.");
                en.put("eventEditorEffectLocationPrompt", "Right-click on a block to play an effect at, then enter \"add\" to add it to the list, or enter \"cancel\" to return");

            en.put("eventEditorMobSpawns", "Event Mob Spawns");
                en.put("eventEditorSetMobTypes", "Set mob types");
                en.put("eventEditorNoTypesSet", "(No types set)");
                en.put("eventEditorMustSetMobTypesFirst", "You must set mob types first!");
                en.put("eventEditorSetMobAmounts", "Set mob amounts");
                en.put("eventEditorNoAmountsSet", "(No amounts set)");
                en.put("eventEditorMustSetMobAmountsFirst", "You must set mob amounts first!");
                en.put("eventEditorMustSetMobTypesAndAmountsFirst", "You must set mob types and amounts first!");
                en.put("eventEditorAddSpawnLocation", "Add spawn location");
                en.put("eventEditorMobSpawnsCleared", "Mob spawns cleared.");
                en.put("eventEditorInvalidMob", "___ is not a valid mob name!");

            en.put("eventEditorLightningPrompt", "Right-click on a block to spawn a lightning strike at, then enter \"add\" to add it to the list, or enter \"clear\" to clear the locations list, or \"cancel\" to return");

            en.put("eventEditorPotionEffects", "Event Potion Effects");
                en.put("eventEditorSetPotionEffects", "Set potion effect types");
                en.put("eventEditorMustSetPotionTypesFirst", "You must set potion effect types first!");
                en.put("eventEditorSetPotionDurations", "Set potion effect durations");
                en.put("eventEditorMustSetPotionDurationsFirst", "You must set potion effect durations first!");
                en.put("eventEditorMustSetPotionTypesAndDurationsFirst", "You must set potion effect types and durations first!");
                en.put("eventEditorNoDurationsSet", "(No durations set)");
                en.put("eventEditorSetPotionMagnitudes", "Set potion effect magnitudes");
                en.put("eventEditorPotionsCleared", "Potion effects cleared.");
                en.put("eventEditorInvalidPotionType", "___ is not a valid potion effect type!");

            en.put("eventEditorEnterNPCId", "Enter NPC ID (or -1 to return)");
            en.put("eventEditorNoNPCExists", "No NPC exists with that id!");
            en.put("eventEditorExplosionPrompt", "Right-click on a block to spawn an explosion at, then enter \"add\" to add it to the list, or enter \"clear\" to clear the explosions list, or \"cancel\" to return");
            en.put("eventEditorSelectBlockFirst", "You must select a block first.");
            en.put("eventEditorSetMessagePrompt", "Enter message, or enter \'none\' to delete, (or \'cancel\' to return)");
            en.put("eventEditorSetItemIDsPrompt", "Enter item IDs separating each one by a space, or enter \"cancel\" to return.");
            en.put("eventEditorSetItemAmountsPrompt", "Enter item amounts (numbers) separating each one by a space, or enter \"cancel\" to return.");
            en.put("eventEditorSetMobTypesPrompt", "Enter mob names separating each one by a space, or enter \"cancel\" to return");
            en.put("eventEditorSetMobAmountsPrompt", "Enter mob amounts separating each one by a space, or enter \"cancel\" to return");
            en.put("eventEditorMobLocationPrompt", "Right-click on a block to select it, then enter \"add\" to add it to the mob spawn location list, or enter \"cancel\" to return");
            en.put("eventEditorSetPotionEffectsPrompt", "Enter potion effect types separating each one by a space, or enter \"cancel\" to return");
            en.put("eventEditorSetPotionDurationsPrompt", "Enter effect durations (in milliseconds) separating each one by a space, or enter \"cancel\" to return");
            en.put("eventEditorSetPotionMagnitudesPrompt", "Enter potion effect magnitudes separating each one by a space, or enter \"cancel\" to return");
            en.put("eventEditorSetHungerPrompt", "Enter hunger level, or -1 to remove it");
                en.put("eventEditorHungerLevelAtLeastZero", "Hunger level must be at least 0!");
            en.put("eventEditorSetSaturationPrompt", "Enter saturation level, or -1 to remove it");
                en.put("eventEditorSaturationLevelAtLeastZero", "Saturation level must be at least 0!");
            en.put("eventEditorSetHealthPrompt", "Enter health level, or -1 to remove it");
                en.put("eventEditorHealthLevelAtLeastZero", "Health level must be at least 0!");
            en.put("eventEditorSetTeleportPrompt", "Right-click on a block to teleport the player to, then enter \"done\" to finish,\nor enter \"clear\" to clear the teleport location, or \"cancel\" to return");
            en.put("eventEditorCommandsNote", "Note: You may use <player> to refer to the player's name.");
            en.put("eventEditorSetCommandsPrompt", "Enter commands separating each one by a comma, or enter \"clear\" to clear the list, or enter \"cancel\" to return.");
            en.put("eventEditorSet", "");
            en.put("eventEditorSet", "");
            en.put("eventEditorSet", "");


            //

            //Effects
            en.put("effBlazeShoot", "Sound of a Blaze firing");
            en.put("effBowFire", "Sound of a bow firing");
            en.put("effClick1", "A click sound");
            en.put("effClick2", "A different click sound");
            en.put("effDoorToggle", "Sound of a door opening or closing");
            en.put("effExtinguish", "Sound of fire being extinguished");
            en.put("effGhastShoot", "Sound of a Ghast firing");
            en.put("effGhastShriek", "Sound of a Ghast shrieking");
            en.put("effZombieWood", "Sound of a Zombie chewing an iron door");
            en.put("effZombieIron", "Sound of a Zombie chewing a wooden door");
            en.put("effEnterName", "Enter an effect name to add it to the list, or enter \"cancel\" to return");

            //

            //Inputs
            en.put("cmdCancel", "cancel");
            en.put("cmdAdd", "add");
            en.put("cmdClear", "clear");
            en.put("cmdNone", "none");
            en.put("cmdDone", "done");
            //

            //Misc
            en.put("event", "Event");
            en.put("exit", "Exit");
            en.put("exited", "Exited.");
            en.put("yes", "Yes");
            en.put("no", "No");
            en.put("done", "Done");
            en.put("quit", "Quit");
            en.put("clear", "Clear");
            en.put("noneSet", "None set");
            en.put("worlds", "Worlds");
            en.put("mobs", "Mobs");
            en.put("invalidOption", "Invalid option!");
            //
        //

    }


}
