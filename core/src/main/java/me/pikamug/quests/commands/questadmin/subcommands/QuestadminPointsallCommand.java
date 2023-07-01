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

package me.pikamug.quests.commands.questadmin.subcommands;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.commands.QuestsSubCommand;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.util.Language;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class QuestadminPointsallCommand extends QuestsSubCommand {

    private final BukkitQuestsPlugin plugin;

    public QuestadminPointsallCommand(BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "pointsall";
    }

    @Override
    public String getNameI18N() {
        return Language.get("COMMAND_QUESTADMIN_POINTSALL");
    }

    @Override
    public String getDescription() {
        return Language.get("COMMAND_QUESTADMIN_POINTSALL_HELP");
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
                    cs.sendMessage(ChatColor.RED + Language.get("inputPosNum"));
                    return;
                }
            } catch (final NumberFormatException e) {
                cs.sendMessage(ChatColor.RED + Language.get("inputNum"));
                return;
            }
            cs.sendMessage(ChatColor.YELLOW + Language.get("settingAllQuestPoints")
                    .replace("<points>", Language.get("questPoints")));
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
                                    String msg = Language.get("errorReading");
                                    msg = msg.replace("<file>", ChatColor.DARK_AQUA + f.getName() + ChatColor.RED);
                                    cs.sendMessage(ChatColor.RED + msg);
                                    failCount++;
                                } else if (!suppressed) {
                                    String msg = Language.get("errorReadingSuppress");
                                    msg = msg.replace("<file>", ChatColor.DARK_AQUA + f.getName() + ChatColor.RED);
                                    cs.sendMessage(ChatColor.RED + msg);
                                    suppressed = true;
                                }
                            }
                        }
                    }
                    cs.sendMessage(ChatColor.GREEN + Language.get("done"));
                    String msg = Language.get("allQuestPointsSet").replace("<points>", Language.get("questPoints"));
                    msg = msg.replace("<number>", ChatColor.AQUA + "" + amount + ChatColor.GOLD);
                    plugin.getServer().broadcastMessage(ChatColor.YELLOW + "" + ChatColor.GOLD + msg);
                } else {
                    cs.sendMessage(ChatColor.RED + Language.get("errorDataFolder"));
                }
            });
        } else {
            cs.sendMessage(ChatColor.RED + Language.get("noPermission"));
        }
    }
}
