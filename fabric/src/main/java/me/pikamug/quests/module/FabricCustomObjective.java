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

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.events.FabricQuestsEventBus;
import me.pikamug.quests.events.quester.FabricQuesterPostUpdateObjectiveEvent;
import me.pikamug.quests.events.quester.FabricQuesterPreUpdateObjectiveEvent;
import me.pikamug.quests.player.FabricQuestProgress;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.Stage;

import java.io.File;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

public class FabricCustomObjective implements CustomObjective {

    private final FabricQuestsPlugin plugin = FabricQuestsPlugin.getInstance();
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

    @Override
    public boolean canShowCount() {
        return showCount;
    }

    @Override
    public void setShowCount(final boolean showCount) {
        this.showCount = showCount;
    }

    @Override
    public Map<String, Object> getDataForPlayer(final UUID uuid, final CustomObjective customObj, final Quest quest) {
        final FabricCustomObjective fabricCustomObj = (FabricCustomObjective) customObj;
        final Quester quester = plugin.getQuester(uuid);
        if (quester != null) {
            final Stage currentStage = quester.getCurrentStage(quest);
            if (currentStage == null) {
                return null;
            }
            CustomObjective found = null;
            for (final CustomObjective co : currentStage.getCustomObjectives()) {
                if (co.getName().equals(fabricCustomObj.getName())) {
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

    @Override
    public void incrementObjective(final UUID uuid, final CustomObjective customObj, final Quest quest,
                                   final int count) {
        final FabricCustomObjective fabricCustomObj = (FabricCustomObjective) customObj;
        final Quester quester = plugin.getQuester(uuid);
        if (quester != null) {
            if (quester.hasCustomObjective(quest, fabricCustomObj.getName())) {
                if (!quester.meetsCondition(quest, true)) {
                    return;
                }
                int index = -1;
                final FabricQuestProgress questProgress = (FabricQuestProgress) quester.getQuestProgressOrDefault(quest);
                final LinkedList<Integer> customObjCounts = questProgress.getCustomObjectiveCounts();
                for (final CustomObjective co : quester.getCurrentStage(quest).getCustomObjectives()) {
                    index++;
                    if (co.getName().equals(fabricCustomObj.getName())) {
                        if (index >= customObjCounts.size()) {
                            FabricQuestsPlugin.LOGGER.error("Index was larger than count for {} by {}",
                                    fabricCustomObj.getName(), fabricCustomObj.getAuthor());
                            continue;
                        }
                        final int old = customObjCounts.get(index);
                        customObjCounts.set(index, old + count);
                        break;
                    }
                }
                if (index > -1) {
                    final int goal = quester.getCurrentStage(quest).getCustomObjectiveCounts().get(index);
                    FabricQuestsEventBus.fire(new FabricQuesterPreUpdateObjectiveEvent(plugin, quester, quest,
                            customObjCounts.get(index), goal));
                    if (customObjCounts.get(index) >= goal) {
                        // Multiplayer
                        final int finalIndex = index;
                        quester.dispatchMultiplayerObjectives(quest, quester.getCurrentStage(quest), (final Quester q) -> {
                            final FabricQuestProgress qProgress
                                    = (FabricQuestProgress) q.getQuestProgressOrDefault(quest);
                            final int old = qProgress.getCustomObjectiveCounts().get(finalIndex);
                            qProgress.getCustomObjectiveCounts().set(finalIndex, old + count);
                            return null;
                        });
                    }
                    FabricQuestsEventBus.fire(new FabricQuesterPostUpdateObjectiveEvent(plugin, quester, quest,
                            customObjCounts.get(index), goal));
                }
            }
        }
    }
}