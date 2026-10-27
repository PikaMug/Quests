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
 * Called when the /quests editor command is run by a player
 */
public class FabricQuestsCommandPreEditorEvent extends FabricQuestsCommandEvent implements FabricCancellable {
    public static final Event<Listener<FabricQuestsCommandPreEditorEvent>> EVENT = createEvent();

    private boolean cancel = false;
    
    public FabricQuestsCommandPreEditorEvent(final FabricQuester quester) {
        super(quester);
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
