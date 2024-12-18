package me.pikamug.quests.dependencies.npc;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiPredicate;

public interface NpcDependency {
    @NotNull String getDependencyName();

    boolean isNpc(final UUID uuid);

    @Nullable String getName(final UUID uuid);

    @Nullable Location getLocation(final UUID uuid);

    @NotNull List<UUID> getAllNpcIds();

    @NotNull Map<UUID, Location> getNpcsByLocationPredicate(final BiPredicate<UUID, Location> predicate);

    default @NotNull Map<UUID, Location> getNpcsByNearbyLocation(final Location location, final double radius) {
        if (location.getWorld() == null) return Collections.emptyMap();
        return getNpcsByLocationPredicate((uuid, npcLocation) ->
                npcLocation.getWorld().getName().equals(location.getWorld().getName()) && location.distance(npcLocation) < radius
        );
    }

    default @NotNull Map<UUID, Location> getNpcsByNearbyLocationSquared(final Location location, final double radius) {
        if (location.getWorld() == null) return Collections.emptyMap();
        return getNpcsByLocationPredicate((uuid, npcLocation) ->
                npcLocation.getWorld().getName().equals(location.getWorld().getName()) && location.distanceSquared(npcLocation) < radius);
    }
}
