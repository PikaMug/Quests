package me.blackvein.quests;

import java.io.File;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

        if(data.contains(eventKey + "clear-inventory"))
            event.clearInv = data.getBoolean(eventKey + "clear-inventory");

        if(data.contains(eventKey + "explosions")){

            for(String s : data.getStringList(eventKey + "explosions")){

                Location loc = Quests.getLocation(s);

                event.explosions.add(loc);

            }

        }

        if(data.contains(eventKey + "effects")){

            List<String> effectList = data.getStringList(eventKey + "effects");
            List<String> effectLocs = data.getStringList(eventKey + "effect-locations");

            for(String s : effectLocs)
                event.effects.put(Quests.getLocation(s), Quests.getEffect(effectList.get(effectLocs.indexOf(s))));
        }

        if(data.contains(eventKey + "item-ids")){

            List<Material> itemIds =  new LinkedList<Material>();

            for(Integer i : data.getIntegerList(eventKey + "item-ids"))
                itemIds.add(Material.getMaterial(i));

            List<Integer> itemAmounts = data.getIntegerList(eventKey + "item-amounts");

            for(Material m : itemIds)
                event.items.put(m, itemAmounts.get(itemIds.indexOf(m)));

        }

        if(data.contains(eventKey + "storm-world")){
            event.stormDuration = data.getInt(eventKey + "storm-duration");
            event.stormWorld = plugin.getServer().getWorld(data.getString(eventKey + "storm-world"));
        }

        if(data.contains(eventKey + "thunder-world")){
            event.thunderDuration = data.getInt(eventKey + "thunder-duration");
            event.thunderWorld = plugin.getServer().getWorld(data.getString(eventKey + "thunder-world"));
        }

        if(data.contains(eventKey + "mob-spawn-locations")){

            List<String> mobLocs = data.getStringList(eventKey + "mob-spawn-locations");
            List<String> mobTypes = data.getStringList(eventKey + "mob-spawn-types");
            List<Integer> mobAmounts = data.getIntegerList(eventKey + "mob-spawn-amounts");

            for(String s : mobLocs){

                Location location = Quests.getLocation(s);
                EntityType type = Quests.getMobType(mobTypes.get(mobLocs.indexOf(s)));
                int amount = mobAmounts.get(mobLocs.indexOf(s));

                event.mobSpawnLocs.add(location);
                event.mobSpawnTypes.add(type);
                event.mobSpawnAmounts.add(amount);

            }

        }

        if(data.contains(eventKey + "lightning-strikes")){

            for(String s : data.getStringList(eventKey + "lightning-strikes")){

                Location loc = Quests.getLocation(s);
                event.lightningStrikes.add(loc);

            }

        }

        if(data.contains(eventKey + "potion-effect-types")){

            List<String> types = data.getStringList(eventKey + "potion-effect-types");
            List<Integer> durations = data.getIntegerList(eventKey + "potion-effect-durations");
            List<Integer> amplifiers = data.getIntegerList(eventKey + "potion-effect-amplifiers");

            for(String s : types){

                PotionEffect effect = Quests.getPotionEffect(s, durations.get(types.indexOf(s)), amplifiers.get(types.indexOf(s)));
                event.potionEffects.add(effect);

            }


        }

        if(data.contains(eventKey + "hunger")){

            event.hunger = data.getInt(eventKey + "hunger");

        }

        if(data.contains(eventKey + "saturation")){

            event.saturation = data.getInt(eventKey + "saturation");

        }

        if(data.contains(eventKey + "health")){

            event.health = data.getInt(eventKey + "health");

        }

        if(data.contains(eventKey + "teleport-location")){

            event.teleport = Quests.getLocation(data.getString(eventKey + "teleport-location"));

        }

        return event;

    }

}
