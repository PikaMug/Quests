package me.pikamug.quests.listeners.npc;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.listeners.BukkitNpcListener;
import me.pikamug.quests.dependencies.npc.citizens.BukkitCitizensDependency;
import net.citizensnpcs.api.event.NPCDeathEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.UUID;

public class BukkitCitizensListener extends BukkitNpcListener {
    public BukkitCitizensListener(BukkitQuestsPlugin plugin, BukkitCitizensDependency npcDependency) {
        super(plugin, npcDependency);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onNPCRightClick(final NPCRightClickEvent event) {
        interactNPC(event.getClicker(), event.getNPC() != null ? event.getNPC().getUniqueId() : null, ClickType.RIGHT);
    }

    @EventHandler
    public void onNPCLeftClick(final NPCLeftClickEvent event) {
        interactNPC(event.getClicker(), event.getNPC() != null ? event.getNPC().getUniqueId() : null, ClickType.LEFT);
    }

    @EventHandler
    public void onNPCDeath(final NPCDeathEvent event) {
        if (event.getNPC() == null || event.getNPC().getEntity() == null
                || event.getNPC().getEntity().getLastDamageCause() == null) {
            return;
        }
        if (event.getNPC().getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            final EntityDamageByEntityEvent damageEvent
                    = (EntityDamageByEntityEvent) event.getNPC().getEntity().getLastDamageCause();
            final Entity damager = damageEvent.getDamager();
            final UUID npcId = event.getNPC().getUniqueId();
            preKillNPC(damager, npcId);
        }
    }
}
