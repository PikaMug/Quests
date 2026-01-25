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
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ActionWeatherPrompt extends ActionsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final BukkitQuestsPlugin plugin;
    
    public ActionWeatherPrompt(final @NotNull UUID uuid) {
        super(uuid);
        this.uuid = uuid;
        this.plugin = BukkitQuestsPlugin.getInstance();
    }
    
    private final int size = 4;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle() {
        return BukkitLang.get("eventEditorWeather");
    }
    
    @Override
    public ChatColor getNumberColor(final int number) {
        switch (number) {
        case 1:
        case 2:
        case 3:
            return ChatColor.BLUE;
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
            return ChatColor.YELLOW + BukkitLang.get("eventEditorSetStorm");
        case 2:
            return ChatColor.YELLOW + BukkitLang.get("eventEditorSetThunder");
        case 3:
            return ChatColor.YELLOW + BukkitLang.get("eventEditorSetLightning");
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
            if (SessionData.get(uuid, Key.A_WORLD_STORM) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final Integer duration = (Integer) SessionData.get(uuid, Key.A_WORLD_STORM_DURATION);
                if (duration != null) {
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + SessionData.get(uuid, Key.A_WORLD_STORM)
                            + ChatColor.GRAY + " -> " + ChatColor.DARK_AQUA + BukkitMiscUtil.getTime(duration * 1000L)
                            + ChatColor.GRAY + ")";
                }
            }
        case 2:
            if (SessionData.get(uuid, Key.A_WORLD_THUNDER) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final Integer duration = (Integer) SessionData.get(uuid, Key.A_WORLD_THUNDER_DURATION);
                if (duration != null) {
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + SessionData.get(uuid, Key.A_WORLD_THUNDER)
                            + ChatColor.GRAY + " -> " + ChatColor.DARK_AQUA + BukkitMiscUtil.getTime(duration * 1000L)
                            + ChatColor.GRAY + ")";
                }
            }
        case 3:
            if (SessionData.get(uuid, Key.A_LIGHTNING) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> locations = (LinkedList<String>) SessionData.get(uuid, Key.A_LIGHTNING);
                if (locations != null) {
                    for (final String loc : locations) {
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(loc);
                    }
                }
                return text.toString();
            }
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
        final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
        switch (input.intValue()) {
        case 1:
            new ActionStormPrompt(uuid).start();
            break;
        case 2:
            new ActionThunderPrompt(uuid).start();
            break;
        case 3:
            if (sender instanceof Player) {
                final ConcurrentHashMap<UUID, Block> selectedLightningLocations
                        = plugin.getActionFactory().getSelectedLightningLocations();
                if (BukkitMiscUtil.getWorlds().isEmpty()) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("unknownError"));
                    new ActionWeatherPrompt(uuid).start();
                    break;
                }
                selectedLightningLocations.put(((Player) sender).getUniqueId(),
                        Bukkit.getWorlds().get(0).getBlockAt(0,0,0));
                plugin.getActionFactory().setSelectedLightningLocations(selectedLightningLocations);
                new ActionLightningPrompt(uuid).start();
            } else {
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("consoleError"));
                new ActionWeatherPrompt(uuid).start();
            }
            break;
        case 4:
            new ActionMainPrompt(uuid).start();
            break;
        default:
            new ActionWeatherPrompt(uuid).start();
            break;
        }
    }
    
    public class ActionStormPrompt extends ActionsEditorIntegerPrompt {

        public ActionStormPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        private final int size = 4;
        
        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return BukkitLang.get("eventEditorStormTitle");
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
                return ChatColor.YELLOW + BukkitLang.get("eventEditorSetWorld");
            case 2:
                return ChatColor.YELLOW + BukkitLang.get("eventEditorSetDuration");
            case 3:
                return ChatColor.YELLOW + BukkitLang.get("clear");
            case 4:
                return ChatColor.GREEN + BukkitLang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        public String getAdditionalText(final int number) {
            switch (number) {
            case 1:
                if (SessionData.get(uuid, Key.A_WORLD_STORM) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + SessionData.get(uuid, Key.A_WORLD_STORM)
                            + ChatColor.GRAY + ")";
                }
            case 2:
                if (SessionData.get(uuid, Key.A_WORLD_STORM_DURATION) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final Integer duration = (Integer) SessionData.get(uuid, Key.A_WORLD_STORM_DURATION);
                    if (duration != null) {
                        return ChatColor.GRAY + "(" + ChatColor.AQUA + BukkitMiscUtil.getTime(duration * 1000L)
                                + ChatColor.GRAY + ")";
                    }
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
        public void acceptInput(final Number input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            switch (input.intValue()) {
            case 1:
                new ActionStormWorldPrompt(uuid).start();
                break;
            case 2:
                if (SessionData.get(uuid, Key.A_WORLD_STORM) == null) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("eventEditorSetWorldFirst"));
                    new ActionStormPrompt(uuid).start();
                } else {
                    new ActionStormDurationPrompt(uuid).start();
                }
                break;
            case 3:
                if (SessionData.get(uuid, Key.A_WORLD_STORM) != null
                        && SessionData.get(uuid, Key.A_WORLD_STORM_DURATION) == null) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("eventEditorMustSetStormDuration"));
                    new ActionStormPrompt(uuid).start();
                } else {
                    new ActionMainPrompt(uuid).start();
                }
                break;
            case 4:
                new ActionMainPrompt(uuid).start();
                break;
            default:
                new ActionStormPrompt(uuid).start();
                break;
            }
        }
    }

    public class ActionStormWorldPrompt extends ActionsEditorStringPrompt {

        public ActionStormWorldPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return BukkitLang.get("eventEditorWorldsTitle");
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("eventEditorEnterStormWorld");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitActionsEditorPostOpenStringPromptEvent event
                    = new BukkitActionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder worlds = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle() + "\n");
            final List<World> worldArr = plugin.getServer().getWorlds();
            for (int i = 0; i < worldArr.size(); i++) {
                worlds.append(ChatColor.AQUA).append(worldArr.get(i).getName());
                if (i < (worldArr.size() - 1)) {
                    worlds.append(ChatColor.GRAY).append(", ");
                }
            }
            worlds.append("\n").append(ChatColor.YELLOW).append(getQueryText());
            return worlds.toString();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                if (plugin.getServer().getWorld(input) != null) {
                    SessionData.set(uuid, Key.A_WORLD_STORM, Objects.requireNonNull(plugin.getServer().getWorld(input))
                            .getName());
                } else {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("eventEditorInvalidWorld")
                            .replace("<input>", input));
                    new ActionStormWorldPrompt(uuid).start();
                    return;
                }
            }
            new ActionStormPrompt(uuid).start();
        }
    }

    public class ActionStormDurationPrompt extends ActionsEditorStringPrompt {

        public ActionStormDurationPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("eventEditorEnterDuration");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitActionsEditorPostOpenStringPromptEvent event
                    = new BukkitActionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            try {
                final int i = Integer.parseInt(input);
                if (i < 1) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("invalidMinimum").replace("<number>", "1"));
                    new ActionStormDurationPrompt(uuid).start();
                    return;
                } else {
                    SessionData.set(uuid, Key.A_WORLD_STORM_DURATION, i);
                }
            } catch (final NumberFormatException e) {
                sender.sendMessage(ChatColor.RED 
                        + BukkitLang.get("reqNotANumber").replace("<input>", input));
            }
            new ActionStormPrompt(uuid).start();
        }
    }

    public class ActionThunderPrompt extends ActionsEditorIntegerPrompt {

        public ActionThunderPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        private final int size = 4;
        
        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return BukkitLang.get("eventEditorThunderTitle");
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
                return ChatColor.YELLOW + BukkitLang.get("eventEditorSetWorld");
            case 2:
                return ChatColor.YELLOW + BukkitLang.get("eventEditorSetDuration");
            case 3:
                return ChatColor.YELLOW + BukkitLang.get("clear");
            case 4:
                return ChatColor.GREEN + BukkitLang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        public String getAdditionalText(final int number) {
            switch (number) {
            case 1:
                if (SessionData.get(uuid, Key.A_WORLD_THUNDER) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + SessionData.get(uuid, Key.A_WORLD_THUNDER)
                            + ChatColor.GRAY + ")";
                }
            case 2:
                if (SessionData.get(uuid, Key.A_WORLD_THUNDER_DURATION) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final Integer duration = (Integer) SessionData.get(uuid, Key.A_WORLD_THUNDER_DURATION);
                    if (duration != null) {
                        return ChatColor.GRAY + "(" + ChatColor.AQUA + BukkitMiscUtil.getTime(duration * 1000L)
                                + ChatColor.GRAY + ")";
                    }
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
        public void acceptInput(final Number input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            switch (input.intValue()) {
            case 1:
                new ActionThunderWorldPrompt(uuid).start();
                break;
            case 2:
                if (SessionData.get(uuid, Key.A_WORLD_THUNDER) == null) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("eventEditorSetWorldFirst"));
                    new ActionThunderPrompt(uuid).start();
                } else {
                    new ActionThunderDurationPrompt(uuid).start();
                }
                break;
            case 3:
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("eventEditorThunderCleared"));
                SessionData.set(uuid, Key.A_WORLD_THUNDER, null);
                SessionData.set(uuid, Key.A_WORLD_THUNDER_DURATION, null);
                new ActionThunderPrompt(uuid).start();
                break;
            case 4:
                if (SessionData.get(uuid, Key.A_WORLD_THUNDER) != null
                        && SessionData.get(uuid, Key.A_WORLD_THUNDER_DURATION) == null) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("eventEditorMustSetThunderDuration"));
                    new ActionThunderPrompt(uuid).start();
                } else {
                    new ActionMainPrompt(uuid).start();
                }
                break;
            default:
                new ActionThunderPrompt(uuid).start();
                break;
            }
        }
    }

    public class ActionThunderWorldPrompt extends ActionsEditorStringPrompt {

        public ActionThunderWorldPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return BukkitLang.get("eventEditorWorldsTitle");
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("eventEditorEnterThunderWorld");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitActionsEditorPostOpenStringPromptEvent event
                    = new BukkitActionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder worlds = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle() + "\n");
            final List<World> worldArr = plugin.getServer().getWorlds();
            for (int i = 0; i < worldArr.size(); i++) {
                worlds.append(ChatColor.AQUA).append(worldArr.get(i).getName());
                if (i < (worldArr.size() - 1)) {
                    worlds.append(ChatColor.GRAY).append(", ");
                }
            }
            worlds.append("\n").append(ChatColor.YELLOW).append(getQueryText());
            return worlds.toString();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                if (plugin.getServer().getWorld(input) != null) {
                    SessionData.set(uuid, Key.A_WORLD_THUNDER, Objects.requireNonNull(plugin.getServer()
                            .getWorld(input)).getName());
                } else {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("eventEditorInvalidWorld")
                            .replace("<input>", input));
                    new ActionThunderWorldPrompt(uuid).start();
                    return;
                }
            }
            new ActionThunderPrompt(uuid).start();
        }
    }

    public class ActionThunderDurationPrompt extends ActionsEditorStringPrompt {

        public ActionThunderDurationPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("eventEditorEnterDuration");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitActionsEditorPostOpenStringPromptEvent event
                    = new BukkitActionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            try {
                final int i = Integer.parseInt(input);
                if (i < 1) {
                    sender.sendMessage(ChatColor.RED 
                            + BukkitLang.get("invalidMinimum").replace("<number>", "1"));
                    new ActionThunderDurationPrompt(uuid).start();
                    return;
                } else {
                    SessionData.set(uuid, Key.A_WORLD_THUNDER_DURATION, i);
                }
            } catch (final NumberFormatException e) {
                sender.sendMessage(ChatColor.RED 
                        + BukkitLang.get("reqNotANumber").replace("<input>", input));
            }
            new ActionThunderPrompt(uuid).start();
        }
    }
    
    public class ActionLightningPrompt extends ActionsEditorStringPrompt {

        public ActionLightningPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("eventEditorLightningPrompt");
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
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            final Player player = (Player) sender;
            if (input.equalsIgnoreCase(BukkitLang.get("cmdAdd"))) {
                final ConcurrentHashMap<UUID, Block> selectedLightningLocations
                        = plugin.getActionFactory().getSelectedLightningLocations();
                final Block block = selectedLightningLocations.get(player.getUniqueId());
                if (block != null) {
                    final Location loc = block.getLocation();
                    final LinkedList<String> locations;
                    if (SessionData.get(uuid, Key.A_LIGHTNING) != null) {
                        locations = (LinkedList<String>) SessionData.get(uuid, Key.A_LIGHTNING);
                    } else {
                        locations = new LinkedList<>();
                    }
                    if (locations != null) {
                        locations.add(BukkitConfigUtil.getLocationInfo(loc));
                    }
                    SessionData.set(uuid, Key.A_LIGHTNING, locations);
                    selectedLightningLocations.remove(player.getUniqueId());
                    plugin.getActionFactory().setSelectedLightningLocations(selectedLightningLocations);
                } else {
                    player.sendMessage(ChatColor.RED + BukkitLang.get("eventEditorSelectBlockFirst"));
                    new ActionLightningPrompt(uuid).start();
                    return;
                }
                new ActionMainPrompt(uuid).start();
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.A_LIGHTNING, null);
                final ConcurrentHashMap<UUID, Block> selectedLightningLocations
                        = plugin.getActionFactory().getSelectedLightningLocations();
                selectedLightningLocations.remove(player.getUniqueId());
                plugin.getActionFactory().setSelectedLightningLocations(selectedLightningLocations);
                new ActionMainPrompt(uuid).start();
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final ConcurrentHashMap<UUID, Block> selectedLightningLocations
                        = plugin.getActionFactory().getSelectedLightningLocations();
                selectedLightningLocations.remove(player.getUniqueId());
                plugin.getActionFactory().setSelectedLightningLocations(selectedLightningLocations);
                new ActionMainPrompt(uuid).start();
            } else {
                new ActionLightningPrompt(uuid).start();
            }
        }
    }
}
