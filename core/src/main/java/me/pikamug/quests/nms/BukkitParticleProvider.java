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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.Map;

public abstract class BukkitParticleProvider {

    private static BukkitParticleProvider loaded;

    static {
        final String internalsName = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            final String packageName = BukkitParticleProvider.class.getPackage().getName();
            if (internalsName.startsWith("v1_8_R")) {
                loaded = (BukkitParticleProvider) Class.forName(packageName + ".BukkitParticleProvider_" + internalsName)
                        .newInstance();
            } else {
                // Should not be an issue because single thread, alternatives welcome!
                loaded = new BukkitParticleProvider_Modern();
            }
        } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException
                | ClassCastException exception) {
            Bukkit.getLogger().severe("[Quests] No valid particle implementation for version " + internalsName);
        }
    }

    abstract Map<BukkitPreBuiltParticle, Object> getParticleMap();

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
        final BukkitPreBuiltParticle pbp = BukkitPreBuiltParticle.fromIdentifier(particleId);
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
        final BukkitPreBuiltParticle particle = BukkitPreBuiltParticle.fromIdentifier(particleId);
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
    public static void sendToPlayer(final Player player, final Location location, final BukkitPreBuiltParticle particle) {
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
