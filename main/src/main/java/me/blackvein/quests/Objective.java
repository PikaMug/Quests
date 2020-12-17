package me.blackvein.quests;

import org.bukkit.inventory.ItemStack;

import me.blackvein.quests.enums.ObjectiveType;

public class Objective {
    private final ObjectiveType type;
    private final int progress;
    private final int goal;
    private final ItemStack progressStack;
    private final ItemStack goalStack;
    
    
    public Objective(final ObjectiveType type, final int progress, final int goal) {
        this.type = type;
        this.progress = progress;
        this.goal = goal;
        this.progressStack = null;
        this.goalStack = null;
    }
    
    public Objective(final ObjectiveType type, final ItemStack progress, final ItemStack goal) {
        this.type = type;
        this.progress = progress.getAmount();
        this.goal = goal.getAmount();
        this.progressStack = progress;
        this.goalStack = goal;
    }
    
    public ObjectiveType getType() {
        return type;
    }
    
    public int getProgress() {
        return progress;
    }
    
    public int getGoal() {
        return goal;
    }
    
    public ItemStack getItemProgress() {
        return progressStack;
    }
    
    public ItemStack getItemGoal() {
        return goalStack;
    }
}
