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
import me.pikamug.quests.tasks.FabricScheduler;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class FabricEntityListener {

    private final FabricQuestsPlugin plugin;

    public FabricEntityListener(FabricQuestsPlugin plugin) {
        this.plugin = plugin;
        register();
    }

    private void register() {
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((level, killer, victim, damageSource) -> {
            onEntityKilled(level, killer, victim, damageSource);
        });

        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (player instanceof ServerPlayer serverPlayer && hand == InteractionHand.MAIN_HAND) {
                final ItemStack stack = serverPlayer.getMainHandItem();
                if ((entity.getType() == EntityType.COW || entity.getType() == EntityType.MOOSHROOM)
                        && stack.getItem() == Items.BUCKET) {
                    deferredMilkCheck(serverPlayer, level, entity);
                } else if (entity.getType() == EntityType.SHEEP && stack.getItem() == Items.SHEARS) {
                    final Sheep sheep = (Sheep) entity;
                    if (!sheep.isSheared()) {
                        deferredShearCheck(serverPlayer, level, entity);
                    }
                } else if (entity instanceof TamableAnimal tamed && !tamed.isTame()) {
                    deferredTameCheck(serverPlayer, level, entity);
                }
            }
            return InteractionResult.PASS;
        });
    }

    private void onEntityKilled(ServerLevel level, Entity killer, LivingEntity victim, DamageSource damageSource) {
        if (plugin.isLoading() || killer == null || victim == null) return;
        final ServerPlayer player = resolveKillerPlayer(level, killer);
        if (player == null || victim.getUUID().equals(player.getUUID())) return;
        final FabricQuester quester = plugin.getQuester(player.getUUID());

        for (final Quest quest : plugin.getLoadedQuests()) {
            if (!quester.getCurrentQuests().containsKey(quest)) continue;
            final Stage stage = quester.getCurrentStage(quest);
            if (stage == null) continue;

            if (stage.getNpcsToKill().contains(victim.getUUID())
                    || plugin.getDependencies().isNpc(victim.getUUID())) {
                boolean matched = false;
                for (int i = 0; i < stage.getNpcsToKill().size(); i++) {
                    if (stage.getNpcsToKill().get(i).equals(victim.getUUID())) {
                        final var progress = quester.getQuestProgressOrDefault(quest);
                        progress.getNpcsNumKilled().set(i, progress.getNpcsNumKilled().get(i) + 1);
                        matched = true;
                    }
                }
                if (matched) quester.checkQuest(quest);
                continue;
            }

            if (victim instanceof ServerPlayer && stage.getPlayersToKill() != null
                    && stage.getPlayersToKill() > 0) {
                final var progress = quester.getQuestProgressOrDefault(quest);
                progress.setPlayersKilled(progress.getPlayersKilled() + 1);
                quester.checkQuest(quest);
                continue;
            }

            if (!stage.getMobsToKill().isEmpty()) {
                boolean matched = false;
                for (int i = 0; i < stage.getMobsToKill().size(); i++) {
                    if (matchesEntityName(stage.getMobsToKill().get(i), victim.getType())) {
                        final var progress = quester.getQuestProgressOrDefault(quest);
                        progress.getMobNumKilled().set(i, progress.getMobNumKilled().get(i) + 1);
                        matched = true;
                    }
                }
                if (matched) quester.checkQuest(quest);
            }
        }
    }

    private ServerPlayer resolveKillerPlayer(ServerLevel level, Entity killer) {
        if (killer instanceof ServerPlayer player) return player;
        if (killer instanceof TamableAnimal animal && animal.isTame()
                && animal.getOwnerReference() != null
                && animal.getOwnerReference().getUUID() != null) {
            return level.getServer().getPlayerList().getPlayer(animal.getOwnerReference().getUUID());
        }
        if (killer instanceof Projectile projectile && projectile.getOwner() instanceof ServerPlayer player) {
            return player;
        }
        return null;
    }

    private boolean matchesEntityName(Object goal, EntityType<?> type) {
        if (goal == null || type == null) return false;
        final String target = goal.toString().trim();
        if (target.isEmpty()) return false;
        final Identifier key = EntityType.getKey(type);
        final String keyString = key.toString();
        final String path = key.getPath();
        if (keyString.equalsIgnoreCase(target)) return true;
        if (path.equalsIgnoreCase(target)) return true;
        return ("minecraft:" + target).equalsIgnoreCase(keyString);
    }

    private void deferredMilkCheck(ServerPlayer player, Level level, Entity entity) {
        final UUID playerId = player.getUUID();
        FabricScheduler.runLater(() -> {
            if (!(level instanceof ServerLevel serverLevel)) return;
            final ServerPlayer p = serverLevel.getServer().getPlayerList().getPlayer(playerId);
            if (p == null) return;
            if (p.getMainHandItem().getItem() == Items.MILK_BUCKET) {
                applyMilkMilked(p);
            }
        }, 1);
    }

    private void applyMilkMilked(ServerPlayer player) {
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        for (final Quest quest : plugin.getLoadedQuests()) {
            if (!quester.getCurrentQuests().containsKey(quest)) continue;
            final Stage stage = quester.getCurrentStage(quest);
            if (stage == null || stage.getCowsToMilk() == null || stage.getCowsToMilk() <= 0) continue;
            final var progress = quester.getQuestProgressOrDefault(quest);
            progress.setCowsMilked(progress.getCowsMilked() + 1);
            quester.checkQuest(quest);
        }
    }

    private void deferredShearCheck(ServerPlayer player, Level level, Entity entity) {
        final UUID playerId = player.getUUID();
        final UUID entityId = entity.getUUID();
        FabricScheduler.runLater(() -> {
            if (!(level instanceof ServerLevel serverLevel)) return;
            final ServerPlayer p = serverLevel.getServer().getPlayerList().getPlayer(playerId);
            if (p == null) return;
            final Entity e = serverLevel.getEntity(entityId);
            if (e instanceof Sheep sheep && sheep.isSheared()) {
                applySheepSheared(p, sheep.getColor().getName(), sheep.getColor().name());
            }
        }, 1);
    }

    private void applySheepSheared(ServerPlayer player, String colorName, String colorEnumName) {
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        for (final Quest quest : plugin.getLoadedQuests()) {
            if (!quester.getCurrentQuests().containsKey(quest)) continue;
            final Stage stage = quester.getCurrentStage(quest);
            if (stage == null || stage.getSheepToShear().isEmpty()) continue;
            boolean matched = false;
            for (int i = 0; i < stage.getSheepToShear().size(); i++) {
                final Object goal = stage.getSheepToShear().get(i);
                if (goal == null) continue;
                final String lowercase = goal.toString().trim().toLowerCase(java.util.Locale.ENGLISH);
                if (lowercase.equals(colorName.toLowerCase(java.util.Locale.ENGLISH))
                        || lowercase.equals(colorEnumName.toLowerCase(java.util.Locale.ENGLISH))) {
                    final var progress = quester.getQuestProgressOrDefault(quest);
                    progress.getSheepSheared().set(i, progress.getSheepSheared().get(i) + 1);
                    matched = true;
                }
            }
            if (matched) quester.checkQuest(quest);
        }
    }

    private void deferredTameCheck(ServerPlayer player, Level level, Entity entity) {
        final UUID playerId = player.getUUID();
        final UUID entityId = entity.getUUID();
        FabricScheduler.runLater(() -> {
            if (!(level instanceof ServerLevel serverLevel)) return;
            final ServerPlayer p = serverLevel.getServer().getPlayerList().getPlayer(playerId);
            if (p == null) return;
            final Entity e = serverLevel.getEntity(entityId);
            if (e instanceof TamableAnimal animal && animal.isTame()
                    && animal.getOwnerReference() != null
                    && playerId.equals(animal.getOwnerReference().getUUID())) {
                applyMobTamed(p, animal.getType());
            }
        }, 1);
    }

    private void applyMobTamed(ServerPlayer player, EntityType<?> type) {
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        for (final Quest quest : plugin.getLoadedQuests()) {
            if (!quester.getCurrentQuests().containsKey(quest)) continue;
            final Stage stage = quester.getCurrentStage(quest);
            if (stage == null || stage.getMobsToTame().isEmpty()) continue;
            boolean matched = false;
            for (int i = 0; i < stage.getMobsToTame().size(); i++) {
                if (matchesEntityName(stage.getMobsToTame().get(i), type)) {
                    final var progress = quester.getQuestProgressOrDefault(quest);
                    progress.getMobsTamed().set(i, progress.getMobsTamed().get(i) + 1);
                    matched = true;
                }
            }
            if (matched) quester.checkQuest(quest);
        }
    }
}