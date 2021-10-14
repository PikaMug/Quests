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

import me.blackvein.quests.util.Lang;
import me.pikamug.unite.api.events.PartyCreateEvent;
import me.pikamug.unite.api.events.PartyDeleteEvent;
import me.pikamug.unite.api.events.PartyJoinEvent;
import me.pikamug.unite.api.events.PartyLeaveEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class UniteListener implements Listener {

    @EventHandler
    public void onPartyCreate(final PartyCreateEvent event) {
        if (event.getCreator() != null) {
            final Player p = Bukkit.getServer().getPlayer(event.getCreator());
            if (p != null) {
                if (Lang.get("questPartiesCreate").length() > 0) {
                    Lang.send(p, ChatColor.YELLOW + Lang.get("questPartiesCreate"));
                }
            }
        }
    }
    
    @EventHandler
    public void onPartyDelete(final PartyDeleteEvent event) {
        if (event.getDisbander() != null) {
            final Player p = Bukkit.getServer().getPlayer(event.getDisbander());
            if (p != null) {
                if (Lang.get("questDungeonsDisband").length() > 0) {
                    Lang.send(p, ChatColor.RED + Lang.get("questDungeonsDisband"));
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerJoinEvent(final PartyJoinEvent event) {
        final Player p = Bukkit.getServer().getPlayer(event.getPlayer());
        if (p != null && Lang.get("questPartiesLeave").length() > 0) {
            Lang.send(p, ChatColor.GREEN + Lang.get(p, "questPartiesJoin"));
            if (Lang.get("questPartiesJoinBroadcast").length() > 0) {
                for (final Player online : Bukkit.getOnlinePlayers()) {
                    if (event.getPlayer().equals(online.getUniqueId())) {
                        continue;
                    }
                    if (event.getPartyProvider().areInSameParty(p, online)) {
                        online.sendMessage(ChatColor.GREEN + Lang.get("questPartiesJoinBroadcast").replace("<player>", p.getName()));
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerLeaveEvent(final PartyLeaveEvent event) {
        final Player p = Bukkit.getServer().getPlayer(event.getPlayer());
        if (p != null && Lang.get("questPartiesLeave").length() > 0) {
            Lang.send(p, ChatColor.RED + Lang.get(p, "questPartiesLeave"));
            if (Lang.get("questPartiesLeaveBroadcast").length() > 0) {
                for (final Player online : Bukkit.getOnlinePlayers()) {
                    if (event.getPlayer().equals(online.getUniqueId())) {
                        continue;
                    }
                    if (event.getPartyProvider().areInSameParty(p, online)) {
                        online.sendMessage(ChatColor.RED + Lang.get("questPartiesLeaveBroadcast").replace("<player>", p.getName()));
                    }
                }
            }
        }
    }
}
