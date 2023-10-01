/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.commands.quest;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.components.BukkitRequirements;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.Stage;
import me.pikamug.quests.util.BukkitItemUtil;
import me.pikamug.quests.util.BukkitMiscUtil;
import me.pikamug.quests.util.BukkitLang;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BukkitQuestCommandHandler {

    private final BukkitQuestsPlugin plugin;

    public BukkitQuestCommandHandler(BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean check(final CommandSender cs, final String[] args) {
        if (cs instanceof Player) {
            if (cs.hasPermission("quests.quest")) {
                if (args.length == 0) {
                    final Player player = (Player) cs;
                    final Quester quester = plugin.getQuester(player.getUniqueId());
                    if (!quester.getCurrentQuests().isEmpty()) {
                        for (final Quest q : quester.getCurrentQuests().keySet()) {
                            final Stage stage = quester.getCurrentStage(q);
                            q.updateCompass(quester, stage);
                            if (plugin.getQuester(player.getUniqueId()).getQuestDataOrDefault(q).getDelayStartTime() == 0
                                    || plugin.getQuester(player.getUniqueId()).getStageTime(q) < 0L) {
                                final String msg = BukkitLang.get(player, "questObjectivesTitle")
                                        .replace("<quest>", q.getName());
                                BukkitLang.send(player, ChatColor.GOLD + msg);
                                quester.showCurrentObjectives(q, quester, false);
                            } else {
                                final long time = plugin.getQuester(player.getUniqueId()).getStageTime(q);
                                String msg = ChatColor.YELLOW + "(" + BukkitLang.get(player, "delay") + ") " + ChatColor.RED
                                        +  BukkitLang.get(player, "plnTooEarly");
                                msg = msg.replace("<quest>", q.getName());
                                msg = msg.replace("<time>", BukkitMiscUtil.getTime(time));
                                BukkitLang.send(player, msg);
                            }
                        }
                    } else {
                        BukkitLang.send(player, ChatColor.YELLOW + BukkitLang.get(player, "noActiveQuest"));
                    }
                } else {
                    showQuestDetails(cs, args);
                }
            } else {
                cs.sendMessage(ChatColor.RED + BukkitLang.get("noPermission"));
                return true;
            }
        } else {
            cs.sendMessage(ChatColor.YELLOW + BukkitLang.get("consoleError"));
            return true;
        }
        return true;
    }

    public List<String> suggest(final CommandSender cs, final String[] args) {
        if (args.length != 1) {
            return Collections.emptyList();
        }
        final List<String> results = new ArrayList<>();
        for (final Quest quest : plugin.getLoadedQuests()) {
            if (quest.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                results.add(ChatColor.stripColor(quest.getName()));
            }
        }
        return results;
    }

    private void showQuestDetails(final CommandSender cs, final String[] args) {
        if (cs.hasPermission("quests.questinfo")) {
            StringBuilder name = new StringBuilder();
            if (args.length == 1) {
                name = new StringBuilder(args[0].toLowerCase());
            } else {
                int index = 0;
                for (final String s : args) {
                    if (index == (args.length - 1)) {
                        name.append(s.toLowerCase());
                    } else {
                        name.append(s.toLowerCase()).append(" ");
                    }
                    index++;
                }
            }
            final Quest q = plugin.getQuest(name.toString());
            if (q != null) {
                final Player player = (Player) cs;
                final Quester quester = plugin.getQuester(player.getUniqueId());
                cs.sendMessage(ChatColor.GOLD + "- " + q.getName() + " -");
                cs.sendMessage(" ");
                if (q.getNpcStart() != null) {
                    String msg = BukkitLang.get("speakTo");
                    msg = msg.replace("<npc>", q.getNpcStartName());
                    cs.sendMessage(ChatColor.YELLOW + msg);
                } else {
                    cs.sendMessage(ChatColor.YELLOW + q.getDescription());
                }
                cs.sendMessage(" ");
                if (plugin.getConfigSettings().canShowQuestReqs()) {
                    final BukkitRequirements reqs = (BukkitRequirements) q.getRequirements();
                    if (reqs.hasRequirement()) {
                        cs.sendMessage(ChatColor.GOLD + BukkitLang.get("requirements"));
                        if (!reqs.getPermissions().isEmpty()) {
                            for (final String perm : reqs.getPermissions()) {
                                if (plugin.getDependencies().getVaultPermission().has(player, perm)) {
                                    cs.sendMessage(ChatColor.GREEN + BukkitLang.get("permissionDisplay") + " " + perm);
                                } else {
                                    cs.sendMessage(ChatColor.RED + BukkitLang.get("permissionDisplay") + " " + perm);
                                }
                            }
                        }
                        if (reqs.getHeroesPrimaryClass() != null) {
                            if (plugin.getDependencies()
                                    .testPrimaryHeroesClass(reqs.getHeroesPrimaryClass(), player.getUniqueId())) {
                                cs.sendMessage(ChatColor.BOLD + "" + ChatColor.GREEN + reqs.getHeroesPrimaryClass()
                                        + ChatColor.RESET + "" + ChatColor.DARK_GREEN + " " + BukkitLang.get("heroesClass"));
                            } else {
                                cs.sendMessage(ChatColor.BOLD + "" + ChatColor.DARK_RED + reqs.getHeroesPrimaryClass()
                                        + ChatColor.RESET + "" + ChatColor.RED + " " + BukkitLang.get("heroesClass"));
                            }
                        }
                        if (reqs.getHeroesSecondaryClass() != null) {
                            if (plugin.getDependencies()
                                    .testSecondaryHeroesClass(reqs.getHeroesSecondaryClass(), player.getUniqueId())) {
                                cs.sendMessage(ChatColor.BOLD + "" + ChatColor.DARK_RED + reqs.getHeroesSecondaryClass()
                                        + ChatColor.RESET + "" + ChatColor.RED + " " + BukkitLang.get("heroesClass"));
                            } else {
                                cs.sendMessage(ChatColor.BOLD + "" + ChatColor.GREEN + reqs.getHeroesSecondaryClass()
                                        + ChatColor.RESET + "" + ChatColor.DARK_GREEN + " " + BukkitLang.get("heroesClass"));
                            }
                        }
                        for (final String skill : reqs.getMcmmoSkills()) {
                            final int level = plugin.getDependencies().getMcmmoSkillLevel(plugin.getDependencies()
                                    .getMcMMOSkill(skill), player.getName());
                            final int req = reqs.getMcmmoAmounts().get(reqs.getMcmmoSkills().indexOf(skill));
                            final String skillName = BukkitMiscUtil.getCapitalized(skill);
                            if (level >= req) {
                                cs.sendMessage(ChatColor.GREEN + skillName + " " + BukkitLang.get("mcMMOLevel") + " " + req);
                            } else {
                                cs.sendMessage(ChatColor.RED + skillName + " " + BukkitLang.get("mcMMOLevel") + " " + req);
                            }
                        }
                        if (reqs.getQuestPoints() != 0) {
                            if (quester.getQuestPoints() >= reqs.getQuestPoints()) {
                                cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.GREEN + reqs.getQuestPoints() + " "
                                        + BukkitLang.get("questPoints"));
                            } else {
                                cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.RED + reqs.getQuestPoints() + " "
                                        + BukkitLang.get("questPoints"));
                            }
                        }
                        if (reqs.getMoney() != 0) {
                            if (plugin.getDependencies().getVaultEconomy() != null && plugin.getDependencies()
                                    .getVaultEconomy().getBalance(quester.getOfflinePlayer()) >= reqs.getMoney()) {
                                cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.GREEN + reqs.getMoney() + " "
                                        + (reqs.getMoney() > 1 ? plugin.getDependencies().getVaultEconomy()
                                        .currencyNamePlural() : plugin.getDependencies().getVaultEconomy()
                                        .currencyNameSingular()));
                            } else {
                                cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.RED + reqs.getMoney() + " "
                                        + (reqs.getMoney() > 1 ? plugin.getDependencies().getVaultEconomy()
                                        .currencyNamePlural() : plugin.getDependencies().getVaultEconomy()
                                        .currencyNameSingular()));
                            }
                        }
                        for (final ItemStack is : reqs.getItems()) {
                            if (plugin.getQuester(player.getUniqueId()).hasItem(is)) {
                                cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.GREEN + BukkitItemUtil.getString(is));
                            } else {
                                cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.RED + BukkitItemUtil.getString(is));
                            }
                        }
                        for (Quest quest : quester.getCompletedQuests()) {
                            if (reqs.getNeededQuestIds().contains(quest.getId())) {
                                cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.GREEN + BukkitLang.get("complete") + " "
                                        + ChatColor.ITALIC + quest.getName());
                            } else {
                                cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.RED + BukkitLang.get("complete") + " "
                                        + ChatColor.ITALIC + quest.getName());
                            }
                        }
                        final Map<String, String> completed = quester.getCompletedQuests().stream()
                                .collect(Collectors.toMap(Quest::getId, Quest::getName));
                        for (final String questId : reqs.getBlockQuestIds()) {
                            if (completed.containsKey(questId)) {
                                String msg = BukkitLang.get("haveCompleted");
                                msg = msg.replace("<quest>", ChatColor.ITALIC + "" + ChatColor.DARK_PURPLE
                                        + completed.get(questId) + ChatColor.RED);
                                cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.RED + msg);
                            } else {
                                String msg = BukkitLang.get("cannotComplete");
                                msg = msg.replace("<quest>", ChatColor.ITALIC + "" + ChatColor.DARK_PURPLE
                                        + plugin.getQuestById(questId).getName() + ChatColor.GREEN);
                                cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.GREEN + msg);
                            }
                        }
                    }
                }
            } else {
                cs.sendMessage(ChatColor.YELLOW + BukkitLang.get("questNotFound"));
            }
        } else {
            cs.sendMessage(ChatColor.RED + BukkitLang.get("noPermission"));
        }
    }
}
