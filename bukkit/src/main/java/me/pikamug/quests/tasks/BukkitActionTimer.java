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

import com.tcoded.folialib.wrapper.task.WrappedTask;
import me.pikamug.quests.player.BukkitQuester;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.quests.util.BukkitMiscUtil;
import org.bukkit.ChatColor;

public class BukkitActionTimer implements Runnable {

    private final Quester quester;
    private final Quest quest;
    private final int time;
    private WrappedTask task;

    public BukkitActionTimer(final Quester quester, final Quest quest, final int time) {
        this.quester = quester;
        this.quest = quest;
        this.time = time;
    }

    public void setTask(final WrappedTask task) {
        this.task = task;
    }

    @Override
    public void run() {
        final BukkitQuester q = (BukkitQuester) quester;
        q.removeTimer(task);
        if (time < 1) {
            quest.failQuest(q, false);
            q.updateJournal();
        } else if (q.getOfflinePlayer().isOnline()){
            q.getPlayer().sendMessage(ChatColor.GREEN + BukkitLang.get(q.getPlayer(), "timerMessage")
                    .replace("<time>", BukkitMiscUtil.getTime(time * 1000L)).replace("<quest>", quest.getName()));
        }
    }
}
