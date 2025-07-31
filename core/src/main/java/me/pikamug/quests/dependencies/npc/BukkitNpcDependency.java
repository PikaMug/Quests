package me.pikamug.quests.dependencies.npc;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiPredicate;

public interface BukkitNpcDependency extends NpcDependency {
    @NotNull String getLabel();

    @Nullable Location getLocation(final UUID uuid);

    @NotNull List<UUID> getAllNpcUniqueIds();

    @NotNull Map<UUID, Location> getNpcsByLocationPredicate(final BiPredicate<UUID, Location> predicate);

    default @NotNull Map<UUID, Location> getNpcsByNearbyLocation(final Location location, final double radius) {
        if (location.getWorld() == null) return Collections.emptyMap();
        return getNpcsByLocationPredicate((uuid, npcLocation) ->
                npcLocation.getWorld().getUID().equals(location.getWorld().getUID()) && location.distance(npcLocation) < radius
        );
    }

    default @NotNull Map<UUID, Location> getNpcsByNearbyLocationSquared(final Location location, final double radius) {
        if (location.getWorld() == null) return Collections.emptyMap();
        return getNpcsByLocationPredicate((uuid, npcLocation) ->
                npcLocation.getWorld().getUID().equals(location.getWorld().getUID()) && location.distanceSquared(npcLocation) < radius);
    }
}
