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
import me.pikamug.quests.convo.quests.QuestsEditorNumericPrompt;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.BukkitLang;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

import me.pikamug.quests.events.editor.quests.QuestsEditorPostOpenNumericPromptEvent;
import org.jetbrains.annotations.NotNull;

public class QuestStageMenuPrompt extends QuestsEditorNumericPrompt {

    private final BukkitQuestsPlugin plugin;
    private final int size = 2;

    public QuestStageMenuPrompt(final ConversationContext context) {
        super(context);
        this.plugin = (BukkitQuestsPlugin)context.getPlugin();
    }
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle(final ConversationContext context) {
        return BukkitLang.get("stageEditorStages");
    }
    
    @Override
    public ChatColor getNumberColor(final ConversationContext context, final int number) {
        final int stages = getStages(context);
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
    public String getSelectionText(final ConversationContext context, final int number) {
        final int stages = getStages(context);
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
    public String getAdditionalText(final ConversationContext context, final int number) {
        return null;
    }

    @Override
    public @NotNull String getBasicPromptText(final @NotNull ConversationContext context) {
        final QuestsEditorPostOpenNumericPromptEvent event
                = new QuestsEditorPostOpenNumericPromptEvent(context, this);
        plugin.getServer().getPluginManager().callEvent(event);
        
        final StringBuilder text = new StringBuilder(ChatColor.LIGHT_PURPLE + "- " + getTitle(context) + " -");
        for (int i = 1; i <= (getStages(context) + size); i++) {
            text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                    .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i));
        }
        return text.toString();
    }

