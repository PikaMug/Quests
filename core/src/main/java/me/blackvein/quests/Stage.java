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
import me.blackvein.quests.conditions.ICondition;
import me.blackvein.quests.module.ICustomObjective;
import me.blackvein.quests.enums.ObjectiveType;
import me.blackvein.quests.quests.IStage;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

public class Stage implements IStage {

    private LinkedList<ItemStack> blocksToBreak = new LinkedList<>();
    private LinkedList<ItemStack> blocksToDamage = new LinkedList<>();
    private LinkedList<ItemStack> blocksToPlace = new LinkedList<>();
    private LinkedList<ItemStack> blocksToUse = new LinkedList<>();
    private LinkedList<ItemStack> blocksToCut = new LinkedList<>();
    private LinkedList<ItemStack> itemsToCraft = new LinkedList<>();
    private LinkedList<ItemStack> itemsToSmelt = new LinkedList<>();
    private LinkedList<ItemStack> itemsToEnchant = new LinkedList<>();
    private LinkedList<ItemStack> itemsToBrew = new LinkedList<>();
    private LinkedList<ItemStack> itemsToConsume = new LinkedList<>();
    private LinkedList<ItemStack> itemsToDeliver = new LinkedList<>();
    private LinkedList<Integer> itemDeliveryTargets = new LinkedList<Integer>() {

        private static final long serialVersionUID = -2774443496142382127L;

        @Override
        public boolean equals(final Object o) {
            if (o instanceof LinkedList) {
                @SuppressWarnings("unchecked")
                final
                LinkedList<Integer> otherList = (LinkedList<Integer>) o;
                for (final Integer i : this) {
                    final Integer other = otherList.get(this.indexOf(i));
                    if (!other.equals(i)) {
                        return false;
                    }
                }
            }
            return true;
        }
    };
    private  LinkedList<String> deliverMessages = new LinkedList<>();
    private LinkedList<Integer> citizensToInteract = new LinkedList<Integer>() {

        private static final long serialVersionUID = -4086855121042524435L;

        @Override
        public boolean equals(final Object o) {
            if (o instanceof LinkedList) {
                @SuppressWarnings("unchecked")
                final
                LinkedList<Integer> otherList = (LinkedList<Integer>) o;
                for (final Integer i : this) {
                    final Integer other = otherList.get(this.indexOf(i));
                    if (!other.equals(i)) {
                        return false;
                    }
                }
            }
            return true;
        }
    };
    private LinkedList<Integer> citizensToKill = new LinkedList<Integer>() {

        private static final long serialVersionUID = 7705964814014176415L;

        @Override
        public boolean equals(final Object o) {
            if (o instanceof LinkedList) {
                @SuppressWarnings("unchecked")
                final
                LinkedList<Integer> otherList = (LinkedList<Integer>) o;
                for (final Integer i : this) {
                    final Integer other = otherList.get(this.indexOf(i));
                    if (!other.equals(i)) {
                        return false;
                    }
                }
            }
            return true;
        }
    };
    private LinkedList<Integer> citizenNumToKill = new LinkedList<>();
    private LinkedList<EntityType> mobsToKill = new LinkedList<>();
    private LinkedList<Integer> mobNumToKill = new LinkedList<>();
    private LinkedList<Location> locationsToKillWithin = new LinkedList<>();
    private LinkedList<Integer> radiiToKillWithin = new LinkedList<>();
    private LinkedList<String> killNames = new LinkedList<>();
    private LinkedList<EntityType> mobsToTame = new LinkedList<>();
    private LinkedList<Integer> mobNumToTame = new LinkedList<>();
    private Integer fishToCatch;
    private Integer cowsToMilk;
    private LinkedList<DyeColor> sheepToShear = new LinkedList<>();
    private LinkedList<Integer> sheepNumToShear = new LinkedList<>();
    private Integer playersToKill;
    private LinkedList<Location> locationsToReach = new LinkedList<>();
    private LinkedList<Integer> radiiToReachWithin = new LinkedList<>();
    private LinkedList<World> worldsToReachWithin = new LinkedList<>();
    private LinkedList<String> locationNames = new LinkedList<>();
    private LinkedList<String> passwordDisplays = new LinkedList<>();
    private LinkedList<String> passwordPhrases = new LinkedList<>();
    private String script;
    private IAction startAction = null;
    private IAction finishAction = null;
    private IAction failAction = null;
    private IAction deathAction = null;
    private Map<String, IAction> chatActions = new HashMap<>();
    private Map<String, IAction> commandActions = new HashMap<>();
    private IAction disconnectAction = null;
    private ICondition condition = null;
    private long delay = -1;
    private String delayMessage = null;
    private String completeMessage = null;
    private String startMessage = null;
    private LinkedList<String> objectiveOverrides = new LinkedList<>();
    private LinkedList<ICustomObjective> customObjectives = new LinkedList<>();
    private LinkedList<Integer> customObjectiveCounts = new LinkedList<>();
    private LinkedList<String> customObjectiveDisplays = new LinkedList<>();
    private LinkedList<Entry<String, Object>> customObjectiveData = new LinkedList<>();
    
