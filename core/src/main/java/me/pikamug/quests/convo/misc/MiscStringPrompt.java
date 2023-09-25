/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.convo.misc;

import me.pikamug.quests.convo.QuestsStringPrompt;
import org.bukkit.conversations.ConversationContext;

public abstract class MiscStringPrompt extends QuestsStringPrompt {
    private final ConversationContext context;

    public MiscStringPrompt(final ConversationContext context) {
        this.context = context;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    public ConversationContext getConversationContext() {
        return context;
    }

    public abstract int getSize();

    public abstract String getTitle(ConversationContext context);

    public abstract String getQueryText(ConversationContext context);
}
