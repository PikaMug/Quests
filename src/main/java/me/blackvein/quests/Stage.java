package main.java.me.blackvein.quests;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class Stage {

    Map<Material, Integer> blocksToDamage = new EnumMap<Material, Integer>(Material.class);
    Map<Material, Integer> blocksToBreak = new EnumMap<Material, Integer>(Material.class);
    Map<Material, Integer> blocksToPlace = new EnumMap<Material, Integer>(Material.class);
    Map<Material, Integer> blocksToUse = new EnumMap<Material, Integer>(Material.class);
    Map<Material, Integer> blocksToCut = new EnumMap<Material, Integer>(Material.class);
    Integer fishToCatch;
    Integer playersToKill;
    Map<Map<Enchantment, Material>, Integer> itemsToEnchant = new HashMap<Map<Enchantment, Material>, Integer>();
    LinkedList<EntityType> mobsToKill = new LinkedList<EntityType>();
    LinkedList<Integer> mobNumToKill = new LinkedList<Integer>();
    LinkedList<Location> locationsToKillWithin = new LinkedList<Location>();
    LinkedList<Integer> radiiToKillWithin = new LinkedList<Integer>();
    LinkedList<String> areaNames = new LinkedList<String>();

    LinkedList<ItemStack> itemsToDeliver = new LinkedList<ItemStack>();
    LinkedList<NPC> itemDeliveryTargets = new LinkedList<NPC>(){

        @Override
        public boolean equals(Object o) {

            if (o instanceof LinkedList) {

                LinkedList<NPC> otherList = (LinkedList<NPC>) o;

                for (NPC n : this) {

                    NPC other = otherList.get(this.indexOf(n));
                    if (other.getId() != n.getId()) {
                        return false;
                    }
                }

            }

            return true;

        }

    };
    public LinkedList<String> deliverMessages = new LinkedList<String>();

    public LinkedList<NPC> citizensToInteract = new LinkedList<NPC>(){

        @Override
        public boolean equals(Object o) {

            if (o instanceof LinkedList) {

                LinkedList<NPC> otherList = (LinkedList<NPC>) o;

                for (NPC n : this) {

                    NPC other = otherList.get(this.indexOf(n));
                    if (other.getId() != n.getId()) {
                        return false;
                    }
                }

            }

            return true;

        }

    };
    public LinkedList<NPC> citizensToKill = new LinkedList<NPC>() {

        @Override
        public boolean equals(Object o) {

            if (o instanceof LinkedList) {

                LinkedList<NPC> otherList = (LinkedList<NPC>) o;

                for (NPC n : this) {

                    NPC other = otherList.get(this.indexOf(n));
                    if (other.getId() != n.getId()) {
                        return false;
                    }
                }

            }

            return true;

        }

    };
    public LinkedList<Integer> citizenNumToKill = new LinkedList<Integer>();

    public LinkedList<String> bossesToKill = new LinkedList<String>();
    public LinkedList<Integer> bossAmountsToKill = new LinkedList<Integer>();

    public LinkedList<Location> locationsToReach = new LinkedList<Location>();
    public LinkedList<Integer> radiiToReachWithin = new LinkedList<Integer>();
    public LinkedList<World> worldsToReachWithin = new LinkedList<World>();
    public LinkedList<String> locationNames = new LinkedList<String>();
    public Map<EntityType, Integer> mobsToTame = new EnumMap<EntityType, Integer>(EntityType.class);
    public Map<DyeColor, Integer> sheepToShear = new EnumMap<DyeColor, Integer>(DyeColor.class);
    public Map<EnumMap<Material, Integer>, Boolean> itemsToCraft = new HashMap<EnumMap<Material, Integer>, Boolean>();
    public String script;
    public Event event;
    public long delay = -1;
    public String delayMessage = null;
	public String completeMessage = null;
	public String startMessage = null;

    @Override
    public boolean equals(Object o) {

        if (o instanceof Stage) {

            Stage other = (Stage) o;

            if (other.blocksToDamage.equals(blocksToDamage) == false) {
                return false;
            }

            if (other.blocksToBreak.equals(blocksToBreak) == false) {
                return false;
            }

            if (other.blocksToPlace.equals(blocksToPlace) == false) {
                return false;
            }

            if (other.blocksToUse.equals(blocksToUse) == false) {
                return false;
            }

            if (other.blocksToCut.equals(blocksToCut) == false) {
                return false;
            }

            if (other.fishToCatch != null && fishToCatch != null) {
                if (other.fishToCatch.equals(fishToCatch) == false) {
                    return false;
                }
            } else if (other.fishToCatch != null && fishToCatch == null) {
                return false;
            } else if (other.fishToCatch == null && fishToCatch != null) {
                return false;
            }

            if (other.playersToKill != null && playersToKill != null) {
                if (other.playersToKill.equals(playersToKill) == false) {
                    return false;
                }
            } else if (other.playersToKill != null && playersToKill == null) {
                return false;
            } else if (other.playersToKill == null && playersToKill != null) {
                return false;
            }

            if (other.itemsToEnchant.equals(itemsToEnchant) == false) {
                return false;
            }

            if (other.mobsToKill.equals(mobsToKill) == false) {
                return false;
            }

            if (other.mobNumToKill.equals(mobNumToKill) == false) {
                return false;
            }

            if (other.locationsToKillWithin.equals(locationsToKillWithin) == false) {
                return false;
            }

            if (other.radiiToKillWithin.equals(radiiToKillWithin) == false) {
                return false;
            }

            if (other.areaNames.equals(areaNames) == false) {
                return false;
            }

            if (other.itemsToDeliver.equals(itemsToDeliver) == false){
                return false;
            }

            if (other.itemDeliveryTargets.equals(itemDeliveryTargets) == false){
                return false;
            }

            if (other.deliverMessages.equals(deliverMessages) == false){
                return false;
            }

            if (other.citizensToInteract.equals(citizensToInteract) == false) {
                return false;
            }

            if (other.citizensToKill.equals(citizensToKill) == false) {
                return false;
            }

            if (other.citizenNumToKill.equals(citizenNumToKill) == false) {
                return false;
            }

            if(other.bossesToKill.equals(bossesToKill) == false){
                return false;
            }

            if(other.bossAmountsToKill.equals(bossAmountsToKill) == false){
                return false;
            }

            if (other.locationsToReach.equals(locationsToReach) == false) {
                return false;
            }

            if (other.radiiToReachWithin.equals(radiiToReachWithin) == false) {
                return false;
            }

            if (other.worldsToReachWithin.equals(worldsToReachWithin) == false) {
                return false;
            }

            if (other.locationNames.equals(locationNames) == false) {
                return false;
            }

            if (other.mobsToTame.equals(mobsToTame) == false) {
                return false;
            }

            if (other.sheepToShear.equals(sheepToShear) == false) {
                return false;
            }

            if (other.itemsToCraft.equals(itemsToCraft) == false) {
                return false;
            }

            if (other.script != null && script != null) {
                if (other.script.equals(script) == false) {
                    return false;
                }
            } else if (other.script != null && script == null) {
                return false;
            } else if (other.script == null && script != null) {
                return false;
            }

            if (other.event != null && event != null) {
                if (other.event.equals(event) == false) {
                    return false;
                }
            } else if (other.event != null && event == null) {
                return false;
            } else if (other.event == null && event != null) {
                return false;
            }

            if(other.delay != delay)
                return false;

            if (other.delayMessage != null && delayMessage != null) {
                if (other.delayMessage.equals(delayMessage) == false) {
                    return false;
                }
            } else if (other.delayMessage != null && delayMessage == null) {
                return false;
            } else if (other.delayMessage == null && delayMessage != null) {
                return false;
            }

            if (other.startMessage != null && startMessage != null) {
                if (other.startMessage.equals(startMessage) == false) {
                    return false;
                }
            } else if (other.startMessage != null && startMessage == null) {
                return false;
            } else if (other.startMessage == null && startMessage != null) {
                return false;
            }

            if (other.completeMessage != null && completeMessage != null) {
                if (other.completeMessage.equals(completeMessage) == false) {
                    return false;
                }
            } else if (other.completeMessage != null && completeMessage == null) {
                return false;
            } else if (other.completeMessage == null && completeMessage != null) {
                return false;
            }

        }

        return true;

    }
}
