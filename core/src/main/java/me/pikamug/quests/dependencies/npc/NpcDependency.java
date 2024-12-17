package me.pikamug.quests.dependencies.npc;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface NpcDependency {
    @NotNull String getDependencyName();

    boolean hasNpc(final UUID uuid);

    @NotNull String getName(final UUID uuid);

    @Nullable Location getLocation(final UUID uuid);

    @NotNull List<UUID> getNpcIds();

    @NotNull List<UUID> getNearbyNpcIds(final Location location, final double distance);
}
