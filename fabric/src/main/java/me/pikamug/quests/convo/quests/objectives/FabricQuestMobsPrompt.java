/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.convo.quests.objectives;

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.convo.quests.FabricQuestsEditorIntegerPrompt;
import me.pikamug.quests.convo.quests.FabricQuestsEditorStringPrompt;
import me.pikamug.quests.convo.quests.stages.FabricQuestStageMainPrompt;
import me.pikamug.quests.util.FabricConfigUtil;
import me.pikamug.quests.util.FabricLang;
import me.pikamug.quests.util.FabricMiscUtil;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.SessionData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FabricQuestMobsPrompt extends FabricQuestsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final FabricQuestsPlugin plugin;
    private final int stageNum;
    private final String pref;

    public FabricQuestMobsPrompt(final int stageNum, final @NotNull UUID uuid) {
        super(uuid);
        this.uuid = uuid;
        this.plugin = FabricQuestsPlugin.getInstance();
        this.stageNum = stageNum;
        this.pref = "stage" + stageNum;
    }

    private final int size = 6;

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public String getTitle() {
        return FabricLang.get("stageEditorMobs");
    }

    @Override
    public ChatFormatting getNumberColor(final int number) {
        switch (number) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                return ChatFormatting.BLUE;
            case 6:
                return ChatFormatting.GREEN;
            default:
                return null;
        }
    }

    @Override
    public String getSelectionText(final int number) {
        switch(number) {
        case 1:
            return ChatFormatting.YELLOW + FabricLang.get("stageEditorKillMobs");
        case 2:
            return ChatFormatting.YELLOW + FabricLang.get("stageEditorTameMobs");
        case 3:
            return ChatFormatting.YELLOW + FabricLang.get("stageEditorCatchFish");
        case 4:
            return ChatFormatting.YELLOW + FabricLang.get("stageEditorMilkCows");
        case 5:
            return ChatFormatting.YELLOW + FabricLang.get("stageEditorShearSheep");
        case 6:
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
            if (SessionData.get(uuid, pref + Key.S_MOB_TYPES) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> mobs = (LinkedList<String>) SessionData.get(uuid, pref + Key.S_MOB_TYPES);
                final LinkedList<Integer> amounts
                        = (LinkedList<Integer>) SessionData.get(uuid, pref + Key.S_MOB_AMOUNTS);
                if (mobs != null && amounts != null) {
                    if (SessionData.get(uuid, pref + Key.S_MOB_KILL_LOCATIONS) == null) {
                        for (int i = 0; i < mobs.size(); i++) {
                            if (FabricMiscUtil.getProperMobType(mobs.get(i)) != null) {
                                text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA)
                                        .append(FabricMiscUtil.getEntityName(Objects.requireNonNull(FabricMiscUtil
                                                .getProperMobType(mobs.get(i))))).append(ChatFormatting.GRAY).append(" x ")
                                        .append(ChatFormatting.DARK_AQUA).append(amounts.get(i));
                            }
                        }
                    } else {
                        final LinkedList<String> locations
                                = (LinkedList<String>) SessionData.get(uuid, pref + Key.S_MOB_KILL_LOCATIONS);
                        final LinkedList<Integer> radii
                                = (LinkedList<Integer>) SessionData.get(uuid, pref + Key.S_MOB_KILL_LOCATIONS_RADIUS);
                        final LinkedList<String> names
                                = (LinkedList<String>) SessionData.get(uuid, pref + Key.S_MOB_KILL_LOCATIONS_NAMES);
                        if (locations != null && radii != null && names != null) {
                            for (int i = 0; i < mobs.size(); i++) {
                                String msg = FabricLang.get("blocksWithin");
                                msg = msg.replace("<amount>", String.valueOf(radii.get(i)));
                                text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.BLUE)
                                        .append(FabricMiscUtil.getEntityName(Objects.requireNonNull(FabricMiscUtil
                                                .getProperMobType(mobs.get(i))))).append(ChatFormatting.GRAY).append(" x ")
                                        .append(ChatFormatting.DARK_AQUA).append(amounts.get(i)).append(ChatFormatting.GRAY)
                                        .append(msg).append(ChatFormatting.YELLOW).append(names.get(i)).append(" (")
                                        .append(locations.get(i)).append(")");
                            }
                        }
                    }
                }
                return text.toString();
            }
        case 2:
            if (SessionData.get(uuid, pref + Key.S_TAME_TYPES) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> mobs = (LinkedList<String>) SessionData.get(uuid, pref + Key.S_TAME_TYPES);
                final LinkedList<Integer> amounts
                        = (LinkedList<Integer>) SessionData.get(uuid, pref + Key.S_TAME_AMOUNTS);
                if (mobs != null && amounts != null) {
                    for (int i = 0; i < mobs.size(); i++) {
                        text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.BLUE)
                                .append(mobs.get(i)).append(ChatFormatting.GRAY).append(" x ").append(ChatFormatting.AQUA)
                                .append(amounts.get(i));
                    }
                }
                return text.toString();
            }
        case 3:
            if (SessionData.get(uuid, pref + Key.S_FISH) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final Integer fish = (Integer) SessionData.get(uuid, pref + Key.S_FISH);
                return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + fish + " " + FabricLang.get("stageEditorFish")
                        + ChatFormatting.GRAY + ")";
            }
        case 4:
            if (SessionData.get(uuid, pref + Key.S_COW_MILK) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final Integer cows = (Integer) SessionData.get(uuid, pref + Key.S_COW_MILK);
                return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + cows + " " + FabricLang.get("stageEditorCows")
                        + ChatFormatting.GRAY + ")";
            }
        case 5:
            if (SessionData.get(uuid, pref + Key.S_SHEAR_COLORS) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> colors = (LinkedList<String>) SessionData.get(uuid, pref + Key.S_SHEAR_COLORS);
                final LinkedList<Integer> amounts
                        = (LinkedList<Integer>) SessionData.get(uuid, pref + Key.S_SHEAR_AMOUNTS);
                if (colors != null && amounts != null) {
                    for (int i = 0; i < colors.size(); i++) {
                        text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.BLUE)
                                .append(colors.get(i)).append(ChatFormatting.GRAY).append(" x ").append(ChatFormatting.AQUA)
                                .append(amounts.get(i));
                    }
                }
                return text.toString();
            }
        case 6:
            return "";
        default:
            return null;
        }
    }

    @Override
    public @NotNull String getPromptText() {
        SessionData.set(uuid, pref, Boolean.TRUE);

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
        final net.minecraft.server.level.ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
        if (sender == null) {
            return;
        }
        switch(input.intValue()) {
        case 1:
            new QuestMobsKillListPrompt(uuid).start();
            break;
        case 2:
            new QuestMobsTameListPrompt(uuid).start();
            break;
        case 3:
            new QuestMobsFishPrompt(uuid).start();
            break;
        case 4:
            new QuestMobsCowsPrompt(uuid).start();
            break;
        case 5:
            new QuestMobsShearListPrompt(uuid).start();
            break;
        case 6:
            try {
                new FabricQuestStageMainPrompt(stageNum, uuid).start();
            } catch (final Exception e) {
                sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        ChatFormatting.RED + FabricLang.get("itemCreateCriticalError")));
                return;
            }
            break;
        default:
            new FabricQuestMobsPrompt(stageNum, uuid).start();
            break;
        }
    }

    public class QuestMobsKillListPrompt extends FabricQuestsEditorIntegerPrompt {

        public QuestMobsKillListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 7;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return FabricLang.get("stageEditorKillMobs");
        }

        @Override
        public ChatFormatting getNumberColor(final int number) {
            switch (number) {
                case 1:
                case 2:
                case 4:
                case 5:
                    return ChatFormatting.BLUE;
                case 3:
                    if (FabricMiscUtil.getPlayer(uuid, plugin) instanceof net.minecraft.server.level.ServerPlayer) {
                        return ChatFormatting.BLUE;
                    } else {
                        return ChatFormatting.GRAY;
                    }
                case 6:
                    return ChatFormatting.RED;
                case 7:
                    return ChatFormatting.GREEN;
                default:
                    return null;
            }
        }

        @Override
        public String getSelectionText(final int number) {
            switch(number) {
            case 1:
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorSetMobTypes");
            case 2:
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorSetMobAmounts");
            case 3:
                if (FabricMiscUtil.getPlayer(uuid, plugin) instanceof net.minecraft.server.level.ServerPlayer) {
                    return ChatFormatting.YELLOW + FabricLang.get("stageEditorSetKillLocations");
                } else {
                    return ChatFormatting.GRAY + FabricLang.get("stageEditorSetKillLocations");
                }
            case 4:
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorSetKillLocationRadii");
            case 5:
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorSetKillLocationNames");
            case 6:
                return ChatFormatting.RED + FabricLang.get("clear");
            case 7:
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
                if (SessionData.get(uuid, pref + Key.S_MOB_TYPES) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> mobTypes = (List<String>) SessionData.get(uuid, pref + Key.S_MOB_TYPES);
                    if (mobTypes != null) {
                        for (final String s : mobTypes) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA).append(s);
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (SessionData.get(uuid, pref + Key.S_MOB_AMOUNTS) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<Integer> mobAmounts = (List<Integer>) SessionData.get(uuid, pref + Key.S_MOB_AMOUNTS);
                    if (mobAmounts != null) {
                        for (final Integer i : mobAmounts) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA).append(i);
                        }
                    }
                    return text.toString();
                }
            case 3:
                if (SessionData.get(uuid, pref + Key.S_MOB_KILL_LOCATIONS) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> mobsKillLocations
                            = (List<String>) SessionData.get(uuid, pref + Key.S_MOB_KILL_LOCATIONS);
                    if (mobsKillLocations != null) {
                        for (final String s : mobsKillLocations) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA).append(s);
                        }
                    }
                    return text.toString();
                }
            case 4:
                if (SessionData.get(uuid, pref + Key.S_MOB_KILL_LOCATIONS_RADIUS) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<Integer> mobKillLocationsRadius
                            = (List<Integer>) SessionData.get(uuid, pref + Key.S_MOB_KILL_LOCATIONS_RADIUS);
                    if (mobKillLocationsRadius != null) {
                        for (final int i : mobKillLocationsRadius) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA).append(i);
                        }
                    }
                    return text.toString();
                }
            case 5:
                if (SessionData.get(uuid, pref + Key.S_MOB_KILL_LOCATIONS_NAMES) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> mobKillLocationsNames
                            = (List<String>) SessionData.get(uuid, pref + Key.S_MOB_KILL_LOCATIONS_NAMES);
                    if (mobKillLocationsNames != null) {
                        for (final String s : mobKillLocationsNames) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA).append(s);
                        }
                    }
                    return text.toString();
                }
            case 6:
            case 7:
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
        @SuppressWarnings("unchecked")
        public void acceptInput(final Number input) {
            final net.minecraft.server.level.ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            switch (input.intValue()) {
            case 1:
                new QuestMobsTypesPrompt(uuid).start();
                break;
            case 2:
                new QuestMobsAmountsPrompt(uuid).start();
                break;
            case 3:
                if (sender instanceof net.minecraft.server.level.ServerPlayer) {
                    final ConcurrentHashMap<UUID, net.minecraft.world.level.block.Block> temp
                            = plugin.getQuestFactory().getSelectedKillLocations();
                    if (FabricMiscUtil.getWorlds().isEmpty()) {
                        sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                ChatFormatting.RED + FabricLang.get("unknownError")));
                        new QuestMobsKillListPrompt(uuid).start();
                        break;
                    }
                    temp.put(uuid, Blocks.AIR);
                    plugin.getQuestFactory().setSelectedKillLocations(temp);
                    new QuestMobsLocationPrompt(uuid).start();
                } else {
                    sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            ChatFormatting.YELLOW + FabricLang.get("consoleError")));
                    new QuestMobsKillListPrompt(uuid).start();
                }
                break;
            case 4:
                new QuestMobsRadiiPrompt(uuid).start();
                break;
            case 5:
                new QuestMobsLocationNamesPrompt(uuid).start();
                break;
            case 6:
                sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        ChatFormatting.YELLOW + FabricLang.get("stageEditorObjectiveCleared")));
                SessionData.set(uuid, pref + Key.S_MOB_TYPES, null);
                SessionData.set(uuid, pref + Key.S_MOB_AMOUNTS, null);
                SessionData.set(uuid, pref + Key.S_MOB_KILL_LOCATIONS, null);
                SessionData.set(uuid, pref + Key.S_MOB_KILL_LOCATIONS_RADIUS, null);
                SessionData.set(uuid, pref + Key.S_MOB_KILL_LOCATIONS_NAMES, null);
                new QuestMobsKillListPrompt(uuid).start();
                break;
            case 7:
                final int one;
                final int two;
                final int three;
                final int four;
                final int five;
                final List<String> types = (List<String>) SessionData.get(uuid, pref + Key.S_MOB_TYPES);
                final List<Integer> amounts = (List<Integer>) SessionData.get(uuid, pref + Key.S_MOB_AMOUNTS);
                final List<String> locations = (List<String>) SessionData.get(uuid, pref + Key.S_MOB_KILL_LOCATIONS);
                final List<Integer> radii
                        = (List<Integer>) SessionData.get(uuid, pref + Key.S_MOB_KILL_LOCATIONS_RADIUS);
                final List<String> names = (List<String>) SessionData.get(uuid, pref + Key.S_MOB_KILL_LOCATIONS_NAMES);
                if (types != null) {
                    one = types.size();
                } else {
                    one = 0;
                }
                if (amounts != null) {
                    two = amounts.size();
                } else {
                    two = 0;
                }
                if (locations != null) {
                    three = locations.size();
                } else {
                    three = 0;
                }
                if (radii != null) {
                    four = radii.size();
                } else {
                    four = 0;
                }
                if (names != null) {
                    five = names.size();
                } else {
                    five = 0;
                }
                if (one == two) {
                    if (three != 0 || four != 0 || five != 0) {
                        if (two == three && three == four && four == five) {
                            new FabricQuestMobsPrompt(stageNum, uuid).start();
                        } else {
                            sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                    ChatFormatting.RED + FabricLang.get("listsNotSameSize")));
                            new QuestMobsKillListPrompt(uuid).start();
                        }
                    } else {
                        new FabricQuestMobsPrompt(stageNum, uuid).start();
                    }
                } else {
                    sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            ChatFormatting.RED + FabricLang.get("listsNotSameSize")));
                    new QuestMobsKillListPrompt(uuid).start();
                }
                break;
            default:
                new FabricQuestMobsPrompt(stageNum, uuid).start();
                break;
            }
        }
    }

    public class QuestMobsTypesPrompt extends FabricQuestsEditorStringPrompt {

        public QuestMobsTypesPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return FabricLang.get("eventEditorMobsTitle");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorMobsPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final StringBuilder mobs = new StringBuilder(ChatFormatting.LIGHT_PURPLE + getTitle() + "\n");
            final List<EntityType<?>> mobArr = new LinkedList<>();
            BuiltInRegistries.ENTITY_TYPE.iterator().forEachRemaining(mobArr::add);
            final List<EntityType<?>> toRemove = new LinkedList<>();
            for (final EntityType<?> type : mobArr) {
                if (type.getCategory() == MobCategory.MISC || type == EntityType.PLAYER) {
                    toRemove.add(type);
                }
            }
            mobArr.removeAll(toRemove);
            mobArr.sort(Comparator.comparing(type -> BuiltInRegistries.ENTITY_TYPE.getKey(type).getPath()));
            for (int i = 0; i < mobArr.size(); i++) {
                mobs.append(ChatFormatting.AQUA).append(FabricMiscUtil.snakeCaseToUpperCamelCase(
                        BuiltInRegistries.ENTITY_TYPE.getKey(mobArr.get(i)).getPath()));
                if (i < (mobArr.size() - 1)) {
                     mobs.append(ChatFormatting.GRAY).append(", ");
                }
            }
            mobs.append("\n").append(ChatFormatting.YELLOW).append(getQueryText());
            return mobs.toString();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final net.minecraft.server.level.ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                final LinkedList<String> mobTypes = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    if (FabricMiscUtil.getProperMobType(s) != null) {
                        mobTypes.add(s);
                    } else {
                        sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                ChatFormatting.RED + FabricLang.get("stageEditorInvalidMob")
                                        .replace("<input>", s)));
                        new QuestMobsTypesPrompt(uuid).start();
                        return;
                    }
                }
                SessionData.set(uuid, pref + Key.S_MOB_TYPES, mobTypes);

                LinkedList<Integer> amounts = new LinkedList<>();
                if (SessionData.get(uuid, pref + Key.S_MOB_AMOUNTS) != null) {
                    amounts = (LinkedList<Integer>) SessionData.get(uuid, pref + Key.S_MOB_AMOUNTS);
                }
                if (amounts != null) {
                    for (int i = 0; i < mobTypes.size(); i++) {
                        if (i >= amounts.size()) {
                            amounts.add(1);
                        }
                    }
                }
                SessionData.set(uuid, pref + Key.S_MOB_AMOUNTS, amounts);
            }
            new QuestMobsKillListPrompt(uuid).start();
        }
    }

    public class QuestMobsAmountsPrompt extends FabricQuestsEditorStringPrompt {

        public QuestMobsAmountsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorMobAmountsPrompt");
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
            final net.minecraft.server.level.ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                final LinkedList<Integer> mobAmounts = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    try {
                        final int i = Integer.parseInt(s);
                        if (i < 1) {
                            sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                    ChatFormatting.RED + FabricLang.get("invalidMinimum")
                                            .replace("<number>", "1")));
                            new QuestMobsAmountsPrompt(uuid).start();
                            return;
                        }
                        mobAmounts.add(i);
                    } catch (final NumberFormatException e) {
                        sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                ChatFormatting.RED + FabricLang.get("reqNotANumber")
                                        .replace("<input>", input)));
                        new QuestMobsAmountsPrompt(uuid).start();
                        return;
                    }
                }
                SessionData.set(uuid, pref + Key.S_MOB_AMOUNTS, mobAmounts);
            }
            new QuestMobsKillListPrompt(uuid).start();
        }
    }

    public class QuestMobsLocationPrompt extends FabricQuestsEditorStringPrompt {

        public QuestMobsLocationPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorMobLocationPrompt");
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
            final net.minecraft.server.level.ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            if (input.equalsIgnoreCase(FabricLang.get("cmdAdd"))) {
                final net.minecraft.world.level.block.Block block
                        = plugin.getQuestFactory().getSelectedKillLocations().get(uuid);
                if (block != null) {
                    final LinkedList<String> locations;
                    if (SessionData.get(uuid, pref + Key.S_MOB_KILL_LOCATIONS) != null) {
                        locations = (LinkedList<String>) SessionData.get(uuid, pref + Key.S_MOB_KILL_LOCATIONS);
                    } else {
                        locations = new LinkedList<>();
                    }
                    if (locations != null) {
                        locations.add(FabricConfigUtil.getLocationInfo(sender.blockPosition()));
                    }
                    SessionData.set(uuid, pref + Key.S_MOB_KILL_LOCATIONS, locations);
                    final ConcurrentHashMap<UUID, net.minecraft.world.level.block.Block> temp
                            = plugin.getQuestFactory().getSelectedKillLocations();
                    temp.remove(uuid);
                    plugin.getQuestFactory().setSelectedKillLocations(temp);
                } else {
                    sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            ChatFormatting.RED + FabricLang.get("stageEditorNoBlockSelected")));
                    new QuestMobsLocationPrompt(uuid).start();
                    return;
                }
                new QuestMobsKillListPrompt(uuid).start();
            } else if (input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                final ConcurrentHashMap<UUID, net.minecraft.world.level.block.Block> temp
                        = plugin.getQuestFactory().getSelectedKillLocations();
                temp.remove(uuid);
                plugin.getQuestFactory().setSelectedKillLocations(temp);
                new QuestMobsKillListPrompt(uuid).start();
            } else {
                new QuestMobsLocationPrompt(uuid).start();
            }
        }
    }

    public class QuestMobsRadiiPrompt extends FabricQuestsEditorStringPrompt {

        public QuestMobsRadiiPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorMobLocationRadiiPrompt");
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
            final net.minecraft.server.level.ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                final LinkedList<Integer> radii = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    try {
                        final int i = Integer.parseInt(s);
                        if (i < 1) {
                            sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                    ChatFormatting.RED + FabricLang.get("invalidMinimum")
                                            .replace("<number>", "1")));
                            new QuestMobsRadiiPrompt(uuid).start();
                            return;
                        }
                        radii.add(i);
                    } catch (final NumberFormatException e) {
                        sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                ChatFormatting.RED + FabricLang.get("stageEditorInvalidItemName")
                                        .replace("<input>", s)));
                        new QuestMobsRadiiPrompt(uuid).start();
                        return;
                    }
                }
                SessionData.set(uuid, pref + Key.S_MOB_KILL_LOCATIONS_RADIUS, radii);
            }
            new QuestMobsKillListPrompt(uuid).start();
        }
    }

    public class QuestMobsLocationNamesPrompt extends FabricQuestsEditorStringPrompt {

        public QuestMobsLocationNamesPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorMobLocationNamesPrompt");
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
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                final LinkedList<String> locNames = new LinkedList<>(Arrays.asList(input.split(FabricLang.get("charSemi"))));
                SessionData.set(uuid, pref + Key.S_MOB_KILL_LOCATIONS_NAMES, locNames);
            }
            new QuestMobsKillListPrompt(uuid).start();
        }
    }

    public class QuestMobsTameListPrompt extends FabricQuestsEditorIntegerPrompt {

        public QuestMobsTameListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 4;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return FabricLang.get("stageEditorTameMobs");
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
            switch(number) {
            case 1:
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorSetMobTypes");
            case 2:
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorSetTameAmounts");
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
            switch(number) {
            case 1:
                if (SessionData.get(uuid, pref + Key.S_TAME_TYPES) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> tameTypes = (List<String>) SessionData.get(uuid, pref + Key.S_TAME_TYPES);
                    if (tameTypes != null) {
                        for (final String s : tameTypes) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA).append(s);
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (SessionData.get(uuid, pref + Key.S_TAME_AMOUNTS) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<Integer> tameAmounts = (List<Integer>) SessionData.get(uuid, pref + Key.S_TAME_AMOUNTS);
                    if (tameAmounts != null) {
                        for (final Integer i : tameAmounts) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA).append(i);
                        }
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
            final StringBuilder text = new StringBuilder(ChatFormatting.AQUA + "- " + getTitle() + " -\n");
            for (int i = 1; i <= size; i++) {
                text.append(getNumberColor(i)).append(ChatFormatting.BOLD).append(i).append(ChatFormatting.RESET)
                        .append(" - ").append(getSelectionText(i)).append(" ")
                        .append(getAdditionalText(i)).append("\n");
            }
            return sendClickableSelection(text.toString(), plugin.getQuester(uuid));
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final Number input) {
            final net.minecraft.server.level.ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            switch(input.intValue()) {
            case 1:
                new QuestMobsTameTypesPrompt(uuid).start();
                break;
            case 2:
                new QuestMobsTameAmountsPrompt(uuid).start();
                break;
            case 3:
                sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        ChatFormatting.YELLOW + FabricLang.get("stageEditorObjectiveCleared")));
                SessionData.set(uuid, pref + Key.S_TAME_TYPES, null);
                SessionData.set(uuid, pref + Key.S_TAME_AMOUNTS, null);
                new QuestMobsTameListPrompt(uuid).start();
                break;
            case 4:
                final int one;
                final int two;
                final List<String> types = (List<String>) SessionData.get(uuid, pref + Key.S_TAME_TYPES);
                final List<Integer> amounts = (List<Integer>) SessionData.get(uuid, pref + Key.S_TAME_AMOUNTS);
                if (types != null) {
                    one = types.size();
                } else {
                    one = 0;
                }
                if (amounts != null) {
                    two = amounts.size();
                } else {
                    two = 0;
                }
                if (one == two) {
                    new FabricQuestMobsPrompt(stageNum, uuid).start();
                } else {
                    sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            ChatFormatting.RED + FabricLang.get("listsNotSameSize")));
                    new QuestMobsTameListPrompt(uuid).start();
                }
                break;
            default:
                new FabricQuestMobsPrompt(stageNum, uuid).start();
                break;
            }
        }
    }

    public class QuestMobsTameTypesPrompt extends FabricQuestsEditorStringPrompt {

        public QuestMobsTameTypesPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return FabricLang.get("eventEditorMobsTitle");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorMobsPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final StringBuilder mobs = new StringBuilder(ChatFormatting.LIGHT_PURPLE + getTitle() + "\n");
            final List<EntityType<?>> mobArr = new LinkedList<>();
            BuiltInRegistries.ENTITY_TYPE.iterator().forEachRemaining(mobArr::add);
            final List<EntityType<?>> toRemove = new LinkedList<>();
            for (final EntityType<?> type : mobArr) {
                if (type.getCategory() == MobCategory.MISC || type == EntityType.PLAYER) {
                    toRemove.add(type);
                }
            }
            mobArr.removeAll(toRemove);
            mobArr.sort(Comparator.comparing(type -> BuiltInRegistries.ENTITY_TYPE.getKey(type).getPath()));
            for (int i = 0; i < mobArr.size(); i++) {
                mobs.append(ChatFormatting.AQUA).append(FabricMiscUtil.snakeCaseToUpperCamelCase(
                        BuiltInRegistries.ENTITY_TYPE.getKey(mobArr.get(i)).getPath()));
                if (i < (mobArr.size() - 1)) {
                     mobs.append(ChatFormatting.GRAY).append(", ");
                }
            }
            mobs.append("\n").append(ChatFormatting.YELLOW).append(getQueryText());
            return mobs.toString();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final net.minecraft.server.level.ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                final LinkedList<String> mobTypes = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    if (FabricMiscUtil.getProperMobType(s) != null) {
                        mobTypes.add(s);
                        SessionData.set(uuid, pref + Key.S_TAME_TYPES, mobTypes);

                        LinkedList<Integer> amounts = new LinkedList<>();
                        if (SessionData.get(uuid, pref + Key.S_TAME_AMOUNTS) != null) {
                            amounts = (LinkedList<Integer>) SessionData.get(uuid, pref + Key.S_TAME_AMOUNTS);
                        }
                        if (amounts != null) {
                            for (int i = 0; i < mobTypes.size(); i++) {
                                if (i >= amounts.size()) {
                                    amounts.add(1);
                                }
                            }
                        }
                        SessionData.set(uuid, pref + Key.S_TAME_AMOUNTS, amounts);
                    } else {
                        sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                ChatFormatting.RED + FabricLang.get("stageEditorInvalidMob")
                                        .replace("<input>", s)));
                        new QuestMobsTameTypesPrompt(uuid).start();
                        return;
                    }
                }
            }
            new QuestMobsTameListPrompt(uuid).start();
        }
    }

    public class QuestMobsTameAmountsPrompt extends FabricQuestsEditorStringPrompt {

        public QuestMobsTameAmountsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorTameAmountsPrompt");
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
            final net.minecraft.server.level.ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                final LinkedList<Integer> mobAmounts = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    try {
                        final int i = Integer.parseInt(s);
                        if (i < 1) {
                            sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                    ChatFormatting.RED + FabricLang.get("invalidMinimum")
                                            .replace("<number>", "1")));
                            new QuestMobsTameAmountsPrompt(uuid).start();
                            return;
                        }
                        mobAmounts.add(i);
                    } catch (final NumberFormatException e) {
                        sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                ChatFormatting.RED + FabricLang.get("reqNotANumber")
                                        .replace("<input>", input)));
                        new QuestMobsTameAmountsPrompt(uuid).start();
                        return;
                    }
                }
                SessionData.set(uuid, pref + Key.S_TAME_AMOUNTS, mobAmounts);
            }
            new QuestMobsTameListPrompt(uuid).start();
        }
    }

    public class QuestMobsFishPrompt extends FabricQuestsEditorStringPrompt {

        public QuestMobsFishPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorCatchFishPrompt");
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
            final net.minecraft.server.level.ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel")) && !input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i < 0) {
                        sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                ChatFormatting.RED + FabricLang.get("stageEditorPositiveAmount")));
                        new QuestMobsFishPrompt(uuid).start();
                        return;
                    } else if (i > 0) {
                        SessionData.set(uuid, pref + Key.S_FISH, i);
                    }
                } catch (final NumberFormatException e) {
                    sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            ChatFormatting.RED + FabricLang.get("reqNotANumber")
                                    .replace("<input>", input)));
                    new QuestMobsFishPrompt(uuid).start();
                    return;
                }
            } else if (input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, pref + Key.S_FISH, null);
            }
            new FabricQuestMobsPrompt(stageNum, uuid).start();
        }
    }

    public class QuestMobsCowsPrompt extends FabricQuestsEditorStringPrompt {

        public QuestMobsCowsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorMilkCowsPrompt");
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
            final net.minecraft.server.level.ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel")) && !input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i < 0) {
                        sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                ChatFormatting.RED + FabricLang.get("stageEditorPositiveAmount")));
                        new QuestMobsCowsPrompt(uuid).start();
                        return;
                    } else if (i > 0) {
                        SessionData.set(uuid, pref + Key.S_COW_MILK, i);
                    }
                } catch (final NumberFormatException e) {
                    sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            ChatFormatting.RED + FabricLang.get("reqNotANumber")
                                    .replace("<input>", input)));
                    new QuestMobsCowsPrompt(uuid).start();
                    return;
                }
            } else if (input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, pref + Key.S_COW_MILK, null);
            }
            new FabricQuestMobsPrompt(stageNum, uuid).start();
        }
    }

    public class QuestMobsShearListPrompt extends FabricQuestsEditorIntegerPrompt {

        public QuestMobsShearListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 4;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return FabricLang.get("stageEditorShearSheep");
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
            switch(number) {
            case 1:
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorSetShearColors");
            case 2:
                return ChatFormatting.YELLOW + FabricLang.get("stageEditorSetShearAmounts");
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
            switch(number) {
            case 1:
                if (SessionData.get(uuid, pref + Key.S_SHEAR_COLORS) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> shearColors = (List<String>) SessionData.get(uuid, pref + Key.S_SHEAR_COLORS);
                    if (shearColors != null) {
                        for (final String s : shearColors) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA).append(s);
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (SessionData.get(uuid, pref + Key.S_SHEAR_AMOUNTS) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<Integer> shearAmounts
                            = (List<Integer>) SessionData.get(uuid, pref + Key.S_SHEAR_AMOUNTS);
                    if (shearAmounts != null) {
                        for (final Integer i : shearAmounts) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA).append(i);
                        }
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
            final StringBuilder text = new StringBuilder(ChatFormatting.AQUA + "- " + getTitle() + " -");
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
            final net.minecraft.server.level.ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            switch(input.intValue()) {
            case 1:
                new QuestMobsShearColorsPrompt(uuid).start();
                break;
            case 2:
                new QuestMobsShearAmountsPrompt(uuid).start();
                break;
            case 3:
                sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        ChatFormatting.YELLOW + FabricLang.get("stageEditorObjectiveCleared")));
                SessionData.set(uuid, pref + Key.S_SHEAR_COLORS, null);
                SessionData.set(uuid, pref + Key.S_SHEAR_AMOUNTS, null);
                new QuestMobsShearListPrompt(uuid).start();
                break;
            case 4:
                final int one;
                final int two;
                final List<String> colors = (List<String>) SessionData.get(uuid, pref + Key.S_SHEAR_COLORS);
                final List<Integer> amounts = (List<Integer>) SessionData.get(uuid, pref + Key.S_SHEAR_AMOUNTS);
                if (colors != null) {
                    one = colors.size();
                } else {
                    one = 0;
                }
                if (amounts != null) {
                    two = amounts.size();
                } else {
                    two = 0;
                }
                if (one == two) {
                    new FabricQuestMobsPrompt(stageNum, uuid).start();
                } else {
                    sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            ChatFormatting.RED + FabricLang.get("listsNotSameSize")));
                    new QuestMobsShearListPrompt(uuid).start();
                }
                break;
            default:
                new FabricQuestMobsPrompt(stageNum, uuid).start();
                break;
            }
        }
    }

    public class QuestMobsShearColorsPrompt extends FabricQuestsEditorStringPrompt {

        public QuestMobsShearColorsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return FabricLang.get("stageEditorColors");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorShearColorsPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final StringBuilder cols = new StringBuilder(ChatFormatting.LIGHT_PURPLE + "- " + getTitle() + " - \n");
            final net.minecraft.world.item.DyeColor[] colArr = net.minecraft.world.item.DyeColor.values();
            for (int i = 0; i < colArr.length; i++) {
                if (i < (colArr.length - 1)) {
                    cols.append(FabricMiscUtil.snakeCaseToUpperCamelCase(colArr[i].name())).append(", ");
                } else {
                    cols.append(FabricMiscUtil.snakeCaseToUpperCamelCase(colArr[i].name())).append("\n");
                }
            }
            return cols.toString() + ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final net.minecraft.server.level.ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                final LinkedList<String> colors = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    if (FabricMiscUtil.getProperDyeColor(s) != null) {
                        colors.add(s);
                        SessionData.set(uuid, pref + Key.S_SHEAR_COLORS, colors);

                        LinkedList<Integer> amounts = new LinkedList<>();
                        if (SessionData.get(uuid, pref + Key.S_SHEAR_AMOUNTS) != null) {
                            amounts = (LinkedList<Integer>) SessionData.get(uuid, pref + Key.S_SHEAR_AMOUNTS);
                        }
                        if (amounts != null) {
                            for (int i = 0; i < colors.size(); i++) {
                                if (i >= amounts.size()) {
                                    amounts.add(1);
                                }
                            }
                        }
                        SessionData.set(uuid, pref + Key.S_SHEAR_AMOUNTS, amounts);
                    } else {
                        sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                ChatFormatting.RED + FabricLang.get("stageEditorInvalidDye")
                                        .replace("<input>", s)));
                        new QuestMobsShearColorsPrompt(uuid).start();
                        return;
                    }
                }
            }
            new QuestMobsShearListPrompt(uuid).start();
        }
    }

    public class QuestMobsShearAmountsPrompt extends FabricQuestsEditorStringPrompt {

        public QuestMobsShearAmountsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("stageEditorShearAmountsPrompt");
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
            final net.minecraft.server.level.ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                final LinkedList<Integer> shearAmounts = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    try {
                        final int i = Integer.parseInt(s);
                        if (i < 1) {
                            sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                    ChatFormatting.RED + FabricLang.get("invalidMinimum")
                                            .replace("<number>", "1")));
                            new QuestMobsShearAmountsPrompt(uuid).start();
                            return;
                        }
                        shearAmounts.add(i);
                    } catch (final NumberFormatException e) {
                        sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                ChatFormatting.RED + FabricLang.get("reqNotANumber")
                                        .replace("<input>", input)));
                        new QuestMobsShearAmountsPrompt(uuid).start();
                        return;
                    }
                }
                SessionData.set(uuid, pref + Key.S_SHEAR_AMOUNTS, shearAmounts);
            }
            new QuestMobsShearListPrompt(uuid).start();
        }
    }
}
