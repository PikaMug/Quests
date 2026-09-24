package me.pikamug.quests.listeners;

import me.pikamug.quests.FabricMixinEvents;
import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.player.FabricQuester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.Stage;
import me.pikamug.quests.tasks.FabricScheduler;
import me.pikamug.quests.util.FabricInventoryUtil;
import me.pikamug.quests.util.FabricItemUtil;
import me.pikamug.quests.util.FabricLang;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FabricPlayerListener {

    private final FabricQuestsPlugin plugin;
    private final ConcurrentHashMap<UUID, ItemStack> stashedJournals = new ConcurrentHashMap<>();

    public FabricPlayerListener(FabricQuestsPlugin plugin) {
        this.plugin = plugin;
        register();
    }

    private void register() {
        // Entity interact (right-click entity - for NPC interaction objectives). The mixin already filters
        // to MAIN_HAND, so a non-null ServerPlayer is guaranteed here.
        FabricMixinEvents.registerUseEntity((player, entity) -> {
            onEntityInteract(player, entity);
            return false;
        });

        // Player join - load quester data
        FabricMixinEvents.registerPlayerJoin(player -> {
            if (plugin.isLoading() || player == null) return;
            plugin.applyLoadedQuester(player.getUUID());
            final FabricQuester quester = plugin.getQuester(player.getUUID());
            quester.setLastKnownName(player.getName().getString());
            quester.saveData();
            // Start stage timers for any active quests
            for (final Quest quest : quester.getCurrentQuests().keySet()) {
                quester.startStageTimer(quest);
            }
            // Auto-accept global quests at login
            FabricScheduler.runLater(() -> {
                final ServerPlayer p = plugin.getServer() == null ? null
                        : plugin.getServer().getPlayerList().getPlayer(player.getUUID());
                if (p == null) return;
                boolean alreadyHasAtLeastOneGlobalQuest = false;
                for (final Quest cq : quester.getCurrentQuests().keySet()) {
                    if (cq.getOptions().canGiveGloballyAtLogin()) {
                        alreadyHasAtLeastOneGlobalQuest = true;
                        break;
                    }
                }
                for (final Quest quest : plugin.getLoadedQuests()) {
                    if (quest.getOptions() == null || !quest.getOptions().canGiveGloballyAtLogin()) continue;
                    if (quester.getCurrentQuests().containsKey(quest)) continue;
                    if (quester.getCompletedQuests().contains(quest)
                            && (quest.getPlanner() == null || quest.getPlanner().getCooldown() < 0)) {
                        continue;
                    }
                    if (!quest.getOptions().canAllowStackingGlobal() && alreadyHasAtLeastOneGlobalQuest) {
                        continue;
                    }
                    if (quester.canAcceptOffer(quest, quest.getOptions().canInformOnStart())) {
                        alreadyHasAtLeastOneGlobalQuest = true;
                        quester.takeQuest(quest, false);
                    }
                }
                if (quester.hasJournal()) {
                    quester.updateJournal();
                }
                quester.findCompassTarget();
            }, 20);
        });

        // Player quit - fire disconnect action, stop timers, save quester data
        FabricMixinEvents.registerPlayerDisconnect(player -> {
            if (plugin.isLoading() || player == null) return;
            final FabricQuester quester = plugin.getQuester(player.getUUID());
            for (final Quest quest : quester.getCurrentQuests().keySet()) {
                quester.stopStageTimer(quest);
                final Stage currentStage = quester.getCurrentStage(quest);
                if (currentStage != null && currentStage.getDisconnectAction() != null) {
                    currentStage.getDisconnectAction().fire(quester, quest);
                }
            }
            quester.saveData();
        });

        // Player death - fire stage death actions and keep the quest journal from dropping
        FabricMixinEvents.registerPlayerDeath((player, damageSource) -> {
            if (plugin.isLoading() || player == null) return;
            onPlayerDeath(player);
        });

        // Player respawn - restore stashed journal and refresh the quest compass
        FabricMixinEvents.registerPlayerRespawn(player -> {
            if (plugin.isLoading() || player == null) return;
            onPlayerRespawn(player);
        });

        // Player changes dimension - refresh compass and re-validate conditions
        FabricMixinEvents.registerPlayerChangeDimension(player -> {
            if (plugin.isLoading() || player == null) return;
            final FabricQuester quester = plugin.getQuester(player.getUUID());
            quester.findCompassTarget();
            for (final Quest quest : plugin.getLoadedQuests()) {
                quester.meetsCondition(quest, true);
            }
        });

        // Compass item interactions - right click cycles to the next tracked quest
        FabricMixinEvents.registerUseItem((player, hand) -> onCompassRightClick(player));
        FabricMixinEvents.registerUseBlock((player, pos, hand) -> onCompassRightClick(player));
        // Left click (block or air) while holding a compass resets tracking
        FabricMixinEvents.registerAttackBlock((player, pos) -> onCompassLeftClick(player));
        FabricMixinEvents.registerSwingAir(this::onCompassLeftClick);
    }

    private void onCompassRightClick(ServerPlayer player) {
        if (plugin.isLoading() || player == null) return;
        if (player.getMainHandItem().getItem() != Items.COMPASS) return;
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        if (quester.canUseCompass()) {
            quester.findNextCompassTarget(true);
        }
    }

    private void onCompassLeftClick(ServerPlayer player) {
        if (plugin.isLoading() || player == null) return;
        if (player.getMainHandItem().getItem() != Items.COMPASS) return;
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        if (quester.canUseCompass()) {
            quester.resetCompass();
            quester.sendMessage(FabricLang.get(quester.getServerPlayer(), "compassReset"));
        }
    }

    private void onPlayerDeath(ServerPlayer player) {
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        for (final Quest quest : plugin.getLoadedQuests()) {
            if (!quester.getCurrentQuests().containsKey(quest)) continue;
            final Stage stage = quester.getCurrentStage(quest);
            if (stage != null && stage.getDeathAction() != null) {
                stage.getDeathAction().fire(quester, quest);
            }
        }
        final var inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            final ItemStack slot = inventory.getItem(i);
            if (!slot.isEmpty() && FabricItemUtil.isJournal(slot)) {
                stashedJournals.put(player.getUUID(), slot.copy());
                inventory.setItem(i, ItemStack.EMPTY);
                break;
            }
        }
    }

    private void onPlayerRespawn(ServerPlayer player) {
        final ItemStack journal = stashedJournals.remove(player.getUUID());
        if (journal != null && !journal.isEmpty()) {
            final var inventory = player.getInventory();
            boolean present = false;
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                if (FabricItemUtil.isJournal(inventory.getItem(i))) {
                    present = true;
                    break;
                }
            }
            if (!present) {
                inventory.add(journal);
            }
        }
        final UUID playerId = player.getUUID();
        FabricScheduler.runLater(() -> {
            final ServerPlayer p = plugin.getServer() == null ? null
                    : plugin.getServer().getPlayerList().getPlayer(playerId);
            if (p != null) {
                plugin.getQuester(playerId).findCompassTarget();
            }
        }, 10);
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
                    quester.sendMessage(FabricLang.get(quester.getServerPlayer(), "questInvalidDeliveryItem")
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
