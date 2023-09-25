/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.listeners;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.commands.quest.BukkitQuestCommandHandler;
import me.pikamug.quests.commands.questadmin.BukkitQuestadminCommandHandler;
import me.pikamug.quests.commands.quests.BukkitQuestsCommandHandler;
import me.pikamug.quests.util.BukkitLang;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BukkitCommandManager implements TabExecutor {
    private final BukkitQuestsPlugin plugin;
    
    public BukkitCommandManager(final BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final @NotNull CommandSender cs, final @NotNull Command cmd,
                             final @NotNull String label, final String[] args) {
        if (plugin.isLoading()) {
            cs.sendMessage(ChatColor.RED + BukkitLang.get("errorLoading"));
            return true;
        }
        if (cs instanceof Player) {
            if (!plugin.canUseQuests(((Player) cs).getUniqueId())) {
                cs.sendMessage(ChatColor.RED + BukkitLang.get((Player) cs, "noPermission"));
                return true;
            }
        }
        if (cmd.getName().equalsIgnoreCase("quest")) {
            return new BukkitQuestCommandHandler(plugin).check(cs, args);
        } else if (cmd.getName().equalsIgnoreCase("quests")) {
            return new BukkitQuestsCommandHandler(plugin).check(cs, args);
        } else if (cmd.getName().equalsIgnoreCase("questadmin") || cmd.getName().equalsIgnoreCase("questsadmin")) {
            return new BukkitQuestadminCommandHandler(plugin).check(cs, args);
        }
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender cs, @NotNull Command cmd,
                                      @NotNull String label, @NotNull String[] args) {
        if (args.length > 0) {
            if (cmd.getName().equalsIgnoreCase("quest")) {
                return new BukkitQuestCommandHandler(plugin).suggest(cs, args);
            } else if (cmd.getName().equalsIgnoreCase("quests")) {
                return new BukkitQuestsCommandHandler(plugin).suggest(cs, args);
            } else if (cmd.getName().equalsIgnoreCase("questadmin")) {
                return new BukkitQuestadminCommandHandler(plugin).suggest(cs, args);
            }
        }
        return null;
    }
}
