/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.module;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.enums.ObjectiveType;
import me.pikamug.quests.events.quester.BukkitQuesterPostUpdateObjectiveEvent;
import me.pikamug.quests.events.quester.BukkitQuesterPreUpdateObjectiveEvent;
import me.pikamug.quests.player.BukkitQuestProgress;
import me.pikamug.quests.player.BukkitQuester;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.components.BukkitObjective;
import me.pikamug.quests.quests.BukkitQuest;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.Stage;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

public class BukkitCustomObjective implements CustomObjective, Listener {

    private final BukkitQuestsPlugin plugin = BukkitQuestsPlugin.getPlugin(BukkitQuestsPlugin.class);
    private String name = null;
    private String author = null;
    private String display = "Progress: %count%";
    private Entry<String, Short> item = new AbstractMap.SimpleEntry<>("BOOK", (short) 0);
    private final LinkedList<Entry<String, Object>> data = new LinkedList<>();
    private final Map<String, String> descriptions = new HashMap<>();
    private String countPrompt = "Enter number";
    private boolean showCount = true;
    private int count = 1;

    @Override
    public String getModuleName() {
        return new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getName()
                .replace(".jar", "");
    }

    @Override
    public Entry<String, Short> getModuleItem() {
        return new AbstractMap.SimpleEntry<>("IRON_INGOT", (short) 0);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getAuthor() {
        return author;
    }

    @Override
    public void setAuthor(final String author) {
        this.author = author;
    }

    @Override
    public String getDisplay() {
        return display;
    }

    @Override
    public void setDisplay(final String display) {
        this.display = display;
    }

    @Override
    public Entry<String, Short> getItem() {
        return item;
    }

    @Override
    public void setItem(final String type, final short durability) {
        this.item = new AbstractMap.SimpleEntry<>(type, durability);
    }

    @Override
    public LinkedList<Entry<String, Object>> getData() {
        return data;
    }

    /**
     * Add a new prompt<p>
     *
     * Note that the "defaultValue" Object will be cast to a String internally
     *
     * @param title Prompt name
     * @param description Description of expected input
     * @param defaultValue Value to be used if input is not received
     */
    @Override
    public void addStringPrompt(final String title, final String description, final Object defaultValue) {
        final Entry<String, Object> prompt = new AbstractMap.SimpleEntry<>(title, defaultValue);
        data.add(prompt);
        descriptions.put(title, description);
    }

    @Override
    public Map<String, String> getDescriptions() {
        return descriptions;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public void setCount(final int count) {
        this.count = count;
    }

    @Override
    public String getCountPrompt() {
        return countPrompt;
    }

    @Override
    public void setCountPrompt(final String countPrompt) {
        this.countPrompt = countPrompt;
    }

    /**
     * Check whether to let user set required amount for objective
     */
    @Override
    public boolean canShowCount() {
        return showCount;
    }

    /**
     * Set whether to let user set required amount for objective
     *
     * @param showCount Whether to show the count
     */
    @Override
    public void setShowCount(final boolean showCount) {
        this.showCount = showCount;
    }

    /**
     * Get custom objective data for applicable player
     *
     * @param uuid UUID of player attempting this objective
     * @param customObj The objective being attempted
     * @param quest Current me.pikamug.quests.Quest which includes this objective
     * @return data Map of custom objective data
     */
    public Map<String, Object> getDataForPlayer(final UUID uuid, final CustomObjective customObj,
                                                final Quest quest) {
        final BukkitCustomObjective bukkitCustomObj = (BukkitCustomObjective) customObj;
        final BukkitQuest bukkitQuest = (BukkitQuest) quest;
        final Quester quester = plugin.getQuester(uuid);
        if (quester != null) {
            final Stage currentStage = quester.getCurrentStage(bukkitQuest);
            if (currentStage == null) {
                return null;
            }
            CustomObjective found = null;
            for (final CustomObjective co : currentStage.getCustomObjectives()) {
                if (co.getName().equals(bukkitCustomObj.getName())) {
                    found = co;
                    break;
                }
            }
            if (found != null) {
                final Map<String, Object> m = new HashMap<>();
                for (final Entry<String, Object> dataMap : found.getData()) {
                    for (final Entry<String, Object> e : currentStage.getCustomObjectiveData()) {
                        if (e.getKey().equals(dataMap.getKey())) {
                            m.put(e.getKey(), e.getValue());
                        }
                    }
                }
                if (!m.isEmpty()) {
                    return m;
                }
            }
        }
        return null;
    }

    /**
     * Increment objective count for applicable player
     *
     * @param uuid UUID of player attempting this objective
     * @param customObj The objective being attempted
     * @param quest Current me.pikamug.quests.Quest which includes this objective
     * @param count Amount to increase objective count by
     */
    public void incrementObjective(final UUID uuid, final CustomObjective customObj, final Quest quest,
                                   final int count) {
        final BukkitCustomObjective bukkitCustomObj = (BukkitCustomObjective) customObj;
        final BukkitQuest bukkitQuest = (BukkitQuest) quest;
        final BukkitQuester quester = plugin.getQuester(uuid);
        if (quester != null) {
            if (quester.hasCustomObjective(bukkitQuest, bukkitCustomObj.getName())) {
                if (!quester.meetsCondition(bukkitQuest, true)) {
                    return;
                }
                int index = -1;
                final BukkitQuestProgress bukkitQuestData = (BukkitQuestProgress) quester.getQuestDataOrDefault(bukkitQuest);
                final LinkedList<Integer> customObjCounts = bukkitQuestData.customObjectiveCounts;
                for (final CustomObjective co : quester.getCurrentStage(bukkitQuest).getCustomObjectives()) {
                    index++;
                    if (co.getName().equals(this.getName())) {
                        if (index >= customObjCounts.size()) {
                            plugin.getLogger().severe("Index was larger than count for " + bukkitCustomObj.getName() + " by "
                                    + bukkitCustomObj.getAuthor());
                            continue;
                        }
                        final int old = customObjCounts.get(index);
                        bukkitQuestData.customObjectiveCounts.set(index, old + count);
                        break;
                    }
                }
                if (index > -1) {
                    final int progress = customObjCounts.get(index);
                    final int goal = quester.getCurrentStage(bukkitQuest).getCustomObjectiveCounts().get(index);

                    final ObjectiveType type = ObjectiveType.CUSTOM;
                    final BukkitQuesterPreUpdateObjectiveEvent preEvent
                            = new BukkitQuesterPreUpdateObjectiveEvent(quester, bukkitQuest, new BukkitObjective(type, null, progress, goal));
                    plugin.getServer().getPluginManager().callEvent(preEvent);

                    if (progress >= goal) {
                        quester.finishObjective(bukkitQuest, new BukkitObjective(type, null, new ItemStack(Material.AIR, 1),
                                new ItemStack(Material.AIR, goal)), null, null, null, null, null, null, bukkitCustomObj);

                        // Multiplayer
                        final int finalIndex = index;
                        quester.dispatchMultiplayerObjectives(bukkitQuest, quester.getCurrentStage(bukkitQuest), (final Quester q) -> {
                            final BukkitQuestProgress qBukkitQuestData = (BukkitQuestProgress) q.getQuestDataOrDefault(bukkitQuest);
                            final int old = qBukkitQuestData.customObjectiveCounts.get(finalIndex);
                            qBukkitQuestData.customObjectiveCounts.set(finalIndex, old + count);
                            q.finishObjective(bukkitQuest, new BukkitObjective(type, null, new ItemStack(Material.AIR, 1),
                                    new ItemStack(Material.AIR, goal)), null, null, null, null, null, null, bukkitCustomObj);
                            return null;
                        });
                    }

                    final BukkitQuesterPostUpdateObjectiveEvent postEvent
                            = new BukkitQuesterPostUpdateObjectiveEvent(quester, bukkitQuest, new BukkitObjective(type, null, progress, goal));
                    plugin.getServer().getPluginManager().callEvent(postEvent);
                }
            }
        }
    }
}
