/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.commands;

import me.pikamug.quests.util.BukkitLang;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class BukkitQuestsSubCommand {

    public abstract String getName();

    public abstract String getNameI18N();

    public abstract String getDescription();

    public abstract String getPermission();

    public abstract String getSyntax();

    public abstract int getMaxArguments();

    public abstract void execute(CommandSender commandSender, String[] args);

    public List<String> tabComplete(CommandSender commandSender, String[] args) {
        return Collections.emptyList();
    }

    public static boolean assertNonPlayer(CommandSender commandSender) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(ChatColor.YELLOW + BukkitLang.get("consoleError"));
            return true;
        }
        return false;
    }

    /**
     * Get an online Player by name
     *
     * @param name Name of the player
     * @return Player or null if not found
     */
    public static Player getOnlinePlayer(final String name) {
        if (name == null) {
            return null;
        }
        for (final Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (p.getName().equalsIgnoreCase(name)) {
                return p;
            }
        }
        for (final Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (p.getName().toLowerCase().startsWith(name)) {
                return p;
            }
        }
        for (final Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (p.getName().toLowerCase().contains(name)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Get a potentially offline Player by name
     *
     * @param name Name of the player
     * @return Player or null if not found
     */
    public static OfflinePlayer getOfflinePlayer(final String name) {
        if (name == null) {
            return null;
        }
        final Player player = getOnlinePlayer(name);
        if (player != null) {
            return player;
        }
        for (final OfflinePlayer p : Bukkit.getServer().getOfflinePlayers()) {
            if (p != null && p.getName() != null && p.getName().equalsIgnoreCase(name)) {
                return p;
            }
        }
        for (final OfflinePlayer p : Bukkit.getServer().getOfflinePlayers()) {
            if (p != null && p.getName() != null && p.getName().toLowerCase().startsWith(name)) {
                return p;
            }
        }
        for (final OfflinePlayer p : Bukkit.getServer().getOfflinePlayers()) {
            if (p != null && p.getName() != null && p.getName().toLowerCase().contains(name)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Used to get quest names that contain spaces from command input
     *
     * @param args an array of Strings
     * @param startingIndex the index to start combining at
     * @param endingIndex the index to stop combining at
     * @param delimiter the character for which the array was split
     * @return a String or null
     */
    public static String concatArgArray(final String[] args, final int startingIndex, final int endingIndex,
                                         final char delimiter) {
        StringBuilder s = new StringBuilder();
        for (int i = startingIndex; i <= endingIndex; i++) {
            s.append(args[i]).append(delimiter);
        }
        s = new StringBuilder(s.substring(0, s.length()));
        return s.toString().trim().equals("") ? null : s.toString().trim();
    }

    public static Map<String, Integer> sort(final Map<String, Integer> unsortedMap) {
        final List<Map.Entry<String, Integer>> list = new LinkedList<>(unsortedMap.entrySet());
        list.sort((o1, o2) -> {
            final int i = o1.getValue();
            final int i2 = o2.getValue();
            return Integer.compare(i2, i);
        });
        final Map<String, Integer> sortedMap = new LinkedHashMap<>();
        for (final Map.Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }
}
