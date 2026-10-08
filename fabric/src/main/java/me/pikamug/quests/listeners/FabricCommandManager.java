/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.listeners;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.pikamug.quests.FabricMixinEvents;
import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.commands.quest.FabricQuestCommandHandler;
import me.pikamug.quests.commands.questadmin.FabricQuestadminCommandHandler;
import me.pikamug.quests.commands.quests.FabricQuestsCommandHandler;
import me.pikamug.quests.quests.Quest;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;

public class FabricCommandManager {

    private final FabricQuestsPlugin plugin;
    private final FabricQuestCommandHandler questHandler;
    private final FabricQuestsCommandHandler questsHandler;
    private final FabricQuestadminCommandHandler questadminHandler;

    private final SuggestionProvider<CommandSourceStack> questSuggestions;

    public FabricCommandManager(FabricQuestsPlugin plugin) {
        this.plugin = plugin;
        questHandler = new FabricQuestCommandHandler(plugin);
        questsHandler = new FabricQuestsCommandHandler(plugin);
        questadminHandler = new FabricQuestadminCommandHandler(plugin);
        questSuggestions = (context, builder) ->
                SharedSuggestionProvider.suggest(plugin.getLoadedQuests().stream().map(Quest::getName), builder);
        register();
    }

    /**
     * Splits brigadier command input into an argument array shaped exactly like the Bukkit counterpart:
     * everything after the first space, split on ' ' retaining trailing empty tokens. A command with no
     * arguments yields a zero-length array.
     */
    private String[] rawArgs(final String input) {
        final int idx = input.indexOf(' ');
        if (idx < 0) {
            return new String[0];
        }
        return input.substring(idx + 1).split(" ", -1);
    }

    /**
     * Routes tab completion through the handler so any argument node can produce subcommand-specific
     * suggestions. A null result falls back to online player names, and results are pre-filtered by the
     * partially-typed token, mirroring Bukkit's tab completion behavior.
     */
    private SuggestionProvider<CommandSourceStack> suggestFor(
            final BiFunction<CommandSourceStack, String[], List<String>> suggester) {
        return (ctx, builder) -> {
            final List<String> results = suggester.apply(ctx.getSource(), rawArgs(ctx.getInput()));
            final List<String> available;
            if (results == null) {
                available = Arrays.asList(ctx.getSource().getServer().getPlayerNames());
            } else {
                available = results;
            }
            final String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
            final List<String> filtered = new ArrayList<>();
            for (final String result : available) {
                if (result != null && result.toLowerCase(Locale.ROOT).startsWith(remaining)) {
                    filtered.add(result);
                }
            }
            return SharedSuggestionProvider.suggest(filtered, builder);
        };
    }

