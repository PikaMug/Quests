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

package me.blackvein.quests.convo.actions.tasks;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;

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

public class EffectPrompt extends ActionsEditorNumericPrompt {
    
    private final Quests plugin;
    
    public EffectPrompt(final ConversationContext context) {
        super(context);
        this.plugin = (Quests)context.getPlugin();
    }
    
    private final int size = 3;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle(final ConversationContext context) {
        return Lang.get("eventEditorEffect");
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
            return ChatColor.YELLOW + Lang.get("eventEditorSetEffects");
        case 2:
            return ChatColor.YELLOW + Lang.get("eventEditorSetExplosions");
        case 3:
            return ChatColor.GREEN + Lang.get("done");
        default:
            return null;
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public String getAdditionalText(final ConversationContext context, final int number) {
        switch (number) {
        case 1:
            if (context.getSessionData(CK.E_EFFECTS) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                String text = "\n";
                final LinkedList<String> effects = (LinkedList<String>) context.getSessionData(CK.E_EFFECTS);
                final LinkedList<String> locations = (LinkedList<String>) context.getSessionData(CK.E_EFFECTS_LOCATIONS);
                for (final String effect : effects) {
                    text += ChatColor.GRAY + "     - " + ChatColor.AQUA + effect + ChatColor.GRAY + " at " 
                            + ChatColor.DARK_AQUA + locations.get(effects.indexOf(effect)) + "\n";
                }
                return text;
            }
        case 2:
            if (context.getSessionData(CK.E_EXPLOSIONS) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                String text = "\n";
                final LinkedList<String> locations = (LinkedList<String>) context.getSessionData(CK.E_EXPLOSIONS);
                for (final String loc : locations) {
                    text += ChatColor.GRAY + "     - " + ChatColor.AQUA + loc + "\n";
                }
                return text;
            }
        case 3:
            return "";
        default:
            return null;
        }
    }

    @Override
    public String getPromptText(final ConversationContext context) {
        final ActionsEditorPostOpenNumericPromptEvent event
                = new ActionsEditorPostOpenNumericPromptEvent(context, this);
        plugin.getServer().getPluginManager().callEvent(event);
        
        String text = ChatColor.GOLD + "- " + getTitle(context) + " -\n";
        for (int i = 1; i <= size; i++) {
            text += getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                    + getSelectionText(context, i) + " " + getAdditionalText(context, i) + "\n";
        }
        return text;
    }

    @Override
    protected Prompt acceptValidatedInput(final ConversationContext context, final Number input) {
        switch (input.intValue()) {
        case 1:
            return new EffectSoundListPrompt(context);
        case 2:
            final Map<UUID, Block> selectedExplosionLocations = plugin.getActionFactory().getSelectedExplosionLocations();
            selectedExplosionLocations.put(((Player) context.getForWhom()).getUniqueId(), null);
            plugin.getActionFactory().setSelectedExplosionLocations(selectedExplosionLocations);
            return new EffectExplosionPrompt(context);
        case 3:
            return new ActionMainPrompt(context);
        default:
            return null;
        }
    }
    
    public class EffectSoundListPrompt extends ActionsEditorNumericPrompt {

        private final Quests plugin;
        
        public EffectSoundListPrompt(final ConversationContext context) {
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
            return Lang.get("eventEditorEffects");
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
                return ChatColor.YELLOW + Lang.get("eventEditorAddEffect");
            case 2:
                return ChatColor.YELLOW + Lang.get("eventEditorAddEffectLocation");
            case 3:
                return ChatColor.RED + Lang.get("clear");
            case 4:
                return ChatColor.GREEN + Lang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                if (context.getSessionData(CK.E_EFFECTS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    String text = "\n";
                    for (final String s : (List<String>) context.getSessionData(CK.E_EFFECTS)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + s + "\n";
                    }
                    return text;
                }
            case 2:
                if (context.getSessionData(CK.E_EFFECTS_LOCATIONS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    String text = "\n";
                    for (final String s : (List<String>) context.getSessionData(CK.E_EFFECTS_LOCATIONS)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + s + "\n";
                    }
                    return text;
                }
            case 3:
            case 4:
                return "";
            default:
                return null;
            }
        }

        @Override
        public String getPromptText(final ConversationContext context) {
            final ActionsEditorPostOpenNumericPromptEvent event
                    = new ActionsEditorPostOpenNumericPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
    
            String text = ChatColor.GOLD + "- " + getTitle(context) + " -\n";
            for (int i = 1; i <= size; i++) {
                text += getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                        + getSelectionText(context, i) + " " + getAdditionalText(context, i) + "\n";
            }
            return text;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        protected Prompt acceptValidatedInput(final ConversationContext context, final Number input) {
            switch (input.intValue()) {
            case 1:
                return new EffectSoundPrompt(context);
            case 2:
                if (context.getSessionData(CK.E_EFFECTS) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorMustAddEffects"));
                    return new EffectSoundListPrompt(context);
                } else {
                    final Map<UUID, Block> selectedEffectLocations = plugin.getActionFactory().getSelectedEffectLocations();
                    selectedEffectLocations.put(((Player) context.getForWhom()).getUniqueId(), null);
                    plugin.getActionFactory().setSelectedEffectLocations(selectedEffectLocations);
                    return new EffectSoundLocationPrompt(context);
                }
            case 3:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("eventEditorEffectsCleared"));
                context.setSessionData(CK.E_EFFECTS, null);
                context.setSessionData(CK.E_EFFECTS_LOCATIONS, null);
                return new EffectSoundListPrompt(context);
            case 4:
                int one;
                int two;
                if (context.getSessionData(CK.E_EFFECTS) != null) {
                    one = ((List<String>) context.getSessionData(CK.E_EFFECTS)).size();
                } else {
                    one = 0;
                }
                if (context.getSessionData(CK.E_EFFECTS_LOCATIONS) != null) {
                    two = ((List<String>) context.getSessionData(CK.E_EFFECTS_LOCATIONS)).size();
                } else {
                    two = 0;
                }
                if (one == two) {
                    return new ActionMainPrompt(context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                    return new EffectSoundListPrompt(context);
                }
            default:
                return null;
            }
        }
    }
    
    public class EffectSoundPrompt extends ActionsEditorStringPrompt {

        public EffectSoundPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("eventEditorEffectsTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("effEnterName");
        }

        @Override
        public String getPromptText(final ConversationContext context) {
            final ActionsEditorPostOpenStringPromptEvent event
                    = new ActionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            String effects = ChatColor.LIGHT_PURPLE + getTitle(context) + "\n";
            final Effect[] vals = Effect.values();
            for (int i = 0; i < vals.length; i++) {
                final Effect eff = vals[i];
                if (i < (vals.length - 1)) {
                    effects += MiscUtil.snakeCaseToUpperCamelCase(eff.name()) + ", ";
                } else {
                    effects += MiscUtil.snakeCaseToUpperCamelCase(eff.name()) + "\n";
                }
                
            }
            return effects + ChatColor.YELLOW + getQueryText(context);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            final Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                if (MiscUtil.getProperEffect(input) != null) {
                    LinkedList<String> effects;
                    if (context.getSessionData(CK.E_EFFECTS) != null) {
                        effects = (LinkedList<String>) context.getSessionData(CK.E_EFFECTS);
                    } else {
                        effects = new LinkedList<String>();
                    }
                    effects.add(input.toUpperCase());
                    context.setSessionData(CK.E_EFFECTS, effects);
                    final Map<UUID, Block> selectedEffectLocations = plugin.getActionFactory().getSelectedEffectLocations();
                    selectedEffectLocations.remove(player.getUniqueId());
                    plugin.getActionFactory().setSelectedEffectLocations(selectedEffectLocations);
                    return new EffectSoundListPrompt(context);
                } else {
                    player.sendMessage(ChatColor.LIGHT_PURPLE + input + " " + ChatColor.RED 
                            + Lang.get("eventEditorInvalidEffect"));
                    return new EffectSoundPrompt(context);
                }
            } else {
                final Map<UUID, Block> selectedEffectLocations = plugin.getActionFactory().getSelectedEffectLocations();
                selectedEffectLocations.remove(player.getUniqueId());
                plugin.getActionFactory().setSelectedEffectLocations(selectedEffectLocations);
                return new EffectSoundListPrompt(context);
            }
        }
    }
    
    public class EffectSoundLocationPrompt extends ActionsEditorStringPrompt {

        public EffectSoundLocationPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("eventEditorEffectLocationPrompt");
        }

        @Override
        public String getPromptText(final ConversationContext context) {
            final ActionsEditorPostOpenStringPromptEvent event
                    = new ActionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            final Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(Lang.get("cmdAdd"))) {
                final Map<UUID, Block> selectedEffectLocations = plugin.getActionFactory().getSelectedEffectLocations();
                final Block block = selectedEffectLocations.get(player.getUniqueId());
                if (block != null) {
                    final Location loc = block.getLocation();
                    LinkedList<String> locs;
                    if (context.getSessionData(CK.E_EFFECTS_LOCATIONS) != null) {
                        locs = (LinkedList<String>) context.getSessionData(CK.E_EFFECTS_LOCATIONS);
                    } else {
                        locs = new LinkedList<String>();
                    }
                    locs.add(ConfigUtil.getLocationInfo(loc));
                    context.setSessionData(CK.E_EFFECTS_LOCATIONS, locs);
                    selectedEffectLocations.remove(player.getUniqueId());
                } else {
                    player.sendMessage(ChatColor.RED + Lang.get("eventEditorSelectBlockFirst"));
                    return new EffectSoundLocationPrompt(context);
                }
                return new EffectSoundListPrompt(context);
            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                final Map<UUID, Block> selectedEffectLocations = plugin.getActionFactory().getSelectedEffectLocations();
                selectedEffectLocations.remove(player.getUniqueId());
                plugin.getActionFactory().setSelectedEffectLocations(selectedEffectLocations);
                return new EffectSoundListPrompt(context);
            } else {
                return new EffectSoundLocationPrompt(context);
            }
        }
    }
    
