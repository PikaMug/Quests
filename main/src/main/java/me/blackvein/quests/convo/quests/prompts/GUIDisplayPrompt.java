package me.blackvein.quests.convo.quests.prompts;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.inventory.ItemStack;

import me.blackvein.quests.Quest;
import me.blackvein.quests.Quests;
import me.blackvein.quests.convo.quests.QuestsEditorNumericPrompt;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenNumericPromptEvent;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;

public class GUIDisplayPrompt extends QuestsEditorNumericPrompt {
    private final Quests plugin;
    
    public GUIDisplayPrompt(Quests plugin, ConversationContext context) {
        super(context);
        this.plugin = plugin;
    }
    
    private final int size = 3;
    
    public int getSize() {
        return size;
    }
    
    public String getTitle(ConversationContext context) {
        return Lang.get("questGUITitle");
    }
    
    public ChatColor getNumberColor(ConversationContext context, int number) {
        switch (number) {
        case 1:
            return ChatColor.BLUE;
        case 2:
            return ChatColor.RED;
        case 3:
            return ChatColor.GREEN;
        default:
            return null;
        }
    }
    
    public String getSelectionText(ConversationContext context, int number) {
        switch (number) {
        case 1:
            return ChatColor.YELLOW + Lang.get("stageEditorDeliveryAddItem");
        case 2:
            return ChatColor.YELLOW + Lang.get("clear");
        case 3:
            return ChatColor.YELLOW + Lang.get("done");
        default:
            return null;
        }
    }
    
    public String getAdditionalText(ConversationContext context, int number) {
        return null;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        QuestsEditorPostOpenNumericPromptEvent event 
                = new QuestsEditorPostOpenNumericPromptEvent(context, this);
        plugin.getServer().getPluginManager().callEvent(event);
        
        if (context.getSessionData("tempStack") != null) {
            ItemStack stack = (ItemStack) context.getSessionData("tempStack");
            boolean failed = false;
            for (Quest quest : plugin.getQuests()) {
                if (quest.getGUIDisplay() != null) {
                    if (ItemUtil.compareItems(stack, quest.getGUIDisplay(), false) == 0) {
                        String error = Lang.get("questGUIError");
                        error = error.replace("<quest>", ChatColor.DARK_PURPLE + quest.getName() + ChatColor.RED);
                        context.getForWhom().sendRawMessage(ChatColor.RED + error);
                        failed = true;
                        break;
                    }
                }
            }
            if (!failed) {
                context.setSessionData(CK.Q_GUIDISPLAY, context.getSessionData("tempStack"));
            }
            context.setSessionData("tempStack", null);
        }
        String text = ChatColor.GOLD + getTitle(context) + "\n";
        if (context.getSessionData(CK.Q_GUIDISPLAY) != null) {
            ItemStack stack = (ItemStack) context.getSessionData(CK.Q_GUIDISPLAY);
            text += " " + ChatColor.RESET + ItemUtil.getDisplayString(stack) + "\n";
        } else {
            text += " " + ChatColor.GRAY + "(" + Lang.get("noneSet") + ")\n";
        }
        for (int i = 1; i <= size; i++) {
            text += getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                    + getSelectionText(context, i) + "\n";
        }
        return text;
    }

    @Override
    protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
        switch (input.intValue()) {
        case 1:
            return new ItemStackPrompt(GUIDisplayPrompt.this);
        case 2:
            context.setSessionData(CK.Q_GUIDISPLAY, null);
            context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("questGUICleared"));
            return new GUIDisplayPrompt(plugin, context);
        case 3:
            return plugin.getQuestFactory().returnToMenu(context);
        default:
            return null;
        }
    }
}
