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
import me.pikamug.quests.BukkitQuestsPlugin;

public class BukkitStageTimer implements Runnable {

    private final Quester quester;
    private final BukkitQuestsPlugin plugin;
    private final Quest quest;

    public BukkitStageTimer(final BukkitQuestsPlugin plugin, final Quester quester, final Quest quest) {
        this.quester = quester;
        this.quest = quest;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (quester == null) {
            return;
        }
        if (quester.getQuestDataOrDefault(quest) == null) {
            return;
        }
        if (quester.getCurrentStage(quest) == null) {
            return;
        }
        if (quester.getCurrentStage(quest).getFinishAction() != null) {
            quester.getCurrentStage(quest).getFinishAction().fire(quester, quest);
        }
        if (quest.getStages().indexOf(quester.getCurrentStage(quest)) == (quest.getStages().size() - 1)) {
            if (quester.getCurrentStage(quest).getScript() != null) {
                plugin.getDependencies().runDenizenScript(quester.getCurrentStage(quest).getScript(), quester, null);
            }
            quest.completeQuest(quester);
        } else if (quester.testComplete(quest)) {
            final int stageNum = quester.getCurrentQuests().get(quest) + 1;
            quester.getQuestDataOrDefault(quest).setDelayStartTime(0);
            quester.getQuestDataOrDefault(quest).setDelayTimeLeft(-1);
            try {
                quest.setStage(quester, stageNum);
            } catch (final IndexOutOfBoundsException e) {
                plugin.getLogger().severe("Unable to set stage of quest " + quest.getName() + " to Stage "
                        + stageNum + " after delay");
            }
        }
        quester.updateJournal();
    }
}
