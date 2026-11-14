/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.quests.components;

import me.pikamug.quests.actions.Action;
import me.pikamug.quests.conditions.Condition;
import me.pikamug.quests.enums.ObjectiveType;
import me.pikamug.quests.module.CustomObjective;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

public class FabricStage implements Stage {

    private LinkedList<Object> blocksToBreak = new LinkedList<>();
    private LinkedList<Object> blocksToDamage = new LinkedList<>();
    private LinkedList<Object> blocksToPlace = new LinkedList<>();
    private LinkedList<Object> blocksToUse = new LinkedList<>();
    private LinkedList<Integer> blocksToBreakAmounts = new LinkedList<>();
    private LinkedList<Integer> blocksToDamageAmounts = new LinkedList<>();
    private LinkedList<Integer> blocksToPlaceAmounts = new LinkedList<>();
    private LinkedList<Integer> blocksToUseAmounts = new LinkedList<>();
    private LinkedList<Short> blocksToBreakDurability = new LinkedList<>();
    private LinkedList<Short> blocksToDamageDurability = new LinkedList<>();
    private LinkedList<Short> blocksToPlaceDurability = new LinkedList<>();
    private LinkedList<Short> blocksToUseDurability = new LinkedList<>();
    private LinkedList<Object> itemsToCraft = new LinkedList<>();
    private LinkedList<Object> itemsToSmelt = new LinkedList<>();
    private LinkedList<Object> itemsToEnchant = new LinkedList<>();
    private LinkedList<Object> itemsToBrew = new LinkedList<>();
    private LinkedList<Object> itemsToConsume = new LinkedList<>();
    private LinkedList<Object> itemsToDeliver = new LinkedList<>();
    private LinkedList<UUID> itemDeliveryTargets = new LinkedList<UUID>() {

        private static final long serialVersionUID = -2774443496142382127L;

        @Override
        public boolean equals(final Object o) {
            if (o instanceof LinkedList) {
                @SuppressWarnings("unchecked")
                final LinkedList<UUID> otherList = (LinkedList<UUID>) o;
                for (final UUID uuid : this) {
                    final UUID other = otherList.get(this.indexOf(uuid));
                    if (!other.equals(uuid)) {
                        return false;
                    }
                }
            }
            return true;
        }
    };
    private LinkedList<String> deliverMessages = new LinkedList<>();
    private LinkedList<UUID> npcsToInteract = new LinkedList<UUID>() {

        private static final long serialVersionUID = -4086855121042524435L;

        @Override
        public boolean equals(final Object o) {
            if (o instanceof LinkedList) {
                @SuppressWarnings("unchecked")
                final LinkedList<UUID> otherList = (LinkedList<UUID>) o;
                for (final UUID uuid : this) {
                    final UUID other = otherList.get(this.indexOf(uuid));
                    if (!other.equals(uuid)) {
                        return false;
                    }
                }
            }
            return true;
        }
    };
    private LinkedList<UUID> npcsToKill = new LinkedList<UUID>() {

        private static final long serialVersionUID = 7705964814014176415L;

        @Override
        public boolean equals(final Object o) {
            if (o instanceof LinkedList) {
                @SuppressWarnings("unchecked")
                final LinkedList<UUID> otherList = (LinkedList<UUID>) o;
                for (final UUID uuid : this) {
                    final UUID other = otherList.get(this.indexOf(uuid));
                    if (!other.equals(uuid)) {
                        return false;
                    }
                }
            }
            return true;
        }
    };
    private LinkedList<Integer> npcNumToKill = new LinkedList<>();
    private LinkedList<Object> mobsToKill = new LinkedList<>();
    private LinkedList<Integer> mobNumToKill = new LinkedList<>();
    private LinkedList<Object> locationsToKillWithin = new LinkedList<>();
    private LinkedList<Integer> radiiToKillWithin = new LinkedList<>();
    private LinkedList<String> killNames = new LinkedList<>();
    private LinkedList<Object> mobsToTame = new LinkedList<>();
    private LinkedList<Integer> mobNumToTame = new LinkedList<>();
    private Integer fishToCatch;
    private Integer cowsToMilk;
    private LinkedList<Object> sheepToShear = new LinkedList<>();
    private LinkedList<Integer> sheepNumToShear = new LinkedList<>();
    private Integer playersToKill;
    private LinkedList<Object> locationsToReach = new LinkedList<>();
    private LinkedList<Integer> radiiToReachWithin = new LinkedList<>();
    private LinkedList<Object> worldsToReachWithin = new LinkedList<>();
    private LinkedList<String> locationNames = new LinkedList<>();
    private LinkedList<String> passwordDisplays = new LinkedList<>();
    private LinkedList<String> passwordPhrases = new LinkedList<>();
    private String script;
    private Action startAction = null;
    private Action finishAction = null;
    private Action failAction = null;
    private Action deathAction = null;
    private Map<String, Action> chatActions = new HashMap<>();
    private Map<String, Action> commandActions = new HashMap<>();
    private Action disconnectAction = null;
    private Condition condition = null;
    private long delay = -1;
    private String delayMessage = null;
    private String completeMessage = null;
    private String startMessage = null;
    private LinkedList<String> objectiveOverrides = new LinkedList<>();
    private final LinkedList<CustomObjective> customObjectives = new LinkedList<>();
    private final LinkedList<Integer> customObjectiveCounts = new LinkedList<>();
    private final LinkedList<String> customObjectiveDisplays = new LinkedList<>();
    private final LinkedList<Entry<String, Object>> customObjectiveData = new LinkedList<>();

