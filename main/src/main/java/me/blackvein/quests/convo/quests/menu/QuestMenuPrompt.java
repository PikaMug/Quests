package me.blackvein.quests.convo.quests.menu;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import me.blackvein.quests.Quest;
import me.blackvein.quests.Quests;
import me.blackvein.quests.convo.quests.QuestsEditorNumericPrompt;
import me.blackvein.quests.convo.quests.QuestsEditorStringPrompt;
import me.blackvein.quests.convo.quests.main.QuestMainPrompt;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenNumericPromptEvent;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenStringPromptEvent;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.Lang;

public class QuestMenuPrompt extends QuestsEditorNumericPrompt {
    
    private final Quests plugin;
    
    public QuestMenuPrompt(ConversationContext context) {
        super(context);
        this.plugin = (Quests)context.getPlugin();
    }

    private final int size = 4;
    
    public int getSize() {
        return size;
    }
    
    public String getTitle(ConversationContext context) {
        return Lang.get("questEditorTitle");
    }
    
    public ChatColor getNumberColor(ConversationContext context, int number) {
        switch (number) {
        case 1:
        case 2:
        case 3:
            return ChatColor.BLUE;
        case 4:
            return ChatColor.RED;
        default:
            return null;
        }
    }
    
    public String getSelectionText(ConversationContext context, int number) {
        switch (number) {
        case 1:
            return ChatColor.YELLOW + Lang.get("questEditorCreate");
        case 2:
            return ChatColor.YELLOW + Lang.get("questEditorEdit");
        case 3:
            return ChatColor.YELLOW + Lang.get("questEditorDelete");
        case 4:
            return ChatColor.RED + Lang.get("exit");
        default:
            return null;
        }
    }
    
    public String getAdditionalText(ConversationContext context, int number) {
        return null;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
        plugin.getServer().getPluginManager().callEvent(event);
        String text = ChatColor.GOLD + getTitle(context) + "\n";
        for (int i = 1; i <= size; i++) {
            text += getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                    + getSelectionText(context, i) + "\n";
        }
        return text;
    }

