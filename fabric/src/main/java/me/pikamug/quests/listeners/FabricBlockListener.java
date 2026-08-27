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
import me.pikamug.quests.enums.ObjectiveType;
import me.pikamug.quests.player.FabricQuester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.Stage;
import me.pikamug.quests.util.FabricItemUtil;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class FabricBlockListener {

    private final FabricQuestsPlugin plugin;

    public FabricBlockListener(FabricQuestsPlugin plugin) {
        this.plugin = plugin;
        register();
    }

    private void register() {
        // Block break
        AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) -> {
            if (player instanceof ServerPlayer serverPlayer) {
                onBlockBreak(serverPlayer, pos);
            }
            return InteractionResult.PASS;
        });

        // Block use (right-click)
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (player instanceof ServerPlayer serverPlayer) {
                onBlockUse(serverPlayer, hitResult.getBlockPos(), hand);
            }
            return InteractionResult.PASS;
        });
    }

    private void onBlockBreak(ServerPlayer player, BlockPos pos) {
        if (plugin.isLoading()) return;
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        final ItemStack tool = player.getMainHandItem();
        final BlockState state = player.level().getBlockState(pos);

        for (final Quest quest : plugin.getLoadedQuests()) {
            if (!quester.getCurrentQuests().containsKey(quest)) continue;
            final Stage stage = quester.getCurrentStage(quest);
            if (stage == null) continue;

            // BREAK_BLOCK
            if (!stage.getBlocksToBreak().isEmpty()) {
                for (int i = 0; i < stage.getBlocksToBreak().size(); i++) {
                    final Object goal = stage.getBlocksToBreak().get(i);
                    if (goal != null && FabricItemUtil.isSimilar(state.getBlock().asItem().getDefaultInstance(),
                            FabricItemUtil.deserialize(goal.toString()))) {
                        quester.getQuestProgressOrDefault(quest).getBlocksBroken().set(i,
                                quester.getQuestProgressOrDefault(quest).getBlocksBroken().get(i) + 1);
                    }
                }
            }

            // CUT_BLOCK
            if (!stage.getBlocksToCut().isEmpty()) {
                for (int i = 0; i < stage.getBlocksToCut().size(); i++) {
                    final Object goal = stage.getBlocksToCut().get(i);
                    if (goal != null && FabricItemUtil.isSimilar(state.getBlock().asItem().getDefaultInstance(),
                            FabricItemUtil.deserialize(goal.toString()))) {
                        quester.getQuestProgressOrDefault(quest).getBlocksCut().set(i,
                                quester.getQuestProgressOrDefault(quest).getBlocksCut().get(i) + 1);
                    }
                }
            }
        }
    }

    private void onBlockUse(ServerPlayer player, BlockPos pos, InteractionHand hand) {
        if (plugin.isLoading() || hand != InteractionHand.MAIN_HAND) return;
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        final ItemStack tool = player.getMainHandItem();
        final BlockState state = player.level().getBlockState(pos);

        for (final Quest quest : plugin.getLoadedQuests()) {
            if (!quester.getCurrentQuests().containsKey(quest)) continue;
            final Stage stage = quester.getCurrentStage(quest);
            if (stage == null) continue;

            // USE_BLOCK
            if (!stage.getBlocksToUse().isEmpty()) {
                for (int i = 0; i < stage.getBlocksToUse().size(); i++) {
                    final Object goal = stage.getBlocksToUse().get(i);
                    if (goal != null && FabricItemUtil.isSimilar(state.getBlock().asItem().getDefaultInstance(),
                            FabricItemUtil.deserialize(goal.toString()))) {
                        quester.getQuestProgressOrDefault(quest).getBlocksUsed().set(i,
                                quester.getQuestProgressOrDefault(quest).getBlocksUsed().get(i) + 1);
                    }
                }
            }
        }
    }
}
