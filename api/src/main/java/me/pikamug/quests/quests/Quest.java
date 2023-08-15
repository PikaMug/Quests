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

package me.pikamug.quests.quests;

import me.pikamug.quests.actions.Action;
import me.pikamug.quests.player.Quester;

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

    Planner getPlanner();

    Rewards getRewards();

    Options getOptions();

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
