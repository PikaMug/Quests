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

package me.pikamug.quests.commands.quests.subcommands;

import me.pikamug.quests.player.BukkitQuester;
import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.commands.QuestsSubCommand;
import me.pikamug.quests.events.command.QuestsCommandPreQuestsJournalEvent;
import me.pikamug.quests.item.BukkitQuestJournal;
import me.pikamug.quests.util.Language;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import xyz.upperlevel.spigot.book.BookUtil;

public class QuestsJournalCommand extends QuestsSubCommand {

    private final BukkitQuestsPlugin plugin;

    public QuestsJournalCommand(BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "journal";
    }

    @Override
    public String getNameI18N() {
        return Language.get("COMMAND_JOURNAL");
    }

    @Override
    public String getDescription() {
        return Language.get("COMMAND_JOURNAL_HELP");
    }

    @Override
    public String getPermission() {
        return "quests.journal";
    }

    @Override
    public String getSyntax() {
        return "/quests journal";
    }

    @Override
    public int getMaxArguments() {
        return 1;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void execute(CommandSender cs, String[] args) {
        if (assertNonPlayer(cs)) {
            return;
        }
        final Player player = (Player) cs;
        if (player.hasPermission(getPermission())) {
            final BukkitQuester quester = plugin.getQuester(player.getUniqueId());
            final QuestsCommandPreQuestsJournalEvent preEvent = new QuestsCommandPreQuestsJournalEvent(quester);
            plugin.getServer().getPluginManager().callEvent(preEvent);
            if (preEvent.isCancelled()) {
                return;
            }

            if (!plugin.getSettings().canGiveJournalItem()) {
                final BukkitQuestJournal journal = new BukkitQuestJournal(plugin, quester);
                BookUtil.openPlayer(player, journal.toItemStack());
            } else {
                final Inventory inv = player.getInventory();
                final int index = quester.getJournalIndex();
                if (index != -1) {
                    inv.setItem(index, null);
                    Language.send(player, ChatColor.YELLOW + Language.get(player, "journalPutAway")
                            .replace("<journal>", Language.get(player, "journalTitle")));
                } else if (player.getItemInHand().getType().equals(Material.AIR)) {
                    final BukkitQuestJournal journal = new BukkitQuestJournal(plugin, quester);
                    player.setItemInHand(journal.toItemStack());
                    Language.send(player, ChatColor.YELLOW + Language.get(player, "journalTaken")
                            .replace("<journal>", Language.get(player, "journalTitle")));
                } else if (inv.firstEmpty() != -1) {
                    final ItemStack[] arr = inv.getContents();
                    for (int i = 0; i < arr.length; i++) {
                        if (arr[i] == null) {
                            final BukkitQuestJournal journal = new BukkitQuestJournal(plugin, quester);
                            inv.setItem(i, journal.toItemStack());
                            Language.send(player, ChatColor.YELLOW + Language.get(player, "journalTaken")
                                    .replace("<journal>", Language.get(player, "journalTitle")));
                            break;
                        }
                    }
                } else {
                    Language.send(player, ChatColor.YELLOW + Language.get(player, "journalNoRoom")
                            .replace("<journal>", Language.get(player, "journalTitle")));
                }
            }
        }
    }
}
