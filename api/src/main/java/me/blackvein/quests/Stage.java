package me.blackvein.quests;

import me.blackvein.quests.actions.Action;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;
import java.util.Map;

public interface Stage {
    LinkedList<ItemStack> getBlocksToBreak();

    void setBlocksToBreak(final LinkedList<ItemStack> blocksToBreak);

    LinkedList<ItemStack> getBlocksToDamage();

    void setBlocksToDamage(final LinkedList<ItemStack> blocksToDamage);

    LinkedList<ItemStack> getBlocksToPlace();

    void setBlocksToPlace(final LinkedList<ItemStack> blocksToPlace);

    LinkedList<ItemStack> getBlocksToUse();

    void setBlocksToUse(final LinkedList<ItemStack> blocksToUse);

    LinkedList<ItemStack> getBlocksToCut();

    void setBlocksToCut(final LinkedList<ItemStack> blocksToCut);

    LinkedList<ItemStack> getItemsToCraft();

    void setItemsToCraft(final LinkedList<ItemStack> itemsToCraft);

    LinkedList<ItemStack> getItemsToSmelt();

    void setItemsToSmelt(final LinkedList<ItemStack> itemsToSmelt);

    LinkedList<ItemStack> getItemsToEnchant();

    void setItemsToEnchant(final LinkedList<ItemStack> itemsToEnchant);

    LinkedList<ItemStack> getItemsToBrew();

    void setItemsToBrew(final LinkedList<ItemStack> itemsToBrew);

    LinkedList<ItemStack> getItemsToConsume();

    void setItemsToConsume(final LinkedList<ItemStack> itemsToConsume);

    LinkedList<ItemStack> getItemsToDeliver();

    void setItemsToDeliver(final LinkedList<ItemStack> itemsToDeliver);

    LinkedList<Integer> getItemDeliveryTargets();

    void setItemDeliveryTargets(final LinkedList<Integer> itemDeliveryTargets);

    LinkedList<String> getDeliverMessages();

    void setDeliverMessages(final LinkedList<String> deliverMessages);

    LinkedList<Integer> getCitizensToInteract();

    void setCitizensToInteract(final LinkedList<Integer> citizensToInteract);

    LinkedList<Integer> getCitizensToKill();

    void setCitizensToKill(final LinkedList<Integer> citizensToKill);

    LinkedList<Integer> getCitizenNumToKill();

    void setCitizenNumToKill(final LinkedList<Integer> citizenNumToKill);

    LinkedList<EntityType> getMobsToKill();

    void setMobsToKill(final LinkedList<EntityType> mobsToKill);

    LinkedList<Integer> getMobNumToKill();

    void setMobNumToKill(final LinkedList<Integer> mobNumToKill);

    LinkedList<Location> getLocationsToKillWithin();

    void setLocationsToKillWithin(final LinkedList<Location> locationsToKillWithin);

    LinkedList<Integer> getRadiiToKillWithin();

    void setRadiiToKillWithin(final LinkedList<Integer> radiiToKillWithin);

    LinkedList<String> getKillNames();

    void setKillNames(final LinkedList<String> killNames);

    LinkedList<Location> getLocationsToReach();

    void setLocationsToReach(final LinkedList<Location> locationsToReach);

    LinkedList<Integer> getRadiiToReachWithin();

    void setRadiiToReachWithin(final LinkedList<Integer> radiiToReachWithin);

    LinkedList<World> getWorldsToReachWithin();

    void setWorldsToReachWithin(final LinkedList<World> worldsToReachWithin);

    LinkedList<String> getLocationNames();

    void setLocationNames(final LinkedList<String> locationNames);

    LinkedList<EntityType> getMobsToTame();

    void setMobsToTame(final LinkedList<EntityType> mobsToTame);

    LinkedList<Integer> getMobNumToTame();

    void setMobNumToTame(final LinkedList<Integer> mobNumToTame);

    Integer getFishToCatch();

    void setFishToCatch(final Integer fishToCatch);

    Integer getCowsToMilk();

    void setCowsToMilk(final Integer cowsToMilk);

    Integer getPlayersToKill();

    void setPlayersToKill(final Integer playersToKill);

    LinkedList<DyeColor> getSheepToShear();

    void setSheepToShear(final LinkedList<DyeColor> sheepToShear);

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

    LinkedList<Integer> getCustomObjectiveCounts();

    LinkedList<String> getCustomObjectiveDisplays();

    LinkedList<Map.Entry<String, Object>> getCustomObjectiveData();

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
