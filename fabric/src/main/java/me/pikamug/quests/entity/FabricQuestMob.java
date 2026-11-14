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

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;

public class FabricQuestMob implements QuestMob {

    private String name;
    private EntityType<?> entityType;
    private Integer spawnAmounts = 1;
    private ItemStack[] inventory = new ItemStack[5];
    private Float[] dropChances = new Float[5];
    private ServerLevel level;
    private BlockPos location;

    @Override public String getName() { return name; }
    @Override public void setName(String v) { this.name = v; }
    @Override public Integer getSpawnAmounts() { return spawnAmounts; }
    @Override public void setSpawnAmounts(int v) { this.spawnAmounts = v; }
    @Override public Float[] getDropChances() { return dropChances; }
    @Override public void setDropChances(Float[] v) { this.dropChances = v; }

    public EntityType<?> getType() { return entityType; }
    public void setType(EntityType<?> entityType) { this.entityType = entityType; }
    public ServerLevel getLevel() { return level; }
    public void setLevel(ServerLevel level) { this.level = level; }
    public BlockPos getSpawnLocation() { return location; }
    public void setSpawnLocation(BlockPos location) { this.location = location; }

    public ItemStack[] getInventory() { return inventory; }
    public void setInventory(ItemStack[] v) { this.inventory = v; }

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

    @Override
    public void spawn() {
        if (entityType == null || level == null || location == null) return;
        for (int i = 0; i < (spawnAmounts != null ? spawnAmounts : 1); i++) {
            final Mob mob = (Mob) entityType.create(level, EntitySpawnReason.COMMAND);
            if (mob != null) {
                mob.setPos(location.getX() + 0.5, location.getY(), location.getZ() + 0.5);
                mob.setYRot(0);
                mob.setXRot(0);
                if (name != null) {
                    mob.setCustomName(Component.literal(name));
                    mob.setCustomNameVisible(true);
                }
                applyEquipment(mob);
                level.addFreshEntity(mob);
            }
        }
    }

    private void applyEquipment(final Mob mob) {
        final EquipmentSlot[] slots = {EquipmentSlot.MAINHAND, EquipmentSlot.FEET, EquipmentSlot.LEGS,
                EquipmentSlot.CHEST, EquipmentSlot.HEAD};
        for (int i = 0; i < slots.length; i++) {
            final ItemStack stack = inventory[i];
            if (stack != null && !stack.isEmpty()) {
                mob.setItemSlot(slots[i], stack.copy());
            }
            if (dropChances[i] != null) {
                mob.setDropChance(slots[i], dropChances[i]);
            }
        }
    }
}
