/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.storage.implementation.file;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.player.BukkitQuestProgress;
import me.pikamug.quests.player.BukkitQuester;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.BukkitStage;
import me.pikamug.quests.storage.implementation.QuesterStorageImpl;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class BukkitQuesterYamlStorage implements QuesterStorageImpl {
    private final BukkitQuestsPlugin plugin;
    private final String directoryPath;
    
    public BukkitQuesterYamlStorage(final BukkitQuestsPlugin plugin, final String directoryPath) {
        this.plugin = plugin;
        this.directoryPath = directoryPath;
    }

    @Override
    public BukkitQuestsPlugin getPlugin() {
        return plugin;
    }

    @Override
    public String getImplementationName() {
        return "YAML";
    }

    @Override
    public void init() throws Exception {
    }

    @Override
    public void close() {
    }
    
    @Override
    public Quester loadQuester(final UUID uniqueId) throws IOException, InvalidConfigurationException {
        final FileConfiguration data = new YamlConfiguration();
        BukkitQuester quester = plugin.getQuester(uniqueId);
        if (quester != null) {
            quester.hardClear();
        } else {
            quester = new BukkitQuester(plugin, uniqueId);
        }
        final File dataFile = getDataFile(quester);
        if (dataFile != null) {
            data.load(dataFile);
        } else {
            return null;
        }
        if (data.contains("completedRedoableQuests")) {
            final List<String> questIds = data.getStringList("completedRedoableQuests");
            final List<Long> questTimes = data.getLongList("completedQuestTimes");
            final ConcurrentHashMap<Quest, Long> completedTimes = quester.getCompletedTimes();
            for (int i = 0; i < questIds.size(); i++) {
                if (questTimes.size() < questIds.size()) {
                    plugin.getLogger().warning("completedQuestTimes was less than completedRedoableQuests "
                            + "while loading quester of UUID " + quester.getUUID());
                }
                if (plugin.getQuestById(questIds.get(i)) != null) {
                    completedTimes.put(plugin.getQuestById(questIds.get(i)), questTimes.get(i));
                } else if (plugin.getQuest(questIds.get(i)) != null) {
                    // Legacy
                    completedTimes.put(plugin.getQuest(questIds.get(i)), questTimes.get(i));
                }
            }
            quester.setCompletedTimes(completedTimes);
        }
        if (data.contains("amountsCompletedQuests")) {
            final List<String> questIds = data.getStringList("amountsCompletedQuests");
            final List<Integer> questAmounts = data.getIntegerList("amountsCompleted");
            final ConcurrentHashMap<Quest, Integer> amountsCompleted = quester.getAmountsCompleted();
            for (int i = 0; i < questIds.size(); i++) {
                if (questAmounts.size() < questIds.size()) {
                    plugin.getLogger().warning("amountsCompleted was less than amountsCompletedQuests "
                            + "while loading quester of UUID " + quester.getUUID());
                }
                if (plugin.getQuestById(questIds.get(i)) != null) {
                    amountsCompleted.put(plugin.getQuestById(questIds.get(i)), questAmounts.get(i));
                } else if (plugin.getQuest(questIds.get(i)) != null) {
                    // Legacy
                    amountsCompleted.put(plugin.getQuest(questIds.get(i)), questAmounts.get(i));
                }
            }
            quester.setAmountsCompleted(amountsCompleted);
        }
        quester.setLastKnownName(data.getString("lastKnownName"));
        quester.setQuestPoints(data.getInt("quest-points"));
        final ConcurrentSkipListSet<Quest> completedQuests = quester.getCompletedQuests();
        if (data.isList("completed-Quests")) {
            for (final String s : data.getStringList("completed-Quests")) {
                for (final Quest q : plugin.getLoadedQuests()) {
                    if (q.getId().equals(s)) {
                        if (!quester.getCompletedQuests().contains(q)) {
                            completedQuests.add(q);
                        }
                        break;
                    } else if (q.getName().equalsIgnoreCase(s)) {
                        // Legacy
                        if (!quester.getCompletedQuests().contains(q)) {
                            completedQuests.add(q);
                        }
                        break;
                    }
                }
            }
        }
        quester.setCompletedQuests(completedQuests);
        if (!data.isString("currentQuests")) {
            final List<String> questIds = data.getStringList("currentQuests");
            final List<Integer> questStages = data.getIntegerList("currentStages");
            final int maxSize = Math.min(questIds.size(), questStages.size());
            final ConcurrentHashMap<Quest, Integer> currentQuests = quester.getCurrentQuests();
            for (int i = 0; i < maxSize; i++) {
                if (questStages.size() < questIds.size()) {
                    plugin.getLogger().warning("currentStages was less than currentQuests "
                            + "while loading quester of UUID " + quester.getUUID());
                }
                if (plugin.getQuestById(questIds.get(i)) != null) {
                    currentQuests.put(plugin.getQuestById(questIds.get(i)), questStages.get(i));
                } else if (plugin.getQuest(questIds.get(i)) != null) {
                    // Legacy
                    currentQuests.put(plugin.getQuest(questIds.get(i)), questStages.get(i));
                }
            }
            quester.setCurrentQuests(currentQuests);
            final ConfigurationSection dataSec = data.getConfigurationSection("questData");
            if (dataSec == null || dataSec.getKeys(false).isEmpty()) {
                return quester;
            }
            final ConcurrentHashMap<Quest, BukkitQuestProgress> questProgress = new ConcurrentHashMap<>();
            for (final String key : dataSec.getKeys(false)) {
                final ConfigurationSection questSec = dataSec.getConfigurationSection(key);
                final Quest quest = plugin.getQuestById(key) != null ? plugin.getQuestById(key) : plugin.getQuest(key);
                if (quest == null || !quester.getCurrentQuests().containsKey(quest)) {
                    continue;
                }
                final BukkitStage stage = (BukkitStage) quester.getCurrentStage(quest);
                if (stage == null) {
                    quest.completeQuest(quester);
                    plugin.getLogger().severe("[Quests] Invalid stage number for player: \"" + uniqueId + "\" on Quest \"" 
                            + quest.getName() + "\". Quest ended.");
                    continue;
                }
                quester.addEmptiesFor(quest, quester.getCurrentQuests().get(quest));
                if (questSec == null) {
                    continue;
                }
                final BukkitQuestProgress bukkitQuestProgress = (BukkitQuestProgress) quester.getQuestProgressOrDefault(quest);
                if (questSec.contains("blocks-broken-amounts")) {
                    final List<Integer> brokenAmounts = questSec.getIntegerList("blocks-broken-amounts");
                    bukkitQuestProgress.setBlocksBroken(new LinkedList<>(brokenAmounts));
                }
                if (questSec.contains("blocks-damaged-amounts")) {
                    final List<Integer> damagedAmounts = questSec.getIntegerList("blocks-damaged-amounts");
                    bukkitQuestProgress.setBlocksDamaged(new LinkedList<>(damagedAmounts));
                }
                if (questSec.contains("blocks-placed-amounts")) {
                    final List<Integer> placedAmounts = questSec.getIntegerList("blocks-placed-amounts");
                    bukkitQuestProgress.setBlocksPlaced(new LinkedList<>(placedAmounts));
                }
                if (questSec.contains("blocks-used-amounts")) {
                    final List<Integer> usedAmounts = questSec.getIntegerList("blocks-used-amounts");
                    bukkitQuestProgress.setBlocksUsed(new LinkedList<>(usedAmounts));
                }
                if (questSec.contains("blocks-cut-amounts")) {
                    final List<Integer> cutAmounts = questSec.getIntegerList("blocks-cut-amounts");
                    bukkitQuestProgress.setBlocksCut(new LinkedList<>(cutAmounts));
                }
                if (questSec.contains("item-craft-amounts")) {
                    final List<Integer> craftAmounts = questSec.getIntegerList("item-craft-amounts");
                    bukkitQuestProgress.setItemsCrafted(new LinkedList<>(craftAmounts));
                }
                if (questSec.contains("item-smelt-amounts")) {
                    final List<Integer> smeltAmounts = questSec.getIntegerList("item-smelt-amounts");
                    bukkitQuestProgress.setItemsSmelted(new LinkedList<>(smeltAmounts));
                }
                if (questSec.contains("item-enchant-amounts")) {
                    final List<Integer> enchantAmounts = questSec.getIntegerList("item-enchant-amounts");
                    bukkitQuestProgress.setItemsEnchanted(new LinkedList<>(enchantAmounts));
                }
                if (questSec.contains("item-brew-amounts")) {
                    final List<Integer> brewAmounts = questSec.getIntegerList("item-brew-amounts");
                    bukkitQuestProgress.setItemsBrewed(new LinkedList<>(brewAmounts));
                }
                if (questSec.contains("item-consume-amounts")) {
                    final List<Integer> consumeAmounts = questSec.getIntegerList("item-consume-amounts");
                    bukkitQuestProgress.setItemsConsumed(new LinkedList<>(consumeAmounts));
                }
                if (questSec.contains("item-delivery-amounts")) {
                    final List<Integer> deliveryAmounts = questSec.getIntegerList("item-delivery-amounts");
                    bukkitQuestProgress.setItemsDelivered(new LinkedList<>(deliveryAmounts));
                }
                if (questSec.contains("has-talked-to")) {
                    final List<Boolean> talkAmount = questSec.getBooleanList("has-talked-to");
                    bukkitQuestProgress.setNpcsInteracted(new LinkedList<>(talkAmount));
                }
                if (questSec.contains("npc-killed-amounts")) {
                    final List<Integer> npcAmounts = questSec.getIntegerList("npc-killed-amounts");
                    bukkitQuestProgress.setNpcsNumKilled(new LinkedList<>(npcAmounts));
                } else if (questSec.contains("citizen-amounts-killed")) {
                    // Legacy
                    final List<Integer> npcAmounts = questSec.getIntegerList("citizen-amounts-killed");
                    bukkitQuestProgress.setNpcsNumKilled(new LinkedList<>(npcAmounts));
                }
                if (questSec.contains("cows-milked")) {
                    bukkitQuestProgress.setCowsMilked(questSec.getInt("cows-milked"));
                }
                if (questSec.contains("fish-caught")) {
                    bukkitQuestProgress.setFishCaught(questSec.getInt("fish-caught"));
                }
                if (questSec.contains("players-killed")) {
                    bukkitQuestProgress.setPlayersKilled(questSec.getInt("players-killed"));
                }
                if (questSec.contains("mobs-killed-amounts")) {
                    final List<Integer> mobAmounts = questSec.getIntegerList("mobs-killed-amounts");
                    bukkitQuestProgress.setMobNumKilled(new LinkedList<>(mobAmounts));
                }
                if (questSec.contains("has-reached-location")) {
                    final List<Boolean> hasReached = questSec.getBooleanList("has-reached-location");
                    bukkitQuestProgress.setLocationsReached(new LinkedList<>(hasReached));
                }
                if (questSec.contains("mob-tame-amounts")) {
                    final List<Integer> tameAmounts = questSec.getIntegerList("mob-tame-amounts");
                    bukkitQuestProgress.setMobsTamed(new LinkedList<>(tameAmounts));
                }
                if (questSec.contains("sheep-sheared")) {
                    final List<Integer> sheepAmounts = questSec.getIntegerList("sheep-sheared");
                    bukkitQuestProgress.setSheepSheared(new LinkedList<>(sheepAmounts));
                }
                if (questSec.contains("passwords-said")) {
                    final List<Boolean> passAmounts = questSec.getBooleanList("passwords-said");
                    bukkitQuestProgress.setPasswordsSaid(new LinkedList<>(passAmounts));
                }
                if (questSec.contains("custom-objective-counts")) {
                    final List<Integer> customObjCounts = questSec.getIntegerList("custom-objective-counts");
                    bukkitQuestProgress.setCustomObjectiveCounts(new LinkedList<>(customObjCounts));
                }
                if (questSec.contains("stage-delay")) {
                    bukkitQuestProgress.setDelayTimeLeft(questSec.getLong("stage-delay"));
                }
                questProgress.put(quest, bukkitQuestProgress);
            }
            quester.setQuestProgress(questProgress);
        }
        return quester;
    }

    @Override
    public void saveQuester(final Quester quester) throws IOException {
        final FileConfiguration data = ((BukkitQuester)quester).getBaseData();
        data.save(new File(directoryPath + File.separator + quester.getUUID() + ".yml"));
    }

    @Override
    public void deleteQuester(final UUID uniqueId) {
        final File f = new File(directoryPath + File.separator + uniqueId + ".yml");
        f.delete();
    }

    @Override
    public String getQuesterLastKnownName(final UUID uniqueId) {
        Quester quester = plugin.getQuester(uniqueId);
        if (quester != null) {
            quester.hardClear();
        } else {
            quester = new BukkitQuester(plugin, uniqueId);
        }
        return quester.getLastKnownName();
    }
    
    @Override
    public Collection<UUID> getSavedUniqueIds() {
        final Collection<UUID> ids = new ConcurrentSkipListSet<>();
        final File folder = new File(directoryPath);
        if (!folder.exists()) {
            return ids;
        }
        final File[] listOfFiles = folder.listFiles((dir, name) -> name.endsWith(".yml"));

        if (listOfFiles == null) {
            return ids;
        }
        for (final File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                final String name = listOfFile.getName().substring(0, listOfFile.getName().lastIndexOf("."));
                final UUID id;
                try {
                    id = UUID.fromString(name);
                } catch (final IllegalArgumentException e) {
                    continue;
                }
                ids.add(id);
            }
        }
        return ids;
    }
    
    /**
     * Get data file for this Quester
     * 
     * @return file if exists, otherwise null
     */
    public File getDataFile(final Quester quester) {
        File dataFile = new File(plugin.getDataFolder(), "data" + File.separator + quester.getUUID().toString() + ".yml");
        if (!dataFile.exists()) {
            final OfflinePlayer p = ((BukkitQuester)quester).getOfflinePlayer();
            dataFile = new File(plugin.getDataFolder(), "data" + File.separator + p.getName() + ".yml");
            if (!dataFile.exists()) {
                return null;
            }
        }
        return dataFile;
    }
}
