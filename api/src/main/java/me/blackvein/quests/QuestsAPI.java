package me.blackvein.quests;

import org.bukkit.plugin.Plugin;

import java.util.List;

public interface QuestsAPI extends Plugin {

    List<CustomObjective> getCustomObjectives();

    List<CustomReward> getCustomRewards();

    List<CustomRequirement> getCustomRequirements();
}
