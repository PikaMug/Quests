/*******************************************************************************************************
 * Continued by FlyingPikachu/HappyPikachu with permission from _Blackvein_. All rights reserved.
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

import me.blackvein.quests.QuestFactory;
import me.blackvein.quests.Quests;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.Lang;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.FixedSetPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

public class PlannerPrompt extends FixedSetPrompt {
	
	final Quests quests;
	final QuestFactory factory;

	public PlannerPrompt(Quests plugin, QuestFactory qf) {
		super("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11");
		quests = plugin;
		factory = qf;
	}

	@Override
	public String getPromptText(ConversationContext context) {
		String text;
		String lang = Lang.get("plannerTitle");
		lang = lang.replaceAll("<quest>", ChatColor.BLUE + (String) context.getSessionData(CK.Q_NAME));
		text = ChatColor.AQUA + lang + "\n";
		if (context.getSessionData(CK.PLN_START_DATE) == null) {
			text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("plnStart") + " " + ChatColor.GRAY + "(" + Lang.get("noneSet") + ")\n";
		} else {
			text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("plnStart") + " (" + Quests.getTime((Long) context.getSessionData(CK.PLN_START_DATE)) + ")\n";
		}
		if (context.getSessionData(CK.PLN_END_DATE) == null) {
			text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("plnEnd") + " " + ChatColor.GRAY + "(" + Lang.get("noneSet") + ")\n";
		} else {
			text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("plnEnd") + " (" + Quests.getTime((Long) context.getSessionData(CK.PLN_END_DATE)) + ")\n";
		}
		if (context.getSessionData(CK.PLN_REPEAT_CYCLE) == null) {
			text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("plnRepeat") + " " + ChatColor.GRAY + "(" + Lang.get("noneSet") + ")\n";
		} else {
			text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("plnRepeat") + " (" + Quests.getTime((Long) context.getSessionData(CK.PLN_REPEAT_CYCLE)) + ")\n";
		}
		if (context.getSessionData(CK.PLN_COOLDOWN) == null) {
			text += ChatColor.BLUE + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("plnCooldown") + " " + ChatColor.GRAY + "(" + Lang.get("noneSet") + ")\n";
		} else {
			text += ChatColor.BLUE + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("plnCooldown") + " (" + Quests.getTime((Long) context.getSessionData(CK.PLN_COOLDOWN)) + ")\n";
		}
		text += ChatColor.GREEN + "" + ChatColor.BOLD + "5" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("done");
		return text;
	}

	@Override
	protected Prompt acceptValidatedInput(ConversationContext context, String input) {
		if (input.equalsIgnoreCase("1")) {
			return new StartPrompt();
		} else if (input.equalsIgnoreCase("2")) {
			return new EndPrompt();
		} else if (input.equalsIgnoreCase("3")) {
			return new RepeatPrompt();
		} else if (input.equalsIgnoreCase("4")) {
			return new CooldownPrompt();
		} else if (input.equalsIgnoreCase("5")) {
			return factory.returnToMenu();
		}
		return null;
	}
	
	private class StartPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			return ChatColor.YELLOW + Lang.get("plnStartPrompt");
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
				return new PlannerPrompt(quests, factory);
			}
			if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
				context.setSessionData(CK.PLN_START_DATE, null);
			}
			long delay;
			try {
				int i = Integer.parseInt(input);
				delay = i * 1000;
			} catch (NumberFormatException e) {
				context.getForWhom().sendRawMessage(ChatColor.ITALIC + "" + ChatColor.RED + input + ChatColor.RESET + ChatColor.RED + " " + Lang.get("stageEditorInvalidNumber"));
				return new StartPrompt();
			}
			if (delay < -1) {
				context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorPositiveAmount"));
			} else if (delay == 0) {
				context.setSessionData(CK.PLN_START_DATE, null);
			} else if (delay != -1) {
				context.setSessionData(CK.PLN_START_DATE, delay);
			}
			return new PlannerPrompt(quests, factory);
		}
	}
	
	private class EndPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			return ChatColor.YELLOW + Lang.get("plnEndPrompt");
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
				return new PlannerPrompt(quests, factory);
			}
			if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
				context.setSessionData(CK.PLN_END_DATE, null);
			}
			long delay;
			try {
				int i = Integer.parseInt(input);
				delay = i * 1000;
			} catch (NumberFormatException e) {
				context.getForWhom().sendRawMessage(ChatColor.ITALIC + "" + ChatColor.RED + input + ChatColor.RESET + ChatColor.RED + " " + Lang.get("stageEditorInvalidNumber"));
				return new EndPrompt();
			}
			if (delay < -1) {
				context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorPositiveAmount"));
			} else if (delay == 0) {
				context.setSessionData(CK.PLN_END_DATE, null);
			} else if (delay != -1) {
				context.setSessionData(CK.PLN_END_DATE, delay);
			}
			return new PlannerPrompt(quests, factory);
		}
	}
	
	private class RepeatPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			return ChatColor.YELLOW + Lang.get("plnRepeatPrompt");
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
				return new PlannerPrompt(quests, factory);
			}
			if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
				context.setSessionData(CK.PLN_REPEAT_CYCLE, null);
			}
			long delay;
			try {
				int i = Integer.parseInt(input);
				delay = i * 1000;
			} catch (NumberFormatException e) {
				context.getForWhom().sendRawMessage(ChatColor.ITALIC + "" + ChatColor.RED + input + ChatColor.RESET + ChatColor.RED + " " + Lang.get("stageEditorInvalidNumber"));
				return new RepeatPrompt();
			}
			if (delay < -1) {
				context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorPositiveAmount"));
			} else if (delay == 0) {
				context.setSessionData(CK.PLN_REPEAT_CYCLE, null);
			} else if (delay != -1) {
				context.setSessionData(CK.PLN_REPEAT_CYCLE, delay);
			}
			return new PlannerPrompt(quests, factory);
		}
	}
	
	private class CooldownPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			return ChatColor.YELLOW + Lang.get("plnCooldownPrompt");
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
				return new PlannerPrompt(quests, factory);
			}
			if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
				context.setSessionData(CK.PLN_COOLDOWN, null);
			}
			long delay;
			try {
				int i = Integer.parseInt(input);
				delay = i * 1000;
			} catch (NumberFormatException e) {
				context.getForWhom().sendRawMessage(ChatColor.ITALIC + "" + ChatColor.RED + input + ChatColor.RESET + ChatColor.RED + " " + Lang.get("stageEditorInvalidNumber"));
				return new CooldownPrompt();
			}
			if (delay < -1) {
				context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorPositiveAmount"));
			} else if (delay == 0) {
				context.setSessionData(CK.PLN_COOLDOWN, null);
			} else if (delay != -1) {
				context.setSessionData(CK.PLN_COOLDOWN, delay);
			}
			return new PlannerPrompt(quests, factory);
		}
	}
}
