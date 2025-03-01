package me.pikamug.quests.listeners.npc;

import io.github.znetworkw.znpcservers.npc.interaction.NPCInteractEvent;
import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.listeners.BukkitNpcListener;
import me.pikamug.quests.dependencies.npc.znpcsplus.legacy.BukkitZnpcsPlusLegacyDependency;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.UUID;

public class BukkitZnpcsPlusLegacyListener extends BukkitNpcListener {
    public BukkitZnpcsPlusLegacyListener(BukkitQuestsPlugin plugin, BukkitZnpcsPlusLegacyDependency npcDependency) {
        super(plugin, npcDependency);
    }

    @EventHandler
    public void onNPCInteract(final NPCInteractEvent event) {
        Player player = event.getPlayer();
        UUID npcUUID = event.getNpc() != null ? event.getNpc().getUUID() : null;

        if (event.isLeftClick()) {
            interactNPC(player, npcUUID, ClickType.LEFT);
        } else if (event.isRightClick()) {
            interactNPC(player, npcUUID, ClickType.RIGHT);
        }
    }
}
