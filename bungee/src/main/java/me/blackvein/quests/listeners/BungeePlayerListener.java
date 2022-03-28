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

import me.blackvein.quests.QuestsBungee;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class BungeePlayerListener implements Listener {
    private static final String CHANNEL = "quests:update";

    private final QuestsBungee plugin;

    public BungeePlayerListener(QuestsBungee plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerLoginOrSwitch(ServerSwitchEvent evt) {
        ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            final DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
            try {
                dataOutputStream.writeUTF("LoadData:" + evt.getPlayer().getUniqueId());
            } catch (IOException e) {
                e.printStackTrace();
            }
            dispatchMessage(byteArrayOutputStream.toByteArray());
        }, 2, TimeUnit.SECONDS);
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent evt) {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        try {
            dataOutputStream.writeUTF("SaveData:" + evt.getPlayer().getUniqueId());
        } catch (IOException e) {
            e.printStackTrace();
        }
        dispatchMessage(byteArrayOutputStream.toByteArray());
    }

    private void dispatchMessage(byte[] message) {
        for (ServerInfo server : plugin.getProxy().getServers().values()) {
            server.sendData(CHANNEL, message, false);
        }
    }
}
