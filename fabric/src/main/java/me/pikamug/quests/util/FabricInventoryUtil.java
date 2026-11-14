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

import java.util.List;

public class FabricInventoryUtil {

    public static boolean hasItems(ServerPlayer player, List<?> items) {
        if (player == null || items == null) return false;
        final int size = player.getInventory().getContainerSize();
        final int[] available = new int[size];
        for (int i = 0; i < size; i++) {
            available[i] = player.getInventory().getItem(i).getCount();
        }
        for (final Object itemObj : items) {
            if (!(itemObj instanceof ItemStack)) return false;
            final ItemStack item = (ItemStack) itemObj;
            if (item.isEmpty()) continue;
            int needed = item.getCount();
            for (int slot = 0; slot < size && needed > 0; slot++) {
                if (available[slot] <= 0) continue;
                final ItemStack stack = player.getInventory().getItem(slot);
                if (!stack.isEmpty() && FabricItemUtil.matches(stack, item)) {
                    final int taken = Math.min(available[slot], needed);
                    available[slot] -= taken;
                    needed -= taken;
                }
            }
            if (needed > 0) return false;
        }
        return true;
    }

    public static int countItem(ServerPlayer player, ItemStack item) {
        if (player == null || item == null || item.isEmpty()) return 0;
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            final ItemStack slot = player.getInventory().getItem(i);
            if (!slot.isEmpty() && FabricItemUtil.matches(slot, item)) {
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
        final int amount = item.getCount();
        final int size = player.getInventory().getContainerSize();
        final int[] slots = new int[size];
        final int[] counts = new int[size];
        int found = 0;
        int used = 0;
        for (int i = 0; i < size && found < amount; i++) {
            final ItemStack slot = player.getInventory().getItem(i);
            if (!slot.isEmpty() && FabricItemUtil.matches(slot, item)) {
                final int take = Math.min(slot.getCount(), amount - found);
                slots[used] = i;
                counts[used] = take;
                used++;
                found += take;
            }
        }
        if (found < amount) return false;
        for (int i = 0; i < used; i++) {
            player.getInventory().getItem(slots[i]).shrink(counts[i]);
        }
        return true;
    }

    public static boolean stripItem(ServerPlayer player, ItemStack item) {
        if (player == null || item == null || item.isEmpty()) return false;
        final int amount = item.getCount();
        final int[] slots = new int[EquipmentSlot.values().length];
        final int[] counts = new int[EquipmentSlot.values().length];
        int found = 0;
        int used = 0;
        for (final EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) continue;
            final ItemStack armor = player.getItemBySlot(slot);
            if (!armor.isEmpty() && FabricItemUtil.matches(armor, item)) {
                final int take = Math.min(armor.getCount(), amount - found);
                slots[used] = slot.ordinal();
                counts[used] = take;
                used++;
                found += take;
                if (found >= amount) break;
            }
        }
        if (found < amount) return false;
        for (int i = 0; i < used; i++) {
            player.getItemBySlot(EquipmentSlot.values()[slots[i]]).shrink(counts[i]);
        }
        return true;
    }

    /**
     * Removes up to {@code count} items matching the goal by material and data
     * components, regardless of stack amounts. Used by item-exchange objectives.
     */
    public static boolean removeItemIgnoreAmount(ServerPlayer player, ItemStack goal, int count) {
        if (player == null || goal == null || goal.isEmpty() || count <= 0) return false;
        int remaining = count;
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            final ItemStack slot = player.getInventory().getItem(i);
            if (!slot.isEmpty() && FabricItemUtil.compareItems(slot, goal, true) == 0) {
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
