package me.blackvein.quests.listeners;

import me.blackvein.quests.QuestsBungee;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class BungeePlayerListener implements Listener {
    private static final String CHANNEL = "quests:update";

    private final QuestsBungee plugin;

    public BungeePlayerListener(QuestsBungee plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerLoginOrSwitch(ServerSwitchEvent evt) {
        if (evt.getFrom() != null) {
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            final DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
            try {
                dataOutputStream.writeUTF("SaveData:" + evt.getPlayer().getUniqueId());
            } catch (IOException e) {
                e.printStackTrace();
            }
            dispatchMessage(byteArrayOutputStream.toByteArray());
        }

        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        try {
            dataOutputStream.writeUTF("LoadData:" + evt.getPlayer().getUniqueId());
        } catch (IOException e) {
            e.printStackTrace();
        }
        dispatchMessage(byteArrayOutputStream.toByteArray());
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
