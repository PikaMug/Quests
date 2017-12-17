package me.blackvein.quests;

import me.blackvein.quests.util.Lang;
import org.bukkit.scheduler.BukkitRunnable;

public class ObjectiveTimer extends BukkitRunnable {

    Quester quester;
    Quests plugin;
    Quest quest;
    private int time;
    private boolean last;

    ObjectiveTimer(Quests plugin, Quester quester, Quest quest, int time, boolean last) {
        this.quester = quester;
        this.quest = quest;
        this.plugin = plugin;
        this.time = time;
        this.last = last;
    }

    @Override
    public void run() {
        quester.timers.remove(getTaskId());
        if (last) {
            quest.failQuest(quester);
            quester.updateJournal();
        } else {
            quester.getPlayer().sendMessage(Quests.parseString(String.format(Lang.get("timerMessage"), time), quest));
        }
    }
}
