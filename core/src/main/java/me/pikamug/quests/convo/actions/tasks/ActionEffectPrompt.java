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
import me.pikamug.quests.util.BukkitConfigUtil;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.quests.util.BukkitMiscUtil;
import me.pikamug.quests.util.Key;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
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

public class ActionEffectPrompt extends ActionsEditorNumericPrompt {
    
    private final BukkitQuestsPlugin plugin;
    
    public ActionEffectPrompt(final ConversationContext context) {
        super(context);
        this.plugin = (BukkitQuestsPlugin)context.getPlugin();
    }
    
    private final int size = 3;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle(final ConversationContext context) {
        return BukkitLang.get("eventEditorEffect");
    }
    
    @Override
    public ChatColor getNumberColor(final ConversationContext context, final int number) {
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
    public String getSelectionText(final ConversationContext context, final int number) {
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
    public String getAdditionalText(final ConversationContext context, final int number) {
        switch (number) {
        case 1:
            if (context.getSessionData(Key.A_EFFECTS) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> effects = (LinkedList<String>) context.getSessionData(Key.A_EFFECTS);
                final LinkedList<String> locations
                        = (LinkedList<String>) context.getSessionData(Key.A_EFFECTS_LOCATIONS);
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
            if (context.getSessionData(Key.A_EXPLOSIONS) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> locations = (LinkedList<String>) context.getSessionData(Key.A_EXPLOSIONS);
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
            return new ActionEffectSoundListPrompt(context);
        case 2:
            if (context.getForWhom() instanceof Player) {
                final ConcurrentHashMap<UUID, Block> selectedExplosionLocations
                        = plugin.getActionFactory().getSelectedExplosionLocations();
                selectedExplosionLocations.put(((Player) context.getForWhom()).getUniqueId(),
                        Bukkit.getWorlds().get(0).getBlockAt(0,0,0));
                plugin.getActionFactory().setSelectedExplosionLocations(selectedExplosionLocations);
                return new ActionEffectExplosionPrompt(context);
            } else {
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("consoleError"));
                return new ActionEffectPrompt(context);
            }
        case 3:
            return new ActionMainPrompt(context);
        default:
            return new ActionEffectPrompt(context);
        }
    }
    
    public class ActionEffectSoundListPrompt extends ActionsEditorNumericPrompt {

        public ActionEffectSoundListPrompt(final ConversationContext context) {
            super(context);

        }
        
        private final int size = 4;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("eventEditorEffectsTitle");
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
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                if (context.getSessionData(Key.A_EFFECTS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    for (final String s : (List<String>) Objects.requireNonNull(context.getSessionData(Key.A_EFFECTS))) {
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s);
                    }
                    return text.toString();
                }
            case 2:
                if (context.getSessionData(Key.A_EFFECTS_LOCATIONS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    for (final String s : (List<String>) Objects.requireNonNull(context
                            .getSessionData(Key.A_EFFECTS_LOCATIONS))) {
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
        
        @SuppressWarnings("unchecked")
        @Override
        protected Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
            switch (input.intValue()) {
            case 1:
                return new ActionEffectSoundPrompt(context);
            case 2:
                if (context.getSessionData(Key.A_EFFECTS) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("eventEditorMustAddEffects"));
                    return new ActionEffectSoundListPrompt(context);
                } else {
                    if (context.getForWhom() instanceof Player) {
                        final ConcurrentHashMap<UUID, Block> selectedEffectLocations
                                = plugin.getActionFactory().getSelectedEffectLocations();
                        selectedEffectLocations.put(((Player) context.getForWhom()).getUniqueId(),
                                Bukkit.getWorlds().get(0).getBlockAt(0,0,0));
                        plugin.getActionFactory().setSelectedEffectLocations(selectedEffectLocations);
                        return new ActionEffectSoundLocationPrompt(context);
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("consoleError"));
                        return new ActionEffectSoundListPrompt(context);
                    }
                }
            case 3:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("eventEditorEffectsCleared"));
                context.setSessionData(Key.A_EFFECTS, null);
                context.setSessionData(Key.A_EFFECTS_LOCATIONS, null);
                return new ActionEffectSoundListPrompt(context);
            case 4:
                final int one;
                final int two;
                final List<String> effects = (List<String>) context.getSessionData(Key.A_EFFECTS);
                final List<String> locations = (List<String>) context.getSessionData(Key.A_EFFECTS_LOCATIONS);
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
                    return new ActionMainPrompt(context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("listsNotSameSize"));
                    return new ActionEffectSoundListPrompt(context);
                }
            default:
                return new ActionEffectSoundListPrompt(context);
            }
        }
    }
    
    public class ActionEffectSoundPrompt extends ActionsEditorStringPrompt {

        public ActionEffectSoundPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("eventEditorEffectsTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("effEnterName");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final ActionsEditorPostOpenStringPromptEvent event
                    = new ActionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder effects = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle(context) + "\n");
            final List<Effect> effArr = new LinkedList<>(Arrays.asList(Effect.values()));
            effArr.sort(Comparator.comparing(Effect::name));
            for (int i = 0; i < effArr.size(); i++) {
                effects.append(ChatColor.AQUA).append(BukkitMiscUtil.snakeCaseToUpperCamelCase(effArr.get(i).name()));
                if (i < (effArr.size() - 1)) {
                    effects.append(ChatColor.GRAY).append(", ");
                }
            }
            effects.append("\n").append(ChatColor.YELLOW).append(getQueryText(context));
            return effects.toString();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                if (BukkitMiscUtil.getProperEffect(input) != null) {
                    final LinkedList<String> effects;
                    if (context.getSessionData(Key.A_EFFECTS) != null) {
                        effects = (LinkedList<String>) context.getSessionData(Key.A_EFFECTS);
                    } else {
                        effects = new LinkedList<>();
                    }
                    if (effects != null && BukkitMiscUtil.getProperEffect(input) != null) {
                        effects.add(Objects.requireNonNull(BukkitMiscUtil.getProperEffect(input)).name());
                    }
                    context.setSessionData(Key.A_EFFECTS, effects);
                    if (context.getForWhom() instanceof Player) {
                        final ConcurrentHashMap<UUID, Block> selectedEffectLocations = plugin.getActionFactory()
                                .getSelectedEffectLocations();
                        selectedEffectLocations.remove(((Player)context.getForWhom()).getUniqueId());
                        plugin.getActionFactory().setSelectedEffectLocations(selectedEffectLocations);
                    }
                    return new ActionEffectSoundListPrompt(context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("eventEditorInvalidEffect")
                            .replace("<input>", input));
                    return new ActionEffectSoundPrompt(context);
                }
            } else {
                if (context.getForWhom() instanceof Player) {
                    final ConcurrentHashMap<UUID, Block> selectedEffectLocations
                            = plugin.getActionFactory().getSelectedEffectLocations();
                    selectedEffectLocations.remove(((Player)context.getForWhom()).getUniqueId());
                    plugin.getActionFactory().setSelectedEffectLocations(selectedEffectLocations);
                }
                return new ActionEffectSoundListPrompt(context);
            }
        }
    }
    
    public class ActionEffectSoundLocationPrompt extends ActionsEditorStringPrompt {

        public ActionEffectSoundLocationPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("eventEditorEffectLocationPrompt");
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
                final Map<UUID, Block> selectedEffectLocations = plugin.getActionFactory().getSelectedEffectLocations();
                final Block block = selectedEffectLocations.get(player.getUniqueId());
                if (block != null) {
                    final Location loc = block.getLocation();
                    final LinkedList<String> locations;
                    if (context.getSessionData(Key.A_EFFECTS_LOCATIONS) != null) {
                        locations = (LinkedList<String>) context.getSessionData(Key.A_EFFECTS_LOCATIONS);
                    } else {
                        locations = new LinkedList<>();
                    }
                    if (locations != null) {
                        locations.add(BukkitConfigUtil.getLocationInfo(loc));
                    }
                    context.setSessionData(Key.A_EFFECTS_LOCATIONS, locations);
                    selectedEffectLocations.remove(player.getUniqueId());
                } else {
                    player.sendMessage(ChatColor.RED + BukkitLang.get("eventEditorSelectBlockFirst"));
                    return new ActionEffectSoundLocationPrompt(context);
                }
                return new ActionEffectSoundListPrompt(context);
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final ConcurrentHashMap<UUID, Block> selectedEffectLocations
                        = plugin.getActionFactory().getSelectedEffectLocations();
                selectedEffectLocations.remove(player.getUniqueId());
                plugin.getActionFactory().setSelectedEffectLocations(selectedEffectLocations);
                return new ActionEffectSoundListPrompt(context);
            } else {
                return new ActionEffectSoundLocationPrompt(context);
            }
        }
    }
    
    public class ActionEffectExplosionPrompt extends ActionsEditorStringPrompt {

        public ActionEffectExplosionPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("eventEditorExplosionPrompt");
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
                final ConcurrentHashMap<UUID, Block> selectedExplosionLocations
                        = plugin.getActionFactory().getSelectedExplosionLocations();
                final Block block = selectedExplosionLocations.get(player.getUniqueId());
                if (block != null) {
                    final Location loc = block.getLocation();
                    final LinkedList<String> locations;
                    if (context.getSessionData(Key.A_EXPLOSIONS) != null) {
                        locations = (LinkedList<String>) context.getSessionData(Key.A_EXPLOSIONS);
                    } else {
                        locations = new LinkedList<>();
                    }
                    if (locations != null) {
                        locations.add(BukkitConfigUtil.getLocationInfo(loc));
                    }
                    context.setSessionData(Key.A_EXPLOSIONS, locations);
                    selectedExplosionLocations.remove(player.getUniqueId());
                    plugin.getActionFactory().setSelectedExplosionLocations(selectedExplosionLocations);
                } else {
                    player.sendMessage(ChatColor.RED + BukkitLang.get("eventEditorSelectBlockFirst"));
                    return new ActionEffectExplosionPrompt(context);
                }
                return new ActionMainPrompt(context);
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(Key.A_EXPLOSIONS, null);
                final ConcurrentHashMap<UUID, Block> selectedExplosionLocations
                        = plugin.getActionFactory().getSelectedExplosionLocations();
                selectedExplosionLocations.remove(player.getUniqueId());
                plugin.getActionFactory().setSelectedExplosionLocations(selectedExplosionLocations);
                return new ActionMainPrompt(context);
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final ConcurrentHashMap<UUID, Block> selectedExplosionLocations
                        = plugin.getActionFactory().getSelectedExplosionLocations();
                selectedExplosionLocations.remove(player.getUniqueId());
                plugin.getActionFactory().setSelectedExplosionLocations(selectedExplosionLocations);
                return new ActionMainPrompt(context);
            } else {
                return new ActionEffectExplosionPrompt(context);
            }
        }
    }
}
