/*
 * Copyright (c) 2014 PikaMug and contributors. All rights reserved.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package me.blackvein.quests.quests;

import me.blackvein.quests.module.ICustomObjective;
import me.blackvein.quests.Quests;
import me.blackvein.quests.convo.quests.main.QuestMainPrompt;
import me.blackvein.quests.convo.quests.menu.QuestMenuPrompt;
import me.blackvein.quests.convo.quests.stages.StageMenuPrompt;
import me.blackvein.quests.interfaces.ReloadCallback;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.ConfigUtil;
import me.blackvein.quests.util.FakeConversable;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;
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
import java.util.stream.Collectors;

public class BukkitQuestFactory implements QuestFactory, ConversationAbandonedListener {

    private final Quests plugin;
    private final ConversationFactory conversationFactory;
    private Map<UUID, Block> selectedBlockStarts = new HashMap<>();
    private Map<UUID, Block> selectedKillLocations = new HashMap<>();
    private Map<UUID, Block> selectedReachLocations = new HashMap<>();
    private Set<UUID> selectingNpcs = new HashSet<>();
    private List<String> editingQuestNames = new LinkedList<>();

    public BukkitQuestFactory(final Quests plugin) {
        this.plugin = plugin;
        // Ensure to initialize factory last so that 'this' is fully initialized before it is passed
        this.conversationFactory = new ConversationFactory(plugin).withModality(false).withLocalEcho(false)
                .withFirstPrompt(new QuestMenuPrompt(new ConversationContext(plugin, new FakeConversable(),
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
        if (abandonedEvent.getContext().getSessionData(CK.Q_NAME) != null) {
            editingQuestNames.remove((String) abandonedEvent.getContext().getSessionData(CK.Q_NAME));
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
    public void loadQuest(final ConversationContext context, final IQuest q) {
        context.setSessionData(CK.ED_QUEST_EDIT, q.getName());
        context.setSessionData(CK.Q_ID, q.getId());
        context.setSessionData(CK.Q_NAME, q.getName());
        context.setSessionData(CK.Q_ASK_MESSAGE, q.getDescription());
        context.setSessionData(CK.Q_FINISH_MESSAGE, q.getFinished());
        if (plugin.getDependencies().getCitizens() != null) {
            if (q.getNpcStart() != null) {
                context.setSessionData(CK.Q_START_NPC, q.getNpcStart().getId());
            }
        }
        context.setSessionData(CK.Q_START_BLOCK, q.getBlockStart());
        if (q.getInitialAction() != null) {
            context.setSessionData(CK.Q_INITIAL_EVENT, q.getInitialAction().getName());
        }
        if (q.getRegionStart() != null) {
            context.setSessionData(CK.Q_REGION, q.getRegionStart());
        }
        if (q.getGUIDisplay() != null) {
            context.setSessionData(CK.Q_GUIDISPLAY, q.getGUIDisplay());
        }
        final Requirements requirements = q.getRequirements();
        if (requirements.getMoney() != 0) {
            context.setSessionData(CK.REQ_MONEY, requirements.getMoney());
        }
        if (requirements.getQuestPoints() != 0) {
            context.setSessionData(CK.REQ_QUEST_POINTS, requirements.getQuestPoints());
        }
        if (requirements.getExp() != 0) {
            context.setSessionData(CK.REW_EXP, requirements.getExp());
        }
        if (!requirements.getItems().isEmpty()) {
            context.setSessionData(CK.REQ_ITEMS, requirements.getItems());
            context.setSessionData(CK.REQ_ITEMS_REMOVE, requirements.getRemoveItems());
        }
        if (!requirements.getNeededQuests().isEmpty()) {
            final List<String> ids = requirements.getNeededQuests().stream().map(IQuest::getId).collect(Collectors.toList());
            context.setSessionData(CK.REQ_QUEST, ids);
        }
        if (!requirements.getBlockQuests().isEmpty()) {
            final List<String> ids = requirements.getBlockQuests().stream().map(IQuest::getId).collect(Collectors.toList());
            context.setSessionData(CK.REQ_QUEST_BLOCK, ids);
        }
        if (!requirements.getMcmmoSkills().isEmpty()) {
            context.setSessionData(CK.REQ_MCMMO_SKILLS, requirements.getMcmmoAmounts());
            context.setSessionData(CK.REQ_MCMMO_SKILL_AMOUNTS, requirements.getMcmmoAmounts());
        }
        if (!requirements.getPermissions().isEmpty()) {
            context.setSessionData(CK.REQ_PERMISSION, requirements.getPermissions());
        }
        if (requirements.getHeroesPrimaryClass() != null) {
            context.setSessionData(CK.REQ_HEROES_PRIMARY_CLASS, requirements.getHeroesPrimaryClass());
        }
        if (requirements.getHeroesSecondaryClass() != null) {
            context.setSessionData(CK.REQ_HEROES_SECONDARY_CLASS, requirements.getHeroesSecondaryClass());
        }
        if (!requirements.getCustomRequirements().isEmpty()) {
            final LinkedList<String> list = new LinkedList<>();
            final LinkedList<Map<String, Object>> dataMapList = new LinkedList<>();
            for (final Entry<String, Map<String, Object>> entry : requirements.getCustomRequirements().entrySet()) {
                list.add(entry.getKey());
                dataMapList.add(entry.getValue());
            }
            context.setSessionData(CK.REQ_CUSTOM, list);
            context.setSessionData(CK.REQ_CUSTOM_DATA, dataMapList);
        }
        if (!requirements.getDetailsOverride().isEmpty()) {
            context.setSessionData(CK.REQ_FAIL_MESSAGE, requirements.getDetailsOverride());
        }
        final Rewards rewards = q.getRewards();
        if (rewards.getMoney() != 0) {
            context.setSessionData(CK.REW_MONEY, rewards.getMoney());
        }
        if (rewards.getQuestPoints() != 0) {
            context.setSessionData(CK.REW_QUEST_POINTS, rewards.getQuestPoints());
        }
        if (rewards.getExp() != 0) {
            context.setSessionData(CK.REW_EXP, rewards.getExp());
        }
        if (!rewards.getItems().isEmpty()) {
            context.setSessionData(CK.REW_ITEMS, rewards.getItems());
        }
        if (!rewards.getCommands().isEmpty()) {
            context.setSessionData(CK.REW_COMMAND, rewards.getCommands());
        }
        if (!rewards.getCommandsOverrideDisplay().isEmpty()) {
            context.setSessionData(CK.REW_COMMAND_OVERRIDE_DISPLAY, rewards.getCommandsOverrideDisplay());
        }
        if (!rewards.getPermissions().isEmpty()) {
            context.setSessionData(CK.REW_PERMISSION, rewards.getPermissions());
        }
        if (!rewards.getPermissions().isEmpty()) {
            context.setSessionData(CK.REW_PERMISSION_WORLDS, rewards.getPermissionWorlds());
        }
        if (!rewards.getMcmmoSkills().isEmpty()) {
            context.setSessionData(CK.REW_MCMMO_SKILLS, rewards.getMcmmoSkills());
            context.setSessionData(CK.REW_MCMMO_AMOUNTS, rewards.getMcmmoAmounts());
        }
        if (!rewards.getHeroesClasses().isEmpty()) {
            context.setSessionData(CK.REW_HEROES_CLASSES, rewards.getHeroesClasses());
            context.setSessionData(CK.REW_HEROES_AMOUNTS, rewards.getHeroesAmounts());
        }
        if (rewards.getPartiesExperience() != 0) {
            context.setSessionData(CK.REW_PARTIES_EXPERIENCE, rewards.getPartiesExperience());
        }
        if (!rewards.getPhatLoots().isEmpty()) {
            context.setSessionData(CK.REW_PHAT_LOOTS, rewards.getPhatLoots());
        }
        if (!rewards.getCustomRewards().isEmpty()) {
            context.setSessionData(CK.REW_CUSTOM, new LinkedList<>(rewards.getCustomRewards().keySet()));
            context.setSessionData(CK.REW_CUSTOM_DATA, new LinkedList<Object>(rewards.getCustomRewards().values()));
        }
        if (!rewards.getDetailsOverride().isEmpty()) {
            context.setSessionData(CK.REW_DETAILS_OVERRIDE, rewards.getDetailsOverride());
        }
        final Planner pln = q.getPlanner();
        if (pln.getStart() != null) {
            context.setSessionData(CK.PLN_START_DATE, pln.getStart());
        }
        if (pln.getEnd() != null) {
            context.setSessionData(CK.PLN_END_DATE, pln.getEnd());
        }
        if (pln.getRepeat() != -1) {
            context.setSessionData(CK.PLN_REPEAT_CYCLE, pln.getRepeat());
        }
        if (pln.getCooldown() != -1) {
            context.setSessionData(CK.PLN_COOLDOWN, pln.getCooldown());
        }
        context.setSessionData(CK.PLN_OVERRIDE, pln.getOverride());
        final Options opt = q.getOptions();
        context.setSessionData(CK.OPT_ALLOW_COMMANDS, opt.canAllowCommands());
        context.setSessionData(CK.OPT_ALLOW_QUITTING, opt.canAllowQuitting());
        context.setSessionData(CK.OPT_IGNORE_SILK_TOUCH, opt.canIgnoreSilkTouch());
        context.setSessionData(CK.OPT_EXTERNAL_PARTY_PLUGIN, opt.getExternalPartyPlugin());
        context.setSessionData(CK.OPT_USE_PARTIES_PLUGIN, opt.canUsePartiesPlugin());
        context.setSessionData(CK.OPT_SHARE_PROGRESS_LEVEL, opt.getShareProgressLevel());
        context.setSessionData(CK.OPT_SHARE_SAME_QUEST_ONLY, opt.canShareSameQuestOnly());
        context.setSessionData(CK.OPT_SHARE_DISTANCE, opt.getShareDistance());
        context.setSessionData(CK.OPT_HANDLE_OFFLINE_PLAYERS, opt.canHandleOfflinePlayers());
        // Stages (Objectives)
        int index = 1;
        for (final IStage stage : q.getStages()) {
            final String pref = "stage" + index;
            index++;
            context.setSessionData(pref, Boolean.TRUE);
            if (!stage.getBlocksToBreak().isEmpty()) {
                final LinkedList<String> names = new LinkedList<>();
                final LinkedList<Integer> amounts = new LinkedList<>();
                final LinkedList<Short> durability = new LinkedList<>();
                for (final ItemStack e : stage.getBlocksToBreak()) {
                    names.add(e.getType().name());
                    amounts.add(e.getAmount());
                    durability.add(e.getDurability());
                }
                context.setSessionData(pref + CK.S_BREAK_NAMES, names);
                context.setSessionData(pref + CK.S_BREAK_AMOUNTS, amounts);
                context.setSessionData(pref + CK.S_BREAK_DURABILITY, durability);
            }
            if (!stage.getBlocksToDamage().isEmpty()) {
                final LinkedList<String> names = new LinkedList<>();
                final LinkedList<Integer> amounts = new LinkedList<>();
                final LinkedList<Short> durability = new LinkedList<>();
                for (final ItemStack e : stage.getBlocksToDamage()) {
                    names.add(e.getType().name());
                    amounts.add(e.getAmount());
                    durability.add(e.getDurability());
                }
                context.setSessionData(pref + CK.S_DAMAGE_NAMES, names);
                context.setSessionData(pref + CK.S_DAMAGE_AMOUNTS, amounts);
                context.setSessionData(pref + CK.S_DAMAGE_DURABILITY, durability);
            }
            if (!stage.getBlocksToPlace().isEmpty()) {
                final LinkedList<String> names = new LinkedList<>();
                final LinkedList<Integer> amounts = new LinkedList<>();
                final LinkedList<Short> durability = new LinkedList<>();
                for (final ItemStack e : stage.getBlocksToPlace()) {
                    names.add(e.getType().name());
                    amounts.add(e.getAmount());
                    durability.add(e.getDurability());
                }
                context.setSessionData(pref + CK.S_PLACE_NAMES, names);
                context.setSessionData(pref + CK.S_PLACE_AMOUNTS, amounts);
                context.setSessionData(pref + CK.S_PLACE_DURABILITY, durability);
            }
            if (!stage.getBlocksToUse().isEmpty()) {
                final LinkedList<String> names = new LinkedList<>();
                final LinkedList<Integer> amounts = new LinkedList<>();
                final LinkedList<Short> durability = new LinkedList<>();
                for (final ItemStack e : stage.getBlocksToUse()) {
                    names.add(e.getType().name());
                    amounts.add(e.getAmount());
                    durability.add(e.getDurability());
                }
                context.setSessionData(pref + CK.S_USE_NAMES, names);
                context.setSessionData(pref + CK.S_USE_AMOUNTS, amounts);
                context.setSessionData(pref + CK.S_USE_DURABILITY, durability);
            }
            if (!stage.getBlocksToCut().isEmpty()) {
                final LinkedList<String> names = new LinkedList<>();
                final LinkedList<Integer> amounts = new LinkedList<>();
                final LinkedList<Short> durability = new LinkedList<>();
                for (final ItemStack e : stage.getBlocksToCut()) {
                    names.add(e.getType().name());
                    amounts.add(e.getAmount());
                    durability.add(e.getDurability());
                }
                context.setSessionData(pref + CK.S_CUT_NAMES, names);
                context.setSessionData(pref + CK.S_CUT_AMOUNTS, amounts);
                context.setSessionData(pref + CK.S_CUT_DURABILITY, durability);
            }
            if (!stage.getItemsToCraft().isEmpty()) {
                final LinkedList<ItemStack> items = new LinkedList<>(stage.getItemsToCraft());
                context.setSessionData(pref + CK.S_CRAFT_ITEMS, items);
            }
            if (!stage.getItemsToSmelt().isEmpty()) {
                final LinkedList<ItemStack> items = new LinkedList<>(stage.getItemsToSmelt());
                context.setSessionData(pref + CK.S_SMELT_ITEMS, items);
            }
            if (!stage.getItemsToEnchant().isEmpty()) {
                final LinkedList<ItemStack> items = new LinkedList<>(stage.getItemsToEnchant());
                context.setSessionData(pref + CK.S_ENCHANT_ITEMS, items);
            }
            if (!stage.getItemsToBrew().isEmpty()) {
                final LinkedList<ItemStack> items = new LinkedList<>(stage.getItemsToBrew());
                context.setSessionData(pref + CK.S_BREW_ITEMS, items);
            }
            if (!stage.getItemsToConsume().isEmpty()) {
                final LinkedList<ItemStack> items = new LinkedList<>(stage.getItemsToConsume());
                context.setSessionData(pref + CK.S_CONSUME_ITEMS, items);
            }
            if (stage.getCowsToMilk() != null) {
                context.setSessionData(pref + CK.S_COW_MILK, stage.getCowsToMilk());
            }
            if (stage.getFishToCatch() != null) {
                context.setSessionData(pref + CK.S_FISH, stage.getFishToCatch());
            }
            if (stage.getPlayersToKill() != null) {
                context.setSessionData(pref + CK.S_PLAYER_KILL, stage.getPlayersToKill());
            }
            if (!stage.getItemsToDeliver().isEmpty()) {
                final LinkedList<ItemStack> items = new LinkedList<>(stage.getItemsToDeliver());
                final LinkedList<Integer> npcs = new LinkedList<>(stage.getItemDeliveryTargets());
                context.setSessionData(pref + CK.S_DELIVERY_ITEMS, items);
                context.setSessionData(pref + CK.S_DELIVERY_NPCS, npcs);
                context.setSessionData(pref + CK.S_DELIVERY_MESSAGES, stage.getDeliverMessages());
            }
            if (!stage.getCitizensToInteract().isEmpty()) {
                final LinkedList<Integer> npcs = new LinkedList<>(stage.getCitizensToInteract());
                context.setSessionData(pref + CK.S_NPCS_TO_TALK_TO, npcs);
            }
            if (!stage.getCitizensToKill().isEmpty()) {
                final LinkedList<Integer> npcs = new LinkedList<>(stage.getCitizensToKill());
                context.setSessionData(pref + CK.S_NPCS_TO_KILL, npcs);
                context.setSessionData(pref + CK.S_NPCS_TO_KILL_AMOUNTS, stage.getCitizenNumToKill());
            }
            if (!stage.getMobsToKill().isEmpty()) {
                final LinkedList<String> mobs = new LinkedList<>();
                for (final EntityType et : stage.getMobsToKill()) {
                    mobs.add(MiscUtil.getPrettyMobName(et));
                }
                context.setSessionData(pref + CK.S_MOB_TYPES, mobs);
                context.setSessionData(pref + CK.S_MOB_AMOUNTS, stage.getMobNumToKill());
                if (!stage.getLocationsToKillWithin().isEmpty()) {
                    final LinkedList<String> locations = new LinkedList<>();
                    for (final Location l : stage.getLocationsToKillWithin()) {
                        locations.add(ConfigUtil.getLocationInfo(l));
                    }
                    context.setSessionData(pref + CK.S_MOB_KILL_LOCATIONS, locations);
                    context.setSessionData(pref + CK.S_MOB_KILL_LOCATIONS_RADIUS, stage.getRadiiToKillWithin());
                    context.setSessionData(pref + CK.S_MOB_KILL_LOCATIONS_NAMES, stage.getKillNames());
                }
            }
            if (!stage.getLocationsToReach().isEmpty()) {
                final LinkedList<String> locations = new LinkedList<>();
                for (final Location l : stage.getLocationsToReach()) {
                    locations.add(ConfigUtil.getLocationInfo(l));
                }
                context.setSessionData(pref + CK.S_REACH_LOCATIONS, locations);
                context.setSessionData(pref + CK.S_REACH_LOCATIONS_RADIUS, stage.getRadiiToReachWithin());
                context.setSessionData(pref + CK.S_REACH_LOCATIONS_NAMES, stage.getLocationNames());
            }
            if (!stage.getMobsToTame().isEmpty()) {
                final LinkedList<String> mobs = new LinkedList<>();
                for (final EntityType e : stage.getMobsToTame()) {
                    mobs.add(MiscUtil.getPrettyMobName(e));
                }
                final LinkedList<Integer> amounts = new LinkedList<>(stage.getMobNumToTame());
                context.setSessionData(pref + CK.S_TAME_TYPES, mobs);
                context.setSessionData(pref + CK.S_TAME_AMOUNTS, amounts);
            }
            if (!stage.getSheepToShear().isEmpty()) {
                final LinkedList<String> colors = new LinkedList<>();
                for (final DyeColor d : stage.getSheepToShear()) {
                    colors.add(MiscUtil.getPrettyDyeColorName(d));

                }
                final LinkedList<Integer> amounts = new LinkedList<>(stage.getSheepNumToShear());
                context.setSessionData(pref + CK.S_SHEAR_COLORS, colors);
                context.setSessionData(pref + CK.S_SHEAR_AMOUNTS, amounts);
            }
            if (!stage.getPasswordDisplays().isEmpty()) {
                context.setSessionData(pref + CK.S_PASSWORD_DISPLAYS, stage.getPasswordDisplays());
                context.setSessionData(pref + CK.S_PASSWORD_PHRASES, stage.getPasswordPhrases());
            }
            if (!stage.getCustomObjectives().isEmpty()) {
                final LinkedList<String> list = new LinkedList<>();
                final LinkedList<Integer> countList = new LinkedList<>();
                for (int i = 0; i < stage.getCustomObjectives().size(); i++) {
                    list.add(stage.getCustomObjectives().get(i).getName());
                    countList.add(stage.getCustomObjectiveCounts().get(i));
                }
                final LinkedList<Entry<String, Object>> dataMapList = new LinkedList<>(stage.getCustomObjectiveData());
                context.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES, list);
                context.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES_COUNT, countList);
                context.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA, dataMapList);
            }
            if (stage.getStartAction() != null) {
                context.setSessionData(pref + CK.S_START_EVENT, stage.getStartAction().getName());
            }
            if (stage.getFinishAction() != null) {
                context.setSessionData(pref + CK.S_FINISH_EVENT, stage.getFinishAction().getName());
            }
            if (stage.getFailAction() != null) {
                context.setSessionData(pref + CK.S_FAIL_EVENT, stage.getFailAction().getName());
            }
            if (stage.getDeathAction() != null) {
                context.setSessionData(pref + CK.S_DEATH_EVENT, stage.getDeathAction().getName());
            }
            if (stage.getDisconnectAction() != null) {
                context.setSessionData(pref + CK.S_DISCONNECT_EVENT, stage.getDisconnectAction().getName());
            }
            if (!stage.getChatActions().isEmpty()) {
                final LinkedList<String> chatEvents = new LinkedList<>();
                final LinkedList<String> chatEventTriggers = new LinkedList<>();
                for (final String s : stage.getChatActions().keySet()) {
                    chatEventTriggers.add(s);
                    chatEvents.add(stage.getChatActions().get(s).getName());
                }
                context.setSessionData(pref + CK.S_CHAT_EVENTS, chatEvents);
                context.setSessionData(pref + CK.S_CHAT_EVENT_TRIGGERS, chatEventTriggers);
            }
            if (!stage.getCommandActions().isEmpty()) {
                final LinkedList<String> commandEvents = new LinkedList<>();
                final LinkedList<String> commandEventTriggers = new LinkedList<>();
                for (final String s : stage.getCommandActions().keySet()) {
                    commandEventTriggers.add(s);
                    commandEvents.add(stage.getCommandActions().get(s).getName());
                }
                context.setSessionData(pref + CK.S_COMMAND_EVENTS, commandEvents);
                context.setSessionData(pref + CK.S_COMMAND_EVENT_TRIGGERS, commandEventTriggers);
            }
            if (stage.getCondition() != null) {
                context.setSessionData(pref + CK.S_CONDITION, stage.getCondition().getName());
            }
            if (stage.getDelay() != -1) {
                context.setSessionData(pref + CK.S_DELAY, stage.getDelay());
                if (stage.getDelayMessage() != null) {
                    context.setSessionData(pref + CK.S_DELAY_MESSAGE, stage.getDelayMessage());
                }
            }
            if (stage.getScript() != null) {
                context.setSessionData(pref + CK.S_DENIZEN, stage.getScript());
            }
            if (stage.getCompleteMessage() != null) {
                context.setSessionData(pref + CK.S_COMPLETE_MESSAGE, stage.getCompleteMessage());
            }
            if (stage.getStartMessage() != null) {
                context.setSessionData(pref + CK.S_START_MESSAGE, stage.getStartMessage());
            }
            if (!stage.getObjectiveOverrides().isEmpty()) {
                context.setSessionData(pref + CK.S_OVERRIDE_DISPLAY, stage.getObjectiveOverrides());
            }
        }
    }

    public void deleteQuest(final ConversationContext context) {
        final FileConfiguration data = new YamlConfiguration();
        final File questsFile = new File(plugin.getDataFolder(), "quests.yml");
        try {
            data.load(questsFile);
        } catch (final IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questErrorReadingFile")
                    .replace("<quest>", questsFile.getName()));
            return;
        }
        final String quest = (String) context.getSessionData(CK.ED_QUEST_DELETE);
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
            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questSaveError"));
            return;
        }
        final ReloadCallback<Boolean> callback = response -> {
            if (!response) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("unknownError"));
            }
        };
        plugin.reload(callback);
        context.getForWhom().sendRawMessage(ChatColor.GREEN + Lang.get("questDeleted"));
        if (plugin.getSettings().getConsoleLogging() > 0) {
            final String identifier = context.getForWhom() instanceof Player ?
                    "Player " + ((Player)context.getForWhom()).getUniqueId() : "CONSOLE";
            plugin.getLogger().info(identifier + " deleted quest " + quest);
        }
    }

    public void saveQuest(final ConversationContext context, final ConfigurationSection section) {
        String edit = null;
        if (context.getSessionData(CK.ED_QUEST_EDIT) != null) {
            edit = (String) context.getSessionData(CK.ED_QUEST_EDIT);
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
        section.set("name", context.getSessionData(CK.Q_NAME) != null
                ? context.getSessionData(CK.Q_NAME) : null);
        section.set("ask-message", context.getSessionData(CK.Q_ASK_MESSAGE) != null
                ? context.getSessionData(CK.Q_ASK_MESSAGE) : null);
        section.set("finish-message", context.getSessionData(CK.Q_FINISH_MESSAGE) != null
                ? context.getSessionData(CK.Q_FINISH_MESSAGE) : null);
        section.set("npc-giver-id", context.getSessionData(CK.Q_START_NPC) != null
                ? context.getSessionData(CK.Q_START_NPC) : null);
        section.set("block-start", context.getSessionData(CK.Q_START_BLOCK) != null
                ? ConfigUtil.getLocationInfo((Location) Objects.requireNonNull(context
                .getSessionData(CK.Q_START_BLOCK))) : null);
        section.set("event", context.getSessionData(CK.Q_INITIAL_EVENT) != null
                ? context.getSessionData(CK.Q_INITIAL_EVENT) : null);
        section.set("region", context.getSessionData(CK.Q_REGION) != null
                ? context.getSessionData(CK.Q_REGION) : null);
        section.set("gui-display", context.getSessionData(CK.Q_GUIDISPLAY) != null
                ? context.getSessionData(CK.Q_GUIDISPLAY) : null);
        saveRequirements(context, section);
        saveStages(context, section);
        saveRewards(context, section);
        savePlanner(context, section);
        saveOptions(context, section);
        if (plugin.getSettings().getConsoleLogging() > 0) {
            final String identifier = context.getForWhom() instanceof Player ?
                    "Player " + ((Player)context.getForWhom()).getUniqueId() : "CONSOLE";
            plugin.getLogger().info(identifier + " saved quest " + context.getSessionData(CK.Q_NAME));
        }
    }

    @SuppressWarnings("unchecked")
    private void saveRequirements(final ConversationContext context, final ConfigurationSection section) {
        final ConfigurationSection requirements = section.createSection("requirements");
        requirements.set("money", context.getSessionData(CK.REQ_MONEY) != null
                ? context.getSessionData(CK.REQ_MONEY) : null);
        requirements.set("quest-points", context.getSessionData(CK.REQ_QUEST_POINTS) != null
                ? context.getSessionData(CK.REQ_QUEST_POINTS) : null);
        requirements.set("exp", context.getSessionData(CK.REQ_EXP) != null
                ? context.getSessionData(CK.REQ_EXP) : null);
        requirements.set("items", context.getSessionData(CK.REQ_ITEMS) != null
                ? context.getSessionData(CK.REQ_ITEMS) : null);
        requirements.set("remove-items", context.getSessionData(CK.REQ_ITEMS_REMOVE) != null
                ? context.getSessionData(CK.REQ_ITEMS_REMOVE) : null);
        requirements.set("permissions", context.getSessionData(CK.REQ_PERMISSION) != null
                ? context.getSessionData(CK.REQ_PERMISSION) : null);
        requirements.set("quests", context.getSessionData(CK.REQ_QUEST) != null
                ? context.getSessionData(CK.REQ_QUEST) : null);
        requirements.set("quest-blocks", context.getSessionData(CK.REQ_QUEST_BLOCK) != null
                ? context.getSessionData(CK.REQ_QUEST_BLOCK) : null);
        requirements.set("mcmmo-skills", context.getSessionData(CK.REQ_MCMMO_SKILLS) != null
                ? context.getSessionData(CK.REQ_MCMMO_SKILLS) : null);
        requirements.set("mcmmo-amounts", context.getSessionData(CK.REQ_MCMMO_SKILL_AMOUNTS) != null
                ? context.getSessionData(CK.REQ_MCMMO_SKILL_AMOUNTS) : null);
        requirements.set("heroes-primary-class", context.getSessionData(CK.REQ_HEROES_PRIMARY_CLASS) != null
                ? context.getSessionData(CK.REQ_HEROES_PRIMARY_CLASS) : null);
        requirements.set("heroes-secondary-class", context.getSessionData(CK.REQ_HEROES_SECONDARY_CLASS) != null
                ? context.getSessionData(CK.REQ_HEROES_SECONDARY_CLASS) : null);
        final LinkedList<String> customRequirements = context.getSessionData(CK.REQ_CUSTOM) != null
                ? (LinkedList<String>) context.getSessionData(CK.REQ_CUSTOM) : null;
        final LinkedList<Map<String, Object>> customRequirementsData = context.getSessionData(CK.REQ_CUSTOM_DATA) != null
                ? (LinkedList<Map<String, Object>>) context.getSessionData(CK.REQ_CUSTOM_DATA) : null;
        if (customRequirements != null && customRequirementsData != null) {
            final ConfigurationSection customRequirementsSec = requirements.createSection("custom-requirements");
            for (int i = 0; i < customRequirements.size(); i++) {
                final ConfigurationSection customReqSec = customRequirementsSec.createSection("req" + (i + 1));
                customReqSec.set("name", customRequirements.get(i));
                customReqSec.set("data", customRequirementsData.get(i));
            }
        }
        requirements.set("fail-requirement-message", context.getSessionData(CK.REQ_FAIL_MESSAGE) != null
                ? context.getSessionData(CK.REQ_FAIL_MESSAGE) : null);
        if (requirements.getKeys(false).isEmpty()) {
            section.set("requirements", null);
        }
    }

    @SuppressWarnings("unchecked")
    private void saveStages(final ConversationContext context, final ConfigurationSection section) {
        final ConfigurationSection stages = section.createSection("stages");
        final ConfigurationSection ordered = stages.createSection("ordered");
        String pref;
        for (int i = 1; i <= new StageMenuPrompt(context).getStages(context); i++) {
            pref = "stage" + i;
            final ConfigurationSection stage = ordered.createSection("" + i);
            stage.set("break-block-names", context.getSessionData(pref + CK.S_BREAK_NAMES) != null
                    ? context.getSessionData(pref + CK.S_BREAK_NAMES) : null);
            stage.set("break-block-amounts", context.getSessionData(pref + CK.S_BREAK_AMOUNTS) != null
                    ? context.getSessionData(pref + CK.S_BREAK_AMOUNTS) : null);
            stage.set("break-block-durability", context.getSessionData(pref + CK.S_BREAK_DURABILITY) != null
                    ? context.getSessionData(pref + CK.S_BREAK_DURABILITY) : null);
            stage.set("damage-block-names", context.getSessionData(pref + CK.S_DAMAGE_NAMES) != null
                    ? context.getSessionData(pref + CK.S_DAMAGE_NAMES) : null);
            stage.set("damage-block-amounts", context.getSessionData(pref + CK.S_DAMAGE_AMOUNTS) != null
                    ? context.getSessionData(pref + CK.S_DAMAGE_AMOUNTS) : null);
            stage.set("damage-block-durability", context.getSessionData(pref + CK.S_DAMAGE_DURABILITY) != null
                    ? context.getSessionData(pref + CK.S_DAMAGE_DURABILITY) : null);
            stage.set("place-block-names", context.getSessionData(pref + CK.S_PLACE_NAMES) != null
                    ? context.getSessionData(pref + CK.S_PLACE_NAMES) : null);
            stage.set("place-block-amounts", context.getSessionData(pref + CK.S_PLACE_AMOUNTS) != null
                    ? context.getSessionData(pref + CK.S_PLACE_AMOUNTS) : null);
            stage.set("place-block-durability", context.getSessionData(pref + CK.S_PLACE_DURABILITY) != null
                    ? context.getSessionData(pref + CK.S_PLACE_DURABILITY) : null);
            stage.set("use-block-names", context.getSessionData(pref + CK.S_USE_NAMES) != null
                    ? context.getSessionData(pref + CK.S_USE_NAMES) : null);
            stage.set("use-block-amounts", context.getSessionData(pref + CK.S_USE_AMOUNTS) != null
                    ? context.getSessionData(pref + CK.S_USE_AMOUNTS) : null);
            stage.set("use-block-durability", context.getSessionData(pref + CK.S_USE_DURABILITY) != null
                    ? context.getSessionData(pref + CK.S_USE_DURABILITY) : null);
            stage.set("cut-block-names", context.getSessionData(pref + CK.S_CUT_NAMES) != null
                    ? context.getSessionData(pref + CK.S_CUT_NAMES) : null);
            stage.set("cut-block-amounts", context.getSessionData(pref + CK.S_CUT_AMOUNTS) != null
                    ? context.getSessionData(pref + CK.S_CUT_AMOUNTS) : null);
            stage.set("cut-block-durability", context.getSessionData(pref + CK.S_CUT_DURABILITY) != null
                    ? context.getSessionData(pref + CK.S_CUT_DURABILITY) : null);
            stage.set("items-to-craft", context.getSessionData(pref + CK.S_CRAFT_ITEMS) != null
                    ? context.getSessionData(pref + CK.S_CRAFT_ITEMS) : null);
            stage.set("items-to-smelt", context.getSessionData(pref + CK.S_SMELT_ITEMS) != null
                    ? context.getSessionData(pref + CK.S_SMELT_ITEMS) : null);
            stage.set("items-to-enchant", context.getSessionData(pref + CK.S_ENCHANT_ITEMS) != null
                    ? context.getSessionData(pref + CK.S_ENCHANT_ITEMS) : null);
            stage.set("items-to-brew", context.getSessionData(pref + CK.S_BREW_ITEMS) != null
                    ? context.getSessionData(pref + CK.S_BREW_ITEMS) : null);
            stage.set("items-to-consume", context.getSessionData(pref + CK.S_CONSUME_ITEMS) != null
                    ? context.getSessionData(pref + CK.S_CONSUME_ITEMS) : null);
            stage.set("cows-to-milk", context.getSessionData(pref + CK.S_COW_MILK) != null
                    ? context.getSessionData(pref + CK.S_COW_MILK) : null);
            stage.set("fish-to-catch", context.getSessionData(pref + CK.S_FISH) != null
                    ? context.getSessionData(pref + CK.S_FISH) : null);
            stage.set("players-to-kill", context.getSessionData(pref + CK.S_PLAYER_KILL) != null
                    ? context.getSessionData(pref + CK.S_PLAYER_KILL) : null);
            stage.set("items-to-deliver", context.getSessionData(pref + CK.S_DELIVERY_ITEMS) != null
                    ? context.getSessionData(pref + CK.S_DELIVERY_ITEMS) : null);
            stage.set("npc-delivery-ids", context.getSessionData(pref + CK.S_DELIVERY_NPCS) != null
                    ? context.getSessionData(pref + CK.S_DELIVERY_NPCS) : null);
            stage.set("delivery-messages", context.getSessionData(pref + CK.S_DELIVERY_MESSAGES) != null
                    ? context.getSessionData(pref + CK.S_DELIVERY_MESSAGES) : null);
            stage.set("npc-ids-to-talk-to", context.getSessionData(pref + CK.S_NPCS_TO_TALK_TO) != null
                    ? context.getSessionData(pref + CK.S_NPCS_TO_TALK_TO) : null);
            stage.set("npc-ids-to-kill", context.getSessionData(pref + CK.S_NPCS_TO_KILL) != null
                    ? context.getSessionData(pref + CK.S_NPCS_TO_KILL) : null);
            stage.set("npc-kill-amounts", context.getSessionData(pref + CK.S_NPCS_TO_KILL_AMOUNTS) != null
                    ? context.getSessionData(pref + CK.S_NPCS_TO_KILL_AMOUNTS) : null);
            stage.set("mobs-to-kill", context.getSessionData(pref + CK.S_MOB_TYPES) != null
                    ? context.getSessionData(pref + CK.S_MOB_TYPES) : null);
            stage.set("mob-amounts", context.getSessionData(pref + CK.S_MOB_AMOUNTS) != null
                    ? context.getSessionData(pref + CK.S_MOB_AMOUNTS) : null);
            stage.set("locations-to-kill", context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS) != null
                    ? context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS) : null);
            stage.set("kill-location-radii", context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_RADIUS) != null
                    ? context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_RADIUS) : null);
            stage.set("kill-location-names", context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_NAMES) != null
                    ? context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_NAMES) : null);
            stage.set("locations-to-reach", context.getSessionData(pref + CK.S_REACH_LOCATIONS) != null
                    ? context.getSessionData(pref + CK.S_REACH_LOCATIONS) : null);
            stage.set("reach-location-radii", context.getSessionData(pref + CK.S_REACH_LOCATIONS_RADIUS) != null
                    ? context.getSessionData(pref + CK.S_REACH_LOCATIONS_RADIUS) : null);
            stage.set("reach-location-names", context.getSessionData(pref + CK.S_REACH_LOCATIONS_NAMES) != null
                    ? context.getSessionData(pref + CK.S_REACH_LOCATIONS_NAMES) : null);
            stage.set("mobs-to-tame", context.getSessionData(pref + CK.S_TAME_TYPES) != null
                    ? context.getSessionData(pref + CK.S_TAME_TYPES) : null);
            stage.set("mob-tame-amounts", context.getSessionData(pref + CK.S_TAME_AMOUNTS) != null
                    ? context.getSessionData(pref + CK.S_TAME_AMOUNTS) : null);
            stage.set("sheep-to-shear", context.getSessionData(pref + CK.S_SHEAR_COLORS) != null
                    ? context.getSessionData(pref + CK.S_SHEAR_COLORS) : null);
            stage.set("sheep-amounts", context.getSessionData(pref + CK.S_SHEAR_AMOUNTS) != null
                    ? context.getSessionData(pref + CK.S_SHEAR_AMOUNTS) : null);
            stage.set("password-displays", context.getSessionData(pref + CK.S_PASSWORD_DISPLAYS) != null
                    ? context.getSessionData(pref + CK.S_PASSWORD_DISPLAYS) : null);
            stage.set("password-phrases", context.getSessionData(pref + CK.S_PASSWORD_PHRASES) != null
                    ? context.getSessionData(pref + CK.S_PASSWORD_PHRASES) : null);
            final LinkedList<String> customObj = (LinkedList<String>) context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES);
            final LinkedList<Integer> customObjCounts
                    = (LinkedList<Integer>) context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES_COUNT);
            final LinkedList<Entry<String, Object>> customObjData
                    = (LinkedList<Entry<String, Object>>) context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA);
            if (context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES) != null) {
                final ConfigurationSection sec = stage.createSection("custom-objectives");
                if (customObj == null || customObjCounts == null || customObjData == null) {
                    continue;
                }
                for (int index = 0; index < customObj.size(); index++) {
                    final ConfigurationSection sec2 = sec.createSection("custom" + (index + 1));
                    sec2.set("name", customObj.get(index));
                    sec2.set("count", customObjCounts.get(index));
                    ICustomObjective found = null;
                    for (final ICustomObjective co : plugin.getCustomObjectives()) {
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
            stage.set("script-to-run", context.getSessionData(pref + CK.S_DENIZEN) != null
                    ? context.getSessionData(pref + CK.S_DENIZEN) : null);
            stage.set("start-event", context.getSessionData(pref + CK.S_START_EVENT) != null
                    ? context.getSessionData(pref + CK.S_START_EVENT) : null);
            stage.set("finish-event", context.getSessionData(pref + CK.S_FINISH_EVENT) != null
                    ? context.getSessionData(pref + CK.S_FINISH_EVENT) : null);
            stage.set("fail-event", context.getSessionData(pref + CK.S_FAIL_EVENT) != null
                    ? context.getSessionData(pref + CK.S_FAIL_EVENT) : null);
            stage.set("death-event", context.getSessionData(pref + CK.S_DEATH_EVENT) != null
                    ? context.getSessionData(pref + CK.S_DEATH_EVENT) : null);
            stage.set("disconnect-event", context.getSessionData(pref + CK.S_DISCONNECT_EVENT) != null
                    ? context.getSessionData(pref + CK.S_DISCONNECT_EVENT) : null);
            stage.set("chat-events", context.getSessionData(pref + CK.S_CHAT_EVENTS) != null
                    ? context.getSessionData(pref + CK.S_CHAT_EVENTS) : null);
            stage.set("chat-event-triggers", context.getSessionData(pref + CK.S_CHAT_EVENT_TRIGGERS) != null
                    ? context.getSessionData(pref + CK.S_CHAT_EVENT_TRIGGERS) : null);
            stage.set("command-events", context.getSessionData(pref + CK.S_COMMAND_EVENTS) != null
                    ? context.getSessionData(pref + CK.S_COMMAND_EVENTS) : null);
            stage.set("command-event-triggers", context.getSessionData(pref + CK.S_COMMAND_EVENT_TRIGGERS) != null
                    ? context.getSessionData(pref + CK.S_COMMAND_EVENT_TRIGGERS) : null);
            stage.set("condition", context.getSessionData(pref + CK.S_CONDITION) != null
                    ? context.getSessionData(pref + CK.S_CONDITION) : null);
            final Long delay = (Long) context.getSessionData(pref + CK.S_DELAY);
            if (delay != null) {
                stage.set("delay", delay.intValue() / 1000);
            }
            final String delayMessage = (String) context.getSessionData(pref + CK.S_DELAY_MESSAGE);
            if (delayMessage != null) {
                stage.set("delay-message", delayMessage.replace("\\n", "\n"));
            }
            final String startMessage = (String) context.getSessionData(pref + CK.S_START_MESSAGE);
            if (startMessage != null) {
                stage.set("start-message", startMessage.replace("\\n", "\n"));
            }
            final String completeMessage = (String) context.getSessionData(pref + CK.S_COMPLETE_MESSAGE);
            if (completeMessage != null) {
                stage.set("complete-message", completeMessage.replace("\\n", "\n"));
            }
            stage.set("objective-override", context.getSessionData(pref + CK.S_OVERRIDE_DISPLAY) != null
                    ? context.getSessionData(pref + CK.S_OVERRIDE_DISPLAY) : null);
        }
    }

    @SuppressWarnings("unchecked")
    private void saveRewards(final ConversationContext context, final ConfigurationSection section) {
        final ConfigurationSection rewards = section.createSection("rewards");
        rewards.set("items", context.getSessionData(CK.REW_ITEMS) != null
                ? context.getSessionData(CK.REW_ITEMS) : null);
        rewards.set("money", context.getSessionData(CK.REW_MONEY) != null
                ? context.getSessionData(CK.REW_MONEY) : null);
        rewards.set("quest-points", context.getSessionData(CK.REW_QUEST_POINTS) != null
                ? context.getSessionData(CK.REW_QUEST_POINTS) : null);
        rewards.set("exp", context.getSessionData(CK.REW_EXP) != null
                ? context.getSessionData(CK.REW_EXP) : null);
        rewards.set("commands", context.getSessionData(CK.REW_COMMAND) != null
                ? context.getSessionData(CK.REW_COMMAND) : null);
        rewards.set("commands-override-display", context.getSessionData(CK.REW_COMMAND_OVERRIDE_DISPLAY) != null
                ? context.getSessionData(CK.REW_COMMAND_OVERRIDE_DISPLAY) : null);
        rewards.set("permissions", context.getSessionData(CK.REW_PERMISSION) != null
                ? context.getSessionData(CK.REW_PERMISSION) : null);
        rewards.set("permission-worlds", context.getSessionData(CK.REW_PERMISSION_WORLDS) != null
                ? context.getSessionData(CK.REW_PERMISSION_WORLDS) : null);
        rewards.set("mcmmo-skills", context.getSessionData(CK.REW_MCMMO_SKILLS) != null
                ? context.getSessionData(CK.REW_MCMMO_SKILLS) : null);
        rewards.set("mcmmo-levels", context.getSessionData(CK.REW_MCMMO_AMOUNTS) != null
                ? context.getSessionData(CK.REW_MCMMO_AMOUNTS) : null);
        rewards.set("heroes-exp-classes", context.getSessionData(CK.REW_HEROES_CLASSES) != null
                ? context.getSessionData(CK.REW_HEROES_CLASSES) : null);
        rewards.set("heroes-exp-amounts", context.getSessionData(CK.REW_HEROES_AMOUNTS) != null
                ? context.getSessionData(CK.REW_HEROES_AMOUNTS) : null);
        rewards.set("parties-experience", context.getSessionData(CK.REW_PARTIES_EXPERIENCE) != null
                ? context.getSessionData(CK.REW_PARTIES_EXPERIENCE) : null);
        rewards.set("phat-loots", context.getSessionData(CK.REW_PHAT_LOOTS) != null
                ? context.getSessionData(CK.REW_PHAT_LOOTS) : null);
        final LinkedList<String> customRewards = context.getSessionData(CK.REW_CUSTOM) != null
                ? (LinkedList<String>) context.getSessionData(CK.REW_CUSTOM) : null;
        final LinkedList<Map<String, Object>> customRewardsData = context.getSessionData(CK.REW_CUSTOM_DATA) != null
                ? (LinkedList<Map<String, Object>>) context.getSessionData(CK.REW_CUSTOM_DATA) : null;
        if (customRewards != null && customRewardsData != null) {
            final ConfigurationSection customRewardsSec = rewards.createSection("custom-rewards");
            for (int i = 0; i < customRewards.size(); i++) {
                final ConfigurationSection customRewSec = customRewardsSec.createSection("req" + (i + 1));
                customRewSec.set("name", customRewards.get(i));
                customRewSec.set("data", customRewardsData.get(i));
            }
        }
        rewards.set("details-override", context.getSessionData(CK.REW_DETAILS_OVERRIDE) != null
                ? context.getSessionData(CK.REW_DETAILS_OVERRIDE) : null);
        if (rewards.getKeys(false).isEmpty()) {
            section.set("rewards", null);
        }
    }

    private void savePlanner(final ConversationContext context, final ConfigurationSection section) {
        final ConfigurationSection pln = section.createSection("planner");
        pln.set("start", context.getSessionData(CK.PLN_START_DATE) != null
                ? context.getSessionData(CK.PLN_START_DATE) : null);
        pln.set("end", context.getSessionData(CK.PLN_END_DATE) != null
                ? context.getSessionData(CK.PLN_END_DATE) : null);
        final Long repeatCycle = (Long) context.getSessionData(CK.PLN_REPEAT_CYCLE);
        pln.set("repeat", repeatCycle != null ? (repeatCycle / 1000L) : null);
        final Long cooldown = (Long) context.getSessionData(CK.PLN_COOLDOWN);
        pln.set("cooldown", cooldown != null ? (cooldown / 1000L) : null);
        pln.set("override", context.getSessionData(CK.PLN_OVERRIDE) != null
                ? context.getSessionData(CK.PLN_OVERRIDE) : null);
        if (pln.getKeys(false).isEmpty()) {
            section.set("planner", null);
        }
    }

    private void saveOptions(final ConversationContext context, final ConfigurationSection section) {
        final ConfigurationSection opts = section.createSection("options");
        opts.set("allow-commands", context.getSessionData(CK.OPT_ALLOW_COMMANDS) != null
                ? context.getSessionData(CK.OPT_ALLOW_COMMANDS) : null);
        opts.set("allow-quitting", context.getSessionData(CK.OPT_ALLOW_QUITTING) != null
                ? context.getSessionData(CK.OPT_ALLOW_QUITTING) : null);
        opts.set("ignore-silk-touch", context.getSessionData(CK.OPT_IGNORE_SILK_TOUCH) != null
                ? context.getSessionData(CK.OPT_IGNORE_SILK_TOUCH) : null);
        opts.set("external-party-plugin", context.getSessionData(CK.OPT_EXTERNAL_PARTY_PLUGIN) != null
                ? context.getSessionData(CK.OPT_EXTERNAL_PARTY_PLUGIN) : null);
        opts.set("use-parties-plugin", context.getSessionData(CK.OPT_USE_PARTIES_PLUGIN) != null
                ? context.getSessionData(CK.OPT_USE_PARTIES_PLUGIN) : null);
        opts.set("share-progress-level", context.getSessionData(CK.OPT_SHARE_PROGRESS_LEVEL) != null
                ? context.getSessionData(CK.OPT_SHARE_PROGRESS_LEVEL) : null);
        opts.set("same-quest-only", context.getSessionData(CK.OPT_SHARE_SAME_QUEST_ONLY) != null
                ? context.getSessionData(CK.OPT_SHARE_SAME_QUEST_ONLY) : null);
        opts.set("share-distance", context.getSessionData(CK.OPT_SHARE_DISTANCE) != null
                ? context.getSessionData(CK.OPT_SHARE_DISTANCE) : null);
        opts.set("handle-offline-players", context.getSessionData(CK.OPT_HANDLE_OFFLINE_PLAYERS) != null
                ? context.getSessionData(CK.OPT_HANDLE_OFFLINE_PLAYERS) : null);
        if (opts.getKeys(false).isEmpty()) {
            section.set("options", null);
        }
    }
}