/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.listeners;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.enums.ObjectiveType;
import me.pikamug.quests.events.quester.BukkitQuesterPostUpdateObjectiveEvent;
import me.pikamug.quests.events.quester.BukkitQuesterPreUpdateObjectiveEvent;
import me.pikamug.quests.nms.BukkitActionBarProvider;
import me.pikamug.quests.player.BukkitQuestProgress;
import me.pikamug.quests.player.BukkitQuester;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.BukkitObjective;
import me.pikamug.quests.quests.components.BukkitStage;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.quests.util.stack.BlockItemStack;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashSet;
import java.util.Set;

public class BukkitBlockListener implements Listener {
    
    private final BukkitQuestsPlugin plugin;
    
    public BukkitBlockListener(final BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }
    
    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGH) // Because HIGHEST conflicts with AutoSell by extendedclip
    public void onBlockBreak(final BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final Player player = event.getPlayer();
        if (plugin.canUseQuests(player.getUniqueId())) {
            final BlockItemStack blockItem = BlockItemStack.of(event.getBlock());

            final BukkitQuester quester = plugin.getQuester(player.getUniqueId());
            final ObjectiveType breakType = ObjectiveType.BREAK_BLOCK;
            final ObjectiveType placeType = ObjectiveType.PLACE_BLOCK;
            final ObjectiveType cutType = ObjectiveType.CUT_BLOCK;
            final Set<String> dispatchedBreakQuestIDs = new HashSet<>();
            final Set<String> dispatchedPlaceQuestIDs = new HashSet<>();
            final Set<String> dispatchedCutQuestIDs = new HashSet<>();
            for (final Quest quest : plugin.getLoadedQuests()) {
                if (!quester.meetsCondition(quest, true)) {
                    continue;
                }
                if (quester.getCurrentQuests().containsKey(quest)) {
                    final BukkitStage currentStage = (BukkitStage) quester.getCurrentStage(quest);
                    if (currentStage == null) {
                        plugin.getLogger().severe("Player " + player.getName() + " (" + player.getUniqueId()
                                + ") has invalid stage for quest " + quest.getName() + " (" + quest.getId() + ")");
                        continue;
                    }
                    if (currentStage.containsObjective(breakType)) {
                        if (quest.getOptions().canIgnoreSilkTouch()
                                && player.getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH)) {
                            BukkitActionBarProvider.sendActionBar(player, ChatColor.RED + BukkitLang
                                    .get(player, "optionSilkTouchFail").replace("<quest>", quest.getName()));
                        } else {
                            quester.breakBlock(quest, blockItem);

                            dispatchedBreakQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, breakType,
                                    (final Quester q, final Quest cq) -> {
                                if (!dispatchedBreakQuestIDs.contains(cq.getId())) {
                                    ((BukkitQuester) q).breakBlock(cq, blockItem);
                                }
                                return null;
                            }));
                        }
                    }
                    final BukkitQuestProgress questProgress = (BukkitQuestProgress) quester.getQuestProgressOrDefault(quest);
                    if (quest.getOptions().canIgnoreBlockReplace()) {
                        // Ignore blocks broken once replaced (self)
                        if (currentStage.containsObjective(placeType)) {
                            for (int i = 0; i < questProgress.blocksPlaced.size(); i++) {
                                final int progress = questProgress.blocksPlaced.get(i) - 1;
                                if (progress < 0) {
                                    break;
                                }
                                if (i >= currentStage.getBlocksToPlace().size()) {
                                    plugin.getLogger().info("Quest " + quest.getId() + " had abnormally large amount " +
                                            "of blocks to place. You can probably ignore this.");
                                    break;
                                }
                                final BlockItemStack is = currentStage.getBlocksToPlace().get(i);
                                if (event.getBlock().getType().equals(is.getType()) && is.getAmount() > 0) {
                                    BlockItemStack goal = BlockItemStack.clone(is, 64);
                                    for (final BlockItemStack stack : currentStage.getBlocksToPlace()) {
                                        if (stack.matches(goal)) {
                                            goal = stack;
                                        }
                                    }

                                    final BukkitQuesterPreUpdateObjectiveEvent preEvent
                                            = new BukkitQuesterPreUpdateObjectiveEvent(quester, quest,
                                            new BukkitObjective(placeType, null, is.getAmount(), goal.getAmount()));
                                    plugin.getServer().getPluginManager().callEvent(preEvent);

                                    questProgress.blocksPlaced.set(i, progress);

                                    final BukkitQuesterPostUpdateObjectiveEvent postEvent
                                            = new BukkitQuesterPostUpdateObjectiveEvent(quester, quest,
                                            new BukkitObjective(placeType, null, progress, goal.getAmount()));
                                    plugin.getServer().getPluginManager().callEvent(postEvent);
                                }
                            }
                        }
                        // Ignore blocks broken once replaced (party support)
                        dispatchedPlaceQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, placeType,
                                (final Quester q, final Quest cq) -> {
                            if (!dispatchedPlaceQuestIDs.contains(cq.getId())) {
                                final BukkitQuestProgress qQuestProgress = (BukkitQuestProgress) q.getQuestProgressOrDefault(cq);
                                for (int i = 0; i < qQuestProgress.blocksPlaced.size(); i++) {
                                    final int progress = qQuestProgress.blocksPlaced.get(i) - 1;
                                    if (progress < 0) {
                                        break;
                                    }
                                    final BlockItemStack is = currentStage.getBlocksToPlace().get(i);
                                    if (event.getBlock().getType().equals(is.getType()) && is.getAmount() > 0) {
                                        BlockItemStack goal = BlockItemStack.clone(is, 64);
                                        for (final BlockItemStack stack : ((BukkitStage) quester.getCurrentStage(cq)).getBlocksToPlace()) {
                                            if (stack.matches(goal)) {
                                                goal = stack;
                                            }
                                        }

                                        final BukkitQuesterPreUpdateObjectiveEvent preEvent
                                                = new BukkitQuesterPreUpdateObjectiveEvent((BukkitQuester) q, cq,
                                                new BukkitObjective(placeType, null, is.getAmount(), goal.getAmount()));
                                        plugin.getServer().getPluginManager().callEvent(preEvent);

                                        qQuestProgress.blocksPlaced.set(i, progress);

                                        final BukkitQuesterPostUpdateObjectiveEvent postEvent
                                                = new BukkitQuesterPostUpdateObjectiveEvent((BukkitQuester) q, cq,
                                                new BukkitObjective(placeType, null, progress, goal.getAmount()));
                                        plugin.getServer().getPluginManager().callEvent(postEvent);
                                    }
                                }
                            }
                            return null;
                        }));
                    }
                    if (currentStage.containsObjective(cutType)) {
                        if (player.getItemInHand().getType().equals(Material.SHEARS)) {
                            quester.cutBlock(quest, blockItem);
                        }
                    }
                    dispatchedCutQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, cutType,
                            (final Quester q, final Quest cq) -> {
                        if (!dispatchedCutQuestIDs.contains(cq.getId())) {
                            if (player.getItemInHand().getType().equals(Material.SHEARS)) {
                                ((BukkitQuester) q).cutBlock(cq, blockItem);
                            }
                        }
                        return null;
                    }));
                }
            }
        }
    }
    
    @SuppressWarnings("deprecation") // since 1.13
    @EventHandler
    public void onBlockDamage(final BlockDamageEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final Player player = event.getPlayer();
        if (plugin.canUseQuests(player.getUniqueId())) {
            final BlockItemStack blockItemStack = BlockItemStack.of(event.getBlock());

            final BukkitQuester quester = plugin.getQuester(player.getUniqueId());
            final ObjectiveType type = ObjectiveType.DAMAGE_BLOCK;
            final Set<String> dispatchedQuestIDs = new HashSet<>();
            for (final Quest quest : plugin.getLoadedQuests()) {
                if (!quester.meetsCondition(quest, true)) {
                    continue;
                }
                
                if (quester.getCurrentQuests().containsKey(quest)
                        && quester.getCurrentStage(quest).containsObjective(type)) {
                    quester.damageBlock(quest, blockItemStack);
                }
                
                dispatchedQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, type, 
                        (final Quester q, final Quest cq) -> {
                    if (!dispatchedQuestIDs.contains(cq.getId())) {
                        ((BukkitQuester) q).damageBlock(cq, blockItemStack);
                    }
                    return null;
                }));
            }
        }
    }
    
    @SuppressWarnings("deprecation") // since 1.13
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(final BlockPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final Player player = event.getPlayer();
        if (plugin.canUseQuests(player.getUniqueId())) {
            final BlockItemStack blockItemStack = BlockItemStack.of(event.getBlock());

            final BukkitQuester quester = plugin.getQuester(player.getUniqueId());
            final ObjectiveType placeType = ObjectiveType.PLACE_BLOCK;
            final ObjectiveType breakType = ObjectiveType.BREAK_BLOCK;
            final Set<String> dispatchedPlaceQuestIDs = new HashSet<>();
            final Set<String> dispatchedBreakQuestIDs = new HashSet<>();
            for (final Quest quest : plugin.getLoadedQuests()) {
                if (!quester.meetsCondition(quest, true)) {
                    continue;
                }

                if (quester.getCurrentQuests().containsKey(quest)) {
                    final BukkitStage currentStage = (BukkitStage) quester.getCurrentStage(quest);

                    if (currentStage.containsObjective(placeType)) {
                        quester.placeBlock(quest, blockItemStack);
                    }

                    final BukkitQuestProgress questProgress = (BukkitQuestProgress) quester.getQuestProgressOrDefault(quest);
                    if (quest.getOptions().canIgnoreBlockReplace()) {
                        // Ignore blocks replaced once broken (self)
                        if (currentStage.containsObjective(breakType)) {
                            for (int i = 0; i < questProgress.blocksBroken.size(); i++) {
                                final int progress = questProgress.blocksBroken.get(i) - 1;
                                if (progress < 0) {
                                    break;
                                }
                                if (i >= currentStage.getBlocksToBreak().size()) {
                                    plugin.getLogger().info("Quest " + quest.getId() + " had abnormally large amount " +
                                            "of blocks to break. You can probably ignore this.");
                                    break;
                                }
                                final BlockItemStack is = currentStage.getBlocksToBreak().get(i);
                                if (event.getBlock().getType().equals(is.getType()) && is.getAmount() > 0) {
                                    BlockItemStack goal = BlockItemStack.clone(is, 64);
                                    for (final BlockItemStack stack : currentStage.getBlocksToBreak()) {
                                        if (stack.matches(goal)) {
                                            goal = stack;
                                        }
                                    }

                                    final BukkitQuesterPreUpdateObjectiveEvent preEvent
                                            = new BukkitQuesterPreUpdateObjectiveEvent(quester, quest,
                                            new BukkitObjective(placeType, null, is.getAmount(), goal.getAmount()));
                                    plugin.getServer().getPluginManager().callEvent(preEvent);

                                    questProgress.blocksBroken.set(i, progress);

                                    final BukkitQuesterPostUpdateObjectiveEvent postEvent
                                            = new BukkitQuesterPostUpdateObjectiveEvent(quester, quest,
                                            new BukkitObjective(placeType, null, progress, goal.getAmount()));
                                    plugin.getServer().getPluginManager().callEvent(postEvent);
                                }
                            }
                        }
                        // Ignore blocks replaced once broken (party support)
                        dispatchedBreakQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, breakType,
                                (final Quester q, final Quest cq) -> {
                            if (!dispatchedBreakQuestIDs.contains(cq.getId())) {
                                final BukkitQuestProgress qQuestProgress = (BukkitQuestProgress) q.getQuestProgressOrDefault(cq);
                                for (int i = 0; i < qQuestProgress.blocksBroken.size(); i++) {
                                    final int progress = qQuestProgress.blocksBroken.get(i) - 1;
                                    if (progress < 0) {
                                        break;
                                    }
                                    final BlockItemStack is = currentStage.getBlocksToBreak().get(i);
                                    if (event.getBlock().getType().equals(is.getType()) && is.getAmount() > 0) {
                                        BlockItemStack goal = BlockItemStack.clone(is, 64);
                                        for (final BlockItemStack stack : ((BukkitStage) quester.getCurrentStage(cq)).getBlocksToBreak()) {
                                            if (stack.matches(goal)) {
                                                goal = stack;
                                            }
                                        }

                                        final BukkitQuesterPreUpdateObjectiveEvent preEvent
                                                = new BukkitQuesterPreUpdateObjectiveEvent((BukkitQuester) q, cq,
                                                new BukkitObjective(breakType, null, is.getAmount(), goal.getAmount()));
                                        plugin.getServer().getPluginManager().callEvent(preEvent);

                                        qQuestProgress.blocksBroken.set(i, progress);

                                        final BukkitQuesterPostUpdateObjectiveEvent postEvent
                                                = new BukkitQuesterPostUpdateObjectiveEvent((BukkitQuester) q, cq,
                                                new BukkitObjective(breakType, null, progress, goal.getAmount()));
                                        plugin.getServer().getPluginManager().callEvent(postEvent);
                                    }
                                }
                            }
                            return null;
                        }));
                    }
                }

                dispatchedPlaceQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, placeType,
                        (final Quester q, final Quest cq) -> {
                    if (!dispatchedPlaceQuestIDs.contains(cq.getId())) {
                        ((BukkitQuester) q).placeBlock(cq, blockItemStack);
                    }
                    return null;
                }));
            }
        }
    }
    
    @SuppressWarnings("deprecation") // since 1.13
    @EventHandler
    public void onBlockUse(final PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }
        EquipmentSlot e = null;
        try {
            e = event.getHand();
        } catch (final NoSuchMethodError err) {
            // Do nothing, getHand() not present pre-1.9
        }
        if (e == null || e.equals(EquipmentSlot.HAND)) { // If the event is fired by HAND (main hand)
            final Player player = event.getPlayer();
            if (plugin.canUseQuests(event.getPlayer().getUniqueId())) {
                final BukkitQuester quester = plugin.getQuester(player.getUniqueId());
                if (quester.isSelectingBlock()) {
                    return;
                }
                if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                    if (!event.isCancelled() && event.getClickedBlock() != null) {
                        final BlockItemStack blockItemStack = BlockItemStack.of(event.getClickedBlock());

                        final ObjectiveType type = ObjectiveType.USE_BLOCK;
                        final Set<String> dispatchedQuestIDs = new HashSet<>();
                        for (final Quest quest : plugin.getLoadedQuests()) {
                            if (!quester.meetsCondition(quest, true)) {
                                continue;
                            }
                            
                            if (quester.getCurrentQuests().containsKey(quest)
                                    && quester.getCurrentStage(quest).containsObjective(type)) {
                                quester.useBlock(quest, blockItemStack);
                            }
                            
                            dispatchedQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, type, 
                                    (final Quester q, final Quest cq) -> {
                                if (!dispatchedQuestIDs.contains(cq.getId())) {
                                    ((BukkitQuester) q).useBlock(cq, blockItemStack);
                                }
                                return null;
                            }));
                        }
                    }
                }
            }
        }
    }
}
