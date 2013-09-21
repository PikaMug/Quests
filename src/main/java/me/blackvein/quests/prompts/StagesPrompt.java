package me.blackvein.quests.prompts;

import me.blackvein.quests.ColorUtil;
import me.blackvein.quests.QuestFactory;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.Lang;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

public class StagesPrompt extends StringPrompt implements ColorUtil{

    private final QuestFactory questFactory;

    public StagesPrompt(QuestFactory qf){

        questFactory = qf;

    }

    @Override
    public String getPromptText(ConversationContext cc) {

        String text = PINK + "- " + PURPLE + Lang.get("stageEditorStages") + PINK + " -\n";

        int stages = getStages(cc);

        for(int i = 1; i <= stages; i++){

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

        try{

            i = Integer.parseInt(string);

        }catch(Exception e){
            return new StagesPrompt(questFactory);
        }

        int stages = getStages(cc);

        if(i < 0)
            return new StagesPrompt(questFactory);
        else if(i < (stages + 1) && i > 0)
            return new CreateStagePrompt((i), questFactory, questFactory.quests.citizens);
        else if(i == (stages + 1))
            return new CreateStagePrompt((stages + 1), questFactory, questFactory.quests.citizens);
        else if(i == (stages + 2))
            return questFactory.returnToMenu();
        else
            return new StagesPrompt(questFactory);

    }

    public static int getStages(ConversationContext cc){

        int num = 1;

        while(true){

            if(cc.getSessionData("stage" + num) != null)
                num++;
            else
                break;

        }

        return (num - 1);

    }

    public static void deleteStage(ConversationContext cc, int stageNum){

        int stages = getStages(cc);
        int current = stageNum;
        String pref = "stage" + current;
        String newPref;
        boolean last = false;

        if(stageNum == stages)
            last = true;

        while(true){

            if(!last){

                current++;

                if(current > stages)
                    break;

                pref = "stage" + current;
                newPref = "stage" + (current - 1);

                cc.setSessionData(newPref + CK.S_BREAK_IDS, cc.getSessionData(pref + CK.S_BREAK_IDS));
                cc.setSessionData(newPref + CK.S_BREAK_AMOUNTS, cc.getSessionData(pref + CK.S_BREAK_AMOUNTS));

                cc.setSessionData(newPref + CK.S_DAMAGE_IDS, cc.getSessionData(pref + CK.S_DAMAGE_IDS));
                cc.setSessionData(newPref + CK.S_DAMAGE_AMOUNTS, cc.getSessionData(pref + CK.S_DAMAGE_AMOUNTS));

                cc.setSessionData(newPref + CK.S_PLACE_IDS, cc.getSessionData(pref + CK.S_PLACE_IDS));
                cc.setSessionData(newPref + CK.S_PLACE_IDS, cc.getSessionData(pref + CK.S_PLACE_AMOUNTS));

                cc.setSessionData(newPref + CK.S_USE_IDS, cc.getSessionData(pref + CK.S_USE_IDS));
                cc.setSessionData(newPref + CK.S_USE_AMOUNTS, cc.getSessionData(pref + CK.S_USE_AMOUNTS));

                cc.setSessionData(newPref + CK.S_CUT_IDS, cc.getSessionData(pref + CK.S_CUT_IDS));
                cc.setSessionData(newPref + CK.S_CUT_AMOUNTS, cc.getSessionData(pref + CK.S_CUT_AMOUNTS));

                cc.setSessionData(newPref + CK.S_FISH, cc.getSessionData(pref + CK.S_FISH));

                cc.setSessionData(newPref + CK.S_PLAYER_KILL, cc.getSessionData(pref + CK.S_PLAYER_KILL));

                cc.setSessionData(newPref + CK.S_ENCHANT_TYPES, cc.getSessionData(pref + CK.S_ENCHANT_TYPES));
                cc.setSessionData(newPref + CK.S_ENCHANT_IDS, cc.getSessionData(pref + CK.S_ENCHANT_IDS));
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

                cc.setSessionData(newPref + CK.S_FINISH_EVENT, cc.getSessionData(pref + CK.S_FINISH_EVENT));

                cc.setSessionData(newPref + CK.S_DELAY, cc.getSessionData(pref + CK.S_DELAY));
                cc.setSessionData(newPref +CK.S_DELAY_MESSAGE, cc.getSessionData(pref + CK.S_DELAY_MESSAGE));

                cc.setSessionData(newPref + CK.S_DENIZEN, cc.getSessionData(pref + CK.S_DENIZEN));

                cc.setSessionData(newPref + CK.S_COMPLETE_MESSAGE, cc.getSessionData(pref + CK.S_COMPLETE_MESSAGE));
                cc.setSessionData(newPref + CK.S_START_MESSAGE, cc.getSessionData(pref + CK.S_START_MESSAGE));

            }


            cc.setSessionData(pref + CK.S_BREAK_IDS, null);
            cc.setSessionData(pref + CK.S_BREAK_AMOUNTS, null);

            cc.setSessionData(pref + CK.S_DAMAGE_IDS, null);
            cc.setSessionData(pref + CK.S_DAMAGE_AMOUNTS, null);

            cc.setSessionData(pref + CK.S_PLACE_IDS, null);
            cc.setSessionData(pref + CK.S_PLACE_AMOUNTS, null);

            cc.setSessionData(pref + CK.S_USE_IDS, null);
            cc.setSessionData(pref + CK.S_USE_AMOUNTS, null);

            cc.setSessionData(pref + CK.S_CUT_IDS, null);
            cc.setSessionData(pref + CK.S_CUT_AMOUNTS, null);

            cc.setSessionData(pref + CK.S_FISH, null);

            cc.setSessionData(pref + CK.S_PLAYER_KILL, null);

            cc.setSessionData(pref + CK.S_ENCHANT_TYPES, null);
            cc.setSessionData(pref + CK.S_ENCHANT_IDS, null);
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

            cc.setSessionData(pref + CK.S_DELAY, null);
            cc.setSessionData(pref + CK.S_DELAY_MESSAGE, null);

            cc.setSessionData(pref + CK.S_DENIZEN, null);

            cc.setSessionData(pref + CK.S_COMPLETE_MESSAGE, null);
            cc.setSessionData(pref + CK.S_START_MESSAGE, null);

            if(last)
                break;

        }

        if(!last)
            cc.setSessionData("stage" + (current - 1), null);
        else
            cc.setSessionData("stage" + (current), null);

    }


}
