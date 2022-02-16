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

import me.blackvein.quests.Quests;
import me.blackvein.quests.commands.QuestsSubCommand;
import me.blackvein.quests.player.IQuester;
import me.blackvein.quests.quests.IQuest;
import me.blackvein.quests.util.Lang;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.Map;
import java.util.UUID;

public class QuestadminStatsCommand extends QuestsSubCommand {

    private final Quests plugin;

    public QuestadminStatsCommand(Quests plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "stats";
    }

    @Override
    public String getNameI18N() {
        return Lang.get("COMMAND_QUESTADMIN_STATS");
    }

    @Override
    public String getDescription() {
        return Lang.get("COMMAND_QUESTADMIN_STATS_HELP");
    }

    @Override
    public String getPermission() {
        return "quests.admin.stats";
    }

    @Override
    public String getSyntax() {
        return "/questadmin stats";
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        if (cs.hasPermission("quests.admin.*") && cs.hasPermission("quests.admin.stats")) {
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
            cs.sendMessage(ChatColor.GOLD + "- " + target.getName() + " -");
            cs.sendMessage(ChatColor.YELLOW + Lang.get("questPoints") + " - " + ChatColor.DARK_PURPLE
                    + quester.getQuestPoints());
            if (quester.getCurrentQuestsTemp().isEmpty()) {
                cs.sendMessage(ChatColor.YELLOW + Lang.get("currentQuest") + " " + ChatColor.DARK_PURPLE+ Lang.get("none"));
            } else {
                cs.sendMessage(ChatColor.YELLOW + Lang.get("currentQuest"));
                for (final Map.Entry<IQuest, Integer> set : quester.getCurrentQuestsTemp().entrySet()) {
                    final IQuest q = set.getKey();
                    final String msg = ChatColor.LIGHT_PURPLE + " - " + ChatColor.DARK_PURPLE + q.getName()
                            + ChatColor.LIGHT_PURPLE + " (" + Lang.get("stageEditorStage") + " " +  (set.getValue() + 1) + ")";
                    cs.sendMessage(msg);
                }
            }
            cs.sendMessage(ChatColor.YELLOW + Lang.get("completedQuest"));

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
        } else {
            cs.sendMessage(ChatColor.RED + Lang.get("noPermission"));
        }
    }
}
