package me.pikamug.quests.dependencies.npc;

import me.pikamug.quests.util.FabricLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiPredicate;

public interface FabricNpcDependency extends NpcDependency {
    @NotNull String getLabel();

    @Nullable FabricLocation getLocation(final UUID uuid);

    @NotNull List<UUID> getAllNpcUniqueIds();

    @NotNull Map<UUID, FabricLocation> getNpcsByLocationPredicate(final BiPredicate<UUID, FabricLocation> predicate);

    default @NotNull Map<UUID, FabricLocation> getNpcsByNearbyLocation(final FabricLocation location, final double radius) {
        if (location.getWorld() == null) return Collections.emptyMap();
        return getNpcsByLocationPredicate((uuid, npcLocation) ->
                npcLocation.getWorldId().equals(location.getWorldId()) && location.distance(npcLocation) < radius
        );
    }

    default @NotNull Map<UUID, FabricLocation> getNpcsByNearbyLocationSquared(final FabricLocation location, final double radius) {
        if (location.getWorld() == null) return Collections.emptyMap();
        return getNpcsByLocationPredicate((uuid, npcLocation) ->
                npcLocation.getWorldId().equals(location.getWorldId()) && location.distanceSquared(npcLocation) < radius);
    }
}
