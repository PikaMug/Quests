package me.blackvein.quests.quests;

public interface Options {
    boolean canAllowCommands();

    void setAllowCommands(final boolean allowCommands);

    boolean canAllowQuitting();

    void setAllowQuitting(final boolean allowQuitting);

    boolean canIgnoreSilkTouch();

    void setIgnoreSilkTouch(final boolean ignoreSilkTouch);

    String getExternalPartyPlugin();

    void setExternalPartyPlugin(final String externalPartyPlugin);

    boolean canUsePartiesPlugin();

    void setUsePartiesPlugin(final boolean usePartiesPlugin);

    int getShareProgressLevel();

    void setShareProgressLevel(final int shareProgressLevel);

    boolean canShareSameQuestOnly();

    void setShareSameQuestOnly(final boolean shareSameQuestOnly);

    double getShareDistance();

    void setShareDistance(final double shareDistance);

    boolean canHandleOfflinePlayers();

    void setHandleOfflinePlayers(final boolean handleOfflinePlayers);
}
