package me.blackvein.quests.conditions;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;

import java.util.List;

public interface ConditionFactory {
    ConversationFactory getConversationFactory();

    List<String> getNamesOfConditionsBeingEdited();

    void setNamesOfConditionsBeingEdited(final List<String> conditionNames);

    Prompt returnToMenu(final ConversationContext context);

    void loadData(final ICondition condition, final ConversationContext context);

    void clearData(final ConversationContext context);

    void deleteCondition(final ConversationContext context);

    void saveCondition(final ConversationContext context);
}
