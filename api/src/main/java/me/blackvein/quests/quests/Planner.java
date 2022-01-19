package me.blackvein.quests.quests;

public interface Planner {
    String getStart();
    long getStartInMillis();
    boolean hasStart();
    void setStart(final String start);
    String getEnd();
    long getEndInMillis();
    boolean hasEnd();
    void setEnd(final String end);
    long getRepeat();
    boolean hasRepeat();
    void setRepeat(final long repeat);
    long getCooldown();
    boolean hasCooldown();
    void setCooldown(final long cooldown);
    boolean getOverride();
    void setOverride(final boolean override);
}
