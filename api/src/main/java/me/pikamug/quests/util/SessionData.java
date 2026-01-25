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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionData {
    public static Map<UUID, Map<Object, Object>> data = new ConcurrentHashMap<>();

    // FIXME: Memory leak waiting to happen. Please add a remove method and call it sometime
    public static Object get(UUID uuid, Object key) {
        Map<Object, Object> map = data.get(uuid);
        if (map == null) {
            return null;
        }
        return map.get(key);
    }

    public static void set(UUID uuid, Object key, Object value) {
        Map<Object, Object> map = data.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>());
        map.put(key, value);
    }
}
