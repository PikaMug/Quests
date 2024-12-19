package me.pikamug.quests.dependencies.npc.fancynpcs;

import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import de.oliver.fancynpcs.api.Npc;
import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.dependencies.npc.NpcDependency;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class FancyNpcsDependency implements NpcDependency {
    private final FancyNpcsPlugin api;

    public FancyNpcsDependency(BukkitQuestsPlugin plugin) {
        this.api = FancyNpcsPlugin.get();
        plugin.getServer().getPluginManager().registerEvents(new FancyNpcsListener(plugin, this), plugin);
    }

    @Override
    public @NotNull String getDependencyName() {
        return "FancyNpcs";
    }

    private @Nullable Npc getNpc(UUID uuid) {
        return api.getNpcManager().getNpcById(uuid.toString());
    }

    @Override
    public boolean isNpc(UUID uuid) {
        return getNpc(uuid) != null;
    }

    @Override
    public @Nullable String getName(UUID uuid) {
        Npc npc = getNpc(uuid);
        return npc != null ? npc.getData().getDisplayName() : null;
    }

    @Override
    public @Nullable Location getLocation(UUID uuid) {
        Npc npc = getNpc(uuid);
        return npc != null ? npc.getData().getLocation() : null;
    }

    @Override
    public @NotNull List<UUID> getAllNpcIds() {
        return api.getNpcManager().getAllNpcs().stream()
                .map(npc -> UUID.fromString(npc.getData().getId()))
                .collect(Collectors.toList());
    }

    @Override
    public @NotNull Map<UUID, Location> getNpcsByLocationPredicate(BiPredicate<UUID, Location> predicate) {
        return api.getNpcManager().getAllNpcs().stream()
                .map(npc -> new AbstractMap.SimpleEntry<>(UUID.fromString(npc.getData().getId()), npc.getData().getLocation()))
                .filter(entry -> predicate.test(entry.getKey(), entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
