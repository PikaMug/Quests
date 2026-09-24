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

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FabricQuestFactory implements QuestFactory {

    private final FabricQuestsPlugin plugin;
    private final Set<UUID> selectingNpcs = ConcurrentHashMap.newKeySet();
    private final List<String> namesOfQuestsBeingEdited = Collections.synchronizedList(new ArrayList<>());
    private final ConcurrentHashMap<UUID, net.minecraft.world.level.block.Block> selectedKillLocations
            = new ConcurrentHashMap<>();

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
        final Quest removed = plugin.getLoadedQuests().stream()
                .filter(q -> questName.equals(q.getId()))
                .findFirst().orElse(null);
        plugin.getLoadedQuests().remove(removed);
        // Remove from quests.json index
        try {
            final Path index = plugin.getPluginDataFolder().toPath().resolve("storage").resolve("quests.json");
            if (Files.exists(index)) {
                final com.google.gson.Gson gson = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
                final com.google.gson.JsonObject root;
                try (Reader reader = Files.newBufferedReader(index)) {
                    final var parsed = com.google.gson.JsonParser.parseReader(reader);
                    root = parsed.isJsonObject() ? parsed.getAsJsonObject() : new com.google.gson.JsonObject();
                }
                if (root.has(questName)) {
                    root.remove(questName);
                    try (Writer writer = Files.newBufferedWriter(index)) {
                        gson.toJson(root, writer);
                    }
                }
            }
        } catch (final Exception e) {
            FabricQuestsPlugin.LOGGER.error("Failed to delete quest from index: {}", questName, e);
        }
        // Remove from all questers
        for (final me.pikamug.quests.player.Quester q : plugin.getOfflineQuesters()) {
            q.hardRemove(removed);
        }
        namesOfQuestsBeingEdited.remove(questName);
        selectingNpcs.remove(uuid);
        FabricQuestsPlugin.LOGGER.info("Deleted quest: {}", questName);
    }

    public void clearData(final UUID uuid) {
        SessionData.clear(uuid);
    }

    public ConcurrentHashMap<UUID, net.minecraft.world.level.block.Block> getSelectedKillLocations() {
        return selectedKillLocations;
    }

    public void setSelectedKillLocations(final ConcurrentHashMap<UUID, net.minecraft.world.level.block.Block> v) {
        selectedKillLocations.clear();
        selectedKillLocations.putAll(v);
    }
}
