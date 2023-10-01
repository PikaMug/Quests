/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
