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
import me.pikamug.quests.util.BukkitLang;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

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
                final File folder = new File(plugin.getDataFolder(), "data");
                final File[] playerFiles = folder.listFiles();
                final Map<String, Integer> questPoints = new HashMap<>();
                if (playerFiles != null) {
                    for (final File f : playerFiles) {
                        if (!f.isDirectory()) {
                            final FileConfiguration data = new YamlConfiguration();
                            try {
                                data.load(f);
                            } catch (final IOException | InvalidConfigurationException e) {
                                e.printStackTrace();
                            }
                            questPoints.put(data.getString("lastKnownName", "Unknown"),
                                    data.getInt("quest-points", 0));
                        }
                    }
                }
                final LinkedHashMap<String, Integer> sortedMap = (LinkedHashMap<String, Integer>) sort(questPoints);
                int numPrinted = 0;
                String msg = BukkitLang.get(cs, "topQuestersTitle");
                msg = msg.replace("<number>", ChatColor.DARK_PURPLE + "" + topNumber + ChatColor.GOLD);
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
