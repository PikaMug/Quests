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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.player.FabricQuester;
import me.pikamug.quests.player.FabricQuestProgress;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.player.QuestProgress;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.storage.implementation.QuesterStorageImpl;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FabricQuesterJsonStorage implements QuesterStorageImpl {

    private final FabricQuestsPlugin plugin;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private Path dataDir;

    public FabricQuesterJsonStorage(FabricQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override public FabricQuestsPlugin getPlugin() { return plugin; }
    @Override public String getImplementationName() { return "JSON"; }

    @Override
    public void init() throws Exception {
        dataDir = plugin.getPluginDataFolder().toPath().resolve("data");
        if (!Files.exists(dataDir)) {
            Files.createDirectories(dataDir);
        }
    }

    @Override
    public void close() {
        // Nothing to close for file storage
    }

    private Quest findQuestById(final String id) {
        for (final Quest quest : plugin.getLoadedQuests()) {
            if (quest.getId().equals(id)) {
                return quest;
            }
        }
        return null;
    }

    @Override
    public Quester loadQuester(UUID uniqueId) throws Exception {
        final FabricQuester quester = new FabricQuester(plugin, uniqueId);
        final Path file = dataDir.resolve(uniqueId.toString() + ".json");
        if (!Files.exists(file)) {
            return quester;
        }
        try (Reader reader = Files.newBufferedReader(file)) {
            final JsonObject json = gson.fromJson(reader, JsonObject.class);
            if (json == null) return quester;
            if (json.has("lastKnownName")) {
                quester.setLastKnownName(json.get("lastKnownName").getAsString());
            }
            if (json.has("questPoints")) {
                quester.setQuestPoints(json.get("questPoints").getAsInt());
            }
            final ConcurrentHashMap<Quest, Integer> current = new ConcurrentHashMap<>();
            if (json.has("currentQuests")) {
                final JsonObject obj = json.getAsJsonObject("currentQuests");
                for (final Map.Entry<String, JsonElement> e : obj.entrySet()) {
                    final Quest quest = findQuestById(e.getKey());
                    if (quest != null) {
                        current.put(quest, e.getValue().getAsInt());
                    } else {
                        plugin.getPluginLogger().warn("Ignoring current quest '{}' not loaded for {}",
                                e.getKey(), uniqueId);
                    }
                }
            }
            quester.setCurrentQuests(current);
            final Collection<Quest> completed = new ArrayList<>();
            if (json.has("completedQuests")) {
                final JsonArray arr = json.getAsJsonArray("completedQuests");
                for (final JsonElement e : arr) {
                    final Quest quest = findQuestById(e.getAsString());
                    if (quest != null) {
                        completed.add(quest);
                    }
                }
            }
            quester.setCompletedQuests(completed);
            final ConcurrentHashMap<Quest, Long> completedTimes = new ConcurrentHashMap<>();
            if (json.has("completedTimes")) {
                final JsonObject obj = json.getAsJsonObject("completedTimes");
                for (final Map.Entry<String, JsonElement> e : obj.entrySet()) {
                    final Quest quest = findQuestById(e.getKey());
                    if (quest != null) {
                        completedTimes.put(quest, e.getValue().getAsLong());
                    }
                }
            }
            quester.setCompletedTimes(completedTimes);
            final ConcurrentHashMap<Quest, Integer> amountsCompleted = new ConcurrentHashMap<>();
            if (json.has("amountsCompleted")) {
                final JsonObject obj = json.getAsJsonObject("amountsCompleted");
                for (final Map.Entry<String, JsonElement> e : obj.entrySet()) {
                    final Quest quest = findQuestById(e.getKey());
                    if (quest != null) {
                        amountsCompleted.put(quest, e.getValue().getAsInt());
                    }
                }
            }
            quester.setAmountsCompleted(amountsCompleted);
            if (json.has("questProgress")) {
                final JsonObject obj = json.getAsJsonObject("questProgress");
                for (final Map.Entry<String, JsonElement> e : obj.entrySet()) {
                    final Quest quest = findQuestById(e.getKey());
                    if (quest == null || !e.getValue().isJsonObject()) {
                        continue;
                    }
                    quester.setQuestProgress(quest, parseProgress(e.getValue().getAsJsonObject()));
                }
            }
            quester.setHasData(true);
            return quester;
        }
    }

    private JsonObject serializeProgress(QuestProgress progress) {
        final JsonObject json = new JsonObject();
        json.add("blocksBroken", toArray(progress.getBlocksBroken()));
        json.add("blocksDamaged", toArray(progress.getBlocksDamaged()));
        json.add("blocksPlaced", toArray(progress.getBlocksPlaced()));
        json.add("blocksUsed", toArray(progress.getBlocksUsed()));
        json.add("itemsCrafted", toArray(progress.getItemsCrafted()));
        json.add("itemsSmelted", toArray(progress.getItemsSmelted()));
        json.add("itemsEnchanted", toArray(progress.getItemsEnchanted()));
        json.add("itemsBrewed", toArray(progress.getItemsBrewed()));
        json.add("itemsConsumed", toArray(progress.getItemsConsumed()));
        json.add("itemsDelivered", toArray(progress.getItemsDelivered()));
        json.add("npcsInteracted", toBooleanArray(progress.getNpcsInteracted()));
        json.add("npcsNumKilled", toArray(progress.getNpcsNumKilled()));
        json.add("mobNumKilled", toArray(progress.getMobNumKilled()));
        json.add("mobsTamed", toArray(progress.getMobsTamed()));
        json.addProperty("fishCaught", progress.getFishCaught());
        json.addProperty("cowsMilked", progress.getCowsMilked());
        json.add("sheepSheared", toArray(progress.getSheepSheared()));
        json.addProperty("playersKilled", progress.getPlayersKilled());
        json.add("locationsReached", toBooleanArray(progress.getLocationsReached()));
        json.add("passwordsSaid", toBooleanArray(progress.getPasswordsSaid()));
        json.add("customObjectiveCounts", toArray(progress.getCustomObjectiveCounts()));
        json.addProperty("delayStartTime", progress.getDelayStartTime());
        json.addProperty("delayTimeLeft", progress.getDelayTimeLeft());
        json.addProperty("doJournalUpdate", progress.canDoJournalUpdate());
        return json;
    }

    private QuestProgress parseProgress(final JsonObject json) {
        final FabricQuestProgress progress = new FabricQuestProgress();
        progress.setBlocksBroken(fromArray(json.getAsJsonArray("blocksBroken")));
        progress.setBlocksDamaged(fromArray(json.getAsJsonArray("blocksDamaged")));
        progress.setBlocksPlaced(fromArray(json.getAsJsonArray("blocksPlaced")));
        progress.setBlocksUsed(fromArray(json.getAsJsonArray("blocksUsed")));
        progress.setItemsCrafted(fromArray(json.getAsJsonArray("itemsCrafted")));
        progress.setItemsSmelted(fromArray(json.getAsJsonArray("itemsSmelted")));
        progress.setItemsEnchanted(fromArray(json.getAsJsonArray("itemsEnchanted")));
        progress.setItemsBrewed(fromArray(json.getAsJsonArray("itemsBrewed")));
        progress.setItemsConsumed(fromArray(json.getAsJsonArray("itemsConsumed")));
        progress.setItemsDelivered(fromArray(json.getAsJsonArray("itemsDelivered")));
        progress.setNpcsInteracted(fromBooleanArray(json.getAsJsonArray("npcsInteracted")));
        progress.setNpcsNumKilled(fromArray(json.getAsJsonArray("npcsNumKilled")));
        progress.setMobNumKilled(fromArray(json.getAsJsonArray("mobNumKilled")));
        progress.setMobsTamed(fromArray(json.getAsJsonArray("mobsTamed")));
        if (json.has("fishCaught")) progress.setFishCaught(json.get("fishCaught").getAsInt());
        if (json.has("cowsMilked")) progress.setCowsMilked(json.get("cowsMilked").getAsInt());
        progress.setSheepSheared(fromArray(json.getAsJsonArray("sheepSheared")));
        if (json.has("playersKilled")) progress.setPlayersKilled(json.get("playersKilled").getAsInt());
        progress.setLocationsReached(fromBooleanArray(json.getAsJsonArray("locationsReached")));
        progress.setPasswordsSaid(fromBooleanArray(json.getAsJsonArray("passwordsSaid")));
        progress.setCustomObjectiveCounts(fromArray(json.getAsJsonArray("customObjectiveCounts")));
        if (json.has("delayStartTime")) progress.setDelayStartTime(json.get("delayStartTime").getAsLong());
        if (json.has("delayTimeLeft")) progress.setDelayTimeLeft(json.get("delayTimeLeft").getAsLong());
        if (json.has("doJournalUpdate")) progress.setDoJournalUpdate(json.get("doJournalUpdate").getAsBoolean());
        return progress;
    }

    private static JsonArray toArray(final LinkedList<Integer> list) {
        final JsonArray arr = new JsonArray();
        if (list != null) {
            list.forEach(arr::add);
        }
        return arr;
    }

    private static JsonArray toBooleanArray(final LinkedList<Boolean> list) {
        final JsonArray arr = new JsonArray();
        if (list != null) {
            list.forEach(arr::add);
        }
        return arr;
    }

    private static LinkedList<Integer> fromArray(final JsonArray arr) {
        final LinkedList<Integer> list = new LinkedList<>();
        if (arr != null) {
            arr.forEach(e -> {
                if (e.isJsonPrimitive()) {
                    list.add(e.getAsInt());
                }
            });
        }
        return list;
    }

    private static LinkedList<Boolean> fromBooleanArray(final JsonArray arr) {
        final LinkedList<Boolean> list = new LinkedList<>();
        if (arr != null) {
            arr.forEach(e -> {
                if (e.isJsonPrimitive()) {
                    list.add(e.getAsBoolean());
                }
            });
        }
        return list;
    }

    @Override
    public void saveQuester(Quester quester) throws Exception {
        if (quester == null) return;
        final Path file = dataDir.resolve(quester.getUUID().toString() + ".json");
        final JsonObject json = new JsonObject();
        json.addProperty("uuid", quester.getUUID().toString());
        if (quester.getLastKnownName() != null) {
            json.addProperty("lastKnownName", quester.getLastKnownName());
        }
        json.addProperty("questPoints", quester.getQuestPoints());
        final JsonObject current = new JsonObject();
        for (final Map.Entry<Quest, Integer> e : quester.getCurrentQuests().entrySet()) {
            if (e.getKey() != null) {
                current.addProperty(e.getKey().getId(), e.getValue());
            }
        }
        json.add("currentQuests", current);
        final JsonArray completed = new JsonArray();
        for (final Quest quest : quester.getCompletedQuests()) {
            if (quest != null) {
                completed.add(quest.getId());
            }
        }
        json.add("completedQuests", completed);
        final JsonObject completedTimes = new JsonObject();
        for (final Map.Entry<Quest, Long> e : quester.getCompletedTimes().entrySet()) {
            if (e.getKey() != null) {
                completedTimes.addProperty(e.getKey().getId(), e.getValue());
            }
        }
        json.add("completedTimes", completedTimes);
        final JsonObject amountsCompleted = new JsonObject();
        for (final Map.Entry<Quest, Integer> e : quester.getAmountsCompleted().entrySet()) {
            if (e.getKey() != null) {
                amountsCompleted.addProperty(e.getKey().getId(), e.getValue());
            }
        }
        json.add("amountsCompleted", amountsCompleted);
        final JsonObject progress = new JsonObject();
        if (quester instanceof FabricQuester fabricQuester) {
            for (final Map.Entry<Quest, QuestProgress> e : fabricQuester.getProgressData().entrySet()) {
                if (e.getKey() != null && e.getValue() != null) {
                    progress.add(e.getKey().getId(), serializeProgress(e.getValue()));
                }
            }
        }
        json.add("questProgress", progress);
        try (Writer writer = Files.newBufferedWriter(file)) {
            gson.toJson(json, writer);
        }
    }

    @Override
    public void deleteQuester(UUID uniqueId) throws Exception {
        final Path file = dataDir.resolve(uniqueId.toString() + ".json");
        Files.deleteIfExists(file);
    }

    @Override
    public String getQuesterLastKnownName(UUID uniqueId) throws Exception {
        final Path file = dataDir.resolve(uniqueId.toString() + ".json");
        if (!Files.exists(file)) return null;
        try (Reader reader = Files.newBufferedReader(file)) {
            final JsonObject json = gson.fromJson(reader, JsonObject.class);
            return json != null && json.has("lastKnownName") ? json.get("lastKnownName").getAsString() : null;
        }
    }

    @Override
    public Collection<UUID> getSavedUniqueIds() throws Exception {
        final Set<UUID> uuids = new HashSet<>();
        if (!Files.exists(dataDir)) return uuids;
        try (var stream = Files.list(dataDir)) {
            stream.filter(p -> p.toString().endsWith(".json")).forEach(p -> {
                try {
                    uuids.add(UUID.fromString(p.getFileName().toString().replace(".json", "")));
                } catch (final IllegalArgumentException ignored) {}
            });
        }
        return uuids;
    }
}
