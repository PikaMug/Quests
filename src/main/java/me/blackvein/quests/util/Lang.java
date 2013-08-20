package me.blackvein.quests.util;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import me.blackvein.quests.Quests;

import org.bukkit.configuration.file.YamlConfiguration;

public class Lang {

	public static String lang = "en";

	private static Map<String, String> en = new LinkedHashMap<String, String>();

	private Quests plugin;

	public Lang (Quests plugin) {
		this.plugin = plugin;
	}

	public static String get(String key){
		return en.get(key);
	}

	public void initPhrases(){

		//English
		//TODO: If finished, completely check everything.
		//Quests
		//Quest create menu
		en.put("questEditorHeader", "Create Quest");
		en.put("questEditorCreate", "Create new Quest");
		en.put("questEditorEdit", "Edit a Quest");
		en.put("questEditorDelete", "Delete Quest");
		en.put("questEditorName", "Set name");

		en.put("questEditorAskMessage", "Set ask message");
		en.put("questEditorFinishMessage", "Set finish message");
		en.put("questEditorRedoDelay", "Set redo delay");
		en.put("questEditorNPCStart", "Set NPC start");
		en.put("questEditorBlockStart", "Set Block start");
		en.put("questEditorInitialEvent", "Set initial Event");
		en.put("questEditorReqs", "Edit Requirements");
		en.put("questEditorStages", "Edit Stages");
		en.put("questEditorRews", "Edit Rewards");

		en.put("questEditorEnterQuestName", "Enter Quest name (or \"cancel\" to return)");
		en.put("questEditorEnterAskMessage", "Enter ask message (or \"cancel\" to return)");
		en.put("questEditorEnterFinishMessage", "Enter finish message (or \"cancel\" to return)");
		en.put("questEditorEnterRedoDelay", "Enter amount of time (in milliseconds), 0 to clear the redo delay or -1 to cancel ");
		en.put("questEditorEnterNPCStart", "Enter NPC ID, -1 to clear the NPC start or -2 to cancel");
		en.put("questEditorEnterBlockStart", "Right-click on a block to use as a start point, then enter \"done\" to save,\n"
				+ "or enter \"clear\" to clear the block start, or \"cancel\" to return");
		en.put("questEditorEnterInitialEvent", "Enter an Event name, or enter \"clear\" to clear the initial Event, or \"cancel\" to return");

		//Quest create menu errors
		en.put("questEditorNameExists", "A Quest with that name already exists!");
		en.put("questEditorBeingEdited", "Someone is creating/editing a Quest with that name!");
		en.put("questEditorInvalidQuestName", "Name may not contain commas!");
		en.put("questEditorInvalidEventName", "is not a valid event name!");
		en.put("questEditorInvalidNPC", "No NPC exists with that id!");
		en.put("questEditorNoStartBlockSelected", "You must select a block first.");
		en.put("questEditorPositiveAmount", "Amount must be a positive number.");
		en.put("questEditorQuestAsRequirement1", "The following Quests have");
		en.put("questEditorQuestAsRequirement2", "as a requirement:");
		en.put("questEditorQuestAsRequirement3", "You must modify these Quests so that they do not use it before deleting it.");
		en.put("questEditorQuestNotFound", "Quest not found!");

		en.put("questEditorEventCleared", "Initial Event cleared.");
		en.put("questEditorSave", "Finish and save");

		en.put("questEditorNeedAskMessage", "You must set an ask message!");
		en.put("questEditorNeedFinishMessage", "You must set a finish message!");
		en.put("questEditorNeedStages", "Your Quest has no Stages!");
		en.put("questEditorSaved", "Quest saved! (You will need to perform a Quest reload for it to appear)");
		en.put("questEditorExited", "Are you sure you want to exit without saving?");
		en.put("questEditorDeleted", "Are you sure you want to delete the Quest");

		en.put("questEditorNoPermsCreate", "You do not have permission to create Quests.");
		en.put("questEditorNoPermsEdit", "You do not have permission to edit Quests.");
		en.put("questEditorNoPermsDelete", "You do not have permission to delete Quests.");
		//

		//Stages
		//Menu
		en.put("stageEditorEditStage", "Edit Stage");
		en.put("stageEditorNewStage", "Add new Stage");
		//create prompt
		en.put("stageEditorStages", "Stages");
		en.put("stageEditorStage", "Stage");
		en.put("stageEditorEditStage", "Edit Stage");
		en.put("stageEditorNewStage", "Add new Stage");
		en.put("stageEditorBreakBlocks", "Break Blocks");
		en.put("stageEditorDamageBlocks", "Damage Blocks");
		en.put("stageEditorPlaceBlocks", "Place Blocks");
		en.put("stageEditorUseBlocks", "Use Blocks");
		en.put("stageEditorCutBlocks", "Cut Blocks");
		en.put("stageEditorCatchFish", "Catch Fish");
		en.put("stageEditorFish", "fish");
		en.put("stageEditorKillPlayers", "Kill Players");
		en.put("stageEditorPlayers", "players");
		en.put("stageEditorEnchantItems", "Enchant Items");
		en.put("stageEditorDeliverItems", "Deliver Items");
		en.put("stageEditorTalkToNPCs", "Talk to NPCs");
		en.put("stageEditorKillNPCs", "Kill NPCs");
		en.put("stageEditorKillBosses", "Kill Bosses");
		en.put("stageEditorKillMobs", "Kill Mobs");
		en.put("stageEditorReachLocs", "");
		en.put("stageEditorReachRadii1", "Reach within");
		en.put("stageEditorReachRadii2", "blocks of");
		en.put("stageEditorTameMobs", "Tame Mobs");
		en.put("stageEditorShearSheep", "Shear Sheep");
		en.put("stageEditorDelayMessage", "Delay Message");
		en.put("stageEditorDenizenScript", "Denizen Script");
		en.put("stageEditorStartMessage", "Start Message");
		en.put("stageEditorCompleteMessage", "Complete Message");
		en.put("stageEditorDelete", "Delete Stage");

		en.put("stageEditorSetBlockIds", "Set block IDs");
		en.put("stageEditorSetBlockAmounts", "Set block amounts");
		en.put("stageEditorNoBlockIds", "You must set Block IDs first!");
		en.put("stageEditorBreakBlocksCleared", "Break blocks objective cleared.");
		en.put("stageEditorInvalidIdAmountList", "The block IDs list block amounts list are not the same size!");
		en.put("stageEditorEnterBlockIds", "Enter block IDs, separating each one by a space, or enter \'cancel\' to return.");
		en.put("stageEditorContainsDuplicates", "List contains duplicates!");
		en.put("stageEditorInvalidBlockId", "is not a valid block ID!");
		en.put("stageEditorInvalidEntryInt", "Invalid entry, input was not a list of numbers!");

		//prompts



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
		en.put("eventEditorAddMobTypes", "Add mob");
		en.put("eventEditorNoTypesSet", "(No type set)");
		en.put("eventEditorMustSetMobTypesFirst", "You must set the mob type first!");
		en.put("eventEditorSetMobAmounts", "Set mob amount");
		en.put("eventEditorNoAmountsSet", "(No amounts set)");
		en.put("eventEditorMustSetMobAmountsFirst", "You must set mob amount first!");
		en.put("eventEditorAddSpawnLocation", "Set spawn location");
		en.put("eventEditorMobSpawnsCleared", "Mob spawns cleared.");
		en.put("eventEditorMustSetMobLocationFirst", "You must set a spawn-location first!");
		en.put("eventEditorInvalidMob", "___ is not a valid mob name!");
		en.put("eventEditorSetMobName", "Set custom name for mob");
		en.put("eventEditorSetMobType", "Set mob type");
		en.put("eventEditorSetMobItemInHand", "Set item in hand");
		en.put("eventEditorSetMobItemInHandDrop", "Set drop chance of item in hand");
		en.put("eventEditorSetMobBoots", "Set boots");
		en.put("eventEditorSetMobBootsDrop", "Set drop chance of boots");
		en.put("eventEditorSetMobLeggings", "Set leggings");
		en.put("eventEditorSetMobLeggingsDrop", "Set drop chance of leggings");
		en.put("eventEditorSetMobChestPlate", "Set chest plate");
		en.put("eventEditorSetMobChestPlateDrop", "Set drop chance of chest plate");
		en.put("eventEditorSetMobHelmet", "Set helmet");
		en.put("eventEditorSetMobHelmetDrop", "Set drop chance of helmet");
		en.put("eventEditorSetMobSpawnLoc", "Right-click on a block to spawn a mob at, then enter \"add\" to the confirm it, or enter \"cancel\" to return");
		en.put("eventEditorSetMobSpawnAmount", "Set the amount of mobs to spawn");
		en.put("eventEditorSetDropChance", "Set the drop chance");
		en.put("eventEditorInvalidDropChance", "Drop chance has to be between 0.0 and 1.0");

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
		en.put("eventEditorSetMobTypesPrompt", "Enter mob name, or enter \"cancel\" to return");
		en.put("eventEditorSetMobAmountsPrompt", "Enter mob amount, or enter \"cancel\" to return");
		en.put("eventEditorSetMobNamePrompt", "Set the name for this mob, or enter \"cancel\" to return");
		en.put("eventEditorSetMobLocationPrompt", "Right-click on a block to select it, then enter \"add\" to add it to the mob spawn location list, or enter \"cancel\" to return");
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
		//en.put("eventEditorSet", "");
		//en.put("eventEditorSet", "");


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
		en.put("delay", "Delay");
		en.put("save", "Save");
		en.put("exit", "Exit");
		en.put("exited", "Exited");
		en.put("cancel", "Cancel");
		en.put("yes", "Yes");
		en.put("no", "No");
		en.put("clear", "Clear");
		en.put("done", "Done");
		en.put("quit", "Quit");
		en.put("noneSet", "None set");
		en.put("noDelaySet", "No delay set");
		en.put("noIdsSet", "No IDs set");
		en.put("worlds", "Worlds");
		en.put("mobs", "Mobs");
		en.put("invalidOption", "Invalid option!");
		//
		//

		File file = new File(plugin.getDataFolder(), "/lang/" + lang + ".yml");
		YamlConfiguration langFile = YamlConfiguration.loadConfiguration(file);

		for (Entry<String, Object> e : langFile.getValues(true).entrySet()) {
			en.put(e.getKey(), (String) e.getValue());
		}

	}

	public void save() {
		File file = new File(plugin.getDataFolder(), "/lang/" + lang + ".yml");
		YamlConfiguration langFile = YamlConfiguration.loadConfiguration(file);

		for (Entry<String, String> e : en.entrySet()) {
			langFile.set(e.getKey(), e.getValue());
		}

		try {
			langFile.save(file);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
