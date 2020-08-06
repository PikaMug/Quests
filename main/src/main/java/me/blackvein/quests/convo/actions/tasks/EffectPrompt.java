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
import org.bukkit.conversations.FixedSetPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import me.blackvein.quests.Quests;
import me.blackvein.quests.convo.actions.main.ActionMainPrompt;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.ConfigUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;

public class EffectPrompt extends FixedSetPrompt {
    
    private final Quests plugin;
    
    public EffectPrompt(final ConversationContext context) {
        super("1", "2", "3");
        this.plugin = (Quests)context.getPlugin();
    }

    @SuppressWarnings("unchecked")
    @Override
    public String getPromptText(final ConversationContext context) {
        String text = ChatColor.GOLD + "- " + Lang.get("eventEditorEffect") + " -\n";
        if (context.getSessionData(CK.E_EFFECTS) == null) {
            text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("eventEditorSetEffects") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("eventEditorSetEffects") + "\n";
            final LinkedList<String> effects = (LinkedList<String>) context.getSessionData(CK.E_EFFECTS);
            final LinkedList<String> locations = (LinkedList<String>) context.getSessionData(CK.E_EFFECTS_LOCATIONS);
            for (final String effect : effects) {
                text += ChatColor.GRAY + "    - " + ChatColor.AQUA + effect + ChatColor.GRAY + " at " 
                        + ChatColor.DARK_AQUA + locations.get(effects.indexOf(effect)) + "\n";
            }
        }
        if (context.getSessionData(CK.E_EXPLOSIONS) == null) {
            text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("eventEditorSetExplosions") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("eventEditorSetExplosions") + "\n";
            final LinkedList<String> locations = (LinkedList<String>) context.getSessionData(CK.E_EXPLOSIONS);
            for (final String loc : locations) {
                text += ChatColor.GRAY + "    - " + ChatColor.AQUA + loc + "\n";
            }
        }
        text += ChatColor.GREEN + "" + ChatColor.BOLD + "3 " + ChatColor.RESET + ChatColor.YELLOW + "- " 
                + Lang.get("done") + "\n";
        return text;
    }

    @Override
    protected Prompt acceptValidatedInput(final ConversationContext context, final String input) {
        if (input.equalsIgnoreCase("1")) {
            return new SoundEffectListPrompt();
        } else if (input.equalsIgnoreCase("2")) {
            final Map<UUID, Block> selectedExplosionLocations = plugin.getActionFactory().getSelectedExplosionLocations();
            selectedExplosionLocations.put(((Player) context.getForWhom()).getUniqueId(), null);
            plugin.getActionFactory().setSelectedExplosionLocations(selectedExplosionLocations);
            return new ExplosionPrompt();
        }
        return new ActionMainPrompt(context);
    }
    
    private class SoundEffectListPrompt extends FixedSetPrompt {

        public SoundEffectListPrompt() {
            super("1", "2", "3", "4");
        }

        @Override
        public String getPromptText(final ConversationContext context) {
            String text = ChatColor.GOLD + "- " + Lang.get("eventEditorEffects") + " -\n";
            if (context.getSessionData(CK.E_EFFECTS) == null) {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("eventEditorAddEffect") + " (" + Lang.get("noneSet") + ")\n";
                text += ChatColor.GRAY + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - "
                        + Lang.get("eventEditorAddEffectLocation") + " (" + Lang.get("eventEditorNoEffects") + ")\n";
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("clear") + "\n";
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("done");
            } else {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("eventEditorAddEffect") + "\n";
                for (final String s : getEffects(context)) {
                    text += ChatColor.GRAY + "    - " + ChatColor.AQUA + s + "\n";
                }
                if (context.getSessionData(CK.E_EFFECTS_LOCATIONS) == null) {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                            + Lang.get("eventEditorAddEffectLocation") + " (" + Lang.get("noneSet") + ")\n";
                } else {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                            + Lang.get("eventEditorAddEffectLocation") + "\n";
                    for (final String s : getEffectLocations(context)) {
                        text += ChatColor.GRAY + "    - " + ChatColor.AQUA + s + "\n";
                    }
                }
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("clear") + "\n";
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("done");
            }
            return text;
        }

