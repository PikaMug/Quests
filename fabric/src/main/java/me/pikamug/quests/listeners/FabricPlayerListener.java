package me.pikamug.quests.listeners;

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.player.FabricQuester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.Stage;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;

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

        // Player join - load quester data
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            final ServerPlayer player = handler.getPlayer();
            if (plugin.isLoading() || player == null) return;
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
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            final ServerPlayer player = handler.getPlayer();
            if (plugin.isLoading() || player == null) return;
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
                boolean matched = false;
                for (int i = 0; i < stage.getNpcsToInteract().size(); i++) {
                    final UUID npcUuid = stage.getNpcsToInteract().get(i);
                    if (entity.getUUID().equals(npcUuid)) {
                        quester.getQuestProgressOrDefault(quest).getNpcsInteracted().set(i, true);
                        matched = true;
                    }
                }
                if (matched) quester.checkQuest(quest);
            }
        }
    }
}
