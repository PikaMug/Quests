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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.convo.conditions.main.FabricConditionMainPrompt;
import me.pikamug.quests.util.FabricItemUtil;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.SessionData;
import net.minecraft.world.item.ItemStack;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class FabricConditionFactory implements ConditionFactory {

    private final FabricQuestsPlugin plugin;
    private List<String> namesOfConditionsBeingEdited = new LinkedList<>();

    public FabricConditionFactory(final FabricQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    public List<String> getNamesOfConditionsBeingEdited() {
        return namesOfConditionsBeingEdited;
    }

    public void setNamesOfConditionsBeingEdited(final Collection<String> conditionNames) {
        namesOfConditionsBeingEdited = new LinkedList<>(conditionNames);
    }

    public void returnToMenu(UUID uuid) {
        new FabricConditionMainPrompt(uuid).start();
    }

    public void loadData(UUID uuid, Condition condition) {
        if (!(condition instanceof FabricCondition)) {
            return;
        }
        FabricCondition fabricCondition = (FabricCondition) condition;
        if (fabricCondition.isFailQuest()) {
            SessionData.set(uuid, Key.C_FAIL_QUEST, true);
        } else {
            SessionData.set(uuid, Key.C_FAIL_QUEST, false);
        }
        if (fabricCondition.getEntitiesWhileRiding() != null && !fabricCondition.getEntitiesWhileRiding().isEmpty()) {
            SessionData.set(uuid, Key.C_WHILE_RIDING_ENTITY, new LinkedList<>(fabricCondition.getEntitiesWhileRiding()));
        }
        if (fabricCondition.getNpcsWhileRiding() != null && !fabricCondition.getNpcsWhileRiding().isEmpty()) {
            SessionData.set(uuid, Key.C_WHILE_RIDING_NPC, new LinkedList<>(fabricCondition.getNpcsWhileRiding()));
        }
        if (fabricCondition.getPermissions() != null && !fabricCondition.getPermissions().isEmpty()) {
            SessionData.set(uuid, Key.C_WHILE_PERMISSION, new LinkedList<>(fabricCondition.getPermissions()));
        }
        if (fabricCondition.getWorldsWhileStayingWithin() != null && !fabricCondition.getWorldsWhileStayingWithin().isEmpty()) {
            SessionData.set(uuid, Key.C_WHILE_WITHIN_WORLD, new LinkedList<>(fabricCondition.getWorldsWhileStayingWithin()));
        }
        if (fabricCondition.getTickStartWhileStayingWithin() > -1) {
            SessionData.set(uuid, Key.C_WHILE_WITHIN_TICKS_START, fabricCondition.getTickStartWhileStayingWithin());
        }
        if (fabricCondition.getTickEndWhileStayingWithin() > -1) {
            SessionData.set(uuid, Key.C_WHILE_WITHIN_TICKS_END, fabricCondition.getTickEndWhileStayingWithin());
        }
        if (fabricCondition.getBiomesWhileStayingWithin() != null && !fabricCondition.getBiomesWhileStayingWithin().isEmpty()) {
            SessionData.set(uuid, Key.C_WHILE_WITHIN_BIOME, new LinkedList<>(fabricCondition.getBiomesWhileStayingWithin()));
        }
        if (fabricCondition.getRegionsWhileStayingWithin() != null && !fabricCondition.getRegionsWhileStayingWithin().isEmpty()) {
            SessionData.set(uuid, Key.C_WHILE_WITHIN_REGION, new LinkedList<>(fabricCondition.getRegionsWhileStayingWithin()));
        }
        if (fabricCondition.getPlaceholdersCheckIdentifier() != null && !fabricCondition.getPlaceholdersCheckIdentifier().isEmpty()) {
            SessionData.set(uuid, Key.C_WHILE_PLACEHOLDER_ID, new LinkedList<>(fabricCondition.getPlaceholdersCheckIdentifier()));
        }
        if (fabricCondition.getPlaceholdersCheckValue() != null && !fabricCondition.getPlaceholdersCheckValue().isEmpty()) {
            SessionData.set(uuid, Key.C_WHILE_PLACEHOLDER_VAL, new LinkedList<>(fabricCondition.getPlaceholdersCheckValue()));
        }
        if (fabricCondition.getItemsWhileHoldingMainHand() != null && !fabricCondition.getItemsWhileHoldingMainHand().isEmpty()) {
            SessionData.set(uuid, Key.C_WHILE_HOLDING_MAIN_HAND, new LinkedList<>(fabricCondition.getItemsWhileHoldingMainHand()));
        }
        if (fabricCondition.getItemsWhileWearing() != null && !fabricCondition.getItemsWhileWearing().isEmpty()) {
            SessionData.set(uuid, Key.C_WHILE_WEARING, new LinkedList<>(fabricCondition.getItemsWhileWearing()));
        }
    }

    public void clearData(final UUID uuid) {
        SessionData.clear(uuid);
    }

    public void deleteCondition(final UUID uuid) {
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
            plugin.getPluginLogger().error("Failed to delete condition: {}", conditionName, e);
        }
    }

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
            if (SessionData.get(uuid, Key.C_WHILE_RIDING_NPC) != null) {
                section.add("ride-npc-uuid", gson.toJsonTree(SessionData.get(uuid, Key.C_WHILE_RIDING_NPC)));
            }
            if (SessionData.get(uuid, Key.C_WHILE_PERMISSION) != null) {
                section.add("permission", gson.toJsonTree(SessionData.get(uuid, Key.C_WHILE_PERMISSION)));
            }
            writeItemsToJson(uuid, section, Key.C_WHILE_HOLDING_MAIN_HAND, "hold-main-hand");
            writeItemsToJson(uuid, section, Key.C_WHILE_WEARING, "wear");
            if (SessionData.get(uuid, Key.C_WHILE_WITHIN_WORLD) != null) {
                section.add("stay-within-world", gson.toJsonTree(SessionData.get(uuid, Key.C_WHILE_WITHIN_WORLD)));
            }
            final Object tickStart = SessionData.get(uuid, Key.C_WHILE_WITHIN_TICKS_START);
            final Object tickEnd = SessionData.get(uuid, Key.C_WHILE_WITHIN_TICKS_END);
            if (tickStart != null || tickEnd != null) {
                final JsonObject ticks = new JsonObject();
                if (tickStart != null) {
                    ticks.addProperty("start", ((Number) tickStart).intValue());
                }
                if (tickEnd != null) {
                    ticks.addProperty("end", ((Number) tickEnd).intValue());
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
            plugin.getPluginLogger().error("Failed to save condition: {}", conditionName, e);
        }
    }

    @SuppressWarnings("unchecked")
    private void writeItemsToJson(final UUID uuid, final JsonObject section, final String key, final String nodeKey) {
        final LinkedList<ItemStack> data = (LinkedList<ItemStack>) SessionData.get(uuid, key);
        if (data != null) {
            final com.google.gson.JsonArray itemsJson = new com.google.gson.JsonArray();
            for (final ItemStack stack : data) {
                if (stack != null) {
                    itemsJson.add(FabricItemUtil.serializeToJson(stack));
                }
            }
            section.add(nodeKey, itemsJson);
        }
    }
}
