package me.blackvein.quests.util;

import java.util.LinkedList;
import java.util.Map.Entry;

import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;
import net.milkbowl.vault.item.Items;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemUtil implements ColorUtil {
	
	static Quests plugin;
	
    /**
     * Will compare stacks by name, amount, data, display name/lore and enchantments
     *
     *
     * @param one ItemStack to compare
     * @param two ItemStack to compare to
     * @return 0 if stacks are equal, or the first inequality from the following
     * values:<br>
     * @return -1&nbsp;-> stack names are unequal<br>
     * @return -2&nbsp;-> stack amounts are unequal<br>
     * @return -3&nbsp;-> stack data is unequal<br>
     * @return -4&nbsp;-> stack display name/lore is unequal<br>
     * @return -5&nbsp;-> stack enchantments are unequal<br>
     */
    public static int compareItems(ItemStack one, ItemStack two, boolean ignoreAmount) {

        if (one == null && two != null || one != null && two == null) {
            return 0;
        }

        if (one == null && two == null) {
            return 0;
        }

        if (one.getType().name() != two.getType().name()) {
            return -1;
        } else if ((one.getAmount() != two.getAmount()) && ignoreAmount == false) {
            return -2;
        } else if (one.getData().equals(two.getData()) == false) {
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

        }

        if (one.getEnchantments().equals(two.getEnchantments()) == false) {
            return -5;
        } else {
            return 0;
        }
    }

    //Formats ->  name-name:amount-amount:data-data:enchantment-enchantment level:displayname-displayname:lore-lore:
    //Returns null if invalid format
    public static ItemStack readItemStack(String data) {
        if (data == null) {
            return null;
        }
        
        ItemStack stack = null;
        String[] args = data.split(":");
        ItemMeta meta = null;
        LinkedList<String> lore = new LinkedList<String>();
        for (String arg : args) {
            if (arg.startsWith("name-")) {
            	//Attempt to match item name. Returns null if invalid format
            	try {
            		stack = new ItemStack(Material.matchMaterial(arg.substring(5)));
            	} catch (NullPointerException npe) {
            		return null;
            	}
                meta = stack.getItemMeta();
            } else if (arg.startsWith("amount-")) {
            	stack.setAmount(Integer.parseInt(arg.substring(7)));
            } else if (arg.startsWith("data-")) {
                stack.setDurability(Short.parseShort(arg.substring(5)));
            } else if (arg.startsWith("enchantment-")) {
                String[] enchs = arg.substring(12).split(" ");
                Enchantment e = Quests.getEnchantment(enchs[0]);
                meta.addEnchant(e, Integer.parseInt(enchs[1]), true);
            } else if (arg.startsWith("displayname-")) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', arg.substring(12)));
            } else if (arg.startsWith("lore-")) {
                lore.add(ChatColor.translateAlternateColorCodes('&', arg.substring(5)));
            } else {
            	return null;
            }

        }

        if (lore.isEmpty() == false) {
            meta.setLore(lore);
        }

        stack.setItemMeta(meta);

        return stack;

    }

    public static String serialize(ItemStack is) {

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
                serial += ":enchantment-" + Quester.enchantmentString(e.getKey()) + " " + e.getValue();
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

        }

        return serial;

    }

    public static String getDisplayString(ItemStack is) {

        String text;
        if (is == null) {
            return null;
        }
        if (is.hasItemMeta() && is.getItemMeta().hasDisplayName()) {
            text = "" + DARKAQUA + ITALIC + is.getItemMeta().getDisplayName() + RESET + AQUA + " x " + is.getAmount();
        } else {
            text = AQUA + Quester.prettyItemString(is.getType().name());
            if (is.getDurability() != 0) {
                text += AQUA + ":" + is.getDurability();
            }

            text += AQUA + " x " + is.getAmount();

            if (is.getEnchantments().isEmpty() == false) {
                text += " " + PURPLE + Lang.get("enchantedItem");
            }

        }

        return text;

    }

    public static String getString(ItemStack is) {

        String text;

        if (is.hasItemMeta() && is.getItemMeta().hasDisplayName()) {
            text = "" + DARKAQUA + ITALIC + is.getItemMeta().getDisplayName() + RESET + AQUA + " x " + is.getAmount();
        } else {
            text = AQUA + Quester.prettyItemString(is.getType().name());
            if (is.getDurability() != 0) {
                text += AQUA + ":" + is.getDurability();
            }

            text += AQUA + " x " + is.getAmount();

        }

        return text;

    }

    public static String getName(ItemStack is) {

        String text = "";

        if (is.hasItemMeta() && is.getItemMeta().hasDisplayName()) {
            text = "" + DARKAQUA + ITALIC + is.getItemMeta().getDisplayName();
        } else {
        	try {
        		text = AQUA + Items.itemByStack(is).getName();
        	} catch (NullPointerException ne) {
        		Bukkit.getLogger().severe("This error is caused by an incompatible version of Vault. Please update!");
        		ne.printStackTrace();
        	}
        }

        return text;

    }
    
    public static boolean isItem(ItemStack is) {
        
        if(is == null)
            return false;
        
        if(is.getType().equals(Material.AIR))
            return false;
        
        return true;
        
    }
    
    public static boolean isJournal(ItemStack is) {
        
        if(is == null)
            return false;
        
        if(is.hasItemMeta() == false)
            return false;
        
        if(is.getItemMeta().hasDisplayName() == false)
            return false;
        
        return is.getItemMeta().getDisplayName().equals(PINK + Lang.get("journalTitle"));
        
    }

}