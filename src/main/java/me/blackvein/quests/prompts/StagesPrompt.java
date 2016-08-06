package me.blackvein.quests.prompts;

import me.blackvein.quests.util.ColorUtil;
import me.blackvein.quests.QuestFactory;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.Lang;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

public class StagesPrompt extends StringPrompt implements ColorUtil {

    private final QuestFactory questFactory;

    public StagesPrompt(QuestFactory qf) {

        questFactory = qf;

    }

    @Override
    public String getPromptText(ConversationContext cc) {

        String text = PINK + "- " + PURPLE + Lang.get("stageEditorStages") + PINK + " -\n";

        int stages = getStages(cc);

        for (int i = 1; i <= stages; i++) {

            text += BOLD + "" + GREEN + i + ". " + RESET + GOLD + Lang.get("stageEditorEditStage") + " " + i + "\n";

        }

        stages++;
        text += "\n" + BOLD + "" + GREEN + stages + ". " + RESET + YELLOW + Lang.get("stageEditorNewStage");
        stages++;
        text += "\n" + BOLD + "" + BLUE + stages + ". " + RESET + YELLOW + Lang.get("done");

        return text;
    }

    @Override
    public Prompt acceptInput(ConversationContext cc, String string) {

        int i;

        try {

            i = Integer.parseInt(string);

        } catch (NumberFormatException e) {
            return new StagesPrompt(questFactory);
        }

        int stages = getStages(cc);

        if (i < 0) {
            return new StagesPrompt(questFactory);
        } else if (i < (stages + 1) && i > 0) {
            return new CreateStagePrompt((i), questFactory, questFactory.quests.citizens);
        } else if (i == (stages + 1)) {
            return new CreateStagePrompt((stages + 1), questFactory, questFactory.quests.citizens);
        } else if (i == (stages + 2)) {
            return questFactory.returnToMenu();
        } else {
            return new StagesPrompt(questFactory);
        }

    }

    public static int getStages(ConversationContext cc) {

        int num = 1;

        while (true) {

            if (cc.getSessionData("stage" + num) != null) {
                num++;
            } else {
                break;
            }

        }

        return (num - 1);

    }

