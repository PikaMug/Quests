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
import me.pikamug.quests.convo.quests.QuestsEditorIntegerPrompt;
import me.pikamug.quests.events.editor.quests.BukkitQuestsEditorPostOpenNumericPromptEvent;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.SessionData;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class QuestStageMenuPrompt extends QuestsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final BukkitQuestsPlugin plugin;
    private final int size = 2;

    public QuestStageMenuPrompt(final @NotNull UUID uuid) {
        super(uuid);
        this.uuid = uuid;
        this.plugin = BukkitQuestsPlugin.getInstance();
    }
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle() {
        return BukkitLang.get("stageEditorStages");
    }
    
    @Override
    public ChatColor getNumberColor(final int number) {
        final int stages = getStages();
        if (number > 0) {
            if (number < stages + 1) {
                return ChatColor.BLUE;
            } else if (number == stages + 1) {
                return ChatColor.BLUE;
            } else if (number == stages + 2) {
                return ChatColor.GREEN;
            }
        }
        return null;
    }
    
    @Override
    public String getSelectionText(final int number) {
        final int stages = getStages();
        if (number > 0) {
            if (number < stages + 1) {
                return ChatColor.GOLD + BukkitLang.get("stageEditorEditStage") + " " + number;
            } else if (number == stages + 1) {
                return ChatColor.YELLOW + BukkitLang.get("stageEditorNewStage");
            } else if (number == stages + 2) {
                return ChatColor.YELLOW + BukkitLang.get("done");
            }
        }
        return null;
    }
    
    @Override
    public String getAdditionalText(final int number) {
        return null;
    }

    @Override
    public @NotNull String getPromptText() {
        final BukkitQuestsEditorPostOpenNumericPromptEvent event
                = new BukkitQuestsEditorPostOpenNumericPromptEvent(uuid, this);
        plugin.getServer().getPluginManager().callEvent(event);
        
        final StringBuilder text = new StringBuilder(ChatColor.LIGHT_PURPLE + "- " + getTitle() + " -");
        for (int i = 1; i <= (getStages() + size); i++) {
            text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                    .append(ChatColor.RESET).append(" - ").append(getSelectionText(i));
        }
        return text.toString();
    }

    @Override
    public void acceptInput(final Number input) {
        final int i = input.intValue();
        final int stages = getStages();
        if (i > 0) {
            if (i < (stages + 1)) {
                new QuestStageMainPrompt((i), uuid).start();
            } else if (i == (stages + 1)) {
                new QuestStageMainPrompt((stages + 1), uuid).start();
            } else if (i == (stages + 2)) {
                plugin.getQuestFactory().returnToMenu(uuid);
            }
            return;
        }
        new QuestStageMenuPrompt(uuid).start();
    }

    public int getStages() {
        int num = 1;
        while (true) {
            if (SessionData.get(uuid, "stage" + num) != null) {
                num++;
            } else {
                break;
            }
        }
        return (num - 1);
    }

    public void deleteStage(final int stageNum) {
        final int stages = getStages();
        int current = stageNum;
        String pref = "stage" + current;
        String newPref;
        final boolean last = stageNum == stages;
        while (true) {
            if (!last) {
                current++;
                if (current > stages) {
                    break;
                }
                pref = "stage" + current;
                newPref = "stage" + (current - 1);
                SessionData.set(uuid, newPref + Key.S_BREAK_NAMES, SessionData.get(uuid, pref + Key.S_BREAK_NAMES));
                SessionData.set(uuid, newPref + Key.S_BREAK_AMOUNTS, SessionData.get(uuid, pref + Key.S_BREAK_AMOUNTS));
                SessionData.set(uuid, newPref + Key.S_BREAK_DURABILITY, SessionData.get(uuid, pref
                        + Key.S_BREAK_DURABILITY));
                SessionData.set(uuid, newPref + Key.S_DAMAGE_NAMES, SessionData.get(uuid, pref + Key.S_DAMAGE_NAMES));
                SessionData.set(uuid, newPref + Key.S_DAMAGE_AMOUNTS, SessionData.get(uuid, pref
                        + Key.S_DAMAGE_AMOUNTS));
                SessionData.set(uuid, newPref + Key.S_DAMAGE_DURABILITY, SessionData.get(uuid, pref
                        + Key.S_DAMAGE_DURABILITY));
                SessionData.set(uuid, newPref + Key.S_PLACE_NAMES, SessionData.get(uuid, pref + Key.S_PLACE_NAMES));
                SessionData.set(uuid, newPref + Key.S_PLACE_NAMES, SessionData.get(uuid, pref + Key.S_PLACE_AMOUNTS));
                SessionData.set(uuid, newPref + Key.S_PLACE_DURABILITY, SessionData.get(uuid, pref
                        + Key.S_PLACE_DURABILITY));
                SessionData.set(uuid, newPref + Key.S_USE_NAMES, SessionData.get(uuid, pref + Key.S_USE_NAMES));
                SessionData.set(uuid, newPref + Key.S_USE_AMOUNTS, SessionData.get(uuid, pref + Key.S_USE_AMOUNTS));
                SessionData.set(uuid, newPref + Key.S_USE_DURABILITY, SessionData.get(uuid, pref
                        + Key.S_USE_DURABILITY));
                SessionData.set(uuid, newPref + Key.S_CUT_NAMES, SessionData.get(uuid, pref + Key.S_CUT_NAMES));
                SessionData.set(uuid, newPref + Key.S_CUT_AMOUNTS, SessionData.get(uuid, pref + Key.S_CUT_AMOUNTS));
                SessionData.set(uuid, newPref + Key.S_CUT_DURABILITY, SessionData.get(uuid, pref
                        + Key.S_CUT_DURABILITY));
                SessionData.set(uuid, newPref + Key.S_CRAFT_ITEMS, SessionData.get(uuid, pref + Key.S_CRAFT_ITEMS));
                SessionData.set(uuid, newPref + Key.S_SMELT_ITEMS, SessionData.get(uuid, pref + Key.S_SMELT_ITEMS));
                SessionData.set(uuid, newPref + Key.S_ENCHANT_ITEMS, SessionData.get(uuid, pref + Key.S_ENCHANT_ITEMS));
                SessionData.set(uuid, newPref + Key.S_BREW_ITEMS, SessionData.get(uuid, pref + Key.S_BREW_ITEMS));
                SessionData.set(uuid, newPref + Key.S_FISH, SessionData.get(uuid, pref + Key.S_FISH));
                SessionData.set(uuid, newPref + Key.S_PLAYER_KILL, SessionData.get(uuid, pref + Key.S_PLAYER_KILL));
                SessionData.set(uuid, newPref + Key.S_DELIVERY_ITEMS, SessionData.get(uuid, pref
                        + Key.S_DELIVERY_ITEMS));
                SessionData.set(uuid, newPref + Key.S_DELIVERY_NPCS, SessionData.get(uuid, pref + Key.S_DELIVERY_NPCS));
                SessionData.set(uuid, newPref + Key.S_DELIVERY_MESSAGES, SessionData.get(uuid, pref
                        + Key.S_DELIVERY_MESSAGES));
                SessionData.set(uuid, newPref + Key.S_NPCS_TO_TALK_TO, SessionData.get(uuid, pref
                        + Key.S_NPCS_TO_TALK_TO));
                SessionData.set(uuid, newPref + Key.S_NPCS_TO_KILL, SessionData.get(uuid, pref + Key.S_NPCS_TO_KILL));
                SessionData.set(uuid, newPref + Key.S_NPCS_TO_KILL_AMOUNTS, SessionData.get(uuid, pref
                        + Key.S_NPCS_TO_KILL_AMOUNTS));
                SessionData.set(uuid, newPref + Key.S_MOB_TYPES, SessionData.get(uuid, pref + Key.S_MOB_TYPES));
                SessionData.set(uuid, newPref + Key.S_MOB_AMOUNTS, SessionData.get(uuid, pref + Key.S_MOB_AMOUNTS));
                SessionData.set(uuid, newPref + Key.S_MOB_KILL_LOCATIONS, SessionData.get(uuid, pref
                        + Key.S_MOB_KILL_LOCATIONS));
                SessionData.set(uuid, newPref + Key.S_MOB_KILL_LOCATIONS_RADIUS, SessionData.get(uuid, pref
                        + Key.S_MOB_KILL_LOCATIONS_RADIUS));
                SessionData.set(uuid, newPref + Key.S_MOB_KILL_LOCATIONS_NAMES, SessionData.get(uuid, pref
                        + Key.S_MOB_KILL_LOCATIONS_NAMES));
                SessionData.set(uuid, newPref + Key.S_REACH_LOCATIONS, SessionData.get(uuid, pref
                        + Key.S_REACH_LOCATIONS));
                SessionData.set(uuid, newPref + Key.S_REACH_LOCATIONS_RADIUS, SessionData.get(uuid, pref
                        + Key.S_REACH_LOCATIONS_RADIUS));
                SessionData.set(uuid, newPref + Key.S_REACH_LOCATIONS_NAMES, SessionData.get(uuid, pref
                        + Key.S_REACH_LOCATIONS_NAMES));
                SessionData.set(uuid, newPref + Key.S_TAME_TYPES, SessionData.get(uuid, pref + Key.S_TAME_TYPES));
                SessionData.set(uuid, newPref + Key.S_TAME_AMOUNTS, SessionData.get(uuid, pref + Key.S_TAME_AMOUNTS));
                SessionData.set(uuid, newPref + Key.S_SHEAR_COLORS, SessionData.get(uuid, pref + Key.S_SHEAR_COLORS));
                SessionData.set(uuid, newPref + Key.S_SHEAR_AMOUNTS, SessionData.get(uuid, pref + Key.S_SHEAR_AMOUNTS));
                SessionData.set(uuid, newPref + Key.S_START_EVENT, SessionData.get(uuid, pref + Key.S_START_EVENT));
                SessionData.set(uuid, newPref + Key.S_DISCONNECT_EVENT, SessionData.get(uuid, pref
                        + Key.S_DISCONNECT_EVENT));
                SessionData.set(uuid, newPref + Key.S_DEATH_EVENT, SessionData.get(uuid, pref + Key.S_DEATH_EVENT));
                SessionData.set(uuid, newPref + Key.S_CHAT_EVENTS, SessionData.get(uuid, pref + Key.S_CHAT_EVENTS));
                SessionData.set(uuid, newPref + Key.S_CHAT_EVENT_TRIGGERS, SessionData.get(uuid, pref
                        + Key.S_CHAT_EVENT_TRIGGERS));
                SessionData.set(uuid, newPref + Key.S_FINISH_EVENT, SessionData.get(uuid, pref + Key.S_FINISH_EVENT));
                SessionData.set(uuid, newPref + Key.S_CUSTOM_OBJECTIVES, SessionData.get(uuid, pref
                        + Key.S_CUSTOM_OBJECTIVES));
                SessionData.set(uuid, newPref + Key.S_CUSTOM_OBJECTIVES_DATA, SessionData.get(uuid, pref
                        + Key.S_CUSTOM_OBJECTIVES_DATA));
                SessionData.set(uuid, newPref + Key.S_CUSTOM_OBJECTIVES_COUNT, SessionData.get(uuid, pref
                        + Key.S_CUSTOM_OBJECTIVES_COUNT));
                SessionData.set(uuid, newPref + Key.S_CUSTOM_OBJECTIVES_DATA_DESCRIPTIONS, SessionData.get(uuid, pref
                        + Key.S_CUSTOM_OBJECTIVES_DATA_DESCRIPTIONS));
                SessionData.set(uuid, newPref + Key.S_CUSTOM_OBJECTIVES_DATA_TEMP, SessionData.get(uuid, pref
                        + Key.S_CUSTOM_OBJECTIVES_DATA_TEMP));
                SessionData.set(uuid, newPref + Key.S_PASSWORD_DISPLAYS, SessionData.get(uuid, pref
                        + Key.S_PASSWORD_DISPLAYS));
                SessionData.set(uuid, newPref + Key.S_PASSWORD_PHRASES, SessionData.get(uuid, pref
                        + Key.S_PASSWORD_PHRASES));
                SessionData.set(uuid, newPref + Key.S_OVERRIDE_DISPLAY, SessionData.get(uuid, pref
                        + Key.S_OVERRIDE_DISPLAY));
                SessionData.set(uuid, newPref + Key.S_DELAY, SessionData.get(uuid, pref + Key.S_DELAY));
                SessionData.set(uuid, newPref + Key.S_DELAY_MESSAGE, SessionData.get(uuid, pref
                        + Key.S_DELAY_MESSAGE));
                SessionData.set(uuid, newPref + Key.S_DENIZEN, SessionData.get(uuid, pref + Key.S_DENIZEN));
                SessionData.set(uuid, newPref + Key.S_COMPLETE_MESSAGE, SessionData.get(uuid, pref
                        + Key.S_COMPLETE_MESSAGE));
                SessionData.set(uuid, newPref + Key.S_START_MESSAGE, SessionData.get(uuid, pref + Key.S_START_MESSAGE));
            }
            SessionData.set(uuid, pref + Key.S_BREAK_NAMES, null);
            SessionData.set(uuid, pref + Key.S_BREAK_AMOUNTS, null);
            SessionData.set(uuid, pref + Key.S_BREAK_DURABILITY, null);
            SessionData.set(uuid, pref + Key.S_DAMAGE_NAMES, null);
            SessionData.set(uuid, pref + Key.S_DAMAGE_AMOUNTS, null);
            SessionData.set(uuid, pref + Key.S_DAMAGE_DURABILITY, null);
            SessionData.set(uuid, pref + Key.S_PLACE_NAMES, null);
            SessionData.set(uuid, pref + Key.S_PLACE_AMOUNTS, null);
            SessionData.set(uuid, pref + Key.S_PLACE_DURABILITY, null);
            SessionData.set(uuid, pref + Key.S_USE_NAMES, null);
            SessionData.set(uuid, pref + Key.S_USE_AMOUNTS, null);
            SessionData.set(uuid, pref + Key.S_USE_DURABILITY, null);
            SessionData.set(uuid, pref + Key.S_CUT_NAMES, null);
            SessionData.set(uuid, pref + Key.S_CUT_AMOUNTS, null);
            SessionData.set(uuid, pref + Key.S_CUT_DURABILITY, null);
            SessionData.set(uuid, pref + Key.S_CRAFT_ITEMS, null);
            SessionData.set(uuid, pref + Key.S_SMELT_ITEMS, null);
            SessionData.set(uuid, pref + Key.S_ENCHANT_ITEMS, null);
            SessionData.set(uuid, pref + Key.S_BREW_ITEMS, null);
            SessionData.set(uuid, pref + Key.S_FISH, null);
            SessionData.set(uuid, pref + Key.S_PLAYER_KILL, null);
            SessionData.set(uuid, pref + Key.S_DELIVERY_ITEMS, null);
            SessionData.set(uuid, pref + Key.S_DELIVERY_NPCS, null);
            SessionData.set(uuid, pref + Key.S_DELIVERY_MESSAGES, null);
            SessionData.set(uuid, pref + Key.S_NPCS_TO_TALK_TO, null);
            SessionData.set(uuid, pref + Key.S_NPCS_TO_KILL, null);
            SessionData.set(uuid, pref + Key.S_NPCS_TO_KILL_AMOUNTS, null);
            SessionData.set(uuid, pref + Key.S_MOB_TYPES, null);
            SessionData.set(uuid, pref + Key.S_MOB_AMOUNTS, null);
            SessionData.set(uuid, pref + Key.S_MOB_KILL_LOCATIONS, null);
            SessionData.set(uuid, pref + Key.S_MOB_KILL_LOCATIONS_RADIUS, null);
            SessionData.set(uuid, pref + Key.S_MOB_KILL_LOCATIONS_NAMES, null);
            SessionData.set(uuid, pref + Key.S_REACH_LOCATIONS, null);
            SessionData.set(uuid, pref + Key.S_REACH_LOCATIONS_RADIUS, null);
            SessionData.set(uuid, pref + Key.S_REACH_LOCATIONS_NAMES, null);
            SessionData.set(uuid, pref + Key.S_TAME_TYPES, null);
            SessionData.set(uuid, pref + Key.S_TAME_AMOUNTS, null);
            SessionData.set(uuid, pref + Key.S_SHEAR_COLORS, null);
            SessionData.set(uuid, pref + Key.S_SHEAR_AMOUNTS, null);
            SessionData.set(uuid, pref + Key.S_FINISH_EVENT, null);
            SessionData.set(uuid, pref + Key.S_START_EVENT, null);
            SessionData.set(uuid, pref + Key.S_DEATH_EVENT, null);
            SessionData.set(uuid, pref + Key.S_CHAT_EVENTS, null);
            SessionData.set(uuid, pref + Key.S_CHAT_EVENT_TRIGGERS, null);
            SessionData.set(uuid, pref + Key.S_DISCONNECT_EVENT, null);
            SessionData.set(uuid, pref + Key.S_CUSTOM_OBJECTIVES, null);
            SessionData.set(uuid, pref + Key.S_CUSTOM_OBJECTIVES_DATA, null);
            SessionData.set(uuid, pref + Key.S_CUSTOM_OBJECTIVES_COUNT, null);
            SessionData.set(uuid, pref + Key.S_CUSTOM_OBJECTIVES_DATA_DESCRIPTIONS, null);
            SessionData.set(uuid, pref + Key.S_CUSTOM_OBJECTIVES_DATA_TEMP, null);
            SessionData.set(uuid, pref + Key.S_PASSWORD_DISPLAYS, null);
            SessionData.set(uuid, pref + Key.S_PASSWORD_PHRASES, null);
            SessionData.set(uuid, pref + Key.S_OVERRIDE_DISPLAY, null);
            SessionData.set(uuid, pref + Key.S_DELAY, null);
            SessionData.set(uuid, pref + Key.S_DELAY_MESSAGE, null);
            SessionData.set(uuid, pref + Key.S_DENIZEN, null);
            SessionData.set(uuid, pref + Key.S_COMPLETE_MESSAGE, null);
            SessionData.set(uuid, pref + Key.S_START_MESSAGE, null);
            if (last) {
                break;
            }
        }
        if (!last) {
            SessionData.set(uuid, "stage" + (current - 1), null);
        } else {
            SessionData.set(uuid, "stage" + (current), null);
        }
    }
}
