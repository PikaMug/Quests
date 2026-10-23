package me.pikamug.quests.util;


import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.ProfileResolver;

import java.util.Optional;
import java.util.UUID;

public final class FabricOfflinePlayer {
    private final MinecraftServer server;

    private UUID uuid;
    private String name;

    public FabricOfflinePlayer(final MinecraftServer server, final UUID uuid) {
        this.server = server;
        this.uuid = uuid;
        this.name = null;
        resolveName();
    }

    public FabricOfflinePlayer(final MinecraftServer server, final String name) {
        this.server = server;
        this.name = name;
        this.uuid = null;
        resolveUuid();
    }

    private ProfileResolver getProfileResolver() {
        if (server == null) {
            return null;
        }

        // Use the profile resolver exposed by your 26.2
        // MinecraftServer mapping.
        return server.services().profileResolver();
    }

    private void resolveUuid() {
        if (server == null || name == null || name.isEmpty()) {
            return;
        }

        final ProfileResolver resolver = getProfileResolver();
        if (resolver == null) {
            return;
        }

        final Optional<GameProfile> profile =
                resolver.fetchByName(name);

        profile.ifPresent(found -> {
            this.uuid = found.id();
            this.name = found.name();
        });
    }

    private void resolveName() {
        if (server == null || uuid == null) {
            return;
        }

        final ServerPlayer online =
                server.getPlayerList().getPlayer(uuid);

        if (online != null) {
            this.name = online.getGameProfile().name();
            return;
        }

        final ProfileResolver resolver = getProfileResolver();
        if (resolver == null) {
            return;
        }

        resolver.fetchById(uuid).ifPresent(profile ->
                this.name = profile.name()
        );
    }

    private NameAndId getNameAndId() {
        final UUID id = getUniqueId();
        final String playerName = getName();

        if (id == null || playerName == null) {
            return null;
        }

        return new NameAndId(id, playerName);
    }

    public UUID getUniqueId() {
        if (uuid == null) {
            resolveUuid();
        }
        return uuid;
    }

    public UUID getUuid() {
        return getUniqueId();
    }

    public String getName() {
        if (name == null) {
            resolveName();
        }
        return name;
    }

    public GameProfile getProfile() {
        final UUID id = getUniqueId();
        final String playerName = getName();

        if (id == null || playerName == null) {
            return null;
        }

        return new GameProfile(id, playerName);
    }

    public boolean isOnline() {
        return server != null
                && uuid != null
                && server.getPlayerList().getPlayer(uuid) != null;
    }

    public boolean isBanned() {
        if (server == null) {
            return false;
        }

        final NameAndId identity = getNameAndId();
        if (identity == null) {
            return false;
        }

        return server.getPlayerList()
                .getBans()
                .isBanned(identity);
    }

    public CompoundTag loadData() {
        if (server == null) {
            return null;
        }

        final NameAndId identity = getNameAndId();
        if (identity == null) {
            return null;
        }

        return server.getPlayerList()
                .loadPlayerData(identity)
                .orElse(null);
    }

    public boolean hasPlayedBefore() {
        return loadData() != null;
    }

    /**
     * PlayerDataStorage in 26.2 does not expose a public method
     * for saving an arbitrary CompoundTag by UUID.<p>
     * Save changes through the actual player/save lifecycle,
     * or implement a separate, version-aware NBT file writer.
     */
    @Deprecated
    public void saveData(final CompoundTag tag) {
        throw new UnsupportedOperationException(
                "Saving arbitrary offline player NBT is not "
                        + "supported by PlayerDataStorage's public API."
        );
    }
}

