package me.pikamug.quests.dependencies.npc.znpcsplus.legacy;

import io.github.znetworkw.znpcservers.npc.NPC;
import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.dependencies.npc.BukkitEntityNpcDependency;
import me.pikamug.quests.dependencies.npc.BukkitNpcDependency;
import me.pikamug.quests.listeners.npc.BukkitZnpcsPlusLegacyListener;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class BukkitZnpcsPlusLegacyDependency implements BukkitNpcDependency, BukkitEntityNpcDependency {

    public BukkitZnpcsPlusLegacyDependency(final BukkitQuestsPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(new BukkitZnpcsPlusLegacyListener(plugin, this), plugin);
    }

    @Override
    public @NotNull String getLabel() {
        return "ZNPCsPlus-Legacy";
    }

    private @Nullable NPC getNpc(final UUID uuid) {
        return NPC.all().stream().filter(npc -> npc.getUUID().equals(uuid)).findAny().orElse(null);
    }

    @Override
    public boolean isNpc(final UUID uuid) {
        return getNpc(uuid) != null;
    }

    @Override
    public @Nullable String getName(final UUID uuid) {
        final NPC npc = getNpc(uuid);
        if (npc == null) {
            return null;
        }
        final Entity entity = (Entity) npc.getBukkitEntity();
        String customName = entity.getCustomName();
        if (customName == null) {
            customName = npc.getNpcPojo().getHologramLines().get(0);
        }
        return customName;
    }

    @Override
    public @Nullable Location getLocation(final UUID uuid) {
        final NPC npc = getNpc(uuid);
        return npc != null ? npc.getLocation() : null;
    }

    @Override
    public @NotNull List<UUID> getAllNpcUniqueIds() {
        return NPC.all().stream().map(NPC::getUUID).collect(Collectors.toList());
    }

    @Override
    public @NotNull Map<UUID, Location> getNpcsByLocationPredicate(final BiPredicate<UUID, Location> predicate) {
        return NPC.all().stream()
                .filter(npc -> predicate.test(npc.getUUID(), npc.getLocation()))
                .collect(Collectors.toMap(NPC::getUUID, NPC::getLocation));
    }

    private @Nullable NPC getNpc(final Entity entity) {
        return NPC.all().stream().filter(npc -> npc.getUUID().equals(entity.getUniqueId())).findAny().orElse(null);
    }

    @Override
    public boolean isNpc(final Entity entity) {
        return getNpc(entity) != null;
    }

    @Override
    public @Nullable Entity getEntity(final UUID uuid) {
        final NPC npc = getNpc(uuid);
        return npc != null ? (Entity) npc.getBukkitEntity() : null;
    }

    @Override
    public @Nullable UUID getUniqueId(final Entity entity) {
        final NPC npc = getNpc(entity);
        return npc != null ? npc.getUUID() : null;
    }
}
