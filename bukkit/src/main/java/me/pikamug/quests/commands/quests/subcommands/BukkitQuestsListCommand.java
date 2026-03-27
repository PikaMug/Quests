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

import me.pikamug.quests.player.BukkitQuester;
import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.commands.BukkitQuestsSubCommand;
import me.pikamug.quests.events.command.BukkitQuestsCommandPreListEvent;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.util.BukkitLang;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BukkitQuestsListCommand extends BukkitQuestsSubCommand {

    private final BukkitQuestsPlugin plugin;

    public BukkitQuestsListCommand(BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getNameI18N() {
        return BukkitLang.get("COMMAND_LIST");
    }

    @Override
    public String getDescription() {
        return BukkitLang.get("COMMAND_LIST_HELP");
    }

    @Override
    public String getPermission() {
        return "quests.list";
    }

    @Override
    public String getSyntax() {
        return "/quests list";
    }

    @Override
    public int getMaxArguments() {
        return 1;
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        if (cs.hasPermission(getPermission())) {
            if (!(cs instanceof Player)) {
                int num = 1;
                cs.sendMessage(ChatColor.GOLD + BukkitLang.get("questListTitle"));
                for (final Quest q : plugin.getLoadedQuests()) {
                    cs.sendMessage(ChatColor.YELLOW + "" + num + ". " + q.getName());
                    num++;
                }
                return;
            }
            final Player player = (Player)cs;
            if (args.length == 1) {
                final BukkitQuester quester = plugin.getQuester(player.getUniqueId());
                final BukkitQuestsCommandPreListEvent preEvent = new BukkitQuestsCommandPreListEvent(quester, 1);
                plugin.getServer().getPluginManager().callEvent(preEvent);
                if (preEvent.isCancelled()) {
                    return;
                }

                quester.listQuests(quester, 1);
            } else if (args.length == 2) {
                final int page;
                try {
                    page = Integer.parseInt(args[1]);
                    if (page < 1) {
                        cs.sendMessage(ChatColor.YELLOW + BukkitLang.get(player, "pageSelectionPosNum"));
                    } else {
                        final BukkitQuester quester = plugin.getQuester(player.getUniqueId());
                        final BukkitQuestsCommandPreListEvent preEvent
                                = new BukkitQuestsCommandPreListEvent(quester, page);
                        plugin.getServer().getPluginManager().callEvent(preEvent);
                        if (preEvent.isCancelled()) {
                            return;
                        }

                        quester.listQuests(quester, page);
                    }
                } catch (final NumberFormatException e) {
                    cs.sendMessage(ChatColor.YELLOW + BukkitLang.get(player, "pageSelectionNum"));
                }
            }
        } else {
            cs.sendMessage(ChatColor.RED + BukkitLang.get(cs, "noPermission"));
        }
    }
}
