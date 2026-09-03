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

import java.util.*;

public class FabricStage implements Stage {

    private LinkedList<Object> blocksToBreak = new LinkedList<>();
    private LinkedList<Object> blocksToDamage = new LinkedList<>();
    private LinkedList<Object> blocksToPlace = new LinkedList<>();
    private LinkedList<Object> blocksToUse = new LinkedList<>();
    private LinkedList<Object> blocksToCut = new LinkedList<>();
    private LinkedList<Integer> blocksToBreakAmounts = new LinkedList<>();
    private LinkedList<Integer> blocksToPlaceAmounts = new LinkedList<>();
    private LinkedList<Integer> blocksToUseAmounts = new LinkedList<>();
    private LinkedList<Integer> blocksToCutAmounts = new LinkedList<>();
    private LinkedList<Object> itemsToCraft = new LinkedList<>();
    private LinkedList<Object> itemsToSmelt = new LinkedList<>();
    private LinkedList<Object> itemsToEnchant = new LinkedList<>();
    private LinkedList<Object> itemsToBrew = new LinkedList<>();
    private LinkedList<Object> itemsToConsume = new LinkedList<>();
    private LinkedList<Object> itemsToDeliver = new LinkedList<>();
    private LinkedList<UUID> itemDeliveryTargets = new LinkedList<>();
    private LinkedList<String> deliverMessages = new LinkedList<>();
    private LinkedList<UUID> npcsToInteract = new LinkedList<>();
    private LinkedList<UUID> npcsToKill = new LinkedList<>();
    private LinkedList<Integer> npcNumToKill = new LinkedList<>();
    private LinkedList<Object> mobsToKill = new LinkedList<>();
    private LinkedList<Integer> mobNumToKill = new LinkedList<>();
    private LinkedList<Object> locationsToKillWithin = new LinkedList<>();
    private LinkedList<Integer> radiiToKillWithin = new LinkedList<>();
    private LinkedList<String> killNames = new LinkedList<>();
    private LinkedList<Object> locationsToReach = new LinkedList<>();
    private LinkedList<Integer> radiiToReachWithin = new LinkedList<>();
    private LinkedList<Object> worldsToReachWithin = new LinkedList<>();
    private LinkedList<String> locationNames = new LinkedList<>();
    private LinkedList<Object> mobsToTame = new LinkedList<>();
    private LinkedList<Integer> mobNumToTame = new LinkedList<>();
    private Integer fishToCatch = 0;
    private Integer cowsToMilk = 0;
    private Integer playersToKill = 0;
    private LinkedList<Object> sheepToShear = new LinkedList<>();
    private LinkedList<Integer> sheepNumToShear = new LinkedList<>();
    private LinkedList<String> passwordDisplays = new LinkedList<>();
    private LinkedList<String> passwordPhrases = new LinkedList<>();
    private String script;
    private Action startAction;
    private Action finishAction;
    private Action failAction;
    private Action deathAction;
    private Map<String, Action> chatActions = new LinkedHashMap<>();
    private Map<String, Action> commandActions = new LinkedHashMap<>();
    private Action disconnectAction;
    private Condition condition;
    private long delay = 0;
    private String delayMessage;
    private String completeMessage;
    private String startMessage;
    private LinkedList<String> objectiveOverrides = new LinkedList<>();
    private LinkedList<CustomObjective> customObjectives = new LinkedList<>();
    private LinkedList<Integer> customObjectiveCounts = new LinkedList<>();
    private LinkedList<String> customObjectiveDisplays = new LinkedList<>();
    private LinkedList<Map.Entry<String, Object>> customObjectiveData = new LinkedList<>();

