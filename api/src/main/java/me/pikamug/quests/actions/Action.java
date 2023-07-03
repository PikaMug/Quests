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

package me.pikamug.quests.actions;

import me.pikamug.quests.entity.QuestMob;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;

import java.util.LinkedList;

public interface Action extends Comparable<Action> {
    String getName();

    void setName(final String name);

    String getMessage();

    void setMessage(final String message);

    boolean isClearInv();

    void setClearInv(final boolean clearInv);

    boolean isFailQuest();

    void setFailQuest(final boolean failQuest);

    int getStormDuration();

    void setStormDuration(final int stormDuration);

    int getThunderDuration();

    void setThunderDuration(final int thunderDuration);

    int getTimer();

    void setTimer(final int timer);

    boolean isCancelTimer();

    void setCancelTimer(final boolean cancelTimer);

    LinkedList<QuestMob> getMobSpawns();

    void setMobSpawns(final LinkedList<QuestMob> mobSpawns);

    LinkedList<String> getCommands();

    void setCommands(final LinkedList<String> commands);

    int getHunger();

    void setHunger(final int hunger);

    int getSaturation();

    void setSaturation(final int saturation);

    float getHealth();

    void setHealth(final float health);

    String getBook();

    void setBook(final String book);

    String getDenizenScript();

    void setDenizenScript(final String scriptName);

    void fire(final Quester quester, final Quest quest);
}
