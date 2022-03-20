package me.blackvein.quests.listeners;

import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

public class BungeeListener implements PluginMessageListener {
    private static final String CHANNEL = "quests:update";

    private final Quests plugin;

    public BungeeListener(final Quests plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] bytes) {
        if (!channel.equalsIgnoreCase(CHANNEL) ) {
            return;
        }

        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        final DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);
        try {
            final String input = dataInputStream.readUTF();
            final UUID uuid = UUID.fromString(input.substring(input.lastIndexOf(":") + 1));
            if (input.startsWith("SaveData:")) {
                final Quester quester = plugin.getQuester(uuid);
                if (quester != null) {
                    plugin.getLogger().info(ChatColor.GREEN + "[Bungee] Saved quester data for UUID " + uuid);
                    quester.saveData();
                }
            } else if (input.startsWith("LoadData:")) {
                final Quester quester = plugin.getQuester(uuid);
                if (quester != null) {
                    plugin.getLogger().info(ChatColor.GREEN + "[Bungee] Loaded quester data for UUID " + uuid);
                    quester.hasData();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
