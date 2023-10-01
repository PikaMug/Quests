/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.util;

import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;

public class BukkitInventoryUtil {
    
    /**
     * Adds item to player's inventory. If full, item is dropped at player's location.
     * 
     * @param player to try giving item to
     * @param item Item with amount to add
     * @throws NullPointerException if item is null
     */
    public static void addItem(final Player player, final ItemStack item) throws NullPointerException {
        if (item == null) {
            throw new NullPointerException("Null item while trying to add to inventory of " + player.getName());
        }
        final PlayerInventory inv = player.getInventory();
        final HashMap<Integer, ItemStack> leftovers = inv.addItem(item);
        if (!leftovers.isEmpty()) {
            for (final ItemStack leftover : leftovers.values()) {
                player.getWorld().dropItem(player.getLocation(), leftover);
            }
        }
    }
    
    /**
     * Whether an item can be removed from player's inventory.
     * 
     * @param inventory Inventory to check
     * @param item Item with amount for removal
     * @return true if possible
     */
    public static boolean canRemoveItem(final Inventory inventory, final ItemStack item) {
        final int amount = item.getAmount();
        final HashMap<Integer, ? extends ItemStack> allItems = inventory.all(item.getType());
        int foundAmount = 0;
        for (final Map.Entry<Integer, ? extends ItemStack> items : allItems.entrySet()) {
            if (BukkitItemUtil.compareItems(item, items.getValue(), true) == 0) {
                if (items.getValue().getAmount() >= amount - foundAmount) {
                    foundAmount = amount;
                } else {
                    foundAmount += items.getValue().getAmount();
                }
                if (foundAmount >= amount) {
                    break;
                }
            }
        }
        return foundAmount == amount;
    }
    
    /**
     * Removes item from player's inventory.
     * 
     * @param inventory Inventory to remove from
     * @param item Item with amount to remove
     * @return true if successful
     */
    public static boolean removeItem(final Inventory inventory, final ItemStack item) {
        final int amount = item.getAmount();
        final HashMap<Integer, ? extends ItemStack> allItems = inventory.all(item.getType());
        final HashMap<Integer, Integer> removeFrom = new HashMap<>();
        int foundAmount = 0;
        for (final Map.Entry<Integer, ? extends ItemStack> items : allItems.entrySet()) {
            if (BukkitItemUtil.compareItems(item, items.getValue(), true) == 0) {
                if (items.getValue().getAmount() >= amount - foundAmount) {
                    removeFrom.put(items.getKey(), amount - foundAmount);
                    foundAmount = amount;
                } else {
                    foundAmount += items.getValue().getAmount();
                    removeFrom.put(items.getKey(), items.getValue().getAmount());
                }
                if (foundAmount >= amount) {
                    break;
                }
            }
        }
        if (foundAmount == amount) {
            for (final Map.Entry<Integer, Integer> toRemove : removeFrom.entrySet()) {
                final ItemStack i = inventory.getItem(toRemove.getKey());
                if (i != null) {
                    if (i.getAmount() - toRemove.getValue() <= 0) {
                        inventory.clear(toRemove.getKey());
                    } else {
                        i.setAmount(i.getAmount() - toRemove.getValue());
                        inventory.setItem(toRemove.getKey(), i);
                    }
                }
            }
            return true;
        }
        return false;
    }
    
    /**
     * Removes item from entity's equipment.
     * 
     * @param equipment EntityEquipment to remove from
     * @param item Item with amount to remove
     * @return true if successful
     */
    public static boolean stripItem(final EntityEquipment equipment, final ItemStack item) {
        final int amount = item.getAmount();
        final ItemStack[] allItems = equipment.getArmorContents();
        final HashMap<Integer, Integer> removeFrom = new HashMap<>();
        int foundAmount = 0;
        
        int index = 0;
        for (final ItemStack i : allItems) {
            if (i == null) {
                index++;
                continue;
            }
            if (BukkitItemUtil.compareItems(item, i, true) == 0) {
                if (i.getAmount() >= amount - foundAmount) {
                    removeFrom.put(index, amount - foundAmount);
                    foundAmount = amount;
                } else {
                    foundAmount += i.getAmount();
                    removeFrom.put(index, i.getAmount());
                }
                if (foundAmount >= amount) {
                    break;
                }
            }
            index++;
        }
        if (foundAmount == amount) {
            for (final Map.Entry<Integer, Integer> toRemove : removeFrom.entrySet()) {
                final ItemStack i = allItems[toRemove.getKey()];
                if (i.getAmount() - toRemove.getValue() <= 0) {
                    allItems[toRemove.getKey()] = null;
                } else {
                    i.setAmount(i.getAmount() - toRemove.getValue());
                    allItems[toRemove.getKey()] = i;
                }
            }
            equipment.setArmorContents(allItems);
            return true;
        }
        return false;
    }
}
