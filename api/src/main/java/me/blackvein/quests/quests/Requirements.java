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

import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public interface Requirements {
    int getMoney();

    void setMoney(final int money);

    int getQuestPoints();

    void setQuestPoints(final int questPoints);

    List<ItemStack> getItems();

    void setItems(final List<ItemStack> items);

    List<Boolean> getRemoveItems();

    void setRemoveItems(final List<Boolean> removeItems);

    List<IQuest> getNeededQuests();

    void setNeededQuests(final List<IQuest> neededQuests);

    List<IQuest> getBlockQuests();

    void setBlockQuests(final List<IQuest> blockQuests);

    List<String> getPermissions();

    void setPermissions(final List<String> permissions);

    List<String> getMcmmoSkills();

    void setMcmmoSkills(final List<String> mcmmoSkills);

    List<Integer> getMcmmoAmounts();

    void setMcmmoAmounts(final List<Integer> mcmmoAmounts);

    String getHeroesPrimaryClass();

    void setHeroesPrimaryClass(final String heroesPrimaryClass);

    String getHeroesSecondaryClass();

    void setHeroesSecondaryClass(final String heroesSecondaryClass);

    Map<String, Map<String, Object>> getCustomRequirements();

    void setCustomRequirements(final Map<String, Map<String, Object>> customRequirements);

    List<String> getDetailsOverride();

    void setDetailsOverride(final List<String> detailsOverride);

    /**
     * Check if quest has at least one requirement
     *
     * @return true if quest contains an requirement
     */
    boolean hasRequirement();
}
