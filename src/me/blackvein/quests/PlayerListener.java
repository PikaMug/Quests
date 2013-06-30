package me.blackvein.quests;

import java.io.File;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerFishEvent.State;

public class PlayerListener implements Listener {

    final Quests plugin;

    public PlayerListener(Quests newPlugin) {

        plugin = newPlugin;

    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent evt) {

        if (plugin.checkQuester(evt.getPlayer().getName()) == false) {

            if (evt.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {

                final Quester quester = plugin.getQuester(evt.getPlayer().getName());
                final Player player = evt.getPlayer();

                if (quester.hasObjective("useBlock")) {

                    quester.useBlock(evt.getClickedBlock().getType());

                }else if (plugin.questFactory.selectedBlockStarts.containsKey(evt.getPlayer())){

                    Block block = evt.getClickedBlock();
                    Location loc = block.getLocation();
                    plugin.questFactory.selectedBlockStarts.put(evt.getPlayer(), block);
                    evt.getPlayer().sendMessage(ChatColor.GOLD + "Selected location " + ChatColor.AQUA + loc.getWorld().getName() + ": " + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + ChatColor.GOLD + " (" + ChatColor.GREEN + Quester.prettyItemString(block.getType().getId()) + ChatColor.GOLD + ")");

                }else if (plugin.eventFactory.selectedExplosionLocations.containsKey(evt.getPlayer())){

                    Block block = evt.getClickedBlock();
                    Location loc = block.getLocation();
                    plugin.eventFactory.selectedExplosionLocations.put(evt.getPlayer(), block);
                    evt.getPlayer().sendMessage(ChatColor.GOLD + "Selected location " + ChatColor.AQUA + loc.getWorld().getName() + ": " + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + ChatColor.GOLD + " (" + ChatColor.GREEN + Quester.prettyItemString(block.getType().getId()) + ChatColor.GOLD + ")");

                }else if (plugin.eventFactory.selectedEffectLocations.containsKey(evt.getPlayer())){

                    Block block = evt.getClickedBlock();
                    Location loc = block.getLocation();
                    plugin.eventFactory.selectedEffectLocations.put(evt.getPlayer(), block);
                    evt.getPlayer().sendMessage(ChatColor.GOLD + "Selected location " + ChatColor.AQUA + loc.getWorld().getName() + ": " + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + ChatColor.GOLD + " (" + ChatColor.GREEN + Quester.prettyItemString(block.getType().getId()) + ChatColor.GOLD + ")");

                }else if (plugin.eventFactory.selectedMobLocations.containsKey(evt.getPlayer())){

                    Block block = evt.getClickedBlock();
                    Location loc = block.getLocation();
                    plugin.eventFactory.selectedMobLocations.put(evt.getPlayer(), block);
                    evt.getPlayer().sendMessage(ChatColor.GOLD + "Selected location " + ChatColor.AQUA + loc.getWorld().getName() + ": " + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + ChatColor.GOLD + " (" + ChatColor.GREEN + Quester.prettyItemString(block.getType().getId()) + ChatColor.GOLD + ")");

                }else if (plugin.eventFactory.selectedLightningLocations.containsKey(evt.getPlayer())){

                    Block block = evt.getClickedBlock();
                    Location loc = block.getLocation();
                    plugin.eventFactory.selectedLightningLocations.put(evt.getPlayer(), block);
                    evt.getPlayer().sendMessage(ChatColor.GOLD + "Selected location " + ChatColor.AQUA + loc.getWorld().getName() + ": " + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + ChatColor.GOLD + " (" + ChatColor.GREEN + Quester.prettyItemString(block.getType().getId()) + ChatColor.GOLD + ")");

                }else if (plugin.eventFactory.selectedTeleportLocations.containsKey(evt.getPlayer())){

                    Block block = evt.getClickedBlock();
                    Location loc = block.getLocation();
                    plugin.eventFactory.selectedTeleportLocations.put(evt.getPlayer(), block);
                    evt.getPlayer().sendMessage(ChatColor.GOLD + "Selected location " + ChatColor.AQUA + loc.getWorld().getName() + ": " + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + ChatColor.GOLD + " (" + ChatColor.GREEN + Quester.prettyItemString(block.getType().getId()) + ChatColor.GOLD + ")");

                }else if (plugin.questFactory.selectedKillLocations.containsKey(evt.getPlayer())){

                    Block block = evt.getClickedBlock();
                    Location loc = block.getLocation();
                    plugin.questFactory.selectedKillLocations.put(evt.getPlayer(), block);
                    evt.getPlayer().sendMessage(ChatColor.GOLD + "Selected location " + ChatColor.AQUA + loc.getWorld().getName() + ": " + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + ChatColor.GOLD + " (" + ChatColor.GREEN + Quester.prettyItemString(block.getType().getId()) + ChatColor.GOLD + ")");

                }else if (plugin.questFactory.selectedReachLocations.containsKey(evt.getPlayer())){

                    Block block = evt.getClickedBlock();
                    Location loc = block.getLocation();
                    plugin.questFactory.selectedReachLocations.put(evt.getPlayer(), block);
                    evt.getPlayer().sendMessage(ChatColor.GOLD + "Selected location " + ChatColor.AQUA + loc.getWorld().getName() + ": " + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + ChatColor.GOLD + " (" + ChatColor.GREEN + Quester.prettyItemString(block.getType().getId()) + ChatColor.GOLD + ")");

                }else if(player.isConversing() == false){

                    for (final Quest q : plugin.quests) {

                        if (q.blockStart != null) {

                            if (q.blockStart.equals(evt.getClickedBlock().getLocation())) {

                                if (quester.currentQuest != null) {

                                    player.sendMessage(ChatColor.YELLOW + "You may only have one active Quest.");

                                } else {

                                    if (quester.completedQuests.contains(q.name)) {

                                        if (q.redoDelay > -1 && (quester.getDifference(q)) > 0) {

                                            player.sendMessage(ChatColor.YELLOW + "You may not take " + ChatColor.AQUA + q.name + ChatColor.YELLOW + " again for another " + ChatColor.DARK_PURPLE + Quests.getTime(quester.getDifference(q)) + ChatColor.YELLOW + ".");
                                            return;

                                        }else if (quester.completedQuests.contains(q.name) && q.redoDelay < 0) {

                                            player.sendMessage(ChatColor.YELLOW + "You have already completed " + ChatColor.AQUA + q.name + ChatColor.YELLOW + ".");
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

    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent evt) {

        if (plugin.checkQuester(evt.getPlayer().getName()) == false) {

            Quester quester = plugin.getQuester(evt.getPlayer().getName());
            if (quester.hasObjective("damageBlock")) {

                quester.damageBlock(evt.getBlock().getType());

            }

        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent evt) {

        if (plugin.checkQuester(evt.getPlayer().getName()) == false) {

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

        if (plugin.checkQuester(evt.getPlayer().getName()) == false) {

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

    }

    @EventHandler
    public void onPlayerShearEntity(PlayerShearEntityEvent evt) {

        if (plugin.checkQuester(evt.getPlayer().getName()) == false) {

            Quester quester = plugin.getQuester(evt.getPlayer().getName());
            if (evt.getEntity().getType().equals(EntityType.SHEEP) && quester.hasObjective("shearSheep")) {

                Sheep sheep = (Sheep) evt.getEntity();
                quester.shearSheep(sheep.getColor());

            }

        }

    }

    @EventHandler
    public void onEntityTame(EntityTameEvent evt) {

        if (evt.getOwner() instanceof Player) {

            Player p = (Player) evt.getOwner();
            if (plugin.checkQuester(p.getName()) == false) {

                Quester quester = plugin.getQuester(p.getName());
                if (quester.hasObjective("tameMob")) {

                    quester.tameMob(evt.getEntityType());

                }

            }

        }

    }

    @EventHandler
    public void onEnchantItem(EnchantItemEvent evt) {

        if (plugin.checkQuester(evt.getEnchanter().getName()) == false) {

            Quester quester = plugin.getQuester(evt.getEnchanter().getName());
            if (quester.hasObjective("enchantItem")) {

                for (Enchantment e : evt.getEnchantsToAdd().keySet()) {

                    quester.enchantItem(e, evt.getItem().getType());

                }

            }

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

                        if (plugin.checkQuester(player.getName()) == false) {

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

                } else if (damager instanceof Player) {

                    Player player = (Player) damager;

                    if (plugin.checkQuester(player.getName()) == false) {

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

    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent evt) {

        Player player = evt.getPlayer();
        if(plugin.checkQuester(player.getName()) == false){

            Quester quester = plugin.getQuester(player.getName());
            if (quester.hasObjective("catchFish") && evt.getState().equals(State.CAUGHT_FISH)) {
                quester.catchFish();
            }

        }

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent evt) {

        if(plugin.checkQuester(evt.getPlayer().getName()) == false){

            Quester quester = new Quester(plugin);
            quester.name = evt.getPlayer().getName();
            if (new File(plugin.getDataFolder(), "data/" + quester.name + ".yml").exists()) {
                quester.loadData();
            } else {
                quester.saveData();
            }
            plugin.questers.put(evt.getPlayer().getName(), quester);

            for (String s : quester.completedQuests) {

                Quest q = plugin.getQuest(s);

                if (q != null) {

                    if (quester.completedTimes.containsKey(q.name) == false && q.redoDelay > -1)
                        quester.completedTimes.put(q.name, System.currentTimeMillis());

                }

            }

            quester.checkQuest();

            if(quester.currentQuest != null){

                if(quester.currentStage.delay > -1){

                    quester.startStageTimer();

                }

            }

        }

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent evt) {

        if(plugin.checkQuester(evt.getPlayer().getName()) == false){

            Quester quester = plugin.getQuester(evt.getPlayer().getName());
            if(quester.currentQuest != null){
                if(quester.currentStage.delay > -1)
                    quester.stopStageTimer();
            }
            quester.saveData();

            plugin.questers.remove(quester.name);

        }

    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent evt) {

        if(plugin.checkQuester(evt.getPlayer().getName()) == false){

            boolean isPlayer = true;
            if (plugin.citizens != null) {
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
}
