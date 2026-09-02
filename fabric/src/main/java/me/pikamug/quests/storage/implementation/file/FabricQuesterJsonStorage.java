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
import com.google.gson.JsonObject;
import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.player.FabricQuester;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.storage.implementation.QuesterStorageImpl;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

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

    @Override
    public Quester loadQuester(UUID uniqueId) throws Exception {
        final Path file = dataDir.resolve(uniqueId.toString() + ".json");
        if (!Files.exists(file)) {
            return new FabricQuester(plugin, uniqueId);
        }
        try (Reader reader = Files.newBufferedReader(file)) {
            final JsonObject json = gson.fromJson(reader, JsonObject.class);
            if (json == null) return new FabricQuester(plugin, uniqueId);
            final FabricQuester quester = new FabricQuester(plugin, uniqueId);
            if (json.has("lastKnownName")) {
                quester.setLastKnownName(json.get("lastKnownName").getAsString());
            }
            if (json.has("questPoints")) {
                quester.setQuestPoints(json.get("questPoints").getAsInt());
            }
            return quester;
        }
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
