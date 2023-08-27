/*
 * Copyright (c) 2014 PikaMug and contributors. All rights reserved.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package me.pikamug.quests.storage;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.enums.StorageType;
import me.pikamug.quests.storage.implementation.QuesterStorageImpl;
import me.pikamug.quests.storage.implementation.custom.CustomStorageProviders;
import me.pikamug.quests.storage.implementation.file.BukkitQuesterYamlStorage;
import me.pikamug.quests.storage.implementation.sql.BukkitQuesterSqlStorage;
import me.pikamug.quests.storage.implementation.sql.connection.hikari.MySqlConnectionFactory;
import me.pikamug.quests.storage.misc.StorageCredentials;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class BukkitStorageFactory {
    private final BukkitQuestsPlugin plugin;

    public BukkitStorageFactory(final BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    public QuesterStorage getInstance() {
        final QuesterStorage storage;
        final StorageType type = StorageType.parse(plugin.getConfig().getString("storage-method.player-data", "yaml"),
                StorageType.YAML);
        plugin.getLogger().info("Loading storage implementation: " + type.name());
        storage = new QuesterStorage(plugin, createNewImplementation(type));

        storage.init();
        return storage;
    }

    public QuesterStorageImpl createNewImplementation(final StorageType method) {
        switch (method) {
            case CUSTOM:
                return CustomStorageProviders.getProvider().provide(plugin);
            case MYSQL:
                return new BukkitQuesterSqlStorage(
                        plugin,
                        new MySqlConnectionFactory(getDatabaseValues(plugin.getConfig())),
                        plugin.getConfig().getString("storage-data.table_prefix")
                );
            case YAML:
                return new BukkitQuesterYamlStorage(plugin, plugin.getDataFolder() + File.separator + "data");
            default:
                throw new RuntimeException("Unknown method: " + method);
        }
    }
    
    private StorageCredentials getDatabaseValues(final FileConfiguration fc) {
        final int maxPoolSize = fc.getInt("storage-data.pool-settings.max-pool-size", 
                fc.getInt("storage-data.pool-size", 10));
        final int minIdle = fc.getInt("storage-data.pool-settings.min-idle", maxPoolSize);
        final int maxLifetime = fc.getInt("storage-data.pool-settings.max-lifetime", 1800000);
        final int keepAliveTime = fc.getInt("storage-data.pool-settings.keepalive-time", 0);
        final int connectionTimeout = fc.getInt("storage-data.pool-settings.connection-timeout", 5000);
        final boolean useUnicode = fc.getBoolean("storage-data.pool-settings.properties.useUnicode", true);
        final String characterEncoding = fc.getString("storage-data.pool-settings.properties.characterEncoding", "utf8");
        Map<String, String> props = new HashMap<>();
        props.put("useUnicode", String.valueOf(useUnicode));
        props.put("characterEncoding", characterEncoding);

        return new StorageCredentials(
                fc.getString("storage-data.address", null),
                fc.getString("storage-data.database", null),
                fc.getString("storage-data.username", null),
                fc.getString("storage-data.password", null),
                maxPoolSize, minIdle, maxLifetime, keepAliveTime, connectionTimeout, props
        );
    }
}
