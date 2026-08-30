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
import me.pikamug.quests.util.FabricItemUtil;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

public class FabricItemListener {

    private final FabricQuestsPlugin plugin;

    public FabricItemListener(FabricQuestsPlugin plugin) {
        this.plugin = plugin;
        register();
    }

    private void register() {
        // Consume item (right-click food)
        UseItemCallback.EVENT.register((player, level, hand) -> {
            if (player instanceof ServerPlayer serverPlayer && hand == net.minecraft.world.InteractionHand.MAIN_HAND) {
                final ItemStack item = player.getItemInHand(hand);
                if (item.isEdible()) {
                    onConsumeItem(serverPlayer, item);
                }
            }
            return InteractionResult.PASS;
        });

        // TODO: Register crafting, smelting, enchanting events via Fabric API callbacks
        // Fabric does not have direct CraftItemEvent/EnchantItemEvent equivalents;
        // these would need to be handled via mixin or InventoryListener pattern
    }

    private void onConsumeItem(ServerPlayer player, ItemStack consumed) {
        if (plugin.isLoading()) return;
        final FabricQuester quester = plugin.getQuester(player.getUUID());

        for (final Quest quest : plugin.getLoadedQuests()) {
            if (!quester.getCurrentQuests().containsKey(quest)) continue;
            final Stage stage = quester.getCurrentStage(quest);
            if (stage == null) continue;

            // CONSUME_ITEM
            if (!stage.getItemsToConsume().isEmpty()) {
                for (int i = 0; i < stage.getItemsToConsume().size(); i++) {
                    final Object goal = stage.getItemsToConsume().get(i);
                    if (goal != null && consumed.getItem().toString().equalsIgnoreCase(goal.toString())) {
                        quester.getQuestProgressOrDefault(quest).getItemsConsumed().set(i,
                                quester.getQuestProgressOrDefault(quest).getItemsConsumed().get(i) + 1);
                        quester.checkQuest(quest);
                    }
                }
            }
        }
    }
}
