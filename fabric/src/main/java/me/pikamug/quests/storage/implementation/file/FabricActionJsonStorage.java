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
            return action;
        } catch (final Exception e) {
            throw new ActionFormatException("Failed to load action: " + name, e);
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
                }
            }
        } catch (final Exception e) {
            plugin.getPluginLogger().error("Failed to load actions index", e);
        }
    }
}
