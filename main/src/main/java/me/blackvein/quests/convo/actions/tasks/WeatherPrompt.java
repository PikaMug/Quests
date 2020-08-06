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
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.FixedSetPrompt;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import me.blackvein.quests.Quests;
import me.blackvein.quests.convo.actions.main.ActionMainPrompt;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.ConfigUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;

public class WeatherPrompt extends FixedSetPrompt {
    
    private final Quests plugin;
    
    public WeatherPrompt(final ConversationContext context) {
        super("1", "2", "3", "4");
        this.plugin = (Quests)context.getPlugin();
    }

    @SuppressWarnings("unchecked")
    @Override
    public String getPromptText(final ConversationContext context) {
        String text = ChatColor.GOLD + "- " + Lang.get("eventEditorWeather") + " -\n";
        if (context.getSessionData(CK.E_WORLD_STORM) == null) {
            text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("eventEditorSetStorm") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("eventEditorSetStorm") + " (" + ChatColor.AQUA 
                    + (String) context.getSessionData(CK.E_WORLD_STORM) + ChatColor.YELLOW + " -> " 
                    + ChatColor.DARK_AQUA + MiscUtil.getTime(Long.valueOf((int)context
                    .getSessionData(CK.E_WORLD_STORM_DURATION) * 1000)) + ChatColor.YELLOW + ")\n";
        }
        if (context.getSessionData(CK.E_WORLD_THUNDER) == null) {
            text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("eventEditorSetThunder") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("eventEditorSetThunder") + " (" + ChatColor.AQUA 
                    + (String) context.getSessionData(CK.E_WORLD_THUNDER) + ChatColor.YELLOW + " -> " 
                    + ChatColor.DARK_AQUA + MiscUtil.getTime(Long.valueOf((int)context
                    .getSessionData(CK.E_WORLD_THUNDER_DURATION) * 1000)) + ChatColor.YELLOW + ")\n";
        }
        
        if (context.getSessionData(CK.E_LIGHTNING) == null) {
            text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("eventEditorSetLightning") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("eventEditorSetLightning") + "\n";
            final LinkedList<String> locations = (LinkedList<String>) context.getSessionData(CK.E_LIGHTNING);
            for (final String loc : locations) {
                text += ChatColor.GRAY + "    - " + ChatColor.AQUA + loc + "\n";
            }
        }
        text += ChatColor.GREEN + "" + ChatColor.BOLD + "4 " + ChatColor.RESET + ChatColor.GREEN + "- " 
                + Lang.get("done") + "\n";
        return text;
    }

    @Override
    protected Prompt acceptValidatedInput(final ConversationContext context, final String input) {
        if (input.equalsIgnoreCase("1")) {
            return new StormPrompt();
        } else if (input.equalsIgnoreCase("2")) {
            return new ThunderPrompt();
        } else if (input.equalsIgnoreCase("3")) {
            final Map<UUID, Block> selectedLightningLocations = plugin.getActionFactory().getSelectedLightningLocations();
            selectedLightningLocations.put(((Player) context.getForWhom()).getUniqueId(), null);
            plugin.getActionFactory().setSelectedLightningLocations(selectedLightningLocations);
            return new LightningPrompt();
        }
        return new ActionMainPrompt(context);
    }
    
    private class StormPrompt extends FixedSetPrompt {

        public StormPrompt() {
            super("1", "2", "3", "4");
        }

