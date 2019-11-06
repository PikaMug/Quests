/*******************************************************************************************************
 * Continued by PikaMug (formerly HappyPikachu) with permission from _Blackvein_. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

package me.blackvein.quests.actions;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.blackvein.quests.Quest;
import me.blackvein.quests.QuestMob;
import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;
import me.blackvein.quests.timers.ActionTimer;
import me.blackvein.quests.util.ConfigUtil;
import me.blackvein.quests.util.InventoryUtil;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;

public class Action {

    private Quests plugin;
    private String name = "";
    protected String message = null;
    protected boolean clearInv = false;
    protected boolean failQuest = false;
    protected LinkedList<Location> explosions = new LinkedList<Location>();
    protected Map<Location, Effect> effects = new HashMap<Location, Effect>();
    protected LinkedList<ItemStack> items = new LinkedList<ItemStack>();
    protected World stormWorld = null;
    protected int stormDuration = 0;
    protected World thunderWorld = null;
    protected int thunderDuration = 0;
    protected int timer = 0;
    protected boolean cancelTimer = false;
    protected LinkedList<QuestMob> mobSpawns = new LinkedList<QuestMob>() {

        private static final long serialVersionUID = -761974607799449780L;

        @Override
        public boolean equals(Object o) {
            if (o instanceof LinkedList) {
                @SuppressWarnings("unchecked")
                LinkedList<QuestMob> other = (LinkedList<QuestMob>) o;
                if (size() != other.size()) {
                    return false;
                }
                for (int i = 0; i < size(); i++) {
                    if (get(i).equals(other.get(i)) == false) {
                        return false;
                    }
                }
            }
            return false;
        }
    };
    protected LinkedList<Location> lightningStrikes = new LinkedList<Location>();
    protected LinkedList<String> commands = new LinkedList<String>();
    protected LinkedList<PotionEffect> potionEffects = new LinkedList<PotionEffect>();
    protected int hunger = -1;
    protected int saturation = -1;
    protected float health = -1;
    protected Location teleport;
    protected String book = "";

    public Action(final Quests plugin) {
        this.plugin = plugin;
    }

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isClearInv() {
        return clearInv;
    }

    public void setClearInv(boolean clearInv) {
        this.clearInv = clearInv;
    }

    public boolean isFailQuest() {
        return failQuest;
    }

    public void setFailQuest(boolean failQuest) {
        this.failQuest = failQuest;
    }

    public LinkedList<Location> getExplosions() {
        return explosions;
    }

    public void setExplosions(LinkedList<Location> explosions) {
        this.explosions = explosions;
    }

    public Map<Location, Effect> getEffects() {
        return effects;
    }

    public void setEffects(Map<Location, Effect> effects) {
        this.effects = effects;
    }

    public LinkedList<ItemStack> getItems() {
        return items;
    }

    public void setItems(LinkedList<ItemStack> items) {
        this.items = items;
    }

    public World getStormWorld() {
        return stormWorld;
    }

    public void setStormWorld(World stormWorld) {
        this.stormWorld = stormWorld;
    }

    public int getStormDuration() {
        return stormDuration;
    }

    public void setStormDuration(int stormDuration) {
        this.stormDuration = stormDuration;
    }

    public World getThunderWorld() {
        return thunderWorld;
    }

    public void setThunderWorld(World thunderWorld) {
        this.thunderWorld = thunderWorld;
    }

    public int getThunderDuration() {
        return thunderDuration;
    }

    public void setThunderDuration(int thunderDuration) {
        this.thunderDuration = thunderDuration;
    }

    public int getTimer() {
        return timer;
    }

    public void setTimer(int timer) {
        this.timer = timer;
    }

    public boolean isCancelTimer() {
        return cancelTimer;
    }

    public void setCancelTimer(boolean cancelTimer) {
        this.cancelTimer = cancelTimer;
    }

    public LinkedList<QuestMob> getMobSpawns() {
        return mobSpawns;
    }

    public void setMobSpawns(LinkedList<QuestMob> mobSpawns) {
        this.mobSpawns = mobSpawns;
    }

    public LinkedList<Location> getLightningStrikes() {
        return lightningStrikes;
    }

    public void setLightningStrikes(LinkedList<Location> lightningStrikes) {
        this.lightningStrikes = lightningStrikes;
    }

    public LinkedList<String> getCommands() {
        return commands;
    }

    public void setCommands(LinkedList<String> commands) {
        this.commands = commands;
    }

    public LinkedList<PotionEffect> getPotionEffects() {
        return potionEffects;
    }

    public void setPotionEffects(LinkedList<PotionEffect> potionEffects) {
        this.potionEffects = potionEffects;
    }

    public int getHunger() {
        return hunger;
    }

    public void setHunger(int hunger) {
        this.hunger = hunger;
    }

    public int getSaturation() {
        return saturation;
    }

    public void setSaturation(int saturation) {
        this.saturation = saturation;
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = health;
    }

    public Location getTeleport() {
        return teleport;
    }

    public void setTeleport(Location teleport) {
        this.teleport = teleport;
    }

    public String getBook() {
        return book;
    }

    public void setBook(String book) {
        this.book = book;
    }

    public void fire(Quester quester, Quest quest) {
        Player player = quester.getPlayer();
        if (message != null) {
            player.sendMessage(ConfigUtil.parseStringWithPossibleLineBreaks(message, quest, player));
        }
        if (clearInv == true) {
            player.getInventory().clear();
        }
        if (explosions.isEmpty() == false) {
            for (Location l : explosions) {
                l.getWorld().createExplosion(l, 4F, false);
            }
        }
        if (effects.isEmpty() == false) {
            for (Location l : effects.keySet()) {
                l.getWorld().playEffect(l, effects.get(l), 1);
            }
        }
        if (items.isEmpty() == false) {
            for (ItemStack is : items) {
                try {
                    InventoryUtil.addItem(player, is);
                } catch (Exception e) {
                    plugin.getLogger().severe("Unable to add null item to inventory of " 
                            + player.getName() + " during quest " + quest.getName() + " event " + name);
                    player.sendMessage(ChatColor.RED + "Quests encountered a problem with an item. "
                            + "Please contact an administrator.");
                }
            }
        }
        if (stormWorld != null) {
            stormWorld.setStorm(true);
            stormWorld.setWeatherDuration(stormDuration);
        }
        if (thunderWorld != null) {
            thunderWorld.setThundering(true);
            thunderWorld.setThunderDuration(thunderDuration);
        }
        if (mobSpawns.isEmpty() == false) {
            for (QuestMob questMob : mobSpawns) {
                questMob.spawn();
            }
        }
        if (lightningStrikes.isEmpty() == false) {
            for (Location l : lightningStrikes) {
                l.getWorld().strikeLightning(l);
            }
        }
        if (commands.isEmpty() == false) {
            for (String s : commands) {
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), 
                        s.replaceAll("<player>", quester.getPlayer().getName()));
            }
        }
        if (potionEffects.isEmpty() == false) {
            for (PotionEffect p : potionEffects) {
                player.addPotionEffect(p);
            }
        }
        if (hunger != -1) {
            player.setFoodLevel(hunger);
        }
        if (saturation != -1) {
            player.setSaturation(saturation);
        }
        if (health != -1) {
            player.setHealth(health);
        }
        if (teleport != null) {
            player.teleport(teleport);
        }
        if (book != null) {
            if (!book.isEmpty()) {
                if (plugin.getDependencies().getCitizensBooksApi() != null) {
                    if (plugin.getDependencies().getCitizensBooksApi().hasFilter(book)) {
                        plugin.getDependencies().getCitizensBooksApi().openBook(player, plugin.getDependencies()
                                .getCitizensBooksApi().getFilter(book));
                    }
                }
            }
        }
        if (failQuest == true) {
            quest.failQuest(quester);
        }
        if (timer > 0) {
            player.sendMessage(Lang.get(player, "timerStart").replaceAll("<time>", String.valueOf(timer)));
            if (timer > 60) {
                quester.getTimers().put(new ActionTimer(quester, quest, 60, false)
                        .runTaskLater(plugin, (timer-60)*20).getTaskId(), quest);
            }
            if (timer > 30) {
                quester.getTimers().put(new ActionTimer(quester, quest, 30, false)
                        .runTaskLater(plugin, (timer-30)*20).getTaskId(), quest);
            }
            if (timer > 10) {
                quester.getTimers().put(new ActionTimer(quester, quest, 10, false)
                        .runTaskLater(plugin, (timer-10)*20).getTaskId(), quest);
            }
            if (timer > 5) {
                quester.getTimers().put(new ActionTimer(quester, quest, 5, false)
                        .runTaskLater(plugin, (timer-5)*20).getTaskId(), quest);
            }
            if (timer > 4) {
                quester.getTimers().put(new ActionTimer(quester, quest, 4, false)
                        .runTaskLater(plugin, (timer-4)*20).getTaskId(), quest);
            }
            if (timer > 3) {
                quester.getTimers().put(new ActionTimer(quester, quest, 3, false)
                        .runTaskLater(plugin, (timer-3)*20).getTaskId(), quest);
            }
            if (timer > 2) {
                quester.getTimers().put(new ActionTimer(quester, quest, 2, false)
                        .runTaskLater(plugin, (timer-2)*20).getTaskId(), quest);
            }
            if (timer > 1) {
                quester.getTimers().put(new ActionTimer(quester, quest, 1, false)
                        .runTaskLater(plugin, (timer-1)*20).getTaskId(), quest);
            }
            quester.getTimers().put(new ActionTimer(quester, quest, 0, true)
                    .runTaskLater(plugin, timer*20).getTaskId(), quest);
        }
        if (cancelTimer) {
            for (Map.Entry<Integer, Quest> entry : quester.getTimers().entrySet()) {
                if (entry.getValue().getName().equals(quest.getName())) {
                    plugin.getServer().getScheduler().cancelTask(entry.getKey());
                    quester.getTimers().remove(entry.getKey());
                }
            }
        }
    }

    public static Action loadAction(String name, Quests plugin) {
        if (name == null || plugin == null) {
            return null;
        }
        File legacy = new File(plugin.getDataFolder(), "events.yml");
        File actions = new File(plugin.getDataFolder(), "actions.yml");
        FileConfiguration data = new YamlConfiguration();
        try {
            if (actions.isFile()) {
                data.load(actions);
            } else {
                data.load(legacy);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
        String legacyName = "events." + name;
        String actionKey = "actions." + name + ".";
        if (data.contains(legacyName)) {
            actionKey = legacyName + ".";
        }
        Action action = new Action(plugin);
        action.name = name;
        if (data.contains(actionKey + "message")) {
            action.message = ConfigUtil.parseString(data.getString(actionKey + "message"));
        }
        if (data.contains(actionKey + "open-book")) {
            action.book = data.getString(actionKey + "open-book");
        }
        if (data.contains(actionKey + "clear-inventory")) {
            if (data.isBoolean(actionKey + "clear-inventory")) {
                action.clearInv = data.getBoolean(actionKey + "clear-inventory");
            } else {
                plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "clear-inventory: " 
                        + ChatColor.GOLD + "inside Action " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD 
                        + " is not a true/false value!");
                return null;
            }
        }
        if (data.contains(actionKey + "fail-quest")) {
            if (data.isBoolean(actionKey + "fail-quest")) {
                action.failQuest = data.getBoolean(actionKey + "fail-quest");
            } else {
                plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "fail-quest: " 
                        + ChatColor.GOLD + "inside Action " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD 
                        + " is not a true/false value!");
                return null;
            }
        }
        if (data.contains(actionKey + "explosions")) {
            if (ConfigUtil.checkList(data.getList(actionKey + "explosions"), String.class)) {
                for (String s : data.getStringList(actionKey + "explosions")) {
                    Location loc = ConfigUtil.getLocation(s);
                    if (loc == null) {
                        plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + loc + ChatColor.GOLD 
                                + " inside " + ChatColor.GREEN + "explosions: " + ChatColor.GOLD + "inside Action " 
                                + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not in proper location format!");
                        plugin.getLogger().severe(ChatColor.GOLD 
                                + "[Quests] Proper location format is: \"WorldName x y z\"");
                        return null;
                    }
                    action.explosions.add(loc);
                }
            } else {
                plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "explosions: " 
                        + ChatColor.GOLD + "inside Action " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD 
                        + " is not a list of locations!");
                return null;
            }
        }
        if (data.contains(actionKey + "effects")) {
            if (ConfigUtil.checkList(data.getList(actionKey + "effects"), String.class)) {
                if (data.contains(actionKey + "effect-locations")) {
                    if (ConfigUtil.checkList(data.getList(actionKey + "effect-locations"), String.class)) {
                        List<String> effectList = data.getStringList(actionKey + "effects");
                        List<String> effectLocs = data.getStringList(actionKey + "effect-locations");
                        for (String s : effectList) {
                            Effect effect = Effect.valueOf(s.toUpperCase());
                            Location l = ConfigUtil.getLocation(effectLocs.get(effectList.indexOf(s)));
                            if (effect == null) {
                                plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + s 
                                        + ChatColor.GOLD + " inside " + ChatColor.GREEN + "effects: " + ChatColor.GOLD 
                                        + "inside Action " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD 
                                        + " is not a valid effect name!");
                                return null;
                            }
                            if (l == null) {
                                plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED 
                                        + effectLocs.get(effectList.indexOf(s)) + ChatColor.GOLD + " inside " 
                                        + ChatColor.GREEN + "effect-locations: " + ChatColor.GOLD + "inside Action " 
                                        + ChatColor.DARK_PURPLE + name + ChatColor.GOLD 
                                        + " is not in proper location format!");
                                plugin.getLogger().severe(ChatColor.GOLD 
                                        + "[Quests] Proper location format is: \"WorldName x y z\"");
                                return null;
                            }
                            action.effects.put(l, effect);
                        }
                    } else {
                        plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "effect-locations: " 
                                + ChatColor.GOLD + "inside Action " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD 
                                 + " is not a list of locations!");
                        return null;
                    }
                } else {
                    plugin.getLogger().severe(ChatColor.GOLD + "[Quests] Action " + ChatColor.DARK_PURPLE + name 
                            + ChatColor.GOLD + " is missing " + ChatColor.RED + "effect-locations:");
                    return null;
                }
            } else {
                plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "effects: " + ChatColor.GOLD 
                        + "inside Action " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD 
                        + " is not a list of effects!");
                return null;
            }
        }
        if (data.contains(actionKey + "items")) {
            LinkedList<ItemStack> temp = new LinkedList<ItemStack>(); // TODO - should maybe be = action.getItems() ?
            @SuppressWarnings("unchecked")
            List<ItemStack> stackList = (List<ItemStack>) data.get(actionKey + "items");
            if (ConfigUtil.checkList(stackList, ItemStack.class)) {
                for (ItemStack stack : stackList) {
                    if (stack != null) {
                        temp.add(stack);
                    }
                }
            } else {
                // Legacy
                if (ConfigUtil.checkList(stackList, String.class)) {
                    List<String> items = data.getStringList(actionKey + "items");
                    for (String item : items) {
                        try {
                            ItemStack stack = ItemUtil.readItemStack(item);
                            if (stack != null) {
                                temp.add(stack);
                            }
                        } catch (Exception e) {
                            plugin.getLogger().severe(ChatColor.GOLD + "[Quests] \"" + ChatColor.RED + item 
                                    + ChatColor.GOLD + "\" inside " + ChatColor.GREEN + " items: " + ChatColor.GOLD 
                                    + "inside Action " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD 
                                    + " is not formatted properly!");
                            return null;
                        }
                    }
                } else {
                    plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "items: " + ChatColor.GOLD
                            + "inside Action " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD 
                            + " is not a list of items!");
                    return null;
                }
            }
            action.setItems(temp);
        }
        if (data.contains(actionKey + "storm-world")) {
            World w = plugin.getServer().getWorld(data.getString(actionKey + "storm-world"));
            if (w == null) {
                plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "storm-world: " 
                        + ChatColor.GOLD + "inside Action " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD 
                        + " is not a valid World name!");
                return null;
            }
            if (data.contains(actionKey + "storm-duration")) {
                if (data.getInt(actionKey + "storm-duration", -999) != -999) {
                    action.stormDuration = data.getInt(actionKey + "storm-duration") * 1000;
                } else {
                    plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "storm-duration: " 
                            + ChatColor.GOLD + "inside Action " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD 
                            + " is not a number!");
                    return null;
                }
                action.stormWorld = w;
            } else {
                plugin.getLogger().severe(ChatColor.GOLD + "[Quests] Action " + ChatColor.DARK_PURPLE + name 
                        + ChatColor.GOLD + " is missing " + ChatColor.RED + "storm-duration:");
                return null;
            }
        }
        if (data.contains(actionKey + "thunder-world")) {
            World w = plugin.getServer().getWorld(data.getString(actionKey + "thunder-world"));
            if (w == null) {
                plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "thunder-world: " 
                        + ChatColor.GOLD + "inside Action " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD 
                        + " is not a valid World name!");
                return null;
            }
            if (data.contains(actionKey + "thunder-duration")) {
                if (data.getInt(actionKey + "thunder-duration", -999) != -999) {
                    action.thunderDuration = data.getInt(actionKey + "thunder-duration");
                } else {
                    plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "thunder-duration: " 
                            + ChatColor.GOLD + "inside Action " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD 
                            + " is not a number!");
                    return null;
                }
                action.thunderWorld = w;
            } else {
                plugin.getLogger().severe(ChatColor.GOLD + "[Quests] Action " + ChatColor.DARK_PURPLE + name 
                        + ChatColor.GOLD + " is missing " + ChatColor.RED + "thunder-duration:");
                return null;
            }
        }
        if (data.contains(actionKey + "mob-spawns")) {
            ConfigurationSection section = data.getConfigurationSection(actionKey + "mob-spawns");
            // is a mob, the keys are just a number or something.
            for (String s : section.getKeys(false)) {
                String mobName = section.getString(s + ".name");
                Location spawnLocation = ConfigUtil.getLocation(section.getString(s + ".spawn-location"));
                EntityType type = MiscUtil.getProperMobType(section.getString(s + ".mob-type"));
                Integer mobAmount = section.getInt(s + ".spawn-amounts");
                if (spawnLocation == null) {
                    plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + s + ChatColor.GOLD 
                            + " inside " + ChatColor.GREEN + " mob-spawn-locations: " + ChatColor.GOLD 
                            + "inside Action " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD 
                            + " is not in proper location format!");
                    plugin.getLogger().severe(ChatColor.GOLD 
                            + "[Quests] Proper location format is: \"WorldName x y z\"");
                    return null;
                }
                if (type == null) {
                    plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED 
                            + section.getString(s + ".mob-type") + ChatColor.GOLD + " inside " + ChatColor.GREEN 
                            + " mob-spawn-types: " + ChatColor.GOLD + "inside Action " + ChatColor.DARK_PURPLE + name 
                            + ChatColor.GOLD + " is not a valid mob name!");
                    return null;
                }
                ItemStack[] inventory = new ItemStack[5];
                Float[] dropChances = new Float[5];
                inventory[0] = ItemUtil.readItemStack(section.getString(s + ".held-item"));
                dropChances[0] = (float) section.getDouble(s + ".held-item-drop-chance");
                inventory[1] = ItemUtil.readItemStack(section.getString(s + ".boots"));
                dropChances[1] = (float) section.getDouble(s + ".boots-drop-chance");
                inventory[2] = ItemUtil.readItemStack(section.getString(s + ".leggings"));
                dropChances[2] = (float) section.getDouble(s + ".leggings-drop-chance");
                inventory[3] = ItemUtil.readItemStack(section.getString(s + ".chest-plate"));
                dropChances[3] = (float) section.getDouble(s + ".chest-plate-drop-chance");
                inventory[4] = ItemUtil.readItemStack(section.getString(s + ".helmet"));
                dropChances[4] = (float) section.getDouble(s + ".helmet-drop-chance");
                QuestMob questMob = new QuestMob(type, spawnLocation, mobAmount);
                questMob.setInventory(inventory);
                questMob.setDropChances(dropChances);
                questMob.setName(mobName);
                action.mobSpawns.add(questMob);
            }
        }
        if (data.contains(actionKey + "lightning-strikes")) {
            if (ConfigUtil.checkList(data.getList(actionKey + "lightning-strikes"), String.class)) {
                for (String s : data.getStringList(actionKey + "lightning-strikes")) {
                    Location loc = ConfigUtil.getLocation(s);
                    if (loc == null) {
                        plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + s + ChatColor.GOLD 
                                + " inside " + ChatColor.GREEN + " lightning-strikes: " + ChatColor.GOLD 
                                + "inside Action " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD 
                                + " is not in proper location format!");
                        plugin.getLogger().severe(ChatColor.GOLD 
                                + "[Quests] Proper location format is: \"WorldName x y z\"");
                        return null;
                    }
                    action.lightningStrikes.add(loc);
                }
            } else {
                plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "lightning-strikes: " 
                        + ChatColor.GOLD + "inside Action " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD 
                        + " is not a list of locations!");
                return null;
            }
        }
        if (data.contains(actionKey + "commands")) {
            if (ConfigUtil.checkList(data.getList(actionKey + "commands"), String.class)) {
                for (String s : data.getStringList(actionKey + "commands")) {
                    if (s.startsWith("/")) {
                        s = s.replaceFirst("/", "");
                    }
                    action.commands.add(s);
                }
            } else {
                plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "commands: " + ChatColor.GOLD 
                        + "inside Action " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD 
                        + " is not a list of commands!");
                return null;
            }
        }
        if (data.contains(actionKey + "potion-effect-types")) {
            if (ConfigUtil.checkList(data.getList(actionKey + "potion-effect-types"), String.class)) {
                if (data.contains(actionKey + "potion-effect-durations")) {
                    if (ConfigUtil.checkList(data.getList(actionKey + "potion-effect-durations"), Integer.class)) {
                        if (data.contains(actionKey + "potion-effect-amplifiers")) {
                            if (ConfigUtil.checkList(data.getList(actionKey + "potion-effect-amplifiers"), Integer.class)) {
                                List<String> types = data.getStringList(actionKey + "potion-effect-types");
                                List<Integer> durations = data.getIntegerList(actionKey + "potion-effect-durations");
                                List<Integer> amplifiers = data.getIntegerList(actionKey + "potion-effect-amplifiers");
                                for (String s : types) {
                                    PotionEffectType type = PotionEffectType.getByName(s);
                                    if (type == null) {
                                        plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + s 
                                                + ChatColor.GOLD + " inside " + ChatColor.GREEN + " lightning-strikes: "
                                                + ChatColor.GOLD + "inside Action " + ChatColor.DARK_PURPLE + name 
                                                + ChatColor.GOLD + " is not a valid potion effect name!");
                                        return null;
                                    }
                                    PotionEffect effect = new PotionEffect(type, durations
                                            .get(types.indexOf(s)), amplifiers.get(types.indexOf(s)));
                                    action.potionEffects.add(effect);
                                }
                            } else {
                                plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED 
                                        + "potion-effect-amplifiers: " + ChatColor.GOLD + "inside Action " 
                                        + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a list of numbers!");
                                return null;
                            }
                        } else {
                            plugin.getLogger().severe(ChatColor.GOLD + "[Quests] Action " + ChatColor.DARK_PURPLE + name
                                    + ChatColor.GOLD + " is missing " + ChatColor.RED + "potion-effect-amplifiers:");
                            return null;
                        }
                    } else {
                        plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED 
                                + "potion-effect-durations: " + ChatColor.GOLD + "inside Action " 
                                + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a list of numbers!");
                        return null;
                    }
                } else {
                    plugin.getLogger().severe(ChatColor.GOLD + "[Quests] Action " + ChatColor.DARK_PURPLE + name 
                            + ChatColor.GOLD + " is missing " + ChatColor.RED + "potion-effect-durations:");
                    return null;
                }
            } else {
                plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "potion-effect-types: " 
                        + ChatColor.GOLD + "inside Action " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD 
                        + " is not a list of potion effects!");
                return null;
            }
        }
        if (data.contains(actionKey + "hunger")) {
            if (data.getInt(actionKey + "hunger", -999) != -999) {
                action.hunger = data.getInt(actionKey + "hunger");
            } else {
                plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "hunger: " + ChatColor.GOLD 
                        + "inside Action " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a number!");
                return null;
            }
        }
        if (data.contains(actionKey + "saturation")) {
            if (data.getInt(actionKey + "saturation", -999) != -999) {
                action.saturation = data.getInt(actionKey + "saturation");
            } else {
                plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "saturation: " 
                        + ChatColor.GOLD + "inside Action " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD 
                        + " is not a number!");
                return null;
            }
        }
        if (data.contains(actionKey + "health")) {
            if (data.getInt(actionKey + "health", -999) != -999) {
                action.health = data.getInt(actionKey + "health");
            } else {
                plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "health: " + ChatColor.GOLD 
                        + "inside Action " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a number!");
                return null;
            }
        }
        if (data.contains(actionKey + "teleport-location")) {
            if (data.isString(actionKey + "teleport-location")) {
                Location l = ConfigUtil.getLocation(data.getString(actionKey + "teleport-location"));
                if (l == null) {
                    plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + data.getString(actionKey 
                            + "teleport-location") + ChatColor.GOLD + "for " + ChatColor.GREEN + " teleport-location: "
                            + ChatColor.GOLD + "inside Action " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD 
                            + " is not in proper location format!");
                    plugin.getLogger().severe(ChatColor.GOLD 
                            + "[Quests] Proper location format is: \"WorldName x y z\"");
                    return null;
                }
                action.teleport = l;
            } else {
                plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "teleport-location: " 
                        + ChatColor.GOLD + "inside Action " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD 
                        + " is not a location!");
                return null;
            }
        }
        if (data.contains(actionKey + "timer")) {
            if (data.isInt(actionKey + "timer")) {
                action.timer = data.getInt(actionKey + "timer");
            } else {
                plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "timer: " + ChatColor.GOLD 
                        + "inside Action " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD + " is not a number!");
                return null;
            }
        }
        if (data.contains(actionKey + "cancel-timer")) {
            if (data.isBoolean(actionKey + "cancel-timer")) {
                action.cancelTimer = data.getBoolean(actionKey + "cancel-timer");
            } else {
                plugin.getLogger().severe(ChatColor.GOLD + "[Quests] " + ChatColor.RED + "cancel-timer: " 
                        + ChatColor.GOLD + "inside Action " + ChatColor.DARK_PURPLE + name + ChatColor.GOLD 
                        + " is not a boolean!");
                return null;
            }
        }
        return action;
    }
}
