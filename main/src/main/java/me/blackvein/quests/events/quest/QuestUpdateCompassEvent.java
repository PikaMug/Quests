package me.blackvein.quests.events.quest;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import me.blackvein.quests.Quest;
import me.blackvein.quests.Quester;

/**
 * Called when a quest has its compass target updated for a quester
 */
public class QuestUpdateCompassEvent extends QuestEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Quester quester;
    private final Location target;
    private boolean cancel = false;

    public QuestUpdateCompassEvent(final Quest quest, final Quester who, final Location target) {
        super(quest);
        this.quester = who;
        this.target = target;
    }
    
    /**
     * Returns the quester involved in this event
     * 
     * @return Quester who is involved in this event
     */
    public Quester getQuester() {
        return quester;
    }
    
    /**
     * Returns the new compass target in this event
     * 
     * @return Location which shall be the new target
     */
    public Location getNewCompassTarget() {
        return target;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(final boolean cancel) {
        this.cancel = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
     
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
