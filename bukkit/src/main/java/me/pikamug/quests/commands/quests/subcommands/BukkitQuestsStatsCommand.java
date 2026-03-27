/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.commands.quests.subcommands;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.commands.BukkitQuestsSubCommand;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.util.BukkitLang;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class BukkitQuestsStatsCommand extends BukkitQuestsSubCommand {

    private final BukkitQuestsPlugin plugin;

    public BukkitQuestsStatsCommand(BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "stats";
    }

    @Override
    public String getNameI18N() {
        return BukkitLang.get("COMMAND_STATS");
    }

    @Override
    public String getDescription() {
        return BukkitLang.get("COMMAND_STATS_HELP");
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
            cs.sendMessage(ChatColor.YELLOW + BukkitLang.get(player, "questPoints") + " - " + ChatColor.DARK_PURPLE
                    + quester.getQuestPoints());
            if (quester.getCurrentQuests().isEmpty()) {
                cs.sendMessage(ChatColor.YELLOW + BukkitLang.get(player, "currentQuest") + " " + ChatColor.DARK_PURPLE
                        + BukkitLang.get("none"));
            } else {
                cs.sendMessage(ChatColor.YELLOW + BukkitLang.get(player, "currentQuest"));
                for (final Map.Entry<Quest, Integer> set : quester.getCurrentQuests().entrySet()) {
                    final Quest q = set.getKey();
                    final String msg = ChatColor.LIGHT_PURPLE + " - " + ChatColor.DARK_PURPLE + q.getName()
                            + ChatColor.LIGHT_PURPLE + " (" + BukkitLang.get(player, "stageEditorStage") + " "
                            +  (set.getValue() + 1) + ")";
                    cs.sendMessage(msg);
                }
            }
            cs.sendMessage(ChatColor.YELLOW + BukkitLang.get(player, "completedQuest"));

            if (quester.getCompletedQuests().isEmpty()) {
                cs.sendMessage(ChatColor.DARK_PURPLE + BukkitLang.get("none"));
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
