package me.pikamug.quests.events.plugin;

import me.pikamug.quests.events.QuestsEvent;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a Quests plugin-related event
 */
public abstract class BukkitQuestsPluginEvent extends QuestsEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    public BukkitQuestsPluginEvent() {
    }

    public BukkitQuestsPluginEvent(final boolean async) {
        super(async);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
