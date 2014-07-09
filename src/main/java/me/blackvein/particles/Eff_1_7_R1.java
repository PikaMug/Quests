package me.blackvein.particles;

import me.blackvein.quests.util.ReflectionUtil;
import net.minecraft.server.v1_7_R1.PacketPlayOutWorldParticles;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public enum Eff_1_7_R1 {

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

    private final String particleName;

    Eff_1_7_R1(String particleName) {
        this.particleName = particleName;
    }

    public void sendToPlayer(Player player, Location location, float offsetX, float offsetY, float offsetZ, float speed, int count) throws Exception {
        PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles();
        ReflectionUtil.setValue(packet, "a", particleName);
        ReflectionUtil.setValue(packet, "b", (float) location.getX());
        ReflectionUtil.setValue(packet, "c", (float) location.getY());
        ReflectionUtil.setValue(packet, "d", (float) location.getZ());
        ReflectionUtil.setValue(packet, "e", offsetX);
        ReflectionUtil.setValue(packet, "f", offsetY);
        ReflectionUtil.setValue(packet, "g", offsetZ);
        ReflectionUtil.setValue(packet, "h", speed);
        ReflectionUtil.setValue(packet, "i", count);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

}
