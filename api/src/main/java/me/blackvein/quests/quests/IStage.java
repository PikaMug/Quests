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

package me.blackvein.quests.quests;

import me.blackvein.quests.actions.IAction;
import me.blackvein.quests.conditions.ICondition;
import me.blackvein.quests.enums.ObjectiveType;
import me.blackvein.quests.module.ICustomObjective;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;
import java.util.Map;

public interface IStage {
    LinkedList<ItemStack> getBlocksToBreak();

    boolean addBlockToBreak(ItemStack blockToBreak);

    void setBlocksToBreak(final LinkedList<ItemStack> blocksToBreak);

    LinkedList<ItemStack> getBlocksToDamage();

    boolean addBlockToDamage(ItemStack blockToDamage);

    void setBlocksToDamage(final LinkedList<ItemStack> blocksToDamage);

    LinkedList<ItemStack> getBlocksToPlace();

    boolean addBlockToPlace(ItemStack blockToPlace);

    void setBlocksToPlace(final LinkedList<ItemStack> blocksToPlace);

    LinkedList<ItemStack> getBlocksToUse();

    boolean addBlockToUse(ItemStack blockToUse);

    void setBlocksToUse(final LinkedList<ItemStack> blocksToUse);

    LinkedList<ItemStack> getBlocksToCut();

    boolean addBlockToCut(ItemStack blockToCut);

    void setBlocksToCut(final LinkedList<ItemStack> blocksToCut);

    LinkedList<ItemStack> getItemsToCraft();

    boolean addItemToCraft(ItemStack itemToCraft);

    void setItemsToCraft(final LinkedList<ItemStack> itemsToCraft);

    LinkedList<ItemStack> getItemsToSmelt();

    boolean addItemToSmelt(ItemStack itemToSmelt);

    void setItemsToSmelt(final LinkedList<ItemStack> itemsToSmelt);

    LinkedList<ItemStack> getItemsToEnchant();

    boolean addItemToEnchant(ItemStack itemToEnchant);

    void setItemsToEnchant(final LinkedList<ItemStack> itemsToEnchant);

    LinkedList<ItemStack> getItemsToBrew();

    boolean addItemsToBrew(ItemStack itemToBrew);

    void setItemsToBrew(final LinkedList<ItemStack> itemsToBrew);

    LinkedList<ItemStack> getItemsToConsume();

    boolean addItemToConsume(ItemStack itemToConsume);

    void setItemsToConsume(final LinkedList<ItemStack> itemsToConsume);

    LinkedList<ItemStack> getItemsToDeliver();

    boolean addItemToDeliver(ItemStack itemToDeliver);

    void setItemsToDeliver(final LinkedList<ItemStack> itemsToDeliver);

    LinkedList<Integer> getItemDeliveryTargets();

    boolean addItemDeliveryTarget(Integer itemDeliveryTarget);

    void setItemDeliveryTargets(final LinkedList<Integer> itemDeliveryTargets);

    LinkedList<String> getDeliverMessages();

    boolean addDeliverMessage(String deliverMessage);

    void setDeliverMessages(final LinkedList<String> deliverMessages);

    LinkedList<Integer> getCitizensToInteract();

    boolean addCitizenToInteract(Integer citizenToInteract);

    void setCitizensToInteract(final LinkedList<Integer> citizensToInteract);

    LinkedList<Integer> getCitizensToKill();

    boolean addCitizenToKill(Integer citizenToKill);

    void setCitizensToKill(final LinkedList<Integer> citizensToKill);

    LinkedList<Integer> getCitizenNumToKill();

    boolean addCitizenNumToKill(Integer citizenNumToKill);

    void setCitizenNumToKill(final LinkedList<Integer> citizenNumToKill);

    LinkedList<EntityType> getMobsToKill();

    boolean addMobToKill(EntityType mobToKill);

    void setMobsToKill(final LinkedList<EntityType> mobsToKill);

    LinkedList<Integer> getMobNumToKill();

    boolean addMobNumToKill(Integer mobNumToKill);

    void setMobNumToKill(final LinkedList<Integer> mobNumToKill);

    LinkedList<Location> getLocationsToKillWithin();

    boolean addLocationToKillWithin(Location locationToKillWithin);

    void setLocationsToKillWithin(final LinkedList<Location> locationsToKillWithin);

    LinkedList<Integer> getRadiiToKillWithin();

