/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.conditions;

import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;

import java.util.LinkedList;
import java.util.UUID;

public interface Condition extends Comparable<Condition> {
    String getName();

    void setName(final String name);

    boolean isFailQuest();

    void setFailQuest(final boolean failQuest);

    LinkedList<String> getEntitiesWhileRiding();

    void setEntitiesWhileRiding(final LinkedList<String> entitiesWhileRiding);

    LinkedList<UUID> getNpcsWhileRiding();

    void setNpcsWhileRiding(final LinkedList<UUID> npcsWhileRiding);

    LinkedList<String> getPermissions();

    void setPermissions(final LinkedList<String> permissions);

    LinkedList<String> getWorldsWhileStayingWithin();

    void setWorldsWhileStayingWithin(final LinkedList<String> worldsWhileStayingWithin);

    int getTickStartWhileStayingWithin();

    void setTickStartWhileStayingWithin(final int tickStartWhileStayingWithin);

    int getTickEndWhileStayingWithin();

    void setTickEndWhileStayingWithin(final int tickEndWhileStayingWithin);

    LinkedList<String> getBiomesWhileStayingWithin();

    void setBiomesWhileStayingWithin(final LinkedList<String> biomesWhileStayingWithin);

    LinkedList<String> getRegionsWhileStayingWithin();

    void setRegionsWhileStayingWithin(final LinkedList<String> biomesWhileStayingWithin);

    LinkedList<String> getPlaceholdersCheckIdentifier();

    void setPlaceholdersCheckIdentifier(final LinkedList<String> placeholdersCheckIdentifier);

    LinkedList<String> getPlaceholdersCheckValue();

    void setPlaceholdersCheckValue(final LinkedList<String> placeholdersCheckValue);

    boolean check(final Quester quester, final Quest quest);
}
