/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.listeners;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.enums.ObjectiveType;
import me.pikamug.quests.player.BukkitQuester;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.BukkitQuest;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.Stage;
import me.pikamug.quests.util.BukkitItemUtil;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.quests.util.BukkitMiscUtil;
import me.pikamug.quests.util.BukkitUpdateChecker;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
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
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class BukkitPlayerListener implements Listener {

    private final BukkitQuestsPlugin plugin;

    public BukkitPlayerListener(final BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClickEvent(final InventoryClickEvent event) {
        final InventoryAction ac = event.getAction();
        if (ac.equals(InventoryAction.NOTHING)) {
            return;
        }
        if (BukkitItemUtil.isItem(event.getCurrentItem()) && BukkitItemUtil.isJournal(event.getCurrentItem())) {
            if (ac.equals(InventoryAction.MOVE_TO_OTHER_INVENTORY) || ac.equals(InventoryAction.DROP_ALL_SLOT)
                    || ac.equals(InventoryAction.DROP_ONE_SLOT)) {
                event.setCancelled(true);
                return;
            }
        } else if (BukkitItemUtil.isItem(event.getCurrentItem()) && BukkitItemUtil.isJournal(event.getCursor())) {
            if (ac.equals(InventoryAction.MOVE_TO_OTHER_INVENTORY) || ac.equals(InventoryAction.DROP_ALL_CURSOR) 
                    || ac.equals(InventoryAction.DROP_ONE_CURSOR)) {
                event.setCancelled(true);
                return;
            }
        } else if (ac.equals(InventoryAction.SWAP_WITH_CURSOR) || ac.equals(InventoryAction.HOTBAR_SWAP)
                || ac.equals(InventoryAction.HOTBAR_MOVE_AND_READD)) {
            if (event.getHotbarButton() > -1) {
                final ItemStack item = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
                if (BukkitItemUtil.isItem(item) && BukkitItemUtil.isJournal(item)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        if (BukkitItemUtil.isItem(event.getCurrentItem()) && BukkitItemUtil.isJournal(event.getCurrentItem())
                || BukkitItemUtil.isItem(event.getCursor()) && BukkitItemUtil.isJournal(event.getCursor())) {
            int upper = event.getView().getTopInventory().getSize();
            if (event.getView().getTopInventory().getType().equals(InventoryType.CRAFTING))
                upper += 4;
            final int lower = event.getView().getBottomInventory().getSize();
            final int relative = event.getRawSlot() - upper;
            if (relative < 0 || relative >= (lower)) {
                event.setCancelled(true);
                return;
            }
        }
        final BukkitQuester quester = plugin.getQuester(event.getWhoClicked().getUniqueId());
        final Player player = (Player) event.getWhoClicked();
        if (event.getView().getTitle().contains(BukkitLang.get(player, "quests"))) {
            final ItemStack clicked = event.getCurrentItem();
            if (BukkitItemUtil.isItem(clicked)) {
                event.setCancelled(true);
                for (final Quest quest : plugin.getLoadedQuests()) {
                    final BukkitQuest bukkitQuest = (BukkitQuest)quest;
                    if (bukkitQuest.getGUIDisplay() != null) {
                        final int i = BukkitItemUtil.compareItems(clicked, bukkitQuest.prepareDisplay(quester), false);
                        if (i == 0 || i == -7) {
                            if (quester.canAcceptOffer(quest, true)) {
                                try { 
                                    quester.takeQuest(quest, false);
                                } catch (final Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                                    event.getWhoClicked().closeInventory(), 1L);
                            return;
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDragEvent(final InventoryDragEvent event) {
        if (BukkitItemUtil.isItem(event.getOldCursor()) && BukkitItemUtil.isJournal(event.getOldCursor())
                || BukkitItemUtil.isItem(event.getCursor()) && BukkitItemUtil.isJournal(event.getCursor())) {
            int upper = event.getView().getTopInventory().getSize();
            if (event.getView().getTopInventory().getType().equals(InventoryType.CRAFTING))
                upper += 4;
            final int lower = event.getView().getBottomInventory().getSize();
            for (int relative : event.getRawSlots()) {
                relative -= upper;
                if (relative < 0 || relative >= (lower)) {
                    event.setCancelled(true);
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(final PlayerDropItemEvent event) {
        if (BukkitItemUtil.isJournal(event.getItemDrop().getItemStack())) {
            if (!event.getPlayer().hasPermission("quests.admin.drop")) {
                event.setCancelled(true);
            }
        }
    }

    @SuppressWarnings("deprecation") // since 1.13
    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent event) {
        EquipmentSlot e = null;
        try {
            e = event.getHand();
        } catch (final NoSuchMethodError err) {
            // Do nothing, getHand() not present pre-1.9
        }
        if (e == null || e.equals(EquipmentSlot.HAND)) { // If the event is fired by HAND (main hand)
            if (BukkitItemUtil.isJournal(event.getPlayer().getItemInHand())) {
                final Player player = event.getPlayer();
                if (event.hasBlock()) {
                    if (event.getClickedBlock() == null) {
                        return;
                    }
                    if (event.getClickedBlock().getType().name().equals("LECTERN")
                            || event.getClickedBlock().getType().name().equals("CHISELED_BOOKSHELF")) {
                        event.setCancelled(true);
                        BukkitLang.send(player, ChatColor.RED + BukkitLang.get(event.getPlayer(), "journalDenied")
                                .replace("<journal>", BukkitLang.get(event.getPlayer(), "journalTitle")));
                        return;
                    }
                    if (plugin.getConfigSettings().canAllowPranks()
                            && event.getClickedBlock().getType().name().contains("PORTAL")) {
                        event.setCancelled(true);
                        BukkitLang.send(player, " " + ChatColor.AQUA + ChatColor.UNDERLINE
                                + "https://www.youtube.com/watch?v=dQw4w9WgXcQ");
                        return;
                    }
                }
                if (plugin.getConfigSettings().canAllowPranks()
                        && event.getPlayer().getInventory().getHelmet() != null
                        && (event.getPlayer().getInventory().getHelmet().getType().name().equals("PUMPKIN")
                        || event.getPlayer().getInventory().getHelmet().getType().name().equals("CARVED_PUMPKIN"))) {
                        if (!event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
                            BukkitLang.send(player, " " + ChatColor.AQUA + ChatColor.UNDERLINE
                                + "https://www.youtube.com/watch?v=nJROKaZJgbI");
                        }
                        event.setCancelled(true);
                        return;
                }
            }
            if (plugin.canUseQuests(event.getPlayer().getUniqueId())) {
                final Quester quester = plugin.getQuester(event.getPlayer().getUniqueId());
                final Player player = event.getPlayer();
                if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                    boolean hasObjective = false;
                    if (!event.isCancelled()) {
                        for (final Quest quest : plugin.getLoadedQuests()) {
                            if (quester.getCurrentQuests().containsKey(quest)
                                    && quester.getCurrentStage(quest).containsObjective(ObjectiveType.USE_BLOCK)) {
                                hasObjective = true;
                            }
                        }
                    }
                    if (!hasObjective) {
                        if (plugin.getQuestFactory().getSelectedBlockStarts().containsKey(event.getPlayer()
                                .getUniqueId())) {
                            final Block block = event.getClickedBlock();
                            if (block == null) {
                                return;
                            }
                            final Location loc = block.getLocation();
                            final ConcurrentHashMap<UUID, Block> temp
                                    = plugin.getQuestFactory().getSelectedBlockStarts();
                            temp.put(player.getUniqueId(), block);
                            plugin.getQuestFactory().setSelectedBlockStarts(temp);
                            if (loc.getWorld() != null) {
                                BukkitLang.send(player, ChatColor.GOLD + BukkitLang.get(player, "questSelectedLocation")
                                        + " " + ChatColor.AQUA + loc.getWorld().getName() + ": " + loc.getX() + ", "
                                        + loc.getY() + ", " + loc.getZ() + ChatColor.GOLD + " (" + ChatColor.GREEN
                                        + BukkitItemUtil.getName(new ItemStack(block.getType())) + ChatColor.GOLD + ")");
                            }
                            event.setCancelled(true);
                        } else if (plugin.getActionFactory().getSelectedExplosionLocations().containsKey(event.getPlayer()
                                .getUniqueId())) {
                            final Block block = event.getClickedBlock();
                            if (block == null) {
                                return;
                            }
                            final Location loc = block.getLocation();
                            final ConcurrentHashMap<UUID, Block> temp
                                    = plugin.getActionFactory().getSelectedExplosionLocations();
                            temp.put(player.getUniqueId(), block);
                            plugin.getActionFactory().setSelectedExplosionLocations(temp);
                            if (loc.getWorld() != null) {
                                BukkitLang.send(player, ChatColor.GOLD + BukkitLang.get(player, "questSelectedLocation")
                                        + " " + ChatColor.AQUA + loc.getWorld().getName() + ": " + loc.getX() + ", "
                                        + loc.getY() + ", " + loc.getZ() + ChatColor.GOLD + " (" + ChatColor.GREEN
                                        + BukkitItemUtil.getName(new ItemStack(block.getType())) + ChatColor.GOLD + ")");
                            }
                            event.setCancelled(true);
                        } else if (plugin.getActionFactory().getSelectedEffectLocations().containsKey(event.getPlayer()
                                .getUniqueId())) {
                            final Block block = event.getClickedBlock();
                            if (block == null) {
                                return;
                            }
                            final Location loc = block.getLocation();
                            final ConcurrentHashMap<UUID, Block> temp
                                    = plugin.getActionFactory().getSelectedEffectLocations();
                            temp.put(player.getUniqueId(), block);
                            plugin.getActionFactory().setSelectedEffectLocations(temp);
                            if (loc.getWorld() != null) {
                                BukkitLang.send(player, ChatColor.GOLD + BukkitLang.get(player, "questSelectedLocation")
                                        + " " + ChatColor.AQUA + loc.getWorld().getName() + ": " + loc.getX() + ", "
                                        + loc.getY() + ", " + loc.getZ() + ChatColor.GOLD + " (" + ChatColor.GREEN
                                        + BukkitItemUtil.getName(new ItemStack(block.getType())) + ChatColor.GOLD + ")");
                            }
                            event.setCancelled(true);
                        } else if (plugin.getActionFactory().getSelectedMobLocations().containsKey(event.getPlayer()
                                .getUniqueId())) {
                            final Block block = event.getClickedBlock();
                            if (block == null) {
                                return;
                            }
                            final Location loc = block.getLocation();
                            final ConcurrentHashMap<UUID, Block> temp
                                    = plugin.getActionFactory().getSelectedMobLocations();
                            temp.put(player.getUniqueId(), block);
                            plugin.getActionFactory().setSelectedMobLocations(temp);
                            if (loc.getWorld() != null) {
                                BukkitLang.send(player, ChatColor.GOLD + BukkitLang.get(player, "questSelectedLocation")
                                        + " " + ChatColor.AQUA + loc.getWorld().getName() + ": " + loc.getX() + ", "
                                        + loc.getY() + ", " + loc.getZ() + ChatColor.GOLD + " (" + ChatColor.GREEN
                                        + BukkitItemUtil.getName(new ItemStack(block.getType())) + ChatColor.GOLD + ")");
                            }
                            event.setCancelled(true);
                        } else if (plugin.getActionFactory().getSelectedLightningLocations().containsKey(event.getPlayer()
                                .getUniqueId())) {
                            final Block block = event.getClickedBlock();
                            if (block == null) {
                                return;
                            }
                            final Location loc = block.getLocation();
                            final ConcurrentHashMap<UUID, Block> temp
                                    = plugin.getActionFactory().getSelectedLightningLocations();
                            temp.put(player.getUniqueId(), block);
                            plugin.getActionFactory().setSelectedLightningLocations(temp);
                            if (loc.getWorld() != null) {
                                BukkitLang.send(player, ChatColor.GOLD + BukkitLang.get(player, "questSelectedLocation")
                                        + " " + ChatColor.AQUA + loc.getWorld().getName() + ": " + loc.getX() + ", "
                                        + loc.getY() + ", " + loc.getZ() + ChatColor.GOLD + " (" + ChatColor.GREEN
                                        + BukkitItemUtil.getName(new ItemStack(block.getType())) + ChatColor.GOLD + ")");
                            }
                            event.setCancelled(true);
                        } else if (plugin.getActionFactory().getSelectedTeleportLocations().containsKey(event.getPlayer()
                                .getUniqueId())) {
                            final Block block = event.getClickedBlock();
                            if (block == null) {
                                return;
                            }
                            final Location loc = block.getLocation();
                            final ConcurrentHashMap<UUID, Block> temp
                                    = plugin.getActionFactory().getSelectedTeleportLocations();
                            temp.put(player.getUniqueId(), block);
                            plugin.getActionFactory().setSelectedTeleportLocations(temp);
                            if (loc.getWorld() != null) {
                                BukkitLang.send(player, ChatColor.GOLD + BukkitLang.get(player, "questSelectedLocation")
                                        + " " + ChatColor.AQUA + loc.getWorld().getName() + ": " + loc.getX() + ", "
                                        + loc.getY() + ", " + loc.getZ() + ChatColor.GOLD + " (" + ChatColor.GREEN
                                        + BukkitItemUtil.getName(new ItemStack(block.getType())) + ChatColor.GOLD + ")");
                            }
                            event.setCancelled(true);
                        } else if (plugin.getQuestFactory().getSelectedKillLocations().containsKey(event.getPlayer()
                                .getUniqueId())) {
                            final Block block = event.getClickedBlock();
                            if (block == null) {
                                return;
                            }
                            final Location loc = block.getLocation();
                            final ConcurrentHashMap<UUID, Block> temp
                                    = plugin.getQuestFactory().getSelectedKillLocations();
                            temp.put(player.getUniqueId(), block);
                            plugin.getQuestFactory().setSelectedKillLocations(temp);
                            if (loc.getWorld() != null) {
                                BukkitLang.send(player, ChatColor.GOLD + BukkitLang.get(player, "questSelectedLocation")
                                        + " " + ChatColor.AQUA + loc.getWorld().getName() + ": " + loc.getX() + ", "
                                        + loc.getY() + ", " + loc.getZ() + ChatColor.GOLD + " (" + ChatColor.GREEN
                                        + BukkitItemUtil.getName(new ItemStack(block.getType())) + ChatColor.GOLD + ")");
                            }
                            event.setCancelled(true);
                        } else if (plugin.getQuestFactory().getSelectedReachLocations().containsKey(event.getPlayer()
                                .getUniqueId())) {
                            final Block block = event.getClickedBlock();
                            if (block == null) {
                                return;
                            }
                            final Location loc = block.getLocation();
                            final ConcurrentHashMap<UUID, Block> temp
                                    = plugin.getQuestFactory().getSelectedReachLocations();
                            temp.put(player.getUniqueId(), block);
                            plugin.getQuestFactory().setSelectedReachLocations(temp);
                            if (loc.getWorld() != null) {
                                BukkitLang.send(player, ChatColor.GOLD + BukkitLang.get(player, "questSelectedLocation")
                                        + " " + ChatColor.AQUA + loc.getWorld().getName() + ": " + loc.getX() + ", "
                                        + loc.getY() + ", " + loc.getZ() + ChatColor.GOLD + " (" + ChatColor.GREEN
                                        + BukkitItemUtil.getName(new ItemStack(block.getType())) + ChatColor.GOLD + ")");
                            }
                            event.setCancelled(true);
                        } else if (!player.isConversing()) {
                            for (final Quest quest : plugin.getLoadedQuests()) {
                                final BukkitQuest bukkitQuest = (BukkitQuest) quest;
                                if (bukkitQuest.getBlockStart() != null && event.getClickedBlock() != null) {
                                    if (bukkitQuest.getBlockStart().equals(event.getClickedBlock().getLocation())) {
                                        if (quester.getCurrentQuests().size() >= plugin.getConfigSettings().getMaxQuests()
                                                && plugin.getConfigSettings().getMaxQuests() > 0) {
                                            String msg = BukkitLang.get(player, "questMaxAllowed");
                                            msg = msg.replace("<number>", String
                                                    .valueOf(plugin.getConfigSettings().getMaxQuests()));
                                            BukkitLang.send(player, ChatColor.YELLOW + msg);
                                        } else {
                                            if (quester.getCompletedQuests().contains(bukkitQuest)) {
                                                if (bukkitQuest.getPlanner().getCooldown() > -1
                                                        && (quester.getRemainingCooldown(bukkitQuest)) > 0) {
                                                    String early = BukkitLang.get(player, "questTooEarly");
                                                    early = early.replace("<quest>", bukkitQuest.getName());
                                                    early = early.replace("<time>", BukkitMiscUtil.getTime(
                                                            quester.getRemainingCooldown(bukkitQuest)));
                                                    BukkitLang.send(player, ChatColor.YELLOW + early);
                                                    continue;
                                                } else if (quester.getCompletedQuests().contains(bukkitQuest)
                                                        && bukkitQuest.getPlanner().getCooldown() < 0) {
                                                    String completed = BukkitLang.get(player, "questAlreadyCompleted");
                                                    completed = completed.replace("<quest>", bukkitQuest.getName());
                                                    BukkitLang.send(player, ChatColor.YELLOW + completed);
                                                    continue;
                                                }
                                            }
                                            for (final Quest currentQuest : quester.getCurrentQuests().keySet()) {
                                                if (currentQuest.getId().equals(bukkitQuest.getId())) {
                                                    BukkitLang.send(player, ChatColor.RED + BukkitLang.get(player,
                                                            "questAlreadyOn"));
                                                    return;
                                                }
                                            }
                                            quester.setQuestIdToTake(bukkitQuest.getId());
                                            if (!plugin.getConfigSettings().canConfirmAccept()) {
                                                quester.takeQuest(bukkitQuest, false);
                                            } else {
                                                final Quest toTake = plugin.getQuestById(quester.getQuestIdToTake());
                                                final String s = ChatColor.GOLD + "- " + ChatColor.DARK_PURPLE
                                                        + toTake.getName() + ChatColor.GOLD + " -\n" + "\n"
                                                        + ChatColor.RESET + toTake.getDescription() + "\n";
                                                for (final String msg : s.split("<br>")) {
                                                    BukkitLang.send(player, msg);
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
                if (event.getItem() != null && event.getItem().getType().equals(Material.COMPASS)) {
                    if (!quester.canUseCompass()) {
                        return;
                    }
                    if (event.getAction().equals(Action.LEFT_CLICK_AIR)
                            || event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                        quester.resetCompass();
                        BukkitLang.send(player, ChatColor.YELLOW + BukkitLang.get(player, "compassReset"));
                    } else if (event.getAction().equals(Action.RIGHT_CLICK_AIR)
                            || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                        quester.findNextCompassTarget(true);
                    }
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerInteractEntity(final PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getType() == EntityType.ITEM_FRAME) {
            final Player player = event.getPlayer();
            if (BukkitItemUtil.isJournal(player.getItemInHand())) {
                event.setCancelled(true);
                BukkitLang.send(player, ChatColor.RED + BukkitLang.get(player, "journalDenied")
                        .replace("<journal>", BukkitLang.get(player, "journalTitle")));
            }
        }
    }
    
    @EventHandler
    public void onPlayerBucketFill(final PlayerBucketFillEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getItemStack() != null && event.getItemStack().getType() == Material.MILK_BUCKET) {
            final Player player = event.getPlayer();
            if (plugin.canUseQuests(player.getUniqueId())) {
                final Quester quester = plugin.getQuester(player.getUniqueId());
                final ObjectiveType type = ObjectiveType.MILK_COW;
                final Set<String> dispatchedQuestIDs = new HashSet<>();
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
    public void onPlayerChat(final AsyncPlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (plugin.canUseQuests(event.getPlayer().getUniqueId())) {
            final Quester quester = plugin.getQuester(event.getPlayer().getUniqueId());
            for (final Quest quest : plugin.getLoadedQuests()) {
                if (!quester.meetsCondition(quest, true)) {
                    continue;
                }
                
                if (quester.getCurrentQuests().containsKey(quest)) {
                    final Stage currentStage = quester.getCurrentStage(quest);
                    if (currentStage == null) {
                        continue;
                    }
                    if (!currentStage.getChatActions().isEmpty()) {
                        final String chat = event.getMessage();
                        for (final String s : currentStage.getChatActions().keySet()) {
                            if (s.equalsIgnoreCase(chat)) {
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        currentStage.getChatActions().get(s).fire(quester, quest);
                                    }

                                }.runTask(this.plugin);
                            }
                        }
                    }
                    final ObjectiveType type = ObjectiveType.PASSWORD;
                    final Set<String> dispatchedQuestIDs = new HashSet<>();
                    if (quester.getCurrentStage(quest).containsObjective(type)) {
                        for (final String pass : quester.getCurrentStage(quest).getPasswordPhrases()) {
                            if (pass.equalsIgnoreCase(event.getMessage())) {
                                event.setCancelled(true);
                                break;
                            }
                        }
                        quester.sayPassword(quest, event);
                    }
                    
                    dispatchedQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, type, 
                            (final Quester q, final Quest cq) -> {
                        if (!dispatchedQuestIDs.contains(cq.getId())) {
                            q.sayPassword(cq, event);
                        }
                        return null;
                    }));
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) {
        if (plugin.canUseQuests(event.getPlayer().getUniqueId())) {
            final Quester quester = plugin.getQuester(event.getPlayer().getUniqueId());
            if (!quester.getCurrentQuests().isEmpty()) {
                for (final Quest quest : quester.getCurrentQuests().keySet()) {
                    if (!quest.getOptions().canAllowCommands()) {
                        if (!event.getMessage().startsWith("/quest")) {
                            final Player player = event.getPlayer();
                            BukkitLang.send(player, ChatColor.RED + BukkitLang.get(player, "optCommandsDenied")
                                    .replace("<quest>", quest.getName()));
                            event.setCancelled(true);
                            plugin.getLogger().info("Player " + player.getName() + " tried to use command "
                                    + event.getMessage() + " but was denied because they are currently on quest "
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
                    if (!currentStage.getCommandActions().isEmpty()) {
                        final String command = event.getMessage().toLowerCase();
                        for (final String s : currentStage.getCommandActions().keySet()) {
                            if (command.startsWith("/" + s.toLowerCase())) {
                                currentStage.getCommandActions().get(s).fire(quester, quest);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerShearEntity(final PlayerShearEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getEntity().getType() == EntityType.SHEEP) {
            final Player player = event.getPlayer();
            if (plugin.canUseQuests(player.getUniqueId())) {
                final Sheep sheep = (Sheep) event.getEntity();
                final Quester quester = plugin.getQuester(player.getUniqueId());
                final ObjectiveType type = ObjectiveType.SHEAR_SHEEP;
                final Set<String> dispatchedQuestIDs = new HashSet<>();
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
    public void onEntityTame(final EntityTameEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getOwner() instanceof Player) {
            final Player player = (Player) event.getOwner();
            if (plugin.canUseQuests(player.getUniqueId())) {
                final Quester quester = plugin.getQuester(player.getUniqueId());
                final ObjectiveType type = ObjectiveType.TAME_MOB;
                final Set<String> dispatchedQuestIDs = new HashSet<>();
                for (final Quest quest : plugin.getLoadedQuests()) {
                    if (!quester.meetsCondition(quest, true)) {
                        continue;
                    }
                    
                    if (quester.getCurrentQuests().containsKey(quest)
                            && quester.getCurrentStage(quest).containsObjective(type)) {
                        quester.tameMob(quest, event.getEntityType());
                    }
                    
                    dispatchedQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, type, 
                            (final Quester q, final Quest cq) -> {
                        if (!dispatchedQuestIDs.contains(cq.getId())) {
                            q.tameMob(cq, event.getEntityType());
                        }
                        return null;
                    }));
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(final EntityDeathEvent event) {
        if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            final EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event.getEntity()
                    .getLastDamageCause();
            final Entity damager = damageEvent.getDamager();

            if (damager instanceof Projectile) {
                final Projectile projectile = (Projectile) damager;
                if (projectile.getShooter() != null && projectile.getShooter() instanceof Entity) {
                    preKillMob((Entity)projectile.getShooter(), event.getEntity());
                }
            } else if (damager instanceof TNTPrimed) {
                final TNTPrimed tnt = (TNTPrimed) damager;
                final Entity source = tnt.getSource();
                if (source != null && source.isValid()) {
                    preKillMob(source, event.getEntity());
                }
            } else if (damager instanceof Wolf) {
                final Wolf wolf = (Wolf) damager;
                if (wolf.isTamed() && wolf.getOwner() != null) {
                    final Quester quester = plugin.getQuester(wolf.getOwner().getUniqueId());
                    if (quester != null) {
                        preKillPlayer(quester.getPlayer(), event.getEntity());
                    }
                }
            } else {
                preKillMob(damager, event.getEntity());
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
            if (plugin.getDependencies().getNpcDependency("Citizens") != null && CitizensAPI.getNPCRegistry().isNPC(target)) {
                return;
            }
            final Quester quester = plugin.getQuester(damager.getUniqueId());
            final ObjectiveType type = ObjectiveType.KILL_MOB;
            for (final Quest quest : plugin.getLoadedQuests()) {
                if (!quester.meetsCondition(quest, true)) {
                    continue;
                }
                if (!quester.getCurrentQuests().containsKey(quest)) {
                    continue;
                }
                
                if (quester.getCurrentStage(quest).containsObjective(type)) {
                    quester.killMob(quest, target.getLocation(), target.getType());
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(final PlayerDeathEvent event) {
        if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            final EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event.getEntity()
                    .getLastDamageCause();
            final Entity damager = damageEvent.getDamager();

            if (event.getEntity().getUniqueId().equals(damager.getUniqueId())) {
                return;
            }
            if (damager instanceof Projectile) {
                final Projectile projectile = (Projectile) damager;
                if (projectile.getShooter() != null && projectile.getShooter() instanceof Entity) {
                    preKillPlayer((Entity)projectile.getShooter(), event.getEntity());
                }
            } else if (damager instanceof TNTPrimed) {
                final TNTPrimed tnt = (TNTPrimed) damager;
                final Entity source = tnt.getSource();
                if (source != null) {
                    if (source.isValid()) {
                        preKillPlayer(source, event.getEntity());
                    }
                }
            } else if (damager instanceof Wolf) {
                final Wolf wolf = (Wolf) damager;
                if (wolf.isTamed() && wolf.getOwner() != null) {
                    final Quester quester = plugin.getQuester(wolf.getOwner().getUniqueId());
                    preKillPlayer(quester.getPlayer(), event.getEntity());
                }
            } else {
                preKillPlayer(damager, event.getEntity());
            }
        }
            
        final Player target = event.getEntity();
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
        for (final ItemStack stack : event.getDrops()) {
            if (BukkitItemUtil.isJournal(stack)) {
                found = stack;
                break;
            }
        }
        if (found != null) {
            event.getDrops().remove(found);
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
            if (plugin.getDependencies().getNpcDependency("Citizens") != null) {
                if (CitizensAPI.getNPCRegistry().isNPC(damager) && CitizensAPI.getNPCRegistry().isNPC(target)) {
                    return;
                }
            }
            final Quester quester = plugin.getQuester(damager.getUniqueId());
            final ObjectiveType type = ObjectiveType.KILL_PLAYER;
            for (final Quest quest : plugin.getLoadedQuests()) {
                if (!quester.meetsCondition(quest, true)) {
                    continue;
                }
                if (!quester.getCurrentQuests().containsKey(quest)) {
                    continue;
                }
                
                if (quester.getCurrentStage(quest).containsObjective(type)) {
                    quester.killPlayer(quest, (Player)target);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerFish(final PlayerFishEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final Player player = event.getPlayer();
        if (plugin.canUseQuests(player.getUniqueId())) {
            final Quester quester = plugin.getQuester(player.getUniqueId());
            final ObjectiveType type = ObjectiveType.CATCH_FISH;
            final Set<String> dispatchedQuestIDs = new HashSet<>();
            for (final Quest quest : plugin.getLoadedQuests()) {
                if (!quester.meetsCondition(quest, true)) {
                    continue;
                }
                
                if (event.getState().equals(State.CAUGHT_FISH)) {
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
                quester.meetsCondition(quest, true);
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(final PlayerRespawnEvent event) {
        final Player player = event.getPlayer();
        if (plugin.canUseQuests(player.getUniqueId())) {
            final Quester quester = plugin.getQuester(player.getUniqueId());
            Bukkit.getScheduler().runTaskLater(plugin, quester::findCompassTarget, 10);
        }
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        if (player.hasPermission("quests.admin.update")) {
            new BukkitUpdateChecker(plugin, 3711).getVersion(version -> {
                if (BukkitUpdateChecker.compareVersions(plugin.getDescription().getVersion().split("-")[0], version)) {
                    event.getPlayer().sendMessage(ChatColor.GRAY + "[" + ChatColor.YELLOW + "Quests" + ChatColor.GRAY
                            + "] " + ChatColor.GREEN + BukkitLang.get(player, "updateTo").replace("<version>",
                            version).replace("<url>", ChatColor.DARK_AQUA + "" + ChatColor.UNDERLINE
                            + plugin.getDescription().getWebsite()));
                }
            });
        }
        if (plugin.canUseQuests(player.getUniqueId())) {
            final Quester noobCheck = new BukkitQuester(plugin, player.getUniqueId());
            if (plugin.getConfigSettings().canGenFilesOnJoin() && !noobCheck.hasData()) {
                noobCheck.saveData();
            }

            plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                final CompletableFuture<Quester> cf = plugin.getStorage().loadQuester(player.getUniqueId());
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
                        if (quester.getCurrentStage(quest).getDelay() > -1) {
                            quester.startStageTimer(quest);
                        }
                    }
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                        if (quester.hasJournal()) {
                            quester.updateJournal();
                        }
                        quester.findCompassTarget();
                    }, 40L);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }, 20L);
        }
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        if (plugin.canUseQuests(event.getPlayer().getUniqueId())) {
            final Quester quester = plugin.getQuester(event.getPlayer().getUniqueId());
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
            if (!plugin.getConfigSettings().canGenFilesOnJoin()) {
                if (quester.hasBaseData()) {
                    quester.saveData();
                }
            } else {
                quester.saveData();
            }
            
            if (plugin.getQuestFactory().getSelectingNpcs().contains(event.getPlayer().getUniqueId())) {
                final ConcurrentSkipListSet<UUID> temp = plugin.getQuestFactory().getSelectingNpcs();
                temp.remove(event.getPlayer().getUniqueId());
                plugin.getQuestFactory().setSelectingNpcs(temp);
            }
            final ConcurrentSkipListSet<Quester> temp = (ConcurrentSkipListSet<Quester>) plugin.getOfflineQuesters();
            temp.removeIf(q -> q.getUUID().equals(quester.getUUID()));
            temp.add(quester);
            plugin.setOfflineQuesters(temp);
        }
    }

    @EventHandler
    public void onPlayerMove(final PlayerMoveEvent event) {
        if (event.getTo() == null) {
            return;
        }
        if (event.getFrom().getBlock().equals(event.getTo().getBlock())) {
            return;
        }
        if (plugin.getDependencies().getNpcDependency("Citizens") != null) {
            if (CitizensAPI.getNPCRegistry().isNPC(event.getPlayer())) {
                return;
            }
        }
        playerMove(event.getPlayer().getUniqueId(), event.getTo());
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
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            final Quester quester = plugin.getQuester(uuid);
            if (quester != null) {
                if (plugin.canUseQuests(uuid)) {
                    final ObjectiveType type = ObjectiveType.REACH_LOCATION;
                    final Set<String> dispatchedQuestIDs = new HashSet<>();
                    for (final Quest quest : plugin.getLoadedQuests()) {
                        if (!quester.meetsCondition(quest, true)) {
                            continue;
                        }

                        if (quester.getCurrentQuests().containsKey(quest)) {
                            if (quester.getCurrentStage(quest) != null
                                    && quester.getCurrentStage(quest).containsObjective(type)) {
                                plugin.getServer().getScheduler().runTask(plugin, () -> quester
                                        .reachLocation(quest, location));
                            }
                        }

                        dispatchedQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, type,
                                (final Quester q, final Quest cq) -> {
                            if (!dispatchedQuestIDs.contains(cq.getId())) {
                                plugin.getServer().getScheduler().runTask(plugin, () -> q
                                        .reachLocation(cq, location));
                            }
                            return null;
                        }));
                    }
                }
            }
        });
    }
}
