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

package me.blackvein.quests.listeners;

import me.blackvein.quests.Quests;
import me.blackvein.quests.commands.quest.QuestCommandHandler;
import me.blackvein.quests.commands.questadmin.QuestadminCommandHandler;
import me.blackvein.quests.commands.quests.QuestsCommandHandler;
import me.blackvein.quests.util.Lang;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CommandManager implements TabExecutor {
    private final Quests plugin;
    
    public CommandManager(final Quests plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final @NotNull CommandSender cs, final @NotNull Command cmd,
                             final @NotNull String label, final String[] args) {
        if (plugin.isLoading()) {
            cs.sendMessage(ChatColor.RED + Lang.get("errorLoading"));
            return true;
        }
        if (cs instanceof Player) {
            if (!plugin.canUseQuests(((Player) cs).getUniqueId())) {
                cs.sendMessage(ChatColor.RED + Lang.get((Player) cs, "noPermission"));
                return true;
            }
        }
        if (cmd.getName().equalsIgnoreCase("quest")) {
            return new QuestCommandHandler(plugin).check(cs, args);
        } else if (cmd.getName().equalsIgnoreCase("quests")) {
            return new QuestsCommandHandler(plugin).check(cs, args);
        } else if (cmd.getName().equalsIgnoreCase("questadmin") || cmd.getName().equalsIgnoreCase("questsadmin")) {
            return new QuestadminCommandHandler(plugin).check(cs, args);
        }
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender cs, @NotNull Command cmd,
                                      @NotNull String label, @NotNull String[] args) {
        if (args.length > 0) {
            if (cmd.getName().equalsIgnoreCase("quest")) {
                return new QuestCommandHandler(plugin).suggest(cs, args);
            } else if (cmd.getName().equalsIgnoreCase("quests")) {
                return new QuestsCommandHandler(plugin).suggest(cs, args);
            } else if (cmd.getName().equalsIgnoreCase("questadmin")) {
                return new QuestadminCommandHandler(plugin).suggest(cs, args);
            }
        }
        return null;
    }
}
