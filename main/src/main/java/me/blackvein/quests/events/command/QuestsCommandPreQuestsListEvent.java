package me.blackvein.quests.events.command;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import me.blackvein.quests.Quester;

/**
 * Called when the /quests list command is run by a player
 */
public class QuestsCommandPreQuestsListEvent extends QuestsCommandEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final int page;
    private boolean cancel = false;

    public QuestsCommandPreQuestsListEvent(Quester quester, int page) {
        super(quester);
        this.page = page;
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
    
    public int getPage() {
        return page;
    }
}
