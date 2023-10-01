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
import me.pikamug.quests.convo.actions.menu.ActionMenuPrompt;
import me.pikamug.quests.entity.BukkitQuestMob;
import me.pikamug.quests.entity.QuestMob;
import me.pikamug.quests.interfaces.ReloadCallback;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.util.BukkitConfigUtil;
import me.pikamug.quests.util.BukkitFakeConversable;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.BukkitLang;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.ConversationPrefix;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;

public class BukkitActionFactory implements ActionFactory, ConversationAbandonedListener {

    private final BukkitQuestsPlugin plugin;
    private final ConversationFactory conversationFactory;
    private Map<UUID, Block> selectedExplosionLocations = new HashMap<>();
    private Map<UUID, Block> selectedEffectLocations = new HashMap<>();
    private Map<UUID, Block> selectedMobLocations = new HashMap<>();
    private Map<UUID, Block> selectedLightningLocations = new HashMap<>();
    private Map<UUID, Block> selectedTeleportLocations = new HashMap<>();
    private List<String> editingActionNames = new LinkedList<>();

    public BukkitActionFactory(final BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
        // Ensure to initialize factory last so that 'this' is fully initialized before it is passed
        this.conversationFactory = new ConversationFactory(plugin).withModality(false).withLocalEcho(false)
                .withFirstPrompt(new ActionMenuPrompt(new ConversationContext(plugin, new BukkitFakeConversable(),
                        new HashMap<>()))).withTimeout(3600)
                .withPrefix(new LineBreakPrefix()).addConversationAbandonedListener(this);
    }
    
    public static class LineBreakPrefix implements ConversationPrefix {
        @Override
        public @NotNull String getPrefix(final @NotNull ConversationContext context) {
            return "\n";
        }
    }
    
    public Map<UUID, Block> getSelectedExplosionLocations() {
        return selectedExplosionLocations;
    }

    public void setSelectedExplosionLocations(final Map<UUID, Block> selectedExplosionLocations) {
        this.selectedExplosionLocations = selectedExplosionLocations;
    }

    public Map<UUID, Block> getSelectedEffectLocations() {
        return selectedEffectLocations;
    }

    public void setSelectedEffectLocations(final Map<UUID, Block> selectedEffectLocations) {
        this.selectedEffectLocations = selectedEffectLocations;
    }

    public Map<UUID, Block> getSelectedMobLocations() {
        return selectedMobLocations;
    }

    public void setSelectedMobLocations(final Map<UUID, Block> selectedMobLocations) {
        this.selectedMobLocations = selectedMobLocations;
    }

    public Map<UUID, Block> getSelectedLightningLocations() {
        return selectedLightningLocations;
    }

    public void setSelectedLightningLocations(final Map<UUID, Block> selectedLightningLocations) {
        this.selectedLightningLocations = selectedLightningLocations;
    }

    public Map<UUID, Block> getSelectedTeleportLocations() {
        return selectedTeleportLocations;
    }

    public void setSelectedTeleportLocations(
            final Map<UUID, Block> selectedTeleportLocations) {
        this.selectedTeleportLocations = selectedTeleportLocations;
    }

    public ConversationFactory getConversationFactory() {
        return conversationFactory;
    }
    
    public List<String> getNamesOfActionsBeingEdited() {
        return editingActionNames;
    }
    
    public void setNamesOfActionsBeingEdited(final List<String> actionNames) {
        this.editingActionNames = actionNames;
    }

    @Override
    public void conversationAbandoned(final ConversationAbandonedEvent abandonedEvent) {
        if (abandonedEvent.getContext().getForWhom() instanceof Player) {
            final UUID uuid = ((Player) abandonedEvent.getContext().getForWhom()).getUniqueId();
            selectedExplosionLocations.remove(uuid);
            selectedEffectLocations.remove(uuid);
            selectedMobLocations.remove(uuid);
            selectedLightningLocations.remove(uuid);
            selectedTeleportLocations.remove(uuid);
        }
    }
    
    public Prompt returnToMenu(final ConversationContext context) {
        return new ActionMainPrompt(context);
    }
    
