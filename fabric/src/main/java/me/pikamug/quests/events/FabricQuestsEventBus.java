/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.events;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class FabricQuestsEventBus {

    private static final Map<Class<? extends FabricQuestEvent>, List<Consumer<FabricQuestEvent>>> HANDLERS
            = new ConcurrentHashMap<>();

    private FabricQuestsEventBus() {}

    public static void register(final Class<? extends FabricQuestEvent> type,
                                final Consumer<FabricQuestEvent> listener) {
        HANDLERS.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    public static void fire(final FabricQuestEvent event) {
        for (final Class<? extends FabricQuestEvent> type : HANDLERS.keySet()) {
            if (type.isAssignableFrom(event.getClass())) {
                for (final Consumer<FabricQuestEvent> handler : HANDLERS.get(type)) {
                    handler.accept(event);
                }
            }
        }
    }
}