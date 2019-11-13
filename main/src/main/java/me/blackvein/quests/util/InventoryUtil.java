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
     * @throws NullPointerException when ItemStack is null
     */
    public static void addItem(Player p, ItemStack i) throws Exception {
        if (i == null) {
            throw new NullPointerException("Null item while trying to add to inventory of " + p.getName());
        }
        PlayerInventory inv = p.getInventory();
        if (i != null) {
            HashMap<Integer, ItemStack> leftover = inv.addItem(i);
            if (leftover != null) {
                if (leftover.isEmpty() == false) {
                    for (ItemStack i2 : leftover.values()) {
                        p.getWorld().dropItem(p.getLocation(), i2);
                    }
                }
            }
        }
    }
    
    /**
     * Removes item from player's inventory
     * 
     * @param inventory Inventory to remove from
     * @param is Item with amount to remove
     * @return true if successful
     */
    public static boolean removeItem(Inventory inventory, ItemStack is) {
        int amount = is.getAmount();
        HashMap<Integer, ? extends ItemStack> allItems = inventory.all(is.getType());
        HashMap<Integer, Integer> removeFrom = new HashMap<Integer, Integer>();
        int foundAmount = 0;
        for (Map.Entry<Integer, ? extends ItemStack> item : allItems.entrySet()) {
            if (ItemUtil.compareItems(is, item.getValue(), true) == 0) {
                if (item.getValue().getAmount() >= amount - foundAmount) {
                    removeFrom.put(item.getKey(), amount - foundAmount);
                    foundAmount = amount;
                } else {
                    foundAmount += item.getValue().getAmount();
                    removeFrom.put(item.getKey(), item.getValue().getAmount());
                }
                if (foundAmount >= amount) {
                    break;
                }
            }
        }
        if (foundAmount == amount) {
            for (Map.Entry<Integer, Integer> toRemove : removeFrom.entrySet()) {
                ItemStack item = inventory.getItem(toRemove.getKey());
                if (item.getAmount() - toRemove.getValue() <= 0) {
                    inventory.clear(toRemove.getKey());
                } else {
                    item.setAmount(item.getAmount() - toRemove.getValue());
                    inventory.setItem(toRemove.getKey(), item);
                }
            }
            return true;
        }
        return false;
    }
}
