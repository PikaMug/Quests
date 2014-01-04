package me.blackvein.quests;

import org.bukkit.entity.Player;

public abstract class CustomRequirement {
    
    private String name = null;
    private String author = null;
    
    public abstract boolean testRequirement(Player p);

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
    
}
