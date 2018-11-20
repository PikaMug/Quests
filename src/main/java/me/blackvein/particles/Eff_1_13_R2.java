package me.blackvein.particles;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * This is the Eff_1_13_R2 Enum, it contains all valid effects that players can
 * use with the 1.13.2 server version.
 * 
 * @author FlyingPikachu
 * @since 3.4.1
 * @version 3
 */

public enum Eff_1_13_R2 {
	
	EXPLOSION(Particle.EXPLOSION_NORMAL),
	EXPLOSION_LARGE(Particle.EXPLOSION_LARGE),
	EXPLOSION_HUGE(Particle.EXPLOSION_HUGE),
	FIREWORKS_SPARK(Particle.FIREWORKS_SPARK),
	BUBBLE(Particle.WATER_BUBBLE),
	WAKE(Particle.WATER_WAKE),
	SPLASH(Particle.WATER_SPLASH),
	SUSPENDED(Particle.SUSPENDED),
	DEPTH_SUSPEND(Particle.SUSPENDED_DEPTH),
	CRIT(Particle.CRIT),
	MAGIC_CRIT(Particle.CRIT_MAGIC),
	SMOKE(Particle.SMOKE_NORMAL),
	LARGE_SMOKE(Particle.SMOKE_LARGE),
	SPELL(Particle.SPELL),
	INSTANT_SPELL(Particle.SPELL_INSTANT),
	MOB_SPELL(Particle.SPELL_MOB),
	MOB_SPELL_AMBIENT(Particle.SPELL_MOB_AMBIENT),
	WITCH_MAGIC(Particle.SPELL_WITCH),
	DRIP_WATER(Particle.DRIP_WATER),
	DRIP_LAVA(Particle.DRIP_LAVA),
	ANGRY_VILLAGER(Particle.VILLAGER_ANGRY),
	HAPPY_VILLAGER(Particle.VILLAGER_HAPPY),
	TOWN_AURA(Particle.TOWN_AURA),
	NOTE(Particle.NOTE),
	PORTAL(Particle.PORTAL),
	ENCHANTMENT_TABLE(Particle.ENCHANTMENT_TABLE),
	FLAME(Particle.FLAME),
	LAVA(Particle.LAVA),
	CLOUD(Particle.CLOUD),
	RED_DUST(Particle.REDSTONE),
	SNOWBALL_POOF(Particle.SNOWBALL),
	SNOW_SHOVEL(Particle.SNOW_SHOVEL),
	SLIME(Particle.SLIME),
	HEART(Particle.HEART),
	BARRIER(Particle.BARRIER),
	ICONCRACK_(Particle.ITEM_CRACK),
	BLOCKCRACK_(Particle.BLOCK_CRACK),
	BLOCKDUST_(Particle.BLOCK_DUST),
	DROPLET(Particle.WATER_DROP),
	MOB_APPEARANCE(Particle.MOB_APPEARANCE),
	SWEEPING_DUST(Particle.SWEEP_ATTACK),
	DRAGON_BREATH(Particle.DRAGON_BREATH),
	ENDROD(Particle.END_ROD),
	DAMAGE_INDICATOR(Particle.DAMAGE_INDICATOR),
	FALLING_DUST(Particle.FALLING_DUST),
	SPIT(Particle.SPIT),
	TOTEM(Particle.TOTEM),
	BUBBLE_COLUMN_UP(Particle.BUBBLE_COLUMN_UP),
	BUBBLE_POP(Particle.BUBBLE_POP),
	CURRENT_DOWN(Particle.CURRENT_DOWN),
	SQUID_INK(Particle.SQUID_INK),
	NAUTILUS(Particle.NAUTILUS),
	DOLPHIN(Particle.DOLPHIN);

	/**
	 * The NMS Particle to be sent to the player.
	 */
	private final Particle particleEnum;

	/**
	 * Create a new instance of the Eff_1_13_R2 enum with the given particle type
	 * to be sent.
	 * 
	 * @param particleEnum
	 *            The particle type to be sent to the player in the
	 *            PacketPlayOutWorldParticles packet.
	 */
	Eff_1_13_R2(Particle particleEnum) {
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
		((CraftPlayer) player).spawnParticle(particleEnum, location, count, offsetX, offsetY, offsetZ, speed, data);
	}
}
