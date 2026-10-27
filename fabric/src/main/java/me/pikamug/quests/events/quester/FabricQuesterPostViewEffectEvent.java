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

import me.pikamug.quests.player.FabricQuester;
import me.pikamug.quests.util.FabricLocation;
import net.fabricmc.fabric.api.event.Event;

import java.util.UUID;

public class FabricQuesterPostViewEffectEvent extends FabricQuesterEvent {
    public static final Event<Listener<FabricQuesterPostViewEffectEvent>> EVENT = createEvent();

    private final UUID targetUuid;
    private final FabricLocation targetLocation;
    private final String effect;
    private final boolean redoable;

    public FabricQuesterPostViewEffectEvent(final FabricQuester quester, final UUID targetUuid,
                                            final FabricLocation targetLocation, final String effect,
                                            final boolean redoable) {
        super(quester);
        this.targetUuid = targetUuid;
        this.targetLocation = targetLocation;
        this.effect = effect;
        this.redoable = redoable;
    }

    /**
     * Get the target of this event, usually an NPC
     *
     * @return UUID of target
     */
    public UUID getTargetUuid() {
        return targetUuid;
    }

    /**
     * Get the target location for this event
     *
     * @return Location of target
     */
    public FabricLocation getTargetLocation() {
        return targetLocation;
    }

    /**
     * Get the effect involved in this event
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
}
