/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.util;

/**
 * Human-readable keys for session data of prompts and factories
 */
public class Key {

    public static final String ED_QUEST_EDIT = "edit";
    public static final String ED_QUEST_DELETE = "delQuest";
    public static final String ED_EVENT_DELETE = "delEvent";
    public static final String ED_CONDITION_DELETE = "delCondition";
    // Quests
    public static final String Q_ID = "questId";
    public static final String Q_NAME = "questName";
    public static final String Q_ASK_MESSAGE = "askMessage";
    public static final String Q_FINISH_MESSAGE = "finishMessage";
    public static final String Q_START_NPC = "npcStart";
    public static final String Q_START_BLOCK = "blockStart";
    public static final String Q_INITIAL_EVENT = "initialEvent";
    public static final String Q_REGION = "region";
    public static final String Q_GUIDISPLAY = "guiDisplay";
    // Requirements
    public static final String REQ_MONEY = "moneyReq";
    public static final String REQ_QUEST_POINTS = "questPointsReq";
    public static final String REQ_ITEMS = "itemReqs";
    public static final String REQ_ITEMS_REMOVE = "removeItemReqs";
    public static final String REQ_EXP = "expReq";
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
    public static final String REQ_FAIL_MESSAGE = "failMessage";
    // Stages
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
    public static final String S_COW_MILK = "cowMilk";
    public static final String S_FISH = "fish";
    public static final String S_PLAYER_KILL = "playerKill";
    public static final String S_CRAFT_ITEMS = "craftItems";
    public static final String S_SMELT_ITEMS = "smeltItems";
    public static final String S_CONSUME_ITEMS = "consumeItems";
    public static final String S_ENCHANT_ITEMS = "enchantItems";
    public static final String S_BREW_ITEMS = "brewItems";
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
    public static final String S_FAIL_EVENT = "failEvent";
    public static final String S_CHAT_EVENTS = "chatEvents";
    public static final String S_CHAT_EVENT_TRIGGERS = "chatEventTriggers";
    public static final String S_CHAT_TEMP_EVENT = "chatTempEvent";
    public static final String S_COMMAND_EVENTS = "commandEvents";
    public static final String S_COMMAND_EVENT_TRIGGERS = "commandEventTriggers";
    public static final String S_COMMAND_TEMP_EVENT = "commandTempEvent";
    public static final String S_DEATH_EVENT = "deathEvent";
    public static final String S_DISCONNECT_EVENT = "disconnectEvent";
    public static final String S_CONDITION = "condition";
    public static final String S_DELAY = "delay";
    public static final String S_DELAY_MESSAGE = "delayMessage";
    public static final String S_DENIZEN = "denizen"; // Legacy
    public static final String S_COMPLETE_MESSAGE = "completeMessage";
    public static final String S_START_MESSAGE = "startMessage";
    public static final String S_PASSWORD_DISPLAYS = "passwordDisplays";
    public static final String S_PASSWORD_PHRASES = "passwordPhrases";
    public static final String S_CUSTOM_OBJECTIVES = "customObjectives";
    public static final String S_CUSTOM_OBJECTIVES_COUNT = "customObjectiveCounts";
    public static final String S_CUSTOM_OBJECTIVES_DATA = "customObjectiveData";
    public static final String S_CUSTOM_OBJECTIVES_DATA_DESCRIPTIONS = "customObjectiveDataDescriptions";
    public static final String S_CUSTOM_OBJECTIVES_DATA_TEMP = "customObjectiveDataTemp";
    public static final String S_OVERRIDE_DISPLAY = "overrideDisplay";
    // Rewards
    public static final String REW_MONEY = "moneyRew";
    public static final String REW_QUEST_POINTS = "questPointsRew";
    public static final String REW_ITEMS = "itemRews";
    public static final String REW_EXP = "expRew";
    public static final String REW_COMMAND = "commandRews";
    public static final String REW_COMMAND_OVERRIDE_DISPLAY = "commandOverrideDisplay";
    public static final String REW_PERMISSION = "permissionRews";
    public static final String REW_PERMISSION_WORLDS = "permissionWorlds";
    public static final String REW_MCMMO_SKILLS = "mcMMOSkillRews";
    public static final String REW_MCMMO_AMOUNTS = "mcMMOSkillAmounts";
    public static final String REW_HEROES_CLASSES = "heroesClassRews";
    public static final String REW_HEROES_AMOUNTS = "heroesAmountRews";
    public static final String REW_PARTIES_EXPERIENCE = "partiesExperienceRews";
    public static final String REW_PHAT_LOOTS = "phatLootRews";
    public static final String REW_CUSTOM = "customRews";
    public static final String REW_CUSTOM_DATA = "customRewData";
    public static final String REW_CUSTOM_DATA_DESCRIPTIONS = "customRewDataDesc";
    public static final String REW_CUSTOM_DATA_TEMP = "customRewDataTemp";
    public static final String REW_DETAILS_OVERRIDE = "detailsOverrideRew";
    // Planner
    public static final String PLN_START_DATE = "startDatePln";
    public static final String PLN_END_DATE = "endDatePln";
    public static final String PLN_REPEAT_CYCLE = "repeatCyclePln";
    public static final String PLN_COOLDOWN = "cooldownPln";
    public static final String PLN_OVERRIDE = "overridePln";
    // Options
    public static final String OPT_ALLOW_COMMANDS = "allowCommandsOpt";
    public static final String OPT_ALLOW_QUITTING = "allowQuittingOpt";
    public static final String OPT_IGNORE_SILK_TOUCH = "ignoreSilkTouchOpt";
    public static final String OPT_EXTERNAL_PARTY_PLUGIN = "externalPartyPluginOpt";
    public static final String OPT_USE_PARTIES_PLUGIN = "usePartiesPluginOpt";
    public static final String OPT_SHARE_PROGRESS_LEVEL = "shareProgressLevelOpt";
    public static final String OPT_SHARE_SAME_QUEST_ONLY = "shareSameQuestOnlyOpt";
    public static final String OPT_SHARE_DISTANCE = "shareDistance";
    public static final String OPT_HANDLE_OFFLINE_PLAYERS = "handleOfflinePlayers";
    public static final String OPT_IGNORE_BLOCK_REPLACE = "ignoreBlockReplace";
    // Actions
    public static final String A_OLD_ACTION = "oldAction";
    public static final String A_NAME = "actName";
    public static final String A_MESSAGE = "actMessage";
    public static final String A_CLEAR_INVENTORY = "actClearInv";
    public static final String A_FAIL_QUEST = "actFailQuest";
    public static final String A_ITEMS = "actItems";
    public static final String A_ITEMS_AMOUNTS = "actItemAmounts";
    public static final String A_EXPLOSIONS = "actExplosions";
    public static final String A_EFFECTS = "actEffects";
    public static final String A_EFFECTS_LOCATIONS = "actEffectLocations";
    public static final String A_WORLD_STORM = "actStormWorld";
    public static final String A_WORLD_STORM_DURATION = "actStormDuration";
    public static final String A_WORLD_THUNDER = "actThunderWorld";
    public static final String A_WORLD_THUNDER_DURATION = "actThunderDuration";
    public static final String A_MOBS = "actMobs";
    public static final String A_LIGHTNING = "actLightningStrikes";
    public static final String A_POTION_TYPES = "actPotionTypes";
    public static final String A_POTION_DURATIONS = "actPotionDurations";
    public static final String A_POTION_STRENGTH = "actPotionMagnitudes";
    public static final String A_HUNGER = "actHunger";
    public static final String A_SATURATION = "actSaturation";
    public static final String A_HEALTH = "actHealth";
    public static final String A_TELEPORT = "actTeleportLocation";
    public static final String A_COMMANDS = "actCommands";
    public static final String A_TIMER = "actTimer";
    public static final String A_CANCEL_TIMER = "actCancelTimer";
    public static final String A_DENIZEN = "actDenizen";
    // Conditions
    public static final String C_OLD_CONDITION = "oldCondition";
    public static final String C_NAME = "conName";
    public static final String C_FAIL_QUEST = "conFailQuest";
    public static final String C_WHILE_RIDING_ENTITY = "conRidingEntity";
    public static final String C_WHILE_RIDING_NPC = "conRidingNpc";
    public static final String C_WHILE_PERMISSION = "conPermission";
    public static final String C_WHILE_HOLDING_MAIN_HAND = "conHoldingMainHand";
    public static final String C_WHILE_WEARING = "conWearing";
    public static final String C_WHILE_WITHIN_WORLD = "conWithinWorld";
    public static final String C_WHILE_WITHIN_TICKS_START = "conWithinTicksStart";
    public static final String C_WHILE_WITHIN_TICKS_END = "conWithinTicksEnd";
    public static final String C_WHILE_WITHIN_BIOME = "conWithinBiome";
    public static final String C_WHILE_WITHIN_REGION = "conWithinRegion";
    public static final String C_WHILE_PLACEHOLDER_ID = "conPlaceholderId";
    public static final String C_WHILE_PLACEHOLDER_VAL = "conPlaceholderVal";
}
