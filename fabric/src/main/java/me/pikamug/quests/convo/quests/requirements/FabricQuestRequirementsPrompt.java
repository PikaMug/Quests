/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.convo.quests.requirements;

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.convo.generic.FabricItemStackPrompt;
import me.pikamug.quests.convo.generic.FabricOverridePrompt;
import me.pikamug.quests.convo.quests.FabricQuestsEditorIntegerPrompt;
import me.pikamug.quests.convo.quests.FabricQuestsEditorStringPrompt;
import me.pikamug.quests.module.CustomRequirement;
import me.pikamug.quests.quests.Quest;
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

public class FabricQuestRequirementsPrompt extends FabricQuestsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final FabricQuestsPlugin plugin;
    private final String classPrefix;
    private boolean hasRequirement = false;
    private final int size = 12;
    
    public FabricQuestRequirementsPrompt(final @NotNull UUID uuid) {
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
        return FabricLang.get("requirementsTitle").replace("<quest>", (String) Objects
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
        case 7:
        case 10:
            return ChatFormatting.BLUE;
        case 8:
            return ChatFormatting.GRAY;
        case 9:
            return ChatFormatting.GRAY;
        case 11:
            if (SessionData.get(uuid, Key.REQ_FAIL_MESSAGE) == null) {
                if (!hasRequirement) {
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
            return ChatFormatting.GRAY + FabricLang.get("reqSetMoney");
        case 2:
            return ChatFormatting.YELLOW + FabricLang.get("reqSetQuestPoints").replace("<points>", FabricLang.get("questPoints"));
        case 3:
            return ChatFormatting.YELLOW + FabricLang.get("reqSetItem");
        case 4:
            return ChatFormatting.YELLOW + FabricLang.get("reqSetExperience");
        case 5:
            return ChatFormatting.YELLOW + FabricLang.get("reqSetPerms");
        case 6:
            return ChatFormatting.YELLOW + FabricLang.get("reqSetQuest");
        case 7:
            return ChatFormatting.YELLOW + FabricLang.get("reqSetQuestBlocks");
        case 8:
            return ChatFormatting.GRAY + FabricLang.get("reqSetMcMMO");
        case 9:
            return ChatFormatting.GRAY + FabricLang.get("reqSetHeroes");
        case 10:
            return ChatFormatting.DARK_PURPLE + FabricLang.get("reqSetCustom");
        case 11:
            if (!hasRequirement) {
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
            if (SessionData.get(uuid, Key.REQ_QUEST_POINTS) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + SessionData.get(uuid, Key.REQ_QUEST_POINTS) + " "
                        + FabricLang.get("questPoints") + ChatFormatting.GRAY + ")";
            }
        case 3:
            if (SessionData.get(uuid, Key.REQ_ITEMS) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<ItemStack> items = (LinkedList<ItemStack>) SessionData.get(uuid, Key.REQ_ITEMS);
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
            if (SessionData.get(uuid, Key.REQ_EXP) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + SessionData.get(uuid, Key.REQ_EXP) + " "
                        + FabricLang.get("points") + ChatFormatting.GRAY + ")";
            }
        case 5:
            if (SessionData.get(uuid, Key.REQ_PERMISSION) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final List<String> perms = (List<String>) SessionData.get(uuid, Key.REQ_PERMISSION);
                if (perms != null) {
                    for (final String s : perms) {
                        text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA).append(s);
                    }
                }
                return text.toString();
            }
        case 6:
            if (SessionData.get(uuid, Key.REQ_QUEST) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final List<String> questReq = (List<String>) SessionData.get(uuid, Key.REQ_QUEST);
                if (questReq != null) {
                    for (String s : questReq) {
                        if (plugin.getQuestById(s) != null) {
                            s = plugin.getQuestById(s).getName();
                        }
                        text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA).append(s);
                    }
                }
                return text.toString();
            }
        case 7:
            if (SessionData.get(uuid, Key.REQ_QUEST_BLOCK) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final List<String> questBlockReq = (List<String>) SessionData.get(uuid, Key.REQ_QUEST_BLOCK);
                if (questBlockReq != null) {
                    for (String s : questBlockReq) {
                        if (plugin.getQuestById(s) != null) {
                            s = plugin.getQuestById(s).getName();
                        }
                        text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA).append(s);
                    }
                }
                return text.toString();
            }
        case 8:
            return ChatFormatting.GRAY + "(" + FabricLang.get("notInstalled") + ")";
        case 9:
            return ChatFormatting.GRAY + "(" + FabricLang.get("notInstalled") + ")";
        case 10:
            if (SessionData.get(uuid, Key.REQ_CUSTOM) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> customReq = (LinkedList<String>) SessionData.get(uuid, Key.REQ_CUSTOM);
                if (customReq != null) {
                    for (final String s : customReq) {
                        text.append("\n").append(ChatFormatting.LIGHT_PURPLE).append("     - ").append(s);
                    }
                }
                return text.toString();
            }
        case 11:
            if (SessionData.get(uuid, Key.REQ_FAIL_MESSAGE) == null) {
                if (!hasRequirement) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("stageEditorOptional") + ")";
                } else {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                }
            } else {
                final StringBuilder text = new StringBuilder();
                final List<String> overrides = (List<String>) SessionData.get(uuid, Key.REQ_FAIL_MESSAGE);
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
    
    @SuppressWarnings("unchecked")
    @Override
    public @NotNull String getPromptText() {
        final String input = (String) SessionData.get(uuid, classPrefix + "-override");
        if (input != null && !input.equalsIgnoreCase(FabricLang.get("cancel"))) {
            if (input.equalsIgnoreCase(FabricLang.get("clear"))) {
                SessionData.set(uuid, Key.REQ_FAIL_MESSAGE, null);
            } else {
                final LinkedList<String> overrides = new LinkedList<>();
                if (SessionData.get(uuid, Key.REQ_FAIL_MESSAGE) != null) {
                    overrides.addAll((List<String>) SessionData.get(uuid, Key.REQ_FAIL_MESSAGE));
                }
                overrides.add(input);
                SessionData.set(uuid, Key.REQ_FAIL_MESSAGE, overrides);
                SessionData.set(uuid, classPrefix + "-override", null);
            }
        }
        checkRequirement();

        final StringBuilder text = new StringBuilder(ChatFormatting.DARK_AQUA + "- "  + getTitle()
                .replace((String) Objects.requireNonNull(SessionData.get(uuid, Key.Q_NAME)), ChatFormatting.AQUA
                + (String) SessionData.get(uuid, Key.Q_NAME) + ChatFormatting.DARK_AQUA) + " -");
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
            new QuestRequirementsMoneyPrompt(uuid).start();
            break;
        case 2:
            new QuestRequirementsQuestPointsPrompt(uuid).start();
            break;
        case 3:
            new QuestRequirementsItemListPrompt(uuid).start();
            break;
        case 4:
            new QuestRequirementsExperiencePrompt(uuid).start();
            break;
        case 5:
            new QuestRequirementsPermissionsPrompt(uuid).start();
            break;
        case 6:
            new QuestRequirementsQuestListPrompt(uuid, true).start();
            break;
        case 7:
            new QuestRequirementsQuestListPrompt(uuid, false).start();
            break;
        case 8:
            new QuestRequirementsPrompt(uuid).start();
            break;
        case 9:
            new QuestRequirementsPrompt(uuid).start();
            break;
        case 10:
            new QuestCustomRequirementModulePrompt(uuid).start();
            break;
        case 11:
            if (hasRequirement) {
                new FabricOverridePrompt(uuid, this, FabricLang.get("overrideCreateEnter")).start();
            } else {
                final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
                if (sender != null) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("invalidOption")));
                }
                new QuestRequirementsPrompt(uuid).start();
            }
            break;
        case 12:
            plugin.getQuestFactory().returnToMenu(uuid);
            break;
        default:
            new QuestRequirementsPrompt(uuid).start();
            break;
        }
    }
    
    public boolean checkRequirement() {
        if (SessionData.get(uuid, Key.REQ_MONEY) != null
                || SessionData.get(uuid, Key.REQ_QUEST_POINTS) != null
                || SessionData.get(uuid, Key.REQ_ITEMS) != null
                || SessionData.get(uuid, Key.REQ_EXP) != null
                || SessionData.get(uuid, Key.REQ_PERMISSION) != null
                || SessionData.get(uuid, Key.REQ_QUEST) != null
                || SessionData.get(uuid, Key.REQ_QUEST_BLOCK) != null
                || SessionData.get(uuid, Key.REQ_MCMMO_SKILLS) != null
                || SessionData.get(uuid, Key.REQ_HEROES_PRIMARY_CLASS) != null
                || SessionData.get(uuid, Key.REQ_HEROES_SECONDARY_CLASS) != null
                || SessionData.get(uuid, Key.REQ_CUSTOM) != null) {
            hasRequirement = true;
            return true;
        }
        return false;
    }

    public class QuestRequirementsMoneyPrompt extends FabricQuestsEditorStringPrompt {
        
        public QuestRequirementsMoneyPrompt(final @NotNull UUID uuid) {
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
                        SessionData.set(uuid, Key.REQ_MONEY, i);
                    } else {
                        if (sender != null) {
                            sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("inputPosNum")));
                        }
                        new QuestRequirementsMoneyPrompt(uuid).start();
                        return;
                    }
                } catch (final NumberFormatException e) {
                    if (sender != null) {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("reqNotANumber")
                                .replace("<input>", input)));
                    }
                    new QuestRequirementsMoneyPrompt(uuid).start();
                    return;
                }
            } else if (input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.REQ_MONEY, null);
                new QuestRequirementsPrompt(uuid).start();
                return;
            }
            new QuestRequirementsPrompt(uuid).start();
        }
    }

    public class QuestRequirementsQuestPointsPrompt extends FabricQuestsEditorStringPrompt {
        
        public QuestRequirementsQuestPointsPrompt(final @NotNull UUID uuid) {
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
                        SessionData.set(uuid, Key.REQ_QUEST_POINTS, i);
                    } else {
                        if (sender != null) {
                            sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("inputPosNum")));
                        }
                        new QuestRequirementsQuestPointsPrompt(uuid).start();
                        return;
                    }
                } catch (final NumberFormatException e) {
                    if (sender != null) {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("reqNotANumber")
                                .replace("<input>", input)));
                    }
                    new QuestRequirementsQuestPointsPrompt(uuid).start();
                    return;
                }
            } else if (input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.REQ_QUEST_POINTS, null);
                new QuestRequirementsPrompt(uuid).start();
                return;
            }
            new QuestRequirementsPrompt(uuid).start();
        }
    }

    public class QuestRequirementsItemListPrompt extends FabricQuestsEditorIntegerPrompt {
        
        public QuestRequirementsItemListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 4;

        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle() {
            return FabricLang.get("itemRequirementsTitle");
        }
        
        @Override
        public ChatFormatting getNumberColor(final int number) {
            switch (number) {
            case 1:
                return ChatFormatting.BLUE;
            case 2:
                if (SessionData.get(uuid, Key.REQ_ITEMS) == null) {
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
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorDeliveryAddItem");
            case 2:
                if (SessionData.get(uuid, Key.REQ_ITEMS) == null) {
                    return ChatFormatting.GRAY + FabricLang.get("reqSetRemoveItems");
                } else {
                    return ChatFormatting.YELLOW + FabricLang.get("reqSetRemoveItems");
                }
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
            switch (number) {
            case 1:
                if (SessionData.get(uuid, Key.REQ_ITEMS) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<ItemStack> reqItems = (List<ItemStack>) SessionData.get(uuid, Key.REQ_ITEMS);
                    if (reqItems != null) {
                        for (final ItemStack is : reqItems) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ")
                                    .append(FabricItemUtil.getDisplayString(is));
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (SessionData.get(uuid, Key.REQ_ITEMS) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    if (SessionData.get(uuid, Key.REQ_ITEMS_REMOVE) == null) {
                        return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                    } else {
                        final StringBuilder text = new StringBuilder();
                        final List<Boolean> reqItemsRemove
                                = (List<Boolean>) SessionData.get(uuid, Key.REQ_ITEMS_REMOVE);
                        if (reqItemsRemove != null) {
                            for (final Boolean b : reqItemsRemove) {
                                text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA)
                                        .append(b.equals(Boolean.TRUE) ? FabricLang.get("yesWord") : FabricLang.get("noWord"));
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
        @SuppressWarnings("unchecked")
        public @NotNull String getPromptText() {
            // Check/add newly made item
            if (SessionData.get(uuid, "tempStack") != null) {
                if (SessionData.get(uuid, Key.REQ_ITEMS) != null) {
                    final List<ItemStack> itemReq = (List<ItemStack>) SessionData.get(uuid, Key.REQ_ITEMS);
                    final ItemStack i = (ItemStack) SessionData.get(uuid, "tempStack");
                    if (itemReq != null && i != null) {
                        itemReq.add(i);
                    }
                    SessionData.set(uuid, Key.REQ_ITEMS, itemReq);
                } else {
                    final LinkedList<ItemStack> itemReq = new LinkedList<>();
                    final ItemStack i = (ItemStack) SessionData.get(uuid, "tempStack");
                    if (i != null) {
                        itemReq.add(i);
                    }
                    SessionData.set(uuid, Key.REQ_ITEMS, itemReq);
                }
                FabricItemStackPrompt.clearSessionData(uuid);
            }

            final StringBuilder text = new StringBuilder(ChatFormatting.AQUA + getTitle() + "\n");
            for (int i = 1; i <= size; i++) {
                text.append(getNumberColor(i)).append(ChatFormatting.BOLD).append(i).append(ChatFormatting.RESET)
                        .append(" - ").append(getSelectionText(i)).append(" ")
                        .append(getAdditionalText(i)).append("\n");
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
            switch (input.intValue()) {
            case 1:
                new FabricItemStackPrompt(uuid, QuestRequirementsItemListPrompt.this).start();
                break;
            case 2:
                if (SessionData.get(uuid, Key.REQ_ITEMS) == null) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("reqMustAddItem")));
                    new QuestRequirementsItemListPrompt(uuid).start();
                } else {
                    new QuestRemoveItemsPrompt(uuid).start();
                }
                break;
            case 3:
                sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("reqItemCleared")));
                SessionData.set(uuid, Key.REQ_ITEMS, null);
                SessionData.set(uuid, Key.REQ_ITEMS_REMOVE, null);
                new QuestRequirementsItemListPrompt(uuid).start();
                break;
            case 4:
                final int missing;
                final List<ItemStack> items = (List<ItemStack>) SessionData.get(uuid, Key.REQ_ITEMS);
                LinkedList<Boolean> remove = (LinkedList<Boolean>) SessionData.get(uuid, Key.REQ_ITEMS_REMOVE);
                if (items != null) {
                    if (remove != null) {
                        missing = items.size() - remove.size();
                    } else {
                        missing = items.size();
                        remove = new LinkedList<>();
                    }
                    for (int i = 0; i < missing; i++) {
                        remove.add(false);
                    }
                }
                SessionData.set(uuid, Key.REQ_ITEMS_REMOVE, remove);
                new QuestRequirementsPrompt(uuid).start();
                break;
            default:
                new QuestRequirementsPrompt(uuid).start();
                break;
            }
        }
    }

    public class QuestRemoveItemsPrompt extends FabricQuestsEditorStringPrompt {
        
        public QuestRemoveItemsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("reqRemoveItemsPrompt");
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
                final LinkedList<Boolean> booleans = new LinkedList<>();
                for (final String s : args) {
                    if (input.startsWith("t") || s.equalsIgnoreCase(FabricLang.get("true"))
                            || s.equalsIgnoreCase(FabricLang.get("yesWord"))) {
                        booleans.add(true);
                    } else if (input.startsWith("f") || s.equalsIgnoreCase(FabricLang.get("false"))
                            || s.equalsIgnoreCase(FabricLang.get("noWord"))) {
                        booleans.add(false);
                    } else {
                        if (sender != null) {
                            sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("itemCreateInvalidInput")));
                        }
                        new QuestRemoveItemsPrompt(uuid).start();
                        return;
                    }
                }
                SessionData.set(uuid, Key.REQ_ITEMS_REMOVE, booleans);
            }
            new QuestRequirementsItemListPrompt(uuid).start();
        }
    }

    public class QuestRequirementsExperiencePrompt extends FabricQuestsEditorStringPrompt {

        public QuestRequirementsExperiencePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("reqExperiencePrompt");
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
                        SessionData.set(uuid, Key.REQ_EXP, i);
                    } else {
                        if (sender != null) {
                            sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("inputPosNum")));
                        }
                        new QuestRequirementsExperiencePrompt(uuid).start();
                        return;
                    }
                } catch (final NumberFormatException e) {
                    if (sender != null) {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("reqNotANumber")
                                .replace("<input>", input)));
                    }
                    new QuestRequirementsExperiencePrompt(uuid).start();
                    return;
                }
            } else if (input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.REQ_EXP, null);
                new QuestRequirementsPrompt(uuid).start();
                return;
            }
            new QuestRequirementsPrompt(uuid).start();
        }
    }

    public class QuestRequirementsPermissionsPrompt extends FabricQuestsEditorStringPrompt {
        
        public QuestRequirementsPermissionsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("reqPermissionsPrompt");
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
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel")) && !input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                final String[] args = input.split(" ");
                final LinkedList<String> permissions = new LinkedList<>(Arrays.asList(args));
                SessionData.set(uuid, Key.REQ_PERMISSION, permissions);
            } else if (input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.REQ_PERMISSION, null);
            }
            new QuestRequirementsPrompt(uuid).start();
        }
    }

    public class QuestRequirementsQuestListPrompt extends FabricQuestsEditorStringPrompt {

        private final boolean isRequiredQuest;

        public QuestRequirementsQuestListPrompt(final @NotNull UUID uuid, final boolean isRequired) {
            super(uuid);
            this.isRequiredQuest = isRequired;
        }

        @Override
        public String getTitle() {
            return FabricLang.get("reqQuestListTitle");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("reqQuestPrompt");
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
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel")) && !input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                final String[] args = input.split(FabricLang.get("charSemi"));
                final LinkedList<String> questIds = new LinkedList<>();
                for (String s : args) {
                    s = s.trim();
                    if (plugin.getQuest(s) == null) {
                        String text = FabricLang.get("reqNotAQuestName");
                        text = text.replace("<quest>", s);
                        if (sender != null) {
                            sender.sendSystemMessage(Component.literal(text));
                        }
                        new QuestRequirementsQuestListPrompt(uuid, isRequiredQuest).start();
                        return;
                    }
                    if (questIds.contains(plugin.getQuest(s).getId())) {
                        if (sender != null) {
                            sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("listDuplicate")));
                        }
                        new QuestRequirementsQuestListPrompt(uuid, isRequiredQuest).start();
                        return;
                    }
                    questIds.add(plugin.getQuest(s).getId());
                }
                if (isRequiredQuest) {
                    SessionData.set(uuid, Key.REQ_QUEST, questIds);
                } else {
                    SessionData.set(uuid, Key.REQ_QUEST_BLOCK, questIds);
                }
            } else if (input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                if (isRequiredQuest) {
                    SessionData.set(uuid, Key.REQ_QUEST, null);
                } else {
                    SessionData.set(uuid, Key.REQ_QUEST_BLOCK, null);
                }
            }
            new QuestRequirementsPrompt(uuid).start();
        }
    }

    public class QuestRequirementsMcMMOListPrompt extends FabricQuestsEditorIntegerPrompt {

        public QuestRequirementsMcMMOListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        private final int size = 3;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle() {
            return FabricLang.get("mcMMORequirementsTitle");
        }
        
        @Override
        public ChatFormatting getNumberColor(final int number) {
            switch (number) {
                case 1:
                case 2:
                    return ChatFormatting.BLUE;
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
                return ChatFormatting.YELLOW + FabricLang.get("reqSetSkills");
            case 2:
                return ChatFormatting.YELLOW + FabricLang.get("reqSetSkillAmounts");
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
                if (SessionData.get(uuid, Key.REQ_MCMMO_SKILLS) == null) {
                    return ChatFormatting.GRAY + " (" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final LinkedList<String> skills = (LinkedList<String>) SessionData.get(uuid, Key.REQ_MCMMO_SKILLS);
                    if (skills != null) {
                        for (final String skill : skills) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA)
                                    .append(skill);
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (SessionData.get(uuid, Key.REQ_MCMMO_SKILL_AMOUNTS) == null) {
                    return ChatFormatting.GRAY + " (" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final LinkedList<Integer> skillAmounts
                            = (LinkedList<Integer>) SessionData.get(uuid, Key.REQ_MCMMO_SKILL_AMOUNTS);
                    if (skillAmounts != null) {
                        for (final int i : skillAmounts) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA).append(i);
                        }
                    }
                    return text.toString();
                }
            case 3:
                return "";
            default:
                return null;
            }
        }
        
        @Override
        public @NotNull String getPromptText() {
            final StringBuilder text = new StringBuilder(ChatFormatting.AQUA + getTitle() + "\n");
            for (int i = 1; i <= size; i++) {
                text.append(getNumberColor(i)).append(ChatFormatting.BOLD).append(i).append(ChatFormatting.RESET)
                        .append(" - ").append(getSelectionText(i)).append(" ").append(getAdditionalText(i)).append("\n");
            }
            return sendClickableSelection(text.toString(), plugin.getQuester(uuid));
        }
        
        @Override
        public void acceptInput(final Number input) {
            switch(input.intValue()) {
            case 1:
                new QuestMcMMOSkillsPrompt(uuid).start();
                break;
            case 2:
                new QuestMcMMOAmountsPrompt(uuid).start();
                break;
            case 3:
                new QuestRequirementsPrompt(uuid).start();
                break;
            default:
                new QuestRequirementsMcMMOListPrompt(uuid).start();
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
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel")) && !input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                final LinkedList<String> skills = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    final String formatted = FabricMiscUtil.getCapitalized(s);
                    skills.add(formatted);
                }
                SessionData.set(uuid, Key.REQ_MCMMO_SKILLS, skills);
                new QuestRequirementsMcMMOListPrompt(uuid).start();
                return;
            } else if (input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                if (sender != null) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("reqMcMMOCleared")));
                }
                SessionData.set(uuid, Key.REQ_MCMMO_SKILLS, null);
                new QuestRequirementsMcMMOListPrompt(uuid).start();
                return;
            } else if (input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                new QuestRequirementsMcMMOListPrompt(uuid).start();
                return;
            }
            new QuestMcMMOSkillsPrompt(uuid).start();
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
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel")) && !input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                final LinkedList<Integer> amounts = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    try {
                        final int i = Integer.parseInt(s);
                        amounts.add(i);
                    } catch (final NumberFormatException nfe) {
                        String text = FabricLang.get("reqNotANumber");
                        text = text.replace("<input>", s);
                        if (sender != null) {
                            sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + text));
                        }
                        new QuestMcMMOAmountsPrompt(uuid).start();
                        return;
                    }
                }
                SessionData.set(uuid, Key.REQ_MCMMO_SKILL_AMOUNTS, amounts);
                new QuestRequirementsMcMMOListPrompt(uuid).start();
                return;
            } else if (input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                if (sender != null) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("reqMcMMOAmountsCleared")));
                }
                SessionData.set(uuid, Key.REQ_MCMMO_SKILL_AMOUNTS, null);
                new QuestRequirementsMcMMOListPrompt(uuid).start();
                return;
            } else if (input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                new QuestRequirementsMcMMOListPrompt(uuid).start();
                return;
            }
            new QuestMcMMOAmountsPrompt(uuid).start();
        }
    }

    public class QuestRequirementsHeroesListPrompt extends FabricQuestsEditorIntegerPrompt {

        public QuestRequirementsHeroesListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        private final int size = 3;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle() {
            return FabricLang.get("heroesRequirementsTitle");
        }
        
        @Override
        public ChatFormatting getNumberColor(final int number) {
            switch (number) {
                case 1:
                case 2:
                    return ChatFormatting.BLUE;
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
                return ChatFormatting.YELLOW + FabricLang.get("reqHeroesSetPrimary");
            case 2:
                return ChatFormatting.YELLOW + FabricLang.get("reqHeroesSetSecondary");
            case 3:
                return ChatFormatting.GREEN + FabricLang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        public String getAdditionalText(final int number) {
            switch(number) {
            case 1:
                if (SessionData.get(uuid, Key.REQ_HEROES_PRIMARY_CLASS) == null) {
                    return ChatFormatting.GRAY + " (" + FabricLang.get("noneSet") + ")";
                } else {
                    return "(" + ChatFormatting.AQUA + SessionData.get(uuid, Key.REQ_HEROES_PRIMARY_CLASS) + ChatFormatting.GREEN
                            + ")\n";
                }
            case 2:
                if (SessionData.get(uuid, Key.REQ_HEROES_SECONDARY_CLASS) == null) {
                    return ChatFormatting.GRAY + " (" + FabricLang.get("noneSet") + ")";
                } else {
                    return "(" + ChatFormatting.AQUA + SessionData.get(uuid, Key.REQ_HEROES_SECONDARY_CLASS)
                            + ChatFormatting.GREEN + ")\n";
                }
            case 3:
                return "";
            default:
                return null;
            }
        }

        @Override
        public @NotNull String getPromptText() {
            final StringBuilder text = new StringBuilder(ChatFormatting.AQUA + getTitle() + "\n");
            for (int i = 1; i <= size; i++) {
                text.append(getNumberColor(i)).append(ChatFormatting.BOLD).append(i).append(ChatFormatting.RESET)
                        .append(" - ").append(getSelectionText(i)).append(" ")
                        .append(getAdditionalText(i)).append("\n");
            }
            return sendClickableSelection(text.toString(), plugin.getQuester(uuid));
        }
        @Override
        public void acceptInput(final Number input) {
            switch(input.intValue()) {
            case 1:
                new QuestHeroesPrimaryPrompt(uuid).start();
                break;
            case 2:
                new QuestHeroesSecondaryPrompt(uuid).start();
                break;
            case 3:
                new QuestRequirementsPrompt(uuid).start();
                break;
            default:
                new QuestRequirementsHeroesListPrompt(uuid).start();
                break;
            }
        }
    }

    public class QuestHeroesPrimaryPrompt extends FabricQuestsEditorStringPrompt {
        
        public QuestHeroesPrimaryPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return FabricLang.get("heroesPrimaryTitle");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("reqHeroesPrimaryPrompt");
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
            if (!input.equalsIgnoreCase(FabricLang.get("cmdClear")) && !input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                SessionData.set(uuid, Key.REQ_HEROES_PRIMARY_CLASS, input);
                new QuestRequirementsHeroesListPrompt(uuid).start();
            } else if (input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.REQ_HEROES_PRIMARY_CLASS, null);
                if (sender != null) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("reqHeroesPrimaryCleared")));
                }
                new QuestRequirementsHeroesListPrompt(uuid).start();
            } else {
                new QuestRequirementsHeroesListPrompt(uuid).start();
            }
        }
    }

    public class QuestHeroesSecondaryPrompt extends FabricQuestsEditorStringPrompt {
        
        public QuestHeroesSecondaryPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return FabricLang.get("heroesSecondaryTitle");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("reqHeroesSecondaryPrompt");
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
            if (!input.equalsIgnoreCase(FabricLang.get("cmdClear")) && !input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                SessionData.set(uuid, Key.REQ_HEROES_SECONDARY_CLASS, input);
                new QuestRequirementsHeroesListPrompt(uuid).start();
            } else if (input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.REQ_HEROES_SECONDARY_CLASS, null);
                if (sender != null) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("reqHeroesSecondaryCleared")));
                }
                new QuestRequirementsHeroesListPrompt(uuid).start();
            } else {
                new QuestRequirementsHeroesListPrompt(uuid).start();
            }
        }
    }

    public class QuestCustomRequirementModulePrompt extends FabricQuestsEditorStringPrompt {

        public QuestCustomRequirementModulePrompt(final @NotNull UUID uuid) {
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
                if (plugin.getCustomRequirements().isEmpty()) {
                    text.append(ChatFormatting.DARK_AQUA).append(ChatFormatting.UNDERLINE)
                            .append("https://pikamug.gitbook.io/quests/casual/modules").append(ChatFormatting.RESET)
                            .append("\n");
                    text.append(ChatFormatting.RED).append("(").append(FabricLang.get("stageEditorNoModules")).append(")")
                            .append("\n");
                } else {
                    for (final String name : plugin.getCustomRequirements().stream()
                            .map(CustomRequirement::getModuleName).collect(Collectors.toCollection(TreeSet::new))) {
                        text.append(ChatFormatting.DARK_PURPLE).append("  - ").append(name).append("\n");
                    }
                }
                return text.toString() + ChatFormatting.YELLOW + getQueryText();
            }
            final MutableComponent component = Component.literal(getTitle() + "\n")
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE));
            final MutableComponent line = Component.literal("");
            if (plugin.getCustomRequirements().isEmpty()) {
                final MutableComponent link = Component.literal("https://pikamug.gitbook.io/quests/casual/modules\n")
                        .withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_AQUA).withUnderlined(true));
                line.append(link);
                line.append(Component.literal(ChatFormatting.RED + "(" + FabricLang.get("stageEditorNoModules") + ")\n"));
            } else {
                for (final String name : plugin.getCustomRequirements().stream().map(CustomRequirement::getModuleName)
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
                for (final CustomRequirement cr : plugin.getCustomRequirements()) {
                    if (cr.getModuleName().equalsIgnoreCase(input)) {
                        found = cr.getModuleName();
                        break;
                    }
                }
                if (found == null) {
                    for (final CustomRequirement cr : plugin.getCustomRequirements()) {
                        if (cr.getModuleName().toLowerCase().contains(input.toLowerCase())) {
                            found = cr.getModuleName();
                            break;
                        }
                    }
                }
                if (found != null) {
                    new QuestCustomRequirementsPrompt(found, uuid).start();
                    return;
                }
            } else if (input != null && input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                new QuestRequirementsPrompt(uuid).start();
                return;
            } else if (input != null && input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.REQ_CUSTOM, null);
                SessionData.set(uuid, Key.REQ_CUSTOM_DATA, null);
                SessionData.set(uuid, Key.REQ_CUSTOM_DATA_TEMP, null);
                sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("reqCustomCleared")));
                new QuestRequirementsPrompt(uuid).start();
                return;
            }
            sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("reqCustomNotFound")));
            new QuestCustomRequirementModulePrompt(uuid).start();
        }
    }

    public class QuestCustomRequirementsPrompt extends FabricQuestsEditorStringPrompt {

        private final String moduleName;

        public QuestCustomRequirementsPrompt(final String moduleName, final UUID uuid) {
            super(uuid);
            this.moduleName = moduleName;
        }

        public String getModuleName() {
            return moduleName;
        }

        @Override
        public String getTitle() {
            return FabricLang.get("customRequirementsTitle");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("reqCustomPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            if (FabricMiscUtil.getPlayer(uuid, plugin) == null || !plugin.getConfigSettings().canClickablePrompts()) {
                final StringBuilder text = new StringBuilder(ChatFormatting.LIGHT_PURPLE + getTitle() + "\n");
                if (plugin.getCustomRequirements().isEmpty()) {
                    text.append(ChatFormatting.DARK_AQUA).append(ChatFormatting.UNDERLINE)
                            .append("https://pikamug.gitbook.io/quests/casual/modules\n");
                    text.append(ChatFormatting.RED).append("(").append(FabricLang.get("stageEditorNoModules")).append(")\n");
                } else {
                    for (final CustomRequirement cr : plugin.getCustomRequirements()) {
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
            if (plugin.getCustomRequirements().isEmpty()) {
                final MutableComponent link = Component.literal("https://pikamug.gitbook.io/quests/casual/modules\n")
                        .withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_AQUA).withUnderlined(true));
                line.append(link);
                line.append(Component.literal(ChatFormatting.RED + "(" + FabricLang.get("stageEditorNoModules") + ")\n"));
            } else {
                for (final CustomRequirement co : plugin.getCustomRequirements()) {
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

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel")) && !input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                CustomRequirement found = null;
                for (final CustomRequirement cr : plugin.getCustomRequirements()) {
                    if (cr.getModuleName().equals(moduleName)) {
                        if (cr.getName().toLowerCase().contains(input.toLowerCase())) {
                            found = cr;
                            break;
                        }
                    }
                }
                if (found != null) {
                    if (SessionData.get(uuid, Key.REQ_CUSTOM) != null) {
                        final LinkedList<String> list = (LinkedList<String>) SessionData.get(uuid, Key.REQ_CUSTOM);
                        final LinkedList<Map<String, Object>> dataMapList
                                = (LinkedList<Map<String, Object>>) SessionData.get(uuid, Key.REQ_CUSTOM_DATA);
                        if (dataMapList != null && list != null && !list.contains(found.getName())) {
                            list.add(found.getName());
                            dataMapList.add(found.getData());
                            SessionData.set(uuid, Key.REQ_CUSTOM, list);
                            SessionData.set(uuid, Key.REQ_CUSTOM_DATA, dataMapList);
                        } else {
                            if (sender != null) {
                                sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("reqCustomAlreadyAdded")));
                            }
                            new QuestCustomRequirementsPrompt(moduleName, uuid).start();
                            return;
                        }
                    } else {
                        final LinkedList<Map<String, Object>> dataMapList = new LinkedList<>();
                        dataMapList.add(found.getData());
                        final LinkedList<String> list = new LinkedList<>();
                        list.add(found.getName());
                        SessionData.set(uuid, Key.REQ_CUSTOM, list);
                        SessionData.set(uuid, Key.REQ_CUSTOM_DATA, dataMapList);
                    }
                    if (!found.getData().isEmpty()) {
                        SessionData.set(uuid, Key.REQ_CUSTOM_DATA_DESCRIPTIONS, found.getDescriptions());
                        new QuestRequirementCustomDataListPrompt(uuid).start();
                        return;
                    }
                } else {
                    if (sender != null) {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("reqCustomNotFound")));
                    }
                    new QuestCustomRequirementsPrompt(moduleName, uuid).start();
                    return;
                }
            } else if (input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.REQ_CUSTOM, null);
                SessionData.set(uuid, Key.REQ_CUSTOM_DATA, null);
                SessionData.set(uuid, Key.REQ_CUSTOM_DATA_TEMP, null);
                if (sender != null) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("reqCustomCleared")));
                }
            }
            new QuestRequirementsPrompt(uuid).start();
        }
    }

    private class QuestRequirementCustomDataListPrompt extends FabricQuestsEditorStringPrompt {

        public QuestRequirementCustomDataListPrompt(final UUID uuid) {
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

        @SuppressWarnings("unchecked")
        @Override
        public @NotNull String getPromptText() {
            final StringBuilder text = new StringBuilder(ChatFormatting.GOLD + "- ");
            final LinkedList<String> list = (LinkedList<String>) SessionData.get(uuid, Key.REQ_CUSTOM);
            final LinkedList<Map<String, Object>> dataMapList
                    = (LinkedList<Map<String, Object>>) SessionData.get(uuid, Key.REQ_CUSTOM_DATA);
            if (dataMapList != null && list != null) {
                final String reqName = list.getLast();
                final Map<String, Object> dataMap = dataMapList.getLast();
                text.append(reqName).append(" -\n");
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
                    = (LinkedList<Map<String, Object>>) SessionData.get(uuid, Key.REQ_CUSTOM_DATA);
            if (dataMapList != null) {
                final Map<String, Object> dataMap = dataMapList.getLast();
                int numInput = 0;
                try {
                    numInput = Integer.parseInt(input);
                } catch (final NumberFormatException nfe) {
                    new QuestRequirementCustomDataListPrompt(uuid).start();
                    return;
                }
                if (numInput < 1 || numInput > dataMap.size() + 1) {
                    new QuestRequirementCustomDataListPrompt(uuid).start();
                    return;
                }
                if (numInput < dataMap.size() + 1) {
                    final LinkedList<String> dataMapKeys = new LinkedList<>(dataMap.keySet());
                    Collections.sort(dataMapKeys);
                    final String selectedKey = dataMapKeys.get(numInput - 1);
                    SessionData.set(uuid, Key.REQ_CUSTOM_DATA_TEMP, selectedKey);
                    new QuestRequirementCustomDataPrompt(uuid).start();
                    return;
                } else {
                    if (dataMap.containsValue(null)) {
                        new QuestRequirementCustomDataListPrompt(uuid).start();
                        return;
                    } else {
                        SessionData.set(uuid, Key.REQ_CUSTOM_DATA_DESCRIPTIONS, null);
                    }
                }
            }
            new QuestRequirementsPrompt(uuid).start();
        }
    }

    private class QuestRequirementCustomDataPrompt extends FabricQuestsEditorStringPrompt {

        public QuestRequirementCustomDataPrompt(final UUID uuid) {
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
            final String temp = (String) SessionData.get(uuid, Key.REQ_CUSTOM_DATA_TEMP);
            @SuppressWarnings("unchecked")
            final Map<String, String> descriptions
                    = (Map<String, String>) SessionData.get(uuid, Key.REQ_CUSTOM_DATA_DESCRIPTIONS);
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
                    = (LinkedList<Map<String, Object>>) SessionData.get(uuid, Key.REQ_CUSTOM_DATA);
            if (dataMapList != null) {
                final Map<String, Object> dataMap = dataMapList.getLast();
                dataMap.put((String) SessionData.get(uuid, Key.REQ_CUSTOM_DATA_TEMP), input);
                SessionData.set(uuid, Key.REQ_CUSTOM_DATA_TEMP, null);
            }
            new QuestRequirementCustomDataListPrompt(uuid).start();
        }
    }
}
