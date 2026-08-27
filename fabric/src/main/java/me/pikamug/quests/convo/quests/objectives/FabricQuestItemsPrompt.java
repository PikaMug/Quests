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
import me.pikamug.quests.convo.quests.stages.FabricQuestStageMainPrompt;
import me.pikamug.quests.util.FabricItemUtil;
import me.pikamug.quests.util.FabricLang;
import me.pikamug.quests.util.FabricMiscUtil;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.SessionData;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class FabricQuestItemsPrompt extends FabricQuestsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final FabricQuestsPlugin plugin;
    private final int stageNum;
    private final String pref;

    public FabricQuestItemsPrompt(final int stageNum, final UUID uuid) {
        super(uuid);
        this.uuid = uuid;
        this.plugin = FabricQuestsPlugin.getInstance();
        this.stageNum = stageNum;
        this.pref = "stage" + stageNum;
    }

    private final int size = 6;

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public String getTitle() {
        return FabricLang.get("stageEditorItems");
    }

    @Override
    public ChatFormatting getNumberColor(final int number) {
        switch (number) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                return ChatFormatting.BLUE;
            case 6:
                return ChatFormatting.GREEN;
            default:
                return null;
        }
    }

    @Override
    public String getSelectionText(final int number) {
        switch(number) {
        case 1:
            return ChatFormatting.YELLOW + FabricLang.get("stageEditorCraftItems");
        case 2:
            return ChatFormatting.YELLOW + FabricLang.get("stageEditorSmeltItems");
        case 3:
            return ChatFormatting.YELLOW + FabricLang.get("stageEditorEnchantItems");
        case 4:
            return ChatFormatting.YELLOW + FabricLang.get("stageEditorBrewPotions");
        case 5:
            return ChatFormatting.YELLOW + FabricLang.get("stageEditorConsumeItems");
        case 6:
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
            if (SessionData.get(uuid, pref + Key.S_CRAFT_ITEMS) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<ItemStack> items
                        = (LinkedList<ItemStack>) SessionData.get(uuid, pref + Key.S_CRAFT_ITEMS);
                if (items != null) {
                    for (final ItemStack item : items) {
                        text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.BLUE)
                                .append(FabricItemUtil.getName(item)).append(ChatFormatting.GRAY).append(" x ")
                                .append(ChatFormatting.AQUA).append(item.getCount());
                    }
                }
                return text.toString();
            }
        case 2:
            if (SessionData.get(uuid, pref + Key.S_SMELT_ITEMS) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<ItemStack> items
                        = (LinkedList<ItemStack>) SessionData.get(uuid, pref + Key.S_SMELT_ITEMS);
                if (items != null) {
                    for (final ItemStack item : items) {
                        text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.BLUE)
                                .append(FabricItemUtil.getName(item)).append(ChatFormatting.GRAY).append(" x ")
                                .append(ChatFormatting.AQUA).append(item.getCount());
                    }
                }
                return text.toString();
            }
        case 3:
            if (SessionData.get(uuid, pref + Key.S_ENCHANT_ITEMS) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<ItemStack> items = (LinkedList<ItemStack>) SessionData.get(uuid, pref + Key.S_ENCHANT_ITEMS);
                if (items != null) {
                    for (final ItemStack item : items) {
                        text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.BLUE)
                                .append(FabricItemUtil.getName(item)).append(ChatFormatting.GRAY).append(" x ")
                                .append(ChatFormatting.AQUA).append(item.getCount());
                    }
                }
                return text.toString();
            }
        case 4:
            if (SessionData.get(uuid, pref + Key.S_BREW_ITEMS) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<ItemStack> items
                        = (LinkedList<ItemStack>) SessionData.get(uuid, pref + Key.S_BREW_ITEMS);
                if (items != null) {
                    for (final ItemStack item : items) {
                        text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.BLUE)
                                .append(FabricItemUtil.getName(item)).append(ChatFormatting.GRAY).append(" x ")
                                .append(ChatFormatting.AQUA).append(item.getCount());
                    }
                }
                return text.toString();
            }
        case 5:
            if (SessionData.get(uuid, pref + Key.S_CONSUME_ITEMS) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<ItemStack> items
                        = (LinkedList<ItemStack>) SessionData.get(uuid, pref + Key.S_CONSUME_ITEMS);
                if (items != null) {
                    for (final ItemStack item : items) {
                        text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.BLUE)
                                .append(FabricItemUtil.getName(item)).append(ChatFormatting.GRAY).append(" x ")
                                .append(ChatFormatting.AQUA).append(item.getCount());
                    }
                }
                return text.toString();
            }
        case 6:
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
            if (SessionData.get(uuid, pref + Key.S_CRAFT_ITEMS) != null) {
                final List<ItemStack> items = (List<ItemStack>) SessionData.get(uuid, pref + Key.S_CRAFT_ITEMS);
                if (items != null) {
                    items.add((ItemStack) SessionData.get(uuid, "tempStack"));
                    SessionData.set(uuid, pref + Key.S_CRAFT_ITEMS, items);
                }
            } else if (SessionData.get(uuid, pref + Key.S_SMELT_ITEMS) != null) {
                final List<ItemStack> items = (List<ItemStack>) SessionData.get(uuid, pref + Key.S_SMELT_ITEMS);
                if (items != null) {
                    items.add((ItemStack) SessionData.get(uuid, "tempStack"));
                    SessionData.set(uuid, pref + Key.S_SMELT_ITEMS, items);
                }
            } else if (SessionData.get(uuid, pref + Key.S_ENCHANT_ITEMS) != null) {
                final List<ItemStack> items = (List<ItemStack>) SessionData.get(uuid, pref + Key.S_ENCHANT_ITEMS);
                if (items != null) {
                    items.add((ItemStack) SessionData.get(uuid, "tempStack"));
                    SessionData.set(uuid, pref + Key.S_ENCHANT_ITEMS, items);
                }
            } else if (SessionData.get(uuid, pref + Key.S_BREW_ITEMS) != null) {
                final List<ItemStack> items = (List<ItemStack>) SessionData.get(uuid, pref + Key.S_BREW_ITEMS);
                if (items != null) {
                    items.add((ItemStack) SessionData.get(uuid, "tempStack"));
                    SessionData.set(uuid, pref + Key.S_BREW_ITEMS, items);
                }
            } else if (SessionData.get(uuid, pref + Key.S_CONSUME_ITEMS) != null) {
                final List<ItemStack> items = (List<ItemStack>) SessionData.get(uuid, pref + Key.S_CONSUME_ITEMS);
                if (items != null) {
                    items.add((ItemStack) SessionData.get(uuid, "tempStack"));
                    SessionData.set(uuid, pref + Key.S_CONSUME_ITEMS, items);
                }
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
    public void acceptInput(final Number input) {
        final net.minecraft.server.level.ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
        if (sender == null) {
            return;
        }
        switch(input.intValue()) {
        case 1:
            new QuestItemsCraftListPrompt(uuid).start();
            break;
        case 2:
            new QuestItemsSmeltListPrompt(uuid).start();
            break;
        case 3:
            new QuestItemsEnchantListPrompt(uuid).start();
            break;
        case 4:
            new QuestItemsBrewListPrompt(uuid).start();
            break;
        case 5:
            new QuestItemsConsumeListPrompt(uuid).start();
            break;
        case 6:
            try {
                new FabricQuestStageMainPrompt(stageNum, uuid).start();
            } catch (final Exception e) {
                sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        ChatFormatting.RED + FabricLang.get("itemCreateCriticalError")));
            }
            break;
        default:
            new FabricQuestItemsPrompt(stageNum, uuid).start();
            break;
        }
    }

    public class QuestItemsCraftListPrompt extends FabricQuestsEditorIntegerPrompt {

        public QuestItemsCraftListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 3;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return FabricLang.get("stageEditorCraftItems");
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
            switch(number) {
            case 1:
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorDeliveryAddItem");
            case 2:
                return ChatFormatting.RED + FabricLang.get("clear");
            case 3:
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
                if (SessionData.get(uuid, pref + Key.S_CRAFT_ITEMS) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<ItemStack> craftItems
                            = (List<ItemStack>) SessionData.get(uuid, pref + Key.S_CRAFT_ITEMS);
                    if (craftItems != null) {
                        for (final ItemStack is : craftItems) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ")
                                    .append(FabricItemUtil.getDisplayString(is));
                        }
                    }
                    return text.toString();
                }
            case 2:
            case 3:
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
                if (SessionData.get(uuid, pref + Key.S_CRAFT_ITEMS) != null) {
                    final List<ItemStack> items = (List<ItemStack>) SessionData.get(uuid, pref + Key.S_CRAFT_ITEMS);
                    if (items != null) {
                        items.add((ItemStack) SessionData.get(uuid, "tempStack"));
                        SessionData.set(uuid, pref + Key.S_CRAFT_ITEMS, items);
                    }
                } else {
                    final LinkedList<ItemStack> items = new LinkedList<>();
                    items.add((ItemStack) SessionData.get(uuid, "tempStack"));
                    SessionData.set(uuid, pref + Key.S_CRAFT_ITEMS, items);
                }
                FabricItemStackPrompt.clearSessionData(uuid);
            }

            final StringBuilder text = new StringBuilder(ChatFormatting.GOLD + "- " + getTitle() + " -");
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
                new FabricItemStackPrompt(uuid, QuestItemsCraftListPrompt.this).start();
                break;
            case 2:
                sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        ChatFormatting.YELLOW + FabricLang.get("stageEditorObjectiveCleared")));
                SessionData.set(uuid, pref + Key.S_CRAFT_ITEMS, null);
                new QuestItemsCraftListPrompt(uuid).start();
                break;
            default:
                new FabricQuestItemsPrompt(stageNum, uuid).start();
                break;
            }
        }
    }

    public class QuestItemsSmeltListPrompt extends FabricQuestsEditorIntegerPrompt {

        public QuestItemsSmeltListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 3;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return FabricLang.get("stageEditorSmeltItems");
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
            switch(number) {
            case 1:
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorDeliveryAddItem");
            case 2:
                return ChatFormatting.RED + FabricLang.get("clear");
            case 3:
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
                if (SessionData.get(uuid, pref + Key.S_SMELT_ITEMS) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<ItemStack> smeltItems
                            = (List<ItemStack>) SessionData.get(uuid, pref + Key.S_SMELT_ITEMS);
                    if (smeltItems != null) {
                        for (final ItemStack is : smeltItems) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ")
                                    .append(FabricItemUtil.getDisplayString(is));
                        }
                    }
                    return text.toString();
                }
            case 2:
            case 3:
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
                if (SessionData.get(uuid, pref + Key.S_SMELT_ITEMS) != null) {
                    final List<ItemStack> items = (List<ItemStack>) SessionData.get(uuid, pref + Key.S_SMELT_ITEMS);
                    if (items != null) {
                        items.add((ItemStack) SessionData.get(uuid, "tempStack"));
                        SessionData.set(uuid, pref + Key.S_SMELT_ITEMS, items);
                    }
                } else {
                    final LinkedList<ItemStack> items = new LinkedList<>();
                    items.add((ItemStack) SessionData.get(uuid, "tempStack"));
                    SessionData.set(uuid, pref + Key.S_SMELT_ITEMS, items);
                }
                FabricItemStackPrompt.clearSessionData(uuid);
            }

            final StringBuilder text = new StringBuilder(ChatFormatting.GOLD + "- " + getTitle() + " -");
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
                new FabricItemStackPrompt(uuid, QuestItemsSmeltListPrompt.this).start();
                break;
            case 2:
                sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        ChatFormatting.YELLOW + FabricLang.get("stageEditorObjectiveCleared")));
                SessionData.set(uuid, pref + Key.S_SMELT_ITEMS, null);
                new QuestItemsSmeltListPrompt(uuid).start();
                break;
            default:
                new FabricQuestItemsPrompt(stageNum, uuid).start();
                break;
            }
        }
    }

    public class QuestItemsEnchantListPrompt extends FabricQuestsEditorIntegerPrompt {

        public QuestItemsEnchantListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 3;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return FabricLang.get("stageEditorEnchantItems");
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
            switch(number) {
            case 1:
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorDeliveryAddItem");
            case 2:
                return ChatFormatting.RED + FabricLang.get("clear");
            case 3:
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
                if (SessionData.get(uuid, pref + Key.S_ENCHANT_ITEMS) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<ItemStack> enchantItems
                            = (List<ItemStack>) SessionData.get(uuid, pref + Key.S_ENCHANT_ITEMS);
                    if (enchantItems != null) {
                        for (final ItemStack is : enchantItems) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ")
                                    .append(FabricItemUtil.getDisplayString(is));
                        }
                    }
                    return text.toString();
                }
            case 2:
            case 3:
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
                if (SessionData.get(uuid, pref + Key.S_ENCHANT_ITEMS) != null) {
                    final List<ItemStack> items = (List<ItemStack>) SessionData.get(uuid, pref + Key.S_ENCHANT_ITEMS);
                    if (items != null) {
                        items.add((ItemStack) SessionData.get(uuid, "tempStack"));
                        SessionData.set(uuid, pref + Key.S_ENCHANT_ITEMS, items);
                    }
                } else {
                    final LinkedList<ItemStack> items = new LinkedList<>();
                    items.add((ItemStack) SessionData.get(uuid, "tempStack"));
                    SessionData.set(uuid, pref + Key.S_ENCHANT_ITEMS, items);
                }
                FabricItemStackPrompt.clearSessionData(uuid);
            }

            final StringBuilder text = new StringBuilder(ChatFormatting.GOLD + "- " + getTitle() + " -");
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
                new FabricItemStackPrompt(uuid, QuestItemsEnchantListPrompt.this).start();
                break;
            case 2:
                sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        ChatFormatting.YELLOW + FabricLang.get("stageEditorObjectiveCleared")));
                SessionData.set(uuid, pref + Key.S_ENCHANT_ITEMS, null);
                new QuestItemsEnchantListPrompt(uuid).start();
                break;
            default:
                new FabricQuestItemsPrompt(stageNum, uuid).start();
                break;
            }
        }
    }

    public class QuestItemsBrewListPrompt extends FabricQuestsEditorIntegerPrompt {

        public QuestItemsBrewListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 3;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return FabricLang.get("stageEditorBrewPotions");
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
            switch(number) {
            case 1:
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorDeliveryAddItem");
            case 2:
                return ChatFormatting.RED + FabricLang.get("clear");
            case 3:
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
                if (SessionData.get(uuid, pref + Key.S_BREW_ITEMS) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<ItemStack> brewItems = (List<ItemStack>) SessionData.get(uuid, pref + Key.S_BREW_ITEMS);
                    if (brewItems != null) {
                        for (final ItemStack is : brewItems) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ")
                                    .append(FabricItemUtil.getDisplayString(is));
                        }
                    }
                    return text.toString();
                }
            case 2:
            case 3:
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
                if (SessionData.get(uuid, pref + Key.S_BREW_ITEMS) != null) {
                    final List<ItemStack> items = (List<ItemStack>) SessionData.get(uuid, pref + Key.S_BREW_ITEMS);
                    if (items != null) {
                        items.add((ItemStack) SessionData.get(uuid, "tempStack"));
                        SessionData.set(uuid, pref + Key.S_BREW_ITEMS, items);
                    }
                } else {
                    final LinkedList<ItemStack> items = new LinkedList<>();
                    items.add((ItemStack) SessionData.get(uuid, "tempStack"));
                    SessionData.set(uuid, pref + Key.S_BREW_ITEMS, items);
                }
                FabricItemStackPrompt.clearSessionData(uuid);
            }

            final StringBuilder text = new StringBuilder(ChatFormatting.GOLD + "- " + getTitle() + " -");
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
                new FabricItemStackPrompt(uuid, QuestItemsBrewListPrompt.this).start();
                break;
            case 2:
                sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        ChatFormatting.YELLOW + FabricLang.get("stageEditorObjectiveCleared")));
                SessionData.set(uuid, pref + Key.S_BREW_ITEMS, null);
                new QuestItemsBrewListPrompt(uuid).start();
                break;
            default:
                new FabricQuestItemsPrompt(stageNum, uuid).start();
                break;
            }
        }
    }

    public class QuestItemsConsumeListPrompt extends FabricQuestsEditorIntegerPrompt {

        public QuestItemsConsumeListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 3;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return FabricLang.get("stageEditorConsumeItems");
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
            switch(number) {
            case 1:
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorDeliveryAddItem");
            case 2:
                return ChatFormatting.RED + FabricLang.get("clear");
            case 3:
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
                if (SessionData.get(uuid, pref + Key.S_CONSUME_ITEMS) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<ItemStack> consumeItems
                            = (List<ItemStack>) SessionData.get(uuid, pref + Key.S_CONSUME_ITEMS);
                    if (consumeItems != null) {
                        for (final ItemStack is : consumeItems) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ")
                                    .append(FabricItemUtil.getDisplayString(is));
                        }
                    }
                    return text.toString();
                }
            case 2:
            case 3:
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
                if (SessionData.get(uuid, pref + Key.S_CONSUME_ITEMS) != null) {
                    final List<ItemStack> items = (List<ItemStack>) SessionData.get(uuid, pref + Key.S_CONSUME_ITEMS);
                    if (items != null) {
                        items.add((ItemStack) SessionData.get(uuid, "tempStack"));
                        SessionData.set(uuid, pref + Key.S_CONSUME_ITEMS, items);
                    }
                } else {
                    final LinkedList<ItemStack> items = new LinkedList<>();
                    items.add((ItemStack) SessionData.get(uuid, "tempStack"));
                    SessionData.set(uuid, pref + Key.S_CONSUME_ITEMS, items);
                }
                FabricItemStackPrompt.clearSessionData(uuid);
            }

            final StringBuilder text = new StringBuilder(ChatFormatting.GOLD + "- " + getTitle() + " -");
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
                new FabricItemStackPrompt(uuid, QuestItemsConsumeListPrompt.this).start();
                break;
            case 2:
                sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        ChatFormatting.YELLOW + FabricLang.get("stageEditorObjectiveCleared")));
                SessionData.set(uuid, pref + Key.S_CONSUME_ITEMS, null);
                new QuestItemsConsumeListPrompt(uuid).start();
                break;
            default:
                new FabricQuestItemsPrompt(stageNum, uuid).start();
                break;
            }
        }
    }
}