    public void loadData(final ConversationContext context, final Action action) {
        BukkitAction bukkitAction = (BukkitAction) action;
        if (bukkitAction.getMessage() != null) {
            context.setSessionData(Key.A_MESSAGE, bukkitAction.getMessage());
        }
        if (bukkitAction.isClearInv()) {
            context.setSessionData(Key.A_CLEAR_INVENTORY, true);
        } else {
            context.setSessionData(Key.A_CLEAR_INVENTORY, false);
        }
        if (bukkitAction.isFailQuest()) {
            context.setSessionData(Key.A_FAIL_QUEST, true);
        } else {
            context.setSessionData(Key.A_FAIL_QUEST, false);
        }
        if (bukkitAction.getItems() != null && !bukkitAction.getItems().isEmpty()) {
            final LinkedList<ItemStack> items = new LinkedList<>(bukkitAction.getItems());
            context.setSessionData(Key.A_ITEMS, items);
        }
        if (bukkitAction.getExplosions() != null && !bukkitAction.getExplosions().isEmpty()) {
            final LinkedList<String> locs = new LinkedList<>();
            for (final Location loc : bukkitAction.getExplosions()) {
                locs.add(BukkitConfigUtil.getLocationInfo(loc));
            }
            context.setSessionData(Key.A_EXPLOSIONS, locs);
        }
        if (bukkitAction.getEffects() != null && !bukkitAction.getEffects().isEmpty()) {
            final LinkedList<String> locs = new LinkedList<>();
            final LinkedList<String> effs = new LinkedList<>();
            for (final Entry<Location, Effect> e : bukkitAction.getEffects().entrySet()) {
                locs.add(BukkitConfigUtil.getLocationInfo(e.getKey()));
                effs.add(e.getValue().toString());
            }
            context.setSessionData(Key.A_EFFECTS, effs);
            context.setSessionData(Key.A_EFFECTS_LOCATIONS, locs);
        }
        if (bukkitAction.getStormWorld() != null) {
            context.setSessionData(Key.A_WORLD_STORM, bukkitAction.getStormWorld().getName());
            context.setSessionData(Key.A_WORLD_STORM_DURATION, bukkitAction.getStormDuration());
        }
        if (bukkitAction.getThunderWorld() != null) {
            context.setSessionData(Key.A_WORLD_THUNDER, bukkitAction.getThunderWorld().getName());
            context.setSessionData(Key.A_WORLD_THUNDER_DURATION, bukkitAction.getThunderDuration());
        }
        if (bukkitAction.getMobSpawns() != null && !bukkitAction.getMobSpawns().isEmpty()) {
            context.setSessionData(Key.A_MOBS, bukkitAction.getMobSpawns());
        }
        if (bukkitAction.getLightningStrikes() != null && !bukkitAction.getLightningStrikes().isEmpty()) {
            final LinkedList<String> locs = new LinkedList<>();
            for (final Location loc : bukkitAction.getLightningStrikes()) {
                locs.add(BukkitConfigUtil.getLocationInfo(loc));
            }
            context.setSessionData(Key.A_LIGHTNING, locs);
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
            context.setSessionData(Key.A_POTION_TYPES, types);
            context.setSessionData(Key.A_POTION_DURATIONS, durations);
            context.setSessionData(Key.A_POTION_STRENGTH, mags);
        }
        if (bukkitAction.getHunger() > -1) {
            context.setSessionData(Key.A_HUNGER, bukkitAction.getHunger());
        }
        if (bukkitAction.getSaturation() > -1) {
            context.setSessionData(Key.A_SATURATION, bukkitAction.getSaturation());
        }
        if (bukkitAction.getHealth() > -1) {
            context.setSessionData(Key.A_HEALTH, bukkitAction.getHealth());
        }
        if (bukkitAction.getTeleport() != null) {
            context.setSessionData(Key.A_TELEPORT, BukkitConfigUtil.getLocationInfo(bukkitAction.getTeleport()));
        }
        if (bukkitAction.getCommands() != null) {
            context.setSessionData(Key.A_COMMANDS, bukkitAction.getCommands());
        }
        if (bukkitAction.getTimer() > 0) {
            context.setSessionData(Key.A_TIMER, bukkitAction.getTimer());
        }
        if (bukkitAction.isCancelTimer()) {
            context.setSessionData(Key.A_CANCEL_TIMER, true);
        }
    }

