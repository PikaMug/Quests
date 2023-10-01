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

import me.pikamug.quests.util.BukkitLang;
import me.pikamug.unite.api.events.PartyCreateEvent;
import me.pikamug.unite.api.events.PartyDeleteEvent;
import me.pikamug.unite.api.events.PartyJoinEvent;
import me.pikamug.unite.api.events.PartyLeaveEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BukkitUniteListener implements Listener {

    @EventHandler
    public void onPartyCreate(final PartyCreateEvent event) {
        if (event.getCreator() != null) {
            final Player p = Bukkit.getServer().getPlayer(event.getCreator());
            if (p != null) {
                if (BukkitLang.get("questPartiesCreate").length() > 0) {
                    BukkitLang.send(p, ChatColor.YELLOW + BukkitLang.get("questPartiesCreate"));
                }
            }
        }
    }
    
    @EventHandler
    public void onPartyDelete(final PartyDeleteEvent event) {
        if (event.getDisbander() != null) {
            final Player p = Bukkit.getServer().getPlayer(event.getDisbander());
            if (p != null) {
                if (BukkitLang.get("questDungeonsDisband").length() > 0) {
                    BukkitLang.send(p, ChatColor.RED + BukkitLang.get("questDungeonsDisband"));
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerJoinEvent(final PartyJoinEvent event) {
        final Player p = Bukkit.getServer().getPlayer(event.getPlayer());
        if (p != null && BukkitLang.get("questPartiesLeave").length() > 0) {
            BukkitLang.send(p, ChatColor.GREEN + BukkitLang.get(p, "questPartiesJoin"));
            if (BukkitLang.get("questPartiesJoinBroadcast").length() > 0) {
                for (final Player online : Bukkit.getOnlinePlayers()) {
                    if (event.getPlayer().equals(online.getUniqueId())) {
                        continue;
                    }
                    if (event.getPartyProvider().areInSameParty(p, online)) {
                        online.sendMessage(ChatColor.GREEN + BukkitLang.get("questPartiesJoinBroadcast").replace("<player>", p.getName()));
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerLeaveEvent(final PartyLeaveEvent event) {
        final Player p = Bukkit.getServer().getPlayer(event.getPlayer());
        if (p != null && BukkitLang.get("questPartiesLeave").length() > 0) {
            BukkitLang.send(p, ChatColor.RED + BukkitLang.get(p, "questPartiesLeave"));
            if (BukkitLang.get("questPartiesLeaveBroadcast").length() > 0) {
                for (final Player online : Bukkit.getOnlinePlayers()) {
                    if (event.getPlayer().equals(online.getUniqueId())) {
                        continue;
                    }
                    if (event.getPartyProvider().areInSameParty(p, online)) {
                        online.sendMessage(ChatColor.RED + BukkitLang.get("questPartiesLeaveBroadcast").replace("<player>", p.getName()));
                    }
                }
            }
        }
    }
}
