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

package me.blackvein.quests.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import de.erethon.dungeonsxl.event.dgroup.DGroupCreateEvent;
import de.erethon.dungeonsxl.event.dgroup.DGroupDisbandEvent;
import de.erethon.dungeonsxl.event.dplayer.DPlayerJoinDGroupEvent;
import de.erethon.dungeonsxl.event.dplayer.DPlayerLeaveDGroupEvent;

import me.blackvein.quests.util.Lang;

public class DungeonsListener implements Listener {
    
    @EventHandler
    public void onGroupCreate(DGroupCreateEvent event) {
        if (Lang.get("questDungeonsCreate").length() > 0) {
            event.getCreator().sendMessage(ChatColor.YELLOW + Lang.get("questDungeonsCreate"));
        }
    }
    
    @EventHandler
    public void onGroupDisbandEvent(DGroupDisbandEvent event) {
        if (Lang.get("questDungeonsDisband").length() > 0) {
            event.getDisbander().sendMessage(ChatColor.RED + Lang.get("questDungeonsDisband"));
        }
    }
    
    @EventHandler
    public void onPlayerJoinEvent(DPlayerJoinDGroupEvent event) {
        if (event.getDGroup() != null && event.getDPlayer() != null) {
            Player i = event.getDGroup().getLeader();
            Player p = event.getDPlayer().getPlayer();
            if (i != null && p != null) {
                if (Lang.get("questDungeonsInvite").length() > 0) {
                    i.sendMessage(ChatColor.GREEN + Lang.get(i, "questDungeonsInvite")
                            .replace("<player>", p.getName()));
                }
                if (Lang.get("questDungeonsJoin").length() > 0) {
                    p.sendMessage(ChatColor.GREEN + Lang.get(p, "questDungeonsJoin").replace("<player>", i.getName()));
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerLeaveEvent(DPlayerLeaveDGroupEvent event) {
        if (event.getDGroup() != null && event.getDPlayer() != null) {
            Player k = event.getDGroup().getLeader();
            Player p = event.getDPlayer().getPlayer();
            if (k != null && p != null) {
                if (Lang.get("questDungeonsKicked").length() > 0) {
                    k.sendMessage(ChatColor.RED + Lang.get(k, "questDungeonsKicked").replace("<player>", k.getName()));
                }
                if (Lang.get("questDungeonsLeave").length() > 0) {
                    p.sendMessage(ChatColor.RED + Lang.get(p, "questDungeonsLeave").replace("<player>", p.getName()));
                }
            }
        }
    }
}
