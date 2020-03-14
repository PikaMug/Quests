package me.blackvein.quests.convo.quests.prompts;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

import me.blackvein.quests.convo.quests.QuestsEditorStringPrompt;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenStringPromptEvent;
import me.blackvein.quests.util.Lang;

public class OverridePrompt extends QuestsEditorStringPrompt {
    private final Prompt oldPrompt;
    private String promptText;
    private String classPrefix;
    
    public OverridePrompt(ConversationContext context, Prompt old, String promptText) {
        super(context, null);
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
        QuestsEditorPostOpenStringPromptEvent event 
                = new QuestsEditorPostOpenStringPromptEvent(context, null, this);
        context.getPlugin().getServer().getPluginManager().callEvent(event);

        String text = ChatColor.YELLOW + promptText + "\n";;
        return text;
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        if (input.equalsIgnoreCase(Lang.get("cmdClear")) == false 
                && input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
            context.setSessionData(classPrefix + "-override", input);
        } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
            context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("cleared"));
            context.setSessionData(classPrefix + "-override", null);
        }
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
