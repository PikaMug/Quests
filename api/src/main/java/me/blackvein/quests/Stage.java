package me.blackvein.quests;

import me.blackvein.quests.actions.Action;
import me.blackvein.quests.conditions.Condition;
import me.blackvein.quests.enums.ObjectiveType;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;
import java.util.Map;

public interface Stage {
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

    Action getStartAction();

    void setStartAction(final Action startAction);

    Action getFinishAction();

    void setFinishAction(final Action finishAction);

    Action getFailAction();

    void setFailAction(final Action failAction);

    Action getDeathAction();

    void setDeathAction(final Action deathAction);

    Map<String, Action> getChatActions();

    void addChatAction(Map.Entry<String, Action> chatAction);

    void setChatActions(final Map<String, Action> chatActions);

    Map<String, Action> getCommandActions();

    void addCommandAction(Map.Entry<String, Action> commandAction);

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

    boolean addObjectiveOverride(String objectiveOverride);

    void setObjectiveOverrides(final LinkedList<String> objectiveOverrides);

    LinkedList<CustomObjective> getCustomObjectives();

    boolean addCustomObjectives(final CustomObjective customObjective);

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
     * Check if stage has the specified type of objective<p>
     *
     * @param type The type of objective to check for
     * @return true if stage contains specified objective
     */
    boolean containsObjective(final ObjectiveType type);
}
