package me.blackvein.quests.util;

public class CK {

    public static final String ED_QUEST_EDIT = "edit";
    public static final String ED_QUEST_DELETE = "delQuest";
    public static final String ED_EVENT_DELETE = "delEvent";

    //Quests
    public static final String Q_NAME = "questName";
    public static final String Q_ASK_MESSAGE = "askMessage";
    public static final String Q_FINISH_MESSAGE = "finishMessage";
    public static final String Q_REDO_DELAY = "redoDelay";
    public static final String Q_START_NPC = "npcStart";
    public static final String Q_START_BLOCK = "blockStart";
    public static final String Q_FAIL_MESSAGE = "failMessage";
    public static final String Q_INITIAL_EVENT = "initialEvent";
    public static final String Q_REGION = "region";
    public static final String Q_GUIDISPLAY = "guiDisplay";

    //Requirements
    public static final String REQ_MONEY = "moneyReq";
    public static final String REQ_QUEST_POINTS = "questPointsReq";
    public static final String REQ_ITEMS = "itemReqs";
    public static final String REQ_ITEMS_REMOVE = "removeItemReqs";
    public static final String REQ_PERMISSION = "permissionReqs";
    public static final String REQ_MCMMO_SKILLS = "mcMMOSkillReqs";
    public static final String REQ_MCMMO_SKILL_AMOUNTS = "mcMMOSkillAmountReqs";
    public static final String REQ_HEROES_PRIMARY_CLASS = "heroesPrimaryClassReq";
    public static final String REQ_HEROES_SECONDARY_CLASS = "heroesSecondaryClassReq";
    public static final String REQ_QUEST = "questReqs";
    public static final String REQ_QUEST_BLOCK = "questBlocks";
    public static final String REQ_CUSTOM = "customReqs";
    public static final String REQ_CUSTOM_DATA = "customReqData";
    public static final String REQ_CUSTOM_DATA_DESCRIPTIONS = "customReqDataDesc";
    public static final String REQ_CUSTOM_DATA_TEMP = "customReqDataTemp";

