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

package me.blackvein.quests.listeners;

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

import me.blackvein.quests.Quest;
import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;

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
            for (final Quest quest : plugin.getQuests()) {
                if (evt.isCancelled() == false) {
                    if (!quester.meetsCondition(quest, true)) {
                        return;
                    }
                    
                    if (quester.getCurrentQuests().containsKey(quest) 
                            && quester.getCurrentStage(quest).containsObjective("breakBlock")) {
                        if (!player.getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH)) {
                            quester.breakBlock(quest, blockItemStack);
                        }
                    }
                    quester.dispatchMultiplayerEverything(quest, "breakBlock", (final Quester q) -> {
                        if (!player.getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH)) {
                            q.breakBlock(quest, blockItemStack);
                        }
                        return null;
                    });
                    if (quester.getCurrentQuests().containsKey(quest)
                            && quester.getCurrentStage(quest).containsObjective("placeBlock")) {
                        for (final ItemStack is : quester.getQuestData(quest).blocksPlaced) {
                            if (evt.getBlock().getType().equals(is.getType()) && is.getAmount() > 0) {
                                final int index = quester.getQuestData(quest).blocksPlaced.indexOf(is);
                                is.setAmount(is.getAmount() - 1);
                                quester.getQuestData(quest).blocksPlaced.set(index, is);
                            }
                        }
                    }
                    quester.dispatchMultiplayerEverything(quest, "placeBlock", (final Quester q) -> {
                        for (final ItemStack is : q.getQuestData(quest).blocksPlaced) {
                            if (evt.getBlock().getType().equals(is.getType()) && is.getAmount() > 0) {
                                final int index = q.getQuestData(quest).blocksPlaced.indexOf(is);
                                is.setAmount(is.getAmount() - 1);
                                q.getQuestData(quest).blocksPlaced.set(index, is);
                            }
                        }
                        return null;
                    });
                    if (quester.getCurrentQuests().containsKey(quest) 
                            && quester.getCurrentStage(quest).containsObjective("cutBlock")) {
                        if (player.getItemInHand().getType().equals(Material.SHEARS)) {
                            quester.cutBlock(quest, blockItemStack);
                        }
                    }
                    quester.dispatchMultiplayerEverything(quest, "cutBlock", (final Quester q) -> {
                        if (player.getItemInHand().getType().equals(Material.SHEARS)) {
                            q.cutBlock(quest, blockItemStack);
                        }
                        return null;
                    });
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
            for (final Quest quest : plugin.getQuests()) {
                if (!quester.meetsCondition(quest, true)) {
                    return;
                }
                
                if (quester.getCurrentQuests().containsKey(quest) 
                        && quester.getCurrentStage(quest).containsObjective("damageBlock")) {
                    quester.damageBlock(quest, blockItemStack);
                }
                
                quester.dispatchMultiplayerEverything(quest, "placeBlock", (final Quester q) -> {
                    q.placeBlock(quest, blockItemStack);
                    return null;
                });
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
            for (final Quest quest : plugin.getQuests()) {
                if (evt.isCancelled() == false) {
                    if (!quester.meetsCondition(quest, true)) {
                        return;
                    }
                    
                    if (quester.getCurrentQuests().containsKey(quest) 
                            && quester.getCurrentStage(quest).containsObjective("placeBlock")) {
                        quester.placeBlock(quest, blockItemStack);
                    }
                    
                    quester.dispatchMultiplayerEverything(quest, "placeBlock", (final Quester q) -> {
                        q.placeBlock(quest, blockItemStack);
                        return null;
                    });
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
                if (evt.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                    if (evt.isCancelled() == false) {
                        final ItemStack blockItemStack = new ItemStack(evt.getClickedBlock().getType(), 1, evt
                                .getClickedBlock().getState().getData().toItemStack().getDurability());
                        for (final Quest quest : plugin.getQuests()) {
                            if (!quester.meetsCondition(quest, true)) {
                                return;
                            }
                            
                            if (quester.getCurrentQuests().containsKey(quest) 
                                    && quester.getCurrentStage(quest).containsObjective("useBlock")) {
                                quester.useBlock(quest, blockItemStack);
                            }
                            
                            quester.dispatchMultiplayerEverything(quest, "useBlock", (final Quester q) -> {
                                q.useBlock(quest, blockItemStack);
                                return null;
                            });
                        }
                    }
                }
            }
        }
    }
}
