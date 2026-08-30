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

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.enums.StorageType;
import me.pikamug.quests.storage.implementation.QuesterStorageImpl;
import me.pikamug.quests.storage.implementation.file.FabricQuesterJsonStorage;
import me.pikamug.quests.storage.implementation.sql.FabricQuesterSqlStorage;
import me.pikamug.quests.storage.implementation.sql.connection.hikari.MySqlConnectionFactory;
import me.pikamug.quests.storage.misc.StorageCredentials;

import java.util.HashMap;
import java.util.Map;

public class FabricStorageFactory {

    private final FabricQuestsPlugin plugin;

    public FabricStorageFactory(FabricQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    public QuesterStorage getInstance() {
        final StorageType type = StorageType.parse(getStorageMethod(), StorageType.YAML);
        plugin.getPluginLogger().info("Loading storage implementation: " + type.name());
        final QuesterStorage storage = new QuesterStorage(plugin, prepareImplementation(type));
        storage.init();
        return storage;
    }

    private String getStorageMethod() {
        return plugin.getConfigSettings().getStorageMethod();
    }

    private QuesterStorageImpl prepareImplementation(final StorageType method) {
        switch (method) {
            case MYSQL:
                return new FabricQuesterSqlStorage(
                        plugin,
                        new MySqlConnectionFactory(getDatabaseValues()),
                        plugin.getConfigSettings().getStorageTablePrefix()
                );
            case YAML:
            default:
                return new FabricQuesterJsonStorage(plugin);
        }
    }

    private StorageCredentials getDatabaseValues() {
        final int maxPoolSize = plugin.getConfigSettings().getStorageMaxPoolSize();
        final int minIdle = plugin.getConfigSettings().getStorageMinIdle();
        final int maxLifetime = plugin.getConfigSettings().getStorageMaxLifetime();
        final int keepAliveTime = plugin.getConfigSettings().getStorageKeepAliveTime();
        final int connectionTimeout = plugin.getConfigSettings().getStorageConnectionTimeout();
        final Map<String, String> properties = new HashMap<>();
        properties.put("useUnicode", "true");
        properties.put("characterEncoding", "utf8");

        return new StorageCredentials(
                plugin.getConfigSettings().getStorageAddress(),
                plugin.getConfigSettings().getStorageDatabase(),
                plugin.getConfigSettings().getStorageUsername(),
                plugin.getConfigSettings().getStoragePassword(),
                maxPoolSize, minIdle, maxLifetime, keepAliveTime, connectionTimeout, properties
        );
    }
}