package me.pikamug.quests.storage.implementation.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.actions.Action;
import me.pikamug.quests.exceptions.ActionFormatException;
import me.pikamug.quests.actions.FabricAction;
import me.pikamug.quests.storage.implementation.ActionStorageImpl;
import me.pikamug.quests.util.FabricItemUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
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
        final Path file = storageDir.resolve(name + ".json");
        if (!Files.exists(file)) return null;
        try (Reader reader = Files.newBufferedReader(file)) {
            final JsonObject json = gson.fromJson(reader, JsonObject.class);
            if (json == null) return null;
            final FabricAction action = new FabricAction();
            action.setName(name);
            if (json.has("message")) action.setMessage(json.get("message").getAsString());
            if (json.has("clear-inventory")) action.setClearInv(json.get("clear-inventory").getAsBoolean());
            if (json.has("fail-quest")) action.setFailQuest(json.get("fail-quest").getAsBoolean());
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
            if (json.has("denizen-script")) action.setDenizenScript(json.get("denizen-script").getAsString());
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
                        items.add(FabricItemUtil.deserialize(itemsArr.get(i).getAsString()));
                    }
                }
                action.setItems(items);
            }
            return action;
        } catch (final Exception e) {
            throw new ActionFormatException("Failed to load action: " + name
                    + (e.getMessage() != null ? " - " + e.getMessage() : ""), name);
        }
    }

    private void loadActions() {
        if (!Files.exists(storageDir)) return;
        final Path actFile = storageDir.resolve("actions.json");
        if (!Files.exists(actFile)) return;
        try (Reader reader = Files.newBufferedReader(actFile)) {
            final JsonObject json = gson.fromJson(reader, JsonObject.class);
            if (json == null) return;
            for (final String name : json.keySet()) {
                if (json.get(name).isJsonObject()) {
                    final Path individualFile = storageDir.resolve(name + ".json");
                    if (!Files.exists(individualFile)) {
                        Files.write(individualFile, gson.toJson(json.getAsJsonObject(name)).getBytes());
                    }
                    if (plugin.getAction(name) == null) {
                        final Action action = loadAction(name);
                        if (action != null) {
                            plugin.getLoadedActions().add(action);
                            plugin.getPluginLogger().info("Loaded action '{}'", name);
                        }
                    }
                }
            }
        } catch (final Exception e) {
            plugin.getPluginLogger().error("Failed to load actions index", e);
        }
    }
}
