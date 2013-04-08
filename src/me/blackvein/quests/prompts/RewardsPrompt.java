                     package me.blackvein.quests.prompts;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import me.blackvein.quests.QuestFactory;
import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.*;


public class RewardsPrompt extends FixedSetPrompt{
    
    Quests quests;
    
    static final ChatColor BOLD = ChatColor.BOLD;
    static final ChatColor ITALIC = ChatColor.ITALIC;
    static final ChatColor AQUA = ChatColor.AQUA;
    static final ChatColor DARKAQUA = ChatColor.DARK_AQUA;
    static final ChatColor BLUE = ChatColor.BLUE;
    static final ChatColor GOLD = ChatColor.GOLD;
    static final ChatColor PINK = ChatColor.LIGHT_PURPLE;
    static final ChatColor PURPLE = ChatColor.DARK_PURPLE;
    static final ChatColor GREEN = ChatColor.GREEN;
    static final ChatColor RED = ChatColor.RED;
    static final ChatColor DARKRED = ChatColor.DARK_RED;
    static final ChatColor YELLOW = ChatColor.YELLOW;
    static final ChatColor GRAY = ChatColor.GRAY;
    static final ChatColor RESET = ChatColor.RESET;
    
    final QuestFactory factory;
    
    public RewardsPrompt(Quests plugin, QuestFactory qf){
        
        super("1", "2", "3", "4", "5", "6", "7", "8");
        quests = plugin;
        factory = qf;
        
    }
    
