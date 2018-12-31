/*******************************************************************************************************
 * Continued by FlyingPikachu/HappyPikachu with permission from _Blackvein_. All rights reserved.
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

import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class LocaleQuery {
	private static Class<?> craftMagicNumbers = null;
	private static Class<?> itemClazz = null;
	private final Quests plugin;
	public static boolean oldVersion = false;
	
	public LocaleQuery(Quests plugin){
		this.plugin = plugin;
		setup();
	}
	
	public void setBukkitVersion(String bukkitVersion) {
		oldVersion = isBelow113(bukkitVersion);
	}
	
	/**
	 * Send message with item name translated to the client's locale<p>
	 * 
	 * Durability arg is arbitrary for 1.13+
	 * 
	 * @param player The player for whom the message is to be sent
	 * @param message The message to be sent to the player
	 * @param material The item to be translated
	 * @param durability Durability for the item being translated
	 */
	public void sendMessage(Player player, String message, Material material, short durability) {
		if (plugin.translateItems) {
			String key = queryByType(material);
			if (key != null) {
				if (oldVersion) {
					if (key.startsWith("tile.") || key.startsWith("item.")) {
						key += getColorIfApplicable(material, durability) + ".name";
					}
				}
				String msg = message.replace("<item>", "\",{\"translate\":\"" + key + "\"},\"");
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + player.getName() + " [\"" + msg + "\"]");
				return;
			}
		}
		player.sendMessage(message.replace("<item>", Quester.prettyItemString(material.name())));
	}
	
	/**
	 * Send message with item name translated to the client's locale<p>
	 * 
	 * Durability arg is arbitrary for 1.13+
	 * 
	 * @param player The player for whom the message is to be sent
	 * @param message The message to be sent to the player
	 * @param material The item to be translated
	 * @param durability Durability for the item being translated
	 * @param enchantment The enchantment to be translated
	 */
	@SuppressWarnings("deprecation")
	public void sendMessage(Player player, String message, Material material, short durability, Enchantment enchantment) {
		if (plugin.translateItems) {
			String key = queryByType(material);
			if (key != null) {
				if (oldVersion) {
					if (key.startsWith("tile.") || key.startsWith("item.")) {
						key += getColorIfApplicable(material, durability) + ".name";
					}
				}
				String key2 = "";
				if (oldVersion) {
					key2 = "enchantment." + enchantment.getName().toLowerCase().replace("_", ".")
							.replace("environmental", "all").replace("protection", "protect");
				} else {
					key2 = "enchantment.minecraft." + enchantment.toString().toLowerCase();
				}
				if (!key2.equals("")) {
					String msg = message.replace("<item>", "\",{\"translate\":\"" + key + "\"},\"")
							.replace("<enchantment>", "\",{\"translate\":\"" + key2 + "\"},\"");
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + player.getName() + " [\"" + msg + "\"]");
					return;
				}
			}
		}
		player.sendMessage(message.replace("<item>", Quester.prettyItemString(material.name()))
				.replace("<enchantment>", Quester.prettyEnchantmentString(enchantment)));
	}
	
	@SuppressWarnings("deprecation")
	public void sendMessage(Player player, String message, EntityType type) {
		if (plugin.translateItems) {
			String key = "";
			if (oldVersion) {
				key = "entity." + type.getName() + ".name";
			} else {
				key = "entity.minecraft." + type.toString().toLowerCase();
			}
			if (!key.equals("")) {
				String msg = message.replace("<mob>", "\",{\"translate\":\"" + key + "\"},\"");
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + player.getName() + " [\"" + msg + "\"]");
				return;
			}
		}
		player.sendMessage(message.replace("<mob>", Quester.prettyMobString(type)));
	}
	
	/**
	 * Gets the key name of the specified material as seen in MC lang file
	 * @param material the material to check
	 * @return the raw key
	 * @throws IllegalArgumentException if an item with that material could not be found
	 */
	public String queryByType(Material material) throws IllegalArgumentException{
	    try {
	    	Object item = MethodUtils.invokeExactStaticMethod(craftMagicNumbers,"getItem", material);
	    	if (item == null) {
	    		throw new IllegalArgumentException("An item with that material could not be found! (Perhaps you have specified a block?)");
	    	}
	    	String name = (String) itemClazz.getMethod("getName").invoke(item);
	    	return name;
	    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
	    	e.printStackTrace();
	    }
	    return null;
	}
	
	public void setup() {
		String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
	    try {
	        craftMagicNumbers = Class.forName("org.bukkit.craftbukkit.{v}.util.CraftMagicNumbers".replace("{v}", version));
	        itemClazz = Class.forName("net.minecraft.server.{v}.Item".replace("{v}", version));
	    } catch (ClassNotFoundException e) {
	        e.printStackTrace();
	    }
	}
	
	public static boolean isBelow113(String bukkitVersion) {
		if (bukkitVersion.matches("^[0-9.]+$")) {
			switch(bukkitVersion) {
			case "1.12" :
				return true;
			case "1.11" :
				return true;
			case "1.10" :
				return true;
			case "1.9.4" :
				return true;
			case "1.9" :
				return true;
			case "1.8.4" :
				return true;
			case "1.8.3" :
				return true;
			case "1.8" :
				return true;
			case "1.7.10" :
				return true;
			case "1.7.9" :
				return true;
			default:
				// Bukkit version is 1.13+
				return false;
			}
		}
		Bukkit.getLogger().severe("Quests received invalid Bukkit version " + bukkitVersion);
		return false;
	}
	
	
    /**
     * Appends a color to an item. Note that this will make item names invalid if the item is not eligible<p>
     * 
     * Method not useful for MC 1.13+
     * 
     * @param material The material to be processed
     * @param durability The durability indicating variation
     * @return color, or blank string if not applicable
     */
    @SuppressWarnings("deprecation")
	public String getColorIfApplicable(Material material, short durability){
    	String key = "";
    	if (material.name().equals("INK_SACK")) {
			DyeColor dye = DyeColor.getByDyeData((byte)durability);
			key = "." + MiscUtil.fixUnderscore(dye.name().toLowerCase());
		} else if ((material.name().equals("WOOL") || material.name().equals("CARPET")) && durability == 0) {
			// White wool/carpet, do nothing
		} else if (material.name().equals("WOOL") || material.name().equals("CARPET") || material.name().equals("STAINED_CLAY")
				|| material.name().equals("STAINED_GLASS") || material.name().equals("STAINED_GLASS_PANE")) {
			DyeColor dye = DyeColor.getByWoolData((byte)durability);
			key = "." + MiscUtil.fixUnderscore(dye.name().toLowerCase());
		} else if (material.name().equals("LEATHER_HELMET") || material.name().equals("LEATHER_CHESTPLATE")
				|| material.name().equals("LEATHER_LEGGINGS") || material.name().equals("LEATHER_BOOTS")) {
			ItemStack is = new ItemStack(material, 1, durability);
			LeatherArmorMeta lam = (LeatherArmorMeta) is.getItemMeta();
			DyeColor dye = DyeColor.getByColor(lam.getColor());
			key = "." + MiscUtil.fixUnderscore(dye.name().toLowerCase());
		}
        return key;
    }
}
