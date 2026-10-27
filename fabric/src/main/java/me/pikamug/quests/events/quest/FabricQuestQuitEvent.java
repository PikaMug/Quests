/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.events.quest;

import me.pikamug.quests.events.FabricCancellable;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
import net.fabricmc.fabric.api.event.Event;

/**
 * Called when a quest is quit by a quester
 */
public class FabricQuestQuitEvent extends FabricQuestEvent implements FabricCancellable {
    public static final Event<Listener<FabricQuestQuitEvent>> EVENT = createEvent();

    private final Quester quester;
    private boolean cancel = false;

    public FabricQuestQuitEvent(final Quest quest, final Quester who) {
        super(quest);
        this.quester = who;
    }
    
    /**
     * Returns the quester involved in this event
     * 
     * @return Quester who is involved in this event
     */
    public Quester getQuester() {
        return quester;
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
