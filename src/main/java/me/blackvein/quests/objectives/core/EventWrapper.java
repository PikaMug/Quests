package me.blackvein.quests.objectives.core;

import org.bukkit.event.Event;

public class EventWrapper {
	
	
	private Event event;

	public EventWrapper(Event event) {
		this.event = event;
	}

	public Event getEvent() {
		return event;
	}
}
