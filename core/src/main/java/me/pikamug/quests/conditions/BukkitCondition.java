/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.conditions;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.util.BukkitItemUtil;
import me.pikamug.quests.util.BukkitMiscUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;
import java.util.Objects;
import java.util.UUID;

public class BukkitCondition implements Condition {

    private final BukkitQuestsPlugin plugin;
    private String name = "";
    private boolean failQuest = false;
    private LinkedList<String> entitiesWhileRiding = new LinkedList<>();
    private LinkedList<UUID> npcsWhileRiding = new LinkedList<>();
    private LinkedList<String> permissions = new LinkedList<>();
    private LinkedList<ItemStack> itemsWhileHoldingMainHand = new LinkedList<>();
    private LinkedList<ItemStack> itemsWhileWearing = new LinkedList<>();
    private LinkedList<String> worldsWhileStayingWithin = new LinkedList<>();
    private int tickStartWhileStayingWithin = -1;
    private int tickEndWhileStayingWithin = -1;
    private LinkedList<String> biomesWhileStayingWithin = new LinkedList<>();
    private LinkedList<String> regionsWhileStayingWithin = new LinkedList<>();
    private LinkedList<String> placeholdersCheckIdentifier = new LinkedList<>();
    private LinkedList<String> placeholdersCheckValue = new LinkedList<>();

