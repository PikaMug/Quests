package me.blackvein.quests;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.Conversable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {

    Quests plugin;

    public PlayerListener(Quests newPlugin) {

        plugin = newPlugin;

    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent evt) {

        if (evt.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {

            final Quester quester = plugin.getQuester(evt.getPlayer().getName());
            final Player player = evt.getPlayer();

            if (quester.hasObjective("useBlock")) {

                quester.useBlock(evt.getClickedBlock().getType());

            } else {

                for (final Quest q : plugin.quests) {

                    if (q.blockStart != null) {

                        if (q.blockStart.equals(evt.getClickedBlock().getLocation())) {

                            if (quester.currentQuest != null) {

                                player.sendMessage(ChatColor.YELLOW + "You may only have one active Quest.");

                            } else {

                                if (quester.completedQuests.contains(q.name)) {

                                    if (q.redoDelay < 0 || q.redoDelay > -1 && (quester.getDifference(q)) > 0) {

                                        player.sendMessage(ChatColor.YELLOW + "You may not take " + ChatColor.AQUA + q.name + ChatColor.YELLOW + " again for another " + ChatColor.DARK_PURPLE + Quests.getTime(quester.getDifference(q)) + ChatColor.YELLOW + ".");
                                        return;

                                    }

                                }

                                quester.questToTake = q.name;

                                String s =
                                        ChatColor.GOLD + "- " + ChatColor.DARK_PURPLE + quester.questToTake + ChatColor.GOLD + " -\n"
                                        + "\n"
                                        + ChatColor.RESET + plugin.getQuest(quester.questToTake).description + "\n";

                                player.sendMessage(s);
                                plugin.conversationFactory.buildConversation((Conversable) player).begin();

                            }

                            break;
                        }

                    }

                }

            }

        }

    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent evt) {

        Quester quester = plugin.getQuester(evt.getPlayer().getName());
        if (quester.hasObjective("damageBlock")) {

            quester.damageBlock(evt.getBlock().getType());

        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent evt) {

        if (evt.getPlayer().getName().toLowerCase().contains("_computercraft_") == false && evt.getPlayer().getName().toLowerCase().contains("_buildcraft_") == false && evt.getPlayer().getName().toLowerCase().contains("_redpower_") == false && evt.getPlayer().getName().toLowerCase().contains("_buildcraft_") == false && evt.getPlayer().getName().toLowerCase().contains("(buildcraft)") == false) {

            Quester quester = plugin.getQuester(evt.getPlayer().getName());
            if (quester.hasObjective("placeBlock")) {

                if (evt.isCancelled() == false) {
                    quester.placeBlock(evt.getBlock().getType());
                }

            }

        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent evt) {

        boolean canOpen = true;

        if (canOpen == true) {

            Quester quester = plugin.getQuester(evt.getPlayer().getName());
            if (quester.hasObjective("breakBlock")) {

                if (evt.getPlayer().getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH) == false && evt.isCancelled() == false) {
                    quester.breakBlock(evt.getBlock().getType());
                }

            }

            if (quester.hasObjective("placeBlock")) {

                if (quester.blocksPlaced.containsKey(evt.getBlock().getType())) {

                    if (quester.blocksPlaced.get(evt.getBlock().getType()) > 0) {

                        if (evt.isCancelled() == false) {
                            quester.blocksPlaced.put(evt.getBlock().getType(), quester.blocksPlaced.get(evt.getBlock().getType()) - 1);
                        }

                    }

                }

            }

            if (evt.getPlayer().getItemInHand().getType().equals(Material.SHEARS) && quester.hasObjective("cutBlock")) {

                quester.cutBlock(evt.getBlock().getType());

            }

        }

    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent evt) {

        Quester quester = plugin.getQuester(evt.getPlayer().getName());
        if (quester.hasObjective("collectItem")) {

            quester.collectItem(evt.getItem().getItemStack());

        }

    }

    @EventHandler
    public void onPlayerShearEntity(PlayerShearEntityEvent evt) {

        Quester quester = plugin.getQuester(evt.getPlayer().getName());
        if (evt.getEntity().getType().equals(EntityType.SHEEP) && quester.hasObjective("shearSheep")) {

            Sheep sheep = (Sheep) evt.getEntity();
            quester.shearSheep(sheep.getColor());

        }

    }

    @EventHandler
    public void onEntityTame(EntityTameEvent evt) {

        if (evt.getOwner() instanceof Player) {

            Player p = (Player) evt.getOwner();
            Quester quester = plugin.getQuester(p.getName());
            if (quester.hasObjective("tameMob")) {

                quester.tameMob(evt.getEntityType());

            }

        }

    }

    @EventHandler
    public void onEnchantItem(EnchantItemEvent evt) {

        Quester quester = plugin.getQuester(evt.getEnchanter().getName());
        if (quester.hasObjective("enchantItem")) {

            for (Enchantment e : evt.getEnchantsToAdd().keySet()) {

                quester.enchantItem(e, evt.getItem().getType());

            }

        }

    }

    @EventHandler
    public void onCraftItem(CraftItemEvent evt) {

        if (evt.getWhoClicked() instanceof Player) {

            Player p = (Player) evt.getWhoClicked();
            Quester quester = plugin.getQuester(p.getName());
            if (quester.hasObjective("craftItem")) {

                quester.craftItem(evt.getCurrentItem());

            }

        }

    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent evt){
        
        if(evt.getPlayer() instanceof Player){
            
            Quester quester = plugin.getQuester(((Player)evt.getPlayer()).getName());
            if(quester.holdingQuestItemFromStorage){
                quester.collectItem(evt.getView().getCursor());
            }
            quester.holdingQuestItemFromStorage = false;
            
        }
        
    }
    /*
     *
     * CRAFTING (Player)
     *
     * 0 - Crafted Slot 1 - Top-left Craft Slot 2 - Top-right Craft Slot 3 -
     * Bottom-left Craft Slot 4 - Bottom-right Craft Slot
     *
     * 5 - Head Slot 6 - Body Slot 7 - Leg Slot 8 - Boots Slot
     *
     * 9-35 - Top-left to Bottom-right inventory slots 36-44 - Left to Right
     * hotbar slots
     *
     * -999 - Drop Slot
     *
     *
     * BREWING
     *
     * 0 - Left Potion Slot 1 - Middle Potion Slot 2 - Right Potion Slot 3-
     * Ingredient Slot
     *
     * 4-30 - Top-left to Bottom-right inventory slots 31-39 - Left to Right
     * hotbar slots
     *
     * ENCHANTING
     *
     * 0 - Enchant Slot
     *
     * 1-27 - Top-left to Bottom-right inventory slots 28-36 - Left to Right
     * hotbar slots
     *
     * ENDER CHEST
     *
     * 0-26 - Top-left to Bottom-right chest slots
     *
     * 27-53 - Top-left to Bottom-right inventory slots 54-62 - Left to Right
     * hotbar slots
     *
     * DISPENSER
     *
     * 0-8 - Top-left to Bottom-right dispenser slots
     *
     * 9-35 - Top-left to Bottom-right inventory slots 36-44 - Left to Right
     * hotbar slots
     *
     * FURNACE
     *
     * 0 - Furnace Slot 1 - Fuel Slot 2 - Product Slot
     *
     * 3-29 - Top-left to Bottom-right inventory slots 30-38 - Left to Right
     * hotbar slots
     *
     * WORKBENCH
     *
     * 0 - Product Slot 1-9 - Top-left to Bottom-right crafting slots
     *
     * CHEST
     *
     * 0-26 - Top-left to Bottom-right chest slots
     *
     * 27-53 - Top-left to Bottom-right inventory slots 54-62 - Left to Right
     * hotbar slots
     *
     * CHEST (Double)
     *
     * 0-53 - Top-left to Bottom-right chest slots
     *
     * 54-80 - Top-left to Bottom-right inventory slots 81-89 - Left to Right
     * hotbar slots
     *
     */

    @EventHandler (priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent evt) {

        Player player = null;
        if(evt.getWhoClicked() instanceof Player)
            player = (Player) evt.getWhoClicked();
        
        if(evt.isShiftClick() == false){
            
            if (player != null && evt.getCursor() != null && evt.getCurrentItem() == null) {

                Quester quester = plugin.getQuester(evt.getWhoClicked().getName());
                if (quester.currentQuest != null) {

                    if (quester.currentQuest.questItems.containsKey(evt.getCursor().getType())) {

                        //Placing Quest item in empty slot

                        String s = Quester.checkPlacement(evt.getInventory(), evt.getRawSlot());
                        if (s == null) {
                            //Placing Quest item in an allowed player inventory slot
                            if (quester.holdingQuestItemFromStorage) {
                                quester.collectItem(evt.getCursor());
                                quester.holdingQuestItemFromStorage = false;
                            }
                        } else {
                            player.sendMessage(ChatColor.YELLOW + s);
                            evt.setCancelled(true);
                            player.updateInventory();
                        }

                    }

                }

            } else if (player != null && evt.getCursor() != null && evt.getCurrentItem() != null) {

                Quester quester = plugin.getQuester(evt.getWhoClicked().getName());
                if (quester.currentQuest != null) {

                    if (quester.currentQuest.questItems.containsKey(evt.getCurrentItem().getType()) || quester.currentQuest.questItems.containsKey(evt.getCursor().getType())) {

                        //Either the cursor item or the slot item (or both) is a Quest item

                        Material cursor = evt.getCursor().getType();
                        Material slot = evt.getCurrentItem().getType();


                        if (cursor == slot && quester.currentQuest.questItems.containsKey(cursor)) {

                            //Both are the same item, and quest items
                            String s = Quester.checkPlacement(evt.getInventory(), evt.getRawSlot());
                            if(s == null){

                                ItemStack from = evt.getCursor();
                                ItemStack to = evt.getCurrentItem();

                                if((from.getAmount() + to.getAmount()) <= from.getMaxStackSize()){
                                    if(quester.holdingQuestItemFromStorage){
                                        quester.collectItem(from);
                                        quester.holdingQuestItemFromStorage = false;
                                    }
                                }else if((from.getAmount() + to.getAmount()) > from.getMaxStackSize() && to.getAmount() < to.getMaxStackSize()){
                                    if(quester.holdingQuestItemFromStorage){
                                        ItemStack difference = to.clone();
                                        difference.setAmount(difference.getMaxStackSize() - difference.getAmount());
                                        quester.collectItem(difference);
                                        quester.holdingQuestItemFromStorage = false;
                                    }
                                }

                            }else{
                                player.sendMessage(ChatColor.YELLOW + s);
                                evt.setCancelled(true);
                                player.updateInventory();
                            }

                        } else if (cursor != slot && quester.currentQuest.questItems.containsKey(cursor)) {

                            //Cursor is a quest item, item in clicked slot is not
                            String s = Quester.checkPlacement(evt.getInventory(), evt.getRawSlot());
                            if (quester.holdingQuestItemFromStorage && s == null) {
                                quester.collectItem(evt.getCursor());
                                quester.holdingQuestItemFromStorage = false;
                            } else if (s != null) {
                                player.sendMessage(ChatColor.YELLOW + s);
                                evt.setCancelled(true);
                                player.updateInventory();
                            }

                        } else if (cursor != slot && quester.currentQuest.questItems.containsKey(slot)) {

                            //Item in clicked slot is a quest item, cursor is not
                            String s = Quester.checkPlacement(evt.getInventory(), evt.getRawSlot());
                            if(s != null)
                                quester.holdingQuestItemFromStorage = true;

                        } else {

                            //Both are different quest items
                            String s = Quester.checkPlacement(evt.getInventory(), evt.getRawSlot());
                            if (quester.holdingQuestItemFromStorage && s == null) {
                                quester.collectItem(evt.getCursor());
                                quester.holdingQuestItemFromStorage = false;
                            } else if (s != null) {
                                player.sendMessage(ChatColor.YELLOW + s);
                                evt.setCancelled(true);
                                player.updateInventory();
                            }

                        }
                    }

                }

            }
        
        }else{
            
            if(player != null && evt.getCurrentItem() != null){
                
                Quester quester = plugin.getQuester(evt.getWhoClicked().getName());
                Material mat = evt.getCurrentItem().getType();
                
                if(quester.currentQuest != null){
                    
                    if(quester.currentQuest.questItems.containsKey(mat)){
                        
                        List<Integer> changedSlots = Quester.getChangedSlots(evt.getInventory(), evt.getCurrentItem());
                        boolean can = true;
                        for(int i : changedSlots){
                            
                            String s = Quester.checkPlacement(evt.getInventory(), i);
                            if(s != null){
                                can = false;
                                break;
                            }
                            
                        }
                        if(!can){
                            
                            evt.setCancelled(true);
                            player.updateInventory();
                            
                        }else if(can && Quester.checkPlacement(evt.getInventory(), evt.getRawSlot()) != null){
                            
                            ItemStack oldStack = evt.getCurrentItem();
                            Inventory inv = plugin.getServer().createInventory(null, evt.getInventory().getType());
                            HashMap<Integer, ItemStack> map = inv.addItem(oldStack);
                                
                            if(map.isEmpty() == false){
                                    
                                    ItemStack newStack = oldStack.clone();
                                    newStack.setAmount(oldStack.getAmount() - map.get(0).getAmount());
                                    quester.collectItem(newStack);
                                    
                            }else{
                                quester.collectItem(oldStack);
                            }
                            
                        }
                        
                    }
                    
                }
                
            }
            
        }

    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent evt) {

        if (evt.getEntity() instanceof Player == false) {

            if (evt.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {

                EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) evt.getEntity().getLastDamageCause();
                Entity damager = damageEvent.getDamager();

                if (damager != null) {

                    if (damager instanceof Projectile) {

                        Projectile p = (Projectile) damager;
                        if (p.getShooter() instanceof Player) {

                            Player player = (Player) p.getShooter();
                            boolean okay = true;

                            if (plugin.citizens != null) {
                                if (plugin.citizens.getNPCRegistry().isNPC(player)) {
                                    okay = false;
                                }
                            }

                            if (okay) {

                                Quester quester = plugin.getQuester(player.getName());
                                if (quester.hasObjective("killMob")) {
                                    quester.killMob(evt.getEntity().getLocation(), evt.getEntity().getType());
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
                            if (quester.hasObjective("killMob")) {
                                quester.killMob(evt.getEntity().getLocation(), evt.getEntity().getType());
                            }

                        }
                    }

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

                if (damager instanceof Projectile) {

                    Projectile p = (Projectile) damager;
                    if (p.getShooter() instanceof Player) {

                        Player player = (Player) p.getShooter();
                        boolean okay = true;

                        if (plugin.citizens != null) {
                            if (plugin.citizens.getNPCRegistry().isNPC(player) || plugin.citizens.getNPCRegistry().isNPC(evt.getEntity())) {
                                okay = false;
                            }
                        }

                        if (okay) {

                            Quester quester = plugin.getQuester(player.getName());
                            if (quester.hasObjective("killPlayer")) {
                                quester.killPlayer(evt.getEntity().getName());
                            }

                        }

                    }

                } else if (damager instanceof Player) {

                    Player player = (Player) damager;
                    boolean okay = true;

                    if (plugin.citizens != null) {

                        if (plugin.citizens.getNPCRegistry().isNPC(player) || plugin.citizens.getNPCRegistry().isNPC(evt.getEntity())) {
                            okay = false;
                        }

                    }

                    if (okay) {

                        Quester quester = plugin.getQuester(player.getName());
                        if (quester.hasObjective("killPlayer")) {
                            quester.killPlayer(evt.getEntity().getName());
                        }

                    }
                }

            }

        }

    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent evt) {

        Player player = evt.getPlayer();
        Quester quester = plugin.getQuester(player.getName());
        if (quester.hasObjective("catchFish") && evt.getState().equals(State.CAUGHT_FISH)) {
            quester.catchFish();
        }

    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent evt) {

        Quester quester = plugin.getQuester(evt.getPlayer().getName());
        if (quester.currentQuest != null) {

            if (quester.currentQuest.questItems.containsKey(evt.getItemDrop().getItemStack().getType())) {

                evt.getPlayer().sendMessage(ChatColor.YELLOW + "You may not discard Quest items.");
                evt.setCancelled(true);

            }

        }

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent evt) {

        Quester quester = new Quester(plugin);
        quester.name = evt.getPlayer().getName();
        if (new File(plugin.getDataFolder(), "data/" + quester.name + ".yml").exists()) {
            quester.loadData();
        } else {
            quester.saveData();
        }
        plugin.questers.put(evt.getPlayer().getName(), quester);

        for (String s : quester.completedQuests) {

            for (Quest q : plugin.quests) {

                if (q.name.equalsIgnoreCase(s)) {

                    if (quester.completedTimes.containsKey(q.name) == false && q.redoDelay > -1) {
                        quester.completedTimes.put(q.name, System.currentTimeMillis());
                    }

                }


            }

        }

        quester.checkQuest();

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent evt) {

        Quester quester = plugin.getQuester(evt.getPlayer().getName());
        quester.saveData();
        plugin.questers.remove(quester.name);

    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent evt) {

        boolean isPlayer = true;
        if (plugin.getServer().getPluginManager().getPlugin("Citizens") != null) {
            if (plugin.citizens.getNPCRegistry().isNPC(evt.getPlayer())) {
                isPlayer = false;
            }
        }

        if (isPlayer) {

            Quester quester = plugin.getQuester(evt.getPlayer().getName());

            if (quester.hasObjective("reachLocation")) {

                quester.reachLocation(evt.getTo());

            }

        }

    }
}
