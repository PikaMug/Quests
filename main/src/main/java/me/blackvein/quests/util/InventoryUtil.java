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

package me.blackvein.quests.util;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class InventoryUtil {
    
    /**
     * Adds item to player's inventory. If full, item is dropped at player's location.
     * 
     * @param player to try giving item to
     * @param item Item with amount to add
     * @throws NullPointerException if item is null
     */
    public static void addItem(Player player, ItemStack item) throws NullPointerException {
        if (item == null) {
            throw new NullPointerException("Null item while trying to add to inventory of " + player.getName());
        }
        PlayerInventory inv = player.getInventory();
        HashMap<Integer, ItemStack> leftovers = inv.addItem(item);
        if (leftovers != null) {
            if (leftovers.isEmpty() == false) {
                for (ItemStack leftover : leftovers.values()) {
                    player.getWorld().dropItem(player.getLocation(), leftover);
                }
            }
        }
    }
    
    /**
     * Removes item from player's inventory.
     * 
     * @param inventory Inventory to remove from
     * @param item Item with amount to remove
     * @return true if successful
     */
    public static boolean removeItem(Inventory inventory, ItemStack item) {
        int amount = item.getAmount();
        HashMap<Integer, ? extends ItemStack> allItems = inventory.all(item.getType());
        HashMap<Integer, Integer> removeFrom = new HashMap<Integer, Integer>();
        int foundAmount = 0;
        for (Map.Entry<Integer, ? extends ItemStack> items : allItems.entrySet()) {
            if (ItemUtil.compareItems(item, items.getValue(), true) == 0) {
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
            for (Map.Entry<Integer, Integer> toRemove : removeFrom.entrySet()) {
                ItemStack i = inventory.getItem(toRemove.getKey());
                if (i.getAmount() - toRemove.getValue() <= 0) {
                    inventory.clear(toRemove.getKey());
                } else {
                    i.setAmount(i.getAmount() - toRemove.getValue());
                    inventory.setItem(toRemove.getKey(), i);
                }
            }
            return true;
        }
        return false;
    }
}