        @Override
        protected Prompt acceptValidatedInput(final ConversationContext context, final String input) {
            if (input.equalsIgnoreCase("1")) {
                return new SoundEffectPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(CK.E_EFFECTS) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorMustAddEffects"));
                    return new SoundEffectListPrompt();
                } else {
                    final Map<UUID, Block> selectedEffectLocations = plugin.getActionFactory().getSelectedEffectLocations();
                    selectedEffectLocations.put(((Player) context.getForWhom()).getUniqueId(), null);
                    plugin.getActionFactory().setSelectedEffectLocations(selectedEffectLocations);
                    return new SoundEffectLocationPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("eventEditorEffectsCleared"));
                context.setSessionData(CK.E_EFFECTS, null);
                context.setSessionData(CK.E_EFFECTS_LOCATIONS, null);
                return new SoundEffectListPrompt();
            } else if (input.equalsIgnoreCase("4")) {
                int one;
                int two;
                if (context.getSessionData(CK.E_EFFECTS) != null) {
                    one = getEffects(context).size();
                } else {
                    one = 0;
                }
                if (context.getSessionData(CK.E_EFFECTS_LOCATIONS) != null) {
                    two = getEffectLocations(context).size();
                } else {
                    two = 0;
                }
                if (one == two) {
                    return new ActionMainPrompt(context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                    return new SoundEffectListPrompt();
                }
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        private List<String> getEffects(final ConversationContext context) {
            return (List<String>) context.getSessionData(CK.E_EFFECTS);
        }

        @SuppressWarnings("unchecked")
        private List<String> getEffectLocations(final ConversationContext context) {
            return (List<String>) context.getSessionData(CK.E_EFFECTS_LOCATIONS);
        }
    }
    
    private class SoundEffectPrompt extends StringPrompt {

        @Override
        public String getPromptText(final ConversationContext context) {
            String effects = ChatColor.LIGHT_PURPLE + Lang.get("eventEditorEffectsTitle") + "\n";
            final Effect[] vals = Effect.values();
            for (int i = 0; i < vals.length; i++) {
                final Effect eff = vals[i];
                if (i < (vals.length - 1)) {
                    effects += MiscUtil.snakeCaseToUpperCamelCase(eff.name()) + ", ";
                } else {
                    effects += MiscUtil.snakeCaseToUpperCamelCase(eff.name()) + "\n";
                }
                
            }
            return effects + ChatColor.YELLOW +  Lang.get("effEnterName");
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
                    return new SoundEffectListPrompt();
                } else {
                    player.sendMessage(ChatColor.LIGHT_PURPLE + input + " " + ChatColor.RED 
                            + Lang.get("eventEditorInvalidEffect"));
                    return new SoundEffectPrompt();
                }
            } else {
                final Map<UUID, Block> selectedEffectLocations = plugin.getActionFactory().getSelectedEffectLocations();
                selectedEffectLocations.remove(player.getUniqueId());
                plugin.getActionFactory().setSelectedEffectLocations(selectedEffectLocations);
                return new SoundEffectListPrompt();
            }
        }
    }
    
    private class SoundEffectLocationPrompt extends StringPrompt {

        @Override
        public String getPromptText(final ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("eventEditorEffectLocationPrompt");
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
                    return new SoundEffectLocationPrompt();
                }
                return new SoundEffectListPrompt();
            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                final Map<UUID, Block> selectedEffectLocations = plugin.getActionFactory().getSelectedEffectLocations();
                selectedEffectLocations.remove(player.getUniqueId());
                plugin.getActionFactory().setSelectedEffectLocations(selectedEffectLocations);
                return new SoundEffectListPrompt();
            } else {
                return new SoundEffectLocationPrompt();
            }
        }
    }
    
    public class ExplosionPrompt extends StringPrompt {

        @Override
        public String getPromptText(final ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("eventEditorExplosionPrompt");
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
                    return new ExplosionPrompt();
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
                return new ExplosionPrompt();
            }
        }
    }
}
