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

package me.pikamug.quests.commands.quests;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.commands.quests.subcommands.QuestsActionsCommand;
import me.pikamug.quests.commands.quests.subcommands.QuestsChoiceCommand;
import me.pikamug.quests.commands.quests.subcommands.QuestsConditionsCommand;
import me.pikamug.quests.commands.quests.subcommands.QuestsEditorCommand;
import me.pikamug.quests.commands.quests.subcommands.QuestsInfoCommand;
import me.pikamug.quests.commands.quests.subcommands.QuestsJournalCommand;
import me.pikamug.quests.commands.quests.subcommands.QuestsListCommand;
import me.pikamug.quests.commands.quests.subcommands.QuestsQuitCommand;
import me.pikamug.quests.commands.quests.subcommands.QuestsStatsCommand;
import me.pikamug.quests.commands.quests.subcommands.QuestsTakeCommand;
import me.pikamug.quests.commands.quests.subcommands.QuestsTopCommand;
import me.pikamug.quests.commands.QuestsSubCommand;
import me.pikamug.quests.util.BukkitLanguage;
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

public class QuestsCommandHandler {

    private final BukkitQuestsPlugin plugin;
    private final Map<String, QuestsSubCommand> subCommands;

    public QuestsCommandHandler(BukkitQuestsPlugin plugin) {
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
                        new QuestsInfoCommand(plugin),
                        new QuestsChoiceCommand())
                .collect(Collectors.toMap(QuestsSubCommand::getName, Function.identity()));
    }

    public boolean check(final CommandSender cs, final String[] args) {
        if (args.length == 0) {
            printHelp(cs);
            return true;
        }
        for (Map.Entry<String, QuestsSubCommand> cmd : subCommands.entrySet()) {
            if (args[0].equalsIgnoreCase(cmd.getKey()) || args[0].equalsIgnoreCase(cmd.getValue().getNameI18N())) {
                if (args.length < cmd.getValue().getMaxArguments()) {
                    cs.sendMessage(getCommandUsage(cs, args[0]));
                }
                cmd.getValue().execute(cs, args);
                return true;
            }
        }
        cs.sendMessage(ChatColor.YELLOW + BukkitLanguage.get(cs, "questsUnknownCommand"));
        return true;
    }

    public List<String> suggest(final CommandSender cs, final String[] args) {
        if (args.length == 1) {
            final List<String> results = new ArrayList<>();
            for (Map.Entry<String, QuestsSubCommand> cmd : subCommands.entrySet()) {
                if (cmd.getKey().startsWith(args[0]) || cmd.getValue().getNameI18N().startsWith(args[0])) {
                    results.add(cmd.getValue().getNameI18N());
                }
            }
            return results;
        }
        for (Map.Entry<String, QuestsSubCommand> cmd : subCommands.entrySet()) {
            if (args[0].equalsIgnoreCase(cmd.getKey()) || args[0].equalsIgnoreCase(cmd.getValue().getNameI18N())) {
                return cmd.getValue().tabComplete(cs, args);
            }
        }
        return Collections.emptyList();
    }

    private void printHelp(final CommandSender cs) {
        if (!cs.hasPermission("quests.quests")) {
            cs.sendMessage(ChatColor.RED + BukkitLanguage.get("noPermission"));
            return;
        }
        cs.sendMessage(ChatColor.GOLD + BukkitLanguage.get("questHelpTitle"));
        cs.sendMessage(ChatColor.YELLOW + "/quests " + BukkitLanguage.get("questDisplayHelp"));
        for (final QuestsSubCommand cmd : subCommands.values()) {
            if (cmd.getName().equals("choice")) {
                continue;
            }
            cs.sendMessage(ChatColor.YELLOW + "/quests " + cmd.getDescription().replace("<command>", ChatColor.GOLD
                    + (plugin.getConfigSettings().canTranslateSubCommands() ? cmd.getNameI18N() : cmd.getName())
                    + ChatColor.YELLOW));
        }
        if (cs instanceof Player) {
            cs.sendMessage(ChatColor.DARK_AQUA + "/quest " + ChatColor.YELLOW + BukkitLanguage.get(cs, "COMMAND_QUEST_HELP"));
            if (cs.hasPermission("quests.questinfo")) {
                cs.sendMessage(ChatColor.DARK_AQUA + "/quest " + ChatColor.YELLOW
                        + BukkitLanguage.get(cs, "COMMAND_QUESTINFO_HELP"));
            }
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED
                    + BukkitLanguage.get(cs, "COMMAND_QUESTADMIN_HELP"));
        }
    }

    private String getCommandUsage(final CommandSender cs, final String cmd) {
        return ChatColor.RED + BukkitLanguage.get(cs, "usage") + ": " + ChatColor.YELLOW + "/quests "
                + BukkitLanguage.get(cs, BukkitLanguage.getKeyFromPrefix("COMMAND_", cmd) + "_HELP")
                .replace("<command>", cmd.toLowerCase());
    }
}
