/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.listeners.npc;

import io.github.znetworkw.znpcservers.npc.interaction.NPCInteractEvent;
import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.listeners.BukkitNpcListener;
import me.pikamug.quests.dependencies.npc.znpcsplus.legacy.BukkitZnpcsPlusLegacyDependency;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.UUID;

public class BukkitZnpcsPlusLegacyListener extends BukkitNpcListener {
    public BukkitZnpcsPlusLegacyListener(BukkitQuestsPlugin plugin, BukkitZnpcsPlusLegacyDependency npcDependency) {
        super(plugin, npcDependency);
    }

    @EventHandler
    public void onNPCInteract(final NPCInteractEvent event) {
        Player player = event.getPlayer();
        UUID npcUUID = event.getNpc() != null ? event.getNpc().getUUID() : null;

        if (event.isLeftClick()) {
            interactNPC(player, npcUUID, ClickType.LEFT);
        } else if (event.isRightClick()) {
            interactNPC(player, npcUUID, ClickType.RIGHT);
        }
    }
}
