package me.blackvein.quests;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.QuestMob;

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

public class Event {
	
	Quests plugin;
    String name = "";
    String message = null;
    boolean clearInv = false;
    boolean failQuest = false;
    LinkedList<Location> explosions = new LinkedList<Location>();
    Map<Location, Effect> effects = new HashMap<Location, Effect>();
    LinkedList<ItemStack> items = new LinkedList<ItemStack>();
    World stormWorld = null;
    int stormDuration = 0;
    World thunderWorld = null;
    int thunderDuration = 0;
    public LinkedList<QuestMob> mobSpawns = new LinkedList<QuestMob>() {

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
    LinkedList<Location> lightningStrikes = new LinkedList<Location>();
    LinkedList<String> commands = new LinkedList<String>();
    LinkedList<PotionEffect> potionEffects = new LinkedList<PotionEffect>();
    int hunger = -1;
    int saturation = -1;
    float health = -1;
    Location teleport;

    public int hashCode() {
    	assert false : "hashCode not designed";
    	return 42; // any arbitrary constant will do
    }
    
    @Override
    public boolean equals(Object o) {

        if (o instanceof Event) {

            Event other = (Event) o;

            if (other.name.equals(name) == false) {
                return false;
            }

            if (other.message != null && message != null) {
                if (other.message.equals(message) == false) {
                    return false;
                }
            } else if (other.message != null && message == null) {
                return false;
            } else if (other.message == null && message != null) {
                return false;
            }

            if (other.clearInv != clearInv) {
                return false;
            }

            if (other.failQuest != failQuest) {
                return false;
            }

            if (other.explosions.equals(explosions) == false) {
                return false;
            }

            if (other.effects.entrySet().equals(effects.entrySet()) == false) {
                return false;
            }

            if (other.items.equals(items) == false) {
                return false;
            }

            if (other.stormWorld != null && stormWorld != null) {
                if (other.stormWorld.equals(stormWorld) == false) {
                    return false;
                }
            } else if (other.stormWorld != null && stormWorld == null) {
                return false;
            } else if (other.stormWorld == null && stormWorld != null) {
                return false;
            }

            if (other.stormDuration != stormDuration) {
                return false;
            }

            if (other.thunderWorld != null && thunderWorld != null) {
                if (other.thunderWorld.equals(thunderWorld) == false) {
                    return false;
                }
            } else if (other.thunderWorld != null && thunderWorld == null) {
                return false;
            } else if (other.thunderWorld == null && thunderWorld != null) {
                return false;
            }

            if (other.thunderDuration != thunderDuration) {
                return false;
            }

            for (QuestMob qm : mobSpawns) {

                if (qm.equals(other.mobSpawns.get(mobSpawns.indexOf(qm))) == false) {
                    return false;
                }

            }

            if (other.lightningStrikes.equals(lightningStrikes) == false) {
                return false;
            }

            if (other.commands.equals(commands) == false) {
                return false;
            }

            if (other.potionEffects.equals(potionEffects) == false) {
                return false;
            }

            if (other.hunger != hunger) {
                return false;
            }

            if (other.saturation != saturation) {
                return false;
            }

            if (other.health != health) {
                return false;
            }

            if (other.teleport != null && teleport != null) {
                if (other.teleport.equals(teleport) == false) {
                    return false;
                }
            } else if (other.teleport != null && teleport == null) {
                return false;
            } else if (other.teleport == null && teleport != null) {
                return false;
            }

        }

        return true;
    }

    public String getName() {

        return name;

    }

    public void fire(Quester quester, Quest quest) {

        Player player = quester.getPlayer();

        if (message != null) {
            player.sendMessage(Quests.parseString(message, quest));
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
                Quests.addItem(player, is);
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
                quester.plugin.getServer().dispatchCommand(quester.plugin.getServer().getConsoleSender(), s.replaceAll("<player>", quester.getPlayer().getName()));
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

        if (failQuest == true) {

            quest.failQuest(quester);

        }

    }

    public static Event loadEvent(String name, Quests plugin) {

        if (name == null || plugin == null) {
            return null;
        }

        Event event = new Event();

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
            event.message = data.getString(eventKey + "message");
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

            //is a mob, the keys are just a number or something.
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
                questMob.inventory = inventory;
                questMob.dropChances = dropChances;
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

        return event;

    }
}
