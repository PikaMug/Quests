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

import me.pikamug.quests.events.QuestsEvent;
import me.pikamug.quests.quests.Quest;
import org.bukkit.event.HandlerList;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a quest-related event
 */
public abstract class QuestEvent extends QuestsEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Quest quest;
    
    public QuestEvent(final Quest quest) {
        this.quest = quest;
    }
    
    public QuestEvent(final Quest quest, final boolean async) {
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
    
    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
     
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
