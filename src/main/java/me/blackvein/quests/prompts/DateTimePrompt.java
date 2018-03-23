package me.blackvein.quests.prompts;

import java.util.Locale;
import java.util.TimeZone;

import me.blackvein.quests.util.Lang;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.FixedSetPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

public class DateTimePrompt extends FixedSetPrompt {
	
	final Prompt oldPrompt;

	public DateTimePrompt(Prompt old) {
		super("0", "1", "2", "3", "4", "5", "6", "7", "8");
		oldPrompt = old;
	}
	
	@Override
	public String getPromptText(ConversationContext cc) {
		String menu = ChatColor.YELLOW + Lang.get("dateTimeTitle") + "\n";
		String dateData = "";
		if (cc.getSessionData("tempDay") != null) {
			dateData += (String) cc.getSessionData("tempDay") + "/";
		}
		if (cc.getSessionData("tempMonth") != null) {
			dateData += (String) cc.getSessionData("tempMonth") + "/";
		}
		if (cc.getSessionData("tempYear") != null) {
			dateData += (String) cc.getSessionData("tempYear") + " ";
		}
		if (cc.getSessionData("tempHour") != null) {
			dateData += (String) cc.getSessionData("tempHour") + ":";
		}
		if (cc.getSessionData("tempMinute") != null) {
			dateData += (String) cc.getSessionData("tempMinute") + ":";
		}
		if (cc.getSessionData("tempSecond") != null) {
			dateData += (String) cc.getSessionData("tempSecond") + " ";
		}
		if (cc.getSessionData("tempZone") == null) {
			cc.setSessionData("tempZone", TimeZone.getDefault().getID());
		}
		TimeZone tz = TimeZone.getTimeZone((String) cc.getSessionData("tempZone"));
		//TODO - CHANGE CURRENT SYSTEM TIME TO USER-SPECIFIED DATE
		dateData += tz.getOffset(System.currentTimeMillis() / 1000 / 60) + " (" + tz.getID() + ")" + " test - " + tz.getDisplayName(Locale.FRENCH);
		if (dateData != null) {
			menu += dateData + "\n";
		}
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
			return new DayPrompt();
		} else if (input.equalsIgnoreCase("2")) {
			return new MonthPrompt();
		} else if (input.equalsIgnoreCase("3")) {
			return new YearPrompt();
		} else if (input.equalsIgnoreCase("4")) {
			return new HourPrompt();
		} else if (input.equalsIgnoreCase("5")) {
			return new MinutePrompt();
		} else if (input.equalsIgnoreCase("6")) {
			return new SecondPrompt();
		} else if (input.equalsIgnoreCase("7")) {
			return new ZonePrompt();
		} else if (input.equalsIgnoreCase("8")) {
			cc.setSessionData("tempDay", null);
			cc.setSessionData("tempMonth", null);
			cc.setSessionData("tempYear", null);
			cc.setSessionData("tempHour", null);
			cc.setSessionData("tempMinute", null);
			cc.setSessionData("tempSecond", null);
			cc.setSessionData("tempZone", null);
		} else if (input.equalsIgnoreCase("9")) {
			int day = (Integer) cc.getSessionData("tempDay");
			int month = (Integer) cc.getSessionData("tempMonth");
			int year = (Integer) cc.getSessionData("tempYear");
			int hour = (Integer) cc.getSessionData("tempHour");
			int minute = (Integer) cc.getSessionData("tempMinute");
			int second = (Integer) cc.getSessionData("tempSecond");
			String zone = (String) cc.getSessionData("tempZone");
			String date = day + ":" + month + ":" + year + ":"
					+ hour + ":" + minute + ":" + second + ":" + zone;
			cc.setSessionData("tempDate", date);
		}
		try {
			return oldPrompt;
		} catch (Exception e) {
			cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateCriticalError"));
			return Prompt.END_OF_CONVERSATION;
		}
	}
	
	private class DayPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext cc) {
			return ChatColor.YELLOW + Lang.get("dateCreateEnterDay");
		}

		@Override
		public Prompt acceptInput(ConversationContext cc, String input) {
			if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
				try {
					int amt = Integer.parseInt(input);
					if (amt < 1 || amt > 31) {
						cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidRange")
								.replace("<least>", "1").replace("<greatest>", "31"));
						return new DayPrompt();
					} else {
						cc.setSessionData("tempDay", Integer.parseInt(input));
						return new DateTimePrompt(oldPrompt);
					}
				} catch (NumberFormatException e) {
					cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
					return new DayPrompt();
				}
			} else {
				return new DateTimePrompt(oldPrompt);
			}
		}
	}
	
	private class MonthPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext cc) {
			return ChatColor.YELLOW + Lang.get("dateCreateEnterMonth");
		}

		@Override
		public Prompt acceptInput(ConversationContext cc, String input) {
			if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
				try {
					int amt = Integer.parseInt(input);
					if (amt < 1 || amt > 12) {
						cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidRange")
								.replace("<least>", "1").replace("<greatest>", "12"));
						return new MonthPrompt();
					} else {
						cc.setSessionData("tempMonth", Integer.parseInt(input));
						return new DateTimePrompt(oldPrompt);
					}
				} catch (NumberFormatException e) {
					cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
					return new MonthPrompt();
				}
			} else {
				return new DateTimePrompt(oldPrompt);
			}
		}
	}
	
	private class YearPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext cc) {
			return ChatColor.YELLOW + Lang.get("dateCreateEnterYear");
		}

		@Override
		public Prompt acceptInput(ConversationContext cc, String input) {
			if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
				try {
					int amt = Integer.parseInt(input);
					if (amt < 1000 || amt > 9999) {
						cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidRange")
								.replace("<least>", "1000").replace("<greatest>", "9999"));
						return new YearPrompt();
					} else {
						cc.setSessionData("tempYear", Integer.parseInt(input));
						return new DateTimePrompt(oldPrompt);
					}
				} catch (NumberFormatException e) {
					cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
					return new YearPrompt();
				}
			} else {
				return new DateTimePrompt(oldPrompt);
			}
		}
	}
	
	private class HourPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext cc) {
			return ChatColor.YELLOW + Lang.get("dateCreateEnterHour");
		}

		@Override
		public Prompt acceptInput(ConversationContext cc, String input) {
			if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
				try {
					int amt = Integer.parseInt(input);
					if (amt < 0 || amt > 23) {
						cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidRange")
								.replace("<least>", "0").replace("<greatest>", "23"));
						return new HourPrompt();
					} else {
						cc.setSessionData("tempHour", Integer.parseInt(input));
						return new DateTimePrompt(oldPrompt);
					}
				} catch (NumberFormatException e) {
					cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
					return new HourPrompt();
				}
			} else {
				return new DateTimePrompt(oldPrompt);
			}
		}
	}
	
	private class MinutePrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext cc) {
			return ChatColor.YELLOW + Lang.get("dateCreateEnterMinute");
		}

		@Override
		public Prompt acceptInput(ConversationContext cc, String input) {
			if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
				try {
					int amt = Integer.parseInt(input);
					if (amt < 0 || amt > 59) {
						cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidRange")
								.replace("<least>", "0").replace("<greatest>", "59"));
						return new MinutePrompt();
					} else {
						cc.setSessionData("tempMinute", Integer.parseInt(input));
						return new DateTimePrompt(oldPrompt);
					}
				} catch (NumberFormatException e) {
					cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
					return new MinutePrompt();
				}
			} else {
				return new DateTimePrompt(oldPrompt);
			}
		}
	}
	
	private class SecondPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext cc) {
			return ChatColor.YELLOW + Lang.get("dateCreateEnterSecond");
		}

		@Override
		public Prompt acceptInput(ConversationContext cc, String input) {
			if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
				try {
					int amt = Integer.parseInt(input);
					if (amt < 0 || amt > 59) {
						cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidRange")
								.replace("<least>", "0").replace("<greatest>", "59"));
						return new SecondPrompt();
					} else {
						cc.setSessionData("tempSecond", Integer.parseInt(input));
						return new DateTimePrompt(oldPrompt);
					}
				} catch (NumberFormatException e) {
					cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
					return new SecondPrompt();
				}
			} else {
				return new DateTimePrompt(oldPrompt);
			}
		}
	}
	
	private class ZonePrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext cc) {
			return ChatColor.YELLOW + Lang.get("dateCreateEnterZone");
		}

		@Override
		public Prompt acceptInput(ConversationContext cc, String input) {
			if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
				try {
					int amt = Integer.parseInt(input.replaceAll("UTC", ""));
					if (amt < -12 || amt > 14) {
						cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidRange")
							.replace("<least>", "-12").replace("<greatest>", "14"));
						return new ZonePrompt();
					} else {
						cc.setSessionData("tempZone", Integer.parseInt(input));
						return new DateTimePrompt(oldPrompt);
					}
				} catch (NumberFormatException e) {
					cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
					return new ZonePrompt();
				}
			} else {
				return new DateTimePrompt(oldPrompt);
			}
		}
	}
}