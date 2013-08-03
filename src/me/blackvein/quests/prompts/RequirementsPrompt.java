package me.blackvein.quests.prompts;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import me.blackvein.quests.ColorUtil;
import me.blackvein.quests.Quest;
import me.blackvein.quests.QuestFactory;
import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import org.bukkit.Material;
import org.bukkit.conversations.*;
import org.bukkit.inventory.ItemStack;


public class RequirementsPrompt extends FixedSetPrompt implements ColorUtil{

    Quests quests;

    final QuestFactory factory;

    public RequirementsPrompt(Quests plugin, QuestFactory qf){

        super("1", "2", "3", "4", "5", "6", "7", "8");
        quests = plugin;
        factory = qf;

    }

    @Override
    public String getPromptText(ConversationContext context){

        String text;

        text = DARKAQUA + "- " + AQUA + context.getSessionData("questName") + AQUA + " | Requirements -\n";

        if(context.getSessionData("moneyReq") == null)
            text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set money requirement " + GRAY + " (" + Lang.get("noneSet") + ")\n";
        else{
            int moneyReq = (Integer) context.getSessionData("moneyReq");
            text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set money requirement (" + moneyReq + " " + (moneyReq > 1 ? Quests.getCurrency(true) : Quests.getCurrency(false)) + ")\n";
        }

        if(context.getSessionData("questPointsReq") == null)
            text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set Quest Points requirement " + GRAY + " (" + Lang.get("noneSet") + ")\n";
        else{
            text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set Quest Points requirement " + GRAY + "(" + AQUA + context.getSessionData("questPointsReq") + " Quest Points" + GRAY + ")\n";
        }

        text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Set item requirements\n";

        if(context.getSessionData("permissionReqs") == null)
            text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Set permission requirements " + GRAY + " (" + Lang.get("noneSet") + ")\n";
        else{
            text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Set permission requirements\n";
            List<String> perms = (List<String>) context.getSessionData("permissionReqs");

            for(String s : perms){

                text += GRAY + "    - " + AQUA + s + "\n";

            }
        }

        if(context.getSessionData("questReqs") == null)
            text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - Set Quest requirements " + GRAY + " (" + Lang.get("noneSet") + ")\n";
        else{
            text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - Set Quest requirements\n";
            List<String> qs = (List<String>) context.getSessionData("questReqs");

            for(String s : qs){

                text += GRAY + "    - " + AQUA + s + "\n";

            }
        }
        
        if(context.getSessionData("questBlocks") == null)
            text += BLUE + "" + BOLD + "6" + RESET + YELLOW + " - Set Quest that mustn't be done " + GRAY + " (" + Lang.get("noneSet") + ")\n";
        else{
            text += BLUE + "" + BOLD + "6" + RESET + YELLOW + " - Set Quest requirements\n";
            List<String> qs = (List<String>) context.getSessionData("questBlocks");

            for(String s : qs){

                text += GRAY + "    - " + AQUA + s + "\n";

            }
        }

        if(context.getSessionData("moneyReq") == null && context.getSessionData("questPointsReq") == null && context.getSessionData("itemReqs") == null && context.getSessionData("permissionReqs") == null && context.getSessionData("questReqs") == null && context.getSessionData("questBlocks") == null){
        	text += GRAY + "" + BOLD + "7 - " + RESET + GRAY + "Set fail requirements message (No requirements set)\n";
        }else if(context.getSessionData("failMessage") == null){
        	text += RED + "" + BOLD + "7 - " + RESET + RED + "Set fail requirements message (Required)\n";
        }else{
        	text += BLUE + "" + BOLD + "7 - " + RESET + YELLOW + "Set fail requirements message" + GRAY + "(" + AQUA + "\"" + context.getSessionData("failMessage") + "\"" + GRAY + ")\n";
        }

        text += GREEN + "" + BOLD + "8" + RESET + YELLOW + " - Done";


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
            return new QuestListPrompt(true);
        }else if(input.equalsIgnoreCase("6")) {
        	return new QuestListPrompt(false);
        }else if(input.equalsIgnoreCase("7")){
            return new FailMessagePrompt();
        }else if(input.equalsIgnoreCase("8")){
            if(context.getSessionData("moneyReq") != null || context.getSessionData("questPointsReq") != null || context.getSessionData("itemReqs") != null || context.getSessionData("permissionReqs") != null || context.getSessionData("questReqs") != null || context.getSessionData("questBlocks") != null){

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
    	
    	private boolean isRequiredQuest;
    	
    	/*public QuestListPrompt() {
    		this.isRequiredQuest = true;
    	}*/
    	
    	public QuestListPrompt(boolean isRequired) {
    		this.isRequiredQuest = isRequired;
    	}

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
                        return new QuestListPrompt(isRequiredQuest);

                    }

                    if(questNames.contains(s)){

                        context.getForWhom().sendRawMessage(RED + "List contains duplicates!");
                        return new QuestListPrompt(isRequiredQuest);

                    }

                    questNames.add(s);

                }

