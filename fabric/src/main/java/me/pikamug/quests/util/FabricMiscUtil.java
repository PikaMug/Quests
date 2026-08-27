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

import me.pikamug.quests.FabricQuestsPlugin;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FabricMiscUtil {

    private static final Map<String, EntityType<?>> entityTypeCache = new ConcurrentHashMap<>();

    public static EntityType<?> getEntityType(String name) {
        return entityTypeCache.computeIfAbsent(name.toUpperCase(), k -> {
            final Optional<EntityType<?>> type = EntityType.byString(k);
            return type.orElse(null);
        });
    }

    public static String getEntityName(EntityType<?> type) {
        if (type == null) return "Unknown";
        return type.getDescription().getString();
    }

    public static String formatTime(long millis) {
        final long seconds = millis / 1000;
        final long minutes = seconds / 60;
        final long hours = minutes / 60;
        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        }
        return String.format("%ds", seconds);
    }

    public static ServerPlayer getPlayer(UUID uuid, FabricQuestsPlugin plugin) {
        if (plugin.getServer() == null) return null;
        return plugin.getServer().getPlayerList().getPlayer(uuid);
    }

    public static String locationToString(ServerLevel level, Vec3 pos) {
        return level.dimension().location() + " " + (int) pos.x + " " + (int) pos.y + " " + (int) pos.z;
    }

    public static boolean isItemType(ItemStack item, String materialName) {
        if (item == null || materialName == null) return false;
        return item.getItem().toString().equalsIgnoreCase(materialName);
    }

    public static String getCapitalized(final String input) {
        if (input.isEmpty()) {
            return input;
        }
        final String firstLetter = input.substring(0, 1);
        final String remainder = input.substring(1);
        return firstLetter.toUpperCase() + remainder.toLowerCase();
    }
}