    @Override public LinkedList<?> getBlocksToBreak() { return blocksToBreak; }
    @Override public LinkedList<?> getBlocksToDamage() { return blocksToDamage; }
    @Override public LinkedList<?> getBlocksToPlace() { return blocksToPlace; }
    @Override public LinkedList<?> getBlocksToUse() { return blocksToUse; }
    @Override public LinkedList<?> getBlocksToCut() { return blocksToCut; }
    public LinkedList<Integer> getBlocksToBreakAmounts() { return blocksToBreakAmounts; }
    public void setBlocksToBreakAmounts(LinkedList<Integer> v) { this.blocksToBreakAmounts = v; }
    public LinkedList<Integer> getBlocksToPlaceAmounts() { return blocksToPlaceAmounts; }
    public void setBlocksToPlaceAmounts(LinkedList<Integer> v) { this.blocksToPlaceAmounts = v; }
    public LinkedList<Integer> getBlocksToUseAmounts() { return blocksToUseAmounts; }
    public void setBlocksToUseAmounts(LinkedList<Integer> v) { this.blocksToUseAmounts = v; }
    public LinkedList<Integer> getBlocksToCutAmounts() { return blocksToCutAmounts; }
    public void setBlocksToCutAmounts(LinkedList<Integer> v) { this.blocksToCutAmounts = v; }
    @Override public LinkedList<?> getItemsToCraft() { return itemsToCraft; }
    @Override public LinkedList<?> getItemsToSmelt() { return itemsToSmelt; }
    @Override public LinkedList<?> getItemsToEnchant() { return itemsToEnchant; }
    @Override public LinkedList<?> getItemsToBrew() { return itemsToBrew; }
    @Override public LinkedList<?> getItemsToConsume() { return itemsToConsume; }
    @Override public LinkedList<?> getItemsToDeliver() { return itemsToDeliver; }
    @Override public LinkedList<UUID> getItemDeliveryTargets() { return itemDeliveryTargets; }
    @Override public void setItemDeliveryTargets(LinkedList<UUID> v) { this.itemDeliveryTargets = v; }
    @Override public LinkedList<String> getDeliverMessages() { return deliverMessages; }
    @Override public void setDeliverMessages(LinkedList<String> v) { this.deliverMessages = v; }
    @Override public LinkedList<UUID> getNpcsToInteract() { return npcsToInteract; }
    @Override public void setNpcsToInteract(LinkedList<UUID> v) { this.npcsToInteract = v; }
    @Override public LinkedList<UUID> getNpcsToKill() { return npcsToKill; }
    @Override public void setNpcsToKill(LinkedList<UUID> v) { this.npcsToKill = v; }
    @Override public LinkedList<Integer> getNpcNumToKill() { return npcNumToKill; }
    @Override public void setNpcNumToKill(LinkedList<Integer> v) { this.npcNumToKill = v; }
    @Override public LinkedList<?> getMobsToKill() { return mobsToKill; }
    @Override public LinkedList<Integer> getMobNumToKill() { return mobNumToKill; }
    @Override public void setMobNumToKill(LinkedList<Integer> v) { this.mobNumToKill = v; }
    @Override public LinkedList<?> getLocationsToKillWithin() { return locationsToKillWithin; }
    @Override public LinkedList<Integer> getRadiiToKillWithin() { return radiiToKillWithin; }
    @Override public void setRadiiToKillWithin(LinkedList<Integer> v) { this.radiiToKillWithin = v; }
    @Override public LinkedList<String> getKillNames() { return killNames; }
    @Override public void setKillNames(LinkedList<String> v) { this.killNames = v; }
    @Override public LinkedList<?> getLocationsToReach() { return locationsToReach; }
    @Override public LinkedList<Integer> getRadiiToReachWithin() { return radiiToReachWithin; }
    @Override public void setRadiiToReachWithin(LinkedList<Integer> v) { this.radiiToReachWithin = v; }
    @Override public LinkedList<?> getWorldsToReachWithin() { return worldsToReachWithin; }
    @Override public LinkedList<String> getLocationNames() { return locationNames; }
    @Override public void setLocationNames(LinkedList<String> v) { this.locationNames = v; }
    @Override public LinkedList<?> getMobsToTame() { return mobsToTame; }
    @Override public LinkedList<Integer> getMobNumToTame() { return mobNumToTame; }
    @Override public void setMobNumToTame(LinkedList<Integer> v) { this.mobNumToTame = v; }
    @Override public Integer getFishToCatch() { return fishToCatch; }
    @Override public void setFishToCatch(Integer v) { this.fishToCatch = v; }
    @Override public Integer getCowsToMilk() { return cowsToMilk; }
    @Override public void setCowsToMilk(Integer v) { this.cowsToMilk = v; }
    @Override public Integer getPlayersToKill() { return playersToKill; }
    @Override public void setPlayersToKill(Integer v) { this.playersToKill = v; }
    @Override public LinkedList<?> getSheepToShear() { return sheepToShear; }
    @Override public LinkedList<Integer> getSheepNumToShear() { return sheepNumToShear; }
    @Override public void setSheepNumToShear(LinkedList<Integer> v) { this.sheepNumToShear = v; }
    @Override public LinkedList<String> getPasswordDisplays() { return passwordDisplays; }
    @Override public void setPasswordDisplays(LinkedList<String> v) { this.passwordDisplays = v; }
    @Override public LinkedList<String> getPasswordPhrases() { return passwordPhrases; }
    @Override public void setPasswordPhrases(LinkedList<String> v) { this.passwordPhrases = v; }
    @Override public String getScript() { return script; }
    @Override public void setScript(String v) { this.script = v; }
    @Override public Action getStartAction() { return startAction; }
    @Override public void setStartAction(Action v) { this.startAction = v; }
    @Override public Action getFinishAction() { return finishAction; }
    @Override public void setFinishAction(Action v) { this.finishAction = v; }
    @Override public Action getFailAction() { return failAction; }
    @Override public void setFailAction(Action v) { this.failAction = v; }
    @Override public Action getDeathAction() { return deathAction; }
    @Override public void setDeathAction(Action v) { this.deathAction = v; }
    @Override public Map<String, Action> getChatActions() { return chatActions; }
    @Override public void setChatActions(Map<String, Action> v) { this.chatActions = v; }
    @Override public Map<String, Action> getCommandActions() { return commandActions; }
    @Override public void setCommandActions(Map<String, Action> v) { this.commandActions = v; }
    @Override public Action getDisconnectAction() { return disconnectAction; }
    @Override public void setDisconnectAction(Action v) { this.disconnectAction = v; }
    @Override public Condition getCondition() { return condition; }
    @Override public void setCondition(Condition v) { this.condition = v; }
    @Override public long getDelay() { return delay; }
    @Override public void setDelay(long v) { this.delay = v; }
    @Override public String getDelayMessage() { return delayMessage; }
    @Override public void setDelayMessage(String v) { this.delayMessage = v; }
    @Override public String getCompleteMessage() { return completeMessage; }
    @Override public void setCompleteMessage(String v) { this.completeMessage = v; }
    @Override public String getStartMessage() { return startMessage; }
    @Override public void setStartMessage(String v) { this.startMessage = v; }
    @Override public LinkedList<String> getObjectiveOverrides() { return objectiveOverrides; }
    @Override public void setObjectiveOverrides(LinkedList<String> v) { this.objectiveOverrides = v; }
    @Override public LinkedList<CustomObjective> getCustomObjectives() { return customObjectives; }
    @Override public void clearCustomObjectives() { customObjectives.clear(); }
    @Override public LinkedList<Integer> getCustomObjectiveCounts() { return customObjectiveCounts; }
    @Override public void clearCustomObjectiveCounts() { customObjectiveCounts.clear(); }
    @Override public LinkedList<String> getCustomObjectiveDisplays() { return customObjectiveDisplays; }
    @Override public void clearCustomObjectiveDisplays() { customObjectiveDisplays.clear(); }
    @Override public LinkedList<Map.Entry<String, Object>> getCustomObjectiveData() { return customObjectiveData; }
    @Override public void clearCustomObjectiveData() { customObjectiveData.clear(); }

