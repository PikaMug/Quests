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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

public class FabricQuestMob implements QuestMob {

    private String name;
    private EntityType<?> entityType;
    private Integer spawnAmounts = 1;
    private Float[] dropChances = new Float[0];
    private ServerLevel level;
    private BlockPos location;

    @Override public String getName() { return name; }
    @Override public void setName(String v) { this.name = v; }
    @Override public Integer getSpawnAmounts() { return spawnAmounts; }
    @Override public void setSpawnAmounts(int v) { this.spawnAmounts = v; }
    @Override public Float[] getDropChances() { return dropChances; }
    @Override public void setDropChances(Float[] v) { this.dropChances = v; }

    public EntityType<?> getEntityType() { return entityType; }
    public void setEntityType(EntityType<?> entityType) { this.entityType = entityType; }
    public ServerLevel getLevel() { return level; }
    public void setLevel(ServerLevel level) { this.level = level; }
    public BlockPos getLocation() { return location; }
    public void setLocation(BlockPos location) { this.location = location; }

    @Override
    public void spawn() {
        if (entityType == null || level == null || location == null) return;
        for (int i = 0; i < (spawnAmounts != null ? spawnAmounts : 1); i++) {
            final Mob mob = (Mob) entityType.create(level, EntitySpawnReason.COMMAND);
            if (mob != null) {
                mob.setPos(location.getX() + 0.5, location.getY(), location.getZ() + 0.5);
                mob.setYRot(0);
                mob.setXRot(0);
                level.addFreshEntity(mob);
            }
        }
    }
}
