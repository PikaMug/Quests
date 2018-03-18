package me.blackvein.quests.prompts;

import me.blackvein.quests.util.Lang;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.FixedSetPrompt;
import org.bukkit.conversations.Prompt;

public class DateTimePrompt extends FixedSetPrompt {
	
	final Prompt oldPrompt;

	public DateTimePrompt(Prompt old) {
		super("0", "1", "2", "3", "4", "5", "6", "7", "8");
		oldPrompt = old;
	}
	
	@Override
	public String getPromptText(ConversationContext cc) {
		String menu = ChatColor.YELLOW + Lang.get("dateTimeTitle") + "\n";
		menu += ChatColor.YELLOW + "" + ChatColor.BOLD + "1. " + ChatColor.RESET + "" + ChatColor.GOLD + Lang.get("timeDay") + "\n";
		menu += ChatColor.YELLOW + "" + ChatColor.BOLD + "2. " + ChatColor.RESET + "" + ChatColor.GOLD + Lang.get("timeMonth") + "\n";
		menu += ChatColor.YELLOW + "" + ChatColor.BOLD + "3. " + ChatColor.RESET + "" + ChatColor.GOLD + Lang.get("timeYear") + "\n";
		menu += ChatColor.YELLOW + "" + ChatColor.BOLD + "4. " + ChatColor.RESET + "" + ChatColor.GOLD + Lang.get("timeHour") + "\n";
		menu += ChatColor.YELLOW + "" + ChatColor.BOLD + "5. " + ChatColor.RESET + "" + ChatColor.GOLD + Lang.get("timeMinute") + "\n";
		menu += ChatColor.YELLOW + "" + ChatColor.BOLD + "6. " + ChatColor.RESET + "" + ChatColor.GOLD + Lang.get("timeSecond") + "\n";
		menu += ChatColor.YELLOW + "" + ChatColor.BOLD + "7. " + ChatColor.RESET + "" + ChatColor.GOLD + Lang.get("timeZone") + "\n";
		menu += ChatColor.YELLOW + "" + ChatColor.BOLD + "8. " + ChatColor.RESET + "" + ChatColor.RED + Lang.get("cancel") + "\n";
		menu += ChatColor.YELLOW + "" + ChatColor.BOLD + "9. " + ChatColor.RESET + "" + ChatColor.GREEN + Lang.get("done") + "\n";
		return menu;
	}

	@Override
	protected Prompt acceptValidatedInput(ConversationContext cc, String input) {
		if (input.equalsIgnoreCase("1")) {
			//return new DayPrompt();
		} else if (input.equalsIgnoreCase("2")) {
			//return new MonthPrompt();
		} else if (input.equalsIgnoreCase("3")) {
			//return new YearPrompt();
		}
		try {
			return oldPrompt;
		} catch (Exception e) {
			cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateCriticalError"));
			return Prompt.END_OF_CONVERSATION;
		}
	}
}