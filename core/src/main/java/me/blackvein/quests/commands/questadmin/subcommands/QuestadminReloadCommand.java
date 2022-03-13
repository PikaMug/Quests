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
import me.blackvein.quests.interfaces.ReloadCallback;
import me.blackvein.quests.util.Lang;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class QuestadminReloadCommand extends QuestsSubCommand {

    private final Quests plugin;

    public QuestadminReloadCommand(Quests plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getNameI18N() {
        return Lang.get("COMMAND_QUESTADMIN_RELOAD");
    }

    @Override
    public String getDescription() {
        return Lang.get("COMMAND_QUESTADMIN_RELOAD_HELP");
    }

    @Override
    public String getPermission() {
        return "quests.admin.reload";
    }

    @Override
    public String getSyntax() {
        return "/questadmin reload";
    }

    @Override
    public int getMaxArguments() {
        return 1;
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.reload")) {
            final ReloadCallback<Boolean> callback = response -> {
                if (response) {
                    cs.sendMessage(ChatColor.GOLD + Lang.get("questsReloaded"));
                    String msg = Lang.get("numQuestsLoaded");
                    msg = msg.replace("<number>", ChatColor.DARK_PURPLE + String.valueOf(plugin.getLoadedQuests().size())
                            + ChatColor.GOLD);
                    cs.sendMessage(ChatColor.GOLD + msg);
                } else {
                    cs.sendMessage(ChatColor.RED + Lang.get("unknownError"));
                }
            };
            plugin.reload(callback);
        } else {
            cs.sendMessage(ChatColor.RED + Lang.get("noPermission"));
        }
    }
}
