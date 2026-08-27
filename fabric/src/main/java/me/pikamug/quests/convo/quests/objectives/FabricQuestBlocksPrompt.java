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
import me.pikamug.quests.convo.quests.FabricQuestsEditorIntegerPrompt;
import me.pikamug.quests.convo.quests.FabricQuestsEditorStringPrompt;
import me.pikamug.quests.convo.quests.stages.FabricQuestStageMainPrompt;
import me.pikamug.quests.util.FabricItemUtil;
import me.pikamug.quests.util.FabricLang;
import me.pikamug.quests.util.FabricMiscUtil;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.SessionData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class FabricQuestBlocksPrompt extends FabricQuestsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final FabricQuestsPlugin plugin;
    private final int stageNum;
    private final String pref;

    public FabricQuestBlocksPrompt(final int stageNum, final UUID uuid) {
        super(uuid);
        this.uuid = uuid;
        this.plugin = FabricQuestsPlugin.getInstance();
        this.stageNum = stageNum;
        this.pref = "stage" + stageNum;
    }

    private final int size = 5;

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public String getTitle() {
        return FabricLang.get("stageEditorBlocks");
    }

    @Override
    public ChatFormatting getNumberColor(final int number) {
        switch (number) {
            case 1:
            case 2:
            case 3:
            case 4:
                return ChatFormatting.BLUE;
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
            return ChatFormatting.YELLOW + FabricLang.get("stageEditorBreakBlocks");
        case 2:
            return ChatFormatting.YELLOW + FabricLang.get("stageEditorDamageBlocks");
        case 3:
            return ChatFormatting.YELLOW + FabricLang.get("stageEditorPlaceBlocks");
        case 4:
            return ChatFormatting.YELLOW + FabricLang.get("stageEditorUseBlocks");
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
            if (SessionData.get(uuid, pref + Key.S_BREAK_NAMES) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> names = (LinkedList<String>) SessionData.get(uuid, pref + Key.S_BREAK_NAMES);
                final LinkedList<Integer> amounts
                        = (LinkedList<Integer>) SessionData.get(uuid, pref + Key.S_BREAK_AMOUNTS);
                if (names != null && amounts != null) {
                    for (int i = 0; i < names.size(); i++) {
                        text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.BLUE)
                                .append(getPrettyItemName(names.get(i))).append(ChatFormatting.GRAY).append(" x ")
                                .append(ChatFormatting.DARK_AQUA).append(amounts.get(i));
                    }
                }
                return text.toString();
            }
        case 2:
            if (SessionData.get(uuid, pref + Key.S_DAMAGE_NAMES) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> names = (LinkedList<String>) SessionData.get(uuid, pref + Key.S_DAMAGE_NAMES);
                final LinkedList<Integer> amounts
                        = (LinkedList<Integer>) SessionData.get(uuid, pref + Key.S_DAMAGE_AMOUNTS);
                if (names != null && amounts != null) {
                    for (int i = 0; i < names.size(); i++) {
                        text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.BLUE)
                                .append(getPrettyItemName(names.get(i))).append(ChatFormatting.GRAY).append(" x ")
                                .append(ChatFormatting.DARK_AQUA).append(amounts.get(i));
                    }
                }
                return text.toString();
            }
        case 3:
            if (SessionData.get(uuid, pref + Key.S_PLACE_NAMES) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> names = (LinkedList<String>) SessionData.get(uuid, pref + Key.S_PLACE_NAMES);
                final LinkedList<Integer> amounts
                        = (LinkedList<Integer>) SessionData.get(uuid, pref + Key.S_PLACE_AMOUNTS);
                if (names != null && amounts != null) {
                    for (int i = 0; i < names.size(); i++) {
                        text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.BLUE)
                                .append(getPrettyItemName(names.get(i))).append(ChatFormatting.GRAY).append(" x ")
                                .append(ChatFormatting.DARK_AQUA).append(amounts.get(i));
                    }
                }
                return text.toString();
            }
        case 4:
            if (SessionData.get(uuid, pref + Key.S_USE_NAMES) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> names = (LinkedList<String>) SessionData.get(uuid, pref + Key.S_USE_NAMES);
                final LinkedList<Integer> amounts
                        = (LinkedList<Integer>) SessionData.get(uuid, pref + Key.S_USE_AMOUNTS);
                if (names != null && amounts != null) {
                    for (int i = 0; i < names.size(); i++) {
                        text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.BLUE)
                                .append(getPrettyItemName(names.get(i))).append(ChatFormatting.GRAY).append(" x ")
                                .append(ChatFormatting.DARK_AQUA).append(amounts.get(i));
                    }
                }
                return text.toString();
            }
        case 5:
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
        final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
        if (sender == null) {
            return;
        }
        switch(input.intValue()) {
        case 1:
            new QuestBlocksBreakListPrompt(uuid).start();
            break;
        case 2:
            new QuestBlocksDamageListPrompt(uuid).start();
            break;
        case 3:
            new QuestBlocksPlaceListPrompt(uuid).start();
            break;
        case 4:
            new QuestBlocksUseListPrompt(uuid).start();
            break;
        case 5:
            try {
                new FabricQuestStageMainPrompt(stageNum, uuid).start();
            } catch (final Exception e) {
                sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        ChatFormatting.RED + FabricLang.get("itemCreateCriticalError")));
                return;
            }
            break;
        default:
            new FabricQuestBlocksPrompt(stageNum, uuid).start();
            break;
        }
    }

    private static String getPrettyItemName(final String registryName) {
        if (registryName == null || registryName.isEmpty()) return "Unknown";
        final ResourceLocation resLoc = new ResourceLocation(registryName.toLowerCase());
        final net.minecraft.world.item.Item item = Registry.ITEM.get(resLoc);
        if (item == null || item == net.minecraft.world.item.Items.AIR) return registryName;
        final net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(item);
        return stack.getHoverName().getString();
    }

    public class QuestBlocksBreakListPrompt extends FabricQuestsEditorIntegerPrompt {

        public QuestBlocksBreakListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 5;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return FabricLang.get("stageEditorBreakBlocks");
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
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorSetBlockNames");
            case 2:
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorSetBlockAmounts");
            case 3:
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorSetBlockDurability");
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
                if (SessionData.get(uuid, pref + Key.S_BREAK_NAMES) != null) {
                    final StringBuilder text = new StringBuilder();
                    final List<String> breakNames = (List<String>) SessionData.get(uuid, pref + Key.S_BREAK_NAMES);
                    if (breakNames != null) {
                        for (final String s : breakNames) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA)
                                    .append(getPrettyItemName(s));
                        }
                    }
                    return text.toString();
                } else {
                    return "";
                }
            case 2:
                if (SessionData.get(uuid, pref + Key.S_BREAK_AMOUNTS) != null) {
                    final StringBuilder text = new StringBuilder();
                    final List<Integer> breakAmounts
                            = (List<Integer>) SessionData.get(uuid, pref + Key.S_BREAK_AMOUNTS);
                    if (breakAmounts != null) {
                        for (final Integer i : breakAmounts) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA).append(i);
                        }
                    }
                    return text.toString();
                } else {
                    return "";
                }
            case 3:
                if (SessionData.get(uuid, pref + Key.S_BREAK_DURABILITY) != null) {
                    final StringBuilder text = new StringBuilder();
                    final List<Short> breakDurability
                            = (List<Short>) SessionData.get(uuid, pref + Key.S_BREAK_DURABILITY);
                    if (breakDurability != null) {
                        for (final Short s : breakDurability) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA).append(s);
                        }
                    }
                    return text.toString();
                } else {
                    return "";
                }
            case 4:
            case 5:
                return "";
            default:
                return null;
            }
        }

        @Override
        public @NotNull String getPromptText() {
            final StringBuilder text = new StringBuilder(ChatFormatting.GOLD + "- " + getTitle() + " -");
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
                new QuestBlockBreakNamesPrompt(uuid).start();
                break;
            case 2:
                new QuestBlockBreakAmountsPrompt(uuid).start();
                break;
            case 3:
                new QuestBlockBreakDurabilityPrompt(uuid).start();
                break;
            case 4:
                sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        ChatFormatting.YELLOW + FabricLang.get("stageEditorObjectiveCleared")));
                SessionData.set(uuid, pref + Key.S_BREAK_NAMES, null);
                SessionData.set(uuid, pref + Key.S_BREAK_AMOUNTS, null);
                SessionData.set(uuid, pref + Key.S_BREAK_DURABILITY, null);
                new QuestBlocksBreakListPrompt(uuid).start();
                break;
            case 5:
                final int one;
                final int two;
                final List<Integer> names = (List<Integer>) SessionData.get(uuid, pref + Key.S_BREAK_NAMES);
                final List<Integer> amounts = (List<Integer>) SessionData.get(uuid, pref + Key.S_BREAK_AMOUNTS);
                if (names != null) {
                    one = names.size();
                } else {
                    one = 0;
                }
                if (amounts != null) {
                    two = amounts.size();
                } else {
                    two = 0;
                }
                if (one == two) {
                    final int missing;
                    LinkedList<Short> durability
                            = (LinkedList<Short>) SessionData.get(uuid, pref + Key.S_BREAK_DURABILITY);
                    if (durability != null) {
                        missing = one - durability.size();
                    } else {
                        missing = one;
                        durability = new LinkedList<>();
                    }
                    for (int i = 0; i < missing; i++) {
                        durability.add((short) 0);
                    }
                    SessionData.set(uuid, pref + Key.S_BREAK_DURABILITY, durability);
                    new FabricQuestBlocksPrompt(stageNum, uuid).start();
                } else {
                    sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            ChatFormatting.RED + FabricLang.get("listsNotSameSize")));
                    new QuestBlocksBreakListPrompt(uuid).start();
                }
                break;
            default:
                new FabricQuestBlocksPrompt(stageNum, uuid).start();
                break;
            }
        }
    }

    public class QuestBlockBreakNamesPrompt extends FabricQuestsEditorStringPrompt {

        public QuestBlockBreakNamesPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorEnterBlockNames");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                final String[] args = input.split(" ");
                final LinkedList<String> names = new LinkedList<>();
                for (final String s : args) {
                    try {
                        final ResourceLocation resLoc = new ResourceLocation(s.toLowerCase());
                        final net.minecraft.world.level.block.Block block = Registry.BLOCK.get(resLoc);
                        if (block != null && block != net.minecraft.world.level.block.Blocks.AIR) {
                            names.add(resLoc.toString());
                        } else {
                            sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                    ChatFormatting.RED + FabricLang.get("stageEditorInvalidBlockName")
                                            .replace("<input>", s)));
                            new QuestBlockBreakNamesPrompt(uuid).start();
                            return;
                        }
                    } catch (final Exception e) {
                        sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                ChatFormatting.RED + FabricLang.get("stageEditorNotListOfNumbers")
                                        .replace("<data>", s)));
                        new QuestBlockBreakNamesPrompt(uuid).start();
                        return;
                    }
                }
                SessionData.set(uuid, pref + Key.S_BREAK_NAMES, names);

                LinkedList<Integer> amounts = new LinkedList<>();
                if (SessionData.get(uuid, pref + Key.S_BREAK_AMOUNTS) != null) {
                    amounts = (LinkedList<Integer>) SessionData.get(uuid, pref + Key.S_BREAK_AMOUNTS);
                }
                for (int i = 0; i < names.size(); i++) {
                    if (amounts != null) {
                        if (i >= amounts.size()) {
                            amounts.add(1);
                        }
                    }
                }
                SessionData.set(uuid, pref + Key.S_BREAK_AMOUNTS, amounts);
            }
            new QuestBlocksBreakListPrompt(uuid).start();
        }
    }

    public class QuestBlockBreakAmountsPrompt extends FabricQuestsEditorStringPrompt {

        public QuestBlockBreakAmountsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorEnterBlockAmounts");
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
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                final String[] args = input.split(" ");
                final LinkedList<Integer> amounts = new LinkedList<>();
                for (final String s : args) {
                    try {
                        if (Integer.parseInt(s) > 0) {
                            amounts.add(Integer.parseInt(s));
                        } else {
                            sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                    ChatFormatting.RED + FabricLang.get("invalidMinimum").replace("<number>", "1")));
                            new QuestBlockBreakAmountsPrompt(uuid).start();
                            return;
                        }
                    } catch (final NumberFormatException e) {
                        sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                ChatFormatting.RED + FabricLang.get("stageEditorNotListOfNumbers")
                                        .replace("<data>", s)));
                        new QuestBlockBreakAmountsPrompt(uuid).start();
                        return;
                    }
                }
                SessionData.set(uuid, pref + Key.S_BREAK_AMOUNTS, amounts);
            }
            new QuestBlocksBreakListPrompt(uuid).start();
        }
    }

    public class QuestBlockBreakDurabilityPrompt extends FabricQuestsEditorStringPrompt {

        public QuestBlockBreakDurabilityPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorEnterBlockDurability");
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
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                final String[] args = input.split(" ");
                final LinkedList<Short> durability = new LinkedList<>();
                for (final String s : args) {
                    try {
                        if (Short.parseShort(s) >= 0) {
                            durability.add(Short.parseShort(s));
                        } else {
                            sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                    ChatFormatting.RED + FabricLang.get("invalidMinimum").replace("<number>", "0")));
                            new QuestBlockBreakDurabilityPrompt(uuid).start();
                            return;
                        }
                    } catch (final NumberFormatException e) {
                        sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                ChatFormatting.RED + FabricLang.get("stageEditorNotListOfNumbers")
                                        .replace("<data>", s)));
                        new QuestBlockBreakDurabilityPrompt(uuid).start();
                        return;
                    }
                }
                SessionData.set(uuid, pref + Key.S_BREAK_DURABILITY, durability);
            }
            new QuestBlocksBreakListPrompt(uuid).start();
        }
    }

    public class QuestBlocksDamageListPrompt extends FabricQuestsEditorIntegerPrompt {

        public QuestBlocksDamageListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 5;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return FabricLang.get("stageEditorDamageBlocks");
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
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorSetBlockNames");
            case 2:
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorSetBlockAmounts");
            case 3:
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorSetBlockDurability");
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
                if (SessionData.get(uuid, pref + Key.S_DAMAGE_NAMES) != null) {
                    final StringBuilder text = new StringBuilder();
                    final List<String> damageNames = (List<String>) SessionData.get(uuid, pref + Key.S_DAMAGE_NAMES);
                    if (damageNames != null) {
                        for (final String s : damageNames) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA)
                                    .append(getPrettyItemName(s));
                        }
                    }
                    return text.toString();
                } else {
                    return "";
                }
            case 2:
                if (SessionData.get(uuid, pref + Key.S_DAMAGE_AMOUNTS) != null) {
                    final StringBuilder text = new StringBuilder();
                    final List<Integer> damageAmounts
                            = (List<Integer>) SessionData.get(uuid, pref + Key.S_DAMAGE_AMOUNTS);
                    if (damageAmounts != null) {
                        for (final Integer i : damageAmounts) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA).append(i);
                        }
                    }
                    return text.toString();
                } else {
                    return "";
                }
            case 3:
                if (SessionData.get(uuid, pref + Key.S_DAMAGE_DURABILITY) != null) {
                    final StringBuilder text = new StringBuilder();
                    final List<Short> damageDurability
                            = (List<Short>) SessionData.get(uuid, pref + Key.S_DAMAGE_DURABILITY);
                    if (damageDurability != null) {
                        for (final Short s : damageDurability) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA).append(s);
                        }
                    }
                    return text.toString();
                } else {
                    return "";
                }
            case 4:
            case 5:
                return "";
            default:
                return null;
            }
        }

        @Override
        public @NotNull String getPromptText() {
            final StringBuilder text = new StringBuilder(ChatFormatting.GOLD + "- " + getTitle() + " -");
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
                new QuestBlockDamageNamesPrompt(uuid).start();
                break;
            case 2:
                new QuestBlockDamageAmountsPrompt(uuid).start();
                break;
            case 3:
                new QuestBlockDamageDurabilityPrompt(uuid).start();
                break;
            case 4:
                sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        ChatFormatting.YELLOW + FabricLang.get("stageEditorObjectiveCleared")));
                SessionData.set(uuid, pref + Key.S_DAMAGE_NAMES, null);
                SessionData.set(uuid, pref + Key.S_DAMAGE_AMOUNTS, null);
                SessionData.set(uuid, pref + Key.S_DAMAGE_DURABILITY, null);
                new QuestBlocksDamageListPrompt(uuid).start();
                break;
            case 5:
                final int one;
                final int two;
                final List<Integer> names = (List<Integer>) SessionData.get(uuid, pref + Key.S_DAMAGE_NAMES);
                final List<Integer> amounts = (List<Integer>) SessionData.get(uuid, pref + Key.S_DAMAGE_AMOUNTS);
                if (names != null) {
                    one = names.size();
                } else {
                    one = 0;
                }
                if (amounts != null) {
                    two = amounts.size();
                } else {
                    two = 0;
                }
                if (one == two) {
                    final int missing;
                    LinkedList<Short> durability
                            = (LinkedList<Short>) SessionData.get(uuid, pref + Key.S_DAMAGE_DURABILITY);
                    if (durability != null) {
                        missing = one - durability.size();
                    } else {
                        missing = one;
                        durability = new LinkedList<>();
                    }
                    for (int i = 0; i < missing; i++) {
                        durability.add((short) 0);
                    }
                    SessionData.set(uuid, pref + Key.S_DAMAGE_DURABILITY, durability);
                    new FabricQuestBlocksPrompt(stageNum, uuid).start();
                } else {
                    sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            ChatFormatting.RED + FabricLang.get("listsNotSameSize")));
                    new QuestBlocksDamageListPrompt(uuid).start();
                }
                break;
            default:
                new FabricQuestBlocksPrompt(stageNum, uuid).start();
                break;
            }
        }
    }

    public class QuestBlockDamageNamesPrompt extends FabricQuestsEditorStringPrompt {

        public QuestBlockDamageNamesPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorEnterBlockNames");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                final String[] args = input.split(" ");
                final LinkedList<String> names = new LinkedList<>();
                for (final String s : args) {
                    try {
                        final ResourceLocation resLoc = new ResourceLocation(s.toLowerCase());
                        final net.minecraft.world.level.block.Block block = Registry.BLOCK.get(resLoc);
                        if (block != null && block != net.minecraft.world.level.block.Blocks.AIR) {
                            names.add(resLoc.toString());
                        } else {
                            sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                    ChatFormatting.RED + FabricLang.get("stageEditorInvalidBlockName")
                                            .replace("<input>", s)));
                            new QuestBlockDamageNamesPrompt(uuid).start();
                            return;
                        }
                    } catch (final Exception e) {
                        sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                ChatFormatting.RED + FabricLang.get("stageEditorNotListOfNumbers")
                                        .replace("<data>", s)));
                        new QuestBlockDamageNamesPrompt(uuid).start();
                        return;
                    }
                }
                SessionData.set(uuid, pref + Key.S_DAMAGE_NAMES, names);

                LinkedList<Integer> amounts = new LinkedList<>();
                if (SessionData.get(uuid, pref + Key.S_DAMAGE_AMOUNTS) != null) {
                    amounts = (LinkedList<Integer>) SessionData.get(uuid, pref + Key.S_DAMAGE_AMOUNTS);
                }
                for (int i = 0; i < names.size(); i++) {
                    if (amounts != null) {
                        if (i >= amounts.size()) {
                            amounts.add(1);
                        }
                    }
                }
                SessionData.set(uuid, pref + Key.S_DAMAGE_AMOUNTS, amounts);
            }
            new QuestBlocksDamageListPrompt(uuid).start();
        }
    }

    public class QuestBlockDamageAmountsPrompt extends FabricQuestsEditorStringPrompt {

        public QuestBlockDamageAmountsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorEnterBlockAmounts");
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
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                final String[] args = input.split(" ");
                final LinkedList<Integer> amounts = new LinkedList<>();
                for (final String s : args) {
                    try {
                        if (Integer.parseInt(s) > 0) {
                            amounts.add(Integer.parseInt(s));
                        } else {
                            sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                    ChatFormatting.RED + FabricLang.get("invalidMinimum").replace("<number>", "1")));
                            new QuestBlockDamageAmountsPrompt(uuid).start();
                            return;
                        }
                    } catch (final NumberFormatException e) {
                        sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                ChatFormatting.RED + FabricLang.get("stageEditorNotListOfNumbers")
                                        .replace("<data>", s)));
                        new QuestBlockDamageAmountsPrompt(uuid).start();
                        return;
                    }
                }
                SessionData.set(uuid, pref + Key.S_DAMAGE_AMOUNTS, amounts);
            }
            new QuestBlocksDamageListPrompt(uuid).start();
        }
    }

    public class QuestBlockDamageDurabilityPrompt extends FabricQuestsEditorStringPrompt {

        public QuestBlockDamageDurabilityPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorEnterBlockDurability");
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
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                final String[] args = input.split(" ");
                final LinkedList<Short> durability = new LinkedList<>();
                for (final String s : args) {
                    try {
                        if (Short.parseShort(s) >= 0) {
                            durability.add(Short.parseShort(s));
                        } else {
                            sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                    ChatFormatting.RED + FabricLang.get("invalidMinimum").replace("<number>", "0")));
                            new QuestBlockDamageDurabilityPrompt(uuid).start();
                            return;
                        }
                    } catch (final NumberFormatException e) {
                        sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                ChatFormatting.RED + FabricLang.get("stageEditorNotListOfNumbers")
                                        .replace("<data>", s)));
                        new QuestBlockDamageDurabilityPrompt(uuid).start();
                        return;
                    }
                }
                SessionData.set(uuid, pref + Key.S_DAMAGE_DURABILITY, durability);
            }
            new QuestBlocksDamageListPrompt(uuid).start();
        }
    }

    public class QuestBlocksPlaceListPrompt extends FabricQuestsEditorIntegerPrompt {

        public QuestBlocksPlaceListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 5;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return FabricLang.get("stageEditorPlaceBlocks");
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
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorSetBlockNames");
            case 2:
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorSetBlockAmounts");
            case 3:
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorSetBlockDurability");
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
                if (SessionData.get(uuid, pref + Key.S_PLACE_NAMES) != null) {
                    final StringBuilder text = new StringBuilder();
                    final List<String> placeNames = (List<String>) SessionData.get(uuid, pref + Key.S_PLACE_NAMES);
                    if (placeNames != null) {
                        for (final String s : placeNames) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA)
                                    .append(getPrettyItemName(s));
                        }
                    }
                    return text.toString();
                } else {
                    return "";
                }
            case 2:
                if (SessionData.get(uuid, pref + Key.S_PLACE_AMOUNTS) != null) {
                    final StringBuilder text = new StringBuilder();
                    final List<Integer> placeAmounts
                            = (List<Integer>) SessionData.get(uuid, pref + Key.S_PLACE_AMOUNTS);
                    if (placeAmounts != null) {
                        for (final Integer i : placeAmounts) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA).append(i);
                        }
                    }
                    return text.toString();
                } else {
                    return "";
                }
            case 3:
                if (SessionData.get(uuid, pref + Key.S_PLACE_DURABILITY) != null) {
                    final StringBuilder text = new StringBuilder();
                    final List<Short> placeDurability
                            = (List<Short>) SessionData.get(uuid, pref + Key.S_PLACE_DURABILITY);
                    if (placeDurability != null) {
                        for (final Short s : placeDurability) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA).append(s);
                        }
                    }
                    return text.toString();
                } else {
                    return "";
                }
            case 4:
            case 5:
                return "";
            default:
                return null;
            }
        }

        @Override
        public @NotNull String getPromptText() {
            final StringBuilder text = new StringBuilder(ChatFormatting.GOLD + "- " + getTitle() + " -");
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
                new QuestBlockPlaceNamesPrompt(uuid).start();
                break;
            case 2:
                new QuestBlockPlaceAmountsPrompt(uuid).start();
                break;
            case 3:
                new QuestBlockPlaceDurabilityPrompt(uuid).start();
                break;
            case 4:
                sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        ChatFormatting.YELLOW + FabricLang.get("stageEditorObjectiveCleared")));
                SessionData.set(uuid, pref + Key.S_PLACE_NAMES, null);
                SessionData.set(uuid, pref + Key.S_PLACE_AMOUNTS, null);
                SessionData.set(uuid, pref + Key.S_PLACE_DURABILITY, null);
                new QuestBlocksPlaceListPrompt(uuid).start();
                break;
            case 5:
                final int one;
                final int two;
                final List<Integer> names = (List<Integer>) SessionData.get(uuid, pref + Key.S_PLACE_NAMES);
                final List<Integer> amounts = (List<Integer>) SessionData.get(uuid, pref + Key.S_PLACE_AMOUNTS);
                if (names != null) {
                    one = names.size();
                } else {
                    one = 0;
                }
                if (amounts != null) {
                    two = amounts.size();
                } else {
                    two = 0;
                }
                if (one == two) {
                    final int missing;
                    LinkedList<Short> durability
                            = (LinkedList<Short>) SessionData.get(uuid, pref + Key.S_PLACE_DURABILITY);
                    if (durability != null) {
                        missing = one - durability.size();
                    } else {
                        missing = one;
                        durability = new LinkedList<>();
                    }
                    for (int i = 0; i < missing; i++) {
                        durability.add((short) 0);
                    }
                    SessionData.set(uuid, pref + Key.S_PLACE_DURABILITY, durability);
                    new FabricQuestBlocksPrompt(stageNum, uuid).start();
                } else {
                    sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            ChatFormatting.RED + FabricLang.get("listsNotSameSize")));
                    new QuestBlocksPlaceListPrompt(uuid).start();
                }
                break;
            default:
                new FabricQuestBlocksPrompt(stageNum, uuid).start();
                break;
            }
        }
    }

    public class QuestBlockPlaceNamesPrompt extends FabricQuestsEditorStringPrompt {

        public QuestBlockPlaceNamesPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorEnterBlockNames");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                final String[] args = input.split(" ");
                final LinkedList<String> names = new LinkedList<>();
                for (final String s : args) {
                    try {
                        final ResourceLocation resLoc = new ResourceLocation(s.toLowerCase());
                        final net.minecraft.world.level.block.Block block = Registry.BLOCK.get(resLoc);
                        if (block != null && block != net.minecraft.world.level.block.Blocks.AIR) {
                            names.add(resLoc.toString());
                        } else {
                            sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                    ChatFormatting.RED + FabricLang.get("stageEditorInvalidBlockName")
                                            .replace("<input>", s)));
                            new QuestBlockPlaceNamesPrompt(uuid).start();
                            return;
                        }
                    } catch (final Exception e) {
                        sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                ChatFormatting.RED + FabricLang.get("stageEditorNotListOfNumbers")
                                        .replace("<data>", s)));
                        new QuestBlockPlaceNamesPrompt(uuid).start();
                        return;
                    }
                }
                SessionData.set(uuid, pref + Key.S_PLACE_NAMES, names);

                LinkedList<Integer> amounts = new LinkedList<>();
                if (SessionData.get(uuid, pref + Key.S_PLACE_AMOUNTS) != null) {
                    amounts = (LinkedList<Integer>) SessionData.get(uuid, pref + Key.S_PLACE_AMOUNTS);
                }
                for (int i = 0; i < names.size(); i++) {
                    if (amounts != null) {
                        if (i >= amounts.size()) {
                            amounts.add(1);
                        }
                    }
                }
                SessionData.set(uuid, pref + Key.S_PLACE_AMOUNTS, amounts);
            }
            new QuestBlocksPlaceListPrompt(uuid).start();
        }
    }

    public class QuestBlockPlaceAmountsPrompt extends FabricQuestsEditorStringPrompt {

        public QuestBlockPlaceAmountsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorEnterBlockAmounts");
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
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                final String[] args = input.split(" ");
                final LinkedList<Integer> amounts = new LinkedList<>();
                for (final String s : args) {
                    try {
                        if (Integer.parseInt(s) > 0) {
                            amounts.add(Integer.parseInt(s));
                        } else {
                            sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                    ChatFormatting.RED + FabricLang.get("invalidMinimum").replace("<number>", "1")));
                            new QuestBlockPlaceAmountsPrompt(uuid).start();
                            return;
                        }
                    } catch (final NumberFormatException e) {
                        sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                ChatFormatting.RED + FabricLang.get("stageEditorNotListOfNumbers")
                                        .replace("<data>", s)));
                        new QuestBlockPlaceAmountsPrompt(uuid).start();
                        return;
                    }
                }
                SessionData.set(uuid, pref + Key.S_PLACE_AMOUNTS, amounts);
            }
            new QuestBlocksPlaceListPrompt(uuid).start();
        }
    }

    public class QuestBlockPlaceDurabilityPrompt extends FabricQuestsEditorStringPrompt {

        public QuestBlockPlaceDurabilityPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorEnterBlockDurability");
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
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                final String[] args = input.split(" ");
                final LinkedList<Short> durability = new LinkedList<>();
                for (final String s : args) {
                    try {
                        if (Short.parseShort(s) >= 0) {
                            durability.add(Short.parseShort(s));
                        } else {
                            sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                    ChatFormatting.RED + FabricLang.get("invalidMinimum").replace("<number>", "0")));
                            new QuestBlockPlaceDurabilityPrompt(uuid).start();
                            return;
                        }
                    } catch (final NumberFormatException e) {
                        sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                ChatFormatting.RED + FabricLang.get("stageEditorNotListOfNumbers")
                                        .replace("<data>", s)));
                        new QuestBlockPlaceDurabilityPrompt(uuid).start();
                        return;
                    }
                }
                SessionData.set(uuid, pref + Key.S_PLACE_DURABILITY, durability);
            }
            new QuestBlocksPlaceListPrompt(uuid).start();
        }
    }

    public class QuestBlocksUseListPrompt extends FabricQuestsEditorIntegerPrompt {

        public QuestBlocksUseListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 5;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return FabricLang.get("stageEditorUseBlocks");
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
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorSetBlockNames");
            case 2:
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorSetBlockAmounts");
            case 3:
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorSetBlockDurability");
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
                if (SessionData.get(uuid, pref + Key.S_USE_NAMES) != null) {
                    final StringBuilder text = new StringBuilder();
                    final List<String> useNames = (List<String>) SessionData.get(uuid, pref + Key.S_USE_NAMES);
                    if (useNames != null) {
                        for (final String s : useNames) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA)
                                    .append(getPrettyItemName(s));
                        }
                    }
                    return text.toString();
                } else {
                    return "";
                }
            case 2:
                if (SessionData.get(uuid, pref + Key.S_USE_AMOUNTS) != null) {
                    final StringBuilder text = new StringBuilder();
                    final List<Integer> useAmounts = (List<Integer>) SessionData.get(uuid, pref + Key.S_USE_AMOUNTS);
                    if (useAmounts != null) {
                        for (final Integer i : useAmounts) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA).append(i);
                        }
                    }
                    return text.toString();
                } else {
                    return "";
                }
            case 3:
                if (SessionData.get(uuid, pref + Key.S_USE_DURABILITY) != null) {
                    final StringBuilder text = new StringBuilder();
                    final List<Short> useDurability = (List<Short>) SessionData.get(uuid, pref + Key.S_USE_DURABILITY);
                    if (useDurability != null) {
                        for (final Short s : useDurability) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA).append(s);
                        }
                    }
                    return text.toString();
                } else {
                    return "";
                }
            case 4:
            case 5:
                return "";
            default:
                return null;
            }
        }

        @Override
        public @NotNull String getPromptText() {
            final StringBuilder text = new StringBuilder(ChatFormatting.GOLD + "- " + getTitle() + " -");
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
                new QuestBlockUseNamesPrompt(uuid).start();
                break;
            case 2:
                new QuestBlockUseAmountsPrompt(uuid).start();
                break;
            case 3:
                new QuestBlockUseDurabilityPrompt(uuid).start();
                break;
            case 4:
                sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        ChatFormatting.YELLOW + FabricLang.get("stageEditorObjectiveCleared")));
                SessionData.set(uuid, pref + Key.S_USE_NAMES, null);
                SessionData.set(uuid, pref + Key.S_USE_AMOUNTS, null);
                SessionData.set(uuid, pref + Key.S_USE_DURABILITY, null);
                new QuestBlocksUseListPrompt(uuid).start();
                break;
            case 5:
                final int one;
                final int two;
                final List<Integer> names = (List<Integer>) SessionData.get(uuid, pref + Key.S_USE_NAMES);
                final List<Integer> amounts = (List<Integer>) SessionData.get(uuid, pref + Key.S_USE_AMOUNTS);
                if (names != null) {
                    one = names.size();
                } else {
                    one = 0;
                }
                if (amounts != null) {
                    two = amounts.size();
                } else {
                    two = 0;
                }
                if (one == two) {
                    final int missing;
                    LinkedList<Short> durability
                            = (LinkedList<Short>) SessionData.get(uuid, pref + Key.S_USE_DURABILITY);
                    if (durability != null) {
                        missing = one - durability.size();
                    } else {
                        missing = one;
                        durability = new LinkedList<>();
                    }
                    for (int i = 0; i < missing; i++) {
                        durability.add((short) 0);
                    }
                    SessionData.set(uuid, pref + Key.S_USE_DURABILITY, durability);
                    new FabricQuestBlocksPrompt(stageNum, uuid).start();
                } else {
                    sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            ChatFormatting.RED + FabricLang.get("listsNotSameSize")));
                    new QuestBlocksUseListPrompt(uuid).start();
                }
                break;
            default:
                new FabricQuestBlocksPrompt(stageNum, uuid).start();
                break;
            }
        }
    }

    public class QuestBlockUseNamesPrompt extends FabricQuestsEditorStringPrompt {

        public QuestBlockUseNamesPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorEnterBlockNames");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                final String[] args = input.split(" ");
                final LinkedList<String> names = new LinkedList<>();
                for (final String s : args) {
                    try {
                        final ResourceLocation resLoc = new ResourceLocation(s.toLowerCase());
                        final net.minecraft.world.level.block.Block block = Registry.BLOCK.get(resLoc);
                        if (block != null && block != net.minecraft.world.level.block.Blocks.AIR) {
                            names.add(resLoc.toString());
                        } else {
                            sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                    ChatFormatting.RED + FabricLang.get("stageEditorInvalidBlockName")
                                            .replace("<input>", s)));
                            new QuestBlockUseNamesPrompt(uuid).start();
                            return;
                        }
                    } catch (final Exception e) {
                        sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                ChatFormatting.RED + FabricLang.get("stageEditorNotListOfNumbers")
                                        .replace("<data>", s)));
                        new QuestBlockUseNamesPrompt(uuid).start();
                        return;
                    }
                }
                SessionData.set(uuid, pref + Key.S_USE_NAMES, names);

                LinkedList<Integer> amounts = new LinkedList<>();
                if (SessionData.get(uuid, pref + Key.S_USE_AMOUNTS) != null) {
                    amounts = (LinkedList<Integer>) SessionData.get(uuid, pref + Key.S_USE_AMOUNTS);
                }
                for (int i = 0; i < names.size(); i++) {
                    if (amounts != null) {
                        if (i >= amounts.size()) {
                            amounts.add(1);
                        }
                    }
                }
                SessionData.set(uuid, pref + Key.S_USE_AMOUNTS, amounts);
            }
            new QuestBlocksUseListPrompt(uuid).start();
        }
    }

    public class QuestBlockUseAmountsPrompt extends FabricQuestsEditorStringPrompt {

        public QuestBlockUseAmountsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorEnterBlockAmounts");
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
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                final String[] args = input.split(" ");
                final LinkedList<Integer> amounts = new LinkedList<>();
                for (final String s : args) {
                    try {
                        if (Integer.parseInt(s) > 0) {
                            amounts.add(Integer.parseInt(s));
                        } else {
                            sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                    ChatFormatting.RED + FabricLang.get("invalidMinimum").replace("<number>", "1")));
                            new QuestBlockUseAmountsPrompt(uuid).start();
                            return;
                        }
                    } catch (final NumberFormatException e) {
                        sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                ChatFormatting.RED + FabricLang.get("stageEditorNotListOfNumbers")
                                        .replace("<data>", s)));
                        new QuestBlockUseAmountsPrompt(uuid).start();
                        return;
                    }
                }
                SessionData.set(uuid, pref + Key.S_USE_AMOUNTS, amounts);
            }
            new QuestBlocksUseListPrompt(uuid).start();
        }
    }

    public class QuestBlockUseDurabilityPrompt extends FabricQuestsEditorStringPrompt {

        public QuestBlockUseDurabilityPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorEnterBlockDurability");
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
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                final String[] args = input.split(" ");
                final LinkedList<Short> durability = new LinkedList<>();
                for (final String s : args) {
                    try {
                        if (Short.parseShort(s) >= 0) {
                            durability.add(Short.parseShort(s));
                        } else {
                            sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                    ChatFormatting.RED + FabricLang.get("invalidMinimum").replace("<number>", "0")));
                            new QuestBlockUseDurabilityPrompt(uuid).start();
                            return;
                        }
                    } catch (final NumberFormatException e) {
                        sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                ChatFormatting.RED + FabricLang.get("stageEditorNotListOfNumbers")
                                        .replace("<data>", s)));
                        new QuestBlockUseDurabilityPrompt(uuid).start();
                        return;
                    }
                }
                SessionData.set(uuid, pref + Key.S_USE_DURABILITY, durability);
            }
            new QuestBlocksUseListPrompt(uuid).start();
        }
    }
}
