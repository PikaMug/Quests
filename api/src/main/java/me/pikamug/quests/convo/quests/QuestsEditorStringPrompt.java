/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.convo.quests;

import me.pikamug.quests.quests.QuestFactory;
import me.pikamug.quests.Quests;
import me.pikamug.quests.convo.QuestsStringPrompt;
import org.bukkit.conversations.ConversationContext;

public abstract class QuestsEditorStringPrompt extends QuestsStringPrompt {
    private final ConversationContext context;
    private QuestFactory factory;
    
    public QuestsEditorStringPrompt(final ConversationContext context) {
        this.context = context;
        if (context != null && context.getPlugin() != null) {
            factory = ((Quests)context.getPlugin()).getQuestFactory();
        }
    }
    
    @Override
    public String getName() {
        return getClass().getSimpleName();
    }
    
    public ConversationContext getConversationContext() {
        return context;
    }
    
    public QuestFactory getQuestFactory() {
        return factory;
    }
    
    public abstract String getTitle(ConversationContext context);
    
    public abstract String getQueryText(ConversationContext context);
}
