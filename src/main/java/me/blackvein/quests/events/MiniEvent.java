package me.blackvein.quests.events;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import me.blackvein.quests.ItemUtil;
import me.blackvein.quests.Quest;
import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class MiniEvent {

	protected boolean cancelled = false;
	protected Map<String, Object> values = new LinkedHashMap<String, Object>();
	
	public MiniEvent(Map<String, Object> map) {
		if (map != null) {
			this.values = map;
		}
	}
	

	public boolean execute(Quester quester) {
		
		if (quester == null) {
			return true;
		}
		for (Entry<String, Object> ent : values.entrySet()) {
			String key = ent.getKey();
			String value = (String)ent.getValue();
			
			switch(key) {
			case "cancel":
				setCancelled(value);
				break;
			case "command":
				executeCommand(value);
				break;
			case "send-message":
				sendMessage(value, quester);
				break;
			case "chat-message":
				chatMessage(value, quester);
				break;
			case "broadcast-message":
				broadcastMessage(value);
				break;
			case "set-stage":
				setStage(value, quester);
				break;
			case "teleport":
				teleport(value, quester);
				break;
			case "take-item":
				takeItem(value, quester);
				break;
			case "add-item":
				addItem(value, quester);
				break;
			}
		}
		
		return isCancelled();
	}

	public void setCancelled(String data) {
		Boolean c = false;
		try {
			c = Boolean.parseBoolean(data);
		} catch (Exception e) {
			
		}
		cancelled = c;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void executeCommand(String command) {
		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
	}

	public void sendMessage(String message, Quester quester) {
		quester.getPlayer().sendMessage(message);
	}

	public void chatMessage(String message, Quester quester) {
		quester.getPlayer().chat(message);
	}

	public void broadcastMessage(String message) {
		Bukkit.getServer().broadcastMessage(message);
	}

	public void setStage(String data, Quester quester) {
		int i = -1;
		
		try {
			i = Integer.parseInt(data);
		} catch (Exception e) {
			return;
		}
		
		if (i > 0)
			quester.currentQuest.setStage(quester, i - 1);
	}
	
	public void teleport(String data, Quester quester) {
		Location loc = Quests.getLocation(data);
		if (loc != null) {
			quester.getPlayer().teleport(loc);
		}
	}
	
	public void takeItem(String data, Quester quester) {
		ItemStack is = ItemUtil.parseItem(data);
		PlayerInventory inv = quester.getPlayer().getInventory();
		
		Quests.removeItem(inv, is);
	}
	
	public void addItem(String data, Quester quester) {
		ItemStack is = ItemUtil.parseItem(data);
		Quests.addItem(quester.getPlayer(), is);
	}
	
	public enum MiniEventType {
		ONDEATH ("onDeath"),
		ONKILL ("onKill"),
		ONREACH("onReach"),
		ONCHAT("onChat"),
		ONSTAGEEND("onStageEnd"),
		ONQUESTQUIT("onQuestQuit"),
		ONNPCINTERACT("OnNPCInteract");
		
		private String name;

		MiniEventType(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
	}

	public Map<?, ?> getValues() {
		return values;
	}
}
