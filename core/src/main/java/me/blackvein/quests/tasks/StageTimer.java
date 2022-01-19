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

package me.blackvein.quests.tasks;

import me.blackvein.quests.quests.IQuest;
import me.blackvein.quests.player.IQuester;
import me.blackvein.quests.Quests;

public class StageTimer implements Runnable {

    private final IQuester quester;
    private final Quests plugin;
    private final IQuest quest;

    public StageTimer(final Quests plugin, final IQuester quester, final IQuest quest) {
        this.quester = quester;
        this.quest = quest;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (quester == null) {
            return;
        }
        if (quester.getQuestData(quest) == null) {
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
                plugin.getDependencies().runDenizenScript(quester.getCurrentStage(quest).getScript(), quester);
            }
            quest.completeQuest(quester);
        } else {
            final int stageNum = quester.getCurrentQuests().get(quest) + 1;
            quester.getQuestData(quest).setDelayStartTime(0);
            quester.getQuestData(quest).setDelayTimeLeft(-1);
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
