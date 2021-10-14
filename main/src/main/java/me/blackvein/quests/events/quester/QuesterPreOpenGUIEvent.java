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

import me.blackvein.quests.Quest;
import me.blackvein.quests.Quester;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

/**
 * Called before a quester opens a quest selection GUI
 */
public class QuesterPreOpenGUIEvent extends QuesterEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final NPC npc;
    LinkedList<Quest> quests;
    private boolean cancel = false;
    
    public QuesterPreOpenGUIEvent(final Quester quester, final NPC npc, final LinkedList<Quest> quests) {
        super(quester);
        this.npc = npc;
        this.quests = quests;
    }
    
    /**
     * Returns the NPC involved in this event
     * 
     * @return NPC who is involved in this event
     */
    public NPC getNPC() {
        return npc;
    }
    
    /**
     * Returns the list of quests involved in this event
     * 
     * @return List of quests involved in this event
     */
    public LinkedList<Quest> getQuests() {
        return quests;
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
