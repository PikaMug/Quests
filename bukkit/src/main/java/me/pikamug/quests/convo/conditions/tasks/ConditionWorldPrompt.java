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
import me.pikamug.quests.convo.conditions.ConditionsEditorIntegerPrompt;
import me.pikamug.quests.convo.conditions.ConditionsEditorStringPrompt;
import me.pikamug.quests.convo.conditions.main.ConditionMainPrompt;
import me.pikamug.quests.dependencies.reflect.worldguard.WorldGuardAPI;
import me.pikamug.quests.events.editor.conditions.BukkitConditionsEditorPostOpenNumericPromptEvent;
import me.pikamug.quests.events.editor.conditions.BukkitConditionsEditorPostOpenStringPromptEvent;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.quests.util.BukkitMiscUtil;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.SessionData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ConditionWorldPrompt extends ConditionsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final BukkitQuestsPlugin plugin;
    
    public ConditionWorldPrompt(final @NotNull UUID uuid) {
        super(uuid);
        this.uuid = uuid;
        this.plugin = BukkitQuestsPlugin.getInstance();
    }
    
    private final int size = 5;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle() {
        return BukkitLang.get("conditionEditorWorld");
    }
    
    @Override
    public ChatColor getNumberColor(final int number) {
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
    public String getSelectionText(final int number) {
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
    public String getAdditionalText(final int number) {
        switch(number) {
        case 1:
            if (SessionData.get(uuid, Key.C_WHILE_WITHIN_WORLD) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final List<String> whileWithinWorld = (List<String>) SessionData.get(uuid, Key.C_WHILE_WITHIN_WORLD);
                if (whileWithinWorld != null) {
                    for (final String s: whileWithinWorld) {
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.BLUE).append(s);
                    }
                }
                return text.toString();
            }
        case 2:
            if (SessionData.get(uuid, Key.C_WHILE_WITHIN_TICKS_START) == null
                    || SessionData.get(uuid, Key.C_WHILE_WITHIN_TICKS_END) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + SessionData.get(uuid, Key.C_WHILE_WITHIN_TICKS_START)
                        + " - " + SessionData.get(uuid, Key.C_WHILE_WITHIN_TICKS_END)+ ChatColor.GRAY + ")";
            }
        case 3:
            if (SessionData.get(uuid, Key.C_WHILE_WITHIN_BIOME) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final List<String> whileWithinBiome = (List<String>) SessionData.get(uuid, Key.C_WHILE_WITHIN_BIOME);
                if (whileWithinBiome != null) {
                    for (final String s: whileWithinBiome) {
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.BLUE).append(s);
                    }
                }
                return text.toString();
            }
        case 4:
            if (plugin.getDependencies().getWorldGuardApi() != null) {
                if (SessionData.get(uuid, Key.C_WHILE_WITHIN_REGION) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> whileWithinRegion
                            = (List<String>) SessionData.get(uuid, Key.C_WHILE_WITHIN_REGION);
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
    public @NotNull String getPromptText() {
        final BukkitConditionsEditorPostOpenNumericPromptEvent event
                = new BukkitConditionsEditorPostOpenNumericPromptEvent(uuid, this);
        plugin.getServer().getPluginManager().callEvent(event);
        
        final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle() + " -");
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
        switch(input.intValue()) {
        case 1:
            new ConditionWorldsPrompt(uuid).start();
            break;
        case 2:
            new ConditionTicksListPrompt(uuid).start();
            break;
        case 3:
            new ConditionBiomesPrompt(uuid).start();
            break;
        case 4:
            if (plugin.getDependencies().getWorldGuardApi() != null) {
                new ConditionRegionsPrompt(uuid).start();
            } else {
                new ConditionWorldPrompt(uuid).start();
            }
            break;
        case 5:
            try {
                new ConditionMainPrompt(uuid).start();
            } catch (final Exception e) {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("itemCreateCriticalError"));
                return;
            }
            break;
        default:
            new ConditionWorldPrompt(uuid).start();
            break;
        }
    }
    
    public class ConditionWorldsPrompt extends ConditionsEditorStringPrompt {
        
        public ConditionWorldsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return BukkitLang.get("conditionEditorWorldsTitle");
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("conditionEditorWorldsPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitConditionsEditorPostOpenStringPromptEvent event
                    = new BukkitConditionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder worlds = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle() + "\n");
            final List<World> worldArr = Bukkit.getWorlds();
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
                final LinkedList<String> worlds = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    if (Bukkit.getWorld(s) != null) {
                        worlds.add(s);
                    } else {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("conditionEditorInvalidWorld")
                                .replace("<input>", s));
                        new ConditionWorldsPrompt(uuid).start();
                        break;
                    }
                }
                SessionData.set(uuid, Key.C_WHILE_WITHIN_WORLD, worlds);
            }
            new ConditionWorldPrompt(uuid).start();
        }
    }

    public class ConditionTicksListPrompt extends ConditionsEditorIntegerPrompt {

        public ConditionTicksListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 4;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return BukkitLang.get("conditionEditorTicksTitle");
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
        public String getAdditionalText(final int number) {
            switch (number) {
                case 1:
                    if (SessionData.get(uuid, Key.C_WHILE_WITHIN_TICKS_START) == null) {
                        return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                    } else {
                        final int i = (int) Objects.requireNonNull(SessionData.get(uuid, Key.C_WHILE_WITHIN_TICKS_START));
                        return ChatColor.GRAY + "(" + ChatColor.AQUA + i + ChatColor.GRAY + ")";
                    }
                case 2:
                    if (SessionData.get(uuid, Key.C_WHILE_WITHIN_TICKS_END) == null) {
                        return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                    } else {
                        final int i = (int) Objects.requireNonNull(SessionData.get(uuid, Key.C_WHILE_WITHIN_TICKS_END));
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
        public @NotNull String getPromptText() {
            final BukkitConditionsEditorPostOpenNumericPromptEvent event
                    = new BukkitConditionsEditorPostOpenNumericPromptEvent(uuid, this);
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
                    new ConditionTickStartPrompt(uuid).start();
                    break;
                case 2:
                    new ConditionTickEndPrompt(uuid).start();
                    break;
                case 3:
                    sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("conditionEditorConditionCleared"));
                    SessionData.set(uuid, Key.C_WHILE_WITHIN_TICKS_START, null);
                    SessionData.set(uuid, Key.C_WHILE_WITHIN_TICKS_END, null);
                    new ConditionWorldPrompt(uuid).start();
                    break;
                case 4:
                    if ((SessionData.get(uuid, Key.C_WHILE_WITHIN_TICKS_START) != null
                            && SessionData.get(uuid, Key.C_WHILE_WITHIN_TICKS_END) != null)
                            || (SessionData.get(uuid, Key.C_WHILE_WITHIN_TICKS_START) == null
                            && SessionData.get(uuid, Key.C_WHILE_WITHIN_TICKS_END) == null)) {
                        new ConditionMainPrompt(uuid).start();
                    } else {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("listsNotSameSize"));
                        new ConditionTicksListPrompt(uuid).start();
                    }
                    break;
                default:
                    new ConditionTicksListPrompt(uuid).start();
                    break;
            }
        }
    }

    public class ConditionTickStartPrompt extends ConditionsEditorStringPrompt {

        public ConditionTickStartPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("conditionEditorTicksPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitConditionsEditorPostOpenStringPromptEvent event
                    = new BukkitConditionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i < 0 || i > 24000) {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("invalidRange")
                                .replace("<least>", "0").replace("<greatest>", "24000"));
                        new ConditionTickStartPrompt(uuid).start();
                    } else {
                        SessionData.set(uuid, Key.C_WHILE_WITHIN_TICKS_START, i);
                    }
                } catch (final NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                            .replace("<input>", input));
                    new ConditionTickStartPrompt(uuid).start();
                }
            }
            new ConditionTicksListPrompt(uuid).start();
        }
    }

    public class ConditionTickEndPrompt extends ConditionsEditorStringPrompt {

        public ConditionTickEndPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("conditionEditorTicksPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitConditionsEditorPostOpenStringPromptEvent event
                    = new BukkitConditionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i < 0 || i > 24000) {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("invalidRange")
                                .replace("<least>", "0").replace("<greatest>", "24000"));
                        new ConditionTickEndPrompt(uuid).start();
                    } else {
                        SessionData.set(uuid, Key.C_WHILE_WITHIN_TICKS_END, i);
                    }
                } catch (final NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                            .replace("<input>", input));
                    new ConditionTickEndPrompt(uuid).start();
                }
            }
            new ConditionTicksListPrompt(uuid).start();
        }
    }
    
    public class ConditionBiomesPrompt extends ConditionsEditorStringPrompt {
        
        public ConditionBiomesPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return BukkitLang.get("conditionEditorBiomesTitle");
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("conditionEditorBiomesPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitConditionsEditorPostOpenStringPromptEvent event
                    = new BukkitConditionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder biomes = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle() + "\n");
            final LinkedList<Biome> biomeArr = new LinkedList<>(Arrays.asList(Biome.values()));
            for (int i = 0; i < biomeArr.size(); i++) {
                biomes.append(ChatColor.AQUA).append(BukkitMiscUtil.snakeCaseToUpperCamelCase(biomeArr.get(i).name()));
                if (i < (biomeArr.size() - 1)) {
                    biomes.append(ChatColor.GRAY).append(", ");
                }
            }
            biomes.append("\n").append(ChatColor.YELLOW).append(getQueryText());
            return biomes.toString();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final LinkedList<String> biomes = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    if (BukkitMiscUtil.getProperBiome(s) != null) {
                        biomes.add(s);
                    } else {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("conditionEditorInvalidBiome")
                                .replace("<input>", s));
                        new ConditionBiomesPrompt(uuid).start();
                    }
                }
                SessionData.set(uuid, Key.C_WHILE_WITHIN_BIOME, biomes);
            }
            new ConditionWorldPrompt(uuid).start();
        }
    }
    
    public class ConditionRegionsPrompt extends ConditionsEditorStringPrompt {
        
        public ConditionRegionsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return BukkitLang.get("conditionEditorRegionsTitle");
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("conditionEditorRegionsPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitConditionsEditorPostOpenStringPromptEvent event
                    = new BukkitConditionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            StringBuilder regions = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle() + "\n");
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
            return regions.toString() + ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
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
                        sender.sendMessage(ChatColor.YELLOW + error);
                        new ConditionRegionsPrompt(uuid).start();
                    }
                }
                SessionData.set(uuid, Key.C_WHILE_WITHIN_REGION, regions);
            }
            new ConditionWorldPrompt(uuid).start();
        }
    }
}
