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
import me.pikamug.quests.quests.components.Stage;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

public class BukkitStage implements Stage {

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
    private  LinkedList<String> deliverMessages = new LinkedList<>();
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
    
    public LinkedList<ItemStack> getBlocksToBreak() {
        return blocksToBreak;
    }

    public boolean addBlockToBreak(@NotNull ItemStack blockToBreak) {
        return blocksToBreak.add(blockToBreak);
    }

    public void setBlocksToBreak(final LinkedList<ItemStack> blocksToBreak) {
        this.blocksToBreak = blocksToBreak;
    }

    public LinkedList<ItemStack> getBlocksToDamage() {
        return blocksToDamage;
    }

    public boolean addBlockToDamage(@NotNull ItemStack blockToDamage) {
        return blocksToDamage.add(blockToDamage);
    }

    public void setBlocksToDamage(final LinkedList<ItemStack> blocksToDamage) {
        this.blocksToDamage = blocksToDamage;
    }

    public LinkedList<ItemStack> getBlocksToPlace() {
        return blocksToPlace;
    }

    public boolean addBlockToPlace(@NotNull ItemStack blockToPlace) {
        return blocksToPlace.add(blockToPlace);
    }

    public void setBlocksToPlace(final LinkedList<ItemStack> blocksToPlace) {
        this.blocksToPlace = blocksToPlace;
    }

    public LinkedList<ItemStack> getBlocksToUse() {
        return blocksToUse;
    }

    public boolean addBlockToUse(@NotNull ItemStack blockToUse) {
        return blocksToUse.add(blockToUse);
    }

    public void setBlocksToUse(final LinkedList<ItemStack> blocksToUse) {
        this.blocksToUse = blocksToUse;
    }

    public LinkedList<ItemStack> getBlocksToCut() {
        return blocksToCut;
    }

    public boolean addBlockToCut(@NotNull ItemStack blockToCut) {
        return blocksToCut.add(blockToCut);
    }

    public void setBlocksToCut(final LinkedList<ItemStack> blocksToCut) {
        this.blocksToCut = blocksToCut;
    }
    
    public LinkedList<ItemStack> getItemsToCraft() {
        return itemsToCraft;
    }

    public boolean addItemToCraft(@NotNull ItemStack itemToCraft) {
        return itemsToCraft.add(itemToCraft);
    }

    public void setItemsToCraft(final LinkedList<ItemStack> itemsToCraft) {
        this.itemsToCraft = itemsToCraft;
    }
    
    public LinkedList<ItemStack> getItemsToSmelt() {
        return itemsToSmelt;
    }

    public boolean addItemToSmelt(@NotNull ItemStack itemToSmelt) {
        return itemsToSmelt.add(itemToSmelt);
    }

    public void setItemsToSmelt(final LinkedList<ItemStack> itemsToSmelt) {
        this.itemsToSmelt = itemsToSmelt;
    }

    public LinkedList<ItemStack> getItemsToEnchant() {
        return itemsToEnchant;
    }

    public boolean addItemToEnchant(@NotNull ItemStack itemToEnchant) {
        return itemsToEnchant.add(itemToEnchant);
    }

    public void setItemsToEnchant(final LinkedList<ItemStack> itemsToEnchant) {
        this.itemsToEnchant = itemsToEnchant;
    }
    
    public LinkedList<ItemStack> getItemsToBrew() {
        return itemsToBrew;
    }

    public boolean addItemsToBrew(@NotNull ItemStack itemToBrew) {
        return itemsToBrew.add(itemToBrew);
    }

    public void setItemsToBrew(final LinkedList<ItemStack> itemsToBrew) {
        this.itemsToBrew = itemsToBrew;
    }
    
    public LinkedList<ItemStack> getItemsToConsume() {
        return itemsToConsume;
    }

    public boolean addItemToConsume(@NotNull ItemStack itemToConsume) {
        return itemsToConsume.add(itemToConsume);
    }

    public void setItemsToConsume(final LinkedList<ItemStack> itemsToConsume) {
        this.itemsToConsume = itemsToConsume;
    }

    public LinkedList<ItemStack> getItemsToDeliver() {
        return itemsToDeliver;
    }

    public boolean addItemToDeliver(@NotNull ItemStack itemToDeliver) {
        return itemsToDeliver.add(itemToDeliver);
    }

    public void setItemsToDeliver(final LinkedList<ItemStack> itemsToDeliver) {
        this.itemsToDeliver = itemsToDeliver;
    }

    public LinkedList<UUID> getItemDeliveryTargets() {
        return itemDeliveryTargets;
    }