    @Override
    public String getPromptText(ConversationContext context){
        
        String text;
        
        text = DARKAQUA + "- " + AQUA + context.getSessionData("questName") + AQUA + " | Rewards -\n";
        
        if(context.getSessionData("moneyRew") == null)
            text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set money reward (None set)\n";
        else{
            int moneyRew = (Integer) context.getSessionData("moneyRew");
            text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set money reward (" + moneyRew + " " + (moneyRew > 1 ? Quests.getCurrency(true) : Quests.getCurrency(false)) + ")\n";
        }
        
        if(context.getSessionData("questPointsRew") == null)
            text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set Quest Points reward (None set)\n";
        else{
            text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set Quest Points reward (" + context.getSessionData("questPointsRew") + " Quest Points)\n";
        }
        
        if(context.getSessionData("itemIdRews") == null)
            text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Set item rewards (None set)\n";
        else{
            text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Set item rewards\n";
            List<Integer> ids = (List<Integer>) context.getSessionData("itemIdRews");
            List<Integer> amounts = (List<Integer>) context.getSessionData("itemAmountRews");
            
            for(int i : ids){
                
                text += GRAY + "    - " + AQUA + Quester.prettyItemString(i) + YELLOW + " x " + DARKAQUA + amounts.get(ids.indexOf(i)) + "\n";
                
            }
        }
        
        if(context.getSessionData("expRew") == null)
            text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Set experience reward (None set)\n";
        else{
            text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Set experience reward (" + context.getSessionData("expRew") + " points)\n";
        }
        
        if(context.getSessionData("commandRews") == null)
            text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - Set command rewards (None set)\n";
        else{
            text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - Set command rewards\n";
            List<String> commands = (List<String>) context.getSessionData("commandRews");
            
            for(String cmd : commands){
                
                text += GRAY + "    - " + AQUA + cmd + "\n";
                
            }
        }
        
        if(context.getSessionData("permissionRews") == null)
            text += BLUE + "" + BOLD + "6" + RESET + YELLOW + " - Set permission rewards (None set)\n";
        else{
            text += BLUE + "" + BOLD + "6" + RESET + YELLOW + " - Set permission rewards\n";
            List<String> permissions = (List<String>) context.getSessionData("permissionRews");
            
            for(String perm : permissions){
                
                text += GRAY + "    - " + AQUA + perm + "\n";
                
            }
        }
        
        //mcMMO
        
        if(Quests.mcmmo != null){
            
            if(context.getSessionData("mcMMOSkillRews") == null)
                text += BLUE + "" + BOLD + "7" + RESET + YELLOW + " - Set mcMMO skill rewards (None set)\n";
            else{
                text += BLUE + "" + BOLD + "7" + RESET + YELLOW + " - Set mcMMO skill rewards\n";
                List<String> skills = (List<String>) context.getSessionData("mcMMOSkillRews");
                List<Integer> amounts = (List<Integer>) context.getSessionData("mcMMOSkillAmounts");
                
                for(String skill : skills){

                    text += GRAY + "    - " + AQUA + skill + GRAY + " x " + DARKAQUA + amounts.get(skills.indexOf(skill)) + "\n";

                }
            }
            
        }
        
        //
        
        if(Quests.mcmmo != null)
            text += GREEN + "" + BOLD + "8" + RESET + YELLOW + " - Done";
        else
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
            return new ExperiencePrompt();
        }else if(input.equalsIgnoreCase("5")){
            return new CommandsPrompt();
        }else if(input.equalsIgnoreCase("6")){
            return new PermissionsPrompt();
        }else if(input.equalsIgnoreCase("7")){
            if(Quests.mcmmo != null)
                return new mcMMOListPrompt();
            else
                return factory.returnToMenu();
        }else if(input.equalsIgnoreCase("8")){
            if(Quests.mcmmo != null)
                return factory.returnToMenu();
            else
                return new RewardsPrompt(quests, factory);
        }
        return null;
        
    }
    
    private class MoneyPrompt extends NumericPrompt {
        
        @Override
        public String getPromptText(ConversationContext context){
            
            return YELLOW + "Enter amount of " + AQUA + (Quests.economy.currencyNamePlural().isEmpty() ? "Money" : Quests.economy.currencyNamePlural()) + YELLOW + ", or 0 to clear the money reward, or -1 to cancel";
            
        }
        
        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input){
            
            if(input.intValue() < -1){
                context.getForWhom().sendRawMessage(RED + "Amount must be positive!");
                return new MoneyPrompt();
            }else if(input.intValue() == 0)
                context.setSessionData("moneyRew", null);
            else if(input.intValue() != -1)
                context.setSessionData("moneyRew", input.intValue());
                
            return new RewardsPrompt(quests, factory);
            
        }
        
    }
    
    private class ExperiencePrompt extends NumericPrompt {
        
        @Override
        public String getPromptText(ConversationContext context){
            
            return YELLOW + "Enter amount of experience, or 0 to clear the experience reward, or -1 to cancel";
            
        }
        
        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input){
            
            if(input.intValue() < -1){
                context.getForWhom().sendRawMessage(RED + "Amount must be positive!");
                return new ExperiencePrompt();
            }else if(input.intValue() == -1)
                context.setSessionData("expRew", null);
            else if(input.intValue() != 0)
                context.setSessionData("expRew", input.intValue());
            
            return new RewardsPrompt(quests, factory);
            
        }
        
    }
    
    private class QuestPointsPrompt extends NumericPrompt {
        
        @Override
        public String getPromptText(ConversationContext context){
            
            return YELLOW + "Enter amount of Quest Points, or 0 to clear the Quest Points reward, or -1 to cancel";
            
        }
        
        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input){
            
            if(input.intValue() < -1){
                context.getForWhom().sendRawMessage(RED + "Amount must be positive!");
                return new QuestPointsPrompt();
            }else if(input.intValue() == -1)
                context.setSessionData("questPointsRew", null);
            else if(input.intValue() != 0)
                context.setSessionData("questPointsRew", input.intValue());
            
            return new RewardsPrompt(quests, factory);
            
        }
        
    }
    
    private class ItemListPrompt extends FixedSetPrompt {
        
        public ItemListPrompt(){
            
            super("1", "2", "3", "4");
            
        }
        
        @Override
        public String getPromptText(ConversationContext context){
            
            String text = GOLD + "- Item Rewards -\n";
            if(context.getSessionData("itemIdRews") == null){
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set item IDs (None set)\n";
                text += GRAY + "2 - Set item amounts (No IDs set)\n";
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Done";
            }else{
                
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set item IDs\n";
                for(Integer i : getItemIds(context)){
                    
                    text += GRAY + "    - " + AQUA + Quester.prettyItemString(i) + "\n";
                    
                }
                
                if(context.getSessionData("itemAmountRews") == null){
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
                if(context.getSessionData("itemIdRews") == null){
                    context.getForWhom().sendRawMessage(RED + "You must set item IDs first!");
                    return new ItemListPrompt();
                }else{
                    return new ItemAmountsPrompt();
                }
            }else if(input.equalsIgnoreCase("3")){
                context.getForWhom().sendRawMessage(YELLOW + "Item rewards cleared.");
                context.setSessionData("itemIdRews", null);
                context.setSessionData("itemAmountRews", null);
                return new ItemListPrompt();
            }else if(input.equalsIgnoreCase("4")){
                
                int one;
                int two;
                
                if(context.getSessionData("itemIdRews") != null)
                    one = ((List<Integer>) context.getSessionData("itemIdRews")).size();
                else
                    one = 0;
                
                if(context.getSessionData("itemAmountRews") != null)
                    two = ((List<Integer>) context.getSessionData("itemAmountRews")).size();
                else
                    two = 0;
                
                if(one == two)
                    return new RewardsPrompt(quests, factory);
                else{
                    context.getForWhom().sendRawMessage(RED + "The " + GOLD + "item IDs list " + RED + "and " + GOLD + "item amounts list " + RED + "are not the same size!");
                    return new ItemListPrompt();
                }
            }
            return null;
            
        }
        
        private List<Integer> getItemIds(ConversationContext context){
            return (List<Integer>) context.getSessionData("itemIdRews");
        }
        
        private List<Integer> getItemAmounts(ConversationContext context){
            return (List<Integer>) context.getSessionData("itemAmountRews");
        }
        
    }
    
    private class ItemIdsPrompt extends StringPrompt {
        
        @Override
        public String getPromptText(ConversationContext context){
            return YELLOW + "Enter item IDs separating each one by a space, or enter \'cancel\' to return.";
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
                                context.getForWhom().sendRawMessage(RED + "List contains duplicates!");
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
                
                context.setSessionData("itemIdRews", ids);
            
            }
            
            return new ItemListPrompt();
            
        }
        
    }
    
    private class ItemAmountsPrompt extends StringPrompt {
        
        @Override
        public String getPromptText(ConversationContext context){
            return YELLOW + "Enter item amounts (numbers) separating each one by a space, or enter \'cancel\' to return.";
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
                
                context.setSessionData("itemAmountRews", amounts);
            
            }
            
            return new ItemListPrompt();
            
        }
        
        
    }
    
    private class CommandsPrompt extends StringPrompt {
        
        @Override
        public String getPromptText(ConversationContext context){
            String note = GOLD + "\nNote: You may put <player> to specify the player who completed the Quest. e.g. " + AQUA + BOLD + ITALIC + "smite <player>" + RESET;
            return YELLOW + "Enter command rewards separating each one by a " + BOLD + "comma" + RESET + YELLOW + ", or enter \'clear\' to clear the list, or enter \'cancel\' to return." + note;
        }
        
        @Override
        public Prompt acceptInput(ConversationContext context, String input){
            
            if(input.equalsIgnoreCase("cancel") == false && input.equalsIgnoreCase("clear") == false){
            
                String[] args = input.split(",");
                LinkedList<String> commands = new LinkedList<String>();
                for(String s : args){

                    if(s.startsWith("/"))
                        s = s.substring(1);
                    
                    commands.add(s);

                }
                
                context.setSessionData("commandRews", commands);
            
            }else if(input.equalsIgnoreCase("clear")){
                context.setSessionData("commandRews", null);
            }
            
            return new RewardsPrompt(quests, factory);
            
        }
        
    }
    
    private class PermissionsPrompt extends StringPrompt {
        
        @Override
        public String getPromptText(ConversationContext context){
            return YELLOW + "Enter permission rewards separating each one by a space, or enter \'clear\' to clear the list, or enter \'cancel\' to return.";
        }
        
        @Override
        public Prompt acceptInput(ConversationContext context, String input){
            
            if(input.equalsIgnoreCase("cancel") == false && input.equalsIgnoreCase("clear") == false){
            
                String[] args = input.split(" ");
                LinkedList<String> permissions = new LinkedList<String>();
                permissions.addAll(Arrays.asList(args));
                
                context.setSessionData("permissionRews", permissions);
            
            }else if(input.equalsIgnoreCase("clear")){
                context.setSessionData("permissionRews", null);
            }
            
            return new RewardsPrompt(quests, factory);
            
        }
        
    }
    
    
    
    
    
    //mcMMO
    
    private class mcMMOListPrompt extends FixedSetPrompt {
        
        public mcMMOListPrompt(){
            
            super("1", "2", "3", "4");
            
        }
        
        @Override
        public String getPromptText(ConversationContext context){
            
            String text = GOLD + "- mcMMO Rewards -\n";
            if(context.getSessionData("mcMMOSkillRews") == null){
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set skills (None set)\n";
                text += GRAY + "2 - Set skill amounts (No skills set)\n";
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Done";
            }else{
                
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set skills\n";
                for(String s : getSkills(context)){
                    
                    text += GRAY + "    - " + AQUA + s + "\n";
                    
                }
                
                if(context.getSessionData("mcMMOAmountRews") == null){
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set skill amounts (None set)\n";
                }else{
                    
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set skill amounts\n";
                    for(Integer i : getSkillAmounts(context)){

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
                if(context.getSessionData("itemIdRews") == null){
                    context.getForWhom().sendRawMessage(RED + "You must set skills first!");
                    return new mcMMOListPrompt();
                }else{
                    return new mcMMOAmountsPrompt();
                }
            }else if(input.equalsIgnoreCase("3")){
                context.getForWhom().sendRawMessage(YELLOW + "mcMMO rewards cleared.");
                context.setSessionData("mcMMOSkillRews", null);
                context.setSessionData("mcMMOAmountRews", null);
                return new mcMMOListPrompt();
            }else if(input.equalsIgnoreCase("4")){
                
                int one;
                int two;
                
                if(context.getSessionData("mcMMOSkillRews") != null)
                    one = ((List<Integer>) context.getSessionData("mcMMOSkillRews")).size();
                else
                    one = 0;
                
                if(context.getSessionData("mcMMOAmountRews") != null)
                    two = ((List<Integer>) context.getSessionData("mcMMOAmountRews")).size();
                else
                    two = 0;
                
                if(one == two)
                    return new RewardsPrompt(quests, factory);
                else{
                    context.getForWhom().sendRawMessage(RED + "The " + GOLD + "skills list " + RED + "and " + GOLD + "skill amounts list " + RED + "are not the same size!");
                    return new mcMMOListPrompt();
                }
            }
            return null;
            
        }
        
        private List<String> getSkills(ConversationContext context){
            return (List<String>) context.getSessionData("mcMMORews");
        }
        
        private List<Integer> getSkillAmounts(ConversationContext context){
            return (List<Integer>) context.getSessionData("mcMMOAmountRews");
        }
        
    }
    
    private class mcMMOSkillsPrompt extends StringPrompt {
        
        @Override
        public String getPromptText(ConversationContext context){
            String skillList =
                    GOLD + "-Skill List-\n" +
                    AQUA + "Acrobatics\n" +
                    AQUA + "All\n" +
                    AQUA + "Archery\n" +
                    AQUA + "Axes\n" +
                    AQUA + "Excavation\n" +
                    AQUA + "Fishing\n" +
                    AQUA + "Herbalism\n" +
                    AQUA + "Mining\n" +
                    AQUA + "Repair\n" +
                    AQUA + "Swords\n" +
                    AQUA + "Taming\n" +
                    AQUA + "Unarmed\n" +
                    AQUA + "Woodcutting\n\n";
                    
            return skillList + YELLOW + "Enter mcMMO skills, separating each one by a space, or enter \'cancel\' to return."
                    + "\n" + GOLD + "Note: The \'All\' option will give levels to all skills.";
        }
        
        @Override
        public Prompt acceptInput(ConversationContext context, String input){
            
            if(input.equalsIgnoreCase("cancel") == false){
            
                String[] args = input.split(" ");
                LinkedList<String> skills = new LinkedList<String>();
                for(String s : args){

                    if(Quests.getMcMMOSkill(s) != null){

                        if(skills.contains(s) == false){
                            skills.add(Quester.getCapitalized(s));
                        }else{
                            context.getForWhom().sendRawMessage(RED + "List contains duplicates!");
                            return new mcMMOSkillsPrompt();
                        }

                    }else{
                        context.getForWhom().sendRawMessage(PINK + s + RED + " is not a valid mcMMO skill!");
                        return new mcMMOSkillsPrompt();
                    }

                }
                
                context.setSessionData("mcMMOSkillRews", skills);
            
            }
            
            return new mcMMOListPrompt();
            
        }
        
    }
    
    private class mcMMOAmountsPrompt extends StringPrompt {
        
        @Override
        public String getPromptText(ConversationContext context){
            return YELLOW + "Enter skill amounts (numbers), separating each one by a space, or enter \'cancel\' to return.";
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
                            return new mcMMOAmountsPrompt();
                        }

                    }catch (Exception e){
                        context.getForWhom().sendRawMessage(RED + "Invalid entry " + PINK + s + RED + ". Input was not a list of numbers!");
                        return new mcMMOAmountsPrompt();
                    }

                }
                
                context.setSessionData("mcMMOAmountRews", amounts);
            
            }
            
            return new mcMMOListPrompt();
            
        }
        
        
    }
    
}
