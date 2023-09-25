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

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;

import java.util.LinkedList;

public class BukkitMiscUtil {
    
    /**
     * Gets a human-readable date and time from milliseconds
     * 
     * @param milliseconds Total amount of time to convert
     * @return Converted time in text
     */
    public static String getTime(final long milliseconds) {
        String message = "";
        final long days = milliseconds / 86400000;
        final long hours = (milliseconds % 86400000) / 3600000;
        final long minutes = ((milliseconds % 86400000) % 3600000) / 60000;
        final long seconds = (((milliseconds % 86400000) % 3600000) % 60000) / 1000;
        final long milliSeconds2 = (((milliseconds % 86400000) % 3600000) % 60000) % 1000;
        if (days > 0L) {
            if (days == 1L) {
                message += " 1 " + BukkitLang.get("timeDay") + ",";
            } else {
                message += " " + days + " " + BukkitLang.get("timeDays") + ",";
            }
        }
        if (hours > 0L) {
            if (hours == 1L) {
                message += " 1 " + BukkitLang.get("timeHour") + ",";
            } else {
                message += " " + hours + " " + BukkitLang.get("timeHours") + ",";
            }
        }
        if (minutes > 0L) {
            if (minutes == 1L) {
                message += " 1 " + BukkitLang.get("timeMinute") + ",";
            } else {
                message += " " + minutes + " " + BukkitLang.get("timeMinutes") + ",";
            }
        }
        if (seconds > 0L) {
            if (seconds == 1L) {
                message += " 1 " + BukkitLang.get("timeSecond") + ",";
            } else {
                message += " " + seconds + " " + BukkitLang.get("timeSeconds") + ",";
            }
        } else {
            if (milliSeconds2 > 0L) {
                if (milliSeconds2 == 1L) {
                    message += " 1 " + BukkitLang.get("timeMillisecond") + ",";
                } else {
                    message += " " + milliSeconds2 + " " + BukkitLang.get("timeMilliseconds") + ",";
                }
            }
        }
        if (message.length() > 0) {
            message = message.substring(1, message.length() - 1);
        } else {
            message = "-1 " + BukkitLang.get("timeSeconds");
        }
        return message;
    }
    
    /**
     * Capitalize first letter of text and set remainder to lowercase
     * 
     * @param input To convert
     * @return Converted text
     */
    public static String getCapitalized(final String input) {
        if (input.isEmpty()) {
            return input;
        }
        final String firstLetter = input.substring(0, 1);
        final String remainder = input.substring(1);
        return firstLetter.toUpperCase() + remainder.toLowerCase();
    }
    
    /**
     * Gets player-friendly name from type. 'ENDER_DRAGON' becomes 'Ender Dragon'
     * 
     * @param type any mob type, ideally
     * @return cleaned-up string
     */
    public static String getPrettyMobName(final EntityType type) {
        final String baseString = type.toString();
        final String[] substrings = baseString.split("_");
        String prettyString = "";
        int size = 1;
        for (final String s : substrings) {
            prettyString = prettyString.concat(getCapitalized(s));
            if (size < substrings.length) {
                prettyString = prettyString.concat(" ");
            }
            size++;
        }
        if (type.equals((EntityType.OCELOT))) {
            prettyString = "Ocelot";
        }
        return prettyString;
    }
    
    /**
     * Convert text from snake_case to UpperCamelCase
     * 
     * @param input To convert
     * @return Converted text
     */
    public static String snakeCaseToUpperCamelCase(final String input) {
        String name = input.toLowerCase();
        name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        for (int i = 0; i < input.chars().filter(num -> num == '_').count(); i++) {
            final int index = name.indexOf('_');
            if (index != -1) {
                name = name.substring(0, (index + 1)) + Character.toUpperCase(name.charAt(index + 1)) 
                        + name.substring(index + 2);
                name = name.replaceFirst("_", "");
            }
        }
        return name;
    }

    /**
     * Gets EntityType from name
     * 
     * @param properName Name to get type from
     * @return EntityType or null if invalid
     */
    public static EntityType getProperMobType(String properName) {
        properName = properName.replace("_", "").replace(" ", "").toUpperCase();
        for (final EntityType et : EntityType.values()) {
            if (et.name().replace("_", "").equalsIgnoreCase(properName)) {
                return et;
            }
        }
        return null;
    }
    
    /**
     * Gets Biome from name
     * 
     * @param properName Name to get biome from
     * @return Biome or null if invalid
     */
    public static Biome getProperBiome(String properName) {
        properName = properName.replace("_", "").replace(" ", "").toUpperCase();
        for (final Biome b : Biome.values()) {
            if (b.name().replace("_", "").equalsIgnoreCase(properName)) {
                return b;
            }
        }
        return null;
    }
    
    /**
     * Gets player-friendly name from type. 'LIGHT_BLUE' becomes 'Light Blue'
     * 
     * @param color any dye type, ideally
     * @return cleaned-up string, or 'White' if null
     */
    public static String getPrettyDyeColorName(final DyeColor color) {
        if (color == null) {
            return "White";
        }
        if (!BukkitLang.get("COLOR_" + color.name()).equals("NULL")) {
            // Legacy
            return BukkitLang.get("COLOR_" + color.name());
        } else {
            final String baseString = color.toString();
            final String[] substrings = baseString.split("_");
            String prettyString = "";
            int size = 1;
            for (final String s : substrings) {
                prettyString = prettyString.concat(getCapitalized(s));
                if (size < substrings.length) {
                    prettyString = prettyString.concat(" ");
                }
                size++;
            }
            return prettyString;
        }
    }
    
    /**
     * Gets DyeColor from name
     * 
     * @param properName Name to get type from
     * @return DyeColor or null if invalid
     */
    public static DyeColor getProperDyeColor(String properName) {
        properName = properName.replace("_", "").replace(" ", "").toUpperCase();
        for (final DyeColor dc : DyeColor.values()) {
            if (dc.name().replace("_", "").equalsIgnoreCase(properName)) {
                return dc;
            }
        }
        return null;
    }
    
    /**
     * Gets effect from name
     * 
     * @param properName Name to get effect from
     * @return Effect or null if invalid
     */
    public static Effect getProperEffect(String properName) {
        properName = properName.replace("_", "").replace(" ", "").toUpperCase();
        for (final Effect eff : Effect.values()) {
            if (eff.name().replace("_", "").equalsIgnoreCase(properName)) {
                return eff;
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
                                               final ChatColor lineColor) {
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
        if (!line.toString().equals(""))
            if (lineColor != null) {
                toReturn.add(lineColor + line.toString());
            } else {
                toReturn.add(line.toString());
            }
        return toReturn;
    }
    
    /**
     * Adds a single space in front of all capital letters
     * 
     * Unused internally. Left for external use
     * 
     * @param input string to process
     * @return processed string
     */
    public static String capitalsToSpaces(String input) {
        int max = input.length();
        for (int i = 1; i < max; i++) {
            if (Character.isUpperCase(input.charAt(i))) {
                input = input.substring(0, i) + " " + input.substring(i);
                i++;
                max++;
            }
        }
        return input;
    }

    /**
     * Capitalize character after space
     * 
     * @param input string to process
     * @return processed string
     */
    public static String spaceToCapital(String input) {
        final int index = input.indexOf(' ');
        if (index == -1) {
            return null;
        }
        input = input.substring(0, (index + 1)) + Character.toUpperCase(input.charAt(index + 1)) 
                + input.substring(index + 2);
        input = input.replaceFirst(" ", "");
        return input;
    }
}
