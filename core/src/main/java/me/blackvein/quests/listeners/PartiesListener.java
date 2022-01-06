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

package me.blackvein.quests.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.alessiodp.parties.api.events.bukkit.party.BukkitPartiesPartyPostCreateEvent;
import com.alessiodp.parties.api.events.bukkit.player.BukkitPartiesPlayerPostJoinEvent;
import com.alessiodp.parties.api.events.bukkit.player.BukkitPartiesPlayerPostLeaveEvent;

import me.blackvein.quests.util.Lang;

public class PartiesListener implements Listener {

    @EventHandler
    public void onPartyCreate(final BukkitPartiesPartyPostCreateEvent event) {
        if (event.getCreator() != null) {
            final Player p = Bukkit.getServer().getPlayer(event.getCreator().getPlayerUUID());
            if (p != null) {
                if (Lang.get("questPartiesCreate").length() > 0) {
                    Lang.send(p, ChatColor.YELLOW + Lang.get("questPartiesCreate"));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoinEvent(final BukkitPartiesPlayerPostJoinEvent event) {
        final Player p = Bukkit.getServer().getPlayer(event.getPartyPlayer().getPlayerUUID());
        if (p != null && Lang.get("questPartiesLeave").length() > 0) {
            Lang.send(p, ChatColor.GREEN + Lang.get(p, "questPartiesJoin"));
            event.getParty().broadcastMessage(ChatColor.GREEN + Lang.get("questPartiesJoinBroadcast").replace("<player>", event.getPartyPlayer().getName()), event.getPartyPlayer());
        }
    }

    @EventHandler
    public void onPlayerLeaveEvent(final BukkitPartiesPlayerPostLeaveEvent event) {
        final Player p = Bukkit.getServer().getPlayer(event.getPartyPlayer().getPlayerUUID());
        if (p != null && Lang.get("questPartiesLeave").length() > 0) {
            Lang.send(p, ChatColor.RED + Lang.get(p, "questPartiesLeave"));
            event.getParty().broadcastMessage(ChatColor.RED + Lang.get("questPartiesLeaveBroadcast").replace("<player>", event.getPartyPlayer().getName()), event.getPartyPlayer());
        }
    }
}
