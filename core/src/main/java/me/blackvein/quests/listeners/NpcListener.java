/*
 * Copyright (c) 2014 PikaMug and contributors. All rights reserved.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package me.blackvein.quests.listeners;

import me.blackvein.quests.Quests;
import me.blackvein.quests.enums.ObjectiveType;
import me.blackvein.quests.player.IQuester;
import me.blackvein.quests.quests.IQuest;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import net.citizensnpcs.api.event.NPCDeathEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.Conversation;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

public class NpcListener implements Listener {

    private final Quests plugin;

    public NpcListener(final Quests plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOWEST)
    public void onNPCRightClick(final NPCRightClickEvent evt) {
        if (plugin.getSettings().getConsoleLogging() > 3) System.out.println("DEBUG: A");
        if (plugin.getDependencies().getCitizens() == null) {
            if (plugin.getSettings().getConsoleLogging() > 3) System.out.println("DEBUG: Citizens was null");
            return;
        }
        if (plugin.getSettings().getConsoleLogging() > 3) System.out.println("DEBUG: B");
        if (plugin.getQuestFactory().getSelectingNpcs().contains(evt.getClicker().getUniqueId())) {
            if (plugin.getSettings().getConsoleLogging() > 3) System.out.println("DEBUG: C");
            if (evt.getNPC() == null) {
                plugin.getLogger().severe("NPC was null while selecting by right-click");
                return;
            }
            evt.getClicker().acceptConversationInput(String.valueOf(evt.getNPC().getUniqueId()));
        }
        if (!evt.getClicker().isConversing()) {
            if (plugin.getSettings().getConsoleLogging() > 3) System.out.println("DEBUG: D");
            final Player player = evt.getClicker();
            final IQuester quester = plugin.getQuester(player.getUniqueId());
            for (final IQuest quest : quester.getCurrentQuestsTemp().keySet()) {
                if (plugin.getSettings().getConsoleLogging() > 3) System.out.println("DEBUG: E1 " + quest.getName());
                if (quester.getCurrentStage(quest).containsObjective(ObjectiveType.DELIVER_ITEM)) {
                    if (plugin.getSettings().getConsoleLogging() > 3) System.out.println("DEBUG: F");
                    final ItemStack hand = player.getItemInHand();
                    int currentIndex = -1;
                    final LinkedList<Integer> matches = new LinkedList<>();
                    int reasonCode = 0;
                    for (final ItemStack is : quester.getCurrentStage(quest).getItemsToDeliver()) {
                        currentIndex++;
                        reasonCode = ItemUtil.compareItems(is, hand, true);
                        if (reasonCode == 0) {
                            matches.add(currentIndex);
                        }
                    }
                    final NPC clicked = evt.getNPC();
                    if (!matches.isEmpty()) {
                        if (plugin.getSettings().getConsoleLogging() > 3) System.out.println("DEBUG: G1");
                        for (final Integer match : matches) {
                            final UUID uuid = quester.getCurrentStage(quest).getItemDeliveryTargets().get(match);
                            if (uuid.equals(clicked.getUniqueId())) {
                                if (plugin.getSettings().getConsoleLogging() > 3) System.out.println("DEBUG: H");
                                quester.deliverToNPC(quest, uuid, hand);
                                return;
                            }
                        }
                    } else if (!hand.getType().equals(Material.AIR)) {
                        if (plugin.getSettings().getConsoleLogging() > 3) System.out.println("DEBUG: G2");
                        for (final UUID uuid : quester.getCurrentStage(quest).getItemDeliveryTargets()) {
                            if (uuid.equals(clicked.getUniqueId())) {
                                if (plugin.getSettings().getConsoleLogging() > 3) System.out.println("DEBUG: H");
                                String text = "";
                                final boolean hasMeta = hand.getItemMeta() != null;
                                if (hasMeta) {
                                    text += ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC
                                            + (hand.getItemMeta().hasDisplayName() ? hand.getItemMeta().getDisplayName()
                                            + ChatColor.GRAY + " (" : "");
                                }
                                text += ChatColor.AQUA + "<item>" + (hand.getDurability() != 0 ? (":" + ChatColor.BLUE
                                        + hand.getDurability()) : "") + ChatColor.GRAY;
                                if (hasMeta) {
                                    text += (hand.getItemMeta().hasDisplayName() ? ")" : "");
                                }
                                text += " x " + ChatColor.DARK_AQUA + hand.getAmount() + ChatColor.GRAY;
                                if (plugin.getSettings().canTranslateNames() && !hasMeta
                                        && !hand.getItemMeta().hasDisplayName()) {
                                    plugin.getLocaleManager().sendMessage(player, Lang
                                            .get(player, "questInvalidDeliveryItem").replace("<item>", text), hand
                                            .getType(), hand.getDurability(), null);
                                } else {
                                    player.sendMessage(Lang.get(player, "questInvalidDeliveryItem")
                                            .replace("<item>", text).replace("<item>", ItemUtil.getName(hand)));
                                }
                                switch (reasonCode) {
                                    case 1:
                                        player.sendMessage(ChatColor.GRAY + Lang.get(player, "difference")
                                                .replace("<data>", "one item is null"));
                                        break;
                                    case 0:
                                        player.sendMessage(ChatColor.GRAY + Lang.get(player, "difference")
                                                .replace("<data>", "ERROR"));
                                        break;
                                    case -1:
                                        player.sendMessage(ChatColor.GRAY + Lang.get(player, "difference")
                                                .replace("<data>", "name"));
                                        break;
                                    case -2:
                                        player.sendMessage(ChatColor.GRAY + Lang.get(player, "difference")
                                                .replace("<data>", "amount"));
                                        break;
                                    case -3:
                                        player.sendMessage(ChatColor.GRAY + Lang.get(player, "difference")
                                                .replace("<data>", "durability"));
                                        break;
                                    case -4:
                                        player.sendMessage(ChatColor.GRAY + Lang.get(player, "difference")
                                                .replace("<data>", "display name or lore"));
                                        break;
                                    case -5:
                                        player.sendMessage(ChatColor.GRAY + Lang.get(player, "difference")
                                                .replace("<data>", "enchantments"));
                                        break;
                                    case -6:
                                        player.sendMessage(ChatColor.GRAY + Lang.get(player, "difference")
                                                .replace("<data>", "stored enchants"));
                                        break;
                                    case -7:
                                        player.sendMessage(ChatColor.GRAY + Lang.get(player, "difference")
                                                .replace("<data>", "item flags"));
                                        break;
                                    case -8:
                                        player.sendMessage(ChatColor.GRAY + Lang.get(player, "difference")
                                                .replace("<data>", "book data"));
                                        break;
                                    case -9:
                                        player.sendMessage(ChatColor.GRAY + Lang.get(player, "difference")
                                                .replace("<data>", "potion type"));
                                        break;
                                    case -10:
                                        player.sendMessage(ChatColor.GRAY + Lang.get(player, "difference")
                                                .replace("<data>", "fish variant"));
                                        break;
                                    default:
                                        player.sendMessage(ChatColor.GRAY + Lang.get(player, "difference")
                                                .replace("<data>", "unknown"));
                                }
                                if (hasMeta) {
                                    if (plugin.getSettings().getConsoleLogging() > 3) System.out.println("DEBUG: hasMeta");
                                    if (hand.getType().equals(Material.ENCHANTED_BOOK)) {
                                        final EnchantmentStorageMeta esMeta = (EnchantmentStorageMeta) hand.getItemMeta();
                                        if (esMeta.hasStoredEnchants()) {
                                            for (final Entry<Enchantment, Integer> e : esMeta.getStoredEnchants()
                                                    .entrySet()) {
                                                if (plugin.getSettings().getConsoleLogging() > 3) System.out.println("DEBUG: send");
                                                final HashMap<Enchantment, Integer> single = new HashMap<>();
                                                single.put(e.getKey(), e.getValue());
                                                plugin.getLocaleManager().sendMessage(player, ChatColor.GRAY + "\u2515 "
                                                        + ChatColor.DARK_GREEN + "<enchantment> <level>\n", single);
                                            }
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }
            boolean hasObjective = false;
            for (final IQuest quest : quester.getCurrentQuestsTemp().keySet()) {
                if (plugin.getSettings().getConsoleLogging() > 3) System.out.println("DEBUG: E2");
                if (quester.getCurrentStage(quest).containsObjective(ObjectiveType.TALK_TO_NPC)) {
                    if (plugin.getSettings().getConsoleLogging() > 3) System.out.println("DEBUG: F");
                    if (quester.getCurrentStage(quest).getNpcsToInteract().contains(evt.getNPC().getUniqueId())) {
                        if (plugin.getSettings().getConsoleLogging() > 3) System.out.println("DEBUG: G");
                        final int npcIndex = quester.getCurrentStage(quest).getNpcsToInteract().indexOf(evt.getNPC()
                                .getUniqueId());
                        if (quester.getQuestData(quest) != null && npcIndex > -1
                                && !quester.getQuestData(quest).npcsInteracted.get(npcIndex)) {
                            hasObjective = true;
                        }
                        quester.interactWithNPC(quest, evt.getNPC().getUniqueId());
                    }
                }
            }
            if (!hasObjective) {
                if (plugin.getSettings().getConsoleLogging() > 3) System.out.println("DEBUG: E3");
                boolean hasAtLeastOneGUI = false;
                final LinkedList<IQuest> npcQuests = new LinkedList<>();
                for (final IQuest q : plugin.getLoadedQuests()) {
                    if (plugin.getSettings().getConsoleLogging() > 3) System.out.println("DEBUG: F");
                    if (quester.getCurrentQuestsTemp().containsKey(q))
                        continue;
                    if (q.getNpcStart() != null && q.getNpcStart().getId() == evt.getNPC().getId()) {
                        if (plugin.getSettings().getConsoleLogging() > 3) System.out.println("DEBUG: G");
                        if (plugin.getSettings().canIgnoreLockedQuests()
                                && (!quester.getCompletedQuestsTemp().contains(q)
                                || q.getPlanner().getCooldown() > -1)) {
                            if (plugin.getSettings().getConsoleLogging() > 3) System.out.println("DEBUG: H1");
                            if (q.testRequirements(quester)) {
                                npcQuests.add(q);
                                if (q.getGUIDisplay() != null) {
                                    if (plugin.getSettings().getConsoleLogging() > 3) System.out.println("DEBUG: I1");
                                    hasAtLeastOneGUI = true;
                                }
                            }
                        } else if (!quester.getCompletedQuestsTemp().contains(q) || q.getPlanner().getCooldown() > -1) {
                            if (plugin.getSettings().getConsoleLogging() > 3) System.out.println("DEBUG: H2");
                            npcQuests.add(q);
                            if (q.getGUIDisplay() != null) {
                                if (plugin.getSettings().getConsoleLogging() > 3) System.out.println("DEBUG: I2");
                                hasAtLeastOneGUI = true;
                            }
                        }
                    }
                }
                if (npcQuests.size() == 1) {
                    if (plugin.getSettings().getConsoleLogging() > 3) System.out.println("DEBUG: J1");
                    final IQuest q = npcQuests.get(0);
                    if (quester.canAcceptOffer(q, true)) {
                        if (plugin.getSettings().getConsoleLogging() > 3) System.out.println("DEBUG: K1");
                        quester.setQuestIdToTake(q.getId());
                        if (!plugin.getSettings().canAskConfirmation()) {
                            if (plugin.getSettings().getConsoleLogging() > 3) System.out.println("DEBUG: 1");
                            quester.takeQuest(q, false);
                        } else {
                            if (q.getGUIDisplay() != null) {
                                if (plugin.getSettings().getConsoleLogging() > 3) System.out.println("DEBUG: 2");
                                quester.showGUIDisplay(evt.getNPC().getUniqueId(), npcQuests);
                            } else {
                                if (plugin.getSettings().getConsoleLogging() > 3) System.out.println("DEBUG: 3");
                                for (final String msg : extracted(quester).split("<br>")) {
                                    player.sendMessage(msg);
                                }
                                plugin.getConversationFactory().buildConversation(player).begin();
                            }
                        }
                    }
                } else if (npcQuests.size() > 1) {
                    if (plugin.getSettings().getConsoleLogging() > 3) System.out.println("DEBUG: J2");
                    if (hasAtLeastOneGUI) {
                        if (plugin.getSettings().getConsoleLogging() > 3) System.out.println("DEBUG: 1");
                        quester.showGUIDisplay(evt.getNPC().getUniqueId(), npcQuests);
                    } else {
                        if (plugin.getSettings().getConsoleLogging() > 3) System.out.println("DEBUG: 2");
                        final Conversation c = plugin.getNpcConversationFactory().buildConversation(player);
                        c.getContext().setSessionData("npcQuests", npcQuests);
                        c.getContext().setSessionData("npc", evt.getNPC().getName());
                        c.begin();
                    }
                } else {
                    if (plugin.getSettings().getConsoleLogging() > 3) System.out.println("DEBUG: NO MORE QUEST");
                    evt.getClicker().sendMessage(ChatColor.YELLOW + Lang.get(player, "noMoreQuest"));
                }
            }
        }
    }

    @EventHandler
    public void onNPCLeftClick(final NPCLeftClickEvent evt) {
        if (plugin.getDependencies().getCitizens() == null) {
            return;
        }
        if (plugin.getQuestFactory().getSelectingNpcs().contains(evt.getClicker().getUniqueId())) {
            if (evt.getNPC() == null) {
                plugin.getLogger().severe("NPC was null while selecting by left-click");
                return;
            }
            evt.getClicker().acceptConversationInput(String.valueOf(evt.getNPC().getUniqueId()));
        }
    }

    @EventHandler
    public void onNPCDeath(final NPCDeathEvent evt) {
        if (plugin.getDependencies().getCitizens() == null) {
            return;
        }
        if (evt.getNPC() == null || evt.getNPC().getEntity() == null 
                || evt.getNPC().getEntity().getLastDamageCause() == null) {
            return;
        }
        if (evt.getNPC().getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            final EntityDamageByEntityEvent damageEvent 
                    = (EntityDamageByEntityEvent) evt.getNPC().getEntity().getLastDamageCause();
            final Entity damager = damageEvent.getDamager();
            if (plugin.getDependencies().getCitizens().getNPCRegistry().isNPC(damager)) {
                return;
            }
            final ObjectiveType type = ObjectiveType.KILL_NPC;
            final Set<String> dispatchedQuestIDs = new HashSet<>();
            Player player = null;
            if (damager instanceof Projectile && ((Projectile)damageEvent.getDamager()).getShooter() instanceof Player) {
                player = (Player) ((Projectile)damageEvent.getDamager()).getShooter();
            } else if (damager instanceof Player) {
                player = (Player) damager;
            }
            if (player != null) {
                final IQuester quester = plugin.getQuester(player.getUniqueId());
                for (final IQuest quest : quester.getCurrentQuestsTemp().keySet()) {
                    if (!quester.meetsCondition(quest, true)) {
                        continue;
                    }

                    if (quester.getCurrentQuestsTemp().containsKey(quest)
                            && quester.getCurrentStage(quest).containsObjective(type)) {
                        quester.killNPC(quest, evt.getNPC().getUniqueId());
                    }

                    dispatchedQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, type,
                            (final IQuester q, final IQuest cq) -> {
                                if (!dispatchedQuestIDs.contains(cq.getId())) {
                                    q.killNPC(cq, evt.getNPC().getUniqueId());
                                }
                                return null;
                            }));
                }
            }
        }
    }

    private String extracted(final IQuester quester) {
        final IQuest quest = plugin.getQuestByIdTemp(quester.getQuestIdToTake());
        return MessageFormat.format("{0}- {1}{2}{3} -\n\n{4}{5}\n", ChatColor.GOLD, ChatColor.DARK_PURPLE, 
                quest.getName(), ChatColor.GOLD, ChatColor.RESET, quest.getDescription());
    }
}
