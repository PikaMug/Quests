package me.pikamug.quests.nms;

import org.bukkit.entity.Player;

class BukkitTitleProvider_Modern extends BukkitTitleProvider {

    @SuppressWarnings("deprecation")
    @Override
    void sendTitlePacket(final Player player, final String title, final String subtitle) {
        player.sendTitle(title, subtitle);
    }
}
