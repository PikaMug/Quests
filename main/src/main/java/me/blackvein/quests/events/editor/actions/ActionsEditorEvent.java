package me.blackvein.quests.events.editor.actions;

import org.bukkit.conversations.ConversationContext;

import me.blackvein.quests.events.QuestsEvent;

/**
 * Represents an Actions Editor-related event
 */
public abstract class ActionsEditorEvent extends QuestsEvent {
    protected ConversationContext context;
    
    public ActionsEditorEvent(final ConversationContext context) {
        this.context = context;
    }
    
    public ActionsEditorEvent(final ConversationContext context, boolean async) {
        super(async);
        this.context = context;
    }
    
    /**
     * Returns the context involved in this event
     * 
     * @return ConversationContext which is involved in this event
     */
    public final ConversationContext getConversationContext() {
        return context;
    }
}
