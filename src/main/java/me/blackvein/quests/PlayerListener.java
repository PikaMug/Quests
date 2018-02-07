/*******************************************************************************************************
 * Continued by FlyingPikachu/HappyPikachu with permission from _Blackvein_. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

package me.blackvein.quests;

import java.io.File;
import java.util.Iterator;

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
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
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
			if (ac.equals(InventoryAction.MOVE_TO_OTHER_INVENTORY) || ac.equals(InventoryAction.DROP_ALL_SLOT) || ac.equals(InventoryAction.DROP_ONE_SLOT)) {
				evt.setCancelled(true);
				return;
			}
		} else if (ItemUtil.isItem(evt.getCurrentItem()) && ItemUtil.isJournal(evt.getCursor())) {
			if (ac.equals(InventoryAction.MOVE_TO_OTHER_INVENTORY) || ac.equals(InventoryAction.DROP_ALL_CURSOR) || ac.equals(InventoryAction.DROP_ONE_CURSOR)) {
				evt.setCancelled(true);
				return;
			}
		}
		if (ItemUtil.isItem(evt.getCurrentItem()) && ItemUtil.isJournal(evt.getCurrentItem()) || ItemUtil.isItem(evt.getCursor()) && ItemUtil.isJournal(evt.getCursor())) {
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
		if (evt.getInventory().getTitle().contains(Lang.get(player, "quests"))) {
			ItemStack clicked = evt.getCurrentItem();
			if (clicked != null) {
				for (Quest quest : plugin.quests) {
					if (quest.guiDisplay != null) {
						if (ItemUtil.compareItems(clicked, quest.guiDisplay, false) == 0) {
							if (quester.currentQuests.size() >= Quests.maxQuests && Quests.maxQuests > 0) {
								String msg = Lang.get(player, "questMaxAllowed");
								msg = msg.replaceAll("<number>", String.valueOf(Quests.maxQuests));
								player.sendMessage(ChatColor.YELLOW + msg);
							} else if (quester.completedQuests.contains(quest.name) && quest.redoDelay < 0) {
								String completed = Lang.get(player, "questAlreadyCompleted");
								completed = completed.replaceAll("<quest>", ChatColor.AQUA + quest.name + ChatColor.YELLOW);
								player.sendMessage(ChatColor.YELLOW + completed);
							} else {
								boolean takeable = true;
								if (quester.completedQuests.contains(quest.name)) {
									if (quester.getDifference(quest) > 0) {
										String early = Lang.get(player, "questTooEarly");
										early = early.replaceAll("<quest>", ChatColor.AQUA + quest.name + ChatColor.YELLOW);
										early = early.replaceAll("<time>", ChatColor.DARK_PURPLE + Quests.getTime(quester.getDifference(quest)) + ChatColor.YELLOW);
										player.sendMessage(ChatColor.YELLOW + early);
										takeable = false;
									}
								}
								if (quest.region != null) {
									boolean inRegion = false;
									Player p = quester.getPlayer();
									RegionManager rm = Quests.worldGuard.getRegionManager(p.getWorld());
									Iterator<ProtectedRegion> it = rm.getApplicableRegions(p.getLocation()).iterator();
									while (it.hasNext()) {
										ProtectedRegion pr = it.next();
										if (pr.getId().equalsIgnoreCase(quest.region)) {
											inRegion = true;
											break;
										}
									}
									if (inRegion == false) {
										String invalidLoc = Lang.get(player, "questInvalidLocation");
										invalidLoc = invalidLoc.replaceAll("<quest>", ChatColor.AQUA + quest.name + ChatColor.YELLOW);
										player.sendMessage(ChatColor.YELLOW + invalidLoc);
										takeable = false;
									}
								}
								if (takeable == true) {
									quester.takeQuest(quest, false);
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
		if (ItemUtil.isItem(evt.getOldCursor()) && ItemUtil.isJournal(evt.getOldCursor()) || ItemUtil.isItem(evt.getCursor()) && ItemUtil.isJournal(evt.getCursor())) {
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
		if (ItemUtil.isJournal(evt.getItemDrop().getItemStack()))
			evt.setCancelled(true);
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent evt) {
		if (plugin.checkQuester(evt.getPlayer().getUniqueId()) == false) {
			if (evt.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
				final Quester quester = plugin.getQuester(evt.getPlayer().getUniqueId());
				final Player player = evt.getPlayer();
				boolean hasObjective = false;
				for (Quest quest : quester.currentQuests.keySet()) {
					if (quester.hasObjective(quest, "useBlock")) {
						ItemStack i = new ItemStack(evt.getClickedBlock().getType(), 1, evt.getClickedBlock().getState().getData().toItemStack().getDurability());
						quester.useBlock(quest, i);
						hasObjective = true;
					}
				}
				if (!hasObjective) {
					if (plugin.questFactory.selectedBlockStarts.containsKey(evt.getPlayer().getUniqueId())) {
						Block block = evt.getClickedBlock();
						Location loc = block.getLocation();
						plugin.questFactory.selectedBlockStarts.put(evt.getPlayer().getUniqueId(), block);
						evt.getPlayer().sendMessage(ChatColor.GOLD + Lang.get(player, "questSelectedLocation") + " " + ChatColor.AQUA + loc.getWorld().getName() + ": " + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + ChatColor.GOLD + " (" + ChatColor.GREEN + ItemUtil.getName(new ItemStack(block.getType())) + ChatColor.GOLD + ")");
					} else if (plugin.eventFactory.selectedExplosionLocations.containsKey(evt.getPlayer().getUniqueId())) {
						Block block = evt.getClickedBlock();
						Location loc = block.getLocation();
						plugin.eventFactory.selectedExplosionLocations.put(evt.getPlayer().getUniqueId(), block);
						evt.getPlayer().sendMessage(ChatColor.GOLD + Lang.get(player, "questSelectedLocation") + " " + ChatColor.AQUA + loc.getWorld().getName() + ": " + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + ChatColor.GOLD + " (" + ChatColor.GREEN + ItemUtil.getName(new ItemStack(block.getType())) + ChatColor.GOLD + ")");
					} else if (plugin.eventFactory.selectedEffectLocations.containsKey(evt.getPlayer().getUniqueId())) {
						Block block = evt.getClickedBlock();
						Location loc = block.getLocation();
						plugin.eventFactory.selectedEffectLocations.put(evt.getPlayer().getUniqueId(), block);
						evt.getPlayer().sendMessage(ChatColor.GOLD + Lang.get(player, "questSelectedLocation") + " " + ChatColor.AQUA + loc.getWorld().getName() + ": " + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + ChatColor.GOLD + " (" + ChatColor.GREEN + ItemUtil.getName(new ItemStack(block.getType())) + ChatColor.GOLD + ")");
					} else if (plugin.eventFactory.selectedMobLocations.containsKey(evt.getPlayer().getUniqueId())) {
						Block block = evt.getClickedBlock();
						Location loc = block.getLocation();
						plugin.eventFactory.selectedMobLocations.put(evt.getPlayer().getUniqueId(), block);
						evt.getPlayer().sendMessage(ChatColor.GOLD + Lang.get(player, "questSelectedLocation") + " " + ChatColor.AQUA + loc.getWorld().getName() + ": " + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + ChatColor.GOLD + " (" + ChatColor.GREEN + ItemUtil.getName(new ItemStack(block.getType()))+ ChatColor.GOLD + ")");
					} else if (plugin.eventFactory.selectedLightningLocations.containsKey(evt.getPlayer().getUniqueId())) {
						Block block = evt.getClickedBlock();
						Location loc = block.getLocation();
						plugin.eventFactory.selectedLightningLocations.put(evt.getPlayer().getUniqueId(), block);
						evt.getPlayer().sendMessage(ChatColor.GOLD + Lang.get(player, "questSelectedLocation") + " " + ChatColor.AQUA + loc.getWorld().getName() + ": " + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + ChatColor.GOLD + " (" + ChatColor.GREEN + ItemUtil.getName(new ItemStack(block.getType())) + ChatColor.GOLD + ")");
					} else if (plugin.eventFactory.selectedTeleportLocations.containsKey(evt.getPlayer().getUniqueId())) {
						Block block = evt.getClickedBlock();
						Location loc = block.getLocation();
						plugin.eventFactory.selectedTeleportLocations.put(evt.getPlayer().getUniqueId(), block);
						evt.getPlayer().sendMessage(ChatColor.GOLD + Lang.get(player, "questSelectedLocation") + " " + ChatColor.AQUA + loc.getWorld().getName() + ": " + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + ChatColor.GOLD + " (" + ChatColor.GREEN + ItemUtil.getName(new ItemStack(block.getType())) + ChatColor.GOLD + ")");
					} else if (plugin.questFactory.selectedKillLocations.containsKey(evt.getPlayer().getUniqueId())) {
						Block block = evt.getClickedBlock();
						Location loc = block.getLocation();
						plugin.questFactory.selectedKillLocations.put(evt.getPlayer().getUniqueId(), block);
						evt.getPlayer().sendMessage(ChatColor.GOLD + Lang.get(player, "questSelectedLocation") + " " + ChatColor.AQUA + loc.getWorld().getName() + ": " + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + ChatColor.GOLD + " (" + ChatColor.GREEN + ItemUtil.getName(new ItemStack(block.getType())) + ChatColor.GOLD + ")");
					} else if (plugin.questFactory.selectedReachLocations.containsKey(evt.getPlayer().getUniqueId())) {
						Block block = evt.getClickedBlock();
						Location loc = block.getLocation();
						plugin.questFactory.selectedReachLocations.put(evt.getPlayer().getUniqueId(), block);
						evt.getPlayer().sendMessage(ChatColor.GOLD + Lang.get(player, "questSelectedLocation") + " " + ChatColor.AQUA + loc.getWorld().getName() + ": " + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + ChatColor.GOLD + " (" + ChatColor.GREEN + ItemUtil.getName(new ItemStack(block.getType())) + ChatColor.GOLD + ")");
					} else if (player.isConversing() == false) {
						for (final Quest q : plugin.quests) {
							if (q.blockStart != null) {
								if (q.blockStart.equals(evt.getClickedBlock().getLocation())) {
									if (quester.currentQuests.size() >= Quests.maxQuests && Quests.maxQuests > 0) {
										String msg = Lang.get(player, "questMaxAllowed");
										msg = msg.replaceAll("<number>", String.valueOf(Quests.maxQuests));
										player.sendMessage(ChatColor.YELLOW + msg);
									} else {
										if (quester.completedQuests.contains(q.name)) {
											if (q.redoDelay > -1 && (quester.getDifference(q)) > 0) {
												String early = Lang.get(player, "questTooEarly");
												early = early.replaceAll("<quest>", ChatColor.AQUA + q.name + ChatColor.YELLOW);
												early = early.replaceAll("<time>", ChatColor.DARK_PURPLE + Quests.getTime(quester.getDifference(q)) + ChatColor.YELLOW);
												player.sendMessage(ChatColor.YELLOW + early);
												return;
											} else if (quester.completedQuests.contains(q.name) && q.redoDelay < 0) {
												String completed = Lang.get(player, "questAlreadyCompleted");
												completed = completed.replaceAll("<quest>", ChatColor.AQUA + q.name + ChatColor.YELLOW);
												player.sendMessage(ChatColor.YELLOW + completed);
												return;
											}
										}
										quester.questToTake = q.name;
										String s = ChatColor.GOLD + "- " + ChatColor.DARK_PURPLE + quester.questToTake + ChatColor.GOLD + " -\n" + "\n" + ChatColor.RESET + plugin.getQuest(quester.questToTake).description + "\n";
										for (String msg : s.split("<br>")) {
											player.sendMessage(msg);
										}
										plugin.conversationFactory.buildConversation(player).begin();
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
		if (plugin.checkQuester(evt.getPlayer().getUniqueId()) == false) {
			Quester quester = plugin.getQuester(evt.getPlayer().getUniqueId());
			if (quester.currentQuests.isEmpty() == false) {
				for (Quest quest : quester.currentQuests.keySet()) {
					Stage currentStage = quester.getCurrentStage(quest);
					if (currentStage == null) {
						plugin.getLogger().severe("currentStage was null for " + quester.id.toString() + " on chat");
						continue;
					}
					if (currentStage.chatEvents.isEmpty() == false) {
						String chat = evt.getMessage();
						for (String s : currentStage.chatEvents.keySet()) {
							if (s.equalsIgnoreCase(chat)) {
								if (quester.getQuestData(quest).eventFired.get(s) == null || quester.getQuestData(quest).eventFired.get(s) == false) {
									currentStage.chatEvents.get(s).fire(quester, quest);
									quester.getQuestData(quest).eventFired.put(s, true);
								}
							}
						}
					}
					if (quester.hasObjective(quest, "password")) {
						quester.sayPass(quest, evt);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent evt) {
		if (plugin.checkQuester(evt.getPlayer().getUniqueId()) == false) {
			Quester quester = plugin.getQuester(evt.getPlayer().getUniqueId());
			if (quester.currentQuests.isEmpty() == false) {
				for (Quest quest : quester.currentQuests.keySet()) {
					Stage currentStage = quester.getCurrentStage(quest);
					if (currentStage == null) {
						plugin.getLogger().severe("currentStage was null for " + quester.id.toString() + " on command");
						continue;
					}
					if (currentStage.commandEvents.isEmpty() == false) {
						String command = evt.getMessage();
						for (String s : currentStage.commandEvents.keySet()) {
							if (command.equalsIgnoreCase("/" + s)) {
								if (quester.getQuestData(quest).eventFired.get(s) == null || quester.getQuestData(quest).eventFired.get(s) == false) {
									currentStage.commandEvents.get(s).fire(quester, quest);
									quester.getQuestData(quest).eventFired.put(s, true);
								}
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onBlockDamage(BlockDamageEvent evt) {
		if (plugin.checkQuester(evt.getPlayer().getUniqueId()) == false) {
			Quester quester = plugin.getQuester(evt.getPlayer().getUniqueId());
			for (Quest quest : quester.currentQuests.keySet()) {
				if (quester.hasObjective(quest, "damageBlock")) {
					ItemStack i = new ItemStack(evt.getBlock().getType(), 1, evt.getBlock().getState().getData().toItemStack().getDurability());
					quester.damageBlock(quest, i);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlace(BlockPlaceEvent evt) {
		if (plugin.checkQuester(evt.getPlayer().getUniqueId()) == false) {
			Quester quester = plugin.getQuester(evt.getPlayer().getUniqueId());
			for (Quest quest : quester.currentQuests.keySet()) {
				if (quester.hasObjective(quest, "placeBlock")) {
					if (evt.isCancelled() == false) {
						ItemStack i = new ItemStack(evt.getBlock().getType(), 1, evt.getBlock().getState().getData().toItemStack().getDurability());
						quester.placeBlock(quest, i);
					}
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent evt) {
		if (plugin.checkQuester(evt.getPlayer().getUniqueId()) == false) {
			Quester quester = plugin.getQuester(evt.getPlayer().getUniqueId());
			for (Quest quest : quester.currentQuests.keySet()) {
				if (quester.hasObjective(quest, "breakBlock")) {
					if (evt.getPlayer().getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH) == false && evt.isCancelled() == false) {
						ItemStack i = new ItemStack(evt.getBlock().getType(), 1, evt.getBlock().getState().getData().toItemStack().getDurability());
						quester.breakBlock(quest, i);
					}
				}
				if (quester.hasObjective(quest, "placeBlock")) {
					for (ItemStack is : quester.getQuestData(quest).blocksPlaced) {
						if (is.getAmount() > 0) {
							if (evt.isCancelled() == false) {
								int index = quester.getQuestData(quest).blocksPlaced.indexOf(is);
								is.setAmount(is.getAmount() - 1);
								quester.getQuestData(quest).blocksPlaced.set(index, is);
							}
						}
					}
				}
				if (evt.getPlayer().getItemInHand().getType().equals(Material.SHEARS) && quester.hasObjective(quest, "cutBlock")) {
					ItemStack i = new ItemStack(evt.getBlock().getType(), 1, evt.getBlock().getState().getData().toItemStack().getDurability());
					quester.cutBlock(quest, i);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerShearEntity(PlayerShearEntityEvent evt) {
		if (plugin.checkQuester(evt.getPlayer().getUniqueId()) == false) {
			Quester quester = plugin.getQuester(evt.getPlayer().getUniqueId());
			for (Quest quest : quester.currentQuests.keySet()) {
				if (evt.getEntity().getType() == EntityType.SHEEP && quester.hasObjective(quest, "shearSheep")) {
					Sheep sheep = (Sheep) evt.getEntity();
					quester.shearSheep(quest, sheep.getColor());
				}
			}
		}
	}

	@EventHandler
	public void onEntityTame(EntityTameEvent evt) {
		if (evt.getOwner() instanceof Player) {
			Player p = (Player) evt.getOwner();
			if (plugin.checkQuester(p.getUniqueId()) == false) {
				Quester quester = plugin.getQuester(p.getUniqueId());
				for (Quest quest : quester.currentQuests.keySet()) {
					if (quester.hasObjective(quest, "tameMob")) {
						quester.tameMob(quest, evt.getEntityType());
					}
				}
			}
		}
	}

	@EventHandler
	public void onEnchantItem(EnchantItemEvent evt) {
		if (plugin.checkQuester(evt.getEnchanter().getUniqueId()) == false) {
			Quester quester = plugin.getQuester(evt.getEnchanter().getUniqueId());
			for (Quest quest : quester.currentQuests.keySet()) {
				if (quester.hasObjective(quest, "enchantItem")) {
					for (Enchantment e : evt.getEnchantsToAdd().keySet()) {
						quester.enchantItem(quest, e, evt.getItem().getType());
					}
				}
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
					ProjectileSource source = projectile.getShooter();
					if (source instanceof Entity) {
						killMob((Entity)source, evt.getEntity());
					}				
				} else if (damager instanceof TNTPrimed) {
					TNTPrimed tnt = (TNTPrimed) damager;
					Entity source = tnt.getSource();
					if (source.isValid()) {
						killMob(source, evt.getEntity());
					}
				} else if (damager instanceof Wolf) {
					Wolf wolf = (Wolf) damager;
					if (wolf.isTamed()) {
						Quester quester = plugin.getQuester(wolf.getOwner().getUniqueId());
						killPlayer(quester.getPlayer(), evt.getEntity());
					}
				} else {
					killMob(damager, evt.getEntity());
				}
			}
		}
	}
	
	/**
	 * Checks if damager is blacklisted.
	 * Ensures damager is Player and not NPC.
	 * Kills target mob/NPC if objective exists
	 * 
	 * @param damager the attacking entity
	 * @param target the entity being attacked
	 * @since 3.1.4
	 */
	public void killMob(Entity damager, Entity target) {
		if (plugin.checkQuester(damager.getUniqueId()) == true) {
			return;
		}
		if (damager instanceof Player) {
			if (plugin.citizens != null) {
				if (CitizensAPI.getNPCRegistry().isNPC(target)) {
					Quester quester = plugin.getQuester(damager.getUniqueId());
					for (Quest quest : quester.currentQuests.keySet()) {
						if (quester.hasObjective(quest, "killNPC")) {
							quester.killNPC(quest, CitizensAPI.getNPCRegistry().getNPC(target));
						}
					}
					return;
				}
			}
			Quester quester = plugin.getQuester(damager.getUniqueId());
			for (Quest quest : quester.currentQuests.keySet()) {
				if (quester.hasObjective(quest, "killMob")) {
					quester.killMob(quest, target.getLocation(), target.getType());
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
					ProjectileSource source = projectile.getShooter();
					if (source instanceof Entity) {
						killPlayer((Entity)source, evt.getEntity());
					}
				} else if (damager instanceof TNTPrimed) {
					TNTPrimed tnt = (TNTPrimed) damager;
					Entity source = tnt.getSource();
					if (source.isValid()) {
						killPlayer(source, evt.getEntity());
					}
				} else if (damager instanceof Wolf) {
					Wolf wolf = (Wolf) damager;
					if (wolf.isTamed()) {
						Quester quester = plugin.getQuester(wolf.getOwner().getUniqueId());
						killPlayer(quester.getPlayer(), evt.getEntity());
					}
				} else {
					killPlayer(damager, evt.getEntity());
				}
			}
		}
			
		Player target = evt.getEntity();
		if (plugin.checkQuester(target.getUniqueId()) == false) {
			Quester quester = plugin.getQuester(target.getUniqueId());
			for (Quest quest : quester.currentQuests.keySet()) {
				Stage stage = quester.getCurrentStage(quest);
				if (stage != null && stage.deathEvent != null) {
					quester.getCurrentStage(quest).deathEvent.fire(quester, quest);
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
	 * Checks if damager is blacklisted
	 * Ensures damager is Player and not NPC
	 * Kills target Player if objective exists
	 * 
	 * @param damager the attacking entity
	 * @param target the entity being attacked
	 * @since 3.1.4
	 */
	public void killPlayer(Entity damager, Entity target) {
		if (plugin.checkQuester(damager.getUniqueId()) == true) {
			return;
		}
		//Ensure damager is player AND not an NPC
		if (damager instanceof Player && !CitizensAPI.getNPCRegistry().isNPC(damager)) {
			//If target is player AND not an NPC...
			if (target instanceof Player && !CitizensAPI.getNPCRegistry().isNPC(target)) {
				Quester quester = plugin.getQuester(damager.getUniqueId());
				for (Quest quest : quester.currentQuests.keySet()) {
					if (quester.hasObjective(quest, "killPlayer")) {
						quester.killPlayer(quest, (Player)target);
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerFish(PlayerFishEvent evt) {
		Player player = evt.getPlayer();
		if (plugin.checkQuester(player.getUniqueId()) == false) {
			Quester quester = plugin.getQuester(player.getUniqueId());
			for (Quest quest : quester.currentQuests.keySet()) {
				if (quester.hasObjective(quest, "catchFish") && evt.getState().equals(State.CAUGHT_FISH)) {
					quester.catchFish(quest);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
		Player player = event.getPlayer();
		if (plugin.checkQuester(player.getUniqueId()) == false) {
			Quester quester = plugin.getQuester(player.getUniqueId());
			quester.findCompassTarget();
		}
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		if (plugin.checkQuester(player.getUniqueId()) == false) {
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
		if (plugin.checkQuester(evt.getPlayer().getUniqueId()) == false) {
			Quester quester = new Quester(plugin);
			quester.id = evt.getPlayer().getUniqueId();
			if (new File(plugin.getDataFolder(), "data" + File.separator + quester.id + ".yml").exists()) {
				quester.loadData();
			} else if (Quests.genFilesOnJoin) {
				quester.saveData();
			}
			plugin.questers.put(evt.getPlayer().getUniqueId(), quester);
			if (Quests.useCompass) {
				quester.resetCompass();
			}
			for (String s : quester.completedQuests) {
				Quest q = plugin.getQuest(s);
				if (q != null) {
					if (quester.completedTimes.containsKey(q.name) == false && q.redoDelay > -1) {
						quester.completedTimes.put(q.name, System.currentTimeMillis());
					}
				}
			}
			for (Quest quest : quester.currentQuests.keySet()) {
				quester.checkQuest(quest);
			}
			for (Quest quest : quester.currentQuests.keySet()) {
				if (quester.getCurrentStage(quest).delay > -1) {
					quester.startStageTimer(quest);
				}
			}
			if (quester.hasJournal)
				quester.updateJournal();
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent evt) {
		if (plugin.checkQuester(evt.getPlayer().getUniqueId()) == false) {
			Quester quester = plugin.getQuester(evt.getPlayer().getUniqueId());
			for (Quest quest : quester.currentQuests.keySet()) {
				Stage currentStage = quester.getCurrentStage(quest);
				if (currentStage == null) {
					plugin.getLogger().severe("currentStage was null for " + quester.id.toString() + " on quit");
					continue;
				}
				if (currentStage.delay > -1) {
					quester.stopStageTimer(quest);
				}
				if (currentStage.disconnectEvent != null) {
					currentStage.disconnectEvent.fire(quester, quest);
				}
			}
			for (Integer timerId : quester.timers.keySet()) {
				plugin.getServer().getScheduler().cancelTask(timerId);
				quester.timers.get(timerId).failQuest(quester);
				quester.timers.remove(timerId);
			}

			if (quester.hasData()) {
				quester.saveData();
			}
			if (plugin.questFactory.selectingNPCs.contains(evt.getPlayer())) {
				plugin.questFactory.selectingNPCs.remove(evt.getPlayer());
			}
			plugin.questers.remove(quester.id);
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent evt) {
		if (evt.getFrom().getBlock().equals(evt.getTo().getBlock())) {
			return;
		}
		if (plugin.checkQuester(evt.getPlayer().getUniqueId()) == false) {
			boolean isPlayer = true;
			if (plugin.citizens != null) {
				if (CitizensAPI.getNPCRegistry().isNPC(evt.getPlayer())) {
					isPlayer = false;
				}
			}
			if (isPlayer) {
				if (plugin.getQuester(evt.getPlayer().getUniqueId()) != null) {
					Quester quester = plugin.getQuester(evt.getPlayer().getUniqueId());
					for (Quest quest : quester.currentQuests.keySet()) {
						if (quester.hasObjective(quest, "reachLocation")) {
							quester.reachLocation(quest, evt.getTo());
						}
					}
				}
			}
		}
	}
}