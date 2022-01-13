package me.blackvein.quests;

import me.blackvein.quests.actions.Action;
import me.blackvein.quests.actions.ActionFactory;
import me.blackvein.quests.conditions.Condition;
import me.blackvein.quests.conditions.ConditionFactory;
import org.bukkit.command.CommandExecutor;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public interface QuestsAPI extends Plugin {

    boolean isLoading();

    String getDetectedServerSoftwareVersion();

    Dependencies getDependencies();

    Settings getSettings();

    List<CustomObjective> getCustomObjectives();

    List<CustomReward> getCustomRewards();

    List<CustomRequirement> getCustomRequirements();

    Collection<Quest> getLoadedQuests();

    Collection<Action> getLoadedActions();

    Collection<Condition> getLoadedConditions();

    Quester getQuester(final UUID id);

    Collection<Quester> getOnlineQuesters();

    Collection<Quester> getOfflineQuesters();

    void setOfflineQuesters(final Collection<Quester> questers);

    LinkedList<Integer> getQuestNpcIds();

    void setQuestNpcIds(final LinkedList<Integer> questNpcIds);

    CommandExecutor getCommandExecutor();

    ConversationFactory getConversationFactory();

    ConversationFactory getNpcConversationFactory();

    QuestFactory getQuestFactory();

    ActionFactory getActionFactory();

    ConditionFactory getConditionFactory();

    /*ConvoListener getConvoListener();

    BlockListener getBlockListener();

    ItemListener getItemListener();

    NpcListener getNpcListener();

    PlayerListener getPlayerListener();

    UniteListener getUniteListener();

    NpcEffectThread getNpcEffectThread();

    PlayerMoveThread getPlayerMoveThread();

    PartiesListener getPartiesListener();

    DenizenTrigger getDenizenTrigger();

    LocaleManager getLocaleManager();

    Storage getStorage();*/
}
