package me.blackvein.quests;

import ca.xshade.questionmanager.Option;
import ca.xshade.questionmanager.Question;
import java.io.File;
import java.util.LinkedList;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.event.player.*;

public class PlayerListener implements Listener {

    Quests plugin;

    public PlayerListener(Quests newPlugin) {

        plugin = newPlugin;

    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent evt){

        if(evt.getAction().equals(Action.RIGHT_CLICK_BLOCK)){

                final Quester quester = plugin.getQuester(evt.getPlayer().getName());
                final Player player = evt.getPlayer();

                if(quester.hasObjective("useBlock")){

                    quester.useBlock(evt.getClickedBlock().getType());

                }else {

                    for(final Quest q : plugin.quests){

                        if(q.blockStart != null){

                            if(q.blockStart.equals(evt.getClickedBlock().getLocation())){

                                if(quester.currentQuest != null){

                                    player.sendMessage(ChatColor.YELLOW + "You may only have one active Quest.");

                                }else {

                                    LinkedList<Option> options = new LinkedList<Option>();
                                    Option yes = new Option("Yes", new Runnable(){

                                        public void run(){

                                            if(q.testRequirements(player) == true){

                                                quester.currentQuest = q;
                                                quester.currentStage = q.stages.getFirst();
                                                quester.addEmpties();
                                                quester.isTalking = false;
                                                if(q.moneyReq > 0){
                                                    Quests.economy.withdrawPlayer(quester.name, q.moneyReq);
                                                }
                                                player.sendMessage(ChatColor.GREEN + "Quest accepted: " + q.name);
                                                player.sendMessage("");
                                                player.sendMessage(ChatColor.GOLD + "---(Objectives)---");
                                                for(String s : quester.getObjectives()){
                                                    player.sendMessage(s);
                                                }

                                            }else {

                                                player.sendMessage(q.failRequirements);
                                                quester.isTalking = false;

                                            }

                                        }

                                    });

                                    Option no = new Option("No", new Runnable(){

                                        public void run(){

                                            quester.isTalking = false;
                                            player.sendMessage(ChatColor.YELLOW + "Cancelled.");

                                        }

                                    });

                                    options.add(yes);
                                    options.add(no);

                                    if(quester.isTalking == false){

                                        quester.isTalking = true;
                                        Question question = new Question(player.getName(), "Accept quest?", options);
                                        plugin.questioner.getQuestionManager().newQuestion(question);
                                        player.sendMessage(ChatColor.GOLD + "- " + q.name + " -");
                                        player.sendMessage(q.description);
                                        try{
                                            plugin.questioner.appendQuestion(question);
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }

                                    }

                                }

                            }

                        }

                    }

                }

        }

    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent evt){

        Quester quester = plugin.getQuester(evt.getPlayer().getName());
        if(quester.hasObjective("damageBlock")){

            quester.damageBlock(evt.getBlock().getType());

        }

    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent evt){

        if(evt.getPlayer().getName().toLowerCase().contains("_computercraft_") == false && evt.getPlayer().getName().toLowerCase().contains("_buildcraft_") == false && evt.getPlayer().getName().toLowerCase().contains("_redpower_") == false && evt.getPlayer().getName().toLowerCase().contains("_buildcraft_") == false && evt.getPlayer().getName().toLowerCase().contains("(buildcraft)") == false){

            Quester quester = plugin.getQuester(evt.getPlayer().getName());
            if(quester.hasObjective("placeBlock")){

                if(evt.isCancelled() == false)
                    quester.placeBlock(evt.getBlock().getType());

            }

        }

    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent evt){

        boolean canOpen = true;

            if(evt.getBlock().getType().equals(Material.BREWING_STAND) && plugin.allowOtherBrewing == false && evt.getPlayer().getName().contains("_computercraft_") == false && evt.getPlayer().getName().contains("_buildcraft_") == false && evt.getPlayer().getName().contains("_redpower_") == false){

                if(plugin.brewers.containsKey(evt.getBlock().getLocation())){

                    if(evt.getPlayer().getName().equalsIgnoreCase(plugin.brewers.get(evt.getBlock().getLocation())) == false){
                        evt.getPlayer().sendMessage(ChatColor.RED + "You may not break other players' Brewing Stands.");
                        evt.setCancelled(true);
                        canOpen = false;
                    }

                }

            }

            if(canOpen == true){

                Quester quester = plugin.getQuester(evt.getPlayer().getName());
                if(quester.hasObjective("breakBlock")){

                    if(evt.getPlayer().getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH) == false && evt.isCancelled() == false)
                        quester.breakBlock(evt.getBlock().getType());

                }

                if(quester.hasObjective("placeBlock")){

                    if(quester.blocksPlaced.containsKey(evt.getBlock().getType())){

                        if(quester.blocksPlaced.get(evt.getBlock().getType()) > 0){

                            if(evt.isCancelled() == false)
                                quester.blocksPlaced.put(evt.getBlock().getType(), quester.blocksPlaced.get(evt.getBlock().getType()) - 1);

                        }

                    }

                }

                if(evt.getPlayer().getItemInHand().getType().equals(Material.SHEARS) && quester.hasObjective("cutBlock")){

                            quester.cutBlock(evt.getBlock().getType());

                }

            }

    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent evt){

        Quester quester = plugin.getQuester(evt.getPlayer().getName());
        if(quester.hasObjective("collectItem")){

            quester.collectItem(evt.getItem().getItemStack());

        }

    }

    @EventHandler
    public void onPlayerShearEntity(PlayerShearEntityEvent evt){

        Quester quester = plugin.getQuester(evt.getPlayer().getName());
        if(evt.getEntity().getType().equals(EntityType.SHEEP) && quester.hasObjective("shearSheep")){

            Sheep sheep = (Sheep) evt.getEntity();
            quester.shearSheep(sheep.getColor());


        }

    }

    @EventHandler
    public void onEntityTame(EntityTameEvent evt){

        if(evt.getOwner() instanceof Player){

            Player p = (Player) evt.getOwner();
            Quester quester = plugin.getQuester(p.getName());
            if(quester.hasObjective("tameMob")){

                quester.tameMob(evt.getEntityType());

            }

        }

    }

    @EventHandler
    public void onEnchantItem(EnchantItemEvent evt){

        Quester quester = plugin.getQuester(evt.getEnchanter().getName());
        if(quester.hasObjective("enchantItem")){

            for(Enchantment e : evt.getEnchantsToAdd().keySet()){

                quester.enchantItem(e, evt.getItem().getType());

            }

        }

    }

    @EventHandler
    public void onCraftItem(CraftItemEvent evt){

        if(evt.getWhoClicked() instanceof Player){

            Player p = (Player) evt.getWhoClicked();
            Quester quester = plugin.getQuester(p.getName());
            if(quester.hasObjective("craftItem")){

                quester.craftItem(evt.getCurrentItem());

            }

        }

    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent evt){

        if(evt.getWhoClicked() instanceof Player && evt.getCursor() != null){

            Quester quester = plugin.getQuester(evt.getWhoClicked().getName());
            if(quester.currentQuest != null){

                if(quester.currentQuest.questItems.containsKey(evt.getCursor().getType())){

                    if(evt.getInventory().getType().equals(InventoryType.CHEST) == true && evt.getRawSlot() > 52){
                        quester.collectItem(evt.getCursor());
                    }

                }

            }

        }

        if(evt.getWhoClicked() instanceof Player && evt.getCurrentItem() != null){

            Quester quester = plugin.getQuester(evt.getWhoClicked().getName());
            if(quester.currentQuest != null){

                if(quester.currentQuest.questItems.containsKey(evt.getCurrentItem().getType())){

                    if(evt.getInventory().getType().equals(InventoryType.CHEST) == false || evt.getInventory().getType().equals(InventoryType.CHEST) == true && evt.getRawSlot() > 52){
                        ((Player) evt.getWhoClicked()).sendMessage(ChatColor.YELLOW + "You may not modify Quest items in your inventory.");
                        evt.setCancelled(true);
                    }else if(evt.getInventory().getType().equals(InventoryType.CHEST) == true && evt.getRawSlot() < 53)
                        quester.collectItem(evt.getCurrentItem());

                }

            }

        }

    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent evt){

        if(evt.getEntity() instanceof Player == false){

            if(evt.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent){

                EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) evt.getEntity().getLastDamageCause();
                Entity damager = damageEvent.getDamager();

                if(damager != null){

                    if(damager instanceof Projectile){

                        Projectile p = (Projectile) damager;
                        if(p.getShooter() instanceof Player){

                                Player player = (Player) p.getShooter();
                                if(plugin.citizens.getNPCRegistry().isNPC(player) == false){
                                
                                    Quester quester = plugin.getQuester(player.getName());
                                    if(quester.hasObjective("killMob"))
                                        quester.killMob(evt.getEntity().getLocation(), evt.getEntity().getType());

                                }
                        }

                    }else if(damager instanceof Player){

                        if(plugin.citizens.getNPCRegistry().isNPC(damager) == false){
                            
                            Player player = (Player) damager;
                            Quester quester = plugin.getQuester(player.getName());
                            if(quester.hasObjective("killMob"))
                                quester.killMob(evt.getEntity().getLocation(), evt.getEntity().getType());

                        }
                    }

                }

            }

        }

    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent evt){

            if(evt.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent){

                EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) evt.getEntity().getLastDamageCause();
                Entity damager = damageEvent.getDamager();

                if(damager != null){

                    if(damager instanceof Projectile){

                        Projectile p = (Projectile) damager;
                        if(p.getShooter() instanceof Player){

                            Player player = (Player) p.getShooter();
                            if(plugin.citizens.getNPCRegistry().isNPC(player) == false){
                                
                                Quester quester = plugin.getQuester(player.getName());
                                if(quester.hasObjective("killPlayer"))
                                    quester.killPlayer(evt.getEntity().getName());
                            
                            }

                        }

                    }else if(damager instanceof Player){

                        Player player = (Player) damager;
                        if(plugin.citizens.getNPCRegistry().isNPC(player) == false){
                        
                            Quester quester = plugin.getQuester(player.getName());
                            if(quester.hasObjective("killPlayer"))
                                quester.killPlayer(evt.getEntity().getName());

                        }
                    }

                }

            }

    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent evt){

        Player player = evt.getPlayer();
        Quester quester = plugin.getQuester(player.getName());
        if(quester.hasObjective("catchFish") && evt.getState().equals(State.CAUGHT_FISH))
            quester.catchFish();

    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent evt){

        Quester quester = plugin.getQuester(evt.getPlayer().getName());
        if(quester.currentQuest != null){

            if(quester.currentQuest.questItems.containsKey(evt.getItemDrop().getItemStack().getType())){

                evt.getPlayer().sendMessage(ChatColor.YELLOW + "You may not discard Quest items.");
                    evt.setCancelled(true);

            }

        }

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent evt){

        Quester quester = new Quester(plugin);
        quester.name = evt.getPlayer().getName();
        if(new File(plugin.getDataFolder(), "data/" + quester.name + ".yml").exists()){
            quester.loadData();
        }else {
            quester.saveData();
        }
        plugin.questers.add(quester);

        for(Quest q : quester.completedQuests){
            if(quester.completedTimes.containsKey(q) == false && q.redoDelay > -1)
                quester.completedTimes.put(q, System.currentTimeMillis());
        }

        quester.checkQuest();

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent evt){

        Quester quester = plugin.getQuester(evt.getPlayer().getName());
        quester.saveData();
        plugin.questers.remove(quester);

    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent evt){

        if(plugin.citizens.getNPCRegistry().isNPC(evt.getPlayer()) == false){
        
            Quester quester = plugin.getQuester(evt.getPlayer().getName());

            if(quester.hasObjective("reachLocation")){

                quester.reachLocation(evt.getTo());

            }
        
        }

    }

}
