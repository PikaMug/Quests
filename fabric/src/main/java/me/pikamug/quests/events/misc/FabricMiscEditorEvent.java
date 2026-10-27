/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.events.misc;

import me.pikamug.quests.convo.QuestsPrompt;
import me.pikamug.quests.events.FabricQuestsEvent;
import net.fabricmc.fabric.api.event.Event;

import java.util.UUID;

/**
 * Represents a miscellaneous editor-related event
 */
public class FabricMiscEditorEvent extends FabricQuestsEvent {
    public static final Event<Listener<FabricMiscEditorEvent>> EVENT = createEvent();

    private final UUID uuid;
    private final QuestsPrompt prompt;

    public FabricMiscEditorEvent(final UUID uuid, final QuestsPrompt prompt) {
        this.uuid = uuid;
        this.prompt = prompt;
    }

    public FabricMiscEditorEvent(final UUID uuid, final QuestsPrompt prompt, final boolean async) {
        super(async);
        this.uuid = uuid;
        this.prompt = prompt;
    }

    /**
     * Returns the UUID involved in this event
     *
     * @return UUID which is involved in this event
     */
    public UUID getUniqueId() {
        return uuid;
    }

    /**
     * Returns the prompt involved in this event
     *
     * @return Prompt which is involved in this event
     */
    public QuestsPrompt getPrompt() {
        return prompt;
    }
}
