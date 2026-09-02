package me.pikamug.quests.storage.implementation.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.quests.FabricQuest;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.exceptions.QuestFormatException;
import me.pikamug.quests.quests.components.FabricStage;
import me.pikamug.quests.quests.components.Stage;
import me.pikamug.quests.storage.implementation.QuestStorageImpl;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class FabricQuestJsonStorage implements QuestStorageImpl {

    private static final Set<String> INDEX_FILES = Set.of("quests.json", "actions.json", "conditions.json");

    private final FabricQuestsPlugin plugin;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private Path storageDir;

    public FabricQuestJsonStorage(FabricQuestsPlugin plugin) {
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
        loadQuests();
    }

    @Override
    public void close() {}

    @Override
    public Quest loadQuest(String name) throws QuestFormatException {
        final Path file = storageDir.resolve(name + ".json");
        if (!Files.exists(file)) return null;
        try (Reader reader = Files.newBufferedReader(file)) {
            final JsonObject json = gson.fromJson(reader, JsonObject.class);
            if (json == null) return null;
            return parseQuest(name, json);
        } catch (final Exception e) {
            throw new QuestFormatException("Failed to load quest: " + name
                    + (e.getMessage() != null ? " - " + e.getMessage() : ""), name);
        }
    }

    private Quest parseQuest(String name, JsonObject json) {
        final FabricQuest quest = new FabricQuest();
        quest.setId(name);
        if (json.has("name")) quest.setName(json.get("name").getAsString());
        if (json.has("description")) quest.setDescription(json.get("description").getAsString());
        if (json.has("finished")) quest.setFinished(json.get("finished").getAsString());
        if (json.has("region-start")) quest.setRegionStart(json.get("region-start").getAsString());
        if (json.has("regionStart")) quest.setRegionStart(json.get("regionStart").getAsString());
        if (json.has("npc-start")) {
            try {
                quest.setNpcStart(UUID.fromString(json.get("npc-start").getAsString()));
            } catch (final Exception ignored) {}
        }
        if (json.has("npc-start-name")) {
            quest.setNpcStartName(json.get("npc-start-name").getAsString());
        }
        if (json.has("gui-display")) {
            // Gui display is stored as a string for now
            if (json.get("gui-display").isJsonPrimitive()) {
                quest.getRewards().setDetailsOverride(List.of(json.get("gui-display").getAsString()));
            }
        }
        // Parse requirements
        if (json.has("requirements")) {
            final JsonObject reqJson = json.getAsJsonObject("requirements");
            parseRequirements(quest, reqJson);
        }
        // Parse planner
        if (json.has("planner")) {
            final JsonObject plnJson = json.getAsJsonObject("planner");
            parsePlanner(quest, plnJson);
        }
        // Parse rewards
        if (json.has("rewards")) {
            final JsonObject rewJson = json.getAsJsonObject("rewards");
            parseRewards(quest, rewJson);
        }
        // Parse options
        if (json.has("options")) {
            final JsonObject optJson = json.getAsJsonObject("options");
            parseOptions(quest, optJson);
        }
        // Parse stages
        if (json.has("stages")) {
            final JsonArray stagesArray = json.getAsJsonArray("stages");
            final LinkedList<Stage> stages = new LinkedList<>();
            for (int i = 0; i < stagesArray.size(); i++) {
                final JsonObject stageJson = stagesArray.get(i).getAsJsonObject();
                final FabricStage stage = parseStage(stageJson);
                stages.add(stage);
            }
            quest.setStages(stages);
        }
        return quest;
    }

    private FabricStage parseStage(JsonObject json) {
        final FabricStage stage = new FabricStage();
        if (json.has("start-message")) stage.setStartMessage(json.get("start-message").getAsString());
        if (json.has("startMessage")) stage.setStartMessage(json.get("startMessage").getAsString());
        if (json.has("complete-message")) stage.setCompleteMessage(json.get("complete-message").getAsString());
        if (json.has("completeMessage")) stage.setCompleteMessage(json.get("completeMessage").getAsString());
        if (json.has("delay")) stage.setDelay(json.get("delay").getAsLong());
        if (json.has("delay-message")) stage.setDelayMessage(json.get("delay-message").getAsString());
        if (json.has("script")) stage.setScript(json.get("script").getAsString());
        if (json.has("password-displays")) {
            final JsonArray arr = json.getAsJsonArray("password-displays");
            final LinkedList<String> list = new LinkedList<>();
            arr.forEach(e -> list.add(e.getAsString()));
            stage.setPasswordDisplays(list);
        }
        if (json.has("password-phrases")) {
            final JsonArray arr = json.getAsJsonArray("password-phrases");
            final LinkedList<String> list = new LinkedList<>();
            arr.forEach(e -> list.add(e.getAsString()));
            stage.setPasswordPhrases(list);
        }
        // Block objectives
        parseBlockList(json, "break-blocks", stage, "break");
        parseBlockList(json, "place-blocks", stage, "place");
        parseBlockList(json, "use-blocks", stage, "use");
        parseBlockList(json, "cut-blocks", stage, "cut");
        // Item objectives
        parseItemList(json, "craft-items", stage, "craft");
        parseItemList(json, "smelt-items", stage, "smelt");
        parseItemList(json, "consume-items", stage, "consume");
        // Mob objectives
        if (json.has("mobs-to-kill")) {
            final JsonArray arr = json.getAsJsonArray("mobs-to-kill");
            final LinkedList<Object> mobs = new LinkedList<>();
            arr.forEach(e -> mobs.add(e.getAsString()));
            stage.setMobsToKill(mobs);
        }
        if (json.has("mob-num-to-kill")) {
            final JsonArray arr = json.getAsJsonArray("mob-num-to-kill");
            final LinkedList<Integer> nums = new LinkedList<>();
            arr.forEach(e -> nums.add(e.getAsInt()));
            stage.setMobNumToKill(nums);
        }
        // NPC objectives
        if (json.has("npcs-to-interact")) {
            final JsonArray arr = json.getAsJsonArray("npcs-to-interact");
            final LinkedList<UUID> npcs = new LinkedList<>();
            arr.forEach(e -> {
                try { npcs.add(UUID.fromString(e.getAsString())); } catch (final Exception ignored) {}
            });
            stage.setNpcsToInteract(npcs);
        }
        if (json.has("npcs-to-kill")) {
            final JsonArray arr = json.getAsJsonArray("npcs-to-kill");
            final LinkedList<UUID> npcs = new LinkedList<>();
            arr.forEach(e -> {
                try { npcs.add(UUID.fromString(e.getAsString())); } catch (final Exception ignored) {}
            });
            stage.setNpcsToKill(npcs);
        }
        if (json.has("npc-num-to-kill")) {
            final JsonArray arr = json.getAsJsonArray("npc-num-to-kill");
            final LinkedList<Integer> nums = new LinkedList<>();
            arr.forEach(e -> nums.add(e.getAsInt()));
            stage.setNpcNumToKill(nums);
        }
        // Player objectives
        if (json.has("players-to-kill")) {
            stage.setPlayersToKill(json.get("players-to-kill").getAsInt());
        }
        // Location objectives
        if (json.has("locations-to-reach")) {
            final JsonArray arr = json.getAsJsonArray("locations-to-reach");
            final LinkedList<Object> locs = new LinkedList<>();
            arr.forEach(e -> locs.add(e.getAsString()));
            stage.setLocationsToReach(locs);
        }
        if (json.has("radii-to-reach-within")) {
            final JsonArray arr = json.getAsJsonArray("radii-to-reach-within");
            final LinkedList<Integer> radii = new LinkedList<>();
            arr.forEach(e -> radii.add(e.getAsInt()));
            stage.setRadiiToReachWithin(radii);
        }
        if (json.has("location-names")) {
            final JsonArray arr = json.getAsJsonArray("location-names");
            final LinkedList<String> names = new LinkedList<>();
            arr.forEach(e -> names.add(e.getAsString()));
            stage.setLocationNames(names);
        }
        // Tame objectives
        if (json.has("mobs-to-tame")) {
            final JsonArray arr = json.getAsJsonArray("mobs-to-tame");
            final LinkedList<Object> mobs = new LinkedList<>();
            arr.forEach(e -> mobs.add(e.getAsString()));
            stage.setMobsToTame(mobs);
        }
        if (json.has("mob-num-to-tame")) {
            final JsonArray arr = json.getAsJsonArray("mob-num-to-tame");
            final LinkedList<Integer> nums = new LinkedList<>();
            arr.forEach(e -> nums.add(e.getAsInt()));
            stage.setMobNumToTame(nums);
        }
        // Shear objectives
        if (json.has("sheep-to-shear")) {
            final JsonArray arr = json.getAsJsonArray("sheep-to-shear");
            final LinkedList<Object> sheep = new LinkedList<>();
            arr.forEach(e -> sheep.add(e.getAsString()));
            stage.setSheepToShear(sheep);
        }
        if (json.has("sheep-num-to-shear")) {
            final JsonArray arr = json.getAsJsonArray("sheep-num-to-shear");
            final LinkedList<Integer> nums = new LinkedList<>();
            arr.forEach(e -> nums.add(e.getAsInt()));
            stage.setSheepNumToShear(nums);
        }
        if (json.has("fish-to-catch")) stage.setFishToCatch(json.get("fish-to-catch").getAsInt());
        if (json.has("cows-to-milk")) stage.setCowsToMilk(json.get("cows-to-milk").getAsInt());
        // Objective overrides
        if (json.has("objective-overrides")) {
            final JsonArray arr = json.getAsJsonArray("objective-overrides");
            final LinkedList<String> overrides = new LinkedList<>();
            arr.forEach(e -> overrides.add(e.getAsString()));
            stage.setObjectiveOverrides(overrides);
        }
        return stage;
    }

    private void parseBlockList(JsonObject json, String key, FabricStage stage, String type) {
        if (json.has(key)) {
            final JsonArray arr = json.getAsJsonArray(key);
            final LinkedList<Object> list = new LinkedList<>();
            arr.forEach(e -> {
                if (e.isJsonObject()) {
                    list.add(e.getAsJsonObject().toString());
                } else {
                    list.add(e.getAsString());
                }
            });
            switch (type) {
                case "break" -> stage.setBlocksToBreak(list);
                case "place" -> stage.setBlocksToPlace(list);
                case "use" -> stage.setBlocksToUse(list);
                case "cut" -> stage.setBlocksToCut(list);
            }
        }
    }

    private void parseItemList(JsonObject json, String key, FabricStage stage, String type) {
        if (json.has(key)) {
            final JsonArray arr = json.getAsJsonArray(key);
            final LinkedList<Object> list = new LinkedList<>();
            arr.forEach(e -> {
                if (e.isJsonObject()) {
                    list.add(e.getAsJsonObject().toString());
                } else {
                    list.add(e.getAsString());
                }
            });
            switch (type) {
                case "craft" -> stage.setItemsToCraft(list);
                case "smelt" -> stage.setItemsToSmelt(list);
                case "consume" -> stage.setItemsToConsume(list);
            }
        }
    }

    private void parseRequirements(FabricQuest quest, JsonObject json) {
        final var req = quest.getRequirements();
        if (json.has("quest-points")) req.setQuestPoints(json.get("quest-points").getAsInt());
        if (json.has("quest-points-amount")) req.setQuestPoints(json.get("quest-points-amount").getAsInt());
        if (json.has("needed-quests")) {
            final JsonArray arr = json.getAsJsonArray("needed-quests");
            final LinkedList<String> ids = new LinkedList<>();
            arr.forEach(e -> ids.add(e.getAsString()));
            req.setNeededQuestIds(ids);
        }
        if (json.has("blocked-quests")) {
            final JsonArray arr = json.getAsJsonArray("blocked-quests");
            final LinkedList<String> ids = new LinkedList<>();
            arr.forEach(e -> ids.add(e.getAsString()));
            req.setBlockQuestIds(ids);
        }
    }

    private void parsePlanner(FabricQuest quest, JsonObject json) {
        final var pln = quest.getPlanner();
        if (json.has("start") && json.get("start").isJsonPrimitive()) {
            final var startEl = json.get("start");
            if (startEl.getAsJsonPrimitive().isNumber()) {
                pln.setStart(new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss")
                        .format(new java.util.Date(startEl.getAsLong())));
            } else {
                pln.setStart(startEl.getAsString());
            }
        }
        if (json.has("end") && json.get("end").isJsonPrimitive()) {
            final var endEl = json.get("end");
            if (endEl.getAsJsonPrimitive().isNumber()) {
                pln.setEnd(new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss")
                        .format(new java.util.Date(endEl.getAsLong())));
            } else {
                pln.setEnd(endEl.getAsString());
            }
        }
        if (json.has("repeat")) pln.setRepeat(json.get("repeat").getAsLong());
        if (json.has("cooldown")) pln.setCooldown(json.get("cooldown").getAsLong());
        if (json.has("override")) pln.setOverride(json.get("override").getAsBoolean());
    }

    private void parseRewards(FabricQuest quest, JsonObject json) {
        final var rew = quest.getRewards();
        if (json.has("quest-points")) rew.setQuestPoints(json.get("quest-points").getAsInt());
        if (json.has("commands")) {
            final JsonArray arr = json.getAsJsonArray("commands");
            final LinkedList<String> cmds = new LinkedList<>();
            arr.forEach(e -> cmds.add(e.getAsString()));
            rew.setCommands(cmds);
        }
        if (json.has("details-override")) {
            final JsonArray arr = json.getAsJsonArray("details-override");
            if (arr != null) {
                final LinkedList<String> overrides = new LinkedList<>();
                arr.forEach(e -> overrides.add(e.getAsString()));
                rew.setDetailsOverride(overrides);
            }
        }
    }

    private void parseOptions(FabricQuest quest, JsonObject json) {
        final var opt = quest.getOptions();
        if (json.has("allow-commands")) opt.setAllowCommands(json.get("allow-commands").getAsBoolean());
        if (json.has("allow-quitting")) opt.setAllowQuitting(json.get("allow-quitting").getAsBoolean());
        if (json.has("ignore-silk-touch")) opt.setIgnoreSilkTouch(json.get("ignore-silk-touch").getAsBoolean());
        if (json.has("override-max-quests")) opt.setOverrideMaxQuests(json.get("override-max-quests").getAsBoolean());
        if (json.has("inform-quest-start")) opt.setInformOnStart(json.get("inform-quest-start").getAsBoolean());
        if (json.has("give-globally-at-login")) opt.setGiveGloballyAtLogin(json.get("give-globally-at-login").getAsBoolean());
    }

    public void loadQuests() {
        if (!Files.exists(storageDir)) return;
        splitIndex("quests.json");
        try (var stream = Files.list(storageDir)) {
            stream.filter(p -> p.toString().endsWith(".json")).forEach(p -> {
                final String name = p.getFileName().toString().replace(".json", "");
                if (INDEX_FILES.contains(name)) return;
                try {
                    final Quest quest = loadQuest(name);
                    if (quest != null) {
                        plugin.getLoadedQuests().add(quest);
                        plugin.getPluginLogger().info("Loaded quest '{}' from {}", name, p.getFileName());
                    }
                } catch (final Exception e) {
                    plugin.getPluginLogger().error("Failed to load quest from {}", p, e);
                }
            });
        } catch (final Exception e) {
            plugin.getPluginLogger().error("Failed to list quest files", e);
        }
    }

    private void splitIndex(final String indexName) {
        final Path indexFile = storageDir.resolve(indexName);
        if (!Files.exists(indexFile)) return;
        try (Reader reader = Files.newBufferedReader(indexFile)) {
            final JsonObject json = gson.fromJson(reader, JsonObject.class);
            if (json == null) return;
            for (final String name : json.keySet()) {
                if (json.get(name).isJsonObject()) {
                    final Path individualFile = storageDir.resolve(name + ".json");
                    if (!Files.exists(individualFile)) {
                        Files.write(individualFile, gson.toJson(json.getAsJsonObject(name)).getBytes());
                    }
                }
            }
        } catch (final Exception e) {
            plugin.getPluginLogger().error("Failed to split quest index {}", indexName, e);
        }
    }

    public void importQuests() {
        final Path rootFile = plugin.getPluginDataFolder().toPath().resolve("quests.json");
        if (!Files.exists(rootFile)) return;
        try (Reader reader = Files.newBufferedReader(rootFile)) {
            final JsonObject json = gson.fromJson(reader, JsonObject.class);
            if (json == null) return;
            for (final String key : json.keySet()) {
                final Quest existing = plugin.getLoadedQuests().stream()
                        .filter(q -> key.equals(q.getId())).findFirst().orElse(null);
                if (existing == null && json.get(key).isJsonObject()) {
                    final Quest quest = parseQuest(key, json.getAsJsonObject(key));
                    if (quest != null) {
                        plugin.getLoadedQuests().add(quest);
                        plugin.getPluginLogger().info("Imported quest '{}' from quests.json", key);
                    }
                }
            }
        } catch (final Exception e) {
            plugin.getPluginLogger().error("Failed to import quests from quests.json", e);
        }
    }
}
