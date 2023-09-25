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

public interface Rewards {
    int getMoney();

    void setMoney(final int money);

    int getQuestPoints();

    void setQuestPoints(final int questPoints);

    int getExp();

    void setExp(final int exp);

    List<String> getCommands();

    void setCommands(final List<String> commands);

    List<String> getCommandsOverrideDisplay();

    void setCommandsOverrideDisplay(final List<String> commandsOverrideDisplay);

    List<String> getPermissions();

    void setPermissions(final List<String> permissions);

    List<String> getPermissionWorlds();

    void setPermissionWorlds(final List<String> worldNames);

    List<?> getItems();

    List<String> getMcmmoSkills();

    void setMcmmoSkills(final List<String> mcmmoSkills);

    List<Integer> getMcmmoAmounts();

    void setMcmmoAmounts(final List<Integer> mcmmoAmounts);

    List<String> getHeroesClasses();

    void setHeroesClasses(final List<String> heroesClasses);

    List<Double> getHeroesAmounts();

    void setHeroesAmounts(final List<Double> heroesAmounts);

    int getPartiesExperience();

    void setPartiesExperience(final int partiesExperience);

    List<String> getPhatLoots();

    void setPhatLoots(final List<String> phatLoots);

    Map<String, Map<String, Object>> getCustomRewards();

    void setCustomRewards(final Map<String, Map<String, Object>> customRewards);

    List<String> getDetailsOverride();

    void setDetailsOverride(final List<String> detailsOverride);

    /**
     * Check if quest has at least one reward
     *
     * @return true if quest contains an reward
     */
    boolean hasReward();
}
