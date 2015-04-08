package me.blackvein.quests.util;

import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

public class PlayerFinder {

    public static Player findOnlinePlayerByPartialCaseInsensitiveNameMatch(String queryString) {
        Player target_online_player = null;

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
