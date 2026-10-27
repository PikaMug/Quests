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

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.convo.QuestsPrompt;
import me.pikamug.quests.events.FabricQuestsEvent;
import me.pikamug.quests.quests.QuestFactory;

import java.util.UUID;

/**
 * Represents a Quests Editor-related event
 */
public abstract class FabricQuestsEditorEvent extends FabricQuestsEvent {
    private final UUID uuid;
    private final QuestsPrompt prompt;
    private final QuestFactory factory;
    
    public FabricQuestsEditorEvent(final UUID uuid, final QuestsPrompt prompt) {
        this.uuid = uuid;
        this.prompt = prompt;
        this.factory = FabricQuestsPlugin.getInstance().getQuestFactory();
    }
    
    public FabricQuestsEditorEvent(final UUID uuid, final QuestsPrompt prompt, final boolean async) {
        super(async);
        this.uuid = uuid;
        this.prompt = prompt;
        this.factory = FabricQuestsPlugin.getInstance().getQuestFactory();
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

    /**
     * Returns the factory involved in this event
     *
     * @return QuestFactory which is involved in this event
     */
    public QuestFactory getQuestFactory() {
        return factory;
    }
}
