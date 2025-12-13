package me.pikamug.quests.util;

import java.util.Map;
import java.util.UUID;

public class SessionData {
    public static Map<UUID, Map<Object, Object>> data;

    public static Object get(UUID uuid, Object key) {
        Map<Object, Object> map = data.get(uuid);
        return map.get(key);
    }

    public static void set(UUID uuid, Object key, Object value) {
        Map<Object, Object> map = data.get(uuid);
        map.put(key, value);
        data.put(uuid, map);
    }
}
