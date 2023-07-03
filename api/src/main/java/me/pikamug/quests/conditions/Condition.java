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
