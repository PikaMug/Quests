/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.convo.generic;

import me.pikamug.quests.convo.quests.QuestsEditorStringPrompt;
import me.pikamug.quests.events.editor.quests.QuestsEditorPostOpenStringPromptEvent;
import me.pikamug.quests.util.BukkitLang;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.jetbrains.annotations.NotNull;

public class OverridePrompt extends QuestsEditorStringPrompt {
    private final Prompt oldPrompt;
    private final String promptText;
    private final String classPrefix;
    
    public OverridePrompt(final ConversationContext context, final Prompt old, final String promptText) {
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
    public String getTitle(final ConversationContext context) {
        return null;
    }
    
    @Override
    public String getQueryText(final ConversationContext context) {
        return promptText;
    }

    @Override
    public @NotNull String getPromptText(final @NotNull ConversationContext context) {
        if (context.getPlugin() != null) {
            final QuestsEditorPostOpenStringPromptEvent event
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
        }

        return ChatColor.YELLOW + getQueryText(context);
    }

    @Override
    public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
        if (input != null) {
            if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorMessageCleared"));
                context.setSessionData(classPrefix + "-override", BukkitLang.get("cmdClear"));
            } else if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                context.setSessionData(classPrefix + "-override", input);
            }
        }
        return oldPrompt;
    }
    
    public static class Builder {
        private ConversationContext context;
        private Prompt oldPrompt;
        private String promptText = "Enter input";
        
        public Builder context(final ConversationContext context) {
            this.context = context;
            return this;
        }
        
        public Builder source(final Prompt prompt) {
            this.oldPrompt = prompt;
            return this;
        }

        public Builder promptText(final String text) {
            this.promptText = text;
            return this;
        }
        
        public OverridePrompt build() {
            return new OverridePrompt(context, oldPrompt, promptText);
        }
    }
}