    boolean addRadiusToKillWithin(Integer radiusToKillWithin);

    void setRadiiToKillWithin(final LinkedList<Integer> radiiToKillWithin);

    LinkedList<String> getKillNames();

    boolean addKillName(String killName);

    void setKillNames(final LinkedList<String> killNames);

    LinkedList<Location> getLocationsToReach();

    boolean addLocationToReach(Location locationToReach);

    void setLocationsToReach(final LinkedList<Location> locationsToReach);

    LinkedList<Integer> getRadiiToReachWithin();

    boolean addRadiusToReachWithin(Integer radiusToReachWithin);

    void setRadiiToReachWithin(final LinkedList<Integer> radiiToReachWithin);

    LinkedList<World> getWorldsToReachWithin();

    boolean addWorldToReachWithin(World worldToReachWithin);

    void setWorldsToReachWithin(final LinkedList<World> worldsToReachWithin);

    LinkedList<String> getLocationNames();

    boolean addLocationName(String locationName);

    void setLocationNames(final LinkedList<String> locationNames);

    LinkedList<EntityType> getMobsToTame();

    boolean addMobToTame(EntityType mobToTame);

    void setMobsToTame(final LinkedList<EntityType> mobsToTame);

    LinkedList<Integer> getMobNumToTame();

    boolean addMobNumToTame(Integer mobNumToTame);

    void setMobNumToTame(final LinkedList<Integer> mobNumToTame);

    Integer getFishToCatch();

    void setFishToCatch(final Integer fishToCatch);

    Integer getCowsToMilk();

    void setCowsToMilk(final Integer cowsToMilk);

    Integer getPlayersToKill();

    void setPlayersToKill(final Integer playersToKill);

    LinkedList<DyeColor> getSheepToShear();

    boolean addSheepToShear(DyeColor sheepToShear);

    void setSheepToShear(final LinkedList<DyeColor> sheepToShear);

    LinkedList<Integer> getSheepNumToShear();

    boolean addSheepNumToShear(Integer sheepNumToShear);

    void setSheepNumToShear(final LinkedList<Integer> sheepNumToShear);

    LinkedList<String> getPasswordDisplays();

    boolean addPasswordDisplay(String passwordDisplay);

    void setPasswordDisplays(final LinkedList<String> passwordDisplays);

    LinkedList<String> getPasswordPhrases();

    boolean addPasswordPhrase(String passwordPhrase);

    void setPasswordPhrases(final LinkedList<String> passwordPhrases);

    String getScript();

    void setScript(final String script);

    IAction getStartAction();

    void setStartAction(final IAction startAction);

    IAction getFinishAction();

    void setFinishAction(final IAction finishAction);

    IAction getFailAction();

    void setFailAction(final IAction failAction);

    IAction getDeathAction();

    void setDeathAction(final IAction deathAction);

    Map<String, IAction> getChatActions();

    void addChatAction(Map.Entry<String, IAction> chatAction);

    void setChatActions(final Map<String, IAction> chatActions);

    Map<String, IAction> getCommandActions();

    void addCommandAction(Map.Entry<String, IAction> commandAction);

    void setCommandActions(final Map<String, IAction> commandActions);

    IAction getDisconnectAction();

    void setDisconnectAction(final IAction disconnectAction);

    ICondition getCondition();

    void setCondition(final ICondition condition);

    long getDelay();

    void setDelay(final long delay);

    String getDelayMessage();

    void setDelayMessage(final String delayMessage);

    String getCompleteMessage();

    void setCompleteMessage(final String completeMessage);

    String getStartMessage();

    void setStartMessage(final String startMessage);

    LinkedList<String> getObjectiveOverrides();

    boolean addObjectiveOverride(String objectiveOverride);

    void setObjectiveOverrides(final LinkedList<String> objectiveOverrides);

    LinkedList<ICustomObjective> getCustomObjectives();

    boolean addCustomObjectives(final ICustomObjective customObjective);

    void clearCustomObjectives();

    LinkedList<Integer> getCustomObjectiveCounts();

    boolean addCustomObjectiveCounts(final Integer customObjectiveCount);

    void clearCustomObjectiveCounts();

    LinkedList<String> getCustomObjectiveDisplays();

    void clearCustomObjectiveDisplays();

    LinkedList<Map.Entry<String, Object>> getCustomObjectiveData();

    boolean addCustomObjectiveData(final Map.Entry<String, Object> customObjectiveDatum);

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
