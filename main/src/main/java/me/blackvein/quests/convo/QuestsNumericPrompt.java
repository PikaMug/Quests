package me.blackvein.quests.convo;

import org.bukkit.conversations.NumericPrompt;
import org.bukkit.event.HandlerList;

public abstract class QuestsNumericPrompt extends NumericPrompt {
    private static final HandlerList HANDLERS = new HandlerList();
    
    public QuestsNumericPrompt() {
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
