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

import me.pikamug.quests.actions.Action;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.components.Options;
import me.pikamug.quests.quests.components.Planner;
import me.pikamug.quests.quests.components.Requirements;
import me.pikamug.quests.quests.components.Rewards;
import me.pikamug.quests.quests.components.Stage;

import java.util.LinkedList;
import java.util.UUID;

public interface Quest extends Comparable<Quest> {

    String getId();

    void setId(String id);

    String getName();

    void setName(final String name);

    String getDescription();

    void setDescription(final String description);

    String getFinished();

    void setFinished(final String finished);

    String getRegionStart();

    void setRegionStart(final String regionStart);

    Stage getStage(final int index);

    LinkedList<Stage> getStages();

    UUID getNpcStart();

    void setNpcStart(final UUID npcStart);

    String getNpcStartName();

    Action getInitialAction();

    void setInitialAction(final Action initialAction);

    Requirements getRequirements();

    void setRequirements(final Requirements requirements);

    Planner getPlanner();

    void setPlanner(final Planner planner);

    Rewards getRewards();

    void setRewards(final Rewards rewards);

    Options getOptions();

    void setOptions(final Options options);

    void nextStage(final Quester quester, final boolean allowSharedProgress);

    void setStage(final Quester quester, final int stage);

    boolean updateCompass(final Quester quester, final Stage stage);

    boolean testRequirements(final Quester quester);

    void completeQuest(final Quester quester);

    void completeQuest(final Quester quester, final boolean allowMultiplayer);

    void failQuest(final Quester quester);

    void failQuest(final Quester quester, final boolean ignoreFailAction);

    boolean isInRegionStart(final Quester quester);
}