    public static void deleteStage(ConversationContext cc, int stageNum) {

        int stages = getStages(cc);
        int current = stageNum;
        String pref = "stage" + current;
        String newPref;
        boolean last = false;

        if (stageNum == stages) {
            last = true;
        }

        while (true) {

            if (!last) {

                current++;

                if (current > stages) {
                    break;
                }

                pref = "stage" + current;
                newPref = "stage" + (current - 1);

                cc.setSessionData(newPref + CK.S_BREAK_NAMES, cc.getSessionData(pref + CK.S_BREAK_NAMES));
                cc.setSessionData(newPref + CK.S_BREAK_AMOUNTS, cc.getSessionData(pref + CK.S_BREAK_AMOUNTS));
                cc.setSessionData(newPref + CK.S_BREAK_DURABILITY, cc.getSessionData(pref + CK.S_BREAK_DURABILITY));

                cc.setSessionData(newPref + CK.S_DAMAGE_NAMES, cc.getSessionData(pref + CK.S_DAMAGE_NAMES));
                cc.setSessionData(newPref + CK.S_DAMAGE_AMOUNTS, cc.getSessionData(pref + CK.S_DAMAGE_AMOUNTS));
                cc.setSessionData(newPref + CK.S_DAMAGE_DURABILITY, cc.getSessionData(pref + CK.S_DAMAGE_DURABILITY));

                cc.setSessionData(newPref + CK.S_PLACE_NAMES, cc.getSessionData(pref + CK.S_PLACE_NAMES));
                cc.setSessionData(newPref + CK.S_PLACE_NAMES, cc.getSessionData(pref + CK.S_PLACE_AMOUNTS));
                cc.setSessionData(newPref + CK.S_PLACE_DURABILITY, cc.getSessionData(pref + CK.S_PLACE_DURABILITY));

                cc.setSessionData(newPref + CK.S_USE_NAMES, cc.getSessionData(pref + CK.S_USE_NAMES));
                cc.setSessionData(newPref + CK.S_USE_AMOUNTS, cc.getSessionData(pref + CK.S_USE_AMOUNTS));
                cc.setSessionData(newPref + CK.S_USE_DURABILITY, cc.getSessionData(pref + CK.S_USE_DURABILITY));

                cc.setSessionData(newPref + CK.S_CUT_NAMES, cc.getSessionData(pref + CK.S_CUT_NAMES));
                cc.setSessionData(newPref + CK.S_CUT_AMOUNTS, cc.getSessionData(pref + CK.S_CUT_AMOUNTS));
                cc.setSessionData(newPref + CK.S_CUT_DURABILITY, cc.getSessionData(pref + CK.S_CUT_DURABILITY));

                cc.setSessionData(newPref + CK.S_FISH, cc.getSessionData(pref + CK.S_FISH));

                cc.setSessionData(newPref + CK.S_PLAYER_KILL, cc.getSessionData(pref + CK.S_PLAYER_KILL));

                cc.setSessionData(newPref + CK.S_ENCHANT_TYPES, cc.getSessionData(pref + CK.S_ENCHANT_TYPES));
                cc.setSessionData(newPref + CK.S_ENCHANT_NAMES, cc.getSessionData(pref + CK.S_ENCHANT_NAMES));
                cc.setSessionData(newPref + CK.S_ENCHANT_AMOUNTS, cc.getSessionData(pref + CK.S_ENCHANT_AMOUNTS));

                cc.setSessionData(newPref + CK.S_DELIVERY_ITEMS, cc.getSessionData(pref + CK.S_DELIVERY_ITEMS));
                cc.setSessionData(newPref + CK.S_DELIVERY_NPCS, cc.getSessionData(pref + CK.S_DELIVERY_NPCS));
                cc.setSessionData(newPref + CK.S_DELIVERY_MESSAGES, cc.getSessionData(pref + CK.S_DELIVERY_MESSAGES));

                cc.setSessionData(newPref + CK.S_NPCS_TO_TALK_TO, cc.getSessionData(pref + CK.S_NPCS_TO_TALK_TO));

                cc.setSessionData(newPref + CK.S_NPCS_TO_KILL, cc.getSessionData(pref + CK.S_NPCS_TO_KILL));
                cc.setSessionData(newPref + CK.S_NPCS_TO_KILL_AMOUNTS, cc.getSessionData(pref + CK.S_NPCS_TO_KILL_AMOUNTS));

                cc.setSessionData(newPref + CK.S_MOB_TYPES, cc.getSessionData(pref + CK.S_MOB_TYPES));
                cc.setSessionData(newPref + CK.S_MOB_AMOUNTS, cc.getSessionData(pref + CK.S_MOB_AMOUNTS));
                cc.setSessionData(newPref + CK.S_MOB_KILL_LOCATIONS, cc.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS));
                cc.setSessionData(newPref + CK.S_MOB_KILL_LOCATIONS_RADIUS, cc.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_RADIUS));
                cc.setSessionData(newPref + CK.S_MOB_KILL_LOCATIONS_NAMES, cc.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_NAMES));

                cc.setSessionData(newPref + CK.S_REACH_LOCATIONS, cc.getSessionData(pref + CK.S_REACH_LOCATIONS));
                cc.setSessionData(newPref + CK.S_REACH_LOCATIONS_RADIUS, cc.getSessionData(pref + CK.S_REACH_LOCATIONS_RADIUS));
                cc.setSessionData(newPref + CK.S_REACH_LOCATIONS_NAMES, cc.getSessionData(pref + CK.S_REACH_LOCATIONS_NAMES));

                cc.setSessionData(newPref + CK.S_TAME_TYPES, cc.getSessionData(pref + CK.S_TAME_TYPES));
                cc.setSessionData(newPref + CK.S_TAME_AMOUNTS, cc.getSessionData(pref + CK.S_TAME_AMOUNTS));

