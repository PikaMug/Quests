package me.pikamug.quests.dependencies.npc.citizens;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.dependencies.npc.NpcDependency;
import net.citizensnpcs.api.CitizensPlugin;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    public @NotNull String getName(UUID uuid) {
        NPC npc = citizens.getNPCRegistry().getByUniqueId(uuid);
        return npc != null ? npc.getName() : "NPC";
    }

    @Override
    public @Nullable Location getLocation(UUID uuid) {
        NPC npc = citizens.getNPCRegistry().getByUniqueId(uuid);
        return npc != null ? npc.getStoredLocation() : null;
    }

    @Override
    public @NotNull List<UUID> getNpcIds() {
        List<UUID> ids = new ArrayList<>();
        for (NPC npc : citizens.getNPCRegistry()) {
            ids.add(npc.getUniqueId());
        }
        return ids;
    }

    @Override
    public @NotNull List<UUID> getNearbyNpcIds(Location location, double distance) {
        List<UUID> ids = new ArrayList<>();
        for (NPC npc : citizens.getNPCRegistry()) {
            if (npc.getStoredLocation().getWorld() == null || location.getWorld() == null) {
                continue;
            }
            if (npc.getStoredLocation().getWorld().getName().equals(location.getWorld().getName())) {
                if (npc.getStoredLocation().distanceSquared(location) < distance) {
                    ids.add(npc.getUniqueId());
                }
            }
        }
        return ids;
    }
}
