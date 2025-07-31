package me.pikamug.quests.dependencies.npc;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface BukkitEntityNpcDependency extends NpcDependency {
    boolean isNpc(final Entity entity);

    @Nullable Entity getEntity(final UUID uuid);

    @Nullable UUID getUniqueId(final Entity entity);
}
