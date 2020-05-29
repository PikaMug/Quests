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
import java.util.Map.Entry;
import java.util.UUID;

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
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import me.blackvein.quests.Quest;
import me.blackvein.quests.QuestMob;
import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;
import me.blackvein.quests.convo.actions.main.ActionMainPrompt;
import me.blackvein.quests.convo.actions.menu.ActionMenuPrompt;
import me.blackvein.quests.interfaces.ReloadCallback;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.ConfigUtil;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;

public class ActionFactory implements ConversationAbandonedListener {

    private final Quests plugin;
    private final ConversationFactory convoCreator;
    private Map<UUID, Block> selectedExplosionLocations = new HashMap<UUID, Block>();
    private Map<UUID, Block> selectedEffectLocations = new HashMap<UUID, Block>();
    private Map<UUID, Block> selectedMobLocations = new HashMap<UUID, Block>();
    private Map<UUID, Block> selectedLightningLocations = new HashMap<UUID, Block>();
    private Map<UUID, Block> selectedTeleportLocations = new HashMap<UUID, Block>();
    private List<String> editingActionNames = new LinkedList<String>();

    public ActionFactory(Quests plugin) {
        this.plugin = plugin;
        // Ensure to initialize convoCreator last so that 'this' is fully initialized before it is passed
        this.convoCreator = new ConversationFactory(plugin).withModality(false).withLocalEcho(false)
                .withFirstPrompt(new ActionMenuPrompt(new ConversationContext(plugin, null, null))).withTimeout(3600)
                .thatExcludesNonPlayersWithMessage("Console may not perform this operation!")
                .addConversationAbandonedListener(this);
    }
    
    public Map<UUID, Block> getSelectedExplosionLocations() {
        return selectedExplosionLocations;
    }

    public void setSelectedExplosionLocations(
            Map<UUID, Block> selectedExplosionLocations) {
        this.selectedExplosionLocations = selectedExplosionLocations;
    }

    public Map<UUID, Block> getSelectedEffectLocations() {
        return selectedEffectLocations;
    }

    public void setSelectedEffectLocations(Map<UUID, Block> selectedEffectLocations) {
        this.selectedEffectLocations = selectedEffectLocations;
    }

    public Map<UUID, Block> getSelectedMobLocations() {
        return selectedMobLocations;
    }

    public void setSelectedMobLocations(Map<UUID, Block> selectedMobLocations) {
        this.selectedMobLocations = selectedMobLocations;
    }

    public Map<UUID, Block> getSelectedLightningLocations() {
        return selectedLightningLocations;
    }

    public void setSelectedLightningLocations(
            Map<UUID, Block> selectedLightningLocations) {
        this.selectedLightningLocations = selectedLightningLocations;
    }

    public Map<UUID, Block> getSelectedTeleportLocations() {
        return selectedTeleportLocations;
    }

    public void setSelectedTeleportLocations(
            Map<UUID, Block> selectedTeleportLocations) {
        this.selectedTeleportLocations = selectedTeleportLocations;
    }

    public ConversationFactory getConversationFactory() {
        return convoCreator;
    }
    
    public List<String> getNamesOfActionsBeingEdited() {
        return editingActionNames;
    }
    
    public void setNamesOfActionsBeingEdited(List<String> actionNames) {
        this.editingActionNames = actionNames;
    }

    @Override
    public void conversationAbandoned(ConversationAbandonedEvent abandonedEvent) {
        Player player = (Player) abandonedEvent.getContext().getForWhom();
        selectedExplosionLocations.remove(player.getUniqueId());
        selectedEffectLocations.remove(player.getUniqueId());
        selectedMobLocations.remove(player.getUniqueId());
        selectedLightningLocations.remove(player.getUniqueId());
        selectedTeleportLocations.remove(player.getUniqueId());
    }
    
    public Prompt returnToMenu(ConversationContext context) {
        return new ActionMainPrompt(context);
    }
    
