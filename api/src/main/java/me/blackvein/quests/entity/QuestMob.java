package me.blackvein.quests.entity;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public interface QuestMob {
    String getName();

    void setName(final String name);

    EntityType getType();

    void setType(final EntityType entityType);

    Location getSpawnLocation();

    void setSpawnLocation(final Location spawnLocation);

    Integer getSpawnAmounts();

    void setSpawnAmounts(final int spawnAmounts);

    ItemStack[] getInventory();

    void setInventory(final ItemStack[] inventory);

    Float[] getDropChances();

    void setDropChances(final Float[] dropChances);

    void setHelmet(final ItemStack helmet, final float dropChance);

    void setChest(final ItemStack chest, final float dropChance);

    void setLeggings(final ItemStack leggings, final float dropChance);

    void setBoots(final ItemStack boots, final float dropChance);

    void setHeldItem(final ItemStack heldItem, final float dropChance);

    void spawn();

    String serialize();
}
