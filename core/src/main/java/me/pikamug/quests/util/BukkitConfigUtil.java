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

import io.github.znetworkw.znpcservers.npc.NPC;
import me.pikamug.quests.dependencies.BukkitDependencies;
import me.pikamug.quests.quests.Quest;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BukkitConfigUtil {
    
    private static final Pattern hexPattern = Pattern.compile("(?i)%#([0-9A-F]{6})%");
    
    /**
     * Checks whether items in a list are instances of a class<p>
     * 
     * Does NOT check whether list objects are null
     * 
     * @param list The list to check objects of
     * @param clazz The class to compare against
     * @return false if arg is null or list object does not match
     */
    public static boolean checkList(final List<?> list, final Class<?> clazz) {
        if (list == null || clazz == null) {
            return false;
        }
        for (final Object o : list) {
            if (o == null) {
                Bukkit.getLogger().severe(clazz.getSimpleName() + " type in Quests file was \"null\"");
                return false;
            }
            if (!clazz.isAssignableFrom(o.getClass())) {
                return false;
            }
        }
        return true;
    }
    
    public static Location getLocation(final String arg) {
        final String[] info = arg.split(" ");
        
        if (info.length < 4) {
            return null;
        }
        
        final StringBuilder sb = new StringBuilder();
        int index = 0;
        final int xIndex = info.length -3;
        final int yIndex = info.length -2;
        final int zIndex = info.length -1;
        
        while (index < xIndex) {
            final String s = info[index];
            if (index == 0) {
                sb.append(s);
            } else {
                sb.append(" ").append(s);
            }
            index++;
        }
        
        final String world = sb.toString();
        
        final double x;
        final double y;
        final double z;
        try {
            x = Double.parseDouble(info[xIndex]);
            y = Double.parseDouble(info[yIndex]);
            z = Double.parseDouble(info[zIndex]);
        } catch (final Exception e) {
            Bukkit.getLogger().severe("Please inform Quests developer location was wrong for "
                    + world + " " + info[xIndex] + " " + info[yIndex] + " " + info[zIndex] + " ");
            return null;
        }
        if (Bukkit.getServer().getWorld(world) == null) {
            Bukkit.getLogger().severe("Quests could not locate world " + world + ", is it loaded?");
            return null;
        }
        return new Location(Bukkit.getServer().getWorld(world), x, y, z);
    }

    public static String getLocationInfo(final Location loc) {
        return Objects.requireNonNull(loc.getWorld()).getName() + " " + loc.getX() + " " + loc.getY() + " "
                + loc.getZ();
    }
    
    public static String[] parseStringWithPossibleLineBreaks(final String s, final Quest quest, final Player player) {
        String parsed = parseString(s);
        if (parsed.contains("<npc>")) {
            if (quest.getNpcStart() != null) {
                parsed = parsed.replace("<npc>", quest.getNpcStartName());
            } else {
                Bukkit.getLogger().warning(quest.getName() + " quest uses <npc> tag but doesn't have an NPC start set");
            }
        }
        if (BukkitDependencies.placeholder != null && player != null) {
            parsed = PlaceholderAPI.setPlaceholders(player, parsed);
        }
        return parsed.split("\n");
    }
    
    public static String[] parseStringWithPossibleLineBreaks(final String s, final Quest quest) {
        String parsed = parseString(s);
        if (parsed.contains("<npc>")) {
            if (quest.getNpcStart() != null) {
                parsed = parsed.replace("<npc>", quest.getNpcStartName());
            } else {
                Bukkit.getLogger().warning(quest.getName() + " quest uses <npc> tag but doesn't have an NPC start set");
            }
        }
        return parsed.split("\n");
    }

    public static String[] parseStringWithPossibleLineBreaks(final String s, final UUID npc, int amount) {
        String parsed = parseString(s, npc);
        if (parsed.contains("<amount>")) {
            parsed = parsed.replace("<amount>", String.valueOf(amount));
        }
        return parsed.split("\n");
    }
    
    public static String parseString(final String s, final Quest quest) {
        String parsed = parseString(s);
        if (quest != null && quest.getName() != null) {
            parsed = parsed.replace("<quest>", quest.getName());
            if (parsed.contains("<npc>")) {
                if (quest.getNpcStart() != null) {
                    parsed = parsed.replace("<npc>", quest.getNpcStartName());
                } else {
                    Bukkit.getLogger().warning(quest.getName() + " quest uses <npc> tag but doesn't have an NPC start set");
                }
            }
        }
        return parsed;
    }
    
    public static String parseString(final String s, final Quest quest, final Player player) {
        String parsed = parseString(s, quest);
        if (BukkitDependencies.placeholder != null && player != null) {
            parsed = PlaceholderAPI.setPlaceholders(player, parsed);
        }
        return parsed;
    }

    public static String parseString(final String s, final UUID npc) {
        String parsed = parseString(s);
        if (parsed.contains("<npc>")) {
            if (BukkitDependencies.citizens != null) {
                parsed = parsed.replace("<npc>", BukkitDependencies.citizens.getNPCRegistry().getByUniqueId(npc).getName());
            } else if (BukkitDependencies.znpcsPlus != null) {
                String name = "null";
                final Optional<NPC> opt = NPC.all().stream().filter(npc1 -> npc1.getUUID().equals(npc)).findAny();
                if (opt.isPresent()) {
                    final Entity znpc = (Entity) opt.get().getBukkitEntity();
                    if (znpc.getCustomName() != null) {
                        name = znpc.getCustomName();
                    } else {
                        name = opt.get().getNpcPojo().getHologramLines().get(0);
                    }
                }
                parsed = parsed.replace("<npc>", name);
            }
        }
        return parsed;
    }
    
    public static String parseString(final String s) {
        String parsed = s;
        parsed = parsed.replace("<black>", ChatColor.BLACK.toString());
        parsed = parsed.replace("<darkblue>", ChatColor.DARK_BLUE.toString());
        parsed = parsed.replace("<darkgreen>", ChatColor.DARK_GREEN.toString());
        parsed = parsed.replace("<darkaqua>", ChatColor.DARK_AQUA.toString());
        parsed = parsed.replace("<darkred>", ChatColor.DARK_RED.toString());
        parsed = parsed.replace("<purple>", ChatColor.DARK_PURPLE.toString());
        parsed = parsed.replace("<gold>", ChatColor.GOLD.toString());
        parsed = parsed.replace("<grey>", ChatColor.GRAY.toString());
        parsed = parsed.replace("<gray>", ChatColor.GRAY.toString());
        parsed = parsed.replace("<darkgrey>", ChatColor.DARK_GRAY.toString());
        parsed = parsed.replace("<darkgray>", ChatColor.DARK_GRAY.toString());
        parsed = parsed.replace("<blue>", ChatColor.BLUE.toString());
        parsed = parsed.replace("<green>", ChatColor.GREEN.toString());
        parsed = parsed.replace("<aqua>", ChatColor.AQUA.toString());
        parsed = parsed.replace("<red>", ChatColor.RED.toString());
        parsed = parsed.replace("<pink>", ChatColor.LIGHT_PURPLE.toString());
        parsed = parsed.replace("<yellow>", ChatColor.YELLOW.toString());
        parsed = parsed.replace("<white>", ChatColor.WHITE.toString());
        parsed = parsed.replace("<random>", ChatColor.MAGIC.toString());
        parsed = parsed.replace("<italic>", ChatColor.ITALIC.toString());
        parsed = parsed.replace("<bold>", ChatColor.BOLD.toString());
        parsed = parsed.replace("<underline>", ChatColor.UNDERLINE.toString());
        parsed = parsed.replace("<strike>", ChatColor.STRIKETHROUGH.toString());
        parsed = parsed.replace("<reset>", ChatColor.RESET.toString());
        parsed = parsed.replace("<br>", "\n");
        parsed = ChatColor.translateAlternateColorCodes('&', parsed);
        
        final Matcher matcher = hexPattern.matcher(parsed);
        while (matcher.find()) {
            final StringBuilder hex = new StringBuilder();
            hex.append(ChatColor.COLOR_CHAR + "x");
            final char[] chars = matcher.group(1).toCharArray();
            for (final char aChar : chars) {
                hex.append(ChatColor.COLOR_CHAR).append(Character.toLowerCase(aChar));
            }
            parsed = parsed.replace(matcher.group(), hex.toString());
        }
        return parsed;
    }
}
