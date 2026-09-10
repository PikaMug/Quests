package me.pikamug.quests.listeners;

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.QuestsEvents;
import me.pikamug.quests.player.FabricQuester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.Stage;
import me.pikamug.quests.util.FabricInventoryUtil;
import me.pikamug.quests.util.FabricItemUtil;
import me.pikamug.quests.util.FabricLang;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.Random;
import java.util.UUID;

public class FabricPlayerListener {

    private final FabricQuestsPlugin plugin;

    public FabricPlayerListener(FabricQuestsPlugin plugin) {
        this.plugin = plugin;
        register();
    }

    private void register() {
        // Entity interact (right-click entity - for NPC interaction objectives). The mixin already filters
        // to MAIN_HAND, so a non-null ServerPlayer is guaranteed here.
        QuestsEvents.registerUseEntity((player, entity) -> {
            onEntityInteract(player, entity);
            return false;
        });

        // Player join - load quester data
        QuestsEvents.registerPlayerJoin(player -> {
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
        QuestsEvents.registerPlayerDisconnect(player -> {
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

            // DELIVER_ITEM
            if (!stage.getItemsToDeliver().isEmpty()) {
                final ItemStack hand = player.getMainHandItem();
                boolean npcMatched = false;
                boolean delivered = false;
                for (int i = 0; i < stage.getItemsToDeliver().size()
                        && i < stage.getItemDeliveryTargets().size(); i++) {
                    if (!stage.getItemDeliveryTargets().get(i).equals(entity.getUUID())) continue;
                    npcMatched = true;
                    final Object goalObj = stage.getItemsToDeliver().get(i);
                    if (!(goalObj instanceof ItemStack goal)) continue;
                    if (FabricItemUtil.compareItems(goal, hand, true) != 0) continue;
                    delivered = true;
                    deliverToNPC(player, quester, quest, stage, i, goal, hand);
                }
                if (npcMatched && !delivered) {
                    quester.sendMessage(FabricLang.get("questInvalidDeliveryItem")
                            .replace("<item>", FabricItemUtil.getName(hand)));
                }
            }

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

    private void deliverToNPC(ServerPlayer player, FabricQuester quester, Quest quest, Stage stage,
                              int index, ItemStack goal, ItemStack hand) {
        final var progress = quester.getQuestProgressOrDefault(quest);
        if (progress == null || progress.getItemsDelivered().size() <= index) return;
        final int goalAmount = Math.max(1, goal.getCount());
        final int amount = progress.getItemsDelivered().get(index);
        final int newProgress = Math.min(hand.getCount() + amount, goalAmount);
        progress.getItemsDelivered().set(index, newProgress);

        final int removed = newProgress - amount;
        if (removed > 0) {
            FabricInventoryUtil.removeItemIgnoreAmount(player, goal, removed);
        }

        if (newProgress >= goalAmount) {
            quester.checkQuest(quest);
        } else {
            final var messages = stage.getDeliverMessages();
            if (messages != null && !messages.isEmpty()) {
                final String message = messages.get(new Random().nextInt(messages.size()))
                        .replace("<item>", FabricItemUtil.getName(goal))
                        .replace("<npc>", plugin.getDependencies().getNpcName(stage.getItemDeliveryTargets().get(index)))
                        .replace("<count>", String.valueOf(goalAmount - newProgress))
                        .replace("<amount>", String.valueOf(goalAmount - newProgress));
                quester.sendMessage(message);
            }
        }
    }
}
