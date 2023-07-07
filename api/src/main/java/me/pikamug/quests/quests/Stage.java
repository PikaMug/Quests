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
import me.pikamug.quests.conditions.Condition;
import me.pikamug.quests.enums.ObjectiveType;
import me.pikamug.quests.module.CustomObjective;

import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

public interface Stage {
    LinkedList<?> getBlocksToBreak();

    LinkedList<?> getBlocksToDamage();

    LinkedList<?> getBlocksToPlace();

    LinkedList<?> getBlocksToUse();

    LinkedList<?> getBlocksToCut();

    LinkedList<?> getItemsToCraft();

    LinkedList<?> getItemsToSmelt();

    LinkedList<?> getItemsToEnchant();

    LinkedList<?> getItemsToBrew();

    LinkedList<?> getItemsToConsume();

    LinkedList<?> getItemsToDeliver();

    LinkedList<UUID> getItemDeliveryTargets();

    void setItemDeliveryTargets(final LinkedList<UUID> itemDeliveryTargets);

    LinkedList<String> getDeliverMessages();

    void setDeliverMessages(final LinkedList<String> deliverMessages);

    LinkedList<UUID> getNpcsToInteract();

    void setNpcsToInteract(final LinkedList<UUID> npcsToInteract);

    LinkedList<UUID> getNpcsToKill();

    void setNpcsToKill(final LinkedList<UUID> npcsToKill);

    LinkedList<Integer> getNpcNumToKill();

    void setNpcNumToKill(final LinkedList<Integer> npcNumToKill);

    LinkedList<?> getMobsToKill();

    LinkedList<Integer> getMobNumToKill();

    void setMobNumToKill(final LinkedList<Integer> mobNumToKill);

    LinkedList<?> getLocationsToKillWithin();

    LinkedList<Integer> getRadiiToKillWithin();

    void setRadiiToKillWithin(final LinkedList<Integer> radiiToKillWithin);

    LinkedList<String> getKillNames();

    void setKillNames(final LinkedList<String> killNames);

    LinkedList<?> getLocationsToReach();

    LinkedList<Integer> getRadiiToReachWithin();

    void setRadiiToReachWithin(final LinkedList<Integer> radiiToReachWithin);

    LinkedList<?> getWorldsToReachWithin();

    LinkedList<String> getLocationNames();

    void setLocationNames(final LinkedList<String> locationNames);

    LinkedList<?> getMobsToTame();

    LinkedList<Integer> getMobNumToTame();

    void setMobNumToTame(final LinkedList<Integer> mobNumToTame);

    Integer getFishToCatch();

    void setFishToCatch(final Integer fishToCatch);

    Integer getCowsToMilk();

    void setCowsToMilk(final Integer cowsToMilk);

    Integer getPlayersToKill();

    void setPlayersToKill(final Integer playersToKill);

    LinkedList<?> getSheepToShear();

    LinkedList<Integer> getSheepNumToShear();

    void setSheepNumToShear(final LinkedList<Integer> sheepNumToShear);

    LinkedList<String> getPasswordDisplays();

    void setPasswordDisplays(final LinkedList<String> passwordDisplays);

    LinkedList<String> getPasswordPhrases();

    void setPasswordPhrases(final LinkedList<String> passwordPhrases);

    String getScript();

    void setScript(final String script);

    Action getStartAction();

    void setStartAction(final Action startAction);

    Action getFinishAction();

    void setFinishAction(final Action finishAction);

    Action getFailAction();

    void setFailAction(final Action failAction);

    Action getDeathAction();

    void setDeathAction(final Action deathAction);

    Map<String, Action> getChatActions();

    void setChatActions(final Map<String, Action> chatActions);

    Map<String, Action> getCommandActions();

    void setCommandActions(final Map<String, Action> commandActions);

    Action getDisconnectAction();

    void setDisconnectAction(final Action disconnectAction);

    Condition getCondition();

    void setCondition(final Condition condition);

    long getDelay();

    void setDelay(final long delay);

    String getDelayMessage();

    void setDelayMessage(final String delayMessage);

    String getCompleteMessage();

    void setCompleteMessage(final String completeMessage);

    String getStartMessage();

    void setStartMessage(final String startMessage);

    LinkedList<String> getObjectiveOverrides();

    void setObjectiveOverrides(final LinkedList<String> objectiveOverrides);

    LinkedList<CustomObjective> getCustomObjectives();

    void clearCustomObjectives();

    LinkedList<Integer> getCustomObjectiveCounts();

    void clearCustomObjectiveCounts();

    LinkedList<String> getCustomObjectiveDisplays();

    void clearCustomObjectiveDisplays();

    LinkedList<Map.Entry<String, Object>> getCustomObjectiveData();

    void clearCustomObjectiveData();

    /**
     * Check if stage has at least one objective<p>
     *
     * Excludes start/complete message, delay, and objective-override
     *
     * @return true if stage contains an objective
     */
    boolean hasObjective();

    /**
     * Check if stage has at least one objective of which the target can be located easily<p>
     *
     * @return true if stage contains a locatable objective
     */
    boolean hasLocatableObjective();

    /**
     * Check if stage has the specified type of objective<p>
     *
     * @param type The type of objective to check for
     * @return true if stage contains specified objective
     */
    boolean containsObjective(final ObjectiveType type);
}
