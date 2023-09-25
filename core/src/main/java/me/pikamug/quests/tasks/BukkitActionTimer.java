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

import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.quests.util.BukkitMiscUtil;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

public class BukkitActionTimer extends BukkitRunnable {

    private final Quester quester;
    private final Quest quest;
    private final int time;

    public BukkitActionTimer(final Quester quester, final Quest quest, final int time) {
        this.quester = quester;
        this.quest = quest;
        this.time = time;
    }

    @Override
    public void run() {
        quester.removeTimer(getTaskId());
        if (time < 1) {
            quest.failQuest(quester, false);
            quester.updateJournal();
        } else {
            quester.getPlayer().sendMessage(ChatColor.GREEN + BukkitLang.get(quester.getPlayer(), "timerMessage")
                    .replace("<time>", ChatColor.RED + BukkitMiscUtil.getTime(time * 1000L) + ChatColor.GREEN)
                    .replace("<quest>", ChatColor.GOLD + quest.getName() + ChatColor.GREEN));
        }
    }
}
