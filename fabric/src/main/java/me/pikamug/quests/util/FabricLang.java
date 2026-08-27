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
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FabricLang {

    private static FabricQuestsPlugin plugin;
    private static LinkedHashMap<String, String> defaultLang = new LinkedHashMap<>();
    private static LinkedHashMap<String, LinkedHashMap<String, String>> otherLang = new LinkedHashMap<>();
    private static final Pattern hexPattern = Pattern.compile("%#([A-Fa-f0-9]{6})%");

    public static void init(FabricQuestsPlugin pluginInstance) throws IOException {
        plugin = pluginInstance;
        extractLangFiles();
        load(plugin, ((me.pikamug.quests.config.FabricConfigSettings) plugin.getConfigSettings()).getLanguage());
    }

    private static void extractLangFiles() throws IOException {
        final Path langDir = plugin.getPluginDataFolder().toPath().resolve("lang");
        if (!Files.exists(langDir)) {
            Files.createDirectories(langDir);
        }

        // Copy embedded lang files from JAR
        final Set<String> locales = extractLocaleList();
        for (final String locale : locales) {
            final Path localeDir = langDir.resolve(locale);
            if (!Files.exists(localeDir)) {
                Files.createDirectories(localeDir);
            }
            final Path stringsFile = localeDir.resolve("strings.json");
            if (!Files.exists(stringsFile)) {
                final InputStream in = plugin.getPluginResource("lang/" + locale + "/strings.json");
                if (in != null) {
                    Files.copy(in, stringsFile);
                    in.close();
                }
            }
        }
    }

    private static Set<String> extractLocaleList() {
        final Set<String> locales = new LinkedHashSet<>();
        try {
            final InputStream indexStream = plugin.getPluginResource("lang/_index.txt");
            if (indexStream != null) {
                final Scanner scanner = new Scanner(indexStream);
                while (scanner.hasNextLine()) {
                    locales.add(scanner.nextLine().trim());
                }
                scanner.close();
                indexStream.close();
            }
        } catch (final Exception ignored) {}
        if (locales.isEmpty()) {
            locales.add("en-US");
        }
        return locales;
    }

    public static void load(FabricQuestsPlugin pluginInstance, String locale) {
        if (pluginInstance != null) {
            plugin = pluginInstance;
        }
        final Path langFile = plugin.getPluginDataFolder().toPath().resolve("lang").resolve(locale).resolve("strings.json");
        defaultLang.clear();
        if (Files.exists(langFile)) {
            try (Reader reader = Files.newBufferedReader(langFile)) {
                final com.google.gson.JsonObject json = new com.google.gson.Gson().fromJson(reader, com.google.gson.JsonObject.class);
                if (json != null) {
                    for (final String key : json.keySet()) {
                        defaultLang.put(key, json.get(key).getAsString());
                    }
                }
            } catch (final IOException e) {
                FabricQuestsPlugin.LOGGER.error("Failed to load language file for {}", locale, e);
            }
        }
    }

    public static String get(String key) {
        return convertString(defaultLang.getOrDefault(key, key));
    }

    public static String get(ServerPlayer player, String key) {
        if (player == null) {
            return get(key);
        }
        final String raw = defaultLang.getOrDefault(key, key);
        return convertString(raw);
    }

    public static void send(ServerPlayer player, String key) {
        final String msg = get(player, key);
        if (msg != null && !msg.isEmpty()) {
            player.sendSystemMessage(Component.literal(msg));
        }
    }

    public static void clear() {
        defaultLang.clear();
        otherLang.clear();
    }

    public static int size() {
        return defaultLang.size();
    }

    public static LinkedHashMap<String, String> values() {
        return defaultLang;
    }

    public static String getModified(String key, String[] args) {
        String result = get(key);
        for (int i = 0; i < args.length; i++) {
            result = result.replace("%" + (i + 1), args[i]);
        }
        return result;
    }

    public static String getKey(String value) {
        for (final Map.Entry<String, String> entry : defaultLang.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static String convertString(String input) {
        if (input == null) return null;
        String result = input;
        result = result.replace("%reset%", ChatFormatting.RESET.toString());
        result = result.replace("%bold%", ChatFormatting.BOLD.toString());
        result = result.replace("%italic%", ChatFormatting.ITALIC.toString());
        result = result.replace("%underline%", ChatFormatting.UNDERLINE.toString());
        result = result.replace("%strikethrough%", ChatFormatting.STRIKETHROUGH.toString());
        result = result.replace("%obfuscated%", ChatFormatting.OBFUSCATED.toString());
        result = result.replace("%black%", ChatFormatting.BLACK.toString());
        result = result.replace("%dark_blue%", ChatFormatting.DARK_BLUE.toString());
        result = result.replace("%dark_green%", ChatFormatting.DARK_GREEN.toString());
        result = result.replace("%dark_aqua%", ChatFormatting.DARK_AQUA.toString());
        result = result.replace("%dark_red%", ChatFormatting.DARK_RED.toString());
        result = result.replace("%dark_purple%", ChatFormatting.DARK_PURPLE.toString());
        result = result.replace("%gold%", ChatFormatting.GOLD.toString());
        result = result.replace("%gray%", ChatFormatting.GRAY.toString());
        result = result.replace("%dark_gray%", ChatFormatting.DARK_GRAY.toString());
        result = result.replace("%blue%", ChatFormatting.BLUE.toString());
        result = result.replace("%green%", ChatFormatting.GREEN.toString());
        result = result.replace("%aqua%", ChatFormatting.AQUA.toString());
        result = result.replace("%red%", ChatFormatting.RED.toString());
        result = result.replace("%light_purple%", ChatFormatting.LIGHT_PURPLE.toString());
        result = result.replace("%yellow%", ChatFormatting.YELLOW.toString());
        result = result.replace("%white%", ChatFormatting.WHITE.toString());
        // Hex color support: %#RRGGBB%
        final Matcher matcher = hexPattern.matcher(result);
        final StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            final String hex = matcher.group(1);
            matcher.appendReplacement(sb, "\u00A7x\u00A7" + hex.charAt(0) + "\u00A7" + hex.charAt(1)
                    + "\u00A7" + hex.charAt(2) + "\u00A7" + hex.charAt(3)
                    + "\u00A7" + hex.charAt(4) + "\u00A7" + hex.charAt(5));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
