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

package me.blackvein.quests.events.command;

import org.bukkit.event.HandlerList;

import me.blackvein.quests.Quester;
import me.blackvein.quests.events.QuestsEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a Quests command-related event
 */
public abstract class QuestsCommandEvent extends QuestsEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Quester quester;
    
    public QuestsCommandEvent(final Quester quester) {
        this.quester = quester;
    }
    
    public QuestsCommandEvent(final Quester quester, final boolean async) {
        super(async);
        this.quester = quester;
    }
    
    /**
     * Returns the quester involved in this event
     * 
     * @return Quester which is involved in this event
     */
    public final Quester getQuester() {
        return quester;
    }
    
    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
     
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
