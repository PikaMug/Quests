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

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import me.blackvein.quests.util.Lang;

public class StageTimer implements Runnable {

	Quester quester;
	Quests plugin;
	Quest quest;

	public StageTimer(Quests quests, Quester q, Quest qu) {
		quester = q;
		quest = qu;
		plugin = quests;
	}

	@Override
	public void run() {
		if (quester.getQuestData(quest).delayOver) {
			Player player = quester.getPlayer();
			if (quest.orderedStages.indexOf(quester.getCurrentStage(quest)) == (quest.orderedStages.size() - 1)) {
				if (quester.getCurrentStage(quest).script != null) {
					plugin.trigger.parseQuestTaskTrigger(quester.getCurrentStage(quest).script, player);
				}
				if (quester.getCurrentStage(quest).finishEvent != null) {
					quester.getCurrentStage(quest).finishEvent.fire(quester, quest);
				}
				quest.completeQuest(quester);
			} else {
				Stage currentStage = quester.getCurrentStage(quest);
				int stageNum = quester.currentQuests.get(quest) + 1;
				quester.hardQuit(quest);
				if (currentStage.script != null) {
					plugin.trigger.parseQuestTaskTrigger(currentStage.script, player);
				}
				if (currentStage.finishEvent != null) {
					currentStage.finishEvent.fire(quester, quest);
				}
				quester.hardStagePut(quest, stageNum);
				quester.addEmptiesFor(quest, 0);
				quester.getQuestData(quest).delayStartTime = 0;
				quester.getQuestData(quest).delayTimeLeft = -1;
				String msg = Lang.get("questObjectivesTitle");
				msg = msg.replaceAll("<quest>", quest.name);
				player.sendMessage(ChatColor.GOLD + msg);
				player.sendMessage(ChatColor.GOLD + Lang.get("questObjectivesTitle"));
				for (String s : quester.getObjectivesReal(quest)) {
					player.sendMessage(s);
				}
				String stageStartMessage = quester.getCurrentStage(quest).startMessage;
				if (stageStartMessage != null) {
					quester.getPlayer().sendMessage(Quests.parseString(stageStartMessage, quest));
				}
			}
			quester.getQuestData(quest).delayOver = true;
			quester.updateJournal();
		}
	}
}
