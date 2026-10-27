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

import me.pikamug.quests.events.FabricQuestsEvent;
import me.pikamug.quests.quests.Quest;

/**
 * Represents a quest-related event
 */
public abstract class FabricQuestEvent extends FabricQuestsEvent {
    private final Quest quest;

    public FabricQuestEvent(final Quest quest) {
        this.quest = quest;
    }

    public FabricQuestEvent(final Quest quest, final boolean async) {
        super(async);
        this.quest = quest;
    }

    /**
     * Returns the quest involved in this event
     * 
     * @return Quest which is involved in this event
     */
    public final Quest getQuest() {
        return quest;
    }
}