    @Override
    public LinkedList<Object> getBlocksToBreak() {
        return blocksToBreak;
    }

    public boolean addBlockToBreak(@NotNull final Object blockToBreak) {
        return blocksToBreak.add(blockToBreak);
    }

    public void setBlocksToBreak(final LinkedList<Object> blocksToBreak) {
        this.blocksToBreak = blocksToBreak;
    }

    public LinkedList<Integer> getBlocksToBreakAmounts() {
        return blocksToBreakAmounts;
    }

    public void setBlocksToBreakAmounts(final LinkedList<Integer> blocksToBreakAmounts) {
        this.blocksToBreakAmounts = blocksToBreakAmounts;
    }

    public LinkedList<Short> getBlocksToBreakDurability() {
        return blocksToBreakDurability;
    }

    public void setBlocksToBreakDurability(final LinkedList<Short> blocksToBreakDurability) {
        this.blocksToBreakDurability = blocksToBreakDurability;
    }

    @Override
    public LinkedList<Object> getBlocksToDamage() {
        return blocksToDamage;
    }

    public boolean addBlockToDamage(@NotNull final Object blockToDamage) {
        return blocksToDamage.add(blockToDamage);
    }

    public void setBlocksToDamage(final LinkedList<Object> blocksToDamage) {
        this.blocksToDamage = blocksToDamage;
    }

    public LinkedList<Integer> getBlocksToDamageAmounts() {
        return blocksToDamageAmounts;
    }

    public void setBlocksToDamageAmounts(final LinkedList<Integer> blocksToDamageAmounts) {
        this.blocksToDamageAmounts = blocksToDamageAmounts;
    }

    public LinkedList<Short> getBlocksToDamageDurability() {
        return blocksToDamageDurability;
    }

    public void setBlocksToDamageDurability(final LinkedList<Short> blocksToDamageDurability) {
        this.blocksToDamageDurability = blocksToDamageDurability;
    }

    @Override
    public LinkedList<Object> getBlocksToPlace() {
        return blocksToPlace;
    }

    public boolean addBlockToPlace(@NotNull final Object blockToPlace) {
        return blocksToPlace.add(blockToPlace);
    }

    public void setBlocksToPlace(final LinkedList<Object> blocksToPlace) {
        this.blocksToPlace = blocksToPlace;
    }

    public LinkedList<Integer> getBlocksToPlaceAmounts() {
        return blocksToPlaceAmounts;
    }

    public void setBlocksToPlaceAmounts(final LinkedList<Integer> blocksToPlaceAmounts) {
        this.blocksToPlaceAmounts = blocksToPlaceAmounts;
    }

    public LinkedList<Short> getBlocksToPlaceDurability() {
        return blocksToPlaceDurability;
    }