    public BukkitCondition(final BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public int compareTo(final Condition condition) {
        return name.compareTo(condition.getName());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public boolean isFailQuest() {
        return failQuest;
    }

    @Override
    public void setFailQuest(final boolean failQuest) {
        this.failQuest = failQuest;
    }

    @Override
    public LinkedList<String> getEntitiesWhileRiding() {
        return entitiesWhileRiding;
    }

    @Override
    public void setEntitiesWhileRiding(final LinkedList<String> entitiesWhileRiding) {
        this.entitiesWhileRiding = entitiesWhileRiding;
    }

    @Override
    public LinkedList<UUID> getNpcsWhileRiding() {
        return npcsWhileRiding;
    }

    @Override
    public void setNpcsWhileRiding(final LinkedList<UUID> npcsWhileRiding) {
        this.npcsWhileRiding = npcsWhileRiding;
    }

    @Override
    public LinkedList<String> getPermissions() {
        return permissions;
    }

    @Override
    public void setPermissions(final LinkedList<String> permissions) {
        this.permissions = permissions;
    }

    public LinkedList<ItemStack> getItemsWhileHoldingMainHand() {
        return itemsWhileHoldingMainHand;
    }

    public void setItemsWhileHoldingMainHand(final LinkedList<ItemStack> itemsWhileHoldingMainHand) {
        this.itemsWhileHoldingMainHand = itemsWhileHoldingMainHand;
    }

    public LinkedList<ItemStack> getItemsWhileWearing() {
        return itemsWhileWearing;
    }

    public void setItemsWhileWearing(final LinkedList<ItemStack> itemsWhileWearing) {
        this.itemsWhileWearing = itemsWhileWearing;
    }

    @Override
    public LinkedList<String> getWorldsWhileStayingWithin() {
        return worldsWhileStayingWithin;
    }

    @Override
    public void setWorldsWhileStayingWithin(final LinkedList<String> worldsWhileStayingWithin) {
        this.worldsWhileStayingWithin = worldsWhileStayingWithin;
    }

    @Override
    public int getTickStartWhileStayingWithin() {
        return tickStartWhileStayingWithin;
    }

    @Override
    public void setTickStartWhileStayingWithin(final int tickStartWhileStayingWithin) {
        this.tickStartWhileStayingWithin = tickStartWhileStayingWithin;
    }

    @Override
    public int getTickEndWhileStayingWithin() {
        return tickEndWhileStayingWithin;
    }

    @Override
    public void setTickEndWhileStayingWithin(final int tickEndWhileStayingWithin) {
        this.tickEndWhileStayingWithin = tickEndWhileStayingWithin;
    }

    @Override
    public LinkedList<String> getBiomesWhileStayingWithin() {
        return biomesWhileStayingWithin;
    }

    @Override
    public void setBiomesWhileStayingWithin(final LinkedList<String> biomesWhileStayingWithin) {
        this.biomesWhileStayingWithin = biomesWhileStayingWithin;
    }

    @Override
    public LinkedList<String> getRegionsWhileStayingWithin() {
        return regionsWhileStayingWithin;
    }

    @Override
    public void setRegionsWhileStayingWithin(final LinkedList<String> biomesWhileStayingWithin) {
        this.regionsWhileStayingWithin = biomesWhileStayingWithin;
    }

    @Override
    public LinkedList<String> getPlaceholdersCheckIdentifier() {
        return placeholdersCheckIdentifier;
    }

    @Override
    public void setPlaceholdersCheckIdentifier(final LinkedList<String> placeholdersCheckIdentifier) {
        this.placeholdersCheckIdentifier = placeholdersCheckIdentifier;
    }

    @Override
    public LinkedList<String> getPlaceholdersCheckValue() {
        return placeholdersCheckValue;
    }

    @Override
    public void setPlaceholdersCheckValue(final LinkedList<String> placeholdersCheckValue) {
        this.placeholdersCheckValue = placeholdersCheckValue;
    }

    /**
     * Checks whether the Quester passes all applicable conditions for provided quest
     * @param quester Quester to check
     * @param quest Quest to check
     * @return true if successful
     */
    @SuppressWarnings("deprecation")
    @Override
    public boolean check(final Quester quester, final Quest quest) {
        final Player player = quester.getPlayer();
        boolean failed = false;
        if (!entitiesWhileRiding.isEmpty()) {
            boolean atLeastOne = false;
            for (final String e : entitiesWhileRiding) {
                if (player.getVehicle() == null) {
                    return false;
                }
                if (player.getVehicle().getType().equals(BukkitMiscUtil.getProperMobType(e))) {
                    atLeastOne = true;
                    break;
                }
            }
            if (!atLeastOne) {
                failed = true;
            }
        } else if (!npcsWhileRiding.isEmpty()) {
            boolean atLeastOne = false;
            for (final UUID n : npcsWhileRiding) {
                if (plugin.getDependencies().getCitizens() == null) {
                    plugin.getLogger().warning("Citizens must be installed for condition ride NPC UUID " + n);
                    return false;
                }
                if (player.getVehicle() == null) {
                    return false;
                }
                if (player.getVehicle().equals(plugin.getDependencies().getCitizens().getNPCRegistry().getByUniqueId(n)
                        .getEntity())) {
                    atLeastOne = true;
                    break;
                }
            }
            if (!atLeastOne) {
                failed = true;
            }
        } else if (!permissions.isEmpty()) {
            // Must have ALL listed permissions
            for (final String p : permissions) {
                if (!plugin.getDependencies().isPluginAvailable("Vault")) {
                    plugin.getLogger().warning("Vault must be installed for condition permission checks: " + p);
                    return false;
                }
                if (!plugin.getDependencies().getVaultPermission().has(player, p)) {
                    failed = true;
                    if (plugin.getConfigSettings().getConsoleLogging() > 2) {
                        plugin.getLogger().info("DEBUG: Condition permission mismatch for " + player.getName() + ": "
                                + p);
                    }
                    break;
                }
            }
        } else if (!itemsWhileHoldingMainHand.isEmpty()) {
            boolean atLeastOne = false;
            for (final ItemStack is : itemsWhileHoldingMainHand) {
                if (BukkitItemUtil.compareItems(player.getItemInHand(), is, true, true) == 0) {
                    atLeastOne = true;
                    break;
                }
            }
            if (!atLeastOne) {
                failed = true;
            }
        } else if (!itemsWhileWearing.isEmpty()) {
            // Must have ALL listed armor equipped
            int matches = 0;
            for (final ItemStack is : itemsWhileWearing) {
                for (ItemStack armor : player.getInventory().getArmorContents()) {
                    if (BukkitItemUtil.compareItems(armor, is, true, true) == 0) {
                        matches++;
                        break;
                    }
                }
            }
            if (matches != itemsWhileWearing.size()) {
                failed = true;
            }
        } else if (!worldsWhileStayingWithin.isEmpty()) {
            boolean atLeastOne = false;
            for (final String w : worldsWhileStayingWithin) {
                if (player.getWorld().getName().equalsIgnoreCase(w)) {
                    atLeastOne = true;
                    break;
                }
            }
            if (!atLeastOne) {
                failed = true;
            }
        } else if (tickStartWhileStayingWithin > -1 && tickEndWhileStayingWithin > -1) {
            long t = player.getWorld().getTime();
            if (t < tickStartWhileStayingWithin || t > tickEndWhileStayingWithin) {
                failed = true;
            }
        } else if (!biomesWhileStayingWithin.isEmpty()) {
            boolean atLeastOne = false;
            for (final String b : biomesWhileStayingWithin) {
                if (BukkitMiscUtil.getProperBiome(b) == null) {
                    plugin.getLogger().warning("Invalid entry for condition biome checks: " + b);
                    return false;
                }
                if (player.getWorld().getBiome(player.getLocation().getBlockX(), player.getLocation().getBlockZ())
                        .name().equalsIgnoreCase(Objects.requireNonNull(BukkitMiscUtil.getProperBiome(b)).name())) {
                    atLeastOne = true;
                    break;
                }
            }
            if (!atLeastOne) {
                failed = true;
            }
        } else if (!regionsWhileStayingWithin.isEmpty()) {
            // Must be within ALL listed regions
            for (final String r : regionsWhileStayingWithin) {
                if (!quester.isInRegion(r)) {
                    failed = true;
                    if (plugin.getConfigSettings().getConsoleLogging() > 2) {
                        plugin.getLogger().info("DEBUG: Condition region mismatch for " + player.getName() + ": " + r);
                    }
                    break;
                }
            }
        } else if (!placeholdersCheckIdentifier.isEmpty()) {
            // Must have ALL listed placeholders equal true
            int index = 0;
            for (final String i : placeholdersCheckIdentifier) {
                if (!plugin.getDependencies().isPluginAvailable("PlaceholderAPI")) {
                    plugin.getLogger().warning("PAPI must be installed for placeholder checks: " + i);
                    return false;
                }
                if (placeholdersCheckValue.size() <= index) {
                    plugin.getLogger().warning("Condition placeholder values outweigh identifiers: " + i);
                    return false;
                }
                if (!placeholdersCheckValue.get(index).equals(PlaceholderAPI.setPlaceholders(player, i))) {
                    failed = true;
                    if (plugin.getConfigSettings().getConsoleLogging() > 2) {
                        plugin.getLogger().info("DEBUG: Condition placeholder mismatch for " + player.getName() + ": "
                                + i);
                    }
                    break;
                }
                index++;
            }
        }
        return !failed;
    }
}
    
