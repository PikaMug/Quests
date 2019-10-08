package me.blackvein.quests.events;

import org.bukkit.event.Event;

public abstract class QuestsEvent extends Event {
    public QuestsEvent() {
    }
    
    public QuestsEvent(boolean async) {
        super(async);
    }
}
