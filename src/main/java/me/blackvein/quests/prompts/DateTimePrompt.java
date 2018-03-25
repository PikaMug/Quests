package me.blackvein.quests.prompts;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import me.blackvein.quests.Quests;
import me.blackvein.quests.util.Lang;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.FixedSetPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

public class DateTimePrompt extends FixedSetPrompt {
	
	Quests quests;
	final Prompt oldPrompt;

	public DateTimePrompt(Quests plugin, Prompt old) {
		super("0", "1", "2", "3", "4", "5", "6", "7", "8");
		quests = plugin;
		oldPrompt = old;
	}
	
	@Override
	public String getPromptText(ConversationContext cc) {
		String menu = ChatColor.YELLOW + Lang.get("dateTimeTitle") + "\n";
		String dateData = "";
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss");
		if (cc.getSessionData("tempDay") == null) {
			cc.setSessionData("tempDay", cal.get(Calendar.DAY_OF_MONTH));
		}
		if (cc.getSessionData("tempMonth") == null) {
			cc.setSessionData("tempMonth", cal.get(Calendar.MONTH));
		}
		if (cc.getSessionData("tempYear") == null) {
			cc.setSessionData("tempYear", cal.get(Calendar.YEAR));
		}

		if (cc.getSessionData("tempHour") == null) {
			cc.setSessionData("tempHour", cal.get(Calendar.HOUR_OF_DAY));
		}
		if (cc.getSessionData("tempMinute") == null) {
			cc.setSessionData("tempMinute", cal.get(Calendar.MINUTE));
		}
		if (cc.getSessionData("tempSecond") == null) {
			cc.setSessionData("tempSecond", cal.get(Calendar.SECOND));
		}
		cal.set((Integer) cc.getSessionData("tempYear"), (Integer) cc.getSessionData("tempMonth"), (Integer) cc.getSessionData("tempDay"),
				(Integer) cc.getSessionData("tempHour"), (Integer) cc.getSessionData("tempMinute"), (Integer) cc.getSessionData("tempSecond"));
		dateData += ChatColor.DARK_AQUA + dateFormat.format(cal.getTime()) + " ";
		dateData += ChatColor.AQUA + timeFormat.format(cal.getTime()) + " ";
		
		if (cc.getSessionData("tempZone") == null) {
			cc.setSessionData("tempZone", cal.getTimeZone().getID());
		}
		TimeZone tz = TimeZone.getTimeZone((String) cc.getSessionData("tempZone"));
		cal.setTimeZone(tz);
		String[] iso = quests.lang.iso.split("-");
		Locale loc = new Locale(iso[0], iso[1]);
		Double hour = (double) (cal.getTimeZone().getRawOffset() / 60 / 60 / 1000);
		String[] sep = String.valueOf(hour).replace("-", "").split("\\.");
		DecimalFormat zoneFormat = new DecimalFormat("00");
		dateData += ChatColor.LIGHT_PURPLE + "UTC" + (hour < 0 ? "-":"+") + zoneFormat.format(Integer.valueOf(sep[0])) + ":" + zoneFormat.format(Integer.valueOf(sep[1]))
				+ ChatColor.RED + " (" + cal.getTimeZone().getDisplayName(loc) + ")";
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
						return new DateTimePrompt(quests, oldPrompt);
					}
				} catch (NumberFormatException e) {
					cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
					return new DayPrompt();
				}
			} else {
				return new DateTimePrompt(quests, oldPrompt);
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
						cc.setSessionData("tempMonth", Integer.parseInt(input) - 1);
						return new DateTimePrompt(quests, oldPrompt);
					}
				} catch (NumberFormatException e) {
					cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
					return new MonthPrompt();
				}
			} else {
				return new DateTimePrompt(quests, oldPrompt);
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
						return new DateTimePrompt(quests, oldPrompt);
					}
				} catch (NumberFormatException e) {
					cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
					return new YearPrompt();
				}
			} else {
				return new DateTimePrompt(quests, oldPrompt);
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
						return new DateTimePrompt(quests, oldPrompt);
					}
				} catch (NumberFormatException e) {
					cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
					return new HourPrompt();
				}
			} else {
				return new DateTimePrompt(quests, oldPrompt);
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
						return new DateTimePrompt(quests, oldPrompt);
					}
				} catch (NumberFormatException e) {
					cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
					return new MinutePrompt();
				}
			} else {
				return new DateTimePrompt(quests, oldPrompt);
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
						return new DateTimePrompt(quests, oldPrompt);
					}
				} catch (NumberFormatException e) {
					cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
					return new SecondPrompt();
				}
			} else {
				return new DateTimePrompt(quests, oldPrompt);
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
					double amt = Double.parseDouble(input.replaceAll("UTC", "").replace(":", "."));
					if (amt < -12.0 || amt > 14.0) {
						cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidRange")
							.replace("<least>", "-12:00").replace("<greatest>", "14:00"));
						return new ZonePrompt();
					} else {
						String[] t = TimeZone.getAvailableIDs((int) Math.round(amt * 60.0 * 60.0 * 1000.0));
						//TODO - let user choose
						for (String output : t) {
							System.out.println("zone id= " + output);
						}
						if (t.length > 0) {
							cc.setSessionData("tempZone", t[0]);
						} else {
							cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
							quests.getLogger().severe("Unable to get time zone for converted offset " + input);
						}	
						return new DateTimePrompt(quests, oldPrompt);
					}
				} catch (NumberFormatException e) {
					cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
					return new ZonePrompt();
				}
			} else {
				return new DateTimePrompt(quests, oldPrompt);
			}
		}
	}
}