    public void loadData(Action event, ConversationContext context) {
        if (event.message != null) {
            context.setSessionData(CK.E_MESSAGE, event.message);
        }
        if (event.clearInv == true) {
            context.setSessionData(CK.E_CLEAR_INVENTORY, Lang.get("yesWord"));
        } else {
            context.setSessionData(CK.E_CLEAR_INVENTORY, Lang.get("noWord"));
        }
        if (event.failQuest == true) {
            context.setSessionData(CK.E_FAIL_QUEST, Lang.get("yesWord"));
        } else {
            context.setSessionData(CK.E_FAIL_QUEST, Lang.get("noWord"));
        }
        if (event.items != null && event.items.isEmpty() == false) {
            LinkedList<ItemStack> items = new LinkedList<ItemStack>();
            items.addAll(event.items);
            context.setSessionData(CK.E_ITEMS, items);
        }
        if (event.explosions != null && event.explosions.isEmpty() == false) {
            LinkedList<String> locs = new LinkedList<String>();
            for (Location loc : event.explosions) {
                locs.add(ConfigUtil.getLocationInfo(loc));
            }
            context.setSessionData(CK.E_EXPLOSIONS, locs);
        }
        if (event.effects != null && event.effects.isEmpty() == false) {
            LinkedList<String> locs = new LinkedList<String>();
            LinkedList<String> effs = new LinkedList<String>();
            for (Entry<Location, Effect> e : event.effects.entrySet()) {
                locs.add(ConfigUtil.getLocationInfo((Location) e.getKey()));
                effs.add(((Effect) e.getValue()).toString());
            }
            context.setSessionData(CK.E_EFFECTS, effs);
            context.setSessionData(CK.E_EFFECTS_LOCATIONS, locs);
        }
        if (event.stormWorld != null) {
            context.setSessionData(CK.E_WORLD_STORM, event.stormWorld.getName());
            context.setSessionData(CK.E_WORLD_STORM_DURATION, (int) event.stormDuration);
        }
        if (event.thunderWorld != null) {
            context.setSessionData(CK.E_WORLD_THUNDER, event.thunderWorld.getName());
            context.setSessionData(CK.E_WORLD_THUNDER_DURATION, (int) event.thunderDuration);
        }
        if (event.mobSpawns != null && event.mobSpawns.isEmpty() == false) {
            LinkedList<String> questMobs = new LinkedList<String>();
            for (QuestMob questMob : event.mobSpawns) {
                questMobs.add(questMob.serialize());
            }
            context.setSessionData(CK.E_MOB_TYPES, questMobs);
        }
        if (event.lightningStrikes != null && event.lightningStrikes.isEmpty() == false) {
            LinkedList<String> locs = new LinkedList<String>();
            for (Location loc : event.lightningStrikes) {
                locs.add(ConfigUtil.getLocationInfo(loc));
            }
            context.setSessionData(CK.E_LIGHTNING, locs);
        }
        if (event.potionEffects != null && event.potionEffects.isEmpty() == false) {
            LinkedList<String> types = new LinkedList<String>();
            LinkedList<Long> durations = new LinkedList<Long>();
            LinkedList<Integer> mags = new LinkedList<Integer>();
            for (PotionEffect pe : event.potionEffects) {
                types.add(pe.getType().getName());
                durations.add((long) pe.getDuration());
                mags.add(pe.getAmplifier());
            }
            context.setSessionData(CK.E_POTION_TYPES, types);
            context.setSessionData(CK.E_POTION_DURATIONS, durations);
            context.setSessionData(CK.E_POTION_STRENGHT, mags);
        }
        if (event.hunger > -1) {
            context.setSessionData(CK.E_HUNGER, (Integer) event.hunger);
        }
        if (event.saturation > -1) {
            context.setSessionData(CK.E_SATURATION, (Integer) event.saturation);
        }
        if (event.health > -1) {
            context.setSessionData(CK.E_HEALTH, (Float) event.health);
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

    public void clearData(ConversationContext context) {
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
        context.setSessionData(CK.E_POTION_STRENGHT, null);
        context.setSessionData(CK.E_HUNGER, null);
        context.setSessionData(CK.E_SATURATION, null);
        context.setSessionData(CK.E_HEALTH, null);
        context.setSessionData(CK.E_TELEPORT, null);
        context.setSessionData(CK.E_COMMANDS, null);
        context.setSessionData(CK.E_TIMER, null);
        context.setSessionData(CK.E_CANCEL_TIMER, null);
    }

    public void deleteAction(ConversationContext context) {
        YamlConfiguration data = new YamlConfiguration();
        File actionsFile = new File(plugin.getDataFolder(), "actions.yml");
        try {
            data.load(actionsFile);
        } catch (IOException e) {
            e.printStackTrace();
            ((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("questErrorReadingFile")
                    .replace("<file>", actionsFile.getName()));
            return;
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
            ((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("questErrorReadingFile")
                    .replace("<file>", actionsFile.getName()));
            return;
        }
        String action = (String) context.getSessionData(CK.ED_EVENT_DELETE);
        String key = "actions";
        ConfigurationSection sec = data.getConfigurationSection(key);
        if (sec == null) {
            key = "events";
            sec = data.getConfigurationSection(key);
        }
        sec.set(action, null);
        try {
            data.save(actionsFile);
        } catch (IOException e) {
            ((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("eventEditorErrorSaving"));
            return;
        }
        ReloadCallback<Boolean> callback = new ReloadCallback<Boolean>() {
            public void execute(Boolean response) {
                if (!response) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("unknownError"));
                }
            }
        };
        plugin.reload(callback);
        ((Player) context.getForWhom()).sendMessage(ChatColor.YELLOW + Lang.get("eventEditorDeleted"));
        for (Quester q : plugin.getQuesters()) {
            for (Quest quest : q.getCurrentQuests().keySet()) {
                q.checkQuest(quest);
            }
        }
        clearData(context);
    }

    @SuppressWarnings("unchecked")
    public void saveAction(ConversationContext context) {
        YamlConfiguration data = new YamlConfiguration();
        File actionsFile = new File(plugin.getDataFolder(), "actions.yml");
        try {
            data.load(actionsFile);
        } catch (IOException e) {
            e.printStackTrace();
            ((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("questErrorReadingFile")
                    .replace("<file>", actionsFile.getName()));
            return;
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
            ((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("questErrorReadingFile")
                    .replace("<file>", actionsFile.getName()));
            return;
        }
        String key = "actions";
        ConfigurationSection sec = data.getConfigurationSection(key);
        if (sec == null) {
            key = "events";
            sec = data.getConfigurationSection(key);
        }
        if (((String) context.getSessionData(CK.E_OLD_EVENT)).isEmpty() == false) {
            data.set(key + "." + (String) context.getSessionData(CK.E_OLD_EVENT), null);
            LinkedList<Action> temp = plugin.getActions();
            temp.remove(plugin.getAction((String) context.getSessionData(CK.E_OLD_EVENT)));
            plugin.setActions(temp);
        }
        ConfigurationSection section = data.createSection(key + "." + (String) context.getSessionData(CK.E_NAME));
        editingActionNames.remove((String) context.getSessionData(CK.E_NAME));
        if (context.getSessionData(CK.E_MESSAGE) != null) {
            section.set("message", (String) context.getSessionData(CK.E_MESSAGE));
        }
        if (context.getSessionData(CK.E_CLEAR_INVENTORY) != null) {
            String s = (String) context.getSessionData(CK.E_CLEAR_INVENTORY);
            if (s.equalsIgnoreCase(Lang.get("yesWord"))) {
                section.set("clear-inventory", true);
            }
        }
        if (context.getSessionData(CK.E_FAIL_QUEST) != null) {
            String s = (String) context.getSessionData(CK.E_FAIL_QUEST);
            if (s.equalsIgnoreCase(Lang.get("yesWord"))) {
                section.set("fail-quest", true);
            }
        }
        if (context.getSessionData(CK.E_ITEMS) != null) {
            section.set("items", (LinkedList<ItemStack>) context.getSessionData(CK.E_ITEMS));
        }
        if (context.getSessionData(CK.E_EXPLOSIONS) != null) {
            section.set("explosions", (LinkedList<String>) context.getSessionData(CK.E_EXPLOSIONS));
        }
        if (context.getSessionData(CK.E_EFFECTS) != null) {
            section.set("effects", (LinkedList<String>) context.getSessionData(CK.E_EFFECTS));
            section.set("effect-locations", (LinkedList<String>) context.getSessionData(CK.E_EFFECTS_LOCATIONS));
        }
        if (context.getSessionData(CK.E_WORLD_STORM) != null) {
            section.set("storm-world", (String) context.getSessionData(CK.E_WORLD_STORM));
            section.set("storm-duration", (Integer) context.getSessionData(CK.E_WORLD_STORM_DURATION));
        }
        if (context.getSessionData(CK.E_WORLD_THUNDER) != null) {
            section.set("thunder-world", (String) context.getSessionData(CK.E_WORLD_THUNDER));
            section.set("thunder-duration", (Integer) context.getSessionData(CK.E_WORLD_THUNDER_DURATION));
        }
        try {
            if (context.getSessionData(CK.E_MOB_TYPES) != null) {
                int count = 0;
                for (String s : (LinkedList<String>) context.getSessionData(CK.E_MOB_TYPES)) {
                    ConfigurationSection ss = section.getConfigurationSection("mob-spawns." + count);
                    if (ss == null) {
                        ss = section.createSection("mob-spawns." + count);
                    }
                    QuestMob questMob = QuestMob.fromString(s);
                    if (questMob == null) {
                        continue;
                    }
                    ss.set("name", questMob.getName());
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (context.getSessionData(CK.E_LIGHTNING) != null) {
            section.set("lightning-strikes", (LinkedList<String>) context.getSessionData(CK.E_LIGHTNING));
        }
        if (context.getSessionData(CK.E_COMMANDS) != null) {
            LinkedList<String> commands = (LinkedList<String>) context.getSessionData(CK.E_COMMANDS);
            if (commands.isEmpty() == false) {
                section.set("commands", commands);
            }
        }
        if (context.getSessionData(CK.E_POTION_TYPES) != null) {
            section.set("potion-effect-types", (LinkedList<String>) context.getSessionData(CK.E_POTION_TYPES));
            section.set("potion-effect-durations", (LinkedList<Long>) context.getSessionData(CK.E_POTION_DURATIONS));
            section.set("potion-effect-amplifiers", (LinkedList<Integer>) context.getSessionData(CK.E_POTION_STRENGHT));
        }
        if (context.getSessionData(CK.E_HUNGER) != null) {
            section.set("hunger", (Integer) context.getSessionData(CK.E_HUNGER));
        }
        if (context.getSessionData(CK.E_SATURATION) != null) {
            section.set("saturation", (Integer) context.getSessionData(CK.E_SATURATION));
        }
        if (context.getSessionData(CK.E_HEALTH) != null) {
            section.set("health", (Integer) context.getSessionData(CK.E_HEALTH));
        }
        if (context.getSessionData(CK.E_TELEPORT) != null) {
            section.set("teleport-location", (String) context.getSessionData(CK.E_TELEPORT));
        }
        if (context.getSessionData(CK.E_TIMER) != null && (int) context.getSessionData(CK.E_TIMER) > 0) {
            section.set("timer", (Integer) context.getSessionData(CK.E_TIMER));
        }
        if (context.getSessionData(CK.E_CANCEL_TIMER) != null) {
            String s = (String) context.getSessionData(CK.E_CANCEL_TIMER);
            if (s.equalsIgnoreCase(Lang.get("yesWord"))) {
                section.set("cancel-timer", true);
            }
        }
        if (context.getSessionData(CK.E_DENIZEN) != null) {
            section.set("denizen-script", (String) context.getSessionData(CK.E_DENIZEN));
        }
        try {
            data.save(actionsFile);
        } catch (IOException e) {
            ((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("eventEditorErrorSaving"));
            return;
        }
        ReloadCallback<Boolean> callback = new ReloadCallback<Boolean>() {
            public void execute(Boolean response) {
                if (!response) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("unknownError"));
                }
            }
        };
        plugin.reload(callback);
        ((Player) context.getForWhom()).sendMessage(ChatColor.YELLOW + Lang.get("eventEditorSaved"));
        for (Quester q : plugin.getQuesters()) {
            for (Quest quest : q.getCurrentQuests().keySet()) {
                q.checkQuest(quest);
            }
        }
        clearData(context);
    }
}