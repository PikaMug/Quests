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
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import me.blackvein.quests.Quest;
import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;

public class ItemListener implements Listener {
    
    private final Quests plugin;
    
    public ItemListener(Quests plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onCraftItem(CraftItemEvent evt) {
        if (evt.getWhoClicked() instanceof Player) {
            if (plugin.canUseQuests(evt.getWhoClicked().getUniqueId())) {
                final ItemStack craftedItem = getCraftedItem(evt);
                Quester quester = plugin.getQuester(evt.getWhoClicked().getUniqueId());
                for (Quest quest : plugin.getQuests()) {
                    if (quester.getCurrentQuests().containsKey(quest) 
                            && quester.getCurrentStage(quest).containsObjective("craftItem")) {
                        quester.craftItem(quest, craftedItem);
                    }
                    
                    quester.dispatchMultiplayerEverything(quest, "craftItem", (Quester q) -> {
                        q.craftItem(quest, craftedItem);
                        return null;
                    });
                }
            }
        }
    }
    
    @SuppressWarnings("deprecation")
    private ItemStack getCraftedItem(CraftItemEvent evt) {
        if (evt.isShiftClick()) {
            final ItemStack recipeResult = evt.getRecipe().getResult();
            final int resultAmt = recipeResult.getAmount(); // Bread = 1, Cookie = 8, etc.
            int leastIngredient = 1;
            for (ItemStack item : evt.getInventory().getMatrix()) {
                if (item != null && !item.getType().equals(Material.AIR)) {
                    leastIngredient = item.getAmount() * resultAmt;
                }
            }
            return new ItemStack(recipeResult.getType(), leastIngredient, recipeResult.getDurability());
        }
        return evt.getCurrentItem();
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent evt) {
        if (evt.getWhoClicked() instanceof Player) {
            if (evt.getInventory().getType() == InventoryType.FURNACE
                    || evt.getInventory().getType().name().equals("BLAST_FURNACE")
                    || evt.getInventory().getType().name().equals("SMOKER")) {
                if (evt.getSlotType() == SlotType.RESULT) {
                    Quester quester = plugin.getQuester(evt.getWhoClicked().getUniqueId());
                    for (Quest quest : plugin.getQuests()) {
                        if (quester.getCurrentQuests().containsKey(quest) 
                                && quester.getCurrentStage(quest).containsObjective("smeltItem")) {
                            quester.smeltItem(quest, evt.getCurrentItem());
                        }
                        
                        quester.dispatchMultiplayerEverything(quest, "smeltItem", (Quester q) -> {
                            q.smeltItem(quest, evt.getCurrentItem());
                            return null;
                        });
                    }
                }
            } else if (evt.getInventory().getType() == InventoryType.BREWING) {
                if (evt.getSlotType() == SlotType.CRAFTING) {
                    Quester quester = plugin.getQuester(evt.getWhoClicked().getUniqueId());
                    for (Quest quest : plugin.getQuests()) {
                        if (quester.getCurrentQuests().containsKey(quest) 
                                && quester.getCurrentStage(quest).containsObjective("brewItem")) {
                            quester.brewItem(quest, evt.getCurrentItem());
                        }
                        
                        quester.dispatchMultiplayerEverything(quest, "brewItem", (Quester q) -> {
                            q.brewItem(quest, evt.getCurrentItem());
                            return null;
                        });
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void onEnchantItem(EnchantItemEvent evt) {
        if (plugin.canUseQuests(evt.getEnchanter().getUniqueId())) {
            Quester quester = plugin.getQuester(evt.getEnchanter().getUniqueId());
            for (Quest quest : plugin.getQuests()) {
                if (quester.getCurrentQuests().containsKey(quest) 
                        && quester.getCurrentStage(quest).containsObjective("enchantItem")) {
                    for (Enchantment e : evt.getEnchantsToAdd().keySet()) {
                        quester.enchantItem(quest, e, evt.getItem().getType());
                    }
                }
                
                quester.dispatchMultiplayerEverything(quest, "enchantItem", (Quester q) -> {
                    for (Enchantment e : evt.getEnchantsToAdd().keySet()) {
                        q.enchantItem(quest, e, evt.getItem().getType());
                    }
                    return null;
                });
            }
        }
    }
    
    
    @EventHandler
    public void onConsumeItem(PlayerItemConsumeEvent evt) {
        if (plugin.canUseQuests(evt.getPlayer().getUniqueId())) {
            final ItemStack consumedItem = evt.getItem().clone();
            consumedItem.setAmount(1);
            Quester quester = plugin.getQuester(evt.getPlayer().getUniqueId());
            for (Quest quest : plugin.getQuests()) {
                if (quester.getCurrentQuests().containsKey(quest) 
                        && quester.getCurrentStage(quest).containsObjective("consumeItem")) {
                    quester.consumeItem(quest, consumedItem);
                }
                
                quester.dispatchMultiplayerEverything(quest, "consumeItem", (Quester q) -> {
                    quester.consumeItem(quest, consumedItem);
                    return null;
                });
            }
        }
    }
}
