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
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.entity.EntityType;

public class MiscUtil {
    
    /**
     * Gets a human-readable date and time from milliseconds
     * 
     * @param milliseconds Total amount of time to convert
     * @return Converted time in text
     */
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
     * @param input To convert
     * @return Converted text
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
        properName = properName.replace("_", "").replace(" ", "").toUpperCase();
        for (EntityType et : EntityType.values()) {
            if (et.isAlive() && et.name().replace("_", "").equalsIgnoreCase(properName)) {
                return et;
            }
        }
        return null;
    }
    
    /**
     * Gets player-friendly name from type. 'LIGHT_BLUE' becomes 'Light Blue'
     * 
     * @param type any dye type, ideally
     * @return cleaned-up string
     */
    public static String getPrettyDyeColorName(DyeColor color) {
        if (!Lang.get("COLOR_" + color.name()).equals("NULL")) {
            // Legacy
            return Lang.get("COLOR_" + color.name());
        } else {
            String baseString = color.toString();
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
        for (DyeColor dc : DyeColor.values()) {
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
        for (Effect eff : Effect.values()) {
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
    public static LinkedList<String> makeLines(String input, String wordDelimiter, int lineLength
            , ChatColor lineColor) {
        LinkedList<String> toReturn = new LinkedList<String>();
        String[] split = input.split(wordDelimiter);
        String line = "";
        int currentLength = 0;
        for (String piece : split) {
            if ((currentLength + piece.length()) > (lineLength + 1)) {
                // TODO - determine whether replaceAll and carots (^) are necessary here
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
     * Unused internally. Left for external use
     * 
     * @param s string to process
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
     * @param s string to process
     * @return processed string
     */
    public static String spaceToCapital(String input) {
        int index = input.indexOf(' ');
        if (index == -1) {
            return null;
        }
        input = input.substring(0, (index + 1)) + Character.toUpperCase(input.charAt(index + 1)) 
                + input.substring(index + 2);
        input = input.replaceFirst(" ", "");
        return input;
    }
}
