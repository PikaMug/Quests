package me.blackvein.quests.events;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import me.blackvein.quests.Quest;
import me.blackvein.quests.Quester;

import org.bukkit.entity.EntityType;

public class MiniEventKill extends MiniEvent {

	private List<String> mobTypes;

	public MiniEventKill(Quest quest, Map<String, Object> keys, String mobs) {
		super(keys);
		this.mobTypes = Arrays.asList(mobs.split(","));
	}
	
	public boolean execute(Quester quester, EntityType type) {
		if (mobTypes.contains(Quester.prettyMobString(type)) || mobTypes.isEmpty()) {
			return super.execute(quester);
		} else {
			return true;
		}
	}

}
