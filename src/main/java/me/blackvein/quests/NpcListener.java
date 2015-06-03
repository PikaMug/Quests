package me.blackvein.quests;

import java.text.MessageFormat;
import java.util.LinkedList;

import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCDeathEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.ChatColor;
import org.bukkit.Material;
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

        if (plugin.questFactory.selectingNPCs.contains(evt.getClicker())) {
            evt.getClicker().sendMessage(ChatColor.GREEN + evt.getNPC().getName() + ": " + ChatColor.DARK_GREEN + "ID " + evt.getNPC().getId());
            return;
        }

        if (evt.getClicker().isConversing() == false) {

            final Player player = evt.getClicker();
            final Quester quester = plugin.getQuester(player.getUniqueId());
            boolean delivery = false;

            for (Quest quest : quester.currentQuests.keySet()) {

                if (quester.hasObjective(quest, "deliverItem") && player.getItemInHand() != null) {

                    ItemStack hand = player.getItemInHand();

                    ItemStack found = null;

                    for (ItemStack is : quester.getCurrentStage(quest).itemsToDeliver) {

                        if (ItemUtil.compareItems(is, hand, true) == 0) {
                            found = is;
                            break;
                        }

                    }

                    NPC clicked = evt.getNPC();

                    if (found != null) {

                        for (Integer n : quester.getCurrentStage(quest).itemDeliveryTargets) {

                            if (n.equals(clicked.getId())) {
                                quester.deliverItem(quest, hand);
                                delivery = true;
                                break;
                            }
                        }

                        break;

                    } else if (!hand.getType().equals(Material.AIR)){

                        for (Integer n : quester.getCurrentStage(quest).itemDeliveryTargets) {

                            if (n.equals(clicked.getId())) {
                            	String text = "";

                            	if (hand.hasItemMeta()) {
                            		text += ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC + (hand.getItemMeta().hasDisplayName() ? hand.getItemMeta().getDisplayName() + ChatColor.GRAY + " (" : "");
                            	}

                            	text += ChatColor.AQUA + Quester.prettyItemString(hand.getType().name()) + (hand.getDurability() != 0 ? (":" + ChatColor.BLUE + hand.getDurability()) : "") + ChatColor.GRAY;

                            	if (hand.hasItemMeta()) {
                            		text += (hand.getItemMeta().hasDisplayName() ? ")" : "");
                            	}

                            	text += " x " + ChatColor.DARK_AQUA + hand.getAmount() + ChatColor.GRAY;

                            	evt.getClicker().sendMessage(Lang.get("questInvalidDeliveryItem").replaceAll("<item>", text));
                                break;
                            }
                        }

                        //break;

                    }

                }

            }

            if (plugin.questNPCs.contains(evt.getNPC()) && delivery == false) {

                if (plugin.checkQuester(player.getUniqueId()) == false) {

                    boolean hasObjective = false;

                    for (Quest quest : quester.currentQuests.keySet()) {

                        if (quester.hasObjective(quest, "talkToNPC")) {

                            if (quester.getQuestData(quest) != null && quester.getQuestData(quest).citizensInteracted.containsKey(evt.getNPC().getId()) && quester.getQuestData(quest).citizensInteracted.get(evt.getNPC().getId()) == false) {
                                hasObjective = true;
                            }

                            quester.interactWithNPC(quest, evt.getNPC());

                        }

                    }

                    if (!hasObjective) {

                        LinkedList<Quest> npcQuests = new LinkedList<Quest>();

                        for (Quest q : plugin.getQuests()) {
                            if(quester.currentQuests.containsKey(q))
                                continue;
                            if (q.npcStart != null && q.npcStart.getId() == evt.getNPC().getId()) {
                                if (Quests.ignoreLockedQuests && (quester.completedQuests.contains(q.name) == false || q.redoDelay > -1)) {
                                    if (q.testRequirements(quester)) {
                                        npcQuests.add(q);
                                    }
                                } else if (quester.completedQuests.contains(q.name) == false || q.redoDelay > -1) {
                                    npcQuests.add(q);
                                }
                            }

                        }

                        if (npcQuests.isEmpty() == false && npcQuests.size() >= 1) {

                            if (plugin.questNPCGUIs.contains(evt.getNPC().getId())) {
                                quester.showGUIDisplay(evt.getNPC(), npcQuests);
                                return;
                            }

                            Conversation c = plugin.NPCConversationFactory.buildConversation(player);
                            c.getContext().setSessionData("quests", npcQuests);
                            c.getContext().setSessionData("npc", evt.getNPC().getName());
                            c.begin();

                        } else if (npcQuests.size() == 1) {

                            Quest q = npcQuests.get(0);

                            if (!quester.completedQuests.contains(q.name)) {

                                if (quester.currentQuests.size() < Quests.maxQuests || Quests.maxQuests < 1) {

                                    quester.questToTake = q.name;

                                    String s = extracted(quester);

                                    for (String msg : s.split("<br>")) {
                                        player.sendMessage(msg);
                                    }

                                    plugin.conversationFactory.buildConversation(player).begin();

                                } else if (quester.currentQuests.containsKey(q) == false) {

                                    String msg = Lang.get("questMaxAllowed");
                                    msg = msg.replaceAll("<number>", String.valueOf(Quests.maxQuests));
                                    player.sendMessage(ChatColor.YELLOW + msg);

                                }

                            } else if (quester.currentQuests.size() < Quests.maxQuests || Quests.maxQuests < 1) {

                                if (quester.getDifference(q) > 0) {
                                    String early = Lang.get("questTooEarly");
                                    early = early.replaceAll("<quest>", ChatColor.AQUA + q.name + ChatColor.YELLOW);
                                    early = early.replaceAll("<time>", ChatColor.DARK_PURPLE + Quests.getTime(quester.getDifference(q)) + ChatColor.YELLOW);
                                    player.sendMessage(ChatColor.YELLOW + early);
                                } else if (q.redoDelay < 0) {
                                    String completed = Lang.get("questAlreadyCompleted");
                                    completed = completed.replaceAll("<quest>", ChatColor.AQUA + q.name + ChatColor.YELLOW);
                                    player.sendMessage(ChatColor.YELLOW + completed);
                                } else {
                                    quester.questToTake = q.name;
                                    String s = extracted(quester);

                                    for (String msg : s.split("<br>")) {
                                        player.sendMessage(msg);
                                    }

                                    plugin.conversationFactory.buildConversation(player).begin();
                                }

                            } else if (quester.currentQuests.containsKey(q) == false) {

                                String msg = Lang.get("questMaxAllowed");
                                msg = msg.replaceAll("<number>", String.valueOf(Quests.maxQuests));
                                player.sendMessage(ChatColor.YELLOW + msg);

                            }

                        } else if (npcQuests.isEmpty()) {

                        	evt.getClicker().sendMessage(ChatColor.YELLOW + Lang.get("noMoreQuest"));

                        }

                    }

                }

            }

        }

    }

    @EventHandler
    public void onNPCLeftClick(NPCLeftClickEvent evt) {

        if (plugin.questFactory.selectingNPCs.contains(evt.getClicker())) {
            evt.getClicker().sendMessage(ChatColor.GREEN + evt.getNPC().getName() + ": " + ChatColor.DARK_GREEN + Lang.get("id") + " " + evt.getNPC().getId());
        }

    }

    @EventHandler
    public void onNPCDeath(NPCDeathEvent evt) {

        if (evt.getNPC().getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {

            EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) evt.getNPC().getEntity().getLastDamageCause();
            Entity damager = damageEvent.getDamager();

            if (damager != null) {

                if (damager instanceof Projectile) {

                    if (evt.getNPC().getEntity().getLastDamageCause().getEntity() instanceof Player) {

                        Player player = (Player) evt.getNPC().getEntity().getLastDamageCause().getEntity();
                        boolean okay = true;

                        if (plugin.citizens != null) {
                            if (CitizensAPI.getNPCRegistry().isNPC(player)) {
                                okay = false;
                            }
                        }

                        if (okay) {

                            Quester quester = plugin.getQuester(player.getUniqueId());

                            for (Quest quest : quester.currentQuests.keySet()) {

                                if (quester.hasObjective(quest, "killNPC")) {
                                    quester.killNPC(quest, evt.getNPC());
                                }

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
                        Quester quester = plugin.getQuester(player.getUniqueId());
                        for (Quest quest : quester.currentQuests.keySet()) {

                            if (quester.hasObjective(quest, "killNPC")) {
                                quester.killNPC(quest, evt.getNPC());
                            }

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
