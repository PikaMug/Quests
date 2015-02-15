package me.blackvein.particles;

import me.blackvein.quests.util.ReflectionUtil;
import net.minecraft.server.v1_8_R1.PacketPlayOutWorldParticles;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public enum Eff_1_8_R1 {
	
	EXPLOSION("0"),
	EXPLOSION_LARGE("1"),
    EXPLOSION_HUGE("2"),
    FIREWORKS_SPARK("3"),
    BUBBLE("4"),
    WAKE("5"),
    SPLASH("6"),
    SUSPENDED("7"),
    DEPTH_SUSPEND("8"),
    CRIT("9"),
    MAGIC_CRIT("10"),
    SMOKE("11"),
    LARGE_SMOKE("12"),
    SPELL("13"),
    INSTANT_SPELL("14"),
    MOB_SPELL("15"),
    MOB_SPELL_AMBIENT("16"),
    WITCH_MAGIC("17"),
    DRIP_WATER("18"),
    DRIP_LAVA("19"),
    ANGRY_VILLAGER("20"),
    HAPPY_VILLAGER("21"),
    TOWN_AURA("22"),
    NOTE("23"),
    PORTAL("24"),
    ENCHANTMENT_TABLE("25"),
    FLAME("26"),
    LAVA("27"),
    FOOTSTEP("28"),
    CLOUD("29"),
    RED_DUST("30"),
    SNOWBALL_POOF("31"),
    SNOW_SHOVEL("32"),
    SLIME("33"),
    HEART("34"),
    BARRIER("35"),
    ICONCRACK_("36"),
    BLOCKCRACK_("37"),
    BLOCKDUST_("38"),
	DROPLET("39"),
	TAKE("40"),
	MOB_APPEARANCE("41");

    private final String particleId;

    Eff_1_8_R1(String particleId) {
        this.particleId = particleId;
    }

    public void sendToPlayer(Player player, Location location, boolean longDistance, float offsetX, float offsetY, float offsetZ, float speed, int count, int[] data) throws Exception {
        PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles();
        ReflectionUtil.setValue(packet, "a", particleId);
        ReflectionUtil.setValue(packet, "b", longDistance);
        ReflectionUtil.setValue(packet, "c", (float) location.getX());
        ReflectionUtil.setValue(packet, "d", (float) location.getY());
        ReflectionUtil.setValue(packet, "e", (float) location.getZ());
        ReflectionUtil.setValue(packet, "f", offsetX);
        ReflectionUtil.setValue(packet, "g", offsetY);
        ReflectionUtil.setValue(packet, "h", offsetZ);
        ReflectionUtil.setValue(packet, "i", speed);
        ReflectionUtil.setValue(packet, "j", count);
        ReflectionUtil.setValue(packet, "k", data);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }
    
}
