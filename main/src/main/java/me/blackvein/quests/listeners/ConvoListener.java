package me.blackvein.quests.listeners;

import me.blackvein.quests.util.Lang;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ConvoListener implements ConversationAbandonedListener {

    @Override
    public void conversationAbandoned(@NotNull final ConversationAbandonedEvent abandonedEvent) {
        if (!abandonedEvent.gracefulExit()) {
            try {
                abandonedEvent.getContext().getForWhom().sendRawMessage(ChatColor.YELLOW
                        + Lang.get((Player) abandonedEvent.getContext().getForWhom(), "questTimeout"));
            } catch (final Exception e) {
                // Do nothing
            }
        }
    }
}
