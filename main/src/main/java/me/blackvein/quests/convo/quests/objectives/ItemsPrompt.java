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

package me.blackvein.quests.convo.quests.objectives;

import java.util.LinkedList;
import java.util.List;

import me.blackvein.quests.convo.generic.ItemStackPrompt;
import me.blackvein.quests.convo.quests.QuestsEditorNumericPrompt;
import me.blackvein.quests.convo.quests.stages.StageMainPrompt;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenNumericPromptEvent;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.FixedSetPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class ItemsPrompt extends QuestsEditorNumericPrompt {
    private final int stageNum;
    private final String pref;

    public ItemsPrompt(int stageNum, ConversationContext context) {
        super(context);
        this.stageNum = stageNum;
        this.pref = "stage" + stageNum;
    }
    
    private final int size = 5;
    
    public int getSize() {
        return size;
    }
    
    public String getTitle(ConversationContext context) {
        return Lang.get("stageEditorItems");
    }
    
    public ChatColor getNumberColor(ConversationContext context, int number) {
        switch (number) {
            case 1:
            case 2:
            case 3:
            case 4:
                return ChatColor.BLUE;
            case 5:
                return ChatColor.GREEN;
            default:
                return null;
        }
    }
    
    public String getSelectionText(ConversationContext context, int number) {
        switch(number) {
        case 1:
            return ChatColor.YELLOW + Lang.get("stageEditorCraftItems");
        case 2:
            return ChatColor.YELLOW + Lang.get("stageEditorSmeltItems");
        case 3:
            return ChatColor.YELLOW + Lang.get("stageEditorEnchantItems");
        case 4:
            return ChatColor.YELLOW + Lang.get("stageEditorBrewPotions");
        case 5:
            return ChatColor.GREEN + Lang.get("done");
        default:
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    public String getAdditionalText(ConversationContext context, int number) {
        switch(number) {
        case 1:
            if (context.getSessionData(pref + CK.S_CRAFT_ITEMS) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                String text = "\n";
                LinkedList<ItemStack> items = (LinkedList<ItemStack>) context.getSessionData(pref + CK.S_CRAFT_ITEMS);
                for (int i = 0; i < items.size(); i++) {
                    text += ChatColor.GRAY + "     - " + ChatColor.BLUE + ItemUtil.getName(items.get(i)) 
                            + ChatColor.GRAY + " x " + ChatColor.AQUA + items.get(i).getAmount() + "\n";
                }
                return text;
            }
        case 2:
            if (context.getSessionData(pref + CK.S_SMELT_ITEMS) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                String text = "\n";
                LinkedList<ItemStack> items = (LinkedList<ItemStack>) context.getSessionData(pref + CK.S_SMELT_ITEMS);
                for (int i = 0; i < items.size(); i++) {
                    text += ChatColor.GRAY + "     - " + ChatColor.BLUE + ItemUtil.getName(items.get(i)) 
                            + ChatColor.GRAY + " x " + ChatColor.AQUA + items.get(i).getAmount() + "\n";
                }
                return text;
            }
        case 3:
            if (context.getSessionData(pref + CK.S_ENCHANT_TYPES) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                String text = "\n";
                LinkedList<String> enchants = (LinkedList<String>) context.getSessionData(pref + CK.S_ENCHANT_TYPES);
                LinkedList<String> names = (LinkedList<String>) context.getSessionData(pref + CK.S_ENCHANT_NAMES);
                LinkedList<Integer> amnts = (LinkedList<Integer>) context.getSessionData(pref + CK.S_ENCHANT_AMOUNTS);
                for (int i = 0; i < enchants.size(); i++) {
                    text += ChatColor.GRAY + "     - " + ChatColor.BLUE + ItemUtil.getPrettyItemName(names.get(i)) 
                            + ChatColor.GRAY + " " + Lang.get("with") + " " + ChatColor.AQUA 
                            + ItemUtil.getPrettyEnchantmentName(ItemUtil.getEnchantmentFromProperName(enchants.get(i))) 
                            + ChatColor.GRAY + " x " + ChatColor.DARK_AQUA + amnts.get(i) + "\n";
                }
                return text;
            }
        case 4:
            if (context.getSessionData(pref + CK.S_BREW_ITEMS) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                String text = "\n";
                LinkedList<ItemStack> items = (LinkedList<ItemStack>) context.getSessionData(pref + CK.S_BREW_ITEMS);
                for (int i = 0; i < items.size(); i++) {
                    text += ChatColor.GRAY + "     - " + ChatColor.BLUE + ItemUtil.getName(items.get(i)) 
                            + ChatColor.GRAY + " x " + ChatColor.AQUA + items.get(i).getAmount() + "\n";
                }
                return text;
            }
        case 5:
            return "";
        default:
            return null;
        }
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
        
        QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
        context.getPlugin().getServer().getPluginManager().callEvent(event);
        
        String text = ChatColor.AQUA + "- " + getTitle(context) + " -\n";
        for (int i = 1; i <= size; i++) {
            text += getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                    + getSelectionText(context, i) + " " + getAdditionalText(context, i) + "\n";
        }
        return text;
    }

    @Override
    protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
        switch(input.intValue()) {
        case 1:
            return new CraftListPrompt();
        case 2:
            return new SmeltListPrompt();
        case 3:
            return new EnchantmentListPrompt();
        case 4:
            return new BrewListPrompt();
        case 5:
            try {
                return new StageMainPrompt(stageNum, context);
            } catch (Exception e) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateCriticalError"));
                return Prompt.END_OF_CONVERSATION;
            }
        default:
            return new ItemsPrompt(stageNum, context);
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
                return new ItemsPrompt(stageNum, context);
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
                return new ItemsPrompt(stageNum, context);
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
                    return new ItemsPrompt(stageNum, context);
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

        @SuppressWarnings("deprecation")
        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.LIGHT_PURPLE + "- " + ChatColor.DARK_PURPLE + Lang.get("stageEditorEnchantments") 
                    + ChatColor.LIGHT_PURPLE + " -\n";
            for (int i = 0; i < Enchantment.values().length; i++) {
                if (i == Enchantment.values().length - 1) {
                    text += ChatColor.GREEN + MiscUtil.snakeCaseToUpperCamelCase(Enchantment.values()[i].getName()) + " ";
                } else {
                    text += ChatColor.GREEN + MiscUtil.snakeCaseToUpperCamelCase(Enchantment.values()[i].getName()) + ", ";
                }
            }
            text = text.substring(0, text.length() - 1);
            return text + "\n" + ChatColor.YELLOW + Lang.get("stageEditorEnchantTypePrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                LinkedList<String> enchTypes = new LinkedList<String>();
                for (String s : input.split(" ")) {
                    if (ItemUtil.getEnchantmentFromProperName(s) != null) {
                        if (enchTypes.contains(s) == false) {
                            enchTypes.add(s);
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.RED + " " + Lang.get("listDuplicate"));
                            return new EnchantTypesPrompt();
                        }
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + ChatColor.RED + " " 
                                + Lang.get("stageEditorInvalidEnchantment"));
                        return new EnchantTypesPrompt();
                    }
                }
                context.setSessionData(pref + CK.S_ENCHANT_TYPES, enchTypes);
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
                return new ItemsPrompt(stageNum, context);
            }
            return null;
        }
        
        @SuppressWarnings("unchecked")
        private List<ItemStack> getItems(ConversationContext context) {
            return (List<ItemStack>) context.getSessionData(pref + CK.S_BREW_ITEMS);
        }
    }
}
