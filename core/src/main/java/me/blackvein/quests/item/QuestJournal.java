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

import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;
import me.blackvein.quests.player.IQuester;
import me.blackvein.quests.quests.BukkitObjective;
import me.blackvein.quests.quests.IQuest;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.upperlevel.spigot.book.BookUtil;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class QuestJournal {

    final Quests plugin;
    final IQuester owner;
    final ItemStack journal;
    
    public QuestJournal(Quests plugin, final IQuester owner) {
        this.plugin = plugin;
        this.owner = owner;
        final Player player = owner.getPlayer();
        final String title = ChatColor.LIGHT_PURPLE + Lang.get(player, "journalTitle");
        journal = BookUtil.writtenBook()
                .author(player.getName())
                .title(title)
                .pages(getPages())
                .build();
        if (journal.getItemMeta() != null) {
            ItemMeta meta = journal.getItemMeta();
            meta.setDisplayName(title);
            journal.setItemMeta(meta);
        }
    }

    public List<BaseComponent[]> getPages() {
        if (owner.getCurrentQuestsTemp().isEmpty()) {
            final Player player = owner.getPlayer();
            final String title = Lang.get(player, "journalTitle");
            return Collections.singletonList(new BookUtil.PageBuilder().add(new TextComponent(ChatColor.DARK_RED
                    + Lang.get(player, "journalNoQuests").replace("<journal>", title))).build());
        } else {
            final List<BaseComponent[]> pages = new LinkedList<>();
            final List<IQuest> sortedList = owner.getCurrentQuestsTemp().keySet().stream()
                    .sorted(Comparator.comparing(IQuest::getName))
                    .collect(Collectors.toList());
            for (final IQuest quest : sortedList) {
                final TextComponent title = new TextComponent(quest.getName());
                title.setColor(net.md_5.bungee.api.ChatColor.DARK_PURPLE);
                title.setBold(true);
                final BookUtil.PageBuilder builder = new BookUtil.PageBuilder().add(title).newLine();
                for (final BukkitObjective obj : ((Quester)owner).getCurrentObjectivesTemp(quest, false, false)) {
                    if (obj.getMessage() != null) {
                        String[] split = null;
                        if (obj.getMessage().contains("<item>") && obj.getGoalAsItem() != null) {
                            split = obj.getMessage().split("<item>");
                            builder.add(split[0]);
                            if (plugin.getSettings().canTranslateNames()) {
                                final TranslatableComponent tc = new TranslatableComponent(plugin.getLocaleManager()
                                        .queryItemStack(obj.getGoalAsItem()));
                                tc.setColor(net.md_5.bungee.api.ChatColor.DARK_AQUA);
                                builder.add(tc);
                            } else {
                                builder.add(ItemUtil.getName(obj.getGoalAsItem()));
                            }
                            builder.add(split[1]).newLine();
                        }
                        if (obj.getMessage().contains("<mob>") && obj.getGoalAsMob() != null) {
                            split = obj.getMessage().split("<mob>");
                            builder.add(split[0]);
                            if (plugin.getSettings().canTranslateNames()) {
                                final TranslatableComponent tc = new TranslatableComponent(plugin.getLocaleManager()
                                        .queryEntityType(obj.getGoalAsMob().getEntityType(), null)); // TODO extra data
                                tc.setColor(net.md_5.bungee.api.ChatColor.DARK_RED);
                                builder.add(tc);
                            } else {
                                builder.add(MiscUtil.snakeCaseToUpperCamelCase(obj.getGoalAsMob().getEntityType().name()));
                            }
                            builder.add(split[1]).newLine();
                        }
                        if (split == null) {
                            builder.add(obj.getMessage()).newLine();
                        }
                    }
                }
                pages.add(builder.build());
            }
            return pages;
        }
    }
    
    public IQuester getOwner() {
        return owner;
    }
    
    public ItemStack toItemStack() {
        return journal;
    }
}
