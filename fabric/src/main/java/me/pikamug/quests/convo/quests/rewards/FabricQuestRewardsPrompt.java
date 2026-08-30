/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.convo.quests.rewards;

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.convo.generic.FabricItemStackPrompt;
import me.pikamug.quests.convo.generic.FabricOverridePrompt;
import me.pikamug.quests.convo.quests.FabricQuestsEditorIntegerPrompt;
import me.pikamug.quests.convo.quests.FabricQuestsEditorStringPrompt;
import me.pikamug.quests.module.CustomReward;
import me.pikamug.quests.util.FabricItemUtil;
import me.pikamug.quests.util.FabricLang;
import me.pikamug.quests.util.FabricMiscUtil;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.SessionData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

public class FabricQuestRewardsPrompt extends FabricQuestsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final FabricQuestsPlugin plugin;
    private final String classPrefix;
    private boolean hasReward = false;
    private final int size = 12;

    public FabricQuestRewardsPrompt(final @NotNull UUID uuid) {
        super(uuid);
        this.uuid = uuid;
        this.plugin = FabricQuestsPlugin.getInstance();
        this.classPrefix = getClass().getSimpleName();
    }
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle() {
        return FabricLang.get("rewardsTitle").replace("<quest>", (String) Objects
                .requireNonNull(SessionData.get(uuid, Key.Q_NAME)));
    }
    
    @Override
    public ChatFormatting getNumberColor(final int number) {
        switch (number) {
        case 1:
            return ChatFormatting.GRAY;
        case 2:
        case 3:
        case 4:
        case 5:
        case 6:
        case 10:
            return ChatFormatting.BLUE;
        case 7:
            return ChatFormatting.GRAY;
        case 8:
            return ChatFormatting.GRAY;
        case 9:
            return ChatFormatting.GRAY;
        case 11:
            if (SessionData.get(uuid, Key.REW_DETAILS_OVERRIDE) == null) {
                if (!hasReward) {
                    return ChatFormatting.GRAY;
                } else {
                    return ChatFormatting.BLUE;
                }
            } else {
                return ChatFormatting.BLUE;
            }
        case 12:
            return ChatFormatting.GREEN;
        default:
            return null;
        }
    }
    
    @Override
    public String getSelectionText(final int number) {
        switch (number) {
        case 1:
            return ChatFormatting.GRAY + FabricLang.get("rewSetMoney");
        case 2:
            return ChatFormatting.YELLOW + FabricLang.get("rewSetQuestPoints").replace("<points>", FabricLang.get("questPoints"));
        case 3:
            return ChatFormatting.YELLOW + FabricLang.get("rewSetItems");
        case 4:
            return ChatFormatting.YELLOW + FabricLang.get("rewSetExperience");
        case 5:
            return ChatFormatting.YELLOW + FabricLang.get("rewSetCommands");
        case 6:
            return ChatFormatting.YELLOW + FabricLang.get("rewSetPermission");
        case 7:
            return ChatFormatting.GRAY + FabricLang.get("rewSetMcMMO");
        case 8:
            return ChatFormatting.GRAY + FabricLang.get("rewSetHeroes");
        case 9:
            return ChatFormatting.GRAY + FabricLang.get("rewSetPartiesExperience");
        case 10:
            return ChatFormatting.DARK_PURPLE + FabricLang.get("rewSetCustom");
        case 11:
            if (!hasReward) {
                return ChatFormatting.GRAY + FabricLang.get("overrideCreateSet");
            } else {
                return ChatFormatting.YELLOW + FabricLang.get("overrideCreateSet");
            }
        case 12:
            return ChatFormatting.YELLOW + FabricLang.get("done");
        default:
            return null;
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public String getAdditionalText(final int number) {
        switch (number) {
        case 1:
            return ChatFormatting.GRAY + "(" + FabricLang.get("notInstalled") + ")";
        case 2:
            if (SessionData.get(uuid, Key.REW_QUEST_POINTS) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + SessionData.get(uuid, Key.REW_QUEST_POINTS) + " "
                        + FabricLang.get("questPoints") + ChatFormatting.GRAY + ")";
            }
        case 3:
            if (SessionData.get(uuid, Key.REW_ITEMS) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<ItemStack> items = (LinkedList<ItemStack>) SessionData.get(uuid, Key.REW_ITEMS);
                if (items != null) {
                    for (final ItemStack item : items) {
                        if (item == null) {
                            text.append(ChatFormatting.RED).append("     - null\n");
                        } else {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.BLUE)
                                    .append(FabricItemUtil.getName(item)).append(ChatFormatting.GRAY).append(" x ")
                                    .append(ChatFormatting.AQUA).append(item.getCount());
                        }
                    }
                }
                return text.toString();
            }
        case 4:
            if (SessionData.get(uuid, Key.REW_EXP) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + SessionData.get(uuid, Key.REW_EXP) + " "
                        + FabricLang.get("points") + ChatFormatting.GRAY + ")";
            }
        case 5:
            if (SessionData.get(uuid, Key.REW_COMMAND) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final List<String> commands = (List<String>) SessionData.get(uuid, Key.REW_COMMAND);
                final List<String> overrides = (List<String>) SessionData.get(uuid, Key.REW_COMMAND_OVERRIDE_DISPLAY);
                int index = 0;
                if (commands != null) {
                    for (final String cmd : commands) {
                        text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA).append(cmd);
                        if (overrides != null) {
                            if (index < overrides.size()) {
                                text.append(ChatFormatting.GRAY).append(" (\"").append(ChatFormatting.AQUA)
                                        .append(overrides.get(index)).append(ChatFormatting.GRAY).append("\")");
                            }
                        }
                        index++;
                    }
                }
                return text.toString();
            }
        case 6:
            if (SessionData.get(uuid, Key.REW_PERMISSION) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final List<String> permissions = (List<String>) SessionData.get(uuid, Key.REW_PERMISSION);
                final List<String> worlds = (List<String>) SessionData.get(uuid, Key.REW_PERMISSION_WORLDS);
                int index = 0;
                if (permissions != null) {
                    for (final String perm : permissions) {
                        text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA).append(perm);
                        if (worlds != null) {
                            if (index < worlds.size()) {
                                text.append(ChatFormatting.GRAY).append("[").append(ChatFormatting.DARK_AQUA)
                                        .append(worlds.get(index)).append(ChatFormatting.GRAY).append("]");
                            }
                        }
                        index++;
                    }
                }
                return text.toString();
            }
        case 7:
            return ChatFormatting.GRAY + "(" + FabricLang.get("notInstalled") + ")";
        case 8:
            return ChatFormatting.GRAY + "(" + FabricLang.get("notInstalled") + ")";
        case 9:
            if (SessionData.get(uuid, Key.REW_PARTIES_EXPERIENCE) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + SessionData.get(uuid, Key.REW_PARTIES_EXPERIENCE) + " "
                        + FabricLang.get("points") + ChatFormatting.GRAY + ")";
            }
        case 10:
            if (SessionData.get(uuid, Key.REW_CUSTOM) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> customRew = (LinkedList<String>) SessionData.get(uuid, Key.REW_CUSTOM);
                if (customRew != null) {
                    for (final String s : customRew) {
                        text.append("\n").append(ChatFormatting.LIGHT_PURPLE).append("     - ").append(s);
                    }
                }
                return text.toString();
            }
        case 11:
            if (SessionData.get(uuid, Key.REW_DETAILS_OVERRIDE) == null) {
                if (!hasReward) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("stageEditorOptional") + ")";
                } else {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                }
            } else {
                final StringBuilder text = new StringBuilder();
                final List<String> overrides = (List<String>) SessionData.get(uuid, Key.REW_DETAILS_OVERRIDE);
                if (overrides != null) {
                    for (final String override : overrides) {
                        text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA)
                                .append(override);
                    }
                }
                return text.toString();
            }
        case 12:
            return "";
        default:
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull String getPromptText() {
        final String input = (String) SessionData.get(uuid, classPrefix + "-override");
        if (input != null && !input.equalsIgnoreCase(FabricLang.get("cancel"))) {
            if (input.equalsIgnoreCase(FabricLang.get("clear"))) {
                SessionData.set(uuid, Key.REW_DETAILS_OVERRIDE, null);
            } else {
                final LinkedList<String> overrides = new LinkedList<>();
                if (SessionData.get(uuid, Key.REW_DETAILS_OVERRIDE) != null) {
                    overrides.addAll((List<String>) SessionData.get(uuid, Key.REW_DETAILS_OVERRIDE));
                }
                overrides.add(input);
                SessionData.set(uuid, Key.REW_DETAILS_OVERRIDE, overrides);
                SessionData.set(uuid, classPrefix + "-override", null);
            }
        }
        checkReward();

        final StringBuilder text = new StringBuilder(ChatFormatting.LIGHT_PURPLE + "- " + getTitle()
                .replace((String) Objects.requireNonNull(SessionData.get(uuid, Key.Q_NAME)), ChatFormatting.AQUA
                + (String) SessionData.get(uuid, Key.Q_NAME) + ChatFormatting.LIGHT_PURPLE) + " -");
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
        switch (input.intValue()) {
        case 1:
            new QuestRewardsMoneyPrompt(uuid).start();
            break;
        case 2:
            new QuestRewardsQuestPointsPrompt(uuid).start();
            break;
        case 3:
            new QuestRewardsItemListPrompt(uuid).start();
            break;
        case 4:
            new QuestRewardsExperiencePrompt(uuid).start();
            break;
        case 5:
            if (!plugin.hasLimitedAccess(uuid)) {
                new QuestRewardsCommandsPrompt(uuid).start();
            } else {
                sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("noPermission")));
                new FabricQuestRewardsPrompt(uuid).start();
            }
            break;
        case 6:
            if (!plugin.hasLimitedAccess(uuid)) {
                new QuestRewardsPermissionsListPrompt(uuid).start();
            } else {
                sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("noPermission")));
                new FabricQuestRewardsPrompt(uuid).start();
            }
            break;
        case 7:
            new FabricQuestRewardsPrompt(uuid).start();
            break;
        case 8:
            new FabricQuestRewardsPrompt(uuid).start();
            break;
        case 9:
            new QuestRewardsPartiesExperiencePrompt(uuid).start();
            break;
        case 10:
            new QuestCustomRewardModulePrompt(uuid).start();
            break;
        case 11:
            if (hasReward) {
                new FabricOverridePrompt(uuid, this, FabricLang.get("overrideCreateEnter")).start();
            } else {
                sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("invalidOption")));
                new FabricQuestRewardsPrompt(uuid).start();
            }
            break;
        case 12:
            plugin.getQuestFactory().returnToMenu(uuid);
            break;
        default:
            new FabricQuestRewardsPrompt(uuid).start();
            break;
        }
    }
    
    public boolean checkReward() {
        if (SessionData.get(uuid, Key.REW_MONEY) != null
                || SessionData.get(uuid, Key.REW_QUEST_POINTS) != null
                || SessionData.get(uuid, Key.REW_ITEMS) != null
                || SessionData.get(uuid, Key.REW_EXP) != null
                || SessionData.get(uuid, Key.REW_COMMAND) != null
                || SessionData.get(uuid, Key.REW_PERMISSION) != null
                || SessionData.get(uuid, Key.REW_MCMMO_SKILLS) != null
                || SessionData.get(uuid, Key.REW_HEROES_CLASSES) != null
                || SessionData.get(uuid, Key.REW_PARTIES_EXPERIENCE) != null
                || SessionData.get(uuid, Key.REW_PHAT_LOOTS) != null
                || SessionData.get(uuid, Key.REW_CUSTOM) != null) {
            hasReward = true;
            return true;
        }
        return false;
    }

    public class QuestRewardsMoneyPrompt extends FabricQuestsEditorStringPrompt {
        
        public QuestRewardsMoneyPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("rewMoneyPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            String text = getQueryText();
            return ChatFormatting.YELLOW + text;
        }
        
        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel")) && !input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i > 0) {
                        SessionData.set(uuid, Key.REW_MONEY, i);
                    } else {
                        if (sender != null) {
                            sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("inputPosNum")));
                        }
                        new QuestRewardsMoneyPrompt(uuid).start();
                        return;
                    }
                } catch (final NumberFormatException e) {
                    if (sender != null) {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("reqNotANumber")
                                .replace("<input>", input)));
                    }
                    new QuestRewardsMoneyPrompt(uuid).start();
                    return;
                }
            } else if (input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.REW_MONEY, null);
                new FabricQuestRewardsPrompt(uuid).start();
                return;
            }
            new FabricQuestRewardsPrompt(uuid).start();
        }
    }

    public class QuestRewardsQuestPointsPrompt extends FabricQuestsEditorStringPrompt {
        
        public QuestRewardsQuestPointsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("rewQuestPointsPrompt").replace("<points>", FabricLang.get("questPoints"));
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
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel")) && !input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i > 0) {
                        SessionData.set(uuid, Key.REW_QUEST_POINTS, i);
                    } else {
                        if (sender != null) {
                            sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("inputPosNum")));
                        }
                        new QuestRewardsQuestPointsPrompt(uuid).start();
                        return;
                    }
                } catch (final NumberFormatException e) {
                    if (sender != null) {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("reqNotANumber")
                                .replace("<input>", input)));
                    }
                    new QuestRewardsQuestPointsPrompt(uuid).start();
                    return;
                }
            } else if (input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.REW_QUEST_POINTS, null);
                new FabricQuestRewardsPrompt(uuid).start();
                return;
            }
            new FabricQuestRewardsPrompt(uuid).start();
        }
    }

    public class QuestRewardsItemListPrompt extends FabricQuestsEditorIntegerPrompt {

        public QuestRewardsItemListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        private final int size = 3;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle() {
            return FabricLang.get("itemRewardsTitle");
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
                if (SessionData.get(uuid, Key.REW_ITEMS) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<ItemStack> items = (List<ItemStack>) SessionData.get(uuid, Key.REW_ITEMS);
                    if (items != null) {
                        for (final ItemStack is : items) {
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
                if (SessionData.get(uuid, Key.REW_ITEMS) != null) {
                    final List<ItemStack> itemRew = (List<ItemStack>) SessionData.get(uuid, Key.REW_ITEMS);
                    if (itemRew != null) {
                        itemRew.add((ItemStack) SessionData.get(uuid, "tempStack"));
                        SessionData.set(uuid, Key.REW_ITEMS, itemRew);
                    }
                } else {
                    final List<ItemStack> itemRew = new LinkedList<>();
                    itemRew.add((ItemStack) SessionData.get(uuid, "tempStack"));
                    SessionData.set(uuid, Key.REW_ITEMS, itemRew);
                }
                FabricItemStackPrompt.clearSessionData(uuid);
            }

            final StringBuilder text = new StringBuilder(ChatFormatting.AQUA + getTitle());
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
            switch (input.intValue()) {
            case 1:
                new FabricItemStackPrompt(uuid, QuestRewardsItemListPrompt.this).start();
                break;
            case 2:
                sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("rewItemsCleared")));
                SessionData.set(uuid, Key.REW_ITEMS, null);
                new QuestRewardsItemListPrompt(uuid).start();
                break;
            case 3:
                new FabricQuestRewardsPrompt(uuid).start();
                break;
            default:
                new QuestRewardsItemListPrompt(uuid).start();
                break;
            }
        }
    }

    public class QuestRewardsExperiencePrompt extends FabricQuestsEditorStringPrompt {

        public QuestRewardsExperiencePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("rewExperiencePrompt");
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
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel")) && !input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i > 0) {
                        SessionData.set(uuid, Key.REW_EXP, i);
                    } else {
                        if (sender != null) {
                            sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("inputPosNum")));
                        }
                        new QuestRewardsExperiencePrompt(uuid).start();
                        return;
                    }
                } catch (final NumberFormatException e) {
                    if (sender != null) {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("reqNotANumber")
                                .replace("<input>", input)));
                    }
                    new QuestRewardsExperiencePrompt(uuid).start();
                    return;
                }
            } else if (input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.REW_EXP, null);
                new FabricQuestRewardsPrompt(uuid).start();
                return;
            }
            new FabricQuestRewardsPrompt(uuid).start();
        }
    }
    
    public class QuestRewardsCommandsPrompt extends FabricQuestsEditorStringPrompt {
        
        public QuestRewardsCommandsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("rewCommandPrompt");
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
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel")) && !input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                final String[] args = input.split(FabricLang.get("charSemi"));
                final List<String> commands = new LinkedList<>();
                for (String s : args) {
                    if (s.startsWith("/")) {
                        s = s.substring(1);
                    }
                    switch (s.trim().split(" ")[0].toLowerCase()) {
                    case "ban":
                    case "ban-ip":
                    case "deop":
                    case "kick":
                    case "kill":
                    case "timings":
                    case "op": 
                    case "pardon":
                    case "pardon-ip":
                    case "reload":
                    case "stop":
                    case "we":
                    case "whitelist":
                    case "worldedit":
                        if (sender != null) {
                            sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("invalidOption")
                                    + ChatFormatting.DARK_RED + " (" + s.trim() + ")"));
                        }
                        continue;
                    default:
                        commands.add(s.trim());
                    }
                }
                SessionData.set(uuid, Key.REW_COMMAND, commands.isEmpty() ? null : commands);
            } else if (input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.REW_COMMAND, null);
            }
            new FabricQuestRewardsPrompt(uuid).start();
        }
    }
    
    public class QuestRewardsPermissionsListPrompt extends FabricQuestsEditorIntegerPrompt {

        public QuestRewardsPermissionsListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        private final int size = 4;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle() {
            return FabricLang.get("permissionRewardsTitle");
        }
        
        @Override
        public ChatFormatting getNumberColor(final int number) {
            switch (number) {
            case 1:
                return ChatFormatting.BLUE;
            case 2:
                if (SessionData.get(uuid, Key.REW_PERMISSION) == null) {
                    return ChatFormatting.GRAY;
                } else {
                    return ChatFormatting.BLUE;
                }
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
            switch (number) {
            case 1:
                return ChatFormatting.YELLOW + FabricLang.get("rewSetPermission");
            case 2:
                if (SessionData.get(uuid, Key.REW_PERMISSION) == null) {
                    return ChatFormatting.GRAY + FabricLang.get("rewSetPermissionWorlds");
                } else {
                    return ChatFormatting.YELLOW + FabricLang.get("rewSetPermissionWorlds");
                }
            case 3:
                return ChatFormatting.RED + FabricLang.get("clear");
            case 4:
                return ChatFormatting.GREEN + FabricLang.get("done");
            default:
                return null;
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public String getAdditionalText(final int number) {
            switch (number) {
            case 1:
                if (SessionData.get(uuid, Key.REW_PERMISSION) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> permission = (List<String>) SessionData.get(uuid, Key.REW_PERMISSION);
                    if (permission != null) {
                        for (final String s : permission) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA).append(s);
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (SessionData.get(uuid, Key.REW_PERMISSION) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    if (SessionData.get(uuid, Key.REW_PERMISSION_WORLDS) == null) {
                        return ChatFormatting.YELLOW + "(" + FabricLang.get("stageEditorOptional") + ")";
                    } else {
                        final StringBuilder text = new StringBuilder();
                        final List<String> permissionWorlds
                                = (List<String>) SessionData.get(uuid, Key.REW_PERMISSION_WORLDS);
                        if (permissionWorlds != null) {
                            for (final String s : permissionWorlds) {
                                text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA)
                                        .append(s);
                            }
                        }
                        return text.toString();
                    }
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
            final StringBuilder text = new StringBuilder(ChatFormatting.GOLD + getTitle());
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
            switch (input.intValue()) {
            case 1:
                new QuestPermissionsPrompt(uuid).start();
                break;
            case 2:
                new QuestPermissionsWorldsPrompt(uuid).start();
                break;
            case 3:
                sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("rewPermissionsCleared")));
                SessionData.set(uuid, Key.REW_PERMISSION, null);
                SessionData.set(uuid, Key.REW_PERMISSION_WORLDS, null);
                new QuestRewardsPermissionsListPrompt(uuid).start();
                break;
            case 4:
                new FabricQuestRewardsPrompt(uuid).start();
                break;
            default:
                new FabricQuestRewardsPrompt(uuid).start();
                break;
            }
        }
        
    }

    public class QuestPermissionsPrompt extends FabricQuestsEditorStringPrompt {
        
        public QuestPermissionsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("rewPermissionsPrompt");
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
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel")) && !input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                final String[] args = input.split(" ");
                final List<String> permissions = new LinkedList<>();
                for (String s : args) {
                    if (s.startsWith("/")) {
                        s = s.substring(1);
                    }
                    final String[] arr = {
                            "*",
                            "bukkit.*",
                            "bukkit.command",
                            "fawe",
                            "minecraft.*",
                            "minecraft.command",
                            "quests",
                            "vault",
                            "worledit"
                    };
                    boolean found = false;
                    for (final String value : arr) {
                        if (s.startsWith(value)) {
                            found = true;
                            break;
                        }
                    } 
                    if (found) {
                        if (sender != null) {
                            sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("invalidOption")
                            + ChatFormatting.DARK_RED + " (" + s.trim() + ")"));
                        }
                    } else {
                        permissions.add(s.trim());
                    }
                }
                SessionData.set(uuid, Key.REW_PERMISSION, permissions);
            } else if (input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.REW_PERMISSION, null);
            }
            new QuestRewardsPermissionsListPrompt(uuid).start();
        }
    }
    
    public class QuestPermissionsWorldsPrompt extends FabricQuestsEditorStringPrompt {
        
        public QuestPermissionsWorldsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("rewPermissionsWorldPrompt");
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
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel")) && !input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                final String[] args = input.split(FabricLang.get("charSemi"));
                final List<String> worlds = new LinkedList<>(Arrays.asList(args));
                SessionData.set(uuid, Key.REW_PERMISSION_WORLDS, worlds);
            } else if (input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.REW_PERMISSION_WORLDS, null);
            }
            new QuestRewardsPermissionsListPrompt(uuid).start();
        }
    }

    public class QuestRewardsMcMMOListPrompt extends FabricQuestsEditorIntegerPrompt {

        public QuestRewardsMcMMOListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        private final int size = 4;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle() {
            return FabricLang.get("mcMMORewardsTitle");
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
                return ChatFormatting.YELLOW + FabricLang.get("reqSetSkills");
            case 2:
                return ChatFormatting.YELLOW + FabricLang.get("reqSetSkillAmounts");
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
                if (SessionData.get(uuid, Key.REW_MCMMO_SKILLS) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> skills = (List<String>) SessionData.get(uuid, Key.REW_MCMMO_SKILLS);
                    if (skills != null) {
                        for (final String s : skills) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA).append(s);
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (SessionData.get(uuid, Key.REW_MCMMO_AMOUNTS) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<Integer> amounts = (List<Integer>) SessionData.get(uuid, Key.REW_MCMMO_AMOUNTS);
                    if (amounts != null) {
                        for (final Integer i : amounts) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA).append(i);
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
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            switch(input.intValue()) {
            case 1:
                new QuestMcMMOSkillsPrompt(uuid).start();
                break;
            case 2:
                if (SessionData.get(uuid, Key.REW_MCMMO_SKILLS) == null) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("rewSetMcMMOSkillsFirst")));
                    new QuestRewardsMcMMOListPrompt(uuid).start();
                } else {
                    new QuestMcMMOAmountsPrompt(uuid).start();
                }
                break;
            case 3:
                sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("rewMcMMOCleared")));
                SessionData.set(uuid, Key.REW_MCMMO_SKILLS, null);
                SessionData.set(uuid, Key.REW_MCMMO_AMOUNTS, null);
                new QuestRewardsMcMMOListPrompt(uuid).start();
                break;
            case 4:
                final int one;
                final int two;
                final List<Integer> skills = (List<Integer>) SessionData.get(uuid, Key.REW_MCMMO_SKILLS);
                final List<Integer> amounts = (List<Integer>) SessionData.get(uuid, Key.REW_MCMMO_AMOUNTS);
                if (skills != null) {
                    one = skills.size();
                } else {
                    one = 0;
                }
                if (amounts != null) {
                    two = amounts.size();
                } else {
                    two = 0;
                }
                if (one == two) {
                    new FabricQuestRewardsPrompt(uuid).start();
                } else {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("listsNotSameSize")));
                    new QuestRewardsMcMMOListPrompt(uuid).start();
                }
                break;
            default:
                new QuestRewardsMcMMOListPrompt(uuid).start();
                break;
            }
        }
    }

    public class QuestMcMMOSkillsPrompt extends FabricQuestsEditorStringPrompt {
        
        public QuestMcMMOSkillsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return FabricLang.get("skillListTitle");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("rewMcMMOPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final StringBuilder skillList = new StringBuilder(ChatFormatting.LIGHT_PURPLE + getTitle() + "\n");
            skillList.append(ChatFormatting.GRAY).append("(").append(FabricLang.get("none")).append(")");
            skillList.append("\n").append(ChatFormatting.YELLOW).append(getQueryText());
            return skillList.toString();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                final String[] args = input.split(" ");
                final List<String> skills = new LinkedList<>();
                for (final String s : args) {
                    if (!skills.contains(s)) {
                        skills.add(FabricMiscUtil.getCapitalized(s));
                    } else {
                        if (sender != null) {
                            sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("listDuplicate")));
                        }
                        new QuestMcMMOSkillsPrompt(uuid).start();
                        return;
                    }
                }
                SessionData.set(uuid, Key.REW_MCMMO_SKILLS, skills);
            }
            new QuestRewardsMcMMOListPrompt(uuid).start();
        }
    }

    public class QuestMcMMOAmountsPrompt extends FabricQuestsEditorStringPrompt {
        
        public QuestMcMMOAmountsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("reqMcMMOAmountsPrompt");
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
                final List<Integer> amounts = new LinkedList<>();
                for (final String s : args) {
                    try {
                        amounts.add(Integer.parseInt(s));
                    } catch (final NumberFormatException e) {
                        String text = FabricLang.get("reqNotANumber");
                        text = text.replace("<input>", s);
                        if (sender != null) {
                            sender.sendSystemMessage(Component.literal(ChatFormatting.RED + text));
                        }
                        new QuestMcMMOAmountsPrompt(uuid).start();
                        return;
                    }
                }
                SessionData.set(uuid, Key.REW_MCMMO_AMOUNTS, amounts);
            }
            new QuestRewardsMcMMOListPrompt(uuid).start();
        }
    }

    public class QuestRewardsHeroesListPrompt extends FabricQuestsEditorIntegerPrompt {

        public QuestRewardsHeroesListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        private final int size = 4;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle() {
            return FabricLang.get("heroesRewardsTitle");
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
                return ChatFormatting.YELLOW + FabricLang.get("rewSetHeroesClasses");
            case 2:
                return ChatFormatting.YELLOW + FabricLang.get("rewSetHeroesAmounts");
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
                if (SessionData.get(uuid, Key.REW_HEROES_CLASSES) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> classes = (List<String>) SessionData.get(uuid, Key.REW_HEROES_CLASSES);
                    if (classes != null) {
                        for (final String s : classes) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA).append(s);
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (SessionData.get(uuid, Key.REW_HEROES_AMOUNTS) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<Double> amounts = (List<Double>) SessionData.get(uuid, Key.REW_HEROES_AMOUNTS);
                    if (amounts != null) {
                        for (final Double d : amounts) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA).append(d);
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
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            switch(input.intValue()) {
            case 1:
                new QuestHeroesClassesPrompt(uuid).start();
                break;
            case 2:
                if (SessionData.get(uuid, Key.REW_HEROES_CLASSES) == null) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("rewSetHeroesClassesFirst")));
                    new QuestRewardsHeroesListPrompt(uuid).start();
                } else {
                    new QuestHeroesExperiencePrompt(uuid).start();
                }
                break;
            case 3:
                sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("rewHeroesCleared")));
                SessionData.set(uuid, Key.REW_HEROES_CLASSES, null);
                SessionData.set(uuid, Key.REW_HEROES_AMOUNTS, null);
                new QuestRewardsHeroesListPrompt(uuid).start();
                break;
            case 4:
                final int one;
                final int two;
                final List<Integer> classes = (List<Integer>) SessionData.get(uuid, Key.REW_HEROES_CLASSES);
                final List<Double> amounts = (List<Double>) SessionData.get(uuid, Key.REW_HEROES_AMOUNTS);
                if (classes != null) {
                    one = classes.size();
                } else {
                    one = 0;
                }
                if (amounts != null) {
                    two = amounts.size();
                } else {
                    two = 0;
                }
                if (one == two) {
                    new FabricQuestRewardsPrompt(uuid).start();
                } else {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("listsNotSameSize")));
                    new QuestRewardsHeroesListPrompt(uuid).start();
                }
                break;
            default:
                new QuestRewardsHeroesListPrompt(uuid).start();
                break;
            }
        }
    }

    public class QuestHeroesClassesPrompt extends FabricQuestsEditorStringPrompt {
        
        public QuestHeroesClassesPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return FabricLang.get("heroesClassesTitle");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("rewHeroesClassesPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final StringBuilder text = new StringBuilder(ChatFormatting.LIGHT_PURPLE + getTitle() + "\n");
            text.append(ChatFormatting.GRAY).append("(").append(FabricLang.get("none")).append(")\n");
            text.append("\n").append(ChatFormatting.YELLOW).append(getQueryText());
            return text.toString();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                final String[] arr = input.split(" ");
                final List<String> classes = new LinkedList<>();
                for (final String s : arr) {
                    classes.add(s);
                }
                SessionData.set(uuid, Key.REW_HEROES_CLASSES, classes);
            }
            new QuestRewardsHeroesListPrompt(uuid).start();
        }
    }

    public class QuestHeroesExperiencePrompt extends FabricQuestsEditorStringPrompt {
        
        public QuestHeroesExperiencePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return FabricLang.get("heroesExperienceTitle");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("rewHeroesExperiencePrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            String text = getTitle() + "\n";
            text += ChatFormatting.YELLOW + getQueryText();
            return text;
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                final String[] arr = input.split(" ");
                final List<Double> amounts = new LinkedList<>();
                for (final String s : arr) {
                    try {
                        final double d = Double.parseDouble(s);
                        amounts.add(d);
                    } catch (final NumberFormatException nfe) {
                        String text = FabricLang.get("reqNotANumber");
                        text = text.replace("<input>", s);
                        if (sender != null) {
                            sender.sendSystemMessage(Component.literal(ChatFormatting.RED + text));
                        }
                        new QuestHeroesExperiencePrompt(uuid).start();
                        return;
                    }
                }
                SessionData.set(uuid, Key.REW_HEROES_AMOUNTS, amounts);
            }
            new QuestRewardsHeroesListPrompt(uuid).start();
        }
    }
    
    public class QuestRewardsPartiesExperiencePrompt extends FabricQuestsEditorStringPrompt {
        
        public QuestRewardsPartiesExperiencePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return null;
        }
        
        @Override
        public String getQueryText() {
            return FabricLang.get("rewPartiesExperiencePrompt");
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
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel")) && !input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i > 0) {
                        SessionData.set(uuid, Key.REW_PARTIES_EXPERIENCE, i);
                    } else {
                        if (sender != null) {
                            sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("inputPosNum")));
                        }
                        new QuestRewardsPartiesExperiencePrompt(uuid).start();
                        return;
                    }
                } catch (final NumberFormatException e) {
                    if (sender != null) {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("reqNotANumber")
                                .replace("<input>", input)));
                    }
                    new QuestRewardsPartiesExperiencePrompt(uuid).start();
                    return;
                }
            } else if (input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.REW_PARTIES_EXPERIENCE, null);
                new FabricQuestRewardsPrompt(uuid).start();
                return;
            }
            new FabricQuestRewardsPrompt(uuid).start();
        }
    }

    public class QuestCustomRewardModulePrompt extends FabricQuestsEditorStringPrompt {

        public QuestCustomRewardModulePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return FabricLang.get("stageEditorModules");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorModulePrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            if (FabricMiscUtil.getPlayer(uuid, plugin) == null || !plugin.getConfigSettings().canClickablePrompts()) {
                final StringBuilder text = new StringBuilder(ChatFormatting.LIGHT_PURPLE + getTitle() + "\n");
                if (plugin.getCustomRewards().isEmpty()) {
                    text.append(ChatFormatting.DARK_AQUA).append(ChatFormatting.UNDERLINE)
                            .append("https://pikamug.gitbook.io/quests/casual/modules").append(ChatFormatting.RESET)
                            .append("\n");
                    text.append(ChatFormatting.RED).append("(").append(FabricLang.get("stageEditorNoModules")).append(")")
                            .append("\n");
                } else {
                    for (final String name : plugin.getCustomRewards().stream().map(CustomReward::getModuleName)
                            .collect(Collectors.toCollection(TreeSet::new))) {
                        text.append(ChatFormatting.DARK_PURPLE).append("  - ").append(name).append("\n");
                    }
                }
                return text.toString() + ChatFormatting.YELLOW + getQueryText();
            }
            final MutableComponent component = Component.literal(getTitle() + "\n")
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE));
            final MutableComponent line = Component.literal("");
            if (plugin.getCustomRewards().isEmpty()) {
                final MutableComponent link = Component.literal("https://pikamug.gitbook.io/quests/casual/modules\n")
                        .withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_AQUA).withUnderlined(true));
                line.append(link);
                line.append(Component.literal(ChatFormatting.RED + "(" + FabricLang.get("stageEditorNoModules") + ")\n"));
            } else {
                for (final String name : plugin.getCustomRewards().stream().map(CustomReward::getModuleName)
                        .collect(Collectors.toCollection(TreeSet::new))) {
                    final MutableComponent click = Component.literal(ChatFormatting.DARK_PURPLE + "  - " + name + "\n")
                            .withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                    "/quests choice " + name)));
                    line.append(click);
                }
            }
            component.append(line);
            component.append(Component.literal(ChatFormatting.YELLOW + getQueryText()));
            FabricMiscUtil.getPlayer(uuid, plugin).sendSystemMessage(component);
            return "";
        }

        @Override
        public void acceptInput(@Nullable final String input) {
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            if (input != null && !input.equalsIgnoreCase(FabricLang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                String found = null;
                for (final CustomReward cr : plugin.getCustomRewards()) {
                    if (cr.getModuleName().equalsIgnoreCase(input)) {
                        found = cr.getModuleName();
                        break;
                    }
                }
                if (found == null) {
                    for (final CustomReward cr : plugin.getCustomRewards()) {
                        if (cr.getModuleName().toLowerCase().contains(input.toLowerCase())) {
                            found = cr.getModuleName();
                            break;
                        }
                    }
                }
                if (found != null) {
                    new QuestCustomRewardsPrompt(found, uuid).start();
                    return;
                }
            } else if (input != null && input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                new FabricQuestRewardsPrompt(uuid).start();
                return;
            } else if (input != null && input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.REW_CUSTOM, null);
                SessionData.set(uuid, Key.REW_CUSTOM_DATA, null);
                SessionData.set(uuid, Key.REW_CUSTOM_DATA_TEMP, null);
                sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("rewCustomCleared")));
                new FabricQuestRewardsPrompt(uuid).start();
                return;
            }
            sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("rewCustomNotFound")));
            new QuestCustomRewardModulePrompt(uuid).start();
        }
    }

    public class QuestCustomRewardsPrompt extends FabricQuestsEditorStringPrompt {

        private final String moduleName;
        
        public QuestCustomRewardsPrompt(final String moduleName, final UUID uuid) {
            super(uuid);
            this.moduleName = moduleName;
        }

        public String getModuleName() {
            return moduleName;
        }

        @Override
        public String getTitle() {
            return FabricLang.get("customRewardsTitle");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("rewCustomRewardPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            if (FabricMiscUtil.getPlayer(uuid, plugin) == null || !plugin.getConfigSettings().canClickablePrompts()) {
                final StringBuilder text = new StringBuilder(ChatFormatting.LIGHT_PURPLE + getTitle() + "\n");
                if (plugin.getCustomRewards().isEmpty()) {
                    text.append(ChatFormatting.DARK_AQUA).append(ChatFormatting.UNDERLINE)
                            .append("https://pikamug.gitbook.io/quests/casual/modules\n");
                    text.append(ChatFormatting.RED).append("(").append(FabricLang.get("stageEditorNoModules")).append(")\n");
                } else {
                    for (final CustomReward cr : plugin.getCustomRewards()) {
                        if (cr.getModuleName().equals(moduleName)) {
                            text.append(ChatFormatting.DARK_PURPLE).append("  - ").append(cr.getName()).append("\n");
                        }
                    }
                }
                return text.toString() + ChatFormatting.YELLOW + getQueryText();
            }
            final MutableComponent component = Component.literal(getTitle() + "\n")
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE));
            final MutableComponent line = Component.literal("");
            if (plugin.getCustomRewards().isEmpty()) {
                final MutableComponent link = Component.literal("https://pikamug.gitbook.io/quests/casual/modules\n")
                        .withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_AQUA).withUnderlined(true));
                line.append(link);
                line.append(Component.literal(ChatFormatting.RED + "(" + FabricLang.get("stageEditorNoModules") + ")\n"));
            } else {
                for (final CustomReward co : plugin.getCustomRewards()) {
                    if (co.getModuleName().equals(moduleName)) {
                        final MutableComponent click = Component.literal(ChatFormatting.DARK_PURPLE + "  - " + co.getName()
                                + "\n").withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                "/quests choice " + co.getName())));
                        line.append(click);
                    }
                }
            }
            component.append(line);
            component.append(Component.literal(ChatFormatting.YELLOW + getQueryText()));
            FabricMiscUtil.getPlayer(uuid, plugin).sendSystemMessage(component);
            return "";
        }

        @SuppressWarnings("unchecked")
        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                CustomReward found = null;
                for (final CustomReward cr : plugin.getCustomRewards()) {
                    if (cr.getModuleName().equals(moduleName)) {
                        if (cr.getName().toLowerCase().contains(input.toLowerCase())) {
                            found = cr;
                            break;
                        }
                    }
                }
                if (found != null) {
                    if (SessionData.get(uuid, Key.REW_CUSTOM) != null) {
                        final LinkedList<String> list = (LinkedList<String>) SessionData.get(uuid, Key.REW_CUSTOM);
                        final LinkedList<Map<String, Object>> dataMapList
                                = (LinkedList<Map<String, Object>>) SessionData.get(uuid, Key.REW_CUSTOM_DATA);
                        if (list != null && dataMapList != null && !list.contains(found.getName())) {
                            list.add(found.getName());
                            dataMapList.add(found.getData());
                            SessionData.set(uuid, Key.REW_CUSTOM, list);
                            SessionData.set(uuid, Key.REW_CUSTOM_DATA, dataMapList);
                        } else {
                            if (sender != null) {
                                sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("rewCustomAlreadyAdded")));
                            }
                            new QuestCustomRewardsPrompt(moduleName, uuid).start();
                            return;
                        }
                    } else {
                        final LinkedList<Map<String, Object>> dataMapList = new LinkedList<>();
                        dataMapList.add(found.getData());
                        final LinkedList<String> list = new LinkedList<>();
                        list.add(found.getName());
                        SessionData.set(uuid, Key.REW_CUSTOM, list);
                        SessionData.set(uuid, Key.REW_CUSTOM_DATA, dataMapList);
                    }
                    if (!found.getData().isEmpty()) {
                        SessionData.set(uuid, Key.REW_CUSTOM_DATA_DESCRIPTIONS, found.getDescriptions());
                        new QuestRewardCustomDataListPrompt(uuid).start();
                        return;
                    }
                } else {
                    if (sender != null) {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("rewCustomNotFound")));
                    }
                    new QuestCustomRewardsPrompt(moduleName, uuid).start();
                    return;
                }
            } else if (input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.REW_CUSTOM, null);
                SessionData.set(uuid, Key.REW_CUSTOM_DATA, null);
                SessionData.set(uuid, Key.REW_CUSTOM_DATA_TEMP, null);
                if (sender != null) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("rewCustomCleared")));
                }
            }
            new FabricQuestRewardsPrompt(uuid).start();
        }
    }

    private class QuestRewardCustomDataListPrompt extends FabricQuestsEditorStringPrompt {

        public QuestRewardCustomDataListPrompt(final UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return null;
        }

        @Override
        @SuppressWarnings("unchecked")
        public @NotNull String getPromptText() {
            final StringBuilder text = new StringBuilder(ChatFormatting.GOLD + "- ");
            final LinkedList<String> list = (LinkedList<String>) SessionData.get(uuid, Key.REW_CUSTOM);
            final LinkedList<Map<String, Object>> dataMapList
                    = (LinkedList<Map<String, Object>>) SessionData.get(uuid, Key.REW_CUSTOM_DATA);
            if (list != null && dataMapList != null) {
                final String rewName = list.getLast();
                final Map<String, Object> dataMap = dataMapList.getLast();
                text.append(rewName).append(" -\n");
                int index = 1;
                final LinkedList<String> dataMapKeys = new LinkedList<>(dataMap.keySet());
                Collections.sort(dataMapKeys);
                for (final String dataKey : dataMapKeys) {
                    text.append(ChatFormatting.BLUE).append(ChatFormatting.BOLD).append(index).append(ChatFormatting.RESET)
                            .append(ChatFormatting.YELLOW).append(" - ").append(dataKey);
                    if (dataMap.get(dataKey) != null && !dataMap.get(dataKey).toString().trim().isEmpty()) {
                        text.append(ChatFormatting.GRAY).append(" (").append(ChatFormatting.AQUA)
                                .append(dataMap.get(dataKey).toString())
                                .append(ChatFormatting.GRAY).append(")\n");
                    } else {
                        text.append(ChatFormatting.GRAY).append(" (").append(FabricLang.get("noneSet")).append(ChatFormatting.GRAY)
                                .append(")\n");
                    }
                    index++;
                }
                text.append(ChatFormatting.GREEN).append(ChatFormatting.BOLD).append(index).append(ChatFormatting.YELLOW).append(" - ")
                        .append(FabricLang.get("done"));
            }
            return sendClickableSelection(text.toString(), plugin.getQuester(uuid));
        }

        @Override
        public void acceptInput(final String input) {
            @SuppressWarnings("unchecked")
            final LinkedList<Map<String, Object>> dataMapList
                    = (LinkedList<Map<String, Object>>) SessionData.get(uuid, Key.REW_CUSTOM_DATA);
            if (dataMapList != null) {
                final Map<String, Object> dataMap = dataMapList.getLast();
                int numInput = 0;
                try {
                    numInput = Integer.parseInt(input);
                } catch (final NumberFormatException nfe) {
                    new QuestRewardCustomDataListPrompt(uuid).start();
                    return;
                }
                if (numInput < 1 || numInput > dataMap.size() + 1) {
                    new QuestRewardCustomDataListPrompt(uuid).start();
                    return;
                }
                if (numInput < dataMap.size() + 1) {
                    final LinkedList<String> dataMapKeys = new LinkedList<>(dataMap.keySet());
                    Collections.sort(dataMapKeys);
                    final String selectedKey = dataMapKeys.get(numInput - 1);
                    SessionData.set(uuid, Key.REW_CUSTOM_DATA_TEMP, selectedKey);
                    new QuestRewardCustomDataPrompt(uuid).start();
                    return;
                } else {
                    if (dataMap.containsValue(null)) {
                        new QuestRewardCustomDataListPrompt(uuid).start();
                        return;
                    } else {
                        SessionData.set(uuid, Key.REW_CUSTOM_DATA_DESCRIPTIONS, null);
                    }
                }
            }
            new FabricQuestRewardsPrompt(uuid).start();
        }
    }

    private class QuestRewardCustomDataPrompt extends FabricQuestsEditorStringPrompt {

        public QuestRewardCustomDataPrompt(final UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return null;
        }

        @Override
        public @NotNull String getPromptText() {
            String text = "";
            final String temp = (String) SessionData.get(uuid, Key.REW_CUSTOM_DATA_TEMP);
            @SuppressWarnings("unchecked")
            final Map<String, String> descriptions
                    = (Map<String, String>) SessionData.get(uuid, Key.REW_CUSTOM_DATA_DESCRIPTIONS);
            if (temp != null && descriptions != null) {
                if (descriptions.get(temp) != null) {
                    text += descriptions.get(temp) + "\n";
                }
                String lang = FabricLang.get("stageEditorCustomDataPrompt");
                lang = lang.replace("<data>", temp);
                text += ChatFormatting.YELLOW + lang;
            }
            return text;
        }

        @Override
        public void acceptInput(final String input) {
            @SuppressWarnings("unchecked")
            final LinkedList<Map<String, Object>> dataMapList
                    = (LinkedList<Map<String, Object>>) SessionData.get(uuid, Key.REW_CUSTOM_DATA);
            if (dataMapList != null) {
                final Map<String, Object> dataMap = dataMapList.getLast();
                dataMap.put((String) SessionData.get(uuid, Key.REW_CUSTOM_DATA_TEMP), input);
                SessionData.set(uuid, Key.REW_CUSTOM_DATA_TEMP, null);
            }
            new QuestRewardCustomDataListPrompt(uuid).start();
        }
    }
}
