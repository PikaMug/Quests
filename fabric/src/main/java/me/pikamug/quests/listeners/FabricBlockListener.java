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

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import me.pikamug.quests.FabricMixinEvents;
import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.convo.misc.FabricQuestAcceptPrompt;
import me.pikamug.quests.enums.ObjectiveType;
import me.pikamug.quests.player.FabricQuester;
import me.pikamug.quests.quests.FabricQuest;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.Stage;
import me.pikamug.quests.util.FabricItemUtil;
import me.pikamug.quests.util.FabricLang;
import me.pikamug.quests.util.FabricMiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockState;

public class FabricBlockListener {

    private final FabricQuestsPlugin plugin;

    public FabricBlockListener(FabricQuestsPlugin plugin) {
        this.plugin = plugin;
        register();
    }

    private void register() {
        // Block damaged (START_DESTROY_BLOCK). The mixin only fires for ServerPlayer.
        FabricMixinEvents.registerAttackBlock(this::onBlockDamage);

        // Block used (right-click). The mixin only fires for ServerPlayer.
        FabricMixinEvents.registerUseBlock(this::onBlockUse);

        // Blocks actually broken / placed. Both mixins only fire for ServerPlayer.
        FabricMixinEvents.registerBlockBroken(this::onBlockBroken);
        FabricMixinEvents.registerBlockPlaced(this::onBlockPlaced);
    }

    private void onBlockDamage(ServerPlayer player, BlockPos pos) {
        if (plugin.isLoading()) return;
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        final BlockState state = player.level().getBlockState(pos);

        for (final Quest quest : plugin.getLoadedQuests()) {
            if (!quester.getCurrentQuests().containsKey(quest)) continue;
            final Stage stage = quester.getCurrentStage(quest);
            if (stage == null) continue;

            // DAMAGE_BLOCK
            if (!stage.getBlocksToDamage().isEmpty()) {
                for (int i = 0; i < stage.getBlocksToDamage().size(); i++) {
                    if (matchesBlock(state, stage.getBlocksToDamage().get(i))) {
                        quester.getQuestProgressOrDefault(quest).getBlocksDamaged().set(i,
                                quester.getQuestProgressOrDefault(quest).getBlocksDamaged().get(i) + 1);
                        quester.checkQuest(quest);
                    }
                }
            }
        }
    }

    private void onBlockBroken(ServerPlayer player, BlockPos pos, BlockState state) {
        if (plugin.isLoading()) return;
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        final ItemStack tool = player.getMainHandItem();

        for (final Quest quest : plugin.getLoadedQuests()) {
            if (!quester.getCurrentQuests().containsKey(quest)) continue;
            final Stage stage = quester.getCurrentStage(quest);
            if (stage == null) continue;
            final var progress = quester.getQuestProgressOrDefault(quest);

            // BREAK_BLOCK
            if (!stage.getBlocksToBreak().isEmpty()) {
                if (quest.getOptions().canIgnoreSilkTouch() && hasSilkTouch(tool)) {
                    quester.sendMessage(FabricLang.get(quester.getServerPlayer(), "optionSilkTouchFail")
                            .replace("<quest>", quest.getName()));
                } else {
                    for (int i = 0; i < stage.getBlocksToBreak().size(); i++) {
                        if (matchesBlock(state, stage.getBlocksToBreak().get(i))) {
                            progress.getBlocksBroken().set(i, progress.getBlocksBroken().get(i) + 1);
                            quester.checkQuest(quest);
                        }
                    }
                }
            }

            // Replacing a block that was a placement goal counts against it
            if (quest.getOptions().canIgnoreBlockReplace() && !stage.getBlocksToPlace().isEmpty()) {
                for (int i = 0; i < stage.getBlocksToPlace().size(); i++) {
                    final int placed = progress.getBlocksPlaced().get(i);
                    if (matchesBlock(state, stage.getBlocksToPlace().get(i)) && placed > 0) {
                        progress.getBlocksPlaced().set(i, placed - 1);
                    }
                }
            }
        }
    }

    private void onBlockPlaced(ServerPlayer player, BlockPos pos, BlockState state) {
        if (plugin.isLoading()) return;
        final FabricQuester quester = plugin.getQuester(player.getUUID());

        for (final Quest quest : plugin.getLoadedQuests()) {
            if (!quester.getCurrentQuests().containsKey(quest)) continue;
            final Stage stage = quester.getCurrentStage(quest);
            if (stage == null) continue;
            final var progress = quester.getQuestProgressOrDefault(quest);

            // PLACE_BLOCK
            if (!stage.getBlocksToPlace().isEmpty()) {
                for (int i = 0; i < stage.getBlocksToPlace().size(); i++) {
                    if (matchesBlock(state, stage.getBlocksToPlace().get(i))) {
                        progress.getBlocksPlaced().set(i, progress.getBlocksPlaced().get(i) + 1);
                        quester.checkQuest(quest);
                    }
                }
            }

            // Replacing a block that counted toward a break goal rolls it back
            if (quest.getOptions().canIgnoreBlockReplace() && !stage.getBlocksToBreak().isEmpty()) {
                for (int i = 0; i < stage.getBlocksToBreak().size(); i++) {
                    final int broken = progress.getBlocksBroken().get(i);
                    if (matchesBlock(state, stage.getBlocksToBreak().get(i)) && broken > 0) {
                        progress.getBlocksBroken().set(i, broken - 1);
                    }
                }
            }
        }
    }

