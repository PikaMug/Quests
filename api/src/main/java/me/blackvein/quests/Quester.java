package me.blackvein.quests;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public interface Quester {
    public UUID getUUID();

    public void setUUID(final UUID id);

    public String getQuestIdToTake();

    public void setQuestIdToTake(final String questIdToTake);

    public String getLastKnownName();

    public void setLastKnownName(final String lastKnownName);

    public int getQuestPoints();

    public void setQuestPoints(final int questPoints);

    /**
     * Get compass target quest. Returns null if not set
     *
     * @return Quest or null
     */
    public Quest getCompassTarget();

    /**
     * Set compass target quest. Does not update in-game
     *
     * @param quest The target quest
     */
    public void setCompassTarget(final Quest quest);

    public ConcurrentHashMap<Integer, Quest> getTimers();

    public void setTimers(final ConcurrentHashMap<Integer, Quest> timers);

    public void removeTimer(final Integer timerId);

    public ConcurrentHashMap<Quest, Integer> getCurrentQuests();

    public void setCurrentQuests(final ConcurrentHashMap<Quest, Integer> currentQuests);

    public ConcurrentSkipListSet<Quest> getCompletedQuests();

    public void setCompletedQuests(final ConcurrentSkipListSet<Quest> completedQuests);

    public ConcurrentHashMap<Quest, Long> getCompletedTimes();

    public void setCompletedTimes(final ConcurrentHashMap<Quest, Long> completedTimes);

    public ConcurrentHashMap<Quest, Integer> getAmountsCompleted();

    public void setAmountsCompleted(final ConcurrentHashMap<Quest, Integer> amountsCompleted);

    public ConcurrentHashMap<Quest, QuestData> getQuestData();

    public void setQuestData(final ConcurrentHashMap<Quest, QuestData> questData);
}
