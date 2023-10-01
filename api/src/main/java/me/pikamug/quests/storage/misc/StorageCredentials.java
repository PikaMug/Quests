/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.storage.misc;

import java.util.Map;
import java.util.Objects;

public class StorageCredentials {

    private final String address;
    private final String database;
    private final String username;
    private final String password;
    private final int maxPoolSize;
    private final int minIdleConnections;
    private final int maxLifetime;
    private final int keepAliveTime;
    private final int connectionTimeout;
    private final Map<String, String> properties;

    public StorageCredentials(final String address, final String database, final String username, final String password,
            final int maxPoolSize, final int minIdleConnections, final int maxLifetime, final int keepAliveTime,
            final int connectionTimeout, final Map<String, String> properties) {
        this.address = address;
        this.database = database;
        this.username = username;
        this.password = password;
        this.maxPoolSize = maxPoolSize;
        this.minIdleConnections = minIdleConnections;
        this.maxLifetime = maxLifetime;
        this.keepAliveTime = keepAliveTime;
        this.connectionTimeout = connectionTimeout;
        this.properties = properties;
    }

    public String getAddress() {
        return Objects.requireNonNull(address, "address");
    }

    public String getDatabase() {
        return Objects.requireNonNull(database, "database");
    }

    public String getUsername() {
        return Objects.requireNonNull(username, "username");
    }

    public String getPassword() {
        return Objects.requireNonNull(password, "password");
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public int getMinIdleConnections() {
        return minIdleConnections;
    }

    public int getMaxLifetime() {
        return maxLifetime;
    }

    public int getKeepAliveTime() {
        return keepAliveTime;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public Map<String, String> getProperties() {
        return properties;
    }
}
