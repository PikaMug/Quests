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

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.dependencies.FabricPlaceholderSupport;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.util.FabricItemUtil;
import me.pikamug.quests.util.FabricMiscUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedList;
import java.util.UUID;

public class FabricCondition implements Condition {

    private final FabricQuestsPlugin plugin;
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

    public FabricCondition(final FabricQuestsPlugin plugin) {
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
    @Override
    public boolean check(final Quester quester, final Quest quest) {
        if (quester == null) return true;

        final ServerPlayer player = getPlayer(quester);
        if (player == null) return true;

        boolean failed = false;
        if (!entitiesWhileRiding.isEmpty()) {
            boolean atLeastOne = false;
            if (player.getVehicle() == null) {
                return false;
            }
            final Entity vehicle = player.getVehicle();
            final boolean isBoat = vehicle instanceof Boat;
            final boolean isMinecart = vehicle instanceof Minecart;
            for (final String entityName : entitiesWhileRiding) {
                if (entityName.equalsIgnoreCase("boats") && isBoat) {
                    atLeastOne = true;
                    break;
                }
                if (entityName.equalsIgnoreCase("minecarts") && isMinecart) {
                    atLeastOne = true;
                    break;
                }
                final EntityType<?> type = resolveEntityType(entityName);
                if (type != null && type == vehicle.getType()) {
                    atLeastOne = true;
                    break;
                }
            }
            if (!atLeastOne) {
                failed = true;
            }
        } else if (!npcsWhileRiding.isEmpty()) {
            boolean atLeastOne = false;
            if (player.getVehicle() == null) {
                return false;
            }
            final Entity vehicle = player.getVehicle();
            for (final UUID npcUuid : npcsWhileRiding) {
                if (vehicle.getUUID().equals(npcUuid)) {
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
                if (!plugin.getDependencies().hasPermission(quester.getUUID(), p)) {
                    failed = true;
                    if (plugin.getConfigSettings().getConsoleLogging() > 3) {
                        plugin.getPluginLogger().info("DEBUG: Condition permission mismatch for " + player.getName().getString()
                                + ": " + p);
                    }
                    break;
                }
            }
        } else if (!itemsWhileHoldingMainHand.isEmpty()) {
            boolean atLeastOne = false;
            final ItemStack held = player.getMainHandItem();
            for (final ItemStack is : itemsWhileHoldingMainHand) {
                if (FabricItemUtil.matches(held, is)) {
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
                for (final EquipmentSlot slot : EquipmentSlot.values()) {
                    if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) continue;
                    final ItemStack armor = player.getItemBySlot(slot);
                    if (!armor.isEmpty() && FabricItemUtil.matches(armor, is)) {
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
            final String currentWorld = player.level().dimension().identifier().toString();
            for (final String w : worldsWhileStayingWithin) {
                if (currentWorld.equalsIgnoreCase(w)
                        || player.level().dimension().identifier().getPath().equalsIgnoreCase(w)) {
                    atLeastOne = true;
                    break;
                }
            }
            if (!atLeastOne) {
                failed = true;
            }
        } else if (tickStartWhileStayingWithin > -1 && tickEndWhileStayingWithin > -1) {
            final long t = player.level().getDefaultClockTime();
            if (t < tickStartWhileStayingWithin || t > tickEndWhileStayingWithin) {
                failed = true;
            }
        } else if (!biomesWhileStayingWithin.isEmpty()) {
            boolean atLeastOne = false;
            final Identifier currentBiome = player.level().getBiome(player.blockPosition())
                    .unwrapKey().map(resourceKey -> resourceKey.identifier()).orElse(null);
            if (currentBiome != null) {
                for (final String b : biomesWhileStayingWithin) {
                    final Identifier requestedBiome = Identifier.tryParse(b);
                    if (requestedBiome != null
                            && currentBiome.toString().equalsIgnoreCase(requestedBiome.toString())) {
                        atLeastOne = true;
                        break;
                    }
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
                    if (plugin.getConfigSettings().getConsoleLogging() > 3) {
                        plugin.getPluginLogger().info("DEBUG: Condition region mismatch for " + player.getName().getString()
                                + ": " + r);
                    }
                    break;
                }
            }
        } else if (!placeholdersCheckIdentifier.isEmpty()) {
            // Must have ALL listed placeholders equal the checked value
            if (!FabricLoader.getInstance().isModLoaded("placeholder-api")) {
                plugin.getPluginLogger().warn("Placeholder API must be installed for placeholder checks: "
                        + placeholdersCheckIdentifier.get(0));
                return false;
            }
            int index = 0;
            for (final String i : placeholdersCheckIdentifier) {
                if (placeholdersCheckValue.size() <= index) {
                    plugin.getPluginLogger().warn("Condition placeholder values outweigh identifiers: " + i);
                    return false;
                }
                final String value;
                try {
                    value = FabricPlaceholderSupport.parse(player, i);
                } catch (final Throwable t) {
                    plugin.getPluginLogger().warn("Failed to resolve placeholder '" + i + "'");
                    return false;
                }
                if (!placeholdersCheckValue.get(index).equals(value)) {
                    failed = true;
                    if (plugin.getConfigSettings().getConsoleLogging() > 3) {
                        plugin.getPluginLogger().info("DEBUG: Condition placeholder mismatch for "
                                + player.getName().getString() + ": " + i);
                    }
                    break;
                }
                index++;
            }
        }
        return !failed;
    }

    private ServerPlayer getPlayer(final Quester quester) {
        final MinecraftServer server = plugin.getServer();
        if (server == null) return null;
        return server.getPlayerList().getPlayer(quester.getUUID());
    }

    /**
     * Resolves an entity name read from config (e.g. {@code PIG} or
     * {@code minecraft:pig}) to its {@link EntityType}. Plain names such as
     * {@code PIG} are given the default {@code minecraft:} namespace so they can
     * be resolved against the entity type registry.
     */
    private EntityType<?> resolveEntityType(final String name) {
        if (name == null) return null;
        final String trimmed = name.trim();
        if (trimmed.isEmpty()) return null;
        final String id = trimmed.contains(":") ? trimmed.toLowerCase() : "minecraft:" + trimmed.toLowerCase();
        return FabricMiscUtil.getEntityType(id);
    }
}
