package me.blackvein.quests.util;

import com.evilmidget38.UUIDFetcher;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

import java.util.UUID;

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

    /**
     *
     * @param queryString player name
     * @return according to Bukkit.getOfflinePlayer, always an object (never null)
     */
    public static OfflinePlayer findOfflinePlayerByExactCaseInsensitiveNameMatch(String queryString) {
        UUID target_offline_player_uuid;

        try {
            target_offline_player_uuid = UUIDFetcher.getUUIDOf(queryString);
        }
        catch (Exception ex) {
            return null;
        }
        if (target_offline_player_uuid == null) {
            return null;
        }

        return Bukkit.getOfflinePlayer(target_offline_player_uuid);
    }

}
