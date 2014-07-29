package me.blackvein.quests;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;

public abstract class CustomRequirement {

    private String name = null;
    private String author = null;
    public final Map<String, Object> datamap = new HashMap<String, Object>();
    public final Map<String, String> descriptions = new HashMap<String, String>();

    public abstract boolean testRequirement(Player p, Map<String, Object> m);

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

    public void addData(String name) {
        datamap.put(name, null);
    }

    public void addDescription(String data, String description) {
        descriptions.put(data, description);
    }

}
