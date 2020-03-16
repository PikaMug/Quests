/*******************************************************************************************************
 * Continued by PikaMug (formerly HappyPikachu) with permission from _Blackvein_. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

package me.blackvein.quests.events.editor.actions;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.event.HandlerList;

import me.blackvein.quests.Quests;
import me.blackvein.quests.actions.ActionFactory;
import me.blackvein.quests.convo.actions.ActionsEditorStringPrompt;

public class ActionsEditorPostOpenStringPromptEvent extends ActionsEditorEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private ActionFactory factory;
    private ActionsEditorStringPrompt prompt;

    public ActionsEditorPostOpenStringPromptEvent(ConversationContext context, ActionsEditorStringPrompt prompt) {
        super(context, prompt);
        this.context = context;
        this.factory = ((Quests)context.getPlugin()).getActionFactory();
        this.prompt = prompt;
    }
    
    @Deprecated
    public ActionsEditorPostOpenStringPromptEvent(ConversationContext context, ActionFactory factory, 
            ActionsEditorStringPrompt prompt) {
        super(context, factory, prompt);
        this.context = context;
        this.factory = factory;
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
     * Returns the string prompt involved in this event
     * 
     * @return Prompt which is involved in this event
     */
    public ActionsEditorStringPrompt getPrompt() {
        return prompt;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
    
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
