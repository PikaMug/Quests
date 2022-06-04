/*
 * Copyright (c) 2014 PikaMug and contributors. All rights reserved.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package me.blackvein.quests.quests;

import me.blackvein.quests.actions.IAction;
import me.blackvein.quests.player.IQuester;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.LinkedList;
import java.util.UUID;

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

    UUID getNpcStart();

    void setNpcStart(final UUID npcStart);

    String getNpcStartName();

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