    public void setBlocksToPlaceDurability(final LinkedList<Short> blocksToPlaceDurability) {
        this.blocksToPlaceDurability = blocksToPlaceDurability;
    }

    @Override
    public LinkedList<Object> getBlocksToUse() {
        return blocksToUse;
    }

    public boolean addBlockToUse(@NotNull final Object blockToUse) {
        return blocksToUse.add(blockToUse);
    }

    public void setBlocksToUse(final LinkedList<Object> blocksToUse) {
        this.blocksToUse = blocksToUse;
    }

    public LinkedList<Integer> getBlocksToUseAmounts() {
        return blocksToUseAmounts;
    }

    public void setBlocksToUseAmounts(final LinkedList<Integer> blocksToUseAmounts) {
        this.blocksToUseAmounts = blocksToUseAmounts;
    }

    public LinkedList<Short> getBlocksToUseDurability() {
        return blocksToUseDurability;
    }

    public void setBlocksToUseDurability(final LinkedList<Short> blocksToUseDurability) {
        this.blocksToUseDurability = blocksToUseDurability;
    }

    @Override
    public LinkedList<Object> getItemsToCraft() {
        return itemsToCraft;
    }

    public boolean addItemToCraft(@NotNull final Object itemToCraft) {
        return itemsToCraft.add(itemToCraft);
    }

    public void setItemsToCraft(final LinkedList<Object> itemsToCraft) {
        this.itemsToCraft = itemsToCraft;
    }

    @Override
    public LinkedList<Object> getItemsToSmelt() {
        return itemsToSmelt;
    }

    public boolean addItemToSmelt(@NotNull final Object itemToSmelt) {
        return itemsToSmelt.add(itemToSmelt);
    }

    public void setItemsToSmelt(final LinkedList<Object> itemsToSmelt) {
        this.itemsToSmelt = itemsToSmelt;
    }

    @Override
    public LinkedList<Object> getItemsToEnchant() {
        return itemsToEnchant;
    }

    public boolean addItemToEnchant(@NotNull final Object itemToEnchant) {
        return itemsToEnchant.add(itemToEnchant);
    }

    public void setItemsToEnchant(final LinkedList<Object> itemsToEnchant) {
        this.itemsToEnchant = itemsToEnchant;
    }

    @Override
    public LinkedList<Object> getItemsToBrew() {
        return itemsToBrew;
    }

    public boolean addItemsToBrew(@NotNull final Object itemToBrew) {
        return itemsToBrew.add(itemToBrew);
    }

    public void setItemsToBrew(final LinkedList<Object> itemsToBrew) {
        this.itemsToBrew = itemsToBrew;
    }

    @Override
    public LinkedList<Object> getItemsToConsume() {
        return itemsToConsume;
    }

    public boolean addItemToConsume(@NotNull final Object itemToConsume) {
        return itemsToConsume.add(itemToConsume);
    }

    public void setItemsToConsume(final LinkedList<Object> itemsToConsume) {
        this.itemsToConsume = itemsToConsume;
    }

    @Override
    public LinkedList<Object> getItemsToDeliver() {
        return itemsToDeliver;
    }

    public boolean addItemToDeliver(@NotNull final Object itemToDeliver) {
        return itemsToDeliver.add(itemToDeliver);
    }

    public void setItemsToDeliver(final LinkedList<Object> itemsToDeliver) {
        this.itemsToDeliver = itemsToDeliver;
    }

    @Override
    public LinkedList<UUID> getItemDeliveryTargets() {
        return itemDeliveryTargets;
    }

    public boolean addItemDeliveryTarget(final UUID itemDeliveryTarget) {
        return itemDeliveryTargets.add(itemDeliveryTarget);
    }

    @Override
    public void setItemDeliveryTargets(final LinkedList<UUID> itemDeliveryTargets) {
        this.itemDeliveryTargets = itemDeliveryTargets;
    }

    @Override
    public LinkedList<String> getDeliverMessages() {
        return deliverMessages;
    }

    public boolean addDeliverMessage(final String deliverMessage) {
        return deliverMessages.add(deliverMessage);
    }

    @Override
    public void setDeliverMessages(final LinkedList<String> deliverMessages) {
        this.deliverMessages = deliverMessages;
    }

