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

package me.blackvein.quests.convo.quests.prompts;

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
import me.blackvein.quests.Quests;
import me.blackvein.quests.actions.Action;
import me.blackvein.quests.convo.quests.QuestsEditorNumericPrompt;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenNumericPromptEvent;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.ConfigUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;

public class StageMainPrompt extends QuestsEditorNumericPrompt {
    private final Quests plugin;
    private final int stageNum;
    private final String stagePrefix;
    private final String classPrefix;
    private boolean hasObjective = false;
    private final int size = 16;

    public StageMainPrompt(Quests plugin, int stageNum, ConversationContext context) {
        super(context);
        this.plugin = plugin;
        this.stageNum = stageNum;
        this.stagePrefix = "stage" + stageNum;
        this.classPrefix = getClass().getSimpleName();
    }
    
    public int getStageNumber() {
        return stageNum;
    }
    
    public int getSize() {
        return size;
    }
    
    public String getTitle(ConversationContext context) {
        return (String) context.getSessionData(CK.Q_NAME) + " | " + Lang.get("stageEditorStage") + " " + stageNum;
    }
    
    public ChatColor getNumberColor(ConversationContext context, int number) {
        switch (number) {
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        case 6:
        case 7:
        case 8:
            return ChatColor.BLUE;
        case 9:
            if (!hasObjective) {
                return ChatColor.GRAY;
            } else {
                return ChatColor.BLUE;
            }
        case 10:
            if (!hasObjective) {
                return ChatColor.GRAY;
            } else {
                return ChatColor.BLUE;
            }
        case 11:
            if (context.getSessionData(stagePrefix + CK.S_DELAY) == null) {
                return ChatColor.GRAY;
            } else {
                return ChatColor.BLUE;
            }
        case 12:
            if (context.getSessionData(stagePrefix + CK.S_START_MESSAGE) == null) {
                if (!hasObjective) {
                    return ChatColor.GRAY;
                } else {
                    return ChatColor.BLUE;
                }
            } else {
                return ChatColor.BLUE;
            }
        case 13:
            if (context.getSessionData(stagePrefix + CK.S_COMPLETE_MESSAGE) == null) {
                if (!hasObjective) {
                    return ChatColor.GRAY;
                } else {
                    return ChatColor.BLUE;
                }
            } else {
                return ChatColor.BLUE;
            }
        case 14:
            if (context.getSessionData(stagePrefix + CK.S_OVERRIDE_DISPLAY) == null) {
                if (!hasObjective) {
                    return ChatColor.GRAY;
                } else {
                    return ChatColor.BLUE;
                }
            } else {
                return ChatColor.BLUE;
            }
        case 15:
            return ChatColor.RED;
        case 16:
            return ChatColor.GREEN;
        default:
            return null;
        }
    }
    
