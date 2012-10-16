package me.blackvein.quests.prompts;

import java.util.LinkedList;
import java.util.List;
import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.*;


public class RequirementPrompt extends FixedSetPrompt{
    
    Quests quests;
    
    static final ChatColor BOLD = ChatColor.BOLD;
    static final ChatColor AQUA = ChatColor.AQUA;
    static final ChatColor DARKAQUA = ChatColor.DARK_AQUA;
    static final ChatColor BLUE = ChatColor.BLUE;
    static final ChatColor GOLD = ChatColor.GOLD;
    static final ChatColor PINK = ChatColor.LIGHT_PURPLE;
    static final ChatColor GREEN = ChatColor.GREEN;
    static final ChatColor RED = ChatColor.RED;
    static final ChatColor DARKRED = ChatColor.DARK_RED;
    static final ChatColor YELLOW = ChatColor.YELLOW;
    static final ChatColor GRAY = ChatColor.GRAY;
    static final ChatColor RESET = ChatColor.RESET;
    
    public RequirementPrompt(Quests plugin){
        
        super("1", "2", "3");
        quests = plugin;
        
    }
    
    @Override
    public String getPromptText(ConversationContext context){
        
        String text;
        
        text = DARKAQUA + "- " + AQUA + context.getSessionData("questName") + AQUA + " | Requirements -\n";
        
        if(context.getSessionData("moneyReq") == null)
            text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set money requirement (None set)\n";
        else{
            int moneyReq = (Integer) context.getSessionData("moneyReq");
            text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set money requirement (" + context.getSessionData("moneyReq") + " " + (moneyReq > 1 ? Quests.getCurrency(true) : Quests.getCurrency(false)) + ")\n";
        }
        
        if(context.getSessionData("questPointsReq") == null)
            text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set Quest Points requirement (None set)\n";
        else{
            text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set Quest Points requirement (" + context.getSessionData("questPointsReq") + " Quest Points)\n";
        }
        
        if(context.getSessionData("itemIdReqs") == null)
            text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Set Item requirements (None set)\n";
        else{
            text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Set Item requirements";
            List<Integer> ids = (List<Integer>) context.getSessionData("itemIdReqs");
            List<Integer> amounts = (List<Integer>) context.getSessionData("itemAmountReqs");
            
            for(int i : ids){
                
                text += GRAY + "    - " + AQUA + Quester.prettyItemString(i) + YELLOW + " x " + AQUA + amounts.get(ids.indexOf(i));
                
            }
        }
        
        
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
        }
        return null;
        
    }
    
    private class MoneyPrompt extends NumericPrompt {
        
        @Override
        public String getPromptText(ConversationContext context){
            
            return YELLOW + "Enter amount of " + (Quests.economy.currencyNamePlural().isEmpty() ? "Money" : Quests.economy.currencyNamePlural());
            
        }
        
        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input){
            
            if(input.intValue() < 1){
                context.getForWhom().sendRawMessage(RED + "Amount must be greater than 0!");
                return new MoneyPrompt();
            }
            
            context.setSessionData("moneyReq", input.intValue());
            return new RequirementPrompt(quests);
            
        }
        
    }
    
    private class QuestPointsPrompt extends NumericPrompt {
        
        @Override
        public String getPromptText(ConversationContext context){
            
            return YELLOW + "Enter amount of Quest Points";
            
        }
        
        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input){
            
            if(input.intValue() < 1){
                context.getForWhom().sendRawMessage(RED + "Amount must be greater than 0!");
                return new QuestPointsPrompt();
            }
            
            context.setSessionData("questPointsReq", input.intValue());
            return new RequirementPrompt(quests);
            
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
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Done";
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
                
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Done";
                
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
                context.getForWhom().sendRawMessage(YELLOW + "Item requirements cleared.");
                context.setSessionData("itemIdReqs", null);
                context.setSessionData("itemAmountReqs", null);
                return new ItemListPrompt();
            }else if(input.equalsIgnoreCase("4")){
                if( ((List<Integer>) context.getSessionData("itemIdReqs")).size() == (((List<Integer>) context.getSessionData("itemAmountReqs")).size()) )
                    return new RequirementPrompt(quests);
                else{
                    context.getForWhom().sendRawMessage(RED + "The item IDs list and item amounts list are not the same size!");
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
        
    }
    
    private class ItemIdsPrompt extends StringPrompt {
        
        @Override
        public String getPromptText(ConversationContext context){
            return YELLOW + "Enter item IDs, seperating each one by a space, or enter \'cancel\' to return.";
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
    
}
