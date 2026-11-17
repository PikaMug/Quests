/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.enums.StorageType;
import me.pikamug.quests.storage.implementation.QuesterStorageImpl;
import me.pikamug.quests.storage.implementation.file.FabricQuesterJsonStorage;
import me.pikamug.quests.storage.implementation.sql.FabricQuesterSqlStorage;
import me.pikamug.quests.storage.implementation.sql.connection.hikari.MySqlConnectionFactory;
import me.pikamug.quests.storage.misc.StorageCredentials;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class FabricStorageFactory {

    private final FabricQuestsPlugin plugin;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private JsonObject config;

    public FabricStorageFactory(FabricQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    public QuesterStorage getInstance() {
        config = loadConfig();
        ensureStorageDefaults();
        final StorageType type = StorageType.parse(getString("storage-method.player-data", "yaml"),
                StorageType.YAML);
        plugin.getPluginLogger().info("Loading storage implementation: " + type.name());
        final QuesterStorage storage = new QuesterStorage(plugin, createNewImplementation(type));
        storage.init();
        return storage;
    }

    private QuesterStorageImpl createNewImplementation(final StorageType method) {
        switch (method) {
            case MYSQL:
                return new FabricQuesterSqlStorage(
                        plugin,
                        new MySqlConnectionFactory(getDatabaseValues()),
                        getString("storage-data.table_prefix", "quests_")
                );
            case YAML:
            default:
                return new FabricQuesterJsonStorage(plugin);
        }
    }

    private StorageCredentials getDatabaseValues() {
        final int maxPoolSize = getInt("storage-data.pool-settings.max-pool-size",
                getInt("storage-data.pool-size", 10));
        final int minIdle = getInt("storage-data.pool-settings.min-idle", maxPoolSize);
        final int maxLifetime = getInt("storage-data.pool-settings.max-lifetime", 1800000);
        final int keepAliveTime = getInt("storage-data.pool-settings.keepalive-time", 0);
        final int connectionTimeout = getInt("storage-data.pool-settings.connection-timeout", 5000);
        final boolean useUnicode = getBoolean("storage-data.pool-settings.properties.useUnicode", true);
        final String characterEncoding = getString("storage-data.pool-settings.properties.characterEncoding", "utf8");
        final Map<String, String> properties = new HashMap<>();
        properties.put("useUnicode", String.valueOf(useUnicode));
        properties.put("characterEncoding", characterEncoding);

        return new StorageCredentials(
                getString("storage-data.address", null),
                getString("storage-data.database", null),
                getString("storage-data.username", null),
                getString("storage-data.password", null),
                maxPoolSize, minIdle, maxLifetime, keepAliveTime, connectionTimeout, properties
        );
    }

    private JsonObject loadConfig() {
        final File configFile = new File(plugin.getPluginDataFolder(), "config.json");
        try (Reader reader = Files.newBufferedReader(configFile.toPath())) {
            final JsonObject loaded = gson.fromJson(reader, JsonObject.class);
            return loaded != null ? loaded : new JsonObject();
        } catch (final IOException e) {
            plugin.getPluginLogger().error("Failed to load config.json", e);
            return new JsonObject();
        }
    }

    private void ensureStorageDefaults() {
        boolean changed = false;

        final JsonObject storageMethod = getOrCreateObject("storage-method");
        changed |= addDefault(storageMethod, "player-data", "yaml");

        final JsonObject storageData = getOrCreateObject("storage-data");
        changed |= addDefault(storageData, "address", "localhost:3306");
        changed |= addDefault(storageData, "database", "minecraft");
        changed |= addDefault(storageData, "username", "root");
        changed |= addDefault(storageData, "password", "");
        changed |= addDefault(storageData, "table_prefix", "quests_");

        final JsonObject poolSettings = getOrCreateObject("storage-data.pool-settings");
        changed |= addDefault(poolSettings, "max-pool-size", 10);
        changed |= addDefault(poolSettings, "min-idle", 10);
        changed |= addDefault(poolSettings, "max-lifetime", 1800000);
        changed |= addDefault(poolSettings, "keepalive-time", 0);
        changed |= addDefault(poolSettings, "connection-timeout", 5000);

        final JsonObject properties = getOrCreateObject("storage-data.pool-settings.properties");
        changed |= addDefault(properties, "useUnicode", true);
        changed |= addDefault(properties, "characterEncoding", "utf8");

        if (changed) {
            saveConfig();
        }
    }

    private JsonObject getOrCreateObject(final String path) {
        JsonObject current = config;
        final String[] keys = path.split("\\.");
        for (final String key : keys) {
            final JsonElement element = current.get(key);
            if (element != null && element.isJsonObject()) {
                current = element.getAsJsonObject();
            } else {
                final JsonObject created = new JsonObject();
                current.add(key, created);
                current = created;
            }
        }
        return current;
    }

    private boolean addDefault(final JsonObject object, final String key, final Object value) {
        if (object.has(key)) {
            return false;
        }
        object.add(key, gson.toJsonTree(value));
        return true;
    }

    private void saveConfig() {
        final File configFile = new File(plugin.getPluginDataFolder(), "config.json");
        try (Writer writer = Files.newBufferedWriter(configFile.toPath())) {
            gson.toJson(config, writer);
        } catch (final IOException e) {
            plugin.getPluginLogger().error("Failed to save config.json", e);
        }
    }

    private JsonElement get(final String path) {
        JsonElement current = config;
        for (final String key : path.split("\\.")) {
            if (current == null || !current.isJsonObject()) {
                return null;
            }
            current = current.getAsJsonObject().get(key);
        }
        return current;
    }

    private String getString(final String path, final String def) {
        final JsonElement element = get(path);
        return element != null && element.isJsonPrimitive() ? element.getAsString() : def;
    }

    private int getInt(final String path, final int def) {
        final JsonElement element = get(path);
        return element != null && element.isJsonPrimitive() ? element.getAsInt() : def;
    }

    private boolean getBoolean(final String path, final boolean def) {
        final JsonElement element = get(path);
        return element != null && element.isJsonPrimitive() ? element.getAsBoolean() : def;
    }
}
