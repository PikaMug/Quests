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

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public abstract class FabricQuestsEvent {
    public interface Listener<T extends FabricQuestsEvent> {
        void onEvent(final T event);
    }

    public FabricQuestsEvent() {
    }

    public FabricQuestsEvent(final boolean async) {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected static <T extends FabricQuestsEvent> Event<Listener<T>> createEvent() {
        final Event event = EventFactory.createArrayBacked(Listener.class,
                listeners -> event1 -> {
                    for (final Object listener : listeners) {
                        ((Listener<T>) listener).onEvent((T) event1);
                    }
                });
        return (Event<Listener<T>>) event;
    }
}
