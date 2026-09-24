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
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public abstract class MixinPlayerList {

    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    private void quests$onPlayerJoin(Connection connection, ServerPlayer player,
                                    CommonListenerCookie commonListenerCookie, CallbackInfo ci) {
        FabricMixinEvents.invokePlayerJoin(player);
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void quests$onPlayerQuit(ServerPlayer player, CallbackInfo ci) {
        FabricMixinEvents.invokePlayerDisconnect(player);
    }

    @Inject(method = "respawn", at = @At("TAIL"))
    private void quests$onPlayerRespawn(ServerPlayer oldPlayer, boolean alive,
                                        Entity.RemovalReason removalReason,
                                        CallbackInfoReturnable<ServerPlayer> cir) {
        FabricMixinEvents.invokePlayerRespawn(cir.getReturnValue());
    }
}