    public LinkedList<ItemStack> getBlocksToBreak() {
        return blocksToBreak;
    }

    @Override
    public boolean addBlockToBreak(ItemStack blockToBreak) {
        return blocksToBreak.add(blockToBreak);
    }

    public void setBlocksToBreak(final LinkedList<ItemStack> blocksToBreak) {
        this.blocksToBreak = blocksToBreak;
    }

    public LinkedList<ItemStack> getBlocksToDamage() {
        return blocksToDamage;
    }

    @Override
    public boolean addBlockToDamage(ItemStack blockToDamage) {
        return blocksToDamage.add(blockToDamage);
    }

    public void setBlocksToDamage(final LinkedList<ItemStack> blocksToDamage) {
        this.blocksToDamage = blocksToDamage;
    }

    public LinkedList<ItemStack> getBlocksToPlace() {
        return blocksToPlace;
    }

    @Override
    public boolean addBlockToPlace(ItemStack blockToPlace) {
        return blocksToPlace.add(blockToPlace);
    }

    public void setBlocksToPlace(final LinkedList<ItemStack> blocksToPlace) {
        this.blocksToPlace = blocksToPlace;
    }

    public LinkedList<ItemStack> getBlocksToUse() {
        return blocksToUse;
    }

    @Override
    public boolean addBlockToUse(ItemStack blockToUse) {
        return blocksToUse.add(blockToUse);
    }

    public void setBlocksToUse(final LinkedList<ItemStack> blocksToUse) {
        this.blocksToUse = blocksToUse;
    }

    public LinkedList<ItemStack> getBlocksToCut() {
        return blocksToCut;
    }

    @Override
    public boolean addBlockToCut(ItemStack blockToCut) {
        return blocksToCut.add(blockToCut);
    }

    public void setBlocksToCut(final LinkedList<ItemStack> blocksToCut) {
        this.blocksToCut = blocksToCut;
    }
    
    public LinkedList<ItemStack> getItemsToCraft() {
        return itemsToCraft;
    }

    @Override
    public boolean addItemToCraft(ItemStack itemToCraft) {
        return itemsToCraft.add(itemToCraft);
    }

    public void setItemsToCraft(final LinkedList<ItemStack> itemsToCraft) {
        this.itemsToCraft = itemsToCraft;
    }
    
    public LinkedList<ItemStack> getItemsToSmelt() {
        return itemsToSmelt;
    }

    @Override
    public boolean addItemToSmelt(ItemStack itemToSmelt) {
        return itemsToSmelt.add(itemToSmelt);
    }

    public void setItemsToSmelt(final LinkedList<ItemStack> itemsToSmelt) {
        this.itemsToSmelt = itemsToSmelt;
    }

    public LinkedList<ItemStack> getItemsToEnchant() {
        return itemsToEnchant;
    }

    @Override
    public boolean addItemToEnchant(ItemStack itemToEnchant) {
        return itemsToEnchant.add(itemToEnchant);
    }

