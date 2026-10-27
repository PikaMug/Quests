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

import me.pikamug.quests.events.FabricQuestsEvent;
import me.pikamug.quests.player.FabricQuester;

/**
 * Represents a quester-related event
 */
public abstract class FabricQuesterEvent extends FabricQuestsEvent {
    private final FabricQuester quester;

    public FabricQuesterEvent(final FabricQuester quester) {
        this.quester = quester;
    }

    public FabricQuesterEvent(final FabricQuester quester, final boolean async) {
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
