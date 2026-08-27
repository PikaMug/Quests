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

import net.minecraft.world.item.ItemStack;

import java.util.*;

public class FabricRequirements implements Requirements {

    private int money = 0;
    private int questPoints = 0;
    private int exp = 0;
    private List<ItemStack> items = new ArrayList<>();
    private List<Boolean> removeItems = new ArrayList<>();
    private List<String> neededQuestIds = new ArrayList<>();
    private List<String> blockQuestIds = new ArrayList<>();
    private List<String> permissions = new ArrayList<>();
    private List<String> mcmmoSkills = new ArrayList<>();
    private List<Integer> mcmmoAmounts = new ArrayList<>();
    private String heroesPrimaryClass;
    private String heroesSecondaryClass;
    private Map<String, Map<String, Object>> customRequirements = new HashMap<>();
    private List<String> detailsOverride = new ArrayList<>();

    @Override public int getMoney() { return money; }
    @Override public void setMoney(int v) { this.money = v; }
    @Override public int getQuestPoints() { return questPoints; }
    @Override public void setQuestPoints(int v) { this.questPoints = v; }
    @Override public int getExp() { return exp; }
    @Override public void setExp(int v) { this.exp = v; }
    @Override public List<?> getItems() { return items; }
    @Override public List<Boolean> getRemoveItems() { return removeItems; }
    @Override public void setRemoveItems(List<Boolean> v) { this.removeItems = v; }
    @Override public List<String> getNeededQuestIds() { return neededQuestIds; }
    @Override public void setNeededQuestIds(List<String> v) { this.neededQuestIds = v; }
    @Override public List<String> getBlockQuestIds() { return blockQuestIds; }
    @Override public void setBlockQuestIds(List<String> v) { this.blockQuestIds = v; }
    @Override public List<String> getPermissions() { return permissions; }
    @Override public void setPermissions(List<String> v) { this.permissions = v; }
    @Override public List<String> getMcmmoSkills() { return mcmmoSkills; }
    @Override public void setMcmmoSkills(List<String> v) { this.mcmmoSkills = v; }
    @Override public List<Integer> getMcmmoAmounts() { return mcmmoAmounts; }
    @Override public void setMcmmoAmounts(List<Integer> v) { this.mcmmoAmounts = v; }
    @Override public String getHeroesPrimaryClass() { return heroesPrimaryClass; }
    @Override public void setHeroesPrimaryClass(String v) { this.heroesPrimaryClass = v; }
    @Override public String getHeroesSecondaryClass() { return heroesSecondaryClass; }
    @Override public void setHeroesSecondaryClass(String v) { this.heroesSecondaryClass = v; }
    @Override public Map<String, Map<String, Object>> getCustomRequirements() { return customRequirements; }
    @Override public void setCustomRequirements(Map<String, Map<String, Object>> v) { this.customRequirements = v; }
    @Override public List<String> getDetailsOverride() { return detailsOverride; }
    @Override public void setDetailsOverride(List<String> v) { this.detailsOverride = v; }

    @Override
    public boolean hasRequirement() {
        return money != 0 || questPoints != 0 || exp != 0 || !items.isEmpty()
                || !neededQuestIds.isEmpty() || !blockQuestIds.isEmpty()
                || !permissions.isEmpty() || !mcmmoSkills.isEmpty()
                || (heroesPrimaryClass != null && !heroesPrimaryClass.isEmpty())
                || (heroesSecondaryClass != null && !heroesSecondaryClass.isEmpty())
                || !customRequirements.isEmpty();
    }
}
