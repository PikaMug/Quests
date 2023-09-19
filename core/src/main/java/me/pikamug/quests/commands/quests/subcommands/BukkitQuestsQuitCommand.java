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

package me.pikamug.quests.commands.quests.subcommands;

import me.pikamug.quests.player.BukkitQuester;
import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.commands.BukkitQuestsSubCommand;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.util.BukkitLang;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BukkitQuestsQuitCommand extends BukkitQuestsSubCommand {

    private final BukkitQuestsPlugin plugin;

    public BukkitQuestsQuitCommand(BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "quit";
    }

    @Override
    public String getNameI18N() {
        return BukkitLang.get("COMMAND_QUIT");
    }

    @Override
    public String getDescription() {
        return BukkitLang.get("COMMAND_QUIT_HELP");
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
        if (args.length == 1) {
            // Shows command usage
            return;
        }
        if (assertNonPlayer(cs)) {
            return;
        }
        final Player player = (Player) cs;
        if (player.hasPermission(getPermission())) {
            final Quester quester = plugin.getQuester(player.getUniqueId());
            if (!quester.getCurrentQuests().isEmpty()) {
                final Quest quest = plugin.getQuest(concatArgArray(args, 1, args.length - 1, ' '));
                if (quest != null) {
                    if (quest.getOptions().canAllowQuitting()) {
                        final String msg = ChatColor.YELLOW + BukkitLang.get(player, "questQuit").replace("<quest>",
                                ChatColor.DARK_PURPLE + quest.getName() + ChatColor.YELLOW);
                        quester.abandonQuest(quest, msg);
                    } else {
                        BukkitLang.send(player, ChatColor.YELLOW + BukkitLang.get(player, "questQuitDisabled"));
                    }
                } else {
                    BukkitLang.send(player, ChatColor.RED + BukkitLang.get(player, "questNotFound"));
                }
            } else {
                BukkitLang.send(player, ChatColor.YELLOW + BukkitLang.get(player, "noActiveQuest"));
            }
        } else {
            BukkitLang.send(player, ChatColor.RED + BukkitLang.get(player, "noPermission"));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender commandSender, String[] args) {
        if (args.length == 2) {
            final List<String> results = new ArrayList<>();
            if (commandSender instanceof Player) {
                final BukkitQuester quester = plugin.getQuester(((Player) commandSender).getUniqueId());
                if (quester != null) {
                    for (final Quest quest : quester.getCurrentQuests().keySet()) {
                        if (quest.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                            results.add(ChatColor.stripColor(quest.getName()));
                        }
                    }
                }
            } else {
                for (final Quest quest : plugin.getLoadedQuests()) {
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
