package me.blackvein.quests.events;

import java.util.LinkedHashMap;
import java.util.Map;

import me.blackvein.quests.Quest;

public class MiniEventChat extends MiniEvent {

	private String chatString;

	public MiniEventChat(Quest quest, Map<String, Object> keys, String data) {
		super(keys);
		this.chatString = data;
	}

}
