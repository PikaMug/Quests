/*******************************************************************************************************
 * Continued by PikaMug (formerly HappyPikachu) with permission from _Blackvein_. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

package me.blackvein.quests.util;

import java.lang.reflect.Method;
import java.util.logging.Level;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.Association;
import com.sk89q.worldguard.protection.association.Associables;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.managers.RegionManager;

/**
 * @author NathanWolf
 */
public class WorldGuardAPI {
    private Object worldGuard = null;
    private WorldGuardPlugin worldGuardPlugin = null;
    private Object regionContainer = null;
    private Method regionContainerGetMethod = null;
    private Method worldAdaptMethod = null;
    private boolean initialized = false;

    public boolean isEnabled() {
        return worldGuardPlugin != null;
    }

    public WorldGuardAPI(Plugin wg) {
        if (wg instanceof WorldGuardPlugin) {
            worldGuardPlugin = (WorldGuardPlugin)wg;

            try {
                Class<?> worldGuardClass = Class.forName("com.sk89q.worldguard.WorldGuard");
                Method getInstanceMethod = worldGuardClass.getMethod("getInstance");
                worldGuard = getInstanceMethod.invoke(null);
                // WorldGuard 7+
            } catch (Exception ex) {
                // WorldGuard <7
            }
        }
    }

    protected RegionAssociable getAssociable(Player player) {
        RegionAssociable associable;
        if (player == null) {
            associable = Associables.constant(Association.NON_MEMBER);
        } else {
            associable = worldGuardPlugin.wrapPlayer(player);
        }

        return associable;
    }
    
    private void initialize() {
        if (!initialized) {
            initialized = true;
            if (worldGuard != null) {
            	// WorldGuard 7+
                try {
                    Method getPlatFormMethod = worldGuard.getClass().getMethod("getPlatform");
                    Object platform = getPlatFormMethod.invoke(worldGuard);
                    Method getRegionContainerMethod = platform.getClass().getMethod("getRegionContainer");
                    regionContainer = getRegionContainerMethod.invoke(platform);
                    Class<?> worldEditWorldClass = Class.forName("com.sk89q.worldedit.world.World");
                    Class<?> worldEditAdapterClass = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
                    worldAdaptMethod = worldEditAdapterClass.getMethod("adapt", World.class);
                    regionContainerGetMethod = regionContainer.getClass().getMethod("get", worldEditWorldClass);
                } catch (Exception e) {
                	Bukkit.getLogger().log(Level.WARNING, "Quests failed to bind to WorldGuard, integration will not work!", e);
                    regionContainer = null;
                    return;
                }
            } else {
            	// WorldGuard <7
                regionContainer = worldGuardPlugin.getRegionContainer();
                try {
                    regionContainerGetMethod = regionContainer.getClass().getMethod("get", World.class);
                } catch (Exception e) {
                	Bukkit.getLogger().log(Level.WARNING, "Quests failed to bind to WorldGuard, integration will not work!", e);
                    regionContainer = null;
                    return;
                }
            }

            if (regionContainer == null) {
            	Bukkit.getLogger().warning("Quests failed to find RegionContainer, WorldGuard integration will not function!");
            }
        }
    }

    @Nullable
    public RegionManager getRegionManager(World world) {
        initialize();
        if (regionContainer == null || regionContainerGetMethod == null) return null;
        RegionManager regionManager = null;
        try {
            if (worldAdaptMethod != null) {
            	// WorldGuard 7+
                Object worldEditWorld = worldAdaptMethod.invoke(null, world);
                regionManager = (RegionManager)regionContainerGetMethod.invoke(regionContainer, worldEditWorld);
            } else {
            	// WorldGuard <7
                regionManager = (RegionManager)regionContainerGetMethod.invoke(regionContainer, world);
            }
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Quests encountered an error getting WorldGuard RegionManager", e);
        }
        return regionManager;
    }
}