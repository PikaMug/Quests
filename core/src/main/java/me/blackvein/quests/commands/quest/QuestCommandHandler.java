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

package me.blackvein.quests.commands.quest;

import me.blackvein.quests.Quests;
import me.blackvein.quests.player.IQuester;
import me.blackvein.quests.quests.IQuest;
import me.blackvein.quests.quests.IStage;
import me.blackvein.quests.quests.Requirements;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuestCommandHandler {

    private final Quests plugin;

    public QuestCommandHandler(Quests plugin) {
        this.plugin = plugin;
    }

    public boolean check(final CommandSender cs, final String[] args) {
        if (cs instanceof Player) {
            if (cs.hasPermission("quests.quest")) {
                if (args.length == 0) {
                    final Player player = (Player) cs;
                    final IQuester quester = plugin.getQuester(player.getUniqueId());
                    if (!quester.getCurrentQuestsTemp().isEmpty()) {
                        for (final IQuest q : quester.getCurrentQuestsTemp().keySet()) {
                            final IStage stage = quester.getCurrentStage(q);
                            q.updateCompass(quester, stage);
                            if (plugin.getQuester(player.getUniqueId()).getQuestData(q).getDelayStartTime() == 0
                                    || plugin.getQuester(player.getUniqueId()).getStageTime(q) < 0L) {
                                final String msg = Lang.get(player, "questObjectivesTitle")
                                        .replace("<quest>", q.getName());
                                Lang.send(player, ChatColor.GOLD + msg);
                                plugin.showObjectives(q, quester, false);
                            } else {
                                final long time = plugin.getQuester(player.getUniqueId()).getStageTime(q);
                                String msg = ChatColor.YELLOW + "(" + Lang.get(player, "delay") + ") " + ChatColor.RED
                                        +  Lang.get(player, "plnTooEarly");
                                msg = msg.replace("<quest>", q.getName());
                                msg = msg.replace("<time>", MiscUtil.getTime(time));
                                Lang.send(player, msg);
                            }
                        }
                    } else {
                        Lang.send(player, ChatColor.YELLOW + Lang.get(player, "noActiveQuest"));
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

    public List<String> suggest(final CommandSender cs, final String[] args) {
        if (args.length != 1) {
            return Collections.emptyList();
        }
        final List<String> results = new ArrayList<>();
        for (final IQuest quest : plugin.getLoadedQuests()) {
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
            final IQuest q = plugin.getQuestTemp(name.toString());
            if (q != null) {
                final Player player = (Player) cs;
                final IQuester quester = plugin.getQuester(player.getUniqueId());
                cs.sendMessage(ChatColor.GOLD + "- " + q.getName() + " -");
                cs.sendMessage(" ");
                if (q.getNpcStart() != null) {
                    String msg = Lang.get("speakTo");
                    msg = msg.replace("<npc>", q.getNpcStartName());
                    cs.sendMessage(ChatColor.YELLOW + msg);
                } else {
                    cs.sendMessage(ChatColor.YELLOW + q.getDescription());
                }
                cs.sendMessage(" ");
                if (plugin.getSettings().canShowQuestReqs()) {
                    final Requirements reqs = q.getRequirements();
                    if (reqs.hasRequirement()) {
                        cs.sendMessage(ChatColor.GOLD + Lang.get("requirements"));
                        if (!reqs.getPermissions().isEmpty()) {
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
                        if (!reqs.getMcmmoSkills().isEmpty()) {
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
                        if (!reqs.getItems().isEmpty()) {
                            for (final ItemStack is : reqs.getItems()) {
                                if (plugin.getQuester(player.getUniqueId()).hasItem(is)) {
                                    cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.GREEN + ItemUtil.getString(is));
                                } else {
                                    cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.RED + ItemUtil.getString(is));
                                }
                            }
                        }
                        if (!reqs.getNeededQuests().isEmpty()) {
                            for (final IQuest quest : reqs.getNeededQuests()) {
                                if (quester.getCompletedQuestsTemp().contains(quest)) {
                                    cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.GREEN + Lang.get("complete") + " "
                                            + ChatColor.ITALIC + quest.getName());
                                } else {
                                    cs.sendMessage(ChatColor.GRAY + "- " + ChatColor.RED + Lang.get("complete") + " "
                                            + ChatColor.ITALIC + quest.getName());
                                }
                            }
                        }
                        if (!reqs.getBlockQuests().isEmpty()) {
                            for (final IQuest quest : reqs.getBlockQuests()) {
                                if (quester.getCompletedQuestsTemp().contains(quest)) {
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
                }
            } else {
                cs.sendMessage(ChatColor.YELLOW + Lang.get("questNotFound"));
            }
        } else {
            cs.sendMessage(ChatColor.RED + Lang.get("noPermission"));
        }
    }
}
