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
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {

    @Shadow
    protected ItemStack useItem;

    @Inject(method = "die", at = @At("HEAD"))
    private void quests$onEntityKilled(DamageSource damageSource, CallbackInfo ci) {
        final LivingEntity victim = (LivingEntity) (Object) this;
        final Entity killer = damageSource.getEntity();
        if (killer == null) return;
        if (victim.level() instanceof ServerLevel serverLevel) {
            QuestsEvents.invokeEntityKilled(serverLevel, killer, victim, damageSource);
        }
    }

    @Inject(method = "completeUsingItem", at = @At("HEAD"))
    private void quests$onItemConsumed(CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayer player
                && (useItem.has(DataComponents.FOOD) || useItem.has(DataComponents.POTION_CONTENTS))) {
            QuestsEvents.invokeItemConsumed(player, useItem.copy());
        }
    }
}