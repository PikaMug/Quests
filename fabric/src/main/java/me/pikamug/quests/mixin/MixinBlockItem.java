/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.mixin;

import me.pikamug.quests.QuestsEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fires when a block is actually placed. {@link BlockItem#placeBlock(BlockPlaceContext, BlockState)}
 * is the commit point that sets the block into the world and returns {@code true} on success.
 * {@link BlockItem} subclasses that override {@code placeBlock} (e.g. two-tall plants) bypass this.
 */
@Mixin(BlockItem.class)
public abstract class MixinBlockItem {

    @Inject(method = "placeBlock", at = @At("RETURN"))
    private void quests$onBlockPlaced(BlockPlaceContext context, BlockState state,
            CallbackInfoReturnable<Boolean> cir) {
        if (!Boolean.TRUE.equals(cir.getReturnValue())) return;
        if (context.getPlayer() instanceof ServerPlayer player) {
            final BlockPos pos = context.getClickedPos();
            final BlockState placed = context.getLevel().getBlockState(pos);
            QuestsEvents.invokeBlockPlaced(player, pos, placed);
        }
    }
}