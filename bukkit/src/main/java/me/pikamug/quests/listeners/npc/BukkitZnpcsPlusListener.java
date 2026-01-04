package me.pikamug.quests.listeners.npc;

import lol.pyr.znpcsplus.api.event.NpcInteractEvent;
import lol.pyr.znpcsplus.api.interaction.InteractionType;
import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.listeners.BukkitNpcListener;
import me.pikamug.quests.dependencies.npc.znpcsplus.BukkitZnpcsPlusDependency;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.UUID;

public class BukkitZnpcsPlusListener extends BukkitNpcListener {
    public BukkitZnpcsPlusListener(BukkitQuestsPlugin plugin, BukkitZnpcsPlusDependency npcDependency) {
        super(plugin, npcDependency);
    }

    @EventHandler
    public void onNPCInteract(final NpcInteractEvent event) {
        Player player = event.getPlayer();
        UUID npcUUID = event.getNpc() != null ? event.getNpc().getUuid() : null;

        // Needed because the NpcInteractEvent is fired async
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (event.getClickType().equals(InteractionType.LEFT_CLICK)) {
                interactNPC(player, npcUUID, ClickType.LEFT);
            } else if (event.getClickType().equals(InteractionType.RIGHT_CLICK)) {
                interactNPC(player, npcUUID, ClickType.RIGHT);
            }
        });
    }
}
