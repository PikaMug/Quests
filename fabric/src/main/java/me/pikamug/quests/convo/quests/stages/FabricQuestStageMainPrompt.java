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

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.actions.Action;
import me.pikamug.quests.conditions.Condition;
import me.pikamug.quests.convo.FabricQuestsIntegerPrompt;
import me.pikamug.quests.convo.generic.FabricOverridePrompt;
import me.pikamug.quests.convo.quests.FabricQuestsEditorIntegerPrompt;
import me.pikamug.quests.convo.quests.FabricQuestsEditorStringPrompt;
import me.pikamug.quests.convo.quests.objectives.FabricQuestBlocksPrompt;
import me.pikamug.quests.convo.quests.objectives.FabricQuestItemsPrompt;
import me.pikamug.quests.convo.quests.objectives.FabricQuestMobsPrompt;
import me.pikamug.quests.convo.quests.objectives.FabricQuestNpcsPrompt;
import me.pikamug.quests.module.CustomObjective;
import me.pikamug.quests.util.FabricConfigUtil;
import me.pikamug.quests.util.FabricLang;
import me.pikamug.quests.util.FabricMiscUtil;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.SessionData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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

public class FabricQuestStageMainPrompt extends FabricQuestsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final FabricQuestsPlugin plugin;
    private final int stageNum;
    private final String stagePrefix;
    private final String classPrefix;
    private boolean hasObjective = false;
    private final int size = 17;

    public FabricQuestStageMainPrompt(final int stageNum, final UUID uuid) {
        super(uuid);
        this.uuid = uuid;
        this.plugin = FabricQuestsPlugin.getInstance();
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
    public String getTitle() {
        return SessionData.get(uuid, Key.Q_NAME) + " | " + FabricLang.get("stageEditorStage") + " " + stageNum;
    }

    @Override
    public ChatFormatting getNumberColor(final int number) {
        switch (number) {
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        case 6:
        case 7:
        case 8:
            return ChatFormatting.BLUE;
        case 9:
        case 10:
        case 11:
            if (!hasObjective) {
                return ChatFormatting.GRAY;
            } else {
                return ChatFormatting.BLUE;
            }
        case 12:
            if (SessionData.get(uuid, stagePrefix + Key.S_DELAY) == null) {
                return ChatFormatting.GRAY;
            } else {
                return ChatFormatting.BLUE;
            }
        case 13:
            if (SessionData.get(uuid, stagePrefix + Key.S_START_MESSAGE) == null) {
                if (!hasObjective) {
                    return ChatFormatting.GRAY;
                } else {
                    return ChatFormatting.BLUE;
                }
            } else {
                return ChatFormatting.BLUE;
            }
        case 14:
            if (SessionData.get(uuid, stagePrefix + Key.S_COMPLETE_MESSAGE) == null) {
                if (!hasObjective) {
                    return ChatFormatting.GRAY;
                } else {
                    return ChatFormatting.BLUE;
                }
            } else {
                return ChatFormatting.BLUE;
            }
        case 15:
            if (SessionData.get(uuid, stagePrefix + Key.S_OVERRIDE_DISPLAY) == null) {
                if (!hasObjective) {
                    return ChatFormatting.GRAY;
                } else {
                    return ChatFormatting.BLUE;
                }
            } else {
                return ChatFormatting.BLUE;
            }
        case 16:
            return ChatFormatting.RED;
        case 17:
            return ChatFormatting.GREEN;
        default:
            return null;
        }
    }

    @Override
    public String getSelectionText(final int number) {
        switch (number) {
        case 1:
            return ChatFormatting.GOLD + FabricLang.get("stageEditorBlocks");
        case 2:
            return ChatFormatting.GOLD + FabricLang.get("stageEditorItems");
        case 3:
            return ChatFormatting.GOLD + FabricLang.get("stageEditorNPCs");
        case 4:
            return ChatFormatting.GOLD + FabricLang.get("stageEditorMobs");
        case 5:
            return ChatFormatting.YELLOW + FabricLang.get("stageEditorKillPlayers");
        case 6:
            return ChatFormatting.YELLOW + FabricLang.get("stageEditorReachLocs");
        case 7:
            return ChatFormatting.YELLOW + FabricLang.get("stageEditorPassword");
        case 8:
            return ChatFormatting.DARK_PURPLE + FabricLang.get("stageEditorCustom");
        case 9:
            if (!hasObjective) {
                return ChatFormatting.GRAY + FabricLang.get("stageEditorEvents");
            } else {
                return ChatFormatting.AQUA + FabricLang.get("stageEditorEvents");
            }
        case 10:
            if (!hasObjective) {
                return ChatFormatting.GRAY + FabricLang.get("stageEditorConditions");
            } else {
                return ChatFormatting.AQUA + FabricLang.get("stageEditorConditions");
            }
        case 11:
            if (!hasObjective) {
                return ChatFormatting.GRAY + FabricLang.get("delay");
            } else {
                return ChatFormatting.YELLOW + FabricLang.get("delay");
            }
        case 12:
            if (SessionData.get(uuid, stagePrefix + Key.S_DELAY) == null) {
                return ChatFormatting.GRAY + FabricLang.get("stageEditorDelayMessage");
            } else {
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorDelayMessage");
            }
        case 13:
            if (SessionData.get(uuid, stagePrefix + Key.S_START_MESSAGE) == null) {
                if (!hasObjective) {
                    return ChatFormatting.GRAY + FabricLang.get("stageEditorStartMessage");
                } else {
                    return ChatFormatting.YELLOW + FabricLang.get("stageEditorStartMessage");
                }
            } else {
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorStartMessage");
            }
        case 14:
            if (SessionData.get(uuid, stagePrefix + Key.S_COMPLETE_MESSAGE) == null) {
                if (!hasObjective) {
                    return ChatFormatting.GRAY + FabricLang.get("stageEditorCompleteMessage");
                } else {
                    return ChatFormatting.YELLOW + FabricLang.get("stageEditorCompleteMessage");
                }
            } else {
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorCompleteMessage");
            }
        case 15:
            if (SessionData.get(uuid, stagePrefix + Key.S_OVERRIDE_DISPLAY) == null) {
                if (!hasObjective) {
                    return ChatFormatting.GRAY + FabricLang.get("overrideCreateSet");
                } else {
                    return ChatFormatting.YELLOW + FabricLang.get("overrideCreateSet");
                }
            } else {
                return ChatFormatting.YELLOW + FabricLang.get("overrideCreateSet");
            }
        case 16:
            return ChatFormatting.RED + FabricLang.get("stageEditorDelete");
        case 17:
            return ChatFormatting.GREEN + FabricLang.get("done");
        default:
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getAdditionalText(final int number) {
        switch (number) {
        case 1:
            if (SessionData.get(uuid, stagePrefix + Key.S_BREAK_NAMES) == null
                    && SessionData.get(uuid, stagePrefix + Key.S_DAMAGE_NAMES) == null
                    && SessionData.get(uuid, stagePrefix + Key.S_PLACE_NAMES) == null
                    && SessionData.get(uuid, stagePrefix + Key.S_USE_NAMES) == null
                    && SessionData.get(uuid, stagePrefix + Key.S_CUT_NAMES) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                return "";
            }
        case 2:
            if (SessionData.get(uuid, stagePrefix + Key.S_CRAFT_ITEMS) == null
                    && SessionData.get(uuid, stagePrefix + Key.S_SMELT_ITEMS) == null
                    && SessionData.get(uuid, stagePrefix + Key.S_ENCHANT_ITEMS) == null
                    && SessionData.get(uuid, stagePrefix + Key.S_BREW_ITEMS) == null
                    && SessionData.get(uuid, stagePrefix + Key.S_CONSUME_ITEMS) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                return "";
            }
        case 3:
            if (SessionData.get(uuid, stagePrefix + Key.S_DELIVERY_NPCS) == null
                    && SessionData.get(uuid, stagePrefix + Key.S_NPCS_TO_TALK_TO) == null
                    && SessionData.get(uuid, stagePrefix + Key.S_NPCS_TO_KILL) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                return "";
            }
        case 4:
            if (SessionData.get(uuid, stagePrefix + Key.S_MOB_TYPES) == null
                    && SessionData.get(uuid, stagePrefix + Key.S_TAME_TYPES) == null
                    && SessionData.get(uuid, stagePrefix + Key.S_FISH) == null
                    && SessionData.get(uuid, stagePrefix + Key.S_COW_MILK) == null
                    && SessionData.get(uuid, stagePrefix + Key.S_SHEAR_COLORS) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                return "";
            }
        case 5:
            if (SessionData.get(uuid, stagePrefix + Key.S_PLAYER_KILL) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final Integer players = (Integer) SessionData.get(uuid, stagePrefix + Key.S_PLAYER_KILL);
                return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + players + " " + FabricLang.get("stageEditorPlayers")
                        + ChatFormatting.GRAY + ")";
            }
        case 6:
            if (SessionData.get(uuid, stagePrefix + Key.S_REACH_LOCATIONS) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> locations
                        = (LinkedList<String>) SessionData.get(uuid, stagePrefix + Key.S_REACH_LOCATIONS);
                final LinkedList<Integer> radii
                        = (LinkedList<Integer>) SessionData.get(uuid, stagePrefix + Key.S_REACH_LOCATIONS_RADIUS);
                final LinkedList<String> names
                        = (LinkedList<String>) SessionData.get(uuid, stagePrefix + Key.S_REACH_LOCATIONS_NAMES);
                if (locations != null && radii != null && names != null) {
                    for (int i = 0; i < locations.size(); i++) {
                        text.append("\n").append(ChatFormatting.GRAY).append("     - ")
                                .append(FabricLang.get("stageEditorReachRadii1")).append(" ").append(ChatFormatting.BLUE)
                                .append(radii.get(i)).append(ChatFormatting.GRAY).append(" ")
                                .append(FabricLang.get("stageEditorReachRadii2")).append(" ").append(ChatFormatting.AQUA)
                                .append(names.get(i)).append(ChatFormatting.GRAY).append(" (").append(ChatFormatting.DARK_AQUA)
                                .append(locations.get(i)).append(ChatFormatting.GRAY).append(")");
                    }
                }
                return text.toString();
            }
        case 7:
            if (SessionData.get(uuid, stagePrefix + Key.S_PASSWORD_PHRASES) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> passPhrases
                        = (LinkedList<String>) SessionData.get(uuid, stagePrefix + Key.S_PASSWORD_PHRASES);
                final LinkedList<String> passDisplays
                        = (LinkedList<String>) SessionData.get(uuid, stagePrefix + Key.S_PASSWORD_DISPLAYS);
                if (passPhrases != null && passDisplays != null) {
                    for (int i = 0; i < passDisplays.size(); i++) {
                        text.append("\n").append(ChatFormatting.AQUA).append("     - \"").append(passDisplays.get(i))
                                .append("\"\n").append(ChatFormatting.DARK_AQUA).append("          - ")
                                .append(passPhrases.get(i));
                    }
                }
                return text.toString();
            }
        case 8:
            if (SessionData.get(uuid, stagePrefix + Key.S_CUSTOM_OBJECTIVES) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> customObj
                        = (LinkedList<String>) SessionData.get(uuid, stagePrefix + Key.S_CUSTOM_OBJECTIVES);
                if (customObj != null) {
                    for (final String s : customObj) {
                        text.append("\n").append(ChatFormatting.LIGHT_PURPLE).append("     - ").append(s);
                    }
                }
                return text.toString();
            }
        case 9:
            if (!hasObjective) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("stageEditorOptional") + ")";
            } else if (SessionData.get(uuid, stagePrefix + Key.S_START_EVENT) == null
                    && SessionData.get(uuid, stagePrefix + Key.S_FINISH_EVENT) == null
                    && SessionData.get(uuid, stagePrefix + Key.S_DEATH_EVENT) == null
                    && SessionData.get(uuid, stagePrefix + Key.S_DISCONNECT_EVENT) == null
                    && SessionData.get(uuid, stagePrefix + Key.S_CHAT_EVENTS) == null
                    && SessionData.get(uuid, stagePrefix + Key.S_COMMAND_EVENTS) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                return "";
            }
        case 10:
            if (!hasObjective) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("stageEditorOptional") + ")";
            } else if (SessionData.get(uuid, stagePrefix + Key.S_CONDITION) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                return "";
            }
        case 11:
            if (!hasObjective) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("stageEditorOptional") + ")";
            } else {
                if (SessionData.get(uuid, stagePrefix + Key.S_DELAY) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final Long time = (Long) SessionData.get(uuid, stagePrefix + Key.S_DELAY);
                    if (time == null) {
                        return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                    } else {
                        return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + FabricMiscUtil.getTime(time) + ChatFormatting.GRAY + ")";
                    }
                }
            }
        case 12:
            if (!hasObjective) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("stageEditorOptional") + ")";
            } else if (SessionData.get(uuid, stagePrefix + Key.S_DELAY) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noDelaySet") + ")";
            } else if (SessionData.get(uuid, stagePrefix + Key.S_DELAY_MESSAGE) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + "\""
                        + SessionData.get(uuid, stagePrefix + Key.S_DELAY_MESSAGE) + "\"" + ChatFormatting.GRAY + ")";
            }
        case 13:
            if (SessionData.get(uuid, stagePrefix + Key.S_START_MESSAGE) == null) {
                if (!hasObjective) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("stageEditorOptional") + ")";
                } else {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                }
            } else {
                return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + "\""
                        + SessionData.get(uuid, stagePrefix + Key.S_START_MESSAGE) + "\"" + ChatFormatting.GRAY + ")";
            }
        case 14:
            if (SessionData.get(uuid, stagePrefix + Key.S_COMPLETE_MESSAGE) == null) {
                if (!hasObjective) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("stageEditorOptional") + ")";
                } else {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                }
            } else {
                return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + "\""
                        + SessionData.get(uuid, stagePrefix + Key.S_COMPLETE_MESSAGE) + "\"" + ChatFormatting.GRAY + ")";
            }
        case 15:
            if (SessionData.get(uuid, stagePrefix + Key.S_OVERRIDE_DISPLAY) == null) {
                if (!hasObjective) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("stageEditorOptional") + ")";
                } else {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                }
            } else {
                final StringBuilder text = new StringBuilder();
                if (stagePrefix != null) {
                    final List<String> overrides
                            = (List<String>) SessionData.get(uuid, stagePrefix + Key.S_OVERRIDE_DISPLAY);
                    if (overrides != null) {
                        for (final String override : overrides) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA)
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

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull String getPromptText() {
        final String input = (String) SessionData.get(uuid, classPrefix + "-override");
        if (input != null && !input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
            if (input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, stagePrefix + Key.S_OVERRIDE_DISPLAY, null);
            } else {
                final LinkedList<String> overrides = new LinkedList<>();
                if (SessionData.get(uuid, stagePrefix + Key.S_OVERRIDE_DISPLAY) != null) {
                    overrides.addAll((List<String>) SessionData.get(uuid, stagePrefix + Key.S_OVERRIDE_DISPLAY));
                }
                overrides.add(input);
                SessionData.set(uuid, stagePrefix + Key.S_OVERRIDE_DISPLAY, overrides);
                SessionData.set(uuid, classPrefix + "-override", null);
            }
        }
        SessionData.set(uuid, stagePrefix, Boolean.TRUE);
        checkObjective();

        final StringBuilder text = new StringBuilder(ChatFormatting.LIGHT_PURPLE + "- " + ChatFormatting.AQUA
                + getTitle().replaceFirst(" \\| ", ChatFormatting.LIGHT_PURPLE + " | ") + " -");
        for (int i = 1; i <= size; i++) {
            text.append("\n").append(getNumberColor(i)).append(ChatFormatting.BOLD).append(i)
                    .append(ChatFormatting.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                    .append(getAdditionalText(i));
        }
        return sendClickableSelection(text.toString(), plugin.getQuester(uuid));
    }

    public void acceptInput(final Number input) {
        final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
        if (sender == null) {
            return;
        }
        switch (input.intValue()) {
        case 1:
            new FabricQuestBlocksPrompt(stageNum, uuid).start();
            break;
        case 2:
            new FabricQuestItemsPrompt(stageNum, uuid).start();
            break;
        case 3:
            new FabricQuestNpcsPrompt(stageNum, uuid).start();
            break;
        case 4:
            new FabricQuestMobsPrompt(stageNum, uuid).start();
            break;
        case 5:
            new QuestKillPlayerPrompt(uuid).start();
            break;
        case 6:
            new QuestReachListPrompt(uuid).start();
            break;
        case 7:
            new QuestPasswordListPrompt(uuid).start();
            break;
        case 8:
            new QuestCustomObjectiveModulePrompt(uuid).start();
            break;
        case 9:
            if (hasObjective) {
                new QuestActionListPrompt(uuid).start();
            } else {
                sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("invalidOption")));
                new FabricQuestStageMainPrompt(stageNum, uuid).start();
            }
            break;
        case 10:
            if (hasObjective) {
                new QuestConditionListPrompt(uuid).start();
            } else {
                sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("invalidOption")));
                new FabricQuestStageMainPrompt(stageNum, uuid).start();
            }
            break;
        case 11:
            if (hasObjective) {
                new QuestDelayPrompt(uuid).start();
            } else {
                sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("invalidOption")));
                new FabricQuestStageMainPrompt(stageNum, uuid).start();
            }
            break;
        case 12:
            if (SessionData.get(uuid, stagePrefix + Key.S_DELAY) == null) {
                sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("stageEditorNoDelaySet")));
                new FabricQuestStageMainPrompt(stageNum, uuid).start();
            } else {
                new QuestDelayMessagePrompt(uuid).start();
            }
            break;
        case 13:
            if (hasObjective) {
                new QuestStartMessagePrompt(uuid).start();
            } else {
                sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("invalidOption")));
                new FabricQuestStageMainPrompt(stageNum, uuid).start();
            }
            break;
        case 14:
            if (hasObjective) {
                new QuestCompleteMessagePrompt(uuid).start();
            } else {
                sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("invalidOption")));
                new FabricQuestStageMainPrompt(stageNum, uuid).start();
            }
            break;
        case 15:
            if (hasObjective) {
                new FabricOverridePrompt(uuid, this, FabricLang.get("overrideCreateEnter")).start();
            } else {
                sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("invalidOption")));
                new FabricQuestStageMainPrompt(stageNum, uuid).start();
            }
            break;
        case 16:
            new QuestStageDeletePrompt(uuid).start();
            break;
        case 17:
            new FabricQuestStageMenuPrompt(uuid).start();
            break;
        default:
            new FabricQuestStageMainPrompt(stageNum, uuid).start();
            break;
        }
    }

    public boolean checkObjective() {
        if (SessionData.get(uuid, stagePrefix + Key.S_BREAK_NAMES) != null
                || SessionData.get(uuid, stagePrefix + Key.S_DAMAGE_NAMES) != null
                || SessionData.get(uuid, stagePrefix + Key.S_PLACE_NAMES) != null
                || SessionData.get(uuid, stagePrefix + Key.S_USE_NAMES) != null
                || SessionData.get(uuid, stagePrefix + Key.S_CUT_NAMES) != null
                || SessionData.get(uuid, stagePrefix + Key.S_CRAFT_ITEMS) != null
                || SessionData.get(uuid, stagePrefix + Key.S_SMELT_ITEMS) != null
                || SessionData.get(uuid, stagePrefix + Key.S_ENCHANT_ITEMS) != null
                || SessionData.get(uuid, stagePrefix + Key.S_BREW_ITEMS) != null
                || SessionData.get(uuid, stagePrefix + Key.S_CONSUME_ITEMS) != null
                || SessionData.get(uuid, stagePrefix + Key.S_DELIVERY_NPCS) != null
                || SessionData.get(uuid, stagePrefix + Key.S_NPCS_TO_TALK_TO) != null
                || SessionData.get(uuid, stagePrefix + Key.S_NPCS_TO_KILL) != null
                || SessionData.get(uuid, stagePrefix + Key.S_MOB_TYPES) != null
                || SessionData.get(uuid, stagePrefix + Key.S_FISH) != null
                || SessionData.get(uuid, stagePrefix + Key.S_COW_MILK) != null
                || SessionData.get(uuid, stagePrefix + Key.S_TAME_TYPES) != null
                || SessionData.get(uuid, stagePrefix + Key.S_SHEAR_COLORS) != null
                || SessionData.get(uuid, stagePrefix + Key.S_PLAYER_KILL) != null
                || SessionData.get(uuid, stagePrefix + Key.S_REACH_LOCATIONS) != null
                || SessionData.get(uuid, stagePrefix + Key.S_PASSWORD_PHRASES) != null
                || SessionData.get(uuid, stagePrefix + Key.S_CUSTOM_OBJECTIVES) != null) {
            hasObjective = true;
            return true;
        }
        return false;
    }

    public class QuestKillPlayerPrompt extends FabricQuestsEditorStringPrompt {

        public QuestKillPlayerPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorKillPlayerPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            if (input != null && !input.equalsIgnoreCase(FabricLang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i < 0) {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("stageEditorPositiveAmount")));
                        new QuestKillPlayerPrompt(uuid).start();
                        return;
                    } else if (i > 0) {
                        SessionData.set(uuid, stagePrefix + Key.S_PLAYER_KILL, i);
                    }
                } catch (final NumberFormatException e) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("reqNotANumber")
                            .replace("<input>", input)));
                    new QuestKillPlayerPrompt(uuid).start();
                    return;
                }
            } else if (input != null && input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, stagePrefix + Key.S_PLAYER_KILL, null);
            }
            new FabricQuestStageMainPrompt(stageNum, uuid).start();
        }
    }

    public class QuestReachListPrompt extends FabricQuestsEditorIntegerPrompt {

        public QuestReachListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 5;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return FabricLang.get("stageEditorReachLocs");
        }

        @Override
        public ChatFormatting getNumberColor(final int number) {
            switch (number) {
                case 1:
                case 2:
                case 3:
                    return ChatFormatting.BLUE;
                case 4:
                    return ChatFormatting.RED;
                case 5:
                    return ChatFormatting.GREEN;
                default:
                    return null;
            }
        }

        @Override
        public String getSelectionText(final int number) {
            switch(number) {
            case 1:
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorSetLocations");
            case 2:
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorSetLocationRadii");
            case 3:
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorSetLocationNames");
            case 4:
                return ChatFormatting.RED + FabricLang.get("clear");
            case 5:
                return ChatFormatting.GREEN + FabricLang.get("done");
            default:
                return null;
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final int number) {
            switch(number) {
            case 1:
                if (SessionData.get(uuid, stagePrefix + Key.S_REACH_LOCATIONS) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> locations
                            = (List<String>) SessionData.get(uuid, stagePrefix + Key.S_REACH_LOCATIONS);
                    if (locations != null) {
                        for (final String s : locations) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.DARK_AQUA)
                                    .append(s);
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (SessionData.get(uuid, stagePrefix + Key.S_REACH_LOCATIONS_RADIUS) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<Integer> radius
                            = (List<Integer>) SessionData.get(uuid, stagePrefix + Key.S_REACH_LOCATIONS_RADIUS);
                    if (radius != null) {
                        for (final Integer i : radius) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA).append(i);
                        }
                    }
                    return text.toString();
                }
            case 3:
                if (SessionData.get(uuid, stagePrefix + Key.S_REACH_LOCATIONS_NAMES) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> names
                            = (List<String>) SessionData.get(uuid, stagePrefix + Key.S_REACH_LOCATIONS_NAMES);
                    if (names != null) {
                        for (final String s : names) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA).append(s);
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
        public @NotNull String getPromptText() {
            final StringBuilder text = new StringBuilder(ChatFormatting.AQUA + "- " + getTitle() + " -");
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatFormatting.BOLD).append(i)
                        .append(ChatFormatting.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                        .append(getAdditionalText(i));
            }
            return sendClickableSelection(text.toString(), plugin.getQuester(uuid));
        }

        @SuppressWarnings("unchecked")
        @Override
        public void acceptInput(final Number input) {
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            switch(input.intValue()) {
            case 1:
                // Store current player position as temp block
                plugin.getTempBlocks().put(uuid, sender.blockPosition());
                new QuestReachLocationPrompt(uuid).start();
                break;
            case 2:
                if (SessionData.get(uuid, stagePrefix + Key.S_REACH_LOCATIONS) == null) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("stageEditorNoLocations")));
                    new QuestReachListPrompt(uuid).start();
                } else {
                    new QuestReachRadiiPrompt(uuid).start();
                }
                break;
            case 3:
                if (SessionData.get(uuid, stagePrefix + Key.S_REACH_LOCATIONS) == null) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("stageEditorNoLocations")));
                    new QuestReachListPrompt(uuid).start();
                } else {
                    new QuestReachNamesPrompt(uuid).start();
                }
                break;
            case 4:
                sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("stageEditorObjectiveCleared")));
                SessionData.set(uuid, stagePrefix + Key.S_REACH_LOCATIONS, null);
                SessionData.set(uuid, stagePrefix + Key.S_REACH_LOCATIONS_RADIUS, null);
                SessionData.set(uuid, stagePrefix + Key.S_REACH_LOCATIONS_NAMES, null);
                new QuestReachListPrompt(uuid).start();
                break;
            case 5:
                final int one;
                final int two;
                final int three;
                final List<String> locations
                        = (List<String>) SessionData.get(uuid, stagePrefix + Key.S_REACH_LOCATIONS);
                final List<Integer> radius
                        = (List<Integer>) SessionData.get(uuid, stagePrefix + Key.S_REACH_LOCATIONS_RADIUS);
                final List<String> names
                        = (List<String>) SessionData.get(uuid, stagePrefix + Key.S_REACH_LOCATIONS_NAMES);
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
                    new FabricQuestStageMainPrompt(stageNum, uuid).start();
                } else {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("listsNotSameSize")));
                    new QuestReachListPrompt(uuid).start();
                }
                break;
            default:
                new FabricQuestStageMainPrompt(stageNum, uuid).start();
                break;
            }
        }
    }

    public class QuestReachLocationPrompt extends FabricQuestsEditorStringPrompt {

        public QuestReachLocationPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorReachLocationPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final String input) {
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            if (input != null && input.equalsIgnoreCase(FabricLang.get("cmdAdd"))) {
                final BlockPos block = plugin.getTempBlocks().get(uuid);
                if (block != null) {
                    final LinkedList<String> locations
                            = SessionData.get(uuid, stagePrefix + Key.S_REACH_LOCATIONS) != null
                            ? (LinkedList<String>) SessionData.get(uuid, stagePrefix + Key.S_REACH_LOCATIONS)
                            : new LinkedList<>();
                    if (locations != null) {
                        locations.add(FabricConfigUtil.getLocationInfo(block));
                    }
                    SessionData.set(uuid, stagePrefix + Key.S_REACH_LOCATIONS, locations);

                    LinkedList<Integer> amounts = new LinkedList<>();
                    LinkedList<String> locNames = new LinkedList<>();
                    if (SessionData.get(uuid, stagePrefix + Key.S_REACH_LOCATIONS_RADIUS) != null) {
                        amounts = (LinkedList<Integer>) SessionData.get(uuid, stagePrefix + Key.S_REACH_LOCATIONS_RADIUS);
                    }
                    if (SessionData.get(uuid, stagePrefix + Key.S_REACH_LOCATIONS_NAMES) != null) {
                        locNames = (LinkedList<String>) SessionData.get(uuid, stagePrefix + Key.S_REACH_LOCATIONS_NAMES);
                    }
                    if (locations != null && amounts != null && locNames != null) {
                        for (int i = 0; i < locations.size(); i++) {
                            if (i >= amounts.size()) {
                                amounts.add(5);
                            }
                            if (i >= locNames.size()) {
                                locNames.add(FabricLang.get("location").replace("<id>", "#" + (i + 1)));
                            }
                        }
                    }
                    SessionData.set(uuid, stagePrefix + Key.S_REACH_LOCATIONS_RADIUS, amounts);
                    SessionData.set(uuid, stagePrefix + Key.S_REACH_LOCATIONS_NAMES, locNames);
                } else {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("stageEditorNoBlockSelected")));
                    new QuestReachLocationPrompt(uuid).start();
                    return;
                }
                plugin.getTempBlocks().remove(uuid);
                new QuestReachListPrompt(uuid).start();
            } else if (input != null && input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                plugin.getTempBlocks().remove(uuid);
                new QuestReachListPrompt(uuid).start();
            } else {
                new QuestReachLocationPrompt(uuid).start();
            }
        }
    }

    public class QuestReachRadiiPrompt extends FabricQuestsEditorStringPrompt {

        public QuestReachRadiiPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorReachLocationRadiiPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            if (input != null && !input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                final LinkedList<Integer> radii = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    try {
                        final int i = Integer.parseInt(s);
                        if (i < 1) {
                            sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("invalidMinimum")
                                    .replace("<number>", "1")));
                            new QuestReachRadiiPrompt(uuid).start();
                            return;
                        }
                        radii.add(i);
                    } catch (final NumberFormatException e) {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("reqNotANumber")
                                .replace("<input>", input)));
                        new QuestReachRadiiPrompt(uuid).start();
                        return;
                    }
                }
                SessionData.set(uuid, stagePrefix + Key.S_REACH_LOCATIONS_RADIUS, radii);
            }
            new QuestReachListPrompt(uuid).start();
        }
    }

    public class QuestReachNamesPrompt extends FabricQuestsEditorStringPrompt {

        public QuestReachNamesPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorReachLocationNamesPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input != null && !input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                final LinkedList<String> locNames = new LinkedList<>(Arrays.asList(input.split(FabricLang.get("charSemi"))));
                SessionData.set(uuid, stagePrefix + Key.S_REACH_LOCATIONS_NAMES, locNames);
            }
            new QuestReachListPrompt(uuid).start();
        }
    }

    public class QuestPasswordListPrompt extends FabricQuestsEditorIntegerPrompt {

        public QuestPasswordListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 4;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return FabricLang.get("stageEditorPassword");
        }

        @Override
        public ChatFormatting getNumberColor(final int number) {
            switch (number) {
                case 1:
                case 2:
                    return ChatFormatting.BLUE;
                case 3:
                    return ChatFormatting.RED;
                case 4:
                    return ChatFormatting.GREEN;
                default:
                    return null;
            }
        }

        @Override
        public String getSelectionText(final int number) {
            switch(number) {
            case 1:
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorAddPasswordDisplay");
            case 2:
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorAddPasswordPhrases");
            case 3:
                return ChatFormatting.RED + FabricLang.get("clear");
            case 4:
                return ChatFormatting.GREEN + FabricLang.get("done");
            default:
                return null;
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final int number) {
            switch(number) {
            case 1:
                if (SessionData.get(uuid, stagePrefix + Key.S_PASSWORD_DISPLAYS) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> displays = (List<String>) SessionData.get(uuid, stagePrefix
                            + Key.S_PASSWORD_DISPLAYS);
                    if (displays != null) {
                        for (final String display : displays) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA)
                                    .append(display);
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (SessionData.get(uuid, stagePrefix + Key.S_PASSWORD_PHRASES) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> phrases = (List<String>) SessionData.get(uuid, stagePrefix
                            + Key.S_PASSWORD_PHRASES);
                    if (phrases != null) {
                        for (final String phrase : phrases) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.DARK_AQUA)
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
        public @NotNull String getPromptText() {
            final StringBuilder text = new StringBuilder(ChatFormatting.AQUA + "- " + getTitle() + " -");
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatFormatting.BOLD).append(i)
                        .append(ChatFormatting.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                        .append(getAdditionalText(i));
            }
            return sendClickableSelection(text.toString(), plugin.getQuester(uuid));
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final Number input) {
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            switch(input.intValue()) {
            case 1:
                new QuestPasswordDisplayPrompt(uuid).start();
                break;
            case 2:
                if (SessionData.get(uuid, stagePrefix + Key.S_PASSWORD_DISPLAYS) == null) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("stageEditorMustSetPasswordDisplays")));
                    new QuestPasswordListPrompt(uuid).start();
                } else {
                    new QuestPasswordPhrasePrompt(uuid).start();
                }
                break;
            case 3:
                sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("stageEditorObjectiveCleared")));
                SessionData.set(uuid, stagePrefix + Key.S_PASSWORD_DISPLAYS, null);
                SessionData.set(uuid, stagePrefix + Key.S_PASSWORD_PHRASES, null);
                new QuestPasswordListPrompt(uuid).start();
                break;
            case 4:
                final int one;
                final int two;
                final List<String> displays
                        = (List<String>) SessionData.get(uuid, stagePrefix + Key.S_PASSWORD_DISPLAYS);
                final List<String> phrases = (List<String>) SessionData.get(uuid, stagePrefix + Key.S_PASSWORD_PHRASES);
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
                    new FabricQuestStageMainPrompt(stageNum, uuid).start();
                } else {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("listsNotSameSize")));
                    new QuestPasswordListPrompt(uuid).start();
                }
                break;
            default:
                new FabricQuestStageMainPrompt(stageNum, uuid).start();
                break;
            }
        }
    }

    public class QuestPasswordDisplayPrompt extends FabricQuestsEditorStringPrompt {

        public QuestPasswordDisplayPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorPasswordDisplayPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.YELLOW + getQueryText() + "\n";
        }

        @Override
        public void acceptInput(final String input) {
            if (input != null && !input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                if (SessionData.get(uuid, stagePrefix + Key.S_PASSWORD_DISPLAYS) != null) {
                    @SuppressWarnings("unchecked")
                    final List<String> displays = (List<String>) SessionData.get(uuid, stagePrefix
                            + Key.S_PASSWORD_DISPLAYS);
                    if (displays != null) {
                        displays.addAll(Arrays.asList(input.split(FabricLang.get("charSemi"))));
                    }
                    SessionData.set(uuid, stagePrefix + Key.S_PASSWORD_DISPLAYS, displays);
                } else {
                    final List<String> displays = new LinkedList<>(Arrays.asList(input.split(FabricLang.get("charSemi"))));
                    SessionData.set(uuid, stagePrefix + Key.S_PASSWORD_DISPLAYS, displays);
                }
            }
            new QuestPasswordListPrompt(uuid).start();
        }
    }

    public class QuestPasswordPhrasePrompt extends FabricQuestsEditorStringPrompt {

        public QuestPasswordPhrasePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorPasswordPhrasePrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.YELLOW + getQueryText() + "\n";
        }

        @Override
        public void acceptInput(final String input) {
            if (input != null && !input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                if (SessionData.get(uuid, stagePrefix + Key.S_PASSWORD_PHRASES) != null) {
                    @SuppressWarnings("unchecked")
                    final List<String> phrases = (List<String>) SessionData.get(uuid, stagePrefix
                            + Key.S_PASSWORD_PHRASES);
                    if (phrases != null) {
                        phrases.addAll(Arrays.asList(input.split(FabricLang.get("charSemi"))));
                    }
                    SessionData.set(uuid, stagePrefix + Key.S_PASSWORD_PHRASES, phrases);
                } else {
                    final List<String> phrases = new LinkedList<>(Arrays.asList(input.split(FabricLang.get("charSemi"))));
                    SessionData.set(uuid, stagePrefix + Key.S_PASSWORD_PHRASES, phrases);
                }
            }
            new QuestPasswordListPrompt(uuid).start();
        }
    }

    public class QuestActionListPrompt extends FabricQuestsEditorIntegerPrompt {

        public QuestActionListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 8;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return FabricLang.get("stageEditorStageEvents");
        }

        @Override
        public ChatFormatting getNumberColor(final int number) {
            switch (number) {
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    return ChatFormatting.BLUE;
                case 8:
                    return ChatFormatting.GREEN;
                default:
                    return null;
            }
        }

        @Override
        public String getSelectionText(final int number) {
            switch(number) {
            case 1:
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorStartEvent");
            case 2:
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorFinishEvent");
            case 3:
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorFailEvent");
            case 4:
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorDeathEvent");
            case 5:
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorDisconnectEvent");
            case 6:
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorChatEvents");
            case 7:
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorCommandEvents");
            case 8:
                return ChatFormatting.GREEN + FabricLang.get("done");
            default:
                return null;
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final int number) {
            switch(number) {
            case 1:
                if (SessionData.get(uuid, stagePrefix + Key.S_START_EVENT) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    return ChatFormatting.YELLOW + "(" + ChatFormatting.AQUA + SessionData.get(uuid, stagePrefix
                            + Key.S_START_EVENT) + ChatFormatting.YELLOW + ")";
                }
            case 2:
                if (SessionData.get(uuid, stagePrefix + Key.S_FINISH_EVENT) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    return ChatFormatting.YELLOW + "(" + ChatFormatting.AQUA + SessionData.get(uuid, stagePrefix
                            + Key.S_FINISH_EVENT) + ChatFormatting.YELLOW + ")";
                }
            case 3:
                if (SessionData.get(uuid, stagePrefix + Key.S_FAIL_EVENT) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    return ChatFormatting.YELLOW + "(" + ChatFormatting.AQUA + SessionData.get(uuid, stagePrefix
                            + Key.S_FAIL_EVENT) + ChatFormatting.YELLOW + ")";
                }
            case 4:
                if (SessionData.get(uuid, stagePrefix + Key.S_DEATH_EVENT) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    return ChatFormatting.YELLOW + "(" + ChatFormatting.AQUA + SessionData.get(uuid, stagePrefix
                            + Key.S_DEATH_EVENT) + ChatFormatting.YELLOW + ")";
                }
            case 5:
                if (SessionData.get(uuid, stagePrefix + Key.S_DISCONNECT_EVENT) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    return ChatFormatting.YELLOW + "(" + ChatFormatting.AQUA + SessionData.get(uuid, stagePrefix
                            + Key.S_DISCONNECT_EVENT) + ChatFormatting.YELLOW + ")";
                }
            case 6:
                if (SessionData.get(uuid, stagePrefix + Key.S_CHAT_EVENTS) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final LinkedList<String> chatEvents = (LinkedList<String>) SessionData.get(uuid, stagePrefix
                            + Key.S_CHAT_EVENTS);
                    final LinkedList<String> chatEventTriggers
                            = (LinkedList<String>) SessionData.get(uuid, stagePrefix + Key.S_CHAT_EVENT_TRIGGERS);
                    if (chatEvents != null && chatEventTriggers != null) {
                        for (final String event : chatEvents) {
                            text.append("\n").append(ChatFormatting.AQUA).append("     - ").append(event)
                                    .append(ChatFormatting.BLUE).append(" (").append(FabricLang.get("stageEditorTriggeredBy"))
                                    .append(": \"").append(chatEventTriggers.get(chatEvents.indexOf(event)))
                                    .append("\")");
                        }
                    }
                    return text.toString();
                }
            case 7:
                if (SessionData.get(uuid, stagePrefix + Key.S_COMMAND_EVENTS) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final LinkedList<String> commandEvents
                            = (LinkedList<String>) SessionData.get(uuid, stagePrefix + Key.S_COMMAND_EVENTS);
                    final LinkedList<String> commandEventTriggers
                            = (LinkedList<String>) SessionData.get(uuid, stagePrefix + Key.S_COMMAND_EVENT_TRIGGERS);
                    if (commandEvents != null && commandEventTriggers != null) {
                        for (final String event : commandEvents) {
                            text.append("\n").append(ChatFormatting.AQUA).append("     - ").append(event)
                                    .append(ChatFormatting.BLUE).append(" (").append(FabricLang.get("stageEditorTriggeredBy"))
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
        public @NotNull String getPromptText() {
            final StringBuilder text = new StringBuilder(ChatFormatting.AQUA + "- " + getTitle() + " -");
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatFormatting.BOLD).append(i)
                        .append(ChatFormatting.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                        .append(getAdditionalText(i));
            }
            return sendClickableSelection(text.toString(), plugin.getQuester(uuid));
        }

        @Override
        public void acceptInput(final Number input) {
            switch(input.intValue()) {
            case 1:
                new QuestStartActionPrompt(uuid).start();
                break;
            case 2:
                new QuestFinishActionPrompt(uuid).start();
                break;
            case 3:
                new QuestFailActionPrompt(uuid).start();
                break;
            case 4:
                new QuestDeathActionPrompt(uuid).start();
                break;
            case 5:
                new QuestDisconnectActionPrompt(uuid).start();
                break;
            case 6:
                new QuestChatActionPrompt(uuid).start();
                break;
            case 7:
                new QuestCommandActionPrompt(uuid).start();
                break;
            case 8:
                new FabricQuestStageMainPrompt(stageNum, uuid).start();
                break;
            default:
                new QuestActionListPrompt(uuid).start();
                break;
            }
        }
    }

    public class QuestStartActionPrompt extends FabricQuestsEditorStringPrompt {

        public QuestStartActionPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return FabricLang.get("stageEditorStartEvent");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorEventsPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final StringBuilder text = new StringBuilder(ChatFormatting.AQUA + "- " + getTitle() + " -\n");
            if (plugin.getLoadedActions().isEmpty()) {
                text.append(ChatFormatting.RED).append("- ").append(FabricLang.get("none")).append("\n");
            } else {
                for (final Action a : plugin.getLoadedActions()) {
                    text.append(ChatFormatting.GREEN).append("- ").append(a.getName()).append("\n");
                }
            }
            return text.toString() + ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            if (input != null && !input.equalsIgnoreCase(FabricLang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                Action found = null;
                for (final Action a : plugin.getLoadedActions()) {
                    if (a.getName().equalsIgnoreCase(input)) {
                        found = a;
                        break;
                    }
                }
                if (found == null) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("stageEditorInvalidEvent")
                            .replace("<input>", input)));
                    new QuestStartActionPrompt(uuid).start();
                } else {
                    SessionData.set(uuid, stagePrefix + Key.S_START_EVENT, found.getName());
                    new QuestActionListPrompt(uuid).start();
                }
            } else if (input != null && input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                new QuestActionListPrompt(uuid).start();
            } else if (input != null && input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, stagePrefix + Key.S_START_EVENT, null);
                sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("stageEditorEventCleared")));
                new QuestActionListPrompt(uuid).start();
            } else {
                new QuestStartActionPrompt(uuid).start();
            }
        }
    }

    public class QuestFinishActionPrompt extends FabricQuestsEditorStringPrompt {

        public QuestFinishActionPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return FabricLang.get("stageEditorFinishEvent");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorEventsPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final StringBuilder text = new StringBuilder(ChatFormatting.AQUA + "- " + getTitle() + " -\n");
            if (plugin.getLoadedActions().isEmpty()) {
                text.append(ChatFormatting.RED).append("- ").append(FabricLang.get("none"));
            } else {
                for (final Action a : plugin.getLoadedActions()) {
                    text.append(ChatFormatting.GREEN).append("- ").append(a.getName()).append("\n");
                }
            }
            return text.toString() + ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            if (input != null && !input.equalsIgnoreCase(FabricLang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                Action found = null;
                for (final Action a : plugin.getLoadedActions()) {
                    if (a.getName().equalsIgnoreCase(input)) {
                        found = a;
                        break;
                    }
                }
                if (found == null) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("stageEditorInvalidEvent")
                            .replace("<input>", input)));
                    new QuestFinishActionPrompt(uuid).start();
                } else {
                    SessionData.set(uuid, stagePrefix + Key.S_FINISH_EVENT, found.getName());
                    new QuestActionListPrompt(uuid).start();
                }
            } else if (input != null && input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                new QuestActionListPrompt(uuid).start();
            } else if (input != null && input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, stagePrefix + Key.S_FINISH_EVENT, null);
                sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("stageEditorEventCleared")));
                new QuestActionListPrompt(uuid).start();
            } else {
                new QuestFinishActionPrompt(uuid).start();
            }
        }
    }

    public class QuestFailActionPrompt extends FabricQuestsEditorStringPrompt {

        public QuestFailActionPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return FabricLang.get("stageEditorFailEvent");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorEventsPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final StringBuilder text = new StringBuilder(ChatFormatting.AQUA + "- " + getTitle() + " -\n");
            if (plugin.getLoadedActions().isEmpty()) {
                text.append(ChatFormatting.RED).append("- ").append(FabricLang.get("none"));
            } else {
                for (final Action a : plugin.getLoadedActions()) {
                    text.append(ChatFormatting.GREEN).append("- ").append(a.getName()).append("\n");
                }
            }
            return text.toString() + ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            if (input != null && !input.equalsIgnoreCase(FabricLang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                Action found = null;
                for (final Action a : plugin.getLoadedActions()) {
                    if (a.getName().equalsIgnoreCase(input)) {
                        found = a;
                        break;
                    }
                }
                if (found == null) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("stageEditorInvalidEvent")
                            .replace("<input>", input)));
                    new QuestFailActionPrompt(uuid).start();
                } else {
                    SessionData.set(uuid, stagePrefix + Key.S_FAIL_EVENT, found.getName());
                    new QuestActionListPrompt(uuid).start();
                }
            } else if (input != null && input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                new QuestActionListPrompt(uuid).start();
            } else if (input != null && input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, stagePrefix + Key.S_FAIL_EVENT, null);
                sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("stageEditorEventCleared")));
                new QuestActionListPrompt(uuid).start();
            } else {
                new QuestFailActionPrompt(uuid).start();
            }
        }
    }

    public class QuestDeathActionPrompt extends FabricQuestsEditorStringPrompt {

        public QuestDeathActionPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return FabricLang.get("stageEditorDeathEvent");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorEventsPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final StringBuilder text = new StringBuilder(ChatFormatting.AQUA + "- " + getTitle() + " -\n");
            if (plugin.getLoadedActions().isEmpty()) {
                text.append(ChatFormatting.RED).append("- ").append(FabricLang.get("none")).append("\n");
            } else {
                for (final Action a : plugin.getLoadedActions()) {
                    text.append(ChatFormatting.GREEN).append("- ").append(a.getName()).append("\n");
                }
            }
            return text.toString() + ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            if (input != null && !input.equalsIgnoreCase(FabricLang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                Action found = null;
                for (final Action a : plugin.getLoadedActions()) {
                    if (a.getName().equalsIgnoreCase(input)) {
                        found = a;
                        break;
                    }
                }
                if (found == null) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("stageEditorInvalidEvent")
                            .replace("<input>", input)));
                    new QuestDeathActionPrompt(uuid).start();
                } else {
                    SessionData.set(uuid, stagePrefix + Key.S_DEATH_EVENT, found.getName());
                    new QuestActionListPrompt(uuid).start();
                }
            } else if (input != null && input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                new QuestActionListPrompt(uuid).start();
            } else if (input != null && input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, stagePrefix + Key.S_DEATH_EVENT, null);
                sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("stageEditorEventCleared")));
                new QuestActionListPrompt(uuid).start();
            } else {
                new QuestDeathActionPrompt(uuid).start();
            }
        }
    }

    public class QuestDisconnectActionPrompt extends FabricQuestsEditorStringPrompt {

        public QuestDisconnectActionPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return FabricLang.get("stageEditorDisconnectEvent");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorEventsPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final StringBuilder text = new StringBuilder(ChatFormatting.AQUA + "- " + getTitle() + " -\n");
            if (plugin.getLoadedActions().isEmpty()) {
                text.append(ChatFormatting.RED).append("- ").append(FabricLang.get("none"));
            } else {
                for (final Action a : plugin.getLoadedActions()) {
                    text.append(ChatFormatting.GREEN).append("- ").append(a.getName()).append("\n");
                }
            }
            return text.toString() + ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            if (input != null && !input.equalsIgnoreCase(FabricLang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                Action found = null;
                for (final Action a : plugin.getLoadedActions()) {
                    if (a.getName().equalsIgnoreCase(input)) {
                        found = a;
                        break;
                    }
                }
                if (found == null) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("stageEditorInvalidEvent")
                            .replace("<input>", input)));
                    new QuestDisconnectActionPrompt(uuid).start();
                } else {
                    SessionData.set(uuid, stagePrefix + Key.S_DISCONNECT_EVENT, found.getName());
                    new QuestActionListPrompt(uuid).start();
                }
            } else if (input != null && input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                new QuestActionListPrompt(uuid).start();
            } else if (input != null && input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, stagePrefix + Key.S_DISCONNECT_EVENT, null);
                sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("stageEditorEventCleared")));
                new QuestActionListPrompt(uuid).start();
            } else {
                new QuestDisconnectActionPrompt(uuid).start();
            }
        }
    }

    public class QuestChatActionPrompt extends FabricQuestsEditorStringPrompt {

        public QuestChatActionPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return FabricLang.get("stageEditorChatEvents");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorChatEventsPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final StringBuilder text = new StringBuilder(ChatFormatting.AQUA + "- " + getTitle() + " -\n");
            if (plugin.getLoadedActions().isEmpty()) {
                text.append(ChatFormatting.RED).append("- ").append(FabricLang.get("none"));
            } else {
                for (final Action a : plugin.getLoadedActions()) {
                    text.append(ChatFormatting.GREEN).append("- ").append(a.getName()).append("\n");
                }
            }
            return text.toString() + ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            if (input != null && !input.equalsIgnoreCase(FabricLang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                Action found = null;
                for (final Action a : plugin.getLoadedActions()) {
                    if (a.getName().equalsIgnoreCase(input)) {
                        found = a;
                        break;
                    }
                }
                if (found == null) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("stageEditorInvalidEvent")
                            .replace("<input>", input)));
                    new QuestChatActionPrompt(uuid).start();
                } else {
                    SessionData.set(uuid, stagePrefix + Key.S_CHAT_TEMP_EVENT, found.getName());
                    new QuestChatActionTriggerPrompt(uuid).start();
                }
            } else if (input != null && input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                new QuestActionListPrompt(uuid).start();
            } else if (input != null && input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, stagePrefix + Key.S_CHAT_EVENTS, null);
                SessionData.set(uuid, stagePrefix + Key.S_CHAT_EVENT_TRIGGERS, null);
                sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("stageEditorChatEventsCleared")));
                new QuestActionListPrompt(uuid).start();
            } else {
                new QuestChatActionPrompt(uuid).start();
            }
        }
    }

    public class QuestChatActionTriggerPrompt extends FabricQuestsEditorStringPrompt {

        public QuestChatActionTriggerPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return FabricLang.get("stageEditorChatTrigger");
        }

        @Override
        public String getQueryText() {
            final String tempEvent = (String) SessionData.get(uuid, stagePrefix + Key.S_CHAT_TEMP_EVENT);
            if (tempEvent != null) {
                return FabricLang.get("stageEditorChatEventsTriggerPrompt").replace("<event>", tempEvent)
                        .replace("<action>", tempEvent);
            } else {
                return FabricLang.get("stageEditorChatEventsTriggerPrompt");
            }
        }

        @Override
        public @NotNull String getPromptText() {
            final String tempEvent = (String) SessionData.get(uuid, stagePrefix + Key.S_CHAT_TEMP_EVENT);
            String text = ChatFormatting.GOLD + "- " + getTitle() + " -\n";
            if (tempEvent != null) {
                text += ChatFormatting.YELLOW + getQueryText()
                        .replaceFirst(tempEvent, ChatFormatting.AQUA + tempEvent + ChatFormatting.YELLOW);
            }
            return text;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final String input) {
            if (input != null && !input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                if (SessionData.get(uuid, stagePrefix + Key.S_CHAT_EVENTS) == null) {
                    final LinkedList<String> chatEvents = new LinkedList<>();
                    final LinkedList<String> chatEventTriggers = new LinkedList<>();
                    final String event = (String) SessionData.get(uuid, stagePrefix + Key.S_CHAT_TEMP_EVENT);
                    chatEvents.add(event);
                    chatEventTriggers.add(input.trim());
                    SessionData.set(uuid, stagePrefix + Key.S_CHAT_EVENTS, chatEvents);
                    SessionData.set(uuid, stagePrefix + Key.S_CHAT_EVENT_TRIGGERS, chatEventTriggers);
                    new QuestActionListPrompt(uuid).start();
                } else {
                    final LinkedList<String> chatEvents
                            = (LinkedList<String>) SessionData.get(uuid, stagePrefix + Key.S_CHAT_EVENTS);
                    final LinkedList<String> chatEventTriggers
                            = (LinkedList<String>) SessionData.get(uuid, stagePrefix + Key.S_CHAT_EVENT_TRIGGERS);
                    final String event = (String) SessionData.get(uuid, stagePrefix + Key.S_CHAT_TEMP_EVENT);
                    if (chatEvents != null && chatEventTriggers != null) {
                        chatEvents.add(event);
                        chatEventTriggers.add(input.trim());
                        SessionData.set(uuid, stagePrefix + Key.S_CHAT_EVENTS, chatEvents);
                        SessionData.set(uuid, stagePrefix + Key.S_CHAT_EVENT_TRIGGERS, chatEventTriggers);
                    }
                    new QuestActionListPrompt(uuid).start();
                }
            } else if (input != null && input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                new QuestActionListPrompt(uuid).start();
            } else {
                new QuestChatActionTriggerPrompt(uuid).start();
            }
        }
    }

    public class QuestCommandActionPrompt extends FabricQuestsEditorStringPrompt {

        public QuestCommandActionPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return FabricLang.get("stageEditorCommandEvents");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorCommandEventsPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final StringBuilder text = new StringBuilder(ChatFormatting.AQUA + "- " + getTitle() + " -\n");
            if (plugin.getLoadedActions().isEmpty()) {
                text.append(ChatFormatting.RED).append("- ").append(FabricLang.get("none"));
            } else {
                for (final Action a : plugin.getLoadedActions()) {
                    text.append(ChatFormatting.GREEN).append("- ").append(a.getName()).append("\n");
                }
            }
            return text.toString() + ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            if (input != null && !input.equalsIgnoreCase(FabricLang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                Action found = null;
                for (final Action a : plugin.getLoadedActions()) {
                    if (a.getName().equalsIgnoreCase(input)) {
                        found = a;
                        break;
                    }
                }
                if (found == null) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("stageEditorInvalidEvent")
                            .replace("<input>", input)));
                    new QuestCommandActionPrompt(uuid).start();
                } else {
                    SessionData.set(uuid, stagePrefix + Key.S_COMMAND_TEMP_EVENT, found.getName());
                    new QuestCommandActionTriggerPrompt(uuid).start();
                }
            } else if (input != null && input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                new QuestActionListPrompt(uuid).start();
            } else if (input != null && input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, stagePrefix + Key.S_COMMAND_EVENTS, null);
                SessionData.set(uuid, stagePrefix + Key.S_COMMAND_EVENT_TRIGGERS, null);
                sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("stageEditorCommandEventsCleared")));
                new QuestActionListPrompt(uuid).start();
            } else {
                new QuestCommandActionPrompt(uuid).start();
            }
        }
    }

    public class QuestCommandActionTriggerPrompt extends FabricQuestsEditorStringPrompt {

        public QuestCommandActionTriggerPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return FabricLang.get("stageEditorCommandTrigger");
        }

        @Override
        public String getQueryText() {
            final String tempEvent = (String) SessionData.get(uuid, stagePrefix + Key.S_COMMAND_TEMP_EVENT);
            if (tempEvent != null) {
                return FabricLang.get("stageEditorCommandEventsTriggerPrompt").replace("<event>", tempEvent)
                        .replace("<action>", tempEvent);
            } else {
                return FabricLang.get("stageEditorCommandEventsTriggerPrompt");
            }
        }

        @Override
        public @NotNull String getPromptText() {
            final String tempEvent = (String) SessionData.get(uuid, stagePrefix + Key.S_COMMAND_TEMP_EVENT);
            String text = ChatFormatting.GOLD + "- " + getTitle() + " -\n";
            if (tempEvent != null) {
                text += ChatFormatting.YELLOW + getQueryText()
                        .replaceFirst(tempEvent, ChatFormatting.AQUA + tempEvent + ChatFormatting.YELLOW);
            }
            return text;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void acceptInput(final String input) {
            if (input != null && !input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                if (SessionData.get(uuid, stagePrefix + Key.S_COMMAND_EVENTS) == null) {
                    final LinkedList<String> commandEvents = new LinkedList<>();
                    final LinkedList<String> commandEventTriggers = new LinkedList<>();
                    final String event = (String) SessionData.get(uuid, stagePrefix + Key.S_COMMAND_TEMP_EVENT);
                    commandEvents.add(event);
                    commandEventTriggers.add(input.trim());
                    SessionData.set(uuid, stagePrefix + Key.S_COMMAND_EVENTS, commandEvents);
                    SessionData.set(uuid, stagePrefix + Key.S_COMMAND_EVENT_TRIGGERS, commandEventTriggers);
                    new QuestActionListPrompt(uuid).start();
                } else {
                    final LinkedList<String> commandEvents
                            = (LinkedList<String>) SessionData.get(uuid, stagePrefix + Key.S_COMMAND_EVENTS);
                    final LinkedList<String> commandEventTriggers
                            = (LinkedList<String>) SessionData.get(uuid, stagePrefix + Key.S_COMMAND_EVENT_TRIGGERS);
                    final String event = (String) SessionData.get(uuid, stagePrefix + Key.S_COMMAND_TEMP_EVENT);
                    if (commandEvents != null && commandEventTriggers != null) {
                        commandEvents.add(event);
                        commandEventTriggers.add(input.trim());
                        SessionData.set(uuid, stagePrefix + Key.S_COMMAND_EVENTS, commandEvents);
                        SessionData.set(uuid, stagePrefix + Key.S_COMMAND_EVENT_TRIGGERS, commandEventTriggers);
                    }
                    new QuestActionListPrompt(uuid).start();
                }
            } else if (input != null && input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                new QuestActionListPrompt(uuid).start();
            } else {
                new QuestCommandActionTriggerPrompt(uuid).start();
            }
        }
    }

    public class QuestConditionListPrompt extends FabricQuestsEditorStringPrompt {

        public QuestConditionListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return FabricLang.get("stageEditorConditions");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorConditionsPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final StringBuilder text = new StringBuilder(ChatFormatting.AQUA + "- " + getTitle() + " -\n");
            if (plugin.getLoadedConditions().isEmpty()) {
                text.append(ChatFormatting.RED).append("- ").append(FabricLang.get("none")).append("\n");
            } else {
                for (final Condition c : plugin.getLoadedConditions()) {
                    text.append(ChatFormatting.GREEN).append("- ").append(c.getName()).append("\n");
                }
            }
            return text.toString() + ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            if (input != null && !input.equalsIgnoreCase(FabricLang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                Condition found = null;
                for (final Condition c : plugin.getLoadedConditions()) {
                    if (c.getName().equalsIgnoreCase(input)) {
                        found = c;
                        break;
                    }
                }
                if (found == null) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("stageEditorInvalidCondition")
                            .replace("<input>", input)));
                    new QuestConditionListPrompt(uuid).start();
                } else {
                    SessionData.set(uuid, stagePrefix + Key.S_CONDITION, found.getName());
                    new FabricQuestStageMainPrompt(stageNum, uuid).start();
                }
            } else if (input != null && input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                new FabricQuestStageMainPrompt(stageNum, uuid).start();
            } else if (input != null && input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, stagePrefix + Key.S_CONDITION, null);
                sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("stageEditorConditionCleared")));
                new FabricQuestStageMainPrompt(stageNum, uuid).start();
            } else {
                new QuestConditionListPrompt(uuid).start();
            }
        }
    }

    public class QuestDelayPrompt extends FabricQuestsEditorStringPrompt {

        public QuestDelayPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("timePrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            if (input != null) {
                if (input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                    new FabricQuestStageMainPrompt(stageNum, uuid).start();
                    return;
                }
                if (input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                    SessionData.set(uuid, stagePrefix + Key.S_DELAY, null);
                    sender.sendSystemMessage(Component.literal(ChatFormatting.GREEN + FabricLang.get("stageEditorDelayCleared")));
                    new FabricQuestStageMainPrompt(stageNum, uuid).start();
                    return;
                }
                long stageDelay = 1L;
                try {
                    final int i = Integer.parseInt(input);
                    stageDelay = i * 1000L;
                } catch (final NumberFormatException e) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("reqNotANumber")
                            .replace("<input>", input)));
                    new QuestDelayPrompt(uuid).start();
                    return;
                }
                if (stageDelay < 1000) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("invalidMinimum")
                            .replace("<number>", "1")));
                    new QuestDelayPrompt(uuid).start();
                    return;
                } else {
                    SessionData.set(uuid, stagePrefix + Key.S_DELAY, stageDelay);
                    new FabricQuestStageMainPrompt(stageNum, uuid).start();
                    return;
                }
            }
            new FabricQuestStageMainPrompt(stageNum, uuid).start();
        }
    }

    public class QuestDelayMessagePrompt extends FabricQuestsEditorStringPrompt {

        public QuestDelayMessagePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorDelayMessagePrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            if (input != null && !input.equalsIgnoreCase(FabricLang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, stagePrefix + Key.S_DELAY_MESSAGE, input);
                new FabricQuestStageMainPrompt(stageNum, uuid).start();
            } else if (input != null && input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, stagePrefix + Key.S_DELAY_MESSAGE, null);
                sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("stageEditorMessageCleared")));
                new FabricQuestStageMainPrompt(stageNum, uuid).start();
            } else {
                new QuestDelayMessagePrompt(uuid).start();
            }
        }
    }

    public class QuestStartMessagePrompt extends FabricQuestsEditorStringPrompt {

        public QuestStartMessagePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorStartMessagePrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            if (input != null && !input.equalsIgnoreCase(FabricLang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, stagePrefix + Key.S_START_MESSAGE, input);
                new FabricQuestStageMainPrompt(stageNum, uuid).start();
            } else if (input != null && input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, stagePrefix + Key.S_START_MESSAGE, null);
                sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("stageEditorMessageCleared")));
                new FabricQuestStageMainPrompt(stageNum, uuid).start();
            } else {
                new FabricQuestStageMainPrompt(stageNum, uuid).start();
            }
        }
    }

    public class QuestCompleteMessagePrompt extends FabricQuestsEditorStringPrompt {

        public QuestCompleteMessagePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorCompleteMessagePrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            if (input != null && !input.equalsIgnoreCase(FabricLang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, stagePrefix + Key.S_COMPLETE_MESSAGE, input);
                new FabricQuestStageMainPrompt(stageNum, uuid).start();
            } else if (input != null && input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, stagePrefix + Key.S_COMPLETE_MESSAGE, null);
                sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("stageEditorMessageCleared")));
                new FabricQuestStageMainPrompt(stageNum, uuid).start();
            } else {
                new FabricQuestStageMainPrompt(stageNum, uuid).start();
            }
        }
    }

    public class QuestStageDeletePrompt extends FabricQuestsEditorStringPrompt {

        public QuestStageDeletePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        public final int size = 2;

        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return null;
        }

        public ChatFormatting getNumberColor(final int number) {
            switch (number) {
            case 1:
                return ChatFormatting.GREEN;
            case 2:
                return ChatFormatting.RED;
            default:
                return null;
            }
        }

        public String getSelectionText(final int number) {
            switch (number) {
            case 1:
                return ChatFormatting.GREEN + FabricLang.get("yesWord");
            case 2:
                return ChatFormatting.RED + FabricLang.get("noWord");
            default:
                return null;
            }
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("confirmDelete");
        }

        @Override
        public @NotNull String getPromptText() {
            final StringBuilder text = new StringBuilder(ChatFormatting.YELLOW + getQueryText() + " ("
                    + ChatFormatting.RED + FabricLang.get("stageEditorStage") + " " + stageNum + ChatFormatting.YELLOW + ")\n"
                    + ChatFormatting.GOLD + "(" + FabricLang.get("stageEditorConfirmStageNote") + ")\n");
            for (int i = 1; i <= size; i++) {
                text.append(getNumberColor(i)).append(ChatFormatting.BOLD).append(i).append(ChatFormatting.RESET)
                        .append(" - ").append(getSelectionText(i)).append("\n");
            }
            return FabricQuestsIntegerPrompt.sendClickableSelection(text.toString(), plugin.getQuester(uuid));
        }

        @Override
        public void acceptInput(final String input) {
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            if (input != null && (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(FabricLang.get("yesWord")))) {
                new FabricQuestStageMenuPrompt(uuid).deleteStage(stageNum);
                sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("stageEditorDeleteSucces")));
                new FabricQuestStageMenuPrompt(uuid).start();
            } else if (input != null && (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(FabricLang.get("noWord")))) {
                new FabricQuestStageMainPrompt(stageNum, uuid).start();
            } else {
                sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("invalidOption")));
                new QuestStageDeletePrompt(uuid).start();
            }
        }
    }

    public class QuestCustomObjectiveModulePrompt extends FabricQuestsEditorStringPrompt {

        public QuestCustomObjectiveModulePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return FabricLang.get("stageEditorModules");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorModulePrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final StringBuilder text = new StringBuilder(ChatFormatting.LIGHT_PURPLE + getTitle() + "\n");
            if (plugin.getCustomObjectives().isEmpty()) {
                text.append(ChatFormatting.DARK_AQUA).append("https://pikamug.gitbook.io/quests/casual/modules\n");
                text.append(ChatFormatting.RED).append("(").append(FabricLang.get("stageEditorNoModules")).append(")\n");
            } else {
                for (final String name : plugin.getCustomObjectives().stream().map(CustomObjective::getModuleName)
                        .collect(Collectors.toCollection(TreeSet::new))) {
                    text.append(ChatFormatting.DARK_PURPLE).append("  - ").append(name).append("\n");
                }
            }
            return text.toString() + ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(@Nullable final String input) {
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            if (input != null && !input.equalsIgnoreCase(FabricLang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                String found = null;
                for (final CustomObjective co : plugin.getCustomObjectives()) {
                    if (co.getModuleName().equalsIgnoreCase(input)) {
                        found = co.getModuleName();
                        break;
                    }
                }
                if (found == null) {
                    for (final CustomObjective co : plugin.getCustomObjectives()) {
                        if (co.getModuleName().toLowerCase().contains(input.toLowerCase())) {
                            found = co.getModuleName();
                            break;
                        }
                    }
                }
                if (found != null) {
                    new QuestCustomObjectivesPrompt(found, uuid).start();
                    return;
                }
            } else if (input != null && input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                new FabricQuestStageMainPrompt(stageNum, uuid).start();
                return;
            } else if (input != null && input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, stagePrefix + Key.S_CUSTOM_OBJECTIVES, null);
                SessionData.set(uuid, stagePrefix + Key.S_CUSTOM_OBJECTIVES_DATA, null);
                SessionData.set(uuid, stagePrefix + Key.S_CUSTOM_OBJECTIVES_DATA_TEMP, null);
                sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("stageEditorCustomCleared")));
                new FabricQuestStageMainPrompt(stageNum, uuid).start();
                return;
            }
            sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("stageEditorModuleNotFound")));
            new QuestCustomObjectiveModulePrompt(uuid).start();
        }
    }

    public class QuestCustomObjectivesPrompt extends FabricQuestsEditorStringPrompt {

        private final String moduleName;

        public QuestCustomObjectivesPrompt(final String moduleName, final UUID uuid) {
            super(uuid);
            this.moduleName = moduleName;
        }

        public String getModuleName() {
            return moduleName;
        }

        @Override
        public String getTitle() {
            return FabricLang.get("customObjectivesTitle");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorCustomPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final StringBuilder text = new StringBuilder(ChatFormatting.LIGHT_PURPLE + "- " + getTitle()
                    + " -\n");
            if (plugin.getCustomObjectives().isEmpty()) {
                text.append(ChatFormatting.DARK_AQUA).append("https://pikamug.gitbook.io/quests/casual/modules\n");
                text.append(ChatFormatting.RED).append("(").append(FabricLang.get("stageEditorNoModules")).append(")\n");
            } else {
                for (final CustomObjective co : plugin.getCustomObjectives()) {
                    if (co.getModuleName().equals(moduleName)) {
                        text.append(ChatFormatting.DARK_PURPLE).append("  - ").append(co.getName()).append("\n");
                    }
                }
            }
            return text.toString() + ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final String input) {
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            if (input != null && !input.equalsIgnoreCase(FabricLang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
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
                    if (SessionData.get(uuid, stagePrefix + Key.S_CUSTOM_OBJECTIVES) != null) {
                        final LinkedList<String> list = (LinkedList<String>) SessionData.get(uuid, stagePrefix
                                + Key.S_CUSTOM_OBJECTIVES);
                        final LinkedList<Entry<String, Object>> dataMapList
                                = (LinkedList<Entry<String, Object>>) SessionData.get(uuid, stagePrefix
                                + Key.S_CUSTOM_OBJECTIVES_DATA);
                        final LinkedList<Integer> countList = (LinkedList<Integer>) SessionData.get(uuid, stagePrefix
                                + Key.S_CUSTOM_OBJECTIVES_COUNT);
                        if (list != null && !list.contains(found.getName()) && dataMapList != null
                                && countList != null) {
                            list.add(found.getName());
                            dataMapList.addAll(found.getData());
                            countList.add(-999);
                            SessionData.set(uuid, stagePrefix + Key.S_CUSTOM_OBJECTIVES, list);
                            SessionData.set(uuid, stagePrefix + Key.S_CUSTOM_OBJECTIVES_DATA, dataMapList);
                        } else {
                            sender.sendSystemMessage(Component.literal(ChatFormatting.RED
                                    + FabricLang.get("stageEditorCustomAlreadyAdded")));
                            new QuestCustomObjectivesPrompt(moduleName, uuid).start();
                            return;
                        }
                    } else {
                        final LinkedList<Integer> countList = new LinkedList<>();
                        final LinkedList<Entry<String, Object>> dataMapList = new LinkedList<>(found.getData());
                        countList.add(-999);
                        final LinkedList<String> list = new LinkedList<>();
                        list.add(found.getName());
                        SessionData.set(uuid, stagePrefix + Key.S_CUSTOM_OBJECTIVES, list);
                        SessionData.set(uuid, stagePrefix + Key.S_CUSTOM_OBJECTIVES_DATA, dataMapList);
                        SessionData.set(uuid, stagePrefix + Key.S_CUSTOM_OBJECTIVES_COUNT, countList);
                    }
                    if (found.canShowCount()) {
                        new QuestCustomObjectiveCountPrompt(uuid).start();
                        return;
                    }
                    if (!found.getData().isEmpty()) {
                        SessionData.set(uuid, stagePrefix + Key.S_CUSTOM_OBJECTIVES_DATA_DESCRIPTIONS, found
                                .getDescriptions());
                        new QuestObjectiveCustomDataListPrompt(uuid).start();
                        return;
                    }
                } else {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("stageEditorModuleNotFound")));
                    new QuestCustomObjectivesPrompt(moduleName, uuid).start();
                    return;
                }
            } else if (input != null && input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, stagePrefix + Key.S_CUSTOM_OBJECTIVES, null);
                SessionData.set(uuid, stagePrefix + Key.S_CUSTOM_OBJECTIVES_DATA, null);
                SessionData.set(uuid, stagePrefix + Key.S_CUSTOM_OBJECTIVES_DATA_TEMP, null);
                sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("stageEditorCustomCleared")));
            }
            new FabricQuestStageMainPrompt(stageNum, uuid).start();
        }
    }

    private class QuestCustomObjectiveCountPrompt extends FabricQuestsEditorStringPrompt {

        public QuestCustomObjectiveCountPrompt(final UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return null;
        }

        @Override
        public @NotNull String getPromptText() {
            final StringBuilder text = new StringBuilder(ChatFormatting.GOLD + "- ");
            @SuppressWarnings("unchecked")
            final LinkedList<String> list
                    = (LinkedList<String>) SessionData.get(uuid, stagePrefix + Key.S_CUSTOM_OBJECTIVES);
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
                    text.append(ChatFormatting.YELLOW).append(found.getCountPrompt());
                }
            }
            return text.toString();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final String input) {
            try {
                final int num = Integer.parseInt(input);
                final LinkedList<Integer> counts
                        = (LinkedList<Integer>) SessionData.get(uuid, stagePrefix + Key.S_CUSTOM_OBJECTIVES_COUNT);
                final LinkedList<String> list = (LinkedList<String>) SessionData.get(uuid, stagePrefix
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
                        SessionData.set(uuid, stagePrefix
                                + Key.S_CUSTOM_OBJECTIVES_DATA_DESCRIPTIONS, found.getDescriptions());
                        new QuestObjectiveCustomDataListPrompt(uuid).start();
                    } else {
                        new FabricQuestStageMainPrompt(stageNum, uuid).start();
                    }
                    return;
                }
            } catch (final NumberFormatException e) {
                final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
                if (sender != null) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("reqNotANumber")
                            .replace("<input>", input)));
                }
                new QuestCustomObjectiveCountPrompt(uuid).start();
                return;
            }
            new FabricQuestStageMainPrompt(stageNum, uuid).start();
        }
    }

    private class QuestObjectiveCustomDataListPrompt extends FabricQuestsEditorStringPrompt {

        public QuestObjectiveCustomDataListPrompt(final UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return null;
        }

        @Override
        @SuppressWarnings("unchecked")
        public @NotNull String getPromptText() {
            final StringBuilder text = new StringBuilder(ChatFormatting.GOLD + "- ");
            final LinkedList<String> list = (LinkedList<String>) SessionData.get(uuid, stagePrefix
                    + Key.S_CUSTOM_OBJECTIVES);
            final LinkedList<Entry<String, Object>> dataMapList
                    = (LinkedList<Entry<String, Object>>) SessionData.get(uuid, stagePrefix
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
                                text.append(ChatFormatting.BLUE).append(ChatFormatting.BOLD).append(index).append(ChatFormatting.RESET)
                                        .append(ChatFormatting.YELLOW).append(" - ").append(dataMap.getKey());
                                if (currentData.getValue() != null && !currentData.getValue().toString().trim().isEmpty()) {
                                    text.append(ChatFormatting.GRAY).append(" (").append(ChatFormatting.AQUA)
                                            .append(currentData.getValue().toString()).append(ChatFormatting.GRAY).append(")\n");
                                } else {
                                    text.append(ChatFormatting.GRAY).append(" (").append(FabricLang.get("noneSet"))
                                            .append(ChatFormatting.GRAY).append(")\n");
                                }
                                index++;
                            }
                        }
                    }
                }
                text.append(ChatFormatting.GREEN).append(ChatFormatting.BOLD).append(index).append(ChatFormatting.YELLOW)
                        .append(" - ").append(FabricLang.get("done"));
            }
            return sendClickableSelection(text.toString(), plugin.getQuester(uuid));
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final String input) {
            final LinkedList<String> list = (LinkedList<String>) SessionData.get(uuid, stagePrefix
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
                    FabricQuestsPlugin.LOGGER.error("Could not find custom objective following input: {}", input);
                    new QuestObjectiveCustomDataListPrompt(uuid).start();
                    return;
                }
                final LinkedList<Entry<String, Object>> dataMapList = found.getData();

                int numInput = 0;
                try {
                    numInput = Integer.parseInt(input);
                } catch (final NumberFormatException nfe) {
                    new QuestObjectiveCustomDataListPrompt(uuid).start();
                    return;
                }
                if (numInput < 1 || numInput > dataMapList.size() + 1) {
                    new QuestObjectiveCustomDataListPrompt(uuid).start();
                    return;
                }
                if (numInput < dataMapList.size() + 1) {
                    final LinkedList<String> dataMapKeys = new LinkedList<>();
                    for (final Entry<String, Object> dataMap : dataMapList) {
                        dataMapKeys.add(dataMap.getKey());
                    }
                    final String selectedKey = dataMapKeys.get(numInput - 1);
                    SessionData.set(uuid, stagePrefix + Key.S_CUSTOM_OBJECTIVES_DATA_TEMP, selectedKey);
                    new QuestObjectiveCustomDataPrompt(uuid).start();
                } else {
                    final LinkedList<Entry<String, Object>> dataMaps
                            = (LinkedList<Entry<String, Object>>) SessionData.get(uuid, stagePrefix
                            + Key.S_CUSTOM_OBJECTIVES_DATA);
                    if (dataMaps != null) {
                        for (final Entry<String, Object> dataMap : dataMaps) {
                            if (dataMap.getValue() == null) {
                                new QuestObjectiveCustomDataListPrompt(uuid).start();
                            }
                        }
                    }
                    SessionData.set(uuid, stagePrefix + Key.S_CUSTOM_OBJECTIVES_DATA_DESCRIPTIONS, null);
                    new FabricQuestStageMainPrompt(stageNum, uuid).start();
                }
                return;
            }
            new FabricQuestStageMainPrompt(stageNum, uuid).start();
        }
    }

    private class QuestObjectiveCustomDataPrompt extends FabricQuestsEditorStringPrompt {

        public QuestObjectiveCustomDataPrompt(final UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return null;
        }

        @Override
        @SuppressWarnings("unchecked")
        public @NotNull String getPromptText() {
            String text = "";
            final String temp = (String) SessionData.get(uuid, stagePrefix + Key.S_CUSTOM_OBJECTIVES_DATA_TEMP);
            final Map<String, String> descriptions
                    = (Map<String, String>) SessionData.get(uuid, stagePrefix
                    + Key.S_CUSTOM_OBJECTIVES_DATA_DESCRIPTIONS);
            if (descriptions != null && descriptions.get(temp) != null) {
                text += ChatFormatting.GOLD + descriptions.get(temp) + "\n";
            }
            String msg = FabricLang.get("stageEditorCustomDataPrompt");
            msg = msg.replace("<data>", temp);
            text += ChatFormatting.YELLOW + msg;
            return text;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final String input) {
            final LinkedList<Entry<String, Object>> dataMapList
                    = (LinkedList<Entry<String, Object>>) SessionData.get(uuid, stagePrefix
                    + Key.S_CUSTOM_OBJECTIVES_DATA);
            final LinkedList<Entry<String, Object>> promptList = new LinkedList<>();
            final String temp = (String) SessionData.get(uuid, stagePrefix + Key.S_CUSTOM_OBJECTIVES_DATA_TEMP);
            if (dataMapList != null) {
                for (final Entry<String, Object> dataMap : dataMapList) {
                    if (dataMap.getKey().equals(temp)) {
                        promptList.add(new AbstractMap.SimpleEntry<>(dataMap.getKey(), input));
                    } else {
                        promptList.add(new AbstractMap.SimpleEntry<>(dataMap.getKey(), dataMap.getValue()));
                    }
                }
            }
            SessionData.set(uuid, stagePrefix + Key.S_CUSTOM_OBJECTIVES_DATA, promptList);
            SessionData.set(uuid, stagePrefix + Key.S_CUSTOM_OBJECTIVES_DATA_TEMP, null);
            new QuestObjectiveCustomDataListPrompt(uuid).start();
        }
    }
}
