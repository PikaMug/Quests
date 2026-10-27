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
import net.fabricmc.fabric.api.event.Event;

/**
 * Called before a quester fails a quest
 */
public class FabricQuesterPreFailQuestEvent extends FabricQuesterEvent implements FabricCancellable {
    public static final Event<Listener<FabricQuesterPreFailQuestEvent>> EVENT = createEvent();

    private final Quest quest;
    private boolean cancel = false;
    
    public FabricQuesterPreFailQuestEvent(final FabricQuester quester, final Quest quest) {
        super(quester);
        this.quest = quest;
    }
    
    /**
     * Returns the quest involved in this event
     * 
     * @return Quest which is involved in this event
     */
    public Quest getQuest() {
        return quest;
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