                Collections.sort(questNames, new Comparator<String>(){

                    @Override
                    public int compare(String one, String two){

                        return one.compareTo(two);

                    }

                });

                if (isRequiredQuest) {
                	context.setSessionData("questReqs", questNames);
                } else {
                	context.setSessionData("questBlocks", questNames);
                }

            }else if(input.equalsIgnoreCase("clear")){

            	if (isRequiredQuest) {
                	context.setSessionData("questReqs", null);
                } else {
                	context.setSessionData("questBlocks", null);
                }

            }

            return new RequirementsPrompt(quests, factory);

        }

    }

    private class ItemListPrompt extends FixedSetPrompt {

        public ItemListPrompt(){

            super("1", "2", "3", "4");

        }

        @Override
        public String getPromptText(ConversationContext context){

            // Check/add newly made item
            if(context.getSessionData("newItem") != null){
                if(context.getSessionData("itemReqs") != null){
                    List<ItemStack> itemRews = getItems(context);
                    itemRews.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData("itemReqs", itemRews);
                }else{
                    LinkedList<ItemStack> itemRews = new LinkedList<ItemStack>();
                    itemRews.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData("itemReqs", itemRews);
                }

                context.setSessionData("newItem", null);
                context.setSessionData("tempStack", null);

            }
            
            String text = GOLD + "- Item Requirements -\n";
            if(context.getSessionData("itemReqs") == null){
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Add item\n";
                text += GRAY + "2 - Set remove items (No items set)\n";
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Done";
            }else{

                for(ItemStack is : getItems(context)){

                    text += GRAY + "    - " + ItemUtil.getDisplayString(is) + "\n";

                }
                
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Add item\n";
                

                if(context.getSessionData("removeItemReqs") == null){
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set remove items (No values set)\n";
                }else{

                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set remove items\n";
                    for(Boolean b : getRemoveItems(context)){

                        text += GRAY + "    - " + AQUA + b.toString().toLowerCase() + "\n";

                    }

                }

                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Done";

            }

            return text;

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input){

            if(input.equalsIgnoreCase("1")){
                return new ItemStackPrompt(ItemListPrompt.this);
            }else if(input.equalsIgnoreCase("2")){
                if(context.getSessionData("itemReqs") == null){
                    context.getForWhom().sendRawMessage(RED + "You must add at least one item first!");
                    return new ItemListPrompt();
                }else{
                    return new RemoveItemsPrompt();
                }
            }else if(input.equalsIgnoreCase("3")){
                context.getForWhom().sendRawMessage(YELLOW + "Item requirements cleared.");
                context.setSessionData("itemReqs", null);
                context.setSessionData("removeItemReqs", null);
                return new ItemListPrompt();
            }else if(input.equalsIgnoreCase("4")){

                int one;
                int two;

                if(context.getSessionData("itemReqs") != null)
                    one = ((List<ItemStack>) context.getSessionData("itemReqs")).size();
                else
                    one = 0;

                if(context.getSessionData("removeItemReqs") != null)
                    two = ((List<Boolean>) context.getSessionData("removeItemReqs")).size();
                else
                    two = 0;

                if(one == two)
                    return new RequirementsPrompt(quests, factory);
                else{
                    context.getForWhom().sendRawMessage(RED + "The " + GOLD + "items list " + RED + "and " + GOLD + "remove items list " + RED + "are not the same size!");
                    return new ItemListPrompt();
                }
            }
            return null;

        }

        private List<ItemStack> getItems(ConversationContext context){
            return (List<ItemStack>) context.getSessionData("itemReqs");
        }

        private List<Boolean> getRemoveItems(ConversationContext context){
            return (List<Boolean>) context.getSessionData("removeItemReqs");
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
