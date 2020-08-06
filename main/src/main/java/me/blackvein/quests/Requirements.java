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

public class Requirements {
    private int money = 0;
    private int questPoints = 0;
    private List<ItemStack> items = new LinkedList<ItemStack>();
    private List<Boolean> removeItems = new LinkedList<Boolean>();
    private List<String> neededQuests = new LinkedList<String>();
    private List<String> blockQuests = new LinkedList<String>();
    private List<String> permissions = new LinkedList<String>();
    private List<String> mcmmoSkills = new LinkedList<String>();
    private List<Integer> mcmmoAmounts = new LinkedList<Integer>();
    private String heroesPrimaryClass = null;
    private String heroesSecondaryClass = null;
    private Map<String, Map<String, Object>> customRequirements = new HashMap<String, Map<String, Object>>();
    private List<String> detailsOverride = new LinkedList<String>();
    
    public int getMoney() {
        return money;
    }
    public void setMoney(final int money) {
        this.money = money;
    }
    public int getQuestPoints() {
        return questPoints;
    }
    public void setQuestPoints(final int questPoints) {
        this.questPoints = questPoints;
    }
    public List<ItemStack> getItems() {
        return items;
    }
    public void setItems(final List<ItemStack> items) {
        this.items = items;
    }
    public List<Boolean> getRemoveItems() {
        return removeItems;
    }
    public void setRemoveItems(final List<Boolean> removeItems) {
        this.removeItems = removeItems;
    }
    public List<String> getNeededQuests() {
        return neededQuests;
    }
    public void setNeededQuests(final List<String> neededQuests) {
        this.neededQuests = neededQuests;
    }
    public List<String> getBlockQuests() {
        return blockQuests;
    }
    public void setBlockQuests(final List<String> blockQuests) {
        this.blockQuests = blockQuests;
    }
    public List<String> getPermissions() {
        return permissions;
    }
    public void setPermissions(final List<String> permissions) {
        this.permissions = permissions;
    }
    public List<String> getMcmmoSkills() {
        return mcmmoSkills;
    }
    public void setMcmmoSkills(final List<String> mcmmoSkills) {
        this.mcmmoSkills = mcmmoSkills;
    }
    public List<Integer> getMcmmoAmounts() {
        return mcmmoAmounts;
    }
    public void setMcmmoAmounts(final List<Integer> mcmmoAmounts) {
        this.mcmmoAmounts = mcmmoAmounts;
    }
    public String getHeroesPrimaryClass() {
        return heroesPrimaryClass;
    }
    public void setHeroesPrimaryClass(final String heroesPrimaryClass) {
        this.heroesPrimaryClass = heroesPrimaryClass;
    }
    public String getHeroesSecondaryClass() {
        return heroesSecondaryClass;
    }
    public void setHeroesSecondaryClass(final String heroesSecondaryClass) {
        this.heroesSecondaryClass = heroesSecondaryClass;
    }
    public Map<String, Map<String, Object>> getCustomRequirements() {
        return customRequirements;
    }
    protected void setCustomRequirements(final Map<String, Map<String, Object>> customRequirements) {
        this.customRequirements = customRequirements;
    }
    public List<String> getDetailsOverride() {
        return detailsOverride;
    }
    public void setDetailsOverride(final List<String> detailsOverride) {
        this.detailsOverride = detailsOverride;
    }
}
