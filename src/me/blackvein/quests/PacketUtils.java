/**
 *
 * This software is part of the mcMMOStatsGui
 *
 * mcMMOStatsGui is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * mcMMOStatsGui is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with mcMMOStatsGui. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package me.blackvein.quests;

import net.minecraft.server.v1_5_R3.Packet;
import org.bukkit.craftbukkit.v1_5_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class PacketUtils {

	public static void sendPacket(Player player, Packet packet) {
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
	}
}