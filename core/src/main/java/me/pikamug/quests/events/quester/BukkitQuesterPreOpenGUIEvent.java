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

package me.pikamug.quests.events.quester;

import me.pikamug.quests.quests.BukkitQuest;
import me.pikamug.quests.player.BukkitQuester;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.UUID;

/**
 * Called before a quester opens a quest selection GUI
 */
public class BukkitQuesterPreOpenGUIEvent extends BukkitQuesterEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final UUID npc;
    LinkedList<BukkitQuest> quests;
    private boolean cancel = false;
    
    public BukkitQuesterPreOpenGUIEvent(final BukkitQuester quester, final UUID npc, final LinkedList<BukkitQuest> quests) {
        super(quester);
        this.npc = npc;
        this.quests = quests;
    }
    
    /**
     * Returns the UUID of the NPC involved in this event
     * 
     * @return UUID of NPC who is involved in this event
     */
    public UUID getNPC() {
        return npc;
    }
    
    /**
     * Returns the list of quests involved in this event
     * 
     * @return List of quests involved in this event
     */
    public LinkedList<BukkitQuest> getQuests() {
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