    public void setItemsToEnchant(final LinkedList<ItemStack> itemsToEnchant) {
        this.itemsToEnchant = itemsToEnchant;
    }
    
    public LinkedList<ItemStack> getItemsToBrew() {
        return itemsToBrew;
    }

    @Override
    public boolean addItemsToBrew(ItemStack itemToBrew) {
        return itemsToBrew.add(itemToBrew);
    }

    public void setItemsToBrew(final LinkedList<ItemStack> itemsToBrew) {
        this.itemsToBrew = itemsToBrew;
    }
    
    public LinkedList<ItemStack> getItemsToConsume() {
        return itemsToConsume;
    }

    @Override
    public boolean addItemToConsume(ItemStack itemToConsume) {
        return itemsToConsume.add(itemToConsume);
    }

    public void setItemsToConsume(final LinkedList<ItemStack> itemsToConsume) {
        this.itemsToBrew = itemsToConsume;
    }

    public LinkedList<ItemStack> getItemsToDeliver() {
        return itemsToDeliver;
    }

    @Override
    public boolean addItemToDeliver(ItemStack itemToDeliver) {
        return itemsToDeliver.add(itemToDeliver);
    }

    public void setItemsToDeliver(final LinkedList<ItemStack> itemsToDeliver) {
        this.itemsToDeliver = itemsToDeliver;
    }

    public LinkedList<Integer> getItemDeliveryTargets() {
        return itemDeliveryTargets;
    }

    @Override
    public boolean addItemDeliveryTarget(Integer itemDeliveryTarget) {
        return itemDeliveryTargets.add(itemDeliveryTarget);
    }

    public void setItemDeliveryTargets(final LinkedList<Integer> itemDeliveryTargets) {
        this.itemDeliveryTargets = itemDeliveryTargets;
    }

    public LinkedList<String> getDeliverMessages() {
        return deliverMessages;
    }

    @Override
    public boolean addDeliverMessage(String deliverMessage) {
        return deliverMessages.add(deliverMessage);
    }

    public void setDeliverMessages(final LinkedList<String> deliverMessages) {
        this.deliverMessages = deliverMessages;
    }

    public LinkedList<Integer> getCitizensToInteract() {
        return citizensToInteract;
    }

    @Override
    public boolean addCitizenToInteract(Integer citizenToInteract) {
        return citizensToInteract.add(citizenToInteract);
    }

    public void setCitizensToInteract(final LinkedList<Integer> citizensToInteract) {
        this.citizensToInteract = citizensToInteract;
    }

    public LinkedList<Integer> getCitizensToKill() {
        return citizensToKill;
    }

    @Override
    public boolean addCitizenToKill(Integer citizenToKill) {
        return citizensToKill.add(citizenToKill);
    }

    public void setCitizensToKill(final LinkedList<Integer> citizensToKill) {
        this.citizensToKill = citizensToKill;
    }

    public LinkedList<Integer> getCitizenNumToKill() {
        return citizenNumToKill;
    }

    @Override
    public boolean addCitizenNumToKill(Integer citizenNumToKill) {
        return this.citizenNumToKill.add(citizenNumToKill);
    }

    public void setCitizenNumToKill(final LinkedList<Integer> citizenNumToKill) {
        this.citizenNumToKill = citizenNumToKill;
    }

    public LinkedList<EntityType> getMobsToKill() {
        return mobsToKill;
    }

    @Override
    public boolean addMobToKill(EntityType mobToKill) {
        return mobsToKill.add(mobToKill);
    }

    public void setMobsToKill(final LinkedList<EntityType> mobsToKill) {
        this.mobsToKill = mobsToKill;
    }

    public LinkedList<Integer> getMobNumToKill() {
        return mobNumToKill;
    }

    @Override
    public boolean addMobNumToKill(Integer mobNumToKill) {
        return this.mobNumToKill.add(mobNumToKill);
    }

    public void setMobNumToKill(final LinkedList<Integer> mobNumToKill) {
        this.mobNumToKill = mobNumToKill;
    }

