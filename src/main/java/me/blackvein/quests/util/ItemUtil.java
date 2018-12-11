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

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import me.blackvein.quests.Quester;

public class ItemUtil {

	/**
	 * Compare two stacks by name, amount, durability, display name, lore, enchantments and stored enchants
	 *
	 *
	 * @param one first ItemStack to compare against second
	 * @param two second ItemStack to compare against first
	 * @param ignoreAmount whether to ignore stack amounts
	 * @return 1 if either stack is null<br>
	 * 0 if stacks are equal<br>
	 * -1 if stack names are unequal<br>
	 * -2 if stack amounts are unequal<br>
	 * -3 if stack durability is unequal<br>
	 * -4 if stack display name/lore is unequal<br>
	 * -5 if stack enchantments are unequal<br>
	 * -6 if stack stored enchants are unequal
	 */
	@SuppressWarnings("deprecation")
	public static int compareItems(ItemStack one, ItemStack two, boolean ignoreAmount) {
		if (one == null || two == null) {
			return 1;
		}
		if (one.getType().name().equals(two.getType().name()) == false) {
			return -1;
		} else if ((one.getAmount() != two.getAmount()) && ignoreAmount == false) {
			return -2;
		} else if (one.getDurability() != two.getDurability()) {
			return -3;
		}
		if (one.hasItemMeta() || two.hasItemMeta()) {
			if (one.hasItemMeta() && two.hasItemMeta() == false) {
				return -4;
			} else if (one.hasItemMeta() == false && two.hasItemMeta()) {
				return -4;
			} else if (one.getItemMeta().hasDisplayName() && two.getItemMeta().hasDisplayName() == false) {
				return -4;
			} else if (one.getItemMeta().hasDisplayName() == false && two.getItemMeta().hasDisplayName()) {
				return -4;
			} else if (one.getItemMeta().hasLore() && two.getItemMeta().hasLore() == false) {
				return -4;
			} else if (one.getItemMeta().hasLore() == false && two.getItemMeta().hasLore()) {
				return -4;
			} else if (one.getItemMeta().hasDisplayName() && two.getItemMeta().hasDisplayName() && ChatColor.stripColor(one.getItemMeta().getDisplayName()).equals(ChatColor.stripColor(two.getItemMeta().getDisplayName())) == false) {
				return -4;
			} else if (one.getItemMeta().hasLore() && two.getItemMeta().hasLore() && one.getItemMeta().getLore().equals(two.getItemMeta().getLore()) == false) {
				return -4;
			}
			for (ItemFlag flag : ItemFlag.values()) {
				if (one.getItemMeta().hasItemFlag(flag) == false && two.getItemMeta().hasItemFlag(flag)) {
					return -4;
				}
			}
		}
		if (one.getEnchantments().equals(two.getEnchantments()) == false) {
			return -5;
		}
		if (one.getType().equals(Material.ENCHANTED_BOOK)) {
			EnchantmentStorageMeta esmeta1 = (EnchantmentStorageMeta) one.getItemMeta();
			EnchantmentStorageMeta esmeta2 = (EnchantmentStorageMeta) two.getItemMeta();
			if (esmeta1.hasStoredEnchants() && esmeta2.hasStoredEnchants() == false) {
				return -6;
			}
			if (esmeta1.getStoredEnchants().equals(esmeta2.getStoredEnchants()) == false) {
				return -6;
			}
		}
		return 0;
	}
	
	/**
	 * Returns an ItemStack based on given values. Checks for legacy pre-1.13 names. Other traits such as
	 * enchantments and lore cannot be added via this method and must be done separately.
	 * 
	 * @param material Item name suitable for Material.matchMaterial()
	 * @param amount The number of items in the stack
	 * @param durability The data value of the item, default of 0
	 * @return ItemStack, or null if invalid format
	 */
	@SuppressWarnings("deprecation")
	public static ItemStack processItemStack(String material, int amount, short durability) {
		try {
			return new ItemStack(Material.matchMaterial(material), amount, durability);
		} catch (Exception e) {
			try {
				Bukkit.getLogger().warning(material + " is invalid! You may need to update your quests.yml or events.yml "
						+ "in accordance with https://bit.ly/2BkBNNN");
				return new ItemStack(Material.matchMaterial(material, true), amount, durability);
			} catch (Exception e2) {
				Bukkit.getLogger().severe("Unable to use LEGACY_" + material + " for as item name");
				e2.printStackTrace();
				return null;
			}
		}
	}

