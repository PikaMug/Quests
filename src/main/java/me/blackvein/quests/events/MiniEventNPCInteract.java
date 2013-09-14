package me.blackvein.quests.events;

import java.util.Map;

import me.blackvein.quests.Quest;

public class MiniEventNPCInteract extends MiniEvent {
	
	private int npcId = -1;

	public MiniEventNPCInteract(Quest quest, Map<String, Object> map, String data) {
		super(map);
		
		try {
			npcId = Integer.parseInt(data);
		} catch (Exception e) {
			
		}
	}

}
