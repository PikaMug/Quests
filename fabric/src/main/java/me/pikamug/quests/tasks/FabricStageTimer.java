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
import me.pikamug.quests.FabricQuestsPlugin;

public class FabricStageTimer implements Runnable {

    private final Quester quester;
    private final FabricQuestsPlugin plugin;
    private final Quest quest;

    public FabricStageTimer(final FabricQuestsPlugin plugin, final Quester quester, final Quest quest) {
        this.quester = quester;
        this.quest = quest;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (quester == null) {
            return;
        }
        if (quester.getQuestProgressOrDefault(quest) == null) {
            return;
        }
        if (quester.getCurrentStage(quest) == null) {
            return;
        }
        if (!quester.testComplete(quest)) {
            return;
        }
        if (quester.getCurrentStage(quest).getFinishAction() != null) {
            quester.getCurrentStage(quest).getFinishAction().fire(quester, quest);
        }
        if (quest.getOrderedStages().indexOf(quester.getCurrentStage(quest)) == (quest.getOrderedStages().size() - 1)) {
            quest.completeQuest(quester);
        } else {
            final int stageNum = quester.getCurrentQuests().get(quest) + 1;
            quester.getQuestProgressOrDefault(quest).setDelayStartTime(0);
            quester.getQuestProgressOrDefault(quest).setDelayTimeLeft(-1);
            try {
                quest.setStage(quester, stageNum);
            } catch (final IndexOutOfBoundsException e) {
                plugin.getPluginLogger().severe("Unable to set stage of quest " + quest.getName() + " to Stage "
                        + stageNum + " after delay");
            }
        }
        quester.updateJournal();
    }
}
