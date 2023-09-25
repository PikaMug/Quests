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

import com.gmail.nossr50.datatypes.skills.SkillType;
import com.herocraftonline.heroes.characters.classes.HeroClass;
import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.convo.generic.ItemStackPrompt;
import me.pikamug.quests.convo.generic.OverridePrompt;
import me.pikamug.quests.convo.quests.QuestsEditorNumericPrompt;
import me.pikamug.quests.convo.quests.QuestsEditorStringPrompt;
import me.pikamug.quests.events.editor.quests.QuestsEditorPostOpenNumericPromptEvent;
import me.pikamug.quests.events.editor.quests.QuestsEditorPostOpenStringPromptEvent;
import me.pikamug.quests.module.CustomRequirement;
import me.pikamug.quests.quests.Quest;
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

public class QuestRequirementsPrompt extends QuestsEditorNumericPrompt {

    private final BukkitQuestsPlugin plugin;
    private final String classPrefix;
    private boolean hasRequirement = false;
    private final int size = 12;
    
    public QuestRequirementsPrompt(final ConversationContext context) {
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
        return BukkitLang.get("requirementsTitle").replace("<quest>", (String) Objects
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
        case 7:
        case 10:
            return ChatColor.BLUE;
        case 8:
            if (plugin.getDependencies().getMcmmoClassic() != null) {
                return ChatColor.BLUE;
            } else {
                return ChatColor.GRAY;
            }
        case 9:
            if (plugin.getDependencies().getHeroes() != null) {
                return ChatColor.BLUE;
            } else {
                return ChatColor.GRAY;
            }
        case 11:
            if (context.getSessionData(Key.REQ_FAIL_MESSAGE) == null) {
                if (!hasRequirement) {
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
                return ChatColor.YELLOW + BukkitLang.get("reqSetMoney");
            } else {
                return ChatColor.GRAY + BukkitLang.get("reqSetMoney");
            }
        case 2:
            return ChatColor.YELLOW + BukkitLang.get("reqSetQuestPoints").replace("<points>", BukkitLang.get("questPoints"));
        case 3:
            return ChatColor.YELLOW + BukkitLang.get("reqSetItem");
        case 4:
            return ChatColor.YELLOW + BukkitLang.get("reqSetExperience");
        case 5:
            return ChatColor.YELLOW + BukkitLang.get("reqSetPerms");
        case 6:
            return ChatColor.YELLOW + BukkitLang.get("reqSetQuest");
        case 7:
            return ChatColor.YELLOW + BukkitLang.get("reqSetQuestBlocks");
        case 8:
            if (plugin.getDependencies().getMcmmoClassic() != null) {
                return ChatColor.YELLOW + BukkitLang.get("reqSetMcMMO");
            } else {
                return ChatColor.GRAY + BukkitLang.get("reqSetMcMMO");
            }
        case 9:
            if (plugin.getDependencies().getHeroes() != null) {
                return ChatColor.YELLOW + BukkitLang.get("reqSetHeroes");
            } else {
                return ChatColor.GRAY + BukkitLang.get("reqSetHeroes");
            }
        case 10:
            return ChatColor.DARK_PURPLE + BukkitLang.get("reqSetCustom");
        case 11:
            if (!hasRequirement) {
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
                final Integer moneyReq = (Integer) context.getSessionData(Key.REQ_MONEY);
                if (moneyReq == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + moneyReq + " " 
                            + (moneyReq > 1 ? plugin.getDependencies().getVaultEconomy().currencyNamePlural()
                            : plugin.getDependencies().getVaultEconomy().currencyNameSingular() + ChatColor.GRAY + ")");
                }
            } else {
                return ChatColor.GRAY + "(" + BukkitLang.get("notInstalled") + ")";
            }
        case 2:
            if (context.getSessionData(Key.REQ_QUEST_POINTS) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + context.getSessionData(Key.REQ_QUEST_POINTS) + " "
                        + BukkitLang.get("questPoints") + ChatColor.GRAY + ")";
            }
        case 3:
            if (context.getSessionData(Key.REQ_ITEMS) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<ItemStack> items = (LinkedList<ItemStack>) context.getSessionData(Key.REQ_ITEMS);
                if (items != null) {
                    for (final ItemStack item : items) {
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.BLUE)
                                .append(BukkitItemUtil.getName(item)).append(ChatColor.GRAY).append(" x ")
                                .append(ChatColor.AQUA).append(item.getAmount());
                    }
                }
                return text.toString();
            }
        case 4:
            if (context.getSessionData(Key.REQ_EXP) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + context.getSessionData(Key.REQ_EXP) + " "
                        + BukkitLang.get("points") + ChatColor.GRAY + ")";
            }
        case 5:
            if (context.getSessionData(Key.REQ_PERMISSION) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final List<String> perms = (List<String>) context.getSessionData(Key.REQ_PERMISSION);
                if (perms != null) {
                    for (final String s : perms) {
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s);
                    }
                }
                return text.toString();
            }
        case 6:
            if (context.getSessionData(Key.REQ_QUEST) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final List<String> questReq = (List<String>) context.getSessionData(Key.REQ_QUEST);
                if (questReq != null) {
                    for (String s : questReq) {
                        if (plugin.getQuestById(s) != null) {
                            s = plugin.getQuestById(s).getName();
                        }
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s);
                    }
                }
                return text.toString();
            }
        case 7:
            if (context.getSessionData(Key.REQ_QUEST_BLOCK) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final List<String> questBlockReq = (List<String>) context.getSessionData(Key.REQ_QUEST_BLOCK);
                if (questBlockReq != null) {
                    for (String s : questBlockReq) {
                        if (plugin.getQuestById(s) != null) {
                            s = plugin.getQuestById(s).getName();
                        }
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s);
                    }
                }
                return text.toString();
            }
        case 8:
            if (plugin.getDependencies().getMcmmoClassic() != null) {
                if (context.getSessionData(Key.REQ_MCMMO_SKILLS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> skills = (List<String>) context.getSessionData(Key.REQ_MCMMO_SKILLS);
                    final List<Integer> amounts = (List<Integer>) context.getSessionData(Key.REQ_MCMMO_SKILL_AMOUNTS);
                    if (skills != null && amounts != null) {
                        for (final String s : skills) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.DARK_GREEN)
                                    .append(s).append(ChatColor.RESET).append(ChatColor.YELLOW).append(" ")
                                    .append(BukkitLang.get("mcMMOLevel")).append(" ").append(ChatColor.GREEN)
                                    .append(amounts.get(skills.indexOf(s)));
                        }
                    }
                    return text.toString();
                }
            } else {
                return ChatColor.GRAY + "(" + BukkitLang.get("notInstalled") + ")";
            }
        case 9:
            if (plugin.getDependencies().getHeroes() != null) {
                if (context.getSessionData(Key.REQ_HEROES_PRIMARY_CLASS) == null
                        && context.getSessionData(Key.REQ_HEROES_SECONDARY_CLASS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")\n";
                } else {
                    String text = "\n";
                    if (context.getSessionData(Key.REQ_HEROES_PRIMARY_CLASS) != null) {
                        text += ChatColor.AQUA + "    " + BukkitLang.get("reqHeroesPrimaryDisplay") + " "
                                + ChatColor.BLUE + context.getSessionData(Key.REQ_HEROES_PRIMARY_CLASS);
                    }
                    if (context.getSessionData(Key.REQ_HEROES_SECONDARY_CLASS) != null) {
                        text += ChatColor.AQUA + "    " + BukkitLang.get("reqHeroesSecondaryDisplay") + " "
                                + ChatColor.BLUE + context.getSessionData(Key.REQ_HEROES_SECONDARY_CLASS);
                    }
                    return text;
                }
            } else {
                return ChatColor.GRAY + "(" + BukkitLang.get("notInstalled") + ")";
            }
        case 10:
           if (context.getSessionData(Key.REQ_CUSTOM) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
           } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> customReq = (LinkedList<String>) context.getSessionData(Key.REQ_CUSTOM);
                if (customReq != null) {
                    for (final String s : customReq) {
                        text.append("\n").append(ChatColor.LIGHT_PURPLE).append("     - ").append(s);
                    }
                }
                return text.toString();
           }
        case 11:
            if (context.getSessionData(Key.REQ_FAIL_MESSAGE) == null) {
                if (!hasRequirement) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("stageEditorOptional") + ")";
                } else {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                }
            } else {
                final StringBuilder text = new StringBuilder();
                final List<String> overrides = (List<String>) context.getSessionData(Key.REQ_FAIL_MESSAGE);
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
                context.setSessionData(Key.REQ_FAIL_MESSAGE, null);
            } else {
                final LinkedList<String> overrides = new LinkedList<>();
                if (context.getSessionData(Key.REQ_FAIL_MESSAGE) != null) {
                    overrides.addAll((List<String>) context.getSessionData(Key.REQ_FAIL_MESSAGE));
                }
                overrides.add(input);
                context.setSessionData(Key.REQ_FAIL_MESSAGE, overrides);
                context.setSessionData(classPrefix + "-override", null);
            }
        }
        checkRequirement(context);

        final QuestsEditorPostOpenNumericPromptEvent event
                = new QuestsEditorPostOpenNumericPromptEvent(context, this);
        plugin.getServer().getPluginManager().callEvent(event);
        
        final StringBuilder text = new StringBuilder(ChatColor.DARK_AQUA + "- "  + getTitle(context)
                .replace((String) Objects.requireNonNull(context.getSessionData(Key.Q_NAME)), ChatColor.AQUA
                + (String) context.getSessionData(Key.Q_NAME) + ChatColor.DARK_AQUA) + " -");
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
                return new QuestRequirementsMoneyPrompt(context);
            } else {
                return new QuestRequirementsPrompt(context);
            }
        case 2:
            return new QuestRequirementsQuestPointsPrompt(context);
        case 3:
            return new QuestRequirementsItemListPrompt(context);
        case 4:
            return new QuestRequirementsExperiencePrompt(context);
        case 5:
            return new QuestRequirementsPermissionsPrompt(context);
        case 6:
            return new QuestRequirementsQuestListPrompt(context, true);
        case 7:
            return new QuestRequirementsQuestListPrompt(context, false);
        case 8:
            if (plugin.getDependencies().getMcmmoClassic() != null) {
                return new QuestRequirementsMcMMOListPrompt(context);
            } else {
                return new QuestRequirementsPrompt(context);
            }
        case 9:
            if (plugin.getDependencies().getHeroes() != null) {
                return new QuestRequirementsHeroesListPrompt(context);
            } else {
                return new QuestRequirementsPrompt(context);
            }
        case 10:
            return new QuestCustomRequirementModulePrompt(context);
        case 11:
            if (hasRequirement) {
                return new OverridePrompt.Builder()
                        .context(context)
                        .source(this)
                        .promptText(BukkitLang.get("overrideCreateEnter"))
                        .build();
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("invalidOption"));
                return new QuestRequirementsPrompt(context);
            }
        case 12:
            return plugin.getQuestFactory().returnToMenu(context);
        default:
            return new QuestRequirementsPrompt(context);
        }
    }
    
    public boolean checkRequirement(final ConversationContext context) {
        if (context.getSessionData(Key.REQ_MONEY) != null
                || context.getSessionData(Key.REQ_QUEST_POINTS) != null
                || context.getSessionData(Key.REQ_ITEMS) != null
                || context.getSessionData(Key.REQ_EXP) != null
                || context.getSessionData(Key.REQ_PERMISSION) != null
                || context.getSessionData(Key.REQ_QUEST) != null
                || context.getSessionData(Key.REQ_QUEST_BLOCK) != null
                || context.getSessionData(Key.REQ_MCMMO_SKILLS) != null
                || context.getSessionData(Key.REQ_HEROES_PRIMARY_CLASS) != null
                || context.getSessionData(Key.REQ_HEROES_SECONDARY_CLASS) != null
                || context.getSessionData(Key.REQ_CUSTOM) != null) {
            hasRequirement = true;
            return true;
        }
        return false;
    }

    public class QuestRequirementsMoneyPrompt extends QuestsEditorStringPrompt {
        
        public QuestRequirementsMoneyPrompt(final ConversationContext context) {
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
                text = text.replace("<money>", ChatColor.DARK_PURPLE+ ((plugin.getDependencies().getVaultEconomy()
                        .currencyNamePlural().isEmpty() ? BukkitLang.get("money") : plugin.getDependencies().getVaultEconomy()
                        .currencyNamePlural())) + ChatColor.YELLOW);
            } else {
                text = text.replace("<money>", ChatColor.DARK_PURPLE + BukkitLang.get("money") + ChatColor.YELLOW);
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
                        context.setSessionData(Key.REQ_MONEY, i);
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("inputPosNum"));
                        return new QuestRequirementsMoneyPrompt(context);
                    }
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                            .replace("<input>", input));
                    return new QuestRequirementsMoneyPrompt(context);
                }
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(Key.REQ_MONEY, null);
                return new QuestRequirementsPrompt(context);
            }
            return new QuestRequirementsPrompt(context);
        }
    }

    public class QuestRequirementsQuestPointsPrompt extends QuestsEditorStringPrompt {
        
        public QuestRequirementsQuestPointsPrompt(final ConversationContext context) {
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
                        context.setSessionData(Key.REQ_QUEST_POINTS, i);
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("inputPosNum"));
                        return new QuestRequirementsQuestPointsPrompt(context);
                    }
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                            .replace("<input>", input));
                    return new QuestRequirementsQuestPointsPrompt(context);
                }
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(Key.REQ_QUEST_POINTS, null);
                return new QuestRequirementsPrompt(context);
            }
            return new QuestRequirementsPrompt(context);
        }
    }

    public class QuestRequirementsItemListPrompt extends QuestsEditorNumericPrompt {
        
        public QuestRequirementsItemListPrompt(final ConversationContext context) {
            super(context);
        }

        private final int size = 4;

        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("itemRequirementsTitle");
        }
        
        @Override
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                return ChatColor.BLUE;
            case 2:
                if (context.getSessionData(Key.REQ_ITEMS) == null) {
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
                return ChatColor.YELLOW + BukkitLang.get("stageEditorDeliveryAddItem");
            case 2:
                if (context.getSessionData(Key.REQ_ITEMS) == null) {
                    return ChatColor.GRAY + BukkitLang.get("reqSetRemoveItems");
                } else {
                    return ChatColor.YELLOW + BukkitLang.get("reqSetRemoveItems");
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
                if (context.getSessionData(Key.REQ_ITEMS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<ItemStack> reqItems = (List<ItemStack>) context.getSessionData(Key.REQ_ITEMS);
                    if (reqItems != null) {
                        for (final ItemStack is : reqItems) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ")
                                    .append(BukkitItemUtil.getDisplayString(is));
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (context.getSessionData(Key.REQ_ITEMS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    if (context.getSessionData(Key.REQ_ITEMS_REMOVE) == null) {
                        return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                    } else {
                        final StringBuilder text = new StringBuilder();
                        final List<Boolean> reqItemsRemove
                                = (List<Boolean>) context.getSessionData(Key.REQ_ITEMS_REMOVE);
                        if (reqItemsRemove != null) {
                            for (final Boolean b : reqItemsRemove) {
                                text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA)
                                        .append(b.equals(Boolean.TRUE) ? BukkitLang.get("yesWord") : BukkitLang.get("noWord"));
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
        
        @SuppressWarnings("unchecked")
        @Override
        public @NotNull String getBasicPromptText(final ConversationContext context) {
            // Check/add newly made item
            if (context.getSessionData("tempStack") != null) {
                if (context.getSessionData(Key.REQ_ITEMS) != null) {
                    final List<ItemStack> itemReq = (List<ItemStack>) context.getSessionData(Key.REQ_ITEMS);
                    final ItemStack i = (ItemStack) context.getSessionData("tempStack");
                    if (itemReq != null && i != null) {
                        itemReq.add(i);
                    }
                    context.setSessionData(Key.REQ_ITEMS, itemReq);
                } else {
                    final LinkedList<ItemStack> itemReq = new LinkedList<>();
                    final ItemStack i = (ItemStack) context.getSessionData("tempStack");
                    if (i != null) {
                        itemReq.add(i);
                    }
                    context.setSessionData(Key.REQ_ITEMS, itemReq);
                }
                ItemStackPrompt.clearSessionData(context);
            }
            
            final QuestsEditorPostOpenNumericPromptEvent event
                    = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.GOLD + getTitle(context) + "\n");
            for (int i = 1; i <= size; i++) {
                text.append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i).append(ChatColor.RESET)
                        .append(" - ").append(getSelectionText(context, i)).append(" ")
                        .append(getAdditionalText(context, i)).append("\n");
            }
            return text.toString();
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
            switch (input.intValue()) {
            case 1:
                return new ItemStackPrompt(context, QuestRequirementsItemListPrompt.this);
            case 2:
                if (context.getSessionData(Key.REQ_ITEMS) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("reqMustAddItem"));
                    return new QuestRequirementsItemListPrompt(context);
                } else {
                    return new QuestRemoveItemsPrompt(context);
                }
            case 3:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("reqItemCleared"));
                context.setSessionData(Key.REQ_ITEMS, null);
                context.setSessionData(Key.REQ_ITEMS_REMOVE, null);
                return new QuestRequirementsItemListPrompt(context);
            case 4:
                final int missing;
                final List<ItemStack> items = (List<ItemStack>) context.getSessionData(Key.REQ_ITEMS);
                LinkedList<Boolean> remove = (LinkedList<Boolean>) context.getSessionData(Key.REQ_ITEMS_REMOVE);
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
                context.setSessionData(Key.REQ_ITEMS_REMOVE, remove);
                return new QuestRequirementsPrompt(context);
            default:
                return new QuestRequirementsPrompt(context);
            }
        }
    }

    public class QuestRemoveItemsPrompt extends QuestsEditorStringPrompt {
        
        public QuestRemoveItemsPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("reqRemoveItemsPrompt");
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
                final LinkedList<Boolean> booleans = new LinkedList<>();
                for (final String s : args) {
                    if (input.startsWith("t") || s.equalsIgnoreCase(BukkitLang.get("true"))
                            || s.equalsIgnoreCase(BukkitLang.get("yesWord"))) {
                        booleans.add(true);
                    } else if (input.startsWith("f") || s.equalsIgnoreCase(BukkitLang.get("false"))
                            || s.equalsIgnoreCase(BukkitLang.get("noWord"))) {
                        booleans.add(false);
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("itemCreateInvalidInput"));
                        return new QuestRemoveItemsPrompt(context);
                    }
                }
                context.setSessionData(Key.REQ_ITEMS_REMOVE, booleans);
            }
            return new QuestRequirementsItemListPrompt(context);
        }
    }

    public class QuestRequirementsExperiencePrompt extends QuestsEditorStringPrompt {

        public QuestRequirementsExperiencePrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("reqExperiencePrompt");
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
                        context.setSessionData(Key.REQ_EXP, i);
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("inputPosNum"));
                        return new QuestRequirementsExperiencePrompt(context);
                    }
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                            .replace("<input>", input));
                    return new QuestRequirementsExperiencePrompt(context);
                }
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(Key.REQ_EXP, null);
                return new QuestRequirementsPrompt(context);
            }
            return new QuestRequirementsPrompt(context);
        }
    }

    public class QuestRequirementsPermissionsPrompt extends QuestsEditorStringPrompt {
        
        public QuestRequirementsPermissionsPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("reqPermissionsPrompt");
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
                final LinkedList<String> permissions = new LinkedList<>(Arrays.asList(args));
                context.setSessionData(Key.REQ_PERMISSION, permissions);
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(Key.REQ_PERMISSION, null);
            }
            return new QuestRequirementsPrompt(context);
        }
    }

    public class QuestRequirementsQuestListPrompt extends QuestsEditorStringPrompt {

        private final boolean isRequiredQuest;

        public QuestRequirementsQuestListPrompt(final ConversationContext context, final boolean isRequired) {
            super(context);
            this.isRequiredQuest = isRequired;
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("reqQuestListTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("reqQuestPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final List<String> names = plugin.getLoadedQuests().stream().map(Quest::getName)
                    .collect(Collectors.toList());
            return sendClickableMenu(getTitle(context), names, getQueryText(context), context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel")) && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                final String[] args = input.split(BukkitLang.get("charSemi"));
                final LinkedList<String> questIds = new LinkedList<>();
                for (String s : args) {
                    s = s.trim();
                    if (plugin.getQuest(s) == null) {
                        String text = BukkitLang.get("reqNotAQuestName");
                        text = text.replace("<quest>", ChatColor.LIGHT_PURPLE + s + ChatColor.RED);
                        context.getForWhom().sendRawMessage(text);
                        return new QuestRequirementsQuestListPrompt(context, isRequiredQuest);
                    }
                    if (questIds.contains(plugin.getQuest(s).getId())) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("listDuplicate"));
                        return new QuestRequirementsQuestListPrompt(context, isRequiredQuest);
                    }
                    questIds.add(plugin.getQuest(s).getId());
                }
                if (isRequiredQuest) {
                    context.setSessionData(Key.REQ_QUEST, questIds);
                } else {
                    context.setSessionData(Key.REQ_QUEST_BLOCK, questIds);
                }
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                if (isRequiredQuest) {
                    context.setSessionData(Key.REQ_QUEST, null);
                } else {
                    context.setSessionData(Key.REQ_QUEST_BLOCK, null);
                }
            }
            return new QuestRequirementsPrompt(context);
        }
    }

    public class QuestRequirementsMcMMOListPrompt extends QuestsEditorNumericPrompt {

        public QuestRequirementsMcMMOListPrompt(final ConversationContext context) {
            super(context);
        }
        
        private final int size = 3;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("mcMMORequirementsTitle");
        }
        
        @Override
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
            switch (number) {
                case 1:
                case 2:
                    return ChatColor.BLUE;
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
                return ChatColor.YELLOW + BukkitLang.get("reqSetSkills");
            case 2:
                return ChatColor.YELLOW + BukkitLang.get("reqSetSkillAmounts");
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
                if (context.getSessionData(Key.REQ_MCMMO_SKILLS) == null) {
                    return ChatColor.GRAY + " (" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final LinkedList<String> skills = (LinkedList<String>) context.getSessionData(Key.REQ_MCMMO_SKILLS);
                    if (skills != null) {
                        for (final String skill : skills) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA)
                                    .append(skill);
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (context.getSessionData(Key.REQ_MCMMO_SKILL_AMOUNTS) == null) {
                    return ChatColor.GRAY + " (" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final LinkedList<Integer> skillAmounts
                            = (LinkedList<Integer>) context.getSessionData(Key.REQ_MCMMO_SKILL_AMOUNTS);
                    if (skillAmounts != null) {
                        for (final int i : skillAmounts) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(i);
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
        public @NotNull String getBasicPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenNumericPromptEvent event
                    = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle(context) + " -\n");
            for (int i = 1; i <= size; i++) {
                text.append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i).append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i)).append(" ").append(getAdditionalText(context, i)).append("\n");
            }
            return text.toString();
        }
        
        @Override
        protected Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
            switch(input.intValue()) {
            case 1:
                return new QuestMcMMOSkillsPrompt(context);
            case 2:
                return new QuestMcMMOAmountsPrompt(context);
            case 3:
                return new QuestRequirementsPrompt(context);
            default:
                return new QuestRequirementsMcMMOListPrompt(context);
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
            for (int i = 0; i < skills.length; i++) {
                if (i == (skills.length - 1)) {
                    skillList.append(ChatColor.GREEN).append(skills[i].getName()).append("\n");
                } else {
                    skillList.append(ChatColor.GREEN).append(skills[i].getName()).append("\n\n");
                }
            }
            return skillList.toString() + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel")) && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                final LinkedList<String> skills = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    final String formatted = BukkitMiscUtil.getCapitalized(s);
                    if (plugin.getDependencies().getMcMMOSkill(formatted) != null) {
                        skills.add(formatted);
                    } else if (skills.contains(formatted)) {
                        context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("listDuplicate"));
                        return new QuestMcMMOSkillsPrompt(context);
                    } else {
                        String text = BukkitLang.get("reqMcMMOError");
                        text = text.replace("<input>", ChatColor.RED + s + ChatColor.YELLOW);
                        context.getForWhom().sendRawMessage(ChatColor.YELLOW + text);
                        return new QuestMcMMOSkillsPrompt(context);
                    }
                }
                context.setSessionData(Key.REQ_MCMMO_SKILLS, skills);
                return new QuestRequirementsMcMMOListPrompt(context);
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("reqMcMMOCleared"));
                context.setSessionData(Key.REQ_MCMMO_SKILLS, null);
                return new QuestRequirementsMcMMOListPrompt(context);
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                return new QuestRequirementsMcMMOListPrompt(context);
            }
            return new QuestMcMMOSkillsPrompt(context);
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
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel")) && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                final LinkedList<Integer> amounts = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    try {
                        final int i = Integer.parseInt(s);
                        amounts.add(i);
                    } catch (final NumberFormatException nfe) {
                        String text = BukkitLang.get("reqNotANumber");
                        text = text.replace("<input>", ChatColor.RED + s + ChatColor.YELLOW);
                        context.getForWhom().sendRawMessage(ChatColor.YELLOW + text);
                        return new QuestMcMMOAmountsPrompt(context);
                    }
                }
                context.setSessionData(Key.REQ_MCMMO_SKILL_AMOUNTS, amounts);
                return new QuestRequirementsMcMMOListPrompt(context);
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("reqMcMMOAmountsCleared"));
                context.setSessionData(Key.REQ_MCMMO_SKILL_AMOUNTS, null);
                return new QuestRequirementsMcMMOListPrompt(context);
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                return new QuestRequirementsMcMMOListPrompt(context);
            }
            return new QuestMcMMOAmountsPrompt(context);
        }
    }

    public class QuestRequirementsHeroesListPrompt extends QuestsEditorNumericPrompt {

        public QuestRequirementsHeroesListPrompt(final ConversationContext context) {
            super(context);
        }
        
        private final int size = 3;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("heroesRequirementsTitle");
        }
        
        @Override
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
            switch (number) {
                case 1:
                case 2:
                    return ChatColor.BLUE;
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
                return ChatColor.YELLOW + BukkitLang.get("reqHeroesSetPrimary");
            case 2:
                return ChatColor.YELLOW + BukkitLang.get("reqHeroesSetSecondary");
            case 3:
                return ChatColor.GREEN + BukkitLang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                if (context.getSessionData(Key.REQ_HEROES_PRIMARY_CLASS) == null) {
                    return ChatColor.GRAY + " (" + BukkitLang.get("noneSet") + ")";
                } else {
                    return "(" + ChatColor.AQUA + context.getSessionData(Key.REQ_HEROES_PRIMARY_CLASS) + ChatColor.GREEN
                            + ")\n";
                }
            case 2:
                if (context.getSessionData(Key.REQ_HEROES_SECONDARY_CLASS) == null) {
                    return ChatColor.GRAY + " (" + BukkitLang.get("noneSet") + ")";
                } else {
                    return "(" + ChatColor.AQUA + context.getSessionData(Key.REQ_HEROES_SECONDARY_CLASS)
                            + ChatColor.GREEN + ")\n";
                }
            case 3:
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

            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle(context) + " -\n");
            for (int i = 1; i <= size; i++) {
                text.append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i).append(ChatColor.RESET)
                        .append(" - ").append(getSelectionText(context, i)).append(" ")
                        .append(getAdditionalText(context, i)).append("\n");
            }
            return text.toString();
        }
        @Override
        protected Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
            switch(input.intValue()) {
            case 1:
                return new QuestHeroesPrimaryPrompt(context);
            case 2:
                return new QuestHeroesSecondaryPrompt(context);
            case 3:
                return new QuestRequirementsPrompt(context);
            default:
                return new QuestRequirementsHeroesListPrompt(context);
            }
        }
    }

    public class QuestHeroesPrimaryPrompt extends QuestsEditorStringPrompt {
        
        public QuestHeroesPrimaryPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("heroesPrimaryTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("reqHeroesPrimaryPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.DARK_PURPLE + getTitle(context) + "\n");
            final LinkedList<String> list = new LinkedList<>();
            for (final HeroClass hc : plugin.getDependencies().getHeroes().getClassManager().getClasses()) {
                if (hc.isPrimary()) {
                    list.add(hc.getName());
                }
            }
            if (list.isEmpty()) {
                text.append(ChatColor.GRAY).append("(").append(BukkitLang.get("none")).append(")\n");
            } else {
                Collections.sort(list);
                for (final String s : list) {
                    text.append(ChatColor.DARK_PURPLE).append("- ").append(ChatColor.LIGHT_PURPLE).append(s)
                            .append("\n");
                }
            }
            text.append(ChatColor.YELLOW).append(getQueryText(context));
            return text.toString();
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdClear")) && !input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final HeroClass hc = plugin.getDependencies().getHeroes().getClassManager().getClass(input);
                if (hc != null) {
                    if (hc.isPrimary()) {
                        context.setSessionData(Key.REQ_HEROES_PRIMARY_CLASS, hc.getName());
                        return new QuestRequirementsHeroesListPrompt(context);
                    } else {
                        String text = BukkitLang.get("reqHeroesNotPrimary");
                        text = text.replace("<class>", ChatColor.LIGHT_PURPLE + hc.getName() + ChatColor.RED);
                        context.getForWhom().sendRawMessage(ChatColor.RED + text);
                        return new QuestHeroesPrimaryPrompt(context);
                    }
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("reqHeroesClassNotFound"));
                    return new QuestHeroesPrimaryPrompt(context);
                }
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(Key.REQ_HEROES_PRIMARY_CLASS, null);
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("reqHeroesPrimaryCleared"));
                return new QuestRequirementsHeroesListPrompt(context);
            } else {
                return new QuestRequirementsHeroesListPrompt(context);
            }
        }
    }

    public class QuestHeroesSecondaryPrompt extends QuestsEditorStringPrompt {
        
        public QuestHeroesSecondaryPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("heroesSecondaryTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("reqHeroesSecondaryPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.DARK_PURPLE + getTitle(context) + "\n");
            final LinkedList<String> list = new LinkedList<>();
            for (final HeroClass hc : plugin.getDependencies().getHeroes().getClassManager().getClasses()) {
                if (hc.isSecondary()) {
                    list.add(hc.getName());
                }
            }
            if (list.isEmpty()) {
                text.append(ChatColor.GRAY).append("(").append(BukkitLang.get("none")).append(")\n");
            } else {
                Collections.sort(list);
                for (final String s : list) {
                    text.append(ChatColor.DARK_PURPLE).append("- ").append(ChatColor.LIGHT_PURPLE).append(s)
                            .append("\n");
                }
            }
            text.append(ChatColor.YELLOW).append(getQueryText(context));
            return text.toString();
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdClear")) && !input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final HeroClass hc = plugin.getDependencies().getHeroes().getClassManager().getClass(input);
                if (hc != null) {
                    if (hc.isSecondary()) {
                        context.setSessionData(Key.REQ_HEROES_SECONDARY_CLASS, hc.getName());
                        return new QuestRequirementsHeroesListPrompt(context);
                    } else {
                        String text = BukkitLang.get("reqHeroesNotSecondary");
                        text = text.replace("<class>", ChatColor.LIGHT_PURPLE + hc.getName() + ChatColor.RED);
                        context.getForWhom().sendRawMessage(ChatColor.RED + text);
                        return new QuestHeroesSecondaryPrompt(context);
                    }
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("reqHeroesClassNotFound"));
                    return new QuestHeroesSecondaryPrompt(context);
                }
            } else if (input.equalsIgnoreCase(BukkitLang.get("clear"))) {
                context.setSessionData(Key.REQ_HEROES_SECONDARY_CLASS, null);
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("reqHeroesSecondaryCleared"));
                return new QuestRequirementsHeroesListPrompt(context);
            } else {
                return new QuestRequirementsHeroesListPrompt(context);
            }
        }
    }

    public class QuestCustomRequirementModulePrompt extends QuestsEditorStringPrompt {

        public QuestCustomRequirementModulePrompt(final ConversationContext context) {
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
                if (plugin.getCustomRequirements().isEmpty()) {
                    text.append(ChatColor.DARK_AQUA).append(ChatColor.UNDERLINE)
                            .append("https://pikamug.gitbook.io/quests/casual/modules").append(ChatColor.RESET)
                            .append("\n");
                    text.append(ChatColor.RED).append("(").append(BukkitLang.get("stageEditorNoModules")).append(")")
                            .append("\n");
                } else {
                    for (final String name : plugin.getCustomRequirements().stream()
                            .map(CustomRequirement::getModuleName).collect(Collectors.toCollection(TreeSet::new))) {
                        text.append(ChatColor.DARK_PURPLE).append("  - ").append(name).append("\n");
                    }
                }
                return text.toString() + ChatColor.YELLOW + getQueryText(context);
            }
            final TextComponent component = new TextComponent(getTitle(context) + "\n");
            component.setColor(net.md_5.bungee.api.ChatColor.LIGHT_PURPLE);
            final TextComponent line = new TextComponent("");
            if (plugin.getCustomRequirements().isEmpty()) {
                final TextComponent link = new TextComponent("https://pikamug.gitbook.io/quests/casual/modules\n");
                link.setColor(net.md_5.bungee.api.ChatColor.DARK_AQUA);
                link.setUnderlined(true);
                line.addExtra(link);
                line.addExtra(ChatColor.RED + "(" + BukkitLang.get("stageEditorNoModules") + ")\n");
            } else {
                for (final String name : plugin.getCustomRequirements().stream().map(CustomRequirement::getModuleName)
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
                for (final CustomRequirement cr : plugin.getCustomRequirements()) {
                    if (cr.getModuleName().equalsIgnoreCase(input)) {
                        found = cr.getModuleName();
                        break;
                    }
                }
                if (found == null) {
                    // No? Check again, but with locale sensitivity
                    for (final CustomRequirement cr : plugin.getCustomRequirements()) {
                        if (cr.getModuleName().toLowerCase().contains(input.toLowerCase())) {
                            found = cr.getModuleName();
                            break;
                        }
                    }
                }
                if (found != null) {
                    return new QuestCustomRequirementsPrompt(found, context);
                }
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                return new QuestRequirementsPrompt(context);
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(Key.REQ_CUSTOM, null);
                context.setSessionData(Key.REQ_CUSTOM_DATA, null);
                context.setSessionData(Key.REQ_CUSTOM_DATA_TEMP, null);
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("reqCustomCleared"));
                return new QuestRequirementsPrompt(context);
            }
            context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("reqCustomNotFound"));
            return new QuestCustomRequirementModulePrompt(context);
        }
    }

    public class QuestCustomRequirementsPrompt extends QuestsEditorStringPrompt {

        private final String moduleName;

        public QuestCustomRequirementsPrompt(final String moduleName, final ConversationContext context) {
            super(context);
            this.moduleName = moduleName;
        }

        public String getModuleName() {
            return moduleName;
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("customRequirementsTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("reqCustomPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);

            if (!(context.getForWhom() instanceof Player) || !plugin.getConfigSettings().canClickablePrompts()) {
                final StringBuilder text = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle(context) + "\n");
                if (plugin.getCustomRequirements().isEmpty()) {
                    text.append(ChatColor.DARK_AQUA).append(ChatColor.UNDERLINE)
                            .append("https://pikamug.gitbook.io/quests/casual/modules\n");
                    text.append(ChatColor.RED).append("(").append(BukkitLang.get("stageEditorNoModules")).append(")\n");
                } else {
                    for (final CustomRequirement cr : plugin.getCustomRequirements()) {
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
            if (plugin.getCustomRequirements().isEmpty()) {
                final TextComponent link = new TextComponent("https://pikamug.gitbook.io/quests/casual/modules\n");
                link.setColor(net.md_5.bungee.api.ChatColor.DARK_AQUA);
                link.setUnderlined(true);
                line.addExtra(link);
                line.addExtra(ChatColor.RED + "(" + BukkitLang.get("stageEditorNoModules") + ")\n");
            } else {
                for (final CustomRequirement co : plugin.getCustomRequirements()) {
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
                    if (context.getSessionData(Key.REQ_CUSTOM) != null) {
                        // The custom requirement may already have been added, so let's check that
                        final LinkedList<String> list = (LinkedList<String>) context.getSessionData(Key.REQ_CUSTOM);
                        final LinkedList<Map<String, Object>> dataMapList
                                = (LinkedList<Map<String, Object>>) context.getSessionData(Key.REQ_CUSTOM_DATA);
                        if (dataMapList != null && list != null && !list.contains(found.getName())) {
                            // Hasn't been added yet, so let's do it
                            list.add(found.getName());
                            dataMapList.add(found.getData());
                            context.setSessionData(Key.REQ_CUSTOM, list);
                            context.setSessionData(Key.REQ_CUSTOM_DATA, dataMapList);
                        } else {
                            // Already added, so inform user
                            context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("reqCustomAlreadyAdded"));
                            return new QuestCustomRequirementsPrompt(moduleName, context);
                        }
                    } else {
                        // The custom requirement hasn't been added yet, so let's do it
                        final LinkedList<Map<String, Object>> dataMapList = new LinkedList<>();
                        dataMapList.add(found.getData());
                        final LinkedList<String> list = new LinkedList<>();
                        list.add(found.getName());
                        context.setSessionData(Key.REQ_CUSTOM, list);
                        context.setSessionData(Key.REQ_CUSTOM_DATA, dataMapList);
                    }
                    // Send user to the custom data prompt if there is any needed
                    if (!found.getData().isEmpty()) {
                        context.setSessionData(Key.REQ_CUSTOM_DATA_DESCRIPTIONS, found.getDescriptions());
                        return new QuestRequirementCustomDataListPrompt();
                    }
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("reqCustomNotFound"));
                    return new QuestCustomRequirementsPrompt(moduleName, context);
                }
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(Key.REQ_CUSTOM, null);
                context.setSessionData(Key.REQ_CUSTOM_DATA, null);
                context.setSessionData(Key.REQ_CUSTOM_DATA_TEMP, null);
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("reqCustomCleared"));
            }
            return new QuestRequirementsPrompt(context);
        }
    }

    private class QuestRequirementCustomDataListPrompt extends StringPrompt {

        @SuppressWarnings("unchecked")
        @Override
        public @NotNull String getPromptText(final ConversationContext context) {
            final StringBuilder text = new StringBuilder(ChatColor.GOLD + "- ");
            final LinkedList<String> list = (LinkedList<String>) context.getSessionData(Key.REQ_CUSTOM);
            final LinkedList<Map<String, Object>> dataMapList
                    = (LinkedList<Map<String, Object>>) context.getSessionData(Key.REQ_CUSTOM_DATA);
            if (dataMapList != null && list != null) {
                final String reqName = list.getLast();
                final Map<String, Object> dataMap = dataMapList.getLast();
                text.append(reqName).append(" -\n");
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
                    = (LinkedList<Map<String, Object>>) context.getSessionData(Key.REQ_CUSTOM_DATA);
            if (dataMapList != null) {
                final Map<String, Object> dataMap = dataMapList.getLast();
                final int numInput;
                try {
                    numInput = Integer.parseInt(input);
                } catch (final NumberFormatException nfe) {
                    return new QuestRequirementCustomDataListPrompt();
                }
                if (numInput < 1 || numInput > dataMap.size() + 1) {
                    return new QuestRequirementCustomDataListPrompt();
                }
                if (numInput < dataMap.size() + 1) {
                    final LinkedList<String> dataMapKeys = new LinkedList<>(dataMap.keySet());
                    Collections.sort(dataMapKeys);
                    final String selectedKey = dataMapKeys.get(numInput - 1);
                    context.setSessionData(Key.REQ_CUSTOM_DATA_TEMP, selectedKey);
                    return new QuestRequirementCustomDataPrompt();
                } else {
                    if (dataMap.containsValue(null)) {
                        return new QuestRequirementCustomDataListPrompt();
                    } else {
                        context.setSessionData(Key.REQ_CUSTOM_DATA_DESCRIPTIONS, null);
                    }
                }
            }
            return new QuestRequirementsPrompt(context);
        }
    }

    private class QuestRequirementCustomDataPrompt extends StringPrompt {

        @Override
        public @NotNull String getPromptText(final ConversationContext context) {
            String text = "";
            final String temp = (String) context.getSessionData(Key.REQ_CUSTOM_DATA_TEMP);
            @SuppressWarnings("unchecked")
            final Map<String, String> descriptions
                    = (Map<String, String>) context.getSessionData(Key.REQ_CUSTOM_DATA_DESCRIPTIONS);
            if (temp != null && descriptions != null) {
                if (descriptions.get(temp) != null) {
                    text += ChatColor.GOLD + descriptions.get(temp) + "\n";
                }
                String lang = BukkitLang.get("stageEditorCustomDataPrompt");
                lang = lang.replace("<data>", ChatColor.GOLD + temp + ChatColor.YELLOW);
                text += ChatColor.YELLOW + lang;
            }
            return text;
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            @SuppressWarnings("unchecked")
            final LinkedList<Map<String, Object>> dataMapList
                    = (LinkedList<Map<String, Object>>) context.getSessionData(Key.REQ_CUSTOM_DATA);
            if (dataMapList != null) {
                final Map<String, Object> dataMap = dataMapList.getLast();
                dataMap.put((String) context.getSessionData(Key.REQ_CUSTOM_DATA_TEMP), input);
                context.setSessionData(Key.REQ_CUSTOM_DATA_TEMP, null);
            }
            return new QuestRequirementCustomDataListPrompt();
        }
    }
}
