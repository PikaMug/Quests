package me.blackvein.particles;

import me.blackvein.quests.util.ReflectionUtil;
import net.minecraft.server.v1_9_R2.EnumParticle;
import net.minecraft.server.v1_9_R2.PacketPlayOutWorldParticles;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

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

    private final EnumParticle particleEnum;

    Eff_1_9_R2(EnumParticle particleEnum) {
        this.particleEnum = particleEnum;
    }

    public void sendToPlayer(Player player, Location location, float offsetX, float offsetY, float offsetZ, float speed, int count, int[] data) throws Exception {
        PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles();
        ReflectionUtil.setValue(packet, "a", particleEnum);
        ReflectionUtil.setValue(packet, "b", (float) location.getX());
        ReflectionUtil.setValue(packet, "c", (float) location.getY());
        ReflectionUtil.setValue(packet, "d", (float) location.getZ());
        ReflectionUtil.setValue(packet, "e", offsetX);
        ReflectionUtil.setValue(packet, "f", offsetY);
        ReflectionUtil.setValue(packet, "g", offsetZ);
        ReflectionUtil.setValue(packet, "h", speed);
        ReflectionUtil.setValue(packet, "i", count);
        ReflectionUtil.setValue(packet, "k", data);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }
    
}