/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.events.quester;

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;

public class FabricQuesterPreUpdateObjectiveEvent extends FabricQuesterEvent {

    private final Quest quest;
    private final int progress;
    private final int goal;

    public FabricQuesterPreUpdateObjectiveEvent(final FabricQuestsPlugin plugin, final Quester quester,
                                                final Quest quest, final int progress, final int goal) {
        super(plugin, quester);
        this.quest = quest;
        this.progress = progress;
        this.goal = goal;
    }

    public Quest getQuest() {
        return quest;
    }

    public int getProgress() {
        return progress;
    }

    public int getGoal() {
        return goal;
    }
}