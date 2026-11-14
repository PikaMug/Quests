/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.commands.quest;

import com.mojang.brigadier.Command;
import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.commands.FabricQuestsSubCommand;
import me.pikamug.quests.player.FabricQuester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.Requirements;
import me.pikamug.quests.quests.components.Stage;
import me.pikamug.quests.util.AnsiUtil;
import me.pikamug.quests.util.FabricItemUtil;
import me.pikamug.quests.util.FabricLang;
import me.pikamug.quests.util.FabricMiscUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class FabricQuestCommandHandler {

    private final FabricQuestsPlugin plugin;

    public FabricQuestCommandHandler(final FabricQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    public int check(final CommandSourceStack source) {
        if (!source.isPlayer()) {
            reply(source,() -> Component.literal(ChatFormatting.RED + FabricLang.get("consoleError")));
            return 0;
        }
        final ServerPlayer player = Objects.requireNonNull(source.getPlayer());
        if (!FabricQuestsSubCommand.hasCommandPermission(source, "quests.quest")) {
            reply(source,() -> Component.literal(ChatFormatting.RED + FabricLang.get(player, "noPermission")));
            return 0;
        }
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        if (quester.getCurrentQuests().isEmpty()) {
            player.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get(player, "noActiveQuest")));
            return Command.SINGLE_SUCCESS;
        }
        for (final Quest quest : quester.getCurrentQuests().keySet()) {
            final Stage stage = quester.getCurrentStage(quest);
            quest.updateCompass(quester, stage);
            if (quester.getQuestProgressOrDefault(quest).getDelayStartTime() == 0
                    || quester.getStageTime(quest) < 0L) {
                final String msg = FabricLang.get(player, "questObjectivesTitle").replace("<quest>", quest.getName());
                player.sendSystemMessage(Component.literal(ChatFormatting.GOLD + msg));
                quester.showCurrentObjectives(quest, quester, false);
            } else {
                final long time = quester.getStageTime(quest);
                final String msg = ChatFormatting.YELLOW + "(" + FabricLang.get(player, "delay") + ") "
                        + ChatFormatting.RED + FabricLang.get(player, "plnTooEarly")
                        .replace("<quest>", quest.getName())
                        .replace("<time>", FabricMiscUtil.getTime(time));
                player.sendSystemMessage(Component.literal(msg));
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    public int showQuestDetails(final CommandSourceStack source, final String name) {
        if (!source.isPlayer()) {
            reply(source,() -> Component.literal(ChatFormatting.RED + FabricLang.get("consoleError")));
            return 0;
        }
        final ServerPlayer player = Objects.requireNonNull(source.getPlayer());
        if (!FabricQuestsSubCommand.hasCommandPermission(source, "quests.questinfo")) {
            reply(source,() -> Component.literal(ChatFormatting.RED + FabricLang.get(player, "noPermission")));
            return 0;
        }
        final Quest quest = plugin.getQuest(name != null ? name.toLowerCase() : null);
        if (quest == null) {
            player.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get(player, "questNotFound")
                    .replace("<input>", name != null ? name : "")));
            return 0;
        }
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        player.sendSystemMessage(Component.literal(ChatFormatting.GOLD + "- " + quest.getName() + " -"));
        player.sendSystemMessage(Component.literal(" "));
        if (quest.getNpcStart() != null) {
            player.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get(player, "speakTo")
                    .replace("<npc>", quest.getNpcStartName())));
        } else {
            player.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + quest.getDescription()));
        }
        player.sendSystemMessage(Component.literal(" "));
        if (plugin.getConfigSettings().canShowQuestReqs()) {
            final Requirements reqs = quest.getRequirements();
            if (reqs != null && reqs.hasRequirement()) {
                player.sendSystemMessage(Component.literal(ChatFormatting.GOLD + FabricLang.get(player, "requirements")));
                for (final String perm : reqs.getPermissions()) {
                    if (perm == null) continue;
                    if (plugin.getDependencies().hasPermission(player.getUUID(), perm)) {
                        player.sendSystemMessage(Component.literal(ChatFormatting.GREEN
                                + FabricLang.get(player, "permissionDisplay") + " " + perm));
                    } else {
                        player.sendSystemMessage(Component.literal(ChatFormatting.RED
                                + FabricLang.get(player, "permissionDisplay") + " " + perm));
                    }
                }
                if (reqs.getQuestPoints() != 0) {
                    if (quester.getQuestPoints() >= reqs.getQuestPoints()) {
                        player.sendSystemMessage(Component.literal(ChatFormatting.GRAY + "- " + ChatFormatting.GREEN
                                + reqs.getQuestPoints() + " " + FabricLang.get(player, "questPoints")));
                    } else {
                        player.sendSystemMessage(Component.literal(ChatFormatting.GRAY + "- " + ChatFormatting.RED
                                + reqs.getQuestPoints() + " " + FabricLang.get(player, "questPoints")));
                    }
                }
                for (final Object item : reqs.getItems()) {
                    if (item instanceof ItemStack) {
                        ItemStack is = (ItemStack) item;
                        if (hasItem(player, is)) {
                            player.sendSystemMessage(Component.literal(ChatFormatting.GRAY + "- "
                                    + ChatFormatting.GREEN + FabricItemUtil.getDisplayString(is)));
                        } else {
                            player.sendSystemMessage(Component.literal(ChatFormatting.GRAY + "- "
                                    + ChatFormatting.RED + FabricItemUtil.getDisplayString(is)));
                        }
                    }
                }
                for (final Quest completed : quester.getCompletedQuests()) {
                    if (reqs.getNeededQuestIds().contains(completed.getId())) {
                        player.sendSystemMessage(Component.literal(ChatFormatting.GRAY + "- " + ChatFormatting.GREEN
                                + FabricLang.get(player, "complete") + " " + ChatFormatting.ITALIC + completed.getName()));
                    } else {
                        player.sendSystemMessage(Component.literal(ChatFormatting.GRAY + "- " + ChatFormatting.RED
                                + FabricLang.get(player, "complete") + " " + ChatFormatting.ITALIC + completed.getName()));
                    }
                }
                final Map<String, String> completedMap = new LinkedHashMap<>();
                for (final Quest completed : quester.getCompletedQuests()) {
                    completedMap.put(completed.getId(), completed.getName());
                }
                for (final String questId : reqs.getBlockQuestIds()) {
                    if (completedMap.containsKey(questId)) {
                        final String msg = FabricLang.get(player, "haveCompleted")
                                .replace("<quest>", completedMap.get(questId));
                        player.sendSystemMessage(Component.literal(ChatFormatting.GRAY + "- "
                                + ChatFormatting.RED + msg));
                    } else {
                        final Quest blocked = plugin.getQuestById(questId);
                        final String msg = FabricLang.get(player, "cannotComplete")
                                .replace("<quest>", blocked != null ? blocked.getName() : questId);
                        player.sendSystemMessage(Component.literal(ChatFormatting.GRAY + "- "
                                + ChatFormatting.GREEN + msg));
                    }
                }
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private boolean hasItem(final ServerPlayer player, final ItemStack goal) {
        int need = Math.max(1, goal.getCount());
        for (final ItemStack is : player.getInventory().getNonEquipmentItems()) {
            if (!is.isEmpty() && FabricItemUtil.matches(is, goal)) {
                need -= is.getCount();
                if (need <= 0) return true;
            }
        }
        return false;
    }

    /**
     * Sends command feedback. Players receive it through the normal success path; console feedback is
     * logged directly (with legacy color codes converted to ANSI) so that only Quests output is colorized.
     */
    private void reply(final CommandSourceStack source, final Supplier<Component> message) {
        if (source.isPlayer()) {
            source.sendSuccess(message, false);
        } else {
            plugin.getPluginLogger().info("{}", AnsiUtil.toAnsi(message.get().getString()));
        }
    }
}