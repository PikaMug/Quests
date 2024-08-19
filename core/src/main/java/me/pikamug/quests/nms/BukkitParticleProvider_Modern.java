/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.nms;

import me.pikamug.quests.enums.BukkitPreBuiltParticle;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

class BukkitParticleProvider_Modern extends BukkitParticleProvider {

    private static final Map<BukkitPreBuiltParticle, Object> PARTICLES = new HashMap<>();

    static {
        PARTICLES.put(BukkitPreBuiltParticle.ENCHANT, Particle.ENCHANT);
        PARTICLES.put(BukkitPreBuiltParticle.CRIT, Particle.CRIT);
        PARTICLES.put(BukkitPreBuiltParticle.SPELL, Particle.INSTANT_EFFECT);
        PARTICLES.put(BukkitPreBuiltParticle.MAGIC_CRIT, Particle.ENCHANTED_HIT);
        PARTICLES.put(BukkitPreBuiltParticle.MOB_SPELL, Particle.ENTITY_EFFECT);
        PARTICLES.put(BukkitPreBuiltParticle.NOTE, Particle.NOTE);
        PARTICLES.put(BukkitPreBuiltParticle.PORTAL, Particle.PORTAL);
        PARTICLES.put(BukkitPreBuiltParticle.DUST, Particle.DUST);
        PARTICLES.put(BukkitPreBuiltParticle.WITCH, Particle.WITCH);
        PARTICLES.put(BukkitPreBuiltParticle.SNOWBALL, Particle.ITEM_SNOWBALL);
        PARTICLES.put(BukkitPreBuiltParticle.SPLASH, Particle.SPLASH);
        PARTICLES.put(BukkitPreBuiltParticle.SMOKE, Particle.TRIAL_OMEN);
    }

    @Override
    Map<BukkitPreBuiltParticle, Object> getParticleMap() {
        return PARTICLES;
    }

    @Override
    void spawnParticle(final Player player, final Location location, final Object particle, final float offsetX, final float offsetY, final float offsetZ, 
            final float speed, final int count, final int[] data) {
        player.spawnParticle((Particle) particle, location, count, offsetX, offsetY, offsetZ, speed, data);
    }
}
