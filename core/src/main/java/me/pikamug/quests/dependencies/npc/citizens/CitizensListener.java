package me.pikamug.quests.dependencies.npc.citizens;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.dependencies.npc.NpcListener;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class CitizensListener extends NpcListener {
    public CitizensListener(BukkitQuestsPlugin plugin, CitizensDependency npcDependency) {
        super(plugin, npcDependency);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onNPCRightClick(final NPCRightClickEvent event) {
        onNpcInteract(event.getClicker(), event.getNPC() != null ? event.getNPC().getUniqueId() : null, ClickType.RIGHT);
    }

    @EventHandler
    public void onNPCLeftClick(final NPCLeftClickEvent event) {
        onNpcInteract(event.getClicker(), event.getNPC() != null ? event.getNPC().getUniqueId() : null, ClickType.LEFT);
    }
}