    public boolean addItemDeliveryTarget(UUID itemDeliveryTarget) {
        return itemDeliveryTargets.add(itemDeliveryTarget);
    }

    public void setItemDeliveryTargets(final LinkedList<UUID> itemDeliveryTargets) {
        this.itemDeliveryTargets = itemDeliveryTargets;
    }

    public LinkedList<String> getDeliverMessages() {
        return deliverMessages;
    }

    public boolean addDeliverMessage(String deliverMessage) {
        return deliverMessages.add(deliverMessage);
    }

    public void setDeliverMessages(final LinkedList<String> deliverMessages) {
        this.deliverMessages = deliverMessages;
    }

    public LinkedList<UUID> getNpcsToInteract() {
        return npcsToInteract;
    }

    public boolean addNpcToInteract(UUID npcToInteract) {
        return npcsToInteract.add(npcToInteract);
    }

    public void setNpcsToInteract(final LinkedList<UUID> npcsToInteract) {
        this.npcsToInteract = npcsToInteract;
    }

    public LinkedList<UUID> getNpcsToKill() {
        return npcsToKill;
    }

    public boolean addNpcToKill(UUID npcToKill) {
        return npcsToKill.add(npcToKill);
    }

    public void setNpcsToKill(final LinkedList<UUID> npcsToKill) {
        this.npcsToKill = npcsToKill;
    }

    public LinkedList<Integer> getNpcNumToKill() {
        return npcNumToKill;
    }

    public boolean addNpcNumToKill(Integer npcNumToKill) {
        return this.npcNumToKill.add(npcNumToKill);
    }

    public void setNpcNumToKill(final LinkedList<Integer> npcNumToKill) {
        this.npcNumToKill = npcNumToKill;
    }

    public LinkedList<EntityType> getMobsToKill() {
        return mobsToKill;
    }

    public boolean addMobToKill(EntityType mobToKill) {
        return mobsToKill.add(mobToKill);
    }

    public void setMobsToKill(final LinkedList<EntityType> mobsToKill) {
        this.mobsToKill = mobsToKill;
    }

    public LinkedList<Integer> getMobNumToKill() {
        return mobNumToKill;
    }

    public boolean addMobNumToKill(Integer mobNumToKill) {
        return this.mobNumToKill.add(mobNumToKill);
    }

    public void setMobNumToKill(final LinkedList<Integer> mobNumToKill) {
        this.mobNumToKill = mobNumToKill;
    }

    public LinkedList<Location> getLocationsToKillWithin() {
        return locationsToKillWithin;
    }

    public boolean addLocationToKillWithin(Location locationToKillWithin) {
        return locationsToKillWithin.add(locationToKillWithin);
    }

    public void setLocationsToKillWithin(final LinkedList<Location> locationsToKillWithin) {
        this.locationsToKillWithin = locationsToKillWithin;
    }

    public LinkedList<Integer> getRadiiToKillWithin() {
        return radiiToKillWithin;
    }

    public boolean addRadiusToKillWithin(Integer radiusToKillWithin) {
        return radiiToKillWithin.add(radiusToKillWithin);
    }

    public void setRadiiToKillWithin(final LinkedList<Integer> radiiToKillWithin) {
        this.radiiToKillWithin = radiiToKillWithin;
    }
    
    public LinkedList<String> getKillNames() {
        return killNames;
    }

    public boolean addKillName(String killName) {
        return killNames.add(killName);
    }

    public void setKillNames(final LinkedList<String> killNames) {
        this.killNames = killNames;
    }

    public LinkedList<Location> getLocationsToReach() {
        return locationsToReach;
    }

    public boolean addLocationToReach(Location locationToReach) {
        return locationsToReach.add(locationToReach);
    }

    public void setLocationsToReach(final LinkedList<Location> locationsToReach) {
        this.locationsToReach = locationsToReach;
    }

    public LinkedList<Integer> getRadiiToReachWithin() {
        return radiiToReachWithin;
    }

    public boolean addRadiusToReachWithin(Integer radiusToReachWithin) {
        return radiiToReachWithin.add(radiusToReachWithin);
    }

    public void setRadiiToReachWithin(final LinkedList<Integer> radiiToReachWithin) {
        this.radiiToReachWithin = radiiToReachWithin;
    }

    public LinkedList<World> getWorldsToReachWithin() {
        return worldsToReachWithin;
    }

    public boolean addWorldToReachWithin(World worldToReachWithin) {
        return worldsToReachWithin.add(worldToReachWithin);
    }

