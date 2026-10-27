/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.events.editor.quests;

import me.pikamug.quests.convo.quests.FabricQuestsEditorStringPrompt;
import net.fabricmc.fabric.api.event.Event;

import java.util.UUID;

public class FabricQuestsEditorPostOpenStringPromptEvent extends FabricQuestsEditorEvent {
    public static final Event<Listener<FabricQuestsEditorPostOpenStringPromptEvent>> EVENT = createEvent();

    private final UUID uuid;
    private final FabricQuestsEditorStringPrompt prompt;
    
    public FabricQuestsEditorPostOpenStringPromptEvent(final UUID uuid, final FabricQuestsEditorStringPrompt prompt) {
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
     * Returns the string prompt involved in this event
     * 
     * @return Prompt which is involved in this event
     */
    @Override
    public FabricQuestsEditorStringPrompt getPrompt() {
        return prompt;
    }
}