    @Override
    public LinkedList<UUID> getNpcsToInteract() {
        return npcsToInteract;
    }

    public boolean addNpcToInteract(final UUID npcToInteract) {
        return npcsToInteract.add(npcToInteract);
    }

    @Override
    public void setNpcsToInteract(final LinkedList<UUID> npcsToInteract) {
        this.npcsToInteract = npcsToInteract;
    }

    @Override
    public LinkedList<UUID> getNpcsToKill() {
        return npcsToKill;
    }

    public boolean addNpcToKill(final UUID npcToKill) {
        return npcsToKill.add(npcToKill);
    }

    @Override
    public void setNpcsToKill(final LinkedList<UUID> npcsToKill) {
        this.npcsToKill = npcsToKill;
    }

    @Override
    public LinkedList<Integer> getNpcNumToKill() {
        return npcNumToKill;
    }

    public boolean addNpcNumToKill(final Integer npcNumToKill) {
        return this.npcNumToKill.add(npcNumToKill);
    }

    @Override
    public void setNpcNumToKill(final LinkedList<Integer> npcNumToKill) {
        this.npcNumToKill = npcNumToKill;
    }

    @Override
    public LinkedList<Object> getMobsToKill() {
        return mobsToKill;
    }

    public boolean addMobToKill(final Object mobToKill) {
        return mobsToKill.add(mobToKill);
    }

    public void setMobsToKill(final LinkedList<Object> mobsToKill) {
        this.mobsToKill = mobsToKill;
    }

    @Override
    public LinkedList<Integer> getMobNumToKill() {
        return mobNumToKill;
    }

    public boolean addMobNumToKill(final Integer mobNumToKill) {
        return this.mobNumToKill.add(mobNumToKill);
    }

    @Override
    public void setMobNumToKill(final LinkedList<Integer> mobNumToKill) {
        this.mobNumToKill = mobNumToKill;
    }

    @Override
    public LinkedList<Object> getLocationsToKillWithin() {
        return locationsToKillWithin;
    }

    public boolean addLocationToKillWithin(final Object locationToKillWithin) {
        return locationsToKillWithin.add(locationToKillWithin);
    }

    public void setLocationsToKillWithin(final LinkedList<Object> locationsToKillWithin) {
        this.locationsToKillWithin = locationsToKillWithin;
    }

    @Override
    public LinkedList<Integer> getRadiiToKillWithin() {
        return radiiToKillWithin;
    }

    public boolean addRadiusToKillWithin(final Integer radiusToKillWithin) {
        return radiiToKillWithin.add(radiusToKillWithin);
    }

    @Override
    public void setRadiiToKillWithin(final LinkedList<Integer> radiiToKillWithin) {
        this.radiiToKillWithin = radiiToKillWithin;
    }

    @Override
    public LinkedList<String> getKillNames() {
        return killNames;
    }

    public boolean addKillName(final String killName) {
        return killNames.add(killName);
    }

    @Override
    public void setKillNames(final LinkedList<String> killNames) {
        this.killNames = killNames;
    }

    @Override
    public LinkedList<Object> getLocationsToReach() {
        return locationsToReach;
    }

    public boolean addLocationToReach(final Object locationToReach) {
        return locationsToReach.add(locationToReach);
    }

    public void setLocationsToReach(final LinkedList<Object> locationsToReach) {
        this.locationsToReach = locationsToReach;
    }

    @Override
    public LinkedList<Integer> getRadiiToReachWithin() {
        return radiiToReachWithin;
    }

    public boolean addRadiusToReachWithin(final Integer radiusToReachWithin) {
        return radiiToReachWithin.add(radiusToReachWithin);
    }

    @Override
    public void setRadiiToReachWithin(final LinkedList<Integer> radiiToReachWithin) {
        this.radiiToReachWithin = radiiToReachWithin;
    }

    @Override
    public LinkedList<Object> getWorldsToReachWithin() {
        return worldsToReachWithin;
    }

    public boolean addWorldToReachWithin(final Object worldToReachWithin) {
        return worldsToReachWithin.add(worldToReachWithin);
    }

