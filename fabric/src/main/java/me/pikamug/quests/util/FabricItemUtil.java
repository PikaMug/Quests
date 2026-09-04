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

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import me.pikamug.quests.FabricQuestsPlugin;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

import java.util.Optional;

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
        final var registry = net.minecraft.core.registries.BuiltInRegistries.ITEM;
        final Identifier id = Identifier.tryParse(materialName.toLowerCase());
        final var optional = id == null ? Optional.<net.minecraft.world.item.Item>empty() : registry.getOptional(id);
        if (optional.isPresent()) {
            return new ItemStack(optional.get(), count);
        }
        return ItemStack.EMPTY;
    }

    /**
     * Serializes an {@link ItemStack} to its CODEC JSON form, preserving count and all data components
     * (enchantments, custom name, lore, etc.). This is the preferred form for config files.
     *
     * @param stack ItemStack to serialize
     * @return the CODEC {@link JsonElement}, or {@link JsonNull} if the stack is null or empty
     */
    public static JsonElement serializeToJson(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return JsonNull.INSTANCE;
        }
        final DataResult<JsonElement> result = ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, stack);
        if (result.result().isPresent()) {
            return result.result().get();
        }
        FabricQuestsPlugin.LOGGER.warn("Could not serialize item (upgrade format?) {}",
                result.error().map(error -> error.message()).orElse(stack.toString()));
        return JsonNull.INSTANCE;
    }

    /**
     * Deserializes an {@link ItemStack} from its CODEC JSON form (see {@link #serializeToJson}). If the format
     * cannot be read (e.g. a config written by a newer Minecraft version), a warning is logged to the server console
     * and an empty stack is returned so the offending node degrades gracefully rather than stalling startup.
     *
     * @param element the CODEC {@link JsonElement}
     * @return the deserialized {@link ItemStack}, or {@link ItemStack#EMPTY} on failure
     */
    public static ItemStack deserializeFromJson(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return ItemStack.EMPTY;
        }
        try {
            final DataResult<ItemStack> result = ItemStack.CODEC.parse(JsonOps.INSTANCE, element);
            if (result.result().isPresent()) {
                return result.result().get();
            }
            FabricQuestsPlugin.LOGGER.warn("Could not load item from config (upgrade format?) {}",
                    result.error().map(error -> error.message()).orElse(element.toString()));
        } catch (final Exception e) {
            FabricQuestsPlugin.LOGGER.warn("Could not load item from config (upgrade format?) {}", element, e);
        }
        return ItemStack.EMPTY;
    }

    /**
     * Compares two stacks by material, count and data components (enchantments, custom name, lore, etc.).
     * Amounts are optional; when {@code ignoreAmount} is true, count is not considered.
     *
     * @param one          first stack
     * @param two          second stack
     * @param ignoreAmount whether to ignore stack counts
     * @return 1 if either stack is null, 2 if amounts differ (when compared), 3 if items differ, 4 if components differ,
     *         0 if equivalent
     */
    public static int compareItems(ItemStack one, ItemStack two, boolean ignoreAmount) {
        if (one == null || two == null) return 1;
        if (!ignoreAmount && one.getCount() != two.getCount()) return 2;
        if (!ItemStack.isSameItem(one, two)) return 3;
        if (!ItemStack.isSameItemSameComponents(one, two)) return 4;
        return 0;
    }

    /**
     * Returns whether the two stacks are equivalent for a quest objective, comparing material, count and components.
     */
    public static boolean matches(ItemStack one, ItemStack two) {
        return compareItems(one, two, false) == 0;
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
        final net.minecraft.world.item.component.CustomData data = is.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (data == null || data.isEmpty()) return false;
        return data.copyTag().getBooleanOr("quests.journal", false);
    }
}
