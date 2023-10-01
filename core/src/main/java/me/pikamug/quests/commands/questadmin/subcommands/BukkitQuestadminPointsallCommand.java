/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.commands.questadmin.subcommands;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.commands.BukkitQuestsSubCommand;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.util.BukkitLang;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class BukkitQuestadminPointsallCommand extends BukkitQuestsSubCommand {

    private final BukkitQuestsPlugin plugin;

    public BukkitQuestadminPointsallCommand(BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "pointsall";
    }

    @Override
    public String getNameI18N() {
        return BukkitLang.get("COMMAND_QUESTADMIN_POINTSALL");
    }

    @Override
    public String getDescription() {
        return BukkitLang.get("COMMAND_QUESTADMIN_POINTSALL_HELP");
    }

    @Override
    public String getPermission() {
        return "quests.admin.pointsall";
    }

    @Override
    public String getSyntax() {
        return "/questadmin pointsall";
    }

    @Override
    public int getMaxArguments() {
        return 2;
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        if (args.length == 1) {
            // Shows command usage
            return;
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.pointsall")) {
            final int amount;
            try {
                amount = Integer.parseInt(args[1]);
                if (amount < 0) {
                    cs.sendMessage(ChatColor.RED + BukkitLang.get("inputPosNum"));
                    return;
                }
            } catch (final NumberFormatException e) {
                cs.sendMessage(ChatColor.RED + BukkitLang.get("inputNum"));
                return;
            }
            cs.sendMessage(ChatColor.YELLOW + BukkitLang.get("settingAllQuestPoints")
                    .replace("<points>", BukkitLang.get("questPoints")));
            for (final Quester q : plugin.getOfflineQuesters()) {
                q.setQuestPoints(amount);
            }
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                final File questerFolder = new File(plugin.getDataFolder(), "data");
                if (questerFolder.exists() && questerFolder.isDirectory()) {
                    final FileConfiguration data = new YamlConfiguration();
                    final File[] files = questerFolder.listFiles();
                    int failCount = 0;
                    boolean suppressed = false;
                    if (files != null) {
                        for (final File f : files) {
                            try {
                                data.load(f);
                                data.set("quest-points", amount);
                                data.save(f);
                            } catch (final IOException | InvalidConfigurationException e) {
                                if (failCount < 10) {
                                    String msg = BukkitLang.get("errorReading");
                                    msg = msg.replace("<file>", ChatColor.DARK_AQUA + f.getName() + ChatColor.RED);
                                    cs.sendMessage(ChatColor.RED + msg);
                                    failCount++;
                                } else if (!suppressed) {
                                    String msg = BukkitLang.get("errorReadingSuppress");
                                    msg = msg.replace("<file>", ChatColor.DARK_AQUA + f.getName() + ChatColor.RED);
                                    cs.sendMessage(ChatColor.RED + msg);
                                    suppressed = true;
                                }
                            }
                        }
                    }
                    cs.sendMessage(ChatColor.GREEN + BukkitLang.get("done"));
                    String msg = BukkitLang.get("allQuestPointsSet").replace("<points>", BukkitLang.get("questPoints"));
                    msg = msg.replace("<number>", ChatColor.AQUA + "" + amount + ChatColor.GOLD);
                    plugin.getServer().broadcastMessage(ChatColor.YELLOW + "" + ChatColor.GOLD + msg);
                } else {
                    cs.sendMessage(ChatColor.RED + BukkitLang.get("errorDataFolder"));
                }
            });
        } else {
            cs.sendMessage(ChatColor.RED + BukkitLang.get("noPermission"));
        }
    }
}
