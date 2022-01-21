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

package me.blackvein.quests.convo.actions.tasks;

import me.blackvein.quests.Quests;
import me.blackvein.quests.convo.actions.ActionsEditorNumericPrompt;
import me.blackvein.quests.convo.actions.ActionsEditorStringPrompt;
import me.blackvein.quests.convo.actions.main.ActionMainPrompt;
import me.blackvein.quests.events.editor.actions.ActionsEditorPostOpenNumericPromptEvent;
import me.blackvein.quests.events.editor.actions.ActionsEditorPostOpenStringPromptEvent;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.ConfigUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class WeatherPrompt extends ActionsEditorNumericPrompt {
    
    private final Quests plugin;
    
    public WeatherPrompt(final ConversationContext context) {
        super(context);
        this.plugin = (Quests)context.getPlugin();
    }
    
    private final int size = 4;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle(final ConversationContext context) {
        return Lang.get("eventEditorWeather");
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
            return ChatColor.YELLOW + Lang.get("eventEditorSetStorm");
        case 2:
            return ChatColor.YELLOW + Lang.get("eventEditorSetThunder");
        case 3:
            return ChatColor.YELLOW + Lang.get("eventEditorSetLightning");
        case 4:
            return ChatColor.GREEN + Lang.get("done");
        default:
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public String getAdditionalText(final ConversationContext context, final int number) {
        switch (number) {
        case 1:
            if (context.getSessionData(CK.E_WORLD_STORM) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                final Integer duration = (Integer) context.getSessionData(CK.E_WORLD_STORM_DURATION);
                if (duration != null) {
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + context.getSessionData(CK.E_WORLD_STORM)
                            + ChatColor.GRAY + " -> " + ChatColor.DARK_AQUA + MiscUtil.getTime(duration * 1000L)
                            + ChatColor.GRAY + ")";
                }
            }
        case 2:
            if (context.getSessionData(CK.E_WORLD_THUNDER) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                final Integer duration = (Integer) context.getSessionData(CK.E_WORLD_THUNDER_DURATION);
                if (duration != null) {
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + context.getSessionData(CK.E_WORLD_THUNDER)
                            + ChatColor.GRAY + " -> " + ChatColor.DARK_AQUA + MiscUtil.getTime(duration * 1000L)
                            + ChatColor.GRAY + ")";
                }
            }
        case 3:
            if (context.getSessionData(CK.E_LIGHTNING) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> locations = (LinkedList<String>) context.getSessionData(CK.E_LIGHTNING);
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
            return new StormPrompt(context);
        case 2:
            return new ThunderPrompt(context);
        case 3:
            if (context.getForWhom() instanceof Player) {
                final Map<UUID, Block> selectedLightningLocations 
                        = plugin.getActionFactory().getSelectedLightningLocations();
                selectedLightningLocations.put(((Player) context.getForWhom()).getUniqueId(), null);
                plugin.getActionFactory().setSelectedLightningLocations(selectedLightningLocations);
                return new LightningPrompt(context);
            } else {
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("consoleError"));
                return new WeatherPrompt(context);
            }
        case 4:
            return new ActionMainPrompt(context);
        default:
            return new WeatherPrompt(context);
        }
    }
    
    public class StormPrompt extends ActionsEditorNumericPrompt {

        public StormPrompt(final ConversationContext context) {
            super(context);
        }
        
        private final int size = 4;
        
        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("eventEditorStormTitle");
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
                return ChatColor.YELLOW + Lang.get("eventEditorSetWorld");
            case 2:
                return ChatColor.YELLOW + Lang.get("eventEditorSetDuration");
            case 3:
                return ChatColor.YELLOW + Lang.get("clear");
            case 4:
                return ChatColor.GREEN + Lang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                if (context.getSessionData(CK.E_WORLD_STORM) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + context.getSessionData(CK.E_WORLD_STORM)
                            + ChatColor.GRAY + ")";
                }
            case 2:
                if (context.getSessionData(CK.E_WORLD_STORM_DURATION) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    final Integer duration = (Integer) context.getSessionData(CK.E_WORLD_STORM_DURATION);
                    if (duration != null) {
                        return ChatColor.GRAY + "(" + ChatColor.AQUA + MiscUtil.getTime(duration * 1000L)
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
                return new StormWorldPrompt(context);
            case 2:
                if (context.getSessionData(CK.E_WORLD_STORM) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorSetWorldFirst"));
                    return new StormPrompt(context);
                } else {
                    return new StormDurationPrompt(context);
                }
            case 3:
                if (context.getSessionData(CK.E_WORLD_STORM) != null 
                        && context.getSessionData(CK.E_WORLD_STORM_DURATION) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorMustSetStormDuration"));
                    return new StormPrompt(context);
                } else {
                    return new ActionMainPrompt(context);
                }
            case 4:
                return new ActionMainPrompt(context);
            default:
                return new StormPrompt(context);
            }
        }
    }

    public class StormWorldPrompt extends ActionsEditorStringPrompt {

        public StormWorldPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("eventEditorWorldsTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("eventEditorEnterStormWorld");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final ActionsEditorPostOpenStringPromptEvent event
                    = new ActionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            StringBuilder effects = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle(context)
                    + ChatColor.DARK_PURPLE);
            for (final World w : plugin.getServer().getWorlds()) {
                effects.append("\n").append(w.getName()).append(", ");
            }
            effects = new StringBuilder(effects.substring(0, effects.length()));
            return ChatColor.YELLOW + effects.toString() + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                if (plugin.getServer().getWorld(input) != null) {
                    context.setSessionData(CK.E_WORLD_STORM, Objects.requireNonNull(plugin.getServer().getWorld(input))
                            .getName());
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + input + " " + ChatColor.RED 
                            + Lang.get("eventEditorInvalidWorld"));
                    return new StormWorldPrompt(context);
                }
            }
            return new StormPrompt(context);
        }
    }

    public class StormDurationPrompt extends ActionsEditorStringPrompt {

        public StormDurationPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("eventEditorEnterDuration");
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
                            + Lang.get("invalidMinimum").replace("<number>", "1"));
                    return new StormDurationPrompt(context);
                } else {
                    context.setSessionData(CK.E_WORLD_STORM_DURATION, i);
                }
            } catch (final NumberFormatException e) {
                context.getForWhom().sendRawMessage(ChatColor.RED 
                        + Lang.get("reqNotANumber").replace("<input>", input));
            }
            return new StormPrompt(context);
        }
    }

    public class ThunderPrompt extends ActionsEditorNumericPrompt {

        public ThunderPrompt(final ConversationContext context) {
            super(context);
        }
        
        private final int size = 4;
        
        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("eventEditorThunderTitle");
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
                return ChatColor.YELLOW + Lang.get("eventEditorSetWorld");
            case 2:
                return ChatColor.YELLOW + Lang.get("eventEditorSetDuration");
            case 3:
                return ChatColor.YELLOW + Lang.get("clear");
            case 4:
                return ChatColor.GREEN + Lang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                if (context.getSessionData(CK.E_WORLD_THUNDER) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + context.getSessionData(CK.E_WORLD_THUNDER)
                            + ChatColor.GRAY + ")";
                }
            case 2:
                if (context.getSessionData(CK.E_WORLD_THUNDER_DURATION) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    final Integer duration = (Integer) context.getSessionData(CK.E_WORLD_THUNDER_DURATION);
                    if (duration != null) {
                        return ChatColor.GRAY + "(" + ChatColor.AQUA + MiscUtil.getTime(duration * 1000L)
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
                return new ThunderWorldPrompt(context);
            case 2:
                if (context.getSessionData(CK.E_WORLD_THUNDER) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorSetWorldFirst"));
                    return new ThunderPrompt(context);
                } else {
                    return new ThunderDurationPrompt(context);
                }
            case 3:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("eventEditorThunderCleared"));
                context.setSessionData(CK.E_WORLD_THUNDER, null);
                context.setSessionData(CK.E_WORLD_THUNDER_DURATION, null);
                return new ThunderPrompt(context);
            case 4:
                if (context.getSessionData(CK.E_WORLD_THUNDER) != null 
                        && context.getSessionData(CK.E_WORLD_THUNDER_DURATION) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorMustSetThunderDuration"));
                    return new ThunderPrompt(context);
                } else {
                    return new ActionMainPrompt(context);
                }
            default:
                return new ThunderPrompt(context);
            }
        }
    }

    public class ThunderWorldPrompt extends ActionsEditorStringPrompt {

        public ThunderWorldPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("eventEditorWorldsTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("eventEditorEnterThunderWorld");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final ActionsEditorPostOpenStringPromptEvent event
                    = new ActionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            StringBuilder effects = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle(context)
                    + ChatColor.DARK_PURPLE);
            for (final World w : plugin.getServer().getWorlds()) {
                effects.append("\n").append(w.getName()).append(", ");
            }
            effects = new StringBuilder(effects.substring(0, effects.length()));
            return ChatColor.YELLOW + effects.toString() + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                if (plugin.getServer().getWorld(input) != null) {
                    context.setSessionData(CK.E_WORLD_THUNDER, Objects.requireNonNull(plugin.getServer()
                            .getWorld(input)).getName());
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + input + " " + ChatColor.RED 
                            + Lang.get("eventEditorInvalidWorld"));
                    return new ThunderWorldPrompt(context);
                }
            }
            return new ThunderPrompt(context);
        }
    }

    public class ThunderDurationPrompt extends ActionsEditorStringPrompt {

        public ThunderDurationPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("eventEditorEnterDuration");
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
                            + Lang.get("invalidMinimum").replace("<number>", "1"));
                    return new ThunderDurationPrompt(context);
                } else {
                    context.setSessionData(CK.E_WORLD_THUNDER_DURATION, i);
                }
            } catch (final NumberFormatException e) {
                context.getForWhom().sendRawMessage(ChatColor.RED 
                        + Lang.get("reqNotANumber").replace("<input>", input));
            }
            return new ThunderPrompt(context);
        }
    }
    
    public class LightningPrompt extends ActionsEditorStringPrompt {

        public LightningPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("eventEditorLightningPrompt");
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
            if (input.equalsIgnoreCase(Lang.get("cmdAdd"))) {
                final Map<UUID, Block> selectedLightningLocations 
                        = plugin.getActionFactory().getSelectedLightningLocations();
                final Block block = selectedLightningLocations.get(player.getUniqueId());
                if (block != null) {
                    final Location loc = block.getLocation();
                    final LinkedList<String> locations;
                    if (context.getSessionData(CK.E_LIGHTNING) != null) {
                        locations = (LinkedList<String>) context.getSessionData(CK.E_LIGHTNING);
                    } else {
                        locations = new LinkedList<>();
                    }
                    if (locations != null) {
                        locations.add(ConfigUtil.getLocationInfo(loc));
                    }
                    context.setSessionData(CK.E_LIGHTNING, locations);
                    selectedLightningLocations.remove(player.getUniqueId());
                    plugin.getActionFactory().setSelectedLightningLocations(selectedLightningLocations);
                } else {
                    player.sendMessage(ChatColor.RED + Lang.get("eventEditorSelectBlockFirst"));
                    return new LightningPrompt(context);
                }
                return new ActionMainPrompt(context);
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.E_LIGHTNING, null);
                final Map<UUID, Block> selectedLightningLocations 
                        = plugin.getActionFactory().getSelectedLightningLocations();
                selectedLightningLocations.remove(player.getUniqueId());
                plugin.getActionFactory().setSelectedLightningLocations(selectedLightningLocations);
                return new ActionMainPrompt(context);
            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                final Map<UUID, Block> selectedLightningLocations 
                        = plugin.getActionFactory().getSelectedLightningLocations();
                selectedLightningLocations.remove(player.getUniqueId());
                plugin.getActionFactory().setSelectedLightningLocations(selectedLightningLocations);
                return new ActionMainPrompt(context);
            } else {
                return new LightningPrompt(context);
            }
        }
    }
}
