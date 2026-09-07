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

import de.z0rdak.yawp.api.core.ILevelRegionApi;
import de.z0rdak.yawp.api.core.RegionManager;
import de.z0rdak.yawp.core.region.IMarkableRegion;
import me.pikamug.quests.FabricQuestsPlugin;
import net.fabricmc.loader.api.FabricLoader;
import net.luckperms.api.LuckPermsProvider;
import net.minecraft.server.level.ServerPlayer;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FabricDependencies implements Dependencies {

    private final FabricQuestsPlugin plugin;
    private boolean hasEasyNpc = false;
    private boolean hasTaterzens = false;
    private boolean hasOpenParties = false;
    private boolean hasLuckPerms = false;
    private boolean hasYawp = false;

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
        if (!hasYawp || player == null) {
            return regions;
        }
        try {
            final Optional<ILevelRegionApi> api = RegionManager.get().getDimRegionApi(player.level().dimension());
            if (api.isPresent()) {
                for (final IMarkableRegion region : api.get().getRegionsAt(player.blockPosition())) {
                    final String name = region.getName();
                    if (name != null) {
                        regions.add(name);
                    }
                }
            }
        } catch (final Exception e) {
            FabricQuestsPlugin.LOGGER.warn("Failed to resolve YAWP regions at player position", e);
        }
        return regions;
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
        if (!hasLuckPerms || uuid == null || permission == null) {
            return false;
        }
        try {
            final net.luckperms.api.model.user.User user =
                    LuckPermsProvider.get().getUserManager().getUser(uuid);
            return user != null && user.getCachedData().getPermissionData()
                    .checkPermission(permission).asBoolean();
        } catch (final Exception e) {
            return false;
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