    //Rewards
    public static final String REW_MONEY = "moneyRew";
    public static final String REW_QUEST_POINTS = "questPointsRew";
    public static final String REW_ITEMS = "itemRews";
    public static final String REW_EXP = "expRew";
    public static final String REW_COMMAND = "commandRews";
    public static final String REW_PERMISSION = "permissionRews";
    public static final String REW_MCMMO_SKILLS = "mcMMOSkillRews";
    public static final String REW_MCMMO_AMOUNTS = "mcMMOSkillAmounts";
    public static final String REW_HEROES_CLASSES = "heroesClassRews";
    public static final String REW_HEROES_AMOUNTS = "heroesAmountRews";
    public static final String REW_PHAT_LOOTS = "phatLootRews";
    public static final String REW_CUSTOM = "customRews";
    public static final String REW_CUSTOM_DATA = "customRewData";
    public static final String REW_CUSTOM_DATA_DESCRIPTIONS = "customRewDataDesc";
    public static final String REW_CUSTOM_DATA_TEMP = "customRewDataTemp";
    //Stages
    public static final String S_BREAK_NAMES = "breakNames";
    public static final String S_BREAK_AMOUNTS = "breakAmounts";
    public static final String S_BREAK_DURABILITY = "breakDurability";
    public static final String S_DAMAGE_NAMES = "damageNames";
    public static final String S_DAMAGE_AMOUNTS = "damageAmounts";
    public static final String S_DAMAGE_DURABILITY = "damageDurability";
    public static final String S_PLACE_NAMES = "placeNames";
    public static final String S_PLACE_AMOUNTS = "placeAmounts";
    public static final String S_PLACE_DURABILITY = "placeDurability";
    public static final String S_USE_NAMES = "useNames";
    public static final String S_USE_AMOUNTS = "useAmounts";
    public static final String S_USE_DURABILITY = "useDurability";
    public static final String S_CUT_NAMES = "cutNames";
    public static final String S_CUT_AMOUNTS = "cutAmounts";
    public static final String S_CUT_DURABILITY = "cutDurability";
    public static final String S_FISH = "fish";
    public static final String S_PLAYER_KILL = "playerKill";
    public static final String S_ENCHANT_TYPES = "enchantTypes";
    public static final String S_ENCHANT_NAMES = "enchantNames";
    public static final String S_ENCHANT_AMOUNTS = "enchantAmounts";
    public static final String S_DELIVERY_ITEMS = "deliveryItems";
    public static final String S_DELIVERY_NPCS = "deliveryNPCs";
    public static final String S_DELIVERY_MESSAGES = "deliveryMessages";
    public static final String S_NPCS_TO_TALK_TO = "npcIdsToTalkTo";
    public static final String S_NPCS_TO_KILL = "npcIdsToKill";
    public static final String S_NPCS_TO_KILL_AMOUNTS = "npcAmountsToKill";
    public static final String S_MOB_TYPES = "mobTypes";
    public static final String S_MOB_AMOUNTS = "mobAmounts";
    public static final String S_MOB_KILL_LOCATIONS = "killLocations";
    public static final String S_MOB_KILL_LOCATIONS_RADIUS = "killLocationRadii";
    public static final String S_MOB_KILL_LOCATIONS_NAMES = "killLocationNames";
    public static final String S_REACH_LOCATIONS = "reachLocations";
    public static final String S_REACH_LOCATIONS_RADIUS = "reachLocationRadii";
    public static final String S_REACH_LOCATIONS_NAMES = "reachLocationNames";
    public static final String S_TAME_TYPES = "tameTypes";
    public static final String S_TAME_AMOUNTS = "tameAmounts";
    public static final String S_SHEAR_COLORS = "shearColors";
    public static final String S_SHEAR_AMOUNTS = "shearAmounts";
    public static final String S_START_EVENT = "startEvent";
    public static final String S_FINISH_EVENT = "finishEvent";
    public static final String S_CHAT_EVENTS = "chatEvents";
    public static final String S_CHAT_EVENT_TRIGGERS = "chatEventTriggers";
    public static final String S_CHAT_TEMP_EVENT = "chatTempEvent";
    public static final String S_DEATH_EVENT = "deathEvent";
    public static final String S_DISCONNECT_EVENT = "disconnectEvent";
    public static final String S_DELAY = "delay";
    public static final String S_DELAY_MESSAGE = "delayMessage";
    public static final String S_DENIZEN = "denizen";
    public static final String S_COMPLETE_MESSAGE = "completeMessage";
    public static final String S_START_MESSAGE = "startMessage";
    public static final String S_OVERRIDE_DISPLAY = "overrideDisplay";
    public static final String S_PASSWORD_DISPLAYS = "passwordDisplays";
    public static final String S_PASSWORD_PHRASES = "passwordPhrases";
    public static final String S_CUSTOM_OBJECTIVES = "customObjectives";
    public static final String S_CUSTOM_OBJECTIVES_COUNT = "customObjectiveCounts";
    public static final String S_CUSTOM_OBJECTIVES_DATA = "customObjectiveData";
    public static final String S_CUSTOM_OBJECTIVES_DATA_DESCRIPTIONS = "customObjectiveDataDescriptions";
    public static final String S_CUSTOM_OBJECTIVES_DATA_TEMP = "customObjectiveDataTemp";

    //Events
    public static final String E_OLD_EVENT = "oldEvent";
    public static final String E_NAME = "evtName";
    public static final String E_MESSAGE = "evtMessage";
    public static final String E_CLEAR_INVENTORY = "evtClearInv";
    public static final String E_FAIL_QUEST = "evtFailQuest";
    public static final String E_ITEMS = "evtItems";
    public static final String E_ITEMS_AMOUNTS = "evtItemAmounts";
    public static final String E_EXPLOSIONS = "evtExplosions";
    public static final String E_EFFECTS = "evtEffects";
    public static final String E_EFFECTS_LOCATIONS = "evtEffectLocations";
    public static final String E_WORLD_STORM = "evtStormWorld";
    public static final String E_WORLD_STORM_DURATION = "evtStormDuration";
    public static final String E_WORLD_THUNDER = "evtThunderWorld";
    public static final String E_WORLD_THUNDER_DURATION = "evtThunderDuration";
    public static final String E_MOB_TYPES = "evtMobTypes";
    public static final String E_LIGHTNING = "evtLightningStrikes";
    public static final String E_POTION_TYPES = "evtPotionTypes";
    public static final String E_POTION_DURATIONS = "evtPotionDurations";
    public static final String E_POTION_STRENGHT = "evtPotionMagnitudes";
    public static final String E_HUNGER = "evtHunger";
    public static final String E_SATURATION = "evtSaturation";
    public static final String E_HEALTH = "evtHealth";
    public static final String E_TELEPORT = "evtTeleportLocation";
    public static final String E_COMMANDS = "evtCommands";

    //Party
    public static final String P_INVITER = "inviter";

}