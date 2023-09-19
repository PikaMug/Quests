package me.pikamug.quests.storage.implementation.file;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.actions.Action;
import me.pikamug.quests.conditions.Condition;
import me.pikamug.quests.dependencies.BukkitDependencies;
import me.pikamug.quests.exceptions.ActionFormatException;
import me.pikamug.quests.exceptions.ConditionFormatException;
import me.pikamug.quests.exceptions.QuestFormatException;
import me.pikamug.quests.exceptions.StageFormatException;
import me.pikamug.quests.quests.BukkitQuest;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.BukkitRequirements;
import me.pikamug.quests.quests.components.BukkitRewards;
import me.pikamug.quests.quests.components.BukkitStage;
import me.pikamug.quests.quests.components.Options;
import me.pikamug.quests.quests.components.Planner;
import me.pikamug.quests.storage.implementation.QuestStorageImpl;
import me.pikamug.quests.util.BukkitConfigUtil;
import me.pikamug.quests.util.BukkitItemUtil;
import me.pikamug.quests.util.BukkitMiscUtil;
import me.pikamug.quests.util.BukkitLang;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Tameable;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public class BukkitQuestYamlStorage implements QuestStorageImpl {

    private final BukkitQuestsPlugin plugin;

    public BukkitQuestYamlStorage(final BukkitQuestsPlugin plugin) {
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
        boolean needsSaving = false;
        FileConfiguration config = null;
        final File file = new File(plugin.getDataFolder(), "quests.yml");
        try {
            config = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(file),
                    StandardCharsets.UTF_8));
        } catch (final IOException e) {
            e.printStackTrace();
        }
        if (config != null) {
            final ConfigurationSection questsSection;
            if (config.contains("quests")) {
                questsSection = config.getConfigurationSection("quests");
            } else {
                questsSection = config.createSection("quests");
                needsSaving = true;
            }
            if (questsSection == null) {
                plugin.getLogger().severe("Missing 'quests' section marker within quests.yml, canceled loading");
                return;
            }
            for (final String questKey : questsSection.getKeys(false)) {
                try {
                    final Quest quest = loadQuest(questKey);
                    if (config.contains("quests." + questKey + ".requirements")) {
                        loadQuestRequirements(config, questsSection, quest, questKey);
                    }
                    if (config.contains("quests." + questKey + ".planner")) {
                        loadQuestPlanner(config, quest, questKey);
                    }
                    if (config.contains("quests." + questKey + ".options")) {
                        loadQuestOptions(config, quest, questKey);
                    }
                    // TODO was this necessary?
                    //quest.setPlugin(this);
                    loadQuestStages(quest, config, questKey);
                    loadQuestRewards(config, quest, questKey);
                    final Collection<Quest> loadedQuests = plugin.getLoadedQuests();
                    loadedQuests.add(quest);
                    plugin.setLoadedQuests(loadedQuests);
                    if (needsSaving) {
                        try {
                            config.save(file);
                        } catch (final IOException e) {
                            plugin.getLogger().log(Level.SEVERE, "Failed to save Quest \"" + questKey + "\"");
                            e.printStackTrace();
                        }
                    }
                } catch (final QuestFormatException | StageFormatException | ActionFormatException
                        | ConditionFormatException e) {
                    e.printStackTrace();
                }
            }
        } else {
            plugin.getLogger().severe("Unable to load quests.yml");
        }
    }

    @Override
    public void close() {
    }

    @Override
    public Quest loadQuest(final String questId) throws QuestFormatException {
        if (questId == null) {
            return null;
        }
        FileConfiguration config = null;
        final File file = new File(plugin.getDataFolder(), "quests.yml");
        try {
            config = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(file),
                    StandardCharsets.UTF_8));
        } catch (final IOException e) {
            e.printStackTrace();
        }
        final BukkitQuest quest = new BukkitQuest(plugin);
        final BukkitDependencies depends = plugin.getDependencies();
        quest.setId(questId);
        if (config.contains("quests." + questId + ".name")) {
            quest.setName(BukkitConfigUtil.parseString(config.getString("quests." + questId + ".name"), quest));
        } else {
            throw new QuestFormatException("'name' is missing", questId);
        }
        if (config.contains("quests." + questId + ".ask-message")) {
            quest.setDescription(BukkitConfigUtil.parseString(config.getString("quests." + questId
                    + ".ask-message"), quest));
        } else {
            throw new QuestFormatException("'ask-message' is missing", questId);
        }
        if (config.contains("quests." + questId + ".finish-message")) {
            quest.setFinished(BukkitConfigUtil.parseString(config.getString("quests." + questId
                    + ".finish-message"), quest));
        } else {
            throw new QuestFormatException("'finish-message' is missing", questId);
        }
        if (config.contains("quests." + questId + ".npc-giver-uuid")) {
            final UUID uuid = UUID.fromString(Objects.requireNonNull(config.getString("quests." + questId
                    + ".npc-giver-uuid")));
            quest.setNpcStart(uuid);
            final Collection<UUID> npcUuids = plugin.getQuestNpcUuids();
            npcUuids.add(uuid);
            plugin.setQuestNpcUuids(npcUuids);
        } else if (depends.getCitizens() != null && config.contains("quests." + questId + ".npc-giver-id")) {
            // Legacy
            final int id = config.getInt("quests." + questId + ".npc-giver-id");
            if (CitizensAPI.getNPCRegistry().getById(id) != null) {
                final NPC npc = CitizensAPI.getNPCRegistry().getById(id);
                quest.setNpcStart(npc.getUniqueId());
                final Collection<UUID> npcUuids = plugin.getQuestNpcUuids();
                npcUuids.add(npc.getUniqueId());
                plugin.setQuestNpcUuids(npcUuids);
            } else {
                throw new QuestFormatException("'npc-giver-id' has invalid NPC ID " + id, questId);
            }
        }
        if (config.contains("quests." + questId + ".block-start")) {
            final String blockStart = config.getString("quests." + questId + ".block-start");
            if (blockStart != null) {
                final Location location = BukkitConfigUtil.getLocation(blockStart);
                if (location != null) {
                    quest.setBlockStart(location);
                } else {
                    throw new QuestFormatException("'block-start' has invalid location", questId);
                }
            } else {
                throw new QuestFormatException("'block-start' has invalid location format", questId);
            }
        }
        if (config.contains("quests." + questId + ".region")
                && depends.getWorldGuardApi() != null) {
            final String region = config.getString("quests." + questId + ".region");
            if (region != null) {
                boolean exists = false;
                for (final World world : plugin.getServer().getWorlds()) {
                    if (world != null && depends.getWorldGuardApi().getRegionManager(world) != null) {
                        if (Objects.requireNonNull(depends.getWorldGuardApi().getRegionManager(world))
                                .hasRegion(region)) {
                            quest.setRegionStart(region);
                            exists = true;
                            break;
                        }
                    }
                }
                if (!exists) {
                    throw new QuestFormatException("'region' has invalid WorldGuard region name", questId);
                }
            } else {
                throw new QuestFormatException("'region' has invalid WorldGuard region", questId);
            }
        }
        if (config.contains("quests." + questId + ".gui-display")) {
            ItemStack stack = config.getItemStack("quests." + questId + ".gui-display");
            if (stack != null) {
                quest.setGUIDisplay(stack);
            } else {
                throw new QuestFormatException("'gui-display' has invalid item format", questId);
            }
        }
        if (config.contains("quests." + questId + ".redo-delay")) {
            // Legacy
            if (config.getInt("quests." + questId + ".redo-delay", -999) != -999) {
                quest.getPlanner().setCooldown(config.getInt("quests." + questId + ".redo-delay") * 1000L);
            } else {
                throw new QuestFormatException("'redo-delay' is not a number", questId);
            }
        }
        if (config.contains("quests." + questId + ".action")) {
            final String actionName = config.getString("quests." + questId + ".action");
            final Optional<Action> action = plugin.getLoadedActions().stream().filter(a -> a.getName()
                    .equals(actionName)).findAny();
            if (action.isPresent()) {
                quest.setInitialAction(action.get());
            } else {
                throw new QuestFormatException("'action' failed to load", questId);
            }
        } else if (config.contains("quests." + questId + ".event")) {
            final String actionName = config.getString("quests." + questId + ".event");
            final Optional<Action> action = plugin.getLoadedActions().stream().filter(a -> a.getName()
                    .equals(actionName)).findAny();
            if (action.isPresent()) {
                quest.setInitialAction(action.get());
            } else {
                throw new QuestFormatException("'event' failed to load", questId);
            }
        }
        return quest;
    }

    public void importQuests() {
        final File f = new File(plugin.getDataFolder(), "import");
        if (f.exists() && f.isDirectory()) {
            final File[] imports = f.listFiles();
            if (imports != null) {
                for (final File file : imports) {
                    if (!file.isDirectory() && file.getName().endsWith(".yml")) {
                        importQuest(file);
                    }
                }
            }
        } else if (!f.mkdir()) {
            plugin.getLogger().warning("Unable to create import directory");
        }
    }

    private void importQuest(final File file) {
        FileConfiguration config = null;
        try {
            config = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(file),
                    StandardCharsets.UTF_8));
        } catch (final IOException e) {
            e.printStackTrace();
        }
        if (config != null) {
            final ConfigurationSection questsSection = config.getConfigurationSection("quests");
            if (questsSection == null) {
                plugin.getLogger().severe("Missing 'quests' section marker, canceled import of file " + file.getName());
                return;
            }
            int count = 0;
            for (final String questId : questsSection.getKeys(false)) {
                try {
                    for (final Quest lq : plugin.getLoadedQuests()) {
                        if (lq.getId().equals(questId)) {
                            throw new QuestFormatException("id already exists", questId);
                        }
                    }
                    final Quest quest = loadQuest(questId);
                    if (config.contains("quests." + questId + ".requirements")) {
                        loadQuestRequirements(config, questsSection, quest, questId);
                    }
                    if (config.contains("quests." + questId + ".planner")) {
                        loadQuestPlanner(config, quest, questId);
                    }
                    if (config.contains("quests." + questId + ".options")) {
                        loadQuestOptions(config, quest, questId);
                    }
                    // TODO was this necessary?
                    //quest.setPlugin(this);
                    loadQuestStages(quest, config, questId);
                    loadQuestRewards(config, quest, questId);
                    final Collection<Quest> loadedQuests = plugin.getLoadedQuests();
                    loadedQuests.add(quest);
                    plugin.setLoadedQuests(loadedQuests);
                    count++;
                } catch (final QuestFormatException | StageFormatException | ActionFormatException
                        | ConditionFormatException e) {
                    e.printStackTrace();
                }
            }
            if (count > 0) {
                plugin.getLogger().info("Imported " + count + " Quests from " + file.getName());
            }
        } else {
            plugin.getLogger().severe("Unable to import quest file " + file.getName());
        }
    }

    @SuppressWarnings({"unchecked"})
    private void loadQuestRewards(final FileConfiguration config, final Quest quest, final String questKey)
            throws QuestFormatException {
        final BukkitRewards rewards = (BukkitRewards) quest.getRewards();
        final BukkitDependencies depends = plugin.getDependencies();
        if (config.contains("quests." + questKey + ".rewards.items")) {
            final LinkedList<ItemStack> temp = new LinkedList<>();
            final List<ItemStack> stackList = (List<ItemStack>) config.get("quests." + questKey + ".rewards.items");
            if (BukkitConfigUtil.checkList(stackList, ItemStack.class)) {
                for (final ItemStack stack : stackList) {
                    if (stack != null) {
                        temp.add(stack);
                    }
                }
            } else {
                throw new QuestFormatException("Reward 'items' has invalid formatting", questKey);
            }
            rewards.setItems(temp);
        }
        if (config.contains("quests." + questKey + ".rewards.money")) {
            if (config.getInt("quests." + questKey + ".rewards.money", -999) != -999) {
                rewards.setMoney(config.getInt("quests." + questKey + ".rewards.money"));
            } else {
                throw new QuestFormatException("Reward 'money' is not a number", questKey);
            }
        }
        if (config.contains("quests." + questKey + ".rewards.quest-points")) {
            if (config.getInt("quests." + questKey + ".rewards.quest-points", -999) != -999) {
                rewards.setQuestPoints(config.getInt("quests." + questKey + ".rewards.quest-points"));
            } else {
                throw new QuestFormatException("Reward 'quest-points' is not a number", questKey);
            }
        }
        if (config.contains("quests." + questKey + ".rewards.exp")) {
            if (config.getInt("quests." + questKey + ".rewards.exp", -999) != -999) {
                rewards.setExp(config.getInt("quests." + questKey + ".rewards.exp"));
            } else {
                throw new QuestFormatException("Reward 'exp' is not a number", questKey);
            }
        }
        if (config.contains("quests." + questKey + ".rewards.commands")) {
            if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".rewards.commands"), String.class)) {
                rewards.setCommands(config.getStringList("quests." + questKey + ".rewards.commands"));
            } else {
                throw new QuestFormatException("Reward 'commands' is not a list of commands", questKey);
            }
        }
        if (config.contains("quests." + questKey + ".rewards.commands-override-display")) {
            // Legacy
            if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".rewards.commands-override-display"),
                    String.class)) {
                rewards.setCommandsOverrideDisplay(config.getStringList("quests." + questKey
                        + ".rewards.commands-override-display"));
            } else {
                throw new QuestFormatException("Reward 'commands-override-display' is not a list of strings", questKey);
            }
        }
        if (config.contains("quests." + questKey + ".rewards.permissions")) {
            if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".rewards.permissions"), String.class)) {
                rewards.setPermissions(config.getStringList("quests." + questKey + ".rewards.permissions"));
            } else {
                throw new QuestFormatException("Reward 'permissions' is not a list of permissions", questKey);
            }
        }
        if (config.contains("quests." + questKey + ".rewards.permission-worlds")) {
            if (BukkitConfigUtil.checkList(config.getList("quests." + questKey
                    + ".rewards.permission-worlds"), String.class)) {
                rewards.setPermissionWorlds(config.getStringList("quests." + questKey + ".rewards.permission-worlds"));
            } else {
                throw new QuestFormatException("Reward 'permission-worlds' is not a list of worlds", questKey);
            }
        }
        if (depends.isPluginAvailable("mcMMO")) {
            if (config.contains("quests." + questKey + ".rewards.mcmmo-skills")) {
                if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".rewards.mcmmo-skills"),
                        String.class)) {
                    if (config.contains("quests." + questKey + ".rewards.mcmmo-levels")) {
                        if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".rewards.mcmmo-levels"),
                                Integer.class)) {
                            for (final String skill : config.getStringList("quests." + questKey
                                    + ".rewards.mcmmo-skills")) {
                                if (depends.getMcmmoClassic() == null) {
                                    throw new QuestFormatException("mcMMO not found for 'mcmmo-skills'", questKey);
                                } else if (depends.getMcMMOSkill(skill) == null) {
                                    throw new QuestFormatException("Reward 'mcmmo-skills' has invalid skill name "
                                            + skill, questKey);
                                }
                            }
                            rewards.setMcmmoSkills(config.getStringList("quests." + questKey
                                    + ".rewards.mcmmo-skills"));
                            rewards.setMcmmoAmounts(config.getIntegerList("quests." + questKey
                                    + ".rewards.mcmmo-levels"));
                        } else {
                            throw new QuestFormatException("Reward 'mcmmo-levels' is not a list of numbers", questKey);
                        }
                    } else {
                        throw new QuestFormatException("Reward 'mcmmo-levels' is missing!", questKey);
                    }
                } else {
                    throw new QuestFormatException("Reward 'mcmmo-skills' is not a list of mcMMO skill names", questKey);
                }
            }
        }
        if (depends.isPluginAvailable("Heroes")) {
            if (config.contains("quests." + questKey + ".rewards.heroes-exp-classes")) {
                if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".rewards.heroes-exp-classes"),
                        String.class)) {
                    if (config.contains("quests." + questKey + ".rewards.heroes-exp-amounts")) {
                        if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".rewards.heroes-exp-amounts"),
                                Double.class)) {
                            for (final String heroClass : config.getStringList("quests." + questKey
                                    + ".rewards.heroes-exp-classes")) {
                                if (depends.getHeroes() == null) {
                                    throw new QuestFormatException("Heroes not found for 'heroes-exp-classes'", questKey);
                                } else if (depends.getHeroes().getClassManager().getClass(heroClass) == null) {
                                    throw new QuestFormatException("Reward 'heroes-exp-classes' has invalid class name "
                                            + heroClass, questKey);
                                }
                            }
                            rewards.setHeroesClasses(config.getStringList("quests." + questKey
                                    + ".rewards.heroes-exp-classes"));
                            rewards.setHeroesAmounts(config.getDoubleList("quests." + questKey
                                    + ".rewards.heroes-exp-amounts"));
                        } else {
                            throw new QuestFormatException("Reward 'heroes-exp-amounts' is not a list of decimal numbers",
                                    questKey);
                        }
                    } else {
                        throw new QuestFormatException("Reward 'heroes-exp-amounts' is missing", questKey);
                    }
                } else {
                    throw new QuestFormatException("Reward 'heroes-exp-classes' is not a list of Heroes classes",
                            questKey);
                }
            }
        }
        if (depends.isPluginAvailable("Parties")) {
            if (config.contains("quests." + questKey + ".rewards.parties-experience")) {
                if (config.getInt("quests." + questKey + ".rewards.parties-experience", -999) != -999) {
                    rewards.setPartiesExperience(config.getInt("quests." + questKey + ".rewards.parties-experience"));
                } else {
                    throw new QuestFormatException("Reward 'parties-experience' is not a number", questKey);
                }
            }
        }
        if (config.contains("quests." + questKey + ".rewards.phat-loots")) {
            throw new QuestFormatException("PhatLoots support has been removed. Use the module instead!", questKey);
        }
        if (config.contains("quests." + questKey + ".rewards.details-override")) {
            if (BukkitConfigUtil.checkList(config.getList("quests." + questKey
                    + ".rewards.details-override"), String.class)) {
                rewards.setDetailsOverride(config.getStringList("quests." + questKey + ".rewards.details-override"));
            }  else {
                throw new QuestFormatException("Reward 'details-override' is not a list of strings", questKey);
            }
        }
    }

    @SuppressWarnings({ "unchecked"})
    private void loadQuestRequirements(final FileConfiguration config, final ConfigurationSection questsSection,
                                       final Quest quest, final String questKey) throws QuestFormatException {
        final BukkitRequirements requires = (BukkitRequirements) quest.getRequirements();
        final BukkitDependencies depends = plugin.getDependencies();
        if (config.contains("quests." + questKey + ".requirements.fail-requirement-message")) {
            final Object o = config.get("quests." + questKey + ".requirements.fail-requirement-message");
            if (o instanceof List) {
                requires.setDetailsOverride(config.getStringList("quests." + questKey
                        + ".requirements.fail-requirement-message"));
            } else {
                // Legacy
                final List<String> override = new LinkedList<>();
                override.add((String) o);
                requires.setDetailsOverride(override);
            }
        }
        if (config.contains("quests." + questKey + ".requirements.items")) {
            final List<ItemStack> temp = new LinkedList<>();
            final List<ItemStack> stackList = (List<ItemStack>) config.get("quests." + questKey
                    + ".requirements.items");
            if (BukkitConfigUtil.checkList(stackList, ItemStack.class)) {
                for (final ItemStack stack : stackList) {
                    if (stack != null) {
                        temp.add(stack);
                    }
                }
            } else {
                throw new QuestFormatException("Requirement 'items' has invalid formatting", questKey);
            }
            requires.setItems(temp);
            if (config.contains("quests." + questKey + ".requirements.remove-items")) {
                if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".requirements.remove-items"),
                        Boolean.class)) {
                    requires.setRemoveItems(config.getBooleanList("quests." + questKey + ".requirements.remove-items"));
                } else {
                    throw new QuestFormatException("Requirement 'remove-items' is not a list of true/false values",
                            questKey);
                }
            } else {
                throw new QuestFormatException("Requirement 'remove-items' is missing", questKey);
            }
        }
        if (config.contains("quests." + questKey + ".requirements.money")) {
            if (config.getInt("quests." + questKey + ".requirements.money", -999) != -999) {
                requires.setMoney(config.getInt("quests." + questKey + ".requirements.money"));
            } else {
                throw new QuestFormatException("Requirement 'money' is not a number", questKey);
            }
        }
        if (config.contains("quests." + questKey + ".requirements.quest-points")) {
            if (config.getInt("quests." + questKey + ".requirements.quest-points", -999) != -999) {
                requires.setQuestPoints(config.getInt("quests." + questKey + ".requirements.quest-points"));
            } else {
                throw new QuestFormatException("Requirement 'quest-points' is not a number", questKey);
            }
        }
        if (config.contains("quests." + questKey + ".requirements.exp")) {
            if (config.getInt("quests." + questKey + ".requirements.exp", -999) != -999) {
                requires.setExp(config.getInt("quests." + questKey + ".requirements.exp"));
            } else {
                throw new QuestFormatException("Requirement 'exp' is not a number", questKey);
            }
        }
        if (config.contains("quests." + questKey + ".requirements.quest-blocks")) {
            if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".requirements.quest-blocks"),
                    String.class)) {
                final List<String> nodes = config.getStringList("quests." + questKey + ".requirements.quest-blocks");
                boolean failed = false;
                String failedQuest = "NULL";
                final List<String> temp = new LinkedList<>();
                for (final String node : nodes) {
                    boolean done = false;
                    for (final String id : questsSection.getKeys(false)) {
                        if (id.equals(node)) {
                            temp.add(node);
                            done = true;
                            break;
                        }
                    }
                    if (!done) {
                        failed = true;
                        failedQuest = node;
                        break;
                    }
                }
                requires.setBlockQuestIds(temp);
                if (failed) {
                    throw new QuestFormatException("Requirement 'quest-blocks' has invalid quest ID " + failedQuest,
                            questKey);
                }
            } else {
                throw new QuestFormatException("Requirement 'quest-blocks' is not a list of quest names", questKey);
            }
        }
        if (config.contains("quests." + questKey + ".requirements.quests")) {
            if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".requirements.quests"), String.class)) {
                final List<String> nodes = config.getStringList("quests." + questKey + ".requirements.quests");
                boolean failed = false;
                String failedQuest = "NULL";
                final List<String> temp = new LinkedList<>();
                for (final String node : nodes) {
                    boolean done = false;
                    for (final String id : questsSection.getKeys(false)) {
                        if (id.equals(node)) {
                            temp.add(node);
                            done = true;
                            break;
                        }
                    }
                    if (!done) {
                        failed = true;
                        failedQuest = node;
                        break;
                    }
                }
                requires.setNeededQuestIds(temp);
                if (failed) {
                    throw new QuestFormatException("Requirement 'quests' has invalid quest ID "
                            + failedQuest, questKey);
                }
            } else {
                throw new QuestFormatException("Requirement 'quests' is not a list of quest names", questKey);
            }
        }
        if (config.contains("quests." + questKey + ".requirements.permissions")) {
            if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".requirements.permissions"),
                    String.class)) {
                requires.setPermissions(config.getStringList("quests." + questKey + ".requirements.permissions"));
            } else {
                throw new QuestFormatException("Requirement 'permissions' is not a list of permissions", questKey);
            }
        }
        if (config.contains("quests." + questKey + ".requirements.mcmmo-skills")) {
            if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".requirements.mcmmo-skills"),
                    String.class)) {
                if (config.contains("quests." + questKey + ".requirements.mcmmo-amounts")) {
                    if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".requirements.mcmmo-amounts"),
                            Integer.class)) {
                        final List<String> skills = config.getStringList("quests." + questKey
                                + ".requirements.mcmmo-skills");
                        final List<Integer> amounts = config.getIntegerList("quests." + questKey
                                + ".requirements.mcmmo-amounts");
                        if (skills.size() != amounts.size()) {
                            throw new QuestFormatException("Requirement 'mcmmo-skills' and 'mcmmo-amounts' are not the "
                                    + "same size", questKey);
                        }
                        requires.setMcmmoSkills(skills);
                        requires.setMcmmoAmounts(amounts);
                    } else {
                        throw new QuestFormatException("Requirement 'mcmmo-amounts' is not a list of numbers", questKey);
                    }
                } else {
                    throw new QuestFormatException("Requirement 'mcmmo-amounts' is missing", questKey);
                }
            } else {
                throw new QuestFormatException("Requirement 'mcmmo-skills' is not a list of skills", questKey);
            }
        }
        if (config.contains("quests." + questKey + ".requirements.heroes-primary-class")) {
            final String className = config.getString("quests." + questKey + ".requirements.heroes-primary-class");
            if (depends.getHeroClass(className) != null && depends.getHeroClass(className).isPrimary()) {
                requires.setHeroesPrimaryClass(depends.getHeroClass(className).getName());
            } else if (depends.getHeroClass(className) != null) {
                throw new QuestFormatException("Requirement 'heroes-primary-class' is not a primary Heroes class",
                        questKey);
            } else {
                throw new QuestFormatException("Requirement 'heroes-primary-class' has invalid Heroes class", questKey);
            }
        }
        if (config.contains("quests." + questKey + ".requirements.heroes-secondary-class")) {
            final String className = config.getString("quests." + questKey + ".requirements.heroes-secondary-class");
            if (depends.getHeroClass(className) != null && depends.getHeroClass(className).isSecondary()) {
                requires.setHeroesSecondaryClass(depends.getHeroClass(className).getName());
            } else if (depends.getHeroClass(className) != null) {
                throw new QuestFormatException("Requirement 'heroes-secondary-class' is not a secondary Heroes class",
                        questKey);
            } else {
                throw new QuestFormatException("Requirement 'heroes-secondary-class' has invalid Heroes class", questKey);
            }
        }
        if (config.contains("quests." + questKey + ".requirements.details-override")) {
            if (BukkitConfigUtil.checkList(config.getList("quests." + questKey
                    + ".requirements.details-override"), String.class)) {
                requires.setDetailsOverride(config.getStringList("quests." + questKey + ".requirements.details-override"));
            }  else {
                throw new QuestFormatException("Requirement 'details-override' is not a list of strings", questKey);
            }
        }
    }

    private void loadQuestPlanner(final FileConfiguration config, final Quest quest, final String questKey)
            throws QuestFormatException {
        final Planner pln = quest.getPlanner();
        if (config.contains("quests." + questKey + ".planner.start")) {
            pln.setStart(config.getString("quests." + questKey + ".planner.start"));
        }
        if (config.contains("quests." + questKey + ".planner.end")) {
            pln.setEnd(config.getString("quests." + questKey + ".planner.end"));
        }
        if (config.contains("quests." + questKey + ".planner.repeat")) {
            if (config.getInt("quests." + questKey + ".planner.repeat", -999) != -999) {
                pln.setRepeat(config.getInt("quests." + questKey + ".planner.repeat") * 1000L);
            } else {
                throw new QuestFormatException("Planner 'repeat' is not a number", questKey);
            }
        }
        if (config.contains("quests." + questKey + ".planner.cooldown")) {
            if (config.getInt("quests." + questKey + ".planner.cooldown", -999) != -999) {
                pln.setCooldown(config.getInt("quests." + questKey + ".planner.cooldown") * 1000L);
            } else {
                throw new QuestFormatException("Planner 'cooldown' is not a number", questKey);
            }
        }
        if (config.contains("quests." + questKey + ".planner.override")) {
            pln.setOverride(config.getBoolean("quests." + questKey + ".planner.override"));
        }
    }

    private void loadQuestOptions(final FileConfiguration config, final Quest quest, final String questKey)
            throws QuestFormatException {
        final Options opts = quest.getOptions();
        if (config.contains("quests." + questKey + ".options.allow-commands")) {
            opts.setAllowCommands(config.getBoolean("quests." + questKey + ".options.allow-commands"));
        }
        if (config.contains("quests." + questKey + ".options.allow-quitting")) {
            opts.setAllowQuitting(config.getBoolean("quests." + questKey + ".options.allow-quitting"));
        } else if (plugin.getConfig().contains("allow-quitting")) {
            // Legacy
            opts.setAllowQuitting(plugin.getConfig().getBoolean("allow-quitting"));
        }
        if (config.contains("quests." + questKey + ".options.ignore-silk-touch")) {
            opts.setIgnoreSilkTouch(config.getBoolean("quests." + questKey + ".options.ignore-silk-touch"));
        }
        if (config.contains("quests." + questKey + ".options.external-party-plugin")) {
            opts.setExternalPartyPlugin(config.getString("quests." + questKey + ".options.external-party-plugin"));
        }
        if (config.contains("quests." + questKey + ".options.use-parties-plugin")) {
            opts.setUsePartiesPlugin(config.getBoolean("quests." + questKey + ".options.use-parties-plugin"));
        }
        if (config.contains("quests." + questKey + ".options.share-progress-level")) {
            opts.setShareProgressLevel(config.getInt("quests." + questKey + ".options.share-progress-level"));
        }
        if (config.contains("quests." + questKey + ".options.same-quest-only")) {
            opts.setShareSameQuestOnly(config.getBoolean("quests." + questKey + ".options.same-quest-only"));
        }
        if (config.contains("quests." + questKey + ".options.share-distance")) {
            opts.setShareDistance(config.getDouble("quests." + questKey + ".options.share-distance"));
        }
        if (config.contains("quests." + questKey + ".options.handle-offline-players")) {
            opts.setHandleOfflinePlayers(config.getBoolean("quests." + questKey + ".options.handle-offline-players"));
        }
        if (config.contains("quests." + questKey + ".options.ignore-block-replace")) {
            opts.setIgnoreBlockReplace(config.getBoolean("quests." + questKey + ".options.ignore-block-replace"));
        }
    }

    @SuppressWarnings({ "unchecked", "unused"})
    private void loadQuestStages(final Quest quest, final FileConfiguration config, final String questKey)
            throws StageFormatException, ActionFormatException, ConditionFormatException {
        final ConfigurationSection questStages = config.getConfigurationSection("quests." + questKey
                + ".stages.ordered");
        if (questStages == null) {
            plugin.getLogger().severe(ChatColor.RED + questKey + " must have at least one stage!");
            return;
        }
        for (final String stage : questStages.getKeys(false)) {
            final int stageNum;
            try {
                stageNum = Integer.parseInt(stage);
            } catch (final NumberFormatException e) {
                plugin.getLogger().severe("Stage key " + stage + "must be a number!");
                continue;
            }
            final BukkitStage oStage = new BukkitStage();
            List<String> breakNames = new LinkedList<>();
            List<Integer> breakAmounts = new LinkedList<>();
            List<Short> breakDurability = new LinkedList<>();
            List<String> damageNames = new LinkedList<>();
            List<Integer> damageAmounts = new LinkedList<>();
            List<Short> damageDurability = new LinkedList<>();
            List<String> placeNames = new LinkedList<>();
            List<Integer> placeAmounts = new LinkedList<>();
            List<Short> placeDurability = new LinkedList<>();
            List<String> useNames = new LinkedList<>();
            List<Integer> useAmounts = new LinkedList<>();
            List<Short> useDurability = new LinkedList<>();
            List<String> cutNames = new LinkedList<>();
            List<Integer> cutAmounts = new LinkedList<>();
            List<Short> cutDurability = new LinkedList<>();
            final List<EntityType> mobsToKill = new LinkedList<>();
            final List<Integer> mobNumsToKill = new LinkedList<>();
            final List<Location> locationsToKillWithin = new LinkedList<>();
            final List<Integer> radiiToKillWithin = new LinkedList<>();
            final List<String> areaNames = new LinkedList<>();
            final List<ItemStack> itemsToCraft;
            final List<ItemStack> itemsToSmelt;
            final List<ItemStack> itemsToEnchant;
            final List<ItemStack> itemsToBrew;
            final List<ItemStack> itemsToConsume;
            final List<String> npcUuidsToTalkTo;
            final List<Integer> npcIdsToTalkTo;
            final List<ItemStack> itemsToDeliver;
            final List<String> itemDeliveryTargetUuids;
            final List<Integer> itemDeliveryTargetIds;
            final List<String> deliveryMessages;
            final List<String> npcUuidsToKill;
            final List<Integer> npcIdsToKill;
            final List<Integer> npcAmountsToKill;
            // Legacy Denizen script load
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".script-to-run")) {
                if (plugin.getDependencies().getDenizenApi().containsScript(config.getString("quests." + questKey
                        + ".stages.ordered." + stageNum + ".script-to-run"))) {
                    oStage.setScript(config.getString("quests." + questKey + ".stages.ordered." + stageNum
                            + ".script-to-run"));
                } else {
                    throw new StageFormatException("'script-to-run' is not a valid Denizen script", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".break-block-names")) {
                if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum
                        + ".break-block-names"), String.class)) {
                    breakNames = config.getStringList("quests." + questKey + ".stages.ordered." + stageNum
                            + ".break-block-names");
                } else {
                    throw new StageFormatException("'break-block-names' is not a list of strings", quest, stageNum);
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".break-block-amounts")) {
                    if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum
                            + ".break-block-amounts"), Integer.class)) {
                        breakAmounts = config.getIntegerList("quests." + questKey + ".stages.ordered." + stageNum
                                + ".break-block-amounts");
                    } else {
                        throw new StageFormatException("'break-block-amounts' is not a list of numbers", quest, stageNum);
                    }
                } else {
                    throw new StageFormatException("'break-block-amounts' is missing", quest, stageNum);
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".break-block-durability")) {
                    if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum
                            + ".break-block-durability"), Integer.class)) {
                        breakDurability = config.getShortList("quests." + questKey + ".stages.ordered." + stageNum
                                + ".break-block-durability");
                    } else {
                        throw new StageFormatException("'break-block-durability' is not a list of numbers", quest,
                                stageNum);
                    }
                } else {
                    throw new StageFormatException("'break-block-durability' is missing", quest, stageNum);
                }
            }
            int breakIndex = 0;
            for (final String s : breakNames) {
                final ItemStack is;
                if (breakIndex < breakDurability.size() && breakDurability.get(breakIndex) != -1) {
                    is = BukkitItemUtil.processItemStack(s, breakAmounts.get(breakIndex), breakDurability.get(breakIndex));
                } else {
                    // Legacy
                    is = BukkitItemUtil.processItemStack(s, breakAmounts.get(breakIndex), (short) 0);
                }
                if (Material.matchMaterial(s) != null) {
                    oStage.addBlockToBreak(is);
                } else {
                    throw new StageFormatException("'break-block-names' has invalid item name " + s, quest, stageNum);
                }
                breakIndex++;
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".damage-block-names")) {
                if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum
                        + ".damage-block-names"), String.class)) {
                    damageNames = config.getStringList("quests." + questKey + ".stages.ordered." + stageNum
                            + ".damage-block-names");
                } else {
                    throw new StageFormatException("'damage-block-names' is not a list of strings", quest, stageNum);
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".damage-block-amounts")) {
                    if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum
                            + ".damage-block-amounts"), Integer.class)) {
                        damageAmounts = config.getIntegerList("quests." + questKey + ".stages.ordered." + stageNum
                                + ".damage-block-amounts");
                    } else {
                        throw new StageFormatException("'damage-block-amounts' is not a list of numbers", quest,
                                stageNum);
                    }
                } else {
                    throw new StageFormatException("'damage-block-amounts' is missing", quest, stageNum);
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + stageNum
                        + ".damage-block-durability")) {
                    if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum
                            + ".damage-block-durability"), Integer.class)) {
                        damageDurability = config.getShortList("quests." + questKey + ".stages.ordered." + stageNum
                                + ".damage-block-durability");
                    } else {
                        throw new StageFormatException("'damage-block-durability' is not a list of numbers", quest,
                                stageNum);
                    }
                } else {
                    throw new StageFormatException("'damage-block-durability' is missing", quest, stageNum);
                }
            }
            int damageIndex = 0;
            for (final String s : damageNames) {
                final ItemStack is;
                if (damageIndex < damageDurability.size() && damageDurability.get(damageIndex) != -1) {
                    is = BukkitItemUtil.processItemStack(s, damageAmounts.get(damageIndex),
                            damageDurability.get(damageIndex));
                } else {
                    // Legacy
                    is = BukkitItemUtil.processItemStack(s, damageAmounts.get(damageIndex), (short) 0);
                }
                if (Material.matchMaterial(s) != null) {
                    oStage.addBlockToDamage(is);
                } else {
                    throw new StageFormatException("'damage-block-names' has invalid item name " + s, quest, stageNum);
                }
                damageIndex++;
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".place-block-names")) {
                if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum
                        + ".place-block-names"), String.class)) {
                    placeNames = config.getStringList("quests." + questKey + ".stages.ordered." + stageNum
                            + ".place-block-names");
                } else {
                    throw new StageFormatException("'place-block-names' is not a list of strings", quest, stageNum);
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".place-block-amounts")) {
                    if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum
                            + ".place-block-amounts"), Integer.class)) {
                        placeAmounts = config.getIntegerList("quests." + questKey + ".stages.ordered." + stageNum
                                + ".place-block-amounts");
                    } else {
                        throw new StageFormatException("'place-block-amounts' is not a list of numbers", quest, stageNum);
                    }
                } else {
                    throw new StageFormatException("'place-block-amounts' is missing", quest, stageNum);
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".place-block-durability")) {
                    if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum
                            + ".place-block-durability"), Integer.class)) {
                        placeDurability = config.getShortList("quests." + questKey + ".stages.ordered." + stageNum
                                + ".place-block-durability");
                    } else {
                        throw new StageFormatException("'place-block-durability' is not a list of numbers", quest,
                                stageNum);
                    }
                } else {
                    throw new StageFormatException("'place-block-durability' is missing", quest, stageNum);
                }
            }
            int placeIndex = 0;
            for (final String s : placeNames) {
                final ItemStack is;
                if (placeIndex < placeDurability.size() && placeDurability.get(placeIndex) != -1) {
                    is = BukkitItemUtil.processItemStack(s, placeAmounts.get(placeIndex), placeDurability.get(placeIndex));
                } else {
                    // Legacy
                    is = BukkitItemUtil.processItemStack(s, placeAmounts.get(placeIndex), (short) 0);
                }
                if (Material.matchMaterial(s) != null) {
                    oStage.addBlockToPlace(is);
                } else {
                    throw new StageFormatException("'place-block-names' has invalid item name " + s, quest, stageNum);
                }
                placeIndex++;
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".use-block-names")) {
                if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum
                        + ".use-block-names"), String.class)) {
                    useNames = config.getStringList("quests." + questKey + ".stages.ordered." + stageNum
                            + ".use-block-names");
                } else {
                    throw new StageFormatException("'use-block-names' is not a list of strings", quest, stageNum);
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".use-block-amounts")) {
                    if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum
                            + ".use-block-amounts"),Integer.class)) {
                        useAmounts = config.getIntegerList("quests." + questKey + ".stages.ordered." + stageNum
                                + ".use-block-amounts");
                    } else {
                        throw new StageFormatException("'use-block-amounts' is not a list of numbers", quest, stageNum);
                    }
                } else {
                    throw new StageFormatException("'use-block-amounts' is missing", quest, stageNum);
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".use-block-durability")) {
                    if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum
                            + ".use-block-durability"), Integer.class)) {
                        useDurability = config.getShortList("quests." + questKey + ".stages.ordered." + stageNum
                                + ".use-block-durability");
                    } else {
                        throw new StageFormatException("'use-block-durability' is not a list of numbers", quest,
                                stageNum);
                    }
                } else {
                    throw new StageFormatException("'use-block-durability' is missing", quest, stageNum);
                }
            }
            int useIndex = 0;
            for (final String s : useNames) {
                final ItemStack is;
                if (useIndex < useDurability.size() && useDurability.get(useIndex) != -1) {
                    is = BukkitItemUtil.processItemStack(s, useAmounts.get(useIndex), useDurability.get(useIndex));
                } else {
                    // Legacy
                    is = BukkitItemUtil.processItemStack(s, useAmounts.get(useIndex), (short) 0);
                }
                if (Material.matchMaterial(s) != null) {
                    oStage.addBlockToUse(is);
                } else {
                    throw new StageFormatException("'use-block-names' has invalid item name " + s, quest, stageNum);
                }
                useIndex++;
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".cut-block-names")) {
                if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum
                        + ".cut-block-names"), String.class)) {
                    cutNames = config.getStringList("quests." + questKey + ".stages.ordered." + stageNum
                            + ".cut-block-names");
                } else {
                    throw new StageFormatException("'cut-block-names' is not a list of strings", quest, stageNum);
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".cut-block-amounts")) {
                    if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum
                            + ".cut-block-amounts"), Integer.class)) {
                        cutAmounts = config.getIntegerList("quests." + questKey + ".stages.ordered." + stageNum
                                + ".cut-block-amounts");
                    } else {
                        throw new StageFormatException("'cut-block-amounts' is not a list of numbers", quest, stageNum);
                    }
                } else {
                    throw new StageFormatException("'cut-block-amounts' is missing", quest, stageNum);
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".cut-block-durability")) {
                    if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum
                            + ".cut-block-durability"), Integer.class)) {
                        cutDurability = config.getShortList("quests." + questKey + ".stages.ordered." + stageNum
                                + ".cut-block-durability");
                    } else {
                        throw new StageFormatException("'cut-block-durability' is not a list of numbers", quest,
                                stageNum);
                    }
                } else {
                    throw new StageFormatException("'cut-block-durability' is missing", quest, stageNum);
                }
            }
            int cutIndex = 0;
            for (final String s : cutNames) {
                final ItemStack is;
                if (cutIndex < cutDurability.size() && cutDurability.get(cutIndex) != -1) {
                    is = BukkitItemUtil.processItemStack(s, cutAmounts.get(cutIndex), cutDurability.get(cutIndex));
                } else {
                    // Legacy
                    is = BukkitItemUtil.processItemStack(s, cutAmounts.get(cutIndex), (short) 0);
                }
                if (Material.matchMaterial(s) != null) {
                    oStage.addBlockToCut(is);
                } else {
                    throw new StageFormatException("'cut-block-names' has invalid item name " + s, quest, stageNum);
                }
                cutIndex++;
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".items-to-craft")) {
                itemsToCraft = (List<ItemStack>) config.get("quests." + questKey + ".stages.ordered." + stageNum
                        + ".items-to-craft");
                if (BukkitConfigUtil.checkList(itemsToCraft, ItemStack.class)) {
                    for (final ItemStack stack : itemsToCraft) {
                        if (stack != null) {
                            oStage.addItemToCraft(stack);
                        } else {
                            throw new StageFormatException("'items-to-craft' has invalid formatting", quest, stageNum);
                        }
                    }
                } else {
                    throw new StageFormatException("'items-to-craft' is not formatted properly", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".items-to-smelt")) {
                itemsToSmelt = (List<ItemStack>) config.get("quests." + questKey + ".stages.ordered." + stageNum
                        + ".items-to-smelt");
                if (BukkitConfigUtil.checkList(itemsToSmelt, ItemStack.class)) {
                    for (final ItemStack stack : itemsToSmelt) {
                        if (stack != null) {
                            oStage.addItemToSmelt(stack);
                        } else {
                            throw new StageFormatException("'items-to-smelt' has invalid formatting", quest, stageNum);
                        }
                    }
                } else {
                    throw new StageFormatException("'items-to-smelt' is not formatted properly", quest, stageNum);
                }
            }

            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".items-to-enchant")) {
                itemsToEnchant = (List<ItemStack>) config.get("quests." + questKey + ".stages.ordered." + stageNum
                        + ".items-to-enchant");
                if (BukkitConfigUtil.checkList(itemsToEnchant, ItemStack.class)) {
                    for (final ItemStack stack : itemsToEnchant) {
                        if (stack != null) {
                            oStage.addItemToEnchant(stack);
                        } else {
                            throw new StageFormatException("'items-to-enchant' has invalid formatting", quest, stageNum);
                        }
                    }
                } else {
                    // Legacy
                    final LinkedList<Material> types = new LinkedList<>();
                    final LinkedList<Enchantment> enchs = new LinkedList<>();
                    final LinkedList<Integer> amts;
                    if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".enchantments")) {
                        if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum
                                + ".enchantments"), String.class)) {
                            for (final String enchant : config.getStringList("quests." + questKey + ".stages.ordered." + stageNum
                                    + ".enchantments")) {
                                final Enchantment e = BukkitItemUtil.getEnchantmentFromProperName(enchant);
                                if (e != null) {
                                    enchs.add(e);
                                } else {
                                    throw new StageFormatException("'enchantments' has invalid enchantment "
                                            + enchant, quest, stageNum);
                                }
                            }
                        } else {
                            throw new StageFormatException("'enchantments' is not a list of enchantment names", quest, stageNum);
                        }
                        if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".enchantment-item-names")) {
                            if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum
                                    + ".enchantment-item-names"), String.class)) {
                                for (final String item : config.getStringList("quests." + questKey + ".stages.ordered." + stageNum
                                        + ".enchantment-item-names")) {
                                    if (Material.matchMaterial(item) != null) {
                                        types.add(Material.matchMaterial(item));
                                    } else {
                                        throw new StageFormatException("'enchantment-item-names' has invalid item name "
                                                + item, quest, stageNum);
                                    }
                                }
                            } else {
                                throw new StageFormatException("'enchantment-item-names' has invalid item name", quest, stageNum);
                            }
                        } else {
                            throw new StageFormatException("'enchantment-item-names' is missing", quest, stageNum);
                        }
                        if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".enchantment-amounts")) {
                            if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum
                                    + ".enchantment-amounts"), Integer.class)) {
                                amts = new LinkedList<>(config.getIntegerList("quests." + questKey + ".stages.ordered."
                                        + stageNum + ".enchantment-amounts"));
                            } else {
                                throw new StageFormatException("'enchantment-amounts' is not a list of numbers", quest, stageNum);
                            }
                        } else {
                            throw new StageFormatException("'enchantment-amounts' is missing", quest, stageNum);
                        }
                        if (!enchs.isEmpty() && !types.isEmpty() && !amts.isEmpty()) {
                            for (int i = 0; i < enchs.size(); i++) {
                                final ItemStack stack = new ItemStack(types.get(i), amts.get(i));
                                stack.addEnchantment(enchs.get(0), 1);
                                oStage.addItemToEnchant(stack);
                            }
                        }
                    }
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".items-to-brew")) {
                itemsToBrew = (List<ItemStack>) config.get("quests." + questKey + ".stages.ordered." + stageNum
                        + ".items-to-brew");
                if (BukkitConfigUtil.checkList(itemsToBrew, ItemStack.class)) {
                    for (final ItemStack stack : itemsToBrew) {
                        if (stack != null) {
                            oStage.addItemsToBrew(stack);
                        } else {
                            throw new StageFormatException("'items-to-brew' has invalid formatting", quest, stageNum);
                        }
                    }
                } else {
                    throw new StageFormatException("'items-to-brew' has invalid formatting", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".items-to-consume")) {
                itemsToConsume = (List<ItemStack>) config.get("quests." + questKey + ".stages.ordered." + stageNum
                        + ".items-to-consume");
                if (BukkitConfigUtil.checkList(itemsToConsume, ItemStack.class)) {
                    for (final ItemStack stack : itemsToConsume) {
                        if (stack != null) {
                            oStage.addItemToConsume(stack);
                        } else {
                            throw new StageFormatException("'items-to-consume' has invalid formatting", quest, stageNum);
                        }
                    }
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".cows-to-milk")) {
                if (config.getInt("quests." + questKey + ".stages.ordered." + stageNum + ".cows-to-milk", -999)
                        != -999) {
                    oStage.setCowsToMilk(config.getInt("quests." + questKey + ".stages.ordered." + stageNum
                            + ".cows-to-milk"));
                } else {
                    throw new StageFormatException("'cows-to-milk' is not a number", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".fish-to-catch")) {
                if (config.getInt("quests." + questKey + ".stages.ordered." + stageNum + ".fish-to-catch", -999)
                        != -999) {
                    oStage.setFishToCatch(config.getInt("quests." + questKey + ".stages.ordered." + stageNum
                            + ".fish-to-catch"));
                } else {
                    throw new StageFormatException("'fish-to-catch' is not a number", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".players-to-kill")) {
                if (config.getInt("quests." + questKey + ".stages.ordered." + stageNum + ".players-to-kill", -999)
                        != -999) {
                    oStage.setPlayersToKill(config.getInt("quests." + questKey + ".stages.ordered." + stageNum
                            + ".players-to-kill"));
                } else {
                    throw new StageFormatException("'players-to-kill' is not a number", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".npc-uuids-to-talk-to")) {
                if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum
                        + ".npc-uuids-to-talk-to"), String.class)) {
                    npcUuidsToTalkTo = config.getStringList("quests." + questKey + ".stages.ordered." + stageNum
                            + ".npc-uuids-to-talk-to");
                    for (final String s : npcUuidsToTalkTo) {
                        final UUID uuid = UUID.fromString(s);
                        oStage.addNpcToInteract(uuid);
                        final Collection<UUID> npcUuids = plugin.getQuestNpcUuids();
                        npcUuids.add(uuid);
                        plugin.setQuestNpcUuids(npcUuids);
                    }
                } else {
                    throw new StageFormatException("'npc-uuids-to-talk-to' is not a list of numbers", quest, stageNum);
                }
            } else if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".npc-ids-to-talk-to")) {
                // Legacy
                if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum
                        + ".npc-ids-to-talk-to"), Integer.class)) {
                    npcIdsToTalkTo = config.getIntegerList("quests." + questKey + ".stages.ordered." + stageNum
                            + ".npc-ids-to-talk-to");
                    for (final int i : npcIdsToTalkTo) {
                        if (plugin.getDependencies().getCitizens() != null) {
                            final NPC npc = CitizensAPI.getNPCRegistry().getById(i);
                            if (npc != null) {
                                final UUID npcUuid = npc.getUniqueId();
                                oStage.addNpcToInteract(npcUuid);
                                final Collection<UUID> npcUuids = plugin.getQuestNpcUuids();
                                npcUuids.add(npcUuid);
                                plugin.setQuestNpcUuids(npcUuids);
                            } else {
                                throw new StageFormatException("'npc-ids-to-talk-to' has invalid NPC ID of " + i, quest,
                                        stageNum);
                            }
                        } else {
                            throw new StageFormatException("Citizens not found for 'npc-ids-to-talk-to'", quest,
                                    stageNum);
                        }
                    }
                } else {
                    throw new StageFormatException("'npc-ids-to-talk-to' is not a list of numbers", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".items-to-deliver")) {
                if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".npc-delivery-uuids")) {
                    if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum
                            + ".npc-delivery-uuids"), String.class)) {
                        if (config.contains("quests." + questKey + ".stages.ordered." + stageNum
                                + ".delivery-messages")) {
                            itemsToDeliver = (List<ItemStack>) config.get("quests." + questKey + ".stages.ordered."
                                    + stageNum + ".items-to-deliver");
                            itemDeliveryTargetUuids = config.getStringList("quests." + questKey + ".stages.ordered."
                                    + stageNum + ".npc-delivery-uuids");
                            deliveryMessages = config.getStringList("quests." + questKey + ".stages.ordered."
                                    + stageNum + ".delivery-messages");
                            int index = 0;
                            if (BukkitConfigUtil.checkList(itemsToDeliver, ItemStack.class)) {
                                for (final ItemStack stack : itemsToDeliver) {
                                    if (stack != null) {
                                        final UUID npcUuid = UUID.fromString(itemDeliveryTargetUuids.get(index));
                                        final String msg = deliveryMessages.size() > index
                                                ? deliveryMessages.get(index)
                                                : deliveryMessages.get(deliveryMessages.size() - 1);
                                        index++;
                                        oStage.addItemToDeliver(stack);
                                        oStage.addItemDeliveryTarget(npcUuid);
                                        oStage.addDeliverMessage(msg);
                                    }
                                }
                            } else {
                                throw new StageFormatException("'items-to-deliver' has invalid formatting", quest,
                                        stageNum);
                            }
                        }
                    } else {
                        throw new StageFormatException("'npc-delivery-uuids' is not a list of numbers", quest, stageNum);
                    }
                } else if (config.contains("quests." + questKey + ".stages.ordered." + stageNum
                        + ".npc-delivery-ids")) {
                    // Legacy
                    if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum
                            + ".npc-delivery-ids"), Integer.class)) {
                        if (config.contains("quests." + questKey + ".stages.ordered." + stageNum
                                + ".delivery-messages")) {
                            itemsToDeliver = (List<ItemStack>) config.get("quests." + questKey + ".stages.ordered."
                                    + stageNum + ".items-to-deliver");
                            itemDeliveryTargetIds = config.getIntegerList("quests." + questKey + ".stages.ordered."
                                    + stageNum + ".npc-delivery-ids");
                            deliveryMessages = config.getStringList("quests." + questKey + ".stages.ordered."
                                    + stageNum + ".delivery-messages");
                            int index = 0;
                            if (BukkitConfigUtil.checkList(itemsToDeliver, ItemStack.class)) {
                                for (final ItemStack stack : itemsToDeliver) {
                                    if (stack != null) {
                                        final int npcId = itemDeliveryTargetIds.get(index);
                                        final String msg = deliveryMessages.size() > index
                                                ? deliveryMessages.get(index)
                                                : deliveryMessages.get(deliveryMessages.size() - 1);
                                        index++;
                                        if (plugin.getDependencies().getCitizens() != null) {
                                            final NPC npc = CitizensAPI.getNPCRegistry().getById(npcId);
                                            if (npc != null) {
                                                oStage.addItemToDeliver(stack);
                                                oStage.addItemDeliveryTarget(npc.getUniqueId());
                                                oStage.addDeliverMessage(msg);
                                            } else {
                                                throw new StageFormatException("'npc-delivery-ids' has invalid NPC " +
                                                        "ID of " + npcId, quest, stageNum);
                                            }
                                        } else {
                                            throw new StageFormatException(
                                                    "Citizens not found for 'npc-delivery-ids'", quest, stageNum);
                                        }
                                    }
                                }
                            } else {
                                throw new StageFormatException("'items-to-deliver' has invalid formatting", quest,
                                        stageNum);
                            }
                        }
                    } else {
                        throw new StageFormatException("'npc-delivery-ids' is not a list of numbers", quest, stageNum);
                    }
                } else {
                    throw new StageFormatException("'npc-delivery-uuid' is missing", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".npc-uuids-to-kill")) {
                if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum
                        + ".npc-uuids-to-kill"), String.class)) {
                    if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".npc-kill-amounts")) {
                        if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum
                                + ".npc-kill-amounts"), Integer.class)) {
                            npcUuidsToKill = config.getStringList("quests." + questKey + ".stages.ordered." + stageNum
                                    + ".npc-uuids-to-kill");
                            npcAmountsToKill = config.getIntegerList("quests." + questKey + ".stages.ordered."
                                    + stageNum + ".npc-kill-amounts");
                            for (final String s : npcUuidsToKill) {
                                final UUID npcUuid = UUID.fromString(s);
                                if (npcAmountsToKill.get(npcUuidsToKill.indexOf(s)) > 0) {
                                    oStage.addNpcToKill(npcUuid);
                                    oStage.addNpcNumToKill(npcAmountsToKill.get(npcUuidsToKill.indexOf(s)));
                                    final Collection<UUID> npcUuids = plugin.getQuestNpcUuids();
                                    npcUuids.add(npcUuid);
                                    plugin.setQuestNpcUuids(npcUuids);
                                } else {
                                    throw new StageFormatException("'npc-kill-amounts' is not a positive number",
                                            quest, stageNum);
                                }
                            }
                        } else {
                            throw new StageFormatException("'npc-kill-amounts' is not a list of numbers", quest,
                                    stageNum);
                        }
                    } else {
                        throw new StageFormatException("'npc-kill-amounts' is missing", quest, stageNum);
                    }
                }
            } else if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".npc-ids-to-kill")) {
                if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum
                        + ".npc-ids-to-kill"), Integer.class)) {
                    // Legacy
                    if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".npc-kill-amounts")) {
                        if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum
                                + ".npc-kill-amounts"), Integer.class)) {
                            npcIdsToKill = config.getIntegerList("quests." + questKey + ".stages.ordered." + stageNum
                                    + ".npc-ids-to-kill");
                            npcAmountsToKill = config.getIntegerList("quests." + questKey + ".stages.ordered."
                                    + stageNum + ".npc-kill-amounts");
                            for (final int i : npcIdsToKill) {
                                if (plugin.getDependencies().getCitizens() != null) {
                                    final NPC npc = CitizensAPI.getNPCRegistry().getById(i);
                                    if (npc != null) {
                                        if (npcAmountsToKill.get(npcIdsToKill.indexOf(i)) > 0) {
                                            final UUID npcUuid = npc.getUniqueId();
                                            oStage.addNpcToKill(npcUuid);
                                            oStage.addNpcNumToKill(npcAmountsToKill.get(npcIdsToKill.indexOf(i)));
                                            final Collection<UUID> npcUuids = plugin.getQuestNpcUuids();
                                            npcUuids.add(npcUuid);
                                            plugin.setQuestNpcUuids(npcUuids);
                                        } else {
                                            throw new StageFormatException("'npc-kill-amounts' is not a positive number",
                                                    quest, stageNum);
                                        }
                                    } else {
                                        throw new StageFormatException("'npc-ids-to-kill' has invalid NPC ID of " + i, quest,
                                                stageNum);
                                    }
                                } else {
                                    throw new StageFormatException(
                                            "Citizens not found for 'npc-ids-to-kill'", quest, stageNum);
                                }
                            }
                        } else {
                            throw new StageFormatException("'npc-kill-amounts' is not a list of numbers", quest,
                                    stageNum);
                        }
                    } else {
                        throw new StageFormatException("'npc-kill-amounts' is missing", quest, stageNum);
                    }
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".mobs-to-kill")) {
                if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum
                        + ".mobs-to-kill"), String.class)) {
                    final List<String> mobNames = config.getStringList("quests." + questKey + ".stages.ordered."
                            + stageNum + ".mobs-to-kill");
                    for (final String mob : mobNames) {
                        final EntityType type = BukkitMiscUtil.getProperMobType(mob);
                        if (type != null) {
                            mobsToKill.add(type);
                        } else {
                            throw new StageFormatException("'mobs-to-kill' has invalid mob name " + mob, quest, stageNum);
                        }
                    }
                } else {
                    throw new StageFormatException("'mobs-to-kill' is not a list of mob names", quest, stageNum);
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".mob-amounts")) {
                    if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum
                            + ".mob-amounts"), Integer.class)) {
                        mobNumsToKill.addAll(config.getIntegerList("quests." + questKey + ".stages.ordered." + stageNum
                                + ".mob-amounts"));
                    } else {
                        throw new StageFormatException("'mob-amounts' is not a list of numbers", quest, stageNum);
                    }
                } else {
                    throw new StageFormatException("'mob-amounts' is missing", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".locations-to-kill")) {
                if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum
                        + ".locations-to-kill"), String.class)) {
                    final List<String> locations = config.getStringList("quests." + questKey + ".stages.ordered."
                            + stageNum + ".locations-to-kill");
                    for (final String loc : locations) {
                        if (BukkitConfigUtil.getLocation(loc) != null) {
                            locationsToKillWithin.add(BukkitConfigUtil.getLocation(loc));
                        } else {
                            throw new StageFormatException("'locations-to-kill' has invalid formatting " + loc, quest,
                                    stageNum);
                        }
                    }
                } else {
                    throw new StageFormatException("'locations-to-kill' is not a list of locations", quest, stageNum);
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".kill-location-radii")) {
                    if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum
                            + ".kill-location-radii"), Integer.class)) {
                        final List<Integer> radii = config.getIntegerList("quests." + questKey + ".stages.ordered."
                                + stageNum + ".kill-location-radii");
                        radiiToKillWithin.addAll(radii);
                    } else {
                        throw new StageFormatException("'kill-location-radii' is not a list of numbers", quest, stageNum);
                    }
                } else {
                    throw new StageFormatException("'kill-location-radii' is missing", quest, stageNum);
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".kill-location-names")) {
                    if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum
                            + ".kill-location-names"), String.class)) {
                        final List<String> locationNames = config.getStringList("quests." + questKey
                                + ".stages.ordered." + stageNum + ".kill-location-names");
                        areaNames.addAll(locationNames);
                    } else {
                        throw new StageFormatException("'kill-location-names' is not a list of names", quest, stageNum);
                    }
                } else {
                    throw new StageFormatException("'kill-location-names' is missing", quest, stageNum);
                }
            }
            for (EntityType mobToKill : mobsToKill) {
                oStage.addMobToKill(mobToKill);
            }
            for (Integer mobNumToKill : mobNumsToKill) {
                oStage.addMobNumToKill(mobNumToKill);
            }
            for (Location locationToKillWithin : locationsToKillWithin) {
                oStage.addLocationToKillWithin(locationToKillWithin);
            }
            for (Integer radiusToKillWithin : radiiToKillWithin) {
                oStage.addRadiusToKillWithin(radiusToKillWithin);
            }
            for (String killName : areaNames) {
                oStage.addKillName(killName);
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".locations-to-reach")) {
                if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum
                        + ".locations-to-reach"), String.class)) {
                    final List<String> locations = config.getStringList("quests." + questKey + ".stages.ordered." + stageNum
                            + ".locations-to-reach");
                    for (final String loc : locations) {
                        if (BukkitConfigUtil.getLocation(loc) != null) {
                            oStage.addLocationToReach(BukkitConfigUtil.getLocation(loc));
                        } else {
                            throw new StageFormatException("'locations-to-reach' has invalid formatting" + loc, quest,
                                    stageNum);
                        }
                    }
                } else {
                    throw new StageFormatException("'locations-to-reach' is not a list of locations", quest, stageNum);
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".reach-location-radii")) {
                    if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum
                            + ".reach-location-radii"), Integer.class)) {
                        final List<Integer> radii = config.getIntegerList("quests." + questKey + ".stages.ordered."
                                + stageNum + ".reach-location-radii");
                        for (Integer radius : radii) {
                            oStage.addRadiusToReachWithin(radius);
                        }
                    } else {
                        throw new StageFormatException("'reach-location-radii' is not a list of numbers", quest,
                                stageNum);
                    }
                } else {
                    throw new StageFormatException("'reach-location-radii' is missing", quest, stageNum);
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".reach-location-names")) {
                    if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum
                            + ".reach-location-names"), String.class)) {
                        final List<String> locationNames = config.getStringList("quests." + questKey
                                + ".stages.ordered." + stageNum + ".reach-location-names");
                        for (String locationName : locationNames) {
                            oStage.addLocationName(locationName);
                        }
                    } else {
                        throw new StageFormatException("'reach-location-names' is not a list of names", quest, stageNum);
                    }
                } else {
                    throw new StageFormatException("'reach-location-names' is missing", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".mobs-to-tame")) {
                if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum
                        + ".mobs-to-tame"), String.class)) {
                    if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".mob-tame-amounts")) {
                        if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum
                                + ".mob-tame-amounts"), Integer.class)) {
                            final List<String> mobs = config.getStringList("quests." + questKey + ".stages.ordered."
                                    + stageNum + ".mobs-to-tame");
                            final List<Integer> mobAmounts = config.getIntegerList("quests." + questKey + ".stages.ordered."
                                    + stageNum + ".mob-tame-amounts");
                            for (final String mob : mobs) {
                                final EntityType type = BukkitMiscUtil.getProperMobType(mob);
                                if (type != null) {
                                    final Class<? extends Entity> ec = type.getEntityClass();
                                    if (ec != null && Tameable.class.isAssignableFrom(ec)) {
                                        oStage.addMobToTame(type);
                                        oStage.addMobNumToTame(mobAmounts.get(mobs.indexOf(mob)));
                                    } else {
                                        throw new StageFormatException("'mobs-to-tame' has invalid tameable mob " + mob,
                                                quest, stageNum);
                                    }
                                } else {
                                    throw new StageFormatException("'mobs-to-tame' has invalid mob name " + mob, quest, stageNum);
                                }
                            }
                        } else {
                            throw new StageFormatException("'mob-tame-amounts' is not a list of numbers", quest,
                                    stageNum);
                        }
                    } else {
                        throw new StageFormatException("'mob-tame-amounts' is missing", quest, stageNum);
                    }
                } else {
                    throw new StageFormatException("'mobs-to-tame' is not a list of mob names", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".sheep-to-shear")) {
                if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum
                        + ".sheep-to-shear"), String.class)) {
                    if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".sheep-amounts")) {
                        if (BukkitConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum
                                + ".sheep-amounts"), Integer.class)) {
                            final List<String> sheep = config.getStringList("quests." + questKey + ".stages.ordered."
                                    + stageNum + ".sheep-to-shear");
                            final List<Integer> shearAmounts = config.getIntegerList("quests." + questKey
                                    + ".stages.ordered." + stageNum + ".sheep-amounts");
                            for (String sheepColor : sheep) {
                                final String originalColor = sheepColor;
                                DyeColor dc = null;
                                if (sheepColor.equalsIgnoreCase("NULL")) {
                                    dc = DyeColor.WHITE;
                                }
                                sheepColor = sheepColor.replace(" ", "_");
                                try {
                                    if (dc == null) {
                                        for (final DyeColor val : DyeColor.values()) {
                                            if (val.name().replace("_", "").equalsIgnoreCase(sheepColor
                                                    .replace("_", ""))) {
                                                dc = val;
                                                break;
                                            }
                                        }
                                    }
                                } catch (final IllegalArgumentException e) {
                                    // Fail silently
                                }
                                if (dc != null) {
                                    oStage.addSheepToShear(dc);
                                    // Legacy start -->
                                } else if (sheepColor.equalsIgnoreCase(BukkitLang.get("COLOR_BLACK"))) {
                                    oStage.addSheepToShear(DyeColor.BLACK);
                                } else if (sheepColor.equalsIgnoreCase(BukkitLang.get("COLOR_BLUE"))) {
                                    oStage.addSheepToShear(DyeColor.BLUE);
                                } else if (sheepColor.equalsIgnoreCase(BukkitLang.get("COLOR_BROWN"))) {
                                    oStage.addSheepToShear(DyeColor.BROWN);
                                } else if (sheepColor.equalsIgnoreCase(BukkitLang.get("COLOR_CYAN"))) {
                                    oStage.addSheepToShear(DyeColor.CYAN);
                                } else if (sheepColor.equalsIgnoreCase(BukkitLang.get("COLOR_GRAY"))) {
                                    oStage.addSheepToShear(DyeColor.GRAY);
                                } else if (sheepColor.equalsIgnoreCase(BukkitLang.get("COLOR_GREEN"))) {
                                    oStage.addSheepToShear(DyeColor.GREEN);
                                } else if (sheepColor.equalsIgnoreCase(BukkitLang.get("COLOR_LIGHT_BLUE"))) {
                                    oStage.addSheepToShear(DyeColor.LIGHT_BLUE);
                                } else if (sheepColor.equalsIgnoreCase(BukkitLang.get("COLOR_LIME"))) {
                                    oStage.addSheepToShear(DyeColor.LIME);
                                } else if (sheepColor.equalsIgnoreCase(BukkitLang.get("COLOR_MAGENTA"))) {
                                    oStage.addSheepToShear(DyeColor.MAGENTA);
                                } else if (sheepColor.equalsIgnoreCase(BukkitLang.get("COLOR_ORANGE"))) {
                                    oStage.addSheepToShear(DyeColor.ORANGE);
                                } else if (sheepColor.equalsIgnoreCase(BukkitLang.get("COLOR_PINK"))) {
                                    oStage.addSheepToShear(DyeColor.PINK);
                                } else if (sheepColor.equalsIgnoreCase(BukkitLang.get("COLOR_PURPLE"))) {
                                    oStage.addSheepToShear(DyeColor.PURPLE);
                                } else if (sheepColor.equalsIgnoreCase(BukkitLang.get("COLOR_RED"))) {
                                    oStage.addSheepToShear(DyeColor.RED);
                                } else if (sheepColor.equalsIgnoreCase(BukkitLang.get("COLOR_SILVER"))) {
                                    // 1.13 changed DyeColor.SILVER -> DyeColor.LIGHT_GRAY
                                    oStage.addSheepToShear(DyeColor.getByColor(Color.SILVER));
                                } else if (sheepColor.equalsIgnoreCase(BukkitLang.get("COLOR_WHITE"))) {
                                    oStage.addSheepToShear(DyeColor.WHITE);
                                } else if (sheepColor.equalsIgnoreCase(BukkitLang.get("COLOR_YELLOW"))) {
                                    oStage.addSheepToShear(DyeColor.YELLOW);
                                    // <-- Legacy end
                                } else {
                                    throw new StageFormatException("'sheep-to-shear' has invalid color " + sheepColor,
                                            quest, stageNum);
                                }
                                oStage.addSheepNumToShear(shearAmounts.get(sheep.indexOf(originalColor)));
                            }
                        } else {
                            throw new StageFormatException("'sheep-amounts' is not a list of numbers", quest, stageNum);
                        }
                    } else {
                        throw new StageFormatException("'sheep-amounts' is missing", quest, stageNum);
                    }
                } else {
                    throw new StageFormatException("'sheep-to-shear' is not a list of colors", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".password-displays")) {
                final List<String> displays = config.getStringList("quests." + questKey + ".stages.ordered." + stageNum
                        + ".password-displays");
                if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".password-phrases")) {
                    final List<String> phrases = config.getStringList("quests." + questKey + ".stages.ordered."
                            + stageNum + ".password-phrases");
                    if (displays.size() == phrases.size()) {
                        for (int passIndex = 0; passIndex < displays.size(); passIndex++) {
                            oStage.addPasswordDisplay(displays.get(passIndex));
                            oStage.addPasswordPhrase(phrases.get(passIndex));
                        }
                    } else {
                        throw new StageFormatException("'password-displays' and 'password-phrases' are not the same size",
                                quest, stageNum);
                    }
                } else {
                    throw new StageFormatException("'password-phrases' is missing", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".objective-override")) {
                final Object o = config.get("quests." + questKey + ".stages.ordered." + stageNum
                        + ".objective-override");
                if (o instanceof List) {
                    for (String objectiveOverride : config.getStringList("quests." + questKey
                            + ".stages.ordered." + stageNum + ".objective-override")) {
                        oStage.addObjectiveOverride(objectiveOverride);
                    }
                } else {
                    // Legacy
                    final String s = config.getString("quests." + questKey + ".stages.ordered." + stageNum
                            + ".objective-override");
                    oStage.addObjectiveOverride(s);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".start-event")) {
                final String actionName = config.getString("quests." + questKey + ".stages.ordered." + stageNum
                        + ".start-event");
                final Optional<Action> action = plugin.getLoadedActions().stream()
                        .filter(a -> a.getName().equals(actionName)).findAny();
                if (action.isPresent()) {
                    oStage.setStartAction(action.get());
                } else {
                    throw new StageFormatException("'start-event' failed to load", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".finish-event")) {
                final String actionName = config.getString("quests." + questKey + ".stages.ordered." + stageNum
                        + ".finish-event");
                final Optional<Action> action = plugin.getLoadedActions().stream()
                        .filter(a -> a.getName().equals(actionName)).findAny();
                if (action.isPresent()) {
                    oStage.setFinishAction(action.get());
                } else {
                    throw new StageFormatException("'finish-event' failed to load", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".fail-event")) {
                final String actionName = config.getString("quests." + questKey + ".stages.ordered." + stageNum
                        + ".fail-event");
                final Optional<Action> action = plugin.getLoadedActions().stream()
                        .filter(a -> a.getName().equals(actionName)).findAny();
                if (action.isPresent()) {
                    oStage.setFailAction(action.get());
                } else {
                    throw new StageFormatException("'fail-event' failed to load", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".death-event")) {
                final String actionName = config.getString("quests." + questKey + ".stages.ordered." + stageNum
                        + ".death-event");
                final Optional<Action> action = plugin.getLoadedActions().stream()
                        .filter(a -> a.getName().equals(actionName)).findAny();
                if (action.isPresent()) {
                    oStage.setDeathAction(action.get());
                } else {
                    throw new StageFormatException("'death-event' failed to load", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".disconnect-event")) {
                final String actionName = config.getString("quests." + questKey + ".stages.ordered." + stageNum
                        + ".disconnect-event");
                final Optional<Action> action = plugin.getLoadedActions().stream()
                        .filter(a -> a.getName().equals(actionName)).findAny();
                if (action.isPresent()) {
                    oStage.setDisconnectAction(action.get());
                } else {
                    throw new StageFormatException("'disconnect-event' failed to load", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".chat-events")) {
                if (config.isList("quests." + questKey + ".stages.ordered." + stageNum + ".chat-events")) {
                    if (config.contains("quests." + questKey + ".stages.ordered." + stageNum
                            + ".chat-event-triggers")) {
                        if (config.isList("quests." + questKey + ".stages.ordered." + stageNum
                                + ".chat-event-triggers")) {
                            final List<String> chatEvents = config.getStringList("quests." + questKey
                                    + ".stages.ordered." + stageNum + ".chat-events");
                            final List<String> chatEventTriggers = config.getStringList("quests." + questKey
                                    + ".stages.ordered." + stageNum + ".chat-event-triggers");
                            for (int i = 0; i < chatEvents.size(); i++) {
                                final String actionName = chatEvents.get(i);
                                final Optional<Action> action = plugin.getLoadedActions().stream()
                                        .filter(a -> a.getName().equals(actionName)).findAny();
                                if (action.isPresent()) {
                                    if (i < chatEventTriggers.size()) {
                                        oStage.addChatAction(new AbstractMap.SimpleEntry<>(chatEventTriggers.get(i),
                                                action.get()));
                                    } else {
                                        throw new StageFormatException("'chat-event-triggers' list is too small",
                                                quest, stageNum);
                                    }
                                } else {
                                    throw new StageFormatException("'chat-events' failed to load " + chatEvents.get(i),
                                            quest, stageNum);
                                }
                            }
                        } else {
                            throw new StageFormatException("'chat-event-triggers' is not in list format", quest,
                                    stageNum);
                        }
                    } else {
                        throw new StageFormatException("'chat-event-triggers' is missing", quest, stageNum);
                    }
                } else {
                    throw new StageFormatException("'chat-events' is not in list format", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".command-events")) {
                if (config.isList("quests." + questKey + ".stages.ordered." + stageNum + ".command-events")) {
                    if (config.contains("quests." + questKey + ".stages.ordered." + stageNum
                            + ".command-event-triggers")) {
                        if (config.isList("quests." + questKey + ".stages.ordered." + stageNum
                                + ".command-event-triggers")) {
                            final List<String> commandEvents = config.getStringList("quests." + questKey
                                    + ".stages.ordered." + stageNum + ".command-events");
                            final List<String> commandEventTriggers = config.getStringList("quests." + questKey
                                    + ".stages.ordered." + stageNum + ".command-event-triggers");
                            for (int i = 0; i < commandEvents.size(); i++) {
                                final String actionName = commandEvents.get(i);
                                final Optional<Action> action = plugin.getLoadedActions().stream()
                                        .filter(a -> a.getName().equals(actionName)).findAny();
                                if (action.isPresent()) {
                                    if (i < commandEventTriggers.size()) {
                                        oStage.addCommandAction(new AbstractMap.SimpleEntry<>(commandEventTriggers
                                                .get(i), action.get()));
                                    } else {
                                        throw new StageFormatException("'command-event-triggers' list is too small",
                                                quest, stageNum);
                                    }
                                } else {
                                    throw new StageFormatException("'command-events' failed to load "
                                            + commandEvents.get(i), quest, stageNum);
                                }
                            }
                        } else {
                            throw new StageFormatException("'command-event-triggers' is not in list format", quest,
                                    stageNum);
                        }
                    } else {
                        throw new StageFormatException("'command-event-triggers' is missing", quest, stageNum);
                    }
                } else {
                    throw new StageFormatException("'command-events' is not in list format", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".condition")) {
                final String conditionName = config.getString("quests." + questKey + ".stages.ordered."
                        + stageNum + ".condition");
                final Optional<Condition> condition = plugin.getLoadedConditions().stream()
                        .filter(c -> c.getName().equals(conditionName)).findAny();
                if (condition.isPresent()) {
                    oStage.setCondition(condition.get());
                } else {
                    throw new StageFormatException("'condition' failed to load", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".delay")) {
                final int delay = config.getInt("quests." + questKey + ".stages.ordered." + stageNum + ".delay", -999);
                if (delay > 0) {
                    oStage.setDelay(delay * 1000L);
                } else if (delay != -999) {
                    throw new StageFormatException("'delay' is not a positive number", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".delay-message")) {
                oStage.setDelayMessage(config.getString("quests." + questKey + ".stages.ordered." + stageNum
                        + ".delay-message"));
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".start-message")) {
                oStage.setStartMessage(config.getString("quests." + questKey + ".stages.ordered." + stageNum
                        + ".start-message"));
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".complete-message")) {
                oStage.setCompleteMessage(config.getString("quests." + questKey + ".stages.ordered." + stageNum
                        + ".complete-message"));
            }
            quest.getStages().add(oStage);
        }
    }
}