    public void setWorldsToReachWithin(final LinkedList<Object> worldsToReachWithin) {
        this.worldsToReachWithin = worldsToReachWithin;
    }

    @Override
    public LinkedList<String> getLocationNames() {
        return locationNames;
    }

    public boolean addLocationName(final String locationName) {
        return locationNames.add(locationName);
    }

    @Override
    public void setLocationNames(final LinkedList<String> locationNames) {
        this.locationNames = locationNames;
    }

    @Override
    public LinkedList<Object> getMobsToTame() {
        return mobsToTame;
    }

    public boolean addMobToTame(final Object mobToTame) {
        return mobsToTame.add(mobToTame);
    }

    public void setMobsToTame(final LinkedList<Object> mobsToTame) {
        this.mobsToTame = mobsToTame;
    }

    @Override
    public LinkedList<Integer> getMobNumToTame() {
        return mobNumToTame;
    }

    public boolean addMobNumToTame(final Integer mobNumToTame) {
        return this.mobNumToTame.add(mobNumToTame);
    }

    @Override
    public void setMobNumToTame(final LinkedList<Integer> mobNumToTame) {
        this.mobNumToTame = mobNumToTame;
    }

    @Override
    public Integer getFishToCatch() {
        return fishToCatch;
    }

    @Override
    public void setFishToCatch(final Integer fishToCatch) {
        this.fishToCatch = fishToCatch;
    }

    @Override
    public Integer getCowsToMilk() {
        return cowsToMilk;
    }

    @Override
    public void setCowsToMilk(final Integer cowsToMilk) {
        this.cowsToMilk = cowsToMilk;
    }

    @Override
    public Integer getPlayersToKill() {
        return playersToKill;
    }

    @Override
    public void setPlayersToKill(final Integer playersToKill) {
        this.playersToKill = playersToKill;
    }

    @Override
    public LinkedList<Object> getSheepToShear() {
        return sheepToShear;
    }

    public boolean addSheepToShear(final Object sheepToShear) {
        return this.sheepToShear.add(sheepToShear);
    }

    public void setSheepToShear(final LinkedList<Object> sheepToShear) {
        this.sheepToShear = sheepToShear;
    }

    @Override
    public LinkedList<Integer> getSheepNumToShear() {
        return sheepNumToShear;
    }

    public boolean addSheepNumToShear(final Integer sheepNumToShear) {
        return this.sheepNumToShear.add(sheepNumToShear);
    }

    @Override
    public void setSheepNumToShear(final LinkedList<Integer> sheepNumToShear) {
        this.sheepNumToShear = sheepNumToShear;
    }

    @Override
    public LinkedList<String> getPasswordDisplays() {
        return passwordDisplays;
    }

    public boolean addPasswordDisplay(final String passwordDisplay) {
        return passwordDisplays.add(passwordDisplay);
    }

    @Override
    public void setPasswordDisplays(final LinkedList<String> passwordDisplays) {
        this.passwordDisplays = passwordDisplays;
    }

    @Override
    public LinkedList<String> getPasswordPhrases() {
        return passwordPhrases;
    }

    public boolean addPasswordPhrase(final String passwordPhrase) {
        return passwordPhrases.add(passwordPhrase);
    }

    @Override
    public void setPasswordPhrases(final LinkedList<String> passwordPhrases) {
        this.passwordPhrases = passwordPhrases;
    }

    @Override
    public String getScript() {
        return script;
    }

    @Override
    public void setScript(final String script) {
        this.script = script;
    }

    @Override
    public Action getStartAction() {
        return startAction;
    }

    @Override
    public void setStartAction(final Action startAction) {
        this.startAction = startAction;
    }

    @Override
    public Action getFinishAction() {
        return finishAction;
    }

    @Override
    public void setFinishAction(final Action finishAction) {
        this.finishAction = finishAction;
    }

    @Override
    public Action getFailAction() {
        return failAction;
    }

    @Override
    public void setFailAction(final Action failAction) {
        this.failAction = failAction;
    }

    @Override
    public Action getDeathAction() {
        return deathAction;
    }

    @Override
    public void setDeathAction(final Action deathAction) {
        this.deathAction = deathAction;
    }

    @Override
    public Map<String, Action> getChatActions() {
        return chatActions;
    }

