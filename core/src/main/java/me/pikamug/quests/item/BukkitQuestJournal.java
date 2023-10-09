/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.item;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.components.BukkitObjective;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.Objective;
import me.pikamug.quests.util.BukkitItemUtil;
import me.pikamug.quests.util.BukkitLang;
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
        final String title = ChatColor.LIGHT_PURPLE + BukkitLang.get(player, "journalTitle");
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
            final String title = BukkitLang.get(player, "journalTitle");
            return Collections.singletonList(new BookUtil.PageBuilder().add(new TextComponent(ChatColor.DARK_RED
                    + BukkitLang.get(player, "journalNoQuests").replace("<journal>", title))).build());
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
                for (final Objective obj : owner.getCurrentObjectives(quest, false, false)) {
                    final BukkitObjective objective = (BukkitObjective) obj;
                    if (!plugin.getConfigSettings().canShowCompletedObjs()
                            && objective.getMessage().startsWith(ChatColor.GRAY.toString())) {
                        continue;
                    }
                    if (objective.getMessage() != null) {
                        String[] split = null;
                        if (objective.getMessage().contains("<item>") && objective.getGoalAsItem() != null) {
                            split = objective.getMessage().split("<item>");
                            builder.add(split[0]);
                            final ItemStack goal = objective.getGoalAsItem();
                            if (goal.getItemMeta() != null && goal.getItemMeta().hasDisplayName()) {
                                builder.add("" + ChatColor.DARK_AQUA + ChatColor.ITALIC
                                        + goal.getItemMeta().getDisplayName());
                            } else {
                                if (plugin.getConfigSettings().canTranslateNames()) {
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
                        if (objective.getMessage().contains("<mob>") && objective.getGoalAsMob() != null) {
                            split = objective.getMessage().split("<mob>");
                            builder.add(split[0]);
                            if (plugin.getConfigSettings().canTranslateNames()) {
                                final TranslatableComponent tc = new TranslatableComponent(plugin.getLocaleManager()
                                        .queryEntityType(objective.getGoalAsMob().getEntityType(), null)); // TODO extra data
                                tc.setColor(net.md_5.bungee.api.ChatColor.DARK_RED);
                                builder.add(tc);
                            } else {
                                builder.add(BukkitMiscUtil.snakeCaseToUpperCamelCase(objective.getGoalAsMob().getEntityType().name()));
                            }
                            builder.add(split[1]).newLine();
                        }
                        if (split == null) {
                            builder.add(objective.getMessage()).newLine();
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
