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

package me.blackvein.quests.item;

import me.blackvein.quests.quests.IQuest;
import me.blackvein.quests.player.IQuester;
import me.blackvein.quests.util.Lang;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class QuestJournal {
    
    final IQuester owner;
    ItemStack journal = new ItemStack(Material.WRITTEN_BOOK);
    
    public QuestJournal(final IQuester owner) {
        this.owner = owner;
        final BookMeta book = (BookMeta) journal.getItemMeta();
        if (book != null) {
            final Player player = owner.getPlayer();
            final String title = Lang.get(player, "journalTitle");
            book.setDisplayName(ChatColor.LIGHT_PURPLE + title);
            book.setTitle(ChatColor.LIGHT_PURPLE + title);
            book.setAuthor(player.getName());
            if (owner.getCurrentQuests().isEmpty()) {
                book.addPage(ChatColor.DARK_RED + Lang.get(player, "journalNoQuests").replace("<journal>", title));
            } else {
                int currentLength = 0;
                int currentLines = 0;
                StringBuilder page = new StringBuilder();
                final List<IQuest> sortedList = owner.getCurrentQuests().keySet().stream()
                        .sorted(Comparator.comparing(IQuest::getName))
                        .collect(Collectors.toList());
                for (final IQuest quest : sortedList) {
                    if ((currentLength + quest.getName().length() > 240) || (currentLines
                            + ((quest.getName().length() % 19) == 0 ? (quest.getName().length() / 19)
                            : ((quest.getName().length() / 19) + 1))) > 13) {
                        book.addPage(page.toString());
                        page.append(ChatColor.DARK_PURPLE).append(ChatColor.BOLD).append(quest.getName()).append("\n");
                        currentLength = quest.getName().length();
                        currentLines = (quest.getName().length() % 19) == 0 ? (quest.getName().length() / 19)
                                : (quest.getName().length() + 1);
                    } else {
                        page.append(ChatColor.DARK_PURPLE).append(ChatColor.BOLD).append(quest.getName()).append("\n");
                        currentLength += quest.getName().length();
                        currentLines += (quest.getName().length() / 19);
                    }
                    if (owner.getCurrentObjectives(quest, false) != null) {
                        for (final String obj : owner.getCurrentObjectives(quest, false)) {
                            // Length/Line check
                            if ((currentLength + obj.length() > 240) || (currentLines + ((obj.length() % 19)
                                    == 0 ? (obj.length() / 19) : ((obj.length() / 19) + 1))) > 13) {
                                book.addPage(page.toString());
                                page = new StringBuilder(obj + "\n");
                                currentLength = obj.length();
                                currentLines = (obj.length() % 19) == 0 ? (obj.length() / 19) : (obj.length() + 1);
                            } else {
                                page.append(obj).append("\n");
                                currentLength += obj.length();
                                currentLines += (obj.length() / 19);
                            }
                        }
                    }
                    if (currentLines < 13)
                        page.append("\n");
                    book.addPage(page.toString());
                    page = new StringBuilder();
                    currentLines = 0;
                    currentLength = 0;
                }
            }
            journal.setItemMeta(book);
        }
    }
    
    public IQuester getOwner() {
        return owner;
    }
    
    public ItemStack toItemStack() {
        return journal;
    }
}
