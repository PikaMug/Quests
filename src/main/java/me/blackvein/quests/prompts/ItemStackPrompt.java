package me.blackvein.quests.prompts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;
import me.blackvein.quests.util.ColorUtil;
import me.blackvein.quests.util.Lang;

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

public class ItemStackPrompt extends FixedSetPrompt implements ColorUtil {

    //Stores itemstack in "tempStack" context data.
	//Stores name in "tempName"
    //Stores amount in "tempAmount"
    //Stores data in "tempData"
    //Stores enchantments in "tempEnchantments"
    //Stores display name in "tempDisplay"
    //Stores lore in "tempLore"
    final Prompt oldPrompt;

    public ItemStackPrompt(Prompt old) {

        super("0", "1", "2", "3", "4", "5", "6", "7", "8");
        oldPrompt = old;

    }

    @Override
    public String getPromptText(ConversationContext cc) {
        String menu = YELLOW + Lang.get("createItemTitle") + "\n";
        if (cc.getSessionData("tempName") != null) {
            String stackData = getItemData(cc);
            if (stackData != null) {
                menu += stackData;
            }
        } else {
            menu += "\n";
        }
        menu += GOLD + "" + BOLD + "0. " + RESET + "" + YELLOW + Lang.get("itemCreateLoadHand") + "\n";
        menu += YELLOW + "" + BOLD + "1. " + RESET + "" + GOLD + Lang.get("itemCreateSetName") + "\n";
        menu += YELLOW + "" + BOLD + "2. " + RESET + "" + GOLD + Lang.get("itemCreateSetAmount") + "\n";
        menu += YELLOW + "" + BOLD + "3. " + RESET + "" + GOLD + Lang.get("itemCreateSetDurab") + "\n";
        menu += YELLOW + "" + BOLD + "4. " + RESET + "" + GOLD + Lang.get("itemCreateSetEnchs") + "\n";
        menu += YELLOW + "" + BOLD + "5. " + RESET + "" + ITALIC + GOLD + Lang.get("itemCreateSetDisplay") + "\n";
        menu += YELLOW + "" + BOLD + "6. " + RESET + "" + ITALIC + GOLD + Lang.get("itemCreateSetLore") + "\n";
        menu += YELLOW + "" + BOLD + "7. " + RESET + "" + RED + Lang.get("cancel") + "\n";
        menu += YELLOW + "" + BOLD + "8. " + RESET + "" + GREEN + Lang.get("done") + "\n";
        return menu;
    }

    @SuppressWarnings("unchecked")
	@Override
    protected Prompt acceptValidatedInput(ConversationContext cc, String input) {

        if (input.equalsIgnoreCase("0")) {

            Player player = (Player) cc.getForWhom();
            ItemStack is = player.getItemInHand();
            if (is == null || is.getType().equals(Material.AIR)) {

                player.sendMessage(RED + Lang.get("itemCreateNoItem"));
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

                }

                player.sendMessage(GREEN + Lang.get("itemCreateLoaded"));
                return new ItemStackPrompt(oldPrompt);

            }

        } else if (input.equalsIgnoreCase("1")) {
            return new NamePrompt();
        } else if (input.equalsIgnoreCase("2")) {

            if (cc.getSessionData("tempName") != null) {
                return new AmountPrompt();
            } else {
                cc.getForWhom().sendRawMessage(RED + Lang.get("itemCreateNoName"));
                return new ItemStackPrompt(oldPrompt);
            }

        } else if (input.equalsIgnoreCase("3")) {

            if (cc.getSessionData("tempName") != null && cc.getSessionData("tempAmount") != null) {
                return new DataPrompt();
            } else {
                cc.getForWhom().sendRawMessage(RED + Lang.get("itemCreateNoIDAmount"));
                return new ItemStackPrompt(oldPrompt);
            }

        } else if (input.equalsIgnoreCase("4")) {

            if (cc.getSessionData("tempName") != null && cc.getSessionData("tempAmount") != null) {
                return new EnchantmentPrompt();
            } else {
                cc.getForWhom().sendRawMessage(RED + Lang.get("itemCreateNoIDAmount"));
                return new ItemStackPrompt(oldPrompt);
            }

        } else if (input.equalsIgnoreCase("5")) {

            if (cc.getSessionData("tempName") != null && cc.getSessionData("tempAmount") != null) {
                return new DisplayPrompt();
            } else {
                cc.getForWhom().sendRawMessage(RED + Lang.get("itemCreateNoNameAmount"));
                return new ItemStackPrompt(oldPrompt);
            }

        } else if (input.equalsIgnoreCase("6")) {

            if (cc.getSessionData("tempName") != null && cc.getSessionData("tempAmount") != null) {
                return new LorePrompt();
            } else {
                cc.getForWhom().sendRawMessage(RED + Lang.get("itemCreateNoNameAmount"));
                return new ItemStackPrompt(oldPrompt);
            }

        } else if (input.equalsIgnoreCase("7")) {

            cc.setSessionData("tempStack", null);
            cc.setSessionData("tempName", null);
            cc.setSessionData("tempAmount", null);
            cc.setSessionData("tempData", null);
            cc.setSessionData("tempEnchantments", null);
            cc.setSessionData("tempDisplay", null);
            cc.setSessionData("tempLore", null);

        } else if (input.equalsIgnoreCase("8")) {

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
                    for (String line : loadedLore)
                    {
                        lore.add(ChatColor.translateAlternateColorCodes('&', line));
                    }
                }

