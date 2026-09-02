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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class FabricConditionEntityPrompt extends FabricConditionsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final FabricQuestsPlugin plugin;

    public FabricConditionEntityPrompt(final @NotNull UUID uuid) {
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
        return FabricLang.get("conditionEditorEntity");
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
        switch(number) {
        case 1:
            return ChatFormatting.YELLOW + FabricLang.get("conditionEditorRideEntity");
        case 2:
            return ChatFormatting.YELLOW + FabricLang.get("conditionEditorRideNPC");
        case 3:
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
            if (SessionData.get(uuid, Key.C_WHILE_RIDING_ENTITY) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final List<String> whileRidingEntity = (List<String>) SessionData.get(uuid, Key.C_WHILE_RIDING_ENTITY);
                if (whileRidingEntity != null) {
                    for (final String s: whileRidingEntity) {
                        final EntityType<?> type = FabricMiscUtil.getEntityType(s);
                        text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.BLUE)
                                .append(type != null ? FabricMiscUtil.getEntityName(type) : s);
                    }
                }
                return text.toString();
            }
        case 2:
            if (plugin.getDependencies().hasAnyNpcDependencies()) {
                if (SessionData.get(uuid, Key.C_WHILE_RIDING_NPC) == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<UUID> whileRidingNpc = (List<UUID>) SessionData.get(uuid, Key.C_WHILE_RIDING_NPC);
                    if (whileRidingNpc != null) {
                        for (final UUID u : whileRidingNpc) {
                            text.append("\n").append(ChatFormatting.GRAY).append("     - ").append(ChatFormatting.BLUE)
                                    .append(plugin.getDependencies().getNpcName(u));
                        }
                    }
                    return text.toString();
                }
            } else {
                return ChatFormatting.GRAY + "(" + FabricLang.get("notInstalled") + ")";
            }
        case 3:
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
            new ConditionEntitiesPrompt(uuid).start();
            break;
        case 2:
            new ConditionNpcsPrompt(uuid).start();
            break;
        case 3:
            try {
                new FabricConditionMainPrompt(uuid).start();
            } catch (final Exception e) {
                sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("itemCreateCriticalError")));
                return;
            }
            break;
        default:
            new FabricConditionEntityPrompt(uuid).start();
            break;
        }
    }

    public class ConditionEntitiesPrompt extends FabricConditionsEditorStringPrompt {

        public ConditionEntitiesPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return FabricLang.get("conditionEditorEntitiesTitle");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("conditionEditorEntitiesPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final StringBuilder mobs = new StringBuilder(ChatFormatting.LIGHT_PURPLE + getTitle() + "\n");
            final LinkedList<EntityType<?>> mobList = new LinkedList<>();
            BuiltInRegistries.ENTITY_TYPE.stream()
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
            if (input == null) {
                return;
            }
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                final LinkedList<String> mobTypes = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    final EntityType<?> type = FabricMiscUtil.getEntityType(s);
                    if (type != null) {
                        mobTypes.add(s);
                        SessionData.set(uuid, Key.C_WHILE_RIDING_ENTITY, mobTypes);
                    } else {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("stageEditorInvalidMob")
                                .replace("<input>", s)));
                        new ConditionEntitiesPrompt(uuid).start();
                        break;
                    }
                }
            }
            new FabricConditionEntityPrompt(uuid).start();
        }
    }

    public class ConditionNpcsPrompt extends FabricConditionsEditorStringPrompt {

        public ConditionNpcsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return FabricLang.get("conditionEditorNpcsTitle");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("enterNpcUniqueIds");
        }

        @Override
        public @NotNull String getPromptText() {
            if (FabricMiscUtil.getPlayer(uuid, plugin) instanceof ServerPlayer) {
                final java.util.Collection<UUID> selectingNpcs = plugin.getQuestFactory().getSelectingNpcs();
                selectingNpcs.add(uuid);
                plugin.getQuestFactory().setSelectingNpcs(selectingNpcs);
                return ChatFormatting.YELLOW + FabricLang.get("questEditorClickNPCStart");
            } else {
                return ChatFormatting.YELLOW + getQueryText();
            }
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
                final LinkedList<String> npcs = SessionData.get(uuid, Key.C_WHILE_RIDING_NPC) != null
                        ? (LinkedList<String>) SessionData.get(uuid, Key.C_WHILE_RIDING_NPC) : new LinkedList<>();
                for (final String s : input.split(" ")) {
                    try {
                        final UUID npcUuid = UUID.fromString(s);
                        if (npcs != null && plugin.getDependencies().isNpc(npcUuid)) {
                            npcs.add(npcUuid.toString());
                        } else {
                            sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("stageEditorInvalidNPC")
                                    .replace("<input>", s)));
                            new ConditionNpcsPrompt(uuid).start();
                            break;
                        }
                    } catch (final IllegalArgumentException e) {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("stageEditorNotListOfUniqueIds")
                                .replace("<data>", input)));
                        new ConditionNpcsPrompt(uuid).start();
                        break;
                    }
                }
                SessionData.set(uuid, Key.C_WHILE_RIDING_NPC, npcs);
            }
            if (sender instanceof ServerPlayer) {
                final java.util.Collection<UUID> selectingNpcs = plugin.getQuestFactory().getSelectingNpcs();
                selectingNpcs.remove(uuid);
                plugin.getQuestFactory().setSelectingNpcs(selectingNpcs);
            }
            new FabricConditionEntityPrompt(uuid).start();
        }
    }
}
