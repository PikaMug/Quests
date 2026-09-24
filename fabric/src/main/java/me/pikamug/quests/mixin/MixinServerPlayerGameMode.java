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

import me.pikamug.quests.FabricMixinEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fires when a block is actually broken. {@link ServerPlayerGameMode#destroyBlock(BlockPos)}
 * is the commit point that removes the block and returns {@code true} on success, so the
 * block-in-progress state is captured at HEAD and reported when the destroy returns true.
 */
@Mixin(ServerPlayerGameMode.class)
public abstract class MixinServerPlayerGameMode {

    @Shadow
    public ServerPlayer player;

    @Unique
    private BlockState quests$stateBeingBroken;

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void quests$captureStateBeforeBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        quests$stateBeingBroken = player == null ? null : player.level().getBlockState(pos);
    }

    @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void quests$onBlockDestroyed(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        final BlockState state = quests$stateBeingBroken;
        quests$stateBeingBroken = null;
        if (Boolean.TRUE.equals(cir.getReturnValue()) && state != null && player != null) {
            FabricMixinEvents.invokeBlockBroken(player, pos, state);
        }
    }
}