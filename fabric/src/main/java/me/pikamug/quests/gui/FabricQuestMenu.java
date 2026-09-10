/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.gui;

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.player.FabricQuester;
import me.pikamug.quests.quests.Quest;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.ChestMenu;

import java.util.LinkedList;
import java.util.List;

/**
 * Chest-menu quest selection replacing Bukkit's command-based quest list.
 * Slots are non-interactive; clicking one offers that quest (see the click
 * mixin in {@code MixinAbstractContainerMenu}).
 */
public final class FabricQuestMenu {

    private static final Item[] ICONS = {
            Items.PAPER, Items.BOOK, Items.MAP, Items.WRITABLE_BOOK, Items.EMERALD,
            Items.GOLD_INGOT, Items.IRON_INGOT, Items.COAL, Items.SPRUCE_SAPLING,
            Items.FEATHER, Items.GLOWSTONE_DUST, Items.AMETHYST_SHARD
    };

    private FabricQuestMenu() {
    }

    public static boolean show(ServerPlayer player, LinkedList<Quest> quests) {
        if (player == null || quests == null || quests.isEmpty()) return false;
        final int rows = quests.size() <= 27 ? 3 : 6;
        final int size = rows * 9;
        final QuestMenuContainer container = new QuestMenuContainer(quests, size);
        for (int i = 0; i < quests.size() && i < size; i++) {
            final Quest quest = quests.get(i);
            final ItemStack stack = new ItemStack(ICONS[i % ICONS.length]);
            stack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,
                    Component.literal(ChatFormatting.YELLOW + quest.getName()));
            final String description = quest.getDescription();
            if (description != null && !description.isEmpty()) {
                stack.set(net.minecraft.core.component.DataComponents.LORE,
                        new net.minecraft.world.item.component.ItemLore(
                                List.of(Component.literal(ChatFormatting.GRAY + description))));
            }
            container.setItem(i, stack);
        }
        player.openMenu(new net.minecraft.world.SimpleMenuProvider((id, inv, p) -> rows == 6
                ? ChestMenu.sixRows(id, inv, container)
                : ChestMenu.threeRows(id, inv, container),
                net.minecraft.network.chat.Component.literal("Available Quests")));
        return true;
    }

    public static final class QuestMenuContainer extends SimpleContainer {

        private final LinkedList<Quest> quests;

        QuestMenuContainer(LinkedList<Quest> quests, int size) {
            super(size);
            this.quests = quests;
        }

        /**
         * Offers the quest bound to the given slot. No-op when the slot has no
         * assignment, so the click mixin can safely call it for any slot.
         *
         * @return {@code true} when a quest was offered
         */
        public boolean select(ServerPlayer player, int slot) {
            if (slot < 0 || slot >= quests.size()) return false;
            final Quest quest = quests.get(slot);
            if (quest == null) return false;
            final FabricQuester quester = FabricQuestsPlugin.getInstance().getQuester(player.getUUID());
            return quester.offerQuest(quest, true);
        }
    }
}