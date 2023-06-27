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

package me.pikamug.quests.convo.actions.tasks;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.convo.actions.ActionsEditorNumericPrompt;
import me.pikamug.quests.convo.actions.ActionsEditorStringPrompt;
import me.pikamug.quests.convo.actions.main.ActionMainPrompt;
import me.pikamug.quests.events.editor.actions.ActionsEditorPostOpenNumericPromptEvent;
import me.pikamug.quests.events.editor.actions.ActionsEditorPostOpenStringPromptEvent;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.BukkitConfigUtil;
import me.pikamug.quests.util.Language;
import me.pikamug.quests.util.BukkitMiscUtil;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
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
        return Language.get("eventEditorEffect");
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
            return ChatColor.YELLOW + Language.get("eventEditorSetEffects");
        case 2:
            return ChatColor.YELLOW + Language.get("eventEditorSetExplosions");
        case 3:
            return ChatColor.GREEN + Language.get("done");
        default:
            return null;
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public String getAdditionalText(final ConversationContext context, final int number) {
        switch (number) {
        case 1:
            if (context.getSessionData(Key.E_EFFECTS) == null) {
                return ChatColor.GRAY + "(" + Language.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> effects = (LinkedList<String>) context.getSessionData(Key.E_EFFECTS);
                final LinkedList<String> locations
                        = (LinkedList<String>) context.getSessionData(Key.E_EFFECTS_LOCATIONS);
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
            if (context.getSessionData(Key.E_EXPLOSIONS) == null) {
                return ChatColor.GRAY + "(" + Language.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> locations = (LinkedList<String>) context.getSessionData(Key.E_EXPLOSIONS);
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
                final Map<UUID, Block> selectedExplosionLocations = plugin.getActionFactory().getSelectedExplosionLocations();
                selectedExplosionLocations.put(((Player) context.getForWhom()).getUniqueId(), null);
                plugin.getActionFactory().setSelectedExplosionLocations(selectedExplosionLocations);
                return new ActionEffectExplosionPrompt(context);
            } else {
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Language.get("consoleError"));
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
            return Language.get("eventEditorEffectsTitle");
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
                return ChatColor.YELLOW + Language.get("eventEditorAddEffect");
            case 2:
                return ChatColor.YELLOW + Language.get("eventEditorAddEffectLocation");
            case 3:
                return ChatColor.RED + Language.get("clear");
            case 4:
                return ChatColor.GREEN + Language.get("done");
            default:
                return null;
            }
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                if (context.getSessionData(Key.E_EFFECTS) == null) {
                    return ChatColor.GRAY + "(" + Language.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    for (final String s : (List<String>) Objects.requireNonNull(context.getSessionData(Key.E_EFFECTS))) {
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s);
                    }
                    return text.toString();
                }
            case 2:
                if (context.getSessionData(Key.E_EFFECTS_LOCATIONS) == null) {
                    return ChatColor.GRAY + "(" + Language.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    for (final String s : (List<String>) Objects.requireNonNull(context
                            .getSessionData(Key.E_EFFECTS_LOCATIONS))) {
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
                if (context.getSessionData(Key.E_EFFECTS) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Language.get("eventEditorMustAddEffects"));
                    return new ActionEffectSoundListPrompt(context);
                } else {
                    if (context.getForWhom() instanceof Player) {
                        final Map<UUID, Block> selectedEffectLocations = plugin.getActionFactory().getSelectedEffectLocations();
                        selectedEffectLocations.put(((Player) context.getForWhom()).getUniqueId(), null);
                        plugin.getActionFactory().setSelectedEffectLocations(selectedEffectLocations);
                        return new ActionEffectSoundLocationPrompt(context);
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.YELLOW + Language.get("consoleError"));
                        return new ActionEffectSoundListPrompt(context);
                    }
                }
            case 3:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Language.get("eventEditorEffectsCleared"));
                context.setSessionData(Key.E_EFFECTS, null);
                context.setSessionData(Key.E_EFFECTS_LOCATIONS, null);
                return new ActionEffectSoundListPrompt(context);
            case 4:
                final int one;
                final int two;
                final List<String> effects = (List<String>) context.getSessionData(Key.E_EFFECTS);
                final List<String> locations = (List<String>) context.getSessionData(Key.E_EFFECTS_LOCATIONS);
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
                    context.getForWhom().sendRawMessage(ChatColor.RED + Language.get("listsNotSameSize"));
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
            return Language.get("eventEditorEffectsTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Language.get("effEnterName");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final ActionsEditorPostOpenStringPromptEvent event
                    = new ActionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder effects = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle(context) + "\n");
            final Effect[] worldArr = Effect.values();
            for (int i = 0; i < worldArr.length; i++) {
                effects.append(ChatColor.AQUA).append(BukkitMiscUtil.snakeCaseToUpperCamelCase(worldArr[i].name()));
                if (i < (worldArr.length - 1)) {
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
            if (!input.equalsIgnoreCase(Language.get("cmdCancel"))) {
                if (BukkitMiscUtil.getProperEffect(input) != null) {
                    final LinkedList<String> effects;
                    if (context.getSessionData(Key.E_EFFECTS) != null) {
                        effects = (LinkedList<String>) context.getSessionData(Key.E_EFFECTS);
                    } else {
                        effects = new LinkedList<>();
                    }
                    if (effects != null && BukkitMiscUtil.getProperEffect(input) != null) {
                        effects.add(Objects.requireNonNull(BukkitMiscUtil.getProperEffect(input)).name());
                    }
                    context.setSessionData(Key.E_EFFECTS, effects);
                    if (context.getForWhom() instanceof Player) {
                        final Map<UUID, Block> selectedEffectLocations = plugin.getActionFactory()
                                .getSelectedEffectLocations();
                        selectedEffectLocations.remove(((Player)context.getForWhom()).getUniqueId());
                        plugin.getActionFactory().setSelectedEffectLocations(selectedEffectLocations);
                    }
                    return new ActionEffectSoundListPrompt(context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Language.get("eventEditorInvalidEffect")
                            .replace("<input>", input));
                    return new ActionEffectSoundPrompt(context);
                }
            } else {
                if (context.getForWhom() instanceof Player) {
                    final Map<UUID, Block> selectedEffectLocations = plugin.getActionFactory()
                            .getSelectedEffectLocations();
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
            return Language.get("eventEditorEffectLocationPrompt");
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
            if (input.equalsIgnoreCase(Language.get("cmdAdd"))) {
                final Map<UUID, Block> selectedEffectLocations = plugin.getActionFactory().getSelectedEffectLocations();
                final Block block = selectedEffectLocations.get(player.getUniqueId());
                if (block != null) {
                    final Location loc = block.getLocation();
                    final LinkedList<String> locations;
                    if (context.getSessionData(Key.E_EFFECTS_LOCATIONS) != null) {
                        locations = (LinkedList<String>) context.getSessionData(Key.E_EFFECTS_LOCATIONS);
                    } else {
                        locations = new LinkedList<>();
                    }
                    if (locations != null) {
                        locations.add(BukkitConfigUtil.getLocationInfo(loc));
                    }
                    context.setSessionData(Key.E_EFFECTS_LOCATIONS, locations);
                    selectedEffectLocations.remove(player.getUniqueId());
                } else {
                    player.sendMessage(ChatColor.RED + Language.get("eventEditorSelectBlockFirst"));
                    return new ActionEffectSoundLocationPrompt(context);
                }
                return new ActionEffectSoundListPrompt(context);
            } else if (input.equalsIgnoreCase(Language.get("cmdCancel"))) {
                final Map<UUID, Block> selectedEffectLocations = plugin.getActionFactory().getSelectedEffectLocations();
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
            return Language.get("eventEditorExplosionPrompt");
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
            if (input.equalsIgnoreCase(Language.get("cmdAdd"))) {
                final Map<UUID, Block> selectedExplosionLocations = plugin.getActionFactory().getSelectedExplosionLocations();
                final Block block = selectedExplosionLocations.get(player.getUniqueId());
                if (block != null) {
                    final Location loc = block.getLocation();
                    final LinkedList<String> locations;
                    if (context.getSessionData(Key.E_EXPLOSIONS) != null) {
                        locations = (LinkedList<String>) context.getSessionData(Key.E_EXPLOSIONS);
                    } else {
                        locations = new LinkedList<>();
                    }
                    if (locations != null) {
                        locations.add(BukkitConfigUtil.getLocationInfo(loc));
                    }
                    context.setSessionData(Key.E_EXPLOSIONS, locations);
                    selectedExplosionLocations.remove(player.getUniqueId());
                    plugin.getActionFactory().setSelectedExplosionLocations(selectedExplosionLocations);
                } else {
                    player.sendMessage(ChatColor.RED + Language.get("eventEditorSelectBlockFirst"));
                    return new ActionEffectExplosionPrompt(context);
                }
                return new ActionMainPrompt(context);
            } else if (input.equalsIgnoreCase(Language.get("cmdClear"))) {
                context.setSessionData(Key.E_EXPLOSIONS, null);
                final Map<UUID, Block> selectedExplosionLocations = plugin.getActionFactory().getSelectedExplosionLocations();
                selectedExplosionLocations.remove(player.getUniqueId());
                plugin.getActionFactory().setSelectedExplosionLocations(selectedExplosionLocations);
                return new ActionMainPrompt(context);
            } else if (input.equalsIgnoreCase(Language.get("cmdCancel"))) {
                final Map<UUID, Block> selectedExplosionLocations = plugin.getActionFactory().getSelectedExplosionLocations();
                selectedExplosionLocations.remove(player.getUniqueId());
                plugin.getActionFactory().setSelectedExplosionLocations(selectedExplosionLocations);
                return new ActionMainPrompt(context);
            } else {
                return new ActionEffectExplosionPrompt(context);
            }
        }
    }
}
