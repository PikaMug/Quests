package me.blackvein.quests.actions;

import org.bukkit.block.Block;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ActionFactory {
    Map<UUID, Block> getSelectedExplosionLocations();

    void setSelectedExplosionLocations(final Map<UUID, Block> selectedExplosionLocations);

    Map<UUID, Block> getSelectedEffectLocations();

    void setSelectedEffectLocations(final Map<UUID, Block> selectedEffectLocations);

    Map<UUID, Block> getSelectedMobLocations();

    void setSelectedMobLocations(final Map<UUID, Block> selectedMobLocations);

    Map<UUID, Block> getSelectedLightningLocations();

    void setSelectedLightningLocations(final Map<UUID, Block> selectedLightningLocations);

    Map<UUID, Block> getSelectedTeleportLocations();

    void setSelectedTeleportLocations(final Map<UUID, Block> selectedTeleportLocations);

    ConversationFactory getConversationFactory();

    List<String> getNamesOfActionsBeingEdited();

    void setNamesOfActionsBeingEdited(final List<String> actionNames);

    Prompt returnToMenu(final ConversationContext context);

    void loadData(final IAction event, final ConversationContext context);

    void clearData(final ConversationContext context);

    void deleteAction(final ConversationContext context);

    void saveAction(final ConversationContext context);
}
