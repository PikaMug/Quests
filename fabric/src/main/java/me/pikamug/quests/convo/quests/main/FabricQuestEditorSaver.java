/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.convo.quests.main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.actions.Action;
import me.pikamug.quests.conditions.Condition;
import me.pikamug.quests.util.FabricItemUtil;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.SessionData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/**
 * Writes a quest being edited in the in-game editor to the JSON storage schema
 * understood by {@link me.pikamug.quests.storage.implementation.file.FabricQuestJsonStorage}.
 * Every value is read from the editor's session data using the {@link Key} constants.
 */
public final class FabricQuestEditorSaver {

    private FabricQuestEditorSaver() {
    }

    /**
     * @param questId the numeric id of the quest being saved
     * @return the quest record written to the {@code quests.json} index (true) or {@code false}
     *         if the quest has no name set
     */
    public static boolean save(UUID uuid, String questId, FabricQuestsPlugin plugin) throws IOException {
        final JsonObject questData = buildQuest(uuid);
        if (questData == null) {
            return false;
        }
        final Path storageDir = plugin.getPluginDataFolder().toPath().resolve("storage");
        if (!Files.exists(storageDir)) {
            Files.createDirectories(storageDir);
        }
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        // Keep the combined index in sync with the edited quest
        final Path index = storageDir.resolve("quests.json");
        JsonObject root = new JsonObject();
        if (Files.exists(index)) {
            try (Reader reader = Files.newBufferedReader(index)) {
                final var parsed = JsonParser.parseReader(reader);
                if (parsed.isJsonObject()) {
                    root = parsed.getAsJsonObject();
                }
            }
        }
        root.add(questId, questData);
        try (Writer writer = Files.newBufferedWriter(index)) {
            gson.toJson(root, writer);
        }
        return true;
    }

    private static JsonObject buildQuest(UUID uuid) {
        final String name = (String) SessionData.get(uuid, Key.Q_NAME);
        if (name == null || name.isEmpty()) {
            return null;
        }
        final JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("ask-message", (String) SessionData.get(uuid, Key.Q_ASK_MESSAGE));
        json.addProperty("finish-message", (String) SessionData.get(uuid, Key.Q_FINISH_MESSAGE));
        if (SessionData.get(uuid, Key.Q_START_NPC) != null) {
            json.addProperty("npc-giver-uuid", (String) SessionData.get(uuid, Key.Q_START_NPC));
        }
        if (SessionData.get(uuid, Key.Q_START_BLOCK) != null) {
            final BlockPos pos = (BlockPos) SessionData.get(uuid, Key.Q_START_BLOCK);
            json.addProperty("block-start-x", pos.getX());
            json.addProperty("block-start-y", pos.getY());
            json.addProperty("block-start-z", pos.getZ());
        }
        if (SessionData.get(uuid, Key.Q_REGION) != null) {
            json.addProperty("region", (String) SessionData.get(uuid, Key.Q_REGION));
        }
        if (SessionData.get(uuid, Key.Q_GUIDISPLAY) instanceof ItemStack display) {
            json.add("gui-display", FabricItemUtil.serializeToJson(display));
        }

        final JsonObject requirements = buildRequirements(uuid);
        if (!requirements.isEmpty()) {
            json.add("requirements", requirements);
        }
        final JsonObject planner = buildPlanner(uuid);
        if (!planner.isEmpty()) {
            json.add("planner", planner);
        }
        final JsonObject rewards = buildRewards(uuid);
        if (!rewards.isEmpty()) {
            json.add("rewards", rewards);
        }
        final JsonObject options = buildOptions(uuid);
        if (!options.isEmpty()) {
            json.add("options", options);
        }

        final JsonArray stages = buildStages(uuid);
        if (!stages.isEmpty()) {
            json.add("stages", stages);
        }
        return json;
    }

    private static JsonObject buildRequirements(UUID uuid) {
        final JsonObject json = new JsonObject();
        addInt(json, "quest-points", SessionData.get(uuid, Key.REQ_QUEST_POINTS));
        addInt(json, "exp", SessionData.get(uuid, Key.REQ_EXP));
        addStringArray(json, "quests", SessionData.get(uuid, Key.REQ_QUEST));
        addStringArray(json, "quest-blocks", SessionData.get(uuid, Key.REQ_QUEST_BLOCK));
        addStringArray(json, "permissions", SessionData.get(uuid, Key.REQ_PERMISSION));
        addItemArray(json, "items", SessionData.get(uuid, Key.REQ_ITEMS));
        addBooleanArray(json, "remove-items", SessionData.get(uuid, Key.REQ_ITEMS_REMOVE));
        final Object fail = SessionData.get(uuid, Key.REQ_FAIL_MESSAGE);
        if (fail != null) {
            if (fail instanceof List<?> list) {
                addStringArray(json, "fail-requirement-message", list);
            } else {
                json.addProperty("fail-requirement-message", fail.toString());
            }
        }
        return json;
    }

