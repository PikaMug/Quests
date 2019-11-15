package me.blackvein.quests.events.editor.quests;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.event.HandlerList;

import me.blackvein.quests.QuestFactory;

public class QuestsEditorPostOpenStageMainPromptEvent extends QuestsEditorEvent {
    private static final HandlerList handlers = new HandlerList();
    private final QuestFactory factory;
    private final int stageNum;

    public QuestsEditorPostOpenStageMainPromptEvent(QuestFactory factory, int stageNum, ConversationContext context) {
        super(context);
        this.context = context;
        this.factory = factory;
        this.stageNum = stageNum;
    }
    
    public QuestFactory getQuestFactory() {
        return factory;
    }
    
    public int getStageNumber() {
        return stageNum;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
