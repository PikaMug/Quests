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

import com.gmail.nossr50.datatypes.skills.SkillType;
import com.herocraftonline.heroes.characters.classes.HeroClass;
import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.convo.generic.ItemStackPrompt;
import me.pikamug.quests.convo.generic.OverridePrompt;
import me.pikamug.quests.convo.quests.QuestsEditorNumericPrompt;
import me.pikamug.quests.convo.quests.QuestsEditorStringPrompt;
import me.pikamug.quests.events.editor.quests.QuestsEditorPostOpenNumericPromptEvent;
import me.pikamug.quests.events.editor.quests.QuestsEditorPostOpenStringPromptEvent;
import me.pikamug.quests.module.CustomReward;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.BukkitItemUtil;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.quests.util.BukkitMiscUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class QuestRewardsPrompt extends QuestsEditorNumericPrompt {

    private final BukkitQuestsPlugin plugin;
    private final String classPrefix;
    private boolean hasReward = false;
    private final int size = 12;

    public QuestRewardsPrompt(final ConversationContext context) {
        super(context);
        this.plugin = (BukkitQuestsPlugin)context.getPlugin();
        this.classPrefix = getClass().getSimpleName();
    }
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle(final ConversationContext context) {
        return BukkitLang.get("rewardsTitle").replace("<quest>", (String) Objects
                .requireNonNull(context.getSessionData(Key.Q_NAME)));
    }
    
    @Override
    public ChatColor getNumberColor(final ConversationContext context, final int number) {
        switch (number) {
        case 1:
            if (plugin.getDependencies().getVaultEconomy() != null) {
                return ChatColor.BLUE;
            } else {
                return ChatColor.GRAY;
            }
        case 2:
        case 3:
        case 4:
        case 5:
        case 6:
        case 10:
            return ChatColor.BLUE;
        case 7:
            if (plugin.getDependencies().getMcmmoClassic() != null) {
                return ChatColor.BLUE;
            } else {
                return ChatColor.GRAY;
            }
        case 8:
            if (plugin.getDependencies().getHeroes() != null) {
                return ChatColor.BLUE;
            } else {
                return ChatColor.GRAY;
            }
        case 9:
            if (plugin.getDependencies().getPartiesApi() != null) {
                return ChatColor.BLUE;
            } else {
                return ChatColor.GRAY;
            }
        case 11:
            if (context.getSessionData(Key.REW_DETAILS_OVERRIDE) == null) {
                if (!hasReward) {
                    return ChatColor.GRAY;
                } else {
                    return ChatColor.BLUE;
                }
            } else {
                return ChatColor.BLUE;
            }
        case 12:
            return ChatColor.GREEN;
        default:
            return null;
        }
    }
    
    @Override
    public String getSelectionText(final ConversationContext context, final int number) {
        switch (number) {
        case 1:
            if (plugin.getDependencies().getVaultEconomy() != null) {
                return ChatColor.YELLOW + BukkitLang.get("rewSetMoney");
            } else {
                return ChatColor.GRAY + BukkitLang.get("rewSetMoney");
            }
        case 2:
            return ChatColor.YELLOW + BukkitLang.get("rewSetQuestPoints").replace("<points>", BukkitLang.get("questPoints"));
        case 3:
            return ChatColor.YELLOW + BukkitLang.get("rewSetItems");
        case 4:
            return ChatColor.YELLOW + BukkitLang.get("rewSetExperience");
        case 5:
            return ChatColor.YELLOW + BukkitLang.get("rewSetCommands");
        case 6:
            return ChatColor.YELLOW + BukkitLang.get("rewSetPermission");
        case 7:
            if (plugin.getDependencies().getMcmmoClassic() != null) {
                return ChatColor.YELLOW + BukkitLang.get("rewSetMcMMO");
            } else {
                return ChatColor.GRAY + BukkitLang.get("rewSetMcMMO");
            }
        case 8:
            if (plugin.getDependencies().getHeroes() != null) {
                return ChatColor.YELLOW + BukkitLang.get("rewSetHeroes");
            } else {
                return ChatColor.GRAY + BukkitLang.get("rewSetHeroes");
            }
        case 9:
            if (plugin.getDependencies().getPartiesApi() != null) {
                return ChatColor.YELLOW + BukkitLang.get("rewSetPartiesExperience");
            } else {
                return ChatColor.GRAY + BukkitLang.get("rewSetPartiesExperience");
            }
        case 10:
            return ChatColor.DARK_PURPLE + BukkitLang.get("rewSetCustom");
        case 11:
            if (!hasReward) {
                return ChatColor.GRAY + BukkitLang.get("overrideCreateSet");
            } else {
                return ChatColor.YELLOW + BukkitLang.get("overrideCreateSet");
            }
        case 12:
            return ChatColor.YELLOW + BukkitLang.get("done");
        default:
            return null;
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public String getAdditionalText(final ConversationContext context, final int number) {
        switch (number) {
        case 1:
            if (plugin.getDependencies().getVaultEconomy() != null) {
                final Integer moneyRew = (Integer) context.getSessionData(Key.REW_MONEY);
                if (moneyRew == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    return ChatColor.GRAY + "(" + ChatColor.AQUA
                            + plugin.getDependencies().getVaultEconomy().format(moneyRew) + ChatColor.GRAY + ")";
                }
            } else {
                return ChatColor.GRAY + "(" + BukkitLang.get("notInstalled") + ")";
            }
        case 2:
            if (context.getSessionData(Key.REW_QUEST_POINTS) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + context.getSessionData(Key.REW_QUEST_POINTS) + " "
                        + BukkitLang.get("questPoints") + ChatColor.GRAY + ")";
            }
        case 3:
            if (context.getSessionData(Key.REW_ITEMS) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<ItemStack> items = (LinkedList<ItemStack>) context.getSessionData(Key.REW_ITEMS);
                if (items != null) {
                    for (final ItemStack item : items) {
                        if (item == null) {
                            text.append(ChatColor.RED).append("     - null\n");
                            plugin.getLogger().severe(ChatColor.RED + "Item reward was null while editing quest ID "
                                    + context.getSessionData(Key.Q_ID));
                        } else {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.BLUE)
                                    .append(BukkitItemUtil.getName(item)).append(ChatColor.GRAY).append(" x ")
                                    .append(ChatColor.AQUA).append(item.getAmount());
                        }
                    }
                }
                return text.toString();
            }
        case 4:
            if (context.getSessionData(Key.REW_EXP) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + context.getSessionData(Key.REW_EXP) + " "
                        + BukkitLang.get("points") + ChatColor.GRAY + ")";
            }
        case 5:
            if (context.getSessionData(Key.REW_COMMAND) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final List<String> commands = (List<String>) context.getSessionData(Key.REW_COMMAND);
                final List<String> overrides = (List<String>) context.getSessionData(Key.REW_COMMAND_OVERRIDE_DISPLAY);
                int index = 0;
                if (commands != null) {
                    for (final String cmd : commands) {
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(cmd);
                        if (overrides != null) {
                            if (index < overrides.size()) {
                                text.append(ChatColor.GRAY).append(" (\"").append(ChatColor.AQUA)
                                        .append(overrides.get(index)).append(ChatColor.GRAY).append("\")");
                            }
                        }
                        index++;
                    }
                }
                return text.toString();
            }
        case 6:
            if (context.getSessionData(Key.REW_PERMISSION) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final List<String> permissions = (List<String>) context.getSessionData(Key.REW_PERMISSION);
                final List<String> worlds = (List<String>) context.getSessionData(Key.REW_PERMISSION_WORLDS);
                int index = 0;
                if (permissions != null) {
                    for (final String perm : permissions) {
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(perm);
                        if (worlds != null) {
                            if (index < worlds.size()) {
                                text.append(ChatColor.GRAY).append("[").append(ChatColor.DARK_AQUA)
                                        .append(worlds.get(index)).append(ChatColor.GRAY).append("]");
                            }
                        }
                        index++;
                    }
                }
                return text.toString();
            }
        case 7:
            if (plugin.getDependencies().getMcmmoClassic() != null) {
                if (context.getSessionData(Key.REW_MCMMO_SKILLS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> skills = (List<String>) context.getSessionData(Key.REW_MCMMO_SKILLS);
                    final List<Integer> amounts = (List<Integer>) context.getSessionData(Key.REW_MCMMO_AMOUNTS);
                    if (skills != null && amounts != null) {
                        for (final String skill : skills) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA)
                                    .append(skill).append(ChatColor.GRAY).append(" x ").append(ChatColor.DARK_AQUA)
                                    .append(amounts.get(skills.indexOf(skill)));
                        }
                    }
                    return text.toString();
                }
            } else {
                return ChatColor.GRAY + "(" + BukkitLang.get("notInstalled") + ")";
            }
        case 8:
            if (plugin.getDependencies().getHeroes() != null) {
                if (context.getSessionData(Key.REW_HEROES_CLASSES) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> heroClasses = (List<String>) context.getSessionData(Key.REW_HEROES_CLASSES);
                    final List<Double> amounts = (List<Double>) context.getSessionData(Key.REW_HEROES_AMOUNTS);
                    if (heroClasses != null && amounts != null) {
                        for (final String heroClass : heroClasses) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA)
                                    .append(amounts.get(heroClasses.indexOf(heroClass))).append(" ")
                                    .append(ChatColor.DARK_AQUA).append(heroClass).append(" ")
                                    .append(BukkitLang.get("experience"));
                        }
                    }
                    return text.toString();
                }
            } else {
                return ChatColor.GRAY + "(" + BukkitLang.get("notInstalled") + ")";
            }
        case 9:
            if (context.getSessionData(Key.REW_PARTIES_EXPERIENCE) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + context.getSessionData(Key.REW_PARTIES_EXPERIENCE) + " "
                        + BukkitLang.get("points") + ChatColor.GRAY + ")";
            }
        case 10:
            if (context.getSessionData(Key.REW_CUSTOM) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> customRew = (LinkedList<String>) context.getSessionData(Key.REW_CUSTOM);
                if (customRew != null) {
                    for (final String s : customRew) {
                        text.append("\n").append(ChatColor.LIGHT_PURPLE).append("     - ").append(s);
                    }
                }
                return text.toString();
            }
        case 11:
            if (context.getSessionData(Key.REW_DETAILS_OVERRIDE) == null) {
                if (!hasReward) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("stageEditorOptional") + ")";
                } else {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                }
            } else {
                final StringBuilder text = new StringBuilder();
                final List<String> overrides = (List<String>) context.getSessionData(Key.REW_DETAILS_OVERRIDE);
                if (overrides != null) {
                    for (final String override : overrides) {
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA)
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
    public @NotNull String getBasicPromptText(final ConversationContext context) {
        final String input = (String) context.getSessionData(classPrefix + "-override");
        if (input != null && !input.equalsIgnoreCase(BukkitLang.get("cancel"))) {
            if (input.equalsIgnoreCase(BukkitLang.get("clear"))) {
                context.setSessionData(Key.REW_DETAILS_OVERRIDE, null);
            } else {
                final LinkedList<String> overrides = new LinkedList<>();
                if (context.getSessionData(Key.REW_DETAILS_OVERRIDE) != null) {
                    overrides.addAll((List<String>) context.getSessionData(Key.REW_DETAILS_OVERRIDE));
                }
                overrides.add(input);
                context.setSessionData(Key.REW_DETAILS_OVERRIDE, overrides);
                context.setSessionData(classPrefix + "-override", null);
            }
        }
        checkReward(context);

        final QuestsEditorPostOpenNumericPromptEvent event
                = new QuestsEditorPostOpenNumericPromptEvent(context, this);
        plugin.getServer().getPluginManager().callEvent(event);
        
        final StringBuilder text = new StringBuilder(ChatColor.LIGHT_PURPLE + "- " + getTitle(context)
                .replace((String) Objects.requireNonNull(context.getSessionData(Key.Q_NAME)), ChatColor.AQUA
                + (String) context.getSessionData(Key.Q_NAME) + ChatColor.LIGHT_PURPLE) + " -");
        for (int i = 1; i <= size; i++) {
            text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                    .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i)).append(" ")
                    .append(getAdditionalText(context, i));
        }
        return text.toString();
    }

    @Override
    protected Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
        switch (input.intValue()) {
        case 1:
            if (plugin.getDependencies().getVaultEconomy() != null) {
                return new QuestRewardsMoneyPrompt(context);
            } else {
                return new QuestRewardsPrompt(context);
            }
        case 2:
            return new QuestRewardsQuestPointsPrompt(context);
        case 3:
            return new QuestRewardsItemListPrompt(context);
        case 4:
            return new QuestRewardsExperiencePrompt(context);
        case 5:
            if (!plugin.hasLimitedAccess(context.getForWhom())) {
                return new QuestRewardsCommandsPrompt(context);
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("noPermission"));
                return new QuestRewardsPrompt(context);
            }
        case 6:
            if (!plugin.hasLimitedAccess(context.getForWhom())) {
                return new QuestRewardsPermissionsListPrompt(context);
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("noPermission"));
                return new QuestRewardsPrompt(context);
            }
        case 7:
            if (plugin.getDependencies().getMcmmoClassic() != null) {
                return new QuestRewardsMcMMOListPrompt(context);
            } else {
                return new QuestRewardsPrompt(context);
            }
        case 8:
            if (plugin.getDependencies().getHeroes() != null) {
                return new QuestRewardsHeroesListPrompt(context);
            } else {
                return new QuestRewardsPrompt(context);
            }
        case 9:
            return new QuestRewardsPartiesExperiencePrompt(context);
        case 10:
            return new QuestCustomRewardModulePrompt(context);
        case 11:
            if (hasReward) {
                return new OverridePrompt.Builder()
                        .source(this)
                        .promptText(BukkitLang.get("overrideCreateEnter"))
                        .build();
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("invalidOption"));
                return new QuestRewardsPrompt(context);
            }
        case 12:
            return plugin.getQuestFactory().returnToMenu(context);
        default:
            return new QuestRewardsPrompt(context);
        }
    }
    
    public boolean checkReward(final ConversationContext context) {
        if (context.getSessionData(Key.REW_MONEY) != null
                || context.getSessionData(Key.REW_QUEST_POINTS) != null
                || context.getSessionData(Key.REW_ITEMS) != null
                || context.getSessionData(Key.REW_EXP) != null
                || context.getSessionData(Key.REW_COMMAND) != null
                || context.getSessionData(Key.REW_PERMISSION) != null
                || context.getSessionData(Key.REW_MCMMO_SKILLS) != null
                || context.getSessionData(Key.REW_HEROES_CLASSES) != null
                || context.getSessionData(Key.REW_PARTIES_EXPERIENCE) != null
                || context.getSessionData(Key.REW_PHAT_LOOTS) != null
                || context.getSessionData(Key.REW_CUSTOM) != null) {
            hasReward = true;
            return true;
        }
        return false;
    }

    public class QuestRewardsMoneyPrompt extends QuestsEditorStringPrompt {
        
        public QuestRewardsMoneyPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("rewMoneyPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            String text = getQueryText(context);
            if (plugin.getDependencies().getVaultEconomy() != null) {
                text = text.replace("<money>", plugin.getDependencies().getVaultEconomy().currencyNamePlural());
            }
            return ChatColor.YELLOW + text;
        }
        
        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel")) && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i > 0) {
                        context.setSessionData(Key.REW_MONEY, i);
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("inputPosNum"));
                        return new QuestRewardsMoneyPrompt(context);
                    }
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                            .replace("<input>", input));
                    return new QuestRewardsMoneyPrompt(context);
                }
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(Key.REW_MONEY, null);
                return new QuestRewardsPrompt(context);
            }
            return new QuestRewardsPrompt(context);
        }
    }

    public class QuestRewardsQuestPointsPrompt extends QuestsEditorStringPrompt {
        
        public QuestRewardsQuestPointsPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("rewQuestPointsPrompt").replace("<points>", BukkitLang.get("questPoints"));
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }
        
        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel")) && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i > 0) {
                        context.setSessionData(Key.REW_QUEST_POINTS, i);
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("inputPosNum"));
                        return new QuestRewardsQuestPointsPrompt(context);
                    }
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                            .replace("<input>", input));
                    return new QuestRewardsQuestPointsPrompt(context);
                }
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(Key.REW_QUEST_POINTS, null);
                return new QuestRewardsPrompt(context);
            }
            return new QuestRewardsPrompt(context);
        }
    }

    public class QuestRewardsItemListPrompt extends QuestsEditorNumericPrompt {

        public QuestRewardsItemListPrompt(final ConversationContext context) {
            super(context);
        }
        
        private final int size = 3;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("itemRewardsTitle");
        }
        
        @Override
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
            switch (number) {
                case 1:
                    return ChatColor.BLUE;
                case 2:
                    return ChatColor.RED;
                case 3:
                    return ChatColor.GREEN;
                default:
                    return null;
            }
        }
        
        @Override
        public String getSelectionText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                return ChatColor.YELLOW + BukkitLang.get("stageEditorDeliveryAddItem");
            case 2:
                return ChatColor.RED + BukkitLang.get("clear");
            case 3:
                return ChatColor.GREEN + BukkitLang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                if (context.getSessionData(Key.REW_ITEMS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<ItemStack> items = (List<ItemStack>) context.getSessionData(Key.REW_ITEMS);
                    if (items != null) {
                        for (final ItemStack is : items) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ")
                                    .append(BukkitItemUtil.getDisplayString(is));
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

        @SuppressWarnings("unchecked")
        @Override
        public @NotNull String getBasicPromptText(final ConversationContext context) {
            // Check/add newly made item
            if (context.getSessionData("tempStack") != null) {
                if (context.getSessionData(Key.REW_ITEMS) != null) {
                    final List<ItemStack> itemRew = (List<ItemStack>) context.getSessionData(Key.REW_ITEMS);
                    if (itemRew != null) {
                        itemRew.add((ItemStack) context.getSessionData("tempStack"));
                        context.setSessionData(Key.REW_ITEMS, itemRew);
                    }
                } else {
                    final List<ItemStack> itemRew = new LinkedList<>();
                    itemRew.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(Key.REW_ITEMS, itemRew);
                }
                ItemStackPrompt.clearSessionData(context);
            }

            final QuestsEditorPostOpenNumericPromptEvent event
                    = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.AQUA + getTitle(context));
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i)).append(" ")
                        .append(getAdditionalText(context, i));
            }
            return text.toString();
        }
        
        @Override
        protected Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
            switch(input.intValue()) {
            case 1:
                return new ItemStackPrompt(context, QuestRewardsItemListPrompt.this);
            case 2:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("rewItemsCleared"));
                context.setSessionData(Key.REW_ITEMS, null);
                return new QuestRewardsItemListPrompt(context);
            case 3:
                return new QuestRewardsPrompt(context);
            default:
                return new QuestRewardsItemListPrompt(context);
            }
        }
    }

    public class QuestRewardsExperiencePrompt extends QuestsEditorStringPrompt {

        public QuestRewardsExperiencePrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("rewExperiencePrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);

            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel")) && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i > 0) {
                        context.setSessionData(Key.REW_EXP, i);
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("inputPosNum"));
                        return new QuestRewardsExperiencePrompt(context);
                    }
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                            .replace("<input>", input));
                    return new QuestRewardsExperiencePrompt(context);
                }
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(Key.REW_EXP, null);
                return new QuestRewardsPrompt(context);
            }
            return new QuestRewardsPrompt(context);
        }
    }
    
    public class QuestRewardsCommandsPrompt extends QuestsEditorStringPrompt {
        
        public QuestRewardsCommandsPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("rewCommandPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel")) && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                final String[] args = input.split(BukkitLang.get("charSemi"));
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
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("invalidOption")
                                + ChatColor.DARK_RED + " (" + s.trim() + ")");
                        continue;
                    default:
                        commands.add(s.trim());
                    }
                }
                context.setSessionData(Key.REW_COMMAND, commands.isEmpty() ? null : commands);
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(Key.REW_COMMAND, null);
            }
            return new QuestRewardsPrompt(context);
        }
    }
    
    public class QuestRewardsPermissionsListPrompt extends QuestsEditorNumericPrompt {

        public QuestRewardsPermissionsListPrompt(final ConversationContext context) {
            super(context);
        }
        
        private final int size = 4;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("permissionRewardsTitle");
        }
        
        @Override
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                return ChatColor.BLUE;
            case 2:
                if (context.getSessionData(Key.REW_PERMISSION) == null) {
                    return ChatColor.GRAY;
                } else {
                    return ChatColor.BLUE;
                }
            case 3:
                return ChatColor.RED;
            case 4:
                return ChatColor.GREEN;
            default:
                return null;
            }
        }

        @Override
        public String getSelectionText(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                return ChatColor.YELLOW + BukkitLang.get("rewSetPermission");
            case 2:
                if (context.getSessionData(Key.REW_PERMISSION) == null) {
                    return ChatColor.GRAY + BukkitLang.get("rewSetPermissionWorlds");
                } else {
                    return ChatColor.YELLOW + BukkitLang.get("rewSetPermissionWorlds");
                }
            case 3:
                return ChatColor.RED + BukkitLang.get("clear");
            case 4:
                return ChatColor.GREEN + BukkitLang.get("done");
            default:
                return null;
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                if (context.getSessionData(Key.REW_PERMISSION) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> permission = (List<String>) context.getSessionData(Key.REW_PERMISSION);
                    if (permission != null) {
                        for (final String s : permission) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s);
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (context.getSessionData(Key.REW_PERMISSION) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    if (context.getSessionData(Key.REW_PERMISSION_WORLDS) == null) {
                        return ChatColor.YELLOW + "(" + BukkitLang.get("stageEditorOptional") + ")";
                    } else {
                        final StringBuilder text = new StringBuilder();
                        final List<String> permissionWorlds
                                = (List<String>) context.getSessionData(Key.REW_PERMISSION_WORLDS);
                        if (permissionWorlds != null) {
                            for (final String s : permissionWorlds) {
                                text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA)
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
        public @NotNull String getBasicPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenNumericPromptEvent event
                    = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.GOLD + getTitle(context));
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i)).append(" ")
                        .append(getAdditionalText(context, i));
            }
            return text.toString();
        }

        @Override
        protected Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
            switch (input.intValue()) {
            case 1:
                return new QuestPermissionsPrompt(context);
            case 2:
                return new QuestPermissionsWorldsPrompt(context);
            case 3:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("rewPermissionsCleared"));
                context.setSessionData(Key.REW_PERMISSION, null);
                context.setSessionData(Key.REW_PERMISSION_WORLDS, null);
                return new QuestRewardsPermissionsListPrompt(context);
            case 4:
                return new QuestRewardsPrompt(context);
            default:
                return null;
            }
        }
        
    }

    public class QuestPermissionsPrompt extends QuestsEditorStringPrompt {
        
        public QuestPermissionsPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("rewPermissionsPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel")) && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
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
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("invalidOption")
                        + ChatColor.DARK_RED + " (" + s.trim() + ")");
                    } else {
                        permissions.add(s.trim());
                    }
                }
                context.setSessionData(Key.REW_PERMISSION, permissions);
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(Key.REW_PERMISSION, null);
            }
            return new QuestRewardsPermissionsListPrompt(context);
        }
    }
    
    public class QuestPermissionsWorldsPrompt extends QuestsEditorStringPrompt {
        
        public QuestPermissionsWorldsPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("rewPermissionsWorldPrompt");
        }
        
        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel")) && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                final String[] args = input.split(BukkitLang.get("charSemi"));
                final List<String> worlds = new LinkedList<>(Arrays.asList(args));
                for (final String w : worlds) {
                    if (!w.equals("null") && plugin.getServer().getWorld(w) == null) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("eventEditorInvalidWorld")
                                .replace("<input>", w));
                        return new QuestPermissionsWorldsPrompt(context);
                    }
                }
                context.setSessionData(Key.REW_PERMISSION_WORLDS, worlds);
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(Key.REW_PERMISSION_WORLDS, null);
            }
            return new QuestRewardsPermissionsListPrompt(context);
        }
    }

    public class QuestRewardsMcMMOListPrompt extends QuestsEditorNumericPrompt {

        public QuestRewardsMcMMOListPrompt(final ConversationContext context) {
            super(context);
        }
        
        private final int size = 4;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("mcMMORewardsTitle");
        }
        
        @Override
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
            switch (number) {
                case 1:
                case 2:
                    return ChatColor.BLUE;
                case 3:
                    return ChatColor.RED;
                case 4:
                    return ChatColor.GREEN;
                default:
                    return null;
            }
        }
        
        @Override
        public String getSelectionText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                return ChatColor.YELLOW + BukkitLang.get("reqSetSkills");
            case 2:
                return ChatColor.YELLOW + BukkitLang.get("reqSetSkillAmounts");
            case 3:
                return ChatColor.RED + BukkitLang.get("clear");
            case 4:
                return ChatColor.GREEN + BukkitLang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                if (context.getSessionData(Key.REW_MCMMO_SKILLS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> skills = (List<String>) context.getSessionData(Key.REW_MCMMO_SKILLS);
                    if (skills != null) {
                        for (final String s : skills) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s);
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (context.getSessionData(Key.REW_MCMMO_AMOUNTS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<Integer> amounts = (List<Integer>) context.getSessionData(Key.REW_MCMMO_AMOUNTS);
                    if (amounts != null) {
                        for (final Integer i : amounts) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(i);
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
        public @NotNull String getBasicPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenNumericPromptEvent event
                    = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle(context) + " -");
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i)).append(" ")
                        .append(getAdditionalText(context, i));
            }
            return text.toString();
        }
        
        @SuppressWarnings("unchecked")
        @Override
        protected Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
            switch(input.intValue()) {
            case 1:
                return new QuestMcMMOSkillsPrompt(context);
            case 2:
                if (context.getSessionData(Key.REW_MCMMO_SKILLS) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("rewSetMcMMOSkillsFirst"));
                    return new QuestRewardsMcMMOListPrompt(context);
                } else {
                    return new QuestMcMMOAmountsPrompt(context);
                }
            case 3:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("rewMcMMOCleared"));
                context.setSessionData(Key.REW_MCMMO_SKILLS, null);
                context.setSessionData(Key.REW_MCMMO_AMOUNTS, null);
                return new QuestRewardsMcMMOListPrompt(context);
            case 4:
                final int one;
                final int two;
                final List<Integer> skills = (List<Integer>) context.getSessionData(Key.REW_MCMMO_SKILLS);
                final List<Integer> amounts = (List<Integer>) context.getSessionData(Key.REW_MCMMO_AMOUNTS);
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
                    return new QuestRewardsPrompt(context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("listsNotSameSize"));
                    return new QuestRewardsMcMMOListPrompt(context);
                }
            default:
                return new QuestRewardsMcMMOListPrompt(context);
            }
        }
    }

    public class QuestMcMMOSkillsPrompt extends QuestsEditorStringPrompt {
        
        public QuestMcMMOSkillsPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("skillListTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("rewMcMMOPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder skillList = new StringBuilder(ChatColor.DARK_GREEN + getTitle(context) + "\n");
            final SkillType[] skills = SkillType.values();
            for (final SkillType skill : skills) {
                skillList.append(ChatColor.GREEN).append(skill.getName()).append("\n");
            }
            return skillList.toString() + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final String[] args = input.split(" ");
                final List<String> skills = new LinkedList<>();
                for (final String s : args) {
                    if (plugin.getDependencies().getMcMMOSkill(s) != null) {
                        if (!skills.contains(s)) {
                            skills.add(BukkitMiscUtil.getCapitalized(s));
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("listDuplicate"));
                            return new QuestMcMMOSkillsPrompt(context);
                        }
                    } else {
                        String text = BukkitLang.get("reqMcMMOError");
                        text = text.replace("<input>", s);
                        context.getForWhom().sendRawMessage(ChatColor.RED + text);
                        return new QuestMcMMOSkillsPrompt(context);
                    }
                }
                context.setSessionData(Key.REW_MCMMO_SKILLS, skills);
            }
            return new QuestRewardsMcMMOListPrompt(context);
        }
    }

    public class QuestMcMMOAmountsPrompt extends QuestsEditorStringPrompt {
        
        public QuestMcMMOAmountsPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("reqMcMMOAmountsPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final String[] args = input.split(" ");
                final List<Integer> amounts = new LinkedList<>();
                for (final String s : args) {
                    try {
                        amounts.add(Integer.parseInt(s));
                    } catch (final NumberFormatException e) {
                        String text = BukkitLang.get("reqNotANumber");
                        text = text.replace("<input>", s);
                        context.getForWhom().sendRawMessage(ChatColor.RED + text);
                        return new QuestMcMMOAmountsPrompt(context);
                    }
                }
                context.setSessionData(Key.REW_MCMMO_AMOUNTS, amounts);
            }
            return new QuestRewardsMcMMOListPrompt(context);
        }
    }

    public class QuestRewardsHeroesListPrompt extends QuestsEditorNumericPrompt {

        public QuestRewardsHeroesListPrompt(final ConversationContext context) {
            super(context);
        }
        
        private final int size = 4;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("heroesRewardsTitle");
        }
        
        @Override
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
            switch (number) {
                case 1:
                case 2:
                    return ChatColor.BLUE;
                case 3:
                    return ChatColor.RED;
                case 4:
                    return ChatColor.GREEN;
                default:
                    return null;
            }
        }
        
        @Override
        public String getSelectionText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                return ChatColor.YELLOW + BukkitLang.get("rewSetHeroesClasses");
            case 2:
                return ChatColor.YELLOW + BukkitLang.get("rewSetHeroesAmounts");
            case 3:
                return ChatColor.RED + BukkitLang.get("clear");
            case 4:
                return ChatColor.GREEN + BukkitLang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                if (context.getSessionData(Key.REW_HEROES_CLASSES) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> classes = (List<String>) context.getSessionData(Key.REW_HEROES_CLASSES);
                    if (classes != null) {
                        for (final String s : classes) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s);
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (context.getSessionData(Key.REW_HEROES_AMOUNTS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<Double> amounts = (List<Double>) context.getSessionData(Key.REW_HEROES_AMOUNTS);
                    if (amounts != null) {
                        for (final Double d : amounts) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(d);
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
        public @NotNull String getBasicPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenNumericPromptEvent event
                    = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle(context) + " -");
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i)).append(" ")
                        .append(getAdditionalText(context, i));
            }
            return text.toString();
        }
        
        @SuppressWarnings("unchecked")
        @Override
        protected Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
            switch(input.intValue()) {
            case 1:
                return new QuestHeroesClassesPrompt(context);
            case 2:
                if (context.getSessionData(Key.REW_HEROES_CLASSES) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("rewSetHeroesClassesFirst"));
                    return new QuestRewardsHeroesListPrompt(context);
                } else {
                    return new QuestHeroesExperiencePrompt(context);
                }
            case 3:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("rewHeroesCleared"));
                context.setSessionData(Key.REW_HEROES_CLASSES, null);
                context.setSessionData(Key.REW_HEROES_AMOUNTS, null);
                return new QuestRewardsHeroesListPrompt(context);
            case 4:
                final int one;
                final int two;
                final List<Integer> classes = (List<Integer>) context.getSessionData(Key.REW_HEROES_CLASSES);
                final List<Double> amounts = (List<Double>) context.getSessionData(Key.REW_HEROES_AMOUNTS);
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
                    return new QuestRewardsPrompt(context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("rewHeroesListsNotSameSize"));
                    return new QuestRewardsHeroesListPrompt(context);
                }
            default:
                return new QuestRewardsHeroesListPrompt(context);
            }
        }
    }

    public class QuestHeroesClassesPrompt extends QuestsEditorStringPrompt {
        
        public QuestHeroesClassesPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("heroesClassesTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("rewHeroesClassesPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            StringBuilder text = new StringBuilder(ChatColor.DARK_PURPLE + getTitle(context) + "\n");
            final List<String> list = new LinkedList<>();
            for (final HeroClass hc : plugin.getDependencies().getHeroes().getClassManager().getClasses()) {
                list.add(hc.getName());
            }
            if (list.isEmpty()) {
                text.append(ChatColor.GRAY).append("(").append(BukkitLang.get("none")).append(")\n");
            } else {
                Collections.sort(list);
                for (final String s : list) {
                    text.append(ChatColor.LIGHT_PURPLE).append(s).append(", ");
                }
                text = new StringBuilder(text.substring(0, text.length() - 2) + "\n");
            }
            text.append(ChatColor.YELLOW).append(getQueryText(context));
            return text.toString();
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final String[] arr = input.split(" ");
                final List<String> classes = new LinkedList<>();
                for (final String s : arr) {
                    final HeroClass hc = plugin.getDependencies().getHeroes().getClassManager().getClass(s);
                    if (hc == null) {
                        String text = BukkitLang.get("rewHeroesInvalidClass");
                        text = text.replace("<input>", s);
                        context.getForWhom().sendRawMessage(ChatColor.RED + text);
                        return new QuestHeroesClassesPrompt(context);
                    } else {
                        classes.add(hc.getName());
                    }
                }
                context.setSessionData(Key.REW_HEROES_CLASSES, classes);
            }
            return new QuestRewardsHeroesListPrompt(context);
        }
    }

    public class QuestHeroesExperiencePrompt extends QuestsEditorStringPrompt {
        
        public QuestHeroesExperiencePrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("heroesExperienceTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("rewHeroesExperiencePrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            String text = getTitle(context) + "\n";
            text += ChatColor.YELLOW + getQueryText(context);
            return text;
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final String[] arr = input.split(" ");
                final List<Double> amounts = new LinkedList<>();
                for (final String s : arr) {
                    try {
                        final double d = Double.parseDouble(s);
                        amounts.add(d);
                    } catch (final NumberFormatException nfe) {
                        String text = BukkitLang.get("reqNotANumber");
                        text = text.replace("<input>", s);
                        context.getForWhom().sendRawMessage(ChatColor.RED + text);
                        return new QuestHeroesExperiencePrompt(context);
                    }
                }
                context.setSessionData(Key.REW_HEROES_AMOUNTS, amounts);
            }
            return new QuestRewardsHeroesListPrompt(context);
        }
    }
    
    public class QuestRewardsPartiesExperiencePrompt extends QuestsEditorStringPrompt {
        
        public QuestRewardsPartiesExperiencePrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }
        
        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("rewPartiesExperiencePrompt");
        }
        
        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }
        
        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel")) && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i > 0) {
                        context.setSessionData(Key.REW_PARTIES_EXPERIENCE, i);
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("inputPosNum"));
                        return new QuestRewardsPartiesExperiencePrompt(context);
                    }
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                            .replace("<input>", input));
                    return new QuestRewardsPartiesExperiencePrompt(context);
                }
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(Key.REW_PARTIES_EXPERIENCE, null);
                return new QuestRewardsPrompt(context);
            }
            return new QuestRewardsPrompt(context);
        }
    }

    public class QuestCustomRewardModulePrompt extends QuestsEditorStringPrompt {

        public QuestCustomRewardModulePrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("stageEditorModules");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("stageEditorModulePrompt");
        }

        @Override
        public @NotNull String getPromptText(@NotNull final ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);

            if (!(context.getForWhom() instanceof Player) || !plugin.getConfigSettings().canClickablePrompts()) {
                final StringBuilder text = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle(context) + "\n");
                if (plugin.getCustomRewards().isEmpty()) {
                    text.append(ChatColor.DARK_AQUA).append(ChatColor.UNDERLINE)
                            .append("https://pikamug.gitbook.io/quests/casual/modules").append(ChatColor.RESET)
                            .append("\n");
                    text.append(ChatColor.RED).append("(").append(BukkitLang.get("stageEditorNoModules")).append(")")
                            .append("\n");
                } else {
                    for (final String name : plugin.getCustomRewards().stream().map(CustomReward::getModuleName)
                            .collect(Collectors.toCollection(TreeSet::new))) {
                        text.append(ChatColor.DARK_PURPLE).append("  - ").append(name).append("\n");
                    }
                }
                return text.toString() + ChatColor.YELLOW + getQueryText(context);
            }
            final TextComponent component = new TextComponent(getTitle(context) + "\n");
            component.setColor(net.md_5.bungee.api.ChatColor.LIGHT_PURPLE);
            final TextComponent line = new TextComponent("");
            if (plugin.getCustomRewards().isEmpty()) {
                final TextComponent link = new TextComponent("https://pikamug.gitbook.io/quests/casual/modules\n");
                link.setColor(net.md_5.bungee.api.ChatColor.DARK_AQUA);
                link.setUnderlined(true);
                line.addExtra(link);
                line.addExtra(ChatColor.RED + "(" + BukkitLang.get("stageEditorNoModules") + ")\n");
            } else {
                for (final String name : plugin.getCustomRewards().stream().map(CustomReward::getModuleName)
                        .collect(Collectors.toCollection(TreeSet::new))) {
                    final TextComponent click = new TextComponent(ChatColor.DARK_PURPLE + "  - " + name + "\n");
                    click.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/quests choice " + name));
                    line.addExtra(click);
                }
            }
            component.addExtra(line);
            component.addExtra(ChatColor.YELLOW + getQueryText(context));
            ((Player)context.getForWhom()).spigot().sendMessage(component);
            return "";
        }

        @Override
        public Prompt acceptInput(@NotNull final ConversationContext context, @Nullable final String input) {
            if (input != null && !input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                String found = null;
                // Check if we have a module with the specified name
                for (final CustomReward cr : plugin.getCustomRewards()) {
                    if (cr.getModuleName().equalsIgnoreCase(input)) {
                        found = cr.getModuleName();
                        break;
                    }
                }
                if (found == null) {
                    // No? Check again, but with locale sensitivity
                    for (final CustomReward cr : plugin.getCustomRewards()) {
                        if (cr.getModuleName().toLowerCase().contains(input.toLowerCase())) {
                            found = cr.getModuleName();
                            break;
                        }
                    }
                }
                if (found != null) {
                    return new QuestCustomRewardsPrompt(found, context);
                }
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                return new QuestRewardsPrompt(context);
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(Key.REW_CUSTOM, null);
                context.setSessionData(Key.REW_CUSTOM_DATA, null);
                context.setSessionData(Key.REW_CUSTOM_DATA_TEMP, null);
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("rewCustomCleared"));
                return new QuestRewardsPrompt(context);
            }
            context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("rewCustomNotFound"));
            return new QuestCustomRewardModulePrompt(context);
        }
    }

    public class QuestCustomRewardsPrompt extends QuestsEditorStringPrompt {

        private final String moduleName;
        
        public QuestCustomRewardsPrompt(final String moduleName, final ConversationContext context) {
            super(context);
            this.moduleName = moduleName;
        }

        public String getModuleName() {
            return moduleName;
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("customRewardsTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("rewCustomRewardPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);

            if (!(context.getForWhom() instanceof Player) || !plugin.getConfigSettings().canClickablePrompts()) {
                final StringBuilder text = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle(context) + "\n");
                if (plugin.getCustomRewards().isEmpty()) {
                    text.append(ChatColor.DARK_AQUA).append(ChatColor.UNDERLINE)
                            .append("https://pikamug.gitbook.io/quests/casual/modules\n");
                    text.append(ChatColor.RED).append("(").append(BukkitLang.get("stageEditorNoModules")).append(")\n");
                } else {
                    for (final CustomReward cr : plugin.getCustomRewards()) {
                        if (cr.getModuleName().equals(moduleName)) {
                            text.append(ChatColor.DARK_PURPLE).append("  - ").append(cr.getName()).append("\n");
                        }
                    }
                }
                return text.toString() + ChatColor.YELLOW + getQueryText(context);
            }
            final TextComponent component = new TextComponent(getTitle(context) + "\n");
            component.setColor(net.md_5.bungee.api.ChatColor.LIGHT_PURPLE);
            final TextComponent line = new TextComponent("");
            if (plugin.getCustomRewards().isEmpty()) {
                final TextComponent link = new TextComponent("https://pikamug.gitbook.io/quests/casual/modules\n");
                link.setColor(net.md_5.bungee.api.ChatColor.DARK_AQUA);
                link.setUnderlined(true);
                line.addExtra(link);
                line.addExtra(ChatColor.RED + "(" + BukkitLang.get("stageEditorNoModules") + ")\n");
            } else {
                for (final CustomReward co : plugin.getCustomRewards()) {
                    if (co.getModuleName().equals(moduleName)) {
                        final TextComponent click = new TextComponent(ChatColor.DARK_PURPLE + "  - " + co.getName()
                                + "\n");
                        click.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/quests choice "
                                + co.getName()));
                        line.addExtra(click);
                    }
                }
            }
            component.addExtra(line);
            component.addExtra(ChatColor.YELLOW + getQueryText(context));
            ((Player)context.getForWhom()).spigot().sendMessage(component);
            return "";
        }

        @SuppressWarnings("unchecked")
        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel")) && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
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
                    if (context.getSessionData(Key.REW_CUSTOM) != null) {
                        // The custom reward may already have been added, so let's check that
                        final LinkedList<String> list = (LinkedList<String>) context.getSessionData(Key.REW_CUSTOM);
                        final LinkedList<Map<String, Object>> dataMapList
                                = (LinkedList<Map<String, Object>>) context.getSessionData(Key.REW_CUSTOM_DATA);
                        if (list != null && dataMapList != null && !list.contains(found.getName())) {
                            // Hasn't been added yet, so let's do it
                            list.add(found.getName());
                            dataMapList.add(found.getData());
                            context.setSessionData(Key.REW_CUSTOM, list);
                            context.setSessionData(Key.REW_CUSTOM_DATA, dataMapList);
                        } else {
                            // Already added, so inform user
                            context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("rewCustomAlreadyAdded"));
                            return new QuestCustomRewardsPrompt(moduleName, context);
                        }
                    } else {
                        // The custom reward hasn't been added yet, so let's do it
                        final LinkedList<Map<String, Object>> dataMapList = new LinkedList<>();
                        dataMapList.add(found.getData());
                        final LinkedList<String> list = new LinkedList<>();
                        list.add(found.getName());
                        context.setSessionData(Key.REW_CUSTOM, list);
                        context.setSessionData(Key.REW_CUSTOM_DATA, dataMapList);
                    }
                    // Send user to the custom data prompt if there is any needed
                    if (!found.getData().isEmpty()) {
                        context.setSessionData(Key.REW_CUSTOM_DATA_DESCRIPTIONS, found.getDescriptions());
                        return new QuestRewardCustomDataListPrompt();
                    }
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("rewCustomNotFound"));
                    return new QuestCustomRewardsPrompt(moduleName, context);
                }
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(Key.REW_CUSTOM, null);
                context.setSessionData(Key.REW_CUSTOM_DATA, null);
                context.setSessionData(Key.REW_CUSTOM_DATA_TEMP, null);
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("rewCustomCleared"));
            }
            return new QuestRewardsPrompt(context);
        }
    }

    private class QuestRewardCustomDataListPrompt extends StringPrompt {

        @SuppressWarnings("unchecked")
        @Override
        public @NotNull String getPromptText(final ConversationContext context) {
            final StringBuilder text = new StringBuilder(ChatColor.GOLD + "- ");
            final LinkedList<String> list = (LinkedList<String>) context.getSessionData(Key.REW_CUSTOM);
            final LinkedList<Map<String, Object>> dataMapList
                    = (LinkedList<Map<String, Object>>) context.getSessionData(Key.REW_CUSTOM_DATA);
            if (list != null && dataMapList != null) {
                final String rewName = list.getLast();
                final Map<String, Object> dataMap = dataMapList.getLast();
                text.append(rewName).append(" -\n");
                int index = 1;
                final LinkedList<String> dataMapKeys = new LinkedList<>(dataMap.keySet());
                Collections.sort(dataMapKeys);
                for (final String dataKey : dataMapKeys) {
                    text.append(ChatColor.BLUE).append(ChatColor.BOLD).append(index).append(ChatColor.RESET)
                            .append(ChatColor.YELLOW).append(" - ").append(dataKey);
                    if (dataMap.get(dataKey) != null) {
                        text.append(ChatColor.GRAY).append(" (").append(ChatColor.AQUA)
                                .append(ChatColor.translateAlternateColorCodes('&', dataMap.get(dataKey).toString()))
                                .append(ChatColor.GRAY).append(")\n");
                    } else {
                        text.append(ChatColor.GRAY).append(" (").append(BukkitLang.get("noneSet")).append(ChatColor.GRAY)
                                .append(")\n");
                    }
                    index++;
                }
                text.append(ChatColor.GREEN).append(ChatColor.BOLD).append(index).append(ChatColor.YELLOW).append(" - ")
                        .append(BukkitLang.get("done"));
            }
            return text.toString();
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            @SuppressWarnings("unchecked")
            final LinkedList<Map<String, Object>> dataMapList
                    = (LinkedList<Map<String, Object>>) context.getSessionData(Key.REW_CUSTOM_DATA);
            if (dataMapList != null) {
                final Map<String, Object> dataMap = dataMapList.getLast();
                final int numInput;
                try {
                    numInput = Integer.parseInt(input);
                } catch (final NumberFormatException nfe) {
                    return new QuestRewardCustomDataListPrompt();
                }
                if (numInput < 1 || numInput > dataMap.size() + 1) {
                    return new QuestRewardCustomDataListPrompt();
                }
                if (numInput < dataMap.size() + 1) {
                    final LinkedList<String> dataMapKeys = new LinkedList<>(dataMap.keySet());
                    Collections.sort(dataMapKeys);
                    final String selectedKey = dataMapKeys.get(numInput - 1);
                    context.setSessionData(Key.REW_CUSTOM_DATA_TEMP, selectedKey);
                    return new QuestRewardCustomDataPrompt();
                } else {
                    if (dataMap.containsValue(null)) {
                        return new QuestRewardCustomDataListPrompt();
                    } else {
                        context.setSessionData(Key.REW_CUSTOM_DATA_DESCRIPTIONS, null);
                    }
                }
            }
            return new QuestRewardsPrompt(context);
        }
    }

    private class QuestRewardCustomDataPrompt extends StringPrompt {

        @Override
        public @NotNull String getPromptText(final ConversationContext context) {
            String text = "";
            final String temp = (String) context.getSessionData(Key.REW_CUSTOM_DATA_TEMP);
            @SuppressWarnings("unchecked")
            final
            Map<String, String> descriptions 
                    = (Map<String, String>) context.getSessionData(Key.REW_CUSTOM_DATA_DESCRIPTIONS);
            if (temp != null && descriptions != null) {
                if (descriptions.get(temp) != null) {
                    text += descriptions.get(temp) + "\n";
                }
                String lang = BukkitLang.get("stageEditorCustomDataPrompt");
                lang = lang.replace("<data>", temp);
                text += ChatColor.YELLOW + lang;
            }
            return text;
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            @SuppressWarnings("unchecked")
            final
            LinkedList<Map<String, Object>> dataMapList
                    = (LinkedList<Map<String, Object>>) context.getSessionData(Key.REW_CUSTOM_DATA);
            if (dataMapList != null) {
                final Map<String, Object> dataMap = dataMapList.getLast();
                dataMap.put((String) context.getSessionData(Key.REW_CUSTOM_DATA_TEMP), input);
                context.setSessionData(Key.REW_CUSTOM_DATA_TEMP, null);
            }
            return new QuestRewardCustomDataListPrompt();
        }
    }
}
