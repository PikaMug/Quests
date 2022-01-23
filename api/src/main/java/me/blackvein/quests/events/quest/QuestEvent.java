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

import org.bukkit.event.HandlerList;

import me.blackvein.quests.quests.IQuest;
import me.blackvein.quests.events.QuestsEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a quest-related event
 */
public abstract class QuestEvent extends QuestsEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final IQuest quest;
    
    public QuestEvent(final IQuest quest) {
        this.quest = quest;
    }
    
    public QuestEvent(final IQuest quest, final boolean async) {
        super(async);
        this.quest = quest;
    }
    
    /**
     * Returns the quest involved in this event
     * 
     * @return Quest which is involved in this event
     */
    public final IQuest getQuest() {
        return quest;
    }
    
    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
     
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
