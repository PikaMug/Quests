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

package me.pikamug.quests.listeners;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.enums.ObjectiveType;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.BukkitQuest;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.util.BukkitItemUtil;
import me.pikamug.quests.util.Language;
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

public class BukkitCitizensListener implements Listener {

    private final BukkitQuestsPlugin plugin;

    public BukkitCitizensListener(final BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOWEST)
    public void onNPCRightClick(final NPCRightClickEvent event) {
        if (plugin.getDependencies().getCitizens() == null) {
            return;
        }
        if (plugin.getQuestFactory().getSelectingNpcs().contains(event.getClicker().getUniqueId())) {
            if (event.getNPC() == null) {
                plugin.getLogger().severe("NPC was null while selecting by right-click");
                return;
            }
            event.getClicker().acceptConversationInput(String.valueOf(event.getNPC().getUniqueId()));
        }
        if (!event.getClicker().isConversing()) {
            final Player player = event.getClicker();
            final Quester quester = plugin.getQuester(player.getUniqueId());
            for (final Quest quest : quester.getCurrentQuestsTemp().keySet()) {
                if (quester.getCurrentStage(quest).containsObjective(ObjectiveType.DELIVER_ITEM)) {
                    final ItemStack hand = player.getItemInHand();
                    int currentIndex = -1;
                    final LinkedList<Integer> matches = new LinkedList<>();
                    int reasonCode = 0;
                    for (final ItemStack is : quester.getCurrentStage(quest).getItemsToDeliver()) {
                        currentIndex++;
                        reasonCode = BukkitItemUtil.compareItems(is, hand, true);
                        if (reasonCode == 0) {
                            matches.add(currentIndex);
                        }
                    }
                    final NPC clicked = event.getNPC();
                    if (!matches.isEmpty()) {
                        for (final Integer match : matches) {
                            final UUID uuid = quester.getCurrentStage(quest).getItemDeliveryTargets().get(match);
                            if (uuid.equals(clicked.getUniqueId())) {
                                quester.deliverToNPC(quest, uuid, hand);
                                return;
                            }
                        }
                    } else if (!hand.getType().equals(Material.AIR)) {
                        for (final UUID uuid : quester.getCurrentStage(quest).getItemDeliveryTargets()) {
                            if (uuid.equals(clicked.getUniqueId())) {
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
                                    plugin.getLocaleManager().sendMessage(player, Language
                                            .get(player, "questInvalidDeliveryItem").replace("<item>", text), hand
                                            .getType(), hand.getDurability(), null);
                                } else {
                                    player.sendMessage(Language.get(player, "questInvalidDeliveryItem")
                                            .replace("<item>", text).replace("<item>", BukkitItemUtil.getName(hand)));
                                }
                                switch (reasonCode) {
                                    case 1:
                                        player.sendMessage(ChatColor.GRAY + Language.get(player, "difference")
                                                .replace("<data>", "one item is null"));
                                        break;
                                    case 0:
                                        player.sendMessage(ChatColor.GRAY + Language.get(player, "difference")
                                                .replace("<data>", "ERROR"));
                                        break;
                                    case -1:
                                        player.sendMessage(ChatColor.GRAY + Language.get(player, "difference")
                                                .replace("<data>", "name"));
                                        break;
                                    case -2:
                                        player.sendMessage(ChatColor.GRAY + Language.get(player, "difference")
                                                .replace("<data>", "amount"));
                                        break;
                                    case -3:
                                        player.sendMessage(ChatColor.GRAY + Language.get(player, "difference")
                                                .replace("<data>", "durability"));
                                        break;
                                    case -4:
                                        player.sendMessage(ChatColor.GRAY + Language.get(player, "difference")
                                                .replace("<data>", "display name or lore"));
                                        break;
                                    case -5:
                                        player.sendMessage(ChatColor.GRAY + Language.get(player, "difference")
                                                .replace("<data>", "enchantments"));
                                        break;
                                    case -6:
                                        player.sendMessage(ChatColor.GRAY + Language.get(player, "difference")
                                                .replace("<data>", "stored enchants"));
                                        break;
                                    case -7:
                                        player.sendMessage(ChatColor.GRAY + Language.get(player, "difference")
                                                .replace("<data>", "item flags"));
                                        break;
                                    case -8:
                                        player.sendMessage(ChatColor.GRAY + Language.get(player, "difference")
                                                .replace("<data>", "book data"));
                                        break;
                                    case -9:
                                        player.sendMessage(ChatColor.GRAY + Language.get(player, "difference")
                                                .replace("<data>", "potion type"));
                                        break;
                                    case -10:
                                        player.sendMessage(ChatColor.GRAY + Language.get(player, "difference")
                                                .replace("<data>", "fish variant"));
                                        break;
                                    default:
                                        player.sendMessage(ChatColor.GRAY + Language.get(player, "difference")
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
            boolean hasObjective = false;
            for (final Quest quest : quester.getCurrentQuestsTemp().keySet()) {
                if (!quester.meetsCondition(quest, true)) {
                    continue;
                }
                if (quester.getCurrentStage(quest).containsObjective(ObjectiveType.TALK_TO_NPC)) {
                    if (quester.getCurrentStage(quest).getNpcsToInteract().contains(event.getNPC().getUniqueId())) {
                        final int npcIndex = quester.getCurrentStage(quest).getNpcsToInteract().indexOf(event.getNPC()
                                .getUniqueId());
                        if (quester.getQuestData(quest) != null && npcIndex > -1
                                && !quester.getQuestData(quest).npcsInteracted.get(npcIndex)) {
                            hasObjective = true;
                        }
                        quester.interactWithNPC(quest, event.getNPC().getUniqueId());
                    }
                }
            }
            if (hasObjective || !plugin.getQuestNpcUuids().contains(event.getNPC().getUniqueId())) {
                return;
            }
            boolean hasAtLeastOneGUI = false;
            final LinkedList<Quest> npcQuests = new LinkedList<>();
            for (final Quest quest : plugin.getLoadedQuests()) {
                final BukkitQuest bukkitQuest = (BukkitQuest) quest;
                if (quester.getCurrentQuestsTemp().containsKey(bukkitQuest)) {
                    continue;
                }
                if (bukkitQuest.getNpcStart() != null && bukkitQuest.getNpcStart().equals(event.getNPC().getUniqueId())) {
                    if (plugin.getSettings().canIgnoreLockedQuests()
                            && (!quester.getCompletedQuestsTemp().contains(bukkitQuest)
                            || bukkitQuest.getPlanner().getCooldown() > -1)) {
                        if (bukkitQuest.testRequirements(quester)) {
                            npcQuests.add(bukkitQuest);
                            if (bukkitQuest.getGUIDisplay() != null) {
                                hasAtLeastOneGUI = true;
                            }
                        }
                    } else if (!quester.getCompletedQuestsTemp().contains(bukkitQuest) || bukkitQuest.getPlanner().getCooldown() > -1) {
                        npcQuests.add(bukkitQuest);
                        if (bukkitQuest.getGUIDisplay() != null) {
                            hasAtLeastOneGUI = true;
                        }
                    }
                }
            }
            if (npcQuests.size() == 1) {
                final BukkitQuest bukkitQuest = (BukkitQuest) npcQuests.get(0);
                if (quester.canAcceptOffer(bukkitQuest, true)) {
                    quester.setQuestIdToTake(bukkitQuest.getId());
                    if (!plugin.getSettings().canConfirmAccept()) {
                        quester.takeQuest(bukkitQuest, false);
                    } else {
                        if (bukkitQuest.getGUIDisplay() != null) {
                            quester.showGUIDisplay(event.getNPC().getUniqueId(), npcQuests);
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
                    quester.showGUIDisplay(event.getNPC().getUniqueId(), npcQuests);
                } else {
                    final Conversation c = plugin.getNpcConversationFactory().buildConversation(player);
                    c.getContext().setSessionData("npcQuests", npcQuests);
                    c.getContext().setSessionData("npc", event.getNPC().getName());
                    c.begin();
                }
            } else {
                Language.send(player, ChatColor.YELLOW + Language.get(player, "noMoreQuest"));
            }
        }
    }

    @EventHandler
    public void onNPCLeftClick(final NPCLeftClickEvent event) {
        if (plugin.getDependencies().getCitizens() == null) {
            return;
        }
        if (plugin.getQuestFactory().getSelectingNpcs().contains(event.getClicker().getUniqueId())) {
            if (event.getNPC() == null) {
                plugin.getLogger().severe("NPC was null while selecting by left-click");
                return;
            }
            event.getClicker().acceptConversationInput(String.valueOf(event.getNPC().getUniqueId()));
        }
    }

    @EventHandler
    public void onNPCDeath(final NPCDeathEvent event) {
        if (plugin.getDependencies().getCitizens() == null) {
            return;
        }
        if (event.getNPC() == null || event.getNPC().getEntity() == null 
                || event.getNPC().getEntity().getLastDamageCause() == null) {
            return;
        }
        if (event.getNPC().getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            final EntityDamageByEntityEvent damageEvent 
                    = (EntityDamageByEntityEvent) event.getNPC().getEntity().getLastDamageCause();
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
                final Quester quester = plugin.getQuester(player.getUniqueId());
                for (final Quest quest : quester.getCurrentQuestsTemp().keySet()) {
                    if (!quester.meetsCondition(quest, true)) {
                        continue;
                    }

                    if (quester.getCurrentQuestsTemp().containsKey(quest)
                            && quester.getCurrentStage(quest).containsObjective(type)) {
                        quester.killNPC(quest, event.getNPC().getUniqueId());
                    }

                    dispatchedQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, type,
                            (final Quester q, final Quest cq) -> {
                                if (!dispatchedQuestIDs.contains(cq.getId())) {
                                    q.killNPC(cq, event.getNPC().getUniqueId());
                                }
                                return null;
                            }));
                }
            }
        }
    }

    private String extracted(final Quester quester) {
        final Quest quest = plugin.getQuestByIdTemp(quester.getQuestIdToTake());
        return MessageFormat.format("{0}- {1}{2}{3} -\n\n{4}{5}\n", ChatColor.GOLD, ChatColor.DARK_PURPLE, 
                quest.getName(), ChatColor.GOLD, ChatColor.RESET, quest.getDescription());
    }
}
