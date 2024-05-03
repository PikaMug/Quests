/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.commands.questadmin.subcommands;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.commands.BukkitQuestsSubCommand;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.util.BukkitLang;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class BukkitQuestadminPointsCommand extends BukkitQuestsSubCommand {

    private final BukkitQuestsPlugin plugin;

    public BukkitQuestadminPointsCommand(BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "points";
    }

    @Override
    public String getNameI18N() {
        return BukkitLang.get("COMMAND_QUESTADMIN_POINTS");
    }

    @Override
    public String getDescription() {
        return BukkitLang.get("COMMAND_QUESTADMIN_POINTS_HELP");
    }

    @Override
    public String getPermission() {
        return "quests.admin.points";
    }

    @Override
    public String getSyntax() {
        return "/questadmin points";
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
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.points")) {
            OfflinePlayer target = getOfflinePlayer(args[1]);
            if (target == null) {
                try {
                    target = Bukkit.getOfflinePlayer(UUID.fromString(args[1]));
                } catch (final IllegalArgumentException e) {
                    // Do nothing
                }
            }
            if (target == null || target.getName() == null) {
                cs.sendMessage(ChatColor.YELLOW + BukkitLang.get("playerNotFound"));
                return;
            }
            final int points;
            try {
                points = Integer.parseInt(args[2]);
            } catch (final NumberFormatException e) {
                cs.sendMessage(ChatColor.YELLOW + BukkitLang.get("inputNum"));
                return;
            }
            final Quester quester = plugin.getQuester(target.getUniqueId());
            quester.setQuestPoints(points);
            String msg1 = BukkitLang.get("setQuestPoints").replace("<points>", BukkitLang.get("questPoints"));
            msg1 = msg1.replace("<player>", target.getName());
            msg1 = msg1.replace("<number>", String.valueOf(points));
            cs.sendMessage(ChatColor.GOLD + msg1);
            if (target.isOnline()) {
                final Player p = (Player)target;
                String msg2 = BukkitLang.get(p, "questPointsSet").replace("<points>", BukkitLang.get("questPoints"));
                msg2 = msg2.replace("<player>", cs.getName());
                msg2 = msg2.replace("<number>", String.valueOf(points));
                p.sendMessage(ChatColor.GREEN + msg2);
            }
            quester.saveData();
        } else {
            cs.sendMessage(ChatColor.RED + BukkitLang.get("noPermission"));
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
