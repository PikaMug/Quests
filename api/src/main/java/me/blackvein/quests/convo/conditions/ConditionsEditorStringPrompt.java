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

package me.blackvein.quests.convo.conditions;

import me.blackvein.quests.QuestsAPI;
import me.blackvein.quests.conditions.ConditionFactory;
import me.blackvein.quests.convo.QuestsStringPrompt;
import org.bukkit.conversations.ConversationContext;

public abstract class ConditionsEditorStringPrompt extends QuestsStringPrompt {
    private final ConversationContext context;
    private ConditionFactory factory;
    
    public ConditionsEditorStringPrompt(final ConversationContext context) {
        this.context = context;
        if (context != null && context.getPlugin() != null) {
            this.factory = ((QuestsAPI)context.getPlugin()).getConditionFactory();
        }
    }
    
    @Deprecated
    public ConditionsEditorStringPrompt(final ConversationContext context, final ConditionFactory factory) {
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
    
    public ConditionFactory getConditionFactory() {
        return factory;
    }
    
    public abstract String getTitle(ConversationContext context);
    
    public abstract String getQueryText(ConversationContext context);
}
