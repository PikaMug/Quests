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

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.convo.quests.QuestsEditorStringPrompt;
import me.pikamug.quests.events.editor.quests.BukkitQuestsEditorPostOpenStringPromptEvent;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.quests.util.SessionData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Prompt;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class OverridePrompt extends QuestsEditorStringPrompt {

    private final @NotNull UUID uuid;
    private final Prompt oldPrompt;
    private final String promptText;
    private final String classPrefix;
    
    public OverridePrompt(final UUID uuid, final Prompt old, final String promptText) {
        super(uuid);
        this.uuid = uuid;
        oldPrompt = old;
        classPrefix = old.getClass().getSimpleName();
        this.promptText = promptText;
    }
    
    private final int size = 1;
    
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle() {
        return null;
    }
    
    @Override
    public String getQueryText() {
        return promptText;
    }

    @Override
    public @NotNull String getPromptText() {
        final BukkitQuestsEditorPostOpenStringPromptEvent event
                = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
        BukkitQuestsPlugin.getInstance().getServer().getPluginManager().callEvent(event);

        return ChatColor.YELLOW + getQueryText();
    }

    @Override
    public void acceptInput(final String input) {
        if (input == null) {
            return;
        }
        final CommandSender sender = Bukkit.getEntity(uuid);
        if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
            sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorMessageCleared"));
            SessionData.set(uuid, classPrefix + "-override", BukkitLang.get("cmdClear"));
        } else if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
            SessionData.set(uuid, classPrefix + "-override", input);
        }
        return oldPrompt;
    }
    
    public static class Builder {
        private @NotNull UUID uuid;
        private Prompt oldPrompt;
        private String promptText = "Enter input";
        
        public Builder sender(final UUID uuid) {
            this.uuid = uuid;
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
            return new OverridePrompt(uuid, oldPrompt, promptText);
        }
    }
}
