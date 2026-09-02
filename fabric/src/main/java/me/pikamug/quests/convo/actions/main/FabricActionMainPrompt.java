/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.convo.actions.main;

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.actions.Action;
import me.pikamug.quests.convo.actions.FabricActionsEditorIntegerPrompt;
import me.pikamug.quests.convo.actions.FabricActionsEditorStringPrompt;
import me.pikamug.quests.convo.actions.tasks.FabricActionEffectPrompt;
import me.pikamug.quests.convo.actions.tasks.FabricActionPlayerPrompt;
import me.pikamug.quests.convo.actions.tasks.FabricActionTimerPrompt;
import me.pikamug.quests.convo.actions.tasks.FabricActionWeatherPrompt;
import me.pikamug.quests.entity.FabricQuestMob;
import me.pikamug.quests.entity.QuestMob;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.Stage;
import me.pikamug.quests.util.FabricLang;
import me.pikamug.quests.util.FabricMiscUtil;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.SessionData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FabricActionMainPrompt extends FabricActionsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final FabricQuestsPlugin plugin;

    public static final ConcurrentHashMap<UUID, BlockPos> selectedMobLocations = new ConcurrentHashMap<>();

    public FabricActionMainPrompt(final @NotNull UUID uuid) {
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
        return FabricLang.get("event") + ": " + SessionData.get(uuid, Key.A_NAME);
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
        case 8:
            return ChatFormatting.BLUE;
        case 7:
            return ChatFormatting.GRAY;
        case 9:
            return ChatFormatting.GREEN;
        case 10:
            return ChatFormatting.RED;
        default:
            return null;
        }
    }

    @Override
    public String getSelectionText(final int number) {
        switch (number) {
        case 1:
            return ChatFormatting.YELLOW + FabricLang.get("eventEditorSetName");
        case 2:
            return ChatFormatting.GOLD + FabricLang.get("eventEditorPlayer");
        case 3:
            return ChatFormatting.GOLD + FabricLang.get("eventEditorTimer");
        case 4:
            return ChatFormatting.GOLD + FabricLang.get("eventEditorEffect");
        case 5:
            return ChatFormatting.GOLD + FabricLang.get("eventEditorWeather");
        case 6:
            return ChatFormatting.YELLOW + FabricLang.get("eventEditorSetMobSpawns");
        case 7:
            return ChatFormatting.GRAY + FabricLang.get("stageEditorDenizenScript");
        case 8:
            return ChatFormatting.YELLOW + FabricLang.get("eventEditorFailQuest");
        case 9:
            return ChatFormatting.GREEN + FabricLang.get("save");
        case 10:
            return ChatFormatting.RED + FabricLang.get("exit");
        default:
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getAdditionalText(final int number) {
        switch (number) {
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        case 9:
        case 10:
            return "";
        case 6:
            if (SessionData.get(uuid, Key.A_MOBS) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final LinkedList<QuestMob> mobs = (LinkedList<QuestMob>) SessionData.get(uuid, Key.A_MOBS);
                final StringBuilder text = new StringBuilder();
                if (mobs != null) {
                    for (final QuestMob mob : mobs) {
                        final FabricQuestMob qm = (FabricQuestMob) mob;
                        text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA)
                                .append(qm.getEntityType() != null ? FabricMiscUtil.getEntityName(qm.getEntityType()) : "Unknown")
                                .append((qm.getName() != null) ? " (" + qm.getName() + ")" : "").append(ChatFormatting.GRAY)
                                .append(" x ").append(ChatFormatting.DARK_AQUA).append(qm.getSpawnAmounts())
                                .append("\n").append(ChatFormatting.GRAY).append("        \u2515 ").append(ChatFormatting.GREEN)
                                .append(qm.getLocation() != null ? qm.getLocation().getX() + " " + qm.getLocation().getY() + " " + qm.getLocation().getZ() : "Unknown");
                    }
                }
                return text.toString();
            }
        case 7:
            return ChatFormatting.GRAY + "(" + FabricLang.get("notInstalled") + ")";
        case 8:
            if (SessionData.get(uuid, Key.A_FAIL_QUEST) == null) {
                return ChatFormatting.GRAY + "(" + ChatFormatting.RED + FabricLang.get("false") + ChatFormatting.GRAY + ")";
            } else {
                final Boolean failOpt = (Boolean) SessionData.get(uuid, Key.A_FAIL_QUEST);
                return ChatFormatting.GRAY + "(" + (Boolean.TRUE.equals(failOpt) ? ChatFormatting.GREEN + FabricLang.get("true")
                        : ChatFormatting.RED + FabricLang.get("false")) + ChatFormatting.GRAY + ")";
            }
        default:
            return null;
        }
    }

    @Override
    public @NotNull String getPromptText() {
        final StringBuilder text = new StringBuilder(ChatFormatting.GOLD + "- " + getTitle().replaceFirst(": ", ": "
                + ChatFormatting.AQUA) + ChatFormatting.GOLD + " -");
        for (int i = 1; i <= size; i++) {
            text.append("\n").append(getNumberColor(i)).append(ChatFormatting.BOLD).append(i)
                    .append(ChatFormatting.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                    .append(getAdditionalText(i));
        }
        return sendClickableSelection(text.toString(), plugin.getQuester(uuid));
    }

    @Override
    public void acceptInput(final Number input) {
        switch (input.intValue()) {
        case 1:
            new ActionNamePrompt(uuid).start();
            break;
        case 2:
            new FabricActionPlayerPrompt(uuid).start();
            break;
        case 3:
            new FabricActionTimerPrompt(uuid).start();
            break;
        case 4:
            new FabricActionEffectPrompt(uuid).start();
            break;
        case 5:
            new FabricActionWeatherPrompt(uuid).start();
            break;
        case 6:
            new ActionMobListPrompt(uuid).start();
            break;
        case 7:
            new FabricActionMainPrompt(uuid).start();
            break;
        case 8:
            final Boolean b = (Boolean) SessionData.get(uuid, Key.A_FAIL_QUEST);
            if (Boolean.TRUE.equals(b)) {
                SessionData.set(uuid, Key.A_FAIL_QUEST, false);
            } else {
                SessionData.set(uuid, Key.A_FAIL_QUEST, true);
            }
            new FabricActionMainPrompt(uuid).start();
            break;
        case 9:
            if (SessionData.get(uuid, Key.A_OLD_ACTION) != null) {
                new ActionSavePrompt(uuid, (String) SessionData.get(uuid, Key.A_OLD_ACTION)).start();
            } else {
                new ActionSavePrompt(uuid, null).start();
            }
            break;
        case 10:
            new ActionExitPrompt(uuid).start();
            break;
        default:
            new FabricActionMainPrompt(uuid).start();
            break;
        }
    }

    public class ActionNamePrompt extends FabricActionsEditorStringPrompt {

        public ActionNamePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("eventEditorEnterEventName");
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
                for (final Action a : plugin.getLoadedActions()) {
                    if (a.getName().equalsIgnoreCase(input)) {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("eventEditorExists")));
                        new ActionNamePrompt(uuid).start();
                        return;
                    }
                }
                final List<String> actionNames = plugin.getActionFactory().getNamesOfActionsBeingEdited();
                if (actionNames.contains(input)) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("questEditorBeingEdited")));
                    new ActionNamePrompt(uuid).start();
                    return;
                }
                if (input.contains(",")) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("questEditorInvalidQuestName")));
                    new ActionNamePrompt(uuid).start();
                    return;
                }
                actionNames.remove((String) SessionData.get(uuid, Key.A_NAME));
                SessionData.set(uuid, Key.A_NAME, input);
                actionNames.add(input);
                plugin.getActionFactory().setNamesOfActionsBeingEdited(actionNames);
            }
            new FabricActionMainPrompt(uuid).start();
        }
    }

    public class ActionMobListPrompt extends FabricActionsEditorIntegerPrompt {

        public ActionMobListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 3;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return FabricLang.get("eventEditorMobSpawnsTitle");
        }

        @Override
        public ChatFormatting getNumberColor(final int number) {
            switch (number) {
            case 1:
                return ChatFormatting.BLUE;
            case 2:
                return ChatFormatting.RED;
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
                return ChatFormatting.YELLOW + FabricLang.get("eventEditorAddMobTypes");
            case 2:
                return ChatFormatting.RED + FabricLang.get("clear");
            case 3:
                return ChatFormatting.GREEN + FabricLang.get("done");
            default:
                return null;
            }
        }

        @Override
        public String getAdditionalText(final int number) {
            switch (number) {
            case 1:
                if (SessionData.get(uuid, Key.A_MOBS) != null) {
                    @SuppressWarnings("unchecked")
                    final LinkedList<QuestMob> mobs = (LinkedList<QuestMob>) SessionData.get(uuid, Key.A_MOBS);
                    final StringBuilder text = new StringBuilder();
                    if (mobs != null) {
                        for (final QuestMob mob : mobs) {
                            final FabricQuestMob qm = (FabricQuestMob) mob;
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.AQUA)
                                    .append(qm.getEntityType() != null ? FabricMiscUtil.getEntityName(qm.getEntityType()) : "Unknown")
                                    .append((qm.getName() != null) ? " (" + qm.getName() + ")" : "")
                                    .append(" x ").append(qm.getSpawnAmounts()).append("\n").append(ChatFormatting.GRAY)
                                    .append("        \u2515 ").append(ChatFormatting.GREEN)
                                    .append(qm.getLocation() != null ? qm.getLocation().getX() + " " + qm.getLocation().getY() + " " + qm.getLocation().getZ() : "Unknown");
                        }
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
                new ActionMobPrompt(uuid, null).start();
                break;
            case 2:
                sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("eventEditorMobSpawnsCleared")));
                SessionData.set(uuid, Key.A_MOBS, null);
                new ActionMobListPrompt(uuid).start();
                break;
            case 3:
                new FabricActionMainPrompt(uuid).start();
                break;
            default:
                new ActionMobListPrompt(uuid).start();
                break;
            }
        }
    }

    public class ActionMobPrompt extends FabricActionsEditorIntegerPrompt {

        private FabricQuestMob questMob;

        public ActionMobPrompt(final @NotNull UUID uuid, final FabricQuestMob questMob) {
            super(uuid);
            this.questMob = questMob;
        }

        private final int size = 7;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return FabricLang.get("eventEditorAddMobTypesTitle");
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
                return ChatFormatting.RED;
            case 7:
                return ChatFormatting.GREEN;
            default:
                return null;
            }
        }

        @Override
        public String getSelectionText(final int number) {
            switch (number) {
            case 1:
                return ChatFormatting.YELLOW + FabricLang.get("eventEditorSetMobName");
            case 2:
                return ChatFormatting.YELLOW + FabricLang.get("eventEditorSetMobType");
            case 3:
                return ChatFormatting.YELLOW + FabricLang.get("eventEditorSetMobSpawnAmount");
            case 4:
                return ChatFormatting.YELLOW + FabricLang.get("eventEditorAddSpawnLocation");
            case 5:
                return ChatFormatting.YELLOW + FabricLang.get("eventEditorSetEquipment");
            case 6:
                return ChatFormatting.RED + FabricLang.get("cancel");
            case 7:
                return ChatFormatting.GREEN + FabricLang.get("done");
            default:
                return null;
            }
        }

        @Override
        public String getAdditionalText(final int number) {
            switch (number) {
            case 1:
                return ChatFormatting.GRAY + "(" + (questMob.getName() == null ? FabricLang.get("noneSet") : ChatFormatting.AQUA
                        + questMob.getName()) + ChatFormatting.GRAY + ")";
            case 2:
                return ChatFormatting.GRAY + "(" + (questMob.getEntityType() == null ? FabricLang.get("noneSet") : ChatFormatting.AQUA
                        + FabricMiscUtil.getEntityName(questMob.getEntityType())) + ChatFormatting.GRAY + ")";
            case 3:
                return ChatFormatting.GRAY + "(" + (questMob.getSpawnAmounts() == null ? ChatFormatting.GRAY
                        + FabricLang.get("noneSet") : ChatFormatting.AQUA + "" + questMob.getSpawnAmounts()) + ChatFormatting.GRAY
                        + ")";
            case 4:
                return ChatFormatting.GRAY + "(" + (questMob.getLocation() == null ? ChatFormatting.GRAY
                        + FabricLang.get("noneSet") : ChatFormatting.AQUA + "" + questMob.getLocation().getX() + " "
                        + questMob.getLocation().getY() + " " + questMob.getLocation().getZ()) + ChatFormatting.GRAY + ")";
            case 5:
                return ChatFormatting.GRAY + "(" + ChatFormatting.GRAY + FabricLang.get("noneSet") + ChatFormatting.GRAY + ")";
            case 6:
            case 7:
                return "";
            default:
                return null;
            }
        }

        @Override
        public @NotNull String getPromptText() {
            if (questMob == null) {
                questMob = new FabricQuestMob();
            }

            final StringBuilder text = new StringBuilder(ChatFormatting.GOLD + getTitle());
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatFormatting.BOLD).append(i)
                        .append(ChatFormatting.RESET).append(" - ").append(getSelectionText(i))
                        .append(ChatFormatting.GRAY).append(" ").append(getAdditionalText(i));
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
                new ActionMobNamePrompt(uuid, questMob).start();
                break;
            case 2:
                new ActionMobTypePrompt(uuid, questMob).start();
                break;
            case 3:
                new ActionMobAmountPrompt(uuid, questMob).start();
                break;
            case 4:
                if (sender instanceof ServerPlayer) {
                    final MinecraftServer server = plugin.getServer();
                    if (server == null) {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("unknownError")));
                        new ActionMobLocationPrompt(uuid, questMob).start();
                        break;
                    }
                    selectedMobLocations.put(uuid, BlockPos.ZERO);
                    new ActionMobLocationPrompt(uuid, questMob).start();
                } else {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("consoleError")));
                    new FabricActionMainPrompt(uuid).start();
                }
                break;
            case 5:
                sender.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("consoleError")));
                new FabricActionMainPrompt(uuid).start();
                break;
            case 6:
                new ActionMobListPrompt(uuid).start();
                break;
            case 7:
                if (questMob.getEntityType() == null) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED
                            + FabricLang.get("eventEditorMustSetMobTypesFirst")));
                    new ActionMobPrompt(uuid, questMob).start();
                } else if (questMob.getLocation() == null) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED
                            + FabricLang.get("eventEditorMustSetMobLocationFirst")));
                    new ActionMobPrompt(uuid, questMob).start();
                } else if (questMob.getSpawnAmounts() == null) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED
                            + FabricLang.get("eventEditorMustSetMobAmountsFirst")));
                    new ActionMobPrompt(uuid, questMob).start();
                }
                final LinkedList<QuestMob> list = SessionData.get(uuid, Key.A_MOBS) == null ? new LinkedList<>()
                        : (LinkedList<QuestMob>) SessionData.get(uuid, Key.A_MOBS);
                if (list != null) {
                    list.add(questMob);
                    SessionData.set(uuid, Key.A_MOBS, list);
                }
                new ActionMobListPrompt(uuid).start();
                break;
            default:
                new ActionMobPrompt(uuid, questMob).start();
                break;
            }
        }
    }

    public class ActionMobNamePrompt extends FabricActionsEditorStringPrompt {

        private final FabricQuestMob questMob;

        public ActionMobNamePrompt(final @NotNull UUID uuid, final FabricQuestMob questMob) {
            super(uuid);
            this.questMob = questMob;
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("eventEditorSetMobNamePrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(String input) {
            if (input == null) {
                return;
            }
            if (input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                new ActionMobPrompt(uuid, questMob).start();
            } else if (input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                questMob.setName(null);
                new ActionMobPrompt(uuid, questMob).start();
            } else {
                questMob.setName(input);
                new ActionMobPrompt(uuid, questMob).start();
            }
        }
    }

    public class ActionMobTypePrompt extends FabricActionsEditorStringPrompt {

        private final FabricQuestMob questMob;

        public ActionMobTypePrompt(final @NotNull UUID uuid, final FabricQuestMob questMob) {
            super(uuid);
            this.questMob = questMob;
        }

        @Override
        public String getTitle() {
            return FabricLang.get("eventEditorMobsTitle");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("eventEditorSetMobTypesPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final StringBuilder mobs = new StringBuilder(ChatFormatting.LIGHT_PURPLE + getTitle() + "\n");
            final LinkedList<EntityType<?>> mobList = new LinkedList<>();
            net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.stream()
                    .filter(et -> et != EntityType.PLAYER)
                    .sorted(Comparator.comparing(et -> et.getDescription().getString()))
                    .forEach(mobList::add);
            for (int i = 0; i < mobList.size(); i++) {
                mobs.append(ChatFormatting.AQUA).append(FabricMiscUtil.getEntityName(mobList.get(i)));
                if (i < (mobList.size() - 1)) {
                    mobs.append(ChatFormatting.GRAY).append(", ");
                }
            }
            mobs.append("\n").append(ChatFormatting.YELLOW).append(getQueryText());
            return mobs.toString();
        }

        @Override
        public void acceptInput(final String input) {
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (input == null) {
                return;
            }
            if (sender == null) {
                return;
            }
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                final EntityType<?> type = FabricMiscUtil.getEntityType(input);
                if (type != null) {
                    questMob.setEntityType(type);
                } else {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("stageEditorInvalidMob")
                            .replace("<input>", input)));
                    new ActionMobTypePrompt(uuid, questMob).start();
                    return;
                }
            }
            new ActionMobPrompt(uuid, questMob).start();
        }
    }

    public class ActionMobAmountPrompt extends FabricActionsEditorStringPrompt {

        private final FabricQuestMob questMob;

        public ActionMobAmountPrompt(final @NotNull UUID uuid, final FabricQuestMob questMob) {
            super(uuid);
            this.questMob = questMob;
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("eventEditorSetMobAmountsPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (input == null) {
                return;
            }
            if (sender == null) {
                return;
            }
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i < 1) {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("invalidMinimum")
                                .replace("<number>", "1")));
                        new ActionMobAmountPrompt(uuid, questMob).start();
                        return;
                    }
                    questMob.setSpawnAmounts(i);
                    new ActionMobPrompt(uuid, questMob).start();
                    return;
                } catch (final NumberFormatException e) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("reqNotANumber")
                            .replace("<input>", input)));
                    new ActionMobAmountPrompt(uuid, questMob).start();
                    return;
                }
            }
            new ActionMobPrompt(uuid, questMob).start();
        }
    }

    public class ActionMobLocationPrompt extends FabricActionsEditorStringPrompt {

        private final FabricQuestMob questMob;

        public ActionMobLocationPrompt(final @NotNull UUID uuid, final FabricQuestMob questMob) {
            super(uuid);
            this.questMob = questMob;
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("eventEditorSetMobLocationPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (input == null) {
                return;
            }
            if (sender == null) {
                return;
            }
            if (input.equalsIgnoreCase(FabricLang.get("cmdAdd"))) {
                final BlockPos block = selectedMobLocations.get(uuid);
                if (block != null) {
                    questMob.setLocation(block);
                    selectedMobLocations.remove(uuid);
                } else {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("eventEditorSelectBlockFirst")));
                    new ActionMobLocationPrompt(uuid, questMob).start();
                    return;
                }
                new ActionMobPrompt(uuid, questMob).start();
            } else if (input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                selectedMobLocations.remove(uuid);
                new ActionMobPrompt(uuid, questMob).start();
            } else {
                new ActionMobLocationPrompt(uuid, questMob).start();
            }
        }
    }

    public class ActionSavePrompt extends FabricActionsEditorStringPrompt {

        String modName = null;
        LinkedList<String> modified = new LinkedList<>();

        public ActionSavePrompt(final @NotNull UUID uuid, final String modifiedName) {
            super(uuid);
            if (modifiedName != null) {
                modName = modifiedName;
                for (final Quest q : plugin.getLoadedQuests()) {
                    for (final Stage s : q.getStages()) {
                        if (s.getFinishAction() != null && s.getFinishAction().getName() != null) {
                            if (s.getFinishAction().getName().equalsIgnoreCase(modifiedName)) {
                                modified.add(q.getName());
                                break;
                            }
                        }
                    }
                }
            }
        }

        private final int size = 2;

        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return null;
        }

        @SuppressWarnings("unused")
        public ChatFormatting getNumberColor(final int number) {
            switch (number) {
            case 1:
                return ChatFormatting.GREEN;
            case 2:
                return ChatFormatting.RED;
            default:
                return null;
            }
        }

        @SuppressWarnings("unused")
        public String getSelectionText(final int number) {
            switch (number) {
            case 1:
                return ChatFormatting.GREEN + FabricLang.get("yesWord");
            case 2:
                return ChatFormatting.RED + FabricLang.get("noWord");
            default:
                return null;
            }
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("questEditorSave");
        }

        @Override
        public @NotNull String getPromptText() {
            final StringBuilder text = new StringBuilder(ChatFormatting.YELLOW + getQueryText());
            if (!modified.isEmpty()) {
                text.append("\n").append(ChatFormatting.RED).append(" ").append(FabricLang.get("eventEditorModifiedNote"));
                for (final String s : modified) {
                    text.append("\n").append(ChatFormatting.GRAY).append("    - ").append(ChatFormatting.DARK_RED).append(s);
                }
                text.append("\n").append(ChatFormatting.RED).append(" ").append(FabricLang.get("eventEditorForcedToQuit"));
            }
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatFormatting.BOLD).append(i)
                        .append(ChatFormatting.RESET).append(" - ").append(getSelectionText(i));
            }
            return sendClickableSelection(text.toString(), plugin.getQuester(uuid));
        }

        @Override
        public void acceptInput(final String input) {
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (input == null) {
                return;
            }
            if (sender == null) {
                return;
            }
            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(FabricLang.get("yesWord"))) {
                if (plugin.hasLimitedAccess(uuid) && !plugin.getConfigSettings().canTrialSave()) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("modeDeny")
                            .replace("<mode>", FabricLang.get("trialMode"))));
                    new FabricActionMainPrompt(uuid).start();
                }
                plugin.getActionFactory().saveAction(uuid);
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(FabricLang.get("noWord"))) {
                new FabricActionMainPrompt(uuid).start();
            } else {
                new ActionSavePrompt(uuid, modName).start();
            }
        }
    }

    public class ActionExitPrompt extends FabricActionsEditorStringPrompt {

        public ActionExitPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 2;

        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return null;
        }

        @SuppressWarnings("unused")
        public ChatFormatting getNumberColor(final int number) {
            switch (number) {
            case 1:
                return ChatFormatting.GREEN;
            case 2:
                return ChatFormatting.RED;
            default:
                return null;
            }
        }

        @SuppressWarnings("unused")
        public String getSelectionText(final int number) {
            switch (number) {
            case 1:
                return ChatFormatting.GREEN + FabricLang.get("yesWord");
            case 2:
                return ChatFormatting.RED + FabricLang.get("noWord");
            default:
                return null;
            }
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("confirmDelete");
        }

        @Override
        public @NotNull String getPromptText() {
            final StringBuilder text = new StringBuilder(ChatFormatting.YELLOW + getQueryText());
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatFormatting.BOLD).append(i)
                        .append(ChatFormatting.RESET).append(" - ").append(getSelectionText(i));
            }
            return sendClickableSelection(text.toString(), plugin.getQuester(uuid));
        }

        @Override
        public void acceptInput(final String input) {
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (input == null) {
                return;
            }
            if (sender == null) {
                return;
            }
            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(FabricLang.get("yesWord"))) {
                sender.sendSystemMessage(Component.literal(ChatFormatting.BOLD + "" + ChatFormatting.YELLOW + FabricLang.get("exited")));
                plugin.getActionFactory().clearData(uuid);
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(FabricLang.get("noWord"))) {
                new FabricActionMainPrompt(uuid).start();
            } else {
                new ActionExitPrompt(uuid).start();
            }
        }
    }
}
