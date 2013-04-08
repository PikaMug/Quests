package me.blackvein.quests.prompts;

import me.blackvein.quests.QuestFactory;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

public class StagesPrompt extends StringPrompt{

    private final QuestFactory questFactory;

    static final ChatColor BOLD = ChatColor.BOLD;
    static final ChatColor ITALIC = ChatColor.ITALIC;
    static final ChatColor AQUA = ChatColor.AQUA;
    static final ChatColor DARKAQUA = ChatColor.DARK_AQUA;
    static final ChatColor BLUE = ChatColor.BLUE;
    static final ChatColor GOLD = ChatColor.GOLD;
    static final ChatColor PINK = ChatColor.LIGHT_PURPLE;
    static final ChatColor PURPLE = ChatColor.DARK_PURPLE;
    static final ChatColor GREEN = ChatColor.GREEN;
    static final ChatColor RED = ChatColor.RED;
    static final ChatColor DARKRED = ChatColor.DARK_RED;
    static final ChatColor YELLOW = ChatColor.YELLOW;
    static final ChatColor GRAY = ChatColor.GRAY;
    static final ChatColor RESET = ChatColor.RESET;

    public StagesPrompt(QuestFactory qf){

        questFactory = qf;

    }

    @Override
    public String getPromptText(ConversationContext cc) {

        String text = PINK + "- " + PURPLE + "Stages" + PINK + " -\n";

        int stages = getStages(cc);

        for(int i = 1; i <= stages; i++){

            text += BOLD + "" + GREEN + i + ". " + RESET + GOLD + "Edit Stage " + i + "\n";

        }

        stages++;
        text += "\n" + BOLD + "" + GREEN + stages + ". " + RESET + YELLOW + "Add new Stage";
        stages++;
        text += "\n" + BOLD + "" + BLUE + stages + ". " + RESET + YELLOW + "Done";

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

                cc.setSessionData(newPref + "breakIds", cc.getSessionData(pref + "breakIds"));
                cc.setSessionData(newPref + "breakAmounts", cc.getSessionData(pref + "breakAmounts"));

                cc.setSessionData(newPref + "damageIds", cc.getSessionData(pref + "damageIds"));
                cc.setSessionData(newPref + "damageAmounts", cc.getSessionData(pref + "damageAmounts"));

                cc.setSessionData(newPref + "placeIds", cc.getSessionData(pref + "placeIds"));
                cc.setSessionData(newPref + "placeAmounts", cc.getSessionData(pref + "placeAmounts"));

                cc.setSessionData(newPref + "useIds", cc.getSessionData(pref + "useIds"));
                cc.setSessionData(newPref + "useAmounts", cc.getSessionData(pref + "useAmounts"));

                cc.setSessionData(newPref + "cutIds", cc.getSessionData(pref + "cutIds"));
                cc.setSessionData(newPref + "cutAmounts", cc.getSessionData(pref + "cutAmounts"));

                cc.setSessionData(newPref + "fish", cc.getSessionData(pref + "fish"));

                cc.setSessionData(newPref + "playerKill", cc.getSessionData(pref + "playerKill"));

                cc.setSessionData(newPref + "enchantTypes", cc.getSessionData(pref + "enchantTypes"));
                cc.setSessionData(newPref + "enchantIds", cc.getSessionData(pref + "enchantIds"));
                cc.setSessionData(newPref + "enchantAmounts", cc.getSessionData(pref + "enchantAmounts"));

                cc.setSessionData(newPref + "deliveryIds", cc.getSessionData(pref + "deliveryIds"));
                cc.setSessionData(newPref + "deliveryAmounts", cc.getSessionData(pref + "deliveryAmounts"));
                cc.setSessionData(newPref + "deliveryNPCs", cc.getSessionData(pref + "deliveryNPCs"));
                cc.setSessionData(newPref + "deliveryMessages", cc.getSessionData(pref + "deliveryMessages"));

                cc.setSessionData(newPref + "npcIdsToTalkTo", cc.getSessionData(pref + "npcIdsToTalkTo"));

                cc.setSessionData(newPref + "mobTypes", cc.getSessionData(pref + "mobTypes"));
                cc.setSessionData(newPref + "mobAmounts", cc.getSessionData(pref + "mobAmounts"));
                cc.setSessionData(newPref + "killLocations", cc.getSessionData(pref + "killLocations"));
                cc.setSessionData(newPref + "killLocationRadii", cc.getSessionData(pref + "killLocationRadii"));
                cc.setSessionData(newPref + "killLocationNames", cc.getSessionData(pref + "killLocationNames"));

                cc.setSessionData(newPref + "reachLocations", cc.getSessionData(pref + "reachLocations"));
                cc.setSessionData(newPref + "reachLocationRadii", cc.getSessionData(pref + "reachLocationRadii"));
                cc.setSessionData(newPref + "reachLocationNames", cc.getSessionData(pref + "reachLocationNames"));

                cc.setSessionData(newPref + "tameTypes", cc.getSessionData(pref + "tameTypes"));
                cc.setSessionData(newPref + "tameAmounts", cc.getSessionData(pref + "tameAmounts"));

                cc.setSessionData(newPref + "shearColors", cc.getSessionData(pref + "shearColors"));
                cc.setSessionData(newPref + "shearAmounts", cc.getSessionData(pref + "shearAmounts"));

                cc.setSessionData(newPref + "event", cc.getSessionData(pref + "event"));

                cc.setSessionData(newPref + "delay", cc.getSessionData(pref + "delay"));
                cc.setSessionData(newPref + "delayMessage", cc.getSessionData(pref + "delayMessage"));

                cc.setSessionData(newPref + "denizen", cc.getSessionData(pref + "denizen"));

            }


