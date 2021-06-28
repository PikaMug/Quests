/*******************************************************************************************************
 * Copyright (c) 2014 PikaMug and contributors. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

package me.blackvein.quests.listeners;

import me.blackvein.quests.Quest;
import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;
import me.blackvein.quests.Requirements;
import me.blackvein.quests.Stage;
import me.blackvein.quests.events.command.QuestsCommandPreQuestsEditorEvent;
import me.blackvein.quests.events.command.QuestsCommandPreQuestsJournalEvent;
import me.blackvein.quests.events.command.QuestsCommandPreQuestsListEvent;
import me.blackvein.quests.events.quest.QuestQuitEvent;
import me.blackvein.quests.interfaces.ReloadCallback;
import me.blackvein.quests.item.QuestJournal;
import me.blackvein.quests.storage.Storage;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.Conversation;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

public class CmdExecutor implements CommandExecutor {
    private final Quests plugin;
    private final Map<String, Integer> commands = new HashMap<String, Integer>();
    private final Map<String, Integer> adminCommands = new HashMap<String, Integer>();
    
    public CmdExecutor(final Quests plugin) {
        this.plugin = plugin;
        init();
    }

    @Override
    public boolean onCommand(final CommandSender cs, final Command cmd, final String label, final String[] args) {
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
        final String error = checkCommand(cmd.getName(), args);
        if (error != null) {
            cs.sendMessage(error);
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("quest")) {
            return questCommandHandler(cs, args);
        } else if (cmd.getName().equalsIgnoreCase("quests")) {
            return questsCommandHandler(cs, args);
        } else if (cmd.getName().equalsIgnoreCase("questadmin")) {
            return questAdminCommandHandler(cs, args);
        }
        return false;
    }
    
    private void init() {
        // [] - required
        // {} - optional
        if (plugin.getSettings().canTranslateSubCommands()) {
            commands.put(Lang.get("COMMAND_LIST"), 1); // list {page}
            commands.put(Lang.get("COMMAND_TAKE"), 2); // take [quest]
            commands.put(Lang.get("COMMAND_QUIT"), 2); // quit [quest]
            commands.put(Lang.get("COMMAND_EDITOR"), 1); // editor
            commands.put(Lang.get("COMMAND_EVENTS_EDITOR"), 1); // actions
            commands.put(Lang.get("COMMAND_CONDITIONS_EDITOR"), 1); // conditions
            commands.put(Lang.get("COMMAND_STATS"), 1); // stats
            commands.put(Lang.get("COMMAND_TOP"), 2); // top {number}
            commands.put(Lang.get("COMMAND_INFO"), 1); // info
            commands.put(Lang.get("COMMAND_JOURNAL"), 1); // journal
            adminCommands.put(Lang.get("COMMAND_QUESTADMIN_STATS"), 2); // stats [player]
            adminCommands.put(Lang.get("COMMAND_QUESTADMIN_GIVE"), 3); // give [player] [quest]
            adminCommands.put(Lang.get("COMMAND_QUESTADMIN_QUIT"), 3); // quit [player] [quest]
            adminCommands.put(Lang.get("COMMAND_QUESTADMIN_REMOVE"), 3); // remove [player] [quest]
            adminCommands.put(Lang.get("COMMAND_QUESTADMIN_POINTS"), 3); // points [player] [amount]
            adminCommands.put(Lang.get("COMMAND_QUESTADMIN_TAKEPOINTS"), 3); // takepoints [player] [amount]
            adminCommands.put(Lang.get("COMMAND_QUESTADMIN_GIVEPOINTS"), 3); // givepoints [player] [amount]
            adminCommands.put(Lang.get("COMMAND_QUESTADMIN_POINTSALL"), 2); // pointsall [amount]
            adminCommands.put(Lang.get("COMMAND_QUESTADMIN_FINISH"), 3); // finish [player] [quest]
            adminCommands.put(Lang.get("COMMAND_QUESTADMIN_NEXTSTAGE"), 3); // nextstage [player] [quest]
            adminCommands.put(Lang.get("COMMAND_QUESTADMIN_SETSTAGE"), 4); // setstage [player] [quest] [stage]
            adminCommands.put(Lang.get("COMMAND_QUESTADMIN_RESET"), 2); // reset [player]
            adminCommands.put(Lang.get("COMMAND_QUESTADMIN_RELOAD"), 1); // reload
        } else {
            commands.put("list", 1); // list {page}
            commands.put("take", 2); // take [quest]
            commands.put("quit", 2); // quit [quest]
            commands.put("editor", 1); // editor
            commands.put("actions", 1); // actions
            commands.put("events", 1); // LEGACY - events
            commands.put("conditions", 1); // conditions
            commands.put("stats", 1); // stats
            commands.put("top", 2); // top [number]
            commands.put("info", 1); // info
            commands.put("journal", 1); // journal
            adminCommands.put("stats", 2); // stats [player]
            adminCommands.put("give", 3); // give [player] [quest]
            adminCommands.put("quit", 3); // quit [player] [quest]
            adminCommands.put("remove", 3); // remove [player] [quest]
            adminCommands.put("points", 3); // points [player] [amount]
            adminCommands.put("takepoints", 3); // takepoints [player] [amount]
            adminCommands.put("givepoints", 3); // givepoints [player] [amount]
            adminCommands.put("pointsall", 2); // pointsall [amount]
            adminCommands.put("finish", 3); // finish [player] [quest]
            adminCommands.put("nextstage", 3); // nextstage [player] [quest]
            adminCommands.put("setstage", 4); // setstage [player] [quest] [stage]
            adminCommands.put("reset", 2); // reset [player]
            adminCommands.put("reload", 1); // reload
        }
    }
    
    public Map<String, Integer> getCommands() {
        return commands;
    }
    
    public Map<String, Integer> getAdminCommands() {
        return adminCommands;
    }

    public String checkCommand(final String cmd, final String[] args) {
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
            if (commands.containsKey(subCmd)) {
                final int min = commands.get(subCmd);
                if (args.length < min) {
                    return getQuestsCommandUsage(subCmd);
                } else {
                    return null;
                }
            }
            return ChatColor.YELLOW + Lang.get("questsUnknownCommand");
        } else if (cmd.equalsIgnoreCase("questsadmin") || cmd.equalsIgnoreCase("questadmin")) {
            final String subCmd = args[0].toLowerCase();
            if (adminCommands.containsKey(subCmd)) {
                final int min = adminCommands.get(subCmd);
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
    
    private boolean questCommandHandler(final CommandSender cs, final String[] args) {
        if (cs instanceof Player) {
            if (((Player) cs).hasPermission("quests.quest")) {
                if (args.length == 0) {
                    final Player player = (Player) cs;
                    final Quester quester = plugin.getQuester(player.getUniqueId());
                    if (quester.getCurrentQuests().isEmpty() == false) {
                        for (final Quest q : quester.getCurrentQuests().keySet()) {
                            final Stage stage = quester.getCurrentStage(q);
                            q.updateCompass(quester, stage);
                            if (plugin.getQuester(player.getUniqueId()).getQuestData(q).getDelayStartTime() == 0) {
                                final String msg = Lang.get(player, "questObjectivesTitle")
                                        .replace("<quest>", q.getName());
                                quester.sendMessage(ChatColor.GOLD + msg);
                                plugin.showObjectives(q, quester, false);
                            } else {
                                final long time = plugin.getQuester(player.getUniqueId()).getStageTime(q);
                                String msg = ChatColor.YELLOW + "(" + Lang.get(player, "delay") + ") " + ChatColor.RED
                                        +  Lang.get(player, "plnTooEarly");
                                msg = msg.replace("<quest>", q.getName());
                                msg = msg.replace("<time>", MiscUtil.getTime(time));
                                quester.sendMessage(msg);
                            }
                        }
                    } else {
                        quester.sendMessage(ChatColor.YELLOW + Lang.get(player, "noActiveQuest"));
                    }
                } else {
                    showQuestDetails(cs, args);
                }
            } else {
                cs.sendMessage(ChatColor.RED + Lang.get("noPermission"));
                return true;
            }
        } else {
            cs.sendMessage(ChatColor.YELLOW + Lang.get("consoleError"));
            return true;
        }
        return true;
    }
    
    private boolean questsCommandHandler(final CommandSender cs, final String[] args) {
        if (args.length == 0) {
            questsHelp(cs);
            return true;
        }
        if (args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase(Lang.get("COMMAND_LIST"))) {
            questsList(cs, args);
        } else if (args[0].equalsIgnoreCase("take") || args[0].equalsIgnoreCase(Lang.get("COMMAND_TAKE"))) {
            if (!(cs instanceof Player)) {
                cs.sendMessage(ChatColor.YELLOW + Lang.get("consoleError"));
                return true;
            }
            questsTake((Player) cs, args);
        } else if (args[0].equalsIgnoreCase("quit") || args[0].equalsIgnoreCase(Lang.get("COMMAND_QUIT"))) {
            if (!(cs instanceof Player)) {
                cs.sendMessage(ChatColor.YELLOW + Lang.get("consoleError"));
                return true;
            }
            questsQuit((Player) cs, args);
        } else if (args[0].equalsIgnoreCase("stats") || args[0].equalsIgnoreCase(Lang.get("COMMAND_STATS"))) {
            if (!(cs instanceof Player)) {
                cs.sendMessage(ChatColor.YELLOW + Lang.get("consoleError"));
                return true;
            }
            questsStats(cs, null);
        } else if (args[0].equalsIgnoreCase("journal") || args[0].equalsIgnoreCase(Lang.get("COMMAND_JOURNAL"))) {
            if (!(cs instanceof Player)) {
                cs.sendMessage(ChatColor.YELLOW + Lang.get("consoleError"));
                return true;
            }
            questsJournal((Player) cs);
        } else if (args[0].equalsIgnoreCase("top") || args[0].equalsIgnoreCase(Lang.get("COMMAND_TOP"))) {
            questsTop(cs, args);
        } else if (args[0].equalsIgnoreCase("editor") || args[0].equalsIgnoreCase(Lang.get("COMMAND_EDITOR"))) {
            questsEditor(cs);
        } else if (args[0].startsWith("action") || args[0].startsWith("event") 
                || args[0].startsWith(Lang.get("COMMAND_EVENTS_EDITOR"))) {
            questsActions(cs);
        } else if (args[0].startsWith("condition") || args[0].startsWith(Lang.get("COMMAND_CONDITIONS_EDITOR"))) {
            questsConditions(cs);
        } else if (args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase(Lang.get("COMMAND_INFO"))) {
            questsInfo(cs);
        } else {
            cs.sendMessage(ChatColor.YELLOW + Lang.get("questsUnknownCommand"));
            return true;
        }
        return true;
    }
    
    private boolean questAdminCommandHandler(final CommandSender cs, final String[] args) {
        if (args.length == 0) {
            adminHelp(cs);
            return true;
        }
        if (args[0].equalsIgnoreCase("stats") || args[0].equalsIgnoreCase(Lang.get("COMMAND_QUESTADMIN_STATS"))) {
            adminStats(cs, args);
        } else if (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase(Lang.get("COMMAND_QUESTADMIN_GIVE"))) {
            adminGive(cs, args);
        } else if (args[0].equalsIgnoreCase("quit") || args[0].equalsIgnoreCase(Lang.get("COMMAND_QUESTADMIN_QUIT"))) {
            adminQuit(cs, args);
        } else if (args[0].equalsIgnoreCase("points") 
                || args[0].equalsIgnoreCase(Lang.get("COMMAND_QUESTADMIN_POINTS"))) {
            adminPoints(cs, args);
        } else if (args[0].equalsIgnoreCase("takepoints") 
                || args[0].equalsIgnoreCase(Lang.get("COMMAND_QUESTADMIN_TAKEPOINTS"))) {
            adminTakePoints(cs, args);
        } else if (args[0].equalsIgnoreCase("givepoints") 
                || args[0].equalsIgnoreCase(Lang.get("COMMAND_QUESTADMIN_GIVEPOINTS"))) {
            adminGivePoints(cs, args);
        } else if (args[0].equalsIgnoreCase("pointsall")
                || args[0].equalsIgnoreCase(Lang.get("COMMAND_QUESTADMIN_POINTSALL"))) {
            adminPointsAll(cs, args);
        } else if (args[0].equalsIgnoreCase("finish") 
                || args[0].equalsIgnoreCase(Lang.get("COMMAND_QUESTADMIN_FINISH"))) {
            adminFinish(cs, args);
        } else if (args[0].equalsIgnoreCase("nextstage") 
                || args[0].equalsIgnoreCase(Lang.get("COMMAND_QUESTADMIN_NEXTSTAGE"))) {
            adminNextStage(cs, args);
        } else if (args[0].equalsIgnoreCase("setstage") 
                || args[0].equalsIgnoreCase(Lang.get("COMMAND_QUESTADMIN_SETSTAGE"))) {
            adminSetStage(cs, args);
        } else if (args[0].equalsIgnoreCase("reset") 
                || args[0].equalsIgnoreCase(Lang.get("COMMAND_QUESTADMIN_RESET"))) {
            adminReset(cs, args);
        } else if (args[0].equalsIgnoreCase("remove") 
                || args[0].equalsIgnoreCase(Lang.get("COMMAND_QUESTADMIN_REMOVE"))) {
            adminRemove(cs, args);
        } else if (args[0].equalsIgnoreCase("reload") 
                || args[0].equalsIgnoreCase(Lang.get("COMMAND_QUESTADMIN_RELOAD"))) {
            adminReload(cs);
        } else {
            cs.sendMessage(ChatColor.YELLOW + Lang.get("questsUnknownAdminCommand"));
        }
        return true;
    }
    
    public void showQuestDetails(final CommandSender cs, final String[] args) {
        if (((Player) cs).hasPermission("quests.questinfo")) {
            String name = "";
            if (args.length == 1) {
                name = args[0].toLowerCase();
            } else {
                int index = 0;
                for (final String s : args) {
                    if (index == (args.length - 1)) {
                        name = name + s.toLowerCase();
                    } else {
                        name = name + s.toLowerCase() + " ";
                    }
                    index++;
                }
            }
            final Quest q = plugin.getQuest(name);
            if (q != null) {
                final Player player = (Player) cs;
                final Quester quester = plugin.getQuester(player.getUniqueId());
                cs.sendMessage(ChatColor.GOLD + "- " + q.getName() + " -");
                cs.sendMessage(" ");
                /*if (q.redoDelay > -1) {
                    if (q.redoDelay == 0) {
                        cs.sendMessage(ChatColor.DARK_AQUA + Lang.get("readoable"));
                    } else {
                        String msg = Lang.get("redoableEvery");
                        msg = msg.replace("<time>", ChatColor.AQUA + getTime(q.redoDelay) + ChatColor.DARK_AQUA);
                        cs.sendMessage(ChatColor.DARK_AQUA + msg);
                    }
                }*/
                if (q.getNpcStart() != null) {
                    String msg = Lang.get("speakTo");
                    msg = msg.replace("<npc>", q.getNpcStart().getName());
                    cs.sendMessage(ChatColor.YELLOW + msg);
                } else {
                    cs.sendMessage(ChatColor.YELLOW + q.getDescription());
                }
                cs.sendMessage(" ");
                if (plugin.getSettings().canShowQuestReqs() == true) {
                    cs.sendMessage(ChatColor.GOLD + Lang.get("requirements"));
                    final Requirements reqs = q.getRequirements();
                    if (reqs.getPermissions().isEmpty() == false) {
                        for (final String perm : reqs.getPermissions()) {
                            if (plugin.getDependencies().getVaultPermission().has(player, perm)) {
                                cs.sendMessage(ChatColor.GREEN + Lang.get("permissionDisplay") + " " + perm);
                            } else {
                                cs.sendMessage(ChatColor.RED + Lang.get("permissionDisplay") + " " + perm);
                            }
                        }
                    }
                    if (reqs.getHeroesPrimaryClass() != null) {
                        if (plugin.getDependencies()
                                .testPrimaryHeroesClass(reqs.getHeroesPrimaryClass(), player.getUniqueId())) {
                            cs.sendMessage(ChatColor.BOLD + "" + ChatColor.GREEN + reqs.getHeroesPrimaryClass()
                                + ChatColor.RESET + "" + ChatColor.DARK_GREEN + " " + Lang.get("heroesClass"));
                        } else {
                            cs.sendMessage(ChatColor.BOLD + "" + ChatColor.DARK_RED + reqs.getHeroesPrimaryClass()
                                + ChatColor.RESET + "" + ChatColor.RED + " " + Lang.get("heroesClass"));
                        }
                    }
                    if (reqs.getHeroesSecondaryClass() != null) {
                        if (plugin.getDependencies()
                                .testSecondaryHeroesClass(reqs.getHeroesSecondaryClass(), player.getUniqueId())) {
                            cs.sendMessage(ChatColor.BOLD + "" + ChatColor.DARK_RED + reqs.getHeroesSecondaryClass()
                                + ChatColor.RESET + "" + ChatColor.RED + " " + Lang.get("heroesClass"));
                        } else {
                            cs.sendMessage(ChatColor.BOLD + "" + ChatColor.GREEN + reqs.getHeroesSecondaryClass()
                                + ChatColor.RESET + "" + ChatColor.DARK_GREEN + " " + Lang.get("heroesClass"));
                        }
                    }
                    if (reqs.getMcmmoSkills().isEmpty() == false) {
                        for (final String skill : reqs.getMcmmoSkills()) {
                            final int level = plugin.getDependencies().getMcmmoSkillLevel(Quests
                                    .getMcMMOSkill(skill), player.getName());
                            final int req = reqs.getMcmmoAmounts().get(reqs.getMcmmoSkills().indexOf(skill));
                            final String skillName = MiscUtil.getCapitalized(skill);
                            if (level >= req) {
                                cs.sendMessage(ChatColor.GREEN + skillName + " " + Lang.get("mcMMOLevel") + " " + req);
                            } else {
                                cs.sendMessage(ChatColor.RED + skillName + " " + Lang.get("mcMMOLevel") + " " + req);
                            }
                        }
                    }
                    if (reqs.getQuestPoints() != 0) {
                        if (quester.getQuestPoints() >= reqs.getQuestPoints()) {
                            cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.GREEN + reqs.getQuestPoints() + " "
                                    + Lang.get("questPoints"));
                        } else {
                            cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.RED + reqs.getQuestPoints() + " "
                                    + Lang.get("questPoints"));
                        }
                    }
                    if (reqs.getMoney() != 0) {
                        if (plugin.getDependencies().getVaultEconomy() != null && plugin.getDependencies()
                                .getVaultEconomy().getBalance(quester.getOfflinePlayer()) >= reqs.getMoney()) {
                            if (reqs.getMoney() == 1) {
                                cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.GREEN + reqs.getMoney() + " " 
                                        + plugin.getDependencies().getCurrency(false));
                            } else {
                                cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.GREEN + reqs.getMoney() + " " 
                                        + plugin.getDependencies().getCurrency(true));
                            }
                        } else {
                            if (reqs.getMoney() == 1) {
                                cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.RED + reqs.getMoney() + " " 
                                        + plugin.getDependencies().getCurrency(false));
                            } else {
                                cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.RED + reqs.getMoney() + " " 
                                        + plugin.getDependencies().getCurrency(true));
                            }
                        }
                    }
                    if (reqs.getItems().isEmpty() == false) {
                        for (final ItemStack is : reqs.getItems()) {
                            if (plugin.getQuester(player.getUniqueId()).hasItem(is) == true) {
                                cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.GREEN + ItemUtil.getString(is));
                            } else {
                                cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.RED + ItemUtil.getString(is));
                            }
                        }
                    }
                    if (reqs.getNeededQuests().isEmpty() == false) {
                        for (final Quest quest : reqs.getNeededQuests()) {
                            if (quester.getCompletedQuests().contains(quest)) {
                                cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.GREEN + Lang.get("complete") + " " 
                                        + ChatColor.ITALIC + quest.getName());
                            } else {
                                cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.RED + Lang.get("complete") + " " 
                                        + ChatColor.ITALIC + quest.getName());
                            }
                        }
                    }
                    if (reqs.getBlockQuests().isEmpty() == false) {
                        for (final Quest quest : reqs.getBlockQuests()) {
                            if (quester.getCompletedQuests().contains(quest)) {
                                String msg = Lang.get("haveCompleted");
                                msg = msg.replace("<quest>", ChatColor.ITALIC + "" + ChatColor.DARK_PURPLE 
                                        + quest.getName() + ChatColor.RED);
                                cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.RED + msg);
                            } else {
                                String msg = Lang.get("cannotComplete");
                                msg = msg.replace("<quest>", ChatColor.ITALIC + "" + ChatColor.DARK_PURPLE 
                                        + quest.getName() + ChatColor.GREEN);
                                cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.GREEN + msg);
                            }
                        }
                    }
                }
            } else {
                cs.sendMessage(ChatColor.YELLOW + Lang.get("questNotFound"));
            }
        } else {
            cs.sendMessage(ChatColor.RED + Lang.get("noPermission"));
        }
    }
    
    private boolean questsInfo(final CommandSender cs) {
        if (cs.hasPermission("quests.info")) {
            cs.sendMessage(ChatColor.YELLOW + "Quests " + ChatColor.GOLD 
                    + plugin.getDescription().getVersion());
            cs.sendMessage(ChatColor.GOLD + Lang.get("createdBy") + " " + ChatColor.RED + "Blackvein"
                    + ChatColor.GOLD + " " + Lang.get("continuedBy") + " " + ChatColor.RED + "PikaMug & contributors");
            cs.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.UNDERLINE
                    + "https://github.com/PikaMug/Quests");
        }
        return true;
    }

    private boolean questsActions(final CommandSender cs) {
        if (cs.hasPermission("quests.events.*") || cs.hasPermission("quests.actions.*") 
                || cs.hasPermission("quests.actions.editor") || cs.hasPermission("quests.events.editor")
                || cs.hasPermission("quests.admin.trial")) {
            final Conversable c = (Conversable) cs;
            if (!c.isConversing()) {
                plugin.getActionFactory().getConversationFactory().buildConversation(c).begin();
            } else {
                cs.sendMessage(ChatColor.RED + Lang.get("duplicateEditor"));
            }
        } else {
            cs.sendMessage(ChatColor.RED + Lang.get("noPermission"));
        }
        return true;
    }
    
    private boolean questsConditions(final CommandSender cs) {
        if (cs.hasPermission("quests.conditions.*") || cs.hasPermission("quests.conditions.editor")
                || cs.hasPermission("quests.admin.trial")) {
            final Conversable c = (Conversable) cs;
            if (!c.isConversing()) {
                plugin.getConditionFactory().getConversationFactory().buildConversation(c).begin();
            } else {
                cs.sendMessage(ChatColor.RED + Lang.get("duplicateEditor"));
            }
        } else {
            cs.sendMessage(ChatColor.RED + Lang.get("noPermission"));
        }
        return true;
    }

    private boolean questsEditor(final CommandSender cs) {
        if (cs.hasPermission("quests.editor.*") || cs.hasPermission("quests.editor.editor")
                || cs.hasPermission("quests.admin.trial")) {
            final Conversable c = (Conversable) cs;
            if (!c.isConversing()) {
                final Conversation cn = plugin.getQuestFactory().getConversationFactory().buildConversation(c);
                if (cs instanceof Player) {
                    final Quester quester = plugin.getQuester(((Player)cs).getUniqueId());
                    final QuestsCommandPreQuestsEditorEvent event 
                            = new QuestsCommandPreQuestsEditorEvent(quester, cn.getContext());
                    plugin.getServer().getPluginManager().callEvent(event);
                    if (event.isCancelled()) {
                        return false;
                    }
                }
                cn.begin();
            } else {
                cs.sendMessage(ChatColor.RED + Lang.get("duplicateEditor"));
            }
        } else {
            cs.sendMessage(ChatColor.RED + Lang.get("noPermission"));
        }
        return true;
    }

    private boolean questsTop(final CommandSender cs, final String[] args) {
        if (cs.hasPermission("quests.top")) {
            if (args.length > 2) {
                cs.sendMessage(ChatColor.YELLOW + Lang.get("COMMAND_TOP_USAGE"));
            } else {
                int topNumber;
                if (args.length == 1) {
                    topNumber = 5; // default
                } else {
                    try {
                        topNumber = Integer.parseInt(args[1]);
                    } catch (final NumberFormatException e) {
                        cs.sendMessage(ChatColor.YELLOW + Lang.get("inputNum"));
                        return true;
                    }
                }
                if (topNumber < 1 || topNumber > plugin.getSettings().getTopLimit()) {
                    cs.sendMessage(ChatColor.YELLOW + Lang.get("invalidRange").replace("<least>", "1")
                            .replace("<greatest>", String.valueOf(plugin.getSettings().getTopLimit())));
                    return true;
                }
                Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                    @Override
                    public void run() {
                        final File folder = new File(plugin.getDataFolder(), "data");
                        final File[] playerFiles = folder.listFiles();
                        final Map<String, Integer> questPoints = new HashMap<String, Integer>();
                        if (playerFiles != null) {
                            for (final File f : playerFiles) {
                                if (!f.isDirectory()) {
                                    final FileConfiguration data = new YamlConfiguration();
                                    try {
                                        data.load(f);
                                    } catch (final IOException e) {
                                        e.printStackTrace();
                                    } catch (final InvalidConfigurationException e) {
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
                        for (final Entry<String, Integer> entry : sortedMap.entrySet()) {
                            numPrinted++;
                            cs.sendMessage(ChatColor.YELLOW + String.valueOf(numPrinted) + ". " + entry.getKey() + " - " 
                                    + ChatColor.DARK_PURPLE + entry.getValue() + ChatColor.YELLOW + " " 
                                    + Lang.get("questPoints"));
                            if (numPrinted == topNumber) {
                                break;
                            }
                        }
                    }
                });
            }
        }
        return true;
    }

    private void questsStats(final CommandSender cs, final String[] args) {
        if (cs.hasPermission("quests.stats")) {
            OfflinePlayer target;
            if (args != null) {
                target = getPlayer(args[1]);
                if (target == null) {
                    try {
                        target = Bukkit.getOfflinePlayer(UUID.fromString(args[1]));
                    } catch (final IllegalArgumentException e) {
                        cs.sendMessage(ChatColor.YELLOW + Lang.get("playerNotFound"));
                        return;
                    }
                }
            } else {
                target = Bukkit.getOfflinePlayer(((Player)cs).getUniqueId());
            }
            final Quester quester = plugin.getQuester(target.getUniqueId());
            cs.sendMessage(ChatColor.GOLD + "- " + target.getName() + " -");
            cs.sendMessage(ChatColor.YELLOW + Lang.get("questPoints") + " - " + ChatColor.DARK_PURPLE
                    + quester.getQuestPoints());
            if (quester.getCurrentQuests().isEmpty()) {
                cs.sendMessage(ChatColor.YELLOW + Lang.get("currentQuest") + " " + ChatColor.DARK_PURPLE+ Lang.get("none"));
            } else {
                cs.sendMessage(ChatColor.YELLOW + Lang.get("currentQuest"));
                for (final Entry<Quest, Integer> set : quester.getCurrentQuests().entrySet()) {
                    final Quest q = set.getKey();
                    final String msg = ChatColor.LIGHT_PURPLE + " - " + ChatColor.DARK_PURPLE + q.getName()
                        + ChatColor.LIGHT_PURPLE + " (" + Lang.get("stageEditorStage") + " " +  (set.getValue() + 1) + ")";
                    cs.sendMessage(msg);
                }
            }
            cs.sendMessage(ChatColor.YELLOW + Lang.get("completedQuest"));

            if (quester.getCompletedQuests().isEmpty()) {
                cs.sendMessage(ChatColor.DARK_PURPLE + Lang.get("none"));
            } else {
                final StringBuilder completed = new StringBuilder(" ");
                int index = 1;
                for (final Quest q : quester.getCompletedQuests()) {
                    
                    completed.append(ChatColor.DARK_PURPLE + q.getName());
                    if (quester.getAmountsCompleted().containsKey(q) && quester.getAmountsCompleted().get(q) > 1) {
                        completed.append(ChatColor.LIGHT_PURPLE + " (x" + quester.getAmountsCompleted().get(q) + ")");
                    }
                    if (index < (quester.getCompletedQuests().size())) {
                        completed.append(", ");
                    }
                    index++;
                }
                cs.sendMessage(completed.toString());
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void questsJournal(final Player player) {
        if (player.hasPermission("quests.journal")) {
            final Quester quester = plugin.getQuester(player.getUniqueId());
            final QuestsCommandPreQuestsJournalEvent preEvent = new QuestsCommandPreQuestsJournalEvent(quester);
            plugin.getServer().getPluginManager().callEvent(preEvent);
            if (preEvent.isCancelled()) {
                return;
            }
            
            final Inventory inv = player.getInventory();
            final int index = quester.getJournalIndex();
            if (index != -1) {
                inv.setItem(index, null);
                player.sendMessage(ChatColor.YELLOW + Lang.get(player, "journalPutAway")
                        .replace("<journal>", Lang.get(player, "journalTitle")));
            } else if (player.getItemInHand() == null || player.getItemInHand().getType().equals(Material.AIR)) {
                final QuestJournal journal = new QuestJournal(quester);
                player.setItemInHand(journal.toItemStack());
                player.sendMessage(ChatColor.YELLOW + Lang.get(player, "journalTaken")
                        .replace("<journal>", Lang.get(player, "journalTitle")));
                //quester.updateJournal();
            } else if (inv.firstEmpty() != -1) {
                final ItemStack[] arr = inv.getContents();
                for (int i = 0; i < arr.length; i++) {
                    if (arr[i] == null) {
                        final QuestJournal journal = new QuestJournal(quester);
                        inv.setItem(i, journal.toItemStack());
                        player.sendMessage(ChatColor.YELLOW + Lang.get(player, "journalTaken")
                                .replace("<journal>", Lang.get(player, "journalTitle")));
                        //quester.updateJournal();
                        break;
                    }
                }
            } else {
                player.sendMessage(ChatColor.YELLOW + Lang.get(player, "journalNoRoom")
                        .replace("<journal>", Lang.get(player, "journalTitle")));
            }
        }
    }

    private void questsQuit(final Player player, final String[] args) {
        if (player.hasPermission("quests.quit")) {
            if (args.length == 1) {
                player.sendMessage(ChatColor.RED + Lang.get(player, "COMMAND_QUIT_HELP"));
                return;
            }
            final Quester quester = plugin.getQuester(player.getUniqueId());
            if (quester.getCurrentQuests().isEmpty() == false) {
                final Quest quest = plugin.getQuest(concatArgArray(args, 1, args.length - 1, ' '));
                if (quest != null) {
                    if (quest.getOptions().canAllowQuitting()) {
                        final QuestQuitEvent event = new QuestQuitEvent(quest, quester);
                        plugin.getServer().getPluginManager().callEvent(event);
                        if (event.isCancelled()) {
                            return;
                        }
                        String msg = Lang.get("questQuit");
                        msg = msg.replace("<quest>", ChatColor.DARK_PURPLE + quest.getName() + ChatColor.YELLOW);
                        quester.quitQuest(quest, msg);
                    } else {
                        player.sendMessage(ChatColor.YELLOW + Lang.get(player, "questQuitDisabled"));
                    }
                } else {
                    player.sendMessage(ChatColor.RED + Lang.get(player, "questNotFound"));
                }
            } else {
                player.sendMessage(ChatColor.YELLOW + Lang.get(player, "noActiveQuest"));
            }
        } else {
            player.sendMessage(ChatColor.RED + Lang.get(player, "noPermission"));
        }
    }

    private void questsTake(final Player player, final String[] args) {
        if (plugin.getSettings().canAllowCommands() == true) {
            if (player.hasPermission("quests.take")) {
                if (args.length == 1) {
                    player.sendMessage(ChatColor.YELLOW + Lang.get(player, "COMMAND_TAKE_USAGE"));
                } else {
                    final Quest questToFind = plugin.getQuest(concatArgArray(args, 1, args.length - 1, ' '));
                    final Quester quester = plugin.getQuester(player.getUniqueId());
                    if (questToFind != null) {
                        for (final Quest q : quester.getCurrentQuests().keySet()) {
                            if (q.getId().equals(questToFind.getId())) {
                                player.sendMessage(ChatColor.RED + Lang.get(player, "questAlreadyOn"));
                                return;
                            }
                        }
                        //quester.setQuestToTake(questToFind);
                        quester.offerQuest(questToFind, true);
                    } else {
                        player.sendMessage(ChatColor.YELLOW + Lang.get(player, "questNotFound"));
                    }
                }
            } else {
                player.sendMessage(ChatColor.RED + Lang.get(player, "noPermission"));
            }
        } else {
            player.sendMessage(ChatColor.YELLOW + Lang.get(player, "questTakeDisabled"));
        }
    }

    private void questsList(final CommandSender cs, final String[] args) {
        if (cs.hasPermission("quests.list")) {
            if (!(cs instanceof Player)) {
                int num = 1;
                cs.sendMessage(ChatColor.GOLD + Lang.get("questListTitle"));
                for (final Quest q : plugin.getLoadedQuests()) {
                    cs.sendMessage(ChatColor.YELLOW + "" + num + ". " + q.getName());
                    num++;
                }
                return;
            }
            final Player player = (Player)cs;
            if (args.length == 1) {
                final Quester quester = plugin.getQuester(player.getUniqueId());
                final QuestsCommandPreQuestsListEvent preEvent = new QuestsCommandPreQuestsListEvent(quester, 1);
                plugin.getServer().getPluginManager().callEvent(preEvent);
                if (preEvent.isCancelled()) {
                    return;
                }
                
                plugin.listQuests(quester, 1);
            } else if (args.length == 2) {
                int page = 1;
                try {
                    page = Integer.parseInt(args[1]);
                    if (page < 1) {
                        cs.sendMessage(ChatColor.YELLOW + Lang.get("pageSelectionPosNum"));
                        return;
                    } else {
                        final Quester quester = plugin.getQuester(player.getUniqueId());
                        final QuestsCommandPreQuestsListEvent preEvent = new QuestsCommandPreQuestsListEvent(quester, page);
                        plugin.getServer().getPluginManager().callEvent(preEvent);
                        if (preEvent.isCancelled()) {
                            return;
                        }
                        
                        plugin.listQuests(quester, page);
                    }
                } catch (final NumberFormatException e) {
                    cs.sendMessage(ChatColor.YELLOW + Lang.get("pageSelectionNum"));
                    return;
                }
            }
        } else {
            cs.sendMessage(ChatColor.RED + Lang.get("noPermission"));
        }
    }

    private void questsHelp(final CommandSender cs) {
        if (cs.hasPermission("quests.quests")) {
            printHelp(cs);
        } else {
            cs.sendMessage(ChatColor.RED + Lang.get("noPermission"));
        }
    }
    
    public void printHelp(final CommandSender cs) {
        final boolean translateSubCommands = plugin.getSettings().canTranslateSubCommands();
        cs.sendMessage(ChatColor.GOLD + Lang.get("questHelpTitle"));
        cs.sendMessage(ChatColor.YELLOW + "/quests " + Lang.get("questDisplayHelp"));
        if (cs.hasPermission("quests.info")) {
            cs.sendMessage(ChatColor.YELLOW + "/quests " + Lang.get("COMMAND_INFO_HELP")
                    .replace("<command>", ChatColor.GOLD + (translateSubCommands ? Lang.get("COMMAND_INFO")
                    : "info") + ChatColor.YELLOW));
        }
        if (cs.hasPermission("quests.list")) {
            cs.sendMessage(ChatColor.YELLOW + "/quests "+ Lang.get("COMMAND_LIST_HELP")
                    .replace("<command>", ChatColor.GOLD + (translateSubCommands ? Lang.get("COMMAND_LIST")
                    : "list") + ChatColor.YELLOW));
        }
        if (cs instanceof Player && cs.hasPermission("quests.take")) {
            cs.sendMessage(ChatColor.YELLOW + "/quests " + Lang.get("COMMAND_TAKE_HELP")
                    .replace("<command>", ChatColor.GOLD + (translateSubCommands ? Lang.get("COMMAND_TAKE")
                    : "take") + ChatColor.YELLOW));
        }
        if (cs instanceof Player && cs.hasPermission("quests.quit")) {
            cs.sendMessage(ChatColor.YELLOW + "/quests " + Lang.get("COMMAND_QUIT_HELP")
                    .replace("<command>", ChatColor.GOLD + (translateSubCommands ? Lang.get("COMMAND_QUIT")
                    : "quit") + ChatColor.YELLOW));
        }
        if (cs instanceof Player && cs.hasPermission("quests.journal")) {
            cs.sendMessage(ChatColor.YELLOW + "/quests " + Lang.get("COMMAND_JOURNAL_HELP")
                    .replace("<journal>", Lang.get("journalTitle"))
                    .replace("<command>", ChatColor.GOLD + (translateSubCommands ? Lang.get("COMMAND_JOURNAL")
                    : "journal") + ChatColor.YELLOW));
        }
        if (cs.hasPermission("quests.stats")) {
            cs.sendMessage(ChatColor.YELLOW + "/quests " + Lang.get("COMMAND_STATS_HELP")
                    .replace("<command>", ChatColor.GOLD + (translateSubCommands ? Lang.get("COMMAND_STATS")
                    : "stats") + ChatColor.YELLOW));
        }
        if (cs.hasPermission("quests.top")) {
            cs.sendMessage(ChatColor.YELLOW + "/quests " + Lang.get("COMMAND_TOP_HELP")
                    .replace("<command>", ChatColor.GOLD + (translateSubCommands ? Lang.get("COMMAND_TOP")
                    : "top") + ChatColor.YELLOW));
        }
        if (cs.hasPermission("quests.editor.*") || cs.hasPermission("quests.editor.editor")
                || cs.hasPermission("quests.admin.trial")) {
            cs.sendMessage(ChatColor.YELLOW + "/quests " + Lang.get("COMMAND_EDITOR_HELP")
                    .replace("<command>", ChatColor.GOLD + (translateSubCommands ? Lang.get("COMMAND_EDITOR")
                    : "editor") + ChatColor.YELLOW));
        }
        if (cs.hasPermission("quests.events.*") || cs.hasPermission("quests.actions.*") 
                || cs.hasPermission("quests.events.editor") || cs.hasPermission("quests.actions.editor")
                || cs.hasPermission("quests.admin.trial")) {
            cs.sendMessage(ChatColor.YELLOW + "/quests " + Lang.get("COMMAND_EVENTS_EDITOR_HELP")
                    .replace("<command>", ChatColor.GOLD + (translateSubCommands
                    ? Lang.get("COMMAND_EVENTS_EDITOR") : "actions") + ChatColor.YELLOW));
        }
        if (cs.hasPermission("quests.conditions.*") || cs.hasPermission("quests.conditions.editor")
                || cs.hasPermission("quests.admin.trial")) {
            cs.sendMessage(ChatColor.YELLOW + "/quests " + Lang.get("COMMAND_CONDITIONS_EDITOR_HELP")
                    .replace("<command>", ChatColor.GOLD + (translateSubCommands
                    ? Lang.get("COMMAND_CONDITIONS_EDITOR") : "conditions") + ChatColor.YELLOW));
        }
        if (cs instanceof Player) {
            cs.sendMessage(ChatColor.DARK_AQUA + "/quest " + ChatColor.YELLOW + Lang.get("COMMAND_QUEST_HELP"));
        }
        if (cs instanceof Player && cs.hasPermission("quests.questinfo")) {
            cs.sendMessage(ChatColor.DARK_AQUA + "/quest " + ChatColor.YELLOW
                    + Lang.get("COMMAND_QUESTINFO_HELP"));
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED
                    + Lang.get("COMMAND_QUESTADMIN_HELP"));
        }
    }
    
    /**
     * @deprecated Use {@link #printHelp(CommandSender)}
     */
    @Deprecated
    public void printHelp(final Player player) {
        printHelp((CommandSender)player);
    }
    
    public String getQuestsCommandUsage(final String cmd) {
        return ChatColor.RED + Lang.get("usage") + ": " + ChatColor.YELLOW + "/quests "
                + Lang.get(Lang.getKeyFromPrefix("COMMAND_", cmd) + "_HELP");
    }
    
    private void adminHelp(final CommandSender cs) {
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin")) {
            printAdminHelp(cs);
        } else {
            cs.sendMessage(ChatColor.RED + Lang.get("noPermission"));
        }
    }

    private void adminReload(final CommandSender cs) {
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.reload")) {
            final ReloadCallback<Boolean> callback = new ReloadCallback<Boolean>() {
                @Override
                public void execute(final Boolean response) {
                    if (response) {
                        cs.sendMessage(ChatColor.GOLD + Lang.get("questsReloaded"));
                        String msg = Lang.get("numQuestsLoaded");
                        msg = msg.replace("<number>", ChatColor.DARK_PURPLE + String.valueOf(plugin.getLoadedQuests().size())
                                + ChatColor.GOLD);
                        cs.sendMessage(ChatColor.GOLD + msg);
                    } else {
                        cs.sendMessage(ChatColor.RED + Lang.get("unknownError"));
                    }
                }
            };
            plugin.reload(callback);
        } else {
            cs.sendMessage(ChatColor.RED + Lang.get("noPermission"));
        }
    }

    private void adminGivePoints(final CommandSender cs, final String[] args) {
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.givepoints")) {
            OfflinePlayer target = getPlayer(args[1]);
            if (target == null) {
                try {
                    target = Bukkit.getOfflinePlayer(UUID.fromString(args[1]));
                } catch (final IllegalArgumentException e) {
                    cs.sendMessage(ChatColor.YELLOW + Lang.get("playerNotFound"));
                    return;
                }
            }
            int points;
            try {
                points = Integer.parseInt(args[2]);
            } catch (final NumberFormatException e) {
                cs.sendMessage(ChatColor.YELLOW + Lang.get("inputNum"));
                return;
            }
            final Quester quester = plugin.getQuester(target.getUniqueId());
            quester.setQuestPoints(quester.getQuestPoints() + Math.abs(points));
            String msg1 = Lang.get("giveQuestPoints").replace("<points>", Lang.get("questPoints"));
            msg1 = msg1.replace("<player>", ChatColor.GREEN + target.getName() + ChatColor.GOLD);
            msg1 = msg1.replace("<number>", ChatColor.DARK_PURPLE + "" + points + ChatColor.GOLD);
            cs.sendMessage(ChatColor.GOLD + msg1);
            if (target.isOnline()) {
                final Player p = (Player)target;
                String msg2 = Lang.get(p, "questPointsGiven").replace("<points>", Lang.get("questPoints"));
                msg2 = msg2.replace("<player>", ChatColor.GREEN + cs.getName() + ChatColor.GOLD);
                msg2 = msg2.replace("<number>", ChatColor.DARK_PURPLE + "" + points + ChatColor.GOLD);
                p.sendMessage(ChatColor.GREEN + msg2);
            }
            quester.saveData();
        } else {
            cs.sendMessage(ChatColor.RED + Lang.get("noPermission"));
        }
    }

    private void adminTakePoints(final CommandSender cs, final String[] args) {
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.takepoints")) {
            OfflinePlayer target = getPlayer(args[1]);
            if (target == null) {
                try {
                    target = Bukkit.getOfflinePlayer(UUID.fromString(args[1]));
                } catch (final IllegalArgumentException e) {
                    cs.sendMessage(ChatColor.YELLOW + Lang.get("playerNotFound"));
                    return;
                }
            }
            int points;
            try {
                points = Integer.parseInt(args[2]);
            } catch (final NumberFormatException e) {
                cs.sendMessage(ChatColor.YELLOW + Lang.get("inputNum"));
                return;
            }
            final Quester quester = plugin.getQuester(target.getUniqueId());
            quester.setQuestPoints(quester.getQuestPoints() - Math.abs(points));
            String msg1 = Lang.get("takeQuestPoints").replace("<points>", Lang.get("questPoints"));
            msg1 = msg1.replace("<player>", ChatColor.GREEN + target.getName() + ChatColor.GOLD);
            msg1 = msg1.replace("<number>", ChatColor.DARK_PURPLE + "" + points + ChatColor.GOLD);
            cs.sendMessage(ChatColor.GOLD + msg1);
            if (target.isOnline()) {
                final Player p = (Player)target;
                String msg2 = Lang.get(p, "questPointsTaken").replace("<points>", Lang.get("questPoints"));
                msg2 = msg2.replace("<player>", ChatColor.GREEN + cs.getName() + ChatColor.GOLD);
                msg2 = msg2.replace("<number>", ChatColor.DARK_PURPLE + "" + points + ChatColor.GOLD);
                p.sendMessage(ChatColor.GREEN + msg2);
            }
            quester.saveData();
        } else {
            cs.sendMessage(ChatColor.RED + Lang.get("noPermission"));
        }
    }

    private void adminPoints(final CommandSender cs, final String[] args) {
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.points")) {
            OfflinePlayer target = getPlayer(args[1]);
            if (target == null) {
                try {
                    target = Bukkit.getOfflinePlayer(UUID.fromString(args[1]));
                } catch (final IllegalArgumentException e) {
                    cs.sendMessage(ChatColor.YELLOW + Lang.get("playerNotFound"));
                    return;
                }
            }
            int points;
            try {
                points = Integer.parseInt(args[2]);
            } catch (final NumberFormatException e) {
                cs.sendMessage(ChatColor.YELLOW + Lang.get("inputNum"));
                return;
            }
            final Quester quester = plugin.getQuester(target.getUniqueId());
            quester.setQuestPoints(points);
            String msg1 = Lang.get("setQuestPoints").replace("<points>", Lang.get("questPoints"));
            msg1 = msg1.replace("<player>", ChatColor.GREEN + target.getName() + ChatColor.GOLD);
            msg1 = msg1.replace("<number>", ChatColor.DARK_PURPLE + "" + points + ChatColor.GOLD);
            cs.sendMessage(ChatColor.GOLD + msg1);
            if (target.isOnline()) {
                final Player p = (Player)target;
                String msg2 = Lang.get(p, "questPointsSet").replace("<points>", Lang.get("questPoints"));
                msg2 = msg2.replace("<player>", ChatColor.GREEN + cs.getName() + ChatColor.GOLD);
                msg2 = msg2.replace("<number>", ChatColor.DARK_PURPLE + "" + points + ChatColor.GOLD);
                p.sendMessage(ChatColor.GREEN + msg2);
            }
            quester.saveData();
        } else {
            cs.sendMessage(ChatColor.RED + Lang.get("noPermission"));
        }
    }

    private void adminGive(final CommandSender cs, final String[] args) {
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.give")) {
            OfflinePlayer target = getPlayer(args[1]);
            if (target == null) {
                try {
                    target = Bukkit.getOfflinePlayer(UUID.fromString(args[1]));
                } catch (final IllegalArgumentException e) {
                    cs.sendMessage(ChatColor.YELLOW + Lang.get("playerNotFound"));
                    return;
                }
            }
            Quest questToGive;
            String name = "";
            if (args.length == 3) {
                name = args[2].toLowerCase();
            } else {
                for (int i = 2; i < args.length; i++) {
                    final int lastIndex = args.length - 1;
                    if (i == lastIndex) {
                        name = name + args[i].toLowerCase();
                    } else {
                        name = name + args[i].toLowerCase() + " ";
                    }
                }
            }
            questToGive = plugin.getQuest(name);
            if (questToGive == null) {
                cs.sendMessage(ChatColor.YELLOW + Lang.get("questNotFound"));
            } else {
                final Quester quester = plugin.getQuester(target.getUniqueId());
                for (final Quest q : quester.getCurrentQuests().keySet()) {
                    if (q.getName().equalsIgnoreCase(questToGive.getName())) {
                        String msg = Lang.get("questsPlayerHasQuestAlready");
                        msg = msg.replace("<player>", ChatColor.ITALIC + "" + ChatColor.GREEN + target.getName()
                                + ChatColor.RESET + ChatColor.YELLOW);
                        msg = msg.replace("<quest>", ChatColor.ITALIC + "" + ChatColor.DARK_PURPLE
                                + questToGive.getName() + ChatColor.RESET + ChatColor.YELLOW);
                        cs.sendMessage(ChatColor.YELLOW + msg);
                        return;
                    }
                }
                quester.hardQuit(questToGive);
                String msg1 = Lang.get("questForceTake");
                msg1 = msg1.replace("<player>", ChatColor.GREEN + target.getName() + ChatColor.GOLD);
                msg1 = msg1.replace("<quest>", ChatColor.DARK_PURPLE + questToGive.getName() + ChatColor.GOLD);
                cs.sendMessage(ChatColor.GOLD + msg1);
                if (target.isOnline()) {
                    final Player p = (Player)target;
                    String msg2 = Lang.get(p, "questForcedTake");
                    msg2 = msg2.replace("<player>", ChatColor.GREEN + cs.getName() + ChatColor.GOLD);
                    msg2 = msg2.replace("<quest>", ChatColor.DARK_PURPLE + questToGive.getName() + ChatColor.GOLD);
                    p.sendMessage(ChatColor.GREEN + msg2);
                }
                quester.takeQuest(questToGive, true);
            }
        } else {
            cs.sendMessage(ChatColor.RED + Lang.get("noPermission"));
        }
    }

    private void adminPointsAll(final CommandSender cs, final String[] args) {
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.points.all")) {
            final int amount;
            try {
                amount = Integer.parseInt(args[1]);
                if (amount < 0) {
                    cs.sendMessage(ChatColor.RED + Lang.get("inputPosNum"));
                    return;
                }
            } catch (final NumberFormatException e) {
                cs.sendMessage(ChatColor.RED + Lang.get("inputNum"));
                return;
            }
            cs.sendMessage(ChatColor.YELLOW + Lang.get("settingAllQuestPoints")
                    .replace("<points>", Lang.get("questPoints")));
            for (final Quester q : plugin.getOfflineQuesters()) {
                q.setQuestPoints(amount);
            }
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
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
                                } catch (final IOException e) {
                                    if (failCount < 10) {
                                        String msg = Lang.get("errorReading");
                                        msg = msg.replace("<file>", ChatColor.DARK_AQUA + f.getName() + ChatColor.RED);
                                        cs.sendMessage(ChatColor.RED + msg);
                                        failCount++;
                                    } else if (suppressed == false) {
                                        String msg = Lang.get("errorReadingSuppress");
                                        msg = msg.replace("<file>", ChatColor.DARK_AQUA + f.getName() + ChatColor.RED);
                                        cs.sendMessage(ChatColor.RED + msg);
                                        suppressed = true;
                                    }
                                } catch (final InvalidConfigurationException e) {
                                    if (failCount < 10) {
                                        String msg = Lang.get("errorReading");
                                        msg = msg.replace("<file>", ChatColor.DARK_AQUA + f.getName() + ChatColor.RED);
                                        cs.sendMessage(ChatColor.RED + msg);
                                        failCount++;
                                    } else if (suppressed == false) {
                                        String msg = Lang.get("errorReadingSuppress");
                                        msg = msg.replace("<file>", ChatColor.DARK_AQUA + f.getName() + ChatColor.RED);
                                        cs.sendMessage(ChatColor.RED + msg);
                                        suppressed = true;
                                    }
                                }
                            }
                        }
                        cs.sendMessage(ChatColor.GREEN + Lang.get("done"));
                        String msg = Lang.get("allQuestPointsSet").replace("<points>", Lang.get("questPoints"));
                        msg = msg.replace("<number>", ChatColor.AQUA + "" + amount + ChatColor.GOLD);
                        plugin.getServer().broadcastMessage(ChatColor.YELLOW + "" + ChatColor.GOLD + msg);
                    } else {
                        cs.sendMessage(ChatColor.RED + Lang.get("errorDataFolder"));
                    }
                }
            });
        } else {
            cs.sendMessage(ChatColor.RED + Lang.get("noPermission"));
        }
    }

    private void adminFinish(final CommandSender cs, final String[] args) {
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.finish")) {
            OfflinePlayer target = getPlayer(args[1]);
            if (target == null) {
                try {
                    target = Bukkit.getOfflinePlayer(UUID.fromString(args[1]));
                } catch (final IllegalArgumentException e) {
                    cs.sendMessage(ChatColor.YELLOW + Lang.get("playerNotFound"));
                    return;
                }
            }
            final Quester quester = plugin.getQuester(target.getUniqueId());
            if (quester.getCurrentQuests().isEmpty()) {
                String msg = Lang.get("noCurrentQuest");
                msg = msg.replace("<player>", target.getName());
                cs.sendMessage(ChatColor.YELLOW + msg);
            } else {
                final Quest quest = plugin.getQuest(concatArgArray(args, 2, args.length - 1, ' '));
                if (quest == null) {
                    cs.sendMessage(ChatColor.RED + Lang.get("questNotFound"));
                    return;
                }
                String msg1 = Lang.get("questForceFinish");
                msg1 = msg1.replace("<player>", ChatColor.GREEN + target.getName() + ChatColor.GOLD);
                msg1 = msg1.replace("<quest>", ChatColor.DARK_PURPLE + quest.getName() + ChatColor.GOLD);
                cs.sendMessage(ChatColor.GOLD + msg1);
                if (target.isOnline()) {
                    final Player p = (Player)target;
                    String msg2 = Lang.get(p, "questForcedFinish");
                    msg2 = msg2.replace("<player>", ChatColor.GREEN + cs.getName() + ChatColor.GOLD);
                    msg2 = msg2.replace("<quest>", ChatColor.DARK_PURPLE + quest.getName() + ChatColor.GOLD);
                    p.sendMessage(ChatColor.GREEN + msg2);
                }
                quest.completeQuest(quester);
                quester.saveData();
            }
        } else {
            cs.sendMessage(ChatColor.RED + Lang.get("noPermission"));
        }
    }

    private void adminSetStage(final CommandSender cs, final String[] args) {
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.setstage")) {
            OfflinePlayer target = getPlayer(args[1]);
            if (target == null) {
                try {
                    target = Bukkit.getOfflinePlayer(UUID.fromString(args[1]));
                } catch (final IllegalArgumentException e) {
                    cs.sendMessage(ChatColor.YELLOW + Lang.get("playerNotFound"));
                    return;
                }
            }
            int stage = -1;
            if (args.length > 3) {
                try {
                    stage = Integer.parseInt(args[args.length - 1]);
                } catch (final NumberFormatException e) {
                    cs.sendMessage(ChatColor.YELLOW + Lang.get("inputNum"));
                }
            } else {
                cs.sendMessage(ChatColor.YELLOW + Lang.get("COMMAND_QUESTADMIN_SETSTAGE_USAGE"));
                return;
            }
            final Quester quester = plugin.getQuester(target.getUniqueId());
            if (quester.getCurrentQuests().isEmpty()) {
                String msg = Lang.get("noCurrentQuest");
                msg = msg.replace("<player>", target.getName());
                cs.sendMessage(ChatColor.YELLOW + msg);
            } else {
                final Quest quest = plugin.getQuest(concatArgArray(args, 2, args.length - 2, ' '));
                if (quest == null) {
                    cs.sendMessage(ChatColor.RED + Lang.get("questNotFound"));
                    return;
                }
                try {
                    quest.setStage(quester, stage - 1);
                } catch (final IndexOutOfBoundsException e) {
                    String msg = Lang.get("invalidRange");
                    msg = msg.replace("<least>", "1").replace("<greatest>", String.valueOf(quest.getStages().size()));
                    cs.sendMessage(ChatColor.RED + msg);
                }
                quester.saveData();
            }
        } else {
            cs.sendMessage(ChatColor.RED + Lang.get("noPermission"));
        }
    }

    private void adminNextStage(final CommandSender cs, final String[] args) {
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.nextstage")) {
            OfflinePlayer target = getPlayer(args[1]);
            if (target == null) {
                try {
                    target = Bukkit.getOfflinePlayer(UUID.fromString(args[1]));
                } catch (final IllegalArgumentException e) {
                    cs.sendMessage(ChatColor.YELLOW + Lang.get("playerNotFound"));
                    return;
                }
            }
            final Quester quester = plugin.getQuester(target.getUniqueId());
            if (quester.getCurrentQuests().isEmpty()) {
                String msg = Lang.get("noCurrentQuest");
                msg = msg.replace("<player>", target.getName());
                cs.sendMessage(ChatColor.YELLOW + msg);
            } else {
                final Quest quest = plugin.getQuest(concatArgArray(args, 2, args.length - 1, ' '));
                if (quest == null) {
                    cs.sendMessage(ChatColor.RED + Lang.get("questNotFound"));
                    return;
                }
                String msg1 = Lang.get("questForceNextStage");
                msg1 = msg1.replace("<player>", ChatColor.GREEN + target.getName() + ChatColor.GOLD);
                msg1 = msg1.replace("<quest>", ChatColor.DARK_PURPLE + quest.getName() + ChatColor.GOLD);
                cs.sendMessage(ChatColor.GOLD + msg1);
                if (target.isOnline()) {
                    final Player p = (Player)target;
                    String msg2 = Lang.get(p, "questForcedNextStage");
                    msg2 = msg2.replace("<player>", ChatColor.GREEN + cs.getName() + ChatColor.GOLD);
                    msg2 = msg2.replace("<quest>", ChatColor.DARK_PURPLE + quest.getName() + ChatColor.GOLD);
                    p.sendMessage(ChatColor.GREEN + msg2);
                }
                quest.nextStage(quester, false);
                quester.saveData();
            }
        } else {
            cs.sendMessage(ChatColor.RED + Lang.get("noPermission"));
        }
    }

    private void adminQuit(final CommandSender cs, final String[] args) {
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.quit")) {
            OfflinePlayer target = getPlayer(args[1]);
            if (target == null) {
                try {
                    target = Bukkit.getOfflinePlayer(UUID.fromString(args[1]));
                } catch (final IllegalArgumentException e) {
                    cs.sendMessage(ChatColor.YELLOW + Lang.get("playerNotFound"));
                    return;
                }
            }
            final Quester quester = plugin.getQuester(target.getUniqueId());
            if (quester.getCurrentQuests().isEmpty()) {
                String msg = Lang.get("noCurrentQuest");
                msg = msg.replace("<player>", target.getName());
                cs.sendMessage(ChatColor.YELLOW + msg);
            } else {
                final Quest quest = plugin.getQuest(concatArgArray(args, 2, args.length - 1, ' '));
                if (quest == null) {
                    cs.sendMessage(ChatColor.RED + Lang.get("questNotFound"));
                    return;
                }
                String msg1 = Lang.get("questForceQuit");
                msg1 = msg1.replace("<player>", ChatColor.GREEN + target.getName() + ChatColor.GOLD);
                msg1 = msg1.replace("<quest>", ChatColor.DARK_PURPLE + quest.getName() + ChatColor.GOLD);
                cs.sendMessage(ChatColor.GOLD + msg1);
                String msg2 = Lang.get((Player)target, "questForcedQuit");
                msg2 = msg2.replace("<player>", ChatColor.GREEN + cs.getName() + ChatColor.GOLD);
                msg2 = msg2.replace("<quest>", ChatColor.DARK_PURPLE + quest.getName() + ChatColor.GOLD);
                quester.quitQuest(quest, msg2);
            }
        } else {
            cs.sendMessage(ChatColor.RED + Lang.get("noPermission"));
        }
    }

    private void adminReset(final CommandSender cs, final String[] args) {
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.reset")) {
            OfflinePlayer target = getPlayer(args[1]);
            if (target == null) {
                try {
                    target = Bukkit.getOfflinePlayer(UUID.fromString(args[1]));
                } catch (final IllegalArgumentException e) {
                    cs.sendMessage(ChatColor.YELLOW + Lang.get("playerNotFound"));
                    return;
                }
            }
            final UUID id = target.getUniqueId();
            final ConcurrentSkipListSet<Quester> temp = (ConcurrentSkipListSet<Quester>) plugin.getOfflineQuesters();
            for (final Iterator<Quester> itr = temp.iterator(); itr.hasNext();) {
                if (itr.next().getUUID().equals(id)) {
                    itr.remove();
                }
            }
            plugin.setOfflineQuesters(temp);
            Quester quester = plugin.getQuester(id);
            try {
                quester.hardClear();
                quester.saveData();
                quester.updateJournal();
                final Storage storage = plugin.getStorage();
                storage.deleteQuesterData(id);
                String msg = Lang.get("questReset");
                if (target.getName() != null) {
                    msg = msg.replace("<player>", ChatColor.GREEN + target.getName() + ChatColor.GOLD);
                } else {
                    msg = msg.replace("<player>", ChatColor.GREEN + args[1] + ChatColor.GOLD);
                }
                cs.sendMessage(ChatColor.GOLD + msg);
                cs.sendMessage(ChatColor.DARK_PURPLE + " UUID: " + ChatColor.DARK_AQUA + id);
            } catch (final Exception e) {
                plugin.getLogger().info("Data file does not exist for " + id.toString());
            }
            quester = new Quester(plugin, id);
            quester.saveData();
            final ConcurrentSkipListSet<Quester> temp2 = (ConcurrentSkipListSet<Quester>) plugin.getOfflineQuesters();
            temp2.add(quester);
            plugin.setOfflineQuesters(temp2);
        } else {
            cs.sendMessage(ChatColor.RED + Lang.get("noPermission"));
        }
    }

    private void adminStats(final CommandSender cs, final String[] args) {
        if (cs.hasPermission("quests.admin.*") && cs.hasPermission("quests.admin.stats")) {
            questsStats(cs, args);
        } else {
            cs.sendMessage(ChatColor.RED + Lang.get("noPermission"));
        }
    }

    private void adminRemove(final CommandSender cs, final String[] args) {
        if (cs.hasPermission("quests.admin.*") && cs.hasPermission("quests.admin.remove")) {
            OfflinePlayer target = getPlayer(args[1]);
            if (target == null) {
                try {
                    target = Bukkit.getOfflinePlayer(UUID.fromString(args[1]));
                } catch (final IllegalArgumentException e) {
                    cs.sendMessage(ChatColor.YELLOW + Lang.get("playerNotFound"));
                    return;
                }
            }
            final Quest toRemove = plugin.getQuest(concatArgArray(args, 2, args.length - 1, ' '));
            if (toRemove == null) {
                cs.sendMessage(ChatColor.RED + Lang.get("questNotFound"));
                return;
            }
            final Quester quester = plugin.getQuester(target.getUniqueId());
            String msg = Lang.get("questRemoved");
            if (target.getName() != null) {
                msg = msg.replace("<player>", ChatColor.GREEN + target.getName() + ChatColor.GOLD);
            } else {
                msg = msg.replace("<player>", ChatColor.GREEN + args[1] + ChatColor.GOLD);
            }
            msg = msg.replace("<quest>", ChatColor.DARK_PURPLE + toRemove.getName() + ChatColor.AQUA);
            cs.sendMessage(ChatColor.GOLD + msg);
            cs.sendMessage(ChatColor.DARK_PURPLE + " UUID: " + ChatColor.DARK_AQUA + quester.getUUID().toString());
            quester.hardRemove(toRemove);
            quester.saveData();
            quester.updateJournal();
        } else {
            cs.sendMessage(ChatColor.RED + Lang.get("noPermission"));
        }
    }
    
    public void printAdminHelp(final CommandSender cs) {
        cs.sendMessage(ChatColor.GOLD + Lang.get("questAdminHelpTitle"));
        cs.sendMessage(ChatColor.YELLOW + "/questadmin" + ChatColor.RED + " " + Lang.get("COMMAND_QUESTADMIN_HELP"));
        final boolean translateSubCommands = plugin.getSettings().canTranslateSubCommands();
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.stats")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED + Lang.get("COMMAND_QUESTADMIN_STATS_HELP")
                    .replace("<command>", ChatColor.GOLD + (translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_STATS")
                    : "stats") + ChatColor.RED));
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.give")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED + Lang.get("COMMAND_QUESTADMIN_GIVE_HELP")
                    .replace("<command>", ChatColor.GOLD + (translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_GIVE")
                    : "give") + ChatColor.RED));
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.quit")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED + Lang.get("COMMAND_QUESTADMIN_QUIT_HELP")
                    .replace("<command>", ChatColor.GOLD + (translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_QUIT")
                    : "quit") + ChatColor.RED));
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.points")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED
                    + Lang.get("COMMAND_QUESTADMIN_POINTS_HELP")
                    .replace("<points>", Lang.get("questPoints"))
                    .replace("<command>", ChatColor.GOLD
                    + (translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_POINTS") : "points") + ChatColor.RED));
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.takepoints")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED
                    + Lang.get("COMMAND_QUESTADMIN_TAKEPOINTS_HELP")
                    .replace("<points>", Lang.get("questPoints"))
                    .replace("<command>", ChatColor.GOLD
                    + (translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_TAKEPOINTS") : "takepoints")
                    + ChatColor.RED));
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.givepoints")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED
                    + Lang.get("COMMAND_QUESTADMIN_GIVEPOINTS_HELP")
                    .replace("<points>", Lang.get("questPoints"))
                    .replace("<command>", ChatColor.GOLD
                    + (translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_GIVEPOINTS") : "givepoints")
                    + ChatColor.RED));
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.pointsall")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED
                    + Lang.get("COMMAND_QUESTADMIN_POINTSALL_HELP")
                    .replace("<points>", Lang.get("questPoints"))
                    .replace("<command>", ChatColor.GOLD
                    + (translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_POINTSALL") : "pointsall") + ChatColor.RED));
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.finish")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED
                    + Lang.get("COMMAND_QUESTADMIN_FINISH_HELP").replace("<command>", ChatColor.GOLD
                    + (translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_FINISH") : "finish") + ChatColor.RED));
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.nextstage")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED
                    + Lang.get("COMMAND_QUESTADMIN_NEXTSTAGE_HELP").replace("<command>", ChatColor.GOLD
                    + (translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_NEXTSTAGE") : "nextstage") + ChatColor.RED));
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.setstage")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED
                    + Lang.get("COMMAND_QUESTADMIN_SETSTAGE_HELP").replace("<command>", ChatColor.GOLD
                    + (translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_SETSTAGE") : "setstage") + ChatColor.RED));
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.reset")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED+ Lang.get("COMMAND_QUESTADMIN_RESET_HELP")
                    .replace("<command>", ChatColor.GOLD + (translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_RESET")
                    : "reset") + ChatColor.RED));
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.remove")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED
                    + Lang.get("COMMAND_QUESTADMIN_REMOVE_HELP").replace("<command>", ChatColor.GOLD
                    + (translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_REMOVE") : "remove") + ChatColor.RED));
        }
        if (cs.hasPermission("quests.admin.*") || cs.hasPermission("quests.admin.reload")) {
            cs.sendMessage(ChatColor.YELLOW + "/questadmin " + ChatColor.RED
                    + Lang.get("COMMAND_QUESTADMIN_RELOAD_HELP").replace("<command>", ChatColor.GOLD
                    + (translateSubCommands ? Lang.get("COMMAND_QUESTADMIN_RELOAD") : "reload") + ChatColor.RED));
        }
    }

    public String getQuestadminCommandUsage(final String cmd) {
        return ChatColor.RED + Lang.get("usage") + ": " + ChatColor.YELLOW + "/questadmin "
                + Lang.get(Lang.getKeyFromPrefix("COMMAND_QUESTADMIN_", cmd) + "_HELP");
    }
    
    private static Map<String, Integer> sort(final Map<String, Integer> unsortedMap) {
        final List<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(unsortedMap.entrySet());
        Collections.sort(list, new Comparator<Entry<String, Integer>>() {

            @Override
            public int compare(final Entry<String, Integer> o1, final Entry<String, Integer> o2) {
                final int i = o1.getValue();
                final int i2 = o2.getValue();
                if (i < i2) {
                    return 1;
                } else if (i == i2) {
                    return 0;
                } else {
                    return -1;
                }
            }
        });
        final Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (final Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }
    
    /**
     * Used to get quest names that contain spaces from command input
     * 
     * @param args an array of Strings
     * @param startingIndex the index to start combining at
     * @param endingIndex the index to stop combining at
     * @param delimiter the character for which the array was split
     * @return a String or null
     */
    private static String concatArgArray(final String[] args, final int startingIndex, final int endingIndex, final char delimiter) {
        String s = "";
        for (int i = startingIndex; i <= endingIndex; i++) {
            s += args[i] + delimiter;
        }
        s = s.substring(0, s.length());
        return s.trim().equals("") ? null : s.trim();
    }
    
    /**
     * Get an online Player by name
     * 
     * @param name Name of the player
     * @return Player or null if not found
     */
    private Player getPlayer(final String name) {
        if (name == null) {
            return null;
        }
        for (final Player p : plugin.getServer().getOnlinePlayers()) {
            if (p.getName().equalsIgnoreCase(name)) {
                return p;
            }
        }
        for (final Player p : plugin.getServer().getOnlinePlayers()) {
            if (p.getName().toLowerCase().startsWith(name)) {
                return p;
            }
        }
        for (final Player p : plugin.getServer().getOnlinePlayers()) {
            if (p.getName().toLowerCase().contains(name)) {
                return p;
            }
        }
        return null;
    }
}
