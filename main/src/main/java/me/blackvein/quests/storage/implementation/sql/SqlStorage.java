/*******************************************************************************************************
 * Continued by PikaMug (formerly HappyPikachu) with permission from _Blackvein_. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

package me.blackvein.quests.storage.implementation.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import java.util.function.Function;

import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;
import me.blackvein.quests.storage.implementation.StorageImplementation;
import me.blackvein.quests.storage.implementation.sql.connection.ConnectionFactory;

public class SqlStorage implements StorageImplementation {
    private static final String PLAYER_SELECT = "SELECT id, hasjournal, FROM '{prefix}players' WHERE uuid=?";
    private static final String PLAYER_SELECT_USERNAME_BY_UUID = "SELECT username FROM '{prefix}players' WHERE uuid=? LIMIT 1";
    private static final String PLAYER_UPDATE_USERNAME_FOR_UUID = "UPDATE '{prefix}players' SET username=? WHERE uuid=?";
    private static final String PLAYER_INSERT = "INSERT INTO '{prefix}players' (uuid, username, hasjournal) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE";
    private static final String PLAYER_DELETE = "DELETE FROM '{prefix}players' WHERE uuid=?";

    private final Quests plugin;
    
    private final ConnectionFactory connectionFactory;
    private final Function<String, String> statementProcessor;

    public SqlStorage(final Quests plugin, final ConnectionFactory connectionFactory, final String tablePrefix) {
        this.plugin = plugin;
        this.connectionFactory = connectionFactory;
        this.statementProcessor = connectionFactory.getStatementProcessor().compose(s -> s.replace("{prefix}", tablePrefix));
    }

    @Override
    public Quests getPlugin() {
        return plugin;
    }

    @Override
    public String getImplementationName() {
        return connectionFactory.getImplementationName();
    }

    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public Function<String, String> getStatementProcessor() {
        return statementProcessor;
    }

    @Override
    public void init() throws Exception {
        connectionFactory.init(plugin);
    }
    
    @Override
    public void close() {
        try {
            connectionFactory.close();
        } catch (final Exception e) {
            this.plugin.getLogger().severe("Problem occurred while closing SQL storage");
            e.printStackTrace();
        }
    }
    
    @Override
    public Quester loadQuesterData(final UUID uniqueId) throws Exception {
        final Quester quester = new Quester(plugin, uniqueId);
        try (Connection c = connectionFactory.getConnection()) {
            if (uniqueId != null) {
                try (PreparedStatement ps = c.prepareStatement(this.statementProcessor.apply(PLAYER_SELECT))) {
                    ps.setString(1, uniqueId.toString());
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            final boolean hasJournal = rs.getBoolean("hasjournal");
                            quester.hasJournal = hasJournal;
                            // TODO the rest
                        }
                    }
                }
            }
        }
        return quester;
    }

    @Override
    public void saveQuesterData(final Quester quester) throws Exception {
        final UUID uniqueId = quester.getUUID();
        final String username = quester.getPlayer().getName();
        final String oldUsername = getQuesterLastKnownName(uniqueId);

        try (Connection c = connectionFactory.getConnection()) {
            if (oldUsername != null && !username.equals(oldUsername)) {
                try (PreparedStatement ps = c.prepareStatement(this.statementProcessor.apply(PLAYER_UPDATE_USERNAME_FOR_UUID))) {
                    ps.setString(1, username);
                    ps.setString(2, uniqueId.toString());
                    ps.execute();
                }
            } else {
                try (PreparedStatement ps = c.prepareStatement(this.statementProcessor.apply(PLAYER_INSERT))) {
                    ps.setString(1, uniqueId.toString());
                    ps.setString(2, username);
                    ps.setBoolean(3, quester.hasJournal);
                    ps.execute();
                }
            }
        }
        if (!username.equals(oldUsername)) {
            
        }
    }

    @Override
    public void deleteQuesterData(final UUID uniqueId) throws Exception {
        try (Connection c = connectionFactory.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(this.statementProcessor.apply(PLAYER_DELETE))) {
                ps.setString(1, uniqueId.toString());
                ps.execute();
            }
        }
        
    }

    @Override
    public String getQuesterLastKnownName(final UUID uniqueId) throws Exception {
        try (Connection c = connectionFactory.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(this.statementProcessor.apply(PLAYER_SELECT_USERNAME_BY_UUID))) {
                ps.setString(1, uniqueId.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("username");
                    }
                }
            }
        }
        return null;
    }
}
