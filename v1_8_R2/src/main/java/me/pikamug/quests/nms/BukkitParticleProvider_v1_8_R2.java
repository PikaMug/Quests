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
import net.minecraft.server.v1_8_R2.EnumParticle;
import net.minecraft.server.v1_8_R2.PacketPlayOutWorldParticles;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class BukkitParticleProvider_v1_8_R2 extends BukkitParticleProvider {

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
