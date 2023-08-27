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

package me.pikamug.quests.player;

import java.util.LinkedList;

public interface QuestProgress {
    LinkedList<?> getBlocksBroken();

    LinkedList<?> getBlocksDamaged();

    LinkedList<?> getBlocksPlaced();

    LinkedList<?> getBlocksUsed();

    LinkedList<?> getBlocksCut();

    LinkedList<?> getItemsCrafted();

    LinkedList<?> getItemsSmelted();

    LinkedList<?> getItemsEnchanted();

    LinkedList<?> getItemsBrewed();

    LinkedList<?> getItemsConsumed();

    LinkedList<?> getItemsDelivered();

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
