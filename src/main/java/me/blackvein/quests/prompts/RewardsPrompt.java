package me.blackvein.quests.prompts;

import com.codisimus.plugins.phatloots.PhatLoot;
import com.codisimus.plugins.phatloots.PhatLootsAPI;
import com.herocraftonline.heroes.characters.classes.HeroClass;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import me.blackvein.quests.CustomReward;
import me.blackvein.quests.util.ColorUtil;
import me.blackvein.quests.QuestFactory;
import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.FixedSetPrompt;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.inventory.ItemStack;

public class RewardsPrompt extends FixedSetPrompt implements ColorUtil {

    final Quests quests;

    final QuestFactory factory;

    public RewardsPrompt(Quests plugin, QuestFactory qf) {

        super("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11");
        quests = plugin;
        factory = qf;

    }

    @Override
    public String getPromptText(ConversationContext context) {

        String text;

        text = DARKAQUA + "- " + AQUA + context.getSessionData(CK.Q_NAME) + AQUA + " | Rewards -\n";

        if (context.getSessionData(CK.REW_MONEY) == null) {
            text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set money reward (None set)\n";
        } else {
            int moneyRew = (Integer) context.getSessionData(CK.REW_MONEY);
            text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set money reward (" + moneyRew + " " + (moneyRew > 1 ? Quests.getCurrency(true) : Quests.getCurrency(false)) + ")\n";
        }

        if (context.getSessionData(CK.REW_QUEST_POINTS) == null) {
            text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set Quest Points reward (None set)\n";
        } else {
            text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set Quest Points reward (" + context.getSessionData(CK.REW_QUEST_POINTS) + " Quest Points)\n";
        }

        text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Set item rewards\n";

        if (context.getSessionData(CK.REW_EXP) == null) {
            text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Set experience reward (None set)\n";
        } else {
            text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Set experience reward (" + context.getSessionData(CK.REW_EXP) + " points)\n";
        }

        if (context.getSessionData(CK.REW_COMMAND) == null) {
            text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - Set command rewards (None set)\n";
        } else {
            text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - Set command rewards\n";
            List<String> commands = (List<String>) context.getSessionData(CK.REW_COMMAND);

            for (String cmd : commands) {

                text += GRAY + "    - " + AQUA + cmd + "\n";

            }
        }

        if (context.getSessionData(CK.REW_PERMISSION) == null) {
            text += BLUE + "" + BOLD + "6" + RESET + YELLOW + " - Set permission rewards (None set)\n";
        } else {
            text += BLUE + "" + BOLD + "6" + RESET + YELLOW + " - Set permission rewards\n";
            List<String> permissions = (List<String>) context.getSessionData(CK.REW_PERMISSION);

            for (String perm : permissions) {

                text += GRAY + "    - " + AQUA + perm + "\n";

            }
        }

        if (Quests.mcmmo != null) {

            if (context.getSessionData(CK.REW_MCMMO_SKILLS) == null) {
                text += BLUE + "" + BOLD + "7" + RESET + YELLOW + " - Set mcMMO skill rewards (None set)\n";
            } else {
                text += BLUE + "" + BOLD + "7" + RESET + YELLOW + " - Set mcMMO skill rewards\n";
                List<String> skills = (List<String>) context.getSessionData(CK.REW_MCMMO_SKILLS);
                List<Integer> amounts = (List<Integer>) context.getSessionData(CK.REW_MCMMO_AMOUNTS);

                for (String skill : skills) {

                    text += GRAY + "    - " + AQUA + skill + GRAY + " x " + DARKAQUA + amounts.get(skills.indexOf(skill)) + "\n";

                }
            }

        } else {

            text += GRAY + "7 - Set mcMMO skill rewards (mcMMO not installed)\n";

        }

        if (Quests.heroes != null) {

            if (context.getSessionData(CK.REW_HEROES_CLASSES) == null) {
                text += BLUE + "" + BOLD + "8" + RESET + YELLOW + " - Set Heroes experience rewards (None set)\n";
            } else {
                text += BLUE + "" + BOLD + "8" + RESET + YELLOW + " - Set Heroes experience rewards\n";
                List<String> heroClasses = (List<String>) context.getSessionData(CK.REW_HEROES_CLASSES);
                List<Double> amounts = (List<Double>) context.getSessionData(CK.REW_HEROES_AMOUNTS);

                for (String heroClass : heroClasses) {

                    text += GRAY + "    - " + AQUA + amounts.get(heroClasses.indexOf(heroClass)) + " " + DARKAQUA + heroClass + " Experience\n";

                }
            }

        } else {

            text += GRAY + "8 - Set Heroes experience rewards (Heroes not installed)\n";

        }

        if (Quests.phatLoots != null) {

            if (context.getSessionData(CK.REW_PHAT_LOOTS) == null) {
                text += BLUE + "" + BOLD + "9" + RESET + YELLOW + " - Set PhatLoot rewards (None set)\n";
            } else {
                text += BLUE + "" + BOLD + "9" + RESET + YELLOW + " - Set PhatLoot rewards\n";
                List<String> phatLoots = (List<String>) context.getSessionData(CK.REW_PHAT_LOOTS);

                for (String phatLoot : phatLoots) {

                    text += GRAY + "    - " + AQUA + phatLoot + "\n";

                }
            }

        } else {

            text += GRAY + "9 - Set PhatLoot rewards (PhatLoots not installed)\n";

        }
        
        if (context.getSessionData(CK.REW_CUSTOM) == null) {
            text += BLUE + "" + BOLD + "10 - " + RESET + ITALIC + PURPLE + "Custom Rewards (None set)\n";
        } else {
            text += BLUE + "" + BOLD + "10 - " + RESET + ITALIC + PURPLE + "Custom Rewards\n";
            LinkedList<String> customRews = (LinkedList<String>) context.getSessionData(CK.REW_CUSTOM);
            for(String s : customRews){
                
                text += RESET + "" + PURPLE + "  - " + PINK + s + "\n";
                
            }
        }

        text += GREEN + "" + BOLD + "11" + RESET + YELLOW + " - Done";

        return text;

    }