    public void addCustomObjectives(final CustomObjective v) { customObjectives.add(v); }
    public void addCustomObjectiveCounts(final int v) { customObjectiveCounts.add(v); }
    public void addCustomObjectiveData(final Map.Entry<String, Object> v) { customObjectiveData.add(v); }

    @Override
    public boolean hasObjective() {
        return !blocksToBreak.isEmpty() || !blocksToDamage.isEmpty() || !blocksToPlace.isEmpty()
                || !blocksToUse.isEmpty() || !blocksToCut.isEmpty() || !itemsToCraft.isEmpty()
                || !itemsToSmelt.isEmpty() || !itemsToEnchant.isEmpty() || !itemsToBrew.isEmpty()
                || !itemsToConsume.isEmpty() || !itemsToDeliver.isEmpty() || !npcsToInteract.isEmpty()
                || !npcsToKill.isEmpty() || !mobsToKill.isEmpty() || !locationsToReach.isEmpty()
                || !mobsToTame.isEmpty() || fishToCatch > 0 || cowsToMilk > 0
                || playersToKill > 0 || !sheepToShear.isEmpty() || !passwordPhrases.isEmpty()
                || !customObjectives.isEmpty();
    }

    @Override
    public boolean hasLocatableObjective() {
        return !locationsToReach.isEmpty();
    }

