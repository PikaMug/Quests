/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests;

import me.pikamug.quests.actions.Action;
import me.pikamug.quests.actions.ActionFactory;
import me.pikamug.quests.conditions.Condition;
import me.pikamug.quests.conditions.ConditionFactory;
import me.pikamug.quests.config.ConfigSettings;
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

    boolean isEnabled();

    boolean isLoading();

    String getDetectedServerSoftwareVersion();

    File getPluginDataFolder();

    Logger getPluginLogger();

    InputStream getPluginResource(String filename);

    Dependencies getDependencies();

    ConfigSettings getConfigSettings();

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

    QuesterStorage getStorage();*/

    void saveResourceAs(String resourcePath, final String outputPath, final boolean replace);
}
