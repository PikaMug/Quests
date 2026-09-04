/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.conditions;

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.util.FabricItemUtil;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.SessionData;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FabricConditionFactory implements ConditionFactory {

    private final FabricQuestsPlugin plugin;
    private final List<String> namesOfConditionsBeingEdited = Collections.synchronizedList(new ArrayList<>());

    public FabricConditionFactory(FabricQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override public List<String> getNamesOfConditionsBeingEdited() { return namesOfConditionsBeingEdited; }
    @Override public void setNamesOfConditionsBeingEdited(Collection<String> v) { namesOfConditionsBeingEdited.clear(); namesOfConditionsBeingEdited.addAll(v); }

    @Override
    public void returnToMenu(UUID uuid) {
        SessionData.clear(uuid);
    }

    @Override
    public void loadData(UUID uuid, Condition condition) {
        if (!(condition instanceof FabricCondition fabCondition)) return;
        if (fabCondition.getName() != null) {
            SessionData.set(uuid, Key.C_NAME, fabCondition.getName());
        }
        SessionData.set(uuid, Key.C_FAIL_QUEST, fabCondition.isFailQuest());
        if (fabCondition.getEntitiesWhileRiding() != null && !fabCondition.getEntitiesWhileRiding().isEmpty()) {
            SessionData.set(uuid, Key.C_WHILE_RIDING_ENTITY, new LinkedList<>(fabCondition.getEntitiesWhileRiding()));
        }
        if (fabCondition.getNpcsWhileRiding() != null && !fabCondition.getNpcsWhileRiding().isEmpty()) {
            SessionData.set(uuid, Key.C_WHILE_RIDING_NPC, new LinkedList<>(fabCondition.getNpcsWhileRiding()));
        }
        if (fabCondition.getPermissions() != null && !fabCondition.getPermissions().isEmpty()) {
            SessionData.set(uuid, Key.C_WHILE_PERMISSION, new LinkedList<>(fabCondition.getPermissions()));
        }
        if (fabCondition.getWorldsWhileStayingWithin() != null && !fabCondition.getWorldsWhileStayingWithin().isEmpty()) {
            SessionData.set(uuid, Key.C_WHILE_WITHIN_WORLD, new LinkedList<>(fabCondition.getWorldsWhileStayingWithin()));
        }
        if (fabCondition.getTickStartWhileStayingWithin() > 0) {
            SessionData.set(uuid, Key.C_WHILE_WITHIN_TICKS_START, fabCondition.getTickStartWhileStayingWithin());
        }
        if (fabCondition.getTickEndWhileStayingWithin() > 0) {
            SessionData.set(uuid, Key.C_WHILE_WITHIN_TICKS_END, fabCondition.getTickEndWhileStayingWithin());
        }
        if (fabCondition.getBiomesWhileStayingWithin() != null && !fabCondition.getBiomesWhileStayingWithin().isEmpty()) {
            SessionData.set(uuid, Key.C_WHILE_WITHIN_BIOME, new LinkedList<>(fabCondition.getBiomesWhileStayingWithin()));
        }
        if (fabCondition.getRegionsWhileStayingWithin() != null && !fabCondition.getRegionsWhileStayingWithin().isEmpty()) {
            SessionData.set(uuid, Key.C_WHILE_WITHIN_REGION, new LinkedList<>(fabCondition.getRegionsWhileStayingWithin()));
        }
        if (fabCondition.getPlaceholdersCheckIdentifier() != null && !fabCondition.getPlaceholdersCheckIdentifier().isEmpty()) {
            SessionData.set(uuid, Key.C_WHILE_PLACEHOLDER_ID, new LinkedList<>(fabCondition.getPlaceholdersCheckIdentifier()));
        }
        if (fabCondition.getPlaceholdersCheckValue() != null && !fabCondition.getPlaceholdersCheckValue().isEmpty()) {
            SessionData.set(uuid, Key.C_WHILE_PLACEHOLDER_VAL, new LinkedList<>(fabCondition.getPlaceholdersCheckValue()));
        }
        if (fabCondition.getItemsWhileHoldingMainHand() != null && !fabCondition.getItemsWhileHoldingMainHand().isEmpty()) {
            SessionData.set(uuid, Key.C_WHILE_HOLDING_MAIN_HAND, new LinkedList<>(fabCondition.getItemsWhileHoldingMainHand()));
        }
        if (fabCondition.getItemsWhileWearing() != null && !fabCondition.getItemsWhileWearing().isEmpty()) {
            SessionData.set(uuid, Key.C_WHILE_WEARING, new LinkedList<>(fabCondition.getItemsWhileWearing()));
        }
    }

    @Override
    public void clearData(UUID uuid) {
        SessionData.clear(uuid);
    }

    @Override
    public void deleteCondition(UUID uuid) {
        final String conditionName = (String) SessionData.get(uuid, Key.ED_CONDITION_DELETE);
        if (conditionName == null || conditionName.isEmpty()) return;
        final Path conditionsFile = plugin.getPluginDataFolder().toPath().resolve("storage").resolve("conditions.json");
        if (!Files.exists(conditionsFile)) return;
        try {
            final Gson gson = new GsonBuilder().setPrettyPrinting().create();
            final String content = new String(Files.readAllBytes(conditionsFile));
            final JsonObject json = gson.fromJson(content, JsonObject.class);
            if (json != null && json.has(conditionName)) {
                json.remove(conditionName);
                Files.write(conditionsFile, gson.toJson(json).getBytes());
            }
            plugin.reload();
            plugin.getConditionFactory().getNamesOfConditionsBeingEdited().remove(conditionName);
            clearData(uuid);
        } catch (final Exception e) {
            FabricQuestsPlugin.LOGGER.error("Failed to delete condition: {}", conditionName, e);
        }
    }

    @Override
    public void saveCondition(UUID uuid) {
        final String conditionName = (String) SessionData.get(uuid, Key.C_NAME);
        if (conditionName == null || conditionName.isEmpty()) return;
        final Path conditionsFile = plugin.getPluginDataFolder().toPath().resolve("storage").resolve("conditions.json");
        try {
            final Gson gson = new GsonBuilder().setPrettyPrinting().create();
            final JsonObject json;
            if (Files.exists(conditionsFile)) {
                json = gson.fromJson(new String(Files.readAllBytes(conditionsFile)), JsonObject.class);
            } else {
                json = new JsonObject();
            }
            final String oldName = (String) SessionData.get(uuid, Key.C_OLD_CONDITION);
            if (oldName != null && !oldName.isEmpty() && !oldName.equals(conditionName) && json != null) {
                json.remove(oldName);
                final Collection<Condition> loaded = plugin.getLoadedConditions();
                loaded.removeIf(c -> oldName.equals(c.getName()));
            }
            final JsonObject section = new JsonObject();
            if (SessionData.get(uuid, Key.C_FAIL_QUEST) != null) {
                section.addProperty("fail-quest", (Boolean) SessionData.get(uuid, Key.C_FAIL_QUEST));
            }
            if (SessionData.get(uuid, Key.C_WHILE_RIDING_ENTITY) != null) {
                section.add("ride-entity", gson.toJsonTree(SessionData.get(uuid, Key.C_WHILE_RIDING_ENTITY)));
            }
            if (SessionData.get(uuid, Key.C_WHILE_PERMISSION) != null) {
                section.add("permission", gson.toJsonTree(SessionData.get(uuid, Key.C_WHILE_PERMISSION)));
            }
            if (SessionData.get(uuid, Key.C_WHILE_WITHIN_WORLD) != null) {
                section.add("stay-within-world", gson.toJsonTree(SessionData.get(uuid, Key.C_WHILE_WITHIN_WORLD)));
            }
            if (SessionData.get(uuid, Key.C_WHILE_WITHIN_TICKS_START) != null) {
                final JsonObject ticks = new JsonObject();
                ticks.addProperty("start", ((Number) SessionData.get(uuid, Key.C_WHILE_WITHIN_TICKS_START)).intValue());
                if (SessionData.get(uuid, Key.C_WHILE_WITHIN_TICKS_END) != null) {
                    ticks.addProperty("end", ((Number) SessionData.get(uuid, Key.C_WHILE_WITHIN_TICKS_END)).intValue());
                }
                section.add("stay-within-ticks", ticks);
            }
            if (SessionData.get(uuid, Key.C_WHILE_WITHIN_BIOME) != null) {
                section.add("stay-within-biome", gson.toJsonTree(SessionData.get(uuid, Key.C_WHILE_WITHIN_BIOME)));
            }
            if (SessionData.get(uuid, Key.C_WHILE_WITHIN_REGION) != null) {
                section.add("stay-within-region", gson.toJsonTree(SessionData.get(uuid, Key.C_WHILE_WITHIN_REGION)));
            }
            if (SessionData.get(uuid, Key.C_WHILE_PLACEHOLDER_ID) != null) {
                section.add("check-placeholder-id", gson.toJsonTree(SessionData.get(uuid, Key.C_WHILE_PLACEHOLDER_ID)));
            }
            if (SessionData.get(uuid, Key.C_WHILE_PLACEHOLDER_VAL) != null) {
                section.add("check-placeholder-value", gson.toJsonTree(SessionData.get(uuid, Key.C_WHILE_PLACEHOLDER_VAL)));
            }
            writeItems(uuid, section, Key.C_WHILE_HOLDING_MAIN_HAND, "hold-main-hand");
            writeItems(uuid, section, Key.C_WHILE_WEARING, "wear");
            if (json == null) {
                final JsonObject newJson = new JsonObject();
                newJson.add(conditionName, section);
                Files.write(conditionsFile, gson.toJson(newJson).getBytes());
            } else {
                json.add(conditionName, section);
                Files.write(conditionsFile, gson.toJson(json).getBytes());
            }
            namesOfConditionsBeingEdited.remove(conditionName);
            plugin.reload();
            clearData(uuid);
        } catch (final Exception e) {
            FabricQuestsPlugin.LOGGER.error("Failed to save condition: {}", conditionName, e);
        }
    }

    private void writeItems(UUID uuid, JsonObject section, String key, String nodeKey) {
        final Object data = SessionData.get(uuid, key);
        if (data instanceof LinkedList<?> itemList) {
            final com.google.gson.JsonArray itemsJson = new com.google.gson.JsonArray();
            for (final Object stack : itemList) {
                if (stack instanceof ItemStack itemStack) {
                    itemsJson.add(FabricItemUtil.serializeToJson(itemStack));
                }
            }
            section.add(nodeKey, itemsJson);
        }
    }
}
