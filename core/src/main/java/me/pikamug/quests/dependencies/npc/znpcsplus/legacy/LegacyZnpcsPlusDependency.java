package me.pikamug.quests.dependencies.npc.znpcsplus.legacy;

import io.github.znetworkw.znpcservers.npc.NPC;
import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.dependencies.npc.EntityNpcDependency;
import me.pikamug.quests.dependencies.npc.NpcDependency;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class LegacyZnpcsPlusDependency implements NpcDependency, EntityNpcDependency {

    public LegacyZnpcsPlusDependency(BukkitQuestsPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(new LegacyZnpcsPlusListener(plugin, this), plugin);
    }

    @Override
    public @NotNull String getDependencyName() {
        return "ZNPCsPlus-Legacy";
    }

    private @Nullable NPC getNpc(UUID uuid) {
        return NPC.all().stream().filter(npc -> npc.getUUID().equals(uuid)).findAny().orElse(null);
    }

    @Override
    public boolean isNpc(UUID uuid) {
        return getNpc(uuid) != null;
    }

    @Override
    public @Nullable String getName(UUID uuid) {
        NPC npc = getNpc(uuid);
        if (npc == null) {
            return null;
        }
        Entity entity = (Entity) npc.getBukkitEntity();
        String customName = entity.getCustomName();
        if (customName == null) {
            customName = npc.getNpcPojo().getHologramLines().get(0);
        }
        return customName;
    }

    @Override
    public @Nullable Location getLocation(UUID uuid) {
        NPC npc = getNpc(uuid);
        return npc != null ? npc.getLocation() : null;
    }

    @Override
    public @NotNull List<UUID> getAllNpcIds() {
        return NPC.all().stream().map(NPC::getUUID).collect(Collectors.toList());
    }

    @Override
    public @NotNull Map<UUID, Location> getNpcsByLocationPredicate(BiPredicate<UUID, Location> predicate) {
        return NPC.all().stream()
                .filter(npc -> predicate.test(npc.getUUID(), npc.getLocation()))
                .collect(Collectors.toMap(NPC::getUUID, NPC::getLocation));
    }

    private @Nullable NPC getNpc(Entity entity) {
        return NPC.all().stream().filter(npc -> npc.getUUID().equals(entity.getUniqueId())).findAny().orElse(null);
    }

    @Override
    public boolean isNpc(Entity entity) {
        return getNpc(entity) != null;
    }

    @Override
    public @Nullable Entity getEntity(UUID uuid) {
        NPC npc = getNpc(uuid);
        return npc != null ? (Entity) npc.getBukkitEntity() : null;
    }

    @Override
    public @Nullable UUID getId(Entity entity) {
        NPC npc = getNpc(entity);
        return npc != null ? npc.getUUID() : null;
    }
}
