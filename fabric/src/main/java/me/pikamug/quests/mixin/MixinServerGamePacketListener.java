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
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MixinServerGamePacketListener {

    @Shadow
    public ServerPlayer player;

    @Inject(method = "handleChat", at = @At("HEAD"), cancellable = true)
    private void quests$onChat(ServerboundChatPacket packet, CallbackInfo ci) {
        final String content = packet.message();
        final boolean allowed = QuestsEvents.invokeChatAllow(player, content);
        QuestsEvents.invokeChatMessage(player, content);
        if (!allowed) {
            ci.cancel();
        }
    }

    @Inject(method = "handleChatCommand", at = @At("HEAD"))
    private void quests$onChatCommand(ServerboundChatCommandPacket packet, CallbackInfo ci) {
        QuestsEvents.invokeCommandMessage(player, packet.command());
    }

    @Inject(method = "handlePlayerAction", at = @At("HEAD"))
    private void quests$onPlayerAction(ServerboundPlayerActionPacket packet, CallbackInfo ci) {
        if (packet.getAction() == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
            final BlockPos pos = packet.getPos();
            if (pos != null && player != null) {
                QuestsEvents.invokeAttackBlock(player, pos);
            }
        }
    }

    @Inject(method = "handleUseItem", at = @At("HEAD"))
    private void quests$onUseItem(ServerboundUseItemPacket packet, CallbackInfo ci) {
        if (player != null && packet.getHand() != null) {
            QuestsEvents.invokeUseItem(player, packet.getHand());
        }
    }

    @Inject(method = "handleUseItemOn", at = @At("HEAD"))
    private void quests$onUseBlock(ServerboundUseItemOnPacket packet, CallbackInfo ci) {
        if (player != null && packet.getHand() != null && packet.getHitResult() != null) {
            final BlockPos pos = packet.getHitResult().getBlockPos();
            if (pos != null) {
                QuestsEvents.invokeUseBlock(player, pos, packet.getHand());
            }
        }
    }

    @Inject(method = "handleInteract", at = @At("HEAD"))
    private void quests$onUseEntity(ServerboundInteractPacket packet, CallbackInfo ci) {
        if (player != null && packet.hand() == InteractionHand.MAIN_HAND) {
            final Entity entity = player.level().getEntity(packet.entityId());
            if (entity != null) {
                QuestsEvents.invokeUseEntity(player, entity);
            }
        }
    }
}