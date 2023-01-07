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
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class QuesterPostViewEffectEvent extends QuesterEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Entity entity;
    private final String effect;
    private final boolean redoable;

    public QuesterPostViewEffectEvent(final Quester quester, Entity entity, String effect, boolean redoable) {
        super(quester);
        this.entity = entity;
        this.effect = effect;
        this.redoable = redoable;
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
