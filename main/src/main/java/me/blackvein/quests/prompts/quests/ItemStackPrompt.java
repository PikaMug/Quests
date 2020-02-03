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

package me.blackvein.quests.prompts.quests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.FixedSetPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.blackvein.quests.util.ConfigUtil;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;
import me.blackvein.quests.util.RomanNumeral;

public class ItemStackPrompt extends FixedSetPrompt {

    // Stores ItemStack in "tempStack" context data
    // Stores name in "tempName"
    // Stores amount in "tempAmount"
    // Stores durability in "tempData"
    // Stores enchantments in "tempEnchantments"
    // Stores display name in "tempDisplay"
    // Stores lore in "tempLore"
    // Stores metadata in "tempMeta"
    private final Prompt oldPrompt;

    public ItemStackPrompt(Prompt old) {
        super("0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
        oldPrompt = old;
    }

    @SuppressWarnings("unchecked")
    @Override
    public String getPromptText(ConversationContext cc) {
        String menu = ChatColor.YELLOW + Lang.get("createItemTitle") + "\n";
        LinkedHashMap<String, Object> map = null;
        if (cc.getSessionData("tempName") != null) {
            String stackData = getItemData(cc);
            if (stackData != null) {
                menu += stackData;
                if (cc.getSessionData("tempMeta") != null) {
                    map = (LinkedHashMap<String, Object>) cc.getSessionData("tempMeta");
                    if (!map.isEmpty()) {
                        for (String key : map.keySet()) {
                            if (key.equals("pages")) {
                                List<String> pages = (List<String>) map.get(key);
                                menu += ChatColor.GRAY + "\u2515 " + ChatColor.DARK_GREEN + key + "=" + pages.size() 
                                        + "\n";
                            } else {
                                menu += ChatColor.GRAY + "\u2515 " + ChatColor.DARK_GREEN + key + "=" + map.get(key) 
                                        + "\n";
                            }
                        }
                    }
                }
            }
        } else {
            menu += "\n";
        }
        menu += ChatColor.GOLD + "" + ChatColor.BOLD + "0. " + ChatColor.RESET + "" + ChatColor.YELLOW 
                + Lang.get("itemCreateLoadHand") + "\n";
        menu += ChatColor.YELLOW + "" + ChatColor.BOLD + "1. " + ChatColor.RESET + "" + ChatColor.GOLD 
                + Lang.get("itemCreateSetName") + "\n";
        menu += ChatColor.YELLOW + "" + ChatColor.BOLD + "2. " + ChatColor.RESET + "" + ChatColor.GOLD 
                + Lang.get("itemCreateSetAmount") + "\n";
        menu += ChatColor.YELLOW + "" + ChatColor.BOLD + "3. " + ChatColor.RESET + "" + ChatColor.GOLD 
                + Lang.get("itemCreateSetDurab") + "\n";
        menu += ChatColor.YELLOW + "" + ChatColor.BOLD + "4. " + ChatColor.RESET + "" + ChatColor.GOLD 
                + Lang.get("itemCreateSetEnchs") + "\n";
        menu += ChatColor.YELLOW + "" + ChatColor.BOLD + "5. " + ChatColor.RESET + "" + ChatColor.ITALIC 
                + ChatColor.GOLD + Lang.get("itemCreateSetDisplay") + "\n";
        menu += ChatColor.YELLOW + "" + ChatColor.BOLD + "6. " + ChatColor.RESET + "" + ChatColor.ITALIC 
                + ChatColor.GOLD + Lang.get("itemCreateSetLore") + "\n";
        if (map != null) {
            if (!map.isEmpty()) {
                menu += ChatColor.YELLOW + "" + ChatColor.BOLD + "7. " + ChatColor.RESET + "" + ChatColor.DARK_GREEN 
                        + Lang.get("itemCreateSetClearMeta") + "\n";
            } else {
                menu += ChatColor.GRAY + "" + ChatColor.BOLD + "7. " + ChatColor.RESET + "" + ChatColor.GRAY 
                        + Lang.get("itemCreateSetClearMeta") + "\n";
            }
        } else {
            menu += ChatColor.GRAY + "" + ChatColor.BOLD + "7. " + ChatColor.RESET + "" + ChatColor.GRAY 
                    + Lang.get("itemCreateSetClearMeta") + "\n";
        }
        menu += ChatColor.RED + "" + ChatColor.BOLD + "8. " + ChatColor.RESET + "" + ChatColor.RED 
                + Lang.get("cancel") + "\n";
        menu += ChatColor.GREEN + "" + ChatColor.BOLD + "9. " + ChatColor.RESET + "" + ChatColor.GREEN 
                + Lang.get("done") + "\n";
        return menu;
    }

    @SuppressWarnings({ "unchecked", "deprecation" })
    @Override
    protected Prompt acceptValidatedInput(ConversationContext cc, String input) {
        if (input.equalsIgnoreCase("0")) {
            cc.setSessionData("tempMeta", null);
            
            Player player = (Player) cc.getForWhom();
            ItemStack is = player.getItemInHand();
            if (is == null || is.getType().equals(Material.AIR)) {
                player.sendMessage(ChatColor.RED + Lang.get("itemCreateNoItem"));
                return new ItemStackPrompt(oldPrompt);
            } else {
                cc.setSessionData("tempName", is.getType().name());
                cc.setSessionData("tempAmount", is.getAmount());
                cc.setSessionData("tempData", null);
                cc.setSessionData("tempEnchantments", null);
                cc.setSessionData("tempDisplay", null);
                cc.setSessionData("tempLore", null);
                if (is.getDurability() != 0) {
                    cc.setSessionData("tempData", is.getDurability());
                }
                if (is.getEnchantments() != null && is.getEnchantments().isEmpty() == false) {
                    cc.setSessionData("tempEnchantments", new HashMap<Enchantment, Integer>(is.getEnchantments()));
                }
                if (is.hasItemMeta()) {
                    ItemMeta meta = is.getItemMeta();
                    if (meta.hasDisplayName()) {
                        String display = meta.getDisplayName().replace(ChatColor.COLOR_CHAR, '&');
                        cc.setSessionData("tempDisplay", display);
                    }
                    if (meta.hasLore()) {
                        LinkedList<String> lore = new LinkedList<String>();
                        lore.addAll(meta.getLore());
                        cc.setSessionData("tempLore", lore);
                    }
                    LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
                    map.putAll(meta.serialize());
                    
                    /*for (String key : map.keySet()) {
                        String s = map.get(key).toString();
                        if (s.contains("minecraft:")) {
                            map.put(key, s.replace("minecraft:", "minecraft|"));
                        }
                    }*/

                    if (map.containsKey("lore")) {
                        map.remove("lore");
                    }
                    if (map.containsKey("display-name")) {
                        map.remove("display-name");
                    }
                    if (map != null && !map.isEmpty()) {
                        cc.setSessionData("tempMeta", map);
                    }
                }
                player.sendMessage(ChatColor.GREEN + Lang.get("itemCreateLoaded"));
                return new ItemStackPrompt(oldPrompt);
            }
        } else if (input.equalsIgnoreCase("1")) {
            cc.setSessionData("tempMeta", null);
            return new NamePrompt();
        } else if (input.equalsIgnoreCase("2")) {
            if (cc.getSessionData("tempName") != null) {
                return new AmountPrompt();
            } else {
                cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateNoName"));
                return new ItemStackPrompt(oldPrompt);
            }
        } else if (input.equalsIgnoreCase("3")) {
            if (cc.getSessionData("tempName") != null && cc.getSessionData("tempAmount") != null) {
                return new DataPrompt();
            } else {
                cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateNoNameAmount"));
                return new ItemStackPrompt(oldPrompt);
            }
        } else if (input.equalsIgnoreCase("4")) {
            if (cc.getSessionData("tempName") != null && cc.getSessionData("tempAmount") != null) {
                return new EnchantmentPrompt();
            } else {
                cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateNoNameAmount"));
                return new ItemStackPrompt(oldPrompt);
            }
        } else if (input.equalsIgnoreCase("5")) {
            if (cc.getSessionData("tempName") != null && cc.getSessionData("tempAmount") != null) {
                return new DisplayPrompt();
            } else {
                cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateNoNameAmount"));
                return new ItemStackPrompt(oldPrompt);
            }
        } else if (input.equalsIgnoreCase("6")) {
            if (cc.getSessionData("tempName") != null && cc.getSessionData("tempAmount") != null) {
                return new LorePrompt();
            } else {
                cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateNoNameAmount"));
                return new ItemStackPrompt(oldPrompt);
            }
        } else if (input.equalsIgnoreCase("7")) {
            if (cc.getSessionData("tempName") != null && cc.getSessionData("tempAmount") != null) {
                cc.setSessionData("tempMeta", null);
                return new ItemStackPrompt(oldPrompt);
            } else {
                cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateNoNameAmount"));
                return new ItemStackPrompt(oldPrompt);
            }
        } else if (input.equalsIgnoreCase("8")) {
            cc.setSessionData("tempStack", null);
            cc.setSessionData("tempName", null);
            cc.setSessionData("tempAmount", null);
            cc.setSessionData("tempData", null);
            cc.setSessionData("tempEnchantments", null);
            cc.setSessionData("tempDisplay", null);
            cc.setSessionData("tempLore", null);
            cc.setSessionData("tempMeta", null);
        } else if (input.equalsIgnoreCase("9")) {
            if (cc.getSessionData("tempName") != null && cc.getSessionData("tempAmount") != null) {
                String name = (String) cc.getSessionData("tempName");
                int amount = (Integer) cc.getSessionData("tempAmount");
                short data = -1;
                Map<Enchantment, Integer> enchs = null;
                String display = null;
                List<String> lore = null;
                if (cc.getSessionData("tempData") != null) {
                    data = (Short) cc.getSessionData("tempData");
                }
                if (cc.getSessionData("tempEnchantments") != null) {
                    enchs = (Map<Enchantment, Integer>) cc.getSessionData("tempEnchantments");
                }
                if (cc.getSessionData("tempDisplay") != null) {
                    display = ChatColor.translateAlternateColorCodes('&', (String) cc.getSessionData("tempDisplay"));
                }
                if (cc.getSessionData("tempLore") != null) {
                    lore = new ArrayList<String>();
                    LinkedList<String> loadedLore = (LinkedList<String>) cc.getSessionData("tempLore");
                    for (String line : loadedLore) {
                        lore.add(ChatColor.translateAlternateColorCodes('&', line));
                    }
                }
                
                ItemStack stack = new ItemStack(Material.matchMaterial(name), amount);
                
                if (data != -1) {
                    stack.setDurability((short) data);
                }
                
                ItemMeta meta = stack.getItemMeta();
                if ((Map<String, Object>) cc.getSessionData("tempMeta") != null) {
                    meta = ItemUtil.deserializeItemMeta(meta.getClass(), (Map<String, Object>) cc
                            .getSessionData("tempMeta"));
                }
                
                if (enchs != null) {
                    for (Entry<Enchantment, Integer> e : enchs.entrySet()) {
                        meta.addEnchant(e.getKey(), e.getValue(), true);
                    }
                }
                if (display != null) {
                    meta.setDisplayName(display);
                }
                if (lore != null) {
                    meta.setLore(lore);
                }
                
                stack.setItemMeta(meta);
                cc.setSessionData("tempStack", stack);
                cc.setSessionData("newItem", Boolean.TRUE);
            } else {
                cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateNoNameAmount"));
                return new ItemStackPrompt(oldPrompt);
            }
        }
        try {
            return oldPrompt;
        } catch (Exception e) {
            cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateCriticalError"));
            return Prompt.END_OF_CONVERSATION;
        }
    }

    private class NamePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext cc) {
            return ChatColor.YELLOW + Lang.get("itemCreateEnterName");
        }