    public String getSelectionText(ConversationContext context, int number) {
        switch (number) {
        case 1:
            return ChatColor.GOLD + Lang.get("stageEditorBlocks");
        case 2:
            return ChatColor.GOLD + Lang.get("stageEditorItems");
        case 3:
            return ChatColor.GOLD + Lang.get("stageEditorNPCs");
        case 4:
            return ChatColor.GOLD + Lang.get("stageEditorMobs");
        case 5:
            return ChatColor.YELLOW + Lang.get("stageEditorKillPlayers");
        case 6:
            return ChatColor.YELLOW + Lang.get("stageEditorReachLocs");
        case 7:
            return ChatColor.YELLOW + Lang.get("stageEditorPassword");
        case 8:
            return ChatColor.DARK_PURPLE + Lang.get("stageEditorCustom");
        case 9:
            if (!hasObjective) {
                return ChatColor.GRAY + Lang.get("stageEditorEvents");
            } else {
                return ChatColor.AQUA + Lang.get("stageEditorEvents");
            }
        case 10:
            if (!hasObjective) {
                return ChatColor.GRAY + Lang.get("delay");
            } else {
                return ChatColor.YELLOW + Lang.get("delay");
            }
        case 11:
            if (context.getSessionData(stagePrefix + CK.S_DELAY) == null) {
                return ChatColor.GRAY + Lang.get("stageEditorDelayMessage");
            } else {
                return ChatColor.YELLOW + Lang.get("stageEditorDelayMessage");
            }
        case 12:
            if (context.getSessionData(stagePrefix + CK.S_START_MESSAGE) == null) {
                if (!hasObjective) {
                    return ChatColor.GRAY + Lang.get("stageEditorStartMessage");
                } else {
                    return ChatColor.YELLOW + Lang.get("stageEditorStartMessage");
                }
            } else {
                return ChatColor.YELLOW + Lang.get("stageEditorStartMessage");
            }
        case 13:
            if (context.getSessionData(stagePrefix + CK.S_COMPLETE_MESSAGE) == null) {
                if (!hasObjective) {
                    return ChatColor.GRAY + Lang.get("stageEditorCompleteMessage");
                } else {
                    return ChatColor.YELLOW + Lang.get("stageEditorCompleteMessage");
                }
            } else {
                return ChatColor.YELLOW + Lang.get("stageEditorCompleteMessage");
            }
        case 14:
            if (context.getSessionData(stagePrefix + CK.S_OVERRIDE_DISPLAY) == null) {
                if (!hasObjective) {
                    return ChatColor.GRAY + Lang.get("overrideCreateAdd");
                } else {
                    return ChatColor.YELLOW + Lang.get("overrideCreateAdd");
                }
            } else {
                return ChatColor.YELLOW + Lang.get("overrideCreateAdd");
            }
        case 15:
            return ChatColor.RED + Lang.get("stageEditorDelete");
        case 16:
            return ChatColor.GREEN + Lang.get("done");
        default:
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    public String getAdditionalText(ConversationContext context, int number) {
        switch (number) {
        case 1:
            if (context.getSessionData(stagePrefix + CK.S_BREAK_NAMES) == null 
                    && context.getSessionData(stagePrefix + CK.S_DAMAGE_NAMES) == null
                    && context.getSessionData(stagePrefix + CK.S_PLACE_NAMES) == null
                    && context.getSessionData(stagePrefix + CK.S_USE_NAMES) == null
                    && context.getSessionData(stagePrefix + CK.S_CUT_NAMES) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            }
        case 2:
            if (context.getSessionData(stagePrefix + CK.S_CRAFT_ITEMS) == null
                    && context.getSessionData(stagePrefix + CK.S_SMELT_ITEMS) == null 
                    && context.getSessionData(stagePrefix + CK.S_ENCHANT_TYPES) == null 
                    && context.getSessionData(stagePrefix + CK.S_BREW_ITEMS) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            }
        case 3:
            if (context.getSessionData(stagePrefix + CK.S_DELIVERY_NPCS) == null
                    && context.getSessionData(stagePrefix + CK.S_NPCS_TO_TALK_TO) == null 
                    && context.getSessionData(stagePrefix + CK.S_NPCS_TO_KILL) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            }
        case 4:
            if (context.getSessionData(stagePrefix + CK.S_MOB_TYPES) == null 
                    && context.getSessionData(stagePrefix + CK.S_FISH) == null 
                    && context.getSessionData(stagePrefix + CK.S_TAME_TYPES) == null 
                    && context.getSessionData(stagePrefix + CK.S_SHEAR_COLORS) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            }
        case 5:
            if (context.getSessionData(stagePrefix + CK.S_PLAYER_KILL) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                Integer players = (Integer) context.getSessionData(stagePrefix + CK.S_PLAYER_KILL);
                return ChatColor.GRAY + "(" + ChatColor.AQUA + players + " " + Lang.get("stageEditorPlayers") 
                        + ChatColor.GRAY + ")";
            }
        case 6:
            if (context.getSessionData(stagePrefix + CK.S_REACH_LOCATIONS) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                String text = "\n";
                LinkedList<String> locations 
                        = (LinkedList<String>) context.getSessionData(stagePrefix + CK.S_REACH_LOCATIONS);
                LinkedList<Integer> radii 
                        = (LinkedList<Integer>) context.getSessionData(stagePrefix + CK.S_REACH_LOCATIONS_RADIUS);
                LinkedList<String> names 
                        = (LinkedList<String>) context.getSessionData(stagePrefix + CK.S_REACH_LOCATIONS_NAMES);
                for (int i = 0; i < locations.size(); i++) {
                    text += ChatColor.GRAY + "     - " + Lang.get("stageEditorReachRadii1") + " " + ChatColor.BLUE 
                            + radii.get(i) + ChatColor.GRAY + " " + Lang.get("stageEditorReachRadii2") + " " 
                            + ChatColor.AQUA + names.get(i) + ChatColor.GRAY + " (" + ChatColor.DARK_AQUA 
                            + locations.get(i) + ChatColor.GRAY + ")\n";
                }
                return text;
            }
        case 7:
            if (context.getSessionData(stagePrefix + CK.S_PASSWORD_PHRASES) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                String text = "\n";
                LinkedList<LinkedList<String>> passPhrases 
                        = (LinkedList<LinkedList<String>>) context.getSessionData(stagePrefix + CK.S_PASSWORD_PHRASES);
                LinkedList<String> passDisplays 
                        = (LinkedList<String>) context.getSessionData(stagePrefix + CK.S_PASSWORD_DISPLAYS);
                for (int i = 0; i < passPhrases.size(); i++) {
                    text += ChatColor.AQUA + "     - \"" + passDisplays.get(i) + "\"\n";
                    LinkedList<String> phrases = passPhrases.get(i);
                    for (String phrase : phrases) {
                        text += ChatColor.DARK_AQUA + "          - " + phrase + "\n";
                    }
                }
                return text;
            }
        case 8:
            if (context.getSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                String text = "\n";
                LinkedList<String> customObjs 
                        = (LinkedList<String>) context.getSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES);
                for (String s : customObjs) {
                    text += ChatColor.LIGHT_PURPLE + "     - " + ChatColor.GOLD + s + "\n";
                }
                return text;
            }
        case 9:
            if (!hasObjective) {
                return ChatColor.GRAY + "(" + Lang.get("stageEditorOptional") + ")";
            }
        case 10:
            if (!hasObjective) {
                return ChatColor.GRAY + "(" + Lang.get("stageEditorOptional") + ")";
            } else {
                if (context.getSessionData(stagePrefix + CK.S_DELAY) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    long time = (Long) context.getSessionData(stagePrefix + CK.S_DELAY);
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + MiscUtil.getTime(time) + ChatColor.GRAY + ")";
                }
            }
        case 11:
            if (context.getSessionData(stagePrefix + CK.S_DELAY) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noDelaySet") + ")";
            } else if (context.getSessionData(stagePrefix + CK.S_DELAY_MESSAGE) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + "\"" 
                        + context.getSessionData(stagePrefix + CK.S_DELAY_MESSAGE) + "\"" + ChatColor.GRAY + ")";
            }
        case 12:
            if (context.getSessionData(stagePrefix + CK.S_START_MESSAGE) == null) {
                if (!hasObjective) {
                    return ChatColor.GRAY + "(" + Lang.get("stageEditorOptional") + ")";
                } else {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                }
            } else {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + "\"" 
                        + context.getSessionData(stagePrefix + CK.S_START_MESSAGE) + "\"" + ChatColor.GRAY + ")";
            }
        case 13:
            if (context.getSessionData(stagePrefix + CK.S_COMPLETE_MESSAGE) == null) {
                if (!hasObjective) {
                    return ChatColor.GRAY + "(" + Lang.get("stageEditorOptional") + ")";
                } else {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                }
            } else {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + "\"" 
                        + context.getSessionData(stagePrefix + CK.S_COMPLETE_MESSAGE) + "\"" + ChatColor.GRAY + ")";
            }
        case 14:
            if (context.getSessionData(stagePrefix + CK.S_OVERRIDE_DISPLAY) == null) {
                if (!hasObjective) {
                    return ChatColor.GRAY + "(" + Lang.get("stageEditorOptional") + ")";
                } else {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                }
            } else {
                String text = "\n";
                LinkedList<String> overrides = new LinkedList<String>();
                overrides.addAll((List<String>) context.getSessionData(stagePrefix + CK.S_OVERRIDE_DISPLAY));
                for (int i = 0; i < overrides.size(); i++) {
                    text += ChatColor.GRAY + "     - " + ChatColor.AQUA + overrides.get(i) + "\n";
                }
                return text;
            }
        case 15:
        case 16:
            return "";
        default:
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public String getPromptText(ConversationContext context) {
        // Check/add newly made override
        if (context.getSessionData(classPrefix + "-override") != null) {
            LinkedList<String> overrides = new LinkedList<String>();
            if (context.getSessionData(stagePrefix + CK.S_OVERRIDE_DISPLAY) != null) {
                overrides.addAll((List<String>) context.getSessionData(stagePrefix + CK.S_OVERRIDE_DISPLAY));
            }
            overrides.add((String) context.getSessionData(classPrefix + "-override"));
            context.setSessionData(stagePrefix + CK.S_OVERRIDE_DISPLAY, overrides);
            context.setSessionData(classPrefix + "-override", null);
        }
        context.setSessionData(stagePrefix, Boolean.TRUE);
        checkObjective(context);
        
        QuestsEditorPostOpenNumericPromptEvent event 
                = new QuestsEditorPostOpenNumericPromptEvent(context, this);
        context.getPlugin().getServer().getPluginManager().callEvent(event);
        
        String text = ChatColor.LIGHT_PURPLE + "- " + ChatColor.AQUA 
                + getTitle(context).replaceFirst(" \\| ", ChatColor.LIGHT_PURPLE + " | ") + " -\n";
        for (int i = 1; i <= size; i++) {
            text += getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                    + getSelectionText(context, i) + " " + getAdditionalText(context, i) + "\n";
        }
        return text;
    }

    @Override
    protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
        switch (input.intValue()) {
        case 1:
            return new BlocksPrompt(plugin, stageNum, context);
        case 2:
            return new ItemsPrompt(plugin, stageNum);
        case 3:
            return new NPCsPrompt(plugin, stageNum);
        case 4:
            return new MobsPrompt(plugin, stageNum);
        case 5:
            return new KillPlayerPrompt();
        case 6:
            return new ReachListPrompt();
        case 7:
            return new PasswordListPrompt();
        case 8:
            return new CustomObjectivesPrompt();
        case 9:
            if (hasObjective) {
                return new EventListPrompt();
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidOption"));
                return new StageMainPrompt(plugin, stageNum, context);
            }
        case 10:
            if (hasObjective) {
                return new DelayPrompt();
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidOption"));
                return new StageMainPrompt(plugin, stageNum, context);
            }
        case 11:
            if (context.getSessionData(stagePrefix + CK.S_DELAY) == null) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNoDelaySet"));
                return new StageMainPrompt(plugin, stageNum, context);
            } else {
                return new DelayMessagePrompt();
            }
        case 12:
            if (hasObjective) {
                return new StartMessagePrompt();
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidOption"));
                return new StageMainPrompt(plugin, stageNum, context);
            }
        case 13:
            if (hasObjective) {
                return new CompleteMessagePrompt();
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidOption"));
                return new StageMainPrompt(plugin, stageNum, context);
            }
        case 14:
            if (hasObjective) {
                return new OverridePrompt.Builder()
                        .source(this)
                        .promptText(Lang.get("overrideCreateEnter"))
                        .build();
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidOption"));
                return new StageMainPrompt(plugin, stageNum, context);
            }
        case 15:
            return new DeletePrompt();
        case 16:
            return new StageMenuPrompt(plugin, context);
        default:
            return new StageMainPrompt(plugin, stageNum, context);
        }
    }
    
