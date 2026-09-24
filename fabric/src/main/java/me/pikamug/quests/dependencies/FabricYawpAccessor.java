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
import net.minecraft.server.level.ServerPlayer;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Delegates region lookups to YAWP (Yet Another World Protector). Only
 * instantiated when the YAWP mod is present, so its API classes are never
 * linked otherwise.
 */
public class FabricYawpAccessor {

    public List<String> getRegionsAt(ServerPlayer player) {
        final List<String> regions = new LinkedList<>();
        final Optional<ILevelRegionApi> api = RegionManager.get().getDimRegionApi(player.level().dimension());
        if (api.isPresent()) {
            for (final IMarkableRegion region : api.get().getRegionsAt(player.blockPosition())) {
                final String name = region.getName();
                if (name != null) {
                    regions.add(name);
                }
            }
        }
        return regions;
    }
}