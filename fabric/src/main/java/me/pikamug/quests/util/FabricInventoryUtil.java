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

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class FabricInventoryUtil {

    public static int countItem(ServerPlayer player, ItemStack item) {
        if (player == null || item == null || item.isEmpty()) return 0;
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            final ItemStack slot = player.getInventory().getItem(i);
            if (!slot.isEmpty() && slot.getItem() == item.getItem()) {
                count += slot.getCount();
            }
        }
        return count;
    }

    public static boolean addItem(ServerPlayer player, ItemStack item) {
        if (player == null || item == null || item.isEmpty()) return false;
        return player.getInventory().add(item);
    }

    public static boolean removeItem(ServerPlayer player, ItemStack item) {
        if (player == null || item == null || item.isEmpty()) return false;
        int remaining = item.getCount();
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            final ItemStack slot = player.getInventory().getItem(i);
            if (!slot.isEmpty() && slot.getItem() == item.getItem()) {
                final int toRemove = Math.min(slot.getCount(), remaining);
                slot.shrink(toRemove);
                remaining -= toRemove;
            }
        }
        return remaining == 0;
    }

    public static int getArmorCount(ServerPlayer player, net.minecraft.world.item.Item item) {
        int count = 0;
        for (final EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) continue;
            final ItemStack armor = player.getItemBySlot(slot);
            if (!armor.isEmpty() && armor.getItem() == item) {
                count += armor.getCount();
            }
        }
        return count;
    }
}
