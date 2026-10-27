package me.pikamug.quests.events.plugin;

import me.pikamug.quests.events.FabricQuestsEvent;

/**
 * Represents a Quests plugin-related event
 */
public abstract class FabricQuestsPluginEvent extends FabricQuestsEvent {

    public FabricQuestsPluginEvent() {
    }

    public FabricQuestsPluginEvent(final boolean async) {
        super(async);
    }
}
