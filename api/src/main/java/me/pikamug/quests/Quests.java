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

import me.pikamug.quests.actions.Action;
import me.pikamug.quests.actions.ActionFactory;
import me.pikamug.quests.conditions.ConditionFactory;
import me.pikamug.quests.conditions.Condition;
import me.pikamug.quests.config.Settings;
import me.pikamug.quests.dependencies.Dependencies;
import me.pikamug.quests.module.CustomObjective;
import me.pikamug.quests.module.CustomRequirement;
import me.pikamug.quests.module.CustomReward;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
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

    Dependencies getDependencies();

    Settings getSettings();

    List<CustomObjective> getCustomObjectives();

    List<CustomReward> getCustomRewards();

    List<CustomRequirement> getCustomRequirements();

    Collection<Quest> getLoadedQuests();

    Collection<Action> getLoadedActions();

    Collection<Condition> getLoadedConditions();

    Quester getQuester(final UUID id);

    Collection<Quester> getOnlineQuesters();

    Collection<Quester> getOfflineQuesters();

    void setOfflineQuesters(final Collection<Quester> questers);

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
