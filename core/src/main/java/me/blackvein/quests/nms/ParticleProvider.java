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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.Map;

public abstract class ParticleProvider {

    private static ParticleProvider loaded;

    static {
        final String internalsName = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            final String packageName = ParticleProvider.class.getPackage().getName();
            if (internalsName.startsWith("v1_8_R")) {
                loaded = (ParticleProvider) Class.forName(packageName + ".ParticleProvider_" + internalsName)
                        .newInstance();
            } else {
                loaded = new ParticleProvider_Bukkit();
            }
        } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException
                | ClassCastException exception) {
            Bukkit.getLogger().severe("[Quests] No valid particle implementation for version " + internalsName);
        }
    }

    abstract Map<PreBuiltParticle, Object> getParticleMap();

    abstract void spawnParticle(Player player, Location location, Object particle, float offsetX, float offsetY,
            float offsetZ, float speed, int count, int[] data);

    /**
     * Sends the particle to the player.
     *
     * @param player
     *                   The player to send the particle to.
     * @param location
     *                   The location to play the particle at.
     * @param particleId
     *                   The particle identifier.
     * @param offsetX
     *                   The offset of the particle in the X direction.
     * @param offsetY
     *                   The offset of the particle in the Y direction.
     * @param offsetZ
     *                   The offset of the particle in the Z direction.
     * @param speed
     *                   The speed that the particle effect will be played at.
     * @param count
     *                   The number of particles to send to the player.
     * @param data
     *                   An integer array needed for some particles, this is used for
     *                   packets such as block crack or particle colour on redstone /
     *                   firework particles.
     */
    public static void sendToPlayer(final Player player, final Location location, final String particleId, final float offsetX, final float offsetY,
            final float offsetZ, final float speed, final int count, final int[] data) {
        final Object particle;
        final PreBuiltParticle pbp = PreBuiltParticle.fromIdentifier(particleId);
        if (pbp != null) {
            particle = loaded.getParticleMap().get(pbp);
        } else {
            try {
                particle = Particle.valueOf(particleId);
            } catch (final IllegalArgumentException exception) {
                return; // Fail silently
            }
        }
        loaded.spawnParticle(player, location, particle, offsetX, offsetY, offsetZ, speed, count, data);
    }

    /**
     * Sends the particle to the player.
     *
     * @param player
     *                   The player to send the particle to.
     * @param location
     *                   The location to play the particle at.
     * @param particleId
     *                   The particle identifier.
     */
    public static void sendToPlayer(final Player player, final Location location, final String particleId) {
        final PreBuiltParticle particle = PreBuiltParticle.fromIdentifier(particleId);
        if (particle != null) {
            final Location pos = location.clone();
            if (particle.getVector() != null) {
                pos.add(particle.getVector());
            }
            sendToPlayer(player, location, particle);
        } else {
            try {
                loaded.spawnParticle(player, location, Particle.valueOf(particleId), 0, 0, 0, 1, 3, null);
            } catch (final NoClassDefFoundError e1) {
                Bukkit.getLogger().severe("[Quests] This protocol does not support npc-effect: " + particleId);
            } catch (final IllegalArgumentException e2) {
                // Fail silently
            }
        }
    }

    /**
     * Sends the particle to the player.
     *
     * @param player
     *                 The player to send the particle to.
     * @param location
     *                 The location to play the particle at.
     * @param particle
     *                 The pre-built particle.
     */
    public static void sendToPlayer(final Player player, final Location location, final PreBuiltParticle particle) {
        final Location pos = location.clone();
        if (particle.getVector() != null) {
            pos.add(particle.getVector());
        }
        try {
            loaded.spawnParticle(player, pos, loaded.getParticleMap().get(particle), particle.getOffsetX(),
                    particle.getOffsetY(), particle.getOffsetZ(), particle.getSpeed(),particle.getCount(), null);
        } catch (final IllegalArgumentException exception) {
            // Fail silently
        }
    }
}
