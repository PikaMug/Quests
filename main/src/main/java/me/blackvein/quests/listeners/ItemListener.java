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

import java.util.HashSet;
import java.util.Set;

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

import me.blackvein.quests.Quest;
import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;
import me.blackvein.quests.enums.ObjectiveType;

public class ItemListener implements Listener {
    
    private final Quests plugin;
    
    public ItemListener(final Quests plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onCraftItem(final CraftItemEvent evt) {
        if (evt.getAction().equals(InventoryAction.NOTHING)) {
            return;
        }
        if (evt.getWhoClicked() instanceof Player) {
            final Player player = (Player) evt.getWhoClicked();
            if (plugin.canUseQuests(player.getUniqueId())) {
                final ItemStack craftedItem = getCraftedItem(evt);
                final Quester quester = plugin.getQuester(player.getUniqueId());
                final ObjectiveType type = ObjectiveType.CRAFT_ITEM;
                final Set<String> dispatchedQuestIDs = new HashSet<String>();
                for (final Quest quest : plugin.getLoadedQuests()) {
                    if (!quester.meetsCondition(quest, true)) {
                        continue;
                    }
                    
                    if (quester.getCurrentQuests().containsKey(quest) 
                            && quester.getCurrentStage(quest).containsObjective(type)) {
                        quester.craftItem(quest, craftedItem);
                    }
                    
                    dispatchedQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, type, (final Quester q, final Quest cq) -> {
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
    private ItemStack getCraftedItem(final CraftItemEvent evt) {
        if (evt.isShiftClick()) {
            final ItemStack recipeResult = evt.getRecipe().getResult();
            final int resultAmt = recipeResult.getAmount(); // Bread = 1, Cookie = 8, etc.
            int leastIngredient = -1;
            for (final ItemStack item : evt.getInventory().getMatrix()) {
                if (item != null && !item.getType().equals(Material.AIR)) {
                    final int re = item.getAmount() * resultAmt;
                    if (leastIngredient == -1 || re < leastIngredient) {
                        leastIngredient = re;
                    }
                }
            }
            return new ItemStack(recipeResult.getType(), leastIngredient, recipeResult.getDurability());
        }
        return evt.getCurrentItem();
    }
    
    @EventHandler
    public void onInventoryClick(final InventoryClickEvent evt) {
        if (evt.getWhoClicked() instanceof Player) {
            final Player player = (Player) evt.getWhoClicked();
            if (evt.getInventory().getType() == InventoryType.FURNACE
                    || evt.getInventory().getType().name().equals("BLAST_FURNACE")
                    || evt.getInventory().getType().name().equals("SMOKER")) {
                if (evt.getSlotType() == SlotType.RESULT) {
                    final Quester quester = plugin.getQuester(player.getUniqueId());
                    final ObjectiveType type = ObjectiveType.SMELT_ITEM;
                    final Set<String> dispatchedQuestIDs = new HashSet<String>();
                    for (final Quest quest : plugin.getLoadedQuests()) {
                        if (!quester.meetsCondition(quest, true)) {
                            continue;
                        }
                        
                        if (quester.getCurrentQuests().containsKey(quest) 
                                && quester.getCurrentStage(quest).containsObjective(type)) {
                            quester.smeltItem(quest, evt.getCurrentItem());
                        }
                        
                        dispatchedQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, type, (final Quester q, final Quest cq) -> {
                            if (!dispatchedQuestIDs.contains(cq.getId())) {
                                q.smeltItem(cq, evt.getCurrentItem());
                            }
                            return null;
                        }));
                    }
                }
            } else if (evt.getInventory().getType() == InventoryType.BREWING) {
                if (evt.getSlotType() == SlotType.CRAFTING) {
                    final Quester quester = plugin.getQuester(player.getUniqueId());
                    final ObjectiveType type = ObjectiveType.BREW_ITEM;
                    final Set<String> dispatchedQuestIDs = new HashSet<String>();
                    for (final Quest quest : plugin.getLoadedQuests()) {
                        if (!quester.meetsCondition(quest, true)) {
                            continue;
                        }
                        
                        if (quester.getCurrentQuests().containsKey(quest) 
                                && quester.getCurrentStage(quest).containsObjective(type)) {
                            quester.brewItem(quest, evt.getCurrentItem());
                        }
                        
                        dispatchedQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, type, (final Quester q, final Quest cq) -> {
                            if (!dispatchedQuestIDs.contains(cq.getId())) {
                                q.brewItem(cq, evt.getCurrentItem());
                            }
                            return null;
                        }));
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void onEnchantItem(final EnchantItemEvent evt) {
        if (plugin.canUseQuests(evt.getEnchanter().getUniqueId())) {
            final ItemStack enchantedItem = evt.getItem().clone();
            enchantedItem.setAmount(1);
            try {
                enchantedItem.addEnchantments(evt.getEnchantsToAdd());
            } catch (final IllegalArgumentException e) {
                // Ignore
            }
            final Quester quester = plugin.getQuester(evt.getEnchanter().getUniqueId());
            final ObjectiveType type = ObjectiveType.ENCHANT_ITEM;
            final Set<String> dispatchedQuestIDs = new HashSet<String>();
            for (final Quest quest : plugin.getLoadedQuests()) {
                if (!quester.meetsCondition(quest, true)) {
                    continue;
                }
                
                if (quester.getCurrentQuests().containsKey(quest) 
                        && quester.getCurrentStage(quest).containsObjective(type)) {
                    quester.enchantItem(quest, enchantedItem);
                }
                
                dispatchedQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, type, (final Quester q, final Quest cq) -> {
                    if (!dispatchedQuestIDs.contains(cq.getId())) {
                        q.enchantItem(cq, enchantedItem);
                    }
                    return null;
                }));
            }
        }
    }
    
    
    @EventHandler
    public void onConsumeItem(final PlayerItemConsumeEvent evt) {
        if (plugin.canUseQuests(evt.getPlayer().getUniqueId())) {
            final ItemStack consumedItem = evt.getItem().clone();
            consumedItem.setAmount(1);
            final Quester quester = plugin.getQuester(evt.getPlayer().getUniqueId());
            final ObjectiveType type = ObjectiveType.CONSUME_ITEM;
            final Set<String> dispatchedQuestIDs = new HashSet<String>();
            for (final Quest quest : plugin.getLoadedQuests()) {
                if (!quester.meetsCondition(quest, true)) {
                    continue;
                }
                
                if (quester.getCurrentQuests().containsKey(quest) 
                        && quester.getCurrentStage(quest).containsObjective(type)) {
                    quester.consumeItem(quest, consumedItem);
                }
                
                dispatchedQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, type, (final Quester q, final Quest cq) -> {
                    if (!dispatchedQuestIDs.contains(cq.getId())) {
                        q.consumeItem(cq, consumedItem);
                    }
                    return null;
                }));
            }
        }
    }
}
