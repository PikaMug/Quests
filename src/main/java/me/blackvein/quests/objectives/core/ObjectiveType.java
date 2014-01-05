package me.blackvein.quests.objectives.core;

import me.blackvein.quests.objectives.Objective;
import me.blackvein.quests.objectives.ObjectiveBreakBlock;
import me.blackvein.quests.objectives.ObjectiveDamageBlock;

public enum ObjectiveType {
	
	BREAK_BLOCK(ObjectiveBreakBlock.class), 
	DAMAGE_BLOCK(ObjectiveDamageBlock.class);
	
	private Class<? extends Objective> type;
	
	ObjectiveType(Class<? extends Objective> type) {
		this.type = type;
	}
	
	public Class<? extends Objective> getType() {
		return type;
	}

}
