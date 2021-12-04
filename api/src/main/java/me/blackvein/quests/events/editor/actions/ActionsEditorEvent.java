/*
 * Copyright (c) 2014 PikaMug and contributors. All rights reserved.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package me.blackvein.quests.events.editor.actions;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.event.HandlerList;

import me.blackvein.quests.Quests;
import me.blackvein.quests.actions.ActionFactory;
import me.blackvein.quests.events.QuestsEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents an Actions Editor-related event
 */
public abstract class ActionsEditorEvent extends QuestsEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    protected ConversationContext context;
    private final ActionFactory factory;
    private final Prompt prompt;
    
    public ActionsEditorEvent(final ConversationContext context, final Prompt prompt) {
        this.context = context;
        this.factory = ((Quests) Objects.requireNonNull(context.getPlugin())).getActionFactory();
        this.prompt = prompt;
    }
    
    public ActionsEditorEvent(final ConversationContext context, final Prompt prompt, final boolean async) {
        super(async);
        this.context = context;
        this.factory = ((Quests) Objects.requireNonNull(context.getPlugin())).getActionFactory();
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
     * @return ActionFactory which is involved in this event
     */
    public ActionFactory getActionFactory() {
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