    private void register() {
        FabricMixinEvents.registerCommandRegister(dispatcher -> {
            dispatcher.register(
                    Commands.literal("quest")
                            .requires(Commands.hasPermission(Commands.LEVEL_ALL))
                            .executes(ctx -> questHandler.handleQuest(ctx.getSource()))
                            .then(Commands.argument("quest", StringArgumentType.greedyString())
                                    .suggests(questSuggestions)
                                    .executes(ctx -> questHandler.handleQuestDetail(ctx.getSource(),
                                            StringArgumentType.getString(ctx, "quest"))))
            );
            dispatcher.register(
                    Commands.literal("q")
                            .requires(Commands.hasPermission(Commands.LEVEL_ALL))
                            .executes(ctx -> questHandler.handleQuest(ctx.getSource()))
                            .then(Commands.argument("quest", StringArgumentType.greedyString())
                                    .suggests(questSuggestions)
                                    .executes(ctx -> questHandler.handleQuestDetail(ctx.getSource(),
                                            StringArgumentType.getString(ctx, "quest"))))
            );

            final LiteralCommandNode<CommandSourceStack> questsNode = dispatcher.register(
                    Commands.literal("quests")
                            .requires(Commands.hasPermission(Commands.LEVEL_ALL))
                            .executes(ctx -> questsHandler.check(ctx.getSource(), rawArgs(ctx.getInput())))
                            .then(Commands.literal("list")
                                    .executes(ctx -> questsHandler.check(ctx.getSource(), rawArgs(ctx.getInput())))
                                    .then(Commands.argument("page", StringArgumentType.word())
                                            .suggests(suggestFor(questsHandler::suggest))
                                            .executes(ctx -> questsHandler.check(ctx.getSource(),
                                                    rawArgs(ctx.getInput()))))
                            )
                            .then(Commands.literal("take")
                                    .executes(ctx -> questsHandler.check(ctx.getSource(), rawArgs(ctx.getInput())))
                                    .then(Commands.argument("quest", StringArgumentType.greedyString())
                                            .suggests(suggestFor(questsHandler::suggest))
                                            .executes(ctx -> questsHandler.check(ctx.getSource(),
                                                    rawArgs(ctx.getInput()))))
                            )
                            .then(Commands.literal("quit")
                                    .executes(ctx -> questsHandler.check(ctx.getSource(), rawArgs(ctx.getInput())))
                                    .then(Commands.argument("quest", StringArgumentType.greedyString())
                                            .suggests(suggestFor(questsHandler::suggest))
                                            .executes(ctx -> questsHandler.check(ctx.getSource(),
                                                    rawArgs(ctx.getInput()))))
                            )
                            .then(Commands.literal("stats")
                                    .executes(ctx -> questsHandler.check(ctx.getSource(), rawArgs(ctx.getInput())))
                            )
                            .then(Commands.literal("top")
                                    .executes(ctx -> questsHandler.check(ctx.getSource(), rawArgs(ctx.getInput())))
                                    .then(Commands.argument("number", StringArgumentType.word())
                                            .suggests(suggestFor(questsHandler::suggest))
                                            .executes(ctx -> questsHandler.check(ctx.getSource(),
                                                    rawArgs(ctx.getInput()))))
                            )
                            .then(Commands.literal("info")
                                    .executes(ctx -> questsHandler.check(ctx.getSource(), rawArgs(ctx.getInput())))
                            )
                            .then(Commands.literal("journal")
                                    .executes(ctx -> questsHandler.check(ctx.getSource(), rawArgs(ctx.getInput())))
                            )
                            .then(Commands.literal("choice")
                                    .executes(ctx -> questsHandler.check(ctx.getSource(), rawArgs(ctx.getInput())))
                                    .then(Commands.argument("value", StringArgumentType.greedyString())
                                            .suggests(suggestFor(questsHandler::suggest))
                                            .executes(ctx -> questsHandler.check(ctx.getSource(),
                                                    rawArgs(ctx.getInput()))))
                            )
                            .then(Commands.literal("editor")
                                    .requires(s -> questsHandler.canEditor(s, "editor"))
                                    .executes(ctx -> questsHandler.check(ctx.getSource(), rawArgs(ctx.getInput())))
                            )
                            .then(Commands.literal("actions")
                                    .requires(s -> questsHandler.canEditor(s, "actions"))
                                    .executes(ctx -> questsHandler.check(ctx.getSource(), rawArgs(ctx.getInput())))
                            )
                            .then(Commands.literal("conditions")
                                    .requires(s -> questsHandler.canEditor(s, "conditions"))
                                    .executes(ctx -> questsHandler.check(ctx.getSource(), rawArgs(ctx.getInput())))
                            )
            );
            // Aliases are registered as executes+redirect: Brigadier 1.3's parseNodes only pulls the
            // redirect target's command when trailing input exists, so a bare redirect node resolves to
            // nothing. The executes covers the no-argument form; the redirect forwards subcommands, whose
            // per-node requires predicates still gate execution through the redirected parse.
            dispatcher.register(
                    Commands.literal("qs")
                            .requires(Commands.hasPermission(Commands.LEVEL_ALL))
                            .executes(ctx -> questsHandler.check(ctx.getSource(), rawArgs(ctx.getInput())))
                            .redirect(questsNode)
            );

            final LiteralCommandNode<CommandSourceStack> qaNode = dispatcher.register(
                    Commands.literal("questadmin")
                            .requires(s -> questadminHandler.hasAdminRoot(s))
                            .executes(ctx -> questadminHandler.check(ctx.getSource(), rawArgs(ctx.getInput())))
                            .then(Commands.literal("stats")
                                    .requires(s -> questadminHandler.hasAdminNode(s, "stats"))
                                    .executes(ctx -> questadminHandler.check(ctx.getSource(),
                                            rawArgs(ctx.getInput())))
                                    .then(Commands.argument("player", StringArgumentType.word())
                                            .suggests(suggestFor(questadminHandler::suggest))
                                            .executes(ctx -> questadminHandler.check(ctx.getSource(),
                                                    rawArgs(ctx.getInput())))
                                    )
                            )
                            .then(Commands.literal("give")
                                    .requires(s -> questadminHandler.hasAdminNode(s, "give"))
                                    .executes(ctx -> questadminHandler.check(ctx.getSource(),
                                            rawArgs(ctx.getInput())))
                                    .then(Commands.argument("player", StringArgumentType.word())
                                            .suggests(suggestFor(questadminHandler::suggest))
.then(Commands.argument("quest", StringArgumentType.greedyString())
                                            .suggests(suggestFor(questadminHandler::suggest))
                                            .executes(ctx -> questadminHandler.check(ctx.getSource(),
                                                    rawArgs(ctx.getInput()))))
                                    )
                            )
                            .then(Commands.literal("quit")
                                    .requires(s -> questadminHandler.hasAdminNode(s, "quit"))
                                    .executes(ctx -> questadminHandler.check(ctx.getSource(),
                                            rawArgs(ctx.getInput())))
                                    .then(Commands.argument("player", StringArgumentType.word())
                                            .suggests(suggestFor(questadminHandler::suggest))
                                            .then(Commands.argument("quest", StringArgumentType.greedyString())
                                                    .suggests(suggestFor(questadminHandler::suggest))
                                                    .executes(ctx -> questadminHandler.check(ctx.getSource(),
                                                            rawArgs(ctx.getInput()))))
                                    )
                            )
                            .then(Commands.literal("points")
                                    .requires(s -> questadminHandler.hasAdminNode(s, "points"))
                                    .executes(ctx -> questadminHandler.check(ctx.getSource(),
                                            rawArgs(ctx.getInput())))
                                    .then(Commands.argument("player", StringArgumentType.word())
                                            .suggests(suggestFor(questadminHandler::suggest))
                                            .executes(ctx -> questadminHandler.check(ctx.getSource(),
                                                    rawArgs(ctx.getInput())))
                                            .then(Commands.argument("amount", StringArgumentType.word())
                                                    .suggests(suggestFor(questadminHandler::suggest))
                                                    .executes(ctx -> questadminHandler.check(ctx.getSource(),
                                                            rawArgs(ctx.getInput()))))
                                    )
                            )
                            .then(Commands.literal("takepoints")
                                    .requires(s -> questadminHandler.hasAdminNode(s, "takepoints"))
                                    .executes(ctx -> questadminHandler.check(ctx.getSource(),
                                            rawArgs(ctx.getInput())))
                                    .then(Commands.argument("player", StringArgumentType.word())
                                            .suggests(suggestFor(questadminHandler::suggest))
                                            .then(Commands.argument("amount", StringArgumentType.word())
                                                    .suggests(suggestFor(questadminHandler::suggest))
                                                    .executes(ctx -> questadminHandler.check(ctx.getSource(),
                                                            rawArgs(ctx.getInput()))))
                                    )
                            )
                            .then(Commands.literal("givepoints")
                                    .requires(s -> questadminHandler.hasAdminNode(s, "givepoints"))
                                    .executes(ctx -> questadminHandler.check(ctx.getSource(),
                                            rawArgs(ctx.getInput())))
                                    .then(Commands.argument("player", StringArgumentType.word())
                                            .suggests(suggestFor(questadminHandler::suggest))
                                            .then(Commands.argument("amount", StringArgumentType.word())
                                                    .suggests(suggestFor(questadminHandler::suggest))
                                                    .executes(ctx -> questadminHandler.check(ctx.getSource(),
                                                            rawArgs(ctx.getInput()))))
                                    )
                            )
                            .then(Commands.literal("finish")
                                    .requires(s -> questadminHandler.hasAdminNode(s, "finish"))
                                    .executes(ctx -> questadminHandler.check(ctx.getSource(),
                                            rawArgs(ctx.getInput())))
                                    .then(Commands.argument("player", StringArgumentType.word())
                                            .suggests(suggestFor(questadminHandler::suggest))
                                            .then(Commands.argument("quest", StringArgumentType.greedyString())
                                                    .suggests(suggestFor(questadminHandler::suggest))
                                                    .executes(ctx -> questadminHandler.check(ctx.getSource(),
                                                            rawArgs(ctx.getInput()))))
                                    )
                            )
                            .then(Commands.literal("nextstage")
                                    .requires(s -> questadminHandler.hasAdminNode(s, "nextstage"))
                                    .executes(ctx -> questadminHandler.check(ctx.getSource(),
                                            rawArgs(ctx.getInput())))
                                    .then(Commands.argument("player", StringArgumentType.word())
                                            .suggests(suggestFor(questadminHandler::suggest))
                                            .then(Commands.argument("quest", StringArgumentType.greedyString())
                                                    .suggests(suggestFor(questadminHandler::suggest))
                                                    .executes(ctx -> questadminHandler.check(ctx.getSource(),
                                                            rawArgs(ctx.getInput()))))
                                    )
                            )
                            .then(Commands.literal("setstage")
                                    .requires(s -> questadminHandler.hasAdminNode(s, "setstage"))
                                    .executes(ctx -> questadminHandler.check(ctx.getSource(),
                                            rawArgs(ctx.getInput())))
                                    .then(Commands.argument("player", StringArgumentType.word())
                                            .suggests(suggestFor(questadminHandler::suggest))
                                            .then(Commands.argument("rest", StringArgumentType.greedyString())
                                                    .suggests(suggestFor(questadminHandler::suggest))
                                                    .executes(ctx -> questadminHandler.check(ctx.getSource(),
                                                            rawArgs(ctx.getInput()))))
                                    )
                            )
                            .then(Commands.literal("reset")
                                    .requires(s -> questadminHandler.hasAdminNode(s, "reset"))
                                    .executes(ctx -> questadminHandler.check(ctx.getSource(),
                                            rawArgs(ctx.getInput())))
                                    .then(Commands.argument("player", StringArgumentType.word())
                                            .suggests(suggestFor(questadminHandler::suggest))
                                            .executes(ctx -> questadminHandler.check(ctx.getSource(),
                                                    rawArgs(ctx.getInput()))))
                            )
                            .then(Commands.literal("remove")
                                    .requires(s -> questadminHandler.hasAdminNode(s, "remove"))
                                    .executes(ctx -> questadminHandler.check(ctx.getSource(),
                                            rawArgs(ctx.getInput())))
                                    .then(Commands.argument("player", StringArgumentType.word())
                                            .suggests(suggestFor(questadminHandler::suggest))
                                            .then(Commands.argument("quest", StringArgumentType.greedyString())
                                                    .suggests(suggestFor(questadminHandler::suggest))
                                                    .executes(ctx -> questadminHandler.check(ctx.getSource(),
                                                            rawArgs(ctx.getInput()))))
                                    )
                            )
                            .then(Commands.literal("reload")
                                    .requires(s -> questadminHandler.hasAdminNode(s, "reload"))
                                    .executes(ctx -> questadminHandler.check(ctx.getSource(),
                                            rawArgs(ctx.getInput())))
                            )
            );
            dispatcher.register(
                    Commands.literal("qa")
                            .requires(s -> questadminHandler.hasAdminRoot(s))
                            .executes(ctx -> questadminHandler.check(ctx.getSource(), rawArgs(ctx.getInput())))
                            .redirect(qaNode)
            );
        });
    }
}