/*
 * Copyright (c) 2014 PikaMug and contributors. All rights reserved.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package me.blackvein.quests.tasks;

import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;
import me.blackvein.quests.events.quester.QuesterPostViewEffectEvent;
import me.blackvein.quests.nms.ParticleProvider;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class NpcEffectThread implements Runnable {

    final Quests plugin;

    public NpcEffectThread(final Quests quests) {
        plugin = quests;
    }

    @Override
    public void run() {
        for (final Player player : plugin.getServer().getOnlinePlayers()) {
            final List<Entity> nearby = player.getNearbyEntities(32.0, 16.0, 32.0);
            if (!nearby.isEmpty()) {
                final Quester quester = plugin.getQuester(player.getUniqueId());
                for (final Entity entity : nearby) {
                    UUID uuid = plugin.getDependencies().getUUIDFromNPC(entity);
                    if (uuid != null) {
                        final QuesterPostViewEffectEvent event;
                        if (plugin.hasQuest(uuid, quester)) {
                            showEffect(player, entity, plugin.getSettings().getEffect());

                            event = new QuesterPostViewEffectEvent(quester, entity,
                                    plugin.getSettings().getEffect(), false);
                            plugin.getServer().getPluginManager().callEvent(event);
                        } else if (plugin.hasCompletedRedoableQuest(uuid, quester)) {
                            showEffect(player, entity, plugin.getSettings().getRedoEffect());

                            event = new QuesterPostViewEffectEvent(quester, entity,
                                    plugin.getSettings().getEffect(), true);
                            plugin.getServer().getPluginManager().callEvent(event);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Display a particle effect above a Citizens NPC one time
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
                ParticleProvider.sendToPlayer(player, eyeLoc, effectType.toUpperCase());
            } catch (final NoClassDefFoundError e) {
                // Unsupported Minecraft version
            }
        }
    }

    /**
     * Display a particle effect above an entity one time
     * @param player Target player to let view the effect
     * @param entity Target entity to place the effect above
     * @param effectType Value of {@link org.bukkit.Particle} or {@link me.blackvein.quests.nms.PreBuiltParticle}
     */
    public void showEffect(final Player player, final Entity entity, final String effectType) {
        if (player == null || entity == null) {
            return;
        }
        final Location eyeLoc = entity.getLocation();
        eyeLoc.setY(eyeLoc.getY() + 2);
        try {
            ParticleProvider.sendToPlayer(player, eyeLoc, effectType.toUpperCase());
        } catch (final NoClassDefFoundError e) {
            // Unsupported Minecraft version
        }
    }
}
