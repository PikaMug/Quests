package me.blackvein.quests.objectives.core;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map.Entry;

import me.blackvein.quests.Quester;
import me.blackvein.quests.Stage;
import me.blackvein.quests.exceptions.InvalidStageException;
import me.blackvein.quests.objectives.Objective;
import me.blackvein.quests.objectives.ObjectiveBreakBlock;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Event;

public class ObjectiveHandler {
	
	private static HashMap<Class<? extends Event>, HashMap<Class<? extends Objective>, Method>> eventMap = new HashMap<Class<? extends Event>, HashMap<Class<? extends Objective>, Method>>();
	private static HashMap<Class<? extends Objective>, Objective> objectives = new HashMap<Class<? extends Objective>, Objective>();
	
	public ObjectiveHandler() {
		this.registerClasses();
	}
	
	public static void executeEvent(Event event) {
		if (eventMap.containsKey(event.getClass())) {
			EventWrapper wrapper = new EventWrapper(event);
			for (Entry<Class<? extends Objective>, Method> entry : eventMap.get(event.getClass()).entrySet()) {
				Objective objective = objectives.get(entry.getKey());
				try {
					entry.getValue().invoke(objective, new Object[] {wrapper});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static Objective getObjective(ObjectiveType type) {
		return objectives.get(type);
	}
	
	public static void initStage(Stage oStage, ConfigurationSection section) {
		for (Objective ob : objectives.values()) {
			try {
				ob.initiageStage(oStage, section);
			} catch (InvalidStageException e) {
				return;
			}
		}
	}
	
	public void registerClasses() {
		try {
			registerClass(ObjectiveBreakBlock.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void registerClass(Class<? extends Objective> module) throws InstantiationException, IllegalAccessException {
		
		objectives.put(module, module.newInstance());
		
		for (Method method : module.getMethods()) {
			if (method.isAnnotationPresent(ObjectiveEvent.class)) {
				
				ObjectiveEvent objectiveEvent = method.getAnnotation(ObjectiveEvent.class);
				Class<? extends Event> eventClass = objectiveEvent.value();
				
				if (!eventMap.containsKey(eventClass)) {
					HashMap<Class<? extends Objective>, Method> tempMap = new HashMap<Class<? extends Objective>, Method>();
					eventMap.put(eventClass, tempMap);
				}
				eventMap.get(eventClass).put(module, method);			
			}
		}
	}
}
