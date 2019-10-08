package me.blackvein.quests.events.command;

import me.blackvein.quests.Quester;
import me.blackvein.quests.events.QuestsEvent;

/**
 * Represents a Quests command-related event
 */
public abstract class QuestsCommandEvent extends QuestsEvent {
    protected Quester quester;
    
    public QuestsCommandEvent(final Quester quester) {
        this.quester = quester;
    }
    
    public QuestsCommandEvent(final Quester quester, boolean async) {
        super(async);
        this.quester = quester;
    }
    
    /**
     * Returns the quester involved in this event
     * 
     * @return Quester which is involved in this event
     */
    public final Quester getQuester() {
        return quester;
    }
}
