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

import org.bukkit.entity.Player;

import me.pikamug.quests.BukkitQuestsPlugin;
import net.citizensnpcs.api.CitizensAPI;

public class BukkitPlayerMoveThread implements Runnable {

    final BukkitQuestsPlugin plugin;

    public BukkitPlayerMoveThread(final BukkitQuestsPlugin quests) {
        plugin = quests;
    }
    
    @Override
    public void run() {
        for (final Player player : plugin.getServer().getOnlinePlayers()) {
            if (plugin.getDependencies().getCitizens() != null) {
                if (CitizensAPI.getNPCRegistry().isNPC(player)) {
                    return;
                }
            }
            plugin.getPlayerListener().playerMove(player.getUniqueId(), player.getLocation());
        }
    }
}
