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

package me.blackvein.quests.dependencies;

import me.blackvein.quests.Quests;
import me.blackvein.quests.player.IQuester;
import net.citizensnpcs.api.npc.NPC;

import java.util.UUID;

public class DenizenTrigger {
    private final Quests plugin;
    
    public DenizenTrigger(final Quests plugin) {
        this.plugin = plugin;
    }

    public boolean runDenizenScript(final String scriptName, final IQuester quester, final UUID uuid) {
        if (scriptName == null) {
            return false;
        }
        if (plugin.getDependencies().getDenizenApi().containsScript(scriptName)) {
            if (plugin.getDependencies().getCitizens() != null) {
                if (uuid == null) {
                    plugin.getLogger().severe("NPC UUID was null for Denizen script named " + scriptName);
                    return false;
                }
                final NPC npc = plugin.getDependencies().getCitizens().getNPCRegistry().getByUniqueId(uuid);
                plugin.getDependencies().getDenizenApi().runTaskScript(scriptName, quester.getPlayer(), npc);
            } else {
                plugin.getDependencies().getDenizenApi().runTaskScript(scriptName, quester.getPlayer(), null);
            }
        }
        return true;
    }
}
