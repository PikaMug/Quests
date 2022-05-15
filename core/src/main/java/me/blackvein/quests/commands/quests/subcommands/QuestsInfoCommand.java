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

package me.blackvein.quests.commands.quests.subcommands;

import me.blackvein.quests.Quests;
import me.blackvein.quests.commands.QuestsSubCommand;
import me.blackvein.quests.util.Lang;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class QuestsInfoCommand extends QuestsSubCommand {

    private final Quests plugin;

    public QuestsInfoCommand(Quests plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public String getNameI18N() {
        return Lang.get("COMMAND_INFO");
    }

    public String getDescription() {
        return Lang.get("COMMAND_INFO_HELP");
    }

    @Override
    public String getPermission() {
        return "quests.info";
    }

    @Override
    public String getSyntax() {
        return "/quests info";
    }

    @Override
    public int getMaxArguments() {
        return 1;
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        if (cs.hasPermission("quests.info")) {
            cs.sendMessage(ChatColor.YELLOW + "Quests " + ChatColor.GOLD + plugin.getDescription().getVersion());
            cs.sendMessage(ChatColor.GOLD + Lang.get("createdBy") + " " + ChatColor.RED + "Blackvein"
                    + ChatColor.GOLD + " " + Lang.get("continuedBy") + " " + ChatColor.RED + "PikaMug & contributors");
            cs.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.UNDERLINE + "https://github.com/PikaMug/Quests");
        }
    }
}
