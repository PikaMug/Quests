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
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class QuestadminGiveCommand extends QuestsSubCommand {

    private final Quests plugin;

    public QuestadminGiveCommand(Quests plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "give";
    }

    @Override
    public String getNameI18N() {
        return Lang.get("COMMAND_QUESTADMIN_GIVE");
    }

    @Override
    public String getDescription() {
        return Lang.get("COMMAND_QUESTADMIN_GIVE_HELP");
    }

    @Override
    public String getPermission() {
        return "quests.admin.give";
    }

    @Override
    public String getSyntax() {
        return "/questadmin give";
    }

    @Override
    public int getMaxArguments() {
        return 3;
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.give")) {
            OfflinePlayer target = getOfflinePlayer(args[1]);
            if (target == null) {
                try {
                    target = Bukkit.getOfflinePlayer(UUID.fromString(args[1]));
                } catch (final IllegalArgumentException e) {
                    cs.sendMessage(ChatColor.YELLOW + Lang.get("playerNotFound"));
                    return;
                }
            }
            final IQuest questToGive;
            StringBuilder name = new StringBuilder();
            if (args.length == 3) {
                name = new StringBuilder(args[2].toLowerCase());
            } else {
                for (int i = 2; i < args.length; i++) {
                    final int lastIndex = args.length - 1;
                    if (i == lastIndex) {
                        name.append(args[i].toLowerCase());
                    } else {
                        name.append(args[i].toLowerCase()).append(" ");
                    }
                }
            }
            questToGive = plugin.getQuestTemp(name.toString());
            if (questToGive == null) {
                cs.sendMessage(ChatColor.YELLOW + Lang.get("questNotFound"));
            } else {
                final IQuester quester = plugin.getQuester(target.getUniqueId());
                for (final IQuest q : quester.getCurrentQuestsTemp().keySet()) {
                    if (q.getName().equalsIgnoreCase(questToGive.getName())) {
                        String msg = Lang.get("questsPlayerHasQuestAlready");
                        msg = msg.replace("<player>", ChatColor.ITALIC + "" + ChatColor.GREEN + target.getName()
                                + ChatColor.RESET + ChatColor.YELLOW);
                        msg = msg.replace("<quest>", ChatColor.ITALIC + "" + ChatColor.DARK_PURPLE
                                + questToGive.getName() + ChatColor.RESET + ChatColor.YELLOW);
                        cs.sendMessage(ChatColor.YELLOW + msg);
                        return;
                    }
                }
                quester.hardQuit(questToGive);
                String msg1 = Lang.get("questForceTake");
                msg1 = msg1.replace("<player>", ChatColor.GREEN + target.getName() + ChatColor.GOLD);
                msg1 = msg1.replace("<quest>", ChatColor.DARK_PURPLE + questToGive.getName() + ChatColor.GOLD);
                cs.sendMessage(ChatColor.GOLD + msg1);
                if (target.isOnline()) {
                    final Player p = (Player)target;
                    String msg2 = Lang.get(p, "questForcedTake");
                    msg2 = msg2.replace("<player>", ChatColor.GREEN + cs.getName() + ChatColor.GOLD);
                    msg2 = msg2.replace("<quest>", ChatColor.DARK_PURPLE + questToGive.getName() + ChatColor.GOLD);
                    p.sendMessage(ChatColor.GREEN + msg2);
                }
                quester.takeQuest(questToGive, true);
            }
        } else {
            cs.sendMessage(ChatColor.RED + Lang.get("noPermission"));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender commandSender, String[] args) {
        if (args.length == 2) {
            return null; // Shows online players
        } else if (args.length == 3) {
            final List<String> results = new ArrayList<>();
            for (final IQuest quest : plugin.getLoadedQuests()) {
                if (quest.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
                    results.add(ChatColor.stripColor(quest.getName()));
                }
            }
            return results;
        }
        return Collections.emptyList();
    }
}
