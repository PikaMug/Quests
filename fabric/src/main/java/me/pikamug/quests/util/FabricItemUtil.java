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

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class FabricItemUtil {

    public static boolean isSimilar(ItemStack a, ItemStack b) {
        if (a == null || b == null) return a == b;
        return a.getItem() == b.getItem() && a.getCount() == b.getCount();
    }

    public static boolean matchesGoal(ItemStack item, ItemStack goal) {
        if (item == null || goal == null) return false;
        return item.getItem() == goal.getItem();
    }

    public static String serialize(ItemStack item) {
        if (item == null || item.isEmpty()) return "AIR";
        return item.getItem().toString() + (item.getCount() > 1 ? " x" + item.getCount() : "");
    }

    public static ItemStack deserialize(String str) {
        if (str == null || str.isEmpty() || str.equalsIgnoreCase("AIR")) {
            return ItemStack.EMPTY;
        }
        final String[] parts = str.split(" x");
        final String materialName = parts[0];
        int count = 1;
        if (parts.length > 1) {
            try {
                count = Integer.parseInt(parts[1]);
            } catch (final NumberFormatException ignored) {}
        }
        // Try to find the item by registry name
        final var registry = net.minecraft.core.Registry.ITEM;
        final var optional = registry.getOptional(new net.minecraft.resources.ResourceLocation(materialName.toLowerCase()));
        if (optional.isPresent()) {
            return new ItemStack(optional.get(), count);
        }
        return ItemStack.EMPTY;
    }

    public static String getName(ItemStack item) {
        if (item == null || item.isEmpty()) return "Air";
        return item.getHoverName().getString();
    }

    public static String getMaterialName(ItemStack item) {
        if (item == null || item.isEmpty()) return "AIR";
        return item.getItem().toString();
    }

    public static String getDisplayString(ItemStack item) {
        if (item == null || item.isEmpty()) return "Air";
        final String name = item.getHoverName().getString();
        final int count = item.getCount();
        if (count > 1) {
            return name + " x" + count;
        }
        return name;
    }

    /**
     * Checks whether an ItemStack is a Quest Journal
     *
     * @param is ItemStack to check
     * @return true if item is a written book carrying the Quests journal marker
     */
    public static boolean isJournal(ItemStack is) {
        if (is == null || is.isEmpty() || is.getItem() != Items.WRITTEN_BOOK) return false;
        if (is.getTag() == null) return false;
        return is.getTag().getBoolean("quests.journal");
    }
}
