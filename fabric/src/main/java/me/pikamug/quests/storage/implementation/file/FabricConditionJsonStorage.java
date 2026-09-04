package me.pikamug.quests.storage.implementation.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.conditions.Condition;
import me.pikamug.quests.exceptions.ConditionFormatException;
import me.pikamug.quests.conditions.FabricCondition;
import me.pikamug.quests.storage.implementation.ConditionStorageImpl;
import me.pikamug.quests.util.FabricItemUtil;
import net.minecraft.world.item.ItemStack;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.UUID;

public class FabricConditionJsonStorage implements ConditionStorageImpl {

    private final FabricQuestsPlugin plugin;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private Path storageDir;

    public FabricConditionJsonStorage(FabricQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override public FabricQuestsPlugin getPlugin() { return plugin; }
    @Override public String getImplementationName() { return "JSON"; }

    @Override
    public void init() throws Exception {
        storageDir = plugin.getPluginDataFolder().toPath().resolve("storage");
        if (!Files.exists(storageDir)) {
            Files.createDirectories(storageDir);
        }
        loadConditions();
    }

    @Override
    public void close() {}

    @Override
    public Condition loadCondition(String name) throws ConditionFormatException {
        final Path file = storageDir.resolve(name + ".json");
        if (!Files.exists(file)) return null;
        try (Reader reader = Files.newBufferedReader(file)) {
            final JsonObject json = gson.fromJson(reader, JsonObject.class);
            if (json == null) return null;
            final FabricCondition condition = new FabricCondition();
            condition.setName(name);
            if (json.has("fail-quest")) condition.setFailQuest(json.get("fail-quest").getAsBoolean());
            if (json.has("ride-entity")) {
                final JsonArray arr = json.getAsJsonArray("ride-entity");
                final LinkedList<String> entities = new LinkedList<>();
                arr.forEach(e -> entities.add(e.getAsString()));
                condition.setEntitiesWhileRiding(entities);
            }
            if (json.has("ride-npc-uuid")) {
                final JsonArray arr = json.getAsJsonArray("ride-npc-uuid");
                final LinkedList<UUID> npcs = new LinkedList<>();
                arr.forEach(e -> {
                    try { npcs.add(UUID.fromString(e.getAsString())); } catch (final Exception ignored) {}
                });
                condition.setNpcsWhileRiding(npcs);
            }
            if (json.has("permission")) {
                final JsonArray arr = json.getAsJsonArray("permission");
                final LinkedList<String> perms = new LinkedList<>();
                arr.forEach(e -> perms.add(e.getAsString()));
                condition.setPermissions(perms);
            }
            if (json.has("stay-within-world")) {
                final JsonArray arr = json.getAsJsonArray("stay-within-world");
                final LinkedList<String> worlds = new LinkedList<>();
                arr.forEach(e -> worlds.add(e.getAsString()));
                condition.setWorldsWhileStayingWithin(worlds);
            }
            if (json.has("stay-within-ticks")) {
                final JsonObject ticks = json.getAsJsonObject("stay-within-ticks");
                if (ticks.has("start")) condition.setTickStartWhileStayingWithin(ticks.get("start").getAsInt());
                if (ticks.has("end")) condition.setTickEndWhileStayingWithin(ticks.get("end").getAsInt());
            }
            if (json.has("stay-within-biome")) {
                final JsonArray arr = json.getAsJsonArray("stay-within-biome");
                final LinkedList<String> biomes = new LinkedList<>();
                arr.forEach(e -> biomes.add(e.getAsString()));
                condition.setBiomesWhileStayingWithin(biomes);
            }
            if (json.has("stay-within-region")) {
                final JsonArray arr = json.getAsJsonArray("stay-within-region");
                final LinkedList<String> regions = new LinkedList<>();
                arr.forEach(e -> regions.add(e.getAsString()));
                condition.setRegionsWhileStayingWithin(regions);
            }
            if (json.has("check-placeholder-id")) {
                final JsonArray arr = json.getAsJsonArray("check-placeholder-id");
                final LinkedList<String> ids = new LinkedList<>();
                arr.forEach(e -> ids.add(e.getAsString()));
                condition.setPlaceholdersCheckIdentifier(ids);
            }
            if (json.has("check-placeholder-value")) {
                final JsonArray arr = json.getAsJsonArray("check-placeholder-value");
                final LinkedList<String> vals = new LinkedList<>();
                arr.forEach(e -> vals.add(e.getAsString()));
                condition.setPlaceholdersCheckValue(vals);
            }
            if (json.has("hold-main-hand")) {
                condition.setItemsWhileHoldingMainHand(parseItemList(json, "hold-main-hand"));
                if (condition.getItemsWhileHoldingMainHand().isEmpty()) {
                    throw new ConditionFormatException("'hold-main-hand' is not a list of items", name);
                }
            }
            if (json.has("wear")) {
                condition.setItemsWhileWearing(parseItemList(json, "wear"));
                if (condition.getItemsWhileWearing().isEmpty()) {
                    throw new ConditionFormatException("'wear' is not a list of items", name);
                }
            }
            return condition;
        } catch (final Exception e) {
            throw new ConditionFormatException("Failed to load condition: " + name
                    + (e.getMessage() != null ? " - " + e.getMessage() : ""), name);
        }
    }

    private LinkedList<ItemStack> parseItemList(JsonObject json, String key) {
        final LinkedList<ItemStack> list = new LinkedList<>();
        final JsonArray arr = json.getAsJsonArray(key);
        for (int i = 0; i < arr.size(); i++) {
            if (arr.get(i).isJsonObject()) {
                list.add(FabricItemUtil.deserializeFromJson(arr.get(i).getAsJsonObject()));
            } else {
                list.add(FabricItemUtil.deserialize(arr.get(i).getAsString()));
            }
        }
        return list;
    }

    private void loadConditions() {
        if (!Files.exists(storageDir)) return;
        final Path condFile = storageDir.resolve("conditions.json");
        if (!Files.exists(condFile)) return;
        try (Reader reader = Files.newBufferedReader(condFile)) {
            final JsonObject json = gson.fromJson(reader, JsonObject.class);
            if (json == null) return;
            for (final String name : json.keySet()) {
                if (json.get(name).isJsonObject()) {
                    final Path individualFile = storageDir.resolve(name + ".json");
                    if (!Files.exists(individualFile)) {
                        Files.write(individualFile, gson.toJson(json.getAsJsonObject(name)).getBytes());
                    }
                    if (plugin.getCondition(name) == null) {
                        final Condition condition = loadCondition(name);
                        if (condition != null) {
                            plugin.getLoadedConditions().add(condition);
                            plugin.getPluginLogger().info("Loaded condition '{}'", name);
                        }
                    }
                }
            }
        } catch (final Exception e) {
            plugin.getPluginLogger().error("Failed to load conditions index", e);
        }
    }
}
