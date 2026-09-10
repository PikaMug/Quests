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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fires when the server applies an enchantment via the enchanting table button.
 * Mirrors Bukkit's EnchantItemEvent for the ENCHANT_ITEM objective.
 */
@Mixin(EnchantmentMenu.class)
public abstract class MixinEnchantmentMenu {

    @Shadow
    @Final
    private Container enchantSlots;

    @Inject(method = "clickMenuButton", at = @At("TAIL"))
    private void quests$onItemEnchanted(Player player, int button, CallbackInfo ci) {
        final ItemStack item = enchantSlots.getItem(0);
        if (!item.isEmpty() && EnchantmentHelper.hasAnyEnchantments(item)
                && player instanceof ServerPlayer serverPlayer) {
            QuestsEvents.invokeItemEnchanted(serverPlayer, item.copy());
        }
    }
}