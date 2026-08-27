package me.pikamug.quests.storage.implementation.jar;

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.storage.implementation.ModuleStorageImpl;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class FabricModuleJarStorage implements ModuleStorageImpl {

    private final FabricQuestsPlugin plugin;
    private final List<URLClassLoader> moduleLoaders = new ArrayList<>();

    public FabricModuleJarStorage(FabricQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override public FabricQuestsPlugin getPlugin() { return plugin; }
    @Override public String getImplementationName() { return "Jar"; }

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

    @Override
    public void loadModule(File jar) {
        if (jar == null || !jar.exists()) return;
        try {
            final URLClassLoader loader = new URLClassLoader(
                    new URL[]{jar.toURI().toURL()},
                    getClass().getClassLoader()
            );
            moduleLoaders.add(loader);
            plugin.getPluginLogger().info("Loaded module: {}", jar.getName());
        } catch (final Exception e) {
            plugin.getPluginLogger().error("Failed to load module: {}", jar.getName(), e);
        }
    }
}
