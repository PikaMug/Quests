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

package me.blackvein.quests.conditions;

import me.blackvein.quests.quests.IQuest;
import me.blackvein.quests.player.IQuester;
import me.blackvein.quests.Quests;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.MiscUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;
import java.util.Objects;

public class Condition implements ICondition {

    private final Quests plugin;
    private String name = "";
    private boolean failQuest = false;
    private LinkedList<String> entitiesWhileRiding = new LinkedList<>();
    private LinkedList<Integer> npcsWhileRiding = new LinkedList<>();
    private LinkedList<String> permissions = new LinkedList<>();
    private LinkedList<ItemStack> itemsWhileHoldingMainHand = new LinkedList<>();
    private LinkedList<String> worldsWhileStayingWithin = new LinkedList<>();
    private LinkedList<String> biomesWhileStayingWithin = new LinkedList<>();
    private LinkedList<String> regionsWhileStayingWithin = new LinkedList<>();
    private LinkedList<String> placeholdersCheckIdentifier = new LinkedList<>();
    private LinkedList<String> placeholdersCheckValue = new LinkedList<>();

    public Condition(final Quests plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public int compareTo(final ICondition condition) {
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
    public LinkedList<Integer> getNpcsWhileRiding() {
        return npcsWhileRiding;
    }

    @Override
    public void setNpcsWhileRiding(final LinkedList<Integer> npcsWhileRiding) {
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

    @Override
    public LinkedList<ItemStack> getItemsWhileHoldingMainHand() {
        return itemsWhileHoldingMainHand;
    }

    @Override
    public void setItemsWhileHoldingMainHand(final LinkedList<ItemStack> itemsWhileHoldingMainHand) {
        this.itemsWhileHoldingMainHand = itemsWhileHoldingMainHand;
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

    @SuppressWarnings("deprecation")
    @Override
    public boolean check(final IQuester quester, final IQuest quest) {
        final Player player = quester.getPlayer();
        if (!entitiesWhileRiding.isEmpty()) {
            for (final String e : entitiesWhileRiding) {
                if (player.getVehicle() != null && player.getVehicle().getType().equals(MiscUtil.getProperMobType(e))) {
                    return true;
                } else if (plugin.getSettings().getConsoleLogging() > 2) {
                    plugin.getLogger().info("DEBUG: ICondition entity mismatch for " + player.getName() + ": " + e);
                }
            }
        } else if (!npcsWhileRiding.isEmpty()) {
            for (final int n : npcsWhileRiding) {
                if (plugin.getDependencies().getCitizens() != null) {
                    if (player.getVehicle() != null && player.getVehicle()
                            .equals(plugin.getDependencies().getCitizens().getNPCRegistry().getById(n).getEntity())) {
                        return true;
                    } else if (plugin.getSettings().getConsoleLogging() > 2) {
                        plugin.getLogger().info("DEBUG: ICondition NPC mismatch for " + player.getName() + ": ID " + n);
                    }
                }
            }
        } else if (!permissions.isEmpty()) {
            for (final String p : permissions) {
                if (plugin.getDependencies().isPluginAvailable("Vault")) {
                    if (plugin.getDependencies().getVaultPermission().has(player, p)) {
                        return plugin.getDependencies().getVaultPermission().has(player, p);
                    } else if (plugin.getSettings().getConsoleLogging() > 2) {
                        plugin.getLogger().info("DEBUG: ICondition permission mismatch for " + player.getName() + ": " + p);
                    }
                } else {
                    plugin.getLogger().warning("Vault must be installed for condition permission checks: " + p);
                }
            }
        } else if (!itemsWhileHoldingMainHand.isEmpty()) {
            for (final ItemStack is : itemsWhileHoldingMainHand) {
                if (ItemUtil.compareItems(player.getItemInHand(), is, true, true) == 0) {
                    return true;
                } else if (plugin.getSettings().getConsoleLogging() > 2) {
                    plugin.getLogger().info("DEBUG: ICondition item mismatch for " + player.getName() + ": code "
                            + ItemUtil.compareItems(player.getItemInHand(), is, true, true));
                }
            }
        } else if (!worldsWhileStayingWithin.isEmpty()) {
            for (final String w : worldsWhileStayingWithin) {
                if (player.getWorld().getName().equalsIgnoreCase(w)) {
                    return true;
                } else if (plugin.getSettings().getConsoleLogging() > 2) {
                    plugin.getLogger().info("DEBUG: ICondition world mismatch for " + player.getName() + ": " + w);
                }
            }
        } else if (!biomesWhileStayingWithin.isEmpty()) {
            for (final String b : biomesWhileStayingWithin) {
                if (MiscUtil.getProperBiome(b) == null) {
                    continue;
                }
                if (player.getWorld().getBiome(player.getLocation().getBlockX(), player.getLocation().getBlockZ())
                        .name().equalsIgnoreCase(Objects.requireNonNull(MiscUtil.getProperBiome(b)).name())) {
                    return true;
                } else if (plugin.getSettings().getConsoleLogging() > 2) {
                    plugin.getLogger().info("DEBUG: ICondition biome mismatch for " + player.getName() + ": "
                            + MiscUtil.getProperBiome(b));
                }
            }
        } else if (!regionsWhileStayingWithin.isEmpty()) {
            for (final String r : regionsWhileStayingWithin) {
                if (quester.isInRegion(r)) {
                    return true;
                } else if (plugin.getSettings().getConsoleLogging() > 2) {
                    plugin.getLogger().info("DEBUG: ICondition region mismatch for " + player.getName() + ": " + r);
                }
            }
        } else if (!placeholdersCheckIdentifier.isEmpty()) {
            int index = 0;
            for (final String i : placeholdersCheckIdentifier) {
                if (plugin.getDependencies().isPluginAvailable("PlaceholderAPI")) {
                    if (placeholdersCheckValue.size() > index &&
                            placeholdersCheckValue.get(index).equals(PlaceholderAPI.setPlaceholders(player, i))) {
                        return true;
                    } else if (plugin.getSettings().getConsoleLogging() > 2) {
                        plugin.getLogger().info("DEBUG: ICondition placeholder mismatch for " + player.getName() + ": " + i);
                    }
                } else {
                    plugin.getLogger().warning("PAPI must be installed for placeholder checks: " + i);
                }
                index++;
            }
        }
        return false;
    }
}
    
