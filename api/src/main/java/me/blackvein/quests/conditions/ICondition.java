package me.blackvein.quests.conditions;

import me.blackvein.quests.quests.IQuest;
import me.blackvein.quests.player.IQuester;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;

public interface ICondition extends Comparable<ICondition> {
    String getName();

    void setName(final String name);

    boolean isFailQuest();

    void setFailQuest(final boolean failQuest);

    LinkedList<String> getEntitiesWhileRiding();

    void setEntitiesWhileRiding(final LinkedList<String> entitiesWhileRiding);

    LinkedList<Integer> getNpcsWhileRiding();

    void setNpcsWhileRiding(final LinkedList<Integer> npcsWhileRiding);

    LinkedList<String> getPermissions();

    void setPermissions(final LinkedList<String> permissions);

    LinkedList<ItemStack> getItemsWhileHoldingMainHand();

    void setItemsWhileHoldingMainHand(final LinkedList<ItemStack> itemsWhileHoldingMainHand);

    LinkedList<String> getWorldsWhileStayingWithin();

    void setWorldsWhileStayingWithin(final LinkedList<String> worldsWhileStayingWithin);

    LinkedList<String> getBiomesWhileStayingWithin();

    void setBiomesWhileStayingWithin(final LinkedList<String> biomesWhileStayingWithin);

    LinkedList<String> getRegionsWhileStayingWithin();

    void setRegionsWhileStayingWithin(final LinkedList<String> biomesWhileStayingWithin);

    LinkedList<String> getPlaceholdersCheckIdentifier();

    void setPlaceholdersCheckIdentifier(final LinkedList<String> placeholdersCheckIdentifier);

    LinkedList<String> getPlaceholdersCheckValue();

    void setPlaceholdersCheckValue(final LinkedList<String> placeholdersCheckValue);

    boolean check(final IQuester quester, final IQuest quest);
}
