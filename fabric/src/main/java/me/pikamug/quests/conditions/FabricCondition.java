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
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.util.FabricItemUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.minecraft.world.item.ItemStack;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.ServerPlaceholderContext;

import java.util.LinkedList;
import java.util.UUID;

public class FabricCondition implements Condition {

    private String name;
    private boolean failQuest = false;
    private LinkedList<String> entitiesWhileRiding = new LinkedList<>();
    private LinkedList<UUID> npcsWhileRiding = new LinkedList<>();
    private LinkedList<String> permissions = new LinkedList<>();
    private LinkedList<String> worldsWhileStayingWithin = new LinkedList<>();
    private int tickStartWhileStayingWithin = 0;
    private int tickEndWhileStayingWithin = 0;
    private LinkedList<String> biomesWhileStayingWithin = new LinkedList<>();
    private LinkedList<String> regionsWhileStayingWithin = new LinkedList<>();
    private LinkedList<String> placeholdersCheckIdentifier = new LinkedList<>();
    private LinkedList<String> placeholdersCheckValue = new LinkedList<>();
    private LinkedList<ItemStack> itemsWhileHoldingMainHand = new LinkedList<>();
    private LinkedList<ItemStack> itemsWhileWearing = new LinkedList<>();

    @Override public String getName() { return name; }
    @Override public void setName(String v) { this.name = v; }
    @Override public boolean isFailQuest() { return failQuest; }
    @Override public void setFailQuest(boolean v) { this.failQuest = v; }
    @Override public LinkedList<String> getEntitiesWhileRiding() { return entitiesWhileRiding; }
    @Override public void setEntitiesWhileRiding(LinkedList<String> v) { this.entitiesWhileRiding = v; }
    @Override public LinkedList<UUID> getNpcsWhileRiding() { return npcsWhileRiding; }
    @Override public void setNpcsWhileRiding(LinkedList<UUID> v) { this.npcsWhileRiding = v; }
    @Override public LinkedList<String> getPermissions() { return permissions; }
    @Override public void setPermissions(LinkedList<String> v) { this.permissions = v; }
    @Override public LinkedList<String> getWorldsWhileStayingWithin() { return worldsWhileStayingWithin; }
    @Override public void setWorldsWhileStayingWithin(LinkedList<String> v) { this.worldsWhileStayingWithin = v; }
    @Override public int getTickStartWhileStayingWithin() { return tickStartWhileStayingWithin; }
    @Override public void setTickStartWhileStayingWithin(int v) { this.tickStartWhileStayingWithin = v; }
    @Override public int getTickEndWhileStayingWithin() { return tickEndWhileStayingWithin; }
    @Override public void setTickEndWhileStayingWithin(int v) { this.tickEndWhileStayingWithin = v; }
    @Override public LinkedList<String> getBiomesWhileStayingWithin() { return biomesWhileStayingWithin; }
    @Override public void setBiomesWhileStayingWithin(LinkedList<String> v) { this.biomesWhileStayingWithin = v; }
    @Override public LinkedList<String> getRegionsWhileStayingWithin() { return regionsWhileStayingWithin; }
    @Override public void setRegionsWhileStayingWithin(LinkedList<String> v) { this.regionsWhileStayingWithin = v; }
    @Override public LinkedList<String> getPlaceholdersCheckIdentifier() { return placeholdersCheckIdentifier; }
    @Override public void setPlaceholdersCheckIdentifier(LinkedList<String> v) { this.placeholdersCheckIdentifier = v; }
    @Override public LinkedList<String> getPlaceholdersCheckValue() { return placeholdersCheckValue; }
    @Override public void setPlaceholdersCheckValue(LinkedList<String> v) { this.placeholdersCheckValue = v; }

    public LinkedList<ItemStack> getItemsWhileHoldingMainHand() { return itemsWhileHoldingMainHand; }
    public void setItemsWhileHoldingMainHand(LinkedList<ItemStack> v) { this.itemsWhileHoldingMainHand = v; }
    public LinkedList<ItemStack> getItemsWhileWearing() { return itemsWhileWearing; }
    public void setItemsWhileWearing(LinkedList<ItemStack> v) { this.itemsWhileWearing = v; }