    private static JsonObject buildPlanner(UUID uuid) {
        final JsonObject json = new JsonObject();
        final Object start = SessionData.get(uuid, Key.PLN_START_DATE);
        final Object end = SessionData.get(uuid, Key.PLN_END_DATE);
        if (start != null) {
            json.addProperty("start", start.toString());
        }
        if (end != null) {
            json.addProperty("end", end.toString());
        }
        addLong(json, "repeat", SessionData.get(uuid, Key.PLN_REPEAT_CYCLE));
        addLong(json, "cooldown", SessionData.get(uuid, Key.PLN_COOLDOWN));
        addBoolean(json, "override", SessionData.get(uuid, Key.PLN_OVERRIDE));
        return json;
    }

    private static JsonObject buildRewards(UUID uuid) {
        final JsonObject json = new JsonObject();
        addInt(json, "quest-points", SessionData.get(uuid, Key.REW_QUEST_POINTS));
        addInt(json, "exp", SessionData.get(uuid, Key.REW_EXP));
        addStringArray(json, "commands", SessionData.get(uuid, Key.REW_COMMAND));
        addStringArray(json, "commands-override-display", SessionData.get(uuid, Key.REW_COMMAND_OVERRIDE_DISPLAY));
        addStringArray(json, "permissions", SessionData.get(uuid, Key.REW_PERMISSION));
        addStringArray(json, "permission-worlds", SessionData.get(uuid, Key.REW_PERMISSION_WORLDS));
        addStringArray(json, "details-override", SessionData.get(uuid, Key.REW_DETAILS_OVERRIDE));
        addItemArray(json, "items", SessionData.get(uuid, Key.REW_ITEMS));
        return json;
    }

    private static JsonObject buildOptions(UUID uuid) {
        final JsonObject json = new JsonObject();
        addBoolean(json, "allow-commands", SessionData.get(uuid, Key.OPT_ALLOW_COMMANDS));
        addBoolean(json, "allow-quitting", SessionData.get(uuid, Key.OPT_ALLOW_QUITTING));
        addBoolean(json, "ignore-silk-touch", SessionData.get(uuid, Key.OPT_IGNORE_SILK_TOUCH));
        final Object party = SessionData.get(uuid, Key.OPT_EXTERNAL_PARTY_PLUGIN);
        if (party != null) {
            json.addProperty("external-party-plugin", party.toString());
        }
        addBoolean(json, "use-parties-plugin", SessionData.get(uuid, Key.OPT_USE_PARTIES_PLUGIN));
        addInt(json, "share-progress-level", SessionData.get(uuid, Key.OPT_SHARE_PROGRESS_LEVEL));
        addBoolean(json, "same-quest-only", SessionData.get(uuid, Key.OPT_SHARE_SAME_QUEST_ONLY));
        addDouble(json, "share-distance", SessionData.get(uuid, Key.OPT_SHARE_DISTANCE));
        addBoolean(json, "handle-offline-players", SessionData.get(uuid, Key.OPT_HANDLE_OFFLINE_PLAYERS));
        addBoolean(json, "ignore-block-replace", SessionData.get(uuid, Key.OPT_IGNORE_BLOCK_REPLACE));
        addBoolean(json, "give-at-login", SessionData.get(uuid, Key.OPT_GIVE_GLOBALLY_AT_LOGIN));
        addBoolean(json, "allow-stacking-global", SessionData.get(uuid, Key.OPT_ALLOW_STACKING_GLOBAL));
        addBoolean(json, "inform-on-start", SessionData.get(uuid, Key.OPT_INFORM_QUEST_START));
        addBoolean(json, "override-max-quests", SessionData.get(uuid, Key.OPT_OVERRIDE_MAX_QUESTS));
        return json;
    }

