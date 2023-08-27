package me.pikamug.quests.storage.implementation.file;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.actions.Action;
import me.pikamug.quests.actions.BukkitAction;
import me.pikamug.quests.entity.BukkitQuestMob;
import me.pikamug.quests.entity.QuestMob;
import me.pikamug.quests.exceptions.ActionFormatException;
import me.pikamug.quests.storage.implementation.ActionStorageImpl;
import me.pikamug.quests.util.BukkitConfigUtil;
import me.pikamug.quests.util.BukkitItemUtil;
import me.pikamug.quests.util.BukkitMiscUtil;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class BukkitActionYamlStorage implements ActionStorageImpl {

    private final BukkitQuestsPlugin plugin;

    public BukkitActionYamlStorage(final BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public BukkitQuestsPlugin getPlugin() {
        return plugin;
    }

    @Override
    public String getImplementationName() {
        return "YAML";
    }

    @Override
    public void init() {
        final YamlConfiguration config = new YamlConfiguration();
        final File legacyFile = new File(plugin.getDataFolder(), "events.yml");
        final File actionsFile = new File(plugin.getDataFolder(), "actions.yml");
        // Using #isFile because #exists and #renameTo can return false positives
        if (legacyFile.isFile()) {
            try {
                if (legacyFile.renameTo(actionsFile)) {
                    plugin.getLogger().log(Level.INFO, "Renamed legacy events.yml to actions.yml");
                }
                if (actionsFile.isFile()) {
                    plugin.getLogger().log(Level.INFO, "Successfully deleted legacy events.yml");
                    if (legacyFile.delete()) {
                        plugin.getLogger().log(Level.INFO, "Done!");
                    }
                }
            } catch (final Exception e) {
                plugin.getLogger().log(Level.WARNING, "Unable to convert events.yml to actions.yml");
                e.printStackTrace();
            }
        }
        if (actionsFile.length() != 0) {
            try {
                if (actionsFile.isFile()) {
                    config.load(actionsFile);
                } else {
                    config.load(legacyFile);
                }
            } catch (final IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
            ConfigurationSection sec = config.getConfigurationSection("actions");
            if (sec == null) {
                plugin.getLogger().log(Level.INFO,
                        "Could not find section \"actions\" from actions.yml. Trying legacy \"events\"...");
                sec = config.getConfigurationSection("events");
            }
            if (sec != null) {
                for (final String s : sec.getKeys(false)) {
                    BukkitAction action = null;
                    try {
                        action = loadAction(s);
                    } catch (final ActionFormatException e) {
                        e.printStackTrace();
                    }
                    if (action != null) {
                        final Collection<Action> actions = plugin.getLoadedActions();
                        actions.add(action);
                        plugin.setLoadedActions(actions);
                    } else {
                        plugin.getLogger().log(Level.SEVERE, "Failed to load Action \"" + s + "\". Skipping.");
                    }
                }
            } else {
                plugin.getLogger().log(Level.SEVERE, "Could not find beginning section from actions.yml. Skipping.");
            }
        } else {
            plugin.getLogger().log(Level.WARNING, "Empty file actions.yml was not loaded.");
        }
    }

    @Override
    public void close() {
    }

    @SuppressWarnings({ "unchecked"})
    @Override
    public BukkitAction loadAction(final String name) throws ActionFormatException {
        if (name == null) {
            return null;
        }
        final File legacy = new File(plugin.getDataFolder(), "events.yml");
        final File actions = new File(plugin.getDataFolder(), "actions.yml");
        final FileConfiguration data = new YamlConfiguration();
        try {
            if (actions.isFile()) {
                data.load(actions);
            } else {
                data.load(legacy);
            }
        } catch (final IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        final String legacyName = "events." + name;
        String actionKey = "actions." + name + ".";
        if (data.contains(legacyName)) {
            actionKey = legacyName + ".";
        }
        final BukkitAction action = new BukkitAction(plugin);
        action.setName(name);
        if (data.contains(actionKey + "message")) {
            action.setMessage(BukkitConfigUtil.parseString(data.getString(actionKey + "message")));
        }
        if (data.contains(actionKey + "open-book")) {
            action.setBook(data.getString(actionKey + "open-book"));
        }
        if (data.contains(actionKey + "clear-inventory")) {
            if (data.isBoolean(actionKey + "clear-inventory")) {
                action.setClearInv(data.getBoolean(actionKey + "clear-inventory"));
            } else {
                throw new ActionFormatException("'clear-inventory' is not a true/false value", actionKey);
            }
        }
        if (data.contains(actionKey + "fail-quest")) {
            if (data.isBoolean(actionKey + "fail-quest")) {
                action.setFailQuest(data.getBoolean(actionKey + "fail-quest"));
            } else {
                throw new ActionFormatException("'fail-quest' is not a true/false value", actionKey);
            }
        }
        if (data.contains(actionKey + "explosions")) {
            if (BukkitConfigUtil.checkList(data.getList(actionKey + "explosions"), String.class)) {
                final LinkedList<Location> explosions = new LinkedList<>();
                for (final String s : data.getStringList(actionKey + "explosions")) {
                    final Location loc = BukkitConfigUtil.getLocation(s);
                    if (loc == null) {
                        throw new ActionFormatException("'explosions' is not in proper \"WorldName x y z\" format",
                                actionKey);
                    }
                    explosions.add(loc);
                }
                action.setExplosions(explosions);
            } else {
                throw new ActionFormatException("'explosions' is not a list of locations", actionKey);
            }
        }
        if (data.contains(actionKey + "effects")) {
            if (BukkitConfigUtil.checkList(data.getList(actionKey + "effects"), String.class)) {
                if (data.contains(actionKey + "effect-locations")) {
                    if (BukkitConfigUtil.checkList(data.getList(actionKey + "effect-locations"), String.class)) {
                        final List<String> effectList = data.getStringList(actionKey + "effects");
                        final List<String> effectLocs = data.getStringList(actionKey + "effect-locations");
                        final Map<Location, Effect> effects = new HashMap<>();
                        for (final String s : effectList) {
                            final Effect effect;
                            try {
                                effect = Effect.valueOf(s.toUpperCase());
                            } catch (final IllegalArgumentException e) {
                                throw new ActionFormatException(s + " is not a valid effect name",
                                        actionKey);
                            }
                            final Location l = BukkitConfigUtil.getLocation(effectLocs.get(effectList.indexOf(s)));
                            if (l == null) {
                                throw new ActionFormatException("'effect-locations' is not in proper \"WorldName x y z\""
                                        + "format", actionKey);
                            }
                            effects.put(l, effect);
                        }
                        action.setEffects(effects);
                    } else {
                        throw new ActionFormatException("'effect-locations' is not a list of locations", actionKey);
                    }
                } else {
                    throw new ActionFormatException("'effect-locations' is missing", actionKey);
                }
            } else {
                throw new ActionFormatException("'effects' is not a list of effects", actionKey);
            }
        }
        if (data.contains(actionKey + "items")) {
            final LinkedList<ItemStack> temp = new LinkedList<>();
            final List<ItemStack> stackList = (List<ItemStack>) data.get(actionKey + "items");
            if (BukkitConfigUtil.checkList(stackList, ItemStack.class)) {
                for (final ItemStack stack : stackList) {
                    if (stack != null) {
                        temp.add(stack);
                    }
                }
            } else {
                throw new ActionFormatException("'items' is not a list of items", actionKey);
            }
            action.setItems(temp);
        }
        if (data.contains(actionKey + "storm-world")) {
            final String world = data.getString(actionKey + "storm-world");
            if (world != null) {
                final World stormWorld = plugin.getServer().getWorld(world);
                if (stormWorld == null) {
                    throw new ActionFormatException("'storm-world' is not a valid world name", actionKey);
                }
                if (data.contains(actionKey + "storm-duration")) {
                    if (data.getInt(actionKey + "storm-duration", -999) != -999) {
                        action.setStormDuration(data.getInt(actionKey + "storm-duration") * 1000);
                    } else {
                        throw new ActionFormatException("'storm-duration' is not a number", actionKey);
                    }
                    action.setStormWorld(stormWorld);
                } else {
                    throw new ActionFormatException("'storm-duration' is missing", actionKey);
                }
            } else {
                throw new ActionFormatException("'storm-world' is not a valid world", actionKey);
            }
        }
        if (data.contains(actionKey + "thunder-world")) {
            final String world = data.getString(actionKey + "thunder-world");
            if (world != null) {
                final World thunderWorld = plugin.getServer().getWorld(world);
                if (thunderWorld == null) {
                    throw new ActionFormatException("'thunder-world' is not a valid world name", actionKey);
                }
                if (data.contains(actionKey + "thunder-duration")) {
                    if (data.getInt(actionKey + "thunder-duration", -999) != -999) {
                        action.setThunderDuration(data.getInt(actionKey + "thunder-duration"));
                    } else {
                        throw new ActionFormatException("'thunder-duration' is not a number", actionKey);
                    }
                    action.setThunderWorld(thunderWorld);
                } else {
                    throw new ActionFormatException("'thunder-duration' is missing", actionKey);
                }
            } else {
                throw new ActionFormatException("'thunder-world' is not a valid world", actionKey);
            }
        }
        if (data.contains(actionKey + "mob-spawns")) {
            final ConfigurationSection section = data.getConfigurationSection(actionKey + "mob-spawns");
            if (section != null) {
                final LinkedList<QuestMob> mobSpawns = new LinkedList<>();
                for (final String s : section.getKeys(false)) {
                    final String mobName = section.getString(s + ".name");
                    final String location = section.getString(s + ".spawn-location");
                    if (location != null) {
                        final Location spawnLocation = BukkitConfigUtil.getLocation(location);
                        final EntityType type = BukkitMiscUtil.getProperMobType(section.getString(s + ".mob-type"));
                        final int mobAmount = section.getInt(s + ".spawn-amounts");
                        if (spawnLocation == null) {
                            throw new ActionFormatException("'mob-spawn-locations' is not in proper \"WorldName x y z\" format",
                                    actionKey);
                        }
                        if (type == null) {
                            throw new ActionFormatException("'mob-spawn-types' is not a list of mob types", actionKey);
                        }
                        final ItemStack[] inventory = new ItemStack[5];
                        final Float[] dropChances = new Float[5];
                        inventory[0] = loadMobItem(section, s + ".held-item");
                        dropChances[0] = (float) section.getDouble(s + ".held-item-drop-chance");
                        inventory[1] = loadMobItem(section, s + ".boots");
                        dropChances[1] = (float) section.getDouble(s + ".boots-drop-chance");
                        inventory[2] = loadMobItem(section, s + ".leggings");
                        dropChances[2] = (float) section.getDouble(s + ".leggings-drop-chance");
                        inventory[3] = loadMobItem(section, s + ".chest-plate");
                        dropChances[3] = (float) section.getDouble(s + ".chest-plate-drop-chance");
                        inventory[4] = loadMobItem(section, s + ".helmet");
                        dropChances[4] = (float) section.getDouble(s + ".helmet-drop-chance");
                        final BukkitQuestMob questMob = new BukkitQuestMob(type, spawnLocation, mobAmount);
                        questMob.setInventory(inventory);
                        questMob.setDropChances(dropChances);
                        questMob.setName(mobName);
                        mobSpawns.add(questMob);
                    } else {
                        throw new ActionFormatException("'mob-spawn-locations' contains an invalid location", actionKey);
                    }
                }
                action.setMobSpawns(mobSpawns);
            }
        }
        if (data.contains(actionKey + "lightning-strikes")) {
            if (BukkitConfigUtil.checkList(data.getList(actionKey + "lightning-strikes"), String.class)) {
                final LinkedList<Location> lightningStrikes = new LinkedList<>();
                for (final String s : data.getStringList(actionKey + "lightning-strikes")) {
                    final Location loc = BukkitConfigUtil.getLocation(s);
                    if (loc == null) {
                        throw new ActionFormatException("'lightning-strikes' is not in proper \"WorldName x y z\" format",
                                actionKey);
                    }
                    lightningStrikes.add(loc);
                }
                action.setLightningStrikes(lightningStrikes);
            } else {
                throw new ActionFormatException("'lightning-strikes' is not a list of locations", actionKey);
            }
        }
        if (data.contains(actionKey + "commands")) {
            if (BukkitConfigUtil.checkList(data.getList(actionKey + "commands"), String.class)) {
                final LinkedList<String> commands = new LinkedList<>();
                for (String s : data.getStringList(actionKey + "commands")) {
                    if (s.startsWith("/")) {
                        s = s.replaceFirst("/", "");
                    }
                    commands.add(s);
                }
                action.setCommands(commands);
            } else {
                throw new ActionFormatException("'commands' is not a list of commands", actionKey);
            }
        }
        if (data.contains(actionKey + "potion-effect-types")) {
            if (BukkitConfigUtil.checkList(data.getList(actionKey + "potion-effect-types"), String.class)) {
                if (data.contains(actionKey + "potion-effect-durations")) {
                    if (BukkitConfigUtil.checkList(data.getList(actionKey + "potion-effect-durations"), Integer.class)) {
                        if (data.contains(actionKey + "potion-effect-amplifiers")) {
                            if (BukkitConfigUtil.checkList(data.getList(actionKey + "potion-effect-amplifiers"),
                                    Integer.class)) {
                                final List<String> types = data.getStringList(actionKey + "potion-effect-types");
                                final List<Integer> durations = data.getIntegerList(actionKey + "potion-effect-durations");
                                final List<Integer> amplifiers = data.getIntegerList(actionKey + "potion-effect-amplifiers");
                                final LinkedList<PotionEffect> potionEffects = new LinkedList<>();
                                for (final String s : types) {
                                    final PotionEffectType type = PotionEffectType.getByName(s);
                                    if (type == null) {
                                        throw new ActionFormatException("potion-effect-types is not a list of potion "
                                                + "effect types", actionKey);
                                    }
                                    final PotionEffect effect = new PotionEffect(type, durations
                                            .get(types.indexOf(s)), amplifiers.get(types.indexOf(s)));
                                    potionEffects.add(effect);
                                }
                                action.setPotionEffects(potionEffects);
                            } else {
                                throw new ActionFormatException("'potion-effect-amplifiers' is not a list of numbers",
                                        actionKey);
                            }
                        } else {
                            throw new ActionFormatException("'potion-effect-amplifiers' is missing", actionKey);
                        }
                    } else {
                        throw new ActionFormatException("'potion-effect-durations' is not a list of numbers", actionKey);
                    }
                } else {
                    throw new ActionFormatException("'potion-effect-durations' is missing", actionKey);
                }
            } else {
                throw new ActionFormatException("'potion-effect-types' is not a list of potion effects", actionKey);
            }
        }
        if (data.contains(actionKey + "hunger")) {
            if (data.getInt(actionKey + "hunger", -999) != -999) {
                action.setHunger(data.getInt(actionKey + "hunger"));
            } else {
                throw new ActionFormatException("'hunger' is not a number", actionKey);
            }
        }
        if (data.contains(actionKey + "saturation")) {
            if (data.getInt(actionKey + "saturation", -999) != -999) {
                action.setSaturation(data.getInt(actionKey + "saturation"));
            } else {
                throw new ActionFormatException("'saturation' is not a number", actionKey);
            }
        }
        if (data.contains(actionKey + "health")) {
            if (data.getInt(actionKey + "health", -999) != -999) {
                action.setHealth(data.getInt(actionKey + "health"));
            } else {
                throw new ActionFormatException("'health' is not a number", actionKey);
            }
        }
        if (data.contains(actionKey + "teleport-location")) {
            if (data.isString(actionKey + "teleport-location")) {
                final String location = data.getString(actionKey + "teleport-location");
                if (location != null) {
                    final Location teleport = BukkitConfigUtil.getLocation(location);
                    if (teleport == null) {
                        throw new ActionFormatException("'teleport-location' is not in proper \"WorldName x y z\" format",
                                actionKey);
                    }
                    action.setTeleport(teleport);
                } else {
                    throw new ActionFormatException("'teleport-location' has invalid location", actionKey);
                }
            } else {
                throw new ActionFormatException("'teleport-location' is not a location", actionKey);
            }
        }
        if (data.contains(actionKey + "timer")) {
            if (data.isInt(actionKey + "timer")) {
                action.setTimer(data.getInt(actionKey + "timer"));
            } else {
                throw new ActionFormatException("'timer' is not a number", actionKey);
            }
        }
        if (data.contains(actionKey + "cancel-timer")) {
            if (data.isBoolean(actionKey + "cancel-timer")) {
                action.setCancelTimer(data.getBoolean(actionKey + "cancel-timer"));
            } else {
                throw new ActionFormatException("'cancel-timer' is not a true/false value", actionKey);
            }
        }
        if (data.contains(actionKey + "denizen-script")) {
            action.setDenizenScript(data.getString(actionKey + "denizen-script"));
        }
        return action;
    }

    /**
     * Load mob inventory item whether legacy or not
     *
     * @deprecated Will be removed at some point
     * @param section section
     * @param node node
     * @return ItemStack
     */
    private ItemStack loadMobItem(ConfigurationSection section, String node) {
        try {
            return (ItemStack) section.get(node);
        } catch (Exception e) {
            return BukkitItemUtil.readItemStack(section.getString(node));
        }
    }
}
