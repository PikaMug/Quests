/*
 * Copyright (c) 2014 PikaMug and contributors. All rights reserved.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package me.pikamug.quests.convo.actions.menu;

import me.pikamug.quests.actions.Action;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.quests.components.Stage;
import me.pikamug.quests.convo.QuestsNumericPrompt;
import me.pikamug.quests.convo.actions.ActionsEditorNumericPrompt;
import me.pikamug.quests.convo.actions.ActionsEditorStringPrompt;
import me.pikamug.quests.convo.actions.main.ActionMainPrompt;
import me.pikamug.quests.events.editor.actions.ActionsEditorPostOpenNumericPromptEvent;
import me.pikamug.quests.events.editor.actions.ActionsEditorPostOpenStringPromptEvent;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.BukkitLanguage;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ActionMenuPrompt extends ActionsEditorNumericPrompt {
    
    private final BukkitQuestsPlugin plugin;
    
    public ActionMenuPrompt(final ConversationContext context) {
        super(context);
        this.plugin = (BukkitQuestsPlugin)context.getPlugin();
    }

    private final int size = 4;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle(final ConversationContext context) {
        final String title = BukkitLanguage.get("eventEditorTitle");
        return title + (plugin.hasLimitedAccess(context.getForWhom()) ? ChatColor.RED + " (" + BukkitLanguage.get("trialMode")
                + ")" : "");
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
            return ChatColor.YELLOW + BukkitLanguage.get("eventEditorCreate");
        case 2:
            return ChatColor.YELLOW + BukkitLanguage.get("eventEditorEdit");
        case 3:
            return ChatColor.YELLOW + BukkitLanguage.get("eventEditorDelete");
        case 4:
            return ChatColor.RED + BukkitLanguage.get("exit");
        default:
            return null;
        }
    }
    
    @Override
    public String getAdditionalText(final ConversationContext context, final int number) {
        return null;
    }

    @Override
    public @NotNull String getBasicPromptText(final @NotNull ConversationContext context) {
        final ActionsEditorPostOpenNumericPromptEvent event 
                = new ActionsEditorPostOpenNumericPromptEvent(context, this);
        plugin.getServer().getPluginManager().callEvent(event);
        
        final StringBuilder text = new StringBuilder(ChatColor.GOLD + getTitle(context));
        for (int i = 1; i <= size; i++) {
            text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i).append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i));
        }
        return text.toString();
    }

    @Override
    protected Prompt acceptValidatedInput(final ConversationContext context, final Number input) {
        final CommandSender cs = (CommandSender) context.getForWhom();
        switch (input.intValue()) {
        case 1:
            if (cs.hasPermission("quests.editor.actions.create") 
                    || cs.hasPermission("quests.editor.events.create")) {
                context.setSessionData(Key.A_OLD_ACTION, "");
                return new ActionSelectCreatePrompt(context);
            } else {
                cs.sendMessage(ChatColor.RED + BukkitLanguage.get("noPermission"));
                return new ActionMenuPrompt(context);
            }
        case 2:
            if (cs.hasPermission("quests.editor.actions.edit") 
                    || cs.hasPermission("quests.editor.events.edit")) {
                if (plugin.getLoadedActions().isEmpty()) {
                    context.getForWhom().sendRawMessage(ChatColor.YELLOW 
                            + BukkitLanguage.get("eventEditorNoneToEdit"));
                    return new ActionMenuPrompt(context);
                } else {
                    return new ActionSelectEditPrompt(context);
                }
            } else {
                cs.sendMessage(ChatColor.RED + BukkitLanguage.get("noPermission"));
                return new ActionMenuPrompt(context);
            }
        case 3:
            if (cs.hasPermission("quests.editor.actions.delete") 
                    || cs.hasPermission("quests.editor.events.delete")) {
                if (plugin.getLoadedActions().isEmpty()) {
                    context.getForWhom().sendRawMessage(ChatColor.YELLOW 
                            + BukkitLanguage.get("eventEditorNoneToDelete"));
                    return new ActionMenuPrompt(context);
                } else {
                    return new ActionSelectDeletePrompt(context);
                }
            } else {
                cs.sendMessage(ChatColor.RED + BukkitLanguage.get("noPermission"));
                return new ActionMenuPrompt(context);
            }
        case 4:
            context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLanguage.get("exited"));
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
            return BukkitLanguage.get("eventCreateTitle");
        }
        
        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLanguage.get("eventEditorEnterEventName");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final ActionsEditorPostOpenStringPromptEvent event 
                    = new ActionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);

            return ChatColor.GOLD + getTitle(context) + "\n" + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, String input) {
            if (input == null) {
                context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLanguage.get("itemCreateInvalidInput"));
                return new ActionSelectCreatePrompt(context);
            }
            input = input.trim();
            if (!input.equalsIgnoreCase(BukkitLanguage.get("cmdCancel"))) {
                for (final Action action : plugin.getLoadedActions()) {
                    if (action.getName().equalsIgnoreCase(input)) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLanguage.get("eventEditorExists"));
                        return new ActionSelectCreatePrompt(context);
                    }
                }
                final List<String> actionNames = plugin.getActionFactory().getNamesOfActionsBeingEdited();
                if (actionNames.contains(input)) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLanguage.get("questEditorBeingEdited"));
                    return new ActionSelectCreatePrompt(context);
                }
                if (input.contains(".") || input.contains(",")) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLanguage.get("questEditorInvalidQuestName"));
                    return new ActionSelectCreatePrompt(context);
                }
                if (input.equals("")) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLanguage.get("itemCreateInvalidInput"));
                    return new ActionSelectCreatePrompt(context);
                }
                context.setSessionData(Key.A_NAME, input);
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
            return BukkitLanguage.get("eventEditTitle");
        }
        
        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLanguage.get("eventEditorEnterEventName");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final ActionsEditorPostOpenStringPromptEvent event 
                    = new ActionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            final List<String> names = plugin.getLoadedActions().stream().map(Action::getName).collect(Collectors.toList());
            return sendClickableMenu(getTitle(context), names, getQueryText(context), context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLanguage.get("cmdCancel"))) {
                final Action action = plugin.getAction(input);
                if (action != null) {
                    context.setSessionData(Key.A_OLD_ACTION, action.getName());
                    context.setSessionData(Key.A_NAME, action.getName());
                    plugin.getActionFactory().loadData(context, action);
                    return new ActionMainPrompt(context);
                }
                context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLanguage.get("eventEditorNotFound"));
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
            return BukkitLanguage.get("eventDeleteTitle");
        }
        
        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLanguage.get("eventEditorEnterEventName");
        }
        
        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final ActionsEditorPostOpenStringPromptEvent event 
                    = new ActionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            final List<String> names = plugin.getLoadedActions().stream().map(Action::getName).collect(Collectors.toList());
            return sendClickableMenu(getTitle(context), names, getQueryText(context), context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLanguage.get("cmdCancel"))) {
                final LinkedList<String> used = new LinkedList<>();
                final Action action = plugin.getAction(input);
                if (action != null) {
                    for (final Quest quest : plugin.getLoadedQuests()) {
                        for (final Stage stage : quest.getStages()) {
                            if (stage.getFinishAction() != null 
                                    && stage.getFinishAction().getName().equalsIgnoreCase(action.getName())) {
                                used.add(quest.getName());
                                break;
                            }
                        }
                    }
                    if (used.isEmpty()) {
                        context.setSessionData(Key.ED_EVENT_DELETE, action.getName());
                        return new ActionConfirmDeletePrompt(context);
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLanguage.get("eventEditorEventInUse")
                        + " \"" + ChatColor.DARK_PURPLE + action.getName() + ChatColor.RED + "\":");
                        for (final String s : used) {
                            context.getForWhom().sendRawMessage(ChatColor.RED + "- " + ChatColor.DARK_RED + s);
                        }
                        context.getForWhom().sendRawMessage(ChatColor.RED 
                                + BukkitLanguage.get("eventEditorMustModifyQuests"));
                        return new ActionSelectDeletePrompt(context);
                    }
                }
                context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLanguage.get("eventEditorNotFound"));
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
        
        private final int size = 2;
        
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @SuppressWarnings("unused")
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                return ChatColor.GREEN;
            case 2:
                return ChatColor.RED;
            default:
                return null;
            }
        }

        @SuppressWarnings("unused")
        public String getSelectionText(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                return ChatColor.GREEN + BukkitLanguage.get("yesWord");
            case 2:
                return ChatColor.RED + BukkitLanguage.get("noWord");
            default:
                return null;
            }
        }
        
        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLanguage.get("confirmDelete");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final ActionsEditorPostOpenStringPromptEvent event 
                    = new ActionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.RED + getQueryText(context) + " (" + ChatColor.YELLOW
                    + context.getSessionData(Key.ED_EVENT_DELETE) + ChatColor.RED + ")\n");
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i));
            }
            return QuestsNumericPrompt.sendClickableSelection(text.toString(), context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(BukkitLanguage.get("yesWord"))) {
                plugin.getActionFactory().deleteAction(context);
                return Prompt.END_OF_CONVERSATION;
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(BukkitLanguage.get("noWord"))) {
                return new ActionMenuPrompt(context);
            } else {
                return new ActionConfirmDeletePrompt(context);
            }
        }
    }
}
