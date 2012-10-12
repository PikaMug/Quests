package me.blackvein.quests;

import java.io.File;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class Event {

    String name = "";
    
    String message = null;
    boolean clearInv = false;
    LinkedList<Location> explosions = new LinkedList<Location>();
    Map<Location, Effect> effects = new HashMap<Location, Effect>();
    Map<Material, Integer> items = new EnumMap<Material, Integer>(Material.class);
    World stormWorld = null;
    int stormDuration = 0;
    World thunderWorld = null;
    int thunderDuration = 0;

    LinkedList<Location> mobSpawnLocs = new LinkedList<Location>();
    LinkedList<EntityType> mobSpawnTypes = new LinkedList<EntityType>();
    LinkedList<Integer> mobSpawnAmounts = new LinkedList<Integer>();

    LinkedList<Location> lightningStrikes = new LinkedList<Location>();

    LinkedList<PotionEffect> potionEffects = new LinkedList<PotionEffect>();
    int hunger = 0;
    int saturation = 0;
    int health = 0;
    Location teleport;

    @Override
    public boolean equals(Object o){
        
        if(o instanceof Event){

            Event other = (Event) o;

            if(other.name.equals(name) == false)
                return false;

            if(other.message != null && message != null){
                if(other.message.equals(message) == false)
                    return false;
            }else if(other.message != null && message == null){
                return false;
            }else if(other.message == null && message != null)
                return false;

            if(other.clearInv != clearInv)
                return false;

            if(other.explosions.equals(explosions) == false)
                return false;

            if(other.effects.entrySet().equals(effects.entrySet()) == false)
                return false;

            if(other.items.entrySet().equals(items.entrySet()) == false)
                return false;

            if(other.stormWorld != null && stormWorld != null){
                if(other.stormWorld.equals(stormWorld) == false)
                    return false;
            }else if(other.stormWorld != null && stormWorld == null){
                return false;
            }else if(other.stormWorld == null && stormWorld != null)
                return false;

            if(other.stormDuration != stormDuration)
                return false;

            if(other.thunderWorld != null && thunderWorld != null){
                if(other.thunderWorld.equals(thunderWorld) == false)
                    return false;
            }else if(other.thunderWorld != null && thunderWorld == null){
                return false;
            }else if(other.thunderWorld == null && thunderWorld != null)
                return false;

            if(other.thunderDuration != thunderDuration)
                return false;

            if(other.mobSpawnLocs.equals(mobSpawnLocs) == false)
                return false;

            if(other.mobSpawnTypes.equals(mobSpawnTypes) == false)
                return false;

            if(other.mobSpawnAmounts.equals(mobSpawnAmounts) == false)
                return false;

            if(other.lightningStrikes.equals(lightningStrikes) == false)
                return false;

            if(other.potionEffects.equals(potionEffects) == false)
                return false;

            if(other.hunger != hunger)
                return false;

            if(other.saturation != saturation)
                return false;

            if(other.health != health)
                return false;

            if(other.teleport != null && teleport != null){
                if(other.teleport.equals(teleport) == false)
                    return false;
            }else if(other.teleport != null && teleport == null){
                return false;
            }else if(other.teleport == null && teleport != null)
                return false;

        }

        return true;
    }

    public void happen(Player player){

        if(message.isEmpty() == false)
            player.sendMessage(message);

        if(clearInv == true){
            player.getInventory().clear();
        }

        if(explosions.isEmpty() == false){

            for(Location l : explosions){

                l.getWorld().createExplosion(l, 4F, false);

            }

        }

        if(effects.isEmpty() == false){

            for(Location l : effects.keySet()){

                l.getWorld().playEffect(l, effects.get(l), 1);

            }

        }

        if(items.isEmpty() == false){

            for(Material m : items.keySet())
                Quests.addItem(player, new ItemStack(m, items.get(m)));

        }

        if(stormWorld != null){
            stormWorld.setStorm(true);
            stormWorld.setWeatherDuration(stormDuration);
        }

        if(thunderWorld != null){
            thunderWorld.setThundering(true);
            thunderWorld.setThunderDuration(thunderDuration);
        }

        if(mobSpawnLocs.isEmpty() == false){

            for(Location l : mobSpawnLocs){

                for(int i = 1; i <= mobSpawnAmounts.get(mobSpawnLocs.indexOf(l)); i++){

                    l.getWorld().spawnEntity(l, mobSpawnTypes.get(mobSpawnLocs.indexOf(l)));

                }

            }

        }

        if(lightningStrikes.isEmpty() == false){

            for(Location l : lightningStrikes){

                l.getWorld().strikeLightning(l);

            }

        }

        if(potionEffects.isEmpty() == false){

            for(PotionEffect p : potionEffects){

                player.addPotionEffect(p);

            }

        }

        if(hunger != 0){

            player.setExhaustion(hunger);

        }

        if(saturation != 0){

            player.setSaturation(saturation);

        }

        if(health != 0){

            player.setHealth(health);

        }

        if(teleport != null){

            player.teleport(teleport);

        }

    }

    public static Event getEvent(String name, Quests plugin, Quest quest){

        if(name == null || plugin == null || quest == null)
            return null;

        Event event = new Event();

        FileConfiguration data = new YamlConfiguration();
        try{
            data.load(new File(plugin.getDataFolder(), "events.yml"));
        }catch (Exception e){
            e.printStackTrace();
        }

        String eventKey = "events." + name + ".";

        event.name = name;
        if(data.contains(eventKey + "message"))
            event.message = plugin.parseString(data.getString(eventKey + "message"), quest);

        if(data.contains(eventKey + "clear-inventory")){

            if(data.isBoolean(eventKey + "clear-inventory"))
                event.clearInv = data.getBoolean(eventKey + "clear-inventory");
            else{
                Quests.printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "clear-inventory: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a true/false value!");
                return null;
            }

        }

        if(data.contains(eventKey + "explosions")){

            if(Quests.checkList(data.getList(eventKey + "explosions"), String.class)){

                for(String s : data.getStringList(eventKey + "explosions")){

                    Location loc = Quests.getLocation(s);

                    if(loc == null){
                        Quests.printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + loc + ChatColor.GOLD + " inside " + ChatColor.GREEN + "explosions: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not in proper location format!");
                        Quests.printSevere(ChatColor.GOLD + "[Quests] Proper location format is: \"WorldName x y z\"");
                        return null;
                    }

                    event.explosions.add(loc);

                }

            }else{
                Quests.printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "explosions: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a list of locations!");
                return null;
            }

        }

        if(data.contains(eventKey + "effects")){

            if(Quests.checkList(data.getList(eventKey + "effects"), String.class)){

                if(data.contains(eventKey + "effect-locations")){

                    if(Quests.checkList(data.getList(eventKey + "effect-locations"), String.class)){

                        List<String> effectList = data.getStringList(eventKey + "effects");
                        List<String> effectLocs = data.getStringList(eventKey + "effect-locations");

                        for(String s : effectList){

                            Effect effect = Quests.getEffect(s);
                            Location l = Quests.getLocation(effectLocs.get(effectList.indexOf(s)));

                            if(effect == null){
                                Quests.printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + s + ChatColor.GOLD + " inside " + ChatColor.GREEN + "effects: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a valid effect name!");
                                return null;
                            }

                            if(l == null){
                                Quests.printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + effectLocs.get(effectList.indexOf(s)) + ChatColor.GOLD + " inside " + ChatColor.GREEN + "effect-locations: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not in proper location format!");
                                Quests.printSevere(ChatColor.GOLD + "[Quests] Proper location format is: \"WorldName x y z\"");
                                return null;
                            }

                            event.effects.put(l, effect);

                        }

                    }else{
                        Quests.printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "effect-locations: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a list of locations!");
                        return null;
                    }

                }else{
                    Quests.printSevere(ChatColor.GOLD + "[Quests] Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is missing " + ChatColor.RED + "effect-locations:");
                    return null;
                }

            }else{
                Quests.printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "effects: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a list of effects!");
                return null;
            }
        }

        if(data.contains(eventKey + "item-ids")){

            if(Quests.checkList(data.getList(eventKey + "item-ids"), Integer.class)){

                if(data.contains(eventKey + "item-amounts")){

                    if(Quests.checkList(data.getList(eventKey + "item-amounts"), Integer.class)){

                        List<Material> itemIds =  new LinkedList<Material>();

                        for(Integer i : data.getIntegerList(eventKey + "item-ids")){
                            Material m = Material.getMaterial(i);
                            if(m != null)
                                itemIds.add(m);
                            else{
                                Quests.printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + i + ChatColor.GOLD + " inside " + ChatColor.GREEN + " item-ids: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a valid item id!");
                                return null;
                            }
                        }

                        List<Integer> itemAmounts = data.getIntegerList(eventKey + "item-amounts");

                        for(Material m : itemIds)
                            event.items.put(m, itemAmounts.get(itemIds.indexOf(m)));

                    }else{
                        Quests.printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "item-amounts: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a list of numbers!");
                        return null;
                    }

                }else{
                    Quests.printSevere(ChatColor.GOLD + "[Quests] Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is missing " + ChatColor.RED + "item-amounts:");
                    return null;
                }

            }else{
                Quests.printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "item-ids: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a list of item ids!");
                return null;
            }

        }

        if(data.contains(eventKey + "storm-world")){

            World w = plugin.getServer().getWorld(data.getString(eventKey + "storm-world"));

            if(w == null){
                Quests.printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "storm-world: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a valid World name!");
                return null;
            }

            if(data.contains(eventKey + "storm-duration")){

                if (data.getInt(eventKey + "storm-duration", -999) != -999) {
                    event.stormDuration = data.getInt(eventKey + "storm-duration");
                } else {
                    Quests.printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "storm-duration: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a number!");
                    return null;
                }

                event.stormWorld = w;

            }else{
                Quests.printSevere(ChatColor.GOLD + "[Quests] Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is missing " + ChatColor.RED + "storm-duration:");
                return null;
            }

        }

        if(data.contains(eventKey + "thunder-world")){

            World w = plugin.getServer().getWorld(data.getString(eventKey + "thunder-world"));

            if(w == null){
                Quests.printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "thunder-world: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a valid World name!");
                return null;
            }

            if(data.contains(eventKey + "thunder-duration")){

                if (data.getInt(eventKey + "thunder-duration", -999) != -999) {
                    event.thunderDuration = data.getInt(eventKey + "thunder-duration");
                } else {
                    Quests.printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "thunder-duration: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a number!");
                    return null;
                }

                event.thunderWorld = w;

            }else{
                Quests.printSevere(ChatColor.GOLD + "[Quests] Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is missing " + ChatColor.RED + "thunder-duration:");
                return null;
            }

        }

        if(data.contains(eventKey + "mob-spawn-locations")){

            if(Quests.checkList(data.getList(eventKey + "mob-spawn-locations"), String.class)){

                if(data.contains(eventKey + "mob-spawn-types")){

                    if(Quests.checkList(data.getList(eventKey + "mob-spawn-types"), String.class)){

                        if(data.contains(eventKey + "mob-spawn-amounts")){

                            if(Quests.checkList(data.getList(eventKey + "mob-spawn-amounts"), Integer.class)){

                                List<String> mobLocs = data.getStringList(eventKey + "mob-spawn-locations");
                                List<String> mobTypes = data.getStringList(eventKey + "mob-spawn-types");
                                List<Integer> mobAmounts = data.getIntegerList(eventKey + "mob-spawn-amounts");

                                for(String s : mobLocs){

                                    Location location = Quests.getLocation(s);
                                    if(location == null){
                                        Quests.printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + s + ChatColor.GOLD + " inside " + ChatColor.GREEN + " mob-spawn-locations: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not in proper location format!");
                                        Quests.printSevere(ChatColor.GOLD + "[Quests] Proper location format is: \"WorldName x y z\"");
                                        return null;
                                    }

                                    EntityType type = Quests.getMobType(mobTypes.get(mobLocs.indexOf(s)));
                                    if(type == null){
                                        Quests.printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + mobTypes.get(mobLocs.indexOf(s)) + ChatColor.GOLD + " inside " + ChatColor.GREEN + " mob-spawn-types: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a valid mob name!");
                                        return null;
                                    }

                                    int amount = mobAmounts.get(mobLocs.indexOf(s));

                                    event.mobSpawnLocs.add(location);
                                    event.mobSpawnTypes.add(type);
                                    event.mobSpawnAmounts.add(amount);

                                }

                            }else{
                                Quests.printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "mob-spawn-amounts: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a list of numbers!");
                                return null;
                            }

                        }else{
                            Quests.printSevere(ChatColor.GOLD + "[Quests] Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is missing " + ChatColor.RED + "mob-spawn-amounts:");
                            return null;
                        }

                    }else{
                        Quests.printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "mob-spawn-types: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a list of mob names!");
                        return null;
                    }

                }else{
                    Quests.printSevere(ChatColor.GOLD + "[Quests] Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is missing " + ChatColor.RED + "mob-spawn-types:");
                    return null;
                }

            }else{
                Quests.printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "mob-spawn-locations: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a list of locations!");
                return null;
            }

        }

        if(data.contains(eventKey + "lightning-strikes")){

            if(Quests.checkList(data.getList(eventKey + "lightning-strikes"), String.class)){

                for(String s : data.getStringList(eventKey + "lightning-strikes")){

                    Location loc = Quests.getLocation(s);
                    if(loc == null){
                        Quests.printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + s + ChatColor.GOLD + " inside " + ChatColor.GREEN + " lightning-strikes: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not in proper location format!");
                        Quests.printSevere(ChatColor.GOLD + "[Quests] Proper location format is: \"WorldName x y z\"");
                        return null;
                    }
                    event.lightningStrikes.add(loc);

                }

            }else{
                Quests.printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "lightning-strikes: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a list of locations!");
                return null;
            }

        }

        if(data.contains(eventKey + "potion-effect-types")){

            if(Quests.checkList(data.getList(eventKey + "potion-effect-types"), String.class)){

                if(data.contains(eventKey + "potion-effect-durations")){

                    if(Quests.checkList(data.getList(eventKey + "potion-effect-durations"), Integer.class)){

                        if(data.contains(eventKey + "potion-effect-amplifiers")){

                            if(Quests.checkList(data.getList(eventKey + "potion-effect-amplifiers"), Integer.class)){

                                List<String> types = data.getStringList(eventKey + "potion-effect-types");
                                List<Integer> durations = data.getIntegerList(eventKey + "potion-effect-durations");
                                List<Integer> amplifiers = data.getIntegerList(eventKey + "potion-effect-amplifiers");

                                for(String s : types){

                                    PotionEffect effect = Quests.getPotionEffect(s, durations.get(types.indexOf(s)), amplifiers.get(types.indexOf(s)));
                                    if(effect == null){
                                        Quests.printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + s + ChatColor.GOLD + " inside " + ChatColor.GREEN + " lightning-strikes: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a valid potion effect name!");
                                        return null;
                                    }
                                    event.potionEffects.add(effect);

                                }

                            }else{
                                Quests.printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "potion-effect-amplifiers: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a list of numbers!");
                                return null;
                            }

                        }else{
                            Quests.printSevere(ChatColor.GOLD + "[Quests] Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is missing " + ChatColor.RED + "potion-effect-amplifiers:");
                            return null;
                        }

                    }else{
                        Quests.printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "potion-effect-durations: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a list of numbers!");
                        return null;
                    }

                }else{
                    Quests.printSevere(ChatColor.GOLD + "[Quests] Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is missing " + ChatColor.RED + "potion-effect-durations:");
                    return null;
                }

            }else{
                Quests.printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "potion-effect-types: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a list of potion effects!");
                return null;
            }


        }

        if(data.contains(eventKey + "hunger")){

            if (data.getInt(eventKey + "hunger", -999) != -999) {
                event.hunger = data.getInt(eventKey + "hunger");
            } else {
                Quests.printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "hunger: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a number!");
                return null;
            }

        }

        if(data.contains(eventKey + "saturation")){

            if (data.getInt(eventKey + "saturation", -999) != -999) {
                event.saturation = data.getInt(eventKey + "saturation");
            } else {
                Quests.printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "saturation: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a number!");
                return null;
            }

        }

        if(data.contains(eventKey + "health")){

            if (data.getInt(eventKey + "health", -999) != -999) {
                event.health = data.getInt(eventKey + "health");
            } else {
                Quests.printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "health: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a number!");
                return null;
            }

        }

        if(data.contains(eventKey + "teleport-location")){

            if(data.isString(eventKey + "teleport-location")){

                Location l = Quests.getLocation(data.getString(eventKey + "teleport-location"));
                if(l == null){
                    Quests.printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + data.getString(eventKey + "teleport-location") + ChatColor.GOLD + "for " + ChatColor.GREEN + " teleport-location: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not in proper location format!");
                    Quests.printSevere(ChatColor.GOLD + "[Quests] Proper location format is: \"WorldName x y z\"");
                    return null;
                }
                event.teleport = l;

            }else{
                Quests.printSevere(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "teleport-location: " + ChatColor.GOLD + "inside Event " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a location!");
                return null;
            }

        }

        return event;

    }

}
