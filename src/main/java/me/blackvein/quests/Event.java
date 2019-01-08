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
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import me.blackvein.quests.timers.EventTimer;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;

public class Event {

	private Quests plugin;
	private String name = "";
	protected String message = null;
	protected boolean clearInv = false;
	protected boolean failQuest = false;
	protected LinkedList<Location> explosions = new LinkedList<Location>();
	protected Map<Location, Effect> effects = new HashMap<Location, Effect>();
	protected LinkedList<ItemStack> items = new LinkedList<ItemStack>();
	protected World stormWorld = null;
	protected int stormDuration = 0;
	protected World thunderWorld = null;
	protected int thunderDuration = 0;
	protected int timer = 0;
	protected boolean cancelTimer = false;
	protected LinkedList<QuestMob> mobSpawns = new LinkedList<QuestMob>() {

		private static final long serialVersionUID = -761974607799449780L;

		@Override
		public boolean equals(Object o) {
			if (o instanceof LinkedList) {
				@SuppressWarnings("unchecked")
				LinkedList<QuestMob> other = (LinkedList<QuestMob>) o;
				if (size() != other.size()) {
					return false;
				}
				for (int i = 0; i < size(); i++) {
					if (get(i).equals(other.get(i)) == false) {
						return false;
					}
				}
			}
			return false;
		}
	};
	protected LinkedList<Location> lightningStrikes = new LinkedList<Location>();
	protected LinkedList<String> commands = new LinkedList<String>();
	protected LinkedList<PotionEffect> potionEffects = new LinkedList<PotionEffect>();
	protected int hunger = -1;
	protected int saturation = -1;
	protected float health = -1;
	protected Location teleport;
	protected String book = "";