    private static JsonArray buildStages(UUID uuid) {
        final JsonArray array = new JsonArray();
        int num = 1;
        while (SessionData.get(uuid, "stage" + num) != null) {
            final String p = "stage" + num;
            final JsonObject json = new JsonObject();
            addString(json, "start-message", SessionData.get(uuid, p + Key.S_START_MESSAGE));
            addString(json, "complete-message", SessionData.get(uuid, p + Key.S_COMPLETE_MESSAGE));
            addLong(json, "delay", SessionData.get(uuid, p + Key.S_DELAY));
            addString(json, "delay-message", SessionData.get(uuid, p + Key.S_DELAY_MESSAGE));
            addStringArray(json, "password-displays", SessionData.get(uuid, p + Key.S_PASSWORD_DISPLAYS));
            addStringArray(json, "password-phrases", SessionData.get(uuid, p + Key.S_PASSWORD_PHRASES));

            addBlockLists(json, "break", SessionData.get(uuid, p + Key.S_BREAK_NAMES),
                    SessionData.get(uuid, p + Key.S_BREAK_AMOUNTS));
            addBlockLists(json, "damage", SessionData.get(uuid, p + Key.S_DAMAGE_NAMES),
                    SessionData.get(uuid, p + Key.S_DAMAGE_AMOUNTS));
            addBlockLists(json, "place", SessionData.get(uuid, p + Key.S_PLACE_NAMES),
                    SessionData.get(uuid, p + Key.S_PLACE_AMOUNTS));
            addBlockLists(json, "use", SessionData.get(uuid, p + Key.S_USE_NAMES),
                    SessionData.get(uuid, p + Key.S_USE_AMOUNTS));
            addBlockLists(json, "cut", SessionData.get(uuid, p + Key.S_CUT_NAMES),
                    SessionData.get(uuid, p + Key.S_CUT_AMOUNTS));

            addItemArray(json, "items-to-craft", SessionData.get(uuid, p + Key.S_CRAFT_ITEMS));
            addItemArray(json, "items-to-smelt", SessionData.get(uuid, p + Key.S_SMELT_ITEMS));
            addItemArray(json, "items-to-enchant", SessionData.get(uuid, p + Key.S_ENCHANT_ITEMS));
            addItemArray(json, "items-to-brew", SessionData.get(uuid, p + Key.S_BREW_ITEMS));
            addItemArray(json, "items-to-consume", SessionData.get(uuid, p + Key.S_CONSUME_ITEMS));
            addItemArray(json, "items-to-deliver", SessionData.get(uuid, p + Key.S_DELIVERY_ITEMS));
            addStringArray(json, "npc-delivery-uuids", SessionData.get(uuid, p + Key.S_DELIVERY_NPCS));
            addStringArray(json, "delivery-messages", SessionData.get(uuid, p + Key.S_DELIVERY_MESSAGES));

            addStringArray(json, "mobs-to-kill", SessionData.get(uuid, p + Key.S_MOB_TYPES));
            addIntArray(json, "mob-amounts", SessionData.get(uuid, p + Key.S_MOB_AMOUNTS));
            addStringArray(json, "npc-uuids-to-talk-to", SessionData.get(uuid, p + Key.S_NPCS_TO_TALK_TO));
            addStringArray(json, "npc-uuids-to-kill", SessionData.get(uuid, p + Key.S_NPCS_TO_KILL));
            addIntArray(json, "npc-kill-amounts", SessionData.get(uuid, p + Key.S_NPCS_TO_KILL_AMOUNTS));
            addInt(json, "players-to-kill", SessionData.get(uuid, p + Key.S_PLAYER_KILL));
            addStringArray(json, "locations-to-kill", SessionData.get(uuid, p + Key.S_MOB_KILL_LOCATIONS));
            addIntArray(json, "kill-location-radii", SessionData.get(uuid, p + Key.S_MOB_KILL_LOCATIONS_RADIUS));
            addStringArray(json, "kill-location-names", SessionData.get(uuid, p + Key.S_MOB_KILL_LOCATIONS_NAMES));
            addStringArray(json, "locations-to-reach", SessionData.get(uuid, p + Key.S_REACH_LOCATIONS));
            addIntArray(json, "reach-location-radii", SessionData.get(uuid, p + Key.S_REACH_LOCATIONS_RADIUS));
            addStringArray(json, "reach-location-names", SessionData.get(uuid, p + Key.S_REACH_LOCATIONS_NAMES));
            addStringArray(json, "mobs-to-tame", SessionData.get(uuid, p + Key.S_TAME_TYPES));
            addIntArray(json, "mob-tame-amounts", SessionData.get(uuid, p + Key.S_TAME_AMOUNTS));
            addStringArray(json, "sheep-to-shear", SessionData.get(uuid, p + Key.S_SHEAR_COLORS));
            addIntArray(json, "sheep-amounts", SessionData.get(uuid, p + Key.S_SHEAR_AMOUNTS));
            addInt(json, "fish-to-catch", SessionData.get(uuid, p + Key.S_FISH));
            addInt(json, "cows-to-milk", SessionData.get(uuid, p + Key.S_COW_MILK));
            addStringArray(json, "objective-override", SessionData.get(uuid, p + Key.S_OVERRIDE_DISPLAY));

            addAction(json, "start-event", SessionData.get(uuid, p + Key.S_START_EVENT));
            addAction(json, "finish-event", SessionData.get(uuid, p + Key.S_FINISH_EVENT));
            addAction(json, "fail-event", SessionData.get(uuid, p + Key.S_FAIL_EVENT));
            addAction(json, "death-event", SessionData.get(uuid, p + Key.S_DEATH_EVENT));
            addAction(json, "disconnect-event", SessionData.get(uuid, p + Key.S_DISCONNECT_EVENT));
            addTriggeredActions(json, "chat-events", "chat-event-triggers",
                    SessionData.get(uuid, p + Key.S_CHAT_EVENTS), SessionData.get(uuid, p + Key.S_CHAT_EVENT_TRIGGERS));
            addTriggeredActions(json, "command-events", "command-event-triggers",
                    SessionData.get(uuid, p + Key.S_COMMAND_EVENTS), SessionData.get(uuid, p + Key.S_COMMAND_EVENT_TRIGGERS));
            final Object condition = SessionData.get(uuid, p + Key.S_CONDITION);
            if (condition != null) {
                json.addProperty("condition", nameOf(condition));
            }
            array.add(json);
            num++;
        }
        return array;
    }

