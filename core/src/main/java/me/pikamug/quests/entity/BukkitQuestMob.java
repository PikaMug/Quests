/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.entity;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import me.pikamug.quests.util.BukkitConfigUtil;
import me.pikamug.quests.util.BukkitItemUtil;
import me.pikamug.quests.util.BukkitMiscUtil;

public class BukkitQuestMob implements QuestMob {

    private String name = null;
    private EntityType entityType = null;
    private Location spawnLocation = null;
    private Integer spawnAmounts = null;
    private ItemStack[] inventory = new ItemStack[5];
    private Float[] dropChances = new Float[5];
    
    public BukkitQuestMob(){
    }

    public BukkitQuestMob(final EntityType entityType, final Location spawnLocation, final int spawnAmounts) {
        this.entityType = entityType;
        this.spawnLocation = spawnLocation;
        this.spawnAmounts = spawnAmounts;
    }

    public String getName() {
        return name;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
    public EntityType getType() {
        return entityType;
    }
    
    public void setType(final EntityType entityType) {
        this.entityType = entityType;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }
    
    public void setSpawnLocation(final Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    public Integer getSpawnAmounts() {
        return spawnAmounts;
    }

    public void setSpawnAmounts(final int spawnAmounts) {
        this.spawnAmounts = spawnAmounts;
    }

    public ItemStack[] getInventory() {
        return inventory;
    }

    public void setInventory(final ItemStack[] inventory) {
        this.inventory = inventory;
    }

    public Float[] getDropChances() {
        return dropChances;
    }

    public void setDropChances(final Float[] dropChances) {
        this.dropChances = dropChances;
    }

    public void setHelmet(final ItemStack helmet, final float dropChance) {
        inventory[4] = helmet;
        dropChances[4] = dropChance;
    }

    public void setChest(final ItemStack chest, final float dropChance) {
        inventory[3] = chest;
        dropChances[3] = dropChance;
    }

    public void setLeggings(final ItemStack leggings, final float dropChance) {
        inventory[2] = leggings;
        dropChances[2] = dropChance;
    }

    public void setBoots(final ItemStack boots, final float dropChance) {
        inventory[1] = boots;
        dropChances[1] = dropChance;
    }

    public void setHeldItem(final ItemStack heldItem, final float dropChance) {
        inventory[0] = heldItem;
        dropChances[0] = dropChance;
    }

    @SuppressWarnings("deprecation")
    public void spawn() {
        final World world = spawnLocation.getWorld();
        if (world == null) {
            return;
        }
        for (int i = 0; i < spawnAmounts; i++) {
            final LivingEntity entity = (LivingEntity) world.spawnEntity(spawnLocation, entityType);
            if (name != null) {
                entity.setCustomName(name);
                entity.setCustomNameVisible(true);
            }
            final EntityEquipment eq = entity.getEquipment();
            if (eq == null) {
                return;
            }
            eq.setItemInHand(inventory[0]);
            eq.setBoots(inventory[1]);
            eq.setLeggings(inventory[2]);
            eq.setChestplate(inventory[3]);
            eq.setHelmet(inventory[4]);
            if (dropChances[0] != null) {
                eq.setItemInHandDropChance(dropChances[0]);
            }
            if (dropChances[1] != null) {
                eq.setBootsDropChance(dropChances[1]);
            }
            if (dropChances[2] != null) {
                eq.setLeggingsDropChance(dropChances[2]);
            }
            if (dropChances[3] != null) {
                eq.setChestplateDropChance(dropChances[3]);
            }
            if (dropChances[4] != null) {
                eq.setHelmetDropChance(dropChances[4]);
            }
        }
    }

    public static BukkitQuestMob fromString(final String str) {
        String name = null;
        EntityType entityType = null;
        Location loc = null;
        Integer amounts = null;
        final ItemStack[] inventory = new ItemStack[5];
        final Float[] dropChances = new Float[5];
        final String[] args = str.split("::");
        for (final String string : args) {
            if (string.startsWith("type-")) {
                entityType = BukkitMiscUtil.getProperMobType(string.substring(5));
            } else if (string.startsWith("name-")) {
                name = string.substring(5);
            } else if (string.startsWith("spawn-")) {
                loc = BukkitConfigUtil.getLocation(string.substring(6));
            } else if (string.startsWith("amounts-")) {
                amounts = Integer.parseInt(string.substring(8));
            } else if (string.startsWith("hand-")) {
                inventory[0] = BukkitItemUtil.readItemStack(string.substring(5));
            } else if (string.startsWith("hand_drop-")) {
                try {
                    dropChances[0] = Float.parseFloat(string.substring(10));
                } catch (final NumberFormatException e) {
                    Bukkit.getLogger().info("Drop chance for hand was required, but left empty."
                            + " Setting to 1.0");
                    dropChances[0] = 1.0f;
                }
            } else if (string.startsWith("boots-")) {
                inventory[1] = BukkitItemUtil.readItemStack(string.substring(6));
            } else if (string.startsWith("boots_drop-")) {
                try {
                    dropChances[1] = Float.parseFloat(string.substring(11));
                } catch (final NumberFormatException e) {
                    Bukkit.getLogger().info("Drop chance for boots was required, but left empty."
                            + " Setting to 1.0");
                    dropChances[1] = 1.0f;
                }
            } else if (string.startsWith("leggings-")) {
                inventory[2] = BukkitItemUtil.readItemStack(string.substring(9));
            } else if (string.startsWith("leggings_drop-")) {
                try {
                    dropChances[2] = Float.parseFloat(string.substring(14));
                } catch (final NumberFormatException e) {
                    Bukkit.getLogger().info("Drop chance for leggings was required, but left empty."
                            + " Setting to 1.0");
                    dropChances[2] = 1.0f;
                }
            } else if (string.startsWith("chest-")) {
                inventory[3] = BukkitItemUtil.readItemStack(string.substring(6));
            } else if (string.startsWith("chest_drop-")) {
                try {
                    dropChances[3] = Float.parseFloat(string.substring(11));
                } catch (final NumberFormatException e) {
                    Bukkit.getLogger().info("Drop chance for chest was required, but left empty."
                            + " Setting to 1.0");
                    dropChances[3] = 1.0f;
                }
            } else if (string.startsWith("helmet-")) {
                inventory[4] = BukkitItemUtil.readItemStack(string.substring(7));
            } else if (string.startsWith("helmet_drop-")) {
                try {
                    dropChances[4] = Float.parseFloat(string.substring(12));
                } catch (final NumberFormatException e) {
                    Bukkit.getLogger().info("Drop chance for helmet was required, but left empty."
                            + " Setting to 1.0");
                    dropChances[4] = 1.0f;
                }
            }
        }
        final BukkitQuestMob qm = new BukkitQuestMob(entityType, loc, amounts != null ? amounts : 1);
        qm.setName(name);
        qm.inventory = inventory;
        qm.dropChances = dropChances;
        return qm;
    }
}
