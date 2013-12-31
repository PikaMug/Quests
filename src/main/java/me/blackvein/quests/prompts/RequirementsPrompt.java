package me.blackvein.quests.prompts;

import com.herocraftonline.heroes.characters.classes.HeroClass;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import me.blackvein.quests.util.ColorUtil;
import me.blackvein.quests.Quest;
import me.blackvein.quests.QuestFactory;
import me.blackvein.quests.Quests;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.FixedSetPrompt;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.inventory.ItemStack;

public class RequirementsPrompt extends FixedSetPrompt implements ColorUtil {

    Quests quests;
    final QuestFactory factory;

    public RequirementsPrompt(Quests plugin, QuestFactory qf) {

        super("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
        quests = plugin;
        factory = qf;

    }

    @Override
    public String getPromptText(ConversationContext context) {

        String text;

        text = DARKAQUA + "- " + AQUA + context.getSessionData(CK.Q_NAME) + AQUA + " | Requirements -\n";

        if (context.getSessionData(CK.REQ_MONEY) == null) {
            text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set money requirement " + GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            int moneyReq = (Integer) context.getSessionData(CK.REQ_MONEY);
            text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set money requirement (" + moneyReq + " " + (moneyReq > 1 ? Quests.getCurrency(true) : Quests.getCurrency(false)) + ")\n";
        }

        if (context.getSessionData(CK.REQ_QUEST_POINTS) == null) {
            text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set Quest Points requirement " + GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set Quest Points requirement " + GRAY + "(" + AQUA + context.getSessionData(CK.REQ_QUEST_POINTS) + " Quest Points" + GRAY + ")\n";
        }

        text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Set item requirements\n";

        if (context.getSessionData(CK.REQ_PERMISSION) == null) {
            text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Set permission requirements " + GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Set permission requirements\n";
            List<String> perms = (List<String>) context.getSessionData(CK.REQ_PERMISSION);

            for (String s : perms) {

                text += GRAY + "    - " + AQUA + s + "\n";

            }
        }

        if (context.getSessionData(CK.REQ_QUEST) == null) {
            text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - Set Quest requirements " + GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - Set Quest requirements\n";
            List<String> qs = (List<String>) context.getSessionData(CK.REQ_QUEST);

            for (String s : qs) {

                text += GRAY + "    - " + AQUA + s + "\n";

            }
        }

        if (context.getSessionData(CK.REQ_QUEST_BLOCK) == null) {
            text += BLUE + "" + BOLD + "6" + RESET + YELLOW + " - Set Quest blocks " + GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += BLUE + "" + BOLD + "6" + RESET + YELLOW + " - Set Quest blocks\n";
            List<String> qs = (List<String>) context.getSessionData(CK.REQ_QUEST_BLOCK);

            for (String s : qs) {

                text += GRAY + "    - " + AQUA + s + "\n";

            }
        }

        if (Quests.mcmmo != null) {

            if (context.getSessionData(CK.REQ_MCMMO_SKILLS) == null) {
                text += BLUE + "" + BOLD + "7" + RESET + YELLOW + " - Set mcMMO requirements " + GRAY + " (" + Lang.get("noneSet") + ")\n";
            } else {
                text += BLUE + "" + BOLD + "7" + RESET + YELLOW + " - Set mcMMO requirements\n";
                List<String> skills = (List<String>) context.getSessionData(CK.REQ_MCMMO_SKILLS);
                List<Integer> amounts = (List<Integer>) context.getSessionData(CK.REQ_MCMMO_SKILL_AMOUNTS);

                for (String s : skills) {
                    text += GRAY + "    - " + DARKGREEN + s + RESET + YELLOW + " level " + GREEN + amounts.get(skills.indexOf(s)) + "\n";
                }
            }

        } else {
            text += GRAY + "6 - Set mcMMO requirements (mcMMO not installed)\n";
        }

        if (Quests.heroes != null) {

            if (context.getSessionData(CK.REQ_HEROES_PRIMARY_CLASS) == null && context.getSessionData(CK.REQ_HEROES_SECONDARY_CLASS) == null) {
                text += BLUE + "" + BOLD + "8" + RESET + YELLOW + " - Set Heroes requirements " + GRAY + " (" + Lang.get("noneSet") + ")\n";
            } else {
                text += BLUE + "" + BOLD + "8" + RESET + YELLOW + " - Set Heroes requirements\n";

                if (context.getSessionData(CK.REQ_HEROES_PRIMARY_CLASS) != null) {
                    text += AQUA + "    Primary Class: " + BLUE + (String) context.getSessionData(CK.REQ_HEROES_PRIMARY_CLASS) + "\n";
                }

                if (context.getSessionData(CK.REQ_HEROES_SECONDARY_CLASS) != null) {
                    text += AQUA + "    Secondary Class: " + BLUE + (String) context.getSessionData(CK.REQ_HEROES_SECONDARY_CLASS) + "\n";
                }
            }

        } else {
            text += GRAY + "8 - Set Heroes requirements (Heroes not installed)\n";
        }

        if (context.getSessionData(CK.REQ_MONEY) == null && context.getSessionData(CK.REQ_QUEST_POINTS) == null && context.getSessionData(CK.REQ_QUEST_BLOCK) == null && context.getSessionData(CK.REQ_ITEMS) == null && context.getSessionData(CK.REQ_PERMISSION) == null && context.getSessionData(CK.REQ_QUEST) == null && context.getSessionData(CK.REQ_QUEST_BLOCK) == null && context.getSessionData(CK.REQ_MCMMO_SKILLS) == null && context.getSessionData(CK.REQ_HEROES_PRIMARY_CLASS) == null && context.getSessionData(CK.REQ_HEROES_SECONDARY_CLASS) == null) {
            text += GRAY + "" + BOLD + "9 - " + RESET + GRAY + "Set fail requirements message (No requirements set)\n";
        } else if (context.getSessionData(CK.Q_FAIL_MESSAGE) == null) {
            text += RED + "" + BOLD + "9 - " + RESET + RED + "Set fail requirements message (Required)\n";
        } else {
            text += BLUE + "" + BOLD + "9 - " + RESET + YELLOW + "Set fail requirements message" + GRAY + "(" + AQUA + "\"" + context.getSessionData(CK.Q_FAIL_MESSAGE) + "\"" + GRAY + ")\n";
        }

        text += GREEN + "" + BOLD + "10" + RESET + YELLOW + " - Done";

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
            return new PermissionsPrompt();
        } else if (input.equalsIgnoreCase("5")) {
            return new QuestListPrompt(true);
        } else if (input.equalsIgnoreCase("6")) {
            return new QuestListPrompt(false);
        } else if (input.equalsIgnoreCase("7")) {
            if (Quests.mcmmo != null) {
                return new mcMMOPrompt();
            } else {
                return new RequirementsPrompt(quests, factory);
            }
        } else if (input.equalsIgnoreCase("8")) {
            if (Quests.heroes != null) {
                return new HeroesPrompt();
            } else {
                return new RequirementsPrompt(quests, factory);
            }
        } else if (input.equalsIgnoreCase("9")) {
            return new FailMessagePrompt();
        } else if (input.equalsIgnoreCase("10")) {
            if (context.getSessionData(CK.REQ_MONEY) != null || context.getSessionData(CK.REQ_QUEST_POINTS) != null || context.getSessionData(CK.REQ_ITEMS) != null || context.getSessionData(CK.REQ_PERMISSION) != null || context.getSessionData(CK.REQ_QUEST) != null || context.getSessionData(CK.REQ_QUEST_BLOCK) != null || context.getSessionData(CK.REQ_MCMMO_SKILLS) != null || context.getSessionData(CK.REQ_HEROES_PRIMARY_CLASS) != null || context.getSessionData(CK.REQ_HEROES_SECONDARY_CLASS) != null) {

                if (context.getSessionData(CK.Q_FAIL_MESSAGE) == null) {
                    context.getForWhom().sendRawMessage(RED + "You must set a fail requirements message!");
                    return new RequirementsPrompt(quests, factory);
                }

            }

            return factory.returnToMenu();
        }
        return null;

    }

