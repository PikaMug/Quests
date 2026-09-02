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

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.convo.actions.FabricActionsEditorIntegerPrompt;
import me.pikamug.quests.convo.actions.FabricActionsEditorStringPrompt;
import me.pikamug.quests.convo.actions.main.FabricActionMainPrompt;
import me.pikamug.quests.util.FabricLang;
import me.pikamug.quests.util.FabricMiscUtil;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.SessionData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FabricActionWeatherPrompt extends FabricActionsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final FabricQuestsPlugin plugin;

    public static final ConcurrentHashMap<UUID, BlockPos> selectedLightningLocations = new ConcurrentHashMap<>();

    public FabricActionWeatherPrompt(final @NotNull UUID uuid) {
        super(uuid);
        this.uuid = uuid;
        this.plugin = FabricQuestsPlugin.getInstance();
    }

    private final int size = 4;

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public String getTitle() {
        return FabricLang.get("eventEditorWeather");
    }

    @Override
    public ChatFormatting getNumberColor(final int number) {
        switch (number) {
        case 1:
        case 2:
        case 3:
            return ChatFormatting.BLUE;
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
            return ChatFormatting.YELLOW + FabricLang.get("eventEditorSetStorm");
        case 2:
            return ChatFormatting.YELLOW + FabricLang.get("eventEditorSetThunder");
        case 3:
            return ChatFormatting.YELLOW + FabricLang.get("eventEditorSetLightning");
        case 4:
            return ChatFormatting.GREEN + FabricLang.get("done");
        default:
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getAdditionalText(final int number) {
        switch (number) {
        case 1:
            if (SessionData.get(uuid, Key.A_WORLD_STORM) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final Integer duration = (Integer) SessionData.get(uuid, Key.A_WORLD_STORM_DURATION);
                if (duration != null) {
                    return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + SessionData.get(uuid, Key.A_WORLD_STORM)
                            + ChatFormatting.GRAY + " -> " + ChatFormatting.DARK_AQUA + FabricMiscUtil.formatTime(duration * 1000L)
                            + ChatFormatting.GRAY + ")";
                }
                break;
            }
        case 2:
            if (SessionData.get(uuid, Key.A_WORLD_THUNDER) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final Integer duration = (Integer) SessionData.get(uuid, Key.A_WORLD_THUNDER_DURATION);
                if (duration != null) {
                    return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + SessionData.get(uuid, Key.A_WORLD_THUNDER)
                            + ChatFormatting.GRAY + " -> " + ChatFormatting.DARK_AQUA + FabricMiscUtil.formatTime(duration * 1000L)
                            + ChatFormatting.GRAY + ")";
                }
                break;
            }
        case 3:
            if (SessionData.get(uuid, Key.A_LIGHTNING) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> locations = (LinkedList<String>) SessionData.get(uuid, Key.A_LIGHTNING);
                if (locations != null) {
                    for (final String loc : locations) {
                        text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA).append(loc);
                    }
                }
                return text.toString();
            }
        case 4:
            return "";
        default:
            return null;
        }
        return "";
    }

    @Override
    public @NotNull String getPromptText() {
        final StringBuilder text = new StringBuilder(ChatFormatting.GOLD + "- " + getTitle() + " -");
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
            new ActionStormPrompt(uuid).start();
            break;
        case 2:
            new ActionThunderPrompt(uuid).start();
            break;
        case 3:
            final MinecraftServer server = plugin.getServer();
            if (server != null && sender instanceof ServerPlayer) {
                final ServerLevel overworld = server.overworld();
                selectedLightningLocations.put(uuid, BlockPos.ZERO);
                new ActionLightningPrompt(uuid).start();
            } else {
                sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("consoleError")));
                new FabricActionWeatherPrompt(uuid).start();
            }
            break;
        case 4:
            new FabricActionMainPrompt(uuid).start();
            break;
        default:
            new FabricActionWeatherPrompt(uuid).start();
            break;
        }
    }

    public class ActionStormPrompt extends FabricActionsEditorIntegerPrompt {

        public ActionStormPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 4;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return FabricLang.get("eventEditorStormTitle");
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
                return ChatFormatting.YELLOW + FabricLang.get("eventEditorSetWorld");
            case 2:
                return ChatFormatting.YELLOW + FabricLang.get("eventEditorSetDuration");
            case 3:
                return ChatFormatting.YELLOW + FabricLang.get("clear");
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
                if (SessionData.get(uuid, Key.A_WORLD_STORM) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + SessionData.get(uuid, Key.A_WORLD_STORM)
                            + ChatFormatting.GRAY + ")";
                }
            case 2:
                if (SessionData.get(uuid, Key.A_WORLD_STORM_DURATION) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final Integer duration = (Integer) SessionData.get(uuid, Key.A_WORLD_STORM_DURATION);
                    if (duration != null) {
                        return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + FabricMiscUtil.formatTime(duration * 1000L)
                                + ChatFormatting.GRAY + ")";
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
                new ActionStormWorldPrompt(uuid).start();
                break;
            case 2:
                if (SessionData.get(uuid, Key.A_WORLD_STORM) == null) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("eventEditorSetWorldFirst")));
                    new ActionStormPrompt(uuid).start();
                } else {
                    new ActionStormDurationPrompt(uuid).start();
                }
                break;
            case 3:
                if (SessionData.get(uuid, Key.A_WORLD_STORM) != null
                        && SessionData.get(uuid, Key.A_WORLD_STORM_DURATION) == null) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("eventEditorMustSetStormDuration")));
                    new ActionStormPrompt(uuid).start();
                } else {
                    new FabricActionMainPrompt(uuid).start();
                }
                break;
            case 4:
                new FabricActionMainPrompt(uuid).start();
                break;
            default:
                new ActionStormPrompt(uuid).start();
                break;
            }
        }
    }

    public class ActionStormWorldPrompt extends FabricActionsEditorStringPrompt {

        public ActionStormWorldPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return FabricLang.get("eventEditorWorldsTitle");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("eventEditorEnterStormWorld");
        }

        @Override
        public @NotNull String getPromptText() {
            final StringBuilder worlds = new StringBuilder(ChatFormatting.LIGHT_PURPLE + getTitle() + "\n");
            final MinecraftServer server = plugin.getServer();
            if (server != null) {
                int total = 0;
                for (final ServerLevel level : server.getAllLevels()) {
                    total++;
                }
                int idx = 0;
                for (final ServerLevel level : server.getAllLevels()) {
                    final String name = level.dimension().identifier().toString();
                    worlds.append(ChatFormatting.AQUA).append(name);
                    if (idx < (total - 1)) {
                        worlds.append(ChatFormatting.GRAY).append(", ");
                    }
                    idx++;
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
                final MinecraftServer server = plugin.getServer();
                if (server != null) {
                    for (final ServerLevel level : server.getAllLevels()) {
                        if (level.dimension().identifier().toString().equalsIgnoreCase(input)
                                || level.dimension().identifier().getPath().equalsIgnoreCase(input)) {
                            SessionData.set(uuid, Key.A_WORLD_STORM, level.dimension().identifier().toString());
                            new ActionStormPrompt(uuid).start();
                            return;
                        }
                    }
                }
                sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("eventEditorInvalidWorld")
                        .replace("<input>", input)));
                new ActionStormWorldPrompt(uuid).start();
                return;
            }
            new ActionStormPrompt(uuid).start();
        }
    }

    public class ActionStormDurationPrompt extends FabricActionsEditorStringPrompt {

        public ActionStormDurationPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("eventEditorEnterDuration");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            try {
                final int i = Integer.parseInt(input);
                if (i < 1) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("invalidMinimum").replace("<number>", "1")));
                    new ActionStormDurationPrompt(uuid).start();
                    return;
                } else {
                    SessionData.set(uuid, Key.A_WORLD_STORM_DURATION, i);
                }
            } catch (final NumberFormatException e) {
                sender.sendSystemMessage(Component.literal(ChatFormatting.RED
                        + FabricLang.get("reqNotANumber").replace("<input>", input)));
            }
            new ActionStormPrompt(uuid).start();
        }
    }

    public class ActionThunderPrompt extends FabricActionsEditorIntegerPrompt {

        public ActionThunderPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 4;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return FabricLang.get("eventEditorThunderTitle");
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
                return ChatFormatting.YELLOW + FabricLang.get("eventEditorSetWorld");
            case 2:
                return ChatFormatting.YELLOW + FabricLang.get("eventEditorSetDuration");
            case 3:
                return ChatFormatting.YELLOW + FabricLang.get("clear");
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
                if (SessionData.get(uuid, Key.A_WORLD_THUNDER) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + SessionData.get(uuid, Key.A_WORLD_THUNDER)
                            + ChatFormatting.GRAY + ")";
                }
            case 2:
                if (SessionData.get(uuid, Key.A_WORLD_THUNDER_DURATION) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final Integer duration = (Integer) SessionData.get(uuid, Key.A_WORLD_THUNDER_DURATION);
                    if (duration != null) {
                        return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + FabricMiscUtil.formatTime(duration * 1000L)
                                + ChatFormatting.GRAY + ")";
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
                new ActionThunderWorldPrompt(uuid).start();
                break;
            case 2:
                if (SessionData.get(uuid, Key.A_WORLD_THUNDER) == null) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("eventEditorSetWorldFirst")));
                    new ActionThunderPrompt(uuid).start();
                } else {
                    new ActionThunderDurationPrompt(uuid).start();
                }
                break;
            case 3:
                sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("eventEditorThunderCleared")));
                SessionData.set(uuid, Key.A_WORLD_THUNDER, null);
                SessionData.set(uuid, Key.A_WORLD_THUNDER_DURATION, null);
                new ActionThunderPrompt(uuid).start();
                break;
            case 4:
                if (SessionData.get(uuid, Key.A_WORLD_THUNDER) != null
                        && SessionData.get(uuid, Key.A_WORLD_THUNDER_DURATION) == null) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("eventEditorMustSetThunderDuration")));
                    new ActionThunderPrompt(uuid).start();
                } else {
                    new FabricActionMainPrompt(uuid).start();
                }
                break;
            default:
                new ActionThunderPrompt(uuid).start();
                break;
            }
        }
    }

    public class ActionThunderWorldPrompt extends FabricActionsEditorStringPrompt {

        public ActionThunderWorldPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return FabricLang.get("eventEditorWorldsTitle");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("eventEditorEnterThunderWorld");
        }

        @Override
        public @NotNull String getPromptText() {
            final StringBuilder worlds = new StringBuilder(ChatFormatting.LIGHT_PURPLE + getTitle() + "\n");
            final MinecraftServer server = plugin.getServer();
            if (server != null) {
                int total = 0;
                for (final ServerLevel level : server.getAllLevels()) {
                    total++;
                }
                int idx = 0;
                for (final ServerLevel level : server.getAllLevels()) {
                    final String name = level.dimension().identifier().toString();
                    worlds.append(ChatFormatting.AQUA).append(name);
                    if (idx < (total - 1)) {
                        worlds.append(ChatFormatting.GRAY).append(", ");
                    }
                    idx++;
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
                final MinecraftServer server = plugin.getServer();
                if (server != null) {
                    for (final ServerLevel level : server.getAllLevels()) {
                        if (level.dimension().identifier().toString().equalsIgnoreCase(input)
                                || level.dimension().identifier().getPath().equalsIgnoreCase(input)) {
                            SessionData.set(uuid, Key.A_WORLD_THUNDER, level.dimension().identifier().toString());
                            new ActionThunderPrompt(uuid).start();
                            return;
                        }
                    }
                }
                sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("eventEditorInvalidWorld")
                        .replace("<input>", input)));
                new ActionThunderWorldPrompt(uuid).start();
                return;
            }
            new ActionThunderPrompt(uuid).start();
        }
    }

    public class ActionThunderDurationPrompt extends FabricActionsEditorStringPrompt {

        public ActionThunderDurationPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("eventEditorEnterDuration");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            try {
                final int i = Integer.parseInt(input);
                if (i < 1) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED
                            + FabricLang.get("invalidMinimum").replace("<number>", "1")));
                    new ActionThunderDurationPrompt(uuid).start();
                    return;
                } else {
                    SessionData.set(uuid, Key.A_WORLD_THUNDER_DURATION, i);
                }
            } catch (final NumberFormatException e) {
                sender.sendSystemMessage(Component.literal(ChatFormatting.RED
                        + FabricLang.get("reqNotANumber").replace("<input>", input)));
            }
            new ActionThunderPrompt(uuid).start();
        }
    }

    public class ActionLightningPrompt extends FabricActionsEditorStringPrompt {

        public ActionLightningPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("eventEditorLightningPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            if (input.equalsIgnoreCase(FabricLang.get("cmdAdd"))) {
                final BlockPos block = selectedLightningLocations.get(uuid);
                if (block != null) {
                    final String locStr = block.getX() + " " + block.getY() + " " + block.getZ();
                    final LinkedList<String> locations;
                    if (SessionData.get(uuid, Key.A_LIGHTNING) != null) {
                        locations = (LinkedList<String>) SessionData.get(uuid, Key.A_LIGHTNING);
                    } else {
                        locations = new LinkedList<>();
                    }
                    if (locations != null) {
                        locations.add(locStr);
                    }
                    SessionData.set(uuid, Key.A_LIGHTNING, locations);
                    selectedLightningLocations.remove(uuid);
                } else {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("eventEditorSelectBlockFirst")));
                    new ActionLightningPrompt(uuid).start();
                    return;
                }
                new FabricActionMainPrompt(uuid).start();
            } else if (input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.A_LIGHTNING, null);
                selectedLightningLocations.remove(uuid);
                new FabricActionMainPrompt(uuid).start();
            } else if (input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                selectedLightningLocations.remove(uuid);
                new FabricActionMainPrompt(uuid).start();
            } else {
                new ActionLightningPrompt(uuid).start();
            }
        }
    }
}
