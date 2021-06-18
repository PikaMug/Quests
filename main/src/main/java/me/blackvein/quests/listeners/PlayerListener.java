/*******************************************************************************************************
 * Copyright (c) 2014 PikaMug and contributors. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

package me.blackvein.quests.listeners;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.blackvein.quests.Quest;
import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;
import me.blackvein.quests.Stage;
import me.blackvein.quests.enums.ObjectiveType;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;
import net.citizensnpcs.api.CitizensAPI;

public class PlayerListener implements Listener {

    private final Quests plugin;

    public PlayerListener(final Quests plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClickEvent(final InventoryClickEvent evt) {
        final InventoryAction ac = evt.getAction();
        if (ac.equals(InventoryAction.NOTHING)) {
            return;
        }
        if (ItemUtil.isItem(evt.getCurrentItem()) && ItemUtil.isJournal(evt.getCurrentItem())) {
            if (ac.equals(InventoryAction.MOVE_TO_OTHER_INVENTORY) || ac.equals(InventoryAction.DROP_ALL_SLOT)
                    || ac.equals(InventoryAction.DROP_ONE_SLOT)) {
                evt.setCancelled(true);
                return;
            }
        } else if (ItemUtil.isItem(evt.getCurrentItem()) && ItemUtil.isJournal(evt.getCursor())) {
            if (ac.equals(InventoryAction.MOVE_TO_OTHER_INVENTORY) || ac.equals(InventoryAction.DROP_ALL_CURSOR) 
                    || ac.equals(InventoryAction.DROP_ONE_CURSOR)) {
                evt.setCancelled(true);
                return;
            }
        } else if (ac.equals(InventoryAction.SWAP_WITH_CURSOR) || ac.equals(InventoryAction.HOTBAR_SWAP)
                || ac.equals(InventoryAction.HOTBAR_MOVE_AND_READD)) {
            if (evt.getHotbarButton() > -1) {
                final ItemStack item = evt.getWhoClicked().getInventory().getItem(evt.getHotbarButton());
                if (ItemUtil.isItem(item) && ItemUtil.isJournal(item)) {
                    evt.setCancelled(true);
                    return;
                }
            }
        }
        if (ItemUtil.isItem(evt.getCurrentItem()) && ItemUtil.isJournal(evt.getCurrentItem()) 
                || ItemUtil.isItem(evt.getCursor()) && ItemUtil.isJournal(evt.getCursor())) {
            int upper = evt.getView().getTopInventory().getSize();
            if (evt.getView().getTopInventory().getType().equals(InventoryType.CRAFTING))
                upper += 4;
            final int lower = evt.getView().getBottomInventory().getSize();
            final int relative = evt.getRawSlot() - upper;
            if (relative < 0 || relative >= (lower)) {
                evt.setCancelled(true);
                return;
            }
        }
        final Quester quester = plugin.getQuester(evt.getWhoClicked().getUniqueId());
        final Player player = (Player) evt.getWhoClicked();
        if (evt.getView().getTitle().contains(Lang.get(player, "quests"))) {
            final ItemStack clicked = evt.getCurrentItem();
            if (ItemUtil.isItem(clicked)) {
                for (final Quest quest : plugin.getLoadedQuests()) {
                    if (quest.getGUIDisplay() != null) {
                        if (ItemUtil.compareItems(clicked, quest.getGUIDisplay(), false) == 0) {
                            if (quester.canAcceptOffer(quest, true)) {
                                try { 
                                    quester.takeQuest(quest, false);
                                } catch (final Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            evt.getWhoClicked().closeInventory();
                        }
                    }
                }
                evt.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDragEvent(final InventoryDragEvent evt) {
        if (ItemUtil.isItem(evt.getOldCursor()) && ItemUtil.isJournal(evt.getOldCursor()) 
                || ItemUtil.isItem(evt.getCursor()) && ItemUtil.isJournal(evt.getCursor())) {
            int upper = evt.getView().getTopInventory().getSize();
            if (evt.getView().getTopInventory().getType().equals(InventoryType.CRAFTING))
                upper += 4;
            final int lower = evt.getView().getBottomInventory().getSize();
            for (int relative : evt.getRawSlots()) {
                relative -= upper;
                if (relative < 0 || relative >= (lower)) {
                    evt.setCancelled(true);
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(final PlayerDropItemEvent evt) {
        if (ItemUtil.isJournal(evt.getItemDrop().getItemStack())) {
            if (!evt.getPlayer().hasPermission("quests.admin.drop")) {
                evt.setCancelled(true);
            }
        }
    }

    @SuppressWarnings("deprecation") // since 1.13
    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent evt) {
        EquipmentSlot e = null;
        try {
            e = evt.getHand();
        } catch (final NoSuchMethodError err) {
            // Do nothing, getHand() not present pre-1.9
        }
        if (e == null || e.equals(EquipmentSlot.HAND)) { // If the event is fired by HAND (main hand)
            if (ItemUtil.isJournal(evt.getPlayer().getItemInHand())) {
                if (evt.hasBlock()) {
                    if (evt.getClickedBlock().getType().name().equals("LECTERN")) {
                        evt.setCancelled(true);
                        evt.getPlayer().sendMessage(ChatColor.RED + Lang.get(evt.getPlayer(), "journalDenied")
                                .replace("<journal>", Lang.get(evt.getPlayer(), "journalTitle")));
                        return;
                    }
                    if (plugin.getSettings().canAllowPranks()
                            && evt.getClickedBlock().getType().name().contains("PORTAL")) {
                        evt.setCancelled(true);
                        evt.getPlayer().sendMessage(" " + ChatColor.AQUA + ChatColor.UNDERLINE 
                                + "https://www.youtube.com/watch?v=dQw4w9WgXcQ");
                        return;
                    }
                }
                if (plugin.getSettings().canAllowPranks()
                        && evt.getPlayer().getInventory().getHelmet() != null
                        && (evt.getPlayer().getInventory().getHelmet().getType().name().equals("PUMPKIN")
                        || evt.getPlayer().getInventory().getHelmet().getType().name().equals("CARVED_PUMPKIN"))) {
                        if (!evt.getAction().equals(Action.RIGHT_CLICK_AIR)) {
                            evt.getPlayer().sendMessage(" " + ChatColor.AQUA + ChatColor.UNDERLINE 
                                + "https://www.youtube.com/watch?v=v4IC7qaNr7I");
                        }
                        evt.setCancelled(true);
                        return;
                }
            }
            if (plugin.canUseQuests(evt.getPlayer().getUniqueId())) {
                final Quester quester = plugin.getQuester(evt.getPlayer().getUniqueId());
                final Player player = evt.getPlayer();
                if (evt.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                    boolean hasObjective = false;
                    if (evt.isCancelled() == false) {
                        for (final Quest quest : plugin.getLoadedQuests()) {
                            if (quester.getCurrentQuests().containsKey(quest) 
                                    && quester.getCurrentStage(quest).containsObjective("useBlock")) {
                                hasObjective = true;
                            }
                        }
                    }
                    if (!hasObjective) {
                        if (plugin.getQuestFactory().getSelectedBlockStarts().containsKey(evt.getPlayer()
                                .getUniqueId())) {
                            final Block block = evt.getClickedBlock();
                            final Location loc = block.getLocation();
                            final Map<UUID, Block> temp = plugin.getQuestFactory().getSelectedBlockStarts();
                            temp.put(evt.getPlayer().getUniqueId(), block);
                            plugin.getQuestFactory().setSelectedBlockStarts(temp);
                            evt.getPlayer().sendMessage(ChatColor.GOLD + Lang.get(player, "questSelectedLocation") 
                                    + " " + ChatColor.AQUA + loc.getWorld().getName() + ": " + loc.getX() + ", " 
                                    + loc.getY() + ", " + loc.getZ() + ChatColor.GOLD + " (" + ChatColor.GREEN 
                                    + ItemUtil.getName(new ItemStack(block.getType())) + ChatColor.GOLD + ")");
                            evt.setCancelled(true);
                        } else if (plugin.getActionFactory().getSelectedExplosionLocations().containsKey(evt.getPlayer()
                                .getUniqueId())) {
                            final Block block = evt.getClickedBlock();
                            final Location loc = block.getLocation();
                            final Map<UUID, Block> temp = plugin.getActionFactory().getSelectedExplosionLocations();
                            temp.put(evt.getPlayer().getUniqueId(), block);
                            plugin.getActionFactory().setSelectedExplosionLocations(temp);
                            evt.getPlayer().sendMessage(ChatColor.GOLD + Lang.get(player, "questSelectedLocation") + " "
                                    + ChatColor.AQUA + loc.getWorld().getName() + ": " + loc.getX() + ", " + loc.getY() 
                                    + ", " + loc.getZ() + ChatColor.GOLD + " (" + ChatColor.GREEN 
                                    + ItemUtil.getName(new ItemStack(block.getType())) + ChatColor.GOLD + ")");
                            evt.setCancelled(true);
                        } else if (plugin.getActionFactory().getSelectedEffectLocations().containsKey(evt.getPlayer()
                                .getUniqueId())) {
                            final Block block = evt.getClickedBlock();
                            final Location loc = block.getLocation();
                            final Map<UUID, Block> temp = plugin.getActionFactory().getSelectedEffectLocations();
                            temp.put(evt.getPlayer().getUniqueId(), block);
                            plugin.getActionFactory().setSelectedEffectLocations(temp);
                            evt.getPlayer().sendMessage(ChatColor.GOLD + Lang.get(player, "questSelectedLocation") + " "
                                    + ChatColor.AQUA + loc.getWorld().getName() + ": " + loc.getX() + ", " + loc.getY() 
                                    + ", " + loc.getZ() + ChatColor.GOLD + " (" + ChatColor.GREEN 
                                    + ItemUtil.getName(new ItemStack(block.getType())) + ChatColor.GOLD + ")");
                            evt.setCancelled(true);
                        } else if (plugin.getActionFactory().getSelectedMobLocations().containsKey(evt.getPlayer()
                                .getUniqueId())) {
                            final Block block = evt.getClickedBlock();
                            final Location loc = block.getLocation();
                            final Map<UUID, Block> temp = plugin.getActionFactory().getSelectedMobLocations();
                            temp.put(evt.getPlayer().getUniqueId(), block);
                            plugin.getActionFactory().setSelectedMobLocations(temp);
                            evt.getPlayer().sendMessage(ChatColor.GOLD + Lang.get(player, "questSelectedLocation") + " "
                                    + ChatColor.AQUA + loc.getWorld().getName() + ": " + loc.getX() + ", " + loc.getY() 
                                    + ", " + loc.getZ() + ChatColor.GOLD + " (" + ChatColor.GREEN 
                                    + ItemUtil.getName(new ItemStack(block.getType())) + ChatColor.GOLD + ")");
                            evt.setCancelled(true);
                        } else if (plugin.getActionFactory().getSelectedLightningLocations().containsKey(evt.getPlayer()
                                .getUniqueId())) {
                            final Block block = evt.getClickedBlock();
                            final Location loc = block.getLocation();
                            final Map<UUID, Block> temp = plugin.getActionFactory().getSelectedLightningLocations();
                            temp.put(evt.getPlayer().getUniqueId(), block);
                            plugin.getActionFactory().setSelectedLightningLocations(temp);
                            evt.getPlayer().sendMessage(ChatColor.GOLD + Lang.get(player, "questSelectedLocation") + " "
                                    + ChatColor.AQUA + loc.getWorld().getName() + ": " + loc.getX() + ", " + loc.getY() 
                                    + ", " + loc.getZ() + ChatColor.GOLD + " (" + ChatColor.GREEN 
                                    + ItemUtil.getName(new ItemStack(block.getType())) + ChatColor.GOLD + ")");
                            evt.setCancelled(true);
                        } else if (plugin.getActionFactory().getSelectedTeleportLocations().containsKey(evt.getPlayer()
                                .getUniqueId())) {
                            final Block block = evt.getClickedBlock();
                            final Location loc = block.getLocation();
                            final Map<UUID, Block> temp = plugin.getActionFactory().getSelectedTeleportLocations();
                            temp.put(evt.getPlayer().getUniqueId(), block);
                            plugin.getActionFactory().setSelectedTeleportLocations(temp);
                            evt.getPlayer().sendMessage(ChatColor.GOLD + Lang.get(player, "questSelectedLocation") + " "
                                    + ChatColor.AQUA + loc.getWorld().getName() + ": " + loc.getX() + ", " + loc.getY() 
                                    + ", " + loc.getZ() + ChatColor.GOLD + " (" + ChatColor.GREEN 
                                    + ItemUtil.getName(new ItemStack(block.getType())) + ChatColor.GOLD + ")");
                            evt.setCancelled(true);
                        } else if (plugin.getQuestFactory().getSelectedKillLocations().containsKey(evt.getPlayer()
                                .getUniqueId())) {
                            final Block block = evt.getClickedBlock();
                            final Location loc = block.getLocation();
                            final Map<UUID, Block> temp = plugin.getQuestFactory().getSelectedKillLocations();
                            temp.put(evt.getPlayer().getUniqueId(), block);
                            plugin.getQuestFactory().setSelectedKillLocations(temp);
                            evt.getPlayer().sendMessage(ChatColor.GOLD + Lang.get(player, "questSelectedLocation") + " "
                                    + ChatColor.AQUA + loc.getWorld().getName() + ": " + loc.getX() + ", " + loc.getY() 
                                    + ", " + loc.getZ() + ChatColor.GOLD + " (" + ChatColor.GREEN 
                                    + ItemUtil.getName(new ItemStack(block.getType())) + ChatColor.GOLD + ")");
                            evt.setCancelled(true);
                        } else if (plugin.getQuestFactory().getSelectedReachLocations().containsKey(evt.getPlayer()
                                .getUniqueId())) {
                            final Block block = evt.getClickedBlock();
                            final Location loc = block.getLocation();
                            final Map<UUID, Block> temp = plugin.getQuestFactory().getSelectedReachLocations();
                            temp.put(evt.getPlayer().getUniqueId(), block);
                            plugin.getQuestFactory().setSelectedReachLocations(temp);
                            evt.getPlayer().sendMessage(ChatColor.GOLD + Lang.get(player, "questSelectedLocation") + " "
                            + ChatColor.AQUA + loc.getWorld().getName() + ": " + loc.getX() + ", " + loc.getY() + ", " 
                                    + loc.getZ() + ChatColor.GOLD + " (" + ChatColor.GREEN 
                                    + ItemUtil.getName(new ItemStack(block.getType())) + ChatColor.GOLD + ")");
                            evt.setCancelled(true);
                        } else if (player.isConversing() == false) {
                            for (final Quest q : plugin.getLoadedQuests()) {
                                if (q.getBlockStart() != null) {
                                    if (q.getBlockStart().equals(evt.getClickedBlock().getLocation())) {
                                        if (quester.getCurrentQuests().size() >= plugin.getSettings().getMaxQuests() 
                                                && plugin.getSettings().getMaxQuests() > 0) {
                                            String msg = Lang.get(player, "questMaxAllowed");
                                            msg = msg.replace("<number>", String
                                                    .valueOf(plugin.getSettings().getMaxQuests()));
                                            player.sendMessage(ChatColor.YELLOW + msg);
                                        } else {
                                            if (quester.getCompletedQuests().contains(q)) {
                                                if (q.getPlanner().getCooldown() > -1 
                                                        && (quester.getCooldownDifference(q)) > 0) {
                                                    String early = Lang.get(player, "questTooEarly");
                                                    early = early.replace("<quest>", ChatColor.AQUA + q.getName() 
                                                            + ChatColor.YELLOW);
                                                    early = early.replace("<time>", ChatColor.DARK_PURPLE 
                                                            + MiscUtil.getTime(quester.getCooldownDifference(q)) 
                                                            + ChatColor.YELLOW);
                                                    player.sendMessage(ChatColor.YELLOW + early);
                                                    continue;
                                                } else if (quester.getCompletedQuests().contains(q) 
                                                        && q.getPlanner().getCooldown() < 0) {
                                                    String completed = Lang.get(player, "questAlreadyCompleted");
                                                    completed = completed.replace("<quest>", ChatColor.AQUA 
                                                            + q.getName() + ChatColor.YELLOW);
                                                    player.sendMessage(ChatColor.YELLOW + completed);
                                                    continue;
                                                }
                                            }
                                            quester.setQuestIdToTake(q.getId());
                                            if (!plugin.getSettings().canAskConfirmation()) {
                                                quester.takeQuest(q, false);
                                            } else {
                                                final Quest quest = plugin.getQuestById(quester.getQuestIdToTake());
                                                final String s = ChatColor.GOLD + "- " + ChatColor.DARK_PURPLE 
                                                        + quest.getName() + ChatColor.GOLD + " -\n" + "\n" 
                                                        + ChatColor.RESET + quest.getDescription() + "\n";
                                                for (final String msg : s.split("<br>")) {
                                                    player.sendMessage(msg);
                                                }
                                                plugin.getConversationFactory().buildConversation(player).begin();
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (evt.getItem() != null && evt.getItem().getType().equals(Material.COMPASS)) {
                    if (!quester.canUseCompass()) {
                        return;
                    }
                    if (evt.getAction().equals(Action.LEFT_CLICK_AIR)
                            || evt.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                        quester.resetCompass();
                        player.sendMessage(ChatColor.YELLOW + Lang.get(player, "compassReset"));
                    } else if (evt.getAction().equals(Action.RIGHT_CLICK_AIR)
                            || evt.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                        quester.findNextCompassTarget(true);
                    }
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerInteractEntity(final PlayerInteractEntityEvent evt) {
        if (evt.getRightClicked().getType() == EntityType.ITEM_FRAME) {
            if (ItemUtil.isJournal(evt.getPlayer().getItemInHand())) {
                evt.setCancelled(true);
                evt.getPlayer().sendMessage(ChatColor.RED + Lang.get(evt.getPlayer(), "journalDenied")
                        .replace("<journal>", Lang.get(evt.getPlayer(), "journalTitle")));
            }
        }
    }
    
    @EventHandler
    public void onPlayerBucketFill(final PlayerBucketFillEvent evt) {
        if (evt.getItemStack().getType() == Material.MILK_BUCKET) {
            final Player player = evt.getPlayer();
            if (plugin.canUseQuests(player.getUniqueId())) {
                final Quester quester = plugin.getQuester(player.getUniqueId());
                final ObjectiveType type = ObjectiveType.MILK_COW;
                final Set<String> dispatchedQuestIDs = new HashSet<String>();
                for (final Quest quest : plugin.getLoadedQuests()) {
                    if (!quester.meetsCondition(quest, true)) {
                        continue;
                    }
                    
                    if (quester.getCurrentQuests().containsKey(quest) 
                            && quester.getCurrentStage(quest).containsObjective(type)) {
                        quester.milkCow(quest);
                    }
                    
                    dispatchedQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, type, 
                            (final Quester q, final Quest cq) -> {
                        if (!dispatchedQuestIDs.contains(cq.getId())) {
                            q.milkCow(cq);
                        }
                        return null;
                    }));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerChat(final AsyncPlayerChatEvent evt) {
        if (plugin.canUseQuests(evt.getPlayer().getUniqueId())) {
            final Quester quester = plugin.getQuester(evt.getPlayer().getUniqueId());
            for (final Quest quest : plugin.getLoadedQuests()) {
                if (!quester.meetsCondition(quest, true)) {
                    continue;
                }
                
                if (quester.getCurrentQuests().containsKey(quest)) {
                    final Stage currentStage = quester.getCurrentStage(quest);
                    if (currentStage == null) {
                        continue;
                    }
                    if (currentStage.getChatActions().isEmpty() == false) {
                        final String chat = evt.getMessage();
                        for (final String s : currentStage.getChatActions().keySet()) {
                            if (s.equalsIgnoreCase(chat)) {
                                if (quester.getQuestData(quest).actionFired.get(s) == null 
                                        || quester.getQuestData(quest).actionFired.get(s) == false) {
                                    new BukkitRunnable() {                        
                                        @Override
                                        public void run() {
                                            currentStage.getChatActions().get(s).fire(quester, quest);
                                        }
                                        
                                    }.runTask(this.plugin);
                                    quester.getQuestData(quest).actionFired.put(s, true);
                                }
                            }
                        }
                    }
                    final ObjectiveType type = ObjectiveType.PASSWORD;
                    final Set<String> dispatchedQuestIDs = new HashSet<String>();
                    if (quester.getCurrentStage(quest).containsObjective(type)) {
                        for (final LinkedList<String> passes : quester.getCurrentStage(quest).getPasswordPhrases()) {
                            for (final String pass : passes) {
                                if (pass.equalsIgnoreCase(evt.getMessage())) {
                                    evt.setCancelled(true);
                                    break;
                                }
                            }
                        }
                        quester.sayPassword(quest, evt);
                    }
                    
                    dispatchedQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, type, 
                            (final Quester q, final Quest cq) -> {
                        if (!dispatchedQuestIDs.contains(cq.getId())) {
                            q.sayPassword(cq, evt);
                        }
                        return null;
                    }));
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent evt) {
        if (plugin.canUseQuests(evt.getPlayer().getUniqueId())) {
            final Quester quester = plugin.getQuester(evt.getPlayer().getUniqueId());
            if (quester.getCurrentQuests().isEmpty() == false) {
                for (final Quest quest : quester.getCurrentQuests().keySet()) {
                    if (!quest.getOptions().canAllowCommands()) {
                        if (!evt.getMessage().startsWith("/quest")) {
                            evt.getPlayer().sendMessage(ChatColor.RED + Lang.get(evt.getPlayer(), "optCommandsDenied")
                                    .replace("<quest>", ChatColor.DARK_PURPLE + quest.getName() + ChatColor.RED));
                            evt.setCancelled(true);
                            plugin.getLogger().info("Player " + evt.getPlayer().getName() + " tried to use command " 
                                    + evt.getMessage() + " but was denied because they are currently on quest "
                                    + quest.getName());
                            return;
                        }
                    }
                    final Stage currentStage = quester.getCurrentStage(quest);
                    if (currentStage == null) {
                        plugin.getLogger().severe("currentStage was null for " + quester.getUUID().toString() 
                               + " on command for quest " + quest.getName());
                        continue;
                    }
                    if (currentStage.getCommandActions().isEmpty() == false) {
                        final String command = evt.getMessage();
                        for (final String s : currentStage.getCommandActions().keySet()) {
                            if (command.equalsIgnoreCase("/" + s)) {
                                if (quester.getQuestData(quest).actionFired.get(s) == null 
                                        || quester.getQuestData(quest).actionFired.get(s) == false) {
                                    currentStage.getCommandActions().get(s).fire(quester, quest);
                                    quester.getQuestData(quest).actionFired.put(s, true);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerShearEntity(final PlayerShearEntityEvent evt) {
        if (evt.getEntity().getType() == EntityType.SHEEP) {
            final Player player = evt.getPlayer();
            if (plugin.canUseQuests(player.getUniqueId())) {
                final Sheep sheep = (Sheep) evt.getEntity();
                final Quester quester = plugin.getQuester(player.getUniqueId());
                final ObjectiveType type = ObjectiveType.SHEAR_SHEEP;
                final Set<String> dispatchedQuestIDs = new HashSet<String>();
                for (final Quest quest : plugin.getLoadedQuests()) {
                    if (!quester.meetsCondition(quest, true)) {
                        continue;
                    }
                    
                    if (quester.getCurrentQuests().containsKey(quest) 
                            && quester.getCurrentStage(quest).containsObjective(type)) {
                        quester.shearSheep(quest, sheep.getColor());
                    }
                    
                    dispatchedQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, type, 
                            (final Quester q, final Quest cq) -> {
                        if (!dispatchedQuestIDs.contains(cq.getId())) {
                            q.shearSheep(cq, sheep.getColor());
                        }
                        return null;
                    }));
                }
            }
        }
    }

    @EventHandler
    public void onEntityTame(final EntityTameEvent evt) {
        if (evt.getOwner() instanceof Player) {
            final Player player = (Player) evt.getOwner();
            if (plugin.canUseQuests(player.getUniqueId())) {
                final Quester quester = plugin.getQuester(player.getUniqueId());
                final ObjectiveType type = ObjectiveType.TAME_MOB;
                final Set<String> dispatchedQuestIDs = new HashSet<String>();
                for (final Quest quest : plugin.getLoadedQuests()) {
                    if (!quester.meetsCondition(quest, true)) {
                        continue;
                    }
                    
                    if (quester.getCurrentQuests().containsKey(quest) 
                            && quester.getCurrentStage(quest).containsObjective(type)) {
                        quester.tameMob(quest, evt.getEntityType());
                    }
                    
                    dispatchedQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, type, 
                            (final Quester q, final Quest cq) -> {
                        if (!dispatchedQuestIDs.contains(cq.getId())) {
                            q.tameMob(cq, evt.getEntityType());
                        }
                        return null;
                    }));
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(final EntityDeathEvent evt) {
        if (evt.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            final EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) evt.getEntity().getLastDamageCause();
            final Entity damager = damageEvent.getDamager();
                
            if (damager != null) {
                if (damager instanceof Projectile) {
                    final Projectile projectile = (Projectile) damager;
                    if (projectile.getShooter() != null && projectile.getShooter() instanceof Entity) {
                        preKillMob((Entity)projectile.getShooter(), evt.getEntity());
                    }
                } else if (damager instanceof TNTPrimed) {
                    final TNTPrimed tnt = (TNTPrimed) damager;
                    final Entity source = tnt.getSource();
                    if (source != null && source.isValid()) {
                        preKillMob(source, evt.getEntity());
                    }
                } else if (damager instanceof Wolf) {
                    final Wolf wolf = (Wolf) damager;
                    if (wolf.isTamed() && wolf.getOwner() != null) {
                        final Quester quester = plugin.getQuester(wolf.getOwner().getUniqueId());
                        if (quester != null) {
                            preKillPlayer(quester.getPlayer(), evt.getEntity());
                        }
                    }
                } else {
                    preKillMob(damager, evt.getEntity());
                }
            }
        }
    }
    
    /**
     * Checks if damager is blacklisted. Ensures damager is Player and not NPC. Kills target mob/NPC if objective exists
     * 
     * @param damager the attacking entity
     * @param target the entity being attacked
     * @since 3.1.4
     */
    public void preKillMob(final Entity damager, final Entity target) {
        if (!plugin.canUseQuests(damager.getUniqueId())) {
            return;
        }
        if (damager instanceof Player) {
            if (plugin.getDependencies().getCitizens() != null && CitizensAPI.getNPCRegistry().isNPC(target)) {
                return;
            }
            final Quester quester = plugin.getQuester(damager.getUniqueId());
            final ObjectiveType type = ObjectiveType.KILL_MOB;
            final Set<String> dispatchedQuestIDs = new HashSet<String>();
            for (final Quest quest : plugin.getLoadedQuests()) {
                if (!quester.meetsCondition(quest, true)) {
                    continue;
                }
                
                if (quester.getCurrentQuests().containsKey(quest) 
                        && quester.getCurrentStage(quest).containsObjective(type)) {
                    quester.killMob(quest, target.getLocation(), target.getType());
                }
                
                dispatchedQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, type, 
                        (final Quester q, final Quest cq) -> {
                    if (!dispatchedQuestIDs.contains(cq.getId())) {
                        q.killMob(cq, target.getLocation(), target.getType());
                    }
                    return null;
                }));
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(final PlayerDeathEvent evt) {
        if (evt.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            final EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) evt.getEntity().getLastDamageCause();
            final Entity damager = damageEvent.getDamager();

            if (damager != null) {
                //Ignore suicide
                if (evt.getEntity().getUniqueId().equals(damager.getUniqueId())) {
                    return;
                }
                if (damager instanceof Projectile) {
                    final Projectile projectile = (Projectile) damager;
                    if (projectile.getShooter() != null && projectile.getShooter() instanceof Entity) {
                        preKillPlayer((Entity)projectile.getShooter(), evt.getEntity());
                    }
                } else if (damager instanceof TNTPrimed) {
                    final TNTPrimed tnt = (TNTPrimed) damager;
                    final Entity source = tnt.getSource();
                    if (source != null) {
                        if (source.isValid()) {
                            preKillPlayer(source, evt.getEntity());
                        }
                    }
                } else if (damager instanceof Wolf) {
                    final Wolf wolf = (Wolf) damager;
                    if (wolf.isTamed()) {
                        final Quester quester = plugin.getQuester(wolf.getOwner().getUniqueId());
                        preKillPlayer(quester.getPlayer(), evt.getEntity());
                    }
                } else {
                    preKillPlayer(damager, evt.getEntity());
                }
            }
        }
            
        final Player target = evt.getEntity();
        if (plugin.canUseQuests(target.getUniqueId())) {
            final Quester quester = plugin.getQuester(target.getUniqueId());
            for (final Quest quest : quester.getCurrentQuests().keySet()) {
                final Stage stage = quester.getCurrentStage(quest);
                if (stage != null && stage.getDeathAction() != null) {
                    quester.getCurrentStage(quest).getDeathAction().fire(quester, quest);
                }
            }
        }
        ItemStack found = null;
        for (final ItemStack stack : evt.getDrops()) {
            if (ItemUtil.isJournal(stack)) {
                found = stack;
                break;
            }
        }
        if (found != null) {
            evt.getDrops().remove(found);
        }
    }
    
    /**
     * Checks if damager is blacklisted. Ensures damager and target are Player and not NPC.
     * Kills target Player if objective exists<p>
     * 
     * As of 3.8.9, damager and target must not be the same entity
     * 
     * @param damager the attacking entity
     * @param target the entity being attacked
     * @since 3.1.4
     */
    public void preKillPlayer(final Entity damager, final Entity target) {
        if (damager == null || target == null || damager.equals(target)) {
            return;
        }
        if (!plugin.canUseQuests(damager.getUniqueId())) {
            return;
        }
        if (damager instanceof Player && target instanceof Player) {
            if (plugin.getDependencies().getCitizens() != null) {
                if (CitizensAPI.getNPCRegistry().isNPC(damager) && CitizensAPI.getNPCRegistry().isNPC(target)) {
                    return;
                }
            }
            final Quester quester = plugin.getQuester(damager.getUniqueId());
            final ObjectiveType type = ObjectiveType.KILL_PLAYER;
            final Set<String> dispatchedQuestIDs = new HashSet<String>();
            for (final Quest quest : plugin.getLoadedQuests()) {
                if (!quester.meetsCondition(quest, true)) {
                    continue;
                }
                
                if (quester.getCurrentQuests().containsKey(quest) 
                        && quester.getCurrentStage(quest).containsObjective(type)) {
                    quester.killPlayer(quest, (Player)target);
                }
                
                dispatchedQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, type, 
                        (final Quester q, final Quest cq) -> {
                    if (!dispatchedQuestIDs.contains(cq.getId())) {
                        q.killPlayer(cq, (Player)target);
                    }
                    return null;
                }));
            }
        }
    }

    @EventHandler
    public void onPlayerFish(final PlayerFishEvent evt) {
        final Player player = evt.getPlayer();
        if (plugin.canUseQuests(player.getUniqueId())) {
            final Quester quester = plugin.getQuester(player.getUniqueId());
            final ObjectiveType type = ObjectiveType.CATCH_FISH;
            final Set<String> dispatchedQuestIDs = new HashSet<String>();
            for (final Quest quest : plugin.getLoadedQuests()) {
                if (!quester.meetsCondition(quest, true)) {
                    continue;
                }
                
                if (evt.getState().equals(State.CAUGHT_FISH)) {
                    if (quester.getCurrentQuests().containsKey(quest) 
                            && quester.getCurrentStage(quest).containsObjective(type)) {
                        quester.catchFish(quest);
                    }
                    
                    dispatchedQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, type, 
                            (final Quester q, final Quest cq) -> {
                        if (!dispatchedQuestIDs.contains(cq.getId())) {
                            q.catchFish(cq);
                        }
                        return null;
                    }));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerChangeWorld(final PlayerChangedWorldEvent event) {
        final Player player = event.getPlayer();
        if (plugin.canUseQuests(player.getUniqueId())) {
            final Quester quester = plugin.getQuester(player.getUniqueId());
            quester.findCompassTarget();
            for (final Quest quest : plugin.getLoadedQuests()) {
                if (!quester.meetsCondition(quest, true)) {
                    continue;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(final PlayerRespawnEvent event) {
        final Player player = event.getPlayer();
        if (plugin.canUseQuests(player.getUniqueId())) {
            final Quester quester = plugin.getQuester(player.getUniqueId());
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {

                @Override
                public void run() {
                    quester.findCompassTarget();
                }
            }, 10);
        }
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent evt) {
        if (plugin.canUseQuests(evt.getPlayer().getUniqueId())) {
            final Quester noobCheck = new Quester(plugin, evt.getPlayer().getUniqueId());
            if (plugin.getSettings().canGenFilesOnJoin() && !noobCheck.hasData()) {
                noobCheck.saveData();
            }
            
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    final CompletableFuture<Quester> cf = plugin.getStorage().loadQuesterData(evt.getPlayer().getUniqueId());
                    try {
                        final Quester quester = cf.get();
                        if (quester == null) {
                            return;
                        }
                        for (final Quest q : quester.getCompletedQuests()) {
                            if (q != null) {
                                if (!quester.getCompletedTimes().containsKey(q) && q.getPlanner().getCooldown() > -1) {
                                    quester.getCompletedTimes().put(q, System.currentTimeMillis());
                                }
                            }
                        }
                        for (final Quest quest : quester.getCurrentQuests().keySet()) {
                            quester.checkQuest(quest);
                        }
                        for (final Quest quest : quester.getCurrentQuests().keySet()) {
                            if (quester.getCurrentStage(quest).getDelay() > -1 /*&& !quester.getQuestData(quest).isDelayOver()*/) {
                                quester.startStageTimer(quest);
                            }
                        }
                        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

                            @Override
                            public void run() {
                                if (quester.hasJournal()) {
                                    quester.updateJournal();
                                }
                                if (quester.canUseCompass()) {
                                    quester.resetCompass();
                                }
                            }
                        }, 40L);
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent evt) {
        if (plugin.canUseQuests(evt.getPlayer().getUniqueId())) {
            final Quester quester = plugin.getQuester(evt.getPlayer().getUniqueId());
            for (final Quest quest : quester.getCurrentQuests().keySet()) {
                final Stage currentStage = quester.getCurrentStage(quest);
                if (currentStage == null) {
                    plugin.getLogger().severe("currentStage was null for " + quester.getUUID().toString() 
                            + " on quit for quest " + quest.getName());
                    continue;
                }
                if (currentStage.getDelay() > -1) {
                    quester.stopStageTimer(quest);
                }
                if (currentStage.getDisconnectAction() != null) {
                    currentStage.getDisconnectAction().fire(quester, quest);
                }
            }
            for (final Integer timerId : quester.getTimers().keySet()) {
                plugin.getServer().getScheduler().cancelTask(timerId);
                if (quester.getTimers().containsKey(timerId)) {
                    quester.getTimers().get(timerId).failQuest(quester);
                    quester.removeTimer(timerId);
                }
            }
            if (!plugin.getSettings().canGenFilesOnJoin()) {
                if (quester.hasBaseData()) {
                    quester.saveData();
                }
            } else {
                quester.saveData();
            }
            
            if (plugin.getQuestFactory().getSelectingNpcs().contains(evt.getPlayer().getUniqueId())) {
                final Set<UUID> temp = plugin.getQuestFactory().getSelectingNpcs();
                temp.remove(evt.getPlayer().getUniqueId());
                plugin.getQuestFactory().setSelectingNpcs(temp);
            }
            final ConcurrentSkipListSet<Quester> temp = (ConcurrentSkipListSet<Quester>) plugin.getOfflineQuesters();
            for (final Iterator<Quester> iterator = temp.iterator(); iterator.hasNext();) {
                final Quester q = iterator.next();
                if (q.getUUID().equals(quester.getUUID())) {
                    iterator.remove();
                }
            }
            plugin.setOfflineQuesters(temp);
        }
    }

    @EventHandler
    public void onPlayerMove(final PlayerMoveEvent evt) {
        if (evt.getPlayer() == null || evt.getTo() == null) {
            return;
        }
        if (evt.getFrom().getBlock().equals(evt.getTo().getBlock())) {
            return;
        }
        if (plugin.getDependencies().getCitizens() != null) {
            if (CitizensAPI.getNPCRegistry().isNPC(evt.getPlayer())) {
                return;
            }
        }
        playerMove(evt.getPlayer().getUniqueId(), evt.getTo());
    }
    
    /**
     * Checks if uuid is blacklisted. Updates reach-location objectives<p>
     * 
     * Runs asynchronously since 3.9.6
     * 
     * @param uuid The UUID of the Player
     * @param location The current location of the Player
     * @since 3.8.2
     */
    public void playerMove(final UUID uuid, final Location location) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

            @Override
            public void run() {
                final Quester quester = plugin.getQuester(uuid);
                if (quester != null) {
                    if (plugin.canUseQuests(uuid)) {
                        final ObjectiveType type = ObjectiveType.REACH_LOCATION;
                        final Set<String> dispatchedQuestIDs = new HashSet<String>();
                        for (final Quest quest : plugin.getLoadedQuests()) {
                            if (!quester.meetsCondition(quest, true)) {
                                continue;
                            }
                            
                            if (quester.getCurrentQuests().containsKey(quest)) {
                                if (quester.getCurrentStage(quest) != null 
                                        && quester.getCurrentStage(quest).containsObjective(type)) {
                                    plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
                                        @Override
                                        public void run() {
                                            quester.reachLocation(quest, location);
                                        }
                                    });
                                }
                            }
                            
                            dispatchedQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, type, 
                                    (final Quester q, final Quest cq) -> {
                                if (!dispatchedQuestIDs.contains(cq.getId())) {
                                    plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
                                        @Override
                                        public void run() {
                                            q.reachLocation(cq, location);
                                        }
                                    });
                                }
                                return null;
                            }));
                        }
                    }
                }
            }
        });
    }
}