    private static void addBlockLists(JsonObject json, String type, Object names, Object amounts) {
        addStringArray(json, type + "-block-names", names);
        addIntArray(json, type + "-block-amounts", amounts);
    }

    private static void addTriggeredActions(JsonObject json, String eventsKey, String triggersKey,
                                            Object events, Object triggers) {
        if (events instanceof List<?> actions && triggers instanceof List<?> phraseList) {
            final JsonArray ev = new JsonArray();
            for (final Object a : actions) {
                ev.add(nameOf(a));
            }
            final JsonArray tr = new JsonArray();
            for (final Object t : phraseList) {
                tr.add(t.toString());
            }
            if (!ev.isEmpty()) {
                json.add(eventsKey, ev);
                json.add(triggersKey, tr);
            }
        }
    }

    private static String nameOf(Object value) {
        if (value instanceof Action action) {
            return action.getName();
        }
        if (value instanceof Condition condition) {
            return condition.getName();
        }
        return value.toString();
    }

    private static void addAction(JsonObject json, String key, Object value) {
        if (value != null) {
            json.addProperty(key, nameOf(value));
        }
    }

    private static void addString(JsonObject json, String key, Object value) {
        if (value != null) {
            json.addProperty(key, value.toString());
        }
    }

    private static void addInt(JsonObject json, String key, Object value) {
        if (value instanceof Number n) {
            json.addProperty(key, n.intValue());
        }
    }

    private static void addLong(JsonObject json, String key, Object value) {
        if (value instanceof Number n) {
            json.addProperty(key, n.longValue());
        }
    }

    private static void addDouble(JsonObject json, String key, Object value) {
        if (value instanceof Number n) {
            json.addProperty(key, n.doubleValue());
        }
    }

    private static void addBoolean(JsonObject json, String key, Object value) {
        if (value instanceof Boolean b) {
            json.addProperty(key, b);
        }
    }

    private static void addStringArray(JsonObject json, String key, Object value) {
        if (!(value instanceof List<?> list) || list.isEmpty()) {
            return;
        }
        final JsonArray array = new JsonArray();
        for (final Object o : list) {
            if (o != null) {
                array.add(o.toString());
            }
        }
        if (!array.isEmpty()) {
            json.add(key, array);
        }
    }

    private static void addIntArray(JsonObject json, String key, Object value) {
        if (!(value instanceof List<?> list) || list.isEmpty()) {
            return;
        }
        final JsonArray array = new JsonArray();
        for (final Object o : list) {
            if (o instanceof Number n) {
                array.add(n.intValue());
            }
        }
        if (!array.isEmpty()) {
            json.add(key, array);
        }
    }

    private static void addBooleanArray(JsonObject json, String key, Object value) {
        if (!(value instanceof List<?> list) || list.isEmpty()) {
            return;
        }
        final JsonArray array = new JsonArray();
        for (final Object o : list) {
            if (o instanceof Boolean b) {
                array.add(b);
            }
        }
        if (!array.isEmpty()) {
            json.add(key, array);
        }
    }

    private static void addItemArray(JsonObject json, String key, Object value) {
        if (!(value instanceof List<?> list) || list.isEmpty()) {
            return;
        }
        final JsonArray array = new JsonArray();
        for (final Object o : list) {
            if (o instanceof ItemStack stack) {
                array.add(FabricItemUtil.serializeToJson(stack));
            }
        }
        if (!array.isEmpty()) {
            json.add(key, array);
        }
    }
}