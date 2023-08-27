package me.pikamug.quests.storage.implementation.file;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.conditions.BukkitCondition;
import me.pikamug.quests.conditions.Condition;
import me.pikamug.quests.exceptions.ConditionFormatException;
import me.pikamug.quests.storage.implementation.ConditionStorageImpl;
import me.pikamug.quests.util.BukkitConfigUtil;
import me.pikamug.quests.util.BukkitMiscUtil;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

public class BukkitConditionYamlStorage implements ConditionStorageImpl {

    private final BukkitQuestsPlugin plugin;

    public BukkitConditionYamlStorage(final BukkitQuestsPlugin plugin) {
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
        final File conditionsFile = new File(plugin.getDataFolder(), "conditions.yml");
        // Using #isFile because #exists and #renameTo can return false positives
        if (conditionsFile.length() != 0) {
            try {
                if (conditionsFile.isFile()) {
                    config.load(conditionsFile);
                }
            } catch (final IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
            final ConfigurationSection sec = config.getConfigurationSection("conditions");
            if (sec != null) {
                for (final String s : sec.getKeys(false)) {
                    BukkitCondition condition = null;
                    try {
                        condition = loadCondition(s);
                    } catch (final ConditionFormatException e) {
                        e.printStackTrace();
                    }
                    if (condition != null) {
                        final Collection<Condition> conditions = plugin.getLoadedConditions();
                        conditions.add(condition);
                        plugin.setLoadedConditions(conditions);
                    } else {
                        plugin.getLogger().log(Level.SEVERE, "Failed to load Condition \"" + s + "\". Skipping.");
                    }
                }
            } else {
                plugin.getLogger().log(Level.SEVERE, "Could not find beginning section from conditions.yml. Skipping.");
            }
        } else {
            plugin.getLogger().log(Level.WARNING, "Empty file conditions.yml was not loaded.");
        }
    }

    @Override
    public void close() {
    }

    @Override
    public BukkitCondition loadCondition(final String name) throws ConditionFormatException {
        if (name == null) {
            return null;
        }
        final File conditions = new File(plugin.getDataFolder(), "conditions.yml");
        final FileConfiguration data = new YamlConfiguration();
        try {
            if (conditions.isFile()) {
                data.load(conditions);
            }
        } catch (final IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        final String conditionKey = "conditions." + name + ".";
        final BukkitCondition condition = new BukkitCondition(plugin);
        condition.setName(name);
        if (data.contains(conditionKey + "fail-quest")) {
            if (data.isBoolean(conditionKey + "fail-quest")) {
                condition.setFailQuest(data.getBoolean(conditionKey + "fail-quest"));
            } else {
                throw new ConditionFormatException("'fail-quest' is not a true/false value", conditionKey);
            }
        }
        if (data.contains(conditionKey + "ride-entity")) {
            if (BukkitConfigUtil.checkList(data.getList(conditionKey + "ride-entity"), String.class)) {
                final LinkedList<String> entities = new LinkedList<>();
                for (final String s : data.getStringList(conditionKey + "ride-entity")) {
                    final EntityType e = BukkitMiscUtil.getProperMobType(s);
                    if (e == null) {
                        throw new ConditionFormatException("'ride-entity' is not a valid entity type",
                                conditionKey);
                    }
                    entities.add(s);
                }
                condition.setEntitiesWhileRiding(entities);
            } else {
                throw new ConditionFormatException("'ride-entity' is not a list of entity types", conditionKey);
            }
        }
        if (data.contains(conditionKey + "ride-npc-uuid")) {
            if (BukkitConfigUtil.checkList(data.getList(conditionKey + "ride-npc-uuid"), String.class)) {
                final LinkedList<UUID> npcList = new LinkedList<>();
                for (final String s : data.getStringList(conditionKey + "ride-npc-uuid")) {
                    final UUID u = UUID.fromString(s);
                    npcList.add(u);
                }
                condition.setNpcsWhileRiding(npcList);
            }
        } else if (data.contains(conditionKey + "ride-npc")) {
            // Legacy
            if (BukkitConfigUtil.checkList(data.getList(conditionKey + "ride-npc"), Integer.class)) {
                final LinkedList<UUID> npcList = new LinkedList<>();
                if (plugin.getDependencies().getCitizens() != null) {
                    for (final int i : data.getIntegerList(conditionKey + "ride-npc")) {
                        final NPC npc = CitizensAPI.getNPCRegistry().getById(i);
                        if (npc != null) {
                            npcList.add(npc.getUniqueId());
                        } else {
                            throw new ConditionFormatException("'ride-npc' is not a valid NPC ID",
                                    conditionKey);
                        }
                    }
                    condition.setNpcsWhileRiding(npcList);
                } else {
                    throw new ConditionFormatException("Citizens not found for 'ride-npc'", conditionKey);
                }
            } else {
                throw new ConditionFormatException("'ride-npc' is not a list of NPC IDs", conditionKey);
            }
        }
        if (data.contains(conditionKey + "permission")) {
            if (BukkitConfigUtil.checkList(data.getList(conditionKey + "permission"), String.class)) {
                final LinkedList<String> permissions
                        = new LinkedList<>(data.getStringList(conditionKey + "permission"));
                condition.setPermissions(permissions);
            } else {
                throw new ConditionFormatException("'permission' is not a list of permissions", conditionKey);
            }
        }
        if (data.contains(conditionKey + "hold-main-hand")) {
            final LinkedList<ItemStack> temp = new LinkedList<>();
            @SuppressWarnings("unchecked")
            final List<ItemStack> stackList = (List<ItemStack>) data.get(conditionKey + "hold-main-hand");
            if (BukkitConfigUtil.checkList(stackList, ItemStack.class)) {
                for (final ItemStack stack : stackList) {
                    if (stack != null) {
                        temp.add(stack);
                    }
                }
            }
            condition.setItemsWhileHoldingMainHand(temp);
        }
        if (data.contains(conditionKey + "wear")) {
            final LinkedList<ItemStack> temp = new LinkedList<>();
            @SuppressWarnings("unchecked")
            final List<ItemStack> stackList = (List<ItemStack>) data.get(conditionKey + "wear");
            if (BukkitConfigUtil.checkList(stackList, ItemStack.class)) {
                for (final ItemStack stack : stackList) {
                    if (stack != null) {
                        temp.add(stack);
                    }
                }
            }
            condition.setItemsWhileWearing(temp);
        }
        if (data.contains(conditionKey + "stay-within-world")) {
            if (BukkitConfigUtil.checkList(data.getList(conditionKey + "stay-within-world"), String.class)) {
                final LinkedList<String> worlds = new LinkedList<>();
                for (final String s : data.getStringList(conditionKey + "stay-within-world")) {
                    final World w = plugin.getServer().getWorld(s);
                    if (w == null) {
                        throw new ConditionFormatException("'stay-within-world' is not a valid world",
                                conditionKey);
                    }
                    worlds.add(s);
                }
                condition.setWorldsWhileStayingWithin(worlds);
            } else {
                throw new ConditionFormatException("'stay-within-world' is not a list of worlds", conditionKey);
            }
        }
        if (data.contains(conditionKey + "stay-within-ticks")) {
            if (data.isInt(conditionKey + "stay-within-ticks.start")) {
                condition.setTickStartWhileStayingWithin(data.getInt(conditionKey + "stay-within-ticks.start"));
            } else {
                throw new ConditionFormatException("'start' tick is not a number", conditionKey);
            }
            if (data.isInt(conditionKey + "stay-within-ticks.end")) {
                condition.setTickEndWhileStayingWithin(data.getInt(conditionKey + "stay-within-ticks.end"));
            } else {
                throw new ConditionFormatException("'end' tick is not a number", conditionKey);
            }
        }
        if (data.contains(conditionKey + "stay-within-biome")) {
            if (BukkitConfigUtil.checkList(data.getList(conditionKey + "stay-within-biome"), String.class)) {
                final LinkedList<String> biomes = new LinkedList<>();
                for (final String s : data.getStringList(conditionKey + "stay-within-biome")) {
                    final Biome b = BukkitMiscUtil.getProperBiome(s);
                    if (b == null) {
                        throw new ConditionFormatException("'stay-within-biome' is not a valid biome",
                                conditionKey);
                    }
                    biomes.add(s);
                }
                condition.setBiomesWhileStayingWithin(biomes);
            } else {
                throw new ConditionFormatException("'stay-within-biome' is not a list of biomes", conditionKey);
            }
        }
        if (data.contains(conditionKey + "stay-within-region")) {
            if (BukkitConfigUtil.checkList(data.getList(conditionKey + "stay-within-region"), String.class)) {
                final LinkedList<String> regions = new LinkedList<>();
                for (final String region : data.getStringList(conditionKey + "stay-within-region")) {
                    if (region != null) {
                        boolean exists = false;
                        for (final World world : plugin.getServer().getWorlds()) {
                            if (world != null && plugin.getDependencies().getWorldGuardApi().getRegionManager(world) != null) {
                                if (Objects.requireNonNull(plugin.getDependencies().getWorldGuardApi().getRegionManager(world))
                                        .hasRegion(region)) {
                                    regions.add(region);
                                    exists = true;
                                    break;
                                }
                            }
                        }
                        if (!exists) {
                            throw new ConditionFormatException("'region' has invalid WorldGuard region name", conditionKey);
                        }
                    } else {
                        throw new ConditionFormatException("'region' has invalid WorldGuard region", conditionKey);
                    }
                }
                condition.setRegionsWhileStayingWithin(regions);
            } else {
                throw new ConditionFormatException("'stay-within-region' is not a list of regions", conditionKey);
            }
        }
        if (data.contains(conditionKey + "check-placeholder-id")) {
            if (BukkitConfigUtil.checkList(data.getList(conditionKey + "check-placeholder-id"), String.class)) {
                final LinkedList<String> id = new LinkedList<>(data.getStringList(conditionKey
                        + "check-placeholder-id"));
                condition.setPlaceholdersCheckIdentifier(id);
            } else {
                throw new ConditionFormatException("'check-placeholder-id' is not a list of identifiers", conditionKey);
            }
            if (BukkitConfigUtil.checkList(data.getList(conditionKey + "check-placeholder-value"), String.class)) {
                final LinkedList<String> val = new LinkedList<>(data.getStringList(conditionKey
                        + "check-placeholder-value"));
                condition.setPlaceholdersCheckValue(val);
            } else {
                throw new ConditionFormatException("'check-placeholder-value' is not a list of values", conditionKey);
            }
        }
        return condition;
    }
}
