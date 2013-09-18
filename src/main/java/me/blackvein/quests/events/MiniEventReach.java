package me.blackvein.quests.events;

import java.util.LinkedHashMap;
import java.util.Map;

import me.blackvein.quests.Quest;
import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;

import org.bukkit.Location;

public class MiniEventReach extends MiniEvent{

	private Location location;
	private int radius;

	public MiniEventReach(Quest quest, Map<String, Object> keys, String data) {
		super(keys);
		try {

			this.location = Quests.getLocation(data.split(":")[0]);
			this.radius = Integer.parseInt(data.split(":")[1]);
		} catch(Exception e) {
			location = null;
			radius = 0;
		}
	}

	public boolean execute(Quester quester, Location loc) {
		if (location == null) {
			return false;
		}

		if (loc.toVector().distance(location.toVector()) > radius) {
			return false;
		} else {
			return super.execute(quester);
		}
	}

}
