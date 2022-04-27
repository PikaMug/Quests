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

package me.blackvein.quests.commands.questadmin.subcommands;

import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;
import me.blackvein.quests.commands.QuestsSubCommand;
import me.blackvein.quests.player.IQuester;
import me.blackvein.quests.quests.IQuest;
import me.blackvein.quests.util.Lang;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class QuestadminQuitCommand extends QuestsSubCommand {

    private final Quests plugin;

    public QuestadminQuitCommand(Quests plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "quit";
    }

    @Override
    public String getNameI18N() {
        return Lang.get("COMMAND_QUESTADMIN_QUIT");
    }

    @Override
    public String getDescription() {
        return Lang.get("COMMAND_QUESTADMIN_QUIT_HELP");
    }

    @Override
    public String getPermission() {
        return "quests.admin.quit";
    }

    @Override
    public String getSyntax() {
        return "/questadmin quit";
    }

    @Override
    public int getMaxArguments() {
        return 3;
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.quit")) {
            OfflinePlayer target = getOfflinePlayer(args[1]);
            if (target == null) {
                try {
                    target = Bukkit.getOfflinePlayer(UUID.fromString(args[1]));
                } catch (final IllegalArgumentException e) {
                    cs.sendMessage(ChatColor.YELLOW + Lang.get("playerNotFound"));
                    return;
                }
            }
            final IQuester quester = plugin.getQuester(target.getUniqueId());
            if (quester.getCurrentQuestsTemp().isEmpty() && target.getName() != null) {
                String msg = Lang.get("noCurrentQuest");
                msg = msg.replace("<player>", target.getName());
                cs.sendMessage(ChatColor.YELLOW + msg);
            } else {
                final IQuest quest = plugin.getQuestTemp(concatArgArray(args, 2, args.length - 1, ' '));
                if (quest == null) {
                    cs.sendMessage(ChatColor.RED + Lang.get("questNotFound"));
                    return;
                }
                String msg1 = Lang.get("questForceQuit");
                msg1 = msg1.replace("<player>", ChatColor.GREEN + target.getName() + ChatColor.GOLD);
                msg1 = msg1.replace("<quest>", ChatColor.DARK_PURPLE + quest.getName() + ChatColor.GOLD);
                cs.sendMessage(ChatColor.GOLD + msg1);
                String msg2 = Lang.get((Player) target, "questForcedQuit");
                msg2 = msg2.replace("<player>", ChatColor.GREEN + cs.getName() + ChatColor.GOLD);
                msg2 = msg2.replace("<quest>", ChatColor.DARK_PURPLE + quest.getName() + ChatColor.GOLD);
                quester.quitQuest(quest, msg2);
            }
        } else {
            cs.sendMessage(ChatColor.RED + Lang.get("noPermission"));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender commandSender, String[] args) {
        if (args.length == 2) {
            return null; // Shows online players
        } else if (args.length == 3) {
            final List<String> results = new ArrayList<>();
            final Player player = Bukkit.getPlayer(args[1]);
            if (player != null) {
                final Quester quester = plugin.getQuester(player.getUniqueId());
                if (quester != null) {
                    for (final IQuest quest : quester.getCurrentQuests().keySet()) {
                        if (quest.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
                            results.add(ChatColor.stripColor(quest.getName()));
                        }
                    }
                }
            } else {
                for (final IQuest quest : plugin.getLoadedQuests()) {
                    if (quest.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
                        results.add(ChatColor.stripColor(quest.getName()));
                    }
                }
            }
            return results;
        }
        return Collections.emptyList();
    }
}
