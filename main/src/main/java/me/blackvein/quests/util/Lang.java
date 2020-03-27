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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import me.blackvein.quests.Quests;
import me.clip.placeholderapi.PlaceholderAPI;

public class Lang {

    private static String iso = "en-US";
    private static final LinkedHashMap<String, String> langMap = new LinkedHashMap<String, String>();
    
    public static String getISO() {
        return iso;
    }
    
    public static void setISO(String iso) {
        Lang.iso = iso;
    }
    
    public static Collection<String> values() {
        return langMap.values();
    }
    
    /**
     * Get lang string AND pass Player for use with PlaceholderAPI, if installed
     * 
     * @param p the Player whom will receive the string
     * @param key label as it appears in lang file, such as "journalNoQuests"
     * @return formatted string, plus processing through PlaceholderAPI by clip
     */
    public static String get(Player p, String key) {
        return langMap.containsKey(key) ? LangToken.convertString(p, langMap.get(key)) : "NULL";
    }

    /**
     * Get lang string
     * 
     * @param key label as it appears in lang file, such as "journalNoQuests"
     * @return formatted string
     */
    public static String get(String key) {
        return langMap.containsKey(key) ? LangToken.convertString(langMap.get(key)) : "NULL";
    }

    /**
     * Get key for lang string
     * @param value The lang string
     * @return key or "NULL" as String
     */
    public static String getKey(String value) {
        for (Entry<String, String> entry : langMap.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return "NULL";
    }

    /**
     * Get prefixed key for lang value
     * @param value The lang string
     * @param keyPrefix String that the key starts with
     * @return full key or "NULL" as String
     */
    public static String getKeyFromPrefix(String keyPrefix, String value) {
        for (Entry<String, String> entry : langMap.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(value) && entry.getKey().toUpperCase().startsWith(keyPrefix)) {
                return entry.getKey();
            }
        }
        return "NULL";
    }

    public static void clear() {
        langMap.clear();
    }

    public static int size() {
        return langMap.size();
    }

    public static String getModified(String key, String[] tokens) {
        String orig = langMap.get(key);
        for (int i = 0; i < tokens.length; i++) {
            orig = orig.replace("%" + (i + 1), tokens[i]);
        }
        return orig;
    }

    public static void loadLang(Quests plugin) throws InvalidConfigurationException, IOException {
        File langFile = new File(plugin.getDataFolder(), File.separator + "lang" + File.separator + iso + File.separator
                + "strings.yml");
        File langFile_new = new File(plugin.getDataFolder(), File.separator + "lang" + File.separator + iso
                + File.separator + "strings_new.yml");
        boolean exists_new = langFile_new.exists();
        LinkedHashMap<String, String> allStrings = new LinkedHashMap<String, String>();
        if (langFile.exists()) {
            FileConfiguration config= YamlConfiguration
                    .loadConfiguration(new InputStreamReader(new FileInputStream(langFile), "UTF-8"));
            FileConfiguration config_new = null;
            if (exists_new) {
                config_new = YamlConfiguration
                        .loadConfiguration(new InputStreamReader(new FileInputStream(langFile_new), "UTF-8"));
            }
            // Load user's lang file and determine new strings
            for (String key : config.getKeys(false)) {
                allStrings.put(key, config.getString(key));
                if (exists_new) {
                    config_new.set(key, null);
                }
            }
            // Add new strings and notify user
            if (exists_new) {
                for (String key : config_new.getKeys(false)) {
                    String value = config_new.getString(key);
                    if (value != null) {
                        allStrings.put(key, value);
                        plugin.getLogger().warning("There are new language phrases in /lang/" + iso
                                + "/strings_new.yml for the current version!"
                                + " You must transfer them to, or regenerate, strings.yml to remove this warning!");
                    }
                }
                config_new.options().header("Below are any new strings for your current version of Quests! " 
                        + "Transfer them to the strings.yml of the"
                        + " same folder to stay up-to-date and suppress console warnings.");
                config_new.options().copyHeader(true);
                config_new.save(langFile_new);
            }
        } else {
            plugin.getLogger().severe("Failed loading lang files for " + iso 
                    + " because they were not found. Using default en-US");
            plugin.getLogger()
                    .info("If the plugin has not generated language files, ensure Quests has write permissions");
            plugin.getLogger()
                    .info("For help, visit https://github.com/FlyingPikachu/Quests/wiki/Casual-%E2%80%90-Translations");
            iso = "en-US";
            FileConfiguration config = YamlConfiguration
                    .loadConfiguration(new InputStreamReader(plugin.getResource("strings.yml"), "UTF-8"));
            for (String key : config.getKeys(false)) {
                allStrings.put(key, config.getString(key));
            }
        }
        
        String cmdAdd = allStrings.get("cmdAdd");
        String cmdClear = allStrings.get("cmdClear");
        String cmdCancel = allStrings.get("cmdCancel");
        String cmdDone = allStrings.get("cmdDone");
        
        String strAdd = allStrings.get("strAdd").replace("<command>", cmdAdd);
        String strClear = allStrings.get("strClear").replace("<command>", cmdClear);
        String strCancel = allStrings.get("strCancel").replace("<command>", cmdCancel);
        String strDone = allStrings.get("strDone").replace("<command>", cmdDone);
        String strSpace = allStrings.get("strSpace");
        String strSemicolon = allStrings.get("strSemicolon");
        for (Entry<String, String> entry : allStrings.entrySet()) {
            if (entry.getValue().contains("<add>")) {
                allStrings.put(entry.getKey(), entry.getValue().replace("<add>", strAdd));
            }
            if (entry.getValue().contains("<clear>")) {
                allStrings.put(entry.getKey(), entry.getValue().replace("<clear>", strClear));
            }
            if (entry.getValue().contains("<cancel>")) {
                allStrings.put(entry.getKey(), entry.getValue().replace("<cancel>", strCancel));
            }
            if (entry.getValue().contains("<done>")) {
                allStrings.put(entry.getKey(), entry.getValue().replace("<done>", strDone));
            } 
            if (entry.getValue().contains("<space>")) {
                allStrings.put(entry.getKey(), entry.getValue().replace("<space>", strSpace));
            }
            if (entry.getValue().contains("<semicolon>")) {
                allStrings.put(entry.getKey(), entry.getValue().replace("<semicolon>", strSemicolon));
            }
        }
        langMap.putAll(allStrings);
        plugin.getLogger().info("Loaded language " + iso + ". Translations via Crowdin");
    }

