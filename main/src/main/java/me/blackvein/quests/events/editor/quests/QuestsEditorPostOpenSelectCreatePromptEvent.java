package me.blackvein.quests.events.editor.quests;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.event.HandlerList;

public class QuestsEditorPostOpenSelectCreatePromptEvent extends QuestsEditorEvent {
    private static final HandlerList handlers = new HandlerList();

    public QuestsEditorPostOpenSelectCreatePromptEvent(ConversationContext context) {
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
