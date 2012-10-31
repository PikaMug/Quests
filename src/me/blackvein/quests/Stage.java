package me.blackvein.quests;

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

public class Stage {

    public String finished;
    Map<Material, Integer> blocksToDamage = new EnumMap<Material, Integer>(Material.class);
    Map<Material, Integer> blocksToBreak = new EnumMap<Material, Integer>(Material.class);
    Map<Material, Integer> blocksToPlace = new EnumMap<Material, Integer>(Material.class);
    Map<Map<Material, Integer>, Boolean> itemsToCollect = new HashMap<Map<Material, Integer>, Boolean>();
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
    LinkedList<NPC> citizensToInteract = new LinkedList<NPC>(){
    
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
    LinkedList<NPC> citizensToKill = new LinkedList<NPC>() {

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
    LinkedList<Integer> citizenNumToKill = new LinkedList<Integer>();
    LinkedList<Location> locationsToReach = new LinkedList<Location>();
    LinkedList<Integer> radiiToReachWithin = new LinkedList<Integer>();
    LinkedList<World> worldsToReachWithin = new LinkedList<World>();
    LinkedList<String> locationNames = new LinkedList<String>();
    Map<EntityType, Integer> mobsToTame = new EnumMap<EntityType, Integer>(EntityType.class);
    Map<DyeColor, Integer> sheepToShear = new EnumMap<DyeColor, Integer>(DyeColor.class);
    Map<Material, Integer> itemsToCraft = new EnumMap<Material, Integer>(Material.class);
    String script;
    Event event;
    long delay = -1;
    String delayMessage = null;

    @Override
    public boolean equals(Object o) {

        if (o instanceof Stage) {

            Stage other = (Stage) o;

            if (other.finished != null && finished != null) {
                if (other.finished.equals(finished) == false) {
                    return false;
                }
            } else if (other.finished != null && finished == null) {
                return false;
            } else if (other.finished == null && finished != null) {
                return false;
            }

            if (other.blocksToDamage.equals(blocksToDamage) == false) {
                return false;
            }

            if (other.blocksToBreak.equals(blocksToBreak) == false) {
                return false;
            }

            if (other.blocksToPlace.equals(blocksToPlace) == false) {
                return false;
            }

            if (other.itemsToCollect.equals(itemsToCollect) == false) {
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

            if (other.citizensToInteract.equals(citizensToInteract) == false) {
                return false;
            }

            if (other.citizensToKill.equals(citizensToKill) == false) {
                return false;
            }

            if (other.citizenNumToKill.equals(citizenNumToKill) == false) {
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

        }

        return true;

    }
}
