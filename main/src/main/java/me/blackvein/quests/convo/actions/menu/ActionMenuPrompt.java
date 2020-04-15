package me.blackvein.quests.convo.actions.menu;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import me.blackvein.quests.Quest;
import me.blackvein.quests.Quests;
import me.blackvein.quests.Stage;
import me.blackvein.quests.actions.Action;
import me.blackvein.quests.convo.actions.ActionsEditorNumericPrompt;
import me.blackvein.quests.convo.actions.ActionsEditorStringPrompt;
import me.blackvein.quests.convo.actions.main.ActionMainPrompt;
import me.blackvein.quests.events.editor.actions.ActionsEditorPostOpenNumericPromptEvent;
import me.blackvein.quests.events.editor.actions.ActionsEditorPostOpenStringPromptEvent;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.Lang;

public class ActionMenuPrompt extends ActionsEditorNumericPrompt {
    
    private final Quests plugin;
    
    public ActionMenuPrompt(ConversationContext context) {
        super(context);
        this.plugin = (Quests)context.getPlugin();
    }

    private final int size = 4;
    
    public int getSize() {
        return size;
    }
    
    public String getTitle(ConversationContext context) {
        return Lang.get("eventEditorTitle");
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
            return ChatColor.YELLOW + Lang.get("eventEditorCreate");
        case 2:
            return ChatColor.YELLOW + Lang.get("eventEditorEdit");
        case 3:
            return ChatColor.YELLOW + Lang.get("eventEditorDelete");
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
        ActionsEditorPostOpenNumericPromptEvent event = new ActionsEditorPostOpenNumericPromptEvent(context, this);
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
            if (player.hasPermission("quests.editor.actions.create") 
                    || player.hasPermission("quests.editor.events.create")) {
                context.setSessionData(CK.E_OLD_EVENT, "");
                return new ActionSelectCreatePrompt(context);
            } else {
                player.sendMessage(ChatColor.RED + Lang.get("noPermission"));
                return new ActionMenuPrompt(context);
            }
        case 2:
            if (player.hasPermission("quests.editor.actions.edit") 
                    || player.hasPermission("quests.editor.events.edit")) {
                if (plugin.getActions().isEmpty()) {
                    ((Player) context.getForWhom()).sendMessage(ChatColor.YELLOW 
                            + Lang.get("eventEditorNoneToEdit"));
                    return new ActionMenuPrompt(context);
                } else {
                    return new ActionSelectEditPrompt();
                }
            } else {
                player.sendMessage(ChatColor.RED + Lang.get("noPermission"));
                return new ActionMenuPrompt(context);
            }
        case 3:
            if (player.hasPermission("quests.editor.actions.delete") 
                    || player.hasPermission("quests.editor.events.delete")) {
                if (plugin.getActions().isEmpty()) {
                    ((Player) context.getForWhom()).sendMessage(ChatColor.YELLOW 
                            + Lang.get("eventEditorNoneToDelete"));
                    return new ActionMenuPrompt(context);
                } else {
                    return new ActionSelectDeletePrompt();
                }
            } else {
                player.sendMessage(ChatColor.RED + Lang.get("noPermission"));
                return new ActionMenuPrompt(context);
            }
        case 4:
            ((Player) context.getForWhom()).sendMessage(ChatColor.YELLOW + Lang.get("exited"));
            return Prompt.END_OF_CONVERSATION;
        default:
            return new ActionMenuPrompt(context);
        }
    }
    
    public class ActionSelectCreatePrompt extends ActionsEditorStringPrompt {
        public ActionSelectCreatePrompt(ConversationContext context) {
            super(context);
        }

        public String getTitle(ConversationContext context) {
            return Lang.get("eventEditorCreate");
        }
        
        public String getQueryText(ConversationContext context) {
            return Lang.get("eventEditorEnterEventName");
        }

        @Override
        public String getPromptText(ConversationContext context) {
            ActionsEditorPostOpenStringPromptEvent event = new ActionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            String text = ChatColor.GOLD + getTitle(context) + "\n" + ChatColor.YELLOW + getQueryText(context);
            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input == null) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
                return new ActionSelectCreatePrompt(context);
            }
            input = input.trim();
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                for (Action e : plugin.getActions()) {
                    if (e.getName().equalsIgnoreCase(input)) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorExists"));
                        return new ActionSelectCreatePrompt(context);
                    }
                }
                List<String> actionNames = plugin.getActionFactory().getNamesOfActionsBeingEdited();
                if (actionNames.contains(input)) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorSomeone"));
                    return new ActionSelectCreatePrompt(context);
                }
                if (input.contains(".") || input.contains(",")) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorInvalidQuestName"));
                    return new ActionSelectCreatePrompt(context);
                }
                if (input.equals("")) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
                    return new ActionSelectCreatePrompt(context);
                }
                context.setSessionData(CK.E_NAME, input);
                actionNames.add(input);
                plugin.getActionFactory().setNamesOfActionsBeingEdited(actionNames);
                return new ActionMainPrompt(context);
            } else {
                return new ActionMenuPrompt(context);
            }
        }
    }

    private class ActionSelectEditPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.GOLD + "- " + Lang.get("eventEditorEdit") + " -\n";
            for (Action a : plugin.getActions()) {
                text += ChatColor.AQUA + a.getName() + ChatColor.GRAY + ", ";
            }
            text = text.substring(0, text.length() - 2) + "\n";
            text += ChatColor.YELLOW + Lang.get("eventEditorEnterEventName");
            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                Action a = plugin.getAction(input);
                if (a != null) {
                    context.setSessionData(CK.E_OLD_EVENT, a.getName());
                    context.setSessionData(CK.E_NAME, a.getName());
                    plugin.getActionFactory().loadData(a, context);
                    return new ActionMainPrompt(context);
                }
                ((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("eventEditorNotFound"));
                return new ActionSelectEditPrompt();
            } else {
                return new ActionMenuPrompt(context);
            }
        }
    }
    
    private class ActionSelectDeletePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.GOLD + "- " + Lang.get("eventEditorDelete") + " -\n";
            for (Action a : plugin.getActions()) {
                text += ChatColor.AQUA + a.getName() + ChatColor.GRAY + ",";
            }
            text = text.substring(0, text.length() - 1) + "\n";
            text += ChatColor.YELLOW + Lang.get("eventEditorEnterEventName");
            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                LinkedList<String> used = new LinkedList<String>();
                Action a = plugin.getAction(input);
                if (a != null) {
                    for (Quest quest : plugin.getQuests()) {
                        for (Stage stage : quest.getStages()) {
                            if (stage.getFinishAction() != null 
                                    && stage.getFinishAction().getName().equalsIgnoreCase(a.getName())) {
                                used.add(quest.getName());
                                break;
                            }
                        }
                    }
                    if (used.isEmpty()) {
                        context.setSessionData(CK.ED_EVENT_DELETE, a.getName());
                        return new ActionConfirmDeletePrompt();
                    } else {
                        ((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("eventEditorEventInUse") 
                        + " \"" + ChatColor.DARK_PURPLE + a.getName() + ChatColor.RED + "\":");
                        for (String s : used) {
                            ((Player) context.getForWhom()).sendMessage(ChatColor.RED + "- " + ChatColor.DARK_RED + s);
                        }
                        ((Player) context.getForWhom()).sendMessage(ChatColor.RED 
                                + Lang.get("eventEditorMustModifyQuests"));
                        return new ActionSelectDeletePrompt();
                    }
                }
                ((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("eventEditorNotFound"));
                return new ActionSelectDeletePrompt();
            } else {
                return new ActionMenuPrompt(context);
            }
        }
    }

    private class ActionConfirmDeletePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.GREEN + "" + ChatColor.BOLD + "1" + ChatColor.RESET + "" + ChatColor.GREEN + " - " 
        + Lang.get("yesWord") + "\n";
            text += ChatColor.RED + "" + ChatColor.BOLD + "2" + ChatColor.RESET + "" + ChatColor.RED + " - " 
        + Lang.get("noWord");
            return ChatColor.RED + Lang.get("confirmDelete") + " (" + ChatColor.YELLOW 
                    + (String) context.getSessionData(CK.ED_EVENT_DELETE) + ChatColor.RED + ")\n" + text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(Lang.get("yesWord"))) {
                plugin.getActionFactory().deleteAction(context);
                return new ActionMenuPrompt(context);
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(Lang.get("noWord"))) {
                return new ActionMenuPrompt(context);
            } else {
                return new ActionConfirmDeletePrompt();
            }
        }
    }
}
