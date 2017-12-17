package me.blackvein.particles;

import net.minecraft.server.v1_9_R2.EnumParticle;
import net.minecraft.server.v1_9_R2.PacketPlayOutWorldParticles;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * This is the Eff_1_9_R2 Enum, it contains all valid effects that players can
 * use with the 1.9.4 server version.
 *
 * @author FlyingPikachu
 * @author GregZ_
 * @version 4
 * @since 2.6.1
 */
public enum Eff_1_9_R2 {

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
    MOB_APPEARANCE(EnumParticle.MOB_APPEARANCE),
    SWEEPING_DUST(EnumParticle.SWEEP_ATTACK),
    DRAGON_BREATH(EnumParticle.DRAGON_BREATH),
    ENDROD(EnumParticle.END_ROD),
    DAMAGE_INDICATOR(EnumParticle.DAMAGE_INDICATOR),;

    /**
     * The NMS EnumParticle to be sent to the player.
     */
    private final EnumParticle particleEnum;

    /**
     * Create a new instance of the Eff_1_9_R2 enum with the given particle type
     * to be sent.
     *
     * @param particleEnum The particle type to be sent to the player in the
     *                     PacketPlayOutWorldParticles packet.
     */
    Eff_1_9_R2(EnumParticle particleEnum) {
        this.particleEnum = particleEnum;
    }

    /**
     * Send the given particle to the player via NMS. It should be noted that
     * all particles have the range limit set to 256 due to the second variable
     * in the packet constructor being false.
     *
     * @param player   The player to send the particle to.
     * @param location The location to play the particle at.
     * @param offsetX  The offset of the particle in the X direction.
     * @param offsetY  The offset of the particle in the Y direction.
     * @param offsetZ  The offset of the particle in the Z direction.
     * @param speed    The speed that the particle effect will be played at.
     * @param count    The number of particles to send to the player.
     * @param data     An integer array needed for some particles, this is used for
     *                 packets such as block crack or particle colour on redstone /
     *                 firework particles.
     * @throws Exception A ReportedException may be thrown if the network manager
     *                   fails to handle the packet.
     */
    public void sendToPlayer(Player player, Location location, float offsetX, float offsetY, float offsetZ, float speed, int count, int[] data) throws Exception {
        PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(particleEnum, false, (float) location.getX(), (float) location.getY(), (float) location.getZ(), offsetX, offsetY, offsetZ, speed, count, data);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }
}