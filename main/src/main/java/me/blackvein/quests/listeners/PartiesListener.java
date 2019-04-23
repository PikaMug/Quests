package me.blackvein.quests.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.alessiodp.parties.api.events.bukkit.party.BukkitPartiesPartyPostCreateEvent;
import com.alessiodp.parties.api.events.bukkit.party.BukkitPartiesPartyPostDeleteEvent;
import com.alessiodp.parties.api.events.bukkit.player.BukkitPartiesPlayerPostJoinEvent;
import com.alessiodp.parties.api.events.bukkit.player.BukkitPartiesPlayerPostLeaveEvent;

import me.blackvein.quests.util.Lang;

public class PartiesListener implements Listener {
	
	@EventHandler
	public void onPartyCreate(BukkitPartiesPartyPostCreateEvent event) {
		Bukkit.getServer().getPlayer(event.getCreator().getPlayerUUID()).sendMessage(ChatColor.YELLOW + Lang.get("questPartiesCreate"));
	}
	
	@EventHandler
	public void onPartyDeleteEvent(BukkitPartiesPartyPostDeleteEvent event) {
		Bukkit.getServer().getPlayer(event.getCommandSender().getPlayerUUID()).sendMessage(ChatColor.RED + Lang.get("questPartiesDelete"));
	}
	
	@EventHandler
	public void onPlayerJoinEvent(BukkitPartiesPlayerPostJoinEvent event) {
		if (event.isInvited()) {
			Player i = Bukkit.getServer().getPlayer(event.getInviter());
			i.sendMessage(ChatColor.GREEN + Lang.get(i, "questPartiesInvite").replace("<player>", i.getName()));
		}
		Player p = Bukkit.getServer().getPlayer(event.getPartyPlayer().getPlayerUUID());
		p.sendMessage(ChatColor.GREEN + Lang.get(p, "questPartiesJoin").replace("<player>", p.getName()));
	}
	
	@EventHandler
	public void onPlayerLeaveEvent(BukkitPartiesPlayerPostLeaveEvent event) {
		if (event.isKicked()) {
			Player k = Bukkit.getServer().getPlayer(event.getKicker().getPlayerUUID());
			k.sendMessage(ChatColor.RED + Lang.get(k, "questPartiesKicked").replace("<player>", k.getName()));
		}
		Player p = Bukkit.getServer().getPlayer(event.getPartyPlayer().getPlayerUUID());
		p.sendMessage(ChatColor.RED + Lang.get(p, "questPartiesLeave").replace("<player>", p.getName()));
	}
}
