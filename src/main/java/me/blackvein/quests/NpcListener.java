package me.blackvein.quests;

import java.text.MessageFormat;
import java.util.LinkedList;
import me.blackvein.quests.util.ItemUtil;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCDeathEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.ChatColor;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.Conversation;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class NpcListener implements Listener {

    final Quests plugin;

    public NpcListener(Quests newPlugin) {

        plugin = newPlugin;

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onNPCRightClick(NPCRightClickEvent evt) {

        if (evt.getClicker().isConversing() == false) {

            final Player player = evt.getClicker();
            final Quester quester = plugin.getQuester(player.getName());
            boolean delivery = false;

            if (quester.hasObjective("deliverItem") && player.getItemInHand() != null) {

                ItemStack hand = player.getItemInHand();

                ItemStack found = null;

                for(ItemStack is : quester.currentStage.itemsToDeliver){

                    if(ItemUtil.compareItems(is, hand, true) == 0){
                        found = is;
                        break;
                    }

                }

                if (found != null) {

                    NPC clicked = evt.getNPC();

                    for (NPC n : quester.currentStage.itemDeliveryTargets) {
                        if (n.getId() == clicked.getId()) {
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

                        LinkedList<Quest> npcQuests = new LinkedList<Quest>();

                        for (Quest q : plugin.getQuests()) {

                            if (q.npcStart != null && q.npcStart.getId() == evt.getNPC().getId()) {
                            	if (Quests.ignoreLockedQuests) {
                            		if (q.testRequirements(quester) && (q.redoDelay <= 0)) {
                            			npcQuests.add(q);
                            		}
                            	} else {
                            		npcQuests.add(q);
                            	}
                            }

                        }



                        if (npcQuests.isEmpty() == false && npcQuests.size() > 1) {

                            Conversation c = plugin.NPCConversationFactory.buildConversation((Conversable) player);
                            c.getContext().setSessionData("quests", npcQuests);
                            c.getContext().setSessionData("npc", evt.getNPC().getName());
                            c.begin();

                        } else if (npcQuests.size() == 1) {

                            Quest q = npcQuests.get(0);

                            if (!quester.completedQuests.contains(q.name)) {

                                if (quester.currentQuest == null) {

                                    quester.questToTake = q.name;

                                    String s = extracted(quester);
                                    
                                    for (String msg : s.split("<br>")) {
                                    	player.sendMessage(msg);
                                    }
                                    
                                    plugin.conversationFactory.buildConversation((Conversable) player).begin();

                                } else if (quester.currentQuest.equals(q) == false) {

                                    player.sendMessage(ChatColor.YELLOW + "You may only have one active Quest.");

                                }

                            } else if (quester.completedQuests.contains(q.name)) {

                                if (quester.currentQuest == null) {

                                    if (quester.getDifference(q) > 0) {
                                        player.sendMessage(ChatColor.YELLOW + "You may not take " + ChatColor.AQUA + q.name + ChatColor.YELLOW + " again for another " + ChatColor.DARK_PURPLE + Quests.getTime(quester.getDifference(q)) + ChatColor.YELLOW + ".");
                                    } else if (q.redoDelay < 0) {
                                        player.sendMessage(ChatColor.YELLOW + "You have already completed " + ChatColor.AQUA + q.name + ChatColor.YELLOW + ".");
                                    } else {
                                        quester.questToTake = q.name;
                                        String s = extracted(quester);
                                        
                                        for (String msg : s.split("<br>")) {
                                        	player.sendMessage(msg);
                                        }

                                        plugin.conversationFactory.buildConversation((Conversable) player).begin();
                                    }

                                } else if (quester.currentQuest.equals(q) == false) {

                                    player.sendMessage(ChatColor.YELLOW + "You may only have one active Quest.");

                                }

                            }

                        }


                    }

                }

            }

        }
    }

    @EventHandler
    public void onNPCDeath(NPCDeathEvent evt) {

        if (evt.getNPC().getBukkitEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {

            EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) evt.getNPC().getBukkitEntity().getLastDamageCause();
            Entity damager = damageEvent.getDamager();

            if (damager != null) {

                if (damager instanceof Projectile) {

                    Projectile p = (Projectile) damager;
                    if (p.getShooter() instanceof Player) {

                        Player player = (Player) p.getShooter();
                        boolean okay = true;

                        if (plugin.citizens != null) {
                            if (CitizensAPI.getNPCRegistry().isNPC(player)) {
                                okay = false;
                            }
                        }

                        if (okay) {

                            Quester quester = plugin.getQuester(player.getName());
                            if (quester.hasObjective("killNPC")) {
                                quester.killNPC(evt.getNPC());
                            }

                        }
                    }

                } else if (damager instanceof Player) {

                    boolean okay = true;

                    if (plugin.citizens != null) {
                        if (plugin.citizens.getNPCRegistry().isNPC(damager)) {
                            okay = false;
                        }
                    }

                    if (okay) {

                        Player player = (Player) damager;
                        Quester quester = plugin.getQuester(player.getName());
                        if (quester.hasObjective("killNPC")) {
                            quester.killNPC(evt.getNPC());
                        }

                    }
                }

            }

        }

    }

    private String extracted(final Quester quester) {
        return MessageFormat.format("{0}- {1}{2}{3} -\n\n{4}{5}\n",
                ChatColor.GOLD,
                ChatColor.DARK_PURPLE,
                quester.questToTake,
                ChatColor.GOLD,
                ChatColor.RESET, plugin.getQuest(quester.questToTake).description);
    }
}