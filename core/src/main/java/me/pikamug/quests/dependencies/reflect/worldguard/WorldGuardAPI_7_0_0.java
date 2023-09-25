/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.dependencies.reflect.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class WorldGuardAPI_7_0_0 {

    @Nullable
    public static WorldGuard getInstance() {
        return WorldGuard.getInstance();
    }
    
    @Nullable
    public static RegionManager getRegionManager(final World world) {
        return WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
    }
    
    public static @NotNull ApplicableRegionSet getApplicableRegions(final World world, final Location location) {
        return Objects.requireNonNull(getRegionManager(world)).getApplicableRegions(BukkitAdapter
                .asBlockVector(location));
    }
    
    public static @NotNull List<String> getApplicableRegionsIDs(final World world, final Location location) {
        return Objects.requireNonNull(getRegionManager(world)).getApplicableRegionsIDs(BukkitAdapter
                .asBlockVector(location));
    }
}
