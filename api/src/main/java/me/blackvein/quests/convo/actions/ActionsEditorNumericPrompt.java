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

package me.blackvein.quests.convo.actions;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;

import me.blackvein.quests.Quests;
import me.blackvein.quests.actions.ActionFactory;
import me.blackvein.quests.convo.QuestsNumericPrompt;

public abstract class ActionsEditorNumericPrompt extends QuestsNumericPrompt {
    private final ConversationContext context;
    private ActionFactory factory;
    
    public ActionsEditorNumericPrompt(final ConversationContext context) {
        this.context = context;
        if (context != null && context.getPlugin() != null) {
            this.factory = ((Quests)context.getPlugin()).getActionFactory();
        }
    }
    
    @Deprecated
    public ActionsEditorNumericPrompt(final ConversationContext context, final ActionFactory factory) {
        this.context = context;
        this.factory = factory;
    }
    
    @Override
    public String getName() {
        return getClass().getSimpleName();
    }
    
    public ConversationContext getConversationContext() {
        return context;
    }
    
    public ActionFactory getActionFactory() {
        return factory;
    }
    
    public abstract int getSize();
    
    public abstract String getTitle(ConversationContext context);
    
    public abstract ChatColor getNumberColor(ConversationContext context, int number);
    
    public abstract String getSelectionText(ConversationContext context, int number);
    
    public abstract String getAdditionalText(ConversationContext context, int number);
}
