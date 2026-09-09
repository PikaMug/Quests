/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Hook registry replacing Fabric API callbacks with direct game hooks.
 *
 * <p>Quests used to depend on <code>net.fabricmc.fabric-api</code> (lifecycle, networking, message,
 * player, command and combat events). The built jar no longer bundles those classes and the Fabric API
 * mod is not installed on the server, so all callbacks are instead emitted from {@code quests.mixins.json}
 * mixins that target the vanilla server classes directly. Mixins fire on the server thread only.</p>
 *
 * <p>Registering a handler is thread-safe; dispatch to <code>CopyOnWriteArrayList</code> backing stores.</p>
 */
public final class QuestsEvents {

    private QuestsEvents() {
    }

    // --- functional interfaces ---------------------------------------------

    public interface ServerStartedHandler {
        void onServerStarted(MinecraftServer server);
    }

    public interface ServerStoppingHandler {
        void onServerStopping(MinecraftServer server);
    }

    public interface ServerTickHandler {
        void onServerTick(MinecraftServer server);
    }

    public interface PlayerJoinHandler {
        void onPlayerJoin(ServerPlayer player);
    }

    public interface PlayerDisconnectHandler {
        void onPlayerDisconnect(ServerPlayer player);
    }

    /**
     * Right-click (interact) an entity. Returning {@code true} consumes the interaction.
     */
    public interface UseEntityHandler {
        boolean onUseEntity(ServerPlayer player, Entity entity);
    }

    public interface UseItemHandler {
        void onUseItem(ServerPlayer player, InteractionHand hand);
    }

    public interface UseBlockHandler {
        void onUseBlock(ServerPlayer player, BlockPos pos, InteractionHand hand);
    }

    public interface AttackBlockHandler {
        void onAttackBlock(ServerPlayer player, BlockPos pos);
    }

    public interface EntityKilledHandler {
        void onEntityKilled(ServerLevel level, Entity killer, LivingEntity victim, DamageSource damageSource);
    }

    /**
     * Return {@code false} to silently consume the message (password phrases).
     */
    public interface ChatAllowHandler {
        boolean allowChat(ServerPlayer player, String content);
    }

    public interface ChatMessageHandler {
        void onChatMessage(ServerPlayer player, String content);
    }

    public interface CommandMessageHandler {
        void onCommandMessage(ServerPlayer player, String content);
    }

    public interface CommandRegisterHandler {
        void onCommandRegister(CommandDispatcher<CommandSourceStack> dispatcher);
    }

    // --- registries --------------------------------------------------------

    private static final List<ServerStartedHandler> SERVER_STARTED = new CopyOnWriteArrayList<>();
    private static final List<ServerStoppingHandler> SERVER_STOPPING = new CopyOnWriteArrayList<>();
    private static final List<ServerTickHandler> SERVER_TICK = new CopyOnWriteArrayList<>();
    private static final List<PlayerJoinHandler> PLAYER_JOIN = new CopyOnWriteArrayList<>();
    private static final List<PlayerDisconnectHandler> PLAYER_DISCONNECT = new CopyOnWriteArrayList<>();
    private static final List<UseEntityHandler> USE_ENTITY = new CopyOnWriteArrayList<>();
    private static final List<UseItemHandler> USE_ITEM = new CopyOnWriteArrayList<>();
    private static final List<UseBlockHandler> USE_BLOCK = new CopyOnWriteArrayList<>();
    private static final List<AttackBlockHandler> ATTACK_BLOCK = new CopyOnWriteArrayList<>();
    private static final List<EntityKilledHandler> ENTITY_KILLED = new CopyOnWriteArrayList<>();
    private static final List<ChatAllowHandler> CHAT_ALLOW = new CopyOnWriteArrayList<>();
    private static final List<ChatMessageHandler> CHAT_MESSAGE = new CopyOnWriteArrayList<>();
    private static final List<CommandMessageHandler> COMMAND_MESSAGE = new CopyOnWriteArrayList<>();
    private static final List<CommandRegisterHandler> COMMAND_REGISTER = new CopyOnWriteArrayList<>();

    // --- registration ------------------------------------------------------

    public static void registerServerStarted(final ServerStartedHandler handler) {
        SERVER_STARTED.add(handler);
    }

    public static void registerServerStopping(final ServerStoppingHandler handler) {
        SERVER_STOPPING.add(handler);
    }

    public static void registerServerTick(final ServerTickHandler handler) {
        SERVER_TICK.add(handler);
    }

    public static void registerPlayerJoin(final PlayerJoinHandler handler) {
        PLAYER_JOIN.add(handler);
    }

    public static void registerPlayerDisconnect(final PlayerDisconnectHandler handler) {
        PLAYER_DISCONNECT.add(handler);
    }