    public LinkedList<Location> getLocationsToKillWithin() {
        return locationsToKillWithin;
    }

    @Override
    public boolean addLocationToKillWithin(Location locationToKillWithin) {
        return locationsToKillWithin.add(locationToKillWithin);
    }

    public void setLocationsToKillWithin(final LinkedList<Location> locationsToKillWithin) {
        this.locationsToKillWithin = locationsToKillWithin;
    }

    public LinkedList<Integer> getRadiiToKillWithin() {
        return radiiToKillWithin;
    }

    @Override
    public boolean addRadiusToKillWithin(Integer radiusToKillWithin) {
        return radiiToKillWithin.add(radiusToKillWithin);
    }

    public void setRadiiToKillWithin(final LinkedList<Integer> radiiToKillWithin) {
        this.radiiToKillWithin = radiiToKillWithin;
    }
    
    public LinkedList<String> getKillNames() {
        return killNames;
    }

    @Override
    public boolean addKillName(String killName) {
        return killNames.add(killName);
    }

    public void setKillNames(final LinkedList<String> killNames) {
        this.killNames = killNames;
    }

    public LinkedList<Location> getLocationsToReach() {
        return locationsToReach;
    }

    @Override
    public boolean addLocationToReach(Location locationToReach) {
        return locationsToReach.add(locationToReach);
    }

    public void setLocationsToReach(final LinkedList<Location> locationsToReach) {
        this.locationsToReach = locationsToReach;
    }

    public LinkedList<Integer> getRadiiToReachWithin() {
        return radiiToReachWithin;
    }

    @Override
    public boolean addRadiusToReachWithin(Integer radiusToReachWithin) {
        return radiiToReachWithin.add(radiusToReachWithin);
    }

    public void setRadiiToReachWithin(final LinkedList<Integer> radiiToReachWithin) {
        this.radiiToReachWithin = radiiToReachWithin;
    }

    public LinkedList<World> getWorldsToReachWithin() {
        return worldsToReachWithin;
    }

    @Override
    public boolean addWorldToReachWithin(World worldToReachWithin) {
        return worldsToReachWithin.add(worldToReachWithin);
    }

    public void setWorldsToReachWithin(final LinkedList<World> worldsToReachWithin) {
        this.worldsToReachWithin = worldsToReachWithin;
    }

    public LinkedList<String> getLocationNames() {
        return locationNames;
    }

    @Override
    public boolean addLocationName(String locationName) {
        return locationNames.add(locationName);
    }

    public void setLocationNames(final LinkedList<String> locationNames) {
        this.locationNames = locationNames;
    }

    public LinkedList<EntityType> getMobsToTame() {
        return mobsToTame;
    }

    @Override
    public boolean addMobToTame(EntityType mobToTame) {
        return mobsToTame.add(mobToTame);
    }

    public void setMobsToTame(final LinkedList<EntityType> mobsToTame) {
        this.mobsToTame = mobsToTame;
    }

    public LinkedList<Integer> getMobNumToTame() {
        return mobNumToTame;
    }

    @Override
    public boolean addMobNumToTame(Integer mobNumToTame) {
        return this.mobNumToTame.add(mobNumToTame);
    }

    public void setMobNumToTame(final LinkedList<Integer> mobNumToTame) {
        this.mobNumToTame = mobNumToTame;
    }

    public Integer getFishToCatch() {
        return fishToCatch;
    }

    public void setFishToCatch(final Integer fishToCatch) {
        this.fishToCatch = fishToCatch;
    }

    public Integer getCowsToMilk() {
        return cowsToMilk;
    }

    public void setCowsToMilk(final Integer cowsToMilk) {
        this.cowsToMilk = cowsToMilk;
    }

    public Integer getPlayersToKill() {
        return playersToKill;
    }

    public void setPlayersToKill(final Integer playersToKill) {
        this.playersToKill = playersToKill;
    }

    public LinkedList<DyeColor> getSheepToShear() {
        return sheepToShear;
    }

