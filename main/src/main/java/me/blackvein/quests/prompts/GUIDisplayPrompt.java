package me.blackvein.quests.prompts;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.inventory.ItemStack;

import me.blackvein.quests.Quest;
import me.blackvein.quests.QuestFactory;
import me.blackvein.quests.Quests;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenGUIDisplayPromptEvent;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;

public class GUIDisplayPrompt extends NumericPrompt {
    private final Quests plugin;
    private final QuestFactory questFactory;
    
    public GUIDisplayPrompt(Quests plugin, QuestFactory qf) {
        this.plugin = plugin;
        this.questFactory = qf;
    }
    
    private final int size = 3;
    
    public int getSize() {
        return size;
    }
    
    public String getTitle() {
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

    @Override
    public String getPromptText(ConversationContext context) {
        QuestsEditorPostOpenGUIDisplayPromptEvent event = new QuestsEditorPostOpenGUIDisplayPromptEvent(questFactory, context);
        plugin.getServer().getPluginManager().callEvent(event);
        
        if (context.getSessionData("tempStack") != null) {
            ItemStack stack = (ItemStack) context.getSessionData("tempStack");
            boolean failed = false;
            for (Quest quest : plugin.getQuests()) {
                if (quest.getGUIDisplay() != null) {
                    if (ItemUtil.compareItems(stack, quest.getGUIDisplay(), false) == 0) {
                        String error = Lang.get("questGUIError");
                        error = error.replaceAll("<quest>", ChatColor.DARK_PURPLE + quest.getName() + ChatColor.RED);
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
        String text = ChatColor.GOLD + getTitle() + "\n";
        if (context.getSessionData(CK.Q_GUIDISPLAY) != null) {
            ItemStack stack = (ItemStack) context.getSessionData(CK.Q_GUIDISPLAY);
            text += " " + ChatColor.RESET + ItemUtil.getDisplayString(stack) + "\n";
        } else {
            text += " " + ChatColor.GRAY + "(" + Lang.get("noneSet") + ")\n";
        }
        for (int i = 1; i <= size; i++) {
            text += getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " + getSelectionText(context, i) + "\n";
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
                return new GUIDisplayPrompt(plugin, questFactory);
            case 3:
                return questFactory.returnToMenu();
            default:
                return null;
        }
    }
}
