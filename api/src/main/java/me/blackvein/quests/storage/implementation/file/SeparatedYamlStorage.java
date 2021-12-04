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

package me.blackvein.quests.storage.implementation.file;

import me.blackvein.quests.Quest;
import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;
import me.blackvein.quests.Stage;
import me.blackvein.quests.storage.implementation.StorageImplementation;
import me.blackvein.quests.util.MiscUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class SeparatedYamlStorage implements StorageImplementation {
    private final Quests plugin;
    private final String directoryPath;
    
    public SeparatedYamlStorage(final Quests plugin, final String directoryPath) {
        this.plugin = plugin;
        this.directoryPath = directoryPath;
    }

    @Override
    public Quests getPlugin() {
        return plugin;
    }

    @Override
    public String getImplementationName() {
        return "YAML";
    }

    @Override
    public void init() throws Exception {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public Quester loadQuester(final UUID uniqueId) throws Exception {
        final FileConfiguration data = new YamlConfiguration();
        Quester quester = plugin.getQuester(uniqueId);
        if (quester != null) {
            quester.hardClear();
        } else {
            quester = new Quester(plugin, uniqueId);
        }
        try {
            final File dataFile = getDataFile(quester);
            if (dataFile != null) {
                data.load(dataFile);
            } else {
                return null;
            }
        } catch (final IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            return null;
        }
        if (data.contains("completedRedoableQuests")) {
            final List<String> questIds = data.getStringList("completedRedoableQuests");
            final List<Long> questTimes = data.getLongList("completedQuestTimes");
            final ConcurrentHashMap<Quest, Long> completedTimes = quester.getCompletedTimes();
            for (int i = 0; i < questIds.size(); i++) {
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
                return null;
            }
            for (final String key : dataSec.getKeys(false)) {
                final ConfigurationSection questSec = dataSec.getConfigurationSection(key);
                final Quest quest = plugin.getQuestById(key) != null ? plugin.getQuestById(key) : plugin.getQuest(key);
                if (quest == null || !quester.getCurrentQuests().containsKey(quest)) {
                    continue;
                }
                final Stage stage = quester.getCurrentStage(quest);
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
                if (questSec.contains("blocks-broken-amounts")) {
                    final List<Integer> brokenAmounts = questSec.getIntegerList("blocks-broken-amounts");
                    int index = 0;
                    for (final int amt : brokenAmounts) {
                        final ItemStack is = quester.getCurrentStage(quest).getBlocksToBreak().get(index);
                        final ItemStack temp = is.clone();
                        temp.setAmount(amt);
                        if (quester.getQuestData(quest).getBlocksBroken().size() > 0) {
                            quester.getQuestData(quest).blocksBroken.set(index, temp);
                        }
                        index++;
                    }
                }
                if (questSec.contains("blocks-damaged-amounts")) {
                    final List<Integer> damagedAmounts = questSec.getIntegerList("blocks-damaged-amounts");
                    int index = 0;
                    for (final int amt : damagedAmounts) {
                        final ItemStack is = quester.getCurrentStage(quest).getBlocksToDamage().get(index);
                        final ItemStack temp = is.clone();
                        temp.setAmount(amt);
                        if (quester.getQuestData(quest).getBlocksDamaged().size() > 0) {
                            quester.getQuestData(quest).blocksDamaged.set(index, temp);
                        }
                        index++;
                    }
                }
                if (questSec.contains("blocks-placed-amounts")) {
                    final List<Integer> placedAmounts = questSec.getIntegerList("blocks-placed-amounts");
                    int index = 0;
                    for (final int amt : placedAmounts) {
                        final ItemStack is = quester.getCurrentStage(quest).getBlocksToPlace().get(index);
                        final ItemStack temp = is.clone();
                        temp.setAmount(amt);
                        if (quester.getQuestData(quest).getBlocksPlaced().size() > 0) {
                            quester.getQuestData(quest).blocksPlaced.set(index, temp);
                        }
                        index++;
                    }
                }
                if (questSec.contains("blocks-used-amounts")) {
                    final List<Integer> usedAmounts = questSec.getIntegerList("blocks-used-amounts");
                    int index = 0;
                    for (final int amt : usedAmounts) {
                        final ItemStack is = quester.getCurrentStage(quest).getBlocksToUse().get(index);
                        final ItemStack temp = is.clone();
                        temp.setAmount(amt);
                        if (quester.getQuestData(quest).getBlocksUsed().size() > 0) {
                            quester.getQuestData(quest).blocksUsed.set(index, temp);
                        }
                        index++;
                    }
                }
                if (questSec.contains("blocks-cut-amounts")) {
                    final List<Integer> cutAmounts = questSec.getIntegerList("blocks-cut-amounts");
                    int index = 0;
                    for (final int amt : cutAmounts) {
                        final ItemStack is = quester.getCurrentStage(quest).getBlocksToCut().get(index);
                        final ItemStack temp = is.clone();
                        temp.setAmount(amt);
                        if (quester.getQuestData(quest).getBlocksCut().size() > 0) {
                            quester.getQuestData(quest).blocksCut.set(index, temp);
                        }
                        index++;
                    }
                }
                if (questSec.contains("item-craft-amounts")) {
                    final List<Integer> craftAmounts = questSec.getIntegerList("item-craft-amounts");
                    int index = 0;
                    for (final int amt : craftAmounts) {
                        final ItemStack is = quester.getCurrentStage(quest).getItemsToCraft().get(index);
                        final ItemStack temp = new ItemStack(is.clone());
                        temp.setAmount(amt);
                        if (quester.getQuestData(quest).getItemsCrafted().size() > 0) {
                            quester.getQuestData(quest).itemsCrafted.set(index, temp);
                        }
                        index++;
                    }
                }
                if (questSec.contains("item-smelt-amounts")) {
                    final List<Integer> smeltAmounts = questSec.getIntegerList("item-smelt-amounts");
                    int index = 0;
                    for (final int amt : smeltAmounts) {
                        final ItemStack is = quester.getCurrentStage(quest).getItemsToSmelt().get(index);
                        final ItemStack temp = new ItemStack(is.clone());
                        temp.setAmount(amt);
                        if (quester.getQuestData(quest).getItemsSmelted().size() > 0) {
                            quester.getQuestData(quest).itemsSmelted.set(index, temp);
                        }
                        index++;
                    }
                }
                if (questSec.contains("item-enchant-amounts")) {
                    final List<Integer> enchantAmounts = questSec.getIntegerList("item-enchant-amounts");
                    int index = 0;
                    for (final int amt : enchantAmounts) {
                        final ItemStack is = quester.getCurrentStage(quest).getItemsToEnchant().get(index);
                        final ItemStack temp = new ItemStack(is.clone());
                        temp.setAmount(amt);
                        if (quester.getQuestData(quest).getItemsEnchanted().size() > 0) {
                            quester.getQuestData(quest).itemsEnchanted.set(index, temp);
                        }
                        index++;
                    }
                }
                if (questSec.contains("item-brew-amounts")) {
                    final List<Integer> brewAmounts = questSec.getIntegerList("item-brew-amounts");
                    int index = 0;
                    for (final int amt : brewAmounts) {
                        final ItemStack is = quester.getCurrentStage(quest).getItemsToBrew().get(index);
                        final ItemStack temp = new ItemStack(is.clone());
                        temp.setAmount(amt);
                        if (quester.getQuestData(quest).getItemsBrewed().size() > 0) {
                            quester.getQuestData(quest).itemsBrewed.set(index, temp);
                        }
                        index++;
                    }
                }
                if (questSec.contains("item-consume-amounts")) {
                    final List<Integer> consumeAmounts = questSec.getIntegerList("item-consume-amounts");
                    int index = 0;
                    for (final int amt : consumeAmounts) {
                        final ItemStack is = quester.getCurrentStage(quest).getItemsToConsume().get(index);
                        final ItemStack temp = new ItemStack(is.clone());
                        temp.setAmount(amt);
                        if (quester.getQuestData(quest).getItemsConsumed().size() > 0) {
                            quester.getQuestData(quest).itemsConsumed.set(index, temp);
                        }
                        index++;
                    }
                }
                
                if (questSec.contains("item-delivery-amounts")) {
                    final List<Integer> deliveryAmounts = questSec.getIntegerList("item-delivery-amounts");
                    int index = 0;
                    for (final int amt : deliveryAmounts) {
                        final ItemStack is = quester.getCurrentStage(quest).getItemsToDeliver().get(index);
                        final ItemStack temp = new ItemStack(is.getType(), amt, is.getDurability());
                        temp.addUnsafeEnchantments(is.getEnchantments());
                        temp.setItemMeta(is.getItemMeta());
                        if (quester.getQuestData(quest).getItemsDelivered().size() > 0) {
                            quester.getQuestData(quest).itemsDelivered.set(index, temp);
                        }
                        index++;
                    }
                }
                if (questSec.contains("has-talked-to")) {
                    final List<Boolean> talkAmount = questSec.getBooleanList("has-talked-to");
                    quester.getQuestData(quest).setCitizensInteracted(new LinkedList<>(talkAmount));
                }
                if (questSec.contains("citizen-amounts-killed")) {
                    final List<Integer> citizensAmounts = questSec.getIntegerList("citizen-amounts-killed");
                    int index = 0;
                    for (final int amt : citizensAmounts) {
                        if (quester.getQuestData(quest).getCitizensNumKilled().size() > 0) {
                            quester.getQuestData(quest).citizensNumKilled.set(index, amt);
                        }
                        index++;
                    }
                }
                if (questSec.contains("cows-milked")) {
                    quester.getQuestData(quest).setCowsMilked(questSec.getInt("cows-milked"));
                }
                if (questSec.contains("fish-caught")) {
                    quester.getQuestData(quest).setFishCaught(questSec.getInt("fish-caught"));
                }
                if (questSec.contains("players-killed")) {
                    quester.getQuestData(quest).setPlayersKilled(questSec.getInt("players-killed"));
                }
                if (questSec.contains("mobs-killed-amounts")) {
                    final List<Integer> mobAmounts = questSec.getIntegerList("mobs-killed-amounts");
                    int index = 0;
                    for (final int amt : mobAmounts) {
                        if (quester.getQuestData(quest).getMobNumKilled().size() > 0) {
                            quester.getQuestData(quest).mobNumKilled.set(index, amt);
                        }
                        index++;
                    }
                }
                if (questSec.contains("locations-to-reach")) {
                    final List<Boolean> hasReached = questSec.getBooleanList("has-reached-location");
                    quester.getQuestData(quest).setLocationsReached(new LinkedList<>(hasReached));
                }
                if (questSec.contains("mob-tame-amounts")) {
                    final List<Integer> tameAmounts = questSec.getIntegerList("mob-tame-amounts");
                    quester.getQuestData(quest).setMobsTamed(new LinkedList<>(tameAmounts));
                }
                if (questSec.contains("sheep-sheared")) {
                    final List<Integer> sheepAmounts = questSec.getIntegerList("sheep-sheared");
                    quester.getQuestData(quest).setSheepSheared(new LinkedList<>(sheepAmounts));
                }
                if (questSec.contains("passwords-said")) {
                    final List<Boolean> passAmounts = questSec.getBooleanList("passwords-said");
                    quester.getQuestData(quest).setPasswordsSaid(new LinkedList<>(passAmounts));
                }
                if (questSec.contains("custom-objective-counts")) {
                    final List<Integer> customObjCounts = questSec.getIntegerList("custom-objective-counts");
                    quester.getQuestData(quest).setCustomObjectiveCounts(new LinkedList<>(customObjCounts));
                }
                if (questSec.contains("stage-delay")) {
                    quester.getQuestData(quest).setDelayTimeLeft(questSec.getLong("stage-delay"));
                }
            }
        }
        return quester;
    }

    @Override
    public void saveQuester(final Quester quester) throws Exception {
        final FileConfiguration data = quester.getBaseData();
        try {
            data.save(new File(directoryPath + File.separator + quester.getUUID() + ".yml"));
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteQuester(final UUID uniqueId) throws Exception {
        final File f = new File(directoryPath + File.separator + uniqueId + ".yml");
        f.delete();
    }

    @Override
    public String getQuesterLastKnownName(final UUID uniqueId) throws Exception {
        Quester quester = plugin.getQuester(uniqueId);
        if (quester != null) {
            quester.hardClear();
        } else {
            quester = new Quester(plugin, uniqueId);
        }
        return quester.getLastKnownName();
    }
    
    @Override
    public Collection<UUID> getSavedUniqueIds() throws Exception {
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
            final OfflinePlayer p = quester.getOfflinePlayer();
            dataFile = new File(plugin.getDataFolder(), "data" + File.separator + p.getName() + ".yml");
            if (!dataFile.exists()) {
                return null;
            }
        }
        return dataFile;
    }
}
