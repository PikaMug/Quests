package me.blackvein.quests;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class StageTimer implements Runnable{

    Quester quester;
    Quests plugin;

    public StageTimer(Quests quests, Quester q){

        quester = q;
        plugin = quests;

    }

    @Override
    public void run(){

        if(quester.delayOver){

            Player player = plugin.getServer().getPlayerExact(quester.name);

            if(quester.currentQuest != null){

                if(quester.currentQuest.orderedStages.indexOf(quester.currentStage) == (quester.currentQuest.orderedStages.size() - 1)){

                    if(quester.currentStage.script != null)
                        plugin.trigger.parseQuestTaskTrigger(quester.currentStage.script, player);
                    if(quester.currentStage.finishEvent != null)
                        quester.currentStage.finishEvent.fire(quester);

                    quester.currentQuest.completeQuest(quester);

                }else {

                    quester.resetObjectives();
                    if(quester.currentStage.script != null)
                        plugin.trigger.parseQuestTaskTrigger(quester.currentStage.script, player);
                    if(quester.currentStage.finishEvent != null)
                        quester.currentStage.finishEvent.fire(quester);
                    quester.currentStage = quester.currentQuest.orderedStages.get(quester.currentStageIndex +  1);
                    quester.currentStageIndex++;
                    quester.addEmpties();
                    quester.delayStartTime = 0;
                    quester.delayTimeLeft = -1;

                    player.sendMessage(ChatColor.GOLD + "---(Objectives)---");
                    for(String s : quester.getObjectivesReal()){

                        player.sendMessage(s);

                    }

                    String stageStartMessage = quester.currentStage.startMessage;
                    if (stageStartMessage != null) {
                            quester.getPlayer().sendMessage(Quests.parseString(stageStartMessage, quester.currentQuest));
                    }

                }

            }

            quester.delayOver = true;

        }

    }

}
