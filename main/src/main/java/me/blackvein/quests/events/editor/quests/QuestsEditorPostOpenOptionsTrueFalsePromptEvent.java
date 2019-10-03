package me.blackvein.quests.events.editor.quests;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.event.HandlerList;

import me.blackvein.quests.QuestFactory;

public class QuestsEditorPostOpenOptionsTrueFalsePromptEvent extends QuestsEditorEvent {
    private static final HandlerList handlers = new HandlerList();
    private final QuestFactory factory;

    public QuestsEditorPostOpenOptionsTrueFalsePromptEvent(QuestFactory factory, ConversationContext context) {
        super(context);
        this.context = context;
        this.factory = factory;
    }
    
    public QuestFactory getQuestFactory() {
        return factory;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