    public class EffectExplosionPrompt extends ActionsEditorStringPrompt {

        public EffectExplosionPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("eventEditorExplosionPrompt");
        }

        @Override
        public String getPromptText(final ConversationContext context) {
            final ActionsEditorPostOpenStringPromptEvent event
                    = new ActionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            final Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(Lang.get("cmdAdd"))) {
                final Map<UUID, Block> selectedExplosionLocations = plugin.getActionFactory().getSelectedExplosionLocations();
                final Block block = selectedExplosionLocations.get(player.getUniqueId());
                if (block != null) {
                    final Location loc = block.getLocation();
                    LinkedList<String> locs;
                    if (context.getSessionData(CK.E_EXPLOSIONS) != null) {
                        locs = (LinkedList<String>) context.getSessionData(CK.E_EXPLOSIONS);
                    } else {
                        locs = new LinkedList<String>();
                    }
                    locs.add(ConfigUtil.getLocationInfo(loc));
                    context.setSessionData(CK.E_EXPLOSIONS, locs);
                    selectedExplosionLocations.remove(player.getUniqueId());
                    plugin.getActionFactory().setSelectedExplosionLocations(selectedExplosionLocations);
                } else {
                    player.sendMessage(ChatColor.RED + Lang.get("eventEditorSelectBlockFirst"));
                    return new EffectExplosionPrompt(context);
                }
                return new ActionMainPrompt(context);
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.E_EXPLOSIONS, null);
                final Map<UUID, Block> selectedExplosionLocations = plugin.getActionFactory().getSelectedExplosionLocations();
                selectedExplosionLocations.remove(player.getUniqueId());
                plugin.getActionFactory().setSelectedExplosionLocations(selectedExplosionLocations);
                return new ActionMainPrompt(context);
            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                final Map<UUID, Block> selectedExplosionLocations = plugin.getActionFactory().getSelectedExplosionLocations();
                selectedExplosionLocations.remove(player.getUniqueId());
                plugin.getActionFactory().setSelectedExplosionLocations(selectedExplosionLocations);
                return new ActionMainPrompt(context);
            } else {
                return new EffectExplosionPrompt(context);
            }
        }
    }
}
