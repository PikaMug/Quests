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

import me.blackvein.quests.Quest;
import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;
import me.blackvein.quests.enums.ObjectiveType;
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

public class NpcListener implements Listener {

    private final Quests plugin;

    public NpcListener(final Quests plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOWEST)
    public void onNPCRightClick(final NPCRightClickEvent evt) {
        if (plugin.getDependencies().getCitizens() == null) {
            return;
        }
        if (plugin.getQuestFactory().getSelectingNpcs().contains(evt.getClicker().getUniqueId())) {
            evt.getClicker().sendMessage(ChatColor.GREEN + evt.getNPC().getName() + " " + ChatColor.DARK_GREEN 
                    + Lang.get("id") + ": "  + evt.getNPC().getId());
            return;
        }
        if (!evt.getClicker().isConversing()) {
            final Player player = evt.getClicker();
            final Quester quester = plugin.getQuester(player.getUniqueId());
            for (final Quest quest : quester.getCurrentQuests().keySet()) {
                if (quester.getCurrentStage(quest).containsObjective(ObjectiveType.DELIVER_ITEM)) {
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
                        for (final Integer match : matches) {
                            final Integer id = quester.getCurrentStage(quest).getItemDeliveryTargets().get(match);
                            if (id.equals(clicked.getId())) {
                                quester.deliverToNPC(quest, clicked, hand);
                                return;
                            }
                        }
                    } else if (!hand.getType().equals(Material.AIR)) {
                        for (final Integer n : quester.getCurrentStage(quest).getItemDeliveryTargets()) {
                            if (n.equals(clicked.getId())) {
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
                                    default:
                                        player.sendMessage(ChatColor.GRAY + Lang.get(player, "difference")
                                                .replace("<data>", "unknown"));
                                }
                                if (hasMeta) {
                                    if (hand.getType().equals(Material.ENCHANTED_BOOK)) {
                                        final EnchantmentStorageMeta esMeta = (EnchantmentStorageMeta) hand.getItemMeta();
                                        if (esMeta.hasStoredEnchants()) {
                                            for (final Entry<Enchantment, Integer> e : esMeta.getStoredEnchants()
                                                    .entrySet()) {
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
            if (plugin.getQuestNpcIds().contains(evt.getNPC().getId())) {
                boolean hasObjective = false;
                for (final Quest quest : quester.getCurrentQuests().keySet()) {
                    if (quester.getCurrentStage(quest).containsObjective(ObjectiveType.TALK_TO_NPC)) {
                        final int npcIndex
                                = quester.getCurrentStage(quest).getCitizensToInteract().indexOf(evt.getNPC().getId());
                        if (quester.getQuestData(quest) != null && npcIndex > -1
                                && !quester.getQuestData(quest).citizensInteracted.get(npcIndex)) {
                            hasObjective = true;
                        }
                        quester.interactWithNPC(quest, evt.getNPC());
                    }
                }
                if (!hasObjective) {
                    boolean hasAtLeastOneGUI = false;
                    final LinkedList<Quest> npcQuests = new LinkedList<>();
                    for (final Quest q : plugin.getLoadedQuests()) {
                        if (quester.getCurrentQuests().containsKey(q))
                            continue;
                        if (q.getNpcStart() != null && q.getNpcStart().getId() == evt.getNPC().getId()) {
                            if (plugin.getSettings().canIgnoreLockedQuests()
                                    && (!quester.getCompletedQuests().contains(q)
                                    || q.getPlanner().getCooldown() > -1)) {
                                if (q.testRequirements(quester)) {
                                    npcQuests.add(q);
                                    if (q.getGUIDisplay() != null) {
                                        hasAtLeastOneGUI = true;
                                    }
                                }
                            } else if (!quester.getCompletedQuests().contains(q) || q.getPlanner().getCooldown() > -1) {
                                npcQuests.add(q);
                                if (q.getGUIDisplay() != null) {
                                    hasAtLeastOneGUI = true;
                                }
                            }
                        }
                    }
                    if (npcQuests.size() == 1) {
                        final Quest q = npcQuests.get(0);
                        if (quester.canAcceptOffer(q, true)) {
                            quester.setQuestIdToTake(q.getId());
                            if (!plugin.getSettings().canAskConfirmation()) {
                                quester.takeQuest(q, false);
                            } else {
                                if (q.getGUIDisplay() != null) {
                                    quester.showGUIDisplay(evt.getNPC(), npcQuests);
                                } else {
                                    for (final String msg : extracted(quester).split("<br>")) {
                                        player.sendMessage(msg);
                                    }
                                    plugin.getConversationFactory().buildConversation(player).begin();
                                }
                            }
                        }
                    } else if (npcQuests.size() > 1) {
                        if (hasAtLeastOneGUI) {
                            quester.showGUIDisplay(evt.getNPC(), npcQuests);
                        } else {
                            final Conversation c = plugin.getNpcConversationFactory().buildConversation(player);
                            c.getContext().setSessionData("npcQuests", npcQuests);
                            c.getContext().setSessionData("npc", evt.getNPC().getName());
                            c.begin();
                        }
                    } else {
                        evt.getClicker().sendMessage(ChatColor.YELLOW + Lang.get(player, "noMoreQuest"));
                    }
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
            evt.getClicker().sendMessage(ChatColor.GREEN + evt.getNPC().getName() + " " + ChatColor.DARK_GREEN 
                    + Lang.get("id") + ": " + evt.getNPC().getId());
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
            if (damager instanceof Projectile
                    && evt.getNPC().getEntity().getLastDamageCause().getEntity() instanceof Player) {
                player = (Player) evt.getNPC().getEntity().getLastDamageCause().getEntity();
            } else if (damager instanceof Player) {
                player = (Player) damager;
            }
            if (player != null) {
                final Quester quester = plugin.getQuester(player.getUniqueId());
                for (final Quest quest : quester.getCurrentQuests().keySet()) {
                    if (!quester.meetsCondition(quest, true)) {
                        continue;
                    }

                    if (quester.getCurrentQuests().containsKey(quest)
                            && quester.getCurrentStage(quest).containsObjective(type)) {
                        quester.killNPC(quest, evt.getNPC());
                    }

                    dispatchedQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, type,
                            (final Quester q, final Quest cq) -> {
                                if (!dispatchedQuestIDs.contains(cq.getId())) {
                                    q.killNPC(cq, evt.getNPC());
                                }
                                return null;
                            }));
                }
            }
        }
    }

    private String extracted(final Quester quester) {
        final Quest quest = plugin.getQuestById(quester.getQuestIdToTake());
        return MessageFormat.format("{0}- {1}{2}{3} -\n\n{4}{5}\n", ChatColor.GOLD, ChatColor.DARK_PURPLE, 
                quest.getName(), ChatColor.GOLD, ChatColor.RESET, quest.getDescription());
    }
}
