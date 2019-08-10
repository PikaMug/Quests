package me.blackvein.quests.events.editor.quests;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.event.HandlerList;

public class QuestsEditorPostOpenCreateMenuEvent extends QuestsEditorEvent {
	private static final HandlerList handlers = new HandlerList();

    public QuestsEditorPostOpenCreateMenuEvent(ConversationContext context) {
    	super(context);
		this.context = context;
    }

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
        return handlers;
    }
}
