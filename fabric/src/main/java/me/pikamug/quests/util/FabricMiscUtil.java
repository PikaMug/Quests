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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        return entityTypeCache.computeIfAbsent(name.toUpperCase(Locale.ROOT), k -> {
            final Identifier id = Identifier.tryParse(name.toLowerCase(Locale.ROOT));
            if (id == null) return null;
            return BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
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

    public static boolean hasPermission(ServerPlayer player, PermissionLevel level) {
        if (player == null || level == null) return false;
        final PermissionSet set = player.permissions();
        if (set instanceof LevelBasedPermissionSet lbs) {
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

    public static String getProperDyeColor(final String input) {
        if (input == null) return null;
        final String stripped = input.toLowerCase().replace("_", "").replace(" ", "");
        if (stripped.equals("lightgray") || stripped.equals("silver")) return "LIGHT_GRAY";
        if (stripped.equals("dark") || stripped.equals("darkgray")) return "DARK_GRAY";
        return stripped.toUpperCase();
    }

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static String parseString(final String input) {
        if (input == null) return "";
        String parsed = input
                .replace("<black>", ChatFormatting.BLACK.toString())
                .replace("<darkblue>", ChatFormatting.DARK_BLUE.toString())
                .replace("<darkgreen>", ChatFormatting.DARK_GREEN.toString())
                .replace("<darkaqua>", ChatFormatting.DARK_AQUA.toString())
                .replace("<darkred>", ChatFormatting.DARK_RED.toString())
                .replace("<purple>", ChatFormatting.DARK_PURPLE.toString())
                .replace("<gold>", ChatFormatting.GOLD.toString())
                .replace("<grey>", ChatFormatting.GRAY.toString())
                .replace("<gray>", ChatFormatting.GRAY.toString())
                .replace("<darkgrey>", ChatFormatting.DARK_GRAY.toString())
                .replace("<darkgray>", ChatFormatting.DARK_GRAY.toString())
                .replace("<blue>", ChatFormatting.BLUE.toString())
                .replace("<green>", ChatFormatting.GREEN.toString())
                .replace("<aqua>", ChatFormatting.AQUA.toString())
                .replace("<red>", ChatFormatting.RED.toString())
                .replace("<pink>", ChatFormatting.LIGHT_PURPLE.toString())
                .replace("<yellow>", ChatFormatting.YELLOW.toString())
                .replace("<white>", ChatFormatting.WHITE.toString())
                .replace("<random>", ChatFormatting.OBFUSCATED.toString())
                .replace("<italic>", ChatFormatting.ITALIC.toString())
                .replace("<i>", ChatFormatting.ITALIC.toString())
                .replace("<em>", ChatFormatting.ITALIC.toString())
                .replace("<bold>", ChatFormatting.BOLD.toString())
                .replace("<b>", ChatFormatting.BOLD.toString())
                .replace("<underline>", ChatFormatting.UNDERLINE.toString())
                .replace("<u>", ChatFormatting.UNDERLINE.toString())
                .replace("<strike>", ChatFormatting.STRIKETHROUGH.toString())
                .replace("<st>", ChatFormatting.STRIKETHROUGH.toString())
                .replace("<obf>", ChatFormatting.OBFUSCATED.toString())
                .replace("<reset>", ChatFormatting.RESET.toString())
                .replace("<br>", "\n");
        parsed = translateAlternateColorCodes(parsed);
        final Matcher matcher = HEX_PATTERN.matcher(parsed);
        while (matcher.find()) {
            final StringBuilder hex = new StringBuilder("§x");
            for (final char c : matcher.group(1).toLowerCase(Locale.ROOT).toCharArray()) {
                hex.append('§').append(c);
            }
            parsed = parsed.replace(matcher.group(), hex.toString());
        }
        return parsed;
    }

    private static String translateAlternateColorCodes(final String input) {
        final char[] b = input.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1]) > -1) {
                b[i] = '§';
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }
        return new String(b);
    }
}