    public void addChatAction(final Entry<String, Action> chatAction) {
        chatActions.put(chatAction.getKey(), chatAction.getValue());
    }

    @Override
    public void setChatActions(final Map<String, Action> chatActions) {
        this.chatActions = chatActions;
    }

    @Override
    public Map<String, Action> getCommandActions() {
        return commandActions;
    }

    public void addCommandAction(final Entry<String, Action> commandAction) {
        commandActions.put(commandAction.getKey(), commandAction.getValue());
    }

    @Override
    public void setCommandActions(final Map<String, Action> commandActions) {
        this.commandActions = commandActions;
    }

    @Override
    public Action getDisconnectAction() {
        return disconnectAction;
    }

    @Override
    public void setDisconnectAction(final Action disconnectAction) {
        this.disconnectAction = disconnectAction;
    }

    @Override
    public Condition getCondition() {
        return condition;
    }

    @Override
    public void setCondition(final Condition condition) {
        this.condition = condition;
    }

    @Override
    public long getDelay() {
        return delay;
    }

    @Override
    public void setDelay(final long delay) {
        this.delay = delay;
    }

    @Override
    public String getDelayMessage() {
        return delayMessage;
    }

    @Override
    public void setDelayMessage(final String delayMessage) {
        this.delayMessage = delayMessage;
    }

    @Override
    public String getCompleteMessage() {
        return completeMessage;
    }

    @Override
    public void setCompleteMessage(final String completeMessage) {
        this.completeMessage = completeMessage;
    }

    @Override
    public String getStartMessage() {
        return startMessage;
    }

    @Override
    public void setStartMessage(final String startMessage) {
        this.startMessage = startMessage;
    }

    @Override
    public LinkedList<String> getObjectiveOverrides() {
        return objectiveOverrides;
    }

    public boolean addObjectiveOverride(final String objectiveOverride) {
        return objectiveOverrides.add(objectiveOverride);
    }

    @Override
    public void setObjectiveOverrides(final LinkedList<String> objectiveOverrides) {
        this.objectiveOverrides = objectiveOverrides;
    }

    @Override
    public LinkedList<CustomObjective> getCustomObjectives() {
        return customObjectives;
    }

    public boolean addCustomObjectives(final CustomObjective customObjective) {
        return customObjectives.add(customObjective);
    }

    @Override
    public void clearCustomObjectives() {
        customObjectives.clear();
    }

    @Override
    public LinkedList<Integer> getCustomObjectiveCounts() {
        return customObjectiveCounts;
    }

    public boolean addCustomObjectiveCounts(final Integer customObjectiveCount) {
        return customObjectiveCounts.add(customObjectiveCount);
    }

    @Override
    public void clearCustomObjectiveCounts() {
        customObjectiveCounts.clear();
    }

    @Override
    public LinkedList<String> getCustomObjectiveDisplays() {
        return customObjectiveDisplays;
    }

    @Override
    public void clearCustomObjectiveDisplays() {
        customObjectiveDisplays.clear();
    }

    @Override
    public LinkedList<Entry<String, Object>> getCustomObjectiveData() {
        return customObjectiveData;
    }

    public boolean addCustomObjectiveData(final Entry<String, Object> customObjectiveDatum) {
        return this.customObjectiveData.add(customObjectiveDatum);
    }

    @Override
    public void clearCustomObjectiveData() {
        customObjectiveData.clear();
    }

    /**
     * Check if stage has at least one objective<p>
     *
     * Excludes start/complete message, delay, and objective-override
     *
     * @return true if stage contains an objective
     */
    @Override
    public boolean hasObjective() {
        if (!blocksToBreak.isEmpty()) { return true; }
        if (!blocksToDamage.isEmpty()) { return true; }
        if (!blocksToPlace.isEmpty()) { return true; }
        if (!blocksToUse.isEmpty()) { return true; }
        if (cowsToMilk != null) { return true; }
        if (fishToCatch != null) { return true; }
        if (playersToKill != null) { return true; }
        if (!itemsToCraft.isEmpty()) { return true; }
        if (!itemsToSmelt.isEmpty()) { return true; }
        if (!itemsToEnchant.isEmpty()) { return true; }
        if (!itemsToBrew.isEmpty()) { return true; }
        if (!itemsToConsume.isEmpty()) { return true; }
        if (!itemsToDeliver.isEmpty()) { return true; }
        if (!npcsToInteract.isEmpty()) { return true; }
        if (!npcsToKill.isEmpty()) { return true; }
        if (!locationsToReach.isEmpty()) { return true; }
        if (!mobsToKill.isEmpty()) { return true; }
        if (!mobsToTame.isEmpty()) { return true; }
        if (!sheepToShear.isEmpty()) { return true; }
        if (!passwordDisplays.isEmpty()) { return true; }
        return !customObjectives.isEmpty();
    }

