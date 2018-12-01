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
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
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

	@SuppressWarnings("deprecation")
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
				quester.addEmptiesFor(quest, stageNum);
				quester.getCurrentStage(quest).delay = -1; // Line added to fix Github issue #505
				quester.getQuestData(quest).delayStartTime = 0;
				quester.getQuestData(quest).delayTimeLeft = -1;
				String msg = Lang.get(player, "questObjectivesTitle");
				msg = msg.replace("<quest>", quest.name);
				player.sendMessage(ChatColor.GOLD + msg);
				for (String s : quester.getObjectivesReal(quest)) {
					try {
						// TODO ensure all applicable strings are translated
						String sbegin = s.substring(s.indexOf(ChatColor.AQUA.toString()) + 2);
						String serial = sbegin.substring(0, sbegin.indexOf(ChatColor.GREEN.toString()));
						
						String enchant = "";
						if (s.contains(ChatColor.LIGHT_PURPLE.toString())) {
							String ebegin = s.substring(s.indexOf(ChatColor.LIGHT_PURPLE.toString()) + 2);
							enchant = ebegin.substring(0, ebegin.indexOf(ChatColor.GREEN.toString()));
						}
						
						// Order is important
						if (Enchantment.getByName(Lang.getKey(enchant).replace("ENCHANTMENT_", "")) != null) {
							Material m = Material.matchMaterial(serial);
							Enchantment e = Enchantment.getByName(Lang.getKey(enchant).replace("ENCHANTMENT_", ""));
							plugin.query.sendMessage(player, s.replace(serial, "<item>").replace(enchant, "<enchantment>"), m, e);
							continue;
						} else if (Material.matchMaterial(serial) != null) {
							Material m = Material.matchMaterial(serial);
							plugin.query.sendMessage(player, s.replace(serial, "<item>"), m);
							continue;
						} else {
							try {
								EntityType type = EntityType.valueOf(serial.toUpperCase().replace(" ", "_"));
								plugin.query.sendMessage(player, s.replace(serial, "<mob>"), type);
							} catch (IllegalArgumentException e) {
								player.sendMessage(s);
							}
						}
					} catch (IndexOutOfBoundsException e) {
						player.sendMessage(s);
					}
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
