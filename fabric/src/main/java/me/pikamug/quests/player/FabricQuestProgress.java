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

public class FabricQuestProgress implements QuestProgress {

    private LinkedList<Integer> blocksBroken = new LinkedList<>();
    private LinkedList<Integer> blocksDamaged = new LinkedList<>();
    private LinkedList<Integer> blocksPlaced = new LinkedList<>();
    private LinkedList<Integer> blocksUsed = new LinkedList<>();
    private LinkedList<Integer> blocksCut = new LinkedList<>();
    private LinkedList<Integer> itemsCrafted = new LinkedList<>();
    private LinkedList<Integer> itemsSmelted = new LinkedList<>();
    private LinkedList<Integer> itemsEnchanted = new LinkedList<>();
    private LinkedList<Integer> itemsBrewed = new LinkedList<>();
    private LinkedList<Integer> itemsConsumed = new LinkedList<>();
    private LinkedList<Integer> itemsDelivered = new LinkedList<>();
    private LinkedList<Boolean> npcsInteracted = new LinkedList<>();
    private LinkedList<Integer> npcsNumKilled = new LinkedList<>();
    private LinkedList<Integer> mobNumKilled = new LinkedList<>();
    private LinkedList<Integer> mobsTamed = new LinkedList<>();
    private int fishCaught = 0;
    private int cowsMilked = 0;
    private LinkedList<Integer> sheepSheared = new LinkedList<>();
    private int playersKilled = 0;
    private LinkedList<Boolean> locationsReached = new LinkedList<>();
    private LinkedList<Boolean> passwordsSaid = new LinkedList<>();
    private LinkedList<Integer> customObjectiveCounts = new LinkedList<>();
    private long delayStartTime = 0;
    private long delayTimeLeft = 0;
    private boolean doJournalUpdate = true;

    @Override public LinkedList<Integer> getBlocksBroken() { return blocksBroken; }
    @Override public void setBlocksBroken(LinkedList<Integer> v) { this.blocksBroken = v; }
    @Override public LinkedList<Integer> getBlocksDamaged() { return blocksDamaged; }
    @Override public void setBlocksDamaged(LinkedList<Integer> v) { this.blocksDamaged = v; }
    @Override public LinkedList<Integer> getBlocksPlaced() { return blocksPlaced; }
    @Override public void setBlocksPlaced(LinkedList<Integer> v) { this.blocksPlaced = v; }
    @Override public LinkedList<Integer> getBlocksUsed() { return blocksUsed; }
    @Override public void setBlocksUsed(LinkedList<Integer> v) { this.blocksUsed = v; }
    @Override public LinkedList<Integer> getBlocksCut() { return blocksCut; }
    @Override public void setBlocksCut(LinkedList<Integer> v) { this.blocksCut = v; }
    @Override public LinkedList<Integer> getItemsCrafted() { return itemsCrafted; }
    @Override public void setItemsCrafted(LinkedList<Integer> v) { this.itemsCrafted = v; }
    @Override public LinkedList<Integer> getItemsSmelted() { return itemsSmelted; }
    @Override public void setItemsSmelted(LinkedList<Integer> v) { this.itemsSmelted = v; }
    @Override public LinkedList<Integer> getItemsEnchanted() { return itemsEnchanted; }
    @Override public void setItemsEnchanted(LinkedList<Integer> v) { this.itemsEnchanted = v; }
    @Override public LinkedList<Integer> getItemsBrewed() { return itemsBrewed; }
    @Override public void setItemsBrewed(LinkedList<Integer> v) { this.itemsBrewed = v; }
    @Override public LinkedList<Integer> getItemsConsumed() { return itemsConsumed; }
    @Override public void setItemsConsumed(LinkedList<Integer> v) { this.itemsConsumed = v; }
    @Override public LinkedList<Integer> getItemsDelivered() { return itemsDelivered; }
    @Override public void setItemsDelivered(LinkedList<Integer> v) { this.itemsDelivered = v; }
    @Override public LinkedList<Boolean> getNpcsInteracted() { return npcsInteracted; }
    @Override public void setNpcsInteracted(LinkedList<Boolean> v) { this.npcsInteracted = v; }
    @Override public LinkedList<Integer> getNpcsNumKilled() { return npcsNumKilled; }
    @Override public void setNpcsNumKilled(LinkedList<Integer> v) { this.npcsNumKilled = v; }
    @Override public LinkedList<Integer> getMobNumKilled() { return mobNumKilled; }
    @Override public void setMobNumKilled(LinkedList<Integer> v) { this.mobNumKilled = v; }
    @Override public LinkedList<Integer> getMobsTamed() { return mobsTamed; }
    @Override public void setMobsTamed(LinkedList<Integer> v) { this.mobsTamed = v; }
    @Override public int getFishCaught() { return fishCaught; }
    @Override public void setFishCaught(int v) { this.fishCaught = v; }
    @Override public int getCowsMilked() { return cowsMilked; }
    @Override public void setCowsMilked(int v) { this.cowsMilked = v; }
    @Override public LinkedList<Integer> getSheepSheared() { return sheepSheared; }
    @Override public void setSheepSheared(LinkedList<Integer> v) { this.sheepSheared = v; }
    @Override public int getPlayersKilled() { return playersKilled; }
    @Override public void setPlayersKilled(int v) { this.playersKilled = v; }
    @Override public LinkedList<Boolean> getLocationsReached() { return locationsReached; }
    @Override public void setLocationsReached(LinkedList<Boolean> v) { this.locationsReached = v; }
    @Override public LinkedList<Boolean> getPasswordsSaid() { return passwordsSaid; }
    @Override public void setPasswordsSaid(LinkedList<Boolean> v) { this.passwordsSaid = v; }
    @Override public LinkedList<Integer> getCustomObjectiveCounts() { return customObjectiveCounts; }
    @Override public void setCustomObjectiveCounts(LinkedList<Integer> v) { this.customObjectiveCounts = v; }
    @Override public long getDelayStartTime() { return delayStartTime; }
    @Override public void setDelayStartTime(long v) { this.delayStartTime = v; }
    @Override public long getDelayTimeLeft() { return delayTimeLeft; }
    @Override public void setDelayTimeLeft(long v) { this.delayTimeLeft = v; }
    @Override public boolean canDoJournalUpdate() { return doJournalUpdate; }
    @Override public void setDoJournalUpdate(boolean v) { this.doJournalUpdate = v; }
}
