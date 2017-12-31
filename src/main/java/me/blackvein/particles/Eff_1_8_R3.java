/*******************************************************************************************************
 * Continued by FlyingPikachu/HappyPikachu with permission from _Blackvein_. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

package me.blackvein.particles;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;

/**
 * This is the Eff_1_8_R3 Enum, it contains all valid effects that players can
 * use with the 1.8.4 - 1.8.8 server versions.
 * 
 * @author FlyingPikachu
 * @author GregZ_
 * @since 2.5.0
 * @version 4
 */
public enum Eff_1_8_R3 {

	EXPLOSION(EnumParticle.EXPLOSION_NORMAL),
	EXPLOSION_LARGE(EnumParticle.EXPLOSION_LARGE),
	EXPLOSION_HUGE(EnumParticle.EXPLOSION_HUGE),
	FIREWORKS_SPARK(EnumParticle.FIREWORKS_SPARK),
	BUBBLE(EnumParticle.WATER_BUBBLE),
	WAKE(EnumParticle.WATER_WAKE),
	SPLASH(EnumParticle.WATER_SPLASH),
	SUSPENDED(EnumParticle.SUSPENDED),
	DEPTH_SUSPEND(EnumParticle.SUSPENDED_DEPTH),
	CRIT(EnumParticle.CRIT),
	MAGIC_CRIT(EnumParticle.CRIT_MAGIC),
	SMOKE(EnumParticle.SMOKE_NORMAL),
	LARGE_SMOKE(EnumParticle.SMOKE_LARGE),
	SPELL(EnumParticle.SPELL),
	INSTANT_SPELL(EnumParticle.SPELL_INSTANT),
	MOB_SPELL(EnumParticle.SPELL_MOB),
	MOB_SPELL_AMBIENT(EnumParticle.SPELL_MOB_AMBIENT),
	WITCH_MAGIC(EnumParticle.SPELL_WITCH),
	DRIP_WATER(EnumParticle.DRIP_WATER),
	DRIP_LAVA(EnumParticle.DRIP_LAVA),
	ANGRY_VILLAGER(EnumParticle.VILLAGER_ANGRY),
	HAPPY_VILLAGER(EnumParticle.VILLAGER_HAPPY),
	TOWN_AURA(EnumParticle.TOWN_AURA),
	NOTE(EnumParticle.NOTE),
	PORTAL(EnumParticle.PORTAL),
	ENCHANTMENT_TABLE(EnumParticle.ENCHANTMENT_TABLE),
	FLAME(EnumParticle.FLAME),
	LAVA(EnumParticle.LAVA),
	FOOTSTEP(EnumParticle.FOOTSTEP),
	CLOUD(EnumParticle.CLOUD),
	RED_DUST(EnumParticle.REDSTONE),
	SNOWBALL_POOF(EnumParticle.SNOWBALL),
	SNOW_SHOVEL(EnumParticle.SNOW_SHOVEL),
	SLIME(EnumParticle.SLIME),
	HEART(EnumParticle.HEART),
	BARRIER(EnumParticle.BARRIER),
	ICONCRACK_(EnumParticle.ITEM_CRACK),
	BLOCKCRACK_(EnumParticle.BLOCK_CRACK),
	BLOCKDUST_(EnumParticle.BLOCK_DUST),
	DROPLET(EnumParticle.WATER_DROP),
	TAKE(EnumParticle.ITEM_TAKE),
	MOB_APPEARANCE(EnumParticle.MOB_APPEARANCE);

	/**
	 * The NMS EnumParticle to be sent to the player.
	 */
	private final EnumParticle particleEnum;

	/**
	 * Create a new instance of the Eff_1_8_R3 enum with the given particle type
	 * to be sent.
	 * 
	 * @param particleEnum
	 *            The particle type to be sent to the player in the
	 *            PacketPlayOutWorldParticles packet.
	 */
	Eff_1_8_R3(EnumParticle particleEnum) {
		this.particleEnum = particleEnum;
	}

	/**
	 * Send the given particle to the player via NMS. It should be noted that
	 * all particles have the range limit set to 256 due to the second variable
	 * in the packet constructor being false.
	 * 
	 * @param player
	 *            The player to send the particle to.
	 * @param location
	 *            The location to play the particle at.
	 * @param offsetX
	 *            The offset of the particle in the X direction.
	 * @param offsetY
	 *            The offset of the particle in the Y direction.
	 * @param offsetZ
	 *            The offset of the particle in the Z direction.
	 * @param speed
	 *            The speed that the particle effect will be played at.
	 * @param count
	 *            The number of particles to send to the player.
	 * @param data
	 *            An integer array needed for some particles, this is used for
	 *            packets such as block crack or particle colour on redstone /
	 *            firework particles.
	 * @throws Exception
	 *             A ReportedException may be thrown if the network manager
	 *             fails to handle the packet.
	 */
	public void sendToPlayer(Player player, Location location, float offsetX, float offsetY, float offsetZ, float speed, int count, int[] data) throws Exception {
		PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(particleEnum, false, (float) location.getX(), (float) location.getY(), (float) location.getZ(), offsetX, offsetY, offsetZ, speed, count, data);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
	}
}