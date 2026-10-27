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
import me.pikamug.quests.quests.Quest;
import net.fabricmc.fabric.api.event.Event;

/**
 * Called after an online quester completes a quest
 */
public class FabricQuesterPostCompleteQuestEvent extends FabricQuesterEvent {
    public static final Event<Listener<FabricQuesterPostCompleteQuestEvent>> EVENT = createEvent();

    private final Quest quest;
    
    public FabricQuesterPostCompleteQuestEvent(final FabricQuester quester, final Quest quest) {
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
}
