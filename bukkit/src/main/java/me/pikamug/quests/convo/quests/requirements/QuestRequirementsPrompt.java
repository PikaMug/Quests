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
import me.pikamug.quests.convo.quests.QuestsEditorIntegerPrompt;
import me.pikamug.quests.convo.quests.QuestsEditorStringPrompt;
import me.pikamug.quests.events.editor.quests.BukkitQuestsEditorPostOpenNumericPromptEvent;
import me.pikamug.quests.events.editor.quests.BukkitQuestsEditorPostOpenStringPromptEvent;
import me.pikamug.quests.module.CustomRequirement;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.util.BukkitItemUtil;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.quests.util.BukkitMiscUtil;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.SessionData;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
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

public class QuestRequirementsPrompt extends QuestsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final BukkitQuestsPlugin plugin;
    private final String classPrefix;
    private boolean hasRequirement = false;
    private final int size = 12;
    
    public QuestRequirementsPrompt(final @NotNull UUID uuid) {
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
        return BukkitLang.get("requirementsTitle").replace("<quest>", (String) Objects
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
            if (SessionData.get(uuid, Key.REQ_FAIL_MESSAGE) == null) {
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
    public String getSelectionText(final int number) {
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
    public String getAdditionalText(final int number) {
        switch (number) {
        case 1:
            if (plugin.getDependencies().getVaultEconomy() != null) {
                final Integer moneyReq = (Integer) SessionData.get(uuid, Key.REQ_MONEY);
                if (moneyReq == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + moneyReq + " " 
                            + plugin.getDependencies().getVaultEconomy().format(moneyReq) + ChatColor.GRAY + ")";
                }
            } else {
                return ChatColor.GRAY + "(" + BukkitLang.get("notInstalled") + ")";
            }
        case 2:
            if (SessionData.get(uuid, Key.REQ_QUEST_POINTS) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + SessionData.get(uuid, Key.REQ_QUEST_POINTS) + " "
                        + BukkitLang.get("questPoints") + ChatColor.GRAY + ")";
            }
        case 3:
            if (SessionData.get(uuid, Key.REQ_ITEMS) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<ItemStack> items = (LinkedList<ItemStack>) SessionData.get(uuid, Key.REQ_ITEMS);
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
            if (SessionData.get(uuid, Key.REQ_EXP) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + SessionData.get(uuid, Key.REQ_EXP) + " "
                        + BukkitLang.get("points") + ChatColor.GRAY + ")";
            }
        case 5:
            if (SessionData.get(uuid, Key.REQ_PERMISSION) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final List<String> perms = (List<String>) SessionData.get(uuid, Key.REQ_PERMISSION);
                if (perms != null) {
                    for (final String s : perms) {
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s);
                    }
                }
                return text.toString();
            }
        case 6:
            if (SessionData.get(uuid, Key.REQ_QUEST) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final List<String> questReq = (List<String>) SessionData.get(uuid, Key.REQ_QUEST);
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
            if (SessionData.get(uuid, Key.REQ_QUEST_BLOCK) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final List<String> questBlockReq = (List<String>) SessionData.get(uuid, Key.REQ_QUEST_BLOCK);
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
                if (SessionData.get(uuid, Key.REQ_MCMMO_SKILLS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> skills = (List<String>) SessionData.get(uuid, Key.REQ_MCMMO_SKILLS);
                    final List<Integer> amounts = (List<Integer>) SessionData.get(uuid, Key.REQ_MCMMO_SKILL_AMOUNTS);
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
                if (SessionData.get(uuid, Key.REQ_HEROES_PRIMARY_CLASS) == null
                        && SessionData.get(uuid, Key.REQ_HEROES_SECONDARY_CLASS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")\n";
                } else {
                    String text = "\n";
                    if (SessionData.get(uuid, Key.REQ_HEROES_PRIMARY_CLASS) != null) {
                        text += ChatColor.AQUA + "    " + BukkitLang.get("reqHeroesPrimaryDisplay") + " "
                                + ChatColor.BLUE + SessionData.get(uuid, Key.REQ_HEROES_PRIMARY_CLASS);
                    }
                    if (SessionData.get(uuid, Key.REQ_HEROES_SECONDARY_CLASS) != null) {
                        text += ChatColor.AQUA + "    " + BukkitLang.get("reqHeroesSecondaryDisplay") + " "
                                + ChatColor.BLUE + SessionData.get(uuid, Key.REQ_HEROES_SECONDARY_CLASS);
                    }
                    return text;
                }
            } else {
                return ChatColor.GRAY + "(" + BukkitLang.get("notInstalled") + ")";
            }
        case 10:
           if (SessionData.get(uuid, Key.REQ_CUSTOM) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
           } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> customReq = (LinkedList<String>) SessionData.get(uuid, Key.REQ_CUSTOM);
                if (customReq != null) {
                    for (final String s : customReq) {
                        text.append("\n").append(ChatColor.LIGHT_PURPLE).append("     - ").append(s);
                    }
                }
                return text.toString();
           }
        case 11:
            if (SessionData.get(uuid, Key.REQ_FAIL_MESSAGE) == null) {
                if (!hasRequirement) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("stageEditorOptional") + ")";
                } else {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                }
            } else {
                final StringBuilder text = new StringBuilder();
                final List<String> overrides = (List<String>) SessionData.get(uuid, Key.REQ_FAIL_MESSAGE);
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
    public @NotNull String getPromptText() {
        final String input = (String) SessionData.get(uuid, classPrefix + "-override");
        if (input != null && !input.equalsIgnoreCase(BukkitLang.get("cancel"))) {
            if (input.equalsIgnoreCase(BukkitLang.get("clear"))) {
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

        final BukkitQuestsEditorPostOpenNumericPromptEvent event
                = new BukkitQuestsEditorPostOpenNumericPromptEvent(uuid, this);
        plugin.getServer().getPluginManager().callEvent(event);
        
        final StringBuilder text = new StringBuilder(ChatColor.DARK_AQUA + "- "  + getTitle()
                .replace((String) Objects.requireNonNull(SessionData.get(uuid, Key.Q_NAME)), ChatColor.AQUA
                + (String) SessionData.get(uuid, Key.Q_NAME) + ChatColor.DARK_AQUA) + " -");
        for (int i = 1; i <= size; i++) {
            text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                    .append(ChatColor.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                    .append(getAdditionalText(i));
        }
        return text.toString();
    }

    @Override
    public void acceptInput(final Number input) {
        switch (input.intValue()) {
        case 1:
            if (plugin.getDependencies().getVaultEconomy() != null) {
                new QuestRequirementsMoneyPrompt(uuid).start();
            } else {
                new QuestRequirementsPrompt(uuid).start();
            }
        case 2:
            new QuestRequirementsQuestPointsPrompt(uuid).start();
        case 3:
            new QuestRequirementsItemListPrompt(uuid).start();
        case 4:
            new QuestRequirementsExperiencePrompt(uuid).start();
        case 5:
            new QuestRequirementsPermissionsPrompt(uuid).start();
        case 6:
            new QuestRequirementsQuestListPrompt(uuid, true);
        case 7:
            new QuestRequirementsQuestListPrompt(uuid, false);
        case 8:
            if (plugin.getDependencies().getMcmmoClassic() != null) {
                new QuestRequirementsMcMMOListPrompt(uuid).start();
            } else {
                new QuestRequirementsPrompt(uuid).start();
            }
        case 9:
            if (plugin.getDependencies().getHeroes() != null) {
                new QuestRequirementsHeroesListPrompt(uuid).start();
            } else {
                new QuestRequirementsPrompt(uuid).start();
            }
        case 10:
            new QuestCustomRequirementModulePrompt(uuid).start();
        case 11:
            if (hasRequirement) {
                new OverridePrompt.Builder()
                        .sender(uuid)
                        .source(this)
                        .promptText(BukkitLang.get("overrideCreateEnter"))
                        .build();
            } else {
                Bukkit.getEntity(uuid).sendMessage(ChatColor.RED + BukkitLang.get("invalidOption"));
                new QuestRequirementsPrompt(uuid).start();
            }
        case 12:
            plugin.getQuestFactory().returnToMenu(uuid);
        default:
            new QuestRequirementsPrompt(uuid).start();
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

    public class QuestRequirementsMoneyPrompt extends QuestsEditorStringPrompt {
        
        public QuestRequirementsMoneyPrompt(final @NotNull UUID uuid) {
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
                text = text.replace("<money>",  plugin.getDependencies().getVaultEconomy().currencyNamePlural());
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
                        SessionData.set(uuid, Key.REQ_MONEY, i);
                    } else {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("inputPosNum"));
                        new QuestRequirementsMoneyPrompt(uuid).start();
                    }
                } catch (final NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                            .replace("<input>", input));
                    new QuestRequirementsMoneyPrompt(uuid).start();
                }
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.REQ_MONEY, null);
                new QuestRequirementsPrompt(uuid).start();
            }
            new QuestRequirementsPrompt(uuid).start();
        }
    }

    public class QuestRequirementsQuestPointsPrompt extends QuestsEditorStringPrompt {
        
        public QuestRequirementsQuestPointsPrompt(final @NotNull UUID uuid) {
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
                        SessionData.set(uuid, Key.REQ_QUEST_POINTS, i);
                    } else {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("inputPosNum"));
                        new QuestRequirementsQuestPointsPrompt(uuid).start();
                    }
                } catch (final NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                            .replace("<input>", input));
                    new QuestRequirementsQuestPointsPrompt(uuid).start();
                }
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.REQ_QUEST_POINTS, null);
                new QuestRequirementsPrompt(uuid).start();
            }
            new QuestRequirementsPrompt(uuid).start();
        }
    }

    public class QuestRequirementsItemListPrompt extends QuestsEditorIntegerPrompt {
        
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
            return BukkitLang.get("itemRequirementsTitle");
        }
        
        @Override
        public ChatColor getNumberColor(final int number) {
            switch (number) {
            case 1:
                return ChatColor.BLUE;
            case 2:
                if (SessionData.get(uuid, Key.REQ_ITEMS) == null) {
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
                return ChatColor.YELLOW + BukkitLang.get("stageEditorDeliveryAddItem");
            case 2:
                if (SessionData.get(uuid, Key.REQ_ITEMS) == null) {
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

        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final int number) {
            switch (number) {
            case 1:
                if (SessionData.get(uuid, Key.REQ_ITEMS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<ItemStack> reqItems = (List<ItemStack>) SessionData.get(uuid, Key.REQ_ITEMS);
                    if (reqItems != null) {
                        for (final ItemStack is : reqItems) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ")
                                    .append(BukkitItemUtil.getDisplayString(is));
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (SessionData.get(uuid, Key.REQ_ITEMS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    if (SessionData.get(uuid, Key.REQ_ITEMS_REMOVE) == null) {
                        return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                    } else {
                        final StringBuilder text = new StringBuilder();
                        final List<Boolean> reqItemsRemove
                                = (List<Boolean>) SessionData.get(uuid, Key.REQ_ITEMS_REMOVE);
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
                ItemStackPrompt.clearSessionData(uuid);
            }
            
            final BukkitQuestsEditorPostOpenNumericPromptEvent event
                    = new BukkitQuestsEditorPostOpenNumericPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.AQUA + getTitle() + "\n");
            for (int i = 1; i <= size; i++) {
                text.append(getNumberColor(i)).append(ChatColor.BOLD).append(i).append(ChatColor.RESET)
                        .append(" - ").append(getSelectionText(i)).append(" ")
                        .append(getAdditionalText(i)).append("\n");
            }
            return text.toString();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final Number input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            switch (input.intValue()) {
            case 1:
                new ItemStackPrompt(uuid, QuestRequirementsItemListPrompt.this).start();
            case 2:
                if (SessionData.get(uuid, Key.REQ_ITEMS) == null) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("reqMustAddItem"));
                    new QuestRequirementsItemListPrompt(uuid).start();
                } else {
                    new QuestRemoveItemsPrompt(uuid).start();
                }
            case 3:
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("reqItemCleared"));
                SessionData.set(uuid, Key.REQ_ITEMS, null);
                SessionData.set(uuid, Key.REQ_ITEMS_REMOVE, null);
                new QuestRequirementsItemListPrompt(uuid).start();
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
            default:
                new QuestRequirementsPrompt(uuid).start();
            }
        }
    }

    public class QuestRemoveItemsPrompt extends QuestsEditorStringPrompt {
        
        public QuestRemoveItemsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("reqRemoveItemsPrompt");
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
                final LinkedList<Boolean> booleans = new LinkedList<>();
                for (final String s : args) {
                    if (input.startsWith("t") || s.equalsIgnoreCase(BukkitLang.get("true"))
                            || s.equalsIgnoreCase(BukkitLang.get("yesWord"))) {
                        booleans.add(true);
                    } else if (input.startsWith("f") || s.equalsIgnoreCase(BukkitLang.get("false"))
                            || s.equalsIgnoreCase(BukkitLang.get("noWord"))) {
                        booleans.add(false);
                    } else {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("itemCreateInvalidInput"));
                        new QuestRemoveItemsPrompt(uuid).start();
                    }
                }
                SessionData.set(uuid, Key.REQ_ITEMS_REMOVE, booleans);
            }
            new QuestRequirementsItemListPrompt(uuid).start();
        }
    }

    public class QuestRequirementsExperiencePrompt extends QuestsEditorStringPrompt {

        public QuestRequirementsExperiencePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("reqExperiencePrompt");
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
                        SessionData.set(uuid, Key.REQ_EXP, i);
                    } else {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("inputPosNum"));
                        new QuestRequirementsExperiencePrompt(uuid).start();
                    }
                } catch (final NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                            .replace("<input>", input));
                    new QuestRequirementsExperiencePrompt(uuid).start();
                }
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.REQ_EXP, null);
                new QuestRequirementsPrompt(uuid).start();
            }
            new QuestRequirementsPrompt(uuid).start();
        }
    }

    public class QuestRequirementsPermissionsPrompt extends QuestsEditorStringPrompt {
        
        public QuestRequirementsPermissionsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("reqPermissionsPrompt");
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
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel")) && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                final String[] args = input.split(" ");
                final LinkedList<String> permissions = new LinkedList<>(Arrays.asList(args));
                SessionData.set(uuid, Key.REQ_PERMISSION, permissions);
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.REQ_PERMISSION, null);
            }
            new QuestRequirementsPrompt(uuid).start();
        }
    }

    public class QuestRequirementsQuestListPrompt extends QuestsEditorStringPrompt {

        private final boolean isRequiredQuest;

        public QuestRequirementsQuestListPrompt(final @NotNull UUID uuid, final boolean isRequired) {
            super(uuid);
            this.isRequiredQuest = isRequired;
        }

        @Override
        public String getTitle() {
            return BukkitLang.get("reqQuestListTitle");
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("reqQuestPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final List<String> names = plugin.getLoadedQuests().stream().map(Quest::getName)
                    .collect(Collectors.toList());
            return sendClickableMenu(getTitle(), names, getQueryText(), plugin.getQuester(uuid));
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel")) && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                final String[] args = input.split(BukkitLang.get("charSemi"));
                final LinkedList<String> questIds = new LinkedList<>();
                for (String s : args) {
                    s = s.trim();
                    if (plugin.getQuest(s) == null) {
                        String text = BukkitLang.get("reqNotAQuestName");
                        text = text.replace("<quest>", s);
                        sender.sendMessage(text);
                        new QuestRequirementsQuestListPrompt(uuid, isRequiredQuest);
                    }
                    if (questIds.contains(plugin.getQuest(s).getId())) {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("listDuplicate"));
                        new QuestRequirementsQuestListPrompt(uuid, isRequiredQuest);
                    }
                    questIds.add(plugin.getQuest(s).getId());
                }
                if (isRequiredQuest) {
                    SessionData.set(uuid, Key.REQ_QUEST, questIds);
                } else {
                    SessionData.set(uuid, Key.REQ_QUEST_BLOCK, questIds);
                }
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                if (isRequiredQuest) {
                    SessionData.set(uuid, Key.REQ_QUEST, null);
                } else {
                    SessionData.set(uuid, Key.REQ_QUEST_BLOCK, null);
                }
            }
            new QuestRequirementsPrompt(uuid).start();
        }
    }

    public class QuestRequirementsMcMMOListPrompt extends QuestsEditorIntegerPrompt {

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
            return BukkitLang.get("mcMMORequirementsTitle");
        }
        
        @Override
        public ChatColor getNumberColor(final int number) {
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
        public String getSelectionText(final int number) {
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
        public String getAdditionalText(final int number) {
            switch(number) {
            case 1:
                if (SessionData.get(uuid, Key.REQ_MCMMO_SKILLS) == null) {
                    return ChatColor.GRAY + " (" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final LinkedList<String> skills = (LinkedList<String>) SessionData.get(uuid, Key.REQ_MCMMO_SKILLS);
                    if (skills != null) {
                        for (final String skill : skills) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA)
                                    .append(skill);
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (SessionData.get(uuid, Key.REQ_MCMMO_SKILL_AMOUNTS) == null) {
                    return ChatColor.GRAY + " (" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final LinkedList<Integer> skillAmounts
                            = (LinkedList<Integer>) SessionData.get(uuid, Key.REQ_MCMMO_SKILL_AMOUNTS);
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
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenNumericPromptEvent event
                    = new BukkitQuestsEditorPostOpenNumericPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder text = new StringBuilder(ChatColor.AQUA + getTitle() + "\n");
            for (int i = 1; i <= size; i++) {
                text.append(getNumberColor(i)).append(ChatColor.BOLD).append(i).append(ChatColor.RESET).append(" - ").append(getSelectionText(i)).append(" ").append(getAdditionalText(i)).append("\n");
            }
            return text.toString();
        }
        
        @Override
        public void acceptInput(final Number input) {
            switch(input.intValue()) {
            case 1:
                new QuestMcMMOSkillsPrompt(uuid).start();
            case 2:
                new QuestMcMMOAmountsPrompt(uuid).start();
            case 3:
                new QuestRequirementsPrompt(uuid).start();
            default:
                new QuestRequirementsMcMMOListPrompt(uuid).start();
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
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel")) && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                final LinkedList<String> skills = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    final String formatted = BukkitMiscUtil.getCapitalized(s);
                    if (plugin.getDependencies().getMcMMOSkill(formatted) != null) {
                        skills.add(formatted);
                    } else if (skills.contains(formatted)) {
                        sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("listDuplicate"));
                        new QuestMcMMOSkillsPrompt(uuid).start();
                    } else {
                        String text = BukkitLang.get("reqMcMMOError");
                        text = text.replace("<input>", s);
                        sender.sendMessage(ChatColor.YELLOW + text);
                        new QuestMcMMOSkillsPrompt(uuid).start();
                    }
                }
                SessionData.set(uuid, Key.REQ_MCMMO_SKILLS, skills);
                new QuestRequirementsMcMMOListPrompt(uuid).start();
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("reqMcMMOCleared"));
                SessionData.set(uuid, Key.REQ_MCMMO_SKILLS, null);
                new QuestRequirementsMcMMOListPrompt(uuid).start();
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                new QuestRequirementsMcMMOListPrompt(uuid).start();
            }
            new QuestMcMMOSkillsPrompt(uuid).start();
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
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel")) && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                final LinkedList<Integer> amounts = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    try {
                        final int i = Integer.parseInt(s);
                        amounts.add(i);
                    } catch (final NumberFormatException nfe) {
                        String text = BukkitLang.get("reqNotANumber");
                        text = text.replace("<input>", s);
                        sender.sendMessage(ChatColor.YELLOW + text);
                        new QuestMcMMOAmountsPrompt(uuid).start();
                    }
                }
                SessionData.set(uuid, Key.REQ_MCMMO_SKILL_AMOUNTS, amounts);
                new QuestRequirementsMcMMOListPrompt(uuid).start();
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("reqMcMMOAmountsCleared"));
                SessionData.set(uuid, Key.REQ_MCMMO_SKILL_AMOUNTS, null);
                new QuestRequirementsMcMMOListPrompt(uuid).start();
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                new QuestRequirementsMcMMOListPrompt(uuid).start();
            }
            new QuestMcMMOAmountsPrompt(uuid).start();
        }
    }

    public class QuestRequirementsHeroesListPrompt extends QuestsEditorIntegerPrompt {

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
            return BukkitLang.get("heroesRequirementsTitle");
        }
        
        @Override
        public ChatColor getNumberColor(final int number) {
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
        public String getSelectionText(final int number) {
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
        public String getAdditionalText(final int number) {
            switch(number) {
            case 1:
                if (SessionData.get(uuid, Key.REQ_HEROES_PRIMARY_CLASS) == null) {
                    return ChatColor.GRAY + " (" + BukkitLang.get("noneSet") + ")";
                } else {
                    return "(" + ChatColor.AQUA + SessionData.get(uuid, Key.REQ_HEROES_PRIMARY_CLASS) + ChatColor.GREEN
                            + ")\n";
                }
            case 2:
                if (SessionData.get(uuid, Key.REQ_HEROES_SECONDARY_CLASS) == null) {
                    return ChatColor.GRAY + " (" + BukkitLang.get("noneSet") + ")";
                } else {
                    return "(" + ChatColor.AQUA + SessionData.get(uuid, Key.REQ_HEROES_SECONDARY_CLASS)
                            + ChatColor.GREEN + ")\n";
                }
            case 3:
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

            final StringBuilder text = new StringBuilder(ChatColor.AQUA + getTitle() + "\n");
            for (int i = 1; i <= size; i++) {
                text.append(getNumberColor(i)).append(ChatColor.BOLD).append(i).append(ChatColor.RESET)
                        .append(" - ").append(getSelectionText(i)).append(" ")
                        .append(getAdditionalText(i)).append("\n");
            }
            return text.toString();
        }
        @Override
        public void acceptInput(final Number input) {
            switch(input.intValue()) {
            case 1:
                new QuestHeroesPrimaryPrompt(uuid).start();
            case 2:
                new QuestHeroesSecondaryPrompt(uuid).start();
            case 3:
                new QuestRequirementsPrompt(uuid).start();
            default:
                new QuestRequirementsHeroesListPrompt(uuid).start();
            }
        }
    }

    public class QuestHeroesPrimaryPrompt extends QuestsEditorStringPrompt {
        
        public QuestHeroesPrimaryPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return BukkitLang.get("heroesPrimaryTitle");
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("reqHeroesPrimaryPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle() + "\n");
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
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdClear")) && !input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final HeroClass hc = plugin.getDependencies().getHeroes().getClassManager().getClass(input);
                if (hc != null) {
                    if (hc.isPrimary()) {
                        SessionData.set(uuid, Key.REQ_HEROES_PRIMARY_CLASS, hc.getName());
                        new QuestRequirementsHeroesListPrompt(uuid).start();
                    } else {
                        String text = BukkitLang.get("reqHeroesNotPrimary");
                        text = text.replace("<class>", ChatColor.LIGHT_PURPLE + hc.getName() + ChatColor.RED);
                        sender.sendMessage(ChatColor.RED + text);
                        new QuestHeroesPrimaryPrompt(uuid).start();
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("reqHeroesClassNotFound"));
                    new QuestHeroesPrimaryPrompt(uuid).start();
                }
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.REQ_HEROES_PRIMARY_CLASS, null);
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("reqHeroesPrimaryCleared"));
                new QuestRequirementsHeroesListPrompt(uuid).start();
            } else {
                new QuestRequirementsHeroesListPrompt(uuid).start();
            }
        }
    }

    public class QuestHeroesSecondaryPrompt extends QuestsEditorStringPrompt {
        
        public QuestHeroesSecondaryPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return BukkitLang.get("heroesSecondaryTitle");
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("reqHeroesSecondaryPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle() + "\n");
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
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdClear")) && !input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final HeroClass hc = plugin.getDependencies().getHeroes().getClassManager().getClass(input);
                if (hc != null) {
                    if (hc.isSecondary()) {
                        SessionData.set(uuid, Key.REQ_HEROES_SECONDARY_CLASS, hc.getName());
                        new QuestRequirementsHeroesListPrompt(uuid).start();
                    } else {
                        String text = BukkitLang.get("reqHeroesNotSecondary");
                        text = text.replace("<class>", ChatColor.LIGHT_PURPLE + hc.getName() + ChatColor.RED);
                        sender.sendMessage(ChatColor.RED + text);
                        new QuestHeroesSecondaryPrompt(uuid).start();
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("reqHeroesClassNotFound"));
                    new QuestHeroesSecondaryPrompt(uuid).start();
                }
            } else if (input.equalsIgnoreCase(BukkitLang.get("clear"))) {
                SessionData.set(uuid, Key.REQ_HEROES_SECONDARY_CLASS, null);
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("reqHeroesSecondaryCleared"));
                new QuestRequirementsHeroesListPrompt(uuid).start();
            } else {
                new QuestRequirementsHeroesListPrompt(uuid).start();
            }
        }
    }

    public class QuestCustomRequirementModulePrompt extends QuestsEditorStringPrompt {

        public QuestCustomRequirementModulePrompt(final @NotNull UUID uuid) {
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

            if (!(Bukkit.getEntity(uuid) instanceof Player) || !plugin.getConfigSettings().canClickablePrompts()) {
                final StringBuilder text = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle() + "\n");
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
                return text.toString() + ChatColor.YELLOW + getQueryText();
            }
            final TextComponent component = new TextComponent(getTitle() + "\n");
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
            component.addExtra(ChatColor.YELLOW + getQueryText());
            Bukkit.getEntity(uuid).spigot().sendMessage(component);
            return "";
        }

        @Override
        public void acceptInput(@Nullable final String input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
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
                    new QuestCustomRequirementsPrompt(found, uuid).start();
                }
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                new QuestRequirementsPrompt(uuid).start();
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.REQ_CUSTOM, null);
                SessionData.set(uuid, Key.REQ_CUSTOM_DATA, null);
                SessionData.set(uuid, Key.REQ_CUSTOM_DATA_TEMP, null);
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("reqCustomCleared"));
                new QuestRequirementsPrompt(uuid).start();
            }
            sender.sendMessage(ChatColor.RED + BukkitLang.get("reqCustomNotFound"));
            new QuestCustomRequirementModulePrompt(uuid).start();
        }
    }

    public class QuestCustomRequirementsPrompt extends QuestsEditorStringPrompt {

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
            return BukkitLang.get("customRequirementsTitle");
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("reqCustomPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            if (!(Bukkit.getEntity(uuid) instanceof Player) || !plugin.getConfigSettings().canClickablePrompts()) {
                final StringBuilder text = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle() + "\n");
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
                return text.toString() + ChatColor.YELLOW + getQueryText();
            }
            final TextComponent component = new TextComponent(getTitle() + "\n");
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
            component.addExtra(ChatColor.YELLOW + getQueryText());
            Bukkit.getEntity(uuid).spigot().sendMessage(component);
            return "";
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
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
                    if (SessionData.get(uuid, Key.REQ_CUSTOM) != null) {
                        // The custom requirement may already have been added, so let's check that
                        final LinkedList<String> list = (LinkedList<String>) SessionData.get(uuid, Key.REQ_CUSTOM);
                        final LinkedList<Map<String, Object>> dataMapList
                                = (LinkedList<Map<String, Object>>) SessionData.get(uuid, Key.REQ_CUSTOM_DATA);
                        if (dataMapList != null && list != null && !list.contains(found.getName())) {
                            // Hasn't been added yet, so let's do it
                            list.add(found.getName());
                            dataMapList.add(found.getData());
                            SessionData.set(uuid, Key.REQ_CUSTOM, list);
                            SessionData.set(uuid, Key.REQ_CUSTOM_DATA, dataMapList);
                        } else {
                            // Already added, so inform user
                            sender.sendMessage(ChatColor.RED + BukkitLang.get("reqCustomAlreadyAdded"));
                            new QuestCustomRequirementsPrompt(moduleName, uuid).start();
                        }
                    } else {
                        // The custom requirement hasn't been added yet, so let's do it
                        final LinkedList<Map<String, Object>> dataMapList = new LinkedList<>();
                        dataMapList.add(found.getData());
                        final LinkedList<String> list = new LinkedList<>();
                        list.add(found.getName());
                        SessionData.set(uuid, Key.REQ_CUSTOM, list);
                        SessionData.set(uuid, Key.REQ_CUSTOM_DATA, dataMapList);
                    }
                    // Send user to the custom data prompt if there is any needed
                    if (!found.getData().isEmpty()) {
                        SessionData.set(uuid, Key.REQ_CUSTOM_DATA_DESCRIPTIONS, found.getDescriptions());
                        new QuestRequirementCustomDataListPrompt(uuid).start();
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("reqCustomNotFound"));
                    new QuestCustomRequirementsPrompt(moduleName, uuid).start();
                }
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.REQ_CUSTOM, null);
                SessionData.set(uuid, Key.REQ_CUSTOM_DATA, null);
                SessionData.set(uuid, Key.REQ_CUSTOM_DATA_TEMP, null);
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("reqCustomCleared"));
            }
            new QuestRequirementsPrompt(uuid).start();
        }
    }

    private class QuestRequirementCustomDataListPrompt extends QuestsEditorStringPrompt {

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
            final StringBuilder text = new StringBuilder(ChatColor.GOLD + "- ");
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
                    = (LinkedList<Map<String, Object>>) SessionData.get(uuid, Key.REQ_CUSTOM_DATA);
            if (dataMapList != null) {
                final Map<String, Object> dataMap = dataMapList.getLast();
                int numInput = 0;
                try {
                    numInput = Integer.parseInt(input);
                } catch (final NumberFormatException nfe) {
                    new QuestRequirementCustomDataListPrompt(uuid).start();
                }
                if (numInput < 1 || numInput > dataMap.size() + 1) {
                    new QuestRequirementCustomDataListPrompt(uuid).start();
                }
                if (numInput < dataMap.size() + 1) {
                    final LinkedList<String> dataMapKeys = new LinkedList<>(dataMap.keySet());
                    Collections.sort(dataMapKeys);
                    final String selectedKey = dataMapKeys.get(numInput - 1);
                    SessionData.set(uuid, Key.REQ_CUSTOM_DATA_TEMP, selectedKey);
                    new QuestRequirementCustomDataPrompt(uuid).start();
                } else {
                    if (dataMap.containsValue(null)) {
                        new QuestRequirementCustomDataListPrompt(uuid).start();
                    } else {
                        SessionData.set(uuid, Key.REQ_CUSTOM_DATA_DESCRIPTIONS, null);
                    }
                }
            }
            new QuestRequirementsPrompt(uuid).start();
        }
    }

    private class QuestRequirementCustomDataPrompt extends QuestsEditorStringPrompt {

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
                    text += ChatColor.GOLD + descriptions.get(temp) + "\n";
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
