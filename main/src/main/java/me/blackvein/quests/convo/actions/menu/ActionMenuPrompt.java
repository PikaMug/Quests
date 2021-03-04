/*******************************************************************************************************
 * Copyright (c) 2014 PikaMug and contributors. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

package me.blackvein.quests.convo.actions.menu;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

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
    
    public ActionMenuPrompt(final ConversationContext context) {
        super(context);
        this.plugin = (Quests)context.getPlugin();
    }

    private final int size = 4;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle(final ConversationContext context) {
        return Lang.get("eventEditorTitle");
    }
    
    @Override
    public ChatColor getNumberColor(final ConversationContext context, final int number) {
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
    
    @Override
    public String getSelectionText(final ConversationContext context, final int number) {
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
    
    @Override
    public String getAdditionalText(final ConversationContext context, final int number) {
        return null;
    }

    @Override
    public String getPromptText(final ConversationContext context) {
        final ActionsEditorPostOpenNumericPromptEvent event 
                = new ActionsEditorPostOpenNumericPromptEvent(context, this);
        plugin.getServer().getPluginManager().callEvent(event);
        
        String text = ChatColor.GOLD + getTitle(context);
        for (int i = 1; i <= size; i++) {
            text += "\n" + getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                    + getSelectionText(context, i);
        }
        return text;
    }

    @Override
    protected Prompt acceptValidatedInput(final ConversationContext context, final Number input) {
        final CommandSender cs = (CommandSender) context.getForWhom();
        switch (input.intValue()) {
        case 1:
            if (cs.hasPermission("quests.editor.actions.create") 
                    || cs.hasPermission("quests.editor.events.create")) {
                context.setSessionData(CK.E_OLD_EVENT, "");
                return new ActionSelectCreatePrompt(context);
            } else {
                cs.sendMessage(ChatColor.RED + Lang.get("noPermission"));
                return new ActionMenuPrompt(context);
            }
        case 2:
            if (cs.hasPermission("quests.editor.actions.edit") 
                    || cs.hasPermission("quests.editor.events.edit")) {
                if (plugin.getActions().isEmpty()) {
                    context.getForWhom().sendRawMessage(ChatColor.YELLOW 
                            + Lang.get("eventEditorNoneToEdit"));
                    return new ActionMenuPrompt(context);
                } else {
                    return new ActionSelectEditPrompt(context);
                }
            } else {
                cs.sendMessage(ChatColor.RED + Lang.get("noPermission"));
                return new ActionMenuPrompt(context);
            }
        case 3:
            if (cs.hasPermission("quests.editor.actions.delete") 
                    || cs.hasPermission("quests.editor.events.delete")) {
                if (plugin.getActions().isEmpty()) {
                    context.getForWhom().sendRawMessage(ChatColor.YELLOW 
                            + Lang.get("eventEditorNoneToDelete"));
                    return new ActionMenuPrompt(context);
                } else {
                    return new ActionSelectDeletePrompt(context);
                }
            } else {
                cs.sendMessage(ChatColor.RED + Lang.get("noPermission"));
                return new ActionMenuPrompt(context);
            }
        case 4:
            context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("exited"));
            return Prompt.END_OF_CONVERSATION;
        default:
            return new ActionMenuPrompt(context);
        }
    }
    
    public class ActionSelectCreatePrompt extends ActionsEditorStringPrompt {
        public ActionSelectCreatePrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("eventCreateTitle");
        }
        
        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("eventEditorEnterEventName");
        }

        @Override
        public String getPromptText(final ConversationContext context) {
            final ActionsEditorPostOpenStringPromptEvent event 
                    = new ActionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final String text = ChatColor.GOLD + getTitle(context) + "\n" + ChatColor.YELLOW + getQueryText(context);
            return text;
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, String input) {
            if (input == null) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
                return new ActionSelectCreatePrompt(context);
            }
            input = input.trim();
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                for (final Action a : plugin.getActions()) {
                    if (a.getName().equalsIgnoreCase(input)) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorExists"));
                        return new ActionSelectCreatePrompt(context);
                    }
                }
                final List<String> actionNames = plugin.getActionFactory().getNamesOfActionsBeingEdited();
                if (actionNames.contains(input)) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorBeingEdited"));
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

    public class ActionSelectEditPrompt extends ActionsEditorStringPrompt {
        
        public ActionSelectEditPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("eventEditTitle");
        }
        
        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("eventEditorEnterEventName");
        }

        @Override
        public String getPromptText(final ConversationContext context) {
            final ActionsEditorPostOpenStringPromptEvent event 
                    = new ActionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            String text = ChatColor.GOLD + getTitle(context) + "\n";
            final List<String> names = plugin.getActions().stream().map(Action::getName).collect(Collectors.toList());
            for (int i = 0; i < names.size(); i++) {
                text += ChatColor.AQUA + names.get(i);
                if (i < (names.size() - 1)) {
                    text += ChatColor.GRAY + ", ";
                }
            }
            text += "\n" + ChatColor.YELLOW + getQueryText(context);
            return text;
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                final Action a = plugin.getAction(input);
                if (a != null) {
                    context.setSessionData(CK.E_OLD_EVENT, a.getName());
                    context.setSessionData(CK.E_NAME, a.getName());
                    plugin.getActionFactory().loadData(a, context);
                    return new ActionMainPrompt(context);
                }
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorNotFound"));
                return new ActionSelectEditPrompt(context);
            } else {
                return new ActionMenuPrompt(context);
            }
        }
    }
    
    public class ActionSelectDeletePrompt extends ActionsEditorStringPrompt {

        public ActionSelectDeletePrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("eventDeleteTitle");
        }
        
        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("eventEditorEnterEventName");
        }
        
        @Override
        public String getPromptText(final ConversationContext context) {
            final ActionsEditorPostOpenStringPromptEvent event 
                    = new ActionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            String text = ChatColor.GOLD + getTitle(context) + "\n";
            final List<String> names = plugin.getActions().stream().map(Action::getName).collect(Collectors.toList());
            for (int i = 0; i < names.size(); i++) {
                text += ChatColor.AQUA + names.get(i);
                if (i < (names.size() - 1)) {
                    text += ChatColor.GRAY + ", ";
                }
            }
            text += "\n" + ChatColor.YELLOW + getQueryText(context);
            return text;
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                final LinkedList<String> used = new LinkedList<String>();
                final Action a = plugin.getAction(input);
                if (a != null) {
                    for (final Quest quest : plugin.getQuests()) {
                        for (final Stage stage : quest.getStages()) {
                            if (stage.getFinishAction() != null 
                                    && stage.getFinishAction().getName().equalsIgnoreCase(a.getName())) {
                                used.add(quest.getName());
                                break;
                            }
                        }
                    }
                    if (used.isEmpty()) {
                        context.setSessionData(CK.ED_EVENT_DELETE, a.getName());
                        return new ActionConfirmDeletePrompt(context);
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorEventInUse") 
                        + " \"" + ChatColor.DARK_PURPLE + a.getName() + ChatColor.RED + "\":");
                        for (final String s : used) {
                            context.getForWhom().sendRawMessage(ChatColor.RED + "- " + ChatColor.DARK_RED + s);
                        }
                        context.getForWhom().sendRawMessage(ChatColor.RED 
                                + Lang.get("eventEditorMustModifyQuests"));
                        return new ActionSelectDeletePrompt(context);
                    }
                }
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorNotFound"));
                return new ActionSelectDeletePrompt(context);
            } else {
                return new ActionMenuPrompt(context);
            }
        }
    }

    public class ActionConfirmDeletePrompt extends ActionsEditorStringPrompt {
        
        public ActionConfirmDeletePrompt(final ConversationContext context) {
            super (context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }
        
        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("confirmDelete");
        }

        @Override
        public String getPromptText(final ConversationContext context) {
            final ActionsEditorPostOpenStringPromptEvent event 
                    = new ActionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            String text = ChatColor.GREEN + "" + ChatColor.BOLD + "1" + ChatColor.RESET + "" + ChatColor.GREEN + " - " 
                    + Lang.get("yesWord") + "\n";
            text += ChatColor.RED + "" + ChatColor.BOLD + "2" + ChatColor.RESET + "" + ChatColor.RED + " - " 
                    + Lang.get("noWord");
            return ChatColor.RED + getQueryText(context) + " (" + ChatColor.YELLOW 
                    + (String) context.getSessionData(CK.ED_EVENT_DELETE) + ChatColor.RED + ")\n" + text;
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(Lang.get("yesWord"))) {
                plugin.getActionFactory().deleteAction(context);
                return Prompt.END_OF_CONVERSATION;
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(Lang.get("noWord"))) {
                return new ActionMenuPrompt(context);
            } else {
                return new ActionConfirmDeletePrompt(context);
            }
        }
    }
}
