package me.blackvein.quests;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class QuestData {

    public Map<Material, Integer> blocksDamaged = new EnumMap<Material, Integer>(Material.class);
    public Map<Material, Integer> blocksBroken = new EnumMap<Material, Integer>(Material.class);
    public Map<Material, Integer> blocksPlaced = new EnumMap<Material, Integer>(Material.class);
    public Map<Material, Integer> blocksUsed = new EnumMap<Material, Integer>(Material.class);
    public Map<Material, Integer> blocksCut = new EnumMap<Material, Integer>(Material.class);
    public Map<Integer, Integer> potionsBrewed = new HashMap<Integer, Integer>();
    public Map<ItemStack, Integer> itemsDelivered = new HashMap<ItemStack, Integer>();
    public int fishCaught = 0;
    public int playersKilled = 0;
    public long delayStartTime = 0;
    public long delayTimeLeft = -1;
    public Map<String, Long> playerKillTimes = new HashMap<String, Long>();
    public Map<Map<Enchantment, Material>, Integer> itemsEnchanted = new HashMap<Map<Enchantment, Material>, Integer>();
    public LinkedList<EntityType> mobsKilled = new LinkedList<EntityType>();
    public LinkedList<Integer> mobNumKilled = new LinkedList<Integer>();
    public LinkedList<Location> locationsToKillWithin = new LinkedList<Location>();
    public LinkedList<Integer> radiiToKillWithin = new LinkedList<Integer>();
    public Map<Integer, Boolean> citizensInteracted = new HashMap<Integer, Boolean>();
    public LinkedList<Integer> citizensKilled = new LinkedList<Integer>();
    public LinkedList<Integer> citizenNumKilled = new LinkedList<Integer>();
    public LinkedList<Location> locationsReached = new LinkedList<Location>();
    public LinkedList<Boolean> hasReached = new LinkedList<Boolean>();
    public LinkedList<Integer> radiiToReachWithin = new LinkedList<Integer>();
    public Map<EntityType, Integer> mobsTamed = new EnumMap<EntityType, Integer>(EntityType.class);
    public Map<DyeColor, Integer> sheepSheared = new EnumMap<DyeColor, Integer>(DyeColor.class);
    public Map<String, Boolean> passwordsSaid = new HashMap<String, Boolean>();
    public Map<String, Integer> customObjectiveCounts = new HashMap<String, Integer>();
    public Map<String, Boolean> eventFired = new HashMap<String, Boolean>();
    public boolean delayOver = true;

}
