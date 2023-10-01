/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
