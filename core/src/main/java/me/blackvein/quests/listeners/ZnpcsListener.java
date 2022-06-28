package me.blackvein.quests.listeners;

import io.github.znetworkw.znpcservers.npc.NPC;
import io.github.znetworkw.znpcservers.npc.event.NPCInteractEvent;
import me.blackvein.quests.Quests;
import me.blackvein.quests.enums.ObjectiveType;
import me.blackvein.quests.player.IQuester;
import me.blackvein.quests.quests.IQuest;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.Conversation;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

public class ZnpcsListener implements Listener {

    private final Quests plugin;

    public ZnpcsListener(final Quests plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onNPCInteract(final NPCInteractEvent evt) {
        if (evt.isLeftClick()) {
            if (plugin.getDependencies().getZnpcs() == null) {
                return;
            }
            if (plugin.getQuestFactory().getSelectingNpcs().contains(evt.getPlayer().getUniqueId())) {
                if (evt.getNpc() == null) {
                    plugin.getLogger().severe("ZNPC was null while selecting by left-click");
                    return;
                }
                evt.getPlayer().acceptConversationInput(String.valueOf(evt.getNpc().getUUID()));
                evt.getPlayer().sendMessage(ChatColor.RED + "Warning: " + ChatColor.RESET
                        + "ZNPCs is not fully supported and will break after server restart. Please ask its developer "
                        + "to fix this at https://github.com/gonalez/znpc-servers/issues/36");
            }
        } else if (evt.isRightClick()) {
            if (plugin.getDependencies().getCitizens() == null) {
                return;
            }
            if (plugin.getQuestFactory().getSelectingNpcs().contains(evt.getPlayer().getUniqueId())) {
                if (evt.getNpc() == null) {
                    plugin.getLogger().severe("ZNPC was null while selecting by right-click");
                    return;
                }
                evt.getPlayer().acceptConversationInput(String.valueOf(evt.getNpc().getUUID()));
                evt.getPlayer().sendMessage(ChatColor.RED + "Warning: " + ChatColor.RESET
                        + "ZNPCs is not fully supported and will break after server restart. Please ask its developer "
                        + "to fix this at https://github.com/gonalez/znpc-servers/issues/36");
            }
            if (!evt.getPlayer().isConversing()) {
                final Player player = evt.getPlayer();
                final IQuester quester = plugin.getQuester(player.getUniqueId());
                for (final IQuest quest : quester.getCurrentQuestsTemp().keySet()) {
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
                        final NPC clicked = evt.getNpc();
                        if (!matches.isEmpty()) {
                            for (final Integer match : matches) {
                                final UUID uuid = quester.getCurrentStage(quest).getItemDeliveryTargets().get(match);
                                if (uuid.equals(clicked.getUUID())) {
                                    quester.deliverToNPC(quest, uuid, hand);
                                    return;
                                }
                            }
                        } else if (!hand.getType().equals(Material.AIR)) {
                            for (final UUID uuid : quester.getCurrentStage(quest).getItemDeliveryTargets()) {
                                if (uuid.equals(clicked.getUUID())) {
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
                                        if (hand.getType().equals(Material.ENCHANTED_BOOK)) {
                                            final EnchantmentStorageMeta esMeta = (EnchantmentStorageMeta) hand.getItemMeta();
                                            if (esMeta.hasStoredEnchants()) {
                                                for (final Map.Entry<Enchantment, Integer> e : esMeta.getStoredEnchants()
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
                for (final IQuest quest : quester.getCurrentQuestsTemp().keySet()) {
                    if (!quester.meetsCondition(quest, true)) {
                        continue;
                    }
                    if (quester.getCurrentStage(quest).containsObjective(ObjectiveType.TALK_TO_NPC)) {
                        if (quester.getCurrentStage(quest).getNpcsToInteract().contains(evt.getNpc().getUUID())) {
                            final int npcIndex = quester.getCurrentStage(quest).getNpcsToInteract().indexOf(evt.getNpc()
                                    .getUUID());
                            if (quester.getQuestData(quest) != null && npcIndex > -1
                                    && !quester.getQuestData(quest).npcsInteracted.get(npcIndex)) {
                                hasObjective = true;
                            }
                            quester.interactWithNPC(quest, evt.getNpc().getUUID());
                        }
                    }
                }
                if (hasObjective || !plugin.getQuestNpcUuids().contains(evt.getNpc().getUUID())) {
                    return;
                }
                boolean hasAtLeastOneGUI = false;
                final LinkedList<IQuest> npcQuests = new LinkedList<>();
                for (final IQuest q : plugin.getLoadedQuests()) {
                    if (quester.getCurrentQuestsTemp().containsKey(q)) {
                        continue;
                    }
                    if (q.getNpcStart() != null && q.getNpcStart().equals(evt.getNpc().getUUID())) {
                        if (plugin.getSettings().canIgnoreLockedQuests()
                                && (!quester.getCompletedQuestsTemp().contains(q)
                                || q.getPlanner().getCooldown() > -1)) {
                            if (q.testRequirements(quester)) {
                                npcQuests.add(q);
                                if (q.getGUIDisplay() != null) {
                                    hasAtLeastOneGUI = true;
                                }
                            }
                        } else if (!quester.getCompletedQuestsTemp().contains(q) || q.getPlanner().getCooldown() > -1) {
                            npcQuests.add(q);
                            if (q.getGUIDisplay() != null) {
                                hasAtLeastOneGUI = true;
                            }
                        }
                    }
                }
                if (npcQuests.size() == 1) {
                    final IQuest q = npcQuests.get(0);
                    if (quester.canAcceptOffer(q, true)) {
                        quester.setQuestIdToTake(q.getId());
                        if (!plugin.getSettings().canAskConfirmation()) {
                            quester.takeQuest(q, false);
                        } else {
                            if (q.getGUIDisplay() != null) {
                                quester.showGUIDisplay(evt.getNpc().getUUID(), npcQuests);
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
                        quester.showGUIDisplay(evt.getNpc().getUUID(), npcQuests);
                    } else {
                        final Conversation c = plugin.getNpcConversationFactory().buildConversation(player);
                        c.getContext().setSessionData("npcQuests", npcQuests);
                        c.getContext().setSessionData("npc", evt.getNpc().getGameProfile().getName());
                        c.begin();
                    }
                } else {
                    Lang.send(player, ChatColor.YELLOW + Lang.get(player, "noMoreQuest"));
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
