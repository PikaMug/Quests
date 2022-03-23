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

package me.blackvein.quests.nms;

import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class ParticleProvider_v1_8_R3 extends ParticleProvider {

    private static final Map<PreBuiltParticle, Object> PARTICLES = new HashMap<>();

    static {
        PARTICLES.put(PreBuiltParticle.ENCHANT, EnumParticle.ENCHANTMENT_TABLE);
        PARTICLES.put(PreBuiltParticle.CRIT, EnumParticle.CRIT);
        PARTICLES.put(PreBuiltParticle.SPELL, EnumParticle.SPELL_INSTANT);
        PARTICLES.put(PreBuiltParticle.MAGIC_CRIT, EnumParticle.CRIT_MAGIC);
        PARTICLES.put(PreBuiltParticle.MOB_SPELL, EnumParticle.SPELL_MOB);
        PARTICLES.put(PreBuiltParticle.NOTE, EnumParticle.NOTE);
        PARTICLES.put(PreBuiltParticle.PORTAL, EnumParticle.PORTAL);
        PARTICLES.put(PreBuiltParticle.DUST, EnumParticle.REDSTONE);
        PARTICLES.put(PreBuiltParticle.WITCH, EnumParticle.SPELL_WITCH);
        PARTICLES.put(PreBuiltParticle.SNOWBALL, EnumParticle.SNOWBALL);
        PARTICLES.put(PreBuiltParticle.SPLASH, EnumParticle.WATER_SPLASH);
        PARTICLES.put(PreBuiltParticle.SMOKE, EnumParticle.TOWN_AURA);
    }

    @Override
    Map<PreBuiltParticle, Object> getParticleMap() {
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
