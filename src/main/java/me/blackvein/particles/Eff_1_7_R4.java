package me.blackvein.particles;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_7_R4.PacketPlayOutWorldParticles;

/**
 * This is the Eff_1_7_R4 Enum, it contains all valid effects that players can
 * use with the 1.7.10 server version.
 * 
 * @author Blackvein
 * @author GregZ_
 * @since 1.9.0
 * @version 3
 */
public enum Eff_1_7_R4 {

	HUGE_EXPLOSION("hugeexplosion"),
	LARGE_EXPLODE("largeexplode"),
	FIREWORKS_SPARK("fireworksSpark"),
	BUBBLE("bubble"),
	SUSPEND("susgpend"),
	DEPTH_SUSPEND("depthSuspend"),
	TOWN_AURA("townaura"),
	CRIT("crit"),
	MAGIC_CRIT("magicCrit"),
	MOB_SPELL("mobSpell"),
	MOB_SPELL_AMBIENT("mobSpellAmbient"),
	SPELL("spell"),
	INSTANT_SPELL("instantSpell"),
	WITCH_MAGIC("witchMagic"),
	NOTE("note"),
	PORTAL("portal"),
	ENCHANTMENT_TABLE("enchantmenttable"),
	EXPLODE("explode"),
	FLAME("flame"),
	LAVA("lava"),
	FOOTSTEP("footstep"),
	SPLASH("splash"),
	LARGE_SMOKE("largesmoke"),
	CLOUD("cloud"),
	RED_DUST("reddust"),
	SNOWBALL_POOF("snowballpoof"),
	DRIP_WATER("dripWater"),
	DRIP_LAVA("dripLava"),
	SNOW_SHOVEL("snowshovel"),
	SLIME("slime"),
	HEART("heart"),
	ANGRY_VILLAGER("angryVillager"),
	HAPPY_VILLAGER("happyVillager"),
	ICONCRACK("iconcrack_"),
	TILECRACK("tilecrack_");

	/**
	 * The name of the particle to be sent.
	 */
	private final String particleName;

	/**
	 * Create a new instance of the Eff_1_7_R4 enum with the given particle name
	 * to be sent.
	 * 
	 * @param particleName
	 *            The name of the particle to be sent to the player in the
	 *            PacketPlayOutWorldParticles packet.
	 */
	Eff_1_7_R4(String particleName) {
		this.particleName = particleName;
	}

	/**
	 * Send the given particle to the player via NMS.
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
	 * @throws Exception
	 *             A ReportedException may be thrown if the network manager
	 *             fails to handle the packet.
	 */
	public void sendToPlayer(Player player, Location location, float offsetX, float offsetY, float offsetZ, float speed, int count) throws Exception {
		PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(particleName, (float) location.getX(), (float) location.getY(), (float) location.getZ(), offsetX, offsetY, offsetZ, speed, count);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
	}
}
