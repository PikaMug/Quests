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

import me.pikamug.quests.convo.quests.QuestsEditorStringPrompt;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class BukkitQuestsEditorPostOpenStringPromptEvent extends BukkitQuestsEditorEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final UUID uuid;
    private final QuestsEditorStringPrompt prompt;
    
    public BukkitQuestsEditorPostOpenStringPromptEvent(final UUID uuid, final QuestsEditorStringPrompt prompt) {
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
    public QuestsEditorStringPrompt getPrompt() {
        return prompt;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
    
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