    private class MoneyPrompt extends NumericPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + "Enter amount of " + PURPLE + ((Quests.economy.currencyNamePlural().isEmpty() ? "Money" : Quests.economy.currencyNamePlural())) + YELLOW + ", or 0 to clear the money requirement, or -1 to cancel";

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {

            if (input.intValue() < -1) {
                context.getForWhom().sendRawMessage(RED + "Amount must be greater than 0!");
                return new MoneyPrompt();
            } else if (input.intValue() == -1) {
                return new RequirementsPrompt(quests, factory);
            } else if (input.intValue() == 0) {
                context.setSessionData(CK.REQ_MONEY, null);
                return new RequirementsPrompt(quests, factory);
            }

            context.setSessionData(CK.REQ_MONEY, input.intValue());
            return new RequirementsPrompt(quests, factory);

        }
    }

    private class QuestPointsPrompt extends NumericPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + "Enter amount of Quest Points, or 0 to clear the Quest Point requirement,\nor -1 to cancel";

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {

            if (input.intValue() < -1) {
                context.getForWhom().sendRawMessage(RED + "Amount must be greater than 0!");
                return new QuestPointsPrompt();
            } else if (input.intValue() == -1) {
                return new RequirementsPrompt(quests, factory);
            } else if (input.intValue() == 0) {
                context.setSessionData(CK.REQ_QUEST_POINTS, null);
                return new RequirementsPrompt(quests, factory);
            }

            context.setSessionData(CK.REQ_QUEST_POINTS, input.intValue());
            return new RequirementsPrompt(quests, factory);

        }
    }

    private class QuestListPrompt extends StringPrompt {

        private final boolean isRequiredQuest;

        /*public QuestListPrompt() {
         this.isRequiredQuest = true;
         }*/
        public QuestListPrompt(boolean isRequired) {
            this.isRequiredQuest = isRequired;
        }

        @Override
        public String getPromptText(ConversationContext context) {

            String text = PINK + "- Quests -\n" + PURPLE;

            boolean none = true;
            for (Quest q : quests.getQuests()) {

                text += q.getName() + ", ";
                none = false;

            }

            if (none) {
                text += "(None)\n";
            } else {
                text = text.substring(0, (text.length() - 2));
                text += "\n";
            }

            text += YELLOW + "Enter a list of Quest names separating each one by a " + RED + BOLD + "comma" + RESET + YELLOW + ", or enter \'clear\' to clear the list, or \'cancel\' to return.";

            return text;

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("cancel") == false && input.equalsIgnoreCase("clear") == false) {

                String[] args = input.split(",");
                LinkedList<String> questNames = new LinkedList<String>();

                for (String s : args) {

                    if (quests.getQuest(s) == null) {

                        context.getForWhom().sendRawMessage(PINK + s + " " + RED + "is not a Quest name!");
                        return new QuestListPrompt(isRequiredQuest);

                    }

                    if (questNames.contains(s)) {

                        context.getForWhom().sendRawMessage(RED + "List contains duplicates!");
                        return new QuestListPrompt(isRequiredQuest);

                    }

                    questNames.add(s);

                }

                Collections.sort(questNames, new Comparator<String>() {
                    @Override
                    public int compare(String one, String two) {

                        return one.compareTo(two);

                    }
                });

                if (isRequiredQuest) {
                    context.setSessionData(CK.REQ_QUEST, questNames);
                } else {
                    context.setSessionData(CK.REQ_QUEST_BLOCK, questNames);
                }

            } else if (input.equalsIgnoreCase("clear")) {

                if (isRequiredQuest) {
                    context.setSessionData(CK.REQ_QUEST, null);
                } else {
                    context.setSessionData(CK.REQ_QUEST_BLOCK, null);
                }

            }

            return new RequirementsPrompt(quests, factory);

        }
    }

    private class ItemListPrompt extends FixedSetPrompt {

        public ItemListPrompt() {

            super("1", "2", "3", "4");

        }

        @Override
        public String getPromptText(ConversationContext context) {

            // Check/add newly made item
            if (context.getSessionData("newItem") != null) {
                if (context.getSessionData(CK.REQ_ITEMS) != null) {
                    List<ItemStack> itemRews = getItems(context);
                    itemRews.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(CK.REQ_ITEMS, itemRews);
                } else {
                    LinkedList<ItemStack> itemRews = new LinkedList<ItemStack>();
                    itemRews.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(CK.REQ_ITEMS, itemRews);
                }

                context.setSessionData("newItem", null);
                context.setSessionData("tempStack", null);

            }

            String text = GOLD + "- Item Requirements -\n";
            if (context.getSessionData(CK.REQ_ITEMS) == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Add item\n";
                text += GRAY + "2 - Set remove items (No items set)\n";
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Done";
            } else {

                for (ItemStack is : getItems(context)) {

                    text += GRAY + "    - " + ItemUtil.getDisplayString(is) + "\n";

                }

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Add item\n";

                if (context.getSessionData(CK.REQ_ITEMS_REMOVE) == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set remove items (No values set)\n";
                } else {

                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set remove items\n";
                    for (Boolean b : getRemoveItems(context)) {

                        text += GRAY + "    - " + AQUA + b.toString().toLowerCase() + "\n";

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
                return new ItemStackPrompt(ItemListPrompt.this);
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(CK.REQ_ITEMS) == null) {
                    context.getForWhom().sendRawMessage(RED + "You must add at least one item first!");
                    return new ItemListPrompt();
                } else {
                    return new RemoveItemsPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                context.getForWhom().sendRawMessage(YELLOW + "Item requirements cleared.");
                context.setSessionData(CK.REQ_ITEMS, null);
                context.setSessionData(CK.REQ_ITEMS_REMOVE, null);
                return new ItemListPrompt();
            } else if (input.equalsIgnoreCase("4")) {

                int one;
                int two;

                if (context.getSessionData(CK.REQ_ITEMS) != null) {
                    one = ((List<ItemStack>) context.getSessionData(CK.REQ_ITEMS)).size();
                } else {
                    one = 0;
                }

                if (context.getSessionData(CK.REQ_ITEMS_REMOVE) != null) {
                    two = ((List<Boolean>) context.getSessionData(CK.REQ_ITEMS_REMOVE)).size();
                } else {
                    two = 0;
                }

                if (one == two) {
                    return new RequirementsPrompt(quests, factory);
                } else {
                    context.getForWhom().sendRawMessage(RED + "The " + GOLD + "items list " + RED + "and " + GOLD + "remove items list " + RED + "are not the same size!");
                    return new ItemListPrompt();
                }
            }
            return null;

        }

        private List<ItemStack> getItems(ConversationContext context) {
            return (List<ItemStack>) context.getSessionData(CK.REQ_ITEMS);
        }

        private List<Boolean> getRemoveItems(ConversationContext context) {
            return (List<Boolean>) context.getSessionData(CK.REQ_ITEMS_REMOVE);
        }
    }

    private class RemoveItemsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return YELLOW + "Enter a list of true/false values, separating each one by a space, or enter \'cancel\' to return.";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("cancel") == false) {

                String[] args = input.split(" ");
                LinkedList<Boolean> booleans = new LinkedList<Boolean>();

                for (String s : args) {

                    if (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("yes")) {
                        booleans.add(true);
                    } else if (s.equalsIgnoreCase("false") || s.equalsIgnoreCase("no")) {
                        booleans.add(false);
                    } else {
                        context.getForWhom().sendRawMessage(PINK + s + RED + " is not a true or false value!\n " + GOLD + "Example: true false true true");
                        return new RemoveItemsPrompt();
                    }

                }

                context.setSessionData(CK.REQ_ITEMS_REMOVE, booleans);

            }

            return new ItemListPrompt();

        }
    }

    private class PermissionsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return YELLOW + "Enter permission requirements separating each one by a space, or enter \'clear\' to clear the list, or enter \'cancel\' to return.";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("cancel") == false && input.equalsIgnoreCase("clear") == false) {

                String[] args = input.split(" ");
                LinkedList<String> permissions = new LinkedList<String>();
                permissions.addAll(Arrays.asList(args));

                context.setSessionData(CK.REQ_PERMISSION, permissions);

            } else if (input.equalsIgnoreCase("clear")) {
                context.setSessionData(CK.REQ_PERMISSION, null);
            }

            return new RequirementsPrompt(quests, factory);

        }
    }

    private class mcMMOPrompt extends FixedSetPrompt {

        public mcMMOPrompt() {
            super("1", "2", "3");
        }

        @Override
        public String getPromptText(ConversationContext cc) {

            String text = DARKGREEN + "- mcMMO Requirements -\n";
            if (cc.getSessionData(CK.REQ_MCMMO_SKILLS) == null) {
                text += BOLD + "" + GREEN + "1" + RESET + GREEN + " - Set skills (None set)\n";
            } else {
                text += BOLD + "" + GREEN + "1" + RESET + GREEN + " - Set skills\n";
                LinkedList<String> skills = (LinkedList<String>) cc.getSessionData(CK.REQ_MCMMO_SKILLS);
                for (String skill : skills) {
                    text += GRAY + "    - " + AQUA + skill + "\n";
                }
            }

            if (cc.getSessionData(CK.REQ_MCMMO_SKILL_AMOUNTS) == null) {
                text += BOLD + "" + GREEN + "2" + RESET + GREEN + " - Set skill amounts (None set)\n";
            } else {
                text += BOLD + "" + GREEN + "2" + RESET + GREEN + " - Set skill amounts\n";
                LinkedList<Integer> amounts = (LinkedList<Integer>) cc.getSessionData(CK.REQ_MCMMO_SKILL_AMOUNTS);
                for (int i : amounts) {
                    text += GRAY + "    - " + AQUA + i + "\n";
                }
            }

            text += BOLD + "" + GREEN + "3" + RESET + GREEN + " - Done";

            return text;
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext cc, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new mcMMOSkillsPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                return new mcMMOAmountsPrompt();
            } else if (input.equalsIgnoreCase("3")) {
                return new RequirementsPrompt(quests, factory);
            }

            return null;

        }
    }

    private class mcMMOSkillsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String skillList
                    = DARKGREEN + "-Skill List-\n"
                    + GREEN + "Acrobatics\n"
                    + GREEN + "All\n"
                    + GREEN + "Archery\n"
                    + GREEN + "Axes\n"
                    + GREEN + "Excavation\n"
                    + GREEN + "Fishing\n"
                    + GREEN + "Herbalism\n"
                    + GREEN + "Mining\n"
                    + GREEN + "Repair\n"
                    + GREEN + "Smelting\n"
                    + GREEN + "Swords\n"
                    + GREEN + "Taming\n"
                    + GREEN + "Unarmed\n"
                    + GREEN + "Woodcutting\n\n";

            return skillList + YELLOW + "Enter mcMMO skills, separating each one by a space, or enter \'clear\' to clear the list, "
                    + "or \'cancel\' to return.\n";
        }

        @Override
        public Prompt acceptInput(ConversationContext cc, String input) {

            if (input.equalsIgnoreCase("cancel") == false && input.equalsIgnoreCase("clear") == false) {

                LinkedList<String> skills = new LinkedList<String>();

                for (String s : input.split(" ")) {

                    String formatted = MiscUtil.getCapitalized(s);

                    if (Quests.getMcMMOSkill(formatted) != null) {
                        skills.add(formatted);
                    } else if (skills.contains(formatted)) {
                        cc.getForWhom().sendRawMessage(YELLOW + "Error: List contains duplicates!");
                        return new mcMMOSkillsPrompt();
                    } else {
                        cc.getForWhom().sendRawMessage(YELLOW + "Error: " + RED + s + YELLOW + " is not an mcMMO skill name!");
                        return new mcMMOSkillsPrompt();
                    }

                }

                cc.setSessionData(CK.REQ_MCMMO_SKILLS, skills);
                return new mcMMOPrompt();

            } else if (input.equalsIgnoreCase("clear")) {
                cc.getForWhom().sendRawMessage(YELLOW + "mcMMO skill requirements cleared.");
                cc.setSessionData(CK.REQ_MCMMO_SKILLS, null);
                return new mcMMOPrompt();
            } else if (input.equalsIgnoreCase("cancel")) {
                return new mcMMOPrompt();
            }

            return new mcMMOSkillsPrompt();

        }

    }

    private class mcMMOAmountsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + "Enter mcMMO skill amounts, separating each one by a space, or enter \'clear\' to clear the list, "
                    + "or \'cancel\' to return.\n";
        }

        @Override
        public Prompt acceptInput(ConversationContext cc, String input) {

            if (input.equalsIgnoreCase("cancel") == false && input.equalsIgnoreCase("clear") == false) {

                LinkedList<Integer> amounts = new LinkedList<Integer>();

                for (String s : input.split(" ")) {

                    try {

                        int i = Integer.parseInt(s);
                        amounts.add(i);

                    } catch (NumberFormatException nfe) {
                        cc.getForWhom().sendRawMessage(YELLOW + "Error: " + RED + s + YELLOW + " is not a number!");
                        return new mcMMOAmountsPrompt();
                    }

                }

                cc.setSessionData(CK.REQ_MCMMO_SKILL_AMOUNTS, amounts);
                return new mcMMOPrompt();

            } else if (input.equalsIgnoreCase("clear")) {
                cc.getForWhom().sendRawMessage(YELLOW + "mcMMO skill amount requirements cleared.");
                cc.setSessionData(CK.REQ_MCMMO_SKILL_AMOUNTS, null);
                return new mcMMOPrompt();
            } else if (input.equalsIgnoreCase("cancel")) {
                return new mcMMOPrompt();
            }

            return new mcMMOAmountsPrompt();

        }

    }

    private class HeroesPrompt extends FixedSetPrompt {

        public HeroesPrompt() {
            super("1", "2", "3");
        }

        @Override
        public String getPromptText(ConversationContext cc) {

            String text = DARKGREEN + "- Heroes Requirements -\n";
            if (cc.getSessionData(CK.REQ_HEROES_PRIMARY_CLASS) == null) {
                text += BOLD + "" + GREEN + "1" + RESET + GREEN + " - Set Primary Class (None set)\n";
            } else {
                text += BOLD + "" + GREEN + "1" + RESET + GREEN + " - Set Primary Class (" + AQUA + (String) cc.getSessionData(CK.REQ_HEROES_PRIMARY_CLASS) + GREEN + ")\n";
            }

            if (cc.getSessionData(CK.REQ_HEROES_SECONDARY_CLASS) == null) {
                text += BOLD + "" + GREEN + "2" + RESET + GREEN + " - Set Secondary Class (None set)\n";
            } else {
                text += BOLD + "" + GREEN + "2" + RESET + GREEN + " - Set Secondary Class (" + AQUA + (String) cc.getSessionData(CK.REQ_HEROES_SECONDARY_CLASS) + GREEN + ")\n";
            }

            text += BOLD + "" + GREEN + "3" + RESET + GREEN + " - Done";

            return text;
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext cc, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new HeroesPrimaryPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                return new HeroesSecondaryPrompt();
            } else if (input.equalsIgnoreCase("3")) {
                return new RequirementsPrompt(quests, factory);
            }

            return null;

        }
    }

    private class HeroesPrimaryPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext cc) {

            String text = PURPLE + "- " + PINK + "Primary Classes" + PURPLE + " -\n";
            LinkedList<String> list = new LinkedList<String>();
            for (HeroClass hc : Quests.heroes.getClassManager().getClasses()) {
                if (hc.isPrimary()) {
                    list.add(hc.getName());
                }
            }

            if (list.isEmpty()) {
                text += GRAY + "(None)\n";
            } else {

                Collections.sort(list);

                for (String s : list) {
                    text += PURPLE + "- " + PINK + s + "\n";
                }

            }

            text += YELLOW + "Enter a Heroes Primary Class name, or enter \"clear\" to clear the requirement, or \"cancel\" to return.";

            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext cc, String input) {

            if (input.equalsIgnoreCase("clear") == false && input.equalsIgnoreCase("cancel") == false) {

                HeroClass hc = Quests.heroes.getClassManager().getClass(input);
                if (hc != null) {

                    if (hc.isPrimary()) {

                        cc.setSessionData(CK.REQ_HEROES_PRIMARY_CLASS, hc.getName());
                        return new HeroesPrompt();

                    } else {
                        cc.getForWhom().sendRawMessage(RED + "The " + PINK + hc.getName() + RED + " class is not primary!");
                        return new HeroesPrimaryPrompt();
                    }

                } else {
                    cc.getForWhom().sendRawMessage(RED + "Class not found!");
                    return new HeroesPrimaryPrompt();
                }

            } else if (input.equalsIgnoreCase("clear")) {

                cc.setSessionData(CK.REQ_HEROES_PRIMARY_CLASS, null);
                cc.getForWhom().sendRawMessage(YELLOW + "Heroes Primary Class requirement cleared.");
                return new HeroesPrompt();

            } else {

                return new HeroesPrompt();

            }

        }
    }
    
    private class HeroesSecondaryPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext cc) {

            String text = PURPLE + "- " + PINK + "Secondary Classes" + PURPLE + " -\n";
            LinkedList<String> list = new LinkedList<String>();
            for (HeroClass hc : Quests.heroes.getClassManager().getClasses()) {
                if (hc.isSecondary()) {
                    list.add(hc.getName());
                }
            }

            if (list.isEmpty()) {
                text += GRAY + "(None)\n";
            } else {

                Collections.sort(list);

                for (String s : list) {
                    text += PURPLE + "- " + PINK + s + "\n";
                }

            }

            text += YELLOW + "Enter a Heroes Secondary Class name, or enter \"clear\" to clear the requirement, or \"cancel\" to return.";

            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext cc, String input) {

            if (input.equalsIgnoreCase("clear") == false && input.equalsIgnoreCase("cancel") == false) {

                HeroClass hc = Quests.heroes.getClassManager().getClass(input);
                if (hc != null) {

                    if (hc.isSecondary()) {

                        cc.setSessionData(CK.REQ_HEROES_SECONDARY_CLASS, hc.getName());
                        return new HeroesPrompt();

                    } else {
                        cc.getForWhom().sendRawMessage(RED + "The " + PINK + hc.getName() + RED + " class is not secondary!");
                        return new HeroesSecondaryPrompt();
                    }

                } else {
                    cc.getForWhom().sendRawMessage(RED + "Class not found!");
                    return new HeroesSecondaryPrompt();
                }

            } else if (input.equalsIgnoreCase("clear")) {

                cc.setSessionData(CK.REQ_HEROES_SECONDARY_CLASS, null);
                cc.getForWhom().sendRawMessage(YELLOW + "Heroes Secondary Class requirement cleared.");
                return new HeroesPrompt();

            } else {

                return new HeroesPrompt();

            }

        }
    }

    private class FailMessagePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return YELLOW + "Enter fail requirements message, or enter \'cancel\' to return.";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("cancel") == false) {
                context.setSessionData(CK.Q_FAIL_MESSAGE, input);
            }

            return new RequirementsPrompt(quests, factory);

        }
    }
}