    @Override
    protected Prompt acceptValidatedInput(ConversationContext context, String input) {

        if (input.equalsIgnoreCase("1")) {
            return new MoneyPrompt();
        } else if (input.equalsIgnoreCase("2")) {
            return new QuestPointsPrompt();
        } else if (input.equalsIgnoreCase("3")) {
            return new ItemListPrompt();
        } else if (input.equalsIgnoreCase("4")) {
            return new ExperiencePrompt();
        } else if (input.equalsIgnoreCase("5")) {
            return new CommandsPrompt();
        } else if (input.equalsIgnoreCase("6")) {
            return new PermissionsPrompt();
        } else if (input.equalsIgnoreCase("7")) {
            if (Quests.mcmmo != null) {
                return new mcMMOListPrompt();
            } else {
                return new RewardsPrompt(quests, factory);
            }
        } else if (input.equalsIgnoreCase("8")) {
            if (Quests.heroes != null) {
                return new HeroesListPrompt();
            } else {
                return new RewardsPrompt(quests, factory);
            }
        } else if (input.equalsIgnoreCase("9")) {
            if (Quests.phatLoots != null) {
                return new PhatLootsPrompt();
            } else {
                return new RewardsPrompt(quests, factory);
            }
        }else if(input.equalsIgnoreCase("10")){
            return new CustomRewardsPrompt();
        }else if (input.equalsIgnoreCase("11")) {
            return factory.returnToMenu();
        }
        return null;

    }

