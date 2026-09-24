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

import me.pikamug.quests.gui.FabricQuestMenu;
import me.pikamug.quests.util.FabricItemUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Routes clicks in the quest-selection chest menu to the quest offer logic.
 * The menu's own container is a {@link FabricQuestMenu.QuestMenuContainer},
 * so regular slots never transfer items. Also enforces the quest journal
 * protections so the journal cannot leave the player's own inventory.
 */
@Mixin(AbstractContainerMenu.class)
public abstract class MixinAbstractContainerMenu {

    @Inject(method = "clicked", at = @At("HEAD"), cancellable = true)
    private void quests$onMenuClicked(int slot, int button, ContainerInput input, Player player,
            CallbackInfo ci) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        final Object self = this;
        if (!(self instanceof AbstractContainerMenu menu)) return;

        if (menu instanceof ChestMenu chestMenu) {
            final Container container = chestMenu.getContainer();
            if (container instanceof FabricQuestMenu.QuestMenuContainer questMenu) {
                questMenu.select(serverPlayer, slot);
                serverPlayer.closeContainer();
                ci.cancel();
                return;
            }
        }

        final ItemStack carried = menu.getCarried();
        final ItemStack clickedItem = slot >= 0 && slot < menu.slots.size()
                ? menu.getSlot(slot).getItem() : ItemStack.EMPTY;
        final boolean journalInvolved = FabricItemUtil.isJournal(carried)
                || FabricItemUtil.isJournal(clickedItem);

        if (input == ContainerInput.PICKUP || input == ContainerInput.PICKUP_ALL
                || input == ContainerInput.QUICK_CRAFT || input == ContainerInput.SWAP) {
            if (input == ContainerInput.SWAP && button >= 0 && button < 9
                    && FabricItemUtil.isJournal(serverPlayer.getInventory().getItem(button))) {
                ci.cancel();
                return;
            }
            if (journalInvolved && slot >= 0 && slot < menu.slots.size()
                    && menu.getSlot(slot).container != serverPlayer.getInventory()) {
                ci.cancel();
                return;
            }
        } else if (input == ContainerInput.QUICK_MOVE || input == ContainerInput.THROW) {
            if (journalInvolved) {
                ci.cancel();
            }
        }
    }
}