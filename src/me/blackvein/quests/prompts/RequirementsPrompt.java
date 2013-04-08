package me.blackvein.quests.prompts;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import me.blackvein.quests.Quest;
import me.blackvein.quests.QuestFactory;
import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.*;


public class RequirementsPrompt extends FixedSetPrompt{

    Quests quests;

    static final ChatColor BOLD = ChatColor.BOLD;
    static final ChatColor AQUA = ChatColor.AQUA;
    static final ChatColor DARKAQUA = ChatColor.DARK_AQUA;
    static final ChatColor BLUE = ChatColor.BLUE;
    static final ChatColor GOLD = ChatColor.GOLD;
    static final ChatColor PINK = ChatColor.LIGHT_PURPLE;
    static final ChatColor PURPLE = ChatColor.DARK_PURPLE;
    static final ChatColor GREEN = ChatColor.GREEN;
    static final ChatColor DARKGREEN = ChatColor.DARK_GREEN;
    static final ChatColor RED = ChatColor.RED;
    static final ChatColor DARKRED = ChatColor.DARK_RED;
    static final ChatColor YELLOW = ChatColor.YELLOW;
    static final ChatColor GRAY = ChatColor.GRAY;
    static final ChatColor RESET = ChatColor.RESET;

    final QuestFactory factory;

    public RequirementsPrompt(Quests plugin, QuestFactory qf){

        super("1", "2", "3", "4", "5", "6", "7");
        quests = plugin;
        factory = qf;

    }

    @Override
    public String getPromptText(ConversationContext context){

        String text;

        text = DARKAQUA + "- " + AQUA + context.getSessionData("questName") + AQUA + " | Requirements -\n";

        if(context.getSessionData("moneyReq") == null)
            text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set money requirement " + GRAY + "(None set)\n";
        else{
            int moneyReq = (Integer) context.getSessionData("moneyReq");
            text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set money requirement (" + context.getSessionData("moneyReq") + " " + (moneyReq > 1 ? Quests.getCurrency(true) : Quests.getCurrency(false)) + ")\n";
        }

        if(context.getSessionData("questPointsReq") == null)
            text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set Quest Points requirement " + GRAY + "(None set)\n";
        else{
            text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set Quest Points requirement " + GRAY + "(" + AQUA + context.getSessionData("questPointsReq") + " Quest Points" + GRAY + ")\n";
        }

        if(context.getSessionData("itemIdReqs") == null)
            text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Set item requirements " + GRAY + "(None set)\n";
        else{
            text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Set item requirements\n";
            List<Integer> ids = (List<Integer>) context.getSessionData("itemIdReqs");
            List<Integer> amounts = (List<Integer>) context.getSessionData("itemAmountReqs");
            List<Boolean> removes = (List<Boolean>) context.getSessionData("removeItemReqs");

            for(int i : ids){

                text += GRAY + "    - " + AQUA + Quester.prettyItemString(i) + YELLOW + " x " + AQUA + amounts.get(ids.indexOf(i));
                if(removes.get(ids.indexOf(i)) == false)
                    text += GRAY + "(" + DARKRED + "Remove" + GRAY + ")\n";
                else
                    text += GRAY + "(" + DARKGREEN + "Keep" + GRAY + ")\n";

            }
        }

        if(context.getSessionData("permissionReqs") == null)
            text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Set permission requirements " + GRAY + "(None set)\n";
        else{
            text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Set permission requirements\n";
            List<String> perms = (List<String>) context.getSessionData("permissionReqs");

            for(String s : perms){

                text += GRAY + "    - " + AQUA + s + "\n";

            }
        }

        if(context.getSessionData("questReqs") == null)
            text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - Set Quest requirements " + GRAY + "(None set)\n";
        else{
            text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - Set Quest requirements\n";
            List<String> qs = (List<String>) context.getSessionData("questReqs");

            for(String s : qs){

                text += GRAY + "    - " + AQUA + s + "\n";

            }
        }

            if(context.getSessionData("moneyReq") == null && context.getSessionData("questPointsReq") == null && context.getSessionData("itemIdReqs") == null && context.getSessionData("permissionReqs") == null && context.getSessionData("questReqs") == null){
                text += GRAY + "" + BOLD + "6 - " + RESET + GRAY + "Set fail requirements message (No requirements set)\n";
            }else if(context.getSessionData("failMessage") == null){
                text += RED + "" + BOLD + "6 - " + RESET + RED + "Set fail requirements message (Required)\n";
            }else{
                text += BLUE + "" + BOLD + "6 - " + RESET + YELLOW + "Set fail requirements message" + GRAY + "(" + AQUA + "\"" + context.getSessionData("failMessage") + "\"" + GRAY + ")\n";
            }

        text += GREEN + "" + BOLD + "7" + RESET + YELLOW + " - Done";


        return text;

    }

