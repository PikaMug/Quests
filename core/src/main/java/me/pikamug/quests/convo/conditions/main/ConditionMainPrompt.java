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

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.conditions.Condition;
import me.pikamug.quests.convo.QuestsNumericPrompt;
import me.pikamug.quests.convo.conditions.ConditionsEditorNumericPrompt;
import me.pikamug.quests.convo.conditions.ConditionsEditorStringPrompt;
import me.pikamug.quests.convo.conditions.tasks.ConditionEntityPrompt;
import me.pikamug.quests.convo.conditions.tasks.ConditionPlayerPrompt;
import me.pikamug.quests.convo.conditions.tasks.ConditionWorldPrompt;
import me.pikamug.quests.events.editor.conditions.BukkitConditionsEditorPostOpenNumericPromptEvent;
import me.pikamug.quests.events.editor.conditions.BukkitConditionsEditorPostOpenStringPromptEvent;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.Stage;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.SessionData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ConditionMainPrompt extends ConditionsEditorNumericPrompt {

    private final @NotNull UUID uuid;
    private final BukkitQuestsPlugin plugin;
    
    public ConditionMainPrompt(final @NotNull UUID uuid) {
        super(uuid);
        this.uuid = uuid;
        this.plugin = BukkitQuestsPlugin.getInstance();
    }

    private final int size = 8;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle() {
        return BukkitLang.get("condition") + ": " + SessionData.get(uuid, Key.C_NAME);
    }
    
    @Override
    public ChatColor getNumberColor(final int number) {
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
    public String getSelectionText(final int number) {
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
    public String getAdditionalText(final int number) {
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
            if (SessionData.get(uuid, Key.C_FAIL_QUEST) == null) {
                return ChatColor.GRAY + "(" + ChatColor.RED + BukkitLang.get("false") + ChatColor.GRAY + ")";
            } else {
                final Boolean failOpt = (Boolean) SessionData.get(uuid, Key.C_FAIL_QUEST);
                return ChatColor.GRAY + "(" + (Boolean.TRUE.equals(failOpt) ? ChatColor.GREEN + BukkitLang.get("true")
                        : ChatColor.RED + BukkitLang.get("false")) + ChatColor.GRAY + ")";
            }
        default:
            return null;
        }
    }

    @Override
    public @NotNull String getPromptText() {
        final BukkitConditionsEditorPostOpenNumericPromptEvent event
                = new BukkitConditionsEditorPostOpenNumericPromptEvent(uuid, this);
        plugin.getServer().getPluginManager().callEvent(event);
        
        final StringBuilder text = new StringBuilder(ChatColor.GOLD + "- " + getTitle().replaceFirst(": ", ": "
                + ChatColor.AQUA) + ChatColor.GOLD + " -");
        for (int i = 1; i <= size; i++) {
            text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                    .append(ChatColor.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                    .append(getAdditionalText(i));
        }
        return text.toString();
    }

    @Override
    public void acceptInput(final Number input) {
        switch (input.intValue()) {
        case 1:
            new ConditionNamePrompt(uuid).start();
        case 2:
            new ConditionEntityPrompt(uuid).start();
        case 3:
            new ConditionPlayerPrompt(uuid).start();
        case 4:
            new ConditionWorldPrompt(uuid).start();
        case 5:
            new ConditionPlaceholderListPrompt(uuid).start();
        case 6:
            final Boolean b = (Boolean) SessionData.get(uuid, Key.C_FAIL_QUEST);
            if (Boolean.TRUE.equals(b)) {
                SessionData.set(uuid, Key.C_FAIL_QUEST, false);
            } else {
                SessionData.set(uuid, Key.C_FAIL_QUEST, true);
            }
            new ConditionMainPrompt(uuid).start();
        case 7:
            if (SessionData.get(uuid, Key.C_OLD_CONDITION) != null) {
                new ConditionSavePrompt(uuid, (String) SessionData.get(uuid, Key.C_OLD_CONDITION));
            } else {
                new ConditionSavePrompt(uuid, null).start();
            }
        case 8:
            new ConditionExitPrompt(uuid).start();
        default:
            new ConditionMainPrompt(uuid).start();
        }
    }
    
    public class ConditionNamePrompt extends ConditionsEditorStringPrompt {

        public ConditionNamePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("conditionEditorEnterName");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitConditionsEditorPostOpenStringPromptEvent event
                    = new BukkitConditionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = Bukkit.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                for (final Condition c : plugin.getLoadedConditions()) {
                    if (c.getName().equalsIgnoreCase(input)) {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("conditionEditorExists"));
                        new ConditionNamePrompt(uuid).start();
                    }
                }
                final List<String> conditionNames = plugin.getConditionFactory().getNamesOfConditionsBeingEdited();
                if (conditionNames.contains(input)) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("questEditorBeingEdited"));
                    new ConditionNamePrompt(uuid).start();
                }
                if (input.contains(",")) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("questEditorInvalidQuestName"));
                    new ConditionNamePrompt(uuid).start();
                }
                conditionNames.remove((String) SessionData.get(uuid, Key.C_NAME));
                SessionData.set(uuid, Key.C_NAME, input);
                conditionNames.add(input);
                plugin.getConditionFactory().setNamesOfConditionsBeingEdited(conditionNames);
            }
            new ConditionMainPrompt(uuid).start();
        }
    }
    
    public class ConditionPlaceholderListPrompt extends ConditionsEditorNumericPrompt {

        public ConditionPlaceholderListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        private final int size = 4;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle() {
            return BukkitLang.get("conditionEditorPlaceholderTitle");
        }
        
        @Override
        public ChatColor getNumberColor(final int number) {
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
        public String getSelectionText(final int number) {
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
        public String getAdditionalText(final int number) {
            switch(number) {
            case 1:
                if (plugin.getDependencies().getPlaceholderApi() != null) {
                    if (SessionData.get(uuid, Key.C_WHILE_PLACEHOLDER_ID) == null) {
                        return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                    } else {
                        final List<String> id = (List<String>) SessionData.get(uuid, Key.C_WHILE_PLACEHOLDER_ID);
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
                    if (SessionData.get(uuid, Key.C_WHILE_PLACEHOLDER_VAL) == null) {
                        return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                    } else {
                        final List<String> val = (List<String>) SessionData.get(uuid, Key.C_WHILE_PLACEHOLDER_VAL);
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
        public @NotNull String getPromptText() {
            final BukkitConditionsEditorPostOpenNumericPromptEvent event
                    = new BukkitConditionsEditorPostOpenNumericPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.AQUA + getTitle());
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                        .append(getAdditionalText(i));
            }
            return text.toString();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final Number input) {
            final CommandSender sender = Bukkit.getEntity(uuid);
            switch(input.intValue()) {
            case 1:
                new ConditionPlaceholderIdentifierPrompt(uuid).start();
            case 2:
                new ConditionPlaceholderValuePrompt(uuid).start();
            case 3:
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("conditionEditorPlaceholderCleared"));
                SessionData.set(uuid, Key.C_WHILE_PLACEHOLDER_ID, null);
                SessionData.set(uuid, Key.C_WHILE_PLACEHOLDER_VAL, null);
                new ConditionPlaceholderListPrompt(uuid).start();
            case 4:
                final int one;
                final int two;
                if (SessionData.get(uuid, Key.C_WHILE_PLACEHOLDER_ID) != null) {
                    one = ((List<String>) Objects.requireNonNull(SessionData.get(uuid, Key.C_WHILE_PLACEHOLDER_ID)))
                            .size();
                } else {
                    one = 0;
                }
                if (SessionData.get(uuid, Key.C_WHILE_PLACEHOLDER_VAL) != null) {
                    two = ((List<String>) Objects.requireNonNull(SessionData.get(uuid, Key.C_WHILE_PLACEHOLDER_VAL)))
                            .size();
                } else {
                    two = 0;
                }
                if (one == two) {
                    new ConditionMainPrompt(uuid).start();
                } else {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("listsNotSameSize"));
                    new ConditionPlaceholderListPrompt(uuid).start();
                }
            default:
                new ConditionPlaceholderListPrompt(uuid).start();
            }
        }
    }
    
    public class ConditionPlaceholderIdentifierPrompt extends ConditionsEditorStringPrompt {

        public ConditionPlaceholderIdentifierPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("conditionEditorEnterPlaceholderId");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitConditionsEditorPostOpenStringPromptEvent event
                    = new BukkitConditionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
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
                SessionData.set(uuid, Key.C_WHILE_PLACEHOLDER_ID, identifiers);
            }
            new ConditionPlaceholderListPrompt(uuid).start();
        }
    }
    
    public class ConditionPlaceholderValuePrompt extends ConditionsEditorStringPrompt {

        public ConditionPlaceholderValuePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("conditionEditorEnterPlaceholderVal");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitConditionsEditorPostOpenStringPromptEvent event
                    = new BukkitConditionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
           if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
               final String[] args = input.split(" ");
               final List<String> values = new LinkedList<>(Arrays.asList(args));
               SessionData.set(uuid, Key.C_WHILE_PLACEHOLDER_VAL, values);
            }
            new ConditionPlaceholderListPrompt(uuid).start();
        }
    }

    public class ConditionSavePrompt extends ConditionsEditorStringPrompt {

        String modName = null;
        LinkedList<String> modified = new LinkedList<>();

        public ConditionSavePrompt(final UUID uuid, final String modifiedName) {
            super(uuid);
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
        public String getTitle() {
            return null;
        }

        @SuppressWarnings("unused")
        public ChatColor getNumberColor(final int number) {
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
        public String getSelectionText(final int number) {
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
        public String getQueryText() {
            return BukkitLang.get("questEditorSave");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitConditionsEditorPostOpenStringPromptEvent event
                    = new BukkitConditionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder text = new StringBuilder(ChatColor.YELLOW + getQueryText());
            if (!modified.isEmpty()) {
                text.append("\n").append(ChatColor.RED).append(" ").append(BukkitLang.get("conditionEditorModifiedNote"));
                for (final String s : modified) {
                    text.append("\n").append(ChatColor.GRAY).append("    - ").append(ChatColor.DARK_RED).append(s);
                }
                text.append("\n").append(ChatColor.RED).append(" ").append(BukkitLang.get("conditionEditorForcedToQuit"));
            }
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(i));
            }
            return QuestsNumericPrompt.sendClickableSelection(text.toString(), plugin.getQuester(uuid));
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = Bukkit.getEntity(uuid);
            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(BukkitLang.get("yesWord"))) {
                if (plugin.hasLimitedAccess(uuid) && !plugin.getConfigSettings().canTrialSave()) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("modeDeny")
                            .replace("<mode>", BukkitLang.get("trialMode")));
                    new ConditionMainPrompt(uuid).start();
                }
                plugin.getConditionFactory().saveCondition(uuid);
                //return Prompt.END_OF_CONVERSATION;
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(BukkitLang.get("noWord"))) {
                new ConditionMainPrompt(uuid).start();
            } else {
                new ConditionSavePrompt(uuid, modName);
            }
        }
    }
    
    public class ConditionExitPrompt extends ConditionsEditorStringPrompt {
        
        public ConditionExitPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 2;
        
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle() {
            return null;
        }

        @SuppressWarnings("unused")
        public ChatColor getNumberColor(final int number) {
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
        public String getSelectionText(final int number) {
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
        public String getQueryText() {
            return BukkitLang.get("confirmDelete");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitConditionsEditorPostOpenStringPromptEvent event
                    = new BukkitConditionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.YELLOW + getQueryText());
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(i));
            }
            return QuestsNumericPrompt.sendClickableSelection(text.toString(), plugin.getQuester(uuid));
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = Bukkit.getEntity(uuid);
            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(BukkitLang.get("yesWord"))) {
                sender.sendMessage(ChatColor.BOLD + "" + ChatColor.YELLOW + BukkitLang.get("exited"));
                plugin.getConditionFactory().clearData(uuid);
                //return Prompt.END_OF_CONVERSATION;
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(BukkitLang.get("noWord"))) {
                new ConditionMainPrompt(uuid).start();
            } else {
                new ConditionExitPrompt(uuid).start();
            }
        }
    }
}
