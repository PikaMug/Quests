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
import me.pikamug.quests.convo.quests.QuestsEditorIntegerPrompt;
import me.pikamug.quests.convo.quests.QuestsEditorStringPrompt;
import me.pikamug.quests.events.editor.quests.BukkitQuestsEditorPostOpenNumericPromptEvent;
import me.pikamug.quests.events.editor.quests.BukkitQuestsEditorPostOpenStringPromptEvent;
import me.pikamug.quests.module.CustomReward;
import me.pikamug.quests.util.BukkitItemUtil;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.quests.util.BukkitMiscUtil;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.SessionData;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
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
import java.util.UUID;
import java.util.stream.Collectors;

public class QuestRewardsPrompt extends QuestsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final BukkitQuestsPlugin plugin;
    private final String classPrefix;
    private boolean hasReward = false;
    private final int size = 12;

    public QuestRewardsPrompt(final @NotNull UUID uuid) {
        super(uuid);
        this.uuid = uuid;
        this.plugin = BukkitQuestsPlugin.getInstance();
        this.classPrefix = getClass().getSimpleName();
    }
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle() {
        return BukkitLang.get("rewardsTitle").replace("<quest>", (String) Objects
                .requireNonNull(SessionData.get(uuid, Key.Q_NAME)));
    }
    
    @Override
    public ChatColor getNumberColor(final int number) {
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
            if (SessionData.get(uuid, Key.REW_DETAILS_OVERRIDE) == null) {
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
    public String getSelectionText(final int number) {
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
    public String getAdditionalText(final int number) {
        switch (number) {
        case 1:
            if (plugin.getDependencies().getVaultEconomy() != null) {
                final Integer moneyRew = (Integer) SessionData.get(uuid, Key.REW_MONEY);
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
            if (SessionData.get(uuid, Key.REW_QUEST_POINTS) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + SessionData.get(uuid, Key.REW_QUEST_POINTS) + " "
                        + BukkitLang.get("questPoints") + ChatColor.GRAY + ")";
            }
        case 3:
            if (SessionData.get(uuid, Key.REW_ITEMS) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<ItemStack> items = (LinkedList<ItemStack>) SessionData.get(uuid, Key.REW_ITEMS);
                if (items != null) {
                    for (final ItemStack item : items) {
                        if (item == null) {
                            text.append(ChatColor.RED).append("     - null\n");
                            plugin.getLogger().severe(ChatColor.RED + "Item reward was null while editing quest ID "
                                    + SessionData.get(uuid, Key.Q_ID));
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
            if (SessionData.get(uuid, Key.REW_EXP) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + SessionData.get(uuid, Key.REW_EXP) + " "
                        + BukkitLang.get("points") + ChatColor.GRAY + ")";
            }
        case 5:
            if (SessionData.get(uuid, Key.REW_COMMAND) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final List<String> commands = (List<String>) SessionData.get(uuid, Key.REW_COMMAND);
                final List<String> overrides = (List<String>) SessionData.get(uuid, Key.REW_COMMAND_OVERRIDE_DISPLAY);
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
            if (SessionData.get(uuid, Key.REW_PERMISSION) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final List<String> permissions = (List<String>) SessionData.get(uuid, Key.REW_PERMISSION);
                final List<String> worlds = (List<String>) SessionData.get(uuid, Key.REW_PERMISSION_WORLDS);
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
                if (SessionData.get(uuid, Key.REW_MCMMO_SKILLS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> skills = (List<String>) SessionData.get(uuid, Key.REW_MCMMO_SKILLS);
                    final List<Integer> amounts = (List<Integer>) SessionData.get(uuid, Key.REW_MCMMO_AMOUNTS);
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
                if (SessionData.get(uuid, Key.REW_HEROES_CLASSES) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> heroClasses = (List<String>) SessionData.get(uuid, Key.REW_HEROES_CLASSES);
                    final List<Double> amounts = (List<Double>) SessionData.get(uuid, Key.REW_HEROES_AMOUNTS);
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
            if (SessionData.get(uuid, Key.REW_PARTIES_EXPERIENCE) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + SessionData.get(uuid, Key.REW_PARTIES_EXPERIENCE) + " "
                        + BukkitLang.get("points") + ChatColor.GRAY + ")";
            }
        case 10:
            if (SessionData.get(uuid, Key.REW_CUSTOM) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> customRew = (LinkedList<String>) SessionData.get(uuid, Key.REW_CUSTOM);
                if (customRew != null) {
                    for (final String s : customRew) {
                        text.append("\n").append(ChatColor.LIGHT_PURPLE).append("     - ").append(s);
                    }
                }
                return text.toString();
            }
        case 11:
            if (SessionData.get(uuid, Key.REW_DETAILS_OVERRIDE) == null) {
                if (!hasReward) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("stageEditorOptional") + ")";
                } else {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                }
            } else {
                final StringBuilder text = new StringBuilder();
                final List<String> overrides = (List<String>) SessionData.get(uuid, Key.REW_DETAILS_OVERRIDE);
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

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull String getPromptText() {
        final String input = (String) SessionData.get(uuid, classPrefix + "-override");
        if (input != null && !input.equalsIgnoreCase(BukkitLang.get("cancel"))) {
            if (input.equalsIgnoreCase(BukkitLang.get("clear"))) {
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

        final BukkitQuestsEditorPostOpenNumericPromptEvent event
                = new BukkitQuestsEditorPostOpenNumericPromptEvent(uuid, this);
        plugin.getServer().getPluginManager().callEvent(event);
        
        final StringBuilder text = new StringBuilder(ChatColor.LIGHT_PURPLE + "- " + getTitle()
                .replace((String) Objects.requireNonNull(SessionData.get(uuid, Key.Q_NAME)), ChatColor.AQUA
                + (String) SessionData.get(uuid, Key.Q_NAME) + ChatColor.LIGHT_PURPLE) + " -");
        for (int i = 1; i <= size; i++) {
            text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                    .append(ChatColor.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                    .append(getAdditionalText(i));
        }
        return sendClickableSelection(text.toString(), plugin.getQuester(uuid));
    }

    @Override
    public void acceptInput(final Number input) {
        final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
        switch (input.intValue()) {
        case 1:
            if (plugin.getDependencies().getVaultEconomy() != null) {
                new QuestRewardsMoneyPrompt(uuid).start();
            } else {
                new QuestRewardsPrompt(uuid).start();
            }
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
                sender.sendMessage(ChatColor.RED + BukkitLang.get("noPermission"));
                new QuestRewardsPrompt(uuid).start();
            }
            break;
        case 6:
            if (!plugin.hasLimitedAccess(uuid)) {
                new QuestRewardsPermissionsListPrompt(uuid).start();
            } else {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("noPermission"));
                new QuestRewardsPrompt(uuid).start();
            }
            break;
        case 7:
            if (plugin.getDependencies().getMcmmoClassic() != null) {
                new QuestRewardsMcMMOListPrompt(uuid).start();
            } else {
                new QuestRewardsPrompt(uuid).start();
            }
            break;
        case 8:
            if (plugin.getDependencies().getHeroes() != null) {
                new QuestRewardsHeroesListPrompt(uuid).start();
            } else {
                new QuestRewardsPrompt(uuid).start();
            }
            break;
        case 9:
            new QuestRewardsPartiesExperiencePrompt(uuid).start();
            break;
        case 10:
            new QuestCustomRewardModulePrompt(uuid).start();
            break;
        case 11:
            if (hasReward) {
                new OverridePrompt(uuid, this, BukkitLang.get("overrideCreateEnter")).start();
            } else {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("invalidOption"));
                new QuestRewardsPrompt(uuid).start();
            }
            break;
        case 12:
            plugin.getQuestFactory().returnToMenu(uuid);
            break;
        default:
            new QuestRewardsPrompt(uuid).start();
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

    public class QuestRewardsMoneyPrompt extends QuestsEditorStringPrompt {
        
        public QuestRewardsMoneyPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("rewMoneyPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            String text = getQueryText();
            if (plugin.getDependencies().getVaultEconomy() != null) {
                text = text.replace("<money>", plugin.getDependencies().getVaultEconomy().currencyNamePlural());
            }
            return ChatColor.YELLOW + text;
        }
        
        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel")) && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i > 0) {
                        SessionData.set(uuid, Key.REW_MONEY, i);
                    } else {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("inputPosNum"));
                        new QuestRewardsMoneyPrompt(uuid).start();
                        return;
                    }
                } catch (final NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                            .replace("<input>", input));
                    new QuestRewardsMoneyPrompt(uuid).start();
                    return;
                }
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.REW_MONEY, null);
                new QuestRewardsPrompt(uuid).start();
                return;
            }
            new QuestRewardsPrompt(uuid).start();
        }
    }

    public class QuestRewardsQuestPointsPrompt extends QuestsEditorStringPrompt {
        
        public QuestRewardsQuestPointsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("rewQuestPointsPrompt").replace("<points>", BukkitLang.get("questPoints"));
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }
        
        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel")) && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i > 0) {
                        SessionData.set(uuid, Key.REW_QUEST_POINTS, i);
                    } else {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("inputPosNum"));
                        new QuestRewardsQuestPointsPrompt(uuid).start();
                        return;
                    }
                } catch (final NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                            .replace("<input>", input));
                    new QuestRewardsQuestPointsPrompt(uuid).start();
                    return;
                }
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.REW_QUEST_POINTS, null);
                new QuestRewardsPrompt(uuid).start();
                return;
            }
            new QuestRewardsPrompt(uuid).start();
        }
    }

    public class QuestRewardsItemListPrompt extends QuestsEditorIntegerPrompt {

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
            return BukkitLang.get("itemRewardsTitle");
        }
        
        @Override
        public ChatColor getNumberColor(final int number) {
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
        public String getSelectionText(final int number) {
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
        public String getAdditionalText(final int number) {
            switch(number) {
            case 1:
                if (SessionData.get(uuid, Key.REW_ITEMS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<ItemStack> items = (List<ItemStack>) SessionData.get(uuid, Key.REW_ITEMS);
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
                ItemStackPrompt.clearSessionData(uuid);
            }

            final BukkitQuestsEditorPostOpenNumericPromptEvent event
                    = new BukkitQuestsEditorPostOpenNumericPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.AQUA + getTitle());
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                        .append(getAdditionalText(i));
            }
            return sendClickableSelection(text.toString(), plugin.getQuester(uuid));
        }
        
        @Override
        public void acceptInput(final Number input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            switch (input.intValue()) {
            case 1:
                new ItemStackPrompt(uuid, QuestRewardsItemListPrompt.this).start();
                break;
            case 2:
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("rewItemsCleared"));
                SessionData.set(uuid, Key.REW_ITEMS, null);
                new QuestRewardsItemListPrompt(uuid).start();
                break;
            case 3:
                new QuestRewardsPrompt(uuid).start();
                break;
            default:
                new QuestRewardsItemListPrompt(uuid).start();
                break;
            }
        }
    }

    public class QuestRewardsExperiencePrompt extends QuestsEditorStringPrompt {

        public QuestRewardsExperiencePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("rewExperiencePrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel")) && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i > 0) {
                        SessionData.set(uuid, Key.REW_EXP, i);
                    } else {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("inputPosNum"));
                        new QuestRewardsExperiencePrompt(uuid).start();
                        return;
                    }
                } catch (final NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                            .replace("<input>", input));
                    new QuestRewardsExperiencePrompt(uuid).start();
                    return;
                }
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.REW_EXP, null);
                new QuestRewardsPrompt(uuid).start();
                return;
            }
            new QuestRewardsPrompt(uuid).start();
        }
    }
    
    public class QuestRewardsCommandsPrompt extends QuestsEditorStringPrompt {
        
        public QuestRewardsCommandsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("rewCommandPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
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
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("invalidOption")
                                + ChatColor.DARK_RED + " (" + s.trim() + ")");
                        continue;
                    default:
                        commands.add(s.trim());
                    }
                }
                SessionData.set(uuid, Key.REW_COMMAND, commands.isEmpty() ? null : commands);
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.REW_COMMAND, null);
            }
            new QuestRewardsPrompt(uuid).start();
        }
    }
    
    public class QuestRewardsPermissionsListPrompt extends QuestsEditorIntegerPrompt {

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
            return BukkitLang.get("permissionRewardsTitle");
        }
        
        @Override
        public ChatColor getNumberColor(final int number) {
            switch (number) {
            case 1:
                return ChatColor.BLUE;
            case 2:
                if (SessionData.get(uuid, Key.REW_PERMISSION) == null) {
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
        public String getSelectionText(final int number) {
            switch (number) {
            case 1:
                return ChatColor.YELLOW + BukkitLang.get("rewSetPermission");
            case 2:
                if (SessionData.get(uuid, Key.REW_PERMISSION) == null) {
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
        public String getAdditionalText(final int number) {
            switch (number) {
            case 1:
                if (SessionData.get(uuid, Key.REW_PERMISSION) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> permission = (List<String>) SessionData.get(uuid, Key.REW_PERMISSION);
                    if (permission != null) {
                        for (final String s : permission) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s);
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (SessionData.get(uuid, Key.REW_PERMISSION) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    if (SessionData.get(uuid, Key.REW_PERMISSION_WORLDS) == null) {
                        return ChatColor.YELLOW + "(" + BukkitLang.get("stageEditorOptional") + ")";
                    } else {
                        final StringBuilder text = new StringBuilder();
                        final List<String> permissionWorlds
                                = (List<String>) SessionData.get(uuid, Key.REW_PERMISSION_WORLDS);
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
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenNumericPromptEvent event
                    = new BukkitQuestsEditorPostOpenNumericPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.GOLD + getTitle());
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                        .append(getAdditionalText(i));
            }
            return sendClickableSelection(text.toString(), plugin.getQuester(uuid));
        }

        @Override
        public void acceptInput(final Number input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            switch (input.intValue()) {
            case 1:
                new QuestPermissionsPrompt(uuid).start();
                break;
            case 2:
                new QuestPermissionsWorldsPrompt(uuid).start();
                break;
            case 3:
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("rewPermissionsCleared"));
                SessionData.set(uuid, Key.REW_PERMISSION, null);
                SessionData.set(uuid, Key.REW_PERMISSION_WORLDS, null);
                new QuestRewardsPermissionsListPrompt(uuid).start();
                break;
            case 4:
                new QuestRewardsPrompt(uuid).start();
                break;
            default:
                new QuestRewardsPrompt(uuid).start();
                break;
            }
        }
        
    }

    public class QuestPermissionsPrompt extends QuestsEditorStringPrompt {
        
        public QuestPermissionsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("rewPermissionsPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
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
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("invalidOption")
                        + ChatColor.DARK_RED + " (" + s.trim() + ")");
                    } else {
                        permissions.add(s.trim());
                    }
                }
                SessionData.set(uuid, Key.REW_PERMISSION, permissions);
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.REW_PERMISSION, null);
            }
            new QuestRewardsPermissionsListPrompt(uuid).start();
        }
    }
    
    public class QuestPermissionsWorldsPrompt extends QuestsEditorStringPrompt {
        
        public QuestPermissionsWorldsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("rewPermissionsWorldPrompt");
        }
        
        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel")) && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                final String[] args = input.split(BukkitLang.get("charSemi"));
                final List<String> worlds = new LinkedList<>(Arrays.asList(args));
                for (final String w : worlds) {
                    if (!w.equals("null") && plugin.getServer().getWorld(w) == null) {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("eventEditorInvalidWorld")
                                .replace("<input>", w));
                        new QuestPermissionsWorldsPrompt(uuid).start();
                        return;
                    }
                }
                SessionData.set(uuid, Key.REW_PERMISSION_WORLDS, worlds);
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.REW_PERMISSION_WORLDS, null);
            }
            new QuestRewardsPermissionsListPrompt(uuid).start();
        }
    }

    public class QuestRewardsMcMMOListPrompt extends QuestsEditorIntegerPrompt {

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
            return BukkitLang.get("mcMMORewardsTitle");
        }
        
        @Override
        public ChatColor getNumberColor(final int number) {
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
        public String getSelectionText(final int number) {
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
        public String getAdditionalText(final int number) {
            switch(number) {
            case 1:
                if (SessionData.get(uuid, Key.REW_MCMMO_SKILLS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> skills = (List<String>) SessionData.get(uuid, Key.REW_MCMMO_SKILLS);
                    if (skills != null) {
                        for (final String s : skills) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s);
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (SessionData.get(uuid, Key.REW_MCMMO_AMOUNTS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<Integer> amounts = (List<Integer>) SessionData.get(uuid, Key.REW_MCMMO_AMOUNTS);
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
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenNumericPromptEvent event
                    = new BukkitQuestsEditorPostOpenNumericPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle() + " -");
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                        .append(getAdditionalText(i));
            }
            return sendClickableSelection(text.toString(), plugin.getQuester(uuid));
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final Number input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            switch(input.intValue()) {
            case 1:
                new QuestMcMMOSkillsPrompt(uuid).start();
                break;
            case 2:
                if (SessionData.get(uuid, Key.REW_MCMMO_SKILLS) == null) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("rewSetMcMMOSkillsFirst"));
                    new QuestRewardsMcMMOListPrompt(uuid).start();
                } else {
                    new QuestMcMMOAmountsPrompt(uuid).start();
                }
                break;
            case 3:
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("rewMcMMOCleared"));
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
                    new QuestRewardsPrompt(uuid).start();
                } else {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("listsNotSameSize"));
                    new QuestRewardsMcMMOListPrompt(uuid).start();
                }
                break;
            default:
                new QuestRewardsMcMMOListPrompt(uuid).start();
                break;
            }
        }
    }

    public class QuestMcMMOSkillsPrompt extends QuestsEditorStringPrompt {
        
        public QuestMcMMOSkillsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return BukkitLang.get("skillListTitle");
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("rewMcMMOPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder skillList = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle() + "\n");
            final SkillType[] skills = SkillType.values();
            for (int i = 0; i < skills.length; i++) {
                skillList.append(ChatColor.AQUA).append(BukkitMiscUtil.snakeCaseToUpperCamelCase(skills[i].getName()));
                if (i < (skills.length - 1)) {
                    skillList.append(ChatColor.GRAY).append(", ");
                }
            }
            skillList.append("\n").append(ChatColor.YELLOW).append(getQueryText());
            return skillList.toString();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final String[] args = input.split(" ");
                final List<String> skills = new LinkedList<>();
                for (final String s : args) {
                    if (plugin.getDependencies().getMcMMOSkill(s) != null) {
                        if (!skills.contains(s)) {
                            skills.add(BukkitMiscUtil.getCapitalized(s));
                        } else {
                            sender.sendMessage(ChatColor.RED + BukkitLang.get("listDuplicate"));
                            new QuestMcMMOSkillsPrompt(uuid).start();
                            break;
                        }
                    } else {
                        String text = BukkitLang.get("reqMcMMOError");
                        text = text.replace("<input>", s);
                        sender.sendMessage(ChatColor.RED + text);
                        new QuestMcMMOSkillsPrompt(uuid).start();
                        break;
                    }
                }
                SessionData.set(uuid, Key.REW_MCMMO_SKILLS, skills);
            }
            new QuestRewardsMcMMOListPrompt(uuid).start();
        }
    }

    public class QuestMcMMOAmountsPrompt extends QuestsEditorStringPrompt {
        
        public QuestMcMMOAmountsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("reqMcMMOAmountsPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final String[] args = input.split(" ");
                final List<Integer> amounts = new LinkedList<>();
                for (final String s : args) {
                    try {
                        amounts.add(Integer.parseInt(s));
                    } catch (final NumberFormatException e) {
                        String text = BukkitLang.get("reqNotANumber");
                        text = text.replace("<input>", s);
                        sender.sendMessage(ChatColor.RED + text);
                        new QuestMcMMOAmountsPrompt(uuid).start();
                        return;
                    }
                }
                SessionData.set(uuid, Key.REW_MCMMO_AMOUNTS, amounts);
            }
            new QuestRewardsMcMMOListPrompt(uuid).start();
        }
    }

    public class QuestRewardsHeroesListPrompt extends QuestsEditorIntegerPrompt {

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
            return BukkitLang.get("heroesRewardsTitle");
        }
        
        @Override
        public ChatColor getNumberColor(final int number) {
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
        public String getSelectionText(final int number) {
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
        public String getAdditionalText(final int number) {
            switch(number) {
            case 1:
                if (SessionData.get(uuid, Key.REW_HEROES_CLASSES) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> classes = (List<String>) SessionData.get(uuid, Key.REW_HEROES_CLASSES);
                    if (classes != null) {
                        for (final String s : classes) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s);
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (SessionData.get(uuid, Key.REW_HEROES_AMOUNTS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<Double> amounts = (List<Double>) SessionData.get(uuid, Key.REW_HEROES_AMOUNTS);
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
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenNumericPromptEvent event
                    = new BukkitQuestsEditorPostOpenNumericPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle() + " -");
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                        .append(getAdditionalText(i));
            }
            return sendClickableSelection(text.toString(), plugin.getQuester(uuid));
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final Number input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            switch(input.intValue()) {
            case 1:
                new QuestHeroesClassesPrompt(uuid).start();
                break;
            case 2:
                if (SessionData.get(uuid, Key.REW_HEROES_CLASSES) == null) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("rewSetHeroesClassesFirst"));
                    new QuestRewardsHeroesListPrompt(uuid).start();
                } else {
                    new QuestHeroesExperiencePrompt(uuid).start();
                }
                break;
            case 3:
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("rewHeroesCleared"));
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
                    new QuestRewardsPrompt(uuid).start();
                } else {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("rewHeroesListsNotSameSize"));
                    new QuestRewardsHeroesListPrompt(uuid).start();
                }
                break;
            default:
                new QuestRewardsHeroesListPrompt(uuid).start();
                break;
            }
        }
    }

    public class QuestHeroesClassesPrompt extends QuestsEditorStringPrompt {
        
        public QuestHeroesClassesPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return BukkitLang.get("heroesClassesTitle");
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("rewHeroesClassesPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            StringBuilder text = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle() + "\n");
            final List<String> list = new LinkedList<>();
            for (final HeroClass hc : plugin.getDependencies().getHeroes().getClassManager().getClasses()) {
                list.add(hc.getName());
            }
            if (list.isEmpty()) {
                text.append(ChatColor.GRAY).append("(").append(BukkitLang.get("none")).append(")\n");
            } else {
                Collections.sort(list);
                for (int i = 0; i < list.size(); i++) {
                    text.append(ChatColor.AQUA).append(BukkitMiscUtil.snakeCaseToUpperCamelCase(list.get(i)));
                    if (i < (list.size() - 1)) {
                        text.append(ChatColor.GRAY).append(", ");
                    }
                }
            }
            text.append("\n").append(ChatColor.YELLOW).append(getQueryText());
            return text.toString();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final String[] arr = input.split(" ");
                final List<String> classes = new LinkedList<>();
                for (final String s : arr) {
                    final HeroClass hc = plugin.getDependencies().getHeroes().getClassManager().getClass(s);
                    if (hc == null) {
                        String text = BukkitLang.get("rewHeroesInvalidClass");
                        text = text.replace("<input>", s);
                        sender.sendMessage(ChatColor.RED + text);
                        new QuestHeroesClassesPrompt(uuid).start();
                        return;
                    } else {
                        classes.add(hc.getName());
                    }
                }
                SessionData.set(uuid, Key.REW_HEROES_CLASSES, classes);
            }
            new QuestRewardsHeroesListPrompt(uuid).start();
        }
    }

    public class QuestHeroesExperiencePrompt extends QuestsEditorStringPrompt {
        
        public QuestHeroesExperiencePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return BukkitLang.get("heroesExperienceTitle");
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("rewHeroesExperiencePrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            String text = getTitle() + "\n";
            text += ChatColor.YELLOW + getQueryText();
            return text;
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
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
                        sender.sendMessage(ChatColor.RED + text);
                        new QuestHeroesExperiencePrompt(uuid).start();
                        return;
                    }
                }
                SessionData.set(uuid, Key.REW_HEROES_AMOUNTS, amounts);
            }
            new QuestRewardsHeroesListPrompt(uuid).start();
        }
    }
    
    public class QuestRewardsPartiesExperiencePrompt extends QuestsEditorStringPrompt {
        
        public QuestRewardsPartiesExperiencePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return null;
        }
        
        @Override
        public String getQueryText() {
            return BukkitLang.get("rewPartiesExperiencePrompt");
        }
        
        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }
        
        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel")) && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i > 0) {
                        SessionData.set(uuid, Key.REW_PARTIES_EXPERIENCE, i);
                    } else {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("inputPosNum"));
                        new QuestRewardsPartiesExperiencePrompt(uuid).start();
                        return;
                    }
                } catch (final NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                            .replace("<input>", input));
                    new QuestRewardsPartiesExperiencePrompt(uuid).start();
                    return;
                }
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.REW_PARTIES_EXPERIENCE, null);
                new QuestRewardsPrompt(uuid).start();
                return;
            }
            new QuestRewardsPrompt(uuid).start();
        }
    }

    public class QuestCustomRewardModulePrompt extends QuestsEditorStringPrompt {

        public QuestCustomRewardModulePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return BukkitLang.get("stageEditorModules");
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("stageEditorModulePrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            if (!(BukkitMiscUtil.getEntity(uuid) instanceof Player) || !plugin.getConfigSettings().canClickablePrompts()) {
                final StringBuilder text = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle() + "\n");
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
                return text.toString() + ChatColor.YELLOW + getQueryText();
            }
            final TextComponent component = new TextComponent(getTitle() + "\n");
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
            component.addExtra(ChatColor.YELLOW + getQueryText());
            BukkitMiscUtil.getEntity(uuid).spigot().sendMessage(component);
            return "";
        }

        @Override
        public void acceptInput(@Nullable final String input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
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
                    new QuestCustomRewardsPrompt(found, uuid).start();
                    return;
                }
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                new QuestRewardsPrompt(uuid).start();
                return;
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.REW_CUSTOM, null);
                SessionData.set(uuid, Key.REW_CUSTOM_DATA, null);
                SessionData.set(uuid, Key.REW_CUSTOM_DATA_TEMP, null);
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("rewCustomCleared"));
                new QuestRewardsPrompt(uuid).start();
                return;
            }
            sender.sendMessage(ChatColor.RED + BukkitLang.get("rewCustomNotFound"));
            new QuestCustomRewardModulePrompt(uuid).start();
        }
    }

    public class QuestCustomRewardsPrompt extends QuestsEditorStringPrompt {

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
            return BukkitLang.get("customRewardsTitle");
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("rewCustomRewardPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            if (!(BukkitMiscUtil.getEntity(uuid) instanceof Player) || !plugin.getConfigSettings().canClickablePrompts()) {
                final StringBuilder text = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle() + "\n");
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
                return text.toString() + ChatColor.YELLOW + getQueryText();
            }
            final TextComponent component = new TextComponent(getTitle() + "\n");
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
            component.addExtra(ChatColor.YELLOW + getQueryText());
            BukkitMiscUtil.getEntity(uuid).spigot().sendMessage(component);
            return "";
        }

        @SuppressWarnings("unchecked")
        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
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
                        // The custom reward may already have been added, so let's check that
                        final LinkedList<String> list = (LinkedList<String>) SessionData.get(uuid, Key.REW_CUSTOM);
                        final LinkedList<Map<String, Object>> dataMapList
                                = (LinkedList<Map<String, Object>>) SessionData.get(uuid, Key.REW_CUSTOM_DATA);
                        if (list != null && dataMapList != null && !list.contains(found.getName())) {
                            // Hasn't been added yet, so let's do it
                            list.add(found.getName());
                            dataMapList.add(found.getData());
                            SessionData.set(uuid, Key.REW_CUSTOM, list);
                            SessionData.set(uuid, Key.REW_CUSTOM_DATA, dataMapList);
                        } else {
                            // Already added, so inform user
                            sender.sendMessage(ChatColor.RED + BukkitLang.get("rewCustomAlreadyAdded"));
                            new QuestCustomRewardsPrompt(moduleName, uuid).start();
                            return;
                        }
                    } else {
                        // The custom reward hasn't been added yet, so let's do it
                        final LinkedList<Map<String, Object>> dataMapList = new LinkedList<>();
                        dataMapList.add(found.getData());
                        final LinkedList<String> list = new LinkedList<>();
                        list.add(found.getName());
                        SessionData.set(uuid, Key.REW_CUSTOM, list);
                        SessionData.set(uuid, Key.REW_CUSTOM_DATA, dataMapList);
                    }
                    // Send user to the custom data prompt if there is any needed
                    if (!found.getData().isEmpty()) {
                        SessionData.set(uuid, Key.REW_CUSTOM_DATA_DESCRIPTIONS, found.getDescriptions());
                        new QuestRewardCustomDataListPrompt(uuid).start();
                        return;
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("rewCustomNotFound"));
                    new QuestCustomRewardsPrompt(moduleName, uuid).start();
                    return;
                }
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.REW_CUSTOM, null);
                SessionData.set(uuid, Key.REW_CUSTOM_DATA, null);
                SessionData.set(uuid, Key.REW_CUSTOM_DATA_TEMP, null);
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("rewCustomCleared"));
            }
            new QuestRewardsPrompt(uuid).start();
        }
    }

    private class QuestRewardCustomDataListPrompt extends QuestsEditorStringPrompt {

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
            final StringBuilder text = new StringBuilder(ChatColor.GOLD + "- ");
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
            new QuestRewardsPrompt(uuid).start();
        }
    }

    private class QuestRewardCustomDataPrompt extends QuestsEditorStringPrompt {

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
                String lang = BukkitLang.get("stageEditorCustomDataPrompt");
                lang = lang.replace("<data>", temp);
                text += ChatColor.YELLOW + lang;
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
