/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.convo.actions.menu;

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.actions.Action;
import me.pikamug.quests.convo.FabricQuestsIntegerPrompt;
import me.pikamug.quests.convo.actions.FabricActionsEditorIntegerPrompt;
import me.pikamug.quests.convo.actions.FabricActionsEditorStringPrompt;
import me.pikamug.quests.convo.actions.main.FabricActionMainPrompt;
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

public class FabricActionMenuPrompt extends FabricActionsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final FabricQuestsPlugin plugin;

    public FabricActionMenuPrompt(final @NotNull UUID uuid) {
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
        final String title = FabricLang.get("eventEditorTitle");
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
            return ChatFormatting.YELLOW + FabricLang.get("eventEditorCreate");
        case 2:
            return ChatFormatting.YELLOW + FabricLang.get("eventEditorEdit");
        case 3:
            return ChatFormatting.YELLOW + FabricLang.get("eventEditorDelete");
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
            text.append("\n").append(getNumberColor(i)).append(ChatFormatting.BOLD).append(i).append(ChatFormatting.RESET).append(" - ").append(getSelectionText(i));
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
            if (me.pikamug.quests.util.FabricMiscUtil.hasPermission(sender, net.minecraft.server.permissions.PermissionLevel.GAMEMASTERS)) {
                SessionData.set(uuid, Key.A_OLD_ACTION, "");
                new ActionSelectCreatePrompt(uuid).start();
            } else {
                sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("noPermission")));
                new FabricActionMenuPrompt(uuid).start();
            }
            break;
        case 2:
            if (me.pikamug.quests.util.FabricMiscUtil.hasPermission(sender, net.minecraft.server.permissions.PermissionLevel.GAMEMASTERS)) {
                if (plugin.getLoadedActions().isEmpty()) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW
                            + FabricLang.get("eventEditorNoneToEdit")));
                    new FabricActionMenuPrompt(uuid).start();
                } else {
                    new ActionSelectEditPrompt(uuid).start();
                }
            } else {
                sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("noPermission")));
                new FabricActionMenuPrompt(uuid).start();
            }
            break;
        case 3:
            if (me.pikamug.quests.util.FabricMiscUtil.hasPermission(sender, net.minecraft.server.permissions.PermissionLevel.GAMEMASTERS)) {
                if (plugin.getLoadedActions().isEmpty()) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW
                            + FabricLang.get("eventEditorNoneToDelete")));
                    new FabricActionMenuPrompt(uuid).start();
                } else {
                    new ActionSelectDeletePrompt(uuid).start();
                }
            } else {
                sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("noPermission")));
                new FabricActionMenuPrompt(uuid).start();
            }
            break;
        case 4:
            sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("exited")));
            return;
        default:
            new FabricActionMenuPrompt(uuid).start();
            break;
        }
    }

    public class ActionSelectCreatePrompt extends FabricActionsEditorStringPrompt {
        public ActionSelectCreatePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return FabricLang.get("eventCreateTitle");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("eventEditorEnterEventName");
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
                new ActionSelectCreatePrompt(uuid).start();
                return;
            }
            input = input.trim();
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                for (final Action action : plugin.getLoadedActions()) {
                    if (action.getName().equalsIgnoreCase(input)) {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("eventEditorExists")));
                        new ActionSelectCreatePrompt(uuid).start();
                        return;
                    }
                }
                final List<String> actionNames = plugin.getActionFactory().getNamesOfActionsBeingEdited();
                if (actionNames.contains(input)) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("questEditorBeingEdited")));
                    new ActionSelectCreatePrompt(uuid).start();
                    return;
                }
                if (input.contains(".") || input.contains(",")) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("questEditorInvalidQuestName")));
                    new ActionSelectCreatePrompt(uuid).start();
                    return;
                }
                if (input.isEmpty()) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("itemCreateInvalidInput")));
                    new ActionSelectCreatePrompt(uuid).start();
                    return;
                }
                SessionData.set(uuid, Key.A_NAME, input);
                actionNames.add(input);
                plugin.getActionFactory().setNamesOfActionsBeingEdited(actionNames);
                new FabricActionMainPrompt(uuid).start();
            } else {
                new FabricActionMenuPrompt(uuid).start();
            }
        }
    }

    public class ActionSelectEditPrompt extends FabricActionsEditorStringPrompt {

        public ActionSelectEditPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return FabricLang.get("eventEditTitle");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("eventEditorEnterEventName");
        }

        @Override
        public @NotNull String getPromptText() {
            final List<String> names = plugin.getLoadedActions().stream().map(Action::getName).collect(Collectors.toList());
            return sendClickableMenu(getTitle(), names, getQueryText(), plugin.getQuester(uuid));
        }

        @Override
        public void acceptInput(final String input) {
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (input == null) {
                return;
            }
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                final Action action = plugin.getAction(input);
                if (action != null) {
                    SessionData.set(uuid, Key.A_OLD_ACTION, action.getName());
                    SessionData.set(uuid, Key.A_NAME, action.getName());
                    plugin.getActionFactory().loadData(uuid, action);
                    new FabricActionMainPrompt(uuid).start();
                    return;
                }
                sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("eventEditorNotFound").replace("<input>", input)));
                new ActionSelectEditPrompt(uuid).start();
            } else {
                new FabricActionMenuPrompt(uuid).start();
            }
        }
    }

    public class ActionSelectDeletePrompt extends FabricActionsEditorStringPrompt {

        public ActionSelectDeletePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return FabricLang.get("eventDeleteTitle");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("eventEditorEnterEventName");
        }

        @Override
        public @NotNull String getPromptText() {
            final List<String> names = plugin.getLoadedActions().stream().map(Action::getName).collect(Collectors.toList());
            return sendClickableMenu(getTitle(), names, getQueryText(), plugin.getQuester(uuid));
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
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
                        SessionData.set(uuid, Key.ED_EVENT_DELETE, action.getName());
                        new ActionConfirmDeletePrompt(uuid).start();
                    } else {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("eventEditorEventInUse")
                                + " \"" + ChatFormatting.DARK_PURPLE + action.getName() + ChatFormatting.RED + "\":"));
                        for (final String s : used) {
                            sender.sendSystemMessage(Component.literal(ChatFormatting.RED + "- " + ChatFormatting.DARK_RED + s));
                        }
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED
                                + FabricLang.get("eventEditorMustModifyQuests")));
                        new ActionSelectDeletePrompt(uuid).start();
                    }
                    return;
                }
                sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("eventEditorNotFound")
                        .replace("<input>", input)));
                new ActionSelectDeletePrompt(uuid).start();
            } else {
                new FabricActionMenuPrompt(uuid).start();
            }
        }
    }

    public class ActionConfirmDeletePrompt extends FabricActionsEditorStringPrompt {

        public ActionConfirmDeletePrompt(final @NotNull UUID uuid) {
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
                    + SessionData.get(uuid, Key.ED_EVENT_DELETE) + ChatFormatting.RED + ")\n");
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatFormatting.BOLD).append(i)
                        .append(ChatFormatting.RESET).append(" - ").append(getSelectionText(i));
            }
            return FabricQuestsIntegerPrompt.sendClickableSelection(text.toString(), plugin.getQuester(uuid));
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(FabricLang.get("yesWord"))) {
                plugin.getActionFactory().deleteAction(uuid);
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(FabricLang.get("noWord"))) {
                new FabricActionMenuPrompt(uuid).start();
            } else {
                new ActionConfirmDeletePrompt(uuid).start();
            }
        }
    }
}
