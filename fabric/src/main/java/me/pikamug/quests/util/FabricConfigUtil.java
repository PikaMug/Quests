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
import me.pikamug.quests.quests.Quest;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FabricConfigUtil {

    //private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern hexPattern = Pattern.compile("(?i)%#([0-9A-F]{6})%");

    public static String getLocationInfo(final BlockPos block) {
        return block.getX() + " " + block.getY() + " " + block.getZ();
    }

    /*public static String[] parseStringWithPossibleLineBreaks(final String s, final me.pikamug.quests.quests.Quest quest, final Player player) {
        String parsed = parseString(s);
        if (parsed.contains("<npc>")) {
            if (quest.getNpcStart() != null) {
                parsed = parsed.replace("<npc>", quest.getNpcStartName());
            } else {
                Bukkit.getLogger().warning(quest.getName() + " quest uses <npc> tag but doesn't have an NPC start set");
            }
        }
        if (FabricDependencies.placeholder != null && player != null) {
            parsed = PlaceholderAPI.setPlaceholders(player, parsed);
        }
        return parsed.split("\n");
    }*/

    public static String[] parseStringWithPossibleLineBreaks(final String s, final Quest quest) {
        String parsed = parseString(s);
        if (parsed.contains("<npc>")) {
            if (quest.getNpcStart() != null) {
                parsed = parsed.replace("<npc>", quest.getNpcStartName());
            } else {
                FabricQuestsPlugin.LOGGER.warn(quest.getName() + " quest uses <npc> tag but doesn't have an NPC start set");
            }
        }
        return parsed.split("\n");
    }

    /*public static String[] parseStringWithPossibleLineBreaks(final String s, final UUID npc, int amount, final FabricQuestsPlugin plugin) {
        String parsed = parseString(s, npc, plugin);
        if (parsed.contains("<amount>")) {
            parsed = parsed.replace("<amount>", String.valueOf(amount));
        }
        return parsed.split("\n");
    }*/

    public static String parseString(final String s, final Quest quest) {
        String parsed = parseString(s);
        if (quest != null && quest.getName() != null) {
            parsed = parsed.replace("<quest>", quest.getName());
            if (parsed.contains("<npc>")) {
                if (quest.getNpcStart() != null) {
                    parsed = parsed.replace("<npc>", quest.getNpcStartName());
                } else {
                    FabricQuestsPlugin.LOGGER.warn(quest.getName() + " quest uses <npc> tag but doesn't have an NPC start set");
                }
            }
        }
        return parsed;
    }

    public static String parseString(final String s, final me.pikamug.quests.quests.Quest quest, final ServerPlayer player) {
        String parsed = parseString(s, quest);
        /*if (FabricDependencies.placeholder != null && player != null) {
            parsed = PlaceholderAPI.setPlaceholders(player, parsed);
        }*/
        return parsed;
    }

    /*public static String parseString(final String s, final UUID npc, final FabricQuestsPlugin plugin) {
        String parsed = parseString(s);
        if (parsed.contains("<npc>")) {
            String name = "null";
            for (final FabricNpcDependency npcDependency : plugin.getDependencies().getNpcDependencies()) {
                final String npcName = npcDependency.getName(npc);
                if (npcName != null) {
                    name = npcName;
                    break;
                }
            }
            parsed = parsed.replace("<npc>", name);
        }
        return parsed;
    }*/

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
        final Matcher matcher = hexPattern.matcher(parsed);
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
