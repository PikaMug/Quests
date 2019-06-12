/*******************************************************************************************************
 * Continued by PikaMug (formerly HappyPikachu) with permission from _Blackvein_. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

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
	
	private final Quests plugin;
	private final QuestFactory factory;
	private String tempKey;
	private StringPrompt tempPrompt;

	public OptionsPrompt(Quests plugin, QuestFactory qf) {
		super("1", "2", "3");
		this.plugin = plugin;
		factory = qf;
	}

	@Override
	public String getPromptText(ConversationContext context) {
		String text;
		String lang = Lang.get("optionsTitle");
		lang = lang.replace("<quest>", ChatColor.AQUA + (String) context.getSessionData(CK.Q_NAME) + ChatColor.DARK_GREEN);
		text = ChatColor.DARK_AQUA + lang + "\n";
		text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.GOLD + " - " + Lang.get("optGeneral") + "\n";
		text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.GOLD + " - " + Lang.get("optMultiplayer") + "\n";
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
			String text = ChatColor.DARK_GREEN + "- " + Lang.get("optGeneral") + " -\n";
			if (context.getSessionData(CK.OPT_ALLOW_COMMANDS) == null) {
				boolean defaultOpt = new Options().getAllowCommands();
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("optAllowCommands") + " (" 
						+ (defaultOpt ? ChatColor.GREEN + String.valueOf(defaultOpt) : ChatColor.RED + String.valueOf(defaultOpt)) + ChatColor.YELLOW + ")\n";
			} else {
				boolean commandsOpt = (Boolean) context.getSessionData(CK.OPT_ALLOW_COMMANDS);
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("optAllowCommands") + " (" 
						+ (commandsOpt ? ChatColor.GREEN + String.valueOf(commandsOpt) : ChatColor.RED + String.valueOf(commandsOpt)) + ChatColor.YELLOW + ")\n";
			}
			if (context.getSessionData(CK.OPT_ALLOW_QUITTING) == null) {
				boolean defaultOpt = new Options().getAllowQuitting();
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("optAllowQuitting") + " (" 
						+ (defaultOpt ? ChatColor.GREEN + String.valueOf(defaultOpt) : ChatColor.RED + String.valueOf(defaultOpt)) + ChatColor.YELLOW + ")\n";
			} else {
				boolean quittingOpt = (Boolean) context.getSessionData(CK.OPT_ALLOW_QUITTING);
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("optAllowQuitting") + " (" 
						+ (quittingOpt ? ChatColor.GREEN + String.valueOf(quittingOpt) : ChatColor.RED + String.valueOf(quittingOpt)) + ChatColor.YELLOW + ")\n";
			}
			text += ChatColor.GREEN + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("done");
			return text;
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase("1")) {
				tempKey = CK.OPT_ALLOW_COMMANDS;
				tempPrompt = new GeneralPrompt();
				return new TrueFalsePrompt();
			} else if (input.equalsIgnoreCase("2")) {
				tempKey = CK.OPT_ALLOW_QUITTING;
				tempPrompt = new GeneralPrompt();
				return new TrueFalsePrompt();
			} else if (input.equalsIgnoreCase("3")) {
				tempKey = null;
				tempPrompt = null;
				try {
					return new OptionsPrompt(plugin, factory);
				} catch (Exception e) {
					context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateCriticalError"));
					return Prompt.END_OF_CONVERSATION;
				}
			}
			return null;
		}
	}
	
	private class MultiplayerPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			String text = ChatColor.DARK_GREEN + "- " + Lang.get("optMultiplayer") + " -\n";
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
				try {
					return new OptionsPrompt(plugin, factory);
				} catch (Exception e) {
					context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateCriticalError"));
					return Prompt.END_OF_CONVERSATION;
				}
			}
			return null;
		}
	}
}