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

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.player.FabricQuester;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.Objective;
import me.pikamug.quests.util.FabricLang;
import me.pikamug.quests.util.FabricMiscUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class FabricQuestJournal {

    final FabricQuestsPlugin plugin;
    final Quester owner;
    final ItemStack journal;

    public FabricQuestJournal(FabricQuestsPlugin plugin, final Quester owner) {
        this.plugin = plugin;
        this.owner = owner;
        final ServerPlayer player = FabricMiscUtil.getPlayer(owner.getUUID(), plugin);
        final String title = FabricLang.get("journalTitle");
        journal = new ItemStack(Items.WRITTEN_BOOK);
        final CompoundTag tag = journal.getOrCreateTag();
        tag.putString("title", title);
        tag.putString("author", player != null ? player.getGameProfile().getName() : "Quests");
        final ListTag pages = new ListTag();
        for (final Component page : getPages()) {
            pages.add(StringTag.valueOf(Component.Serializer.toJson(page)));
        }
        tag.put("pages", pages);
        tag.putByte("resolved", (byte) 1);
        tag.putBoolean("quests.journal", true);
        journal.setHoverName(Component.literal(title).withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    public List<Component> getPages() {
        if (owner.getCurrentQuests().isEmpty()) {
            final String title = FabricLang.get("journalTitle");
            return Collections.singletonList(Component.literal(FabricLang.get("journalNoQuests")
                    .replace("<journal>", title)).withStyle(ChatFormatting.DARK_RED));
        }
        final List<Component> pages = new LinkedList<>();
        final List<Quest> sortedList = owner.getCurrentQuests().keySet().stream()
                .sorted(Comparator.comparing(Quest::getName))
                .collect(Collectors.toList());
        for (final Quest quest : sortedList) {
            final MutableComponent page = Component.empty();
            page.append(Component.literal(quest.getName()).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
            for (final Objective obj : owner.getCurrentObjectives(quest, false, false)) {
                if (obj.getMessage() == null) {
                    continue;
                }
                if (!plugin.getConfigSettings().canShowCompletedObjs()
                        && obj.getProgress() >= obj.getGoal()) {
                    continue;
                }
                page.append(Component.literal("\n- " + obj.getMessage()
                        + " (" + obj.getProgress() + "/" + obj.getGoal() + ")")
                        .withStyle(ChatFormatting.GRAY));
            }
            pages.add(page);
        }
        return pages;
    }

    public Quester getOwner() {
        return owner;
    }

    public ItemStack toItemStack() {
        return journal;
    }
}