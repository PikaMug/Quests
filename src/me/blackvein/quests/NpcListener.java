package me.blackvein.quests;

import ca.xshade.questionmanager.Option;
import ca.xshade.questionmanager.Question;
import java.util.LinkedList;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NpcListener implements Listener{

    Quests plugin;

    public NpcListener(Quests newPlugin){

        plugin = newPlugin;

    }

    @EventHandler
    public void onNPCRightClick(NPCRightClickEvent evt){

        if(plugin.questNPCs.contains(evt.getNPC())){

            final Player player = evt.getClicker();
            final NPC npc = evt.getNPC();

            final Quester quester = plugin.getQuester(player.getName());
            if(quester.hasObjective("talkToNPC")){

                quester.interactWithNPC(evt.getNPC());

            }else {

                for(final Quest q : plugin.quests){

                    if(q.npcStart != null && player.hasPermission("quests.quest")){

                        if(q.npcStart.equals(evt.getNPC()) && quester.completedQuests.contains(q) == false){

                            if(quester.currentQuest == null){

                                LinkedList<Option> options = new LinkedList<Option>();
                                Option yes = new Option("Yes", new Runnable(){

                                    public void run(){

                                        if(q.testRequirements(player) == true){

                                            quester.currentQuest = q;
                                            quester.currentStage = q.stages.getFirst();
                                            quester.addEmpties();
                                            quester.isTalking = false;
                                            if(q.moneyReq > 0){
                                                Quests.economy.withdrawPlayer(quester.name, q.moneyReq);
                                            }

                                            for(int i : q.itemIds){
                                                if (q.removeItems.get(i) == true)
                                                    Quests.removeItem(player.getInventory(), Material.getMaterial(i), q.itemAmounts.get(q.itemIds.indexOf(i)));
                                            }

                                            player.sendMessage(ChatColor.GREEN + "Quest accepted: " + q.name);
                                            player.sendMessage("");
                                            player.sendMessage(ChatColor.GOLD + "---(Objectives)---");
                                            for(String s : quester.getObjectives()){
                                                player.sendMessage(s);
                                            }

                                        }else {

                                            player.sendMessage(npc.getName() + ": " + q.failRequirements);
                                            quester.isTalking = false;

                                        }

                                    }

                                });

                                Option no = new Option("No", new Runnable(){

                                    public void run(){

                                        quester.isTalking = false;
                                        player.sendMessage(ChatColor.YELLOW + "Cancelled.");

                                    }

                                });

                                options.add(yes);
                                options.add(no);

                                if(quester.isTalking == false){

                                    quester.isTalking = true;
                                    Question question = new Question(player.getName(), "Accept quest?", options);
                                    plugin.questioner.getQuestionManager().newQuestion(question);
                                    player.sendMessage(ChatColor.GOLD + "- " + q.name + " -");
                                    player.sendMessage(npc.getName() + ": " + q.description);
                                    try{
                                        plugin.questioner.appendQuestion(question);
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }

                                }

                            }else {

                                player.sendMessage(ChatColor.YELLOW + "You may only have one active Quest.");

                            }

                            break;
                        }

                    }

                }

            }

        }

    }

}
