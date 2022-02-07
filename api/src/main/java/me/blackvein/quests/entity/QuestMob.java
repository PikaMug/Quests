/*
 * Copyright (c) 2014 PikaMug and contributors. All rights reserved.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
