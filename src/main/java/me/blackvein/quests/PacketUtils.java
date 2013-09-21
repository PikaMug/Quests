package me.blackvein.quests;

import net.minecraft.server.v1_6_R3.Packet;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class PacketUtils {

	public static void sendPacket(Player player, Packet packet) {
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
	}
}