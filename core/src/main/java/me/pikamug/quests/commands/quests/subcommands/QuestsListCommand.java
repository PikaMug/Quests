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
import me.pikamug.quests.commands.QuestsSubCommand;
import me.pikamug.quests.events.command.QuestsCommandPreQuestsListEvent;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.util.BukkitLanguage;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QuestsListCommand extends QuestsSubCommand {

    private final BukkitQuestsPlugin plugin;

    public QuestsListCommand(BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getNameI18N() {
        return BukkitLanguage.get("COMMAND_LIST");
    }

    @Override
    public String getDescription() {
        return BukkitLanguage.get("COMMAND_LIST_HELP");
    }

    @Override
    public String getPermission() {
        return "quests.list";
    }

    @Override
    public String getSyntax() {
        return "/quests list";
    }

    @Override
    public int getMaxArguments() {
        return 1;
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        if (cs.hasPermission(getPermission())) {
            if (!(cs instanceof Player)) {
                int num = 1;
                cs.sendMessage(ChatColor.GOLD + BukkitLanguage.get("questListTitle"));
                for (final Quest q : plugin.getLoadedQuests()) {
                    cs.sendMessage(ChatColor.YELLOW + "" + num + ". " + q.getName());
                    num++;
                }
                return;
            }
            final Player player = (Player)cs;
            if (args.length == 1) {
                final BukkitQuester quester = plugin.getQuester(player.getUniqueId());
                final QuestsCommandPreQuestsListEvent preEvent = new QuestsCommandPreQuestsListEvent(quester, 1);
                plugin.getServer().getPluginManager().callEvent(preEvent);
                if (preEvent.isCancelled()) {
                    return;
                }

                quester.listQuests(quester, 1);
            } else if (args.length == 2) {
                final int page;
                try {
                    page = Integer.parseInt(args[1]);
                    if (page < 1) {
                        cs.sendMessage(ChatColor.YELLOW + BukkitLanguage.get(player, "pageSelectionPosNum"));
                    } else {
                        final BukkitQuester quester = plugin.getQuester(player.getUniqueId());
                        final QuestsCommandPreQuestsListEvent preEvent
                                = new QuestsCommandPreQuestsListEvent(quester, page);
                        plugin.getServer().getPluginManager().callEvent(preEvent);
                        if (preEvent.isCancelled()) {
                            return;
                        }

                        quester.listQuests(quester, page);
                    }
                } catch (final NumberFormatException e) {
                    cs.sendMessage(ChatColor.YELLOW + BukkitLanguage.get(player, "pageSelectionNum"));
                }
            }
        } else {
            cs.sendMessage(ChatColor.RED + BukkitLanguage.get(cs, "noPermission"));
        }
    }
}
