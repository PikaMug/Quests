package me.blackvein.quests;

import me.blackvein.quests.listeners.BungeePlayerListener;
import net.md_5.bungee.api.plugin.Plugin;

public class QuestsBungee extends Plugin {
    private static final String CHANNEL = "quests:update";

    private BungeePlayerListener playerListener;

    @Override
    public void onEnable() {
        playerListener = new BungeePlayerListener(this);
        getProxy().registerChannel(CHANNEL);
        getProxy().getPluginManager().registerListener(this, playerListener);
    }

    @Override
    public void onDisable() {
        getProxy().unregisterChannel(CHANNEL);
        getProxy().getPluginManager().unregisterListener(playerListener);
    }
}
