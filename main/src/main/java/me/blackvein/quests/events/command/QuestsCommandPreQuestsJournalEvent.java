package me.blackvein.quests.events.command;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import me.blackvein.quests.Quester;

/**
 * Called when the /quests journal command is run by a player
 */
public class QuestsCommandPreQuestsJournalEvent extends QuestsCommandEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancel = false;

    public QuestsCommandPreQuestsJournalEvent(Quester quester) {
        super(quester);
    }
    
    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
