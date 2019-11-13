/*******************************************************************************************************
 * Continued by PikaMug (formerly HappyPikachu) with permission from _Blackvein_. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

package me.blackvein.quests.tasks;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import me.blackvein.quests.Quest;
import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;
import me.blackvein.quests.Stage;
import me.blackvein.quests.actions.Action;
import me.blackvein.quests.util.ConfigUtil;
import me.blackvein.quests.util.Lang;

public class StageTimer implements Runnable {

    private Quester quester;
    private Quests plugin;
    private Quest quest;

    public StageTimer(Quests quests, Quester q, Quest qu) {
        quester = q;
        quest = qu;
        plugin = quests;
    }

    @Override
    public void run() {
        if (quester == null) {
            return;
        }
        if (quester.getQuestData(quest) == null) {
            return;
        }
        if (quester.getQuestData(quest).delayOver) {
            if (quest.getStages().indexOf(quester.getCurrentStage(quest)) == (quest.getStages().size() - 1)) {
                if (quester.getCurrentStage(quest).getScript() != null) {
                    plugin.getDependencies().runDenizenScript(quester.getCurrentStage(quest).getScript(), quester);
                }
                if (quester.getCurrentStage(quest).getFinishEvent() != null) {
                    quester.getCurrentStage(quest).getFinishEvent().fire(quester, quest);
                }
                quest.completeQuest(quester);
            } else {
                Stage currentStage = quester.getCurrentStage(quest);
                int stageNum = quester.getCurrentQuests().get(quest) + 1;
                quester.hardQuit(quest);
                if (currentStage.getScript() != null) {
                    plugin.getDependencies().runDenizenScript(currentStage.getScript(), quester);
                }
                if (currentStage.getFinishEvent() != null) {
                    currentStage.getFinishEvent().fire(quester, quest);
                }
                quester.hardStagePut(quest, stageNum);
                quester.addEmptiesFor(quest, stageNum);
                // Added this line at some point, not sure why. Commented out to fix Github #726
                //quester.getCurrentStage(quest).setDelay(-1);
                quester.getQuestData(quest).delayStartTime = 0;
                quester.getQuestData(quest).delayTimeLeft = -1;
                Action stageStartEvent = quester.getCurrentStage(quest).getStartEvent();
                if (stageStartEvent != null) {
                    stageStartEvent.fire(quester, quest);
                }
                Player player = quester.getPlayer();
                String msg = Lang.get(player, "questObjectivesTitle");
                msg = msg.replace("<quest>", quest.getName());
                player.sendMessage(ChatColor.GOLD + msg);
                plugin.showObjectives(quest, quester, false);
                String stageStartMessage = quester.getCurrentStage(quest).getStartMessage();
                if (stageStartMessage != null) {
                    quester.getPlayer().sendMessage(ConfigUtil
                            .parseStringWithPossibleLineBreaks(stageStartMessage, quest));
                }
            }
            if (quester.getQuestData(quest) != null) {
                quester.getQuestData(quest).delayOver = true;
            }
            quester.updateJournal();
        }
    }
}
