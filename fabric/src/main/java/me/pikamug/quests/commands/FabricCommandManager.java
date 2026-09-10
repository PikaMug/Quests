/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.QuestsEvents;
import me.pikamug.quests.convo.actions.menu.FabricActionMenuPrompt;
import me.pikamug.quests.convo.conditions.menu.FabricConditionMenuPrompt;
import me.pikamug.quests.convo.quests.menu.FabricQuestMenuPrompt;
import me.pikamug.quests.item.FabricQuestJournal;
import me.pikamug.quests.player.FabricQuester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.util.FabricLang;
import me.pikamug.quests.util.SessionData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.browsit.conversations.api.Conversations;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FabricCommandManager {

    private final FabricQuestsPlugin plugin;

    public FabricCommandManager(FabricQuestsPlugin plugin) {
        this.plugin = plugin;
        register();
    }

    private void register() {
        QuestsEvents.registerCommandRegister(dispatcher -> {
            // /quest - Show current quest objectives
            dispatcher.register(Commands.literal("quest")
                    .requires(Commands.hasPermission(Commands.LEVEL_ALL))
                    .executes(ctx -> {
                        if (ctx.getSource().isPlayer()) {
                            return handleQuest(ctx.getSource().getPlayer());
                        }
                        ctx.getSource().sendSuccess(() -> Component.literal("Use /quest in-game"), false);
                        return Command.SINGLE_SUCCESS;
                    })
            );

            // /quests - Quest management
            final LiteralCommandNode<CommandSourceStack> questsNode = dispatcher.register(
                    Commands.literal("quests")
                            .requires(Commands.hasPermission(Commands.LEVEL_ALL))
                            .then(Commands.literal("list")
                                    .executes(ctx -> handleQuestsList(ctx.getSource(), 1))
                                    .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                            .executes(ctx -> handleQuestsList(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "page"))))
                            )
                            .then(Commands.literal("take")
                                    .then(Commands.argument("quest", StringArgumentType.greedyString())
                                            .executes(ctx -> handleQuestsTake(ctx.getSource(), StringArgumentType.getString(ctx, "quest"))))
                            )
                            .then(Commands.literal("quit")
                                    .then(Commands.argument("quest", StringArgumentType.greedyString())
                                            .executes(ctx -> handleQuestsQuit(ctx.getSource(), StringArgumentType.getString(ctx, "quest"))))
                            )
                            .then(Commands.literal("stats")
                                    .executes(ctx -> handleQuestsStats(ctx.getSource()))
                            )
                            .then(Commands.literal("top")
                                    .executes(ctx -> handleQuestsTop(ctx.getSource(), 5))
                                    .then(Commands.argument("number", IntegerArgumentType.integer(1))
                                            .executes(ctx -> handleQuestsTop(ctx.getSource(),
                                                    IntegerArgumentType.getInteger(ctx, "number"))))
                            )
                            .then(Commands.literal("info")
                                    .executes(ctx -> handleQuestsInfo(ctx.getSource()))
                            )
                            .then(Commands.literal("journal")
                                    .executes(ctx -> handleQuestsJournal(ctx.getSource()))
                            )
                            .then(Commands.literal("choice")
                                    .then(Commands.argument("value", StringArgumentType.greedyString())
                                            .executes(ctx -> handleQuestsChoice(ctx.getSource(),
                                                    StringArgumentType.getString(ctx, "value"))))
                            )
                            .then(Commands.literal("editor")
                                    .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                    .executes(ctx -> handleQuestsEditor(ctx.getSource(), "editor"))
                            )
                            .then(Commands.literal("actions")
                                    .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                    .executes(ctx -> handleQuestsEditor(ctx.getSource(), "actions"))
                            )
                            .then(Commands.literal("conditions")
                                    .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                    .executes(ctx -> handleQuestsEditor(ctx.getSource(), "conditions"))
                            )
            );
            dispatcher.register(Commands.literal("qs").redirect(questsNode));

            // /questadmin - Admin commands
            // Build each sub-command separately to avoid deep nesting paren issues
            final var reloadSub = Commands.literal("reload")
                    .executes(ctx -> handleReload(ctx.getSource()));

            final var giveSub = Commands.literal("give")
                    .then(Commands.argument("player", StringArgumentType.word())
                            .then(Commands.argument("quest", StringArgumentType.greedyString())
                                    .executes(ctx -> handleAdminGive(ctx.getSource(),
                                            StringArgumentType.getString(ctx, "player"),
                                            StringArgumentType.getString(ctx, "quest")))));

            final var quitSub = Commands.literal("quit")
                    .then(Commands.argument("player", StringArgumentType.word())
                            .then(Commands.argument("quest", StringArgumentType.greedyString())
                                    .executes(ctx -> handleAdminQuit(ctx.getSource(),
                                            StringArgumentType.getString(ctx, "player"),
                                            StringArgumentType.getString(ctx, "quest")))));

            final var pointsSub = Commands.literal("points")
                    .then(Commands.argument("player", StringArgumentType.word())
                            .executes(ctx -> handleAdminPoints(ctx.getSource(),
                                    StringArgumentType.getString(ctx, "player")))
                            .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                    .executes(ctx -> handleAdminSetPoints(ctx.getSource(),
                                            StringArgumentType.getString(ctx, "player"),
                                            IntegerArgumentType.getInteger(ctx, "amount")))));

            final var givepointsSub = Commands.literal("givepoints")
                    .then(Commands.argument("player", StringArgumentType.word())
                            .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                    .executes(ctx -> handleAdminGivePoints(ctx.getSource(),
                                            StringArgumentType.getString(ctx, "player"),
                                            IntegerArgumentType.getInteger(ctx, "amount")))));

            final var takepointsSub = Commands.literal("takepoints")
                    .then(Commands.argument("player", StringArgumentType.word())
                            .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                    .executes(ctx -> handleAdminTakePoints(ctx.getSource(),
                                            StringArgumentType.getString(ctx, "player"),
                                            IntegerArgumentType.getInteger(ctx, "amount")))));

            final var resetSub = Commands.literal("reset")
                    .then(Commands.argument("player", StringArgumentType.word())
                            .executes(ctx -> handleAdminReset(ctx.getSource(),
                                    StringArgumentType.getString(ctx, "player"))));

            final var finishSub = Commands.literal("finish")
                    .then(Commands.argument("player", StringArgumentType.word())
                            .then(Commands.argument("quest", StringArgumentType.greedyString())
                                    .executes(ctx -> handleAdminFinish(ctx.getSource(),
                                            StringArgumentType.getString(ctx, "player"),
                                            StringArgumentType.getString(ctx, "quest")))));

            final var nextstageSub = Commands.literal("nextstage")
                    .then(Commands.argument("player", StringArgumentType.word())
                            .executes(ctx -> handleAdminNextStage(ctx.getSource(),
                                    StringArgumentType.getString(ctx, "player"))));

            final var removeSub = Commands.literal("remove")
                    .then(Commands.argument("player", StringArgumentType.word())
                            .then(Commands.argument("quest", StringArgumentType.greedyString())
                                    .executes(ctx -> handleAdminRemove(ctx.getSource(),
                                            StringArgumentType.getString(ctx, "player"),
                                            StringArgumentType.getString(ctx, "quest")))));

            final var statsSub = Commands.literal("stats")
                    .then(Commands.argument("player", StringArgumentType.word())
                            .executes(ctx -> handleAdminStats(ctx.getSource(),
                                    StringArgumentType.getString(ctx, "player"))));

            final var setstageSub = Commands.literal("setstage")
                    .then(Commands.argument("player", StringArgumentType.word())
                            .then(Commands.argument("rest", StringArgumentType.greedyString())
                                    .executes(ctx -> handleAdminSetStage(ctx.getSource(),
                                            StringArgumentType.getString(ctx, "player"),
                                            StringArgumentType.getString(ctx, "rest")))));

            final LiteralCommandNode<CommandSourceStack> qaNode = dispatcher.register(
                    Commands.literal("questadmin")
                            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                            .then(reloadSub)
                            .then(giveSub)
                            .then(quitSub)
                            .then(pointsSub)
                            .then(givepointsSub)
                            .then(takepointsSub)
                            .then(resetSub)
                            .then(finishSub)
                            .then(nextstageSub)
                            .then(removeSub)
                            .then(statsSub)
                            .then(setstageSub)
            );
            dispatcher.register(Commands.literal("qa").redirect(qaNode));
        });
    }

    private int handleQuest(ServerPlayer player) {
        if (player == null) return 0;
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        if (quester.getCurrentQuests().isEmpty()) {
            player.sendSystemMessage(Component.literal(FabricLang.get("noCurrentQuest")));
            return Command.SINGLE_SUCCESS;
        }
        for (final Quest quest : quester.getCurrentQuests().keySet()) {
            quester.showCurrentObjectives(quest, quester, false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private int handleQuestsList(CommandSourceStack source, int page) {
        final Collection<Quest> quests = plugin.getLoadedQuests();
        if (quests.isEmpty()) {
            source.sendSuccess(() -> Component.literal(FabricLang.get("noQuests")), false);
            return Command.SINGLE_SUCCESS;
        }
        source.sendSuccess(() -> Component.literal("--- Available Quests ---"), false);
        for (final Quest quest : quests) {
            source.sendSuccess(() -> Component.literal("  " + quest.getName() + " - " + quest.getDescription()), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private int handleQuestsTake(CommandSourceStack source, String questName) {
        if (!source.isPlayer()) return 0;
        final ServerPlayer player = source.getPlayer();
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        final Quest quest = findQuest(questName);
        if (quest == null) {
            player.sendSystemMessage(Component.literal(FabricLang.get("questNotFound")));
            return 0;
        }
        quester.offerQuest(quest, true);
        return Command.SINGLE_SUCCESS;
    }

    private int handleQuestsQuit(CommandSourceStack source, String questName) {
        if (!source.isPlayer()) return 0;
        final ServerPlayer player = source.getPlayer();
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        final Quest quest = findQuest(questName);
        if (quest == null) {
            player.sendSystemMessage(Component.literal(FabricLang.get("questNotFound")));
            return 0;
        }
        quester.quitQuest(quest, FabricLang.get("questQuit").replace("<quest>", quest.getName()));
        return Command.SINGLE_SUCCESS;
    }

    private int handleQuestsStats(CommandSourceStack source) {
        if (!source.isPlayer()) return 0;
        final ServerPlayer player = source.getPlayer();
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        source.sendSuccess(() -> Component.literal("--- Quest Stats ---"), false);
        source.sendSuccess(() -> Component.literal("Quest Points: " + quester.getQuestPoints()), false);
        source.sendSuccess(() -> Component.literal("Active Quests: " + quester.getCurrentQuests().size()), false);
        source.sendSuccess(() -> Component.literal("Completed Quests: " + quester.getCompletedQuests().size()), false);
        return Command.SINGLE_SUCCESS;
    }

    private int handleQuestsTop(CommandSourceStack source, int topNumber) {
        final int limit = plugin.getConfigSettings().getTopLimit();
        if (topNumber < 1 || topNumber > limit) {
            source.sendSuccess(() -> Component.literal(ChatFormatting.YELLOW
                    + FabricLang.get("invalidRange").replace("<least>", "1")
                    .replace("<greatest>", String.valueOf(limit))), false);
            return Command.SINGLE_SUCCESS;
        }
        final Map<String, Integer> questPoints = new java.util.LinkedHashMap<>();
        for (final me.pikamug.quests.player.Quester quester : plugin.getOfflineQuesters()) {
            questPoints.put(quester.getLastKnownName(), quester.getQuestPoints());
        }
        final List<java.util.Map.Entry<String, Integer>> sorted = new java.util.ArrayList<>(questPoints.entrySet());
        sorted.sort(java.util.Map.Entry.comparingByValue(java.util.Collections.reverseOrder()));
        source.sendSuccess(() -> Component.literal(ChatFormatting.GOLD
                + FabricLang.get("topQuestersTitle").replace("<number>", String.valueOf(topNumber))), false);
        int printed = 0;
        for (final java.util.Map.Entry<String, Integer> entry : sorted) {
            printed++;
            final int lineNo = printed;
            source.sendSuccess(() -> Component.literal(ChatFormatting.YELLOW + String.valueOf(lineNo) + ". "
                    + entry.getKey() + " - " + ChatFormatting.DARK_PURPLE + entry.getValue()
                    + ChatFormatting.YELLOW + " " + FabricLang.get("questPoints")), false);
            if (printed >= topNumber) break;
        }
        return Command.SINGLE_SUCCESS;
    }

    private int handleQuestsInfo(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Quests Plugin v5.3.3 (Fabric)"), false);
        source.sendSuccess(() -> Component.literal("Loaded: " + plugin.getLoadedQuests().size() + " quests"), false);
        return Command.SINGLE_SUCCESS;
    }

    private int handleQuestsJournal(CommandSourceStack source) {
        if (!source.isPlayer()) return 0;
        final ServerPlayer player = source.getPlayer();
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        final net.minecraft.world.entity.player.Inventory inv = player.getInventory();
        final int index = quester.getJournalIndex();
        if (index != -1) {
            inv.setItem(index, ItemStack.EMPTY);
            player.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("journalPutAway")
                    .replace("<journal>", FabricLang.get("journalTitle"))));
        } else if (player.getMainHandItem().isEmpty()) {
            final FabricQuestJournal journal = new FabricQuestJournal(plugin, quester);
            player.getInventory().setItem(player.getInventory().getSelectedSlot(), journal.toItemStack());
            player.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("journalTaken")
                    .replace("<journal>", FabricLang.get("journalTitle"))));
        } else if (inv.getFreeSlot() != -1) {
            final FabricQuestJournal journal = new FabricQuestJournal(plugin, quester);
            inv.add(journal.toItemStack());
            player.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("journalTaken")
                    .replace("<journal>", FabricLang.get("journalTitle"))));
        } else {
            player.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("journalNoRoom")
                    .replace("<journal>", FabricLang.get("journalTitle"))));
        }
        return Command.SINGLE_SUCCESS;
    }

    private int handleQuestsChoice(CommandSourceStack source, String input) {
        if (!source.isPlayer()) return 0;
        if (input == null || input.isEmpty()) return Command.SINGLE_SUCCESS;
        final ServerPlayer player = source.getPlayer();
        final Optional<org.browsit.conversations.api.data.Conversation> conversation = Conversations
                .getConversationOf(player.getUUID());
        if (!conversation.isPresent()) {
            player.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("notConversing")));
            return Command.SINGLE_SUCCESS;
        }
        conversation.get().handleInput(input);
        return Command.SINGLE_SUCCESS;
    }

    private int handleQuestsEditor(CommandSourceStack source, String editor) {
        if (!source.isPlayer()) {
            source.sendSuccess(() -> Component.literal("Use /quests " + editor + " in-game"), false);
            return 0;
        }
        final ServerPlayer player = source.getPlayer();
        if (Conversations.getConversationOf(player.getUUID()).isPresent()) {
            player.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("duplicateEditor")));
            return Command.SINGLE_SUCCESS;
        }
        SessionData.clear(player.getUUID());
        switch (editor) {
        case "actions":
            new FabricActionMenuPrompt(player.getUUID()).start();
            break;
        case "conditions":
            new FabricConditionMenuPrompt(player.getUUID()).start();
            break;
        default:
            new FabricQuestMenuPrompt(player.getUUID()).start();
            break;
        }
        return Command.SINGLE_SUCCESS;
    }

    private int handleReload(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Reloading..."), false);
        plugin.reload();
        source.sendSuccess(() -> Component.literal("Reload complete."), false);
        return Command.SINGLE_SUCCESS;
    }

    private int handleAdminGive(CommandSourceStack source, String playerName, String questName) {
        final ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(playerName);
        if (target == null) {
            source.sendSuccess(() -> Component.literal("Player not found: " + playerName), false);
            return 0;
        }
        final Quest quest = findQuest(questName);
        if (quest == null) {
            source.sendSuccess(() -> Component.literal("Quest not found: " + questName), false);
            return 0;
        }
        plugin.getQuester(target.getUUID()).takeQuest(quest, true);
        source.sendSuccess(() -> Component.literal("Gave quest '" + quest.getName() + "' to " + playerName), false);
        return Command.SINGLE_SUCCESS;
    }

    private int handleAdminQuit(CommandSourceStack source, String playerName, String questName) {
        final ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(playerName);
        if (target == null) {
            source.sendSuccess(() -> Component.literal("Player not found: " + playerName), false);
            return 0;
        }
        final Quest quest = findQuest(questName);
        if (quest == null) {
            source.sendSuccess(() -> Component.literal("Quest not found: " + questName), false);
            return 0;
        }
        plugin.getQuester(target.getUUID()).hardQuit(quest);
        source.sendSuccess(() -> Component.literal("Removed quest '" + quest.getName() + "' from " + playerName), false);
        return Command.SINGLE_SUCCESS;
    }

    private int handleAdminPoints(CommandSourceStack source, String playerName) {
        final ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(playerName);
        if (target == null) {
            source.sendSuccess(() -> Component.literal("Player not found: " + playerName), false);
            return 0;
        }
        final int points = plugin.getQuester(target.getUUID()).getQuestPoints();
        source.sendSuccess(() -> Component.literal(playerName + " has " + points + " quest points"), false);
        return Command.SINGLE_SUCCESS;
    }

    private int handleAdminSetPoints(CommandSourceStack source, String playerName, int amount) {
        final ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(playerName);
        if (target == null) {
            source.sendSuccess(() -> Component.literal("Player not found: " + playerName), false);
            return 0;
        }
        plugin.getQuester(target.getUUID()).setQuestPoints(amount);
        source.sendSuccess(() -> Component.literal("Set " + playerName + "'s quest points to " + amount), false);
        return Command.SINGLE_SUCCESS;
    }

    private int handleAdminGivePoints(CommandSourceStack source, String playerName, int amount) {
        final ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(playerName);
        if (target == null) {
            source.sendSuccess(() -> Component.literal("Player not found: " + playerName), false);
            return 0;
        }
        final FabricQuester quester = plugin.getQuester(target.getUUID());
        quester.setQuestPoints(quester.getQuestPoints() + amount);
        source.sendSuccess(() -> Component.literal("Gave " + amount + " quest points to " + playerName), false);
        return Command.SINGLE_SUCCESS;
    }

    private int handleAdminTakePoints(CommandSourceStack source, String playerName, int amount) {
        final ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(playerName);
        if (target == null) {
            source.sendSuccess(() -> Component.literal("Player not found: " + playerName), false);
            return 0;
        }
        final FabricQuester quester = plugin.getQuester(target.getUUID());
        quester.setQuestPoints(Math.max(0, quester.getQuestPoints() - amount));
        source.sendSuccess(() -> Component.literal("Took " + amount + " quest points from " + playerName), false);
        return Command.SINGLE_SUCCESS;
    }

    private int handleAdminReset(CommandSourceStack source, String playerName) {
        final ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(playerName);
        if (target == null) {
            source.sendSuccess(() -> Component.literal("Player not found: " + playerName), false);
            return 0;
        }
        plugin.getQuester(target.getUUID()).hardClear();
        source.sendSuccess(() -> Component.literal("Reset all quest data for " + playerName), false);
        return Command.SINGLE_SUCCESS;
    }

    private int handleAdminFinish(CommandSourceStack source, String playerName, String questName) {
        final ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(playerName);
        if (target == null) {
            source.sendSuccess(() -> Component.literal("Player not found: " + playerName), false);
            return 0;
        }
        final Quest quest = findQuest(questName);
        if (quest == null) {
            source.sendSuccess(() -> Component.literal("Quest not found: " + questName), false);
            return 0;
        }
        quest.completeQuest(plugin.getQuester(target.getUUID()));
        source.sendSuccess(() -> Component.literal("Completed quest '" + quest.getName() + "' for " + playerName), false);
        return Command.SINGLE_SUCCESS;
    }

    private int handleAdminNextStage(CommandSourceStack source, String playerName) {
        final ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(playerName);
        if (target == null) {
            source.sendSuccess(() -> Component.literal("Player not found: " + playerName), false);
            return 0;
        }
        final FabricQuester quester = plugin.getQuester(target.getUUID());
        for (final Quest quest : quester.getCurrentQuests().keySet()) {
            quest.nextStage(quester, false);
            break;
        }
        source.sendSuccess(() -> Component.literal("Advanced " + playerName + " to next stage"), false);
        return Command.SINGLE_SUCCESS;
    }

    private int handleAdminRemove(CommandSourceStack source, String playerName, String questName) {
        final ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(playerName);
        if (target == null) {
            source.sendSuccess(() -> Component.literal("Player not found: " + playerName), false);
            return 0;
        }
        final Quest quest = findQuest(questName);
        if (quest == null) {
            source.sendSuccess(() -> Component.literal("Quest not found: " + questName), false);
            return 0;
        }
        plugin.getQuester(target.getUUID()).hardRemove(quest);
        source.sendSuccess(() -> Component.literal("Removed quest '" + quest.getName() + "' from " + playerName), false);
        return Command.SINGLE_SUCCESS;
    }

    private int handleAdminStats(CommandSourceStack source, String playerName) {
        final ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(playerName);
        if (target == null) {
            source.sendSuccess(() -> Component.literal("Player not found: " + playerName), false);
            return 0;
        }
        final FabricQuester quester = plugin.getQuester(target.getUUID());
        source.sendSuccess(() -> Component.literal("--- Stats for " + playerName + " ---"), false);
        source.sendSuccess(() -> Component.literal("Quest Points: " + quester.getQuestPoints()), false);
        source.sendSuccess(() -> Component.literal("Active Quests: " + quester.getCurrentQuests().size()), false);
        source.sendSuccess(() -> Component.literal("Completed Quests: " + quester.getCompletedQuests().size()), false);
        return Command.SINGLE_SUCCESS;
    }

    private int handleAdminSetStage(CommandSourceStack source, String playerName, String rest) {
        if (rest == null) return 0;
        final String[] parts = rest.split(" ");
        if (parts.length < 2) {
            source.sendSuccess(() -> Component.literal(ChatFormatting.YELLOW + FabricLang.get("inputNum")), false);
            return 0;
        }
        int stage;
        try {
            stage = Integer.parseInt(parts[parts.length - 1]);
        } catch (final NumberFormatException e) {
            source.sendSuccess(() -> Component.literal(ChatFormatting.YELLOW + FabricLang.get("inputNum")), false);
            return 0;
        }
        final String questName = String.join(" ", java.util.Arrays.copyOf(parts, parts.length - 1));
        final ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(playerName);
        if (target == null) {
            source.sendSuccess(() -> Component.literal(ChatFormatting.YELLOW + FabricLang.get("playerNotFound")), false);
            return 0;
        }
        final FabricQuester quester = plugin.getQuester(target.getUUID());
        if (quester.getCurrentQuests().isEmpty()) {
            source.sendSuccess(() -> Component.literal(ChatFormatting.YELLOW + FabricLang.get("noCurrentQuest")), false);
            return 0;
        }
        final Quest quest = findQuest(questName);
        if (quest == null) {
            source.sendSuccess(() -> Component.literal(ChatFormatting.RED + FabricLang.get("questNotFound")
                    .replace("<input>", questName)), false);
            return 0;
        }
        if (!quester.getCurrentQuests().containsKey(quest)) {
            source.sendSuccess(() -> Component.literal(ChatFormatting.GOLD + FabricLang.get("questForceTake")
                    .replace("<player>", quester.getLastKnownName())
                    .replace("<quest>", quest.getName())), false);
            target.sendSystemMessage(Component.literal(ChatFormatting.GREEN + FabricLang.get("questForcedTake")
                    .replace("<player>", quester.getLastKnownName())
                    .replace("<quest>", quest.getName())));
            quester.takeQuest(quest, true);
        }
        final int index = stage - 1;
        if (index < 0 || index >= quest.getStages().size()) {
            source.sendSuccess(() -> Component.literal(ChatFormatting.RED + FabricLang.get("invalidRange")
                    .replace("<least>", "1")
                    .replace("<greatest>", String.valueOf(quest.getStages().size()))), false);
            return 0;
        }
        quest.setStage(quester, index);
        quester.saveData();
        return Command.SINGLE_SUCCESS;
    }

    private Quest findQuest(String name) {
        if (name == null) return null;
        final String lower = name.toLowerCase();
        for (final Quest quest : plugin.getLoadedQuests()) {
            if (quest.getId() != null && quest.getId().equalsIgnoreCase(name)) return quest;
            if (quest.getName() != null && quest.getName().toLowerCase().contains(lower)) return quest;
        }
        return null;
    }
}
