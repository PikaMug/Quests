package me.pikamug.quests.tasks;

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.dependencies.FabricDependencies;
import me.pikamug.quests.player.FabricQuester;
import me.pikamug.quests.quests.Quest;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Method;
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

        final ParticleOptions particle = getParticleEffect();
        if (particle == null) return;

        for (final UUID npcUuid : plugin.getQuestNpcUuids()) {
            final Vec3 npcPos = getNpcLocation(npcUuid);
            if (npcPos == null) continue;

            // Show particles to nearby questers
            for (final ServerPlayer player : plugin.getServer().getPlayerList().getPlayers()) {
                if (player.position().distanceTo(npcPos) > 16) continue;
                final FabricQuester quester = plugin.getQuester(player.getUUID());
                for (final Quest quest : quester.getCurrentQuests().keySet()) {
                    // Check if this NPC is relevant to the quest
                    final var stage = quester.getCurrentStage(quest);
                    if (stage == null) continue;
                    if (stage.getNpcsToInteract().contains(npcUuid) || stage.getNpcsToKill().contains(npcUuid)) {
                        final ServerLevel level = player.serverLevel();
                        level.sendParticles(player, particle, false, npcPos.x, npcPos.y + 2, npcPos.z, 5, 0.3, 0.5, 0.3, 0.01);
                        break;
                    }
                }
            }
        }
    }

    private Vec3 getNpcLocation(UUID npcUuid) {
        // NPC libs (BOs-Easy-NPC, Taterzens) store NPCs as regular entities
        // Use reflection to try known APIs, fall back to entity lookup
        if (((FabricDependencies) plugin.getDependencies()).hasEasyNpc()) {
            try {
                final Class<?> npcApi = Class.forName("com.bos infos.easynpc.api.NpcAPI");
                final Method getNpc = npcApi.getMethod("getNpc", UUID.class);
                final Object npc = getNpc.invoke(null, npcUuid);
                if (npc != null) {
                    final Method getEntity = npc.getClass().getMethod("getEntity");
                    final Object entity = getEntity.invoke(npc);
                    if (entity instanceof net.minecraft.world.entity.Entity mcEntity) {
                        return mcEntity.position();
                    }
                }
            } catch (final Exception ignored) {}
        }
        if (((FabricDependencies) plugin.getDependencies()).hasTaterzens()) {
            try {
                final Class<?> taterzensApi = Class.forName("org.policymc.taterzens.api.TaterzensAPI");
                final Method getNpc = taterzensApi.getMethod("getNpc", UUID.class);
                final Object npc = getNpc.invoke(null, npcUuid);
                if (npc != null) {
                    final Method getEntity = npc.getClass().getMethod("npcEntity");
                    final Object entity = getEntity.invoke(npc);
                    if (entity instanceof net.minecraft.world.entity.Entity mcEntity) {
                        return mcEntity.position();
                    }
                }
            } catch (final Exception ignored) {}
        }
        return findEntityLocation(npcUuid);
    }

    private Vec3 findEntityLocation(UUID uuid) {
        final MinecraftServer server = plugin.getServer();
        if (server == null) return null;
        for (final ServerLevel level : server.getAllLevels()) {
            final var entity = level.getEntity(uuid);
            if (entity != null) {
                return entity.position();
            }
        }
        return null;
    }

    private ParticleOptions getParticleEffect() {
        final String effectName = plugin.getConfigSettings().getEffect();
        if (effectName == null) return null;
        switch (effectName.toUpperCase()) {
            case "ENCHANTMENT_TABLE": return ParticleTypes.ENCHANT;
            case "FLAME": return ParticleTypes.FLAME;
            case "CRIT": return ParticleTypes.CRIT;
            case "HEART": return ParticleTypes.HEART;
            case "PORTAL": return ParticleTypes.PORTAL;
            case "VILLAGER_HAPPY": return ParticleTypes.HAPPY_VILLAGER;
            case "SPELL": return ParticleTypes.INSTANT_EFFECT;
            default: return ParticleTypes.ENCHANT;
        }
    }
}
