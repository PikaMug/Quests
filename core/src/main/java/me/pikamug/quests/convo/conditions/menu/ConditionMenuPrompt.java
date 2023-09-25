/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.convo.conditions.menu;

import me.pikamug.quests.conditions.Condition;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.quests.components.Stage;
import me.pikamug.quests.convo.QuestsNumericPrompt;
import me.pikamug.quests.convo.conditions.ConditionsEditorNumericPrompt;
import me.pikamug.quests.convo.conditions.ConditionsEditorStringPrompt;
import me.pikamug.quests.convo.conditions.main.ConditionMainPrompt;
import me.pikamug.quests.events.editor.conditions.ConditionsEditorPostOpenNumericPromptEvent;
import me.pikamug.quests.events.editor.conditions.ConditionsEditorPostOpenStringPromptEvent;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.BukkitLang;
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
        final String title = BukkitLang.get("conditionEditorTitle");
        return title + (plugin.hasLimitedAccess(context.getForWhom()) ? ChatColor.RED + " (" + BukkitLang.get("trialMode")
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
            return ChatColor.YELLOW + BukkitLang.get("conditionEditorCreate");
        case 2:
            return ChatColor.YELLOW + BukkitLang.get("conditionEditorEdit");
        case 3:
            return ChatColor.YELLOW + BukkitLang.get("conditionEditorDelete");
        case 4:
            return ChatColor.RED + BukkitLang.get("exit");
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
                cs.sendMessage(ChatColor.RED + BukkitLang.get("noPermission"));
                return new ConditionMenuPrompt(context);
            }
        case 2:
            if (cs.hasPermission("quests.conditions.edit")) {
                if (plugin.getLoadedConditions().isEmpty()) {
                    context.getForWhom().sendRawMessage(ChatColor.YELLOW 
                            + BukkitLang.get("conditionEditorNoneToEdit"));
                    return new ConditionMenuPrompt(context);
                } else {
                    return new ConditionSelectEditPrompt(context);
                }
            } else {
                cs.sendMessage(ChatColor.RED + BukkitLang.get("noPermission"));
                return new ConditionMenuPrompt(context);
            }
        case 3:
            if (cs.hasPermission("quests.conditions.delete")) {
                if (plugin.getLoadedConditions().isEmpty()) {
                    context.getForWhom().sendRawMessage(ChatColor.YELLOW 
                            + BukkitLang.get("conditionEditorNoneToDelete"));
                    return new ConditionMenuPrompt(context);
                } else {
                    return new ConditionSelectDeletePrompt(context);
                }
            } else {
                cs.sendMessage(ChatColor.RED + BukkitLang.get("noPermission"));
                return new ConditionMenuPrompt(context);
            }
        case 4:
            context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("exited"));
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
            return BukkitLang.get("conditionCreateTitle");
        }
        
        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("conditionEditorEnterName");
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
                context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("itemCreateInvalidInput"));
                return new ConditionSelectCreatePrompt(context);
            }
            input = input.trim();
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                for (final Condition c : plugin.getLoadedConditions()) {
                    if (c.getName().equalsIgnoreCase(input)) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("conditionEditorExists"));
                        return new ConditionSelectCreatePrompt(context);
                    }
                }
                final List<String> actionNames = plugin.getConditionFactory().getNamesOfConditionsBeingEdited();
                if (actionNames.contains(input)) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("questEditorBeingEdited"));
                    return new ConditionSelectCreatePrompt(context);
                }
                if (input.contains(".") || input.contains(",")) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("questEditorInvalidQuestName"));
                    return new ConditionSelectCreatePrompt(context);
                }
                if (input.equals("")) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("itemCreateInvalidInput"));
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
            return BukkitLang.get("conditionEditTitle");
        }
        
        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("conditionEditorEnterName");
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
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final Condition c = plugin.getCondition(input);
                if (c != null) {
                    context.setSessionData(Key.C_OLD_CONDITION, c.getName());
                    context.setSessionData(Key.C_NAME, c.getName());
                    plugin.getConditionFactory().loadData(context, c);
                    return new ConditionMainPrompt(context);
                }
                context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("conditionEditorNotFound"));
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
            return BukkitLang.get("conditionDeleteTitle");
        }
        
        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("conditionEditorEnterName");
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
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
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
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("conditionEditorInUse")
                        + " \"" + ChatColor.DARK_PURPLE + c.getName() + ChatColor.RED + "\":");
                        for (final String s : used) {
                            context.getForWhom().sendRawMessage(ChatColor.RED + "- " + ChatColor.DARK_RED + s);
                        }
                        context.getForWhom().sendRawMessage(ChatColor.RED 
                                + BukkitLang.get("eventEditorMustModifyQuests"));
                        return new ConditionSelectDeletePrompt(context);
                    }
                }
                context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("conditionEditorNotFound"));
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
                return ChatColor.GREEN + BukkitLang.get("yesWord");
            case 2:
                return ChatColor.RED + BukkitLang.get("noWord");
            default:
                return null;
            }
        }
        
        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("confirmDelete");
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
            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(BukkitLang.get("yesWord"))) {
                plugin.getConditionFactory().deleteCondition(context);
                return Prompt.END_OF_CONVERSATION;
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(BukkitLang.get("noWord"))) {
                return new ConditionMenuPrompt(context);
            } else {
                return new ConditionConfirmDeletePrompt(context);
            }
        }
    }
}
