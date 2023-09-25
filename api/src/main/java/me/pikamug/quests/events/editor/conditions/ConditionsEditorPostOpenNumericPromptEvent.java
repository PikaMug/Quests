/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.events.editor.conditions;

import me.pikamug.quests.convo.conditions.ConditionsEditorNumericPrompt;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.event.HandlerList;

import org.jetbrains.annotations.NotNull;

public class ConditionsEditorPostOpenNumericPromptEvent extends ConditionsEditorEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final ConditionsEditorNumericPrompt prompt;
    
    public ConditionsEditorPostOpenNumericPromptEvent(final ConversationContext context, final ConditionsEditorNumericPrompt prompt) {
        super(context, prompt);
        this.context = context;
        this.prompt = prompt;
    }
    
    /**
     * Returns the numeric prompt involved in this event
     * 
     * @return Prompt which is involved in this event
     */
    @Override
    public ConditionsEditorNumericPrompt getPrompt() {
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
