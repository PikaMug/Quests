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
import me.blackvein.quests.storage.Storage;
import me.blackvein.quests.util.Lang;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

public class QuestadminResetCommand extends QuestsSubCommand {

    private final Quests plugin;

    public QuestadminResetCommand(Quests plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "reset";
    }

    @Override
    public String getNameI18N() {
        return Lang.get("COMMAND_QUESTADMIN_RESET");
    }

    @Override
    public String getDescription() {
        return Lang.get("COMMAND_QUESTADMIN_RESET_HELP");
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
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.reset")) {
            OfflinePlayer target = getOfflinePlayer(args[1]);
            if (target == null) {
                try {
                    target = Bukkit.getOfflinePlayer(UUID.fromString(args[1]));
                } catch (final IllegalArgumentException e) {
                    cs.sendMessage(ChatColor.YELLOW + Lang.get("playerNotFound"));
                    return;
                }
            }
            final UUID id = target.getUniqueId();
            final ConcurrentSkipListSet<IQuester> temp = (ConcurrentSkipListSet<IQuester>) plugin.getOfflineQuesters();
            temp.removeIf(quester -> quester.getUUID().equals(id));
            plugin.setOfflineQuesters(temp);
            IQuester quester = plugin.getQuester(id);
            try {
                quester.resetCompass();
                quester.hardClear();
                quester.saveData();
                quester.updateJournal();
                final Storage storage = plugin.getStorage();
                storage.deleteQuester(id);
                String msg = Lang.get("questReset");
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
            quester = new Quester(plugin, id);
            quester.saveData();
            final ConcurrentSkipListSet<IQuester> temp2 = (ConcurrentSkipListSet<IQuester>) plugin.getOfflineQuesters();
            temp2.add(quester);
            plugin.setOfflineQuesters(temp2);
        } else {
            cs.sendMessage(ChatColor.RED + Lang.get("noPermission"));
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
