package me.blackvein.quests;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;

public abstract class CustomRequirement {
    
    private String name = null;
    private String author = null;
    public final Map<String,Object> datamap = new HashMap<String, Object>();
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
    
    public void setData(String key, Object val) {
        datamap.put(key, val);
    }
    
    public void setDescription(String key, String description){
        descriptions.put(key, description);
    }
    
}
