package me.blackvein.quests.quests;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface QuestFactory {
    Map<UUID, Block> getSelectedBlockStarts();

    void setSelectedBlockStarts(final Map<UUID, Block> selectedBlockStarts);

    Map<UUID, Block> getSelectedKillLocations();

    void setSelectedKillLocations(final Map<UUID, Block> selectedKillLocations);

    Map<UUID, Block> getSelectedReachLocations();

    void setSelectedReachLocations(final Map<UUID, Block> selectedReachLocations);

    Set<UUID> getSelectingNpcs();

    void setSelectingNpcs(final Set<UUID> selectingNpcs);

    List<String> getNamesOfQuestsBeingEdited();

    void setNamesOfQuestsBeingEdited(final List<String> questNames);

    ConversationFactory getConversationFactory();

    Prompt returnToMenu(final ConversationContext context);

    void loadQuest(final ConversationContext context, final IQuest q);

    void deleteQuest(final ConversationContext context);

    void saveQuest(final ConversationContext context, final ConfigurationSection section);

    /*void saveRequirements(final ConversationContext context, final ConfigurationSection section);

    void saveStages(final ConversationContext context, final ConfigurationSection section);

    void saveRewards(final ConversationContext context, final ConfigurationSection section);

    void savePlanner(final ConversationContext context, final ConfigurationSection section);

    void saveOptions(final ConversationContext context, final ConfigurationSection section);*/
}
