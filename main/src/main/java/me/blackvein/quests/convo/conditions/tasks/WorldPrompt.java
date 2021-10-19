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

package me.blackvein.quests.convo.conditions.tasks;

import com.sk89q.worldguard.protection.managers.RegionManager;
import me.blackvein.quests.Quests;
import me.blackvein.quests.convo.conditions.main.ConditionMainPrompt;
import me.blackvein.quests.convo.quests.QuestsEditorNumericPrompt;
import me.blackvein.quests.convo.quests.QuestsEditorStringPrompt;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenNumericPromptEvent;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenStringPromptEvent;
import me.blackvein.quests.reflect.worldguard.WorldGuardAPI;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class WorldPrompt extends QuestsEditorNumericPrompt {
    
    private final Quests plugin;
    
    public WorldPrompt(final ConversationContext context) {
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
        return Lang.get("conditionEditorWorld");
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
        switch(number) {
        case 1:
            return ChatColor.YELLOW + Lang.get("conditionEditorStayWithinWorld");
        case 2:
            return ChatColor.YELLOW + Lang.get("conditionEditorStayWithinBiome");
        case 3:
            if (plugin.getDependencies().getWorldGuardApi() != null) {
                return ChatColor.YELLOW + Lang.get("conditionEditorStayWithinRegion");
            } else {
                return ChatColor.GRAY + Lang.get("conditionEditorStayWithinRegion");
            }
        case 4:
            return ChatColor.GREEN + Lang.get("done");
        default:
            return null;
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public String getAdditionalText(final ConversationContext context, final int number) {
        switch(number) {
        case 1:
            if (context.getSessionData(CK.C_WHILE_WITHIN_WORLD) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder("\n");
                final List<String> whileWithinWorld = (List<String>) context.getSessionData(CK.C_WHILE_WITHIN_WORLD);
                if (whileWithinWorld != null) {
                    for (final String s: whileWithinWorld) {
                        text.append(ChatColor.GRAY).append("     - ").append(ChatColor.BLUE).append(s).append("\n");
                    }
                }
                return text.toString();
            }
        case 2:
            if (context.getSessionData(CK.C_WHILE_WITHIN_BIOME) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder("\n");
                final List<String> whileWithinBiome = (List<String>) context.getSessionData(CK.C_WHILE_WITHIN_BIOME);
                if (whileWithinBiome != null) {
                    for (final String s: whileWithinBiome) {
                        text.append(ChatColor.GRAY).append("     - ").append(ChatColor.BLUE).append(s).append("\n");
                    }
                }
                return text.toString();
            }
        case 3:
            if (plugin.getDependencies().getWorldGuardApi() != null) {
                if (context.getSessionData(CK.C_WHILE_WITHIN_REGION) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder("\n");
                    final List<String> whileWithinRegion
                            = (List<String>) context.getSessionData(CK.C_WHILE_WITHIN_REGION);
                    if (whileWithinRegion != null) {
                        for (final String s: whileWithinRegion) {
                            text.append(ChatColor.GRAY).append("     - ").append(ChatColor.BLUE).append(s).append("\n");
                        }
                    }
                    return text.toString();
                }
            } else {
                return ChatColor.GRAY + "(" + Lang.get("notInstalled") + ")";
            }
        case 4:
            return "";
        default:
            return null;
        }
    }

    @Override
    public @NotNull String getBasicPromptText(final @NotNull ConversationContext context) {
        if (context.getPlugin() != null) {
            final QuestsEditorPostOpenNumericPromptEvent event
                    = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
        }
        
        final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle(context) + " -");
        for (int i = 1; i <= size; i++) {
            text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                    .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i)).append(" ")
                    .append(getAdditionalText(context, i));
        }
        return text.toString();
    }
    
    @Override
    protected Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
        switch(input.intValue()) {
        case 1:
            return new WorldsPrompt(context);
        case 2:
            return new BiomesPrompt(context);
        case 3:
            return new RegionsPrompt(context);
        case 4:
            try {
                return new ConditionMainPrompt(context);
            } catch (final Exception e) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateCriticalError"));
                return Prompt.END_OF_CONVERSATION;
            }
        default:
            return new WorldPrompt(context);
        }
    }
    
    public class WorldsPrompt extends QuestsEditorStringPrompt {
        
        public WorldsPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("conditionEditorWorldsTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("conditionEditorWorldsPrompt");
        }
        
        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            final StringBuilder worlds = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle(context) + "\n");
            final List<World> worldArr = Bukkit.getWorlds();
            for (int i = 0; i < worldArr.size(); i++) {
                if (i < (worldArr.size() - 1)) {
                    worlds.append(worldArr.get(i).getName()).append(", ");
                } else {
                    worlds.append(worldArr.get(i).getName()).append("\n");
                }
            }
            return worlds.toString() + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                final LinkedList<String> worlds = new LinkedList<String>();
                for (final String s : input.split(" ")) {
                    if (Bukkit.getWorld(s) != null) {
                        worlds.add(s);
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("conditionEditorInvalidWorld"));
                        return new WorldsPrompt(context);
                    }
                }
                context.setSessionData(CK.C_WHILE_WITHIN_WORLD, worlds);
            }
            return new WorldPrompt(context);
        }
    }
    
    public class BiomesPrompt extends QuestsEditorStringPrompt {
        
        public BiomesPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("conditionEditorBiomesTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("conditionEditorBiomesPrompt");
        }
        
        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            final StringBuilder biomes = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle(context) + "\n");
            final LinkedList<Biome> biomeArr = new LinkedList<Biome>(Arrays.asList(Biome.values()));
            for (int i = 0; i < biomeArr.size(); i++) {
                if (i < (biomeArr.size() - 1)) {
                    biomes.append(MiscUtil.snakeCaseToUpperCamelCase(biomeArr.get(i).name())).append(", ");
                } else {
                    biomes.append(MiscUtil.snakeCaseToUpperCamelCase(biomeArr.get(i).name())).append("\n");
                }
            }
            return biomes.toString() + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                final LinkedList<String> biomes = new LinkedList<String>();
                for (final String s : input.split(" ")) {
                    if (MiscUtil.getProperBiome(s) != null) {
                        biomes.add(s);
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("conditionEditorInvalidBiome"));
                        return new BiomesPrompt(context);
                    }
                }
                context.setSessionData(CK.C_WHILE_WITHIN_BIOME, biomes);
            }
            return new WorldPrompt(context);
        }
    }
    
    public class RegionsPrompt extends QuestsEditorStringPrompt {
        
        public RegionsPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("conditionEditorRegionsTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("conditionEditorRegionsPrompt");
        }
        
        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            StringBuilder regions = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle(context) + "\n");
            boolean any = false;
            for (final World world : plugin.getServer().getWorlds()) {
                final WorldGuardAPI api = plugin.getDependencies().getWorldGuardApi();
                final RegionManager regionManager = api.getRegionManager(world);
                if (regionManager != null) {
                    for (final String region : regionManager.getRegions().keySet()) {
                        any = true;
                        regions.append(ChatColor.GREEN).append(region).append(", ");
                    }
                }
            }
            if (any) {
                regions = new StringBuilder(regions.substring(0, regions.length() - 2) + "\n");
            } else {
                regions.append(ChatColor.GRAY).append("(").append(Lang.get("none")).append(")\n");
            }
            return regions.toString() + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                final LinkedList<String> regions = new LinkedList<String>();
                for (final String r : input.split(" ")) {
                    boolean found = false;
                    for (final World world : plugin.getServer().getWorlds()) {
                        final WorldGuardAPI api = plugin.getDependencies().getWorldGuardApi();
                        final RegionManager regionManager = api.getRegionManager(world);
                        if (regionManager != null) {
                            for (final String region : regionManager.getRegions().keySet()) {
                                if (region.equalsIgnoreCase(r)) {
                                    regions.add(region);
                                    found = true;
                                    break;
                                }
                            }
                        }
                        
                        if (found) {
                            break;
                        }
                    }
                    if (!found) {
                        String error = Lang.get("questWGInvalidRegion");
                        error = error.replace("<region>", ChatColor.RED + r + ChatColor.YELLOW);
                        context.getForWhom().sendRawMessage(ChatColor.YELLOW + error);
                        return new RegionsPrompt(context);
                    }
                }
                context.setSessionData(CK.C_WHILE_WITHIN_REGION, regions);
            }
            return new WorldPrompt(context);
        }
    }
}
