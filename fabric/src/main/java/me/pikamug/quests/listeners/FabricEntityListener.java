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

import me.pikamug.quests.FabricMixinEvents;
import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.enums.ObjectiveType;
import me.pikamug.quests.player.FabricQuester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.Stage;
import me.pikamug.quests.tasks.FabricScheduler;
import me.pikamug.quests.util.FabricMiscUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
        FabricMixinEvents.registerEntityKilled(this::onEntityKilled);

        FabricMixinEvents.registerFishingCatch(this::onFishingCatch);

        FabricMixinEvents.registerUseEntity((serverPlayer, entity) -> {
            if (entity != null) {
                final ItemStack stack = serverPlayer.getMainHandItem();
                if ((entity.getType() == FabricMiscUtil.COW || entity.getType() == FabricMiscUtil.MOOSHROOM)
                        && stack.getItem() == Items.BUCKET) {
                    deferredMilkCheck(serverPlayer, serverPlayer.level(), entity);
                } else if (entity.getType() == FabricMiscUtil.SHEEP && stack.getItem() == Items.SHEARS) {
                    final Sheep sheep = (Sheep) entity;
                    if (!sheep.isSheared()) {
                        deferredShearCheck(serverPlayer, serverPlayer.level(), entity);
                    }
                } else if (entity instanceof TamableAnimal tamed && !tamed.isTame()) {
                    deferredTameCheck(serverPlayer, serverPlayer.level(), entity);
                }
            }
            return false;
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
                dispatchNpcKill(quester, quest, victim);
                continue;
            }

            if (victim instanceof ServerPlayer && stage.getPlayersToKill() != null
                    && stage.getPlayersToKill() > 0) {
                final var progress = quester.getQuestProgressOrDefault(quest);
                progress.setPlayersKilled(progress.getPlayersKilled() + 1);
                quester.checkQuest(quest);
                dispatchPlayerKill(quester, quest);
                continue;
            }

            if (!stage.getMobsToKill().isEmpty()) {
                boolean matched = false;
                for (int i = 0; i < stage.getMobsToKill().size(); i++) {
                    if (matchesEntityName(stage.getMobsToKill().get(i), victim.getType())
                            && isWithinKillLocation(stage, i, victim.getX(), victim.getY(), victim.getZ(), level)) {
                        final var progress = quester.getQuestProgressOrDefault(quest);
                        progress.getMobNumKilled().set(i, progress.getMobNumKilled().get(i) + 1);
                        matched = true;
                    }
                }
                if (matched) quester.checkQuest(quest);
                dispatchMobKill(quester, quest, stage, victim, level);
            }
        }
    }

    private void dispatchMobKill(FabricQuester quester, Quest quest, Stage stage, LivingEntity victim,
            ServerLevel level) {
        quester.dispatchMultiplayerEverything(quest, ObjectiveType.KILL_MOB, (q, cq) -> {
            final Stage qStage = q.getCurrentStage(cq);
            if (qStage == null || qStage.getMobsToKill().isEmpty()) return null;
            final var qProgress = q.getQuestProgressOrDefault(cq);
            for (int i = 0; i < qStage.getMobsToKill().size(); i++) {
                if (!matchesEntityName(qStage.getMobsToKill().get(i), victim.getType())) continue;
                if (!isWithinKillLocation(qStage, i, victim.getX(), victim.getY(), victim.getZ(), level)) continue;
                if (qProgress.getMobNumKilled().size() <= i) continue;
                final int qGoal = (qStage.getMobNumToKill() != null && qStage.getMobNumToKill().size() > i)
                        ? qStage.getMobNumToKill().get(i) : 1;
                qProgress.getMobNumKilled().set(i, Math.min(qProgress.getMobNumKilled().get(i) + 1, qGoal));
                if (q.testComplete(cq)) q.checkQuest(cq);
            }
            return null;
        });
    }

    private void dispatchNpcKill(FabricQuester quester, Quest quest, LivingEntity victim) {
        quester.dispatchMultiplayerEverything(quest, ObjectiveType.KILL_NPC, (q, cq) -> {
            final Stage qStage = q.getCurrentStage(cq);
            if (qStage == null || qStage.getNpcsToKill().isEmpty()) return null;
            final var qProgress = q.getQuestProgressOrDefault(cq);
            for (int i = 0; i < qStage.getNpcsToKill().size(); i++) {
                if (!qStage.getNpcsToKill().get(i).equals(victim.getUUID())) continue;
                if (qProgress.getNpcsNumKilled().size() <= i) continue;
                final int qGoal = (qStage.getNpcNumToKill() != null && qStage.getNpcNumToKill().size() > i)
                        ? qStage.getNpcNumToKill().get(i) : 1;
                qProgress.getNpcsNumKilled().set(i, Math.min(qProgress.getNpcsNumKilled().get(i) + 1, qGoal));
                if (q.testComplete(cq)) q.checkQuest(cq);
            }
            return null;
        });
    }

    private void dispatchPlayerKill(FabricQuester quester, Quest quest) {
        quester.dispatchMultiplayerEverything(quest, ObjectiveType.KILL_PLAYER, (q, cq) -> {
            final Stage qStage = q.getCurrentStage(cq);
            if (qStage == null || qStage.getPlayersToKill() == null || qStage.getPlayersToKill() <= 0) return null;
            final var qProgress = q.getQuestProgressOrDefault(cq);
            qProgress.setPlayersKilled(Math.min(qProgress.getPlayersKilled() + 1, qStage.getPlayersToKill()));
            if (q.testComplete(cq)) q.checkQuest(cq);
            return null;
        });
    }

    private boolean isWithinKillLocation(Stage stage, int index, double x, double y, double z, ServerLevel level) {
        if (stage.getLocationsToKillWithin() == null || stage.getLocationsToKillWithin().isEmpty()) return true;
        if (stage.getLocationsToKillWithin().size() <= index) return true;
        final Object locObj = stage.getLocationsToKillWithin().get(index);
        if (locObj == null) return true;
        // Parse "world:x,y,z" format (same as reach-location objectives)
        final String[] loc = locObj.toString().split(":");
        if (loc.length < 2) return true;
        final String[] coords = loc[1].split(",");
        if (coords.length < 3) return true;
        final int radius = (stage.getRadiiToKillWithin() != null && stage.getRadiiToKillWithin().size() > index)
                ? stage.getRadiiToKillWithin().get(index) : 0;
        try {
            final double lx = Double.parseDouble(coords[0]);
            final double ly = Double.parseDouble(coords[1]);
            final double lz = Double.parseDouble(coords[2]);
            if (!level.dimension().identifier().getPath().equalsIgnoreCase(loc[0])) return false;
            return x < lx + radius && x > lx - radius
                    && z < lz + radius && z > lz - radius
                    && y < ly + radius && y > ly - radius;
        } catch (final NumberFormatException e) {
            return true;
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
        if (killer instanceof net.minecraft.world.entity.item.PrimedTnt tnt && tnt.getOwner() != null) {
            return resolveKillerPlayer(level, tnt.getOwner());
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

    private void onFishingCatch(ServerPlayer player) {
        if (plugin.isLoading() || player == null) return;
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        for (final Quest quest : plugin.getLoadedQuests()) {
            if (!quester.getCurrentQuests().containsKey(quest)) continue;
            final Stage stage = quester.getCurrentStage(quest);
            if (stage == null || stage.getFishToCatch() == null || stage.getFishToCatch() <= 0) continue;
            final var progress = quester.getQuestProgressOrDefault(quest);
            progress.setFishCaught(progress.getFishCaught() + 1);
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