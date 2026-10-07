/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.commands.quests;

import com.mojang.brigadier.Command;
import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.commands.FabricQuestsSubCommand;
import me.pikamug.quests.commands.quests.subcommands.FabricQuestsActionsCommand;
import me.pikamug.quests.commands.quests.subcommands.FabricQuestsChoiceCommand;
import me.pikamug.quests.commands.quests.subcommands.FabricQuestsConditionsCommand;
import me.pikamug.quests.commands.quests.subcommands.FabricQuestsEditorCommand;
import me.pikamug.quests.commands.quests.subcommands.FabricQuestsInfoCommand;
import me.pikamug.quests.commands.quests.subcommands.FabricQuestsJournalCommand;
import me.pikamug.quests.commands.quests.subcommands.FabricQuestsListCommand;
import me.pikamug.quests.commands.quests.subcommands.FabricQuestsQuitCommand;
import me.pikamug.quests.commands.quests.subcommands.FabricQuestsStatsCommand;
import me.pikamug.quests.commands.quests.subcommands.FabricQuestsTakeCommand;
import me.pikamug.quests.commands.quests.subcommands.FabricQuestsTopCommand;
import me.pikamug.quests.util.FabricLang;
import me.pikamug.quests.util.FabricMiscUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.pikamug.quests.commands.FabricQuestsSubCommand.lang;
import static me.pikamug.quests.commands.FabricQuestsSubCommand.reply;

public class FabricQuestsCommandHandler {

    private final FabricQuestsPlugin plugin;

    private final FabricQuestsSubCommand list;
    private final FabricQuestsSubCommand take;
    private final FabricQuestsSubCommand quit;
    private final FabricQuestsSubCommand stats;
    private final FabricQuestsSubCommand journal;
    private final FabricQuestsSubCommand top;
    private final FabricQuestsSubCommand editor;
    private final FabricQuestsSubCommand actions;
    private final FabricQuestsSubCommand conditions;
    private final FabricQuestsSubCommand info;
    private final FabricQuestsSubCommand choice;

    private final Map<String, FabricQuestsSubCommand> subCommands = new HashMap<>();

    public FabricQuestsCommandHandler(final FabricQuestsPlugin plugin) {
        this.plugin = plugin;
        list = new FabricQuestsListCommand(plugin);
        take = new FabricQuestsTakeCommand(plugin);
        quit = new FabricQuestsQuitCommand(plugin);
        stats = new FabricQuestsStatsCommand(plugin);
        journal = new FabricQuestsJournalCommand(plugin);
        top = new FabricQuestsTopCommand(plugin);
        editor = new FabricQuestsEditorCommand(plugin);
        actions = new FabricQuestsActionsCommand(plugin);
        conditions = new FabricQuestsConditionsCommand(plugin);
        info = new FabricQuestsInfoCommand(plugin);
        choice = new FabricQuestsChoiceCommand(plugin);
        subCommands.put("list", list);
        subCommands.put("take", take);
        subCommands.put("quit", quit);
        subCommands.put("stats", stats);
        subCommands.put("journal", journal);
        subCommands.put("top", top);
        subCommands.put("editor", editor);
        subCommands.put("actions", actions);
        subCommands.put("conditions", conditions);
        subCommands.put("info", info);
        subCommands.put("choice", choice);
    }

    public int check(final CommandSourceStack source, final String[] args) {
        if (args.length < 1) {
            return handleQuestsHelp(source);
        }
        final FabricQuestsSubCommand sub = subCommands.get(args[0].toLowerCase());
        if (sub == null) {
            return printQuestUnknownCommand(args[0], source);
        } else if (args.length < sub.getMaxArguments()) {
            return getQuestCommandUsage(source, sub.getName());
        } else if (args.length > sub.getMaxArguments() + 1 && !args[0].toLowerCase().equals("editor")) {
            return getQuestCommandUsage(source, sub.getName());
        } else {
            return sub.execute(source, args);
        }
    }

    public int handleQuestsHelp(final CommandSourceStack source) {
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

    private int printQuestUnknownCommand(final String sub, final CommandSourceStack source) {
        reply(source,() -> Component.literal(ChatFormatting.RED + lang(source, "questsUnknownCommand")), false);
        return Command.SINGLE_SUCCESS;
    }

    private int getQuestCommandUsage(final CommandSourceStack source, final String cmd) {
        reply(source,() -> Component.literal(ChatFormatting.RED + lang(source, "usage") + ": "
                + ChatFormatting.YELLOW + "/quests " + lang(source, FabricLang.getKeyFromPrefix("COMMAND_", cmd))
                + " " + lang(source, FabricLang.getKeyFromPrefix("COMMAND_", cmd) + "_HELP")
                .replace("<command>", cmd.toLowerCase())), false);
        return 0;
    }

    /**
     * Handles command tab completion
     *
     * @param source the source of this command
     * @param args the arguments for this command
     * @return the possible completions, or null to provide online player names
     */
    public List<String> suggest(final CommandSourceStack source, final String[] args) {
        if (args.length < 1) {
            return null;
        }
        final FabricQuestsSubCommand sub = subCommands.get(args[0].toLowerCase());
        if (sub == null) {
            final List<String> names = new ArrayList<>();
            for (final FabricQuestsSubCommand c : subCommands.values()) {
                names.add(plugin.getConfigSettings().canTranslateSubCommands() ? c.getNameI18N() : c.getName());
            }
            return names;
        }
        return sub.tabComplete(source, args);
    }

    public boolean canEditor(final CommandSourceStack source, final String sub) {
        if (!source.isPlayer()) return true;
        final ServerPlayer player = source.getPlayer();
        if (FabricMiscUtil.hasPermission(player, PermissionLevel.GAMEMASTERS)) return true;
        return plugin.getDependencies().hasPermission(player.getUUID(), "quests." + sub + ".editor");
    }

    private boolean hasAdminRoot(final CommandSourceStack source) {
        if (!source.isPlayer()) return true;
        final ServerPlayer player = source.getPlayer();
        if (FabricMiscUtil.hasPermission(player, PermissionLevel.GAMEMASTERS)) return true;
        return plugin.getDependencies().hasPermission(player.getUUID(), "quests.admin")
                || plugin.getDependencies().hasPermission(player.getUUID(), "quests.admin.*");
    }
}