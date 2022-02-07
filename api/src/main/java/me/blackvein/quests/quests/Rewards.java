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
    List<ItemStack> getItems();
    void setItems(final List<ItemStack> items);
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
}
