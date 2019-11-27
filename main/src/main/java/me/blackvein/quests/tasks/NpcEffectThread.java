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

package me.blackvein.quests.tasks;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;
import me.blackvein.quests.particle.ParticleProvider;
import net.citizensnpcs.api.npc.NPC;

public class NpcEffectThread implements Runnable {

    final Quests plugin;

    public NpcEffectThread(Quests quests) {
        plugin = quests;
    }

    @Override
    public void run() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            Quester quester = plugin.getQuester(player.getUniqueId());
            List<Entity> nearby = player.getNearbyEntities(32.0, 32.0, 32.0);
            if (nearby.isEmpty() == false) {
                for (Entity e : nearby) {
                    if (plugin.getDependencies().getCitizens() != null 
                            && plugin.getDependencies().getCitizens().getNPCRegistry() != null) {
                        if (plugin.getDependencies().getCitizens().getNPCRegistry().isNPC(e)) {
                            NPC npc = plugin.getDependencies().getCitizens().getNPCRegistry().getNPC(e);
                            if (plugin.hasQuest(npc, quester)) {
                                showEffect(player, npc, plugin.getSettings().getEffect());
                            } else if (plugin.hasCompletedRedoableQuest(npc, quester)) {
                                showEffect(player, npc, plugin.getSettings().getRedoEffect());
                            }
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
     */
    public void showEffect(Player player, NPC npc, String effectType) {
        if (player == null || npc == null) {
            return;
        }
        if (plugin.getDependencies().getCitizens() != null) {
            Location eyeLoc = npc.getEntity().getLocation();
            eyeLoc.setY(eyeLoc.getY() + 1.5);
            ParticleProvider.sendToPlayer(player, eyeLoc, effectType);
        }
    }
}
