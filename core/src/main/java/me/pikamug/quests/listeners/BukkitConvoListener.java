package me.pikamug.quests.listeners;

import me.pikamug.quests.util.BukkitLang;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BukkitConvoListener implements ConversationAbandonedListener {

    @Override
    public void conversationAbandoned(@NotNull final ConversationAbandonedEvent abandonedEvent) {
        if (!abandonedEvent.gracefulExit()) {
            try {
                abandonedEvent.getContext().getForWhom().sendRawMessage(ChatColor.YELLOW
                        + BukkitLang.get((Player) abandonedEvent.getContext().getForWhom(), "questTimeout"));
            } catch (final Exception e) {
                // Do nothing
            }
        }
    }
}
