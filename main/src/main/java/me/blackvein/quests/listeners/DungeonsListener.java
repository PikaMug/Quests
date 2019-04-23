package me.blackvein.quests.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import de.erethon.dungeonsxl.event.dgroup.DGroupCreateEvent;
import de.erethon.dungeonsxl.event.dgroup.DGroupDisbandEvent;
import de.erethon.dungeonsxl.event.dplayer.DPlayerJoinDGroupEvent;
import de.erethon.dungeonsxl.event.dplayer.DPlayerLeaveDGroupEvent;

import me.blackvein.quests.util.Lang;

public class DungeonsListener implements Listener {
	@EventHandler
	public void onGroupCreate(DGroupCreateEvent event) {
		event.getCreator().sendMessage(ChatColor.YELLOW + Lang.get("questDungeonsCreate"));
	}
	
	@EventHandler
	public void onGroupDisbandEvent(DGroupDisbandEvent event) {
		event.getDisbander().sendMessage(ChatColor.RED + Lang.get("questDungeonsDisband"));
	}
	
	@EventHandler
	public void onPlayerJoinEvent(DPlayerJoinDGroupEvent event) {
		Player i = event.getDGroup().getCaptain();
		Player p = event.getDPlayer().getPlayer();
		if (i != null && p != null) {
			i.sendMessage(ChatColor.GREEN + Lang.get(i, "questDungeonsInvite").replace("<player>", p.getName()));
			p.sendMessage(ChatColor.GREEN + Lang.get(p, "questDungeonsJoin").replace("<player>", i.getName()));
		}
	}
	
	@EventHandler
	public void onPlayerLeaveEvent(DPlayerLeaveDGroupEvent event) {
		Player k = event.getDGroup().getCaptain();
		Player p = event.getDPlayer().getPlayer();
		if (k != null && p != null) {
			k.sendMessage(ChatColor.RED + Lang.get(k, "questDungeonsKicked").replace("<player>", k.getName()));
			p.sendMessage(ChatColor.RED + Lang.get(p, "questDungeonsLeave").replace("<player>", p.getName()));
		}
	}
}