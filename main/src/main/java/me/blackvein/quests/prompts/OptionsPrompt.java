package me.blackvein.quests.prompts;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.FixedSetPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

import me.blackvein.quests.Options;
import me.blackvein.quests.QuestFactory;
import me.blackvein.quests.Quests;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.Lang;

public class OptionsPrompt extends FixedSetPrompt {
	
	@SuppressWarnings("unused")
	private final Quests plugin;
	private final QuestFactory factory;
	private String tempKey;
	private StringPrompt tempPrompt;

	public OptionsPrompt(Quests plugin, QuestFactory qf) {
		super("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11");
		this.plugin = plugin;
		factory = qf;
	}

	@Override
	public String getPromptText(ConversationContext context) {
		String text;
		String lang = Lang.get("optionsTitle");
		lang = lang.replace("<quest>", ChatColor.AQUA + (String) context.getSessionData(CK.Q_NAME) + ChatColor.DARK_AQUA);
		text = ChatColor.DARK_AQUA + lang + "\n";
		text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("optGeneral") + "\n";
		text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("optMultiplayer") + "\n";
		text += ChatColor.GREEN + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("done");
		return text;
	}

	@Override
	protected Prompt acceptValidatedInput(ConversationContext context, String input) {
		if (input.equalsIgnoreCase("1")) {
			return new GeneralPrompt();
		} else if (input.equalsIgnoreCase("2")) {
			return new MultiplayerPrompt();
		} else if (input.equalsIgnoreCase("3")) {
			return factory.returnToMenu();
		}
		return null;
	}
	
	private class TrueFalsePrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			String text = Lang.get("optBooleanPrompt");
			text = text.replace("<true>", Lang.get("true"));
			text = text.replace("<false>", Lang.get("false"));
			return ChatColor.YELLOW + text;
		}
		
		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
				try {
					boolean b = Boolean.parseBoolean(input);
					if (input.equalsIgnoreCase("t") || input.equalsIgnoreCase(Lang.get("true"))) {
						b = true;
					}
					context.setSessionData(tempKey, b);
				} catch (Exception e) {
					e.printStackTrace();
					return new TrueFalsePrompt();
				}
			} else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
				context.setSessionData(tempKey, null);
				return tempPrompt;
			}
			return tempPrompt;
		}
	}
	
	private class GeneralPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			String text;
			String lang = Lang.get("questEditorOpts") + ": " + Lang.get("optGeneral");
			lang = lang.replace("<quest>", ChatColor.AQUA + (String) context.getSessionData(CK.Q_NAME) + ChatColor.DARK_AQUA);
			text = ChatColor.DARK_AQUA + lang + "\n";
			text += ChatColor.RED + "WIP" + "\n";
			text += ChatColor.GREEN + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("done");
			return text;
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase("1")) {
				tempKey = null;
				tempPrompt = null;
				return factory.returnToMenu();
			}
			return null;
		}
	}
	
	private class MultiplayerPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			String text;
			String lang = Lang.get("questEditorOpts") + ": " + Lang.get("optMultiplayer");
			lang = lang.replace("<quest>", ChatColor.AQUA + (String) context.getSessionData(CK.Q_NAME) + ChatColor.DARK_AQUA);
			text = ChatColor.DARK_AQUA + lang + "\n";
			if (context.getSessionData(CK.OPT_USE_DUNGEONSXL_PLUGIN) == null) {
				boolean defaultOpt = new Options().getUseDungeonsXLPlugin();
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("optUseDungeonsXLPlugin") + " (" 
						+ (defaultOpt ? ChatColor.GREEN + String.valueOf(defaultOpt) : ChatColor.RED + String.valueOf(defaultOpt)) + ChatColor.YELLOW + ")\n";
			} else {
				boolean dungeonsOpt = (Boolean) context.getSessionData(CK.OPT_USE_DUNGEONSXL_PLUGIN);
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("optUseDungeonsXLPlugin") + " (" 
						+ (dungeonsOpt ? ChatColor.GREEN + String.valueOf(dungeonsOpt) : ChatColor.RED + String.valueOf(dungeonsOpt)) + ChatColor.YELLOW + ")\n";
			}
			if (context.getSessionData(CK.OPT_USE_PARTIES_PLUGIN) == null) {
				boolean defaultOpt = new Options().getUsePartiesPlugin();
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("optUsePartiesPlugin") + " ("
						+ (defaultOpt ? ChatColor.GREEN + String.valueOf(defaultOpt) : ChatColor.RED + String.valueOf(defaultOpt)) + ChatColor.YELLOW + ")\n";
			} else {
				boolean partiesOpt = (Boolean) context.getSessionData(CK.OPT_USE_PARTIES_PLUGIN);
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("optUsePartiesPlugin") + " (" 
						+ (partiesOpt ? ChatColor.GREEN + String.valueOf(partiesOpt) : ChatColor.RED + String.valueOf(partiesOpt)) + ChatColor.YELLOW +  ")\n";
			}
			text += ChatColor.GREEN + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("done");
			return text;
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase("1")) {
				tempKey = CK.OPT_USE_DUNGEONSXL_PLUGIN;
				tempPrompt = new MultiplayerPrompt();
				return new TrueFalsePrompt();
			} else if (input.equalsIgnoreCase("2")) {
				tempKey = CK.OPT_USE_PARTIES_PLUGIN;
				tempPrompt = new MultiplayerPrompt();
				return new TrueFalsePrompt();
			} else if (input.equalsIgnoreCase("3")) {
				tempKey = null;
				tempPrompt = null;
				return factory.returnToMenu();
			}
			return null;
		}
	}
}