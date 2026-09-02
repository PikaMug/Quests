/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.convo.quests.menu;

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.convo.quests.FabricQuestsEditorIntegerPrompt;
import me.pikamug.quests.convo.quests.FabricQuestsEditorStringPrompt;
import me.pikamug.quests.convo.quests.main.FabricQuestMainPrompt;
import me.pikamug.quests.quests.Quest;
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

public class FabricQuestMenuPrompt extends FabricQuestsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final FabricQuestsPlugin plugin;

    public FabricQuestMenuPrompt(final @NotNull UUID uuid) {
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
        final String title = FabricLang.get("questEditorTitle");
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
            return ChatFormatting.YELLOW + FabricLang.get("questEditorCreate");
        case 2:
            return ChatFormatting.YELLOW + FabricLang.get("questEditorEdit");
        case 3:
            return ChatFormatting.YELLOW + FabricLang.get("questEditorDelete");
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
            if (me.pikamug.quests.util.FabricMiscUtil.hasPermission(sender, net.minecraft.server.permissions.PermissionLevel.GAMEMASTERS)) {
                new FabricQuestSelectCreatePrompt(uuid).start();
            } else {
                sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("noPermission")));
                new FabricQuestMenuPrompt(uuid).start();
            }
            break;
        case 2:
            if (me.pikamug.quests.util.FabricMiscUtil.hasPermission(sender, net.minecraft.server.permissions.PermissionLevel.GAMEMASTERS)) {
                new FabricQuestSelectEditPrompt(uuid).start();
            } else {
                sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("noPermission")));
                new FabricQuestMenuPrompt(uuid).start();
            }
            break;
        case 3:
            if (me.pikamug.quests.util.FabricMiscUtil.hasPermission(sender, net.minecraft.server.permissions.PermissionLevel.GAMEMASTERS)) {
                new FabricQuestSelectDeletePrompt(uuid).start();
            } else {
                sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("noPermission")));
                new FabricQuestMenuPrompt(uuid).start();
            }
            break;
        case 4:
            sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("exited")));
            return;
        default:
            new FabricQuestMenuPrompt(uuid).start();
            break;
        }
    }

    public class FabricQuestSelectCreatePrompt extends FabricQuestsEditorStringPrompt {

        public FabricQuestSelectCreatePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return FabricLang.get("questCreateTitle");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("questEditorEnterQuestName");
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
                new FabricQuestSelectCreatePrompt(uuid).start();
                return;
            }
            input = input.trim();
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                for (final Quest q : plugin.getLoadedQuests()) {
                    if (q.getName().equalsIgnoreCase(input)) {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("questEditorNameExists")));
                        new FabricQuestSelectCreatePrompt(uuid).start();
                        return;
                    }
                }
                final List<String> questNames = plugin.getQuestFactory().getNamesOfQuestsBeingEdited();
                if (questNames.contains(input)) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("questEditorBeingEdited")));
                    new FabricQuestSelectCreatePrompt(uuid).start();
                    return;
                }
                if (input.contains(".") || input.contains(",")) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("questEditorInvalidQuestName")));
                    new FabricQuestSelectCreatePrompt(uuid).start();
                    return;
                }
                if (input.isEmpty()) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("itemCreateInvalidInput")));
                    new FabricQuestSelectCreatePrompt(uuid).start();
                    return;
                }
                SessionData.set(uuid, Key.Q_NAME, input);
                SessionData.set(uuid, Key.Q_ASK_MESSAGE, FabricLang.get("questEditorDefaultAskMessage"));
                SessionData.set(uuid, Key.Q_FINISH_MESSAGE, FabricLang.get("questEditorDefaultFinishMessage"));
                questNames.add(input);
                plugin.getQuestFactory().setNamesOfQuestsBeingEdited(questNames);
                new FabricQuestMainPrompt(uuid).start();
            } else {
                new FabricQuestMenuPrompt(uuid).start();
            }
        }
    }

    public class FabricQuestSelectEditPrompt extends FabricQuestsEditorStringPrompt {

        public FabricQuestSelectEditPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return FabricLang.get("questEditTitle");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("questEditorEnterQuestName");
        }

        @Override
        public @NotNull String getPromptText() {
            final List<String> names = plugin.getLoadedQuests().stream().map(Quest::getName)
                    .collect(Collectors.toList());
            return sendClickableMenu(getTitle(), names, getQueryText(), plugin.getQuester(uuid));
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                final Quest q = plugin.getQuest(input);
                if (q != null) {
                    plugin.getQuestFactory().loadQuest(uuid, q);
                    new FabricQuestMainPrompt(uuid).start();
                    return;
                }
                sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("questNotFound")
                        .replace("<input>", input)));
                new FabricQuestSelectEditPrompt(uuid).start();
            } else {
                new FabricQuestMenuPrompt(uuid).start();
            }
        }
    }

    public class FabricQuestSelectDeletePrompt extends FabricQuestsEditorStringPrompt {

        public FabricQuestSelectDeletePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return FabricLang.get("questDeleteTitle");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("questEditorEnterQuestName");
        }

        @Override
        public @NotNull String getPromptText() {
            final List<String> names = plugin.getLoadedQuests().stream().map(Quest::getName)
                    .collect(Collectors.toList());
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
                final Quest found = plugin.getQuest(input);
                if (found != null) {
                    for (final Quest q : plugin.getLoadedQuests()) {
                        if (q.getRequirements().getNeededQuestIds().contains(q.getId())
                                || q.getRequirements().getBlockQuestIds().contains(q.getId())) {
                            used.add(q.getName());
                        }
                    }
                    if (used.isEmpty()) {
                        SessionData.set(uuid, Key.ED_QUEST_DELETE, found.getName());
                        new FabricQuestConfirmDeletePrompt(uuid).start();
                        return;
                    } else {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED
                                + FabricLang.get("questEditorQuestAsRequirement1") + " \"" + ChatFormatting.DARK_PURPLE
                                + SessionData.get(uuid, Key.ED_QUEST_DELETE) + ChatFormatting.RED + "\" "
                                + FabricLang.get("questEditorQuestAsRequirement2")));
                        for (final String s : used) {
                            sender.sendSystemMessage(Component.literal(ChatFormatting.RED + "- " + ChatFormatting.DARK_RED + s));
                        }
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED
                                + FabricLang.get("questEditorQuestAsRequirement3")));
                        new FabricQuestSelectDeletePrompt(uuid).start();
                        return;
                    }
                }
                sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("questNotFound")
                        .replace("<input>", input)));
                new FabricQuestSelectDeletePrompt(uuid).start();
            } else {
                new FabricQuestMenuPrompt(uuid).start();
            }
        }
    }

    public class FabricQuestConfirmDeletePrompt extends FabricQuestsEditorStringPrompt {

        public FabricQuestConfirmDeletePrompt(final @NotNull UUID uuid) {
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
                    + SessionData.get(uuid, Key.ED_QUEST_DELETE) + ChatFormatting.RED + ")\n");
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
                plugin.getQuestFactory().deleteQuest(uuid);
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(FabricLang.get("noWord"))) {
                new FabricQuestMenuPrompt(uuid).start();
            } else {
                new FabricQuestConfirmDeletePrompt(uuid).start();
            }
        }
    }
}
