package me.pikamug.quests.tasks;

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.dependencies.FabricDependencies;
import me.pikamug.quests.enums.ObjectiveType;
import me.pikamug.quests.player.FabricQuester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.Stage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

public class FabricPlayerMoveThread implements Runnable {

    private final FabricQuestsPlugin plugin;
    private long interval = 1;

    public FabricPlayerMoveThread(FabricQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    @Override
    public void run() {
        if (plugin.getServer() == null) return;
        for (final ServerPlayer player : plugin.getServer().getPlayerList().getPlayers()) {
            if (((FabricDependencies) plugin.getDependencies()).isNpc(player.getUUID())) continue;
            checkLocationObjectives(player);
        }
    }

    private void checkLocationObjectives(ServerPlayer player) {
        if (plugin.isLoading()) return;
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        final Vec3 playerPos = player.position();
        final String currentWorld = player.level().dimension().identifier().getPath();
        final ObjectiveType type = ObjectiveType.REACH_LOCATION;
        final Set<String> dispatchedQuestIDs = new HashSet<>();

        for (final Quest quest : plugin.getLoadedQuests()) {
            if (!quester.meetsCondition(quest, true)) continue;
            if (quester.getCurrentQuests().containsKey(quest)
                    && quester.getCurrentStage(quest) != null
                    && quester.getCurrentStage(quest).containsObjective(type)) {
                applyReachLocation(quester, quest, playerPos, currentWorld);
            }

            dispatchedQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, type, (q, cq) -> {
                if (!dispatchedQuestIDs.contains(cq.getId())) {
                    applyReachLocation((FabricQuester) q, cq, playerPos, currentWorld);
                }
                return null;
            }));
        }
    }

    private void applyReachLocation(FabricQuester quester, Quest quest, Vec3 playerPos, String currentWorld) {
        final Stage stage = quester.getCurrentStage(quest);
        if (stage == null) return;
        if (stage.getLocationsToReach().isEmpty()) return;
        if (!stage.hasLocatableObjective()) return;

        final me.pikamug.quests.player.QuestProgress progress = quester.getQuestProgressOrDefault(quest);
        boolean changed = false;
        for (int i = 0; i < stage.getLocationsToReach().size(); i++) {
            final Object locObj = stage.getLocationsToReach().get(i);
            if (locObj == null) continue;
            final String locStr = locObj.toString();
            // Parse "world:x,y,z" format
            final String[] parts = locStr.split(":");
            if (parts.length < 2) continue;
            final String worldName = parts[0];
            final String[] coords = parts[1].split(",");
            if (coords.length < 3) continue;
            try {
                final double x = Double.parseDouble(coords[0]);
                final double y = Double.parseDouble(coords[1]);
                final double z = Double.parseDouble(coords[2]);
                if (!currentWorld.equalsIgnoreCase(worldName)) continue;
                final int radius = (stage.getRadiiToReachWithin() != null && stage.getRadiiToReachWithin().size() > i)
                        ? stage.getRadiiToReachWithin().get(i) : 0;
                final double dist = playerPos.distanceTo(new Vec3(x, y, z));
                if (dist <= radius + 1) { // +1 for edge tolerance
                    if (progress.getLocationsReached().size() <= i) continue;
                    if (!progress.getLocationsReached().get(i)) {
                        progress.getLocationsReached().set(i, true);
                        quester.finishObjective(quest, ObjectiveType.REACH_LOCATION, i);
                        changed = true;
                    }
                }
            } catch (final NumberFormatException ignored) {}
        }
        if (changed) {
            quester.checkQuest(quest);
        }
    }
}
