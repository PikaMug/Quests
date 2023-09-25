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
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.HashSet;
import java.util.Set;

public class BukkitItemListener implements Listener {
    
    private final BukkitQuestsPlugin plugin;
    
    public BukkitItemListener(final BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onCraftItem(final CraftItemEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getAction().equals(InventoryAction.NOTHING)) {
            return;
        }
        if (event.getWhoClicked() instanceof Player) {
            final Player player = (Player) event.getWhoClicked();
            if (plugin.canUseQuests(player.getUniqueId())) {
                final ItemStack craftedItem = getCraftedItem(event);
                final Quester quester = plugin.getQuester(player.getUniqueId());
                final ObjectiveType type = ObjectiveType.CRAFT_ITEM;
                final Set<String> dispatchedQuestIDs = new HashSet<>();
                for (final Quest quest : plugin.getLoadedQuests()) {
                    if (!quester.meetsCondition(quest, true)) {
                        continue;
                    }
                    
                    if (quester.getCurrentQuests().containsKey(quest)
                            && quester.getCurrentStage(quest).containsObjective(type)) {
                        quester.craftItem(quest, craftedItem);
                    }
                    
                    dispatchedQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, type,
                            (final Quester q, final Quest cq) -> {
                        if (!dispatchedQuestIDs.contains(cq.getId())) {
                            q.craftItem(cq, craftedItem);
                        }
                        return null;
                    }));
                }
            }
        }
    }
    
    @SuppressWarnings("deprecation")
    private ItemStack getCraftedItem(final CraftItemEvent event) {
        if (event.isShiftClick()) {
            final ItemStack recipeResult = event.getRecipe().getResult();
            final int resultAmt = recipeResult.getAmount(); // Bread = 1, Cookie = 8, etc.
            int leastIngredient = -1;
            for (final ItemStack item : event.getInventory().getMatrix()) {
                if (item != null && !item.getType().equals(Material.AIR)) {
                    final int re = item.getAmount() * resultAmt;
                    if (leastIngredient == -1 || re < leastIngredient) {
                        leastIngredient = re;
                    }
                }
            }
            return new ItemStack(recipeResult.getType(), leastIngredient, recipeResult.getDurability());
        }
        return event.getCurrentItem();
    }
    
    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getWhoClicked() instanceof Player) {
            final Player player = (Player) event.getWhoClicked();
            if (event.getInventory().getType() == InventoryType.FURNACE
                    || event.getInventory().getType().name().equals("BLAST_FURNACE")
                    || event.getInventory().getType().name().equals("SMOKER")) {
                if (event.getSlotType() == SlotType.RESULT) {
                    final Quester quester = plugin.getQuester(player.getUniqueId());
                    final ObjectiveType type = ObjectiveType.SMELT_ITEM;
                    final Set<String> dispatchedQuestIDs = new HashSet<>();
                    for (final Quest quest : plugin.getLoadedQuests()) {
                        if (!quester.meetsCondition(quest, true)) {
                            continue;
                        }
                        
                        if (quester.getCurrentQuests().containsKey(quest)
                                && quester.getCurrentStage(quest).containsObjective(type)) {
                            quester.smeltItem(quest, event.getCurrentItem());
                        }
                        
                        dispatchedQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, type,
                                (final Quester q, final Quest cq) -> {
                            if (!dispatchedQuestIDs.contains(cq.getId())) {
                                q.smeltItem(cq, event.getCurrentItem());
                            }
                            return null;
                        }));
                    }
                }
            } else if (event.getInventory().getType() == InventoryType.BREWING) {
                if (event.getSlotType() == SlotType.CRAFTING
                        || event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
                    final Quester quester = plugin.getQuester(player.getUniqueId());
                    final ObjectiveType type = ObjectiveType.BREW_ITEM;
                    final Set<String> dispatchedQuestIDs = new HashSet<>();
                    for (final Quest quest : plugin.getLoadedQuests()) {
                        if (!quester.meetsCondition(quest, true)) {
                            continue;
                        }
                        
                        if (quester.getCurrentQuests().containsKey(quest)
                                && quester.getCurrentStage(quest).containsObjective(type)) {
                            if (isAllowedBrewingAction(event)) {
                                quester.brewItem(quest, event.getCurrentItem());
                            }
                        }
                        
                        dispatchedQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, type,
                                (final Quester q, final Quest cq) -> {
                            if (!dispatchedQuestIDs.contains(cq.getId())) {
                                if (isAllowedBrewingAction(event)) {
                                    q.brewItem(cq, event.getCurrentItem());
                                }
                            }
                            return null;
                        }));
                    }
                }
            }
        }
    }

    public boolean isAllowedBrewingAction(final InventoryClickEvent event) {
        if (event.getCursor() != null && isWaterBottle(event.getCursor())) {
            return true;
        }
        final int slot = event.getRawSlot();
        final InventoryAction action = event.getAction();
        // Prevent shift-click into Brewing Stand
        if (event.getSlotType() != SlotType.CRAFTING && action.equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
            event.setCancelled(true);
            return false;
        }
        // Prevent placing into Brewing Stand
        if (slot == 0 || slot == 1 || slot == 2) {
            if (action.equals(InventoryAction.PLACE_ONE) || action.equals(InventoryAction.PLACE_SOME)
                    || action.equals(InventoryAction.PLACE_ALL)) {
                event.setCancelled(true);
                return false;
            }
        }
        return true;
    }

    public boolean isWaterBottle(final ItemStack item) {
        if (item == null) {
            return false;
        }
        if (item.getType().equals(Material.POTION)) {
            final PotionMeta meta = (PotionMeta) item.getItemMeta();
            if (meta == null) {
                return false;
            }
            if (meta.getBasePotionData().getType().equals(PotionType.WATER)) {
                return true;
            }
        }
        return false;
    }
    
    @EventHandler
    public void onEnchantItem(final EnchantItemEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (plugin.canUseQuests(event.getEnchanter().getUniqueId())) {
            final ItemStack enchantedItem = event.getItem().clone();
            enchantedItem.setAmount(1);
            enchantedItem.addUnsafeEnchantments(event.getEnchantsToAdd());
            final Quester quester = plugin.getQuester(event.getEnchanter().getUniqueId());
            final ObjectiveType type = ObjectiveType.ENCHANT_ITEM;
            final Set<String> dispatchedQuestIDs = new HashSet<>();
            for (final Quest quest : plugin.getLoadedQuests()) {
                if (!quester.meetsCondition(quest, true)) {
                    continue;
                }
                
                if (quester.getCurrentQuests().containsKey(quest)
                        && quester.getCurrentStage(quest).containsObjective(type)) {
                    if (enchantedItem.getType().equals(Material.BOOK)) {
                        quester.enchantBook(quest, enchantedItem, event.getEnchantsToAdd());
                    } else {
                        quester.enchantItem(quest, enchantedItem);
                    }
                }
                
                dispatchedQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, type,
                        (final Quester q, final Quest cq) -> {
                    if (!dispatchedQuestIDs.contains(cq.getId())) {
                        if (enchantedItem.getType().equals(Material.BOOK)) {
                            q.enchantBook(cq, enchantedItem, event.getEnchantsToAdd());
                        } else {
                            q.enchantItem(cq, enchantedItem);
                        }
                    }
                    return null;
                }));
            }
        }
    }
    
    @EventHandler
    public void onConsumeItem(final PlayerItemConsumeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (plugin.canUseQuests(event.getPlayer().getUniqueId())) {
            final ItemStack consumedItem = event.getItem().clone();
            consumedItem.setAmount(1);
            final Quester quester = plugin.getQuester(event.getPlayer().getUniqueId());
            final ObjectiveType type = ObjectiveType.CONSUME_ITEM;
            final Set<String> dispatchedQuestIDs = new HashSet<>();
            for (final Quest quest : plugin.getLoadedQuests()) {
                if (!quester.meetsCondition(quest, true)) {
                    continue;
                }
                
                if (quester.getCurrentQuests().containsKey(quest)
                        && quester.getCurrentStage(quest).containsObjective(type)) {
                    quester.consumeItem(quest, consumedItem);
                }
                
                dispatchedQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, type,
                        (final Quester q, final Quest cq) -> {
                    if (!dispatchedQuestIDs.contains(cq.getId())) {
                        q.consumeItem(cq, consumedItem);
                    }
                    return null;
                }));
            }
        }
    }
}