	/**
	 * Get ItemStack from formatted string. See serialize() for reverse function.
	 * 
	 * <p>Supplied format = name-name:amount-amount:data-data:enchantment-enchantment level:displayname-displayname:lore-lore
	 * <p>May continue with extraneous data such as :ItemFlags-flags:stored-enchants:{enc, level}:internal-hashstring
	 * 
	 * @param data formatted string
	 * @return ItemStack, or null if invalid format
	 */
	@SuppressWarnings("deprecation")
	public static ItemStack readItemStack(String data) {
		if (data == null) {
			return null;
		}
		ItemStack stack = null;
		String[] args = data.split(":");
		String name = null;
		int amount = 0;
		short durability = 0;
		Map<Enchantment, Integer> enchs = new HashMap<Enchantment, Integer>();
		String display = null;
		LinkedList<String> lore = new LinkedList<String>();
		String[] flags = new String[10];
		//ItemFlag[] flags = new ItemFlag[ItemFlag.values().length];
		LinkedHashMap<Enchantment, Integer> stored = new LinkedHashMap<Enchantment, Integer>();
		LinkedHashMap<String, Object> extra = new LinkedHashMap<String, Object>();
		ItemMeta meta = null;
		EnchantmentStorageMeta esmeta = null;
		for (String targ : args) {
			String arg = targ.replace("minecraft|", "minecraft:");
			if (arg.startsWith("name-")) {
				name = arg.substring(5).toUpperCase();
			} else if (arg.startsWith("amount-")) {
				amount = Integer.parseInt(arg.substring(7));
			} else if (arg.startsWith("data-")) {
				durability = Short.parseShort(arg.substring(5));
			} else if (arg.startsWith("enchantment-")) {
				String[] temp = arg.substring(12).split(" ");
				try {
					String key = Lang.getKey(temp[0]).replace(" ", "");
					if (!key.equals("NULL")) {
						// Legacy localized name
						Enchantment e = Enchantment.getByName(key.replace("ENCHANTMENT_", ""));
						if (e != null) {
							enchs.put(e, Integer.parseInt(temp[1]));
						} else {
							Bukkit.getLogger().severe("Legacy enchantment name \'" + temp[0] + "\' on " + name + " is invalid. Make sure it is spelled correctly");
						}
					} else {
						// Modern enum name
						if (Enchantment.getByName(temp[0]) != null) {
							enchs.put(Enchantment.getByName(temp[0]), Integer.parseInt(temp[1]));
						} else {
							Bukkit.getLogger().severe("Enum enchantment name \'" + temp[0] + "\' on " + name + " is invalid. Make sure it is spelled correctly");
						}
					}
				} catch (Exception e) {
					Bukkit.getLogger().severe("The enchantment name \'" + temp[0] + "\' on " + name + " is invalid. Make sure quests.yml is UTF-8 encoded");
					return null;
				}
			} else if (arg.startsWith("displayname-")) {
				display = ChatColor.translateAlternateColorCodes('&', arg.substring(12));
			} else if (arg.startsWith("lore-")) {
				lore.add(ChatColor.translateAlternateColorCodes('&', arg.substring(5)));
			} else if (arg.startsWith("ItemFlags-")) {
				int dash = arg.lastIndexOf('-');
				String value = arg.substring(dash + 1);
				String[] mapping = value.replace("[", "").replace("]", "").split(", ");
				int index = 0;
				for (String s : mapping) {
					//flags[index] = ItemFlag.valueOf(s);
					flags[index] = s;
					index++;
				}
			} else if (arg.startsWith("stored-enchants")) {
				int dash = arg.lastIndexOf('-');
				String value = arg.substring(dash + 1);
				String[] mapping = value.replace("{", "").replace("}", "").split(", ");
				for (String s : mapping) {
					if (s.contains("=")) {
						String[] keyval = s.split("=");
						stored.put(Enchantment.getByName(keyval[0]), Integer.valueOf(keyval[1]));
					}
				}
			} else if (arg.contains("-")) {
				int dash = arg.lastIndexOf('-');
				String key = arg.substring(0, dash);
				String value = arg.substring(dash + 1);
				
				int i = -1;
				try {
					// Num such as book generation
					i = Integer.valueOf(value);
				} catch (NumberFormatException e) {
					// Do nothing
				}
				
				if (i > -1) {
					extra.put(key, i);
				} else if (value.startsWith("[") && value.endsWith("]")) {
					// List such as book pages
					List<String> pages = Arrays.asList(value.split(", "));
					extra.put(key, pages);
				} else if (value.startsWith("{") && value.endsWith("}")) {
					// For nested mappings. Does NOT handle stored enchants, see earlier code
					String[] mapping = value.replace("{", "").replace("}", "").split(", ");
					Map<String, String> nested = new HashMap<String, String>();
					for (String s : mapping) {
						if (s.contains("=")) {
							String[] keyval = s.split("=");
							nested.put(keyval[0], keyval[1]);
						} else {
							Bukkit.getLogger().severe("Quests does not know how to handle "
									+ value + " so please contact the developer on Github");
							return null;
						}
					}
					extra.put(key, nested);
				} else {
					extra.put(key, value);
				}
			} else {
				return null;
			}
		}
		stack = processItemStack(name, amount, durability);
		if (stack == null) {
			return null;
		}
		meta = stack.getItemMeta();
		if (!extra.isEmpty()) {
			meta = ItemUtil.deserializeItemMeta(meta.getClass(), (Map<String, Object>) extra);
		}
		if (!enchs.isEmpty()) {
			for (Enchantment e : enchs.keySet()) {
				try {
					meta.addEnchant(e, enchs.get(e), true);
				} catch (IllegalArgumentException iae) {
					Bukkit.getLogger().severe("Enchantment on " + name + " cannot be null. Skipping for that quest");
				}
			}
		}
		if (display != null) {
			meta.setDisplayName(display);
		}
		if (!lore.isEmpty()) {
			meta.setLore(lore);
		}
		if (flags[0] != null && flags[0].toString().equals("")) {
			for (String flag : flags) {
				if (flag != null) {
					try {
						meta.addItemFlags(ItemFlag.valueOf(flag));
					} catch (NullPointerException npe) {
						Bukkit.getLogger().severe(flag + " is not a valid ItemFlag");
					}
				}
			}
		}
		if (stack.getType().equals(Material.ENCHANTED_BOOK)) {
			esmeta = (EnchantmentStorageMeta) meta;
			for (Entry<Enchantment, Integer> e : stored.entrySet()) {
				esmeta.addStoredEnchant(e.getKey(), e.getValue(), true);
			}
			stack.setItemMeta(esmeta);
		} else {
			stack.setItemMeta(meta);
		}
		return stack;
	}

