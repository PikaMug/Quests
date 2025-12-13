/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.convo.conditions.main;

import me.pikamug.quests.conditions.Condition;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.quests.components.Stage;
import me.pikamug.quests.convo.QuestsNumericPrompt;
import me.pikamug.quests.convo.conditions.ConditionsEditorNumericPrompt;
import me.pikamug.quests.convo.conditions.ConditionsEditorStringPrompt;
import me.pikamug.quests.convo.conditions.tasks.ConditionEntityPrompt;
import me.pikamug.quests.convo.conditions.tasks.ConditionPlayerPrompt;
import me.pikamug.quests.convo.conditions.tasks.ConditionWorldPrompt;
import me.pikamug.quests.events.editor.conditions.BukkitConditionsEditorPostOpenNumericPromptEvent;
import me.pikamug.quests.events.editor.conditions.BukkitConditionsEditorPostOpenStringPromptEvent;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.BukkitLang;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ConditionMainPrompt extends ConditionsEditorNumericPrompt {
    
    private final BukkitQuestsPlugin plugin;
    
    public ConditionMainPrompt(final ConversationContext context) {
        super(context);
        this.plugin = (BukkitQuestsPlugin)context.getPlugin();
    }

    private final int size = 8;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle(final ConversationContext context) {
        return BukkitLang.get("condition") + ": " + context.getSessionData(Key.C_NAME);
    }
    
    @Override
    public ChatColor getNumberColor(final ConversationContext context, final int number) {
        switch (number) {
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        case 6:
            return ChatColor.BLUE;
        case 7:
            return ChatColor.GREEN;
        case 8:
            return ChatColor.RED;
        default:
            return null;
        }
    }
    
    @Override
    public String getSelectionText(final ConversationContext context, final int number) {
        switch (number) {
        case 1:
            return ChatColor.YELLOW + BukkitLang.get("conditionEditorSetName");
        case 2:
            return ChatColor.GOLD + BukkitLang.get("conditionEditorEntity");
        case 3:
            return ChatColor.GOLD + BukkitLang.get("eventEditorPlayer");
        case 4:
            return ChatColor.GOLD + BukkitLang.get("conditionEditorWorld");
        case 5:
            return ChatColor.YELLOW + BukkitLang.get("conditionEditorCheckPlaceholder");
        case 6:
            return ChatColor.YELLOW + BukkitLang.get("eventEditorFailQuest");
        case 7:
            return ChatColor.GREEN + BukkitLang.get("save");
        case 8:
            return ChatColor.RED + BukkitLang.get("exit");
        default:
            return null;
        }
    }
    
    @Override
    public String getAdditionalText(final ConversationContext context, final int number) {
        switch (number) {
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        case 7:
        case 8:
            return "";
        case 6:
            if (context.getSessionData(Key.C_FAIL_QUEST) == null) {
                return ChatColor.GRAY + "(" + ChatColor.RED + BukkitLang.get("false") + ChatColor.GRAY + ")";
            } else {
                final Boolean failOpt = (Boolean) context.getSessionData(Key.C_FAIL_QUEST);
                return ChatColor.GRAY + "(" + (Boolean.TRUE.equals(failOpt) ? ChatColor.GREEN + BukkitLang.get("true")
                        : ChatColor.RED + BukkitLang.get("false")) + ChatColor.GRAY + ")";
            }
        default:
            return null;
        }
    }

    @Override
    public @NotNull String getBasicPromptText(final @NotNull ConversationContext context) {
        final BukkitConditionsEditorPostOpenNumericPromptEvent event
                = new BukkitConditionsEditorPostOpenNumericPromptEvent(context, this);
        plugin.getServer().getPluginManager().callEvent(event);
        
        final StringBuilder text = new StringBuilder(ChatColor.GOLD + "- " + getTitle(context).replaceFirst(": ", ": "
                + ChatColor.AQUA) + ChatColor.GOLD + " -");
        for (int i = 1; i <= size; i++) {
            text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                    .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i)).append(" ")
                    .append(getAdditionalText(context, i));
        }
        return text.toString();
    }

    @Override
    public Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
        switch (input.intValue()) {
        case 1:
            return new ConditionNamePrompt(context);
        case 2:
            return new ConditionEntityPrompt(context);
        case 3:
            return new ConditionPlayerPrompt(context);
        case 4:
            return new ConditionWorldPrompt(context);
        case 5:
            return new ConditionPlaceholderListPrompt(context);
        case 6:
            final Boolean b = (Boolean) context.getSessionData(Key.C_FAIL_QUEST);
            if (Boolean.TRUE.equals(b)) {
                context.setSessionData(Key.C_FAIL_QUEST, false);
            } else {
                context.setSessionData(Key.C_FAIL_QUEST, true);
            }
            return new ConditionMainPrompt(context);
        case 7:
            if (context.getSessionData(Key.C_OLD_CONDITION) != null) {
                return new ConditionSavePrompt(context, (String) context.getSessionData(Key.C_OLD_CONDITION));
            } else {
                return new ConditionSavePrompt(context, null);
            }
        case 8:
            return new ConditionExitPrompt(context);
        default:
            return new ConditionMainPrompt(context);
        }
    }
    
    public class ConditionNamePrompt extends ConditionsEditorStringPrompt {

        public ConditionNamePrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("conditionEditorEnterName");
        }
        
        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final BukkitConditionsEditorPostOpenStringPromptEvent event
                    = new BukkitConditionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                for (final Condition c : plugin.getLoadedConditions()) {
                    if (c.getName().equalsIgnoreCase(input)) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("conditionEditorExists"));
                        return new ConditionNamePrompt(context);
                    }
                }
                final List<String> conditionNames = plugin.getConditionFactory().getNamesOfConditionsBeingEdited();
                if (conditionNames.contains(input)) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("questEditorBeingEdited"));
                    return new ConditionNamePrompt(context);
                }
                if (input.contains(",")) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("questEditorInvalidQuestName"));
                    return new ConditionNamePrompt(context);
                }
                conditionNames.remove((String) context.getSessionData(Key.C_NAME));
                context.setSessionData(Key.C_NAME, input);
                conditionNames.add(input);
                plugin.getConditionFactory().setNamesOfConditionsBeingEdited(conditionNames);
            }
            return new ConditionMainPrompt(context);
        }
    }
    
    public class ConditionPlaceholderListPrompt extends ConditionsEditorNumericPrompt {

        public ConditionPlaceholderListPrompt(final ConversationContext context) {
            super(context);
        }
        
        private final int size = 4;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("conditionEditorPlaceholderTitle");
        }
        
        @Override
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
            switch (number) {
                case 1:
                case 2:
                    return ChatColor.BLUE;
                case 3:
                    return ChatColor.RED;
                case 4:
                    return ChatColor.GREEN;
                default:
                    return null;
            }
        }
        
        @Override
        public String getSelectionText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                return ChatColor.YELLOW + BukkitLang.get("conditionEditorSetPlaceholderId");
            case 2:
                return ChatColor.YELLOW + BukkitLang.get("conditionEditorSetPlaceholderVal");
            case 3:
                return ChatColor.RED + BukkitLang.get("clear");
            case 4:
                return ChatColor.GREEN + BukkitLang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                if (plugin.getDependencies().getPlaceholderApi() != null) {
                    if (context.getSessionData(Key.C_WHILE_PLACEHOLDER_ID) == null) {
                        return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                    } else {
                        final List<String> id = (List<String>) context.getSessionData(Key.C_WHILE_PLACEHOLDER_ID);
                        final StringBuilder text = new StringBuilder();
                        if (id != null) {
                            for (final String i : id) {
                                text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).
                                        append(i);
                            }
                        }
                        return text.toString();
                    }
                } else {
                    return ChatColor.GRAY + "(" + BukkitLang.get("notInstalled") + ")";
                }
            case 2:
                if (plugin.getDependencies().getPlaceholderApi() != null) {
                    if (context.getSessionData(Key.C_WHILE_PLACEHOLDER_VAL) == null) {
                        return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                    } else {
                        final List<String> val = (List<String>) context.getSessionData(Key.C_WHILE_PLACEHOLDER_VAL);
                        final StringBuilder text = new StringBuilder();
                        if (val != null) {
                            for (final String i : val) {
                                text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA)
                                        .append(i);
                            }
                        }
                        return text.toString();
                    }
                } else {
                    return ChatColor.GRAY + "(" + BukkitLang.get("notInstalled") + ")";
                }
            case 3:
            case 4:
                return "";
            default:
                return null;
            }
        }
        
        @Override
        public @NotNull String getBasicPromptText(final @NotNull ConversationContext context) {
            final BukkitConditionsEditorPostOpenNumericPromptEvent event
                    = new BukkitConditionsEditorPostOpenNumericPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.AQUA + getTitle(context));
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i)).append(" ")
                        .append(getAdditionalText(context, i));
            }
            return text.toString();
        }
        
        @SuppressWarnings("unchecked")
        @Override
        protected Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
            switch(input.intValue()) {
            case 1:
                return new ConditionPlaceholderIdentifierPrompt(context);
            case 2:
                return new ConditionPlaceholderValuePrompt(context);
            case 3:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("conditionEditorPlaceholderCleared"));
                context.setSessionData(Key.C_WHILE_PLACEHOLDER_ID, null);
                context.setSessionData(Key.C_WHILE_PLACEHOLDER_VAL, null);
                return new ConditionPlaceholderListPrompt(context);
            case 4:
                final int one;
                final int two;
                if (context.getSessionData(Key.C_WHILE_PLACEHOLDER_ID) != null) {
                    one = ((List<String>) Objects.requireNonNull(context.getSessionData(Key.C_WHILE_PLACEHOLDER_ID)))
                            .size();
                } else {
                    one = 0;
                }
                if (context.getSessionData(Key.C_WHILE_PLACEHOLDER_VAL) != null) {
                    two = ((List<String>) Objects.requireNonNull(context.getSessionData(Key.C_WHILE_PLACEHOLDER_VAL)))
                            .size();
                } else {
                    two = 0;
                }
                if (one == two) {
                    return new ConditionMainPrompt(context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("listsNotSameSize"));
                    return new ConditionPlaceholderListPrompt(context);
                }
            default:
                return new ConditionPlaceholderListPrompt(context);
            }
        }
    }
    
    public class ConditionPlaceholderIdentifierPrompt extends ConditionsEditorStringPrompt {

        public ConditionPlaceholderIdentifierPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("conditionEditorEnterPlaceholderId");
        }
        
        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final BukkitConditionsEditorPostOpenStringPromptEvent event
                    = new BukkitConditionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
           if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final String[] args = input.split(" ");
                final List<String> identifiers = new LinkedList<>();
                for (String arg : args) { 
                    if (!arg.trim().startsWith("%")) {
                        arg = "%" + arg.trim();
                    }
                    if (!arg.endsWith("%")) {
                        arg = arg + "%";
                    }
                    identifiers.add(arg);
                }
                context.setSessionData(Key.C_WHILE_PLACEHOLDER_ID, identifiers);
            }
            return new ConditionPlaceholderListPrompt(context);
        }
    }
    
    public class ConditionPlaceholderValuePrompt extends ConditionsEditorStringPrompt {

        public ConditionPlaceholderValuePrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("conditionEditorEnterPlaceholderVal");
        }
        
        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final BukkitConditionsEditorPostOpenStringPromptEvent event
                    = new BukkitConditionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
           if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
               final String[] args = input.split(" ");
               final List<String> values = new LinkedList<>(Arrays.asList(args));
               context.setSessionData(Key.C_WHILE_PLACEHOLDER_VAL, values);
            }
            return new ConditionPlaceholderListPrompt(context);
        }
    }

    public class ConditionSavePrompt extends ConditionsEditorStringPrompt {

        String modName = null;
        LinkedList<String> modified = new LinkedList<>();

        public ConditionSavePrompt(final ConversationContext context, final String modifiedName) {
            super(context);
            if (modifiedName != null) {
                modName = modifiedName;
                for (final Quest q : plugin.getLoadedQuests()) {
                    for (final Stage s : q.getStages()) {
                        if (s.getCondition() != null && s.getCondition().getName() != null) {
                            if (s.getCondition().getName().equalsIgnoreCase(modifiedName)) {
                                modified.add(q.getName());
                                break;
                            }
                        }
                    }
                }
            }
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
            return BukkitLang.get("questEditorSave");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final BukkitConditionsEditorPostOpenStringPromptEvent event
                    = new BukkitConditionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder text = new StringBuilder(ChatColor.YELLOW + getQueryText(context));
            if (!modified.isEmpty()) {
                text.append("\n").append(ChatColor.RED).append(" ").append(BukkitLang.get("conditionEditorModifiedNote"));
                for (final String s : modified) {
                    text.append("\n").append(ChatColor.GRAY).append("    - ").append(ChatColor.DARK_RED).append(s);
                }
                text.append("\n").append(ChatColor.RED).append(" ").append(BukkitLang.get("conditionEditorForcedToQuit"));
            }
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
                if (plugin.hasLimitedAccess(context.getForWhom()) && !plugin.getConfigSettings().canTrialSave()) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("modeDeny")
                            .replace("<mode>", BukkitLang.get("trialMode")));
                    return new ConditionMainPrompt(context);
                }
                plugin.getConditionFactory().saveCondition(context);
                return Prompt.END_OF_CONVERSATION;
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(BukkitLang.get("noWord"))) {
                return new ConditionMainPrompt(context);
            } else {
                return new ConditionSavePrompt(context, modName);
            }
        }
    }
    
    public class ConditionExitPrompt extends ConditionsEditorStringPrompt {
        
        public ConditionExitPrompt(final ConversationContext context) {
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
            final BukkitConditionsEditorPostOpenStringPromptEvent event
                    = new BukkitConditionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.YELLOW + getQueryText(context));
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
                context.getForWhom().sendRawMessage(ChatColor.BOLD + "" + ChatColor.YELLOW + BukkitLang.get("exited"));
                plugin.getConditionFactory().clearData(context);
                return Prompt.END_OF_CONVERSATION;
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(BukkitLang.get("noWord"))) {
                return new ConditionMainPrompt(context);
            } else {
                return new ConditionExitPrompt(context);
            }
        }
    }
}
