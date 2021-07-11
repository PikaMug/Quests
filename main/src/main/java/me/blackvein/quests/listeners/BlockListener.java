/*******************************************************************************************************
 * Copyright (c) 2014 PikaMug and contributors. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

package me.blackvein.quests.listeners;

import me.blackvein.quests.Objective;
import me.blackvein.quests.Quest;
import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;
import me.blackvein.quests.enums.ObjectiveType;
import me.blackvein.quests.events.quester.QuesterPostUpdateObjectiveEvent;
import me.blackvein.quests.events.quester.QuesterPreUpdateObjectiveEvent;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
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
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class BlockListener implements Listener {
    
    private final Quests plugin;
    
    public BlockListener(final Quests plugin) {
        this.plugin = plugin;
    }
    
    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGH) // Because HIGHEST conflicts with AutoSell by extendedclip
    public void onBlockBreak(final BlockBreakEvent evt) {
        final Player player = evt.getPlayer();
        if (plugin.canUseQuests(player.getUniqueId())) {
            final ItemStack blockItemStack = new ItemStack(evt.getBlock().getType(), 1, evt.getBlock().getState()
                    .getData().toItemStack().getDurability());
            final Quester quester = plugin.getQuester(player.getUniqueId());
            final ObjectiveType breakType = ObjectiveType.BREAK_BLOCK;
            final ObjectiveType placeType = ObjectiveType.PLACE_BLOCK;
            final ObjectiveType cutType = ObjectiveType.CUT_BLOCK;
            final Set<String> dispatchedBreakQuestIDs = new HashSet<String>();
            final Set<String> dispatchedPlaceQuestIDs = new HashSet<String>();
            final Set<String> dispatchedCutQuestIDs = new HashSet<String>();
            for (final Quest quest : plugin.getLoadedQuests()) {
                if (!evt.isCancelled()) {
                    if (!quester.meetsCondition(quest, true)) {
                        continue;
                    }
                    if (quester.getCurrentQuests().containsKey(quest)) {
                        if (quester.getCurrentStage(quest) == null) {
                            plugin.getLogger().severe("Player " + player.getName() + " (" + player.getUniqueId()
                                    + ") has invalid stage for quest " + quest.getName() + " (" + quest.getId() + ")");
                            continue;
                        }
                        if (quester.getCurrentStage(quest).containsObjective(breakType)) {
                            if (quest.getOptions().canIgnoreSilkTouch()
                                    && player.getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH)) {
                                Lang.send(player, ChatColor.RED + Lang.get(player, "optionSilkTouchFail")
                                        .replace("<quest>", quest.getName()));
                            } else {
                                quester.breakBlock(quest, blockItemStack);

                                // Multiplayer
                                dispatchedBreakQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, breakType,
                                        (final Quester q, final Quest cq) -> {
                                            if (!dispatchedBreakQuestIDs.contains(cq.getId())) {
                                                q.breakBlock(cq, blockItemStack);
                                            }
                                            return null;
                                        }));
                            }
                        }
                        if (quester.getCurrentStage(quest).containsObjective(placeType)) {
                            for (final ItemStack is : quester.getQuestData(quest).blocksPlaced) {
                                if (evt.getBlock().getType().equals(is.getType()) && is.getAmount() > 0) {
                                    ItemStack toPlace = new ItemStack(is.getType(), 64);
                                    for (final ItemStack stack : quester.getCurrentStage(quest).getBlocksToPlace()) {
                                        if (ItemUtil.compareItems(is, stack, true) == 0) {
                                            toPlace = stack;
                                        }
                                    }

                                    final QuesterPreUpdateObjectiveEvent preEvent
                                            = new QuesterPreUpdateObjectiveEvent(quester, quest,
                                            new Objective(placeType, is.getAmount(), toPlace.getAmount()));
                                    plugin.getServer().getPluginManager().callEvent(preEvent);

                                    final int index = quester.getQuestData(quest).blocksPlaced.indexOf(is);
                                    final int newAmount = is.getAmount() - 1;
                                    is.setAmount(newAmount);
                                    quester.getQuestData(quest).blocksPlaced.set(index, is);

                                    final QuesterPostUpdateObjectiveEvent postEvent
                                            = new QuesterPostUpdateObjectiveEvent(quester, quest,
                                            new Objective(placeType, newAmount, toPlace.getAmount()));
                                    plugin.getServer().getPluginManager().callEvent(postEvent);
                                }
                            }
                        }
                        dispatchedPlaceQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, placeType,
                                (final Quester q, final Quest cq) -> {
                                    if (!dispatchedPlaceQuestIDs.contains(cq.getId())) {
                                        for (final ItemStack is : q.getQuestData(cq).blocksPlaced) {
                                            if (evt.getBlock().getType().equals(is.getType()) && is.getAmount() > 0) {
                                                ItemStack toPlace = new ItemStack(is.getType(), 64);
                                                for (final ItemStack stack : quester.getCurrentStage(cq).getBlocksToPlace()) {
                                                    if (ItemUtil.compareItems(is, stack, true) == 0) {
                                                        toPlace = stack;
                                                    }
                                                }

                                                final QuesterPreUpdateObjectiveEvent preEvent
                                                        = new QuesterPreUpdateObjectiveEvent(q, cq,
                                                        new Objective(placeType, is.getAmount(), toPlace.getAmount()));
                                                plugin.getServer().getPluginManager().callEvent(preEvent);

                                                final int index = q.getQuestData(cq).blocksPlaced.indexOf(is);
                                                final int newAmount = is.getAmount() - 1;
                                                is.setAmount(newAmount);
                                                q.getQuestData(cq).blocksPlaced.set(index, is);

                                                final QuesterPostUpdateObjectiveEvent postEvent
                                                        = new QuesterPostUpdateObjectiveEvent(q, cq,
                                                        new Objective(placeType, newAmount, toPlace.getAmount()));
                                                plugin.getServer().getPluginManager().callEvent(postEvent);
                                            }
                                        }
                                    }
                                    return null;
                                }));
                        if (quester.getCurrentStage(quest).containsObjective(cutType)) {
                            if (player.getItemInHand().getType().equals(Material.SHEARS)) {
                                quester.cutBlock(quest, blockItemStack);
                            }
                        }
                        dispatchedCutQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, cutType,
                                (final Quester q, final Quest cq) -> {
                                    if (!dispatchedCutQuestIDs.contains(cq.getId())) {
                                        if (player.getItemInHand().getType().equals(Material.SHEARS)) {
                                            q.cutBlock(cq, blockItemStack);
                                        }
                                    }
                                    return null;
                                }));
                    }
                }
            }
        }
    }
    
    @SuppressWarnings("deprecation") // since 1.13
    @EventHandler
    public void onBlockDamage(final BlockDamageEvent evt) {
        final Player player = evt.getPlayer();
        if (plugin.canUseQuests(player.getUniqueId())) {
            final ItemStack blockItemStack = new ItemStack(evt.getBlock().getType(), 1, evt.getBlock().getState()
                    .getData().toItemStack().getDurability());
            final Quester quester = plugin.getQuester(player.getUniqueId());
            final ObjectiveType type = ObjectiveType.DAMAGE_BLOCK;
            final Set<String> dispatchedQuestIDs = new HashSet<String>();
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
                        q.placeBlock(cq, blockItemStack);
                    }
                    return null;
                }));
            }
        }
    }
    
    @SuppressWarnings("deprecation") // since 1.13
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(final BlockPlaceEvent evt) {
        final Player player = evt.getPlayer();
        if (plugin.canUseQuests(player.getUniqueId())) {
            final ItemStack blockItemStack = new ItemStack(evt.getBlock().getType(), 1, evt.getBlock().getState()
                    .getData().toItemStack().getDurability());
            final Quester quester = plugin.getQuester(player.getUniqueId());
            final ObjectiveType type = ObjectiveType.PLACE_BLOCK;
            final Set<String> dispatchedQuestIDs = new HashSet<String>();
            for (final Quest quest : plugin.getLoadedQuests()) {
                if (evt.isCancelled() == false) {
                    if (!quester.meetsCondition(quest, true)) {
                        continue;
                    }
                    
                    if (quester.getCurrentQuests().containsKey(quest) 
                            && quester.getCurrentStage(quest).containsObjective(type)) {
                        quester.placeBlock(quest, blockItemStack);
                    }
                    
                    dispatchedQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, type, 
                            (final Quester q, final Quest cq) -> {
                        if (!dispatchedQuestIDs.contains(cq.getId())) {
                            q.placeBlock(cq, blockItemStack);
                        }
                        return null;
                    }));
                }
            }
        }
    }
    
    @SuppressWarnings("deprecation") // since 1.13
    @EventHandler
    public void onBlockUse(final PlayerInteractEvent evt) {
        EquipmentSlot e = null;
        try {
            e = evt.getHand();
        } catch (final NoSuchMethodError err) {
            // Do nothing, getHand() not present pre-1.9
        }
        if (e == null || e.equals(EquipmentSlot.HAND)) { //If the event is fired by HAND (main hand)
            final Player player = evt.getPlayer();
            if (plugin.canUseQuests(evt.getPlayer().getUniqueId())) {
                final Quester quester = plugin.getQuester(player.getUniqueId());
                if (quester.isSelectingBlock()) {
                    return;
                }
                if (evt.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                    if (!evt.isCancelled()) {
                        final ItemStack blockItemStack = new ItemStack(evt.getClickedBlock().getType(), 1, evt
                                .getClickedBlock().getState().getData().toItemStack().getDurability());
                        final ObjectiveType type = ObjectiveType.USE_BLOCK;
                        final Set<String> dispatchedQuestIDs = new HashSet<String>();
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
                                    q.useBlock(cq, blockItemStack);
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