        @Override
        public String getPromptText(final ConversationContext context) {
            String text = ChatColor.GOLD + Lang.get("eventEditorStormTitle") + "\n";
            if (context.getSessionData(CK.E_WORLD_STORM) == null) {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("eventEditorSetWorld") + " (" + Lang.get("noneSet") + ")\n";
                text += ChatColor.GRAY + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.GRAY + " - " 
                        + Lang.get("eventEditorSetDuration") + " " + Lang.get("eventEditorNoWorld") + "\n";
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("clear") + "\n";
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("done");
            } else {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("eventEditorSetWorld") + " (" + ChatColor.AQUA 
                        + ((String) context.getSessionData(CK.E_WORLD_STORM)) + ChatColor.YELLOW + ")\n";
                if (context.getSessionData(CK.E_WORLD_STORM_DURATION) == null) {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                            + Lang.get("eventEditorSetDuration") + " (" + Lang.get("noneSet") + ")\n";
                } else {
                    final int dur = (int) context.getSessionData(CK.E_WORLD_STORM_DURATION);
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                            + Lang.get("eventEditorSetDuration") + " (" + ChatColor.AQUA + MiscUtil.getTime(dur * 1000) 
                            + ChatColor.YELLOW + ")\n";
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
                return new StormWorldPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(CK.E_WORLD_STORM) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorSetWorldFirst"));
                    return new StormPrompt();
                } else {
                    return new StormDurationPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("eventEditorStormCleared"));
                context.setSessionData(CK.E_WORLD_STORM, null);
                context.setSessionData(CK.E_WORLD_STORM_DURATION, null);
                return new StormPrompt();
            } else if (input.equalsIgnoreCase("4")) {
                if (context.getSessionData(CK.E_WORLD_STORM) != null 
                        && context.getSessionData(CK.E_WORLD_STORM_DURATION) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorMustSetStormDuration"));
                    return new StormPrompt();
                } else {
                    return new ActionMainPrompt(context);
                }
            }
            return null;
        }
    }

    private class StormWorldPrompt extends StringPrompt {

