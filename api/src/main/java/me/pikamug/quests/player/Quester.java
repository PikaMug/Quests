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

import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.Stage;
import me.pikamug.quests.quests.components.Objective;
import me.pikamug.quests.enums.ObjectiveType;
import me.pikamug.quests.module.CustomObjective;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface Quester extends Comparable<Quester> {
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

    Player getPlayer();

    OfflinePlayer getOfflinePlayer();

    void sendMessage(final String message);

    Stage getCurrentStage(final Quest quest);

    QuestProgress getQuestDataOrDefault(final Quest quest);

    boolean hasJournal();

    ItemStack getJournal();

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

    void breakBlock(final Quest quest, final ItemStack itemStack);

    void damageBlock(final Quest quest, final ItemStack itemStack);

    void placeBlock(final Quest quest, final ItemStack itemStack);

    void useBlock(final Quest quest, final ItemStack itemStack);

    void cutBlock(final Quest quest, final ItemStack itemStack);

    void craftItem(final Quest quest, final ItemStack itemStack);

    void smeltItem(final Quest quest, final ItemStack itemStack);

    void enchantBook(final Quest quest, final ItemStack itemStack, final Map<Enchantment, Integer> enchantsToAdd);

    void enchantItem(final Quest quest, final ItemStack itemStack);

    void brewItem(final Quest quest, final ItemStack itemStack);

    void consumeItem(final Quest quest, final ItemStack itemStack);

    void deliverToNPC(final Quest quest, final UUID npc, final ItemStack itemStack);

    void interactWithNPC(final Quest quest, final UUID npc);

    void killNPC(final Quest quest, final UUID npc);

    void milkCow(final Quest quest);

    void catchFish(final Quest quest);

    void killMob(final Quest quest, final Location killedLocation, final EntityType entityType);

    void killPlayer(final Quest quest, final Player player);

    void reachLocation(final Quest quest, final Location location);

    void tameMob(final Quest quest, final EntityType entityType);

    void shearSheep(final Quest quest, final DyeColor color);

    void sayPassword(final Quest quest, final AsyncPlayerChatEvent evt);

    void finishObjective(final Quest quest, final Objective objective, final EntityType mob,
                         final String extra, final UUID npc, final Location location, final DyeColor color,
                         final String pass, final CustomObjective co);

    boolean testComplete(final Quest quest);

    void addEmptiesFor(final Quest quest, final int stage);

    boolean saveData();

    long getCompletionDifference(final Quest quest);

    long getRemainingCooldown(final Quest quest);

    FileConfiguration getBaseData();

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

    boolean hasItem(final ItemStack is);

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
