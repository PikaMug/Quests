/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.convo.conditions.tasks;

import com.sk89q.worldguard.protection.managers.RegionManager;
import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.convo.conditions.ConditionsEditorNumericPrompt;
import me.pikamug.quests.convo.conditions.ConditionsEditorStringPrompt;
import me.pikamug.quests.convo.conditions.main.ConditionMainPrompt;
import me.pikamug.quests.events.editor.conditions.ConditionsEditorPostOpenNumericPromptEvent;
import me.pikamug.quests.events.editor.conditions.ConditionsEditorPostOpenStringPromptEvent;
import me.pikamug.quests.dependencies.reflect.worldguard.WorldGuardAPI;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.quests.util.BukkitMiscUtil;
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
import java.util.Objects;

public class ConditionWorldPrompt extends ConditionsEditorNumericPrompt {
    
    private final BukkitQuestsPlugin plugin;
    
    public ConditionWorldPrompt(final ConversationContext context) {
        super(context);
        this.plugin = (BukkitQuestsPlugin)context.getPlugin();
    }
    
    private final int size = 5;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle(final ConversationContext context) {
        return BukkitLang.get("conditionEditorWorld");
    }
    
    @Override
    public ChatColor getNumberColor(final ConversationContext context, final int number) {
        switch (number) {
            case 1:
            case 2:
            case 3:
            case 4:
                return ChatColor.BLUE;
            case 5:
                return ChatColor.GREEN;
            default:
                return null;
        }
    }
    
