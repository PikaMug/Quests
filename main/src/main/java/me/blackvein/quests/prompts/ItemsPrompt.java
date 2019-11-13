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

package me.blackvein.quests.prompts;

import java.util.LinkedList;
import java.util.List;

import me.blackvein.quests.QuestFactory;
import me.blackvein.quests.Quests;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.FixedSetPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class ItemsPrompt extends FixedSetPrompt {
    private final Quests plugin;
    private final int stageNum;
    private final String pref;
    private final QuestFactory questFactory;

    public ItemsPrompt(Quests plugin, int stageNum, QuestFactory qf) {
        super("1", "2", "3", "4", "5");
        this.plugin = plugin;
        this.stageNum = stageNum;
        this.pref = "stage" + stageNum;
        this.questFactory = qf;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public String getPromptText(ConversationContext context) {
        // Check/add newly made item
        if (context.getSessionData("newItem") != null) {
            if (context.getSessionData(pref + CK.S_CRAFT_ITEMS) != null) {
                List<ItemStack> items = (List<ItemStack>) context.getSessionData(pref + CK.S_CRAFT_ITEMS);
                items.add((ItemStack) context.getSessionData("tempStack"));
                context.setSessionData(pref + CK.S_CRAFT_ITEMS, items);
            } else if (context.getSessionData(pref + CK.S_SMELT_ITEMS) != null) {
                List<ItemStack> items = (List<ItemStack>) context.getSessionData(pref + CK.S_SMELT_ITEMS);
                items.add((ItemStack) context.getSessionData("tempStack"));
                context.setSessionData(pref + CK.S_SMELT_ITEMS, items);
            }
            context.setSessionData("newItem", null);
            context.setSessionData("tempStack", null);
        }
        context.setSessionData(pref, Boolean.TRUE);
        String text = ChatColor.AQUA + "- " + Lang.get("stageEditorItems") + " -\n";
        if (context.getSessionData(pref + CK.S_CRAFT_ITEMS) == null) {
            text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "1 " + ChatColor.RESET + ChatColor.LIGHT_PURPLE 
                    + "- " + Lang.get("stageEditorCraftItems") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "1 " + ChatColor.RESET + ChatColor.LIGHT_PURPLE 
                    + "- " + Lang.get("stageEditorCraftItems") + "\n";
            LinkedList<ItemStack> items = (LinkedList<ItemStack>) context.getSessionData(pref + CK.S_CRAFT_ITEMS);
            for (int i = 0; i < items.size(); i++) {
                text += ChatColor.GRAY + "     - " + ChatColor.BLUE + ItemUtil.getName(items.get(i)) + ChatColor.GRAY 
                        + " x " + ChatColor.AQUA + items.get(i).getAmount() + "\n";
            }
        }
        if (context.getSessionData(pref + CK.S_SMELT_ITEMS) == null) {
            text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "2 " + ChatColor.RESET + ChatColor.LIGHT_PURPLE 
                    + "- " + Lang.get("stageEditorSmeltItems") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "2 " + ChatColor.RESET + ChatColor.LIGHT_PURPLE 
                    + "- " + Lang.get("stageEditorSmeltItems") + "\n";
            LinkedList<ItemStack> items = (LinkedList<ItemStack>) context.getSessionData(pref + CK.S_SMELT_ITEMS);
            for (int i = 0; i < items.size(); i++) {
                text += ChatColor.GRAY + "     - " + ChatColor.BLUE + ItemUtil.getName(items.get(i)) + ChatColor.GRAY 
                        + " x " + ChatColor.AQUA + items.get(i).getAmount() + "\n";
            }
        }
        if (context.getSessionData(pref + CK.S_ENCHANT_TYPES) == null) {
            text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "3 " + ChatColor.RESET + ChatColor.LIGHT_PURPLE 
                    + "- " + Lang.get("stageEditorEnchantItems") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "3 " + ChatColor.RESET + ChatColor.LIGHT_PURPLE 
                    + "- " + Lang.get("stageEditorEnchantItems") + "\n";
            LinkedList<String> enchants = (LinkedList<String>) context.getSessionData(pref + CK.S_ENCHANT_TYPES);
            LinkedList<String> names = (LinkedList<String>) context.getSessionData(pref + CK.S_ENCHANT_NAMES);
            LinkedList<Integer> amnts = (LinkedList<Integer>) context.getSessionData(pref + CK.S_ENCHANT_AMOUNTS);
            for (int i = 0; i < enchants.size(); i++) {
                text += ChatColor.GRAY + "     - " + ChatColor.BLUE + ItemUtil.getPrettyItemName(names.get(i)) 
                        + ChatColor.GRAY + " " + Lang.get("with") + " " + ChatColor.AQUA 
                        + ItemUtil.getPrettyEnchantmentName(ItemUtil.getEnchantmentFromProperName(enchants.get(i))) 
                        + ChatColor.GRAY + " x " + ChatColor.DARK_AQUA + amnts.get(i) + "\n";
            }
        }
        if (context.getSessionData(pref + CK.S_BREW_ITEMS) == null) {
            text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "4 " + ChatColor.RESET + ChatColor.LIGHT_PURPLE 
                    + "- " + Lang.get("stageEditorBrewPotions") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "4 " + ChatColor.RESET + ChatColor.LIGHT_PURPLE 
                    + "- " + Lang.get("stageEditorBrewPotions") + "\n";
            LinkedList<ItemStack> items = (LinkedList<ItemStack>) context.getSessionData(pref + CK.S_BREW_ITEMS);
            for (int i = 0; i < items.size(); i++) {
                text += ChatColor.GRAY + "     - " + ChatColor.BLUE + ItemUtil.getName(items.get(i)) + ChatColor.GRAY 
                        + " x " + ChatColor.AQUA + items.get(i).getAmount() + "\n";
            }
        }
        text += ChatColor.GREEN + "" + ChatColor.BOLD + "5 " + ChatColor.RESET + ChatColor.DARK_PURPLE + "- " 
                + Lang.get("done") + "\n";
        return text;
    }

    @Override
    protected Prompt acceptValidatedInput(ConversationContext context, String input) {
        if (input.equalsIgnoreCase("1")) {
            return new CraftListPrompt();
        } else if (input.equalsIgnoreCase("2")) {
            return new SmeltListPrompt();
        } else if (input.equalsIgnoreCase("3")) {
            return new EnchantmentListPrompt();
        } else if (input.equalsIgnoreCase("4")) {
            return new BrewListPrompt();
        }
        try {
            return new CreateStagePrompt(plugin, stageNum, questFactory);
        } catch (Exception e) {
            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateCriticalError"));
            return Prompt.END_OF_CONVERSATION;
        }
    }
    
    private class CraftListPrompt extends FixedSetPrompt {
        
        public CraftListPrompt() {
            super("1", "2", "3");
        }

        @Override
        public String getPromptText(ConversationContext context) {
            // Check/add newly made item
            if (context.getSessionData("newItem") != null) {
                if (context.getSessionData(pref + CK.S_CRAFT_ITEMS) != null) {
                    List<ItemStack> items = getItems(context);
                    items.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(pref + CK.S_CRAFT_ITEMS, items);
                } else {
                    LinkedList<ItemStack> items = new LinkedList<ItemStack>();
                    items.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(pref + CK.S_CRAFT_ITEMS, items);
                }
                context.setSessionData("newItem", null);
                context.setSessionData("tempStack", null);
            }
            String text = ChatColor.GOLD + "- " + Lang.get("stageEditorCraftItems") + " -\n";
            if (context.getSessionData(pref + CK.S_CRAFT_ITEMS) == null) {
                text += ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorDeliveryAddItem") + "\n";
            } else {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorDeliveryAddItem") + "\n";
                for (ItemStack is : getItems(context)) {
                    text += ChatColor.GRAY + "     - " + ItemUtil.getDisplayString(is) + "\n";
                }
            }
            text += ChatColor.RED + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("clear") + "\n";
            text += ChatColor.GREEN + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("done");
            return text;
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase("1")) {
                return new ItemStackPrompt(CraftListPrompt.this);
            } else if (input.equalsIgnoreCase("2")) {
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorObjectiveCleared"));
                context.setSessionData(pref + CK.S_CRAFT_ITEMS, null);
                return new CraftListPrompt();
            } else if (input.equalsIgnoreCase("3")) {
                return new ItemsPrompt(plugin, stageNum, questFactory);
            }
            return null;
        }
        
        @SuppressWarnings("unchecked")
        private List<ItemStack> getItems(ConversationContext context) {
            return (List<ItemStack>) context.getSessionData(pref + CK.S_CRAFT_ITEMS);
        }
    }
    
    private class SmeltListPrompt extends FixedSetPrompt {
        
        public SmeltListPrompt() {
            super("1", "2", "3");
        }

        @Override
        public String getPromptText(ConversationContext context) {
            // Check/add newly made item
            if (context.getSessionData("newItem") != null) {
                if (context.getSessionData(pref + CK.S_SMELT_ITEMS) != null) {
                    List<ItemStack> items = getItems(context);
                    items.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(pref + CK.S_SMELT_ITEMS, items);
                } else {
                    LinkedList<ItemStack> items = new LinkedList<ItemStack>();
                    items.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(pref + CK.S_SMELT_ITEMS, items);
                }
                context.setSessionData("newItem", null);
                context.setSessionData("tempStack", null);
            }
            String text = ChatColor.GOLD + "- " + Lang.get("stageEditorSmeltItems") + " -\n";
            if (context.getSessionData(pref + CK.S_SMELT_ITEMS) == null) {
                text += ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorDeliveryAddItem") + "\n";
            } else {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorDeliveryAddItem") + "\n";
                for (ItemStack is : getItems(context)) {
                    text += ChatColor.GRAY + "     - " + ItemUtil.getDisplayString(is) + "\n";
                }
            }
            text += ChatColor.RED + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("clear") + "\n";
            text += ChatColor.GREEN + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("done");
            return text;
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase("1")) {
                return new ItemStackPrompt(SmeltListPrompt.this);
            } else if (input.equalsIgnoreCase("2")) {
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorObjectiveCleared"));
                context.setSessionData(pref + CK.S_SMELT_ITEMS, null);
                return new SmeltListPrompt();
            } else if (input.equalsIgnoreCase("3")) {
                return new ItemsPrompt(plugin, stageNum, questFactory);
            }
            return null;
        }
        
        @SuppressWarnings("unchecked")
        private List<ItemStack> getItems(ConversationContext context) {
            return (List<ItemStack>) context.getSessionData(pref + CK.S_SMELT_ITEMS);
        }
    }

    private class EnchantmentListPrompt extends FixedSetPrompt {

        public EnchantmentListPrompt() {
            super("1", "2", "3", "4", "5");
        }

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.GOLD + "- " + Lang.get("stageEditorEnchantItems") + " -\n";
            if (context.getSessionData(pref + CK.S_ENCHANT_TYPES) == null) {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorSetEnchantments") + " (" + Lang.get("noneSet") + ")\n";
                text += ChatColor.GRAY + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.GRAY + " - " 
                        + Lang.get("stageEditorSetItemNames") + " (" + Lang.get("noneSet") + ")\n";
                text += ChatColor.GRAY + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.GRAY + " - " 
                        + Lang.get("stageEditorSetEnchantAmounts") + " (" + Lang.get("noneSet") + ")\n";
                text += ChatColor.RED + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("clear") + "\n";
                text += ChatColor.GREEN + "" + ChatColor.BOLD + "5" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("done");
            } else {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorSetEnchantments") + "\n";
                for (String s : getEnchantTypes(context)) {
                    text += ChatColor.GRAY + "     - " + ChatColor.AQUA + s + "\n";
                }
                if (context.getSessionData(pref + CK.S_ENCHANT_NAMES) == null) {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                            + Lang.get("stageEditorSetItemNames") + " (" + Lang.get("noneSet") + ")\n";
                } else {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                            + Lang.get("stageEditorSetItemNames") + "\n";
                    for (String s : getEnchantItems(context)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + ItemUtil.getPrettyItemName(s) + "\n";
                    }
                }
                if (context.getSessionData(pref + CK.S_ENCHANT_AMOUNTS) == null) {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                            + Lang.get("stageEditorSetEnchantAmounts") + " (" + Lang.get("noneSet") + ")\n";
                } else {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                            + Lang.get("stageEditorSetEnchantAmounts") + "\n";
                    for (int i : getEnchantAmounts(context)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + i + "\n";
                    }
                }
                text += ChatColor.RED + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("clear") + "\n";
                text += ChatColor.GREEN + "" + ChatColor.BOLD + "5" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("done");
            }
            return text;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase("1")) {
                return new EnchantTypesPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(pref + CK.S_ENCHANT_TYPES) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNoEnchantments"));
                    return new EnchantmentListPrompt();
                } else {
                    return new EnchantItemsPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                if (context.getSessionData(pref + CK.S_ENCHANT_TYPES) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNoEnchantments"));
                    return new EnchantmentListPrompt();
                } else {
                    return new EnchantAmountsPrompt();
                }
            } else if (input.equalsIgnoreCase("4")) {
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorObjectiveCleared"));
                context.setSessionData(pref + CK.S_ENCHANT_TYPES, null);
                context.setSessionData(pref + CK.S_ENCHANT_NAMES, null);
                context.setSessionData(pref + CK.S_ENCHANT_AMOUNTS, null);
                return new EnchantmentListPrompt();
            } else if (input.equalsIgnoreCase("5")) {
                int one;
                int two;
                int three;
                if (context.getSessionData(pref + CK.S_ENCHANT_TYPES) != null) {
                    one = ((List<String>) context.getSessionData(pref + CK.S_ENCHANT_TYPES)).size();
                } else {
                    one = 0;
                }
                if (context.getSessionData(pref + CK.S_ENCHANT_NAMES) != null) {
                    two = ((List<String>) context.getSessionData(pref + CK.S_ENCHANT_NAMES)).size();
                } else {
                    two = 0;
                }
                if (context.getSessionData(pref + CK.S_ENCHANT_AMOUNTS) != null) {
                    three = ((List<Integer>) context.getSessionData(pref + CK.S_ENCHANT_AMOUNTS)).size();
                } else {
                    three = 0;
                }
                if (one == two && two == three) {
                    return new ItemsPrompt(plugin, stageNum, questFactory);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                    return new EnchantmentListPrompt();
                }
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        private List<String> getEnchantTypes(ConversationContext context) {
            return (List<String>) context.getSessionData(pref + CK.S_ENCHANT_TYPES);
        }

        @SuppressWarnings("unchecked")
        private List<String> getEnchantItems(ConversationContext context) {
            return (List<String>) context.getSessionData(pref + CK.S_ENCHANT_NAMES);
        }

        @SuppressWarnings("unchecked")
        private List<Integer> getEnchantAmounts(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + CK.S_ENCHANT_AMOUNTS);
        }
    }

    private class EnchantTypesPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.LIGHT_PURPLE + "- " + ChatColor.DARK_PURPLE + Lang.get("stageEditorEnchantments") 
                    + ChatColor.LIGHT_PURPLE + " -\n";
            for (int i = 0; i < Enchantment.values().length; i++) {
                if (i == Enchantment.values().length - 1) {
                    text += ChatColor.GREEN + ItemUtil.getPrettyEnchantmentName(Enchantment.values()[i]) + " ";
                } else {
                    text += ChatColor.GREEN + ItemUtil.getPrettyEnchantmentName(Enchantment.values()[i]) + ", ";
                }
            }
            text = text.substring(0, text.length() - 1);
            return text + "\n" + ChatColor.YELLOW + Lang.get("stageEditorEnchantTypePrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                String[] args = input.split(Lang.get("charSemi"));
                LinkedList<String> enchs = new LinkedList<String>();
                boolean valid;
                for (String s : args) {
                    s = s.trim();
                    valid = false;
                    for (Enchantment e : Enchantment.values()) {
                        if (ItemUtil.getPrettyEnchantmentName(e).equalsIgnoreCase(s)) {
                            if (enchs.contains(s) == false) {
                                enchs.add(ItemUtil.getPrettyEnchantmentName(e));
                                valid = true;
                                break;
                            } else {
                                context.getForWhom().sendRawMessage(ChatColor.RED + " " + Lang.get("listDuplicate"));
                                return new EnchantTypesPrompt();
                            }
                        }
                    }
                    if (valid == false) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + ChatColor.RED + " " 
                                + Lang.get("stageEditorInvalidEnchantment"));
                        return new EnchantTypesPrompt();
                    }
                }
                context.setSessionData(pref + CK.S_ENCHANT_TYPES, enchs);
            }
            return new EnchantmentListPrompt();
        }
    }

    private class EnchantItemsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("stageEditorItemNamesPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                String[] args = input.split(" ");
                LinkedList<String> names = new LinkedList<String>();
                for (String s : args) {
                    try {
                        if (Material.matchMaterial(s) != null) {
                            //if (names.contains(s) == false) {
                                names.add(s);
                            /*} else {
                                context.getForWhom().sendRawMessage(ChatColor.RED + " " + Lang.get("listDuplicate"));
                                return new EnchantItemsPrompt();
                            }*/
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + ChatColor.RED + " " 
                                    + Lang.get("stageEditorInvalidItemName"));
                            return new EnchantItemsPrompt();
                        }
                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new EnchantItemsPrompt();
                    }
                }
                context.setSessionData(pref + CK.S_ENCHANT_NAMES, names);
            }
            return new EnchantmentListPrompt();
        }
    }

    private class EnchantAmountsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("stageEditorEnchantAmountsPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                String[] args = input.split(" ");
                LinkedList<Integer> amounts = new LinkedList<Integer>();
                for (String s : args) {
                    try {
                        if (Integer.parseInt(s) > 0) {
                            amounts.add(Integer.parseInt(s));
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidMinimum")
                                    .replace("<number>", "1"));
                            return new EnchantAmountsPrompt();
                        }
                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new EnchantAmountsPrompt();
                    }
                }
                context.setSessionData(pref + CK.S_ENCHANT_AMOUNTS, amounts);
            }
            return new EnchantmentListPrompt();
        }
    }
    
    private class BrewListPrompt extends FixedSetPrompt {
        
        public BrewListPrompt() {
            super("1", "2", "3");
        }

        @Override
        public String getPromptText(ConversationContext context) {
            // Check/add newly made item
            if (context.getSessionData("newItem") != null) {
                if (context.getSessionData(pref + CK.S_BREW_ITEMS) != null) {
                    List<ItemStack> items = getItems(context);
                    items.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(pref + CK.S_BREW_ITEMS, items);
                } else {
                    LinkedList<ItemStack> items = new LinkedList<ItemStack>();
                    items.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(pref + CK.S_BREW_ITEMS, items);
                }
                context.setSessionData("newItem", null);
                context.setSessionData("tempStack", null);
            }
            String text = ChatColor.GOLD + "- " + Lang.get("stageEditorBrewPotions") + " -\n";
            if (context.getSessionData(pref + CK.S_BREW_ITEMS) == null) {
                text += ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorDeliveryAddItem") + "\n";
            } else {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorDeliveryAddItem") + "\n";
                for (ItemStack is : getItems(context)) {
                    text += ChatColor.GRAY + "     - " + ItemUtil.getDisplayString(is) + "\n";
                }
            }
            text += ChatColor.RED + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("clear") + "\n";
            text += ChatColor.GREEN + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("done");
            return text;
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase("1")) {
                return new ItemStackPrompt(BrewListPrompt.this);
            } else if (input.equalsIgnoreCase("2")) {
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorObjectiveCleared"));
                context.setSessionData(pref + CK.S_BREW_ITEMS, null);
                return new BrewListPrompt();
            } else if (input.equalsIgnoreCase("3")) {
                return new ItemsPrompt(plugin, stageNum, questFactory);
            }
            return null;
        }
        
        @SuppressWarnings("unchecked")
        private List<ItemStack> getItems(ConversationContext context) {
            return (List<ItemStack>) context.getSessionData(pref + CK.S_BREW_ITEMS);
        }
    }
}
