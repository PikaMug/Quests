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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FabricRewards implements Rewards {
    private int money = 0;
    private int questPoints = 0;
    private int exp = 0;
    private List<String> commands = new LinkedList<>();
    private List<String> commandsOverrideDisplay = new LinkedList<>();
    private List<String> permissions = new LinkedList<>();
    private List<String> permissionWorlds = new LinkedList<>();
    private List<ItemStack> items = new LinkedList<>();
    private Map<String, Map<String, Object>> customRewards = new HashMap<>();
    private List<String> detailsOverride = new LinkedList<>();

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
    public int getExp() {
        return exp;
    }
    public void setExp(final int exp) {
        this.exp = exp;
    }
    public List<String> getCommands() {
        return commands;
    }
    public void setCommands(final List<String> commands) {
        this.commands = commands;
    }
    public List<String> getCommandsOverrideDisplay() {
        return commandsOverrideDisplay;
    }
    public void setCommandsOverrideDisplay(final List<String> commandsOverrideDisplay) {
        this.commandsOverrideDisplay = commandsOverrideDisplay;
    }
    public List<String> getPermissions() {
        return permissions;
    }
    public void setPermissions(final List<String> permissions) {
        this.permissions = permissions;
    }
    public List<String> getPermissionWorlds() {
        return permissionWorlds;
    }
    public void setPermissionWorlds(final List<String> worldNames) {
        this.permissionWorlds = worldNames;
    }
    public List<ItemStack> getItems() {
        return items;
    }
    public void setItems(final List<ItemStack> items) {
        this.items = items;
    }
    public Map<String, Map<String, Object>> getCustomRewards() {
        return customRewards;
    }
    public void setCustomRewards(final Map<String, Map<String, Object>> customRewards) {
        this.customRewards = customRewards;
    }
    public List<String> getDetailsOverride() {
        return detailsOverride;
    }
    public void setDetailsOverride(final List<String> detailsOverride) {
        this.detailsOverride = detailsOverride;
    }

    @Override
    public boolean hasReward() {
        if (money != 0) { return true; }
        if (questPoints != 0) { return true; }
        if (exp != 0) { return true; }
        if (!commands.isEmpty()) { return true; }
        if (!permissions.isEmpty()) { return true; }
        if (!items.isEmpty()) { return true; }
        return !customRewards.isEmpty();
    }
}
