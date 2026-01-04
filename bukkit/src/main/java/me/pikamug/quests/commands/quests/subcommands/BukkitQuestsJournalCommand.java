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

import me.pikamug.quests.player.BukkitQuester;
import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.commands.BukkitQuestsSubCommand;
import me.pikamug.quests.events.command.BukkitQuestsCommandPreJournalEvent;
import me.pikamug.quests.item.BukkitQuestJournal;
import me.pikamug.quests.util.BukkitLang;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import xyz.upperlevel.spigot.book.BookUtil;

public class BukkitQuestsJournalCommand extends BukkitQuestsSubCommand {

    private final BukkitQuestsPlugin plugin;

    public BukkitQuestsJournalCommand(BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "journal";
    }

    @Override
    public String getNameI18N() {
        return BukkitLang.get("COMMAND_JOURNAL");
    }

    @Override
    public String getDescription() {
        return BukkitLang.get("COMMAND_JOURNAL_HELP");
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
            final BukkitQuestsCommandPreJournalEvent preEvent = new BukkitQuestsCommandPreJournalEvent(quester);
            plugin.getServer().getPluginManager().callEvent(preEvent);
            if (preEvent.isCancelled()) {
                return;
            }

            if (!plugin.getConfigSettings().canGiveJournalItem()) {
                final BukkitQuestJournal journal = new BukkitQuestJournal(plugin, quester);
                BookUtil.openPlayer(player, journal.toItemStack());
            } else {
                final Inventory inv = player.getInventory();
                final int index = quester.getJournalIndex();
                if (index != -1) {
                    inv.setItem(index, null);
                    BukkitLang.send(player, ChatColor.YELLOW + BukkitLang.get(player, "journalPutAway")
                            .replace("<journal>", BukkitLang.get(player, "journalTitle")));
                } else if (player.getItemInHand().getType().equals(Material.AIR)) {
                    final BukkitQuestJournal journal = new BukkitQuestJournal(plugin, quester);
                    player.setItemInHand(journal.toItemStack());
                    BukkitLang.send(player, ChatColor.YELLOW + BukkitLang.get(player, "journalTaken")
                            .replace("<journal>", BukkitLang.get(player, "journalTitle")));
                } else if (inv.firstEmpty() != -1) {
                    final ItemStack[] arr = inv.getContents();
                    for (int i = 0; i < arr.length; i++) {
                        if (arr[i] == null) {
                            final BukkitQuestJournal journal = new BukkitQuestJournal(plugin, quester);
                            inv.setItem(i, journal.toItemStack());
                            BukkitLang.send(player, ChatColor.YELLOW + BukkitLang.get(player, "journalTaken")
                                    .replace("<journal>", BukkitLang.get(player, "journalTitle")));
                            break;
                        }
                    }
                } else {
                    BukkitLang.send(player, ChatColor.YELLOW + BukkitLang.get(player, "journalNoRoom")
                            .replace("<journal>", BukkitLang.get(player, "journalTitle")));
                }
            }
        }
    }
}
