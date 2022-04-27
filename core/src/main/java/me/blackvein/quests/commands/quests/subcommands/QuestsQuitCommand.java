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

package me.blackvein.quests.commands.quests.subcommands;

import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;
import me.blackvein.quests.commands.QuestsSubCommand;
import me.blackvein.quests.player.IQuester;
import me.blackvein.quests.quests.IQuest;
import me.blackvein.quests.util.Lang;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuestsQuitCommand extends QuestsSubCommand {

    private final Quests plugin;

    public QuestsQuitCommand(Quests plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "quit";
    }

    @Override
    public String getNameI18N() {
        return Lang.get("COMMAND_QUIT");
    }

    @Override
    public String getDescription() {
        return Lang.get("COMMAND_QUIT_HELP");
    }

    @Override
    public String getPermission() {
        return "quests.quit";
    }

    @Override
    public String getSyntax() {
        return "/quests quit";
    }

    @Override
    public int getMaxArguments() {
        return 2;
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        if (assertNonPlayer(cs)) {
            return;
        }
        final Player player = (Player) cs;
        if (player.hasPermission(getPermission())) {
            if (args.length == 1) {
                Lang.send(player, ChatColor.RED + Lang.get(player, "COMMAND_QUIT_HELP"));
                return;
            }
            final IQuester quester = plugin.getQuester(player.getUniqueId());
            if (!quester.getCurrentQuestsTemp().isEmpty()) {
                final IQuest quest = plugin.getQuestTemp(concatArgArray(args, 1, args.length - 1, ' '));
                if (quest != null) {
                    if (quest.getOptions().canAllowQuitting()) {
                        final String msg = ChatColor.YELLOW + Lang.get("questQuit").replace("<quest>",
                                ChatColor.DARK_PURPLE + quest.getName() + ChatColor.YELLOW);
                        quester.abandonQuest(quest, msg);
                    } else {
                        Lang.send(player, ChatColor.YELLOW + Lang.get(player, "questQuitDisabled"));
                    }
                } else {
                    Lang.send(player, ChatColor.RED + Lang.get(player, "questNotFound"));
                }
            } else {
                Lang.send(player, ChatColor.YELLOW + Lang.get(player, "noActiveQuest"));
            }
        } else {
            Lang.send(player, ChatColor.RED + Lang.get(player, "noPermission"));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender commandSender, String[] args) {
        if (args.length == 2) {
            final List<String> results = new ArrayList<>();
            if (commandSender instanceof Player) {
                final Quester quester = plugin.getQuester(((Player) commandSender).getUniqueId());
                if (quester != null) {
                    for (final IQuest quest : quester.getCurrentQuests().keySet()) {
                        if (quest.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                            results.add(ChatColor.stripColor(quest.getName()));
                        }
                    }
                }
            } else {
                for (final IQuest quest : plugin.getLoadedQuests()) {
                    if (quest.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        results.add(ChatColor.stripColor(quest.getName()));
                    }
                }
            }
            return results;
        }
        return Collections.emptyList();
    }
}
