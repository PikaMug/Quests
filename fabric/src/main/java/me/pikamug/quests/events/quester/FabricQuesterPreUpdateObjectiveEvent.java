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

import me.pikamug.quests.events.FabricCancellable;
import me.pikamug.quests.player.FabricQuester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.Objective;
import net.fabricmc.fabric.api.event.Event;

/**
 * Called before a quester progresses an objective
 */
public class FabricQuesterPreUpdateObjectiveEvent extends FabricQuesterEvent implements FabricCancellable {
    public static final Event<Listener<FabricQuesterPreUpdateObjectiveEvent>> EVENT = createEvent();

    private final Quest quest;
    private final Objective objective;
    private boolean cancel = false;
    
    public FabricQuesterPreUpdateObjectiveEvent(final FabricQuester quester, final Quest quest, final Objective objective) {
        super(quester);
        this.quest = quest;
        this.objective = objective;
    }
    
    /**
     * Returns the quest involved in this event
     * 
     * @return Quest which is involved in this event
     */
    public Quest getQuest() {
        return quest;
    }
    
    /**
     * Returns the objective involved in this event
     * 
     * @return Objective which is involved in this event
     */
    public Objective getObjective() {
        return objective;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(final boolean cancel) {
        this.cancel = cancel;
    }
}
