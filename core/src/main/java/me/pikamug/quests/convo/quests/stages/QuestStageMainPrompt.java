/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.convo.quests.stages;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.actions.Action;
import me.pikamug.quests.conditions.Condition;
import me.pikamug.quests.convo.QuestsNumericPrompt;
import me.pikamug.quests.convo.generic.OverridePrompt;
import me.pikamug.quests.convo.quests.QuestsEditorNumericPrompt;
import me.pikamug.quests.convo.quests.QuestsEditorStringPrompt;
import me.pikamug.quests.convo.quests.objectives.QuestBlocksPrompt;
import me.pikamug.quests.convo.quests.objectives.QuestItemsPrompt;
import me.pikamug.quests.convo.quests.objectives.QuestMobsPrompt;
import me.pikamug.quests.convo.quests.objectives.QuestNpcsPrompt;
import me.pikamug.quests.events.editor.quests.BukkitQuestsEditorPostOpenNumericPromptEvent;
import me.pikamug.quests.events.editor.quests.BukkitQuestsEditorPostOpenStringPromptEvent;
import me.pikamug.quests.module.CustomObjective;
import me.pikamug.quests.util.BukkitConfigUtil;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.quests.util.BukkitMiscUtil;
import me.pikamug.quests.util.Key;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class QuestStageMainPrompt extends QuestsEditorNumericPrompt {
    private final BukkitQuestsPlugin plugin;
    private final int stageNum;
    private final String stagePrefix;
    private final String classPrefix;
    private boolean hasObjective = false;
    private final int size = 17;

    public QuestStageMainPrompt(final int stageNum, final ConversationContext context) {
        super(context);
        this.plugin = (BukkitQuestsPlugin)context.getPlugin();
        this.stageNum = stageNum;
        this.stagePrefix = "stage" + stageNum;
        this.classPrefix = getClass().getSimpleName();
    }
    
    public int getStageNumber() {
        return stageNum;
    }
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle(final ConversationContext context) {
        return context.getSessionData(Key.Q_NAME) + " | " + BukkitLang.get("stageEditorStage") + " " + stageNum;
    }
    
    @Override
    public ChatColor getNumberColor(final ConversationContext context, final int number) {
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
        case 10:
        case 11:
            if (!hasObjective) {
                return ChatColor.GRAY;
            } else {
                return ChatColor.BLUE;
            }
        case 12:
            if (context.getSessionData(stagePrefix + Key.S_DELAY) == null) {
                return ChatColor.GRAY;
            } else {
                return ChatColor.BLUE;
            }
        case 13:
            if (context.getSessionData(stagePrefix + Key.S_START_MESSAGE) == null) {
                if (!hasObjective) {
                    return ChatColor.GRAY;
                } else {
                    return ChatColor.BLUE;
                }
            } else {
                return ChatColor.BLUE;
            }
        case 14:
            if (context.getSessionData(stagePrefix + Key.S_COMPLETE_MESSAGE) == null) {
                if (!hasObjective) {
                    return ChatColor.GRAY;
                } else {
                    return ChatColor.BLUE;
                }
            } else {
                return ChatColor.BLUE;
            }
        case 15:
            if (context.getSessionData(stagePrefix + Key.S_OVERRIDE_DISPLAY) == null) {
                if (!hasObjective) {
                    return ChatColor.GRAY;
                } else {
                    return ChatColor.BLUE;
                }
            } else {
                return ChatColor.BLUE;
            }
        case 16:
            return ChatColor.RED;
        case 17:
            return ChatColor.GREEN;
        default:
            return null;
        }
    }
    
    @Override
    public String getSelectionText(final ConversationContext context, final int number) {
        switch (number) {
        case 1:
            return ChatColor.GOLD + BukkitLang.get("stageEditorBlocks");
        case 2:
            return ChatColor.GOLD + BukkitLang.get("stageEditorItems");
        case 3:
            return ChatColor.GOLD + BukkitLang.get("stageEditorNPCs");
        case 4:
            return ChatColor.GOLD + BukkitLang.get("stageEditorMobs");
        case 5:
            return ChatColor.YELLOW + BukkitLang.get("stageEditorKillPlayers");
        case 6:
            return ChatColor.YELLOW + BukkitLang.get("stageEditorReachLocs");
        case 7:
            return ChatColor.YELLOW + BukkitLang.get("stageEditorPassword");
        case 8:
            return ChatColor.DARK_PURPLE + BukkitLang.get("stageEditorCustom");
        case 9:
            if (!hasObjective) {
                return ChatColor.GRAY + BukkitLang.get("stageEditorEvents");
            } else {
                return ChatColor.AQUA + BukkitLang.get("stageEditorEvents");
            }
        case 10:
            if (!hasObjective) {
                return ChatColor.GRAY + BukkitLang.get("stageEditorConditions");
            } else {
                return ChatColor.AQUA + BukkitLang.get("stageEditorConditions");
            }
        case 11:
            if (!hasObjective) {
                return ChatColor.GRAY + BukkitLang.get("delay");
            } else {
                return ChatColor.YELLOW + BukkitLang.get("delay");
            }
        case 12:
            if (context.getSessionData(stagePrefix + Key.S_DELAY) == null) {
                return ChatColor.GRAY + BukkitLang.get("stageEditorDelayMessage");
            } else {
                return ChatColor.YELLOW + BukkitLang.get("stageEditorDelayMessage");
            }
        case 13:
            if (context.getSessionData(stagePrefix + Key.S_START_MESSAGE) == null) {
                if (!hasObjective) {
                    return ChatColor.GRAY + BukkitLang.get("stageEditorStartMessage");
                } else {
                    return ChatColor.YELLOW + BukkitLang.get("stageEditorStartMessage");
                }
            } else {
                return ChatColor.YELLOW + BukkitLang.get("stageEditorStartMessage");
            }
        case 14:
            if (context.getSessionData(stagePrefix + Key.S_COMPLETE_MESSAGE) == null) {
                if (!hasObjective) {
                    return ChatColor.GRAY + BukkitLang.get("stageEditorCompleteMessage");
                } else {
                    return ChatColor.YELLOW + BukkitLang.get("stageEditorCompleteMessage");
                }
            } else {
                return ChatColor.YELLOW + BukkitLang.get("stageEditorCompleteMessage");
            }
        case 15:
            if (context.getSessionData(stagePrefix + Key.S_OVERRIDE_DISPLAY) == null) {
                if (!hasObjective) {
                    return ChatColor.GRAY + BukkitLang.get("overrideCreateSet");
                } else {
                    return ChatColor.YELLOW + BukkitLang.get("overrideCreateSet");
                }
            } else {
                return ChatColor.YELLOW + BukkitLang.get("overrideCreateSet");
            }
        case 16:
            return ChatColor.RED + BukkitLang.get("stageEditorDelete");
        case 17:
            return ChatColor.GREEN + BukkitLang.get("done");
        default:
            return null;
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public String getAdditionalText(final ConversationContext context, final int number) {
        switch (number) {
        case 1:
            if (context.getSessionData(stagePrefix + Key.S_BREAK_NAMES) == null
                    && context.getSessionData(stagePrefix + Key.S_DAMAGE_NAMES) == null
                    && context.getSessionData(stagePrefix + Key.S_PLACE_NAMES) == null
                    && context.getSessionData(stagePrefix + Key.S_USE_NAMES) == null
                    && context.getSessionData(stagePrefix + Key.S_CUT_NAMES) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                return "";
            }
        case 2:
            if (context.getSessionData(stagePrefix + Key.S_CRAFT_ITEMS) == null
                    && context.getSessionData(stagePrefix + Key.S_SMELT_ITEMS) == null
                    && context.getSessionData(stagePrefix + Key.S_ENCHANT_ITEMS) == null
                    && context.getSessionData(stagePrefix + Key.S_BREW_ITEMS) == null
                    && context.getSessionData(stagePrefix + Key.S_CONSUME_ITEMS) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                return "";
            }
        case 3:
            if (context.getSessionData(stagePrefix + Key.S_DELIVERY_NPCS) == null
                    && context.getSessionData(stagePrefix + Key.S_NPCS_TO_TALK_TO) == null
                    && context.getSessionData(stagePrefix + Key.S_NPCS_TO_KILL) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                return "";
            }
        case 4:
            if (context.getSessionData(stagePrefix + Key.S_MOB_TYPES) == null
                    && context.getSessionData(stagePrefix + Key.S_TAME_TYPES) == null
                    && context.getSessionData(stagePrefix + Key.S_FISH) == null
                    && context.getSessionData(stagePrefix + Key.S_COW_MILK) == null
                    && context.getSessionData(stagePrefix + Key.S_SHEAR_COLORS) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                return "";
            }
        case 5:
            if (context.getSessionData(stagePrefix + Key.S_PLAYER_KILL) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final Integer players = (Integer) context.getSessionData(stagePrefix + Key.S_PLAYER_KILL);
                return ChatColor.GRAY + "(" + ChatColor.AQUA + players + " " + BukkitLang.get("stageEditorPlayers")
                        + ChatColor.GRAY + ")";
            }
        case 6:
            if (context.getSessionData(stagePrefix + Key.S_REACH_LOCATIONS) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> locations 
                        = (LinkedList<String>) context.getSessionData(stagePrefix + Key.S_REACH_LOCATIONS);
                final LinkedList<Integer> radii 
                        = (LinkedList<Integer>) context.getSessionData(stagePrefix + Key.S_REACH_LOCATIONS_RADIUS);
                final LinkedList<String> names 
                        = (LinkedList<String>) context.getSessionData(stagePrefix + Key.S_REACH_LOCATIONS_NAMES);
                if (locations != null && radii != null && names != null) {
                    for (int i = 0; i < locations.size(); i++) {
                        text.append("\n").append(ChatColor.GRAY).append("     - ")
                                .append(BukkitLang.get("stageEditorReachRadii1")).append(" ").append(ChatColor.BLUE)
                                .append(radii.get(i)).append(ChatColor.GRAY).append(" ")
                                .append(BukkitLang.get("stageEditorReachRadii2")).append(" ").append(ChatColor.AQUA)
                                .append(names.get(i)).append(ChatColor.GRAY).append(" (").append(ChatColor.DARK_AQUA)
                                .append(locations.get(i)).append(ChatColor.GRAY).append(")");
                    }
                }
                return text.toString();
            }
        case 7:
            if (context.getSessionData(stagePrefix + Key.S_PASSWORD_PHRASES) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> passPhrases
                        = (LinkedList<String>) context.getSessionData(stagePrefix + Key.S_PASSWORD_PHRASES);
                final LinkedList<String> passDisplays 
                        = (LinkedList<String>) context.getSessionData(stagePrefix + Key.S_PASSWORD_DISPLAYS);
                if (passPhrases != null && passDisplays != null) {
                    for (int i = 0; i < passDisplays.size(); i++) {
                        text.append("\n").append(ChatColor.AQUA).append("     - \"").append(passDisplays.get(i))
                                .append("\"\n").append(ChatColor.DARK_AQUA).append("          - ")
                                .append(passPhrases.get(i));
                    }
                }
                return text.toString();
            }
        case 8:
            if (context.getSessionData(stagePrefix + Key.S_CUSTOM_OBJECTIVES) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> customObj
                        = (LinkedList<String>) context.getSessionData(stagePrefix + Key.S_CUSTOM_OBJECTIVES);
                if (customObj != null) {
                    for (final String s : customObj) {
                        text.append("\n").append(ChatColor.LIGHT_PURPLE).append("     - ").append(s);
                    }
                }
                return text.toString();
            }
        case 9:
            if (!hasObjective) {
                return ChatColor.GRAY + "(" + BukkitLang.get("stageEditorOptional") + ")";
            } else if (context.getSessionData(stagePrefix + Key.S_START_EVENT) == null
                    && context.getSessionData(stagePrefix + Key.S_FINISH_EVENT) == null
                    && context.getSessionData(stagePrefix + Key.S_DEATH_EVENT) == null
                    && context.getSessionData(stagePrefix + Key.S_DISCONNECT_EVENT) == null
                    && context.getSessionData(stagePrefix + Key.S_CHAT_EVENTS) == null
                    && context.getSessionData(stagePrefix + Key.S_COMMAND_EVENTS) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                return "";
            }
        case 10:
            if (!hasObjective) {
                return ChatColor.GRAY + "(" + BukkitLang.get("stageEditorOptional") + ")";
            } else if (context.getSessionData(stagePrefix + Key.S_CONDITION) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                return "";
            }
        case 11:
            if (!hasObjective) {
                return ChatColor.GRAY + "(" + BukkitLang.get("stageEditorOptional") + ")";
            } else {
                if (context.getSessionData(stagePrefix + Key.S_DELAY) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final Long time = (Long) context.getSessionData(stagePrefix + Key.S_DELAY);
                    if (time == null) {
                        return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                    } else {
                        return ChatColor.GRAY + "(" + ChatColor.AQUA + BukkitMiscUtil.getTime(time) + ChatColor.GRAY + ")";
                    }
                }
            }
        case 12:
            if (!hasObjective) {
                return ChatColor.GRAY + "(" + BukkitLang.get("stageEditorOptional") + ")";
            } else if (context.getSessionData(stagePrefix + Key.S_DELAY) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noDelaySet") + ")";
            } else if (context.getSessionData(stagePrefix + Key.S_DELAY_MESSAGE) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + "\"" 
                        + context.getSessionData(stagePrefix + Key.S_DELAY_MESSAGE) + "\"" + ChatColor.GRAY + ")";
            }
        case 13:
            if (context.getSessionData(stagePrefix + Key.S_START_MESSAGE) == null) {
                if (!hasObjective) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("stageEditorOptional") + ")";
                } else {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                }
            } else {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + "\"" 
                        + context.getSessionData(stagePrefix + Key.S_START_MESSAGE) + "\"" + ChatColor.GRAY + ")";
            }
        case 14:
            if (context.getSessionData(stagePrefix + Key.S_COMPLETE_MESSAGE) == null) {
                if (!hasObjective) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("stageEditorOptional") + ")";
                } else {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                }
            } else {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + "\"" 
                        + context.getSessionData(stagePrefix + Key.S_COMPLETE_MESSAGE) + "\"" + ChatColor.GRAY + ")";
            }
        case 15:
            if (context.getSessionData(stagePrefix + Key.S_OVERRIDE_DISPLAY) == null) {
                if (!hasObjective) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("stageEditorOptional") + ")";
                } else {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                }
            } else {
                final StringBuilder text = new StringBuilder();
                if (stagePrefix != null) {
                    final List<String> overrides
                            = (List<String>) context.getSessionData(stagePrefix + Key.S_OVERRIDE_DISPLAY);
                    if (overrides != null) {
                        for (final String override : overrides) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA)
                                    .append(override);
                        }
                    }
                }
                return text.toString();
            }
        case 16:
        case 17:
            return "";
        default:
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull String getBasicPromptText(final ConversationContext context) {
        final String input = (String) context.getSessionData(classPrefix + "-override");
        if (input != null && !input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
            if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + Key.S_OVERRIDE_DISPLAY, null);
            } else {
                final LinkedList<String> overrides = new LinkedList<>();
                if (context.getSessionData(stagePrefix + Key.S_OVERRIDE_DISPLAY) != null) {
                    overrides.addAll((List<String>) context.getSessionData(stagePrefix + Key.S_OVERRIDE_DISPLAY));
                }
                overrides.add(input);
                context.setSessionData(stagePrefix + Key.S_OVERRIDE_DISPLAY, overrides);
                context.setSessionData(classPrefix + "-override", null);
            }
        }
        context.setSessionData(stagePrefix, Boolean.TRUE);
        checkObjective(context);

        final BukkitQuestsEditorPostOpenNumericPromptEvent event
                = new BukkitQuestsEditorPostOpenNumericPromptEvent(context, this);
        plugin.getServer().getPluginManager().callEvent(event);
        
        final StringBuilder text = new StringBuilder(ChatColor.LIGHT_PURPLE + "- " + ChatColor.AQUA
                + getTitle(context).replaceFirst(" \\| ", ChatColor.LIGHT_PURPLE + " | ") + " -");
        for (int i = 1; i <= size; i++) {
            text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                    .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i)).append(" ")
                    .append(getAdditionalText(context, i));
        }
        return text.toString();
    }

    @Override
    protected Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
        switch (input.intValue()) {
        case 1:
            return new QuestBlocksPrompt(stageNum, context);
        case 2:
            return new QuestItemsPrompt(stageNum, context);
        case 3:
            return new QuestNpcsPrompt(stageNum, context);
        case 4:
            return new QuestMobsPrompt(stageNum, context);
        case 5:
            return new QuestKillPlayerPrompt(context);
        case 6:
            return new QuestReachListPrompt(context);
        case 7:
            return new QuestPasswordListPrompt(context);
        case 8:
            return new QuestCustomObjectiveModulePrompt(context);
        case 9:
            if (hasObjective) {
                return new QuestActionListPrompt(context);
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("invalidOption"));
                return new QuestStageMainPrompt(stageNum, context);
            }
        case 10:
            if (hasObjective) {
                return new QuestConditionListPrompt(context);
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("invalidOption"));
                return new QuestStageMainPrompt(stageNum, context);
            }
        case 11:
            if (hasObjective) {
                return new QuestDelayPrompt(context);
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("invalidOption"));
                return new QuestStageMainPrompt(stageNum, context);
            }
        case 12:
            if (context.getSessionData(stagePrefix + Key.S_DELAY) == null) {
                context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("stageEditorNoDelaySet"));
                return new QuestStageMainPrompt(stageNum, context);
            } else {
                return new QuestDelayMessagePrompt(context);
            }
        case 13:
            if (hasObjective) {
                return new QuestStartMessagePrompt(context);
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("invalidOption"));
                return new QuestStageMainPrompt(stageNum, context);
            }
        case 14:
            if (hasObjective) {
                return new QuestCompleteMessagePrompt(context);
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("invalidOption"));
                return new QuestStageMainPrompt(stageNum, context);
            }
        case 15:
            if (hasObjective) {
                return new OverridePrompt.Builder()
                        .source(this)
                        .promptText(BukkitLang.get("overrideCreateEnter"))
                        .build();
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("invalidOption"));
                return new QuestStageMainPrompt(stageNum, context);
            }
        case 16:
            return new QuestStageDeletePrompt(context);
        case 17:
            return new QuestStageMenuPrompt(context);
        default:
            return new QuestStageMainPrompt(stageNum, context);
        }
    }
    
    public boolean checkObjective(final ConversationContext context) {
        if (context.getSessionData(stagePrefix + Key.S_BREAK_NAMES) != null
                || context.getSessionData(stagePrefix + Key.S_DAMAGE_NAMES) != null
                || context.getSessionData(stagePrefix + Key.S_PLACE_NAMES) != null
                || context.getSessionData(stagePrefix + Key.S_USE_NAMES) != null
                || context.getSessionData(stagePrefix + Key.S_CUT_NAMES) != null
                || context.getSessionData(stagePrefix + Key.S_CRAFT_ITEMS) != null
                || context.getSessionData(stagePrefix + Key.S_SMELT_ITEMS) != null
                || context.getSessionData(stagePrefix + Key.S_ENCHANT_ITEMS) != null
                || context.getSessionData(stagePrefix + Key.S_BREW_ITEMS) != null
                || context.getSessionData(stagePrefix + Key.S_CONSUME_ITEMS) != null
                || context.getSessionData(stagePrefix + Key.S_DELIVERY_NPCS) != null
                || context.getSessionData(stagePrefix + Key.S_NPCS_TO_TALK_TO) != null
                || context.getSessionData(stagePrefix + Key.S_NPCS_TO_KILL) != null
                || context.getSessionData(stagePrefix + Key.S_MOB_TYPES) != null
                || context.getSessionData(stagePrefix + Key.S_FISH) != null
                || context.getSessionData(stagePrefix + Key.S_COW_MILK) != null
                || context.getSessionData(stagePrefix + Key.S_TAME_TYPES) != null
                || context.getSessionData(stagePrefix + Key.S_SHEAR_COLORS) != null
                || context.getSessionData(stagePrefix + Key.S_PLAYER_KILL) != null
                || context.getSessionData(stagePrefix + Key.S_REACH_LOCATIONS) != null
                || context.getSessionData(stagePrefix + Key.S_PASSWORD_PHRASES) != null
                || context.getSessionData(stagePrefix + Key.S_CUSTOM_OBJECTIVES) != null) {
            hasObjective = true;
            return true;
        }
        return false;
    }
    
    public class QuestKillPlayerPrompt extends QuestsEditorStringPrompt {
        
        public QuestKillPlayerPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("stageEditorKillPlayerPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input != null && !input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i < 0) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("stageEditorPositiveAmount"));
                        return new QuestKillPlayerPrompt(context);
                    } else if (i > 0) {
                        context.setSessionData(stagePrefix + Key.S_PLAYER_KILL, i);
                    }
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                            .replace("<input>", input));
                    return new QuestKillPlayerPrompt(context);
                }
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + Key.S_PLAYER_KILL, null);
            }
            return new QuestStageMainPrompt(stageNum, context);
        }
    }
    
    public class QuestReachListPrompt extends QuestsEditorNumericPrompt {

        public QuestReachListPrompt(final ConversationContext context) {
            super(context);
        }
        
        private final int size = 5;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("stageEditorReachLocs");
        }
        
        @Override
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
            switch (number) {
                case 1:
                case 2:
                case 3:
                    return ChatColor.BLUE;
                case 4:
                    return ChatColor.RED;
                case 5:
                    return ChatColor.GREEN;
                default:
                    return null;
            }
        }
        
        @Override
        public String getSelectionText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                return ChatColor.YELLOW + BukkitLang.get("stageEditorSetLocations");
            case 2:
                return ChatColor.YELLOW + BukkitLang.get("stageEditorSetLocationRadii");
            case 3:
                return ChatColor.YELLOW + BukkitLang.get("stageEditorSetLocationNames");
            case 4:
                return ChatColor.RED + BukkitLang.get("clear");
            case 5:
                return ChatColor.GREEN + BukkitLang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                if (context.getSessionData(stagePrefix + Key.S_REACH_LOCATIONS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> locations
                            = (List<String>) context.getSessionData(stagePrefix + Key.S_REACH_LOCATIONS);
                    if (locations != null) {
                        for (final String s : locations) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.DARK_AQUA)
                                    .append(s);
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (context.getSessionData(stagePrefix + Key.S_REACH_LOCATIONS_RADIUS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<Integer> radius
                            = (List<Integer>) context.getSessionData(stagePrefix + Key.S_REACH_LOCATIONS_RADIUS);
                    if (radius != null) {
                        for (final Integer i : radius) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(i);
                        }
                    }
                    return text.toString();
                }
            case 3:
                if (context.getSessionData(stagePrefix + Key.S_REACH_LOCATIONS_NAMES) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> names
                            = (List<String>) context.getSessionData(stagePrefix + Key.S_REACH_LOCATIONS_NAMES);
                    if (names != null) {
                        for (final String s : names) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s);
                        }
                    }
                    return text.toString();
                }
            case 4:
            case 5:
                return "";
            default:
                return null;
            }
        }
        
        @Override
        public @NotNull String getBasicPromptText(final @NotNull ConversationContext context) {
            final BukkitQuestsEditorPostOpenNumericPromptEvent event
                    = new BukkitQuestsEditorPostOpenNumericPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle(context) + " -");
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i)).append(" ")
                        .append(getAdditionalText(context, i));
            }
            return text.toString();
        }
        
        @SuppressWarnings("unchecked")
        @Override
        protected Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
            switch(input.intValue()) {
            case 1:
                if (context.getForWhom() instanceof Player) {
                    final ConcurrentHashMap<UUID, Block> temp = plugin.getQuestFactory().getSelectedReachLocations();
                    if (BukkitMiscUtil.getWorlds().isEmpty()) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("unknownError"));
                        return new QuestReachListPrompt(context);
                    }
                    temp.put(((Player) context.getForWhom()).getUniqueId(),
                            Bukkit.getWorlds().get(0).getBlockAt(0,0,0));
                    plugin.getQuestFactory().setSelectedReachLocations(temp);
                    return new QuestReachLocationPrompt(context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("consoleError"));
                    return new QuestReachListPrompt(context);
                }
                
            case 2:
                if (context.getSessionData(stagePrefix + Key.S_REACH_LOCATIONS) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("stageEditorNoLocations"));
                    return new QuestReachListPrompt(context);
                } else {
                    return new QuestReachRadiiPrompt(context);
                }
            case 3:
                if (context.getSessionData(stagePrefix + Key.S_REACH_LOCATIONS) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("stageEditorNoLocations"));
                    return new QuestReachListPrompt(context);
                } else {
                    return new QuestReachNamesPrompt(context);
                }
            case 4:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorObjectiveCleared"));
                context.setSessionData(stagePrefix + Key.S_REACH_LOCATIONS, null);
                context.setSessionData(stagePrefix + Key.S_REACH_LOCATIONS_RADIUS, null);
                context.setSessionData(stagePrefix + Key.S_REACH_LOCATIONS_NAMES, null);
                return new QuestReachListPrompt(context);
            case 5:
                final int one;
                final int two;
                final int three;
                final List<String> locations
                        = (List<String>) context.getSessionData(stagePrefix + Key.S_REACH_LOCATIONS);
                final List<Integer> radius
                        = (List<Integer>) context.getSessionData(stagePrefix + Key.S_REACH_LOCATIONS_RADIUS);
                final List<String> names
                        = (List<String>) context.getSessionData(stagePrefix + Key.S_REACH_LOCATIONS_NAMES);
                if (locations != null) {
                    one = locations.size();
                } else {
                    one = 0;
                }
                if (radius != null) {
                    two = radius.size();
                } else {
                    two = 0;
                }
                if (names != null) {
                    three = names.size();
                } else {
                    three = 0;
                }
                if (one == two && two == three) {
                    return new QuestStageMainPrompt(stageNum, context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("listsNotSameSize"));
                    return new QuestReachListPrompt(context);
                }
            default:
                return new QuestStageMainPrompt(stageNum, context);
            }
        }
    }

    public class QuestReachLocationPrompt extends QuestsEditorStringPrompt {
        
        public QuestReachLocationPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("stageEditorReachLocationPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            final Player player = (Player) context.getForWhom();
            if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdAdd"))) {
                final Block block = plugin.getQuestFactory().getSelectedReachLocations().get(player.getUniqueId());
                if (block != null) {
                    final Location loc = block.getLocation();
                    final LinkedList<String> locations
                            = context.getSessionData(stagePrefix + Key.S_REACH_LOCATIONS) != null
                            ? (LinkedList<String>) context.getSessionData(stagePrefix + Key.S_REACH_LOCATIONS)
                            : new LinkedList<>();
                    if (locations != null) {
                        locations.add(BukkitConfigUtil.getLocationInfo(loc));
                    }
                    context.setSessionData(stagePrefix + Key.S_REACH_LOCATIONS, locations);

                    LinkedList<Integer> amounts = new LinkedList<>();
                    LinkedList<String> names = new LinkedList<>();
                    if (context.getSessionData(stagePrefix + Key.S_REACH_LOCATIONS_RADIUS) != null) {
                        amounts = (LinkedList<Integer>) context.getSessionData(stagePrefix + Key.S_REACH_LOCATIONS_RADIUS);
                    }
                    if (context.getSessionData(stagePrefix + Key.S_REACH_LOCATIONS_NAMES) != null) {
                        names = (LinkedList<String>) context.getSessionData(stagePrefix + Key.S_REACH_LOCATIONS_NAMES);
                    }
                    if (locations != null && amounts != null && names != null) {
                        for (int i = 0; i < locations.size(); i++) {
                            if (i >= amounts.size()) {
                                amounts.add(5);
                            }
                            if (i >= names.size()) {
                                names.add(BukkitLang.get("location").replace("<id>", "#" + (i + 1)));
                            }
                        }
                    }
                    context.setSessionData(stagePrefix + Key.S_REACH_LOCATIONS_RADIUS, amounts);
                    context.setSessionData(stagePrefix + Key.S_REACH_LOCATIONS_NAMES, names);
                } else {
                    player.sendMessage(ChatColor.RED + BukkitLang.get("stageEditorNoBlockSelected"));
                    return new QuestReachLocationPrompt(context);
                }
                final ConcurrentHashMap<UUID, Block> temp = plugin.getQuestFactory().getSelectedReachLocations();
                temp.remove(player.getUniqueId());
                plugin.getQuestFactory().setSelectedReachLocations(temp);
                return new QuestReachListPrompt(context);
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final ConcurrentHashMap<UUID, Block> temp = plugin.getQuestFactory().getSelectedReachLocations();
                temp.remove(player.getUniqueId());
                plugin.getQuestFactory().setSelectedReachLocations(temp);
                return new QuestReachListPrompt(context);
            } else {
                return new QuestReachLocationPrompt(context);
            }
        }
    }

    public class QuestReachRadiiPrompt extends QuestsEditorStringPrompt {
        
        public QuestReachRadiiPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("stageEditorReachLocationRadiiPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);

            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input != null && !input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final LinkedList<Integer> radii = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    try {
                        final int i = Integer.parseInt(s);
                        if (i < 1) {
                            context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("invalidMinimum")
                                    .replace("<number>", "1"));
                            return new QuestReachRadiiPrompt(context);
                        }
                        radii.add(i);
                    } catch (final NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                                .replace("<input>", input));
                        return new QuestReachRadiiPrompt(context);
                    }
                }
                context.setSessionData(stagePrefix + Key.S_REACH_LOCATIONS_RADIUS, radii);
            }
            return new QuestReachListPrompt(context);
        }
    }

    public class QuestReachNamesPrompt extends QuestsEditorStringPrompt {
        
        public QuestReachNamesPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("stageEditorReachLocationNamesPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input != null && !input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final LinkedList<String> locNames = new LinkedList<>(Arrays.asList(input.split(BukkitLang.get("charSemi"))));
                context.setSessionData(stagePrefix + Key.S_REACH_LOCATIONS_NAMES, locNames);
            }
            return new QuestReachListPrompt(context);
        }
    }

    public class QuestPasswordListPrompt extends QuestsEditorNumericPrompt {

        public QuestPasswordListPrompt(final ConversationContext context) {
            super(context);
        }
        
        private final int size = 4;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("stageEditorPassword");
        }
        
        @Override
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
            switch (number) {
                case 1:
                case 2:
                    return ChatColor.BLUE;
                case 3:
                    return ChatColor.RED;
                case 4:
                    return ChatColor.GREEN;
                default:
                    return null;
            }
        }
        
        @Override
        public String getSelectionText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                return ChatColor.YELLOW + BukkitLang.get("stageEditorAddPasswordDisplay");
            case 2:
                return ChatColor.YELLOW + BukkitLang.get("stageEditorAddPasswordPhrases");
            case 3:
                return ChatColor.RED + BukkitLang.get("clear");
            case 4:
                return ChatColor.GREEN + BukkitLang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                if (context.getSessionData(stagePrefix + Key.S_PASSWORD_DISPLAYS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> displays = (List<String>) context.getSessionData(stagePrefix
                            + Key.S_PASSWORD_DISPLAYS);
                    if (displays != null) {
                        for (final String display : displays) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA)
                                    .append(display);
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (context.getSessionData(stagePrefix + Key.S_PASSWORD_PHRASES) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> phrases = (List<String>) context.getSessionData(stagePrefix
                            + Key.S_PASSWORD_PHRASES);
                    if (phrases != null) {
                        for (final String phrase : phrases) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.DARK_AQUA)
                                    .append(phrase);
                        }
                    }
                    return text.toString();
                }
            case 3:
            case 4:
                return "";
            default:
                return null;
            }
        }

        @Override
        public @NotNull String getBasicPromptText(final @NotNull ConversationContext context) {
            final BukkitQuestsEditorPostOpenNumericPromptEvent event
                    = new BukkitQuestsEditorPostOpenNumericPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle(context) + " -");
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i)).append(" ")
                        .append(getAdditionalText(context, i));
            }
            return text.toString();
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
            switch(input.intValue()) {
            case 1:
                return new QuestPasswordDisplayPrompt(context);
            case 2:
                if (context.getSessionData(stagePrefix + Key.S_PASSWORD_DISPLAYS) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("stageEditorMustSetPasswordDisplays"));
                    return new QuestPasswordListPrompt(context);
                } else {
                    return new QuestPasswordPhrasePrompt(context);
                }
            case 3:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorObjectiveCleared"));
                context.setSessionData(stagePrefix + Key.S_PASSWORD_DISPLAYS, null);
                context.setSessionData(stagePrefix + Key.S_PASSWORD_PHRASES, null);
                return new QuestPasswordListPrompt(context);
            case 4:
                final int one;
                final int two;
                final List<String> displays
                        = (List<String>) context.getSessionData(stagePrefix + Key.S_PASSWORD_DISPLAYS);
                final List<String> phrases = (List<String>) context.getSessionData(stagePrefix + Key.S_PASSWORD_PHRASES);
                if (displays != null) {
                    one = displays.size();
                } else {
                    one = 0;
                }
                if (phrases != null) {
                    two = phrases.size();
                } else {
                    two = 0;
                }
                if (one == two) {
                    return new QuestStageMainPrompt(stageNum, context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("listsNotSameSize"));
                    return new QuestPasswordListPrompt(context);
                }
            default:
                return new QuestStageMainPrompt(stageNum, context);
            }
        }
    }

    public class QuestPasswordDisplayPrompt extends QuestsEditorStringPrompt {

        public QuestPasswordDisplayPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("stageEditorPasswordDisplayPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context) + "\n";
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input != null && !input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                if (context.getSessionData(stagePrefix + Key.S_PASSWORD_DISPLAYS) != null) {
                    @SuppressWarnings("unchecked")
                    final List<String> displays = (List<String>) context.getSessionData(stagePrefix
                            + Key.S_PASSWORD_DISPLAYS);
                    if (displays != null) {
                        displays.addAll(Arrays.asList(input.split(BukkitLang.get("charSemi"))));
                    }
                    context.setSessionData(stagePrefix + Key.S_PASSWORD_DISPLAYS, displays);
                } else {
                    final List<String> displays = new LinkedList<>(Arrays.asList(input.split(BukkitLang.get("charSemi"))));
                    context.setSessionData(stagePrefix + Key.S_PASSWORD_DISPLAYS, displays);
                }
            }
            return new QuestPasswordListPrompt(context);
        }
    }

    public class QuestPasswordPhrasePrompt extends QuestsEditorStringPrompt {
        
        public QuestPasswordPhrasePrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("stageEditorPasswordPhrasePrompt");
        }


        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context) + "\n";
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input != null && !input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                if (context.getSessionData(stagePrefix + Key.S_PASSWORD_PHRASES) != null) {
                    @SuppressWarnings("unchecked")
                    final List<String> phrases = (List<String>) context.getSessionData(stagePrefix
                            + Key.S_PASSWORD_PHRASES);
                    if (phrases != null) {
                        phrases.addAll(Arrays.asList(input.split(BukkitLang.get("charSemi"))));
                    }
                    context.setSessionData(stagePrefix + Key.S_PASSWORD_PHRASES, phrases);
                } else {
                    final List<String> phrases = new LinkedList<>(Arrays.asList(input.split(BukkitLang.get("charSemi"))));
                    context.setSessionData(stagePrefix + Key.S_PASSWORD_PHRASES, phrases);
                }
            }
            return new QuestPasswordListPrompt(context);
        }
    }

    public class QuestActionListPrompt extends QuestsEditorNumericPrompt {

        public QuestActionListPrompt(final ConversationContext context) {
            super(context);
        }
        
        private final int size = 8;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("stageEditorStageEvents");
        }
        
        @Override
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
            switch (number) {
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    return ChatColor.BLUE;
                case 8:
                    return ChatColor.GREEN;
                default:
                    return null;
            }
        }
        
        @Override
        public String getSelectionText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                return ChatColor.YELLOW + BukkitLang.get("stageEditorStartEvent");
            case 2:
                return ChatColor.YELLOW + BukkitLang.get("stageEditorFinishEvent");
            case 3:
                return ChatColor.YELLOW + BukkitLang.get("stageEditorFailEvent");
            case 4:
                return ChatColor.YELLOW + BukkitLang.get("stageEditorDeathEvent");
            case 5:
                return ChatColor.YELLOW + BukkitLang.get("stageEditorDisconnectEvent");
            case 6:
                return ChatColor.YELLOW + BukkitLang.get("stageEditorChatEvents");
            case 7:
                return ChatColor.YELLOW + BukkitLang.get("stageEditorCommandEvents");
            case 8:
                return ChatColor.GREEN + BukkitLang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                if (context.getSessionData(stagePrefix + Key.S_START_EVENT) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    return ChatColor.YELLOW + "(" + ChatColor.AQUA + context.getSessionData(stagePrefix
                            + Key.S_START_EVENT) + ChatColor.YELLOW + ")";
                }
            case 2:
                if (context.getSessionData(stagePrefix + Key.S_FINISH_EVENT) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    return ChatColor.YELLOW + "(" + ChatColor.AQUA + context.getSessionData(stagePrefix
                            + Key.S_FINISH_EVENT) + ChatColor.YELLOW + ")";
                }
            case 3:
                if (context.getSessionData(stagePrefix + Key.S_FAIL_EVENT) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    return ChatColor.YELLOW + "(" + ChatColor.AQUA + context.getSessionData(stagePrefix
                            + Key.S_FAIL_EVENT) + ChatColor.YELLOW + ")";
                }  
            case 4:
                if (context.getSessionData(stagePrefix + Key.S_DEATH_EVENT) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    return ChatColor.YELLOW + "(" + ChatColor.AQUA + context.getSessionData(stagePrefix
                            + Key.S_DEATH_EVENT) + ChatColor.YELLOW + ")";
                }
            case 5:
                if (context.getSessionData(stagePrefix + Key.S_DISCONNECT_EVENT) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    return ChatColor.YELLOW + "(" + ChatColor.AQUA + context.getSessionData(stagePrefix
                            + Key.S_DISCONNECT_EVENT) + ChatColor.YELLOW + ")";
                }
            case 6:
                if (context.getSessionData(stagePrefix + Key.S_CHAT_EVENTS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final LinkedList<String> chatEvents = (LinkedList<String>) context.getSessionData(stagePrefix 
                            + Key.S_CHAT_EVENTS);
                    final LinkedList<String> chatEventTriggers 
                            = (LinkedList<String>) context.getSessionData(stagePrefix + Key.S_CHAT_EVENT_TRIGGERS);
                    if (chatEvents != null && chatEventTriggers != null) {
                        for (final String event : chatEvents) {
                            text.append("\n").append(ChatColor.AQUA).append("     - ").append(event)
                                    .append(ChatColor.BLUE).append(" (").append(BukkitLang.get("stageEditorTriggeredBy"))
                                    .append(": \"").append(chatEventTriggers.get(chatEvents.indexOf(event)))
                                    .append("\")");
                        }
                    }
                    return text.toString();
                }
            case 7:
                if (context.getSessionData(stagePrefix + Key.S_COMMAND_EVENTS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final LinkedList<String> commandEvents 
                            = (LinkedList<String>) context.getSessionData(stagePrefix + Key.S_COMMAND_EVENTS);
                    final LinkedList<String> commandEventTriggers 
                            = (LinkedList<String>) context.getSessionData(stagePrefix + Key.S_COMMAND_EVENT_TRIGGERS);
                    if (commandEvents != null && commandEventTriggers != null) {
                        for (final String event : commandEvents) {
                            text.append("\n").append(ChatColor.AQUA).append("     - ").append(event)
                                    .append(ChatColor.BLUE).append(" (").append(BukkitLang.get("stageEditorTriggeredBy"))
                                    .append(": \"").append(commandEventTriggers.get(commandEvents.indexOf(event)))
                                    .append("\")");
                        }
                    }
                    return text.toString();
                }
            case 8:
                return "";
            default:
                return null;
            }
        }

        @Override
        public @NotNull String getBasicPromptText(final @NotNull ConversationContext context) {
            final BukkitQuestsEditorPostOpenNumericPromptEvent event
                    = new BukkitQuestsEditorPostOpenNumericPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle(context) + " -");
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i)).append(" ")
                        .append(getAdditionalText(context, i));
            }
            return text.toString();
        }
        
        @Override
        protected Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
            switch(input.intValue()) {
            case 1:
                return new QuestStartActionPrompt(context);
            case 2:
                return new QuestFinishActionPrompt(context);
            case 3:
                return new QuestFailActionPrompt(context);
            case 4:
                return new QuestDeathActionPrompt(context);
            case 5:
                return new QuestDisconnectActionPrompt(context);
            case 6:
                return new QuestChatActionPrompt(context);
            case 7:
                return new QuestCommandActionPrompt(context);
            case 8:
                return new QuestStageMainPrompt(stageNum, context);
            default:
                return new QuestActionListPrompt(context);
            }
        }
    }

    public class QuestStartActionPrompt extends QuestsEditorStringPrompt {
        
        public QuestStartActionPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("stageEditorStartEvent");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("stageEditorEventsPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle(context) + " -\n");
            if (plugin.getLoadedActions().isEmpty()) {
                text.append(ChatColor.RED).append("- ").append(BukkitLang.get("none")).append("\n");
            } else {
                for (final Action a : plugin.getLoadedActions()) {
                    text.append(ChatColor.GREEN).append("- ").append(a.getName()).append("\n");
                }
            }
            return text.toString() + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input != null && !input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                Action found = null;
                for (final Action a : plugin.getLoadedActions()) {
                    if (a.getName().equalsIgnoreCase(input)) {
                        found = a;
                        break;
                    }
                }
                if (found == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("stageEditorInvalidEvent")
                            .replace("<input>", input));
                    return new QuestStartActionPrompt(context);
                } else {
                    context.setSessionData(stagePrefix + Key.S_START_EVENT, found.getName());
                    return new QuestActionListPrompt(context);
                }
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                return new QuestActionListPrompt(context);
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + Key.S_START_EVENT, null);
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorEventCleared"));
                return new QuestActionListPrompt(context);
            } else {
                return new QuestStartActionPrompt(context);
            }
        }
    }

    public class QuestFinishActionPrompt extends QuestsEditorStringPrompt {
        
        public QuestFinishActionPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("stageEditorFinishEvent");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("stageEditorEventsPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle(context) + " -\n");
            if (plugin.getLoadedActions().isEmpty()) {
                text.append(ChatColor.RED).append("- ").append(BukkitLang.get("none"));
            } else {
                for (final Action a : plugin.getLoadedActions()) {
                    text.append(ChatColor.GREEN).append("- ").append(a.getName()).append("\n");
                }
            }
            return text.toString() + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input != null && !input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                Action found = null;
                for (final Action a : plugin.getLoadedActions()) {
                    if (a.getName().equalsIgnoreCase(input)) {
                        found = a;
                        break;
                    }
                }
                if (found == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("stageEditorInvalidEvent")
                            .replace("<input>", input));
                    return new QuestFinishActionPrompt(context);
                } else {
                    context.setSessionData(stagePrefix + Key.S_FINISH_EVENT, found.getName());
                    return new QuestActionListPrompt(context);
                }
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                return new QuestActionListPrompt(context);
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + Key.S_FINISH_EVENT, null);
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorEventCleared"));
                return new QuestActionListPrompt(context);
            } else {
                return new QuestFinishActionPrompt(context);
            }
        }
    }
    
    public class QuestFailActionPrompt extends QuestsEditorStringPrompt {
        
        public QuestFailActionPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("stageEditorFailEvent");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("stageEditorEventsPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle(context) + " -\n");
            if (plugin.getLoadedActions().isEmpty()) {
                text.append(ChatColor.RED).append("- ").append(BukkitLang.get("none"));
            } else {
                for (final Action a : plugin.getLoadedActions()) {
                    text.append(ChatColor.GREEN).append("- ").append(a.getName()).append("\n");
                }
            }
            return text.toString() + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input != null && !input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                Action found = null;
                for (final Action a : plugin.getLoadedActions()) {
                    if (a.getName().equalsIgnoreCase(input)) {
                        found = a;
                        break;
                    }
                }
                if (found == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("stageEditorInvalidEvent")
                            .replace("<input>", input));
                    return new QuestFailActionPrompt(context);
                } else {
                    context.setSessionData(stagePrefix + Key.S_FAIL_EVENT, found.getName());
                    return new QuestActionListPrompt(context);
                }
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                return new QuestActionListPrompt(context);
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + Key.S_FAIL_EVENT, null);
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorEventCleared"));
                return new QuestActionListPrompt(context);
            } else {
                return new QuestFailActionPrompt(context);
            }
        }
    }

    public class QuestDeathActionPrompt extends QuestsEditorStringPrompt {
        
        public QuestDeathActionPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("stageEditorDeathEvent");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("stageEditorEventsPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle(context) + " -\n");
            if (plugin.getLoadedActions().isEmpty()) {
                text.append(ChatColor.RED).append("- ").append(BukkitLang.get("none")).append("\n");
            } else {
                for (final Action a : plugin.getLoadedActions()) {
                    text.append(ChatColor.GREEN).append("- ").append(a.getName()).append("\n");
                }
            }
            return text.toString() + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input != null && !input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                Action found = null;
                for (final Action a : plugin.getLoadedActions()) {
                    if (a.getName().equalsIgnoreCase(input)) {
                        found = a;
                        break;
                    }
                }
                if (found == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("stageEditorInvalidEvent")
                            .replace("<input>", input));
                    return new QuestDeathActionPrompt(context);
                } else {
                    context.setSessionData(stagePrefix + Key.S_DEATH_EVENT, found.getName());
                    return new QuestActionListPrompt(context);
                }
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                return new QuestActionListPrompt(context);
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + Key.S_DEATH_EVENT, null);
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorEventCleared"));
                return new QuestActionListPrompt(context);
            } else {
                return new QuestDeathActionPrompt(context);
            }
        }
    }

    public class QuestDisconnectActionPrompt extends QuestsEditorStringPrompt {
        
        public QuestDisconnectActionPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("stageEditorDisconnectEvent");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("stageEditorEventsPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle(context) + " -\n");
            if (plugin.getLoadedActions().isEmpty()) {
                text.append(ChatColor.RED).append("- ").append(BukkitLang.get("none"));
            } else {
                for (final Action a : plugin.getLoadedActions()) {
                    text.append(ChatColor.GREEN).append("- ").append(a.getName()).append("\n");
                }
            }
            return text.toString() + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input != null && !input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                Action found = null;
                for (final Action a : plugin.getLoadedActions()) {
                    if (a.getName().equalsIgnoreCase(input)) {
                        found = a;
                        break;
                    }
                }
                if (found == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("stageEditorInvalidEvent")
                            .replace("<input>", input));
                    return new QuestDisconnectActionPrompt(context);
                } else {
                    context.setSessionData(stagePrefix + Key.S_DISCONNECT_EVENT, found.getName());
                    return new QuestActionListPrompt(context);
                }
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                return new QuestActionListPrompt(context);
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + Key.S_DISCONNECT_EVENT, null);
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorEventCleared"));
                return new QuestActionListPrompt(context);
            } else {
                return new QuestDisconnectActionPrompt(context);
            }
        }
    }

    public class QuestChatActionPrompt extends QuestsEditorStringPrompt {
        
        public QuestChatActionPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("stageEditorChatEvents");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("stageEditorChatEventsPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle(context) + " -\n");
            if (plugin.getLoadedActions().isEmpty()) {
                text.append(ChatColor.RED).append("- ").append(BukkitLang.get("none"));
            } else {
                for (final Action a : plugin.getLoadedActions()) {
                    text.append(ChatColor.GREEN).append("- ").append(a.getName()).append("\n");
                }
            }
            return text.toString() + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input != null && !input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                Action found = null;
                for (final Action a : plugin.getLoadedActions()) {
                    if (a.getName().equalsIgnoreCase(input)) {
                        found = a;
                        break;
                    }
                }
                if (found == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("stageEditorInvalidEvent")
                            .replace("<input>", input));
                    return new QuestChatActionPrompt(context);
                } else {
                    context.setSessionData(stagePrefix + Key.S_CHAT_TEMP_EVENT, found.getName());
                    return new QuestChatActionTriggerPrompt(context);
                }
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                return new QuestActionListPrompt(context);
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + Key.S_CHAT_EVENTS, null);
                context.setSessionData(stagePrefix + Key.S_CHAT_EVENT_TRIGGERS, null);
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorChatEventsCleared"));
                return new QuestActionListPrompt(context);
            } else {
                return new QuestChatActionPrompt(context);
            }
        }
    }

    public class QuestChatActionTriggerPrompt extends QuestsEditorStringPrompt {
        
        public QuestChatActionTriggerPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("stageEditorChatTrigger");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            final String tempEvent = (String) context.getSessionData(stagePrefix + Key.S_CHAT_TEMP_EVENT);
            if (tempEvent != null) {
                return BukkitLang.get("stageEditorChatEventsTriggerPrompt").replace("<event>", tempEvent)
                        .replace("<action>", tempEvent);
            } else {
                return BukkitLang.get("stageEditorChatEventsTriggerPrompt");
            }
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final String tempEvent = (String) context.getSessionData(stagePrefix + Key.S_CHAT_TEMP_EVENT);
            String text = ChatColor.GOLD + "- " + getTitle(context) + " -\n";
            if (tempEvent != null) {
                text += ChatColor.YELLOW + getQueryText(context)
                        .replaceFirst(tempEvent, ChatColor.AQUA + tempEvent + ChatColor.YELLOW);
            }
            return text;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input != null && !input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                if (context.getSessionData(stagePrefix + Key.S_CHAT_EVENTS) == null) {
                    final LinkedList<String> chatEvents = new LinkedList<>();
                    final LinkedList<String> chatEventTriggers = new LinkedList<>();
                    final String event = (String) context.getSessionData(stagePrefix + Key.S_CHAT_TEMP_EVENT);
                    chatEvents.add(event);
                    chatEventTriggers.add(input.trim());
                    context.setSessionData(stagePrefix + Key.S_CHAT_EVENTS, chatEvents);
                    context.setSessionData(stagePrefix + Key.S_CHAT_EVENT_TRIGGERS, chatEventTriggers);
                    return new QuestActionListPrompt(context);
                } else {
                    final LinkedList<String> chatEvents 
                            = (LinkedList<String>) context.getSessionData(stagePrefix + Key.S_CHAT_EVENTS);
                    final LinkedList<String> chatEventTriggers 
                            = (LinkedList<String>) context.getSessionData(stagePrefix + Key.S_CHAT_EVENT_TRIGGERS);
                    final String event = (String) context.getSessionData(stagePrefix + Key.S_CHAT_TEMP_EVENT);
                    if (chatEvents != null && chatEventTriggers != null) {
                        chatEvents.add(event);
                        chatEventTriggers.add(input.trim());
                        context.setSessionData(stagePrefix + Key.S_CHAT_EVENTS, chatEvents);
                        context.setSessionData(stagePrefix + Key.S_CHAT_EVENT_TRIGGERS, chatEventTriggers);
                    }
                    return new QuestActionListPrompt(context);
                }
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                return new QuestActionListPrompt(context);
            } else {
                return new QuestChatActionTriggerPrompt(context);
            }
        }
    }
    
    public class QuestCommandActionPrompt extends QuestsEditorStringPrompt {
        
        public QuestCommandActionPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("stageEditorCommandEvents");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("stageEditorCommandEventsPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle(context) + " -\n");
            if (plugin.getLoadedActions().isEmpty()) {
                text.append(ChatColor.RED).append("- ").append(BukkitLang.get("none"));
            } else {
                for (final Action a : plugin.getLoadedActions()) {
                    text.append(ChatColor.GREEN).append("- ").append(a.getName()).append("\n");
                }
            }
            return text.toString() + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input != null && !input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                Action found = null;
                for (final Action a : plugin.getLoadedActions()) {
                    if (a.getName().equalsIgnoreCase(input)) {
                        found = a;
                        break;
                    }
                }
                if (found == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("stageEditorInvalidEvent")
                            .replace("<input>", input));
                    return new QuestCommandActionPrompt(context);
                } else {
                    context.setSessionData(stagePrefix + Key.S_COMMAND_TEMP_EVENT, found.getName());
                    return new QuestCommandActionTriggerPrompt(context);
                }
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                return new QuestActionListPrompt(context);
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + Key.S_COMMAND_EVENTS, null);
                context.setSessionData(stagePrefix + Key.S_COMMAND_EVENT_TRIGGERS, null);
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorCommandEventsCleared"));
                return new QuestActionListPrompt(context);
            } else {
                return new QuestCommandActionPrompt(context);
            }
        }
    }

    public class QuestCommandActionTriggerPrompt extends QuestsEditorStringPrompt {
        
        public QuestCommandActionTriggerPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("stageEditorCommandTrigger");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            final String tempEvent = (String) context.getSessionData(stagePrefix + Key.S_COMMAND_TEMP_EVENT);
            if (tempEvent != null) {
                return BukkitLang.get("stageEditorCommandEventsTriggerPrompt").replace("<event>", tempEvent)
                        .replace("<action>", tempEvent);
            } else {
                return BukkitLang.get("stageEditorCommandEventsTriggerPrompt");
            }
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final String tempEvent = (String) context.getSessionData(stagePrefix + Key.S_COMMAND_TEMP_EVENT);
            String text = ChatColor.GOLD + "- " + getTitle(context) + " -\n";
            if (tempEvent != null) {
                text += ChatColor.YELLOW + getQueryText(context)
                        .replaceFirst(tempEvent, ChatColor.AQUA + tempEvent + ChatColor.YELLOW);
            }
            return text;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input != null && !input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                if (context.getSessionData(stagePrefix + Key.S_COMMAND_EVENTS) == null) {
                    final LinkedList<String> commandEvents = new LinkedList<>();
                    final LinkedList<String> commandEventTriggers = new LinkedList<>();
                    final String event = (String) context.getSessionData(stagePrefix + Key.S_COMMAND_TEMP_EVENT);
                    commandEvents.add(event);
                    commandEventTriggers.add(input.trim());
                    context.setSessionData(stagePrefix + Key.S_COMMAND_EVENTS, commandEvents);
                    context.setSessionData(stagePrefix + Key.S_COMMAND_EVENT_TRIGGERS, commandEventTriggers);
                    return new QuestActionListPrompt(context);
                } else {
                    final LinkedList<String> commandEvents 
                            = (LinkedList<String>) context.getSessionData(stagePrefix + Key.S_COMMAND_EVENTS);
                    final LinkedList<String> commandEventTriggers 
                            = (LinkedList<String>) context.getSessionData(stagePrefix + Key.S_COMMAND_EVENT_TRIGGERS);
                    final String event = (String) context.getSessionData(stagePrefix + Key.S_COMMAND_TEMP_EVENT);
                    if (commandEvents != null && commandEventTriggers != null) {
                        commandEvents.add(event);
                        commandEventTriggers.add(input.trim());
                        context.setSessionData(stagePrefix + Key.S_COMMAND_EVENTS, commandEvents);
                        context.setSessionData(stagePrefix + Key.S_COMMAND_EVENT_TRIGGERS, commandEventTriggers);
                    }
                    return new QuestActionListPrompt(context);
                }
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                return new QuestActionListPrompt(context);
            } else {
                return new QuestCommandActionTriggerPrompt(context);
            }
        }
    }

    public class QuestConditionListPrompt extends QuestsEditorStringPrompt {

        public QuestConditionListPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("stageEditorConditions");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("stageEditorConditionsPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle(context) + " -\n");
            if (plugin.getLoadedConditions().isEmpty()) {
                text.append(ChatColor.RED).append("- ").append(BukkitLang.get("none")).append("\n");
            } else {
                for (final Condition c : plugin.getLoadedConditions()) {
                    text.append(ChatColor.GREEN).append("- ").append(c.getName()).append("\n");
                }
            }
            return text.toString() + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input != null && !input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                Condition found = null;
                for (final Condition c : plugin.getLoadedConditions()) {
                    if (c.getName().equalsIgnoreCase(input)) {
                        found = c;
                        break;
                    }
                }
                if (found == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("stageEditorInvalidCondition")
                            .replace("<input>", input));
                    return new QuestConditionListPrompt(context);
                } else {
                    context.setSessionData(stagePrefix + Key.S_CONDITION, found.getName());
                    return new QuestStageMainPrompt(stageNum, context);
                }
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                return new QuestStageMainPrompt(stageNum, context);
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + Key.S_CONDITION, null);
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorConditionCleared"));
                return new QuestStageMainPrompt(stageNum, context);
            } else {
                return new QuestConditionListPrompt(context);
            }
        }
    }
    
    public class QuestDelayPrompt extends QuestsEditorStringPrompt {
        
        public QuestDelayPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("timePrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input != null) {
                if (input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                    return new QuestStageMainPrompt(stageNum, context);
                }
                if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                    context.setSessionData(stagePrefix + Key.S_DELAY, null);
                    context.getForWhom().sendRawMessage(ChatColor.GREEN + BukkitLang.get("stageEditorDelayCleared"));
                    return new QuestStageMainPrompt(stageNum, context);
                }
                final long stageDelay;
                try {
                    final int i = Integer.parseInt(input);
                    stageDelay = i * 1000L;
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                            .replace("<input>", input));
                    return new QuestDelayPrompt(context);
                }
                if (stageDelay < 1000) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("invalidMinimum")
                            .replace("<number>", "1"));
                    return new QuestDelayPrompt(context);
                } else {
                    context.setSessionData(stagePrefix + Key.S_DELAY, stageDelay);
                    return new QuestStageMainPrompt(stageNum, context);
                }
            }
            return new QuestStageMainPrompt(stageNum, context);
        }
    }

    public class QuestDelayMessagePrompt extends QuestsEditorStringPrompt {
        
        public QuestDelayMessagePrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("stageEditorDelayMessagePrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input != null && !input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + Key.S_DELAY_MESSAGE, input);
                return new QuestStageMainPrompt(stageNum, context);
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + Key.S_DELAY_MESSAGE, null);
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorMessageCleared"));
                return new QuestStageMainPrompt(stageNum, context);
            } else {
                return new QuestDelayMessagePrompt(context);
            }
        }
    }
    

    public class QuestStartMessagePrompt extends QuestsEditorStringPrompt {
        
        public QuestStartMessagePrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("stageEditorStartMessagePrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input != null && !input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + Key.S_START_MESSAGE, input);
                return new QuestStageMainPrompt(stageNum, context);
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + Key.S_START_MESSAGE, null);
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorMessageCleared"));
                return new QuestStageMainPrompt(stageNum, context);
            } else {
                return new QuestStageMainPrompt(stageNum, context);
            }
        }
    }

    public class QuestCompleteMessagePrompt extends QuestsEditorStringPrompt {
        
        public QuestCompleteMessagePrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("stageEditorCompleteMessagePrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input != null && !input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + Key.S_COMPLETE_MESSAGE, input);
                return new QuestStageMainPrompt(stageNum, context);
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + Key.S_COMPLETE_MESSAGE, null);
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorMessageCleared"));
                return new QuestStageMainPrompt(stageNum, context);
            } else {
                return new QuestStageMainPrompt(stageNum, context);
            }
        }
    }

    public class QuestStageDeletePrompt extends QuestsEditorStringPrompt {
        
        public QuestStageDeletePrompt(final ConversationContext context) {
            super(context);
        }
        
        public final int size = 2;
        
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @SuppressWarnings("unused")
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                return ChatColor.GREEN;
            case 2:
                return ChatColor.RED;
            default:
                return null;
            }
        }

        @SuppressWarnings("unused")
        public String getSelectionText(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                return ChatColor.GREEN + BukkitLang.get("yesWord");
            case 2:
                return ChatColor.RED + BukkitLang.get("noWord");
            default:
                return null;
            }
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("confirmDelete");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.YELLOW + getQueryText(context) + " ("
                    + ChatColor.RED + BukkitLang.get("stageEditorStage") + " " + stageNum + ChatColor.YELLOW + ")\n"
                    + ChatColor.GOLD + "(" + BukkitLang.get("stageEditorConfirmStageNote") + ")\n");
            for (int i = 1; i <= size; i++) {
                text.append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i).append(ChatColor.RESET)
                        .append(" - ").append(getSelectionText(context, i)).append("\n");
            }
            return QuestsNumericPrompt.sendClickableSelection(text.toString(), context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input != null && (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(BukkitLang.get("yesWord")))) {
                new QuestStageMenuPrompt(context).deleteStage(context, stageNum);
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorDeleteSucces"));
                return new QuestStageMenuPrompt(context);
            } else if (input != null && (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(BukkitLang.get("noWord")))) {
                return new QuestStageMainPrompt(stageNum, context);
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("invalidOption"));
                return new QuestStageDeletePrompt(context);
            }
        }
    }

    public class QuestCustomObjectiveModulePrompt extends QuestsEditorStringPrompt {

        public QuestCustomObjectiveModulePrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("stageEditorModules");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("stageEditorModulePrompt");
        }

        @Override
        public @NotNull String getPromptText(@NotNull final ConversationContext context) {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);

            if (!(context.getForWhom() instanceof Player) || !plugin.getConfigSettings().canClickablePrompts()) {
                final StringBuilder text = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle(context) + "\n");
                if (plugin.getCustomObjectives().isEmpty()) {
                    text.append(ChatColor.DARK_AQUA).append(ChatColor.UNDERLINE)
                            .append("https://pikamug.gitbook.io/quests/casual/modules").append(ChatColor.RESET)
                            .append("\n");
                    text.append(ChatColor.RED).append("(").append(BukkitLang.get("stageEditorNoModules")).append(")")
                            .append("\n");
                } else {
                    for (final String name : plugin.getCustomObjectives().stream().map(CustomObjective::getModuleName)
                            .collect(Collectors.toCollection(TreeSet::new))) {
                        text.append(ChatColor.DARK_PURPLE).append("  - ").append(name).append("\n");
                    }
                }
                return text.toString() + ChatColor.YELLOW + getQueryText(context);
            }
            final TextComponent component = new TextComponent(getTitle(context) + "\n");
            component.setColor(net.md_5.bungee.api.ChatColor.LIGHT_PURPLE);
            final TextComponent line = new TextComponent("");
            if (plugin.getCustomObjectives().isEmpty()) {
                final TextComponent link = new TextComponent("https://pikamug.gitbook.io/quests/casual/modules\n");
                link.setColor(net.md_5.bungee.api.ChatColor.DARK_AQUA);
                link.setUnderlined(true);
                line.addExtra(link);
                line.addExtra(ChatColor.RED + "(" + BukkitLang.get("stageEditorNoModules") + ")\n");
            } else {
                for (final String name : plugin.getCustomObjectives().stream().map(CustomObjective::getModuleName)
                        .collect(Collectors.toCollection(TreeSet::new))) {
                    final TextComponent click = new TextComponent(ChatColor.DARK_PURPLE + "  - " + name + "\n");
                    click.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/quests choice " + name));
                    line.addExtra(click);
                }
            }
            component.addExtra(line);
            component.addExtra(ChatColor.YELLOW + getQueryText(context));
            ((Player)context.getForWhom()).spigot().sendMessage(component);
            return "";
        }

        @Override
        public Prompt acceptInput(@NotNull final ConversationContext context, @Nullable final String input) {
            if (input != null && !input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                String found = null;
                // Check if we have a module with the specified name
                for (final CustomObjective co : plugin.getCustomObjectives()) {
                    if (co.getModuleName().equalsIgnoreCase(input)) {
                        found = co.getModuleName();
                        break;
                    }
                }
                if (found == null) {
                    // No? Check again, but with locale sensitivity
                    for (final CustomObjective co : plugin.getCustomObjectives()) {
                        if (co.getModuleName().toLowerCase().contains(input.toLowerCase())) {
                            found = co.getModuleName();
                            break;
                        }
                    }
                }
                if (found != null) {
                    return new QuestCustomObjectivesPrompt(found, context);
                }
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                return new QuestStageMainPrompt(stageNum, context);
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + Key.S_CUSTOM_OBJECTIVES, null);
                context.setSessionData(stagePrefix + Key.S_CUSTOM_OBJECTIVES_DATA, null);
                context.setSessionData(stagePrefix + Key.S_CUSTOM_OBJECTIVES_DATA_TEMP, null);
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorCustomCleared"));
                return new QuestStageMainPrompt(stageNum, context);
            }
            context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("stageEditorModuleNotFound"));
            return new QuestCustomObjectiveModulePrompt(context);
        }
    }

    public class QuestCustomObjectivesPrompt extends QuestsEditorStringPrompt {

        private final String moduleName;

        public QuestCustomObjectivesPrompt(final String moduleName, final ConversationContext context) {
            super(context);
            this.moduleName = moduleName;
        }

        public String getModuleName() {
            return moduleName;
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("customObjectivesTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("stageEditorCustomPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);

            if (!(context.getForWhom() instanceof Player) || !plugin.getConfigSettings().canClickablePrompts()) {
                final StringBuilder text = new StringBuilder(ChatColor.LIGHT_PURPLE + "- " + getTitle(context)
                        + " -\n");
                if (plugin.getCustomObjectives().isEmpty()) {
                    text.append(ChatColor.DARK_AQUA).append(ChatColor.UNDERLINE)
                            .append("https://pikamug.gitbook.io/quests/casual/modules\n");
                    text.append(ChatColor.RED).append("(").append(BukkitLang.get("stageEditorNoModules")).append(")\n");
                } else {
                    for (final CustomObjective co : plugin.getCustomObjectives()) {
                        if (co.getModuleName().equals(moduleName)) {
                            text.append(ChatColor.DARK_PURPLE).append("  - ").append(co.getName()).append("\n");
                        }
                    }
                }
                return text.toString() + ChatColor.YELLOW + getQueryText(context);
            }
            final TextComponent component = new TextComponent(getTitle(context) + "\n");
            component.setColor(net.md_5.bungee.api.ChatColor.LIGHT_PURPLE);
            final TextComponent line = new TextComponent("");
            if (plugin.getCustomObjectives().isEmpty()) {
                final TextComponent link = new TextComponent("https://pikamug.gitbook.io/quests/casual/modules\n");
                link.setColor(net.md_5.bungee.api.ChatColor.DARK_AQUA);
                link.setUnderlined(true);
                line.addExtra(link);
                line.addExtra(ChatColor.RED + "(" + BukkitLang.get("stageEditorNoModules") + ")\n");
            } else {
                for (final CustomObjective co : plugin.getCustomObjectives()) {
                    if (co.getModuleName().equals(moduleName)) {
                        final TextComponent click = new TextComponent(ChatColor.DARK_PURPLE + "  - " + co.getName()
                                + "\n");
                        click.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/quests choice "
                                + co.getName()));
                        line.addExtra(click);
                    }
                }
            }
            component.addExtra(line);
            component.addExtra(ChatColor.YELLOW + getQueryText(context));
            ((Player)context.getForWhom()).spigot().sendMessage(component);
            return "";
        }

        @SuppressWarnings("unchecked")
        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input != null && !input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                CustomObjective found = null;
                for (final CustomObjective co : plugin.getCustomObjectives()) {
                    if (co.getModuleName().equals(moduleName)) {
                        if (co.getName().toLowerCase().contains(input.toLowerCase())) {
                            found = co;
                            break;
                        }
                    }
                }
                if (found != null) {
                    if (context.getSessionData(stagePrefix + Key.S_CUSTOM_OBJECTIVES) != null) {
                        // The custom objective may already have been added, so let's check that
                        final LinkedList<String> list = (LinkedList<String>) context.getSessionData(stagePrefix 
                                + Key.S_CUSTOM_OBJECTIVES);
                        final LinkedList<Entry<String, Object>> dataMapList
                                = (LinkedList<Entry<String, Object>>) context.getSessionData(stagePrefix 
                                + Key.S_CUSTOM_OBJECTIVES_DATA);
                        final LinkedList<Integer> countList = (LinkedList<Integer>) context.getSessionData(stagePrefix 
                                + Key.S_CUSTOM_OBJECTIVES_COUNT);
                        if (list != null && !list.contains(found.getName()) && dataMapList != null
                                && countList != null) {
                            // Hasn't been added yet, so let's do it
                            list.add(found.getName());
                            dataMapList.addAll(found.getData());
                            countList.add(-999);
                            context.setSessionData(stagePrefix + Key.S_CUSTOM_OBJECTIVES, list);
                            context.setSessionData(stagePrefix + Key.S_CUSTOM_OBJECTIVES_DATA, dataMapList);
                        } else {
                            // Already added, so inform user
                            context.getForWhom().sendRawMessage(ChatColor.RED
                                    + BukkitLang.get("stageEditorCustomAlreadyAdded"));
                            return new QuestCustomObjectivesPrompt(moduleName, context);
                        }
                    } else {
                        // The custom objective hasn't been added yet, so let's do it
                        final LinkedList<Integer> countList = new LinkedList<>();
                        final LinkedList<Entry<String, Object>> dataMapList = new LinkedList<>(found.getData());
                        countList.add(-999);
                        final LinkedList<String> list = new LinkedList<>();
                        list.add(found.getName());
                        context.setSessionData(stagePrefix + Key.S_CUSTOM_OBJECTIVES, list);
                        context.setSessionData(stagePrefix + Key.S_CUSTOM_OBJECTIVES_DATA, dataMapList);
                        context.setSessionData(stagePrefix + Key.S_CUSTOM_OBJECTIVES_COUNT, countList);
                    }
                    // Send user to the count prompt / custom data prompt if there is any needed
                    if (found.canShowCount()) {
                        return new QuestCustomObjectiveCountPrompt();
                    }
                    if (!found.getData().isEmpty()) {
                        context.setSessionData(stagePrefix + Key.S_CUSTOM_OBJECTIVES_DATA_DESCRIPTIONS, found
                                .getDescriptions());
                        return new QuestObjectiveCustomDataListPrompt();
                    }
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("stageEditorModuleNotFound"));
                    return new QuestCustomObjectivesPrompt(moduleName, context);
                }
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + Key.S_CUSTOM_OBJECTIVES, null);
                context.setSessionData(stagePrefix + Key.S_CUSTOM_OBJECTIVES_DATA, null);
                context.setSessionData(stagePrefix + Key.S_CUSTOM_OBJECTIVES_DATA_TEMP, null);
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorCustomCleared"));
            }
            return new QuestStageMainPrompt(stageNum, context);
        }
    }

    private class QuestCustomObjectiveCountPrompt extends StringPrompt {

        @Override
        public @NotNull String getPromptText(final ConversationContext context) {
            final StringBuilder text = new StringBuilder(ChatColor.GOLD + "- ");
            @SuppressWarnings("unchecked")
            final LinkedList<String> list
                    = (LinkedList<String>) context.getSessionData(stagePrefix + Key.S_CUSTOM_OBJECTIVES);
            if (list != null && plugin != null) {
                final String objName = list.getLast();
                text.append(objName).append(" -\n");
                CustomObjective found = null;
                for (final CustomObjective co : plugin.getCustomObjectives()) {
                    if (co.getName().equals(objName)) {
                        found = co;
                        break;
                    }
                }
                if (found != null) {
                    text.append(ChatColor.YELLOW).append(found.getCountPrompt());
                }
            }
            return text.toString();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            try {
                final int num = Integer.parseInt(input);
                final LinkedList<Integer> counts 
                        = (LinkedList<Integer>) context.getSessionData(stagePrefix + Key.S_CUSTOM_OBJECTIVES_COUNT);
                final LinkedList<String> list = (LinkedList<String>) context.getSessionData(stagePrefix 
                        + Key.S_CUSTOM_OBJECTIVES);
                if (counts != null && list != null && plugin != null) {
                    counts.set(counts.size() - 1, num);
                    final String objName = list.getLast();
                    CustomObjective found = null;
                    for (final CustomObjective co : plugin.getCustomObjectives()) {
                        if (co.getName().equals(objName)) {
                            found = co;
                            break;
                        }
                    }
                    if (found != null && !found.getData().isEmpty()) {
                        context.setSessionData(stagePrefix
                                + Key.S_CUSTOM_OBJECTIVES_DATA_DESCRIPTIONS, found.getDescriptions());
                        return new QuestObjectiveCustomDataListPrompt();
                    } else {
                        return new QuestStageMainPrompt(stageNum, context);
                    }
                }
            } catch (final NumberFormatException e) {
                context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                        .replace("<input>", input));
                return new QuestCustomObjectiveCountPrompt();
            }
            return new QuestStageMainPrompt(stageNum, context);
        }
    }

    private class QuestObjectiveCustomDataListPrompt extends StringPrompt {

        @SuppressWarnings("unchecked")
        @Override
        public @NotNull String getPromptText(final ConversationContext context) {
            final StringBuilder text = new StringBuilder(ChatColor.GOLD + "- ");
            final LinkedList<String> list = (LinkedList<String>) context.getSessionData(stagePrefix 
                    + Key.S_CUSTOM_OBJECTIVES);
            final LinkedList<Entry<String, Object>> dataMapList
                    = (LinkedList<Entry<String, Object>>) context.getSessionData(stagePrefix
                    + Key.S_CUSTOM_OBJECTIVES_DATA);
            if (list != null && plugin != null) {
                final String objName = list.getLast();
                CustomObjective found = null;
                for (final CustomObjective co : plugin.getCustomObjectives()) {
                    if (co.getName().equals(objName)) {
                        found = co;
                        break;
                    }
                }
                if (found == null) {
                    return "Could not find custom objective";
                }
                text.append(objName).append(" -\n");
                int index = 1;
                for (final Entry<String, Object> dataMap : found.getData()) {
                    if (dataMapList != null) {
                        for (final Entry<String, Object> currentData : dataMapList) {
                            if (currentData.getKey().equals(dataMap.getKey())) {
                                text.append(ChatColor.BLUE).append(ChatColor.BOLD).append(index).append(ChatColor.RESET)
                                        .append(ChatColor.YELLOW).append(" - ").append(dataMap.getKey());
                                if (currentData.getValue() != null) {
                                    text.append(ChatColor.GRAY).append(" (").append(ChatColor.AQUA)
                                            .append(ChatColor.translateAlternateColorCodes('&',
                                            currentData.getValue().toString())).append(ChatColor.GRAY).append(")\n");
                                } else {
                                    text.append(ChatColor.GRAY).append(" (").append(BukkitLang.get("noneSet"))
                                            .append(ChatColor.GRAY).append(")\n");
                                }
                                index++;
                            }
                        }
                    }
                }
                text.append(ChatColor.GREEN).append(ChatColor.BOLD).append(index).append(ChatColor.YELLOW)
                        .append(" - ").append(BukkitLang.get("done"));
            }
            return text.toString();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            final LinkedList<String> list = (LinkedList<String>) context.getSessionData(stagePrefix 
                    + Key.S_CUSTOM_OBJECTIVES);
            if (list != null && plugin != null) {
                final String objName = list.getLast();
                CustomObjective found = null;
                for (final CustomObjective co : plugin.getCustomObjectives()) {
                    if (co.getName().equals(objName)) {
                        found = co;
                        break;
                    }
                }
                if (found == null) {
                    plugin.getLogger().severe("Could not find custom objective following input: " + input);
                    return new QuestObjectiveCustomDataListPrompt();
                }
                final LinkedList<Entry<String, Object>> dataMapList = found.getData();

                final int numInput;
                try {
                    numInput = Integer.parseInt(input);
                } catch (final NumberFormatException nfe) {
                    return new QuestObjectiveCustomDataListPrompt();
                }
                if (numInput < 1 || numInput > dataMapList.size() + 1) {
                    return new QuestObjectiveCustomDataListPrompt();
                }
                if (numInput < dataMapList.size() + 1) {
                    final LinkedList<String> dataMapKeys = new LinkedList<>();
                    for (final Entry<String, Object> dataMap : dataMapList) {
                        dataMapKeys.add(dataMap.getKey());
                    }
                    // Collections.sort(dataMapKeys);
                    final String selectedKey = dataMapKeys.get(numInput - 1);
                    context.setSessionData(stagePrefix + Key.S_CUSTOM_OBJECTIVES_DATA_TEMP, selectedKey);
                    return new QuestObjectiveCustomDataPrompt();
                } else {
                    final LinkedList<Entry<String, Object>> dataMaps
                            = (LinkedList<Entry<String, Object>>) context.getSessionData(stagePrefix
                            + Key.S_CUSTOM_OBJECTIVES_DATA);
                    if (dataMaps != null) {
                        for (final Entry<String, Object> dataMap : dataMaps) {
                            if (dataMap.getValue() == null) {
                                return new QuestObjectiveCustomDataListPrompt();
                            }
                        }
                    }
                    context.setSessionData(stagePrefix + Key.S_CUSTOM_OBJECTIVES_DATA_DESCRIPTIONS, null);
                    return new QuestStageMainPrompt(stageNum, context);
                }
            }
            return new QuestStageMainPrompt(stageNum, context);
        }
    }

    private class QuestObjectiveCustomDataPrompt extends StringPrompt {

        @Override
        public @NotNull String getPromptText(final ConversationContext context) {
            String text = "";
            final String temp = (String) context.getSessionData(stagePrefix + Key.S_CUSTOM_OBJECTIVES_DATA_TEMP);
            @SuppressWarnings("unchecked")
            final
            Map<String, String> descriptions 
                    = (Map<String, String>) context.getSessionData(stagePrefix 
                    + Key.S_CUSTOM_OBJECTIVES_DATA_DESCRIPTIONS);
            if (descriptions != null && descriptions.get(temp) != null) {
                text += ChatColor.GOLD + descriptions.get(temp) + "\n";
            }
            String msg = BukkitLang.get("stageEditorCustomDataPrompt");
            msg = msg.replace("<data>", temp);
            text += ChatColor.YELLOW + msg;
            return text;
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            @SuppressWarnings("unchecked")
            final
            LinkedList<Entry<String, Object>> dataMapList
                    = (LinkedList<Entry<String, Object>>) context.getSessionData(stagePrefix
                    + Key.S_CUSTOM_OBJECTIVES_DATA);
            final LinkedList<Entry<String, Object>> promptList = new LinkedList<>();
            final String temp = (String) context.getSessionData(stagePrefix + Key.S_CUSTOM_OBJECTIVES_DATA_TEMP);
            if (dataMapList != null) {
                for (final Entry<String, Object> dataMap : dataMapList) {
                    if (dataMap.getKey().equals(temp)) {
                        promptList.add(new AbstractMap.SimpleEntry<>(dataMap.getKey(), input));
                    } else {
                        promptList.add(new AbstractMap.SimpleEntry<>(dataMap.getKey(), dataMap.getValue()));
                    }
                }
            }
            context.setSessionData(stagePrefix + Key.S_CUSTOM_OBJECTIVES_DATA, promptList);
            context.setSessionData(stagePrefix + Key.S_CUSTOM_OBJECTIVES_DATA_TEMP, null);
            return new QuestObjectiveCustomDataListPrompt();
        }
    }
}
