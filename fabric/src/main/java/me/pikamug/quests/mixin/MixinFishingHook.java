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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hooks a genuine fish catch. {@link FishingHook#retrieve(ItemStack)} is the
 * reel-in method; the loot-table roll that produces the catch is unique to the
 * fish-catch path (a bare reel with nothing hooked takes an earlier return).
 * Injecting at that call counts only actual catches, not empty reels.
 */
@Mixin(FishingHook.class)
public abstract class MixinFishingHook {

    @Inject(method = "retrieve",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItems"
                            + "(Lnet/minecraft/world/level/storage/loot/LootParams;)"
                            + "Lit/unimi/dsi/fastutil/objects/ObjectArrayList;"))
    private void quests$onFishCaught(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        final FishingHook bobber = (FishingHook) (Object) this;
        if (bobber.getPlayerOwner() instanceof ServerPlayer player) {
            FabricMixinEvents.invokeFishingCatch(player);
        }
    }
}