    @Override
    protected Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
        final int i = input.intValue();
        final int stages = getStages(context);
        if (i > 0) {
            if (i < (stages + 1)) {
                return new QuestStageMainPrompt((i), context);
            } else if (i == (stages + 1)) {
                return new QuestStageMainPrompt((stages + 1), context);
            } else if (i == (stages + 2)) {
                return plugin.getQuestFactory().returnToMenu(context);
            }
        }
        return new QuestStageMenuPrompt(context);
    }

    public int getStages(final ConversationContext context) {
        int num = 1;
        while (true) {
            if (context.getSessionData("stage" + num) != null) {
                num++;
            } else {
                break;
            }
        }
        return (num - 1);
    }

    public void deleteStage(final ConversationContext context, final int stageNum) {
        final int stages = getStages(context);
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
                context.setSessionData(newPref + Key.S_BREAK_NAMES, context.getSessionData(pref + Key.S_BREAK_NAMES));
                context.setSessionData(newPref + Key.S_BREAK_AMOUNTS, context.getSessionData(pref + Key.S_BREAK_AMOUNTS));
                context.setSessionData(newPref + Key.S_BREAK_DURABILITY, context.getSessionData(pref
                        + Key.S_BREAK_DURABILITY));
                context.setSessionData(newPref + Key.S_DAMAGE_NAMES, context.getSessionData(pref + Key.S_DAMAGE_NAMES));
                context.setSessionData(newPref + Key.S_DAMAGE_AMOUNTS, context.getSessionData(pref
                        + Key.S_DAMAGE_AMOUNTS));
                context.setSessionData(newPref + Key.S_DAMAGE_DURABILITY, context.getSessionData(pref
                        + Key.S_DAMAGE_DURABILITY));
                context.setSessionData(newPref + Key.S_PLACE_NAMES, context.getSessionData(pref + Key.S_PLACE_NAMES));
                context.setSessionData(newPref + Key.S_PLACE_NAMES, context.getSessionData(pref + Key.S_PLACE_AMOUNTS));
                context.setSessionData(newPref + Key.S_PLACE_DURABILITY, context.getSessionData(pref
                        + Key.S_PLACE_DURABILITY));
                context.setSessionData(newPref + Key.S_USE_NAMES, context.getSessionData(pref + Key.S_USE_NAMES));
                context.setSessionData(newPref + Key.S_USE_AMOUNTS, context.getSessionData(pref + Key.S_USE_AMOUNTS));
                context.setSessionData(newPref + Key.S_USE_DURABILITY, context.getSessionData(pref
                        + Key.S_USE_DURABILITY));
                context.setSessionData(newPref + Key.S_CUT_NAMES, context.getSessionData(pref + Key.S_CUT_NAMES));
                context.setSessionData(newPref + Key.S_CUT_AMOUNTS, context.getSessionData(pref + Key.S_CUT_AMOUNTS));
                context.setSessionData(newPref + Key.S_CUT_DURABILITY, context.getSessionData(pref
                        + Key.S_CUT_DURABILITY));
                context.setSessionData(newPref + Key.S_CRAFT_ITEMS, context.getSessionData(pref + Key.S_CRAFT_ITEMS));
                context.setSessionData(newPref + Key.S_SMELT_ITEMS, context.getSessionData(pref + Key.S_SMELT_ITEMS));
                context.setSessionData(newPref + Key.S_ENCHANT_ITEMS, context.getSessionData(pref + Key.S_ENCHANT_ITEMS));
                context.setSessionData(newPref + Key.S_BREW_ITEMS, context.getSessionData(pref + Key.S_BREW_ITEMS));
                context.setSessionData(newPref + Key.S_FISH, context.getSessionData(pref + Key.S_FISH));
                context.setSessionData(newPref + Key.S_PLAYER_KILL, context.getSessionData(pref + Key.S_PLAYER_KILL));
                context.setSessionData(newPref + Key.S_DELIVERY_ITEMS, context.getSessionData(pref
                        + Key.S_DELIVERY_ITEMS));
                context.setSessionData(newPref + Key.S_DELIVERY_NPCS, context.getSessionData(pref + Key.S_DELIVERY_NPCS));
                context.setSessionData(newPref + Key.S_DELIVERY_MESSAGES, context.getSessionData(pref
                        + Key.S_DELIVERY_MESSAGES));
                context.setSessionData(newPref + Key.S_NPCS_TO_TALK_TO, context.getSessionData(pref
                        + Key.S_NPCS_TO_TALK_TO));
                context.setSessionData(newPref + Key.S_NPCS_TO_KILL, context.getSessionData(pref + Key.S_NPCS_TO_KILL));
                context.setSessionData(newPref + Key.S_NPCS_TO_KILL_AMOUNTS, context.getSessionData(pref
                        + Key.S_NPCS_TO_KILL_AMOUNTS));
                context.setSessionData(newPref + Key.S_MOB_TYPES, context.getSessionData(pref + Key.S_MOB_TYPES));
                context.setSessionData(newPref + Key.S_MOB_AMOUNTS, context.getSessionData(pref + Key.S_MOB_AMOUNTS));
                context.setSessionData(newPref + Key.S_MOB_KILL_LOCATIONS, context.getSessionData(pref
                        + Key.S_MOB_KILL_LOCATIONS));
                context.setSessionData(newPref + Key.S_MOB_KILL_LOCATIONS_RADIUS, context.getSessionData(pref
                        + Key.S_MOB_KILL_LOCATIONS_RADIUS));
                context.setSessionData(newPref + Key.S_MOB_KILL_LOCATIONS_NAMES, context.getSessionData(pref
                        + Key.S_MOB_KILL_LOCATIONS_NAMES));
                context.setSessionData(newPref + Key.S_REACH_LOCATIONS, context.getSessionData(pref
                        + Key.S_REACH_LOCATIONS));
                context.setSessionData(newPref + Key.S_REACH_LOCATIONS_RADIUS, context.getSessionData(pref
                        + Key.S_REACH_LOCATIONS_RADIUS));
                context.setSessionData(newPref + Key.S_REACH_LOCATIONS_NAMES, context.getSessionData(pref
                        + Key.S_REACH_LOCATIONS_NAMES));
                context.setSessionData(newPref + Key.S_TAME_TYPES, context.getSessionData(pref + Key.S_TAME_TYPES));
                context.setSessionData(newPref + Key.S_TAME_AMOUNTS, context.getSessionData(pref + Key.S_TAME_AMOUNTS));
                context.setSessionData(newPref + Key.S_SHEAR_COLORS, context.getSessionData(pref + Key.S_SHEAR_COLORS));
                context.setSessionData(newPref + Key.S_SHEAR_AMOUNTS, context.getSessionData(pref + Key.S_SHEAR_AMOUNTS));
                context.setSessionData(newPref + Key.S_START_EVENT, context.getSessionData(pref + Key.S_START_EVENT));
                context.setSessionData(newPref + Key.S_DISCONNECT_EVENT, context.getSessionData(pref
                        + Key.S_DISCONNECT_EVENT));
                context.setSessionData(newPref + Key.S_DEATH_EVENT, context.getSessionData(pref + Key.S_DEATH_EVENT));
                context.setSessionData(newPref + Key.S_CHAT_EVENTS, context.getSessionData(pref + Key.S_CHAT_EVENTS));
                context.setSessionData(newPref + Key.S_CHAT_EVENT_TRIGGERS, context.getSessionData(pref
                        + Key.S_CHAT_EVENT_TRIGGERS));
                context.setSessionData(newPref + Key.S_FINISH_EVENT, context.getSessionData(pref + Key.S_FINISH_EVENT));
                context.setSessionData(newPref + Key.S_CUSTOM_OBJECTIVES, context.getSessionData(pref
                        + Key.S_CUSTOM_OBJECTIVES));
                context.setSessionData(newPref + Key.S_CUSTOM_OBJECTIVES_DATA, context.getSessionData(pref
                        + Key.S_CUSTOM_OBJECTIVES_DATA));
                context.setSessionData(newPref + Key.S_CUSTOM_OBJECTIVES_COUNT, context.getSessionData(pref
                        + Key.S_CUSTOM_OBJECTIVES_COUNT));
                context.setSessionData(newPref + Key.S_CUSTOM_OBJECTIVES_DATA_DESCRIPTIONS, context.getSessionData(pref
                        + Key.S_CUSTOM_OBJECTIVES_DATA_DESCRIPTIONS));
                context.setSessionData(newPref + Key.S_CUSTOM_OBJECTIVES_DATA_TEMP, context.getSessionData(pref
                        + Key.S_CUSTOM_OBJECTIVES_DATA_TEMP));
                context.setSessionData(newPref + Key.S_PASSWORD_DISPLAYS, context.getSessionData(pref
                        + Key.S_PASSWORD_DISPLAYS));
                context.setSessionData(newPref + Key.S_PASSWORD_PHRASES, context.getSessionData(pref
                        + Key.S_PASSWORD_PHRASES));
                context.setSessionData(newPref + Key.S_OVERRIDE_DISPLAY, context.getSessionData(pref
                        + Key.S_OVERRIDE_DISPLAY));
                context.setSessionData(newPref + Key.S_DELAY, context.getSessionData(pref + Key.S_DELAY));
                context.setSessionData(newPref + Key.S_DELAY_MESSAGE, context.getSessionData(pref
                        + Key.S_DELAY_MESSAGE));
                context.setSessionData(newPref + Key.S_DENIZEN, context.getSessionData(pref + Key.S_DENIZEN));
                context.setSessionData(newPref + Key.S_COMPLETE_MESSAGE, context.getSessionData(pref
                        + Key.S_COMPLETE_MESSAGE));
                context.setSessionData(newPref + Key.S_START_MESSAGE, context.getSessionData(pref + Key.S_START_MESSAGE));
            }
            context.setSessionData(pref + Key.S_BREAK_NAMES, null);
            context.setSessionData(pref + Key.S_BREAK_AMOUNTS, null);
            context.setSessionData(pref + Key.S_BREAK_DURABILITY, null);
            context.setSessionData(pref + Key.S_DAMAGE_NAMES, null);
            context.setSessionData(pref + Key.S_DAMAGE_AMOUNTS, null);
            context.setSessionData(pref + Key.S_DAMAGE_DURABILITY, null);
            context.setSessionData(pref + Key.S_PLACE_NAMES, null);
            context.setSessionData(pref + Key.S_PLACE_AMOUNTS, null);
            context.setSessionData(pref + Key.S_PLACE_DURABILITY, null);
            context.setSessionData(pref + Key.S_USE_NAMES, null);
            context.setSessionData(pref + Key.S_USE_AMOUNTS, null);
            context.setSessionData(pref + Key.S_USE_DURABILITY, null);
            context.setSessionData(pref + Key.S_CUT_NAMES, null);
            context.setSessionData(pref + Key.S_CUT_AMOUNTS, null);
            context.setSessionData(pref + Key.S_CUT_DURABILITY, null);
            context.setSessionData(pref + Key.S_CRAFT_ITEMS, null);
            context.setSessionData(pref + Key.S_SMELT_ITEMS, null);
            context.setSessionData(pref + Key.S_ENCHANT_ITEMS, null);
            context.setSessionData(pref + Key.S_BREW_ITEMS, null);
            context.setSessionData(pref + Key.S_FISH, null);
            context.setSessionData(pref + Key.S_PLAYER_KILL, null);
            context.setSessionData(pref + Key.S_DELIVERY_ITEMS, null);
            context.setSessionData(pref + Key.S_DELIVERY_NPCS, null);
            context.setSessionData(pref + Key.S_DELIVERY_MESSAGES, null);
            context.setSessionData(pref + Key.S_NPCS_TO_TALK_TO, null);
            context.setSessionData(pref + Key.S_NPCS_TO_KILL, null);
            context.setSessionData(pref + Key.S_NPCS_TO_KILL_AMOUNTS, null);
            context.setSessionData(pref + Key.S_MOB_TYPES, null);
            context.setSessionData(pref + Key.S_MOB_AMOUNTS, null);
            context.setSessionData(pref + Key.S_MOB_KILL_LOCATIONS, null);
            context.setSessionData(pref + Key.S_MOB_KILL_LOCATIONS_RADIUS, null);
            context.setSessionData(pref + Key.S_MOB_KILL_LOCATIONS_NAMES, null);
            context.setSessionData(pref + Key.S_REACH_LOCATIONS, null);
            context.setSessionData(pref + Key.S_REACH_LOCATIONS_RADIUS, null);
            context.setSessionData(pref + Key.S_REACH_LOCATIONS_NAMES, null);
            context.setSessionData(pref + Key.S_TAME_TYPES, null);
            context.setSessionData(pref + Key.S_TAME_AMOUNTS, null);
            context.setSessionData(pref + Key.S_SHEAR_COLORS, null);
            context.setSessionData(pref + Key.S_SHEAR_AMOUNTS, null);
            context.setSessionData(pref + Key.S_FINISH_EVENT, null);
            context.setSessionData(pref + Key.S_START_EVENT, null);
            context.setSessionData(pref + Key.S_DEATH_EVENT, null);
            context.setSessionData(pref + Key.S_CHAT_EVENTS, null);
            context.setSessionData(pref + Key.S_CHAT_EVENT_TRIGGERS, null);
            context.setSessionData(pref + Key.S_DISCONNECT_EVENT, null);
            context.setSessionData(pref + Key.S_CUSTOM_OBJECTIVES, null);
            context.setSessionData(pref + Key.S_CUSTOM_OBJECTIVES_DATA, null);
            context.setSessionData(pref + Key.S_CUSTOM_OBJECTIVES_COUNT, null);
            context.setSessionData(pref + Key.S_CUSTOM_OBJECTIVES_DATA_DESCRIPTIONS, null);
            context.setSessionData(pref + Key.S_CUSTOM_OBJECTIVES_DATA_TEMP, null);
            context.setSessionData(pref + Key.S_PASSWORD_DISPLAYS, null);
            context.setSessionData(pref + Key.S_PASSWORD_PHRASES, null);
            context.setSessionData(pref + Key.S_OVERRIDE_DISPLAY, null);
            context.setSessionData(pref + Key.S_DELAY, null);
            context.setSessionData(pref + Key.S_DELAY_MESSAGE, null);
            context.setSessionData(pref + Key.S_DENIZEN, null);
            context.setSessionData(pref + Key.S_COMPLETE_MESSAGE, null);
            context.setSessionData(pref + Key.S_START_MESSAGE, null);
            if (last) {
                break;
            }
        }
        if (!last) {
            context.setSessionData("stage" + (current - 1), null);
        } else {
            context.setSessionData("stage" + (current), null);
        }
    }
}
