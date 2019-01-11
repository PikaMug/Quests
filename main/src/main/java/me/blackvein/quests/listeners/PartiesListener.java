package me.blackvein.quests.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.alessiodp.parties.api.events.bukkit.party.BukkitPartiesPartyPostCreateEvent;
import com.alessiodp.parties.api.events.bukkit.party.BukkitPartiesPartyPostDeleteEvent;
import com.alessiodp.parties.api.events.bukkit.player.BukkitPartiesPlayerPostJoinEvent;
import com.alessiodp.parties.api.events.bukkit.player.BukkitPartiesPlayerPostLeaveEvent;

public class PartiesListener implements Listener {
	//TODO add configurable strings
	
	@EventHandler
	public void onPartyCreate(BukkitPartiesPartyPostCreateEvent event) {
		Bukkit.getServer().getPlayer(event.getCreator().getPlayerUUID()).sendMessage(ChatColor.YELLOW + "Players added to this party may perform quests together!");
	}
	
	@EventHandler
	public void onPartyDeleteEvent(BukkitPartiesPartyPostDeleteEvent event) {
		Bukkit.getServer().getPlayer(event.getCommandSender().getPlayerUUID()).sendMessage(ChatColor.RED + "The quest party was disbanded.");
	}
	
	@EventHandler
	public void onPlayerJoinEvent(BukkitPartiesPlayerPostJoinEvent event) {
		if (event.isInvited()) {
			Bukkit.getServer().getPlayer(event.getInviter()).sendMessage(ChatColor.GREEN + event.getPartyPlayer().getName() + " can now perform quests with you!");
		}
		Bukkit.getServer().getPlayer(event.getPartyPlayer().getPlayerUUID()).sendMessage(ChatColor.GREEN + "You can now perform quests with " + event.getParty().getName() + ".");
	}
	
	@EventHandler
	public void onPlayerLeaveEvent(BukkitPartiesPlayerPostLeaveEvent event) {
		if (event.isKicked()) {
			Bukkit.getServer().getPlayer(event.getKicker().getPlayerUUID()).sendMessage(ChatColor.RED + event.getPartyPlayer().getName() + " can no longer perform quests with you.");
		}
		Bukkit.getServer().getPlayer(event.getPartyPlayer().getPlayerUUID()).sendMessage(ChatColor.RED + "You can no longer perform quests with " + event.getParty().getName() + ".");
	}
}
