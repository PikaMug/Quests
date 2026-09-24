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

import me.pikamug.quests.FabricMixinEvents;
import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.player.FabricQuester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.Stage;
import me.pikamug.quests.util.FabricItemUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class FabricItemListener {

    private final FabricQuestsPlugin plugin;

    public FabricItemListener(FabricQuestsPlugin plugin) {
        this.plugin = plugin;
        register();
    }

    private void register() {
        FabricMixinEvents.registerItemCrafted(this::onCraftItem);
        FabricMixinEvents.registerItemSmelted(this::onSmeltItem);
        FabricMixinEvents.registerItemEnchanted(this::onEnchantItem);
        FabricMixinEvents.registerItemBrewed(this::onBrewItem);
        FabricMixinEvents.registerItemConsumed(this::onConsumeItem);
    }

    private void onCraftItem(ServerPlayer player, ItemStack crafted) {
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        for (final Quest quest : plugin.getLoadedQuests()) {
            if (!quester.getCurrentQuests().containsKey(quest)) continue;
            final Stage stage = quester.getCurrentStage(quest);
            if (stage == null) continue;
            if (!stage.getItemsToCraft().isEmpty()) {
                final var progress = quester.getQuestProgressOrDefault(quest);
                for (int i = 0; i < stage.getItemsToCraft().size(); i++) {
                    final Object goal = stage.getItemsToCraft().get(i);
                    if (goal != null && FabricItemUtil.matches(crafted, (ItemStack) goal)) {
                        final int goalAmount = Math.max(1, ((ItemStack) goal).getCount());
                        progress.getItemsCrafted().set(i,
                                Math.min(crafted.getCount() + progress.getItemsCrafted().get(i), goalAmount));
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
                final var progress = quester.getQuestProgressOrDefault(quest);
                for (int i = 0; i < stage.getItemsToSmelt().size(); i++) {
                    final Object goal = stage.getItemsToSmelt().get(i);
                    if (goal != null && FabricItemUtil.matches(smelted, (ItemStack) goal)) {
                        final int goalAmount = Math.max(1, ((ItemStack) goal).getCount());
                        progress.getItemsSmelted().set(i,
                                Math.min(smelted.getCount() + progress.getItemsSmelted().get(i), goalAmount));
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
                final var progress = quester.getQuestProgressOrDefault(quest);
                for (int i = 0; i < stage.getItemsToEnchant().size(); i++) {
                    final Object goal = stage.getItemsToEnchant().get(i);
                    if (goal != null && FabricItemUtil.matches(enchanted, (ItemStack) goal)) {
                        final int goalAmount = Math.max(1, ((ItemStack) goal).getCount());
                        progress.getItemsEnchanted().set(i,
                                Math.min(enchanted.getCount() + progress.getItemsEnchanted().get(i), goalAmount));
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
                final var progress = quester.getQuestProgressOrDefault(quest);
                for (int i = 0; i < stage.getItemsToBrew().size(); i++) {
                    final Object goal = stage.getItemsToBrew().get(i);
                    if (goal != null && FabricItemUtil.matches(brewed, (ItemStack) goal)) {
                        final int goalAmount = Math.max(1, ((ItemStack) goal).getCount());
                        progress.getItemsBrewed().set(i,
                                Math.min(brewed.getCount() + progress.getItemsBrewed().get(i), goalAmount));
                        quester.checkQuest(quest);
                    }
                }
            }
        }
    }

    private void onConsumeItem(ServerPlayer player, ItemStack consumed) {
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        for (final Quest quest : plugin.getLoadedQuests()) {
            if (!quester.getCurrentQuests().containsKey(quest)) continue;
            final Stage stage = quester.getCurrentStage(quest);
            if (stage == null) continue;
            if (!stage.getItemsToConsume().isEmpty()) {
                final var progress = quester.getQuestProgressOrDefault(quest);
                for (int i = 0; i < stage.getItemsToConsume().size(); i++) {
                    final Object goal = stage.getItemsToConsume().get(i);
                    if (goal != null && FabricItemUtil.matches(consumed, (ItemStack) goal)) {
                        progress.getItemsConsumed().set(i,
                                Math.min(consumed.getCount() + progress.getItemsConsumed().get(i), 64));
                        quester.checkQuest(quest);
                    }
                }
            }
        }
    }
}