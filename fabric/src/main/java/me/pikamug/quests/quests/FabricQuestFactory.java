/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.quests;

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.util.SessionData;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FabricQuestFactory implements QuestFactory {

    private final FabricQuestsPlugin plugin;
    private final Set<UUID> selectingNpcs = ConcurrentHashMap.newKeySet();
    private final List<String> namesOfQuestsBeingEdited = Collections.synchronizedList(new ArrayList<>());

    public FabricQuestFactory(FabricQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override public Set<UUID> getSelectingNpcs() { return selectingNpcs; }
    @Override public void setSelectingNpcs(Collection<UUID> v) { selectingNpcs.clear(); selectingNpcs.addAll(v); }
    @Override public List<String> getNamesOfQuestsBeingEdited() { return namesOfQuestsBeingEdited; }
    @Override public void setNamesOfQuestsBeingEdited(Collection<String> v) { namesOfQuestsBeingEdited.clear(); namesOfQuestsBeingEdited.addAll(v); }

    @Override
    public void returnToMenu(UUID uuid) {
        selectingNpcs.remove(uuid);
    }

    @Override
    public void loadQuest(UUID uuid, Quest q) {
        if (q != null) {
            plugin.getLoadedQuests().add(q);
        }
    }

    @Override
    public void deleteQuest(UUID uuid) {
        final String questName = namesOfQuestsBeingEdited.isEmpty() ? null : namesOfQuestsBeingEdited.get(0);
        if (questName == null) return;
        // Remove from loaded quests
        plugin.getLoadedQuests().removeIf(q -> questName.equals(q.getId()));
        // Remove from file system
        try {
            final java.nio.file.Path questFile = plugin.getPluginDataFolder().toPath()
                    .resolve("storage").resolve(questName + ".json");
            if (java.nio.file.Files.exists(questFile)) {
                java.nio.file.Files.delete(questFile);
            }
        } catch (final Exception e) {
            FabricQuestsPlugin.LOGGER.error("Failed to delete quest file: {}", questName, e);
        }
        // Remove from all questers
        for (final me.pikamug.quests.player.Quester q : plugin.getOfflineQuesters()) {
            final Quest toRemove = plugin.getLoadedQuests().stream()
                    .filter(quest -> questName.equals(quest.getId()))
                    .findFirst().orElse(null);
            q.hardRemove(toRemove);
        }
        namesOfQuestsBeingEdited.remove(questName);
        selectingNpcs.remove(uuid);
        FabricQuestsPlugin.LOGGER.info("Deleted quest: {}", questName);
    }

    public void clearData(final UUID uuid) {
        SessionData.clear(uuid);
    }
}
