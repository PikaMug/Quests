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

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.convo.quests.main.FabricQuestMainPrompt;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.components.FabricStage;
import me.pikamug.quests.quests.components.Options;
import me.pikamug.quests.quests.components.Planner;
import me.pikamug.quests.quests.components.Requirements;
import me.pikamug.quests.quests.components.Rewards;
import me.pikamug.quests.quests.components.Stage;
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
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FabricQuestFactory implements QuestFactory {

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
}