    public void clearData(final ConversationContext context) {
        context.setSessionData(Key.A_OLD_ACTION, null);
        context.setSessionData(Key.A_NAME, null);
        context.setSessionData(Key.A_MESSAGE, null);
        context.setSessionData(Key.A_CLEAR_INVENTORY, null);
        context.setSessionData(Key.A_FAIL_QUEST, null);
        context.setSessionData(Key.A_ITEMS, null);
        context.setSessionData(Key.A_ITEMS_AMOUNTS, null);
        context.setSessionData(Key.A_EXPLOSIONS, null);
        context.setSessionData(Key.A_EFFECTS, null);
        context.setSessionData(Key.A_EFFECTS_LOCATIONS, null);
        context.setSessionData(Key.A_WORLD_STORM, null);
        context.setSessionData(Key.A_WORLD_STORM_DURATION, null);
        context.setSessionData(Key.A_WORLD_THUNDER, null);
        context.setSessionData(Key.A_WORLD_THUNDER_DURATION, null);
        context.setSessionData(Key.A_MOBS, null);
        context.setSessionData(Key.A_LIGHTNING, null);
        context.setSessionData(Key.A_POTION_TYPES, null);
        context.setSessionData(Key.A_POTION_DURATIONS, null);
        context.setSessionData(Key.A_POTION_STRENGTH, null);
        context.setSessionData(Key.A_HUNGER, null);
        context.setSessionData(Key.A_SATURATION, null);
        context.setSessionData(Key.A_HEALTH, null);
        context.setSessionData(Key.A_TELEPORT, null);
        context.setSessionData(Key.A_COMMANDS, null);
        context.setSessionData(Key.A_TIMER, null);
        context.setSessionData(Key.A_CANCEL_TIMER, null);
    }

