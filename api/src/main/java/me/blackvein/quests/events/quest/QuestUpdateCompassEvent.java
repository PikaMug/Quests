/*
 * Copyright (c) 2014 PikaMug and contributors. All rights reserved.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package me.blackvein.quests.events.quest;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import me.blackvein.quests.Quest;
import me.blackvein.quests.Quester;
import org.jetbrains.annotations.NotNull;

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
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
     
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
