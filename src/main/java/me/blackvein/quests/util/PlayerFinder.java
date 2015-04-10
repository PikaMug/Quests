package me.blackvein.quests.util;

import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

public class PlayerFinder {

    /**
     *
     * @param queryString String that contains part of player name
     * @return if there is a player with exact name (case insensitive), return it
     *          else if there is a player with name which contains part of queryString, return it
     *          else {@code null}
     */
    public static Player findOnlinePlayerByPartialCaseInsensitiveNameMatch(String queryString) {
        Player target_online_player = findOnlinePlayerByExactCaseInsensitiveNameMatch(queryString);
        if (target_online_player != null) {
            return target_online_player;
        }

        for (Player online_player : Bukkit.getOnlinePlayers()) {
            if (online_player.getName().toLowerCase().contains(queryString.toLowerCase())) {
                target_online_player = online_player;
                break;
            }
        }

        return target_online_player;
    }

    public static Player findOnlinePlayerByExactCaseInsensitiveNameMatch(String queryString) {
        Player target_online_player = null;

        for (Player online_player : Bukkit.getOnlinePlayers()) {
            if (online_player.getName().equalsIgnoreCase(queryString)) {
                target_online_player = online_player;
                break;
            }
        }

        return target_online_player;
    }

}
