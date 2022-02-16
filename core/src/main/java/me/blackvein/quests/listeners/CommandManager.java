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
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class CommandManager implements CommandExecutor {
    private final Quests plugin;
    private final Map<String, Integer> commandSizes = new HashMap<>();
    private final Map<String, Integer> adminCommandSizes = new HashMap<>();
    
    public CommandManager(final Quests plugin) {
        this.plugin = plugin;
        init();
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
        final String error = validateCommand(cmd.getName(), args);
        if (error != null) {
            cs.sendMessage(error);
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("quest")) {
            return new QuestCommandHandler(plugin).check(cs, args);
        } else if (cmd.getName().equalsIgnoreCase("quests")) {
            return new QuestsCommandHandler(plugin).check(cs, args);
        } else if (cmd.getName().equalsIgnoreCase("questadmin")) {
            return new QuestadminCommandHandler(plugin).check(cs, args);
        }
        return false;
    }
    
    private void init() {
        // [] - required
        // {} - optional
        if (plugin.getSettings().canTranslateSubCommands()) {
            commandSizes.put(Lang.get("COMMAND_LIST"), 1); // list {page}
            commandSizes.put(Lang.get("COMMAND_TAKE"), 2); // take [quest]
            commandSizes.put(Lang.get("COMMAND_QUIT"), 2); // quit [quest]
            commandSizes.put(Lang.get("COMMAND_EDITOR"), 1); // editor
            commandSizes.put(Lang.get("COMMAND_EVENTS_EDITOR"), 1); // actions
            commandSizes.put(Lang.get("COMMAND_CONDITIONS_EDITOR"), 1); // conditions
            commandSizes.put(Lang.get("COMMAND_STATS"), 1); // stats
            commandSizes.put(Lang.get("COMMAND_TOP"), 2); // top {number}
            commandSizes.put(Lang.get("COMMAND_INFO"), 1); // info
            commandSizes.put(Lang.get("COMMAND_JOURNAL"), 1); // journal
            adminCommandSizes.put(Lang.get("COMMAND_QUESTADMIN_STATS"), 2); // stats [player]
            adminCommandSizes.put(Lang.get("COMMAND_QUESTADMIN_GIVE"), 3); // give [player] [quest]
            adminCommandSizes.put(Lang.get("COMMAND_QUESTADMIN_QUIT"), 3); // quit [player] [quest]
            adminCommandSizes.put(Lang.get("COMMAND_QUESTADMIN_REMOVE"), 3); // remove [player] [quest]
            adminCommandSizes.put(Lang.get("COMMAND_QUESTADMIN_POINTS"), 3); // points [player] [amount]
            adminCommandSizes.put(Lang.get("COMMAND_QUESTADMIN_TAKEPOINTS"), 3); // takepoints [player] [amount]
            adminCommandSizes.put(Lang.get("COMMAND_QUESTADMIN_GIVEPOINTS"), 3); // givepoints [player] [amount]
            adminCommandSizes.put(Lang.get("COMMAND_QUESTADMIN_POINTSALL"), 2); // pointsall [amount]
            adminCommandSizes.put(Lang.get("COMMAND_QUESTADMIN_FINISH"), 3); // finish [player] [quest]
            adminCommandSizes.put(Lang.get("COMMAND_QUESTADMIN_NEXTSTAGE"), 3); // nextstage [player] [quest]
            adminCommandSizes.put(Lang.get("COMMAND_QUESTADMIN_SETSTAGE"), 4); // setstage [player] [quest] [stage]
            adminCommandSizes.put(Lang.get("COMMAND_QUESTADMIN_RESET"), 2); // reset [player]
            adminCommandSizes.put(Lang.get("COMMAND_QUESTADMIN_RELOAD"), 1); // reload
        } else {
            commandSizes.put("list", 1); // list {page}
            commandSizes.put("take", 2); // take [quest]
            commandSizes.put("quit", 2); // quit [quest]
            commandSizes.put("editor", 1); // editor
            commandSizes.put("actions", 1); // actions
            commandSizes.put("events", 1); // LEGACY - events
            commandSizes.put("conditions", 1); // conditions
            commandSizes.put("stats", 1); // stats
            commandSizes.put("top", 2); // top [number]
            commandSizes.put("info", 1); // info
            commandSizes.put("journal", 1); // journal
            adminCommandSizes.put("stats", 2); // stats [player]
            adminCommandSizes.put("give", 3); // give [player] [quest]
            adminCommandSizes.put("quit", 3); // quit [player] [quest]
            adminCommandSizes.put("remove", 3); // remove [player] [quest]
            adminCommandSizes.put("points", 3); // points [player] [amount]
            adminCommandSizes.put("takepoints", 3); // takepoints [player] [amount]
            adminCommandSizes.put("givepoints", 3); // givepoints [player] [amount]
            adminCommandSizes.put("pointsall", 2); // pointsall [amount]
            adminCommandSizes.put("finish", 3); // finish [player] [quest]
            adminCommandSizes.put("nextstage", 3); // nextstage [player] [quest]
            adminCommandSizes.put("setstage", 4); // setstage [player] [quest] [stage]
            adminCommandSizes.put("reset", 2); // reset [player]
            adminCommandSizes.put("reload", 1); // reload
        }
    }

    public String validateCommand(final String cmd, final String[] args) {
        if (cmd.equalsIgnoreCase("quest") || args.length == 0) {
            return null;
        }
        if (cmd.equalsIgnoreCase("quests")) {
            String subCmd = args[0].toLowerCase();
            if (subCmd.equals("action")) {
                subCmd = "actions";
            } else if (subCmd.equals("condition")) {
                subCmd = "conditions";
            }
            if (commandSizes.containsKey(subCmd)) {
                final int min = commandSizes.get(subCmd);
                if (args.length < min) {
                    return getQuestsCommandUsage(subCmd);
                } else {
                    return null;
                }
            }
            return ChatColor.YELLOW + Lang.get("questsUnknownCommand");
        } else if (cmd.equalsIgnoreCase("questsadmin") || cmd.equalsIgnoreCase("questadmin")) {
            final String subCmd = args[0].toLowerCase();
            if (adminCommandSizes.containsKey(subCmd)) {
                final int min = adminCommandSizes.get(subCmd);
                if (args.length < min) {
                    return getQuestadminCommandUsage(subCmd);
                } else {
                    return null;
                }
            }
            return ChatColor.YELLOW + Lang.get("questsUnknownAdminCommand");
        }
        return "NULL";
    }
    
    public String getQuestsCommandUsage(final String cmd) {
        return ChatColor.RED + Lang.get("usage") + ": " + ChatColor.YELLOW + "/quests "
                + Lang.get(Lang.getKeyFromPrefix("COMMAND_", cmd) + "_HELP");
    }

    public String getQuestadminCommandUsage(final String cmd) {
        return ChatColor.RED + Lang.get("usage") + ": " + ChatColor.YELLOW + "/questadmin "
                + Lang.get(Lang.getKeyFromPrefix("COMMAND_QUESTADMIN_", cmd) + "_HELP");
    }
}