                ItemStack stack = new ItemStack(Material.matchMaterial(name), amount);
                ItemMeta meta = stack.getItemMeta();

                if (data != -1) {
                    stack.setDurability((short) data);
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
                cc.getForWhom().sendRawMessage(RED + Lang.get("itemCreateNoNameAmount"));
                return new ItemStackPrompt(oldPrompt);
            }

        }

        try {
            return oldPrompt;
        } catch (Exception e) {
            cc.getForWhom().sendRawMessage(RED + Lang.get("itemCreateCriticalError"));
            return Prompt.END_OF_CONVERSATION;
        }
    }

    private class NamePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext cc) {
            return YELLOW + Lang.get("itemCreateEnterName");
        }

        @Override
        public Prompt acceptInput(ConversationContext cc, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                String dataString = null;
                if (input.contains(":")) {
                    String[] splitInput = input.split(":");
                    input = splitInput[0];
                    if (splitInput.length > 1) {
                        dataString = splitInput[1];
                    }
                }

                Material mat = Material.matchMaterial(input.toUpperCase().replace(" ", "_"));
                if (mat == null) {
                    cc.getForWhom().sendRawMessage(RED + Lang.get("itemCreateInvalidName"));
                    return new NamePrompt();
                } else {
                    cc.setSessionData("tempName", mat.name());
                    cc.setSessionData("tempAmount", 1);

                    if (dataString != null) {
                        try {
                            short data = Short.parseShort(dataString);
                            cc.setSessionData("tempData", data);
                        } catch (NumberFormatException e) {
                            cc.getForWhom().sendRawMessage(RED + Lang.get("itemCreateInvalidData"));
                            return new NamePrompt();
                        }
                    }
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
            return YELLOW + Lang.get("itemCreateEnterAmount");
        }

        @Override
        public Prompt acceptInput(ConversationContext cc, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                try {

                    int amt = Integer.parseInt(input);
                    if (amt < 1 || amt > 64) {
                        cc.getForWhom().sendRawMessage(RED + Lang.get("itemCreateInvalidAmount"));
                        return new AmountPrompt();
                    } else {
                        cc.setSessionData("tempAmount", Integer.parseInt(input));
                        return new ItemStackPrompt(oldPrompt);
                    }

                } catch (NumberFormatException e) {
                    cc.getForWhom().sendRawMessage(RED + Lang.get("itemCreateInvalidInput"));
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
            return YELLOW + Lang.get("itemCreateEnterDurab");
        }

        @Override
        public Prompt acceptInput(ConversationContext cc, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {

                try {

                    int amt = Integer.parseInt(input);
                    if (amt < 1) {
                        cc.getForWhom().sendRawMessage(RED + Lang.get("itemCreateInvalidDurab"));
                        return new DataPrompt();
                    } else {
                        cc.setSessionData("tempData", Short.parseShort(input));
                        return new ItemStackPrompt(oldPrompt);
                    }

                } catch (NumberFormatException e) {
                    cc.getForWhom().sendRawMessage(RED + Lang.get("itemCreateInvalidInput"));
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

            String text = PINK + Lang.get("enchantmentsTitle") + "\n";
            for (Enchantment e : Enchantment.values()) {

                text += GREEN + Quester.prettyEnchantmentString(e) + ", ";

            }
            text = text.substring(0, text.length() - 1);

            return text + "\n" + YELLOW + Lang.get("itemCreateEnterEnch");

        }

        @Override
        public Prompt acceptInput(ConversationContext cc, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdClear")) == false && input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                Enchantment e = Quests.getEnchantmentPretty(input);
                if (e != null) {

                    cc.setSessionData("tempEnchant", e);
                    return new LevelPrompt(Quester.prettyEnchantmentString(e));

                } else {

                    cc.getForWhom().sendRawMessage(RED + Lang.get("itemCreateInvalidEnch"));
                    return new EnchantmentPrompt();

                }

            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
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
                text = text.replaceAll("<enchantment>", enchantment);
                return AQUA + text;
            }

            @Override
            public Prompt acceptInput(ConversationContext cc, String input) {

                try {

                    int num = Integer.parseInt(input);
                    if (num < 1) {
                        cc.getForWhom().sendRawMessage(RED + Lang.get("itemCreateInvalidLevel"));
                        return new LevelPrompt(enchantment);
                    } else {

                        if (cc.getSessionData("tempEnchantments") != null) {

                            @SuppressWarnings("unchecked")
							Map<Enchantment, Integer> enchs = (Map<Enchantment, Integer>) cc.getSessionData("tempEnchantments");
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
                    cc.getForWhom().sendRawMessage(RED + Lang.get("itemCreateNotNumber"));
                    return new LevelPrompt(enchantment);
                }

            }

        }

    }

    private class DisplayPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext cc) {
            return YELLOW + Lang.get("itemCreateEnterName");
        }

        @Override
        public Prompt acceptInput(ConversationContext cc, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {

                input = Quests.parseString(input);

                cc.setSessionData("tempDisplay", input);

            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {

                cc.setSessionData("tempDisplay", null);

            }

            return new ItemStackPrompt(oldPrompt);

        }

    }

    private class LorePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext cc) {
            return YELLOW + Lang.get("itemCreateEnterLore");
        }

        @Override
        public Prompt acceptInput(ConversationContext cc, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {

                input = Quests.parseString(input);

                LinkedList<String> lore = new LinkedList<String>();
                lore.addAll(Arrays.asList(input.split(";")));
                cc.setSessionData("tempLore", lore);

            } else if (input.equalsIgnoreCase("clear")) {

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
                item = AQUA + Quester.prettyItemString(name);

                if (cc.getSessionData("tempData") != null) {
                    item += ":" + BLUE + (Short) cc.getSessionData("tempData");
                }

            } else {

                item = PINK + "" + ITALIC + (String) cc.getSessionData("tempDisplay") + RESET + "" + GRAY + " (";
                String name = (String) cc.getSessionData("tempName");
                item += AQUA + Quester.prettyItemString(name);
                if (cc.getSessionData("tempData") != null) {
                    item += ":" + BLUE + (Short) cc.getSessionData("tempData");
                }
                item += GRAY + ")";

            }

            if (cc.getSessionData("tempAmount") != null) {
                item += GRAY + " x " + DARKAQUA + (Integer) cc.getSessionData("tempAmount");
            } else {
                item += GRAY + " x " + DARKAQUA + "1";
            }

            item += "\n";

            if (cc.getSessionData("tempEnchantments") != null) {

                @SuppressWarnings("unchecked")
				Map<Enchantment, Integer> enchantments = (Map<Enchantment, Integer>) cc.getSessionData("tempEnchantments");
                for (Entry<Enchantment, Integer> e : enchantments.entrySet()) {

                    item += GRAY + "  - " + RED + Quester.prettyEnchantmentString(e.getKey()) + " " + Quests.getNumeral(e.getValue()) + "\n";

                }

            }

            if (cc.getSessionData("tempLore") != null) {

                @SuppressWarnings("unchecked")
				List<String> lore = (List<String>) cc.getSessionData("tempLore");

                item += DARKGREEN + "(Lore)\n\"";
                for (String s : lore) {

                    if (lore.indexOf(s) != (lore.size() - 1)) {
                        item += DARKGREEN + "" + ITALIC + s + "\n";
                    } else {
                        item += DARKGREEN + "" + ITALIC + s + "\"\n";
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