    @Override
    protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
        final Player player = (Player) context.getForWhom();
        switch (input.intValue()) {
        case 1:
            if (player.hasPermission("quests.editor.*") || player.hasPermission("quests.editor.create")) {
                return new QuestSelectCreatePrompt(context);
            } else {
                player.sendMessage(ChatColor.RED + Lang.get("noPermission"));
                return new QuestMenuPrompt(context);
            }
        case 2:
            if (player.hasPermission("quests.editor.*") || player.hasPermission("quests.editor.edit")) {
                return new QuestSelectEditPrompt();
            } else {
                player.sendMessage(ChatColor.RED + Lang.get("noPermission"));
                return new QuestMenuPrompt(context);
            }
        case 3:
            if (player.hasPermission("quests.editor.*") || player.hasPermission("quests.editor.delete")) {
                return new QuestSelectDeletePrompt();
            } else {
                player.sendMessage(ChatColor.RED + Lang.get("noPermission"));
                return new QuestMenuPrompt(context);
            }
        case 4:
            context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("exited"));
            return Prompt.END_OF_CONVERSATION;
        default:
            return new QuestMenuPrompt(context);
        }
    }
    
    public class QuestSelectCreatePrompt extends QuestsEditorStringPrompt {
        
        public QuestSelectCreatePrompt(ConversationContext context) {
            super(context);
        }

        public String getTitle(ConversationContext context) {
            return Lang.get("questCreateTitle");
        }
        
        public String getQueryText(ConversationContext context) {
            return Lang.get("questEditorEnterQuestName");
        }
        
        @Override
        public String getPromptText(ConversationContext context) {
            QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            String text = ChatColor.GOLD + getTitle(context)+ "\n" + ChatColor.YELLOW + getQueryText(context);
            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input == null) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
                return new QuestSelectCreatePrompt(context);
            }
            input = input.trim();
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                for (Quest q : plugin.getQuests()) {
                    if (q.getName().equalsIgnoreCase(input)) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorNameExists"));
                        return new QuestSelectCreatePrompt(context);
                    }
                }
                List<String> questNames = plugin.getQuestFactory().getNamesOfQuestsBeingEdited();
                if (questNames.contains(input)) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorBeingEdited"));
                    return new QuestSelectCreatePrompt(context);
                }
                if (input.contains(".") || input.contains(",")) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorInvalidQuestName"));
                    return new QuestSelectCreatePrompt(context);
                }
                if (input.equals("")) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
                    return new QuestSelectCreatePrompt(context);
                }
                context.setSessionData(CK.Q_NAME, input);
                questNames.add(input);
                plugin.getQuestFactory().setNamesOfQuestsBeingEdited(questNames);
                return new QuestMainPrompt(context);
            } else {
                return new QuestMenuPrompt(context);
            }
        }
    }
    
    public class QuestSelectEditPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String s = ChatColor.GOLD + Lang.get("questEditTitle") + "\n";
            for (Quest q : plugin.getQuests()) {
                s += ChatColor.GRAY + "- " + ChatColor.AQUA + q.getName() + "\n";
            }
            return s + ChatColor.YELLOW + Lang.get("questEditorEnterQuestName");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                Quest q = plugin.getQuest(input);
                if (q != null) {
                    plugin.getQuestFactory().loadQuest(context, q);
                    return new QuestMainPrompt(context);
                }
                return new QuestSelectEditPrompt();
            } else {
                return new QuestMenuPrompt(context);
            }
        }
    }
    
    public class QuestSelectDeletePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.GOLD + Lang.get("questDeleteTitle") + "\n";
            for (Quest quest : plugin.getQuests()) {
                text += ChatColor.AQUA + quest.getName() + ChatColor.GRAY + ",";
            }
            text = text.substring(0, text.length() - 1) + "\n";
            text += ChatColor.YELLOW + Lang.get("questEditorEnterQuestName");
            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                LinkedList<String> used = new LinkedList<String>();
                Quest found = plugin.getQuest(input);
                if (found != null) {
                    for (Quest q : plugin.getQuests()) {
                        if (q.getRequirements().getNeededQuests().contains(q.getName()) 
                                || q.getRequirements().getBlockQuests().contains(q.getName())) {
                            used.add(q.getName());
                        }
                    }
                    if (used.isEmpty()) {
                        context.setSessionData(CK.ED_QUEST_DELETE, found.getName());
                        return new QuestConfirmDeletePrompt();
                    } else {
                        ((Player) context.getForWhom()).sendMessage(ChatColor.RED 
                                + Lang.get("questEditorQuestAsRequirement1") + " \"" + ChatColor.DARK_PURPLE 
                                + context.getSessionData(CK.ED_QUEST_DELETE) + ChatColor.RED + "\" " 
                                + Lang.get("questEditorQuestAsRequirement2"));
                        for (String s : used) {
                            ((Player) context.getForWhom()).sendMessage(ChatColor.RED + "- " + ChatColor.DARK_RED + s);
                        }
                        ((Player) context.getForWhom()).sendMessage(ChatColor.RED 
                                + Lang.get("questEditorQuestAsRequirement3"));
                        return new QuestSelectDeletePrompt();
                    }
                }
                ((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("questEditorQuestNotFound"));
                return new QuestSelectDeletePrompt();
            } else {
                return new QuestMenuPrompt(context);
            }
        }
    }
    
    public class QuestConfirmDeletePrompt extends StringPrompt {
        
        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.GREEN + "" + ChatColor.BOLD + "1" + ChatColor.RESET + "" + ChatColor.GREEN + " - " 
                    + Lang.get("yesWord") + "\n";
            text += ChatColor.RED + "" + ChatColor.BOLD + "2" + ChatColor.RESET + "" + ChatColor.RED + " - " 
                    + Lang.get("noWord");
            return ChatColor.RED + Lang.get("confirmDelete") + " (" + ChatColor.YELLOW 
                    + (String) context.getSessionData(CK.ED_QUEST_DELETE) + ChatColor.RED + ")\n" + text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(Lang.get("yesWord"))) {
                plugin.getQuestFactory().deleteQuest(context);
                return Prompt.END_OF_CONVERSATION;
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(Lang.get("noWord"))) {
                return new QuestMenuPrompt(context);
            } else {
                return new QuestConfirmDeletePrompt();
            }
        }
    }
}
