package me.pikamug.quests.storage.implementation.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.actions.Action;
import me.pikamug.quests.entity.FabricQuestMob;
import me.pikamug.quests.entity.QuestMob;
import me.pikamug.quests.exceptions.ActionFormatException;
import me.pikamug.quests.actions.FabricAction;
import me.pikamug.quests.storage.implementation.ActionStorageImpl;
import me.pikamug.quests.util.FabricConfigUtil;
import me.pikamug.quests.util.FabricItemUtil;
import me.pikamug.quests.util.FabricLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class FabricActionJsonStorage implements ActionStorageImpl {

    private final FabricQuestsPlugin plugin;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private Path storageDir;

    public FabricActionJsonStorage(FabricQuestsPlugin plugin) {
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
        loadActions();
    }

    @Override
    public void close() {}

    @Override
    public Action loadAction(String name) throws ActionFormatException {
        final JsonObject json = indexEntry("actions.json", name);
        if (json == null) return null;
        return parseAction(name, json);
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

    private Action parseAction(final String name, final JsonObject json) throws ActionFormatException {
        final FabricAction action = new FabricAction(plugin);
        action.setName(name);
        if (json.has("message")) action.setMessage(json.get("message").getAsString());
        if (json.has("clear-inventory")) action.setClearInv(json.get("clear-inventory").getAsBoolean());
        if (json.has("fail-quest")) action.setFailQuest(json.get("fail-quest").getAsBoolean());
        if (json.has("storm-world")) {
            final ServerLevel world = resolveLevel(json.get("storm-world").getAsString());
            if (world == null) {
                throw new ActionFormatException("'storm-world' is not a loaded world", name);
            }
            action.setStormWorld(world);
        }
        if (json.has("thunder-world")) {
            final ServerLevel world = resolveLevel(json.get("thunder-world").getAsString());
            if (world == null) {
                throw new ActionFormatException("'thunder-world' is not a loaded world", name);
            }
            action.setThunderWorld(world);
        }
        if (json.has("storm-duration")) action.setStormDuration(json.get("storm-duration").getAsInt());
        if (json.has("thunder-duration")) action.setThunderDuration(json.get("thunder-duration").getAsInt());
        if (json.has("timer")) action.setTimer(json.get("timer").getAsInt());
        if (json.has("cancel-timer")) action.setCancelTimer(json.get("cancel-timer").getAsBoolean());
        if (json.has("hunger")) action.setHunger(json.get("hunger").getAsInt());
        if (json.has("saturation")) action.setSaturation(json.get("saturation").getAsInt());
        if (json.has("health")) action.setHealth(json.get("health").getAsFloat());
        if (json.has("commands")) {
            final JsonArray arr = json.getAsJsonArray("commands");
            final LinkedList<String> cmds = new LinkedList<>();
            arr.forEach(e -> cmds.add(e.getAsString()));
            action.setCommands(cmds);
        }
        if (json.has("book")) action.setBook(json.get("book").getAsString());
        if (json.has("explosions")) {
            final JsonArray arr = json.getAsJsonArray("explosions");
            final LinkedList<FabricLocation> explosions = new LinkedList<>();
            for (final com.google.gson.JsonElement e : arr) {
                final FabricLocation loc = parseLocation(e.getAsString());
                if (loc == null) {
                    throw new ActionFormatException("'explosions' is not in proper \"WorldName x y z\" format",
                            name);
                }
                explosions.add(loc);
            }
            action.setExplosions(explosions);
        }
        if (json.has("effects") && json.has("effect-locations")) {
            final JsonArray effectsArr = json.getAsJsonArray("effects");
            final JsonArray locationsArr = json.getAsJsonArray("effect-locations");
            if (effectsArr.size() != locationsArr.size()) {
                throw new ActionFormatException("'effects' and 'effect-locations' must be lists of the same size", name);
            }
            final LinkedHashMap<FabricLocation, SoundEvent> effects = new LinkedHashMap<>();
            for (int i = 0; i < effectsArr.size(); i++) {
                final FabricLocation loc = parseLocation(locationsArr.get(i).getAsString());
                if (loc == null) {
                    throw new ActionFormatException("'effect-locations' is not a list of locations", name);
                }
                final String effectName = effectsArr.get(i).getAsString();
                final Identifier effectId = effectName.contains(":")
                        ? Identifier.tryParse(effectName.toLowerCase())
                        : Identifier.tryBuild("minecraft", effectName.toLowerCase());
                final SoundEvent sound = effectId == null ? null : BuiltInRegistries.SOUND_EVENT.getValue(effectId);
                if (sound == null) {
                    throw new ActionFormatException("'effects' is not a list of sound effects", name);
                }
                effects.put(loc, sound);
            }
            action.setEffects(effects);
        }
        if (json.has("mob-spawns")) {
            final JsonArray mobsArr = json.getAsJsonArray("mob-spawns");
            final LinkedList<QuestMob> mobs = new LinkedList<>();
            for (final JsonElement entry : mobsArr) {
                if (!entry.isJsonObject()) {
                    throw new ActionFormatException("'mob-spawns' is not a list of mobs", name);
                }
                final JsonObject mobJson = entry.getAsJsonObject();
                final FabricQuestMob mob = new FabricQuestMob();
                if (mobJson.has("name")) {
                    mob.setName(mobJson.get("name").getAsString());
                }
                if (mobJson.has("mob-type")) {
                    final String typeName = mobJson.get("mob-type").getAsString();
                    final Identifier id = typeName.contains(":")
                            ? Identifier.tryParse(typeName.toLowerCase()) : Identifier.tryBuild("minecraft", typeName.toLowerCase());
                    final EntityType<?> type = id == null ? null : BuiltInRegistries.ENTITY_TYPE.getValue(id);
                    if (type == null) {
                        throw new ActionFormatException("'mob-type' is not a valid entity type", name);
                    }
                    mob.setType(type);
                }
                if (mobJson.has("spawn-amounts")) {
                    mob.setSpawnAmounts(mobJson.get("spawn-amounts").getAsInt());
                }
                if (mobJson.has("spawn-location")) {
                    final FabricLocation loc = parseLocation(mobJson.get("spawn-location").getAsString());
                    if (loc == null) {
                        throw new ActionFormatException("'spawn-location' is not in proper \"WorldName x y z\" format",
                                name);
                    }
                    mob.setLevel(loc.getLevel());
                    mob.setSpawnLocation(BlockPos.containing(loc.getX(), loc.getY(), loc.getZ()));
                }
                if (mobJson.has("inventory") && mobJson.get("inventory").isJsonArray()) {
                    final JsonArray inventoryArr = mobJson.getAsJsonArray("inventory");
                    final net.minecraft.world.item.ItemStack[] inventory = mob.getInventory();
                    for (int i = 0; i < inventory.length && i < inventoryArr.size(); i++) {
                        if (!inventoryArr.get(i).isJsonNull()) {
                            inventory[i] = FabricItemUtil.deserializeFromJson(inventoryArr.get(i));
                        }
                    }
                }
                if (mobJson.has("drop-chances") && mobJson.get("drop-chances").isJsonArray()) {
                    final JsonArray dropArr = mobJson.getAsJsonArray("drop-chances");
                    final Float[] dropChances = mob.getDropChances();
                    for (int i = 0; i < dropChances.length && i < dropArr.size(); i++) {
                        if (!dropArr.get(i).isJsonNull()) {
                            dropChances[i] = dropArr.get(i).getAsFloat();
                        }
                    }
                }
                if (mob.getType() == null || mob.getLevel() == null || mob.getSpawnLocation() == null) {
                    throw new ActionFormatException("'mob-spawns' entry is missing a mob type or location", name);
                }
                mobs.add(mob);
            }
            action.setMobSpawns(mobs);
        }
        if (json.has("lightning-strikes")) {
            final JsonArray arr = json.getAsJsonArray("lightning-strikes");
            final LinkedList<FabricLocation> strikes = new LinkedList<>();
            for (final com.google.gson.JsonElement e : arr) {
                final FabricLocation loc = parseLocation(e.getAsString());
                if (loc == null) {
                    throw new ActionFormatException("'lightning-strikes' is not in proper \"WorldName x y z\" format",
                            name);
                }
                strikes.add(loc);
            }
            action.setLightningStrikes(strikes);
        }
        if (json.has("teleport-location")) {
            final FabricLocation loc = parseLocation(json.get("teleport-location").getAsString());
            if (loc == null) {
                throw new ActionFormatException("'teleport-location' is not in proper \"WorldName x y z\" format",
                        name);
            }
            action.setTeleport(loc);
        }
        if (json.has("potion-effect-types") && json.has("potion-effect-durations")
                && json.has("potion-effect-amplifiers")) {
            final JsonArray typesArr = json.getAsJsonArray("potion-effect-types");
            final JsonArray durationsArr = json.getAsJsonArray("potion-effect-durations");
            final JsonArray amplifiersArr = json.getAsJsonArray("potion-effect-amplifiers");
            if (typesArr.size() != durationsArr.size() || typesArr.size() != amplifiersArr.size()) {
                throw new ActionFormatException("'potion-effect-types', 'potion-effect-durations' and "
                        + "'potion-effect-amplifiers' must be lists of the same size", name);
            }
            final LinkedList<MobEffectInstance> effects = new LinkedList<>();
            for (int i = 0; i < typesArr.size(); i++) {
                final String typeName = typesArr.get(i).getAsString();
                final MobEffect effect = BuiltInRegistries.MOB_EFFECT
                        .getValue(Identifier.tryBuild("minecraft", typeName.toLowerCase()));
                if (effect == null) {
                    throw new ActionFormatException("potion-effect-types is not a list of potion effect types", name);
                }
                final Holder<MobEffect> holder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect);
                effects.add(new MobEffectInstance(holder, durationsArr.get(i).getAsInt(),
                        amplifiersArr.get(i).getAsInt()));
            }
            action.setPotionEffects(effects);
        }
        if (json.has("items")) {
            final JsonArray itemsArr = json.getAsJsonArray("items");
            final LinkedList<net.minecraft.world.item.ItemStack> items = new LinkedList<>();
            for (int i = 0; i < itemsArr.size(); i++) {
                if (itemsArr.get(i).isJsonObject()) {
                    items.add(FabricItemUtil.deserializeFromJson(itemsArr.get(i).getAsJsonObject()));
                } else {
                    items.add(FabricItemUtil.readItemStack(itemsArr.get(i).getAsString()));
                }
            }
            action.setItems(items);
        }
        return action;
    }

    private ServerLevel resolveLevel(final String name) {
        if (plugin.getServer() == null) {
            return null;
        }
        for (final ServerLevel level : plugin.getServer().getAllLevels()) {
            final Identifier id = level.dimension().identifier();
            if (id.toString().equalsIgnoreCase(name) || id.getPath().equalsIgnoreCase(name)) {
                return level;
            }
        }
        return null;
    }

    private FabricLocation parseLocation(final String value) {
        FabricLocation loc = FabricConfigUtil.getLocation(value);
        if (loc == null && value != null && value.trim().split("\\s+").length == 3) {
            loc = FabricConfigUtil.getLocation("minecraft:overworld " + value.trim());
        }
        return loc;
    }

    private void loadActions() {
        if (!Files.exists(storageDir)) return;
        final Path actFile = storageDir.resolve("actions.json");
        if (!Files.exists(actFile)) return;
        try (Reader reader = Files.newBufferedReader(actFile)) {
            final JsonObject json = gson.fromJson(reader, JsonObject.class);
            if (json == null) return;
            for (final String name : json.keySet()) {
                if (json.get(name).isJsonObject() && plugin.getAction(name) == null) {
                    try {
                        final Action action = parseAction(name, json.getAsJsonObject(name));
                        if (action != null) {
                            plugin.getLoadedActions().add(action);
                        }
                    } catch (final ActionFormatException e) {
                        plugin.getPluginLogger().error("Failed to load action: {}", name, e);
                    }
                }
            }
        } catch (final Exception e) {
            plugin.getPluginLogger().error("Failed to load actions index", e);
        }
    }
}