    @Override
    public String getSelectionText(final ConversationContext context, final int number) {
        switch(number) {
        case 1:
            return ChatColor.YELLOW + BukkitLang.get("conditionEditorStayWithinWorld");
        case 2:
            return ChatColor.YELLOW + BukkitLang.get("conditionEditorStayWithinTicks");
        case 3:
            return ChatColor.YELLOW + BukkitLang.get("conditionEditorStayWithinBiome");
        case 4:
            if (plugin.getDependencies().getWorldGuardApi() != null) {
                return ChatColor.YELLOW + BukkitLang.get("conditionEditorStayWithinRegion");
            } else {
                return ChatColor.GRAY + BukkitLang.get("conditionEditorStayWithinRegion");
            }
        case 5:
            return ChatColor.GREEN + BukkitLang.get("done");
        default:
            return null;
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public String getAdditionalText(final ConversationContext context, final int number) {
        switch(number) {
        case 1:
            if (context.getSessionData(Key.C_WHILE_WITHIN_WORLD) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final List<String> whileWithinWorld = (List<String>) context.getSessionData(Key.C_WHILE_WITHIN_WORLD);
                if (whileWithinWorld != null) {
                    for (final String s: whileWithinWorld) {
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.BLUE).append(s);
                    }
                }
                return text.toString();
            }
        case 2:
            if (context.getSessionData(Key.C_WHILE_WITHIN_TICKS_START) == null
                    || context.getSessionData(Key.C_WHILE_WITHIN_TICKS_END) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + context.getSessionData(Key.C_WHILE_WITHIN_TICKS_START)
                        + " - " + context.getSessionData(Key.C_WHILE_WITHIN_TICKS_END)+ ChatColor.GRAY + ")";
            }
        case 3:
            if (context.getSessionData(Key.C_WHILE_WITHIN_BIOME) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final List<String> whileWithinBiome = (List<String>) context.getSessionData(Key.C_WHILE_WITHIN_BIOME);
                if (whileWithinBiome != null) {
                    for (final String s: whileWithinBiome) {
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.BLUE).append(s);
                    }
                }
                return text.toString();
            }
        case 4:
            if (plugin.getDependencies().getWorldGuardApi() != null) {
                if (context.getSessionData(Key.C_WHILE_WITHIN_REGION) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> whileWithinRegion
                            = (List<String>) context.getSessionData(Key.C_WHILE_WITHIN_REGION);
                    if (whileWithinRegion != null) {
                        for (final String s: whileWithinRegion) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.BLUE).append(s);
                        }
                    }
                    return text.toString();
                }
            } else {
                return ChatColor.GRAY + "(" + BukkitLang.get("notInstalled") + ")";
            }
        case 5:
            return "";
        default:
            return null;
        }
    }

    @Override
    public @NotNull String getBasicPromptText(final @NotNull ConversationContext context) {
        final ConditionsEditorPostOpenNumericPromptEvent event
                = new ConditionsEditorPostOpenNumericPromptEvent(context, this);
        plugin.getServer().getPluginManager().callEvent(event);
        
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
            return new ConditionWorldsPrompt(context);
        case 2:
            return new ConditionTicksListPrompt(context);
        case 3:
            return new ConditionBiomesPrompt(context);
        case 4:
            if (plugin.getDependencies().getWorldGuardApi() != null) {
                return new ConditionRegionsPrompt(context);
            } else {
                return new ConditionWorldPrompt(context);
            }
        case 5:
            try {
                return new ConditionMainPrompt(context);
            } catch (final Exception e) {
                context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("itemCreateCriticalError"));
                return Prompt.END_OF_CONVERSATION;
            }
        default:
            return new ConditionWorldPrompt(context);
        }
    }
    
    public class ConditionWorldsPrompt extends ConditionsEditorStringPrompt {
        
        public ConditionWorldsPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("conditionEditorWorldsTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("conditionEditorWorldsPrompt");
        }
        
        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final ConditionsEditorPostOpenStringPromptEvent event
                    = new ConditionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder worlds = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle(context) + "\n");
            final List<World> worldArr = Bukkit.getWorlds();
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
                final LinkedList<String> worlds = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    if (Bukkit.getWorld(s) != null) {
                        worlds.add(s);
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("conditionEditorInvalidWorld")
                                .replace("<input>", s));
                        return new ConditionWorldsPrompt(context);
                    }
                }
                context.setSessionData(Key.C_WHILE_WITHIN_WORLD, worlds);
            }
            return new ConditionWorldPrompt(context);
        }
    }

    public class ConditionTicksListPrompt extends ConditionsEditorNumericPrompt {

        public ConditionTicksListPrompt(final ConversationContext context) {
            super(context);

        }

        private final int size = 4;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("conditionEditorTicksTitle");
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
                    return ChatColor.YELLOW + BukkitLang.get("conditionEditorSetStartTick");
                case 2:
                    return ChatColor.YELLOW + BukkitLang.get("conditionEditorSetEndTick");
                case 3:
                    return ChatColor.RED + BukkitLang.get("clear");
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
                    if (context.getSessionData(Key.C_WHILE_WITHIN_TICKS_START) == null) {
                        return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                    } else {
                        final int i = (int) Objects.requireNonNull(context.getSessionData(Key.C_WHILE_WITHIN_TICKS_START));
                        return ChatColor.GRAY + "(" + ChatColor.AQUA + i + ChatColor.GRAY + ")";
                    }
                case 2:
                    if (context.getSessionData(Key.C_WHILE_WITHIN_TICKS_END) == null) {
                        return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                    } else {
                        final int i = (int) Objects.requireNonNull(context.getSessionData(Key.C_WHILE_WITHIN_TICKS_END));
                        return ChatColor.GRAY + "(" + ChatColor.AQUA + i + ChatColor.GRAY + ")";
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
            final ConditionsEditorPostOpenNumericPromptEvent event
                    = new ConditionsEditorPostOpenNumericPromptEvent(context, this);
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
                    return new ConditionTickStartPrompt(context);
                case 2:
                    return new ConditionTickEndPrompt(context);
                case 3:
                    context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("conditionEditorConditionCleared"));
                    context.setSessionData(Key.C_WHILE_WITHIN_TICKS_START, null);
                    context.setSessionData(Key.C_WHILE_WITHIN_TICKS_END, null);
                    return new ConditionWorldPrompt(context);
                case 4:
                    if ((context.getSessionData(Key.C_WHILE_WITHIN_TICKS_START) != null
                            && context.getSessionData(Key.C_WHILE_WITHIN_TICKS_END) != null)
                            || (context.getSessionData(Key.C_WHILE_WITHIN_TICKS_START) == null
                            && context.getSessionData(Key.C_WHILE_WITHIN_TICKS_END) == null)) {
                        return new ConditionMainPrompt(context);
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("listsNotSameSize"));
                        return new ConditionTicksListPrompt(context);
                    }
                default:
                    return new ConditionTicksListPrompt(context);
            }
        }
    }

    public class ConditionTickStartPrompt extends ConditionsEditorStringPrompt {

        public ConditionTickStartPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("conditionEditorTicksPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final ConditionsEditorPostOpenStringPromptEvent event
                    = new ConditionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);

            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i < 0 || i > 24000) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("invalidRange")
                                .replace("<least>", "0").replace("<greatest>", "24000"));
                        return new ConditionTickStartPrompt(context);
                    } else {
                        context.setSessionData(Key.C_WHILE_WITHIN_TICKS_START, i);
                    }
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                            .replace("<input>", input));
                    return new ConditionTickStartPrompt(context);
                }
            }
            return new ConditionTicksListPrompt(context);
        }
    }

    public class ConditionTickEndPrompt extends ConditionsEditorStringPrompt {

        public ConditionTickEndPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("conditionEditorTicksPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final ConditionsEditorPostOpenStringPromptEvent event
                    = new ConditionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);

            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i < 0 || i > 24000) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("invalidRange")
                                .replace("<least>", "0").replace("<greatest>", "24000"));
                        return new ConditionTickEndPrompt(context);
                    } else {
                        context.setSessionData(Key.C_WHILE_WITHIN_TICKS_END, i);
                    }
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                            .replace("<input>", input));
                    return new ConditionTickEndPrompt(context);
                }
            }
            return new ConditionTicksListPrompt(context);
        }
    }
    
    public class ConditionBiomesPrompt extends ConditionsEditorStringPrompt {
        
        public ConditionBiomesPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("conditionEditorBiomesTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("conditionEditorBiomesPrompt");
        }
        
        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final ConditionsEditorPostOpenStringPromptEvent event
                    = new ConditionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder biomes = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle(context) + "\n");
            final LinkedList<Biome> biomeArr = new LinkedList<>(Arrays.asList(Biome.values()));
            for (int i = 0; i < biomeArr.size(); i++) {
                biomes.append(ChatColor.AQUA).append(BukkitMiscUtil.snakeCaseToUpperCamelCase(biomeArr.get(i).name()));
                if (i < (biomeArr.size() - 1)) {
                    biomes.append(ChatColor.GRAY).append(", ");
                }
            }
            biomes.append("\n").append(ChatColor.YELLOW).append(getQueryText(context));
            return biomes.toString();
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final LinkedList<String> biomes = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    if (BukkitMiscUtil.getProperBiome(s) != null) {
                        biomes.add(s);
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("conditionEditorInvalidBiome")
                                .replace("<input>", s));
                        return new ConditionBiomesPrompt(context);
                    }
                }
                context.setSessionData(Key.C_WHILE_WITHIN_BIOME, biomes);
            }
            return new ConditionWorldPrompt(context);
        }
    }
    
    public class ConditionRegionsPrompt extends ConditionsEditorStringPrompt {
        
        public ConditionRegionsPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("conditionEditorRegionsTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("conditionEditorRegionsPrompt");
        }
        
        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final ConditionsEditorPostOpenStringPromptEvent event
                    = new ConditionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            StringBuilder regions = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle(context) + "\n");
            boolean any = false;
            final WorldGuardAPI api = plugin.getDependencies().getWorldGuardApi();
            if (api != null) {
                for (final World world : plugin.getServer().getWorlds()) {
                    final RegionManager regionManager = api.getRegionManager(world);
                    if (regionManager != null) {
                        for (final String region : regionManager.getRegions().keySet()) {
                            any = true;
                            regions.append(ChatColor.GREEN).append(region).append(", ");
                        }
                    }
                }
            }
            if (any) {
                regions = new StringBuilder(regions.substring(0, regions.length() - 2) + "\n");
            } else {
                regions.append(ChatColor.GRAY).append("(").append(BukkitLang.get("none")).append(")\n");
            }
            return regions.toString() + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final LinkedList<String> regions = new LinkedList<>();
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
                        String error = BukkitLang.get("questWGInvalidRegion");
                        error = error.replace("<region>", ChatColor.RED + r + ChatColor.YELLOW);
                        context.getForWhom().sendRawMessage(ChatColor.YELLOW + error);
                        return new ConditionRegionsPrompt(context);
                    }
                }
                context.setSessionData(Key.C_WHILE_WITHIN_REGION, regions);
            }
            return new ConditionWorldPrompt(context);
        }
    }
}
