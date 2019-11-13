package me.blackvein.quests.tasks;

import org.bukkit.entity.Player;

import me.blackvein.quests.Quests;
import net.citizensnpcs.api.CitizensAPI;

public class PlayerMoveThread implements Runnable {

    final Quests plugin;

    public PlayerMoveThread(Quests quests) {
        plugin = quests;
    }
    
    @Override
    public void run() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (plugin.getDependencies().getCitizens() != null) {
                if (CitizensAPI.getNPCRegistry().isNPC(player)) {
                    return;
                }
            }
            plugin.getPlayerListener().playerMove(player.getUniqueId(), player.getLocation());
        }
    }
}
