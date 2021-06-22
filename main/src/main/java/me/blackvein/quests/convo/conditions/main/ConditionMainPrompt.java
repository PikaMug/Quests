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

package me.blackvein.quests.convo.conditions.main;

import me.blackvein.quests.Quest;
import me.blackvein.quests.Quests;
import me.blackvein.quests.Stage;
import me.blackvein.quests.conditions.Condition;
import me.blackvein.quests.convo.conditions.ConditionsEditorNumericPrompt;
import me.blackvein.quests.convo.conditions.ConditionsEditorStringPrompt;
import me.blackvein.quests.convo.conditions.tasks.EntityPrompt;
import me.blackvein.quests.convo.conditions.tasks.PlayerPrompt;
import me.blackvein.quests.convo.conditions.tasks.WorldPrompt;
import me.blackvein.quests.events.editor.conditions.ConditionsEditorPostOpenNumericPromptEvent;
import me.blackvein.quests.events.editor.conditions.ConditionsEditorPostOpenStringPromptEvent;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.Lang;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ConditionMainPrompt extends ConditionsEditorNumericPrompt {
    
    private final Quests plugin;
    
    public ConditionMainPrompt(final ConversationContext context) {
        super(context);
        this.plugin = (Quests)context.getPlugin();
    }

    private final int size = 8;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle(final ConversationContext context) {
        return Lang.get("condition") + ": " + context.getSessionData(CK.C_NAME);
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
            return ChatColor.YELLOW + Lang.get("conditionEditorSetName");
        case 2:
            return ChatColor.GOLD + Lang.get("conditionEditorEntity");
        case 3:
            return ChatColor.GOLD + Lang.get("eventEditorPlayer");
        case 4:
            return ChatColor.GOLD + Lang.get("conditionEditorWorld");
        case 5:
            return ChatColor.YELLOW + Lang.get("conditionEditorCheckPlaceholder");
        case 6:
            return ChatColor.YELLOW + Lang.get("eventEditorFailQuest") + ":";
        case 7:
            return ChatColor.GREEN + Lang.get("save");
        case 8:
            return ChatColor.RED + Lang.get("exit");
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
            return "";
        case 6:
            if (context.getSessionData(CK.C_FAIL_QUEST) == null) {
                context.setSessionData(CK.C_FAIL_QUEST, Lang.get("noWord"));
            }
            return "" + ChatColor.AQUA + context.getSessionData(CK.C_FAIL_QUEST);
        case 7:
        case 8:
            return "";
        default:
            return null;
        }
    }

    @Override
    public String getPromptText(final ConversationContext context) {
        final ConditionsEditorPostOpenNumericPromptEvent event = new ConditionsEditorPostOpenNumericPromptEvent(context, this);
        plugin.getServer().getPluginManager().callEvent(event);
        
        String text = ChatColor.GOLD + "- " + getTitle(context).replaceFirst(": ", ": " + ChatColor.AQUA) 
                + ChatColor.GOLD + " -";
        for (int i = 1; i <= size; i++) {
            text += "\n" + getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                    + getSelectionText(context, i) + " " + getAdditionalText(context, i);
        }
        return text;
    }

    @Override
    public Prompt acceptValidatedInput(final ConversationContext context, final Number input) {
        switch (input.intValue()) {
        case 1:
            return new ConditionNamePrompt(context);
        case 2:
            return new EntityPrompt(context);
        case 3:
            return new PlayerPrompt(context);
        case 4:
            return new WorldPrompt(context);
        case 5:
            return new ConditionPlaceholderListPrompt(context);
        case 6:
            final String s = (String) context.getSessionData(CK.C_FAIL_QUEST);
            if (s.equalsIgnoreCase(Lang.get("yesWord"))) {
                context.setSessionData(CK.C_FAIL_QUEST, Lang.get("noWord"));
            } else {
                context.setSessionData(CK.C_FAIL_QUEST, Lang.get("yesWord"));
            }
            return new ConditionMainPrompt(context);
        case 7:
            if (context.getSessionData(CK.C_OLD_CONDITION) != null) {
                return new ConditionSavePrompt(context, (String) context.getSessionData(CK.C_OLD_CONDITION));
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
            return Lang.get("conditionEditorEnterName");
        }
        
        @Override
        public String getPromptText(final ConversationContext context) {
            final ConditionsEditorPostOpenStringPromptEvent event 
                    = new ConditionsEditorPostOpenStringPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                for (final Condition c : plugin.getLoadedConditions()) {
                    if (c.getName().equalsIgnoreCase(input)) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("conditionEditorExists"));
                        return new ConditionNamePrompt(context);
                    }
                }
                final List<String> actionNames = plugin.getConditionFactory().getNamesOfConditionsBeingEdited();
                if (actionNames.contains(input)) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorBeingEdited"));
                    return new ConditionNamePrompt(context);
                }
                if (input.contains(",")) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorInvalidQuestName"));
                    return new ConditionNamePrompt(context);
                }
                actionNames.remove(context.getSessionData(CK.C_NAME));
                context.setSessionData(CK.C_NAME, input);
                actionNames.add(input);
                plugin.getConditionFactory().setNamesOfConditionsBeingEdited(actionNames);
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
            return Lang.get("conditionEditorPlaceholderTitle");
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
                return ChatColor.YELLOW + Lang.get("conditionEditorSetPlaceholderId");
            case 2:
                return ChatColor.YELLOW + Lang.get("conditionEditorSetPlaceholderVal");
            case 3:
                return ChatColor.RED + Lang.get("clear");
            case 4:
                return ChatColor.GREEN + Lang.get("done");
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
                    if (context.getSessionData(CK.C_WHILE_PLACEHOLDER_ID) == null) {
                        return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                    } else {
                        String text = "\n";
                        for (final String i : (List<String>) context.getSessionData(CK.C_WHILE_PLACEHOLDER_ID)) {
                            text += ChatColor.GRAY + "     - " + ChatColor.AQUA + i + "\n";
                        }
                        return text;
                    }
                } else {
                    return ChatColor.GRAY + "(" + Lang.get("notInstalled") + ")";
                }
            case 2:
                if (plugin.getDependencies().getPlaceholderApi() != null) {
                    if (context.getSessionData(CK.C_WHILE_PLACEHOLDER_VAL) == null) {
                        return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                    } else {
                        String text = "\n";
                        for (final String i : (List<String>) context.getSessionData(CK.C_WHILE_PLACEHOLDER_VAL)) {
                            text += ChatColor.GRAY + "     - " + ChatColor.AQUA + i + "\n";
                        }
                        return text;
                    }
                } else {
                    return ChatColor.GRAY + "(" + Lang.get("notInstalled") + ")";
                }
            case 3:
            case 4:
                return "";
            default:
                return null;
            }
        }
        
        @Override
        public String getPromptText(final ConversationContext context) {
            final ConditionsEditorPostOpenNumericPromptEvent event 
                    = new ConditionsEditorPostOpenNumericPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            String text = ChatColor.AQUA + getTitle(context) + "\n";
            for (int i = 1; i <= size; i++) {
                text += getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                        + getSelectionText(context, i) + " " + getAdditionalText(context, i) + "\n";
            }
            return text;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        protected Prompt acceptValidatedInput(final ConversationContext context, final Number input) {
            switch(input.intValue()) {
            case 1:
                return new ConditionPlaceholderIdentifierPrompt(context);
            case 2:
                return new ConditionPlaceholderValuePrompt(context);
            case 3:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("conditionEditorPlaceholderCleared"));
                context.setSessionData(CK.C_WHILE_PLACEHOLDER_ID, null);
                context.setSessionData(CK.C_WHILE_PLACEHOLDER_VAL, null);
                return new ConditionPlaceholderListPrompt(context);
            case 4:
                int one;
                int two;
                if (context.getSessionData(CK.C_WHILE_PLACEHOLDER_ID) != null) {
                    one = ((List<String>) context.getSessionData(CK.C_WHILE_PLACEHOLDER_ID)).size();
                } else {
                    one = 0;
                }
                if (context.getSessionData(CK.C_WHILE_PLACEHOLDER_VAL) != null) {
                    two = ((List<String>) context.getSessionData(CK.C_WHILE_PLACEHOLDER_VAL)).size();
                } else {
                    two = 0;
                }
                if (one == two) {
                    return new ConditionMainPrompt(context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
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
            return Lang.get("conditionEditorEnterPlaceholderId");
        }
        
        @Override
        public String getPromptText(final ConversationContext context) {
            final ConditionsEditorPostOpenStringPromptEvent event 
                    = new ConditionsEditorPostOpenStringPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
           if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                final String[] args = input.split(" ");
                final List<String> identifiers = new LinkedList<String>();
                for (String arg : args) { 
                    if (!arg.trim().startsWith("%")) {
                        arg = "%" + arg.trim();
                    }
                    if (!arg.endsWith("%")) {
                        arg = arg + "%";
                    }
                    identifiers.add(arg);
                }
                context.setSessionData(CK.C_WHILE_PLACEHOLDER_ID, identifiers);
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
            return Lang.get("conditionEditorEnterPlaceholderVal");
        }
        
        @Override
        public String getPromptText(final ConversationContext context) {
            final ConditionsEditorPostOpenStringPromptEvent event 
                    = new ConditionsEditorPostOpenStringPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
           if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                final String[] args = input.split(" ");
                final List<String> values = new LinkedList<String>();
                values.addAll(Arrays.asList(args));
                context.setSessionData(CK.C_WHILE_PLACEHOLDER_VAL, values);
            }
            return new ConditionPlaceholderListPrompt(context);
        }
    }

    public class ConditionSavePrompt extends ConditionsEditorStringPrompt {

        String modName = null;
        LinkedList<String> modified = new LinkedList<String>();

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
        
        public String getSelectionText(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                return ChatColor.GREEN + Lang.get("yesWord");
            case 2:
                return ChatColor.RED + Lang.get("noWord");
            default:
                return null;
            }
        }
        
        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("questEditorSave");
        }

        @Override
        public String getPromptText(final ConversationContext context) {
            final ConditionsEditorPostOpenStringPromptEvent event 
                    = new ConditionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);

            String text = ChatColor.YELLOW + getQueryText(context);
            if (modified.isEmpty() == false) {
                text += "\n" + ChatColor.RED + Lang.get("conditionEditorModifiedNote");
                for (final String s : modified) {
                    text += "\n" + ChatColor.GRAY + "    - " + ChatColor.DARK_RED + s;
                }
                text += "\n" + ChatColor.RED + Lang.get("conditionEditorForcedToQuit");
            }
            for (int i = 1; i <= size; i++) {
                text += "\n" + getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                        + getSelectionText(context, i);
            }
            return text;
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(Lang.get("yesWord"))) {
                if (plugin.hasLimitedAccess(context.getForWhom()) && !plugin.getSettings().canTrialSave()) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("noPermission"));
                    return new ConditionMainPrompt(context);
                }
                plugin.getConditionFactory().saveCondition(context);
                return Prompt.END_OF_CONVERSATION;
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(Lang.get("noWord"))) {
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
        
        public String getSelectionText(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                return ChatColor.GREEN + Lang.get("yesWord");
            case 2:
                return ChatColor.RED + Lang.get("noWord");
            default:
                return null;
            }
        }
        
        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("confirmDelete");
        }

        @Override
        public String getPromptText(final ConversationContext context) {
            final ConditionsEditorPostOpenStringPromptEvent event 
                    = new ConditionsEditorPostOpenStringPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            String text = ChatColor.YELLOW + getQueryText(context);
            for (int i = 1; i <= size; i++) {
                text += "\n" + getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                        + getSelectionText(context, i);
            }
            return text;
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(Lang.get("yesWord"))) {
                context.getForWhom().sendRawMessage(ChatColor.BOLD + "" + ChatColor.YELLOW + Lang.get("exited"));
                plugin.getConditionFactory().clearData(context);
                return Prompt.END_OF_CONVERSATION;
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(Lang.get("noWord"))) {
                return new ConditionMainPrompt(context);
            } else {
                return new ConditionExitPrompt(context);
            }
        }
    }
}
