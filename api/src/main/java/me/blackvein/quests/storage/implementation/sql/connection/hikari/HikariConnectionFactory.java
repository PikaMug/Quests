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

package me.blackvein.quests.storage.implementation.sql.connection.hikari;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.plugin.java.JavaPlugin;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import me.blackvein.quests.Quests;
import me.blackvein.quests.storage.implementation.sql.connection.ConnectionFactory;
import me.blackvein.quests.storage.misc.StorageCredentials;

public abstract class HikariConnectionFactory implements ConnectionFactory {
    private final StorageCredentials configuration;
    private HikariDataSource hikari;

    public HikariConnectionFactory(final StorageCredentials configuration) {
        this.configuration = configuration;
    }

    /**
     * Gets the default port used by the database
     *
     * @return the default port
     */
    protected abstract String defaultPort();

    /**
     * Configures the {@link HikariConfig} with the relevant database properties.
     *
     * <p>Each driver does this slightly differently.</p>
     *
     * @param config the hikari config
     * @param address the database address
     * @param port the database port
     * @param databaseName the database name
     * @param username the database username
     * @param password the database password
     */
    protected abstract void configureDatabase(HikariConfig config, String address, String port, String databaseName, 
            String username, String password);

    /**
     * Allows the connection factory instance to override certain properties before they are set.
     *
     * @param properties the current properties
     */
    protected void overrideProperties(final Map<String, String> properties) {
        // https://github.com/brettwooldridge/HikariCP/wiki/Rapid-Recovery
        properties.putIfAbsent("socketTimeout", String.valueOf(TimeUnit.SECONDS.toMillis(30)));
    }

    /**
     * Sets the given connection properties onto the config.
     *
     * @param config the hikari config
     * @param properties the properties
     */
    protected void setProperties(final HikariConfig config, final Map<String, String> properties) {
        for (final Map.Entry<String, String> property : properties.entrySet()) {
            config.addDataSourceProperty(property.getKey(), property.getValue());
        }
    }

    @Override
    public void init(final Quests plugin) {
        final HikariConfig config = new HikariConfig();
        config.setPoolName("quests-hikari");

        final String[] addressSplit = configuration.getAddress().split(":");
        final String address = addressSplit[0];
        final String port = addressSplit.length > 1 ? addressSplit[1] : defaultPort();

        configureDatabase(config, address, port, configuration.getDatabase(), configuration.getUsername(), 
                configuration.getPassword());

        final Map<String, String> properties = new HashMap<>(this.configuration.getProperties());

        overrideProperties(properties);

        setProperties(config, properties);

        config.setMaximumPoolSize(this.configuration.getMaxPoolSize());
        config.setMinimumIdle(this.configuration.getMinIdleConnections());
        config.setMaxLifetime(this.configuration.getMaxLifetime());
        //config.setKeepaliveTime(this.configuration.getKeepAliveTime());
        config.setConnectionTimeout(this.configuration.getConnectionTimeout());

        hikari = new HikariDataSource(config);
    }

    @Override
    public void close() {
        if (hikari != null) {
            hikari.close();
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (hikari == null) {
            throw new SQLException("Unable to get a connection from the pool because hikari is null");
        }

        final Connection connection = hikari.getConnection();
        if (connection == null) {
            throw new SQLException("Unable to get a connection from the pool because getConnection returned null");
        }

        return connection;
    }
    
    public static String identifyClassLoader(final ClassLoader classLoader) throws ReflectiveOperationException {
        final Class<?> pluginClassLoaderClass = Class.forName("org.bukkit.plugin.java.PluginClassLoader");
        if (pluginClassLoaderClass.isInstance(classLoader)) {
            final Method getPluginMethod = pluginClassLoaderClass.getDeclaredMethod("getPlugin");
            getPluginMethod.setAccessible(true);

            final JavaPlugin plugin = (JavaPlugin) getPluginMethod.invoke(classLoader);
            return plugin.getName();
        }
        return null;
    }
}
