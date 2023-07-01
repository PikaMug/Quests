/*
 * Copyright (c) 2014 PikaMug and contributors. All rights reserved.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package me.pikamug.quests.tasks;

import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.util.Language;
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
            quester.getPlayer().sendMessage(ChatColor.GREEN + Language.get(quester.getPlayer(), "timerMessage")
                    .replace("<time>", ChatColor.RED + BukkitMiscUtil.getTime(time * 1000L) + ChatColor.GREEN)
                    .replace("<quest>", ChatColor.GOLD + quest.getName() + ChatColor.GREEN));
        }
    }
}
