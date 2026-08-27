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

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.conditions.Condition;
import me.pikamug.quests.convo.conditions.FabricConditionsEditorIntegerPrompt;
import me.pikamug.quests.convo.conditions.FabricConditionsEditorStringPrompt;
import me.pikamug.quests.convo.conditions.tasks.FabricConditionEntityPrompt;
import me.pikamug.quests.convo.conditions.tasks.FabricConditionPlayerPrompt;
import me.pikamug.quests.convo.conditions.tasks.FabricConditionWorldPrompt;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.Stage;
import me.pikamug.quests.util.FabricLang;
import me.pikamug.quests.util.FabricMiscUtil;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.SessionData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class FabricConditionMainPrompt extends FabricConditionsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final FabricQuestsPlugin plugin;

    public FabricConditionMainPrompt(final @NotNull UUID uuid) {
        super(uuid);
        this.uuid = uuid;
        this.plugin = FabricQuestsPlugin.getInstance();
    }

    private final int size = 8;

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public String getTitle() {
        return FabricLang.get("condition") + ": " + SessionData.get(uuid, Key.C_NAME);
    }

    @Override
    public ChatFormatting getNumberColor(final int number) {
        switch (number) {
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        case 6:
            return ChatFormatting.BLUE;
        case 7:
            return ChatFormatting.GREEN;
        case 8:
            return ChatFormatting.RED;
        default:
            return null;
        }
    }

    @Override
    public String getSelectionText(final int number) {
        switch (number) {
        case 1:
            return ChatFormatting.YELLOW + FabricLang.get("conditionEditorSetName");
        case 2:
            return ChatFormatting.GOLD + FabricLang.get("conditionEditorEntity");
        case 3:
            return ChatFormatting.GOLD + FabricLang.get("eventEditorPlayer");
        case 4:
            return ChatFormatting.GOLD + FabricLang.get("conditionEditorWorld");
        case 5:
            return ChatFormatting.YELLOW + FabricLang.get("conditionEditorCheckPlaceholder");
        case 6:
            return ChatFormatting.YELLOW + FabricLang.get("eventEditorFailQuest");
        case 7:
            return ChatFormatting.GREEN + FabricLang.get("save");
        case 8:
            return ChatFormatting.RED + FabricLang.get("exit");
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
                return ChatFormatting.GRAY + "(" + ChatFormatting.RED + FabricLang.get("false") + ChatFormatting.GRAY + ")";
            } else {
                final Boolean failOpt = (Boolean) SessionData.get(uuid, Key.C_FAIL_QUEST);
                return ChatFormatting.GRAY + "(" + (Boolean.TRUE.equals(failOpt) ? ChatFormatting.GREEN + FabricLang.get("true")
                        : ChatFormatting.RED + FabricLang.get("false")) + ChatFormatting.GRAY + ")";
            }
        default:
            return null;
        }
    }

    @Override
    public @NotNull String getPromptText() {
        final StringBuilder text = new StringBuilder(ChatFormatting.GOLD + "- " + getTitle().replaceFirst(": ", ": "
                + ChatFormatting.AQUA) + ChatFormatting.GOLD + " -");
        for (int i = 1; i <= size; i++) {
            text.append("\n").append(getNumberColor(i)).append(ChatFormatting.BOLD).append(i)
                    .append(ChatFormatting.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                    .append(getAdditionalText(i));
        }
        return sendClickableSelection(text.toString(), plugin.getQuester(uuid));
    }

    @Override
    public void acceptInput(final Number input) {
        switch (input.intValue()) {
        case 1:
            new ConditionNamePrompt(uuid).start();
            break;
        case 2:
            new FabricConditionEntityPrompt(uuid).start();
            break;
        case 3:
            new FabricConditionPlayerPrompt(uuid).start();
            break;
        case 4:
            new FabricConditionWorldPrompt(uuid).start();
            break;
        case 5:
            new ConditionPlaceholderListPrompt(uuid).start();
            break;
        case 6:
            final Boolean b = (Boolean) SessionData.get(uuid, Key.C_FAIL_QUEST);
            if (Boolean.TRUE.equals(b)) {
                SessionData.set(uuid, Key.C_FAIL_QUEST, false);
            } else {
                SessionData.set(uuid, Key.C_FAIL_QUEST, true);
            }
            new FabricConditionMainPrompt(uuid).start();
            break;
        case 7:
            if (SessionData.get(uuid, Key.C_OLD_CONDITION) != null) {
                new ConditionSavePrompt(uuid, (String) SessionData.get(uuid, Key.C_OLD_CONDITION)).start();
            } else {
                new ConditionSavePrompt(uuid, null).start();
            }
            break;
        case 8:
            new ConditionExitPrompt(uuid).start();
            break;
        default:
            new FabricConditionMainPrompt(uuid).start();
            break;
        }
    }

    public class ConditionNamePrompt extends FabricConditionsEditorStringPrompt {

        public ConditionNamePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("conditionEditorEnterName");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                for (final Condition c : plugin.getLoadedConditions()) {
                    if (c.getName().equalsIgnoreCase(input)) {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("conditionEditorExists")));
                        new ConditionNamePrompt(uuid).start();
                        return;
                    }
                }
                final List<String> conditionNames = plugin.getConditionFactory().getNamesOfConditionsBeingEdited();
                if (conditionNames.contains(input)) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("questEditorBeingEdited")));
                    new ConditionNamePrompt(uuid).start();
                    return;
                }
                if (input.contains(",")) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("questEditorInvalidQuestName")));
                    new ConditionNamePrompt(uuid).start();
                    return;
                }
                conditionNames.remove((String) SessionData.get(uuid, Key.C_NAME));
                SessionData.set(uuid, Key.C_NAME, input);
                conditionNames.add(input);
                plugin.getConditionFactory().setNamesOfConditionsBeingEdited(conditionNames);
            }
            new FabricConditionMainPrompt(uuid).start();
        }
    }

    public class ConditionPlaceholderListPrompt extends FabricConditionsEditorIntegerPrompt {

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
            return FabricLang.get("conditionEditorPlaceholderTitle");
        }

        @Override
        public ChatFormatting getNumberColor(final int number) {
            switch (number) {
                case 1:
                case 2:
                    return ChatFormatting.BLUE;
                case 3:
                    return ChatFormatting.RED;
                case 4:
                    return ChatFormatting.GREEN;
                default:
                    return null;
            }
        }

        @Override
        public String getSelectionText(final int number) {
            switch(number) {
            case 1:
                return ChatFormatting.YELLOW + FabricLang.get("conditionEditorSetPlaceholderId");
            case 2:
                return ChatFormatting.YELLOW + FabricLang.get("conditionEditorSetPlaceholderVal");
            case 3:
                return ChatFormatting.RED + FabricLang.get("clear");
            case 4:
                return ChatFormatting.GREEN + FabricLang.get("done");
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
                        return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                    } else {
                        final List<String> id = (List<String>) SessionData.get(uuid, Key.C_WHILE_PLACEHOLDER_ID);
                        final StringBuilder text = new StringBuilder();
                        if (id != null) {
                            for (final String i : id) {
                                text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA).
                                        append(i);
                            }
                        }
                        return text.toString();
                    }
                } else {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("notInstalled") + ")";
                }
            case 2:
                if (plugin.getDependencies().getPlaceholderApi() != null) {
                    if (SessionData.get(uuid, Key.C_WHILE_PLACEHOLDER_VAL) == null) {
                        return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                    } else {
                        final List<String> val = (List<String>) SessionData.get(uuid, Key.C_WHILE_PLACEHOLDER_VAL);
                        final StringBuilder text = new StringBuilder();
                        if (val != null) {
                            for (final String i : val) {
                                text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA)
                                        .append(i);
                            }
                        }
                        return text.toString();
                    }
                } else {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("notInstalled") + ")";
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
            final StringBuilder text = new StringBuilder(ChatFormatting.AQUA + getTitle());
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatFormatting.BOLD).append(i)
                        .append(ChatFormatting.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                        .append(getAdditionalText(i));
            }
            return sendClickableSelection(text.toString(), plugin.getQuester(uuid));
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final Number input) {
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            switch(input.intValue()) {
            case 1:
                new ConditionPlaceholderIdentifierPrompt(uuid).start();
                break;
            case 2:
                new ConditionPlaceholderValuePrompt(uuid).start();
                break;
            case 3:
                sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("conditionEditorPlaceholderCleared")));
                SessionData.set(uuid, Key.C_WHILE_PLACEHOLDER_ID, null);
                SessionData.set(uuid, Key.C_WHILE_PLACEHOLDER_VAL, null);
                new ConditionPlaceholderListPrompt(uuid).start();
                break;
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
                    new FabricConditionMainPrompt(uuid).start();
                } else {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("listsNotSameSize")));
                    new ConditionPlaceholderListPrompt(uuid).start();
                }
                break;
            default:
                new ConditionPlaceholderListPrompt(uuid).start();
                break;
            }
        }
    }

    public class ConditionPlaceholderIdentifierPrompt extends FabricConditionsEditorStringPrompt {

        public ConditionPlaceholderIdentifierPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("conditionEditorEnterPlaceholderId");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
           if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
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

    public class ConditionPlaceholderValuePrompt extends FabricConditionsEditorStringPrompt {

        public ConditionPlaceholderValuePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("conditionEditorEnterPlaceholderVal");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
           if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
               final String[] args = input.split(" ");
               final List<String> values = new LinkedList<>(Arrays.asList(args));
               SessionData.set(uuid, Key.C_WHILE_PLACEHOLDER_VAL, values);
            }
            new ConditionPlaceholderListPrompt(uuid).start();
        }
    }

    public class ConditionSavePrompt extends FabricConditionsEditorStringPrompt {

        String modName = null;
        LinkedList<String> modified = new LinkedList<>();

        public ConditionSavePrompt(final @NotNull UUID uuid, final String modifiedName) {
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
        public ChatFormatting getNumberColor(final int number) {
            switch (number) {
            case 1:
                return ChatFormatting.GREEN;
            case 2:
                return ChatFormatting.RED;
            default:
                return null;
            }
        }

        @SuppressWarnings("unused")
        public String getSelectionText(final int number) {
            switch (number) {
            case 1:
                return ChatFormatting.GREEN + FabricLang.get("yesWord");
            case 2:
                return ChatFormatting.RED + FabricLang.get("noWord");
            default:
                return null;
            }
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("questEditorSave");
        }

        @Override
        public @NotNull String getPromptText() {
            final StringBuilder text = new StringBuilder(ChatFormatting.YELLOW + getQueryText());
            if (!modified.isEmpty()) {
                text.append("\n").append(ChatFormatting.RED).append(" ").append(FabricLang.get("conditionEditorModifiedNote"));
                for (final String s : modified) {
                    text.append("\n").append(ChatFormatting.GRAY).append("    - ").append(ChatFormatting.DARK_RED).append(s);
                }
                text.append("\n").append(ChatFormatting.RED).append(" ").append(FabricLang.get("conditionEditorForcedToQuit"));
            }
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatFormatting.BOLD).append(i)
                        .append(ChatFormatting.RESET).append(" - ").append(getSelectionText(i));
            }
            return sendClickableSelection(text.toString(), plugin.getQuester(uuid));
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(FabricLang.get("yesWord"))) {
                if (plugin.hasLimitedAccess(uuid) && !plugin.getConfigSettings().canTrialSave()) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("modeDeny")
                            .replace("<mode>", FabricLang.get("trialMode"))));
                    new FabricConditionMainPrompt(uuid).start();
                    return;
                }
                plugin.getConditionFactory().saveCondition(uuid);
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(FabricLang.get("noWord"))) {
                new FabricConditionMainPrompt(uuid).start();
            } else {
                new ConditionSavePrompt(uuid, modName).start();
            }
        }
    }

    public class ConditionExitPrompt extends FabricConditionsEditorStringPrompt {

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
        public ChatFormatting getNumberColor(final int number) {
            switch (number) {
            case 1:
                return ChatFormatting.GREEN;
            case 2:
                return ChatFormatting.RED;
            default:
                return null;
            }
        }

        @SuppressWarnings("unused")
        public String getSelectionText(final int number) {
            switch (number) {
            case 1:
                return ChatFormatting.GREEN + FabricLang.get("yesWord");
            case 2:
                return ChatFormatting.RED + FabricLang.get("noWord");
            default:
                return null;
            }
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("confirmDelete");
        }

        @Override
        public @NotNull String getPromptText() {
            final StringBuilder text = new StringBuilder(ChatFormatting.YELLOW + getQueryText());
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatFormatting.BOLD).append(i)
                        .append(ChatFormatting.RESET).append(" - ").append(getSelectionText(i));
            }
            return sendClickableSelection(text.toString(), plugin.getQuester(uuid));
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(FabricLang.get("yesWord"))) {
                sender.sendSystemMessage(Component.literal(ChatFormatting.BOLD + "" + ChatFormatting.YELLOW + FabricLang.get("exited")));
                plugin.getConditionFactory().clearData(uuid);
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(FabricLang.get("noWord"))) {
                new FabricConditionMainPrompt(uuid).start();
            } else {
                new ConditionExitPrompt(uuid).start();
            }
        }
    }
}
