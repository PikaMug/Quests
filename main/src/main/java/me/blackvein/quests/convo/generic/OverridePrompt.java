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

package me.blackvein.quests.convo.generic;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

import me.blackvein.quests.convo.quests.QuestsEditorStringPrompt;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenStringPromptEvent;

public class OverridePrompt extends QuestsEditorStringPrompt {
    private final Prompt oldPrompt;
    private String promptText;
    private String classPrefix;
    
    public OverridePrompt(ConversationContext context, Prompt old, String promptText) {
        super(context);
        oldPrompt = old;
        classPrefix = old.getClass().getSimpleName();
        this.promptText = promptText;
    }
    
    private final int size = 1;
    
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle(ConversationContext context) {
        return null;
    }
    
    @Override
    public String getQueryText(ConversationContext context) {
        return null;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
        context.getPlugin().getServer().getPluginManager().callEvent(event);

        String text = ChatColor.YELLOW + promptText + "\n";;
        return text;
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        context.setSessionData(classPrefix + "-override", input);
        return oldPrompt;
    }
    
    public static class Builder {
        private ConversationContext context;
        private Prompt oldPrompt;
        private String promptText = "Enter input";
        
        public Builder context(ConversationContext context) {
            this.context = context;
            return this;
        }
        
        public Builder source(Prompt prompt) {
            this.oldPrompt = prompt;
            return this;
        }

        public Builder promptText(String text) {
            this.promptText = text;
            return this;
        }
        
        public OverridePrompt build() {
            return new OverridePrompt(context, oldPrompt, promptText);
        }
    }
}
