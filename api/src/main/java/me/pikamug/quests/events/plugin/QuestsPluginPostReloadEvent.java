package me.pikamug.quests.events.plugin;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QuestsPluginPostReloadEvent extends QuestsPluginEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final boolean success;
    private final @Nullable Exception exception;

    public QuestsPluginPostReloadEvent(final boolean success, final @Nullable Exception exception) {
        this.success = success;
        this.exception = exception;
    }

    /**
     * Returns whether the plugin was successfully reloaded
     *
     * @return true if the reload was successful
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the exception that was thrown while reloading the plugin
     *
     * @return the exception, or null if the plugin reload was successful
     */
    public @Nullable Exception getException() {
        return exception;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
