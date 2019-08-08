package me.blackvein.quests.events.editor.quests;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class QuestsEditorOpenCreateMenuEvent extends QuestsEditorEvent implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
    private boolean cancel = false;

    public QuestsEditorOpenCreateMenuEvent(ConversationContext context) {
    	super(context);
		this.context = context;
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
