package me.pikamug.quests.storage.implementation.jar;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.exceptions.QuestFormatException;
import me.pikamug.quests.exceptions.StageFormatException;
import me.pikamug.quests.module.BukkitCustomObjective;
import me.pikamug.quests.module.BukkitCustomRequirement;
import me.pikamug.quests.module.BukkitCustomReward;
import me.pikamug.quests.storage.implementation.ModuleStorageImpl;
import me.pikamug.quests.module.CustomObjective;
import me.pikamug.quests.module.CustomRequirement;
import me.pikamug.quests.module.CustomReward;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.BukkitStage;
import me.pikamug.quests.quests.components.Requirements;
import me.pikamug.quests.quests.components.Rewards;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class BukkitModuleJarStorage implements ModuleStorageImpl {

    private final BukkitQuestsPlugin plugin;

    public BukkitModuleJarStorage(final BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public BukkitQuestsPlugin getPlugin() {
        return plugin;
    }

    @Override
    public String getImplementationName() {
        return "JAR";
    }

    @Override
    public void init() {
        final File f = new File(plugin.getDataFolder(), "modules");
        if (f.exists() && f.isDirectory()) {
            final File[] modules = f.listFiles();
            if (modules != null) {
                for (final File module : modules) {
                    if (!module.isDirectory() && module.getName().endsWith(".jar")) {
                        loadModule(module);
                    }
                }
            }
        } else if (!f.mkdir()) {
            plugin.getLogger().warning("Unable to create module directory");
        }
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
                if (questsSection != null) {
                    for (final String questKey : questsSection.getKeys(false)) {
                        try {
                            if (config.contains("quests." + questKey)) {
                                loadCustomSections(plugin.getQuestById(questKey), config, questKey);
                            } else {
                                throw new QuestFormatException("Unable to load custom sections", questKey);
                            }
                        } catch (final QuestFormatException | StageFormatException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        } else {
            plugin.getLogger().severe("Unable to load module data from quests.yml");
        }
    }

    @Override
    public void close() {
    }

    /**
     * Load the specified jar as a module
     *
     * @param jar A custom reward/requirement/objective jar
     */
    public void loadModule(final File jar) {
        try {
            @SuppressWarnings("resource")
            final JarFile jarFile = new JarFile(jar);
            final Enumeration<JarEntry> entry = jarFile.entries();
            final URL[] urls = { new URL("jar:file:" + jar.getPath() + "!/") };
            final ClassLoader cl = URLClassLoader.newInstance(urls, plugin.getClass().getClassLoader());
            int count = 0;
            while (entry.hasMoreElements()) {
                final JarEntry je = entry.nextElement();
                if (je.isDirectory() || !je.getName().endsWith(".class")) {
                    continue;
                }
                final String className = je.getName().substring(0, je.getName().length() - 6).replace('/', '.');
                Class<?> c = null;
                try {
                    c = Class.forName(className, true, cl);
                } catch (final NoClassDefFoundError e) {
                    plugin.getLogger().severe("Module error! Seek help from developer of module:");
                    e.printStackTrace();
                }
                if (c != null) {
                    if (BukkitCustomRequirement.class.isAssignableFrom(c)) {
                        final Class<? extends BukkitCustomRequirement> requirementClass = c.asSubclass(BukkitCustomRequirement.class);
                        final Constructor<? extends BukkitCustomRequirement> constructor = requirementClass.getConstructor();
                        final BukkitCustomRequirement requirement = constructor.newInstance();
                        final Optional<CustomRequirement> oo = plugin.getCustomRequirement(requirement.getClass().getName());
                        final List<CustomRequirement> customRequirementList = plugin.getCustomRequirements();
                        oo.ifPresent(customRequirementList::remove);
                        customRequirementList.add(requirement);
                        plugin.setCustomRequirements(customRequirementList);
                        final String name = requirement.getName() == null ? "[" + jar.getName() + "]" : requirement.getName();
                        final String author = requirement.getAuthor() == null ? "[Unknown]" : requirement.getAuthor();
                        count++;
                        plugin.getLogger().info("Loaded \"" + name + "\" by " + author);
                    } else if (BukkitCustomReward.class.isAssignableFrom(c)) {
                        final Class<? extends BukkitCustomReward> rewardClass = c.asSubclass(BukkitCustomReward.class);
                        final Constructor<? extends BukkitCustomReward> constructor = rewardClass.getConstructor();
                        final BukkitCustomReward reward = constructor.newInstance();
                        final Optional<CustomReward> oo = plugin.getCustomReward(reward.getClass().getName());
                        final List<CustomReward> customRewardList = plugin.getCustomRewards();
                        oo.ifPresent(customRewardList::remove);
                        customRewardList.add(reward);
                        plugin.setCustomRewards(customRewardList);
                        final String name = reward.getName() == null ? "[" + jar.getName() + "]" : reward.getName();
                        final String author = reward.getAuthor() == null ? "[Unknown]" : reward.getAuthor();
                        count++;
                        plugin.getLogger().info("Loaded \"" + name + "\" by " + author);
                    } else if (CustomObjective.class.isAssignableFrom(c)) {
                        final Class<? extends BukkitCustomObjective> objectiveClass = c.asSubclass(BukkitCustomObjective.class);
                        final Constructor<? extends BukkitCustomObjective> constructor = objectiveClass.getConstructor();
                        final BukkitCustomObjective objective = constructor.newInstance();
                        final Optional<CustomObjective> oo = plugin.getCustomObjective(objective.getClass().getName());
                        final List<CustomObjective> customObjectiveList = plugin.getCustomObjectives();
                        if (oo.isPresent() && oo.get() instanceof BukkitCustomObjective) {
                            HandlerList.unregisterAll((BukkitCustomObjective)oo.get());
                            customObjectiveList.remove(oo.get());
                        }
                        customObjectiveList.add(objective);
                        plugin.setCustomObjectives(customObjectiveList);
                        final String name = objective.getName() == null ? "[" + jar.getName() + "]" : objective.getName();
                        final String author = objective.getAuthor() == null ? "[Unknown]" : objective.getAuthor();
                        count++;
                        plugin.getLogger().info("Loaded \"" + name + "\" by " + author);
                        try {
                            plugin.getServer().getPluginManager().registerEvents(objective, plugin);
                            plugin.getLogger().info("Registered events for custom objective \"" + name + "\"");
                        } catch (final Exception ex) {
                            plugin.getLogger().warning("Failed to register events for custom objective \"" + name
                                    + "\". Does the objective class listen for events?");
                            ex.printStackTrace();
                        }
                    }
                }
            }
            if (count == 0) {
                plugin.getLogger().severe("Unable to load module from file " + jar.getName() + " (not a valid module)!");
            }
        } catch (final Exception e) {
            plugin.getLogger().severe("Unable to load module from file " + jar.getName() + " (contact module developer)!");
            e.printStackTrace();
        }
    }

    private void loadCustomSections(final Quest quest, final FileConfiguration config, final String questKey)
            throws StageFormatException, QuestFormatException {
        final ConfigurationSection questStages = config.getConfigurationSection("quests." + questKey
                + ".stages.ordered");
        if (questStages != null) {
            for (final String stageNum : questStages.getKeys(false)) {
                if (quest == null) {
                    plugin.getLogger().warning("Unable to consider custom objectives because quest for " + questKey
                            + " was null");
                    return;
                }
                if (quest.getStage(Integer.parseInt(stageNum) - 1) == null) {
                    plugin.getLogger().severe("Unable to load custom objectives because stage" + (Integer.parseInt(stageNum)
                            - 1) + " for " + quest.getName() + " was null");
                    return;
                }
                final BukkitStage oStage = (BukkitStage) quest.getStage(Integer.parseInt(stageNum) - 1);
                oStage.clearCustomObjectives();
                oStage.clearCustomObjectiveCounts();
                oStage.clearCustomObjectiveData();
                oStage.clearCustomObjectiveDisplays();
                if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".custom-objectives")) {
                    final ConfigurationSection sec = config.getConfigurationSection("quests." + questKey
                            + ".stages.ordered." + stageNum + ".custom-objectives");
                    if (sec != null) {
                        for (final String path : sec.getKeys(false)) {
                            final String name = sec.getString(path + ".name");
                            final int count = sec.getInt(path + ".count");
                            Optional<CustomObjective> found = Optional.empty();
                            for (final CustomObjective cr : plugin.getCustomObjectives()) {
                                if (cr.getName().equalsIgnoreCase(name)) {
                                    found = Optional.of(cr);
                                    break;
                                }
                            }
                            if (found.isPresent()) {
                                oStage.addCustomObjectives(found.get());
                                oStage.addCustomObjectiveCounts(Math.max(count, 0));
                                final ConfigurationSection sec2 = sec.getConfigurationSection(path + ".data");
                                for (final Map.Entry<String,Object> prompt : found.get().getData()) {
                                    final Map.Entry<String, Object> data = populateCustoms(sec2, prompt);
                                    oStage.addCustomObjectiveData(data);
                                }
                            } else {
                                throw new QuestFormatException(name + " custom objective not found for Stage "
                                        + stageNum, questKey);
                            }
                        }
                    }
                }
            }
            final Rewards rews = quest.getRewards();
            if (config.contains("quests." + questKey + ".rewards.custom-rewards")) {
                final ConfigurationSection sec = config.getConfigurationSection("quests." + questKey
                        + ".rewards.custom-rewards");
                final Map<String, Map<String, Object>> temp = new HashMap<>();
                if (sec != null) {
                    for (final String path : sec.getKeys(false)) {
                        final String name = sec.getString(path + ".name");
                        Optional<CustomReward> found = Optional.empty();
                        for (final CustomReward cr : plugin.getCustomRewards()) {
                            if (cr.getName().equalsIgnoreCase(name)) {
                                found = Optional.of(cr);
                                break;
                            }
                        }
                        if (found.isPresent()) {
                            final ConfigurationSection sec2 = sec.getConfigurationSection(path + ".data");
                            final Map<String, Object> data = populateCustoms(sec2, found.get().getData());
                            temp.put(name, data);
                        } else {
                            throw new QuestFormatException(name + " custom reward not found", questKey);
                        }
                    }
                }
                rews.setCustomRewards(temp);
            }
            final Requirements reqs = quest.getRequirements();
            if (config.contains("quests." + questKey + ".requirements.custom-requirements")) {
                final ConfigurationSection sec = config.getConfigurationSection("quests." + questKey
                        + ".requirements.custom-requirements");
                final Map<String, Map<String, Object>> temp = new HashMap<>();
                if (sec != null) {
                    for (final String path : sec.getKeys(false)) {
                        final String name = sec.getString(path + ".name");
                        Optional<CustomRequirement> found = Optional.empty();
                        for (final CustomRequirement cr : plugin.getCustomRequirements()) {
                            if (cr.getName().equalsIgnoreCase(name)) {
                                found = Optional.of(cr);
                                break;
                            }
                        }
                        if (found.isPresent()) {
                            final ConfigurationSection sec2 = sec.getConfigurationSection(path + ".data");
                            final Map<String, Object> data = populateCustoms(sec2, found.get().getData());
                            temp.put(name, data);
                        } else {
                            throw new QuestFormatException(name + " custom requirement not found", questKey);
                        }
                    }
                }
                reqs.setCustomRequirements(temp);
            }
        }
    }

    /**
     * Permits use of fallbacks for customs maps<p>
     *
     * Avoid null objects in datamap by initializing the entry value with empty string if no fallback present.
     *
     * @param section The section of configuration to check
     * @param dataMap The map to process
     * @return Populated map
     */
    private static Map<String, Object> populateCustoms(final ConfigurationSection section,
                                                       final Map<String, Object> dataMap) {
        final Map<String,Object> data = new HashMap<>();
        if (section != null) {
            for (final String key : dataMap.keySet()) {
                data.put(key, section.contains(key) ? section.get(key) : dataMap.get(key) != null
                        ? dataMap.get(key) : "");
            }
        }
        return data;
    }

    /**
     * Permits use of fallbacks for customs entries<p>
     *
     * Avoid null objects in datamap by initializing the entry value with empty string if no fallback present.
     *
     * @param section The section of configuration to check
     * @param dataMap The entry to process
     * @return Populated entry, or null
     */
    private static Map.Entry<String, Object> populateCustoms(final ConfigurationSection section,
                                                             final Map.Entry<String, Object> dataMap) {
        Map.Entry<String, Object> data = null;
        if (section != null) {
            final String key = dataMap.getKey();
            final Object value = dataMap.getValue();
            data = new AbstractMap.SimpleEntry<>(key, section.contains(key) ? section.get(key) : value != null
                    ? value : "");
        }
        return data;
    }
}
