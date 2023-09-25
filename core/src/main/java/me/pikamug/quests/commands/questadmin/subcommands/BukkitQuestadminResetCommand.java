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

import me.pikamug.quests.player.BukkitQuester;
import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.commands.BukkitQuestsSubCommand;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.storage.QuesterStorage;
import me.pikamug.quests.util.BukkitLang;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

public class BukkitQuestadminResetCommand extends BukkitQuestsSubCommand {

    private final BukkitQuestsPlugin plugin;

    public BukkitQuestadminResetCommand(BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "reset";
    }

    @Override
    public String getNameI18N() {
        return BukkitLang.get("COMMAND_QUESTADMIN_RESET");
    }

    @Override
    public String getDescription() {
        return BukkitLang.get("COMMAND_QUESTADMIN_RESET_HELP");
    }

    @Override
    public String getPermission() {
        return "quests.admin.reset";
    }

    @Override
    public String getSyntax() {
        return "/questadmin reset";
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
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.reset")) {
            OfflinePlayer target = getOfflinePlayer(args[1]);
            if (target == null) {
                try {
                    target = Bukkit.getOfflinePlayer(UUID.fromString(args[1]));
                } catch (final IllegalArgumentException e) {
                    cs.sendMessage(ChatColor.YELLOW + BukkitLang.get("playerNotFound"));
                    return;
                }
            }
            final UUID id = target.getUniqueId();
            final ConcurrentSkipListSet<Quester> temp = (ConcurrentSkipListSet<Quester>) plugin.getOfflineQuesters();
            temp.removeIf(quester -> quester.getUUID().equals(id));
            plugin.setOfflineQuesters(temp);
            Quester quester = plugin.getQuester(id);
            try {
                quester.resetCompass();
                quester.hardClear();
                quester.saveData();
                quester.updateJournal();
                final QuesterStorage storage = plugin.getStorage();
                storage.deleteQuester(id);
                String msg = BukkitLang.get("questReset");
                if (target.getName() != null) {
                    msg = msg.replace("<player>", ChatColor.GREEN + target.getName() + ChatColor.GOLD);
                } else {
                    msg = msg.replace("<player>", ChatColor.GREEN + args[1] + ChatColor.GOLD);
                }
                cs.sendMessage(ChatColor.GOLD + msg);
                cs.sendMessage(ChatColor.DARK_PURPLE + " UUID: " + ChatColor.DARK_AQUA + id);
            } catch (final Exception e) {
                plugin.getLogger().info("Data file does not exist for " + id);
            }
            quester = new BukkitQuester(plugin, id);
            quester.saveData();
            final ConcurrentSkipListSet<Quester> temp2 = (ConcurrentSkipListSet<Quester>) plugin.getOfflineQuesters();
            temp2.add(quester);
            plugin.setOfflineQuesters(temp2);
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
