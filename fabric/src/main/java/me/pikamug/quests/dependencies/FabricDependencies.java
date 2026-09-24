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
import net.fabricmc.loader.api.FabricLoader;
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
     * Returns the names of the YAWP regions the given player is currently
     * standing in. Returns an empty list when YAWP is not installed, the
     * player is offline, or the region data cannot be resolved.
     *
     * @param player the player to check
     * @return the names of the regions the player is inside
     */
    public List<String> getRegionsAt(ServerPlayer player) {
        final List<String> regions = new LinkedList<>();
        if (yawp == null || player == null) {
            return regions;
        }
        try {
            return yawp.getRegionsAt(player);
        } catch (final Exception e) {
            FabricQuestsPlugin.LOGGER.warn("Failed to resolve YAWP regions at player position", e);
            return regions;
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
