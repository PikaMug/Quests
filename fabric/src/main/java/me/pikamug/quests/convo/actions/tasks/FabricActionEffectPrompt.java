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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FabricActionEffectPrompt extends FabricActionsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final FabricQuestsPlugin plugin;

    public static final ConcurrentHashMap<UUID, BlockPos> selectedEffectLocations = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<UUID, BlockPos> selectedExplosionLocations = new ConcurrentHashMap<>();

    public FabricActionEffectPrompt(final @NotNull UUID uuid) {
        super(uuid);
        this.uuid = uuid;
        this.plugin = FabricQuestsPlugin.getInstance();
    }

    private final int size = 3;

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public String getTitle() {
        return FabricLang.get("eventEditorEffect");
    }

    @Override
    public ChatFormatting getNumberColor(final int number) {
        switch (number) {
        case 1:
        case 2:
            return ChatFormatting.BLUE;
        case 3:
            return ChatFormatting.GREEN;
        default:
            return null;
        }
    }

    @Override
    public String getSelectionText(final int number) {
        switch (number) {
        case 1:
            return ChatFormatting.YELLOW + FabricLang.get("eventEditorSetEffects");
        case 2:
            return ChatFormatting.YELLOW + FabricLang.get("eventEditorSetExplosions");
        case 3:
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
            if (SessionData.get(uuid, Key.A_EFFECTS) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> effects = (LinkedList<String>) SessionData.get(uuid, Key.A_EFFECTS);
                final LinkedList<String> locations
                        = (LinkedList<String>) SessionData.get(uuid, Key.A_EFFECTS_LOCATIONS);
                if (effects != null && locations != null) {
                    for (final String effect : effects) {
                        text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA).append(effect)
                                .append(ChatFormatting.GRAY).append(" at ").append(ChatFormatting.DARK_AQUA)
                                .append(locations.get(effects.indexOf(effect)));
                    }
                }
                return text.toString();
            }
        case 2:
            if (SessionData.get(uuid, Key.A_EXPLOSIONS) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> locations = (LinkedList<String>) SessionData.get(uuid, Key.A_EXPLOSIONS);
                if (locations != null) {
                    for (final String loc : locations) {
                        text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA).append(loc);
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
            new ActionEffectSoundListPrompt(uuid).start();
            break;
        case 2:
            if (sender instanceof ServerPlayer) {
                final MinecraftServer server = plugin.getServer();
                if (server == null) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("unknownError")));
                    new FabricActionEffectPrompt(uuid).start();
                    break;
                }
                selectedExplosionLocations.put(uuid, BlockPos.ZERO);
                new ActionEffectExplosionPrompt(uuid).start();
            } else {
                sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("consoleError")));
                new FabricActionEffectPrompt(uuid).start();
            }
            break;
        case 3:
            new FabricActionMainPrompt(uuid).start();
            break;
        default:
            new FabricActionEffectPrompt(uuid).start();
            break;
        }
    }

    public class ActionEffectSoundListPrompt extends FabricActionsEditorIntegerPrompt {

        public ActionEffectSoundListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 4;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return FabricLang.get("eventEditorEffectsTitle");
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
                return ChatFormatting.YELLOW + FabricLang.get("eventEditorAddEffect");
            case 2:
                return ChatFormatting.YELLOW + FabricLang.get("eventEditorAddEffectLocation");
            case 3:
                return ChatFormatting.RED + FabricLang.get("clear");
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
                if (SessionData.get(uuid, Key.A_EFFECTS) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    for (final String s : (List<String>) Objects.requireNonNull(SessionData.get(uuid, Key.A_EFFECTS))) {
                        text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA).append(s);
                    }
                    return text.toString();
                }
            case 2:
                if (SessionData.get(uuid, Key.A_EFFECTS_LOCATIONS) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    for (final String s : (List<String>) Objects.requireNonNull(SessionData
                            .get(uuid, Key.A_EFFECTS_LOCATIONS))) {
                        text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA).append(s);
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
        @SuppressWarnings("unchecked")
        public void acceptInput(final Number input) {
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            switch (input.intValue()) {
            case 1:
                new ActionEffectSoundPrompt(uuid).start();
                break;
            case 2:
                if (SessionData.get(uuid, Key.A_EFFECTS) == null) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("eventEditorMustAddEffects")));
                    new ActionEffectSoundListPrompt(uuid).start();
                } else {
                    if (sender instanceof ServerPlayer) {
                        final MinecraftServer server = plugin.getServer();
                        if (server == null) {
                            sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("unknownError")));
                            new ActionEffectSoundListPrompt(uuid).start();
                            break;
                        }
                        selectedEffectLocations.put(uuid, BlockPos.ZERO);
                        new ActionEffectSoundLocationPrompt(uuid).start();
                    } else {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("consoleError")));
                        new ActionEffectSoundListPrompt(uuid).start();
                    }
                }
                break;
            case 3:
                sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("eventEditorEffectsCleared")));
                SessionData.set(uuid, Key.A_EFFECTS, null);
                SessionData.set(uuid, Key.A_EFFECTS_LOCATIONS, null);
                new ActionEffectSoundListPrompt(uuid).start();
                break;
            case 4:
                final int one;
                final int two;
                final List<String> effects = (List<String>) SessionData.get(uuid, Key.A_EFFECTS);
                final List<String> locations = (List<String>) SessionData.get(uuid, Key.A_EFFECTS_LOCATIONS);
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
                    new FabricActionMainPrompt(uuid).start();
                } else {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("listsNotSameSize")));
                    new ActionEffectSoundListPrompt(uuid).start();
                }
                break;
            default:
                new ActionEffectSoundListPrompt(uuid).start();
                break;
            }
        }
    }

    public class ActionEffectSoundPrompt extends FabricActionsEditorStringPrompt {

        public ActionEffectSoundPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return FabricLang.get("eventEditorEffectsTitle");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("effEnterName");
        }

        @Override
        public @NotNull String getPromptText() {
            final StringBuilder effects = new StringBuilder(ChatFormatting.LIGHT_PURPLE + getTitle() + "\n");
            effects.append(ChatFormatting.YELLOW).append(getQueryText());
            return effects.toString();
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
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                final LinkedList<String> effects;
                if (SessionData.get(uuid, Key.A_EFFECTS) != null) {
                    effects = (LinkedList<String>) SessionData.get(uuid, Key.A_EFFECTS);
                } else {
                    effects = new LinkedList<>();
                }
                if (effects != null) {
                    effects.add(input.toUpperCase());
                }
                SessionData.set(uuid, Key.A_EFFECTS, effects);
                if (sender instanceof ServerPlayer) {
                    selectedEffectLocations.remove(uuid);
                }
                new ActionEffectSoundListPrompt(uuid).start();
            } else {
                if (sender instanceof ServerPlayer) {
                    selectedEffectLocations.remove(uuid);
                }
                new ActionEffectSoundListPrompt(uuid).start();
            }
        }
    }

    public class ActionEffectSoundLocationPrompt extends FabricActionsEditorStringPrompt {

        public ActionEffectSoundLocationPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("eventEditorEffectLocationPrompt");
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
                final BlockPos block = selectedEffectLocations.get(uuid);
                if (block != null) {
                    final String locStr = block.getX() + " " + block.getY() + " " + block.getZ();
                    final LinkedList<String> locations;
                    if (SessionData.get(uuid, Key.A_EFFECTS_LOCATIONS) != null) {
                        locations = (LinkedList<String>) SessionData.get(uuid, Key.A_EFFECTS_LOCATIONS);
                    } else {
                        locations = new LinkedList<>();
                    }
                    if (locations != null) {
                        locations.add(locStr);
                    }
                    SessionData.set(uuid, Key.A_EFFECTS_LOCATIONS, locations);
                    selectedEffectLocations.remove(uuid);
                    new ActionEffectSoundListPrompt(uuid).start();
                } else {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("eventEditorSelectBlockFirst")));
                    new ActionEffectSoundLocationPrompt(uuid).start();
                }
            } else if (input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                selectedEffectLocations.remove(uuid);
                new ActionEffectSoundListPrompt(uuid).start();
            } else {
                new ActionEffectSoundLocationPrompt(uuid).start();
            }
        }
    }

    public class ActionEffectExplosionPrompt extends FabricActionsEditorStringPrompt {

        public ActionEffectExplosionPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("eventEditorExplosionPrompt");
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
                final BlockPos block = selectedExplosionLocations.get(uuid);
                if (block != null) {
                    final String locStr = block.getX() + " " + block.getY() + " " + block.getZ();
                    final LinkedList<String> locations;
                    if (SessionData.get(uuid, Key.A_EXPLOSIONS) != null) {
                        locations = (LinkedList<String>) SessionData.get(uuid, Key.A_EXPLOSIONS);
                    } else {
                        locations = new LinkedList<>();
                    }
                    if (locations != null) {
                        locations.add(locStr);
                    }
                    SessionData.set(uuid, Key.A_EXPLOSIONS, locations);
                    selectedExplosionLocations.remove(uuid);
                    new FabricActionMainPrompt(uuid).start();
                } else {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("eventEditorSelectBlockFirst")));
                    new ActionEffectExplosionPrompt(uuid).start();
                }
            } else if (input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.A_EXPLOSIONS, null);
                selectedExplosionLocations.remove(uuid);
                new FabricActionMainPrompt(uuid).start();
            } else if (input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                selectedExplosionLocations.remove(uuid);
                new FabricActionMainPrompt(uuid).start();
            } else {
                new ActionEffectExplosionPrompt(uuid).start();
            }
        }
    }
}
