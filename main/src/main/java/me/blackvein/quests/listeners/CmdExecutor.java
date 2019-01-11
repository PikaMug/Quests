package me.blackvein.quests.listeners;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import me.blackvein.quests.Quest;
import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;
import me.blackvein.quests.Requirements;
import me.blackvein.quests.Stage;
import me.blackvein.quests.exceptions.InvalidStageException;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.conversations.Conversable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class CmdExecutor implements CommandExecutor{
	private final Quests plugin;
	public HashMap<String, Integer> commands = new HashMap<String, Integer>();
	public HashMap<String, Integer> adminCommands = new HashMap<String, Integer>();
	
	public CmdExecutor(Quests plugin) {
		this.plugin = plugin;
		init();
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String label, String[] args) {
		if (cs instanceof Player) {
			if (plugin.checkQuester(((Player) cs).getUniqueId()) == true) {
				cs.sendMessage(ChatColor.RED + Lang.get((Player) cs, "noPermission"));
				return true;
			}
		}
		String error = checkCommand(cmd.getName(), args);
		if (error != null) {
			cs.sendMessage(error);
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("quest")) {
			return questCommandHandler(cs, args);
		} else if (cmd.getName().equalsIgnoreCase("quests")) {
			return questActionsCommandHandler(cs, args);
		} else if (cmd.getName().equalsIgnoreCase("questadmin")) {
			return questAdminCommandHandler(cs, args);
		}
		return false;
	}
	
	private void init() {
		// [] - required
		// {} - optional
		if (plugin.getSettings().canTranslateSubCommands()) {
			commands.put(Lang.get("COMMAND_LIST"), 1); // list {page}
			commands.put(Lang.get("COMMAND_TAKE"), 2); // take [quest]
			commands.put(Lang.get("COMMAND_QUIT"), 2); // quit [quest]
			commands.put(Lang.get("COMMAND_EDITOR"), 1); // editor
			commands.put(Lang.get("COMMAND_EVENTS_EDITOR"), 1); // events
			commands.put(Lang.get("COMMAND_STATS"), 1); // stats
			commands.put(Lang.get("COMMAND_TOP"), 2); // top {number}
			commands.put(Lang.get("COMMAND_INFO"), 1); // info
			commands.put(Lang.get("COMMAND_JOURNAL"), 1); // journal
			adminCommands.put(Lang.get("COMMAND_QUESTADMIN_STATS"), 2); // stats [player]
			adminCommands.put(Lang.get("COMMAND_QUESTADMIN_GIVE"), 3); // give [player] [quest]
			adminCommands.put(Lang.get("COMMAND_QUESTADMIN_QUIT"), 3); // quit [player] [quest]
			adminCommands.put(Lang.get("COMMAND_QUESTADMIN_REMOVE"), 3); // remove [player] [quest]
			adminCommands.put(Lang.get("COMMAND_QUESTADMIN_POINTS"), 3); // points [player] [amount]
			adminCommands.put(Lang.get("COMMAND_QUESTADMIN_TAKEPOINTS"), 3); // takepoints [player] [amount]
			adminCommands.put(Lang.get("COMMAND_QUESTADMIN_GIVEPOINTS"), 3); // givepoints [player] [amount]
			adminCommands.put(Lang.get("COMMAND_QUESTADMIN_POINTSALL"), 2); // pointsall [amount]
			adminCommands.put(Lang.get("COMMAND_QUESTADMIN_FINISH"), 3); // finish [player] [quest]
			adminCommands.put(Lang.get("COMMAND_QUESTADMIN_NEXTSTAGE"), 3); // nextstage [player] [quest]
			adminCommands.put(Lang.get("COMMAND_QUESTADMIN_SETSTAGE"), 4); // setstage [player] [quest] [stage]
			adminCommands.put(Lang.get("COMMAND_QUESTADMIN_RESET"), 2); // reset [player]
			adminCommands.put(Lang.get("COMMAND_QUESTADMIN_TOGGLEGUI"), 2); // togglegui [npc id]
			adminCommands.put(Lang.get("COMMAND_QUESTADMIN_RELOAD"), 1); // reload
		} else {
			commands.put("list", 1); // list {page}
			commands.put("take", 2); // take [quest]
			commands.put("quit", 2); // quit [quest]
			commands.put("editor", 1); // editor
			commands.put("events", 1); // events
			commands.put("stats", 1); // stats
			commands.put("top", 2); // top [number]
			commands.put("info", 1); // info
			commands.put("journal", 1); // journal
			adminCommands.put("stats", 2); // stats [player]
			adminCommands.put("give", 3); // give [player] [quest]
			adminCommands.put("quit", 3); // quit [player] [quest]
			adminCommands.put("remove", 3); // remove [player] [quest]
			adminCommands.put("points", 3); // points [player] [amount]
			adminCommands.put("takepoints", 3); // takepoints [player] [amount]
			adminCommands.put("givepoints", 3); // givepoints [player] [amount]
			adminCommands.put("pointsall", 2); // pointsall [amount]
			adminCommands.put("finish", 3); // finish [player] [quest]
			adminCommands.put("nextstage", 3); // nextstage [player] [quest]
			adminCommands.put("setstage", 4); // setstage [player] [quest] [stage]
			adminCommands.put("reset", 2); // reset [player]
			adminCommands.put("togglegui", 2); // togglegui [npc id]
			adminCommands.put("reload", 1); // reload
		}
	}

	public String checkCommand(String cmd, String[] args) {
		if (cmd.equalsIgnoreCase("quest") || args.length == 0) {
			return null;
		}
		if (cmd.equalsIgnoreCase("quests")) {
			if (commands.containsKey(args[0].toLowerCase())) {
				int min = commands.get(args[0].toLowerCase());
				if (args.length < min)
					return getQuestsCommandUsage(args[0]);
				else
					return null;
			}
			return ChatColor.YELLOW + Lang.get("questsUnknownCommand");
		} else if (cmd.equalsIgnoreCase("questsadmin") || cmd.equalsIgnoreCase("questadmin")) {
			if (adminCommands.containsKey(args[0].toLowerCase())) {
				int min = adminCommands.get(args[0].toLowerCase());
				if (args.length < min)
					return getQuestadminCommandUsage(args[0]);
				else
					return null;
			}
			return ChatColor.YELLOW + Lang.get("questsUnknownAdminCommand");
		}
		return "NULL";
	}
	
	private boolean questCommandHandler(final CommandSender cs, String[] args) {
		if (cs instanceof Player) {
			if (((Player) cs).hasPermission("quests.quest")) {
				if (args.length == 0) {
					Player player = (Player) cs;
					Quester quester = plugin.getQuester(player.getUniqueId());
					if (quester.getCurrentQuests().isEmpty() == false) {
						for (Quest q : quester.getCurrentQuests().keySet()) {
							Stage stage = quester.getCurrentStage(q);
							q.updateCompass(quester, stage);
							if (plugin.getQuester(player.getUniqueId()).getQuestData(q).delayStartTime == 0) {
								String msg = Lang.get(player, "questObjectivesTitle");
								msg = msg.replace("<quest>", q.getName());
								player.sendMessage(ChatColor.GOLD + msg);
								plugin.showObjectives(q, quester, false);
							}
						}
					} else {
						player.sendMessage(ChatColor.YELLOW + Lang.get(player, "noActiveQuest"));
					}
				} else {
					showQuestDetails(cs, args);
				}
			} else {
				cs.sendMessage(ChatColor.RED + Lang.get("NoPermission"));
				return true;
			}
		} else {
			cs.sendMessage(ChatColor.YELLOW + Lang.get("consoleError"));
			return true;
		}
		return true;
	}
	
	private boolean questActionsCommandHandler(final CommandSender cs, String[] args) {
		if (cs instanceof Player) {
			if (args.length == 0) {
				questsHelp(cs);
				return true;
			} else {
				boolean translateSubCommands = plugin.getSettings().canTranslateSubCommands();
				if (args[0].equalsIgnoreCase(translateSubCommands ? Lang.get("COMMAND_LIST") : "list")) {
					questsList(cs, args);
				} else if (args[0].equalsIgnoreCase(translateSubCommands ? Lang.get("COMMAND_TAKE") : "take")) {
					questsTake((Player) cs, args);
				} else if (args[0].equalsIgnoreCase(translateSubCommands ? Lang.get("COMMAND_QUIT") : "quit")) {
					questsQuit((Player) cs, args);
				} else if (args[0].equalsIgnoreCase(translateSubCommands ? Lang.get("COMMAND_STATS") : "stats")) {
					questsStats(cs, null);
				} else if (args[0].equalsIgnoreCase(translateSubCommands ? Lang.get("COMMAND_JOURNAL") : "journal")) {
					questsJournal((Player) cs);
				} else if (args[0].equalsIgnoreCase(translateSubCommands ? Lang.get("COMMAND_TOP") : "top")) {
					questsTop(cs, args);
				} else if (args[0].equalsIgnoreCase(translateSubCommands ? Lang.get("COMMAND_EDITOR") : "editor")) {
					questsEditor(cs);
				} else if (args[0].equalsIgnoreCase(translateSubCommands ? Lang.get("COMMAND_EVENTS_EDITOR") : "events")) {
					questsEvents(cs);
				} else if (args[0].equalsIgnoreCase(translateSubCommands ? Lang.get("COMMAND_INFO") : "info")) {
					questsInfo(cs);
				} else {
					cs.sendMessage(ChatColor.YELLOW + Lang.get("questsUnknownCommand"));
					return true;
				}
			}
		} else {
			cs.sendMessage(ChatColor.YELLOW + Lang.get("consoleError"));
			return true;
		}
		return true;
	}
	
	private boolean questAdminCommandHandler(final CommandSender cs, String[] args) {
		if (args.length == 0) {
			adminHelp(cs);
			return true;
		}
		boolean translateSubCommands = plugin.getSettings().canTranslateSubCommands();
		if (args[0].equalsIgnoreCase(translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_STATS") : "stats")) {
			adminStats(cs, args);
		} else if (args[0].equalsIgnoreCase(translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_GIVE") : "give")) {
			adminGive(cs, args);
		} else if (args[0].equalsIgnoreCase(translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_QUIT") : "quit")) {
			adminQuit(cs, args);
		} else if (args[0].equalsIgnoreCase(translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_POINTS") : "points")) {
			adminPoints(cs, args);
		} else if (args[0].equalsIgnoreCase(translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_TAKEPOINTS") : "takepoints")) {
			adminTakePoints(cs, args);
		} else if (args[0].equalsIgnoreCase(translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_GIVEPOINTS") : "givepoints")) {
			adminGivePoints(cs, args);
		} else if (args[0].equalsIgnoreCase(translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_POINTSALL") : "pointsall")) {
			adminPointsAll(cs, args);
		} else if (args[0].equalsIgnoreCase(translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_FINISH") : "finish")) {
			adminFinish(cs, args);
		} else if (args[0].equalsIgnoreCase(translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_NEXTSTAGE") : "nextstage")) {
			adminNextStage(cs, args);
		} else if (args[0].equalsIgnoreCase(translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_SETSTAGE") : "setstage")) {
			adminSetStage(cs, args);
		} else if (args[0].equalsIgnoreCase(translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_RESET") : "reset")) {
			adminReset(cs, args);
		} else if (args[0].equalsIgnoreCase(translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_REMOVE") : "remove")) {
			adminRemove(cs, args);
		} else if (args[0].equalsIgnoreCase(translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_TOGGLEGUI") : "togglegui")) {
			adminToggieGUI(cs, args);
		} else if (args[0].equalsIgnoreCase(translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_RELOAD") : "reload")) {
			adminReload(cs);
		} else {
			cs.sendMessage(ChatColor.YELLOW + Lang.get("questsUnknownAdminCommand"));
		}
		return true;
	}
	
	public void showQuestDetails(final CommandSender cs, String[] args) {
		if (((Player) cs).hasPermission("quests.questinfo")) {
			String name = "";
			if (args.length == 1) {
				name = args[0].toLowerCase();
			} else {
				int index = 0;
				for (String s : args) {
					if (index == (args.length - 1)) {
						name = name + s.toLowerCase();
					} else {
						name = name + s.toLowerCase() + " ";
					}
					index++;
				}
			}
			Quest q = plugin.getQuest(name);
			if (q != null) {
				Player player = (Player) cs;
				Quester quester = plugin.getQuester(player.getUniqueId());
				cs.sendMessage(ChatColor.GOLD + "- " + q.getName() + " -");
				cs.sendMessage(" ");
				/*if (q.redoDelay > -1) {
					if (q.redoDelay == 0) {
						cs.sendMessage(ChatColor.DARK_AQUA + Lang.get("readoable"));
					} else {
						String msg = Lang.get("redoableEvery");
						msg = msg.replace("<time>", ChatColor.AQUA + getTime(q.redoDelay) + ChatColor.DARK_AQUA);
						cs.sendMessage(ChatColor.DARK_AQUA + msg);
					}
				}*/
				if (q.getNpcStart() != null) {
					String msg = Lang.get("speakTo");
					msg = msg.replace("<npc>", q.getNpcStart().getName());
					cs.sendMessage(ChatColor.YELLOW + msg);
				} else {
					cs.sendMessage(ChatColor.YELLOW + q.getDescription());
				}
				cs.sendMessage(" ");
				if (plugin.getSettings().canShowQuestReqs() == true) {
					cs.sendMessage(ChatColor.GOLD + Lang.get("requirements"));
					Requirements reqs = q.getRequirements();
					if (reqs.getPermissions().isEmpty() == false) {
						for (String perm : reqs.getPermissions()) {
							if (plugin.getDependencies().getVaultPermission().has(player, perm)) {
								cs.sendMessage(ChatColor.GREEN + Lang.get("permissionDisplay") + " " + perm);
							} else {
								cs.sendMessage(ChatColor.RED + Lang.get("permissionDisplay") + " " + perm);
							}
						}
					}
					if (reqs.getHeroesPrimaryClass() != null) {
						if (plugin.testPrimaryHeroesClass(reqs.getHeroesPrimaryClass(), player.getUniqueId())) {
							cs.sendMessage(ChatColor.BOLD + "" + ChatColor.GREEN + reqs.getHeroesPrimaryClass() + ChatColor.RESET + "" + ChatColor.DARK_GREEN + " " + Lang.get("heroesClass"));
						} else {
							cs.sendMessage(ChatColor.BOLD + "" + ChatColor.DARK_RED + reqs.getHeroesPrimaryClass() + ChatColor.RESET + "" + ChatColor.RED + " " + Lang.get("heroesClass"));
						}
					}
					if (reqs.getHeroesSecondaryClass() != null) {
						if (plugin.testSecondaryHeroesClass(reqs.getHeroesSecondaryClass(), player.getUniqueId())) {
							cs.sendMessage(ChatColor.BOLD + "" + ChatColor.DARK_RED + reqs.getHeroesSecondaryClass() + ChatColor.RESET + "" + ChatColor.RED + " " + Lang.get("heroesClass"));
						} else {
							cs.sendMessage(ChatColor.BOLD + "" + ChatColor.GREEN + reqs.getHeroesSecondaryClass() + ChatColor.RESET + "" + ChatColor.DARK_GREEN + " " + Lang.get("heroesClass"));
						}
					}
					if (reqs.getMcmmoSkills().isEmpty() == false) {
						for (String skill : reqs.getMcmmoSkills()) {
							int level = Quests.getMCMMOSkillLevel(Quests.getMcMMOSkill(skill), player.getName());
							int req = reqs.getMcmmoAmounts().get(reqs.getMcmmoSkills().indexOf(skill));
							String skillName = MiscUtil.getCapitalized(skill);
							if (level >= req) {
								cs.sendMessage(ChatColor.GREEN + skillName + " " + Lang.get("mcMMOLevel") + " " + req);
							} else {
								cs.sendMessage(ChatColor.RED + skillName + " " + Lang.get("mcMMOLevel") + " " + req);
							}
						}
					}
					if (reqs.getQuestPoints() != 0) {
						if (quester.getQuestPoints() >= reqs.getQuestPoints()) {
							cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.GREEN + reqs.getQuestPoints() + " " + Lang.get("questPoints"));
						} else {
							cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.RED + reqs.getQuestPoints() + " " + Lang.get("questPoints"));
						}
					}
					if (reqs.getMoney() != 0) {
						if (plugin.getDependencies().getVaultEconomy() != null && plugin.getDependencies().getVaultEconomy().getBalance(quester.getOfflinePlayer()) >= reqs.getMoney()) {
							if (reqs.getMoney() == 1) {
								cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.GREEN + reqs.getMoney() + " " + plugin.getCurrency(false));
							} else {
								cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.GREEN + reqs.getMoney() + " " + plugin.getCurrency(true));
							}
						} else {
							if (reqs.getMoney() == 1) {
								cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.RED + reqs.getMoney() + " " + plugin.getCurrency(false));
							} else {
								cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.RED + reqs.getMoney() + " " + plugin.getCurrency(true));
							}
						}
					}
					if (reqs.getItems().isEmpty() == false) {
						for (ItemStack is : reqs.getItems()) {
							if (plugin.getQuester(player.getUniqueId()).hasItem(is) == true) {
								cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.GREEN + ItemUtil.getString(is));
							} else {
								cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.RED + ItemUtil.getString(is));
							}
						}
					}
					if (reqs.getNeededQuests().isEmpty() == false) {
						for (String s : reqs.getNeededQuests()) {
							if (quester.getCompletedQuests().contains(s)) {
								cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.GREEN + Lang.get("complete") + " " + ChatColor.ITALIC + s);
							} else {
								cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.RED + Lang.get("complete") + " " + ChatColor.ITALIC + s);
							}
						}
					}
					if (reqs.getBlockQuests().isEmpty() == false) {
						for (String s : reqs.getBlockQuests()) {
							if (quester.getCompletedQuests().contains(s)) {
								String msg = Lang.get("haveCompleted");
								msg = msg.replace("<quest>", ChatColor.ITALIC + "" + ChatColor.DARK_PURPLE + s + ChatColor.RED);
								cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.RED + msg);
							} else {
								String msg = Lang.get("cannotComplete");
								msg = msg.replace("<quest>", ChatColor.ITALIC + "" + ChatColor.DARK_PURPLE + s + ChatColor.GREEN);
								cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.GREEN + msg);
							}
						}
					}
				}
			} else {
				cs.sendMessage(ChatColor.YELLOW + Lang.get("questNotFound"));
			}
		} else {
			cs.sendMessage(ChatColor.RED + Lang.get("NoPermission"));
		}
	}
	
	private boolean questsInfo(final CommandSender cs) {
		if (cs.hasPermission("quests.info")) {
			cs.sendMessage(ChatColor.GOLD + Lang.get("quests") + " " + plugin.getDescription().getVersion());
			cs.sendMessage(ChatColor.GOLD + Lang.get("createdBy") + " " + ChatColor.DARK_RED + "Blackvein"
					+ ChatColor.GOLD + " " + Lang.get("continuedBy") + " " + ChatColor.DARK_RED + "FlyingPikachu");
			cs.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.UNDERLINE + "https://www.spigotmc.org/resources/quests.3711/");
		}
		return true;
	}

	private boolean questsEvents(final CommandSender cs) {
		if (cs.hasPermission("quests.editor.*") || cs.hasPermission("quests.events.editor")) {
			plugin.getEventFactory().getConversationFactory().buildConversation((Conversable) cs).begin();
		} else {
			cs.sendMessage(ChatColor.RED + Lang.get("NoPermission"));
		}
		return true;
	}

	private boolean questsEditor(final CommandSender cs) {
		if (cs.hasPermission("quests.editor.*") || cs.hasPermission("quests.editor.editor")) {
			plugin.getQuestFactory().getConversationFactory().buildConversation((Conversable) cs).begin();
		} else {
			cs.sendMessage(ChatColor.RED + Lang.get("NoPermission"));
		}
		return true;
	}

	private boolean questsTop(final CommandSender cs, String[] args) {
		if (args.length > 2) {
			cs.sendMessage(ChatColor.YELLOW + Lang.get("COMMAND_TOP_USAGE"));
		} else {
			int topNumber;
			if (args.length == 1) {
				topNumber = 5; // default
			} else {
				try {
					topNumber = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
					cs.sendMessage(ChatColor.YELLOW + Lang.get("inputNum"));
					return true;
				}
			}
			if (topNumber < 1) {
				cs.sendMessage(ChatColor.YELLOW + Lang.get("inputPosNum"));
				return true;
			}
			File folder = new File(plugin.getDataFolder(), "data");
			File[] playerFiles = folder.listFiles();
			Map<String, Integer> questPoints = new HashMap<String, Integer>();
			if (playerFiles != null) {
				for (File f : playerFiles) {
					if (!f.isDirectory()) {
						FileConfiguration data = new YamlConfiguration();
						try {
							data.load(f);
						} catch (IOException e) {
							e.printStackTrace();
						} catch (InvalidConfigurationException e) {
							e.printStackTrace();
						}
						String name = f.getName().substring(0, (f.getName().indexOf(".")));
						questPoints.put(name, data.getInt("quest-points"));
					}
				}
			}
			LinkedHashMap<String, Integer> sortedMap = (LinkedHashMap<String, Integer>) sort(questPoints);
			int numPrinted = 0;
			String msg = Lang.get("topQuestersTitle");
			msg = msg.replace("<number>", ChatColor.DARK_PURPLE + "" + topNumber + ChatColor.GOLD);
			cs.sendMessage(ChatColor.GOLD + msg);
			for (String s : sortedMap.keySet()) {
				int i = (Integer) sortedMap.get(s);
				s = s.trim();
				try {
					UUID id = UUID.fromString(s);
					s = Bukkit.getOfflinePlayer(id).getName();
				} catch (IllegalArgumentException e) {
					plugin.getLogger().warning("File name \"" + s + "\"in /data folder is not a valid player UUID!");
					break;
				}
				numPrinted++;
				cs.sendMessage(ChatColor.YELLOW + String.valueOf(numPrinted) + ". " + s + " - " + ChatColor.DARK_PURPLE + i + ChatColor.YELLOW + " " + Lang.get("questPoints"));
				if (numPrinted == topNumber) {
					break;
				}
			}
		}
		return true;
	}

	private void questsStats(final CommandSender cs, String[] args) {
		Quester quester;
		if (args != null) {
			quester = plugin.getQuester(args[1]);
			if (quester == null) {
				cs.sendMessage(ChatColor.YELLOW + Lang.get("playerNotFound"));
				return;
			} else if (Bukkit.getOfflinePlayer(quester.getUUID()).getName() != null) {
				cs.sendMessage(ChatColor.GOLD + "- " + Bukkit.getOfflinePlayer(quester.getUUID()).getName() + " -");
			} else {
				cs.sendMessage(ChatColor.GOLD + "- " + args[1] + " -");
			}
		} else {
			quester = plugin.getQuester(((Player) cs).getUniqueId());
			cs.sendMessage(ChatColor.GOLD + "- " + ((Player) cs).getName() + " -");
		}
		cs.sendMessage(ChatColor.YELLOW + Lang.get("questPointsDisplay") + " " + ChatColor.DARK_PURPLE + quester.getQuestPoints());
		if (quester.getCurrentQuests().isEmpty()) {
			cs.sendMessage(ChatColor.YELLOW + Lang.get("currentQuest") + " " + ChatColor.DARK_PURPLE + Lang.get("none"));
		} else {
			cs.sendMessage(ChatColor.YELLOW + Lang.get("currentQuest"));
			for (Quest q : quester.getCurrentQuests().keySet()) {
				cs.sendMessage(ChatColor.LIGHT_PURPLE + " - " + ChatColor.DARK_PURPLE + q.getName());
			}
		}
		String completed;
		if (quester.getCompletedQuests().isEmpty()) {
			completed = ChatColor.DARK_PURPLE + Lang.get("none");
		} else {
			completed = ChatColor.DARK_PURPLE + "";
			for (String s : quester.getCompletedQuests()) {
				completed += s;
				if (quester.getAmountsCompleted().containsKey(s) && quester.getAmountsCompleted().get(s) > 1) {
					completed += ChatColor.LIGHT_PURPLE + " (x" + quester.getAmountsCompleted().get(s) + ")";
				}
				if (quester.getCompletedQuests().indexOf(s) < (quester.getCompletedQuests().size() - 1)) {
					completed += ", ";
				}
			}
		}
		cs.sendMessage(ChatColor.YELLOW + Lang.get("completedQuestsTitle"));
		cs.sendMessage(completed);
	}

	@SuppressWarnings("deprecation")
	private void questsJournal(final Player player) {
		Quester quester = plugin.getQuester(player.getUniqueId());
		if (quester.hasJournal) {
			Inventory inv = player.getInventory();
			ItemStack[] arr = inv.getContents();
			for (int i = 0; i < arr.length; i++) {
				if (arr[i] != null) {
					if (ItemUtil.isJournal(arr[i])) {
						inv.setItem(i, null);
						break;
					}
				}
			}
			player.sendMessage(ChatColor.YELLOW + Lang.get(player, "journalPutAway"));
			quester.hasJournal = false;
		} else if (player.getItemInHand() == null || player.getItemInHand().getType().equals(Material.AIR)) {
			ItemStack stack = new ItemStack(Material.WRITTEN_BOOK, 1);
			ItemMeta meta = stack.getItemMeta();
			meta.setDisplayName(ChatColor.LIGHT_PURPLE + Lang.get("journalTitle"));
			stack.setItemMeta(meta);
			player.setItemInHand(stack);
			player.sendMessage(ChatColor.YELLOW + Lang.get(player, "journalTaken"));
			quester.hasJournal = true;
			quester.updateJournal();
		} else {
			Inventory inv = player.getInventory();
			ItemStack[] arr = inv.getContents();
			boolean given = false;
			for (int i = 0; i < arr.length; i++) {
				if (arr[i] == null) {
					ItemStack stack = new ItemStack(Material.WRITTEN_BOOK, 1);
					ItemMeta meta = stack.getItemMeta();
					meta.setDisplayName(ChatColor.LIGHT_PURPLE + Lang.get("journalTitle"));
					stack.setItemMeta(meta);
					inv.setItem(i, stack);
					player.sendMessage(ChatColor.YELLOW + Lang.get(player, "journalTaken"));
					given = true;
					break;
				}
			}
			if (given) {
				quester.hasJournal = true;
				quester.updateJournal();
			} else
				player.sendMessage(ChatColor.YELLOW + Lang.get(player, "journalNoRoom"));
		}
	}

	private boolean questsQuit(final Player player, String[] args) {
		if (plugin.getSettings().canAllowQuitting() == true) {
			if (((Player) player).hasPermission("quests.quit")) {
				if (args.length == 1) {
					player.sendMessage(ChatColor.RED + Lang.get(player, "COMMAND_QUIT_HELP"));
					return true;
				}
				Quester quester = plugin.getQuester(player.getUniqueId());
				if (quester.getCurrentQuests().isEmpty() == false) {
					Quest quest = plugin.getQuest(MiscUtil.concatArgArray(args, 1, args.length - 1, ' '));
					if (quest == null) {
						player.sendMessage(ChatColor.RED + Lang.get(player, "questNotFound"));
						return true;
					}
					quester.hardQuit(quest);
					String msg = Lang.get("questQuit");
					msg = msg.replace("<quest>", ChatColor.DARK_PURPLE + quest.getName() + ChatColor.YELLOW);
					player.sendMessage(ChatColor.YELLOW + msg);
					quester.saveData();
					quester.loadData();
					quester.updateJournal();
					return true;
				} else {
					player.sendMessage(ChatColor.YELLOW + Lang.get(player, "noActiveQuest"));
					return true;
				}
			} else {
				player.sendMessage(ChatColor.RED + Lang.get(player, "NoPermission"));
				return true;
			}
		} else {
			player.sendMessage(ChatColor.YELLOW + Lang.get(player, "questQuitDisabled"));
			return true;
		}
	}

	private void questsTake(final Player player, String[] args) {
		if (plugin.getSettings().canAllowCommands() == true) {
			if (((Player) player).hasPermission("quests.take")) {
				if (args.length == 1) {
					player.sendMessage(ChatColor.YELLOW + Lang.get(player, "COMMAND_TAKE_USAGE"));
				} else {
					String name = null;
					if (args.length == 2) {
						name = args[1].toLowerCase();
					} else {
						boolean first = true;
						int lastIndex = (args.length - 1);
						int index = 0;
						for (String s : args) {
							if (index != 0) {
								if (first) {
									first = false;
									if (args.length > 2) {
										name = s.toLowerCase() + " ";
									} else {
										name = s.toLowerCase();
									}
								} else if (index == lastIndex) {
									name = name + s.toLowerCase();
								} else {
									name = name + s.toLowerCase() + " ";
								}
							}
							index++;
						}
					}
					Quest questToFind = plugin.getQuest(name);
					if (questToFind != null) {
						final Quest q = questToFind;
						final Quester quester = plugin.getQuester(player.getUniqueId());
						if (quester.getCurrentQuests().size() >= plugin.getSettings().getMaxQuests() && plugin.getSettings().getMaxQuests() > 0) {
							String msg = Lang.get(player, "questMaxAllowed");
							msg = msg.replace("<number>", String.valueOf(plugin.getSettings().getMaxQuests()));
							player.sendMessage(ChatColor.YELLOW + msg);
						} else if (quester.getCurrentQuests().containsKey(q)) {
							String msg = Lang.get(player, "questAlreadyOn");
							player.sendMessage(ChatColor.YELLOW + msg);
						} else if (quester.getCompletedQuests().contains(q.getName()) && q.getPlanner().getCooldown() < 0) {
							String msg = Lang.get(player, "questAlreadyCompleted");
							msg = msg.replace("<quest>", ChatColor.DARK_PURPLE + q.getName() + ChatColor.YELLOW);
							player.sendMessage(ChatColor.YELLOW + msg);
						} else if (q.getNpcStart() != null && plugin.getSettings().canAllowCommandsForNpcQuests() == false) {
							String msg = Lang.get(player, "mustSpeakTo");
							msg = msg.replace("<npc>", ChatColor.DARK_PURPLE + q.getNpcStart().getName() + ChatColor.YELLOW);
							player.sendMessage(ChatColor.YELLOW + msg);
						} else if (q.getBlockStart() != null) {
							String msg = Lang.get(player, "noCommandStart");
							msg = msg.replace("<quest>", ChatColor.DARK_PURPLE + q.getName() + ChatColor.YELLOW);
							player.sendMessage(ChatColor.YELLOW + msg);
						} else {
							boolean takeable = true;
							if (quester.getCompletedQuests().contains(q.getName())) {
								if (quester.getCooldownDifference(q) > 0) {
									String early = Lang.get(player, "questTooEarly");
									early = early.replace("<quest>", ChatColor.AQUA + q.getName() + ChatColor.YELLOW);
									early = early.replace("<time>", ChatColor.DARK_PURPLE + Quests.getTime(quester.getCooldownDifference(q)) + ChatColor.YELLOW);
									player.sendMessage(ChatColor.YELLOW + early);
									takeable = false;
								}
							}
							if (q.getRegion() != null) {
								boolean inRegion = false;
								Player p = quester.getPlayer();
								RegionManager rm = plugin.getDependencies().getWorldGuard().getRegionManager(p.getWorld());
								Iterator<ProtectedRegion> it = rm.getApplicableRegions(p.getLocation()).iterator();
								while (it.hasNext()) {
									ProtectedRegion pr = it.next();
									if (pr.getId().equalsIgnoreCase(q.getRegion())) {
										inRegion = true;
										break;
									}
								}
								if (inRegion == false) {
									String msg = Lang.get(player, "questInvalidLocation");
									msg = msg.replace("<quest>", ChatColor.AQUA + q.getName() + ChatColor.YELLOW);
									player.sendMessage(ChatColor.YELLOW + msg);
									takeable = false;
								}
							}
							if (takeable == true) {
								if (player instanceof Conversable) {
									if (((Player) player).isConversing() == false) {
										quester.setQuestToTake(q.getName());
										String s = ChatColor.GOLD + "- " + ChatColor.DARK_PURPLE + quester.getQuestToTake() + ChatColor.GOLD + " -\n" + "\n" + ChatColor.RESET + plugin.getQuest(quester.getQuestToTake()).getDescription() + "\n";
										for (String msg : s.split("<br>")) {
											player.sendMessage(msg);
										}
										if (!plugin.getSettings().canAskConfirmation()) {
											plugin.getQuester(player.getUniqueId()).takeQuest(plugin.getQuest(plugin.getQuester(player.getUniqueId()).getQuestToTake()), false);
										} else {
											plugin.getConversationFactory().buildConversation((Conversable) player).begin();
										}
									} else {
										player.sendMessage(ChatColor.YELLOW + Lang.get(player, "alreadyConversing"));
									}
								}
							}
						}
					} else {
						player.sendMessage(ChatColor.YELLOW + Lang.get(player, "questNotFound"));
					}
				}
			} else {
				player.sendMessage(ChatColor.RED + Lang.get(player, "NoPermission"));
			}
		} else {
			player.sendMessage(ChatColor.YELLOW + Lang.get(player, "questTakeDisabled"));
		}
	}

	private void questsList(final CommandSender cs, String[] args) {
		if (((Player) cs).hasPermission("quests.list")) {
			if (args.length == 1) {
				plugin.listQuests((Player) cs, 1);
			} else if (args.length == 2) {
				int page;
				try {
					page = Integer.parseInt(args[1]);
					if (page < 1) {
						cs.sendMessage(ChatColor.YELLOW + Lang.get("pageSelectionPosNum"));
						return;
					}
				} catch (NumberFormatException e) {
					cs.sendMessage(ChatColor.YELLOW + Lang.get("pageSelectionNum"));
					return;
				}
				plugin.listQuests((Player) cs, page);
			}
		} else {
			cs.sendMessage(ChatColor.RED + Lang.get("NoPermission"));
		}
	}

	private void questsHelp(final CommandSender cs) {
		if (((Player) cs).hasPermission("quests.quests")) {
			Player p = (Player) cs;
			printHelp(p);
		} else {
			cs.sendMessage(ChatColor.RED + Lang.get("NoPermission"));
		}
	}
	
	public void printHelp(Player player) {
		boolean translateSubCommands = plugin.getSettings().canTranslateSubCommands();
		player.sendMessage(ChatColor.GOLD + Lang.get(player, "questHelpTitle"));
		player.sendMessage(ChatColor.YELLOW + "/quests " + Lang.get(player, "questDisplayHelp"));
		if (player.hasPermission("quests.list")) {
			player.sendMessage(ChatColor.YELLOW + "/quests "+ Lang.get(player, "COMMAND_LIST_HELP")
					.replace("<command>", translateSubCommands ? Lang.get(player, "COMMAND_LIST") : "list"));
		}
		if (player.hasPermission("quests.take")) {
			player.sendMessage(ChatColor.YELLOW + "/quests " + Lang.get(player, "COMMAND_TAKE_HELP")
					.replace("<command>", translateSubCommands ? Lang.get(player, "COMMAND_TAKE") : "take"));
		}
		if (player.hasPermission("quests.quit")) {
			player.sendMessage(ChatColor.YELLOW + "/quests " + Lang.get(player, "COMMAND_QUIT_HELP")
					.replace("<command>", translateSubCommands ? Lang.get(player, "COMMAND_QUIT") : "quit"));
		}
		if (player.hasPermission("quests.journal")) {
			player.sendMessage(ChatColor.YELLOW + "/quests " + Lang.get(player, "COMMAND_JOURNAL_HELP")
					.replace("<command>", translateSubCommands ? Lang.get(player, "COMMAND_JOURNAL") : "journal"));
		}
		if (player.hasPermission("quests.editor.*") || player.hasPermission("quests.editor.editor")) {
			player.sendMessage(ChatColor.YELLOW + "/quests " + Lang.get(player, "COMMAND_EDITOR_HELP")
					.replace("<command>", translateSubCommands ? Lang.get(player, "COMMAND_EDITOR") : "editor"));
		}
		if (player.hasPermission("quests.events.*") || player.hasPermission("quests.events.editor")) {
			player.sendMessage(ChatColor.YELLOW + "/quests " + Lang.get(player, "COMMAND_EVENTS_EDITOR_HELP")
					.replace("<command>", translateSubCommands ? Lang.get(player, "COMMAND_EVENTS") : "events"));
		}
		if (player.hasPermission("quests.stats")) {
			player.sendMessage(ChatColor.YELLOW + "/quests " + Lang.get(player, "COMMAND_STATS_HELP")
					.replace("<command>", translateSubCommands ? Lang.get(player, "COMMAND_STATS") : "stats"));
		}
		if (player.hasPermission("quests.top")) {
			player.sendMessage(ChatColor.YELLOW + "/quests " + Lang.get(player, "COMMAND_TOP_HELP")
					.replace("<command>", translateSubCommands ? Lang.get(player, "COMMAND_TOP") : "top"));
		}
		if (player.hasPermission("quests.info")) {
		player.sendMessage(ChatColor.YELLOW + "/quests " + Lang.get(player, "COMMAND_INFO_HELP")
				.replace("<command>", translateSubCommands ? Lang.get(player, "COMMAND_INFO") : "info"));
		}
		player.sendMessage(" ");
		player.sendMessage(ChatColor.YELLOW + "/quest " + Lang.get(player, "COMMAND_QUEST_HELP"));
		if (player.hasPermission("quests.questinfo")) {
			player.sendMessage(ChatColor.YELLOW + "/quest " + Lang.get(player, "COMMAND_QUESTINFO_HELP"));
		}
		if (player.hasPermission("quests.admin.*") || player.hasPermission("quests.admin")) {
			player.sendMessage(ChatColor.DARK_RED + "/questadmin " + ChatColor.RED + Lang.get(player, "COMMAND_QUESTADMIN_HELP"));
		}
	}
	
	public String getQuestsCommandUsage(String cmd) {
		return ChatColor.RED + Lang.get("usage") + ": " + ChatColor.YELLOW + "/quests " + Lang.get(Lang.getCommandKey(cmd) + "_HELP");
	}
	
	private void adminHelp(final CommandSender cs) {
		if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin")) {
			printAdminHelp(cs);
		} else {
			cs.sendMessage(ChatColor.RED + Lang.get("NoPermission"));
		}
	}

	private void adminReload(final CommandSender cs) {
		if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.reload")) {
			plugin.reloadQuests();
			cs.sendMessage(ChatColor.GOLD + Lang.get("questsReloaded"));
			String msg = Lang.get("numQuestsLoaded");
			msg = msg.replace("<number>", ChatColor.DARK_PURPLE + String.valueOf(plugin.getQuests().size()) + ChatColor.GOLD);
			cs.sendMessage(ChatColor.GOLD + msg);
		} else {
			cs.sendMessage(ChatColor.RED + Lang.get("NoPermission"));
		}
	}

	private void adminToggieGUI(final CommandSender cs, String[] args) {
		if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.togglegui")) {
			try {
				int i = Integer.parseInt(args[1]);
				if (plugin.getDependencies().getCitizens().getNPCRegistry().getById(i) == null) {
					String msg = Lang.get("errorNPCID");
					msg = msg.replace("errorNPCID", ChatColor.DARK_PURPLE + "" + i + ChatColor.RED);
					cs.sendMessage(ChatColor.RED + msg);
				} else if (plugin.getQuestNpcGuis().contains(i)) {
					LinkedList<Integer> temp = plugin.getQuestNpcGuis();
					temp.remove(plugin.getQuestNpcGuis().indexOf(i));
					plugin.setQuestNpcGuis(temp);
					plugin.updateData();
					String msg = Lang.get("disableNPCGUI");
					msg = msg.replace("<npc>", ChatColor.DARK_PURPLE + plugin.getDependencies().getCitizens().getNPCRegistry().getById(i).getName() + ChatColor.YELLOW);
					cs.sendMessage(ChatColor.YELLOW + msg);
				} else {
					LinkedList<Integer> temp = plugin.getQuestNpcGuis();
					temp.add(i);
					plugin.setQuestNpcGuis(temp);
					plugin.updateData();
					String msg = Lang.get("enableNPCGUI");
					msg = msg.replace("<npc>", ChatColor.DARK_PURPLE + plugin.getDependencies().getCitizens().getNPCRegistry().getById(i).getName() + ChatColor.YELLOW);
					cs.sendMessage(ChatColor.YELLOW + msg);
				}
			} catch (NumberFormatException nfe) {
				cs.sendMessage(ChatColor.RED + Lang.get("inputNum"));
			} catch (Exception ex) {
				ex.printStackTrace();
				cs.sendMessage(ChatColor.RED + Lang.get("unknownError"));
			}
		} else {
			cs.sendMessage(ChatColor.RED + Lang.get("NoPermission"));
		}
	}

	private void adminGivePoints(final CommandSender cs, String[] args) {
		if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.givepoints")) {
			Player target = null;
			for (Player p : plugin.getServer().getOnlinePlayers()) {
				if (p.getName().equalsIgnoreCase(args[1])) {
					target = p;
					break;
				}
			}
			if (target == null) {
				cs.sendMessage(ChatColor.YELLOW + Lang.get("playerNotFound"));
			} else {
				int points;
				try {
					points = Integer.parseInt(args[2]);
					Quester quester = plugin.getQuester(target.getUniqueId());
					quester.setQuestPoints(quester.getQuestPoints() + Math.abs(points));
					String msg1 = Lang.get("giveQuestPoints");
					msg1 = msg1.replace("<player>", ChatColor.GREEN + target.getName() + ChatColor.GOLD);
					msg1 = msg1.replace("<number>", ChatColor.DARK_PURPLE + "" + points + ChatColor.GOLD);
					cs.sendMessage(ChatColor.GOLD + msg1);
					String msg2 = Lang.get(target, "questPointsGiven");
					msg2 = msg2.replace("<player>", ChatColor.GREEN + cs.getName() + ChatColor.GOLD);
					msg2 = msg2.replace("<number>", ChatColor.DARK_PURPLE + "" + points + ChatColor.GOLD);
					target.sendMessage(ChatColor.GREEN + msg2);
					quester.saveData();
				} catch (NumberFormatException e) {
					cs.sendMessage(ChatColor.YELLOW + Lang.get("inputNum"));
				}
			}
		} else {
			cs.sendMessage(ChatColor.RED + Lang.get("NoPermission"));
		}
	}

	private void adminTakePoints(final CommandSender cs, String[] args) {
		if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.takepoints")) {
			Player target = null;
			for (Player p : plugin.getServer().getOnlinePlayers()) {
				if (p.getName().equalsIgnoreCase(args[1])) {
					target = p;
					break;
				}
			}
			if (target == null) {
				cs.sendMessage(ChatColor.YELLOW + Lang.get("playerNotFound"));
			} else {
				int points;
				try {
					points = Integer.parseInt(args[2]);
				} catch (NumberFormatException e) {
					cs.sendMessage(ChatColor.YELLOW + Lang.get("inputNum"));
					return;
				}
				Quester quester = plugin.getQuester(target.getUniqueId());
				quester.setQuestPoints(quester.getQuestPoints() - Math.abs(points));
				String msg1 = Lang.get("takeQuestPoints");
				msg1 = msg1.replace("<player>", ChatColor.GREEN + target.getName() + ChatColor.GOLD);
				msg1 = msg1.replace("<number>", ChatColor.DARK_PURPLE + "" + points + ChatColor.GOLD);
				cs.sendMessage(ChatColor.GOLD + msg1);
				String msg2 = Lang.get(target, "questPointsTaken");
				msg2 = msg2.replace("<player>", ChatColor.GREEN + cs.getName() + ChatColor.GOLD);
				msg2 = msg2.replace("<number>", ChatColor.DARK_PURPLE + "" + points + ChatColor.GOLD);
				target.sendMessage(ChatColor.GREEN + msg2);
				quester.saveData();
			}
		} else {
			cs.sendMessage(ChatColor.RED + Lang.get("NoPermission"));
		}
	}

	private void adminPoints(final CommandSender cs, String[] args) {
		if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.points")) {
			Player target = null;
			for (Player p : plugin.getServer().getOnlinePlayers()) {
				if (p.getName().equalsIgnoreCase(args[1])) {
					target = p;
					break;
				}
			}
			if (target == null) {
				cs.sendMessage(ChatColor.YELLOW + Lang.get("playerNotFound"));
			} else {
				int points;
				try {
					points = Integer.parseInt(args[2]);
				} catch (NumberFormatException e) {
					cs.sendMessage(ChatColor.YELLOW + Lang.get("inputNum"));
					return;
				}
				Quester quester = plugin.getQuester(target.getUniqueId());
				quester.setQuestPoints(points);
				String msg1 = Lang.get("setQuestPoints");
				msg1 = msg1.replace("<player>", ChatColor.GREEN + target.getName() + ChatColor.GOLD);
				msg1 = msg1.replace("<number>", ChatColor.DARK_PURPLE + "" + points + ChatColor.GOLD);
				cs.sendMessage(ChatColor.GOLD + msg1);
				String msg2 = Lang.get("questPointsSet");
				msg2 = msg2.replace("<player>", ChatColor.GREEN + cs.getName() + ChatColor.GOLD);
				msg2 = msg2.replace("<number>", ChatColor.DARK_PURPLE + "" + points + ChatColor.GOLD);
				target.sendMessage(ChatColor.GREEN + msg2);
				quester.saveData();
			}
		} else {
			cs.sendMessage(ChatColor.RED + Lang.get("NoPermission"));
		}
	}

	private void adminGive(final CommandSender cs, String[] args) {
		if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.give")) {
			Player target = null;
			for (Player p : plugin.getServer().getOnlinePlayers()) {
				if (p.getName().toLowerCase().contains(args[1].toLowerCase())) {
					target = p;
					break;
				}
			}
			if (target == null) {
				cs.sendMessage(ChatColor.YELLOW + Lang.get("playerNotFound"));
			} else {
				Quest questToGive;
				String name = "";
				if (args.length == 3) {
					name = args[2].toLowerCase();
				} else {
					for (int i = 2; i < args.length; i++) {
						int lastIndex = args.length - 1;
						if (i == lastIndex) {
							name = name + args[i].toLowerCase();
						} else {
							name = name + args[i].toLowerCase() + " ";
						}
					}
				}
				questToGive = plugin.getQuest(name);
				if (questToGive == null) {
					cs.sendMessage(ChatColor.YELLOW + Lang.get("questNotFound"));
				} else {
					Quester quester = plugin.getQuester(target.getUniqueId());
					for (Quest q : quester.getCurrentQuests().keySet()) {
						if (q.getName().equalsIgnoreCase(questToGive.getName())) {
							String msg = Lang.get("questsPlayerHasQuestAlready");
							msg = msg.replace("<player>", ChatColor.ITALIC + "" + ChatColor.GREEN + target.getName() + ChatColor.RESET + ChatColor.YELLOW);
							msg = msg.replace("<quest>", ChatColor.ITALIC + "" + ChatColor.DARK_PURPLE + questToGive.getName() + ChatColor.RESET + ChatColor.YELLOW);
							cs.sendMessage(ChatColor.YELLOW + msg);
							return;
						}
					}
					quester.hardQuit(questToGive);
					String msg1 = Lang.get("questForceTake");
					msg1 = msg1.replace("<player>", ChatColor.GREEN + target.getName() + ChatColor.GOLD);
					msg1 = msg1.replace("<quest>", ChatColor.DARK_PURPLE + questToGive.getName() + ChatColor.GOLD);
					cs.sendMessage(ChatColor.GOLD + msg1);
					String msg2 = Lang.get(target, "questForcedTake");
					msg2 = msg2.replace("<player>", ChatColor.GREEN + cs.getName() + ChatColor.GOLD);
					msg2 = msg2.replace("<quest>", ChatColor.DARK_PURPLE + questToGive.getName() + ChatColor.GOLD);
					target.sendMessage(ChatColor.GREEN + msg2);
					quester.takeQuest(questToGive, true);
				}
			}
		} else {
			cs.sendMessage(ChatColor.RED + Lang.get("NoPermission"));
		}
	}

	private void adminPointsAll(final CommandSender cs, String[] args) {
		if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.points.all")) {
			final int amount;
			try {
				amount = Integer.parseInt(args[1]);
				if (amount < 0) {
					cs.sendMessage(ChatColor.RED + Lang.get("inputPosNum"));
					return;
				}
			} catch (NumberFormatException e) {
				cs.sendMessage(ChatColor.RED + Lang.get("inputNum"));
				return;
			}
			Thread thread = new Thread(new Runnable() {

				@Override
				public void run() {
					File questerFolder = new File(plugin.getDataFolder(), "data");
					if (questerFolder.exists() && questerFolder.isDirectory()) {
						FileConfiguration data = new YamlConfiguration();
						File[] files = questerFolder.listFiles();
						int failCount = 0;
						boolean suppressed = false;
						if (files != null) {
							for (File f : files) {
								try {
									data.load(f);
									data.set("quest-points", amount);
									data.save(f);
								} catch (IOException e) {
									if (failCount < 10) {
										String msg = Lang.get("errorReading");
										msg = msg.replace("<file>", ChatColor.DARK_AQUA + f.getName() + ChatColor.RED);
										cs.sendMessage(ChatColor.RED + msg);
										failCount++;
									} else if (suppressed == false) {
										String msg = Lang.get("errorReadingSuppress");
										msg = msg.replace("<file>", ChatColor.DARK_AQUA + f.getName() + ChatColor.RED);
										cs.sendMessage(ChatColor.RED + msg);
										suppressed = true;
									}
								} catch (InvalidConfigurationException e) {
									if (failCount < 10) {
										String msg = Lang.get("errorReading");
										msg = msg.replace("<file>", ChatColor.DARK_AQUA + f.getName() + ChatColor.RED);
										cs.sendMessage(ChatColor.RED + msg);
										failCount++;
									} else if (suppressed == false) {
										String msg = Lang.get("errorReadingSuppress");
										msg = msg.replace("<file>", ChatColor.DARK_AQUA + f.getName() + ChatColor.RED);
										cs.sendMessage(ChatColor.RED + msg);
										suppressed = true;
									}
								}
							}
						}
						cs.sendMessage(ChatColor.GREEN + Lang.get("done"));
						String msg = Lang.get("allQuestPointsSet");
						msg = msg.replace("<number>", ChatColor.AQUA + "" + amount + ChatColor.GOLD);
						plugin.getServer().broadcastMessage(ChatColor.YELLOW + "" + ChatColor.GOLD + msg);
					} else {
						cs.sendMessage(ChatColor.RED + Lang.get("errorDataFolder"));
					}
				}
			});
			cs.sendMessage(ChatColor.YELLOW + Lang.get("settingAllQuestPoints"));
			for (Quester q : plugin.getQuesters()) {
				q.setQuestPoints(amount);
			}
			thread.start();
		} else {
			cs.sendMessage(ChatColor.RED + Lang.get("NoPermission"));
		}
	}

	private void adminFinish(final CommandSender cs, String[] args) {
		if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.finish")) {
			Player target = null;
			for (Player p : plugin.getServer().getOnlinePlayers()) {
				if (p.getName().toLowerCase().contains(args[1].toLowerCase())) {
					target = p;
					break;
				}
			}
			if (target == null) {
				cs.sendMessage(ChatColor.YELLOW + Lang.get("playerNotFound"));
			} else {
				Quester quester = plugin.getQuester(target.getUniqueId());
				if (quester.getCurrentQuests().isEmpty()) {
					String msg = Lang.get("noCurrentQuest");
					msg = msg.replace("<player>", target.getName());
					cs.sendMessage(ChatColor.YELLOW + msg);
				} else {
					Quest quest = plugin.getQuest(MiscUtil.concatArgArray(args, 2, args.length - 1, ' '));
					if (quest == null) {
						cs.sendMessage(ChatColor.RED + Lang.get("questNotFound"));
						return;
					}
					String msg1 = Lang.get("questForceFinish");
					msg1 = msg1.replace("<player>", ChatColor.GREEN + target.getName() + ChatColor.GOLD);
					msg1 = msg1.replace("<quest>", ChatColor.DARK_PURPLE + quest.getName() + ChatColor.GOLD);
					cs.sendMessage(ChatColor.GOLD + msg1);
					String msg2 = Lang.get(target, "questForcedFinish");
					msg2 = msg2.replace("<player>", ChatColor.GREEN + cs.getName() + ChatColor.GOLD);
					msg2 = msg2.replace("<quest>", ChatColor.DARK_PURPLE + quest.getName() + ChatColor.GOLD);
					target.sendMessage(ChatColor.GREEN + msg2);
					quest.completeQuest(quester);
					quester.saveData();
				}
			}
		} else {
			cs.sendMessage(ChatColor.RED + Lang.get("NoPermission"));
		}
	}

	private void adminSetStage(final CommandSender cs, String[] args) {
		if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.setstage")) {
			Player target = null;
			for (Player p : plugin.getServer().getOnlinePlayers()) {
				// To ensure the correct player is selected
				if (p.getName().equalsIgnoreCase(args[1])) {
					target = p;
					break;
				}
			}
			if (target == null) {
				for (Player p : plugin.getServer().getOnlinePlayers()) {
					if (p.getName().toLowerCase().contains(args[1].toLowerCase())) {
						target = p;
						break;
					}
				}
			}
			int stage = -1;
			if (args.length > 3) {
				try {
					stage = Integer.parseInt(args[2]);
				} catch (NumberFormatException e) {
					cs.sendMessage(ChatColor.YELLOW + Lang.get("inputNum"));
				}
			} else {
				cs.sendMessage(ChatColor.YELLOW + Lang.get("COMMAND_QUESTADMIN_SETSTAGE_USAGE"));
				return;
			}
			if (target == null) {
				cs.sendMessage(ChatColor.YELLOW + Lang.get("playerNotFound"));
			} else {
				Quester quester = plugin.getQuester(target.getUniqueId());
				if (quester.getCurrentQuests().isEmpty()) {
					String msg = Lang.get("noCurrentQuest");
					msg = msg.replace("<player>", target.getName());
					cs.sendMessage(ChatColor.YELLOW + msg);
				} else {
					Quest quest = plugin.getQuest(MiscUtil.concatArgArray(args, 2, args.length - 1, ' '));
					if (quest == null) {
						cs.sendMessage(ChatColor.RED + Lang.get("questNotFound"));
						return;
					}
					try {
						quest.setStage(quester, stage);
					} catch (InvalidStageException e) {
						String msg = Lang.get("invalidStageNum");
						msg = msg.replace("<quest>", ChatColor.DARK_PURPLE + quest.getName() + ChatColor.RED);
						cs.sendMessage(ChatColor.RED + msg);
					}
					quester.saveData();
				}
			}
		} else {
			cs.sendMessage(ChatColor.RED + Lang.get("NoPermission"));
		}
	}

	private void adminNextStage(final CommandSender cs, String[] args) {
		if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.nextstage")) {
			Player target = null;
			for (Player p : plugin.getServer().getOnlinePlayers()) {
				if (p.getName().toLowerCase().contains(args[1].toLowerCase())) {
					target = p;
					break;
				}
			}
			if (target == null) {
				cs.sendMessage(ChatColor.YELLOW + Lang.get("playerNotFound"));
			} else {
				Quester quester = plugin.getQuester(target.getUniqueId());
				if (quester.getCurrentQuests().isEmpty()) {
					String msg = Lang.get("noCurrentQuest");
					msg = msg.replace("<player>", target.getName());
					cs.sendMessage(ChatColor.YELLOW + msg);
				} else {
					Quest quest = plugin.getQuest(MiscUtil.concatArgArray(args, 2, args.length - 1, ' '));
					if (quest == null) {
						cs.sendMessage(ChatColor.RED + Lang.get("questNotFound"));
						return;
					}
					String msg1 = Lang.get("questForceNextStage");
					msg1 = msg1.replace("<player>", ChatColor.GREEN + target.getName() + ChatColor.GOLD);
					msg1 = msg1.replace("<quest>", ChatColor.DARK_PURPLE + quest.getName() + ChatColor.GOLD);
					cs.sendMessage(ChatColor.GOLD + msg1);
					String msg2 = Lang.get(target, "questForcedNextStage");
					msg2 = msg2.replace("<player>", ChatColor.GREEN + cs.getName() + ChatColor.GOLD);
					msg2 = msg2.replace("<quest>", ChatColor.DARK_PURPLE + quest.getName() + ChatColor.GOLD);
					target.sendMessage(ChatColor.GREEN + msg2);
					quest.nextStage(quester);
					quester.saveData();
				}
			}
		} else {
			cs.sendMessage(ChatColor.RED + Lang.get("NoPermission"));
		}
	}

	private void adminQuit(final CommandSender cs, String[] args) {
		try {
			if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.quit")) {
				Player target = null;
				for (Player p : plugin.getServer().getOnlinePlayers()) {
					if (p.getName().toLowerCase().contains(args[1].toLowerCase())) {
						target = p;
						break;
					}
				}
				if (target == null) {
					cs.sendMessage(ChatColor.YELLOW + Lang.get("playerNotFound"));
				} else {
					Quester quester = plugin.getQuester(target.getUniqueId());
					if (quester.getCurrentQuests().isEmpty()) {
						String msg = Lang.get("noCurrentQuest");
						msg = msg.replace("<player>", target.getName());
						cs.sendMessage(ChatColor.YELLOW + msg);
					} else {
						Quest quest = plugin.getQuest(MiscUtil.concatArgArray(args, 2, args.length - 1, ' '));
						if (quest == null) {
							cs.sendMessage(ChatColor.RED + Lang.get("questNotFound"));
							return;
						}
						quester.hardQuit(quest);
						String msg1 = Lang.get("questForceQuit");
						msg1 = msg1.replace("<player>", ChatColor.GREEN + target.getName() + ChatColor.GOLD);
						msg1 = msg1.replace("<quest>", ChatColor.DARK_PURPLE + quest.getName() + ChatColor.GOLD);
						cs.sendMessage(ChatColor.GOLD + msg1);
						String msg2 = Lang.get(target, "questForcedQuit");
						msg2 = msg2.replace("<player>", ChatColor.GREEN + cs.getName() + ChatColor.GOLD);
						msg2 = msg2.replace("<quest>", ChatColor.DARK_PURPLE + quest.getName() + ChatColor.GOLD);
						target.sendMessage(ChatColor.GREEN + msg2);
						quester.saveData();
						quester.updateJournal();
					}
				}
			} else {
				cs.sendMessage(ChatColor.RED + Lang.get("NoPermission"));
			}
		} catch (NullPointerException npe) {
			npe.printStackTrace();
		}
	}

	private void adminReset(final CommandSender cs, String[] args) {
		if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.reset")) {
			Quester quester = plugin.getQuester(args[1]);
			if (quester == null) {
				cs.sendMessage(ChatColor.YELLOW + Lang.get("playerNotFound"));
				return;
			}
			UUID id = quester.getUUID();
			LinkedList<Quester> temp = plugin.getQuesters();
			for (Quester q : temp) {
				if (q.getUUID().equals(id)) {
					temp.remove(q);
				}
			}
			plugin.setQuesters(temp);
			try {
				quester.hardClear();
				quester.saveData();
				quester.updateJournal();
				final File dataFolder = new File(plugin.getDataFolder(), "data" + File.separator);
				final File quest = new File(dataFolder, id + ".yml");
				quest.delete();
				String msg = Lang.get("questReset");
				if (Bukkit.getOfflinePlayer(id).getName() != null) {
					msg = msg.replace("<player>", ChatColor.GREEN + Bukkit.getOfflinePlayer(id).getName() + ChatColor.GOLD);
				} else {
					msg = msg.replace("<player>", ChatColor.GREEN + args[1] + ChatColor.GOLD);
				}
				cs.sendMessage(ChatColor.GOLD + msg);
				cs.sendMessage(ChatColor.DARK_PURPLE + " UUID: " + ChatColor.DARK_AQUA + id);
			} catch (Exception e) {
				plugin.getLogger().info("Data file does not exist for " + id.toString());
			}
			quester = new Quester(plugin);
			quester.setUUID(id);
			quester.saveData();
			LinkedList<Quester> temp2 = plugin.getQuesters();
			temp.add(quester);
			plugin.setQuesters(temp2);
		} else {
			cs.sendMessage(ChatColor.RED + Lang.get("NoPermission"));
		}
	}

	private void adminStats(final CommandSender cs, String[] args) {
		if (cs.hasPermission("quests.admin.*") && cs.hasPermission("quests.admin.stats")) {
			questsStats(cs, args);
		} else {
			cs.sendMessage(ChatColor.RED + Lang.get("NoPermission"));
		}
	}

	private void adminRemove(final CommandSender cs, String[] args) {
		if (cs.hasPermission("quests.admin.*") && cs.hasPermission("quests.admin.remove")) {
			Quester quester = plugin.getQuester(args[1]);
			if (quester == null) {
				cs.sendMessage(ChatColor.YELLOW + Lang.get("playerNotFound"));
				return;
			}
			Quest toRemove = plugin.getQuest(MiscUtil.concatArgArray(args, 2, args.length - 1, ' '));
			if (toRemove == null) {
				cs.sendMessage(ChatColor.RED + Lang.get("questNotFound"));
				return;
			}
			String msg = Lang.get("questRemoved");
			if (Bukkit.getOfflinePlayer(quester.getUUID()).getName() != null) {
				msg = msg.replace("<player>", ChatColor.GREEN + Bukkit.getOfflinePlayer(quester.getUUID()).getName() + ChatColor.GOLD);
			} else {
				msg = msg.replace("<player>", ChatColor.GREEN + args[1] + ChatColor.GOLD);
			}
			msg = msg.replace("<quest>", ChatColor.DARK_PURPLE + toRemove.getName() + ChatColor.AQUA);
			cs.sendMessage(ChatColor.GOLD + msg);
			cs.sendMessage(ChatColor.DARK_PURPLE + " UUID: " + ChatColor.DARK_AQUA + quester.getUUID().toString());
			quester.hardRemove(toRemove);
			quester.saveData();
			quester.updateJournal();
		} else {
			cs.sendMessage(ChatColor.RED + Lang.get("NoPermission"));
		}
	}
	
	public void printAdminHelp(CommandSender cs) {
		cs.sendMessage(ChatColor.RED + Lang.get("questAdminHelpTitle"));
		cs.sendMessage("");
		cs.sendMessage(ChatColor.DARK_RED + "/questadmin" + ChatColor.RED + " " + Lang.get("COMMAND_QUESTADMIN_HELP"));
		boolean translateSubCommands = plugin.getSettings().canTranslateSubCommands();
		if (cs.hasPermission("quests.admin.*")) {
			cs.sendMessage(ChatColor.DARK_RED + "/questadmin " + ChatColor.RED + Lang.get("COMMAND_QUESTADMIN_STATS_HELP")
					.replace("<command>", translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_STATS") : "stats"));
			cs.sendMessage(ChatColor.DARK_RED + "/questadmin " + ChatColor.RED + Lang.get("COMMAND_QUESTADMIN_GIVE_HELP")
					.replace("<command>", translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_GIVE") : "give"));
			cs.sendMessage(ChatColor.DARK_RED + "/questadmin " + ChatColor.RED + Lang.get("COMMAND_QUESTADMIN_QUIT_HELP")
					.replace("<command>", translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_QUIT") : "quit"));
			cs.sendMessage(ChatColor.DARK_RED + "/questadmin " + ChatColor.RED + Lang.get("COMMAND_QUESTADMIN_POINTS_HELP")
					.replace("<command>", translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_POINTS") : "points"));
			cs.sendMessage(ChatColor.DARK_RED + "/questadmin " + ChatColor.RED + Lang.get("COMMAND_QUESTADMIN_TAKEPOINTS_HELP")
					.replace("<command>", translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_TAKEPOINTS") : "takepoints"));
			cs.sendMessage(ChatColor.DARK_RED + "/questadmin " + ChatColor.RED + Lang.get("COMMAND_QUESTADMIN_GIVEPOINTS_HELP")
					.replace("<command>", translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_GIVEPOINTS") : "givepoints"));
			cs.sendMessage(ChatColor.DARK_RED + "/questadmin " + ChatColor.RED + Lang.get("COMMAND_QUESTADMIN_POINTSALL_HELP")
					.replace("<command>", translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_POINTSALL") : "pointsall"));
			cs.sendMessage(ChatColor.DARK_RED + "/questadmin " + ChatColor.RED + Lang.get("COMMAND_QUESTADMIN_FINISH_HELP")
					.replace("<command>", translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_FINISH") : "finish"));
			cs.sendMessage(ChatColor.DARK_RED + "/questadmin " + ChatColor.RED + Lang.get("COMMAND_QUESTADMIN_NEXTSTAGE_HELP")
					.replace("<command>", translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_NEXTSTAGE") : "nextstage"));
			cs.sendMessage(ChatColor.DARK_RED + "/questadmin " + ChatColor.RED + Lang.get("COMMAND_QUESTADMIN_SETSTAGE_HELP")
					.replace("<command>", translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_SETSTAGE") : "setstage"));
			cs.sendMessage(ChatColor.DARK_RED + "/questadmin " + ChatColor.RED + Lang.get("COMMAND_QUESTADMIN_RESET_HELP")
					.replace("<command>", translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_RESET") : "reset"));
			cs.sendMessage(ChatColor.DARK_RED + "/questadmin " + ChatColor.RED + Lang.get("COMMAND_QUESTADMIN_REMOVE_HELP")
					.replace("<command>", translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_REMOVE") : "remove"));
			cs.sendMessage(ChatColor.DARK_RED + "/questadmin " + ChatColor.RED + Lang.get("COMMAND_QUESTADMIN_TOGGLEGUI_HELP")
					.replace("<command>", translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_TOGGLEGUI") : "togglegui"));
			cs.sendMessage(ChatColor.DARK_RED + "/questadmin " + ChatColor.RED + Lang.get("COMMAND_QUESTADMIN_RELOAD_HELP")
					.replace("<command>", translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_RELOAD") : "reload"));
		} else {
			if (cs.hasPermission("quests.admin.stats")) {
				cs.sendMessage(ChatColor.DARK_RED + "/questadmin " + ChatColor.RED + Lang.get("COMMAND_QUESTADMIN_STATS_HELP")
						.replace("<command>", translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_STATS") : "stats"));
			}
			if (cs.hasPermission("quests.admin.give")) {
				cs.sendMessage(ChatColor.DARK_RED + "/questadmin " + ChatColor.RED + Lang.get("COMMAND_QUESTADMIN_GIVE_HELP")
						.replace("<command>", translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_GIVE") : "give"));
			}
			if (cs.hasPermission("quests.admin.quit")) {
				cs.sendMessage(ChatColor.DARK_RED + "/questadmin " + ChatColor.RED + Lang.get("COMMAND_QUESTADMIN_QUIT_HELP")
						.replace("<command>", translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_QUIT") : "quit"));
			}
			if (cs.hasPermission("quests.admin.points")) {
				cs.sendMessage(ChatColor.DARK_RED + "/questadmin " + ChatColor.RED + Lang.get("COMMAND_QUESTADMIN_POINTS_HELP")
						.replace("<command>", translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_POINTS") : "points"));
			}
			if (cs.hasPermission("quests.admin.takepoints")) {
				cs.sendMessage(ChatColor.DARK_RED + "/questadmin " + ChatColor.RED + Lang.get("COMMAND_QUESTADMIN_TAKEPOINTS_HELP")
						.replace("<command>", translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_TAKEPOINTS") : "takepoints"));
			}
			if (cs.hasPermission("quests.admin.givepoints")) {
				cs.sendMessage(ChatColor.DARK_RED + "/questadmin " + ChatColor.RED + Lang.get("COMMAND_QUESTADMIN_GIVEPOINTS_HELP")
						.replace("<command>", translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_GIVEPOINTS") : "givepoints"));
			}
			if (cs.hasPermission("quests.admin.pointsall")) {
				cs.sendMessage(ChatColor.DARK_RED + "/questadmin " + ChatColor.RED + Lang.get("COMMAND_QUESTADMIN_POINTSALL_HELP")
						.replace("<command>", translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_POINTSALL") : "pointsall"));
			}
			if (cs.hasPermission("quests.admin.finish")) {
				cs.sendMessage(ChatColor.DARK_RED + "/questadmin " + ChatColor.RED + Lang.get("COMMAND_QUESTADMIN_FINISH_HELP")
						.replace("<command>", translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_FINISH") : "finish"));
			}
			if (cs.hasPermission("quests.admin.nextstage")) {
				cs.sendMessage(ChatColor.DARK_RED + "/questadmin " + ChatColor.RED + Lang.get("COMMAND_QUESTADMIN_NEXTSTAGE_HELP")
						.replace("<command>", translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_NEXTSTAGE") : "nextstage"));
			}
			if (cs.hasPermission("quests.admin.setstage")) {
				cs.sendMessage(ChatColor.DARK_RED + "/questadmin " + ChatColor.RED + Lang.get("COMMAND_QUESTADMIN_SETSTAGE_HELP")
						.replace("<command>", translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_SETSTAGE") : "setstage"));
			}
			if (cs.hasPermission("quests.admin.reset")) {
				cs.sendMessage(ChatColor.DARK_RED + "/questadmin " + ChatColor.RED + Lang.get("COMMAND_QUESTADMIN_RESET_HELP")
						.replace("<command>", translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_RESET") : "reset"));
			}
			if (cs.hasPermission("quests.admin.remove")) {
				cs.sendMessage(ChatColor.DARK_RED + "/questadmin " + ChatColor.RED + Lang.get("COMMAND_QUESTADMIN_REMOVE_HELP")
						.replace("<command>", translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_REMOVE") : "remove"));
			}
			if (cs.hasPermission("quests.admin.togglegui")) {
				cs.sendMessage(ChatColor.DARK_RED + "/questadmin " + ChatColor.RED + Lang.get("COMMAND_QUESTADMIN_TOGGLEGUI_HELP")
						.replace("<command>", translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_TOGGLEGUI") : "togglegui"));
			}
			if (cs.hasPermission("quests.admin.reload")) {
				cs.sendMessage(ChatColor.DARK_RED + "/questadmin " + ChatColor.RED + Lang.get("COMMAND_QUESTADMIN_RELOAD_HELP")
						.replace("<command>", translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_RELOAD") : "reload"));
			}
		}
	}

	public String getQuestadminCommandUsage(String cmd) {
		return ChatColor.RED + Lang.get("usage") + ": " + ChatColor.YELLOW + "/questadmin " + Lang.get(Lang.getCommandKey(cmd) + "_HELP");
	}
	
	private static Map<String, Integer> sort(Map<String, Integer> unsortedMap) {
		List<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(unsortedMap.entrySet());
		Collections.sort(list, new Comparator<Entry<String, Integer>>() {

			@Override
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				int i = o1.getValue();
				int i2 = o2.getValue();
				if (i < i2) {
					return 1;
				} else if (i == i2) {
					return 0;
				} else {
					return -1;
				}
			}
		});
		Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
		for (Entry<String, Integer> entry : list) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
}