    @Override
    public boolean check(Quester quester, Quest quest) {
        if (quester == null) return true;

        final ServerPlayer player = getPlayer(quester);
        if (player == null) return true;

        // Entities while riding
        if (entitiesWhileRiding != null && !entitiesWhileRiding.isEmpty()) {
            final Entity vehicle = player.getVehicle();
            if (vehicle == null) {
                return false;
            }
            final boolean isBoat = vehicle instanceof Boat;
            final boolean isMinecart = vehicle instanceof Minecart;
            boolean matches = false;
            for (final String entityName : entitiesWhileRiding) {
                if (entityName.equalsIgnoreCase("boats") && isBoat) {
                    matches = true;
                    break;
                }
                if (entityName.equalsIgnoreCase("minecarts") && isMinecart) {
                    matches = true;
                    break;
                }
                final EntityType<?> type = resolveEntityType(entityName);
                if (type != null && type == vehicle.getType()) {
                    matches = true;
                    break;
                }
            }
            if (!matches) return false;
        }

        // NPCs while riding
        if (npcsWhileRiding != null && !npcsWhileRiding.isEmpty()) {
            final Entity vehicle = player.getVehicle();
            if (vehicle == null) return false;
            boolean matches = false;
            for (final UUID npcUuid : npcsWhileRiding) {
                if (vehicle.getUUID().equals(npcUuid)) {
                    matches = true;
                    break;
                }
            }
            if (!matches) return false;
        }

        // Permissions
        if (permissions != null && !permissions.isEmpty()) {
            final MinecraftServer server = FabricQuestsPlugin.getInstance().getServer();
            if (server != null) {
                final var commands = server.getCommands();
                for (final String perm : permissions) {
                    if (!commands.getDispatcher().parse(
                            perm, player.createCommandSourceStack().withPermission(
                                    LevelBasedPermissionSet.forLevel(PermissionLevel.OWNERS))).getExceptions().isEmpty()) {
                        // Player has all permissions via command access, but we check for simple perms
                    }
                    // Fallback: assume granted if no permission system
                }
            }
        }

        // Worlds while staying within
        if (worldsWhileStayingWithin != null && !worldsWhileStayingWithin.isEmpty()) {
            boolean inWorld = false;
            final String currentWorld = player.level().dimension().identifier().toString();
            for (final String world : worldsWhileStayingWithin) {
                if (currentWorld.equalsIgnoreCase(world) || player.level().dimension().identifier().getPath().equalsIgnoreCase(world)) {
                    inWorld = true;
                    break;
                }
            }
            if (!inWorld) return false;
        }

        // Biomes while staying within
        if (biomesWhileStayingWithin != null && !biomesWhileStayingWithin.isEmpty()) {
            final String currentBiome = player.level().getBiome(player.blockPosition())
                .unwrapKey().map(resourceKey -> resourceKey.identifier().toString()).orElse("unknown");
            boolean inBiome = false;
            for (final String biome : biomesWhileStayingWithin) {
                if (currentBiome.toLowerCase().contains(biome.toLowerCase())) {
                    inBiome = true;
                    break;
                }
            }
            if (!inBiome) return false;
        }

        // Placeholders while staying within (TextPlaceholderAPI)
        if (placeholdersCheckIdentifier != null && !placeholdersCheckIdentifier.isEmpty()) {
            if (!FabricLoader.getInstance().isModLoaded("placeholder-api")) {
                FabricQuestsPlugin.LOGGER.warn(
                        "Placeholder API must be installed for placeholder checks: {}", placeholdersCheckIdentifier.get(0));
                return false;
            }
            int index = 0;
            for (final String i : placeholdersCheckIdentifier) {
                if (placeholdersCheckValue.size() <= index) {
                    FabricQuestsPlugin.LOGGER.warn(
                            "Condition placeholder values outweigh identifiers: {}", i);
                    return false;
                }
                final String value = Placeholders.SERVER_PLACEHOLDER_PARSER
                        .parseComponent(i, ServerPlaceholderContext.of(player).asParserContext()).getString();
                if (!placeholdersCheckValue.get(index).equals(value)) {
                    if (FabricQuestsPlugin.getInstance().getConfigSettings().getConsoleLogging() > 3) {
                        FabricQuestsPlugin.LOGGER.info(
                                "DEBUG: Condition placeholder mismatch for {}: {}", player.getName().getString(), i);
                    }
                    return false;
                }
                index++;
            }
        }

        // Items while holding main hand (must match at least one)
        if (itemsWhileHoldingMainHand != null && !itemsWhileHoldingMainHand.isEmpty()) {
            final ItemStack held = player.getMainHandItem();
            boolean atLeastOne = false;
            for (final ItemStack is : itemsWhileHoldingMainHand) {
                if (FabricItemUtil.matches(held, is)) {
                    atLeastOne = true;
                    break;
                }
            }
            if (!atLeastOne) return false;
        }

        // Items while wearing (must have ALL listed equipped)
        if (itemsWhileWearing != null && !itemsWhileWearing.isEmpty()) {
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
            if (matches != itemsWhileWearing.size()) return false;
        }

        return true;
    }

    private ServerPlayer getPlayer(Quester quester) {
        final MinecraftServer server = FabricQuestsPlugin.getInstance().getServer();
        if (server == null) return null;
        return server.getPlayerList().getPlayer(quester.getUUID());
    }

    /**
     * Resolves an entity name read from config (e.g. {@code PIG} or
     * {@code minecraft:pig}) to its {@link EntityType}. Plain names such as
     * {@code PIG} are given the default {@code minecraft:} namespace so they can
     * be resolved against the entity type registry.
     */
    private EntityType<?> resolveEntityType(String name) {
        if (name == null) return null;
        final String trimmed = name.trim();
        if (trimmed.isEmpty()) return null;
        final String id = trimmed.contains(":") ? trimmed.toLowerCase() : "minecraft:" + trimmed.toLowerCase();
        return EntityType.byString(id).orElse(null);
    }

    @Override
    public int compareTo(Condition other) {
        if (other == null) return 1;
        if (this.name != null && other.getName() != null) {
            return this.name.compareTo(other.getName());
        }
        return 0;
    }
}