        @Override
        public Prompt acceptInput(ConversationContext cc, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                String s = input.replace(":", "");
                Material mat = Material.matchMaterial(s.toUpperCase().replace(" ", "_"));
                if (mat == null) {
                    cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidName"));
                    return new NamePrompt();
                } else {
                    cc.setSessionData("tempName", mat.name());
                    cc.setSessionData("tempAmount", 1);
                    return new ItemStackPrompt(oldPrompt);
                }
            } else {
                return new ItemStackPrompt(oldPrompt);
            }
        }
    }

    private class AmountPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext cc) {
            return ChatColor.YELLOW + Lang.get("itemCreateEnterAmount");
        }

        @Override
        public Prompt acceptInput(ConversationContext cc, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                try {
                    int amt = Integer.parseInt(input);
                    if (amt < 1 || amt > 64) {
                        cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidRange")
                                .replace("<least>", "1").replace("<greatest>", "64"));
                        return new AmountPrompt();
                    } else {
                        cc.setSessionData("tempAmount", Integer.parseInt(input));
                        return new ItemStackPrompt(oldPrompt);
                    }
                } catch (NumberFormatException e) {
                    cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
                    return new AmountPrompt();
                }
            } else {
                return new ItemStackPrompt(oldPrompt);
            }
        }
    }

    private class DataPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext cc) {
            return ChatColor.YELLOW + Lang.get("itemCreateEnterDurab");
        }

        @Override
        public Prompt acceptInput(ConversationContext cc, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                try {
                    int amt = Integer.parseInt(input);
                    if (amt < 1) {
                        cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidDurab"));
                        return new DataPrompt();
                    } else {
                        cc.setSessionData("tempData", Short.parseShort(input));
                        return new ItemStackPrompt(oldPrompt);
                    }
                } catch (NumberFormatException e) {
                    if (input.equals("*")) {
                        cc.setSessionData("tempData", Short.parseShort("999")); // wildcard value
                        return new ItemStackPrompt(oldPrompt);
                    }
                    cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
                    return new DataPrompt();
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                cc.setSessionData("tempData", null);
            }
            return new ItemStackPrompt(oldPrompt);
        }
    }

    private class EnchantmentPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext cc) {
            String text = ChatColor.LIGHT_PURPLE + Lang.get("enchantmentsTitle") + "\n";
            for (Enchantment e : Enchantment.values()) {
                text += ChatColor.GREEN + ItemUtil.getPrettyEnchantmentName(e) + ", ";
            }
            text = text.substring(0, text.length() - 2);
            return text + "\n" + ChatColor.YELLOW + Lang.get("itemCreateEnterEnch");
        }

        @Override
        public Prompt acceptInput(ConversationContext cc, String input) {
            String s = input.replace(":", "");
            if (s.equalsIgnoreCase(Lang.get("cmdClear")) == false 
                    && s.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                Enchantment e = ItemUtil.getEnchantmentFromPrettyName(MiscUtil.getCapitalized(s));
                if (e != null) {
                    cc.setSessionData("tempEnchant", e);
                    return new LevelPrompt(ItemUtil.getPrettyEnchantmentName(e));
                } else {
                    cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidEnch"));
                    return new EnchantmentPrompt();
                }
            } else if (s.equalsIgnoreCase(Lang.get("cmdClear"))) {
                cc.setSessionData("tempEnchantments", null);
            }
            return new ItemStackPrompt(oldPrompt);
        }

        protected class LevelPrompt extends StringPrompt {

            final String enchantment;

            protected LevelPrompt(String ench) {
                enchantment = ench;
            }

            @Override
            public String getPromptText(ConversationContext cc) {
                String text = Lang.get("itemCreateEnterLevel");
                text = text.replace("<enchantment>", enchantment);
                return ChatColor.AQUA + text;
            }

            @Override
            public Prompt acceptInput(ConversationContext cc, String input) {
                try {
                    int num = Integer.parseInt(input);
                    if (num < 1) {
                        cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidMinimum")
                                .replace("<number>", "1"));
                        return new LevelPrompt(enchantment);
                    } else {
                        if (cc.getSessionData("tempEnchantments") != null) {
                            @SuppressWarnings("unchecked")
                            Map<Enchantment, Integer> enchs 
                                    = (Map<Enchantment, Integer>) cc.getSessionData("tempEnchantments");
                            enchs.put((Enchantment) cc.getSessionData("tempEnchant"), num);
                            cc.setSessionData("tempEnchantments", enchs);
                        } else {
                            Map<Enchantment, Integer> enchs = new HashMap<Enchantment, Integer>();
                            enchs.put((Enchantment) cc.getSessionData("tempEnchant"), num);
                            cc.setSessionData("tempEnchantments", enchs);
                        }
                        return new ItemStackPrompt(oldPrompt);
                    }
                } catch (NumberFormatException e) {
                    cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqNotANumber").replace("<input>", input));
                    return new LevelPrompt(enchantment);
                }
            }
        }
    }

    private class DisplayPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext cc) {
            return ChatColor.YELLOW + Lang.get("itemCreateEnterDisplay");
        }

        @Override
        public Prompt acceptInput(ConversationContext cc, String input) {
            String s = input.replace(":", "");
            if (s.equalsIgnoreCase(Lang.get("cmdCancel")) == false 
                    && s.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                s = ConfigUtil.parseString(s);
                cc.setSessionData("tempDisplay", s);
            } else if (s.equalsIgnoreCase(Lang.get("cmdClear"))) {
                cc.setSessionData("tempDisplay", null);
            }
            return new ItemStackPrompt(oldPrompt);
        }
    }

    private class LorePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext cc) {
            return ChatColor.YELLOW + Lang.get("itemCreateEnterLore");
        }

        @Override
        public Prompt acceptInput(ConversationContext cc, String input) {
            String s = input.replace(":", "");
            if (s.equalsIgnoreCase(Lang.get("cmdCancel")) == false 
                    && s.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                s = ConfigUtil.parseString(s);
                LinkedList<String> lore = new LinkedList<String>();
                lore.addAll(Arrays.asList(s.split(Lang.get("charSemi"))));
                cc.setSessionData("tempLore", lore);
            } else if (s.equalsIgnoreCase("clear")) {
                cc.setSessionData("tempLore", null);
            }
            return new ItemStackPrompt(oldPrompt);
        }
    }

    private String getItemData(ConversationContext cc) {
        if (cc.getSessionData("tempName") != null) {
            String item;
            if (cc.getSessionData("tempDisplay") == null) {
                String name = (String) cc.getSessionData("tempName");
                item = ChatColor.AQUA + ItemUtil.getPrettyItemName(name);
                if (cc.getSessionData("tempData") != null) {
                    item += ":" + ChatColor.BLUE + (Short) cc.getSessionData("tempData");
                }
            } else {
                item = ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC + (String) cc.getSessionData("tempDisplay") 
                        + ChatColor.RESET + "" + ChatColor.GRAY + " (";
                String name = (String) cc.getSessionData("tempName");
                item += ChatColor.AQUA + ItemUtil.getPrettyItemName(name);
                if (cc.getSessionData("tempData") != null) {
                    item += ":" + ChatColor.BLUE + (Short) cc.getSessionData("tempData");
                }
                item += ChatColor.GRAY + ")";
            }
            if (cc.getSessionData("tempAmount") != null) {
                item += ChatColor.GRAY + " x " + ChatColor.DARK_AQUA + (Integer) cc.getSessionData("tempAmount");
            } else {
                item += ChatColor.GRAY + " x " + ChatColor.DARK_AQUA + "1";
            }
            item += "\n";
            if (cc.getSessionData("tempEnchantments") != null) {
                @SuppressWarnings("unchecked")
                Map<Enchantment, Integer> enchantments 
                        = (Map<Enchantment, Integer>) cc.getSessionData("tempEnchantments");
                for (Entry<Enchantment, Integer> e : enchantments.entrySet()) {
                    item += ChatColor.GRAY + "  - " + ChatColor.RED + ItemUtil.getPrettyEnchantmentName(e.getKey()) 
                            + " " + RomanNumeral.getNumeral(e.getValue()) + "\n";
                }
            }
            if (cc.getSessionData("tempLore") != null) {
                @SuppressWarnings("unchecked")
                List<String> lore = (List<String>) cc.getSessionData("tempLore");
                item += ChatColor.DARK_GREEN + "(Lore)\n\"";
                for (String s : lore) {
                    if (lore.indexOf(s) != (lore.size() - 1)) {
                        item += ChatColor.DARK_GREEN + "" + ChatColor.ITALIC + s + "\n";
                    } else {
                        item += ChatColor.DARK_GREEN + "" + ChatColor.ITALIC + s + "\"\n";
                    }
                }
            }
            item += "\n";
            return item;
        } else {
            return null;
        }
    }
}
