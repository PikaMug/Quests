package me.blackvein.quests.events.editor.quests;

import org.bukkit.conversations.ConversationContext;

import me.blackvein.quests.events.QuestsEvent;

/**
 * Represents a Quests Editor-related event
 */
public abstract class QuestsEditorEvent extends QuestsEvent {
    protected ConversationContext context;
    
    public QuestsEditorEvent(final ConversationContext context) {
        this.context = context;
    }
    
    public QuestsEditorEvent(final ConversationContext context, boolean async) {
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
