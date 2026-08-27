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
import me.pikamug.quests.player.QuestProgress;
import me.pikamug.quests.quests.Quest;

public class FabricActionTimer implements Runnable {

    private final FabricQuestsPlugin plugin;
    private final FabricQuester quester;
    private final Quest quest;
    private int timeLeft;
    private boolean cancelled = false;

    public FabricActionTimer(FabricQuestsPlugin plugin, FabricQuester quester, Quest quest, int seconds) {
        this.plugin = plugin;
        this.quester = quester;
        this.quest = quest;
        this.timeLeft = seconds;
    }

    @Override
    public void run() {
        if (cancelled) return;
        if (!quester.getCurrentQuests().containsKey(quest)) {
            cancelled = true;
            return;
        }
        timeLeft--;
        if (timeLeft <= 0) {
            // Timer expired - fail quest
            quest.failQuest(quester);
            cancelled = true;
        }
    }

    public void cancel() {
        this.cancelled = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public int getTimeLeft() {
        return timeLeft;
    }
}
