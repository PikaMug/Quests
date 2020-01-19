/*******************************************************************************************************

 * Continued by PikaMug (formerly HappyPikachu) with permission from _Blackvein_. All rights reserved.
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

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
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
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
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
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;
import net.citizensnpcs.api.CitizensAPI;

public class PlayerListener implements Listener {

    final Quests plugin;

    public PlayerListener(Quests newPlugin) {
        plugin = newPlugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClickEvent(InventoryClickEvent evt) {
        InventoryAction ac = evt.getAction();
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
        }
        if (ItemUtil.isItem(evt.getCurrentItem()) && ItemUtil.isJournal(evt.getCurrentItem()) 
                || ItemUtil.isItem(evt.getCursor()) && ItemUtil.isJournal(evt.getCursor())) {
            int upper = evt.getView().getTopInventory().getSize();
            if (evt.getView().getTopInventory().getType().equals(InventoryType.CRAFTING))
                upper += 4;
            int lower = evt.getView().getBottomInventory().getSize();
            int relative = evt.getRawSlot() - upper;
            if (relative < 0 || relative >= (lower)) {
                evt.setCancelled(true);
                return;
            }
        }
        Quester quester = plugin.getQuester(evt.getWhoClicked().getUniqueId());
        Player player = (Player) evt.getWhoClicked();
        if (evt.getView().getTitle().contains(Lang.get(player, "quests"))) {
            ItemStack clicked = evt.getCurrentItem();
            if (clicked != null) {
                for (Quest quest : plugin.getQuests()) {
                    if (quest.getGUIDisplay() != null) {
                        if (ItemUtil.compareItems(clicked, quest.getGUIDisplay(), false) == 0) {
                            if (quester.getCurrentQuests().size() >= plugin.getSettings().getMaxQuests() 
                                    && plugin.getSettings().getMaxQuests() > 0) {
                                String msg = Lang.get(player, "questMaxAllowed");
                                msg = msg.replace("<number>", String.valueOf(plugin.getSettings().getMaxQuests()));
                                player.sendMessage(ChatColor.YELLOW + msg);
                            } else if (quester.getCompletedQuests().contains(quest.getName()) 
                                    && quest.getPlanner().getCooldown() < 0) {
                                String completed = Lang.get(player, "questAlreadyCompleted");
                                completed = completed.replace("<quest>", ChatColor.AQUA + quest.getName() 
                                        + ChatColor.YELLOW);
                                player.sendMessage(ChatColor.YELLOW + completed);
                            } else {
                                boolean takeable = true;
                                if (quester.getCompletedQuests().contains(quest.getName())) {
                                    if (quester.getCooldownDifference(quest) > 0) {
                                        String early = Lang.get(player, "questTooEarly");
                                        early = early.replace("<quest>", ChatColor.AQUA + quest.getName() 
                                                + ChatColor.YELLOW);
                                        early = early.replace("<time>", ChatColor.DARK_PURPLE 
                                                + MiscUtil.getTime(quester.getCooldownDifference(quest)) 
                                                + ChatColor.YELLOW);
                                        player.sendMessage(ChatColor.YELLOW + early);
                                        takeable = false;
                                    }
                                }
                                if (quest.getRegionStart() != null) {
                                    if (!quest.isInRegion(quester)) {
                                        String invalidLoc = Lang.get(player, "questInvalidLocation");
                                        invalidLoc = invalidLoc.replace("<quest>", ChatColor.AQUA + quest.getName() 
                                                + ChatColor.YELLOW);
                                        player.sendMessage(ChatColor.YELLOW + invalidLoc);
                                        takeable = false;
                                    }
                                }
                                if (takeable == true) {
                                    try { 
                                        quester.takeQuest(quest, false);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    
                                }
                                evt.getWhoClicked().closeInventory();
                            }
                        }
                    }
                }
                evt.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDragEvent(InventoryDragEvent evt) {
        if (ItemUtil.isItem(evt.getOldCursor()) && ItemUtil.isJournal(evt.getOldCursor()) 
                || ItemUtil.isItem(evt.getCursor()) && ItemUtil.isJournal(evt.getCursor())) {
            int upper = evt.getView().getTopInventory().getSize();
            if (evt.getView().getTopInventory().getType().equals(InventoryType.CRAFTING))
                upper += 4;
            int lower = evt.getView().getBottomInventory().getSize();
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
    public void onPlayerDropItem(PlayerDropItemEvent evt) {
        if (ItemUtil.isJournal(evt.getItemDrop().getItemStack())) {
            if (!evt.getPlayer().hasPermission("quests.admin.drop")) {
                evt.setCancelled(true);
            }
        }
    }

    @SuppressWarnings("deprecation") // since 1.13
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent evt) {
        EquipmentSlot e = null;
        try {
            e = evt.getHand();
        } catch (NoSuchMethodError err) {
            // Do nothing, getHand() not present pre-1.9
        }
        if (e == null || e.equals(EquipmentSlot.HAND)) { //If the event is fired by HAND (main hand)
            if (plugin.checkQuester(evt.getPlayer().getUniqueId()) == false) {
                if (evt.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                    final Quester quester = plugin.getQuester(evt.getPlayer().getUniqueId());
                    final Player player = evt.getPlayer();
                    boolean hasObjective = false;
                    if (evt.isCancelled() == false) {
                        final ItemStack blockItemStack = new ItemStack(evt.getClickedBlock().getType(), 1, evt
                                .getClickedBlock().getState().getData().toItemStack().getDurability());
                        for (Quest quest : plugin.getQuests()) {
                            if (quester.getCurrentQuests().containsKey(quest) 
                                    && quester.containsObjective(quest, "useBlock")) {
                                quester.useBlock(quest, blockItemStack);
                                hasObjective = true;
                            }
                            
                            quester.dispatchMultiplayerEverything(quest, "useBlock", (Quester q) -> {
                                q.useBlock(quest, blockItemStack);
                                return null;
                            });
                        }
                    }
                    if (!hasObjective) {
                        if (plugin.getQuestFactory().getSelectedBlockStarts().containsKey(evt.getPlayer()
                                .getUniqueId())) {
                            Block block = evt.getClickedBlock();
                            Location loc = block.getLocation();
                            Map<UUID, Block> temp = plugin.getQuestFactory().getSelectedBlockStarts();
                            temp.put(evt.getPlayer().getUniqueId(), block);
                            plugin.getQuestFactory().setSelectedBlockStarts(temp);
                            evt.getPlayer().sendMessage(ChatColor.GOLD + Lang.get(player, "questSelectedLocation") 
                                    + " " + ChatColor.AQUA + loc.getWorld().getName() + ": " + loc.getX() + ", " 
                                    + loc.getY() + ", " + loc.getZ() + ChatColor.GOLD + " (" + ChatColor.GREEN 
                                    + ItemUtil.getName(new ItemStack(block.getType())) + ChatColor.GOLD + ")");
                            evt.setCancelled(true);
                        } else if (plugin.getEventFactory().getSelectedExplosionLocations().containsKey(evt.getPlayer()
                                .getUniqueId())) {
                            Block block = evt.getClickedBlock();
                            Location loc = block.getLocation();
                            Map<UUID, Block> temp = plugin.getEventFactory().getSelectedExplosionLocations();
                            temp.put(evt.getPlayer().getUniqueId(), block);
                            plugin.getEventFactory().setSelectedExplosionLocations(temp);
                            evt.getPlayer().sendMessage(ChatColor.GOLD + Lang.get(player, "questSelectedLocation") + " "
                                    + ChatColor.AQUA + loc.getWorld().getName() + ": " + loc.getX() + ", " + loc.getY() 
                                    + ", " + loc.getZ() + ChatColor.GOLD + " (" + ChatColor.GREEN 
                                    + ItemUtil.getName(new ItemStack(block.getType())) + ChatColor.GOLD + ")");
                            evt.setCancelled(true);
                        } else if (plugin.getEventFactory().getSelectedEffectLocations().containsKey(evt.getPlayer()
                                .getUniqueId())) {
                            Block block = evt.getClickedBlock();
                            Location loc = block.getLocation();
                            Map<UUID, Block> temp = plugin.getEventFactory().getSelectedEffectLocations();
                            temp.put(evt.getPlayer().getUniqueId(), block);
                            plugin.getEventFactory().setSelectedEffectLocations(temp);
                            evt.getPlayer().sendMessage(ChatColor.GOLD + Lang.get(player, "questSelectedLocation") + " "
                                    + ChatColor.AQUA + loc.getWorld().getName() + ": " + loc.getX() + ", " + loc.getY() 
                                    + ", " + loc.getZ() + ChatColor.GOLD + " (" + ChatColor.GREEN 
                                    + ItemUtil.getName(new ItemStack(block.getType())) + ChatColor.GOLD + ")");
                            evt.setCancelled(true);
                        } else if (plugin.getEventFactory().getSelectedMobLocations().containsKey(evt.getPlayer()
                                .getUniqueId())) {
                            Block block = evt.getClickedBlock();
                            Location loc = block.getLocation();
                            Map<UUID, Block> temp = plugin.getEventFactory().getSelectedMobLocations();
                            temp.put(evt.getPlayer().getUniqueId(), block);
                            plugin.getEventFactory().setSelectedMobLocations(temp);
                            evt.getPlayer().sendMessage(ChatColor.GOLD + Lang.get(player, "questSelectedLocation") + " "
                                    + ChatColor.AQUA + loc.getWorld().getName() + ": " + loc.getX() + ", " + loc.getY() 
                                    + ", " + loc.getZ() + ChatColor.GOLD + " (" + ChatColor.GREEN 
                                    + ItemUtil.getName(new ItemStack(block.getType())) + ChatColor.GOLD + ")");
                            evt.setCancelled(true);
                        } else if (plugin.getEventFactory().getSelectedLightningLocations().containsKey(evt.getPlayer()
                                .getUniqueId())) {
                            Block block = evt.getClickedBlock();
                            Location loc = block.getLocation();
                            Map<UUID, Block> temp = plugin.getEventFactory().getSelectedLightningLocations();
                            temp.put(evt.getPlayer().getUniqueId(), block);
                            plugin.getEventFactory().setSelectedLightningLocations(temp);
                            evt.getPlayer().sendMessage(ChatColor.GOLD + Lang.get(player, "questSelectedLocation") + " "
                                    + ChatColor.AQUA + loc.getWorld().getName() + ": " + loc.getX() + ", " + loc.getY() 
                                    + ", " + loc.getZ() + ChatColor.GOLD + " (" + ChatColor.GREEN 
                                    + ItemUtil.getName(new ItemStack(block.getType())) + ChatColor.GOLD + ")");
                            evt.setCancelled(true);
                        } else if (plugin.getEventFactory().getSelectedTeleportLocations().containsKey(evt.getPlayer()
                                .getUniqueId())) {
                            Block block = evt.getClickedBlock();
                            Location loc = block.getLocation();
                            Map<UUID, Block> temp = plugin.getEventFactory().getSelectedTeleportLocations();
                            temp.put(evt.getPlayer().getUniqueId(), block);
                            plugin.getEventFactory().setSelectedTeleportLocations(temp);
                            evt.getPlayer().sendMessage(ChatColor.GOLD + Lang.get(player, "questSelectedLocation") + " "
                                    + ChatColor.AQUA + loc.getWorld().getName() + ": " + loc.getX() + ", " + loc.getY() 
                                    + ", " + loc.getZ() + ChatColor.GOLD + " (" + ChatColor.GREEN 
                                    + ItemUtil.getName(new ItemStack(block.getType())) + ChatColor.GOLD + ")");
                            evt.setCancelled(true);
                        } else if (plugin.getQuestFactory().getSelectedKillLocations().containsKey(evt.getPlayer()
                                .getUniqueId())) {
                            Block block = evt.getClickedBlock();
                            Location loc = block.getLocation();
                            Map<UUID, Block> temp = plugin.getQuestFactory().getSelectedKillLocations();
                            temp.put(evt.getPlayer().getUniqueId(), block);
                            plugin.getQuestFactory().setSelectedKillLocations(temp);
                            evt.getPlayer().sendMessage(ChatColor.GOLD + Lang.get(player, "questSelectedLocation") + " "
                                    + ChatColor.AQUA + loc.getWorld().getName() + ": " + loc.getX() + ", " + loc.getY() 
                                    + ", " + loc.getZ() + ChatColor.GOLD + " (" + ChatColor.GREEN 
                                    + ItemUtil.getName(new ItemStack(block.getType())) + ChatColor.GOLD + ")");
                            evt.setCancelled(true);
                        } else if (plugin.getQuestFactory().getSelectedReachLocations().containsKey(evt.getPlayer()
                                .getUniqueId())) {
                            Block block = evt.getClickedBlock();
                            Location loc = block.getLocation();
                            Map<UUID, Block> temp = plugin.getQuestFactory().getSelectedReachLocations();
                            temp.put(evt.getPlayer().getUniqueId(), block);
                            plugin.getQuestFactory().setSelectedReachLocations(temp);
                            evt.getPlayer().sendMessage(ChatColor.GOLD + Lang.get(player, "questSelectedLocation") + " "
                            + ChatColor.AQUA + loc.getWorld().getName() + ": " + loc.getX() + ", " + loc.getY() + ", " 
                                    + loc.getZ() + ChatColor.GOLD + " (" + ChatColor.GREEN 
                                    + ItemUtil.getName(new ItemStack(block.getType())) + ChatColor.GOLD + ")");
                            evt.setCancelled(true);
                        } else if (player.isConversing() == false) {
                            for (final Quest q : plugin.getQuests()) {
                                if (q.getBlockStart() != null) {
                                    if (q.getBlockStart().equals(evt.getClickedBlock().getLocation())) {
                                        if (quester.getCurrentQuests().size() >= plugin.getSettings().getMaxQuests() 
                                                && plugin.getSettings().getMaxQuests() > 0) {
                                            String msg = Lang.get(player, "questMaxAllowed");
                                            msg = msg.replace("<number>", String.valueOf(plugin.getSettings().getMaxQuests()));
                                            player.sendMessage(ChatColor.YELLOW + msg);
                                        } else {
                                            if (quester.getCompletedQuests().contains(q.getName())) {
                                                if (q.getPlanner().getCooldown() > -1 
                                                        && (quester.getCooldownDifference(q)) > 0) {
                                                    String early = Lang.get(player, "questTooEarly");
                                                    early = early.replace("<quest>", ChatColor.AQUA + q.getName() 
                                                            + ChatColor.YELLOW);
                                                    early = early.replace("<time>", ChatColor.DARK_PURPLE 
                                                            + Quests.getTime(quester.getCooldownDifference(q)) 
                                                            + ChatColor.YELLOW);
                                                    player.sendMessage(ChatColor.YELLOW + early);
                                                    return;
                                                } else if (quester.getCompletedQuests().contains(q.getName()) 
                                                        && q.getPlanner().getCooldown() < 0) {
                                                    String completed = Lang.get(player, "questAlreadyCompleted");
                                                    completed = completed.replace("<quest>", ChatColor.AQUA 
                                                            + q.getName() + ChatColor.YELLOW);
                                                    player.sendMessage(ChatColor.YELLOW + completed);
                                                    return;
                                                }
                                            }
                                            quester.setQuestToTake(q.getName());
                                            String s = ChatColor.GOLD + "- " + ChatColor.DARK_PURPLE 
                                                    + quester.getQuestToTake() + ChatColor.GOLD + " -\n" + "\n" 
                                                    + ChatColor.RESET + plugin.getQuest(quester.getQuestToTake())
                                                    .getDescription() + "\n";
                                            for (String msg : s.split("<br>")) {
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
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent evt) {
        if (evt.getRightClicked().getType() == EntityType.ITEM_FRAME) {
            if (ItemUtil.isJournal(evt.getPlayer().getItemInHand())) {
                evt.setCancelled(true);
                evt.getPlayer().sendMessage(ChatColor.RED + Lang.get(evt.getPlayer(), "journalDenied"));
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent evt) {
        if (plugin.canUseQuests(evt.getPlayer().getUniqueId())) {
            final Quester quester = plugin.getQuester(evt.getPlayer().getUniqueId());
            for (final Quest quest : plugin.getQuests()) {
                if (quester.getCurrentQuests().containsKey(quest)) {
                    final Stage currentStage = quester.getCurrentStage(quest);
                    if (currentStage == null) {
                        plugin.getLogger().severe("currentStage was null for " + quester.getUUID().toString() 
                                + " on chat for quest " + quest.getName());
                        continue;
                    }
                    if (currentStage.getChatActions().isEmpty() == false) {
                        String chat = evt.getMessage();
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
                    if (quester.containsObjective(quest, "password")) {
                        quester.sayPassword(quest, evt);
                    }
                }
                
                quester.dispatchMultiplayerEverything(quest, "password", (Quester q) -> {
                    q.sayPassword(quest, evt);
                    return null;
                });
            }
        }
    }
    
    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent evt) {
        if (plugin.canUseQuests(evt.getPlayer().getUniqueId())) {
            Quester quester = plugin.getQuester(evt.getPlayer().getUniqueId());
            if (quester.getCurrentQuests().isEmpty() == false) {
                for (Quest quest : quester.getCurrentQuests().keySet()) {
                    if (!quest.getOptions().getAllowCommands()) {
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
                    Stage currentStage = quester.getCurrentStage(quest);
                    if (currentStage == null) {
                        plugin.getLogger().severe("currentStage was null for " + quester.getUUID().toString() 
                               + " on command for quest " + quest.getName());
                        continue;
                    }
                    if (currentStage.getCommandActions().isEmpty() == false) {
                        String command = evt.getMessage();
                        for (String s : currentStage.getCommandActions().keySet()) {
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

    @SuppressWarnings("deprecation") // since 1.13
    @EventHandler
    public void onBlockDamage(BlockDamageEvent evt) {
        if (plugin.checkQuester(evt.getPlayer().getUniqueId()) == false) {
            final ItemStack blockItemStack = new ItemStack(evt.getBlock().getType(), 1, evt.getBlock().getState()
                    .getData().toItemStack().getDurability());
            Quester quester = plugin.getQuester(evt.getPlayer().getUniqueId());
            for (Quest quest : plugin.getQuests()) {
                if (quester.getCurrentQuests().containsKey(quest) && quester.containsObjective(quest, "damageBlock")) {
                    quester.damageBlock(quest, blockItemStack);
                }
                
                quester.dispatchMultiplayerEverything(quest, "placeBlock", (Quester q) -> {
                    q.placeBlock(quest, blockItemStack);
                    return null;
                });
            }
        }
    }

    @SuppressWarnings("deprecation") // since 1.13
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent evt) {
        if (plugin.checkQuester(evt.getPlayer().getUniqueId()) == false) {
            final ItemStack blockItemStack = new ItemStack(evt.getBlock().getType(), 1, evt.getBlock().getState()
                    .getData().toItemStack().getDurability());
            Quester quester = plugin.getQuester(evt.getPlayer().getUniqueId());
            for (Quest quest : plugin.getQuests()) {
                if (evt.isCancelled() == false) {
                    if (quester.getCurrentQuests().containsKey(quest) 
                            && quester.containsObjective(quest, "placeBlock")) {
                        quester.placeBlock(quest, blockItemStack);
                    }
                    
                    quester.dispatchMultiplayerEverything(quest, "placeBlock", (Quester q) -> {
                        q.placeBlock(quest, blockItemStack);
                        return null;
                    });
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent evt) {
        if (plugin.checkQuester(evt.getPlayer().getUniqueId()) == false) {
            final ItemStack blockItemStack = new ItemStack(evt.getBlock().getType(), 1, evt.getBlock().getState()
                    .getData().toItemStack().getDurability());
            Quester quester = plugin.getQuester(evt.getPlayer().getUniqueId());
            for (Quest quest : plugin.getQuests()) {
                if (evt.isCancelled() == false) {
                    if (quester.getCurrentQuests().containsKey(quest) 
                            && quester.containsObjective(quest, "breakBlock")) {
                        if (!evt.getPlayer().getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH)) {
                            quester.breakBlock(quest, blockItemStack);
                        }
                    }
                    quester.dispatchMultiplayerEverything(quest, "breakBlock", (Quester q) -> {
                        if (!evt.getPlayer().getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH)) {
                            q.breakBlock(quest, blockItemStack);
                        }
                        return null;
                    });
                    if (quester.getCurrentQuests().containsKey(quest)
                            && quester.containsObjective(quest, "placeBlock")) {
                        for (ItemStack is : quester.getQuestData(quest).blocksPlaced) {
                            if (evt.getBlock().getType().equals(is.getType()) && is.getAmount() > 0) {
                                int index = quester.getQuestData(quest).blocksPlaced.indexOf(is);
                                is.setAmount(is.getAmount() - 1);
                                quester.getQuestData(quest).blocksPlaced.set(index, is);
                            }
                        }
                    }
                    quester.dispatchMultiplayerEverything(quest, "placeBlock", (Quester q) -> {
                        for (ItemStack is : q.getQuestData(quest).blocksPlaced) {
                            if (evt.getBlock().getType().equals(is.getType()) && is.getAmount() > 0) {
                                int index = q.getQuestData(quest).blocksPlaced.indexOf(is);
                                is.setAmount(is.getAmount() - 1);
                                q.getQuestData(quest).blocksPlaced.set(index, is);
                            }
                        }
                        return null;
                    });
                    if (quester.getCurrentQuests().containsKey(quest) && quester.containsObjective(quest, "cutBlock")) {
                        if (evt.getPlayer().getItemInHand().getType().equals(Material.SHEARS)) {
                            quester.cutBlock(quest, blockItemStack);
                        }
                    }
                    quester.dispatchMultiplayerEverything(quest, "cutBlock", (Quester q) -> {
                        if (evt.getPlayer().getItemInHand().getType().equals(Material.SHEARS)) {
                            q.cutBlock(quest, blockItemStack);
                        }
                        return null;
                    });
                }
            }
        }
    }

    @EventHandler
    public void onPlayerShearEntity(PlayerShearEntityEvent evt) {
        if (evt.getEntity().getType() == EntityType.SHEEP) {
            if (plugin.canUseQuests(evt.getPlayer().getUniqueId())) {
                Sheep sheep = (Sheep) evt.getEntity();
                Quester quester = plugin.getQuester(evt.getPlayer().getUniqueId());
                for (Quest quest : plugin.getQuests()) {
                    if (quester.getCurrentQuests().containsKey(quest) 
                            && quester.containsObjective(quest, "shearSheep")) {
                        quester.shearSheep(quest, sheep.getColor());
                    }
                    
                    quester.dispatchMultiplayerEverything(quest, "shearSheep", (Quester q) -> {
                        q.shearSheep(quest, sheep.getColor());
                        return null;
                    });
                }
            }
        }
    }

    @EventHandler
    public void onEntityTame(EntityTameEvent evt) {
        if (evt.getOwner() instanceof Player) {
            Player p = (Player) evt.getOwner();
            if (plugin.canUseQuests(p.getUniqueId())) {
                Quester quester = plugin.getQuester(p.getUniqueId());
                for (Quest quest : plugin.getQuests()) {
                    if (quester.getCurrentQuests().containsKey(quest) && quester.containsObjective(quest, "tameMob")) {
                        quester.tameMob(quest, evt.getEntityType());
                    }
                    
                    quester.dispatchMultiplayerEverything(quest, "tameMob", (Quester q) -> {
                        q.tameMob(quest, evt.getEntityType());
                        return null;
                    });
                }
            }
        }
    }
    

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onCraftItem(CraftItemEvent evt) {
        if (evt.getWhoClicked() instanceof Player) {
            if (plugin.checkQuester(evt.getWhoClicked().getUniqueId()) == false) {
                final ItemStack craftedItem = getCraftedItem(evt);
                Quester quester = plugin.getQuester(evt.getWhoClicked().getUniqueId());
                for (Quest quest : plugin.getQuests()) {
                    if (quester.getCurrentQuests().containsKey(quest) 
                            && quester.containsObjective(quest, "craftItem")) {
                        quester.craftItem(quest, craftedItem);
                    }
                    
                    quester.dispatchMultiplayerEverything(quest, "craftItem", (Quester q) -> {
                        q.craftItem(quest, craftedItem);
                        return null;
                    });
                }
            }
        }
    }
    
    @SuppressWarnings("deprecation")
    private ItemStack getCraftedItem(CraftItemEvent evt) {
        if (evt.isShiftClick()) {
            ItemStack result = evt.getRecipe().getResult();
            int numberOfItems = result.getAmount();
            int itemsChecked = 0;
            
            for (ItemStack item : evt.getInventory().getMatrix()) {
                if (item != null && !item.getType().equals(Material.AIR)) {
                    if (itemsChecked == 0) {
                        numberOfItems = item.getAmount();
                    } else {
                        numberOfItems = Math.min(numberOfItems, item.getAmount());
                        itemsChecked++;
                    }
                }
            }
            return new ItemStack(result.getType(), numberOfItems, result.getDurability());
        }
        return evt.getCurrentItem();
    }
    
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent evt) {
        if (evt.getWhoClicked() instanceof Player) {
            if (evt.getInventory().getType() == InventoryType.FURNACE
                    || evt.getInventory().getType().name().equals("BLAST_FURNACE")
                    || evt.getInventory().getType().name().equals("SMOKER")) {
                if (evt.getSlotType() == SlotType.RESULT) {
                    Quester quester = plugin.getQuester(evt.getWhoClicked().getUniqueId());
                    for (Quest quest : plugin.getQuests()) {
                        if (quester.getCurrentQuests().containsKey(quest) 
                                && quester.containsObjective(quest, "smeltItem")) {
                            quester.smeltItem(quest, evt.getCurrentItem());
                        }
                        
                        quester.dispatchMultiplayerEverything(quest, "smeltItem", (Quester q) -> {
                            q.smeltItem(quest, evt.getCurrentItem());
                            return null;
                        });
                    }
                }
            } else if (evt.getInventory().getType() == InventoryType.BREWING) {
                if (evt.getSlotType() == SlotType.CRAFTING) {
                    Quester quester = plugin.getQuester(evt.getWhoClicked().getUniqueId());
                    for (Quest quest : plugin.getQuests()) {
                        if (quester.getCurrentQuests().containsKey(quest) 
                                && quester.containsObjective(quest, "brewItem")) {
                            quester.brewItem(quest, evt.getCurrentItem());
                        }
                        
                        quester.dispatchMultiplayerEverything(quest, "brewItem", (Quester q) -> {
                            q.brewItem(quest, evt.getCurrentItem());
                            return null;
                        });
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void onEnchantItem(EnchantItemEvent evt) {
        if (plugin.canUseQuests(evt.getEnchanter().getUniqueId())) {
            Quester quester = plugin.getQuester(evt.getEnchanter().getUniqueId());
            for (Quest quest : plugin.getQuests()) {
                if (quester.getCurrentQuests().containsKey(quest) && quester.containsObjective(quest, "enchantItem")) {
                    for (Enchantment e : evt.getEnchantsToAdd().keySet()) {
                        quester.enchantItem(quest, e, evt.getItem().getType());
                    }
                }
                
                quester.dispatchMultiplayerEverything(quest, "enchantItem", (Quester q) -> {
                    for (Enchantment e : evt.getEnchantsToAdd().keySet()) {
                        q.enchantItem(quest, e, evt.getItem().getType());
                    }
                    return null;
                });
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent evt) {
        if (evt.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) evt.getEntity().getLastDamageCause();
            Entity damager = damageEvent.getDamager();
                
            if (damager != null) {
                if (damager instanceof Projectile) {
                    Projectile projectile = (Projectile) damager;
                    if (projectile.getShooter() != null && projectile.getShooter() instanceof Entity) {
                        preKillMob((Entity)projectile.getShooter(), evt.getEntity());
                    }
                } else if (damager instanceof TNTPrimed) {
                    TNTPrimed tnt = (TNTPrimed) damager;
                    Entity source = tnt.getSource();
                    if (source != null && source.isValid()) {
                        preKillMob(source, evt.getEntity());
                    }
                } else if (damager instanceof Wolf) {
                    Wolf wolf = (Wolf) damager;
                    if (wolf.isTamed() && wolf.getOwner() != null) {
                        Quester quester = plugin.getQuester(wolf.getOwner().getUniqueId());
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
    public void preKillMob(Entity damager, Entity target) {
        if (!plugin.canUseQuests(damager.getUniqueId())) {
            return;
        }
        if (damager instanceof Player) {
            Quester quester = plugin.getQuester(damager.getUniqueId());
            if (plugin.getDependencies().getCitizens() != null && CitizensAPI.getNPCRegistry().isNPC(target)) {
                for (Quest quest : plugin.getQuests()) {
                    if (quester.getCurrentQuests().containsKey(quest) && quester.containsObjective(quest, "killNPC")) {
                        quester.killNPC(quest, CitizensAPI.getNPCRegistry().getNPC(target));
                    }
                    
                    quester.dispatchMultiplayerEverything(quest, "killNPC", (Quester q) -> {
                        q.killNPC(quest, CitizensAPI.getNPCRegistry().getNPC(target));
                        return null;
                    });
                }
            } else {
                for (Quest quest : plugin.getQuests()) {
                    if (quester.getCurrentQuests().containsKey(quest) && quester.containsObjective(quest, "killMob")) {
                        quester.killMob(quest, target.getLocation(), target.getType());
                    }
                    
                    quester.dispatchMultiplayerEverything(quest, "killMob", (Quester q) -> {
                        q.killMob(quest, target.getLocation(), target.getType());
                        return null;
                    });
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent evt) {
        if (evt.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) evt.getEntity().getLastDamageCause();
            Entity damager = damageEvent.getDamager();

            if (damager != null) {
                //Ignore suicide
                if (evt.getEntity().getUniqueId().equals(damager.getUniqueId())) {
                    return;
                }
                if (damager instanceof Projectile) {
                    Projectile projectile = (Projectile) damager;
                    if (projectile.getShooter() != null && projectile.getShooter() instanceof Entity) {
                        preKillPlayer((Entity)projectile.getShooter(), evt.getEntity());
                    }
                } else if (damager instanceof TNTPrimed) {
                    TNTPrimed tnt = (TNTPrimed) damager;
                    Entity source = tnt.getSource();
                    if (source != null) {
                        if (source.isValid()) {
                            preKillPlayer(source, evt.getEntity());
                        }
                    }
                } else if (damager instanceof Wolf) {
                    Wolf wolf = (Wolf) damager;
                    if (wolf.isTamed()) {
                        Quester quester = plugin.getQuester(wolf.getOwner().getUniqueId());
                        preKillPlayer(quester.getPlayer(), evt.getEntity());
                    }
                } else {
                    preKillPlayer(damager, evt.getEntity());
                }
            }
        }
            
        Player target = evt.getEntity();
        if (plugin.canUseQuests(target.getUniqueId())) {
            Quester quester = plugin.getQuester(target.getUniqueId());
            for (Quest quest : quester.getCurrentQuests().keySet()) {
                Stage stage = quester.getCurrentStage(quest);
                if (stage != null && stage.getDeathAction() != null) {
                    quester.getCurrentStage(quest).getDeathAction().fire(quester, quest);
                }
            }
        }
        ItemStack found = null;
        for (ItemStack stack : evt.getDrops()) {
            if (ItemUtil.isJournal(stack)) {
                found = stack;
                break;
            }
        }
        if (found != null) {
            Quester quester = plugin.getQuester(target.getUniqueId());
            evt.getDrops().remove(found);
            quester.hasJournal = false;
        }
    }
    
    /**
     * Checks if damager is blacklisted. Ensures damager and target are Player and not NPC.
     * Kills target Player if objective exists
     * 
     * @param damager the attacking entity
     * @param target the entity being attacked
     * @since 3.1.4
     */
    public void preKillPlayer(Entity damager, Entity target) {
        if (damager == null) {
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
            Quester quester = plugin.getQuester(damager.getUniqueId());
            for (Quest quest : plugin.getQuests()) {
                if (quester.getCurrentQuests().containsKey(quest) && quester.containsObjective(quest, "killPlayer")) {
                    quester.killPlayer(quest, (Player)target);
                }
                
                quester.dispatchMultiplayerEverything(quest, "killPlayer", (Quester q) -> {
                    q.killPlayer(quest, (Player)target);
                    return null;
                });
            }
        }
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent evt) {
        Player player = evt.getPlayer();
        if (plugin.canUseQuests(player.getUniqueId())) {
            Quester quester = plugin.getQuester(player.getUniqueId());
            for (Quest quest : plugin.getQuests()) {
                if (quester.getCurrentQuests().containsKey(quest) && quester.containsObjective(quest, "catchFish") 
                        && evt.getState().equals(State.CAUGHT_FISH)) {
                    quester.catchFish(quest);
                }
                
                quester.dispatchMultiplayerEverything(quest, "catchFish", (Quester q) -> {
                    q.catchFish(quest);
                    return null;
                });
            }
        }
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (plugin.canUseQuests(player.getUniqueId())) {
            Quester quester = plugin.getQuester(player.getUniqueId());
            quester.findCompassTarget();
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
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
    public void onPlayerJoin(PlayerJoinEvent evt) {
        if (plugin.canUseQuests(evt.getPlayer().getUniqueId())) {
            Quester quester = new Quester(plugin);
            quester.setUUID(evt.getPlayer().getUniqueId());
            if (new File(plugin.getDataFolder(), "data" + File.separator + quester.getUUID() + ".yml").exists()) {
                quester.loadData();
            } else if (plugin.getSettings().canGenFilesOnJoin()) {
                quester.saveData();
            }
            LinkedList<Quester> temp = plugin.getQuesters();
            temp.add(quester);
            plugin.setQuesters(temp);
            if (plugin.getSettings().canUseCompass()) {
                quester.resetCompass();
            }
            for (String s : quester.getCompletedQuests()) {
                Quest q = plugin.getQuest(s);
                if (q != null) {
                    if (quester.getCompletedTimes().containsKey(q.getName()) == false 
                            && q.getPlanner().getCooldown() > -1) {
                        quester.getCompletedTimes().put(q.getName(), System.currentTimeMillis());
                    }
                }
            }
            for (Quest quest : quester.getCurrentQuests().keySet()) {
                quester.checkQuest(quest);
            }
            for (Quest quest : quester.getCurrentQuests().keySet()) {
                if (quester.getCurrentStage(quest).getDelay() > -1 
                        && quester.getQuestData(quest).isDelayOver() == false) {
                    quester.startStageTimer(quest);
                }
            }
            if (quester.hasJournal)
                quester.updateJournal();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent evt) {
        if (plugin.canUseQuests(evt.getPlayer().getUniqueId())) {
            Quester quester = plugin.getQuester(evt.getPlayer().getUniqueId());
            for (Quest quest : quester.getCurrentQuests().keySet()) {
                Stage currentStage = quester.getCurrentStage(quest);
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
            for (Integer timerId : quester.getTimers().keySet()) {
                plugin.getServer().getScheduler().cancelTask(timerId);
                if (quester.getTimers().containsKey(timerId)) {
                    quester.getTimers().get(timerId).failQuest(quester);
                    quester.removeTimer(timerId);
                }
            }

            if (quester.hasData()) {
                quester.saveData();
            }
            if (plugin.getQuestFactory().getSelectingNpcs().contains(evt.getPlayer())) {
                HashSet<Player> temp = plugin.getQuestFactory().getSelectingNpcs();
                temp.remove(evt.getPlayer());
                plugin.getQuestFactory().setSelectingNpcs(temp);
            }
            LinkedList<Quester> temp = plugin.getQuesters();
            for (Iterator<Quester> iterator = temp.iterator(); iterator.hasNext();) {
                Quester q = iterator.next();
                if (q.getUUID().equals(quester.getUUID())) {
                    iterator.remove();
                }
            }
            plugin.setQuesters(temp);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent evt) {
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
     * Checks if uuid is blacklisted. Updates reach-location objectives
     * 
     * @param uuid The UUID of the Player
     * @param location The current location of the Player
     * @since 3.8.2
     */
    public void playerMove(UUID uuid, Location location) {
        if (plugin.getQuester(uuid) != null) {
            if (plugin.canUseQuests(uuid)) {
                Quester quester = plugin.getQuester(uuid);
                for (Quest quest : plugin.getQuests()) {
                    if (quester.getCurrentQuests().containsKey(quest) 
                            && quester.containsObjective(quest, "reachLocation")) {
                        quester.reachLocation(quest, location);
                    }
                    
                    quester.dispatchMultiplayerEverything(quest, "reachLocation", (Quester q) -> {
                        q.reachLocation(quest, location);
                        return null;
                    });
                }
            }
        }
    }
}
