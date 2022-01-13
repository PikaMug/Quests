package me.blackvein.quests;

import me.blackvein.quests.actions.Action;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.LinkedList;

public interface Quest extends Comparable<Quest> {
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

    Stage getStage(final int index);

    LinkedList<Stage> getStages();

    NPC getNpcStart();

    void setNpcStart(final NPC npcStart);

    Location getBlockStart();

    void setBlockStart(final Location blockStart);

    Action getInitialAction();

    void setInitialAction(final Action initialAction);

    Requirements getRequirements();

    Planner getPlanner();

    Rewards getRewards();

    Options getOptions();

    void nextStage(final Quester quester, final boolean allowSharedProgress);

    void setStage(final Quester quester, final int stage);

    boolean updateCompass(final Quester quester, final Stage stage);

    boolean testRequirements(final Quester quester);

    boolean testRequirements(final OfflinePlayer player);

    void completeQuest(final Quester quester);

    void completeQuest(final Quester quester, final boolean allowMultiplayer);

    void failQuest(final Quester quester);

    void failQuest(final Quester quester, final boolean ignoreFailAction);

    boolean isInRegion(final Quester quester);

    boolean isInRegionStart(final Quester quester);
}
