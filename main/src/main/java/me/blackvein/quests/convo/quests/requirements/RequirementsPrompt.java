/*
 * Copyright (c) 2014 PikaMug and contributors. All rights reserved.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package me.blackvein.quests.convo.quests.requirements;

import com.gmail.nossr50.datatypes.skills.SkillType;
import com.herocraftonline.heroes.characters.classes.HeroClass;
import me.blackvein.quests.CustomRequirement;
import me.blackvein.quests.CustomReward;
import me.blackvein.quests.Quest;
import me.blackvein.quests.Quests;
import me.blackvein.quests.convo.generic.ItemStackPrompt;
import me.blackvein.quests.convo.generic.OverridePrompt;
import me.blackvein.quests.convo.quests.QuestsEditorNumericPrompt;
import me.blackvein.quests.convo.quests.QuestsEditorStringPrompt;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenNumericPromptEvent;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenStringPromptEvent;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
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

public class RequirementsPrompt extends QuestsEditorNumericPrompt {

    private final Quests plugin;
    private final String classPrefix;
    private boolean hasRequirement = false;
    private final int size = 11;
    
    public RequirementsPrompt(final ConversationContext context) {
        super(context);
        this.plugin = (Quests)context.getPlugin();
        this.classPrefix = getClass().getSimpleName();
    }
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle(final ConversationContext context) {
        return Lang.get("requirementsTitle").replace("<quest>", (String) Objects
                .requireNonNull(context.getSessionData(CK.Q_NAME)));
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
        case 9:
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
        case 10:
            if (context.getSessionData(CK.REQ_FAIL_MESSAGE) == null) {
                if (!hasRequirement) {
                    return ChatColor.GRAY;
                } else {
                    return ChatColor.BLUE;
                }
            } else {
                return ChatColor.BLUE;
            }
        case 11:
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
                return ChatColor.YELLOW + Lang.get("reqSetMoney");
            } else {
                return ChatColor.GRAY + Lang.get("reqSetMoney");
            }
        case 2:
            return ChatColor.YELLOW + Lang.get("reqSetQuestPoints").replace("<points>", Lang.get("questPoints"));
        case 3:
            return ChatColor.YELLOW + Lang.get("reqSetItem");
        case 4:
            return ChatColor.YELLOW + Lang.get("reqSetPerms");
        case 5:
            return ChatColor.YELLOW + Lang.get("reqSetQuest");
        case 6:
            return ChatColor.YELLOW + Lang.get("reqSetQuestBlocks");
        case 7:
            if (plugin.getDependencies().getMcmmoClassic() != null) {
                return ChatColor.YELLOW + Lang.get("reqSetMcMMO");
            } else {
                return ChatColor.GRAY + Lang.get("reqSetMcMMO");
            }
        case 8:
            if (plugin.getDependencies().getHeroes() != null) {
                return ChatColor.YELLOW + Lang.get("reqSetHeroes");
            } else {
                return ChatColor.GRAY + Lang.get("reqSetHeroes");
            }
        case 9:
            return ChatColor.DARK_PURPLE + Lang.get("reqSetCustom");
        case 10:
            if (!hasRequirement) {
                return ChatColor.GRAY + Lang.get("overrideCreateSet");
            } else {
                return ChatColor.YELLOW + Lang.get("overrideCreateSet");
            }
        case 11:
            return ChatColor.YELLOW + Lang.get("done");
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
                final Integer moneyReq = (Integer) context.getSessionData(CK.REQ_MONEY);
                if (moneyReq == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + moneyReq + " " 
                            + (moneyReq > 1 ? plugin.getDependencies().getCurrency(true) 
                            : plugin.getDependencies().getCurrency(false)) + ChatColor.GRAY + ")";
                }
            } else {
                return ChatColor.GRAY + "(" + Lang.get("notInstalled") + ")";
            }
        case 2:
            if (context.getSessionData(CK.REQ_QUEST_POINTS) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + context.getSessionData(CK.REQ_QUEST_POINTS) + " " 
                        + Lang.get("questPoints") + ChatColor.GRAY + ")";
            }
        case 3:
            if (context.getSessionData(CK.REQ_ITEMS) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder("\n");
                final LinkedList<ItemStack> items = (LinkedList<ItemStack>) context.getSessionData(CK.REQ_ITEMS);
                if (items != null) {
                    for (final ItemStack item : items) {
                        text.append(ChatColor.GRAY).append("     - ").append(ChatColor.BLUE)
                                .append(ItemUtil.getName(item)).append(ChatColor.GRAY).append(" x ")
                                .append(ChatColor.AQUA).append(item.getAmount()).append("\n");
                    }
                }
                return text.toString();
            }
        case 4:
            if (context.getSessionData(CK.REQ_PERMISSION) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder("\n");
                final List<String> perms = (List<String>) context.getSessionData(CK.REQ_PERMISSION);
                if (perms != null) {
                    for (final String s : perms) {
                        text.append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s).append("\n");
                    }
                }
                return text.toString();
            }
        case 5:
            if (context.getSessionData(CK.REQ_QUEST) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder("\n");
                final List<String> questReq = (List<String>) context.getSessionData(CK.REQ_QUEST);
                if (questReq != null) {
                    for (String s : questReq) {
                        if (plugin.getQuestById(s) != null) {
                            s = plugin.getQuestById(s).getName();
                        }
                        text.append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s).append("\n");
                    }
                }
                return text.toString();
            }
        case 6:
            if (context.getSessionData(CK.REQ_QUEST_BLOCK) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder("\n");
                final List<String> questBlockReq = (List<String>) context.getSessionData(CK.REQ_QUEST_BLOCK);
                if (questBlockReq != null) {
                    for (String s : questBlockReq) {
                        if (plugin.getQuestById(s) != null) {
                            s = plugin.getQuestById(s).getName();
                        }
                        text.append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s).append("\n");
                    }
                }
                return text.toString();
            }
        case 7:
            if (plugin.getDependencies().getMcmmoClassic() != null) {
                if (context.getSessionData(CK.REQ_MCMMO_SKILLS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder("\n");
                    final List<String> skills = (List<String>) context.getSessionData(CK.REQ_MCMMO_SKILLS);
                    final List<Integer> amounts = (List<Integer>) context.getSessionData(CK.REQ_MCMMO_SKILL_AMOUNTS);
                    if (skills != null && amounts != null) {
                        for (final String s : skills) {
                            text.append(ChatColor.GRAY).append("     - ").append(ChatColor.DARK_GREEN).append(s)
                                    .append(ChatColor.RESET).append(ChatColor.YELLOW).append(" ")
                                    .append(Lang.get("mcMMOLevel")).append(" ").append(ChatColor.GREEN)
                                    .append(amounts.get(skills.indexOf(s))).append("\n");
                        }
                    }
                    return text.toString();
                }
            } else {
                return ChatColor.GRAY + "(" + Lang.get("notInstalled") + ")";
            }
        case 8:
            if (plugin.getDependencies().getHeroes() != null) {
                if (context.getSessionData(CK.REQ_HEROES_PRIMARY_CLASS) == null 
                        && context.getSessionData(CK.REQ_HEROES_SECONDARY_CLASS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")\n";
                } else {
                    String text = "\n";
                    if (context.getSessionData(CK.REQ_HEROES_PRIMARY_CLASS) != null) {
                        text += ChatColor.AQUA + "    " + Lang.get("reqHeroesPrimaryDisplay") + " " 
                                + ChatColor.BLUE + context.getSessionData(CK.REQ_HEROES_PRIMARY_CLASS);
                    }
                    if (context.getSessionData(CK.REQ_HEROES_SECONDARY_CLASS) != null) {
                        text += ChatColor.AQUA + "    " + Lang.get("reqHeroesSecondaryDisplay") + " " 
                                + ChatColor.BLUE + context.getSessionData(CK.REQ_HEROES_SECONDARY_CLASS);
                    }
                    return text;
                }
            } else {
                return ChatColor.GRAY + "(" + Lang.get("notInstalled") + ")";
            }
        case 9:
           if (context.getSessionData(CK.REQ_CUSTOM) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
           } else {
                final StringBuilder text = new StringBuilder("\n");
                final LinkedList<String> customReq = (LinkedList<String>) context.getSessionData(CK.REQ_CUSTOM);
                if (customReq != null) {
                    for (final String s : customReq) {
                        text.append(ChatColor.RESET).append(ChatColor.DARK_PURPLE).append("  - ")
                                .append(ChatColor.LIGHT_PURPLE).append(s).append("\n");
                    }
                }
                return text.toString();
           }
        case 10:
            if (context.getSessionData(CK.REQ_FAIL_MESSAGE) == null) {
                if (!hasRequirement) {
                    return ChatColor.GRAY + "(" + Lang.get("stageEditorOptional") + ")";
                } else {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                }
            } else {
                final StringBuilder text = new StringBuilder("\n");
                final List<String> overrides = (List<String>) context.getSessionData(CK.REQ_FAIL_MESSAGE);
                if (overrides != null) {
                    for (final String override : overrides) {
                        text.append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(override).append("\n");
                    }
                }
                return text.toString();
            }
        case 11:
            return "";
        default:
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public @NotNull String getBasicPromptText(final ConversationContext context) {
        final String input = (String) context.getSessionData(classPrefix + "-override");
        if (input != null && !input.equalsIgnoreCase(Lang.get("cancel"))) {
            if (input.equalsIgnoreCase(Lang.get("clear"))) {
                context.setSessionData(CK.REQ_FAIL_MESSAGE, null);
            } else {
                final LinkedList<String> overrides = new LinkedList<>();
                if (context.getSessionData(CK.REQ_FAIL_MESSAGE) != null) {
                    overrides.addAll((List<String>) context.getSessionData(CK.REQ_FAIL_MESSAGE));
                }
                overrides.add(input);
                context.setSessionData(CK.REQ_FAIL_MESSAGE, overrides);
                context.setSessionData(classPrefix + "-override", null);
            }
        }
        checkRequirement(context);

        if (context.getPlugin() != null) {
            final QuestsEditorPostOpenNumericPromptEvent event
                    = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
        }
        
        final StringBuilder text = new StringBuilder(ChatColor.DARK_AQUA + getTitle(context).replace((String) Objects
                .requireNonNull(context.getSessionData(CK.Q_NAME)), ChatColor.AQUA
                + (String) context.getSessionData(CK.Q_NAME) + ChatColor.DARK_AQUA));
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
                return new RequirementsMoneyPrompt(context);
            } else {
                return new RequirementsPrompt(context);
            }
        case 2:
            return new RequirementsQuestPointsPrompt(context);
        case 3:
            return new RequirementsItemListPrompt(context);
        case 4:
            return new RequirementsPermissionsPrompt(context);
        case 5:
            return new RequirementsQuestListPrompt(context, true);
        case 6:
            return new RequirementsQuestListPrompt(context, false);
        case 7:
            if (plugin.getDependencies().getMcmmoClassic() != null) {
                return new RequirementsMcMMOListPrompt(context);
            } else {
                return new RequirementsPrompt(context);
            }
        case 8:
            if (plugin.getDependencies().getHeroes() != null) {
                return new RequirementsHeroesListPrompt(context);
            } else {
                return new RequirementsPrompt(context);
            }
        case 9:
            return new CustomRequirementModulePrompt(context);
        case 10:
            if (hasRequirement) {
                return new OverridePrompt.Builder()
                        .context(context)
                        .source(this)
                        .promptText(Lang.get("overrideCreateEnter"))
                        .build();
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidOption"));
                return new RequirementsPrompt(context);
            }
        case 11:
            return plugin.getQuestFactory().returnToMenu(context);
        default:
            return new RequirementsPrompt(context);
        }
    }
    
    public boolean checkRequirement(final ConversationContext context) {
        if (context.getSessionData(CK.REQ_MONEY) != null 
                || context.getSessionData(CK.REQ_QUEST_POINTS) != null
                || context.getSessionData(CK.REQ_ITEMS) != null
                || context.getSessionData(CK.REQ_PERMISSION) != null
                || context.getSessionData(CK.REQ_QUEST) != null
                || context.getSessionData(CK.REQ_QUEST_BLOCK) != null
                || context.getSessionData(CK.REQ_MCMMO_SKILLS) != null 
                || context.getSessionData(CK.REQ_HEROES_PRIMARY_CLASS) != null
                || context.getSessionData(CK.REQ_HEROES_SECONDARY_CLASS) != null
                || context.getSessionData(CK.REQ_CUSTOM) != null) {
            hasRequirement = true;
            return true;
        }
        return false;
    }

    public class RequirementsMoneyPrompt extends QuestsEditorStringPrompt {
        
        public RequirementsMoneyPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("rewMoneyPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            String text = getQueryText(context);
            if (plugin.getDependencies().getVaultEconomy() != null) {
                text = text.replace("<money>", ChatColor.DARK_PURPLE+ ((plugin.getDependencies().getVaultEconomy()
                        .currencyNamePlural().isEmpty() ? Lang.get("money") : plugin.getDependencies().getVaultEconomy()
                        .currencyNamePlural())) + ChatColor.YELLOW);
            } else {
                text = text.replace("<money>", ChatColor.DARK_PURPLE + Lang.get("money") + ChatColor.YELLOW);
            }
            return ChatColor.YELLOW + text;
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel")) && !input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i > 0) {
                        context.setSessionData(CK.REQ_MONEY, i);
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("inputPosNum"));
                        return new RequirementsMoneyPrompt(context);
                    }
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqNotANumber")
                            .replace("<input>", input));
                    return new RequirementsMoneyPrompt(context);
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.REQ_MONEY, null);
                return new RequirementsPrompt(context);
            }
            return new RequirementsPrompt(context);
        }
    }

    public class RequirementsQuestPointsPrompt extends QuestsEditorStringPrompt {
        
        public RequirementsQuestPointsPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("rewQuestPointsPrompt").replace("<points>", Lang.get("questPoints"));
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            return ChatColor.YELLOW + getQueryText(context);
        }
        
        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel")) && !input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i > 0) {
                        context.setSessionData(CK.REQ_QUEST_POINTS, i);
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("inputPosNum"));
                        return new RequirementsQuestPointsPrompt(context);
                    }
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqNotANumber")
                            .replace("<input>", input));
                    return new RequirementsQuestPointsPrompt(context);
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.REQ_QUEST_POINTS, null);
                return new RequirementsPrompt(context);
            }
            return new RequirementsPrompt(context);
        }
    }

    public class RequirementsItemListPrompt extends QuestsEditorNumericPrompt {
        
        public RequirementsItemListPrompt(final ConversationContext context) {
            super(context);
        }

        private final int size = 4;

        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("itemRequirementsTitle");
        }
        
        @Override
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                return ChatColor.BLUE;
            case 2:
                if (context.getSessionData(CK.REQ_ITEMS) == null) {
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
                return ChatColor.YELLOW + Lang.get("stageEditorDeliveryAddItem");
            case 2:
                if (context.getSessionData(CK.REQ_ITEMS) == null) {
                    return ChatColor.GRAY + Lang.get("reqSetRemoveItems");
                } else {
                    return ChatColor.YELLOW + Lang.get("reqSetRemoveItems");
                }
            case 3:
                return ChatColor.RED + Lang.get("clear");
            case 4:
                return ChatColor.GREEN + Lang.get("done");
            default:
                return null;
            }
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                if (context.getSessionData(CK.REQ_ITEMS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder("\n");
                    final List<ItemStack> reqItems = (List<ItemStack>) context.getSessionData(CK.REQ_ITEMS);
                    if (reqItems != null) {
                        for (final ItemStack is : reqItems) {
                            text.append(ChatColor.GRAY).append("     - ").append(ItemUtil.getDisplayString(is))
                                    .append("\n");
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (context.getSessionData(CK.REQ_ITEMS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    if (context.getSessionData(CK.REQ_ITEMS_REMOVE) == null) {
                        return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                    } else {
                        final StringBuilder text = new StringBuilder("\n");
                        final List<Boolean> reqItemsRemove = (List<Boolean>) context.getSessionData(CK.REQ_ITEMS_REMOVE);
                        if (reqItemsRemove != null) {
                            for (final Boolean b : reqItemsRemove) {
                                text.append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA)
                                        .append(b.equals(Boolean.TRUE) ? Lang.get("yesWord") : Lang.get("noWord"))
                                        .append("\n");
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
                if (context.getSessionData(CK.REQ_ITEMS) != null) {
                    final List<ItemStack> itemReq = (List<ItemStack>) context.getSessionData(CK.REQ_ITEMS);
                    final ItemStack i = (ItemStack) context.getSessionData("tempStack");
                    if (itemReq != null && i != null) {
                        itemReq.add(i);
                    }
                    context.setSessionData(CK.REQ_ITEMS, itemReq);
                } else {
                    final LinkedList<ItemStack> itemReq = new LinkedList<>();
                    final ItemStack i = (ItemStack) context.getSessionData("tempStack");
                    if (i != null) {
                        itemReq.add(i);
                    }
                    context.setSessionData(CK.REQ_ITEMS, itemReq);
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
                return new ItemStackPrompt(context, RequirementsItemListPrompt.this);
            case 2:
                if (context.getSessionData(CK.REQ_ITEMS) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqMustAddItem"));
                    return new RequirementsItemListPrompt(context);
                } else {
                    return new RemoveItemsPrompt(context);
                }
            case 3:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("reqItemCleared"));
                context.setSessionData(CK.REQ_ITEMS, null);
                context.setSessionData(CK.REQ_ITEMS_REMOVE, null);
                return new RequirementsItemListPrompt(context);
            case 4:
                final int one;
                final int two;
                final List<ItemStack> items = (List<ItemStack>) context.getSessionData(CK.REQ_ITEMS);
                final List<Boolean> remove = (List<Boolean>) context.getSessionData(CK.REQ_ITEMS_REMOVE);
                if (items != null) {
                    one = items.size();
                } else {
                    one = 0;
                }
                if (remove != null) {
                    two = remove.size();
                } else {
                    two = 0;
                }
                if (one == two) {
                    return new RequirementsPrompt(context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                    return new RequirementsItemListPrompt(context);
                }
            default:
                return null;
            }
        }
    }

    public class RemoveItemsPrompt extends QuestsEditorStringPrompt {
        
        public RemoveItemsPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("reqRemoveItemsPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                final String[] args = input.split(" ");
                final LinkedList<Boolean> booleans = new LinkedList<>();
                for (final String s : args) {
                    if (input.startsWith("t") || s.equalsIgnoreCase(Lang.get("true")) 
                            || s.equalsIgnoreCase(Lang.get("yesWord"))) {
                        booleans.add(true);
                    } else if (input.startsWith("f") || s.equalsIgnoreCase(Lang.get("false")) 
                            || s.equalsIgnoreCase(Lang.get("noWord"))) {
                        booleans.add(false);
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
                        return new RemoveItemsPrompt(context);
                    }
                }
                context.setSessionData(CK.REQ_ITEMS_REMOVE, booleans);
            }
            return new RequirementsItemListPrompt(context);
        }
    }

    public class RequirementsPermissionsPrompt extends QuestsEditorStringPrompt {
        
        public RequirementsPermissionsPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("reqPermissionsPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel")) && !input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                final String[] args = input.split(" ");
                final LinkedList<String> permissions = new LinkedList<>(Arrays.asList(args));
                context.setSessionData(CK.REQ_PERMISSION, permissions);
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.REQ_PERMISSION, null);
            }
            return new RequirementsPrompt(context);
        }
    }

    public class RequirementsQuestListPrompt extends QuestsEditorStringPrompt {

        private final boolean isRequiredQuest;

        public RequirementsQuestListPrompt(final ConversationContext context, final boolean isRequired) {
            super(context);
            this.isRequiredQuest = isRequired;
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("reqQuestListTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("reqQuestPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }

            StringBuilder text = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle(context) + "\n"
                    + ChatColor.DARK_PURPLE);
            boolean none = true;
            for (final Quest q : plugin.getLoadedQuests()) {
                text.append(q.getName()).append(", ");
                none = false;
            }
            if (none) {
                text.append("(").append(Lang.get("none")).append(")\n");
            } else {
                text = new StringBuilder(text.substring(0, (text.length() - 2)));
                text.append("\n");
            }
            text.append(ChatColor.YELLOW).append(getQueryText(context));
            return text.toString();
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel")) && !input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                final String[] args = input.split(Lang.get("charSemi"));
                final LinkedList<String> questIds = new LinkedList<>();
                for (final String s : args) {
                    if (plugin.getQuest(s) == null) {
                        String text = Lang.get("reqNotAQuestName");
                        text = text.replace("<quest>", ChatColor.LIGHT_PURPLE + s + ChatColor.RED);
                        context.getForWhom().sendRawMessage(text);
                        return new RequirementsQuestListPrompt(context, isRequiredQuest);
                    }
                    if (questIds.contains(plugin.getQuest(s).getId())) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listDuplicate"));
                        return new RequirementsQuestListPrompt(context, isRequiredQuest);
                    }
                    questIds.add(plugin.getQuest(s).getId());
                }
                if (isRequiredQuest) {
                    context.setSessionData(CK.REQ_QUEST, questIds);
                } else {
                    context.setSessionData(CK.REQ_QUEST_BLOCK, questIds);
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                if (isRequiredQuest) {
                    context.setSessionData(CK.REQ_QUEST, null);
                } else {
                    context.setSessionData(CK.REQ_QUEST_BLOCK, null);
                }
            }
            return new RequirementsPrompt(context);
        }
    }

    public class RequirementsMcMMOListPrompt extends QuestsEditorNumericPrompt {

        public RequirementsMcMMOListPrompt(final ConversationContext context) {
            super(context);
        }
        
        private final int size = 3;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("mcMMORequirementsTitle");
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
                return ChatColor.YELLOW + Lang.get("reqSetSkills");
            case 2:
                return ChatColor.YELLOW + Lang.get("reqSetSkillAmounts");
            case 3:
                return ChatColor.GREEN + Lang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                if (context.getSessionData(CK.REQ_MCMMO_SKILLS) == null) {
                    return ChatColor.GRAY + " (" + Lang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder("\n");
                    final LinkedList<String> skills = (LinkedList<String>) context.getSessionData(CK.REQ_MCMMO_SKILLS);
                    if (skills != null) {
                        for (final String skill : skills) {
                            text.append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(skill)
                                    .append("\n");
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (context.getSessionData(CK.REQ_MCMMO_SKILL_AMOUNTS) == null) {
                    return ChatColor.GRAY + " (" + Lang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder("\n");
                    final LinkedList<Integer> skillAmounts
                            = (LinkedList<Integer>) context.getSessionData(CK.REQ_MCMMO_SKILL_AMOUNTS);
                    if (skillAmounts != null) {
                        for (final int i : skillAmounts) {
                            text.append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(i).append("\n");
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
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenNumericPromptEvent event
                        = new QuestsEditorPostOpenNumericPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }

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
                return new McMMOSkillsPrompt(context);
            case 2:
                return new McMMOAmountsPrompt(context);
            case 3:
                return new RequirementsPrompt(context);
            default:
                return new RequirementsMcMMOListPrompt(context);
            }
        }
    }

    public class McMMOSkillsPrompt extends QuestsEditorStringPrompt {
        
        public McMMOSkillsPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("skillListTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("rewMcMMOPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
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
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel")) && !input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                final LinkedList<String> skills = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    final String formatted = MiscUtil.getCapitalized(s);
                    if (Quests.getMcMMOSkill(formatted) != null) {
                        skills.add(formatted);
                    } else if (skills.contains(formatted)) {
                        context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("listDuplicate"));
                        return new McMMOSkillsPrompt(context);
                    } else {
                        String text = Lang.get("reqMcMMOError");
                        text = text.replace("<input>", ChatColor.RED + s + ChatColor.YELLOW);
                        context.getForWhom().sendRawMessage(ChatColor.YELLOW + text);
                        return new McMMOSkillsPrompt(context);
                    }
                }
                context.setSessionData(CK.REQ_MCMMO_SKILLS, skills);
                return new RequirementsMcMMOListPrompt(context);
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("reqMcMMOCleared"));
                context.setSessionData(CK.REQ_MCMMO_SKILLS, null);
                return new RequirementsMcMMOListPrompt(context);
            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                return new RequirementsMcMMOListPrompt(context);
            }
            return new McMMOSkillsPrompt(context);
        }
    }

    public class McMMOAmountsPrompt extends QuestsEditorStringPrompt {
        
        public McMMOAmountsPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("reqMcMMOAmountsPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel")) && !input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                final LinkedList<Integer> amounts = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    try {
                        final int i = Integer.parseInt(s);
                        amounts.add(i);
                    } catch (final NumberFormatException nfe) {
                        String text = Lang.get("reqNotANumber");
                        text = text.replace("<input>", ChatColor.RED + s + ChatColor.YELLOW);
                        context.getForWhom().sendRawMessage(ChatColor.YELLOW + text);
                        return new McMMOAmountsPrompt(context);
                    }
                }
                context.setSessionData(CK.REQ_MCMMO_SKILL_AMOUNTS, amounts);
                return new RequirementsMcMMOListPrompt(context);
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("reqMcMMOAmountsCleared"));
                context.setSessionData(CK.REQ_MCMMO_SKILL_AMOUNTS, null);
                return new RequirementsMcMMOListPrompt(context);
            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                return new RequirementsMcMMOListPrompt(context);
            }
            return new McMMOAmountsPrompt(context);
        }
    }

    public class RequirementsHeroesListPrompt extends QuestsEditorNumericPrompt {

        public RequirementsHeroesListPrompt(final ConversationContext context) {
            super(context);
        }
        
        private final int size = 3;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("heroesRequirementsTitle");
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
                return ChatColor.YELLOW + Lang.get("reqHeroesSetPrimary");
            case 2:
                return ChatColor.YELLOW + Lang.get("reqHeroesSetSecondary");
            case 3:
                return ChatColor.GREEN + Lang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                if (context.getSessionData(CK.REQ_HEROES_PRIMARY_CLASS) == null) {
                    return ChatColor.GRAY + " (" + Lang.get("noneSet") + ")";
                } else {
                    return "(" + ChatColor.AQUA + context.getSessionData(CK.REQ_HEROES_PRIMARY_CLASS) + ChatColor.GREEN
                            + ")\n";
                }
            case 2:
                if (context.getSessionData(CK.REQ_HEROES_SECONDARY_CLASS) == null) {
                    return ChatColor.GRAY + " (" + Lang.get("noneSet") + ")";
                } else {
                    return "(" + ChatColor.AQUA + context.getSessionData(CK.REQ_HEROES_SECONDARY_CLASS)
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
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }

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
                return new HeroesPrimaryPrompt(context);
            case 2:
                return new HeroesSecondaryPrompt(context);
            case 3:
                return new RequirementsPrompt(context);
            default:
                return new RequirementsHeroesListPrompt(context);
            }
        }
    }

    public class HeroesPrimaryPrompt extends QuestsEditorStringPrompt {
        
        public HeroesPrimaryPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("heroesPrimaryTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("reqHeroesPrimaryPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            final StringBuilder text = new StringBuilder(ChatColor.DARK_PURPLE + getTitle(context) + "\n");
            final LinkedList<String> list = new LinkedList<>();
            for (final HeroClass hc : plugin.getDependencies().getHeroes().getClassManager().getClasses()) {
                if (hc.isPrimary()) {
                    list.add(hc.getName());
                }
            }
            if (list.isEmpty()) {
                text.append(ChatColor.GRAY).append("(").append(Lang.get("none")).append(")\n");
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
            if (!input.equalsIgnoreCase(Lang.get("cmdClear")) && !input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                final HeroClass hc = plugin.getDependencies().getHeroes().getClassManager().getClass(input);
                if (hc != null) {
                    if (hc.isPrimary()) {
                        context.setSessionData(CK.REQ_HEROES_PRIMARY_CLASS, hc.getName());
                        return new RequirementsHeroesListPrompt(context);
                    } else {
                        String text = Lang.get("reqHeroesNotPrimary");
                        text = text.replace("<class>", ChatColor.LIGHT_PURPLE + hc.getName() + ChatColor.RED);
                        context.getForWhom().sendRawMessage(ChatColor.RED + text);
                        return new HeroesPrimaryPrompt(context);
                    }
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqHeroesClassNotFound"));
                    return new HeroesPrimaryPrompt(context);
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.REQ_HEROES_PRIMARY_CLASS, null);
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("reqHeroesPrimaryCleared"));
                return new RequirementsHeroesListPrompt(context);
            } else {
                return new RequirementsHeroesListPrompt(context);
            }
        }
    }

    public class HeroesSecondaryPrompt extends QuestsEditorStringPrompt {
        
        public HeroesSecondaryPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("heroesSecondaryTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("reqHeroesSecondaryPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            final StringBuilder text = new StringBuilder(ChatColor.DARK_PURPLE + getTitle(context) + "\n");
            final LinkedList<String> list = new LinkedList<>();
            for (final HeroClass hc : plugin.getDependencies().getHeroes().getClassManager().getClasses()) {
                if (hc.isSecondary()) {
                    list.add(hc.getName());
                }
            }
            if (list.isEmpty()) {
                text.append(ChatColor.GRAY).append("(").append(Lang.get("none")).append(")\n");
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
            if (!input.equalsIgnoreCase(Lang.get("cmdClear")) && !input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                final HeroClass hc = plugin.getDependencies().getHeroes().getClassManager().getClass(input);
                if (hc != null) {
                    if (hc.isSecondary()) {
                        context.setSessionData(CK.REQ_HEROES_SECONDARY_CLASS, hc.getName());
                        return new RequirementsHeroesListPrompt(context);
                    } else {
                        String text = Lang.get("reqHeroesNotSecondary");
                        text = text.replace("<class>", ChatColor.LIGHT_PURPLE + hc.getName() + ChatColor.RED);
                        context.getForWhom().sendRawMessage(ChatColor.RED + text);
                        return new HeroesSecondaryPrompt(context);
                    }
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqHeroesClassNotFound"));
                    return new HeroesSecondaryPrompt(context);
                }
            } else if (input.equalsIgnoreCase(Lang.get("clear"))) {
                context.setSessionData(CK.REQ_HEROES_SECONDARY_CLASS, null);
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("reqHeroesSecondaryCleared"));
                return new RequirementsHeroesListPrompt(context);
            } else {
                return new RequirementsHeroesListPrompt(context);
            }
        }
    }

    public class CustomRequirementModulePrompt extends QuestsEditorStringPrompt {

        public CustomRequirementModulePrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("stageEditorModules");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorModulePrompt");
        }

        @Override
        public @NotNull String getPromptText(@NotNull final ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }

            final StringBuilder text = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle(context) + "\n");
            if (plugin.getCustomRequirements().isEmpty()) {
                text.append(ChatColor.DARK_AQUA).append(ChatColor.UNDERLINE)
                        .append("https://pikamug.gitbook.io/quests/casual/modules").append(ChatColor.RESET)
                        .append("\n");
                text.append(ChatColor.DARK_PURPLE).append("(").append(Lang.get("stageEditorNoModules")).append(") ");
            } else {
                for (final String name : plugin.getCustomRequirements().stream().map(CustomRequirement::getModuleName)
                        .collect(Collectors.toCollection(TreeSet::new))) {
                    text.append(ChatColor.DARK_PURPLE).append("  - ").append(name).append("\n");
                }
            }
            return text.toString() + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(@NotNull final ConversationContext context, @Nullable final String input) {
            if (input != null && !input.equalsIgnoreCase(Lang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(Lang.get("cmdClear"))) {
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
                    return new CustomRequirementsPrompt(found, context);
                }
            } else if (input != null && input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                return new RequirementsPrompt(context);
            } else if (input != null && input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.REQ_CUSTOM, null);
                context.setSessionData(CK.REQ_CUSTOM_DATA, null);
                context.setSessionData(CK.REQ_CUSTOM_DATA_TEMP, null);
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("reqCustomCleared"));
            }
            context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorModuleNotFound"));
            return new CustomRequirementModulePrompt(context);
        }
    }

    public class CustomRequirementsPrompt extends QuestsEditorStringPrompt {

        private final String moduleName;

        public CustomRequirementsPrompt(final String moduleName, final ConversationContext context) {
            super(context);
            this.moduleName = moduleName;
        }

        public String getModuleName() {
            return moduleName;
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("customRequirementsTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("reqCustomPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }

            final StringBuilder text = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle(context) + "\n");
            if (plugin.getCustomRequirements().isEmpty()) {
                text.append(ChatColor.DARK_AQUA).append(ChatColor.UNDERLINE)
                        .append("https://pikamug.gitbook.io/quests/casual/modules\n").append(ChatColor.DARK_PURPLE)
                        .append("(").append(Lang.get("stageEditorNoModules")).append(") ");
            } else {
                for (final CustomRequirement cr : plugin.getCustomRequirements()) {
                    text.append(ChatColor.DARK_PURPLE).append("  - ").append(cr.getName()).append("\n");
                }
            }
            return text.toString() + ChatColor.YELLOW + getQueryText(context);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel")) && !input.equalsIgnoreCase(Lang.get("cmdClear"))) {
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
                    if (context.getSessionData(CK.REQ_CUSTOM) != null) {
                        // The custom requirement may already have been added, so let's check that
                        final LinkedList<String> list = (LinkedList<String>) context.getSessionData(CK.REQ_CUSTOM);
                        final LinkedList<Map<String, Object>> dataMapList
                                = (LinkedList<Map<String, Object>>) context.getSessionData(CK.REQ_CUSTOM_DATA);
                        if (dataMapList != null && list != null && !list.contains(found.getName())) {
                            // Hasn't been added yet, so let's do it
                            list.add(found.getName());
                            dataMapList.add(found.getData());
                            context.setSessionData(CK.REQ_CUSTOM, list);
                            context.setSessionData(CK.REQ_CUSTOM_DATA, dataMapList);
                        } else {
                            // Already added, so inform user
                            context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("reqCustomAlreadyAdded"));
                            return new CustomRequirementsPrompt(moduleName, context);
                        }
                    } else {
                        // The custom requirement hasn't been added yet, so let's do it
                        final LinkedList<Map<String, Object>> dataMapList = new LinkedList<>();
                        dataMapList.add(found.getData());
                        final LinkedList<String> list = new LinkedList<>();
                        list.add(found.getName());
                        context.setSessionData(CK.REQ_CUSTOM, list);
                        context.setSessionData(CK.REQ_CUSTOM_DATA, dataMapList);
                    }
                    // Send user to the custom data prompt if there is any needed
                    if (!found.getData().isEmpty()) {
                        context.setSessionData(CK.REQ_CUSTOM_DATA_DESCRIPTIONS, found.getDescriptions());
                        return new RequirementCustomDataListPrompt();
                    }
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("reqCustomNotFound"));
                    return new CustomRequirementsPrompt(moduleName, context);
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.REQ_CUSTOM, null);
                context.setSessionData(CK.REQ_CUSTOM_DATA, null);
                context.setSessionData(CK.REQ_CUSTOM_DATA_TEMP, null);
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("reqCustomCleared"));
            }
            return new RequirementsPrompt(context);
        }
    }

    private class RequirementCustomDataListPrompt extends StringPrompt {

        @SuppressWarnings("unchecked")
        @Override
        public @NotNull String getPromptText(final ConversationContext context) {
            final StringBuilder text = new StringBuilder(ChatColor.GOLD + "- ");
            final LinkedList<String> list = (LinkedList<String>) context.getSessionData(CK.REQ_CUSTOM);
            final LinkedList<Map<String, Object>> dataMapList
                    = (LinkedList<Map<String, Object>>) context.getSessionData(CK.REQ_CUSTOM_DATA);
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
                        text.append(ChatColor.GRAY).append(" (").append(Lang.get("noneSet")).append(ChatColor.GRAY)
                                .append(")\n");
                    }
                    index++;
                }
                text.append(ChatColor.GREEN).append(ChatColor.BOLD).append(index).append(ChatColor.YELLOW).append(" - ")
                        .append(Lang.get("done"));
            }
            return text.toString();
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            @SuppressWarnings("unchecked")
            final LinkedList<Map<String, Object>> dataMapList
                    = (LinkedList<Map<String, Object>>) context.getSessionData(CK.REQ_CUSTOM_DATA);
            if (dataMapList != null) {
                final Map<String, Object> dataMap = dataMapList.getLast();
                final int numInput;
                try {
                    numInput = Integer.parseInt(input);
                } catch (final NumberFormatException nfe) {
                    return new RequirementCustomDataListPrompt();
                }
                if (numInput < 1 || numInput > dataMap.size() + 1) {
                    return new RequirementCustomDataListPrompt();
                }
                if (numInput < dataMap.size() + 1) {
                    final LinkedList<String> dataMapKeys = new LinkedList<>(dataMap.keySet());
                    Collections.sort(dataMapKeys);
                    final String selectedKey = dataMapKeys.get(numInput - 1);
                    context.setSessionData(CK.REQ_CUSTOM_DATA_TEMP, selectedKey);
                    return new RequirementCustomDataPrompt();
                } else {
                    if (dataMap.containsValue(null)) {
                        return new RequirementCustomDataListPrompt();
                    } else {
                        context.setSessionData(CK.REQ_CUSTOM_DATA_DESCRIPTIONS, null);
                    }
                }
            }
            return new RequirementsPrompt(context);
        }
    }

    private class RequirementCustomDataPrompt extends StringPrompt {

        @Override
        public @NotNull String getPromptText(final ConversationContext context) {
            String text = "";
            final String temp = (String) context.getSessionData(CK.REQ_CUSTOM_DATA_TEMP);
            @SuppressWarnings("unchecked")
            final
            Map<String, String> descriptions
                    = (Map<String, String>) context.getSessionData(CK.REQ_CUSTOM_DATA_DESCRIPTIONS);
            if (temp != null && descriptions != null) {
                if (descriptions.get(temp) != null) {
                    text += ChatColor.GOLD + descriptions.get(temp) + "\n";
                }
                String lang = Lang.get("stageEditorCustomDataPrompt");
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
                    = (LinkedList<Map<String, Object>>) context.getSessionData(CK.REQ_CUSTOM_DATA);
            if (dataMapList != null) {
                final Map<String, Object> dataMap = dataMapList.getLast();
                dataMap.put((String) context.getSessionData(CK.REQ_CUSTOM_DATA_TEMP), input);
                context.setSessionData(CK.REQ_CUSTOM_DATA_TEMP, null);
            }
            return new RequirementCustomDataListPrompt();
        }
    }
}
