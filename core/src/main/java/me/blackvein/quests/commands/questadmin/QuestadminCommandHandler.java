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

package me.blackvein.quests.commands.questadmin;

import me.blackvein.quests.Quests;
import me.blackvein.quests.commands.questadmin.subcommands.QuestadminFinishCommand;
import me.blackvein.quests.commands.questadmin.subcommands.QuestadminGiveCommand;
import me.blackvein.quests.commands.questadmin.subcommands.QuestadminGivepointsCommand;
import me.blackvein.quests.commands.questadmin.subcommands.QuestadminNextstageCommand;
import me.blackvein.quests.commands.questadmin.subcommands.QuestadminPointsCommand;
import me.blackvein.quests.commands.questadmin.subcommands.QuestadminPointsallCommand;
import me.blackvein.quests.commands.questadmin.subcommands.QuestadminQuitCommand;
import me.blackvein.quests.commands.questadmin.subcommands.QuestadminReloadCommand;
import me.blackvein.quests.commands.questadmin.subcommands.QuestadminRemoveCommand;
import me.blackvein.quests.commands.questadmin.subcommands.QuestadminResetCommand;
import me.blackvein.quests.commands.questadmin.subcommands.QuestadminSetstageCommand;
import me.blackvein.quests.commands.questadmin.subcommands.QuestadminStatsCommand;
import me.blackvein.quests.commands.questadmin.subcommands.QuestadminTakepointsCommand;
import me.blackvein.quests.commands.QuestsSubCommand;
import me.blackvein.quests.util.Lang;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QuestadminCommandHandler {

    private final Quests plugin;
    private final Map<String, QuestsSubCommand> subCommands;

    public QuestadminCommandHandler(Quests plugin) {
        this.plugin = plugin;
        subCommands = Stream.of(new QuestadminStatsCommand(plugin),
                        new QuestadminGiveCommand(plugin),
                        new QuestadminQuitCommand(plugin),
                        new QuestadminPointsCommand(plugin),
                        new QuestadminTakepointsCommand(plugin),
                        new QuestadminGivepointsCommand(plugin),
                        new QuestadminPointsallCommand(plugin),
                        new QuestadminFinishCommand(plugin),
                        new QuestadminNextstageCommand(plugin),
                        new QuestadminSetstageCommand(plugin),
                        new QuestadminResetCommand(plugin),
                        new QuestadminRemoveCommand(plugin),
                        new QuestadminReloadCommand(plugin))
                .collect(Collectors.toMap(QuestsSubCommand::getName, Function.identity()));
    }

    public boolean check(final CommandSender cs, final String[] args) {
        if (args.length == 0) {
            printAdminHelp(cs);
            return true;
        }
        for (Map.Entry<String, QuestsSubCommand> cmd : subCommands.entrySet()) {
            if (args[0].equalsIgnoreCase(cmd.getKey()) || args[0].equalsIgnoreCase(cmd.getValue().getNameI18N())) {
                if (args.length < cmd.getValue().getMaxArguments()) {
                    cs.sendMessage(getAdminCommandUsage(cs, args[0]));
                }
                cmd.getValue().execute(cs, args);
                return true;
            }
        }
        cs.sendMessage(ChatColor.YELLOW + Lang.get("questsUnknownAdminCommand"));
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

    private void printAdminHelp(final CommandSender cs) {
        if (!(cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin"))) {
            cs.sendMessage(ChatColor.RED + Lang.get("noPermission"));
            return;
        }
        cs.sendMessage(ChatColor.GOLD + Lang.get("questAdminHelpTitle"));
        cs.sendMessage(ChatColor.YELLOW + "/questadmin" + ChatColor.RED + " " + Lang.get("COMMAND_QUESTADMIN_HELP"));
        final boolean translateSubCommands = plugin.getSettings().canTranslateSubCommands();
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.stats")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED + Lang.get("COMMAND_QUESTADMIN_STATS_HELP")
                    .replace("<command>", ChatColor.GOLD + (translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_STATS")
                            : "stats") + ChatColor.RED));
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.give")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED + Lang.get("COMMAND_QUESTADMIN_GIVE_HELP")
                    .replace("<command>", ChatColor.GOLD + (translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_GIVE")
                            : "give") + ChatColor.RED));
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.quit")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED + Lang.get("COMMAND_QUESTADMIN_QUIT_HELP")
                    .replace("<command>", ChatColor.GOLD + (translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_QUIT")
                            : "quit") + ChatColor.RED));
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.points")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED
                    + Lang.get("COMMAND_QUESTADMIN_POINTS_HELP")
                    .replace("<points>", Lang.get("questPoints"))
                    .replace("<command>", ChatColor.GOLD
                            + (translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_POINTS") : "points") + ChatColor.RED));
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.takepoints")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED
                    + Lang.get("COMMAND_QUESTADMIN_TAKEPOINTS_HELP")
                    .replace("<points>", Lang.get("questPoints"))
                    .replace("<command>", ChatColor.GOLD
                            + (translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_TAKEPOINTS") : "takepoints")
                            + ChatColor.RED));
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.givepoints")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED
                    + Lang.get("COMMAND_QUESTADMIN_GIVEPOINTS_HELP")
                    .replace("<points>", Lang.get("questPoints"))
                    .replace("<command>", ChatColor.GOLD
                            + (translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_GIVEPOINTS") : "givepoints")
                            + ChatColor.RED));
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.pointsall")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED
                    + Lang.get("COMMAND_QUESTADMIN_POINTSALL_HELP")
                    .replace("<points>", Lang.get("questPoints"))
                    .replace("<command>", ChatColor.GOLD
                            + (translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_POINTSALL") : "pointsall") + ChatColor.RED));
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.finish")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED
                    + Lang.get("COMMAND_QUESTADMIN_FINISH_HELP").replace("<command>", ChatColor.GOLD
                    + (translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_FINISH") : "finish") + ChatColor.RED));
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.nextstage")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED
                    + Lang.get("COMMAND_QUESTADMIN_NEXTSTAGE_HELP").replace("<command>", ChatColor.GOLD
                    + (translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_NEXTSTAGE") : "nextstage") + ChatColor.RED));
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.setstage")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED
                    + Lang.get("COMMAND_QUESTADMIN_SETSTAGE_HELP").replace("<command>", ChatColor.GOLD
                    + (translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_SETSTAGE") : "setstage") + ChatColor.RED));
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.reset")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED+ Lang.get("COMMAND_QUESTADMIN_RESET_HELP")
                    .replace("<command>", ChatColor.GOLD + (translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_RESET")
                            : "reset") + ChatColor.RED));
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.remove")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED
                    + Lang.get("COMMAND_QUESTADMIN_REMOVE_HELP").replace("<command>", ChatColor.GOLD
                    + (translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_REMOVE") : "remove") + ChatColor.RED));
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.reload")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED
                    + Lang.get("COMMAND_QUESTADMIN_RELOAD_HELP").replace("<command>", ChatColor.GOLD
                    + (translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_RELOAD") : "reload") + ChatColor.RED));
        }
    }

    private String getAdminCommandUsage(final CommandSender cs, final String cmd) {
        return ChatColor.RED + Lang.get(cs, "usage") + ": " + ChatColor.YELLOW + "/questadmin "
                + Lang.get(cs, Lang.getKeyFromPrefix("COMMAND_QUESTADMIN_", cmd) + "_HELP")
                .replace("<command>", cmd.toLowerCase());
    }
}
