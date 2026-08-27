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
import me.pikamug.quests.convo.generic.FabricItemStackPrompt;
import me.pikamug.quests.util.FabricItemUtil;
import me.pikamug.quests.util.FabricLang;
import me.pikamug.quests.util.FabricMiscUtil;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.RomanNumeral;
import me.pikamug.quests.util.SessionData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FabricActionPlayerPrompt extends FabricActionsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final FabricQuestsPlugin plugin;

    public static final ConcurrentHashMap<UUID, BlockPos> selectedTeleportLocations = new ConcurrentHashMap<>();

    public FabricActionPlayerPrompt(final @NotNull UUID uuid) {
        super(uuid);
        this.uuid = uuid;
        this.plugin = FabricQuestsPlugin.getInstance();
    }

    private final int size = 10;

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public String getTitle() {
        return FabricLang.get("eventEditorPlayer");
    }

    @Override
    public ChatFormatting getNumberColor(final int number) {
        switch (number) {
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        case 6:
        case 7:
        case 8:
        case 9:
            return ChatFormatting.BLUE;
        case 10:
            return ChatFormatting.GREEN;
        default:
            return null;
        }
    }

    @Override
    public String getSelectionText(final int number) {
        switch (number) {
        case 1:
            return ChatFormatting.YELLOW + FabricLang.get("eventEditorSetMessage");
        case 2:
            return ChatFormatting.YELLOW + FabricLang.get("eventEditorSetItems");
        case 3:
            return ChatFormatting.YELLOW + FabricLang.get("eventEditorSetPotionEffects");
        case 4:
            return ChatFormatting.YELLOW + FabricLang.get("eventEditorSetHunger");
        case 5:
            return ChatFormatting.YELLOW + FabricLang.get("eventEditorSetSaturation");
        case 6:
            return ChatFormatting.YELLOW + FabricLang.get("eventEditorSetHealth");
        case 7:
            return ChatFormatting.YELLOW + FabricLang.get("eventEditorSetTeleport");
        case 8:
            return ChatFormatting.YELLOW + FabricLang.get("eventEditorSetCommands");
        case 9:
            return ChatFormatting.YELLOW + FabricLang.get("eventEditorClearInv");
        case 10:
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
            if (SessionData.get(uuid, Key.A_MESSAGE) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + SessionData.get(uuid, Key.A_MESSAGE) + ChatFormatting.GRAY + ")";
            }
        case 2:
            if (SessionData.get(uuid, Key.A_ITEMS) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<ItemStack> items = (LinkedList<ItemStack>) SessionData.get(uuid, Key.A_ITEMS);
                if (items != null) {
                    for (final ItemStack is : items) {
                        if (is != null) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(FabricItemUtil.getName(is));
                        }
                    }
                    return text.toString();
                }
                break;
            }
        case 3:
            if (SessionData.get(uuid, Key.A_POTION_TYPES) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> types = (LinkedList<String>) SessionData.get(uuid, Key.A_POTION_TYPES);
                final LinkedList<Long> durations = (LinkedList<Long>) SessionData.get(uuid, Key.A_POTION_DURATIONS);
                final LinkedList<Integer> mags = (LinkedList<Integer>) SessionData.get(uuid, Key.A_POTION_STRENGTH);
                int index = -1;
                if (types != null && durations != null && mags != null) {
                    for (final String type : types) {
                        index++;
                        text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA).append(type)
                                .append(ChatFormatting.DARK_PURPLE).append(" ").append(RomanNumeral.getNumeral(mags
                                .get(index))).append(ChatFormatting.GRAY).append(" -> ").append(ChatFormatting.DARK_AQUA)
                                .append(FabricMiscUtil.formatTime(durations.get(index) * 50L));
                    }
                }
                return text.toString();
            }
        case 4:
            if (SessionData.get(uuid, Key.A_HUNGER) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + SessionData.get(uuid, Key.A_HUNGER) + ChatFormatting.GRAY
                        + ")";
            }
        case 5:
            if (SessionData.get(uuid, Key.A_SATURATION) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + SessionData.get(uuid, Key.A_SATURATION) + ChatFormatting.GRAY
                        + ")";
            }
        case 6:
            if (SessionData.get(uuid, Key.A_HEALTH) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + SessionData.get(uuid, Key.A_HEALTH) + ChatFormatting.GRAY
                        + ")";
            }
        case 7:
            if (SessionData.get(uuid, Key.A_TELEPORT) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + SessionData.get(uuid, Key.A_TELEPORT) + ChatFormatting.GRAY
                        + ")";
            }
        case 8:
            if (SessionData.get(uuid, Key.A_COMMANDS) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                for (final String s : (LinkedList<String>) Objects.requireNonNull(SessionData
                        .get(uuid, Key.A_COMMANDS))) {
                    text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA).append(s);
                }
                return text.toString();
            }
        case 9:
            if (SessionData.get(uuid, Key.A_CLEAR_INVENTORY) == null) {
                return ChatFormatting.GRAY + "(" + ChatFormatting.RED + FabricLang.get("false") + ChatFormatting.GRAY + ")";
            } else {
                final Boolean clearOpt = (Boolean) SessionData.get(uuid, Key.A_CLEAR_INVENTORY);
                return ChatFormatting.GRAY + "(" + (Boolean.TRUE.equals(clearOpt) ? ChatFormatting.GREEN + FabricLang.get("true")
                        : ChatFormatting.RED + FabricLang.get("false")) + ChatFormatting.GRAY + ")";
            }
        case 10:
            return "";
        default:
            return null;
        }
        return "";
    }

    @Override
    public @NotNull String getPromptText() {
        if (SessionData.get(uuid, Key.A_CLEAR_INVENTORY) == null) {
            SessionData.set(uuid, Key.A_CLEAR_INVENTORY, false);
        }

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
            new ActionPlayerMessagePrompt(uuid).start();
            break;
        case 2:
            new ActionPlayerItemListPrompt(uuid).start();
            break;
        case 3:
            new ActionPlayerPotionListPrompt(uuid).start();
            break;
        case 4:
            new ActionPlayerHungerPrompt(uuid).start();
            break;
        case 5:
            new ActionPlayerSaturationPrompt(uuid).start();
            break;
        case 6:
            new ActionPlayerHealthPrompt(uuid).start();
            break;
        case 7:
            if (sender instanceof ServerPlayer) {
                final MinecraftServer server = plugin.getServer();
                if (server == null) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("unknownError")));
                    new FabricActionPlayerPrompt(uuid).start();
                    break;
                }
                selectedTeleportLocations.put(uuid, BlockPos.ZERO);
                new ActionPlayerTeleportPrompt(uuid).start();
            } else {
                sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("consoleError")));
                new FabricActionPlayerPrompt(uuid).start();
            }
            break;
        case 8:
            if (!plugin.hasLimitedAccess(uuid)) {
                new ActionPlayerCommandsPrompt(uuid).start();
            } else {
                sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("noPermission")));
                new FabricActionPlayerPrompt(uuid).start();
            }
            break;
        case 9:
            final Boolean b = (Boolean) SessionData.get(uuid, Key.A_CLEAR_INVENTORY);
            if (Boolean.TRUE.equals(b)) {
                SessionData.set(uuid, Key.A_CLEAR_INVENTORY, false);
            } else {
                SessionData.set(uuid, Key.A_CLEAR_INVENTORY, true);
            }
            new FabricActionPlayerPrompt(uuid).start();
            break;
        case 10:
            new FabricActionMainPrompt(uuid).start();
            break;
        default:
            new FabricActionPlayerPrompt(uuid).start();
            break;
        }
    }

    public class ActionPlayerMessagePrompt extends FabricActionsEditorStringPrompt {

        public ActionPlayerMessagePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("eventEditorSetMessagePrompt");
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
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel")) && !input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.A_MESSAGE, input);
            } else if (input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.A_MESSAGE, null);
            }
            new FabricActionMainPrompt(uuid).start();
        }
    }

    public class ActionPlayerItemListPrompt extends FabricActionsEditorIntegerPrompt {

        public ActionPlayerItemListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 3;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return FabricLang.get("eventEditorGiveItemsTitle");
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
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorDeliveryAddItem");
            case 2:
                return ChatFormatting.RED + FabricLang.get("clear");
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
                if (SessionData.get(uuid, Key.A_ITEMS) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    for (final ItemStack is : (List<ItemStack>) Objects.requireNonNull(SessionData
                            .get(uuid, Key.A_ITEMS))) {
                        text.append("\n").append(ChatFormatting.GRAY).append("     - ")
                                .append(FabricItemUtil.getDisplayString(is));
                    }
                    return text.toString();
                }
            case 2:
            case 3:
                return "";
            default:
                return null;
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public @NotNull String getPromptText() {
            if (SessionData.get(uuid, "tempStack") != null) {
                if (SessionData.get(uuid, Key.A_ITEMS) != null) {
                    final List<ItemStack> items = (List<ItemStack>) SessionData.get(uuid, Key.A_ITEMS);
                    if (items != null) {
                        items.add((ItemStack) SessionData.get(uuid, "tempStack"));
                        SessionData.set(uuid, Key.A_ITEMS, items);
                    }
                } else {
                    final LinkedList<ItemStack> itemRewards = new LinkedList<>();
                    itemRewards.add((ItemStack) SessionData.get(uuid, "tempStack"));
                    SessionData.set(uuid, Key.A_ITEMS, itemRewards);
                }
                FabricItemStackPrompt.clearSessionData(uuid);
            }

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
                new FabricItemStackPrompt(uuid, ActionPlayerItemListPrompt.this).start();
                break;
            case 2:
                sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("eventEditorItemsCleared")));
                SessionData.set(uuid, Key.A_ITEMS, null);
                new ActionPlayerItemListPrompt(uuid).start();
                break;
            case 3:
                new FabricActionMainPrompt(uuid).start();
                break;
            default:
                new ActionPlayerItemListPrompt(uuid).start();
                break;
            }
        }
    }

    public class ActionPlayerPotionListPrompt extends FabricActionsEditorIntegerPrompt {

        public ActionPlayerPotionListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 5;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return FabricLang.get("eventEditorPotionEffectsTitle");
        }

        @Override
        public ChatFormatting getNumberColor(final int number) {
            switch (number) {
            case 1:
            case 2:
            case 3:
                return ChatFormatting.BLUE;
            case 4:
                return ChatFormatting.RED;
            case 5:
                return ChatFormatting.GREEN;
            default:
                return null;
            }
        }

        @Override
        public String getSelectionText(final int number) {
            switch (number) {
            case 1:
                return ChatFormatting.YELLOW + FabricLang.get("eventEditorSetPotionEffectTypes");
            case 2:
                return ChatFormatting.YELLOW + FabricLang.get("eventEditorSetPotionDurations");
            case 3:
                return ChatFormatting.YELLOW + FabricLang.get("eventEditorSetPotionMagnitudes");
            case 4:
                return ChatFormatting.RED + FabricLang.get("clear");
            case 5:
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
                if (SessionData.get(uuid, Key.A_POTION_TYPES) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    for (final String s : (LinkedList<String>) Objects.requireNonNull(SessionData
                            .get(uuid, Key.A_POTION_TYPES))) {
                        text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA).append(s);
                    }
                    return text.toString();
                }
            case 2:
                if (SessionData.get(uuid, Key.A_POTION_DURATIONS) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    for (final Long l : (LinkedList<Long>) Objects.requireNonNull(SessionData.get(uuid,
                            Key.A_POTION_DURATIONS))) {
                        text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.DARK_AQUA)
                                .append(FabricMiscUtil.formatTime(l * 50L));
                    }
                    return text.toString();
                }
            case 3:
                if (SessionData.get(uuid, Key.A_POTION_STRENGTH) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    for (final int i : (LinkedList<Integer>) Objects.requireNonNull(SessionData.get(uuid,
                            Key.A_POTION_STRENGTH))) {
                        text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.DARK_PURPLE)
                                .append(i);
                    }
                    return text.toString();
                }
            case 4:
            case 5:
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
                new ActionPlayerPotionTypesPrompt(uuid).start();
                break;
            case 2:
                if (SessionData.get(uuid, Key.A_POTION_TYPES) == null) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("eventEditorMustSetPotionTypesFirst")));
                    new ActionPlayerPotionListPrompt(uuid).start();
                } else {
                    new ActionPlayerPotionDurationsPrompt(uuid).start();
                }
                break;
            case 3:
                if (SessionData.get(uuid, Key.A_POTION_TYPES) == null) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED
                            + FabricLang.get("eventEditorMustSetPotionTypesAndDurationsFirst")));
                    new ActionPlayerPotionListPrompt(uuid).start();
                } else if (SessionData.get(uuid, Key.A_POTION_DURATIONS) == null) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED
                            + FabricLang.get("eventEditorMustSetPotionDurationsFirst")));
                    new ActionPlayerPotionListPrompt(uuid).start();
                } else {
                    new ActionPlayerPotionMagnitudesPrompt(uuid).start();
                }
                break;
            case 4:
                sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("eventEditorPotionsCleared")));
                SessionData.set(uuid, Key.A_POTION_TYPES, null);
                SessionData.set(uuid, Key.A_POTION_DURATIONS, null);
                SessionData.set(uuid, Key.A_POTION_STRENGTH, null);
                new ActionPlayerPotionListPrompt(uuid).start();
                break;
            case 5:
                final int one;
                final int two;
                final int three;
                final List<String> types = (List<String>) SessionData.get(uuid, Key.A_POTION_TYPES);
                final List<Long> durations = (List<Long>) SessionData.get(uuid, Key.A_POTION_DURATIONS);
                final List<Integer> strength = (List<Integer>) SessionData.get(uuid, Key.A_POTION_STRENGTH);
                if (types != null) {
                    one = types.size();
                } else {
                    one = 0;
                }
                if (durations != null) {
                    two = durations.size();
                } else {
                    two = 0;
                }
                if (strength != null) {
                    three = strength.size();
                } else {
                    three = 0;
                }
                if (one == two && two == three) {
                    new FabricActionMainPrompt(uuid).start();
                } else {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("eventEditorListSizeMismatch")));
                    new ActionPlayerPotionListPrompt(uuid).start();
                }
                break;
            default:
                new ActionPlayerPotionListPrompt(uuid).start();
                break;
            }
        }
    }

    public class ActionPlayerPotionTypesPrompt extends FabricActionsEditorStringPrompt {

        public ActionPlayerPotionTypesPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return FabricLang.get("eventEditorPotionTypesTitle");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("eventEditorSetPotionEffectsPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final StringBuilder potions = new StringBuilder(ChatFormatting.LIGHT_PURPLE + getTitle() + "\n");
            final List<MobEffect> effArr = new LinkedList<>();
            net.minecraft.core.Registry.MOB_EFFECT.stream()
                    .sorted(Comparator.comparing(e -> e.getDescriptionId()))
                    .forEach(effArr::add);
            for (int i = 0; i < effArr.size(); i++) {
                final String name = effArr.get(i).getDescriptionId()
                        .replaceFirst("effect\\.minecraft\\.", "").toUpperCase();
                potions.append(ChatFormatting.AQUA).append(name);
                if (i < (effArr.size() - 1)) {
                    potions.append(ChatFormatting.GRAY).append(", ");
                }
            }
            potions.append("\n").append(ChatFormatting.YELLOW).append(getQueryText());
            return potions.toString();
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
                final LinkedList<String> effTypes = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    final MobEffect effect = net.minecraft.core.Registry.MOB_EFFECT.get(
                            new net.minecraft.resources.ResourceLocation("minecraft", s.toLowerCase()));
                    if (effect != null && effect != net.minecraft.world.effect.MobEffects.EMPTY) {
                        effTypes.add(s.toUpperCase());
                        SessionData.set(uuid, Key.A_POTION_TYPES, effTypes);
                    } else {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("eventEditorInvalidPotionType")
                                .replace("<input>", s)));
                        new ActionPlayerPotionTypesPrompt(uuid).start();
                        return;
                    }
                }
            }
            new ActionPlayerPotionListPrompt(uuid).start();
        }
    }

    public class ActionPlayerPotionDurationsPrompt extends FabricActionsEditorStringPrompt {

        public ActionPlayerPotionDurationsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("eventEditorSetPotionDurationsPrompt");
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
                final LinkedList<Long> effDurations = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    try {
                        final int i = Integer.parseInt(s);
                        final long l = i * 1000L;
                        if (l < 1000) {
                            sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("invalidMinimum")
                                    .replace("<number>", "1")));
                            new ActionPlayerPotionDurationsPrompt(uuid).start();
                            return;
                        }
                        effDurations.add(l / 50L);
                    } catch (final NumberFormatException e) {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("reqNotANumber")
                                .replace("<input>", s)));
                        new ActionPlayerPotionDurationsPrompt(uuid).start();
                        return;
                    }
                }
                SessionData.set(uuid, Key.A_POTION_DURATIONS, effDurations);
            }
            new ActionPlayerPotionListPrompt(uuid).start();
        }
    }

    public class ActionPlayerPotionMagnitudesPrompt extends FabricActionsEditorStringPrompt {

        public ActionPlayerPotionMagnitudesPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("eventEditorSetPotionMagnitudesPrompt");
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
                final LinkedList<Integer> magAmounts = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    try {
                        final int i = Integer.parseInt(s);
                        if (i < 1) {
                            sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("invalidMinimum")
                                    .replace("<number>", "1")));
                            new ActionPlayerPotionMagnitudesPrompt(uuid).start();
                            return;
                        }
                        magAmounts.add(i);
                    } catch (final NumberFormatException e) {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("reqNotANumber")
                                .replace("<input>", s)));
                        new ActionPlayerPotionMagnitudesPrompt(uuid).start();
                        return;
                    }
                }
                SessionData.set(uuid, Key.A_POTION_STRENGTH, magAmounts);
            }
            new ActionPlayerPotionListPrompt(uuid).start();
        }
    }

    public class ActionPlayerHungerPrompt extends FabricActionsEditorStringPrompt {

        public ActionPlayerHungerPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("eventEditorSetHungerPrompt");
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
            if (!input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i < 0) {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED
                                + FabricLang.get("invalidMinimum").replace("<number>", "0")));
                        new ActionPlayerHungerPrompt(uuid).start();
                        return;
                    } else {
                        SessionData.set(uuid, Key.A_HUNGER, i);
                    }
                } catch (final NumberFormatException e) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED
                            + FabricLang.get("reqNotANumber").replace("<input>", input)));
                    new ActionPlayerHungerPrompt(uuid).start();
                    return;
                }
            } else {
                SessionData.set(uuid, Key.A_HUNGER, null);
            }
            new FabricActionMainPrompt(uuid).start();
        }
    }

    public class ActionPlayerSaturationPrompt extends FabricActionsEditorStringPrompt {

        public ActionPlayerSaturationPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("eventEditorSetSaturationPrompt");
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
            if (!input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i < 0) {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED
                                + FabricLang.get("invalidMinimum").replace("<number>", "0")));
                        new ActionPlayerSaturationPrompt(uuid).start();
                        return;
                    } else {
                        SessionData.set(uuid, Key.A_SATURATION, i);
                    }
                } catch (final NumberFormatException e) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED
                            + FabricLang.get("reqNotANumber").replace("<input>", input)));
                    new ActionPlayerSaturationPrompt(uuid).start();
                    return;
                }
            } else {
                SessionData.set(uuid, Key.A_SATURATION, null);
            }
            new FabricActionMainPrompt(uuid).start();
        }
    }

    public class ActionPlayerHealthPrompt extends FabricActionsEditorStringPrompt {

        public ActionPlayerHealthPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("eventEditorSetHealthPrompt");
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
            if (!input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i < 0) {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED
                                + FabricLang.get("invalidMinimum").replace("<number>", "0")));
                        new ActionPlayerHealthPrompt(uuid).start();
                        return;
                    } else {
                        SessionData.set(uuid, Key.A_HEALTH, i);
                    }
                } catch (final NumberFormatException e) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED
                            + FabricLang.get("reqNotANumber").replace("<input>", input)));
                    new ActionPlayerHealthPrompt(uuid).start();
                    return;
                }
            } else {
                SessionData.set(uuid, Key.A_HEALTH, null);
            }
            new FabricActionMainPrompt(uuid).start();
        }
    }

    public class ActionPlayerTeleportPrompt extends FabricActionsEditorStringPrompt {

        public ActionPlayerTeleportPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("eventEditorSetTeleportPrompt");
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
            if (input.equalsIgnoreCase(FabricLang.get("cmdDone"))) {
                final BlockPos block = selectedTeleportLocations.get(uuid);
                if (block != null) {
                    final String locStr = block.getX() + " " + block.getY() + " " + block.getZ();
                    SessionData.set(uuid, Key.A_TELEPORT, locStr);
                    selectedTeleportLocations.remove(uuid);
                } else {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("eventEditorSelectBlockFirst")));
                    new ActionPlayerTeleportPrompt(uuid).start();
                    return;
                }
                new FabricActionMainPrompt(uuid).start();
            } else if (input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.A_TELEPORT, null);
                selectedTeleportLocations.remove(uuid);
                new FabricActionMainPrompt(uuid).start();
            } else if (input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                selectedTeleportLocations.remove(uuid);
                new FabricActionMainPrompt(uuid).start();
            } else {
                new ActionPlayerTeleportPrompt(uuid).start();
            }
        }
    }

    public class ActionPlayerCommandsPrompt extends FabricActionsEditorStringPrompt {

        public ActionPlayerCommandsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("eventEditorSetCommandsPrompt");
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
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel")) && !input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                final String[] commands = input.split(FabricLang.get("charSemi"));
                final LinkedList<String> cmdList = new LinkedList<>(Arrays.asList(commands));
                SessionData.set(uuid, Key.A_COMMANDS, cmdList);
            } else if (input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.A_COMMANDS, null);
            }
            new FabricActionMainPrompt(uuid).start();
        }
    }
}
