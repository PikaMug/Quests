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

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

class ParticleProvider_Bukkit extends ParticleProvider {

    private static final Map<PreBuiltParticle, Object> PARTICLES = new HashMap<>();

    static {
        PARTICLES.put(PreBuiltParticle.ENCHANT, Particle.ENCHANTMENT_TABLE);
        PARTICLES.put(PreBuiltParticle.CRIT, Particle.CRIT);
        PARTICLES.put(PreBuiltParticle.SPELL, Particle.SPELL_INSTANT);
        PARTICLES.put(PreBuiltParticle.MAGIC_CRIT, Particle.CRIT_MAGIC);
        PARTICLES.put(PreBuiltParticle.MOB_SPELL, Particle.SPELL_MOB);
        PARTICLES.put(PreBuiltParticle.NOTE, Particle.NOTE);
        PARTICLES.put(PreBuiltParticle.PORTAL, Particle.PORTAL);
        PARTICLES.put(PreBuiltParticle.DUST, Particle.REDSTONE);
        PARTICLES.put(PreBuiltParticle.WITCH, Particle.SPELL_WITCH);
        PARTICLES.put(PreBuiltParticle.SNOWBALL, Particle.SNOWBALL);
        PARTICLES.put(PreBuiltParticle.SPLASH, Particle.WATER_SPLASH);
        PARTICLES.put(PreBuiltParticle.SMOKE, Particle.TOWN_AURA);
    }

    @Override
    Map<PreBuiltParticle, Object> getParticleMap() {
        return PARTICLES;
    }

    @Override
    void spawnParticle(final Player player, final Location location, final Object particle, final float offsetX, final float offsetY, final float offsetZ, 
            final float speed, final int count, final int[] data) {
        player.spawnParticle((Particle) particle, location, count, offsetX, offsetY, offsetZ, speed, data);
    }
}
