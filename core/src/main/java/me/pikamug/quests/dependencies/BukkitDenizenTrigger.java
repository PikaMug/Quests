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

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.player.Quester;
import net.citizensnpcs.api.npc.NPC;

import java.util.UUID;

public class BukkitDenizenTrigger {
    private final BukkitQuestsPlugin plugin;
    
    public BukkitDenizenTrigger(final BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean runDenizenScript(final String scriptName, final Quester quester, final UUID uuid) {
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
