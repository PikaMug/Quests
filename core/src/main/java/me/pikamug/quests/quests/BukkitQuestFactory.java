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
import me.pikamug.quests.convo.quests.menu.QuestMenuPrompt;
import me.pikamug.quests.convo.quests.stages.QuestStageMenuPrompt;
import me.pikamug.quests.interfaces.ReloadCallback;
import me.pikamug.quests.module.CustomObjective;
import me.pikamug.quests.quests.components.BukkitStage;
import me.pikamug.quests.quests.components.Options;
import me.pikamug.quests.quests.components.Planner;
import me.pikamug.quests.quests.components.Requirements;
import me.pikamug.quests.quests.components.Rewards;
import me.pikamug.quests.quests.components.Stage;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.BukkitConfigUtil;
import me.pikamug.quests.util.BukkitFakeConversable;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.quests.util.BukkitMiscUtil;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.ConversationPrefix;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class BukkitQuestFactory implements QuestFactory, ConversationAbandonedListener {

    private final BukkitQuestsPlugin plugin;
    private final ConversationFactory conversationFactory;
    private Map<UUID, Block> selectedBlockStarts = new HashMap<>();
    private Map<UUID, Block> selectedKillLocations = new HashMap<>();
    private Map<UUID, Block> selectedReachLocations = new HashMap<>();
    private Set<UUID> selectingNpcs = new HashSet<>();
    private List<String> editingQuestNames = new LinkedList<>();

    public BukkitQuestFactory(final BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
        // Ensure to initialize factory last so that 'this' is fully initialized before it is passed
        this.conversationFactory = new ConversationFactory(plugin).withModality(false).withLocalEcho(false)
                .withFirstPrompt(new QuestMenuPrompt(new ConversationContext(plugin, new BukkitFakeConversable(),
                        new HashMap<>()))).withTimeout(3600)
                .withPrefix(new LineBreakPrefix()).addConversationAbandonedListener(this);
    }

    public static class LineBreakPrefix implements ConversationPrefix {
        @Override
        public @NotNull String getPrefix(final @NotNull ConversationContext context) {
            return "\n";
        }
    }

    public Map<UUID, Block> getSelectedBlockStarts() {
        return selectedBlockStarts;
    }

    public void setSelectedBlockStarts(final Map<UUID, Block> selectedBlockStarts) {
        this.selectedBlockStarts = selectedBlockStarts;
    }

    public Map<UUID, Block> getSelectedKillLocations() {
        return selectedKillLocations;
    }

    public void setSelectedKillLocations(final Map<UUID, Block> selectedKillLocations) {
        this.selectedKillLocations = selectedKillLocations;
    }

    public Map<UUID, Block> getSelectedReachLocations() {
        return selectedReachLocations;
    }

    public void setSelectedReachLocations(final Map<UUID, Block> selectedReachLocations) {
        this.selectedReachLocations = selectedReachLocations;
    }

    public Set<UUID> getSelectingNpcs() {
        return selectingNpcs;
    }

    public void setSelectingNpcs(final Set<UUID> selectingNpcs) {
        this.selectingNpcs = selectingNpcs;
    }

    public List<String> getNamesOfQuestsBeingEdited() {
        return editingQuestNames;
    }

    public void setNamesOfQuestsBeingEdited(final List<String> questNames) {
        this.editingQuestNames = questNames;
    }

    public ConversationFactory getConversationFactory() {
        return conversationFactory;
    }

    @Override
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
    }

    public Prompt returnToMenu(final ConversationContext context) {
        return new QuestMainPrompt(context);
    }

    @SuppressWarnings("deprecation")
    public void loadQuest(final ConversationContext context, final Quest quest) {
        BukkitQuest bukkitQuest = (BukkitQuest) quest;
        try {
            context.setSessionData(Key.ED_QUEST_EDIT, bukkitQuest.getName());
            context.setSessionData(Key.Q_ID, bukkitQuest.getId());
            context.setSessionData(Key.Q_NAME, bukkitQuest.getName());
            context.setSessionData(Key.Q_ASK_MESSAGE, bukkitQuest.getDescription());
            context.setSessionData(Key.Q_FINISH_MESSAGE, bukkitQuest.getFinished());
            if (plugin.getDependencies().getCitizens() != null) {
                if (bukkitQuest.getNpcStart() != null) {
                    context.setSessionData(Key.Q_START_NPC, bukkitQuest.getNpcStart().toString());
                }
            }
            context.setSessionData(Key.Q_START_BLOCK, bukkitQuest.getBlockStart());
            if (bukkitQuest.getInitialAction() != null) {
                context.setSessionData(Key.Q_INITIAL_EVENT, bukkitQuest.getInitialAction().getName());
            }
            if (bukkitQuest.getRegionStart() != null) {
                context.setSessionData(Key.Q_REGION, bukkitQuest.getRegionStart());
            }
            if (bukkitQuest.getGUIDisplay() != null) {
                context.setSessionData(Key.Q_GUIDISPLAY, bukkitQuest.getGUIDisplay());
            }
            final Requirements requirements = bukkitQuest.getRequirements();
            if (requirements.getMoney() != 0) {
                context.setSessionData(Key.REQ_MONEY, requirements.getMoney());
            }
            if (requirements.getQuestPoints() != 0) {
                context.setSessionData(Key.REQ_QUEST_POINTS, requirements.getQuestPoints());
            }
            if (requirements.getExp() != 0) {
                context.setSessionData(Key.REQ_EXP, requirements.getExp());
            }
            if (!requirements.getItems().isEmpty()) {
                context.setSessionData(Key.REQ_ITEMS, requirements.getItems());
                context.setSessionData(Key.REQ_ITEMS_REMOVE, requirements.getRemoveItems());
            }
            if (!requirements.getNeededQuestIds().isEmpty()) {
                context.setSessionData(Key.REQ_QUEST, requirements.getNeededQuestIds());
            }
            if (!requirements.getBlockQuestIds().isEmpty()) {
                context.setSessionData(Key.REQ_QUEST_BLOCK, requirements.getBlockQuestIds());
            }
            if (!requirements.getMcmmoSkills().isEmpty()) {
                context.setSessionData(Key.REQ_MCMMO_SKILLS, requirements.getMcmmoAmounts());
                context.setSessionData(Key.REQ_MCMMO_SKILL_AMOUNTS, requirements.getMcmmoAmounts());
            }
            if (!requirements.getPermissions().isEmpty()) {
                context.setSessionData(Key.REQ_PERMISSION, requirements.getPermissions());
            }
            if (requirements.getHeroesPrimaryClass() != null) {
                context.setSessionData(Key.REQ_HEROES_PRIMARY_CLASS, requirements.getHeroesPrimaryClass());
            }
            if (requirements.getHeroesSecondaryClass() != null) {
                context.setSessionData(Key.REQ_HEROES_SECONDARY_CLASS, requirements.getHeroesSecondaryClass());
            }
            if (!requirements.getCustomRequirements().isEmpty()) {
                final LinkedList<String> list = new LinkedList<>();
                final LinkedList<Map<String, Object>> dataMapList = new LinkedList<>();
                for (final Entry<String, Map<String, Object>> entry : requirements.getCustomRequirements().entrySet()) {
                    list.add(entry.getKey());
                    dataMapList.add(entry.getValue());
                }
                context.setSessionData(Key.REQ_CUSTOM, list);
                context.setSessionData(Key.REQ_CUSTOM_DATA, dataMapList);
            }
            if (!requirements.getDetailsOverride().isEmpty()) {
                context.setSessionData(Key.REQ_FAIL_MESSAGE, requirements.getDetailsOverride());
            }
            final Rewards rewards = bukkitQuest.getRewards();
            if (rewards.getMoney() != 0) {
                context.setSessionData(Key.REW_MONEY, rewards.getMoney());
            }
            if (rewards.getQuestPoints() != 0) {
                context.setSessionData(Key.REW_QUEST_POINTS, rewards.getQuestPoints());
            }
            if (rewards.getExp() != 0) {
                context.setSessionData(Key.REW_EXP, rewards.getExp());
            }
            if (!rewards.getItems().isEmpty()) {
                context.setSessionData(Key.REW_ITEMS, rewards.getItems());
            }
            if (!rewards.getCommands().isEmpty()) {
                context.setSessionData(Key.REW_COMMAND, rewards.getCommands());
            }
            if (!rewards.getCommandsOverrideDisplay().isEmpty()) {
                context.setSessionData(Key.REW_COMMAND_OVERRIDE_DISPLAY, rewards.getCommandsOverrideDisplay());
            }
            if (!rewards.getPermissions().isEmpty()) {
                context.setSessionData(Key.REW_PERMISSION, rewards.getPermissions());
            }
            if (!rewards.getPermissions().isEmpty()) {
                context.setSessionData(Key.REW_PERMISSION_WORLDS, rewards.getPermissionWorlds());
            }
            if (!rewards.getMcmmoSkills().isEmpty()) {
                context.setSessionData(Key.REW_MCMMO_SKILLS, rewards.getMcmmoSkills());
                context.setSessionData(Key.REW_MCMMO_AMOUNTS, rewards.getMcmmoAmounts());
            }
            if (!rewards.getHeroesClasses().isEmpty()) {
                context.setSessionData(Key.REW_HEROES_CLASSES, rewards.getHeroesClasses());
                context.setSessionData(Key.REW_HEROES_AMOUNTS, rewards.getHeroesAmounts());
            }
            if (rewards.getPartiesExperience() != 0) {
                context.setSessionData(Key.REW_PARTIES_EXPERIENCE, rewards.getPartiesExperience());
            }
            if (!rewards.getPhatLoots().isEmpty()) {
                context.setSessionData(Key.REW_PHAT_LOOTS, rewards.getPhatLoots());
            }
            if (!rewards.getCustomRewards().isEmpty()) {
                context.setSessionData(Key.REW_CUSTOM, new LinkedList<>(rewards.getCustomRewards().keySet()));
                context.setSessionData(Key.REW_CUSTOM_DATA, new LinkedList<Object>(rewards.getCustomRewards().values()));
            }
            if (!rewards.getDetailsOverride().isEmpty()) {
                context.setSessionData(Key.REW_DETAILS_OVERRIDE, rewards.getDetailsOverride());
            }
            final Planner pln = bukkitQuest.getPlanner();
            if (pln.getStart() != null) {
                context.setSessionData(Key.PLN_START_DATE, pln.getStart());
            }
            if (pln.getEnd() != null) {
                context.setSessionData(Key.PLN_END_DATE, pln.getEnd());
            }
            if (pln.getRepeat() != -1) {
                context.setSessionData(Key.PLN_REPEAT_CYCLE, pln.getRepeat());
            }
            if (pln.getCooldown() != -1) {
                context.setSessionData(Key.PLN_COOLDOWN, pln.getCooldown());
            }
            context.setSessionData(Key.PLN_OVERRIDE, pln.getOverride());
            final Options opt = bukkitQuest.getOptions();
            context.setSessionData(Key.OPT_ALLOW_COMMANDS, opt.canAllowCommands());
            context.setSessionData(Key.OPT_ALLOW_QUITTING, opt.canAllowQuitting());
            context.setSessionData(Key.OPT_IGNORE_SILK_TOUCH, opt.canIgnoreSilkTouch());
            context.setSessionData(Key.OPT_EXTERNAL_PARTY_PLUGIN, opt.getExternalPartyPlugin());
            context.setSessionData(Key.OPT_USE_PARTIES_PLUGIN, opt.canUsePartiesPlugin());
            context.setSessionData(Key.OPT_SHARE_PROGRESS_LEVEL, opt.getShareProgressLevel());
            context.setSessionData(Key.OPT_SHARE_SAME_QUEST_ONLY, opt.canShareSameQuestOnly());
            context.setSessionData(Key.OPT_SHARE_DISTANCE, opt.getShareDistance());
            context.setSessionData(Key.OPT_HANDLE_OFFLINE_PLAYERS, opt.canHandleOfflinePlayers());
            context.setSessionData(Key.OPT_IGNORE_BLOCK_REPLACE, opt.canIgnoreBlockReplace());
            // Stages (Objectives)
            int index = 1;
            for (final Stage stage : bukkitQuest.getStages()) {
                final BukkitStage bukkitStage = (BukkitStage) stage;
                final String pref = "stage" + index;
                index++;
                context.setSessionData(pref, Boolean.TRUE);
                if (!bukkitStage.getBlocksToBreak().isEmpty()) {
                    final LinkedList<String> names = new LinkedList<>();
                    final LinkedList<Integer> amounts = new LinkedList<>();
                    final LinkedList<Short> durability = new LinkedList<>();
                    for (final ItemStack e : bukkitStage.getBlocksToBreak()) {
                        names.add(e.getType().name());
                        amounts.add(e.getAmount());
                        durability.add(e.getDurability());
                    }
                    context.setSessionData(pref + Key.S_BREAK_NAMES, names);
                    context.setSessionData(pref + Key.S_BREAK_AMOUNTS, amounts);
                    context.setSessionData(pref + Key.S_BREAK_DURABILITY, durability);
                }
                if (!bukkitStage.getBlocksToDamage().isEmpty()) {
                    final LinkedList<String> names = new LinkedList<>();
                    final LinkedList<Integer> amounts = new LinkedList<>();
                    final LinkedList<Short> durability = new LinkedList<>();
                    for (final ItemStack e : bukkitStage.getBlocksToDamage()) {
                        names.add(e.getType().name());
                        amounts.add(e.getAmount());
                        durability.add(e.getDurability());
                    }
                    context.setSessionData(pref + Key.S_DAMAGE_NAMES, names);
                    context.setSessionData(pref + Key.S_DAMAGE_AMOUNTS, amounts);
                    context.setSessionData(pref + Key.S_DAMAGE_DURABILITY, durability);
                }
                if (!bukkitStage.getBlocksToPlace().isEmpty()) {
                    final LinkedList<String> names = new LinkedList<>();
                    final LinkedList<Integer> amounts = new LinkedList<>();
                    final LinkedList<Short> durability = new LinkedList<>();
                    for (final ItemStack e : bukkitStage.getBlocksToPlace()) {
                        names.add(e.getType().name());
                        amounts.add(e.getAmount());
                        durability.add(e.getDurability());
                    }
                    context.setSessionData(pref + Key.S_PLACE_NAMES, names);
                    context.setSessionData(pref + Key.S_PLACE_AMOUNTS, amounts);
                    context.setSessionData(pref + Key.S_PLACE_DURABILITY, durability);
                }
                if (!bukkitStage.getBlocksToUse().isEmpty()) {
                    final LinkedList<String> names = new LinkedList<>();
                    final LinkedList<Integer> amounts = new LinkedList<>();
                    final LinkedList<Short> durability = new LinkedList<>();
                    for (final ItemStack e : bukkitStage.getBlocksToUse()) {
                        names.add(e.getType().name());
                        amounts.add(e.getAmount());
                        durability.add(e.getDurability());
                    }
                    context.setSessionData(pref + Key.S_USE_NAMES, names);
                    context.setSessionData(pref + Key.S_USE_AMOUNTS, amounts);
                    context.setSessionData(pref + Key.S_USE_DURABILITY, durability);
                }
                if (!bukkitStage.getBlocksToCut().isEmpty()) {
                    final LinkedList<String> names = new LinkedList<>();
                    final LinkedList<Integer> amounts = new LinkedList<>();
                    final LinkedList<Short> durability = new LinkedList<>();
                    for (final ItemStack e : bukkitStage.getBlocksToCut()) {
                        names.add(e.getType().name());
                        amounts.add(e.getAmount());
                        durability.add(e.getDurability());
                    }
                    context.setSessionData(pref + Key.S_CUT_NAMES, names);
                    context.setSessionData(pref + Key.S_CUT_AMOUNTS, amounts);
                    context.setSessionData(pref + Key.S_CUT_DURABILITY, durability);
                }
                if (!bukkitStage.getItemsToCraft().isEmpty()) {
                    final LinkedList<ItemStack> items = new LinkedList<>(bukkitStage.getItemsToCraft());
                    context.setSessionData(pref + Key.S_CRAFT_ITEMS, items);
                }
                if (!bukkitStage.getItemsToSmelt().isEmpty()) {
                    final LinkedList<ItemStack> items = new LinkedList<>(bukkitStage.getItemsToSmelt());
                    context.setSessionData(pref + Key.S_SMELT_ITEMS, items);
                }
                if (!bukkitStage.getItemsToEnchant().isEmpty()) {
                    final LinkedList<ItemStack> items = new LinkedList<>(bukkitStage.getItemsToEnchant());
                    context.setSessionData(pref + Key.S_ENCHANT_ITEMS, items);
                }
                if (!bukkitStage.getItemsToBrew().isEmpty()) {
                    final LinkedList<ItemStack> items = new LinkedList<>(bukkitStage.getItemsToBrew());
                    context.setSessionData(pref + Key.S_BREW_ITEMS, items);
                }
                if (!bukkitStage.getItemsToConsume().isEmpty()) {
                    final LinkedList<ItemStack> items = new LinkedList<>(bukkitStage.getItemsToConsume());
                    context.setSessionData(pref + Key.S_CONSUME_ITEMS, items);
                }
                if (bukkitStage.getCowsToMilk() != null) {
                    context.setSessionData(pref + Key.S_COW_MILK, bukkitStage.getCowsToMilk());
                }
                if (bukkitStage.getFishToCatch() != null) {
                    context.setSessionData(pref + Key.S_FISH, bukkitStage.getFishToCatch());
                }
                if (bukkitStage.getPlayersToKill() != null) {
                    context.setSessionData(pref + Key.S_PLAYER_KILL, bukkitStage.getPlayersToKill());
                }
                if (!bukkitStage.getItemsToDeliver().isEmpty()) {
                    final LinkedList<ItemStack> items = new LinkedList<>(bukkitStage.getItemsToDeliver());
                    final LinkedList<String> npcs = new LinkedList<>();
                    for (UUID uuid : bukkitStage.getItemDeliveryTargets()) {
                        npcs.add(uuid.toString());
                    }
                    context.setSessionData(pref + Key.S_DELIVERY_ITEMS, items);
                    context.setSessionData(pref + Key.S_DELIVERY_NPCS, npcs);
                    context.setSessionData(pref + Key.S_DELIVERY_MESSAGES, bukkitStage.getDeliverMessages());
                }
                if (!bukkitStage.getNpcsToInteract().isEmpty()) {
                    final LinkedList<String> npcs = new LinkedList<>();
                    for (UUID uuid : bukkitStage.getNpcsToInteract()) {
                        npcs.add(uuid.toString());
                    }
                    context.setSessionData(pref + Key.S_NPCS_TO_TALK_TO, npcs);
                }
                if (!bukkitStage.getNpcsToKill().isEmpty()) {
                    final LinkedList<String> npcs = new LinkedList<>();
                    for (UUID uuid : bukkitStage.getNpcsToKill()) {
                        npcs.add(uuid.toString());
                    }
                    context.setSessionData(pref + Key.S_NPCS_TO_KILL, npcs);
                    context.setSessionData(pref + Key.S_NPCS_TO_KILL_AMOUNTS, bukkitStage.getNpcNumToKill());
                }
                if (!bukkitStage.getMobsToKill().isEmpty()) {
                    final LinkedList<String> mobs = new LinkedList<>();
                    for (final EntityType et : bukkitStage.getMobsToKill()) {
                        mobs.add(BukkitMiscUtil.getPrettyMobName(et));
                    }
                    context.setSessionData(pref + Key.S_MOB_TYPES, mobs);
                    context.setSessionData(pref + Key.S_MOB_AMOUNTS, bukkitStage.getMobNumToKill());
                    if (!bukkitStage.getLocationsToKillWithin().isEmpty()) {
                        final LinkedList<String> locations = new LinkedList<>();
                        for (final Location l : bukkitStage.getLocationsToKillWithin()) {
                            locations.add(BukkitConfigUtil.getLocationInfo(l));
                        }
                        context.setSessionData(pref + Key.S_MOB_KILL_LOCATIONS, locations);
                        context.setSessionData(pref + Key.S_MOB_KILL_LOCATIONS_RADIUS, bukkitStage.getRadiiToKillWithin());
                        context.setSessionData(pref + Key.S_MOB_KILL_LOCATIONS_NAMES, bukkitStage.getKillNames());
                    }
                }
                if (!bukkitStage.getLocationsToReach().isEmpty()) {
                    final LinkedList<String> locations = new LinkedList<>();
                    for (final Location l : bukkitStage.getLocationsToReach()) {
                        locations.add(BukkitConfigUtil.getLocationInfo(l));
                    }
                    context.setSessionData(pref + Key.S_REACH_LOCATIONS, locations);
                    context.setSessionData(pref + Key.S_REACH_LOCATIONS_RADIUS, bukkitStage.getRadiiToReachWithin());
                    context.setSessionData(pref + Key.S_REACH_LOCATIONS_NAMES, bukkitStage.getLocationNames());
                }
                if (!bukkitStage.getMobsToTame().isEmpty()) {
                    final LinkedList<String> mobs = new LinkedList<>();
                    for (final EntityType e : bukkitStage.getMobsToTame()) {
                        mobs.add(BukkitMiscUtil.getPrettyMobName(e));
                    }
                    final LinkedList<Integer> amounts = new LinkedList<>(bukkitStage.getMobNumToTame());
                    context.setSessionData(pref + Key.S_TAME_TYPES, mobs);
                    context.setSessionData(pref + Key.S_TAME_AMOUNTS, amounts);
                }
                if (!bukkitStage.getSheepToShear().isEmpty()) {
                    final LinkedList<String> colors = new LinkedList<>();
                    for (final DyeColor d : bukkitStage.getSheepToShear()) {
                        colors.add(BukkitMiscUtil.getPrettyDyeColorName(d));

                    }
                    final LinkedList<Integer> amounts = new LinkedList<>(bukkitStage.getSheepNumToShear());
                    context.setSessionData(pref + Key.S_SHEAR_COLORS, colors);
                    context.setSessionData(pref + Key.S_SHEAR_AMOUNTS, amounts);
                }
                if (!bukkitStage.getPasswordDisplays().isEmpty()) {
                    context.setSessionData(pref + Key.S_PASSWORD_DISPLAYS, bukkitStage.getPasswordDisplays());
                    context.setSessionData(pref + Key.S_PASSWORD_PHRASES, bukkitStage.getPasswordPhrases());
                }
                if (!bukkitStage.getCustomObjectives().isEmpty()) {
                    final LinkedList<String> list = new LinkedList<>();
                    final LinkedList<Integer> countList = new LinkedList<>();
                    for (int i = 0; i < bukkitStage.getCustomObjectives().size(); i++) {
                        list.add(bukkitStage.getCustomObjectives().get(i).getName());
                        countList.add(bukkitStage.getCustomObjectiveCounts().get(i));
                    }
                    final LinkedList<Entry<String, Object>> dataMapList = new LinkedList<>(bukkitStage.getCustomObjectiveData());
                    context.setSessionData(pref + Key.S_CUSTOM_OBJECTIVES, list);
                    context.setSessionData(pref + Key.S_CUSTOM_OBJECTIVES_COUNT, countList);
                    context.setSessionData(pref + Key.S_CUSTOM_OBJECTIVES_DATA, dataMapList);
                }
                if (bukkitStage.getStartAction() != null) {
                    context.setSessionData(pref + Key.S_START_EVENT, bukkitStage.getStartAction().getName());
                }
                if (bukkitStage.getFinishAction() != null) {
                    context.setSessionData(pref + Key.S_FINISH_EVENT, bukkitStage.getFinishAction().getName());
                }
                if (bukkitStage.getFailAction() != null) {
                    context.setSessionData(pref + Key.S_FAIL_EVENT, bukkitStage.getFailAction().getName());
                }
                if (bukkitStage.getDeathAction() != null) {
                    context.setSessionData(pref + Key.S_DEATH_EVENT, bukkitStage.getDeathAction().getName());
                }
                if (bukkitStage.getDisconnectAction() != null) {
                    context.setSessionData(pref + Key.S_DISCONNECT_EVENT, bukkitStage.getDisconnectAction().getName());
                }
                if (!bukkitStage.getChatActions().isEmpty()) {
                    final LinkedList<String> chatEvents = new LinkedList<>();
                    final LinkedList<String> chatEventTriggers = new LinkedList<>();
                    for (final String s : bukkitStage.getChatActions().keySet()) {
                        chatEventTriggers.add(s);
                        chatEvents.add(bukkitStage.getChatActions().get(s).getName());
                    }
                    context.setSessionData(pref + Key.S_CHAT_EVENTS, chatEvents);
                    context.setSessionData(pref + Key.S_CHAT_EVENT_TRIGGERS, chatEventTriggers);
                }
                if (!bukkitStage.getCommandActions().isEmpty()) {
                    final LinkedList<String> commandEvents = new LinkedList<>();
                    final LinkedList<String> commandEventTriggers = new LinkedList<>();
                    for (final String s : bukkitStage.getCommandActions().keySet()) {
                        commandEventTriggers.add(s);
                        commandEvents.add(bukkitStage.getCommandActions().get(s).getName());
                    }
                    context.setSessionData(pref + Key.S_COMMAND_EVENTS, commandEvents);
                    context.setSessionData(pref + Key.S_COMMAND_EVENT_TRIGGERS, commandEventTriggers);
                }
                if (bukkitStage.getCondition() != null) {
                    context.setSessionData(pref + Key.S_CONDITION, bukkitStage.getCondition().getName());
                }
                if (bukkitStage.getDelay() != -1) {
                    context.setSessionData(pref + Key.S_DELAY, bukkitStage.getDelay());
                    if (bukkitStage.getDelayMessage() != null) {
                        context.setSessionData(pref + Key.S_DELAY_MESSAGE, bukkitStage.getDelayMessage());
                    }
                }
                if (bukkitStage.getScript() != null) {
                    context.setSessionData(pref + Key.S_DENIZEN, bukkitStage.getScript());
                }
                if (bukkitStage.getCompleteMessage() != null) {
                    context.setSessionData(pref + Key.S_COMPLETE_MESSAGE, bukkitStage.getCompleteMessage());
                }
                if (bukkitStage.getStartMessage() != null) {
                    context.setSessionData(pref + Key.S_START_MESSAGE, bukkitStage.getStartMessage());
                }
                if (!bukkitStage.getObjectiveOverrides().isEmpty()) {
                    context.setSessionData(pref + Key.S_OVERRIDE_DISPLAY, bukkitStage.getObjectiveOverrides());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteQuest(final ConversationContext context) {
        final FileConfiguration data = new YamlConfiguration();
        final File questsFile = new File(plugin.getDataFolder(), "quests.yml");
        try {
            data.load(questsFile);
        } catch (final IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("questErrorReadingFile")
                    .replace("<quest>", questsFile.getName()));
            return;
        }
        final String quest = (String) context.getSessionData(Key.ED_QUEST_DELETE);
        final ConfigurationSection sec = data.getConfigurationSection("quests");
        if (sec != null) {
            for (final String key : sec.getKeys(false)) {
                if (sec.getString(key + ".name") != null
                        && Objects.requireNonNull(sec.getString(key + ".name")).equalsIgnoreCase(quest)) {
                    sec.set(key, null);
                    break;
                }
            }
        }
        try {
            data.save(questsFile);
        } catch (final IOException e) {
            context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("questSaveError"));
            return;
        }
        final ReloadCallback<Boolean> callback = response -> {
            if (!response) {
                context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("unknownError"));
            }
        };
        plugin.reload(callback);
        context.getForWhom().sendRawMessage(ChatColor.GREEN + BukkitLang.get("questDeleted"));
        if (plugin.getConfigSettings().getConsoleLogging() > 0) {
            final String identifier = context.getForWhom() instanceof Player ?
                    "Player " + ((Player)context.getForWhom()).getUniqueId() : "CONSOLE";
            plugin.getLogger().info(identifier + " deleted quest " + quest);
        }
    }

    public void saveQuest(final ConversationContext context, final ConfigurationSection section) {
        String edit = null;
        if (context.getSessionData(Key.ED_QUEST_EDIT) != null) {
            edit = (String) context.getSessionData(Key.ED_QUEST_EDIT);
        }
        if (edit != null) {
            final ConfigurationSection questList = section.getParent();
            if (questList != null) {
                for (final String key : questList.getKeys(false)) {
                    final String name = questList.getString(key + ".name");
                    if (name != null) {
                        if (name.equalsIgnoreCase(edit)) {
                            questList.set(key, null);
                            break;
                        }
                    }
                }
            }
        }
        section.set("name", context.getSessionData(Key.Q_NAME) != null
                ? context.getSessionData(Key.Q_NAME) : null);
        section.set("ask-message", context.getSessionData(Key.Q_ASK_MESSAGE) != null
                ? context.getSessionData(Key.Q_ASK_MESSAGE) : null);
        section.set("finish-message", context.getSessionData(Key.Q_FINISH_MESSAGE) != null
                ? context.getSessionData(Key.Q_FINISH_MESSAGE) : null);
        section.set("npc-giver-uuid", context.getSessionData(Key.Q_START_NPC) != null
                ? context.getSessionData(Key.Q_START_NPC) : null);
        section.set("block-start", context.getSessionData(Key.Q_START_BLOCK) != null
                ? BukkitConfigUtil.getLocationInfo((Location) Objects.requireNonNull(context
                .getSessionData(Key.Q_START_BLOCK))) : null);
        section.set("event", context.getSessionData(Key.Q_INITIAL_EVENT) != null
                ? context.getSessionData(Key.Q_INITIAL_EVENT) : null);
        section.set("region", context.getSessionData(Key.Q_REGION) != null
                ? context.getSessionData(Key.Q_REGION) : null);
        section.set("gui-display", context.getSessionData(Key.Q_GUIDISPLAY) != null
                ? context.getSessionData(Key.Q_GUIDISPLAY) : null);
        saveRequirements(context, section);
        saveStages(context, section);
        saveRewards(context, section);
        savePlanner(context, section);
        saveOptions(context, section);
        if (plugin.getConfigSettings().getConsoleLogging() > 0) {
            final String identifier = context.getForWhom() instanceof Player ?
                    "Player " + ((Player)context.getForWhom()).getUniqueId() : "CONSOLE";
            plugin.getLogger().info(identifier + " saved quest " + context.getSessionData(Key.Q_NAME));
        }
    }

    @SuppressWarnings("unchecked")
    private void saveRequirements(final ConversationContext context, final ConfigurationSection section) {
        final ConfigurationSection requirements = section.createSection("requirements");
        requirements.set("money", context.getSessionData(Key.REQ_MONEY) != null
                ? context.getSessionData(Key.REQ_MONEY) : null);
        requirements.set("quest-points", context.getSessionData(Key.REQ_QUEST_POINTS) != null
                ? context.getSessionData(Key.REQ_QUEST_POINTS) : null);
        requirements.set("exp", context.getSessionData(Key.REQ_EXP) != null
                ? context.getSessionData(Key.REQ_EXP) : null);
        requirements.set("items", context.getSessionData(Key.REQ_ITEMS) != null
                ? context.getSessionData(Key.REQ_ITEMS) : null);
        requirements.set("remove-items", context.getSessionData(Key.REQ_ITEMS_REMOVE) != null
                ? context.getSessionData(Key.REQ_ITEMS_REMOVE) : null);
        requirements.set("permissions", context.getSessionData(Key.REQ_PERMISSION) != null
                ? context.getSessionData(Key.REQ_PERMISSION) : null);
        requirements.set("quests", context.getSessionData(Key.REQ_QUEST) != null
                ? context.getSessionData(Key.REQ_QUEST) : null);
        requirements.set("quest-blocks", context.getSessionData(Key.REQ_QUEST_BLOCK) != null
                ? context.getSessionData(Key.REQ_QUEST_BLOCK) : null);
        requirements.set("mcmmo-skills", context.getSessionData(Key.REQ_MCMMO_SKILLS) != null
                ? context.getSessionData(Key.REQ_MCMMO_SKILLS) : null);
        requirements.set("mcmmo-amounts", context.getSessionData(Key.REQ_MCMMO_SKILL_AMOUNTS) != null
                ? context.getSessionData(Key.REQ_MCMMO_SKILL_AMOUNTS) : null);
        requirements.set("heroes-primary-class", context.getSessionData(Key.REQ_HEROES_PRIMARY_CLASS) != null
                ? context.getSessionData(Key.REQ_HEROES_PRIMARY_CLASS) : null);
        requirements.set("heroes-secondary-class", context.getSessionData(Key.REQ_HEROES_SECONDARY_CLASS) != null
                ? context.getSessionData(Key.REQ_HEROES_SECONDARY_CLASS) : null);
        final LinkedList<String> customRequirements = context.getSessionData(Key.REQ_CUSTOM) != null
                ? (LinkedList<String>) context.getSessionData(Key.REQ_CUSTOM) : null;
        final LinkedList<Map<String, Object>> customRequirementsData = context.getSessionData(Key.REQ_CUSTOM_DATA) != null
                ? (LinkedList<Map<String, Object>>) context.getSessionData(Key.REQ_CUSTOM_DATA) : null;
        if (customRequirements != null && customRequirementsData != null) {
            final ConfigurationSection customRequirementsSec = requirements.createSection("custom-requirements");
            for (int i = 0; i < customRequirements.size(); i++) {
                final ConfigurationSection customReqSec = customRequirementsSec.createSection("req" + (i + 1));
                customReqSec.set("name", customRequirements.get(i));
                customReqSec.set("data", customRequirementsData.get(i));
            }
        }
        requirements.set("fail-requirement-message", context.getSessionData(Key.REQ_FAIL_MESSAGE) != null
                ? context.getSessionData(Key.REQ_FAIL_MESSAGE) : null);
        if (requirements.getKeys(false).isEmpty()) {
            section.set("requirements", null);
        }
    }

    @SuppressWarnings("unchecked")
    private void saveStages(final ConversationContext context, final ConfigurationSection section) {
        final ConfigurationSection stages = section.createSection("stages");
        final ConfigurationSection ordered = stages.createSection("ordered");
        String pref;
        for (int i = 1; i <= new QuestStageMenuPrompt(context).getStages(context); i++) {
            pref = "stage" + i;
            final ConfigurationSection stage = ordered.createSection("" + i);
            stage.set("break-block-names", context.getSessionData(pref + Key.S_BREAK_NAMES) != null
                    ? context.getSessionData(pref + Key.S_BREAK_NAMES) : null);
            stage.set("break-block-amounts", context.getSessionData(pref + Key.S_BREAK_AMOUNTS) != null
                    ? context.getSessionData(pref + Key.S_BREAK_AMOUNTS) : null);
            stage.set("break-block-durability", context.getSessionData(pref + Key.S_BREAK_DURABILITY) != null
                    ? context.getSessionData(pref + Key.S_BREAK_DURABILITY) : null);
            stage.set("damage-block-names", context.getSessionData(pref + Key.S_DAMAGE_NAMES) != null
                    ? context.getSessionData(pref + Key.S_DAMAGE_NAMES) : null);
            stage.set("damage-block-amounts", context.getSessionData(pref + Key.S_DAMAGE_AMOUNTS) != null
                    ? context.getSessionData(pref + Key.S_DAMAGE_AMOUNTS) : null);
            stage.set("damage-block-durability", context.getSessionData(pref + Key.S_DAMAGE_DURABILITY) != null
                    ? context.getSessionData(pref + Key.S_DAMAGE_DURABILITY) : null);
            stage.set("place-block-names", context.getSessionData(pref + Key.S_PLACE_NAMES) != null
                    ? context.getSessionData(pref + Key.S_PLACE_NAMES) : null);
            stage.set("place-block-amounts", context.getSessionData(pref + Key.S_PLACE_AMOUNTS) != null
                    ? context.getSessionData(pref + Key.S_PLACE_AMOUNTS) : null);
            stage.set("place-block-durability", context.getSessionData(pref + Key.S_PLACE_DURABILITY) != null
                    ? context.getSessionData(pref + Key.S_PLACE_DURABILITY) : null);
            stage.set("use-block-names", context.getSessionData(pref + Key.S_USE_NAMES) != null
                    ? context.getSessionData(pref + Key.S_USE_NAMES) : null);
            stage.set("use-block-amounts", context.getSessionData(pref + Key.S_USE_AMOUNTS) != null
                    ? context.getSessionData(pref + Key.S_USE_AMOUNTS) : null);
            stage.set("use-block-durability", context.getSessionData(pref + Key.S_USE_DURABILITY) != null
                    ? context.getSessionData(pref + Key.S_USE_DURABILITY) : null);
            stage.set("cut-block-names", context.getSessionData(pref + Key.S_CUT_NAMES) != null
                    ? context.getSessionData(pref + Key.S_CUT_NAMES) : null);
            stage.set("cut-block-amounts", context.getSessionData(pref + Key.S_CUT_AMOUNTS) != null
                    ? context.getSessionData(pref + Key.S_CUT_AMOUNTS) : null);
            stage.set("cut-block-durability", context.getSessionData(pref + Key.S_CUT_DURABILITY) != null
                    ? context.getSessionData(pref + Key.S_CUT_DURABILITY) : null);
            stage.set("items-to-craft", context.getSessionData(pref + Key.S_CRAFT_ITEMS) != null
                    ? context.getSessionData(pref + Key.S_CRAFT_ITEMS) : null);
            stage.set("items-to-smelt", context.getSessionData(pref + Key.S_SMELT_ITEMS) != null
                    ? context.getSessionData(pref + Key.S_SMELT_ITEMS) : null);
            stage.set("items-to-enchant", context.getSessionData(pref + Key.S_ENCHANT_ITEMS) != null
                    ? context.getSessionData(pref + Key.S_ENCHANT_ITEMS) : null);
            stage.set("items-to-brew", context.getSessionData(pref + Key.S_BREW_ITEMS) != null
                    ? context.getSessionData(pref + Key.S_BREW_ITEMS) : null);
            stage.set("items-to-consume", context.getSessionData(pref + Key.S_CONSUME_ITEMS) != null
                    ? context.getSessionData(pref + Key.S_CONSUME_ITEMS) : null);
            stage.set("cows-to-milk", context.getSessionData(pref + Key.S_COW_MILK) != null
                    ? context.getSessionData(pref + Key.S_COW_MILK) : null);
            stage.set("fish-to-catch", context.getSessionData(pref + Key.S_FISH) != null
                    ? context.getSessionData(pref + Key.S_FISH) : null);
            stage.set("players-to-kill", context.getSessionData(pref + Key.S_PLAYER_KILL) != null
                    ? context.getSessionData(pref + Key.S_PLAYER_KILL) : null);
            stage.set("items-to-deliver", context.getSessionData(pref + Key.S_DELIVERY_ITEMS) != null
                    ? context.getSessionData(pref + Key.S_DELIVERY_ITEMS) : null);
            stage.set("npc-delivery-uuids", context.getSessionData(pref + Key.S_DELIVERY_NPCS) != null
                    ? context.getSessionData(pref + Key.S_DELIVERY_NPCS) : null);
            stage.set("delivery-messages", context.getSessionData(pref + Key.S_DELIVERY_MESSAGES) != null
                    ? context.getSessionData(pref + Key.S_DELIVERY_MESSAGES) : null);
            stage.set("npc-uuids-to-talk-to", context.getSessionData(pref + Key.S_NPCS_TO_TALK_TO) != null
                    ? context.getSessionData(pref + Key.S_NPCS_TO_TALK_TO) : null);
            stage.set("npc-uuids-to-kill", context.getSessionData(pref + Key.S_NPCS_TO_KILL) != null
                    ? context.getSessionData(pref + Key.S_NPCS_TO_KILL) : null);
            stage.set("npc-kill-amounts", context.getSessionData(pref + Key.S_NPCS_TO_KILL_AMOUNTS) != null
                    ? context.getSessionData(pref + Key.S_NPCS_TO_KILL_AMOUNTS) : null);
            stage.set("mobs-to-kill", context.getSessionData(pref + Key.S_MOB_TYPES) != null
                    ? context.getSessionData(pref + Key.S_MOB_TYPES) : null);
            stage.set("mob-amounts", context.getSessionData(pref + Key.S_MOB_AMOUNTS) != null
                    ? context.getSessionData(pref + Key.S_MOB_AMOUNTS) : null);
            stage.set("locations-to-kill", context.getSessionData(pref + Key.S_MOB_KILL_LOCATIONS) != null
                    ? context.getSessionData(pref + Key.S_MOB_KILL_LOCATIONS) : null);
            stage.set("kill-location-radii", context.getSessionData(pref + Key.S_MOB_KILL_LOCATIONS_RADIUS) != null
                    ? context.getSessionData(pref + Key.S_MOB_KILL_LOCATIONS_RADIUS) : null);
            stage.set("kill-location-names", context.getSessionData(pref + Key.S_MOB_KILL_LOCATIONS_NAMES) != null
                    ? context.getSessionData(pref + Key.S_MOB_KILL_LOCATIONS_NAMES) : null);
            stage.set("locations-to-reach", context.getSessionData(pref + Key.S_REACH_LOCATIONS) != null
                    ? context.getSessionData(pref + Key.S_REACH_LOCATIONS) : null);
            stage.set("reach-location-radii", context.getSessionData(pref + Key.S_REACH_LOCATIONS_RADIUS) != null
                    ? context.getSessionData(pref + Key.S_REACH_LOCATIONS_RADIUS) : null);
            stage.set("reach-location-names", context.getSessionData(pref + Key.S_REACH_LOCATIONS_NAMES) != null
                    ? context.getSessionData(pref + Key.S_REACH_LOCATIONS_NAMES) : null);
            stage.set("mobs-to-tame", context.getSessionData(pref + Key.S_TAME_TYPES) != null
                    ? context.getSessionData(pref + Key.S_TAME_TYPES) : null);
            stage.set("mob-tame-amounts", context.getSessionData(pref + Key.S_TAME_AMOUNTS) != null
                    ? context.getSessionData(pref + Key.S_TAME_AMOUNTS) : null);
            stage.set("sheep-to-shear", context.getSessionData(pref + Key.S_SHEAR_COLORS) != null
                    ? context.getSessionData(pref + Key.S_SHEAR_COLORS) : null);
            stage.set("sheep-amounts", context.getSessionData(pref + Key.S_SHEAR_AMOUNTS) != null
                    ? context.getSessionData(pref + Key.S_SHEAR_AMOUNTS) : null);
            stage.set("password-displays", context.getSessionData(pref + Key.S_PASSWORD_DISPLAYS) != null
                    ? context.getSessionData(pref + Key.S_PASSWORD_DISPLAYS) : null);
            stage.set("password-phrases", context.getSessionData(pref + Key.S_PASSWORD_PHRASES) != null
                    ? context.getSessionData(pref + Key.S_PASSWORD_PHRASES) : null);
            final LinkedList<String> customObj = (LinkedList<String>) context.getSessionData(pref + Key.S_CUSTOM_OBJECTIVES);
            final LinkedList<Integer> customObjCounts
                    = (LinkedList<Integer>) context.getSessionData(pref + Key.S_CUSTOM_OBJECTIVES_COUNT);
            final LinkedList<Entry<String, Object>> customObjData
                    = (LinkedList<Entry<String, Object>>) context.getSessionData(pref + Key.S_CUSTOM_OBJECTIVES_DATA);
            if (context.getSessionData(pref + Key.S_CUSTOM_OBJECTIVES) != null) {
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
            stage.set("script-to-run", context.getSessionData(pref + Key.S_DENIZEN) != null
                    ? context.getSessionData(pref + Key.S_DENIZEN) : null);
            stage.set("start-event", context.getSessionData(pref + Key.S_START_EVENT) != null
                    ? context.getSessionData(pref + Key.S_START_EVENT) : null);
            stage.set("finish-event", context.getSessionData(pref + Key.S_FINISH_EVENT) != null
                    ? context.getSessionData(pref + Key.S_FINISH_EVENT) : null);
            stage.set("fail-event", context.getSessionData(pref + Key.S_FAIL_EVENT) != null
                    ? context.getSessionData(pref + Key.S_FAIL_EVENT) : null);
            stage.set("death-event", context.getSessionData(pref + Key.S_DEATH_EVENT) != null
                    ? context.getSessionData(pref + Key.S_DEATH_EVENT) : null);
            stage.set("disconnect-event", context.getSessionData(pref + Key.S_DISCONNECT_EVENT) != null
                    ? context.getSessionData(pref + Key.S_DISCONNECT_EVENT) : null);
            stage.set("chat-events", context.getSessionData(pref + Key.S_CHAT_EVENTS) != null
                    ? context.getSessionData(pref + Key.S_CHAT_EVENTS) : null);
            stage.set("chat-event-triggers", context.getSessionData(pref + Key.S_CHAT_EVENT_TRIGGERS) != null
                    ? context.getSessionData(pref + Key.S_CHAT_EVENT_TRIGGERS) : null);
            stage.set("command-events", context.getSessionData(pref + Key.S_COMMAND_EVENTS) != null
                    ? context.getSessionData(pref + Key.S_COMMAND_EVENTS) : null);
            stage.set("command-event-triggers", context.getSessionData(pref + Key.S_COMMAND_EVENT_TRIGGERS) != null
                    ? context.getSessionData(pref + Key.S_COMMAND_EVENT_TRIGGERS) : null);
            stage.set("condition", context.getSessionData(pref + Key.S_CONDITION) != null
                    ? context.getSessionData(pref + Key.S_CONDITION) : null);
            final Long delay = (Long) context.getSessionData(pref + Key.S_DELAY);
            if (delay != null) {
                stage.set("delay", delay.intValue() / 1000);
            }
            final String delayMessage = (String) context.getSessionData(pref + Key.S_DELAY_MESSAGE);
            if (delayMessage != null) {
                stage.set("delay-message", delayMessage.replace("\\n", "\n"));
            }
            final String startMessage = (String) context.getSessionData(pref + Key.S_START_MESSAGE);
            if (startMessage != null) {
                stage.set("start-message", startMessage.replace("\\n", "\n"));
            }
            final String completeMessage = (String) context.getSessionData(pref + Key.S_COMPLETE_MESSAGE);
            if (completeMessage != null) {
                stage.set("complete-message", completeMessage.replace("\\n", "\n"));
            }
            stage.set("objective-override", context.getSessionData(pref + Key.S_OVERRIDE_DISPLAY) != null
                    ? context.getSessionData(pref + Key.S_OVERRIDE_DISPLAY) : null);
        }
    }

    @SuppressWarnings("unchecked")
    private void saveRewards(final ConversationContext context, final ConfigurationSection section) {
        final ConfigurationSection rewards = section.createSection("rewards");
        rewards.set("items", context.getSessionData(Key.REW_ITEMS) != null
                ? context.getSessionData(Key.REW_ITEMS) : null);
        rewards.set("money", context.getSessionData(Key.REW_MONEY) != null
                ? context.getSessionData(Key.REW_MONEY) : null);
        rewards.set("quest-points", context.getSessionData(Key.REW_QUEST_POINTS) != null
                ? context.getSessionData(Key.REW_QUEST_POINTS) : null);
        rewards.set("exp", context.getSessionData(Key.REW_EXP) != null
                ? context.getSessionData(Key.REW_EXP) : null);
        rewards.set("commands", context.getSessionData(Key.REW_COMMAND) != null
                ? context.getSessionData(Key.REW_COMMAND) : null);
        rewards.set("commands-override-display", context.getSessionData(Key.REW_COMMAND_OVERRIDE_DISPLAY) != null
                ? context.getSessionData(Key.REW_COMMAND_OVERRIDE_DISPLAY) : null);
        rewards.set("permissions", context.getSessionData(Key.REW_PERMISSION) != null
                ? context.getSessionData(Key.REW_PERMISSION) : null);
        rewards.set("permission-worlds", context.getSessionData(Key.REW_PERMISSION_WORLDS) != null
                ? context.getSessionData(Key.REW_PERMISSION_WORLDS) : null);
        rewards.set("mcmmo-skills", context.getSessionData(Key.REW_MCMMO_SKILLS) != null
                ? context.getSessionData(Key.REW_MCMMO_SKILLS) : null);
        rewards.set("mcmmo-levels", context.getSessionData(Key.REW_MCMMO_AMOUNTS) != null
                ? context.getSessionData(Key.REW_MCMMO_AMOUNTS) : null);
        rewards.set("heroes-exp-classes", context.getSessionData(Key.REW_HEROES_CLASSES) != null
                ? context.getSessionData(Key.REW_HEROES_CLASSES) : null);
        rewards.set("heroes-exp-amounts", context.getSessionData(Key.REW_HEROES_AMOUNTS) != null
                ? context.getSessionData(Key.REW_HEROES_AMOUNTS) : null);
        rewards.set("parties-experience", context.getSessionData(Key.REW_PARTIES_EXPERIENCE) != null
                ? context.getSessionData(Key.REW_PARTIES_EXPERIENCE) : null);
        rewards.set("phat-loots", context.getSessionData(Key.REW_PHAT_LOOTS) != null
                ? context.getSessionData(Key.REW_PHAT_LOOTS) : null);
        final LinkedList<String> customRewards = context.getSessionData(Key.REW_CUSTOM) != null
                ? (LinkedList<String>) context.getSessionData(Key.REW_CUSTOM) : null;
        final LinkedList<Map<String, Object>> customRewardsData = context.getSessionData(Key.REW_CUSTOM_DATA) != null
                ? (LinkedList<Map<String, Object>>) context.getSessionData(Key.REW_CUSTOM_DATA) : null;
        if (customRewards != null && customRewardsData != null) {
            final ConfigurationSection customRewardsSec = rewards.createSection("custom-rewards");
            for (int i = 0; i < customRewards.size(); i++) {
                final ConfigurationSection customRewSec = customRewardsSec.createSection("req" + (i + 1));
                customRewSec.set("name", customRewards.get(i));
                customRewSec.set("data", customRewardsData.get(i));
            }
        }
        rewards.set("details-override", context.getSessionData(Key.REW_DETAILS_OVERRIDE) != null
                ? context.getSessionData(Key.REW_DETAILS_OVERRIDE) : null);
        if (rewards.getKeys(false).isEmpty()) {
            section.set("rewards", null);
        }
    }

    private void savePlanner(final ConversationContext context, final ConfigurationSection section) {
        final ConfigurationSection pln = section.createSection("planner");
        pln.set("start", context.getSessionData(Key.PLN_START_DATE) != null
                ? context.getSessionData(Key.PLN_START_DATE) : null);
        pln.set("end", context.getSessionData(Key.PLN_END_DATE) != null
                ? context.getSessionData(Key.PLN_END_DATE) : null);
        final Long repeatCycle = (Long) context.getSessionData(Key.PLN_REPEAT_CYCLE);
        pln.set("repeat", repeatCycle != null ? (repeatCycle / 1000L) : null);
        final Long cooldown = (Long) context.getSessionData(Key.PLN_COOLDOWN);
        pln.set("cooldown", cooldown != null ? (cooldown / 1000L) : null);
        pln.set("override", context.getSessionData(Key.PLN_OVERRIDE) != null
                ? context.getSessionData(Key.PLN_OVERRIDE) : null);
        if (pln.getKeys(false).isEmpty()) {
            section.set("planner", null);
        }
    }

    private void saveOptions(final ConversationContext context, final ConfigurationSection section) {
        final ConfigurationSection opts = section.createSection("options");
        opts.set("allow-commands", context.getSessionData(Key.OPT_ALLOW_COMMANDS) != null
                ? context.getSessionData(Key.OPT_ALLOW_COMMANDS) : null);
        opts.set("allow-quitting", context.getSessionData(Key.OPT_ALLOW_QUITTING) != null
                ? context.getSessionData(Key.OPT_ALLOW_QUITTING) : null);
        opts.set("ignore-silk-touch", context.getSessionData(Key.OPT_IGNORE_SILK_TOUCH) != null
                ? context.getSessionData(Key.OPT_IGNORE_SILK_TOUCH) : null);
        opts.set("external-party-plugin", context.getSessionData(Key.OPT_EXTERNAL_PARTY_PLUGIN) != null
                ? context.getSessionData(Key.OPT_EXTERNAL_PARTY_PLUGIN) : null);
        opts.set("use-parties-plugin", context.getSessionData(Key.OPT_USE_PARTIES_PLUGIN) != null
                ? context.getSessionData(Key.OPT_USE_PARTIES_PLUGIN) : null);
        opts.set("share-progress-level", context.getSessionData(Key.OPT_SHARE_PROGRESS_LEVEL) != null
                ? context.getSessionData(Key.OPT_SHARE_PROGRESS_LEVEL) : null);
        opts.set("same-quest-only", context.getSessionData(Key.OPT_SHARE_SAME_QUEST_ONLY) != null
                ? context.getSessionData(Key.OPT_SHARE_SAME_QUEST_ONLY) : null);
        opts.set("share-distance", context.getSessionData(Key.OPT_SHARE_DISTANCE) != null
                ? context.getSessionData(Key.OPT_SHARE_DISTANCE) : null);
        opts.set("handle-offline-players", context.getSessionData(Key.OPT_HANDLE_OFFLINE_PLAYERS) != null
                ? context.getSessionData(Key.OPT_HANDLE_OFFLINE_PLAYERS) : null);
        opts.set("ignore-block-replace", context.getSessionData(Key.OPT_IGNORE_BLOCK_REPLACE) != null
                ? context.getSessionData(Key.OPT_IGNORE_BLOCK_REPLACE) : null);
        if (opts.getKeys(false).isEmpty()) {
            section.set("options", null);
        }
    }
}