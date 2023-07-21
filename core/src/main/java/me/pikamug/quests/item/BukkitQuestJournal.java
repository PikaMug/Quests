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

package me.pikamug.quests.item;

import me.pikamug.quests.player.BukkitQuester;
import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.BukkitObjective;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.util.BukkitItemUtil;
import me.pikamug.quests.util.Language;
import me.pikamug.quests.util.BukkitMiscUtil;
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

public class BukkitQuestJournal {

    final BukkitQuestsPlugin plugin;
    final Quester owner;
    final ItemStack journal;
    
    public BukkitQuestJournal(BukkitQuestsPlugin plugin, final Quester owner) {
        this.plugin = plugin;
        this.owner = owner;
        final Player player = owner.getPlayer();
        final String title = ChatColor.LIGHT_PURPLE + Language.get(player, "journalTitle");
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
        if (owner.getCurrentQuests().isEmpty()) {
            final Player player = owner.getPlayer();
            final String title = Language.get(player, "journalTitle");
            return Collections.singletonList(new BookUtil.PageBuilder().add(new TextComponent(ChatColor.DARK_RED
                    + Language.get(player, "journalNoQuests").replace("<journal>", title))).build());
        } else {
            final List<BaseComponent[]> pages = new LinkedList<>();
            final List<Quest> sortedList = owner.getCurrentQuests().keySet().stream()
                    .sorted(Comparator.comparing(Quest::getName))
                    .collect(Collectors.toList());
            for (final Quest quest : sortedList) {
                final TextComponent title = new TextComponent(quest.getName());
                title.setColor(net.md_5.bungee.api.ChatColor.DARK_PURPLE);
                title.setBold(true);
                final BookUtil.PageBuilder builder = new BookUtil.PageBuilder().add(title).newLine();
                for (final BukkitObjective obj : ((BukkitQuester)owner).getCurrentObjectivesTemp(quest, false, false)) {
                    if (!plugin.getSettings().canShowCompletedObjs()
                            && obj.getMessage().startsWith(ChatColor.GRAY.toString())) {
                        continue;
                    }
                    if (obj.getMessage() != null) {
                        String[] split = null;
                        if (obj.getMessage().contains("<item>") && obj.getGoalAsItem() != null) {
                            split = obj.getMessage().split("<item>");
                            builder.add(split[0]);
                            final ItemStack goal = obj.getGoalAsItem();
                            if (goal.getItemMeta() != null && goal.getItemMeta().hasDisplayName()) {
                                builder.add("" + ChatColor.DARK_AQUA + ChatColor.ITALIC
                                        + goal.getItemMeta().getDisplayName());
                            } else {
                                if (plugin.getSettings().canTranslateNames()) {
                                    final TranslatableComponent tc = new TranslatableComponent(plugin.getLocaleManager()
                                            .queryItemStack(goal));
                                    tc.setColor(net.md_5.bungee.api.ChatColor.DARK_AQUA);
                                    builder.add(tc);
                                } else {
                                    builder.add(ChatColor.AQUA + BukkitItemUtil.getPrettyItemName(goal.getType().name()));
                                }
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
                                builder.add(BukkitMiscUtil.snakeCaseToUpperCamelCase(obj.getGoalAsMob().getEntityType().name()));
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
    
    public Quester getOwner() {
        return owner;
    }
    
    public ItemStack toItemStack() {
        return journal;
    }
}
