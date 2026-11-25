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
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FabricMiscUtil {

    private static final Map<String, EntityType<?>> entityTypeCache = new ConcurrentHashMap<>();

    public static final EntityType<?> PLAYER = getEntityType("player");
    public static final EntityType<?> COW = getEntityType("cow");
    public static final EntityType<?> MOOSHROOM = getEntityType("mooshroom");
    public static final EntityType<?> SHEEP = getEntityType("sheep");
    @SuppressWarnings("unchecked")
    public static final EntityType<LightningBolt> LIGHTNING_BOLT = (EntityType<LightningBolt>) getEntityType("lightning_bolt");

    public static EntityType<?> getEntityType(String name) {
        if (name == null) return null;
        final String normalized = normalizeEntityId(name);
        if (normalized.isEmpty()) return null;
        return entityTypeCache.computeIfAbsent(normalized.toUpperCase(Locale.ROOT), k -> {
            final Identifier id = Identifier.tryParse(normalized);
            if (id == null) return null;
            return BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
        });
    }

    private static String normalizeEntityId(final String name) {
        String normalized = name.trim().replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                .toLowerCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
        if (normalized.indexOf(':') == -1) {
            switch (normalized.replace("_", "")) {
            case "mushroomcow":
                normalized = "mooshroom";
                break;
            case "pigzombie":
                normalized = "zombified_piglin";
                break;
            case "snowman":
                normalized = "snow_golem";
                break;
            case "firework":
                normalized = "firework_rocket";
                break;
            case "primedtnt":
                normalized = "tnt";
                break;
            case "lightning":
                normalized = "lightning_bolt";
                break;
            default:
                break;
            }
        }
        return normalized;
    }

    public static String getPrettyMobName(EntityType<?> type) {
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

    public static boolean hasPermission(ServerPlayer player, PermissionLevel level) {
        if (player == null || level == null) return false;
        final PermissionSet set = player.permissions();
        if (set instanceof LevelBasedPermissionSet) {
            LevelBasedPermissionSet lbs = (LevelBasedPermissionSet) set;
            return lbs.level().isEqualOrHigherThan(level);
        }
        return false;
    }

    public static String locationToString(ServerLevel level, Vec3 pos) {
        return level.dimension().identifier() + " " + (int) pos.x + " " + (int) pos.y + " " + (int) pos.z;
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

    public static String getTime(final Long millis) {
        return formatTime(millis != null ? millis : 0L);
    }

    public static List<ServerLevel> getWorlds() {
        final FabricQuestsPlugin plugin = FabricQuestsPlugin.getInstance();
        if (plugin.getServer() == null) return new LinkedList<>();
        final List<ServerLevel> worlds = new LinkedList<>();
        plugin.getServer().getAllLevels().forEach(worlds::add);
        return worlds;
    }

    public static EntityType<?> getProperMobType(final String mob) {
        return getEntityType(mob);
    }

    public static String getMobTypeName(final EntityType<?> type) {
        if (type == null) return "Unknown";
        return snakeCaseToUpperCamelCase(EntityType.getKey(type).getPath());
    }

    public static String snakeCaseToUpperCamelCase(final String input) {
        if (input == null || input.isEmpty()) return input;
        final String[] parts = input.toLowerCase().split("_");
        final StringBuilder sb = new StringBuilder();
        for (final String part : parts) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
        }
        return sb.toString();
    }

    /**
     * Gets player-friendly name from type. 'LIGHT_BLUE' becomes 'Light Blue'
     *
     * @param color any dye type, ideally
     * @return cleaned-up string, or 'White' if null
     */
    public static String getPrettyDyeColorName(final DyeColor color) {
        if (color == null) return "White";
        final String key = "COLOR_" + color.name();
        final String translated = FabricLang.get(key);
        if (translated != null && !translated.equalsIgnoreCase("NULL") && !translated.equals(key)) {
            return translated;
        }
        final String[] parts = color.getSerializedName().split("_");
        final StringBuilder sb = new StringBuilder();
        for (final String part : parts) {
            if (!part.isEmpty()) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
        }
        return sb.toString();
    }

    /**
     * Gets DyeColor from name
     *
     * @param properName Name to get type from
     * @return DyeColor or null if invalid
     */
    public static DyeColor getProperDyeColor(final String properName) {
        if (properName == null) {
            return null;
        }
        final String stripped = properName.trim().toLowerCase(Locale.ROOT)
                .replace("_", "").replace(" ", "").replace("-", "");
        if (stripped.isEmpty() || stripped.equals("null")) return DyeColor.WHITE;
        if (stripped.equals("silver")) return DyeColor.LIGHT_GRAY;
        if (stripped.equals("dark") || stripped.equals("darkgray")) return DyeColor.GRAY;
        for (final DyeColor color : DyeColor.values()) {
            if (color.getSerializedName().replace("_", "").equals(stripped)) {
                return color;
            }
        }
        return null;
    }

    /**
     * Split text into multiple lines
     *
     * @param input Text to convert
     * @param wordDelimiter Character(s) used to split up text
     * @param lineLength Maximum number of characters per line
     * @param lineColor Color to use at start of each new line
     * @return Converted text
     */
    public static LinkedList<String> makeLines(final String input, final String wordDelimiter, final int lineLength,
                                               final ChatFormatting lineColor) {
        final LinkedList<String> toReturn = new LinkedList<>();
        final String[] split = input.split(wordDelimiter);
        StringBuilder line = new StringBuilder();
        int currentLength = 0;
        for (final String piece : split) {
            if ((currentLength + piece.length()) > (lineLength + 1)) {
                // TODO - determine whether replaceAll and carots (^) are necessary here
                final String s = line.toString().replaceAll("^" + wordDelimiter, "");
                if (lineColor != null) {
                    toReturn.add(lineColor + s);
                } else {
                    toReturn.add(s);
                }
                line = new StringBuilder(piece + wordDelimiter);
                currentLength = piece.length() + 1;
            } else {
                line.append(piece).append(wordDelimiter);
                currentLength += piece.length() + 1;
            }
        }
        if (!line.toString().isEmpty())
            if (lineColor != null) {
                toReturn.add(lineColor + line.toString());
            } else {
                toReturn.add(line.toString());
            }
        return toReturn;
    }
}
