/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.storage.implementation.sql;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.Quests;
import me.pikamug.quests.player.BukkitQuestProgress;
import me.pikamug.quests.player.BukkitQuester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.Stage;
import me.pikamug.quests.storage.implementation.sql.connection.ConnectionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BukkitQuesterSqlStorageTest {
    private static final String URL = "jdbc:h2:mem:quests;MODE=MySQL;DB_CLOSE_DELAY=-1";

    private BukkitQuestsPlugin plugin;
    private BukkitQuester quester;
    private BukkitQuesterSqlStorage storage;
    private UUID uniqueId;
    private Quest activeQuest;
    private Quest staleQuest;

    @Before
    public void setUp() throws Exception {
        plugin = mock(BukkitQuestsPlugin.class);
        quester = mock(BukkitQuester.class);
        storage = new BukkitQuesterSqlStorage(plugin, new H2ConnectionFactory(), "quests_");
        storage.init();

        uniqueId = UUID.randomUUID();
        activeQuest = quest("active");
        staleQuest = quest("stale");

        when(plugin.getQuester(uniqueId)).thenReturn(quester);
        when(plugin.getQuestById("active")).thenReturn(activeQuest);
        when(plugin.getQuestById("stale")).thenReturn(staleQuest);
        when(quester.getUUID()).thenReturn(uniqueId);
        when(quester.getLastKnownName()).thenReturn("Quester");
        when(quester.getQuestPoints()).thenReturn(10);
        when(quester.getCompletedQuests()).thenReturn(Collections.emptySet());
        when(quester.getCurrentStage(any(Quest.class))).thenReturn(mock(Stage.class));

        try (Connection connection = DriverManager.getConnection(URL);
             Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO quests_players VALUES ('" + uniqueId + "', 'Quester', 10)");
            statement.execute("INSERT INTO quests_player_currentquests VALUES (NULL, '" + uniqueId
                    + "', 'active', 0), (NULL, '" + uniqueId + "', 'stale', 0)");
            statement.execute("INSERT INTO quests_player_questdata (uuid, quest_id, mobs_killed) VALUES ('"
                    + uniqueId + "', 'active', '1'), ('" + uniqueId + "', 'stale', '2')");
        }
    }

    @After
    public void tearDown() throws Exception {
        try (Connection connection = DriverManager.getConnection(URL);
             Statement statement = connection.createStatement()) {
            statement.execute("DROP ALL OBJECTS");
        }
    }

    @Test
    public void removesStaleRowsAndUpsertsCurrentProgressInOneSave() throws Exception {
        setCurrentState(1, 5);

        storage.saveQuester(quester);

        assertEquals(1, queryInt("SELECT stageNum FROM quests_player_currentquests "
                + "WHERE uuid=? AND questid='active'"));
        assertEquals("5", queryString("SELECT mobs_killed FROM quests_player_questdata "
                + "WHERE uuid=? AND quest_id='active'"));
        assertFalse(exists("SELECT 1 FROM quests_player_currentquests WHERE uuid=? AND questid='stale'"));
        assertFalse(exists("SELECT 1 FROM quests_player_questdata WHERE uuid=? AND quest_id='stale'"));
    }

    @Test
    public void rollsBackAllChangesWhenSaveFails() throws Exception {
        setCurrentState(1, 5);
        final Quest invalidQuest = quest(String.join("", Collections.nCopies(101, "q")));
        final ConcurrentHashMap<Quest, Long> completedTimes = new ConcurrentHashMap<>();
        completedTimes.put(invalidQuest, 100L);
        final ConcurrentHashMap<Quest, Integer> amountsCompleted = new ConcurrentHashMap<>();
        amountsCompleted.put(invalidQuest, 1);
        when(quester.getCompletedTimes()).thenReturn(completedTimes);
        when(quester.getAmountsCompleted()).thenReturn(amountsCompleted);

        try {
            storage.saveQuester(quester);
            fail("Expected save failure");
        } catch (final SQLException expected) {
            // Expected because the completion amount is missing.
        }

        assertEquals(0, queryInt("SELECT stageNum FROM quests_player_currentquests "
                + "WHERE uuid=? AND questid='active'"));
        assertEquals("1", queryString("SELECT mobs_killed FROM quests_player_questdata "
                + "WHERE uuid=? AND quest_id='active'"));
    }

    @Test
    public void repairsMissingCompletionAmount() throws Exception {
        setCurrentState(1, 5);
        final ConcurrentHashMap<Quest, Long> completedTimes = new ConcurrentHashMap<>();
        completedTimes.put(activeQuest, 100L);
        when(quester.getCompletedTimes()).thenReturn(completedTimes);
        when(quester.getAmountsCompleted()).thenReturn(new ConcurrentHashMap<>());

        storage.saveQuester(quester);

        assertEquals(1, queryInt("SELECT amount FROM quests_player_redoablequests "
                + "WHERE uuid=? AND questid='active'"));
    }

    private void setCurrentState(final int stage, final int mobsKilled) {
        final ConcurrentHashMap<Quest, Integer> currentQuests = new ConcurrentHashMap<>();
        currentQuests.put(activeQuest, stage);
        when(quester.getCurrentQuests()).thenReturn(currentQuests);
        when(quester.getCompletedTimes()).thenReturn(new ConcurrentHashMap<>());
        when(quester.getAmountsCompleted()).thenReturn(new ConcurrentHashMap<>());

        final BukkitQuestProgress progress = new BukkitQuestProgress(quester);
        progress.setDoJournalUpdate(false);
        progress.getMobNumKilled().add(mobsKilled);
        final ConcurrentHashMap<Quest, BukkitQuestProgress> questProgress = new ConcurrentHashMap<>();
        questProgress.put(activeQuest, progress);
        when(quester.getQuestProgress()).thenReturn(questProgress);
    }

    private Quest quest(final String id) {
        final Quest quest = mock(Quest.class);
        when(quest.getId()).thenReturn(id);
        return quest;
    }

    private int queryInt(final String sql) throws SQLException {
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uniqueId.toString());
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1);
            }
        }
    }

    private String queryString(final String sql) throws SQLException {
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uniqueId.toString());
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getString(1);
            }
        }
    }

    private boolean exists(final String sql) throws SQLException {
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uniqueId.toString());
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private static class H2ConnectionFactory implements ConnectionFactory {
        @Override
        public String getImplementationName() {
            return "H2 test";
        }

        @Override
        public void init(final Quests plugin) {
        }

        @Override
        public void close() {
        }

        @Override
        public Function<String, String> getStatementProcessor() {
            return sql -> sql.replace('\'', '`').replace(" DEFAULT CHARSET = utf8mb4", "")
                    .replace(" DEFAULT CHARSET = utf8", "");
        }

        @Override
        public Connection getConnection() throws SQLException {
            return DriverManager.getConnection(URL);
        }
    }
}
