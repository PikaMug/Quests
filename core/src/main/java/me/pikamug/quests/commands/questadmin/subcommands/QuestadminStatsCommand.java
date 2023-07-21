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
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.util.Language;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class QuestadminStatsCommand extends QuestsSubCommand {

    private final BukkitQuestsPlugin plugin;

    public QuestadminStatsCommand(BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "stats";
    }

    @Override
    public String getNameI18N() {
        return Language.get("COMMAND_QUESTADMIN_STATS");
    }

    @Override
    public String getDescription() {
        return Language.get("COMMAND_QUESTADMIN_STATS_HELP");
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
    public int getMaxArguments() {
        return 2;
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        if (args.length == 1) {
            // Shows command usage
            return;
        }
        if (cs.hasPermission("quests.admin.*") && cs.hasPermission("quests.admin.stats")) {
            OfflinePlayer target = getOfflinePlayer(args[1]);
            if (target == null) {
                try {
                    target = Bukkit.getOfflinePlayer(UUID.fromString(args[1]));
                } catch (final IllegalArgumentException e) {
                    cs.sendMessage(ChatColor.YELLOW + Language.get("playerNotFound"));
                    return;
                }
            }
            final Quester quester = plugin.getQuester(target.getUniqueId());
            cs.sendMessage(ChatColor.GOLD + "- " + target.getName() + " -");
            cs.sendMessage(ChatColor.YELLOW + Language.get("questPoints") + " - " + ChatColor.DARK_PURPLE
                    + quester.getQuestPoints());
            if (quester.getCurrentQuests().isEmpty()) {
                cs.sendMessage(ChatColor.YELLOW + Language.get("currentQuest") + " " + ChatColor.DARK_PURPLE+ Language.get("none"));
            } else {
                cs.sendMessage(ChatColor.YELLOW + Language.get("currentQuest"));
                for (final Map.Entry<Quest, Integer> set : quester.getCurrentQuests().entrySet()) {
                    final Quest q = set.getKey();
                    final String msg = ChatColor.LIGHT_PURPLE + " - " + ChatColor.DARK_PURPLE + q.getName()
                            + ChatColor.LIGHT_PURPLE + " (" + Language.get("stageEditorStage") + " " +  (set.getValue() + 1) + ")";
                    cs.sendMessage(msg);
                }
            }
            cs.sendMessage(ChatColor.YELLOW + Language.get("completedQuest"));

            if (quester.getCompletedQuestsTemp().isEmpty()) {
                cs.sendMessage(ChatColor.DARK_PURPLE + Language.get("none"));
            } else {
                final StringBuilder completed = new StringBuilder(" ");
                int index = 1;
                for (final Quest q : quester.getCompletedQuestsTemp()) {
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
