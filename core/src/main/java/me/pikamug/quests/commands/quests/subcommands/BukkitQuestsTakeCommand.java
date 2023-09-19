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
