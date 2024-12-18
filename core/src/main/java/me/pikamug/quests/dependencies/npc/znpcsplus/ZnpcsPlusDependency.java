package me.pikamug.quests.dependencies.npc.znpcsplus;

import lol.pyr.znpcsplus.api.NpcApi;
import lol.pyr.znpcsplus.api.NpcApiProvider;
import lol.pyr.znpcsplus.api.entity.EntityProperty;
import lol.pyr.znpcsplus.api.npc.Npc;
import lol.pyr.znpcsplus.api.npc.NpcEntry;
import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.dependencies.npc.NpcDependency;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiPredicate;

public class ZnpcsPlusDependency implements NpcDependency {
    private final NpcApi api;

    public ZnpcsPlusDependency(BukkitQuestsPlugin plugin) {
        this.api = NpcApiProvider.get();
        plugin.getServer().getPluginManager().registerEvents(new ZnpcsPlusListener(plugin, this), plugin);
    }

    @Override
    public @NotNull String getDependencyName() {
        return "ZNPCsPlus";
    }

    @Override
    public boolean isNpc(UUID uuid) {
        return api.getNpcRegistry().getByUuid(uuid) != null;
    }

    @Override
    public @Nullable String getName(UUID uuid) {
        Npc npc = api.getNpcRegistry().getByUuid(uuid).getNpc();
        if (npc == null) return null;
        EntityProperty<String> displayNameProperty = api.getPropertyRegistry().getByName("display_name", String.class);
        if (displayNameProperty != null && npc.hasProperty(displayNameProperty)) {
            return npc.getProperty(displayNameProperty);
        }
        return null;
    }

    @Override
    public @Nullable Location getLocation(UUID uuid) {
        Npc npc = api.getNpcRegistry().getByUuid(uuid).getNpc();
        if (npc == null) return null;
        World world = npc.getWorld();
        if (world == null) return null;
        return npc.getLocation().toBukkitLocation(world);
    }

    @Override
    public @NotNull List<UUID> getAllNpcIds() {
        List<UUID> ids = new ArrayList<>();
        for (NpcEntry npcEntry : api.getNpcRegistry().getAllPlayerMade()) {
            ids.add(npcEntry.getNpc().getUuid());
        }
        return ids;
    }

    @Override
    public @NotNull Map<UUID, Location> getNpcsByLocationPredicate(BiPredicate<UUID, Location> predicate) {
        Map<UUID, Location> npcs = new HashMap<>();
        for (NpcEntry npcEntry : api.getNpcRegistry().getAllPlayerMade()) {
            Npc npc = npcEntry.getNpc();
            World world = npc.getWorld();
            if (world == null) {
                continue;
            }
            Location location = npc.getLocation().toBukkitLocation(world);
            if (predicate.test(npc.getUuid(), location)) {
                npcs.put(npc.getUuid(), location);
            }
        }
        return npcs;
    }
}
