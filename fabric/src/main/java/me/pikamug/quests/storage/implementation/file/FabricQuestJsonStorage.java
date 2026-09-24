package me.pikamug.quests.storage.implementation.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.actions.Action;
import me.pikamug.quests.conditions.Condition;
import me.pikamug.quests.quests.FabricQuest;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.exceptions.QuestFormatException;
import me.pikamug.quests.quests.components.FabricStage;
import me.pikamug.quests.quests.components.Stage;
import me.pikamug.quests.storage.implementation.QuestStorageImpl;
import me.pikamug.quests.util.FabricItemUtil;
import net.minecraft.world.item.ItemStack;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
        removeLegacyIndividualFiles();
        loadQuests();
    }

    @Override
    public void close() {}

    /**
     * Deletes quest/action/condition files written as individual {@code storage/<name>.json}
     * entries by older builds of this port. Those files were pure duplicates of the entries
     * in the {@code quests.json}, {@code actions.json} and {@code conditions.json} index files
     * and should never have been generated (see the Bukkit module for parity).
     */
    private void removeLegacyIndividualFiles() {
        try (var stream = Files.list(storageDir)) {
            stream.filter(p -> p.toString().endsWith(".json"))
                    .filter(p -> !INDEX_FILES.contains(p.getFileName().toString()))
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                            plugin.getPluginLogger().info("Removed legacy individual storage file {}", p.getFileName());
                        } catch (final Exception e) {
                            plugin.getPluginLogger().error("Failed to remove legacy storage file {}", p, e);
                        }
                    });
        } catch (final Exception e) {
            plugin.getPluginLogger().error("Failed to list storage directory", e);
        }
    }

    @Override
    public Quest loadQuest(String name) throws QuestFormatException {
        final JsonObject json = indexEntry("quests.json", name);
        if (json == null) return null;
        return parseQuest(name, json);
    }

    private JsonObject indexEntry(final String indexName, final String name) {
        final Path indexFile = storageDir.resolve(indexName);
        if (!Files.exists(indexFile)) return null;
        try (Reader reader = Files.newBufferedReader(indexFile)) {
            final JsonObject root = gson.fromJson(reader, JsonObject.class);
            if (root != null && root.has(name) && root.get(name).isJsonObject()) {
                return root.getAsJsonObject(name);
            }
        } catch (final Exception e) {
            plugin.getPluginLogger().error("Failed to read '{}' from {}", name, indexName, e);
        }
        return null;
    }

    /**
     * Returns the raw quest definition stored in the {@code quests.json} index for the given
     * quest id, or {@code null} if no such entry exists.
     *
     * @param name the quest id to look up
     * @return the quest JSON, or {@code null}
     */
    public JsonObject getQuestData(final String name) {
        return indexEntry("quests.json", name);
    }

    private Quest parseQuest(String name, JsonObject json) {
        final FabricQuest quest = new FabricQuest();
        quest.setId(name);
        if (json.has("name")) quest.setName(json.get("name").getAsString());
        if (json.has("ask-message")) quest.setDescription(json.get("ask-message").getAsString());
        if (json.has("finish-message")) quest.setFinished(json.get("finish-message").getAsString());
        if (json.has("region")) quest.setRegionStart(json.get("region").getAsString());
        if (json.has("block-start-x") && json.has("block-start-y") && json.has("block-start-z")) {
            try {
                quest.setBlockStart(new net.minecraft.core.BlockPos(
                        json.get("block-start-x").getAsInt(),
                        json.get("block-start-y").getAsInt(),
                        json.get("block-start-z").getAsInt()));
            } catch (final Exception ignored) {}
        }
        if (json.has("npc-giver-uuid")) {
            try {
                quest.setNpcStart(UUID.fromString(json.get("npc-giver-uuid").getAsString()));
            } catch (final Exception ignored) {}
        }
        if (json.has("npc-giver-name")) {
            quest.setNpcStartName(json.get("npc-giver-name").getAsString());
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
        if (json.has("complete-message")) stage.setCompleteMessage(json.get("complete-message").getAsString());
        if (json.has("delay")) stage.setDelay(json.get("delay").getAsLong());
        if (json.has("delay-message")) stage.setDelayMessage(json.get("delay-message").getAsString());
        if (json.has("script-to-run")) stage.setScript(json.get("script-to-run").getAsString());
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
        parseBlockList(json, "break-block-names", "break-block-amounts", stage, "break");
        parseBlockList(json, "damage-block-names", "damage-block-amounts", stage, "damage");
        parseBlockList(json, "place-block-names", "place-block-amounts", stage, "place");
        parseBlockList(json, "use-block-names", "use-block-amounts", stage, "use");
        // Note: cut-block objectives are intentionally not supported in this port
        // Item objectives
        parseItemList(json, "items-to-craft", stage, "craft");
        parseItemList(json, "items-to-smelt", stage, "smelt");
        parseItemList(json, "items-to-enchant", stage, "enchant");
        parseItemList(json, "items-to-brew", stage, "brew");
        parseItemList(json, "items-to-consume", stage, "consume");
        parseItemList(json, "items-to-deliver", stage, "deliver");
        if (json.has("npc-delivery-uuids")) {
            final JsonArray arr = json.getAsJsonArray("npc-delivery-uuids");
            final LinkedList<UUID> targets = new LinkedList<>();
            arr.forEach(e -> {
                try { targets.add(UUID.fromString(e.getAsString())); } catch (final Exception ignored) {}
            });
            stage.setItemDeliveryTargets(targets);
        }
        if (json.has("delivery-messages")) {
            final JsonArray arr = json.getAsJsonArray("delivery-messages");
            final LinkedList<String> msgs = new LinkedList<>();
            arr.forEach(e -> msgs.add(e.getAsString()));
            stage.setDeliverMessages(msgs);
        }
        // Mob objectives
        if (json.has("mobs-to-kill")) {
            final JsonArray arr = json.getAsJsonArray("mobs-to-kill");
            final LinkedList<Object> mobs = new LinkedList<>();
            arr.forEach(e -> mobs.add(e.getAsString()));
            stage.setMobsToKill(mobs);
        }
        if (json.has("mob-amounts")) {
            final JsonArray arr = json.getAsJsonArray("mob-amounts");
            final LinkedList<Integer> nums = new LinkedList<>();
            arr.forEach(e -> nums.add(e.getAsInt()));
            stage.setMobNumToKill(nums);
        }
        // NPC objectives
        if (json.has("npc-uuids-to-talk-to")) {
            final JsonArray arr = json.getAsJsonArray("npc-uuids-to-talk-to");
            final LinkedList<UUID> npcs = new LinkedList<>();
            arr.forEach(e -> {
                try { npcs.add(UUID.fromString(e.getAsString())); } catch (final Exception ignored) {}
            });
            stage.setNpcsToInteract(npcs);
        }
        if (json.has("npc-uuids-to-kill")) {
            final JsonArray arr = json.getAsJsonArray("npc-uuids-to-kill");
            final LinkedList<UUID> npcs = new LinkedList<>();
            arr.forEach(e -> {
                try { npcs.add(UUID.fromString(e.getAsString())); } catch (final Exception ignored) {}
            });
            stage.setNpcsToKill(npcs);
        }
        if (json.has("npc-kill-amounts")) {
            final JsonArray arr = json.getAsJsonArray("npc-kill-amounts");
            final LinkedList<Integer> nums = new LinkedList<>();
            arr.forEach(e -> nums.add(e.getAsInt()));
            stage.setNpcNumToKill(nums);
        }
        // Player objectives
        if (json.has("players-to-kill")) {
            stage.setPlayersToKill(json.get("players-to-kill").getAsInt());
        }
        // Kill location restrictions
        if (json.has("locations-to-kill")) {
            final JsonArray arr = json.getAsJsonArray("locations-to-kill");
            final LinkedList<Object> locs = new LinkedList<>();
            arr.forEach(e -> locs.add(e.getAsString()));
            stage.setLocationsToKillWithin(locs);
        }
        if (json.has("kill-location-radii")) {
            final JsonArray arr = json.getAsJsonArray("kill-location-radii");
            final LinkedList<Integer> radii = new LinkedList<>();
            arr.forEach(e -> radii.add(e.getAsInt()));
            stage.setRadiiToKillWithin(radii);
        }
        if (json.has("kill-location-names")) {
            final JsonArray arr = json.getAsJsonArray("kill-location-names");
            final LinkedList<String> names = new LinkedList<>();
            arr.forEach(e -> names.add(e.getAsString()));
            stage.setKillNames(names);
        }
        // Location objectives
        if (json.has("locations-to-reach")) {
            final JsonArray arr = json.getAsJsonArray("locations-to-reach");
            final LinkedList<Object> locs = new LinkedList<>();
            arr.forEach(e -> locs.add(e.getAsString()));
            stage.setLocationsToReach(locs);
        }
        if (json.has("reach-location-radii")) {
            final JsonArray arr = json.getAsJsonArray("reach-location-radii");
            final LinkedList<Integer> radii = new LinkedList<>();
            arr.forEach(e -> radii.add(e.getAsInt()));
            stage.setRadiiToReachWithin(radii);
        }
        if (json.has("reach-location-names")) {
            final JsonArray arr = json.getAsJsonArray("reach-location-names");
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
        if (json.has("mob-tame-amounts")) {
            final JsonArray arr = json.getAsJsonArray("mob-tame-amounts");
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
        if (json.has("sheep-amounts")) {
            final JsonArray arr = json.getAsJsonArray("sheep-amounts");
            final LinkedList<Integer> nums = new LinkedList<>();
            arr.forEach(e -> nums.add(e.getAsInt()));
            stage.setSheepNumToShear(nums);
        }
        if (json.has("fish-to-catch")) stage.setFishToCatch(json.get("fish-to-catch").getAsInt());
        if (json.has("cows-to-milk")) stage.setCowsToMilk(json.get("cows-to-milk").getAsInt());
        // Objective overrides
        if (json.has("objective-override")) {
            final JsonElement element = json.get("objective-override");
            final LinkedList<String> overrides = new LinkedList<>();
            if (element.isJsonArray()) {
                element.getAsJsonArray().forEach(e -> overrides.add(e.getAsString()));
            } else {
                // Legacy
                overrides.add(element.getAsString());
            }
            stage.setObjectiveOverrides(overrides);
        }
        // Stage actions
        parseStageAction(json, "start-event", stage, "start");
        parseStageAction(json, "finish-event", stage, "finish");
        parseStageAction(json, "fail-event", stage, "fail");
        parseStageAction(json, "death-event", stage, "death");
        parseStageAction(json, "disconnect-event", stage, "disconnect");
        // Chat and command event actions
        parseTriggeredActions(json, "chat-events", "chat-event-triggers", stage, true);
        parseTriggeredActions(json, "command-events", "command-event-triggers", stage, false);
        // Stage condition
        if (json.has("condition")) {
            final Condition condition = resolveCondition(json.get("condition").getAsString());
            if (condition != null) {
                stage.setCondition(condition);
            } else {
                plugin.getPluginLogger().error("Failed to load condition '{}' for stage",
                        json.get("condition").getAsString());
            }
        }
        return stage;
    }

    private void parseStageAction(JsonObject json, String key, FabricStage stage, String type) {
        if (!json.has(key)) return;
        final Action action = resolveAction(json.get(key).getAsString());
        if (action == null) {
            plugin.getPluginLogger().error("Failed to load action '{}' for stage '{}'",
                    json.get(key).getAsString(), type);
            return;
        }
        switch (type) {
            case "start" -> stage.setStartAction(action);
            case "finish" -> stage.setFinishAction(action);
            case "fail" -> stage.setFailAction(action);
            case "death" -> stage.setDeathAction(action);
            case "disconnect" -> stage.setDisconnectAction(action);
        }
    }

    private void parseTriggeredActions(JsonObject json, String eventsKey, String triggersKey,
                                       FabricStage stage, boolean chat) {
        if (!json.has(eventsKey) || !json.get(eventsKey).isJsonArray()) return;
        final JsonArray events = json.getAsJsonArray(eventsKey);
        final JsonArray triggers = json.has(triggersKey) ? json.getAsJsonArray(triggersKey) : null;
        final Map<String, Action> actions = new LinkedHashMap<>();
        for (int i = 0; i < events.size(); i++) {
            if (triggers == null || triggers.size() <= i) {
                return;
            }
            final Action action = resolveAction(events.get(i).getAsString());
            if (action != null) {
                actions.put(triggers.get(i).getAsString(), action);
            } else {
                plugin.getPluginLogger().error("Failed to load action '{}' for stage", events.get(i).getAsString());
            }
        }
        if (chat) {
            stage.setChatActions(actions);
        } else {
            stage.setCommandActions(actions);
        }
    }

    private Action resolveAction(String name) {
        for (final Action action : plugin.getLoadedActions()) {
            if (action.getName().equals(name)) {
                return action;
            }
        }
        return null;
    }

    private Condition resolveCondition(String name) {
        for (final Condition condition : plugin.getLoadedConditions()) {
            if (condition.getName().equals(name)) {
                return condition;
            }
        }
        return null;
    }

    private void parseBlockList(JsonObject json, String key, String amountKey, FabricStage stage, String type) {
        if (json.has(key)) {
            final JsonArray arr = json.getAsJsonArray(key);
            final LinkedList<Object> list = new LinkedList<>();
            arr.forEach(e -> {
                if (e.isJsonObject()) {
                    list.add(e.getAsJsonObject().toString());
                } else {
                    list.add(normalizeBlockName(e.getAsString()));
                }
            });
            final LinkedList<Integer> amounts = new LinkedList<>();
            if (json.has(amountKey)) {
                json.getAsJsonArray(amountKey).forEach(a -> amounts.add(a.getAsInt()));
            }
            while (amounts.size() < list.size()) {
                amounts.add(1);
            }
            switch (type) {
                case "break" -> {
                    stage.setBlocksToBreak(list);
                    stage.setBlocksToBreakAmounts(amounts);
                }
                case "damage" -> stage.setBlocksToDamage(list);
                case "place" -> {
                    stage.setBlocksToPlace(list);
                    stage.setBlocksToPlaceAmounts(amounts);
                }
                case "use" -> {
                    stage.setBlocksToUse(list);
                    stage.setBlocksToUseAmounts(amounts);
                }
            }
        }
    }

    /**
     * Normalizes a block name read from config. Plain names such as {@code DIRT}
     * are given the default {@code minecraft:} namespace so they can be resolved
     * against the item registry (e.g. {@code minecraft:dirt}). Values that already
     * carry a namespace are left untouched, just lowercased.
     */
    private String normalizeBlockName(String name) {
        if (name == null) {
            return name;
        }
        final String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }
        if (trimmed.contains(":")) {
            return trimmed.toLowerCase();
        }
        return "minecraft:" + trimmed.toLowerCase();
    }

    private void parseItemList(JsonObject json, String key, FabricStage stage, String type) {
        if (json.has(key)) {
            final JsonArray arr = json.getAsJsonArray(key);
            final LinkedList<Object> list = new LinkedList<>();
            arr.forEach(e -> {
                if (e.isJsonObject()) {
                    list.add(FabricItemUtil.deserializeFromJson(e.getAsJsonObject()));
                } else {
                    list.add(FabricItemUtil.deserialize(e.getAsString()));
                }
            });
            switch (type) {
                case "craft" -> stage.setItemsToCraft(list);
                case "smelt" -> stage.setItemsToSmelt(list);
                case "enchant" -> stage.setItemsToEnchant(list);
                case "brew" -> stage.setItemsToBrew(list);
                case "consume" -> stage.setItemsToConsume(list);
                case "deliver" -> stage.setItemsToDeliver(list);
            }
        }
    }

    private void parseRequirements(FabricQuest quest, JsonObject json) {
        final var req = quest.getRequirements();
        if (json.has("quest-points")) req.setQuestPoints(json.get("quest-points").getAsInt());
        if (json.has("exp")) req.setExp(json.get("exp").getAsInt());
        if (json.has("quests")) {
            final JsonArray arr = json.getAsJsonArray("quests");
            final LinkedList<String> ids = new LinkedList<>();
            arr.forEach(e -> ids.add(e.getAsString()));
            req.setNeededQuestIds(ids);
        }
        if (json.has("quest-blocks")) {
            final JsonArray arr = json.getAsJsonArray("quest-blocks");
            final LinkedList<String> ids = new LinkedList<>();
            arr.forEach(e -> ids.add(e.getAsString()));
            req.setBlockQuestIds(ids);
        }
        if (json.has("permissions")) {
            final JsonArray arr = json.getAsJsonArray("permissions");
            final LinkedList<String> perms = new LinkedList<>();
            arr.forEach(e -> perms.add(e.getAsString()));
            req.setPermissions(perms);
        }
        if (json.has("items")) {
            ((me.pikamug.quests.quests.components.FabricRequirements) req)
                    .setItems(parseItemObjectList(json.getAsJsonArray("items")));
        }
        if (json.has("remove-items")) {
            final JsonArray arr = json.getAsJsonArray("remove-items");
            final LinkedList<Boolean> remove = new LinkedList<>();
            arr.forEach(e -> remove.add(e.getAsBoolean()));
            req.setRemoveItems(remove);
        }
        if (json.has("fail-requirement-message")) {
            final JsonElement element = json.get("fail-requirement-message");
            final LinkedList<String> override = new LinkedList<>();
            if (element.isJsonArray()) {
                element.getAsJsonArray().forEach(e -> override.add(e.getAsString()));
            } else {
                // Legacy
                override.add(element.getAsString());
            }
            req.setDetailsOverride(override);
        } else if (json.has("details-override")) {
            final JsonArray arr = json.getAsJsonArray("details-override");
            final LinkedList<String> override = new LinkedList<>();
            arr.forEach(e -> override.add(e.getAsString()));
            req.setDetailsOverride(override);
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
        if (json.has("exp")) rew.setExp(json.get("exp").getAsInt());
        if (json.has("commands")) {
            final JsonArray arr = json.getAsJsonArray("commands");
            final LinkedList<String> cmds = new LinkedList<>();
            arr.forEach(e -> cmds.add(e.getAsString()));
            rew.setCommands(cmds);
        }
        if (json.has("commands-override-display")) {
            final JsonArray arr = json.getAsJsonArray("commands-override-display");
            final LinkedList<String> overrides = new LinkedList<>();
            arr.forEach(e -> overrides.add(e.getAsString()));
            rew.setCommandsOverrideDisplay(overrides);
        }
        if (json.has("permissions")) {
            final JsonArray arr = json.getAsJsonArray("permissions");
            final LinkedList<String> perms = new LinkedList<>();
            arr.forEach(e -> perms.add(e.getAsString()));
            rew.setPermissions(perms);
        }
        if (json.has("permission-worlds")) {
            final JsonArray arr = json.getAsJsonArray("permission-worlds");
            final LinkedList<String> worlds = new LinkedList<>();
            arr.forEach(e -> worlds.add(e.getAsString()));
            rew.setPermissionWorlds(worlds);
        }
        if (json.has("details-override")) {
            final JsonArray arr = json.getAsJsonArray("details-override");
            if (arr != null) {
                final LinkedList<String> overrides = new LinkedList<>();
                arr.forEach(e -> overrides.add(e.getAsString()));
                rew.setDetailsOverride(overrides);
            }
        }
        if (json.has("items")) {
            ((me.pikamug.quests.quests.components.FabricRewards) rew)
                    .setItems(parseItemObjectList(json.getAsJsonArray("items")));
        }
    }

    private LinkedList<ItemStack> parseItemObjectList(JsonArray arr) {
        final LinkedList<ItemStack> list = new LinkedList<>();
        arr.forEach(e -> {
            if (e.isJsonObject()) {
                list.add(FabricItemUtil.deserializeFromJson(e.getAsJsonObject()));
            } else {
                list.add(FabricItemUtil.deserialize(e.getAsString()));
            }
        });
        return list;
    }

    private void parseOptions(FabricQuest quest, JsonObject json) {
        final var opt = quest.getOptions();
        if (json.has("allow-commands")) opt.setAllowCommands(json.get("allow-commands").getAsBoolean());
        if (json.has("allow-quitting")) opt.setAllowQuitting(json.get("allow-quitting").getAsBoolean());
        if (json.has("ignore-silk-touch")) opt.setIgnoreSilkTouch(json.get("ignore-silk-touch").getAsBoolean());
        if (json.has("external-party-plugin")) opt.setExternalPartyPlugin(json.get("external-party-plugin").getAsString());
        if (json.has("use-parties-plugin")) opt.setUsePartiesPlugin(json.get("use-parties-plugin").getAsBoolean());
        if (json.has("share-progress-level")) opt.setShareProgressLevel(json.get("share-progress-level").getAsInt());
        if (json.has("same-quest-only")) opt.setShareSameQuestOnly(json.get("same-quest-only").getAsBoolean());
        if (json.has("share-distance")) opt.setShareDistance(json.get("share-distance").getAsDouble());
        if (json.has("handle-offline-players")) opt.setHandleOfflinePlayers(json.get("handle-offline-players").getAsBoolean());
        if (json.has("ignore-block-replace")) opt.setIgnoreBlockReplace(json.get("ignore-block-replace").getAsBoolean());
        if (json.has("give-at-login")) opt.setGiveGloballyAtLogin(json.get("give-at-login").getAsBoolean());
        if (json.has("allow-stacking-global")) opt.setAllowStackingGlobal(json.get("allow-stacking-global").getAsBoolean());
        if (json.has("inform-on-start")) opt.setInformOnStart(json.get("inform-on-start").getAsBoolean());
        if (json.has("override-max-quests")) opt.setOverrideMaxQuests(json.get("override-max-quests").getAsBoolean());
    }

    public void loadQuests() {
        if (!Files.exists(storageDir)) return;
        final Path indexFile = storageDir.resolve("quests.json");
        if (!Files.exists(indexFile)) return;
        try (Reader reader = Files.newBufferedReader(indexFile)) {
            final JsonObject json = gson.fromJson(reader, JsonObject.class);
            if (json == null) return;
            for (final String name : json.keySet()) {
                if (json.get(name).isJsonObject() && plugin.getLoadedQuests().stream()
                        .noneMatch(q -> name.equals(q.getId()))) {
                    try {
                        final Quest quest = parseQuest(name, json.getAsJsonObject(name));
                        if (quest != null) {
                            plugin.getLoadedQuests().add(quest);
                        }
                    } catch (final Exception e) {
                        plugin.getPluginLogger().error("Failed to load quest '{}'", name, e);
                    }
                }
            }
        } catch (final Exception e) {
            plugin.getPluginLogger().error("Failed to load quests index", e);
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
