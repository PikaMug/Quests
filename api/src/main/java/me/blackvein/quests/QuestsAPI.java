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

package me.blackvein.quests;

import me.blackvein.quests.actions.IAction;
import me.blackvein.quests.actions.ActionFactory;
import me.blackvein.quests.conditions.ICondition;
import me.blackvein.quests.conditions.ConditionFactory;
import me.blackvein.quests.config.ISettings;
import me.blackvein.quests.dependencies.IDependencies;
import me.blackvein.quests.module.ICustomObjective;
import me.blackvein.quests.player.IQuester;
import me.blackvein.quests.quests.IQuest;
import me.blackvein.quests.quests.QuestFactory;
import org.bukkit.command.CommandExecutor;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public interface QuestsAPI extends Plugin {

    boolean isLoading();

    String getDetectedServerSoftwareVersion();

    IDependencies getDependencies();

    ISettings getSettings();

    List<ICustomObjective> getCustomObjectives();

    List<CustomReward> getCustomRewards();

    List<CustomRequirement> getCustomRequirements();

    Collection<IQuest> getLoadedQuests();

    Collection<IAction> getLoadedActions();

    Collection<ICondition> getLoadedConditions();

    IQuester getQuester(final UUID id);

    Collection<IQuester> getOnlineQuesters();

    Collection<IQuester> getOfflineQuesters();

    void setOfflineQuesters(final Collection<IQuester> questers);

    LinkedList<Integer> getQuestNpcIds();

    void setQuestNpcIds(final LinkedList<Integer> questNpcIds);

    CommandExecutor getCommandExecutor();

    ConversationFactory getConversationFactory();

    ConversationFactory getNpcConversationFactory();

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
