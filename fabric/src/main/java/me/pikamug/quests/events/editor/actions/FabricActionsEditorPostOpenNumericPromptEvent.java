/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.events.editor.actions;

import me.pikamug.quests.convo.actions.FabricActionsEditorIntegerPrompt;
import net.fabricmc.fabric.api.event.Event;

import java.util.UUID;

public class FabricActionsEditorPostOpenNumericPromptEvent extends FabricActionsEditorEvent {
    public static final Event<Listener<FabricActionsEditorPostOpenNumericPromptEvent>> EVENT = createEvent();

    private final UUID uuid;
    private final FabricActionsEditorIntegerPrompt prompt;
    
    public FabricActionsEditorPostOpenNumericPromptEvent(final UUID uuid, final FabricActionsEditorIntegerPrompt prompt) {
        super(uuid, prompt);
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
     * Returns the numeric prompt involved in this event
     * 
     * @return Prompt which is involved in this event
     */
    @Override
    public FabricActionsEditorIntegerPrompt getPrompt() {
        return prompt;
    }
}
