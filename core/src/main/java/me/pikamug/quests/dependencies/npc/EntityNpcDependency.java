package me.pikamug.quests.dependencies.npc;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface EntityNpcDependency {
    boolean hasNpc(final Entity entity);

    @Nullable Entity getEntity(final UUID uuid);

    @Nullable UUID getId(final Entity entity);
}
