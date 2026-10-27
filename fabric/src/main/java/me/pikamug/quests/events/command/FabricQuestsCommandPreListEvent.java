/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.events.command;

import me.pikamug.quests.events.FabricCancellable;
import me.pikamug.quests.player.FabricQuester;
import net.fabricmc.fabric.api.event.Event;

/**
 * Called when the /quests list command is run by a player
 */
public class FabricQuestsCommandPreListEvent extends FabricQuestsCommandEvent implements FabricCancellable {
    public static final Event<Listener<FabricQuestsCommandPreListEvent>> EVENT = createEvent();

    private final int page;
    private boolean cancel = false;

    public FabricQuestsCommandPreListEvent(final FabricQuester quester, final int page) {
        super(quester);
        this.page = page;
    }
    
    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(final boolean cancel) {
        this.cancel = cancel;
    }
    
    public int getPage() {
        return page;
    }
}
