package me.blackvein.quests;

import me.blackvein.quests.actions.Action;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;

public interface Quest {
    public String getId();

    public String getName();

    public void setName(final String name);

    public String getDescription();

    public void setDescription(final String description);

    public String getFinished();

    public void setFinished(final String finished);

    public String getRegionStart();

    public void setRegionStart(final String regionStart);

    public ItemStack getGUIDisplay();

    public void setGUIDisplay(final ItemStack guiDisplay);

    public Stage getStage(final int index);

    public LinkedList<Stage> getStages();

    public NPC getNpcStart();

    public void setNpcStart(final NPC npcStart);

    public Location getBlockStart();

    public void setBlockStart(final Location blockStart);

    public Action getInitialAction();

    public void setInitialAction(final Action initialAction);

    public Requirements getRequirements();

    public Planner getPlanner();

    public Rewards getRewards();

    public Options getOptions();
}
