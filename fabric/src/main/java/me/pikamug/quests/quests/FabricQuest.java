/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.quests;

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.actions.Action;
import me.pikamug.quests.player.FabricQuester;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.components.*;

import java.util.LinkedList;
import java.util.UUID;

public class FabricQuest implements Quest {

    private String id;
    private String name;
    private String description;
    private String finished;
    private String regionStart;
    private LinkedList<Stage> stages = new LinkedList<>();
    private UUID npcStart;
    private String npcStartName;
    private Action initialAction;
    private Requirements requirements = new FabricRequirements();
    private Planner planner = new FabricPlanner();
    private Rewards rewards = new FabricRewards();
    private Options options = new FabricOptions();

    @Override public String getId() { return id; }
    @Override public void setId(String id) { this.id = id; }
    @Override public String getName() { return name; }
    @Override public void setName(String v) { this.name = v; }
    @Override public String getDescription() { return description; }
    @Override public void setDescription(String v) { this.description = v; }
    @Override public String getFinished() { return finished; }
    @Override public void setFinished(String v) { this.finished = v; }
    @Override public String getRegionStart() { return regionStart; }
    @Override public void setRegionStart(String v) { this.regionStart = v; }
    @Override public Stage getStage(int index) {
        if (index < 0 || index >= stages.size()) return null;
        return stages.get(index);
    }
    @Override public LinkedList<Stage> getStages() { return stages; }
    @Override public UUID getNpcStart() { return npcStart; }
    @Override public void setNpcStart(UUID v) { this.npcStart = v; }
    @Override public String getNpcStartName() { return npcStartName; }
    public void setNpcStartName(String v) { this.npcStartName = v; }
    @Override public Action getInitialAction() { return initialAction; }
    @Override public void setInitialAction(Action v) { this.initialAction = v; }
    @Override public Requirements getRequirements() { return requirements; }
    @Override public void setRequirements(Requirements v) { this.requirements = v; }
    @Override public Planner getPlanner() { return planner; }
    @Override public void setPlanner(Planner v) { this.planner = v; }
    @Override public Rewards getRewards() { return rewards; }
    @Override public void setRewards(Rewards v) { this.rewards = v; }
    @Override public Options getOptions() { return options; }
    @Override public void setOptions(Options v) { this.options = v; }

    public void setStages(LinkedList<Stage> stages) { this.stages = stages; }

    @Override
    public void nextStage(Quester quester, boolean allowSharedProgress) {
        if (quester == null) return;
        final int currentStage = quester.getCurrentQuests().getOrDefault(this, 0);
        final int nextStage = currentStage + 1;
        if (nextStage < stages.size()) {
            quester.getCurrentQuests().put(this, nextStage);
            final Stage stage = stages.get(nextStage);
            if (stage.getStartAction() != null) {
                stage.getStartAction().fire(quester, this);
            }
        } else {
            completeQuest(quester);
        }
    }

    @Override
    public void setStage(Quester quester, int stage) {
        if (quester == null || stage < 0 || stage >= stages.size()) return;
        quester.getCurrentQuests().put(this, stage);
    }

    @Override
    public boolean updateCompass(Quester quester, Stage stage) {
        if (quester == null || stage == null) return false;
        quester.setCompassTarget(this);
        return true;
    }

    @Override
    public boolean testRequirements(Quester quester) {
        if (quester == null || requirements == null) return false;

        // Check quest points
        if (requirements.getQuestPoints() > 0 && quester.getQuestPoints() < requirements.getQuestPoints()) {
            return false;
        }

        // Check needed quests
        if (requirements.getNeededQuestIds() != null) {
            for (final String neededId : requirements.getNeededQuestIds()) {
                boolean found = false;
                for (final Quest completed : quester.getCompletedQuests()) {
                    if (completed.getId() != null && completed.getId().equalsIgnoreCase(neededId)) {
                        found = true;
                        break;
                    }
                }
                if (!found) return false;
            }
        }

        // Check blocked quests
        if (requirements.getBlockQuestIds() != null) {
            for (final String blockedId : requirements.getBlockQuestIds()) {
                for (final Quest completed : quester.getCompletedQuests()) {
                    if (completed.getId() != null && completed.getId().equalsIgnoreCase(blockedId)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    @Override
    public void completeQuest(Quester quester) {
        completeQuest(quester, true);
    }

    @Override
    public void completeQuest(Quester quester, boolean allowMultiplayer) {
        if (quester == null) return;
        quester.getCurrentQuests().remove(this);
        quester.getCompletedQuests().add(this);
        final int count = quester.getAmountsCompleted().getOrDefault(this, 0);
        quester.getAmountsCompleted().put(this, count + 1);
        quester.getCompletedTimes().put(this, System.currentTimeMillis());
        // Grant rewards
        if (rewards != null) {
            quester.setQuestPoints(quester.getQuestPoints() + rewards.getQuestPoints());
        }
        quester.saveData();
        FabricQuestsPlugin.LOGGER.info("Quest '{}' completed by {}", name, quester.getUUID());
    }

    @Override
    public void failQuest(Quester quester) {
        failQuest(quester, false);
    }

    @Override
    public void failQuest(Quester quester, boolean ignoreFailAction) {
        if (quester == null) return;
        quester.getCurrentQuests().remove(this);
        quester.saveData();
        FabricQuestsPlugin.LOGGER.info("Quest '{}' failed by {}", name, quester.getUUID());
    }

    @Override
    public boolean isInRegionStart(Quester quester) {
        // TODO: implement WorldGuard region check
        return true;
    }

    @Override
    public int compareTo(Quest other) {
        if (other == null) return 1;
        if (this.name != null && other.getName() != null) {
            return this.name.compareTo(other.getName());
        }
        return 0;
    }
}
