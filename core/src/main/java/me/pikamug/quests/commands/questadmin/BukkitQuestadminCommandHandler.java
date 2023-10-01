/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.commands.questadmin;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.commands.questadmin.subcommands.BukkitQuestadminFinishCommand;
import me.pikamug.quests.commands.questadmin.subcommands.BukkitQuestadminGiveCommand;
import me.pikamug.quests.commands.questadmin.subcommands.BukkitQuestadminGivepointsCommand;
import me.pikamug.quests.commands.questadmin.subcommands.BukkitQuestadminNextstageCommand;
import me.pikamug.quests.commands.questadmin.subcommands.BukkitQuestadminPointsCommand;
import me.pikamug.quests.commands.questadmin.subcommands.BukkitQuestadminPointsallCommand;
import me.pikamug.quests.commands.questadmin.subcommands.BukkitQuestadminQuitCommand;
import me.pikamug.quests.commands.questadmin.subcommands.BukkitQuestadminReloadCommand;
import me.pikamug.quests.commands.questadmin.subcommands.BukkitQuestadminRemoveCommand;
import me.pikamug.quests.commands.questadmin.subcommands.BukkitQuestadminResetCommand;
import me.pikamug.quests.commands.questadmin.subcommands.BukkitQuestadminSetstageCommand;
import me.pikamug.quests.commands.questadmin.subcommands.BukkitQuestadminStatsCommand;
import me.pikamug.quests.commands.questadmin.subcommands.BukkitQuestadminTakepointsCommand;
import me.pikamug.quests.commands.BukkitQuestsSubCommand;
import me.pikamug.quests.util.BukkitLang;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BukkitQuestadminCommandHandler {

    private final BukkitQuestsPlugin plugin;
    private final Map<String, BukkitQuestsSubCommand> subCommands;

    public BukkitQuestadminCommandHandler(BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
        subCommands = Stream.of(new BukkitQuestadminStatsCommand(plugin),
                        new BukkitQuestadminGiveCommand(plugin),
                        new BukkitQuestadminQuitCommand(plugin),
                        new BukkitQuestadminPointsCommand(plugin),
                        new BukkitQuestadminTakepointsCommand(plugin),
                        new BukkitQuestadminGivepointsCommand(plugin),
                        new BukkitQuestadminPointsallCommand(plugin),
                        new BukkitQuestadminFinishCommand(plugin),
                        new BukkitQuestadminNextstageCommand(plugin),
                        new BukkitQuestadminSetstageCommand(plugin),
                        new BukkitQuestadminResetCommand(plugin),
                        new BukkitQuestadminRemoveCommand(plugin),
                        new BukkitQuestadminReloadCommand(plugin))
                .collect(Collectors.toMap(BukkitQuestsSubCommand::getName, Function.identity()));
    }

    public boolean check(final CommandSender cs, final String[] args) {
        if (args.length == 0) {
            printAdminHelp(cs);
            return true;
        }
        for (Map.Entry<String, BukkitQuestsSubCommand> cmd : subCommands.entrySet()) {
            if (args[0].equalsIgnoreCase(cmd.getKey()) || args[0].equalsIgnoreCase(cmd.getValue().getNameI18N())) {
                if (args.length < cmd.getValue().getMaxArguments()) {
                    cs.sendMessage(getAdminCommandUsage(cs, args[0]));
                }
                cmd.getValue().execute(cs, args);
                return true;
            }
        }
        cs.sendMessage(ChatColor.YELLOW + BukkitLang.get("questsUnknownAdminCommand"));
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

    private void printAdminHelp(final CommandSender cs) {
        if (!(cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin"))) {
            cs.sendMessage(ChatColor.RED + BukkitLang.get("noPermission"));
            return;
        }
        cs.sendMessage(ChatColor.GOLD + BukkitLang.get("questAdminHelpTitle"));
        cs.sendMessage(ChatColor.YELLOW + "/questadmin" + ChatColor.RED + " " + BukkitLang.get("COMMAND_QUESTADMIN_HELP"));
        final boolean translateSubCommands = plugin.getConfigSettings().canTranslateSubCommands();
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.stats")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED + BukkitLang.get("COMMAND_QUESTADMIN_STATS_HELP")
                    .replace("<command>", ChatColor.GOLD + (translateSubCommands ? BukkitLang.get("COMMAND_QUESTADMIN_STATS")
                            : "stats") + ChatColor.RED));
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.give")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED + BukkitLang.get("COMMAND_QUESTADMIN_GIVE_HELP")
                    .replace("<command>", ChatColor.GOLD + (translateSubCommands ? BukkitLang.get("COMMAND_QUESTADMIN_GIVE")
                            : "give") + ChatColor.RED));
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.quit")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED + BukkitLang.get("COMMAND_QUESTADMIN_QUIT_HELP")
                    .replace("<command>", ChatColor.GOLD + (translateSubCommands ? BukkitLang.get("COMMAND_QUESTADMIN_QUIT")
                            : "quit") + ChatColor.RED));
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.points")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED
                    + BukkitLang.get("COMMAND_QUESTADMIN_POINTS_HELP")
                    .replace("<points>", BukkitLang.get("questPoints"))
                    .replace("<command>", ChatColor.GOLD
                            + (translateSubCommands ? BukkitLang.get("COMMAND_QUESTADMIN_POINTS") : "points") + ChatColor.RED));
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.takepoints")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED
                    + BukkitLang.get("COMMAND_QUESTADMIN_TAKEPOINTS_HELP")
                    .replace("<points>", BukkitLang.get("questPoints"))
                    .replace("<command>", ChatColor.GOLD
                            + (translateSubCommands ? BukkitLang.get("COMMAND_QUESTADMIN_TAKEPOINTS") : "takepoints")
                            + ChatColor.RED));
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.givepoints")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED
                    + BukkitLang.get("COMMAND_QUESTADMIN_GIVEPOINTS_HELP")
                    .replace("<points>", BukkitLang.get("questPoints"))
                    .replace("<command>", ChatColor.GOLD
                            + (translateSubCommands ? BukkitLang.get("COMMAND_QUESTADMIN_GIVEPOINTS") : "givepoints")
                            + ChatColor.RED));
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.pointsall")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED
                    + BukkitLang.get("COMMAND_QUESTADMIN_POINTSALL_HELP")
                    .replace("<points>", BukkitLang.get("questPoints"))
                    .replace("<command>", ChatColor.GOLD
                            + (translateSubCommands ? BukkitLang.get("COMMAND_QUESTADMIN_POINTSALL") : "pointsall") + ChatColor.RED));
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.finish")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED
                    + BukkitLang.get("COMMAND_QUESTADMIN_FINISH_HELP").replace("<command>", ChatColor.GOLD
                    + (translateSubCommands ? BukkitLang.get("COMMAND_QUESTADMIN_FINISH") : "finish") + ChatColor.RED));
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.nextstage")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED
                    + BukkitLang.get("COMMAND_QUESTADMIN_NEXTSTAGE_HELP").replace("<command>", ChatColor.GOLD
                    + (translateSubCommands ? BukkitLang.get("COMMAND_QUESTADMIN_NEXTSTAGE") : "nextstage") + ChatColor.RED));
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.setstage")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED
                    + BukkitLang.get("COMMAND_QUESTADMIN_SETSTAGE_HELP").replace("<command>", ChatColor.GOLD
                    + (translateSubCommands ? BukkitLang.get("COMMAND_QUESTADMIN_SETSTAGE") : "setstage") + ChatColor.RED));
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.reset")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED+ BukkitLang.get("COMMAND_QUESTADMIN_RESET_HELP")
                    .replace("<command>", ChatColor.GOLD + (translateSubCommands ? BukkitLang.get("COMMAND_QUESTADMIN_RESET")
                            : "reset") + ChatColor.RED));
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.remove")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED
                    + BukkitLang.get("COMMAND_QUESTADMIN_REMOVE_HELP").replace("<command>", ChatColor.GOLD
                    + (translateSubCommands ? BukkitLang.get("COMMAND_QUESTADMIN_REMOVE") : "remove") + ChatColor.RED));
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.reload")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED
                    + BukkitLang.get("COMMAND_QUESTADMIN_RELOAD_HELP").replace("<command>", ChatColor.GOLD
                    + (translateSubCommands ? BukkitLang.get("COMMAND_QUESTADMIN_RELOAD") : "reload") + ChatColor.RED));
        }
    }

    private String getAdminCommandUsage(final CommandSender cs, final String cmd) {
        return ChatColor.RED + BukkitLang.get(cs, "usage") + ": " + ChatColor.YELLOW + "/questadmin "
                + BukkitLang.get(cs, BukkitLang.getKeyFromPrefix("COMMAND_QUESTADMIN_", cmd) + "_HELP")
                .replace("<command>", cmd.toLowerCase());
    }
}
