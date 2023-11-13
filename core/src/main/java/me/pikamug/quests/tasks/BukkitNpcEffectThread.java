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

import lol.pyr.znpcsplus.api.npc.Npc;
import lol.pyr.znpcsplus.api.npc.NpcEntry;
import me.pikamug.quests.enums.BukkitPreBuiltParticle;
import me.pikamug.quests.player.BukkitQuester;
import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.events.quester.BukkitQuesterPostViewEffectEvent;
import me.pikamug.quests.nms.BukkitParticleProvider;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class BukkitNpcEffectThread implements Runnable {

    final BukkitQuestsPlugin plugin;

    public BukkitNpcEffectThread(final BukkitQuestsPlugin quests) {
        plugin = quests;
    }

    @Override
    public void run() {
        for (final Player player : plugin.getServer().getOnlinePlayers()) {
            if (plugin.getDependencies().getCitizens() != null) {
                final List<Entity> nearby = player.getNearbyEntities(32.0, 16.0, 32.0);
                if (!nearby.isEmpty()) {
                    for (final Entity entity : nearby) {
                        showConfigEffect(plugin.getQuester(player.getUniqueId()), entity);
                    }
                }
            }
            if (plugin.getDependencies().getZnpcsPlus() != null) {
                for (io.github.znetworkw.znpcservers.npc.NPC npc : io.github.znetworkw.znpcservers.npc.NPC.all()) {
                    if (npc.getLocation().getWorld() == null || player.getLocation().getWorld() == null) {
                        return;
                    }
                    if (npc.getLocation().getWorld().getName().equals(player.getLocation().getWorld().getName())) {
                        if (npc.getLocation().distanceSquared(player.getLocation()) < 24) {
                            showConfigEffect(plugin.getQuester(player.getUniqueId()), (Entity) npc.getBukkitEntity());
                        }
                    }
                }
            }
            if (plugin.getDependencies().getZnpcsPlusApi() != null) {
                Collection<? extends NpcEntry> znpcs = plugin.getDependencies().getZnpcsPlusApi().getNpcRegistry().getAllPlayerMade();
                for (NpcEntry npc : znpcs) {
                    if (npc.getNpc().getWorld() == null || player.getLocation().getWorld() == null) {
                        return;
                    }
                    if (npc.getNpc().getWorld().equals(player.getLocation().getWorld())) {
                        if (npc.getNpc().getLocation().toBukkitLocation(npc.getNpc().getWorld())
                                .distanceSquared(player.getLocation()) < 24) {
                            showConfigEffect(plugin.getQuester(player.getUniqueId()), npc.getNpc());
                        }
                    }
                }
            }
        }
    }

    /**
     * Display config setting particle effect above an entity one time
     * @param quester Target quester to let view the effect
     * @param entity Target entity to place the effect above
     */
    public void showConfigEffect(final BukkitQuester quester, final Entity entity) {
        UUID uuid = plugin.getDependencies().getUuidFromNpc(entity);
        if (uuid != null) {
            final BukkitQuesterPostViewEffectEvent event;
            if (quester.canAcceptQuest(uuid)) {
                showEffect(quester.getPlayer(), entity, plugin.getConfigSettings().getEffect());

                event = new BukkitQuesterPostViewEffectEvent(quester, entity,
                        plugin.getConfigSettings().getEffect(), false);
                plugin.getServer().getPluginManager().callEvent(event);
            } else if (quester.canAcceptCompletedRedoableQuest(uuid)) {
                showEffect(quester.getPlayer(), entity, plugin.getConfigSettings().getRedoEffect());

                event = new BukkitQuesterPostViewEffectEvent(quester, entity,
                        plugin.getConfigSettings().getRedoEffect(), true);
                plugin.getServer().getPluginManager().callEvent(event);
            }
        }
    }

    /**
     * Display config setting particle effect above an {@link Npc} one time
     * @param quester Target quester to let view the effect
     * @param npc Target NPC to place the effect above
     */
    public void showConfigEffect(final BukkitQuester quester, final Npc npc) {
        if (npc == null) return;
        final BukkitQuesterPostViewEffectEvent event;
        if (quester.canAcceptQuest(npc.getUuid())) {
            if (npc.getWorld() == null) return;
            showEffect(quester.getPlayer(), npc.getLocation().toBukkitLocation(npc.getWorld()).add(0, 2, 0), plugin.getConfigSettings().getEffect());

            event = new BukkitQuesterPostViewEffectEvent(quester, npc,
                    plugin.getConfigSettings().getEffect(), false);
            plugin.getServer().getPluginManager().callEvent(event);
        } else if (quester.canAcceptCompletedRedoableQuest(npc.getUuid())) {
            if (npc.getWorld() == null) return;
            showEffect(quester.getPlayer(), npc.getLocation().toBukkitLocation(npc.getWorld()).add(0, 2, 0), plugin.getConfigSettings().getRedoEffect());

            event = new BukkitQuesterPostViewEffectEvent(quester, npc,
                    plugin.getConfigSettings().getRedoEffect(), true);
            plugin.getServer().getPluginManager().callEvent(event);
        }
    }
    
    /**
     * Display specified particle effect above a Citizens NPC one time
     * @param player Target player to let view the effect
     * @param npc Target NPC to place the effect above
     * @param effectType Value of EnumParticle such as NOTE or SMOKE
     * @deprecated Use {@link #showEffect(Player, Entity, String)}
     */
    public void showEffect(final Player player, final NPC npc, final String effectType) {
        if (player == null || npc == null || npc.getEntity() == null) {
            return;
        }
        if (plugin.getDependencies().getCitizens() != null) {
            final Location eyeLoc = npc.getEntity().getLocation();
            eyeLoc.setY(eyeLoc.getY() + 2);
            try {
                BukkitParticleProvider.sendToPlayer(player, eyeLoc, effectType.toUpperCase());
            } catch (final NoClassDefFoundError e) {
                // Unsupported Minecraft version
            }
        }
    }

    /**
     * Display specified particle effect above an entity one time
     * @param player Target player to let view the effect
     * @param entity Target entity to place the effect above
     * @param effectType Value of {@link org.bukkit.Particle} or {@link BukkitPreBuiltParticle}
     */
    public void showEffect(final Player player, final Entity entity, final String effectType) {
        showEffect(player, entity.getLocation().add(0, 2, 0), effectType);
    }

    /**
     * Display specified particle effect at a location one time
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
