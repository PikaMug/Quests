package me.blackvein.quests.events.editor.actions;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.event.HandlerList;

public class ActionsEditorPostOpenSelectCreatePromptEvent extends ActionsEditorEvent {
    private static final HandlerList handlers = new HandlerList();

    public ActionsEditorPostOpenSelectCreatePromptEvent(ConversationContext context) {
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
