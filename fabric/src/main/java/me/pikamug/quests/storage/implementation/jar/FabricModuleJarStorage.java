/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.storage.implementation.jar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.exceptions.QuestFormatException;
import me.pikamug.quests.exceptions.StageFormatException;
import me.pikamug.quests.module.CustomObjective;
import me.pikamug.quests.module.CustomRequirement;
import me.pikamug.quests.module.CustomReward;
import me.pikamug.quests.module.FabricCustomObjective;
import me.pikamug.quests.module.FabricCustomRequirement;
import me.pikamug.quests.module.FabricCustomReward;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.FabricStage;
import me.pikamug.quests.quests.components.Requirements;
import me.pikamug.quests.quests.components.Rewards;
import me.pikamug.quests.storage.implementation.ModuleStorageImpl;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class FabricModuleJarStorage implements ModuleStorageImpl {

    private final FabricQuestsPlugin plugin;
    private final List<URLClassLoader> moduleLoaders = new ArrayList<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public FabricModuleJarStorage(FabricQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override public FabricQuestsPlugin getPlugin() { return plugin; }
    @Override public String getImplementationName() { return "JAR"; }

    @Override
    public void init() {
        final File modulesDir = new File(plugin.getPluginDataFolder(), "modules");
        if (!modulesDir.exists()) {
            modulesDir.mkdirs();
            return;
        }
        final File[] jars = modulesDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jars != null) {
            for (final File jar : jars) {
                loadModule(jar);
            }
        }
        // Apply custom sections from quest files now that modules are registered
        final Path storageDir = plugin.getPluginDataFolder().toPath().resolve("storage");
        for (final Quest quest : plugin.getLoadedQuests()) {
            final Path file = storageDir.resolve(quest.getId() + ".json");
            if (!Files.exists(file)) continue;
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                final JsonObject json = gson.fromJson(reader, JsonObject.class);
                if (json != null) {
                    loadCustomSections(quest, json, quest.getId());
                }
            } catch (final QuestFormatException | StageFormatException ex) {
                plugin.getPluginLogger().error("Unable to load custom sections", ex);
            } catch (final Exception ex) {
                plugin.getPluginLogger().error("Unable to load module data from {}", file, ex);
            }
        }
    }

    @Override
    public void close() {
        for (final URLClassLoader loader : moduleLoaders) {
            try {
                loader.close();
            } catch (final Exception e) {
                plugin.getPluginLogger().error("Failed to close module classloader", e);
            }
        }
        moduleLoaders.clear();
    }

    /**
     * Load the specified jar as a module
     *
     * @param jar A custom reward/requirement/objective jar
     */
    public void loadModule(final File jar) {
        if (jar == null || !jar.exists() || jar.isDirectory()) return;
        try (final JarFile jarFile = new JarFile(jar)) {
            final Enumeration<JarEntry> entry = jarFile.entries();
            final URL[] urls = { new URL("jar:file:" + jar.getPath() + "!/") };
            @SuppressWarnings("resource")
            final ClassLoader cl = URLClassLoader.newInstance(urls, getClass().getClassLoader());
            moduleLoaders.add((URLClassLoader) cl);
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
                    plugin.getPluginLogger().error("Module error! Seek help from developer of jar " + jar.getName());
                }
                if (c != null) {
                    if (FabricCustomRequirement.class.isAssignableFrom(c)) {
                        final Class<? extends FabricCustomRequirement> requirementClass
                                = c.asSubclass(FabricCustomRequirement.class);
                        try {
                            final Constructor<? extends FabricCustomRequirement> constructor
                                    = requirementClass.getConstructor();
                            final FabricCustomRequirement requirement = constructor.newInstance();
                            plugin.getCustomRequirements().removeIf(r -> r.getClass().getName()
                                    .equals(requirement.getClass().getName()));
                            plugin.getCustomRequirements().add(requirement);
                            final String name = requirement.getName() == null ? "[" + jar.getName() + "]"
                                    : requirement.getName();
                            final String author = requirement.getAuthor() == null ? "[Unknown]"
                                    : requirement.getAuthor();
                            count++;
                            plugin.getPluginLogger().info("Loaded \"{}\" by {}", name, author);
                        } catch (final Exception e) {
                            plugin.getPluginLogger().error("Unable to instantiate requirement " + className, e);
                        }
                    } else if (FabricCustomReward.class.isAssignableFrom(c)) {
                        final Class<? extends FabricCustomReward> rewardClass
                                = c.asSubclass(FabricCustomReward.class);
                        try {
                            final Constructor<? extends FabricCustomReward> constructor = rewardClass.getConstructor();
                            final FabricCustomReward reward = constructor.newInstance();
                            plugin.getCustomRewards().removeIf(r -> r.getClass().getName()
                                    .equals(reward.getClass().getName()));
                            plugin.getCustomRewards().add(reward);
                            final String name = reward.getName() == null ? "[" + jar.getName() + "]"
                                    : reward.getName();
                            final String author = reward.getAuthor() == null ? "[Unknown]" : reward.getAuthor();
                            count++;
                            plugin.getPluginLogger().info("Loaded \"{}\" by {}", name, author);
                        } catch (final Exception e) {
                            plugin.getPluginLogger().error("Unable to instantiate reward " + className, e);
                        }
                    } else if (FabricCustomObjective.class.isAssignableFrom(c)) {
                        final Class<? extends FabricCustomObjective> objectiveClass
                                = c.asSubclass(FabricCustomObjective.class);
                        try {
                            final Constructor<? extends FabricCustomObjective> constructor
                                    = objectiveClass.getConstructor();
                            final FabricCustomObjective objective = constructor.newInstance();
                            plugin.getCustomObjectives().removeIf(o -> o.getClass().getName()
                                    .equals(objective.getClass().getName()));
                            plugin.getCustomObjectives().add(objective);
                            final String name = objective.getName() == null ? "[" + jar.getName() + "]"
                                    : objective.getName();
                            final String author = objective.getAuthor() == null ? "[Unknown]"
                                    : objective.getAuthor();
                            count++;
                            plugin.getPluginLogger().info("Loaded \"{}\" by {}", name, author);
                        } catch (final Exception e) {
                            plugin.getPluginLogger().error("Unable to instantiate objective " + className, e);
                        }
                    }
                }
            }
            if (count == 0) {
                plugin.getPluginLogger().error("Unable to load module from file {} (not a valid module)!",
                        jar.getName());
            }
        } catch (final IOException e) {
            plugin.getPluginLogger().error("Unable to load module from file {} (contact module developer)!",
                    jar.getName());
        }
    }

    private void loadCustomSections(final Quest quest, final JsonObject json, final String questKey)
            throws StageFormatException, QuestFormatException {
        if (json.has("stages")) {
            final JsonArray stagesArray = json.getAsJsonArray("stages");
            for (int stageNum = 0; stageNum < stagesArray.size(); stageNum++) {
                if (quest == null) {
                    plugin.getPluginLogger().warn("Unable to consider custom objectives because quest for {} was null",
                            questKey);
                    return;
                }
                if (quest.getStage(stageNum) == null) {
                    plugin.getPluginLogger().error("Unable to load custom objectives because stage {} for {} was null",
                            stageNum, quest.getName());
                    return;
                }
                final FabricStage oStage = (FabricStage) quest.getStage(stageNum);
                oStage.clearCustomObjectives();
                oStage.clearCustomObjectiveCounts();
                oStage.clearCustomObjectiveData();
                oStage.clearCustomObjectiveDisplays();
                final JsonObject stageJson = stagesArray.get(stageNum).getAsJsonObject();
                if (stageJson.has("custom-objectives")) {
                    final JsonObject sec = stageJson.getAsJsonObject("custom-objectives");
                    for (final String path : sec.keySet()) {
                        final JsonObject customJson = sec.getAsJsonObject(path);
                        final String name = customJson.has("name") ? customJson.get("name").getAsString() : path;
                        final int count = customJson.has("count") ? customJson.get("count").getAsInt() : 1;
                        CustomObjective found = null;
                        for (final CustomObjective cr : plugin.getCustomObjectives()) {
                            if (cr.getName().equalsIgnoreCase(name)) {
                                found = cr;
                                break;
                            }
                        }
                        if (found != null) {
                            oStage.addCustomObjectives(found);
                            oStage.addCustomObjectiveCounts(Math.max(count, 0));
                            final JsonObject sec2 = customJson.has("data")
                                    ? customJson.getAsJsonObject("data") : null;
                            for (final Map.Entry<String, Object> prompt : found.getData()) {
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
        if (json.has("rewards")) {
            final Rewards rews = quest.getRewards();
            if (json.getAsJsonObject("rewards").has("custom-rewards")) {
                final JsonObject sec = json.getAsJsonObject("rewards").getAsJsonObject("custom-rewards");
                final Map<String, Map<String, Object>> temp = new HashMap<>();
                for (final String path : sec.keySet()) {
                    final JsonObject customJson = sec.getAsJsonObject(path);
                    final String name = customJson.has("name") ? customJson.get("name").getAsString() : path;
                    CustomReward found = null;
                    for (final CustomReward cr : plugin.getCustomRewards()) {
                        if (cr.getName().equalsIgnoreCase(name)) {
                            found = cr;
                            break;
                        }
                    }
                    if (found != null) {
                        final JsonObject sec2 = customJson.has("data") ? customJson.getAsJsonObject("data") : null;
                        final Map<String, Object> data = populateCustoms(sec2, found.getData());
                        temp.put(name, data);
                    } else {
                        throw new QuestFormatException(name + " custom reward not found", questKey);
                    }
                }
                rews.setCustomRewards(temp);
            }
        }
        if (json.has("requirements")) {
            final Requirements reqs = quest.getRequirements();
            if (json.getAsJsonObject("requirements").has("custom-requirements")) {
                final JsonObject sec = json.getAsJsonObject("requirements").getAsJsonObject("custom-requirements");
                final Map<String, Map<String, Object>> temp = new HashMap<>();
                for (final String path : sec.keySet()) {
                    final JsonObject customJson = sec.getAsJsonObject(path);
                    final String name = customJson.has("name") ? customJson.get("name").getAsString() : path;
                    CustomRequirement found = null;
                    for (final CustomRequirement cr : plugin.getCustomRequirements()) {
                        if (cr.getName().equalsIgnoreCase(name)) {
                            found = cr;
                            break;
                        }
                    }
                    if (found != null) {
                        final JsonObject sec2 = customJson.has("data") ? customJson.getAsJsonObject("data") : null;
                        final Map<String, Object> data = populateCustoms(sec2, found.getData());
                        temp.put(name, data);
                    } else {
                        throw new QuestFormatException(name + " custom requirement not found", questKey);
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
    private static Map<String, Object> populateCustoms(final JsonObject section,
                                                       final Map<String, Object> dataMap) {
        final Map<String, Object> data = new HashMap<>();
        if (section != null) {
            for (final String key : dataMap.keySet()) {
                data.put(key, section.has(key) ? fromJson(section.get(key)) : dataMap.get(key) != null
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
    private static Map.Entry<String, Object> populateCustoms(final JsonObject section,
                                                             final Map.Entry<String, Object> dataMap) {
        Map.Entry<String, Object> data = null;
        if (section != null) {
            final String key = dataMap.getKey();
            final Object value = dataMap.getValue();
            data = new AbstractMap.SimpleEntry<>(key, section.has(key) ? fromJson(section.get(key)) : value != null
                    ? value : "");
        }
        return data;
    }

    private static Object fromJson(final JsonElement el) {
        if (el == null || el.isJsonNull()) {
            return null;
        }
        if (el.isJsonPrimitive()) {
            final JsonPrimitive prim = el.getAsJsonPrimitive();
            if (prim.isBoolean()) return prim.getAsBoolean();
            if (prim.isNumber()) return prim.getAsNumber();
            return prim.getAsString();
        }
        if (el.isJsonArray()) {
            final List<Object> list = new ArrayList<>();
            el.getAsJsonArray().forEach(e -> list.add(fromJson(e)));
            return list;
        }
        return el.toString();
    }
}