/*******************************************************************************************************
 * Continued by PikaMug (formerly HappyPikachu) with permission from _Blackvein_. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

package me.blackvein.quests;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.inventory.ItemStack;

public class Rewards {
    private int money = 0;
    private int questPoints = 0;
    private int exp = 0;
    private List<String> commands = new LinkedList<String>();
    private List<String> commandsOverrideDisplay = new LinkedList<String>();
    private List<String> permissions = new LinkedList<String>();
    private List<String> permissionWorlds = new LinkedList<String>();
    private List<ItemStack> items = new LinkedList<ItemStack>();
    private List<String> mcmmoSkills = new LinkedList<String>();
    private List<Integer> mcmmoAmounts = new LinkedList<Integer>();
    private List<String> heroesClasses = new LinkedList<String>();
    private List<Double> heroesAmounts = new LinkedList<Double>();
    private List<String> phatLoots = new LinkedList<String>();
    private Map<String, Map<String, Object>> customRewards = new HashMap<String, Map<String, Object>>();
    
    public int getMoney() {
        return money;
    }
    public void setMoney(int money) {
        this.money = money;
    }
    public int getQuestPoints() {
        return questPoints;
    }
    public void setQuestPoints(int questPoints) {
        this.questPoints = questPoints;
    }
    public int getExp() {
        return exp;
    }
    public void setExp(int exp) {
        this.exp = exp;
    }
    public List<String> getCommands() {
        return commands;
    }
    public void setCommands(List<String> commands) {
        this.commands = commands;
    }
    public List<String> getCommandsOverrideDisplay() {
        return commandsOverrideDisplay;
    }
    public void setCommandsOverrideDisplay(List<String> commandsOverrideDisplay) {
        this.commandsOverrideDisplay = commandsOverrideDisplay;
    }
    public List<String> getPermissions() {
        return permissions;
    }
    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
    public List<String> getPermissionWorlds() {
        return permissionWorlds;
    }
    public void setPermissionWorlds(List<String> worldNames) {
        this.permissionWorlds = worldNames;
    }
    public List<ItemStack> getItems() {
        return items;
    }
    public void setItems(List<ItemStack> items) {
        this.items = items;
    }
    public List<String> getMcmmoSkills() {
        return mcmmoSkills;
    }
    public void setMcmmoSkills(List<String> mcmmoSkills) {
        this.mcmmoSkills = mcmmoSkills;
    }
    public List<Integer> getMcmmoAmounts() {
        return mcmmoAmounts;
    }
    public void setMcmmoAmounts(List<Integer> mcmmoAmounts) {
        this.mcmmoAmounts = mcmmoAmounts;
    }
    public List<String> getHeroesClasses() {
        return heroesClasses;
    }
    public void setHeroesClasses(List<String> heroesClasses) {
        this.heroesClasses = heroesClasses;
    }
    public List<Double> getHeroesAmounts() {
        return heroesAmounts;
    }
    public void setHeroesAmounts(List<Double> heroesAmounts) {
        this.heroesAmounts = heroesAmounts;
    }
    public List<String> getPhatLoots() {
        return phatLoots;
    }
    public void setPhatLoots(List<String> phatLoots) {
        this.phatLoots = phatLoots;
    }
    public Map<String, Map<String, Object>> getCustomRewards() {
        return customRewards;
    }
    protected void setCustomRewards(Map<String, Map<String, Object>> customRewards) {
        this.customRewards = customRewards;
    }
}