            cc.setSessionData(pref + "breakIds", null);
            cc.setSessionData(pref + "breakAmounts", null);

            cc.setSessionData(pref + "damageIds", null);
            cc.setSessionData(pref + "damageAmounts", null);

            cc.setSessionData(pref + "placeIds", null);
            cc.setSessionData(pref + "placeAmounts", null);

            cc.setSessionData(pref + "useIds", null);
            cc.setSessionData(pref + "useAmounts", null);

            cc.setSessionData(pref + "cutIds", null);
            cc.setSessionData(pref + "cutAmounts", null);

            cc.setSessionData(pref + "fish", null);

            cc.setSessionData(pref + "playerKill", null);

            cc.setSessionData(pref + "enchantTypes", null);
            cc.setSessionData(pref + "enchantIds", null);
            cc.setSessionData(pref + "enchantAmounts", null);

            cc.setSessionData(pref + "deliveryIds", null);
            cc.setSessionData(pref + "deliveryAmounts", null);
            cc.setSessionData(pref + "deliveryNPCs", null);
            cc.setSessionData(pref + "deliveryMessages", null);

            cc.setSessionData(pref + "npcIdsToTalkTo", null);

            cc.setSessionData(pref + "mobTypes", null);
            cc.setSessionData(pref + "mobAmounts", null);
            cc.setSessionData(pref + "killLocations", null);
            cc.setSessionData(pref + "killLocationRadii", null);
            cc.setSessionData(pref + "killLocationNames", null);

            cc.setSessionData(pref + "reachLocations", null);
            cc.setSessionData(pref + "reachLocationRadii", null);
            cc.setSessionData(pref + "reachLocationNames", null);

            cc.setSessionData(pref + "tameTypes", null);
            cc.setSessionData(pref + "tameAmounts", null);

            cc.setSessionData(pref + "shearColors", null);
            cc.setSessionData(pref + "shearAmounts", null);

            cc.setSessionData(pref + "event", null);

            cc.setSessionData(pref + "delay", null);
            cc.setSessionData(pref + "delayMessage", null);

            cc.setSessionData(pref + "denizen", null);

            if(last)
                break;

        }

        if(!last)
            cc.setSessionData("stage" + (current - 1), null);
        else
            cc.setSessionData("stage" + (current), null);

    }


}
