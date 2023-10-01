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

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.commands.quests.subcommands.BukkitQuestsActionsCommand;
import me.pikamug.quests.commands.quests.subcommands.BukkitQuestsChoiceCommand;
import me.pikamug.quests.commands.quests.subcommands.BukkitQuestsConditionsCommand;
import me.pikamug.quests.commands.quests.subcommands.BukkitQuestsEditorCommand;
import me.pikamug.quests.commands.quests.subcommands.BukkitQuestsInfoCommand;
import me.pikamug.quests.commands.quests.subcommands.BukkitQuestsJournalCommand;
import me.pikamug.quests.commands.quests.subcommands.BukkitQuestsListCommand;
import me.pikamug.quests.commands.quests.subcommands.BukkitQuestsQuitCommand;
import me.pikamug.quests.commands.quests.subcommands.BukkitQuestsStatsCommand;
import me.pikamug.quests.commands.quests.subcommands.BukkitQuestsTakeCommand;
import me.pikamug.quests.commands.quests.subcommands.BukkitQuestsTopCommand;
import me.pikamug.quests.commands.BukkitQuestsSubCommand;
import me.pikamug.quests.util.BukkitLang;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BukkitQuestsCommandHandler {

    private final BukkitQuestsPlugin plugin;
    private final Map<String, BukkitQuestsSubCommand> subCommands;

    public BukkitQuestsCommandHandler(BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
        subCommands = Stream.of(new BukkitQuestsListCommand(plugin),
                        new BukkitQuestsTakeCommand(plugin),
                        new BukkitQuestsQuitCommand(plugin),
                        new BukkitQuestsStatsCommand(plugin),
                        new BukkitQuestsJournalCommand(plugin),
                        new BukkitQuestsTopCommand(plugin),
                        new BukkitQuestsEditorCommand(plugin),
                        new BukkitQuestsActionsCommand(plugin),
                        new BukkitQuestsConditionsCommand(plugin),
                        new BukkitQuestsInfoCommand(plugin),
                        new BukkitQuestsChoiceCommand())
                .collect(Collectors.toMap(BukkitQuestsSubCommand::getName, Function.identity()));
    }

    public boolean check(final CommandSender cs, final String[] args) {
        if (args.length == 0) {
            printHelp(cs);
            return true;
        }
        for (Map.Entry<String, BukkitQuestsSubCommand> cmd : subCommands.entrySet()) {
            if (args[0].equalsIgnoreCase(cmd.getKey()) || args[0].equalsIgnoreCase(cmd.getValue().getNameI18N())) {
                if (args.length < cmd.getValue().getMaxArguments()) {
                    cs.sendMessage(getCommandUsage(cs, args[0]));
                }
                cmd.getValue().execute(cs, args);
                return true;
            }
        }
        cs.sendMessage(ChatColor.YELLOW + BukkitLang.get(cs, "questsUnknownCommand"));
        return true;
    }

    public List<String> suggest(final CommandSender cs, final String[] args) {
        if (args.length == 1) {
            final List<String> results = new ArrayList<>();
            for (Map.Entry<String, BukkitQuestsSubCommand> cmd : subCommands.entrySet()) {
                if (cmd.getKey().startsWith(args[0]) || cmd.getValue().getNameI18N().startsWith(args[0])) {
                    results.add(cmd.getValue().getNameI18N());
                }
            }
            return results;
        }
        for (Map.Entry<String, BukkitQuestsSubCommand> cmd : subCommands.entrySet()) {
            if (args[0].equalsIgnoreCase(cmd.getKey()) || args[0].equalsIgnoreCase(cmd.getValue().getNameI18N())) {
                return cmd.getValue().tabComplete(cs, args);
            }
        }
        return Collections.emptyList();
    }

    private void printHelp(final CommandSender cs) {
        if (!cs.hasPermission("quests.quests")) {
            cs.sendMessage(ChatColor.RED + BukkitLang.get("noPermission"));
            return;
        }
        cs.sendMessage(ChatColor.GOLD + BukkitLang.get("questHelpTitle"));
        cs.sendMessage(ChatColor.YELLOW + "/quests " + BukkitLang.get("questDisplayHelp"));
        for (final BukkitQuestsSubCommand cmd : subCommands.values()) {
            if (cmd.getName().equals("choice")) {
                continue;
            }
            cs.sendMessage(ChatColor.YELLOW + "/quests " + cmd.getDescription().replace("<command>", ChatColor.GOLD
                    + (plugin.getConfigSettings().canTranslateSubCommands() ? cmd.getNameI18N() : cmd.getName())
                    + ChatColor.YELLOW));
        }
        if (cs instanceof Player) {
            cs.sendMessage(ChatColor.DARK_AQUA + "/quest " + ChatColor.YELLOW + BukkitLang.get(cs, "COMMAND_QUEST_HELP"));
            if (cs.hasPermission("quests.questinfo")) {
                cs.sendMessage(ChatColor.DARK_AQUA + "/quest " + ChatColor.YELLOW
                        + BukkitLang.get(cs, "COMMAND_QUESTINFO_HELP"));
            }
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED
                    + BukkitLang.get(cs, "COMMAND_QUESTADMIN_HELP"));
        }
    }

    private String getCommandUsage(final CommandSender cs, final String cmd) {
        return ChatColor.RED + BukkitLang.get(cs, "usage") + ": " + ChatColor.YELLOW + "/quests "
                + BukkitLang.get(cs, BukkitLang.getKeyFromPrefix("COMMAND_", cmd) + "_HELP")
                .replace("<command>", cmd.toLowerCase());
    }
}
