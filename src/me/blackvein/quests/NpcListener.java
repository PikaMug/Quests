package me.blackvein.quests;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.ChatColor;
import org.bukkit.conversations.Conversable;
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

                        if(q.npcStart.equals(evt.getNPC()) && quester.completedQuests.contains(q.name) == false){

                            if(quester.currentQuest == null){

                                quester.questToTake = q;
                                plugin.conversationFactory.buildConversation((Conversable)player).begin();

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
