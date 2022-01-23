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

package me.blackvein.quests.events.quester;

import me.blackvein.quests.Quester;
import me.blackvein.quests.quests.IQuest;
import me.blackvein.quests.quests.IStage;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called before an online quester changes stage
 */
public class QuesterPreChangeStageEvent extends QuesterEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final IQuest quest;
    private final IStage current;
    private final IStage next;
    private boolean cancel = false;;
    
    public QuesterPreChangeStageEvent(final Quester quester, final IQuest quest, final IStage current, final IStage next) {
        super(quester);
        this.quest = quest;
        this.current = current;
        this.next = next;
    }
    
    /**
     * Returns the quest involved in this event
     * 
     * @return Quest which is involved in this event
     */
    public IQuest getQuest() {
        return quest;
    }
    
    public IStage getCurrentStage() {
        return current;
    }
    
    public IStage getNextStage() {
        return next;
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
