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

package me.blackvein.quests.storage.misc;

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
    private final int connectionTimeout;
    private final Map<String, String> properties;

    public StorageCredentials(final String address, final String database, final String username, final String password,
            final int maxPoolSize, final int minIdleConnections, final int maxLifetime, final int connectionTimeout,
            final Map<String, String> properties) {
        this.address = address;
        this.database = database;
        this.username = username;
        this.password = password;
        this.maxPoolSize = maxPoolSize;
        this.minIdleConnections = minIdleConnections;
        this.maxLifetime = maxLifetime;
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

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public Map<String, String> getProperties() {
        return properties;
    }
}
