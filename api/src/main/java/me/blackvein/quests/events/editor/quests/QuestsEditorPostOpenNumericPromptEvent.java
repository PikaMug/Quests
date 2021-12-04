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

package me.blackvein.quests.events.editor.quests;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.event.HandlerList;

import me.blackvein.quests.convo.quests.QuestsEditorNumericPrompt;
import org.jetbrains.annotations.NotNull;

public class QuestsEditorPostOpenNumericPromptEvent extends QuestsEditorEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final QuestsEditorNumericPrompt prompt;
    
    public QuestsEditorPostOpenNumericPromptEvent(final ConversationContext context, final QuestsEditorNumericPrompt prompt) {
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
    public QuestsEditorNumericPrompt getPrompt() {
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
