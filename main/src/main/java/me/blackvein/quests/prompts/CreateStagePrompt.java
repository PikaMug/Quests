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

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.FixedSetPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import me.blackvein.quests.CustomObjective;
import me.blackvein.quests.Event;
import me.blackvein.quests.QuestFactory;
import me.blackvein.quests.Quests;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.Lang;
import net.aufdemrand.denizencore.scripts.ScriptRegistry;

public class CreateStagePrompt extends FixedSetPrompt {

	private final Quests plugin;
	private final int stageNum;
	private final String pref;
	private final QuestFactory questFactory;
	private boolean hasObjective = false;

	public CreateStagePrompt(Quests plugin, int stageNum, QuestFactory qf) {
		super("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17");
		this.plugin = plugin;
		this.stageNum = stageNum;
		this.pref = "stage" + stageNum;
		this.questFactory = qf;
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public String getPromptText(ConversationContext context) {
		try {
			context.setSessionData(pref, Boolean.TRUE);
			String text = ChatColor.LIGHT_PURPLE + "- " + ChatColor.AQUA + (String) context.getSessionData(CK.Q_NAME) + ChatColor.LIGHT_PURPLE + " | " + Lang.get("stageEditorStage") + " " + ChatColor.DARK_PURPLE + stageNum + ChatColor.LIGHT_PURPLE + " -\n";
			if (context.getSessionData(pref + CK.S_BREAK_NAMES) == null && context.getSessionData(pref + CK.S_DAMAGE_NAMES) == null
					&& context.getSessionData(pref + CK.S_PLACE_NAMES) == null && context.getSessionData(pref + CK.S_USE_NAMES) == null
					&& context.getSessionData(pref + CK.S_CUT_NAMES) == null) {
				text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "1 " + ChatColor.RESET + ChatColor.DARK_PURPLE + "- " + Lang.get("stageEditorBlocks") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
			} else {
				hasObjective = true;
				text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "1 " + ChatColor.RESET + ChatColor.DARK_PURPLE + "- " + Lang.get("stageEditorBlocks") + "\n";
			}
			if (context.getSessionData(pref + CK.S_CRAFT_ITEMS) == null && context.getSessionData(pref + CK.S_ENCHANT_TYPES) == null) {
				text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "2 " + ChatColor.RESET + ChatColor.DARK_PURPLE + "- " + Lang.get("stageEditorItems") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
			} else {
				hasObjective = true;
				text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "2 " + ChatColor.RESET + ChatColor.DARK_PURPLE + "- " + Lang.get("stageEditorItems") + "\n";
			}
			if (context.getSessionData(pref + CK.S_DELIVERY_NPCS) == null && context.getSessionData(pref + CK.S_NPCS_TO_TALK_TO) == null && context.getSessionData(pref + CK.S_NPCS_TO_KILL) == null) {
				text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "3 " + ChatColor.RESET + ChatColor.DARK_PURPLE + "- " + Lang.get("stageEditorNPCs") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
			} else {
				hasObjective = true;
				text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "3 " + ChatColor.RESET + ChatColor.DARK_PURPLE + "- " + Lang.get("stageEditorNPCs") + "\n";
			}
			if (context.getSessionData(pref + CK.S_MOB_TYPES) == null && context.getSessionData(pref + CK.S_FISH) == null && context.getSessionData(pref + CK.S_TAME_TYPES) == null && context.getSessionData(pref + CK.S_SHEAR_COLORS) == null) {
				text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "4 " + ChatColor.RESET + ChatColor.DARK_PURPLE + "- " + Lang.get("stageEditorMobs") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
			} else {
				hasObjective = true;
				text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "4 " + ChatColor.RESET + ChatColor.DARK_PURPLE + "- " + Lang.get("stageEditorMobs") + "\n";
			}
			if (context.getSessionData(pref + CK.S_PLAYER_KILL) == null) {
				text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "5 " + ChatColor.RESET + ChatColor.DARK_PURPLE + "- " + Lang.get("stageEditorKillPlayers") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
			} else {
				hasObjective = true;
				Integer players = (Integer) context.getSessionData(pref + CK.S_PLAYER_KILL);
				text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "5 " + ChatColor.RESET + ChatColor.DARK_PURPLE + "- " + Lang.get("stageEditorKillPlayers") + ChatColor.GRAY + " (" + ChatColor.AQUA + players + " " + Lang.get("stageEditorPlayers") + ChatColor.GRAY + ")\n";
			}
			if (context.getSessionData(pref + CK.S_REACH_LOCATIONS) == null) {
				text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "6 " + ChatColor.RESET + ChatColor.DARK_PURPLE + "- " + Lang.get("stageEditorReachLocs") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
			} else {
				hasObjective = true;
				text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "6 " + ChatColor.RESET + ChatColor.DARK_PURPLE + "- " + Lang.get("stageEditorReachLocs") + "\n";
				LinkedList<String> locations = (LinkedList<String>) context.getSessionData(pref + CK.S_REACH_LOCATIONS);
				LinkedList<Integer> radii = (LinkedList<Integer>) context.getSessionData(pref + CK.S_REACH_LOCATIONS_RADIUS);
				LinkedList<String> names = (LinkedList<String>) context.getSessionData(pref + CK.S_REACH_LOCATIONS_NAMES);
				for (int i = 0; i < locations.size(); i++) {
					text += ChatColor.GRAY + "     - " + Lang.get("stageEditorReachRadii1") + " " + ChatColor.BLUE + radii.get(i) + ChatColor.GRAY + " " + Lang.get("stageEditorReachRadii2") + " " + ChatColor.AQUA + names.get(i) + ChatColor.GRAY + " (" + ChatColor.DARK_AQUA + locations.get(i) + ChatColor.GRAY + ")\n";
				}
			}
			if (context.getSessionData(pref + CK.S_PASSWORD_PHRASES) == null) {
				text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "7 " + ChatColor.RESET + ChatColor.DARK_PURPLE + "- " + Lang.get("stageEditorPassword") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
			} else {
				hasObjective = true;
				LinkedList<LinkedList<String>> passPhrases = (LinkedList<LinkedList<String>>) context.getSessionData(pref + CK.S_PASSWORD_PHRASES);
				LinkedList<String> passDisplays = (LinkedList<String>) context.getSessionData(pref + CK.S_PASSWORD_DISPLAYS);
				text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "7 " + ChatColor.RESET + ChatColor.DARK_PURPLE + "- " + Lang.get("stageEditorPassword") + "\n";
				for (int i = 0; i < passPhrases.size(); i++) {
					text += ChatColor.AQUA + "     - \"" + passDisplays.get(i) + "\"\n";
					LinkedList<String> phrases = passPhrases.get(i);
					for (String phrase : phrases) {
						text += ChatColor.DARK_AQUA + "      - " + phrase + "\n";
					}
				}
			}
			if (context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES) == null) {
				text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "8 " + ChatColor.RESET + ChatColor.LIGHT_PURPLE + "- " + Lang.get("stageEditorCustom") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
			} else {
				hasObjective = true;
				LinkedList<String> customObjs = (LinkedList<String>) context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES);
				text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "8 " + ChatColor.RESET + ChatColor.LIGHT_PURPLE + "- " + Lang.get("stageEditorCustom") + "\n";
				for (String s : customObjs) {
					text += ChatColor.LIGHT_PURPLE + "     - " + ChatColor.GOLD + s + "\n";
				}
			}
			if (!hasObjective) {
				text += ChatColor.GRAY + "" + ChatColor.BOLD + "9 " + ChatColor.RESET + ChatColor.GRAY + "- " + Lang.get("stageEditorEvents") + ChatColor.GRAY + " (" + Lang.get("stageEditorOptional") + ")\n";
			} else {
				text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "9 " + ChatColor.RESET + ChatColor.YELLOW + "- " + Lang.get("stageEditorEvents") + "\n";
			}
			if (!hasObjective) {
				text += ChatColor.GRAY + "" + ChatColor.BOLD + "10 " + ChatColor.RESET + ChatColor.GRAY + "- " + Lang.get("delay")  + ChatColor.GRAY + " (" + Lang.get("stageEditorOptional") + ")\n";
			} else {
				if (context.getSessionData(pref + CK.S_DELAY) == null) {
					text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "10 " + ChatColor.RESET + ChatColor.DARK_PURPLE + "- " + Lang.get("delay") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
				} else {
					long time = (Long) context.getSessionData(pref + CK.S_DELAY);
					text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "10 " + ChatColor.RESET + ChatColor.DARK_PURPLE + "- " + Lang.get("delay") + ChatColor.GRAY + " (" + ChatColor.AQUA + Quests.getTime(time) + ChatColor.GRAY + ")\n";
				}
			}
			if (context.getSessionData(pref + CK.S_DELAY) == null) {
				text += ChatColor.GRAY + "" + ChatColor.BOLD + "11 " + ChatColor.RESET + ChatColor.GRAY + "- " + Lang.get("stageEditorDelayMessage") + ChatColor.GRAY + " (" + Lang.get("noDelaySet") + ")\n";
			} else if (context.getSessionData(pref + CK.S_DELAY_MESSAGE) == null) {
				text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "11 " + ChatColor.RESET + ChatColor.DARK_PURPLE + "- " + Lang.get("stageEditorDelayMessage") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
			} else {
				text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "11 " + ChatColor.RESET + ChatColor.DARK_PURPLE + "- " + Lang.get("stageEditorDelayMessage") + ChatColor.GRAY + " (" + ChatColor.AQUA + "\"" + context.getSessionData(pref + CK.S_DELAY_MESSAGE) + "\"" + ChatColor.GRAY + ")\n";
			}
			if (plugin.getDependencies().getDenizen() == null) {
				text += ChatColor.GRAY + "" + ChatColor.BOLD + "12 " + ChatColor.RESET + ChatColor.GRAY + "- " + Lang.get("stageEditorDenizenScript") + ChatColor.GRAY + " (" + Lang.get("questDenNotInstalled") + ")\n";
			} else {
				if (context.getSessionData(pref + CK.S_DENIZEN) == null) {
					text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "12 " + ChatColor.RESET + ChatColor.DARK_PURPLE + "- " + Lang.get("stageEditorDenizenScript") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
				} else {
					hasObjective = true;
					text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "12 " + ChatColor.RESET + ChatColor.DARK_PURPLE + "- " + Lang.get("stageEditorDenizenScript") + ChatColor.GRAY + " (" + ChatColor.AQUA + context.getSessionData(pref + CK.S_DENIZEN) + ChatColor.GRAY + "\n";
				}
			}
			if (context.getSessionData(pref + CK.S_START_MESSAGE) == null) {
				if (!hasObjective) {
					text += ChatColor.GRAY + "" + ChatColor.BOLD + "13 " + ChatColor.RESET + ChatColor.GRAY + "- " + Lang.get("stageEditorStartMessage") + ChatColor.GRAY + " (" + Lang.get("stageEditorOptional") + ")\n";
				} else {
					text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "13 " + ChatColor.RESET + ChatColor.YELLOW + "- " + Lang.get("stageEditorStartMessage") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
				}
			} else {
				text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "13 " + ChatColor.RESET + ChatColor.DARK_PURPLE + "- " + Lang.get("stageEditorStartMessage") + ChatColor.GRAY + "(" + ChatColor.AQUA + "\"" + context.getSessionData(pref + CK.S_START_MESSAGE) + "\"" + ChatColor.GRAY + ")\n";
			}
			if (context.getSessionData(pref + CK.S_COMPLETE_MESSAGE) == null) {
				if (!hasObjective) {
					text += ChatColor.GRAY + "" + ChatColor.BOLD + "14 " + ChatColor.RESET + ChatColor.GRAY + "- " + Lang.get("stageEditorCompleteMessage") + ChatColor.GRAY + " (" + Lang.get("stageEditorOptional") + ")\n";
				} else {
					text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "14 " + ChatColor.RESET + ChatColor.YELLOW + "- " + Lang.get("stageEditorCompleteMessage") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
				}
			} else {
				text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "14 " + ChatColor.RESET + ChatColor.DARK_PURPLE + "- " + Lang.get("stageEditorCompleteMessage") + ChatColor.GRAY + "(" + ChatColor.AQUA + "\"" + context.getSessionData(pref + CK.S_COMPLETE_MESSAGE) + "\"" + ChatColor.GRAY + ")\n";
			}
			if (context.getSessionData(pref + CK.S_OVERRIDE_DISPLAY) == null) {
				if (!hasObjective) {
					text += ChatColor.GRAY + "" + ChatColor.BOLD + "15 " + ChatColor.RESET + ChatColor.GRAY + "- " + Lang.get("stageEditorObjectiveOverride") + ChatColor.GRAY + " (" + Lang.get("stageEditorOptional") + ")\n";
				} else {
					text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "15 " + ChatColor.RESET + ChatColor.YELLOW + "- " + Lang.get("stageEditorObjectiveOverride") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
				}
			} else {
				text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "15 " + ChatColor.RESET + ChatColor.DARK_PURPLE + "- " + Lang.get("stageEditorObjectiveOverride") + ChatColor.GRAY + "(" + ChatColor.DARK_AQUA + "\"" + context.getSessionData(pref + CK.S_OVERRIDE_DISPLAY) + "\"" + ChatColor.GRAY + ")\n";
			}
			text += ChatColor.RED + "" + ChatColor.BOLD + "16 " + ChatColor.RESET + ChatColor.DARK_PURPLE + "- " + Lang.get("stageEditorDelete") + "\n";
			text += ChatColor.GREEN + "" + ChatColor.BOLD + "17 " + ChatColor.RESET + ChatColor.DARK_PURPLE + "- " + Lang.get("done") + "\n";
			return text;
		} catch (Exception e) {
			e.printStackTrace();
			return "error";
		}
	}

	@Override
	protected Prompt acceptValidatedInput(ConversationContext context, String input) {
		if (input.equalsIgnoreCase("1")) {
			return new BlocksPrompt(plugin, stageNum, questFactory);
		} else if (input.equalsIgnoreCase("2")) {
			return new ItemsPrompt(plugin, stageNum, questFactory);
		} else if (input.equalsIgnoreCase("3")) {
			return new NPCsPrompt(plugin, stageNum, questFactory);
		} else if (input.equalsIgnoreCase("4")) {
			return new MobsPrompt(plugin, stageNum, questFactory);
		} else if (input.equalsIgnoreCase("5")) {
			return new KillPlayerPrompt();
		} else if (input.equalsIgnoreCase("6")) {
			return new ReachListPrompt();
		} else if (input.equalsIgnoreCase("7")) {
			return new PasswordListPrompt();
		} else if (input.equalsIgnoreCase("8")) {
			return new CustomObjectivesPrompt();
		} else if (input.equalsIgnoreCase("9")) {
			if (hasObjective) {
				return new EventListPrompt();
			} else {
				context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidOption"));
				return new CreateStagePrompt(plugin, stageNum, questFactory);
			}
		} else if (input.equalsIgnoreCase("10")) {
			if (hasObjective) {
				return new DelayPrompt();
			} else {
				context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidOption"));
				return new CreateStagePrompt(plugin, stageNum, questFactory);
			}
		} else if (input.equalsIgnoreCase("11")) {
			if (context.getSessionData(pref + CK.S_DELAY) == null) {
				context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNoDelaySet"));
				return new CreateStagePrompt(plugin, stageNum, questFactory);
			} else {
				return new DelayMessagePrompt();
			}
		} else if (input.equalsIgnoreCase("12")) {
			if (plugin.getDependencies().getDenizen() == null) {
				context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNoDenizen"));
				return new CreateStagePrompt(plugin, stageNum, questFactory);
			} else {
				return new DenizenPrompt();
			}
		} else if (input.equalsIgnoreCase("13")) {
			if (hasObjective) {
				return new StartMessagePrompt();
			} else {
				context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidOption"));
				return new CreateStagePrompt(plugin, stageNum, questFactory);
			}
		} else if (input.equalsIgnoreCase("14")) {
			if (hasObjective) {
				return new CompleteMessagePrompt();
			} else {
				context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidOption"));
				return new CreateStagePrompt(plugin, stageNum, questFactory);
			}
		} else if (input.equalsIgnoreCase("15")) {
			if (hasObjective) {
				return new OverrideDisplayPrompt();
			} else {
				context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidOption"));
				return new CreateStagePrompt(plugin, stageNum, questFactory);
			}
		} else if (input.equalsIgnoreCase("16")) {
			return new DeletePrompt();
		} else if (input.equalsIgnoreCase("17")) {
			return new StagesPrompt(plugin, questFactory);
		} else {
			return new CreateStagePrompt(plugin, stageNum, questFactory);
		}
	}

	private class PasswordListPrompt extends FixedSetPrompt {

		public PasswordListPrompt() {
			super("1", "2", "3", "4");
		}

		@Override
		public String getPromptText(ConversationContext context) {
			String text = ChatColor.GOLD + "- " + Lang.get("stageEditorPassword") + "-\n";
			if (context.getSessionData(pref + CK.S_PASSWORD_DISPLAYS) == null) {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("stageEditorAddPasswordDisplay") + " (" + Lang.get("noneSet") + ")\n";
				text += ChatColor.GRAY + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.BLUE + Lang.get("stageEditorAddPasswordPhrases") + " (" + Lang.get("noneSet") + ")\n";
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("clear") + "\n";
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("done");
			} else {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("stageEditorAddPasswordDisplay") + "\n";
				for (String display : getPasswordDisplays(context)) {
					text += ChatColor.GRAY + "     - " + ChatColor.AQUA + display + "\n";
				}
				if (context.getSessionData(pref + CK.S_PASSWORD_PHRASES) == null) {
					text += ChatColor.BLUE + "2 - " + Lang.get("stageEditorAddPasswordPhrases") + " (" + Lang.get("noneSet") + ")\n";
				} else {
					text += ChatColor.YELLOW + "2 - " + Lang.get("stageEditorAddPasswordPhrases") + "\n";
					for (LinkedList<String> phraseList : getPasswordPhrases(context)) {
						text += ChatColor.GRAY + "     - ";
						for (String s : phraseList) {
							if (phraseList.getLast().equals(s) == false) {
								text += ChatColor.DARK_AQUA + s + ChatColor.GRAY + "|";
							} else {
								text += ChatColor.DARK_AQUA + s + "\n";
							}
						}
					}
				}
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("clear") + "\n";
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("done");
			}
			return text;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase("1")) {
				return new PasswordDisplayPrompt();
			} else if (input.equalsIgnoreCase("2")) {
				if (context.getSessionData(pref + CK.S_PASSWORD_DISPLAYS) == null) {
					context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorMustSetPasswordDisplays"));
					return new PasswordListPrompt();
				} else {
					return new PasswordPhrasePrompt();
				}
			} else if (input.equalsIgnoreCase("3")) {
				context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorAddPasswordCleared"));
				context.setSessionData(pref + CK.S_PASSWORD_DISPLAYS, null);
				context.setSessionData(pref + CK.S_PASSWORD_PHRASES, null);
				return new PasswordListPrompt();
			} else if (input.equalsIgnoreCase("4")) {
				int one;
				int two;
				if (context.getSessionData(pref + CK.S_PASSWORD_DISPLAYS) != null) {
					one = ((List<String>) context.getSessionData(pref + CK.S_PASSWORD_DISPLAYS)).size();
				} else {
					one = 0;
				}
				if (context.getSessionData(pref + CK.S_PASSWORD_PHRASES) != null) {
					two = ((LinkedList<LinkedList<String>>) context.getSessionData(pref + CK.S_PASSWORD_PHRASES)).size();
				} else {
					two = 0;
				}
				if (one == two) {
					return new CreateStagePrompt(plugin, stageNum, questFactory);
				} else {
					context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorPasswordNotSameSize"));
					return new PasswordListPrompt();
				}
			}
			return null;
		}

		@SuppressWarnings("unchecked")
		private List<String> getPasswordDisplays(ConversationContext context) {
			return (List<String>) context.getSessionData(pref + CK.S_PASSWORD_DISPLAYS);
		}

		@SuppressWarnings("unchecked")
		private LinkedList<LinkedList<String>> getPasswordPhrases(ConversationContext context) {
			return (LinkedList<LinkedList<String>>) context.getSessionData(pref + CK.S_PASSWORD_PHRASES);
		}
	}

	private class PasswordDisplayPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			String text = ChatColor.YELLOW + Lang.get("stageEditorPasswordDisplayPrompt") + "\n";
			text += ChatColor.ITALIC + "" + ChatColor.GOLD + Lang.get("stageEditorPasswordDisplayHint");
			return text;
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
				if (context.getSessionData(pref + CK.S_PASSWORD_DISPLAYS) != null) {
					@SuppressWarnings("unchecked")
					List<String> displays = (List<String>) context.getSessionData(pref + CK.S_PASSWORD_DISPLAYS);
					displays.add(input);
					context.setSessionData(pref + CK.S_PASSWORD_DISPLAYS, displays);
				} else {
					List<String> displays = new LinkedList<String>();
					displays.add(input);
					context.setSessionData(pref + CK.S_PASSWORD_DISPLAYS, displays);
				}
			}
			return new PasswordListPrompt();
		}
	}

	private class PasswordPhrasePrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			String text = ChatColor.YELLOW + Lang.get("stageEditorPasswordPhrasePrompt") + "\n";
			text += ChatColor.ITALIC + "" + ChatColor.GOLD + Lang.get("stageEditorPasswordPhraseHint");
			return text;
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
				if (context.getSessionData(pref + CK.S_PASSWORD_PHRASES) != null) {
					@SuppressWarnings("unchecked")
					LinkedList<LinkedList<String>> phrases = (LinkedList<LinkedList<String>>) context.getSessionData(pref + CK.S_PASSWORD_PHRASES);
					LinkedList<String> newPhrases = new LinkedList<String>();
					newPhrases.addAll(Arrays.asList(input.split(Lang.get("charSemi"))));
					phrases.add(newPhrases);
					context.setSessionData(pref + CK.S_PASSWORD_PHRASES, phrases);
				} else {
					LinkedList<LinkedList<String>> phrases = new LinkedList<LinkedList<String>>();
					LinkedList<String> newPhrases = new LinkedList<String>();
					newPhrases.addAll(Arrays.asList(input.split(Lang.get("charSemi"))));
					phrases.add(newPhrases);
					context.setSessionData(pref + CK.S_PASSWORD_PHRASES, phrases);
				}
			}
			return new PasswordListPrompt();
		}
	}

	private class OverrideDisplayPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			String text = ChatColor.YELLOW + Lang.get("stageEditorObjectiveOverridePrompt") + "\n";
			text += ChatColor.ITALIC + "" + ChatColor.GOLD + Lang.get("stageEditorObjectiveOverrideHint");
			return text;
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase(Lang.get("cmdClear")) == false && input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
				context.setSessionData(pref + CK.S_OVERRIDE_DISPLAY, input);
			} else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
				context.setSessionData(pref + CK.S_OVERRIDE_DISPLAY, null);
				context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorObjectiveOverrideCleared"));
			}
			return new CreateStagePrompt(plugin, stageNum, questFactory);
		}
	}

	private class KillPlayerPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			return ChatColor.YELLOW + Lang.get("stageEditorKillPlayerPrompt");
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
				try {
					int i = Integer.parseInt(input);
					if (i < 0) {
						context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorPositiveAmount"));
						return new KillPlayerPrompt();
					} else if (i > 0) {
						context.setSessionData(pref + CK.S_PLAYER_KILL, i);
					}
				} catch (NumberFormatException e) {
					context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + input + " " + ChatColor.RED 
							+ Lang.get("stageEditorInvalidNumber"));
					return new KillPlayerPrompt();
				}
			} else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
				context.setSessionData(pref + CK.S_PLAYER_KILL, null);
			}
			return new CreateStagePrompt(plugin, stageNum, questFactory);
		}
	}

	private class ReachListPrompt extends FixedSetPrompt {

		public ReachListPrompt() {
			super("1", "2", "3", "4", "5");
		}

		@Override
		public String getPromptText(ConversationContext context) {
			String text = ChatColor.GOLD + "- " + Lang.get("stageEditorReachLocs") + " -\n";
			if (context.getSessionData(pref + CK.S_REACH_LOCATIONS) == null) {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("stageEditorSetLocations") + " (" + Lang.get("noneSet") + ")\n";
				text += ChatColor.GRAY + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.GRAY + " - " + Lang.get("stageEditorSetLocationRadii") + " (" + Lang.get("noneSet") + ")\n";
				text += ChatColor.GRAY + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.GRAY + " - " + Lang.get("stageEditorSetLocationNames") + " (" + Lang.get("noneSet") + ")\n";
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("clear") + "\n";
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "5" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("done");
			} else {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("stageEditorSetLocations") + "\n";
				for (String s : getLocations(context)) {
					text += ChatColor.GRAY + "     - " + ChatColor.DARK_AQUA + s + "\n";
				}
				if (context.getSessionData(pref + CK.S_REACH_LOCATIONS_RADIUS) == null) {
					text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.BLUE + " - " + Lang.get("stageEditorSetLocationRadii") + " (" + Lang.get("noneSet") + ")\n";
				} else {
					text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("stageEditorSetLocationRadii") + "\n";
					for (Integer i : getLocationRadii(context)) {
						text += ChatColor.GRAY + "     - " + ChatColor.AQUA + i + "\n";
					}
				}
				if (context.getSessionData(pref + CK.S_REACH_LOCATIONS_NAMES) == null) {
					text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.BLUE + " - " + Lang.get("stageEditorSetLocationNames") + " (" + Lang.get("noneSet") + ")\n";
				} else {
					text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("stageEditorSetLocationNames") + "\n";
					for (String s : getLocationNames(context)) {
						text += ChatColor.GRAY + "     - " + ChatColor.AQUA + s + "\n";
					}
				}
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("clear") + "\n";
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "5" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("done");
			}
			return text;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase("1")) {
				Map<UUID, Block> temp = questFactory.getSelectedReachLocations();
				temp.put(((Player) context.getForWhom()).getUniqueId(), null);
				questFactory.setSelectedReachLocations(temp);
				return new ReachLocationPrompt();
			} else if (input.equalsIgnoreCase("2")) {
				if (context.getSessionData(pref + CK.S_REACH_LOCATIONS) == null) {
					context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNoLocations"));
					return new ReachListPrompt();
				} else {
					return new ReachRadiiPrompt();
				}
			} else if (input.equalsIgnoreCase("3")) {
				if (context.getSessionData(pref + CK.S_REACH_LOCATIONS) == null) {
					context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNoLocations"));
					return new ReachListPrompt();
				} else {
					return new ReachNamesPrompt();
				}
			} else if (input.equalsIgnoreCase("4")) {
				context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorReachLocationsCleared"));
				context.setSessionData(pref + CK.S_REACH_LOCATIONS, null);
				context.setSessionData(pref + CK.S_REACH_LOCATIONS_RADIUS, null);
				context.setSessionData(pref + CK.S_REACH_LOCATIONS_NAMES, null);
				return new ReachListPrompt();
			} else if (input.equalsIgnoreCase("5")) {
				int one;
				int two;
				int three;
				if (context.getSessionData(pref + CK.S_REACH_LOCATIONS) != null) {
					one = ((List<String>) context.getSessionData(pref + CK.S_REACH_LOCATIONS)).size();
				} else {
					one = 0;
				}
				if (context.getSessionData(pref + CK.S_REACH_LOCATIONS_RADIUS) != null) {
					two = ((List<Integer>) context.getSessionData(pref + CK.S_REACH_LOCATIONS_RADIUS)).size();
				} else {
					two = 0;
				}
				if (context.getSessionData(pref + CK.S_REACH_LOCATIONS_NAMES) != null) {
					three = ((List<String>) context.getSessionData(pref + CK.S_REACH_LOCATIONS_NAMES)).size();
				} else {
					three = 0;
				}
				if (one == two && two == three) {
					return new CreateStagePrompt(plugin, stageNum, questFactory);
				} else {
					context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("allListsNotSameSize"));
					return new ReachListPrompt();
				}
			} else {
				return new ReachListPrompt();
			}
		}

		@SuppressWarnings("unchecked")
		private List<String> getLocations(ConversationContext context) {
			return (List<String>) context.getSessionData(pref + CK.S_REACH_LOCATIONS);
		}

		@SuppressWarnings("unchecked")
		private List<Integer> getLocationRadii(ConversationContext context) {
			return (List<Integer>) context.getSessionData(pref + CK.S_REACH_LOCATIONS_RADIUS);
		}

		@SuppressWarnings("unchecked")
		private List<String> getLocationNames(ConversationContext context) {
			return (List<String>) context.getSessionData(pref + CK.S_REACH_LOCATIONS_NAMES);
		}
	}

	private class ReachLocationPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			return ChatColor.YELLOW + Lang.get("stageEditorReachLocationPrompt");
		}

		@SuppressWarnings("unchecked")
		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			Player player = (Player) context.getForWhom();
			if (input.equalsIgnoreCase(Lang.get("cmdAdd"))) {
				Block block = questFactory.getSelectedReachLocations().get(player.getUniqueId());
				if (block != null) {
					Location loc = block.getLocation();
					LinkedList<String> locs;
					if (context.getSessionData(pref + CK.S_REACH_LOCATIONS) != null) {
						locs = (LinkedList<String>) context.getSessionData(pref + CK.S_REACH_LOCATIONS);
					} else {
						locs = new LinkedList<String>();
					}
					locs.add(Quests.getLocationInfo(loc));
					context.setSessionData(pref + CK.S_REACH_LOCATIONS, locs);
					Map<UUID, Block> temp = questFactory.getSelectedReachLocations();
					temp.remove(player.getUniqueId());
					questFactory.setSelectedReachLocations(temp);
				} else {
					player.sendMessage(ChatColor.RED + Lang.get("stageEditorNoBlockSelected"));
					return new ReachLocationPrompt();
				}
				return new ReachListPrompt();
			} else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
				Map<UUID, Block> temp = questFactory.getSelectedReachLocations();
				temp.remove(player.getUniqueId());
				questFactory.setSelectedReachLocations(temp);
				return new ReachListPrompt();
			} else {
				return new ReachLocationPrompt();
			}
		}
	}

	private class ReachRadiiPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			return ChatColor.YELLOW + Lang.get("stageEditorReachLocationRadiiPrompt");
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			Player player = (Player) context.getForWhom();
			if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
				LinkedList<Integer> radii = new LinkedList<Integer>();
				for (String s : input.split(" ")) {
					try {
						int i = Integer.parseInt(s);
						if (i < 1) {
							context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidMinimum").replace("<number>", "1"));
							return new ReachRadiiPrompt();
						}
						radii.add(i);
					} catch (NumberFormatException e) {
						player.sendMessage(ChatColor.LIGHT_PURPLE + input + " " + ChatColor.RED + Lang.get("stageEditorInvalidNumber"));
						return new ReachRadiiPrompt();
					}
				}
				context.setSessionData(pref + CK.S_REACH_LOCATIONS_RADIUS, radii);
			}
			return new ReachListPrompt();
		}
	}

	private class ReachNamesPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			return ChatColor.YELLOW + Lang.get("stageEditorReachLocationNamesPrompt");
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
				LinkedList<String> locNames = new LinkedList<String>();
				locNames.addAll(Arrays.asList(input.split(Lang.get("charSemi"))));
				context.setSessionData(pref + CK.S_REACH_LOCATIONS_NAMES, locNames);
			}
			return new ReachListPrompt();
		}
	}

	private class EventListPrompt extends FixedSetPrompt {

		public EventListPrompt() {
			super("1", "2", "3", "4", "5", "6", "7");
		}

		@SuppressWarnings("unchecked")
		@Override
		public String getPromptText(ConversationContext context) {
			String text = ChatColor.GREEN + "- " + Lang.get("stageEditorStageEvents") + " -\n";
			if (context.getSessionData(pref + CK.S_START_EVENT) == null) {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("stageEditorStartEvent") + " (" + Lang.get("noneSet") + ")\n";
			} else {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("stageEditorStartEvent") + " (" + ChatColor.AQUA + ((String) context.getSessionData(pref + CK.S_START_EVENT)) + ChatColor.YELLOW + ")\n";
			}
			if (context.getSessionData(pref + CK.S_FINISH_EVENT) == null) {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("stageEditorFinishEvent") + " (" + Lang.get("noneSet") + ")\n";
			} else {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("stageEditorFinishEvent") + " (" + ChatColor.AQUA + ((String) context.getSessionData(pref + CK.S_FINISH_EVENT)) + ChatColor.YELLOW + ")\n";
			}
			if (context.getSessionData(pref + CK.S_DEATH_EVENT) == null) {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("stageEditorDeathEvent") + " (" + Lang.get("noneSet") + ")\n";
			} else {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("stageEditorDeathEvent") + " (" + ChatColor.AQUA + ((String) context.getSessionData(pref + CK.S_DEATH_EVENT)) + ChatColor.YELLOW + ")\n";
			}
			if (context.getSessionData(pref + CK.S_DISCONNECT_EVENT) == null) {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("stageEditorDisconnectEvent") + " (" + Lang.get("noneSet") + ")\n";
			} else {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("stageEditorDisconnectEvent") + " (" + ChatColor.AQUA + ((String) context.getSessionData(pref + CK.S_DISCONNECT_EVENT)) + ChatColor.YELLOW + ")\n";
			}
			if (context.getSessionData(pref + CK.S_CHAT_EVENTS) == null) {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "5" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("stageEditorChatEvents") + " (" + Lang.get("noneSet") + ")\n";
			} else {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "5" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("stageEditorChatEvents") + "\n";
				LinkedList<String> chatEvents = (LinkedList<String>) context.getSessionData(pref + CK.S_CHAT_EVENTS);
				LinkedList<String> chatEventTriggers = (LinkedList<String>) context.getSessionData(pref + CK.S_CHAT_EVENT_TRIGGERS);
				for (String event : chatEvents) {
					text += ChatColor.AQUA + "     - " + event + ChatColor.BLUE + " (" + Lang.get("stageEditorTriggeredBy") + ": \"" + chatEventTriggers.get(chatEvents.indexOf(event)) + "\")\n";
				}
			}
			if (context.getSessionData(pref + CK.S_COMMAND_EVENTS) == null) {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "6" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("stageEditorCommandEvents") + " (" + Lang.get("noneSet") + ")\n";
			} else {
				text += ChatColor.BLUE + "" + ChatColor.BOLD + "6" + ChatColor.RESET + ChatColor.YELLOW + " - " + Lang.get("stageEditorCommandEvents") + "\n";
				LinkedList<String> commandEvents = (LinkedList<String>) context.getSessionData(pref + CK.S_COMMAND_EVENTS);
				LinkedList<String> commandEventTriggers = (LinkedList<String>) context.getSessionData(pref + CK.S_COMMAND_EVENT_TRIGGERS);
				for (String event : commandEvents) {
					text += ChatColor.AQUA + "     - " + event + ChatColor.BLUE + " (" + Lang.get("stageEditorTriggeredBy") + ": \"" + commandEventTriggers.get(commandEvents.indexOf(event)) + "\")\n";
				}
			}
			text += ChatColor.BLUE + "" + ChatColor.BOLD + "7" + ChatColor.RESET + ChatColor.BLUE + " - " + Lang.get("back");
			return text;
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase("1")) {
				return new StartEventPrompt();
			} else if (input.equalsIgnoreCase("2")) {
				return new FinishEventPrompt();
			} else if (input.equalsIgnoreCase("3")) {
				return new DeathEventPrompt();
			} else if (input.equalsIgnoreCase("4")) {
				return new DisconnectEventPrompt();
			} else if (input.equalsIgnoreCase("5")) {
				return new ChatEventPrompt();
			} else if (input.equalsIgnoreCase("6")) {
				return new CommandEventPrompt();
			} else if (input.equalsIgnoreCase("7")) {
				return new CreateStagePrompt(plugin, stageNum, questFactory);
			} else {
				return new EventListPrompt();
			}
		}
	}

	private class StartEventPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			String text = ChatColor.DARK_GREEN + "- " + Lang.get("stageEditorStartEvent") + " -\n";
			if (plugin.getEvents().isEmpty()) {
				text += ChatColor.RED + "- None";
			} else {
				for (Event e : plugin.getEvents()) {
					text += ChatColor.GREEN + "- " + e.getName() + "\n";
				}
			}
			return text + ChatColor.YELLOW + Lang.get("stageEditorEventsPrompt");
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			Player player = (Player) context.getForWhom();
			if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
				Event found = null;
				for (Event e : plugin.getEvents()) {
					if (e.getName().equalsIgnoreCase(input)) {
						found = e;
						break;
					}
				}
				if (found == null) {
					player.sendMessage(ChatColor.RED + input + ChatColor.YELLOW + " " + Lang.get("stageEditorInvalidEvent"));
					return new StartEventPrompt();
				} else {
					context.setSessionData(pref + CK.S_START_EVENT, found.getName());
					return new EventListPrompt();
				}
			} else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
				return new EventListPrompt();
			} else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
				context.setSessionData(pref + CK.S_START_EVENT, null);
				player.sendMessage(ChatColor.YELLOW + Lang.get("stageEditorStartEventCleared"));
				return new EventListPrompt();
			} else {
				return new StartEventPrompt();
			}
		}
	}

	private class FinishEventPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			String text = ChatColor.DARK_GREEN + "- " + Lang.get("stageEditorFinishEvent") + " -\n";
			if (plugin.getEvents().isEmpty()) {
				text += ChatColor.RED + "- " + Lang.get("none");
			} else {
				for (Event e : plugin.getEvents()) {
					text += ChatColor.GREEN + "- " + e.getName() + "\n";
				}
			}
			return text + ChatColor.YELLOW + Lang.get("stageEditorEventsPrompt");
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			Player player = (Player) context.getForWhom();
			if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
				Event found = null;
				for (Event e : plugin.getEvents()) {
					if (e.getName().equalsIgnoreCase(input)) {
						found = e;
						break;
					}
				}
				if (found == null) {
					player.sendMessage(ChatColor.RED + input + ChatColor.YELLOW + " " + Lang.get("stageEditorInvalidEvent"));
					return new FinishEventPrompt();
				} else {
					context.setSessionData(pref + CK.S_FINISH_EVENT, found.getName());
					return new EventListPrompt();
				}
			} else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
				return new EventListPrompt();
			} else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
				context.setSessionData(pref + CK.S_FINISH_EVENT, null);
				player.sendMessage(ChatColor.YELLOW + Lang.get("stageEditorFinishEventCleared"));
				return new EventListPrompt();
			} else {
				return new FinishEventPrompt();
			}
		}
	}

	private class DeathEventPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			String text = ChatColor.DARK_GREEN + "- " + Lang.get("stageEditorDeathEvent") + " -\n";
			if (plugin.getEvents().isEmpty()) {
				text += ChatColor.RED + "- None";
			} else {
				for (Event e : plugin.getEvents()) {
					text += ChatColor.GREEN + "- " + e.getName() + "\n";
				}
			}
			return text + ChatColor.YELLOW + Lang.get("stageEditorEventsPrompt");
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			Player player = (Player) context.getForWhom();
			if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
				Event found = null;
				for (Event e : plugin.getEvents()) {
					if (e.getName().equalsIgnoreCase(input)) {
						found = e;
						break;
					}
				}
				if (found == null) {
					player.sendMessage(ChatColor.RED + input + ChatColor.YELLOW + " " + Lang.get("stageEditorInvalidEvent"));
					return new DeathEventPrompt();
				} else {
					context.setSessionData(pref + CK.S_DEATH_EVENT, found.getName());
					return new EventListPrompt();
				}
			} else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
				return new EventListPrompt();
			} else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
				context.setSessionData(pref + CK.S_DEATH_EVENT, null);
				player.sendMessage(ChatColor.YELLOW + Lang.get("stageEditorDeathEventCleared"));
				return new EventListPrompt();
			} else {
				return new DeathEventPrompt();
			}
		}
	}

	private class DisconnectEventPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			String text = ChatColor.DARK_GREEN + "- " + Lang.get("stageEditorDisconnectEvent") + " -\n";
			if (plugin.getEvents().isEmpty()) {
				text += ChatColor.RED + "- " + Lang.get("none");
			} else {
				for (Event e : plugin.getEvents()) {
					text += ChatColor.GREEN + "- " + e.getName() + "\n";
				}
			}
			return text + ChatColor.YELLOW + Lang.get("stageEditorEventsPrompt");
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			Player player = (Player) context.getForWhom();
			if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
				Event found = null;
				for (Event e : plugin.getEvents()) {
					if (e.getName().equalsIgnoreCase(input)) {
						found = e;
						break;
					}
				}
				if (found == null) {
					player.sendMessage(ChatColor.RED + input + ChatColor.YELLOW + " " + Lang.get("stageEditorInvalidEvent"));
					return new DisconnectEventPrompt();
				} else {
					context.setSessionData(pref + CK.S_DISCONNECT_EVENT, found.getName());
					return new EventListPrompt();
				}
			} else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
				return new EventListPrompt();
			} else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
				context.setSessionData(pref + CK.S_DISCONNECT_EVENT, null);
				player.sendMessage(ChatColor.YELLOW + Lang.get("stageEditorDisconnectEventCleared"));
				return new EventListPrompt();
			} else {
				return new DisconnectEventPrompt();
			}
		}
	}

	private class ChatEventPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			String text = ChatColor.DARK_GREEN + "- " + Lang.get("stageEditorChatEvents") + " -\n";
			if (plugin.getEvents().isEmpty()) {
				text += ChatColor.RED + "- " + Lang.get("none");
			} else {
				for (Event e : plugin.getEvents()) {
					text += ChatColor.GREEN + "- " + e.getName() + "\n";
				}
			}
			return text + ChatColor.YELLOW + Lang.get("stageEditorChatEventsPrompt");
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			Player player = (Player) context.getForWhom();
			if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
				Event found = null;
				for (Event e : plugin.getEvents()) {
					if (e.getName().equalsIgnoreCase(input)) {
						found = e;
						break;
					}
				}
				if (found == null) {
					player.sendMessage(ChatColor.RED + input + ChatColor.YELLOW + " " + Lang.get("stageEditorInvalidEvent"));
					return new ChatEventPrompt();
				} else {
					context.setSessionData(pref + CK.S_CHAT_TEMP_EVENT, found.getName());
					return new ChatEventTriggerPrompt();
				}
			} else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
				return new EventListPrompt();
			} else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
				context.setSessionData(pref + CK.S_CHAT_EVENTS, null);
				context.setSessionData(pref + CK.S_CHAT_EVENT_TRIGGERS, null);
				player.sendMessage(ChatColor.YELLOW + Lang.get("stageEditorChatEventsCleared"));
				return new EventListPrompt();
			} else {
				return new ChatEventPrompt();
			}
		}
	}

	private class ChatEventTriggerPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			String tempEvent = (String) context.getSessionData(pref + CK.S_CHAT_TEMP_EVENT);
			String text = ChatColor.GOLD + "- " + Lang.get("stageEditorChatTrigger") + " -\n";
			text += Lang.get("stageEditorChatEventsTriggerPrompt").replaceAll("<event>", tempEvent);
			return text;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
				if (context.getSessionData(pref + CK.S_CHAT_EVENTS) == null) {
					LinkedList<String> chatEvents = new LinkedList<String>();
					LinkedList<String> chatEventTriggers = new LinkedList<String>();
					String event = (String) context.getSessionData(pref + CK.S_CHAT_TEMP_EVENT);
					chatEvents.add(event);
					chatEventTriggers.add(input.trim());
					context.setSessionData(pref + CK.S_CHAT_EVENTS, chatEvents);
					context.setSessionData(pref + CK.S_CHAT_EVENT_TRIGGERS, chatEventTriggers);
					return new EventListPrompt();
				} else {
					LinkedList<String> chatEvents = (LinkedList<String>) context.getSessionData(pref + CK.S_CHAT_EVENTS);
					LinkedList<String> chatEventTriggers = (LinkedList<String>) context.getSessionData(pref + CK.S_CHAT_EVENT_TRIGGERS);
					String event = (String) context.getSessionData(pref + CK.S_CHAT_TEMP_EVENT);
					chatEvents.add(event);
					chatEventTriggers.add(input.trim());
					context.setSessionData(pref + CK.S_CHAT_EVENTS, chatEvents);
					context.setSessionData(pref + CK.S_CHAT_EVENT_TRIGGERS, chatEventTriggers);
					return new EventListPrompt();
				}
			} else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
				return new EventListPrompt();
			} else {
				return new ChatEventTriggerPrompt();
			}
		}
	}
	
	private class CommandEventPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			String text = ChatColor.DARK_GREEN + "- " + Lang.get("stageEditorCommandEvents") + " -\n";
			if (plugin.getEvents().isEmpty()) {
				text += ChatColor.RED + "- " + Lang.get("none");
			} else {
				for (Event e : plugin.getEvents()) {
					text += ChatColor.GREEN + "- " + e.getName() + "\n";
				}
			}
			return text + ChatColor.YELLOW + Lang.get("stageEditorCommandEventsPrompt");
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			Player player = (Player) context.getForWhom();
			if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
				Event found = null;
				for (Event e : plugin.getEvents()) {
					if (e.getName().equalsIgnoreCase(input)) {
						found = e;
						break;
					}
				}
				if (found == null) {
					player.sendMessage(ChatColor.RED + input + ChatColor.YELLOW + " " + Lang.get("stageEditorInvalidEvent"));
					return new CommandEventPrompt();
				} else {
					context.setSessionData(pref + CK.S_COMMAND_TEMP_EVENT, found.getName());
					return new CommandEventTriggerPrompt();
				}
			} else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
				return new EventListPrompt();
			} else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
				context.setSessionData(pref + CK.S_COMMAND_EVENTS, null);
				context.setSessionData(pref + CK.S_COMMAND_EVENT_TRIGGERS, null);
				player.sendMessage(ChatColor.YELLOW + Lang.get("stageEditorCommandEventsCleared"));
				return new EventListPrompt();
			} else {
				return new CommandEventPrompt();
			}
		}
	}

	private class CommandEventTriggerPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			String tempEvent = (String) context.getSessionData(pref + CK.S_COMMAND_TEMP_EVENT);
			String text = ChatColor.GOLD + "- " + Lang.get("stageEditorCommandTrigger") + " -\n";
			text += Lang.get("stageEditorCommandEventsTriggerPrompt").replaceAll("<event>", tempEvent);
			return text;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
				if (context.getSessionData(pref + CK.S_COMMAND_EVENTS) == null) {
					LinkedList<String> commandEvents = new LinkedList<String>();
					LinkedList<String> commandEventTriggers = new LinkedList<String>();
					String event = (String) context.getSessionData(pref + CK.S_COMMAND_TEMP_EVENT);
					commandEvents.add(event);
					commandEventTriggers.add(input.trim());
					context.setSessionData(pref + CK.S_COMMAND_EVENTS, commandEvents);
					context.setSessionData(pref + CK.S_COMMAND_EVENT_TRIGGERS, commandEventTriggers);
					return new EventListPrompt();
				} else {
					LinkedList<String> commandEvents = (LinkedList<String>) context.getSessionData(pref + CK.S_COMMAND_EVENTS);
					LinkedList<String> commandEventTriggers = (LinkedList<String>) context.getSessionData(pref + CK.S_COMMAND_EVENT_TRIGGERS);
					String event = (String) context.getSessionData(pref + CK.S_COMMAND_TEMP_EVENT);
					commandEvents.add(event);
					commandEventTriggers.add(input.trim());
					context.setSessionData(pref + CK.S_COMMAND_EVENTS, commandEvents);
					context.setSessionData(pref + CK.S_COMMAND_EVENT_TRIGGERS, commandEventTriggers);
					return new EventListPrompt();
				}
			} else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
				return new EventListPrompt();
			} else {
				return new CommandEventTriggerPrompt();
			}
		}
	}

	private class DelayPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			return ChatColor.YELLOW + Lang.get("stageEditorDelayPrompt");
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			Player player = (Player) context.getForWhom();
			if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
				return new CreateStagePrompt(plugin, stageNum, questFactory);
			}
			if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
				context.setSessionData(pref + CK.S_DELAY, null);
				player.sendMessage(ChatColor.GREEN + Lang.get("stageEditorDelayCleared"));
				return new CreateStagePrompt(plugin, stageNum, questFactory);
			}
			long stageDelay;
			try {
				int i = Integer.parseInt(input);
				stageDelay = i * 1000;
			} catch (NumberFormatException e) {
				player.sendMessage(ChatColor.LIGHT_PURPLE + input + " " + ChatColor.RED + Lang.get("stageEditorInvalidNumber"));
				return new DelayPrompt();
			}
			if (stageDelay < 1000) {
				player.sendMessage(ChatColor.RED + Lang.get("invalidMinimum").replace("<number>", "1"));
				return new DelayPrompt();
			} else {
				context.setSessionData(pref + CK.S_DELAY, stageDelay);
				return new CreateStagePrompt(plugin, stageNum, questFactory);
			}
		}
	}

	private class DelayMessagePrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			return ChatColor.YELLOW + Lang.get("stageEditorDelayMessagePrompt");
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			Player player = (Player) context.getForWhom();
			if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
				context.setSessionData(pref + CK.S_DELAY_MESSAGE, input);
				return new CreateStagePrompt(plugin, stageNum, questFactory);
			} else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
				context.setSessionData(pref + CK.S_DELAY_MESSAGE, null);
				player.sendMessage(ChatColor.YELLOW + Lang.get("stageEditorDelayMessageCleared"));
				return new CreateStagePrompt(plugin, stageNum, questFactory);
			} else {
				return new DelayMessagePrompt();
			}
		}
	}

	private class DenizenPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			String text = ChatColor.DARK_AQUA + "- " + Lang.get("stageEditorDenizenScript") + " -";
			for (String s : ScriptRegistry._getScriptNames()) {
				text += ChatColor.AQUA + "- " + s + "\n";
			}
			return text + ChatColor.YELLOW + Lang.get("stageEditorScriptPrompt");
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			Player player = (Player) context.getForWhom();
			if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
				if (ScriptRegistry.containsScript(input)) {
					context.setSessionData(pref + CK.S_DENIZEN, ScriptRegistry.getScriptContainer(input).getName());
					return new CreateStagePrompt(plugin, stageNum, questFactory);
				} else {
					player.sendMessage(ChatColor.RED + Lang.get("stageEditorInvalidScript"));
					return new DenizenPrompt();
				}
			} else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
				context.setSessionData(pref + CK.S_DENIZEN, null);
				player.sendMessage(ChatColor.YELLOW + Lang.get("stageEditorDenizenCleared"));
				return new CreateStagePrompt(plugin, stageNum, questFactory);
			} else {
				return new CreateStagePrompt(plugin, stageNum, questFactory);
			}
		}
	}

	private class DeletePrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			String text = ChatColor.GREEN + "" + ChatColor.BOLD + "1" + ChatColor.RESET + "" + ChatColor.GREEN + " - " + Lang.get("yesWord") + "\n";
			text += ChatColor.GREEN + "" + ChatColor.BOLD + "2" + ChatColor.RESET + "" + ChatColor.GREEN + " - " + Lang.get("noWord");
			return ChatColor.RED + Lang.get("stageEditorConfirmStageDelete") + "\n" + ChatColor.YELLOW + Lang.get("stageEditorStage") + " " + stageNum + ": " + context.getSessionData(CK.Q_NAME) + ChatColor.RED + "\n(" + Lang.get("stageEditorConfirmStageNote") + ")\n" + text;
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			Player player = (Player) context.getForWhom();
			if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase("Yes")) {
				StagesPrompt.deleteStage(context, stageNum);
				player.sendMessage(ChatColor.YELLOW + Lang.get("stageEditorDeleteSucces"));
				return new StagesPrompt(plugin, questFactory);
			} else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase("No")) {
				return new CreateStagePrompt(plugin, stageNum, questFactory);
			} else {
				player.sendMessage(ChatColor.RED + Lang.get("invalidOption"));
				return new DeletePrompt();
			}
		}
	}

	private class StartMessagePrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			return ChatColor.YELLOW + Lang.get("stageEditorStartMessagePrompt");
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			Player player = (Player) context.getForWhom();
			if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
				context.setSessionData(pref + CK.S_START_MESSAGE, input);
				return new CreateStagePrompt(plugin, stageNum, questFactory);
			} else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
				context.setSessionData(pref + CK.S_START_MESSAGE, null);
				player.sendMessage(ChatColor.YELLOW + Lang.get("stageEditorStartMessageCleared"));
				return new CreateStagePrompt(plugin, stageNum, questFactory);
			} else {
				return new CreateStagePrompt(plugin, stageNum, questFactory);
			}
		}
	}

	private class CompleteMessagePrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			return ChatColor.YELLOW + Lang.get("stageEditorCompleteMessagePrompt");
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			Player player = (Player) context.getForWhom();
			if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
				context.setSessionData(pref + CK.S_COMPLETE_MESSAGE, input);
				return new CreateStagePrompt(plugin, stageNum, questFactory);
			} else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
				context.setSessionData(pref + CK.S_COMPLETE_MESSAGE, null);
				player.sendMessage(ChatColor.YELLOW + Lang.get("stageEditorCompleteMessageCleared"));
				return new CreateStagePrompt(plugin, stageNum, questFactory);
			} else {
				return new CreateStagePrompt(plugin, stageNum, questFactory);
			}
		}
	}

	private class CustomObjectivesPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			String text = ChatColor.LIGHT_PURPLE + "- " + Lang.get("stageEditorCustom") + " -\n";
			if (plugin.getCustomObjectives().isEmpty()) {
				text += ChatColor.BOLD + "" + ChatColor.DARK_PURPLE + "(" + Lang.get("stageEditorNoModules") + ") ";
			} else {
				for (CustomObjective co : plugin.getCustomObjectives()) {
					text += ChatColor.DARK_PURPLE + " - " + co.getName() + "\n";
				}
			}
			return text + ChatColor.YELLOW + Lang.get("stageEditorCustomPrompt");
		}

		@SuppressWarnings("unchecked")
		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
				CustomObjective found = null;
				// Check if we have a custom objective with the specified name
				for (CustomObjective co : plugin.getCustomObjectives()) {
					if (co.getName().equalsIgnoreCase(input)) {
						found = co;
						break;
					}
				}
				if (found == null) {
					// No? Check again, but with locale sensitivity
					for (CustomObjective co : plugin.getCustomObjectives()) {
						if (co.getName().toLowerCase().contains(input.toLowerCase())) {
							found = co;
							break;
						}
					}
				}
				if (found != null) {
					if (context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES) != null) {
						// The custom objective may already have been added, so let's check that
						LinkedList<String> list = (LinkedList<String>) context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES);
						LinkedList<Entry<String, Object>> datamapList = (LinkedList<Entry<String, Object>>) context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA);
						LinkedList<Integer> countList = (LinkedList<Integer>) context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES_COUNT);
						if (list.contains(found.getName()) == false) {
							// Hasn't been added yet, so let's do it
							list.add(found.getName());
							datamapList.addAll(found.getData());
							countList.add(-999);
							context.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES, list);
							context.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA, datamapList);
						} else {
							// Already added, so inform user
							context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorCustomAlreadyAdded"));
							return new CustomObjectivesPrompt();
						}
					} else {
						// The custom objective hasn't been added yet, so let's do it
						LinkedList<Entry<String, Object>> datamapList = new LinkedList<Entry<String, Object>>();
						LinkedList<Integer> countList = new LinkedList<Integer>();
						datamapList.addAll(found.getData());
						countList.add(-999);
						LinkedList<String> list = new LinkedList<String>();
						list.add(found.getName());
						context.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES, list);
						context.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA, datamapList);
						context.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES_COUNT, countList);
					}
					// Send user to the count prompt / custom data prompt if there is any needed
					if (found.canShowCount()) {
						return new CustomObjectiveCountPrompt();
					}
					if (found.getData().isEmpty() == false) {
						context.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA_DESCRIPTIONS, found.getDescriptions());
						return new ObjectiveCustomDataListPrompt();
					}
				} else {
					context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorModuleNotFound"));
					return new CustomObjectivesPrompt();
				}
			} else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
				context.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES, null);
				context.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA, null);
				context.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA_TEMP, null);
				context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorCustomCleared"));
			}
			return new CreateStagePrompt(plugin, stageNum, questFactory);
		}
	}

	private class CustomObjectiveCountPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			String text = ChatColor.BOLD + "" + ChatColor.AQUA + "- ";
			@SuppressWarnings("unchecked")
			LinkedList<String> list = (LinkedList<String>) context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES);
			String objName = list.getLast();
			text += objName + " -\n";
			CustomObjective found = null;
			for (CustomObjective co : plugin.getCustomObjectives()) {
				if (co.getName().equals(objName)) {
					found = co;
					break;
				}
			}
			if (found != null) {
				text += ChatColor.BLUE + found.getCountPrompt().toString() + "\n\n";
			}
			return text;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			try {
				int num = Integer.parseInt(input);
				LinkedList<Integer> counts = (LinkedList<Integer>) context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES_COUNT);
				counts.set(counts.size() - 1, num);
				LinkedList<String> list = (LinkedList<String>) context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES);
				String objName = list.getLast();
				CustomObjective found = null;
				for (CustomObjective co : plugin.getCustomObjectives()) {
					if (co.getName().equals(objName)) {
						found = co;
						break;
					}
				}
				if (found != null && found.getData().isEmpty() == false) {
					context.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA_DESCRIPTIONS, found.getDescriptions());
					return new ObjectiveCustomDataListPrompt();
				} else {
					return new CreateStagePrompt(plugin, stageNum, questFactory);
				}
			} catch (NumberFormatException e) {
				context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + input + " " + ChatColor.RED + Lang.get("stageEditorInvalidNumber"));
				return new CustomObjectiveCountPrompt();
			}
		}
	}

	private class ObjectiveCustomDataListPrompt extends StringPrompt {

		@SuppressWarnings("unchecked")
		@Override
		public String getPromptText(ConversationContext context) {
			String text = ChatColor.BOLD + "" + ChatColor.AQUA + "- ";
			LinkedList<String> list = (LinkedList<String>) context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES);
			LinkedList<Entry<String, Object>> datamapList = (LinkedList<Entry<String, Object>>) context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA);
			
			String objName = list.getLast();
			text += objName + " -\n";
			int index = 1;
			
			for (Entry<String, Object> datamap : datamapList) {
				text += ChatColor.BOLD + "" + ChatColor.DARK_BLUE + index + " - " + ChatColor.RESET + ChatColor.BLUE + datamap.getKey();
				if (datamap.getValue() != null) {
					text += ChatColor.GREEN + " (" + datamap.getValue().toString() + ")\n";
				} else {
					text += ChatColor.RED + " (" + Lang.get("valRequired") + ")\n";
				}
				index++;
				
			}
			
			text += ChatColor.BOLD + "" + ChatColor.DARK_BLUE + index + " - " + ChatColor.AQUA + Lang.get("finish");
			return text;
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			@SuppressWarnings("unchecked")
			LinkedList<Entry<String, Object>> datamapList = (LinkedList<Entry<String, Object>>) context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA);
			
			int numInput;
			try {
				numInput = Integer.parseInt(input);
			} catch (NumberFormatException nfe) {
				return new ObjectiveCustomDataListPrompt();
			}
			if (numInput < 1 || numInput > datamapList.size() + 1) {
				return new ObjectiveCustomDataListPrompt();
			}
			if (numInput < datamapList.size() + 1) {
				LinkedList<String> datamapKeys = new LinkedList<String>();
				for (Entry<String, Object> datamap : datamapList) {
					datamapKeys.add(datamap.getKey());
				}
//				Collections.sort(datamapKeys);
				String selectedKey = datamapKeys.get(numInput - 1);
				context.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA_TEMP, selectedKey);
				return new ObjectiveCustomDataPrompt();
			} else {
				for (Entry<String, Object> datamap : datamapList) {
					if (datamap.getValue() == null) {
						return new ObjectiveCustomDataListPrompt();
					}
				}
				context.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA_DESCRIPTIONS, null);
				return new CreateStagePrompt(plugin, stageNum, questFactory);
			}
		}
	}

	private class ObjectiveCustomDataPrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			String text = "";
			String temp = (String) context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA_TEMP);
			@SuppressWarnings("unchecked")
			Map<String, String> descriptions = (Map<String, String>) context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA_DESCRIPTIONS);
			if (descriptions.get(temp) != null) {
				text += ChatColor.GOLD + descriptions.get(temp) + "\n";
			}
			String msg = Lang.get("stageEditorCustomDataPrompt");
			msg = msg.replaceAll("<data>", ChatColor.BOLD + temp + ChatColor.RESET + ChatColor.YELLOW);
			text += ChatColor.YELLOW + msg;
			return text;
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			@SuppressWarnings("unchecked")
			LinkedList<Entry<String, Object>> datamapList = (LinkedList<Entry<String, Object>>) context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA);
			LinkedList<Entry<String, Object>> promptList = new LinkedList<Entry<String, Object>>();
			String temp = (String) context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA_TEMP);
			
			for (Entry<String, Object> datamap : datamapList) {
					if (datamap.getKey().equals(temp)) {
						promptList.add(new AbstractMap.SimpleEntry<String, Object>(datamap.getKey(), input));
					} else {
						promptList.add(new AbstractMap.SimpleEntry<String, Object>(datamap.getKey(), datamap.getValue()));
					}
				
			}
			context.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA, promptList);
			context.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA_TEMP, null);
			return new ObjectiveCustomDataListPrompt();
		}
	}
}
