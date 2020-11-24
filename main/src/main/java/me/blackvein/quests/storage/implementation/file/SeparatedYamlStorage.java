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

package me.blackvein.quests.storage.implementation.file;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import me.blackvein.quests.Quest;
import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;
import me.blackvein.quests.Stage;
import me.blackvein.quests.storage.implementation.StorageImplementation;
import me.blackvein.quests.util.ConfigUtil;
import me.blackvein.quests.util.MiscUtil;

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
    public Quester loadQuesterData(final UUID uniqueId) throws Exception {
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
        } catch (final IOException e) {
            e.printStackTrace();
            return null;
        } catch (final InvalidConfigurationException e) {
            e.printStackTrace();
            return null;
        }
        if (data.contains("completedRedoableQuests")) {
            final List<String> questNames = data.getStringList("completedRedoableQuests");
            final List<Long> questTimes = data.getLongList("completedQuestTimes");
            for (int i = 0; i < questNames.size(); i++) {
                final ConcurrentHashMap<Quest, Long> completedTimes = quester.getCompletedTimes();
                completedTimes.put(plugin.getQuest(questNames.get(i)), questTimes.get(i));
                quester.setCompletedTimes(completedTimes);
            }
        }
        if (data.contains("amountsCompletedQuests")) {
            final List<String> questNames = data.getStringList("amountsCompletedQuests");
            final List<Integer> questAmts = data.getIntegerList("amountsCompleted");
            for (int i = 0; i < questNames.size(); i++) {
                final ConcurrentHashMap<Quest, Integer> amountsCompleted = quester.getAmountsCompleted();
                amountsCompleted.put(plugin.getQuest(questNames.get(i)), questAmts.get(i));
                quester.setAmountsCompleted(amountsCompleted);
            }
        }
        int questPoints = quester.getQuestPoints();
        questPoints = data.getInt("quest-points");
        quester.setQuestPoints(questPoints);
        quester.hasJournal = data.getBoolean("hasJournal");
        if (data.isList("completed-Quests")) {
            for (final String s : data.getStringList("completed-Quests")) {
                for (final Quest q : plugin.getQuests()) {
                    if (q.getName().equalsIgnoreCase(s)) {
                        if (!quester.getCompletedQuests().contains(q)) {
                            final LinkedList<Quest> completedQuests = quester.getCompletedQuests();
                            completedQuests.add(q);
                            quester.setCompletedQuests(completedQuests);
                        }
                        break;
                    }
                }
            }
        } else {
            quester.setCompletedQuests(new LinkedList<Quest>());
        }
        if (data.isString("currentQuests") == false) {
            final List<String> questNames = data.getStringList("currentQuests");
            final List<Integer> questStages = data.getIntegerList("currentStages");
            // These appear to differ sometimes? That seems bad.
            final int maxSize = Math.min(questNames.size(), questStages.size());
            for (int i = 0; i < maxSize; i++) {
                if (plugin.getQuest(questNames.get(i)) != null) {
                    final ConcurrentHashMap<Quest, Integer> currentQuests = quester.getCurrentQuests();
                    currentQuests.put(plugin.getQuest(questNames.get(i)), questStages.get(i));
                    quester.setCurrentQuests(currentQuests);
                }
            }
            final ConfigurationSection dataSec = data.getConfigurationSection("questData");
            if (dataSec == null || dataSec.getKeys(false).isEmpty()) {
                return null;
            }
            for (final String key : dataSec.getKeys(false)) {
                final ConfigurationSection questSec = dataSec.getConfigurationSection(key);
                final Quest quest = plugin.getQuest(key);
                Stage stage;
                if (quest == null || quester.getCurrentQuests().containsKey(quest) == false) {
                    continue;
                }
                stage = quester.getCurrentStage(quest);
                if (stage == null) {
                    quest.completeQuest(quester);
                    plugin.getLogger().severe("[Quests] Invalid stage number for player: \"" + uniqueId + "\" on Quest \"" 
                            + quest.getName() + "\". Quest ended.");
                    continue;
                }
                quester.addEmptiesFor(quest, quester.getCurrentQuests().get(quest));
                if (questSec == null)
                    continue;
                if (questSec.contains("blocks-broken-names")) {
                    final List<String> names = questSec.getStringList("blocks-broken-names");
                    final List<Integer> amounts = questSec.getIntegerList("blocks-broken-amounts");
                    final List<Short> durability = questSec.getShortList("blocks-broken-durability");
                    int index = 0;
                    for (final String s : names) {
                        ItemStack is;
                        try {
                            is = new ItemStack(Material.matchMaterial(s), amounts.get(index), durability.get(index));
                        } catch (final IndexOutOfBoundsException e) {
                            // Legacy
                            is = new ItemStack(Material.matchMaterial(s), amounts.get(index), (short) 0);
                        }
                        if (quester.getQuestData(quest).blocksBroken.size() > 0) {
                            quester.getQuestData(quest).blocksBroken.set(index, is);
                        }
                        index++;
                    }
                }
                if (questSec.contains("blocks-damaged-names")) {
                    final List<String> names = questSec.getStringList("blocks-damaged-names");
                    final List<Integer> amounts = questSec.getIntegerList("blocks-damaged-amounts");
                    final List<Short> durability = questSec.getShortList("blocks-damaged-durability");
                    int index = 0;
                    for (final String s : names) {
                        ItemStack is;
                        try {
                            is = new ItemStack(Material.matchMaterial(s), amounts.get(index), durability.get(index));
                        } catch (final IndexOutOfBoundsException e) {
                            // Legacy
                            is = new ItemStack(Material.matchMaterial(s), amounts.get(index), (short) 0);
                        }
                        if (quester.getQuestData(quest).blocksDamaged.size() > 0) {
                            quester.getQuestData(quest).blocksDamaged.set(index, is);
                        }
                        index++;
                    }
                }
                if (questSec.contains("blocks-placed-names")) {
                    final List<String> names = questSec.getStringList("blocks-placed-names");
                    final List<Integer> amounts = questSec.getIntegerList("blocks-placed-amounts");
                    final List<Short> durability = questSec.getShortList("blocks-placed-durability");
                    int index = 0;
                    for (final String s : names) {
                        ItemStack is;
                        try {
                            is = new ItemStack(Material.matchMaterial(s), amounts.get(index), durability.get(index));
                        } catch (final IndexOutOfBoundsException e) {
                            // Legacy
                            is = new ItemStack(Material.matchMaterial(s), amounts.get(index), (short) 0);
                        }
                        if (quester.getQuestData(quest).blocksPlaced.size() > 0) {
                            quester.getQuestData(quest).blocksPlaced.set(index, is);
                        }
                        index++;
                    }
                }
                if (questSec.contains("blocks-used-names")) {
                    final List<String> names = questSec.getStringList("blocks-used-names");
                    final List<Integer> amounts = questSec.getIntegerList("blocks-used-amounts");
                    final List<Short> durability = questSec.getShortList("blocks-used-durability");
                    int index = 0;
                    for (final String s : names) {
                        ItemStack is;
                        try {
                            is = new ItemStack(Material.matchMaterial(s), amounts.get(index), durability.get(index));
                        } catch (final IndexOutOfBoundsException e) {
                            // Legacy
                            is = new ItemStack(Material.matchMaterial(s), amounts.get(index), (short) 0);
                        }
                        if (quester.getQuestData(quest).blocksUsed.size() > 0) {
                            quester.getQuestData(quest).blocksUsed.set(index, is);
                        }
                        index++;
                    }
                }
                if (questSec.contains("blocks-cut-names")) {
                    final List<String> names = questSec.getStringList("blocks-cut-names");
                    final List<Integer> amounts = questSec.getIntegerList("blocks-cut-amounts");
                    final List<Short> durability = questSec.getShortList("blocks-cut-durability");
                    int index = 0;
                    for (final String s : names) {
                        ItemStack is;
                        try {
                            is = new ItemStack(Material.matchMaterial(s), amounts.get(index), durability.get(index));
                        } catch (final IndexOutOfBoundsException e) {
                            // Legacy
                            is = new ItemStack(Material.matchMaterial(s), amounts.get(index), (short) 0);
                        }
                        if (quester.getQuestData(quest).blocksCut.size() > 0) {
                            quester.getQuestData(quest).blocksCut.set(index, is);
                        }
                        index++;
                    }
                }
                if (questSec.contains("item-craft-amounts")) {
                    final List<Integer> craftAmounts = questSec.getIntegerList("item-craft-amounts");
                    for (int i = 0; i < craftAmounts.size(); i++) {
                        if (i < quester.getCurrentStage(quest).getItemsToCraft().size()) {
                            quester.getQuestData(quest).itemsCrafted.put(quester.getCurrentStage(quest)
                                    .getItemsToCraft().get(i), craftAmounts.get(i));
                        }
                    }
                }
                if (questSec.contains("item-smelt-amounts")) {
                    final List<Integer> smeltAmounts = questSec.getIntegerList("item-smelt-amounts");
                    for (int i = 0; i < smeltAmounts.size(); i++) {
                        if (i < quester.getCurrentStage(quest).getItemsToSmelt().size()) {
                            quester.getQuestData(quest).itemsSmelted.put(quester.getCurrentStage(quest)
                                    .getItemsToSmelt().get(i), smeltAmounts.get(i));
                        }
                    }
                }
                if (questSec.contains("item-enchant-amounts")) {
                    final List<Integer> enchantAmounts = questSec.getIntegerList("item-enchant-amounts");
                    for (int i = 0; i < enchantAmounts.size(); i++) {
                        if (i < quester.getCurrentStage(quest).getItemsToEnchant().size()) {
                            quester.getQuestData(quest).itemsEnchanted.put(quester.getCurrentStage(quest)
                                    .getItemsToEnchant().get(i), enchantAmounts.get(i));
                        }
                    }
                }
                if (questSec.contains("item-brew-amounts")) {
                    final List<Integer> brewAmounts = questSec.getIntegerList("item-brew-amounts");
                    for (int i = 0; i < brewAmounts.size(); i++) {
                        if (i < quester.getCurrentStage(quest).getItemsToBrew().size()) {
                            quester.getQuestData(quest).itemsBrewed.put(quester.getCurrentStage(quest)
                                    .getItemsToBrew().get(i), brewAmounts.get(i));
                        }
                    }
                }
                if (questSec.contains("item-consume-amounts")) {
                    final List<Integer> consumeAmounts = questSec.getIntegerList("item-consume-amounts");
                    for (int i = 0; i < consumeAmounts.size(); i++) {
                        if (i < quester.getCurrentStage(quest).getItemsToConsume().size()) {
                            quester.getQuestData(quest).itemsConsumed.put(quester.getCurrentStage(quest)
                                    .getItemsToConsume().get(i), consumeAmounts.get(i));
                        }
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
                if (questSec.contains("mobs-killed")) {
                    final LinkedList<EntityType> mobs = new LinkedList<EntityType>();
                    final List<Integer> amounts = questSec.getIntegerList("mobs-killed-amounts");
                    for (final String s : questSec.getStringList("mobs-killed")) {
                        final EntityType mob = MiscUtil.getProperMobType(s);
                        if (mob != null) {
                            mobs.add(mob);
                        }
                        quester.getQuestData(quest).mobsKilled.clear();
                        quester.getQuestData(quest).mobNumKilled.clear();
                        for (final EntityType e : mobs) {
                            quester.getQuestData(quest).mobsKilled.add(e);
                            quester.getQuestData(quest).mobNumKilled.add(amounts.get(mobs.indexOf(e)));
                        }
                        if (questSec.contains("mob-kill-locations")) {
                            final LinkedList<Location> locations = new LinkedList<Location>();
                            final List<Integer> radii = questSec.getIntegerList("mob-kill-location-radii");
                            for (final String loc : questSec.getStringList("mob-kill-locations")) {
                                if (ConfigUtil.getLocation(loc) != null) {
                                    locations.add(ConfigUtil.getLocation(loc));
                                }
                            }
                            quester.getQuestData(quest).locationsToKillWithin = locations;
                            quester.getQuestData(quest).radiiToKillWithin.clear();
                            for (final int i : radii) {
                                quester.getQuestData(quest).radiiToKillWithin.add(i);
                            }
                        }
                    }
                }
                if (questSec.contains("item-delivery-amounts")) {
                    final List<Integer> deliveryAmounts = questSec.getIntegerList("item-delivery-amounts");
                    int index = 0;
                    for (final int amt : deliveryAmounts) {
                        final ItemStack is = quester.getCurrentStage(quest).getItemsToDeliver().get(index);
                        final ItemStack temp = new ItemStack(is.getType(), amt, is.getDurability());
                        try {
                            temp.addEnchantments(is.getEnchantments());
                        } catch (final Exception e) {
                            plugin.getLogger().warning("Unable to add enchantment(s) " + is.getEnchantments().toString()
                                    + " to delivery item " + is.getType().name() + " x " + amt + " for quest " 
                                    + quest.getName());
                        }
                        temp.setItemMeta(is.getItemMeta());
                        if (quester.getQuestData(quest).itemsDelivered.size() > 0) {
                            quester.getQuestData(quest).itemsDelivered.set(index, temp);
                        }
                        index++;
                    }
                }
                if (questSec.contains("citizen-ids-to-talk-to")) {
                    final List<Integer> ids = questSec.getIntegerList("citizen-ids-to-talk-to");
                    final List<Boolean> has = questSec.getBooleanList("has-talked-to");
                    for (final int i : ids) {
                        quester.getQuestData(quest).citizensInteracted.put(i, has.get(ids.indexOf(i)));
                    }
                }
                if (questSec.contains("citizen-ids-killed")) {
                    final List<Integer> ids = questSec.getIntegerList("citizen-ids-killed");
                    final List<Integer> num = questSec.getIntegerList("citizen-amounts-killed");
                    quester.getQuestData(quest).citizensKilled.clear();
                    quester.getQuestData(quest).citizenNumKilled.clear();
                    for (final int i : ids) {
                        quester.getQuestData(quest).citizensKilled.add(i);
                        quester.getQuestData(quest).citizenNumKilled.add(num.get(ids.indexOf(i)));
                    }
                }
                if (questSec.contains("locations-to-reach")) {
                    final LinkedList<Location> locations = new LinkedList<Location>();
                    final List<Boolean> has = questSec.getBooleanList("has-reached-location");
                    while (has.size() < locations.size()) {
                        // TODO - Find proper cause of Github issues #646 and #825
                        plugin.getLogger().info("Added missing has-reached-location data for Quester " + uniqueId);
                        has.add(false);
                    }
                    final List<Integer> radii = questSec.getIntegerList("radii-to-reach-within");
                    for (final String loc : questSec.getStringList("locations-to-reach")) {
                        if (ConfigUtil.getLocation(loc) != null) {
                            locations.add(ConfigUtil.getLocation(loc));
                        }
                    }
                    quester.getQuestData(quest).locationsReached = locations;
                    quester.getQuestData(quest).hasReached.clear();
                    quester.getQuestData(quest).radiiToReachWithin.clear();
                    for (final boolean b : has) {
                        quester.getQuestData(quest).hasReached.add(b);
                    }
                    for (final int i : radii) {
                        quester.getQuestData(quest).radiiToReachWithin.add(i);
                    }
                }
                if (questSec.contains("mobs-to-tame")) {
                    final List<String> mobs = questSec.getStringList("mobs-to-tame");
                    final List<Integer> amounts = questSec.getIntegerList("mob-tame-amounts");
                    for (final String mob : mobs) {
                        quester.getQuestData(quest).mobsTamed.put(EntityType.valueOf(mob.toUpperCase()), amounts
                                .get(mobs.indexOf(mob)));
                    }
                }
                if (questSec.contains("sheep-to-shear")) {
                    final List<String> colors = questSec.getStringList("sheep-to-shear");
                    final List<Integer> amounts = questSec.getIntegerList("sheep-sheared");
                    for (final String color : colors) {
                        quester.getQuestData(quest).sheepSheared.put(MiscUtil.getProperDyeColor(color), amounts.get(colors
                                .indexOf(color)));
                    }
                }
                if (questSec.contains("passwords")) {
                    final List<String> passwords = questSec.getStringList("passwords");
                    final List<Boolean> said = questSec.getBooleanList("passwords-said");
                    for (int i = 0; i < passwords.size(); i++) {
                        quester.getQuestData(quest).passwordsSaid.put(passwords.get(i), said.get(i));
                    }
                }
                if (questSec.contains("custom-objectives")) {
                    final List<String> customObj = questSec.getStringList("custom-objectives");
                    final List<Integer> customObjCount = questSec.getIntegerList("custom-objective-counts");
                    for (int i = 0; i < customObj.size(); i++) {
                        quester.getQuestData(quest).customObjectiveCounts.put(customObj.get(i), customObjCount.get(i));
                    }
                }
                if (questSec.contains("stage-delay")) {
                    quester.getQuestData(quest).setDelayTimeLeft(questSec.getLong("stage-delay"));
                }
                if (quester.getCurrentStage(quest).getChatActions().isEmpty() == false) {
                    for (final String chatTrig : quester.getCurrentStage(quest).getChatActions().keySet()) {
                        quester.getQuestData(quest).actionFired.put(chatTrig, false);
                    }
                }
                if (questSec.contains("chat-triggers")) {
                    final List<String> chatTriggers = questSec.getStringList("chat-triggers");
                    for (final String s : chatTriggers) {
                        quester.getQuestData(quest).actionFired.put(s, true);
                    }
                }
                if (quester.getCurrentStage(quest).getCommandActions().isEmpty() == false) {
                    for (final String commandTrig : quester.getCurrentStage(quest).getCommandActions().keySet()) {
                        quester.getQuestData(quest).actionFired.put(commandTrig, false);
                    }
                }
                if (questSec.contains("command-triggers")) {
                    final List<String> commandTriggers = questSec.getStringList("command-triggers");
                    for (final String s : commandTriggers) {
                        quester.getQuestData(quest).actionFired.put(s, true);
                    }
                }
            }
        }
        return quester;
    }

    @Override
    public void saveQuesterData(final Quester quester) throws Exception {
        final FileConfiguration data = quester.getBaseData();
        try {
            data.save(new File(directoryPath + File.separator + quester.getUUID() + ".yml"));
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteQuesterData(final UUID uniqueId) throws Exception {
        final File f = new File(directoryPath + File.separator + uniqueId + ".yml");
        f.delete();
    }

    @Override
    public String getQuesterLastKnownName(final UUID uniqueId) throws Exception {
        final FileConfiguration data = new YamlConfiguration();
        Quester quester = plugin.getQuester(uniqueId);
        if (quester != null) {
            quester.hardClear();
        } else {
            quester = new Quester(plugin, uniqueId);
        }
        return data.getString("lastKnownName");
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
