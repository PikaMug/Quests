/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.tasks;

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.player.FabricQuester;
import me.pikamug.quests.quests.Quest;

public class FabricStageTimer implements Runnable {

    private final FabricQuestsPlugin plugin;
    private final FabricQuester quester;
    private final Quest quest;
    private final int delaySeconds;
    private int tickCount = 0;
    private boolean cancelled = false;

    public FabricStageTimer(FabricQuestsPlugin plugin, FabricQuester quester, Quest quest, int delaySeconds) {
        this.plugin = plugin;
        this.quester = quester;
        this.quest = quest;
        this.delaySeconds = delaySeconds;
    }

    @Override
    public void run() {
        if (cancelled) return;
        if (!quester.getCurrentQuests().containsKey(quest)) {
            cancelled = true;
            return;
        }
        tickCount++;
        if (tickCount >= delaySeconds * 20) {
            cancelled = true;
            // Auto-complete stage
            quester.checkQuest(quest);
        }
    }

    public void cancel() {
        this.cancelled = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
