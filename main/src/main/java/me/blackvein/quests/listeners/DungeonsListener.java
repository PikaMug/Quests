/*******************************************************************************************************
 * Copyright (c) 2014 PikaMug and contributors. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

package me.blackvein.quests.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import de.erethon.dungeonsxl.api.event.group.GroupCreateEvent;
import de.erethon.dungeonsxl.api.event.group.GroupDisbandEvent;
import de.erethon.dungeonsxl.api.event.group.GroupPlayerJoinEvent;
import de.erethon.dungeonsxl.api.event.group.GroupPlayerLeaveEvent;
import me.blackvein.quests.util.Lang;

public class DungeonsListener implements Listener {
    
    @EventHandler
    public void onGroupCreate(final GroupCreateEvent event) {
        if (Lang.get("questDungeonsCreate").length() > 0) {
            final Player p = event.getCreator().getPlayer();
            Lang.send(p, ChatColor.YELLOW + Lang.get("questDungeonsCreate"));
        }
    }
    
    @EventHandler
    public void onGroupDisbandEvent(final GroupDisbandEvent event) {
        if (Lang.get("questDungeonsDisband").length() > 0) {
            final Player p = event.getDisbander().getPlayer();
            Lang.send(p, ChatColor.RED + Lang.get("questDungeonsDisband"));
        }
    }
    
    @EventHandler
    public void onPlayerJoinEvent(final GroupPlayerJoinEvent event) {
        if (event.getGroup() != null && event.getPlayer() != null) {
            final Player i = event.getGroup().getLeader();
            final Player p = event.getPlayer().getPlayer();
            if (i != null && p != null) {
                if (Lang.get("questDungeonsInvite").length() > 0) {
                    Lang.send(i, ChatColor.GREEN + Lang.get(i, "questDungeonsInvite")
                            .replace("<player>", p.getName()));
                }
                if (Lang.get("questDungeonsJoin").length() > 0) {
                    Lang.send(p, ChatColor.GREEN + Lang.get(p, "questDungeonsJoin").replace("<player>", i.getName()));
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerLeaveEvent(final GroupPlayerLeaveEvent event) {
        if (event.getGroup() != null && event.getPlayer() != null) {
            final Player k = event.getGroup().getLeader();
            final Player p = event.getPlayer().getPlayer();
            if (k != null && p != null) {
                if (Lang.get("questDungeonsKicked").length() > 0) {
                    Lang.send(k, ChatColor.RED + Lang.get(k, "questDungeonsKicked").replace("<player>", k.getName()));
                }
                if (Lang.get("questDungeonsLeave").length() > 0) {
                    Lang.send(p, ChatColor.RED + Lang.get(p, "questDungeonsLeave").replace("<player>", p.getName()));
                }
            }
        }
    }
}