	/**
	 * Get formatted string from ItemStack. See readItemStack() for reverse function.
	 * 
	 * <p>Returned format = name-name:amount-amount:data-data:enchantment-enchantment level:displayname-displayname:lore-lore:
	 * 
	 * @param is ItemStack
	 * @return formatted string, or null if invalid stack
	 */
	@SuppressWarnings("deprecation")
	public static String serializeItemStack(ItemStack is) {
		String serial;
		if (is == null) {
			return null;
		}
		serial = "name-" + is.getType().name();
		serial += ":amount-" + is.getAmount();
		if (is.getDurability() != 0) {
			serial += ":data-" + is.getDurability();
		}
		if (is.getEnchantments().isEmpty() == false) {
			for (Entry<Enchantment, Integer> e : is.getEnchantments().entrySet()) {
				serial += ":enchantment-" + e.getKey().getName() + " " + e.getValue();
			}
		}
		if (is.hasItemMeta()) {
			ItemMeta meta = is.getItemMeta();
			if (meta.hasDisplayName()) {
				serial += ":displayname-" + meta.getDisplayName();
			}
			if (meta.hasLore()) {
				for (String s : meta.getLore()) {
					serial += ":lore-" + s;
				}
			}
			
			LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
			map.putAll(meta.serialize());
			
			if (map.containsKey("lore")) {
				map.remove("lore");
			}
			if (map.containsKey("display-name")) {
				map.remove("display-name");
			}
			for (String key : map.keySet()) {
				serial += ":" + key + "-" + map.get(key).toString().replace("minecraft:", "minecraft|");
			}
		}
		return serial;
	}
	
