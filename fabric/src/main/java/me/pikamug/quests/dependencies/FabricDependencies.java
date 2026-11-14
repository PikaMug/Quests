/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.dependencies;

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.events.FabricEventAccessor;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class FabricDependencies implements Dependencies {

    private final FabricQuestsPlugin plugin;
    private boolean hasEasyNpc = false;
    private boolean hasTaterzens = false;
    private boolean hasOpenParties = false;
    private boolean hasLuckPerms = false;
    private boolean hasYawp = false;
    private FabricLuckPermsAccessor luckPerms;
    private FabricYawpAccessor yawp;
    private FabricOpenPartiesAccessor openParties;
    private FabricEventAccessor eventAccessor;

    public FabricDependencies(final FabricQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init() {
        hasEasyNpc = FabricLoader.getInstance().isModLoaded("easy_npc");
        hasTaterzens = FabricLoader.getInstance().isModLoaded("taterzens");
        hasOpenParties = FabricLoader.getInstance().isModLoaded("openpartiesandclaims");
        hasLuckPerms = FabricLoader.getInstance().isModLoaded("luckperms");
        hasYawp = FabricLoader.getInstance().isModLoaded("yawp");

        if (hasEasyNpc) {
            FabricQuestsPlugin.LOGGER.info("Detected {} support", "BOs-Easy-NPC");
        }
        if (hasTaterzens) {
            FabricQuestsPlugin.LOGGER.info("Detected {} support", "Taterzens");
        }
        if (hasOpenParties) {
            FabricQuestsPlugin.LOGGER.info("Detected {} support", "Open Parties and Claims");
        }
        if (hasLuckPerms) {
            FabricQuestsPlugin.LOGGER.info("Detected {} support", "LuckPerms");
        }
        if (hasYawp) {
            FabricQuestsPlugin.LOGGER.info("Detected {} support", "Yet Another World Protector");
        }

        // Optional mods must never be hard-linked at class-load: the JVM
        // resolves their types eagerly, so dangling references crash the
        // server even when the mod is absent. Accessors are only constructed
        // here, guarded by the mod check above.
        if (hasLuckPerms) {
            try {
                luckPerms = new FabricLuckPermsAccessor();
            } catch (final Throwable t) {
                hasLuckPerms = false;
                FabricQuestsPlugin.LOGGER.warn("Failed to initialize LuckPerms support", t);
            }
        }
        if (hasYawp) {
            try {
                yawp = new FabricYawpAccessor();
            } catch (final Throwable t) {
                hasYawp = false;
                FabricQuestsPlugin.LOGGER.warn("Failed to initialize YAWP support", t);
            }
        }
        if (hasOpenParties) {
            try {
                openParties = new FabricOpenPartiesAccessor();
            } catch (final Throwable t) {
                hasOpenParties = false;
                FabricQuestsPlugin.LOGGER.warn("Failed to initialize Open Parties and Claims support", t);
            }
        }
        if (FabricLoader.getInstance().isModLoaded("fabric-api")
                || FabricLoader.getInstance().isModLoaded("fabric-api-base")) {
            try {
                eventAccessor = new FabricEventAccessor();
                FabricQuestsPlugin.LOGGER.info("Detected {} support", "Fabric API events");
            } catch (final Throwable t) {
                FabricQuestsPlugin.LOGGER.warn("Failed to initialize Fabric API event support", t);
            }
        } else {
            FabricQuestsPlugin.LOGGER.debug("Fabric API not detected; Quests events disabled");
        }
    }

    @Override
    public boolean isPluginAvailable(String pluginName) {
        return FabricLoader.getInstance().isModLoaded(pluginName.toLowerCase());
    }

    public boolean isNpc(UUID uuid) {
        return plugin.getQuestNpcUuids().contains(uuid);
    }

    public boolean hasEasyNpc() {
        return hasEasyNpc;
    }

    public boolean hasTaterzens() {
        return hasTaterzens;
    }

    public boolean hasAnyNpcDependencies() {
        return hasEasyNpc || hasTaterzens;
    }

    /**
     * Returns whether the Open Parties and Claims mod is installed, which
     * backs the "use parties plugin" quest option on Fabric.
     */
    public boolean hasOpenParties() {
        return hasOpenParties;
    }

    /**
     * Returns whether the LuckPerms mod is installed.
     */
    public boolean hasLuckPerms() {
        return hasLuckPerms;
    }

    /**
     * Returns whether the Yet Another World Protector (YAWP) mod is installed,
     * which backs the WorldGuard-style region checks on Fabric.
     */
    public boolean hasYawp() {
        return hasYawp;
    }

    /**
     * Returns the Fabric API event accessor, or null when Fabric API is not
     * installed or its event support failed to initialize. Callers must
     * null-check before firing events.
     */
    public FabricEventAccessor getEventAccessor() {
        return eventAccessor;
    }

    /**
     * Returns the names of the regions the given player is currently standing
     * in, using YAWP regions and Open Parties and Claims claims. Returns an
     * empty list when neither mod is installed, the player is offline, or the
     * region data cannot be resolved.
     *
     * @param player the player to check
     * @return the names of the regions the player is inside
     */
    public List<String> getRegionsAt(ServerPlayer player) {
        final List<String> regions = new LinkedList<>();
        if (player == null) {
            return regions;
        }
        if (yawp != null) {
            try {
                addUnique(regions, yawp.getRegionsAt(player));
            } catch (final Exception e) {
                FabricQuestsPlugin.LOGGER.warn("Failed to resolve YAWP regions at player position", e);
            }
        }
        if (openParties != null) {
            try {
                addUnique(regions, openParties.getClaimNamesAt(plugin.getServer(), player));
            } catch (final Exception e) {
                FabricQuestsPlugin.LOGGER.warn("Failed to resolve Open Parties and Claims claims at player position", e);
            }
        }
        return regions;
    }

    /**
     * Checks whether the given player is currently inside the named region,
     * ignoring case and surrounding whitespace. Returns {@code false} when the
     * player is offline, the name is blank, or no region provider is
     * installed.
     */
    public boolean isInRegion(ServerPlayer player, String regionName) {
        if (player == null || regionName == null) {
            return false;
        }
        final String target = ChatFormatting.stripFormatting(regionName).trim();
        if (target.isEmpty()) {
            return false;
        }
        for (final String region : getRegionsAt(player)) {
            if (region != null && ChatFormatting.stripFormatting(region).trim().equalsIgnoreCase(target)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether any region provider (YAWP or Open Parties and Claims)
     * is available for WorldGuard-style region checks.
     */
    public boolean hasRegionSupport() {
        return yawp != null || openParties != null;
    }

    /**
     * Returns the names of all known YAWP regions and Open Parties and Claims
     * claims, for use in editor prompts. Returns an empty list when no region
     * provider is installed or the names cannot be resolved.
     */
    public List<String> getKnownRegionNames() {
        final List<String> names = new LinkedList<>();
        if (plugin.getServer() == null) {
            return names;
        }
        if (yawp != null) {
            for (final net.minecraft.server.level.ServerLevel level : plugin.getServer().getAllLevels()) {
                try {
                    addUnique(names, yawp.getRegionNames(level));
                } catch (final Exception e) {
                    FabricQuestsPlugin.LOGGER.warn("Failed to list YAWP regions", e);
                }
            }
        }
        if (openParties != null) {
            try {
                addUnique(names, openParties.getKnownClaimNames(plugin.getServer()));
            } catch (final Exception e) {
                FabricQuestsPlugin.LOGGER.warn("Failed to list Open Parties and Claims claims", e);
            }
        }
        return names;
    }

    private void addUnique(List<String> target, List<String> source) {
        for (final String value : source) {
            if (value != null && !value.isEmpty() && !target.contains(value)) {
                target.add(value);
            }
        }
    }

    /**
     * Returns the UUIDs of the player's party members via Open Parties and
     * Claims, excluding the player themselves. Returns an empty list when the
     * mod is not installed or the player has no party.
     */
    public List<UUID> getPartyMemberUuids(UUID memberId) {
        final List<UUID> uuids = new LinkedList<>();
        if (openParties == null || memberId == null || plugin.getServer() == null) {
            return uuids;
        }
        try {
            return openParties.getPartyMemberUuids(plugin.getServer(), memberId);
        } catch (final Exception e) {
            FabricQuestsPlugin.LOGGER.warn("Failed to resolve party members for {}", memberId, e);
            return uuids;
        }
    }

    /**
     * Returns the online {@link ServerPlayer}s in the player's party via Open
     * Parties and Claims, excluding the player themselves. Returns an empty
     * list when the mod is not installed or the player has no party.
     */
    public List<ServerPlayer> getOnlinePartyMembers(UUID memberId) {
        final List<ServerPlayer> members = new LinkedList<>();
        if (openParties == null || memberId == null || plugin.getServer() == null) {
            return members;
        }
        try {
            return openParties.getOnlinePartyMembers(plugin.getServer(), memberId);
        } catch (final Exception e) {
            FabricQuestsPlugin.LOGGER.warn("Failed to resolve online party members for {}", memberId, e);
            return members;
        }
    }

    /**
     * Checks whether a player has a permission node, using LuckPerms when it is
     * installed. Mirrors the Bukkit module's Vault-backed permission requirement
     * check. Returns {@code false} when LuckPerms is not available.
     *
     * @param uuid       the player's UUID
     * @param permission the permission node to check
     * @return {@code true} if the player is granted the permission
     */
    public boolean hasPermission(UUID uuid, String permission) {
        if (luckPerms == null || uuid == null || permission == null) {
            return false;
        }
        try {
            return luckPerms.hasPermission(uuid, permission);
        } catch (final Exception e) {
            return false;
        }
    }

    /**
     * Grants a permission node to a player through LuckPerms. No-op when
     * LuckPerms is not installed (mirrors Bukkit where the Vault permission
     * reward silently does nothing without a permission plugin).
     */
    public void grantPermission(UUID uuid, String permission) {
        if (luckPerms == null || uuid == null || permission == null) {
            return;
        }
        try {
            luckPerms.grantPermission(uuid, permission);
        } catch (final Exception e) {
            FabricQuestsPlugin.LOGGER.warn("Failed to grant permission '{}' to {}", permission, uuid, e);
        }
    }

    public String getNpcName(UUID uuid) {
        final net.minecraft.server.level.ServerPlayer player =
                plugin.getServer() != null ? plugin.getServer().getPlayerList().getPlayer(uuid) : null;
        return player != null ? player.getName().getString() : uuid.toString().substring(0, 8);
    }

    /**
     * Returns the TextPlaceholderAPI marker when the placeholder-api mod is
     * installed. Otherwise returns null so placeholder-based conditions are
     * reported as "not installed".
     */
    public Object getPlaceholderApi() {
        return FabricLoader.getInstance().isModLoaded("placeholder-api") ? Boolean.TRUE : null;
    }
}
