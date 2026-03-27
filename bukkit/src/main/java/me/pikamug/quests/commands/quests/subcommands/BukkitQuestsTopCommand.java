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
import me.pikamug.quests.util.BukkitLang;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class BukkitQuestsTopCommand extends BukkitQuestsSubCommand {

    private final BukkitQuestsPlugin plugin;

    public BukkitQuestsTopCommand(BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "top";
    }

    @Override
    public String getNameI18N() {
        return BukkitLang.get("COMMAND_TOP");
    }

    @Override
    public String getDescription() {
        return BukkitLang.get("COMMAND_TOP_HELP");
    }

    @Override
    public String getPermission() {
        return "quests.top";
    }

    @Override
    public String getSyntax() {
        return "/quests top";
    }

    @Override
    public int getMaxArguments() {
        return 1;
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        if (cs.hasPermission(getPermission())) {
            final int topNumber;
            if (args.length == 1) {
                topNumber = 5; // default
            } else {
                try {
                    topNumber = Integer.parseInt(args[1]);
                } catch (final NumberFormatException e) {
                    cs.sendMessage(ChatColor.YELLOW + BukkitLang.get(cs, "inputNum"));
                    return;
                }
            }
            if (topNumber < 1 || topNumber > plugin.getConfigSettings().getTopLimit()) {
                cs.sendMessage(ChatColor.YELLOW + BukkitLang.get(cs, "invalidRange").replace("<least>", "1")
                        .replace("<greatest>", String.valueOf(plugin.getConfigSettings().getTopLimit())));
                return;
            }
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                final Map<String, Integer> questPoints = plugin.getOfflineQuesters().stream()
                        .collect(Collectors.toMap(Quester::getLastKnownName, Quester::getQuestPoints));
                final LinkedHashMap<String, Integer> sortedMap = (LinkedHashMap<String, Integer>) sort(questPoints);
                int numPrinted = 0;
                String msg = BukkitLang.get(cs, "topQuestersTitle");
                msg = msg.replace("<number>", String.valueOf(topNumber));
                cs.sendMessage(ChatColor.GOLD + msg);
                for (final Map.Entry<String, Integer> entry : sortedMap.entrySet()) {
                    numPrinted++;
                    cs.sendMessage(ChatColor.YELLOW + String.valueOf(numPrinted) + ". " + entry.getKey() + " - "
                            + ChatColor.DARK_PURPLE + entry.getValue() + ChatColor.YELLOW + " "
                            + BukkitLang.get(cs, "questPoints"));
                    if (numPrinted == topNumber) {
                        break;
                    }
                }
            });
        }
    }
}
