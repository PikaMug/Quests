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

                if(quester.currentQuest.stages.indexOf(quester.currentStage) == (quester.currentQuest.stages.size() - 1)){

                    if(quester.currentStage.script != null)
                        plugin.trigger.parseQuestTaskTrigger(quester.currentStage.script, player);
                    if(quester.currentStage.event != null)
                        quester.currentStage.event.happen(quester);

                    quester.currentQuest.completeQuest(quester);

                }else {

                    quester.reset();
                    if(quester.currentStage.script != null)
                        plugin.trigger.parseQuestTaskTrigger(quester.currentStage.script, player);
                    if(quester.currentStage.event != null)
                        quester.currentStage.event.happen(quester);
                    quester.currentStage = quester.currentQuest.stages.get(quester.currentQuest.stages.indexOf(quester.currentStage) + 1);
                    quester.addEmpties();
                    quester.delayStartTime = 0;
                    quester.delayTimeLeft = -1;

                    player.sendMessage(ChatColor.GOLD + "---(Objectives)---");
                    for(String s : quester.getObjectives()){

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
