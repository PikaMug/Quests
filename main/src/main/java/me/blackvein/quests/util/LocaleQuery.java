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

package me.blackvein.quests.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import me.blackvein.quests.Quests;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Villager.Career;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;

@SuppressWarnings("deprecation")
public class LocaleQuery {
    private static Class<?> craftMagicNumbers = null;
    private static Class<?> itemClazz = null;
    private final Quests plugin;
    private static boolean oldVersion = false;
    private static boolean hasBasePotionData = false;
    private Map<String, String> oldBlocks = getBlockKeys();
    private Map<String, String> oldItems = getItemKeys();
    private Map<String, String> oldPotions_18 = getPotionKeys_18();
    private Map<String, String> oldPotions = getPotionKeys();
    private Map<String, String> oldLingeringPotions = getLingeringPotionKeys();
    private Map<String, String> oldSplashPotions = getSplashPotionKeys();
    private Map<String, String> oldEntities = getEntityKeys();
    
    public LocaleQuery(Quests plugin) {
        this.plugin = plugin;
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            craftMagicNumbers = Class.forName("org.bukkit.craftbukkit.{v}.util.CraftMagicNumbers".replace("{v}", version));
            itemClazz = Class.forName("net.minecraft.server.{v}.Item".replace("{v}", version));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    public void setBukkitVersion(String bukkitVersion) {
        oldVersion = isBelow113(bukkitVersion);
        if (Material.getMaterial("LINGERING_POTION") != null) {
            // Bukkit version is 1.9+
            hasBasePotionData = true;
        }
    }
    
    /**
     * Send message with item name translated to the client's locale.
     * Material is required. Durability arg is arbitrary for 1.13+
     * and can be ignored by setting to a value less than 0.
     * Enchantments & meta are optional and may be left null or empty,
     * but note that most Potions use meta for 1.13+.<p>
     * 
     * Message should contain {@code <item>} string for replacement by
     * this method (along with applicable {@code <enchantment>} strings).
     * 
     * @param player The player whom the message is to be sent to
     * @param message The message to be sent to the player
     * @param material The item to be translated
     * @param durability Durability for the item being translated
     * @param enchantments Enchantments for the item being translated
     * @param meta ItemMeta for the item being translated
     */
    public boolean sendMessage(Player player, String message, Material material, short durability, Map<Enchantment, Integer> enchantments, ItemMeta meta) {
        if (material == null) {
            return false;
        }
        String matKey = "";
        String[] enchKeys = enchantments != null ? new String[enchantments.size()] : null;
        if (oldVersion) {
            if (material.isBlock()) {
                if (durability >= 0 && oldBlocks.containsKey(material.name() + "." + durability)) {
                    matKey = oldBlocks.get(material.name() + "." + durability);
                } else if (oldBlocks.containsKey(material.name())) {
                    matKey = oldBlocks.get(material.name());
                } else {
                    plugin.getLogger().severe("Block not found: " + material.name() + "." + durability);
                    return false;
                }
            } else {
                ItemStack i = new ItemStack(material, 1, durability);
                if (durability >= 0 && i.getItemMeta() instanceof PotionMeta) {
                    if (hasBasePotionData) {
                        if (material.equals(Material.POTION)) {
                            matKey = oldPotions.get(((PotionMeta)i.getItemMeta()).getBasePotionData().getType().name());
                        } else if (material.equals(Material.LINGERING_POTION)) {
                            matKey = oldLingeringPotions.get(((PotionMeta)i.getItemMeta()).getBasePotionData().getType().name());
                        } else if (material.equals(Material.SPLASH_POTION)) {
                            matKey = oldSplashPotions.get(((PotionMeta)i.getItemMeta()).getBasePotionData().getType().name());
                        }
                    } else if (new Potion(durability).getType() != null) {
                        matKey = oldPotions_18.get(new Potion(durability).getType().name());
                    }
                } else if (durability >= 0 && oldItems.containsKey(material.name() + "." + durability)) {
                    matKey = oldItems.get(material.name() + "." + durability);
                } else if (oldItems.containsKey(material.name())) {
                    matKey = oldItems.get(material.name());
                } else {
                    plugin.getLogger().severe("Item not found: " + material.name() + "." + durability);
                    return false;
                }
            }
            if (enchantments != null && !enchantments.isEmpty()) {
                int count = 0;
                for (Enchantment e : enchantments.keySet()) {
                    enchKeys[count] = "enchantment." + e.getName().toLowerCase().replace("_", ".")
                        .replace("environmental", "all").replace("protection", "protect");
                    count++;
                }
            }
        } else {
            try {
                matKey = queryMaterial(material);
            } catch (Exception ex) {
                plugin.getLogger().severe("Unable to query Material: " + material.name());
                return false;
            }
            if (meta != null && meta instanceof PotionMeta) {
                matKey = "item.minecraft.potion.effect." + ((PotionMeta)meta).getBasePotionData().getType().name().toLowerCase()
                        .replace("regen", "regeneration").replace("speed", "swiftness");
            }
            if (enchantments != null && !enchantments.isEmpty()) {
                int count = 0;
                for (Enchantment e : enchantments.keySet()) {
                    enchKeys[count] = "enchantment.minecraft." + e.toString().toLowerCase();
                    count++;
                }
            }
        }
        String msg = message.replace("<item>", "\",{\"translate\":\"" + matKey + "\"},\"");
        if (enchKeys != null && enchKeys.length > 0) {
            for (String ek : enchKeys) {
                msg.replaceFirst("<enchantment>", "\",{\"translate\":\"" + ek + "\"},\"");
            }
        }
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + player.getName() + " [\"" + msg + "\"]");
        return true;
    }
    
    /**
     * Send message with item name translated to the client's locale.
     * Material is required. Durability arg is arbitrary for 1.13+
     * and can be ignored by setting to a value less than 0.
     * Enchantments are optional and may be left null or empty.<p>
     * 
     * Message should contain {@code <item>} string for replacement by
     * this method (along with applicable {@code <enchantment>} strings).
     * 
     * @param player The player whom the message is to be sent to
     * @param message The message to be sent to the player
     * @param material The item to be translated
     * @param durability Durability for the item being translated
     * @param enchantments Enchantments for the item being translated
     */
    public boolean sendMessage(Player player, String message, Material material, short durability, Map<Enchantment, Integer> enchantments) {
        return sendMessage(player, message, material, durability, enchantments, null);
    }
    
