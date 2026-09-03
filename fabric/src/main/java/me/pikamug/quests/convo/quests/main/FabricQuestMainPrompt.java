/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.convo.quests.main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.convo.generic.FabricItemStackPrompt;
import me.pikamug.quests.convo.quests.FabricQuestsEditorIntegerPrompt;
import me.pikamug.quests.convo.quests.FabricQuestsEditorStringPrompt;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.util.FabricItemUtil;
import me.pikamug.quests.util.FabricLang;
import me.pikamug.quests.util.FabricMiscUtil;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.SessionData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FabricQuestMainPrompt extends FabricQuestsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final FabricQuestsPlugin plugin;

    public FabricQuestMainPrompt(final @NotNull UUID uuid) {
        super(uuid);
        this.uuid = uuid;
        this.plugin = FabricQuestsPlugin.getInstance();
    }

    private final int size = 14;

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public String getTitle() {
        final StringBuilder title = new StringBuilder(FabricLang.get("quest") + ": " + SessionData.get(uuid, Key.Q_NAME));

        if (plugin.hasLimitedAccess(uuid)) {
            title.append(ChatFormatting.RED).append(" (").append(FabricLang.get("trialMode")).append(")");
        } else if (SessionData.get(uuid, Key.Q_ID) != null) {
            title.append(ChatFormatting.GRAY).append(" (").append(FabricLang.get("id")).append(":")
                    .append(SessionData.get(uuid, Key.Q_ID)).append(")");
        }
        return title.toString();
    }

    @Override
    public ChatFormatting getNumberColor(final int number) {
        switch (number) {
        case 1:
        case 2:
        case 3:
        case 4:
        case 8:
        case 9:
        case 10:
        case 11:
        case 12:
            return ChatFormatting.BLUE;
        case 5:
            if (FabricMiscUtil.getPlayer(uuid, plugin) instanceof ServerPlayer) {
                return ChatFormatting.BLUE;
            } else {
                return ChatFormatting.GRAY;
            }
        case 6:
            return ChatFormatting.GRAY;
        case 7:
            if (plugin.getDependencies().hasAnyNpcDependencies()) {
                return ChatFormatting.BLUE;
            } else {
                return ChatFormatting.GRAY;
            }
        case 13:
            return ChatFormatting.GREEN;
        case 14:
            return ChatFormatting.RED;
        default:
            return null;
        }
    }

    @Override
    public String getSelectionText(final int number) {
        switch (number) {
        case 1:
            return ChatFormatting.YELLOW + FabricLang.get("questEditorName");
        case 2:
            return ChatFormatting.YELLOW + FabricLang.get("questEditorAskMessage");
        case 3:
            return ChatFormatting.YELLOW + FabricLang.get("questEditorFinishMessage");
        case 4:
            if (plugin.getDependencies().hasAnyNpcDependencies()) {
                return ChatFormatting.YELLOW + FabricLang.get("questEditorNPCStart");
            } else {
                return ChatFormatting.GRAY + FabricLang.get("questEditorNPCStart");
            }
        case 5:
            if (FabricMiscUtil.getPlayer(uuid, plugin) instanceof ServerPlayer) {
                return ChatFormatting.YELLOW + FabricLang.get("questEditorBlockStart");
            } else {
                return ChatFormatting.GRAY + FabricLang.get("questEditorBlockStart");
            }
        case 6:
            return ChatFormatting.GRAY + FabricLang.get("questWGSetRegion");
        case 7:
            if (plugin.getDependencies().hasAnyNpcDependencies()) {
                return ChatFormatting.YELLOW + FabricLang.get("questEditorSetGUI");
            } else {
                return ChatFormatting.GRAY + FabricLang.get("questEditorSetGUI");
            }
        case 8:
            return ChatFormatting.DARK_AQUA + FabricLang.get("questEditorReqs");
        case 9:
            return ChatFormatting.AQUA + FabricLang.get("questEditorPln");
        case 10:
            return ChatFormatting.LIGHT_PURPLE + FabricLang.get("questEditorStages");
        case 11:
            return ChatFormatting.DARK_PURPLE + FabricLang.get("questEditorRews");
        case 12:
            return ChatFormatting.DARK_GREEN + FabricLang.get("questEditorOpts");
        case 13:
            return ChatFormatting.GREEN + FabricLang.get("save");
        case 14:
            return ChatFormatting.RED + FabricLang.get("exit");
        default:
            return null;
        }
    }

    @Override
    public String getAdditionalText(final int number) {
        switch (number) {
        case 1:
        case 8:
        case 9:
        case 10:
        case 11:
        case 12:
        case 13:
        case 14:
            return "";
        case 2:
            return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + SessionData.get(uuid, Key.Q_ASK_MESSAGE) + ChatFormatting.RESET
                    + ChatFormatting.GRAY + ")";
        case 3:
            return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + SessionData.get(uuid, Key.Q_FINISH_MESSAGE)
                    + ChatFormatting.RESET + ChatFormatting.GRAY + ")";
        case 4:
            if (SessionData.get(uuid, Key.Q_START_NPC) == null
                    && plugin.getDependencies().hasAnyNpcDependencies()) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else if (plugin.getDependencies().hasAnyNpcDependencies()) {
                final UUID u = UUID.fromString((String) Objects.requireNonNull(SessionData
                        .get(uuid, Key.Q_START_NPC)));
                final ServerPlayer npc = FabricMiscUtil.getPlayer(u, plugin);
                final String npcName = npc != null ? npc.getName().getString() : u.toString().substring(0, 8);
                return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + npcName
                        + ChatFormatting.RESET + ChatFormatting.GRAY + ")";
            } else {
                return ChatFormatting.GRAY + "(" + FabricLang.get("notInstalled") + ")";
            }
        case 5:
            if (SessionData.get(uuid, Key.Q_START_BLOCK) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final Object blockData = SessionData.get(uuid, Key.Q_START_BLOCK);
                if (blockData instanceof BlockPos) {
                    final BlockPos pos = (BlockPos) blockData;
                    return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + pos.getX() + ", "
                            + pos.getY() + ", " + pos.getZ() + ChatFormatting.RESET + ChatFormatting.GRAY + ")";
                }
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            }
        case 6:
            if (SessionData.get(uuid, Key.Q_REGION) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + SessionData.get(uuid, Key.Q_REGION)
                        + ChatFormatting.RESET + ChatFormatting.GRAY + ")";
            }
        case 7:
            if (plugin.getDependencies().hasAnyNpcDependencies()) {
                if (SessionData.get(uuid, Key.Q_GUIDISPLAY) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + FabricItemUtil.getDisplayString((ItemStack)
                            SessionData.get(uuid, Key.Q_GUIDISPLAY)) + ChatFormatting.RESET + ChatFormatting.GRAY + ")";
                }
            } else {
                return ChatFormatting.GRAY + "(" + FabricLang.get("notInstalled") + ")";
            }
        default:
            return null;
        }
    }

    @Override
    public @NotNull String getPromptText() {
        final StringBuilder text = new StringBuilder(ChatFormatting.GOLD + "- " + getTitle().replaceFirst(": ", ": "
                + ChatFormatting.AQUA) + ChatFormatting.GOLD + " -");
        try {
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatFormatting.BOLD).append(i)
                        .append(ChatFormatting.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                        .append(getAdditionalText(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
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
            new FabricQuestNamePrompt(uuid).start();
            break;
        case 2:
            new FabricQuestAskMessagePrompt(uuid).start();
            break;
        case 3:
            new FabricQuestFinishMessagePrompt(uuid).start();
            break;
        case 4:
            if (plugin.getDependencies().hasAnyNpcDependencies()) {
                new FabricQuestNPCStartPrompt(uuid).start();
            } else {
                new FabricQuestMainPrompt(uuid).start();
            }
            break;
        case 5:
            if (sender instanceof ServerPlayer) {
                new FabricQuestBlockStartPrompt(uuid).start();
            } else {
                sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("consoleError")));
                new FabricQuestMainPrompt(uuid).start();
            }
            break;
        case 6:
            new FabricQuestMainPrompt(uuid).start();
            break;
        case 7:
            if (plugin.getDependencies().hasAnyNpcDependencies()) {
                new FabricQuestGuiDisplayPrompt(uuid).start();
            } else {
                new FabricQuestMainPrompt(uuid).start();
            }
            break;
        case 8:
            new me.pikamug.quests.convo.quests.requirements.FabricQuestRequirementsPrompt(uuid).start();
            break;
        case 9:
            new me.pikamug.quests.convo.quests.planner.FabricQuestPlannerPrompt(uuid).start();
            break;
        case 10:
            new me.pikamug.quests.convo.quests.stages.FabricQuestStageMenuPrompt(uuid).start();
            break;
        case 11:
            new me.pikamug.quests.convo.quests.rewards.FabricQuestRewardsPrompt(uuid).start();
            break;
        case 12:
            new me.pikamug.quests.convo.quests.options.FabricQuestOptionsPrompt(uuid).start();
            break;
        case 13:
            new FabricQuestSavePrompt(uuid).start();
            break;
        case 14:
            new FabricQuestExitPrompt(uuid).start();
            break;
        default:
            new FabricQuestMainPrompt(uuid).start();
            break;
        }
    }

    public class FabricQuestNamePrompt extends FabricQuestsEditorStringPrompt {

        public FabricQuestNamePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("questEditorEnterQuestName");
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
                for (final Quest q : plugin.getLoadedQuests()) {
                    if (q.getName().equalsIgnoreCase(input)) {
                        String s = null;
                        if (SessionData.get(uuid, Key.ED_QUEST_EDIT) != null) {
                            s = (String) SessionData.get(uuid, Key.ED_QUEST_EDIT);
                        }
                        if (s != null && !s.equalsIgnoreCase(input)) {
                            sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("questEditorNameExists")));
                            new FabricQuestNamePrompt(uuid).start();
                            return;
                        }
                    }
                }
                final List<String> questNames = plugin.getQuestFactory().getNamesOfQuestsBeingEdited();
                if (questNames.contains(input)) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("questEditorBeingEdited")));
                    new FabricQuestNamePrompt(uuid).start();
                    return;
                }
                if (input.contains(",")) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("questEditorInvalidQuestName")));
                    new FabricQuestNamePrompt(uuid).start();
                    return;
                }
                questNames.remove((String) SessionData.get(uuid, Key.Q_NAME));
                SessionData.set(uuid, Key.Q_NAME, input);
                questNames.add(input);
                plugin.getQuestFactory().setNamesOfQuestsBeingEdited(questNames);
            }
            new FabricQuestMainPrompt(uuid).start();
        }
    }

    public class FabricQuestAskMessagePrompt extends FabricQuestsEditorStringPrompt {

        public FabricQuestAskMessagePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("questEditorEnterAskMessage");
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
                if (input.startsWith("++")) {
                    if (SessionData.get(uuid, Key.Q_ASK_MESSAGE) != null) {
                        SessionData.set(uuid, Key.Q_ASK_MESSAGE, SessionData.get(uuid, Key.Q_ASK_MESSAGE) + " "
                                + input.substring(2));
                        new FabricQuestMainPrompt(uuid).start();
                        return;
                    }
                }
                SessionData.set(uuid, Key.Q_ASK_MESSAGE, input);
            }
            new FabricQuestMainPrompt(uuid).start();
        }
    }

    public class FabricQuestFinishMessagePrompt extends FabricQuestsEditorStringPrompt {

        public FabricQuestFinishMessagePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("questEditorEnterFinishMessage");
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
                if (input.startsWith("++")) {
                    if (SessionData.get(uuid, Key.Q_FINISH_MESSAGE) != null) {
                        SessionData.set(uuid, Key.Q_FINISH_MESSAGE, SessionData.get(uuid, Key.Q_FINISH_MESSAGE) + " "
                                + input.substring(2));
                        new FabricQuestMainPrompt(uuid).start();
                        return;
                    }
                }
                SessionData.set(uuid, Key.Q_FINISH_MESSAGE, input);
            }
            new FabricQuestMainPrompt(uuid).start();
        }
    }

    public class FabricQuestNPCStartPrompt extends FabricQuestsEditorStringPrompt {

        public FabricQuestNPCStartPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("questEditorEnterNPCStart");
        }

        @Override
        public @NotNull String getPromptText() {
            if (FabricMiscUtil.getPlayer(uuid, plugin) instanceof ServerPlayer) {
                final Collection<UUID> selectingNpcs = plugin.getQuestFactory().getSelectingNpcs();
                selectingNpcs.add(uuid);
                plugin.getQuestFactory().setSelectingNpcs(selectingNpcs);
                return ChatFormatting.YELLOW + FabricLang.get("questEditorClickNPCStart");
            } else {
                return ChatFormatting.YELLOW + getQueryText();
            }
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
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel")) && !input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                try {
                    final UUID npcUuid = UUID.fromString(input);
                    if (!plugin.getDependencies().isNpc(npcUuid)) {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("stageEditorInvalidNPC")
                                .replace("<input>", input)));
                        new FabricQuestNPCStartPrompt(uuid).start();
                        return;
                    }
                    SessionData.set(uuid, Key.Q_START_NPC, npcUuid.toString());
                    final Collection<UUID> selectingNpcs = plugin.getQuestFactory().getSelectingNpcs();
                    selectingNpcs.remove(uuid);
                    plugin.getQuestFactory().setSelectingNpcs(selectingNpcs);
                    new FabricQuestMainPrompt(uuid).start();
                    return;
                } catch (final IllegalArgumentException e) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED
                            + FabricLang.get("reqNotAUniqueId").replace("<input>", input)));
                    new FabricQuestNPCStartPrompt(uuid).start();
                }
            } else if (input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.Q_START_NPC, null);
            }
            final Collection<UUID> selectingNpcs = plugin.getQuestFactory().getSelectingNpcs();
            selectingNpcs.remove(uuid);
            plugin.getQuestFactory().setSelectingNpcs(selectingNpcs);
            new FabricQuestMainPrompt(uuid).start();
        }
    }

    public class FabricQuestBlockStartPrompt extends FabricQuestsEditorStringPrompt {

        public FabricQuestBlockStartPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("questEditorEnterBlockStart");
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
            if (input.equalsIgnoreCase(FabricLang.get("cmdDone")) || input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                if (input.equalsIgnoreCase(FabricLang.get("cmdDone"))) {
                    final BlockPos blockPos = sender.blockPosition();
                    SessionData.set(uuid, Key.Q_START_BLOCK, blockPos);
                }
                new FabricQuestMainPrompt(uuid).start();
                return;
            } else if (input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.Q_START_BLOCK, null);
                new FabricQuestMainPrompt(uuid).start();
                return;
            }
            new FabricQuestBlockStartPrompt(uuid).start();
        }
    }

    public class FabricQuestRegionPrompt extends FabricQuestsEditorStringPrompt {

        public FabricQuestRegionPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return FabricLang.get("questRegionTitle");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("questWGPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final StringBuilder text = new StringBuilder(ChatFormatting.AQUA + getTitle() + "\n");
            text.append(ChatFormatting.GRAY).append("(").append(FabricLang.get("none")).append(")\n");
            return text.toString() + ChatFormatting.YELLOW + getQueryText();
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
            if (input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.Q_REGION, null);
                sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("questWGRegionCleared")));
            }
            new FabricQuestMainPrompt(uuid).start();
        }
    }

    public class FabricQuestGuiDisplayPrompt extends FabricQuestsEditorIntegerPrompt {

        public FabricQuestGuiDisplayPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 3;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return FabricLang.get("questGUITitle");
        }

        @Override
        public ChatFormatting getNumberColor(final int number) {
            switch (number) {
            case 1:
                return ChatFormatting.BLUE;
            case 2:
                return ChatFormatting.RED;
            case 3:
                return ChatFormatting.GREEN;
            default:
                return null;
            }
        }

        @Override
        public String getSelectionText(final int number) {
            switch (number) {
            case 1:
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorDeliveryAddItem");
            case 2:
                return ChatFormatting.YELLOW + FabricLang.get("clear");
            case 3:
                return ChatFormatting.YELLOW + FabricLang.get("done");
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
            if (SessionData.get(uuid, "tempStack") != null) {
                final ItemStack stack = (ItemStack) SessionData.get(uuid, "tempStack");
                if (stack != null) {
                    SessionData.set(uuid, Key.Q_GUIDISPLAY, stack.copy());
                }
                FabricItemStackPrompt.clearSessionData(uuid);
            }

            final StringBuilder text = new StringBuilder(ChatFormatting.GOLD + getTitle() + "\n");
            if (SessionData.get(uuid, Key.Q_GUIDISPLAY) != null) {
                final ItemStack stack = (ItemStack) SessionData.get(uuid, Key.Q_GUIDISPLAY);
                text.append(" ").append(ChatFormatting.RESET).append(FabricItemUtil.getDisplayString(stack)).append("\n");
            } else {
                text.append(" ").append(ChatFormatting.GRAY).append("(").append(FabricLang.get("noneSet")).append(")\n");
            }
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
                new FabricItemStackPrompt(uuid, FabricQuestGuiDisplayPrompt.this).start();
                return;
            case 2:
                SessionData.set(uuid, Key.Q_GUIDISPLAY, null);
                sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("questGUICleared")));
                new FabricQuestGuiDisplayPrompt(uuid).start();
                return;
            case 3:
                plugin.getQuestFactory().returnToMenu(uuid);
                return;
            default:
                new FabricQuestGuiDisplayPrompt(uuid).start();
                return;
            }
        }
    }

    public class FabricQuestSavePrompt extends FabricQuestsEditorStringPrompt {

        public FabricQuestSavePrompt(final @NotNull UUID uuid) {
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
            return FabricLang.get("questEditorSave");
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
                if (plugin.hasLimitedAccess(uuid) && !plugin.getConfigSettings().canTrialSave()) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("modeDeny")
                            .replace("<mode>", FabricLang.get("trialMode"))));
                    new FabricQuestMainPrompt(uuid).start();
                    return;
                }
                if (SessionData.get(uuid, Key.Q_ASK_MESSAGE) == null) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("questEditorNeedAskMessage")));
                    new FabricQuestMainPrompt(uuid).start();
                    return;
                }

                if (SessionData.get(uuid, Key.Q_FINISH_MESSAGE) == null) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("questEditorNeedFinishMessage")));
                    new FabricQuestMainPrompt(uuid).start();
                    return;
                }

                if (new me.pikamug.quests.convo.quests.stages.FabricQuestStageMenuPrompt(uuid).getStages() == 0) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("questEditorNeedStages")));
                    new FabricQuestMainPrompt(uuid).start();
                    return;
                }

                try {
                    final Path storageDir = plugin.getPluginDataFolder().toPath().resolve("storage");
                    if (!Files.exists(storageDir)) {
                        Files.createDirectories(storageDir);
                    }
                    final Path questsFile = storageDir.resolve("quests.json");
                    JsonObject root = new JsonObject();
                    if (Files.exists(questsFile)) {
                        try (Reader reader = Files.newBufferedReader(questsFile)) {
                            final JsonElement parsed = JsonParser.parseReader(reader);
                            if (parsed.isJsonObject()) {
                                root = parsed.getAsJsonObject();
                            }
                        }
                    }
                    JsonObject quests = root.has("quests") ? root.getAsJsonObject("quests") : new JsonObject();

                    String questId;
                    if (SessionData.get(uuid, Key.Q_ID) == null) {
                        final Locale locale = Locale.US;
                        final int padding = 6;
                        String format = "%0" + padding + "d";
                        int num = 1;
                        String customNum = String.format(locale, format, num);
                        while (quests.has(customNum)) {
                            num++;
                            customNum = String.format(locale, format, num);
                        }
                        questId = customNum;
                    } else {
                        questId = (String) SessionData.get(uuid, Key.Q_ID);
                    }

                    final JsonObject questData = new JsonObject();
                    questData.addProperty("name", (String) SessionData.get(uuid, Key.Q_NAME));
                    questData.addProperty("ask-message", (String) SessionData.get(uuid, Key.Q_ASK_MESSAGE));
                    questData.addProperty("finish-message", (String) SessionData.get(uuid, Key.Q_FINISH_MESSAGE));
                    if (SessionData.get(uuid, Key.Q_START_NPC) != null) {
                        questData.addProperty("npc-giver-uuid", (String) SessionData.get(uuid, Key.Q_START_NPC));
                    }
                    if (SessionData.get(uuid, Key.Q_START_BLOCK) != null) {
                        final BlockPos pos = (BlockPos) SessionData.get(uuid, Key.Q_START_BLOCK);
                        questData.addProperty("block-start-x", pos.getX());
                        questData.addProperty("block-start-y", pos.getY());
                        questData.addProperty("block-start-z", pos.getZ());
                    }
                    if (SessionData.get(uuid, Key.Q_REGION) != null) {
                        questData.addProperty("region", (String) SessionData.get(uuid, Key.Q_REGION));
                    }
                    quests.add(questId, questData);
                    root.add("quests", quests);

                    final Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    try (Writer writer = Files.newBufferedWriter(questsFile)) {
                        gson.toJson(root, writer);
                    }

                    sender.sendSystemMessage(Component.literal(ChatFormatting.GREEN
                            + FabricLang.get("questEditorSaved").replace("<command>", "/questadmin "
                            + FabricLang.get("COMMAND_QUESTADMIN_RELOAD"))));
                } catch (final IOException e) {
                    FabricQuestsPlugin.LOGGER.error("Failed to save quest", e);
                }
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(FabricLang.get("noWord"))) {
                new FabricQuestMainPrompt(uuid).start();
            } else {
                new FabricQuestSavePrompt(uuid).start();
            }
        }
    }

    public class FabricQuestExitPrompt extends FabricQuestsEditorStringPrompt {

        public FabricQuestExitPrompt(final @NotNull UUID uuid) {
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
                plugin.getQuestFactory().returnToMenu(uuid);
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(FabricLang.get("noWord"))) {
                new FabricQuestMainPrompt(uuid).start();
            } else {
                new FabricQuestExitPrompt(uuid).start();
            }
        }
    }
}
