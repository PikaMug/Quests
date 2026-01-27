/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionData {
    public static Map<UUID, Map<Object, Object>> data = new HashMap<>();

    public static Object get(final UUID uuid, final Object key) {
        final Map<Object, Object> map = data.get(uuid);
        if (map == null) {
            return null;
        }
        return map.get(key);
    }

    public static void set(final UUID uuid, final Object key, final Object value) {
        final Map<Object, Object> map = data.computeIfAbsent(uuid, k -> new HashMap<>());
        map.put(key, value);
    }

    public static void remove(final UUID uuid) {
        data.remove(uuid);
    }
}
