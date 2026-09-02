/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.listeners;

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.player.FabricQuester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.Stage;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FabricCraftingListener {

    private final FabricQuestsPlugin plugin;
    private final Map<UUID, Tracker> trackers = new HashMap<>();

    public FabricCraftingListener(FabricQuestsPlugin plugin) {
        this.plugin = plugin;
        ServerTickEvents.END_SERVER_TICK.register(this::onTick);
    }

    private void onTick(net.minecraft.server.MinecraftServer server) {
        if (plugin.isLoading()) return;
        for (final ServerPlayer player : server.getPlayerList().getPlayers()) {
            final AbstractContainerMenu menu = player.containerMenu;
            final UUID id = player.getUUID();
            Tracker tracker = trackers.get(id);
            if (tracker == null || tracker.menu != menu) {
                tracker = new Tracker(menu);
                trackers.put(id, tracker);
                tracker.snapshot();
                continue;
            }
            detectCraft(player, tracker);
            detectSmelt(player, tracker);
            detectEnchant(player, tracker);
            detectBrew(player, tracker);
            tracker.snapshot();
        }
    }

    private void detectCraft(ServerPlayer player, Tracker tracker) {
        if (!(tracker.menu instanceof CraftingMenu) && !(tracker.menu instanceof InventoryMenu)) return;
        final ItemStack cur = tracker.menu.getSlot(0).getItem();
        final ItemStack ex = tracker.craftResult;

        if (!cur.isEmpty() && ex.isEmpty()) {
            onCraftItem(player, cur);
        } else if (cur.isEmpty() && !ex.isEmpty()) {
            onCraftItem(player, ex);
        }

        final ItemStack cursor = tracker.menu.getCarried();
        final ItemStack prevCursor = tracker.craftCursor;
        if (!cursor.isEmpty() && prevCursor.isEmpty() && !cur.isEmpty()
                && cursor.getItem() == cur.getItem()) {
            onCraftItem(player, cursor);
        }
    }

    private void detectSmelt(ServerPlayer player, Tracker tracker) {
        if (!(tracker.menu instanceof FurnaceMenu)) return;
        final ItemStack cur = tracker.menu.getSlot(2).getItem();
        final ItemStack ex = tracker.smeltResult;
        if (cur.isEmpty() || cur.getCount() < ex.getCount()) {
            for (int i = 0; i < ex.getCount() - (cur.isEmpty() ? 0 : cur.getCount()); i++) {
                onSmeltItem(player, ex);
            }
        }
    }

    private void detectEnchant(ServerPlayer player, Tracker tracker) {
        if (!(tracker.menu instanceof EnchantmentMenu)) return;
        final ItemStack cur = tracker.menu.getSlot(0).getItem();
        final ItemStack ex = tracker.enchantItem;
        if (!cur.isEmpty() && !ex.isEmpty() && !hasEnchantments(ex) && hasEnchantments(cur)) {
            onEnchantItem(player, cur);
        }
    }

    private void detectBrew(ServerPlayer player, Tracker tracker) {
        if (!(tracker.menu instanceof BrewingStandMenu)) return;
        for (int i = 0; i < 3; i++) {
            final ItemStack cur = tracker.menu.getSlot(i).getItem();
            final ItemStack ex = tracker.bottles[i];
            if (cur.isEmpty() && !ex.isEmpty()) {
                onBrewItem(player, ex);
            }
        }
    }

    private boolean hasEnchantments(ItemStack item) {
        return EnchantmentHelper.hasAnyEnchantments(item);
    }

    private void onCraftItem(ServerPlayer player, ItemStack crafted) {
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        for (final Quest quest : plugin.getLoadedQuests()) {
            if (!quester.getCurrentQuests().containsKey(quest)) continue;
            final Stage stage = quester.getCurrentStage(quest);
            if (stage == null) continue;
            if (!stage.getItemsToCraft().isEmpty()) {
                for (int i = 0; i < stage.getItemsToCraft().size(); i++) {
                    final Object goal = stage.getItemsToCraft().get(i);
                    if (goal != null && crafted.getItem().toString().equalsIgnoreCase(goal.toString())) {
                        quester.getQuestProgressOrDefault(quest).getItemsCrafted().set(i,
                                quester.getQuestProgressOrDefault(quest).getItemsCrafted().get(i) + 1);
                        quester.checkQuest(quest);
                    }
                }
            }
        }
    }

    private void onSmeltItem(ServerPlayer player, ItemStack smelted) {
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        for (final Quest quest : plugin.getLoadedQuests()) {
            if (!quester.getCurrentQuests().containsKey(quest)) continue;
            final Stage stage = quester.getCurrentStage(quest);
            if (stage == null) continue;
            if (!stage.getItemsToSmelt().isEmpty()) {
                for (int i = 0; i < stage.getItemsToSmelt().size(); i++) {
                    final Object goal = stage.getItemsToSmelt().get(i);
                    if (goal != null && smelted.getItem().toString().equalsIgnoreCase(goal.toString())) {
                        quester.getQuestProgressOrDefault(quest).getItemsSmelted().set(i,
                                quester.getQuestProgressOrDefault(quest).getItemsSmelted().get(i) + 1);
                        quester.checkQuest(quest);
                    }
                }
            }
        }
    }

    private void onEnchantItem(ServerPlayer player, ItemStack enchanted) {
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        for (final Quest quest : plugin.getLoadedQuests()) {
            if (!quester.getCurrentQuests().containsKey(quest)) continue;
            final Stage stage = quester.getCurrentStage(quest);
            if (stage == null) continue;
            if (!stage.getItemsToEnchant().isEmpty()) {
                for (int i = 0; i < stage.getItemsToEnchant().size(); i++) {
                    final Object goal = stage.getItemsToEnchant().get(i);
                    if (goal != null && enchanted.getItem().toString().equalsIgnoreCase(goal.toString())) {
                        quester.getQuestProgressOrDefault(quest).getItemsEnchanted().set(i,
                                quester.getQuestProgressOrDefault(quest).getItemsEnchanted().get(i) + 1);
                        quester.checkQuest(quest);
                    }
                }
            }
        }
    }

    private void onBrewItem(ServerPlayer player, ItemStack brewed) {
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        for (final Quest quest : plugin.getLoadedQuests()) {
            if (!quester.getCurrentQuests().containsKey(quest)) continue;
            final Stage stage = quester.getCurrentStage(quest);
            if (stage == null) continue;
            if (!stage.getItemsToBrew().isEmpty()) {
                for (int i = 0; i < stage.getItemsToBrew().size(); i++) {
                    final Object goal = stage.getItemsToBrew().get(i);
                    if (goal != null && brewed.getItem().toString().equalsIgnoreCase(goal.toString())) {
                        quester.getQuestProgressOrDefault(quest).getItemsBrewed().set(i,
                                quester.getQuestProgressOrDefault(quest).getItemsBrewed().get(i) + 1);
                        quester.checkQuest(quest);
                    }
                }
            }
        }
    }

    private static class Tracker {
        final AbstractContainerMenu menu;
        ItemStack craftResult;
        ItemStack craftCursor;
        ItemStack smeltResult;
        ItemStack enchantItem;
        final ItemStack[] bottles = new ItemStack[3];

        Tracker(AbstractContainerMenu menu) {
            this.menu = menu;
        }

        void snapshot() {
            craftResult = menu.getSlot(0).getItem().copy();
            craftCursor = menu.getCarried().copy();
            if (menu instanceof FurnaceMenu) {
                smeltResult = menu.getSlot(2).getItem().copy();
            }
            if (menu instanceof EnchantmentMenu) {
                enchantItem = menu.getSlot(0).getItem().copy();
            }
            if (menu instanceof BrewingStandMenu) {
                for (int i = 0; i < 3; i++) {
                    bottles[i] = menu.getSlot(i).getItem().copy();
                }
            }
        }
    }
}