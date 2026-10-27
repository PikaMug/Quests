package me.pikamug.quests.events.plugin;

import net.fabricmc.fabric.api.event.Event;
import org.jetbrains.annotations.Nullable;

public class FabricQuestsPluginPostReloadEvent extends FabricQuestsPluginEvent {
    public static final Event<Listener<FabricQuestsPluginPostReloadEvent>> EVENT = createEvent();

    private final boolean success;
    private final @Nullable Exception exception;

    public FabricQuestsPluginPostReloadEvent(final boolean success, final @Nullable Exception exception) {
        super();
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
}