    public static void registerUseEntity(final UseEntityHandler handler) {
        USE_ENTITY.add(handler);
    }

    public static void registerUseItem(final UseItemHandler handler) {
        USE_ITEM.add(handler);
    }

    public static void registerUseBlock(final UseBlockHandler handler) {
        USE_BLOCK.add(handler);
    }

    public static void registerAttackBlock(final AttackBlockHandler handler) {
        ATTACK_BLOCK.add(handler);
    }

    public static void registerEntityKilled(final EntityKilledHandler handler) {
        ENTITY_KILLED.add(handler);
    }

    public static void registerChatAllow(final ChatAllowHandler handler) {
        CHAT_ALLOW.add(handler);
    }

    public static void registerChatMessage(final ChatMessageHandler handler) {
        CHAT_MESSAGE.add(handler);
    }

    public static void registerCommandMessage(final CommandMessageHandler handler) {
        COMMAND_MESSAGE.add(handler);
    }

    public static void registerCommandRegister(final CommandRegisterHandler handler) {
        COMMAND_REGISTER.add(handler);
    }

    // --- dispatch (mixin targets) -----------------------------------------

    public static void invokeServerStarted(final MinecraftServer server) {
        for (final ServerStartedHandler handler : SERVER_STARTED) {
            handler.onServerStarted(server);
        }
    }

    public static void invokeServerStopping(final MinecraftServer server) {
        for (final ServerStoppingHandler handler : SERVER_STOPPING) {
            handler.onServerStopping(server);
        }
    }

    public static void invokeServerTick(final MinecraftServer server) {
        for (final ServerTickHandler handler : SERVER_TICK) {
            handler.onServerTick(server);
        }
    }

    public static void invokePlayerJoin(final ServerPlayer player) {
        if (player == null) return;
        for (final PlayerJoinHandler handler : PLAYER_JOIN) {
            handler.onPlayerJoin(player);
        }
    }

    public static void invokePlayerDisconnect(final ServerPlayer player) {
        if (player == null) return;
        for (final PlayerDisconnectHandler handler : PLAYER_DISCONNECT) {
            handler.onPlayerDisconnect(player);
        }
    }

    public static boolean invokeUseEntity(final ServerPlayer player, final Entity entity) {
        if (player == null || entity == null) return false;
        boolean handled = false;
        for (final UseEntityHandler handler : USE_ENTITY) {
            if (handler.onUseEntity(player, entity)) handled = true;
        }
        return handled;
    }

    public static void invokeUseItem(final ServerPlayer player, final InteractionHand hand) {
        if (player == null || hand == null) return;
        for (final UseItemHandler handler : USE_ITEM) {
            handler.onUseItem(player, hand);
        }
    }

    public static void invokeUseBlock(final ServerPlayer player, final BlockPos pos, final InteractionHand hand) {
        if (player == null || pos == null || hand == null) return;
        for (final UseBlockHandler handler : USE_BLOCK) {
            handler.onUseBlock(player, pos, hand);
        }
    }

    public static void invokeAttackBlock(final ServerPlayer player, final BlockPos pos) {
        if (player == null || pos == null) return;
        for (final AttackBlockHandler handler : ATTACK_BLOCK) {
            handler.onAttackBlock(player, pos);
        }
    }

    public static void invokeEntityKilled(final ServerLevel level, final Entity killer,
                                   final LivingEntity victim, final DamageSource damageSource) {
        if (level == null || killer == null || victim == null) return;
        for (final EntityKilledHandler handler : ENTITY_KILLED) {
            handler.onEntityKilled(level, killer, victim, damageSource);
        }
    }

    /**
     * @return {@code true} if the message may be broadcast
     */
    public static boolean invokeChatAllow(final ServerPlayer player, final String content) {
        if (player == null || content == null) return true;
        for (final ChatAllowHandler handler : CHAT_ALLOW) {
            if (!handler.allowChat(player, content)) return false;
        }
        return true;
    }

    public static void invokeChatMessage(final ServerPlayer player, final String content) {
        if (player == null || content == null) return;
        for (final ChatMessageHandler handler : CHAT_MESSAGE) {
            handler.onChatMessage(player, content);
        }
    }

    public static void invokeCommandMessage(final ServerPlayer player, final String content) {
        if (player == null || content == null) return;
        for (final CommandMessageHandler handler : COMMAND_MESSAGE) {
            handler.onCommandMessage(player, content);
        }
    }

    public static void invokeCommandRegister(final CommandDispatcher<CommandSourceStack> dispatcher) {
        if (dispatcher == null) return;
        for (final CommandRegisterHandler handler : COMMAND_REGISTER) {
            handler.onCommandRegister(dispatcher);
        }
    }
}