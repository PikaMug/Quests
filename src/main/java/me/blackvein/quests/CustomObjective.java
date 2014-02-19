package me.blackvein.quests;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public abstract class CustomObjective implements Listener {
    
    private String name = null;
    private String author = null;
    public final Map<String,Object> datamap = new HashMap<String, Object>();
    public final Map<String, String> descriptions = new HashMap<String, String>();
    private String countPrompt = "null";
    private String display = "null";
    private boolean enableCount = true;
    private boolean showCount = true;
    private int count = 1;
    
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
    
    public void addDescription(String data, String description){
        descriptions.put(data, description);
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getCountPrompt() {
        return countPrompt;
    }

    public void setCountPrompt(String countPrompt) {
        this.countPrompt = countPrompt;
    }

    public boolean isCountShown() {
        return showCount;
    }

    public void setShowCount(boolean showCount) {
        this.showCount = showCount;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public boolean isEnableCount() {
        return enableCount;
    }

    public void setEnableCount(boolean enableCount) {
        this.enableCount = enableCount;
    }
    
    public Map<String, Object> getDatamap(Player player, CustomObjective obj){
        
        Quester quester = Quests.getInstance().getQuester(player.getName());
        if(quester != null){
            
            int index = -1;
            int tempIndex = 0;
            
            for(me.blackvein.quests.CustomObjective co : quester.currentStage.customObjectives){
                
                if(co.getName().equals(obj.getName())){
                    index = tempIndex;
                    break;
                }
                
                tempIndex++;
                
            }
            
            if(index > -1){
                
                return quester.currentStage.customObjectiveData.get(index);
                
            }
        
        }
        
        return null;
        
    }
    
    public static void incrementObjective(Player player, CustomObjective obj, int count){
        
        Quester quester = Quests.getInstance().getQuester(player.getName());
        if(quester != null){
        
            //Check if the player has Quest with objective
            
            boolean hasQuest = false;
            
            for(CustomObjective co : quester.currentStage.customObjectives){
                
                if(co.getName().equals(obj.getName())){
                    hasQuest = true;
                    break;
                }
                
            }
            
            if(hasQuest && quester.hasCustomObjective(obj.getName())){
                
                if(quester.customObjectiveCounts.containsKey(obj.getName())){
                    int old = quester.customObjectiveCounts.get(obj.getName());
                    Quests.getInstance().getQuester(player.getName()).customObjectiveCounts.put(obj.getName(), old + count);
                }else{
                    Quests.getInstance().getQuester(player.getName()).customObjectiveCounts.put(obj.getName(), count);
                }

                int index = -1;
                for(int i = 0; i < quester.currentStage.customObjectives.size(); i++){
                    if(quester.currentStage.customObjectives.get(i).getName().equals(obj.getName())){
                        index = i;
                        break;
                    }
                }

                if(index > -1){

                    if(quester.customObjectiveCounts.get(obj.getName()) >= quester.currentStage.customObjectiveCounts.get(index)){
                       quester.finishObjective("customObj", null, null, null, null, null, null, null, null, null, obj);
                    }

                }
            
            }
        
        }
        
    }
    
    @Override
    public boolean equals(Object o){
        
        if(o instanceof CustomObjective){
            
            CustomObjective other = (CustomObjective) o;
            
            if(other.name.equals(name) == false)
                return false;
            
            if(other.author.equals(name) == false)
                return false;
            
            for(String s : other.datamap.keySet()){
                if(datamap.containsKey(s) == false)
                    return false;
            }
            
            for(Object val : other.datamap.values()){
                if(datamap.containsValue(val) == false)
                    return false;
            }
            
            for(String s : other.descriptions.keySet()){
                if(descriptions.containsKey(s) == false)
                    return false;
            }
            
            for(String s : other.descriptions.values()){
                if(descriptions.containsValue(s) == false)
                    return false;
            }
            
            if(other.countPrompt.equals(countPrompt) == false)
                return false;
            
            if(other.display.equals(display) == false)
                return false;
            
            if(other.enableCount != enableCount)
                return false;
            
            if(other.showCount != showCount)
                return false;
            
            if(other.count != count)
                return false;
            
        }
        
        return true;
    }
    
}
