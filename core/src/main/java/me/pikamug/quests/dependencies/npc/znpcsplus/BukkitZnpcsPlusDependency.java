package me.pikamug.quests.dependencies.npc.znpcsplus;

import lol.pyr.znpcsplus.api.NpcApi;
import lol.pyr.znpcsplus.api.NpcApiProvider;
import lol.pyr.znpcsplus.api.entity.EntityProperty;
import lol.pyr.znpcsplus.api.npc.Npc;
import lol.pyr.znpcsplus.api.npc.NpcEntry;
import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.dependencies.npc.BukkitNpcDependency;
import me.pikamug.quests.listeners.npc.BukkitZnpcsPlusListener;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiPredicate;

public class BukkitZnpcsPlusDependency implements BukkitNpcDependency {
    private static final NpcApi api = NpcApiProvider.get();

    public BukkitZnpcsPlusDependency(final BukkitQuestsPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(new BukkitZnpcsPlusListener(plugin, this), plugin);
    }

    @Override
    public @NotNull String getLabel() {
        return "ZNPCsPlus";
    }

    private @Nullable Npc getNpc(final UUID uuid) {
        final NpcEntry npcEntry = api.getNpcRegistry().getByUuid(uuid);
        return npcEntry != null ? npcEntry.getNpc() : null;
    }

    @Override
    public boolean isNpc(final UUID uuid) {
        return getNpc(uuid) != null;
    }

    @Override
    public @Nullable String getName(final UUID uuid) {
        final Npc npc = getNpc(uuid);
        if (npc == null) return null;
        final EntityProperty<String> displayNameProperty = api.getPropertyRegistry().getByName("display_name", String.class);
        if (displayNameProperty != null && npc.hasProperty(displayNameProperty)) {
            return npc.getProperty(displayNameProperty);
        }
        return null;
    }

    @Override
    public @Nullable Location getLocation(final UUID uuid) {
        final Npc npc = getNpc(uuid);
        if (npc == null) return null;
        final World world = npc.getWorld();
        if (world == null) return null;
        return npc.getLocation().toBukkitLocation(world);
    }

    @Override
    public @NotNull List<UUID> getAllNpcUniqueIds() {
        final List<UUID> ids = new ArrayList<>();
        for (final NpcEntry npcEntry : api.getNpcRegistry().getAllPlayerMade()) {
            ids.add(npcEntry.getNpc().getUuid());
        }
        return ids;
    }

    @Override
    public @NotNull Map<UUID, Location> getNpcsByLocationPredicate(final BiPredicate<UUID, Location> predicate) {
        final Map<UUID, Location> npcs = new HashMap<>();
        for (final NpcEntry npcEntry : api.getNpcRegistry().getAllPlayerMade()) {
            final Npc npc = npcEntry.getNpc();
            final World world = npc.getWorld();
            if (world == null) {
                continue;
            }
            final Location location = npc.getLocation().toBukkitLocation(world);
            if (predicate.test(npc.getUuid(), location)) {
                npcs.put(npc.getUuid(), location);
            }
        }
        return npcs;
    }

    public NpcApi getApi() {
        return api;
    }
}
