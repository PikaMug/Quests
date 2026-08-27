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

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.conditions.Condition;
import me.pikamug.quests.convo.conditions.FabricConditionsEditorIntegerPrompt;
import me.pikamug.quests.convo.conditions.FabricConditionsEditorStringPrompt;
import me.pikamug.quests.convo.conditions.main.FabricConditionMainPrompt;
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

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class FabricConditionMenuPrompt extends FabricConditionsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final FabricQuestsPlugin plugin;

    public FabricConditionMenuPrompt(final @NotNull UUID uuid) {
        super(uuid);
        this.uuid = uuid;
        this.plugin = FabricQuestsPlugin.getInstance();
    }

    private final int size = 4;

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public String getTitle() {
        final String title = FabricLang.get("conditionEditorTitle");
        return title + (plugin.hasLimitedAccess(uuid) ? ChatFormatting.RED + " (" + FabricLang.get("trialMode")
                + ")" : "");
    }

    @Override
    public ChatFormatting getNumberColor(final int number) {
        switch (number) {
        case 1:
        case 2:
        case 3:
            return ChatFormatting.BLUE;
        case 4:
            return ChatFormatting.RED;
        default:
            return null;
        }
    }

    @Override
    public String getSelectionText(final int number) {
        switch (number) {
        case 1:
            return ChatFormatting.YELLOW + FabricLang.get("conditionEditorCreate");
        case 2:
            return ChatFormatting.YELLOW + FabricLang.get("conditionEditorEdit");
        case 3:
            return ChatFormatting.YELLOW + FabricLang.get("conditionEditorDelete");
        case 4:
            return ChatFormatting.RED + FabricLang.get("exit");
        default:
            return null;
        }
    }

    @Override
    public String getAdditionalText(final int number) {
        return null;
    }

    @Override
    public @NotNull String getPromptText() {
        final StringBuilder text = new StringBuilder(ChatFormatting.GOLD + getTitle());
        for (int i = 1; i <= size; i++) {
            text.append("\n").append(getNumberColor(i)).append(ChatFormatting.BOLD).append(i)
                    .append(ChatFormatting.RESET).append(" - ").append(getSelectionText(i));
        }
        return sendClickableSelection(text.toString(), plugin.getQuester(uuid));
    }

    @Override
    public void acceptInput(final Number input) {
        final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
        if (sender == null) {
            return;
        }
        switch (input.intValue()) {
        case 1:
            if (sender.hasPermissions(2)) {
                SessionData.set(uuid, Key.C_OLD_CONDITION, "");
                new ConditionSelectCreatePrompt(uuid).start();
            } else {
                sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("noPermission")));
                new FabricConditionMenuPrompt(uuid).start();
            }
            break;
        case 2:
            if (sender.hasPermissions(2)) {
                if (plugin.getLoadedConditions().isEmpty()) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW
                            + FabricLang.get("conditionEditorNoneToEdit")));
                    new FabricConditionMenuPrompt(uuid).start();
                } else {
                    new ConditionSelectEditPrompt(uuid).start();
                }
            } else {
                sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("noPermission")));
                new FabricConditionMenuPrompt(uuid).start();
            }
            break;
        case 3:
            if (sender.hasPermissions(2)) {
                if (plugin.getLoadedConditions().isEmpty()) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW
                            + FabricLang.get("conditionEditorNoneToDelete")));
                    new FabricConditionMenuPrompt(uuid).start();
                } else {
                    new ConditionSelectDeletePrompt(uuid).start();
                }
            } else {
                sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("noPermission")));
                new FabricConditionMenuPrompt(uuid).start();
            }
            break;
        case 4:
            sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("exited")));
            break;
        default:
            new FabricConditionMenuPrompt(uuid).start();
            break;
        }
    }

    public class ConditionSelectCreatePrompt extends FabricConditionsEditorStringPrompt {

        public ConditionSelectCreatePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return FabricLang.get("conditionCreateTitle");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("conditionEditorEnterName");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.GOLD + getTitle() + "\n" + ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(String input) {
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (input == null) {
                sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("itemCreateInvalidInput")));
                new ConditionSelectCreatePrompt(uuid).start();
                return;
            }
            if (sender == null) {
                return;
            }
            input = input.trim();
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                for (final Condition c : plugin.getLoadedConditions()) {
                    if (c.getName().equalsIgnoreCase(input)) {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("conditionEditorExists")));
                        new ConditionSelectCreatePrompt(uuid).start();
                        return;
                    }
                }
                final List<String> actionNames = plugin.getConditionFactory().getNamesOfConditionsBeingEdited();
                if (actionNames.contains(input)) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("questEditorBeingEdited")));
                    new ConditionSelectCreatePrompt(uuid).start();
                    return;
                }
                if (input.contains(".") || input.contains(",")) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("questEditorInvalidQuestName")));
                    new ConditionSelectCreatePrompt(uuid).start();
                    return;
                }
                if (input.isEmpty()) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("itemCreateInvalidInput")));
                    new ConditionSelectCreatePrompt(uuid).start();
                    return;
                }
                SessionData.set(uuid, Key.C_NAME, input);
                actionNames.add(input);
                plugin.getConditionFactory().setNamesOfConditionsBeingEdited(actionNames);
                new FabricConditionMainPrompt(uuid).start();
            } else {
                new FabricConditionMenuPrompt(uuid).start();
            }
        }
    }

    public class ConditionSelectEditPrompt extends FabricConditionsEditorStringPrompt {

        public ConditionSelectEditPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return FabricLang.get("conditionEditTitle");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("conditionEditorEnterName");
        }

        @Override
        public @NotNull String getPromptText() {
            final List<String> names = plugin.getLoadedConditions().stream().map(Condition::getName)
                    .collect(Collectors.toList());
            return sendClickableMenu(getTitle(), names, getQueryText(), plugin.getQuester(uuid));
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
                final Condition c = plugin.getCondition(input);
                if (c != null) {
                    SessionData.set(uuid, Key.C_OLD_CONDITION, c.getName());
                    SessionData.set(uuid, Key.C_NAME, c.getName());
                    plugin.getConditionFactory().loadData(uuid, c);
                    new FabricConditionMainPrompt(uuid).start();
                    return;
                }
                sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("conditionEditorNotFound")
                        .replace("<input>", input)));
                new ConditionSelectEditPrompt(uuid).start();
            } else {
                new FabricConditionMenuPrompt(uuid).start();
            }
        }
    }

    public class ConditionSelectDeletePrompt extends FabricConditionsEditorStringPrompt {

        public ConditionSelectDeletePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return FabricLang.get("conditionDeleteTitle");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("conditionEditorEnterName");
        }

        @Override
        public @NotNull String getPromptText() {
            final List<String> names = plugin.getLoadedConditions().stream().map(Condition::getName)
                    .collect(Collectors.toList());
            return sendClickableMenu(getTitle(), names, getQueryText(), plugin.getQuester(uuid));
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
                        SessionData.set(uuid, Key.ED_CONDITION_DELETE, c.getName());
                        new ConditionConfirmDeletePrompt(uuid).start();
                    } else {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("conditionEditorInUse")
                        + " \"" + ChatFormatting.DARK_PURPLE + c.getName() + ChatFormatting.RED + "\":"));
                        for (final String s : used) {
                            sender.sendSystemMessage(Component.literal(ChatFormatting.RED + "- " + ChatFormatting.DARK_RED + s));
                        }
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED
                                + FabricLang.get("eventEditorMustModifyQuests")));
                        new ConditionSelectDeletePrompt(uuid).start();
                    }
                    return;
                }
                sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("conditionEditorNotFound")
                        .replace("<input>", input)));
                new ConditionSelectDeletePrompt(uuid).start();
            } else {
                new FabricConditionMenuPrompt(uuid).start();
            }
        }
    }

    public class ConditionConfirmDeletePrompt extends FabricConditionsEditorStringPrompt {

        public ConditionConfirmDeletePrompt(final @NotNull UUID uuid) {
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
            final StringBuilder text = new StringBuilder(ChatFormatting.RED + getQueryText() + " (" + ChatFormatting.YELLOW
                    + SessionData.get(uuid, Key.ED_CONDITION_DELETE) + ChatFormatting.RED + ")\n");
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
            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(FabricLang.get("yesWord"))) {
                plugin.getConditionFactory().deleteCondition(uuid);
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(FabricLang.get("noWord"))) {
                new FabricConditionMenuPrompt(uuid).start();
            } else {
                new ConditionConfirmDeletePrompt(uuid).start();
            }
        }
    }
}
