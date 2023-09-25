/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.quests.components;

import java.util.List;
import java.util.Map;

public interface Requirements {
    int getMoney();

    void setMoney(final int money);

    int getQuestPoints();

    void setQuestPoints(final int questPoints);

    int getExp();

    void setExp(final int exp);

    List<?> getItems();

    List<Boolean> getRemoveItems();

    void setRemoveItems(final List<Boolean> removeItems);

    List<String> getNeededQuestIds();

    void setNeededQuestIds(final List<String> neededQuests);

    List<String> getBlockQuestIds();

    void setBlockQuestIds(final List<String> blockQuests);

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
