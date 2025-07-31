package me.pikamug.quests.dependencies.npc;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface NpcDependency {
    boolean isNpc(final UUID uuid);

    @Nullable
    String getName(final UUID uuid);
}
