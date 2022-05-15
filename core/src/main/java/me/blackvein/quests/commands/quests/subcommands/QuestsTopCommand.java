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

public class QuestsTopCommand extends QuestsSubCommand {

    private final Quests plugin;

    public QuestsTopCommand(Quests plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "top";
    }

    @Override
    public String getNameI18N() {
        return Lang.get("COMMAND_TOP");
    }

    @Override
    public String getDescription() {
        return Lang.get("COMMAND_TOP_HELP");
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
                    cs.sendMessage(ChatColor.YELLOW + Lang.get("inputNum"));
                    return;
                }
            }
            if (topNumber < 1 || topNumber > plugin.getSettings().getTopLimit()) {
                cs.sendMessage(ChatColor.YELLOW + Lang.get("invalidRange").replace("<least>", "1")
                        .replace("<greatest>", String.valueOf(plugin.getSettings().getTopLimit())));
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
                String msg = Lang.get("topQuestersTitle");
                msg = msg.replace("<number>", ChatColor.DARK_PURPLE + "" + topNumber + ChatColor.GOLD);
                cs.sendMessage(ChatColor.GOLD + msg);
                for (final Map.Entry<String, Integer> entry : sortedMap.entrySet()) {
                    numPrinted++;
                    cs.sendMessage(ChatColor.YELLOW + String.valueOf(numPrinted) + ". " + entry.getKey() + " - "
                            + ChatColor.DARK_PURPLE + entry.getValue() + ChatColor.YELLOW + " "
                            + Lang.get("questPoints"));
                    if (numPrinted == topNumber) {
                        break;
                    }
                }
            });
        }
    }
}
