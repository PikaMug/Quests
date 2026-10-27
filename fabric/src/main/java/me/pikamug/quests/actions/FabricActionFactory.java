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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.convo.actions.main.FabricActionMainPrompt;
import me.pikamug.quests.util.FabricConfigUtil;
import me.pikamug.quests.util.FabricItemUtil;
import me.pikamug.quests.util.FabricLocation;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.SessionData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FabricActionFactory implements ActionFactory {

    private final FabricQuestsPlugin plugin;
    private ConcurrentHashMap<UUID, FabricLocation> selectedExplosionLocations = new ConcurrentHashMap<>();
    private ConcurrentHashMap<UUID, FabricLocation> selectedEffectLocations = new ConcurrentHashMap<>();
    private ConcurrentHashMap<UUID, FabricLocation> selectedMobLocations = new ConcurrentHashMap<>();
    private ConcurrentHashMap<UUID, FabricLocation> selectedLightningLocations = new ConcurrentHashMap<>();
    private ConcurrentHashMap<UUID, FabricLocation> selectedTeleportLocations = new ConcurrentHashMap<>();
    private List<String> editingActionNames = new LinkedList<>();

    public FabricActionFactory(FabricQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    public ConcurrentHashMap<UUID, FabricLocation> getSelectedExplosionLocations() {
        return selectedExplosionLocations;
    }

    public void setSelectedExplosionLocations(final ConcurrentHashMap<UUID, FabricLocation> selectedExplosionLocations) {
        this.selectedExplosionLocations = selectedExplosionLocations;
    }

    public ConcurrentHashMap<UUID, FabricLocation> getSelectedEffectLocations() {
        return selectedEffectLocations;
    }

    public void setSelectedEffectLocations(final ConcurrentHashMap<UUID, FabricLocation> selectedEffectLocations) {
        this.selectedEffectLocations = selectedEffectLocations;
    }

    public ConcurrentHashMap<UUID, FabricLocation> getSelectedMobLocations() {
        return selectedMobLocations;
    }

    public void setSelectedMobLocations(final ConcurrentHashMap<UUID, FabricLocation> selectedMobLocations) {
        this.selectedMobLocations = selectedMobLocations;
    }

    public ConcurrentHashMap<UUID, FabricLocation> getSelectedLightningLocations() {
        return selectedLightningLocations;
    }

    public void setSelectedLightningLocations(final ConcurrentHashMap<UUID, FabricLocation> selectedLightningLocations) {
        this.selectedLightningLocations = selectedLightningLocations;
    }

    public ConcurrentHashMap<UUID, FabricLocation> getSelectedTeleportLocations() {
        return selectedTeleportLocations;
    }

    public void setSelectedTeleportLocations(final ConcurrentHashMap<UUID, FabricLocation> selectedTeleportLocations) {
        this.selectedTeleportLocations = selectedTeleportLocations;
    }

    public List<String> getNamesOfActionsBeingEdited() {
        return editingActionNames;
    }

    public void setNamesOfActionsBeingEdited(final Collection<String> actionNames) {
        this.editingActionNames = new LinkedList<>(actionNames);
    }

    public void returnToMenu(final UUID uuid) {
        new FabricActionMainPrompt(uuid).start();
    }

    @Override
    public void loadData(UUID uuid, Action action) {
        if (!(action instanceof FabricAction)) {
            return;
        }
        FabricAction fabricAction = (FabricAction) action;
        if (fabricAction.getMessage() != null) {
            SessionData.set(uuid, Key.A_MESSAGE, fabricAction.getMessage());
        }
        SessionData.set(uuid, Key.A_CLEAR_INVENTORY, fabricAction.isClearInv());
        SessionData.set(uuid, Key.A_FAIL_QUEST, fabricAction.isFailQuest());
        if (fabricAction.getItems() != null && !fabricAction.getItems().isEmpty()) {
            SessionData.set(uuid, Key.A_ITEMS, fabricAction.getItems());
        }
        if (fabricAction.getExplosions() != null && !fabricAction.getExplosions().isEmpty()) {
            final LinkedList<String> locs = new LinkedList<>();
            for (final FabricLocation loc : fabricAction.getExplosions()) {
                locs.add(FabricConfigUtil.getLocationInfo(loc));
            }
            SessionData.set(uuid, Key.A_EXPLOSIONS, locs);
        }
        if (fabricAction.getEffects() != null && !fabricAction.getEffects().isEmpty()) {
            final LinkedList<String> locs = new LinkedList<>();
            final LinkedList<String> effs = new LinkedList<>();
            for (final Map.Entry<FabricLocation, SoundEvent> e : fabricAction.getEffects().entrySet()) {
                locs.add(FabricConfigUtil.getLocationInfo(e.getKey()));
                effs.add(e.getValue().toString());
            }
            SessionData.set(uuid, Key.A_EFFECTS, effs);
            SessionData.set(uuid, Key.A_EFFECTS_LOCATIONS, locs);
        }
        if (fabricAction.getStormWorld() != null) {
            SessionData.set(uuid, Key.A_WORLD_STORM, fabricAction.getStormWorld().dimension().identifier());
            SessionData.set(uuid, Key.A_WORLD_STORM_DURATION, fabricAction.getStormDuration());
        }
        if (fabricAction.getThunderWorld() != null) {
            SessionData.set(uuid, Key.A_WORLD_THUNDER, fabricAction.getThunderWorld().dimension().identifier());
            SessionData.set(uuid, Key.A_WORLD_THUNDER_DURATION, fabricAction.getThunderDuration());
        }
        if (fabricAction.getMobSpawns() != null && !fabricAction.getMobSpawns().isEmpty()) {
            SessionData.set(uuid, Key.A_MOBS, fabricAction.getMobSpawns());
        }
        if (fabricAction.getLightningStrikes() != null && !fabricAction.getLightningStrikes().isEmpty()) {
            final LinkedList<String> locs = new LinkedList<>();
            for (final FabricLocation loc : fabricAction.getLightningStrikes()) {
                locs.add(FabricConfigUtil.getLocationInfo(loc));
            }
            SessionData.set(uuid, Key.A_LIGHTNING, locs);
        }
        if (fabricAction.getPotionEffects() != null && !fabricAction.getPotionEffects().isEmpty()) {
            final LinkedList<String> types = new LinkedList<>();
            final LinkedList<Long> durations = new LinkedList<>();
            final LinkedList<Integer> strengths = new LinkedList<>();
            for (final MobEffectInstance pe : fabricAction.getPotionEffects()) {
                types.add(pe.getEffect().value().getDescriptionId()
                        .replaceFirst("effect\\.minecraft\\.", "").toUpperCase());
                durations.add((long) pe.getDuration());
                strengths.add(pe.getAmplifier());
            }
            SessionData.set(uuid, Key.A_POTION_TYPES, types);
            SessionData.set(uuid, Key.A_POTION_DURATIONS, durations);
            SessionData.set(uuid, Key.A_POTION_STRENGTH, strengths);
        }
        if (fabricAction.getHunger() != 0) {
            SessionData.set(uuid, Key.A_HUNGER, fabricAction.getHunger());
        }
        if (fabricAction.getSaturation() != 0) {
            SessionData.set(uuid, Key.A_SATURATION, fabricAction.getSaturation());
        }
        if (fabricAction.getHealth() > 0) {
            SessionData.set(uuid, Key.A_HEALTH, fabricAction.getHealth());
        }
        if (fabricAction.getTeleport() != null) {
            SessionData.set(uuid, Key.A_TELEPORT, FabricConfigUtil.getLocationInfo(fabricAction.getTeleport()));
        }
        if (fabricAction.getCommands() != null && !fabricAction.getCommands().isEmpty()) {
            SessionData.set(uuid, Key.A_COMMANDS, fabricAction.getCommands());
        }
        if (fabricAction.getTimer() > 0) {
            SessionData.set(uuid, Key.A_TIMER, fabricAction.getTimer());
        }
        if (fabricAction.isCancelTimer()) {
            SessionData.set(uuid, Key.A_CANCEL_TIMER, true);
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
            if (SessionData.get(uuid, Key.A_ITEMS) != null && SessionData.get(uuid, Key.A_ITEMS) instanceof LinkedList) {
                @SuppressWarnings("unchecked")
                LinkedList<?> itemList = (LinkedList<?>) SessionData.get(uuid, Key.A_ITEMS);
                final com.google.gson.JsonArray itemsJson = new com.google.gson.JsonArray();
                for (final Object stack : itemList) {
                    if (stack instanceof ItemStack) {
                        ItemStack itemStack = (ItemStack) stack;
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
            editingActionNames.remove(actionName);
            plugin.reload();
            clearData(uuid);
        } catch (final Exception e) {
            FabricQuestsPlugin.LOGGER.error("Failed to save action: {}", actionName, e);
        }
    }
}
