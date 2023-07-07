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

package me.pikamug.quests.commands.quest;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.BukkitRequirements;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.Stage;
import me.pikamug.quests.util.BukkitItemUtil;
import me.pikamug.quests.util.BukkitMiscUtil;
import me.pikamug.quests.util.Language;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QuestCommandHandler {

    private final BukkitQuestsPlugin plugin;

    public QuestCommandHandler(BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean check(final CommandSender cs, final String[] args) {
        if (cs instanceof Player) {
            if (cs.hasPermission("quests.quest")) {
                if (args.length == 0) {
                    final Player player = (Player) cs;
                    final Quester quester = plugin.getQuester(player.getUniqueId());
                    if (!quester.getCurrentQuestsTemp().isEmpty()) {
                        for (final Quest q : quester.getCurrentQuestsTemp().keySet()) {
                            final Stage stage = quester.getCurrentStage(q);
                            q.updateCompass(quester, stage);
                            if (plugin.getQuester(player.getUniqueId()).getQuestData(q).getDelayStartTime() == 0
                                    || plugin.getQuester(player.getUniqueId()).getStageTime(q) < 0L) {
                                final String msg = Language.get(player, "questObjectivesTitle")
                                        .replace("<quest>", q.getName());
                                Language.send(player, ChatColor.GOLD + msg);
                                quester.showCurrentObjectives(q, quester, false);
                            } else {
                                final long time = plugin.getQuester(player.getUniqueId()).getStageTime(q);
                                String msg = ChatColor.YELLOW + "(" + Language.get(player, "delay") + ") " + ChatColor.RED
                                        +  Language.get(player, "plnTooEarly");
                                msg = msg.replace("<quest>", q.getName());
                                msg = msg.replace("<time>", BukkitMiscUtil.getTime(time));
                                Language.send(player, msg);
                            }
                        }
                    } else {
                        Language.send(player, ChatColor.YELLOW + Language.get(player, "noActiveQuest"));
                    }
                } else {
                    showQuestDetails(cs, args);
                }
            } else {
                cs.sendMessage(ChatColor.RED + Language.get("noPermission"));
                return true;
            }
        } else {
            cs.sendMessage(ChatColor.YELLOW + Language.get("consoleError"));
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
            final Quest q = plugin.getQuestTemp(name.toString());
            if (q != null) {
                final Player player = (Player) cs;
                final Quester quester = plugin.getQuester(player.getUniqueId());
                cs.sendMessage(ChatColor.GOLD + "- " + q.getName() + " -");
                cs.sendMessage(" ");
                if (q.getNpcStart() != null) {
                    String msg = Language.get("speakTo");
                    msg = msg.replace("<npc>", q.getNpcStartName());
                    cs.sendMessage(ChatColor.YELLOW + msg);
                } else {
                    cs.sendMessage(ChatColor.YELLOW + q.getDescription());
                }
                cs.sendMessage(" ");
                if (plugin.getSettings().canShowQuestReqs()) {
                    final BukkitRequirements reqs = (BukkitRequirements) q.getRequirements();
                    if (reqs.hasRequirement()) {
                        cs.sendMessage(ChatColor.GOLD + Language.get("requirements"));
                        if (!reqs.getPermissions().isEmpty()) {
                            for (final String perm : reqs.getPermissions()) {
                                if (plugin.getDependencies().getVaultPermission().has(player, perm)) {
                                    cs.sendMessage(ChatColor.GREEN + Language.get("permissionDisplay") + " " + perm);
                                } else {
                                    cs.sendMessage(ChatColor.RED + Language.get("permissionDisplay") + " " + perm);
                                }
                            }
                        }
                        if (reqs.getHeroesPrimaryClass() != null) {
                            if (plugin.getDependencies()
                                    .testPrimaryHeroesClass(reqs.getHeroesPrimaryClass(), player.getUniqueId())) {
                                cs.sendMessage(ChatColor.BOLD + "" + ChatColor.GREEN + reqs.getHeroesPrimaryClass()
                                        + ChatColor.RESET + "" + ChatColor.DARK_GREEN + " " + Language.get("heroesClass"));
                            } else {
                                cs.sendMessage(ChatColor.BOLD + "" + ChatColor.DARK_RED + reqs.getHeroesPrimaryClass()
                                        + ChatColor.RESET + "" + ChatColor.RED + " " + Language.get("heroesClass"));
                            }
                        }
                        if (reqs.getHeroesSecondaryClass() != null) {
                            if (plugin.getDependencies()
                                    .testSecondaryHeroesClass(reqs.getHeroesSecondaryClass(), player.getUniqueId())) {
                                cs.sendMessage(ChatColor.BOLD + "" + ChatColor.DARK_RED + reqs.getHeroesSecondaryClass()
                                        + ChatColor.RESET + "" + ChatColor.RED + " " + Language.get("heroesClass"));
                            } else {
                                cs.sendMessage(ChatColor.BOLD + "" + ChatColor.GREEN + reqs.getHeroesSecondaryClass()
                                        + ChatColor.RESET + "" + ChatColor.DARK_GREEN + " " + Language.get("heroesClass"));
                            }
                        }
                        for (final String skill : reqs.getMcmmoSkills()) {
                            final int level = plugin.getDependencies().getMcmmoSkillLevel(plugin.getDependencies()
                                    .getMcMMOSkill(skill), player.getName());
                            final int req = reqs.getMcmmoAmounts().get(reqs.getMcmmoSkills().indexOf(skill));
                            final String skillName = BukkitMiscUtil.getCapitalized(skill);
                            if (level >= req) {
                                cs.sendMessage(ChatColor.GREEN + skillName + " " + Language.get("mcMMOLevel") + " " + req);
                            } else {
                                cs.sendMessage(ChatColor.RED + skillName + " " + Language.get("mcMMOLevel") + " " + req);
                            }
                        }
                        if (reqs.getQuestPoints() != 0) {
                            if (quester.getQuestPoints() >= reqs.getQuestPoints()) {
                                cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.GREEN + reqs.getQuestPoints() + " "
                                        + Language.get("questPoints"));
                            } else {
                                cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.RED + reqs.getQuestPoints() + " "
                                        + Language.get("questPoints"));
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
                        for (Quest quest : quester.getCompletedQuestsTemp()) {
                            if (reqs.getNeededQuestIds().contains(quest.getId())) {
                                cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.GREEN + Language.get("complete") + " "
                                        + ChatColor.ITALIC + quest.getName());
                            } else {
                                cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.RED + Language.get("complete") + " "
                                        + ChatColor.ITALIC + quest.getName());
                            }
                        }
                        final Map<String, String> completed = quester.getCompletedQuestsTemp().stream()
                                .collect(Collectors.toMap(Quest::getId, Quest::getName));
                        for (final String questId : reqs.getBlockQuestIds()) {
                            if (completed.containsKey(questId)) {
                                String msg = Language.get("haveCompleted");
                                msg = msg.replace("<quest>", ChatColor.ITALIC + "" + ChatColor.DARK_PURPLE
                                        + completed.get(questId) + ChatColor.RED);
                                cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.RED + msg);
                            } else {
                                String msg = Language.get("cannotComplete");
                                msg = msg.replace("<quest>", ChatColor.ITALIC + "" + ChatColor.DARK_PURPLE
                                        + plugin.getQuestById(questId).getName() + ChatColor.GREEN);
                                cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.GREEN + msg);
                            }
                        }
                    }
                }
            } else {
                cs.sendMessage(ChatColor.YELLOW + Language.get("questNotFound"));
            }
        } else {
            cs.sendMessage(ChatColor.RED + Language.get("noPermission"));
        }
    }
}
