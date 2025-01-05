package me.pikamug.quests.dependencies.npc.fancynpcs;

import de.oliver.fancynpcs.api.actions.ActionTrigger;
import de.oliver.fancynpcs.api.events.NpcInteractEvent;
import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.dependencies.npc.NpcListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.UUID;

public class FancyNpcsListener extends NpcListener {
    public FancyNpcsListener(BukkitQuestsPlugin plugin, FancyNpcsDependency npcDependency) {
        super(plugin, npcDependency);
    }

    @EventHandler
    public void onNpcInteract(NpcInteractEvent event) {
        UUID uuid = UUID.fromString(event.getNpc().getData().getId());
        Player player = event.getPlayer();
        ActionTrigger trigger = event.getInteractionType();

        if (trigger == ActionTrigger.LEFT_CLICK) {
            onNpcInteract(player, uuid, ClickType.LEFT);
        } else if (trigger == ActionTrigger.RIGHT_CLICK) {
            onNpcInteract(player, uuid, ClickType.RIGHT);
        }
    }
}
