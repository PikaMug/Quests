package me.blackvein.quests.prompts;

import com.codisimus.plugins.phatloots.PhatLoot;
import com.codisimus.plugins.phatloots.PhatLootsAPI;
import com.gmail.nossr50.datatypes.skills.SkillType;
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

    @SuppressWarnings("unchecked")
	@Override
    public String getPromptText(ConversationContext context) {

        String text;

        String lang = Lang.get("rewardsTitle");
        lang = lang.replaceAll("<quest>", AQUA + (String) context.getSessionData(CK.Q_NAME) + DARKAQUA);
        text = DARKAQUA + lang + "\n";

        if (context.getSessionData(CK.REW_MONEY) == null) {
            text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("rewSetMoney") + " (" + Lang.get("noneSet") + ")\n";
        } else {
            int moneyRew = (Integer) context.getSessionData(CK.REW_MONEY);
            text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("rewSetMoney") + " (" + moneyRew + " " + (moneyRew > 1 ? Quests.getCurrency(true) : Quests.getCurrency(false)) + ")\n";
        }

        if (context.getSessionData(CK.REW_QUEST_POINTS) == null) {
            text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("rewSetQuestPoints") + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("rewSetQuestPoints") + " (" + context.getSessionData(CK.REW_QUEST_POINTS) + " " + Lang.get("questPoints") + ")\n";
        }

        text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("rewSetItems") + "\n";

        if (context.getSessionData(CK.REW_EXP) == null) {
            text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("rewSetExperience") + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("rewSetExperience") + " (" + context.getSessionData(CK.REW_EXP) + " " + Lang.get("points") + ")\n";
        }

        if (context.getSessionData(CK.REW_COMMAND) == null) {
            text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - " + Lang.get("rewSetCommands") + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - " + Lang.get("rewSetCommands") + "\n";
            List<String> commands = (List<String>) context.getSessionData(CK.REW_COMMAND);

            for (String cmd : commands) {

                text += GRAY + "    - " + AQUA + cmd + "\n";

            }
        }

        if (context.getSessionData(CK.REW_PERMISSION) == null) {
            text += BLUE + "" + BOLD + "6" + RESET + YELLOW + " - " + Lang.get("rewSetPermission") + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += BLUE + "" + BOLD + "6" + RESET + YELLOW + " - " + Lang.get("rewSetPermission") + "\n";
            List<String> permissions = (List<String>) context.getSessionData(CK.REW_PERMISSION);

            for (String perm : permissions) {

                text += GRAY + "    - " + AQUA + perm + "\n";

            }
        }

        if (Quests.mcmmo != null) {

            if (context.getSessionData(CK.REW_MCMMO_SKILLS) == null) {
                text += BLUE + "" + BOLD + "7" + RESET + YELLOW + " - " + Lang.get("rewSetMcMMO") + " (" + Lang.get("noneSet") + ")\n";
            } else {
                text += BLUE + "" + BOLD + "7" + RESET + YELLOW + " - " + Lang.get("rewSetMcMMO") + "\n";
                List<String> skills = (List<String>) context.getSessionData(CK.REW_MCMMO_SKILLS);
                List<Integer> amounts = (List<Integer>) context.getSessionData(CK.REW_MCMMO_AMOUNTS);

                for (String skill : skills) {

                    text += GRAY + "    - " + AQUA + skill + GRAY + " x " + DARKAQUA + amounts.get(skills.indexOf(skill)) + "\n";

                }
            }

        } else {

            text += GRAY + "7 - " + Lang.get("rewSetMcMMO") + " (" + Lang.get("reqNoMcMMO") + ")\n";

        }

        if (Quests.heroes != null) {

            if (context.getSessionData(CK.REW_HEROES_CLASSES) == null) {
                text += BLUE + "" + BOLD + "8" + RESET + YELLOW + " - " + Lang.get("rewSetHeroes") + " (" + Lang.get("noneSet") + ")\n";
            } else {
                text += BLUE + "" + BOLD + "8" + RESET + YELLOW + " - " + Lang.get("rewSetHeroes") + "\n";
                List<String> heroClasses = (List<String>) context.getSessionData(CK.REW_HEROES_CLASSES);
                List<Double> amounts = (List<Double>) context.getSessionData(CK.REW_HEROES_AMOUNTS);

                for (String heroClass : heroClasses) {

                    text += GRAY + "    - " + AQUA + amounts.get(heroClasses.indexOf(heroClass)) + " " + DARKAQUA + heroClass + " " + Lang.get("experience") + "\n";

                }
            }

        } else {

            text += GRAY + "8 - " + Lang.get("rewSetHeroes") + " (" + Lang.get("rewNoHeroes") + ")\n";

        }

        if (Quests.phatLoots != null) {

            if (context.getSessionData(CK.REW_PHAT_LOOTS) == null) {
                text += BLUE + "" + BOLD + "9" + RESET + YELLOW + " - " + Lang.get("rewSetPhat") + " (" + Lang.get("noneSet") + ")\n";
            } else {
                text += BLUE + "" + BOLD + "9" + RESET + YELLOW + " - " + Lang.get("rewSetPhat") + "\n";
                List<String> phatLoots = (List<String>) context.getSessionData(CK.REW_PHAT_LOOTS);

                for (String phatLoot : phatLoots) {

                    text += GRAY + "    - " + AQUA + phatLoot + "\n";

                }
            }

        } else {

            text += GRAY + "9 - " + Lang.get("rewSetPhat") + " (" + Lang.get("rewNoPhat") + ")\n";

        }

        if (context.getSessionData(CK.REW_CUSTOM) == null) {
            text += BLUE + "" + BOLD + "10 - " + RESET + ITALIC + PURPLE + Lang.get("rewSetCustom") + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += BLUE + "" + BOLD + "10 - " + RESET + ITALIC + PURPLE + Lang.get("rewSetCustom") + "\n";
            LinkedList<String> customRews = (LinkedList<String>) context.getSessionData(CK.REW_CUSTOM);
            for (String s : customRews) {

                text += RESET + "" + PURPLE + "  - " + PINK + s + "\n";

            }
        }

        text += GREEN + "" + BOLD + "11" + RESET + YELLOW + " - " + Lang.get("done");

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
        } else if (input.equalsIgnoreCase("10")) {
            return new CustomRewardsPrompt();
        } else if (input.equalsIgnoreCase("11")) {
            return factory.returnToMenu();
        }
        return null;

    }

    private class MoneyPrompt extends NumericPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String text = Lang.get("rewMoneyPrompt");
            text = text.replaceAll("<money>", AQUA + (Quests.economy.currencyNamePlural().isEmpty() ? Lang.get("money") : Quests.economy.currencyNamePlural()) + YELLOW);
            return YELLOW + text;

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {

            if (input.intValue() < -1) {
                context.getForWhom().sendRawMessage(RED + Lang.get("inputPosNum"));
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

            return YELLOW + Lang.get("rewExperiencePrompt");

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {

            if (input.intValue() < -1) {
                context.getForWhom().sendRawMessage(RED + Lang.get("inputPosNum"));
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

            return YELLOW + Lang.get("rewQuestPointsPrompt");

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {

            if (input.intValue() < -1) {
                context.getForWhom().sendRawMessage(RED + Lang.get("inputPosNum"));
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

            String text = GOLD + Lang.get("itemRewardsTitle") + "\n";
            if (context.getSessionData(CK.REW_ITEMS) == null) {
                text += GRAY + " (" + Lang.get("noneSet") + ")\n";
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("reqAddItem") + "\n";
                text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("done");
            } else {

                for (ItemStack is : getItems(context)) {

                    text += GRAY + "- " + ItemUtil.getDisplayString(is) + "\n";

                }
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("reqAddItem") + "\n";
                text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("done");

            }

            return text;

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new ItemStackPrompt(ItemListPrompt.this);
            } else if (input.equalsIgnoreCase("2")) {
                context.getForWhom().sendRawMessage(YELLOW + Lang.get("rewItemsCleared"));
                context.setSessionData(CK.REW_ITEMS, null);
                return new ItemListPrompt();
            } else if (input.equalsIgnoreCase("3")) {
                return new RewardsPrompt(quests, factory);
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
            lang1 = lang1.replaceAll("<comma>", BOLD + "" + RED + "comma" + RESET + YELLOW);
            String lang2 = Lang.get("rewCommandPromptHint");
            return YELLOW + lang1 + "\n" + lang2;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {

                String[] args = input.split(",");
                LinkedList<String> commands = new LinkedList<String>();
                for (String s : args) {

                    if (s.startsWith("/")) {
                        s = s.substring(1);
                    }

                    commands.add(s);

                }

                context.setSessionData(CK.REW_COMMAND, commands);

            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.REW_COMMAND, null);
            }

            return new RewardsPrompt(quests, factory);

        }

    }

    private class PermissionsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return YELLOW + Lang.get("rewPermissionsPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {

                String[] args = input.split(" ");
                LinkedList<String> permissions = new LinkedList<String>();
                permissions.addAll(Arrays.asList(args));

                context.setSessionData(CK.REW_PERMISSION, permissions);

            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
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

            String text = GOLD + Lang.get("mcMMORewardsTitle") + "\n";
            if (context.getSessionData(CK.REW_MCMMO_SKILLS) == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("reqSetSkills") + " (" + Lang.get("noneSet") + ")\n";
                text += GRAY + "2 - " + Lang.get("reqSetSkillAmounts") + " (" + Lang.get("rewNoMcMMOSkills") + ")\n";
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("done");
            } else {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("reqSetSkills") + "\n";
                for (String s : getSkills(context)) {

                    text += GRAY + "    - " + AQUA + s + "\n";

                }

                if (context.getSessionData(CK.REW_MCMMO_AMOUNTS) == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("reqSetSkillAmounts") + " (" + Lang.get("noneSet") + ")\n";
                } else {

                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("reqSetSkillAmounts") + "\n";
                    for (Integer i : getSkillAmounts(context)) {

                        text += GRAY + "    - " + AQUA + i + "\n";

                    }

                }

                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("done");

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
                    context.getForWhom().sendRawMessage(RED + Lang.get("rewSetMcMMOSkillsFirst"));
                    return new mcMMOListPrompt();
                } else {
                    return new mcMMOAmountsPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                context.getForWhom().sendRawMessage(YELLOW + Lang.get("rewMcMMOCleared"));
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
                    context.getForWhom().sendRawMessage(RED + Lang.get("rewMcMMOListsNotSameSize"));
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

            String skillList = DARKGREEN + Lang.get("skillListTitle") + "\n";
            SkillType[] skills = SkillType.values();
            for (int i = 0; i < skills.length; i++) {

                if (i == (skills.length - 1)) {
                    skillList += GREEN + skills[i].getName() + "\n";
                } else {
                    skillList += GREEN + skills[i].getName() + "\n\n";
                }

            }

            return skillList + Lang.get("rewMcMMOPrompt") + "\n" + GOLD + Lang.get("rewMcMMOPromptHint");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                String[] args = input.split(" ");
                LinkedList<String> skills = new LinkedList<String>();
                for (String s : args) {

                    if (Quests.getMcMMOSkill(s) != null) {

                        if (skills.contains(s) == false) {
                            skills.add(Quester.getCapitalized(s));
                        } else {
                            context.getForWhom().sendRawMessage(RED + Lang.get("listDuplicate"));
                            return new mcMMOSkillsPrompt();
                        }

                    } else {
                        String text = Lang.get("reqMcMMOError");
                        text = text.replaceAll("<input>", PINK + s + RED);
                        context.getForWhom().sendRawMessage(RED + text);
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
            return YELLOW + Lang.get("reqMcMMOAmountsPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                String[] args = input.split(" ");
                LinkedList<Integer> amounts = new LinkedList<Integer>();
                for (String s : args) {

                    try {

                        amounts.add(Integer.parseInt(s));

                    } catch (NumberFormatException e) {
                        String text = Lang.get("reqNotANumber");
                        text = text.replaceAll("<input>", PINK + s + RED);
                        context.getForWhom().sendRawMessage(RED + text);
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

            String text = GOLD + Lang.get("heroesRewardsTitle") + "\n";
            if (context.getSessionData(CK.REW_HEROES_CLASSES) == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("rewSetHeroesClasses") + " (" + Lang.get("noneSet") + ")\n";
                text += GRAY + "2 - " + Lang.get("rewSetHeroesAmounts") + "(" + Lang.get("rewNoHeroesClasses") + ")\n";
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("done");
            } else {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("rewSetHeroesClasses") + "\n";
                for (String s : getClasses(context)) {

                    text += GRAY + "    - " + AQUA + s + "\n";

                }

                if (context.getSessionData(CK.REW_HEROES_AMOUNTS) == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("rewSetHeroesAmounts") + " (" + Lang.get("noneSet") + ")\n";
                } else {

                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("rewSetHeroesAmounts") + "\n";
                    for (Double d : getClassAmounts(context)) {

                        text += GRAY + "    - " + AQUA + d + "\n";

                    }

                }

                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("done");

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
                    context.getForWhom().sendRawMessage(RED + Lang.get("rewSetHeroesClassesFirst"));
                    return new HeroesListPrompt();
                } else {
                    return new HeroesExperiencePrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                context.getForWhom().sendRawMessage(YELLOW + Lang.get("rewHeroesCleared"));
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
                    context.getForWhom().sendRawMessage(RED + Lang.get("rewHeroesListsNotSameSize"));
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

            String text = PURPLE + Lang.get("heroesClassesTitle") + "\n";
            LinkedList<String> list = new LinkedList<String>();
            for (HeroClass hc : Quests.heroes.getClassManager().getClasses()) {
                list.add(hc.getName());
            }

            if (list.isEmpty()) {
                text += GRAY + "(" + Lang.get("none") + ")\n";
            } else {

                Collections.sort(list);

                for (String s : list) {
                    text += PINK + s + ", ";
                }

                text = text.substring(0, text.length() - 2) + "\n";

            }

            text += YELLOW + Lang.get("rewHeroesClassesPrompt");

            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext cc, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                String[] arr = input.split(" ");
                LinkedList<String> classes = new LinkedList<String>();

                for (String s : arr) {

                    HeroClass hc = Quests.heroes.getClassManager().getClass(s);
                    if (hc == null) {
                        String text = Lang.get("rewHeroesInvalidClass");
                        text = text.replaceAll("<input>", PINK + s + RED);
                        cc.getForWhom().sendRawMessage(RED + text);
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

            text += YELLOW + Lang.get("rewHeroesExperiencePrompt");

            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext cc, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                String[] arr = input.split(" ");
                LinkedList<Double> amounts = new LinkedList<Double>();

                for (String s : arr) {

                    try {

                        double d = Double.parseDouble(s);
                        amounts.add(d);

                    } catch (NumberFormatException nfe) {
                        String text = Lang.get("reqNotANumber");
                        text = text.replaceAll("<input>", PINK + s + RED);
                        cc.getForWhom().sendRawMessage(RED + text);
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

            String text = DARKAQUA + Lang.get("phatLootsRewardsTitle") + "\n";

            for (PhatLoot pl : PhatLootsAPI.getAllPhatLoots()) {

                text += GRAY + "- " + BLUE + pl.name + "\n";

            }

            text += YELLOW + Lang.get("rewPhatLootsPrompt");

            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext cc, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {

                String[] arr = input.split(" ");
                LinkedList<String> loots = new LinkedList<String>();

                for (String s : arr) {

                    if (PhatLootsAPI.getPhatLoot(s) == null) {
                        String text = Lang.get("rewPhatLootsInvalid");
                        text = text.replaceAll("<input>", DARKRED + s + RED);
                        cc.getForWhom().sendRawMessage(RED + text);
                        return new PhatLootsPrompt();
                    }

                }

                loots.addAll(Arrays.asList(arr));
                cc.setSessionData(CK.REW_PHAT_LOOTS, loots);
                return new RewardsPrompt(quests, factory);

            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {

                cc.setSessionData(CK.REW_PHAT_LOOTS, null);
                cc.getForWhom().sendRawMessage(YELLOW + Lang.get("rewPhatLootsCleared"));
                return new RewardsPrompt(quests, factory);

            } else {
                return new RewardsPrompt(quests, factory);
            }

        }
    }

    private class CustomRewardsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String text = PINK + Lang.get("customRewardsTitle") + "\n";
            if (quests.customRewards.isEmpty()) {
                text += BOLD + "" + PURPLE + "(" + Lang.get("stageEditorNoModules") + ")";
            } else {
                for (CustomReward cr : quests.customRewards) {
                    text += PURPLE + " - " + cr.getName() + "\n";
                }
            }

            return text + YELLOW + Lang.get("rewCustomRewardPrompt");
        }

        @SuppressWarnings("unchecked")
		@Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {

                CustomReward found = null;
                for (CustomReward cr : quests.customRewards) {
                    if (cr.getName().equalsIgnoreCase(input)) {
                        found = cr;
                        break;
                    }
                }

                if (found == null) {
                    for (CustomReward cr : quests.customRewards) {
                        if (cr.getName().toLowerCase().contains(input.toLowerCase())) {
                            found = cr;
                            break;
                        }
                    }
                }

                if (found != null) {

                    if (context.getSessionData(CK.REW_CUSTOM) != null) {
                        LinkedList<String> list = (LinkedList<String>) context.getSessionData(CK.REW_CUSTOM);
                        LinkedList<Map<String, Object>> datamapList = (LinkedList<Map<String, Object>>) context.getSessionData(CK.REW_CUSTOM_DATA);
                        if (list.contains(found.getName()) == false) {
                            list.add(found.getName());
                            datamapList.add(found.datamap);
                            context.setSessionData(CK.REW_CUSTOM, list);
                            context.setSessionData(CK.REW_CUSTOM_DATA, datamapList);
                        } else {
                            context.getForWhom().sendRawMessage(YELLOW + Lang.get("rewCustomAlreadyAdded"));
                            return new CustomRewardsPrompt();
                        }
                    } else {
                        LinkedList<Map<String, Object>> datamapList = new LinkedList<Map<String, Object>>();
                        datamapList.add(found.datamap);
                        LinkedList<String> list = new LinkedList<String>();
                        list.add(found.getName());
                        context.setSessionData(CK.REW_CUSTOM, list);
                        context.setSessionData(CK.REW_CUSTOM_DATA, datamapList);
                    }

                    //Send user to the custom data prompt if there is any needed
                    if (found.datamap.isEmpty() == false) {

                        context.setSessionData(CK.REW_CUSTOM_DATA_DESCRIPTIONS, found.descriptions);
                        return new RewardCustomDataListPrompt();

                    }
                    //

                } else {
                    context.getForWhom().sendRawMessage(YELLOW + Lang.get("rewCustomNotFound"));
                    return new CustomRewardsPrompt();
                }

            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.REW_CUSTOM, null);
                context.setSessionData(CK.REW_CUSTOM_DATA, null);
                context.setSessionData(CK.REW_CUSTOM_DATA_TEMP, null);
                context.getForWhom().sendRawMessage(YELLOW + Lang.get("rewCustomCleared"));
            }

            return new RewardsPrompt(quests, factory);

        }
    }

    private class RewardCustomDataListPrompt extends StringPrompt {

        @SuppressWarnings("unchecked")
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
            for (String key : datamap.keySet()) {
                datamapKeys.add(key);
            }
            Collections.sort(datamapKeys);

            for (String dataKey : datamapKeys) {

                text += BOLD + "" + DARKBLUE + index + " - " + RESET + BLUE + dataKey;
                if (datamap.get(dataKey) != null) {
                    text += GREEN + " (" + (String) datamap.get(dataKey) + ")\n";
                } else {
                    text += RED + " (" + Lang.get("valRequired") + ")\n";
                }

                index++;

            }

            text += BOLD + "" + DARKBLUE + index + " - " + AQUA + Lang.get("finish");

            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            @SuppressWarnings("unchecked")
			LinkedList<Map<String, Object>> datamapList = (LinkedList<Map<String, Object>>) context.getSessionData(CK.REW_CUSTOM_DATA);
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
                    return new RewardsPrompt(quests, factory);
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
			Map<String, String> descriptions = (Map<String, String>) context.getSessionData(CK.REW_CUSTOM_DATA_DESCRIPTIONS);
            if (descriptions.get(temp) != null) {
                text += GOLD + descriptions.get(temp) + "\n";
            }

            String lang = Lang.get("stageEditorCustomDataPrompt");
            lang = lang.replaceAll("<data>", temp);
            text += YELLOW + lang;
            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            @SuppressWarnings("unchecked")
			LinkedList<Map<String, Object>> datamapList = (LinkedList<Map<String, Object>>) context.getSessionData(CK.REW_CUSTOM_DATA);
            Map<String, Object> datamap = datamapList.getLast();
            datamap.put((String) context.getSessionData(CK.REW_CUSTOM_DATA_TEMP), input);
            context.setSessionData(CK.REW_CUSTOM_DATA_TEMP, null);
            return new RewardCustomDataListPrompt();
        }

    }

}
