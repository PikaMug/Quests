/*
 * Copyright (c) 2014 PikaMug and contributors. All rights reserved.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package me.blackvein.quests.commands.quests;

import me.blackvein.quests.Quests;
import me.blackvein.quests.commands.quests.subcommands.QuestsActionsCommand;
import me.blackvein.quests.commands.quests.subcommands.QuestsConditionsCommand;
import me.blackvein.quests.commands.quests.subcommands.QuestsEditorCommand;
import me.blackvein.quests.commands.quests.subcommands.QuestsInfoCommand;
import me.blackvein.quests.commands.quests.subcommands.QuestsJournalCommand;
import me.blackvein.quests.commands.quests.subcommands.QuestsListCommand;
import me.blackvein.quests.commands.quests.subcommands.QuestsQuitCommand;
import me.blackvein.quests.commands.quests.subcommands.QuestsStatsCommand;
import me.blackvein.quests.commands.quests.subcommands.QuestsTakeCommand;
import me.blackvein.quests.commands.quests.subcommands.QuestsTopCommand;
import me.blackvein.quests.commands.QuestsSubCommand;
import me.blackvein.quests.util.Lang;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QuestsCommandHandler {

    private final Quests plugin;
    private final Map<String, QuestsSubCommand> subCommands;

    public QuestsCommandHandler(Quests plugin) {
        this.plugin = plugin;
        subCommands = Stream.of(new QuestsListCommand(plugin),
                        new QuestsTakeCommand(plugin),
                        new QuestsQuitCommand(plugin),
                        new QuestsStatsCommand(plugin),
                        new QuestsJournalCommand(plugin),
                        new QuestsTopCommand(plugin),
                        new QuestsEditorCommand(plugin),
                        new QuestsActionsCommand(plugin),
                        new QuestsConditionsCommand(plugin),
                        new QuestsInfoCommand(plugin))
                .collect(Collectors.toMap(QuestsSubCommand::getName, Function.identity()));
    }

    public boolean check(final CommandSender cs, final String[] args) {
        if (args.length == 0) {
            printHelp(cs);
            return true;
        }
        for (Map.Entry<String, QuestsSubCommand> cmd : subCommands.entrySet()) {
            if (args[0].equalsIgnoreCase(cmd.getKey()) || args[0].equalsIgnoreCase(cmd.getValue().getNameI18N())) {
                cmd.getValue().execute(cs, args);
                return true;
            }
        }
        cs.sendMessage(ChatColor.YELLOW + Lang.get("questsUnknownCommand"));
        /*if (args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase(Lang.get("COMMAND_LIST"))) {
            new QuestsListCommand(plugin).execute(cs, args);
        } else if (args[0].equalsIgnoreCase("take") || args[0].equalsIgnoreCase(Lang.get("COMMAND_TAKE"))) {
            new QuestsTakeCommand(plugin).execute(cs, args);
        } else if (args[0].equalsIgnoreCase("quit") || args[0].equalsIgnoreCase(Lang.get("COMMAND_QUIT"))) {
            new QuestsQuitCommand(plugin).execute(cs, args);
        } else if (args[0].equalsIgnoreCase("stats") || args[0].equalsIgnoreCase(Lang.get("COMMAND_STATS"))) {
            new QuestsStatsCommand(plugin).execute(cs, args);
        } else if (args[0].equalsIgnoreCase("journal") || args[0].equalsIgnoreCase(Lang.get("COMMAND_JOURNAL"))) {
            new QuestsJournalCommand(plugin).execute(cs, args);
        } else if (args[0].equalsIgnoreCase("top") || args[0].equalsIgnoreCase(Lang.get("COMMAND_TOP"))) {
            new QuestsTopCommand(plugin).execute(cs, args);
        } else if (args[0].equalsIgnoreCase("editor") || args[0].equalsIgnoreCase(Lang.get("COMMAND_EDITOR"))) {
            new QuestsEditorCommand(plugin).execute(cs, args);
        } else if (args[0].startsWith("action") || args[0].startsWith("event")
                || args[0].startsWith(Lang.get("COMMAND_EVENTS_EDITOR"))) {
            new QuestsActionsCommand(plugin).execute(cs, args);
        } else if (args[0].startsWith("condition") || args[0].startsWith(Lang.get("COMMAND_CONDITIONS_EDITOR"))) {
            new QuestsConditionsCommand(plugin).execute(cs, args);
        } else if (args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase(Lang.get("COMMAND_INFO"))) {
            new QuestsInfoCommand(plugin).execute(cs, args);
        } else {
            cs.sendMessage(ChatColor.YELLOW + Lang.get("questsUnknownCommand"));
            return true;
        }*/
        return true;
    }

    private void printHelp(final CommandSender cs) {
        if (!cs.hasPermission("quests.quests")) {
            cs.sendMessage(ChatColor.RED + Lang.get("noPermission"));
            return;
        }
        cs.sendMessage(ChatColor.GOLD + Lang.get("questHelpTitle"));
        cs.sendMessage(ChatColor.YELLOW + "/quests " + Lang.get("questDisplayHelp"));
        for (final QuestsSubCommand cmd : subCommands.values()) {
            cs.sendMessage(ChatColor.YELLOW + "/quests " + cmd.getDescription().replace("<command>", ChatColor.GOLD
                    + (plugin.getSettings().canTranslateSubCommands() ? cmd.getNameI18N() : cmd.getName())
                    + ChatColor.YELLOW));
        }
        if (cs instanceof Player) {
            cs.sendMessage(ChatColor.DARK_AQUA + "/quest " + ChatColor.YELLOW + Lang.get("COMMAND_QUEST_HELP"));
            if (cs.hasPermission("quests.questinfo")) {
                cs.sendMessage(ChatColor.DARK_AQUA + "/quest " + ChatColor.YELLOW
                        + Lang.get("COMMAND_QUESTINFO_HELP"));
            }
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED
                    + Lang.get("COMMAND_QUESTADMIN_HELP"));
        }
    }
}
