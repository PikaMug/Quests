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

package me.blackvein.quests.commands;

import me.blackvein.quests.util.Lang;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class QuestsSubCommand {

    public abstract String getName();

    public abstract String getNameI18N();

    public abstract String getDescription();

    public abstract String getPermission();

    public abstract String getSyntax();

    public abstract void execute(CommandSender commandSender, String[] args);

    public static boolean assertNonPlayer(CommandSender commandSender) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(ChatColor.YELLOW + Lang.get("consoleError"));
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
