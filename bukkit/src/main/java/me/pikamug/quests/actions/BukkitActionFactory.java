/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.actions;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.convo.actions.main.ActionMainPrompt;
import me.pikamug.quests.entity.BukkitQuestMob;
import me.pikamug.quests.entity.QuestMob;
import me.pikamug.quests.interfaces.ReloadCallback;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.util.BukkitConfigUtil;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.quests.util.BukkitMiscUtil;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.SessionData;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BukkitActionFactory implements ActionFactory/*, ConversationAbandonedListener*/ {

    private final BukkitQuestsPlugin plugin;
    private ConcurrentHashMap<UUID, Block> selectedExplosionLocations = new ConcurrentHashMap<>();
    private ConcurrentHashMap<UUID, Block> selectedEffectLocations = new ConcurrentHashMap<>();
    private ConcurrentHashMap<UUID, Block> selectedMobLocations = new ConcurrentHashMap<>();
    private ConcurrentHashMap<UUID, Block> selectedLightningLocations = new ConcurrentHashMap<>();
    private ConcurrentHashMap<UUID, Block> selectedTeleportLocations = new ConcurrentHashMap<>();
    private List<String> editingActionNames = new LinkedList<>();

    public BukkitActionFactory(final BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }
    
    public ConcurrentHashMap<UUID, Block> getSelectedExplosionLocations() {
        return selectedExplosionLocations;
    }

    public void setSelectedExplosionLocations(final ConcurrentHashMap<UUID, Block> selectedExplosionLocations) {
        this.selectedExplosionLocations = selectedExplosionLocations;
    }

    public ConcurrentHashMap<UUID, Block> getSelectedEffectLocations() {
        return selectedEffectLocations;
    }

    public void setSelectedEffectLocations(final ConcurrentHashMap<UUID, Block> selectedEffectLocations) {
        this.selectedEffectLocations = selectedEffectLocations;
    }

    public ConcurrentHashMap<UUID, Block> getSelectedMobLocations() {
        return selectedMobLocations;
    }

    public void setSelectedMobLocations(final ConcurrentHashMap<UUID, Block> selectedMobLocations) {
        this.selectedMobLocations = selectedMobLocations;
    }

    public ConcurrentHashMap<UUID, Block> getSelectedLightningLocations() {
        return selectedLightningLocations;
    }

    public void setSelectedLightningLocations(final ConcurrentHashMap<UUID, Block> selectedLightningLocations) {
        this.selectedLightningLocations = selectedLightningLocations;
    }

    public ConcurrentHashMap<UUID, Block> getSelectedTeleportLocations() {
        return selectedTeleportLocations;
    }

    public void setSelectedTeleportLocations(final ConcurrentHashMap<UUID, Block> selectedTeleportLocations) {
        this.selectedTeleportLocations = selectedTeleportLocations;
    }

    /*public ConversationFactory getConversationFactory() {
        return conversationFactory;
    }*/
    
    public List<String> getNamesOfActionsBeingEdited() {
        return editingActionNames;
    }
    
    public void setNamesOfActionsBeingEdited(final List<String> actionNames) {
        this.editingActionNames = actionNames;
    }

    /*@Override
    public void conversationAbandoned(final ConversationAbandonedEvent abandonedEvent) {
        if (abandonedEvent.getContext().getForWhom() instanceof Player) {
            final UUID uuid = ((Player) abandonedEvent.getContext().getForWhom()).getUniqueId();
            selectedExplosionLocations.remove(uuid);
            selectedEffectLocations.remove(uuid);
            selectedMobLocations.remove(uuid);
            selectedLightningLocations.remove(uuid);
            selectedTeleportLocations.remove(uuid);
        }
    }*/
    
    public void returnToMenu(final UUID uuid) {
        new ActionMainPrompt(uuid).start();
    }
    
    public void loadData(final UUID uuid, final Action action) {
        BukkitAction bukkitAction = (BukkitAction) action;
        if (bukkitAction.getMessage() != null) {
            SessionData.set(uuid, Key.A_MESSAGE, bukkitAction.getMessage());
        }
        if (bukkitAction.isClearInv()) {
            SessionData.set(uuid, Key.A_CLEAR_INVENTORY, true);
        } else {
            SessionData.set(uuid, Key.A_CLEAR_INVENTORY, false);
        }
        if (bukkitAction.isFailQuest()) {
            SessionData.set(uuid, Key.A_FAIL_QUEST, true);
        } else {
            SessionData.set(uuid, Key.A_FAIL_QUEST, false);
        }
        if (bukkitAction.getItems() != null && !bukkitAction.getItems().isEmpty()) {
            final LinkedList<ItemStack> items = new LinkedList<>(bukkitAction.getItems());
            SessionData.set(uuid, Key.A_ITEMS, items);
        }
        if (bukkitAction.getExplosions() != null && !bukkitAction.getExplosions().isEmpty()) {
            final LinkedList<String> locs = new LinkedList<>();
            for (final Location loc : bukkitAction.getExplosions()) {
                locs.add(BukkitConfigUtil.getLocationInfo(loc));
            }
            SessionData.set(uuid, Key.A_EXPLOSIONS, locs);
        }
        if (bukkitAction.getEffects() != null && !bukkitAction.getEffects().isEmpty()) {
            final LinkedList<String> locs = new LinkedList<>();
            final LinkedList<String> effs = new LinkedList<>();
            for (final Entry<Location, Effect> e : bukkitAction.getEffects().entrySet()) {
                locs.add(BukkitConfigUtil.getLocationInfo(e.getKey()));
                effs.add(e.getValue().toString());
            }
            SessionData.set(uuid, Key.A_EFFECTS, effs);
            SessionData.set(uuid, Key.A_EFFECTS_LOCATIONS, locs);
        }
        if (bukkitAction.getStormWorld() != null) {
            SessionData.set(uuid, Key.A_WORLD_STORM, bukkitAction.getStormWorld().getName());
            SessionData.set(uuid, Key.A_WORLD_STORM_DURATION, bukkitAction.getStormDuration());
        }
        if (bukkitAction.getThunderWorld() != null) {
            SessionData.set(uuid, Key.A_WORLD_THUNDER, bukkitAction.getThunderWorld().getName());
            SessionData.set(uuid, Key.A_WORLD_THUNDER_DURATION, bukkitAction.getThunderDuration());
        }
        if (bukkitAction.getMobSpawns() != null && !bukkitAction.getMobSpawns().isEmpty()) {
            SessionData.set(uuid, Key.A_MOBS, bukkitAction.getMobSpawns());
        }
        if (bukkitAction.getLightningStrikes() != null && !bukkitAction.getLightningStrikes().isEmpty()) {
            final LinkedList<String> locs = new LinkedList<>();
            for (final Location loc : bukkitAction.getLightningStrikes()) {
                locs.add(BukkitConfigUtil.getLocationInfo(loc));
            }
            SessionData.set(uuid, Key.A_LIGHTNING, locs);
        }
        if (bukkitAction.getPotionEffects() != null && !bukkitAction.getPotionEffects().isEmpty()) {
            final LinkedList<String> types = new LinkedList<>();
            final LinkedList<Long> durations = new LinkedList<>();
            final LinkedList<Integer> mags = new LinkedList<>();
            for (final PotionEffect pe : bukkitAction.getPotionEffects()) {
                types.add(pe.getType().getName());
                durations.add((long) pe.getDuration());
                mags.add(pe.getAmplifier());
            }
            SessionData.set(uuid, Key.A_POTION_TYPES, types);
            SessionData.set(uuid, Key.A_POTION_DURATIONS, durations);
            SessionData.set(uuid, Key.A_POTION_STRENGTH, mags);
        }
        if (bukkitAction.getHunger() > -1) {
            SessionData.set(uuid, Key.A_HUNGER, bukkitAction.getHunger());
        }
        if (bukkitAction.getSaturation() > -1) {
            SessionData.set(uuid, Key.A_SATURATION, bukkitAction.getSaturation());
        }
        if (bukkitAction.getHealth() > -1) {
            SessionData.set(uuid, Key.A_HEALTH, bukkitAction.getHealth());
        }
        if (bukkitAction.getTeleport() != null) {
            SessionData.set(uuid, Key.A_TELEPORT, BukkitConfigUtil.getLocationInfo(bukkitAction.getTeleport()));
        }
        if (bukkitAction.getCommands() != null) {
            SessionData.set(uuid, Key.A_COMMANDS, bukkitAction.getCommands());
        }
        if (bukkitAction.getTimer() > 0) {
            SessionData.set(uuid, Key.A_TIMER, bukkitAction.getTimer());
        }
        if (bukkitAction.isCancelTimer()) {
            SessionData.set(uuid, Key.A_CANCEL_TIMER, true);
        }
    }

    public void clearData(final UUID uuid) {
        SessionData.remove(uuid);
    }

    public void deleteAction(final UUID uuid) {
        final YamlConfiguration data = new YamlConfiguration();
        final File actionsFile = new File(plugin.getDataFolder(), "storage" + File.separatorChar + "actions.yml");
        final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
        try {
            data.load(actionsFile);
        } catch (final IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            sender.sendMessage(ChatColor.RED + BukkitLang.get("questErrorReadingFile")
                    .replace("<file>", actionsFile.getName()));
            return;
        }
        final String action = (String) SessionData.get(uuid, Key.ED_EVENT_DELETE);
        String key = "actions";
        ConfigurationSection sec = data.getConfigurationSection(key);
        if (sec == null) {
            key = "events";
            sec = data.getConfigurationSection(key);
        }
        if (sec != null && action != null) {
            sec.set(action, null);
        }
        try {
            data.save(actionsFile);
        } catch (final IOException e) {
            sender.sendMessage(ChatColor.RED + BukkitLang.get("questSaveError"));
            return;
        }
        final ReloadCallback<Boolean> callback = response -> {
            if (!response) {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("unknownError"));
            }
        };
        plugin.reload(callback);
        sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("eventEditorDeleted"));
        if (plugin.getConfigSettings().getConsoleLogging() > 0) {
            final String identifier = sender instanceof Player ? "Player " + uuid : "CONSOLE";
            plugin.getLogger().info(identifier + " deleted action " + action);
        }
        for (final Quester q : plugin.getOfflineQuesters()) {
            for (final Quest quest : q.getCurrentQuests().keySet()) {
                q.checkQuest(quest);
            }
        }
        clearData(uuid);
    }

    @SuppressWarnings("unchecked")
    public void saveAction(final UUID uuid) {
        final YamlConfiguration data = new YamlConfiguration();
        final File actionsFile = new File(plugin.getDataFolder(), "storage" + File.separatorChar + "actions.yml");
        final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
        try {
            data.load(actionsFile);
        } catch (final IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            sender.sendMessage(ChatColor.RED + BukkitLang.get("questErrorReadingFile")
                    .replace("<file>", actionsFile.getName()));
            return;
        }
        String key = "actions";
        if (data.getConfigurationSection(key) == null) {
            key = "events";
        }
        if (SessionData.get(uuid, Key.A_OLD_ACTION) != null
                && !((String) Objects.requireNonNull(SessionData.get(uuid, Key.A_OLD_ACTION))).isEmpty()) {
            data.set(key + "." + SessionData.get(uuid, Key.A_OLD_ACTION), null);
            final Collection<Action> temp = plugin.getLoadedActions();
            temp.remove(plugin.getAction((String) SessionData.get(uuid, Key.A_OLD_ACTION)));
            plugin.setLoadedActions(temp);
        }
        final ConfigurationSection section = data.createSection(key + "." + SessionData.get(uuid, Key.A_NAME));
        editingActionNames.remove((String) SessionData.get(uuid, Key.A_NAME));
        if (SessionData.get(uuid, Key.A_MESSAGE) != null) {
            section.set("message", SessionData.get(uuid, Key.A_MESSAGE));
        }
        if (SessionData.get(uuid, Key.A_CLEAR_INVENTORY) != null) {
            final Boolean b = (Boolean) SessionData.get(uuid, Key.A_CLEAR_INVENTORY);
            if (b != null) {
                section.set("clear-inventory", b);
            }
        }
        if (SessionData.get(uuid, Key.A_FAIL_QUEST) != null) {
            final Boolean b = (Boolean) SessionData.get(uuid, Key.A_FAIL_QUEST);
            if (b != null) {
                section.set("fail-quest", b);
            }
        }
        if (SessionData.get(uuid, Key.A_ITEMS) != null) {
            section.set("items", SessionData.get(uuid, Key.A_ITEMS));
        }
        if (SessionData.get(uuid, Key.A_EXPLOSIONS) != null) {
            section.set("explosions", SessionData.get(uuid, Key.A_EXPLOSIONS));
        }
        if (SessionData.get(uuid, Key.A_EFFECTS) != null) {
            section.set("effects", SessionData.get(uuid, Key.A_EFFECTS));
            section.set("effect-locations", SessionData.get(uuid, Key.A_EFFECTS_LOCATIONS));
        }
        if (SessionData.get(uuid, Key.A_WORLD_STORM) != null) {
            section.set("storm-world", SessionData.get(uuid, Key.A_WORLD_STORM));
            section.set("storm-duration", SessionData.get(uuid, Key.A_WORLD_STORM_DURATION));
        }
        if (SessionData.get(uuid, Key.A_WORLD_THUNDER) != null) {
            section.set("thunder-world", SessionData.get(uuid, Key.A_WORLD_THUNDER));
            section.set("thunder-duration", SessionData.get(uuid, Key.A_WORLD_THUNDER_DURATION));
        }
        try {
            if (SessionData.get(uuid, Key.A_MOBS) != null) {
                int count = 0;
                for (final QuestMob mob : (LinkedList<QuestMob>) Objects.requireNonNull(SessionData
                        .get(uuid, Key.A_MOBS))) {
                    ConfigurationSection cs = section.getConfigurationSection("mob-spawns." + count);
                    if (cs == null) {
                        cs = section.createSection("mob-spawns." + count);
                    }
                    final BukkitQuestMob questMob = (BukkitQuestMob) mob;
                    if (questMob.getName() != null) {
                        cs.set("name", questMob.getName());
                    }
                    cs.set("spawn-location", BukkitConfigUtil.getLocationInfo(questMob.getSpawnLocation()));
                    cs.set("mob-type", questMob.getType().name());
                    cs.set("spawn-amounts", questMob.getSpawnAmounts());
                    cs.set("held-item", questMob.getInventory()[0]);
                    cs.set("held-item-drop-chance", questMob.getDropChances()[0]);
                    cs.set("boots", questMob.getInventory()[1]);
                    cs.set("boots-drop-chance", questMob.getDropChances()[1]);
                    cs.set("leggings", questMob.getInventory()[2]);
                    cs.set("leggings-drop-chance", questMob.getDropChances()[2]);
                    cs.set("chest-plate", questMob.getInventory()[3]);
                    cs.set("chest-plate-drop-chance", questMob.getDropChances()[3]);
                    cs.set("helmet", questMob.getInventory()[4]);
                    cs.set("helmet-drop-chance", questMob.getDropChances()[4]);
                    count++;
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        if (SessionData.get(uuid, Key.A_LIGHTNING) != null) {
            section.set("lightning-strikes", SessionData.get(uuid, Key.A_LIGHTNING));
        }
        if (SessionData.get(uuid, Key.A_COMMANDS) != null) {
            final LinkedList<String> commands = (LinkedList<String>) SessionData.get(uuid, Key.A_COMMANDS);
            if (commands != null && !commands.isEmpty()) {
                section.set("commands", commands);
            }
        }
        if (SessionData.get(uuid, Key.A_POTION_TYPES) != null) {
            section.set("potion-effect-types", SessionData.get(uuid, Key.A_POTION_TYPES));
            section.set("potion-effect-durations", SessionData.get(uuid, Key.A_POTION_DURATIONS));
            section.set("potion-effect-amplifiers", SessionData.get(uuid, Key.A_POTION_STRENGTH));
        }
        if (SessionData.get(uuid, Key.A_HUNGER) != null) {
            section.set("hunger", SessionData.get(uuid, Key.A_HUNGER));
        }
        if (SessionData.get(uuid, Key.A_SATURATION) != null) {
            section.set("saturation", SessionData.get(uuid, Key.A_SATURATION));
        }
        if (SessionData.get(uuid, Key.A_HEALTH) != null) {
            section.set("health", SessionData.get(uuid, Key.A_HEALTH));
        }
        if (SessionData.get(uuid, Key.A_TELEPORT) != null) {
            section.set("teleport-location", SessionData.get(uuid, Key.A_TELEPORT));
        }
        if (SessionData.get(uuid, Key.A_TIMER) != null) {
            final Integer i = (Integer) SessionData.get(uuid, Key.A_TIMER);
            if (i != null && i > 0) {
                section.set("timer", SessionData.get(uuid, Key.A_TIMER));
            }
        }
        if (SessionData.get(uuid, Key.A_CANCEL_TIMER) != null) {
            final Boolean b = (Boolean) SessionData.get(uuid, Key.A_CANCEL_TIMER);
            if (b != null) {
                section.set("cancel-timer", b);
            }
        }
        if (SessionData.get(uuid, Key.A_DENIZEN) != null) {
            section.set("denizen-script", SessionData.get(uuid, Key.A_DENIZEN));
        }
        try {
            data.save(actionsFile);
        } catch (final IOException e) {
            sender.sendMessage(ChatColor.RED + BukkitLang.get("questSaveError"));
            return;
        }
        final ReloadCallback<Boolean> callback = response -> {
            if (!response) {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("unknownError"));
            }
        };
        plugin.reload(callback);
        sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("eventEditorSaved"));
        if (plugin.getConfigSettings().getConsoleLogging() > 0) {
            final String identifier = sender instanceof Player ? "Player " + uuid : "CONSOLE";
            plugin.getLogger().info(identifier + " saved action " + SessionData.get(uuid, Key.A_NAME));
        }
        for (final Quester q : plugin.getOfflineQuesters()) {
            for (final Quest quest : q.getCurrentQuests().keySet()) {
                q.checkQuest(quest);
            }
        }
        clearData(uuid);
    }
}