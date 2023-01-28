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

import me.blackvein.quests.QuestsAPI;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lang {

    private static QuestsAPI plugin;
    private static final LinkedHashMap<String, String> defaultLang = new LinkedHashMap<>();
    private static final LinkedHashMap<String, LinkedHashMap<String, String>> otherLang = new LinkedHashMap<>();
    private static final Pattern hexPattern = Pattern.compile("(?i)%#([0-9A-F]{6})%");

    /**
     * @deprecated Use Settings#getLanguage()
     */
    public static String getISO() {
        return plugin.getSettings().getLanguage();
    }

    /**
     * @deprecated Use Settings#setLanguage(String)
     */
    public static void setISO(final String iso) {
        plugin.getSettings().setLanguage(iso);
    }
    
    public static Collection<String> values() {
        return defaultLang.values();
    }
    
    /**
     * Get lang string AND pass Player for use with PlaceholderAPI, if installed
     * 
     * @param player the Player whom will receive the string
     * @param key label as it appears in lang file, such as "journalNoQuests"
     * @return formatted string, plus processing through PlaceholderAPI by clip
     */
    @SuppressWarnings("deprecation")
    public static String get(final Player player, final String key) {
        if (key == null) {
            return null;
        }
        if (player == null) {
            return get(key);
        }
        String locale;
        try {
            locale = player.getLocale();
        } catch (NoSuchMethodError e) {
            locale = player.spigot().getLocale();
        }
        final int separator = locale.indexOf("_");
        if (separator == -1) {
            return defaultLang.containsKey(key) ? LangToken.convertString(player, defaultLang.get(key)) : "NULL";
        }
        final String language = locale.substring(0, separator);
        final String country = locale.substring(separator + 1).toUpperCase();
        locale = language + "-" + country;
        if (plugin.getSettings().canLanguageOverrideClient() || locale.equals(getISO())) {
            return defaultLang.containsKey(key) ? LangToken.convertString(player, defaultLang.get(key)) : "NULL";
        }
        if (!otherLang.containsKey(locale)) {
            try {
                init(plugin, locale);
            } catch (Exception e) {
                return defaultLang.containsKey(key) ? LangToken.convertString(player, defaultLang.get(key)) : "NULL";
            }
        }
        if (otherLang.get(locale).get(key) == null) {
            return defaultLang.get(key);
        }
        return LangToken.convertString(otherLang.get(locale).get(key));
    }

    /**
     * Get lang string AND pass sender for use with PlaceholderAPI, if installed
     *
     * @param commandSender the sender whom will receive the string
     * @param key label as it appears in lang file, such as "journalNoQuests"
     * @return formatted string, plus processing through PlaceholderAPI by clip
     */
    public static String get(final CommandSender commandSender, final String key) {
        if (key == null) {
            return null;
        }
        if (commandSender instanceof Player) {
            return get((Player)commandSender, key);
        }
        return get(key);
    }

    /**
     * Get lang string
     * 
     * @param key label as it appears in lang file, such as "journalNoQuests"
     * @return formatted string
     */
    public static String get(final String key) {
        if (key == null) {
            return null;
        }
        return defaultLang.containsKey(key) ? LangToken.convertString(defaultLang.get(key)) : "NULL";
    }

    /**
     * Get key for lang string
     *
     * @param value The lang string
     * @return key or "NULL" as String
     */
    public static String getKey(final String value) {
        for (final Entry<String, String> entry : defaultLang.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return "NULL";
    }

    /**
     * Get prefixed key for lang value
     *
     * @param value The lang string
     * @param keyPrefix String that the key starts with
     * @return full key or "NULL" as String
     */
    public static String getKeyFromPrefix(final String keyPrefix, final String value) {
        for (final Entry<String, String> entry : defaultLang.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(value) && entry.getKey().toUpperCase().startsWith(keyPrefix)) {
                return entry.getKey();
            }
        }
        return "NULL";
    }

    public static void clear() {
        defaultLang.clear();
    }

    public static int size() {
        return defaultLang.size();
    }

    public static String getModified(final String key, final String[] tokens) {
        String orig = defaultLang.get(key);
        for (int i = 0; i < tokens.length; i++) {
            orig = orig.replace("%" + (i + 1), tokens[i]);
        }
        return orig;
    }

    /**
     * Send message if not null or empty
     *
     * @param player The player to send message to
     * @param message The message to be sent
     */
    public static void send(final Player player, final String message) {
        if (message != null && !ChatColor.stripColor(message).equals("")) {
            player.sendMessage(message);
        }
    }

    public static void init(final QuestsAPI plugin, String iso) throws InvalidConfigurationException, IOException {
        Lang.plugin = plugin;
        final File langFile = new File(plugin.getPluginDataFolder(), File.separator + "lang" + File.separator + iso
                + File.separator + "strings.yml");
        final File langFile_new = new File(plugin.getPluginDataFolder(), File.separator + "lang" + File.separator + iso
                + File.separator + "strings_new.yml");
        boolean exists_new = langFile_new.exists();
        final LinkedHashMap<String, String> allStrings = new LinkedHashMap<>();
        if (!(langFile.exists() && iso.split("-").length > 1)) {
            if (defaultLang.isEmpty()) {
                plugin.getPluginLogger().severe("Failed loading lang files for " + iso
                        + " because they were not found. Using default en-US instead");
                plugin.getPluginLogger()
                        .info("If the plugin has not generated language files, ensure Quests has write permissions");
                plugin.getPluginLogger()
                        .info("For help, visit https://pikamug.gitbook.io/quests/casual/translations");
                plugin.getSettings().setLanguage("en-US");
                if (plugin.getSettings().getConsoleLogging() > 3) {
                    plugin.getPluginLogger().info("CodeSource: " + plugin.getClass().getProtectionDomain().getCodeSource()
                            .toString());
                    plugin.getPluginLogger().info("LocationPath: " + plugin.getClass().getProtectionDomain().getCodeSource()
                            .getLocation().getPath());
                }
            }
            final FileConfiguration config = YamlConfiguration.loadConfiguration(new InputStreamReader(Objects
                    .requireNonNull(plugin.getPluginResource("strings.yml")), StandardCharsets.UTF_8));
            for (final String key : config.getKeys(false)) {
                allStrings.put(key, config.getString(key));
            }
        }
        FileConfiguration config;
        if (langFile.length() > 4) {
            config = YamlConfiguration
                    .loadConfiguration(new InputStreamReader(new FileInputStream(langFile), StandardCharsets.UTF_8));
        } else {
            config = YamlConfiguration.loadConfiguration(new InputStreamReader(Objects
                    .requireNonNull(plugin.getPluginResource("strings.yml")), StandardCharsets.UTF_8));
        }
        FileConfiguration config_new = null;
        if (exists_new) {
            if (langFile_new.length() > 5) {
                config_new = YamlConfiguration.loadConfiguration(new InputStreamReader(
                        new FileInputStream(langFile_new), StandardCharsets.UTF_8));
            } else {
                exists_new = false;
            }
        }
        // Load user's lang file and determine new strings
        for (final String key : config.getKeys(false)) {
            allStrings.put(key, config.getString(key));
            if (exists_new) {
                config_new.set(key, null);
            }
        }
        // Add new strings and notify console
        if (exists_new) {
            for (final String key : config_new.getKeys(false)) {
                final String value = config_new.getString(key);
                if (value != null) {
                    allStrings.put(key, value);
                    plugin.getPluginLogger().warning("There are new language phrases in /lang/" + iso
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
        
        final String cmdAdd = allStrings.get("cmdAdd");
        final String cmdClear = allStrings.get("cmdClear");
        final String cmdCancel = allStrings.get("cmdCancel");
        final String cmdDone = allStrings.get("cmdDone");

        final String missing = "( Missing translation: https://crowdin.com/translate/translate-quests )";
        final String strAdd = allStrings.get("strAdd") != null
                ? allStrings.get("strAdd").replace("<command>", cmdAdd) : missing;
        final String strClear = allStrings.get("strClear") != null
                ? allStrings.get("strClear").replace("<command>", cmdClear) : missing;
        final String strCancel = allStrings.get("strCancel") != null
                ? allStrings.get("strCancel").replace("<command>", cmdCancel) : missing;
        final String strDone = allStrings.get("strDone") != null
                ? allStrings.get("strDone").replace("<command>", cmdDone) : missing;
        final String strSpace = allStrings.get("strSpace");
        final String strSemicolon = allStrings.get("strSemicolon");
        for (final Entry<String, String> entry : allStrings.entrySet()) {
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
            if (strSpace != null && entry.getValue().contains("<space>")) {
                allStrings.put(entry.getKey(), entry.getValue().replace("<space>", strSpace));
            }
            if (strSemicolon != null && entry.getValue().contains("<semicolon>")) {
                allStrings.put(entry.getKey(), entry.getValue().replace("<semicolon>", strSemicolon));
            }
        }
        if (iso.equals(getISO())) {
            defaultLang.clear();
            defaultLang.putAll(allStrings);
        } else {
            otherLang.put(iso, allStrings);
        }
        if (langFile.length() > 4) {
            plugin.getPluginLogger().info("Loaded language " + iso + ". Translations via Crowdin");
        } else {
            plugin.getPluginLogger().info("Failed to load language " + iso + " due to lack of translations");
        }
    }

    private static class LangToken {

        static Map<String, String> tokenMap = new HashMap<>();

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
            if (s == null) {
                return null;
            }
            if (tokenMap.isEmpty()) {
                LangToken.init();
            }
            for (final Entry<String, String> token : tokenMap.entrySet()) {
                s = s.replace(token.getKey(), token.getValue());
                s = s.replace(token.getKey().toUpperCase(), token.getValue());
            }
            final Matcher matcher = hexPattern.matcher(s);
            while (matcher.find()) {
                final StringBuilder hex = new StringBuilder();
                hex.append(ChatColor.COLOR_CHAR + "x");
                final char[] chars = matcher.group(1).toCharArray();
                for (final char aChar : chars) {
                    hex.append(ChatColor.COLOR_CHAR).append(Character.toLowerCase(aChar));
                }
                s = s.replace(matcher.group(), hex.toString());
            }
            return s;
        }
        
        public static String convertString(final Player p, String s) {
            if (s == null) {
                return null;
            }
            if (tokenMap.isEmpty()) {
                LangToken.init();
            }
            s = convertString(s);
            final Plugin placeholderApi = Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI");
            if (placeholderApi != null ) {
                if (placeholderApi.isEnabled()) {
                    s = PlaceholderAPI.setPlaceholders(p, s);
                }
            }
            return s;
        }
    }
}