    @Override
    public boolean addSheepToShear(DyeColor sheepToShear) {
        return this.sheepToShear.add(sheepToShear);
    }

    public void setSheepToShear(final LinkedList<DyeColor> sheepToShear) {
        this.sheepToShear = sheepToShear;
    }

    public LinkedList<Integer> getSheepNumToShear() {
        return sheepNumToShear;
    }

    @Override
    public boolean addSheepNumToShear(Integer sheepNumToShear) {
        return this.sheepNumToShear.add(sheepNumToShear);
    }

    public void setSheepNumToShear(final LinkedList<Integer> sheepNumToShear) {
        this.sheepNumToShear = sheepNumToShear;
    }

    public LinkedList<String> getPasswordDisplays() {
        return passwordDisplays;
    }

    @Override
    public boolean addPasswordDisplay(String passwordDisplay) {
        return passwordDisplays.add(passwordDisplay);
    }

    public void setPasswordDisplays(final LinkedList<String> passwordDisplays) {
        this.passwordDisplays = passwordDisplays;
    }

    public LinkedList<String> getPasswordPhrases() {
        return passwordPhrases;
    }

    @Override
    public boolean addPasswordPhrase(String passwordPhrase) {
        return passwordPhrases.add(passwordPhrase);
    }

    public void setPasswordPhrases(final LinkedList<String> passwordPhrases) {
        this.passwordPhrases = passwordPhrases;
    }

    public String getScript() {
        return script;
    }

    public void setScript(final String script) {
        this.script = script;
    }

    public IAction getStartAction() {
        return startAction;
    }

    public void setStartAction(final IAction startAction) {
        this.startAction = startAction;
    }
    
    public IAction getFinishAction() {
        return finishAction;
    }

    public void setFinishAction(final IAction finishAction) {
        this.finishAction = finishAction;
    }
    
    public IAction getFailAction() {
        return failAction;
    }

    public void setFailAction(final IAction failAction) {
        this.failAction = failAction;
    }

    public IAction getDeathAction() {
        return deathAction;
    }

    public void setDeathAction(final IAction deathAction) {
        this.deathAction = deathAction;
    }

    public Map<String, IAction> getChatActions() {
        return chatActions;
    }

    @Override
    public void addChatAction(Entry<String, IAction> chatAction) {
        chatActions.put(chatAction.getKey(), chatAction.getValue());
    }

    public void setChatActions(final Map<String, IAction> chatActions) {
        this.chatActions = chatActions;
    }

    public Map<String, IAction> getCommandActions() {
        return commandActions;
    }

    @Override
    public void addCommandAction(Entry<String, IAction> commandAction) {
        commandActions.put(commandAction.getKey(), commandAction.getValue());
    }

    public void setCommandActions(final Map<String, IAction> commandActions) {
        this.commandActions = commandActions;
    }

    public IAction getDisconnectAction() {
        return disconnectAction;
    }

    public void setDisconnectAction(final IAction disconnectAction) {
        this.disconnectAction = disconnectAction;
    }
    
    public ICondition getCondition() {
        return condition;
    }
    