    private static class LangToken {

        static Map<String, String> tokenMap = new HashMap<String, String>();

        public static void init() {
            tokenMap.put("%br%", "\n");
            tokenMap.put("%tab%", "\t");
            tokenMap.put("%rtr%", "\r");
            tokenMap.put("%bold%", ChatColor.BOLD.toString());
            tokenMap.put("%italic%", ChatColor.ITALIC.toString());
            tokenMap.put("%underline%", ChatColor.UNDERLINE.toString());
            tokenMap.put("%strikethrough%", ChatColor.STRIKETHROUGH.toString());
            tokenMap.put("%magic%", ChatColor.MAGIC.toString());
            tokenMap.put("%reset%", ChatColor.RESET.toString());
            tokenMap.put("%white%", ChatColor.WHITE.toString());
            tokenMap.put("%black%", ChatColor.BLACK.toString());
            tokenMap.put("%aqua%", ChatColor.AQUA.toString());
            tokenMap.put("%darkaqua%", ChatColor.DARK_AQUA.toString());
            tokenMap.put("%blue%", ChatColor.BLUE.toString());
            tokenMap.put("%darkblue%", ChatColor.DARK_BLUE.toString());
            tokenMap.put("%gold%", ChatColor.GOLD.toString());
            tokenMap.put("%gray%", ChatColor.GRAY.toString());
            tokenMap.put("%darkgray%", ChatColor.DARK_GRAY.toString());
            tokenMap.put("%pink%", ChatColor.LIGHT_PURPLE.toString());
            tokenMap.put("%purple%", ChatColor.DARK_PURPLE.toString());
            tokenMap.put("%green%", ChatColor.GREEN.toString());
            tokenMap.put("%darkgreen%", ChatColor.DARK_GREEN.toString());
            tokenMap.put("%red%", ChatColor.RED.toString());
            tokenMap.put("%darkred%", ChatColor.DARK_RED.toString());
            tokenMap.put("%yellow%", ChatColor.YELLOW.toString());
        }

        public static String convertString(String s) {
            if (tokenMap.isEmpty()) {
                LangToken.init();
            }
            for (String token : tokenMap.keySet()) {
                s = s.replace(token, tokenMap.get(token));
                s = s.replace(token.toUpperCase(), tokenMap.get(token));
            }
            return s;
        }
        
        public static String convertString(Player p, String s) {
            if (tokenMap.isEmpty()) {
                LangToken.init();
            }
            for (String token : tokenMap.keySet()) {
                s = s.replace(token, tokenMap.get(token));
                s = s.replace(token.toUpperCase(), tokenMap.get(token));
                if (Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null ) {
                    if (!Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI").isEnabled()) {
                        s = PlaceholderAPI.setPlaceholders(p, s);
                    }
                }
            }
            return s;
        }
    }
}
