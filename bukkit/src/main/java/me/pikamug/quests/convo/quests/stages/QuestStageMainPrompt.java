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
import me.pikamug.quests.convo.QuestsIntegerPrompt;
import me.pikamug.quests.convo.generic.OverridePrompt;
import me.pikamug.quests.convo.quests.QuestsEditorIntegerPrompt;
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
import me.pikamug.quests.util.SessionData;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
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

public class QuestStageMainPrompt extends QuestsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final BukkitQuestsPlugin plugin;
    private final int stageNum;
    private final String stagePrefix;
    private final String classPrefix;
    private boolean hasObjective = false;
    private final int size = 17;

    public QuestStageMainPrompt(final int stageNum, final UUID uuid) {
        super(uuid);
        this.uuid = uuid;
        this.plugin = BukkitQuestsPlugin.getInstance();
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
        return SessionData.get(uuid, Key.Q_NAME) + " | " + BukkitLang.get("stageEditorStage") + " " + stageNum;
    }
    
    @Override
    public ChatColor getNumberColor(final int number) {
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
            if (SessionData.get(uuid, stagePrefix + Key.S_DELAY) == null) {
                return ChatColor.GRAY;
            } else {
                return ChatColor.BLUE;
            }
        case 13:
            if (SessionData.get(uuid, stagePrefix + Key.S_START_MESSAGE) == null) {
                if (!hasObjective) {
                    return ChatColor.GRAY;
                } else {
                    return ChatColor.BLUE;
                }
            } else {
                return ChatColor.BLUE;
            }
        case 14:
            if (SessionData.get(uuid, stagePrefix + Key.S_COMPLETE_MESSAGE) == null) {
                if (!hasObjective) {
                    return ChatColor.GRAY;
                } else {
                    return ChatColor.BLUE;
                }
            } else {
                return ChatColor.BLUE;
            }
        case 15:
            if (SessionData.get(uuid, stagePrefix + Key.S_OVERRIDE_DISPLAY) == null) {
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
    public String getSelectionText(final int number) {
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
            if (SessionData.get(uuid, stagePrefix + Key.S_DELAY) == null) {
                return ChatColor.GRAY + BukkitLang.get("stageEditorDelayMessage");
            } else {
                return ChatColor.YELLOW + BukkitLang.get("stageEditorDelayMessage");
            }
        case 13:
            if (SessionData.get(uuid, stagePrefix + Key.S_START_MESSAGE) == null) {
                if (!hasObjective) {
                    return ChatColor.GRAY + BukkitLang.get("stageEditorStartMessage");
                } else {
                    return ChatColor.YELLOW + BukkitLang.get("stageEditorStartMessage");
                }
            } else {
                return ChatColor.YELLOW + BukkitLang.get("stageEditorStartMessage");
            }
        case 14:
            if (SessionData.get(uuid, stagePrefix + Key.S_COMPLETE_MESSAGE) == null) {
                if (!hasObjective) {
                    return ChatColor.GRAY + BukkitLang.get("stageEditorCompleteMessage");
                } else {
                    return ChatColor.YELLOW + BukkitLang.get("stageEditorCompleteMessage");
                }
            } else {
                return ChatColor.YELLOW + BukkitLang.get("stageEditorCompleteMessage");
            }
        case 15:
            if (SessionData.get(uuid, stagePrefix + Key.S_OVERRIDE_DISPLAY) == null) {
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
    public String getAdditionalText(final int number) {
        switch (number) {
        case 1:
            if (SessionData.get(uuid, stagePrefix + Key.S_BREAK_NAMES) == null
                    && SessionData.get(uuid, stagePrefix + Key.S_DAMAGE_NAMES) == null
                    && SessionData.get(uuid, stagePrefix + Key.S_PLACE_NAMES) == null
                    && SessionData.get(uuid, stagePrefix + Key.S_USE_NAMES) == null
                    && SessionData.get(uuid, stagePrefix + Key.S_CUT_NAMES) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                return "";
            }
        case 2:
            if (SessionData.get(uuid, stagePrefix + Key.S_CRAFT_ITEMS) == null
                    && SessionData.get(uuid, stagePrefix + Key.S_SMELT_ITEMS) == null
                    && SessionData.get(uuid, stagePrefix + Key.S_ENCHANT_ITEMS) == null
                    && SessionData.get(uuid, stagePrefix + Key.S_BREW_ITEMS) == null
                    && SessionData.get(uuid, stagePrefix + Key.S_CONSUME_ITEMS) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                return "";
            }
        case 3:
            if (SessionData.get(uuid, stagePrefix + Key.S_DELIVERY_NPCS) == null
                    && SessionData.get(uuid, stagePrefix + Key.S_NPCS_TO_TALK_TO) == null
                    && SessionData.get(uuid, stagePrefix + Key.S_NPCS_TO_KILL) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                return "";
            }
        case 4:
            if (SessionData.get(uuid, stagePrefix + Key.S_MOB_TYPES) == null
                    && SessionData.get(uuid, stagePrefix + Key.S_TAME_TYPES) == null
                    && SessionData.get(uuid, stagePrefix + Key.S_FISH) == null
                    && SessionData.get(uuid, stagePrefix + Key.S_COW_MILK) == null
                    && SessionData.get(uuid, stagePrefix + Key.S_SHEAR_COLORS) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                return "";
            }
        case 5:
            if (SessionData.get(uuid, stagePrefix + Key.S_PLAYER_KILL) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final Integer players = (Integer) SessionData.get(uuid, stagePrefix + Key.S_PLAYER_KILL);
                return ChatColor.GRAY + "(" + ChatColor.AQUA + players + " " + BukkitLang.get("stageEditorPlayers")
                        + ChatColor.GRAY + ")";
            }
        case 6:
            if (SessionData.get(uuid, stagePrefix + Key.S_REACH_LOCATIONS) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
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
            if (SessionData.get(uuid, stagePrefix + Key.S_PASSWORD_PHRASES) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> passPhrases
                        = (LinkedList<String>) SessionData.get(uuid, stagePrefix + Key.S_PASSWORD_PHRASES);
                final LinkedList<String> passDisplays 
                        = (LinkedList<String>) SessionData.get(uuid, stagePrefix + Key.S_PASSWORD_DISPLAYS);
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
            if (SessionData.get(uuid, stagePrefix + Key.S_CUSTOM_OBJECTIVES) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> customObj
                        = (LinkedList<String>) SessionData.get(uuid, stagePrefix + Key.S_CUSTOM_OBJECTIVES);
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
            } else if (SessionData.get(uuid, stagePrefix + Key.S_START_EVENT) == null
                    && SessionData.get(uuid, stagePrefix + Key.S_FINISH_EVENT) == null
                    && SessionData.get(uuid, stagePrefix + Key.S_DEATH_EVENT) == null
                    && SessionData.get(uuid, stagePrefix + Key.S_DISCONNECT_EVENT) == null
                    && SessionData.get(uuid, stagePrefix + Key.S_CHAT_EVENTS) == null
                    && SessionData.get(uuid, stagePrefix + Key.S_COMMAND_EVENTS) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                return "";
            }
        case 10:
            if (!hasObjective) {
                return ChatColor.GRAY + "(" + BukkitLang.get("stageEditorOptional") + ")";
            } else if (SessionData.get(uuid, stagePrefix + Key.S_CONDITION) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                return "";
            }
        case 11:
            if (!hasObjective) {
                return ChatColor.GRAY + "(" + BukkitLang.get("stageEditorOptional") + ")";
            } else {
                if (SessionData.get(uuid, stagePrefix + Key.S_DELAY) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final Long time = (Long) SessionData.get(uuid, stagePrefix + Key.S_DELAY);
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
            } else if (SessionData.get(uuid, stagePrefix + Key.S_DELAY) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noDelaySet") + ")";
            } else if (SessionData.get(uuid, stagePrefix + Key.S_DELAY_MESSAGE) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + "\"" 
                        + SessionData.get(uuid, stagePrefix + Key.S_DELAY_MESSAGE) + "\"" + ChatColor.GRAY + ")";
            }
        case 13:
            if (SessionData.get(uuid, stagePrefix + Key.S_START_MESSAGE) == null) {
                if (!hasObjective) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("stageEditorOptional") + ")";
                } else {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                }
            } else {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + "\"" 
                        + SessionData.get(uuid, stagePrefix + Key.S_START_MESSAGE) + "\"" + ChatColor.GRAY + ")";
            }
        case 14:
            if (SessionData.get(uuid, stagePrefix + Key.S_COMPLETE_MESSAGE) == null) {
                if (!hasObjective) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("stageEditorOptional") + ")";
                } else {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                }
            } else {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + "\"" 
                        + SessionData.get(uuid, stagePrefix + Key.S_COMPLETE_MESSAGE) + "\"" + ChatColor.GRAY + ")";
            }
        case 15:
            if (SessionData.get(uuid, stagePrefix + Key.S_OVERRIDE_DISPLAY) == null) {
                if (!hasObjective) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("stageEditorOptional") + ")";
                } else {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                }
            } else {
                final StringBuilder text = new StringBuilder();
                if (stagePrefix != null) {
                    final List<String> overrides
                            = (List<String>) SessionData.get(uuid, stagePrefix + Key.S_OVERRIDE_DISPLAY);
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
    
    @Override
    @SuppressWarnings("unchecked")
    public @NotNull String getPromptText() {
        final String input = (String) SessionData.get(uuid, classPrefix + "-override");
        if (input != null && !input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
            if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
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

        final BukkitQuestsEditorPostOpenNumericPromptEvent event
                = new BukkitQuestsEditorPostOpenNumericPromptEvent(uuid, this);
        plugin.getServer().getPluginManager().callEvent(event);
        
        final StringBuilder text = new StringBuilder(ChatColor.LIGHT_PURPLE + "- " + ChatColor.AQUA
                + getTitle().replaceFirst(" \\| ", ChatColor.LIGHT_PURPLE + " | ") + " -");
        for (int i = 1; i <= size; i++) {
            text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                    .append(ChatColor.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                    .append(getAdditionalText(i));
        }
        return text.toString();
    }

    public void acceptInput(final Number input) {
        final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
        switch (input.intValue()) {
        case 1:
            new QuestBlocksPrompt(stageNum, uuid).start();
            break;
        case 2:
            new QuestItemsPrompt(stageNum, uuid).start();
            break;
        case 3:
            new QuestNpcsPrompt(stageNum, uuid).start();
            break;
        case 4:
            new QuestMobsPrompt(stageNum, uuid).start();
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
                sender.sendMessage(ChatColor.RED + BukkitLang.get("invalidOption"));
                new QuestStageMainPrompt(stageNum, uuid).start();
            }
            break;
        case 10:
            if (hasObjective) {
                new QuestConditionListPrompt(uuid).start();
            } else {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("invalidOption"));
                new QuestStageMainPrompt(stageNum, uuid).start();
            }
            break;
        case 11:
            if (hasObjective) {
                new QuestDelayPrompt(uuid).start();
            } else {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("invalidOption"));
                new QuestStageMainPrompt(stageNum, uuid).start();
            }
            break;
        case 12:
            if (SessionData.get(uuid, stagePrefix + Key.S_DELAY) == null) {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("stageEditorNoDelaySet"));
                new QuestStageMainPrompt(stageNum, uuid).start();
            } else {
                new QuestDelayMessagePrompt(uuid).start();
            }
            break;
        case 13:
            if (hasObjective) {
                new QuestStartMessagePrompt(uuid).start();
            } else {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("invalidOption"));
                new QuestStageMainPrompt(stageNum, uuid).start();
            }
            break;
        case 14:
            if (hasObjective) {
                new QuestCompleteMessagePrompt(uuid).start();
            } else {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("invalidOption"));
                new QuestStageMainPrompt(stageNum, uuid).start();
            }
            break;
        case 15:
            if (hasObjective) {
                new OverridePrompt.Builder()
                        .source(this)
                        .promptText(BukkitLang.get("overrideCreateEnter"))
                        .build()
                        .start();
            } else {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("invalidOption"));
                new QuestStageMainPrompt(stageNum, uuid).start();
            }
            break;
        case 16:
            new QuestStageDeletePrompt(uuid).start();
            break;
        case 17:
            new QuestStageMenuPrompt(uuid).start();
            break;
        default:
            new QuestStageMainPrompt(stageNum, uuid).start();
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
    
    public class QuestKillPlayerPrompt extends QuestsEditorStringPrompt {
        
        public QuestKillPlayerPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("stageEditorKillPlayerPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (input != null && !input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i < 0) {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("stageEditorPositiveAmount"));
                        new QuestKillPlayerPrompt(uuid).start();
                    } else if (i > 0) {
                        SessionData.set(uuid, stagePrefix + Key.S_PLAYER_KILL, i);
                    }
                } catch (final NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                            .replace("<input>", input));
                    new QuestKillPlayerPrompt(uuid).start();
                }
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, stagePrefix + Key.S_PLAYER_KILL, null);
            }
            new QuestStageMainPrompt(stageNum, uuid).start();
        }
    }
    
    public class QuestReachListPrompt extends QuestsEditorIntegerPrompt {

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
            return BukkitLang.get("stageEditorReachLocs");
        }
        
        @Override
        public ChatColor getNumberColor(final int number) {
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
        public String getSelectionText(final int number) {
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
        public String getAdditionalText(final int number) {
            switch(number) {
            case 1:
                if (SessionData.get(uuid, stagePrefix + Key.S_REACH_LOCATIONS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> locations
                            = (List<String>) SessionData.get(uuid, stagePrefix + Key.S_REACH_LOCATIONS);
                    if (locations != null) {
                        for (final String s : locations) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.DARK_AQUA)
                                    .append(s);
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (SessionData.get(uuid, stagePrefix + Key.S_REACH_LOCATIONS_RADIUS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<Integer> radius
                            = (List<Integer>) SessionData.get(uuid, stagePrefix + Key.S_REACH_LOCATIONS_RADIUS);
                    if (radius != null) {
                        for (final Integer i : radius) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(i);
                        }
                    }
                    return text.toString();
                }
            case 3:
                if (SessionData.get(uuid, stagePrefix + Key.S_REACH_LOCATIONS_NAMES) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> names
                            = (List<String>) SessionData.get(uuid, stagePrefix + Key.S_REACH_LOCATIONS_NAMES);
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
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenNumericPromptEvent event
                    = new BukkitQuestsEditorPostOpenNumericPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle() + " -");
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                        .append(getAdditionalText(i));
            }
            return text.toString();
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public void acceptInput(final Number input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            switch(input.intValue()) {
            case 1:
                if (sender instanceof Player) {
                    final ConcurrentHashMap<UUID, Block> temp = plugin.getQuestFactory().getSelectedReachLocations();
                    if (BukkitMiscUtil.getWorlds().isEmpty()) {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("unknownError"));
                        new QuestReachListPrompt(uuid).start();
                    }
                    temp.put(((Player) sender).getUniqueId(),
                            Bukkit.getWorlds().get(0).getBlockAt(0,0,0));
                    plugin.getQuestFactory().setSelectedReachLocations(temp);
                    new QuestReachLocationPrompt(uuid).start();
                } else {
                    sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("consoleError"));
                    new QuestReachListPrompt(uuid).start();
                }
                break;
            case 2:
                if (SessionData.get(uuid, stagePrefix + Key.S_REACH_LOCATIONS) == null) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("stageEditorNoLocations"));
                    new QuestReachListPrompt(uuid).start();
                } else {
                    new QuestReachRadiiPrompt(uuid).start();
                }
                break;
            case 3:
                if (SessionData.get(uuid, stagePrefix + Key.S_REACH_LOCATIONS) == null) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("stageEditorNoLocations"));
                    new QuestReachListPrompt(uuid).start();
                } else {
                    new QuestReachNamesPrompt(uuid).start();
                }
                break;
            case 4:
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorObjectiveCleared"));
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
                    new QuestStageMainPrompt(stageNum, uuid).start();
                } else {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("listsNotSameSize"));
                    new QuestReachListPrompt(uuid).start();
                }
                break;
            default:
                new QuestStageMainPrompt(stageNum, uuid).start();
                break;
            }
        }
    }

    public class QuestReachLocationPrompt extends QuestsEditorStringPrompt {
        
        public QuestReachLocationPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("stageEditorReachLocationPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final String input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            final Player player = (Player) sender;
            if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdAdd"))) {
                final Block block = plugin.getQuestFactory().getSelectedReachLocations().get(player.getUniqueId());
                if (block != null) {
                    final Location loc = block.getLocation();
                    final LinkedList<String> locations
                            = SessionData.get(uuid, stagePrefix + Key.S_REACH_LOCATIONS) != null
                            ? (LinkedList<String>) SessionData.get(uuid, stagePrefix + Key.S_REACH_LOCATIONS)
                            : new LinkedList<>();
                    if (locations != null) {
                        locations.add(BukkitConfigUtil.getLocationInfo(loc));
                    }
                    SessionData.set(uuid, stagePrefix + Key.S_REACH_LOCATIONS, locations);

                    LinkedList<Integer> amounts = new LinkedList<>();
                    LinkedList<String> names = new LinkedList<>();
                    if (SessionData.get(uuid, stagePrefix + Key.S_REACH_LOCATIONS_RADIUS) != null) {
                        amounts = (LinkedList<Integer>) SessionData.get(uuid, stagePrefix + Key.S_REACH_LOCATIONS_RADIUS);
                    }
                    if (SessionData.get(uuid, stagePrefix + Key.S_REACH_LOCATIONS_NAMES) != null) {
                        names = (LinkedList<String>) SessionData.get(uuid, stagePrefix + Key.S_REACH_LOCATIONS_NAMES);
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
                    SessionData.set(uuid, stagePrefix + Key.S_REACH_LOCATIONS_RADIUS, amounts);
                    SessionData.set(uuid, stagePrefix + Key.S_REACH_LOCATIONS_NAMES, names);
                } else {
                    player.sendMessage(ChatColor.RED + BukkitLang.get("stageEditorNoBlockSelected"));
                    new QuestReachLocationPrompt(uuid).start();
                }
                final ConcurrentHashMap<UUID, Block> temp = plugin.getQuestFactory().getSelectedReachLocations();
                temp.remove(player.getUniqueId());
                plugin.getQuestFactory().setSelectedReachLocations(temp);
                new QuestReachListPrompt(uuid).start();
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final ConcurrentHashMap<UUID, Block> temp = plugin.getQuestFactory().getSelectedReachLocations();
                temp.remove(player.getUniqueId());
                plugin.getQuestFactory().setSelectedReachLocations(temp);
                new QuestReachListPrompt(uuid).start();
            } else {
                new QuestReachLocationPrompt(uuid).start();
            }
        }
    }

    public class QuestReachRadiiPrompt extends QuestsEditorStringPrompt {
        
        public QuestReachRadiiPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("stageEditorReachLocationRadiiPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (input != null && !input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final LinkedList<Integer> radii = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    try {
                        final int i = Integer.parseInt(s);
                        if (i < 1) {
                            sender.sendMessage(ChatColor.RED + BukkitLang.get("invalidMinimum")
                                    .replace("<number>", "1"));
                            new QuestReachRadiiPrompt(uuid).start();
                        }
                        radii.add(i);
                    } catch (final NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                                .replace("<input>", input));
                        new QuestReachRadiiPrompt(uuid).start();
                    }
                }
                SessionData.set(uuid, stagePrefix + Key.S_REACH_LOCATIONS_RADIUS, radii);
            }
            new QuestReachListPrompt(uuid).start();
        }
    }

    public class QuestReachNamesPrompt extends QuestsEditorStringPrompt {
        
        public QuestReachNamesPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("stageEditorReachLocationNamesPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input != null && !input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final LinkedList<String> locNames = new LinkedList<>(Arrays.asList(input.split(BukkitLang.get("charSemi"))));
                SessionData.set(uuid, stagePrefix + Key.S_REACH_LOCATIONS_NAMES, locNames);
            }
            new QuestReachListPrompt(uuid).start();
        }
    }

    public class QuestPasswordListPrompt extends QuestsEditorIntegerPrompt {

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
            return BukkitLang.get("stageEditorPassword");
        }
        
        @Override
        public ChatColor getNumberColor(final int number) {
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
        public String getSelectionText(final int number) {
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
        public String getAdditionalText(final int number) {
            switch(number) {
            case 1:
                if (SessionData.get(uuid, stagePrefix + Key.S_PASSWORD_DISPLAYS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> displays = (List<String>) SessionData.get(uuid, stagePrefix
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
                if (SessionData.get(uuid, stagePrefix + Key.S_PASSWORD_PHRASES) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> phrases = (List<String>) SessionData.get(uuid, stagePrefix
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
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenNumericPromptEvent event
                    = new BukkitQuestsEditorPostOpenNumericPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle() + " -");
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                        .append(getAdditionalText(i));
            }
            return text.toString();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final Number input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            switch(input.intValue()) {
            case 1:
                new QuestPasswordDisplayPrompt(uuid).start();
            case 2:
                if (SessionData.get(uuid, stagePrefix + Key.S_PASSWORD_DISPLAYS) == null) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("stageEditorMustSetPasswordDisplays"));
                    new QuestPasswordListPrompt(uuid).start();
                } else {
                    new QuestPasswordPhrasePrompt(uuid).start();
                }
            case 3:
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorObjectiveCleared"));
                SessionData.set(uuid, stagePrefix + Key.S_PASSWORD_DISPLAYS, null);
                SessionData.set(uuid, stagePrefix + Key.S_PASSWORD_PHRASES, null);
                new QuestPasswordListPrompt(uuid).start();
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
                    new QuestStageMainPrompt(stageNum, uuid).start();
                } else {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("listsNotSameSize"));
                    new QuestPasswordListPrompt(uuid).start();
                }
            default:
                new QuestStageMainPrompt(stageNum, uuid).start();
            }
        }
    }

    public class QuestPasswordDisplayPrompt extends QuestsEditorStringPrompt {

        public QuestPasswordDisplayPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("stageEditorPasswordDisplayPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText() + "\n";
        }

        @Override
        public void acceptInput(final String input) {
            if (input != null && !input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                if (SessionData.get(uuid, stagePrefix + Key.S_PASSWORD_DISPLAYS) != null) {
                    @SuppressWarnings("unchecked")
                    final List<String> displays = (List<String>) SessionData.get(uuid, stagePrefix
                            + Key.S_PASSWORD_DISPLAYS);
                    if (displays != null) {
                        displays.addAll(Arrays.asList(input.split(BukkitLang.get("charSemi"))));
                    }
                    SessionData.set(uuid, stagePrefix + Key.S_PASSWORD_DISPLAYS, displays);
                } else {
                    final List<String> displays = new LinkedList<>(Arrays.asList(input.split(BukkitLang.get("charSemi"))));
                    SessionData.set(uuid, stagePrefix + Key.S_PASSWORD_DISPLAYS, displays);
                }
            }
            new QuestPasswordListPrompt(uuid).start();
        }
    }

    public class QuestPasswordPhrasePrompt extends QuestsEditorStringPrompt {
        
        public QuestPasswordPhrasePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("stageEditorPasswordPhrasePrompt");
        }


        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText() + "\n";
        }

        @Override
        public void acceptInput(final String input) {
            if (input != null && !input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                if (SessionData.get(uuid, stagePrefix + Key.S_PASSWORD_PHRASES) != null) {
                    @SuppressWarnings("unchecked")
                    final List<String> phrases = (List<String>) SessionData.get(uuid, stagePrefix
                            + Key.S_PASSWORD_PHRASES);
                    if (phrases != null) {
                        phrases.addAll(Arrays.asList(input.split(BukkitLang.get("charSemi"))));
                    }
                    SessionData.set(uuid, stagePrefix + Key.S_PASSWORD_PHRASES, phrases);
                } else {
                    final List<String> phrases = new LinkedList<>(Arrays.asList(input.split(BukkitLang.get("charSemi"))));
                    SessionData.set(uuid, stagePrefix + Key.S_PASSWORD_PHRASES, phrases);
                }
            }
            new QuestPasswordListPrompt(uuid).start();
        }
    }

    public class QuestActionListPrompt extends QuestsEditorIntegerPrompt {

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
            return BukkitLang.get("stageEditorStageEvents");
        }
        
        @Override
        public ChatColor getNumberColor(final int number) {
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
        public String getSelectionText(final int number) {
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
        public String getAdditionalText(final int number) {
            switch(number) {
            case 1:
                if (SessionData.get(uuid, stagePrefix + Key.S_START_EVENT) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    return ChatColor.YELLOW + "(" + ChatColor.AQUA + SessionData.get(uuid, stagePrefix
                            + Key.S_START_EVENT) + ChatColor.YELLOW + ")";
                }
            case 2:
                if (SessionData.get(uuid, stagePrefix + Key.S_FINISH_EVENT) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    return ChatColor.YELLOW + "(" + ChatColor.AQUA + SessionData.get(uuid, stagePrefix
                            + Key.S_FINISH_EVENT) + ChatColor.YELLOW + ")";
                }
            case 3:
                if (SessionData.get(uuid, stagePrefix + Key.S_FAIL_EVENT) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    return ChatColor.YELLOW + "(" + ChatColor.AQUA + SessionData.get(uuid, stagePrefix
                            + Key.S_FAIL_EVENT) + ChatColor.YELLOW + ")";
                }  
            case 4:
                if (SessionData.get(uuid, stagePrefix + Key.S_DEATH_EVENT) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    return ChatColor.YELLOW + "(" + ChatColor.AQUA + SessionData.get(uuid, stagePrefix
                            + Key.S_DEATH_EVENT) + ChatColor.YELLOW + ")";
                }
            case 5:
                if (SessionData.get(uuid, stagePrefix + Key.S_DISCONNECT_EVENT) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    return ChatColor.YELLOW + "(" + ChatColor.AQUA + SessionData.get(uuid, stagePrefix
                            + Key.S_DISCONNECT_EVENT) + ChatColor.YELLOW + ")";
                }
            case 6:
                if (SessionData.get(uuid, stagePrefix + Key.S_CHAT_EVENTS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final LinkedList<String> chatEvents = (LinkedList<String>) SessionData.get(uuid, stagePrefix 
                            + Key.S_CHAT_EVENTS);
                    final LinkedList<String> chatEventTriggers 
                            = (LinkedList<String>) SessionData.get(uuid, stagePrefix + Key.S_CHAT_EVENT_TRIGGERS);
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
                if (SessionData.get(uuid, stagePrefix + Key.S_COMMAND_EVENTS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final LinkedList<String> commandEvents 
                            = (LinkedList<String>) SessionData.get(uuid, stagePrefix + Key.S_COMMAND_EVENTS);
                    final LinkedList<String> commandEventTriggers 
                            = (LinkedList<String>) SessionData.get(uuid, stagePrefix + Key.S_COMMAND_EVENT_TRIGGERS);
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
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenNumericPromptEvent event
                    = new BukkitQuestsEditorPostOpenNumericPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle() + " -");
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                        .append(getAdditionalText(i));
            }
            return text.toString();
        }
        
        @Override
        public void acceptInput(final Number input) {
            switch(input.intValue()) {
            case 1:
                new QuestStartActionPrompt(uuid).start();
            case 2:
                new QuestFinishActionPrompt(uuid).start();
            case 3:
                new QuestFailActionPrompt(uuid).start();
            case 4:
                new QuestDeathActionPrompt(uuid).start();
            case 5:
                new QuestDisconnectActionPrompt(uuid).start();
            case 6:
                new QuestChatActionPrompt(uuid).start();
            case 7:
                new QuestCommandActionPrompt(uuid).start();
            case 8:
                new QuestStageMainPrompt(stageNum, uuid).start();
            default:
                new QuestActionListPrompt(uuid).start();
            }
        }
    }

    public class QuestStartActionPrompt extends QuestsEditorStringPrompt {
        
        public QuestStartActionPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return BukkitLang.get("stageEditorStartEvent");
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("stageEditorEventsPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle() + " -\n");
            if (plugin.getLoadedActions().isEmpty()) {
                text.append(ChatColor.RED).append("- ").append(BukkitLang.get("none")).append("\n");
            } else {
                for (final Action a : plugin.getLoadedActions()) {
                    text.append(ChatColor.GREEN).append("- ").append(a.getName()).append("\n");
                }
            }
            return text.toString() + ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
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
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("stageEditorInvalidEvent")
                            .replace("<input>", input));
                    new QuestStartActionPrompt(uuid).start();
                } else {
                    SessionData.set(uuid, stagePrefix + Key.S_START_EVENT, found.getName());
                    new QuestActionListPrompt(uuid).start();
                }
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                new QuestActionListPrompt(uuid).start();
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, stagePrefix + Key.S_START_EVENT, null);
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorEventCleared"));
                new QuestActionListPrompt(uuid).start();
            } else {
                new QuestStartActionPrompt(uuid).start();
            }
        }
    }

    public class QuestFinishActionPrompt extends QuestsEditorStringPrompt {
        
        public QuestFinishActionPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return BukkitLang.get("stageEditorFinishEvent");
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("stageEditorEventsPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle() + " -\n");
            if (plugin.getLoadedActions().isEmpty()) {
                text.append(ChatColor.RED).append("- ").append(BukkitLang.get("none"));
            } else {
                for (final Action a : plugin.getLoadedActions()) {
                    text.append(ChatColor.GREEN).append("- ").append(a.getName()).append("\n");
                }
            }
            return text.toString() + ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
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
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("stageEditorInvalidEvent")
                            .replace("<input>", input));
                    new QuestFinishActionPrompt(uuid).start();
                } else {
                    SessionData.set(uuid, stagePrefix + Key.S_FINISH_EVENT, found.getName());
                    new QuestActionListPrompt(uuid).start();
                }
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                new QuestActionListPrompt(uuid).start();
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, stagePrefix + Key.S_FINISH_EVENT, null);
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorEventCleared"));
                new QuestActionListPrompt(uuid).start();
            } else {
                new QuestFinishActionPrompt(uuid).start();
            }
        }
    }
    
    public class QuestFailActionPrompt extends QuestsEditorStringPrompt {
        
        public QuestFailActionPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return BukkitLang.get("stageEditorFailEvent");
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("stageEditorEventsPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle() + " -\n");
            if (plugin.getLoadedActions().isEmpty()) {
                text.append(ChatColor.RED).append("- ").append(BukkitLang.get("none"));
            } else {
                for (final Action a : plugin.getLoadedActions()) {
                    text.append(ChatColor.GREEN).append("- ").append(a.getName()).append("\n");
                }
            }
            return text.toString() + ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
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
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("stageEditorInvalidEvent")
                            .replace("<input>", input));
                    new QuestFailActionPrompt(uuid).start();
                } else {
                    SessionData.set(uuid, stagePrefix + Key.S_FAIL_EVENT, found.getName());
                    new QuestActionListPrompt(uuid).start();
                }
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                new QuestActionListPrompt(uuid).start();
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, stagePrefix + Key.S_FAIL_EVENT, null);
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorEventCleared"));
                new QuestActionListPrompt(uuid).start();
            } else {
                new QuestFailActionPrompt(uuid).start();
            }
        }
    }

    public class QuestDeathActionPrompt extends QuestsEditorStringPrompt {
        
        public QuestDeathActionPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return BukkitLang.get("stageEditorDeathEvent");
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("stageEditorEventsPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle() + " -\n");
            if (plugin.getLoadedActions().isEmpty()) {
                text.append(ChatColor.RED).append("- ").append(BukkitLang.get("none")).append("\n");
            } else {
                for (final Action a : plugin.getLoadedActions()) {
                    text.append(ChatColor.GREEN).append("- ").append(a.getName()).append("\n");
                }
            }
            return text.toString() + ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
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
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("stageEditorInvalidEvent")
                            .replace("<input>", input));
                    new QuestDeathActionPrompt(uuid).start();
                } else {
                    SessionData.set(uuid, stagePrefix + Key.S_DEATH_EVENT, found.getName());
                    new QuestActionListPrompt(uuid).start();
                }
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                new QuestActionListPrompt(uuid).start();
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, stagePrefix + Key.S_DEATH_EVENT, null);
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorEventCleared"));
                new QuestActionListPrompt(uuid).start();
            } else {
                new QuestDeathActionPrompt(uuid).start();
            }
        }
    }

    public class QuestDisconnectActionPrompt extends QuestsEditorStringPrompt {
        
        public QuestDisconnectActionPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return BukkitLang.get("stageEditorDisconnectEvent");
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("stageEditorEventsPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle() + " -\n");
            if (plugin.getLoadedActions().isEmpty()) {
                text.append(ChatColor.RED).append("- ").append(BukkitLang.get("none"));
            } else {
                for (final Action a : plugin.getLoadedActions()) {
                    text.append(ChatColor.GREEN).append("- ").append(a.getName()).append("\n");
                }
            }
            return text.toString() + ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
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
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("stageEditorInvalidEvent")
                            .replace("<input>", input));
                    new QuestDisconnectActionPrompt(uuid).start();
                } else {
                    SessionData.set(uuid, stagePrefix + Key.S_DISCONNECT_EVENT, found.getName());
                    new QuestActionListPrompt(uuid).start();
                }
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                new QuestActionListPrompt(uuid).start();
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, stagePrefix + Key.S_DISCONNECT_EVENT, null);
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorEventCleared"));
                new QuestActionListPrompt(uuid).start();
            } else {
                new QuestDisconnectActionPrompt(uuid).start();
            }
        }
    }

    public class QuestChatActionPrompt extends QuestsEditorStringPrompt {
        
        public QuestChatActionPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return BukkitLang.get("stageEditorChatEvents");
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("stageEditorChatEventsPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle() + " -\n");
            if (plugin.getLoadedActions().isEmpty()) {
                text.append(ChatColor.RED).append("- ").append(BukkitLang.get("none"));
            } else {
                for (final Action a : plugin.getLoadedActions()) {
                    text.append(ChatColor.GREEN).append("- ").append(a.getName()).append("\n");
                }
            }
            return text.toString() + ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
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
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("stageEditorInvalidEvent")
                            .replace("<input>", input));
                    new QuestChatActionPrompt(uuid).start();
                } else {
                    SessionData.set(uuid, stagePrefix + Key.S_CHAT_TEMP_EVENT, found.getName());
                    new QuestChatActionTriggerPrompt(uuid).start();
                }
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
               new QuestActionListPrompt(uuid).start();
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, stagePrefix + Key.S_CHAT_EVENTS, null);
                SessionData.set(uuid, stagePrefix + Key.S_CHAT_EVENT_TRIGGERS, null);
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorChatEventsCleared"));
                new QuestActionListPrompt(uuid).start();
            } else {
                new QuestChatActionPrompt(uuid).start();
            }
        }
    }

    public class QuestChatActionTriggerPrompt extends QuestsEditorStringPrompt {
        
        public QuestChatActionTriggerPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return BukkitLang.get("stageEditorChatTrigger");
        }

        @Override
        public String getQueryText() {
            final String tempEvent = (String) SessionData.get(uuid, stagePrefix + Key.S_CHAT_TEMP_EVENT);
            if (tempEvent != null) {
                return BukkitLang.get("stageEditorChatEventsTriggerPrompt").replace("<event>", tempEvent)
                        .replace("<action>", tempEvent);
            } else {
                return BukkitLang.get("stageEditorChatEventsTriggerPrompt");
            }
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final String tempEvent = (String) SessionData.get(uuid, stagePrefix + Key.S_CHAT_TEMP_EVENT);
            String text = ChatColor.GOLD + "- " + getTitle() + " -\n";
            if (tempEvent != null) {
                text += ChatColor.YELLOW + getQueryText()
                        .replaceFirst(tempEvent, ChatColor.AQUA + tempEvent + ChatColor.YELLOW);
            }
            return text;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final String input) {
            if (input != null && !input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
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
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                new QuestActionListPrompt(uuid).start();
            } else {
                new QuestChatActionTriggerPrompt(uuid).start();
            }
        }
    }
    
    public class QuestCommandActionPrompt extends QuestsEditorStringPrompt {
        
        public QuestCommandActionPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return BukkitLang.get("stageEditorCommandEvents");
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("stageEditorCommandEventsPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle() + " -\n");
            if (plugin.getLoadedActions().isEmpty()) {
                text.append(ChatColor.RED).append("- ").append(BukkitLang.get("none"));
            } else {
                for (final Action a : plugin.getLoadedActions()) {
                    text.append(ChatColor.GREEN).append("- ").append(a.getName()).append("\n");
                }
            }
            return text.toString() + ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
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
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("stageEditorInvalidEvent")
                            .replace("<input>", input));
                    new QuestCommandActionPrompt(uuid).start();
                } else {
                    SessionData.set(uuid, stagePrefix + Key.S_COMMAND_TEMP_EVENT, found.getName());
                    new QuestCommandActionTriggerPrompt(uuid).start();
                }
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                new QuestActionListPrompt(uuid).start();
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, stagePrefix + Key.S_COMMAND_EVENTS, null);
                SessionData.set(uuid, stagePrefix + Key.S_COMMAND_EVENT_TRIGGERS, null);
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorCommandEventsCleared"));
                new QuestActionListPrompt(uuid).start();
            } else {
                new QuestCommandActionPrompt(uuid).start();
            }
        }
    }

    public class QuestCommandActionTriggerPrompt extends QuestsEditorStringPrompt {
        
        public QuestCommandActionTriggerPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return BukkitLang.get("stageEditorCommandTrigger");
        }

        @Override
        public String getQueryText() {
            final String tempEvent = (String) SessionData.get(uuid, stagePrefix + Key.S_COMMAND_TEMP_EVENT);
            if (tempEvent != null) {
                return BukkitLang.get("stageEditorCommandEventsTriggerPrompt").replace("<event>", tempEvent)
                        .replace("<action>", tempEvent);
            } else {
                return BukkitLang.get("stageEditorCommandEventsTriggerPrompt");
            }
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final String tempEvent = (String) SessionData.get(uuid, stagePrefix + Key.S_COMMAND_TEMP_EVENT);
            String text = ChatColor.GOLD + "- " + getTitle() + " -\n";
            if (tempEvent != null) {
                text += ChatColor.YELLOW + getQueryText()
                        .replaceFirst(tempEvent, ChatColor.AQUA + tempEvent + ChatColor.YELLOW);
            }
            return text;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void acceptInput(final String input) {
            if (input != null && !input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
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
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                new QuestActionListPrompt(uuid).start();
            } else {
                new QuestCommandActionTriggerPrompt(uuid).start();
            }
        }
    }

    public class QuestConditionListPrompt extends QuestsEditorStringPrompt {

        public QuestConditionListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return BukkitLang.get("stageEditorConditions");
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("stageEditorConditionsPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle() + " -\n");
            if (plugin.getLoadedConditions().isEmpty()) {
                text.append(ChatColor.RED).append("- ").append(BukkitLang.get("none")).append("\n");
            } else {
                for (final Condition c : plugin.getLoadedConditions()) {
                    text.append(ChatColor.GREEN).append("- ").append(c.getName()).append("\n");
                }
            }
            return text.toString() + ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
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
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("stageEditorInvalidCondition")
                            .replace("<input>", input));
                    new QuestConditionListPrompt(uuid).start();
                } else {
                    SessionData.set(uuid, stagePrefix + Key.S_CONDITION, found.getName());
                    new QuestStageMainPrompt(stageNum, uuid).start();
                }
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                new QuestStageMainPrompt(stageNum, uuid).start();
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, stagePrefix + Key.S_CONDITION, null);
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorConditionCleared"));
                new QuestStageMainPrompt(stageNum, uuid).start();
            } else {
                new QuestConditionListPrompt(uuid).start();
            }
        }
    }
    
    public class QuestDelayPrompt extends QuestsEditorStringPrompt {
        
        public QuestDelayPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("timePrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (input != null) {
                if (input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                    new QuestStageMainPrompt(stageNum, uuid).start();
                }
                if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                    SessionData.set(uuid, stagePrefix + Key.S_DELAY, null);
                    sender.sendMessage(ChatColor.GREEN + BukkitLang.get("stageEditorDelayCleared"));
                    new QuestStageMainPrompt(stageNum, uuid).start();
                }
                long stageDelay = 1L;
                try {
                    final int i = Integer.parseInt(input);
                    stageDelay = i * 1000L;
                } catch (final NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                            .replace("<input>", input));
                    new QuestDelayPrompt(uuid).start();
                }
                if (stageDelay < 1000) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("invalidMinimum")
                            .replace("<number>", "1"));
                    new QuestDelayPrompt(uuid).start();
                } else {
                    SessionData.set(uuid, stagePrefix + Key.S_DELAY, stageDelay);
                    new QuestStageMainPrompt(stageNum, uuid).start();
                }
            }
            new QuestStageMainPrompt(stageNum, uuid).start();
        }
    }

    public class QuestDelayMessagePrompt extends QuestsEditorStringPrompt {
        
        public QuestDelayMessagePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("stageEditorDelayMessagePrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (input != null && !input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, stagePrefix + Key.S_DELAY_MESSAGE, input);
                new QuestStageMainPrompt(stageNum, uuid).start();
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, stagePrefix + Key.S_DELAY_MESSAGE, null);
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorMessageCleared"));
                new QuestStageMainPrompt(stageNum, uuid).start();
            } else {
                new QuestDelayMessagePrompt(uuid).start();
            }
        }
    }
    

    public class QuestStartMessagePrompt extends QuestsEditorStringPrompt {
        
        public QuestStartMessagePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("stageEditorStartMessagePrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (input != null && !input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, stagePrefix + Key.S_START_MESSAGE, input);
                new QuestStageMainPrompt(stageNum, uuid).start();
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, stagePrefix + Key.S_START_MESSAGE, null);
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorMessageCleared"));
                new QuestStageMainPrompt(stageNum, uuid).start();
            } else {
                new QuestStageMainPrompt(stageNum, uuid).start();
            }
        }
    }

    public class QuestCompleteMessagePrompt extends QuestsEditorStringPrompt {
        
        public QuestCompleteMessagePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("stageEditorCompleteMessagePrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (input != null && !input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, stagePrefix + Key.S_COMPLETE_MESSAGE, input);
                new QuestStageMainPrompt(stageNum, uuid).start();
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, stagePrefix + Key.S_COMPLETE_MESSAGE, null);
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorMessageCleared"));
                new QuestStageMainPrompt(stageNum, uuid).start();
            } else {
                new QuestStageMainPrompt(stageNum, uuid).start();
            }
        }
    }

    public class QuestStageDeletePrompt extends QuestsEditorStringPrompt {
        
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

        @SuppressWarnings("unused")
        public ChatColor getNumberColor(final int number) {
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
        public String getSelectionText(final int number) {
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
        public String getQueryText() {
            return BukkitLang.get("confirmDelete");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.YELLOW + getQueryText() + " ("
                    + ChatColor.RED + BukkitLang.get("stageEditorStage") + " " + stageNum + ChatColor.YELLOW + ")\n"
                    + ChatColor.GOLD + "(" + BukkitLang.get("stageEditorConfirmStageNote") + ")\n");
            for (int i = 1; i <= size; i++) {
                text.append(getNumberColor(i)).append(ChatColor.BOLD).append(i).append(ChatColor.RESET)
                        .append(" - ").append(getSelectionText(i)).append("\n");
            }
            return QuestsIntegerPrompt.sendClickableSelection(text.toString(), plugin.getQuester(uuid));
        }

        @Override
        public void acceptInput(final String input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (input != null && (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(BukkitLang.get("yesWord")))) {
                new QuestStageMenuPrompt(uuid).deleteStage(stageNum);
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorDeleteSucces"));
                new QuestStageMenuPrompt(uuid).start();
            } else if (input != null && (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(BukkitLang.get("noWord")))) {
                new QuestStageMainPrompt(stageNum, uuid).start();
            } else {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("invalidOption"));
                new QuestStageDeletePrompt(uuid).start();
            }
        }
    }

    public class QuestCustomObjectiveModulePrompt extends QuestsEditorStringPrompt {

        public QuestCustomObjectiveModulePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return BukkitLang.get("stageEditorModules");
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("stageEditorModulePrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            if (!(BukkitMiscUtil.getEntity(uuid) instanceof Player) || !plugin.getConfigSettings().canClickablePrompts()) {
                final StringBuilder text = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle() + "\n");
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
                return text.toString() + ChatColor.YELLOW + getQueryText();
            }
            final TextComponent component = new TextComponent(getTitle() + "\n");
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
            component.addExtra(ChatColor.YELLOW + getQueryText());
            BukkitMiscUtil.getEntity(uuid).spigot().sendMessage(component);
            return "";
        }

        @Override
        public void acceptInput(@Nullable final String input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
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
                    new QuestCustomObjectivesPrompt(found, uuid).start();
                }
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                new QuestStageMainPrompt(stageNum, uuid).start();
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, stagePrefix + Key.S_CUSTOM_OBJECTIVES, null);
                SessionData.set(uuid, stagePrefix + Key.S_CUSTOM_OBJECTIVES_DATA, null);
                SessionData.set(uuid, stagePrefix + Key.S_CUSTOM_OBJECTIVES_DATA_TEMP, null);
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorCustomCleared"));
                new QuestStageMainPrompt(stageNum, uuid).start();
            }
            sender.sendMessage(ChatColor.RED + BukkitLang.get("stageEditorModuleNotFound"));
            new QuestCustomObjectiveModulePrompt(uuid).start();
        }
    }

    public class QuestCustomObjectivesPrompt extends QuestsEditorStringPrompt {

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
            return BukkitLang.get("customObjectivesTitle");
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("stageEditorCustomPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            if (!(BukkitMiscUtil.getEntity(uuid) instanceof Player) || !plugin.getConfigSettings().canClickablePrompts()) {
                final StringBuilder text = new StringBuilder(ChatColor.LIGHT_PURPLE + "- " + getTitle()
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
                return text.toString() + ChatColor.YELLOW + getQueryText();
            }
            final TextComponent component = new TextComponent(getTitle() + "\n");
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
            component.addExtra(ChatColor.YELLOW + getQueryText());
            BukkitMiscUtil.getEntity(uuid).spigot().sendMessage(component);
            return "";
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final String input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
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
                    if (SessionData.get(uuid, stagePrefix + Key.S_CUSTOM_OBJECTIVES) != null) {
                        // The custom objective may already have been added, so let's check that
                        final LinkedList<String> list = (LinkedList<String>) SessionData.get(uuid, stagePrefix 
                                + Key.S_CUSTOM_OBJECTIVES);
                        final LinkedList<Entry<String, Object>> dataMapList
                                = (LinkedList<Entry<String, Object>>) SessionData.get(uuid, stagePrefix 
                                + Key.S_CUSTOM_OBJECTIVES_DATA);
                        final LinkedList<Integer> countList = (LinkedList<Integer>) SessionData.get(uuid, stagePrefix 
                                + Key.S_CUSTOM_OBJECTIVES_COUNT);
                        if (list != null && !list.contains(found.getName()) && dataMapList != null
                                && countList != null) {
                            // Hasn't been added yet, so let's do it
                            list.add(found.getName());
                            dataMapList.addAll(found.getData());
                            countList.add(-999);
                            SessionData.set(uuid, stagePrefix + Key.S_CUSTOM_OBJECTIVES, list);
                            SessionData.set(uuid, stagePrefix + Key.S_CUSTOM_OBJECTIVES_DATA, dataMapList);
                        } else {
                            // Already added, so inform user
                            sender.sendMessage(ChatColor.RED
                                    + BukkitLang.get("stageEditorCustomAlreadyAdded"));
                            new QuestCustomObjectivesPrompt(moduleName, uuid);
                        }
                    } else {
                        // The custom objective hasn't been added yet, so let's do it
                        final LinkedList<Integer> countList = new LinkedList<>();
                        final LinkedList<Entry<String, Object>> dataMapList = new LinkedList<>(found.getData());
                        countList.add(-999);
                        final LinkedList<String> list = new LinkedList<>();
                        list.add(found.getName());
                        SessionData.set(uuid, stagePrefix + Key.S_CUSTOM_OBJECTIVES, list);
                        SessionData.set(uuid, stagePrefix + Key.S_CUSTOM_OBJECTIVES_DATA, dataMapList);
                        SessionData.set(uuid, stagePrefix + Key.S_CUSTOM_OBJECTIVES_COUNT, countList);
                    }
                    // Send user to the count prompt / custom data prompt if there is any needed
                    if (found.canShowCount()) {
                        new QuestCustomObjectiveCountPrompt(uuid).start();
                    }
                    if (!found.getData().isEmpty()) {
                        SessionData.set(uuid, stagePrefix + Key.S_CUSTOM_OBJECTIVES_DATA_DESCRIPTIONS, found
                                .getDescriptions());
                        new QuestObjectiveCustomDataListPrompt(uuid).start();
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("stageEditorModuleNotFound"));
                    new QuestCustomObjectivesPrompt(moduleName, uuid).start();
                }
            } else if (input != null && input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, stagePrefix + Key.S_CUSTOM_OBJECTIVES, null);
                SessionData.set(uuid, stagePrefix + Key.S_CUSTOM_OBJECTIVES_DATA, null);
                SessionData.set(uuid, stagePrefix + Key.S_CUSTOM_OBJECTIVES_DATA_TEMP, null);
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorCustomCleared"));
            }
            new QuestStageMainPrompt(stageNum, uuid).start();
        }
    }

    private class QuestCustomObjectiveCountPrompt extends QuestsEditorStringPrompt {

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
            final StringBuilder text = new StringBuilder(ChatColor.GOLD + "- ");
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
                    text.append(ChatColor.YELLOW).append(found.getCountPrompt());
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
                        new QuestStageMainPrompt(stageNum, uuid).start();
                    }
                }
            } catch (final NumberFormatException e) {
                BukkitMiscUtil.getEntity(uuid).sendMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                        .replace("<input>", input));
                new QuestCustomObjectiveCountPrompt(uuid);
            }
            new QuestStageMainPrompt(stageNum, uuid).start();
        }
    }

    private class QuestObjectiveCustomDataListPrompt extends QuestsEditorStringPrompt {

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
            final StringBuilder text = new StringBuilder(ChatColor.GOLD + "- ");
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
                    plugin.getLogger().severe("Could not find custom objective following input: " + input);
                    new QuestObjectiveCustomDataListPrompt(uuid).start();
                }
                final LinkedList<Entry<String, Object>> dataMapList = found.getData();

                int numInput = 0;
                try {
                    numInput = Integer.parseInt(input);
                } catch (final NumberFormatException nfe) {
                    new QuestObjectiveCustomDataListPrompt(uuid).start();
                }
                if (numInput < 1 || numInput > dataMapList.size() + 1) {
                    new QuestObjectiveCustomDataListPrompt(uuid).start();
                }
                if (numInput < dataMapList.size() + 1) {
                    final LinkedList<String> dataMapKeys = new LinkedList<>();
                    for (final Entry<String, Object> dataMap : dataMapList) {
                        dataMapKeys.add(dataMap.getKey());
                    }
                    // Collections.sort(dataMapKeys);
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
                    new QuestStageMainPrompt(stageNum, uuid).start();
                }
            }
            new QuestStageMainPrompt(stageNum, uuid).start();
        }
    }

    private class QuestObjectiveCustomDataPrompt extends QuestsEditorStringPrompt {

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
        public @NotNull String getPromptText() {
            String text = "";
            final String temp = (String) SessionData.get(uuid, stagePrefix + Key.S_CUSTOM_OBJECTIVES_DATA_TEMP);
            @SuppressWarnings("unchecked")
            final
            Map<String, String> descriptions 
                    = (Map<String, String>) SessionData.get(uuid, stagePrefix 
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
        public void acceptInput(final String input) {
            @SuppressWarnings("unchecked")
            final
            LinkedList<Entry<String, Object>> dataMapList
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
