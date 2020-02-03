package me.blackvein.quests.convo;

import org.bukkit.conversations.StringPrompt;
import org.bukkit.event.HandlerList;

public abstract class QuestsStringPrompt extends StringPrompt {
    private static final HandlerList HANDLERS = new HandlerList();
    
    public QuestsStringPrompt() {
    }
    
    public String getName() {
        return getClass().getSimpleName();
    }
    
    public HandlerList getHandlers() {
        return HANDLERS;
    }
     
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
