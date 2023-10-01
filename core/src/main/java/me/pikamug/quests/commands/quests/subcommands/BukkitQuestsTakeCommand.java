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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BukkitQuestsTakeCommand extends BukkitQuestsSubCommand {

    private final BukkitQuestsPlugin plugin;

    public BukkitQuestsTakeCommand(BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "take";
    }

    @Override
    public String getNameI18N() {
        return BukkitLang.get("COMMAND_TAKE");
    }

    @Override
    public String getDescription() {
        return BukkitLang.get("COMMAND_TAKE_HELP");
    }

    @Override
    public String getPermission() {
        return "quests.take";
    }

    @Override
    public String getSyntax() {
        return "/quests take";
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
        if (assertNonPlayer(cs)) {
            return;
        }
        final Player player = (Player) cs;
        if (plugin.getConfigSettings().canAllowCommands()) {
            if (player.hasPermission(getPermission())) {
                final Quest questToFind = plugin.getQuest(concatArgArray(args, 1, args.length - 1, ' '));
                final Quester quester = plugin.getQuester(player.getUniqueId());
                if (questToFind != null) {
                    for (final Quest q : quester.getCurrentQuests().keySet()) {
                        if (q.getId().equals(questToFind.getId())) {
                            BukkitLang.send(player, ChatColor.RED + BukkitLang.get(player, "questAlreadyOn"));
                            return;
                        }
                    }
                    quester.offerQuest(questToFind, true);
                } else {
                    BukkitLang.send(player, ChatColor.YELLOW + BukkitLang.get(player, "questNotFound"));
                }
            } else {
                BukkitLang.send(player, ChatColor.RED + BukkitLang.get(player, "noPermission"));
            }
        } else {
            BukkitLang.send(player, ChatColor.YELLOW + BukkitLang.get(player, "questTakeDisabled"));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender commandSender, String[] args) {
        if (args.length == 2) {
            final List<String> results = new ArrayList<>();
            for (final Quest quest : plugin.getLoadedQuests()) {
                if (quest.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                    results.add(ChatColor.stripColor(quest.getName()));
                }
            }
            return results;
        }
        return Collections.emptyList();
    }
}
