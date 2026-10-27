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

import me.pikamug.quests.player.FabricQuester;
import me.pikamug.quests.events.FabricQuestsEvent;

/**
 * Represents a Quests command-related event
 */
public abstract class FabricQuestsCommandEvent extends FabricQuestsEvent {
    private final FabricQuester quester;

    public FabricQuestsCommandEvent(final FabricQuester quester) {
        this.quester = quester;
    }

    public FabricQuestsCommandEvent(final FabricQuester quester, final boolean async) {
        super(async);
        this.quester = quester;
    }

    /**
     * Returns the quester involved in this event
     * 
     * @return Quester which is involved in this event
     */
    public final FabricQuester getQuester() {
        return quester;
    }
}
