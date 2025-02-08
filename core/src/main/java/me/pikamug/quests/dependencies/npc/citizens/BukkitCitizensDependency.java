package me.pikamug.quests.dependencies.npc.citizens;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.dependencies.npc.BukkitEntityNpcDependency;
import me.pikamug.quests.dependencies.npc.BukkitNpcDependency;
import me.pikamug.quests.listeners.npc.BukkitCitizensListener;
import net.citizensnpcs.api.CitizensPlugin;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiPredicate;

public class BukkitCitizensDependency implements BukkitNpcDependency, BukkitEntityNpcDependency {
    private final CitizensPlugin citizens;

    public BukkitCitizensDependency(final BukkitQuestsPlugin plugin) {
        this.citizens = (CitizensPlugin) Bukkit.getServer().getPluginManager().getPlugin("Citizens");
        plugin.getServer().getPluginManager().registerEvents(new BukkitCitizensListener(plugin, this), plugin);
    }

    @Override
    public @NotNull String getLabel() {
        return "Citizens";
    }

    @Override
    public boolean isNpc(final UUID uuid) {
        return citizens.getNPCRegistry().getByUniqueId(uuid) != null;
    }

    @Override
    public @Nullable String getName(final UUID uuid) {
        final NPC npc = citizens.getNPCRegistry().getByUniqueId(uuid);
        return npc != null ? npc.getName() : null;
    }

    @Override
    public @Nullable Location getLocation(final UUID uuid) {
        final NPC npc = citizens.getNPCRegistry().getByUniqueId(uuid);
        return npc != null ? npc.getStoredLocation() : null;
    }

    @Override
    public @NotNull List<UUID> getAllNpcUniqueIds() {
        final List<UUID> ids = new ArrayList<>();
        for (final NPC npc : citizens.getNPCRegistry()) {
            ids.add(npc.getUniqueId());
        }
        return ids;
    }

    @Override
    public @NotNull Map<UUID, Location> getNpcsByLocationPredicate(final BiPredicate<UUID, Location> predicate) {
        final Map<UUID, Location> npcs = new HashMap<>();
        for (final NPC npc : citizens.getNPCRegistry()) {
            final UUID uuid = npc.getUniqueId();
            final Location location = npc.getStoredLocation();
            if (location.getWorld() == null) {
                continue;
            }
            if (predicate.test(uuid, location)) {
                npcs.put(uuid, location);
            }
        }
        return npcs;
    }

    @Override
    public boolean isNpc(final Entity entity) {
        return citizens.getNPCRegistry().isNPC(entity);
    }

    @Override
    public @Nullable Entity getEntity(UUID uuid) {
        final NPC npc = citizens.getNPCRegistry().getByUniqueId(uuid);
        return npc != null ? npc.getEntity() : null;
    }

    @Override
    public @Nullable UUID getUniqueId(final Entity entity) {
        final NPC npc = citizens.getNPCRegistry().getNPC(entity);
        return npc != null ? npc.getUniqueId() : null;
    }

    public CitizensPlugin getApi() {
        return citizens;
    }
}
