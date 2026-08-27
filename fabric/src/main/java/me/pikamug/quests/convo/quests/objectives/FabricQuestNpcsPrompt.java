/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.convo.quests.objectives;

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.convo.generic.FabricItemStackPrompt;
import me.pikamug.quests.convo.quests.FabricQuestsEditorIntegerPrompt;
import me.pikamug.quests.convo.quests.FabricQuestsEditorStringPrompt;
import me.pikamug.quests.convo.quests.stages.FabricQuestStageMainPrompt;
import me.pikamug.quests.util.FabricItemUtil;
import me.pikamug.quests.util.FabricLang;
import me.pikamug.quests.util.FabricMiscUtil;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.SessionData;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class FabricQuestNpcsPrompt extends FabricQuestsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final FabricQuestsPlugin plugin;
    private final int stageNum;
    private final String pref;

    public FabricQuestNpcsPrompt(final int stageNum, final @NotNull UUID uuid) {
        super(uuid);
        this.uuid = uuid;
        this.plugin = FabricQuestsPlugin.getInstance();
        this.stageNum = stageNum;
        this.pref = "stage" + stageNum;
    }

    private final int size = 4;

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public String getTitle() {
        return FabricLang.get("stageEditorNPCs");
    }

    @Override
    public ChatFormatting getNumberColor(final int number) {
        switch (number) {
            case 1:
            case 2:
            case 3:
                return ChatFormatting.BLUE;
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
            return ChatFormatting.YELLOW + FabricLang.get("stageEditorDeliverItems");
        case 2:
            return ChatFormatting.YELLOW + FabricLang.get("stageEditorTalkToNPCs");
        case 3:
            return ChatFormatting.YELLOW + FabricLang.get("stageEditorKillNPCs");
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
            if (plugin.getDependencies().hasAnyNpcDependencies()) {
                if (SessionData.get(uuid, pref + Key.S_DELIVERY_ITEMS) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final LinkedList<String> npcs
                            = (LinkedList<String>) SessionData.get(uuid, pref + Key.S_DELIVERY_NPCS);
                    final LinkedList<ItemStack> items
                            = (LinkedList<ItemStack>) SessionData.get(uuid, pref + Key.S_DELIVERY_ITEMS);
                    if (npcs != null && items != null) {
                        for (int i = 0; i < npcs.size(); i++) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.BLUE)
                                    .append(FabricItemUtil.getName(items.get(i))).append(ChatFormatting.GRAY).append(" x ")
                                    .append(ChatFormatting.AQUA).append(items.get(i).getCount()).append(ChatFormatting.GRAY)
                                    .append(" ").append(FabricLang.get("to")).append(" ").append(ChatFormatting.BLUE)
                                    .append(plugin.getDependencies().getNpcName(UUID.fromString(npcs.get(i))));
                        }
                    }
                    return text.toString();
                }
            } else {
                return ChatFormatting.GRAY + "(" + FabricLang.get("notInstalled") + ")";
            }
        case 2:
            if (plugin.getDependencies().hasAnyNpcDependencies()) {
                if (SessionData.get(uuid, pref + Key.S_NPCS_TO_TALK_TO) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final LinkedList<String> npcs
                            = (LinkedList<String>) SessionData.get(uuid, pref + Key.S_NPCS_TO_TALK_TO);
                    if (npcs != null) {
                        for (final String npc : npcs) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.BLUE)
                                    .append(plugin.getDependencies().getNpcName(UUID.fromString(npc)));
                        }
                    }
                    return text.toString();
                }
            } else {
                return ChatFormatting.GRAY + "(" + FabricLang.get("notInstalled") + ")";
            }
        case 3:
            if (plugin.getDependencies().hasAnyNpcDependencies()) {
                if (SessionData.get(uuid, pref + Key.S_NPCS_TO_KILL) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final LinkedList<String> npcs
                            = (LinkedList<String>) SessionData.get(uuid, pref + Key.S_NPCS_TO_KILL);
                    final LinkedList<Integer> amounts
                            = (LinkedList<Integer>) SessionData.get(uuid, pref + Key.S_NPCS_TO_KILL_AMOUNTS);
                    if (npcs != null && amounts != null) {
                        for (int i = 0; i < npcs.size(); i++) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.BLUE)
                                    .append(plugin.getDependencies().getNpcName(UUID.fromString(npcs.get(i))))
                                    .append(ChatFormatting.GRAY).append(" x ").append(ChatFormatting.AQUA).append(amounts.get(i));
                        }
                    }
                    return text.toString();
                }
            } else {
                return ChatFormatting.GRAY + "(" + FabricLang.get("notInstalled") + ")";
            }
        case 4:
            return "";
        default:
            return null;
        }
    }

    @Override
    public @NotNull String getPromptText() {
        SessionData.set(uuid, pref, Boolean.TRUE);

        final StringBuilder text = new StringBuilder(ChatFormatting.AQUA + "- " + getTitle() + " -");
        for (int i = 1; i <= size; i++) {
            text.append("\n").append(getNumberColor(i)).append(ChatFormatting.BOLD).append(i)
                    .append(ChatFormatting.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                    .append(getAdditionalText(i));
        }
        return sendClickableSelection(text.toString(), plugin.getQuester(uuid));
    }

    @Override
    public void acceptInput(final Number input) {
        final net.minecraft.server.level.ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
        if (sender == null) {
            return;
        }
        switch(input.intValue()) {
        case 1:
            if (plugin.getDependencies().hasAnyNpcDependencies()) {
                new QuestNpcsDeliveryListPrompt(uuid).start();
            } else {
                sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        ChatFormatting.RED + FabricLang.get("stageEditorNoCitizens")));
                new FabricQuestStageMainPrompt(stageNum, uuid).start();
            }
            break;
        case 2:
            if (plugin.getDependencies().hasAnyNpcDependencies()) {
                new QuestNpcsIdsToTalkToPrompt(uuid).start();
            } else {
                sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        ChatFormatting.RED + FabricLang.get("stageEditorNoCitizens")));
                new FabricQuestStageMainPrompt(stageNum, uuid).start();
            }
            break;
        case 3:
            if (plugin.getDependencies().hasAnyNpcDependencies()) {
                new QuestNpcsKillListPrompt(uuid).start();
            } else {
                sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        ChatFormatting.RED + FabricLang.get("stageEditorNoCitizens")));
                new FabricQuestStageMainPrompt(stageNum, uuid).start();
            }
            break;
        case 4:
            try {
                new FabricQuestStageMainPrompt(stageNum, uuid).start();
            } catch (final Exception e) {
                sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        ChatFormatting.RED + FabricLang.get("itemCreateCriticalError")));
                return;
            }
            break;
        default:
            new FabricQuestNpcsPrompt(stageNum, uuid).start();
            break;
        }
    }

    public class QuestNpcsDeliveryListPrompt extends FabricQuestsEditorIntegerPrompt {

        public QuestNpcsDeliveryListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 5;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return FabricLang.get("stageEditorDeliverItems");
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
                case 5:
                    return ChatFormatting.GREEN;
                default:
                    return null;
            }
        }

        @Override
        public String getSelectionText(final int number) {
            switch(number) {
            case 1:
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorDeliveryAddItem");
            case 2:
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorNPCUniqueIds");
            case 3:
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorDeliveryMessages");
            case 4:
                return ChatFormatting.RED + FabricLang.get("clear");
            case 5:
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
                if (SessionData.get(uuid, pref + Key.S_DELIVERY_ITEMS) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<ItemStack> deliveryItems
                            = (List<ItemStack>) SessionData.get(uuid, pref + Key.S_DELIVERY_ITEMS);
                    if (deliveryItems != null) {
                        for (final ItemStack is : deliveryItems) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ")
                                    .append(FabricItemUtil.getDisplayString(is));
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (SessionData.get(uuid, pref + Key.S_DELIVERY_NPCS) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> deliveryNpcs = (List<String>) SessionData.get(uuid, pref + Key.S_DELIVERY_NPCS);
                    if (deliveryNpcs != null) {
                        for (final String s : deliveryNpcs) {
                            final UUID npcUuid = UUID.fromString(s);
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA)
                                    .append(plugin.getDependencies().getNpcName(npcUuid)).append(ChatFormatting.GRAY)
                                    .append(" (").append(ChatFormatting.BLUE).append(s).append(ChatFormatting.GRAY).append(")");
                        }
                    }
                    return text.toString();
                }
            case 3:
                if (SessionData.get(uuid, pref + Key.S_DELIVERY_MESSAGES) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> deliveryMessages
                            = (List<String>) SessionData.get(uuid, pref + Key.S_DELIVERY_MESSAGES);
                    if (deliveryMessages != null) {
                        for (final String s : deliveryMessages) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA)
                                    .append("\"").append(s).append("\"");
                        }
                    }
                    return text.toString();
                }
            case 4:
            case 5:
                return "";
            default:
                return null;
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public @NotNull String getPromptText() {
            // Check/add newly made item
            if (SessionData.get(uuid, "tempStack") != null) {
                if (SessionData.get(uuid, pref + Key.S_DELIVERY_ITEMS) != null) {
                    final List<ItemStack> itemRew
                            = (List<ItemStack>) SessionData.get(uuid, pref + Key.S_DELIVERY_ITEMS);
                    if (itemRew != null) {
                        itemRew.add((ItemStack) SessionData.get(uuid, "tempStack"));
                    }
                    SessionData.set(uuid, pref + Key.S_DELIVERY_ITEMS, itemRew);
                } else {
                    final LinkedList<ItemStack> itemRews = new LinkedList<>();
                    itemRews.add((ItemStack) SessionData.get(uuid, "tempStack"));
                    SessionData.set(uuid, pref + Key.S_DELIVERY_ITEMS, itemRews);
                }
                FabricItemStackPrompt.clearSessionData(uuid);
            }

            final StringBuilder text = new StringBuilder(ChatFormatting.AQUA + "- " + getTitle() + " -");
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
            final net.minecraft.server.level.ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            switch(input.intValue()) {
            case 1:
                new FabricItemStackPrompt(uuid, QuestNpcsDeliveryListPrompt.this).start();
                break;
            case 2:
                new QuestNpcDeliveryNpcsPrompt(uuid).start();
                break;
            case 3:
                new QuestNpcDeliveryMessagesPrompt(uuid).start();
                break;
            case 4:
                sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        ChatFormatting.YELLOW + FabricLang.get("cleared")));
                SessionData.set(uuid, pref + Key.S_DELIVERY_ITEMS, null);
                SessionData.set(uuid, pref + Key.S_DELIVERY_NPCS, null);
                SessionData.set(uuid, pref + Key.S_DELIVERY_MESSAGES, null);
                new QuestNpcsDeliveryListPrompt(uuid).start();
                break;
            case 5:
                final int one;
                final int two;
                final List<ItemStack> items = (List<ItemStack>) SessionData.get(uuid, pref + Key.S_DELIVERY_ITEMS);
                final List<UUID> npcs = (List<UUID>) SessionData.get(uuid, pref + Key.S_DELIVERY_NPCS);
                if (items != null) {
                    one = items.size();
                } else {
                    one = 0;
                }
                if (npcs != null) {
                    two = npcs.size();
                } else {
                    two = 0;
                }
                if (one == two) {
                    if (SessionData.get(uuid, pref + Key.S_DELIVERY_MESSAGES) == null && one != 0) {
                        sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                ChatFormatting.RED + FabricLang.get("stageEditorNoDeliveryMessage")));
                        new QuestNpcsDeliveryListPrompt(uuid).start();
                    } else {
                        new FabricQuestNpcsPrompt(stageNum, uuid).start();
                    }
                } else {
                    sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            ChatFormatting.RED + FabricLang.get("listsNotSameSize")));
                    new QuestNpcsDeliveryListPrompt(uuid).start();
                }
                break;
            default:
                new FabricQuestNpcsPrompt(stageNum, uuid).start();
                break;
            }
        }
    }

    public class QuestNpcDeliveryNpcsPrompt extends FabricQuestsEditorStringPrompt {

        public QuestNpcDeliveryNpcsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("enterNpcUniqueIds");
        }

        @Override
        public @NotNull String getPromptText() {
            final net.minecraft.server.level.ServerPlayer player = FabricMiscUtil.getPlayer(uuid, plugin);
            if (player instanceof net.minecraft.server.level.ServerPlayer) {
                final Collection<UUID> selectingNpcs = plugin.getQuestFactory().getSelectingNpcs();
                selectingNpcs.add(uuid);
                plugin.getQuestFactory().setSelectingNpcs(selectingNpcs);
                return ChatFormatting.YELLOW + FabricLang.get("questEditorClickNPCStart");
            } else {
                return ChatFormatting.YELLOW + getQueryText();
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final net.minecraft.server.level.ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                final LinkedList<String> npcs = SessionData.get(uuid, pref + Key.S_DELIVERY_NPCS) != null
                        ? (LinkedList<String>) SessionData.get(uuid, pref + Key.S_DELIVERY_NPCS) : new LinkedList<>();
                for (final String s : input.split(" ")) {
                    try {
                        final UUID npcUuid = UUID.fromString(s);
                        if (npcs != null && plugin.getDependencies().isNpc(npcUuid)) {
                            npcs.add(npcUuid.toString());
                        } else {
                            sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                    ChatFormatting.RED + FabricLang.get("stageEditorInvalidNPC")
                                            .replace("<input>", s)));
                            new QuestNpcDeliveryNpcsPrompt(uuid).start();
                        }
                    } catch (final IllegalArgumentException e) {
                        sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                ChatFormatting.RED + FabricLang.get("stageEditorNotListOfUniqueIds")
                                        .replace("<data>", input)));
                        new QuestNpcDeliveryNpcsPrompt(uuid).start();
                    }
                }
                SessionData.set(uuid, pref + Key.S_DELIVERY_NPCS, npcs);

                LinkedList<String> messages = new LinkedList<>();
                if (SessionData.get(uuid, pref + Key.S_DELIVERY_MESSAGES) != null) {
                    messages = (LinkedList<String>) SessionData.get(uuid, pref + Key.S_DELIVERY_MESSAGES);
                }
                if (messages != null && npcs != null) {
                    for (int i = 0; i < npcs.size(); i++) {
                        if (i >= messages.size()) {
                            messages.add(ChatFormatting.RESET + FabricLang.get("thankYouMore"));
                        }
                    }
                }
                SessionData.set(uuid, pref + Key.S_DELIVERY_MESSAGES, messages);
            }
            if (sender instanceof net.minecraft.server.level.ServerPlayer) {
                final Collection<UUID> selectingNpcs = plugin.getQuestFactory().getSelectingNpcs();
                selectingNpcs.remove(uuid);
                plugin.getQuestFactory().setSelectingNpcs(selectingNpcs);
            }
            new QuestNpcsDeliveryListPrompt(uuid).start();
        }
    }

    public class QuestNpcDeliveryMessagesPrompt extends FabricQuestsEditorStringPrompt {

        public QuestNpcDeliveryMessagesPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorDeliveryMessagesPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.YELLOW + getQueryText() + "\n" + ChatFormatting.GOLD
                    + FabricLang.get("stageEditorNPCNote");
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                final String[] args = input.split(FabricLang.get("charSemi"));
                final LinkedList<String> messages = new LinkedList<>(Arrays.asList(args));
                SessionData.set(uuid, pref + Key.S_DELIVERY_MESSAGES, messages);
            }
            new QuestNpcsDeliveryListPrompt(uuid).start();
        }
    }

    public class QuestNpcsIdsToTalkToPrompt extends FabricQuestsEditorStringPrompt {

        public QuestNpcsIdsToTalkToPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("enterOrClearNpcUniqueIds");
        }

        @Override
        public @NotNull String getPromptText() {
            final net.minecraft.server.level.ServerPlayer player = FabricMiscUtil.getPlayer(uuid, plugin);
            if (player instanceof net.minecraft.server.level.ServerPlayer) {
                final Collection<UUID> selectingNpcs = plugin.getQuestFactory().getSelectingNpcs();
                selectingNpcs.add(uuid);
                plugin.getQuestFactory().setSelectingNpcs(selectingNpcs);
                return ChatFormatting.YELLOW + FabricLang.get("questEditorClickNPCStart");
            } else {
                return ChatFormatting.YELLOW + getQueryText();
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final net.minecraft.server.level.ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                final String[] args = input.split(" ");
                final LinkedList<String> npcs = SessionData.get(uuid, pref + Key.S_NPCS_TO_TALK_TO) != null
                        ? (LinkedList<String>) SessionData.get(uuid, pref + Key.S_NPCS_TO_TALK_TO) : new LinkedList<>();
                for (final String s : args) {
                    try {
                        final UUID npcUuid = UUID.fromString(s);
                        if (npcs != null && plugin.getDependencies().isNpc(npcUuid)) {
                            npcs.add(npcUuid.toString());
                        } else {
                            sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                    ChatFormatting.RED + FabricLang.get("stageEditorInvalidNPC")
                                            .replace("<input>", s)));
                            new QuestNpcsIdsToTalkToPrompt(uuid).start();
                        }
                    } catch (final NumberFormatException e) {
                        sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                ChatFormatting.RED + FabricLang.get("stageEditorNotListOfUniqueIds")
                                        .replace("<data>", s)));
                        new QuestNpcsIdsToTalkToPrompt(uuid).start();
                    }
                }
                SessionData.set(uuid, pref + Key.S_NPCS_TO_TALK_TO, npcs);
            } else if (input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, pref + Key.S_NPCS_TO_TALK_TO, null);
            }
            if (sender instanceof net.minecraft.server.level.ServerPlayer) {
                final Collection<UUID> selectingNpcs = plugin.getQuestFactory().getSelectingNpcs();
                selectingNpcs.remove(uuid);
                plugin.getQuestFactory().setSelectingNpcs(selectingNpcs);
            }
            new FabricQuestStageMainPrompt(stageNum, uuid).start();
        }
    }

    public class QuestNpcsKillListPrompt extends FabricQuestsEditorIntegerPrompt {

        public QuestNpcsKillListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 4;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return FabricLang.get("stageEditorNPCs");
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
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorNPCUniqueIds");
            case 2:
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorSetKillAmounts");
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
                if (plugin.getDependencies().hasAnyNpcDependencies()) {
                    if (SessionData.get(uuid, pref + Key.S_NPCS_TO_KILL) == null) {
                        return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                    } else {
                        final StringBuilder text = new StringBuilder();
                        final List<String> npcsToKill = (List<String>) SessionData.get(uuid, pref + Key.S_NPCS_TO_KILL);
                        if (npcsToKill != null) {
                            for (final String s : npcsToKill) {
                                text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.BLUE)
                                        .append(plugin.getDependencies().getNpcName(UUID.fromString(s)))
                                        .append(ChatFormatting.GRAY).append(" (").append(ChatFormatting.AQUA).append(s)
                                        .append(ChatFormatting.GRAY).append(")");
                            }
                        }
                        return text.toString();
                    }
                } else {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("notInstalled") + ")";
                }
            case 2:
                if (SessionData.get(uuid, pref + Key.S_NPCS_TO_KILL_AMOUNTS) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<Integer> npcsToKillAmounts
                            = (List<Integer>) SessionData.get(uuid, pref + Key.S_NPCS_TO_KILL_AMOUNTS);
                    if (npcsToKillAmounts != null) {
                        for (final Integer i : npcsToKillAmounts) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.BLUE).append(i);
                        }
                    }
                    return text.toString();
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
            final StringBuilder text = new StringBuilder(ChatFormatting.AQUA + "- " + getTitle() + " -");
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
            final net.minecraft.server.level.ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            switch(input.intValue()) {
            case 1:
                new QuestNpcIdsToKillPrompt(uuid).start();
                break;
            case 2:
                new QuestNpcAmountsToKillPrompt(uuid).start();
                break;
            case 3:
                sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        ChatFormatting.YELLOW + FabricLang.get("stageEditorObjectiveCleared")));
                SessionData.set(uuid, pref + Key.S_NPCS_TO_KILL, null);
                SessionData.set(uuid, pref + Key.S_NPCS_TO_KILL_AMOUNTS, null);
                new QuestNpcsKillListPrompt(uuid).start();
                break;
            case 4:
                final int one;
                final int two;
                final List<UUID> kill = (List<UUID>) SessionData.get(uuid, pref + Key.S_NPCS_TO_KILL);
                final List<Integer> killAmounts
                        = (List<Integer>) SessionData.get(uuid, pref + Key.S_NPCS_TO_KILL_AMOUNTS);
                if (kill != null) {
                    one = kill.size();
                } else {
                    one = 0;
                }
                if (killAmounts != null) {
                    two = killAmounts.size();
                } else {
                    two = 0;
                }
                if (one == two) {
                    new FabricQuestStageMainPrompt(stageNum, uuid).start();
                } else {
                    sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            ChatFormatting.RED + FabricLang.get("listsNotSameSize")));
                    new QuestNpcsKillListPrompt(uuid).start();
                }
                break;
            default:
                new FabricQuestNpcsPrompt(stageNum, uuid).start();
                break;
            }
        }
    }

    public class QuestNpcIdsToKillPrompt extends FabricQuestsEditorStringPrompt {

        public QuestNpcIdsToKillPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("enterNpcUniqueIds");
        }

        @Override
        public @NotNull String getPromptText() {
            final net.minecraft.server.level.ServerPlayer player = FabricMiscUtil.getPlayer(uuid, plugin);
            if (player instanceof net.minecraft.server.level.ServerPlayer) {
                final Collection<UUID> selectingNpcs = plugin.getQuestFactory().getSelectingNpcs();
                selectingNpcs.add(uuid);
                plugin.getQuestFactory().setSelectingNpcs(selectingNpcs);
                return ChatFormatting.YELLOW + FabricLang.get("questEditorClickNPCStart");
            } else {
                return ChatFormatting.YELLOW + getQueryText();
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final net.minecraft.server.level.ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                final String[] args = input.split(" ");
                final LinkedList<String> npcs = SessionData.get(uuid, pref + Key.S_NPCS_TO_KILL) != null
                        ? (LinkedList<String>) SessionData.get(uuid, pref + Key.S_NPCS_TO_KILL) : new LinkedList<>();
                for (final String s : args) {
                    try {
                        final UUID npcUuid = UUID.fromString(s);
                        if (npcs != null && plugin.getDependencies().isNpc(npcUuid)) {
                            npcs.add(npcUuid.toString());
                        } else {
                            sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                    ChatFormatting.RED + FabricLang.get("stageEditorInvalidNPC")
                                            .replace("<input>", s)));
                            new QuestNpcIdsToKillPrompt(uuid).start();
                        }
                    } catch (final IllegalArgumentException e) {
                        sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                ChatFormatting.RED + FabricLang.get("stageEditorNotListOfUniqueIds")
                                        .replace("<data>", s)));
                        new QuestNpcIdsToKillPrompt(uuid).start();
                    }
                }
                SessionData.set(uuid, pref + Key.S_NPCS_TO_KILL, npcs);

                LinkedList<Integer> amounts = new LinkedList<>();
                if (SessionData.get(uuid, pref + Key.S_NPCS_TO_KILL_AMOUNTS) != null) {
                    amounts = (LinkedList<Integer>) SessionData.get(uuid, pref + Key.S_NPCS_TO_KILL_AMOUNTS);
                }
                if (npcs != null && amounts != null) {
                    for (int i = 0; i < npcs.size(); i++) {
                        if (i >= amounts.size()) {
                            amounts.add(1);
                        }
                    }
                }
                SessionData.set(uuid, pref + Key.S_NPCS_TO_KILL_AMOUNTS, amounts);
            }
            final Collection<UUID> selectingNpcs = plugin.getQuestFactory().getSelectingNpcs();
            selectingNpcs.remove(uuid);
            plugin.getQuestFactory().setSelectingNpcs(selectingNpcs);
            new QuestNpcsKillListPrompt(uuid).start();
        }
    }

    public class QuestNpcAmountsToKillPrompt extends FabricQuestsEditorStringPrompt {

        public QuestNpcAmountsToKillPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorKillNPCsPrompt");
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
            final net.minecraft.server.level.ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                final String[] args = input.split(" ");
                final LinkedList<Integer> amounts = new LinkedList<>();
                for (final String s : args) {
                    try {
                        if (Integer.parseInt(s) > 0) {
                            amounts.add(Integer.parseInt(s));
                        } else {
                            sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                    ChatFormatting.RED + FabricLang.get("invalidMinimum")
                                            .replace("<number>", "1")));
                            new QuestNpcAmountsToKillPrompt(uuid).start();
                        }
                    } catch (final NumberFormatException e) {
                        sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                ChatFormatting.RED + FabricLang.get("stageEditorNotListOfUniqueIds")
                                        .replace("<data>", s)));
                        new QuestNpcAmountsToKillPrompt(uuid).start();
                    }
                }
                SessionData.set(uuid, pref + Key.S_NPCS_TO_KILL_AMOUNTS, amounts);
            }
            new QuestNpcsKillListPrompt(uuid).start();
        }
    }
}
