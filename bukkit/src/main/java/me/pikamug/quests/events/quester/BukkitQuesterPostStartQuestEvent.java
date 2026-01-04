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

import me.pikamug.quests.player.BukkitQuester;
import me.pikamug.quests.quests.Quest;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called after an online quester starts a quest
 */
public class BukkitQuesterPostStartQuestEvent extends BukkitQuesterEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Quest quest;
    
    public BukkitQuesterPostStartQuestEvent(final BukkitQuester quester, final Quest quest) {
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
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
     
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
