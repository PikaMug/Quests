/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.quests;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.convo.quests.main.FabricQuestMainPrompt;
import me.pikamug.quests.module.CustomObjective;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.components.FabricStage;
import me.pikamug.quests.quests.components.Options;
import me.pikamug.quests.quests.components.Planner;
import me.pikamug.quests.quests.components.Requirements;
import me.pikamug.quests.quests.components.Rewards;
import me.pikamug.quests.quests.components.Stage;
import me.pikamug.quests.util.FabricItemUtil;
import me.pikamug.quests.util.FabricLocation;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.SessionData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FabricQuestFactory implements QuestFactory {

    private static final Gson GSON = new Gson();
    private final FabricQuestsPlugin plugin;
    private ConcurrentHashMap<UUID, Block> selectedBlockStarts = new ConcurrentHashMap<>();
    private ConcurrentHashMap<UUID, Block> selectedKillLocations = new ConcurrentHashMap<>();
    private Set<UUID> selectingNpcs = ConcurrentHashMap.newKeySet();
    private List<String> namesOfQuestsBeingEdited = new LinkedList<>();

    public FabricQuestFactory(final FabricQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    public ConcurrentHashMap<UUID, Block> getSelectedBlockStarts() {
        return selectedBlockStarts;
    }

    public void setSelectedBlockStarts(final ConcurrentHashMap<UUID, Block> selectedBlockStarts) {
        this.selectedBlockStarts = selectedBlockStarts;
    }

    public ConcurrentHashMap<UUID, Block> getSelectedKillLocations() {
        return selectedKillLocations;
    }

    public void setSelectedKillLocations(final ConcurrentHashMap<UUID, Block> selectedKillLocations) {
        this.selectedKillLocations = selectedKillLocations;
    }

    public Set<UUID> getSelectingNpcs() {
        return selectingNpcs;
    }

    public void setSelectingNpcs(final Collection<UUID> selectingNpcs) {
        final Set<UUID> newSelectingNpcs = ConcurrentHashMap.newKeySet();
        newSelectingNpcs.addAll(selectingNpcs);
        this.selectingNpcs = newSelectingNpcs;
    }

    public List<String> getNamesOfQuestsBeingEdited() {
        return namesOfQuestsBeingEdited;
    }

    public void setNamesOfQuestsBeingEdited(final Collection<String> questNames) {
        this.namesOfQuestsBeingEdited = new LinkedList<>(questNames);
    }

    public void returnToMenu(final UUID uuid) {
        new FabricQuestMainPrompt(uuid).start();
    }

    public void loadQuest(final UUID uuid, final Quest quest) {
        if (!(quest instanceof FabricQuest)) {
            return;
        }
        final FabricQuest fabricQuest = (FabricQuest) quest;
        try {
            SessionData.set(uuid, Key.ED_QUEST_EDIT, fabricQuest.getName());
            SessionData.set(uuid, Key.Q_ID, fabricQuest.getId());
            SessionData.set(uuid, Key.Q_NAME, fabricQuest.getName());
            SessionData.set(uuid, Key.Q_ASK_MESSAGE, fabricQuest.getDescription());
            SessionData.set(uuid, Key.Q_FINISH_MESSAGE, fabricQuest.getFinished());
            if (plugin.getDependencies().hasAnyNpcDependencies()) {
                if (fabricQuest.getNpcStart() != null) {
                    SessionData.set(uuid, Key.Q_START_NPC, fabricQuest.getNpcStart().toString());
                }
            }
            SessionData.set(uuid, Key.Q_START_BLOCK, fabricQuest.getBlockStart());
            if (fabricQuest.getInitialAction() != null) {
                SessionData.set(uuid, Key.Q_INITIAL_EVENT, fabricQuest.getInitialAction().getName());
            }
            if (fabricQuest.getRegionStart() != null) {
                SessionData.set(uuid, Key.Q_REGION, fabricQuest.getRegionStart());
            }
            if (fabricQuest.getGUIDisplay() != null) {
                SessionData.set(uuid, Key.Q_GUIDISPLAY, fabricQuest.getGUIDisplay());
            }
            final Requirements requirements = fabricQuest.getRequirements();
            if (requirements.getMoney() != 0) {
                SessionData.set(uuid, Key.REQ_MONEY, requirements.getMoney());
            }
            if (requirements.getQuestPoints() != 0) {
                SessionData.set(uuid, Key.REQ_QUEST_POINTS, requirements.getQuestPoints());
            }
            if (requirements.getExp() != 0) {
                SessionData.set(uuid, Key.REQ_EXP, requirements.getExp());
            }
            if (!requirements.getItems().isEmpty()) {
                SessionData.set(uuid, Key.REQ_ITEMS, requirements.getItems());
                SessionData.set(uuid, Key.REQ_ITEMS_REMOVE, requirements.getRemoveItems());
            }
            if (!requirements.getNeededQuestIds().isEmpty()) {
                SessionData.set(uuid, Key.REQ_QUEST, requirements.getNeededQuestIds());
            }
            if (!requirements.getBlockQuestIds().isEmpty()) {
                SessionData.set(uuid, Key.REQ_QUEST_BLOCK, requirements.getBlockQuestIds());
            }
            if (!requirements.getPermissions().isEmpty()) {
                SessionData.set(uuid, Key.REQ_PERMISSION, requirements.getPermissions());
            }
            if (requirements.getHeroesPrimaryClass() != null) {
                SessionData.set(uuid, Key.REQ_HEROES_PRIMARY_CLASS, requirements.getHeroesPrimaryClass());
            }
            if (requirements.getHeroesSecondaryClass() != null) {
                SessionData.set(uuid, Key.REQ_HEROES_SECONDARY_CLASS, requirements.getHeroesSecondaryClass());
            }
            if (!requirements.getCustomRequirements().isEmpty()) {
                final LinkedList<String> list = new LinkedList<>();
                final LinkedList<Object> dataMapList = new LinkedList<>();
                for (final Entry<String, java.util.Map<String, Object>> entry
                        : requirements.getCustomRequirements().entrySet()) {
                    list.add(entry.getKey());
                    dataMapList.add(entry.getValue());
                }
                SessionData.set(uuid, Key.REQ_CUSTOM, list);
                SessionData.set(uuid, Key.REQ_CUSTOM_DATA, dataMapList);
            }
            if (!requirements.getDetailsOverride().isEmpty()) {
                SessionData.set(uuid, Key.REQ_FAIL_MESSAGE, requirements.getDetailsOverride());
            }
            final Rewards rewards = fabricQuest.getRewards();
            if (rewards.getMoney() != 0) {
                SessionData.set(uuid, Key.REW_MONEY, rewards.getMoney());
            }
            if (rewards.getQuestPoints() != 0) {
                SessionData.set(uuid, Key.REW_QUEST_POINTS, rewards.getQuestPoints());
            }
            if (rewards.getExp() != 0) {
                SessionData.set(uuid, Key.REW_EXP, rewards.getExp());
            }
            if (!rewards.getItems().isEmpty()) {
                SessionData.set(uuid, Key.REW_ITEMS, rewards.getItems());
            }
            if (!rewards.getCommands().isEmpty()) {
                SessionData.set(uuid, Key.REW_COMMAND, rewards.getCommands());
            }
            if (!rewards.getCommandsOverrideDisplay().isEmpty()) {
                SessionData.set(uuid, Key.REW_COMMAND_OVERRIDE_DISPLAY, rewards.getCommandsOverrideDisplay());
            }
            if (!rewards.getPermissions().isEmpty()) {
                SessionData.set(uuid, Key.REW_PERMISSION, rewards.getPermissions());
            }
            if (!rewards.getPermissionWorlds().isEmpty()) {
                SessionData.set(uuid, Key.REW_PERMISSION_WORLDS, rewards.getPermissionWorlds());
            }
            if (!rewards.getHeroesClasses().isEmpty()) {
                SessionData.set(uuid, Key.REW_HEROES_CLASSES, rewards.getHeroesClasses());
                SessionData.set(uuid, Key.REW_HEROES_AMOUNTS, rewards.getHeroesAmounts());
            }
            if (!rewards.getPhatLoots().isEmpty()) {
                SessionData.set(uuid, Key.REW_PHAT_LOOTS, rewards.getPhatLoots());
            }
            if (!rewards.getCustomRewards().isEmpty()) {
                SessionData.set(uuid, Key.REW_CUSTOM, new LinkedList<>(rewards.getCustomRewards().keySet()));
                SessionData.set(uuid, Key.REW_CUSTOM_DATA,
                        new LinkedList<Object>(rewards.getCustomRewards().values()));
            }
            if (!rewards.getDetailsOverride().isEmpty()) {
                SessionData.set(uuid, Key.REW_DETAILS_OVERRIDE, rewards.getDetailsOverride());
            }
            final Planner planner = fabricQuest.getPlanner();
            if (planner.getStart() != null) {
                SessionData.set(uuid, Key.PLN_START_DATE, planner.getStart());
            }
            if (planner.getEnd() != null) {
                SessionData.set(uuid, Key.PLN_END_DATE, planner.getEnd());
            }
            if (planner.getRepeat() != -1) {
                SessionData.set(uuid, Key.PLN_REPEAT_CYCLE, planner.getRepeat());
            }
            if (planner.getCooldown() != -1) {
                SessionData.set(uuid, Key.PLN_COOLDOWN, planner.getCooldown());
            }
            SessionData.set(uuid, Key.PLN_OVERRIDE, planner.getOverride());
            final Options options = fabricQuest.getOptions();
            SessionData.set(uuid, Key.OPT_ALLOW_COMMANDS, options.canAllowCommands());
            SessionData.set(uuid, Key.OPT_ALLOW_QUITTING, options.canAllowQuitting());
            SessionData.set(uuid, Key.OPT_IGNORE_SILK_TOUCH, options.canIgnoreSilkTouch());
            SessionData.set(uuid, Key.OPT_USE_PARTIES_PLUGIN, options.canUsePartiesPlugin());
            SessionData.set(uuid, Key.OPT_SHARE_PROGRESS_LEVEL, options.getShareProgressLevel());
            SessionData.set(uuid, Key.OPT_SHARE_SAME_QUEST_ONLY, options.canShareSameQuestOnly());
            SessionData.set(uuid, Key.OPT_SHARE_DISTANCE, options.getShareDistance());
            SessionData.set(uuid, Key.OPT_HANDLE_OFFLINE_PLAYERS, options.canHandleOfflinePlayers());
            SessionData.set(uuid, Key.OPT_IGNORE_BLOCK_REPLACE, options.canIgnoreBlockReplace());
            SessionData.set(uuid, Key.OPT_GIVE_GLOBALLY_AT_LOGIN, options.canGiveGloballyAtLogin());
            SessionData.set(uuid, Key.OPT_ALLOW_STACKING_GLOBAL, options.canAllowStackingGlobal());
            SessionData.set(uuid, Key.OPT_INFORM_QUEST_START, options.canInformOnStart());
            SessionData.set(uuid, Key.OPT_OVERRIDE_MAX_QUESTS, options.canOverrideMaxQuests());
            // Stages (Objectives)
            int index = 1;
            for (final Stage stage : fabricQuest.getStages()) {
                final FabricStage fabricStage = (FabricStage) stage;
                final String pref = "stage" + index;
                index++;
                SessionData.set(uuid, pref, Boolean.TRUE);
                if (!fabricStage.getBlocksToBreak().isEmpty()) {
                    SessionData.set(uuid, pref + Key.S_BREAK_NAMES, toStringList(fabricStage.getBlocksToBreak()));
                    SessionData.set(uuid, pref + Key.S_BREAK_AMOUNTS,
                            new LinkedList<>(fabricStage.getBlocksToBreakAmounts()));
                    SessionData.set(uuid, pref + Key.S_BREAK_DURABILITY,
                            new LinkedList<>(fabricStage.getBlocksToBreakDurability()));
                }
                if (!fabricStage.getBlocksToDamage().isEmpty()) {
                    SessionData.set(uuid, pref + Key.S_DAMAGE_NAMES, toStringList(fabricStage.getBlocksToDamage()));
                    SessionData.set(uuid, pref + Key.S_DAMAGE_AMOUNTS,
                            new LinkedList<>(fabricStage.getBlocksToDamageAmounts()));
                    SessionData.set(uuid, pref + Key.S_DAMAGE_DURABILITY,
                            new LinkedList<>(fabricStage.getBlocksToDamageDurability()));
                }
                if (!fabricStage.getBlocksToPlace().isEmpty()) {
                    SessionData.set(uuid, pref + Key.S_PLACE_NAMES, toStringList(fabricStage.getBlocksToPlace()));
                    SessionData.set(uuid, pref + Key.S_PLACE_AMOUNTS,
                            new LinkedList<>(fabricStage.getBlocksToPlaceAmounts()));
                    SessionData.set(uuid, pref + Key.S_PLACE_DURABILITY,
                            new LinkedList<>(fabricStage.getBlocksToPlaceDurability()));
                }
                if (!fabricStage.getBlocksToUse().isEmpty()) {
                    SessionData.set(uuid, pref + Key.S_USE_NAMES, toStringList(fabricStage.getBlocksToUse()));
                    SessionData.set(uuid, pref + Key.S_USE_AMOUNTS,
                            new LinkedList<>(fabricStage.getBlocksToUseAmounts()));
                    SessionData.set(uuid, pref + Key.S_USE_DURABILITY,
                            new LinkedList<>(fabricStage.getBlocksToUseDurability()));
                }
                if (!fabricStage.getItemsToCraft().isEmpty()) {
                    SessionData.set(uuid, pref + Key.S_CRAFT_ITEMS, toItemList(fabricStage.getItemsToCraft()));
                }
                if (!fabricStage.getItemsToSmelt().isEmpty()) {
                    SessionData.set(uuid, pref + Key.S_SMELT_ITEMS, toItemList(fabricStage.getItemsToSmelt()));
                }
                if (!fabricStage.getItemsToEnchant().isEmpty()) {
                    SessionData.set(uuid, pref + Key.S_ENCHANT_ITEMS, toItemList(fabricStage.getItemsToEnchant()));
                }
                if (!fabricStage.getItemsToBrew().isEmpty()) {
                    SessionData.set(uuid, pref + Key.S_BREW_ITEMS, toItemList(fabricStage.getItemsToBrew()));
                }
                if (!fabricStage.getItemsToConsume().isEmpty()) {
                    SessionData.set(uuid, pref + Key.S_CONSUME_ITEMS, toItemList(fabricStage.getItemsToConsume()));
                }
                if (fabricStage.getCowsToMilk() != null) {
                    SessionData.set(uuid, pref + Key.S_COW_MILK, fabricStage.getCowsToMilk());
                }
                if (fabricStage.getFishToCatch() != null) {
                    SessionData.set(uuid, pref + Key.S_FISH, fabricStage.getFishToCatch());
                }
                if (fabricStage.getPlayersToKill() != null) {
                    SessionData.set(uuid, pref + Key.S_PLAYER_KILL, fabricStage.getPlayersToKill());
                }
                if (!fabricStage.getItemsToDeliver().isEmpty()) {
                    final LinkedList<String> npcs = new LinkedList<>();
                    for (final UUID u : fabricStage.getItemDeliveryTargets()) {
                        npcs.add(u.toString());
                    }
                    SessionData.set(uuid, pref + Key.S_DELIVERY_ITEMS, toItemList(fabricStage.getItemsToDeliver()));
                    SessionData.set(uuid, pref + Key.S_DELIVERY_NPCS, npcs);
                    SessionData.set(uuid, pref + Key.S_DELIVERY_MESSAGES, fabricStage.getDeliverMessages());
                }
                if (!fabricStage.getNpcsToInteract().isEmpty()) {
                    final LinkedList<String> npcs = new LinkedList<>();
                    for (final UUID u : fabricStage.getNpcsToInteract()) {
                        npcs.add(u.toString());
                    }
                    SessionData.set(uuid, pref + Key.S_NPCS_TO_TALK_TO, npcs);
                }
                if (!fabricStage.getNpcsToKill().isEmpty()) {
                    final LinkedList<String> npcs = new LinkedList<>();
                    for (final UUID u : fabricStage.getNpcsToKill()) {
                        npcs.add(u.toString());
                    }
                    SessionData.set(uuid, pref + Key.S_NPCS_TO_KILL, npcs);
                    SessionData.set(uuid, pref + Key.S_NPCS_TO_KILL_AMOUNTS, fabricStage.getNpcNumToKill());
                }
                if (!fabricStage.getMobsToKill().isEmpty()) {
                    SessionData.set(uuid, pref + Key.S_MOB_TYPES, toStringList(fabricStage.getMobsToKill()));
                    SessionData.set(uuid, pref + Key.S_MOB_AMOUNTS,
                            new LinkedList<>(fabricStage.getMobNumToKill()));
                    if (!fabricStage.getLocationsToKillWithin().isEmpty()) {
                        SessionData.set(uuid, pref + Key.S_MOB_KILL_LOCATIONS,
                                toStringList(fabricStage.getLocationsToKillWithin()));
                        SessionData.set(uuid, pref + Key.S_MOB_KILL_LOCATIONS_RADIUS,
                                fabricStage.getRadiiToKillWithin());
                        SessionData.set(uuid, pref + Key.S_MOB_KILL_LOCATIONS_NAMES,
                                fabricStage.getKillNames());
                    }
                }
                if (!fabricStage.getLocationsToReach().isEmpty()) {
                    SessionData.set(uuid, pref + Key.S_REACH_LOCATIONS,
                            toStringList(fabricStage.getLocationsToReach()));
                    SessionData.set(uuid, pref + Key.S_REACH_LOCATIONS_RADIUS,
                            fabricStage.getRadiiToReachWithin());
                    SessionData.set(uuid, pref + Key.S_REACH_LOCATIONS_NAMES,
                            fabricStage.getLocationNames());
                }
                if (!fabricStage.getMobsToTame().isEmpty()) {
                    SessionData.set(uuid, pref + Key.S_TAME_TYPES, toStringList(fabricStage.getMobsToTame()));
                    SessionData.set(uuid, pref + Key.S_TAME_AMOUNTS,
                            new LinkedList<>(fabricStage.getMobNumToTame()));
                }
                if (!fabricStage.getSheepToShear().isEmpty()) {
                    SessionData.set(uuid, pref + Key.S_SHEAR_COLORS, toStringList(fabricStage.getSheepToShear()));
                    SessionData.set(uuid, pref + Key.S_SHEAR_AMOUNTS,
                            new LinkedList<>(fabricStage.getSheepNumToShear()));
                }
                if (!fabricStage.getPasswordDisplays().isEmpty()) {
                    SessionData.set(uuid, pref + Key.S_PASSWORD_DISPLAYS, fabricStage.getPasswordDisplays());
                    SessionData.set(uuid, pref + Key.S_PASSWORD_PHRASES, fabricStage.getPasswordPhrases());
                }
                if (!fabricStage.getCustomObjectives().isEmpty()) {
                    final LinkedList<String> list = new LinkedList<>();
                    final LinkedList<Integer> countList = new LinkedList<>();
                    for (int i = 0; i < fabricStage.getCustomObjectives().size(); i++) {
                        list.add(fabricStage.getCustomObjectives().get(i).getName());
                        countList.add(fabricStage.getCustomObjectiveCounts().get(i));
                    }
                    SessionData.set(uuid, pref + Key.S_CUSTOM_OBJECTIVES, list);
                    SessionData.set(uuid, pref + Key.S_CUSTOM_OBJECTIVES_COUNT, countList);
                    SessionData.set(uuid, pref + Key.S_CUSTOM_OBJECTIVES_DATA,
                            new LinkedList<>(fabricStage.getCustomObjectiveData()));
                }
                if (fabricStage.getStartAction() != null) {
                    SessionData.set(uuid, pref + Key.S_START_EVENT, fabricStage.getStartAction().getName());
                }
                if (fabricStage.getFinishAction() != null) {
                    SessionData.set(uuid, pref + Key.S_FINISH_EVENT, fabricStage.getFinishAction().getName());
                }
                if (fabricStage.getFailAction() != null) {
                    SessionData.set(uuid, pref + Key.S_FAIL_EVENT, fabricStage.getFailAction().getName());
                }
                if (fabricStage.getDeathAction() != null) {
                    SessionData.set(uuid, pref + Key.S_DEATH_EVENT, fabricStage.getDeathAction().getName());
                }
                if (fabricStage.getDisconnectAction() != null) {
                    SessionData.set(uuid, pref + Key.S_DISCONNECT_EVENT,
                            fabricStage.getDisconnectAction().getName());
                }
                if (!fabricStage.getChatActions().isEmpty()) {
                    final LinkedList<String> chatEvents = new LinkedList<>();
                    final LinkedList<String> chatEventTriggers = new LinkedList<>();
                    for (final String s : fabricStage.getChatActions().keySet()) {
                        chatEventTriggers.add(s);
                        chatEvents.add(fabricStage.getChatActions().get(s).getName());
                    }
                    SessionData.set(uuid, pref + Key.S_CHAT_EVENTS, chatEvents);
                    SessionData.set(uuid, pref + Key.S_CHAT_EVENT_TRIGGERS, chatEventTriggers);
                }
                if (!fabricStage.getCommandActions().isEmpty()) {
                    final LinkedList<String> commandEvents = new LinkedList<>();
                    final LinkedList<String> commandEventTriggers = new LinkedList<>();
                    for (final String s : fabricStage.getCommandActions().keySet()) {
                        commandEventTriggers.add(s);
                        commandEvents.add(fabricStage.getCommandActions().get(s).getName());
                    }
                    SessionData.set(uuid, pref + Key.S_COMMAND_EVENTS, commandEvents);
                    SessionData.set(uuid, pref + Key.S_COMMAND_EVENT_TRIGGERS, commandEventTriggers);
                }
                if (fabricStage.getCondition() != null) {
                    SessionData.set(uuid, pref + Key.S_CONDITION, fabricStage.getCondition().getName());
                }
                if (fabricStage.getDelay() != -1) {
                    SessionData.set(uuid, pref + Key.S_DELAY, fabricStage.getDelay());
                    if (fabricStage.getDelayMessage() != null) {
                        SessionData.set(uuid, pref + Key.S_DELAY_MESSAGE, fabricStage.getDelayMessage());
                    }
                }
                if (fabricStage.getCompleteMessage() != null) {
                    SessionData.set(uuid, pref + Key.S_COMPLETE_MESSAGE, fabricStage.getCompleteMessage());
                }
                if (fabricStage.getStartMessage() != null) {
                    SessionData.set(uuid, pref + Key.S_START_MESSAGE, fabricStage.getStartMessage());
                }
                if (!fabricStage.getObjectiveOverrides().isEmpty()) {
                    SessionData.set(uuid, pref + Key.S_OVERRIDE_DISPLAY, fabricStage.getObjectiveOverrides());
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private static LinkedList<String> toStringList(final LinkedList<Object> source) {
        final LinkedList<String> list = new LinkedList<>();
        for (final Object o : source) {
            list.add(Objects.toString(o, null));
        }
        return list;
    }

    private static LinkedList<ItemStack> toItemList(final LinkedList<Object> source) {
        final LinkedList<ItemStack> list = new LinkedList<>();
        for (final Object o : source) {
            if (o instanceof ItemStack) {
                list.add((ItemStack) o);
            }
        }
        return list;
    }

    public void clearData(final UUID uuid) {
        SessionData.clear(uuid);
    }

    public void deleteQuest(final UUID uuid) {
        final String questName = (String) SessionData.get(uuid, Key.ED_QUEST_DELETE);
        if (questName == null) return;
        final Quest removed = plugin.getLoadedQuests().stream()
                .filter(q -> questName.equals(q.getId()))
                .findFirst().orElse(null);
        plugin.getLoadedQuests().remove(removed);
        try {
            final Path index = plugin.getPluginDataFolder().toPath().resolve("storage").resolve("quests.json");
            if (Files.exists(index)) {
                final com.google.gson.Gson gson = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
                final com.google.gson.JsonObject root;
                try (Reader reader = Files.newBufferedReader(index)) {
                    final com.google.gson.JsonElement parsed = com.google.gson.JsonParser.parseReader(reader);
                    root = parsed.isJsonObject() ? parsed.getAsJsonObject() : new com.google.gson.JsonObject();
                }
                if (root.has(questName)) {
                    root.remove(questName);
                    try (Writer writer = Files.newBufferedWriter(index)) {
                        gson.toJson(root, writer);
                    }
                }
            }
        } catch (final Exception e) {
            plugin.getPluginLogger().error("Failed to delete quest from index: {}", questName, e);
        }
        for (final Quester q : plugin.getOfflineQuesters()) {
            q.hardRemove(removed);
        }
        namesOfQuestsBeingEdited.remove(questName);
        selectingNpcs.remove(uuid);
        plugin.getPluginLogger().info("Deleted quest: {}", questName);
    }

    public void saveQuest(final UUID uuid, final JsonObject section) {
        section.addProperty("name", SessionData.get(uuid, Key.Q_NAME) != null
                ? (String) SessionData.get(uuid, Key.Q_NAME) : null);
        section.addProperty("ask-message", SessionData.get(uuid, Key.Q_ASK_MESSAGE) != null
                ? (String) SessionData.get(uuid, Key.Q_ASK_MESSAGE) : null);
        section.addProperty("finish-message", SessionData.get(uuid, Key.Q_FINISH_MESSAGE) != null
                ? (String) SessionData.get(uuid, Key.Q_FINISH_MESSAGE) : null);
        section.addProperty("npc-giver-uuid", SessionData.get(uuid, Key.Q_START_NPC) != null
                ? (String) SessionData.get(uuid, Key.Q_START_NPC) : null);
        if (SessionData.get(uuid, Key.Q_START_BLOCK) instanceof FabricLocation) {
            final FabricLocation loc = (FabricLocation) SessionData.get(uuid, Key.Q_START_BLOCK);
            if (loc != null && loc.getLevel() != null) {
                section.addProperty("block-start-world", loc.getWorldId());
                section.addProperty("block-start-x", loc.getBlockPos().getX());
                section.addProperty("block-start-y", loc.getBlockPos().getY());
                section.addProperty("block-start-z", loc.getBlockPos().getZ());
            }
        }
        section.addProperty("event", SessionData.get(uuid, Key.Q_INITIAL_EVENT) != null
                ? (String) SessionData.get(uuid, Key.Q_INITIAL_EVENT) : null);
        section.addProperty("region", SessionData.get(uuid, Key.Q_REGION) != null
                ? (String) SessionData.get(uuid, Key.Q_REGION) : null);
        if (SessionData.get(uuid, Key.Q_GUIDISPLAY) instanceof ItemStack) {
            ItemStack display = (ItemStack) SessionData.get(uuid, Key.Q_GUIDISPLAY);
            section.add("gui-display", FabricItemUtil.serializeToJson(display));
        }

        saveRequirements(uuid, section);
        saveStages(uuid, section);
        saveRewards(uuid, section);
        savePlanner(uuid, section);
        saveOptions(uuid, section);
        if (plugin.getConfigSettings().getConsoleLogging() > 0) {
            plugin.getPluginLogger().info("{} saved quest {}", uuid, SessionData.get(uuid, Key.Q_NAME));
        }
        SessionData.clear(uuid);
    }

    @SuppressWarnings("unchecked")
    private static void saveRequirements(final UUID uuid, final JsonObject questData) {
        final JsonObject requirements = new JsonObject();
        requirements.addProperty("quest-points", (Number) SessionData.get(uuid, Key.REQ_QUEST_POINTS));
        final Number exp = (Number) SessionData.get(uuid, Key.REQ_EXP);
        if (exp != null) {
            requirements.addProperty("exp", exp);
        }
        final List<String> quests = (List<String>) SessionData.get(uuid, Key.REQ_QUEST);
        if (quests != null && !quests.isEmpty()) {
            requirements.add("quests", GSON.toJsonTree(quests));
        }
        final List<String> questBlocks = (List<String>) SessionData.get(uuid, Key.REQ_QUEST_BLOCK);
        if (questBlocks != null && !questBlocks.isEmpty()) {
            requirements.add("quest-blocks", GSON.toJsonTree(questBlocks));
        }
        final List<String> permissions = (List<String>) SessionData.get(uuid, Key.REQ_PERMISSION);
        if (permissions != null && !permissions.isEmpty()) {
            requirements.add("permissions", GSON.toJsonTree(permissions));
        }
        final List<ItemStack> items = (List<ItemStack>) SessionData.get(uuid, Key.REQ_ITEMS);
        if (items != null && !items.isEmpty()) {
            final JsonArray itemArray = toItemArray(items);
            if (!itemArray.isEmpty()) {
                requirements.add("items", itemArray);
            }
        }
        final List<Boolean> removeItems = (List<Boolean>) SessionData.get(uuid, Key.REQ_ITEMS_REMOVE);
        if (removeItems != null && !removeItems.isEmpty()) {
            requirements.add("remove-items", GSON.toJsonTree(removeItems));
        }
        final List<String> fail = (List<String>) SessionData.get(uuid, Key.REQ_FAIL_MESSAGE);
        if (fail != null && !fail.isEmpty()) {
            requirements.add("fail-requirement-message", GSON.toJsonTree(fail));
        }
        final List<String> custom = (List<String>) SessionData.get(uuid, Key.REQ_CUSTOM);
        final List<Map<String, Object>> customData =
                (List<Map<String, Object>>) SessionData.get(uuid, Key.REQ_CUSTOM_DATA);
        saveCustomSection(requirements, "custom-requirements", "req", custom, customData);
        if (!requirements.isEmpty()) {
            questData.add("requirements", requirements);
        }
    }

    private static void savePlanner(final UUID uuid, final JsonObject questData) {
        final JsonObject planner = new JsonObject();
        final String start = (String) SessionData.get(uuid, Key.PLN_START_DATE);
        if (start != null) {
            planner.addProperty("start", start);
        }
        final String end = (String) SessionData.get(uuid, Key.PLN_END_DATE);
        if (end != null) {
            planner.addProperty("end", end);
        }
        final Number repeat = (Number) SessionData.get(uuid, Key.PLN_REPEAT_CYCLE);
        if (repeat != null) {
            planner.addProperty("repeat", repeat);
        }
        final Number cooldown = (Number) SessionData.get(uuid, Key.PLN_COOLDOWN);
        if (cooldown != null) {
            planner.addProperty("cooldown", cooldown);
        }
        final Boolean override = (Boolean) SessionData.get(uuid, Key.PLN_OVERRIDE);
        if (override != null) {
            planner.addProperty("override", override);
        }
        if (!planner.isEmpty()) {
            questData.add("planner", planner);
        }
    }

    @SuppressWarnings("unchecked")
    private static void saveRewards(final UUID uuid, final JsonObject questData) {
        final JsonObject rewards = new JsonObject();
        final Number questPoints = (Number) SessionData.get(uuid, Key.REW_QUEST_POINTS);
        if (questPoints != null) {
            rewards.addProperty("quest-points", questPoints);
        }
        final Number exp = (Number) SessionData.get(uuid, Key.REW_EXP);
        if (exp != null) {
            rewards.addProperty("exp", exp);
        }
        final List<String> commands = (List<String>) SessionData.get(uuid, Key.REW_COMMAND);
        if (commands != null && !commands.isEmpty()) {
            rewards.add("commands", GSON.toJsonTree(commands));
        }
        final List<String> commandOverrides =
                (List<String>) SessionData.get(uuid, Key.REW_COMMAND_OVERRIDE_DISPLAY);
        if (commandOverrides != null && !commandOverrides.isEmpty()) {
            rewards.add("commands-override-display", GSON.toJsonTree(commandOverrides));
        }
        final List<String> permissions = (List<String>) SessionData.get(uuid, Key.REW_PERMISSION);
        if (permissions != null && !permissions.isEmpty()) {
            rewards.add("permissions", GSON.toJsonTree(permissions));
        }
        final List<String> permissionWorlds = (List<String>) SessionData.get(uuid, Key.REW_PERMISSION_WORLDS);
        if (permissionWorlds != null && !permissionWorlds.isEmpty()) {
            rewards.add("permission-worlds", GSON.toJsonTree(permissionWorlds));
        }
        final List<String> detailsOverrides = (List<String>) SessionData.get(uuid, Key.REW_DETAILS_OVERRIDE);
        if (detailsOverrides != null && !detailsOverrides.isEmpty()) {
            rewards.add("details-override", GSON.toJsonTree(detailsOverrides));
        }
        final List<ItemStack> items = (List<ItemStack>) SessionData.get(uuid, Key.REW_ITEMS);
        if (items != null && !items.isEmpty()) {
            final JsonArray itemArray = toItemArray(items);
            if (!itemArray.isEmpty()) {
                rewards.add("items", itemArray);
            }
        }
        final List<String> custom = (List<String>) SessionData.get(uuid, Key.REW_CUSTOM);
        final List<Map<String, Object>> customData =
                (List<Map<String, Object>>) SessionData.get(uuid, Key.REW_CUSTOM_DATA);
        saveCustomSection(rewards, "custom-rewards", "rew", custom, customData);
        if (!rewards.isEmpty()) {
            questData.add("rewards", rewards);
        }
    }

    private static void saveOptions(final UUID uuid, final JsonObject questData) {
        final JsonObject options = new JsonObject();
        final Boolean allowCommands = (Boolean) SessionData.get(uuid, Key.OPT_ALLOW_COMMANDS);
        if (allowCommands != null) {
            options.addProperty("allow-commands", allowCommands);
        }
        final Boolean allowQuitting = (Boolean) SessionData.get(uuid, Key.OPT_ALLOW_QUITTING);
        if (allowQuitting != null) {
            options.addProperty("allow-quitting", allowQuitting);
        }
        final Boolean ignoreSilkTouch = (Boolean) SessionData.get(uuid, Key.OPT_IGNORE_SILK_TOUCH);
        if (ignoreSilkTouch != null) {
            options.addProperty("ignore-silk-touch", ignoreSilkTouch);
        }
        final Boolean usePartiesPlugin = (Boolean) SessionData.get(uuid, Key.OPT_USE_PARTIES_PLUGIN);
        if (usePartiesPlugin != null) {
            options.addProperty("use-parties-plugin", usePartiesPlugin);
        }
        final Number shareProgressLevel = (Number) SessionData.get(uuid, Key.OPT_SHARE_PROGRESS_LEVEL);
        if (shareProgressLevel != null) {
            options.addProperty("share-progress-level", shareProgressLevel);
        }
        final Boolean shareSameQuestOnly = (Boolean) SessionData.get(uuid, Key.OPT_SHARE_SAME_QUEST_ONLY);
        if (shareSameQuestOnly != null) {
            options.addProperty("same-quest-only", shareSameQuestOnly);
        }
        final Number shareDistance = (Number) SessionData.get(uuid, Key.OPT_SHARE_DISTANCE);
        if (shareDistance != null) {
            options.addProperty("share-distance", shareDistance);
        }
        final Boolean handleOfflinePlayers = (Boolean) SessionData.get(uuid, Key.OPT_HANDLE_OFFLINE_PLAYERS);
        if (handleOfflinePlayers != null) {
            options.addProperty("handle-offline-players", handleOfflinePlayers);
        }
        final Boolean ignoreBlockReplace = (Boolean) SessionData.get(uuid, Key.OPT_IGNORE_BLOCK_REPLACE);
        if (ignoreBlockReplace != null) {
            options.addProperty("ignore-block-replace", ignoreBlockReplace);
        }
        final Boolean giveAtLogin = (Boolean) SessionData.get(uuid, Key.OPT_GIVE_GLOBALLY_AT_LOGIN);
        if (giveAtLogin != null) {
            options.addProperty("give-at-login", giveAtLogin);
        }
        final Boolean allowStackingGlobal = (Boolean) SessionData.get(uuid, Key.OPT_ALLOW_STACKING_GLOBAL);
        if (allowStackingGlobal != null) {
            options.addProperty("allow-stacking-global", allowStackingGlobal);
        }
        final Boolean informOnStart = (Boolean) SessionData.get(uuid, Key.OPT_INFORM_QUEST_START);
        if (informOnStart != null) {
            options.addProperty("inform-on-start", informOnStart);
        }
        final Boolean overrideMaxQuests = (Boolean) SessionData.get(uuid, Key.OPT_OVERRIDE_MAX_QUESTS);
        if (overrideMaxQuests != null) {
            options.addProperty("override-max-quests", overrideMaxQuests);
        }
        if (!options.isEmpty()) {
            questData.add("options", options);
        }
    }

    @SuppressWarnings("unchecked")
    private void saveStages(final UUID uuid, final JsonObject questData) {
        final JsonArray stages = new JsonArray();
        int num = 1;
        while (SessionData.get(uuid, "stage" + num) != null) {
            final String p = "stage" + num;
            final JsonObject json = new JsonObject();
            final String startMessage = (String) SessionData.get(uuid, p + Key.S_START_MESSAGE);
            if (startMessage != null) {
                json.addProperty("start-message", startMessage);
            }
            final String completeMessage = (String) SessionData.get(uuid, p + Key.S_COMPLETE_MESSAGE);
            if (completeMessage != null) {
                json.addProperty("complete-message", completeMessage);
            }
            final Number delay = (Number) SessionData.get(uuid, p + Key.S_DELAY);
            if (delay != null) {
                json.addProperty("delay", delay);
            }
            final String delayMessage = (String) SessionData.get(uuid, p + Key.S_DELAY_MESSAGE);
            if (delayMessage != null) {
                json.addProperty("delay-message", delayMessage);
            }
            final List<String> passwordDisplays = (List<String>) SessionData.get(uuid, p + Key.S_PASSWORD_DISPLAYS);
            if (passwordDisplays != null && !passwordDisplays.isEmpty()) {
                json.add("password-displays", GSON.toJsonTree(passwordDisplays));
            }
            final List<String> passwordPhrases = (List<String>) SessionData.get(uuid, p + Key.S_PASSWORD_PHRASES);
            if (passwordPhrases != null && !passwordPhrases.isEmpty()) {
                json.add("password-phrases", GSON.toJsonTree(passwordPhrases));
            }

            saveBlockLists(json, "break", (List<String>) SessionData.get(uuid, p + Key.S_BREAK_NAMES),
                    (List<Integer>) SessionData.get(uuid, p + Key.S_BREAK_AMOUNTS),
                    (List<Integer>) SessionData.get(uuid, p + Key.S_BREAK_DURABILITY));
            saveBlockLists(json, "damage", (List<String>) SessionData.get(uuid, p + Key.S_DAMAGE_NAMES),
                    (List<Integer>) SessionData.get(uuid, p + Key.S_DAMAGE_AMOUNTS),
                    (List<Integer>) SessionData.get(uuid, p + Key.S_DAMAGE_DURABILITY));
            saveBlockLists(json, "place", (List<String>) SessionData.get(uuid, p + Key.S_PLACE_NAMES),
                    (List<Integer>) SessionData.get(uuid, p + Key.S_PLACE_AMOUNTS),
                    (List<Integer>) SessionData.get(uuid, p + Key.S_PLACE_DURABILITY));
            saveBlockLists(json, "use", (List<String>) SessionData.get(uuid, p + Key.S_USE_NAMES),
                    (List<Integer>) SessionData.get(uuid, p + Key.S_USE_AMOUNTS),
                    (List<Integer>) SessionData.get(uuid, p + Key.S_USE_DURABILITY));

            final List<ItemStack> craftItems = (List<ItemStack>) SessionData.get(uuid, p + Key.S_CRAFT_ITEMS);
            if (craftItems != null && !craftItems.isEmpty()) {
                final JsonArray itemArray = toItemArray(craftItems);
                if (!itemArray.isEmpty()) {
                    json.add("items-to-craft", itemArray);
                }
            }
            final List<ItemStack> smeltItems = (List<ItemStack>) SessionData.get(uuid, p + Key.S_SMELT_ITEMS);
            if (smeltItems != null && !smeltItems.isEmpty()) {
                final JsonArray itemArray = toItemArray(smeltItems);
                if (!itemArray.isEmpty()) {
                    json.add("items-to-smelt", itemArray);
                }
            }
            final List<ItemStack> enchantItems = (List<ItemStack>) SessionData.get(uuid, p + Key.S_ENCHANT_ITEMS);
            if (enchantItems != null && !enchantItems.isEmpty()) {
                final JsonArray itemArray = toItemArray(enchantItems);
                if (!itemArray.isEmpty()) {
                    json.add("items-to-enchant", itemArray);
                }
            }
            final List<ItemStack> brewItems = (List<ItemStack>) SessionData.get(uuid, p + Key.S_BREW_ITEMS);
            if (brewItems != null && !brewItems.isEmpty()) {
                final JsonArray itemArray = toItemArray(brewItems);
                if (!itemArray.isEmpty()) {
                    json.add("items-to-brew", itemArray);
                }
            }
            final List<ItemStack> consumeItems = (List<ItemStack>) SessionData.get(uuid, p + Key.S_CONSUME_ITEMS);
            if (consumeItems != null && !consumeItems.isEmpty()) {
                final JsonArray itemArray = toItemArray(consumeItems);
                if (!itemArray.isEmpty()) {
                    json.add("items-to-consume", itemArray);
                }
            }
            final List<ItemStack> deliverItems = (List<ItemStack>) SessionData.get(uuid, p + Key.S_DELIVERY_ITEMS);
            if (deliverItems != null && !deliverItems.isEmpty()) {
                final JsonArray itemArray = toItemArray(deliverItems);
                if (!itemArray.isEmpty()) {
                    json.add("items-to-deliver", itemArray);
                }
            }
            final List<String> deliveryNpcs = (List<String>) SessionData.get(uuid, p + Key.S_DELIVERY_NPCS);
            if (deliveryNpcs != null && !deliveryNpcs.isEmpty()) {
                json.add("npc-delivery-uuids", GSON.toJsonTree(deliveryNpcs));
            }
            final List<String> deliveryMessages = (List<String>) SessionData.get(uuid, p + Key.S_DELIVERY_MESSAGES);
            if (deliveryMessages != null && !deliveryMessages.isEmpty()) {
                json.add("delivery-messages", GSON.toJsonTree(deliveryMessages));
            }

            final List<String> mobs = (List<String>) SessionData.get(uuid, p + Key.S_MOB_TYPES);
            if (mobs != null && !mobs.isEmpty()) {
                json.add("mobs-to-kill", GSON.toJsonTree(mobs));
            }
            final List<Integer> mobAmounts = (List<Integer>) SessionData.get(uuid, p + Key.S_MOB_AMOUNTS);
            if (mobAmounts != null && !mobAmounts.isEmpty()) {
                json.add("mob-amounts", GSON.toJsonTree(mobAmounts));
            }
            final List<String> npcsToTalkTo = (List<String>) SessionData.get(uuid, p + Key.S_NPCS_TO_TALK_TO);
            if (npcsToTalkTo != null && !npcsToTalkTo.isEmpty()) {
                json.add("npc-uuids-to-talk-to", GSON.toJsonTree(npcsToTalkTo));
            }
            final List<String> npcsToKill = (List<String>) SessionData.get(uuid, p + Key.S_NPCS_TO_KILL);
            if (npcsToKill != null && !npcsToKill.isEmpty()) {
                json.add("npc-uuids-to-kill", GSON.toJsonTree(npcsToKill));
            }
            final List<Integer> npcKillAmounts = (List<Integer>) SessionData.get(uuid, p + Key.S_NPCS_TO_KILL_AMOUNTS);
            if (npcKillAmounts != null && !npcKillAmounts.isEmpty()) {
                json.add("npc-kill-amounts", GSON.toJsonTree(npcKillAmounts));
            }
            final Number playersToKill = (Number) SessionData.get(uuid, p + Key.S_PLAYER_KILL);
            if (playersToKill != null) {
                json.addProperty("players-to-kill", playersToKill);
            }
            final List<String> killLocations = (List<String>) SessionData.get(uuid, p + Key.S_MOB_KILL_LOCATIONS);
            if (killLocations != null && !killLocations.isEmpty()) {
                json.add("locations-to-kill", GSON.toJsonTree(killLocations));
            }
            final List<Integer> killRadii =
                    (List<Integer>) SessionData.get(uuid, p + Key.S_MOB_KILL_LOCATIONS_RADIUS);
            if (killRadii != null && !killRadii.isEmpty()) {
                json.add("kill-location-radii", GSON.toJsonTree(killRadii));
            }
            final List<String> killNames = (List<String>) SessionData.get(uuid, p + Key.S_MOB_KILL_LOCATIONS_NAMES);
            if (killNames != null && !killNames.isEmpty()) {
                json.add("kill-location-names", GSON.toJsonTree(killNames));
            }
            final List<String> reachLocations = (List<String>) SessionData.get(uuid, p + Key.S_REACH_LOCATIONS);
            if (reachLocations != null && !reachLocations.isEmpty()) {
                json.add("locations-to-reach", GSON.toJsonTree(reachLocations));
            }
            final List<Integer> reachRadii = (List<Integer>) SessionData.get(uuid, p + Key.S_REACH_LOCATIONS_RADIUS);
            if (reachRadii != null && !reachRadii.isEmpty()) {
                json.add("reach-location-radii", GSON.toJsonTree(reachRadii));
            }
            final List<String> reachNames = (List<String>) SessionData.get(uuid, p + Key.S_REACH_LOCATIONS_NAMES);
            if (reachNames != null && !reachNames.isEmpty()) {
                json.add("reach-location-names", GSON.toJsonTree(reachNames));
            }
            final List<String> tameMobs = (List<String>) SessionData.get(uuid, p + Key.S_TAME_TYPES);
            if (tameMobs != null && !tameMobs.isEmpty()) {
                json.add("mobs-to-tame", GSON.toJsonTree(tameMobs));
            }
            final List<Integer> tameAmounts = (List<Integer>) SessionData.get(uuid, p + Key.S_TAME_AMOUNTS);
            if (tameAmounts != null && !tameAmounts.isEmpty()) {
                json.add("mob-tame-amounts", GSON.toJsonTree(tameAmounts));
            }
            final List<String> shearColors = (List<String>) SessionData.get(uuid, p + Key.S_SHEAR_COLORS);
            if (shearColors != null && !shearColors.isEmpty()) {
                json.add("sheep-to-shear", GSON.toJsonTree(shearColors));
            }
            final List<Integer> shearAmounts = (List<Integer>) SessionData.get(uuid, p + Key.S_SHEAR_AMOUNTS);
            if (shearAmounts != null && !shearAmounts.isEmpty()) {
                json.add("sheep-amounts", GSON.toJsonTree(shearAmounts));
            }
            final Number fishToCatch = (Number) SessionData.get(uuid, p + Key.S_FISH);
            if (fishToCatch != null) {
                json.addProperty("fish-to-catch", fishToCatch);
            }
            final Number cowsToMilk = (Number) SessionData.get(uuid, p + Key.S_COW_MILK);
            if (cowsToMilk != null) {
                json.addProperty("cows-to-milk", cowsToMilk);
            }
            final List<String> objectiveOverrides = (List<String>) SessionData.get(uuid, p + Key.S_OVERRIDE_DISPLAY);
            if (objectiveOverrides != null && !objectiveOverrides.isEmpty()) {
                json.add("objective-override", GSON.toJsonTree(objectiveOverrides));
            }

            final String startEvent = (String) SessionData.get(uuid, p + Key.S_START_EVENT);
            if (startEvent != null) {
                json.addProperty("start-event", startEvent);
            }
            final String finishEvent = (String) SessionData.get(uuid, p + Key.S_FINISH_EVENT);
            if (finishEvent != null) {
                json.addProperty("finish-event", finishEvent);
            }
            final String failEvent = (String) SessionData.get(uuid, p + Key.S_FAIL_EVENT);
            if (failEvent != null) {
                json.addProperty("fail-event", failEvent);
            }
            final String deathEvent = (String) SessionData.get(uuid, p + Key.S_DEATH_EVENT);
            if (deathEvent != null) {
                json.addProperty("death-event", deathEvent);
            }
            final String disconnectEvent = (String) SessionData.get(uuid, p + Key.S_DISCONNECT_EVENT);
            if (disconnectEvent != null) {
                json.addProperty("disconnect-event", disconnectEvent);
            }
            saveTriggeredActions(json, "chat-events", "chat-event-triggers",
                    (List<String>) SessionData.get(uuid, p + Key.S_CHAT_EVENTS),
                    (List<String>) SessionData.get(uuid, p + Key.S_CHAT_EVENT_TRIGGERS));
            saveTriggeredActions(json, "command-events", "command-event-triggers",
                    (List<String>) SessionData.get(uuid, p + Key.S_COMMAND_EVENTS),
                    (List<String>) SessionData.get(uuid, p + Key.S_COMMAND_EVENT_TRIGGERS));
            final String condition = (String) SessionData.get(uuid, p + Key.S_CONDITION);
            if (condition != null) {
                json.addProperty("condition", condition);
            }
            saveCustomObjectives(json, uuid, p);
            stages.add(json);
            num++;
        }
        if (!stages.isEmpty()) {
            questData.add("stages", stages);
        }
    }

    private static void saveBlockLists(final JsonObject json, final String type, final List<String> names,
                                       final List<Integer> amounts, final List<Integer> durability) {
        if (names != null && !names.isEmpty()) {
            json.add(type + "-block-names", GSON.toJsonTree(names));
        }
        if (amounts != null && !amounts.isEmpty()) {
            json.add(type + "-block-amounts", GSON.toJsonTree(amounts));
        }
        if (durability != null && !durability.isEmpty()) {
            json.add(type + "-block-durability", GSON.toJsonTree(durability));
        }
    }

    private static void saveCustomSection(final JsonObject json, final String sectionKey, final String entryPrefix,
                                          final List<String> names, final List<Map<String, Object>> data) {
        if (names == null || names.isEmpty()) {
            return;
        }
        final JsonObject section = new JsonObject();
        for (int i = 0; i < names.size(); i++) {
            final String name = names.get(i);
            if (name == null) {
                continue;
            }
            final JsonObject entry = new JsonObject();
            entry.addProperty("name", name);
            if (data != null && i < data.size() && data.get(i) != null) {
                entry.add("data", GSON.toJsonTree(data.get(i)));
            }
            section.add(entryPrefix + (i + 1), entry);
        }
        if (!section.isEmpty()) {
            json.add(sectionKey, section);
        }
    }

    @SuppressWarnings("unchecked")
    private void saveCustomObjectives(final JsonObject json, final UUID uuid, final String prefix) {
        final List<String> names = (List<String>) SessionData.get(uuid, prefix + Key.S_CUSTOM_OBJECTIVES);
        if (names == null || names.isEmpty()) {
            return;
        }
        final List<Integer> counts =
                (List<Integer>) SessionData.get(uuid, prefix + Key.S_CUSTOM_OBJECTIVES_COUNT);
        final List<Map.Entry<String, Object>> data =
                (List<Map.Entry<String, Object>>) SessionData.get(uuid, prefix + Key.S_CUSTOM_OBJECTIVES_DATA);
        final JsonObject section = new JsonObject();
        for (int i = 0; i < names.size(); i++) {
            final String name = names.get(i);
            if (name == null) {
                continue;
            }
            final JsonObject entry = new JsonObject();
            entry.addProperty("name", name);
            if (counts != null && i < counts.size()) {
                final Integer count = counts.get(i);
                if (count != null && count >= 0) {
                    entry.addProperty("count", count);
                }
            }
            final JsonObject dataJson = new JsonObject();
            final CustomObjective found = findCustomObjective(name);
            if (found != null) {
                for (final Map.Entry<String, Object> prompt : found.getData()) {
                    Object value = prompt.getValue();
                    if (data != null) {
                        for (final Map.Entry<String, Object> datum : data) {
                            if (prompt.getKey().equals(datum.getKey())) {
                                value = datum.getValue();
                            }
                        }
                    }
                    dataJson.add(prompt.getKey(), GSON.toJsonTree(value));
                }
            }
            if (!dataJson.isEmpty()) {
                entry.add("data", dataJson);
            }
            section.add("custom" + (i + 1), entry);
        }
        if (!section.isEmpty()) {
            json.add("custom-objectives", section);
        }
    }

    private CustomObjective findCustomObjective(String name) {
        for (final CustomObjective objective : plugin.getCustomObjectives()) {
            if (objective.getName().equalsIgnoreCase(name)) {
                return objective;
            }
        }
        return null;
    }

    private static void saveTriggeredActions(final JsonObject json, final String eventsKey, final String triggersKey,
                                             final List<String> events, final List<String> triggers) {
        if (events != null && triggers != null && !events.isEmpty()) {
            json.add(eventsKey, GSON.toJsonTree(events));
            json.add(triggersKey, GSON.toJsonTree(triggers));
        }
    }

    private static JsonArray toItemArray(final List<ItemStack> items) {
        final JsonArray array = new JsonArray();
        for (final ItemStack item : items) {
            array.add(FabricItemUtil.serializeToJson(item));
        }
        return array;
    }
}
