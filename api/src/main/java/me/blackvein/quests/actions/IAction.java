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

package me.blackvein.quests.actions;

import me.blackvein.quests.quests.IQuest;
import me.blackvein.quests.player.IQuester;
import me.blackvein.quests.entity.QuestMob;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.LinkedList;
import java.util.Map;

public interface IAction extends Comparable<IAction> {
    String getName();

    void setName(final String name);

    String getMessage();

    void setMessage(final String message);

    boolean isClearInv();

    void setClearInv(final boolean clearInv);

    boolean isFailQuest();

    void setFailQuest(final boolean failQuest);

    LinkedList<Location> getExplosions();

    void setExplosions(final LinkedList<Location> explosions);

    Map<Location, Effect> getEffects();

    void setEffects(final Map<Location, Effect> effects);

    LinkedList<ItemStack> getItems();

    void setItems(final LinkedList<ItemStack> items);

    World getStormWorld();

    void setStormWorld(final World stormWorld);

    int getStormDuration();

    void setStormDuration(final int stormDuration);

    World getThunderWorld();

    void setThunderWorld(final World thunderWorld);

    int getThunderDuration();

    void setThunderDuration(final int thunderDuration);

    int getTimer();

    void setTimer(final int timer);

    boolean isCancelTimer();

    void setCancelTimer(final boolean cancelTimer);

    LinkedList<QuestMob> getMobSpawns();

    void setMobSpawns(final LinkedList<QuestMob> mobSpawns);

    LinkedList<Location> getLightningStrikes();

    void setLightningStrikes(final LinkedList<Location> lightningStrikes);

    LinkedList<String> getCommands();

    void setCommands(final LinkedList<String> commands);

    LinkedList<PotionEffect> getPotionEffects();

    void setPotionEffects(final LinkedList<PotionEffect> potionEffects);

    int getHunger();

    void setHunger(final int hunger);

    int getSaturation();

    void setSaturation(final int saturation);

    float getHealth();

    void setHealth(final float health);

    Location getTeleport();

    void setTeleport(final Location teleport);

    String getBook();

    void setBook(final String book);

    String getDenizenScript();

    void setDenizenScript(final String scriptName);

    void fire(final IQuester quester, final IQuest quest);
}
