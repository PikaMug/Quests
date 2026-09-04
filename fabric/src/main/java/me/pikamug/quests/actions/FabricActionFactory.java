/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.actions;

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.SessionData;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import me.pikamug.quests.util.FabricItemUtil;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FabricActionFactory implements ActionFactory {

    private final FabricQuestsPlugin plugin;
    private final List<String> namesOfActionsBeingEdited = Collections.synchronizedList(new ArrayList<>());

    public FabricActionFactory(FabricQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override public List<String> getNamesOfActionsBeingEdited() { return namesOfActionsBeingEdited; }
    @Override public void setNamesOfActionsBeingEdited(Collection<String> v) { namesOfActionsBeingEdited.clear(); namesOfActionsBeingEdited.addAll(v); }

    @Override
    public void returnToMenu(UUID uuid) {
        SessionData.clear(uuid);
    }

    @Override
    public void loadData(UUID uuid, Action action) {
        if (!(action instanceof FabricAction fabAction)) return;
        if (fabAction.getName() != null) {
            SessionData.set(uuid, Key.A_NAME, fabAction.getName());
        }
        if (fabAction.getMessage() != null) {
            SessionData.set(uuid, Key.A_MESSAGE, fabAction.getMessage());
        }
        SessionData.set(uuid, Key.A_CLEAR_INVENTORY, fabAction.isClearInv());
        SessionData.set(uuid, Key.A_FAIL_QUEST, fabAction.isFailQuest());
        if (fabAction.getCommands() != null && !fabAction.getCommands().isEmpty()) {
            SessionData.set(uuid, Key.A_COMMANDS, fabAction.getCommands());
        }
        if (fabAction.getStormDuration() > 0) {
            SessionData.set(uuid, Key.A_WORLD_STORM_DURATION, fabAction.getStormDuration());
        }
        if (fabAction.getThunderDuration() > 0) {
            SessionData.set(uuid, Key.A_WORLD_THUNDER_DURATION, fabAction.getThunderDuration());
        }
        if (fabAction.getMobSpawns() != null && !fabAction.getMobSpawns().isEmpty()) {
            SessionData.set(uuid, Key.A_MOBS, fabAction.getMobSpawns());
        }
        if (fabAction.getHunger() != 0) {
            SessionData.set(uuid, Key.A_HUNGER, fabAction.getHunger());
        }
        if (fabAction.getSaturation() != 0) {
            SessionData.set(uuid, Key.A_SATURATION, fabAction.getSaturation());
        }
        if (fabAction.getHealth() > 0) {
            SessionData.set(uuid, Key.A_HEALTH, fabAction.getHealth());
        }
        if (fabAction.getTimer() > 0) {
            SessionData.set(uuid, Key.A_TIMER, fabAction.getTimer());
        }
        if (fabAction.isCancelTimer()) {
            SessionData.set(uuid, Key.A_CANCEL_TIMER, true);
        }
        if (fabAction.getPotionEffects() != null && !fabAction.getPotionEffects().isEmpty()) {
            final LinkedList<String> types = new LinkedList<>();
            final LinkedList<Long> durations = new LinkedList<>();
            final LinkedList<Integer> strengths = new LinkedList<>();
            for (final MobEffectInstance pe : fabAction.getPotionEffects()) {
                types.add(pe.getEffect().value().getDescriptionId()
                        .replaceFirst("effect\\.minecraft\\.", "").toUpperCase());
                durations.add((long) pe.getDuration());
                strengths.add(pe.getAmplifier());
            }
            SessionData.set(uuid, Key.A_POTION_TYPES, types);
            SessionData.set(uuid, Key.A_POTION_DURATIONS, durations);
            SessionData.set(uuid, Key.A_POTION_STRENGTH, strengths);
        }
        if (fabAction.getItems() != null && !fabAction.getItems().isEmpty()) {
            SessionData.set(uuid, Key.A_ITEMS, fabAction.getItems());
        }
    }

    @Override
    public void clearData(UUID uuid) {
        SessionData.clear(uuid);
    }

    @Override
    public void deleteAction(UUID uuid) {
        final String actionName = (String) SessionData.get(uuid, Key.ED_EVENT_DELETE);
        if (actionName == null || actionName.isEmpty()) return;
        final Path actionsFile = plugin.getPluginDataFolder().toPath().resolve("storage").resolve("actions.json");
        if (!Files.exists(actionsFile)) return;
        try {
            final Gson gson = new GsonBuilder().setPrettyPrinting().create();
            final String content = new String(Files.readAllBytes(actionsFile));
            final JsonObject json = gson.fromJson(content, JsonObject.class);
            if (json != null && json.has(actionName)) {
                json.remove(actionName);
                Files.write(actionsFile, gson.toJson(json).getBytes());
            }
            plugin.reload();
            plugin.getActionFactory().getNamesOfActionsBeingEdited().remove(actionName);
            clearData(uuid);
        } catch (final Exception e) {
            FabricQuestsPlugin.LOGGER.error("Failed to delete action: {}", actionName, e);
        }
    }

    @Override
    public void saveAction(UUID uuid) {
        final String actionName = (String) SessionData.get(uuid, Key.A_NAME);
        if (actionName == null || actionName.isEmpty()) return;
        final Path actionsFile = plugin.getPluginDataFolder().toPath().resolve("storage").resolve("actions.json");
        try {
            final Gson gson = new GsonBuilder().setPrettyPrinting().create();
            final JsonObject json;
            if (Files.exists(actionsFile)) {
                json = gson.fromJson(new String(Files.readAllBytes(actionsFile)), JsonObject.class);
            } else {
                json = new JsonObject();
            }
            // Handle rename
            final String oldName = (String) SessionData.get(uuid, Key.A_OLD_ACTION);
            if (oldName != null && !oldName.isEmpty() && !oldName.equals(actionName) && json != null) {
                json.remove(oldName);
                final Collection<Action> loaded = plugin.getLoadedActions();
                loaded.removeIf(a -> oldName.equals(a.getName()));
            }
            final JsonObject section = new JsonObject();
            if (SessionData.get(uuid, Key.A_MESSAGE) != null) {
                section.addProperty("message", (String) SessionData.get(uuid, Key.A_MESSAGE));
            }
            if (SessionData.get(uuid, Key.A_CLEAR_INVENTORY) != null) {
                section.addProperty("clear-inventory", (Boolean) SessionData.get(uuid, Key.A_CLEAR_INVENTORY));
            }
            if (SessionData.get(uuid, Key.A_FAIL_QUEST) != null) {
                section.addProperty("fail-quest", (Boolean) SessionData.get(uuid, Key.A_FAIL_QUEST));
            }
            if (SessionData.get(uuid, Key.A_COMMANDS) != null) {
                section.add("commands", gson.toJsonTree(SessionData.get(uuid, Key.A_COMMANDS)));
            }
            if (SessionData.get(uuid, Key.A_WORLD_STORM_DURATION) != null) {
                section.addProperty("storm-duration", ((Number) SessionData.get(uuid, Key.A_WORLD_STORM_DURATION)).intValue());
            }
            if (SessionData.get(uuid, Key.A_WORLD_THUNDER_DURATION) != null) {
                section.addProperty("thunder-duration", ((Number) SessionData.get(uuid, Key.A_WORLD_THUNDER_DURATION)).intValue());
            }
            if (SessionData.get(uuid, Key.A_HUNGER) != null) {
                section.addProperty("hunger", ((Number) SessionData.get(uuid, Key.A_HUNGER)).intValue());
            }
            if (SessionData.get(uuid, Key.A_SATURATION) != null) {
                section.addProperty("saturation", ((Number) SessionData.get(uuid, Key.A_SATURATION)).intValue());
            }
            if (SessionData.get(uuid, Key.A_HEALTH) != null) {
                section.addProperty("health", ((Number) SessionData.get(uuid, Key.A_HEALTH)).floatValue());
            }
            if (SessionData.get(uuid, Key.A_TIMER) != null) {
                section.addProperty("timer", ((Number) SessionData.get(uuid, Key.A_TIMER)).intValue());
            }
            if (SessionData.get(uuid, Key.A_CANCEL_TIMER) != null) {
                section.addProperty("cancel-timer", (Boolean) SessionData.get(uuid, Key.A_CANCEL_TIMER));
            }
            if (SessionData.get(uuid, Key.A_POTION_TYPES) != null) {
                section.add("potion-effect-types", gson.toJsonTree(SessionData.get(uuid, Key.A_POTION_TYPES)));
            }
            if (SessionData.get(uuid, Key.A_POTION_DURATIONS) != null) {
                section.add("potion-effect-durations", gson.toJsonTree(SessionData.get(uuid, Key.A_POTION_DURATIONS)));
            }
            if (SessionData.get(uuid, Key.A_POTION_STRENGTH) != null) {
                section.add("potion-effect-amplifiers", gson.toJsonTree(SessionData.get(uuid, Key.A_POTION_STRENGTH)));
            }
            if (SessionData.get(uuid, Key.A_ITEMS) != null && SessionData.get(uuid, Key.A_ITEMS) instanceof LinkedList<?> itemList) {
                final com.google.gson.JsonArray itemsJson = new com.google.gson.JsonArray();
                for (final Object stack : itemList) {
                    if (stack instanceof ItemStack itemStack) {
                        itemsJson.add(FabricItemUtil.serializeToJson(itemStack));
                    }
                }
                section.add("items", itemsJson);
            }
            if (json == null) {
                final JsonObject newJson = new JsonObject();
                newJson.add(actionName, section);
                Files.write(actionsFile, gson.toJson(newJson).getBytes());
            } else {
                json.add(actionName, section);
                Files.write(actionsFile, gson.toJson(json).getBytes());
            }
            namesOfActionsBeingEdited.remove(actionName);
            plugin.reload();
            clearData(uuid);
        } catch (final Exception e) {
            FabricQuestsPlugin.LOGGER.error("Failed to save action: {}", actionName, e);
        }
    }
}
