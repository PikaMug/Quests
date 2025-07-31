/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.player;

import java.util.LinkedList;

public interface QuestProgress {
    LinkedList<Integer> getBlocksBroken();

    void setBlocksBroken(final LinkedList<Integer> blocksBroken);

    LinkedList<Integer> getBlocksDamaged();

    void setBlocksDamaged(final LinkedList<Integer> blocksDamaged);

    LinkedList<Integer> getBlocksPlaced();

    void setBlocksPlaced(final LinkedList<Integer> blocksPlaced);

    LinkedList<Integer> getBlocksUsed();

    void setBlocksUsed(final LinkedList<Integer> blocksUsed);

    LinkedList<Integer> getBlocksCut();

    void setBlocksCut(final LinkedList<Integer> blocksCut);

    LinkedList<Integer> getItemsCrafted();

    void setItemsCrafted(final LinkedList<Integer> itemsCrafted);

    LinkedList<Integer> getItemsSmelted();

    void setItemsSmelted(final LinkedList<Integer> itemsSmelted);

    LinkedList<Integer> getItemsEnchanted();

    void setItemsEnchanted(final LinkedList<Integer> itemsEnchanted);

    LinkedList<Integer> getItemsBrewed();

    void setItemsBrewed(final LinkedList<Integer> itemsBrewed);

    LinkedList<Integer> getItemsConsumed();

    void setItemsConsumed(final LinkedList<Integer> itemsConsumed);

    LinkedList<Integer> getItemsDelivered();

    void setItemsDelivered(final LinkedList<Integer> itemsDelivered);

    LinkedList<Boolean> getNpcsInteracted();

    void setNpcsInteracted(final LinkedList<Boolean> npcsInteracted);

    LinkedList<Integer> getNpcsNumKilled();

    void setNpcsNumKilled(final LinkedList<Integer> npcsNumKilled);

    LinkedList<Integer> getMobNumKilled();

    void setMobNumKilled(final LinkedList<Integer> mobNumKilled);

    LinkedList<Integer> getMobsTamed();

    void setMobsTamed(final LinkedList<Integer> mobsTamed);

    int getFishCaught();

    void setFishCaught(final int fishCaught);

    int getCowsMilked();

    void setCowsMilked(final int cowsMilked);

    LinkedList<Integer> getSheepSheared();

    void setSheepSheared(final LinkedList<Integer> sheepSheared);

    int getPlayersKilled();

    void setPlayersKilled(final int playersKilled);

    LinkedList<Boolean> getLocationsReached();

    void setLocationsReached(final LinkedList<Boolean> locationsReached);

    LinkedList<Boolean> getPasswordsSaid();

    void setPasswordsSaid(final LinkedList<Boolean> passwordsSaid);

    LinkedList<Integer> getCustomObjectiveCounts();

    void setCustomObjectiveCounts(final LinkedList<Integer> customObjectiveCounts);

    long getDelayStartTime();

    void setDelayStartTime(final long delayStartTime);

    long getDelayTimeLeft();

    void setDelayTimeLeft(final long delayTimeLeft);

    boolean canDoJournalUpdate();

    void setDoJournalUpdate(final boolean b);
}
