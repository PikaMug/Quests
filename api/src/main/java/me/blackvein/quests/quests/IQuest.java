package me.blackvein.quests.quests;

import me.blackvein.quests.actions.IAction;
import me.blackvein.quests.player.IQuester;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.LinkedList;

public interface IQuest extends Comparable<IQuest> {
    Plugin getPlugin();

    void setPlugin(Plugin plugin);

    String getId();

    void setId(String id);

    String getName();

    void setName(final String name);

    String getDescription();

    void setDescription(final String description);

    String getFinished();

    void setFinished(final String finished);

    String getRegionStart();

    void setRegionStart(final String regionStart);

    ItemStack getGUIDisplay();

    void setGUIDisplay(final ItemStack guiDisplay);

    IStage getStage(final int index);

    LinkedList<IStage> getStages();

    NPC getNpcStart();

    void setNpcStart(final NPC npcStart);

    Location getBlockStart();

    void setBlockStart(final Location blockStart);

    IAction getInitialAction();

    void setInitialAction(final IAction initialAction);

    Requirements getRequirements();

    Planner getPlanner();

    Rewards getRewards();

    Options getOptions();

    void nextStage(final IQuester quester, final boolean allowSharedProgress);

    void setStage(final IQuester quester, final int stage);

    boolean updateCompass(final IQuester quester, final IStage stage);

    boolean testRequirements(final IQuester quester);

    boolean testRequirements(final OfflinePlayer player);

    void completeQuest(final IQuester quester);

    void completeQuest(final IQuester quester, final boolean allowMultiplayer);

    void failQuest(final IQuester quester);

    void failQuest(final IQuester quester, final boolean ignoreFailAction);

    boolean isInRegion(final IQuester quester);

    boolean isInRegionStart(final IQuester quester);
}
