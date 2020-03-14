/*******************************************************************************************************
 * Continued by PikaMug (formerly HappyPikachu) with permission from _Blackvein_. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

package me.blackvein.quests;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import me.blackvein.quests.actions.Action;

public class Stage {

    protected LinkedList<ItemStack> blocksToBreak = new LinkedList<ItemStack>();
    protected LinkedList<ItemStack> blocksToDamage = new LinkedList<ItemStack>();
    protected LinkedList<ItemStack> blocksToPlace = new LinkedList<ItemStack>();
    protected LinkedList<ItemStack> blocksToUse = new LinkedList<ItemStack>();
    protected LinkedList<ItemStack> blocksToCut = new LinkedList<ItemStack>();
    protected Integer cowsToMilk;
    protected Integer fishToCatch;
    protected Integer playersToKill;
    protected LinkedList<ItemStack> itemsToCraft = new LinkedList<ItemStack>();
    protected LinkedList<ItemStack> itemsToSmelt = new LinkedList<ItemStack>();
    protected Map<Map<Enchantment, Material>, Integer> itemsToEnchant 
            = new HashMap<Map<Enchantment, Material>, Integer>();
    protected LinkedList<ItemStack> itemsToBrew = new LinkedList<ItemStack>();
    protected LinkedList<ItemStack> itemsToDeliver = new LinkedList<ItemStack>();
    protected LinkedList<Integer> itemDeliveryTargets = new LinkedList<Integer>() {

        private static final long serialVersionUID = -2774443496142382127L;

        @Override
        public boolean equals(Object o) {
            if (o instanceof LinkedList) {
                @SuppressWarnings("unchecked")
                LinkedList<Integer> otherList = (LinkedList<Integer>) o;
                for (Integer i : this) {
                    Integer other = otherList.get(this.indexOf(i));
                    if (!other.equals(i)) {
                        return false;
                    }
                }
            }
            return true;
        }
    };
    protected  LinkedList<String> deliverMessages = new LinkedList<String>();
    protected LinkedList<Integer> citizensToInteract = new LinkedList<Integer>() {

        private static final long serialVersionUID = -4086855121042524435L;

        @Override
        public boolean equals(Object o) {
            if (o instanceof LinkedList) {
                @SuppressWarnings("unchecked")
                LinkedList<Integer> otherList = (LinkedList<Integer>) o;
                for (Integer i : this) {
                    Integer other = otherList.get(this.indexOf(i));
                    if (!other.equals(i)) {
                        return false;
                    }
                }
            }
            return true;
        }
    };
    protected LinkedList<Integer> citizensToKill = new LinkedList<Integer>() {

        private static final long serialVersionUID = 7705964814014176415L;

        @Override
        public boolean equals(Object o) {
            if (o instanceof LinkedList) {
                @SuppressWarnings("unchecked")
                LinkedList<Integer> otherList = (LinkedList<Integer>) o;
                for (Integer i : this) {
                    Integer other = otherList.get(this.indexOf(i));
                    if (!other.equals(i)) {
                        return false;
                    }
                }
            }
            return true;
        }
    };
    protected LinkedList<Integer> citizenNumToKill = new LinkedList<Integer>();
    protected LinkedList<EntityType> mobsToKill = new LinkedList<EntityType>();
    protected LinkedList<Integer> mobNumToKill = new LinkedList<Integer>();
    protected LinkedList<Location> locationsToKillWithin = new LinkedList<Location>();
    protected LinkedList<Integer> radiiToKillWithin = new LinkedList<Integer>();
    protected LinkedList<String> killNames = new LinkedList<String>();
    protected LinkedList<Location> locationsToReach = new LinkedList<Location>();
    protected LinkedList<Integer> radiiToReachWithin = new LinkedList<Integer>();
    protected LinkedList<World> worldsToReachWithin = new LinkedList<World>();
    protected LinkedList<String> locationNames = new LinkedList<String>();
    protected Map<EntityType, Integer> mobsToTame = new EnumMap<EntityType, Integer>(EntityType.class);
    protected Map<DyeColor, Integer> sheepToShear = new EnumMap<DyeColor, Integer>(DyeColor.class);
    protected LinkedList<String> passwordDisplays = new LinkedList<String>();
    protected LinkedList<LinkedList<String>> passwordPhrases = new LinkedList<LinkedList<String>>();
    protected String script;
    protected Action startAction = null;
    protected Action deathAction = null;
    protected Map<String, Action> chatActions = new HashMap<String, Action>();
    protected Map<String, Action> commandActions = new HashMap<String, Action>();
    protected Action disconnectAction = null;
    protected Action finishAction = null;
    protected long delay = -1;
    protected String delayMessage = null;
    protected String completeMessage = null;
    protected String startMessage = null;
    protected LinkedList<String> objectiveOverrides = new LinkedList<String>();
    protected LinkedList<CustomObjective> customObjectives = new LinkedList<CustomObjective>();
    protected LinkedList<Integer> customObjectiveCounts = new LinkedList<Integer>();
    protected LinkedList<String> customObjectiveDisplays = new LinkedList<String>();
    protected LinkedList<Entry<String, Object>> customObjectiveData = new LinkedList<Entry<String, Object>>();
    
    public LinkedList<ItemStack> getBlocksToBreak() {
        return blocksToBreak;
    }

    public void setBlocksToBreak(LinkedList<ItemStack> blocksToBreak) {
        this.blocksToBreak = blocksToBreak;
    }

    public LinkedList<ItemStack> getBlocksToDamage() {
        return blocksToDamage;
    }

    public void setBlocksToDamage(LinkedList<ItemStack> blocksToDamage) {
        this.blocksToDamage = blocksToDamage;
    }

    public LinkedList<ItemStack> getBlocksToPlace() {
        return blocksToPlace;
    }

    public void setBlocksToPlace(LinkedList<ItemStack> blocksToPlace) {
        this.blocksToPlace = blocksToPlace;
    }

    public LinkedList<ItemStack> getBlocksToUse() {
        return blocksToUse;
    }

    public void setBlocksToUse(LinkedList<ItemStack> blocksToUse) {
        this.blocksToUse = blocksToUse;
    }

    public LinkedList<ItemStack> getBlocksToCut() {
        return blocksToCut;
    }

    public void setBlocksToCut(LinkedList<ItemStack> blocksToCut) {
        this.blocksToCut = blocksToCut;
    }
    
    public Integer getCowsToMilk() {
        return cowsToMilk;
    }

    public void setCowsToMilk(Integer cowsToMilk) {
        this.cowsToMilk = cowsToMilk;
    }

    public Integer getFishToCatch() {
        return fishToCatch;
    }

    public void setFishToCatch(Integer fishToCatch) {
        this.fishToCatch = fishToCatch;
    }

    public Integer getPlayersToKill() {
        return playersToKill;
    }

    public void setPlayersToKill(Integer playersToKill) {
        this.playersToKill = playersToKill;
    }
    
    public LinkedList<ItemStack> getItemsToCraft() {
        return itemsToCraft;
    }

    public void setItemsToCraft(LinkedList<ItemStack> itemsToCraft) {
        this.itemsToCraft = itemsToCraft;
    }
    
    public LinkedList<ItemStack> getItemsToSmelt() {
        return itemsToSmelt;
    }

    public void setItemsToSmelt(LinkedList<ItemStack> itemsToSmelt) {
        this.itemsToSmelt = itemsToSmelt;
    }

    public Map<Map<Enchantment, Material>, Integer> getItemsToEnchant() {
        return itemsToEnchant;
    }

    public void setItemsToEnchant(
            Map<Map<Enchantment, Material>, Integer> itemsToEnchant) {
        this.itemsToEnchant = itemsToEnchant;
    }
    
    public LinkedList<ItemStack> getItemsToBrew() {
        return itemsToBrew;
    }

    public void setItemsToBrew(LinkedList<ItemStack> itemsToBrew) {
        this.itemsToBrew = itemsToBrew;
    }

    public LinkedList<ItemStack> getItemsToDeliver() {
        return itemsToDeliver;
    }

    public void setItemsToDeliver(LinkedList<ItemStack> itemsToDeliver) {
        this.itemsToDeliver = itemsToDeliver;
    }

    public LinkedList<Integer> getItemDeliveryTargets() {
        return itemDeliveryTargets;
    }

    public void setItemDeliveryTargets(LinkedList<Integer> itemDeliveryTargets) {
        this.itemDeliveryTargets = itemDeliveryTargets;
    }

    public LinkedList<String> getDeliverMessages() {
        return deliverMessages;
    }

    public void setDeliverMessages(LinkedList<String> deliverMessages) {
        this.deliverMessages = deliverMessages;
    }

    public LinkedList<Integer> getCitizensToInteract() {
        return citizensToInteract;
    }

    public void setCitizensToInteract(LinkedList<Integer> citizensToInteract) {
        this.citizensToInteract = citizensToInteract;
    }

    public LinkedList<Integer> getCitizensToKill() {
        return citizensToKill;
    }

    public void setCitizensToKill(LinkedList<Integer> citizensToKill) {
        this.citizensToKill = citizensToKill;
    }

    public LinkedList<Integer> getCitizenNumToKill() {
        return citizenNumToKill;
    }

    public void setCitizenNumToKill(LinkedList<Integer> citizenNumToKill) {
        this.citizenNumToKill = citizenNumToKill;
    }

    public LinkedList<EntityType> getMobsToKill() {
        return mobsToKill;
    }

    public void setMobsToKill(LinkedList<EntityType> mobsToKill) {
        this.mobsToKill = mobsToKill;
    }

    public LinkedList<Integer> getMobNumToKill() {
        return mobNumToKill;
    }

    public void setMobNumToKill(LinkedList<Integer> mobNumToKill) {
        this.mobNumToKill = mobNumToKill;
    }

    public LinkedList<Location> getLocationsToKillWithin() {
        return locationsToKillWithin;
    }

    public void setLocationsToKillWithin(LinkedList<Location> locationsToKillWithin) {
        this.locationsToKillWithin = locationsToKillWithin;
    }

    public LinkedList<Integer> getRadiiToKillWithin() {
        return radiiToKillWithin;
    }

    public void setRadiiToKillWithin(LinkedList<Integer> radiiToKillWithin) {
        this.radiiToKillWithin = radiiToKillWithin;
    }
    
    public LinkedList<String> getKillNames() {
        return killNames;
    }

    public void setKillNames(LinkedList<String> killNames) {
        this.killNames = killNames;
    }

    public LinkedList<Location> getLocationsToReach() {
        return locationsToReach;
    }

    public void setLocationsToReach(LinkedList<Location> locationsToReach) {
        this.locationsToReach = locationsToReach;
    }

    public LinkedList<Integer> getRadiiToReachWithin() {
        return radiiToReachWithin;
    }

    public void setRadiiToReachWithin(LinkedList<Integer> radiiToReachWithin) {
        this.radiiToReachWithin = radiiToReachWithin;
    }

    public LinkedList<World> getWorldsToReachWithin() {
        return worldsToReachWithin;
    }

    public void setWorldsToReachWithin(LinkedList<World> worldsToReachWithin) {
        this.worldsToReachWithin = worldsToReachWithin;
    }

    public LinkedList<String> getLocationNames() {
        return locationNames;
    }

    public void setLocationNames(LinkedList<String> locationNames) {
        this.locationNames = locationNames;
    }

    public Map<EntityType, Integer> getMobsToTame() {
        return mobsToTame;
    }

    public void setMobsToTame(Map<EntityType, Integer> mobsToTame) {
        this.mobsToTame = mobsToTame;
    }

    public Map<DyeColor, Integer> getSheepToShear() {
        return sheepToShear;
    }

    public void setSheepToShear(Map<DyeColor, Integer> sheepToShear) {
        this.sheepToShear = sheepToShear;
    }

    public LinkedList<String> getPasswordDisplays() {
        return passwordDisplays;
    }

    public void setPasswordDisplays(LinkedList<String> passwordDisplays) {
        this.passwordDisplays = passwordDisplays;
    }

    public LinkedList<LinkedList<String>> getPasswordPhrases() {
        return passwordPhrases;
    }

    public void setPasswordPhrases(LinkedList<LinkedList<String>> passwordPhrases) {
        this.passwordPhrases = passwordPhrases;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public Action getStartAction() {
        return startAction;
    }

    public void setStartAction(Action startAction) {
        this.startAction = startAction;
    }

    public Action getDeathAction() {
        return deathAction;
    }

    public void setDeathAction(Action deathAction) {
        this.deathAction = deathAction;
    }

    public Map<String, Action> getChatActions() {
        return chatActions;
    }

    public void setChatActions(Map<String, Action> chatActions) {
        this.chatActions = chatActions;
    }

    public Map<String, Action> getCommandActions() {
        return commandActions;
    }

    public void setCommandActions(Map<String, Action> commandActions) {
        this.commandActions = commandActions;
    }

    public Action getDisconnectAction() {
        return disconnectAction;
    }

    public void setDisconnectAction(Action disconnectAction) {
        this.disconnectAction = disconnectAction;
    }

    public Action getFinishAction() {
        return finishAction;
    }

    public void setFinishAction(Action finishAction) {
        this.finishAction = finishAction;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public String getDelayMessage() {
        return delayMessage;
    }

    public void setDelayMessage(String delayMessage) {
        this.delayMessage = delayMessage;
    }

    public String getCompleteMessage() {
        return completeMessage;
    }

    public void setCompleteMessage(String completeMessage) {
        this.completeMessage = completeMessage;
    }

    public String getStartMessage() {
        return startMessage;
    }

    public void setStartMessage(String startMessage) {
        this.startMessage = startMessage;
    }

    public LinkedList<String> getObjectiveOverrides() {
        return objectiveOverrides;
    }

    public void setObjectiveOverrides(LinkedList<String> objectiveOverrides) {
        this.objectiveOverrides = objectiveOverrides;
    }
    
    public LinkedList<CustomObjective> getCustomObjectives() {
        return customObjectives;
    }

    public LinkedList<Integer> getCustomObjectiveCounts() {
        return customObjectiveCounts;
    }

    public LinkedList<String> getCustomObjectiveDisplays() {
        return customObjectiveDisplays;
    }

    public LinkedList<Entry<String, Object>> getCustomObjectiveData() {
        return customObjectiveData;
    }

    /**
     * Check if stage has at least one objective<p>
     * 
     * Excludes start/complete message, delay, and objective-override
     * 
     * @return true if stage contains an objective
     */
    public boolean hasObjective() {
        if (blocksToBreak.isEmpty() == false) { return true; }
        if (blocksToDamage.isEmpty() == false) { return true; }
        if (blocksToPlace.isEmpty() == false) { return true; }
        if (blocksToUse.isEmpty() == false) { return true; }
        if (blocksToCut.isEmpty() == false) { return true; }
        if (cowsToMilk != null) { return true; }
        if (fishToCatch != null) { return true; }
        if (playersToKill != null) { return true; }
        if (itemsToCraft.isEmpty() == false) { return true; }
        if (itemsToSmelt.isEmpty() == false) { return true; }
        if (itemsToEnchant.isEmpty() == false) { return true; }
        if (itemsToBrew.isEmpty() == false) { return true; }
        if (itemsToDeliver.isEmpty() == false) { return true; }
        if (citizensToInteract.isEmpty() == false) { return true; }
        if (citizensToKill.isEmpty() == false) { return true; }
        if (locationsToReach.isEmpty() == false) { return true; }
        if (mobsToTame.isEmpty() == false) { return true; }
        if (sheepToShear.isEmpty() == false) { return true; }
        if (passwordDisplays.isEmpty() == false) { return true; }
        if (customObjectives.isEmpty() == false) { return true; }
        return false;
    }
    
    /**
     * Check if stage has the specified objective<p>
     * 
     * Accepted strings are: breakBlock, damageBlock, placeBlock, useBlock,
     * cutBlock, craftItem, smeltItem, enchantItem, brewItem, milkCow, catchFish,
     * killMob, deliverItem, killPlayer, talkToNPC, killNPC, tameMob,
     * shearSheep, password, reachLocation
     * 
     * @param type The type of objective to check for
     * @return true if stage contains specified objective
     */
    public boolean containsObjective(String type) {
        if (type.equalsIgnoreCase("breakBlock")) {
            return !blocksToBreak.isEmpty();
        } else if (type.equalsIgnoreCase("damageBlock")) {
            return !blocksToDamage.isEmpty();
        } else if (type.equalsIgnoreCase("placeBlock")) {
            return !blocksToPlace.isEmpty();
        } else if (type.equalsIgnoreCase("useBlock")) {
            return !blocksToUse.isEmpty();
        } else if (type.equalsIgnoreCase("cutBlock")) {
            return !blocksToCut.isEmpty();
        } else if (type.equalsIgnoreCase("craftItem")) {
            return !itemsToCraft.isEmpty();
        } else if (type.equalsIgnoreCase("smeltItem")) {
            return !itemsToSmelt.isEmpty();
        } else if (type.equalsIgnoreCase("enchantItem")) {
            return !itemsToEnchant.isEmpty();
        } else if (type.equalsIgnoreCase("brewItem")) {
            return !itemsToBrew.isEmpty();
        } else if (type.equalsIgnoreCase("milkCow")) {
            return cowsToMilk != null;
        } else if (type.equalsIgnoreCase("catchFish")) {
            return fishToCatch != null;
        } else if (type.equalsIgnoreCase("killMob")) {
            return !mobsToKill.isEmpty();
        } else if (type.equalsIgnoreCase("deliverItem")) {
            return !itemsToDeliver.isEmpty();
        } else if (type.equalsIgnoreCase("killPlayer")) {
            return playersToKill != null;
        } else if (type.equalsIgnoreCase("talkToNPC")) {
            return !citizensToInteract.isEmpty();
        } else if (type.equalsIgnoreCase("killNPC")) {
            return !citizensToKill.isEmpty();
        } else if (type.equalsIgnoreCase("tameMob")) {
            return !mobsToTame.isEmpty();
        } else if (type.equalsIgnoreCase("shearSheep")) {
            return !sheepToShear.isEmpty();
        } else if (type.equalsIgnoreCase("password")) {
            return !passwordPhrases.isEmpty();
        } else if (type.equalsIgnoreCase("reachLocation")) {
            return !locationsToReach.isEmpty();
        } else {
            return false;
        }
    }
}
