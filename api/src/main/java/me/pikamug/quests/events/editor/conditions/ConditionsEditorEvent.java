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

import me.pikamug.quests.Quests;
import me.pikamug.quests.conditions.ConditionFactory;
import me.pikamug.quests.events.QuestsEvent;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents an Conditions Editor-related event
 */
public abstract class ConditionsEditorEvent extends QuestsEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    protected ConversationContext context;
    private final ConditionFactory factory;
    private final Prompt prompt;
    
    public ConditionsEditorEvent(final ConversationContext context, final Prompt prompt) {
        this.context = context;
        this.factory = ((Quests) Objects.requireNonNull(context.getPlugin())).getConditionFactory();
        this.prompt = prompt;
    }
    
    public ConditionsEditorEvent(final ConversationContext context, final Prompt prompt, final boolean async) {
        super(async);
        this.context = context;
        this.factory = ((Quests) Objects.requireNonNull(context.getPlugin())).getConditionFactory();
        this.prompt = prompt;
    }
    
    /**
     * Returns the context involved in this event
     * 
     * @return ConversationContext which is involved in this event
     */
    public ConversationContext getConversationContext() {
        return context;
    }
    
    /**
     * Returns the factory involved in this event
     * 
     * @return ConditionFactory which is involved in this event
     */
    public ConditionFactory getConditionFactory() {
        return factory;
    }
    
    /**
     * Returns the prompt involved in this event
     * 
     * @return Prompt which is involved in this event
     */
    public Prompt getPrompt() {
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
