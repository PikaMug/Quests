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

    InputStream getPluginResource(final String filename);

    Dependencies getDependencies();

    ConfigSettings getConfigSettings();

    List<CustomObjective> getCustomObjectives();

    List<CustomReward> getCustomRewards();

    List<CustomRequirement> getCustomRequirements();

    Collection<Quest> getLoadedQuests();

    Collection<Action> getLoadedActions();

    Collection<Condition> getLoadedConditions();

    Quester getQuester(final UUID id);

    /**
     * Get a Quest by ID
     *
     * @param id ID of the quest
     * @return Exact match or null if not found
     */
    Quest getQuestById(final String id);

    /**
     * Get a Quest by name
     *
     * @param name Name of the quest
     * @return Closest match or null if not found
     */
    Quest getQuest(final String name);

    /**
     * Get an Action by name
     *
     * @param name Name of the action
     * @return Closest match or null if not found
     */
    Action getAction(final String name);

    /**
     * Get a Condition by name
     *
     * @param name Name of the condition
     * @return Closest match or null if not found
     */
    Condition getCondition(final String name);

    Collection<Quester> getOnlineQuesters();

    Collection<Quester> getOfflineQuesters();

    void addQuester(final Quester q);

    void removeQuester(final Quester q);

    QuestFactory getQuestFactory();

    ActionFactory getActionFactory();

    ConditionFactory getConditionFactory();

    void saveResourceAs(final String resourcePath, final String outputPath, final boolean replace);
}
