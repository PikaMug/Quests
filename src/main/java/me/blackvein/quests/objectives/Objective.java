package me.blackvein.quests.objectives;

import java.util.List;

import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;
import me.blackvein.quests.Stage;
import me.blackvein.quests.exceptions.InvalidStageException;
import me.blackvein.quests.objectives.core.ObjectiveType;

import org.bukkit.configuration.ConfigurationSection;

public abstract class Objective {

	public abstract boolean isFinished(Quester quester);
	public abstract void setObjectives(Quester quester, Stage stage);
	public abstract void initiageStage(Stage stage, ConfigurationSection section) throws InvalidStageException;
	public abstract ObjectiveType getType();
	public abstract List<String> getObjectiveMessages(Quester quester);
	
	public void printSevere(String string) {
		Quests.log.severe(string);
	}
	
}