    @Override
    public boolean containsObjective(ObjectiveType type) {
        if (type == null) return false;
        switch (type) {
            case BREAK_BLOCK: return !blocksToBreak.isEmpty();
            case DAMAGE_BLOCK: return !blocksToDamage.isEmpty();
            case PLACE_BLOCK: return !blocksToPlace.isEmpty();
            case USE_BLOCK: return !blocksToUse.isEmpty();
            case CUT_BLOCK: return !blocksToCut.isEmpty();
            case CRAFT_ITEM: return !itemsToCraft.isEmpty();
            case SMELT_ITEM: return !itemsToSmelt.isEmpty();
            case ENCHANT_ITEM: return !itemsToEnchant.isEmpty();
            case BREW_ITEM: return !itemsToBrew.isEmpty();
            case CONSUME_ITEM: return !itemsToConsume.isEmpty();
            case DELIVER_ITEM: return !itemsToDeliver.isEmpty();
            case MILK_COW: return cowsToMilk > 0;
            case CATCH_FISH: return fishToCatch > 0;
            case KILL_MOB: return !mobsToKill.isEmpty();
            case KILL_PLAYER: return playersToKill > 0;
            case TALK_TO_NPC: return !npcsToInteract.isEmpty();
            case KILL_NPC: return !npcsToKill.isEmpty();
            case TAME_MOB: return !mobsToTame.isEmpty();
            case SHEAR_SHEEP: return !sheepToShear.isEmpty();
            case REACH_LOCATION: return !locationsToReach.isEmpty();
            case PASSWORD: return !passwordPhrases.isEmpty();
            case CUSTOM: return !customObjectives.isEmpty();
            default: return false;
        }
    }

    public void setBlocksToBreak(LinkedList<Object> v) { this.blocksToBreak = v; }
    public void setBlocksToDamage(LinkedList<Object> v) { this.blocksToDamage = v; }
    public void setBlocksToPlace(LinkedList<Object> v) { this.blocksToPlace = v; }
    public void setBlocksToUse(LinkedList<Object> v) { this.blocksToUse = v; }
    public void setBlocksToCut(LinkedList<Object> v) { this.blocksToCut = v; }
    public void setItemsToCraft(LinkedList<Object> v) { this.itemsToCraft = v; }
    public void setItemsToSmelt(LinkedList<Object> v) { this.itemsToSmelt = v; }
    public void setItemsToEnchant(LinkedList<Object> v) { this.itemsToEnchant = v; }
    public void setItemsToBrew(LinkedList<Object> v) { this.itemsToBrew = v; }
    public void setItemsToConsume(LinkedList<Object> v) { this.itemsToConsume = v; }
    public void setItemsToDeliver(LinkedList<Object> v) { this.itemsToDeliver = v; }
    public void setMobsToKill(LinkedList<Object> v) { this.mobsToKill = v; }
    public void setLocationsToKillWithin(LinkedList<Object> v) { this.locationsToKillWithin = v; }
    public void setLocationsToReach(LinkedList<Object> v) { this.locationsToReach = v; }
    public void setWorldsToReachWithin(LinkedList<Object> v) { this.worldsToReachWithin = v; }
    public void setMobsToTame(LinkedList<Object> v) { this.mobsToTame = v; }
    public void setSheepToShear(LinkedList<Object> v) { this.sheepToShear = v; }
}
