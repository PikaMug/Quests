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

package me.pikamug.quests.commands.questadmin.subcommands;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.commands.QuestsSubCommand;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.util.Language;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class QuestadminGivepointsCommand extends QuestsSubCommand {

    private final BukkitQuestsPlugin plugin;

    public QuestadminGivepointsCommand(BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "givepoints";
    }

    @Override
    public String getNameI18N() {
        return Language.get("COMMAND_QUESTADMIN_GIVEPOINTS");
    }

    @Override
    public String getDescription() {
        return Language.get("COMMAND_QUESTADMIN_GIVEPOINTS_HELP");
    }

    @Override
    public String getPermission() {
        return "quests.admin.givepoints";
    }

    @Override
    public String getSyntax() {
        return "/questadmin givepoints";
    }

    @Override
    public int getMaxArguments() {
        return 3;
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        if (args.length == 1) {
            // Shows command usage
            return;
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.givepoints")) {
            OfflinePlayer target = getOfflinePlayer(args[1]);
            if (target == null) {
                try {
                    target = Bukkit.getOfflinePlayer(UUID.fromString(args[1]));
                } catch (final IllegalArgumentException e) {
                    cs.sendMessage(ChatColor.YELLOW + Language.get("playerNotFound"));
                    return;
                }
            }
            final int points;
            try {
                points = Integer.parseInt(args[2]);
            } catch (final NumberFormatException e) {
                cs.sendMessage(ChatColor.YELLOW + Language.get("inputNum"));
                return;
            }
            final Quester quester = plugin.getQuester(target.getUniqueId());
            quester.setQuestPoints(quester.getQuestPoints() + Math.abs(points));
            String msg1 = Language.get("giveQuestPoints").replace("<points>", Language.get("questPoints"));
            msg1 = msg1.replace("<player>", ChatColor.GREEN + target.getName() + ChatColor.GOLD);
            msg1 = msg1.replace("<number>", ChatColor.DARK_PURPLE + "" + points + ChatColor.GOLD);
            cs.sendMessage(ChatColor.GOLD + msg1);
            if (target.isOnline()) {
                final Player p = (Player)target;
                String msg2 = Language.get(p, "questPointsGiven").replace("<points>", Language.get("questPoints"));
                msg2 = msg2.replace("<player>", ChatColor.GREEN + cs.getName() + ChatColor.GOLD);
                msg2 = msg2.replace("<number>", ChatColor.DARK_PURPLE + "" + points + ChatColor.GOLD);
                p.sendMessage(ChatColor.GREEN + msg2);
            }
            quester.saveData();
        } else {
            cs.sendMessage(ChatColor.RED + Language.get("noPermission"));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender commandSender, String[] args) {
        if (args.length == 2) {
            return null; // Shows online players
        }
        return Collections.emptyList();
    }
}
