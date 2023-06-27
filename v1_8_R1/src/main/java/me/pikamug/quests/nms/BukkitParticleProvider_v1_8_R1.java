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

package me.pikamug.quests.nms;

import me.pikamug.quests.enums.BukkitPreBuiltParticle;
import net.minecraft.server.v1_8_R1.EnumParticle;
import net.minecraft.server.v1_8_R1.PacketPlayOutWorldParticles;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class BukkitParticleProvider_v1_8_R1 extends BukkitParticleProvider {

    private static final Map<BukkitPreBuiltParticle, Object> PARTICLES = new HashMap<>();

    static {
        PARTICLES.put(BukkitPreBuiltParticle.ENCHANT, EnumParticle.ENCHANTMENT_TABLE);
        PARTICLES.put(BukkitPreBuiltParticle.CRIT, EnumParticle.CRIT);
        PARTICLES.put(BukkitPreBuiltParticle.SPELL, EnumParticle.SPELL_INSTANT);
        PARTICLES.put(BukkitPreBuiltParticle.MAGIC_CRIT, EnumParticle.CRIT_MAGIC);
        PARTICLES.put(BukkitPreBuiltParticle.MOB_SPELL, EnumParticle.SPELL_MOB);
        PARTICLES.put(BukkitPreBuiltParticle.NOTE, EnumParticle.NOTE);
        PARTICLES.put(BukkitPreBuiltParticle.PORTAL, EnumParticle.PORTAL);
        PARTICLES.put(BukkitPreBuiltParticle.DUST, EnumParticle.REDSTONE);
        PARTICLES.put(BukkitPreBuiltParticle.WITCH, EnumParticle.SPELL_WITCH);
        PARTICLES.put(BukkitPreBuiltParticle.SNOWBALL, EnumParticle.SNOWBALL);
        PARTICLES.put(BukkitPreBuiltParticle.SPLASH, EnumParticle.WATER_SPLASH);
        PARTICLES.put(BukkitPreBuiltParticle.SMOKE, EnumParticle.TOWN_AURA);
    }

    @Override
    Map<BukkitPreBuiltParticle, Object> getParticleMap() {
        return PARTICLES;
    }

    @Override
    void spawnParticle(final Player player, final Location location, final Object particle, final float offsetX,
            final float offsetY, final float offsetZ, final float speed, final int count, final int[] data) {
        final PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles((EnumParticle) particle, false,
                (float) location.getX(), (float) location.getY(), (float) location.getZ(), offsetX, offsetY, offsetZ,
                speed, count, data);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }
}
