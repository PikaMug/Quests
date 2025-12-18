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
