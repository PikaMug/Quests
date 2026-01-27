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

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.convo.quests.main.QuestMainPrompt;
import me.pikamug.quests.convo.quests.stages.QuestStageMenuPrompt;
import me.pikamug.quests.interfaces.ReloadCallback;
import me.pikamug.quests.module.CustomObjective;
import me.pikamug.quests.quests.components.BukkitStage;
import me.pikamug.quests.quests.components.Options;
import me.pikamug.quests.quests.components.Planner;
import me.pikamug.quests.quests.components.Requirements;
import me.pikamug.quests.quests.components.Rewards;
import me.pikamug.quests.quests.components.Stage;
import me.pikamug.quests.util.BukkitConfigUtil;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.quests.util.BukkitMiscUtil;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.SessionData;
import me.pikamug.quests.util.stack.BlockItemStack;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class BukkitQuestFactory implements QuestFactory/*, ConversationAbandonedListener*/ {

    private final BukkitQuestsPlugin plugin;
    private ConcurrentHashMap<UUID, Block> selectedBlockStarts = new ConcurrentHashMap<>();
    private ConcurrentHashMap<UUID, Block> selectedKillLocations = new ConcurrentHashMap<>();
    private ConcurrentHashMap<UUID, Block> selectedReachLocations = new ConcurrentHashMap<>();
    private ConcurrentSkipListSet<UUID> selectingNpcs = new ConcurrentSkipListSet<>();
    private List<String> editingQuestNames = new LinkedList<>();

    public BukkitQuestFactory(final BukkitQuestsPlugin plugin) {
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

    public ConcurrentHashMap<UUID, Block> getSelectedReachLocations() {
        return selectedReachLocations;
    }

    public void setSelectedReachLocations(final ConcurrentHashMap<UUID, Block> selectedReachLocations) {
        this.selectedReachLocations = selectedReachLocations;
    }

    public ConcurrentSkipListSet<UUID> getSelectingNpcs() {
        return selectingNpcs;
    }

    public void setSelectingNpcs(final ConcurrentSkipListSet<UUID> selectingNpcs) {
        this.selectingNpcs = selectingNpcs;
    }

    public List<String> getNamesOfQuestsBeingEdited() {
        return editingQuestNames;
    }

    public void setNamesOfQuestsBeingEdited(final List<String> questNames) {
        this.editingQuestNames = questNames;
    }

    /*public ConversationFactory getConversationFactory() {
        return conversationFactory;
    }*/

    /*@Override
    public void conversationAbandoned(final ConversationAbandonedEvent abandonedEvent) {
        if (abandonedEvent.getContext().getSessionData(Key.Q_NAME) != null) {
            editingQuestNames.remove((String) abandonedEvent.getContext().getSessionData(Key.Q_NAME));
        }
        if (abandonedEvent.getContext().getForWhom() instanceof Player) {
            final UUID uuid = ((Player) abandonedEvent.getContext().getForWhom()).getUniqueId();
            selectedBlockStarts.remove(uuid);
            selectedKillLocations.remove(uuid);
            selectedReachLocations.remove(uuid);
        }
    }*/

    public void returnToMenu(final UUID uuid) {
        new QuestMainPrompt(uuid).start();
    }

    @SuppressWarnings("deprecation")
    public void loadQuest(final UUID uuid, final Quest quest) {
        BukkitQuest bukkitQuest = (BukkitQuest) quest;
        try {
            SessionData.set(uuid, Key.ED_QUEST_EDIT, bukkitQuest.getName());
            SessionData.set(uuid, Key.Q_ID, bukkitQuest.getId());
            SessionData.set(uuid, Key.Q_NAME, bukkitQuest.getName());
            SessionData.set(uuid, Key.Q_ASK_MESSAGE, bukkitQuest.getDescription());
            SessionData.set(uuid, Key.Q_FINISH_MESSAGE, bukkitQuest.getFinished());
            if (plugin.getDependencies().hasAnyNpcDependencies()) {
                if (bukkitQuest.getNpcStart() != null) {
                    SessionData.set(uuid, Key.Q_START_NPC, bukkitQuest.getNpcStart().toString());
                }
            }
            SessionData.set(uuid, Key.Q_START_BLOCK, bukkitQuest.getBlockStart());
            if (bukkitQuest.getInitialAction() != null) {
                SessionData.set(uuid, Key.Q_INITIAL_EVENT, bukkitQuest.getInitialAction().getName());
            }
            if (bukkitQuest.getRegionStart() != null) {
                SessionData.set(uuid, Key.Q_REGION, bukkitQuest.getRegionStart());
            }
            if (bukkitQuest.getGUIDisplay() != null) {
                SessionData.set(uuid, Key.Q_GUIDISPLAY, bukkitQuest.getGUIDisplay());
            }
            final Requirements requirements = bukkitQuest.getRequirements();
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
            if (!requirements.getMcmmoSkills().isEmpty()) {
                SessionData.set(uuid, Key.REQ_MCMMO_SKILLS, requirements.getMcmmoAmounts());
                SessionData.set(uuid, Key.REQ_MCMMO_SKILL_AMOUNTS, requirements.getMcmmoAmounts());
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
                final LinkedList<Map<String, Object>> dataMapList = new LinkedList<>();
                for (final Entry<String, Map<String, Object>> entry : requirements.getCustomRequirements().entrySet()) {
                    list.add(entry.getKey());
                    dataMapList.add(entry.getValue());
                }
                SessionData.set(uuid, Key.REQ_CUSTOM, list);
                SessionData.set(uuid, Key.REQ_CUSTOM_DATA, dataMapList);
            }
            if (!requirements.getDetailsOverride().isEmpty()) {
                SessionData.set(uuid, Key.REQ_FAIL_MESSAGE, requirements.getDetailsOverride());
            }
            final Rewards rewards = bukkitQuest.getRewards();
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
            if (!rewards.getPermissions().isEmpty()) {
                SessionData.set(uuid, Key.REW_PERMISSION_WORLDS, rewards.getPermissionWorlds());
            }
            if (!rewards.getMcmmoSkills().isEmpty()) {
                SessionData.set(uuid, Key.REW_MCMMO_SKILLS, rewards.getMcmmoSkills());
                SessionData.set(uuid, Key.REW_MCMMO_AMOUNTS, rewards.getMcmmoAmounts());
            }
            if (!rewards.getHeroesClasses().isEmpty()) {
                SessionData.set(uuid, Key.REW_HEROES_CLASSES, rewards.getHeroesClasses());
                SessionData.set(uuid, Key.REW_HEROES_AMOUNTS, rewards.getHeroesAmounts());
            }
            if (rewards.getPartiesExperience() != 0) {
                SessionData.set(uuid, Key.REW_PARTIES_EXPERIENCE, rewards.getPartiesExperience());
            }
            if (!rewards.getPhatLoots().isEmpty()) {
                SessionData.set(uuid, Key.REW_PHAT_LOOTS, rewards.getPhatLoots());
            }
            if (!rewards.getCustomRewards().isEmpty()) {
                SessionData.set(uuid, Key.REW_CUSTOM, new LinkedList<>(rewards.getCustomRewards().keySet()));
                SessionData.set(uuid, Key.REW_CUSTOM_DATA, new LinkedList<Object>(rewards.getCustomRewards().values()));
            }
            if (!rewards.getDetailsOverride().isEmpty()) {
                SessionData.set(uuid, Key.REW_DETAILS_OVERRIDE, rewards.getDetailsOverride());
            }
            final Planner pln = bukkitQuest.getPlanner();
            if (pln.getStart() != null) {
                SessionData.set(uuid, Key.PLN_START_DATE, pln.getStart());
            }
            if (pln.getEnd() != null) {
                SessionData.set(uuid, Key.PLN_END_DATE, pln.getEnd());
            }
            if (pln.getRepeat() != -1) {
                SessionData.set(uuid, Key.PLN_REPEAT_CYCLE, pln.getRepeat());
            }
            if (pln.getCooldown() != -1) {
                SessionData.set(uuid, Key.PLN_COOLDOWN, pln.getCooldown());
            }
            SessionData.set(uuid, Key.PLN_OVERRIDE, pln.getOverride());
            final Options opt = bukkitQuest.getOptions();
            SessionData.set(uuid, Key.OPT_ALLOW_COMMANDS, opt.canAllowCommands());
            SessionData.set(uuid, Key.OPT_ALLOW_QUITTING, opt.canAllowQuitting());
            SessionData.set(uuid, Key.OPT_IGNORE_SILK_TOUCH, opt.canIgnoreSilkTouch());
            SessionData.set(uuid, Key.OPT_EXTERNAL_PARTY_PLUGIN, opt.getExternalPartyPlugin());
            SessionData.set(uuid, Key.OPT_USE_PARTIES_PLUGIN, opt.canUsePartiesPlugin());
            SessionData.set(uuid, Key.OPT_SHARE_PROGRESS_LEVEL, opt.getShareProgressLevel());
            SessionData.set(uuid, Key.OPT_SHARE_SAME_QUEST_ONLY, opt.canShareSameQuestOnly());
            SessionData.set(uuid, Key.OPT_SHARE_DISTANCE, opt.getShareDistance());
            SessionData.set(uuid, Key.OPT_HANDLE_OFFLINE_PLAYERS, opt.canHandleOfflinePlayers());
            SessionData.set(uuid, Key.OPT_IGNORE_BLOCK_REPLACE, opt.canIgnoreBlockReplace());
            SessionData.set(uuid, Key.OPT_GIVE_GLOBALLY_AT_LOGIN, opt.canGiveGloballyAtLogin());
            SessionData.set(uuid, Key.OPT_ALLOW_STACKING_GLOBAL, opt.canAllowStackingGlobal());
            SessionData.set(uuid, Key.OPT_INFORM_QUEST_START, opt.canInformOnStart());
            // Stages (Objectives)
            int index = 1;
            for (final Stage stage : bukkitQuest.getStages()) {
                final BukkitStage bukkitStage = (BukkitStage) stage;
                final String pref = "stage" + index;
                index++;
                SessionData.set(uuid, pref, Boolean.TRUE);
                if (!bukkitStage.getBlocksToBreak().isEmpty()) {
                    final LinkedList<String> names = new LinkedList<>();
                    final LinkedList<Integer> amounts = new LinkedList<>();
                    final LinkedList<Short> durability = new LinkedList<>();
                    for (final BlockItemStack e : bukkitStage.getBlocksToBreak()) {
                        names.add(e.getType().name());
                        amounts.add(e.getAmount());
                        durability.add(e.getDurability());
                    }
                    SessionData.set(uuid, pref + Key.S_BREAK_NAMES, names);
                    SessionData.set(uuid, pref + Key.S_BREAK_AMOUNTS, amounts);
                    SessionData.set(uuid, pref + Key.S_BREAK_DURABILITY, durability);
                }
                if (!bukkitStage.getBlocksToDamage().isEmpty()) {
                    final LinkedList<String> names = new LinkedList<>();
                    final LinkedList<Integer> amounts = new LinkedList<>();
                    final LinkedList<Short> durability = new LinkedList<>();
                    for (final BlockItemStack e : bukkitStage.getBlocksToDamage()) {
                        names.add(e.getType().name());
                        amounts.add(e.getAmount());
                        durability.add(e.getDurability());
                    }
                    SessionData.set(uuid, pref + Key.S_DAMAGE_NAMES, names);
                    SessionData.set(uuid, pref + Key.S_DAMAGE_AMOUNTS, amounts);
                    SessionData.set(uuid, pref + Key.S_DAMAGE_DURABILITY, durability);
                }
                if (!bukkitStage.getBlocksToPlace().isEmpty()) {
                    final LinkedList<String> names = new LinkedList<>();
                    final LinkedList<Integer> amounts = new LinkedList<>();
                    final LinkedList<Short> durability = new LinkedList<>();
                    for (final BlockItemStack e : bukkitStage.getBlocksToPlace()) {
                        names.add(e.getType().name());
                        amounts.add(e.getAmount());
                        durability.add(e.getDurability());
                    }
                    SessionData.set(uuid, pref + Key.S_PLACE_NAMES, names);
                    SessionData.set(uuid, pref + Key.S_PLACE_AMOUNTS, amounts);
                    SessionData.set(uuid, pref + Key.S_PLACE_DURABILITY, durability);
                }
                if (!bukkitStage.getBlocksToUse().isEmpty()) {
                    final LinkedList<String> names = new LinkedList<>();
                    final LinkedList<Integer> amounts = new LinkedList<>();
                    final LinkedList<Short> durability = new LinkedList<>();
                    for (final BlockItemStack e : bukkitStage.getBlocksToUse()) {
                        names.add(e.getType().name());
                        amounts.add(e.getAmount());
                        durability.add(e.getDurability());
                    }
                    SessionData.set(uuid, pref + Key.S_USE_NAMES, names);
                    SessionData.set(uuid, pref + Key.S_USE_AMOUNTS, amounts);
                    SessionData.set(uuid, pref + Key.S_USE_DURABILITY, durability);
                }
                if (!bukkitStage.getBlocksToCut().isEmpty()) {
                    final LinkedList<String> names = new LinkedList<>();
                    final LinkedList<Integer> amounts = new LinkedList<>();
                    final LinkedList<Short> durability = new LinkedList<>();
                    for (final BlockItemStack e : bukkitStage.getBlocksToCut()) {
                        names.add(e.getType().name());
                        amounts.add(e.getAmount());
                        durability.add(e.getDurability());
                    }
                    SessionData.set(uuid, pref + Key.S_CUT_NAMES, names);
                    SessionData.set(uuid, pref + Key.S_CUT_AMOUNTS, amounts);
                    SessionData.set(uuid, pref + Key.S_CUT_DURABILITY, durability);
                }
                if (!bukkitStage.getItemsToCraft().isEmpty()) {
                    final LinkedList<ItemStack> items = new LinkedList<>(bukkitStage.getItemsToCraft());
                    SessionData.set(uuid, pref + Key.S_CRAFT_ITEMS, items);
                }
                if (!bukkitStage.getItemsToSmelt().isEmpty()) {
                    final LinkedList<ItemStack> items = new LinkedList<>(bukkitStage.getItemsToSmelt());
                    SessionData.set(uuid, pref + Key.S_SMELT_ITEMS, items);
                }
                if (!bukkitStage.getItemsToEnchant().isEmpty()) {
                    final LinkedList<ItemStack> items = new LinkedList<>(bukkitStage.getItemsToEnchant());
                    SessionData.set(uuid, pref + Key.S_ENCHANT_ITEMS, items);
                }
                if (!bukkitStage.getItemsToBrew().isEmpty()) {
                    final LinkedList<ItemStack> items = new LinkedList<>(bukkitStage.getItemsToBrew());
                    SessionData.set(uuid, pref + Key.S_BREW_ITEMS, items);
                }
                if (!bukkitStage.getItemsToConsume().isEmpty()) {
                    final LinkedList<ItemStack> items = new LinkedList<>(bukkitStage.getItemsToConsume());
                    SessionData.set(uuid, pref + Key.S_CONSUME_ITEMS, items);
                }
                if (bukkitStage.getCowsToMilk() != null) {
                    SessionData.set(uuid, pref + Key.S_COW_MILK, bukkitStage.getCowsToMilk());
                }
                if (bukkitStage.getFishToCatch() != null) {
                    SessionData.set(uuid, pref + Key.S_FISH, bukkitStage.getFishToCatch());
                }
                if (bukkitStage.getPlayersToKill() != null) {
                    SessionData.set(uuid, pref + Key.S_PLAYER_KILL, bukkitStage.getPlayersToKill());
                }
                if (!bukkitStage.getItemsToDeliver().isEmpty()) {
                    final LinkedList<ItemStack> items = new LinkedList<>(bukkitStage.getItemsToDeliver());
                    final LinkedList<String> npcs = new LinkedList<>();
                    for (UUID u : bukkitStage.getItemDeliveryTargets()) {
                        npcs.add(u.toString());
                    }
                    SessionData.set(uuid, pref + Key.S_DELIVERY_ITEMS, items);
                    SessionData.set(uuid, pref + Key.S_DELIVERY_NPCS, npcs);
                    SessionData.set(uuid, pref + Key.S_DELIVERY_MESSAGES, bukkitStage.getDeliverMessages());
                }
                if (!bukkitStage.getNpcsToInteract().isEmpty()) {
                    final LinkedList<String> npcs = new LinkedList<>();
                    for (UUID u : bukkitStage.getNpcsToInteract()) {
                        npcs.add(u.toString());
                    }
                    SessionData.set(uuid, pref + Key.S_NPCS_TO_TALK_TO, npcs);
                }
                if (!bukkitStage.getNpcsToKill().isEmpty()) {
                    final LinkedList<String> npcs = new LinkedList<>();
                    for (UUID u : bukkitStage.getNpcsToKill()) {
                        npcs.add(u.toString());
                    }
                    SessionData.set(uuid, pref + Key.S_NPCS_TO_KILL, npcs);
                    SessionData.set(uuid, pref + Key.S_NPCS_TO_KILL_AMOUNTS, bukkitStage.getNpcNumToKill());
                }
                if (!bukkitStage.getMobsToKill().isEmpty()) {
                    final LinkedList<String> mobs = new LinkedList<>();
                    for (final EntityType et : bukkitStage.getMobsToKill()) {
                        mobs.add(BukkitMiscUtil.getPrettyMobName(et));
                    }
                    SessionData.set(uuid, pref + Key.S_MOB_TYPES, mobs);
                    SessionData.set(uuid, pref + Key.S_MOB_AMOUNTS, bukkitStage.getMobNumToKill());
                    if (!bukkitStage.getLocationsToKillWithin().isEmpty()) {
                        final LinkedList<String> locations = new LinkedList<>();
                        for (final Location l : bukkitStage.getLocationsToKillWithin()) {
                            locations.add(BukkitConfigUtil.getLocationInfo(l));
                        }
                        SessionData.set(uuid, pref + Key.S_MOB_KILL_LOCATIONS, locations);
                        SessionData.set(uuid, pref + Key.S_MOB_KILL_LOCATIONS_RADIUS, bukkitStage.getRadiiToKillWithin());
                        SessionData.set(uuid, pref + Key.S_MOB_KILL_LOCATIONS_NAMES, bukkitStage.getKillNames());
                    }
                }
                if (!bukkitStage.getLocationsToReach().isEmpty()) {
                    final LinkedList<String> locations = new LinkedList<>();
                    for (final Location l : bukkitStage.getLocationsToReach()) {
                        locations.add(BukkitConfigUtil.getLocationInfo(l));
                    }
                    SessionData.set(uuid, pref + Key.S_REACH_LOCATIONS, locations);
                    SessionData.set(uuid, pref + Key.S_REACH_LOCATIONS_RADIUS, bukkitStage.getRadiiToReachWithin());
                    SessionData.set(uuid, pref + Key.S_REACH_LOCATIONS_NAMES, bukkitStage.getLocationNames());
                }
                if (!bukkitStage.getMobsToTame().isEmpty()) {
                    final LinkedList<String> mobs = new LinkedList<>();
                    for (final EntityType e : bukkitStage.getMobsToTame()) {
                        mobs.add(BukkitMiscUtil.getPrettyMobName(e));
                    }
                    final LinkedList<Integer> amounts = new LinkedList<>(bukkitStage.getMobNumToTame());
                    SessionData.set(uuid, pref + Key.S_TAME_TYPES, mobs);
                    SessionData.set(uuid, pref + Key.S_TAME_AMOUNTS, amounts);
                }
                if (!bukkitStage.getSheepToShear().isEmpty()) {
                    final LinkedList<String> colors = new LinkedList<>();
                    for (final DyeColor d : bukkitStage.getSheepToShear()) {
                        colors.add(BukkitMiscUtil.getPrettyDyeColorName(d));

                    }
                    final LinkedList<Integer> amounts = new LinkedList<>(bukkitStage.getSheepNumToShear());
                    SessionData.set(uuid, pref + Key.S_SHEAR_COLORS, colors);
                    SessionData.set(uuid, pref + Key.S_SHEAR_AMOUNTS, amounts);
                }
                if (!bukkitStage.getPasswordDisplays().isEmpty()) {
                    SessionData.set(uuid, pref + Key.S_PASSWORD_DISPLAYS, bukkitStage.getPasswordDisplays());
                    SessionData.set(uuid, pref + Key.S_PASSWORD_PHRASES, bukkitStage.getPasswordPhrases());
                }
                if (!bukkitStage.getCustomObjectives().isEmpty()) {
                    final LinkedList<String> list = new LinkedList<>();
                    final LinkedList<Integer> countList = new LinkedList<>();
                    for (int i = 0; i < bukkitStage.getCustomObjectives().size(); i++) {
                        list.add(bukkitStage.getCustomObjectives().get(i).getName());
                        countList.add(bukkitStage.getCustomObjectiveCounts().get(i));
                    }
                    final LinkedList<Entry<String, Object>> dataMapList = new LinkedList<>(bukkitStage.getCustomObjectiveData());
                    SessionData.set(uuid, pref + Key.S_CUSTOM_OBJECTIVES, list);
                    SessionData.set(uuid, pref + Key.S_CUSTOM_OBJECTIVES_COUNT, countList);
                    SessionData.set(uuid, pref + Key.S_CUSTOM_OBJECTIVES_DATA, dataMapList);
                }
                if (bukkitStage.getStartAction() != null) {
                    SessionData.set(uuid, pref + Key.S_START_EVENT, bukkitStage.getStartAction().getName());
                }
                if (bukkitStage.getFinishAction() != null) {
                    SessionData.set(uuid, pref + Key.S_FINISH_EVENT, bukkitStage.getFinishAction().getName());
                }
                if (bukkitStage.getFailAction() != null) {
                    SessionData.set(uuid, pref + Key.S_FAIL_EVENT, bukkitStage.getFailAction().getName());
                }
                if (bukkitStage.getDeathAction() != null) {
                    SessionData.set(uuid, pref + Key.S_DEATH_EVENT, bukkitStage.getDeathAction().getName());
                }
                if (bukkitStage.getDisconnectAction() != null) {
                    SessionData.set(uuid, pref + Key.S_DISCONNECT_EVENT, bukkitStage.getDisconnectAction().getName());
                }
                if (!bukkitStage.getChatActions().isEmpty()) {
                    final LinkedList<String> chatEvents = new LinkedList<>();
                    final LinkedList<String> chatEventTriggers = new LinkedList<>();
                    for (final String s : bukkitStage.getChatActions().keySet()) {
                        chatEventTriggers.add(s);
                        chatEvents.add(bukkitStage.getChatActions().get(s).getName());
                    }
                    SessionData.set(uuid, pref + Key.S_CHAT_EVENTS, chatEvents);
                    SessionData.set(uuid, pref + Key.S_CHAT_EVENT_TRIGGERS, chatEventTriggers);
                }
                if (!bukkitStage.getCommandActions().isEmpty()) {
                    final LinkedList<String> commandEvents = new LinkedList<>();
                    final LinkedList<String> commandEventTriggers = new LinkedList<>();
                    for (final String s : bukkitStage.getCommandActions().keySet()) {
                        commandEventTriggers.add(s);
                        commandEvents.add(bukkitStage.getCommandActions().get(s).getName());
                    }
                    SessionData.set(uuid, pref + Key.S_COMMAND_EVENTS, commandEvents);
                    SessionData.set(uuid, pref + Key.S_COMMAND_EVENT_TRIGGERS, commandEventTriggers);
                }
                if (bukkitStage.getCondition() != null) {
                    SessionData.set(uuid, pref + Key.S_CONDITION, bukkitStage.getCondition().getName());
                }
                if (bukkitStage.getDelay() != -1) {
                    SessionData.set(uuid, pref + Key.S_DELAY, bukkitStage.getDelay());
                    if (bukkitStage.getDelayMessage() != null) {
                        SessionData.set(uuid, pref + Key.S_DELAY_MESSAGE, bukkitStage.getDelayMessage());
                    }
                }
                if (bukkitStage.getScript() != null) {
                    SessionData.set(uuid, pref + Key.S_DENIZEN, bukkitStage.getScript());
                }
                if (bukkitStage.getCompleteMessage() != null) {
                    SessionData.set(uuid, pref + Key.S_COMPLETE_MESSAGE, bukkitStage.getCompleteMessage());
                }
                if (bukkitStage.getStartMessage() != null) {
                    SessionData.set(uuid, pref + Key.S_START_MESSAGE, bukkitStage.getStartMessage());
                }
                if (!bukkitStage.getObjectiveOverrides().isEmpty()) {
                    SessionData.set(uuid, pref + Key.S_OVERRIDE_DISPLAY, bukkitStage.getObjectiveOverrides());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearData(final UUID uuid) {
        SessionData.remove(uuid);
    }

    public void deleteQuest(final UUID uuid) {
        final FileConfiguration data = new YamlConfiguration();
        final File questsFile = new File(plugin.getDataFolder(), "storage" + File.separatorChar + "quests.yml");
        final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
        try {
            data.load(questsFile);
        } catch (final IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            sender.sendMessage(ChatColor.RED + BukkitLang.get("questErrorReadingFile")
                    .replace("<quest>", questsFile.getName()));
            return;
        }
        final String delete = (String) SessionData.get(uuid, Key.ED_QUEST_DELETE);
        if (delete != null && plugin.getQuest(delete) != null) {
            final ConfigurationSection sec = data.getConfigurationSection("quests");
            if (sec != null) {
                for (final String key : sec.getKeys(false)) {
                    final String name = sec.getString(key + ".name");
                    if (name != null && plugin.getQuest(name) != null) {
                        if (plugin.getQuest(name).getId().equals(plugin.getQuest(delete).getId())) {
                            sec.set(key, null);
                            sender.sendMessage(ChatColor.GREEN + BukkitLang.get("questDeleted"));
                            if (plugin.getConfigSettings().getConsoleLogging() > 0) {
                                final String identifier = sender instanceof Player ? "Player " + uuid : "CONSOLE";
                                plugin.getLogger().info(identifier + " deleted quest " + delete);
                            }
                            break;
                        }
                    }
                }
            }
        }

        try {
            data.save(questsFile);
        } catch (final IOException e) {
            sender.sendMessage(ChatColor.RED + BukkitLang.get("questSaveError"));
            return;
        }
        final ReloadCallback<Boolean> callback = response -> {
            if (!response) {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("unknownError"));
            }
        };
        plugin.reload(callback);
    }

    public void saveQuest(final UUID uuid, final ConfigurationSection section) {
        final String edit = (String) SessionData.get(uuid, Key.ED_QUEST_EDIT);
        if (edit != null && plugin.getQuest(edit) != null) {
            final ConfigurationSection questList = section.getParent();
            if (questList != null) {
                for (final String key : questList.getKeys(false)) {
                    final String name = questList.getString(key + ".name");
                    if (name != null && plugin.getQuest(name) != null) {
                        if (plugin.getQuest(name).getId().equals(plugin.getQuest(edit).getId())) {
                            questList.set(key, null);
                            break;
                        }
                    }
                }
            }
        }
        section.set("name", SessionData.get(uuid, Key.Q_NAME) != null
                ? SessionData.get(uuid, Key.Q_NAME) : null);
        section.set("ask-message", SessionData.get(uuid, Key.Q_ASK_MESSAGE) != null
                ? SessionData.get(uuid, Key.Q_ASK_MESSAGE) : null);
        section.set("finish-message", SessionData.get(uuid, Key.Q_FINISH_MESSAGE) != null
                ? SessionData.get(uuid, Key.Q_FINISH_MESSAGE) : null);
        section.set("npc-giver-uuid", SessionData.get(uuid, Key.Q_START_NPC) != null
                ? SessionData.get(uuid, Key.Q_START_NPC) : null);
        section.set("block-start", SessionData.get(uuid, Key.Q_START_BLOCK) != null
                ? BukkitConfigUtil.getLocationInfo((Location) Objects.requireNonNull(SessionData
                .get(uuid, Key.Q_START_BLOCK))) : null);
        section.set("event", SessionData.get(uuid, Key.Q_INITIAL_EVENT) != null
                ? SessionData.get(uuid, Key.Q_INITIAL_EVENT) : null);
        section.set("region", SessionData.get(uuid, Key.Q_REGION) != null
                ? SessionData.get(uuid, Key.Q_REGION) : null);
        section.set("gui-display", SessionData.get(uuid, Key.Q_GUIDISPLAY) != null
                ? SessionData.get(uuid, Key.Q_GUIDISPLAY) : null);
        saveRequirements(uuid, section);
        saveStages(uuid, section);
        saveRewards(uuid, section);
        savePlanner(uuid, section);
        saveOptions(uuid, section);
        if (plugin.getConfigSettings().getConsoleLogging() > 0) {
            final String identifier =  BukkitMiscUtil.getEntity(uuid) instanceof Player ? "Player " + uuid : "CONSOLE";
            plugin.getLogger().info(identifier + " saved quest " + SessionData.get(uuid, Key.Q_NAME));
        }
        SessionData.remove(uuid);
    }

    @SuppressWarnings("unchecked")
    private void saveRequirements(final UUID uuid, final ConfigurationSection section) {
        final ConfigurationSection requirements = section.createSection("requirements");
        requirements.set("money", SessionData.get(uuid, Key.REQ_MONEY) != null
                ? SessionData.get(uuid, Key.REQ_MONEY) : null);
        requirements.set("quest-points", SessionData.get(uuid, Key.REQ_QUEST_POINTS) != null
                ? SessionData.get(uuid, Key.REQ_QUEST_POINTS) : null);
        requirements.set("exp", SessionData.get(uuid, Key.REQ_EXP) != null
                ? SessionData.get(uuid, Key.REQ_EXP) : null);
        requirements.set("items", SessionData.get(uuid, Key.REQ_ITEMS) != null
                ? SessionData.get(uuid, Key.REQ_ITEMS) : null);
        requirements.set("remove-items", SessionData.get(uuid, Key.REQ_ITEMS_REMOVE) != null
                ? SessionData.get(uuid, Key.REQ_ITEMS_REMOVE) : null);
        requirements.set("permissions", SessionData.get(uuid, Key.REQ_PERMISSION) != null
                ? SessionData.get(uuid, Key.REQ_PERMISSION) : null);
        requirements.set("quests", SessionData.get(uuid, Key.REQ_QUEST) != null
                ? SessionData.get(uuid, Key.REQ_QUEST) : null);
        requirements.set("quest-blocks", SessionData.get(uuid, Key.REQ_QUEST_BLOCK) != null
                ? SessionData.get(uuid, Key.REQ_QUEST_BLOCK) : null);
        requirements.set("mcmmo-skills", SessionData.get(uuid, Key.REQ_MCMMO_SKILLS) != null
                ? SessionData.get(uuid, Key.REQ_MCMMO_SKILLS) : null);
        requirements.set("mcmmo-amounts", SessionData.get(uuid, Key.REQ_MCMMO_SKILL_AMOUNTS) != null
                ? SessionData.get(uuid, Key.REQ_MCMMO_SKILL_AMOUNTS) : null);
        requirements.set("heroes-primary-class", SessionData.get(uuid, Key.REQ_HEROES_PRIMARY_CLASS) != null
                ? SessionData.get(uuid, Key.REQ_HEROES_PRIMARY_CLASS) : null);
        requirements.set("heroes-secondary-class", SessionData.get(uuid, Key.REQ_HEROES_SECONDARY_CLASS) != null
                ? SessionData.get(uuid, Key.REQ_HEROES_SECONDARY_CLASS) : null);
        final LinkedList<String> customRequirements = SessionData.get(uuid, Key.REQ_CUSTOM) != null
                ? (LinkedList<String>) SessionData.get(uuid, Key.REQ_CUSTOM) : null;
        final LinkedList<Map<String, Object>> customRequirementsData = SessionData.get(uuid, Key.REQ_CUSTOM_DATA) != null
                ? (LinkedList<Map<String, Object>>) SessionData.get(uuid, Key.REQ_CUSTOM_DATA) : null;
        if (customRequirements != null && customRequirementsData != null) {
            final ConfigurationSection customRequirementsSec = requirements.createSection("custom-requirements");
            for (int i = 0; i < customRequirements.size(); i++) {
                final ConfigurationSection customReqSec = customRequirementsSec.createSection("req" + (i + 1));
                customReqSec.set("name", customRequirements.get(i));
                customReqSec.set("data", customRequirementsData.get(i));
            }
        }
        requirements.set("fail-requirement-message", SessionData.get(uuid, Key.REQ_FAIL_MESSAGE) != null
                ? SessionData.get(uuid, Key.REQ_FAIL_MESSAGE) : null);
        if (requirements.getKeys(false).isEmpty()) {
            section.set("requirements", null);
        }
    }

    @SuppressWarnings("unchecked")
    private void saveStages(final UUID uuid, final ConfigurationSection section) {
        final ConfigurationSection stages = section.createSection("stages");
        final ConfigurationSection ordered = stages.createSection("ordered");
        String pref;
        for (int i = 1; i <= new QuestStageMenuPrompt(uuid).getStages(); i++) {
            pref = "stage" + i;
            final ConfigurationSection stage = ordered.createSection("" + i);
            stage.set("break-block-names", SessionData.get(uuid, pref + Key.S_BREAK_NAMES) != null
                    ? SessionData.get(uuid, pref + Key.S_BREAK_NAMES) : null);
            stage.set("break-block-amounts", SessionData.get(uuid, pref + Key.S_BREAK_AMOUNTS) != null
                    ? SessionData.get(uuid, pref + Key.S_BREAK_AMOUNTS) : null);
            stage.set("break-block-durability", SessionData.get(uuid, pref + Key.S_BREAK_DURABILITY) != null
                    ? SessionData.get(uuid, pref + Key.S_BREAK_DURABILITY) : null);
            stage.set("damage-block-names", SessionData.get(uuid, pref + Key.S_DAMAGE_NAMES) != null
                    ? SessionData.get(uuid, pref + Key.S_DAMAGE_NAMES) : null);
            stage.set("damage-block-amounts", SessionData.get(uuid, pref + Key.S_DAMAGE_AMOUNTS) != null
                    ? SessionData.get(uuid, pref + Key.S_DAMAGE_AMOUNTS) : null);
            stage.set("damage-block-durability", SessionData.get(uuid, pref + Key.S_DAMAGE_DURABILITY) != null
                    ? SessionData.get(uuid, pref + Key.S_DAMAGE_DURABILITY) : null);
            stage.set("place-block-names", SessionData.get(uuid, pref + Key.S_PLACE_NAMES) != null
                    ? SessionData.get(uuid, pref + Key.S_PLACE_NAMES) : null);
            stage.set("place-block-amounts", SessionData.get(uuid, pref + Key.S_PLACE_AMOUNTS) != null
                    ? SessionData.get(uuid, pref + Key.S_PLACE_AMOUNTS) : null);
            stage.set("place-block-durability", SessionData.get(uuid, pref + Key.S_PLACE_DURABILITY) != null
                    ? SessionData.get(uuid, pref + Key.S_PLACE_DURABILITY) : null);
            stage.set("use-block-names", SessionData.get(uuid, pref + Key.S_USE_NAMES) != null
                    ? SessionData.get(uuid, pref + Key.S_USE_NAMES) : null);
            stage.set("use-block-amounts", SessionData.get(uuid, pref + Key.S_USE_AMOUNTS) != null
                    ? SessionData.get(uuid, pref + Key.S_USE_AMOUNTS) : null);
            stage.set("use-block-durability", SessionData.get(uuid, pref + Key.S_USE_DURABILITY) != null
                    ? SessionData.get(uuid, pref + Key.S_USE_DURABILITY) : null);
            stage.set("cut-block-names", SessionData.get(uuid, pref + Key.S_CUT_NAMES) != null
                    ? SessionData.get(uuid, pref + Key.S_CUT_NAMES) : null);
            stage.set("cut-block-amounts", SessionData.get(uuid, pref + Key.S_CUT_AMOUNTS) != null
                    ? SessionData.get(uuid, pref + Key.S_CUT_AMOUNTS) : null);
            stage.set("cut-block-durability", SessionData.get(uuid, pref + Key.S_CUT_DURABILITY) != null
                    ? SessionData.get(uuid, pref + Key.S_CUT_DURABILITY) : null);
            stage.set("items-to-craft", SessionData.get(uuid, pref + Key.S_CRAFT_ITEMS) != null
                    ? SessionData.get(uuid, pref + Key.S_CRAFT_ITEMS) : null);
            stage.set("items-to-smelt", SessionData.get(uuid, pref + Key.S_SMELT_ITEMS) != null
                    ? SessionData.get(uuid, pref + Key.S_SMELT_ITEMS) : null);
            stage.set("items-to-enchant", SessionData.get(uuid, pref + Key.S_ENCHANT_ITEMS) != null
                    ? SessionData.get(uuid, pref + Key.S_ENCHANT_ITEMS) : null);
            stage.set("items-to-brew", SessionData.get(uuid, pref + Key.S_BREW_ITEMS) != null
                    ? SessionData.get(uuid, pref + Key.S_BREW_ITEMS) : null);
            stage.set("items-to-consume", SessionData.get(uuid, pref + Key.S_CONSUME_ITEMS) != null
                    ? SessionData.get(uuid, pref + Key.S_CONSUME_ITEMS) : null);
            stage.set("cows-to-milk", SessionData.get(uuid, pref + Key.S_COW_MILK) != null
                    ? SessionData.get(uuid, pref + Key.S_COW_MILK) : null);
            stage.set("fish-to-catch", SessionData.get(uuid, pref + Key.S_FISH) != null
                    ? SessionData.get(uuid, pref + Key.S_FISH) : null);
            stage.set("players-to-kill", SessionData.get(uuid, pref + Key.S_PLAYER_KILL) != null
                    ? SessionData.get(uuid, pref + Key.S_PLAYER_KILL) : null);
            stage.set("items-to-deliver", SessionData.get(uuid, pref + Key.S_DELIVERY_ITEMS) != null
                    ? SessionData.get(uuid, pref + Key.S_DELIVERY_ITEMS) : null);
            stage.set("npc-delivery-uuids", SessionData.get(uuid, pref + Key.S_DELIVERY_NPCS) != null
                    ? SessionData.get(uuid, pref + Key.S_DELIVERY_NPCS) : null);
            stage.set("delivery-messages", SessionData.get(uuid, pref + Key.S_DELIVERY_MESSAGES) != null
                    ? SessionData.get(uuid, pref + Key.S_DELIVERY_MESSAGES) : null);
            stage.set("npc-uuids-to-talk-to", SessionData.get(uuid, pref + Key.S_NPCS_TO_TALK_TO) != null
                    ? SessionData.get(uuid, pref + Key.S_NPCS_TO_TALK_TO) : null);
            stage.set("npc-uuids-to-kill", SessionData.get(uuid, pref + Key.S_NPCS_TO_KILL) != null
                    ? SessionData.get(uuid, pref + Key.S_NPCS_TO_KILL) : null);
            stage.set("npc-kill-amounts", SessionData.get(uuid, pref + Key.S_NPCS_TO_KILL_AMOUNTS) != null
                    ? SessionData.get(uuid, pref + Key.S_NPCS_TO_KILL_AMOUNTS) : null);
            stage.set("mobs-to-kill", SessionData.get(uuid, pref + Key.S_MOB_TYPES) != null
                    ? SessionData.get(uuid, pref + Key.S_MOB_TYPES) : null);
            stage.set("mob-amounts", SessionData.get(uuid, pref + Key.S_MOB_AMOUNTS) != null
                    ? SessionData.get(uuid, pref + Key.S_MOB_AMOUNTS) : null);
            stage.set("locations-to-kill", SessionData.get(uuid, pref + Key.S_MOB_KILL_LOCATIONS) != null
                    ? SessionData.get(uuid, pref + Key.S_MOB_KILL_LOCATIONS) : null);
            stage.set("kill-location-radii", SessionData.get(uuid, pref + Key.S_MOB_KILL_LOCATIONS_RADIUS) != null
                    ? SessionData.get(uuid, pref + Key.S_MOB_KILL_LOCATIONS_RADIUS) : null);
            stage.set("kill-location-names", SessionData.get(uuid, pref + Key.S_MOB_KILL_LOCATIONS_NAMES) != null
                    ? SessionData.get(uuid, pref + Key.S_MOB_KILL_LOCATIONS_NAMES) : null);
            stage.set("locations-to-reach", SessionData.get(uuid, pref + Key.S_REACH_LOCATIONS) != null
                    ? SessionData.get(uuid, pref + Key.S_REACH_LOCATIONS) : null);
            stage.set("reach-location-radii", SessionData.get(uuid, pref + Key.S_REACH_LOCATIONS_RADIUS) != null
                    ? SessionData.get(uuid, pref + Key.S_REACH_LOCATIONS_RADIUS) : null);
            stage.set("reach-location-names", SessionData.get(uuid, pref + Key.S_REACH_LOCATIONS_NAMES) != null
                    ? SessionData.get(uuid, pref + Key.S_REACH_LOCATIONS_NAMES) : null);
            stage.set("mobs-to-tame", SessionData.get(uuid, pref + Key.S_TAME_TYPES) != null
                    ? SessionData.get(uuid, pref + Key.S_TAME_TYPES) : null);
            stage.set("mob-tame-amounts", SessionData.get(uuid, pref + Key.S_TAME_AMOUNTS) != null
                    ? SessionData.get(uuid, pref + Key.S_TAME_AMOUNTS) : null);
            stage.set("sheep-to-shear", SessionData.get(uuid, pref + Key.S_SHEAR_COLORS) != null
                    ? SessionData.get(uuid, pref + Key.S_SHEAR_COLORS) : null);
            stage.set("sheep-amounts", SessionData.get(uuid, pref + Key.S_SHEAR_AMOUNTS) != null
                    ? SessionData.get(uuid, pref + Key.S_SHEAR_AMOUNTS) : null);
            stage.set("password-displays", SessionData.get(uuid, pref + Key.S_PASSWORD_DISPLAYS) != null
                    ? SessionData.get(uuid, pref + Key.S_PASSWORD_DISPLAYS) : null);
            stage.set("password-phrases", SessionData.get(uuid, pref + Key.S_PASSWORD_PHRASES) != null
                    ? SessionData.get(uuid, pref + Key.S_PASSWORD_PHRASES) : null);
            final LinkedList<String> customObj = (LinkedList<String>) SessionData.get(uuid, pref + Key.S_CUSTOM_OBJECTIVES);
            final LinkedList<Integer> customObjCounts
                    = (LinkedList<Integer>) SessionData.get(uuid, pref + Key.S_CUSTOM_OBJECTIVES_COUNT);
            final LinkedList<Entry<String, Object>> customObjData
                    = (LinkedList<Entry<String, Object>>) SessionData.get(uuid, pref + Key.S_CUSTOM_OBJECTIVES_DATA);
            if (SessionData.get(uuid, pref + Key.S_CUSTOM_OBJECTIVES) != null) {
                final ConfigurationSection sec = stage.createSection("custom-objectives");
                if (customObj == null || customObjCounts == null || customObjData == null) {
                    continue;
                }
                for (int index = 0; index < customObj.size(); index++) {
                    final ConfigurationSection sec2 = sec.createSection("custom" + (index + 1));
                    sec2.set("name", customObj.get(index));
                    sec2.set("count", customObjCounts.get(index));
                    CustomObjective found = null;
                    for (final CustomObjective co : plugin.getCustomObjectives()) {
                        if (co.getName().equals(customObj.get(index))) {
                            found = co;
                            break;
                        }
                    }
                    if (found == null) {
                        continue;
                    }
                    final ConfigurationSection sec3 = sec2.createSection("data");
                    for (final Entry<String, Object> dataMap : found.getData()) {
                        for (final Entry<String, Object> e : customObjData) {
                            if (e.getKey().equals(dataMap.getKey())) {
                                sec3.set(e.getKey(), e.getValue()); // if anything goes wrong it's probably here
                            }
                        }
                    }
                }
            }
            stage.set("script-to-run", SessionData.get(uuid, pref + Key.S_DENIZEN) != null
                    ? SessionData.get(uuid, pref + Key.S_DENIZEN) : null);
            stage.set("start-event", SessionData.get(uuid, pref + Key.S_START_EVENT) != null
                    ? SessionData.get(uuid, pref + Key.S_START_EVENT) : null);
            stage.set("finish-event", SessionData.get(uuid, pref + Key.S_FINISH_EVENT) != null
                    ? SessionData.get(uuid, pref + Key.S_FINISH_EVENT) : null);
            stage.set("fail-event", SessionData.get(uuid, pref + Key.S_FAIL_EVENT) != null
                    ? SessionData.get(uuid, pref + Key.S_FAIL_EVENT) : null);
            stage.set("death-event", SessionData.get(uuid, pref + Key.S_DEATH_EVENT) != null
                    ? SessionData.get(uuid, pref + Key.S_DEATH_EVENT) : null);
            stage.set("disconnect-event", SessionData.get(uuid, pref + Key.S_DISCONNECT_EVENT) != null
                    ? SessionData.get(uuid, pref + Key.S_DISCONNECT_EVENT) : null);
            stage.set("chat-events", SessionData.get(uuid, pref + Key.S_CHAT_EVENTS) != null
                    ? SessionData.get(uuid, pref + Key.S_CHAT_EVENTS) : null);
            stage.set("chat-event-triggers", SessionData.get(uuid, pref + Key.S_CHAT_EVENT_TRIGGERS) != null
                    ? SessionData.get(uuid, pref + Key.S_CHAT_EVENT_TRIGGERS) : null);
            stage.set("command-events", SessionData.get(uuid, pref + Key.S_COMMAND_EVENTS) != null
                    ? SessionData.get(uuid, pref + Key.S_COMMAND_EVENTS) : null);
            stage.set("command-event-triggers", SessionData.get(uuid, pref + Key.S_COMMAND_EVENT_TRIGGERS) != null
                    ? SessionData.get(uuid, pref + Key.S_COMMAND_EVENT_TRIGGERS) : null);
            stage.set("condition", SessionData.get(uuid, pref + Key.S_CONDITION) != null
                    ? SessionData.get(uuid, pref + Key.S_CONDITION) : null);
            final Long delay = (Long) SessionData.get(uuid, pref + Key.S_DELAY);
            if (delay != null) {
                stage.set("delay", delay.intValue() / 1000);
            }
            final String delayMessage = (String) SessionData.get(uuid, pref + Key.S_DELAY_MESSAGE);
            if (delayMessage != null) {
                stage.set("delay-message", delayMessage.replace("\\n", "\n"));
            }
            final String startMessage = (String) SessionData.get(uuid, pref + Key.S_START_MESSAGE);
            if (startMessage != null) {
                stage.set("start-message", startMessage.replace("\\n", "\n"));
            }
            final String completeMessage = (String) SessionData.get(uuid, pref + Key.S_COMPLETE_MESSAGE);
            if (completeMessage != null) {
                stage.set("complete-message", completeMessage.replace("\\n", "\n"));
            }
            stage.set("objective-override", SessionData.get(uuid, pref + Key.S_OVERRIDE_DISPLAY) != null
                    ? SessionData.get(uuid, pref + Key.S_OVERRIDE_DISPLAY) : null);
        }
    }

    @SuppressWarnings("unchecked")
    private void saveRewards(final UUID uuid, final ConfigurationSection section) {
        final ConfigurationSection rewards = section.createSection("rewards");
        rewards.set("items", SessionData.get(uuid, Key.REW_ITEMS) != null
                ? SessionData.get(uuid, Key.REW_ITEMS) : null);
        rewards.set("money", SessionData.get(uuid, Key.REW_MONEY) != null
                ? SessionData.get(uuid, Key.REW_MONEY) : null);
        rewards.set("quest-points", SessionData.get(uuid, Key.REW_QUEST_POINTS) != null
                ? SessionData.get(uuid, Key.REW_QUEST_POINTS) : null);
        rewards.set("exp", SessionData.get(uuid, Key.REW_EXP) != null
                ? SessionData.get(uuid, Key.REW_EXP) : null);
        rewards.set("commands", SessionData.get(uuid, Key.REW_COMMAND) != null
                ? SessionData.get(uuid, Key.REW_COMMAND) : null);
        rewards.set("commands-override-display", SessionData.get(uuid, Key.REW_COMMAND_OVERRIDE_DISPLAY) != null
                ? SessionData.get(uuid, Key.REW_COMMAND_OVERRIDE_DISPLAY) : null);
        rewards.set("permissions", SessionData.get(uuid, Key.REW_PERMISSION) != null
                ? SessionData.get(uuid, Key.REW_PERMISSION) : null);
        rewards.set("permission-worlds", SessionData.get(uuid, Key.REW_PERMISSION_WORLDS) != null
                ? SessionData.get(uuid, Key.REW_PERMISSION_WORLDS) : null);
        rewards.set("mcmmo-skills", SessionData.get(uuid, Key.REW_MCMMO_SKILLS) != null
                ? SessionData.get(uuid, Key.REW_MCMMO_SKILLS) : null);
        rewards.set("mcmmo-levels", SessionData.get(uuid, Key.REW_MCMMO_AMOUNTS) != null
                ? SessionData.get(uuid, Key.REW_MCMMO_AMOUNTS) : null);
        rewards.set("heroes-exp-classes", SessionData.get(uuid, Key.REW_HEROES_CLASSES) != null
                ? SessionData.get(uuid, Key.REW_HEROES_CLASSES) : null);
        rewards.set("heroes-exp-amounts", SessionData.get(uuid, Key.REW_HEROES_AMOUNTS) != null
                ? SessionData.get(uuid, Key.REW_HEROES_AMOUNTS) : null);
        rewards.set("parties-experience", SessionData.get(uuid, Key.REW_PARTIES_EXPERIENCE) != null
                ? SessionData.get(uuid, Key.REW_PARTIES_EXPERIENCE) : null);
        rewards.set("phat-loots", SessionData.get(uuid, Key.REW_PHAT_LOOTS) != null
                ? SessionData.get(uuid, Key.REW_PHAT_LOOTS) : null);
        final LinkedList<String> customRewards = SessionData.get(uuid, Key.REW_CUSTOM) != null
                ? (LinkedList<String>) SessionData.get(uuid, Key.REW_CUSTOM) : null;
        final LinkedList<Map<String, Object>> customRewardsData = SessionData.get(uuid, Key.REW_CUSTOM_DATA) != null
                ? (LinkedList<Map<String, Object>>) SessionData.get(uuid, Key.REW_CUSTOM_DATA) : null;
        if (customRewards != null && customRewardsData != null) {
            final ConfigurationSection customRewardsSec = rewards.createSection("custom-rewards");
            for (int i = 0; i < customRewards.size(); i++) {
                final ConfigurationSection customRewSec = customRewardsSec.createSection("req" + (i + 1));
                customRewSec.set("name", customRewards.get(i));
                customRewSec.set("data", customRewardsData.get(i));
            }
        }
        rewards.set("details-override", SessionData.get(uuid, Key.REW_DETAILS_OVERRIDE) != null
                ? SessionData.get(uuid, Key.REW_DETAILS_OVERRIDE) : null);
        if (rewards.getKeys(false).isEmpty()) {
            section.set("rewards", null);
        }
    }

    private void savePlanner(final UUID uuid, final ConfigurationSection section) {
        final ConfigurationSection pln = section.createSection("planner");
        pln.set("start", SessionData.get(uuid, Key.PLN_START_DATE) != null
                ? SessionData.get(uuid, Key.PLN_START_DATE) : null);
        pln.set("end", SessionData.get(uuid, Key.PLN_END_DATE) != null
                ? SessionData.get(uuid, Key.PLN_END_DATE) : null);
        final Long repeatCycle = (Long) SessionData.get(uuid, Key.PLN_REPEAT_CYCLE);
        pln.set("repeat", repeatCycle != null ? (repeatCycle / 1000L) : null);
        final Long cooldown = (Long) SessionData.get(uuid, Key.PLN_COOLDOWN);
        pln.set("cooldown", cooldown != null ? (cooldown / 1000L) : null);
        pln.set("override", SessionData.get(uuid, Key.PLN_OVERRIDE) != null
                ? SessionData.get(uuid, Key.PLN_OVERRIDE) : null);
        if (pln.getKeys(false).isEmpty()) {
            section.set("planner", null);
        }
    }

    private void saveOptions(final UUID uuid, final ConfigurationSection section) {
        final ConfigurationSection opts = section.createSection("options");
        opts.set("allow-commands", SessionData.get(uuid, Key.OPT_ALLOW_COMMANDS) != null
                ? SessionData.get(uuid, Key.OPT_ALLOW_COMMANDS) : null);
        opts.set("allow-quitting", SessionData.get(uuid, Key.OPT_ALLOW_QUITTING) != null
                ? SessionData.get(uuid, Key.OPT_ALLOW_QUITTING) : null);
        opts.set("ignore-silk-touch", SessionData.get(uuid, Key.OPT_IGNORE_SILK_TOUCH) != null
                ? SessionData.get(uuid, Key.OPT_IGNORE_SILK_TOUCH) : null);
        opts.set("external-party-plugin", SessionData.get(uuid, Key.OPT_EXTERNAL_PARTY_PLUGIN) != null
                ? SessionData.get(uuid, Key.OPT_EXTERNAL_PARTY_PLUGIN) : null);
        opts.set("use-parties-plugin", SessionData.get(uuid, Key.OPT_USE_PARTIES_PLUGIN) != null
                ? SessionData.get(uuid, Key.OPT_USE_PARTIES_PLUGIN) : null);
        opts.set("share-progress-level", SessionData.get(uuid, Key.OPT_SHARE_PROGRESS_LEVEL) != null
                ? SessionData.get(uuid, Key.OPT_SHARE_PROGRESS_LEVEL) : null);
        opts.set("same-quest-only", SessionData.get(uuid, Key.OPT_SHARE_SAME_QUEST_ONLY) != null
                ? SessionData.get(uuid, Key.OPT_SHARE_SAME_QUEST_ONLY) : null);
        opts.set("share-distance", SessionData.get(uuid, Key.OPT_SHARE_DISTANCE) != null
                ? SessionData.get(uuid, Key.OPT_SHARE_DISTANCE) : null);
        opts.set("handle-offline-players", SessionData.get(uuid, Key.OPT_HANDLE_OFFLINE_PLAYERS) != null
                ? SessionData.get(uuid, Key.OPT_HANDLE_OFFLINE_PLAYERS) : null);
        opts.set("ignore-block-replace", SessionData.get(uuid, Key.OPT_IGNORE_BLOCK_REPLACE) != null
                ? SessionData.get(uuid, Key.OPT_IGNORE_BLOCK_REPLACE) : null);
        opts.set("give-at-login", SessionData.get(uuid, Key.OPT_GIVE_GLOBALLY_AT_LOGIN) != null
                ? SessionData.get(uuid, Key.OPT_GIVE_GLOBALLY_AT_LOGIN) : null);
        opts.set("allow-stacking-global", SessionData.get(uuid, Key.OPT_ALLOW_STACKING_GLOBAL) != null
                ? SessionData.get(uuid, Key.OPT_ALLOW_STACKING_GLOBAL) : null);
        opts.set("inform-on-start", SessionData.get(uuid, Key.OPT_INFORM_QUEST_START) != null
                ? SessionData.get(uuid, Key.OPT_INFORM_QUEST_START) : null);
        opts.set("override-max-quests", SessionData.get(uuid, Key.OPT_OVERRIDE_MAX_QUESTS) != null
                ? SessionData.get(uuid, Key.OPT_OVERRIDE_MAX_QUESTS) : null);
        if (opts.getKeys(false).isEmpty()) {
            section.set("options", null);
        }
    }
}