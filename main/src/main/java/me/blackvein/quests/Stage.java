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
    protected Action startEvent = null;
    protected Action deathEvent = null;
    protected Map<String, Action> chatEvents = new HashMap<String, Action>();
    protected Map<String, Action> commandEvents = new HashMap<String, Action>();
    protected Action disconnectEvent = null;
    protected Action finishEvent = null;
    protected long delay = -1;
    protected String delayMessage = null;
    protected String completeMessage = null;
    protected String startMessage = null;
    protected String objectiveOverride = null;
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

    /**
     * @deprecated use getKillNames()
     */
    public LinkedList<String> getAreaNames() {
        return killNames;
    }

    /**
     * @deprecated use setKillNames()
     */
    public void setAreaNames(LinkedList<String> killNames) {
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

    public Action getStartEvent() {
        return startEvent;
    }

    public void setStartEvent(Action startEvent) {
        this.startEvent = startEvent;
    }

    public Action getDeathEvent() {
        return deathEvent;
    }

    public void setDeathEvent(Action deathEvent) {
        this.deathEvent = deathEvent;
    }

    public Map<String, Action> getChatEvents() {
        return chatEvents;
    }

    public void setChatEvents(Map<String, Action> chatEvents) {
        this.chatEvents = chatEvents;
    }

    public Map<String, Action> getCommandEvents() {
        return commandEvents;
    }

    public void setCommandEvents(Map<String, Action> commandEvents) {
        this.commandEvents = commandEvents;
    }

    public Action getDisconnectEvent() {
        return disconnectEvent;
    }

    public void setDisconnectEvent(Action disconnectEvent) {
        this.disconnectEvent = disconnectEvent;
    }

    public Action getFinishEvent() {
        return finishEvent;
    }

    public void setFinishEvent(Action finishEvent) {
        this.finishEvent = finishEvent;
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

    public String getObjectiveOverride() {
        return objectiveOverride;
    }

    public void setObjectiveOverride(String objectiveOverride) {
        this.objectiveOverride = objectiveOverride;
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
}
