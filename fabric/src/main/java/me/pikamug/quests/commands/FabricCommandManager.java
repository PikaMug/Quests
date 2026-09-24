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
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.pikamug.quests.FabricMixinEvents;
import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.convo.actions.menu.FabricActionMenuPrompt;
import me.pikamug.quests.convo.conditions.menu.FabricConditionMenuPrompt;
import me.pikamug.quests.convo.quests.menu.FabricQuestMenuPrompt;
import me.pikamug.quests.item.FabricQuestJournal;
import me.pikamug.quests.player.FabricQuester;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.Requirements;
import me.pikamug.quests.quests.components.Stage;
import me.pikamug.quests.util.AnsiUtil;
import me.pikamug.quests.util.FabricItemUtil;
import me.pikamug.quests.util.FabricLang;
import me.pikamug.quests.util.FabricMiscUtil;
import me.pikamug.quests.util.SessionData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.item.ItemStack;
import org.browsit.conversations.api.Conversations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class FabricCommandManager {

    private static final String ADMIN = "quests.admin";

    private static final Logger SERVER_LOGGER = LoggerFactory.getLogger("net.minecraft.server.MinecraftServer");

    private final FabricQuestsPlugin plugin;

    private final SuggestionProvider<CommandSourceStack> questSuggestions = (context, builder) ->
            SharedSuggestionProvider.suggest(plugin.getLoadedQuests().stream().map(Quest::getName), builder);

    public FabricCommandManager(FabricQuestsPlugin plugin) {
        this.plugin = plugin;
        register();
    }

    private void register() {
        FabricMixinEvents.registerCommandRegister(dispatcher -> {
            final LiteralCommandNode<CommandSourceStack> questNode = dispatcher.register(
                    Commands.literal("quest")
                            .requires(Commands.hasPermission(Commands.LEVEL_ALL))
                            .executes(ctx -> handleQuest(ctx.getSource()))
                            .then(Commands.argument("quest", StringArgumentType.greedyString())
                                    .suggests(questSuggestions)
                                    .executes(ctx -> handleQuestDetail(ctx.getSource(),
                                            StringArgumentType.getString(ctx, "quest"))))
            );
            dispatcher.register(Commands.literal("q").redirect(questNode));

            final LiteralCommandNode<CommandSourceStack> questsNode = dispatcher.register(
                    Commands.literal("quests")
                            .requires(Commands.hasPermission(Commands.LEVEL_ALL))
                            .executes(ctx -> handleQuestsHelp(ctx.getSource()))
                            .then(Commands.literal("list")
                                    .executes(ctx -> handleQuestsList(ctx.getSource(), 1))
                                    .then(Commands.argument("page", IntegerArgumentType.integer())
                                            .executes(ctx -> handleQuestsList(ctx.getSource(),
                                                    IntegerArgumentType.getInteger(ctx, "page"))))
                            )
                            .then(Commands.literal("take")
                                    .then(Commands.argument("quest", StringArgumentType.greedyString())
                                            .suggests(questSuggestions)
                                            .executes(ctx -> handleQuestsTake(ctx.getSource(),
                                                    StringArgumentType.getString(ctx, "quest"))))
                            )
                            .then(Commands.literal("quit")
                                    .then(Commands.argument("quest", StringArgumentType.greedyString())
                                            .suggests(questSuggestions)
                                            .executes(ctx -> handleQuestsQuit(ctx.getSource(),
                                                    StringArgumentType.getString(ctx, "quest"))))
                            )
                            .then(Commands.literal("stats")
                                    .executes(ctx -> handleQuestsStats(ctx.getSource()))
                            )
                            .then(Commands.literal("top")
                                    .executes(ctx -> handleQuestsTop(ctx.getSource(), 5))
                                    .then(Commands.argument("number", IntegerArgumentType.integer())
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
                                    .requires(s -> canEditor(s, "editor"))
                                    .executes(ctx -> handleQuestsEditor(ctx.getSource(), "editor"))
                            )
                            .then(Commands.literal("actions")
                                    .requires(s -> canEditor(s, "actions"))
                                    .executes(ctx -> handleQuestsEditor(ctx.getSource(), "actions"))
                            )
                            .then(Commands.literal("conditions")
                                    .requires(s -> canEditor(s, "conditions"))
                                    .executes(ctx -> handleQuestsEditor(ctx.getSource(), "conditions"))
                            )
            );
            dispatcher.register(Commands.literal("qs").redirect(questsNode));

            // /questadmin - Admin commands
            final var reloadSub = Commands.literal("reload")
                    .requires(s -> hasAdminNode(s, "reload"))
                    .executes(ctx -> handleReload(ctx.getSource()));

            final var giveSub = Commands.literal("give")
                    .requires(s -> hasAdminNode(s, "give"))
                    .then(Commands.argument("player", StringArgumentType.word())
                            .then(Commands.argument("quest", StringArgumentType.greedyString())
                                    .executes(ctx -> handleAdminGive(ctx.getSource(),
                                            StringArgumentType.getString(ctx, "player"),
                                            StringArgumentType.getString(ctx, "quest")))));

            final var quitSub = Commands.literal("quit")
                    .requires(s -> hasAdminNode(s, "quit"))
                    .then(Commands.argument("player", StringArgumentType.word())
                            .then(Commands.argument("quest", StringArgumentType.greedyString())
                                    .executes(ctx -> handleAdminQuit(ctx.getSource(),
                                            StringArgumentType.getString(ctx, "player"),
                                            StringArgumentType.getString(ctx, "quest")))));

            final var pointsSub = Commands.literal("points")
                    .requires(s -> hasAdminNode(s, "points"))
                    .then(Commands.argument("player", StringArgumentType.word())
                            .executes(ctx -> handleAdminPoints(ctx.getSource(),
                                    StringArgumentType.getString(ctx, "player")))
                            .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                    .executes(ctx -> handleAdminSetPoints(ctx.getSource(),
                                            StringArgumentType.getString(ctx, "player"),
                                            IntegerArgumentType.getInteger(ctx, "amount")))));

            final var givepointsSub = Commands.literal("givepoints")
                    .requires(s -> hasAdminNode(s, "givepoints"))
                    .then(Commands.argument("player", StringArgumentType.word())
                            .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                    .executes(ctx -> handleAdminGivePoints(ctx.getSource(),
                                            StringArgumentType.getString(ctx, "player"),
                                            IntegerArgumentType.getInteger(ctx, "amount")))));

            final var takepointsSub = Commands.literal("takepoints")
                    .requires(s -> hasAdminNode(s, "takepoints"))
                    .then(Commands.argument("player", StringArgumentType.word())
                            .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                    .executes(ctx -> handleAdminTakePoints(ctx.getSource(),
                                            StringArgumentType.getString(ctx, "player"),
                                            IntegerArgumentType.getInteger(ctx, "amount")))));

            final var resetSub = Commands.literal("reset")
                    .requires(s -> hasAdminNode(s, "reset"))
                    .then(Commands.argument("player", StringArgumentType.word())
                            .executes(ctx -> handleAdminReset(ctx.getSource(),
                                    StringArgumentType.getString(ctx, "player"))));

            final var finishSub = Commands.literal("finish")
                    .requires(s -> hasAdminNode(s, "finish"))
                    .then(Commands.argument("player", StringArgumentType.word())
                            .then(Commands.argument("quest", StringArgumentType.greedyString())
                                    .executes(ctx -> handleAdminFinish(ctx.getSource(),
                                            StringArgumentType.getString(ctx, "player"),
                                            StringArgumentType.getString(ctx, "quest")))));

            final var nextstageSub = Commands.literal("nextstage")
                    .requires(s -> hasAdminNode(s, "nextstage"))
                    .then(Commands.argument("player", StringArgumentType.word())
                            .executes(ctx -> handleAdminNextStage(ctx.getSource(),
                                    StringArgumentType.getString(ctx, "player"))));

            final var removeSub = Commands.literal("remove")
                    .requires(s -> hasAdminNode(s, "remove"))
                    .then(Commands.argument("player", StringArgumentType.word())
                            .then(Commands.argument("quest", StringArgumentType.greedyString())
                                    .executes(ctx -> handleAdminRemove(ctx.getSource(),
                                            StringArgumentType.getString(ctx, "player"),
                                            StringArgumentType.getString(ctx, "quest")))));

            final var statsSub = Commands.literal("stats")
                    .requires(s -> hasAdminNode(s, "stats"))
                    .then(Commands.argument("player", StringArgumentType.word())
                            .executes(ctx -> handleAdminStats(ctx.getSource(),
                                    StringArgumentType.getString(ctx, "player"))));

            final var setstageSub = Commands.literal("setstage")
                    .requires(s -> hasAdminNode(s, "setstage"))
                    .then(Commands.argument("player", StringArgumentType.word())
                            .then(Commands.argument("rest", StringArgumentType.greedyString())
                                    .executes(ctx -> handleAdminSetStage(ctx.getSource(),
                                            StringArgumentType.getString(ctx, "player"),
                                            StringArgumentType.getString(ctx, "rest")))));

            final LiteralCommandNode<CommandSourceStack> qaNode = dispatcher.register(
                    Commands.literal("questadmin")
                            .requires(s -> hasAdminRoot(s))
                            .executes(ctx -> handleAdminHelp(ctx.getSource()))
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

    private String lang(final CommandSourceStack source, final String key) {
        return FabricLang.get(source.isPlayer() ? source.getPlayer() : null, key);
    }

    private String lang(final ServerPlayer player, final String key) {
        return FabricLang.get(player, key);
    }

    /**
     * Sends command feedback. Players receive it through the normal success path; console feedback is
     * logged directly (with legacy color codes converted to ANSI) so that only Quests output is colorized.
     */
    private void reply(final CommandSourceStack source, final Supplier<Component> message, final boolean broadcast) {
        if (source.isPlayer()) {
            source.sendSuccess(() -> message.get(), broadcast);
        } else {
            SERVER_LOGGER.info("{}", AnsiUtil.toAnsi(message.get().getString()));
        }
    }

    private boolean hasAdminRoot(final CommandSourceStack source) {
        if (!source.isPlayer()) return true;
        final ServerPlayer player = source.getPlayer();
        if (FabricMiscUtil.hasPermission(player, PermissionLevel.GAMEMASTERS)) return true;
        return plugin.getDependencies().hasPermission(player.getUUID(), ADMIN)
                || plugin.getDependencies().hasPermission(player.getUUID(), ADMIN + ".*");
    }

    private boolean hasAdminNode(final CommandSourceStack source, final String sub) {
        if (!source.isPlayer()) return true;
        final ServerPlayer player = source.getPlayer();
        if (FabricMiscUtil.hasPermission(player, PermissionLevel.GAMEMASTERS)) return true;
        return plugin.getDependencies().hasPermission(player.getUUID(), ADMIN + ".*")
                || plugin.getDependencies().hasPermission(player.getUUID(), ADMIN + "." + sub);
    }

    private boolean canEditor(final CommandSourceStack source, final String sub) {
        if (!source.isPlayer()) return true;
        final ServerPlayer player = source.getPlayer();
        if (FabricMiscUtil.hasPermission(player, PermissionLevel.GAMEMASTERS)) return true;
        return plugin.getDependencies().hasPermission(player.getUUID(), "quests." + sub + ".editor");
    }

    private int handleQuest(final CommandSourceStack source) {
        if (!source.isPlayer()) {
            reply(source,() -> Component.literal("§c" + FabricLang.get("consoleError")), false);
            return 0;
        }
        final ServerPlayer player = source.getPlayer();
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        if (quester.getCurrentQuests().isEmpty()) {
            player.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + lang(player, "noActiveQuest")));
            return Command.SINGLE_SUCCESS;
        }
        for (final Quest quest : quester.getCurrentQuests().keySet()) {
            final Stage stage = quester.getCurrentStage(quest);
            quest.updateCompass(quester, stage);
            if (quester.getQuestProgressOrDefault(quest).getDelayStartTime() == 0
                    || quester.getStageTime(quest) < 0L) {
                final String msg = lang(player, "questObjectivesTitle").replace("<quest>", quest.getName());
                player.sendSystemMessage(Component.literal(ChatFormatting.GOLD + msg));
                quester.showCurrentObjectives(quest, quester, false);
            } else {
                final long time = quester.getStageTime(quest);
                final String msg = ChatFormatting.YELLOW + "(" + lang(player, "delay") + ") "
                        + ChatFormatting.RED + lang(player, "plnTooEarly")
                        .replace("<quest>", quest.getName())
                        .replace("<time>", FabricMiscUtil.getTime(time));
                player.sendSystemMessage(Component.literal(msg));
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private int handleQuestDetail(final CommandSourceStack source, final String name) {
        if (!source.isPlayer()) {
            reply(source,() -> Component.literal("§c" + FabricLang.get("consoleError")), false);
            return 0;
        }
        final ServerPlayer player = source.getPlayer();
        final Quest quest = plugin.getQuest(name != null ? name.toLowerCase() : null);
        if (quest == null) {
            player.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + lang(player, "questNotFound")
                    .replace("<input>", name != null ? name : "")));
            return 0;
        }
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        player.sendSystemMessage(Component.literal(ChatFormatting.GOLD + "- " + quest.getName() + " -"));
        player.sendSystemMessage(Component.literal(" "));
        if (quest.getNpcStart() != null) {
            player.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + lang(player, "speakTo")
                    .replace("<npc>", quest.getNpcStartName())));
        } else {
            player.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + quest.getDescription()));
        }
        player.sendSystemMessage(Component.literal(" "));
        if (plugin.getConfigSettings().canShowQuestReqs()) {
            final Requirements reqs = quest.getRequirements();
            if (reqs != null && reqs.hasRequirement()) {
                player.sendSystemMessage(Component.literal(ChatFormatting.GOLD + lang(player, "requirements")));
                for (final String perm : reqs.getPermissions()) {
                    if (perm == null) continue;
                    if (plugin.getDependencies().hasPermission(player.getUUID(), perm)) {
                        player.sendSystemMessage(Component.literal(ChatFormatting.GREEN
                                + lang(player, "permissionDisplay") + " " + perm));
                    } else {
                        player.sendSystemMessage(Component.literal(ChatFormatting.RED
                                + lang(player, "permissionDisplay") + " " + perm));
                    }
                }
                if (reqs.getQuestPoints() != 0) {
                    if (quester.getQuestPoints() >= reqs.getQuestPoints()) {
                        player.sendSystemMessage(Component.literal(ChatFormatting.GRAY + "- " + ChatFormatting.GREEN
                                + reqs.getQuestPoints() + " " + lang(player, "questPoints")));
                    } else {
                        player.sendSystemMessage(Component.literal(ChatFormatting.GRAY + "- " + ChatFormatting.RED
                                + reqs.getQuestPoints() + " " + lang(player, "questPoints")));
                    }
                }
                for (final Object item : reqs.getItems()) {
                    if (item instanceof ItemStack is) {
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
                                + lang(player, "complete") + " " + ChatFormatting.ITALIC + completed.getName()));
                    } else {
                        player.sendSystemMessage(Component.literal(ChatFormatting.GRAY + "- " + ChatFormatting.RED
                                + lang(player, "complete") + " " + ChatFormatting.ITALIC + completed.getName()));
                    }
                }
                final Map<String, String> completedMap = new LinkedHashMap<>();
                for (final Quest completed : quester.getCompletedQuests()) {
                    completedMap.put(completed.getId(), completed.getName());
                }
                for (final String questId : reqs.getBlockQuestIds()) {
                    if (completedMap.containsKey(questId)) {
                        final String msg = lang(player, "haveCompleted")
                                .replace("<quest>", completedMap.get(questId));
                        player.sendSystemMessage(Component.literal(ChatFormatting.GRAY + "- "
                                + ChatFormatting.RED + msg));
                    } else {
                        final Quest blocked = plugin.getQuestById(questId);
                        final String msg = lang(player, "cannotComplete")
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

    private int handleQuestsHelp(final CommandSourceStack source) {
        final ServerPlayer player = source.isPlayer() ? source.getPlayer() : null;
        reply(source,() -> Component.literal(ChatFormatting.GOLD + lang(source, "questHelpTitle")), false);
        reply(source,() -> Component.literal(ChatFormatting.YELLOW + "/quests " + lang(source, "questDisplayHelp")),
                false);
        final boolean translate = plugin.getConfigSettings().canTranslateSubCommands();
        printQuestsHelpLine(source, translate, player, "list", "COMMAND_LIST");
        printQuestsHelpLine(source, translate, player, "take", "COMMAND_TAKE");
        printQuestsHelpLine(source, translate, player, "quit", "COMMAND_QUIT");
        printQuestsHelpLine(source, translate, player, "stats", "COMMAND_STATS");
        printQuestsHelpLine(source, translate, player, "journal", "COMMAND_JOURNAL");
        printQuestsHelpLine(source, translate, player, "top", "COMMAND_TOP");
        printQuestsHelpLine(source, translate, player, "editor", "COMMAND_EDITOR");
        printQuestsHelpLine(source, translate, player, "actions", "COMMAND_EVENTS_EDITOR");
        printQuestsHelpLine(source, translate, player, "conditions", "COMMAND_CONDITIONS_EDITOR");
        printQuestsHelpLine(source, translate, player, "info", "COMMAND_INFO");
        if (player != null) {
            player.sendSystemMessage(Component.literal(ChatFormatting.DARK_AQUA + "/quest " + ChatFormatting.YELLOW
                    + lang(player, "COMMAND_QUEST_HELP")));
            player.sendSystemMessage(Component.literal(ChatFormatting.DARK_AQUA + "/quest " + ChatFormatting.YELLOW
                    + lang(player, "COMMAND_QUESTINFO_HELP")));
        }
        if (hasAdminRoot(source)) {
            reply(source,() -> Component.literal(ChatFormatting.YELLOW + "/questadmin " + ChatFormatting.RED
                    + lang(source, "COMMAND_QUESTADMIN_HELP")), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private void printQuestsHelpLine(final CommandSourceStack source, final boolean translate,
                                     final ServerPlayer player, final String sub, final String key) {
        final String name = translate ? lang(player, key) : sub;
        reply(source,() -> Component.literal(ChatFormatting.YELLOW + "/quests "
                + lang(source, key + "_HELP").replace("<command>", name)), false);
    }

    private int handleAdminHelp(final CommandSourceStack source) {
        reply(source,() -> Component.literal(ChatFormatting.GOLD + lang(source, "questAdminHelpTitle")), false);
        reply(source,() -> Component.literal(ChatFormatting.YELLOW + "/questadmin" + ChatFormatting.RED + " "
                + lang(source, "COMMAND_QUESTADMIN_HELP")), false);
        final boolean translate = plugin.getConfigSettings().canTranslateSubCommands();
        printAdminHelpLine(source, translate, "stats", "COMMAND_QUESTADMIN_STATS");
        printAdminHelpLine(source, translate, "give", "COMMAND_QUESTADMIN_GIVE");
        printAdminHelpLine(source, translate, "quit", "COMMAND_QUESTADMIN_QUIT");
        printAdminHelpLine(source, translate, "points", "COMMAND_QUESTADMIN_POINTS");
        printAdminHelpLine(source, translate, "takepoints", "COMMAND_QUESTADMIN_TAKEPOINTS");
        printAdminHelpLine(source, translate, "givepoints", "COMMAND_QUESTADMIN_GIVEPOINTS");
        printAdminHelpLine(source, translate, "finish", "COMMAND_QUESTADMIN_FINISH");
        printAdminHelpLine(source, translate, "nextstage", "COMMAND_QUESTADMIN_NEXTSTAGE");
        printAdminHelpLine(source, translate, "setstage", "COMMAND_QUESTADMIN_SETSTAGE");
        printAdminHelpLine(source, translate, "reset", "COMMAND_QUESTADMIN_RESET");
        printAdminHelpLine(source, translate, "remove", "COMMAND_QUESTADMIN_REMOVE");
        printAdminHelpLine(source, translate, "reload", "COMMAND_QUESTADMIN_RELOAD");
        return Command.SINGLE_SUCCESS;
    }

    private void printAdminHelpLine(final CommandSourceStack source, final boolean translate,
                                    final String sub, final String key) {
        if (!hasAdminNode(source, sub)) return;
        final String name = translate ? lang(source, key) : sub;
        reply(source,() -> Component.literal(ChatFormatting.YELLOW + "/questadmin " + ChatFormatting.RED
                + lang(source, key + "_HELP").replace("<command>", name)), false);
    }

    private int handleQuestsList(final CommandSourceStack source, int page) {
        if (!source.isPlayer()) {
            int num = 1;
            reply(source,() -> Component.literal(ChatFormatting.GOLD + lang(source, "questListTitle")), false);
            if (plugin.getLoadedQuests().isEmpty()) {
                reply(source,() -> Component.literal(ChatFormatting.YELLOW + lang(source, "noQuests")), false);
                return Command.SINGLE_SUCCESS;
            }
            for (final Quest q : plugin.getLoadedQuests()) {
                final int lineNo = num;
                reply(source,() -> Component.literal(ChatFormatting.YELLOW + String.valueOf(lineNo)
                        + ". " + q.getName()), false);
                num++;
            }
            return Command.SINGLE_SUCCESS;
        }
        final ServerPlayer player = source.getPlayer();
        if (page < 1) {
            player.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + lang(player, "pageSelectionPosNum")));
            return Command.SINGLE_SUCCESS;
        }
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        quester.listQuests(quester, page);
        return Command.SINGLE_SUCCESS;
    }

    private int handleQuestsTake(final CommandSourceStack source, String questName) {
        if (!source.isPlayer()) return 0;
        final ServerPlayer player = source.getPlayer();
        if (!plugin.getConfigSettings().canAllowCommands()) {
            player.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + lang(player, "questTakeDisabled")));
            return 0;
        }
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        final Quest quest = plugin.getQuest(questName != null ? questName.toLowerCase() : null);
        if (quest == null) {
            player.sendSystemMessage(Component.literal(ChatFormatting.RED + lang(player, "questNotFound")
                    .replace("<input>", questName != null ? questName : "")));
            return 0;
        }
        for (final Quest q : quester.getCurrentQuests().keySet()) {
            if (q.getId() != null && q.getId().equals(quest.getId())) {
                player.sendSystemMessage(Component.literal(ChatFormatting.RED + lang(player, "questAlreadyOn")));
                return 0;
            }
        }
        quester.offerQuest(quest, true);
        return Command.SINGLE_SUCCESS;
    }

    private int handleQuestsQuit(final CommandSourceStack source, String questName) {
        if (!source.isPlayer()) return 0;
        final ServerPlayer player = source.getPlayer();
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        if (quester.getCurrentQuests().isEmpty()) {
            player.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + lang(player, "noActiveQuest")));
            return 0;
        }
        final Quest quest = plugin.getQuest(questName != null ? questName.toLowerCase() : null);
        if (quest == null) {
            player.sendSystemMessage(Component.literal(ChatFormatting.RED + lang(player, "questNotFound")
                    .replace("<input>", questName != null ? questName : "")));
            return 0;
        }
        if (quest.getOptions().canAllowQuitting()) {
            final String msg = lang(player, "questQuit").replace("<quest>", quest.getName());
            quester.quitQuest(quest, msg);
        } else {
            player.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + lang(player, "questQuitDisabled")));
        }
        return Command.SINGLE_SUCCESS;
    }

    private int handleQuestsStats(final CommandSourceStack source) {
        if (!source.isPlayer()) return 0;
        final ServerPlayer player = source.getPlayer();
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        displayStats(source, quester);
        return Command.SINGLE_SUCCESS;
    }

    private void displayStats(final CommandSourceStack source, final FabricQuester quester) {
        final ServerPlayer player = source.isPlayer() ? source.getPlayer() : null;
        reply(source,() -> Component.literal(ChatFormatting.YELLOW + lang(player, "questPoints") + " - "
                + ChatFormatting.DARK_PURPLE + quester.getQuestPoints()), false);
        if (quester.getCurrentQuests().isEmpty()) {
            reply(source,() -> Component.literal(ChatFormatting.YELLOW + lang(player, "currentQuest")
                    + " " + ChatFormatting.DARK_PURPLE + lang(player, "none")), false);
        } else {
            reply(source,() -> Component.literal(ChatFormatting.YELLOW + lang(player, "currentQuest")), false);
            for (final Map.Entry<Quest, Integer> entry : quester.getCurrentQuests().entrySet()) {
                final String questName = entry.getKey().getName();
                final int stageNumber = entry.getValue() + 1;
                reply(source,() -> Component.literal(ChatFormatting.YELLOW + "- " + questName
                        + ChatFormatting.LIGHT_PURPLE + " (" + lang(player, "stageEditorStage") + " " + stageNumber
                        + ")"), false);
            }
        }
        if (quester.getCompletedQuests().isEmpty()) {
            reply(source,() -> Component.literal(ChatFormatting.YELLOW + lang(player, "completedQuest")
                    + " " + ChatFormatting.DARK_PURPLE + lang(player, "none")), false);
        } else {
            reply(source,() -> Component.literal(ChatFormatting.YELLOW + lang(player, "completedQuest")), false);
            for (final Quest q : quester.getCompletedQuests()) {
                reply(source,() -> Component.literal(ChatFormatting.YELLOW + "- " + q.getName()), false);
            }
        }
    }

    private int handleQuestsTop(final CommandSourceStack source, int topNumber) {
        final int limit = plugin.getConfigSettings().getTopLimit();
        final ServerPlayer player = source.isPlayer() ? source.getPlayer() : null;
        if (topNumber < 1 || topNumber > limit) {
            reply(source,() -> Component.literal(ChatFormatting.YELLOW
                    + lang(player, "invalidRange").replace("<least>", "1")
                    .replace("<greatest>", String.valueOf(limit))), false);
            return Command.SINGLE_SUCCESS;
        }
        final Map<String, Integer> questPoints = new LinkedHashMap<>();
        for (final Quester quester : plugin.getOfflineQuesters()) {
            if (quester.getLastKnownName() != null) {
                questPoints.put(quester.getLastKnownName(), quester.getQuestPoints());
            }
        }
        final List<Map.Entry<String, Integer>> sorted = new java.util.ArrayList<>(questPoints.entrySet());
        sorted.sort(Map.Entry.comparingByValue(java.util.Collections.reverseOrder()));
        reply(source,() -> Component.literal(ChatFormatting.GOLD
                + lang(player, "topQuestersTitle").replace("<number>", String.valueOf(topNumber))), false);
        int printed = 0;
        for (final Map.Entry<String, Integer> entry : sorted) {
            printed++;
            final int lineNo = printed;
            reply(source,() -> Component.literal(ChatFormatting.YELLOW + String.valueOf(lineNo) + ". "
                    + entry.getKey() + " - " + ChatFormatting.DARK_PURPLE + String.valueOf(entry.getValue())
                    + ChatFormatting.YELLOW + " " + lang(player, "questPoints")), false);
            if (printed >= topNumber) break;
        }
        return Command.SINGLE_SUCCESS;
    }

    private int handleQuestsInfo(final CommandSourceStack source) {
        reply(source,() -> Component.literal(ChatFormatting.GOLD + lang(source, "developedBy")
                + " " + "PikaMug & contributors"), false);
        reply(source,() -> Component.literal(ChatFormatting.GOLD + "Quests v5.3.3 (Fabric)"), false);
        reply(source,() -> Component.literal(ChatFormatting.GOLD + lang(source, "numQuestsLoaded")
                .replace("<number>", String.valueOf(plugin.getLoadedQuests().size()))), false);
        return Command.SINGLE_SUCCESS;
    }

    private int handleQuestsJournal(final CommandSourceStack source) {
        if (!source.isPlayer()) return 0;
        final ServerPlayer player = source.getPlayer();
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        final net.minecraft.world.entity.player.Inventory inv = player.getInventory();
        final int index = quester.getJournalIndex();
        if (index != -1) {
            inv.setItem(index, ItemStack.EMPTY);
            player.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + lang(player, "journalPutAway")
                    .replace("<journal>", lang(player, "journalTitle"))));
        } else if (player.getMainHandItem().isEmpty()) {
            final FabricQuestJournal journal = new FabricQuestJournal(plugin, quester);
            player.getInventory().setItem(player.getInventory().getSelectedSlot(), journal.toItemStack());
            player.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + lang(player, "journalTaken")
                    .replace("<journal>", lang(player, "journalTitle"))));
        } else if (inv.getFreeSlot() != -1) {
            final FabricQuestJournal journal = new FabricQuestJournal(plugin, quester);
            inv.add(journal.toItemStack());
            player.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + lang(player, "journalTaken")
                    .replace("<journal>", lang(player, "journalTitle"))));
        } else {
            player.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + lang(player, "journalNoRoom")
                    .replace("<journal>", lang(player, "journalTitle"))));
        }
        return Command.SINGLE_SUCCESS;
    }

    private int handleQuestsChoice(final CommandSourceStack source, String input) {
        if (!source.isPlayer()) return 0;
        if (input == null || input.isEmpty()) return Command.SINGLE_SUCCESS;
        final ServerPlayer player = source.getPlayer();
        final Optional<org.browsit.conversations.api.data.Conversation> conversation = Conversations
                .getConversationOf(player.getUUID());
        if (!conversation.isPresent()) {
            player.sendSystemMessage(Component.literal(ChatFormatting.RED + lang(player, "notConversing")));
            return Command.SINGLE_SUCCESS;
        }
        conversation.get().handleInput(input);
        return Command.SINGLE_SUCCESS;
    }

    private int handleQuestsEditor(final CommandSourceStack source, String editor) {
        if (!source.isPlayer()) {
            reply(source,() -> Component.literal("§c" + FabricLang.get("consoleError")), false);
            return 0;
        }
        final ServerPlayer player = source.getPlayer();
        if (Conversations.getConversationOf(player.getUUID()).isPresent()) {
            player.sendSystemMessage(Component.literal(ChatFormatting.RED + lang(player, "duplicateEditor")));
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

    private UUID resolveTargetUuid(final CommandSourceStack source, final String arg) {
        if (arg == null) return null;
        final ServerPlayer online = source.getServer().getPlayerList().getPlayerByName(arg);
        if (online != null) return online.getUUID();
        UUID uuid = null;
        try {
            uuid = UUID.fromString(arg);
        } catch (final IllegalArgumentException e) {
            // Do nothing
        }
        if (uuid != null) {
            for (final Quester q : plugin.getOfflineQuesters()) {
                if (q.getUUID().equals(uuid)) return uuid;
            }
            return null;
        }
        for (final Quester q : plugin.getOfflineQuesters()) {
            final String name = q.getLastKnownName();
            if (name != null && name.equalsIgnoreCase(arg)) return q.getUUID();
        }
        for (final Quester q : plugin.getOfflineQuesters()) {
            final String name = q.getLastKnownName();
            if (name != null && name.toLowerCase().startsWith(arg.toLowerCase())) return q.getUUID();
        }
        for (final Quester q : plugin.getOfflineQuesters()) {
            final String name = q.getLastKnownName();
            if (name != null && name.toLowerCase().contains(arg.toLowerCase())) return q.getUUID();
        }
        return null;
    }

    private FabricQuester resolveTarget(final CommandSourceStack source, final String arg) {
        final UUID uuid = resolveTargetUuid(source, arg);
        if (uuid == null) return null;
        return plugin.getQuester(uuid);
    }

    private int handleReload(final CommandSourceStack source) {
        reply(source,() -> Component.literal(ChatFormatting.GOLD + lang(source, "questsReloading")), false);
        plugin.reload();
        reply(source,() -> Component.literal(ChatFormatting.GOLD + lang(source, "questsReloaded")), false);
        reply(source,() -> Component.literal(ChatFormatting.GOLD + lang(source, "numQuestsLoaded")
                .replace("<number>", String.valueOf(plugin.getLoadedQuests().size()))), false);
        return Command.SINGLE_SUCCESS;
    }

    private int handleAdminGive(final CommandSourceStack source, String playerName, String questName) {
        final FabricQuester quester = resolveTarget(source, playerName);
        if (quester == null) {
            reply(source,() -> Component.literal(ChatFormatting.YELLOW + lang(source, "playerNotFound")), false);
            return 0;
        }
        final Quest quest = findQuest(questName);
        if (quest == null) {
            reply(source,() -> Component.literal(ChatFormatting.YELLOW + lang(source, "questNotFound")
                    .replace("<input>", questName != null ? questName : "")), false);
            return 0;
        }
        for (final Quest q : quester.getCurrentQuests().keySet()) {
            if (q.getId() != null && q.getId().equals(quest.getId())) {
                final String msg = lang(source, "questsPlayerHasQuestAlready")
                        .replace("<player>", quester.getLastKnownName())
                        .replace("<quest>", quest.getName());
                reply(source,() -> Component.literal(ChatFormatting.YELLOW + msg), false);
                return 0;
            }
        }
        quester.hardQuit(quest);
        final String msg1 = lang(source, "questForceTake")
                .replace("<player>", quester.getLastKnownName())
                .replace("<quest>", quest.getName());
        reply(source,() -> Component.literal(ChatFormatting.GOLD + msg1), false);
        final ServerPlayer onlineTarget = source.getServer().getPlayerList().getPlayer(quester.getUUID());
        if (onlineTarget != null) {
            final String msg2 = lang(onlineTarget, "questForcedTake")
                    .replace("<player>", source.isPlayer() ? source.getPlayer().getName().getString() : "Console")
                    .replace("<quest>", quest.getName());
            onlineTarget.sendSystemMessage(Component.literal(ChatFormatting.GREEN + msg2));
        }
        quester.takeQuest(quest, true);
        return Command.SINGLE_SUCCESS;
    }

    private int handleAdminQuit(final CommandSourceStack source, String playerName, String questName) {
        final FabricQuester quester = resolveTarget(source, playerName);
        if (quester == null) {
            reply(source,() -> Component.literal(ChatFormatting.YELLOW + lang(source, "playerNotFound")), false);
            return 0;
        }
        if (quester.getCurrentQuests().isEmpty()) {
            final String msg = lang(source, "noCurrentQuest")
                    .replace("<player>", quester.getLastKnownName());
            reply(source,() -> Component.literal(ChatFormatting.YELLOW + msg), false);
            return 0;
        }
        final Quest quest = findQuest(questName);
        if (quest == null) {
            reply(source,() -> Component.literal(ChatFormatting.RED + lang(source, "questNotFound")
                    .replace("<input>", questName != null ? questName : "")), false);
            return 0;
        }
        final String msg1 = lang(source, "questForceQuit")
                .replace("<player>", quester.getLastKnownName())
                .replace("<quest>", quest.getName());
        reply(source,() -> Component.literal(ChatFormatting.GOLD + msg1), false);
        final ServerPlayer onlineTarget = source.getServer().getPlayerList().getPlayer(quester.getUUID());
        final String msg2 = lang(onlineTarget, "questForcedQuit")
                .replace("<player>", source.isPlayer() ? source.getPlayer().getName().getString() : "Console")
                .replace("<quest>", quest.getName());
        quester.quitQuest(quest, msg2);
        return Command.SINGLE_SUCCESS;
    }

    private int handleAdminPoints(final CommandSourceStack source, String playerName) {
        final FabricQuester quester = resolveTarget(source, playerName);
        if (quester == null) {
            reply(source,() -> Component.literal(ChatFormatting.YELLOW + lang(source, "playerNotFound")), false);
            return 0;
        }
        reply(source,() -> Component.literal(ChatFormatting.YELLOW + quester.getLastKnownName() + " - "
                + ChatFormatting.DARK_PURPLE + quester.getQuestPoints() + ChatFormatting.YELLOW + " "
                + lang(source, "questPoints")), false);
        return Command.SINGLE_SUCCESS;
    }

    private int handleAdminSetPoints(final CommandSourceStack source, String playerName, int amount) {
        final FabricQuester quester = resolveTarget(source, playerName);
        if (quester == null) {
            reply(source,() -> Component.literal(ChatFormatting.YELLOW + lang(source, "playerNotFound")), false);
            return 0;
        }
        quester.setQuestPoints(amount);
        final String msg1 = lang(source, "setQuestPoints")
                .replace("<points>", lang(source, "questPoints"))
                .replace("<player>", quester.getLastKnownName())
                .replace("<number>", String.valueOf(amount));
        reply(source,() -> Component.literal(ChatFormatting.GOLD + msg1), false);
        final ServerPlayer onlineTarget = source.getServer().getPlayerList().getPlayer(quester.getUUID());
        if (onlineTarget != null) {
            final String msg2 = lang(onlineTarget, "questPointsSet")
                    .replace("<points>", lang(onlineTarget, "questPoints"))
                    .replace("<player>", source.isPlayer() ? source.getPlayer().getName().getString() : "Console")
                    .replace("<number>", String.valueOf(amount));
            onlineTarget.sendSystemMessage(Component.literal(ChatFormatting.GREEN + msg2));
        }
        quester.saveData();
        return Command.SINGLE_SUCCESS;
    }

    private int handleAdminGivePoints(final CommandSourceStack source, String playerName, int amount) {
        final FabricQuester quester = resolveTarget(source, playerName);
        if (quester == null) {
            reply(source,() -> Component.literal(ChatFormatting.YELLOW + lang(source, "playerNotFound")), false);
            return 0;
        }
        quester.setQuestPoints(quester.getQuestPoints() + amount);
        final String msg1 = lang(source, "giveQuestPoints")
                .replace("<points>", lang(source, "questPoints"))
                .replace("<player>", quester.getLastKnownName())
                .replace("<number>", String.valueOf(amount));
        reply(source,() -> Component.literal(ChatFormatting.GOLD + msg1), false);
        final ServerPlayer onlineTarget = source.getServer().getPlayerList().getPlayer(quester.getUUID());
        if (onlineTarget != null) {
            final String msg2 = lang(onlineTarget, "questPointsGiven")
                    .replace("<points>", lang(onlineTarget, "questPoints"))
                    .replace("<player>", source.isPlayer() ? source.getPlayer().getName().getString() : "Console")
                    .replace("<number>", String.valueOf(amount));
            onlineTarget.sendSystemMessage(Component.literal(ChatFormatting.GREEN + msg2));
        }
        quester.saveData();
        return Command.SINGLE_SUCCESS;
    }

    private int handleAdminTakePoints(final CommandSourceStack source, String playerName, int amount) {
        final FabricQuester quester = resolveTarget(source, playerName);
        if (quester == null) {
            reply(source,() -> Component.literal(ChatFormatting.YELLOW + lang(source, "playerNotFound")), false);
            return 0;
        }
        quester.setQuestPoints(Math.max(0, quester.getQuestPoints() - amount));
        final String msg1 = lang(source, "takeQuestPoints")
                .replace("<points>", lang(source, "questPoints"))
                .replace("<player>", quester.getLastKnownName())
                .replace("<number>", String.valueOf(amount));
        reply(source,() -> Component.literal(ChatFormatting.GOLD + msg1), false);
        final ServerPlayer onlineTarget = source.getServer().getPlayerList().getPlayer(quester.getUUID());
        if (onlineTarget != null) {
            final String msg2 = lang(onlineTarget, "questPointsTaken")
                    .replace("<points>", lang(onlineTarget, "questPoints"))
                    .replace("<player>", source.isPlayer() ? source.getPlayer().getName().getString() : "Console")
                    .replace("<number>", String.valueOf(amount));
            onlineTarget.sendSystemMessage(Component.literal(ChatFormatting.GREEN + msg2));
        }
        quester.saveData();
        return Command.SINGLE_SUCCESS;
    }

    private int handleAdminReset(final CommandSourceStack source, String playerName) {
        final FabricQuester quester = resolveTarget(source, playerName);
        if (quester == null) {
            reply(source,() -> Component.literal(ChatFormatting.YELLOW + lang(source, "playerNotFound")), false);
            return 0;
        }
        quester.hardClear();
        final String msg = lang(source, "questReset").replace("<player>", quester.getLastKnownName());
        reply(source,() -> Component.literal(ChatFormatting.GOLD + msg), false);
        quester.saveData();
        return Command.SINGLE_SUCCESS;
    }

    private int handleAdminFinish(final CommandSourceStack source, String playerName, String questName) {
        final FabricQuester quester = resolveTarget(source, playerName);
        if (quester == null) {
            reply(source,() -> Component.literal(ChatFormatting.YELLOW + lang(source, "playerNotFound")), false);
            return 0;
        }
        if (quester.getCurrentQuests().isEmpty()) {
            final String msg = lang(source, "noCurrentQuest")
                    .replace("<player>", quester.getLastKnownName());
            reply(source,() -> Component.literal(ChatFormatting.YELLOW + msg), false);
            return 0;
        }
        final Quest quest = findQuest(questName);
        if (quest == null) {
            reply(source,() -> Component.literal(ChatFormatting.RED + lang(source, "questNotFound")
                    .replace("<input>", questName != null ? questName : "")), false);
            return 0;
        }
        final String msg1 = lang(source, "questForceFinish")
                .replace("<player>", quester.getLastKnownName())
                .replace("<quest>", quest.getName());
        reply(source,() -> Component.literal(ChatFormatting.GOLD + msg1), false);
        final ServerPlayer onlineTarget = source.getServer().getPlayerList().getPlayer(quester.getUUID());
        if (onlineTarget != null) {
            final String msg2 = lang(onlineTarget, "questForcedFinish")
                    .replace("<player>", source.isPlayer() ? source.getPlayer().getName().getString() : "Console")
                    .replace("<quest>", quest.getName());
            onlineTarget.sendSystemMessage(Component.literal(ChatFormatting.GREEN + msg2));
        }
        quest.completeQuest(quester);
        quester.saveData();
        return Command.SINGLE_SUCCESS;
    }

    private int handleAdminNextStage(final CommandSourceStack source, String playerName) {
        final FabricQuester quester = resolveTarget(source, playerName);
        if (quester == null) {
            reply(source,() -> Component.literal(ChatFormatting.YELLOW + lang(source, "playerNotFound")), false);
            return 0;
        }
        if (quester.getCurrentQuests().isEmpty()) {
            final String msg = lang(source, "noCurrentQuest")
                    .replace("<player>", quester.getLastKnownName());
            reply(source,() -> Component.literal(ChatFormatting.YELLOW + msg), false);
            return 0;
        }
        for (final Quest quest : quester.getCurrentQuests().keySet()) {
            final String msg1 = lang(source, "questForceNextStage")
                    .replace("<player>", quester.getLastKnownName())
                    .replace("<quest>", quest.getName());
            reply(source,() -> Component.literal(ChatFormatting.GOLD + msg1), false);
            final ServerPlayer onlineTarget = source.getServer().getPlayerList().getPlayer(quester.getUUID());
            if (onlineTarget != null) {
                final String msg2 = lang(onlineTarget, "questForcedNextStage")
                        .replace("<player>", source.isPlayer() ? source.getPlayer().getName().getString() : "Console")
                        .replace("<quest>", quest.getName());
                onlineTarget.sendSystemMessage(Component.literal(ChatFormatting.GREEN + msg2));
            }
            quest.nextStage(quester, false);
            quester.saveData();
            break;
        }
        return Command.SINGLE_SUCCESS;
    }

    private int handleAdminRemove(final CommandSourceStack source, String playerName, String questName) {
        final FabricQuester quester = resolveTarget(source, playerName);
        if (quester == null) {
            reply(source,() -> Component.literal(ChatFormatting.YELLOW + lang(source, "playerNotFound")), false);
            return 0;
        }
        final Quest quest = findQuest(questName);
        if (quest == null) {
            reply(source,() -> Component.literal(ChatFormatting.RED + lang(source, "questNotFound")
                    .replace("<input>", questName != null ? questName : "")), false);
            return 0;
        }
        quester.hardRemove(quest);
        final String msg = lang(source, "questRemoved")
                .replace("<player>", quester.getLastKnownName())
                .replace("<quest>", quest.getName());
        reply(source,() -> Component.literal(ChatFormatting.GOLD + msg), false);
        quester.saveData();
        return Command.SINGLE_SUCCESS;
    }

    private int handleAdminStats(final CommandSourceStack source, String playerName) {
        final FabricQuester quester = resolveTarget(source, playerName);
        if (quester == null) {
            reply(source,() -> Component.literal(ChatFormatting.YELLOW + lang(source, "playerNotFound")), false);
            return 0;
        }
        displayStats(source, quester);
        return Command.SINGLE_SUCCESS;
    }

    private int handleAdminSetStage(final CommandSourceStack source, String playerName, String rest) {
        if (rest == null) return 0;
        final String[] parts = rest.split(" ");
        if (parts.length < 2) {
            reply(source,() -> Component.literal(ChatFormatting.YELLOW + lang(source, "inputNum")), false);
            return 0;
        }
        int stage;
        try {
            stage = Integer.parseInt(parts[parts.length - 1]);
        } catch (final NumberFormatException e) {
            reply(source,() -> Component.literal(ChatFormatting.YELLOW + lang(source, "inputNum")), false);
            return 0;
        }
        final String questName = String.join(" ", java.util.Arrays.copyOf(parts, parts.length - 1));
        final FabricQuester quester = resolveTarget(source, playerName);
        if (quester == null) {
            reply(source,() -> Component.literal(ChatFormatting.YELLOW + lang(source, "playerNotFound")), false);
            return 0;
        }
        final String targetName = quester.getLastKnownName() != null ? quester.getLastKnownName() : playerName;
        if (quester.getCurrentQuests().isEmpty()) {
            reply(source,() -> Component.literal(ChatFormatting.YELLOW + lang(source, "noCurrentQuest")
                    .replace("<player>", targetName)), false);
            return 0;
        }
        final Quest quest = findQuest(questName);
        if (quest == null) {
            reply(source,() -> Component.literal(ChatFormatting.RED + lang(source, "questNotFound")
                    .replace("<input>", questName)), false);
            return 0;
        }
        if (!quester.getCurrentQuests().containsKey(quest)) {
            final String msg1 = lang(source, "questForceTake")
                    .replace("<player>", targetName)
                    .replace("<quest>", quest.getName());
            reply(source,() -> Component.literal(ChatFormatting.GOLD + msg1), false);
            final ServerPlayer onlineTarget = source.getServer().getPlayerList().getPlayer(quester.getUUID());
            if (onlineTarget != null) {
                final String msg2 = lang(onlineTarget, "questForcedTake")
                        .replace("<player>", source.isPlayer() ? source.getPlayer().getName().getString() : "Console")
                        .replace("<quest>", quest.getName());
                onlineTarget.sendSystemMessage(Component.literal(ChatFormatting.GREEN + msg2));
            }
            quester.takeQuest(quest, true);
        }
        final int index = stage - 1;
        if (index < 0 || index >= quest.getStages().size()) {
            reply(source,() -> Component.literal(ChatFormatting.RED + lang(source, "invalidRange")
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
        final Quest exact = plugin.getQuest(name.toLowerCase());
        if (exact != null) return exact;
        final String lower = name.toLowerCase();
        for (final Quest quest : plugin.getLoadedQuests()) {
            if (quest.getId() != null && quest.getId().equalsIgnoreCase(name)) return quest;
            if (quest.getName() != null && quest.getName().toLowerCase().contains(lower)) return quest;
        }
        return null;
    }
}