    @Override
    protected Prompt acceptValidatedInput(ConversationContext context, String input){

        if(input.equalsIgnoreCase("1")){
            return new MoneyPrompt();
        }else if(input.equalsIgnoreCase("2")){
            return new QuestPointsPrompt();
        }else if(input.equalsIgnoreCase("3")){
            return new ItemListPrompt();
        }else if(input.equalsIgnoreCase("4")){
            return new PermissionsPrompt();
        }else if(input.equalsIgnoreCase("5")){
            return new QuestListPrompt();
        }else if(input.equalsIgnoreCase("6")){
            return new FailMessagePrompt();
        }else if(input.equalsIgnoreCase("7")){
            if(context.getSessionData("moneyReq") != null || context.getSessionData("questPointsReq") != null || context.getSessionData("itemIdReqs") != null || context.getSessionData("permissionReqs") != null || context.getSessionData("questReqs") != null){

                if(context.getSessionData("failMessage") == null){
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
        public String getPromptText(ConversationContext context){

            return YELLOW + "Enter amount of " + PURPLE + ((Quests.economy.currencyNamePlural().isEmpty() ? "Money" : Quests.economy.currencyNamePlural())) + YELLOW + ", or 0 to clear the money requirement, or -1 to cancel";

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input){

            if(input.intValue() < -1){
                context.getForWhom().sendRawMessage(RED + "Amount must be greater than 0!");
                return new MoneyPrompt();
            }else if(input.intValue() == -1){
                return new RequirementsPrompt(quests, factory);
            }else if(input.intValue() == 0){
                context.setSessionData("moneyReq", null);
                return new RequirementsPrompt(quests, factory);
            }

            context.setSessionData("moneyReq", input.intValue());
            return new RequirementsPrompt(quests, factory);

        }

    }

    private class QuestPointsPrompt extends NumericPrompt {

        @Override
        public String getPromptText(ConversationContext context){

            return YELLOW + "Enter amount of Quest Points, or 0 to clear the Quest Point requirement,\nor -1 to cancel";

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input){

            if(input.intValue() < -1){
                context.getForWhom().sendRawMessage(RED + "Amount must be greater than 0!");
                return new QuestPointsPrompt();
            }else if(input.intValue() == -1){
                return new RequirementsPrompt(quests, factory);
            }else if(input.intValue() == 0){
                context.setSessionData("questPointsReq", null);
                return new RequirementsPrompt(quests, factory);
            }

            context.setSessionData("questPointsReq", input.intValue());
            return new RequirementsPrompt(quests, factory);

        }

    }

    private class QuestListPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context){

            String text = PINK + "- Quests -\n" + PURPLE;

            boolean none = true;
            for(Quest q : quests.getQuests()){

                text += q.getName() + ", ";
                none = false;

            }

            if(none)
                text += "(None)\n";
            else{
                text = text.substring(0, (text.length() - 2));
                text += "\n";
            }

            text += YELLOW + "Enter a list of Quest names separating each one by a " + RED + BOLD + "comma" + RESET + YELLOW + ", or enter \'clear\' to clear the list, or \'cancel\' to return.";

            return text;

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input){

            if(input.equalsIgnoreCase("cancel") == false && input.equalsIgnoreCase("clear") == false){

                String[] args = input.split(",");
                LinkedList<String> questNames = new LinkedList<String>();

                for(String s : args){

                    if(quests.getQuest(s) == null){

                        context.getForWhom().sendRawMessage(PINK + s + " " + RED + "is not a Quest name!");
                        return new QuestListPrompt();

                    }

                    if(questNames.contains(s)){

                        context.getForWhom().sendRawMessage(RED + "List contains duplicates!");
                        return new QuestListPrompt();

                    }

                    questNames.add(s);

                }

                Collections.sort(questNames, new Comparator(){

                    @Override
                    public int compare(Object one, Object two){

                        String s = (String) one;
                        String s2 = (String) two;

                        return s.compareTo(s2);

                    }

                });

                context.setSessionData("questReqs", questNames);

            }else if(input.equalsIgnoreCase("clear")){

                context.setSessionData("questReqs", null);

            }

            return new RequirementsPrompt(quests, factory);

        }

    }

    private class ItemListPrompt extends FixedSetPrompt {

        public ItemListPrompt(){

            super("1", "2", "3", "4", "5");

        }

        @Override
        public String getPromptText(ConversationContext context){

            String text = GOLD + "- Item Requirements -\n";
            if(context.getSessionData("itemIdReqs") == null){
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set item IDs (None set)\n";
                text += GRAY + "2 - Set item amounts (No IDs set)\n";
                text += GRAY + "3 - Set remove items (No IDs set)\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - Done";
            }else{

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set item IDs\n";
                for(Integer i : getItemIds(context)){

                    text += GRAY + "    - " + AQUA + Quester.prettyItemString(i) + "\n";

                }

                if(context.getSessionData("itemAmountReqs") == null){
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set item amounts (None set)\n";
                }else{

                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set item amounts\n";
                    for(Integer i : getItemAmounts(context)){

                        text += GRAY + "    - " + AQUA + i + "\n";

                    }

                }

                if(context.getSessionData("removeItemReqs") == null){
                    text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Set remove items (None set)\n";
                }else{

                    text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Set remove items\n";
                    for(Boolean b : getRemoveItems(context)){

                        text += GRAY + "    - " + AQUA + b.toString().toLowerCase() + "\n";

                    }

                }

                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - Done";

            }

            return text;

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input){

            if(input.equalsIgnoreCase("1")){
                return new ItemIdsPrompt();
            }else if(input.equalsIgnoreCase("2")){
                if(context.getSessionData("itemIdReqs") == null){
                    context.getForWhom().sendRawMessage(RED + "You must set item IDs first!");
                    return new ItemListPrompt();
                }else{
                    return new ItemAmountsPrompt();
                }
            }else if(input.equalsIgnoreCase("3")){
                if(context.getSessionData("itemIdReqs") == null){
                    context.getForWhom().sendRawMessage(RED + "You must set item IDs first!");
                    return new ItemListPrompt();
                }else{
                    return new RemoveItemsPrompt();
                }
            }else if(input.equalsIgnoreCase("4")){
                context.getForWhom().sendRawMessage(YELLOW + "Item requirements cleared.");
                context.setSessionData("itemIdReqs", null);
                context.setSessionData("itemAmountReqs", null);
                context.setSessionData("removeItemReqs", null);
                return new ItemListPrompt();
            }else if(input.equalsIgnoreCase("5")){

                int one;
                int two;
                int three;

                if(context.getSessionData("itemIdReqs") != null)
                    one = ((List<Integer>) context.getSessionData("itemIdReqs")).size();
                else
                    one = 0;

                if(context.getSessionData("itemAmountReqs") != null)
                    two = ((List<Integer>) context.getSessionData("itemAmountReqs")).size();
                else
                    two = 0;

                if(context.getSessionData("removeItemReqs") != null)
                    three = ((List<Integer>) context.getSessionData("removeItemReqs")).size();
                else
                    three = 0;

                if(one == two && two == three)
                    return new RequirementsPrompt(quests, factory);
                else{
                    context.getForWhom().sendRawMessage(RED + "The " + GOLD + "item IDs list" + RED + ", " + GOLD + "item amounts list " + RED + "and " + GOLD + "remove items list " + RED + "are not the same size!");
                    return new ItemListPrompt();
                }
            }
            return null;

        }

        private List<Integer> getItemIds(ConversationContext context){
            return (List<Integer>) context.getSessionData("itemIdReqs");
        }

        private List<Integer> getItemAmounts(ConversationContext context){
            return (List<Integer>) context.getSessionData("itemAmountReqs");
        }

        private List<Boolean> getRemoveItems(ConversationContext context){
            return (List<Boolean>) context.getSessionData("removeItemReqs");
        }

    }

    private class ItemIdsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context){
            return YELLOW + "Enter item IDs, separating each one by a space, or enter \'cancel\' to return.";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input){

            if(input.equalsIgnoreCase("cancel") == false){

                String[] args = input.split(" ");
                LinkedList<Integer> ids = new LinkedList<Integer>();
                for(String s : args){

                    try{

                        if(Material.getMaterial(Integer.parseInt(s)) != null){

                            if(ids.contains(Integer.parseInt(s)) == false){
                                ids.add(Integer.parseInt(s));
                            }else{
                                context.getForWhom().sendRawMessage(RED + " List contains duplicates!");
                                return new ItemIdsPrompt();
                            }

                        }else{
                            context.getForWhom().sendRawMessage(PINK + s + RED + " is not a valid item ID!");
                            return new ItemIdsPrompt();
                        }

                    }catch (Exception e){
                        context.getForWhom().sendRawMessage(RED + "Invalid entry " + PINK + s + RED + ". Input was not a list of numbers!");
                        return new ItemIdsPrompt();
                    }

                }

                context.setSessionData("itemIdReqs", ids);

            }

            return new ItemListPrompt();

        }

    }

    private class ItemAmountsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context){
            return YELLOW + "Enter item amounts (numbers), separating each one by a space, or enter \'cancel\' to return.";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input){

            if(input.equalsIgnoreCase("cancel") == false){

                String[] args = input.split(" ");
                LinkedList<Integer> amounts = new LinkedList<Integer>();
                for(String s : args){

                    try{

                        if(Integer.parseInt(s) > 0)
                            amounts.add(Integer.parseInt(s));
                        else{
                            context.getForWhom().sendRawMessage(PINK + s + RED + " is not greater than 0!");
                            return new ItemAmountsPrompt();
                        }

                    }catch (Exception e){
                        context.getForWhom().sendRawMessage(RED + "Invalid entry " + PINK + s + RED + ". Input was not a list of numbers!");
                        return new ItemAmountsPrompt();
                    }

                }

                context.setSessionData("itemAmountReqs", amounts);

            }

            return new ItemListPrompt();

        }


    }

    private class RemoveItemsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context){
            return YELLOW + "Enter a list of true/false values, separating each one by a space, or enter \'cancel\' to return.";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input){

            if(input.equalsIgnoreCase("cancel") == false){

                String[] args = input.split(" ");
                LinkedList<Boolean> booleans = new LinkedList<Boolean>();

                for(String s : args){

                    if(s.equalsIgnoreCase("true") || s.equalsIgnoreCase("yes"))
                        booleans.add(true);
                    else if(s.equalsIgnoreCase("false") || s.equalsIgnoreCase("no"))
                        booleans.add(false);
                    else{
                        context.getForWhom().sendRawMessage(PINK + s + RED + " is not a true or false value!\n " + GOLD + "Example: true false true true");
                        return new RemoveItemsPrompt();
                    }

                }

                context.setSessionData("removeItemReqs", booleans);

            }

            return new ItemListPrompt();

        }


    }

    private class PermissionsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context){
            return YELLOW + "Enter permission requirements separating each one by a space, or enter \'clear\' to clear the list, or enter \'cancel\' to return.";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input){

            if(input.equalsIgnoreCase("cancel") == false && input.equalsIgnoreCase("clear") == false){

                String[] args = input.split(" ");
                LinkedList<String> permissions = new LinkedList<String>();
                permissions.addAll(Arrays.asList(args));

                context.setSessionData("permissionReqs", permissions);

            }else if(input.equalsIgnoreCase("clear")){
                context.setSessionData("permissionReqs", null);
            }

            return new RequirementsPrompt(quests, factory);

        }

    }

    private class FailMessagePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context){
            return YELLOW + "Enter fail requirements message, or enter \'cancel\' to return.";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input){

            if(input.equalsIgnoreCase("cancel") == false)
                context.setSessionData("failMessage", input);

            return new RequirementsPrompt(quests, factory);

        }

    }

}
