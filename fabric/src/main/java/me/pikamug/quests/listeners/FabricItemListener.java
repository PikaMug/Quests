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
import me.pikamug.quests.enums.ObjectiveType;
import me.pikamug.quests.player.FabricQuester;
import me.pikamug.quests.player.QuestProgress;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.Stage;
import me.pikamug.quests.util.FabricItemUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

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
        FabricMixinEvents.registerBrewingAction(this::isAllowedBrewingAction);
    }

    private void onCraftItem(ServerPlayer player, ItemStack crafted) {
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        final ObjectiveType type = ObjectiveType.CRAFT_ITEM;
        final Set<String> dispatchedQuestIDs = new HashSet<>();
        for (final Quest quest : plugin.getLoadedQuests()) {
            if (!quester.meetsCondition(quest, true)) continue;
            if (quester.getCurrentQuests().containsKey(quest)
                    && quester.getCurrentStage(quest).containsObjective(type)) {
                applyCraftItem(quester, quest, crafted);
            }

            dispatchedQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, type,
                    (q, cq) -> {
                if (!dispatchedQuestIDs.contains(cq.getId())) {
                    applyCraftItem((FabricQuester) q, cq, crafted);
                }
                return null;
            }));
        }
    }

    private void applyCraftItem(FabricQuester quester, Quest quest, ItemStack crafted) {
        final Stage stage = quester.getCurrentStage(quest);
        if (stage == null || !stage.containsObjective(ObjectiveType.CRAFT_ITEM)) return;
        applyItemProgress(quester, quest, crafted, ObjectiveType.CRAFT_ITEM, stage.getItemsToCraft(),
                quester.getQuestProgressOrDefault(quest).getItemsCrafted());
    }

    private void onSmeltItem(ServerPlayer player, ItemStack smelted) {
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        final ObjectiveType type = ObjectiveType.SMELT_ITEM;
        final Set<String> dispatchedQuestIDs = new HashSet<>();
        for (final Quest quest : plugin.getLoadedQuests()) {
            if (!quester.meetsCondition(quest, true)) continue;
            if (quester.getCurrentQuests().containsKey(quest)
                    && quester.getCurrentStage(quest).containsObjective(type)) {
                applySmeltItem(quester, quest, smelted);
            }

            dispatchedQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, type,
                    (q, cq) -> {
                if (!dispatchedQuestIDs.contains(cq.getId())) {
                    applySmeltItem((FabricQuester) q, cq, smelted);
                }
                return null;
            }));
        }
    }

    private void applySmeltItem(FabricQuester quester, Quest quest, ItemStack smelted) {
        final Stage stage = quester.getCurrentStage(quest);
        if (stage == null || !stage.containsObjective(ObjectiveType.SMELT_ITEM)) return;
        applyItemProgress(quester, quest, smelted, ObjectiveType.SMELT_ITEM, stage.getItemsToSmelt(),
                quester.getQuestProgressOrDefault(quest).getItemsSmelted());
    }

    private void onEnchantItem(ServerPlayer player, ItemStack enchanted) {
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        final ObjectiveType type = ObjectiveType.ENCHANT_ITEM;
        final Set<String> dispatchedQuestIDs = new HashSet<>();
        for (final Quest quest : plugin.getLoadedQuests()) {
            if (!quester.meetsCondition(quest, true)) continue;
            if (quester.getCurrentQuests().containsKey(quest)
                    && quester.getCurrentStage(quest).containsObjective(type)) {
                applyEnchantItem(quester, quest, enchanted);
            }

            dispatchedQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, type,
                    (q, cq) -> {
                if (!dispatchedQuestIDs.contains(cq.getId())) {
                    applyEnchantItem((FabricQuester) q, cq, enchanted);
                }
                return null;
            }));
        }
    }

    private void applyEnchantItem(FabricQuester quester, Quest quest, ItemStack enchanted) {
        final Stage stage = quester.getCurrentStage(quest);
        if (stage == null || !stage.containsObjective(ObjectiveType.ENCHANT_ITEM)) return;
        applyItemProgress(quester, quest, enchanted, ObjectiveType.ENCHANT_ITEM, stage.getItemsToEnchant(),
                quester.getQuestProgressOrDefault(quest).getItemsEnchanted());
    }

    private void onBrewItem(ServerPlayer player, ItemStack brewed) {
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        final ObjectiveType type = ObjectiveType.BREW_ITEM;
        final Set<String> dispatchedQuestIDs = new HashSet<>();
        for (final Quest quest : plugin.getLoadedQuests()) {
            if (!quester.meetsCondition(quest, true)) continue;
            if (quester.getCurrentQuests().containsKey(quest)
                    && quester.getCurrentStage(quest).containsObjective(type)) {
                applyBrewItem(quester, quest, brewed);
            }

            dispatchedQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, type,
                    (q, cq) -> {
                if (!dispatchedQuestIDs.contains(cq.getId())) {
                    applyBrewItem((FabricQuester) q, cq, brewed);
                }
                return null;
            }));
        }
    }

    private void applyBrewItem(FabricQuester quester, Quest quest, ItemStack brewed) {
        final Stage stage = quester.getCurrentStage(quest);
        if (stage == null || !stage.containsObjective(ObjectiveType.BREW_ITEM)) return;
        applyItemProgress(quester, quest, brewed, ObjectiveType.BREW_ITEM, stage.getItemsToBrew(),
                quester.getQuestProgressOrDefault(quest).getItemsBrewed());
    }

    private boolean isAllowedBrewingAction(ServerPlayer player, BrewingStandMenu menu, int slot, int button,
            ContainerInput input) {
        if (!plugin.getConfigSettings().canPreventExploit()) return true;
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        if (!hasBrewObjective(quester)) return true;

        if (input == ContainerInput.QUICK_CRAFT) return false;
        if (input == ContainerInput.QUICK_MOVE) return slot < 5;
        if (slot < 0 || slot > 2) return true;

        if (input == ContainerInput.SWAP) {
            if (button < 0 || button > 8) return true;
            final ItemStack hotbar = player.getInventory().getItem(button);
            return hotbar.isEmpty() || isWaterBottle(hotbar);
        }
        if (input == ContainerInput.PICKUP) {
            final ItemStack carried = menu.getCarried();
            return carried.isEmpty() || isWaterBottle(carried);
        }
        return true;
    }

    private boolean hasBrewObjective(FabricQuester quester) {
        for (final Quest quest : quester.getCurrentQuests().keySet()) {
            final Stage stage = quester.getCurrentStage(quest);
            if (stage != null && stage.containsObjective(ObjectiveType.BREW_ITEM)) return true;
        }
        return false;
    }

    private boolean isWaterBottle(ItemStack item) {
        if (item == null || item.isEmpty() || !item.is(Items.POTION)) return false;
        return item.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.WATER);
    }

    private void onConsumeItem(ServerPlayer player, ItemStack consumed) {
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        final ObjectiveType type = ObjectiveType.CONSUME_ITEM;
        final Set<String> dispatchedQuestIDs = new HashSet<>();
        for (final Quest quest : plugin.getLoadedQuests()) {
            if (!quester.meetsCondition(quest, true)) continue;
            if (quester.getCurrentQuests().containsKey(quest)
                    && quester.getCurrentStage(quest).containsObjective(type)) {
                applyConsumeItem(quester, quest, consumed);
            }

            dispatchedQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, type,
                    (q, cq) -> {
                if (!dispatchedQuestIDs.contains(cq.getId())) {
                    applyConsumeItem((FabricQuester) q, cq, consumed);
                }
                return null;
            }));
        }
    }

    private void applyConsumeItem(FabricQuester quester, Quest quest, ItemStack consumed) {
        final Stage stage = quester.getCurrentStage(quest);
        if (stage == null || !stage.containsObjective(ObjectiveType.CONSUME_ITEM)) return;
        applyItemProgress(quester, quest, consumed, ObjectiveType.CONSUME_ITEM, stage.getItemsToConsume(),
                quester.getQuestProgressOrDefault(quest).getItemsConsumed());
    }

    private void applyItemProgress(FabricQuester quester, Quest quest, ItemStack item, ObjectiveType type,
            LinkedList<?> goals, LinkedList<Integer> progress) {
        if (goals == null || progress == null) return;
        for (int i = 0; i < goals.size(); i++) {
            final Object goal = goals.get(i);
            if (!(goal instanceof ItemStack) || !FabricItemUtil.matches(item, (ItemStack) goal)) continue;
            final int goalAmount = Math.max(1, ((ItemStack) goal).getCount());
            final int before = progress.get(i);
            final int after = Math.min(item.getCount() + before, goalAmount);
            progress.set(i, after);
            if (before < goalAmount && after >= goalAmount) {
                quester.finishObjective(quest, type, i);
            }
            quester.checkQuest(quest);
        }
    }
}
