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
import me.pikamug.quests.convo.actions.ActionsEditorNumericPrompt;
import me.pikamug.quests.convo.actions.ActionsEditorStringPrompt;
import me.pikamug.quests.convo.actions.main.ActionMainPrompt;
import me.pikamug.quests.events.editor.actions.ActionsEditorPostOpenNumericPromptEvent;
import me.pikamug.quests.events.editor.actions.ActionsEditorPostOpenStringPromptEvent;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.BukkitConfigUtil;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.quests.util.BukkitMiscUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ActionWeatherPrompt extends ActionsEditorNumericPrompt {
    
    private final BukkitQuestsPlugin plugin;
    
    public ActionWeatherPrompt(final ConversationContext context) {
        super(context);
        this.plugin = (BukkitQuestsPlugin)context.getPlugin();
    }
    
    private final int size = 4;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle(final ConversationContext context) {
        return BukkitLang.get("eventEditorWeather");
    }
    
    @Override
    public ChatColor getNumberColor(final ConversationContext context, final int number) {
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
    public String getSelectionText(final ConversationContext context, final int number) {
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
    
    @SuppressWarnings("unchecked")
    @Override
    public String getAdditionalText(final ConversationContext context, final int number) {
        switch (number) {
        case 1:
            if (context.getSessionData(Key.A_WORLD_STORM) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final Integer duration = (Integer) context.getSessionData(Key.A_WORLD_STORM_DURATION);
                if (duration != null) {
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + context.getSessionData(Key.A_WORLD_STORM)
                            + ChatColor.GRAY + " -> " + ChatColor.DARK_AQUA + BukkitMiscUtil.getTime(duration * 1000L)
                            + ChatColor.GRAY + ")";
                }
            }
        case 2:
            if (context.getSessionData(Key.A_WORLD_THUNDER) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final Integer duration = (Integer) context.getSessionData(Key.A_WORLD_THUNDER_DURATION);
                if (duration != null) {
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + context.getSessionData(Key.A_WORLD_THUNDER)
                            + ChatColor.GRAY + " -> " + ChatColor.DARK_AQUA + BukkitMiscUtil.getTime(duration * 1000L)
                            + ChatColor.GRAY + ")";
                }
            }
        case 3:
            if (context.getSessionData(Key.A_LIGHTNING) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> locations = (LinkedList<String>) context.getSessionData(Key.A_LIGHTNING);
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
    public @NotNull String getBasicPromptText(final @NotNull ConversationContext context) {
        final ActionsEditorPostOpenNumericPromptEvent event
                = new ActionsEditorPostOpenNumericPromptEvent(context, this);
        plugin.getServer().getPluginManager().callEvent(event);
        
        final StringBuilder text = new StringBuilder(ChatColor.GOLD + "- " + getTitle(context) + " -");
        for (int i = 1; i <= size; i++) {
            text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                    .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i)).append(" ")
                    .append(getAdditionalText(context, i));
        }
        return text.toString();
    }

    @Override
    protected Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
        switch (input.intValue()) {
        case 1:
            return new ActionStormPrompt(context);
        case 2:
            return new ActionThunderPrompt(context);
        case 3:
            if (context.getForWhom() instanceof Player) {
                final Map<UUID, Block> selectedLightningLocations 
                        = plugin.getActionFactory().getSelectedLightningLocations();
                selectedLightningLocations.put(((Player) context.getForWhom()).getUniqueId(), null);
                plugin.getActionFactory().setSelectedLightningLocations(selectedLightningLocations);
                return new ActionLightningPrompt(context);
            } else {
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("consoleError"));
                return new ActionWeatherPrompt(context);
            }
        case 4:
            return new ActionMainPrompt(context);
        default:
            return new ActionWeatherPrompt(context);
        }
    }
    
    public class ActionStormPrompt extends ActionsEditorNumericPrompt {

        public ActionStormPrompt(final ConversationContext context) {
            super(context);
        }
        
        private final int size = 4;
        
        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("eventEditorStormTitle");
        }
        
        @Override
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
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
        public String getSelectionText(final ConversationContext context, final int number) {
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
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                if (context.getSessionData(Key.A_WORLD_STORM) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + context.getSessionData(Key.A_WORLD_STORM)
                            + ChatColor.GRAY + ")";
                }
            case 2:
                if (context.getSessionData(Key.A_WORLD_STORM_DURATION) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final Integer duration = (Integer) context.getSessionData(Key.A_WORLD_STORM_DURATION);
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
        public @NotNull String getBasicPromptText(final @NotNull ConversationContext context) {
            final ActionsEditorPostOpenNumericPromptEvent event
                    = new ActionsEditorPostOpenNumericPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.GOLD + getTitle(context));
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i)).append(" ")
                        .append(getAdditionalText(context, i));
            }
            return text.toString();
        }

        @Override
        protected Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
            switch (input.intValue()) {
            case 1:
                return new ActionStormWorldPrompt(context);
            case 2:
                if (context.getSessionData(Key.A_WORLD_STORM) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("eventEditorSetWorldFirst"));
                    return new ActionStormPrompt(context);
                } else {
                    return new ActionStormDurationPrompt(context);
                }
            case 3:
                if (context.getSessionData(Key.A_WORLD_STORM) != null
                        && context.getSessionData(Key.A_WORLD_STORM_DURATION) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("eventEditorMustSetStormDuration"));
                    return new ActionStormPrompt(context);
                } else {
                    return new ActionMainPrompt(context);
                }
            case 4:
                return new ActionMainPrompt(context);
            default:
                return new ActionStormPrompt(context);
            }
        }
    }

    public class ActionStormWorldPrompt extends ActionsEditorStringPrompt {

        public ActionStormWorldPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("eventEditorWorldsTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("eventEditorEnterStormWorld");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final ActionsEditorPostOpenStringPromptEvent event
                    = new ActionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder worlds = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle(context) + "\n");
            final List<World> worldArr = plugin.getServer().getWorlds();
            for (int i = 0; i < worldArr.size(); i++) {
                worlds.append(ChatColor.AQUA).append(worldArr.get(i).getName());
                if (i < (worldArr.size() - 1)) {
                    worlds.append(ChatColor.GRAY).append(", ");
                }
            }
            worlds.append("\n").append(ChatColor.YELLOW).append(getQueryText(context));
            return worlds.toString();
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                if (plugin.getServer().getWorld(input) != null) {
                    context.setSessionData(Key.A_WORLD_STORM, Objects.requireNonNull(plugin.getServer().getWorld(input))
                            .getName());
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("eventEditorInvalidWorld")
                            .replace("<input>", input));
                    return new ActionStormWorldPrompt(context);
                }
            }
            return new ActionStormPrompt(context);
        }
    }

    public class ActionStormDurationPrompt extends ActionsEditorStringPrompt {

        public ActionStormDurationPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("eventEditorEnterDuration");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final ActionsEditorPostOpenStringPromptEvent event
                    = new ActionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            try {
                final int i = Integer.parseInt(input);
                if (i < 1) {
                    context.getForWhom().sendRawMessage(ChatColor.RED 
                            + BukkitLang.get("invalidMinimum").replace("<number>", "1"));
                    return new ActionStormDurationPrompt(context);
                } else {
                    context.setSessionData(Key.A_WORLD_STORM_DURATION, i);
                }
            } catch (final NumberFormatException e) {
                context.getForWhom().sendRawMessage(ChatColor.RED 
                        + BukkitLang.get("reqNotANumber").replace("<input>", input));
            }
            return new ActionStormPrompt(context);
        }
    }

    public class ActionThunderPrompt extends ActionsEditorNumericPrompt {

        public ActionThunderPrompt(final ConversationContext context) {
            super(context);
        }
        
        private final int size = 4;
        
        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("eventEditorThunderTitle");
        }
        
        @Override
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
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
        public String getSelectionText(final ConversationContext context, final int number) {
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
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                if (context.getSessionData(Key.A_WORLD_THUNDER) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + context.getSessionData(Key.A_WORLD_THUNDER)
                            + ChatColor.GRAY + ")";
                }
            case 2:
                if (context.getSessionData(Key.A_WORLD_THUNDER_DURATION) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final Integer duration = (Integer) context.getSessionData(Key.A_WORLD_THUNDER_DURATION);
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
        public @NotNull String getBasicPromptText(final @NotNull ConversationContext context) {
            final ActionsEditorPostOpenNumericPromptEvent event
                    = new ActionsEditorPostOpenNumericPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.GOLD + getTitle(context));
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i)).append(" ")
                        .append(getAdditionalText(context, i));
            }
            return text.toString();
        }
        
        @Override
        protected Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
            switch (input.intValue()) {
            case 1:
                return new ActionThunderWorldPrompt(context);
            case 2:
                if (context.getSessionData(Key.A_WORLD_THUNDER) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("eventEditorSetWorldFirst"));
                    return new ActionThunderPrompt(context);
                } else {
                    return new ActionThunderDurationPrompt(context);
                }
            case 3:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("eventEditorThunderCleared"));
                context.setSessionData(Key.A_WORLD_THUNDER, null);
                context.setSessionData(Key.A_WORLD_THUNDER_DURATION, null);
                return new ActionThunderPrompt(context);
            case 4:
                if (context.getSessionData(Key.A_WORLD_THUNDER) != null
                        && context.getSessionData(Key.A_WORLD_THUNDER_DURATION) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("eventEditorMustSetThunderDuration"));
                    return new ActionThunderPrompt(context);
                } else {
                    return new ActionMainPrompt(context);
                }
            default:
                return new ActionThunderPrompt(context);
            }
        }
    }

    public class ActionThunderWorldPrompt extends ActionsEditorStringPrompt {

        public ActionThunderWorldPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("eventEditorWorldsTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("eventEditorEnterThunderWorld");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final ActionsEditorPostOpenStringPromptEvent event
                    = new ActionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder worlds = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle(context) + "\n");
            final List<World> worldArr = plugin.getServer().getWorlds();
            for (int i = 0; i < worldArr.size(); i++) {
                worlds.append(ChatColor.AQUA).append(worldArr.get(i).getName());
                if (i < (worldArr.size() - 1)) {
                    worlds.append(ChatColor.GRAY).append(", ");
                }
            }
            worlds.append("\n").append(ChatColor.YELLOW).append(getQueryText(context));
            return worlds.toString();
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                if (plugin.getServer().getWorld(input) != null) {
                    context.setSessionData(Key.A_WORLD_THUNDER, Objects.requireNonNull(plugin.getServer()
                            .getWorld(input)).getName());
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("eventEditorInvalidWorld")
                            .replace("<input>", input));
                    return new ActionThunderWorldPrompt(context);
                }
            }
            return new ActionThunderPrompt(context);
        }
    }

    public class ActionThunderDurationPrompt extends ActionsEditorStringPrompt {

        public ActionThunderDurationPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("eventEditorEnterDuration");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final ActionsEditorPostOpenStringPromptEvent event
                    = new ActionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            try {
                final int i = Integer.parseInt(input);
                if (i < 1) {
                    context.getForWhom().sendRawMessage(ChatColor.RED 
                            + BukkitLang.get("invalidMinimum").replace("<number>", "1"));
                    return new ActionThunderDurationPrompt(context);
                } else {
                    context.setSessionData(Key.A_WORLD_THUNDER_DURATION, i);
                }
            } catch (final NumberFormatException e) {
                context.getForWhom().sendRawMessage(ChatColor.RED 
                        + BukkitLang.get("reqNotANumber").replace("<input>", input));
            }
            return new ActionThunderPrompt(context);
        }
    }
    
    public class ActionLightningPrompt extends ActionsEditorStringPrompt {

        public ActionLightningPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("eventEditorLightningPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final ActionsEditorPostOpenStringPromptEvent event
                    = new ActionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            final Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(BukkitLang.get("cmdAdd"))) {
                final Map<UUID, Block> selectedLightningLocations 
                        = plugin.getActionFactory().getSelectedLightningLocations();
                final Block block = selectedLightningLocations.get(player.getUniqueId());
                if (block != null) {
                    final Location loc = block.getLocation();
                    final LinkedList<String> locations;
                    if (context.getSessionData(Key.A_LIGHTNING) != null) {
                        locations = (LinkedList<String>) context.getSessionData(Key.A_LIGHTNING);
                    } else {
                        locations = new LinkedList<>();
                    }
                    if (locations != null) {
                        locations.add(BukkitConfigUtil.getLocationInfo(loc));
                    }
                    context.setSessionData(Key.A_LIGHTNING, locations);
                    selectedLightningLocations.remove(player.getUniqueId());
                    plugin.getActionFactory().setSelectedLightningLocations(selectedLightningLocations);
                } else {
                    player.sendMessage(ChatColor.RED + BukkitLang.get("eventEditorSelectBlockFirst"));
                    return new ActionLightningPrompt(context);
                }
                return new ActionMainPrompt(context);
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(Key.A_LIGHTNING, null);
                final Map<UUID, Block> selectedLightningLocations 
                        = plugin.getActionFactory().getSelectedLightningLocations();
                selectedLightningLocations.remove(player.getUniqueId());
                plugin.getActionFactory().setSelectedLightningLocations(selectedLightningLocations);
                return new ActionMainPrompt(context);
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final Map<UUID, Block> selectedLightningLocations 
                        = plugin.getActionFactory().getSelectedLightningLocations();
                selectedLightningLocations.remove(player.getUniqueId());
                plugin.getActionFactory().setSelectedLightningLocations(selectedLightningLocations);
                return new ActionMainPrompt(context);
            } else {
                return new ActionLightningPrompt(context);
            }
        }
    }
}
