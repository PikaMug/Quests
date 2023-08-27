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

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.commands.QuestsSubCommand;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.util.BukkitLanguage;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class QuestsStatsCommand extends QuestsSubCommand {

    private final BukkitQuestsPlugin plugin;

    public QuestsStatsCommand(BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "stats";
    }

    @Override
    public String getNameI18N() {
        return BukkitLanguage.get("COMMAND_STATS");
    }

    @Override
    public String getDescription() {
        return BukkitLanguage.get("COMMAND_STATS_HELP");
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
            final Quester quester = plugin.getQuester(player.getUniqueId());
            cs.sendMessage(ChatColor.GOLD + "- " + player.getName() + " -");
            cs.sendMessage(ChatColor.YELLOW + BukkitLanguage.get(player, "questPoints") + " - " + ChatColor.DARK_PURPLE
                    + quester.getQuestPoints());
            if (quester.getCurrentQuests().isEmpty()) {
                cs.sendMessage(ChatColor.YELLOW + BukkitLanguage.get(player, "currentQuest") + " " + ChatColor.DARK_PURPLE
                        + BukkitLanguage.get("none"));
            } else {
                cs.sendMessage(ChatColor.YELLOW + BukkitLanguage.get(player, "currentQuest"));
                for (final Map.Entry<Quest, Integer> set : quester.getCurrentQuests().entrySet()) {
                    final Quest q = set.getKey();
                    final String msg = ChatColor.LIGHT_PURPLE + " - " + ChatColor.DARK_PURPLE + q.getName()
                            + ChatColor.LIGHT_PURPLE + " (" + BukkitLanguage.get(player, "stageEditorStage") + " "
                            +  (set.getValue() + 1) + ")";
                    cs.sendMessage(msg);
                }
            }
            cs.sendMessage(ChatColor.YELLOW + BukkitLanguage.get(player, "completedQuest"));

            if (quester.getCompletedQuests().isEmpty()) {
                cs.sendMessage(ChatColor.DARK_PURPLE + BukkitLanguage.get("none"));
            } else {
                final StringBuilder completed = new StringBuilder(" ");
                int index = 1;
                for (final Quest q : quester.getCompletedQuests()) {
                    completed.append(ChatColor.DARK_PURPLE).append(q.getName());
                    if (quester.getAmountsCompleted().containsKey(q) && quester.getAmountsCompleted().get(q) > 1) {
                        completed.append(ChatColor.LIGHT_PURPLE).append(" (x").append(quester.getAmountsCompleted()
                                .get(q)).append(")");
                    }
                    if (index < (quester.getCompletedQuests().size())) {
                        completed.append(", ");
                    }
                    index++;
                }
                cs.sendMessage(completed.toString());
            }
        }
    }
}