    private void onBlockUse(ServerPlayer player, BlockPos pos, InteractionHand hand) {
        if (plugin.isLoading() || hand != InteractionHand.MAIN_HAND) return;
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        final BlockState state = player.level().getBlockState(pos);

        boolean hasObjective = false;
        for (final Quest quest : plugin.getLoadedQuests()) {
            if (quester.getCurrentQuests().containsKey(quest)
                    && quester.getCurrentStage(quest).containsObjective(ObjectiveType.USE_BLOCK)) {
                hasObjective = true;
                break;
            }
        }

        if (!hasObjective) {
            // Block-start selection: right-click a block while editing the "start block" option
            if (plugin.isSelectingBlockStart(player.getUUID())) {
                plugin.getSelectedBlockStarts().put(player.getUUID(), pos);
                quester.sendMessage("§6" + FabricLang.get(quester.getServerPlayer(), "questSelectedLocation") + " §b"
                        + player.level().dimension().identifier() + ": " + pos.getX() + ", "
                        + pos.getY() + ", " + pos.getZ());
                return;
            }

            // Right-clicking a quest's start block offers the quest
            for (final Quest quest : plugin.getLoadedQuests()) {
                final FabricQuest fabricQuest = (FabricQuest) quest;
                if (fabricQuest.getBlockStart() == null || !fabricQuest.getBlockStart().equals(pos)) continue;
                if (plugin.getConfigSettings().getMaxQuests() > 0
                        && quester.getCurrentQuests().size() >= plugin.getConfigSettings().getMaxQuests()) {
                    quester.sendMessage("§e" + FabricLang.get(quester.getServerPlayer(), "questMaxAllowed").replace("<number>",
                            String.valueOf(plugin.getConfigSettings().getMaxQuests())));
                } else {
                    if (quester.getCompletedQuests().contains(fabricQuest)) {
                        if (fabricQuest.getPlanner().getCooldown() > -1
                                && quester.getRemainingCooldown(fabricQuest) > 0) {
                            quester.sendMessage("§e" + FabricLang.get(quester.getServerPlayer(), "questTooEarly")
                                    .replace("<quest>", fabricQuest.getName())
                                    .replace("<time>", FabricMiscUtil.getTime(
                                            quester.getRemainingCooldown(fabricQuest))));
                            continue;
                        } else if (fabricQuest.getPlanner().getCooldown() < 0) {
                            quester.sendMessage("§e" + FabricLang.get(quester.getServerPlayer(), "questAlreadyCompleted")
                                    .replace("<quest>", fabricQuest.getName()));
                            continue;
                        }
                    }
                    for (final Quest currentQuest : quester.getCurrentQuests().keySet()) {
                        if (currentQuest.getId().equals(fabricQuest.getId())) {
                            quester.sendMessage("§c" + FabricLang.get(quester.getServerPlayer(), "questAlreadyOn"));
                            return;
                        }
                    }
                    quester.setQuestIdToTake(fabricQuest.getId());
                    if (!plugin.getConfigSettings().canConfirmAccept()) {
                        quester.takeQuest(fabricQuest, false);
                    } else {
                        final Quest toTake = plugin.getQuestById(quester.getQuestIdToTake());
                        final String content = "§6- §5" + (toTake != null ? toTake.getName() : "?")
                                + "§6 -\n\n§r" + (toTake != null ? toTake.getDescription() : "");
                        for (final String msg : content.split("<br>")) {
                            quester.sendMessage(msg);
                        }
                        new FabricQuestAcceptPrompt(player.getUUID(), plugin).start();
                    }
                }
                break;
            }
        }

        for (final Quest quest : plugin.getLoadedQuests()) {
            if (!quester.getCurrentQuests().containsKey(quest)) continue;
            final Stage stage = quester.getCurrentStage(quest);
            if (stage == null) continue;

            // USE_BLOCK
            if (!stage.getBlocksToUse().isEmpty()) {
                for (int i = 0; i < stage.getBlocksToUse().size(); i++) {
                    if (matchesBlock(state, stage.getBlocksToUse().get(i))) {
                        quester.getQuestProgressOrDefault(quest).getBlocksUsed().set(i,
                                quester.getQuestProgressOrDefault(quest).getBlocksUsed().get(i) + 1);
                        quester.checkQuest(quest);
                    }
                }
            }
        }
    }

    private boolean matchesBlock(BlockState state, Object goal) {
        if (state == null || goal == null) return false;
        final ItemStack goalStack = FabricItemUtil.deserialize(goal.toString());
        return !goalStack.isEmpty() && state.getBlock().asItem() == goalStack.getItem();
    }

    private boolean hasSilkTouch(ItemStack tool) {
        if (tool == null || tool.isEmpty()) return false;
        for (final Object2IntMap.Entry<Holder<Enchantment>> entry : tool.getEnchantments().entrySet()) {
            if (entry.getKey().is(Enchantments.SILK_TOUCH)) return true;
        }
        return false;
    }
}