    public void setWorldsToReachWithin(final LinkedList<World> worldsToReachWithin) {
        this.worldsToReachWithin = worldsToReachWithin;
    }

    public LinkedList<String> getLocationNames() {
        return locationNames;
    }

    public boolean addLocationName(String locationName) {
        return locationNames.add(locationName);
    }

    public void setLocationNames(final LinkedList<String> locationNames) {
        this.locationNames = locationNames;
    }

    public LinkedList<EntityType> getMobsToTame() {
        return mobsToTame;
    }

    public boolean addMobToTame(EntityType mobToTame) {
        return mobsToTame.add(mobToTame);
    }

    public void setMobsToTame(final LinkedList<EntityType> mobsToTame) {
        this.mobsToTame = mobsToTame;
    }

    public LinkedList<Integer> getMobNumToTame() {
        return mobNumToTame;
    }

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

    public boolean addSheepToShear(DyeColor sheepToShear) {
        return this.sheepToShear.add(sheepToShear);
    }

    public void setSheepToShear(final LinkedList<DyeColor> sheepToShear) {
        this.sheepToShear = sheepToShear;
    }

    public LinkedList<Integer> getSheepNumToShear() {
        return sheepNumToShear;
    }

    public boolean addSheepNumToShear(Integer sheepNumToShear) {
        return this.sheepNumToShear.add(sheepNumToShear);
    }

    public void setSheepNumToShear(final LinkedList<Integer> sheepNumToShear) {
        this.sheepNumToShear = sheepNumToShear;
    }

    public LinkedList<String> getPasswordDisplays() {
        return passwordDisplays;
    }

    public boolean addPasswordDisplay(String passwordDisplay) {
        return passwordDisplays.add(passwordDisplay);
    }

    public void setPasswordDisplays(final LinkedList<String> passwordDisplays) {
        this.passwordDisplays = passwordDisplays;
    }

    public LinkedList<String> getPasswordPhrases() {
        return passwordPhrases;
    }

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

    public Action getStartAction() {
        return startAction;
    }

    public void setStartAction(final Action startAction) {
        this.startAction = startAction;
    }
    
    public Action getFinishAction() {
        return finishAction;
    }

    public void setFinishAction(final Action finishAction) {
        this.finishAction = finishAction;
    }
    
    public Action getFailAction() {
        return failAction;
    }

    public void setFailAction(final Action failAction) {
        this.failAction = failAction;
    }

    public Action getDeathAction() {
        return deathAction;
    }

    public void setDeathAction(final Action deathAction) {
        this.deathAction = deathAction;
    }

    public Map<String, Action> getChatActions() {
        return chatActions;
    }

    public void addChatAction(Entry<String, Action> chatAction) {
        chatActions.put(chatAction.getKey(), chatAction.getValue());
    }

    public void setChatActions(final Map<String, Action> chatActions) {
        this.chatActions = chatActions;
    }

    public Map<String, Action> getCommandActions() {
        return commandActions;
    }

    public void addCommandAction(Entry<String, Action> commandAction) {
        commandActions.put(commandAction.getKey(), commandAction.getValue());
    }

    public void setCommandActions(final Map<String, Action> commandActions) {
        this.commandActions = commandActions;
    }

    public Action getDisconnectAction() {
        return disconnectAction;
    }

    public void setDisconnectAction(final Action disconnectAction) {
        this.disconnectAction = disconnectAction;
    }
    
    public Condition getCondition() {
        return condition;
    }
    
    public void setCondition(final Condition condition) {
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

    public boolean addObjectiveOverride(String objectiveOverride) {
        return objectiveOverrides.add(objectiveOverride);
    }

    public void setObjectiveOverrides(final LinkedList<String> objectiveOverrides) {
        this.objectiveOverrides = objectiveOverrides;
    }
    
    public LinkedList<CustomObjective> getCustomObjectives() {
        return customObjectives;
    }

    public boolean addCustomObjectives(final CustomObjective customObjective) {
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
        if (!npcsToInteract.isEmpty()) { return true; }
        if (!npcsToKill.isEmpty()) { return true; }
        if (!locationsToReach.isEmpty()) { return true; }
        if (!mobsToKill.isEmpty()) {return true; }
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
    public boolean hasLocatableObjective() {
        if (!npcsToInteract.isEmpty()) { return true; }
        if (!npcsToKill.isEmpty()) { return true; }
        if (!locationsToReach.isEmpty()) { return true; }
        if (!itemDeliveryTargets.isEmpty()) { return true; }
        if (playersToKill != null) { return true; }
        if (!mobsToKill.isEmpty()) {return true; }
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
        } else {
            return false;
        }
    }
}
