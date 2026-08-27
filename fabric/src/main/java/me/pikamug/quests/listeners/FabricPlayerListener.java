package me.pikamug.quests.listeners;

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.player.FabricQuester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.Stage;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;

public class FabricPlayerListener {

    private final FabricQuestsPlugin plugin;

    public FabricPlayerListener(FabricQuestsPlugin plugin) {
        this.plugin = plugin;
        register();
    }

    private void register() {
        // Entity interact (right-click entity - for NPC interaction objectives)
        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (player instanceof ServerPlayer serverPlayer && hand == InteractionHand.MAIN_HAND) {
                onEntityInteract(serverPlayer, entity);
            }
            return InteractionResult.PASS;
        });

        // Entity death (for KILL_MOB and KILL_PLAYER objectives)
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            // We listen for entity death via tracking living entities
        });

        // Player join - load quester data
        ServerLifecycleEvents.PLAYER_JOIN.register((server, player) -> {
            if (plugin.isLoading()) return;
            final FabricQuester quester = plugin.getQuester(player.getUUID());
            quester.setLastKnownName(player.getName().getString());
            if (!quester.hasData()) {
                quester.saveData();
            }
            // Start stage timers for any active quests
            for (final Quest quest : quester.getCurrentQuests().keySet()) {
                quester.startStageTimer(quest);
            }
        });

        // Player quit - save quester data
        ServerLifecycleEvents.PLAYER_DISCONNECTION.register((server, player) -> {
            if (plugin.isLoading()) return;
            final FabricQuester quester = plugin.getQuester(player.getUUID());
            for (final Quest quest : quester.getCurrentQuests().keySet()) {
                quester.stopStageTimer(quest);
            }
            quester.saveData();
        });
    }

    private void onEntityInteract(ServerPlayer player, Entity entity) {
        if (plugin.isLoading()) return;
        final FabricQuester quester = plugin.getQuester(player.getUUID());

        for (final Quest quest : plugin.getLoadedQuests()) {
            if (!quester.getCurrentQuests().containsKey(quest)) continue;
            final Stage stage = quester.getCurrentStage(quest);
            if (stage == null) continue;

            // TALK_TO_NPC
            if (!stage.getNpcsToInteract().isEmpty()) {
                for (int i = 0; i < stage.getNpcsToInteract().size(); i++) {
                    final UUID npcUuid = stage.getNpcsToInteract().get(i);
                    if (entity.getUUID().equals(npcUuid)) {
                        quester.getQuestProgressOrDefault(quest).getNpcsInteracted().set(i, true);
                    }
                }
            }
        }
    }

    public void onMobKill(ServerPlayer killer, LivingEntity victim) {
        if (plugin.isLoading() || killer == null) return;
        final FabricQuester quester = plugin.getQuester(killer.getUUID());

        for (final Quest quest : plugin.getLoadedQuests()) {
            if (!quester.getCurrentQuests().containsKey(quest)) continue;
            final Stage stage = quester.getCurrentStage(quest);
            if (stage == null) continue;

            // KILL_MOB
            if (!stage.getMobsToKill().isEmpty()) {
                for (int i = 0; i < stage.getMobsToKill().size(); i++) {
                    final Object goal = stage.getMobsToKill().get(i);
                    if (goal != null && victim.getType().toString().equalsIgnoreCase(goal.toString())) {
                        final var progress = quester.getQuestProgressOrDefault(quest);
                        progress.getMobNumKilled().set(i, progress.getMobNumKilled().get(i) + 1);
                    }
                }
            }

            // KILL_PLAYER
            if (victim instanceof ServerPlayer && stage.getPlayersToKill() != null && stage.getPlayersToKill() > 0) {
                quester.getQuestProgressOrDefault(quest).setPlayersKilled(
                        quester.getQuestProgressOrDefault(quest).getPlayersKilled() + 1);
            }

            // TAME_MOB and SHEAR_SHEEP require mixins for proper detection on Fabric
        }
    }
}