	public Event(final Quests plugin) {
		this.plugin = plugin;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isClearInv() {
		return clearInv;
	}

	public void setClearInv(boolean clearInv) {
		this.clearInv = clearInv;
	}

	public boolean isFailQuest() {
		return failQuest;
	}

	public void setFailQuest(boolean failQuest) {
		this.failQuest = failQuest;
	}

	public LinkedList<Location> getExplosions() {
		return explosions;
	}

	public void setExplosions(LinkedList<Location> explosions) {
		this.explosions = explosions;
	}

	public Map<Location, Effect> getEffects() {
		return effects;
	}

	public void setEffects(Map<Location, Effect> effects) {
		this.effects = effects;
	}

	public LinkedList<ItemStack> getItems() {
		return items;
	}

	public void setItems(LinkedList<ItemStack> items) {
		this.items = items;
	}

	public World getStormWorld() {
		return stormWorld;
	}

	public void setStormWorld(World stormWorld) {
		this.stormWorld = stormWorld;
	}

	public int getStormDuration() {
		return stormDuration;
	}

	public void setStormDuration(int stormDuration) {
		this.stormDuration = stormDuration;
	}

	public World getThunderWorld() {
		return thunderWorld;
	}

	public void setThunderWorld(World thunderWorld) {
		this.thunderWorld = thunderWorld;
	}

	public int getThunderDuration() {
		return thunderDuration;
	}

	public void setThunderDuration(int thunderDuration) {
		this.thunderDuration = thunderDuration;
	}

	public int getTimer() {
		return timer;
	}

	public void setTimer(int timer) {
		this.timer = timer;
	}

	public boolean isCancelTimer() {
		return cancelTimer;
	}

	public void setCancelTimer(boolean cancelTimer) {
		this.cancelTimer = cancelTimer;
	}

	public LinkedList<QuestMob> getMobSpawns() {
		return mobSpawns;
	}

	public void setMobSpawns(LinkedList<QuestMob> mobSpawns) {
		this.mobSpawns = mobSpawns;
	}

	public LinkedList<Location> getLightningStrikes() {
		return lightningStrikes;
	}

	public void setLightningStrikes(LinkedList<Location> lightningStrikes) {
		this.lightningStrikes = lightningStrikes;
	}

	public LinkedList<String> getCommands() {
		return commands;
	}

	public void setCommands(LinkedList<String> commands) {
		this.commands = commands;
	}

	public LinkedList<PotionEffect> getPotionEffects() {
		return potionEffects;
	}

	public void setPotionEffects(LinkedList<PotionEffect> potionEffects) {
		this.potionEffects = potionEffects;
	}

	public int getHunger() {
		return hunger;
	}

	public void setHunger(int hunger) {
		this.hunger = hunger;
	}

	public int getSaturation() {
		return saturation;
	}

	public void setSaturation(int saturation) {
		this.saturation = saturation;
	}

	public float getHealth() {
		return health;
	}

	public void setHealth(float health) {
		this.health = health;
	}

	public Location getTeleport() {
		return teleport;
	}

	public void setTeleport(Location teleport) {
		this.teleport = teleport;
	}

	public String getBook() {
		return book;
	}

	public void setBook(String book) {
		this.book = book;
	}

	public void fire(Quester quester, Quest quest) {
		Player player = quester.getPlayer();
		if (message != null) {
			player.sendMessage(plugin.parseString(message, quest, player));
		}
		if (clearInv == true) {
			player.getInventory().clear();
		}
		if (explosions.isEmpty() == false) {
			for (Location l : explosions) {
				l.getWorld().createExplosion(l, 4F, false);
			}
		}
		if (effects.isEmpty() == false) {
			for (Location l : effects.keySet()) {
				l.getWorld().playEffect(l, effects.get(l), 1);
			}
		}
		if (items.isEmpty() == false) {
			for (ItemStack is : items) {
				try {
					Quests.addItem(player, is);
				} catch (Exception e) {
					plugin.getLogger().severe("Unable to add null item to inventory of " 
							+ player.getName() + " during quest " + quest.getName() + " event " + name);
					player.sendMessage(ChatColor.RED + "Quests encountered a problem with an item. "
							+ "Please contact an administrator.");
				}
			}
		}
		if (stormWorld != null) {
			stormWorld.setStorm(true);
			stormWorld.setWeatherDuration(stormDuration);
		}
		if (thunderWorld != null) {
			thunderWorld.setThundering(true);
			thunderWorld.setThunderDuration(thunderDuration);
		}
		if (mobSpawns.isEmpty() == false) {
			for (QuestMob questMob : mobSpawns) {
				questMob.spawn();
			}
		}
		if (lightningStrikes.isEmpty() == false) {
			for (Location l : lightningStrikes) {
				l.getWorld().strikeLightning(l);
			}
		}
		if (commands.isEmpty() == false) {
			for (String s : commands) {
				plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), s.replaceAll("<player>", quester.getPlayer().getName()));
			}
		}
		if (potionEffects.isEmpty() == false) {
			for (PotionEffect p : potionEffects) {
				player.addPotionEffect(p);
			}
		}
		if (hunger != -1) {
			player.setFoodLevel(hunger);
		}
		if (saturation != -1) {
			player.setSaturation(saturation);
		}
		if (health != -1) {
			player.setHealth(health);
		}
		if (teleport != null) {
			player.teleport(teleport);
		}
		if (book != null) {
			if (!book.isEmpty()) {
				if (plugin.getDependencies().getCitizensBooksApi() != null) {
					if (plugin.getDependencies().getCitizensBooksApi().hasFilter(book)) {
		                plugin.getDependencies().getCitizensBooksApi().openBook(player, plugin.getDependencies().getCitizensBooksApi().getFilter(book));
		            }
				}
			}
        }
		if (failQuest == true) {
			quest.failQuest(quester);
		}
		if (timer > 0) {
			player.sendMessage(Lang.get(player, "timerStart").replaceAll("<time>", String.valueOf(timer)));
			if (timer > 60) {
				quester.timers.put(new EventTimer(quester, quest, 60, false)
						.runTaskLaterAsynchronously(plugin, (timer-60)*20).getTaskId(), quest);
			}
			if (timer > 30) {
				quester.timers.put(new EventTimer(quester, quest, 30, false)
						.runTaskLaterAsynchronously(plugin, (timer-30)*20).getTaskId(), quest);
			}
			if (timer > 10) {
				quester.timers.put(new EventTimer(quester, quest, 10, false)
						.runTaskLaterAsynchronously(plugin, (timer-10)*20).getTaskId(), quest);
			}
			if (timer > 5) {
				quester.timers.put(new EventTimer(quester, quest, 5, false)
						.runTaskLaterAsynchronously(plugin, (timer-5)*20).getTaskId(), quest);
			}
			if (timer > 4) {
				quester.timers.put(new EventTimer(quester, quest, 4, false)
						.runTaskLaterAsynchronously(plugin, (timer-4)*20).getTaskId(), quest);
			}
			if (timer > 3) {
				quester.timers.put(new EventTimer(quester, quest, 3, false)
						.runTaskLaterAsynchronously(plugin, (timer-3)*20).getTaskId(), quest);
			}
			if (timer > 2) {
				quester.timers.put(new EventTimer(quester, quest, 2, false)
						.runTaskLaterAsynchronously(plugin, (timer-2)*20).getTaskId(), quest);
			}
			if (timer > 1) {
				quester.timers.put(new EventTimer(quester, quest, 1, false)
						.runTaskLaterAsynchronously(plugin, (timer-1)*20).getTaskId(), quest);
			}
			quester.timers.put(new EventTimer(quester, quest, 0, true)
					.runTaskLaterAsynchronously(plugin, timer*20).getTaskId(), quest);
		}
		if (cancelTimer) {
			for (Map.Entry<Integer, Quest> entry : quester.timers.entrySet()) {
				if (entry.getValue().getName().equals(quest.getName())) {
					plugin.getServer().getScheduler().cancelTask(entry.getKey());
					quester.timers.remove(entry.getKey());
				}
			}
		}
	}

	public static Event loadEvent(String name, Quests plugin) {
		if (name == null || plugin == null) {
			return null;
		}
		Event event = new Event(plugin);
		FileConfiguration data = new YamlConfiguration();
		try {
			data.load(new File(plugin.getDataFolder(), "events.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
		String eventKey = "events." + name + ".";
		event.name = name;
		if (data.contains(eventKey + "message")) {
			event.message = Quests.parseString(data.getString(eventKey + "message"));
		}
		if (data.contains(eventKey + "open-book")) {
		    event.book = data.getString(eventKey + "open-book");
        }
		if (data.contains(eventKey + "clear-inventory")) {
			if (data.isBoolean(eventKey + "clear-inventory")) {
				event.clearInv = data.getBoolean(eventKey + "clear-inventory");
			} else {
				plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "clear-inventory: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a true/false value!");
				return null;
			}
		}
		if (data.contains(eventKey + "fail-quest")) {
			if (data.isBoolean(eventKey + "fail-quest")) {
				event.failQuest = data.getBoolean(eventKey + "fail-quest");
			} else {
				plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "fail-quest: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a true/false value!");
				return null;
			}
		}
		if (data.contains(eventKey + "explosions")) {
			if (Quests.checkList(data.getList(eventKey + "explosions"), String.class)) {
				for (String s : data.getStringList(eventKey + "explosions")) {
					Location loc = Quests.getLocation(s);
					if (loc == null) {
						plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + loc + ChatColor.GOLD + " inside " + ChatColor.GREEN + "explosions: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not in proper location format!");
						plugin.getLogger().severe(ChatColor.GOLD + "[Quests] Proper location format is: \"WorldName x y z\"");
						return null;
					}
					event.explosions.add(loc);
				}
			} else {
				plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "explosions: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a list of locations!");
				return null;
			}
		}
		if (data.contains(eventKey + "effects")) {
			if (Quests.checkList(data.getList(eventKey + "effects"), String.class)) {
				if (data.contains(eventKey + "effect-locations")) {
					if (Quests.checkList(data.getList(eventKey + "effect-locations"), String.class)) {
						List<String> effectList = data.getStringList(eventKey + "effects");
						List<String> effectLocs = data.getStringList(eventKey + "effect-locations");
						for (String s : effectList) {
							Effect effect = Quests.getEffect(s);
							Location l = Quests.getLocation(effectLocs.get(effectList.indexOf(s)));
							if (effect == null) {
								plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + s + ChatColor.GOLD + " inside " + ChatColor.GREEN + "effects: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a valid effect name!");
								return null;
							}
							if (l == null) {
								plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + effectLocs.get(effectList.indexOf(s)) + ChatColor.GOLD + " inside " + ChatColor.GREEN + "effect-locations: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not in proper location format!");
								plugin.getLogger().severe(ChatColor.GOLD + "[Quests] Proper location format is: \"WorldName x y z\"");
								return null;
							}
							event.effects.put(l, effect);
						}
					} else {
						plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "effect-locations: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a list of locations!");
						return null;
					}
				} else {
					plugin.getLogger().severe(ChatColor.GOLD + "[Quests] Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is missing " + ChatColor.RED + "effect-locations:");
					return null;
				}
			} else {
				plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "effects: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a list of effects!");
				return null;
			}
		}
		if (data.contains(eventKey + "items")) {
			if (Quests.checkList(data.getList(eventKey + "items"), String.class)) {
				List<ItemStack> eventItems = new LinkedList<ItemStack>();
				for (String s : data.getStringList(eventKey + "items")) {
					try {
						eventItems.add(ItemUtil.readItemStack(s));
					} catch (Exception e) {
						plugin.getLogger().severe(ChatColor.GOLD + "[Quests] \"" + ChatColor.RED + s + ChatColor.GOLD + "\" inside " + ChatColor.GREEN + " items: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not formatted properly!");
						return null;
					}
				}
				event.items.addAll(eventItems);
			} else {
				plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "items: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a list of items!");
				return null;
			}
		}
		if (data.contains(eventKey + "storm-world")) {
			World w = plugin.getServer().getWorld(data.getString(eventKey + "storm-world"));
			if (w == null) {
				plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "storm-world: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a valid World name!");
				return null;
			}
			if (data.contains(eventKey + "storm-duration")) {
				if (data.getInt(eventKey + "storm-duration", -999) != -999) {
					event.stormDuration = data.getInt(eventKey + "storm-duration") * 1000;
				} else {
					plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "storm-duration: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a number!");
					return null;
				}
				event.stormWorld = w;
			} else {
				plugin.getLogger().severe(ChatColor.GOLD + "[Quests] Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is missing " + ChatColor.RED + "storm-duration:");
				return null;
			}
		}
		if (data.contains(eventKey + "thunder-world")) {
			World w = plugin.getServer().getWorld(data.getString(eventKey + "thunder-world"));
			if (w == null) {
				plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "thunder-world: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a valid World name!");
				return null;
			}
			if (data.contains(eventKey + "thunder-duration")) {
				if (data.getInt(eventKey + "thunder-duration", -999) != -999) {
					event.thunderDuration = data.getInt(eventKey + "thunder-duration");
				} else {
					plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "thunder-duration: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a number!");
					return null;
				}
				event.thunderWorld = w;
			} else {
				plugin.getLogger().severe(ChatColor.GOLD + "[Quests] Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is missing " + ChatColor.RED + "thunder-duration:");
				return null;
			}
		}
		if (data.contains(eventKey + "mob-spawns")) {
			ConfigurationSection section = data.getConfigurationSection(eventKey + "mob-spawns");
			// is a mob, the keys are just a number or something.
			for (String s : section.getKeys(false)) {
				String mobName = section.getString(s + ".name");
				Location spawnLocation = Quests.getLocation(section.getString(s + ".spawn-location"));
				EntityType type = Quests.getMobType(section.getString(s + ".mob-type"));
				Integer mobAmount = section.getInt(s + ".spawn-amounts");
				if (spawnLocation == null) {
					plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + s + ChatColor.GOLD + " inside " + ChatColor.GREEN + " mob-spawn-locations: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not in proper location format!");
					plugin.getLogger().severe(ChatColor.GOLD + "[Quests] Proper location format is: \"WorldName x y z\"");
					return null;
				}
				if (type == null) {
					plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + section.getString(s + ".mob-type") + ChatColor.GOLD + " inside " + ChatColor.GREEN + " mob-spawn-types: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a valid mob name!");
					return null;
				}
				ItemStack[] inventory = new ItemStack[5];
				Float[] dropChances = new Float[5];
				inventory[0] = ItemUtil.readItemStack(section.getString(s + ".held-item"));
				dropChances[0] = (float) section.getDouble(s + ".held-item-drop-chance");
				inventory[1] = ItemUtil.readItemStack(section.getString(s + ".boots"));
				dropChances[1] = (float) section.getDouble(s + ".boots-drop-chance");
				inventory[2] = ItemUtil.readItemStack(section.getString(s + ".leggings"));
				dropChances[2] = (float) section.getDouble(s + ".leggings-drop-chance");
				inventory[3] = ItemUtil.readItemStack(section.getString(s + ".chest-plate"));
				dropChances[3] = (float) section.getDouble(s + ".chest-plate-drop-chance");
				inventory[4] = ItemUtil.readItemStack(section.getString(s + ".helmet"));
				dropChances[4] = (float) section.getDouble(s + ".helmet-drop-chance");
				QuestMob questMob = new QuestMob(type, spawnLocation, mobAmount);
				questMob.setInventory(inventory);
				questMob.setDropChances(dropChances);
				questMob.setName(mobName);
				event.mobSpawns.add(questMob);
			}
		}
		if (data.contains(eventKey + "lightning-strikes")) {
			if (Quests.checkList(data.getList(eventKey + "lightning-strikes"), String.class)) {
				for (String s : data.getStringList(eventKey + "lightning-strikes")) {
					Location loc = Quests.getLocation(s);
					if (loc == null) {
						plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + s + ChatColor.GOLD + " inside " + ChatColor.GREEN + " lightning-strikes: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not in proper location format!");
						plugin.getLogger().severe(ChatColor.GOLD + "[Quests] Proper location format is: \"WorldName x y z\"");
						return null;
					}
					event.lightningStrikes.add(loc);
				}
			} else {
				plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "lightning-strikes: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a list of locations!");
				return null;
			}
		}
		if (data.contains(eventKey + "commands")) {
			if (Quests.checkList(data.getList(eventKey + "commands"), String.class)) {
				for (String s : data.getStringList(eventKey + "commands")) {
					if (s.startsWith("/")) {
						s = s.replaceFirst("/", "");
					}
					event.commands.add(s);
				}
			} else {
				plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "commands: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a list of commands!");
				return null;
			}
		}
		if (data.contains(eventKey + "potion-effect-types")) {
			if (Quests.checkList(data.getList(eventKey + "potion-effect-types"), String.class)) {
				if (data.contains(eventKey + "potion-effect-durations")) {
					if (Quests.checkList(data.getList(eventKey + "potion-effect-durations"), Integer.class)) {
						if (data.contains(eventKey + "potion-effect-amplifiers")) {
							if (Quests.checkList(data.getList(eventKey + "potion-effect-amplifiers"), Integer.class)) {
								List<String> types = data.getStringList(eventKey + "potion-effect-types");
								List<Integer> durations = data.getIntegerList(eventKey + "potion-effect-durations");
								List<Integer> amplifiers = data.getIntegerList(eventKey + "potion-effect-amplifiers");
								for (String s : types) {
									PotionEffect effect = Quests.getPotionEffect(s, durations.get(types.indexOf(s)), amplifiers.get(types.indexOf(s)));
									if (effect == null) {
										plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + s + ChatColor.GOLD + " inside " + ChatColor.GREEN + " lightning-strikes: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a valid potion effect name!");
										return null;
									}
									event.potionEffects.add(effect);
								}
							} else {
								plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "potion-effect-amplifiers: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a list of numbers!");
								return null;
							}
						} else {
							plugin.getLogger().severe(ChatColor.GOLD + "[Quests] Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is missing " + ChatColor.RED + "potion-effect-amplifiers:");
							return null;
						}
					} else {
						plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "potion-effect-durations: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a list of numbers!");
						return null;
					}
				} else {
					plugin.getLogger().severe(ChatColor.GOLD + "[Quests] Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is missing " + ChatColor.RED + "potion-effect-durations:");
					return null;
				}
			} else {
				plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "potion-effect-types: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a list of potion effects!");
				return null;
			}
		}
		if (data.contains(eventKey + "hunger")) {
			if (data.getInt(eventKey + "hunger", -999) != -999) {
				event.hunger = data.getInt(eventKey + "hunger");
			} else {
				plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "hunger: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a number!");
				return null;
			}
		}
		if (data.contains(eventKey + "saturation")) {
			if (data.getInt(eventKey + "saturation", -999) != -999) {
				event.saturation = data.getInt(eventKey + "saturation");
			} else {
				plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "saturation: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a number!");
				return null;
			}
		}
		if (data.contains(eventKey + "health")) {
			if (data.getInt(eventKey + "health", -999) != -999) {
				event.health = data.getInt(eventKey + "health");
			} else {
				plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "health: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a number!");
				return null;
			}
		}
		if (data.contains(eventKey + "teleport-location")) {
			if (data.isString(eventKey + "teleport-location")) {
				Location l = Quests.getLocation(data.getString(eventKey + "teleport-location"));
				if (l == null) {
					plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + data.getString(eventKey + "teleport-location") + ChatColor.GOLD + "for " + ChatColor.GREEN + " teleport-location: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not in proper location format!");
					plugin.getLogger().severe(ChatColor.GOLD + "[Quests] Proper location format is: \"WorldName x y z\"");
					return null;
				}
				event.teleport = l;
			} else {
				plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "teleport-location: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a location!");
				return null;
			}
		}
		if (data.contains(eventKey + "timer")) {
			if (data.isInt(eventKey + "timer")) {
				event.timer = data.getInt(eventKey + "timer");
			} else {
				plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "timer: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a number!");
				return null;
			}
		}
		if (data.contains(eventKey + "cancel-timer")) {
			if (data.isBoolean(eventKey + "cancel-timer")) {
				event.cancelTimer = data.getBoolean(eventKey + "cancel-timer");
			} else {
				plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "cancel-timer: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a boolean!");
				return null;
			}
		}
		return event;
	}
}
