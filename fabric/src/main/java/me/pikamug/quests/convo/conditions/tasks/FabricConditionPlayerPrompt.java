/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.convo.conditions.tasks;

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.convo.conditions.FabricConditionsEditorIntegerPrompt;
import me.pikamug.quests.convo.conditions.FabricConditionsEditorStringPrompt;
import me.pikamug.quests.convo.conditions.main.FabricConditionMainPrompt;
import me.pikamug.quests.convo.generic.FabricItemStackPrompt;
import me.pikamug.quests.util.FabricItemUtil;
import me.pikamug.quests.util.FabricLang;
import me.pikamug.quests.util.FabricMiscUtil;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.SessionData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class FabricConditionPlayerPrompt extends FabricConditionsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final FabricQuestsPlugin plugin;

    public FabricConditionPlayerPrompt(final @NotNull UUID uuid) {
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
        return FabricLang.get("eventEditorPlayer");
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
            return ChatFormatting.YELLOW + FabricLang.get("conditionEditorPermissions");
        case 2:
            return ChatFormatting.YELLOW + FabricLang.get("conditionEditorItemsInMainHand");
        case 3:
            return ChatFormatting.YELLOW + FabricLang.get("conditionEditorItemsWear");
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
            if (SessionData.get(uuid, Key.C_WHILE_PERMISSION) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final List<String> whilePermission = (List<String>) SessionData.get(uuid, Key.C_WHILE_PERMISSION);
                if (whilePermission != null) {
                    for (final String s: whilePermission) {
                        // Replace standard period characters to prevent clickable links
                        text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.BLUE)
                                .append(s.replace(".", "\uFF0E"));
                    }
                }
                return text.toString();
            }
        case 2:
            if (SessionData.get(uuid, Key.C_WHILE_HOLDING_MAIN_HAND) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<ItemStack> whileHoldingMainHand
                        = (LinkedList<ItemStack>) SessionData.get(uuid, Key.C_WHILE_HOLDING_MAIN_HAND);
                if (whileHoldingMainHand != null) {
                    for (final ItemStack item : whileHoldingMainHand) {
                        text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.BLUE)
                                .append(FabricItemUtil.getName(item)).append(ChatFormatting.GRAY).append(" x ")
                                .append(ChatFormatting.AQUA).append(item.getCount());
                    }
                }
                return text.toString();
            }
        case 3:
            if (SessionData.get(uuid, Key.C_WHILE_WEARING) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<ItemStack> whileWearing
                        = (LinkedList<ItemStack>) SessionData.get(uuid, Key.C_WHILE_WEARING);
                if (whileWearing != null) {
                    for (final ItemStack item : whileWearing) {
                        text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.BLUE)
                                .append(FabricItemUtil.getName(item)).append(ChatFormatting.GRAY).append(" x ")
                                .append(ChatFormatting.AQUA).append(item.getCount());
                    }
                }
                return text.toString();
            }
        case 4:
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
            if (SessionData.get(uuid, Key.C_WHILE_HOLDING_MAIN_HAND) != null) {
                final List<ItemStack> items = (List<ItemStack>) SessionData.get(uuid, Key.C_WHILE_HOLDING_MAIN_HAND);
                if (items != null) {
                    items.add((ItemStack) SessionData.get(uuid, "tempStack"));
                    SessionData.set(uuid, Key.C_WHILE_HOLDING_MAIN_HAND, items);
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
        final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
        if (sender == null) {
            return;
        }
        switch(input.intValue()) {
        case 1:
            new ConditionPermissionsPrompt(uuid).start();
            break;
        case 2:
            new ConditionItemsInMainHandListPrompt(uuid).start();
            break;
        case 3:
            new ConditionItemsWearListPrompt(uuid).start();
            break;
        case 4:
            try {
                new FabricConditionMainPrompt(uuid).start();
            } catch (final Exception e) {
                sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("itemCreateCriticalError")));
            }
            break;
        default:
            new FabricConditionPlayerPrompt(uuid).start();
            break;
        }
    }

    public class ConditionPermissionsPrompt extends FabricConditionsEditorStringPrompt {

        public ConditionPermissionsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("conditionEditorPermissionsPrompt");
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
                final LinkedList<String> permissions = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    permissions.add(s.trim());
                }
                SessionData.set(uuid, Key.C_WHILE_PERMISSION, permissions);
            }
            new FabricConditionPlayerPrompt(uuid).start();
        }
    }

    public class ConditionItemsInMainHandListPrompt extends FabricConditionsEditorIntegerPrompt {

        public ConditionItemsInMainHandListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 3;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return FabricLang.get("conditionEditorItemsInMainHand");
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
                if (SessionData.get(uuid, Key.C_WHILE_HOLDING_MAIN_HAND) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<ItemStack> whileHoldingMainHand
                            = (List<ItemStack>) SessionData.get(uuid, Key.C_WHILE_HOLDING_MAIN_HAND);
                    if (whileHoldingMainHand != null) {
                        for (final ItemStack is : whileHoldingMainHand) {
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
        public String getPromptText() {
            // Check/add newly made item
            if (SessionData.get(uuid, "tempStack") != null) {
                if (SessionData.get(uuid, Key.C_WHILE_HOLDING_MAIN_HAND) != null) {
                    final List<ItemStack> items
                            = (List<ItemStack>) SessionData.get(uuid, Key.C_WHILE_HOLDING_MAIN_HAND);
                    if (items != null) {
                        items.add((ItemStack) SessionData.get(uuid, "tempStack"));
                        SessionData.set(uuid, Key.C_WHILE_HOLDING_MAIN_HAND, items);
                    }
                } else {
                    final LinkedList<ItemStack> items = new LinkedList<>();
                    items.add((ItemStack) SessionData.get(uuid, "tempStack"));
                    SessionData.set(uuid, Key.C_WHILE_HOLDING_MAIN_HAND, items);
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
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            switch(input.intValue()) {
            case 1:
                new FabricItemStackPrompt(uuid, ConditionItemsInMainHandListPrompt.this).start();
                break;
            case 2:
                sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("conditionEditorConditionCleared")));
                SessionData.set(uuid, Key.C_WHILE_HOLDING_MAIN_HAND, null);
                new ConditionItemsInMainHandListPrompt(uuid).start();
                break;
            case 3:
                new FabricConditionPlayerPrompt(uuid).start();
                break;
            default:
                new ConditionItemsInMainHandListPrompt(uuid).start();
                break;
            }
        }
    }

    public class ConditionItemsWearListPrompt extends FabricConditionsEditorIntegerPrompt {

        public ConditionItemsWearListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 3;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return FabricLang.get("conditionEditorItemsWear");
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
                    if (SessionData.get(uuid, Key.C_WHILE_WEARING) == null) {
                        return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                    } else {
                        final StringBuilder text = new StringBuilder();
                        final List<ItemStack> whileWearing
                                = (List<ItemStack>) SessionData.get(uuid, Key.C_WHILE_WEARING);
                        if (whileWearing != null) {
                            for (final ItemStack is : whileWearing) {
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
        public String getPromptText() {
            // Check/add newly made item
            if (SessionData.get(uuid, "tempStack") != null) {
                if (SessionData.get(uuid, Key.C_WHILE_WEARING) != null) {
                    final List<ItemStack> items
                            = (List<ItemStack>) SessionData.get(uuid, Key.C_WHILE_WEARING);
                    if (items != null) {
                        items.add((ItemStack) SessionData.get(uuid, "tempStack"));
                        SessionData.set(uuid, Key.C_WHILE_WEARING, items);
                    }
                } else {
                    final LinkedList<ItemStack> items = new LinkedList<>();
                    items.add((ItemStack) SessionData.get(uuid, "tempStack"));
                    SessionData.set(uuid, Key.C_WHILE_WEARING, items);
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
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            switch(input.intValue()) {
                case 1:
                    new FabricItemStackPrompt(uuid, ConditionItemsWearListPrompt.this).start();
                    break;
                case 2:
                    sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("conditionEditorConditionCleared")));
                    SessionData.set(uuid, Key.C_WHILE_WEARING, null);
                    new ConditionItemsWearListPrompt(uuid).start();
                    break;
                case 3:
                    new FabricConditionPlayerPrompt(uuid).start();
                    break;
                default:
                    new ConditionItemsWearListPrompt(uuid).start();
                    break;
            }
        }
    }
}
