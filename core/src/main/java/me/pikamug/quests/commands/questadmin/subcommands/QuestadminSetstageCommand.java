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

import me.pikamug.quests.quests.BukkitQuest;
import me.pikamug.quests.player.BukkitQuester;
import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.commands.QuestsSubCommand;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.util.Language;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class QuestadminSetstageCommand extends QuestsSubCommand {

    private final BukkitQuestsPlugin plugin;

    public QuestadminSetstageCommand(BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "setstage";
    }

    @Override
    public String getNameI18N() {
        return Language.get("COMMAND_QUESTADMIN_SETSTAGE");
    }

    @Override
    public String getDescription() {
        return Language.get("COMMAND_QUESTADMIN_SETSTAGE_HELP");
    }

    @Override
    public String getPermission() {
        return "quests.admin.setstage";
    }

    @Override
    public String getSyntax() {
        return "/questadmin setstage";
    }

    @Override
    public int getMaxArguments() {
        return 4;
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        if (args.length == 1) {
            // Shows command usage
            return;
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.setstage")) {
            OfflinePlayer target = getOfflinePlayer(args[1]);
            if (target == null) {
                try {
                    target = Bukkit.getOfflinePlayer(UUID.fromString(args[1]));
                } catch (final IllegalArgumentException e) {
                    cs.sendMessage(ChatColor.YELLOW + Language.get("playerNotFound"));
                    return;
                }
            }
            int stage = -1;
            try {
                stage = Integer.parseInt(args[args.length - 1]);
            } catch (final NumberFormatException e) {
                cs.sendMessage(ChatColor.YELLOW + Language.get("inputNum"));
                return;
            }
            final Quester quester = plugin.getQuester(target.getUniqueId());
            if (quester.getCurrentQuests().isEmpty() && target.getName() != null) {
                String msg = Language.get("noCurrentQuest");
                msg = msg.replace("<player>", target.getName());
                cs.sendMessage(ChatColor.YELLOW + msg);
            } else {
                final Quest quest = plugin.getQuestTemp(concatArgArray(args, 2, args.length - 2, ' '));
                if (quest == null) {
                    cs.sendMessage(ChatColor.RED + Language.get("questNotFound"));
                    return;
                }
                if (!quester.getCurrentQuests().containsKey(quest)) {
                    String msg1 = Language.get("questForceTake");
                    msg1 = msg1.replace("<player>", ChatColor.GREEN + quester.getLastKnownName() + ChatColor.GOLD);
                    msg1 = msg1.replace("<quest>", ChatColor.DARK_PURPLE + quest.getName() + ChatColor.GOLD);
                    cs.sendMessage(ChatColor.GOLD + msg1);
                    if (quester.getPlayer() != null && quester.getPlayer().isOnline()) {
                        String msg2 = Language.get("questForcedTake");
                        msg2 = msg2.replace("<player>", ChatColor.GREEN + quester.getLastKnownName() + ChatColor.GOLD);
                        msg2 = msg2.replace("<quest>", ChatColor.DARK_PURPLE + quest.getName() + ChatColor.GOLD);
                        quester.sendMessage(ChatColor.GREEN + msg2);
                    }
                    quester.takeQuest(quest, true);
                    quester.saveData();
                }
                try {
                    quest.setStage(quester, stage - 1);
                } catch (final IndexOutOfBoundsException e) {
                    String msg = Language.get("invalidRange");
                    msg = msg.replace("<least>", "1").replace("<greatest>", String.valueOf(quest.getStages().size()));
                    cs.sendMessage(ChatColor.RED + msg);
                }
                quester.saveData();
            }
        } else {
            cs.sendMessage(ChatColor.RED + Language.get("noPermission"));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender commandSender, String[] args) {
        if (args.length == 2) {
            return null; // Shows online players
        } else if (args.length == 3) {
            final List<String> results = new ArrayList<>();
            final Player player = Bukkit.getPlayer(args[1]);
            if (player != null) {
                final BukkitQuester quester = plugin.getQuester(player.getUniqueId());
                if (quester != null) {
                    for (final Quest quest : quester.getCurrentQuests().keySet()) {
                        if (quest.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
                            results.add(ChatColor.stripColor(quest.getName()));
                        }
                    }
                }
            } else {
                for (final Quest quest : plugin.getLoadedQuests()) {
                    if (quest.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
                        results.add(ChatColor.stripColor(quest.getName()));
                    }
                }
            }
            return results;
        } else if (args.length > 3) {
            final BukkitQuest quest = plugin.getQuest(args[2]);
            if (quest != null) {
                final List<String> results = new ArrayList<>();
                for (int i = 1; i <= quest.getStages().size(); i++) {
                    results.add(String.valueOf(i));
                }
                return results;
            }
        }
        return Collections.emptyList();
    }
}
