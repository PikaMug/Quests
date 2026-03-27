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
import me.pikamug.quests.events.QuestsEvent;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a quester-related event
 */
public abstract class BukkitQuesterEvent extends QuestsEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final BukkitQuester quester;
    
    public BukkitQuesterEvent(final BukkitQuester quester) {
        this.quester = quester;
    }
    
    public BukkitQuesterEvent(final BukkitQuester quester, final boolean async) {
        super(async);
        this.quester = quester;

    }
    
    /**
     * Returns the quester involved in this event
     * 
     * @return Quester which is involved in this event
     */
    public final BukkitQuester getQuester() {
        return quester;
    }
    
    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
     
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
