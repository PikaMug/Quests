package me.blackvein.quests.item;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import me.blackvein.quests.Quest;
import me.blackvein.quests.Quester;
import me.blackvein.quests.util.Lang;

public class QuestJournal {
    
    final Quester owner;
    ItemStack journal = new ItemStack(Material.WRITTEN_BOOK);
    
    public QuestJournal(final Quester owner) {
        this.owner = owner;
        final BookMeta book = (BookMeta) journal.getItemMeta();
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
            String page = "";
            final List<Quest> sortedList = owner.getCurrentQuests().keySet().stream()
                    .sorted(Comparator.comparing(Quest::getName))
                    .collect(Collectors.toList());
            for (final Quest quest : sortedList) {
                if ((currentLength + quest.getName().length() > 240) || (currentLines 
                        + ((quest.getName().length() % 19) == 0 ? (quest.getName().length() / 19) 
                        : ((quest.getName().length() / 19) + 1))) > 13) {
                    book.addPage(page);
                    page += ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + quest.getName() + "\n";
                    currentLength = quest.getName().length();
                    currentLines = (quest.getName().length() % 19) == 0 ? (quest.getName().length() / 19) 
                            : (quest.getName().length() + 1);
                } else {
                    page += ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + quest.getName() + "\n";
                    currentLength += quest.getName().length();
                    currentLines += (quest.getName().length() / 19);
                }
                if (owner.getCurrentObjectives(quest, false) != null) {
                    for (final String obj : owner.getCurrentObjectives(quest, false)) {
                        // Length/Line check
                        if ((currentLength + obj.length() > 240) || (currentLines + ((obj.length() % 19) 
                                == 0 ? (obj.length() / 19) : ((obj.length() / 19) + 1))) > 13) {
                            book.addPage(page);
                            page = obj + "\n";
                            currentLength = obj.length();
                            currentLines = (obj.length() % 19) == 0 ? (obj.length() / 19) : (obj.length() + 1);
                        } else {
                            page += obj + "\n";
                            currentLength += obj.length();
                            currentLines += (obj.length() / 19);
                        }
                    }
                }
                if (currentLines < 13)
                    page += "\n";
                book.addPage(page);
                page = "";
                currentLines = 0;
                currentLength = 0;
            }
        }
        journal.setItemMeta(book);
    }
    
    public Quester getOwner() {
        return owner;
    }
    
    public ItemStack toItemStack() {
        return journal;
    }
}
