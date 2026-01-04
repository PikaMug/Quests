/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.player;

import me.pikamug.quests.Quests;
import me.pikamug.quests.enums.ObjectiveType;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.Objective;
import me.pikamug.quests.quests.components.Stage;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface Quester extends Comparable<Quester> {
    Quests getPlugin();

    UUID getUUID();

    void setUUID(final UUID id);

    String getQuestIdToTake();

    void setQuestIdToTake(final String questIdToTake);

    String getQuestIdToQuit();

    void setQuestIdToQuit(final String questIdToQuit);

    String getLastKnownName();

    void setLastKnownName(final String lastKnownName);

    int getQuestPoints();

    void setQuestPoints(final int questPoints);

    /**
     * Get compass target quest. Returns null if not set
     *
     * @return Quest or null
     */
    Quest getCompassTarget();

    /**
     * Set compass target quest. Does not update in-game
     *
     * @param quest The target quest
     */
    void setCompassTarget(final Quest quest);

    ConcurrentHashMap<Integer, Quest> getTimers();

    void setTimers(final ConcurrentHashMap<Integer, Quest> timers);

    void removeTimer(final Integer timerId);

    ConcurrentHashMap<Quest, Integer> getCurrentQuests();

    void setCurrentQuests(final ConcurrentHashMap<Quest, Integer> currentQuests);

    ConcurrentSkipListSet<Quest> getCompletedQuests();

    void setCompletedQuests(final ConcurrentSkipListSet<Quest> completedQuests);

    ConcurrentHashMap<Quest, Long> getCompletedTimes();

    void setCompletedTimes(final ConcurrentHashMap<Quest, Long> completedTimes);

    ConcurrentHashMap<Quest, Integer> getAmountsCompleted();

    void setAmountsCompleted(final ConcurrentHashMap<Quest, Integer> amountsCompleted);

    /*Player getPlayer();

    OfflinePlayer getOfflinePlayer();*/

    void sendMessage(final String message);

    Stage getCurrentStage(final Quest quest);

    /**
     * @deprecated Use {@link #getQuestProgressOrDefault(Quest)} instead
     */
    QuestProgress getQuestDataOrDefault(final Quest quest);

    QuestProgress getQuestProgressOrDefault(final Quest quest);

    boolean hasJournal();

    //ItemStack getJournal();

    int getJournalIndex();

    void updateJournal();

    boolean offerQuest(final Quest quest, final boolean giveReason);

    boolean canAcceptOffer(final Quest quest, final boolean giveReason);

    boolean isOnTime(final Quest quest, final boolean giveReason);

    void takeQuest(final Quest quest, final boolean ignoreRequirements);

    boolean abandonQuest(final Quest quest, final String message);

    boolean abandonQuest(final Quest quest, final String[] messages);

    void quitQuest(final Quest quest, final String message);

    void quitQuest(final Quest quest, final String[] messages);

    void listQuests(final Quester quester, final int page);

    LinkedList<String> getCurrentRequirements(final Quest quest, final boolean ignoreOverrides);

    LinkedList<Objective> getCurrentObjectives(final Quest quest, final boolean ignoreOverrides, final boolean formatNames);

    void showCurrentObjectives(final Quest quest, final Quester quester, final boolean ignoreOverrides);

    boolean hasObjective(final Quest quest, final ObjectiveType type);

    boolean hasCustomObjective(final Quest quest, final String name);

    void showCurrentConditions(final Quest quest, final Quester quester);

    boolean testComplete(final Quest quest);

    void addEmptiesFor(final Quest quest, final int stage);

    boolean saveData();

    long getCompletionDifference(final Quest quest);

    long getRemainingCooldown(final Quest quest);

    boolean hasData();

    boolean hasBaseData();

    void startStageTimer(final Quest quest);

    void stopStageTimer(final Quest quest);

    long getStageTime(final Quest quest);

    void checkQuest(final Quest quest);

    void showGUIDisplay(final UUID npc, final LinkedList<Quest> quests);

    void hardQuit(final Quest quest);

    void hardRemove(final Quest quest);

    void hardClear();

    void hardStagePut(final Quest key, final Integer val);

    void hardDataPut(final Quest key, final QuestProgress val);

    boolean canUseCompass();

    void resetCompass();

    void findCompassTarget();

    void findNextCompassTarget(final boolean notify);

    Set<String> dispatchMultiplayerEverything(final Quest quest, final ObjectiveType type,
                                              final BiFunction<Quester, Quest, Void> fun);

    Set<String> dispatchMultiplayerObjectives(final Quest quest, final Stage currentStage,
                                              final Function<Quester, Void> fun);

    List<Quester> getMultiplayerQuesters(final Quest quest);

    boolean meetsCondition(final Quest quest, final boolean giveReason);

    boolean isSelectingBlock();

    boolean isInRegion(final String regionID);

    boolean canAcceptQuest(final UUID npc);

    boolean canAcceptCompletedQuest(final UUID npc);

    boolean canAcceptCompletedRedoableQuest(final UUID npc);
}
