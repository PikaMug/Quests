package me.blackvein.quests.prompts;

import me.blackvein.quests.Quests;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.FixedSetPrompt;
import org.bukkit.conversations.Prompt;


public class RequirementPrompt extends FixedSetPrompt{
    
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
    static final ChatColor RESET = ChatColor.RESET;
    
    @Override
    public String getPromptText(ConversationContext context){
        
        String text;
        
        text = DARKAQUA + "- " + AQUA + context.getSessionData("questName") + AQUA + " | Requirements -\n";
        
        if(context.getSessionData("moneyReq") == null)
            text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set money requirement (None set)\n";
        else{
            int moneyReq = (Integer) context.getSessionData("moneyReq");
            text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set money requirement (" + context.getSessionData("moneyReq") + " " + (moneyReq > 1 ? Quests.getCurrency(true) : Quests.getCurrency(false)) + " )\n";
        }
        
        
        return text;
        
    }
    
    @Override
    protected Prompt acceptValidatedInput(ConversationContext context, String input){
        
        return null;
        
    }
    
}
