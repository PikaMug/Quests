/*
 * Copyright (c) 2014 PikaMug and contributors. All rights reserved.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package me.pikamug.quests.actions;

import me.pikamug.quests.quests.IQuest;
import me.pikamug.quests.player.IQuester;
import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.convo.actions.main.ActionMainPrompt;
import me.pikamug.quests.convo.actions.menu.ActionMenuPrompt;
import me.pikamug.quests.entity.BukkitQuestMob;
import me.pikamug.quests.entity.QuestMob;
import me.pikamug.quests.interfaces.ReloadCallback;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.BukkitConfigUtil;
import me.pikamug.quests.util.BukkitFakeConversable;
import me.pikamug.quests.util.BukkitItemUtil;
import me.pikamug.quests.util.Language;
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

    public void setSelectedExplosionLocations(
            final Map<UUID, Block> selectedExplosionLocations) {
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

    public void setSelectedLightningLocations(
            final Map<UUID, Block> selectedLightningLocations) {
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
    
    public void loadData(final IAction event, final ConversationContext context) {
        if (event.getMessage() != null) {
            context.setSessionData(Key.E_MESSAGE, event.getMessage());
        }
        if (event.isClearInv()) {
            context.setSessionData(Key.E_CLEAR_INVENTORY, true);
        } else {
            context.setSessionData(Key.E_CLEAR_INVENTORY, false);
        }
        if (event.isFailQuest()) {
            context.setSessionData(Key.E_FAIL_QUEST, true);
        } else {
            context.setSessionData(Key.E_FAIL_QUEST, false);
        }
        if (event.getItems() != null && !event.getItems().isEmpty()) {
            final LinkedList<ItemStack> items = new LinkedList<>(event.getItems());
            context.setSessionData(Key.E_ITEMS, items);
        }
        if (event.getExplosions() != null && !event.getExplosions().isEmpty()) {
            final LinkedList<String> locs = new LinkedList<>();
            for (final Location loc : event.getExplosions()) {
                locs.add(BukkitConfigUtil.getLocationInfo(loc));
            }
            context.setSessionData(Key.E_EXPLOSIONS, locs);
        }
        if (event.getEffects() != null && !event.getEffects().isEmpty()) {
            final LinkedList<String> locs = new LinkedList<>();
            final LinkedList<String> effs = new LinkedList<>();
            for (final Entry<Location, Effect> e : event.getEffects().entrySet()) {
                locs.add(BukkitConfigUtil.getLocationInfo(e.getKey()));
                effs.add(e.getValue().toString());
            }
            context.setSessionData(Key.E_EFFECTS, effs);
            context.setSessionData(Key.E_EFFECTS_LOCATIONS, locs);
        }
        if (event.getStormWorld() != null) {
            context.setSessionData(Key.E_WORLD_STORM, event.getStormWorld().getName());
            context.setSessionData(Key.E_WORLD_STORM_DURATION, event.getStormDuration());
        }
        if (event.getThunderWorld() != null) {
            context.setSessionData(Key.E_WORLD_THUNDER, event.getThunderWorld().getName());
            context.setSessionData(Key.E_WORLD_THUNDER_DURATION, event.getThunderDuration());
        }
        if (event.getMobSpawns() != null && !event.getMobSpawns().isEmpty()) {
            final LinkedList<String> questMobs = new LinkedList<>();
            for (final QuestMob questMob : event.getMobSpawns()) {
                questMobs.add(questMob.serialize());
            }
            context.setSessionData(Key.E_MOB_TYPES, questMobs);
        }
        if (event.getLightningStrikes() != null && !event.getLightningStrikes().isEmpty()) {
            final LinkedList<String> locs = new LinkedList<>();
            for (final Location loc : event.getLightningStrikes()) {
                locs.add(BukkitConfigUtil.getLocationInfo(loc));
            }
            context.setSessionData(Key.E_LIGHTNING, locs);
        }
        if (event.getPotionEffects() != null && !event.getPotionEffects().isEmpty()) {
            final LinkedList<String> types = new LinkedList<>();
            final LinkedList<Long> durations = new LinkedList<>();
            final LinkedList<Integer> mags = new LinkedList<>();
            for (final PotionEffect pe : event.getPotionEffects()) {
                types.add(pe.getType().getName());
                durations.add((long) pe.getDuration());
                mags.add(pe.getAmplifier());
            }
            context.setSessionData(Key.E_POTION_TYPES, types);
            context.setSessionData(Key.E_POTION_DURATIONS, durations);
            context.setSessionData(Key.E_POTION_STRENGTH, mags);
        }
        if (event.getHunger() > -1) {
            context.setSessionData(Key.E_HUNGER, event.getHunger());
        }
        if (event.getSaturation() > -1) {
            context.setSessionData(Key.E_SATURATION, event.getSaturation());
        }
        if (event.getHealth() > -1) {
            context.setSessionData(Key.E_HEALTH, event.getHealth());
        }
        if (event.getTeleport() != null) {
            context.setSessionData(Key.E_TELEPORT, BukkitConfigUtil.getLocationInfo(event.getTeleport()));
        }
        if (event.getCommands() != null) {
            context.setSessionData(Key.E_COMMANDS, event.getCommands());
        }
        if (event.getTimer() > 0) {
            context.setSessionData(Key.E_TIMER, event.getTimer());
        }
        if (event.isCancelTimer()) {
            context.setSessionData(Key.E_CANCEL_TIMER, true);
        }
    }

    public void clearData(final ConversationContext context) {
        context.setSessionData(Key.E_OLD_EVENT, null);
        context.setSessionData(Key.E_NAME, null);
        context.setSessionData(Key.E_MESSAGE, null);
        context.setSessionData(Key.E_CLEAR_INVENTORY, null);
        context.setSessionData(Key.E_FAIL_QUEST, null);
        context.setSessionData(Key.E_ITEMS, null);
        context.setSessionData(Key.E_ITEMS_AMOUNTS, null);
        context.setSessionData(Key.E_EXPLOSIONS, null);
        context.setSessionData(Key.E_EFFECTS, null);
        context.setSessionData(Key.E_EFFECTS_LOCATIONS, null);
        context.setSessionData(Key.E_WORLD_STORM, null);
        context.setSessionData(Key.E_WORLD_STORM_DURATION, null);
        context.setSessionData(Key.E_WORLD_THUNDER, null);
        context.setSessionData(Key.E_WORLD_THUNDER_DURATION, null);
        context.setSessionData(Key.E_MOB_TYPES, null);
        context.setSessionData(Key.E_LIGHTNING, null);
        context.setSessionData(Key.E_POTION_TYPES, null);
        context.setSessionData(Key.E_POTION_DURATIONS, null);
        context.setSessionData(Key.E_POTION_STRENGTH, null);
        context.setSessionData(Key.E_HUNGER, null);
        context.setSessionData(Key.E_SATURATION, null);
        context.setSessionData(Key.E_HEALTH, null);
        context.setSessionData(Key.E_TELEPORT, null);
        context.setSessionData(Key.E_COMMANDS, null);
        context.setSessionData(Key.E_TIMER, null);
        context.setSessionData(Key.E_CANCEL_TIMER, null);
    }

    public void deleteAction(final ConversationContext context) {
        final YamlConfiguration data = new YamlConfiguration();
        final File actionsFile = new File(plugin.getDataFolder(), "actions.yml");
        try {
            data.load(actionsFile);
        } catch (final IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            context.getForWhom().sendRawMessage(ChatColor.RED + Language.get("questErrorReadingFile")
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
            context.getForWhom().sendRawMessage(ChatColor.RED + Language.get("questSaveError"));
            return;
        }
        final ReloadCallback<Boolean> callback = response -> {
            if (!response) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Language.get("unknownError"));
            }
        };
        plugin.reload(callback);
        context.getForWhom().sendRawMessage(ChatColor.YELLOW + Language.get("eventEditorDeleted"));
        if (plugin.getSettings().getConsoleLogging() > 0) {
            final String identifier = context.getForWhom() instanceof Player ? 
                    "Player " + ((Player)context.getForWhom()).getUniqueId() : "CONSOLE";
            plugin.getLogger().info(identifier + " deleted action " + action);
        }
        for (final IQuester q : plugin.getOfflineQuesters()) {
            for (final IQuest quest : q.getCurrentQuestsTemp().keySet()) {
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
            context.getForWhom().sendRawMessage(ChatColor.RED + Language.get("questErrorReadingFile")
                    .replace("<file>", actionsFile.getName()));
            return;
        }
        String key = "actions";
        if (data.getConfigurationSection(key) == null) {
            key = "events";
        }
        if (context.getSessionData(Key.E_OLD_EVENT) != null
                && !((String) Objects.requireNonNull(context.getSessionData(Key.E_OLD_EVENT))).isEmpty()) {
            data.set(key + "." + context.getSessionData(Key.E_OLD_EVENT), null);
            final Collection<IAction> temp = plugin.getLoadedActions();
            temp.remove(plugin.getAction((String) context.getSessionData(Key.E_OLD_EVENT)));
            plugin.setLoadedActions(temp);
        }
        final ConfigurationSection section = data.createSection(key + "." + context.getSessionData(Key.E_NAME));
        editingActionNames.remove((String) context.getSessionData(Key.E_NAME));
        if (context.getSessionData(Key.E_MESSAGE) != null) {
            section.set("message", context.getSessionData(Key.E_MESSAGE));
        }
        if (context.getSessionData(Key.E_CLEAR_INVENTORY) != null) {
            final Boolean b = (Boolean) context.getSessionData(Key.E_CLEAR_INVENTORY);
            if (b != null) {
                section.set("clear-inventory", b);
            }
        }
        if (context.getSessionData(Key.E_FAIL_QUEST) != null) {
            final Boolean b = (Boolean) context.getSessionData(Key.E_FAIL_QUEST);
            if (b != null) {
                section.set("fail-quest", b);
            }
        }
        if (context.getSessionData(Key.E_ITEMS) != null) {
            section.set("items", context.getSessionData(Key.E_ITEMS));
        }
        if (context.getSessionData(Key.E_EXPLOSIONS) != null) {
            section.set("explosions", context.getSessionData(Key.E_EXPLOSIONS));
        }
        if (context.getSessionData(Key.E_EFFECTS) != null) {
            section.set("effects", context.getSessionData(Key.E_EFFECTS));
            section.set("effect-locations", context.getSessionData(Key.E_EFFECTS_LOCATIONS));
        }
        if (context.getSessionData(Key.E_WORLD_STORM) != null) {
            section.set("storm-world", context.getSessionData(Key.E_WORLD_STORM));
            section.set("storm-duration", context.getSessionData(Key.E_WORLD_STORM_DURATION));
        }
        if (context.getSessionData(Key.E_WORLD_THUNDER) != null) {
            section.set("thunder-world", context.getSessionData(Key.E_WORLD_THUNDER));
            section.set("thunder-duration", context.getSessionData(Key.E_WORLD_THUNDER_DURATION));
        }
        try {
            if (context.getSessionData(Key.E_MOB_TYPES) != null) {
                int count = 0;
                for (final String s : (LinkedList<String>) Objects.requireNonNull(context
                        .getSessionData(Key.E_MOB_TYPES))) {
                    ConfigurationSection ss = section.getConfigurationSection("mob-spawns." + count);
                    if (ss == null) {
                        ss = section.createSection("mob-spawns." + count);
                    }
                    final QuestMob questMob = BukkitQuestMob.fromString(s);
                    if (questMob.getName() != null) {
                        ss.set("name", questMob.getName());
                    }
                    ss.set("spawn-location", BukkitConfigUtil.getLocationInfo(questMob.getSpawnLocation()));
                    ss.set("mob-type", questMob.getType().name());
                    ss.set("spawn-amounts", questMob.getSpawnAmounts());
                    ss.set("held-item", BukkitItemUtil.serializeItemStack(questMob.getInventory()[0]));
                    ss.set("held-item-drop-chance", questMob.getDropChances()[0]);
                    ss.set("boots", BukkitItemUtil.serializeItemStack(questMob.getInventory()[1]));
                    ss.set("boots-drop-chance", questMob.getDropChances()[1]);
                    ss.set("leggings", BukkitItemUtil.serializeItemStack(questMob.getInventory()[2]));
                    ss.set("leggings-drop-chance", questMob.getDropChances()[2]);
                    ss.set("chest-plate", BukkitItemUtil.serializeItemStack(questMob.getInventory()[3]));
                    ss.set("chest-plate-drop-chance", questMob.getDropChances()[3]);
                    ss.set("helmet", BukkitItemUtil.serializeItemStack(questMob.getInventory()[4]));
                    ss.set("helmet-drop-chance", questMob.getDropChances()[4]);
                    count++;
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        if (context.getSessionData(Key.E_LIGHTNING) != null) {
            section.set("lightning-strikes", context.getSessionData(Key.E_LIGHTNING));
        }
        if (context.getSessionData(Key.E_COMMANDS) != null) {
            final LinkedList<String> commands = (LinkedList<String>) context.getSessionData(Key.E_COMMANDS);
            if (commands != null && !commands.isEmpty()) {
                section.set("commands", commands);
            }
        }
        if (context.getSessionData(Key.E_POTION_TYPES) != null) {
            section.set("potion-effect-types", context.getSessionData(Key.E_POTION_TYPES));
            section.set("potion-effect-durations", context.getSessionData(Key.E_POTION_DURATIONS));
            section.set("potion-effect-amplifiers", context.getSessionData(Key.E_POTION_STRENGTH));
        }
        if (context.getSessionData(Key.E_HUNGER) != null) {
            section.set("hunger", context.getSessionData(Key.E_HUNGER));
        }
        if (context.getSessionData(Key.E_SATURATION) != null) {
            section.set("saturation", context.getSessionData(Key.E_SATURATION));
        }
        if (context.getSessionData(Key.E_HEALTH) != null) {
            section.set("health", context.getSessionData(Key.E_HEALTH));
        }
        if (context.getSessionData(Key.E_TELEPORT) != null) {
            section.set("teleport-location", context.getSessionData(Key.E_TELEPORT));
        }
        if (context.getSessionData(Key.E_TIMER) != null) {
            final Integer i = (Integer) context.getSessionData(Key.E_TIMER);
            if (i != null && i > 0) {
                section.set("timer", context.getSessionData(Key.E_TIMER));
            }
        }
        if (context.getSessionData(Key.E_CANCEL_TIMER) != null) {
            final Boolean b = (Boolean) context.getSessionData(Key.E_CANCEL_TIMER);
            if (b != null) {
                section.set("cancel-timer", b);
            }
        }
        if (context.getSessionData(Key.E_DENIZEN) != null) {
            section.set("denizen-script", context.getSessionData(Key.E_DENIZEN));
        }
        try {
            data.save(actionsFile);
        } catch (final IOException e) {
            context.getForWhom().sendRawMessage(ChatColor.RED + Language.get("questSaveError"));
            return;
        }
        final ReloadCallback<Boolean> callback = response -> {
            if (!response) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Language.get("unknownError"));
            }
        };
        plugin.reload(callback);
        context.getForWhom().sendRawMessage(ChatColor.YELLOW + Language.get("eventEditorSaved"));
        if (plugin.getSettings().getConsoleLogging() > 0) {
            final String identifier = context.getForWhom() instanceof Player ? 
                    "Player " + ((Player)context.getForWhom()).getUniqueId() : "CONSOLE";
            plugin.getLogger().info(identifier + " saved action " + context.getSessionData(Key.E_NAME));
        }
        for (final IQuester q : plugin.getOfflineQuesters()) {
            for (final IQuest quest : q.getCurrentQuestsTemp().keySet()) {
                q.checkQuest(quest);
            }
        }
        clearData(context);
    }
}