/*******************************************************************************************************
 * Continued by PikaMug (formerly HappyPikachu) with permission from _Blackvein_. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

package me.blackvein.quests.convo.quests.prompts;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.FixedSetPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.inventory.ItemStack;

import com.codisimus.plugins.phatloots.PhatLoot;
import com.codisimus.plugins.phatloots.PhatLootsAPI;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.herocraftonline.heroes.characters.classes.HeroClass;

import me.blackvein.quests.CustomReward;
import me.blackvein.quests.Quests;
import me.blackvein.quests.convo.quests.QuestsEditorNumericPrompt;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenNumericPromptEvent;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;

public class RewardsPrompt extends QuestsEditorNumericPrompt {

    private final Quests plugin;
    private final String classPrefix;
    private boolean hasReward = false;
    private final int size = 12;

    public RewardsPrompt(ConversationContext context) {
        super(context);
        this.plugin = (Quests)context.getPlugin();
        this.classPrefix = getClass().getSimpleName();
    }
    
    public int getSize() {
        return size;
    }
    
    public String getTitle(ConversationContext context) {
        return Lang.get("rewardsTitle").replace("<quest>", (String) context.getSessionData(CK.Q_NAME));
    }
    
    public ChatColor getNumberColor(ConversationContext context, int number) {
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
            if (plugin.getDependencies().getPhatLoots() != null) {
                return ChatColor.BLUE;
            } else {
                return ChatColor.GRAY;
            }
        case 10:
            return ChatColor.BLUE;
        case 11:
            if (context.getSessionData(CK.REW_DETAILS_OVERRIDE) == null) {
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
    
    public String getSelectionText(ConversationContext context, int number) {
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
            if (plugin.getDependencies().getPhatLoots() != null) {
                return ChatColor.YELLOW + Lang.get("rewSetPhat");
            } else {
                return ChatColor.GRAY + Lang.get("rewSetPhat");
            }
        case 10:
            return ChatColor.DARK_PURPLE + Lang.get("rewSetCustom");
        case 11:
            if (!hasReward) {
                return ChatColor.GRAY + Lang.get("overrideCreateSet");
            } else {
                return ChatColor.YELLOW + Lang.get("overrideCreateSet");
            }
        case 12:
            return ChatColor.YELLOW + Lang.get("done");
        default:
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    public String getAdditionalText(ConversationContext context, int number) {
        switch (number) {
        case 1:
            if (plugin.getDependencies().getVaultEconomy() != null) {
                if (context.getSessionData(CK.REW_MONEY) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    int moneyRew = (Integer) context.getSessionData(CK.REW_MONEY);
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + moneyRew + " " 
                            + (moneyRew > 1 ? plugin.getDependencies().getCurrency(true) 
                            : plugin.getDependencies().getCurrency(false)) + ChatColor.GRAY + ")";
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
                String text = "\n";
                LinkedList<ItemStack> items = (LinkedList<ItemStack>) context.getSessionData(CK.REW_ITEMS);
                for (int i = 0; i < items.size(); i++) {
                    text += ChatColor.GRAY + "     - " + ChatColor.BLUE + ItemUtil.getName(items.get(i)) 
                            + ChatColor.GRAY + " x " + ChatColor.AQUA + items.get(i).getAmount() + "\n";
                }
                return text;
            }
        case 4:
            if (context.getSessionData(CK.REW_EXP) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + context.getSessionData(CK.REW_EXP) + " " 
                        + Lang.get("points") + ChatColor.DARK_GRAY + ")";
            }
        case 5:
            if (context.getSessionData(CK.REW_COMMAND) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                String text = "\n";
                List<String> commands = (List<String>) context.getSessionData(CK.REW_COMMAND);
                List<String> overrides = (List<String>) context.getSessionData(CK.REW_COMMAND_OVERRIDE_DISPLAY);
                int index = 0;
                for (String cmd : commands) {
                    text += ChatColor.GRAY + "     - " + ChatColor.AQUA + cmd;
                    if (overrides != null) {
                        if (index < overrides.size()) {
                            text += ChatColor.GRAY + " (\"" + ChatColor.AQUA + overrides.get(index) 
                                    + ChatColor.GRAY + "\")";
                        }
                    }
                    text += "\n";
                    index++;
                }
                return text;
            }
        case 6:
            if (context.getSessionData(CK.REW_PERMISSION) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                String text = "\n";
                List<String> permissions = (List<String>) context.getSessionData(CK.REW_PERMISSION);
                List<String> worlds = (List<String>) context.getSessionData(CK.REW_PERMISSION_WORLDS);
                int index = 0;
                for (String perm : permissions) {
                    text += ChatColor.GRAY + "     - " + ChatColor.AQUA + perm;
                    if (worlds != null) {
                        if (index < worlds.size()) {
                            text += ChatColor.GRAY + "[" + ChatColor.DARK_AQUA + worlds.get(index) 
                                    + ChatColor.GRAY + "]";
                        }
                    }
                    text += "\n";
                    index++;
                }
                return text;
            }
        case 7:
            if (plugin.getDependencies().getMcmmoClassic() != null) {
                if (context.getSessionData(CK.REW_MCMMO_SKILLS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    String text = "\n";
                    List<String> skills = (List<String>) context.getSessionData(CK.REW_MCMMO_SKILLS);
                    List<Integer> amounts = (List<Integer>) context.getSessionData(CK.REW_MCMMO_AMOUNTS);
                    for (String skill : skills) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + skill + ChatColor.GRAY + " x " 
                                + ChatColor.DARK_AQUA + amounts.get(skills.indexOf(skill)) + "\n";
                    }
                    return text;
                }
            } else {
                return ChatColor.GRAY + "(" + Lang.get("notInstalled") + ")";
            }
        case 8:
            if (plugin.getDependencies().getHeroes() != null) {
                if (context.getSessionData(CK.REW_HEROES_CLASSES) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    String text = "\n";
                    List<String> heroClasses = (List<String>) context.getSessionData(CK.REW_HEROES_CLASSES);
                    List<Double> amounts = (List<Double>) context.getSessionData(CK.REW_HEROES_AMOUNTS);
                    for (String heroClass : heroClasses) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA 
                                + amounts.get(heroClasses.indexOf(heroClass)) + " " + ChatColor.DARK_AQUA 
                                + heroClass + " " + Lang.get("experience") + "\n";
                    }
                    return text;
                }
            } else {
                return ChatColor.GRAY + "(" + Lang.get("notInstalled") + ")";
            }
        case 9:
            if (plugin.getDependencies().getPhatLoots() != null) {
                if (context.getSessionData(CK.REW_PHAT_LOOTS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    String text = "\n";
                    List<String> phatLoots = (List<String>) context.getSessionData(CK.REW_PHAT_LOOTS);
                    for (String phatLoot : phatLoots) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + phatLoot + "\n";
                    }
                    return text;
                }
            } else {
                return ChatColor.GRAY + "(" + Lang.get("notInstalled") + ")";
            }
        case 10:
            if (context.getSessionData(CK.REW_CUSTOM) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                String text = "\n";
                LinkedList<String> customRews = (LinkedList<String>) context.getSessionData(CK.REW_CUSTOM);
                for (String s : customRews) {
                    text += ChatColor.RESET + "" + ChatColor.DARK_PURPLE + "  - " + ChatColor.LIGHT_PURPLE + s 
                            + "\n";
                }
                return text;
            }
        case 11:
            if (context.getSessionData(CK.REW_DETAILS_OVERRIDE) == null) {
                if (!hasReward) {
                    return ChatColor.GRAY + "(" + Lang.get("stageEditorOptional") + ")";
                } else {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                }
            } else {
                String text = "\n";
                LinkedList<String> overrides = new LinkedList<String>();
                overrides.addAll((List<String>) context.getSessionData(CK.REW_DETAILS_OVERRIDE));
                for (int i = 0; i < overrides.size(); i++) {
                    text += ChatColor.GRAY + "     - " + ChatColor.AQUA + overrides.get(i) + "\n";
                }
                return text;
            }
        case 12:
            return "";
        default:
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public String getPromptText(ConversationContext context) {
        final String input = (String) context.getSessionData(classPrefix + "-override");
        if (input != null && !input.equalsIgnoreCase(Lang.get("cancel"))) {
            if (input.equalsIgnoreCase(Lang.get("clear"))) {
                context.setSessionData(CK.REW_DETAILS_OVERRIDE, null);
            } else {
                LinkedList<String> overrides = new LinkedList<String>();
                if (context.getSessionData(CK.REW_DETAILS_OVERRIDE) != null) {
                    overrides.addAll((List<String>) context.getSessionData(CK.REW_DETAILS_OVERRIDE));
                }
                overrides.add(input);
                context.setSessionData(CK.REW_DETAILS_OVERRIDE, overrides);
                context.setSessionData(classPrefix + "-override", null);
            }
        }
        checkReward(context);
        
        QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
        context.getPlugin().getServer().getPluginManager().callEvent(event);
        
        String text = ChatColor.LIGHT_PURPLE + getTitle(context).replace((String) context
                .getSessionData(CK.Q_NAME), ChatColor.AQUA + (String) context.getSessionData(CK.Q_NAME) 
                + ChatColor.LIGHT_PURPLE) + "\n";
        for (int i = 1; i <= size; i++) {
            text += getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                    + getSelectionText(context, i) + " " + getAdditionalText(context, i) + "\n";
        }
        return text;
    }

    @Override
    protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
        switch (input.intValue()) {
        case 1:
            if (plugin.getDependencies().getVaultEconomy() != null) {
                return new MoneyPrompt();
            } else {
                return new RewardsPrompt(context);
            }
        case 2:
            return new QuestPointsPrompt();
        case 3:
            return new ItemListPrompt();
        case 4:
            return new ExperiencePrompt();
        case 5:
            return new CommandsPrompt();
        case 6:
            return new PermissionsListPrompt(context);
        case 7:
            if (plugin.getDependencies().getMcmmoClassic() != null) {
                return new mcMMOListPrompt();
            } else {
                return new RewardsPrompt(context);
            }
        case 8:
            if (plugin.getDependencies().getHeroes() != null) {
                return new HeroesListPrompt();
            } else {
                return new RewardsPrompt(context);
            }
        case 9:
            if (plugin.getDependencies().getPhatLoots() != null) {
                return new PhatLootsPrompt();
            } else {
                return new RewardsPrompt(context);
            }
        case 10:
            return new CustomRewardsPrompt();
        case 11:
            if (hasReward) {
                return new OverridePrompt.Builder()
                        .source(this)
                        .promptText(Lang.get("overrideCreateEnter"))
                        .build();
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidOption"));
                return new RewardsPrompt(context);
            }
        case 12:
            return plugin.getQuestFactory().returnToMenu(context);
        default:
            return null;
        }
    }
    
    public boolean checkReward(ConversationContext context) {
        if (context.getSessionData(CK.REW_MONEY) != null 
                || context.getSessionData(CK.REW_EXP) != null
                || context.getSessionData(CK.REW_QUEST_POINTS) != null 
                || context.getSessionData(CK.REW_ITEMS) != null
                || context.getSessionData(CK.REW_COMMAND) != null
                || context.getSessionData(CK.REW_PERMISSION) != null 
                || context.getSessionData(CK.REW_MCMMO_SKILLS) != null 
                || context.getSessionData(CK.REW_HEROES_CLASSES) != null 
                || context.getSessionData(CK.REW_PHAT_LOOTS) != null
                || context.getSessionData(CK.REW_CUSTOM) != null) {
            hasReward = true;
            return true;
        }
        return false;
    }

    private class MoneyPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String text = Lang.get("rewMoneyPrompt");
            if (plugin.getDependencies().getVaultEconomy() != null) {
                text = text.replace("<money>", ChatColor.AQUA + (plugin.getDependencies().getVaultEconomy()
                        .currencyNamePlural().isEmpty() ? Lang.get("money") : plugin.getDependencies().getVaultEconomy()
                        .currencyNamePlural()) + ChatColor.YELLOW);
            } else {
                text = text.replace("<money>", ChatColor.AQUA + Lang.get("money") + ChatColor.YELLOW);
            }
            return ChatColor.YELLOW + text;
        }
        
        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                try {
                    int i = Integer.parseInt(input);
                    if (i > 0) {
                        context.setSessionData(CK.REW_MONEY, i);
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("inputPosNum"));
                        return new MoneyPrompt();
                    }
                } catch (NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqNotANumber")
                            .replace("<input>", input));
                    return new MoneyPrompt();
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.REW_MONEY, null);
                return new RewardsPrompt(context);
            }
            return new RewardsPrompt(context);
        }
    }

    private class ExperiencePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("rewExperiencePrompt");
        }
        
        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                try {
                    int i = Integer.parseInt(input);
                    if (i > 0) {
                        context.setSessionData(CK.REW_EXP, i);
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("inputPosNum"));
                        return new ExperiencePrompt();
                    }
                } catch (NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqNotANumber")
                            .replace("<input>", input));
                    return new ExperiencePrompt();
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.REW_EXP, null);
                return new RewardsPrompt(context);
            }
            return new RewardsPrompt(context);
        }
    }

    private class QuestPointsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("rewQuestPointsPrompt").replace("<points>", Lang.get("questPoints"));
        }
        
        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                try {
                    int i = Integer.parseInt(input);
                    if (i > 0) {
                        context.setSessionData(CK.REW_QUEST_POINTS, i);
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("inputPosNum"));
                        return new QuestPointsPrompt();
                    }
                } catch (NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqNotANumber")
                            .replace("<input>", input));
                    return new QuestPointsPrompt();
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.REW_QUEST_POINTS, null);
                return new RewardsPrompt(context);
            }
            return new RewardsPrompt(context);
        }
    }

    private class ItemListPrompt extends FixedSetPrompt {

        public ItemListPrompt() {
            super("1", "2", "3");
        }

        @Override
        public String getPromptText(ConversationContext context) {
            // Check/add newly made item
            if (context.getSessionData("newItem") != null) {
                if (context.getSessionData(CK.REW_ITEMS) != null) {
                    List<ItemStack> itemRews = getItems(context);
                    itemRews.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(CK.REW_ITEMS, itemRews);
                } else {
                    List<ItemStack> itemRews = new LinkedList<ItemStack>();
                    itemRews.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(CK.REW_ITEMS, itemRews);
                }
                context.setSessionData("newItem", null);
                context.setSessionData("tempStack", null);
            }
            String text = ChatColor.GOLD + Lang.get("itemRewardsTitle") + "\n";
            if (context.getSessionData(CK.REW_ITEMS) == null) {
                text += ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorDeliveryAddItem") + "\n";
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("clear") + "\n";
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("done");
            } else {
                for (ItemStack is : getItems(context)) {
                    text += ChatColor.GRAY + "- " + ItemUtil.getDisplayString(is) + "\n";
                }
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorDeliveryAddItem") + "\n";
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("clear") + "\n";
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("done");
            }
            return text;
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase("1")) {
                return new ItemStackPrompt(ItemListPrompt.this);
            } else if (input.equalsIgnoreCase("2")) {
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("rewItemsCleared"));
                context.setSessionData(CK.REW_ITEMS, null);
                return new ItemListPrompt();
            } else if (input.equalsIgnoreCase("3")) {
                return new RewardsPrompt(context);
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        private List<ItemStack> getItems(ConversationContext context) {
            return (List<ItemStack>) context.getSessionData(CK.REW_ITEMS);
        }
    }
    
    private class CommandsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String lang1 = Lang.get("rewCommandPrompt");
            lang1 = lang1.replace("<comma>", ChatColor.BOLD + "" + ChatColor.RED + "comma" + ChatColor.RESET 
                    + ChatColor.YELLOW);
            String lang2 = Lang.get("rewCommandPromptHint");
            return ChatColor.YELLOW + lang1 + "\n" + ChatColor.GOLD + lang2;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                String[] args = input.split(Lang.get("charSemi"));
                List<String> commands = new LinkedList<String>();
                for (String s : args) {
                    if (s.startsWith("/")) {
                        s = s.substring(1);
                    }
                    commands.add(s);
                }
                context.setSessionData(CK.REW_COMMAND, commands.isEmpty() ? null : commands);
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.REW_COMMAND, null);
            }
            return new RewardsPrompt(context);
        }
    }
    
    private class PermissionsListPrompt extends QuestsEditorNumericPrompt {

        public PermissionsListPrompt(ConversationContext context) {
            super(context);
        }
        
        private final int size = 4;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(ConversationContext context) {
            return Lang.get("permissionRewardsTitle");
        }
        
        @Override
        public ChatColor getNumberColor(ConversationContext context, int number) {
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
        public String getSelectionText(ConversationContext context, int number) {
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
        public String getAdditionalText(ConversationContext context, int number) {
            switch (number) {
            case 1:
                if (context.getSessionData(CK.REW_PERMISSION) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    String text = "\n";
                    for (String s : (List<String>) context.getSessionData(CK.REW_PERMISSION)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + s + "\n";
                    }
                    return text;
                }
            case 2:
                if (context.getSessionData(CK.REW_PERMISSION) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    if (context.getSessionData(CK.REW_PERMISSION_WORLDS) == null) {
                        return ChatColor.YELLOW + "(" + Lang.get("stageEditorOptional") + ")";
                    } else {
                        String text = "\n";
                        for (String s : (List<String>) context.getSessionData(CK.REW_PERMISSION_WORLDS)) {
                            text += ChatColor.GRAY + "     - " + ChatColor.AQUA + s + "\n";
                        }
                        return text;
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
        public String getPromptText(ConversationContext context) {
            QuestsEditorPostOpenNumericPromptEvent event 
                    = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            String text = ChatColor.GOLD + getTitle(context) + "\n";
            for (int i = 1; i <= size; i++) {
                text += getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                        + getSelectionText(context, i) + " " + getAdditionalText(context, i) + "\n";
            }
            return text;
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
            switch (input.intValue()) {
            case 1:
                return new PermissionsPrompt();
            case 2:
                return new PermissionsWorldsPrompt();
            case 3:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("rewPermissionsCleared"));
                context.setSessionData(CK.REW_PERMISSION, null);
                context.setSessionData(CK.REW_PERMISSION_WORLDS, null);
                return new PermissionsListPrompt(context);
            case 4:
                return new RewardsPrompt(context);
            default:
                return null;
            }
        }
        
    }

    private class PermissionsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("rewPermissionsPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                String[] args = input.split(" ");
                List<String> permissions = new LinkedList<String>();
                permissions.addAll(Arrays.asList(args));
                context.setSessionData(CK.REW_PERMISSION, permissions);
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.REW_PERMISSION, null);
            }
            return new PermissionsListPrompt(context);
        }
    }
    
    private class PermissionsWorldsPrompt extends StringPrompt {
        
        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("rewPermissionsWorldPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                String[] args = input.split(Lang.get("charSemi"));
                List<String> worlds = new LinkedList<String>();
                worlds.addAll(Arrays.asList(args));
                for (String w : worlds) {
                    if (!w.equals("null") && w != null && context.getPlugin().getServer().getWorld(w) == null) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + w + " " 
                                + Lang.get("eventEditorInvalidWorld"));
                        return new PermissionsWorldsPrompt();
                    }
                }
                context.setSessionData(CK.REW_PERMISSION_WORLDS, worlds);
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.REW_PERMISSION_WORLDS, null);
            }
            return new PermissionsListPrompt(context);
        }
    }

    private class mcMMOListPrompt extends FixedSetPrompt {

        public mcMMOListPrompt() {
            super("1", "2", "3", "4");
        }

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.GOLD + Lang.get("mcMMORewardsTitle") + "\n";
            if (context.getSessionData(CK.REW_MCMMO_SKILLS) == null) {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("reqSetSkills") + " (" + Lang.get("noneSet") + ")\n";
                text += ChatColor.GRAY + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.GRAY + " - " 
                        + Lang.get("reqSetSkillAmounts") + " (" + Lang.get("rewNoMcMMOSkills") + ")\n";
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("clear") + "\n";
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("done");
            } else {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("reqSetSkills") + "\n";
                for (String s : getSkills(context)) {
                    text += ChatColor.GRAY + "     - " + ChatColor.AQUA + s + "\n";
                }
                if (context.getSessionData(CK.REW_MCMMO_AMOUNTS) == null) {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                            + Lang.get("reqSetSkillAmounts") + " (" + Lang.get("noneSet") + ")\n";
                } else {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                            + Lang.get("reqSetSkillAmounts") + "\n";
                    for (Integer i : getSkillAmounts(context)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + i + "\n";
                    }
                }
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("clear") + "\n";
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("done");
            }
            return text;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase("1")) {
                return new mcMMOSkillsPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(CK.REW_MCMMO_SKILLS) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("rewSetMcMMOSkillsFirst"));
                    return new mcMMOListPrompt();
                } else {
                    return new mcMMOAmountsPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("rewMcMMOCleared"));
                context.setSessionData(CK.REW_MCMMO_SKILLS, null);
                context.setSessionData(CK.REW_MCMMO_AMOUNTS, null);
                return new mcMMOListPrompt();
            } else if (input.equalsIgnoreCase("4")) {
                int one;
                int two;
                if (context.getSessionData(CK.REW_MCMMO_SKILLS) != null) {
                    one = ((List<Integer>) context.getSessionData(CK.REW_MCMMO_SKILLS)).size();
                } else {
                    one = 0;
                }
                if (context.getSessionData(CK.REW_MCMMO_AMOUNTS) != null) {
                    two = ((List<Integer>) context.getSessionData(CK.REW_MCMMO_AMOUNTS)).size();
                } else {
                    two = 0;
                }
                if (one == two) {
                    return new RewardsPrompt(context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("rewMcMMOListsNotSameSize"));
                    return new mcMMOListPrompt();
                }
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        private List<String> getSkills(ConversationContext context) {
            return (List<String>) context.getSessionData(CK.REW_MCMMO_SKILLS);
        }

        @SuppressWarnings("unchecked")
        private List<Integer> getSkillAmounts(ConversationContext context) {
            return (List<Integer>) context.getSessionData(CK.REW_MCMMO_AMOUNTS);
        }
    }

    private class mcMMOSkillsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String skillList = ChatColor.DARK_GREEN + Lang.get("skillListTitle") + "\n";
            SkillType[] skills = SkillType.values();
            for (int i = 0; i < skills.length; i++) {
                if (i == (skills.length - 1)) {
                    skillList += ChatColor.GREEN + skills[i].getName() + "\n";
                } else {
                    skillList += ChatColor.GREEN + skills[i].getName() + "\n\n";
                }
            }
            return skillList + ChatColor.YELLOW + Lang.get("rewMcMMOPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                String[] args = input.split(" ");
                List<String> skills = new LinkedList<String>();
                for (String s : args) {
                    if (Quests.getMcMMOSkill(s) != null) {
                        if (skills.contains(s) == false) {
                            skills.add(MiscUtil.getCapitalized(s));
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listDuplicate"));
                            return new mcMMOSkillsPrompt();
                        }
                    } else {
                        String text = Lang.get("reqMcMMOError");
                        text = text.replace("<input>", ChatColor.LIGHT_PURPLE + s + ChatColor.RED);
                        context.getForWhom().sendRawMessage(ChatColor.RED + text);
                        return new mcMMOSkillsPrompt();
                    }
                }
                context.setSessionData(CK.REW_MCMMO_SKILLS, skills);
            }
            return new mcMMOListPrompt();
        }
    }

    private class mcMMOAmountsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("reqMcMMOAmountsPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                String[] args = input.split(" ");
                List<Integer> amounts = new LinkedList<Integer>();
                for (String s : args) {
                    try {
                        amounts.add(Integer.parseInt(s));
                    } catch (NumberFormatException e) {
                        String text = Lang.get("reqNotANumber");
                        text = text.replace("<input>", ChatColor.LIGHT_PURPLE + s + ChatColor.RED);
                        context.getForWhom().sendRawMessage(ChatColor.RED + text);
                        return new mcMMOAmountsPrompt();
                    }
                }
                context.setSessionData(CK.REW_MCMMO_AMOUNTS, amounts);
            }
            return new mcMMOListPrompt();
        }
    }

    private class HeroesListPrompt extends FixedSetPrompt {

        public HeroesListPrompt() {
            super("1", "2", "3", "4");
        }

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.GOLD + Lang.get("heroesRewardsTitle") + "\n";
            if (context.getSessionData(CK.REW_HEROES_CLASSES) == null) {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("rewSetHeroesClasses") + " (" + Lang.get("noneSet") + ")\n";
                text += ChatColor.GRAY + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.GRAY + " - " 
                        + Lang.get("rewSetHeroesAmounts") + "(" + Lang.get("rewNoHeroesClasses") + ")\n";
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("clear") + "\n";
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("done");
            } else {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - "
                        + Lang.get("rewSetHeroesClasses") + "\n";
                for (String s : getClasses(context)) {
                    text += ChatColor.GRAY + "     - " + ChatColor.AQUA + s + "\n";
                }
                if (context.getSessionData(CK.REW_HEROES_AMOUNTS) == null) {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                            + Lang.get("rewSetHeroesAmounts") + " (" + Lang.get("noneSet") + ")\n";
                } else {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                            + Lang.get("rewSetHeroesAmounts") + "\n";
                    for (Double d : getClassAmounts(context)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + d + "\n";
                    }
                }
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("clear") + "\n";
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("done");
            }
            return text;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase("1")) {
                return new HeroesClassesPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(CK.REW_HEROES_CLASSES) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("rewSetHeroesClassesFirst"));
                    return new HeroesListPrompt();
                } else {
                    return new HeroesExperiencePrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("rewHeroesCleared"));
                context.setSessionData(CK.REW_HEROES_CLASSES, null);
                context.setSessionData(CK.REW_HEROES_AMOUNTS, null);
                return new HeroesListPrompt();
            } else if (input.equalsIgnoreCase("4")) {
                int one;
                int two;
                if (context.getSessionData(CK.REW_HEROES_CLASSES) != null) {
                    one = ((List<Integer>) context.getSessionData(CK.REW_HEROES_CLASSES)).size();
                } else {
                    one = 0;
                }
                if (context.getSessionData(CK.REW_HEROES_AMOUNTS) != null) {
                    two = ((List<Double>) context.getSessionData(CK.REW_HEROES_AMOUNTS)).size();
                } else {
                    two = 0;
                }
                if (one == two) {
                    return new RewardsPrompt(context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("rewHeroesListsNotSameSize"));
                    return new HeroesListPrompt();
                }
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        private List<String> getClasses(ConversationContext context) {
            return (List<String>) context.getSessionData(CK.REW_HEROES_CLASSES);
        }

        @SuppressWarnings("unchecked")
        private List<Double> getClassAmounts(ConversationContext context) {
            return (List<Double>) context.getSessionData(CK.REW_HEROES_AMOUNTS);
        }
    }

    private class HeroesClassesPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext cc) {
            String text = ChatColor.DARK_PURPLE + Lang.get("heroesClassesTitle") + "\n";
            List<String> list = new LinkedList<String>();
            for (HeroClass hc : plugin.getDependencies().getHeroes().getClassManager().getClasses()) {
                list.add(hc.getName());
            }
            if (list.isEmpty()) {
                text += ChatColor.GRAY + "(" + Lang.get("none") + ")\n";
            } else {
                Collections.sort(list);
                for (String s : list) {
                    text += ChatColor.LIGHT_PURPLE + s + ", ";
                }
                text = text.substring(0, text.length() - 2) + "\n";
            }
            text += ChatColor.YELLOW + Lang.get("rewHeroesClassesPrompt");
            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext cc, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                String[] arr = input.split(" ");
                List<String> classes = new LinkedList<String>();
                for (String s : arr) {
                    HeroClass hc = plugin.getDependencies().getHeroes().getClassManager().getClass(s);
                    if (hc == null) {
                        String text = Lang.get("rewHeroesInvalidClass");
                        text = text.replace("<input>", ChatColor.LIGHT_PURPLE + s + ChatColor.RED);
                        cc.getForWhom().sendRawMessage(ChatColor.RED + text);
                        return new HeroesClassesPrompt();
                    } else {
                        classes.add(hc.getName());
                    }
                }
                cc.setSessionData(CK.REW_HEROES_CLASSES, classes);
                return new HeroesListPrompt();
            } else {
                return new HeroesListPrompt();
            }
        }
    }

    private class HeroesExperiencePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext cc) {
            String text = Lang.get("heroesExperienceTitle") + "\n";
            text += ChatColor.YELLOW + Lang.get("rewHeroesExperiencePrompt");
            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext cc, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                String[] arr = input.split(" ");
                List<Double> amounts = new LinkedList<Double>();
                for (String s : arr) {
                    try {
                        double d = Double.parseDouble(s);
                        amounts.add(d);
                    } catch (NumberFormatException nfe) {
                        String text = Lang.get("reqNotANumber");
                        text = text.replace("<input>", ChatColor.LIGHT_PURPLE + s + ChatColor.RED);
                        cc.getForWhom().sendRawMessage(ChatColor.RED + text);
                        return new HeroesExperiencePrompt();
                    }
                }
                cc.setSessionData(CK.REW_HEROES_AMOUNTS, amounts);
                return new HeroesListPrompt();
            } else {
                return new HeroesListPrompt();
            }
        }
    }

    private class PhatLootsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext cc) {
            String text = ChatColor.DARK_AQUA + Lang.get("phatLootsRewardsTitle") + "\n";
            for (PhatLoot pl : PhatLootsAPI.getAllPhatLoots()) {
                text += ChatColor.GRAY + "- " + ChatColor.BLUE + pl.name + "\n";
            }
            text += ChatColor.YELLOW + Lang.get("rewPhatLootsPrompt");
            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                String[] arr = input.split(" ");
                List<String> loots = new LinkedList<String>();
                for (String s : arr) {
                    if (PhatLootsAPI.getPhatLoot(s) == null) {
                        String text = Lang.get("rewPhatLootsInvalid");
                        text = text.replace("<input>", ChatColor.DARK_RED + s + ChatColor.RED);
                        context.getForWhom().sendRawMessage(ChatColor.RED + text);
                        return new PhatLootsPrompt();
                    }
                }
                loots.addAll(Arrays.asList(arr));
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

    private class CustomRewardsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.LIGHT_PURPLE + Lang.get("customRewardsTitle") + "\n";
            if (plugin.getCustomRewards().isEmpty()) {
                text += ChatColor.DARK_PURPLE + "(" + Lang.get("stageEditorNoModules") + ") ";
            } else {
                for (CustomReward cr : plugin.getCustomRewards()) {
                    text += ChatColor.DARK_PURPLE + "  - " + cr.getName() + "\n";
                }
            }
            return text + ChatColor.YELLOW + Lang.get("rewCustomRewardPrompt");
        }

        @SuppressWarnings("unchecked")
        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                CustomReward found = null;
                // Check if we have a custom reward with the specified name
                for (CustomReward cr : plugin.getCustomRewards()) {
                    if (cr.getName().equalsIgnoreCase(input)) {
                        found = cr;
                        break;
                    }
                }
                if (found == null) {
                    // No? Check again, but with locale sensitivity
                    for (CustomReward cr : plugin.getCustomRewards()) {
                        if (cr.getName().toLowerCase().contains(input.toLowerCase())) {
                            found = cr;
                            break;
                        }
                    }
                }
                if (found != null) {
                    if (context.getSessionData(CK.REW_CUSTOM) != null) {
                        // The custom reward may already have been added, so let's check that
                        LinkedList<String> list = (LinkedList<String>) context.getSessionData(CK.REW_CUSTOM);
                        LinkedList<Map<String, Object>> datamapList 
                                = (LinkedList<Map<String, Object>>) context.getSessionData(CK.REW_CUSTOM_DATA);
                        if (list.contains(found.getName()) == false) {
                            // Hasn't been added yet, so let's do it
                            list.add(found.getName());
                            datamapList.add(found.getData());
                            context.setSessionData(CK.REW_CUSTOM, list);
                            context.setSessionData(CK.REW_CUSTOM_DATA, datamapList);
                        } else {
                            // Already added, so inform user
                            context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("rewCustomAlreadyAdded"));
                            return new CustomRewardsPrompt();
                        }
                    } else {
                        // The custom reward hasn't been added yet, so let's do it
                        LinkedList<Map<String, Object>> datamapList = new LinkedList<Map<String, Object>>();
                        datamapList.add(found.getData());
                        LinkedList<String> list = new LinkedList<String>();
                        list.add(found.getName());
                        context.setSessionData(CK.REW_CUSTOM, list);
                        context.setSessionData(CK.REW_CUSTOM_DATA, datamapList);
                    }
                    // Send user to the custom data prompt if there is any needed
                    if (found.getData().isEmpty() == false) {
                        context.setSessionData(CK.REW_CUSTOM_DATA_DESCRIPTIONS, found.getDescriptions());
                        return new RewardCustomDataListPrompt();
                    }
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("rewCustomNotFound"));
                    return new CustomRewardsPrompt();
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
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.AQUA + "- ";
            LinkedList<String> list = (LinkedList<String>) context.getSessionData(CK.REW_CUSTOM);
            LinkedList<Map<String, Object>> datamapList 
                    = (LinkedList<Map<String, Object>>) context.getSessionData(CK.REW_CUSTOM_DATA);
            String rewName = list.getLast();
            Map<String, Object> datamap = datamapList.getLast();
            text += rewName + " -\n";
            int index = 1;
            LinkedList<String> datamapKeys = new LinkedList<String>();
            for (String key : datamap.keySet()) {
                datamapKeys.add(key);
            }
            Collections.sort(datamapKeys);
            for (String dataKey : datamapKeys) {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + index + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + dataKey;
                if (datamap.get(dataKey) != null) {
                    text += ChatColor.GREEN + " (" + datamap.get(dataKey).toString() + ")\n";
                } else {
                    text += ChatColor.RED + " (" + Lang.get("valRequired") + ")\n";
                }
                index++;
            }
            text += ChatColor.GREEN + "" + ChatColor.BOLD + index + ChatColor.YELLOW + " - " + Lang.get("done");
            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            @SuppressWarnings("unchecked")
            LinkedList<Map<String, Object>> datamapList 
                    = (LinkedList<Map<String, Object>>) context.getSessionData(CK.REW_CUSTOM_DATA);
            Map<String, Object> datamap = datamapList.getLast();
            int numInput;
            try {
                numInput = Integer.parseInt(input);
            } catch (NumberFormatException nfe) {
                return new RewardCustomDataListPrompt();
            }
            if (numInput < 1 || numInput > datamap.size() + 1) {
                return new RewardCustomDataListPrompt();
            }
            if (numInput < datamap.size() + 1) {
                LinkedList<String> datamapKeys = new LinkedList<String>();
                for (String key : datamap.keySet()) {
                    datamapKeys.add(key);
                }
                Collections.sort(datamapKeys);
                String selectedKey = datamapKeys.get(numInput - 1);
                context.setSessionData(CK.REW_CUSTOM_DATA_TEMP, selectedKey);
                return new RewardCustomDataPrompt();
            } else {
                if (datamap.containsValue(null)) {
                    return new RewardCustomDataListPrompt();
                } else {
                    context.setSessionData(CK.REW_CUSTOM_DATA_DESCRIPTIONS, null);
                    return new RewardsPrompt(context);
                }
            }
        }
    }

    private class RewardCustomDataPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String text = "";
            String temp = (String) context.getSessionData(CK.REW_CUSTOM_DATA_TEMP);
            @SuppressWarnings("unchecked")
            Map<String, String> descriptions 
                    = (Map<String, String>) context.getSessionData(CK.REW_CUSTOM_DATA_DESCRIPTIONS);
            if (descriptions.get(temp) != null) {
                text += ChatColor.GOLD + descriptions.get(temp) + "\n";
            }
            String lang = Lang.get("stageEditorCustomDataPrompt");
            lang = lang.replace("<data>", temp);
            text += ChatColor.YELLOW + lang;
            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            @SuppressWarnings("unchecked")
            LinkedList<Map<String, Object>> datamapList 
                    = (LinkedList<Map<String, Object>>) context.getSessionData(CK.REW_CUSTOM_DATA);
            Map<String, Object> datamap = datamapList.getLast();
            datamap.put((String) context.getSessionData(CK.REW_CUSTOM_DATA_TEMP), input);
            context.setSessionData(CK.REW_CUSTOM_DATA_TEMP, null);
            return new RewardCustomDataListPrompt();
        }
    }
}
