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
import me.pikamug.quests.player.BukkitQuestProgress;
import me.pikamug.quests.player.BukkitQuester;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.components.BukkitStage;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.storage.implementation.QuesterStorageImpl;
import me.pikamug.quests.storage.implementation.sql.connection.ConnectionFactory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BukkitQuesterSqlStorage implements QuesterStorageImpl {
    private static final String PLAYER_SELECT = "SELECT lastknownname, questpoints FROM '{prefix}players' WHERE uuid=?";
    private static final String PLAYER_SELECT_UUID = "SELECT DISTINCT uuid FROM '{prefix}players'";
    private static final String PLAYER_SELECT_USERNAME = "SELECT lastknownname FROM '{prefix}players' WHERE uuid=? LIMIT 1";
    private static final String PLAYER_UPDATE_USERNAME = "UPDATE '{prefix}players' SET lastknownname=? WHERE uuid=?";
    private static final String PLAYER_INSERT = "INSERT INTO '{prefix}players' (uuid, lastknownname, questpoints) "
            + "VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE uuid=uuid, lastknownname=VALUES(lastknownname), questpoints=VALUES(questpoints)";
    private static final String PLAYER_DELETE = "DELETE FROM '{prefix}players' WHERE uuid=?";
    
    private static final String PLAYER_CURRENT_QUESTS_SELECT_BY_UUID = "SELECT questid, stageNum FROM '{prefix}player_currentquests' WHERE uuid=?";
    private static final String PLAYER_CURRENT_QUESTS_DELETE_FOR_UUID_AND_QUEST = "DELETE FROM '{prefix}player_currentquests' WHERE uuid=? AND questid=?";
    private static final String PLAYER_CURRENT_QUESTS_INSERT = "INSERT INTO '{prefix}player_currentquests' (uuid, questid, stageNum) "
            + "VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE uuid=uuid, questid=questid, stageNum=VALUES(stageNum)";
    private static final String PLAYER_CURRENT_QUESTS_DELETE = "DELETE FROM '{prefix}player_currentquests' WHERE uuid=?";
    
    private static final String PLAYER_COMPLETED_QUESTS_SELECT_BY_UUID = "SELECT questid FROM '{prefix}player_completedquests' WHERE uuid=?";
    private static final String PLAYER_COMPLETED_QUESTS_DELETE_FOR_UUID_AND_QUEST = "DELETE FROM '{prefix}player_completedquests' WHERE uuid=? AND questid=?";
    private static final String PLAYER_COMPLETED_QUESTS_INSERT = "INSERT INTO '{prefix}player_completedquests' (uuid, questid) "
            + "VALUES(?, ?) ON DUPLICATE KEY UPDATE uuid=uuid, questid=questid";
    private static final String PLAYER_COMPLETED_QUESTS_DELETE = "DELETE FROM '{prefix}player_completedquests' WHERE uuid=?";
    
    private static final String PLAYER_REDOABLE_QUESTS_SELECT_BY_UUID = "SELECT questid, lasttime, amount FROM '{prefix}player_redoablequests' WHERE uuid=?";
    private static final String PLAYER_REDOABLE_QUESTS_DELETE_FOR_UUID_AND_QUEST = "DELETE FROM '{prefix}player_redoablequests' WHERE uuid=? AND questid=?";
    private static final String PLAYER_REDOABLE_QUESTS_INSERT = "INSERT INTO '{prefix}player_redoablequests' (uuid, questid, lasttime, amount) "
            + "VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE uuid=uuid, questid=questid, lasttime=VALUES(lasttime), amount=VALUES(amount)";
    private static final String PLAYER_REDOABLE_QUESTS_DELETE = "DELETE FROM '{prefix}player_redoablequests' WHERE uuid=?";

    private static final String PLAYER_QUEST_PROGRESS_SELECT_BY_UUID = "SELECT * FROM '{prefix}player_questdata' WHERE uuid=?";
    private static final String PLAYER_QUEST_PROGRESS_DELETE_FOR_UUID_AND_QUEST = "DELETE FROM '{prefix}player_questdata' WHERE uuid=? AND quest_id=?";
    private static final String PLAYER_QUEST_PROGRESS_INSERT = "INSERT INTO '{prefix}player_questdata'"
            + " (uuid, quest_id, blocks_broken, blocks_damaged, blocks_placed, blocks_used, blocks_cut,"
            + " items_crafted, items_smelted, items_enchanted, items_brewed, items_consumed,"
            + " items_delivered, npcs_interacted, npcs_killed,"
            + " mobs_killed, mobs_tamed, fish_caught, cows_milked, sheep_sheared,"
            + " players_killed, locations_reached, passwords_said, custom_counts,"
            + " delay_start_time, delay_time_left)"
            + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            + " ON DUPLICATE KEY UPDATE uuid=uuid, quest_id=quest_id,"
            + " blocks_broken=VALUES(blocks_broken),"
            + " blocks_damaged=VALUES(blocks_damaged),"
            + " blocks_placed=VALUES(blocks_placed),"
            + " blocks_used=VALUES(blocks_used),"
            + " blocks_cut=VALUES(blocks_cut),"
            + " items_crafted=VALUES(items_crafted),"
            + " items_smelted=VALUES(items_smelted),"
            + " items_enchanted=VALUES(items_enchanted),"
            + " items_brewed=VALUES(items_brewed),"
            + " items_consumed=VALUES(items_consumed),"
            + " items_delivered=VALUES(items_delivered),"
            + " npcs_interacted=VALUES(npcs_interacted),"
            + " npcs_killed=VALUES(npcs_killed),"
            + " mobs_killed=VALUES(mobs_killed),"
            + " mobs_tamed=VALUES(mobs_tamed),"
            + " fish_caught=VALUES(fish_caught),"
            + " cows_milked=VALUES(cows_milked),"
            + " sheep_sheared=VALUES(sheep_sheared),"
            + " players_killed=VALUES(players_killed),"
            + " locations_reached=VALUES(locations_reached),"
            + " passwords_said=VALUES(passwords_said),"
            + " custom_counts=VALUES(custom_counts),"
            + " delay_start_time=VALUES(delay_start_time),"
            + " delay_time_left=VALUES(delay_time_left)";
    private static final String PLAYER_QUEST_PROGRESS_DELETE = "DELETE FROM '{prefix}player_questdata' WHERE uuid=?";

    private final BukkitQuestsPlugin plugin;
    private final ConnectionFactory connectionFactory;
    private final Function<String, String> statementProcessor;

    public BukkitQuesterSqlStorage(final BukkitQuestsPlugin plugin, final ConnectionFactory connectionFactory, final String tablePrefix) {
        this.plugin = plugin;
        this.connectionFactory = connectionFactory;
        this.statementProcessor = connectionFactory.getStatementProcessor().compose(s -> s.replace("{prefix}", tablePrefix));
    }

    @Override
    public BukkitQuestsPlugin getPlugin() {
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
        
        try (Connection c = connectionFactory.getConnection()) {
            final String[] queries = new String[5];
            queries[0] = "CREATE TABLE IF NOT EXISTS `" + statementProcessor.apply("{prefix}players") 
                    + "` (`uuid` VARCHAR(36) NOT NULL, "
                    + "`lastknownname` VARCHAR(16) NOT NULL, "
                    + "`questpoints` BIGINT NOT NULL, "
                    + "PRIMARY KEY (`uuid`)"
                    + ") DEFAULT CHARSET = utf8mb4";
            queries[1] = "CREATE TABLE IF NOT EXISTS `" + statementProcessor.apply("{prefix}player_currentquests")
                    + "` (id INT AUTO_INCREMENT NOT NULL,"
                    + "`uuid` VARCHAR(36) NOT NULL, "
                    + "`questid` VARCHAR(100) NOT NULL,"
                    + "`stageNum` INT NOT NULL,"
                    + "PRIMARY KEY (`id`),"
                    + "UNIQUE KEY (`uuid`, `questid`)"
                    + ") DEFAULT CHARSET = utf8mb4";
            queries[2] = "CREATE TABLE IF NOT EXISTS `" + statementProcessor.apply("{prefix}player_completedquests")
                    + "` (id INT AUTO_INCREMENT NOT NULL,"
                    + "`uuid` VARCHAR(36) NOT NULL, "
                    + "`questid` VARCHAR(100) NOT NULL,"
                    + "PRIMARY KEY (`id`),"
                    + "UNIQUE KEY (`uuid`, `questid`)"
                    + ") DEFAULT CHARSET = utf8mb4";
            queries[3] = "CREATE TABLE IF NOT EXISTS `" + statementProcessor.apply("{prefix}player_redoablequests")
                    + "` (id INT AUTO_INCREMENT NOT NULL,"
                    + "`uuid` VARCHAR(36) NOT NULL, "
                    + "`questid` VARCHAR(100) NOT NULL,"
                    + "`lasttime` BIGINT NOT NULL,"
                    + "`amount` INT NOT NULL,"
                    + "PRIMARY KEY (`id`),"
                    + "UNIQUE KEY (`uuid`, `questid`)"
                    + ") DEFAULT CHARSET = utf8mb4";
            queries[4] = "CREATE TABLE IF NOT EXISTS `" + statementProcessor.apply("{prefix}player_questdata")
                    + "` (id INT AUTO_INCREMENT NOT NULL,"
                    + "`uuid` VARCHAR(36) NOT NULL,  "
                    + "`quest_id` VARCHAR(100) NOT NULL,"
                    + "`blocks_broken` VARCHAR(100) NULL,"
                    + "`blocks_damaged` VARCHAR(100) NULL,"
                    + "`blocks_placed` VARCHAR(100) NULL,"
                    + "`blocks_used` VARCHAR(100) NULL,"
                    + "`blocks_cut` VARCHAR(100) NULL,"
                    + "`items_crafted` VARCHAR(100) NULL,"
                    + "`items_smelted` VARCHAR(100) NULL,"
                    + "`items_enchanted` VARCHAR(100) NULL,"
                    + "`items_brewed` VARCHAR(100) NULL,"
                    + "`items_consumed` VARCHAR(100) NULL,"
                    + "`items_delivered` VARCHAR(100) NULL,"
                    + "`npcs_interacted` VARCHAR(100) NULL,"
                    + "`npcs_killed` VARCHAR(100) NULL,"
                    + "`mobs_killed` VARCHAR(100) NULL,"
                    + "`mobs_tamed` VARCHAR(100) NULL,"
                    + "`fish_caught` INT NULL,"
                    + "`cows_milked` INT NULL,"
                    + "`sheep_sheared` VARCHAR(100) NULL,"
                    + "`players_killed` INT NULL,"
                    + "`locations_reached` VARCHAR(100) NULL,"
                    + "`passwords_said` VARCHAR(100) NULL,"
                    + "`custom_counts` VARCHAR(100) NULL,"
                    + "`delay_start_time` BIGINT NULL,"
                    + "`delay_time_left` BIGINT NULL,"
                    + "PRIMARY KEY (`id`),"
                    + "UNIQUE KEY (`uuid`, `quest_id`)"
                    + ") DEFAULT CHARSET = utf8mb4";
            try (final Statement s = c.createStatement()) {
                for (final String query : queries) {
                    try {
                        s.execute(query);
                    } catch (final SQLException e) {
                        if (e.getMessage().contains("Unknown character set")) {
                            s.execute(query.replace("utf8mb4", "utf8"));
                        } else {
                            throw e;
                        }
                    }
                }
            }
        }
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
    public Quester loadQuester(final @NotNull UUID uniqueId) throws Exception {
        final BukkitQuester quester = plugin.getQuester(uniqueId);
        if (quester == null) {
            return null;
        }
        try (final Connection c = connectionFactory.getConnection()) {
            try (final PreparedStatement ps = c.prepareStatement(statementProcessor.apply(PLAYER_SELECT))) {
                ps.setString(1, uniqueId.toString());
                try (final ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        quester.setLastKnownName(rs.getString("lastknownname"));
                        quester.setQuestPoints(rs.getInt("questpoints"));
                    }
                }
            }
            quester.setCurrentQuests(getQuesterCurrentQuests(uniqueId));
            quester.setCompletedQuests(getQuesterCompletedQuests(uniqueId));
            quester.setCompletedTimes(getQuesterCompletedTimes(uniqueId));
            quester.setAmountsCompleted(getQuesterAmountsCompleted(uniqueId));
            quester.setQuestProgress(getQuesterQuestProgress(uniqueId));
        }
        return quester;
    }

    @Override
    public void saveQuester(final Quester quester) throws SQLException {
        BukkitQuester bukkitQuester = (BukkitQuester) quester;
        final UUID uniqueId = bukkitQuester.getUUID();
        final String lastKnownName = bukkitQuester.getLastKnownName();
        final String oldLastKnownName = getQuesterLastKnownName(uniqueId);
        final Set<String> currentQuests = bukkitQuester.getCurrentQuests().keySet().stream().map(Quest::getId).collect(Collectors.toSet());
        final Set<String> oldCurrentQuests = getQuesterCurrentQuests(uniqueId).keySet().stream().map(Quest::getId).collect(Collectors.toSet());
        oldCurrentQuests.removeAll(currentQuests);
        final Set<String> completedQuests = bukkitQuester.getCompletedQuests().stream().map(Quest::getId).collect(Collectors.toSet());
        final Set<String> oldCompletedQuests = getQuesterCompletedQuests(uniqueId).stream().map(Quest::getId).collect(Collectors.toSet());
        oldCompletedQuests.removeAll(completedQuests);
        final Set<String> redoableQuests = bukkitQuester.getCompletedTimes().keySet().stream().map(Quest::getId).collect(Collectors.toSet());
        final Set<String> oldRedoableQuests = getQuesterCompletedTimes(uniqueId).keySet().stream().map(Quest::getId).collect(Collectors.toSet());
        oldRedoableQuests.removeAll(redoableQuests);
        final Set<String> questData = bukkitQuester.getQuestProgress().keySet().stream().map(Quest::getId).collect(Collectors.toSet());
        final Set<String> oldQuestData = getQuesterQuestProgress(uniqueId).keySet().stream().map(Quest::getId).collect(Collectors.toSet());
        oldQuestData.removeAll(questData);
        
        try (final Connection c = connectionFactory.getConnection()) {
            if (oldLastKnownName != null && lastKnownName != null && !lastKnownName.equals(oldLastKnownName)) {
                try (final PreparedStatement ps = c.prepareStatement(statementProcessor.apply(PLAYER_UPDATE_USERNAME))) {
                    ps.setString(1, lastKnownName);
                    ps.setString(2, uniqueId.toString());
                    ps.execute();
                }
            } else {
                try (final PreparedStatement ps = c.prepareStatement(statementProcessor.apply(PLAYER_INSERT))) {
                    ps.setString(1, uniqueId.toString());
                    ps.setString(2, lastKnownName != null ? lastKnownName : "unspecified");
                    ps.setInt(3, bukkitQuester.getQuestPoints());
                    ps.execute();
                }
            }
            
            if (!oldCurrentQuests.isEmpty()) {
                for (final String questId : oldCurrentQuests) {
                    try (final PreparedStatement ps = c.prepareStatement(statementProcessor.apply(PLAYER_CURRENT_QUESTS_DELETE_FOR_UUID_AND_QUEST))) {
                        ps.setString(1, uniqueId.toString());
                        ps.setString(2, questId);
                        ps.execute();
                    }
                }
            } else {
                for (final Entry<Quest, Integer> entry : bukkitQuester.getCurrentQuests().entrySet()) {
                    try (final PreparedStatement ps = c.prepareStatement(statementProcessor.apply(PLAYER_CURRENT_QUESTS_INSERT))) {
                        ps.setString(1, uniqueId.toString());
                        ps.setString(2, entry.getKey().getId());
                        ps.setInt(3, entry.getValue());
                        ps.execute();
                    }
                }
            }
            
            if (!oldCompletedQuests.isEmpty()) {
                for (final String questId : oldCompletedQuests) {
                    try (final PreparedStatement ps = c.prepareStatement(statementProcessor.apply(PLAYER_COMPLETED_QUESTS_DELETE_FOR_UUID_AND_QUEST))) {
                        ps.setString(1, uniqueId.toString());
                        ps.setString(2, questId);
                        ps.execute();
                    }
                }
            } else {
                for (final Quest quest : bukkitQuester.getCompletedQuests()) {
                    try (final PreparedStatement ps = c.prepareStatement(statementProcessor.apply(PLAYER_COMPLETED_QUESTS_INSERT))) {
                        ps.setString(1, uniqueId.toString());
                        ps.setString(2, quest.getId());
                        ps.execute();
                    }
                }
            }
            
            if (!oldRedoableQuests.isEmpty()) {
                for (final String questId : oldRedoableQuests) {
                    try (final PreparedStatement ps = c.prepareStatement(statementProcessor.apply(PLAYER_REDOABLE_QUESTS_DELETE_FOR_UUID_AND_QUEST))) {
                        ps.setString(1, uniqueId.toString());
                        ps.setString(2, questId);
                        ps.execute();
                    }
                }
            } else {
                for (final Entry<Quest, Long> entry : bukkitQuester.getCompletedTimes().entrySet()) {
                    if (entry.getKey() == null) {
                        plugin.getLogger().severe("Quest was null for completed times of quester " + bukkitQuester.getUUID());
                        return;
                    }
                    if (!bukkitQuester.getAmountsCompleted().containsKey(entry.getKey()) || bukkitQuester.getAmountsCompleted().get(entry.getKey()) == null) {
                        plugin.getLogger().warning("Quester " + bukkitQuester.getUUID() + " is missing amounts completed for quest ID " + entry.getKey().getId());
                        return;
                    }
                    final int amount = bukkitQuester.getAmountsCompleted().get(entry.getKey());
                    try (final PreparedStatement ps = c.prepareStatement(statementProcessor.apply(PLAYER_REDOABLE_QUESTS_INSERT))) {
                        ps.setString(1, uniqueId.toString());
                        ps.setString(2, entry.getKey().getId());
                        ps.setLong(3, entry.getValue());
                        ps.setInt(4, amount);
                        ps.execute();
                    }
                }
            }

            if (!oldQuestData.isEmpty()) {
                for (final String questId : oldQuestData) {
                    try (final PreparedStatement ps = c.prepareStatement(statementProcessor.apply(PLAYER_QUEST_PROGRESS_DELETE_FOR_UUID_AND_QUEST))) {
                        ps.setString(1, uniqueId.toString());
                        ps.setString(2, questId);
                        ps.execute();
                    }
                }
            } else {
                for (final Entry<Quest, BukkitQuestProgress> entry : bukkitQuester.getQuestProgress().entrySet()) {
                    try (final PreparedStatement ps = c.prepareStatement(statementProcessor.apply(PLAYER_QUEST_PROGRESS_INSERT))) {
                        ps.setString(1, uniqueId.toString());
                        ps.setString(2, entry.getKey().getId());
                        ps.setString(3, serializeItemStackProgress(entry.getValue().getBlocksBroken()));
                        ps.setString(4, serializeItemStackProgress(entry.getValue().getBlocksDamaged()));
                        ps.setString(5, serializeItemStackProgress(entry.getValue().getBlocksPlaced()));
                        ps.setString(6, serializeItemStackProgress(entry.getValue().getBlocksUsed()));
                        ps.setString(7, serializeItemStackProgress(entry.getValue().getBlocksCut()));
                        ps.setString(8, serializeItemStackProgress(entry.getValue().getItemsCrafted()));
                        ps.setString(9, serializeItemStackProgress(entry.getValue().getItemsSmelted()));
                        ps.setString(10, serializeItemStackProgress(entry.getValue().getItemsEnchanted()));
                        ps.setString(11, serializeItemStackProgress(entry.getValue().getItemsBrewed()));
                        ps.setString(12, serializeItemStackProgress(entry.getValue().getItemsConsumed()));
                        ps.setString(13, serializeItemStackProgress(entry.getValue().getItemsDelivered()));
                        ps.setString(14, serializeProgress(entry.getValue().getNpcsInteracted()));
                        ps.setString(15, serializeProgress(entry.getValue().getNpcsNumKilled()));
                        ps.setString(16, serializeProgress(entry.getValue().getMobNumKilled()));
                        ps.setString(17, serializeProgress(entry.getValue().getMobsTamed()));
                        ps.setInt(18, entry.getValue().getFishCaught());
                        ps.setInt(19, entry.getValue().getCowsMilked());
                        ps.setString(20, serializeProgress(entry.getValue().getSheepSheared()));
                        ps.setInt(21, entry.getValue().getPlayersKilled());
                        ps.setString(22, serializeProgress(entry.getValue().getLocationsReached()));
                        ps.setString(23, serializeProgress(entry.getValue().getPasswordsSaid()));
                        ps.setString(24, serializeProgress(entry.getValue().getCustomObjectiveCounts()));
                        ps.setLong(25, entry.getValue().getDelayStartTime());
                        ps.setLong(26, entry.getValue().getDelayTimeLeft());
                        ps.execute();
                    }
                }
            }
        }
    }

    @Override
    public void deleteQuester(final UUID uniqueId) throws Exception {
        try (final Connection c = connectionFactory.getConnection()) {
            try (final PreparedStatement ps = c.prepareStatement(statementProcessor.apply(PLAYER_DELETE))) {
                ps.setString(1, uniqueId.toString());
                ps.execute();
            }
            try (final PreparedStatement ps = c.prepareStatement(statementProcessor.apply(PLAYER_CURRENT_QUESTS_DELETE))) {
                ps.setString(1, uniqueId.toString());
                ps.execute();
            }
            try (final PreparedStatement ps = c.prepareStatement(statementProcessor.apply(PLAYER_COMPLETED_QUESTS_DELETE))) {
                ps.setString(1, uniqueId.toString());
                ps.execute();
            }
            try (final PreparedStatement ps = c.prepareStatement(statementProcessor.apply(PLAYER_REDOABLE_QUESTS_DELETE))) {
                ps.setString(1, uniqueId.toString());
                ps.execute();
            }
            try (final PreparedStatement ps = c.prepareStatement(statementProcessor.apply(PLAYER_QUEST_PROGRESS_DELETE))) {
                ps.setString(1, uniqueId.toString());
                ps.execute();
            }
        }
    }

    @Override
    public String getQuesterLastKnownName(final UUID uniqueId) throws SQLException {
        try (final Connection c = connectionFactory.getConnection()) {
            try (final PreparedStatement ps = c.prepareStatement(statementProcessor.apply(PLAYER_SELECT_USERNAME))) {
                ps.setString(1, uniqueId.toString());
                try (final ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("lastknownname");
                    }
                }
            }
        }
        return null;
    }
    
    public ConcurrentHashMap<Quest, Integer> getQuesterCurrentQuests(final UUID uniqueId) throws SQLException {
        final ConcurrentHashMap<Quest, Integer> currentQuests = new ConcurrentHashMap<>();
        try (final Connection c = connectionFactory.getConnection()) {
            try (final PreparedStatement ps = c.prepareStatement(statementProcessor.apply(PLAYER_CURRENT_QUESTS_SELECT_BY_UUID))) {
                ps.setString(1, uniqueId.toString());
                try (final ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        final Quest quest = plugin.getQuestById(rs.getString("questid"));
                        if (quest != null) {
                            currentQuests.put(quest, rs.getInt("stageNum"));
                        }
                    }
                }
            }
        }
        return currentQuests;
    }

    public ConcurrentHashMap<Quest, BukkitQuestProgress> getQuesterQuestProgress(final UUID uniqueId) throws SQLException {
        final Quester quester = plugin.getQuester(uniqueId);
        final ConcurrentHashMap<Quest, BukkitQuestProgress> questProgress = new ConcurrentHashMap<>();
        try (final Connection c = connectionFactory.getConnection()) {
            try (final PreparedStatement ps = c.prepareStatement(statementProcessor.apply(PLAYER_QUEST_PROGRESS_SELECT_BY_UUID))) {
                ps.setString(1, uniqueId.toString());
                try (final ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        final Quest quest = plugin.getQuestById(rs.getString("quest_id"));
                        final BukkitQuestProgress data = new BukkitQuestProgress(quester);
                        if (quest != null && quester.getCurrentStage(quest) != null) {
                            final BukkitStage stage = (BukkitStage) quester.getCurrentStage(quest);
                            data.blocksBroken.addAll(deserializeItemStackProgress(rs.getString("blocks_broken"),
                                    stage.getBlocksToBreak()));
                            data.blocksDamaged.addAll(deserializeItemStackProgress(rs.getString("blocks_damaged"),
                                    stage.getBlocksToDamage()));
                            data.blocksPlaced.addAll(deserializeItemStackProgress(rs.getString("blocks_placed"),
                                    stage.getBlocksToPlace()));
                            data.blocksUsed.addAll(deserializeItemStackProgress(rs.getString("blocks_used"),
                                    stage.getBlocksToUse()));
                            data.blocksCut.addAll(deserializeItemStackProgress(rs.getString("blocks_cut"),
                                    stage.getBlocksToCut()));
                            data.itemsCrafted.addAll(deserializeItemStackProgress(rs.getString("items_crafted"),
                                    stage.getItemsToCraft()));
                            data.itemsSmelted.addAll(deserializeItemStackProgress(rs.getString("items_smelted"),
                                    stage.getItemsToSmelt()));
                            data.itemsEnchanted.addAll(deserializeItemStackProgress(rs.getString("items_enchanted"),
                                    stage.getItemsToEnchant()));
                            data.itemsBrewed.addAll(deserializeItemStackProgress(rs.getString("items_brewed"),
                                    stage.getItemsToBrew()));
                            data.itemsConsumed.addAll(deserializeItemStackProgress(rs.getString("items_consumed"),
                                    stage.getItemsToConsume()));
                            data.itemsDelivered.addAll(deserializeItemStackProgress(rs.getString("items_delivered"),
                                    stage.getItemsToDeliver()));
                            data.npcsInteracted.addAll(deserializeBooleanProgress(rs.getString("npcs_interacted")));
                            data.npcsNumKilled.addAll(deserializeIntProgress(rs.getString("npcs_killed")));
                            data.mobNumKilled.addAll(deserializeIntProgress(rs.getString("mobs_killed")));
                            data.mobsTamed.addAll(deserializeIntProgress(rs.getString("mobs_tamed")));
                            data.setFishCaught(rs.getInt("fish_caught"));
                            data.setCowsMilked(rs.getInt("cows_milked"));
                            data.sheepSheared.addAll(deserializeIntProgress(rs.getString("sheep_sheared")));
                            data.setPlayersKilled(rs.getInt("players_killed"));
                            data.locationsReached.addAll(deserializeBooleanProgress(rs.getString("locations_reached")));
                            data.passwordsSaid.addAll(deserializeBooleanProgress(rs.getString("passwords_said")));
                            data.customObjectiveCounts.addAll(deserializeIntProgress(rs.getString("custom_counts")));
                            data.setDelayStartTime(rs.getLong("delay_start_time"));
                            data.setDelayTimeLeft(rs.getLong("delay_time_left"));
                            questProgress.put(quest, data);
                        }
                    }
                }
            }
        }
        return questProgress;
    }
    
    public ConcurrentSkipListSet<Quest> getQuesterCompletedQuests(final UUID uniqueId) throws SQLException {
        final ConcurrentSkipListSet<Quest> completedQuests = new ConcurrentSkipListSet<>();
        try (final Connection c = connectionFactory.getConnection()) {
            try (final PreparedStatement ps = c.prepareStatement(statementProcessor.apply(PLAYER_COMPLETED_QUESTS_SELECT_BY_UUID))) {
                ps.setString(1, uniqueId.toString());
                try (final ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        final Quest quest = plugin.getQuestById(rs.getString("questid"));
                        if (quest != null) {
                            completedQuests.add(quest);
                        }
                    }
                }
            }
        }
        return completedQuests;
    }
    
    public ConcurrentHashMap<Quest, Long> getQuesterCompletedTimes(final UUID uniqueId) throws SQLException {
        final ConcurrentHashMap<Quest, Long> completedTimes = new ConcurrentHashMap<>();
        try (final Connection c = connectionFactory.getConnection()) {
            try (final PreparedStatement ps = c.prepareStatement(statementProcessor.apply(PLAYER_REDOABLE_QUESTS_SELECT_BY_UUID))) {
                ps.setString(1, uniqueId.toString());
                try (final ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        final Quest quest = plugin.getQuestById(rs.getString("questid"));
                        if (quest != null) {
                            completedTimes.put(quest, rs.getLong("lasttime"));
                        }
                    }
                }
            }
        }
        return completedTimes;
    }
    
    public ConcurrentHashMap<Quest, Integer> getQuesterAmountsCompleted(final UUID uniqueId) throws SQLException {
        final ConcurrentHashMap<Quest, Integer> amountsCompleted = new ConcurrentHashMap<>();
        try (final Connection c = connectionFactory.getConnection()) {
            try (final PreparedStatement ps = c.prepareStatement(statementProcessor.apply(PLAYER_REDOABLE_QUESTS_SELECT_BY_UUID))) {
                ps.setString(1, uniqueId.toString());
                try (final ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        final Quest quest = plugin.getQuestById(rs.getString("questid"));
                        if (quest != null) {
                            amountsCompleted.put(quest, rs.getInt("amount"));
                        }
                    }
                }
            }
        }
        return amountsCompleted;
    }

    @Override
    public Collection<UUID> getSavedUniqueIds() throws SQLException {
        final Collection<UUID> ids = new ConcurrentSkipListSet<>();
        try (final Connection c = connectionFactory.getConnection()) {
            try (final PreparedStatement ps = c.prepareStatement(statementProcessor.apply(PLAYER_SELECT_UUID))) {
                try (final ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        final UUID id;
                        try {
                            id = UUID.fromString(rs.getString("uuid"));
                        } catch (final IllegalArgumentException e) {
                            continue;
                        }
                        ids.add(id);
                    }
                }
            }
        }
        return ids;
    }

    public String serializeProgress(final LinkedList<?> list) {
        if (list.isEmpty()) {
            return null;
        } else if (list.size() == 1) {
            return String.valueOf(list.get(0));
        } else {
            return list.stream().map(String::valueOf).collect(Collectors.joining(",", "{", "}"));
        }
    }

    public LinkedList<Integer> deserializeIntProgress(String string) {
        final LinkedList<Integer> list = new LinkedList<>();
        if (string != null) {
            string = string.replace("{", "").replace("}", "");
            for (final String section : string.split(",")) {
                list.add(Integer.parseInt(section));
            }
        }
        return list;
    }

    public LinkedList<Boolean> deserializeBooleanProgress(String string) {
        final LinkedList<Boolean> list = new LinkedList<>();
        if (string != null) {
            string = string.replace("{", "").replace("}", "");
            for (final String section : string.split(",")) {
                list.add(Boolean.parseBoolean(section));
            }
        }
        return list;
    }

    public String serializeItemStackProgress(final LinkedList<ItemStack> list) {
        if (list.isEmpty()) {
            return null;
        } else if (list.size() == 1) {
            return String.valueOf(list.get(0).getAmount());
        } else {
            return list.stream().map(n -> String.valueOf(n.getAmount())).collect(Collectors.joining(",", "{", "}"));
        }
    }

    @SuppressWarnings("deprecation")
    public LinkedList<ItemStack> deserializeItemStackProgress(String string, final LinkedList<ItemStack> objective) {
        final LinkedList<ItemStack> list = new LinkedList<>();
        if (string != null) {
            string = string.replace("{", "").replace("}", "");
            int index = 0;
            for (final String section : string.split(",")) {
                if (index < objective.size()) {
                    final int amt = Integer.parseInt(section);
                    final ItemStack is = objective.get(index);
                    final ItemStack temp = new ItemStack(is.getType(), amt, is.getDurability());
                    temp.addUnsafeEnchantments(is.getEnchantments());
                    temp.setItemMeta(is.getItemMeta());
                    list.add(temp);
                    index++;
                } else {
                    break;
                }
            }
        }
        return list;
    }
}