    private class MoneyPrompt extends NumericPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + "Enter amount of " + AQUA + (Quests.economy.currencyNamePlural().isEmpty() ? "Money" : Quests.economy.currencyNamePlural()) + YELLOW + ", or 0 to clear the money reward, or -1 to cancel";

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {

            if (input.intValue() < -1) {
                context.getForWhom().sendRawMessage(RED + "Amount must be positive!");
                return new MoneyPrompt();
            } else if (input.intValue() == 0) {
                context.setSessionData(CK.REW_MONEY, null);
            } else if (input.intValue() != -1) {
                context.setSessionData(CK.REW_MONEY, input.intValue());
            }

            return new RewardsPrompt(quests, factory);

        }

    }

    private class ExperiencePrompt extends NumericPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + "Enter amount of experience, or 0 to clear the experience reward, or -1 to cancel";

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {

            if (input.intValue() < -1) {
                context.getForWhom().sendRawMessage(RED + "Amount must be positive!");
                return new ExperiencePrompt();
            } else if (input.intValue() == -1) {
                context.setSessionData(CK.REW_EXP, null);
            } else if (input.intValue() != 0) {
                context.setSessionData(CK.REW_EXP, input.intValue());
            }

            return new RewardsPrompt(quests, factory);

        }

    }

    private class QuestPointsPrompt extends NumericPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + "Enter amount of Quest Points, or 0 to clear the Quest Points reward, or -1 to cancel";

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {

            if (input.intValue() < -1) {
                context.getForWhom().sendRawMessage(RED + "Amount must be positive!");
                return new QuestPointsPrompt();
            } else if (input.intValue() == -1) {
                context.setSessionData(CK.REW_QUEST_POINTS, null);
            } else if (input.intValue() != 0) {
                context.setSessionData(CK.REW_QUEST_POINTS, input.intValue());
            }

            return new RewardsPrompt(quests, factory);

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
                    LinkedList<ItemStack> itemRews = new LinkedList<ItemStack>();
                    itemRews.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(CK.REW_ITEMS, itemRews);
                }

                context.setSessionData("newItem", null);
                context.setSessionData("tempStack", null);

            }

            String text = GOLD + "- Item Rewards -\n";
            if (context.getSessionData(CK.REW_ITEMS) == null) {
                text += GRAY + " (" + Lang.get("noneSet") + ")\n";
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Add item\n";
                text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Done";
            } else {

                for (ItemStack is : getItems(context)) {

                    text += GRAY + "- " + ItemUtil.getDisplayString(is) + "\n";

                }
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Add item\n";
                text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Done";

            }

            return text;

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new ItemStackPrompt(ItemListPrompt.this);
            } else if (input.equalsIgnoreCase("2")) {
                context.getForWhom().sendRawMessage(YELLOW + "Item rewards cleared.");
                context.setSessionData(CK.REW_ITEMS, null);
                return new ItemListPrompt();
            } else if (input.equalsIgnoreCase("3")) {
                return new RewardsPrompt(quests, factory);
            }
            return null;

        }

        private List<ItemStack> getItems(ConversationContext context) {
            return (List<ItemStack>) context.getSessionData(CK.REW_ITEMS);
        }

    }

    private class CommandsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String note = GOLD + "\nNote: You may put <player> to specify the player who completed the Quest. e.g. " + AQUA + BOLD + ITALIC + "smite <player>" + RESET;
            return YELLOW + "Enter command rewards separating each one by a " + BOLD + "comma" + RESET + YELLOW + ", or enter \'clear\' to clear the list, or enter \'cancel\' to return." + note;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("cancel") == false && input.equalsIgnoreCase("clear") == false) {

                String[] args = input.split(",");
                LinkedList<String> commands = new LinkedList<String>();
                for (String s : args) {

                    if (s.startsWith("/")) {
                        s = s.substring(1);
                    }

                    commands.add(s);

                }

                context.setSessionData(CK.REW_COMMAND, commands);

            } else if (input.equalsIgnoreCase("clear")) {
                context.setSessionData(CK.REW_COMMAND, null);
            }

            return new RewardsPrompt(quests, factory);

        }

    }

    private class PermissionsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return YELLOW + "Enter permission rewards separating each one by a space, or enter \'clear\' to clear the list, or enter \'cancel\' to return.";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("cancel") == false && input.equalsIgnoreCase("clear") == false) {

                String[] args = input.split(" ");
                LinkedList<String> permissions = new LinkedList<String>();
                permissions.addAll(Arrays.asList(args));

                context.setSessionData(CK.REW_PERMISSION, permissions);

            } else if (input.equalsIgnoreCase("clear")) {
                context.setSessionData(CK.REW_PERMISSION, null);
            }

            return new RewardsPrompt(quests, factory);

        }

    }

    //mcMMO
    private class mcMMOListPrompt extends FixedSetPrompt {

        public mcMMOListPrompt() {

            super("1", "2", "3", "4");

        }

        @Override
        public String getPromptText(ConversationContext context) {

            String text = GOLD + "- mcMMO Rewards -\n";
            if (context.getSessionData(CK.REW_MCMMO_SKILLS) == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set skills (None set)\n";
                text += GRAY + "2 - Set skill amounts (No skills set)\n";
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Done";
            } else {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set skills\n";
                for (String s : getSkills(context)) {

                    text += GRAY + "    - " + AQUA + s + "\n";

                }

                if (context.getSessionData(CK.REW_MCMMO_AMOUNTS) == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set skill amounts (None set)\n";
                } else {

                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set skill amounts\n";
                    for (Integer i : getSkillAmounts(context)) {

                        text += GRAY + "    - " + AQUA + i + "\n";

                    }

                }

                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Done";

            }

            return text;

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new mcMMOSkillsPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(CK.REW_MCMMO_SKILLS) == null) {
                    context.getForWhom().sendRawMessage(RED + "You must set skills first!");
                    return new mcMMOListPrompt();
                } else {
                    return new mcMMOAmountsPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                context.getForWhom().sendRawMessage(YELLOW + "mcMMO rewards cleared.");
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
                    return new RewardsPrompt(quests, factory);
                } else {
                    context.getForWhom().sendRawMessage(RED + "The " + GOLD + "skills list " + RED + "and " + GOLD + "skill amounts list " + RED + "are not the same size!");
                    return new mcMMOListPrompt();
                }
            }
            return null;

        }

        private List<String> getSkills(ConversationContext context) {
            return (List<String>) context.getSessionData(CK.REW_MCMMO_SKILLS);
        }

        private List<Integer> getSkillAmounts(ConversationContext context) {
            return (List<Integer>) context.getSessionData(CK.REW_MCMMO_AMOUNTS);
        }

    }

    private class mcMMOSkillsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String skillList
                    = GOLD + "-Skill List-\n"
                    + AQUA + "Acrobatics\n"
                    + GRAY + "All\n"
                    + AQUA + "Archery\n"
                    + AQUA + "Axes\n"
                    + AQUA + "Excavation\n"
                    + AQUA + "Fishing\n"
                    + AQUA + "Herbalism\n"
                    + AQUA + "Mining\n"
                    + AQUA + "Repair\n"
                    + AQUA + "Smelting\n"
                    + AQUA + "Swords\n"
                    + AQUA + "Taming\n"
                    + AQUA + "Unarmed\n"
                    + AQUA + "Woodcutting\n\n";

            return skillList + YELLOW + "Enter mcMMO skills, separating each one by a space, or enter \'cancel\' to return."
                    + "\n" + GOLD + "Note: The \'All\' option will give levels to all skills.";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("cancel") == false) {

                String[] args = input.split(" ");
                LinkedList<String> skills = new LinkedList<String>();
                for (String s : args) {

                    if (Quests.getMcMMOSkill(s) != null) {

                        if (skills.contains(s) == false) {
                            skills.add(Quester.getCapitalized(s));
                        } else {
                            context.getForWhom().sendRawMessage(RED + "List contains duplicates!");
                            return new mcMMOSkillsPrompt();
                        }

                    } else {
                        context.getForWhom().sendRawMessage(PINK + s + RED + " is not a valid mcMMO skill!");
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
            return YELLOW + "Enter skill amounts (numbers), separating each one by a space, or enter \'cancel\' to return.";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("cancel") == false) {

                String[] args = input.split(" ");
                LinkedList<Integer> amounts = new LinkedList<Integer>();
                for (String s : args) {

                    try {

                        if (Integer.parseInt(s) > 0) {
                            amounts.add(Integer.parseInt(s));
                        } else {
                            context.getForWhom().sendRawMessage(PINK + s + RED + " is not greater than 0!");
                            return new mcMMOAmountsPrompt();
                        }

                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(RED + "Invalid entry " + PINK + s + RED + ". Input was not a list of numbers!");
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

            String text = GOLD + "- Heroes Rewards -\n";
            if (context.getSessionData(CK.REW_HEROES_CLASSES) == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set classes (None set)\n";
                text += GRAY + "2 - Set experience amounts (No classes set)\n";
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Done";
            } else {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set classes\n";
                for (String s : getClasses(context)) {

                    text += GRAY + "    - " + AQUA + s + "\n";

                }

                if (context.getSessionData(CK.REW_HEROES_AMOUNTS) == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set experience amounts (None set)\n";
                } else {

                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set experience amounts\n";
                    for (Double d : getClassAmounts(context)) {

                        text += GRAY + "    - " + AQUA + d + "\n";

                    }

                }

                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Done";

            }

            return text;

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new HeroesClassesPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(CK.REW_HEROES_CLASSES) == null) {
                    context.getForWhom().sendRawMessage(RED + "You must set classes first!");
                    return new HeroesListPrompt();
                } else {
                    return new HeroesExperiencePrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                context.getForWhom().sendRawMessage(YELLOW + "Heroes rewards cleared.");
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
                    return new RewardsPrompt(quests, factory);
                } else {
                    context.getForWhom().sendRawMessage(RED + "The " + GOLD + "classes list " + RED + "and " + GOLD + "experience amounts list " + RED + "are not the same size!");
                    return new HeroesListPrompt();
                }
            }
            return null;

        }

        private List<String> getClasses(ConversationContext context) {
            return (List<String>) context.getSessionData(CK.REW_HEROES_CLASSES);
        }

        private List<Double> getClassAmounts(ConversationContext context) {
            return (List<Double>) context.getSessionData(CK.REW_HEROES_AMOUNTS);
        }

    }

    private class HeroesClassesPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext cc) {

            String text = PURPLE + "- " + PINK + "Heroes Classes" + PURPLE + " -\n";
            LinkedList<String> list = new LinkedList<String>();
            for (HeroClass hc : Quests.heroes.getClassManager().getClasses()) {
                list.add(hc.getName());
            }

            if (list.isEmpty()) {
                text += GRAY + "(None)\n";
            } else {

                Collections.sort(list);

                for (String s : list) {
                    text += PINK + s + ", ";
                }

                text = text.substring(0, text.length() - 2) + "\n";

            }

            text += YELLOW + "Enter Heroes classes separating each one by a space, or enter \"cancel\" to return.";

            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext cc, String input) {

            if (input.equalsIgnoreCase("cancel") == false) {

                String[] arr = input.split(" ");
                LinkedList<String> classes = new LinkedList<String>();

                for (String s : arr) {

                    HeroClass hc = Quests.heroes.getClassManager().getClass(s);
                    if (hc == null) {
                        cc.getForWhom().sendRawMessage(RED + "Error: " + PINK + s + RED + " is not a valid Heroes class name!");
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

            String text = PURPLE + "- " + PINK + "Heroes Experience" + PURPLE + " -\n";

            text += YELLOW + "Enter experience amounts (numbers, decimals are allowed) separating each one by a space, or enter \"cancel\" to return.";

            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext cc, String input) {

            if (input.equalsIgnoreCase("cancel") == false) {

                String[] arr = input.split(" ");
                LinkedList<Double> amounts = new LinkedList<Double>();

                for (String s : arr) {

                    try {

                        double d = Double.parseDouble(s);
                        if (d > 0) {
                            amounts.add(d);
                        } else {
                            cc.getForWhom().sendRawMessage(RED + "Error: " + PINK + s + RED + " is not greater than zero!");
                            return new HeroesExperiencePrompt();
                        }

                    } catch (NumberFormatException nfe) {
                        cc.getForWhom().sendRawMessage(RED + "Error: " + PINK + s + RED + " is not a number!");
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

            String text = DARKAQUA + "- " + AQUA + "PhatLoots" + DARKAQUA + " -\n";

            for (PhatLoot pl : PhatLootsAPI.getAllPhatLoots()) {

                text += GRAY + "- " + BLUE + pl.name + "\n";

            }

            text += YELLOW + "Enter PhatLoots separating each one by a space, or enter \"clear\" to clear the list, or \"cancel\" to return.";

            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext cc, String input) {

            if (input.equalsIgnoreCase("cancel") == false && input.equalsIgnoreCase("clear") == false) {

                String[] arr = input.split(" ");
                LinkedList<String> loots = new LinkedList<String>();

                for (String s : arr) {

                    if (PhatLootsAPI.getPhatLoot(s) == null) {
                        cc.getForWhom().sendRawMessage(DARKRED + s + RED + " is not a valid PhatLoot name!");
                        return new PhatLootsPrompt();
                    }

                }

                loots.addAll(Arrays.asList(arr));
                cc.setSessionData(CK.REW_PHAT_LOOTS, loots);
                return new RewardsPrompt(quests, factory);

            } else if (input.equalsIgnoreCase("clear")) {

                cc.setSessionData(CK.REW_PHAT_LOOTS, null);
                cc.getForWhom().sendRawMessage(YELLOW + "PhatLoots reward cleared.");
                return new RewardsPrompt(quests, factory);

            } else {
                return new RewardsPrompt(quests, factory);
            }

        }
    }
    
    private class CustomRewardsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String text = PINK + "- Custom Rewards -\n";
            if(quests.customRewards.isEmpty()){
                text += BOLD + "" + PURPLE + "(No modules loaded)";
            }else {
                for(CustomReward cr : quests.customRewards)
                    text += PURPLE + " - " + cr.getName() + "\n";
            }
            
            return text + YELLOW + "Enter the name of a custom reward to add, or enter \'clear\' to clear all custom rewards, or \'cancel\' to return.";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("cancel") == false && input.equalsIgnoreCase("clear") == false) {

                CustomReward found = null;
                for(CustomReward cr : quests.customRewards){
                    if(cr.getName().equalsIgnoreCase(input)){
                        found = cr;
                        break;
                    }
                }
                
                if(found == null){
                    for(CustomReward cr : quests.customRewards){
                        if(cr.getName().toLowerCase().contains(input.toLowerCase())){
                            found = cr;
                            break;
                        }
                    }
                }
                
                if(found != null){
                    
                    if(context.getSessionData(CK.REW_CUSTOM) != null){
                        LinkedList<String> list = (LinkedList<String>) context.getSessionData(CK.REW_CUSTOM);
                        LinkedList<Map<String, Object>> datamapList = (LinkedList<Map<String, Object>>) context.getSessionData(CK.REW_CUSTOM_DATA);
                        if(list.contains(found.getName()) == false){
                            list.add(found.getName());
                            datamapList.add(found.datamap);
                            context.setSessionData(CK.REW_CUSTOM, list);
                            context.setSessionData(CK.REW_CUSTOM_DATA, datamapList);
                        }else{
                            context.getForWhom().sendRawMessage(YELLOW + "That custom reward has already been added!");
                            return new CustomRewardsPrompt();
                        }
                    }else{
                        LinkedList<Map<String, Object>> datamapList = new LinkedList<Map<String, Object>>();
                        datamapList.add(found.datamap);
                        LinkedList<String> list = new LinkedList<String>();
                        list.add(found.getName());
                        context.setSessionData(CK.REW_CUSTOM, list);
                        context.setSessionData(CK.REW_CUSTOM_DATA, datamapList);
                    }
                    
                    //Send user to the custom data prompt if there is any needed
                    if(found.datamap.isEmpty() == false){
                        
                        context.setSessionData(CK.REW_CUSTOM_DATA_DESCRIPTIONS, found.descriptions);
                        return new RewardCustomDataListPrompt();
                        
                    }
                    //
                    
                }else{
                    context.getForWhom().sendRawMessage(YELLOW + "Custom reward module not found.");
                    return new CustomRewardsPrompt();
                }

            } else if (input.equalsIgnoreCase("clear")) {
                context.setSessionData(CK.REW_CUSTOM, null);
                context.setSessionData(CK.REW_CUSTOM_DATA, null);
                context.setSessionData(CK.REW_CUSTOM_DATA_TEMP, null);
                context.getForWhom().sendRawMessage(YELLOW + "Custom rewards cleared.");
            }

            return new RewardsPrompt(quests, factory);

        }
    }
    
    private class RewardCustomDataListPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            
            String text = BOLD + "" + AQUA + "- ";
            
            LinkedList<String> list = (LinkedList<String>) context.getSessionData(CK.REW_CUSTOM);
            LinkedList<Map<String, Object>> datamapList = (LinkedList<Map<String, Object>>) context.getSessionData(CK.REW_CUSTOM_DATA);
            
            String rewName = list.getLast();
            Map<String, Object> datamap = datamapList.getLast();
            
            text += rewName + " -\n";
            int index = 1;
            
            LinkedList<String> datamapKeys = new LinkedList<String>();
            for(String key : datamap.keySet())
                datamapKeys.add(key);
            Collections.sort(datamapKeys);
            
            for(String dataKey : datamapKeys){
                
                text += BOLD + "" + DARKBLUE + index + " - " + RESET + BLUE + dataKey;
                if(datamap.get(dataKey) != null)
                    text += GREEN + " (" + (String) datamap.get(dataKey) + ")\n";
                else
                    text += RED + " (Value required)\n";
                
                index++;
                
            }
            
            text += BOLD + "" + DARKBLUE + index + " - " + AQUA + "Finish";
            
            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            
            LinkedList<Map<String, Object>> datamapList = (LinkedList<Map<String, Object>>) context.getSessionData(CK.REW_CUSTOM_DATA);
            Map<String, Object> datamap = datamapList.getLast();
            
            int numInput;
            
            try{
                numInput = Integer.parseInt(input);
            }catch(NumberFormatException nfe){
                return new RewardCustomDataListPrompt();
            }
            
            if(numInput < 1 || numInput > datamap.size() + 1)
                return new RewardCustomDataListPrompt();
            
            if(numInput < datamap.size() + 1){
                
                LinkedList<String> datamapKeys = new LinkedList<String>();
                for(String key : datamap.keySet())
                    datamapKeys.add(key);
                Collections.sort(datamapKeys);

                String selectedKey = datamapKeys.get(numInput - 1);
                context.setSessionData(CK.REW_CUSTOM_DATA_TEMP, selectedKey);
                return new RewardCustomDataPrompt();
                
            }else{
                
                if(datamap.containsValue(null)){
                    return new RewardCustomDataListPrompt();
                }else{
                    context.setSessionData(CK.REW_CUSTOM_DATA_DESCRIPTIONS, null);
                    return new RewardsPrompt(quests, factory);
                }
                
            }

        }
        
    }
    
    private class RewardCustomDataPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String text = "";
            String temp = (String)context.getSessionData(CK.REW_CUSTOM_DATA_TEMP);
            Map<String, String> descriptions = (Map<String, String>) context.getSessionData(CK.REW_CUSTOM_DATA_DESCRIPTIONS);
            if(descriptions.get(temp) != null)
                text += GOLD + descriptions.get(temp) + "\n";
                
            text += YELLOW + "Enter value for ";
            text += BOLD + temp + RESET + YELLOW + ":";
            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            LinkedList<Map<String, Object>> datamapList = (LinkedList<Map<String, Object>>) context.getSessionData(CK.REW_CUSTOM_DATA);
            Map<String, Object> datamap = datamapList.getLast();
            datamap.put((String)context.getSessionData(CK.REW_CUSTOM_DATA_TEMP), input);
            context.setSessionData(CK.REW_CUSTOM_DATA_TEMP, null);
            return new RewardCustomDataListPrompt();
        }
        
    }

}
