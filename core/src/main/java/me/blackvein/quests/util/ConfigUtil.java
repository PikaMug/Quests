/*
 * Copyright (c) 2014 PikaMug and contributors. All rights reserved.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package me.blackvein.quests.util;

import me.blackvein.quests.Dependencies;
import me.blackvein.quests.quests.IQuest;
import me.clip.placeholderapi.PlaceholderAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigUtil {
    
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
    
    public static String[] parseStringWithPossibleLineBreaks(final String s, final IQuest quest, final Player player) {
        String parsed = parseString(s);
        if (parsed.contains("<npc>")) {
            if (quest.getNpcStart() != null) {
                parsed = parsed.replace("<npc>", quest.getNpcStart().getName());
            } else {
                Bukkit.getLogger().warning(quest.getName() + " quest uses <npc> tag but doesn't have an NPC start set");
            }
        }
        if (Dependencies.placeholder != null && player != null) {
            parsed = PlaceholderAPI.setPlaceholders(player, parsed);
        }
        return parsed.split("\n");
    }
    
    public static String[] parseStringWithPossibleLineBreaks(final String s, final IQuest quest) {
        String parsed = parseString(s);
        if (parsed.contains("<npc>")) {
            if (quest.getNpcStart() != null) {
                parsed = parsed.replace("<npc>", quest.getNpcStart().getName());
            } else {
                Bukkit.getLogger().warning(quest.getName() + " quest uses <npc> tag but doesn't have an NPC start set");
            }
        }
        return parsed.split("\n");
    }

    public static String[] parseStringWithPossibleLineBreaks(final String s, final NPC npc, int amount) {
        String parsed = parseString(s);
        if (parsed.contains("<npc>")) {
            parsed = parsed.replace("<npc>", npc.getName());
        }
        if (parsed.contains("<amount>")) {
            parsed = parsed.replace("<amount>", String.valueOf(amount));
        }
        return parsed.split("\n");
    }
    
    public static String parseString(final String s, final IQuest quest) {
        String parsed = parseString(s);
        if (parsed.contains("<npc>")) {
            if (quest.getNpcStart() != null) {
                parsed = parsed.replace("<npc>", quest.getNpcStart().getName());
            } else {
                Bukkit.getLogger().warning(quest.getName() + " quest uses <npc> tag but doesn't have an NPC start set");
            }
        }
        return parsed;
    }
    
    public static String parseString(final String s, final IQuest quest, final Player player) {
        String parsed = parseString(s, quest);
        if (Dependencies.placeholder != null && player != null) {
            parsed = PlaceholderAPI.setPlaceholders(player, parsed);
        }
        return parsed;
    }

    public static String parseString(final String s, final UUID npc) {
        String parsed = parseString(s);
        if (Dependencies.citizens != null && parsed.contains("<npc>")) {
            parsed = parsed.replace("<npc>", Dependencies.citizens.getNPCRegistry().getByUniqueId(npc).getName());
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
