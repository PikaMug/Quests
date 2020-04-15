package me.blackvein.quests.convo.actions.tasks;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.FixedSetPrompt;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;

import me.blackvein.quests.convo.actions.main.ActionMainPrompt;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.Lang;

public class TimerPrompt extends FixedSetPrompt {
    
    public TimerPrompt() {
        super("1", "2", "3");
    }

    @Override
    public String getPromptText(ConversationContext context) {
        String text = ChatColor.GOLD + "- " + Lang.get("eventEditorTimer") + " -\n";
        if (context.getSessionData(CK.E_TIMER) == null) {
            text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("eventEditorSetTimer") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("eventEditorSetTimer") + "(" + ChatColor.AQUA + "\"" 
                    + context.getSessionData(CK.E_TIMER) + "\"" + ChatColor.YELLOW + ")\n";
        }
        if (context.getSessionData(CK.E_CANCEL_TIMER) == null) {
            context.setSessionData(CK.E_CANCEL_TIMER, Lang.get("noWord"));
        }
        text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                + Lang.get("eventEditorCancelTimer") + ": " + ChatColor.AQUA 
                + context.getSessionData(CK.E_CANCEL_TIMER) + "\n";
        text += ChatColor.GREEN + "" + ChatColor.BOLD + "3 " + ChatColor.RESET + ChatColor.YELLOW + "- " 
                + Lang.get("done") + "\n";
        return text;
    }

    @Override
    protected Prompt acceptValidatedInput(ConversationContext context, String input) {
        if (input.equalsIgnoreCase("1")) {
            return new FailTimerPrompt();
        } else if (input.equalsIgnoreCase("2")) {
            String s = (String) context.getSessionData(CK.E_CANCEL_TIMER);
            if (s.equalsIgnoreCase(Lang.get("yesWord"))) {
                context.setSessionData(CK.E_CANCEL_TIMER, Lang.get("noWord"));
            } else {
                context.setSessionData(CK.E_CANCEL_TIMER, Lang.get("yesWord"));
            }
            return new ActionMainPrompt(context);
        }
        return new ActionMainPrompt(context);
    }
    
    public class FailTimerPrompt extends NumericPrompt {

        @Override
        protected Prompt acceptValidatedInput(final ConversationContext context, final Number number) {
            context.setSessionData(CK.E_TIMER, number);
            return new ActionMainPrompt(context);
        }

        @Override
        public String getPromptText(final ConversationContext conversationContext) {
            return ChatColor.YELLOW + Lang.get("eventEditorEnterTimerSeconds");
        }
    }
}