                cc.setSessionData(newPref + CK.S_SHEAR_COLORS, cc.getSessionData(pref + CK.S_SHEAR_COLORS));
                cc.setSessionData(newPref + CK.S_SHEAR_AMOUNTS, cc.getSessionData(pref + CK.S_SHEAR_AMOUNTS));

                cc.setSessionData(newPref + CK.S_START_EVENT, cc.getSessionData(pref + CK.S_START_EVENT));
                cc.setSessionData(newPref + CK.S_DISCONNECT_EVENT, cc.getSessionData(pref + CK.S_DISCONNECT_EVENT));
                cc.setSessionData(newPref + CK.S_DEATH_EVENT, cc.getSessionData(pref + CK.S_DEATH_EVENT));
                cc.setSessionData(newPref + CK.S_CHAT_EVENTS, cc.getSessionData(pref + CK.S_CHAT_EVENTS));
                cc.setSessionData(newPref + CK.S_CHAT_EVENT_TRIGGERS, cc.getSessionData(pref + CK.S_CHAT_EVENT_TRIGGERS));
                cc.setSessionData(newPref + CK.S_FINISH_EVENT, cc.getSessionData(pref + CK.S_FINISH_EVENT));

                cc.setSessionData(newPref + CK.S_CUSTOM_OBJECTIVES, cc.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES));
                cc.setSessionData(newPref + CK.S_CUSTOM_OBJECTIVES_DATA, cc.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES_COUNT));
                cc.setSessionData(newPref + CK.S_CUSTOM_OBJECTIVES_COUNT, cc.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES_COUNT));
                cc.setSessionData(newPref + CK.S_CUSTOM_OBJECTIVES_DATA_DESCRIPTIONS, cc.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA_DESCRIPTIONS));
                cc.setSessionData(newPref + CK.S_CUSTOM_OBJECTIVES_DATA_TEMP, cc.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA_TEMP));

                cc.setSessionData(newPref + CK.S_PASSWORD_DISPLAYS, cc.getSessionData(pref + CK.S_PASSWORD_DISPLAYS));
                cc.setSessionData(newPref + CK.S_PASSWORD_PHRASES, cc.getSessionData(pref + CK.S_PASSWORD_PHRASES));

                cc.setSessionData(newPref + CK.S_OVERRIDE_DISPLAY, cc.getSessionData(pref + CK.S_OVERRIDE_DISPLAY));

                cc.setSessionData(newPref + CK.S_DELAY, cc.getSessionData(pref + CK.S_DELAY));
                cc.setSessionData(newPref + CK.S_DELAY_MESSAGE, cc.getSessionData(pref + CK.S_DELAY_MESSAGE));

                cc.setSessionData(newPref + CK.S_DENIZEN, cc.getSessionData(pref + CK.S_DENIZEN));

                cc.setSessionData(newPref + CK.S_COMPLETE_MESSAGE, cc.getSessionData(pref + CK.S_COMPLETE_MESSAGE));
                cc.setSessionData(newPref + CK.S_START_MESSAGE, cc.getSessionData(pref + CK.S_START_MESSAGE));

            }

            cc.setSessionData(pref + CK.S_BREAK_NAMES, null);
            cc.setSessionData(pref + CK.S_BREAK_AMOUNTS, null);
            cc.setSessionData(pref + CK.S_BREAK_DURABILITY, null);

            cc.setSessionData(pref + CK.S_DAMAGE_NAMES, null);
            cc.setSessionData(pref + CK.S_DAMAGE_AMOUNTS, null);
            cc.setSessionData(pref + CK.S_DAMAGE_DURABILITY, null);

            cc.setSessionData(pref + CK.S_PLACE_NAMES, null);
            cc.setSessionData(pref + CK.S_PLACE_AMOUNTS, null);
            cc.setSessionData(pref + CK.S_PLACE_DURABILITY, null);

            cc.setSessionData(pref + CK.S_USE_NAMES, null);
            cc.setSessionData(pref + CK.S_USE_AMOUNTS, null);
            cc.setSessionData(pref + CK.S_USE_DURABILITY, null);

            cc.setSessionData(pref + CK.S_CUT_NAMES, null);
            cc.setSessionData(pref + CK.S_CUT_AMOUNTS, null);
            cc.setSessionData(pref + CK.S_CUT_DURABILITY, null);

            cc.setSessionData(pref + CK.S_FISH, null);

            cc.setSessionData(pref + CK.S_PLAYER_KILL, null);

            cc.setSessionData(pref + CK.S_ENCHANT_TYPES, null);
            cc.setSessionData(pref + CK.S_ENCHANT_NAMES, null);
            cc.setSessionData(pref + CK.S_ENCHANT_AMOUNTS, null);

            cc.setSessionData(pref + CK.S_DELIVERY_ITEMS, null);
            cc.setSessionData(pref + CK.S_DELIVERY_NPCS, null);
            cc.setSessionData(pref + CK.S_DELIVERY_MESSAGES, null);

            cc.setSessionData(pref + CK.S_NPCS_TO_TALK_TO, null);

            cc.setSessionData(pref + CK.S_NPCS_TO_KILL, null);
            cc.setSessionData(pref + CK.S_NPCS_TO_KILL_AMOUNTS, null);

            cc.setSessionData(pref + CK.S_MOB_TYPES, null);
            cc.setSessionData(pref + CK.S_MOB_AMOUNTS, null);
            cc.setSessionData(pref + CK.S_MOB_KILL_LOCATIONS, null);
            cc.setSessionData(pref + CK.S_MOB_KILL_LOCATIONS_RADIUS, null);
            cc.setSessionData(pref + CK.S_MOB_KILL_LOCATIONS_NAMES, null);

            cc.setSessionData(pref + CK.S_REACH_LOCATIONS, null);
            cc.setSessionData(pref + CK.S_REACH_LOCATIONS_RADIUS, null);
            cc.setSessionData(pref + CK.S_REACH_LOCATIONS_NAMES, null);

            cc.setSessionData(pref + CK.S_TAME_TYPES, null);
            cc.setSessionData(pref + CK.S_TAME_AMOUNTS, null);

            cc.setSessionData(pref + CK.S_SHEAR_COLORS, null);
            cc.setSessionData(pref + CK.S_SHEAR_AMOUNTS, null);

            cc.setSessionData(pref + CK.S_FINISH_EVENT, null);
            cc.setSessionData(pref + CK.S_START_EVENT, null);
            cc.setSessionData(pref + CK.S_DEATH_EVENT, null);
            cc.setSessionData(pref + CK.S_CHAT_EVENTS, null);
            cc.setSessionData(pref + CK.S_CHAT_EVENT_TRIGGERS, null);
            cc.setSessionData(pref + CK.S_DISCONNECT_EVENT, null);

            cc.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES, null);
            cc.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA, null);
            cc.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES_COUNT, null);
            cc.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA_DESCRIPTIONS, null);
            cc.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA_TEMP, null);

            cc.setSessionData(pref + CK.S_PASSWORD_DISPLAYS, null);
            cc.setSessionData(pref + CK.S_PASSWORD_PHRASES, null);

            cc.setSessionData(pref + CK.S_OVERRIDE_DISPLAY, null);

            cc.setSessionData(pref + CK.S_DELAY, null);
            cc.setSessionData(pref + CK.S_DELAY_MESSAGE, null);

            cc.setSessionData(pref + CK.S_DENIZEN, null);

            cc.setSessionData(pref + CK.S_COMPLETE_MESSAGE, null);
            cc.setSessionData(pref + CK.S_START_MESSAGE, null);

            if (last) {
                break;
            }

        }

        if (!last) {
            cc.setSessionData("stage" + (current - 1), null);
        } else {
            cc.setSessionData("stage" + (current), null);
        }

    }

}
