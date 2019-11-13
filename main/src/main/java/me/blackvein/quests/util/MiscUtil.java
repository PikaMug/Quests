/*******************************************************************************************************
 * Continued by PikaMug (formerly HappyPikachu) with permission from _Blackvein_. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

package me.blackvein.quests.util;

import java.util.LinkedList;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.entity.EntityType;

public class MiscUtil {
    
    public static String getTime(long milliseconds) {
        String message = "";
        long days = milliseconds / 86400000;
        long hours = (milliseconds % 86400000) / 3600000;
        long minutes = ((milliseconds % 86400000) % 3600000) / 60000;
        long seconds = (((milliseconds % 86400000) % 3600000) % 60000) / 1000;
        long milliSeconds2 = (((milliseconds % 86400000) % 3600000) % 60000) % 1000;
        if (days > 0L) {
            if (days == 1L) {
                message += " 1 " + Lang.get("timeDay") + ",";
            } else {
                message += " " + days + " " + Lang.get("timeDays") + ",";
            }
        }
        if (hours > 0L) {
            if (hours == 1L) {
                message += " 1 " + Lang.get("timeHour") + ",";
            } else {
                message += " " + hours + " " + Lang.get("timeHours") + ",";
            }
        }
        if (minutes > 0L) {
            if (minutes == 1L) {
                message += " 1 " + Lang.get("timeMinute") + ",";
            } else {
                message += " " + minutes + " " + Lang.get("timeMinutes") + ",";
            }
        }
        if (seconds > 0L) {
            if (seconds == 1L) {
                message += " 1 " + Lang.get("timeSecond") + ",";
            } else {
                message += " " + seconds + " " + Lang.get("timeSeconds") + ",";
            }
        } else {
            if (milliSeconds2 > 0L) {
                if (milliSeconds2 == 1L) {
                    message += " 1 " + Lang.get("timeMillisecond") + ",";
                } else {
                    message += " " + milliSeconds2 + " " + Lang.get("timeMilliseconds") + ",";
                }
            }
        }
        if (message.length() > 0) {
            message = message.substring(1, message.length() - 1);
        }
        return message;
    }
    
    /**
     * Capitalize first letter of text and set remainder to lowercase
     * 
     * @param input
     * @return
     */
    public static String getCapitalized(String input) {
        if (input.isEmpty()) {
            return input;
        }
        String firstLetter = input.substring(0, 1);
        String remainder = input.substring(1);
        String capitalized = firstLetter.toUpperCase() + remainder.toLowerCase();
        return capitalized;
    }
    
    /**
     * Gets player-friendly name from type. 'ENDER_DRAGON' becomes 'Ender Dragon'
     * 
     * @param type any mob type, ideally
     * @return cleaned-up string
     */
    public static String getPrettyMobName(EntityType type) {
        String baseString = type.toString();
        String[] substrings = baseString.split("_");
        String prettyString = "";
        int size = 1;
        for (String s : substrings) {
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
     * @param type To convert
     * @return Converted text
     */
    public static String snakeCaseToUpperCamelCase(String input) {
        String name = input.toLowerCase();
        name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        for (int i = 0; i < input.chars().filter(num -> num == '_').count(); i++) {
            int index = name.indexOf('_');
            if (index != -1) {
                name = name.substring(0, (index + 1)) + Character.toUpperCase(name.charAt(index + 1)) 
                        + name.substring(index + 2);
                name = name.replaceFirst("_", "");
            }
        }
        return name;
    }
    
    /**
     * Convert EntityType name from snake_case to UpperCamelCase
     * 
     * @deprecated Use {@link #snakeCaseToUpperCamelCase(String)}
     * @param type To convert
     * @return Converted text
     */
    public static String getProperMobName(EntityType type) {
        return snakeCaseToUpperCamelCase(type.name());
    }

    /**
     * Gets living EntityType from name
     * 
     * @param properName Name to get type from
     * @return EntityType or null if invalid
     */
    public static EntityType getProperMobType(String properName) {
        properName = properName.replaceAll("_", "").replaceAll(" ", "").toUpperCase();
        for (EntityType et : EntityType.values()) {
            if (et.isAlive() && et.name().replaceAll("_", "").equalsIgnoreCase(properName)) {
                return et;
            }
        }
        return null;
    }
    
    public static String getPrettyDyeColorName(DyeColor color) {
        return Lang.get("COLOR_" + color.name());
    }

    public static DyeColor getDyeColor(String s) {
        String col = Lang.getKey(getCapitalized(s));
        col = col.replace("COLOR_", "");
        DyeColor color = null;
        try {
            color = DyeColor.valueOf(col);
        } catch (IllegalArgumentException e) {
            // Do nothing
        }
        return color != null ? color : getDyeColorLegacy(s);
    }

    public static DyeColor getDyeColorLegacy(String s) {
        if (s.equalsIgnoreCase("Black") || s.equalsIgnoreCase(Lang.get("COLOR_BLACK"))) {
            return DyeColor.BLACK;
        } else if (s.equalsIgnoreCase("Blue") || s.equalsIgnoreCase(Lang.get("COLOR_BLUE"))) {
            return DyeColor.BLUE;
        } else if (s.equalsIgnoreCase("Brown") || s.equalsIgnoreCase(Lang.get("COLOR_BROWN"))) {
            return DyeColor.BROWN;
        } else if (s.equalsIgnoreCase("Cyan") || s.equalsIgnoreCase(Lang.get("COLOR_CYAN"))) {
            return DyeColor.CYAN;
        } else if (s.equalsIgnoreCase("Gray") || s.equalsIgnoreCase(Lang.get("COLOR_GRAY"))) {
            return DyeColor.GRAY;
        } else if (s.equalsIgnoreCase("Green") || s.equalsIgnoreCase(Lang.get("COLOR_GREEN"))) {
            return DyeColor.GREEN;
        } else if (s.equalsIgnoreCase("LightBlue") || s.equalsIgnoreCase(Lang.get("COLOR_LIGHT_BLUE"))) {
            return DyeColor.LIGHT_BLUE;
        } else if (s.equalsIgnoreCase("Lime") || s.equalsIgnoreCase(Lang.get("COLOR_LIME"))) {
            return DyeColor.LIME;
        } else if (s.equalsIgnoreCase("Magenta") || s.equalsIgnoreCase(Lang.get("COLOR_MAGENTA"))) {
            return DyeColor.MAGENTA;
        } else if (s.equalsIgnoreCase("Orange") || s.equalsIgnoreCase(Lang.get("COLOR_ORAGE"))) {
            return DyeColor.ORANGE;
        } else if (s.equalsIgnoreCase("Pink") || s.equalsIgnoreCase(Lang.get("COLOR_PINK"))) {
            return DyeColor.PINK;
        } else if (s.equalsIgnoreCase("Purple") || s.equalsIgnoreCase(Lang.get("COLOR_PURPLE"))) {
            return DyeColor.PURPLE;
        } else if (s.equalsIgnoreCase("Red") || s.equalsIgnoreCase(Lang.get("COLOR_RED"))) {
            return DyeColor.RED;
        // 1.13 changed DyeColor.SILVER -> DyeColor.LIGHT_GRAY
        } else if (s.equalsIgnoreCase("Silver") || s.equalsIgnoreCase("LightGray") 
                || s.equalsIgnoreCase(Lang.get("COLOR_SILVER"))) {
            return DyeColor.getByColor(Color.SILVER);
        } else if (s.equalsIgnoreCase("White") || s.equalsIgnoreCase(Lang.get("COLOR_WHITE"))) {
            return DyeColor.WHITE;
        } else if (s.equalsIgnoreCase("Yellow") || s.equalsIgnoreCase(Lang.get("COLOR_YELLOW"))) {
            return DyeColor.YELLOW;
        } else {
            return null;
        }
    }

    public static String getDyeString(DyeColor dc) {
        return Lang.get("COLOR_" + dc.name());
    }
    
    /**
     * @deprecated Will be removed in a future version of Quests
     */
    public static String concatArgArray(String[] args, int startingIndex, int endingIndex, char delimiter) {
        String s = "";
        for (int i = startingIndex; i <= endingIndex; i++) {
            s += args[i] + delimiter;
        }
        s = s.substring(0, s.length());
        return s.trim().equals("") ? null : s.trim();
    }

    public static LinkedList<String> makeLines(String s, String wordDelimiter, int lineLength, ChatColor lineColor) {
        LinkedList<String> toReturn = new LinkedList<String>();
        String[] split = s.split(wordDelimiter);
        String line = "";
        int currentLength = 0;
        for (String piece : split) {
            if ((currentLength + piece.length()) > (lineLength + 1)) {
                if (lineColor != null) {
                    toReturn.add(lineColor + line.replaceAll("^" + wordDelimiter, ""));
                } else {
                    toReturn.add(line.replaceAll("^" + wordDelimiter, ""));
                }
                line = piece + wordDelimiter;
                currentLength = piece.length() + 1;
            } else {
                line += piece + wordDelimiter;
                currentLength += piece.length() + 1;
            }
        }
        if (line.equals("") == false)
            if (lineColor != null) {
                toReturn.add(lineColor + line);
            } else {
                toReturn.add(line);
            }
        return toReturn;
    }
    
    /**
     * Adds a single space in front of all capital letters
     * 
     * @param s string to process
     * @return processed string
     */
    public static String capitalsToSpaces(String s) {
        int max = s.length();
        for (int i = 1; i < max; i++) {
            if (Character.isUpperCase(s.charAt(i))) {
                s = s.substring(0, i) + " " + s.substring(i);
                i++;
                max++;
            }
        }
        return s;
    }

    /**
     * Capitalize character after space
     * 
     * @param s string to process
     * @return processed string
     */
    public static String spaceToCapital(String s) {
        int index = s.indexOf(' ');
        if (index == -1) {
            return null;
        }
        s = s.substring(0, (index + 1)) + Character.toUpperCase(s.charAt(index + 1)) + s.substring(index + 2);
        s = s.replaceFirst(" ", "");
        return s;
    }
}
