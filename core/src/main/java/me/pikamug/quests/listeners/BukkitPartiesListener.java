/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.alessiodp.parties.api.events.bukkit.party.BukkitPartiesPartyPostCreateEvent;
import com.alessiodp.parties.api.events.bukkit.player.BukkitPartiesPlayerPostJoinEvent;
import com.alessiodp.parties.api.events.bukkit.player.BukkitPartiesPlayerPostLeaveEvent;

import me.pikamug.quests.util.BukkitLang;

public class BukkitPartiesListener implements Listener {

    @EventHandler
    public void onPartyCreate(final BukkitPartiesPartyPostCreateEvent event) {
        if (event.getCreator() != null) {
            final Player p = Bukkit.getServer().getPlayer(event.getCreator().getPlayerUUID());
            if (p != null) {
                if (BukkitLang.get("questPartiesCreate").length() > 0) {
                    BukkitLang.send(p, ChatColor.YELLOW + BukkitLang.get("questPartiesCreate"));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoinEvent(final BukkitPartiesPlayerPostJoinEvent event) {
        final Player p = Bukkit.getServer().getPlayer(event.getPartyPlayer().getPlayerUUID());
        if (p != null && BukkitLang.get("questPartiesLeave").length() > 0) {
            BukkitLang.send(p, ChatColor.GREEN + BukkitLang.get(p, "questPartiesJoin"));
            event.getParty().broadcastMessage(ChatColor.GREEN + BukkitLang.get("questPartiesJoinBroadcast").replace("<player>", event.getPartyPlayer().getName()), event.getPartyPlayer());
        }
    }

    @EventHandler
    public void onPlayerLeaveEvent(final BukkitPartiesPlayerPostLeaveEvent event) {
        final Player p = Bukkit.getServer().getPlayer(event.getPartyPlayer().getPlayerUUID());
        if (p != null && BukkitLang.get("questPartiesLeave").length() > 0) {
            BukkitLang.send(p, ChatColor.RED + BukkitLang.get(p, "questPartiesLeave"));
            event.getParty().broadcastMessage(ChatColor.RED + BukkitLang.get("questPartiesLeaveBroadcast").replace("<player>", event.getPartyPlayer().getName()), event.getPartyPlayer());
        }
    }
}
