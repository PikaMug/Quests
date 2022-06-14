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
import me.blackvein.quests.events.command.QuestsCommandPreQuestsListEvent;
import me.blackvein.quests.quests.IQuest;
import me.blackvein.quests.util.Lang;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QuestsListCommand extends QuestsSubCommand {

    private final Quests plugin;

    public QuestsListCommand(Quests plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getNameI18N() {
        return Lang.get("COMMAND_LIST");
    }

    @Override
    public String getDescription() {
        return Lang.get("COMMAND_LIST_HELP");
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
                cs.sendMessage(ChatColor.GOLD + Lang.get("questListTitle"));
                for (final IQuest q : plugin.getLoadedQuests()) {
                    cs.sendMessage(ChatColor.YELLOW + "" + num + ". " + q.getName());
                    num++;
                }
                return;
            }
            final Player player = (Player)cs;
            if (args.length == 1) {
                final Quester quester = plugin.getQuester(player.getUniqueId());
                final QuestsCommandPreQuestsListEvent preEvent = new QuestsCommandPreQuestsListEvent(quester, 1);
                plugin.getServer().getPluginManager().callEvent(preEvent);
                if (preEvent.isCancelled()) {
                    return;
                }

                plugin.listQuests(quester, 1);
            } else if (args.length == 2) {
                final int page;
                try {
                    page = Integer.parseInt(args[1]);
                    if (page < 1) {
                        cs.sendMessage(ChatColor.YELLOW + Lang.get(player, "pageSelectionPosNum"));
                    } else {
                        final Quester quester = plugin.getQuester(player.getUniqueId());
                        final QuestsCommandPreQuestsListEvent preEvent
                                = new QuestsCommandPreQuestsListEvent(quester, page);
                        plugin.getServer().getPluginManager().callEvent(preEvent);
                        if (preEvent.isCancelled()) {
                            return;
                        }

                        plugin.listQuests(quester, page);
                    }
                } catch (final NumberFormatException e) {
                    cs.sendMessage(ChatColor.YELLOW + Lang.get(player, "pageSelectionNum"));
                }
            }
        } else {
            cs.sendMessage(ChatColor.RED + Lang.get(cs, "noPermission"));
        }
    }
}