    /**
     * Check if stage has at least one objective of which the target can be easily located<p>
     *
     * @return true if stage contains a locatable objective
     */
    @Override
    public boolean hasLocatableObjective() {
        if (!npcsToInteract.isEmpty()) { return true; }
        if (!npcsToKill.isEmpty()) { return true; }
        if (!locationsToReach.isEmpty()) { return true; }
        if (!itemDeliveryTargets.isEmpty()) { return true; }
        if (playersToKill != null) { return true; }
        if (!mobsToKill.isEmpty()) { return true; }
        if (!mobsToTame.isEmpty()) { return true; }
        if (!sheepToShear.isEmpty()) { return true; }
        return false;
    }

    /**
     * Check if stage has the specified type of objective<p>
     *
     * @param type The type of objective to check for
     * @return true if stage contains specified objective
     */
    @Override
    public boolean containsObjective(final ObjectiveType type) {
        if (type.equals(ObjectiveType.BREAK_BLOCK)) {
            return !blocksToBreak.isEmpty();
        } else if (type.equals(ObjectiveType.DAMAGE_BLOCK)) {
            return !blocksToDamage.isEmpty();
        } else if (type.equals(ObjectiveType.PLACE_BLOCK)) {
            return !blocksToPlace.isEmpty();
        } else if (type.equals(ObjectiveType.USE_BLOCK)) {
            return !blocksToUse.isEmpty();
        } else if (type.equals(ObjectiveType.CRAFT_ITEM)) {
            return !itemsToCraft.isEmpty();
        } else if (type.equals(ObjectiveType.SMELT_ITEM)) {
            return !itemsToSmelt.isEmpty();
        } else if (type.equals(ObjectiveType.ENCHANT_ITEM)) {
            return !itemsToEnchant.isEmpty();
        } else if (type.equals(ObjectiveType.BREW_ITEM)) {
            return !itemsToBrew.isEmpty();
        } else if (type.equals(ObjectiveType.CONSUME_ITEM)) {
            return !itemsToConsume.isEmpty();
        } else if (type.equals(ObjectiveType.DELIVER_ITEM)) {
            return !itemsToDeliver.isEmpty();
        } else if (type.equals(ObjectiveType.MILK_COW)) {
            return cowsToMilk != null;
        } else if (type.equals(ObjectiveType.CATCH_FISH)) {
            return fishToCatch != null;
        } else if (type.equals(ObjectiveType.KILL_MOB)) {
            return !mobsToKill.isEmpty();
        } else if (type.equals(ObjectiveType.KILL_PLAYER)) {
            return playersToKill != null;
        } else if (type.equals(ObjectiveType.TALK_TO_NPC)) {
            return !npcsToInteract.isEmpty();
        } else if (type.equals(ObjectiveType.KILL_NPC)) {
            return !npcsToKill.isEmpty();
        } else if (type.equals(ObjectiveType.TAME_MOB)) {
            return !mobsToTame.isEmpty();
        } else if (type.equals(ObjectiveType.SHEAR_SHEEP)) {
            return !sheepToShear.isEmpty();
        } else if (type.equals(ObjectiveType.REACH_LOCATION)) {
            return !locationsToReach.isEmpty();
        } else if (type.equals(ObjectiveType.PASSWORD)) {
            return !passwordPhrases.isEmpty();
        } else if (type.equals(ObjectiveType.CUSTOM)) {
            return !customObjectives.isEmpty();
        } else {
            return false;
        }
    }
}
