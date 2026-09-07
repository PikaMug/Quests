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

import me.pikamug.quests.player.FabricQuester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.util.FabricLang;
import me.pikamug.quests.util.FabricMiscUtil;

public class FabricActionTimer implements Runnable {

    private final FabricQuester quester;
    private final Quest quest;
    private final int time;
    private boolean cancelled = false;

    public FabricActionTimer(FabricQuester quester, Quest quest, int seconds) {
        this.quester = quester;
        this.quest = quest;
        this.time = seconds;
    }

    @Override
    public void run() {
        if (cancelled) return;
        quester.getActionTimers().remove(this);
        if (time < 1) {
            quest.failQuest(quester);
            quester.updateJournal();
        } else {
            quester.sendMessage(FabricLang.get("timerMessage")
                    .replace("<time>", FabricMiscUtil.formatTime(time * 1000L))
                    .replace("<quest>", quest.getName()));
        }
    }

    public void cancel() {
        this.cancelled = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }
}