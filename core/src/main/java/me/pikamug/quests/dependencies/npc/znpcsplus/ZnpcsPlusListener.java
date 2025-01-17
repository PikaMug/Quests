package me.pikamug.quests.dependencies.npc.znpcsplus;

import lol.pyr.znpcsplus.api.event.NpcInteractEvent;
import lol.pyr.znpcsplus.api.interaction.InteractionType;
import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.dependencies.npc.NpcListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.UUID;

public class ZnpcsPlusListener extends NpcListener {
    public ZnpcsPlusListener(BukkitQuestsPlugin plugin, ZnpcsPlusDependency npcDependency) {
        super(plugin, npcDependency);
    }

    @EventHandler
    public void onNPCInteract(final NpcInteractEvent event) {
        Player player = event.getPlayer();
        UUID npcUUID = event.getNpc() != null ? event.getNpc().getUuid() : null;

        // Needed because the NpcInteractEvent is fired async
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (event.getClickType().equals(InteractionType.LEFT_CLICK)) {
                onNpcInteract(player, npcUUID, ClickType.LEFT);
            } else if (event.getClickType().equals(InteractionType.RIGHT_CLICK)) {
                onNpcInteract(player, npcUUID, ClickType.RIGHT);
            }
        });
    }
}
