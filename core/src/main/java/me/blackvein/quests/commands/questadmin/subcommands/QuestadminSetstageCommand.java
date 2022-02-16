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

import java.util.UUID;

public class QuestadminSetstageCommand extends QuestsSubCommand {

    private final Quests plugin;

    public QuestadminSetstageCommand(Quests plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "setstage";
    }

    @Override
    public String getNameI18N() {
        return Lang.get("COMMAND_QUESTADMIN_SETSTAGE");
    }

    @Override
    public String getDescription() {
        return Lang.get("COMMAND_QUESTADMIN_SETSTAGE_HELP");
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
    public void execute(CommandSender cs, String[] args) {
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.setstage")) {
            OfflinePlayer target = getOfflinePlayer(args[1]);
            if (target == null) {
                try {
                    target = Bukkit.getOfflinePlayer(UUID.fromString(args[1]));
                } catch (final IllegalArgumentException e) {
                    cs.sendMessage(ChatColor.YELLOW + Lang.get("playerNotFound"));
                    return;
                }
            }
            int stage = -1;
            if (args.length > 3) {
                try {
                    stage = Integer.parseInt(args[args.length - 1]);
                } catch (final NumberFormatException e) {
                    cs.sendMessage(ChatColor.YELLOW + Lang.get("inputNum"));
                }
            } else {
                cs.sendMessage(ChatColor.YELLOW + Lang.get("COMMAND_QUESTADMIN_SETSTAGE_USAGE"));
                return;
            }
            final IQuester quester = plugin.getQuester(target.getUniqueId());
            if (quester.getCurrentQuestsTemp().isEmpty() && target.getName() != null) {
                String msg = Lang.get("noCurrentQuest");
                msg = msg.replace("<player>", target.getName());
                cs.sendMessage(ChatColor.YELLOW + msg);
            } else {
                final IQuest quest = plugin.getQuestTemp(concatArgArray(args, 2, args.length - 2, ' '));
                if (quest == null) {
                    cs.sendMessage(ChatColor.RED + Lang.get("questNotFound"));
                    return;
                }
                try {
                    quest.setStage(quester, stage - 1);
                } catch (final IndexOutOfBoundsException e) {
                    String msg = Lang.get("invalidRange");
                    msg = msg.replace("<least>", "1").replace("<greatest>", String.valueOf(quest.getStages().size()));
                    cs.sendMessage(ChatColor.RED + msg);
                }
                quester.saveData();
            }
        } else {
            cs.sendMessage(ChatColor.RED + Lang.get("noPermission"));
        }
    }
}