    /**
     * Send message with enchantments translated to the client's locale.
     * Map of Enchantment+level is required.
     * 
     * Message should contain one {@code <enchantment>} string for each
     * replacement by this method.
     * 
     * @param player The player whom the message is to be sent to
     * @param message The message to be sent to the player
     * @param enchantments Enchantments for the item being translated
     */
    public boolean sendMessage(Player player, String message, Map<Enchantment, Integer> enchantments) {
        if (enchantments == null) {
            return false;
        }
        String[] enchKeys = enchantments != null ? new String[enchantments.size()] : null;
        if (oldVersion) {
            if (enchantments != null && !enchantments.isEmpty()) {
                int count = 0;
                for (Enchantment e : enchantments.keySet()) {
                    enchKeys[count] = "enchantment." + e.getName().toLowerCase().replace("_", ".")
                        .replace("environmental", "all").replace("protection", "protect");
                    count++;
                }
            }
        } else {
            if (enchantments != null && !enchantments.isEmpty()) {
                int count = 0;
                for (Enchantment e : enchantments.keySet()) {
                    enchKeys[count] = "enchantment.minecraft." + e.toString().toLowerCase();
                    count++;
                }
            }
        }
        String msg = message;
        if (enchKeys != null && enchKeys.length > 0) {
            for (String ek : enchKeys) {
                msg.replaceFirst("<enchantment>", "\",{\"translate\":\"" + ek + "\"},\"");
            }
        }
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + player.getName() + " [\"" + msg + "\"]");
        return true;
    }
    
    /**
     * Send message with entity name translated to the client's locale.
     * EntityType is required.<p>
     * 
     * Message should contain {@code <mob>}
     * string for replacement by this method.
     * 
     * @param player The player whom the message is to be sent to
     * @param message The message to be sent to the player
     * @param type The entity type to be translated
     * @param extra Career, Ocelot, or Rabbit type if applicable
     */
    public boolean sendMessage(Player player, String message, EntityType type, String extra) {
        if (type == null ) {
            return false;
        }
        String key = "";
        if (oldVersion) {
            if (type.name().equals("VILLAGER") && Career.valueOf(extra) != null) {
                key = oldEntities.get(type.name() + "." + Career.valueOf(extra).name());
            } else if (type.name().equals("OCELOT") && Ocelot.Type.valueOf(extra) != null) {
                key = oldEntities.get(type.name() + "." + Ocelot.Type.valueOf(extra).name());
            } else if (type.name().equals("RABBIT") && Rabbit.Type.valueOf(extra) != null) {
                if (Rabbit.Type.valueOf(extra).equals(Rabbit.Type.THE_KILLER_BUNNY)) {
                    key = oldEntities.get(type.name() + "." + Rabbit.Type.valueOf(extra).name());
                }
            } else {
                key = oldEntities.get(type.name());
            }
        } else {
            if (type.name().equals("PIG_ZOMBIE")) {
                key = "entity.minecraft.zombie_pigman";
            } else {
                key = "entity.minecraft." + type.toString().toLowerCase();
            }
        }
        String msg = message.replace("<mob>", "\",{\"translate\":\"" + key + "\"},\"");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + player.getName() + " [\"" + msg + "\"]");
        return true;
    }
    
    /**
     * Gets the key name of the specified material as it would appear in a Minecraft lang file.
     * 
     * @param material the material to check
     * @return the raw key
     * @throws IllegalArgumentException if an item with that material could not be found
     */
    public String queryMaterial(Material material) throws IllegalArgumentException{
        try {
            Object item = null;
            Method m = craftMagicNumbers.getDeclaredMethod("getItem", material.getClass());
            m.setAccessible(true);
            item = m.invoke(craftMagicNumbers, material);
            if (item == null) {
                throw new IllegalArgumentException(material.name() + " material could not be queried!");
            }                          
            String name = (String) itemClazz.getMethod("getName").invoke(item);
            return name;
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Checks whether the server's Bukkit version supports use of the ItemMeta#getBasePotionData method.
     * 
     * @return true if Bukkit version is at 1.9 or above
     */
    public boolean hasBasePotionData() {
        return hasBasePotionData;
    }
    
    /**
     * Checks whether the server's Bukkit version is below 1.13.
     * 
     * @return true if Bukkit version is at 1.12.2 or below
     */
    public static boolean isBelow113(String bukkitVersion) {
        return _isBelow113(bukkitVersion);
    }
    
    private static boolean _isBelow113(String bukkitVersion) {
        if (bukkitVersion.matches("^[0-9.]+$")) {
            switch(bukkitVersion) {
            case "1.12.2" :
            case "1.12.1" :
            case "1.12" :
            case "1.11.2" :
            case "1.11.1" :
            case "1.11" :
            case "1.10.2" :
            case "1.10.1" :
            case "1.10" :
            case "1.9.4" :
            case "1.9.3" :
            case "1.9.2" :
            case "1.9.1" :
            case "1.9" :
            case "1.8.9" :
            case "1.8.8" :
            case "1.8.7" :
            case "1.8.6" :
            case "1.8.5" :
            case "1.8.4" :
            case "1.8.3" :
            case "1.8.2" :
            case "1.8.1" :
            case "1.8" :
            case "1.7.10" :
            case "1.7.9" :
            case "1.7.2" :
                return true;
            default:
                // Bukkit version is 1.13+ or unsupported
                return false;
            }
        }
        Bukkit.getLogger().severe("Quests received invalid Bukkit version " + bukkitVersion);
        return false;
    }
    
    private LinkedHashMap<String, String> getBlockKeys() {
        LinkedHashMap<String, String> keys = new LinkedHashMap<String, String>();
        keys.put("AIR", "tile.air.name");
        keys.put("BARRIER", "tile.barrier.name");
        keys.put("STONE", "tile.stone.stone.name");
        keys.put("STONE.1", "tile.stone.granite.name");
        keys.put("STONE.2", "tile.stone.graniteSmooth.name");
        keys.put("STONE.3", "tile.stone.diorite.name");
        keys.put("STONE.4", "tile.stone.dioriteSmooth.name");
        keys.put("STONE.5", "tile.stone.andesite.name");
        keys.put("STONE.6", "tile.stone.andesiteSmooth.name");
        keys.put("HAY_BLOCK", "tile.hayBlock.name");
        keys.put("GRASS", "tile.grass.name");
        keys.put("DIRT", "tile.dirt.name");
        keys.put("DIRT.0", "tile.dirt.default.name");
        keys.put("DIRT.1", "tile.dirt.coarse.name");
        keys.put("DIRT.2", "tile.dirt.podzol.name");
        keys.put("COBBLESTONE", "tile.stonebrick.name");
        keys.put("WOOD", "tile.wood.name");
        keys.put("WOOD.0", "tile.wood.oak.name");
        keys.put("WOOD.1", "tile.wood.spruce.name");
        keys.put("WOOD.2", "tile.wood.birch.name");
        keys.put("WOOD.3", "tile.wood.jungle.name");
        keys.put("WOOD.4", "tile.wood.acacia.name");
        keys.put("WOOD.5", "tile.wood.big_oak.name");
        keys.put("SAPLING.0", "tile.sapling.oak.name");
        keys.put("SAPLING.1", "tile.sapling.spruce.name");
        keys.put("SAPLING.2", "tile.sapling.birch.name");
        keys.put("SAPLING.3", "tile.sapling.jungle.name");
        keys.put("SAPLING.3", "tile.sapling.acacia.name");
        keys.put("SAPLING.4", "tile.sapling.big_oak.name");
        keys.put("DEAD_BUSH", "tile.deadbush.name");
        keys.put("BEDROCK", "tile.bedrock.name");
        keys.put("WATER", "tile.water.name");
        keys.put("LAVA", "tile.lava.name");
        keys.put("SAND", "tile.sand.name");
        keys.put("SAND.0", "tile.sand.default.name");
        keys.put("SAND.1", "tile.sand.red.name");
        keys.put("SANDSTONE", "tile.sandStone.name");
        keys.put("SANDSTONE.0", "tile.sandStone.default.name");
        keys.put("SANDSTONE.1", "tile.sandStone.chiseled.name");
        keys.put("SANDSTONE.2", "tile.sandStone.smooth.name");
        keys.put("RED_SANDSTONE", "tile.redSandStone.name");
        keys.put("RED_SANDSTONE.0", "tile.redSandStone.default.name");
        keys.put("RED_SANDSTONE.1", "tile.redSandStone.chiseled.name");
        keys.put("RED_SANDSTONE.2", "tile.redSandStone.smooth.name");
        keys.put("GRAVEL", "tile.gravel.name");
        keys.put("GOLD_ORE", "tile.oreGold.name");
        keys.put("IRON_ORE", "tile.oreIron.name");
        keys.put("COAL_ORE", "tile.oreCoal.name");
        keys.put("LOG", "tile.log.name");
        keys.put("LOG.0", "tile.log.oak.name");
        keys.put("LOG.1", "tile.log.spruce.name");
        keys.put("LOG.2", "tile.log.birch.name");
        keys.put("LOG.3", "tile.log.jungle.name");
        keys.put("LOG_2.0", "tile.log.acacia.name");
        keys.put("LOG_2.1", "tile.log.big_oak.name");
        keys.put("LEAVES"," tile.leaves.name");
        keys.put("LEAVES.0", "tile.leaves.oak.name");
        keys.put("LEAVES.1", "tile.leaves.spruce.name");
        keys.put("LEAVES.2", "tile.leaves.birch.name");
        keys.put("LEAVES.3", "tile.leaves.jungle.name");
        keys.put("LEAVES_2.0", "tile.leaves.acacia.name");
        keys.put("LEAVES2.1", "tile.leaves.big_oak.name");
        keys.put("LONG_GRASS", "tile.tallgrass.name");
        keys.put("LONG_GRASS.2", "tile.tallgrass.shrub.name");
        keys.put("LONG_GRASS.0", "tile.tallgrass.grass.name");
        keys.put("LONG_GRASS.1", "tile.tallgrass.fern.name");
        keys.put("SPONGE", "tile.sponge.dry.name");
        keys.put("SPONGE.1", "tile.sponge.wet.name");
        keys.put("GLASS", "tile.glass.name");
        keys.put("STAINED_GLASS", "tile.stainedGlass.name");
        keys.put("STAINED_GLASS.15", "tile.stainedGlass.black.name");
        keys.put("STAINED_GLASS.14", "tile.stainedGlass.red.name");
        keys.put("STAINED_GLASS.13", "tile.stainedGlass.green.name");
        keys.put("STAINED_GLASS.12", "tile.stainedGlass.brown.name");
        keys.put("STAINED_GLASS.11", "tile.stainedGlass.blue.name");
        keys.put("STAINED_GLASS.10", "tile.stainedGlass.purple.name");
        keys.put("STAINED_GLASS.9", "tile.stainedGlass.cyan.name");
        keys.put("STAINED_GLASS.8", "tile.stainedGlass.silver.name");
        keys.put("STAINED_GLASS.7", "tile.stainedGlass.gray.name");
        keys.put("STAINED_GLASS.6", "tile.stainedGlass.pink.name");
        keys.put("STAINED_GLASS.5", "tile.stainedGlass.lime.name");
        keys.put("STAINED_GLASS.4", "tile.stainedGlass.yellow.name");
        keys.put("STAINED_GLASS.3", "tile.stainedGlass.lightBlue.name");
        keys.put("STAINED_GLASS.2", "tile.stainedGlass.magenta.name");
        keys.put("STAINED_GLASS.1", "tile.stainedGlass.orange.name");
        keys.put("STAINED_GLASS.0", "tile.stainedGlass.white.name");
        keys.put("STAINED_GLASS_PANE", "tile.thinStainedGlass.name");
        keys.put("STAINED_GLASS_PANE.15", "tile.thinStainedGlass.black.name");
        keys.put("STAINED_GLASS_PANE.14", "tile.thinStainedGlass.red.name");
        keys.put("STAINED_GLASS_PANE.13", "tile.thinStainedGlass.green.name");
        keys.put("STAINED_GLASS_PANE.12", "tile.thinStainedGlass.brown.name");
        keys.put("STAINED_GLASS_PANE.11", "tile.thinStainedGlass.blue.name");
        keys.put("STAINED_GLASS_PANE.10", "tile.thinStainedGlass.purple.name");
        keys.put("STAINED_GLASS_PANE.9", "tile.thinStainedGlass.cyan.name");
        keys.put("STAINED_GLASS_PANE.8", "tile.thinStainedGlass.silver.name");
        keys.put("STAINED_GLASS_PANE.7", "tile.thinStainedGlass.gray.name");
        keys.put("STAINED_GLASS_PANE.6", "tile.thinStainedGlass.pink.name");
        keys.put("STAINED_GLASS_PANE.5", "tile.thinStainedGlass.lime.name");
        keys.put("STAINED_GLASS_PANE.4", "tile.thinStainedGlass.yellow.name");
        keys.put("STAINED_GLASS_PANE.3", "tile.thinStainedGlass.lightBlue.name");
        keys.put("STAINED_GLASS_PANE.2", "tile.thinStainedGlass.magenta.name");
        keys.put("STAINED_GLASS_PANE.1", "tile.thinStainedGlass.orange.name");
        keys.put("STAINED_GLASS_PANE.0", "tile.thinStainedGlass.white.name");
        keys.put("THIN_GLASS", "tile.thinGlass.name");
        keys.put("WOOL", "tile.cloth.name");
        keys.put("YELLOW_FLOWER", "tile.flower1.name");
        keys.put("YELLOW_FLOWER.0", "tile.flower1.dandelion.name");
        keys.put("RED_ROSE", "tile.flower2.name");
        keys.put("RED_ROSE.0", "tile.flower2.poppy.name");
        keys.put("RED_ROSE.1", "tile.flower2.blueOrchid.name");
        keys.put("RED_ROSE.2", "tile.flower2.allium.name");
        keys.put("RED_ROSE.3", "tile.flower2.houstonia.name");
        keys.put("RED_ROSE.4", "tile.flower2.tulipRed.name");
        keys.put("RED_ROSE.5", "tile.flower2.tulipOrange.name");
        keys.put("RED_ROSE.6", "tile.flower2.tulipWhite.name");
        keys.put("RED_ROSE.7", "tile.flower2.tulipPink.name");
        keys.put("RED_ROSE.8", "tile.flower2.oxeyeDaisy.name");
        keys.put("DOUBLE_PLANT", "tile.doublePlant.name");
        keys.put("DOUBLE_PLANT.0", "tile.doublePlant.sunflower.name");
        keys.put("DOUBLE_PLANT.1", "tile.doublePlant.syringa.name");
        keys.put("DOUBLE_PLANT.2", "tile.doublePlant.grass.name");
        keys.put("DOUBLE_PLANT.3", "tile.doublePlant.fern.name");
        keys.put("DOUBLE_PLANT.4", "tile.doublePlant.rose.name");
        keys.put("DOUBLE_PLANT.5", "tile.doublePlant.paeonia.name");
        keys.put("BROWN_MUSHROOM", "tile.mushroom.name");
        keys.put("GOLD_BLOCK", "tile.blockGold.name");
        keys.put("IRON_BLOCK", "tile.blockIron.name");
        keys.put("STONE_SLAB", "tile.stoneSlab.name");
        keys.put("STONE_SLAB.0", "tile.stoneSlab.stone.name");
        keys.put("STONE_SLAB.1", "tile.stoneSlab.sand.name");
        keys.put("STONE_SLAB.2", "tile.stoneSlab.wood.name");
        keys.put("STONE_SLAB.3", "tile.stoneSlab.cobble.name");
        keys.put("STONE_SLAB.4", "tile.stoneSlab.brick.name");
        keys.put("STONE_SLAB.5", "tile.stoneSlab.smoothStoneBrick.name");
        keys.put("STONE_SLAB.6", "tile.stoneSlab.netherBrick.name");
        keys.put("STONE_SLAB.7", "tile.stoneSlab.quartz.name");
        keys.put("STONE_SLAB2", "tile.stoneSlab2.red_sandstone.name");
        keys.put("WOOD_STEP", "tile.woodSlab.name");
        keys.put("WOOD_STEP.0", "tile.woodSlab.oak.name");
        keys.put("WOOD_STEP.1", "tile.woodSlab.spruce.name");
        keys.put("WOOD_STEP.2", "tile.woodSlab.birch.name");
        keys.put("WOOD_STEP.3", "tile.woodSlab.jungle.name");
        keys.put("WOOD_STEP.4", "tile.woodSlab.acacia.name");
        keys.put("WOOD_STEP.5", "tile.woodSlab.big_oak.name");
        keys.put("BRICK", "tile.brick.name");
        keys.put("TNT", "tile.tnt.name");
        keys.put("BOOKSHELF", "tile.bookshelf.name");
        keys.put("MOSSY_COBBLESTONE", "tile.stoneMoss.name");
        keys.put("OBSIDIAN", "tile.obsidian.name");
        keys.put("TORCH", "tile.torch.name");
        keys.put("FIRE", "tile.fire.name");
        keys.put("MOB_SPAWNER", "tile.mobSpawner.name");
        keys.put("WOOD_STAIRS", "tile.stairsWood.name");
        keys.put("SPRUCE_WOOD_STAIRS", "tile.stairsWoodSpruce.name");
        keys.put("BIRCH_WOOD_STAIRS", "tile.stairsWoodBirch.name");
        keys.put("JUNGLE_WOOD_STAIRS", "tile.stairsWoodJungle.name");
        keys.put("ACACIA_STAIRS", "tile.stairsWoodAcacia.name");
        keys.put("DARK_OAK_STAIRS", "tile.stairsWoodDarkOak.name");
        keys.put("CHEST", "tile.chest.name");
        keys.put("TRAPPED_CHEST", "tile.chestTrap.name");
        keys.put("REDSTONE_WIRE", "tile.redstoneDust.name");
        keys.put("DIAMOND_ORE", "tile.oreDiamond.name");
        keys.put("COAL_BLOCK", "tile.blockCoal.name");
        keys.put("DIAMOND_BLOCK", "tile.blockDiamond.name");
        keys.put("WORKBENCH", "tile.workbench.name");
        keys.put("CROPS", "tile.crops.name");
        keys.put("SOIL", "tile.farmland.name");
        keys.put("FURNACE", "tile.furnace.name");
        keys.put("SIGN", "tile.sign.name");
        keys.put("WOODEN_DOOR", "tile.doorWood.name");
        keys.put("LADDER", "tile.ladder.name");
        keys.put("RAILS", "tile.rail.name");
        keys.put("POWERED_RAIL", "tile.goldenRail.name");
        keys.put("ACTIVATOR_RAIL", "tile.activatorRail.name");
        keys.put("DETECTOR_RAIL", "tile.detectorRail.name");
        keys.put("COBBLESTONE_STAIRS", "tile.stairsStone.name");
        keys.put("SANDSTONE_STAIRS", "tile.stairsSandStone.name");
        keys.put("RED_SANDSTONE_STAIRS", "tile.stairsRedSandStone.name");
        keys.put("LEVER", "tile.lever.name");
        keys.put("STONE_PLATE", "tile.pressurePlateStone.name");
        keys.put("WOOD_PLATE", "tile.pressurePlateWood.name");
        keys.put("GOLD_PLATE", "tile.weightedPlate_light.name");
        keys.put("IRON_PLATE", "tile.weightedPlate_heavy.name");
        keys.put("IRON_DOOR_BLOCK", "tile.doorIron.name");
        keys.put("REDSTONE_ORE", "tile.oreRedstone.name");
        keys.put("REDSTONE_TORCH_ON", "tile.notGate.name");
        keys.put("REDSTONE_TORCH_OFF", "tile.notGate.name"); // added
        keys.put("STONE_BUTTON", "tile.button.name");
        keys.put("WOOD_BUTTON", "tile.button.name"); //added
        keys.put("SNOW", "tile.snow.name");
        keys.put("SNOW_BLOCK", "tile.snow.name"); //added
        keys.put("CARPET", "tile.woolCarpet.name");
        keys.put("CARPET.15", "tile.woolCarpet.black.name");
        keys.put("CARPET.14", "tile.woolCarpet.red.name");
        keys.put("CARPET.13", "tile.woolCarpet.green.name");
        keys.put("CARPET.12", "tile.woolCarpet.brown.name");
        keys.put("CARPET.11", "tile.woolCarpet.blue.name");
        keys.put("CARPET.10", "tile.woolCarpet.purple.name");
        keys.put("CARPET.9", "tile.woolCarpet.cyan.name");
        keys.put("CARPET.8", "tile.woolCarpet.silver.name");
        keys.put("CARPET.7", "tile.woolCarpet.gray.name");
        keys.put("CARPET.6", "tile.woolCarpet.pink.name");
        keys.put("CARPET.5", "tile.woolCarpet.lime.name");
        keys.put("CARPET.4", "tile.woolCarpet.yellow.name");
        keys.put("CARPET.3", "tile.woolCarpet.lightBlue.name");
        keys.put("CARPET.2", "tile.woolCarpet.magenta.name");
        keys.put("CARPET.1", "tile.woolCarpet.orange.name");
        keys.put("CARPET.0", "tile.woolCarpet.white.name");
        keys.put("ICE", "tile.ice.name");
        keys.put("FROSTED_ICE", "tile.frostedIce.name");
        keys.put("PACKED_ICE", "tile.icePacked.name");
        keys.put("CACTUS", "tile.cactus.name");
        keys.put("CLAY", "tile.clay.name");
        keys.put("STAINED_CLAY", "tile.clayHardenedStained.name");
        keys.put("STAINED_CLAY.15", "tile.clayHardenedStained.black.name");
        keys.put("STAINED_CLAY.14", "tile.clayHardenedStained.red.name");
        keys.put("STAINED_CLAY.13", "tile.clayHardenedStained.green.name");
        keys.put("STAINED_CLAY.12", "tile.clayHardenedStained.brown.name");
        keys.put("STAINED_CLAY.11", "tile.clayHardenedStained.blue.name");
        keys.put("STAINED_CLAY.10", "tile.clayHardenedStained.purple.name");
        keys.put("STAINED_CLAY.9", "tile.clayHardenedStained.cyan.name");
        keys.put("STAINED_CLAY.8", "tile.clayHardenedStained.silver.name");
        keys.put("STAINED_CLAY.7", "tile.clayHardenedStained.gray.name");
        keys.put("STAINED_CLAY.6", "tile.clayHardenedStained.pink.name");
        keys.put("STAINED_CLAY.5", "tile.clayHardenedStained.lime.name");
        keys.put("STAINED_CLAY.4", "tile.clayHardenedStained.yellow.name");
        keys.put("STAINED_CLAY.3", "tile.clayHardenedStained.lightBlue.name");
        keys.put("STAINED_CLAY.2", "tile.clayHardenedStained.magenta.name");
        keys.put("STAINED_CLAY.1", "tile.clayHardenedStained.orange.name");
        keys.put("STAINED_CLAY.0", "tile.clayHardenedStained.white.name");
        keys.put("HARD_CLAY", "tile.clayHardened.name");
        keys.put("SUGAR_CANE_BLOCK", "tile.reeds.name");
        keys.put("JUKEBOX", "tile.jukebox.name");
        keys.put("FENCE", "tile.fence.name");
        keys.put("SPRUCE_FENCE", "tile.spruceFence.name");
        keys.put("BIRCH_FENCE", "tile.birchFence.name");
        keys.put("JUNGLE_FENCE", "tile.jungleFence.name");
        keys.put("DARK_OAK_FENCE", "tile.darkOakFence.name");
        keys.put("ACACIA_FENCE", "tile.acaciaFence.name");
        keys.put("FENCE_GATE", "tile.fenceGate.name");
        keys.put("SPRUCE_FENCE_GATE", "tile.spruceFenceGate.name");
        keys.put("BIRCH_FENCE_GATE", "tile.birchFenceGate.name");
        keys.put("JUNGLE_FENCE_GATE", "tile.jungleFenceGate.name");
        keys.put("DARK_OAK_FENCE_GATE", "tile.darkOakFenceGate.name");
        keys.put("ACACIA_FENCE_GATE", "tile.acaciaFenceGate.name");
        keys.put("PUMPKIN_STEM", "tile.pumpkinStem.name");
        keys.put("PUMPKIN", "tile.pumpkin.name");
        keys.put("JACK_O_LANTERN", "tile.litpumpkin.name");
        keys.put("NETHERRACK", "tile.hellrock.name");
        keys.put("SOUL_SAND", "tile.hellsand.name");
        keys.put("GLOWSTONE", "tile.lightgem.name");
        keys.put("PORTAL", "tile.portal.name");
        keys.put("WOOL.15", "tile.cloth.black.name");
        keys.put("WOOL.14", "tile.cloth.red.name");
        keys.put("WOOL.13", "tile.cloth.green.name");
        keys.put("WOOL.12", "tile.cloth.brown.name");
        keys.put("WOOL.11", "tile.cloth.blue.name");
        keys.put("WOOL.10", "tile.cloth.purple.name");
        keys.put("WOOL.9", "tile.cloth.cyan.name");
        keys.put("WOOL.8", "tile.cloth.silver.name");
        keys.put("WOOL.7", "tile.cloth.gray.name");
        keys.put("WOOL.6", "tile.cloth.pink.name");
        keys.put("WOOL.5", "tile.cloth.lime.name");
        keys.put("WOOL.4", "tile.cloth.yellow.name");
        keys.put("WOOL.3", "tile.cloth.lightBlue.name");
        keys.put("WOOL.2", "tile.cloth.magenta.name");
        keys.put("WOOL.1", "tile.cloth.orange.name");
        keys.put("WOOL.0", "tile.cloth.white.name");
        keys.put("LAPIS_BLOCK", "tile.oreLapis.name");
        keys.put("LAPIS_ORE", "tile.blockLapis.name");
        keys.put("DISPENSER", "tile.dispenser.name");
        keys.put("DROPPER", "tile.dropper.name");
        keys.put("NOTE_BLOCK", "tile.musicBlock.name");
        keys.put("CAKE", "tile.cake.name");
        keys.put("LOCKED_CHEST", "tile.lockedchest.name");
        keys.put("TRAP_DOOR", "tile.trapdoor.name");
        keys.put("IRON_TRAPDOOR", "tile.ironTrapdoor.name");
        keys.put("WEB", "tile.web.name");
        keys.put("SMOOTH_BRICK", "tile.stonebricksmooth.name");
        keys.put("SMOOTH_BRICK.0", "tile.stonebricksmooth.default.name");
        keys.put("SMOOTH_BRICK.2", "tile.stonebricksmooth.mossy.name");
        keys.put("SMOOTH_BRICK.3", "tile.stonebricksmooth.cracked.name");
        keys.put("SMOOTH_BRICK.4", "tile.stonebricksmooth.chiseled.name");
        keys.put("MONSTER_EGGS", "tile.monsterStoneEgg.name");
        keys.put("MONSTER_EGGS.0", "tile.monsterStoneEgg.stone.name");
        keys.put("MONSTER_EGGS.1", "tile.monsterStoneEgg.cobble.name");
        keys.put("MONSTER_EGGS.2", "tile.monsterStoneEgg.brick.name");
        keys.put("MONSTER_EGGS.3", "tile.monsterStoneEgg.mossybrick.name");
        keys.put("MONSTER_EGGS.4", "tile.monsterStoneEgg.crackedbrick.name");
        keys.put("MONSTER_EGGS.5", "tile.monsterStoneEgg.chiseledbrick.name");
        keys.put("PISTON_BASE", "tile.pistonBase.name");
        keys.put("PISTON_STICKY_BASE", "tile.pistonStickyBase.name");
        keys.put("IRON_FENCE", "tile.fenceIron.name");
        keys.put("MELON_BLOCK", "tile.melon.name");
        keys.put("BRICK_STAIRS", "tile.stairsBrick.name");
        keys.put("SMOOTH_STAIRS", "tile.stairsStoneBrickSmooth.name");
        keys.put("VINE", "tile.vine.name");
        keys.put("NETHER_BRICK", "tile.netherBrick.name");
        keys.put("NETHER_FENCE", "tile.netherFence.name");
        keys.put("NETHER_BRICK_STAIRS", "tile.stairsNetherBrick.name");
        keys.put("NETHER_STALK", "tile.netherStalk.name");
        keys.put("CAULDRON", "tile.cauldron.name");
        keys.put("ENCHANTMENT_TABLE", "tile.enchantmentTable.name");
        keys.put("ANVIL", "tile.anvil.name");
        keys.put("ANVIL.0", "tile.anvil.intact.name");
        keys.put("ANVIL.1", "tile.anvil.slightlyDamaged.name");
        keys.put("ANVIL.2", "tile.anvil.veryDamaged.name");
        keys.put("ENDER_STONE", "tile.whiteStone.name");
        keys.put("ENDER_PORTAL_FRAME", "tile.endPortalFrame.name");
        keys.put("MYCEL", "tile.mycel.name");
        keys.put("WATER_LILY", "tile.waterlily.name");
        keys.put("DRAGON_EGG", "tile.dragonEgg.name");
        keys.put("REDSTONE_LAMP_OFF", "tile.redstoneLight.name");
        keys.put("REDSTONE_LAMP_ON", "tile.redstoneLight.name"); //added
        keys.put("COCOA", "tile.cocoa.name");
        keys.put("ENDER_CHEST", "tile.enderChest.name");
        keys.put("EMERALD_ORE", "tile.oreEmerald.name");
        keys.put("EMERLAND_BLOCK", "tile.blockEmerald.name");
        keys.put("REDSTONE_ORE", "tile.blockRedstone.name");
        keys.put("TRIPWARE", "tile.tripWire.name");
        keys.put("TRIPWIRE_HOOK", "tile.tripWireSource.name");
        keys.put("COMMAND", "tile.commandBlock.name");
        keys.put("COMMAND_REPEATING", "tile.repeatingCommandBlock.name");
        keys.put("COMMAND_CHAIN", "tile.chainCommandBlock.name");
        keys.put("BEACON", "tile.beacon.name");
        keys.put("COBBLE_WALL", "tile.cobbleWall.normal.name");
        keys.put("COBBLE_WALL.1", "tile.cobbleWall.mossy.name");
        keys.put("CARROT", "tile.carrots.name");
        keys.put("POTATO", "tile.potatoes.name");
        keys.put("DAYLIGHT_DETECTOR", "tile.daylightDetector.name");
        keys.put("QUARTZ_ORE", "tile.netherquartz.name");
        keys.put("HOPPER", "tile.hopper.name");
        keys.put("QUARTZ_BLOCK", "tile.quartzBlock.name");
        keys.put("QUARTZ_BLOCK.0", "tile.quartzBlock.default.name");
        keys.put("QUARTZ_BLOCK.1", "tile.quartzBlock.chiseled.name");
        keys.put("QUARTZ_BLOCK.2", "tile.quartzBlock.lines.name");
        keys.put("QUARTZ_STAIRS", "tile.stairsQuartz.name");
        keys.put("SLIME_BLOCK", "tile.slime.name");
        keys.put("PRISMARINE", "tile.prismarine.rough.name");
        keys.put("PRISMARINE.2", "tile.prismarine.bricks.name");
        keys.put("PRISMARINE.1", "tile.prismarine.dark.name");
        keys.put("SEA_LANTERN", "tile.seaLantern.name");
        keys.put("END_ROD", "tile.endRod.name");
        keys.put("CHORUS_PLANT", "tile.chorusPlant.name");
        keys.put("CHORUS_FLOWER", "tile.chorusFlower.name");
        keys.put("PURPUR_BLOCK", "tile.purpurBlock.name");
        keys.put("PURPUR_PILLAR", "tile.purpurPillar.name");
        keys.put("PURPUR_STAIRS", "tile.stairsPurpur.name");
        keys.put("PURPUR_SLAB", "tile.purpurSlab.name");
        keys.put("END_BRICKS", "tile.endBricks.name");
        keys.put("BEETROOT_BLOCK", "tile.beetroots.name");
        keys.put("GRASS_PATH", "tile.grassPath.name");
        keys.put("MAGMA", "tile.magma.name");
        keys.put("NETHER_WART_BLOCK", "tile.netherWartBlock.name");
        keys.put("RED_NETHER_BRICK", "tile.redNetherBrick.name");
        keys.put("BONE_BLOCK", "tile.boneBlock.name");
        keys.put("OBSERVER", "tile.observer.name");
        keys.put("WHITE_SHULKER_BOX", "tile.shulkerBoxWhite.name");
        keys.put("ORANGE_SHULKER_BOX", "tile.shulkerBoxOrange.name");
        keys.put("MAGENTA_SHULKER_BOX", "tile.shulkerBoxMagenta.name");
        keys.put("LIGHT_BLUE_SHULKER_BOX", "tile.shulkerBoxLightBlue.name");
        keys.put("YELLOW_SHULKER_BOX", "tile.shulkerBoxYellow.name");
        keys.put("LIME_SHULKER_BOX", "tile.shulkerBoxLime.name");
        keys.put("PINK_SHULKER_BOX", "tile.shulkerBoxPink.name");
        keys.put("GRAY_SHULKER_BOX", "tile.shulkerBoxGray.name");
        keys.put("SILVER_SHULKER_BOX", "tile.shulkerBoxSilver.name");
        keys.put("CYAN_SHULKER_BOX", "tile.shulkerBoxCyan.name");
        keys.put("PURPLE_SHULKER_BOX", "tile.shulkerBoxPurple.name");
        keys.put("BLUE_SHULKER_BOX", "tile.shulkerBoxBlue.name");
        keys.put("BROWN_SHULKER_BOX", "tile.shulkerBoxBrown.name");
        keys.put("GREEN_SHULKER_BOX", "tile.shulkerBoxGreen.name");
        keys.put("RED_SHULKER_BOX", "tile.shulkerBoxRed.name");
        keys.put("BLACK_SHULKER_BOX", "tile.shulkerBoxBlack.name");
        keys.put("WHITE_GLAZED_TERRACOTTA", "tile.glazedTerracottaWhite.name");
        keys.put("ORANGE_GLAZED_TERRACOTTA", "tile.glazedTerracottaOrange.name");
        keys.put("MAGENTA_GLAZED_TERRACOTTA", "tile.glazedTerracottaMagenta.name");
        keys.put("LIGHT_BLUE_GLAZED_TERRACOTTA", "tile.glazedTerracottaLightBlue.name");
        keys.put("YELLOW_GLAZED_TERRACOTTA", "tile.glazedTerracottaYellow.name");
        keys.put("LIME_GLAZED_TERRACOTTA", "tile.glazedTerracottaLime.name");
        keys.put("PINK_GLAZED_TERRACOTTA", "tile.glazedTerracottaPink.name");
        keys.put("GRAY_GLAZED_TERRACOTTA", "tile.glazedTerracottaGray.name");
        keys.put("SILVER_GLAZED_TERRACOTTA", "tile.glazedTerracottaSilver.name");
        keys.put("CYAN_GLAZED_TERRACOTTA", "tile.glazedTerracottaCyan.name");
        keys.put("PURPLE_GLAZED_TERRACOTTA", "tile.glazedTerracottaPurple.name");
        keys.put("BLUE_GLAZED_TERRACOTTA", "tile.glazedTerracottaBlue.name");
        keys.put("BROWN_GLAZED_TERRACOTTA", "tile.glazedTerracottaBrown.name");
        keys.put("GREEN_GLAZED_TERRACOTTA", "tile.glazedTerracottaGreen.name");
        keys.put("RED_GLAZED_TERRACOTTA", "tile.glazedTerracottaRed.name");
        keys.put("BLACK_GLAZED_TERRACOTTA", "tile.glazedTerracottaBlack.name");
        keys.put("CONCRETE.15", "tile.concrete.black.name");
        keys.put("CONCRETE.14", "tile.concrete.red.name");
        keys.put("CONCRETE.13", "tile.concrete.green.name");
        keys.put("CONCRETE.12", "tile.concrete.brown.name");
        keys.put("CONCRETE.11", "tile.concrete.blue.name");
        keys.put("CONCRETE.10", "tile.concrete.purple.name");
        keys.put("CONCRETE.9", "tile.concrete.cyan.name");
        keys.put("CONCRETE.8", "tile.concrete.silver.name");
        keys.put("CONCRETE.7", "tile.concrete.gray.name");
        keys.put("CONCRETE.6", "tile.concrete.pink.name");
        keys.put("CONCRETE.5", "tile.concrete.lime.name");
        keys.put("CONCRETE.4", "tile.concrete.yellow.name");
        keys.put("CONCRETE.3", "tile.concrete.lightBlue.name");
        keys.put("CONCRETE.2", "tile.concrete.magenta.name");
        keys.put("CONCRETE.1", "tile.concrete.orange.name");
        keys.put("CONCRETE", "tile.concrete.white.name");
        keys.put("CONCRETE_POWER.15", "tile.concretePowder.black.name");
        keys.put("CONCRETE_POWER.14", "tile.concretePowder.red.name");
        keys.put("CONCRETE_POWER.13", "tile.concretePowder.green.name");
        keys.put("CONCRETE_POWER.12", "tile.concretePowder.brown.name");
        keys.put("CONCRETE_POWER.11", "tile.concretePowder.blue.name");
        keys.put("CONCRETE_POWER.10", "tile.concretePowder.purple.name");
        keys.put("CONCRETE_POWER.9", "tile.concretePowder.cyan.name");
        keys.put("CONCRETE_POWER.8", "tile.concretePowder.silver.name");
        keys.put("CONCRETE_POWER.7", "tile.concretePowder.gray.name");
        keys.put("CONCRETE_POWER.6", "tile.concretePowder.pink.name");
        keys.put("CONCRETE_POWER.5", "tile.concretePowder.lime.name");
        keys.put("CONCRETE_POWER.4", "tile.concretePowder.yellow.name");
        keys.put("CONCRETE_POWER.3", "tile.concretePowder.lightBlue.name");
        keys.put("CONCRETE_POWER.2", "tile.concretePowder.magenta.name");
        keys.put("CONCRETE_POWER.1", "tile.concretePowder.orange.name");
        keys.put("CONCRETE_POWER.0", "tile.concretePowder.white.name");
        keys.put("STRUCTURE_VOID", "tile.structureVoid.name");
        keys.put("STRUCTURE_BLOCK", "tile.structureBlock.name");
        keys.put("BED_BLOCK.15", "item.bed.black.name"); // added, 1.11+
        keys.put("BED_BLOCK.14", "item.bed.red.name"); // added, 1.11+
        keys.put("BED_BLOCK.13", "item.bed.green.name"); // added, 1.11+
        keys.put("BED_BLOCK.12", "item.bed.brown.name"); // added, 1.11+
        keys.put("BED_BLOCK.11", "item.bed.blue.name"); // added, 1.11+
        keys.put("BED_BLOCK.10", "item.bed.purple.name"); // added, 1.11+
        keys.put("BED_BLOCK.9", "item.bed.cyan.name"); // added, 1.11+
        keys.put("BED_BLOCK.8", "item.bed.silver.name"); // added, 1.11+
        keys.put("BED_BLOCK.7", "item.bed.gray.name"); // added, 1.11+
        keys.put("BED_BLOCK.6", "item.bed.pink.name"); // added, 1.11+
        keys.put("BED_BLOCK.5", "item.bed.lime.name"); // added, 1.11+
        keys.put("BED_BLOCK.4", "item.bed.yellow.name"); // added, 1.11+
        keys.put("BED_BLOCK.3", "item.bed.lightBlue.name"); // added, 1.11+
        keys.put("BED_BLOCK.2", "item.bed.magenta.name"); // added, 1.11+
        keys.put("BED_BLOCK.1", "item.bed.orange.name"); // added, 1.11+
        keys.put("BED_BLOCK.0", "item.bed.white.name"); // added, 1.11+
        keys.put("BED_BLOCK", "item.bed.name"); // added
        keys.put("DIODE_BLOCK_OFF", "item.diode.name"); // added
        keys.put("DIODE_BLOCK_ON", "item.diode.name"); // added
        keys.put("REDSTONE_COMPARATOR_OFF", "item.comparator.name"); // added
        keys.put("REDSTONE_COMPARATOR_OFF", "item.comparator.name"); // added
        keys.put("SKULL.0", "item.skull.skeleton.name"); // added
        keys.put("SKULL.1", "item.skull.wither.name"); // added
        keys.put("SKULL.2", "item.skull.zombie.name"); // added
        keys.put("SKULL.3", "item.skull.char.name"); // added
        keys.put("SKULL.4", "item.skull.player.name"); // added
        keys.put("SKULL.5", "item.skull.creeper.name"); // added
        keys.put("SKULL.6", "item.skull.dragon.name"); // added
        return keys;
    }
    
    private LinkedHashMap<String, String> getItemKeys() {
        LinkedHashMap<String, String> keys = new LinkedHashMap<String, String>();
        keys.put("NAME_TAG", "item.nameTag.name");
        keys.put("LEASH", "item.leash.name");
        keys.put("IRON_SPADE", "item.shovelIron.name");
        keys.put("IRON_PICKAXE", "item.pickaxeIron.name");
        keys.put("IRON_AXE", "item.hatchetIron.name");
        keys.put("FLINT_AND_STEEL", "item.flintAndSteel.name");
        keys.put("APPLE", "item.apple.name");
        keys.put("COOKIE", "item.cookie.name");
        keys.put("BOW", "item.bow.name");
        keys.put("ARROW", "item.arrow.name");
        keys.put("SPECTRAL_ARROW", "item.spectral_arrow.name");
        keys.put("TIPPED_ARROW", "item.tipped_arrow.name");
        keys.put("COAL", "item.coal.name");
        keys.put("COAL.1", "item.charcoal.name");
        keys.put("DIAMOND", "item.diamond.name");
        keys.put("EMERALD", "item.emerald.name");
        keys.put("IRON_INGOT", "item.ingotIron.name");
        keys.put("GOLD_INGOT", "item.ingotGold.name");
        keys.put("IRON_SWORD", "item.swordIron.name");
        keys.put("WOOD_SWORD", "item.swordWood.name");
        keys.put("WOOD_SPADE", "item.shovelWood.name");
        keys.put("WOOD_PICKAXE", "item.pickaxeWood.name");
        keys.put("WOOD_AXE", "item.hatchetWood.name");
        keys.put("STONE_SWORD", "item.swordStone.name");
        keys.put("STONE_SPADE", "item.shovelStone.name");
        keys.put("STONE_PICKAXE", "item.pickaxeStone.name");
        keys.put("STONE_AXE", "item.hatchetStone.name");
        keys.put("DIAMOND_SWORD", "item.swordDiamond.name");
        keys.put("DIAMOND_SPADE", "item.shovelDiamond.name");
        keys.put("DIAMOND_PICKAXE", "item.pickaxeDiamond.name");
        keys.put("DIAMOND_AXE", "item.hatchetDiamond.name");
        keys.put("STICK", "item.stick.name");
        keys.put("BOWL", "item.bowl.name");
        keys.put("MUSHROOM_SOUP", "item.mushroomStew.name");
        keys.put("GOLD_SWORD", "item.swordGold.name");
        keys.put("GOLD_SPADE", "item.shovelGold.name");
        keys.put("GOLD_PICKAXE", "item.pickaxeGold.name");
        keys.put("GOLD_AXE", "item.hatchetGold.name");
        keys.put("STRING", "item.string.name");
        keys.put("FEATHER", "item.feather.name");
        keys.put("SULPHUR", "item.sulphur.name");
        keys.put("WOOD_HOE", "item.hoeWood.name");
        keys.put("STONE_HOE", "item.hoeStone.name");
        keys.put("IRON_HOE", "item.hoeIron.name");
        keys.put("DIAMOND_HOE", "item.hoeDiamond.name");
        keys.put("GOLD_HOE", "item.hoeGold.name");
        keys.put("SEEDS", "item.seeds.name");
        keys.put("PUMPKIN_SEEDS", "item.seeds_pumpkin.name");
        keys.put("MELON_SEEDS", "item.seeds_melon.name");
        keys.put("MELON", "item.melon.name");
        keys.put("WHEAT", "item.wheat.name");
        keys.put("BREAD", "item.bread.name");
        keys.put("LEATHER_HELMET", "item.helmetCloth.name");
        keys.put("LEATHER_CHESTPLATE", "item.chestplateCloth.name");
        keys.put("LEATHER_LEGGINGS", "item.leggingsCloth.name");
        keys.put("LEATHER_BOOTS", "item.bootsCloth.name");
        keys.put("CHAINMAIL_HELMET", "item.helmetChain.name");
        keys.put("CHAINMAIL_CHESTPLATE", "item.chestplateChain.name");
        keys.put("CHAINMAIL_LEGGINGS", "item.leggingsChain.name");
        keys.put("CHAINMAIL_BOOTS", "item.bootsChain.name");
        keys.put("IRON_HELMET", "item.helmetIron.name");
        keys.put("IRON_CHESTPLATE", "item.chestplateIron.name");
        keys.put("IRON_LEGGINGS", "item.leggingsIron.name");
        keys.put("IRON_BOOTS", "item.bootsIron.name");
        keys.put("DIAMOND_HELMET", "item.helmetDiamond.name");
        keys.put("DIAMOND_CHESTPLATE", "item.chestplateDiamond.name");
        keys.put("DIAMOND_LEGGINGS", "item.leggingsDiamond.name");
        keys.put("DIAMOND_BOOTS", "item.bootsDiamond.name");
        keys.put("GOLD_HELMET", "item.helmetGold.name");
        keys.put("GOLD_CHESTPLATE", "item.chestplateGold.name");
        keys.put("GOLD_LEGGINGS", "item.leggingsGold.name");
        keys.put("GOLD_BOOTS", "item.bootsGold.name");
        keys.put("FLINT", "item.flint.name");
        keys.put("PORK", "item.porkchopRaw.name");
        keys.put("GRILLED_PORK", "item.porkchopCooked.name");
        keys.put("RAW_CHICKEN", "item.chickenRaw.name");
        keys.put("COOKED_CHICKEN", "item.chickenCooked.name");
        keys.put("MUTTON", "item.muttonRaw.name");
        keys.put("COOKED_MUTTON", "item.muttonCooked.name");
        keys.put("RABBIT", "item.rabbitRaw.name");
        keys.put("COOKED_RABBIT", "item.rabbitCooked.name");
        keys.put("RABBIT_STEW", "item.rabbitStew.name");
        keys.put("RABBIT_FOOT", "item.rabbitFoot.name");
        keys.put("RABBIT_HIDE", "item.rabbitHide.name");
        keys.put("RAW_BEEF", "item.beefRaw.name");
        keys.put("COOKED_BEEF", "item.beefCooked.name");
        keys.put("PAINTING", "item.painting.name");
        keys.put("ITEM_FRAME", "item.frame.name");
        keys.put("GOLDEN_APPLE", "item.appleGold.name");
        keys.put("SIGN", "item.sign.name");
        keys.put("WOOD_DOOR", "item.doorOak.name");
        keys.put("SPRUCE_DOOR_ITEM", "item.doorSpruce.name");
        keys.put("BIRCH_DOOR_ITEM", "item.doorBirch.name");
        keys.put("JUNGLE_DOOR_ITEM", "item.doorJungle.name");
        keys.put("ACACIA_DOOR_ITEM", "item.doorAcacia.name");
        keys.put("DARK_OAK_DOOR_ITEM", "item.doorDarkOak.name");
        keys.put("BUCKET", "item.bucket.name");
        keys.put("WATER_BUCKET", "item.bucketWater.name");
        keys.put("LAVA_BUCKET", "item.bucketLava.name");
        keys.put("MINECART", "item.minecart.name");
        keys.put("SADDLE", "item.saddle.name");
        keys.put("IRON_DOOR", "item.doorIron.name");
        keys.put("REDSTONE", "item.redstone.name");
        keys.put("SNOW_BALL", "item.snowball.name");
        keys.put("BOAT", "item.boat.oak.name");
        keys.put("BOAT_SPRUCE", "item.boat.spruce.name");
        keys.put("BOAT_BIRCH", "item.boat.birch.name");
        keys.put("BOAT_JUNGLE", "item.boat.jungle.name");
        keys.put("BOAT_ACACIA", "item.boat.acacia.name");
        keys.put("BOAT_DARK_OAK", "item.boat.dark_oak.name");
        keys.put("LEATHER", "item.leather.name");
        keys.put("MILK_BUCKET", "item.milk.name");
        keys.put("CLAY_BRICK", "item.brick.name");
        keys.put("CLAY_BALL", "item.clay.name");
        keys.put("SUGAR_CANE", "item.reeds.name");
        keys.put("PAPER ", "item.paper.name");
        keys.put("BOOK", "item.book.name");
        keys.put("SLIME_BALL", "item.slimeball.name");
        keys.put("STORAGE_MINECART", "item.minecartChest.name");
        keys.put("POWERED_MINECART", "item.minecartFurnace.name");
        keys.put("EXPLOSIVE_MINECART", "item.minecartTnt.name");
        keys.put("HOPPER_MINECART", "item.minecartHopper.name");
        keys.put("COMMAND_MINECART", "item.minecartCommandBlock.name");
        keys.put("EGG", "item.egg.name");
        keys.put("COMPASS", "item.compass.name");
        keys.put("FISHING_ROD", "item.fishingRod.name");
        keys.put("WATCH", "item.clock.name");
        keys.put("GLOWSTONE_DUST", "item.yellowDust.name");
        keys.put("RAW_FISH", "item.fish.cod.raw.name");
        keys.put("RAW_FISH.1", "item.fish.salmon.raw.name");
        keys.put("RAW_FISH.3", "item.fish.pufferfish.raw.name");
        keys.put("RAW_FISH.2", "item.fish.clownfish.raw.name");
        keys.put("COOKED_FISH", "item.fish.cod.cooked.name");
        keys.put("COOKED_FISH.1", "item.fish.salmon.cooked.name");
        keys.put("GOLD_RECORD", "item.record.name"); // added
        keys.put("GREEN_RECORD", "item.record.name"); // added
        keys.put("RECORD_6", "item.record.name"); // added
        keys.put("RECORD_4", "item.record.name"); // added
        keys.put("RECORD_5", "item.record.name"); // added
        keys.put("RECORD_6", "item.record.name"); // added
        keys.put("RECORD_7", "item.record.name"); // added
        keys.put("RECORD_8", "item.record.name"); // added
        keys.put("RECORD_9", "item.record.name"); // added
        keys.put("RECORD_10", "item.record.name"); // added
        keys.put("RECORD_11", "item.record.name"); // added
        keys.put("RECORD_12", "item.record.name"); // added
        keys.put("BONE", "item.bone.name");
        keys.put("INK_SACK", "item.dyePowder.black.name");
        keys.put("INK_SACK.1", "item.dyePowder.red.name");
        keys.put("INK_SACK.2", "item.dyePowder.green.name");
        keys.put("INK_SACK.3", "item.dyePowder.brown.name");
        keys.put("INK_SACK.4", "item.dyePowder.blue.name");
        keys.put("INK_SACK.5", "item.dyePowder.purple.name");
        keys.put("INK_SACK.6", "item.dyePowder.cyan.name");
        keys.put("INK_SACK.7", "item.dyePowder.silver.name");
        keys.put("INK_SACK.8", "item.dyePowder.gray.name");
        keys.put("INK_SACK.9", "item.dyePowder.pink.name");
        keys.put("INK_SACK.10", "item.dyePowder.lime.name");
        keys.put("INK_SACK.11", "item.dyePowder.yellow.name");
        keys.put("INK_SACK.12", "item.dyePowder.lightBlue.name");
        keys.put("INK_SACK.13", "item.dyePowder.magenta.name");
        keys.put("INK_SACK.14", "item.dyePowder.orange.name");
        keys.put("INK_SACK.15", "item.dyePowder.white.name");
        keys.put("SUGAR", "item.sugar.name");
        keys.put("CAKE", "item.cake.name");
        keys.put("BED.15", "item.bed.black.name"); // 1.11+
        keys.put("BED.14", "item.bed.red.name"); // 1.11+
        keys.put("BED.13", "item.bed.green.name"); // 1.11+
        keys.put("BED.12", "item.bed.brown.name"); // 1.11+
        keys.put("BED.11", "item.bed.blue.name"); // 1.11+
        keys.put("BED.10", "item.bed.purple.name"); // 1.11+
        keys.put("BED.9", "item.bed.cyan.name"); // 1.11+
        keys.put("BED.8", "item.bed.silver.name"); // 1.11+
        keys.put("BED.7", "item.bed.gray.name"); // 1.11+
        keys.put("BED.6", "item.bed.pink.name"); // 1.11+
        keys.put("BED.5", "item.bed.lime.name"); // 1.11+
        keys.put("BED.4", "item.bed.yellow.name"); // 1.11+
        keys.put("BED.3", "item.bed.lightBlue.name"); // 1.11+
        keys.put("BED.2", "item.bed.magenta.name"); // 1.11+
        keys.put("BED.1", "item.bed.orange.name"); // 1.11+
        keys.put("BED.0", "item.bed.white.name"); // 1.11+
        keys.put("BED", "item.bed.name");
        keys.put("DIODE", "item.diode.name");
        keys.put("REDSTONE_COMPARATOR", "item.comparator.name");
        keys.put("MAP", "item.map.name");
        keys.put("LEAVES", "item.leaves.name");
        keys.put("LEAVES_2", "item.leaves.name"); // added
        keys.put("SHEARS", "item.shears.name");
        keys.put("ROTTEN_FLESH", "item.rottenFlesh.name");
        keys.put("ENDER_PEARL", "item.enderPearl.name");
        keys.put("BLAZE_ROD", "item.blazeRod.name");
        keys.put("GHAST_TEAR", "item.ghastTear.name");
        keys.put("NETHER_WARTS", "item.netherStalkSeeds.name");
        keys.put("POTION", "item.potion.name");
        keys.put("SPLASH_POTION", "item.splash_potion.name");
        keys.put("LINGERING_POTION", "item.lingering_potion.name");
        keys.put("END_CRYSTAL", "item.end_crystal.name");
        keys.put("GOLD_NUGGET", "item.goldNugget.name");
        keys.put("GLASS_BOTTLE", "item.glassBottle.name");
        keys.put("SPIDER_EYE", "item.spiderEye.name");
        keys.put("FERMENTED_SPIDER_EYE", "item.fermentedSpiderEye.name");
        keys.put("BLAZE_POWDER", "item.blazePowder.name");
        keys.put("MAGMA_CREAM", "item.magmaCream.name");
        keys.put("CAULDRON_ITEM", "item.cauldron.name");
        keys.put("BREWING_STAND_ITEM", "item.brewingStand.name");
        keys.put("EYE_OF_ENDER", "item.eyeOfEnder.name");
        keys.put("SPECKLED_MELON", "item.speckledMelon.name");
        keys.put("MONSTER_EGG", "item.monsterPlacer.name");
        keys.put("EXP_BOTTLE", "item.expBottle.name");
        keys.put("FIREBALL", "item.fireball.name");
        keys.put("BOOK_AND_QUILL", "item.writingBook.name");
        keys.put("WRITTEN_BOOK", "item.writtenBook.name");
        keys.put("FLOWER_POT_ITEM", "item.flowerPot.name");
        keys.put("EMPTY_MAP", "item.emptyMap.name");
        keys.put("CARROT_ITEM", "item.carrots.name");
        keys.put("GOLDEN_CARROT", "item.carrotGolden.name");
        keys.put("POTATO_ITEM", "item.potato.name");
        keys.put("BAKED_POTATO", "item.potatoBaked.name");
        keys.put("POISONOUS_POTATO", "item.potatoPoisonous.name");
        keys.put("SKULL_ITEM.0", "item.skull.skeleton.name");
        keys.put("SKULL_ITEM.1", "item.skull.wither.name");
        keys.put("SKULL_ITEM.2", "item.skull.zombie.name");
        keys.put("SKULL_ITEM.3 ", "item.skull.char.name");
        keys.put("SKULL_ITEM.4", "item.skull.player.name");
        keys.put("SKULL_ITEM.5", "item.skull.creeper.name");
        keys.put("SKULL_ITEM.6", "item.skull.dragon.name");
        keys.put("CARROT_STICK", "item.carrotOnAStick.name");
        keys.put("NETHER_STAR", "item.netherStar.name");
        keys.put("PUMPKIN_PIE", "item.pumpkinPie.name");
        keys.put("ENCHANTED_BOOK", "item.enchantedBook.name");
        keys.put("FIREWORK", "item.fireworks.name");
        keys.put("FIREWORK_CHARGE", "item.fireworksCharge.name");
        keys.put("NETHER_BRICK_ITEM", "item.netherbrick.name");
        keys.put("QUARTZ_ORE", "item.netherquartz.name");
        keys.put("ARMOR_STAND", "item.armorStand.name");
        keys.put("IRON_BARDING", "item.horsearmormetal.name");
        keys.put("GOLD_BARDING", "item.horsearmorgold.name");
        keys.put("DIAMOND_BARDING", "item.horsearmordiamond.name");
        keys.put("PRISMARINE_SHARD", "item.prismarineShard.name");
        keys.put("PRISMARINE_CRYSTALS", "item.prismarineCrystals.name");
        keys.put("CHORUS_FRUIT", "item.chorusFruit.name");
        keys.put("CHORUS_FRUIT_POPPED", "item.chorusFruitPopped.name");
        keys.put("BEETROOT", "item.beetroot.name");
        keys.put("BEETROOT_SEEDS", "item.beetroot_seeds.name");
        keys.put("BEETROOT_SOUP", "item.beetroot_soup.name");
        keys.put("DRAGONS_BREATH", "item.dragon_breath.name");
        keys.put("ELYTRA", "item.elytra.name");
        keys.put("TOTEM", "item.totem.name");
        keys.put("SHULKER_SHELL", "item.shulkerShell.name");
        keys.put("IRON_NUGGET", "item.ironNugget.name");
        keys.put("KNOWLEDGE_BOOK", "item.knowledgeBook.name");
        keys.put("BANNER.15", "item.banner.black.name");
        keys.put("BANNER.14", "item.banner.red.name");
        keys.put("BANNER.13", "item.banner.green.name");
        keys.put("BANNER.12", "item.banner.brown.name");
        keys.put("BANNER.11", "item.banner.blue.name");
        keys.put("BANNER.10", "item.banner.purple.name");
        keys.put("BANNER.9", "item.banner.cyan.name");
        keys.put("BANNER.8", "item.banner.silver.name");
        keys.put("BANNER.7", "item.banner.gray.name");
        keys.put("BANNER.6", "item.banner.pink.name");
        keys.put("BANNER.5", "item.banner.lime.name");
        keys.put("BANNER.4", "item.banner.yellow.name");
        keys.put("BANNER.3", "item.banner.lightBlue.name");
        keys.put("BANNER.2", "item.banner.magenta.name");
        keys.put("BANNER.1", "item.banner.orange.name");
        keys.put("BANNER.0", "item.banner.white.name");
        keys.put("STANDING_BANNER.15", "item.STANDING_BANNER.black.name"); // added
        keys.put("STANDING_BANNER.14", "item.STANDING_BANNER.red.name"); // added
        keys.put("STANDING_BANNER.13", "item.STANDING_BANNER.green.name"); // added
        keys.put("STANDING_BANNER.12", "item.STANDING_BANNER.brown.name"); // added
        keys.put("STANDING_BANNER.11", "item.STANDING_BANNER.blue.name"); // added
        keys.put("STANDING_BANNER.10", "item.STANDING_BANNER.purple.name"); // added
        keys.put("STANDING_BANNER.9", "item.STANDING_BANNER.cyan.name"); // added
        keys.put("STANDING_BANNER.8", "item.STANDING_BANNER.silver.name"); // added
        keys.put("STANDING_BANNER.7", "item.STANDING_BANNER.gray.name"); // added
        keys.put("STANDING_BANNER.6", "item.STANDING_BANNER.pink.name"); // added
        keys.put("STANDING_BANNER.5", "item.STANDING_BANNER.lime.name"); // added
        keys.put("STANDING_BANNER.4", "item.STANDING_BANNER.yellow.name"); // added
        keys.put("STANDING_BANNER.3", "item.STANDING_BANNER.lightBlue.name"); // added
        keys.put("STANDING_BANNER.2", "item.STANDING_BANNER.magenta.name"); // added
        keys.put("STANDING_BANNER.1", "item.STANDING_BANNER.orange.name"); // added
        keys.put("STANDING_BANNER.0", "item.STANDING_BANNER.white.name"); // added
        keys.put("WALL_BANNER.15", "item.WALL_BANNER.black.name"); // added
        keys.put("WALL_BANNER.14", "item.WALL_BANNER.red.name"); // added
        keys.put("WALL_BANNER.13", "item.WALL_BANNER.green.name"); // added
        keys.put("WALL_BANNER.12", "item.WALL_BANNER.brown.name"); // added
        keys.put("WALL_BANNER.11", "item.WALL_BANNER.blue.name"); // added
        keys.put("WALL_BANNER.10", "item.WALL_BANNER.purple.name"); // added
        keys.put("WALL_BANNER.9", "item.WALL_BANNER.cyan.name"); // added
        keys.put("WALL_BANNER.8", "item.WALL_BANNER.silver.name"); // added
        keys.put("WALL_BANNER.7", "item.WALL_BANNER.gray.name"); // added
        keys.put("WALL_BANNER.6", "item.WALL_BANNER.pink.name"); // added
        keys.put("WALL_BANNER.5", "item.WALL_BANNER.lime.name"); // added
        keys.put("WALL_BANNER.4", "item.WALL_BANNER.yellow.name"); // added
        keys.put("WALL_BANNER.3", "item.WALL_BANNER.lightBlue.name"); // added
        keys.put("WALL_BANNER.2", "item.WALL_BANNER.magenta.name"); // added
        keys.put("WALL_BANNER.1", "item.WALL_BANNER.orange.name"); // added
        keys.put("WALL_BANNER.0", "item.WALL_BANNER.white.name"); // added
        return keys;
    }
    
    public Map<String, String> getPotionKeys_18() {
        LinkedHashMap<String, String> keys = new LinkedHashMap<String, String>();
        keys.put("WATER", "potion.empty");
        keys.put("SPEED", "potion.moveSpeed.postfix");
        keys.put("SLOWNESS", "potion.moveSlowdown.postfix");
        keys.put("STRENGTH", "potion.damageBoost.postfix");
        keys.put("WEAKNESS", "potion.weakness.postfix");
        keys.put("INSTANT_HEAL", "potion.effect.healing");
        keys.put("INSTANT_DAMAGE", "potion.harm.postfix");
        keys.put("JUMP", "potion.jump.postfix");
        keys.put("REGEN", "potion.regeneration.postfix");
        keys.put("FIRE_RESISTANCE", "potion.fireResistance.postfix");
        keys.put("WATER_BREATHING", "potion.waterBreathing.postfix");
        keys.put("INVISIBILITY", "potion.invisibility.postfix");
        keys.put("NIGHT_VISION", "potion.nightVision.postfix");
        keys.put("POISON", "potion.poison.postfix");
        return keys;
    }
    
    public Map<String, String> getPotionKeys() {
        LinkedHashMap<String, String> keys = new LinkedHashMap<String, String>();
        keys.put("UNCRAFTABLE", "potion.effect.empty");
        keys.put("WATER", "potion.effect.water");
        keys.put("MUNDANE", "potion.effect.mundane");
        keys.put("THICK", "potion.effect.thick");
        keys.put("AWKWARD", "potion.effect.awkward");
        keys.put("NIGHT_VISION", "potion.effect.night_vision");
        keys.put("INVISIBILITY", "potion.effect.invisibility");
        keys.put("JUMP", "potion.effect.leaping");
        keys.put("FIRE_RESISTANCE", "potion.effect.fire_resistance");
        keys.put("SPEED", "potion.effect.swiftness");
        keys.put("SLOWNESS", "potion.effect.slowness");
        keys.put("WATER_BREATHING", "potion.effect.water_breathing");
        keys.put("INSTANT_HEAL", "potion.effect.healing");
        keys.put("INSTANT_DAMAGE", "potion.effect.harming");
        keys.put("POISON", "potion.effect.poison");
        keys.put("REGEN", "potion.effect.regeneration");
        keys.put("STRENGTH", "potion.effect.strength");
        keys.put("WEAKNESS", "potion.effect.weakness");
        keys.put("SLOW_FALLING", "potion.effect.levitation");
        keys.put("LUCK", "potion.effect.luck");
        return keys;
    }
    
    public Map<String, String> getSplashPotionKeys() {
        LinkedHashMap<String, String> keys = new LinkedHashMap<String, String>();
        keys.put("UNCRAFTABLE", "splash_potion.effect.empty");
        keys.put("WATER", "splash_potion.effect.water");
        keys.put("MUNDANE", "splash_potion.effect.mundane");
        keys.put("THICK", "splash_potion.effect.thick");
        keys.put("AWKWARD", "splash_potion.effect.awkward");
        keys.put("NIGHT_VISION", "splash_potion.effect.night_vision");
        keys.put("INVISIBILITY", "splash_potion.effect.invisibility");
        keys.put("JUMP", "splash_potion.effect.leaping");
        keys.put("FIRE_RESISTANCE", "splash_potion.effect.fire_resistance");
        keys.put("SPEED", "splash_potion.effect.swiftness");
        keys.put("SLOWNESS", "splash_potion.effect.slowness");
        keys.put("WATER_BREATHING", "splash_potion.effect.water_breathing");
        keys.put("INSTANT_HEAL", "splash_potion.effect.healing");
        keys.put("INSTANT_DAMAGE", "splash_potion.effect.harming");
        keys.put("POISON", "splash_potion.effect.poison");
        keys.put("REGEN", "splash_potion.effect.regeneration");
        keys.put("STRENGTH", "splash_potion.effect.strength");
        keys.put("WEAKNESS", "splash_potion.effect.weakness");
        keys.put("SLOW_FALLING", "splash_potion.effect.levitation");
        keys.put("LUCK", "splash_potion.effect.luck");
        return keys;
    }
    
    public Map<String, String> getLingeringPotionKeys() {
        LinkedHashMap<String, String> keys = new LinkedHashMap<String, String>();
        keys.put("UNCRAFTABLE", "lingering_potion.effect.empty");
        keys.put("WATER", "lingering_potion.effect.water");
        keys.put("MUNDANE", "lingering_potion.effect.mundane");
        keys.put("THICK", "lingering_potion.effect.thick");
        keys.put("AWKWARD", "lingering_potion.effect.awkward");
        keys.put("NIGHT_VISION", "lingering_potion.effect.night_vision");
        keys.put("INVISIBILITY", "lingering_potion.effect.invisibility");
        keys.put("JUMP", "lingering_potion.effect.leaping");
        keys.put("FIRE_RESISTANCE", "lingering_potion.effect.fire_resistance");
        keys.put("SPEED", "lingering_potion.effect.swiftness");
        keys.put("SLOWNESS", "lingering_potion.effect.slowness");
        keys.put("WATER_BREATHING", "lingering_potion.effect.water_breathing");
        keys.put("INSTANT_HEAL", "lingering_potion.effect.healing");
        keys.put("INSTANT_DAMAGE", "lingering_potion.effect.harming");
        keys.put("POISON", "lingering_potion.effect.poison");
        keys.put("REGEN", "lingering_potion.effect.regeneration");
        keys.put("STRENGTH", "lingering_potion.effect.strength");
        keys.put("WEAKNESS", "lingering_potion.effect.weakness");
        keys.put("SLOW_FALLING", "lingering_potion.effect.levitation");
        keys.put("LUCK", "lingering_potion.effect.luck");
        return keys;
    }
    
    public Map<String, String> getEntityKeys() {
        LinkedHashMap<String, String> keys = new LinkedHashMap<String, String>();
        keys.put("DROPPED_ITEM", "entity.Item.name");
        keys.put("EXPERIENCE_ORB", "entity.XPOrb.name");
        keys.put("SMALL_FIREBALL", "entity.SmallFireball.name");
        keys.put("FIREBALL", "entity.Fireball.name");
        keys.put("DRAGON_FIREBALL", "entity.DragonFireball.name");
        keys.put("SPLASH_POTION", "item.splash_potion.name"); // added
        keys.put("LINGERING_POTION", "item.lingering_potion.name"); // added
        keys.put("ARROW", "entity.Arrow.name");
        keys.put("SNOWBALL", "entity.Snowball.name");
        keys.put("PAINTING", "entity.Painting.name");
        keys.put("ARMOR_STAND", "entity.ArmorStand.name");
        keys.put("CREEPER", "entity.Creeper.name");
        keys.put("SKELETON", "entity.Skeleton.name");
        keys.put("SPIDER", "entity.Spider.name");
        keys.put("GIANT", "entity.Giant.name");
        keys.put("ZOMBIE", "entity.Zombie.name");
        keys.put("SLIME", "entity.Slime.name");
        keys.put("GHAST", "entity.Ghast.name");
        keys.put("PIG_ZOMBIE", "entity.PigZombie.name");
        keys.put("ENDERMAN", "entity.Enderman.name");
        keys.put("ENDERMITE", "entity.Endermite.name");
        keys.put("SILVERFISH", "entity.Silverfish.name");
        keys.put("CAVE_SPIDER", "entity.CaveSpider.name");
        keys.put("BLAZE", "entity.Blaze.name");
        keys.put("MAGMA_CUBE", "entity.LavaSlime.name");
        keys.put("MUSHROOM_COW", "entity.MushroomCow.name");
        keys.put("VILLAGER", "entity.Villager.name");
        keys.put("ZOMBIE_VILLAGER", "entity.Villager.name"); // added
        keys.put("IRON_GOLEM", "entity.VillagerGolem.name");
        keys.put("SNOWMAN", "entity.SnowMan.name");
        keys.put("ENDER_DRAGON", "entity.EnderDragon.name");
        keys.put("WITHER", "entity.WitherBoss.name");
        keys.put("WITCH", "entity.Witch.name");
        keys.put("GUARDIAN", "entity.Guardian.name");
        keys.put("SHULKER", "entity.Shulker.name");
        keys.put("POLAR_BEAR", "entity.PolarBear.name");
        keys.put("EVOKER", "entity.EvocationIllager.name");
        keys.put("EVOKER_FANGS", "entity.EvocationIllager.name");
        keys.put("VEX", "entity.Vex.name");
        keys.put("VINDICATOR", "entity.VindicationIllager.name");
        keys.put("PARROT", "entity.Parrot.name");
        keys.put("ILLUSIONER", "entity.IllusionIllager.name");
        keys.put("VILLAGER.FARMER", "entity.Villager.farmer");
        keys.put("VILLAGER.FISHERMAN", "entity.Villager.fisherman");
        keys.put("VILLAGER.SHEPHERD", "entity.Villager.shepherd");
        keys.put("VILLAGER.FLETCHER", "entity.Villager.fletcher");
        keys.put("VILLAGER.LIBRARIAN", "entity.Villager.librarian");
        keys.put("VILLAGER.CLERIC", "entity.Villager.cleric");
        keys.put("VILLAGER.ARMORER", "entity.Villager.armor");
        keys.put("VILLAGER.WEAPON_SMITH", "entity.Villager.weapon");
        keys.put("VILLAGER.TOOL_SMITH", "entity.Villager.tool");
        keys.put("VILLAGER.BUTCHER", "entity.Villager.butcher");
        keys.put("VILLAGER.LEATHERWORKER", "entity.Villager.leather");
        keys.put("VILLAGER.NITWIT", "entity.Villager.nitwit");
        keys.put("VILLAGER.CARTOGRAPHER", "entity.Villager.cartographer");
        keys.put("PIG", "entity.Pig.name");
        keys.put("SHEEP", "entity.Sheep.name");
        keys.put("COW", "entity.Cow.name");
        keys.put("CHICKEN", "entity.Chicken.name");
        keys.put("SQUID", "entity.Squid.name");
        keys.put("WOLF", "entity.Wolf.name");
        keys.put("OCELOT", "entity.Ozelot.name");
        keys.put("BLACK_CAT", "entity.Cat.name");
        keys.put("RED_CAT", "entity.Cat.name"); // added
        keys.put("SIAMESE_CAT", "entity.Cat.name"); // added
        keys.put("BAT", "entity.Bat.name");
        keys.put("HORSE", "entity.horse.name");
        keys.put("DONKEY", "entity.donkey.name");
        keys.put("MULE", "entity.mule.name");
        keys.put("SKELETON_HORSE", "entity.skeletonhorse.name");
        keys.put("ZOMBIE_HORSE", "entity.zombiehorse.name");
        keys.put("RABBIT", "entity.Rabbit.name");
        keys.put("RABBIT.THE_KILLER_BUNNY", "entity.KillerBunny.name");
        keys.put("LLAMA", "entity.Llama.name");
        keys.put("LLAMA_SPIT", "entity.Llama.name"); // added
        keys.put("PRIMED_TNT", "entity.PrimedTnt.name");
        keys.put("FALLING_BLOCK", "entity.FallingSand.name");
        keys.put("MINECART", "entity.Minecart.name");
        keys.put("MINECART_HOPPER", "entity.MinecartHopper.name");
        keys.put("MINECART_CHEST", "entity.MinecartChest.name");
        keys.put("MINECART_COMMAND", "item.minecartCommandBlock.name"); // added
        keys.put("MINECART_FURNACE", "item.minecartFurnace.name"); // added
        keys.put("MINECART_HOPPER", "item.minecartHopper.name"); // added
        keys.put("MINECART_MOB_SPAWNER", "entity.Minecart.name"); // added
        keys.put("MINECART_TNT", "item.minecartTnt.name"); // added
        keys.put("BOAT", "entity.Boat.name");
        keys.put("UNKNOWN", "entity.generic.name");
        keys.put("SPECTRAL_ARROW", "item.spectral_arrow.name"); // added
        keys.put("TIPPED_ARROW", "item.tipped_arrow.name"); // added
        keys.put("ENDER_CRYSTAL", "item.end_crystal.name"); // added 
        keys.put("ENDER_PEARL", "item.enderPearl.name"); // added
        keys.put("ENDER_SIGNAL", "item.end_crystal.name"); // added
        keys.put("LEASH_HITCH", "item.leash.name"); // added
        keys.put("ITEM_FRAME", "item.frame.name"); // added
        keys.put("FISHING_HOOK", "item.fishingRod.name"); // added
        keys.put("COMPLEX_PART", "entity.EnderDragon.name"); // added
        return keys;
    }
}