	/**
	 * Essentially the reverse of ItemMeta.serialize()
	 * 
	 * @param ItemMeta class, key/value map of metadata
	 * @return ItemMeta
	 */
	public static ItemMeta deserializeItemMeta(Class<? extends ItemMeta> itemMetaClass, Map<String, Object> args) {
		DelegateDeserialization delegate = itemMetaClass.getAnnotation(DelegateDeserialization.class);
		return (ItemMeta) ConfigurationSerialization.deserializeObject(args, delegate.value());
	}

	/**
	 * Returns a formatted display name. If none exists, returns item name.
	 * Also returns formatted durability and amount.
	 * A;so includes formatted enchantments.
	 * 
	 * Format is ([display]name:durability) with (enchantments:levels) x (amount)
	 * 
	 * @param is ItemStack to check
	 * @return true display or item name, plus durability and amount, plus enchantments
	 */
	@SuppressWarnings("deprecation")
	public static String getDisplayString(ItemStack is) {
		String text;
		if (is == null) {
			return null;
		}
		if (is.hasItemMeta() && is.getItemMeta().hasDisplayName()) {
			text = "" + ChatColor.DARK_AQUA + ChatColor.ITALIC + is.getItemMeta().getDisplayName() + ChatColor.RESET + ChatColor.AQUA + " x " + is.getAmount();
		} else {
			text = ChatColor.AQUA + getName(is);
			if (is.getDurability() != 0) {
				text += ChatColor.AQUA + ":" + is.getDurability();
			}
			if (is.getEnchantments().isEmpty() == false) {
				text += " " + ChatColor.GRAY + Lang.get("with") + ChatColor.DARK_PURPLE;
				for (Entry<Enchantment, Integer> e : is.getEnchantments().entrySet()) {
					text += " " + Quester.prettyEnchantmentString(e.getKey()) + ":" + e.getValue();
				}
			}
			text += ChatColor.AQUA + " x " + is.getAmount();
		}
		return text;
	}
	
	/**
	 * Returns a formatted display name. If none exists, returns item name.
	 * Also returns formatted durability and amount.
	 * 
	 * Format is ([display]name:durability) x (amount)
	 * 
	 * @param is ItemStack to check
	 * @return true display or item name, plus durability and amount
	 */
	@SuppressWarnings("deprecation")
	public static String getString(ItemStack is) {
		String text;
		if (is.hasItemMeta() && is.getItemMeta().hasDisplayName()) {
			text = "" + ChatColor.DARK_AQUA + ChatColor.ITALIC + is.getItemMeta().getDisplayName() + ChatColor.RESET + ChatColor.AQUA + " x " + is.getAmount();
		} else {
			text = ChatColor.AQUA + Quester.prettyItemString(is.getType().name());
			if (is.getDurability() != 0) {
				text += ChatColor.AQUA + ":" + is.getDurability();
			}
			text += ChatColor.AQUA + " x " + is.getAmount();
		}
		return text;
	}

	/**
	 * Returns a formatted display name. If none exists, returns item name.
	 * 
	 * @param is ItemStack to check
	 * @return true display or item name
	 */
	public static String getName(ItemStack is) {
		String text = "";
		if (is.hasItemMeta() && is.getItemMeta().hasDisplayName()) {
			text = "" + ChatColor.DARK_AQUA + ChatColor.ITALIC + is.getItemMeta().getDisplayName();
		} else {
			text = ChatColor.AQUA + Quester.prettyItemString(is.getType().name());
		}
		return text;
	}

	/**
	 * Ensures that an ItemStack is a valid, non-AIR material
	 * 
	 * @param is ItemStack to check
	 * @return true if stack is not null or Material.AIR
	 */
	public static boolean isItem(ItemStack is) {
		if (is == null)
			return false;
		if (is.getType().equals(Material.AIR))
			return false;
		return true;
	}

	/**
	 * Checks whether an ItemStack is a Quest Journal based on book title
	 * 
	 * @param is IemsStack to check
	 * @return true if display name equals colored journal title
	 */
	public static boolean isJournal(ItemStack is) {
		if (is == null)
			return false;
		if (is.hasItemMeta() == false)
			return false;
		if (is.getItemMeta().hasDisplayName() == false)
			return false;
		return is.getItemMeta().getDisplayName().equals(ChatColor.LIGHT_PURPLE + Lang.get("journalTitle"));
	}
}