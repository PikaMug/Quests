/*******************************************************************************************************
 * Copyright (c) 2014 PikaMug and contributors. All rights reserved.
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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@SuppressWarnings("deprecation")
public class ItemUtil {

    /*public static String toBase64(ItemStack itemStack) {
        if (itemStack == null) {
            return null;
        }
        try {
            final ByteArrayOutputStream io = new ByteArrayOutputStream();
            final BukkitObjectOutputStream os = new BukkitObjectOutputStream(io);
            os.writeObject(itemStack);
            os.flush();

            final byte[] serializedObject = io.toByteArray();
            return Base64.getEncoder().encodeToString(serializedObject);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ItemStack fromBase64(String encodedObject) {
        if (encodedObject == null) {
            return null;
        }
        final byte[] serializedObject = Base64.getDecoder().decode(encodedObject);

        try {
            final ByteArrayInputStream in = new ByteArrayInputStream((serializedObject));
            final BukkitObjectInputStream is = new BukkitObjectInputStream(in);
            final Object object = is.readObject();
            if (object instanceof ItemStack) {
                return (ItemStack)object;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }*/

    /**
     * Compare two stacks by name, amount, durability, display name, lore, enchantments, stored enchants and item flags
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
     * -6 if stack stored enchants are unequal<br>
     * -7 if stack item flags are unequal
     * -8 if stack Written Book data is unequal
     * -9 if stack Potion type is unequal
     */
    public static int compareItems(final ItemStack one, final ItemStack two, final boolean ignoreAmount) {
        return compareItems(one, two, ignoreAmount, false);
    }
    
    /**
     * Compare two stacks by name, amount, durability, display name, lore, enchantments, stored enchants and item flags
     *
     *
     * @param one first ItemStack to compare against second
     * @param two second ItemStack to compare against first
     * @param ignoreAmount whether to ignore stack amounts
     * @param ignoreDurability whether to ignore stack durabilities
     * @return 1 if either stack is null<br>
     * 0 if stacks are equal<br>
     * -1 if stack names are unequal<br>
     * -2 if stack amounts are unequal<br>
     * -3 if stack durability is unequal<br>
     * -4 if stack display name/lore is unequal<br>
     * -5 if stack enchantments are unequal<br>
     * -6 if stack stored enchants are unequal<br>
     * -7 if stack item flags are unequal
     * -8 if stack Written Book data is unequal
     * -9 if stack Potion type is unequal
     */
    public static int compareItems(final ItemStack one, final ItemStack two, final boolean ignoreAmount, 
            final boolean ignoreDurability) {
        if (one == null || two == null) {
            return 1;
        }
        if (one.getType().name().equals(two.getType().name()) == false) {
            return -1;
        } else if ((one.getAmount() != two.getAmount()) && ignoreAmount == false) {
            return -2;
        } else if ((one.getDurability() != two.getDurability()) && ignoreDurability == false) {
            if (one.getDurability() < 999 && two.getDurability() < 999) { // wildcard value
                return -3;
            }
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
            } else if (one.getItemMeta().hasDisplayName() && two.getItemMeta().hasDisplayName() 
                    && ChatColor.stripColor(one.getItemMeta().getDisplayName())
                    .equals(ChatColor.stripColor(two.getItemMeta().getDisplayName())) == false) {
                return -4;
            } else if (one.getItemMeta().hasLore() && two.getItemMeta().hasLore() 
                    && one.getItemMeta().getLore().equals(two.getItemMeta().getLore()) == false) {
                return -4;
            }
            try {
                final ItemMeta test = one.getItemMeta();
                test.setUnbreakable(true);
                // We're on 1.11+ so check ItemFlags
                for (final ItemFlag flag : ItemFlag.values()) {
                    if (one.getItemMeta().hasItemFlag(flag) == false && two.getItemMeta().hasItemFlag(flag)) {
                        return -7;
                    }
                }
            } catch (final Throwable tr) {
                // We're below 1.11 so don't check ItemFlags
            }
            if (one.getType().equals(Material.WRITTEN_BOOK)) {
                final BookMeta bmeta1 = (BookMeta) one.getItemMeta();
                final BookMeta bmeta2 = (BookMeta) two.getItemMeta();
                if (bmeta1.getTitle().equals(bmeta2.getTitle()) == false) {
                    if (bmeta1.getAuthor().equals(bmeta2.getAuthor()) == false) {
                        if (bmeta1.getPages().equals(bmeta2.getPages()) == false) {
                            return -8;
                        }
                    }
                }
            }
            if (one.getItemMeta() instanceof PotionMeta) {
                if (Material.getMaterial("LINGERING_POTION") != null) {
                    // Bukkit version is 1.9+
                    if (one.getType().equals(Material.POTION) || one.getType().equals(Material.LINGERING_POTION) 
                            || one.getType().equals(Material.SPLASH_POTION)) {
                        final PotionMeta pmeta1 = (PotionMeta) one.getItemMeta();
                        final PotionMeta pmeta2 = (PotionMeta) two.getItemMeta();
                        if (pmeta1.getBasePotionData().getType()
                                .equals(pmeta2.getBasePotionData().getType()) == false) {
                            return -9;
                        }
                        if (pmeta1.getBasePotionData().isExtended() != pmeta2.getBasePotionData().isExtended()) {
                            return -9;
                        }
                        if (pmeta1.getBasePotionData().isUpgraded() != pmeta2.getBasePotionData().isUpgraded()) {
                            return -9;
                        }
                    }
                }
            }
        }
        if (Material.getMaterial("LINGERING_POTION") == null) {
            if (one.getType().equals(Material.POTION)) {
                // Bukkit version is below 1.9
                final Potion pot1 = new Potion(one.getDurability());
                final Potion pot2 = new Potion(two.getDurability());
                if (pot1.getType() == null || pot2.getType() == null) {
                    return -9;
                }
                if (!pot1.getType().equals(pot2.getType())) {
                    return -9;
                }
            }
        }
        if (one.getEnchantments().equals(two.getEnchantments()) == false) {
            return -5;
        }
        if (one.getType().equals(Material.ENCHANTED_BOOK)) {
            final EnchantmentStorageMeta esmeta1 = (EnchantmentStorageMeta) one.getItemMeta();
            final EnchantmentStorageMeta esmeta2 = (EnchantmentStorageMeta) two.getItemMeta();
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
     * @param material Item name suitable for Material.getMaterial()
     * @param amount The number of items in the stack
     * @param durability The data value of the item, default of 0
     * @return ItemStack, or null if invalid format
     */
    public static ItemStack processItemStack(final String material, final int amount, final short durability) {
        if (material == null) {
            return null;
        }
        try {
            final Material mat = Material.getMaterial(material.toUpperCase());
            if (mat == null) {
                return null;
            }
            return new ItemStack(mat, amount, durability);
        } catch (final Exception e) {
            try {
                Bukkit.getLogger().warning(material + " x " + amount
                        + " is invalid! You may need to update your quests.yml or actions.yml "
                        + "in accordance with https://bit.ly/2BkBNNN");
                final Material mat = Material.matchMaterial(material, true);
                if (mat == null) {
                    return null;
                }
                return new ItemStack(mat, amount, durability);
            } catch (final Exception e2) {
                Bukkit.getLogger().severe("Unable to use LEGACY_" + material + " as item name");
                e2.printStackTrace();
                return null;
            }
        }
    }

    /**
     * Get ItemStack from formatted string. See serialize() for reverse function.
     *
     * <p>Supplied format = name-name:amount-amount:data-data:enchantment-enchantment level:displayname-displayname
     * :lore-lore
     * <p>May continue with extraneous data such as :ItemFlags-flags:stored-enchants:{enc, level}:internal-hashstring
     *
     * @deprecated Legacy code, do not use
     * @param data formatted string
     * @return ItemStack, or null if invalid format
     */
    public static ItemStack readItemStack(final String data) {
        if (data == null) {
            return null;
        }
        ItemStack stack = null;
        final String[] args = data.split(":");
        String name = null;
        int amount = 0;
        short durability = 0;
        final Map<Enchantment, Integer> enchs = new HashMap<Enchantment, Integer>();
        String display = null;
        final LinkedList<String> lore = new LinkedList<String>();
        final String[] flags = new String[10];
        final LinkedHashMap<Enchantment, Integer> stored = new LinkedHashMap<Enchantment, Integer>();
        int potionColor = -1;
        final LinkedHashMap<String, Object> extra = new LinkedHashMap<String, Object>();
        ItemMeta meta = null;
        PotionMeta pmeta = null;
        EnchantmentStorageMeta esmeta = null;
        for (final String targ : args) {
            final String arg = targ.replace("minecraft|", "minecraft:");
            if (arg.equals("")) {
                continue;
            }
            if (arg.startsWith("name-")) {
                name = arg.substring(5).toUpperCase();
            } else if (arg.startsWith("amount-")) {
                amount = Integer.parseInt(arg.substring(7));
            } else if (arg.startsWith("data-")) {
                durability = Short.parseShort(arg.substring(5));
            } else if (arg.startsWith("enchantment-")) {
                final String[] temp = arg.substring(12).split(" ");
                try {
                    final String key = Lang.getKey(temp[0]).replace(" ", "");
                    if (!key.equals("NULL")) {
                        // Legacy localized name
                        final Enchantment e = Enchantment.getByName(key.replace("ENCHANTMENT_", ""));
                        if (e != null) {
                            enchs.put(e, Integer.parseInt(temp[1]));
                        } else {
                            Bukkit.getLogger().severe("Legacy enchantment name \'" + temp[0] + "\' on " + name
                                    + " is invalid. Make sure it is spelled correctly");
                        }
                    } else {
                        // Modern enum name
                        if (Enchantment.getByName(temp[0]) != null) {
                            enchs.put(Enchantment.getByName(temp[0]), Integer.parseInt(temp[1]));
                        } else {
                            Bukkit.getLogger().severe("Enum enchantment name \'" + temp[0] + "\' on " + name
                                    + " is invalid. Make sure it is spelled correctly");
                        }
                    }
                } catch (final Exception e) {
                    Bukkit.getLogger().severe("The enchantment name \'" + temp[0] + "\' on " + name
                            + " is invalid. Make sure quests.yml is UTF-8 encoded");
                    return null;
                }
            } else if (arg.startsWith("displayname-")) {
                display = ChatColor.translateAlternateColorCodes('&', arg.substring(12));
            } else if (arg.startsWith("lore-")) {
                lore.add(ChatColor.translateAlternateColorCodes('&', arg.substring(5)));
            } else if (arg.startsWith("ItemFlags-")) {
                final int dash = arg.lastIndexOf('-');
                final String value = arg.substring(dash + 1);
                final String[] mapping = value.replace("[", "").replace("]", "").split(", ");
                int index = 0;
                for (final String s : mapping) {
                    flags[index] = s;
                    index++;
                }
            } else if (arg.startsWith("stored-enchants")) {
                final int dash = arg.lastIndexOf('-');
                final String value = arg.substring(dash + 1);
                final String[] mapping = value.replace("{", "").replace("}", "").split(", ");
                for (final String s : mapping) {
                    if (s.contains("=")) {
                        final String[] keyval = s.split("=");
                        stored.put(Enchantment.getByName(keyval[0]), Integer.valueOf(keyval[1]));
                    }
                }
            } else if (arg.contains("-")) {
                final int dash = arg.lastIndexOf('-');
                final String key = arg.substring(0, dash);
                final String value = arg.substring(dash + 1);

                int i = -1;
                try {
                    // Num such as book generation
                    i = Integer.valueOf(value);
                } catch (final NumberFormatException e) {
                    // Do nothing
                }

                if (i > -1) {
                    extra.put(key, i);
                } else if (value.startsWith("[") && value.endsWith("]")) {
                    // List such as book pages
                    final List<String> pages = Arrays.asList(value.split(", "));
                    extra.put(key, pages);
                } else if (value.startsWith("{") && value.endsWith("}")) {
                    // For nested mappings. Does NOT handle stored enchants, see earlier code
                    final String[] mapping = value.replace("{", "").replace("}", "").split(", ");
                    final Map<String, String> nested = new HashMap<String, String>();
                    for (final String s : mapping) {
                        if (s.contains("=")) {
                            final String[] keyval = s.split("=");
                            nested.put(keyval[0], keyval[1]);
                        } else {
                            Bukkit.getLogger().severe("Quests does not know how to handle "
                                    + value + " so please contact the developer on Github");
                            return null;
                        }
                    }
                    extra.put(key, nested);
                } else if (value.equals("true")) {
                    // For some NBT tags
                    try {
                        if (key.equalsIgnoreCase("unbreakable")) {
                            meta.setUnbreakable(true);
                        }
                    } catch (final Throwable tr) {
                        // ItemMeta.setUnbrekable() not introduced until 1.11
                        // However, NBT tags could be set by Spigot-only methods, so show error
                        Bukkit.getLogger().info("You are running a version of CraftBukkit"
                                + " for which Quests cannot set the NBT tag " + key);
                    }
                } else if (!key.contains("custom-color")){
                    extra.put(key, value);
                }
            } else if (arg.startsWith("[") && arg.endsWith("]")) {
                if (arg.contains("rgb")) {
                    // Custom potion color
                    final String[] mapping = arg.replace("[", "").replace("]", "").split("x");
                    potionColor = Integer.valueOf(mapping[1]);
                } else {
                    Bukkit.getLogger().severe("Quests does not know how to handle "
                            + arg + " so please contact the developer on Github");
                    return null;
                }
            } else {
                Bukkit.getLogger().severe("Quests was unable to read item argument: " + arg);
                return null;
            }
        }
        stack = processItemStack(name, amount, durability);
        if (stack == null) {
            return null;
        }
        meta = stack.getItemMeta();
        if (!extra.isEmpty()) {
            ItemMeta toLoad = null;
            toLoad = ItemUtil.deserializeItemMeta(meta.getClass(), extra);
            if (toLoad != null) {
                meta = toLoad;
            }
        }
        if (!enchs.isEmpty()) {
            for (final Enchantment e : enchs.keySet()) {
                try {
                    meta.addEnchant(e, enchs.get(e), true);
                } catch (final IllegalArgumentException iae) {
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
        for (final String flag : flags) {
            if (flag != null && !flag.equals("")) {
                try {
                    meta.addItemFlags(ItemFlag.valueOf(flag));
                } catch (final NullPointerException npe) {
                    Bukkit.getLogger().severe(flag + " is not a valid ItemFlag");
                } catch (final Throwable tr) {
                    // ItemMeta.addItemFlags() not introduced until 1.8.3
                    Bukkit.getLogger().info("You are running a version of CraftBukkit"
                            + " for which Quests cannot add the item flag " + flag);
                }
            }
        }
        if (potionColor != -1) {
            pmeta = (PotionMeta) meta;
            try {
                pmeta.setColor(Color.fromRGB(potionColor));
            } catch (final Throwable tr) {
                // PotionMeta.setColor() not introduced until 1.11 (?)
                Bukkit.getLogger().info("You are running a version of CraftBukkit"
                        + " for which Quests cannot set the potion color " + potionColor);
            }
        }
        if (stack.getType().equals(Material.ENCHANTED_BOOK)) {
            esmeta = (EnchantmentStorageMeta) meta;
            for (final Entry<Enchantment, Integer> e : stored.entrySet()) {
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
     * <p>Returned format = name-name:amount-amount:data-data:enchantment-enchantment level:displayname-displayname
     * :lore-lore:
     *
     * @deprecated Legacy code, do not use
     * @param is ItemStack
     * @return formatted string, or null if invalid stack
     */
    public static String serializeItemStack(final ItemStack is) {
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
            for (final Entry<Enchantment, Integer> e : is.getEnchantments().entrySet()) {
                serial += ":enchantment-" + e.getKey().getName() + " " + e.getValue();
            }
        }
        if (is.hasItemMeta()) {
            final ItemMeta meta = is.getItemMeta();
            if (meta.hasDisplayName()) {
                serial += ":displayname-" + meta.getDisplayName();
            }
            if (meta.hasLore()) {
                for (final String s : meta.getLore()) {
                    serial += ":lore-" + s;
                }
            }

            final LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
            map.putAll(meta.serialize());

            if (map.containsKey("lore")) {
                map.remove("lore");
            }
            if (map.containsKey("display-name")) {
                map.remove("display-name");
            }
            for (final String key : map.keySet()) {
                serial += ":" + key + "-" + map.get(key).toString().replace("minecraft:", "minecraft|");
            }
        }
        return serial;
    }

    /**
     * Essentially the reverse of ItemMeta.serialize()
     * 
     * @param itemMetaClass key/value map of metadata
     * @return ItemMeta
     */
    public static ItemMeta deserializeItemMeta(final Class<? extends ItemMeta> itemMetaClass, final Map<String, Object> args) {
        final DelegateDeserialization delegate = itemMetaClass.getAnnotation(DelegateDeserialization.class);
        return (ItemMeta) ConfigurationSerialization.deserializeObject(args, delegate.value());
    }

    /**
     * Returns a formatted display name. If none exists, returns item name.
     * Also returns formatted durability and amount.
     * Also includes formatted enchantments.
     * 
     * Format is ([display]name:durability) with (enchantments:levels) x (amount)
     * 
     * @param is ItemStack to check
     * @return true display or item name, plus durability and amount, plus enchantments
     */
    public static String getDisplayString(final ItemStack is) {
        String text;
        if (is == null) {
            return null;
        }
        if (is.hasItemMeta() && is.getItemMeta().hasDisplayName()) {
            text = "" + ChatColor.DARK_AQUA + ChatColor.ITALIC + is.getItemMeta().getDisplayName() + ChatColor.RESET 
                    + ChatColor.AQUA + " x " + is.getAmount();
        } else {
            text = ChatColor.AQUA + getName(is);
            if (is.getDurability() != 0) {
                text += ChatColor.AQUA + ":" + is.getDurability();
            }
            if (!is.getEnchantments().isEmpty()) {
                text += " " + ChatColor.GRAY + Lang.get("with") + ChatColor.DARK_PURPLE;
                for (final Entry<Enchantment, Integer> e : is.getEnchantments().entrySet()) {
                    text += " " + ItemUtil.getPrettyEnchantmentName(e.getKey()) + ":" + e.getValue();
                }
            }
            text += ChatColor.AQUA + " x " + is.getAmount();
        }
        return text;
    }
    
    /**
     * Returns a formatted display name, plus durability and amount. If none exists, returns item name.
     * 
     * Format is ([display]name:durability) x (amount)
     * 
     * @param is ItemStack to check
     * @return true display or item name, plus durability and amount, if stack is not null
     */
    public static String getString(final ItemStack is) {
        if (is == null) {
            return null;
        }
        String text;
        if (is.hasItemMeta() && is.getItemMeta().hasDisplayName()) {
            text = "" + ChatColor.DARK_AQUA + ChatColor.ITALIC + is.getItemMeta().getDisplayName() + ChatColor.RESET 
                    + ChatColor.AQUA + " x " + is.getAmount();
        } else {
            text = ChatColor.AQUA + getPrettyItemName(is.getType().name());
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
     * @return true display or item name, if stack is not null
     */
    public static String getName(final ItemStack is) {
        if (is == null) {
            return null;
        }
        String text = "";
        if (is.hasItemMeta() && is.getItemMeta().hasDisplayName()) {
            text = "" + ChatColor.DARK_AQUA + ChatColor.ITALIC + is.getItemMeta().getDisplayName();
        } else {
            text = ChatColor.AQUA + getPrettyItemName(is.getType().name());
        }
        return text;
    }

    /**
     * Ensures that an ItemStack is a valid, non-AIR material
     * 
     * @param is ItemStack to check
     * @return true if stack is not null or Material.AIR
     */
    public static boolean isItem(final ItemStack is) {
        if (is == null) {
            return false;
        }
        return !is.getType().equals(Material.AIR);
    }

    /**
     * Checks whether an ItemStack is a Quest Journal based on book title
     * 
     * @param is ItemStack to check
     * @return true if display name equals colored journal title
     */
    public static boolean isJournal(final ItemStack is) {
        if (is == null) {
            return false;
        }
        if (!is.hasItemMeta()) {
            return false;
        }
        if (!is.getItemMeta().hasDisplayName()) {
            return false;
        }
        return is.getItemMeta().getDisplayName().equals(ChatColor.LIGHT_PURPLE + Lang.get("journalTitle"));
    }
    

    /**
     * Cleans up item names. 'WOODEN_BUTTON' becomes 'Wooden Button'
     * 
     * @param itemName any item name, ideally
     * @return cleaned-up string
     */
    public static String getPrettyItemName(final String itemName) {
        final Material material = Material.matchMaterial(itemName);
        if (material == null) {
            return "invalid";
        }
        final String baseString = material.toString();
        final String[] substrings = baseString.split("_");
        String prettyString = "";
        int size = 1;
        for (final String s : substrings) {
            prettyString = prettyString.concat(MiscUtil.getCapitalized(s));
            if (size < substrings.length) {
                prettyString = prettyString.concat(" ");
            }
            size++;
        }
        return prettyString;
    }
    
    /**
     * Gets player-friendly name from enchantment. 'FIRE_ASPECT' becomes 'Fire Aspect'
     * 
     * @param e Enchantment to get pretty localized name of
     * @return pretty localized name
     */
    public static String getPrettyEnchantmentName(final Enchantment e) {
        final String baseString = e.getName();
        final String[] substrings = baseString.split("_");
        String prettyString = "";
        int size = 1;
        for (final String s : substrings) {
            prettyString = prettyString.concat(MiscUtil.getCapitalized(s));
            if (size < substrings.length) {
                prettyString = prettyString.concat(" ");
            }
            size++;
        }
        return prettyString;
    }
    
    /**
     * Gets enchantment from name
     * 
     * @param properName Name to get enchantment from
     * @return Enchantment or null if invalid
     */
    public static Enchantment getEnchantmentFromProperName(String properName) {
        properName = properName.replace(" ", "").toUpperCase();
        for (final Enchantment e : Enchantment.values()) {
            if (e.getName().replace("_", "").equalsIgnoreCase(properName.replace("_", ""))) {
                return e;
            }
            if (getEnchantmentFromProperLegacyName(properName) != null) {
                return e;
            }
        }
        return null;
    }

    /**
     * Gets Enchantment from name as it appears in lang file
     * 
     * @deprecated Use {@link #getEnchantmentFromProperName(String)}
     * @param enchant Name to match lang value to
     * @return Enchantment or null if invalid
     */
    @Deprecated
    public static Enchantment getEnchantmentFromProperLegacyName(final String enchant) {
        if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_ARROW_DAMAGE"))) {
            return Enchantment.ARROW_DAMAGE;
        } else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_ARROW_FIRE"))) {
            return Enchantment.ARROW_FIRE;
        } else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_ARROW_INFINITE"))) {
            return Enchantment.ARROW_INFINITE;
        } else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_ARROW_KNOCKBACK"))) {
            return Enchantment.ARROW_KNOCKBACK;
        } else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_DAMAGE_ALL"))) {
            return Enchantment.DAMAGE_ALL;
        } else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_DAMAGE_ARTHROPODS"))) {
            return Enchantment.DAMAGE_ARTHROPODS;
        } else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_DAMAGE_UNDEAD"))) {
            return Enchantment.DAMAGE_UNDEAD;
        } else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_DIG_SPEED"))) {
            return Enchantment.DIG_SPEED;
        } else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_DURABILITY"))) {
            return Enchantment.DURABILITY;
        } else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_FIRE_ASPECT"))) {
            return Enchantment.FIRE_ASPECT;
        } else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_KNOCKBACK"))) {
            return Enchantment.KNOCKBACK;
        } else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_LOOT_BONUS_BLOCKS"))) {
            return Enchantment.LOOT_BONUS_BLOCKS;
        } else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_LOOT_BONUS_MOBS"))) {
            return Enchantment.LOOT_BONUS_MOBS;
        } else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_LUCK"))) {
            return Enchantment.LOOT_BONUS_MOBS;
        } else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_LURE"))) {
            return Enchantment.LOOT_BONUS_MOBS;
        } else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_OXYGEN"))) {
            return Enchantment.OXYGEN;
        } else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_PROTECTION_ENVIRONMENTAL"))) {
            return Enchantment.PROTECTION_ENVIRONMENTAL;
        } else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_PROTECTION_EXPLOSIONS"))) {
            return Enchantment.PROTECTION_EXPLOSIONS;
        } else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_PROTECTION_FALL"))) {
            return Enchantment.PROTECTION_FALL;
        } else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_PROTECTION_FIRE"))) {
            return Enchantment.PROTECTION_FIRE;
        } else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_PROTECTION_PROJECTILE"))) {
            return Enchantment.PROTECTION_PROJECTILE;
        } else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_SILK_TOUCH"))) {
            return Enchantment.SILK_TOUCH;
        } else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_THORNS"))) {
            return Enchantment.THORNS;
        } else if (enchant.equalsIgnoreCase(Lang.get("ENCHANTMENT_WATER_WORKER"))) {
            return Enchantment.WATER_WORKER;
        } else {
            return null;
        }
    }

    public static Enchantment getEnchantmentFromPrettyName(String enchant) {
        if (enchant != null) {
            while (MiscUtil.spaceToCapital(enchant) != null) {
                enchant = MiscUtil.spaceToCapital(enchant);
            }
        }
        return getEnchantmentFromProperName(enchant);
    }
}
