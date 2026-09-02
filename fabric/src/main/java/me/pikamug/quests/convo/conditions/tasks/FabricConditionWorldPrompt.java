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

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.convo.conditions.FabricConditionsEditorIntegerPrompt;
import me.pikamug.quests.convo.conditions.FabricConditionsEditorStringPrompt;
import me.pikamug.quests.convo.conditions.main.FabricConditionMainPrompt;
import me.pikamug.quests.util.FabricLang;
import me.pikamug.quests.util.FabricMiscUtil;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.SessionData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class FabricConditionWorldPrompt extends FabricConditionsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final FabricQuestsPlugin plugin;

    public FabricConditionWorldPrompt(final @NotNull UUID uuid) {
        super(uuid);
        this.uuid = uuid;
        this.plugin = FabricQuestsPlugin.getInstance();
    }

    private final int size = 5;

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public String getTitle() {
        return FabricLang.get("conditionEditorWorld");
    }

    @Override
    public ChatFormatting getNumberColor(final int number) {
        switch (number) {
            case 1:
            case 2:
            case 3:
            case 4:
                return ChatFormatting.BLUE;
            case 5:
                return ChatFormatting.GREEN;
            default:
                return null;
        }
    }

    @Override
    public String getSelectionText(final int number) {
        switch(number) {
        case 1:
            return ChatFormatting.YELLOW + FabricLang.get("conditionEditorStayWithinWorld");
        case 2:
            return ChatFormatting.YELLOW + FabricLang.get("conditionEditorStayWithinTicks");
        case 3:
            return ChatFormatting.YELLOW + FabricLang.get("conditionEditorStayWithinBiome");
        case 4:
            return ChatFormatting.GRAY + FabricLang.get("conditionEditorStayWithinRegion");
        case 5:
            return ChatFormatting.GREEN + FabricLang.get("done");
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
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final List<String> whileWithinWorld = (List<String>) SessionData.get(uuid, Key.C_WHILE_WITHIN_WORLD);
                if (whileWithinWorld != null) {
                    for (final String s: whileWithinWorld) {
                        text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.BLUE).append(s);
                    }
                }
                return text.toString();
            }
        case 2:
            if (SessionData.get(uuid, Key.C_WHILE_WITHIN_TICKS_START) == null
                    || SessionData.get(uuid, Key.C_WHILE_WITHIN_TICKS_END) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + SessionData.get(uuid, Key.C_WHILE_WITHIN_TICKS_START)
                        + " - " + SessionData.get(uuid, Key.C_WHILE_WITHIN_TICKS_END)+ ChatFormatting.GRAY + ")";
            }
        case 3:
            if (SessionData.get(uuid, Key.C_WHILE_WITHIN_BIOME) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final List<String> whileWithinBiome = (List<String>) SessionData.get(uuid, Key.C_WHILE_WITHIN_BIOME);
                if (whileWithinBiome != null) {
                    for (final String s: whileWithinBiome) {
                        text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.BLUE).append(s);
                    }
                }
                return text.toString();
            }
        case 4:
            return ChatFormatting.GRAY + "(" + FabricLang.get("notInstalled") + ")";
        case 5:
            return "";
        default:
            return null;
        }
    }

    @Override
    public @NotNull String getPromptText() {
        final StringBuilder text = new StringBuilder(ChatFormatting.AQUA + "- " + getTitle() + " -");
        for (int i = 1; i <= size; i++) {
            text.append("\n").append(getNumberColor(i)).append(ChatFormatting.BOLD).append(i)
                    .append(ChatFormatting.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                    .append(getAdditionalText(i));
        }
        return sendClickableSelection(text.toString(), plugin.getQuester(uuid));
    }

    @Override
    public void acceptInput(final Number input) {
        final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
        if (sender == null) {
            return;
        }
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
            new FabricConditionWorldPrompt(uuid).start();
            break;
        case 5:
            try {
                new FabricConditionMainPrompt(uuid).start();
            } catch (final Exception e) {
                sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("itemCreateCriticalError")));
                return;
            }
            break;
        default:
            new FabricConditionWorldPrompt(uuid).start();
            break;
        }
    }

    public class ConditionWorldsPrompt extends FabricConditionsEditorStringPrompt {

        public ConditionWorldsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return FabricLang.get("conditionEditorWorldsTitle");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("conditionEditorWorldsPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final StringBuilder worlds = new StringBuilder(ChatFormatting.LIGHT_PURPLE + getTitle() + "\n");
            final java.util.List<ServerLevel> worldArr = new java.util.ArrayList<>();
            for (final ServerLevel level : plugin.getServer().getAllLevels()) {
                worldArr.add(level);
            }
            for (int i = 0; i < worldArr.size(); i++) {
                worlds.append(ChatFormatting.AQUA).append(worldArr.get(i).dimension().identifier().toString());
                if (i < (worldArr.size() - 1)) {
                    worlds.append(ChatFormatting.GRAY).append(", ");
                }
            }
            worlds.append("\n").append(ChatFormatting.YELLOW).append(getQueryText());
            return worlds.toString();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                final LinkedList<String> worlds = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    boolean found = false;
                    for (final ServerLevel level : plugin.getServer().getAllLevels()) {
                        if (level.dimension().identifier().toString().equals(s)) {
                            worlds.add(s);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("conditionEditorInvalidWorld")
                                .replace("<input>", s)));
                        new ConditionWorldsPrompt(uuid).start();
                        break;
                    }
                }
                SessionData.set(uuid, Key.C_WHILE_WITHIN_WORLD, worlds);
            }
            new FabricConditionWorldPrompt(uuid).start();
        }
    }

    public class ConditionTicksListPrompt extends FabricConditionsEditorIntegerPrompt {

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
            return FabricLang.get("conditionEditorTicksTitle");
        }
        @Override
        public ChatFormatting getNumberColor(final int number) {
            switch (number) {
                case 1:
                case 2:
                    return ChatFormatting.BLUE;
                case 3:
                    return ChatFormatting.RED;
                case 4:
                    return ChatFormatting.GREEN;
                default:
                    return null;
            }
        }

        @Override
        public String getSelectionText(final int number) {
            switch (number) {
                case 1:
                    return ChatFormatting.YELLOW + FabricLang.get("conditionEditorSetStartTick");
                case 2:
                    return ChatFormatting.YELLOW + FabricLang.get("conditionEditorSetEndTick");
                case 3:
                    return ChatFormatting.RED + FabricLang.get("clear");
                case 4:
                    return ChatFormatting.GREEN + FabricLang.get("done");
                default:
                    return null;
            }
        }

        @Override
        public String getAdditionalText(final int number) {
            switch (number) {
                case 1:
                    if (SessionData.get(uuid, Key.C_WHILE_WITHIN_TICKS_START) == null) {
                        return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                    } else {
                        final int i = (int) Objects.requireNonNull(SessionData.get(uuid, Key.C_WHILE_WITHIN_TICKS_START));
                        return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + i + ChatFormatting.GRAY + ")";
                    }
                case 2:
                    if (SessionData.get(uuid, Key.C_WHILE_WITHIN_TICKS_END) == null) {
                        return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                    } else {
                        final int i = (int) Objects.requireNonNull(SessionData.get(uuid, Key.C_WHILE_WITHIN_TICKS_END));
                        return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + i + ChatFormatting.GRAY + ")";
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
            final StringBuilder text = new StringBuilder(ChatFormatting.GOLD + getTitle());
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatFormatting.BOLD).append(i)
                        .append(ChatFormatting.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                        .append(getAdditionalText(i));
            }
            return sendClickableSelection(text.toString(), plugin.getQuester(uuid));
        }

        @Override
        public void acceptInput(final Number input) {
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            switch (input.intValue()) {
                case 1:
                    new ConditionTickStartPrompt(uuid).start();
                    break;
                case 2:
                    new ConditionTickEndPrompt(uuid).start();
                    break;
                case 3:
                    sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("conditionEditorConditionCleared")));
                    SessionData.set(uuid, Key.C_WHILE_WITHIN_TICKS_START, null);
                    SessionData.set(uuid, Key.C_WHILE_WITHIN_TICKS_END, null);
                    new FabricConditionWorldPrompt(uuid).start();
                    break;
                case 4:
                    if ((SessionData.get(uuid, Key.C_WHILE_WITHIN_TICKS_START) != null
                            && SessionData.get(uuid, Key.C_WHILE_WITHIN_TICKS_END) != null)
                            || (SessionData.get(uuid, Key.C_WHILE_WITHIN_TICKS_START) == null
                            && SessionData.get(uuid, Key.C_WHILE_WITHIN_TICKS_END) == null)) {
                        new FabricConditionMainPrompt(uuid).start();
                    } else {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("listsNotSameSize")));
                        new ConditionTicksListPrompt(uuid).start();
                    }
                    break;
                default:
                    new ConditionTicksListPrompt(uuid).start();
                    break;
            }
        }
    }

    public class ConditionTickStartPrompt extends FabricConditionsEditorStringPrompt {

        public ConditionTickStartPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("conditionEditorTicksPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i < 0 || i > 24000) {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("invalidRange")
                                .replace("<least>", "0").replace("<greatest>", "24000")));
                        new ConditionTickStartPrompt(uuid).start();
                    } else {
                        SessionData.set(uuid, Key.C_WHILE_WITHIN_TICKS_START, i);
                    }
                } catch (final NumberFormatException e) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("reqNotANumber")
                            .replace("<input>", input)));
                    new ConditionTickStartPrompt(uuid).start();
                }
            }
            new ConditionTicksListPrompt(uuid).start();
        }
    }

    public class ConditionTickEndPrompt extends FabricConditionsEditorStringPrompt {

        public ConditionTickEndPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("conditionEditorTicksPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i < 0 || i > 24000) {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("invalidRange")
                                .replace("<least>", "0").replace("<greatest>", "24000")));
                        new ConditionTickEndPrompt(uuid).start();
                    } else {
                        SessionData.set(uuid, Key.C_WHILE_WITHIN_TICKS_END, i);
                    }
                } catch (final NumberFormatException e) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("reqNotANumber")
                            .replace("<input>", input)));
                    new ConditionTickEndPrompt(uuid).start();
                }
            }
            new ConditionTicksListPrompt(uuid).start();
        }
    }

    public class ConditionBiomesPrompt extends FabricConditionsEditorStringPrompt {

        public ConditionBiomesPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return FabricLang.get("conditionEditorBiomesTitle");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("conditionEditorBiomesPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final StringBuilder biomes = new StringBuilder(ChatFormatting.LIGHT_PURPLE + getTitle() + "\n");
            final Registry<Biome> biomeRegistry = plugin.getServer().registryAccess().lookupOrThrow(Registries.BIOME);
            final LinkedList<Biome> biomeArr = new LinkedList<>();
            for (final Biome biome : biomeRegistry) {
                biomeArr.add(biome);
            }
            for (int i = 0; i < biomeArr.size(); i++) {
                final Identifier loc = biomeRegistry.getKey(biomeArr.get(i));
                biomes.append(ChatFormatting.AQUA).append(loc != null ? loc.toString() : biomeArr.get(i).toString());
                if (i < (biomeArr.size() - 1)) {
                    biomes.append(ChatFormatting.GRAY).append(", ");
                }
            }
            biomes.append("\n").append(ChatFormatting.YELLOW).append(getQueryText());
            return biomes.toString();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                final LinkedList<String> biomes = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    // Validate biome exists in registry
                    final Identifier loc = Identifier.tryParse(s);
                    if (loc != null && plugin.getServer().registryAccess().lookupOrThrow(Registries.BIOME).get(loc).isPresent()) {
                        biomes.add(s);
                    } else {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("conditionEditorInvalidBiome")
                                .replace("<input>", s)));
                        new ConditionBiomesPrompt(uuid).start();
                    }
                }
                SessionData.set(uuid, Key.C_WHILE_WITHIN_BIOME, biomes);
            }
            new FabricConditionWorldPrompt(uuid).start();
        }
    }
}
