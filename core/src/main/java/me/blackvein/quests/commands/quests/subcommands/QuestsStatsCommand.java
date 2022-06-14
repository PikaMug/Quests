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

import me.blackvein.quests.Quests;
import me.blackvein.quests.commands.QuestsSubCommand;
import me.blackvein.quests.player.IQuester;
import me.blackvein.quests.quests.IQuest;
import me.blackvein.quests.util.Lang;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class QuestsStatsCommand extends QuestsSubCommand {

    private final Quests plugin;

    public QuestsStatsCommand(Quests plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "stats";
    }

    @Override
    public String getNameI18N() {
        return Lang.get("COMMAND_STATS");
    }

    @Override
    public String getDescription() {
        return Lang.get("COMMAND_STATS_HELP");
    }

    @Override
    public String getPermission() {
        return "quests.stats";
    }

    @Override
    public String getSyntax() {
        return "/quests stats";
    }

    @Override
    public int getMaxArguments() {
        return 1;
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        if (assertNonPlayer(cs)) {
            return;
        }
        final Player player = (Player) cs;
        if (cs.hasPermission(getPermission())) {
            final IQuester quester = plugin.getQuester(player.getUniqueId());
            cs.sendMessage(ChatColor.GOLD + "- " + player.getName() + " -");
            cs.sendMessage(ChatColor.YELLOW + Lang.get(player, "questPoints") + " - " + ChatColor.DARK_PURPLE
                    + quester.getQuestPoints());
            if (quester.getCurrentQuestsTemp().isEmpty()) {
                cs.sendMessage(ChatColor.YELLOW + Lang.get(player, "currentQuest") + " " + ChatColor.DARK_PURPLE
                        + Lang.get("none"));
            } else {
                cs.sendMessage(ChatColor.YELLOW + Lang.get(player, "currentQuest"));
                for (final Map.Entry<IQuest, Integer> set : quester.getCurrentQuestsTemp().entrySet()) {
                    final IQuest q = set.getKey();
                    final String msg = ChatColor.LIGHT_PURPLE + " - " + ChatColor.DARK_PURPLE + q.getName()
                            + ChatColor.LIGHT_PURPLE + " (" + Lang.get(player, "stageEditorStage") + " "
                            +  (set.getValue() + 1) + ")";
                    cs.sendMessage(msg);
                }
            }
            cs.sendMessage(ChatColor.YELLOW + Lang.get(player, "completedQuest"));

            if (quester.getCompletedQuestsTemp().isEmpty()) {
                cs.sendMessage(ChatColor.DARK_PURPLE + Lang.get("none"));
            } else {
                final StringBuilder completed = new StringBuilder(" ");
                int index = 1;
                for (final IQuest q : quester.getCompletedQuestsTemp()) {
                    completed.append(ChatColor.DARK_PURPLE).append(q.getName());
                    if (quester.getAmountsCompleted().containsKey(q) && quester.getAmountsCompleted().get(q) > 1) {
                        completed.append(ChatColor.LIGHT_PURPLE).append(" (x").append(quester.getAmountsCompleted()
                                .get(q)).append(")");
                    }
                    if (index < (quester.getCompletedQuestsTemp().size())) {
                        completed.append(", ");
                    }
                    index++;
                }
                cs.sendMessage(completed.toString());
            }
        }
    }
}
