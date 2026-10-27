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

import me.pikamug.quests.enums.ObjectiveType;
import me.pikamug.quests.events.quester.FabricQuesterPostUpdateObjectiveEvent;
import me.pikamug.quests.events.quester.FabricQuesterPreUpdateObjectiveEvent;
import me.pikamug.quests.player.FabricQuester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.FabricObjective;

/**
 * Delegates Quests events to the Fabric API event system. Only instantiated when
 * Fabric API is present, so its API classes are never linked otherwise.
 */
public class FabricEventAccessor {

    public FabricEventAccessor() {
        // Force event classes to initialize now so a broken Fabric API install is
        // detected during dependency setup rather than during gameplay.
        FabricQuesterPreUpdateObjectiveEvent.EVENT.getClass();
        FabricQuesterPostUpdateObjectiveEvent.EVENT.getClass();
    }

    public void firePreUpdateObjective(final FabricQuester quester, final Quest quest,
                                       final int progress, final int goal) {
        FabricQuesterPreUpdateObjectiveEvent.EVENT.invoker().onEvent(
                new FabricQuesterPreUpdateObjectiveEvent(quester, quest,
                        new FabricObjective(ObjectiveType.CUSTOM, null, progress, goal)));
    }

    public void firePostUpdateObjective(final FabricQuester quester, final Quest quest,
                                        final int progress, final int goal) {
        FabricQuesterPostUpdateObjectiveEvent.EVENT.invoker().onEvent(
                new FabricQuesterPostUpdateObjectiveEvent(quester, quest,
                        new FabricObjective(ObjectiveType.CUSTOM, null, progress, goal)));
    }
}
