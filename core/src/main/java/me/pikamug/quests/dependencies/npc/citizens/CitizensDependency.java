package me.pikamug.quests.dependencies.npc.citizens;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.dependencies.npc.NpcDependency;
import net.citizensnpcs.api.CitizensPlugin;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiPredicate;

public class CitizensDependency implements NpcDependency {
    private final CitizensPlugin citizens;

    public CitizensDependency(BukkitQuestsPlugin plugin) {
        this.citizens = (CitizensPlugin) Bukkit.getServer().getPluginManager().getPlugin("Citizens");
        plugin.getServer().getPluginManager().registerEvents(new CitizensListener(plugin, this), plugin);
    }

    @Override
    public @NotNull String getDependencyName() {
        return "Citizens";
    }

    @Override
    public boolean hasNpc(UUID uuid) {
        return citizens.getNPCRegistry().getByUniqueId(uuid) != null;
    }

    @Override
    public @Nullable String getName(UUID uuid) {
        NPC npc = citizens.getNPCRegistry().getByUniqueId(uuid);
        return npc != null ? npc.getName() : null;
    }

    @Override
    public @Nullable Location getLocation(UUID uuid) {
        NPC npc = citizens.getNPCRegistry().getByUniqueId(uuid);
        return npc != null ? npc.getStoredLocation() : null;
    }

    @Override
    public @NotNull List<UUID> getAllNpcIds() {
        List<UUID> ids = new ArrayList<>();
        for (NPC npc : citizens.getNPCRegistry()) {
            ids.add(npc.getUniqueId());
        }
        return ids;
    }

    @Override
    public @NotNull Map<UUID, Location> getNpcsByLocationPredicate(BiPredicate<UUID, Location> predicate) {
        Map<UUID, Location> npcs = new HashMap<>();
        for (NPC npc : citizens.getNPCRegistry()) {
            UUID uuid = npc.getUniqueId();
            Location location = npc.getStoredLocation();
            if (location.getWorld() == null) {
                continue;
            }
            if (predicate.test(uuid, location)) {
                npcs.put(uuid, location);
            }
        }
        return npcs;
    }

    public CitizensPlugin getCitizens() {
        return citizens;
    }
}
