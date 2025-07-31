/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.tasks;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.dependencies.npc.BukkitNpcDependency;
import me.pikamug.quests.enums.BukkitPreBuiltParticle;
import me.pikamug.quests.events.quester.BukkitQuesterPostViewEffectEvent;
import me.pikamug.quests.nms.BukkitParticleProvider;
import me.pikamug.quests.player.BukkitQuester;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class BukkitNpcEffectThread implements Runnable {

    final BukkitQuestsPlugin plugin;

    public BukkitNpcEffectThread(final BukkitQuestsPlugin quests) {
        plugin = quests;
    }

    @Override
    public void run() {
        for (final Player player : plugin.getServer().getOnlinePlayers()) {
            for (final BukkitNpcDependency npcDependency : plugin.getDependencies().getNpcDependencies()) {
                final Map<UUID, Location> npcLocations = npcDependency.getNpcsByNearbyLocationSquared(player.getLocation(), 24);
                for (Map.Entry<UUID, Location> entry : npcLocations.entrySet()) {
                    showConfigEffect(plugin.getQuester(player.getUniqueId()), entry.getKey(), entry.getValue());
                }
            }
        }
    }

    /**
     * Display config setting particle effect above an acceptable NPC one time
     *
     * @param quester Target quester to let view the effect
     * @param targetUuid Target NPC UUID to place the effect above
     * @param targetLocation Target NPC location to place the effect above
     */
    public void showConfigEffect(final BukkitQuester quester, final UUID targetUuid, final Location targetLocation) {
        if (targetUuid != null) {
            targetLocation.add(0, 2, 0);
            final BukkitQuesterPostViewEffectEvent event;
            if (quester.canAcceptQuest(targetUuid)) {
                showEffect(quester.getPlayer(), targetLocation, plugin.getConfigSettings().getEffect());

                event = new BukkitQuesterPostViewEffectEvent(quester, targetUuid, targetLocation,
                        plugin.getConfigSettings().getEffect(), false);
                plugin.getServer().getPluginManager().callEvent(event);
            } else if (quester.canAcceptCompletedRedoableQuest(targetUuid)) {
                showEffect(quester.getPlayer(), targetLocation, plugin.getConfigSettings().getRedoEffect());

                event = new BukkitQuesterPostViewEffectEvent(quester, targetUuid, targetLocation,
                        plugin.getConfigSettings().getRedoEffect(), true);
                plugin.getServer().getPluginManager().callEvent(event);
            }
        }
    }

    /**
     * Display specified particle effect at a location one time
     *
     * @param player Target player to let view the effect
     * @param location Target location to place the effect at
     * @param effectType Value of {@link org.bukkit.Particle} or {@link BukkitPreBuiltParticle}
     */
    public void showEffect(final Player player, final Location location, final String effectType) {
        if (player == null || location == null) {
            return;
        }
        try {
            BukkitParticleProvider.sendToPlayer(player, location, effectType.toUpperCase());
        } catch (final NoClassDefFoundError e) {
            // Unsupported Minecraft version
        }
    }
}
