package main.java.me.blackvein.quests;

import me.ThaH3lper.com.Api.BossDeathEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;


public class EpicBossListener implements Listener{

    final Quests plugin;

    public EpicBossListener(Quests quests){

        plugin = quests;

    }

    @EventHandler
    public void onBossDeath(BossDeathEvent evt){

        String boss = evt.getBossName();
        Player player = evt.getPlayer();
        Quester quester = plugin.getQuester(player.getName());
        if(quester.hasObjective("killBoss")){
            quester.killBoss(boss);
        }


    }

}
