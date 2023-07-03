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

package me.pikamug.quests.convo.conditions.menu;

import me.pikamug.quests.conditions.Condition;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.quests.Stage;
import me.pikamug.quests.convo.QuestsNumericPrompt;
import me.pikamug.quests.convo.conditions.ConditionsEditorNumericPrompt;
import me.pikamug.quests.convo.conditions.ConditionsEditorStringPrompt;
import me.pikamug.quests.convo.conditions.main.ConditionMainPrompt;
import me.pikamug.quests.events.editor.conditions.ConditionsEditorPostOpenNumericPromptEvent;
import me.pikamug.quests.events.editor.conditions.ConditionsEditorPostOpenStringPromptEvent;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.Language;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ConditionMenuPrompt extends ConditionsEditorNumericPrompt {
    
    private final BukkitQuestsPlugin plugin;
    
    public ConditionMenuPrompt(final ConversationContext context) {
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
        final String title = Language.get("conditionEditorTitle");
        return title + (plugin.hasLimitedAccess(context.getForWhom()) ? ChatColor.RED + " (" + Language.get("trialMode")
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
            return ChatColor.YELLOW + Language.get("conditionEditorCreate");
        case 2:
            return ChatColor.YELLOW + Language.get("conditionEditorEdit");
        case 3:
            return ChatColor.YELLOW + Language.get("conditionEditorDelete");
        case 4:
            return ChatColor.RED + Language.get("exit");
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
        final ConditionsEditorPostOpenNumericPromptEvent event 
                = new ConditionsEditorPostOpenNumericPromptEvent(context, this);
        plugin.getServer().getPluginManager().callEvent(event);
        
        final StringBuilder text = new StringBuilder(ChatColor.GOLD + getTitle(context));
        for (int i = 1; i <= size; i++) {
            text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                    .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i));
        }
        return text.toString();
    }

    @Override
    protected Prompt acceptValidatedInput(final ConversationContext context, final Number input) {
        final CommandSender cs = (CommandSender) context.getForWhom();
        switch (input.intValue()) {
        case 1:
            if (cs.hasPermission("quests.conditions.create")) {
                context.setSessionData(Key.C_OLD_CONDITION, "");
                return new ConditionSelectCreatePrompt(context);
            } else {
                cs.sendMessage(ChatColor.RED + Language.get("noPermission"));
                return new ConditionMenuPrompt(context);
            }
        case 2:
            if (cs.hasPermission("quests.conditions.edit")) {
                if (plugin.getLoadedConditions().isEmpty()) {
                    context.getForWhom().sendRawMessage(ChatColor.YELLOW 
                            + Language.get("conditionEditorNoneToEdit"));
                    return new ConditionMenuPrompt(context);
                } else {
                    return new ConditionSelectEditPrompt(context);
                }
            } else {
                cs.sendMessage(ChatColor.RED + Language.get("noPermission"));
                return new ConditionMenuPrompt(context);
            }
        case 3:
            if (cs.hasPermission("quests.conditions.delete")) {
                if (plugin.getLoadedConditions().isEmpty()) {
                    context.getForWhom().sendRawMessage(ChatColor.YELLOW 
                            + Language.get("conditionEditorNoneToDelete"));
                    return new ConditionMenuPrompt(context);
                } else {
                    return new ConditionSelectDeletePrompt(context);
                }
            } else {
                cs.sendMessage(ChatColor.RED + Language.get("noPermission"));
                return new ConditionMenuPrompt(context);
            }
        case 4:
            context.getForWhom().sendRawMessage(ChatColor.YELLOW + Language.get("exited"));
            return Prompt.END_OF_CONVERSATION;
        default:
            return new ConditionMenuPrompt(context);
        }
    }
    
    public class ConditionSelectCreatePrompt extends ConditionsEditorStringPrompt {
        
        public ConditionSelectCreatePrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Language.get("conditionCreateTitle");
        }
        
        @Override
        public String getQueryText(final ConversationContext context) {
            return Language.get("conditionEditorEnterName");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final ConditionsEditorPostOpenStringPromptEvent event 
                    = new ConditionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);

            return ChatColor.GOLD + getTitle(context) + "\n" + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, String input) {
            if (input == null) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Language.get("itemCreateInvalidInput"));
                return new ConditionSelectCreatePrompt(context);
            }
            input = input.trim();
            if (!input.equalsIgnoreCase(Language.get("cmdCancel"))) {
                for (final Condition c : plugin.getLoadedConditions()) {
                    if (c.getName().equalsIgnoreCase(input)) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Language.get("conditionEditorExists"));
                        return new ConditionSelectCreatePrompt(context);
                    }
                }
                final List<String> actionNames = plugin.getConditionFactory().getNamesOfConditionsBeingEdited();
                if (actionNames.contains(input)) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Language.get("questEditorBeingEdited"));
                    return new ConditionSelectCreatePrompt(context);
                }
                if (input.contains(".") || input.contains(",")) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Language.get("questEditorInvalidQuestName"));
                    return new ConditionSelectCreatePrompt(context);
                }
                if (input.equals("")) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Language.get("itemCreateInvalidInput"));
                    return new ConditionSelectCreatePrompt(context);
                }
                context.setSessionData(Key.C_NAME, input);
                actionNames.add(input);
                plugin.getConditionFactory().setNamesOfConditionsBeingEdited(actionNames);
                return new ConditionMainPrompt(context);
            } else {
                return new ConditionMenuPrompt(context);
            }
        }
    }

    public class ConditionSelectEditPrompt extends ConditionsEditorStringPrompt {
        
        public ConditionSelectEditPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Language.get("conditionEditTitle");
        }
        
        @Override
        public String getQueryText(final ConversationContext context) {
            return Language.get("conditionEditorEnterName");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final ConditionsEditorPostOpenStringPromptEvent event 
                    = new ConditionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            final List<String> names = plugin.getLoadedConditions().stream().map(Condition::getName)
                    .collect(Collectors.toList());
            return sendClickableMenu(getTitle(context), names, getQueryText(context), context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Language.get("cmdCancel"))) {
                final Condition c = plugin.getCondition(input);
                if (c != null) {
                    context.setSessionData(Key.C_OLD_CONDITION, c.getName());
                    context.setSessionData(Key.C_NAME, c.getName());
                    plugin.getConditionFactory().loadData(context, c);
                    return new ConditionMainPrompt(context);
                }
                context.getForWhom().sendRawMessage(ChatColor.RED + Language.get("conditionEditorNotFound"));
                return new ConditionSelectEditPrompt(context);
            } else {
                return new ConditionMenuPrompt(context);
            }
        }
    }
    
    public class ConditionSelectDeletePrompt extends ConditionsEditorStringPrompt {

        public ConditionSelectDeletePrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Language.get("conditionDeleteTitle");
        }
        
        @Override
        public String getQueryText(final ConversationContext context) {
            return Language.get("conditionEditorEnterName");
        }
        
        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final ConditionsEditorPostOpenStringPromptEvent event 
                    = new ConditionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            final List<String> names = plugin.getLoadedConditions().stream().map(Condition::getName)
                    .collect(Collectors.toList());
            return sendClickableMenu(getTitle(context), names, getQueryText(context), context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Language.get("cmdCancel"))) {
                final LinkedList<String> used = new LinkedList<>();
                final Condition c = plugin.getCondition(input);
                if (c != null) {
                    for (final Quest quest : plugin.getLoadedQuests()) {
                        for (final Stage stage : quest.getStages()) {
                            if (stage.getCondition() != null 
                                    && stage.getCondition().getName().equalsIgnoreCase(c.getName())) {
                                used.add(quest.getName());
                                break;
                            }
                        }
                    }
                    if (used.isEmpty()) {
                        context.setSessionData(Key.ED_CONDITION_DELETE, c.getName());
                        return new ConditionConfirmDeletePrompt(context);
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Language.get("conditionEditorInUse")
                        + " \"" + ChatColor.DARK_PURPLE + c.getName() + ChatColor.RED + "\":");
                        for (final String s : used) {
                            context.getForWhom().sendRawMessage(ChatColor.RED + "- " + ChatColor.DARK_RED + s);
                        }
                        context.getForWhom().sendRawMessage(ChatColor.RED 
                                + Language.get("eventEditorMustModifyQuests"));
                        return new ConditionSelectDeletePrompt(context);
                    }
                }
                context.getForWhom().sendRawMessage(ChatColor.RED + Language.get("conditionEditorNotFound"));
                return new ConditionSelectDeletePrompt(context);
            } else {
                return new ConditionMenuPrompt(context);
            }
        }
    }

    public class ConditionConfirmDeletePrompt extends ConditionsEditorStringPrompt {
        
        public ConditionConfirmDeletePrompt(final ConversationContext context) {
            super(context);
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
                return ChatColor.GREEN + Language.get("yesWord");
            case 2:
                return ChatColor.RED + Language.get("noWord");
            default:
                return null;
            }
        }
        
        @Override
        public String getQueryText(final ConversationContext context) {
            return Language.get("confirmDelete");
        }
        
        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final ConditionsEditorPostOpenStringPromptEvent event 
                    = new ConditionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.RED + getQueryText(context) + " (" + ChatColor.YELLOW
                    + context.getSessionData(Key.ED_CONDITION_DELETE) + ChatColor.RED + ")\n");
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
            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(Language.get("yesWord"))) {
                plugin.getConditionFactory().deleteCondition(context);
                return Prompt.END_OF_CONVERSATION;
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(Language.get("noWord"))) {
                return new ConditionMenuPrompt(context);
            } else {
                return new ConditionConfirmDeletePrompt(context);
            }
        }
    }
}
