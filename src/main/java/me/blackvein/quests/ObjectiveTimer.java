/*******************************************************************************************************
 * Continued by FlyingPikachu/HappyPikachu with permission from _Blackvein_. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

package me.blackvein.quests;

import org.bukkit.scheduler.BukkitRunnable;

import me.blackvein.quests.util.Lang;

public class ObjectiveTimer extends BukkitRunnable {

    Quester quester;
    Quests plugin;
    Quest quest;
    private int time;
    private boolean last;

    ObjectiveTimer(Quests plugin, Quester quester, Quest quest, int time, boolean last) {
        this.quester = quester;
        this.quest = quest;
        this.plugin = plugin;
        this.time = time;
        this.last = last;
    }

    @Override
    public void run() {
        quester.timers.remove(getTaskId());
        if (last) {
            quest.failQuest(quester);
            quester.updateJournal();
        } else {
            quester.getPlayer().sendMessage(Lang.get("timerMessage").replaceAll("<time>", String.valueOf(time)));
        }
    }
}