    public boolean checkObjective(ConversationContext context) {
        if (context.getSessionData(stagePrefix + CK.S_BREAK_NAMES) != null 
                || context.getSessionData(stagePrefix + CK.S_DAMAGE_NAMES) != null
                || context.getSessionData(stagePrefix + CK.S_PLACE_NAMES) != null 
                || context.getSessionData(stagePrefix + CK.S_USE_NAMES) != null
                || context.getSessionData(stagePrefix + CK.S_CUT_NAMES) != null
                || context.getSessionData(stagePrefix + CK.S_CRAFT_ITEMS) != null 
                || context.getSessionData(stagePrefix + CK.S_SMELT_ITEMS) != null 
                || context.getSessionData(stagePrefix + CK.S_ENCHANT_TYPES) != null 
                || context.getSessionData(stagePrefix + CK.S_BREW_ITEMS) != null
                || context.getSessionData(stagePrefix + CK.S_DELIVERY_NPCS) != null 
                || context.getSessionData(stagePrefix + CK.S_NPCS_TO_TALK_TO) != null 
                || context.getSessionData(stagePrefix + CK.S_NPCS_TO_KILL) != null
                || context.getSessionData(stagePrefix + CK.S_MOB_TYPES) != null 
                || context.getSessionData(stagePrefix + CK.S_FISH) != null 
                || context.getSessionData(stagePrefix + CK.S_TAME_TYPES) != null 
                || context.getSessionData(stagePrefix + CK.S_SHEAR_COLORS) != null
                || context.getSessionData(stagePrefix + CK.S_PLAYER_KILL) != null
                || context.getSessionData(stagePrefix + CK.S_REACH_LOCATIONS) != null
                || context.getSessionData(stagePrefix + CK.S_PASSWORD_PHRASES) != null
                || context.getSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES) != null) {
            hasObjective = true;
            return true;
        }
        return false;
    }

    private class PasswordListPrompt extends FixedSetPrompt {

        public PasswordListPrompt() {
            super("1", "2", "3", "4");
        }

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.GOLD + "- " + Lang.get("stageEditorPassword") + "-\n";
            if (context.getSessionData(stagePrefix + CK.S_PASSWORD_DISPLAYS) == null) {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorAddPasswordDisplay") + " (" + Lang.get("noneSet") + ")\n";
                text += ChatColor.GRAY + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.GRAY + " - " 
                        + Lang.get("stageEditorAddPasswordPhrases") + " (" + Lang.get("noneSet") + ")\n";
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("clear") + "\n";
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("done");
            } else {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorAddPasswordDisplay") + "\n";
                for (String display : getPasswordDisplays(context)) {
                    text += ChatColor.GRAY + "     - " + ChatColor.AQUA + display + "\n";
                }
                if (context.getSessionData(stagePrefix + CK.S_PASSWORD_PHRASES) == null) {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.BLUE + " - " 
                            + Lang.get("stageEditorAddPasswordPhrases") + " (" + Lang.get("noneSet") + ")\n";
                } else {
                    text += ChatColor.BLUE  + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                            + Lang.get("stageEditorAddPasswordPhrases") + "\n";
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
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("clear") + "\n";
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("done");
            }
            return text;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase("1")) {
                return new PasswordDisplayPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(stagePrefix + CK.S_PASSWORD_DISPLAYS) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorMustSetPasswordDisplays"));
                    return new PasswordListPrompt();
                } else {
                    return new PasswordPhrasePrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorObjectiveCleared"));
                context.setSessionData(stagePrefix + CK.S_PASSWORD_DISPLAYS, null);
                context.setSessionData(stagePrefix + CK.S_PASSWORD_PHRASES, null);
                return new PasswordListPrompt();
            } else if (input.equalsIgnoreCase("4")) {
                int one;
                int two;
                if (context.getSessionData(stagePrefix + CK.S_PASSWORD_DISPLAYS) != null) {
                    one = ((List<String>) context.getSessionData(stagePrefix + CK.S_PASSWORD_DISPLAYS)).size();
                } else {
                    one = 0;
                }
                if (context.getSessionData(stagePrefix + CK.S_PASSWORD_PHRASES) != null) {
                    two = ((LinkedList<LinkedList<String>>) context.getSessionData(stagePrefix + CK.S_PASSWORD_PHRASES))
                            .size();
                } else {
                    two = 0;
                }
                if (one == two) {
                    return new StageMainPrompt(plugin, stageNum, context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                    return new PasswordListPrompt();
                }
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        private List<String> getPasswordDisplays(ConversationContext context) {
            return (List<String>) context.getSessionData(stagePrefix + CK.S_PASSWORD_DISPLAYS);
        }

        @SuppressWarnings("unchecked")
        private LinkedList<LinkedList<String>> getPasswordPhrases(ConversationContext context) {
            return (LinkedList<LinkedList<String>>) context.getSessionData(stagePrefix + CK.S_PASSWORD_PHRASES);
        }
    }

    private class PasswordDisplayPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("stageEditorPasswordDisplayPrompt") + "\n";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                if (context.getSessionData(stagePrefix + CK.S_PASSWORD_DISPLAYS) != null) {
                    @SuppressWarnings("unchecked")
                    List<String> displays = (List<String>) context.getSessionData(stagePrefix + CK.S_PASSWORD_DISPLAYS);
                    displays.add(input);
                    context.setSessionData(stagePrefix + CK.S_PASSWORD_DISPLAYS, displays);
                } else {
                    List<String> displays = new LinkedList<String>();
                    displays.add(input);
                    context.setSessionData(stagePrefix + CK.S_PASSWORD_DISPLAYS, displays);
                }
            }
            return new PasswordListPrompt();
        }
    }

    private class PasswordPhrasePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("stageEditorPasswordPhrasePrompt") + "\n";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                if (context.getSessionData(stagePrefix + CK.S_PASSWORD_PHRASES) != null) {
                    @SuppressWarnings("unchecked")
                    LinkedList<LinkedList<String>> phrases 
                            = (LinkedList<LinkedList<String>>) context.getSessionData(stagePrefix + CK.S_PASSWORD_PHRASES);
                    LinkedList<String> newPhrases = new LinkedList<String>();
                    newPhrases.addAll(Arrays.asList(input.split(Lang.get("charSemi"))));
                    phrases.add(newPhrases);
                    context.setSessionData(stagePrefix + CK.S_PASSWORD_PHRASES, phrases);
                } else {
                    LinkedList<LinkedList<String>> phrases = new LinkedList<LinkedList<String>>();
                    LinkedList<String> newPhrases = new LinkedList<String>();
                    newPhrases.addAll(Arrays.asList(input.split(Lang.get("charSemi"))));
                    phrases.add(newPhrases);
                    context.setSessionData(stagePrefix + CK.S_PASSWORD_PHRASES, phrases);
                }
            }
            return new PasswordListPrompt();
        }
    }

    private class KillPlayerPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("stageEditorKillPlayerPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                try {
                    int i = Integer.parseInt(input);
                    if (i < 0) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorPositiveAmount"));
                        return new KillPlayerPrompt();
                    } else if (i > 0) {
                        context.setSessionData(stagePrefix + CK.S_PLAYER_KILL, i);
                    }
                } catch (NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqNotANumber")
                            .replace("<input>", input));
                    return new KillPlayerPrompt();
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + CK.S_PLAYER_KILL, null);
            }
            return new StageMainPrompt(plugin, stageNum, context);
        }
    }

    private class ReachListPrompt extends FixedSetPrompt {

        public ReachListPrompt() {
            super("1", "2", "3", "4", "5");
        }

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.GOLD + "- " + Lang.get("stageEditorReachLocs") + " -\n";
            if (context.getSessionData(stagePrefix + CK.S_REACH_LOCATIONS) == null) {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorSetLocations") + " (" + Lang.get("noneSet") + ")\n";
                text += ChatColor.GRAY + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.GRAY + " - " 
                        + Lang.get("stageEditorSetLocationRadii") + " (" + Lang.get("noneSet") + ")\n";
                text += ChatColor.GRAY + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.GRAY + " - " 
                        + Lang.get("stageEditorSetLocationNames") + " (" + Lang.get("noneSet") + ")\n";
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("clear") + "\n";
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "5" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("done");
            } else {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorSetLocations") + "\n";
                for (String s : getLocations(context)) {
                    text += ChatColor.GRAY + "     - " + ChatColor.DARK_AQUA + s + "\n";
                }
                if (context.getSessionData(stagePrefix + CK.S_REACH_LOCATIONS_RADIUS) == null) {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.BLUE + " - " 
                            + Lang.get("stageEditorSetLocationRadii") + " (" + Lang.get("noneSet") + ")\n";
                } else {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                            + Lang.get("stageEditorSetLocationRadii") + "\n";
                    for (Integer i : getLocationRadii(context)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + i + "\n";
                    }
                }
                if (context.getSessionData(stagePrefix + CK.S_REACH_LOCATIONS_NAMES) == null) {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.BLUE + " - " 
                            + Lang.get("stageEditorSetLocationNames") + " (" + Lang.get("noneSet") + ")\n";
                } else {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                            + Lang.get("stageEditorSetLocationNames") + "\n";
                    for (String s : getLocationNames(context)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + s + "\n";
                    }
                }
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("clear") + "\n";
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "5" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("done");
            }
            return text;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase("1")) {
                Map<UUID, Block> temp = plugin.getQuestFactory().getSelectedReachLocations();
                temp.put(((Player) context.getForWhom()).getUniqueId(), null);
                plugin.getQuestFactory().setSelectedReachLocations(temp);
                return new ReachLocationPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(stagePrefix + CK.S_REACH_LOCATIONS) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNoLocations"));
                    return new ReachListPrompt();
                } else {
                    return new ReachRadiiPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                if (context.getSessionData(stagePrefix + CK.S_REACH_LOCATIONS) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNoLocations"));
                    return new ReachListPrompt();
                } else {
                    return new ReachNamesPrompt();
                }
            } else if (input.equalsIgnoreCase("4")) {
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorObjectiveCleared"));
                context.setSessionData(stagePrefix + CK.S_REACH_LOCATIONS, null);
                context.setSessionData(stagePrefix + CK.S_REACH_LOCATIONS_RADIUS, null);
                context.setSessionData(stagePrefix + CK.S_REACH_LOCATIONS_NAMES, null);
                return new ReachListPrompt();
            } else if (input.equalsIgnoreCase("5")) {
                int one;
                int two;
                int three;
                if (context.getSessionData(stagePrefix + CK.S_REACH_LOCATIONS) != null) {
                    one = ((List<String>) context.getSessionData(stagePrefix + CK.S_REACH_LOCATIONS)).size();
                } else {
                    one = 0;
                }
                if (context.getSessionData(stagePrefix + CK.S_REACH_LOCATIONS_RADIUS) != null) {
                    two = ((List<Integer>) context.getSessionData(stagePrefix + CK.S_REACH_LOCATIONS_RADIUS)).size();
                } else {
                    two = 0;
                }
                if (context.getSessionData(stagePrefix + CK.S_REACH_LOCATIONS_NAMES) != null) {
                    three = ((List<String>) context.getSessionData(stagePrefix + CK.S_REACH_LOCATIONS_NAMES)).size();
                } else {
                    three = 0;
                }
                if (one == two && two == three) {
                    return new StageMainPrompt(plugin, stageNum, context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                    return new ReachListPrompt();
                }
            } else {
                return new ReachListPrompt();
            }
        }

        @SuppressWarnings("unchecked")
        private List<String> getLocations(ConversationContext context) {
            return (List<String>) context.getSessionData(stagePrefix + CK.S_REACH_LOCATIONS);
        }

        @SuppressWarnings("unchecked")
        private List<Integer> getLocationRadii(ConversationContext context) {
            return (List<Integer>) context.getSessionData(stagePrefix + CK.S_REACH_LOCATIONS_RADIUS);
        }

        @SuppressWarnings("unchecked")
        private List<String> getLocationNames(ConversationContext context) {
            return (List<String>) context.getSessionData(stagePrefix + CK.S_REACH_LOCATIONS_NAMES);
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
                Block block = plugin.getQuestFactory().getSelectedReachLocations().get(player.getUniqueId());
                if (block != null) {
                    Location loc = block.getLocation();
                    LinkedList<String> locs;
                    if (context.getSessionData(stagePrefix + CK.S_REACH_LOCATIONS) != null) {
                        locs = (LinkedList<String>) context.getSessionData(stagePrefix + CK.S_REACH_LOCATIONS);
                    } else {
                        locs = new LinkedList<String>();
                    }
                    locs.add(ConfigUtil.getLocationInfo(loc));
                    context.setSessionData(stagePrefix + CK.S_REACH_LOCATIONS, locs);
                    Map<UUID, Block> temp = plugin.getQuestFactory().getSelectedReachLocations();
                    temp.remove(player.getUniqueId());
                    plugin.getQuestFactory().setSelectedReachLocations(temp);
                } else {
                    player.sendMessage(ChatColor.RED + Lang.get("stageEditorNoBlockSelected"));
                    return new ReachLocationPrompt();
                }
                return new ReachListPrompt();
            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                Map<UUID, Block> temp = plugin.getQuestFactory().getSelectedReachLocations();
                temp.remove(player.getUniqueId());
                plugin.getQuestFactory().setSelectedReachLocations(temp);
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
                            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidMinimum")
                                    .replace("<number>", "1"));
                            return new ReachRadiiPrompt();
                        }
                        radii.add(i);
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + Lang.get("reqNotANumber").replace("<input>", input));
                        return new ReachRadiiPrompt();
                    }
                }
                context.setSessionData(stagePrefix + CK.S_REACH_LOCATIONS_RADIUS, radii);
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
                context.setSessionData(stagePrefix + CK.S_REACH_LOCATIONS_NAMES, locNames);
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
            String text = ChatColor.AQUA + "- " + Lang.get("stageEditorStageEvents") + " -\n";
            if (context.getSessionData(stagePrefix + CK.S_START_EVENT) == null) {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorStartEvent") + " (" + Lang.get("noneSet") + ")\n";
            } else {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorStartEvent") + " (" + ChatColor.AQUA 
                        + ((String) context.getSessionData(stagePrefix + CK.S_START_EVENT)) + ChatColor.YELLOW + ")\n";
            }
            if (context.getSessionData(stagePrefix + CK.S_FINISH_EVENT) == null) {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorFinishEvent") + " (" + Lang.get("noneSet") + ")\n";
            } else {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorFinishEvent") + " (" + ChatColor.AQUA 
                        + ((String) context.getSessionData(stagePrefix + CK.S_FINISH_EVENT)) + ChatColor.YELLOW + ")\n";
            }
            if (context.getSessionData(stagePrefix + CK.S_DEATH_EVENT) == null) {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorDeathEvent") + " (" + Lang.get("noneSet") + ")\n";
            } else {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorDeathEvent") + " (" + ChatColor.AQUA 
                        + ((String) context.getSessionData(stagePrefix + CK.S_DEATH_EVENT)) + ChatColor.YELLOW + ")\n";
            }
            if (context.getSessionData(stagePrefix + CK.S_DISCONNECT_EVENT) == null) {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorDisconnectEvent") + " (" + Lang.get("noneSet") + ")\n";
            } else {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorDisconnectEvent") + " (" + ChatColor.AQUA 
                        + ((String) context.getSessionData(stagePrefix + CK.S_DISCONNECT_EVENT)) + ChatColor.YELLOW + ")\n";
            }
            if (context.getSessionData(stagePrefix + CK.S_CHAT_EVENTS) == null) {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "5" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorChatEvents") + " (" + Lang.get("noneSet") + ")\n";
            } else {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "5" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorChatEvents") + "\n";
                LinkedList<String> chatEvents = (LinkedList<String>) context.getSessionData(stagePrefix + CK.S_CHAT_EVENTS);
                LinkedList<String> chatEventTriggers 
                        = (LinkedList<String>) context.getSessionData(stagePrefix + CK.S_CHAT_EVENT_TRIGGERS);
                for (String event : chatEvents) {
                    text += ChatColor.AQUA + "     - " + event + ChatColor.BLUE + " (" 
                            + Lang.get("stageEditorTriggeredBy") + ": \"" 
                            + chatEventTriggers.get(chatEvents.indexOf(event)) + "\")\n";
                }
            }
            if (context.getSessionData(stagePrefix + CK.S_COMMAND_EVENTS) == null) {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "6" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorCommandEvents") + " (" + Lang.get("noneSet") + ")\n";
            } else {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "6" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorCommandEvents") + "\n";
                LinkedList<String> commandEvents 
                        = (LinkedList<String>) context.getSessionData(stagePrefix + CK.S_COMMAND_EVENTS);
                LinkedList<String> commandEventTriggers 
                        = (LinkedList<String>) context.getSessionData(stagePrefix + CK.S_COMMAND_EVENT_TRIGGERS);
                for (String event : commandEvents) {
                    text += ChatColor.AQUA + "     - " + event + ChatColor.BLUE + " (" 
                            + Lang.get("stageEditorTriggeredBy") + ": \"" 
                            + commandEventTriggers.get(commandEvents.indexOf(event)) + "\")\n";
                }
            }
            text += ChatColor.GREEN + "" + ChatColor.BOLD + "7" + ChatColor.RESET + ChatColor.BLUE + " - " 
                    + Lang.get("done");
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
                return new StageMainPrompt(plugin, stageNum, context);
            } else {
                return new EventListPrompt();
            }
        }
    }

    private class StartEventPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.AQUA + "- " + Lang.get("stageEditorStartEvent") + " -\n";
            if (plugin.getActions().isEmpty()) {
                text += ChatColor.RED + "- None";
            } else {
                for (Action e : plugin.getActions()) {
                    text += ChatColor.GREEN + "- " + e.getName() + "\n";
                }
            }
            return text + ChatColor.YELLOW + Lang.get("stageEditorEventsPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                Action found = null;
                for (Action e : plugin.getActions()) {
                    if (e.getName().equalsIgnoreCase(input)) {
                        found = e;
                        break;
                    }
                }
                if (found == null) {
                    player.sendMessage(ChatColor.RED + input + ChatColor.YELLOW + " " 
                            + Lang.get("stageEditorInvalidEvent"));
                    return new StartEventPrompt();
                } else {
                    context.setSessionData(stagePrefix + CK.S_START_EVENT, found.getName());
                    return new EventListPrompt();
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                return new EventListPrompt();
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + CK.S_START_EVENT, null);
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
            String text = ChatColor.AQUA + "- " + Lang.get("stageEditorFinishEvent") + " -\n";
            if (plugin.getActions().isEmpty()) {
                text += ChatColor.RED + "- " + Lang.get("none");
            } else {
                for (Action e : plugin.getActions()) {
                    text += ChatColor.GREEN + "- " + e.getName() + "\n";
                }
            }
            return text + ChatColor.YELLOW + Lang.get("stageEditorEventsPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                Action found = null;
                for (Action e : plugin.getActions()) {
                    if (e.getName().equalsIgnoreCase(input)) {
                        found = e;
                        break;
                    }
                }
                if (found == null) {
                    player.sendMessage(ChatColor.RED + input + ChatColor.YELLOW + " " 
                            + Lang.get("stageEditorInvalidEvent"));
                    return new FinishEventPrompt();
                } else {
                    context.setSessionData(stagePrefix + CK.S_FINISH_EVENT, found.getName());
                    return new EventListPrompt();
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                return new EventListPrompt();
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + CK.S_FINISH_EVENT, null);
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
            String text = ChatColor.AQUA + "- " + Lang.get("stageEditorDeathEvent") + " -\n";
            if (plugin.getActions().isEmpty()) {
                text += ChatColor.RED + "- None";
            } else {
                for (Action e : plugin.getActions()) {
                    text += ChatColor.GREEN + "- " + e.getName() + "\n";
                }
            }
            return text + ChatColor.YELLOW + Lang.get("stageEditorEventsPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                Action found = null;
                for (Action e : plugin.getActions()) {
                    if (e.getName().equalsIgnoreCase(input)) {
                        found = e;
                        break;
                    }
                }
                if (found == null) {
                    player.sendMessage(ChatColor.RED + input + ChatColor.YELLOW + " " 
                            + Lang.get("stageEditorInvalidEvent"));
                    return new DeathEventPrompt();
                } else {
                    context.setSessionData(stagePrefix + CK.S_DEATH_EVENT, found.getName());
                    return new EventListPrompt();
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                return new EventListPrompt();
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + CK.S_DEATH_EVENT, null);
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
            String text = ChatColor.AQUA + "- " + Lang.get("stageEditorDisconnectEvent") + " -\n";
            if (plugin.getActions().isEmpty()) {
                text += ChatColor.RED + "- " + Lang.get("none");
            } else {
                for (Action e : plugin.getActions()) {
                    text += ChatColor.GREEN + "- " + e.getName() + "\n";
                }
            }
            return text + ChatColor.YELLOW + Lang.get("stageEditorEventsPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                Action found = null;
                for (Action e : plugin.getActions()) {
                    if (e.getName().equalsIgnoreCase(input)) {
                        found = e;
                        break;
                    }
                }
                if (found == null) {
                    player.sendMessage(ChatColor.RED + input + ChatColor.YELLOW + " " 
                            + Lang.get("stageEditorInvalidEvent"));
                    return new DisconnectEventPrompt();
                } else {
                    context.setSessionData(stagePrefix + CK.S_DISCONNECT_EVENT, found.getName());
                    return new EventListPrompt();
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                return new EventListPrompt();
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + CK.S_DISCONNECT_EVENT, null);
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
            String text = ChatColor.AQUA + "- " + Lang.get("stageEditorChatEvents") + " -\n";
            if (plugin.getActions().isEmpty()) {
                text += ChatColor.RED + "- " + Lang.get("none");
            } else {
                for (Action e : plugin.getActions()) {
                    text += ChatColor.GREEN + "- " + e.getName() + "\n";
                }
            }
            return text + ChatColor.YELLOW + Lang.get("stageEditorChatEventsPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                Action found = null;
                for (Action e : plugin.getActions()) {
                    if (e.getName().equalsIgnoreCase(input)) {
                        found = e;
                        break;
                    }
                }
                if (found == null) {
                    player.sendMessage(ChatColor.RED + input + ChatColor.YELLOW + " " 
                            + Lang.get("stageEditorInvalidEvent"));
                    return new ChatEventPrompt();
                } else {
                    context.setSessionData(stagePrefix + CK.S_CHAT_TEMP_EVENT, found.getName());
                    return new ChatEventTriggerPrompt();
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                return new EventListPrompt();
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + CK.S_CHAT_EVENTS, null);
                context.setSessionData(stagePrefix + CK.S_CHAT_EVENT_TRIGGERS, null);
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
            String tempEvent = (String) context.getSessionData(stagePrefix + CK.S_CHAT_TEMP_EVENT);
            String text = ChatColor.GOLD + "- " + Lang.get("stageEditorChatTrigger") + " -\n";
            text += ChatColor.YELLOW + Lang.get("stageEditorChatEventsTriggerPrompt")
                    .replace("<event>", ChatColor.AQUA + tempEvent + ChatColor.YELLOW)
                    .replace("<action>", ChatColor.AQUA + tempEvent + ChatColor.YELLOW);
            return text;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                if (context.getSessionData(stagePrefix + CK.S_CHAT_EVENTS) == null) {
                    LinkedList<String> chatEvents = new LinkedList<String>();
                    LinkedList<String> chatEventTriggers = new LinkedList<String>();
                    String event = (String) context.getSessionData(stagePrefix + CK.S_CHAT_TEMP_EVENT);
                    chatEvents.add(event);
                    chatEventTriggers.add(input.trim());
                    context.setSessionData(stagePrefix + CK.S_CHAT_EVENTS, chatEvents);
                    context.setSessionData(stagePrefix + CK.S_CHAT_EVENT_TRIGGERS, chatEventTriggers);
                    return new EventListPrompt();
                } else {
                    LinkedList<String> chatEvents 
                            = (LinkedList<String>) context.getSessionData(stagePrefix + CK.S_CHAT_EVENTS);
                    LinkedList<String> chatEventTriggers 
                            = (LinkedList<String>) context.getSessionData(stagePrefix + CK.S_CHAT_EVENT_TRIGGERS);
                    String event = (String) context.getSessionData(stagePrefix + CK.S_CHAT_TEMP_EVENT);
                    chatEvents.add(event);
                    chatEventTriggers.add(input.trim());
                    context.setSessionData(stagePrefix + CK.S_CHAT_EVENTS, chatEvents);
                    context.setSessionData(stagePrefix + CK.S_CHAT_EVENT_TRIGGERS, chatEventTriggers);
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
            String text = ChatColor.AQUA + "- " + Lang.get("stageEditorCommandEvents") + " -\n";
            if (plugin.getActions().isEmpty()) {
                text += ChatColor.RED + "- " + Lang.get("none");
            } else {
                for (Action e : plugin.getActions()) {
                    text += ChatColor.GREEN + "- " + e.getName() + "\n";
                }
            }
            return text + ChatColor.YELLOW + Lang.get("stageEditorCommandEventsPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                Action found = null;
                for (Action e : plugin.getActions()) {
                    if (e.getName().equalsIgnoreCase(input)) {
                        found = e;
                        break;
                    }
                }
                if (found == null) {
                    player.sendMessage(ChatColor.RED + input + ChatColor.YELLOW + " " 
                            + Lang.get("stageEditorInvalidEvent"));
                    return new CommandEventPrompt();
                } else {
                    context.setSessionData(stagePrefix + CK.S_COMMAND_TEMP_EVENT, found.getName());
                    return new CommandEventTriggerPrompt();
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                return new EventListPrompt();
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + CK.S_COMMAND_EVENTS, null);
                context.setSessionData(stagePrefix + CK.S_COMMAND_EVENT_TRIGGERS, null);
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
            String tempEvent = (String) context.getSessionData(stagePrefix + CK.S_COMMAND_TEMP_EVENT);
            String text = ChatColor.GOLD + "- " + Lang.get("stageEditorCommandTrigger") + " -\n";
            text += ChatColor.YELLOW + Lang.get("stageEditorCommandEventsTriggerPrompt")
                    .replace("<event>", ChatColor.AQUA + tempEvent + ChatColor.YELLOW)
                    .replace("<action>", ChatColor.AQUA + tempEvent + ChatColor.YELLOW);
            return text;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                if (context.getSessionData(stagePrefix + CK.S_COMMAND_EVENTS) == null) {
                    LinkedList<String> commandEvents = new LinkedList<String>();
                    LinkedList<String> commandEventTriggers = new LinkedList<String>();
                    String event = (String) context.getSessionData(stagePrefix + CK.S_COMMAND_TEMP_EVENT);
                    commandEvents.add(event);
                    commandEventTriggers.add(input.trim());
                    context.setSessionData(stagePrefix + CK.S_COMMAND_EVENTS, commandEvents);
                    context.setSessionData(stagePrefix + CK.S_COMMAND_EVENT_TRIGGERS, commandEventTriggers);
                    return new EventListPrompt();
                } else {
                    LinkedList<String> commandEvents 
                            = (LinkedList<String>) context.getSessionData(stagePrefix + CK.S_COMMAND_EVENTS);
                    LinkedList<String> commandEventTriggers 
                            = (LinkedList<String>) context.getSessionData(stagePrefix + CK.S_COMMAND_EVENT_TRIGGERS);
                    String event = (String) context.getSessionData(stagePrefix + CK.S_COMMAND_TEMP_EVENT);
                    commandEvents.add(event);
                    commandEventTriggers.add(input.trim());
                    context.setSessionData(stagePrefix + CK.S_COMMAND_EVENTS, commandEvents);
                    context.setSessionData(stagePrefix + CK.S_COMMAND_EVENT_TRIGGERS, commandEventTriggers);
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
            return ChatColor.YELLOW + Lang.get("timePrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                return new StageMainPrompt(plugin, stageNum, context);
            }
            if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + CK.S_DELAY, null);
                player.sendMessage(ChatColor.GREEN + Lang.get("stageEditorDelayCleared"));
                return new StageMainPrompt(plugin, stageNum, context);
            }
            long stageDelay;
            try {
                int i = Integer.parseInt(input);
                stageDelay = i * 1000;
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + Lang.get("reqNotANumber").replace("<input>", input));
                return new DelayPrompt();
            }
            if (stageDelay < 1000) {
                player.sendMessage(ChatColor.RED + Lang.get("invalidMinimum").replace("<number>", "1"));
                return new DelayPrompt();
            } else {
                context.setSessionData(stagePrefix + CK.S_DELAY, stageDelay);
                return new StageMainPrompt(plugin, stageNum, context);
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
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                context.setSessionData(stagePrefix + CK.S_DELAY_MESSAGE, input);
                return new StageMainPrompt(plugin, stageNum, context);
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + CK.S_DELAY_MESSAGE, null);
                player.sendMessage(ChatColor.YELLOW + Lang.get("stageEditorMessageCleared"));
                return new StageMainPrompt(plugin, stageNum, context);
            } else {
                return new DelayMessagePrompt();
            }
        }
    }

    private class DeletePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.GREEN + "" + ChatColor.BOLD + "1" + ChatColor.RESET + "" + ChatColor.GREEN + " - " 
                    + Lang.get("yesWord") + "\n";
            text += ChatColor.RED + "" + ChatColor.BOLD + "2" + ChatColor.RESET + "" + ChatColor.RED + " - " 
                    + Lang.get("noWord");
            return ChatColor.RED + Lang.get("confirmDelete") + " (" + ChatColor.YELLOW + Lang.get("stageEditorStage") 
                    + " " + stageNum + ChatColor.RED + ")\n" + ChatColor.GOLD + "(" 
                    + Lang.get("stageEditorConfirmStageNote") + ")\n" + text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase("Yes")) {
                new StageMenuPrompt(plugin, context).deleteStage(context, stageNum);
                player.sendMessage(ChatColor.YELLOW + Lang.get("stageEditorDeleteSucces"));
                return new StageMenuPrompt(plugin, context);
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase("No")) {
                return new StageMainPrompt(plugin, stageNum, context);
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
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                context.setSessionData(stagePrefix + CK.S_START_MESSAGE, input);
                return new StageMainPrompt(plugin, stageNum, context);
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + CK.S_START_MESSAGE, null);
                player.sendMessage(ChatColor.YELLOW + Lang.get("stageEditorMessageCleared"));
                return new StageMainPrompt(plugin, stageNum, context);
            } else {
                return new StageMainPrompt(plugin, stageNum, context);
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
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                context.setSessionData(stagePrefix + CK.S_COMPLETE_MESSAGE, input);
                return new StageMainPrompt(plugin, stageNum, context);
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + CK.S_COMPLETE_MESSAGE, null);
                player.sendMessage(ChatColor.YELLOW + Lang.get("stageEditorMessageCleared"));
                return new StageMainPrompt(plugin, stageNum, context);
            } else {
                return new StageMainPrompt(plugin, stageNum, context);
            }
        }
    }

    private class CustomObjectivesPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.LIGHT_PURPLE + "- " + Lang.get("stageEditorCustom") + " -\n";
            if (plugin.getCustomObjectives().isEmpty()) {
                text += ChatColor.DARK_PURPLE + "(" + Lang.get("stageEditorNoModules") + ") ";
            } else {
                for (CustomObjective co : plugin.getCustomObjectives()) {
                    text += ChatColor.DARK_PURPLE + "  - " + co.getName() + "\n";
                }
            }
            return text + ChatColor.YELLOW + Lang.get("stageEditorCustomPrompt");
        }

        @SuppressWarnings("unchecked")
        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
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
                    if (context.getSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES) != null) {
                        // The custom objective may already have been added, so let's check that
                        LinkedList<String> list 
                                = (LinkedList<String>) context.getSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES);
                        LinkedList<Entry<String, Object>> datamapList = (LinkedList<Entry<String, Object>>) context
                                .getSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES_DATA);
                        LinkedList<Integer> countList 
                                = (LinkedList<Integer>) context.getSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES_COUNT);
                        if (list.contains(found.getName()) == false) {
                            // Hasn't been added yet, so let's do it
                            list.add(found.getName());
                            datamapList.addAll(found.getData());
                            countList.add(-999);
                            context.setSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES, list);
                            context.setSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES_DATA, datamapList);
                        } else {
                            // Already added, so inform user
                            context.getForWhom().sendRawMessage(ChatColor.YELLOW 
                                    + Lang.get("stageEditorCustomAlreadyAdded"));
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
                        context.setSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES, list);
                        context.setSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES_DATA, datamapList);
                        context.setSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES_COUNT, countList);
                    }
                    // Send user to the count prompt / custom data prompt if there is any needed
                    if (found.canShowCount()) {
                        return new CustomObjectiveCountPrompt();
                    }
                    if (found.getData().isEmpty() == false) {
                        context.setSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES_DATA_DESCRIPTIONS, found
                                .getDescriptions());
                        return new ObjectiveCustomDataListPrompt();
                    }
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorModuleNotFound"));
                    return new CustomObjectivesPrompt();
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES, null);
                context.setSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES_DATA, null);
                context.setSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES_DATA_TEMP, null);
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorCustomCleared"));
            }
            return new StageMainPrompt(plugin, stageNum, context);
        }
    }

    private class CustomObjectiveCountPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.BOLD + "" + ChatColor.AQUA + "- ";
            @SuppressWarnings("unchecked")
            LinkedList<String> list = (LinkedList<String>) context.getSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES);
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
                LinkedList<Integer> counts 
                        = (LinkedList<Integer>) context.getSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES_COUNT);
                counts.set(counts.size() - 1, num);
                LinkedList<String> list = (LinkedList<String>) context.getSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES);
                String objName = list.getLast();
                CustomObjective found = null;
                for (CustomObjective co : plugin.getCustomObjectives()) {
                    if (co.getName().equals(objName)) {
                        found = co;
                        break;
                    }
                }
                if (found != null && found.getData().isEmpty() == false) {
                    context.setSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES_DATA_DESCRIPTIONS, found.getDescriptions());
                    return new ObjectiveCustomDataListPrompt();
                } else {
                    return new StageMainPrompt(plugin, stageNum, context);
                }
            } catch (NumberFormatException e) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqNotANumber")
                        .replace("<input>", input));
                return new CustomObjectiveCountPrompt();
            }
        }
    }

    private class ObjectiveCustomDataListPrompt extends StringPrompt {

        @SuppressWarnings("unchecked")
        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.AQUA + "- ";
            LinkedList<String> list = (LinkedList<String>) context.getSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES);
            LinkedList<Entry<String, Object>> datamapList 
                    = (LinkedList<Entry<String, Object>>) context.getSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES_DATA);
            String objName = list.getLast();
            CustomObjective found = null;
            for (CustomObjective co : plugin.getCustomObjectives()) {
                if (co.getName().equals(objName)) {
                    found = co;
                    break;
                }
            }
            if (found == null) {
                return "ERROR";
            }
            text += objName + " -\n";
            int index = 1;
            for (Entry<String, Object> datamap : found.getData()) {
                for (Entry<String, Object> currentData : datamapList) {
                    if (currentData.getKey().equals(datamap.getKey())) {
                        text += ChatColor.BLUE + "" + ChatColor.BOLD + index + ChatColor.RESET + ChatColor.YELLOW 
                                + " - " + datamap.getKey();
                if (currentData.getValue() != null) {
                    text += ChatColor.GREEN + " (" + currentData.getValue().toString() + ")\n";
                } else {
                    text += ChatColor.RED + " (" + Lang.get("valRequired") + ")\n";
                }
                index++;
                    }
                }
            }
            text += ChatColor.GREEN + "" + ChatColor.BOLD + index + ChatColor.YELLOW + " - " + Lang.get("done");
            return text;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            LinkedList<String> list = (LinkedList<String>) context.getSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES);
            String objName = list.getLast();
            CustomObjective found = null;
            for (CustomObjective co : plugin.getCustomObjectives()) {
                if (co.getName().equals(objName)) {
                    found = co;
                    break;
                }
            }
            if (found == null) {
                plugin.getLogger().severe("ERROR");
                return new ObjectiveCustomDataListPrompt();
            }
            LinkedList<Entry<String, Object>> datamapList = found.getData();
            
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
//                Collections.sort(datamapKeys);
                String selectedKey = datamapKeys.get(numInput - 1);
                context.setSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES_DATA_TEMP, selectedKey);
                return new ObjectiveCustomDataPrompt();
            } else {
                for (Entry<String, Object> datamap : (LinkedList<Entry<String, Object>>) context.getSessionData(stagePrefix 
                        + CK.S_CUSTOM_OBJECTIVES_DATA)) {
                    if (datamap.getValue() == null) {
                        return new ObjectiveCustomDataListPrompt();
                    }
                }
                context.setSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES_DATA_DESCRIPTIONS, null);
                return new StageMainPrompt(plugin, stageNum, context);
            }
        }
    }

    private class ObjectiveCustomDataPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String text = "";
            String temp = (String) context.getSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES_DATA_TEMP);
            @SuppressWarnings("unchecked")
            Map<String, String> descriptions 
                    = (Map<String, String>) context.getSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES_DATA_DESCRIPTIONS);
            if (descriptions.get(temp) != null) {
                text += ChatColor.GOLD + descriptions.get(temp) + "\n";
            }
            String msg = Lang.get("stageEditorCustomDataPrompt");
            msg = msg.replace("<data>", ChatColor.BOLD + temp + ChatColor.RESET + ChatColor.YELLOW);
            text += ChatColor.YELLOW + msg;
            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            @SuppressWarnings("unchecked")
            LinkedList<Entry<String, Object>> datamapList 
                    = (LinkedList<Entry<String, Object>>) context.getSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES_DATA);
            LinkedList<Entry<String, Object>> promptList = new LinkedList<Entry<String, Object>>();
            String temp = (String) context.getSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES_DATA_TEMP);
            for (Entry<String, Object> datamap : datamapList) {
                if (datamap.getKey().equals(temp)) {
                    promptList.add(new AbstractMap.SimpleEntry<String, Object>(datamap.getKey(), input));
                } else {
                    promptList.add(new AbstractMap.SimpleEntry<String, Object>(datamap.getKey(), datamap.getValue()));
                }
            }
            context.setSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES_DATA, promptList);
            context.setSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES_DATA_TEMP, null);
            return new ObjectiveCustomDataListPrompt();
        }
    }
}
