package me.blackvein.quests.quests;

import me.blackvein.quests.quests.IQuest;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public interface Requirements {
    int getMoney();

    void setMoney(final int money);

    int getQuestPoints();

    void setQuestPoints(final int questPoints);

    List<ItemStack> getItems();

    void setItems(final List<ItemStack> items);

    List<Boolean> getRemoveItems();

    void setRemoveItems(final List<Boolean> removeItems);

    List<IQuest> getNeededQuests();

    void setNeededQuests(final List<IQuest> neededQuests);

    List<IQuest> getBlockQuests();

    void setBlockQuests(final List<IQuest> blockQuests);

    List<String> getPermissions();

    void setPermissions(final List<String> permissions);

    List<String> getMcmmoSkills();

    void setMcmmoSkills(final List<String> mcmmoSkills);

    List<Integer> getMcmmoAmounts();

    void setMcmmoAmounts(final List<Integer> mcmmoAmounts);

    String getHeroesPrimaryClass();

    void setHeroesPrimaryClass(final String heroesPrimaryClass);

    String getHeroesSecondaryClass();

    void setHeroesSecondaryClass(final String heroesSecondaryClass);

    Map<String, Map<String, Object>> getCustomRequirements();

    void setCustomRequirements(final Map<String, Map<String, Object>> customRequirements);

    List<String> getDetailsOverride();

    void setDetailsOverride(final List<String> detailsOverride);
}
