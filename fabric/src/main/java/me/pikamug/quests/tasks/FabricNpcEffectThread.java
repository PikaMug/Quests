package me.pikamug.quests.tasks;

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.events.quester.FabricQuesterPostViewEffectEvent;
import me.pikamug.quests.player.FabricQuester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.Stage;
import me.pikamug.quests.util.FabricLocation;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SpellParticleOption;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FabricNpcEffectThread implements Runnable {

    private final FabricQuestsPlugin plugin;
    private int tickCounter = 0;

    public FabricNpcEffectThread(FabricQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (plugin.getServer() == null) return;
        tickCounter++;
        if (tickCounter < 20) return; // Run every second
        tickCounter = 0;

        if (!plugin.getConfigSettings().canNpcEffects()) return;

        for (final UUID npcUuid : collectQuestNpcUuids()) {
            final Vec3 npcPos = getNpcLocation(npcUuid);
            if (npcPos == null) continue;

            for (final ServerPlayer player : plugin.getServer().getPlayerList().getPlayers()) {
                if (player.position().distanceToSqr(npcPos) > 24) continue;
                final FabricQuester quester = plugin.getQuester(player.getUUID());
                if (!showObjectiveEffect(quester, player, npcUuid, npcPos)) {
                    showConfigEffect(quester, player, npcUuid, npcPos);
                }
            }
        }
    }

    private boolean showObjectiveEffect(final FabricQuester quester, final ServerPlayer player,
                                        final UUID npcUuid, final Vec3 npcPos) {
        for (final Quest quest : quester.getCurrentQuests().keySet()) {
            final Stage stage = quester.getCurrentStage(quest);
            if (stage == null) continue;
            if (stage.getNpcsToInteract().contains(npcUuid) || stage.getNpcsToKill().contains(npcUuid)) {
                showEffect(quester, player, npcUuid, npcPos, plugin.getConfigSettings().getEffect(), false);
                return true;
            }
        }
        return false;
    }

    private void showConfigEffect(final FabricQuester quester, final ServerPlayer player,
                                  final UUID npcUuid, final Vec3 npcPos) {
        if (quester.canAcceptQuest(npcUuid)) {
            showEffect(quester, player, npcUuid, npcPos, plugin.getConfigSettings().getEffect(), false);
        } else if (quester.canAcceptCompletedRedoableQuest(npcUuid)) {
            showEffect(quester, player, npcUuid, npcPos, plugin.getConfigSettings().getRedoEffect(), true);
        }
    }

    private void showEffect(final FabricQuester quester, final ServerPlayer player, final UUID targetUuid,
                            final Vec3 npcPos, final String effectName, final boolean redoable) {
        final ParticleOptions particle = getParticleEffect(effectName);
        if (particle == null) return;
        final ServerLevel level = player.level();
        level.sendParticles(player, particle, false, false,
                npcPos.x, npcPos.y + 2, npcPos.z, 5, 0.3, 0.5, 0.3, 0.01);
        final FabricLocation targetLocation = new FabricLocation(level, npcPos.x, npcPos.y + 2, npcPos.z);
        final FabricQuesterPostViewEffectEvent event = new FabricQuesterPostViewEffectEvent(
                quester, targetUuid, targetLocation, effectName, redoable);
        FabricQuesterPostViewEffectEvent.EVENT.invoker().onEvent(event);
    }

    private Set<UUID> collectQuestNpcUuids() {
        final Set<UUID> npcUuids = new HashSet<>(plugin.getQuestNpcUuids());
        for (final Quest quest : plugin.getLoadedQuests()) {
            if (quest.getNpcStart() != null) {
                npcUuids.add(quest.getNpcStart());
            }
            for (final Stage stage : quest.getStages()) {
                if (stage == null) continue;
                npcUuids.addAll(stage.getNpcsToInteract());
                npcUuids.addAll(stage.getNpcsToKill());
            }
        }
        return npcUuids;
    }

    private Vec3 getNpcLocation(UUID npcUuid) {
        final FabricLocation location = plugin.getDependencies().getNpcLocation(npcUuid);
        return location != null ? location.toVec3() : findEntityLocation(npcUuid);
    }

    private Vec3 findEntityLocation(UUID uuid) {
        final MinecraftServer server = plugin.getServer();
        if (server == null) return null;
        for (final ServerLevel level : server.getAllLevels()) {
            final net.minecraft.world.entity.Entity entity = level.getEntity(uuid);
            if (entity != null) {
                return entity.position();
            }
        }
        return null;
    }

    private ParticleOptions getParticleEffect(final String effectName) {
        if (effectName == null) return null;
        switch (effectName.toUpperCase()) {
            case "ANGRY_VILLAGER": return ParticleTypes.ANGRY_VILLAGER;
            case "ENCHANTMENT_TABLE": return ParticleTypes.ENCHANT;
            case "FLAME": return ParticleTypes.FLAME;
            case "CRIT": return ParticleTypes.CRIT;
            case "HEART": return ParticleTypes.HEART;
            case "NOTE": return ParticleTypes.NOTE;
            case "PORTAL": return ParticleTypes.PORTAL;
            case "VILLAGER_HAPPY": return ParticleTypes.HAPPY_VILLAGER;
            case "SPELL": return SpellParticleOption.create(ParticleTypes.INSTANT_EFFECT, 0xFFFFFF, 1.0f);
            default: return ParticleTypes.ENCHANT;
        }
    }
}
