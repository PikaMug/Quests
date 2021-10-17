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

package me.blackvein.quests.actions;

import me.blackvein.quests.Quest;
import me.blackvein.quests.QuestMob;
import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;
import me.blackvein.quests.convo.actions.main.ActionMainPrompt;
import me.blackvein.quests.convo.actions.menu.ActionMenuPrompt;
import me.blackvein.quests.interfaces.ReloadCallback;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.ConfigUtil;
import me.blackvein.quests.util.FakeConversable;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
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

public class ActionFactory implements ConversationAbandonedListener {

    private final Quests plugin;
    private final ConversationFactory conversationFactory;
    private Map<UUID, Block> selectedExplosionLocations = new HashMap<>();
    private Map<UUID, Block> selectedEffectLocations = new HashMap<>();
    private Map<UUID, Block> selectedMobLocations = new HashMap<>();
    private Map<UUID, Block> selectedLightningLocations = new HashMap<>();
    private Map<UUID, Block> selectedTeleportLocations = new HashMap<>();
    private List<String> editingActionNames = new LinkedList<>();

    public ActionFactory(final Quests plugin) {
        this.plugin = plugin;
        // Ensure to initialize factory last so that 'this' is fully initialized before it is passed
        this.conversationFactory = new ConversationFactory(plugin).withModality(false).withLocalEcho(false)
                .withFirstPrompt(new ActionMenuPrompt(new ConversationContext(plugin, new FakeConversable(),
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
    
    public void loadData(final Action event, final ConversationContext context) {
        if (event.message != null) {
            context.setSessionData(CK.E_MESSAGE, event.message);
        }
        if (event.clearInv) {
            context.setSessionData(CK.E_CLEAR_INVENTORY, Lang.get("yesWord"));
        } else {
            context.setSessionData(CK.E_CLEAR_INVENTORY, Lang.get("noWord"));
        }
        if (event.failQuest) {
            context.setSessionData(CK.E_FAIL_QUEST, Lang.get("yesWord"));
        } else {
            context.setSessionData(CK.E_FAIL_QUEST, Lang.get("noWord"));
        }
        if (event.items != null && !event.items.isEmpty()) {
            final LinkedList<ItemStack> items = new LinkedList<>(event.items);
            context.setSessionData(CK.E_ITEMS, items);
        }
        if (event.explosions != null && !event.explosions.isEmpty()) {
            final LinkedList<String> locs = new LinkedList<>();
            for (final Location loc : event.explosions) {
                locs.add(ConfigUtil.getLocationInfo(loc));
            }
            context.setSessionData(CK.E_EXPLOSIONS, locs);
        }
        if (event.effects != null && !event.effects.isEmpty()) {
            final LinkedList<String> locs = new LinkedList<>();
            final LinkedList<String> effs = new LinkedList<>();
            for (final Entry<Location, Effect> e : event.effects.entrySet()) {
                locs.add(ConfigUtil.getLocationInfo(e.getKey()));
                effs.add(e.getValue().toString());
            }
            context.setSessionData(CK.E_EFFECTS, effs);
            context.setSessionData(CK.E_EFFECTS_LOCATIONS, locs);
        }
        if (event.stormWorld != null) {
            context.setSessionData(CK.E_WORLD_STORM, event.stormWorld.getName());
            context.setSessionData(CK.E_WORLD_STORM_DURATION, event.stormDuration);
        }
        if (event.thunderWorld != null) {
            context.setSessionData(CK.E_WORLD_THUNDER, event.thunderWorld.getName());
            context.setSessionData(CK.E_WORLD_THUNDER_DURATION, event.thunderDuration);
        }
        if (event.mobSpawns != null && !event.mobSpawns.isEmpty()) {
            final LinkedList<String> questMobs = new LinkedList<>();
            for (final QuestMob questMob : event.mobSpawns) {
                questMobs.add(questMob.serialize());
            }
            context.setSessionData(CK.E_MOB_TYPES, questMobs);
        }
        if (event.lightningStrikes != null && !event.lightningStrikes.isEmpty()) {
            final LinkedList<String> locs = new LinkedList<>();
            for (final Location loc : event.lightningStrikes) {
                locs.add(ConfigUtil.getLocationInfo(loc));
            }
            context.setSessionData(CK.E_LIGHTNING, locs);
        }
        if (event.potionEffects != null && !event.potionEffects.isEmpty()) {
            final LinkedList<String> types = new LinkedList<>();
            final LinkedList<Long> durations = new LinkedList<>();
            final LinkedList<Integer> mags = new LinkedList<>();
            for (final PotionEffect pe : event.potionEffects) {
                types.add(pe.getType().getName());
                durations.add((long) pe.getDuration());
                mags.add(pe.getAmplifier());
            }
            context.setSessionData(CK.E_POTION_TYPES, types);
            context.setSessionData(CK.E_POTION_DURATIONS, durations);
            context.setSessionData(CK.E_POTION_STRENGTH, mags);
        }
        if (event.hunger > -1) {
            context.setSessionData(CK.E_HUNGER, event.hunger);
        }
        if (event.saturation > -1) {
            context.setSessionData(CK.E_SATURATION, event.saturation);
        }
        if (event.health > -1) {
            context.setSessionData(CK.E_HEALTH, event.health);
        }
        if (event.teleport != null) {
            context.setSessionData(CK.E_TELEPORT, ConfigUtil.getLocationInfo(event.teleport));
        }
        if (event.commands != null) {
            context.setSessionData(CK.E_COMMANDS, event.commands);
        }
        if (event.timer > 0) {
            context.setSessionData(CK.E_TIMER, event.timer);
        }
        if (event.cancelTimer) {
            context.setSessionData(CK.E_CANCEL_TIMER, true);
        }
    }

    public void clearData(final ConversationContext context) {
        context.setSessionData(CK.E_OLD_EVENT, null);
        context.setSessionData(CK.E_NAME, null);
        context.setSessionData(CK.E_MESSAGE, null);
        context.setSessionData(CK.E_CLEAR_INVENTORY, null);
        context.setSessionData(CK.E_FAIL_QUEST, null);
        context.setSessionData(CK.E_ITEMS, null);
        context.setSessionData(CK.E_ITEMS_AMOUNTS, null);
        context.setSessionData(CK.E_EXPLOSIONS, null);
        context.setSessionData(CK.E_EFFECTS, null);
        context.setSessionData(CK.E_EFFECTS_LOCATIONS, null);
        context.setSessionData(CK.E_WORLD_STORM, null);
        context.setSessionData(CK.E_WORLD_STORM_DURATION, null);
        context.setSessionData(CK.E_WORLD_THUNDER, null);
        context.setSessionData(CK.E_WORLD_THUNDER_DURATION, null);
        context.setSessionData(CK.E_MOB_TYPES, null);
        context.setSessionData(CK.E_LIGHTNING, null);
        context.setSessionData(CK.E_POTION_TYPES, null);
        context.setSessionData(CK.E_POTION_DURATIONS, null);
        context.setSessionData(CK.E_POTION_STRENGTH, null);
        context.setSessionData(CK.E_HUNGER, null);
        context.setSessionData(CK.E_SATURATION, null);
        context.setSessionData(CK.E_HEALTH, null);
        context.setSessionData(CK.E_TELEPORT, null);
        context.setSessionData(CK.E_COMMANDS, null);
        context.setSessionData(CK.E_TIMER, null);
        context.setSessionData(CK.E_CANCEL_TIMER, null);
    }

    public void deleteAction(final ConversationContext context) {
        final YamlConfiguration data = new YamlConfiguration();
        final File actionsFile = new File(plugin.getDataFolder(), "actions.yml");
        try {
            data.load(actionsFile);
        } catch (final IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questErrorReadingFile")
                    .replace("<file>", actionsFile.getName()));
            return;
        }
        final String action = (String) context.getSessionData(CK.ED_EVENT_DELETE);
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
            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questSaveError"));
            return;
        }
        final ReloadCallback<Boolean> callback = response -> {
            if (!response) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("unknownError"));
            }
        };
        plugin.reload(callback);
        context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("eventEditorDeleted"));
        if (plugin.getSettings().getConsoleLogging() > 0) {
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
            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questErrorReadingFile")
                    .replace("<file>", actionsFile.getName()));
            return;
        }
        String key = "actions";
        if (data.getConfigurationSection(key) == null) {
            key = "events";
        }
        if (context.getSessionData(CK.E_OLD_EVENT) != null
                && !((String) Objects.requireNonNull(context.getSessionData(CK.E_OLD_EVENT))).isEmpty()) {
            data.set(key + "." + context.getSessionData(CK.E_OLD_EVENT), null);
            final Collection<Action> temp = plugin.getLoadedActions();
            temp.remove(plugin.getAction((String) context.getSessionData(CK.E_OLD_EVENT)));
            plugin.setLoadedActions(temp);
        }
        final ConfigurationSection section = data.createSection(key + "." + context.getSessionData(CK.E_NAME));
        editingActionNames.remove((String) context.getSessionData(CK.E_NAME));
        if (context.getSessionData(CK.E_MESSAGE) != null) {
            section.set("message", context.getSessionData(CK.E_MESSAGE));
        }
        if (context.getSessionData(CK.E_CLEAR_INVENTORY) != null) {
            final String s = (String) context.getSessionData(CK.E_CLEAR_INVENTORY);
            if (s != null && s.equalsIgnoreCase(Lang.get("yesWord"))) {
                section.set("clear-inventory", true);
            }
        }
        if (context.getSessionData(CK.E_FAIL_QUEST) != null) {
            final String s = (String) context.getSessionData(CK.E_FAIL_QUEST);
            if (s != null && s.equalsIgnoreCase(Lang.get("yesWord"))) {
                section.set("fail-quest", true);
            }
        }
        if (context.getSessionData(CK.E_ITEMS) != null) {
            section.set("items", context.getSessionData(CK.E_ITEMS));
        }
        if (context.getSessionData(CK.E_EXPLOSIONS) != null) {
            section.set("explosions", context.getSessionData(CK.E_EXPLOSIONS));
        }
        if (context.getSessionData(CK.E_EFFECTS) != null) {
            section.set("effects", context.getSessionData(CK.E_EFFECTS));
            section.set("effect-locations", context.getSessionData(CK.E_EFFECTS_LOCATIONS));
        }
        if (context.getSessionData(CK.E_WORLD_STORM) != null) {
            section.set("storm-world", context.getSessionData(CK.E_WORLD_STORM));
            section.set("storm-duration", context.getSessionData(CK.E_WORLD_STORM_DURATION));
        }
        if (context.getSessionData(CK.E_WORLD_THUNDER) != null) {
            section.set("thunder-world", context.getSessionData(CK.E_WORLD_THUNDER));
            section.set("thunder-duration", context.getSessionData(CK.E_WORLD_THUNDER_DURATION));
        }
        try {
            if (context.getSessionData(CK.E_MOB_TYPES) != null) {
                int count = 0;
                for (final String s : (LinkedList<String>) Objects.requireNonNull(context
                        .getSessionData(CK.E_MOB_TYPES))) {
                    ConfigurationSection ss = section.getConfigurationSection("mob-spawns." + count);
                    if (ss == null) {
                        ss = section.createSection("mob-spawns." + count);
                    }
                    final QuestMob questMob = QuestMob.fromString(s);
                    if (questMob.getName() != null) {
                        ss.set("name", questMob.getName());
                    }
                    ss.set("spawn-location", ConfigUtil.getLocationInfo(questMob.getSpawnLocation()));
                    ss.set("mob-type", questMob.getType().name());
                    ss.set("spawn-amounts", questMob.getSpawnAmounts());
                    ss.set("held-item", ItemUtil.serializeItemStack(questMob.getInventory()[0]));
                    ss.set("held-item-drop-chance", questMob.getDropChances()[0]);
                    ss.set("boots", ItemUtil.serializeItemStack(questMob.getInventory()[1]));
                    ss.set("boots-drop-chance", questMob.getDropChances()[1]);
                    ss.set("leggings", ItemUtil.serializeItemStack(questMob.getInventory()[2]));
                    ss.set("leggings-drop-chance", questMob.getDropChances()[2]);
                    ss.set("chest-plate", ItemUtil.serializeItemStack(questMob.getInventory()[3]));
                    ss.set("chest-plate-drop-chance", questMob.getDropChances()[3]);
                    ss.set("helmet", ItemUtil.serializeItemStack(questMob.getInventory()[4]));
                    ss.set("helmet-drop-chance", questMob.getDropChances()[4]);
                    count++;
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        if (context.getSessionData(CK.E_LIGHTNING) != null) {
            section.set("lightning-strikes", context.getSessionData(CK.E_LIGHTNING));
        }
        if (context.getSessionData(CK.E_COMMANDS) != null) {
            final LinkedList<String> commands = (LinkedList<String>) context.getSessionData(CK.E_COMMANDS);
            if (commands != null && !commands.isEmpty()) {
                section.set("commands", commands);
            }
        }
        if (context.getSessionData(CK.E_POTION_TYPES) != null) {
            section.set("potion-effect-types", context.getSessionData(CK.E_POTION_TYPES));
            section.set("potion-effect-durations", context.getSessionData(CK.E_POTION_DURATIONS));
            section.set("potion-effect-amplifiers", context.getSessionData(CK.E_POTION_STRENGTH));
        }
        if (context.getSessionData(CK.E_HUNGER) != null) {
            section.set("hunger", context.getSessionData(CK.E_HUNGER));
        }
        if (context.getSessionData(CK.E_SATURATION) != null) {
            section.set("saturation", context.getSessionData(CK.E_SATURATION));
        }
        if (context.getSessionData(CK.E_HEALTH) != null) {
            section.set("health", context.getSessionData(CK.E_HEALTH));
        }
        if (context.getSessionData(CK.E_TELEPORT) != null) {
            section.set("teleport-location", context.getSessionData(CK.E_TELEPORT));
        }
        if (context.getSessionData(CK.E_TIMER) != null) {
            final Integer i = (Integer) context.getSessionData(CK.E_TIMER);
            if (i != null && i > 0) {
                section.set("timer", context.getSessionData(CK.E_TIMER));
            }
        }
        if (context.getSessionData(CK.E_CANCEL_TIMER) != null) {
            final String s = (String) context.getSessionData(CK.E_CANCEL_TIMER);
            if (s != null && s.equalsIgnoreCase(Lang.get("yesWord"))) {
                section.set("cancel-timer", true);
            }
        }
        if (context.getSessionData(CK.E_DENIZEN) != null) {
            section.set("denizen-script", context.getSessionData(CK.E_DENIZEN));
        }
        try {
            data.save(actionsFile);
        } catch (final IOException e) {
            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questSaveError"));
            return;
        }
        final ReloadCallback<Boolean> callback = response -> {
            if (!response) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("unknownError"));
            }
        };
        plugin.reload(callback);
        context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("eventEditorSaved"));
        if (plugin.getSettings().getConsoleLogging() > 0) {
            final String identifier = context.getForWhom() instanceof Player ? 
                    "Player " + ((Player)context.getForWhom()).getUniqueId() : "CONSOLE";
            plugin.getLogger().info(identifier + " saved action " + context.getSessionData(CK.E_NAME));
        }
        for (final Quester q : plugin.getOfflineQuesters()) {
            for (final Quest quest : q.getCurrentQuests().keySet()) {
                q.checkQuest(quest);
            }
        }
        clearData(context);
    }
}