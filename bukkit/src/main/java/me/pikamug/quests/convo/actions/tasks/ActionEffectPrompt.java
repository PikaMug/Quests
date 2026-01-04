/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.convo.actions.tasks;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.convo.actions.ActionsEditorIntegerPrompt;
import me.pikamug.quests.convo.actions.ActionsEditorStringPrompt;
import me.pikamug.quests.convo.actions.main.ActionMainPrompt;
import me.pikamug.quests.events.editor.actions.BukkitActionsEditorPostOpenNumericPromptEvent;
import me.pikamug.quests.events.editor.actions.BukkitActionsEditorPostOpenStringPromptEvent;
import me.pikamug.quests.util.BukkitConfigUtil;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.quests.util.BukkitMiscUtil;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.SessionData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ActionEffectPrompt extends ActionsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final BukkitQuestsPlugin plugin;
    
    public ActionEffectPrompt(final @NotNull UUID uuid) {
        super(uuid);
        this.uuid = uuid;
        this.plugin = BukkitQuestsPlugin.getInstance();
    }
    
    private final int size = 3;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle() {
        return BukkitLang.get("eventEditorEffect");
    }
    
    @Override
    public ChatColor getNumberColor(final int number) {
        switch (number) {
        case 1:
        case 2:
            return ChatColor.BLUE;
        case 3:
            return ChatColor.GREEN;
        default:
            return null;
        }
    }
    
    @Override
    public String getSelectionText(final int number) {
        switch (number) {
        case 1:
            return ChatColor.YELLOW + BukkitLang.get("eventEditorSetEffects");
        case 2:
            return ChatColor.YELLOW + BukkitLang.get("eventEditorSetExplosions");
        case 3:
            return ChatColor.GREEN + BukkitLang.get("done");
        default:
            return null;
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public String getAdditionalText(final int number) {
        switch (number) {
        case 1:
            if (SessionData.get(uuid, Key.A_EFFECTS) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> effects = (LinkedList<String>) SessionData.get(uuid, Key.A_EFFECTS);
                final LinkedList<String> locations
                        = (LinkedList<String>) SessionData.get(uuid, Key.A_EFFECTS_LOCATIONS);
                if (effects != null && locations != null) {
                    for (final String effect : effects) {
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(effect)
                                .append(ChatColor.GRAY).append(" at ").append(ChatColor.DARK_AQUA)
                                .append(locations.get(effects.indexOf(effect)));
                    }
                }
                return text.toString();
            }
        case 2:
            if (SessionData.get(uuid, Key.A_EXPLOSIONS) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> locations = (LinkedList<String>) SessionData.get(uuid, Key.A_EXPLOSIONS);
                if (locations != null) {
                    for (final String loc : locations) {
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(loc);
                    }
                }
                return text.toString();
            }
        case 3:
            return "";
        default:
            return null;
        }
    }

    @Override
    public @NotNull String getPromptText() {
        final BukkitActionsEditorPostOpenNumericPromptEvent event
                = new BukkitActionsEditorPostOpenNumericPromptEvent(uuid, this);
        plugin.getServer().getPluginManager().callEvent(event);
        
        final StringBuilder text = new StringBuilder(ChatColor.GOLD + "- " + getTitle() + " -");
        for (int i = 1; i <= size; i++) {
            text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                    .append(ChatColor.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                    .append(getAdditionalText(i));
        }
        return text.toString();
    }

    @Override
    public void acceptInput(final Number input) {
        final CommandSender sender = Bukkit.getEntity(uuid);
        switch (input.intValue()) {
        case 1:
            new ActionEffectSoundListPrompt(uuid).start();
        case 2:
            if (sender instanceof Player) {
                final ConcurrentHashMap<UUID, Block> selectedExplosionLocations
                        = plugin.getActionFactory().getSelectedExplosionLocations();
                if (BukkitMiscUtil.getWorlds().isEmpty()) {
                    sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("unknownError"));
                    new ActionEffectPrompt(uuid).start();
                }
                selectedExplosionLocations.put(((Player) sender).getUniqueId(),
                        Bukkit.getWorlds().get(0).getBlockAt(0,0,0));
                plugin.getActionFactory().setSelectedExplosionLocations(selectedExplosionLocations);
                new ActionEffectExplosionPrompt(uuid).start();
            } else {
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("consoleError"));
                new ActionEffectPrompt(uuid).start();
            }
        case 3:
            new ActionMainPrompt(uuid).start();
        default:
            new ActionEffectPrompt(uuid).start();
        }
    }
    
    public class ActionEffectSoundListPrompt extends ActionsEditorIntegerPrompt {

        public ActionEffectSoundListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        private final int size = 4;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle() {
            return BukkitLang.get("eventEditorEffectsTitle");
        }
        @Override
        public ChatColor getNumberColor(final int number) {
            switch (number) {
            case 1:
            case 2:
                return ChatColor.BLUE;
            case 3:
                return ChatColor.RED;
            case 4:
                return ChatColor.GREEN;
            default:
                return null;
            }
        }
        
        @Override
        public String getSelectionText(final int number) {
            switch (number) {
            case 1:
                return ChatColor.YELLOW + BukkitLang.get("eventEditorAddEffect");
            case 2:
                return ChatColor.YELLOW + BukkitLang.get("eventEditorAddEffectLocation");
            case 3:
                return ChatColor.RED + BukkitLang.get("clear");
            case 4:
                return ChatColor.GREEN + BukkitLang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final int number) {
            switch (number) {
            case 1:
                if (SessionData.get(uuid, Key.A_EFFECTS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    for (final String s : (List<String>) Objects.requireNonNull(SessionData.get(uuid, Key.A_EFFECTS))) {
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s);
                    }
                    return text.toString();
                }
            case 2:
                if (SessionData.get(uuid, Key.A_EFFECTS_LOCATIONS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    for (final String s : (List<String>) Objects.requireNonNull(SessionData
                            .get(uuid, Key.A_EFFECTS_LOCATIONS))) {
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s);
                    }
                    return text.toString();
                }
            case 3:
            case 4:
                return "";
            default:
                return null;
            }
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitActionsEditorPostOpenNumericPromptEvent event
                    = new BukkitActionsEditorPostOpenNumericPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
    
            final StringBuilder text = new StringBuilder(ChatColor.GOLD + getTitle());
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                        .append(getAdditionalText(i));
            }
            return text.toString();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final Number input) {
            final CommandSender sender = Bukkit.getEntity(uuid);
            switch (input.intValue()) {
            case 1:
                new ActionEffectSoundPrompt(uuid).start();
            case 2:
                if (SessionData.get(uuid, Key.A_EFFECTS) == null) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("eventEditorMustAddEffects"));
                    new ActionEffectSoundListPrompt(uuid).start();
                } else {
                    if (sender instanceof Player) {
                        final ConcurrentHashMap<UUID, Block> selectedEffectLocations
                                = plugin.getActionFactory().getSelectedEffectLocations();
                        if (BukkitMiscUtil.getWorlds().isEmpty()) {
                            sender.sendMessage(ChatColor.RED + BukkitLang.get("unknownError"));
                            new ActionEffectSoundListPrompt(uuid).start();
                        }
                        selectedEffectLocations.put(((Player) sender).getUniqueId(),
                                Bukkit.getWorlds().get(0).getBlockAt(0,0,0));
                        plugin.getActionFactory().setSelectedEffectLocations(selectedEffectLocations);
                        new ActionEffectSoundLocationPrompt(uuid).start();
                    } else {
                        sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("consoleError"));
                        new ActionEffectSoundListPrompt(uuid).start();
                    }
                }
            case 3:
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("eventEditorEffectsCleared"));
                SessionData.set(uuid, Key.A_EFFECTS, null);
                SessionData.set(uuid, Key.A_EFFECTS_LOCATIONS, null);
                new ActionEffectSoundListPrompt(uuid).start();
            case 4:
                final int one;
                final int two;
                final List<String> effects = (List<String>) SessionData.get(uuid, Key.A_EFFECTS);
                final List<String> locations = (List<String>) SessionData.get(uuid, Key.A_EFFECTS_LOCATIONS);
                if (effects != null) {
                    one = effects.size();
                } else {
                    one = 0;
                }
                if (locations != null) {
                    two = locations.size();
                } else {
                    two = 0;
                }
                if (one == two) {
                    new ActionMainPrompt(uuid).start();
                } else {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("listsNotSameSize"));
                    new ActionEffectSoundListPrompt(uuid).start();
                }
            default:
                new ActionEffectSoundListPrompt(uuid).start();
            }
        }
    }
    
    public class ActionEffectSoundPrompt extends ActionsEditorStringPrompt {

        public ActionEffectSoundPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return BukkitLang.get("eventEditorEffectsTitle");
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("effEnterName");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitActionsEditorPostOpenStringPromptEvent event
                    = new BukkitActionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder effects = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle() + "\n");
            final List<Effect> effArr = new LinkedList<>(Arrays.asList(Effect.values()));
            effArr.sort(Comparator.comparing(Effect::name));
            for (int i = 0; i < effArr.size(); i++) {
                effects.append(ChatColor.AQUA).append(BukkitMiscUtil.snakeCaseToUpperCamelCase(effArr.get(i).name()));
                if (i < (effArr.size() - 1)) {
                    effects.append(ChatColor.GRAY).append(", ");
                }
            }
            effects.append("\n").append(ChatColor.YELLOW).append(getQueryText());
            return effects.toString();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = Bukkit.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                if (BukkitMiscUtil.getProperEffect(input) != null) {
                    final LinkedList<String> effects;
                    if (SessionData.get(uuid, Key.A_EFFECTS) != null) {
                        effects = (LinkedList<String>) SessionData.get(uuid, Key.A_EFFECTS);
                    } else {
                        effects = new LinkedList<>();
                    }
                    if (effects != null && BukkitMiscUtil.getProperEffect(input) != null) {
                        effects.add(Objects.requireNonNull(BukkitMiscUtil.getProperEffect(input)).name());
                    }
                    SessionData.set(uuid, Key.A_EFFECTS, effects);
                    if (sender instanceof Player) {
                        final ConcurrentHashMap<UUID, Block> selectedEffectLocations = plugin.getActionFactory()
                                .getSelectedEffectLocations();
                        selectedEffectLocations.remove(uuid);
                        plugin.getActionFactory().setSelectedEffectLocations(selectedEffectLocations);
                    }
                    new ActionEffectSoundListPrompt(uuid).start();
                } else {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("eventEditorInvalidEffect")
                            .replace("<input>", input));
                    new ActionEffectSoundPrompt(uuid).start();
                }
            } else {
                if (sender instanceof Player) {
                    final ConcurrentHashMap<UUID, Block> selectedEffectLocations
                            = plugin.getActionFactory().getSelectedEffectLocations();
                    selectedEffectLocations.remove(uuid);
                    plugin.getActionFactory().setSelectedEffectLocations(selectedEffectLocations);
                }
                new ActionEffectSoundListPrompt(uuid).start();
            }
        }
    }
    
    public class ActionEffectSoundLocationPrompt extends ActionsEditorStringPrompt {

        public ActionEffectSoundLocationPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("eventEditorEffectLocationPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitActionsEditorPostOpenStringPromptEvent event
                    = new BukkitActionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = Bukkit.getEntity(uuid);
            final Player player = (Player) sender;
            if (input.equalsIgnoreCase(BukkitLang.get("cmdAdd"))) {
                final Map<UUID, Block> selectedEffectLocations = plugin.getActionFactory().getSelectedEffectLocations();
                final Block block = selectedEffectLocations.get(player.getUniqueId());
                if (block != null) {
                    final Location loc = block.getLocation();
                    final LinkedList<String> locations;
                    if (SessionData.get(uuid, Key.A_EFFECTS_LOCATIONS) != null) {
                        locations = (LinkedList<String>) SessionData.get(uuid, Key.A_EFFECTS_LOCATIONS);
                    } else {
                        locations = new LinkedList<>();
                    }
                    if (locations != null) {
                        locations.add(BukkitConfigUtil.getLocationInfo(loc));
                    }
                    SessionData.set(uuid, Key.A_EFFECTS_LOCATIONS, locations);
                    selectedEffectLocations.remove(player.getUniqueId());
                } else {
                    player.sendMessage(ChatColor.RED + BukkitLang.get("eventEditorSelectBlockFirst"));
                    new ActionEffectSoundLocationPrompt(uuid).start();
                }
                new ActionEffectSoundListPrompt(uuid).start();
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final ConcurrentHashMap<UUID, Block> selectedEffectLocations
                        = plugin.getActionFactory().getSelectedEffectLocations();
                selectedEffectLocations.remove(player.getUniqueId());
                plugin.getActionFactory().setSelectedEffectLocations(selectedEffectLocations);
                new ActionEffectSoundListPrompt(uuid).start();
            } else {
                new ActionEffectSoundLocationPrompt(uuid).start();
            }
        }
    }
    
    public class ActionEffectExplosionPrompt extends ActionsEditorStringPrompt {

        public ActionEffectExplosionPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("eventEditorExplosionPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitActionsEditorPostOpenStringPromptEvent event
                    = new BukkitActionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = Bukkit.getEntity(uuid);
            final Player player = (Player) sender;
            if (input.equalsIgnoreCase(BukkitLang.get("cmdAdd"))) {
                final ConcurrentHashMap<UUID, Block> selectedExplosionLocations
                        = plugin.getActionFactory().getSelectedExplosionLocations();
                final Block block = selectedExplosionLocations.get(player.getUniqueId());
                if (block != null) {
                    final Location loc = block.getLocation();
                    final LinkedList<String> locations;
                    if (SessionData.get(uuid, Key.A_EXPLOSIONS) != null) {
                        locations = (LinkedList<String>) SessionData.get(uuid, Key.A_EXPLOSIONS);
                    } else {
                        locations = new LinkedList<>();
                    }
                    if (locations != null) {
                        locations.add(BukkitConfigUtil.getLocationInfo(loc));
                    }
                    SessionData.set(uuid, Key.A_EXPLOSIONS, locations);
                    selectedExplosionLocations.remove(player.getUniqueId());
                    plugin.getActionFactory().setSelectedExplosionLocations(selectedExplosionLocations);
                } else {
                    player.sendMessage(ChatColor.RED + BukkitLang.get("eventEditorSelectBlockFirst"));
                    new ActionEffectExplosionPrompt(uuid).start();
                }
                new ActionMainPrompt(uuid).start();
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.A_EXPLOSIONS, null);
                final ConcurrentHashMap<UUID, Block> selectedExplosionLocations
                        = plugin.getActionFactory().getSelectedExplosionLocations();
                selectedExplosionLocations.remove(player.getUniqueId());
                plugin.getActionFactory().setSelectedExplosionLocations(selectedExplosionLocations);
                new ActionMainPrompt(uuid).start();
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final ConcurrentHashMap<UUID, Block> selectedExplosionLocations
                        = plugin.getActionFactory().getSelectedExplosionLocations();
                selectedExplosionLocations.remove(player.getUniqueId());
                plugin.getActionFactory().setSelectedExplosionLocations(selectedExplosionLocations);
                new ActionMainPrompt(uuid).start();
            } else {
                new ActionEffectExplosionPrompt(uuid).start();
            }
        }
    }
}
