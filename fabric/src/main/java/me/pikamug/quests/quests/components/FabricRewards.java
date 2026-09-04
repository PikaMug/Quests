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

public class FabricRewards implements Rewards {

    private int money = 0;
    private int questPoints = 0;
    private int exp = 0;
    private List<String> commands = new ArrayList<>();
    private List<String> commandsOverrideDisplay = new ArrayList<>();
    private List<String> permissions = new ArrayList<>();
    private List<String> permissionWorlds = new ArrayList<>();
    private List<ItemStack> items = new ArrayList<>();
    private List<String> mcmmoSkills = new ArrayList<>();
    private List<Integer> mcmmoAmounts = new ArrayList<>();
    private List<String> heroesClasses = new ArrayList<>();
    private List<Double> heroesAmounts = new ArrayList<>();
    private int partiesExperience = 0;
    private List<String> phatLoots = new ArrayList<>();
    private Map<String, Map<String, Object>> customRewards = new HashMap<>();
    private List<String> detailsOverride = new ArrayList<>();

    @Override public int getMoney() { return money; }
    @Override public void setMoney(int v) { this.money = v; }
    @Override public int getQuestPoints() { return questPoints; }
    @Override public void setQuestPoints(int v) { this.questPoints = v; }
    @Override public int getExp() { return exp; }
    @Override public void setExp(int v) { this.exp = v; }
    @Override public List<String> getCommands() { return commands; }
    @Override public void setCommands(List<String> v) { this.commands = v; }
    @Override public List<String> getCommandsOverrideDisplay() { return commandsOverrideDisplay; }
    @Override public void setCommandsOverrideDisplay(List<String> v) { this.commandsOverrideDisplay = v; }
    @Override public List<String> getPermissions() { return permissions; }
    @Override public void setPermissions(List<String> v) { this.permissions = v; }
    @Override public List<String> getPermissionWorlds() { return permissionWorlds; }
    @Override public void setPermissionWorlds(List<String> v) { this.permissionWorlds = v; }
    @Override public List<?> getItems() { return items; }
    public void setItems(List<ItemStack> v) { this.items = v; }
    @Override public List<String> getMcmmoSkills() { return mcmmoSkills; }
    @Override public void setMcmmoSkills(List<String> v) { this.mcmmoSkills = v; }
    @Override public List<Integer> getMcmmoAmounts() { return mcmmoAmounts; }
    @Override public void setMcmmoAmounts(List<Integer> v) { this.mcmmoAmounts = v; }
    @Override public List<String> getHeroesClasses() { return heroesClasses; }
    @Override public void setHeroesClasses(List<String> v) { this.heroesClasses = v; }
    @Override public List<Double> getHeroesAmounts() { return heroesAmounts; }
    @Override public void setHeroesAmounts(List<Double> v) { this.heroesAmounts = v; }
    @Override public int getPartiesExperience() { return partiesExperience; }
    @Override public void setPartiesExperience(int v) { this.partiesExperience = v; }
    @Override public List<String> getPhatLoots() { return phatLoots; }
    @Override public void setPhatLoots(List<String> v) { this.phatLoots = v; }
    @Override public Map<String, Map<String, Object>> getCustomRewards() { return customRewards; }
    @Override public void setCustomRewards(Map<String, Map<String, Object>> v) { this.customRewards = v; }
    @Override public List<String> getDetailsOverride() { return detailsOverride; }
    @Override public void setDetailsOverride(List<String> v) { this.detailsOverride = v; }

    @Override
    public boolean hasReward() {
        return money != 0 || questPoints != 0 || exp != 0 || !commands.isEmpty()
                || !permissions.isEmpty() || !items.isEmpty() || !mcmmoSkills.isEmpty()
                || !heroesClasses.isEmpty() || partiesExperience != 0 || !phatLoots.isEmpty()
                || !customRewards.isEmpty();
    }
}
