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

package me.pikamug.quests;

import me.pikamug.quests.actions.ActionFactory;
import me.pikamug.quests.actions.IAction;
import me.pikamug.quests.conditions.ConditionFactory;
import me.pikamug.quests.conditions.ICondition;
import me.pikamug.quests.config.ISettings;
import me.pikamug.quests.dependencies.IDependencies;
import me.pikamug.quests.module.ICustomObjective;
import me.pikamug.quests.module.ICustomRequirement;
import me.pikamug.quests.module.ICustomReward;
import me.pikamug.quests.player.IQuester;
import me.pikamug.quests.quests.IQuest;
import me.pikamug.quests.quests.QuestFactory;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public interface Quests {

    boolean isLoading();

    String getDetectedServerSoftwareVersion();

    File getPluginDataFolder();

    Logger getPluginLogger();

    InputStream getPluginResource(String filename);

    IDependencies getDependencies();

    ISettings getSettings();

    List<ICustomObjective> getCustomObjectives();

    List<ICustomReward> getCustomRewards();

    List<ICustomRequirement> getCustomRequirements();

    Collection<IQuest> getLoadedQuests();

    Collection<IAction> getLoadedActions();

    Collection<ICondition> getLoadedConditions();

    IQuester getQuester(final UUID id);

    Collection<IQuester> getOnlineQuesters();

    Collection<IQuester> getOfflineQuesters();

    void setOfflineQuesters(final Collection<IQuester> questers);

    QuestFactory getQuestFactory();

    ActionFactory getActionFactory();

    ConditionFactory getConditionFactory();

    /*ConvoListener getConvoListener();

    BlockListener getBlockListener();

    ItemListener getItemListener();

    NpcListener getNpcListener();

    PlayerListener getPlayerListener();

    UniteListener getUniteListener();

    NpcEffectThread getNpcEffectThread();

    PlayerMoveThread getPlayerMoveThread();

    PartiesListener getPartiesListener();

    DenizenTrigger getDenizenTrigger();

    LocaleManager getLocaleManager();

    Storage getStorage();*/
}
