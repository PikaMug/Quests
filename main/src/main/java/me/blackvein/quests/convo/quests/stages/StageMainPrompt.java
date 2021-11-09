/*
 * Copyright (c) 2014 PikaMug and contributors. All rights reserved.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package me.blackvein.quests.convo.quests.stages;

import me.blackvein.quests.CustomObjective;
import me.blackvein.quests.Quests;
import me.blackvein.quests.actions.Action;
import me.blackvein.quests.conditions.Condition;
import me.blackvein.quests.convo.QuestsNumericPrompt;
import me.blackvein.quests.convo.generic.OverridePrompt;
import me.blackvein.quests.convo.quests.QuestsEditorNumericPrompt;
import me.blackvein.quests.convo.quests.QuestsEditorStringPrompt;
import me.blackvein.quests.convo.quests.objectives.BlocksPrompt;
import me.blackvein.quests.convo.quests.objectives.ItemsPrompt;
import me.blackvein.quests.convo.quests.objectives.MobsPrompt;
import me.blackvein.quests.convo.quests.objectives.NpcsPrompt;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenNumericPromptEvent;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenStringPromptEvent;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.ConfigUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;
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
import java.util.stream.Collectors;

public class StageMainPrompt extends QuestsEditorNumericPrompt {
    private final Quests plugin;
    private final int stageNum;
    private final String stagePrefix;
    private final String classPrefix;
    private boolean hasObjective = false;
    private final int size = 17;

    public StageMainPrompt(final int stageNum, final ConversationContext context) {
        super(context);
        this.plugin = (Quests)context.getPlugin();
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
        return context.getSessionData(CK.Q_NAME) + " | " + Lang.get("stageEditorStage") + " " + stageNum;
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
            if (context.getSessionData(stagePrefix + CK.S_DELAY) == null) {
                return ChatColor.GRAY;
            } else {
                return ChatColor.BLUE;
            }
        case 13:
            if (context.getSessionData(stagePrefix + CK.S_START_MESSAGE) == null) {
                if (!hasObjective) {
                    return ChatColor.GRAY;
                } else {
                    return ChatColor.BLUE;
                }
            } else {
                return ChatColor.BLUE;
            }
        case 14:
            if (context.getSessionData(stagePrefix + CK.S_COMPLETE_MESSAGE) == null) {
                if (!hasObjective) {
                    return ChatColor.GRAY;
                } else {
                    return ChatColor.BLUE;
                }
            } else {
                return ChatColor.BLUE;
            }
        case 15:
            if (context.getSessionData(stagePrefix + CK.S_OVERRIDE_DISPLAY) == null) {
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
                return ChatColor.GRAY + Lang.get("stageEditorConditions");
            } else {
                return ChatColor.AQUA + Lang.get("stageEditorConditions");
            }
        case 11:
            if (!hasObjective) {
                return ChatColor.GRAY + Lang.get("delay");
            } else {
                return ChatColor.YELLOW + Lang.get("delay");
            }
        case 12:
            if (context.getSessionData(stagePrefix + CK.S_DELAY) == null) {
                return ChatColor.GRAY + Lang.get("stageEditorDelayMessage");
            } else {
                return ChatColor.YELLOW + Lang.get("stageEditorDelayMessage");
            }
        case 13:
            if (context.getSessionData(stagePrefix + CK.S_START_MESSAGE) == null) {
                if (!hasObjective) {
                    return ChatColor.GRAY + Lang.get("stageEditorStartMessage");
                } else {
                    return ChatColor.YELLOW + Lang.get("stageEditorStartMessage");
                }
            } else {
                return ChatColor.YELLOW + Lang.get("stageEditorStartMessage");
            }
        case 14:
            if (context.getSessionData(stagePrefix + CK.S_COMPLETE_MESSAGE) == null) {
                if (!hasObjective) {
                    return ChatColor.GRAY + Lang.get("stageEditorCompleteMessage");
                } else {
                    return ChatColor.YELLOW + Lang.get("stageEditorCompleteMessage");
                }
            } else {
                return ChatColor.YELLOW + Lang.get("stageEditorCompleteMessage");
            }
        case 15:
            if (context.getSessionData(stagePrefix + CK.S_OVERRIDE_DISPLAY) == null) {
                if (!hasObjective) {
                    return ChatColor.GRAY + Lang.get("overrideCreateSet");
                } else {
                    return ChatColor.YELLOW + Lang.get("overrideCreateSet");
                }
            } else {
                return ChatColor.YELLOW + Lang.get("overrideCreateSet");
            }
        case 16:
            return ChatColor.RED + Lang.get("stageEditorDelete");
        case 17:
            return ChatColor.GREEN + Lang.get("done");
        default:
            return null;
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public String getAdditionalText(final ConversationContext context, final int number) {
        switch (number) {
        case 1:
            if (context.getSessionData(stagePrefix + CK.S_BREAK_NAMES) == null 
                    && context.getSessionData(stagePrefix + CK.S_DAMAGE_NAMES) == null
                    && context.getSessionData(stagePrefix + CK.S_PLACE_NAMES) == null
                    && context.getSessionData(stagePrefix + CK.S_USE_NAMES) == null
                    && context.getSessionData(stagePrefix + CK.S_CUT_NAMES) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                return "";
            }
        case 2:
            if (context.getSessionData(stagePrefix + CK.S_CRAFT_ITEMS) == null
                    && context.getSessionData(stagePrefix + CK.S_SMELT_ITEMS) == null 
                    && context.getSessionData(stagePrefix + CK.S_ENCHANT_ITEMS) == null 
                    && context.getSessionData(stagePrefix + CK.S_BREW_ITEMS) == null
                    && context.getSessionData(stagePrefix + CK.S_CONSUME_ITEMS) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                return "";
            }
        case 3:
            if (context.getSessionData(stagePrefix + CK.S_DELIVERY_NPCS) == null
                    && context.getSessionData(stagePrefix + CK.S_NPCS_TO_TALK_TO) == null 
                    && context.getSessionData(stagePrefix + CK.S_NPCS_TO_KILL) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                return "";
            }
        case 4:
            if (context.getSessionData(stagePrefix + CK.S_MOB_TYPES) == null
                    && context.getSessionData(stagePrefix + CK.S_TAME_TYPES) == null 
                    && context.getSessionData(stagePrefix + CK.S_FISH) == null 
                    && context.getSessionData(stagePrefix + CK.S_COW_MILK) == null
                    && context.getSessionData(stagePrefix + CK.S_SHEAR_COLORS) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                return "";
            }
        case 5:
            if (context.getSessionData(stagePrefix + CK.S_PLAYER_KILL) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                final Integer players = (Integer) context.getSessionData(stagePrefix + CK.S_PLAYER_KILL);
                return ChatColor.GRAY + "(" + ChatColor.AQUA + players + " " + Lang.get("stageEditorPlayers") 
                        + ChatColor.GRAY + ")";
            }
        case 6:
            if (context.getSessionData(stagePrefix + CK.S_REACH_LOCATIONS) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder("\n");
                final LinkedList<String> locations 
                        = (LinkedList<String>) context.getSessionData(stagePrefix + CK.S_REACH_LOCATIONS);
                final LinkedList<Integer> radii 
                        = (LinkedList<Integer>) context.getSessionData(stagePrefix + CK.S_REACH_LOCATIONS_RADIUS);
                final LinkedList<String> names 
                        = (LinkedList<String>) context.getSessionData(stagePrefix + CK.S_REACH_LOCATIONS_NAMES);
                if (locations != null && radii != null && names != null) {
                    for (int i = 0; i < locations.size(); i++) {
                        text.append(ChatColor.GRAY).append("     - ").append(Lang.get("stageEditorReachRadii1"))
                                .append(" ").append(ChatColor.BLUE).append(radii.get(i)).append(ChatColor.GRAY)
                                .append(" ").append(Lang.get("stageEditorReachRadii2")).append(" ")
                                .append(ChatColor.AQUA).append(names.get(i)).append(ChatColor.GRAY).append(" (")
                                .append(ChatColor.DARK_AQUA).append(locations.get(i)).append(ChatColor.GRAY)
                                .append(")\n");
                    }
                }
                return text.toString();
            }
        case 7:
            if (context.getSessionData(stagePrefix + CK.S_PASSWORD_PHRASES) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder("\n");
                final LinkedList<String> passPhrases
                        = (LinkedList<String>) context.getSessionData(stagePrefix + CK.S_PASSWORD_PHRASES);
                final LinkedList<String> passDisplays 
                        = (LinkedList<String>) context.getSessionData(stagePrefix + CK.S_PASSWORD_DISPLAYS);
                if (passPhrases != null && passDisplays != null) {
                    for (int i = 0; i < passDisplays.size(); i++) {
                        text.append(ChatColor.AQUA).append("     - \"").append(passDisplays.get(i)).append("\"\n");
                        text.append(ChatColor.DARK_AQUA).append("          - ").append(passPhrases.get(i)).append("\n");
                    }
                }
                return text.toString();
            }
        case 8:
            if (context.getSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder("\n");
                final LinkedList<String> customObj
                        = (LinkedList<String>) context.getSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES);
                if (customObj != null) {
                    for (final String s : customObj) {
                        text.append(ChatColor.LIGHT_PURPLE).append("     - ").append(ChatColor.GOLD).append(s)
                                .append("\n");
                    }
                }
                return text.toString();
            }
        case 9:
            if (!hasObjective) {
                return ChatColor.GRAY + "(" + Lang.get("stageEditorOptional") + ")";
            } else if (context.getSessionData(stagePrefix + CK.S_START_EVENT) == null
                    && context.getSessionData(stagePrefix + CK.S_FINISH_EVENT) == null 
                    && context.getSessionData(stagePrefix + CK.S_DEATH_EVENT) == null 
                    && context.getSessionData(stagePrefix + CK.S_DISCONNECT_EVENT) == null
                    && context.getSessionData(stagePrefix + CK.S_CHAT_EVENTS) == null
                    && context.getSessionData(stagePrefix + CK.S_COMMAND_EVENTS) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                return "";
            }
        case 10:
            if (!hasObjective) {
                return ChatColor.GRAY + "(" + Lang.get("stageEditorOptional") + ")";
            } else if (context.getSessionData(stagePrefix + CK.S_CONDITION) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                return "";
            }
        case 11:
            if (!hasObjective) {
                return ChatColor.GRAY + "(" + Lang.get("stageEditorOptional") + ")";
            } else {
                if (context.getSessionData(stagePrefix + CK.S_DELAY) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    final Long time = (Long) context.getSessionData(stagePrefix + CK.S_DELAY);
                    if (time == null) {
                        return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                    } else {
                        return ChatColor.GRAY + "(" + ChatColor.AQUA + MiscUtil.getTime(time) + ChatColor.GRAY + ")";
                    }
                }
            }
        case 12:
            if (!hasObjective) {
                return ChatColor.GRAY + "(" + Lang.get("stageEditorOptional") + ")";
            } else if (context.getSessionData(stagePrefix + CK.S_DELAY) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noDelaySet") + ")";
            } else if (context.getSessionData(stagePrefix + CK.S_DELAY_MESSAGE) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + "\"" 
                        + context.getSessionData(stagePrefix + CK.S_DELAY_MESSAGE) + "\"" + ChatColor.GRAY + ")";
            }
        case 13:
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
        case 14:
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
        case 15:
            if (context.getSessionData(stagePrefix + CK.S_OVERRIDE_DISPLAY) == null) {
                if (!hasObjective) {
                    return ChatColor.GRAY + "(" + Lang.get("stageEditorOptional") + ")";
                } else {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                }
            } else {
                final StringBuilder text = new StringBuilder("\n");
                if (stagePrefix != null) {
                    final List<String> overrides
                            = (List<String>) context.getSessionData(stagePrefix + CK.S_OVERRIDE_DISPLAY);
                    if (overrides != null) {
                        for (final String override : overrides) {
                            text.append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(override)
                                    .append("\n");
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
        if (input != null && !input.equalsIgnoreCase(Lang.get("cancel"))) {
            if (input.equalsIgnoreCase(Lang.get("clear"))) {
                context.setSessionData(stagePrefix + CK.S_OVERRIDE_DISPLAY, null);
            } else {
                final LinkedList<String> overrides = new LinkedList<>();
                if (context.getSessionData(stagePrefix + CK.S_OVERRIDE_DISPLAY) != null) {
                    overrides.addAll((List<String>) context.getSessionData(stagePrefix + CK.S_OVERRIDE_DISPLAY));
                }
                overrides.add(input);
                context.setSessionData(stagePrefix + CK.S_OVERRIDE_DISPLAY, overrides);
                context.setSessionData(classPrefix + "-override", null);
            }
        }
        context.setSessionData(stagePrefix, Boolean.TRUE);
        checkObjective(context);

        if (context.getPlugin() != null) {
            final QuestsEditorPostOpenNumericPromptEvent event
                    = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
        }
        
        final StringBuilder text = new StringBuilder(ChatColor.LIGHT_PURPLE + "- " + ChatColor.AQUA
                + getTitle(context).replaceFirst(" \\| ", ChatColor.LIGHT_PURPLE + " | ") + " -");
        for (int i = 1; i <= size; i++) {
            text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i).append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i)).append(" ").append(getAdditionalText(context, i));
        }
        return text.toString();
    }

    @Override
    protected Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
        switch (input.intValue()) {
        case 1:
            return new BlocksPrompt(stageNum, context);
        case 2:
            return new ItemsPrompt(stageNum, context);
        case 3:
            return new NpcsPrompt(stageNum, context);
        case 4:
            return new MobsPrompt(stageNum, context);
        case 5:
            return new KillPlayerPrompt(context);
        case 6:
            return new ReachListPrompt(context);
        case 7:
            return new PasswordListPrompt(context);
        case 8:
            return new CustomObjectiveModulePrompt(context);
        case 9:
            if (hasObjective) {
                return new ActionListPrompt(context);
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidOption"));
                return new StageMainPrompt(stageNum, context);
            }
        case 10:
            if (hasObjective) {
                return new ConditionListPrompt(context);
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidOption"));
                return new StageMainPrompt(stageNum, context);
            }
        case 11:
            if (hasObjective) {
                return new DelayPrompt(context);
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidOption"));
                return new StageMainPrompt(stageNum, context);
            }
        case 12:
            if (context.getSessionData(stagePrefix + CK.S_DELAY) == null) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNoDelaySet"));
                return new StageMainPrompt(stageNum, context);
            } else {
                return new DelayMessagePrompt(context);
            }
        case 13:
            if (hasObjective) {
                return new StartMessagePrompt(context);
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidOption"));
                return new StageMainPrompt(stageNum, context);
            }
        case 14:
            if (hasObjective) {
                return new CompleteMessagePrompt(context);
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidOption"));
                return new StageMainPrompt(stageNum, context);
            }
        case 15:
            if (hasObjective) {
                return new OverridePrompt.Builder()
                        .source(this)
                        .promptText(Lang.get("overrideCreateEnter"))
                        .build();
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidOption"));
                return new StageMainPrompt(stageNum, context);
            }
        case 16:
            return new StageDeletePrompt(context);
        case 17:
            return new StageMenuPrompt(context);
        default:
            return new StageMainPrompt(stageNum, context);
        }
    }
    
    public boolean checkObjective(final ConversationContext context) {
        if (context.getSessionData(stagePrefix + CK.S_BREAK_NAMES) != null 
                || context.getSessionData(stagePrefix + CK.S_DAMAGE_NAMES) != null
                || context.getSessionData(stagePrefix + CK.S_PLACE_NAMES) != null 
                || context.getSessionData(stagePrefix + CK.S_USE_NAMES) != null
                || context.getSessionData(stagePrefix + CK.S_CUT_NAMES) != null
                || context.getSessionData(stagePrefix + CK.S_CRAFT_ITEMS) != null 
                || context.getSessionData(stagePrefix + CK.S_SMELT_ITEMS) != null 
                || context.getSessionData(stagePrefix + CK.S_ENCHANT_ITEMS) != null 
                || context.getSessionData(stagePrefix + CK.S_BREW_ITEMS) != null
                || context.getSessionData(stagePrefix + CK.S_CONSUME_ITEMS) != null
                || context.getSessionData(stagePrefix + CK.S_DELIVERY_NPCS) != null 
                || context.getSessionData(stagePrefix + CK.S_NPCS_TO_TALK_TO) != null 
                || context.getSessionData(stagePrefix + CK.S_NPCS_TO_KILL) != null
                || context.getSessionData(stagePrefix + CK.S_MOB_TYPES) != null 
                || context.getSessionData(stagePrefix + CK.S_FISH) != null 
                || context.getSessionData(stagePrefix + CK.S_COW_MILK) != null 
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
    
    public class KillPlayerPrompt extends QuestsEditorStringPrompt {
        
        public KillPlayerPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorKillPlayerPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input != null && !input.equalsIgnoreCase(Lang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i < 0) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorPositiveAmount"));
                        return new KillPlayerPrompt(context);
                    } else if (i > 0) {
                        context.setSessionData(stagePrefix + CK.S_PLAYER_KILL, i);
                    }
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqNotANumber")
                            .replace("<input>", input));
                    return new KillPlayerPrompt(context);
                }
            } else if (input != null && input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + CK.S_PLAYER_KILL, null);
            }
            return new StageMainPrompt(stageNum, context);
        }
    }
    
    public class ReachListPrompt extends QuestsEditorNumericPrompt {

        public ReachListPrompt(final ConversationContext context) {
            super(context);
        }
        
        private final int size = 5;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("stageEditorReachLocs");
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
                return ChatColor.YELLOW + Lang.get("stageEditorSetLocations");
            case 2:
                return ChatColor.YELLOW + Lang.get("stageEditorSetLocationRadii");
            case 3:
                return ChatColor.YELLOW + Lang.get("stageEditorSetLocationNames");
            case 4:
                return ChatColor.RED + Lang.get("clear");
            case 5:
                return ChatColor.GREEN + Lang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                if (context.getSessionData(stagePrefix + CK.S_REACH_LOCATIONS) == null) {
                    return ChatColor.GRAY + " (" + Lang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder("\n");
                    final List<String> locations
                            = (List<String>) context.getSessionData(stagePrefix + CK.S_REACH_LOCATIONS);
                    if (locations != null) {
                        for (final String s : locations) {
                            text.append(ChatColor.GRAY).append("     - ").append(ChatColor.DARK_AQUA).append(s)
                                    .append("\n");
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (context.getSessionData(stagePrefix + CK.S_REACH_LOCATIONS_RADIUS) == null) {
                    return ChatColor.GRAY + " (" + Lang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder("\n");
                    final List<Integer> radius
                            = (List<Integer>) context.getSessionData(stagePrefix + CK.S_REACH_LOCATIONS_RADIUS);
                    if (radius != null) {
                        for (final Integer i : radius) {
                            text.append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(i).append("\n");
                        }
                    }
                    return text.toString();
                }
            case 3:
                if (context.getSessionData(stagePrefix + CK.S_REACH_LOCATIONS_NAMES) == null) {
                    return ChatColor.GRAY + " (" + Lang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder("\n");
                    final List<String> names
                            = (List<String>) context.getSessionData(stagePrefix + CK.S_REACH_LOCATIONS_NAMES);
                    if (names != null) {
                        for (final String s : names) {
                            text.append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s).append("\n");
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
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenNumericPromptEvent event
                        = new QuestsEditorPostOpenNumericPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }

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
                    final Map<UUID, Block> temp = plugin.getQuestFactory().getSelectedReachLocations();
                    temp.put(((Player) context.getForWhom()).getUniqueId(), null);
                    plugin.getQuestFactory().setSelectedReachLocations(temp);
                    return new ReachLocationPrompt(context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("consoleError"));
                    return new ReachListPrompt(context);
                }
                
            case 2:
                if (context.getSessionData(stagePrefix + CK.S_REACH_LOCATIONS) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNoLocations"));
                    return new ReachListPrompt(context);
                } else {
                    return new ReachRadiiPrompt(context);
                }
            case 3:
                if (context.getSessionData(stagePrefix + CK.S_REACH_LOCATIONS) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNoLocations"));
                    return new ReachListPrompt(context);
                } else {
                    return new ReachNamesPrompt(context);
                }
            case 4:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorObjectiveCleared"));
                context.setSessionData(stagePrefix + CK.S_REACH_LOCATIONS, null);
                context.setSessionData(stagePrefix + CK.S_REACH_LOCATIONS_RADIUS, null);
                context.setSessionData(stagePrefix + CK.S_REACH_LOCATIONS_NAMES, null);
                return new ReachListPrompt(context);
            case 5:
                final int one;
                final int two;
                final int three;
                final List<String> locations
                        = (List<String>) context.getSessionData(stagePrefix + CK.S_REACH_LOCATIONS);
                final List<Integer> radius
                        = (List<Integer>) context.getSessionData(stagePrefix + CK.S_REACH_LOCATIONS_RADIUS);
                final List<String> names
                        = (List<String>) context.getSessionData(stagePrefix + CK.S_REACH_LOCATIONS_NAMES);
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
                    return new StageMainPrompt(stageNum, context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                    return new ReachListPrompt(context);
                }
            default:
                return new StageMainPrompt(stageNum, context);
            }
        }
    }

    public class ReachLocationPrompt extends QuestsEditorStringPrompt {
        
        public ReachLocationPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorReachLocationPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            final Player player = (Player) context.getForWhom();
            if (input != null && input.equalsIgnoreCase(Lang.get("cmdAdd"))) {
                final Block block = plugin.getQuestFactory().getSelectedReachLocations().get(player.getUniqueId());
                if (block != null) {
                    final Location loc = block.getLocation();
                    final LinkedList<String> locations;
                    if (context.getSessionData(stagePrefix + CK.S_REACH_LOCATIONS) != null) {
                        locations = (LinkedList<String>) context.getSessionData(stagePrefix + CK.S_REACH_LOCATIONS);
                    } else {
                        locations = new LinkedList<>();
                    }
                    if (locations != null) {
                        locations.add(ConfigUtil.getLocationInfo(loc));
                    }
                    context.setSessionData(stagePrefix + CK.S_REACH_LOCATIONS, locations);
                    final Map<UUID, Block> temp = plugin.getQuestFactory().getSelectedReachLocations();
                    temp.remove(player.getUniqueId());
                    plugin.getQuestFactory().setSelectedReachLocations(temp);
                } else {
                    player.sendMessage(ChatColor.RED + Lang.get("stageEditorNoBlockSelected"));
                    return new ReachLocationPrompt(context);
                }
                return new ReachListPrompt(context);
            } else if (input != null && input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                final Map<UUID, Block> temp = plugin.getQuestFactory().getSelectedReachLocations();
                temp.remove(player.getUniqueId());
                plugin.getQuestFactory().setSelectedReachLocations(temp);
                return new ReachListPrompt(context);
            } else {
                return new ReachLocationPrompt(context);
            }
        }
    }

    public class ReachRadiiPrompt extends QuestsEditorStringPrompt {
        
        public ReachRadiiPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorReachLocationRadiiPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            final Player player = (Player) context.getForWhom();
            if (input != null && !input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                final LinkedList<Integer> radii = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    try {
                        final int i = Integer.parseInt(s);
                        if (i < 1) {
                            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidMinimum")
                                    .replace("<number>", "1"));
                            return new ReachRadiiPrompt(context);
                        }
                        radii.add(i);
                    } catch (final NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + Lang.get("reqNotANumber").replace("<input>", input));
                        return new ReachRadiiPrompt(context);
                    }
                }
                context.setSessionData(stagePrefix + CK.S_REACH_LOCATIONS_RADIUS, radii);
            }
            return new ReachListPrompt(context);
        }
    }

    public class ReachNamesPrompt extends QuestsEditorStringPrompt {
        
        public ReachNamesPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorReachLocationNamesPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input != null && !input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                final LinkedList<String> locNames = new LinkedList<>(Arrays.asList(input.split(Lang.get("charSemi"))));
                context.setSessionData(stagePrefix + CK.S_REACH_LOCATIONS_NAMES, locNames);
            }
            return new ReachListPrompt(context);
        }
    }

    public class PasswordListPrompt extends QuestsEditorNumericPrompt {

        public PasswordListPrompt(final ConversationContext context) {
            super(context);
        }
        
        private final int size = 4;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("stageEditorPassword");
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
                return ChatColor.YELLOW + Lang.get("stageEditorAddPasswordDisplay");
            case 2:
                return ChatColor.YELLOW + Lang.get("stageEditorAddPasswordPhrases");
            case 3:
                return ChatColor.RED + Lang.get("clear");
            case 4:
                return ChatColor.GREEN + Lang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                if (context.getSessionData(stagePrefix + CK.S_PASSWORD_DISPLAYS) == null) {
                    return ChatColor.GRAY + " (" + Lang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder("\n");
                    final List<String> displays = (List<String>) context.getSessionData(stagePrefix
                            + CK.S_PASSWORD_DISPLAYS);
                    if (displays != null) {
                        for (final String display : displays) {
                            text.append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(display)
                                    .append("\n");
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (context.getSessionData(stagePrefix + CK.S_PASSWORD_PHRASES) == null) {
                    return ChatColor.GRAY + " (" + Lang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder("\n");
                    final List<String> phrases = (List<String>) context.getSessionData(stagePrefix
                            + CK.S_PASSWORD_PHRASES);
                    if (phrases != null) {
                        for (final String phrase : phrases) {
                            text.append(ChatColor.GRAY).append("     - ").append(ChatColor.DARK_AQUA).append(phrase)
                                    .append("\n");
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
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenNumericPromptEvent event
                        = new QuestsEditorPostOpenNumericPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }

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
                return new PasswordDisplayPrompt(context);
            case 2:
                if (context.getSessionData(stagePrefix + CK.S_PASSWORD_DISPLAYS) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorMustSetPasswordDisplays"));
                    return new PasswordListPrompt(context);
                } else {
                    return new PasswordPhrasePrompt(context);
                }
            case 3:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorObjectiveCleared"));
                context.setSessionData(stagePrefix + CK.S_PASSWORD_DISPLAYS, null);
                context.setSessionData(stagePrefix + CK.S_PASSWORD_PHRASES, null);
                return new PasswordListPrompt(context);
            case 4:
                final int one;
                final int two;
                final List<String> displays
                        = (List<String>) context.getSessionData(stagePrefix + CK.S_PASSWORD_DISPLAYS);
                final List<String> phrases = (List<String>) context.getSessionData(stagePrefix + CK.S_PASSWORD_PHRASES);
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
                    return new StageMainPrompt(stageNum, context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                    return new PasswordListPrompt(context);
                }
            default:
                return new StageMainPrompt(stageNum, context);
            }
        }
    }

    public class PasswordDisplayPrompt extends QuestsEditorStringPrompt {

        public PasswordDisplayPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorPasswordDisplayPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            return ChatColor.YELLOW + getQueryText(context) + "\n";
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input != null && !input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                if (context.getSessionData(stagePrefix + CK.S_PASSWORD_DISPLAYS) != null) {
                    @SuppressWarnings("unchecked")
                    final List<String> displays = (List<String>) context.getSessionData(stagePrefix
                            + CK.S_PASSWORD_DISPLAYS);
                    if (displays != null) {
                        displays.addAll(Arrays.asList(input.split(Lang.get("charSemi"))));
                    }
                    context.setSessionData(stagePrefix + CK.S_PASSWORD_DISPLAYS, displays);
                } else {
                    final List<String> displays = new LinkedList<>(Arrays.asList(input.split(Lang.get("charSemi"))));
                    context.setSessionData(stagePrefix + CK.S_PASSWORD_DISPLAYS, displays);
                }
            }
            return new PasswordListPrompt(context);
        }
    }

    public class PasswordPhrasePrompt extends QuestsEditorStringPrompt {
        
        public PasswordPhrasePrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorPasswordPhrasePrompt");
        }


        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            return ChatColor.YELLOW + getQueryText(context) + "\n";
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input != null && !input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                if (context.getSessionData(stagePrefix + CK.S_PASSWORD_PHRASES) != null) {
                    @SuppressWarnings("unchecked")
                    final List<String> phrases = (List<String>) context.getSessionData(stagePrefix
                            + CK.S_PASSWORD_PHRASES);
                    if (phrases != null) {
                        phrases.addAll(Arrays.asList(input.split(Lang.get("charSemi"))));
                    }
                    context.setSessionData(stagePrefix + CK.S_PASSWORD_PHRASES, phrases);
                } else {
                    final List<String> phrases = new LinkedList<>(Arrays.asList(input.split(Lang.get("charSemi"))));
                    context.setSessionData(stagePrefix + CK.S_PASSWORD_PHRASES, phrases);
                }
            }
            return new PasswordListPrompt(context);
        }
    }

    public class ActionListPrompt extends QuestsEditorNumericPrompt {

        public ActionListPrompt(final ConversationContext context) {
            super(context);
        }
        
        private final int size = 8;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("stageEditorStageEvents");
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
                return ChatColor.YELLOW + Lang.get("stageEditorStartEvent");
            case 2:
                return ChatColor.YELLOW + Lang.get("stageEditorFinishEvent");
            case 3:
                return ChatColor.YELLOW + Lang.get("stageEditorFailEvent");
            case 4:
                return ChatColor.YELLOW + Lang.get("stageEditorDeathEvent");
            case 5:
                return ChatColor.YELLOW + Lang.get("stageEditorDisconnectEvent");
            case 6:
                return ChatColor.YELLOW + Lang.get("stageEditorChatEvents");
            case 7:
                return ChatColor.YELLOW + Lang.get("stageEditorCommandEvents");
            case 8:
                return ChatColor.GREEN + Lang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                if (context.getSessionData(stagePrefix + CK.S_START_EVENT) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    return ChatColor.YELLOW + "(" + ChatColor.AQUA + context.getSessionData(stagePrefix
                            + CK.S_START_EVENT) + ChatColor.YELLOW + ")\n";
                }
            case 2:
                if (context.getSessionData(stagePrefix + CK.S_FINISH_EVENT) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    return ChatColor.YELLOW + "(" + ChatColor.AQUA + context.getSessionData(stagePrefix
                            + CK.S_FINISH_EVENT) + ChatColor.YELLOW + ")\n";
                }
            case 3:
                if (context.getSessionData(stagePrefix + CK.S_FAIL_EVENT) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    return ChatColor.YELLOW + "(" + ChatColor.AQUA + context.getSessionData(stagePrefix
                            + CK.S_FAIL_EVENT) + ChatColor.YELLOW + ")\n";
                }  
            case 4:
                if (context.getSessionData(stagePrefix + CK.S_DEATH_EVENT) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    return ChatColor.YELLOW + "(" + ChatColor.AQUA + context.getSessionData(stagePrefix
                            + CK.S_DEATH_EVENT) + ChatColor.YELLOW + ")\n";
                }
            case 5:
                if (context.getSessionData(stagePrefix + CK.S_DISCONNECT_EVENT) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    return ChatColor.YELLOW + "(" + ChatColor.AQUA + context.getSessionData(stagePrefix
                            + CK.S_DISCONNECT_EVENT) + ChatColor.YELLOW + ")\n";
                }
            case 6:
                if (context.getSessionData(stagePrefix + CK.S_CHAT_EVENTS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder("\n");
                    final LinkedList<String> chatEvents = (LinkedList<String>) context.getSessionData(stagePrefix 
                            + CK.S_CHAT_EVENTS);
                    final LinkedList<String> chatEventTriggers 
                            = (LinkedList<String>) context.getSessionData(stagePrefix + CK.S_CHAT_EVENT_TRIGGERS);
                    if (chatEvents != null && chatEventTriggers != null) {
                        for (final String event : chatEvents) {
                            text.append(ChatColor.AQUA).append("     - ").append(event).append(ChatColor.BLUE)
                                    .append(" (").append(Lang.get("stageEditorTriggeredBy")).append(": \"")
                                    .append(chatEventTriggers.get(chatEvents.indexOf(event))).append("\")\n");
                        }
                    }
                    return text.toString();
                }
            case 7:
                if (context.getSessionData(stagePrefix + CK.S_COMMAND_EVENTS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder("\n");
                    final LinkedList<String> commandEvents 
                            = (LinkedList<String>) context.getSessionData(stagePrefix + CK.S_COMMAND_EVENTS);
                    final LinkedList<String> commandEventTriggers 
                            = (LinkedList<String>) context.getSessionData(stagePrefix + CK.S_COMMAND_EVENT_TRIGGERS);
                    if (commandEvents != null && commandEventTriggers != null) {
                        for (final String event : commandEvents) {
                            text.append(ChatColor.AQUA).append("     - ").append(event).append(ChatColor.BLUE)
                                    .append(" (").append(Lang.get("stageEditorTriggeredBy")).append(": \"")
                                    .append(commandEventTriggers.get(commandEvents.indexOf(event))).append("\")\n");
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
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenNumericPromptEvent event
                        = new QuestsEditorPostOpenNumericPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }

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
                return new StartActionPrompt(context);
            case 2:
                return new FinishActionPrompt(context);
            case 3:
                return new FailActionPrompt(context);
            case 4:
                return new DeathActionPrompt(context);
            case 5:
                return new DisconnectActionPrompt(context);
            case 6:
                return new ChatActionPrompt(context);
            case 7:
                return new CommandActionPrompt(context);
            case 8:
                return new StageMainPrompt(stageNum, context);
            default:
                return new ActionListPrompt(context);
            }
        }
    }

    public class StartActionPrompt extends QuestsEditorStringPrompt {
        
        public StartActionPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("stageEditorStartEvent");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorEventsPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle(context) + " -\n");
            if (plugin.getLoadedActions().isEmpty()) {
                text.append(ChatColor.RED).append("- ").append(Lang.get("none")).append("\n");
            } else {
                for (final Action a : plugin.getLoadedActions()) {
                    text.append(ChatColor.GREEN).append("- ").append(a.getName()).append("\n");
                }
            }
            return text.toString() + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            final Player player = (Player) context.getForWhom();
            if (input != null && !input.equalsIgnoreCase(Lang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                Action found = null;
                for (final Action a : plugin.getLoadedActions()) {
                    if (a.getName().equalsIgnoreCase(input)) {
                        found = a;
                        break;
                    }
                }
                if (found == null) {
                    player.sendMessage(ChatColor.RED + input + ChatColor.YELLOW + " " 
                            + Lang.get("stageEditorInvalidEvent"));
                    return new StartActionPrompt(context);
                } else {
                    context.setSessionData(stagePrefix + CK.S_START_EVENT, found.getName());
                    return new ActionListPrompt(context);
                }
            } else if (input != null && input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                return new ActionListPrompt(context);
            } else if (input != null && input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + CK.S_START_EVENT, null);
                player.sendMessage(ChatColor.YELLOW + Lang.get("stageEditorEventCleared"));
                return new ActionListPrompt(context);
            } else {
                return new StartActionPrompt(context);
            }
        }
    }

    public class FinishActionPrompt extends QuestsEditorStringPrompt {
        
        public FinishActionPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("stageEditorFinishEvent");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorEventsPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle(context) + " -\n");
            if (plugin.getLoadedActions().isEmpty()) {
                text.append(ChatColor.RED).append("- ").append(Lang.get("none"));
            } else {
                for (final Action a : plugin.getLoadedActions()) {
                    text.append(ChatColor.GREEN).append("- ").append(a.getName()).append("\n");
                }
            }
            return text.toString() + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            final Player player = (Player) context.getForWhom();
            if (input != null && !input.equalsIgnoreCase(Lang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                Action found = null;
                for (final Action a : plugin.getLoadedActions()) {
                    if (a.getName().equalsIgnoreCase(input)) {
                        found = a;
                        break;
                    }
                }
                if (found == null) {
                    player.sendMessage(ChatColor.RED + input + ChatColor.YELLOW + " " 
                            + Lang.get("stageEditorInvalidEvent"));
                    return new FinishActionPrompt(context);
                } else {
                    context.setSessionData(stagePrefix + CK.S_FINISH_EVENT, found.getName());
                    return new ActionListPrompt(context);
                }
            } else if (input != null && input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                return new ActionListPrompt(context);
            } else if (input != null && input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + CK.S_FINISH_EVENT, null);
                player.sendMessage(ChatColor.YELLOW + Lang.get("stageEditorEventCleared"));
                return new ActionListPrompt(context);
            } else {
                return new FinishActionPrompt(context);
            }
        }
    }
    
    public class FailActionPrompt extends QuestsEditorStringPrompt {
        
        public FailActionPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("stageEditorFailEvent");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorEventsPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle(context) + " -\n");
            if (plugin.getLoadedActions().isEmpty()) {
                text.append(ChatColor.RED).append("- ").append(Lang.get("none"));
            } else {
                for (final Action a : plugin.getLoadedActions()) {
                    text.append(ChatColor.GREEN).append("- ").append(a.getName()).append("\n");
                }
            }
            return text.toString() + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            final Player player = (Player) context.getForWhom();
            if (input != null && !input.equalsIgnoreCase(Lang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                Action found = null;
                for (final Action a : plugin.getLoadedActions()) {
                    if (a.getName().equalsIgnoreCase(input)) {
                        found = a;
                        break;
                    }
                }
                if (found == null) {
                    player.sendMessage(ChatColor.RED + input + ChatColor.YELLOW + " " 
                            + Lang.get("stageEditorInvalidEvent"));
                    return new FailActionPrompt(context);
                } else {
                    context.setSessionData(stagePrefix + CK.S_FAIL_EVENT, found.getName());
                    return new ActionListPrompt(context);
                }
            } else if (input != null && input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                return new ActionListPrompt(context);
            } else if (input != null && input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + CK.S_FAIL_EVENT, null);
                player.sendMessage(ChatColor.YELLOW + Lang.get("stageEditorEventCleared"));
                return new ActionListPrompt(context);
            } else {
                return new FailActionPrompt(context);
            }
        }
    }

    public class DeathActionPrompt extends QuestsEditorStringPrompt {
        
        public DeathActionPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("stageEditorDeathEvent");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorEventsPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle(context) + " -\n");
            if (plugin.getLoadedActions().isEmpty()) {
                text.append(ChatColor.RED).append("- ").append(Lang.get("none")).append("\n");
            } else {
                for (final Action a : plugin.getLoadedActions()) {
                    text.append(ChatColor.GREEN).append("- ").append(a.getName()).append("\n");
                }
            }
            return text.toString() + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            final Player player = (Player) context.getForWhom();
            if (input != null && !input.equalsIgnoreCase(Lang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                Action found = null;
                for (final Action a : plugin.getLoadedActions()) {
                    if (a.getName().equalsIgnoreCase(input)) {
                        found = a;
                        break;
                    }
                }
                if (found == null) {
                    player.sendMessage(ChatColor.RED + input + ChatColor.YELLOW + " " 
                            + Lang.get("stageEditorInvalidEvent"));
                    return new DeathActionPrompt(context);
                } else {
                    context.setSessionData(stagePrefix + CK.S_DEATH_EVENT, found.getName());
                    return new ActionListPrompt(context);
                }
            } else if (input != null && input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                return new ActionListPrompt(context);
            } else if (input != null && input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + CK.S_DEATH_EVENT, null);
                player.sendMessage(ChatColor.YELLOW + Lang.get("stageEditorEventCleared"));
                return new ActionListPrompt(context);
            } else {
                return new DeathActionPrompt(context);
            }
        }
    }

    public class DisconnectActionPrompt extends QuestsEditorStringPrompt {
        
        public DisconnectActionPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("stageEditorDisconnectEvent");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorEventsPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle(context) + " -\n");
            if (plugin.getLoadedActions().isEmpty()) {
                text.append(ChatColor.RED).append("- ").append(Lang.get("none"));
            } else {
                for (final Action a : plugin.getLoadedActions()) {
                    text.append(ChatColor.GREEN).append("- ").append(a.getName()).append("\n");
                }
            }
            return text.toString() + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            final Player player = (Player) context.getForWhom();
            if (input != null && !input.equalsIgnoreCase(Lang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                Action found = null;
                for (final Action a : plugin.getLoadedActions()) {
                    if (a.getName().equalsIgnoreCase(input)) {
                        found = a;
                        break;
                    }
                }
                if (found == null) {
                    player.sendMessage(ChatColor.RED + input + ChatColor.YELLOW + " " 
                            + Lang.get("stageEditorInvalidEvent"));
                    return new DisconnectActionPrompt(context);
                } else {
                    context.setSessionData(stagePrefix + CK.S_DISCONNECT_EVENT, found.getName());
                    return new ActionListPrompt(context);
                }
            } else if (input != null && input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                return new ActionListPrompt(context);
            } else if (input != null && input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + CK.S_DISCONNECT_EVENT, null);
                player.sendMessage(ChatColor.YELLOW + Lang.get("stageEditorEventCleared"));
                return new ActionListPrompt(context);
            } else {
                return new DisconnectActionPrompt(context);
            }
        }
    }

    public class ChatActionPrompt extends QuestsEditorStringPrompt {
        
        public ChatActionPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("stageEditorChatEvents");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorChatEventsPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle(context) + " -\n");
            if (plugin.getLoadedActions().isEmpty()) {
                text.append(ChatColor.RED).append("- ").append(Lang.get("none"));
            } else {
                for (final Action a : plugin.getLoadedActions()) {
                    text.append(ChatColor.GREEN).append("- ").append(a.getName()).append("\n");
                }
            }
            return text.toString() + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            final Player player = (Player) context.getForWhom();
            if (input != null && !input.equalsIgnoreCase(Lang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                Action found = null;
                for (final Action a : plugin.getLoadedActions()) {
                    if (a.getName().equalsIgnoreCase(input)) {
                        found = a;
                        break;
                    }
                }
                if (found == null) {
                    player.sendMessage(ChatColor.RED + input + ChatColor.YELLOW + " " 
                            + Lang.get("stageEditorInvalidEvent"));
                    return new ChatActionPrompt(context);
                } else {
                    context.setSessionData(stagePrefix + CK.S_CHAT_TEMP_EVENT, found.getName());
                    return new ChatActionTriggerPrompt(context);
                }
            } else if (input != null && input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                return new ActionListPrompt(context);
            } else if (input != null && input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + CK.S_CHAT_EVENTS, null);
                context.setSessionData(stagePrefix + CK.S_CHAT_EVENT_TRIGGERS, null);
                player.sendMessage(ChatColor.YELLOW + Lang.get("stageEditorChatEventsCleared"));
                return new ActionListPrompt(context);
            } else {
                return new ChatActionPrompt(context);
            }
        }
    }

    public class ChatActionTriggerPrompt extends QuestsEditorStringPrompt {
        
        public ChatActionTriggerPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("stageEditorChatTrigger");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            final String tempEvent = (String) context.getSessionData(stagePrefix + CK.S_CHAT_TEMP_EVENT);
            if (tempEvent != null) {
                return Lang.get("stageEditorChatEventsTriggerPrompt").replace("<event>", tempEvent)
                        .replace("<action>", tempEvent);
            } else {
                return Lang.get("stageEditorChatEventsTriggerPrompt");
            }
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            final String tempEvent = (String) context.getSessionData(stagePrefix + CK.S_CHAT_TEMP_EVENT);
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
            if (input != null && !input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                if (context.getSessionData(stagePrefix + CK.S_CHAT_EVENTS) == null) {
                    final LinkedList<String> chatEvents = new LinkedList<>();
                    final LinkedList<String> chatEventTriggers = new LinkedList<>();
                    final String event = (String) context.getSessionData(stagePrefix + CK.S_CHAT_TEMP_EVENT);
                    chatEvents.add(event);
                    chatEventTriggers.add(input.trim());
                    context.setSessionData(stagePrefix + CK.S_CHAT_EVENTS, chatEvents);
                    context.setSessionData(stagePrefix + CK.S_CHAT_EVENT_TRIGGERS, chatEventTriggers);
                    return new ActionListPrompt(context);
                } else {
                    final LinkedList<String> chatEvents 
                            = (LinkedList<String>) context.getSessionData(stagePrefix + CK.S_CHAT_EVENTS);
                    final LinkedList<String> chatEventTriggers 
                            = (LinkedList<String>) context.getSessionData(stagePrefix + CK.S_CHAT_EVENT_TRIGGERS);
                    final String event = (String) context.getSessionData(stagePrefix + CK.S_CHAT_TEMP_EVENT);
                    if (chatEvents != null && chatEventTriggers != null) {
                        chatEvents.add(event);
                        chatEventTriggers.add(input.trim());
                        context.setSessionData(stagePrefix + CK.S_CHAT_EVENTS, chatEvents);
                        context.setSessionData(stagePrefix + CK.S_CHAT_EVENT_TRIGGERS, chatEventTriggers);
                    }
                    return new ActionListPrompt(context);
                }
            } else if (input != null && input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                return new ActionListPrompt(context);
            } else {
                return new ChatActionTriggerPrompt(context);
            }
        }
    }
    
    public class CommandActionPrompt extends QuestsEditorStringPrompt {
        
        public CommandActionPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("stageEditorCommandEvents");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorCommandEventsPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }

            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle(context) + " -\n");
            if (plugin.getLoadedActions().isEmpty()) {
                text.append(ChatColor.RED).append("- ").append(Lang.get("none"));
            } else {
                for (final Action a : plugin.getLoadedActions()) {
                    text.append(ChatColor.GREEN).append("- ").append(a.getName()).append("\n");
                }
            }
            return text.toString() + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            final Player player = (Player) context.getForWhom();
            if (input != null && !input.equalsIgnoreCase(Lang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                Action found = null;
                for (final Action a : plugin.getLoadedActions()) {
                    if (a.getName().equalsIgnoreCase(input)) {
                        found = a;
                        break;
                    }
                }
                if (found == null) {
                    player.sendMessage(ChatColor.RED + input + ChatColor.YELLOW + " " 
                            + Lang.get("stageEditorInvalidEvent"));
                    return new CommandActionPrompt(context);
                } else {
                    context.setSessionData(stagePrefix + CK.S_COMMAND_TEMP_EVENT, found.getName());
                    return new CommandActionTriggerPrompt(context);
                }
            } else if (input != null && input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                return new ActionListPrompt(context);
            } else if (input != null && input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + CK.S_COMMAND_EVENTS, null);
                context.setSessionData(stagePrefix + CK.S_COMMAND_EVENT_TRIGGERS, null);
                player.sendMessage(ChatColor.YELLOW + Lang.get("stageEditorCommandEventsCleared"));
                return new ActionListPrompt(context);
            } else {
                return new CommandActionPrompt(context);
            }
        }
    }

    public class CommandActionTriggerPrompt extends QuestsEditorStringPrompt {
        
        public CommandActionTriggerPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("stageEditorCommandTrigger");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            final String tempEvent = (String) context.getSessionData(stagePrefix + CK.S_COMMAND_TEMP_EVENT);
            if (tempEvent != null) {
                return Lang.get("stageEditorCommandEventsTriggerPrompt").replace("<event>", tempEvent)
                        .replace("<action>", tempEvent);
            } else {
                return Lang.get("stageEditorCommandEventsTriggerPrompt");
            }
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }

            
            final String tempEvent = (String) context.getSessionData(stagePrefix + CK.S_COMMAND_TEMP_EVENT);
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
            if (input != null && !input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                if (context.getSessionData(stagePrefix + CK.S_COMMAND_EVENTS) == null) {
                    final LinkedList<String> commandEvents = new LinkedList<>();
                    final LinkedList<String> commandEventTriggers = new LinkedList<>();
                    final String event = (String) context.getSessionData(stagePrefix + CK.S_COMMAND_TEMP_EVENT);
                    commandEvents.add(event);
                    commandEventTriggers.add(input.trim());
                    context.setSessionData(stagePrefix + CK.S_COMMAND_EVENTS, commandEvents);
                    context.setSessionData(stagePrefix + CK.S_COMMAND_EVENT_TRIGGERS, commandEventTriggers);
                    return new ActionListPrompt(context);
                } else {
                    final LinkedList<String> commandEvents 
                            = (LinkedList<String>) context.getSessionData(stagePrefix + CK.S_COMMAND_EVENTS);
                    final LinkedList<String> commandEventTriggers 
                            = (LinkedList<String>) context.getSessionData(stagePrefix + CK.S_COMMAND_EVENT_TRIGGERS);
                    final String event = (String) context.getSessionData(stagePrefix + CK.S_COMMAND_TEMP_EVENT);
                    if (commandEvents != null && commandEventTriggers != null) {
                        commandEvents.add(event);
                        commandEventTriggers.add(input.trim());
                        context.setSessionData(stagePrefix + CK.S_COMMAND_EVENTS, commandEvents);
                        context.setSessionData(stagePrefix + CK.S_COMMAND_EVENT_TRIGGERS, commandEventTriggers);
                    }
                    return new ActionListPrompt(context);
                }
            } else if (input != null && input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                return new ActionListPrompt(context);
            } else {
                return new CommandActionTriggerPrompt(context);
            }
        }
    }

    public class ConditionListPrompt extends QuestsEditorStringPrompt {

        public ConditionListPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("stageEditorConditions");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorConditionsPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }

            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle(context) + " -\n");
            if (plugin.getLoadedConditions().isEmpty()) {
                text.append(ChatColor.RED).append("- ").append(Lang.get("none")).append("\n");
            } else {
                for (final Condition c : plugin.getLoadedConditions()) {
                    text.append(ChatColor.GREEN).append("- ").append(c.getName()).append("\n");
                }
            }
            return text.toString() + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            final Player player = (Player) context.getForWhom();
            if (input != null && !input.equalsIgnoreCase(Lang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                Condition found = null;
                for (final Condition c : plugin.getLoadedConditions()) {
                    if (c.getName().equalsIgnoreCase(input)) {
                        found = c;
                        break;
                    }
                }
                if (found == null) {
                    player.sendMessage(ChatColor.RED + input + ChatColor.YELLOW + " " 
                            + Lang.get("stageEditorInvalidCondition"));
                    return new ConditionListPrompt(context);
                } else {
                    context.setSessionData(stagePrefix + CK.S_CONDITION, found.getName());
                    return new StageMainPrompt(stageNum, context);
                }
            } else if (input != null && input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                return new StageMainPrompt(stageNum, context);
            } else if (input != null && input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + CK.S_CONDITION, null);
                player.sendMessage(ChatColor.YELLOW + Lang.get("stageEditorConditionCleared"));
                return new StageMainPrompt(stageNum, context);
            } else {
                return new ConditionListPrompt(context);
            }
        }
    }
    
    public class DelayPrompt extends QuestsEditorStringPrompt {
        
        public DelayPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("timePrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event 
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            if (context.getPlugin() != null) {
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input != null) {
                if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                    return new StageMainPrompt(stageNum, context);
                }
                if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                    context.setSessionData(stagePrefix + CK.S_DELAY, null);
                    context.getForWhom().sendRawMessage(ChatColor.GREEN + Lang.get("stageEditorDelayCleared"));
                    return new StageMainPrompt(stageNum, context);
                }
                final long stageDelay;
                try {
                    final int i = Integer.parseInt(input);
                    stageDelay = i * 1000L;
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqNotANumber").replace("<input>", input));
                    return new DelayPrompt(context);
                }
                if (stageDelay < 1000) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidMinimum").replace("<number>", "1"));
                    return new DelayPrompt(context);
                } else {
                    context.setSessionData(stagePrefix + CK.S_DELAY, stageDelay);
                    return new StageMainPrompt(stageNum, context);
                }
            }
            return new StageMainPrompt(stageNum, context);
        }
    }

    public class DelayMessagePrompt extends QuestsEditorStringPrompt {
        
        public DelayMessagePrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorDelayMessagePrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input != null && !input.equalsIgnoreCase(Lang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + CK.S_DELAY_MESSAGE, input);
                return new StageMainPrompt(stageNum, context);
            } else if (input != null && input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + CK.S_DELAY_MESSAGE, null);
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorMessageCleared"));
                return new StageMainPrompt(stageNum, context);
            } else {
                return new DelayMessagePrompt(context);
            }
        }
    }
    

    public class StartMessagePrompt extends QuestsEditorStringPrompt {
        
        public StartMessagePrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorStartMessagePrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input != null && !input.equalsIgnoreCase(Lang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + CK.S_START_MESSAGE, input);
                return new StageMainPrompt(stageNum, context);
            } else if (input != null && input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + CK.S_START_MESSAGE, null);
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorMessageCleared"));
                return new StageMainPrompt(stageNum, context);
            } else {
                return new StageMainPrompt(stageNum, context);
            }
        }
    }

    public class CompleteMessagePrompt extends QuestsEditorStringPrompt {
        
        public CompleteMessagePrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorCompleteMessagePrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input != null && !input.equalsIgnoreCase(Lang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + CK.S_COMPLETE_MESSAGE, input);
                return new StageMainPrompt(stageNum, context);
            } else if (input != null && input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + CK.S_COMPLETE_MESSAGE, null);
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorMessageCleared"));
                return new StageMainPrompt(stageNum, context);
            } else {
                return new StageMainPrompt(stageNum, context);
            }
        }
    }

    public class StageDeletePrompt extends QuestsEditorStringPrompt {
        
        public StageDeletePrompt(final ConversationContext context) {
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
        
        public String getSelectionText(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                return ChatColor.GREEN + Lang.get("yesWord");
            case 2:
                return ChatColor.RED + Lang.get("noWord");
            default:
                return null;
            }
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("confirmDelete");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            final StringBuilder text = new StringBuilder(ChatColor.YELLOW + getQueryText(context) + " ("
                    + ChatColor.RED + Lang.get("stageEditorStage") + " " + stageNum + ChatColor.YELLOW + ")\n"
                    + ChatColor.GOLD + "(" + Lang.get("stageEditorConfirmStageNote") + ")\n");
            for (int i = 1; i <= size; i++) {
                text.append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i).append(ChatColor.RESET)
                        .append(" - ").append(getSelectionText(context, i)).append("\n");
            }
            return QuestsNumericPrompt.sendClickableSelection(text.toString(), context.getForWhom());
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input != null && (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(Lang.get("yesWord")))) {
                new StageMenuPrompt(context).deleteStage(context, stageNum);
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorDeleteSucces"));
                return new StageMenuPrompt(context);
            } else if (input != null && (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(Lang.get("noWord")))) {
                return new StageMainPrompt(stageNum, context);
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidOption"));
                return new StageDeletePrompt(context);
            }
        }
    }

    public class CustomObjectiveModulePrompt extends QuestsEditorStringPrompt {

        public CustomObjectiveModulePrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("stageEditorModules");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorModulePrompt");
        }

        @Override
        public @NotNull String getPromptText(@NotNull final ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }

            final StringBuilder text = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle(context) + "\n");
            if (plugin.getCustomObjectives().isEmpty()) {
                text.append(ChatColor.DARK_AQUA).append(ChatColor.UNDERLINE)
                        .append("https://pikamug.gitbook.io/quests/casual/modules").append(ChatColor.RESET)
                        .append("\n");
                text.append(ChatColor.DARK_PURPLE).append("(").append(Lang.get("stageEditorNoModules")).append(") ");
            } else {
                for (final String name : plugin.getCustomObjectives().stream().map(CustomObjective::getModuleName)
                        .collect(Collectors.toCollection(TreeSet::new))) {
                    text.append(ChatColor.DARK_PURPLE).append("  - ").append(name).append("\n");
                }
            }
            return text.toString() + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(@NotNull final ConversationContext context, @Nullable final String input) {
            if (input != null && !input.equalsIgnoreCase(Lang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(Lang.get("cmdClear"))) {
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
                    return new CustomObjectivesPrompt(found, context);
                }
            } else if (input != null && input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                return new StageMainPrompt(stageNum, context);
            } else if (input != null && input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES, null);
                context.setSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES_DATA, null);
                context.setSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES_DATA_TEMP, null);
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorCustomCleared"));
            }
            context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorModuleNotFound"));
            return new CustomObjectiveModulePrompt(context);
        }
    }

    public class CustomObjectivesPrompt extends QuestsEditorStringPrompt {

        private final String moduleName;

        public CustomObjectivesPrompt(final String moduleName, final ConversationContext context) {
            super(context);
            this.moduleName = moduleName;
        }

        public String getModuleName() {
            return moduleName;
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("stageEditorCustom");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorCustomPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            final StringBuilder text = new StringBuilder(ChatColor.LIGHT_PURPLE + "- " + getTitle(context) + " -\n");
            if (plugin.getCustomObjectives().isEmpty()) {
                text.append(ChatColor.DARK_AQUA).append(ChatColor.UNDERLINE)
                        .append("https://pikamug.gitbook.io/quests/casual/modules\n");
                text.append(ChatColor.DARK_PURPLE).append("(").append(Lang.get("stageEditorNoModules")).append(") ");
            } else {
                for (final CustomObjective co : plugin.getCustomObjectives()) {
                    if (co.getModuleName().equals(moduleName)) {
                        text.append(ChatColor.DARK_PURPLE).append("  - ").append(co.getName()).append("\n");
                    }
                }
            }
            return text.toString() + ChatColor.YELLOW + getQueryText(context);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input != null && !input.equalsIgnoreCase(Lang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(Lang.get("cmdClear"))) {
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
                    if (context.getSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES) != null) {
                        // The custom objective may already have been added, so let's check that
                        final LinkedList<String> list = (LinkedList<String>) context.getSessionData(stagePrefix 
                                + CK.S_CUSTOM_OBJECTIVES);
                        final LinkedList<Entry<String, Object>> dataMapList
                                = (LinkedList<Entry<String, Object>>) context.getSessionData(stagePrefix 
                                + CK.S_CUSTOM_OBJECTIVES_DATA);
                        final LinkedList<Integer> countList = (LinkedList<Integer>) context.getSessionData(stagePrefix 
                                + CK.S_CUSTOM_OBJECTIVES_COUNT);
                        if (list != null && !list.contains(found.getName()) && dataMapList != null
                                && countList != null) {
                            // Hasn't been added yet, so let's do it
                            list.add(found.getName());
                            dataMapList.addAll(found.getData());
                            countList.add(-999);
                            context.setSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES, list);
                            context.setSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES_DATA, dataMapList);
                        } else {
                            // Already added, so inform user
                            context.getForWhom().sendRawMessage(ChatColor.YELLOW 
                                    + Lang.get("stageEditorCustomAlreadyAdded"));
                            return new CustomObjectivesPrompt(moduleName, context);
                        }
                    } else {
                        // The custom objective hasn't been added yet, so let's do it
                        final LinkedList<Integer> countList = new LinkedList<>();
                        final LinkedList<Entry<String, Object>> dataMapList = new LinkedList<>(found.getData());
                        countList.add(-999);
                        final LinkedList<String> list = new LinkedList<>();
                        list.add(found.getName());
                        context.setSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES, list);
                        context.setSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES_DATA, dataMapList);
                        context.setSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES_COUNT, countList);
                    }
                    // Send user to the count prompt / custom data prompt if there is any needed
                    if (found.canShowCount()) {
                        return new CustomObjectiveCountPrompt();
                    }
                    if (!found.getData().isEmpty()) {
                        context.setSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES_DATA_DESCRIPTIONS, found
                                .getDescriptions());
                        return new ObjectiveCustomDataListPrompt();
                    }
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorModuleNotFound"));
                    return new CustomObjectivesPrompt(moduleName, context);
                }
            } else if (input != null && input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES, null);
                context.setSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES_DATA, null);
                context.setSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES_DATA_TEMP, null);
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorCustomCleared"));
            }
            return new StageMainPrompt(stageNum, context);
        }
    }

    private class CustomObjectiveCountPrompt extends StringPrompt {

        @Override
        public @NotNull String getPromptText(final ConversationContext context) {
            String text = ChatColor.BOLD + "" + ChatColor.AQUA + "- ";
            @SuppressWarnings("unchecked")
            final
            LinkedList<String> list = (LinkedList<String>) context.getSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES);
            if (list != null && plugin != null) {
                final String objName = list.getLast();
                text += objName + " -\n";
                CustomObjective found = null;
                for (final CustomObjective co : plugin.getCustomObjectives()) {
                    if (co.getName().equals(objName)) {
                        found = co;
                        break;
                    }
                }
                if (found != null) {
                    text += ChatColor.BLUE + found.getCountPrompt() + "\n\n";
                }
            }
            return text;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            try {
                final int num = Integer.parseInt(input);
                final LinkedList<Integer> counts 
                        = (LinkedList<Integer>) context.getSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES_COUNT);
                final LinkedList<String> list = (LinkedList<String>) context.getSessionData(stagePrefix 
                        + CK.S_CUSTOM_OBJECTIVES);
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
                                + CK.S_CUSTOM_OBJECTIVES_DATA_DESCRIPTIONS, found.getDescriptions());
                        return new ObjectiveCustomDataListPrompt();
                    } else {
                        return new StageMainPrompt(stageNum, context);
                    }
                }
            } catch (final NumberFormatException e) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqNotANumber")
                        .replace("<input>", input));
                return new CustomObjectiveCountPrompt();
            }
            return new StageMainPrompt(stageNum, context);
        }
    }

    private class ObjectiveCustomDataListPrompt extends StringPrompt {

        @SuppressWarnings("unchecked")
        @Override
        public @NotNull String getPromptText(final ConversationContext context) {
            final StringBuilder text = new StringBuilder(ChatColor.GOLD + "- ");
            final LinkedList<String> list = (LinkedList<String>) context.getSessionData(stagePrefix 
                    + CK.S_CUSTOM_OBJECTIVES);
            final LinkedList<Entry<String, Object>> dataMapList
                    = (LinkedList<Entry<String, Object>>) context.getSessionData(stagePrefix 
                    + CK.S_CUSTOM_OBJECTIVES_DATA);
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
                                    text.append(ChatColor.GRAY).append(" (").append(Lang.get("noneSet"))
                                            .append(ChatColor.GRAY).append(")\n");
                                }
                                index++;
                            }
                        }
                    }
                }
                text.append(ChatColor.GREEN).append(ChatColor.BOLD).append(index).append(ChatColor.YELLOW)
                        .append(" - ").append(Lang.get("done"));
            }
            return text.toString();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            final LinkedList<String> list = (LinkedList<String>) context.getSessionData(stagePrefix 
                    + CK.S_CUSTOM_OBJECTIVES);
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
                    return new ObjectiveCustomDataListPrompt();
                }
                final LinkedList<Entry<String, Object>> dataMapList = found.getData();

                final int numInput;
                try {
                    numInput = Integer.parseInt(input);
                } catch (final NumberFormatException nfe) {
                    return new ObjectiveCustomDataListPrompt();
                }
                if (numInput < 1 || numInput > dataMapList.size() + 1) {
                    return new ObjectiveCustomDataListPrompt();
                }
                if (numInput < dataMapList.size() + 1) {
                    final LinkedList<String> dataMapKeys = new LinkedList<>();
                    for (final Entry<String, Object> dataMap : dataMapList) {
                        dataMapKeys.add(dataMap.getKey());
                    }
                    // Collections.sort(dataMapKeys);
                    final String selectedKey = dataMapKeys.get(numInput - 1);
                    context.setSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES_DATA_TEMP, selectedKey);
                    return new ObjectiveCustomDataPrompt();
                } else {
                    final LinkedList<Entry<String, Object>> dataMaps
                            = (LinkedList<Entry<String, Object>>) context.getSessionData(stagePrefix
                            + CK.S_CUSTOM_OBJECTIVES_DATA);
                    if (dataMaps != null) {
                        for (final Entry<String, Object> dataMap : dataMaps) {
                            if (dataMap.getValue() == null) {
                                return new ObjectiveCustomDataListPrompt();
                            }
                        }
                    }
                    context.setSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES_DATA_DESCRIPTIONS, null);
                    return new StageMainPrompt(stageNum, context);
                }
            }
            return new StageMainPrompt(stageNum, context);
        }
    }

    private class ObjectiveCustomDataPrompt extends StringPrompt {

        @Override
        public @NotNull String getPromptText(final ConversationContext context) {
            String text = "";
            final String temp = (String) context.getSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES_DATA_TEMP);
            @SuppressWarnings("unchecked")
            final
            Map<String, String> descriptions 
                    = (Map<String, String>) context.getSessionData(stagePrefix 
                    + CK.S_CUSTOM_OBJECTIVES_DATA_DESCRIPTIONS);
            if (descriptions != null && descriptions.get(temp) != null) {
                text += ChatColor.GOLD + descriptions.get(temp) + "\n";
            }
            String msg = Lang.get("stageEditorCustomDataPrompt");
            msg = msg.replace("<data>", ChatColor.BOLD + temp + ChatColor.RESET + ChatColor.YELLOW);
            text += ChatColor.YELLOW + msg;
            return text;
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            @SuppressWarnings("unchecked")
            final
            LinkedList<Entry<String, Object>> dataMapList
                    = (LinkedList<Entry<String, Object>>) context.getSessionData(stagePrefix 
                    + CK.S_CUSTOM_OBJECTIVES_DATA);
            final LinkedList<Entry<String, Object>> promptList = new LinkedList<>();
            final String temp = (String) context.getSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES_DATA_TEMP);
            if (dataMapList != null) {
                for (final Entry<String, Object> dataMap : dataMapList) {
                    if (dataMap.getKey().equals(temp)) {
                        promptList.add(new AbstractMap.SimpleEntry<>(dataMap.getKey(), input));
                    } else {
                        promptList.add(new AbstractMap.SimpleEntry<>(dataMap.getKey(), dataMap.getValue()));
                    }
                }
            }
            context.setSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES_DATA, promptList);
            context.setSessionData(stagePrefix + CK.S_CUSTOM_OBJECTIVES_DATA_TEMP, null);
            return new ObjectiveCustomDataListPrompt();
        }
    }
}