    public void setCondition(final ICondition condition) {
        this.condition = condition;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(final long delay) {
        this.delay = delay;
    }

    public String getDelayMessage() {
        return delayMessage;
    }

    public void setDelayMessage(final String delayMessage) {
        this.delayMessage = delayMessage;
    }

    public String getCompleteMessage() {
        return completeMessage;
    }

    public void setCompleteMessage(final String completeMessage) {
        this.completeMessage = completeMessage;
    }

    public String getStartMessage() {
        return startMessage;
    }

    public void setStartMessage(final String startMessage) {
        this.startMessage = startMessage;
    }

    public LinkedList<String> getObjectiveOverrides() {
        return objectiveOverrides;
    }

    @Override
    public boolean addObjectiveOverride(String objectiveOverride) {
        return objectiveOverrides.add(objectiveOverride);
    }

    public void setObjectiveOverrides(final LinkedList<String> objectiveOverrides) {
        this.objectiveOverrides = objectiveOverrides;
    }
    
    public LinkedList<ICustomObjective> getCustomObjectives() {
        return customObjectives;
    }

    public boolean addCustomObjectives(final ICustomObjective customObjective) {
        return customObjectives.add(customObjective);
    }

    public void clearCustomObjectives() {
        customObjectives.clear();
    }

    public LinkedList<Integer> getCustomObjectiveCounts() {
        return customObjectiveCounts;
    }

    public boolean addCustomObjectiveCounts(final Integer customObjectiveCount) {
        return customObjectiveCounts.add(customObjectiveCount);
    }

    public void clearCustomObjectiveCounts() {
        customObjectiveCounts.clear();
    }

    public LinkedList<String> getCustomObjectiveDisplays() {
        return customObjectiveDisplays;
    }

    public void clearCustomObjectiveDisplays() {
        customObjectiveDisplays.clear();
    }

    public LinkedList<Entry<String, Object>> getCustomObjectiveData() {
        return customObjectiveData;
    }

    public boolean addCustomObjectiveData(final Entry<String, Object> customObjectiveDatum) {
        return this.customObjectiveData.add(customObjectiveDatum);
    }

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
    public boolean hasObjective() {
        if (!blocksToBreak.isEmpty()) { return true; }
        if (!blocksToDamage.isEmpty()) { return true; }
        if (!blocksToPlace.isEmpty()) { return true; }
        if (!blocksToUse.isEmpty()) { return true; }
        if (!blocksToCut.isEmpty()) { return true; }
        if (cowsToMilk != null) { return true; }
        if (fishToCatch != null) { return true; }
        if (playersToKill != null) { return true; }
        if (!itemsToCraft.isEmpty()) { return true; }
        if (!itemsToSmelt.isEmpty()) { return true; }
        if (!itemsToEnchant.isEmpty()) { return true; }
        if (!itemsToBrew.isEmpty()) { return true; }
        if (!itemsToConsume.isEmpty()) { return true; }
        if (!itemsToDeliver.isEmpty()) { return true; }
        if (!citizensToInteract.isEmpty()) { return true; }
        if (!citizensToKill.isEmpty()) { return true; }
        if (!locationsToReach.isEmpty()) { return true; }
        if (!mobsToTame.isEmpty()) { return true; }
        if (!sheepToShear.isEmpty()) { return true; }
        if (!passwordDisplays.isEmpty()) { return true; }
        return !customObjectives.isEmpty();
    }
    
    /**
     * Check if stage has the specified type of objective<p>
     * 
     * @param type The type of objective to check for
     * @return true if stage contains specified objective
     */
    public boolean containsObjective(final ObjectiveType type) {
        if (type.equals(ObjectiveType.BREAK_BLOCK)) {
            return !blocksToBreak.isEmpty();
        } else if (type.equals(ObjectiveType.DAMAGE_BLOCK)) {
            return !blocksToDamage.isEmpty();
        } else if (type.equals(ObjectiveType.PLACE_BLOCK)) {
            return !blocksToPlace.isEmpty();
        } else if (type.equals(ObjectiveType.USE_BLOCK)) {
            return !blocksToUse.isEmpty();
        } else if (type.equals(ObjectiveType.CUT_BLOCK)) {
            return !blocksToCut.isEmpty();
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
            return !citizensToInteract.isEmpty();
        } else if (type.equals(ObjectiveType.KILL_NPC)) {
            return !citizensToKill.isEmpty();
        } else if (type.equals(ObjectiveType.TAME_MOB)) {
            return !mobsToTame.isEmpty();
        } else if (type.equals(ObjectiveType.SHEAR_SHEEP)) {
            return !sheepToShear.isEmpty();
        } else if (type.equals(ObjectiveType.REACH_LOCATION)) {
            return !locationsToReach.isEmpty();
        } else if (type.equals(ObjectiveType.PASSWORD)) {
            return !passwordPhrases.isEmpty();
        } else {
            return false;
        }
    }
}
