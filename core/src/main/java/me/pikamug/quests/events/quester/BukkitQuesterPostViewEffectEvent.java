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

import lol.pyr.znpcsplus.api.npc.Npc;
import me.pikamug.quests.player.BukkitQuester;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class BukkitQuesterPostViewEffectEvent extends BukkitQuesterEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Entity entity;
    private final Npc npc;
    private final String effect;
    private final boolean redoable;

    public BukkitQuesterPostViewEffectEvent(final BukkitQuester quester, Entity entity, String effect, boolean redoable) {
        super(quester);
        this.entity = entity;
        this.effect = effect;
        this.redoable = redoable;
        this.npc=null;
    }

    public BukkitQuesterPostViewEffectEvent(final BukkitQuester quester, Npc npc, String effect, boolean redoable) {
        super(quester);
        this.npc = npc;
        this.effect = effect;
        this.redoable = redoable;
        this.entity = null;
    }

    /**
     * Returns the entity involved in this event
     *
     * @return entity who is involved in this event
     */
    public Entity getEntity() {
        return entity;
    }

    /**
     * Returns the npc involved in this event (only if using ZNPCsPlus 2.0.0 or higher)
     *
     * @return npc who is involved in this event
     */
    public Npc getNpc() {
        return npc;
    }

    /**
     * Returns the effect involved in this event
     *
     * @return Effect which is involved in this event
     */
    public String getEffect() {
        return effect;
    }

    /**
     * Whether the effect is for a redoable quest
     *
     * @return true if redoable
     */
    public boolean isRedoable() {
        return redoable;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
