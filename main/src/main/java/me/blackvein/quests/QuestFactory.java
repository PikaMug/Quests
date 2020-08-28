/*******************************************************************************************************
 * Continued by PikaMug (formerly HappyPikachu) with permission from _Blackvein_. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

package me.blackvein.quests;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.blackvein.quests.convo.quests.main.QuestMainPrompt;
import me.blackvein.quests.convo.quests.menu.QuestMenuPrompt;
import me.blackvein.quests.convo.quests.stages.StageMenuPrompt;
import me.blackvein.quests.interfaces.ReloadCallback;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.ConfigUtil;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;

public class QuestFactory implements ConversationAbandonedListener {

    private final Quests plugin;
    private final ConversationFactory convoCreator;
    private Map<UUID, Block> selectedBlockStarts = new HashMap<UUID, Block>();
    private Map<UUID, Block> selectedKillLocations = new HashMap<UUID, Block>();
    private Map<UUID, Block> selectedReachLocations = new HashMap<UUID, Block>();
    private Set<UUID> selectingNpcs = new HashSet<UUID>();
    private List<String> editingQuestNames = new LinkedList<String>();
    
    public QuestFactory(final Quests plugin) {
        this.plugin = plugin;
        // Ensure to initialize convoCreator last so that 'this' is fully initialized before it is passed
        this.convoCreator = new ConversationFactory(plugin).withModality(false).withLocalEcho(false)
                .withFirstPrompt(new QuestMenuPrompt(new ConversationContext(plugin, null, null))).withTimeout(3600)
                .thatExcludesNonPlayersWithMessage("Console may not perform this operation!")
                .addConversationAbandonedListener(this);
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

    /**
     * @deprecated Use {@link#getNamesOfQuestsBeingEdited}
     */
    @Deprecated
    public List<String> getNames() {
        return editingQuestNames;
    }

    /**
     * @deprecated Use {@link#setNamesOfQuestsBeingEdited}
     */
    @Deprecated
    public void setNames(final List<String> names) {
        this.editingQuestNames = names;
    }
    
    public List<String> getNamesOfQuestsBeingEdited() {
        return editingQuestNames;
    }
    
    public void setNamesOfQuestsBeingEdited(final List<String> questNames) {
        this.editingQuestNames = questNames;
    }
    
    public ConversationFactory getConversationFactory() {
        return convoCreator;
    }

    @Override
    public void conversationAbandoned(final ConversationAbandonedEvent abandonedEvent) {
        if (abandonedEvent.getContext().getSessionData(CK.Q_NAME) != null) {
            editingQuestNames.remove(abandonedEvent.getContext().getSessionData(CK.Q_NAME));
        }
        final UUID uuid = ((Player) abandonedEvent.getContext().getForWhom()).getUniqueId();
        selectedBlockStarts.remove(uuid);
        selectedKillLocations.remove(uuid);
        selectedReachLocations.remove(uuid);
    }
    
    public Prompt returnToMenu(final ConversationContext context) {
        return new QuestMainPrompt(context);
    }
    
    @SuppressWarnings("deprecation")
    public void loadQuest(final ConversationContext context, final Quest q) {
        context.setSessionData(CK.ED_QUEST_EDIT, q.getName());
        context.setSessionData(CK.Q_ID, q.getId());
        context.setSessionData(CK.Q_NAME, q.getName());
        context.setSessionData(CK.Q_ASK_MESSAGE, q.getDescription());
        context.setSessionData(CK.Q_FINISH_MESSAGE, q.getFinished());
        if (q.getNpcStart() != null) {
            context.setSessionData(CK.Q_START_NPC, q.getNpcStart().getId());
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
        final Requirements reqs = q.getRequirements();
        if (reqs.getMoney() != 0) {
            context.setSessionData(CK.REQ_MONEY, reqs.getMoney());
        }
        if (reqs.getQuestPoints() != 0) {
            context.setSessionData(CK.REQ_QUEST_POINTS, reqs.getQuestPoints());
        }
        if (reqs.getItems().isEmpty() == false) {
            context.setSessionData(CK.REQ_ITEMS, reqs.getItems());
            context.setSessionData(CK.REQ_ITEMS_REMOVE, reqs.getRemoveItems());
        }
        if (reqs.getNeededQuests().isEmpty() == false) {
            context.setSessionData(CK.REQ_QUEST, reqs.getNeededQuests());
        }
        if (reqs.getBlockQuests().isEmpty() == false) {
            context.setSessionData(CK.REQ_QUEST_BLOCK, reqs.getBlockQuests());
        }
        if (reqs.getMcmmoSkills().isEmpty() == false) {
            context.setSessionData(CK.REQ_MCMMO_SKILLS, reqs.getMcmmoAmounts());
            context.setSessionData(CK.REQ_MCMMO_SKILL_AMOUNTS, reqs.getMcmmoAmounts());
        }
        if (reqs.getPermissions().isEmpty() == false) {
            context.setSessionData(CK.REQ_PERMISSION, reqs.getPermissions());
        }
        if (reqs.getHeroesPrimaryClass() != null) {
            context.setSessionData(CK.REQ_HEROES_PRIMARY_CLASS, reqs.getHeroesPrimaryClass());
        }
        if (reqs.getHeroesSecondaryClass() != null) {
            context.setSessionData(CK.REQ_HEROES_SECONDARY_CLASS, reqs.getHeroesSecondaryClass());
        }
        if (reqs.getCustomRequirements().isEmpty() == false) {
            final LinkedList<String> list = new LinkedList<String>();
            final LinkedList<Map<String, Object>> datamapList = new LinkedList<Map<String, Object>>();
            for (final Entry<String, Map<String, Object>> entry : reqs.getCustomRequirements().entrySet()) {
                list.add(entry.getKey());
                datamapList.add(entry.getValue());
            }
            context.setSessionData(CK.REQ_CUSTOM, list);
            context.setSessionData(CK.REQ_CUSTOM_DATA, datamapList);
        }
        if (reqs.getDetailsOverride().isEmpty() == false) {
            context.setSessionData(CK.REQ_FAIL_MESSAGE, reqs.getDetailsOverride());
        }
        final Rewards rews = q.getRewards();
        if (rews.getMoney() != 0) {
            context.setSessionData(CK.REW_MONEY, rews.getMoney());
        }
        if (rews.getQuestPoints() != 0) {
            context.setSessionData(CK.REW_QUEST_POINTS, rews.getQuestPoints());
        }
        if (rews.getExp() != 0) {
            context.setSessionData(CK.REW_EXP, rews.getExp());
        }
        if (rews.getItems().isEmpty() == false) {
            context.setSessionData(CK.REW_ITEMS, rews.getItems());
        }
        if (rews.getCommands().isEmpty() == false) {
            context.setSessionData(CK.REW_COMMAND, rews.getCommands());
        }
        if (rews.getCommandsOverrideDisplay().isEmpty() == false) {
            context.setSessionData(CK.REW_COMMAND_OVERRIDE_DISPLAY, rews.getCommandsOverrideDisplay());
        }
        if (rews.getPermissions().isEmpty() == false) {
            context.setSessionData(CK.REW_PERMISSION, rews.getPermissions());
        }
        if (rews.getPermissions().isEmpty() == false) {
            context.setSessionData(CK.REW_PERMISSION_WORLDS, rews.getPermissionWorlds());
        }
        if (rews.getMcmmoSkills().isEmpty() == false) {
            context.setSessionData(CK.REW_MCMMO_SKILLS, rews.getMcmmoSkills());
            context.setSessionData(CK.REW_MCMMO_AMOUNTS, rews.getMcmmoAmounts());
        }
        if (rews.getHeroesClasses().isEmpty() == false) {
            context.setSessionData(CK.REW_HEROES_CLASSES, rews.getHeroesClasses());
            context.setSessionData(CK.REW_HEROES_AMOUNTS, rews.getHeroesAmounts());
        }
        if (rews.getPhatLoots().isEmpty() == false) {
            context.setSessionData(CK.REW_PHAT_LOOTS, rews.getPhatLoots());
        }
        if (rews.getCustomRewards().isEmpty() == false) {
            context.setSessionData(CK.REW_CUSTOM, new LinkedList<String>(rews.getCustomRewards().keySet()));
            context.setSessionData(CK.REW_CUSTOM_DATA, new LinkedList<Object>(rews.getCustomRewards().values()));
        }
        if (rews.getDetailsOverride().isEmpty() == false) {
            context.setSessionData(CK.REW_DETAILS_OVERRIDE, rews.getDetailsOverride());
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
        context.setSessionData(CK.OPT_ALLOW_COMMANDS, opt.getAllowCommands());
        context.setSessionData(CK.OPT_ALLOW_QUITTING, opt.getAllowQuitting());
        context.setSessionData(CK.OPT_USE_DUNGEONSXL_PLUGIN, opt.getUseDungeonsXLPlugin());
        context.setSessionData(CK.OPT_USE_PARTIES_PLUGIN, opt.getUsePartiesPlugin());
        context.setSessionData(CK.OPT_SHARE_PROGRESS_LEVEL, opt.getShareProgressLevel());
        context.setSessionData(CK.OPT_REQUIRE_SAME_QUEST, opt.getRequireSameQuest());
        // Stages (Objectives)
        int index = 1;
        for (final Stage stage : q.getStages()) {
            final String pref = "stage" + index;
            index++;
            context.setSessionData(pref, Boolean.TRUE);
            if (!stage.getBlocksToBreak().isEmpty()) {
                final LinkedList<String> names = new LinkedList<String>();
                final LinkedList<Integer> amnts = new LinkedList<Integer>();
                final LinkedList<Short> durab = new LinkedList<Short>();
                for (final ItemStack e : stage.getBlocksToBreak()) {
                    names.add(e.getType().name());
                    amnts.add(e.getAmount());
                    durab.add(e.getDurability());
                }
                context.setSessionData(pref + CK.S_BREAK_NAMES, names);
                context.setSessionData(pref + CK.S_BREAK_AMOUNTS, amnts);
                context.setSessionData(pref + CK.S_BREAK_DURABILITY, durab);
            }
            if (!stage.getBlocksToDamage().isEmpty()) {
                final LinkedList<String> names = new LinkedList<String>();
                final LinkedList<Integer> amnts = new LinkedList<Integer>();
                final LinkedList<Short> durab = new LinkedList<Short>();
                for (final ItemStack e : stage.getBlocksToDamage()) {
                    names.add(e.getType().name());
                    amnts.add(e.getAmount());
                    durab.add(e.getDurability());
                }
                context.setSessionData(pref + CK.S_DAMAGE_NAMES, names);
                context.setSessionData(pref + CK.S_DAMAGE_AMOUNTS, amnts);
                context.setSessionData(pref + CK.S_DAMAGE_DURABILITY, durab);
            }
            if (!stage.getBlocksToPlace().isEmpty()) {
                final LinkedList<String> names = new LinkedList<String>();
                final LinkedList<Integer> amnts = new LinkedList<Integer>();
                final LinkedList<Short> durab = new LinkedList<Short>();
                for (final ItemStack e : stage.getBlocksToPlace()) {
                    names.add(e.getType().name());
                    amnts.add(e.getAmount());
                    durab.add(e.getDurability());
                }
                context.setSessionData(pref + CK.S_PLACE_NAMES, names);
                context.setSessionData(pref + CK.S_PLACE_AMOUNTS, amnts);
                context.setSessionData(pref + CK.S_PLACE_DURABILITY, durab);
            }
            if (!stage.getBlocksToUse().isEmpty()) {
                final LinkedList<String> names = new LinkedList<String>();
                final LinkedList<Integer> amnts = new LinkedList<Integer>();
                final LinkedList<Short> durab = new LinkedList<Short>();
                for (final ItemStack e : stage.getBlocksToUse()) {
                    names.add(e.getType().name());
                    amnts.add(e.getAmount());
                    durab.add(e.getDurability());
                }
                context.setSessionData(pref + CK.S_USE_NAMES, names);
                context.setSessionData(pref + CK.S_USE_AMOUNTS, amnts);
                context.setSessionData(pref + CK.S_USE_DURABILITY, durab);
            }
            if (!stage.getBlocksToCut().isEmpty()) {
                final LinkedList<String> names = new LinkedList<String>();
                final LinkedList<Integer> amnts = new LinkedList<Integer>();
                final LinkedList<Short> durab = new LinkedList<Short>();
                for (final ItemStack e : stage.getBlocksToCut()) {
                    names.add(e.getType().name());
                    amnts.add(e.getAmount());
                    durab.add(e.getDurability());
                }
                context.setSessionData(pref + CK.S_CUT_NAMES, names);
                context.setSessionData(pref + CK.S_CUT_AMOUNTS, amnts);
                context.setSessionData(pref + CK.S_CUT_DURABILITY, durab);
            }
            if (!stage.getItemsToCraft().isEmpty()) {
                final LinkedList<ItemStack> items = new LinkedList<ItemStack>();
                for (final ItemStack is : stage.getItemsToCraft()) {
                    items.add(is);
                }
                context.setSessionData(pref + CK.S_CRAFT_ITEMS, items);
            }
            if (!stage.getItemsToSmelt().isEmpty()) {
                final LinkedList<ItemStack> items = new LinkedList<ItemStack>();
                for (final ItemStack is : stage.getItemsToSmelt()) {
                    items.add(is);
                }
                context.setSessionData(pref + CK.S_SMELT_ITEMS, items);
            }
            if (!stage.getItemsToEnchant().isEmpty()) {
                final LinkedList<String> enchants = new LinkedList<String>();
                final LinkedList<String> names = new LinkedList<String>();
                final LinkedList<Integer> amounts = new LinkedList<Integer>();
                for (final Entry<Map<Enchantment, Material>, Integer> e : stage.getItemsToEnchant().entrySet()) {
                    amounts.add(e.getValue());
                    for (final Entry<Enchantment, Material> e2 : e.getKey().entrySet()) {
                        names.add(e2.getValue().name());
                        enchants.add(ItemUtil.getPrettyEnchantmentName(e2.getKey()));
                    }
                }
                context.setSessionData(pref + CK.S_ENCHANT_TYPES, enchants);
                context.setSessionData(pref + CK.S_ENCHANT_NAMES, names);
                context.setSessionData(pref + CK.S_ENCHANT_AMOUNTS, amounts);
            }
            if (!stage.getItemsToBrew().isEmpty()) {
                final LinkedList<ItemStack> items = new LinkedList<ItemStack>();
                for (final ItemStack is : stage.getItemsToBrew()) {
                    items.add(is);
                }
                context.setSessionData(pref + CK.S_BREW_ITEMS, items);
            }
            if (!stage.getItemsToConsume().isEmpty()) {
                final LinkedList<ItemStack> items = new LinkedList<ItemStack>();
                for (final ItemStack is : stage.getItemsToConsume()) {
                    items.add(is);
                }
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
                final LinkedList<ItemStack> items = new LinkedList<ItemStack>();
                final LinkedList<Integer> npcs = new LinkedList<Integer>();
                for (final ItemStack is : stage.getItemsToDeliver()) {
                    items.add(is);
                }
                for (final Integer n : stage.getItemDeliveryTargets()) {
                    npcs.add(n);
                }
                context.setSessionData(pref + CK.S_DELIVERY_ITEMS, items);
                context.setSessionData(pref + CK.S_DELIVERY_NPCS, npcs);
                context.setSessionData(pref + CK.S_DELIVERY_MESSAGES, stage.getDeliverMessages());
            }
            if (!stage.getCitizensToInteract().isEmpty()) {
                final LinkedList<Integer> npcs = new LinkedList<Integer>();
                for (final Integer n : stage.getCitizensToInteract()) {
                    npcs.add(n);
                }
                context.setSessionData(pref + CK.S_NPCS_TO_TALK_TO, npcs);
            }
            if (!stage.getCitizensToKill().isEmpty()) {
                final LinkedList<Integer> npcs = new LinkedList<Integer>();
                for (final Integer n : stage.getCitizensToKill()) {
                    npcs.add(n);
                }
                context.setSessionData(pref + CK.S_NPCS_TO_KILL, npcs);
                context.setSessionData(pref + CK.S_NPCS_TO_KILL_AMOUNTS, stage.getCitizenNumToKill());
            }
            if (!stage.getMobsToKill().isEmpty()) {
                final LinkedList<String> mobs = new LinkedList<String>();
                for (final EntityType et : stage.getMobsToKill()) {
                    mobs.add(MiscUtil.getPrettyMobName(et));
                }
                context.setSessionData(pref + CK.S_MOB_TYPES, mobs);
                context.setSessionData(pref + CK.S_MOB_AMOUNTS, stage.getMobNumToKill());
                if (!stage.getLocationsToKillWithin().isEmpty()) {
                    final LinkedList<String> locs = new LinkedList<String>();
                    for (final Location l : stage.getLocationsToKillWithin()) {
                        locs.add(ConfigUtil.getLocationInfo(l));
                    }
                    context.setSessionData(pref + CK.S_MOB_KILL_LOCATIONS, locs);
                    context.setSessionData(pref + CK.S_MOB_KILL_LOCATIONS_RADIUS, stage.getRadiiToKillWithin());
                    context.setSessionData(pref + CK.S_MOB_KILL_LOCATIONS_NAMES, stage.getKillNames());
                }
            }
            if (!stage.getLocationsToReach().isEmpty()) {
                final LinkedList<String> locs = new LinkedList<String>();
                for (final Location l : stage.getLocationsToReach()) {
                    locs.add(ConfigUtil.getLocationInfo(l));
                }
                context.setSessionData(pref + CK.S_REACH_LOCATIONS, locs);
                context.setSessionData(pref + CK.S_REACH_LOCATIONS_RADIUS, stage.getRadiiToReachWithin());
                context.setSessionData(pref + CK.S_REACH_LOCATIONS_NAMES, stage.getLocationNames());
            }
            if (!stage.getMobsToTame().isEmpty()) {
                final LinkedList<String> mobs = new LinkedList<String>();
                final LinkedList<Integer> amnts = new LinkedList<Integer>();
                for (final Entry<EntityType, Integer> e : stage.getMobsToTame().entrySet()) {
                    mobs.add(MiscUtil.getPrettyMobName(e.getKey()));
                    amnts.add(e.getValue());
                }
                context.setSessionData(pref + CK.S_TAME_TYPES, mobs);
                context.setSessionData(pref + CK.S_TAME_AMOUNTS, amnts);
            }
            if (!stage.getSheepToShear().isEmpty()) {
                final LinkedList<String> colors = new LinkedList<String>();
                final LinkedList<Integer> amnts = new LinkedList<Integer>();
                for (final Entry<DyeColor, Integer> e : stage.getSheepToShear().entrySet()) {
                    colors.add(MiscUtil.getPrettyDyeColorName(e.getKey()));
                    amnts.add(e.getValue());
                }
                context.setSessionData(pref + CK.S_SHEAR_COLORS, colors);
                context.setSessionData(pref + CK.S_SHEAR_AMOUNTS, amnts);
            }
            if (!stage.getPasswordDisplays().isEmpty()) {
                context.setSessionData(pref + CK.S_PASSWORD_DISPLAYS, stage.getPasswordDisplays());
                context.setSessionData(pref + CK.S_PASSWORD_PHRASES, stage.getPasswordPhrases());
            }
            if (!stage.getCustomObjectives().isEmpty()) {
                final LinkedList<String> list = new LinkedList<String>();
                final LinkedList<Integer> countList = new LinkedList<Integer>();
                final LinkedList<Entry<String, Object>> datamapList = new LinkedList<Entry<String, Object>>();
                for (int i = 0; i < stage.getCustomObjectives().size(); i++) {
                    list.add(stage.getCustomObjectives().get(i).getName());
                    countList.add(stage.getCustomObjectiveCounts().get(i));
                }
                datamapList.addAll(stage.getCustomObjectiveData());
                context.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES, list);
                context.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES_COUNT, countList);
                context.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA, datamapList);
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
                final LinkedList<String> chatEvents = new LinkedList<String>();
                final LinkedList<String> chatEventTriggers = new LinkedList<String>();
                for (final String s : stage.getChatActions().keySet()) {
                    chatEventTriggers.add(s);
                    chatEvents.add(stage.getChatActions().get(s).getName());
                }
                context.setSessionData(pref + CK.S_CHAT_EVENTS, chatEvents);
                context.setSessionData(pref + CK.S_CHAT_EVENT_TRIGGERS, chatEventTriggers);
            }
            if (!stage.getCommandActions().isEmpty()) {
                final LinkedList<String> commandEvents = new LinkedList<String>();
                final LinkedList<String> commandEventTriggers = new LinkedList<String>();
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
        } catch (final IOException e) {
            e.printStackTrace();
            ((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("questErrorReadingFile")
                    .replace("<quest>", questsFile.getName()));
            return;
        } catch (final InvalidConfigurationException e) {
            e.printStackTrace();
            ((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("questErrorReadingFile")
                    .replace("<quest>", questsFile.getName()));
            return;
        }
        final String quest = (String) context.getSessionData(CK.ED_QUEST_DELETE);
        final ConfigurationSection sec = data.getConfigurationSection("quests");
        for (final String key : sec.getKeys(false)) {
            if (sec.getString(key + ".name").equalsIgnoreCase(quest)) {
                sec.set(key, null);
                break;
            }
        }
        try {
            data.save(questsFile);
        } catch (final IOException e) {
            ((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("questSaveError"));
            return;
        }
        final ReloadCallback<Boolean> callback = new ReloadCallback<Boolean>() {
            @Override
            public void execute(final Boolean response) {
                if (!response) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("unknownError"));
                }
            }
        };
        plugin.reload(callback);
        context.getForWhom().sendRawMessage(ChatColor.GREEN + Lang.get("questDeleted"));
    }

    public void saveQuest(final ConversationContext context, final ConfigurationSection section) {
        String edit = null;
        if (context.getSessionData(CK.ED_QUEST_EDIT) != null) {
            edit = (String) context.getSessionData(CK.ED_QUEST_EDIT);
        }
        if (edit != null) {
            final ConfigurationSection questList = section.getParent();
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
        section.set("name", context.getSessionData(CK.Q_NAME) != null 
                ? (String) context.getSessionData(CK.Q_NAME) : null);
        section.set("ask-message", context.getSessionData(CK.Q_ASK_MESSAGE) != null 
                ? (String) context.getSessionData(CK.Q_ASK_MESSAGE) : null);
        section.set("finish-message", context.getSessionData(CK.Q_FINISH_MESSAGE) != null 
                ? (String) context.getSessionData(CK.Q_FINISH_MESSAGE) : null);
        section.set("npc-giver-id", context.getSessionData(CK.Q_START_NPC) != null 
                ? (Integer) context.getSessionData(CK.Q_START_NPC) : null);
        section.set("block-start", context.getSessionData(CK.Q_START_BLOCK) != null 
                ? ConfigUtil.getLocationInfo((Location) context.getSessionData(CK.Q_START_BLOCK)) : null);
        section.set("event", context.getSessionData(CK.Q_INITIAL_EVENT) != null 
                ? (String) context.getSessionData(CK.Q_INITIAL_EVENT) : null);
        section.set("region", context.getSessionData(CK.Q_REGION) != null 
                ? (String) context.getSessionData(CK.Q_REGION) : null);
        section.set("gui-display", context.getSessionData(CK.Q_GUIDISPLAY) != null 
                ? (ItemStack) context.getSessionData(CK.Q_GUIDISPLAY) : null);
        saveRequirements(context, section);
        saveStages(context, section);
        saveRewards(context, section);
        savePlanner(context, section);
        saveOptions(context, section);
    }
    
    @SuppressWarnings("unchecked")
    private void saveRequirements(final ConversationContext context, final ConfigurationSection section) {
        final ConfigurationSection reqs = section.createSection("requirements");
        reqs.set("money", context.getSessionData(CK.REQ_MONEY) != null 
                ? (Integer) context.getSessionData(CK.REQ_MONEY) : null);
        reqs.set("quest-points", context.getSessionData(CK.REQ_QUEST_POINTS) != null 
                ? (Integer) context.getSessionData(CK.REQ_QUEST_POINTS) : null);
        reqs.set("items", context.getSessionData(CK.REQ_ITEMS) != null 
                ? (List<ItemStack>) context.getSessionData(CK.REQ_ITEMS) : null);
        reqs.set("remove-items", context.getSessionData(CK.REQ_ITEMS_REMOVE) != null 
                ? (List<Boolean>) context.getSessionData(CK.REQ_ITEMS_REMOVE) : null);
        reqs.set("permissions", context.getSessionData(CK.REQ_PERMISSION) != null 
                ? (List<String>) context.getSessionData(CK.REQ_PERMISSION) : null);
        reqs.set("quests", context.getSessionData(CK.REQ_QUEST) != null 
                ? (List<String>) context.getSessionData(CK.REQ_QUEST) : null);
        reqs.set("quest-blocks", context.getSessionData(CK.REQ_QUEST_BLOCK) != null 
                ? (List<String>) context.getSessionData(CK.REQ_QUEST_BLOCK) : null);
        reqs.set("mcmmo-skills", context.getSessionData(CK.REQ_MCMMO_SKILLS) != null 
                ? (List<String>) context.getSessionData(CK.REQ_MCMMO_SKILLS) : null);
        reqs.set("mcmmo-amounts", context.getSessionData(CK.REQ_MCMMO_SKILL_AMOUNTS) != null 
                ? (List<Integer>) context.getSessionData(CK.REQ_MCMMO_SKILL_AMOUNTS) : null);
        reqs.set("heroes-primary-class", context.getSessionData(CK.REQ_HEROES_PRIMARY_CLASS) != null 
                ? (String) context.getSessionData(CK.REQ_HEROES_PRIMARY_CLASS) : null);
        reqs.set("heroes-secondary-class", context.getSessionData(CK.REQ_HEROES_SECONDARY_CLASS) != null 
                ? (String) context.getSessionData(CK.REQ_HEROES_SECONDARY_CLASS) : null);
        final LinkedList<String> customReqs = context.getSessionData(CK.REQ_CUSTOM) != null 
                ? (LinkedList<String>) context.getSessionData(CK.REQ_CUSTOM) : null;
        final LinkedList<Map<String, Object>> customReqsData = context.getSessionData(CK.REQ_CUSTOM_DATA) != null 
                ? (LinkedList<Map<String, Object>>) context.getSessionData(CK.REQ_CUSTOM_DATA) : null;
        if (customReqs != null) {
            final ConfigurationSection customReqsSec = reqs.createSection("custom-requirements");
            for (int i = 0; i < customReqs.size(); i++) {
                final ConfigurationSection customReqSec = customReqsSec.createSection("req" + (i + 1));
                customReqSec.set("name", customReqs.get(i));
                customReqSec.set("data", customReqsData.get(i));
            }
        }
        reqs.set("fail-requirement-message", context.getSessionData(CK.REQ_FAIL_MESSAGE) != null 
                ? (List<String>)context.getSessionData(CK.REQ_FAIL_MESSAGE) : null);
        if (reqs.getKeys(false).isEmpty()) {
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
                    ? (LinkedList<Integer>) context.getSessionData(pref + CK.S_BREAK_NAMES) : null);
            stage.set("break-block-amounts", context.getSessionData(pref + CK.S_BREAK_AMOUNTS) != null 
                    ? (LinkedList<Integer>) context.getSessionData(pref + CK.S_BREAK_AMOUNTS) : null);
            stage.set("break-block-durability", context.getSessionData(pref + CK.S_BREAK_DURABILITY) != null 
                    ? (LinkedList<Short>) context.getSessionData(pref + CK.S_BREAK_DURABILITY) : null);
            stage.set("damage-block-names", context.getSessionData(pref + CK.S_DAMAGE_NAMES) != null 
                    ? (LinkedList<Integer>) context.getSessionData(pref + CK.S_DAMAGE_NAMES) : null);
            stage.set("damage-block-amounts", context.getSessionData(pref + CK.S_DAMAGE_AMOUNTS) != null 
                    ? (LinkedList<Integer>) context.getSessionData(pref + CK.S_DAMAGE_AMOUNTS) : null);
            stage.set("damage-block-durability", context.getSessionData(pref + CK.S_DAMAGE_DURABILITY) != null 
                    ? (LinkedList<Short>) context.getSessionData(pref + CK.S_DAMAGE_DURABILITY) : null);
            stage.set("place-block-names", context.getSessionData(pref + CK.S_PLACE_NAMES) != null 
                    ? (LinkedList<Integer>) context.getSessionData(pref + CK.S_PLACE_NAMES) : null);
            stage.set("place-block-amounts", context.getSessionData(pref + CK.S_PLACE_AMOUNTS) != null 
                    ? (LinkedList<Integer>) context.getSessionData(pref + CK.S_PLACE_AMOUNTS) : null);
            stage.set("place-block-durability", context.getSessionData(pref + CK.S_PLACE_DURABILITY) != null 
                    ? (LinkedList<Short>) context.getSessionData(pref + CK.S_PLACE_DURABILITY) : null);
            stage.set("use-block-names", context.getSessionData(pref + CK.S_USE_NAMES) != null 
                    ? (LinkedList<Integer>) context.getSessionData(pref + CK.S_USE_NAMES) : null);
            stage.set("use-block-amounts", context.getSessionData(pref + CK.S_USE_AMOUNTS) != null 
                    ? (LinkedList<Integer>) context.getSessionData(pref + CK.S_USE_AMOUNTS) : null);
            stage.set("use-block-durability", context.getSessionData(pref + CK.S_USE_DURABILITY) != null 
                    ? (LinkedList<Short>) context.getSessionData(pref + CK.S_USE_DURABILITY) : null);
            stage.set("cut-block-names", context.getSessionData(pref + CK.S_CUT_NAMES) != null 
                    ? (LinkedList<Integer>) context.getSessionData(pref + CK.S_CUT_NAMES) : null);
            stage.set("cut-block-amounts", context.getSessionData(pref + CK.S_CUT_AMOUNTS) != null 
                    ? (LinkedList<Integer>) context.getSessionData(pref + CK.S_CUT_AMOUNTS) : null);
            stage.set("cut-block-durability", context.getSessionData(pref + CK.S_CUT_DURABILITY) != null 
                    ? (LinkedList<Short>) context.getSessionData(pref + CK.S_CUT_DURABILITY) : null);
            stage.set("items-to-craft", context.getSessionData(pref + CK.S_CRAFT_ITEMS) != null 
                    ? (LinkedList<ItemStack>) context.getSessionData(pref + CK.S_CRAFT_ITEMS) : null);
            stage.set("items-to-smelt", context.getSessionData(pref + CK.S_SMELT_ITEMS) != null 
                    ? (LinkedList<ItemStack>) context.getSessionData(pref + CK.S_SMELT_ITEMS) : null);
            stage.set("enchantments", context.getSessionData(pref + CK.S_ENCHANT_TYPES) != null 
                    ? (LinkedList<String>) context.getSessionData(pref + CK.S_ENCHANT_TYPES) : null);
            stage.set("enchantment-item-names", context.getSessionData(pref + CK.S_ENCHANT_NAMES) != null 
                    ? (LinkedList<Integer>) context.getSessionData(pref + CK.S_ENCHANT_NAMES) : null);
            stage.set("enchantment-amounts", context.getSessionData(pref + CK.S_ENCHANT_AMOUNTS) != null 
                    ? (LinkedList<Integer>) context.getSessionData(pref + CK.S_ENCHANT_AMOUNTS) : null);
            stage.set("items-to-brew", context.getSessionData(pref + CK.S_BREW_ITEMS) != null 
                    ? (LinkedList<ItemStack>) context.getSessionData(pref + CK.S_BREW_ITEMS) : null);
            stage.set("items-to-consume", context.getSessionData(pref + CK.S_CONSUME_ITEMS) != null 
                    ? (LinkedList<ItemStack>) context.getSessionData(pref + CK.S_CONSUME_ITEMS) : null);
            stage.set("cows-to-milk", context.getSessionData(pref + CK.S_COW_MILK) != null 
                    ? (Integer) context.getSessionData(pref + CK.S_COW_MILK) : null);
            stage.set("fish-to-catch", context.getSessionData(pref + CK.S_FISH) != null 
                    ? (Integer) context.getSessionData(pref + CK.S_FISH) : null);
            stage.set("players-to-kill", context.getSessionData(pref + CK.S_PLAYER_KILL) != null 
                    ? (Integer) context.getSessionData(pref + CK.S_PLAYER_KILL) : null);
            stage.set("items-to-deliver", context.getSessionData(pref + CK.S_DELIVERY_ITEMS) != null 
                    ? (LinkedList<ItemStack>) context.getSessionData(pref + CK.S_DELIVERY_ITEMS) : null);
            stage.set("npc-delivery-ids", context.getSessionData(pref + CK.S_DELIVERY_NPCS) != null 
                    ? (LinkedList<Integer>) context.getSessionData(pref + CK.S_DELIVERY_NPCS) : null);
            stage.set("delivery-messages", context.getSessionData(pref + CK.S_DELIVERY_MESSAGES) != null 
                    ? (LinkedList<String>) context.getSessionData(pref + CK.S_DELIVERY_MESSAGES) : null);
            stage.set("npc-ids-to-talk-to", context.getSessionData(pref + CK.S_NPCS_TO_TALK_TO) != null 
                    ? (LinkedList<Integer>) context.getSessionData(pref + CK.S_NPCS_TO_TALK_TO) : null);
            stage.set("npc-ids-to-kill", context.getSessionData(pref + CK.S_NPCS_TO_KILL) != null 
                    ? (LinkedList<Integer>) context.getSessionData(pref + CK.S_NPCS_TO_KILL) : null);
            stage.set("npc-kill-amounts", context.getSessionData(pref + CK.S_NPCS_TO_KILL_AMOUNTS) != null 
                    ? (LinkedList<Integer>) context.getSessionData(pref + CK.S_NPCS_TO_KILL_AMOUNTS) : null);
            stage.set("mobs-to-kill", context.getSessionData(pref + CK.S_MOB_TYPES) != null 
                    ? (LinkedList<String>) context.getSessionData(pref + CK.S_MOB_TYPES) : null);
            stage.set("mob-amounts", context.getSessionData(pref + CK.S_MOB_AMOUNTS) != null 
                    ? (LinkedList<Integer>) context.getSessionData(pref + CK.S_MOB_AMOUNTS) : null);
            stage.set("locations-to-kill", context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS) != null 
                    ? (LinkedList<String>) context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS) : null);
            stage.set("kill-location-radii", context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_RADIUS) != null 
                    ? (LinkedList<Integer>) context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_RADIUS) : null);
            stage.set("kill-location-names", context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_NAMES) != null 
                    ? (LinkedList<String>) context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_NAMES) : null);
            stage.set("locations-to-reach", context.getSessionData(pref + CK.S_REACH_LOCATIONS) != null 
                    ? (LinkedList<String>) context.getSessionData(pref + CK.S_REACH_LOCATIONS) : null);
            stage.set("reach-location-radii", context.getSessionData(pref + CK.S_REACH_LOCATIONS_RADIUS) != null 
                    ? (LinkedList<Integer>) context.getSessionData(pref + CK.S_REACH_LOCATIONS_RADIUS) : null);
            stage.set("reach-location-names", context.getSessionData(pref + CK.S_REACH_LOCATIONS_NAMES) != null 
                    ? (LinkedList<String>) context.getSessionData(pref + CK.S_REACH_LOCATIONS_NAMES) : null);
            stage.set("mobs-to-tame", context.getSessionData(pref + CK.S_TAME_TYPES) != null 
                    ? (LinkedList<String>) context.getSessionData(pref + CK.S_TAME_TYPES) : null);
            stage.set("mob-tame-amounts", context.getSessionData(pref + CK.S_TAME_AMOUNTS) != null 
                    ? (LinkedList<Integer>) context.getSessionData(pref + CK.S_TAME_AMOUNTS) : null);
            stage.set("sheep-to-shear", context.getSessionData(pref + CK.S_SHEAR_COLORS) != null 
                    ? (LinkedList<String>) context.getSessionData(pref + CK.S_SHEAR_COLORS) : null);
            stage.set("sheep-amounts", context.getSessionData(pref + CK.S_SHEAR_AMOUNTS) != null 
                    ? (LinkedList<Integer>) context.getSessionData(pref + CK.S_SHEAR_AMOUNTS) : null);
            stage.set("password-displays", context.getSessionData(pref + CK.S_PASSWORD_DISPLAYS) != null 
                    ? (LinkedList<String>) context.getSessionData(pref + CK.S_PASSWORD_DISPLAYS) : null);
            final LinkedList<LinkedList<String>> passPhrases 
                    = (LinkedList<LinkedList<String>>) context.getSessionData(pref + CK.S_PASSWORD_PHRASES);
            if (context.getSessionData(pref + CK.S_PASSWORD_PHRASES) != null) {
                final LinkedList<String> toPut = new LinkedList<String>();
                for (final LinkedList<String> list : passPhrases) {
                    String combine = "";
                    for (final String s : list) {
                        if (list.getLast().equals(s) == false) {
                            combine += s + "|";
                        } else {
                            combine += s;
                        }
                    }
                    toPut.add(combine);
                }
                stage.set("password-phrases", toPut);
            }
            final LinkedList<String> customObjs = (LinkedList<String>) context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES);
            final LinkedList<Integer> customObjCounts 
                    = (LinkedList<Integer>) context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES_COUNT);
            final LinkedList<Entry<String, Object>> customObjsData 
                    = (LinkedList<Entry<String, Object>>) context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA);
            if (context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES) != null) {
                final ConfigurationSection sec = stage.createSection("custom-objectives");
                for (int index = 0; index < customObjs.size(); index++) {
                    final ConfigurationSection sec2 = sec.createSection("custom" + (index + 1));
                    sec2.set("name", customObjs.get(index));
                    sec2.set("count", customObjCounts.get(index));
                    CustomObjective found = null;
                    for (final CustomObjective co : plugin.getCustomObjectives()) {
                        if (co.getName().equals(customObjs.get(index))) {
                            found = co;
                            break;
                        }
                    }
                    final ConfigurationSection sec3 = sec2.createSection("data");
                    for (final Entry<String, Object> datamap : found.getData()) {
                        for (final Entry<String, Object> e : customObjsData) {
                            if (e.getKey().equals(datamap.getKey())) {
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
            if (context.getSessionData(pref + CK.S_DELAY) != null) {
                stage.set("delay", delay.intValue() / 1000);
            }
            final String delayMessage = (String) context.getSessionData(pref + CK.S_DELAY_MESSAGE);
            if (context.getSessionData(pref + CK.S_DELAY_MESSAGE) != null) {
                stage.set("delay-message", delayMessage == null ? delayMessage : delayMessage.replace("\\n", "\n"));
            }
            final String startMessage = (String) context.getSessionData(pref + CK.S_START_MESSAGE);
            if (context.getSessionData(pref + CK.S_START_MESSAGE) != null) {
                stage.set("start-message", startMessage == null ? startMessage : startMessage.replace("\\n", "\n"));
            }
            final String completeMessage = (String) context.getSessionData(pref + CK.S_COMPLETE_MESSAGE);
            if (context.getSessionData(pref + CK.S_COMPLETE_MESSAGE) != null) {
                stage.set("complete-message", completeMessage == null ? completeMessage 
                        : completeMessage.replace("\\n", "\n"));
            }
            stage.set("objective-override", context.getSessionData(pref + CK.S_OVERRIDE_DISPLAY) != null 
                    ? context.getSessionData(pref + CK.S_OVERRIDE_DISPLAY) : null);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void saveRewards(final ConversationContext context, final ConfigurationSection section) {
        final ConfigurationSection rews = section.createSection("rewards");
        rews.set("items", context.getSessionData(CK.REW_ITEMS) != null 
                ? (List<ItemStack>) context.getSessionData(CK.REW_ITEMS) : null);
        rews.set("money", context.getSessionData(CK.REW_MONEY) != null 
                ? (Integer) context.getSessionData(CK.REW_MONEY) : null);
        rews.set("quest-points", context.getSessionData(CK.REW_QUEST_POINTS) != null 
                ? (Integer) context.getSessionData(CK.REW_QUEST_POINTS) : null);
        rews.set("exp", context.getSessionData(CK.REW_EXP) != null 
                ? (Integer) context.getSessionData(CK.REW_EXP) : null);
        rews.set("commands", context.getSessionData(CK.REW_COMMAND) != null 
                ? (List<String>) context.getSessionData(CK.REW_COMMAND) : null);
        rews.set("commands-override-display", context.getSessionData(CK.REW_COMMAND_OVERRIDE_DISPLAY) != null 
                ? (List<String>) context.getSessionData(CK.REW_COMMAND_OVERRIDE_DISPLAY) : null);
        rews.set("permissions", context.getSessionData(CK.REW_PERMISSION) != null 
                ? (List<String>)context.getSessionData(CK.REW_PERMISSION) : null);
        rews.set("permission-worlds", context.getSessionData(CK.REW_PERMISSION_WORLDS) != null 
                ? (List<String>)context.getSessionData(CK.REW_PERMISSION_WORLDS) : null);
        rews.set("mcmmo-skills", context.getSessionData(CK.REW_MCMMO_SKILLS) != null 
                ? (List<String>) context.getSessionData(CK.REW_MCMMO_SKILLS) : null);
        rews.set("mcmmo-levels", context.getSessionData(CK.REW_MCMMO_AMOUNTS) != null 
                ? (List<Integer>) context.getSessionData(CK.REW_MCMMO_AMOUNTS) : null);
        rews.set("heroes-exp-classes", context.getSessionData(CK.REW_HEROES_CLASSES) != null 
                ? (List<String>) context.getSessionData(CK.REW_HEROES_CLASSES) : null);
        rews.set("heroes-exp-amounts", context.getSessionData(CK.REW_HEROES_AMOUNTS) != null 
                ? (List<Double>) context.getSessionData(CK.REW_HEROES_AMOUNTS) : null);
        rews.set("phat-loots", context.getSessionData(CK.REW_PHAT_LOOTS) != null 
                ? (List<String>) context.getSessionData(CK.REW_PHAT_LOOTS) : null);
        final LinkedList<String> customRews = context.getSessionData(CK.REW_CUSTOM) != null 
                ? (LinkedList<String>) context.getSessionData(CK.REW_CUSTOM) : null;
        final LinkedList<Map<String, Object>> customRewsData = context.getSessionData(CK.REW_CUSTOM_DATA) != null 
                ? (LinkedList<Map<String, Object>>) context.getSessionData(CK.REW_CUSTOM_DATA) : null;
        if (customRews != null) {
            final ConfigurationSection customRewsSec = rews.createSection("custom-rewards");
            for (int i = 0; i < customRews.size(); i++) {
                final ConfigurationSection customRewSec = customRewsSec.createSection("req" + (i + 1));
                customRewSec.set("name", customRews.get(i));
                customRewSec.set("data", customRewsData.get(i));
            }
        }
        rews.set("details-override", context.getSessionData(CK.REW_DETAILS_OVERRIDE) != null 
                ? (List<String>)context.getSessionData(CK.REW_DETAILS_OVERRIDE) : null);
        if (rews.getKeys(false).isEmpty()) {
            section.set("rewards", null);
        }
    }
    
    private void savePlanner(final ConversationContext context, final ConfigurationSection section) {
        final ConfigurationSection pln = section.createSection("planner");
        pln.set("start", context.getSessionData(CK.PLN_START_DATE) != null 
                ? (String) context.getSessionData(CK.PLN_START_DATE) : null);
        pln.set("end", context.getSessionData(CK.PLN_END_DATE) != null 
                ? (String) context.getSessionData(CK.PLN_END_DATE) : null);
        pln.set("repeat", context.getSessionData(CK.PLN_REPEAT_CYCLE) != null 
                ? ((Long) context.getSessionData(CK.PLN_REPEAT_CYCLE) / 1000) : null);
        pln.set("cooldown", context.getSessionData(CK.PLN_COOLDOWN) != null 
                ? ((Long) context.getSessionData(CK.PLN_COOLDOWN) / 1000) : null);
        pln.set("override", context.getSessionData(CK.PLN_OVERRIDE) != null 
                ? (Boolean) context.getSessionData(CK.PLN_OVERRIDE) : null);
        if (pln.getKeys(false).isEmpty()) {
            section.set("planner", null);
        }
    }
    
    private void saveOptions(final ConversationContext context, final ConfigurationSection section) {
        final ConfigurationSection opts = section.createSection("options");
        opts.set("allow-commands", context.getSessionData(CK.OPT_ALLOW_COMMANDS) != null 
                ? (Boolean) context.getSessionData(CK.OPT_ALLOW_COMMANDS) : null);
        opts.set("allow-quitting", context.getSessionData(CK.OPT_ALLOW_QUITTING) != null 
                ? (Boolean) context.getSessionData(CK.OPT_ALLOW_QUITTING) : null);
        opts.set("use-dungeonsxl-plugin", context.getSessionData(CK.OPT_USE_DUNGEONSXL_PLUGIN) != null 
                ? (Boolean) context.getSessionData(CK.OPT_USE_DUNGEONSXL_PLUGIN) : null);
        opts.set("use-parties-plugin", context.getSessionData(CK.OPT_USE_PARTIES_PLUGIN) != null 
                ? (Boolean) context.getSessionData(CK.OPT_USE_PARTIES_PLUGIN) : null);
        opts.set("share-progress-level", context.getSessionData(CK.OPT_SHARE_PROGRESS_LEVEL) != null 
                ? (Integer) context.getSessionData(CK.OPT_SHARE_PROGRESS_LEVEL) : null);
        opts.set("require-same-quest", context.getSessionData(CK.OPT_REQUIRE_SAME_QUEST) != null 
                ? (Boolean) context.getSessionData(CK.OPT_REQUIRE_SAME_QUEST) : null);
        if (opts.getKeys(false).isEmpty()) {
            section.set("options", null);
        }
    }
}