    public void deleteAction(final ConversationContext context) {
        final YamlConfiguration data = new YamlConfiguration();
        final File actionsFile = new File(plugin.getDataFolder(), "actions.yml");
        try {
            data.load(actionsFile);
        } catch (final IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("questErrorReadingFile")
                    .replace("<file>", actionsFile.getName()));
            return;
        }
        final String action = (String) context.getSessionData(Key.ED_EVENT_DELETE);
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
            context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("questSaveError"));
            return;
        }
        final ReloadCallback<Boolean> callback = response -> {
            if (!response) {
                context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("unknownError"));
            }
        };
        plugin.reload(callback);
        context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("eventEditorDeleted"));
        if (plugin.getConfigSettings().getConsoleLogging() > 0) {
            final String identifier = context.getForWhom() instanceof Player ? 
                    "Player " + ((Player)context.getForWhom()).getUniqueId() : "CONSOLE";
            plugin.getLogger().info(identifier + " deleted action " + action);
        }
        for (final Quester q : plugin.getOfflineQuesters()) {
            for (final Quest quest : q.getCurrentQuests().keySet()) {
                q.checkQuest(quest);
            }
        }
        clearData(context);
    }

    @SuppressWarnings("unchecked")
    public void saveAction(final ConversationContext context) {
        final YamlConfiguration data = new YamlConfiguration();
        final File actionsFile = new File(plugin.getDataFolder(), "actions.yml");
        try {
            data.load(actionsFile);
        } catch (final IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("questErrorReadingFile")
                    .replace("<file>", actionsFile.getName()));
            return;
        }
        String key = "actions";
        if (data.getConfigurationSection(key) == null) {
            key = "events";
        }
        if (context.getSessionData(Key.A_OLD_ACTION) != null
                && !((String) Objects.requireNonNull(context.getSessionData(Key.A_OLD_ACTION))).isEmpty()) {
            data.set(key + "." + context.getSessionData(Key.A_OLD_ACTION), null);
            final Collection<Action> temp = plugin.getLoadedActions();
            temp.remove(plugin.getAction((String) context.getSessionData(Key.A_OLD_ACTION)));
            plugin.setLoadedActions(temp);
        }
        final ConfigurationSection section = data.createSection(key + "." + context.getSessionData(Key.A_NAME));
        editingActionNames.remove((String) context.getSessionData(Key.A_NAME));
        if (context.getSessionData(Key.A_MESSAGE) != null) {
            section.set("message", context.getSessionData(Key.A_MESSAGE));
        }
        if (context.getSessionData(Key.A_CLEAR_INVENTORY) != null) {
            final Boolean b = (Boolean) context.getSessionData(Key.A_CLEAR_INVENTORY);
            if (b != null) {
                section.set("clear-inventory", b);
            }
        }
        if (context.getSessionData(Key.A_FAIL_QUEST) != null) {
            final Boolean b = (Boolean) context.getSessionData(Key.A_FAIL_QUEST);
            if (b != null) {
                section.set("fail-quest", b);
            }
        }
        if (context.getSessionData(Key.A_ITEMS) != null) {
            section.set("items", context.getSessionData(Key.A_ITEMS));
        }
        if (context.getSessionData(Key.A_EXPLOSIONS) != null) {
            section.set("explosions", context.getSessionData(Key.A_EXPLOSIONS));
        }
        if (context.getSessionData(Key.A_EFFECTS) != null) {
            section.set("effects", context.getSessionData(Key.A_EFFECTS));
            section.set("effect-locations", context.getSessionData(Key.A_EFFECTS_LOCATIONS));
        }
        if (context.getSessionData(Key.A_WORLD_STORM) != null) {
            section.set("storm-world", context.getSessionData(Key.A_WORLD_STORM));
            section.set("storm-duration", context.getSessionData(Key.A_WORLD_STORM_DURATION));
        }
        if (context.getSessionData(Key.A_WORLD_THUNDER) != null) {
            section.set("thunder-world", context.getSessionData(Key.A_WORLD_THUNDER));
            section.set("thunder-duration", context.getSessionData(Key.A_WORLD_THUNDER_DURATION));
        }
        try {
            if (context.getSessionData(Key.A_MOBS) != null) {
                int count = 0;
                for (final QuestMob mob : (LinkedList<QuestMob>) Objects.requireNonNull(context
                        .getSessionData(Key.A_MOBS))) {
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
        if (context.getSessionData(Key.A_LIGHTNING) != null) {
            section.set("lightning-strikes", context.getSessionData(Key.A_LIGHTNING));
        }
        if (context.getSessionData(Key.A_COMMANDS) != null) {
            final LinkedList<String> commands = (LinkedList<String>) context.getSessionData(Key.A_COMMANDS);
            if (commands != null && !commands.isEmpty()) {
                section.set("commands", commands);
            }
        }
        if (context.getSessionData(Key.A_POTION_TYPES) != null) {
            section.set("potion-effect-types", context.getSessionData(Key.A_POTION_TYPES));
            section.set("potion-effect-durations", context.getSessionData(Key.A_POTION_DURATIONS));
            section.set("potion-effect-amplifiers", context.getSessionData(Key.A_POTION_STRENGTH));
        }
        if (context.getSessionData(Key.A_HUNGER) != null) {
            section.set("hunger", context.getSessionData(Key.A_HUNGER));
        }
        if (context.getSessionData(Key.A_SATURATION) != null) {
            section.set("saturation", context.getSessionData(Key.A_SATURATION));
        }
        if (context.getSessionData(Key.A_HEALTH) != null) {
            section.set("health", context.getSessionData(Key.A_HEALTH));
        }
        if (context.getSessionData(Key.A_TELEPORT) != null) {
            section.set("teleport-location", context.getSessionData(Key.A_TELEPORT));
        }
        if (context.getSessionData(Key.A_TIMER) != null) {
            final Integer i = (Integer) context.getSessionData(Key.A_TIMER);
            if (i != null && i > 0) {
                section.set("timer", context.getSessionData(Key.A_TIMER));
            }
        }
        if (context.getSessionData(Key.A_CANCEL_TIMER) != null) {
            final Boolean b = (Boolean) context.getSessionData(Key.A_CANCEL_TIMER);
            if (b != null) {
                section.set("cancel-timer", b);
            }
        }
        if (context.getSessionData(Key.A_DENIZEN) != null) {
            section.set("denizen-script", context.getSessionData(Key.A_DENIZEN));
        }
        try {
            data.save(actionsFile);
        } catch (final IOException e) {
            context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("questSaveError"));
            return;
        }
        final ReloadCallback<Boolean> callback = response -> {
            if (!response) {
                context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("unknownError"));
            }
        };
        plugin.reload(callback);
        context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("eventEditorSaved"));
        if (plugin.getConfigSettings().getConsoleLogging() > 0) {
            final String identifier = context.getForWhom() instanceof Player ? 
                    "Player " + ((Player)context.getForWhom()).getUniqueId() : "CONSOLE";
            plugin.getLogger().info(identifier + " saved action " + context.getSessionData(Key.A_NAME));
        }
        for (final Quester q : plugin.getOfflineQuesters()) {
            for (final Quest quest : q.getCurrentQuests().keySet()) {
                q.checkQuest(quest);
            }
        }
        clearData(context);
    }
}