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

package me.blackvein.quests.convo.quests.rewards;

import com.codisimus.plugins.phatloots.PhatLoot;
import com.codisimus.plugins.phatloots.PhatLootsAPI;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.herocraftonline.heroes.characters.classes.HeroClass;
import me.blackvein.quests.CustomObjective;
import me.blackvein.quests.CustomRequirement;
import me.blackvein.quests.CustomReward;
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

public class RewardsPrompt extends QuestsEditorNumericPrompt {

    private final Quests plugin;
    private final String classPrefix;
    private boolean hasReward = false;
    private final int size = 13;

    public RewardsPrompt(final ConversationContext context) {
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
        return Lang.get("rewardsTitle").replace("<quest>", (String) Objects
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
        case 11:
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
        case 10:
            if (plugin.getDependencies().getPhatLoots() != null) {
                return ChatColor.BLUE;
            } else {
                return ChatColor.GRAY;
            }
        case 12:
            if (context.getSessionData(CK.REW_DETAILS_OVERRIDE) == null) {
                if (!hasReward) {
                    return ChatColor.GRAY;
                } else {
                    return ChatColor.BLUE;
                }
            } else {
                return ChatColor.BLUE;
            }
        case 13:
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
                return ChatColor.YELLOW + Lang.get("rewSetMoney");
            } else {
                return ChatColor.GRAY + Lang.get("rewSetMoney");
            }
        case 2:
            return ChatColor.YELLOW + Lang.get("rewSetQuestPoints").replace("<points>", Lang.get("questPoints"));
        case 3:
            return ChatColor.YELLOW + Lang.get("rewSetItems");
        case 4:
            return ChatColor.YELLOW + Lang.get("rewSetExperience");
        case 5:
            return ChatColor.YELLOW + Lang.get("rewSetCommands");
        case 6:
            return ChatColor.YELLOW + Lang.get("rewSetPermission");
        case 7:
            if (plugin.getDependencies().getMcmmoClassic() != null) {
                return ChatColor.YELLOW + Lang.get("rewSetMcMMO");
            } else {
                return ChatColor.GRAY + Lang.get("rewSetMcMMO");
            }
        case 8:
            if (plugin.getDependencies().getHeroes() != null) {
                return ChatColor.YELLOW + Lang.get("rewSetHeroes");
            } else {
                return ChatColor.GRAY + Lang.get("rewSetHeroes");
            }
        case 9:
            if (plugin.getDependencies().getPartiesApi() != null) {
                return ChatColor.YELLOW + Lang.get("rewSetPartiesExperience");
            } else {
                return ChatColor.GRAY + Lang.get("rewSetPartiesExperience");
            }
        case 10:
            if (plugin.getDependencies().getPhatLoots() != null) {
                return ChatColor.YELLOW + Lang.get("rewSetPhat");
            } else {
                return ChatColor.GRAY + Lang.get("rewSetPhat");
            }
        case 11:
            return ChatColor.DARK_PURPLE + Lang.get("rewSetCustom");
        case 12:
            if (!hasReward) {
                return ChatColor.GRAY + Lang.get("overrideCreateSet");
            } else {
                return ChatColor.YELLOW + Lang.get("overrideCreateSet");
            }
        case 13:
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
                final Integer moneyRew = (Integer) context.getSessionData(CK.REW_MONEY);
                if (moneyRew == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    return ChatColor.GRAY + "(" + ChatColor.AQUA
                            + plugin.getDependencies().getVaultEconomy().format(moneyRew) + ChatColor.GRAY + ")";
                }
            } else {
                return ChatColor.GRAY + "(" + Lang.get("notInstalled") + ")";
            }
        case 2:
            if (context.getSessionData(CK.REW_QUEST_POINTS) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + context.getSessionData(CK.REW_QUEST_POINTS) + " " 
                        + Lang.get("questPoints") + ChatColor.GRAY + ")";
            }
        case 3:
            if (context.getSessionData(CK.REW_ITEMS) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<ItemStack> items = (LinkedList<ItemStack>) context.getSessionData(CK.REW_ITEMS);
                if (items != null) {
                    for (final ItemStack item : items) {
                        if (item == null) {
                            text.append(ChatColor.RED).append("     - null\n");
                            plugin.getLogger().severe(ChatColor.RED + "Item reward was null while editing quest ID "
                                    + context.getSessionData(CK.Q_ID));
                        } else {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.BLUE)
                                    .append(ItemUtil.getName(item)).append(ChatColor.GRAY).append(" x ")
                                    .append(ChatColor.AQUA).append(item.getAmount());
                        }
                    }
                }
                return text.toString();
            }
        case 4:
            if (context.getSessionData(CK.REW_EXP) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + context.getSessionData(CK.REW_EXP) + " " 
                        + Lang.get("points") + ChatColor.GRAY + ")";
            }
        case 5:
            if (context.getSessionData(CK.REW_COMMAND) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final List<String> commands = (List<String>) context.getSessionData(CK.REW_COMMAND);
                final List<String> overrides = (List<String>) context.getSessionData(CK.REW_COMMAND_OVERRIDE_DISPLAY);
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
            if (context.getSessionData(CK.REW_PERMISSION) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final List<String> permissions = (List<String>) context.getSessionData(CK.REW_PERMISSION);
                final List<String> worlds = (List<String>) context.getSessionData(CK.REW_PERMISSION_WORLDS);
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
                if (context.getSessionData(CK.REW_MCMMO_SKILLS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> skills = (List<String>) context.getSessionData(CK.REW_MCMMO_SKILLS);
                    final List<Integer> amounts = (List<Integer>) context.getSessionData(CK.REW_MCMMO_AMOUNTS);
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
                return ChatColor.GRAY + "(" + Lang.get("notInstalled") + ")";
            }
        case 8:
            if (plugin.getDependencies().getHeroes() != null) {
                if (context.getSessionData(CK.REW_HEROES_CLASSES) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> heroClasses = (List<String>) context.getSessionData(CK.REW_HEROES_CLASSES);
                    final List<Double> amounts = (List<Double>) context.getSessionData(CK.REW_HEROES_AMOUNTS);
                    if (heroClasses != null && amounts != null) {
                        for (final String heroClass : heroClasses) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA)
                                    .append(amounts.get(heroClasses.indexOf(heroClass))).append(" ")
                                    .append(ChatColor.DARK_AQUA).append(heroClass).append(" ")
                                    .append(Lang.get("experience"));
                        }
                    }
                    return text.toString();
                }
            } else {
                return ChatColor.GRAY + "(" + Lang.get("notInstalled") + ")";
            }
        case 9:
            if (context.getSessionData(CK.REW_PARTIES_EXPERIENCE) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + context.getSessionData(CK.REW_PARTIES_EXPERIENCE) + " "
                        + Lang.get("points") + ChatColor.GRAY + ")";
            }
        case 10:
            if (plugin.getDependencies().getPhatLoots() != null) {
                if (context.getSessionData(CK.REW_PHAT_LOOTS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> phatLoots = (List<String>) context.getSessionData(CK.REW_PHAT_LOOTS);
                    if (phatLoots != null) {
                        for (final String phatLoot : phatLoots) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA)
                                    .append(phatLoot);
                        }
                    }
                    return text.toString();
                }
            } else {
                return ChatColor.GRAY + "(" + Lang.get("notInstalled") + ")";
            }
        case 11:
            if (context.getSessionData(CK.REW_CUSTOM) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> customRew = (LinkedList<String>) context.getSessionData(CK.REW_CUSTOM);
                if (customRew != null) {
                    for (final String s : customRew) {
                        text.append("\n").append(ChatColor.LIGHT_PURPLE).append("     - ").append(s);
                    }
                }
                return text.toString();
            }
        case 12:
            if (context.getSessionData(CK.REW_DETAILS_OVERRIDE) == null) {
                if (!hasReward) {
                    return ChatColor.GRAY + "(" + Lang.get("stageEditorOptional") + ")";
                } else {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                }
            } else {
                final StringBuilder text = new StringBuilder();
                final List<String> overrides = (List<String>) context.getSessionData(CK.REW_DETAILS_OVERRIDE);
                if (overrides != null) {
                    for (final String override : overrides) {
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA)
                                .append(override);
                    }
                }
                return text.toString();
            }
        case 13:
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
                context.setSessionData(CK.REW_DETAILS_OVERRIDE, null);
            } else {
                final LinkedList<String> overrides = new LinkedList<>();
                if (context.getSessionData(CK.REW_DETAILS_OVERRIDE) != null) {
                    overrides.addAll((List<String>) context.getSessionData(CK.REW_DETAILS_OVERRIDE));
                }
                overrides.add(input);
                context.setSessionData(CK.REW_DETAILS_OVERRIDE, overrides);
                context.setSessionData(classPrefix + "-override", null);
            }
        }
        checkReward(context);

        if (context.getPlugin() != null) {
            final QuestsEditorPostOpenNumericPromptEvent event
                    = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
        }
        
        final StringBuilder text = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle(context).replace((String) Objects
                .requireNonNull(context.getSessionData(CK.Q_NAME)), ChatColor.AQUA + (String) context
                .getSessionData(CK.Q_NAME) + ChatColor.LIGHT_PURPLE));
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
                return new RewardsMoneyPrompt(context);
            } else {
                return new RewardsPrompt(context);
            }
        case 2:
            return new RewardsQuestPointsPrompt(context);
        case 3:
            return new RewardsItemListPrompt(context);
        case 4:
            return new RewardsExperiencePrompt(context);
        case 5:
            if (!plugin.hasLimitedAccess(context.getForWhom())) {
                return new RewardsCommandsPrompt(context);
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("noPermission"));
                return new RewardsPrompt(context);
            }
        case 6:
            if (!plugin.hasLimitedAccess(context.getForWhom())) {
                return new RewardsPermissionsListPrompt(context);
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("noPermission"));
                return new RewardsPrompt(context);
            }
        case 7:
            if (plugin.getDependencies().getMcmmoClassic() != null) {
                return new RewardsMcMMOListPrompt(context);
            } else {
                return new RewardsPrompt(context);
            }
        case 8:
            if (plugin.getDependencies().getHeroes() != null) {
                return new RewardsHeroesListPrompt(context);
            } else {
                return new RewardsPrompt(context);
            }
        case 9:
            return new RewardsPartiesExperiencePrompt(context);
        case 10:
            if (plugin.getDependencies().getPhatLoots() != null) {
                return new RewardsPhatLootsPrompt(context);
            } else {
                return new RewardsPrompt(context);
            }
        case 11:
            return new CustomRewardModulePrompt(context);
        case 12:
            if (hasReward) {
                return new OverridePrompt.Builder()
                        .source(this)
                        .promptText(Lang.get("overrideCreateEnter"))
                        .build();
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidOption"));
                return new RewardsPrompt(context);
            }
        case 13:
            return plugin.getQuestFactory().returnToMenu(context);
        default:
            return new RewardsPrompt(context);
        }
    }
    
    public boolean checkReward(final ConversationContext context) {
        if (context.getSessionData(CK.REW_MONEY) != null 
                || context.getSessionData(CK.REW_EXP) != null
                || context.getSessionData(CK.REW_QUEST_POINTS) != null 
                || context.getSessionData(CK.REW_ITEMS) != null
                || context.getSessionData(CK.REW_COMMAND) != null
                || context.getSessionData(CK.REW_PERMISSION) != null 
                || context.getSessionData(CK.REW_MCMMO_SKILLS) != null 
                || context.getSessionData(CK.REW_HEROES_CLASSES) != null
                || context.getSessionData(CK.REW_PARTIES_EXPERIENCE) != null
                || context.getSessionData(CK.REW_PHAT_LOOTS) != null
                || context.getSessionData(CK.REW_CUSTOM) != null) {
            hasReward = true;
            return true;
        }
        return false;
    }

    public class RewardsMoneyPrompt extends QuestsEditorStringPrompt {
        
        public RewardsMoneyPrompt(final ConversationContext context) {
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
                text = text.replace("<money>", ChatColor.AQUA
                        + plugin.getDependencies().getVaultEconomy().currencyNamePlural() + ChatColor.YELLOW);
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
                        context.setSessionData(CK.REW_MONEY, i);
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("inputPosNum"));
                        return new RewardsMoneyPrompt(context);
                    }
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqNotANumber")
                            .replace("<input>", input));
                    return new RewardsMoneyPrompt(context);
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.REW_MONEY, null);
                return new RewardsPrompt(context);
            }
            return new RewardsPrompt(context);
        }
    }

    public class RewardsExperiencePrompt extends QuestsEditorStringPrompt {

        public RewardsExperiencePrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("rewExperiencePrompt");
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
                        context.setSessionData(CK.REW_EXP, i);
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("inputPosNum"));
                        return new RewardsExperiencePrompt(context);
                    }
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqNotANumber")
                            .replace("<input>", input));
                    return new RewardsExperiencePrompt(context);
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.REW_EXP, null);
                return new RewardsPrompt(context);
            }
            return new RewardsPrompt(context);
        }
    }

    public class RewardsQuestPointsPrompt extends QuestsEditorStringPrompt {
        
        public RewardsQuestPointsPrompt(final ConversationContext context) {
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
                        context.setSessionData(CK.REW_QUEST_POINTS, i);
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("inputPosNum"));
                        return new RewardsQuestPointsPrompt(context);
                    }
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqNotANumber")
                            .replace("<input>", input));
                    return new RewardsQuestPointsPrompt(context);
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.REW_QUEST_POINTS, null);
                return new RewardsPrompt(context);
            }
            return new RewardsPrompt(context);
        }
    }

    public class RewardsItemListPrompt extends QuestsEditorNumericPrompt {

        public RewardsItemListPrompt(final ConversationContext context) {
            super(context);
        }
        
        private final int size = 3;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("itemRewardsTitle");
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
                return ChatColor.YELLOW + Lang.get("stageEditorDeliveryAddItem");
            case 2:
                return ChatColor.RED + Lang.get("clear");
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
                if (context.getSessionData(CK.REW_ITEMS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<ItemStack> items = (List<ItemStack>) context.getSessionData(CK.REW_ITEMS);
                    if (items != null) {
                        for (final ItemStack is : items) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ")
                                    .append(ItemUtil.getDisplayString(is));
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
                if (context.getSessionData(CK.REW_ITEMS) != null) {
                    final List<ItemStack> itemRew = (List<ItemStack>) context.getSessionData(CK.REW_ITEMS);
                    if (itemRew != null) {
                        itemRew.add((ItemStack) context.getSessionData("tempStack"));
                        context.setSessionData(CK.REW_ITEMS, itemRew);
                    }
                } else {
                    final List<ItemStack> itemRew = new LinkedList<>();
                    itemRew.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(CK.REW_ITEMS, itemRew);
                }
                ItemStackPrompt.clearSessionData(context);
            }

            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenNumericPromptEvent event
                        = new QuestsEditorPostOpenNumericPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
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
                return new ItemStackPrompt(context, RewardsItemListPrompt.this);
            case 2:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("rewItemsCleared"));
                context.setSessionData(CK.REW_ITEMS, null);
                return new RewardsItemListPrompt(context);
            case 3:
                return new RewardsPrompt(context);
            default:
                return new RewardsItemListPrompt(context);
            }
        }
    }
    
    public class RewardsCommandsPrompt extends QuestsEditorStringPrompt {
        
        public RewardsCommandsPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("rewCommandPrompt");
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
                final String[] args = input.split(Lang.get("charSemi"));
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
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidOption") 
                                + ChatColor.DARK_RED + " (" + s.trim() + ")");
                        continue;
                    default:
                        commands.add(s.trim());
                    }
                }
                context.setSessionData(CK.REW_COMMAND, commands.isEmpty() ? null : commands);
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.REW_COMMAND, null);
            }
            return new RewardsPrompt(context);
        }
    }
    
    public class RewardsPermissionsListPrompt extends QuestsEditorNumericPrompt {

        public RewardsPermissionsListPrompt(final ConversationContext context) {
            super(context);
        }
        
        private final int size = 4;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("permissionRewardsTitle");
        }
        
        @Override
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                return ChatColor.BLUE;
            case 2:
                if (context.getSessionData(CK.REW_PERMISSION) == null) {
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
                return ChatColor.YELLOW + Lang.get("rewSetPermission");
            case 2:
                if (context.getSessionData(CK.REW_PERMISSION) == null) {
                    return ChatColor.GRAY + Lang.get("rewSetPermissionWorlds");
                } else {
                    return ChatColor.YELLOW + Lang.get("rewSetPermissionWorlds");
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
                if (context.getSessionData(CK.REW_PERMISSION) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> permission = (List<String>) context.getSessionData(CK.REW_PERMISSION);
                    if (permission != null) {
                        for (final String s : permission) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s);
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (context.getSessionData(CK.REW_PERMISSION) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    if (context.getSessionData(CK.REW_PERMISSION_WORLDS) == null) {
                        return ChatColor.YELLOW + "(" + Lang.get("stageEditorOptional") + ")";
                    } else {
                        final StringBuilder text = new StringBuilder();
                        final List<String> permissionWorlds
                                = (List<String>) context.getSessionData(CK.REW_PERMISSION_WORLDS);
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
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenNumericPromptEvent event
                        = new QuestsEditorPostOpenNumericPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
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
                return new PermissionsPrompt(context);
            case 2:
                return new PermissionsWorldsPrompt(context);
            case 3:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("rewPermissionsCleared"));
                context.setSessionData(CK.REW_PERMISSION, null);
                context.setSessionData(CK.REW_PERMISSION_WORLDS, null);
                return new RewardsPermissionsListPrompt(context);
            case 4:
                return new RewardsPrompt(context);
            default:
                return null;
            }
        }
        
    }

    public class PermissionsPrompt extends QuestsEditorStringPrompt {
        
        public PermissionsPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("rewPermissionsPrompt");
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
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidOption") 
                        + ChatColor.DARK_RED + " (" + s.trim() + ")");
                    } else {
                        permissions.add(s.trim());
                    }
                }
                context.setSessionData(CK.REW_PERMISSION, permissions);
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.REW_PERMISSION, null);
            }
            return new RewardsPermissionsListPrompt(context);
        }
    }
    
    public class PermissionsWorldsPrompt extends QuestsEditorStringPrompt {
        
        public PermissionsWorldsPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("rewPermissionsWorldPrompt");
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
                final String[] args = input.split(Lang.get("charSemi"));
                final List<String> worlds = new LinkedList<>(Arrays.asList(args));
                for (final String w : worlds) {
                    if (!w.equals("null") && context.getPlugin() != null
                            && context.getPlugin().getServer().getWorld(w) == null) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + w + " "
                                + Lang.get("eventEditorInvalidWorld"));
                        return new PermissionsWorldsPrompt(context);
                    }
                }
                context.setSessionData(CK.REW_PERMISSION_WORLDS, worlds);
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.REW_PERMISSION_WORLDS, null);
            }
            return new RewardsPermissionsListPrompt(context);
        }
    }

    public class RewardsMcMMOListPrompt extends QuestsEditorNumericPrompt {

        public RewardsMcMMOListPrompt(final ConversationContext context) {
            super(context);
        }
        
        private final int size = 4;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("mcMMORewardsTitle");
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
                return ChatColor.YELLOW + Lang.get("reqSetSkills");
            case 2:
                return ChatColor.YELLOW + Lang.get("reqSetSkillAmounts");
            case 3:
                return ChatColor.RED + Lang.get("clear");
            case 4:
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
                if (context.getSessionData(CK.REW_MCMMO_SKILLS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> skills = (List<String>) context.getSessionData(CK.REW_MCMMO_SKILLS);
                    if (skills != null) {
                        for (final String s : skills) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s);
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (context.getSessionData(CK.REW_MCMMO_AMOUNTS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<Integer> amounts = (List<Integer>) context.getSessionData(CK.REW_MCMMO_AMOUNTS);
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
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenNumericPromptEvent event
                        = new QuestsEditorPostOpenNumericPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
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
                return new McMMOSkillsPrompt(context);
            case 2:
                if (context.getSessionData(CK.REW_MCMMO_SKILLS) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("rewSetMcMMOSkillsFirst"));
                    return new RewardsMcMMOListPrompt(context);
                } else {
                    return new McMMOAmountsPrompt(context);
                }
            case 3:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("rewMcMMOCleared"));
                context.setSessionData(CK.REW_MCMMO_SKILLS, null);
                context.setSessionData(CK.REW_MCMMO_AMOUNTS, null);
                return new RewardsMcMMOListPrompt(context);
            case 4:
                final int one;
                final int two;
                final List<Integer> skills = (List<Integer>) context.getSessionData(CK.REW_MCMMO_SKILLS);
                final List<Integer> amounts = (List<Integer>) context.getSessionData(CK.REW_MCMMO_AMOUNTS);
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
                    return new RewardsPrompt(context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                    return new RewardsMcMMOListPrompt(context);
                }
            default:
                return new RewardsMcMMOListPrompt(context);
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
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                final String[] args = input.split(" ");
                final List<String> skills = new LinkedList<>();
                for (final String s : args) {
                    if (Quests.getMcMMOSkill(s) != null) {
                        if (!skills.contains(s)) {
                            skills.add(MiscUtil.getCapitalized(s));
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listDuplicate"));
                            return new McMMOSkillsPrompt(context);
                        }
                    } else {
                        String text = Lang.get("reqMcMMOError");
                        text = text.replace("<input>", ChatColor.LIGHT_PURPLE + s + ChatColor.RED);
                        context.getForWhom().sendRawMessage(ChatColor.RED + text);
                        return new McMMOSkillsPrompt(context);
                    }
                }
                context.setSessionData(CK.REW_MCMMO_SKILLS, skills);
            }
            return new RewardsMcMMOListPrompt(context);
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
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                final String[] args = input.split(" ");
                final List<Integer> amounts = new LinkedList<>();
                for (final String s : args) {
                    try {
                        amounts.add(Integer.parseInt(s));
                    } catch (final NumberFormatException e) {
                        String text = Lang.get("reqNotANumber");
                        text = text.replace("<input>", ChatColor.LIGHT_PURPLE + s + ChatColor.RED);
                        context.getForWhom().sendRawMessage(ChatColor.RED + text);
                        return new McMMOAmountsPrompt(context);
                    }
                }
                context.setSessionData(CK.REW_MCMMO_AMOUNTS, amounts);
            }
            return new RewardsMcMMOListPrompt(context);
        }
    }

    public class RewardsHeroesListPrompt extends QuestsEditorNumericPrompt {

        public RewardsHeroesListPrompt(final ConversationContext context) {
            super(context);
        }
        
        private final int size = 4;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("heroesRewardsTitle");
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
                return ChatColor.YELLOW + Lang.get("rewSetHeroesClasses");
            case 2:
                return ChatColor.YELLOW + Lang.get("rewSetHeroesAmounts");
            case 3:
                return ChatColor.RED + Lang.get("clear");
            case 4:
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
                if (context.getSessionData(CK.REW_HEROES_CLASSES) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> classes = (List<String>) context.getSessionData(CK.REW_HEROES_CLASSES);
                    if (classes != null) {
                        for (final String s : classes) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s);
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (context.getSessionData(CK.REW_HEROES_AMOUNTS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<Double> amounts = (List<Double>) context.getSessionData(CK.REW_HEROES_AMOUNTS);
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
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenNumericPromptEvent event
                        = new QuestsEditorPostOpenNumericPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }

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
                return new HeroesClassesPrompt(context);
            case 2:
                if (context.getSessionData(CK.REW_HEROES_CLASSES) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("rewSetHeroesClassesFirst"));
                    return new RewardsHeroesListPrompt(context);
                } else {
                    return new HeroesExperiencePrompt(context);
                }
            case 3:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("rewHeroesCleared"));
                context.setSessionData(CK.REW_HEROES_CLASSES, null);
                context.setSessionData(CK.REW_HEROES_AMOUNTS, null);
                return new RewardsHeroesListPrompt(context);
            case 4:
                final int one;
                final int two;
                final List<Integer> classes = (List<Integer>) context.getSessionData(CK.REW_HEROES_CLASSES);
                final List<Double> amounts = (List<Double>) context.getSessionData(CK.REW_HEROES_AMOUNTS);
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
                    return new RewardsPrompt(context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("rewHeroesListsNotSameSize"));
                    return new RewardsHeroesListPrompt(context);
                }
            default:
                return new RewardsHeroesListPrompt(context);
            }
        }
    }

    public class HeroesClassesPrompt extends QuestsEditorStringPrompt {
        
        public HeroesClassesPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("heroesClassesTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("rewHeroesClassesPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            StringBuilder text = new StringBuilder(ChatColor.DARK_PURPLE + getTitle(context) + "\n");
            final List<String> list = new LinkedList<>();
            for (final HeroClass hc : plugin.getDependencies().getHeroes().getClassManager().getClasses()) {
                list.add(hc.getName());
            }
            if (list.isEmpty()) {
                text.append(ChatColor.GRAY).append("(").append(Lang.get("none")).append(")\n");
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
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                final String[] arr = input.split(" ");
                final List<String> classes = new LinkedList<>();
                for (final String s : arr) {
                    final HeroClass hc = plugin.getDependencies().getHeroes().getClassManager().getClass(s);
                    if (hc == null) {
                        String text = Lang.get("rewHeroesInvalidClass");
                        text = text.replace("<input>", ChatColor.LIGHT_PURPLE + s + ChatColor.RED);
                        context.getForWhom().sendRawMessage(ChatColor.RED + text);
                        return new HeroesClassesPrompt(context);
                    } else {
                        classes.add(hc.getName());
                    }
                }
                context.setSessionData(CK.REW_HEROES_CLASSES, classes);
            }
            return new RewardsHeroesListPrompt(context);
        }
    }

    public class HeroesExperiencePrompt extends QuestsEditorStringPrompt {
        
        public HeroesExperiencePrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("heroesExperienceTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("rewHeroesExperiencePrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            String text = getTitle(context) + "\n";
            text += ChatColor.YELLOW + getQueryText(context);
            return text;
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                final String[] arr = input.split(" ");
                final List<Double> amounts = new LinkedList<>();
                for (final String s : arr) {
                    try {
                        final double d = Double.parseDouble(s);
                        amounts.add(d);
                    } catch (final NumberFormatException nfe) {
                        String text = Lang.get("reqNotANumber");
                        text = text.replace("<input>", ChatColor.LIGHT_PURPLE + s + ChatColor.RED);
                        context.getForWhom().sendRawMessage(ChatColor.RED + text);
                        return new HeroesExperiencePrompt(context);
                    }
                }
                context.setSessionData(CK.REW_HEROES_AMOUNTS, amounts);
            }
            return new RewardsHeroesListPrompt(context);
        }
    }
    
    public class RewardsPartiesExperiencePrompt extends QuestsEditorStringPrompt {
        
        public RewardsPartiesExperiencePrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }
        
        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("rewPartiesExperiencePrompt");
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
                        context.setSessionData(CK.REW_PARTIES_EXPERIENCE, i);
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("inputPosNum"));
                        return new RewardsPartiesExperiencePrompt(context);
                    }
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqNotANumber")
                            .replace("<input>", input));
                    return new RewardsPartiesExperiencePrompt(context);
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.REW_PARTIES_EXPERIENCE, null);
                return new RewardsPrompt(context);
            }
            return new RewardsPrompt(context);
        }
    }

    public class RewardsPhatLootsPrompt extends QuestsEditorStringPrompt {
        
        public RewardsPhatLootsPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("phatLootsRewardsTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("rewPhatLootsPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            final StringBuilder text = new StringBuilder(ChatColor.DARK_AQUA + getTitle(context) + "\n");
            for (final PhatLoot pl : PhatLootsAPI.getAllPhatLoots()) {
                text.append(ChatColor.GRAY).append("- ").append(ChatColor.BLUE).append(pl.name).append("\n");
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
                final String[] arr = input.split(" ");
                for (final String s : arr) {
                    if (PhatLootsAPI.getPhatLoot(s) == null) {
                        String text = Lang.get("rewPhatLootsInvalid");
                        text = text.replace("<input>", ChatColor.DARK_RED + s + ChatColor.RED);
                        context.getForWhom().sendRawMessage(ChatColor.RED + text);
                        return new RewardsPhatLootsPrompt(context);
                    }
                }
                final List<String> loots = new LinkedList<>(Arrays.asList(arr));
                context.setSessionData(CK.REW_PHAT_LOOTS, loots);
                return new RewardsPrompt(context);
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.REW_PHAT_LOOTS, null);
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("rewPhatLootsCleared"));
                return new RewardsPrompt(context);
            } else {
                return new RewardsPrompt(context);
            }
        }
    }

    public class CustomRewardModulePrompt extends QuestsEditorStringPrompt {

        public CustomRewardModulePrompt(final ConversationContext context) {
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
            if (!(context.getForWhom() instanceof Player)
                    || !((Quests)context.getPlugin()).getSettings().canClickablePrompts()) {
                final StringBuilder text = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle(context) + "\n");
                if (plugin.getCustomRewards().isEmpty()) {
                    text.append(ChatColor.DARK_AQUA).append(ChatColor.UNDERLINE)
                            .append("https://pikamug.gitbook.io/quests/casual/modules").append(ChatColor.RESET)
                            .append("\n");
                    text.append(ChatColor.DARK_PURPLE).append("(").append(Lang.get("stageEditorNoModules"))
                            .append(") ");
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
            if (plugin.getCustomObjectives().isEmpty()) {
                final TextComponent link = new TextComponent("https://pikamug.gitbook.io/quests/casual/modules\n");
                link.setColor(net.md_5.bungee.api.ChatColor.DARK_AQUA);
                link.setUnderlined(true);
                line.addExtra(link);
                line.addExtra(ChatColor.DARK_AQUA + "(" + Lang.get("stageEditorNoModules") + ") ");
            } else {
                for (final String name : plugin.getCustomRewards().stream().map(CustomReward::getModuleName)
                        .collect(Collectors.toCollection(TreeSet::new))) {
                    final TextComponent click = new TextComponent(ChatColor.DARK_PURPLE + "  - " + name + "\n");
                    click.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, name));
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
            if (input != null && !input.equalsIgnoreCase(Lang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(Lang.get("cmdClear"))) {
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
                    return new CustomRewardsPrompt(found, context);
                }
            } else if (input != null && input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                return new RewardsPrompt(context);
            } else if (input != null && input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.REW_CUSTOM, null);
                context.setSessionData(CK.REW_CUSTOM_DATA, null);
                context.setSessionData(CK.REW_CUSTOM_DATA_TEMP, null);
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("rewCustomCleared"));
                return new RewardsPrompt(context);
            }
            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("rewCustomNotFound"));
            return new CustomRewardModulePrompt(context);
        }
    }

    public class CustomRewardsPrompt extends QuestsEditorStringPrompt {

        private final String moduleName;
        
        public CustomRewardsPrompt(final String moduleName, final ConversationContext context) {
            super(context);
            this.moduleName = moduleName;
        }

        public String getModuleName() {
            return moduleName;
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("customRewardsTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("rewCustomRewardPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            if (!(context.getForWhom() instanceof Player)
                    || !((Quests)context.getPlugin()).getSettings().canClickablePrompts()) {
                final StringBuilder text = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle(context) + "\n");
                if (plugin.getCustomRewards().isEmpty()) {
                    text.append(ChatColor.DARK_AQUA).append(ChatColor.UNDERLINE)
                            .append("https://pikamug.gitbook.io/quests/casual/modules\n").append(ChatColor.DARK_PURPLE)
                            .append("(").append(Lang.get("stageEditorNoModules")).append(") ");
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
            if (plugin.getCustomObjectives().isEmpty()) {
                final TextComponent link = new TextComponent("https://pikamug.gitbook.io/quests/casual/modules\n");
                link.setColor(net.md_5.bungee.api.ChatColor.DARK_AQUA);
                link.setUnderlined(true);
                line.addExtra(link);
                line.addExtra(ChatColor.DARK_AQUA + "(" + Lang.get("stageEditorNoModules") + ") ");
            } else {
                for (final CustomReward co : plugin.getCustomRewards()) {
                    if (co.getModuleName().equals(moduleName)) {
                        final TextComponent click = new TextComponent(ChatColor.DARK_PURPLE + "  - " + co.getName()
                                + "\n");
                        click.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, co.getName()));
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
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel")) && !input.equalsIgnoreCase(Lang.get("cmdClear"))) {
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
                    if (context.getSessionData(CK.REW_CUSTOM) != null) {
                        // The custom reward may already have been added, so let's check that
                        final LinkedList<String> list = (LinkedList<String>) context.getSessionData(CK.REW_CUSTOM);
                        final LinkedList<Map<String, Object>> dataMapList
                                = (LinkedList<Map<String, Object>>) context.getSessionData(CK.REW_CUSTOM_DATA);
                        if (list != null && dataMapList != null && !list.contains(found.getName())) {
                            // Hasn't been added yet, so let's do it
                            list.add(found.getName());
                            dataMapList.add(found.getData());
                            context.setSessionData(CK.REW_CUSTOM, list);
                            context.setSessionData(CK.REW_CUSTOM_DATA, dataMapList);
                        } else {
                            // Already added, so inform user
                            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("rewCustomAlreadyAdded"));
                            return new CustomRewardsPrompt(moduleName, context);
                        }
                    } else {
                        // The custom reward hasn't been added yet, so let's do it
                        final LinkedList<Map<String, Object>> dataMapList = new LinkedList<>();
                        dataMapList.add(found.getData());
                        final LinkedList<String> list = new LinkedList<>();
                        list.add(found.getName());
                        context.setSessionData(CK.REW_CUSTOM, list);
                        context.setSessionData(CK.REW_CUSTOM_DATA, dataMapList);
                    }
                    // Send user to the custom data prompt if there is any needed
                    if (!found.getData().isEmpty()) {
                        context.setSessionData(CK.REW_CUSTOM_DATA_DESCRIPTIONS, found.getDescriptions());
                        return new RewardCustomDataListPrompt();
                    }
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("rewCustomNotFound"));
                    return new CustomRewardsPrompt(moduleName, context);
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.REW_CUSTOM, null);
                context.setSessionData(CK.REW_CUSTOM_DATA, null);
                context.setSessionData(CK.REW_CUSTOM_DATA_TEMP, null);
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("rewCustomCleared"));
            }
            return new RewardsPrompt(context);
        }
    }

    private class RewardCustomDataListPrompt extends StringPrompt {

        @SuppressWarnings("unchecked")
        @Override
        public @NotNull String getPromptText(final ConversationContext context) {
            final StringBuilder text = new StringBuilder(ChatColor.GOLD + "- ");
            final LinkedList<String> list = (LinkedList<String>) context.getSessionData(CK.REW_CUSTOM);
            final LinkedList<Map<String, Object>> dataMapList
                    = (LinkedList<Map<String, Object>>) context.getSessionData(CK.REW_CUSTOM_DATA);
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
                    = (LinkedList<Map<String, Object>>) context.getSessionData(CK.REW_CUSTOM_DATA);
            if (dataMapList != null) {
                final Map<String, Object> dataMap = dataMapList.getLast();
                final int numInput;
                try {
                    numInput = Integer.parseInt(input);
                } catch (final NumberFormatException nfe) {
                    return new RewardCustomDataListPrompt();
                }
                if (numInput < 1 || numInput > dataMap.size() + 1) {
                    return new RewardCustomDataListPrompt();
                }
                if (numInput < dataMap.size() + 1) {
                    final LinkedList<String> dataMapKeys = new LinkedList<>(dataMap.keySet());
                    Collections.sort(dataMapKeys);
                    final String selectedKey = dataMapKeys.get(numInput - 1);
                    context.setSessionData(CK.REW_CUSTOM_DATA_TEMP, selectedKey);
                    return new RewardCustomDataPrompt();
                } else {
                    if (dataMap.containsValue(null)) {
                        return new RewardCustomDataListPrompt();
                    } else {
                        context.setSessionData(CK.REW_CUSTOM_DATA_DESCRIPTIONS, null);
                    }
                }
            }
            return new RewardsPrompt(context);
        }
    }

    private class RewardCustomDataPrompt extends StringPrompt {

        @Override
        public @NotNull String getPromptText(final ConversationContext context) {
            String text = "";
            final String temp = (String) context.getSessionData(CK.REW_CUSTOM_DATA_TEMP);
            @SuppressWarnings("unchecked")
            final
            Map<String, String> descriptions 
                    = (Map<String, String>) context.getSessionData(CK.REW_CUSTOM_DATA_DESCRIPTIONS);
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
                    = (LinkedList<Map<String, Object>>) context.getSessionData(CK.REW_CUSTOM_DATA);
            if (dataMapList != null) {
                final Map<String, Object> dataMap = dataMapList.getLast();
                dataMap.put((String) context.getSessionData(CK.REW_CUSTOM_DATA_TEMP), input);
                context.setSessionData(CK.REW_CUSTOM_DATA_TEMP, null);
            }
            return new RewardCustomDataListPrompt();
        }
    }
}
