package me.pikamug.quests.dependencies.npc.znpcsplus.legacy;

import io.github.znetworkw.znpcservers.npc.interaction.NPCInteractEvent;
import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.dependencies.npc.NpcListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.UUID;

public class LegacyZnpcsPlusListener extends NpcListener {
    public LegacyZnpcsPlusListener(BukkitQuestsPlugin plugin, LegacyZnpcsPlusDependency npcDependency) {
        super(plugin, npcDependency);
    }

    @EventHandler
    public void onNPCInteract(final NPCInteractEvent event) {
        Player player = event.getPlayer();
        UUID npcUUID = event.getNpc() != null ? event.getNpc().getUUID() : null;

        if (event.isLeftClick()) {
            onNpcInteract(player, npcUUID, ClickType.LEFT);
        } else if (event.isRightClick()) {
            onNpcInteract(player, npcUUID, ClickType.RIGHT);
        }
    }
}
