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
import me.pikamug.quests.quests.components.BukkitObjective;
import me.pikamug.quests.quests.components.BukkitStage;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.util.BukkitItemUtil;
import me.pikamug.quests.util.BukkitLang;
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
            final ItemStack blockItemStack = new ItemStack(event.getBlock().getType(), 1, event.getBlock().getState()
                    .getData().toItemStack().getDurability());
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
                            quester.breakBlock(quest, blockItemStack);

                            dispatchedBreakQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, breakType,
                                    (final Quester q, final Quest cq) -> {
                                if (!dispatchedBreakQuestIDs.contains(cq.getId())) {
                                    q.breakBlock(cq, blockItemStack);
                                }
                                return null;
                            }));
                        }
                    }
                    final BukkitQuestProgress questData = (BukkitQuestProgress) quester.getQuestDataOrDefault(quest);
                    if (quest.getOptions().canIgnoreBlockReplace()) {
                        // Ignore blocks broken once replaced (self)
                        if (currentStage.containsObjective(placeType)) {
                            for (final ItemStack is : questData.blocksPlaced) {
                                if (event.getBlock().getType().equals(is.getType()) && is.getAmount() > 0) {
                                    ItemStack toPlace = new ItemStack(is.getType(), 64);
                                    for (final ItemStack stack : currentStage.getBlocksToPlace()) {
                                        if (BukkitItemUtil.compareItems(is, stack, true) == 0) {
                                            toPlace = stack;
                                        }
                                    }

                                    final BukkitQuesterPreUpdateObjectiveEvent preEvent
                                            = new BukkitQuesterPreUpdateObjectiveEvent(quester, quest,
                                            new BukkitObjective(placeType, null, is.getAmount(), toPlace.getAmount()));
                                    plugin.getServer().getPluginManager().callEvent(preEvent);

                                    final int index = questData.blocksPlaced.indexOf(is);
                                    final int newAmount = is.getAmount() - 1;
                                    is.setAmount(newAmount);
                                    questData.blocksPlaced.set(index, is);

                                    final BukkitQuesterPostUpdateObjectiveEvent postEvent
                                            = new BukkitQuesterPostUpdateObjectiveEvent(quester, quest,
                                            new BukkitObjective(placeType, null, newAmount, toPlace.getAmount()));
                                    plugin.getServer().getPluginManager().callEvent(postEvent);
                                }
                            }
                        }
                        // Ignore blocks broken once replaced (party support)
                        dispatchedPlaceQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, placeType,
                                (final Quester q, final Quest cq) -> {
                            if (!dispatchedPlaceQuestIDs.contains(cq.getId())) {
                                final BukkitQuestProgress qQuestData = (BukkitQuestProgress) q.getQuestDataOrDefault(cq);
                                for (final ItemStack is : qQuestData.blocksPlaced) {
                                    if (event.getBlock().getType().equals(is.getType()) && is.getAmount() > 0) {
                                        ItemStack toPlace = new ItemStack(is.getType(), 64);
                                        for (final ItemStack stack : ((BukkitStage) quester.getCurrentStage(cq)).getBlocksToPlace()) {
                                            if (BukkitItemUtil.compareItems(is, stack, true) == 0) {
                                                toPlace = stack;
                                            }
                                        }

                                        final BukkitQuesterPreUpdateObjectiveEvent preEvent
                                                = new BukkitQuesterPreUpdateObjectiveEvent((BukkitQuester) q, cq,
                                                new BukkitObjective(placeType, null, is.getAmount(), toPlace.getAmount()));
                                        plugin.getServer().getPluginManager().callEvent(preEvent);

                                        final int index = qQuestData.blocksPlaced.indexOf(is);
                                        final int newAmount = is.getAmount() - 1;
                                        is.setAmount(newAmount);
                                        qQuestData.blocksPlaced.set(index, is);

                                        final BukkitQuesterPostUpdateObjectiveEvent postEvent
                                                = new BukkitQuesterPostUpdateObjectiveEvent((BukkitQuester) q, cq,
                                                new BukkitObjective(placeType, null, newAmount, toPlace.getAmount()));
                                        plugin.getServer().getPluginManager().callEvent(postEvent);
                                    }
                                }
                            }
                            return null;
                        }));
                    }
                    if (currentStage.containsObjective(cutType)) {
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
    
    @SuppressWarnings("deprecation") // since 1.13
    @EventHandler
    public void onBlockDamage(final BlockDamageEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final Player player = event.getPlayer();
        if (plugin.canUseQuests(player.getUniqueId())) {
            final ItemStack blockItemStack = new ItemStack(event.getBlock().getType(), 1, event.getBlock().getState()
                    .getData().toItemStack().getDurability());
            final Quester quester = plugin.getQuester(player.getUniqueId());
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
                        q.damageBlock(cq, blockItemStack);
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
            final ItemStack blockItemStack = new ItemStack(event.getBlock().getType(), 1, event.getBlock().getState()
                    .getData().toItemStack().getDurability());
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

                    final BukkitQuestProgress questData = (BukkitQuestProgress) quester.getQuestDataOrDefault(quest);
                    if (quest.getOptions().canIgnoreBlockReplace()) {
                        // Ignore blocks replaced once broken (self)
                        if (currentStage.containsObjective(breakType)) {
                            for (final ItemStack is : questData.blocksBroken) {
                                if (event.getBlock().getType().equals(is.getType()) && is.getAmount() > 0) {
                                    ItemStack toBreak = new ItemStack(is.getType(), 64);
                                    for (final ItemStack stack : currentStage.getBlocksToBreak()) {
                                        if (BukkitItemUtil.compareItems(is, stack, true) == 0) {
                                            toBreak = stack;
                                        }
                                    }

                                    final BukkitQuesterPreUpdateObjectiveEvent preEvent
                                            = new BukkitQuesterPreUpdateObjectiveEvent(quester, quest,
                                            new BukkitObjective(placeType, null, is.getAmount(), toBreak.getAmount()));
                                    plugin.getServer().getPluginManager().callEvent(preEvent);

                                    final int index = questData.blocksBroken.indexOf(is);
                                    final int newAmount = is.getAmount() - 1;
                                    is.setAmount(newAmount);
                                    questData.blocksBroken.set(index, is);

                                    final BukkitQuesterPostUpdateObjectiveEvent postEvent
                                            = new BukkitQuesterPostUpdateObjectiveEvent(quester, quest,
                                            new BukkitObjective(placeType, null, newAmount, toBreak.getAmount()));
                                    plugin.getServer().getPluginManager().callEvent(postEvent);
                                }
                            }
                        }
                        // Ignore blocks replaced once broken (party support)
                        dispatchedBreakQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, breakType,
                                (final Quester q, final Quest cq) -> {
                            if (!dispatchedBreakQuestIDs.contains(cq.getId())) {
                                final BukkitQuestProgress qQuestData = (BukkitQuestProgress) q.getQuestDataOrDefault(cq);
                                for (final ItemStack is : qQuestData.blocksBroken) {
                                    if (event.getBlock().getType().equals(is.getType()) && is.getAmount() > 0) {
                                        ItemStack toBreak = new ItemStack(is.getType(), 64);
                                        for (final ItemStack stack : ((BukkitStage) quester.getCurrentStage(cq)).getBlocksToBreak()) {
                                            if (BukkitItemUtil.compareItems(is, stack, true) == 0) {
                                                toBreak = stack;
                                            }
                                        }

                                        final BukkitQuesterPreUpdateObjectiveEvent preEvent
                                                = new BukkitQuesterPreUpdateObjectiveEvent((BukkitQuester) q, cq,
                                                new BukkitObjective(breakType, null, is.getAmount(), toBreak.getAmount()));
                                        plugin.getServer().getPluginManager().callEvent(preEvent);

                                        final int index = qQuestData.blocksBroken.indexOf(is);
                                        final int newAmount = is.getAmount() - 1;
                                        is.setAmount(newAmount);
                                        qQuestData.blocksBroken.set(index, is);

                                        final BukkitQuesterPostUpdateObjectiveEvent postEvent
                                                = new BukkitQuesterPostUpdateObjectiveEvent((BukkitQuester) q, cq,
                                                new BukkitObjective(breakType, null, newAmount, toBreak.getAmount()));
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
                        q.placeBlock(cq, blockItemStack);
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
                final Quester quester = plugin.getQuester(player.getUniqueId());
                if (quester.isSelectingBlock()) {
                    return;
                }
                if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                    if (!event.isCancelled() && event.getClickedBlock() != null) {
                        final ItemStack blockItemStack = new ItemStack(event.getClickedBlock().getType(), 1, event
                                .getClickedBlock().getState().getData().toItemStack().getDurability());
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
