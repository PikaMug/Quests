package me.pikamug.quests.dependencies.npc;

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.util.FabricLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiPredicate;

public abstract class FabricEntityNpcDependency implements FabricNpcDependency {
    private final FabricQuestsPlugin plugin;
    private final String label;
    private final String[] npcClassNames;
    private Class<?>[] npcClasses;
    private boolean npcClassesLoaded;

    protected FabricEntityNpcDependency(final FabricQuestsPlugin plugin, final String label,
                                        final String... npcClassNames) {
        this.plugin = plugin;
        this.label = label;
        this.npcClassNames = npcClassNames;
    }

    @Override
    public final @NotNull String getLabel() {
        return label;
    }

    public final boolean isNpc(final Entity entity) {
        if (entity == null) {
            return false;
        }
        for (final Class<?> npcClass : getNpcClasses()) {
            if (npcClass.isInstance(entity)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public final boolean isNpc(final UUID uuid) {
        return findNpc(uuid) != null;
    }

    @Override
    public final @Nullable String getName(final UUID uuid) {
        final Entity entity = findNpc(uuid);
        if (entity == null) {
            return null;
        }
        final Component customName = entity.getCustomName();
        return customName != null ? customName.getString() : entity.getName().getString();
    }

    @Override
    public final @Nullable FabricLocation getLocation(final UUID uuid) {
        return toLocation(findNpc(uuid));
    }

    @Override
    public final @NotNull List<UUID> getAllNpcUniqueIds() {
        final List<UUID> ids = new ArrayList<>();
        for (final Entity entity : getNpcEntities()) {
            ids.add(entity.getUUID());
        }
        return ids;
    }

    @Override
    public final @NotNull Map<UUID, FabricLocation> getNpcsByLocationPredicate(
            final BiPredicate<UUID, FabricLocation> predicate) {
        final Map<UUID, FabricLocation> npcs = new HashMap<>();
        for (final Entity entity : getNpcEntities()) {
            final UUID uuid = entity.getUUID();
            final FabricLocation location = toLocation(entity);
            if (location == null || !predicate.test(uuid, location)) {
                continue;
            }
            npcs.put(uuid, location);
        }
        return npcs;
    }

    public final @Nullable Entity getEntity(final UUID uuid) {
        return findNpc(uuid);
    }

    public final @Nullable UUID getUniqueId(final Entity entity) {
        return isNpc(entity) ? entity.getUUID() : null;
    }

    private @Nullable Entity findNpc(final UUID uuid) {
        if (uuid == null) {
            return null;
        }
        final MinecraftServer server = plugin.getServer();
        if (server == null) {
            return null;
        }
        for (final ServerLevel level : server.getAllLevels()) {
            final Entity entity = level.getEntity(uuid);
            if (entity != null && isNpc(entity)) {
                return entity;
            }
        }
        return null;
    }

    private List<Entity> getNpcEntities() {
        final List<Entity> npcs = new ArrayList<>();
        final MinecraftServer server = plugin.getServer();
        if (server == null) {
            return npcs;
        }
        for (final ServerLevel level : server.getAllLevels()) {
            for (final Entity entity : level.getAllEntities()) {
                if (isNpc(entity)) {
                    npcs.add(entity);
                }
            }
        }
        return npcs;
    }

    private @Nullable FabricLocation toLocation(final Entity entity) {
        if (entity == null || !(entity.level() instanceof ServerLevel)) {
            return null;
        }
        final ServerLevel level = (ServerLevel) entity.level();
        final Vec3 position = entity.position();
        return new FabricLocation(level, position.x, position.y, position.z);
    }

    private Class<?>[] getNpcClasses() {
        if (npcClassesLoaded) {
            return npcClasses;
        }
        npcClassesLoaded = true;
        final List<Class<?>> classes = new ArrayList<>();
        for (final String className : npcClassNames) {
            try {
                classes.add(Class.forName(className));
            } catch (final ClassNotFoundException ignored) {
            }
        }
        npcClasses = classes.toArray(new Class<?>[0]);
        return npcClasses;
    }
}
