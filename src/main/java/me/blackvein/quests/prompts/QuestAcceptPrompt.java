package me.blackvein.quests.prompts;

import java.text.MessageFormat;
import java.util.LinkedList;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import me.blackvein.quests.Quest;
import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;
import me.blackvein.quests.util.Lang;

public class QuestAcceptPrompt extends StringPrompt {

	final Quests plugin;
	Quester quester;
	LinkedList<Quest> quests;

	public QuestAcceptPrompt(Quests plugin) {
		this.plugin = plugin;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getPromptText(ConversationContext cc) {
		quests = (LinkedList<Quest>) cc.getSessionData("quests");
		quester = plugin.getQuester(((Player) cc.getForWhom()).getUniqueId());
		String npc = (String) cc.getSessionData("npc");
		String text = Lang.get("questNPCListTitle");
		text = text.replaceAll("<npc>", npc);
		String menu = text + "\n";
		for (int i = 1; i <= quests.size(); i++) {
			Quest quest = quests.get(i - 1);
			if (quester.completedQuests.contains(quest.getName())) {
				menu += ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "" + i + ". " + ChatColor.RESET + "" + ChatColor.GREEN + "" + ChatColor.ITALIC + quest.getName() + ChatColor.RESET + "" + ChatColor.GREEN + " " + Lang.get("redoCompleted") + "\n";
			} else {
				menu += ChatColor.GOLD + "" + ChatColor.BOLD + "" + i + ". " + ChatColor.RESET + "" + ChatColor.YELLOW + "" + ChatColor.ITALIC + quest.getName() + "\n";
			}
		}
		menu += ChatColor.GOLD + "" + ChatColor.BOLD + "" + (quests.size() + 1) + ". " + ChatColor.RESET + "" + ChatColor.GRAY + Lang.get("cancel") + "\n";
		menu += ChatColor.WHITE + Lang.get("enterAnOption");
		return menu;
	}

	@Override
	public Prompt acceptInput(ConversationContext cc, String input) {
		int numInput = -1;
		try {
			numInput = Integer.parseInt(input);
		} catch (NumberFormatException e) {
			// Continue
		}
		if (input.equalsIgnoreCase(Lang.get("cancel")) || numInput == (quests.size() + 1)) {
			cc.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("cancelled"));
			return Prompt.END_OF_CONVERSATION;
		} else {
			Quest q = null;
			for (Quest quest : quests) {
				if (quest.getName().equalsIgnoreCase(input)) {
					q = quest;
					break;
				}
			}
			if (q == null) {
				for (Quest quest : quests) {
					if (numInput == (quests.indexOf(quest) + 1)) {
						q = quest;
						break;
					}
				}
			}
			if (q == null) {
				for (Quest quest : quests) {
					if (StringUtils.containsIgnoreCase(quest.getName(), input)) {
						q = quest;
						break;
					}
				}
			}
			if (q == null) {
				cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidSelection"));
				return new QuestAcceptPrompt(plugin);
			} else {
				Player player = quester.getPlayer();
				if (!quester.completedQuests.contains(q.name)) {
					if (quester.currentQuests.size() < Quests.maxQuests || Quests.maxQuests < 1) {
						if (q.testRequirements(quester)) {
							quester.questToTake = q.name;
							String s = extracted(quester);
							for (String msg : s.split("<br>")) {
								player.sendMessage(msg);
							}
							plugin.conversationFactory.buildConversation((Conversable) player).begin();
						} else {
							player.sendMessage(q.failRequirements);
						}
					} else if (quester.currentQuests.containsKey(q) == false) {
						String msg = Lang.get("questMaxAllowed");
						msg = msg.replaceAll("<number>", String.valueOf(Quests.maxQuests));
						player.sendMessage(ChatColor.YELLOW + msg);
					}
				} else if (quester.completedQuests.contains(q.name)) {
					if (quester.currentQuests.size() < Quests.maxQuests || Quests.maxQuests < 1) {
						if (quester.getDifference(q) > 0) {
							String early = Lang.get("questTooEarly");
							early = early.replaceAll("<quest>", ChatColor.AQUA + q.name + ChatColor.YELLOW);
							early = early.replaceAll("<time>", ChatColor.DARK_PURPLE + Quests.getTime(quester.getDifference(q)) + ChatColor.YELLOW);
							player.sendMessage(ChatColor.YELLOW + early);
						} else if (q.redoDelay < 0) {
							String completed = Lang.get("questAlreadyCompleted");
							completed = completed.replaceAll("<quest>", ChatColor.AQUA + q.name + ChatColor.YELLOW);
							player.sendMessage(ChatColor.YELLOW + completed);
						} else {
							quester.questToTake = q.name;
							String s = extracted(quester);
							for (String msg : s.split("<br>")) {
								player.sendMessage(msg);
							}
							plugin.conversationFactory.buildConversation((Conversable) player).begin();
						}
					} else if (quester.currentQuests.containsKey(q) == false) {
						String msg = Lang.get("questMaxAllowed");
						msg = msg.replaceAll("<number>", String.valueOf(Quests.maxQuests));
						player.sendMessage(ChatColor.YELLOW + msg);
					}
				}
				return Prompt.END_OF_CONVERSATION;
			}
		}
	}

	private String extracted(final Quester quester) {
		return MessageFormat.format("{0}- {1}{2}{3} -\n\n{4}{5}\n", ChatColor.GOLD, ChatColor.DARK_PURPLE, quester.questToTake, ChatColor.GOLD, ChatColor.RESET, plugin.getQuest(quester.questToTake).description);
	}
}
