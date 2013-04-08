package me.blackvein.quests;

import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.ChatColor;
import org.bukkit.conversations.Conversable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class NpcListener implements Listener {

    Quests plugin;

    public NpcListener(Quests newPlugin) {

        plugin = newPlugin;

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onNPCRightClick(NPCRightClickEvent evt) {

        final Player player = evt.getClicker();
        final Quester quester = plugin.getQuester(player.getName());
        boolean delivery = false;
        
        if(quester.hasObjective("deliverItem") && player.getItemInHand() != null){
            
            ItemStack hand = player.getItemInHand();
            if(quester.itemsDelivered.containsKey(hand.getType())){
                
                NPC clicked = evt.getNPC();
                
                for(NPC n : quester.currentStage.itemDeliveryTargets){
                    if(n.getId() == clicked.getId()){
                        quester.deliverItem(hand);
                        delivery = true;
                        break;
                    }
                }
                
            }
            
        }
        
        if (plugin.questNPCs.contains(evt.getNPC()) && delivery == false) {

            if (plugin.checkQuester(player.getName()) == false) {
                if (quester.hasObjective("talkToNPC")) {

                    quester.interactWithNPC(evt.getNPC());

                } else {

                    for (Quest q : plugin.quests) {

                        if (q.npcStart != null && player.hasPermission("quests.quest")) {

                            if (q.npcStart.equals(evt.getNPC()) && quester.completedQuests.contains(q.name) == false) {

                                if (quester.currentQuest == null) {

                                    quester.questToTake = q.name;

                                    String s =
                                            ChatColor.GOLD + "- " + ChatColor.DARK_PURPLE + quester.questToTake + ChatColor.GOLD + " -\n"
                                            + "\n"
                                            + ChatColor.RESET + plugin.getQuest(quester.questToTake).description + "\n";
                                    player.sendMessage(s);
                                    plugin.conversationFactory.buildConversation((Conversable) player).begin();

                                } else if (quester.currentQuest.equals(q) == false) {

                                    player.sendMessage(ChatColor.YELLOW + "You may only have one active Quest.");

                                }

                                break;

                            } else if (q.npcStart.equals(evt.getNPC()) && quester.completedQuests.contains(q.name) == true) {

                                if (quester.currentQuest == null) {

                                    if (quester.getDifference(q) > 0) {
                                        player.sendMessage(ChatColor.YELLOW + "You may not take " + ChatColor.AQUA + q.name + ChatColor.YELLOW + " again for another " + ChatColor.DARK_PURPLE + Quests.getTime(quester.getDifference(q)) + ChatColor.YELLOW + ".");
                                    } else if (quester.completedQuests.contains(q.name) && q.redoDelay < 0) {
                                        player.sendMessage(ChatColor.YELLOW + "You have already completed " + ChatColor.AQUA + q.name + ChatColor.YELLOW + ".");
                                    }else{
                                        
                                        quester.questToTake = q.name;
                                        String s =
                                            ChatColor.GOLD + "- " + ChatColor.DARK_PURPLE + quester.questToTake + ChatColor.GOLD + " -\n"
                                            + "\n"
                                            + ChatColor.RESET + plugin.getQuest(quester.questToTake).description + "\n";

                                        player.sendMessage(s);
                                        
                                        plugin.conversationFactory.buildConversation((Conversable) player).begin();
                                    }

                                } else if (quester.currentQuest.equals(q) == false) {

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

    @EventHandler
    public void onNPCDespawn(NPCDespawnEvent evt) {
    }
}
