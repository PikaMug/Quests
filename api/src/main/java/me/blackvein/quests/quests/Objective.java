package me.blackvein.quests.quests;

import me.blackvein.quests.enums.ObjectiveType;
import org.bukkit.inventory.ItemStack;

public interface Objective {
    ObjectiveType getType();

    int getProgress();

    int getGoal();

    ItemStack getItemProgress();

    ItemStack getItemGoal();
}