        @Override
        public String getPromptText(final ConversationContext context) {
            String effects = ChatColor.LIGHT_PURPLE + Lang.get("eventEditorWorldsTitle") + "\n" + ChatColor.DARK_PURPLE;
            for (final World w : plugin.getServer().getWorlds()) {
                effects += w.getName() + ", ";
            }
            effects = effects.substring(0, effects.length());
            return ChatColor.YELLOW + effects + Lang.get("eventEditorEnterStormWorld");
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            final Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                if (plugin.getServer().getWorld(input) != null) {
                    context.setSessionData(CK.E_WORLD_STORM, plugin.getServer().getWorld(input).getName());
                } else {
                    player.sendMessage(ChatColor.LIGHT_PURPLE + input + " " + ChatColor.RED 
                            + Lang.get("eventEditorInvalidWorld"));
                    return new StormWorldPrompt();
                }
            }
            return new StormPrompt();
        }
    }

    private class StormDurationPrompt extends NumericPrompt {

        @Override
        public String getPromptText(final ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("eventEditorEnterDuration");
        }

        @Override
        protected Prompt acceptValidatedInput(final ConversationContext context, final Number input) {
            if (input.intValue() < 1) {
                context.getForWhom().sendRawMessage(ChatColor.RED 
                        + Lang.get("invalidMinimum").replace("<number>", "1"));
                return new StormDurationPrompt();
            } else {
                context.setSessionData(CK.E_WORLD_STORM_DURATION, input.intValue());
            }
            return new StormPrompt();
        }
    }

    private class ThunderPrompt extends FixedSetPrompt {

        public ThunderPrompt() {
            super("1", "2", "3", "4");
        }

        @Override
        public String getPromptText(final ConversationContext context) {
            String text = ChatColor.GOLD + Lang.get("eventEditorThunderTitle") + "\n";
            if (context.getSessionData(CK.E_WORLD_THUNDER) == null) {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("eventEditorSetWorld") + " (" + Lang.get("noneSet") + ")\n";
                text += ChatColor.GRAY + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.GRAY + " - " 
                        + Lang.get("eventEditorSetDuration") + " " + Lang.get("eventEditorNoWorld") + "\n";
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("clear") + "\n";
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("done");
            } else {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("eventEditorSetWorld") + " (" + ChatColor.AQUA 
                        + ((String) context.getSessionData(CK.E_WORLD_THUNDER)) + ChatColor.YELLOW + ")\n";
                if (context.getSessionData(CK.E_WORLD_THUNDER_DURATION) == null) {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                            + Lang.get("eventEditorSetDuration") + " (" + Lang.get("noneSet") + ")\n";
                } else {
                    final int dur = (int) context.getSessionData(CK.E_WORLD_THUNDER_DURATION);
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                            + Lang.get("eventEditorSetDuration") + " (" + ChatColor.AQUA + MiscUtil.getTime(dur * 1000) 
                            + ChatColor.YELLOW + ")\n";
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
                return new ThunderWorldPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(CK.E_WORLD_THUNDER) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorSetWorldFirst"));
                    return new ThunderPrompt();
                } else {
                    return new ThunderDurationPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("eventEditorThunderCleared"));
                context.setSessionData(CK.E_WORLD_THUNDER, null);
                context.setSessionData(CK.E_WORLD_THUNDER_DURATION, null);
                return new ThunderPrompt();
            } else if (input.equalsIgnoreCase("4")) {
                if (context.getSessionData(CK.E_WORLD_THUNDER) != null 
                        && context.getSessionData(CK.E_WORLD_THUNDER_DURATION) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorMustSetThunderDuration"));
                    return new ThunderPrompt();
                } else {
                    return new ActionMainPrompt(context);
                }
            }
            return null;
        }
    }

    private class ThunderWorldPrompt extends StringPrompt {

        @Override
        public String getPromptText(final ConversationContext context) {
            String effects = ChatColor.LIGHT_PURPLE + Lang.get("eventEditorWorldsTitle") + "\n" + ChatColor.DARK_PURPLE;
            for (final World w : plugin.getServer().getWorlds()) {
                effects += w.getName() + ", ";
            }
            effects = effects.substring(0, effects.length());
            return ChatColor.YELLOW + effects + Lang.get("eventEditorEnterThunderWorld");
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            final Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                if (plugin.getServer().getWorld(input) != null) {
                    context.setSessionData(CK.E_WORLD_THUNDER, plugin.getServer().getWorld(input).getName());
                } else {
                    player.sendMessage(ChatColor.LIGHT_PURPLE + input + " " + ChatColor.RED 
                            + Lang.get("eventEditorInvalidWorld"));
                    return new ThunderWorldPrompt();
                }
            }
            return new ThunderPrompt();
        }
    }

    private class ThunderDurationPrompt extends NumericPrompt {

        @Override
        public String getPromptText(final ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("eventEditorEnterDuration");
        }

        @Override
        protected Prompt acceptValidatedInput(final ConversationContext context, final Number input) {
            if (input.intValue() < 1) {
                context.getForWhom().sendRawMessage(ChatColor.RED 
                        + Lang.get("invalidMinimum").replace("<number>", "1"));
                return new ThunderDurationPrompt();
            } else {
                context.setSessionData(CK.E_WORLD_THUNDER_DURATION, input.intValue());
            }
            return new ThunderPrompt();
        }
    }
    
    public class LightningPrompt extends StringPrompt {

        @Override
        public String getPromptText(final ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("eventEditorLightningPrompt");
        }

        @SuppressWarnings("unchecked")
        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            final Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(Lang.get("cmdAdd"))) {
                final Map<UUID, Block> selectedLightningLocations = plugin.getActionFactory().getSelectedLightningLocations();
                final Block block = selectedLightningLocations.get(player.getUniqueId());
                if (block != null) {
                    final Location loc = block.getLocation();
                    LinkedList<String> locs;
                    if (context.getSessionData(CK.E_LIGHTNING) != null) {
                        locs = (LinkedList<String>) context.getSessionData(CK.E_LIGHTNING);
                    } else {
                        locs = new LinkedList<String>();
                    }
                    locs.add(ConfigUtil.getLocationInfo(loc));
                    context.setSessionData(CK.E_LIGHTNING, locs);
                    selectedLightningLocations.remove(player.getUniqueId());
                    plugin.getActionFactory().setSelectedLightningLocations(selectedLightningLocations);
                } else {
                    player.sendMessage(ChatColor.RED + Lang.get("eventEditorSelectBlockFirst"));
                    return new LightningPrompt();
                }
                return new ActionMainPrompt(context);
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.E_LIGHTNING, null);
                final Map<UUID, Block> selectedLightningLocations = plugin.getActionFactory().getSelectedLightningLocations();
                selectedLightningLocations.remove(player.getUniqueId());
                plugin.getActionFactory().setSelectedLightningLocations(selectedLightningLocations);
                return new ActionMainPrompt(context);
            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                final Map<UUID, Block> selectedLightningLocations = plugin.getActionFactory().getSelectedLightningLocations();
                selectedLightningLocations.remove(player.getUniqueId());
                plugin.getActionFactory().setSelectedLightningLocations(selectedLightningLocations);
                return new ActionMainPrompt(context);
            } else {
                return new LightningPrompt();
            }
        }
    }
}
