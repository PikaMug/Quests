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
import me.blackvein.quests.convo.quests.QuestsEditorStringPrompt;
import me.blackvein.quests.convo.quests.stages.StageMainPrompt;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenNumericPromptEvent;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenStringPromptEvent;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
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
    
    private final int size = 6;
    
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
            case 5:
                return ChatColor.BLUE;
            case 6:
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
            return ChatColor.YELLOW + Lang.get("stageEditorConsumeItems");
        case 6:
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
            if (context.getSessionData(pref + CK.S_CONSUME_ITEMS) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                String text = "\n";
                LinkedList<ItemStack> items = (LinkedList<ItemStack>) context.getSessionData(pref + CK.S_CONSUME_ITEMS);
                for (int i = 0; i < items.size(); i++) {
                    text += ChatColor.GRAY + "     - " + ChatColor.BLUE + ItemUtil.getName(items.get(i)) 
                            + ChatColor.GRAY + " x " + ChatColor.AQUA + items.get(i).getAmount() + "\n";
                }
                return text;
            }
        case 6:
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
            return new ItemsCraftListPrompt(context);
        case 2:
            return new ItemsSmeltListPrompt(context);
        case 3:
            return new ItemsEnchantmentListPrompt(context);
        case 4:
            return new ItemsBrewListPrompt(context);
        case 5:
            return new ItemsConsumeListPrompt(context);
        case 6:
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
    
    public class ItemsCraftListPrompt extends QuestsEditorNumericPrompt {
        
        public ItemsCraftListPrompt(ConversationContext context) {
            super(context);
        }

        private final int size = 3;
        
        public int getSize() {
            return size;
        }
        
        public String getTitle(ConversationContext context) {
            return Lang.get("stageEditorCraftItems");
        }
        
        public ChatColor getNumberColor(ConversationContext context, int number) {
            switch (number) {
                case 1:
                    return ChatColor.BLUE;
                case 2:
                    return ChatColor.RED;
                case 3:
                    return ChatColor.GREEN;
                default:
                    return null;
            }
        }
        
        public String getSelectionText(ConversationContext context, int number) {
            switch(number) {
            case 1:
                return ChatColor.YELLOW + Lang.get("stageEditorDeliveryAddItem");
            case 2:
                return ChatColor.RED + Lang.get("clear");
            case 3:
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
                    for (ItemStack is : (List<ItemStack>) context.getSessionData(pref + CK.S_CRAFT_ITEMS)) {
                        text += ChatColor.GRAY + "     - " + ItemUtil.getDisplayString(is) + "\n";
                    }
                    return text;
                }
            case 2:
            case 3:
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
                } else {
                    LinkedList<ItemStack> items = new LinkedList<ItemStack>();
                    items.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(pref + CK.S_CRAFT_ITEMS, items);
                }
                context.setSessionData("newItem", null);
                context.setSessionData("tempStack", null);
            }
            
            QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);

            String text = ChatColor.GOLD + "- " + getTitle(context) + " -\n";
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
                return new ItemStackPrompt(ItemsCraftListPrompt.this);
            case 2:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorObjectiveCleared"));
                context.setSessionData(pref + CK.S_CRAFT_ITEMS, null);
                return new ItemsCraftListPrompt(context);
            case 3:
                return new ItemsPrompt(stageNum, context);
            default:
                return new ItemsPrompt(stageNum, context);
            }
        }
    }
    
    public class ItemsSmeltListPrompt extends QuestsEditorNumericPrompt {
        
        public ItemsSmeltListPrompt(ConversationContext context) {
            super(context);
        }
        
        private final int size = 3;
        
        public int getSize() {
            return size;
        }
        
        public String getTitle(ConversationContext context) {
            return Lang.get("stageEditorSmeltItems");
        }
        
        public ChatColor getNumberColor(ConversationContext context, int number) {
            switch (number) {
                case 1:
                    return ChatColor.BLUE;
                case 2:
                    return ChatColor.RED;
                case 3:
                    return ChatColor.GREEN;
                default:
                    return null;
            }
        }
        
        public String getSelectionText(ConversationContext context, int number) {
            switch(number) {
            case 1:
                return ChatColor.YELLOW + Lang.get("stageEditorDeliveryAddItem");
            case 2:
                return ChatColor.RED + Lang.get("clear");
            case 3:
                return ChatColor.GREEN + Lang.get("done");
            default:
                return null;
            }
        }
        
        @SuppressWarnings("unchecked")
        public String getAdditionalText(ConversationContext context, int number) {
            switch(number) {
            case 1:
                if (context.getSessionData(pref + CK.S_SMELT_ITEMS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    String text = "\n";
                    for (ItemStack is : (List<ItemStack>) context.getSessionData(pref + CK.S_SMELT_ITEMS)) {
                        text += ChatColor.GRAY + "     - " + ItemUtil.getDisplayString(is) + "\n";
                    }
                    return text;
                }
            case 2:
            case 3:
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
                if (context.getSessionData(pref + CK.S_SMELT_ITEMS) != null) {
                    List<ItemStack> items = (List<ItemStack>) context.getSessionData(pref + CK.S_SMELT_ITEMS);
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
            
            QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);

            String text = ChatColor.GOLD + "- " + getTitle(context) + " -\n";
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
                return new ItemStackPrompt(ItemsSmeltListPrompt.this);
            case 2:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorObjectiveCleared"));
                context.setSessionData(pref + CK.S_SMELT_ITEMS, null);
                return new ItemsSmeltListPrompt(context);
            case 3:
                return new ItemsPrompt(stageNum, context);
            default:
                return new ItemsPrompt(stageNum, context);
            }
        }
    }

    public class ItemsEnchantmentListPrompt extends QuestsEditorNumericPrompt {

        public ItemsEnchantmentListPrompt(ConversationContext context) {
            super(context);
        }
        
        private final int size = 5;
        
        public int getSize() {
            return size;
        }
        
        public String getTitle(ConversationContext context) {
            return Lang.get("stageEditorEnchantItems");
        }
        
        public ChatColor getNumberColor(ConversationContext context, int number) {
            switch (number) {
                case 1:
                case 2:
                case 3:
                    return ChatColor.BLUE;
                case 4:
                    return ChatColor.RED;
                case 5:
                    return ChatColor.GREEN;
                default:
                    return null;
            }
        }
        
        public String getSelectionText(ConversationContext context, int number) {
            switch(number) {
            case 1:
            case 2:
            case 3:
                return ChatColor.YELLOW + Lang.get("stageEditorDeliveryAddItem");
            case 4:
                return ChatColor.RED + Lang.get("clear");
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
                if (context.getSessionData(pref + CK.S_ENCHANT_TYPES) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    String text = "\n";
                    for (String s : (List<String>) context.getSessionData(pref + CK.S_ENCHANT_TYPES)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + s + "\n";
                    }
                    return text;
                }
            case 2:
                if (context.getSessionData(pref + CK.S_ENCHANT_NAMES) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    String text = "\n";
                    for (String s : (List<String>) context.getSessionData(pref + CK.S_ENCHANT_NAMES)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + ItemUtil.getPrettyItemName(s) + "\n";
                    }
                    return text;
                }
            case 3:
                if (context.getSessionData(pref + CK.S_ENCHANT_AMOUNTS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    String text = "\n";
                    for (int i : (List<Integer>) context.getSessionData(pref + CK.S_ENCHANT_AMOUNTS)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + i + "\n";
                    }
                    return text;
                }
            case 4:
            case 5:
                return "";
            default:
                return null;
            }
        }

        @Override
        public String getPromptText(ConversationContext context) {
            QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);

            String text = ChatColor.GOLD + "- " + getTitle(context) + " -\n";
            for (int i = 1; i <= size; i++) {
                text += getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                        + getSelectionText(context, i) + " " + getAdditionalText(context, i) + "\n";
            }
            return text;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
            switch(input.intValue()) {
            case 1:
                return new ItemEnchantTypesPrompt(context);
            case 2:
                return new ItemEnchantItemsPrompt(context);
            case 3:
                return new ItemEnchantAmountsPrompt(context);
            case 4:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorObjectiveCleared"));
                context.setSessionData(pref + CK.S_ENCHANT_TYPES, null);
                context.setSessionData(pref + CK.S_ENCHANT_NAMES, null);
                context.setSessionData(pref + CK.S_ENCHANT_AMOUNTS, null);
                return new ItemsEnchantmentListPrompt(context);
            case 5:
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
                    return new ItemsEnchantmentListPrompt(context);
                }
            default:
                return new ItemsPrompt(stageNum, context);
            }
        }
    }

    public class ItemEnchantTypesPrompt extends QuestsEditorStringPrompt {

        public ItemEnchantTypesPrompt(ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(ConversationContext context) {
            return Lang.get("stageEditorEnchantments");
        }

        @Override
        public String getQueryText(ConversationContext context) {
            return Lang.get("stageEditorEnchantTypePrompt");
        }

        @SuppressWarnings("deprecation")
        @Override
        public String getPromptText(ConversationContext context) {
            QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            String text = ChatColor.LIGHT_PURPLE + "- " + ChatColor.DARK_PURPLE + getTitle(context)
                    + ChatColor.LIGHT_PURPLE + " -\n";
            for (int i = 0; i < Enchantment.values().length; i++) {
                if (i == Enchantment.values().length - 1) {
                    text += ChatColor.GREEN + MiscUtil.snakeCaseToUpperCamelCase(Enchantment.values()[i].getName()) + " ";
                } else {
                    text += ChatColor.GREEN + MiscUtil.snakeCaseToUpperCamelCase(Enchantment.values()[i].getName()) + ", ";
                }
            }
            text = text.substring(0, text.length() - 1);
            return text + "\n" + ChatColor.YELLOW + getQueryText(context);
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
                            return new ItemEnchantTypesPrompt(context);
                        }
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + ChatColor.RED + " " 
                                + Lang.get("stageEditorInvalidEnchantment"));
                        return new ItemEnchantTypesPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_ENCHANT_TYPES, enchTypes);
            }
            return new ItemsEnchantmentListPrompt(context);
        }
    }

    public class ItemEnchantItemsPrompt extends QuestsEditorStringPrompt {

        public ItemEnchantItemsPrompt(ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(ConversationContext context) {
            return Lang.get("stageEditorItemNamesPrompt");
        }

        @Override
        public String getPromptText(ConversationContext context) {
            QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                String[] args = input.split(" ");
                LinkedList<String> names = new LinkedList<String>();
                for (String s : args) {
                    try {
                        if (Material.matchMaterial(s) != null) {
                            names.add(s);
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + ChatColor.RED + " " 
                                    + Lang.get("stageEditorInvalidItemName"));
                            return new ItemEnchantItemsPrompt(context);
                        }
                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new ItemEnchantItemsPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_ENCHANT_NAMES, names);
            }
            return new ItemsEnchantmentListPrompt(context);
        }
    }

    public class ItemEnchantAmountsPrompt extends QuestsEditorStringPrompt {

        public ItemEnchantAmountsPrompt(ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(ConversationContext context) {
            return Lang.get("stageEditorEnchantAmountsPrompt");
        }

        @Override
        public String getPromptText(ConversationContext context) {
            QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
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
                            return new ItemEnchantAmountsPrompt(context);
                        }
                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new ItemEnchantAmountsPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_ENCHANT_AMOUNTS, amounts);
            }
            return new ItemsEnchantmentListPrompt(context);
        }
    }
    
    public class ItemsBrewListPrompt extends QuestsEditorNumericPrompt {
        
        public ItemsBrewListPrompt(ConversationContext context) {
            super(context);
        }
        
        private final int size = 3;
        
        public int getSize() {
            return size;
        }
        
        public String getTitle(ConversationContext context) {
            return Lang.get("stageEditorBrewPotions");
        }
        
        public ChatColor getNumberColor(ConversationContext context, int number) {
            switch (number) {
                case 1:
                    return ChatColor.BLUE;
                case 2:
                    return ChatColor.RED;
                case 3:
                    return ChatColor.GREEN;
                default:
                    return null;
            }
        }
        
        public String getSelectionText(ConversationContext context, int number) {
            switch(number) {
            case 1:
                return ChatColor.YELLOW + Lang.get("stageEditorDeliveryAddItem");
            case 2:
                return ChatColor.RED + Lang.get("clear");
            case 3:
                return ChatColor.GREEN + Lang.get("done");
            default:
                return null;
            }
        }
        
        @SuppressWarnings("unchecked")
        public String getAdditionalText(ConversationContext context, int number) {
            switch(number) {
            case 1:
                if (context.getSessionData(pref + CK.S_BREW_ITEMS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    String text = "\n";
                    for (ItemStack is : (List<ItemStack>) context.getSessionData(pref + CK.S_BREW_ITEMS)) {
                        text += ChatColor.GRAY + "     - " + ItemUtil.getDisplayString(is) + "\n";
                    }
                    return text;
                }
            case 2:
            case 3:
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
                if (context.getSessionData(pref + CK.S_BREW_ITEMS) != null) {
                    List<ItemStack> items = (List<ItemStack>) context.getSessionData(pref + CK.S_BREW_ITEMS);
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
            
            QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);

            String text = ChatColor.GOLD + "- " + getTitle(context) + " -\n";
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
                return new ItemStackPrompt(ItemsBrewListPrompt.this);
            case 2:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorObjectiveCleared"));
                context.setSessionData(pref + CK.S_BREW_ITEMS, null);
                return new ItemsBrewListPrompt(context);
            case 3:
                return new ItemsPrompt(stageNum, context);
            default:
                return new ItemsPrompt(stageNum, context);
            }
        }
    }
    
    public class ItemsConsumeListPrompt extends QuestsEditorNumericPrompt {
        
        public ItemsConsumeListPrompt(ConversationContext context) {
            super(context);
        }
        
        private final int size = 3;
        
        public int getSize() {
            return size;
        }
        
        public String getTitle(ConversationContext context) {
            return Lang.get("stageEditorConsumeItems");
        }
        
        public ChatColor getNumberColor(ConversationContext context, int number) {
            switch (number) {
                case 1:
                    return ChatColor.BLUE;
                case 2:
                    return ChatColor.RED;
                case 3:
                    return ChatColor.GREEN;
                default:
                    return null;
            }
        }
        
        public String getSelectionText(ConversationContext context, int number) {
            switch(number) {
            case 1:
                return ChatColor.YELLOW + Lang.get("stageEditorDeliveryAddItem");
            case 2:
                return ChatColor.RED + Lang.get("clear");
            case 3:
                return ChatColor.GREEN + Lang.get("done");
            default:
                return null;
            }
        }
        
        @SuppressWarnings("unchecked")
        public String getAdditionalText(ConversationContext context, int number) {
            switch(number) {
            case 1:
                if (context.getSessionData(pref + CK.S_CONSUME_ITEMS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    String text = "\n";
                    for (ItemStack is : (List<ItemStack>) context.getSessionData(pref + CK.S_CONSUME_ITEMS)) {
                        text += ChatColor.GRAY + "     - " + ItemUtil.getDisplayString(is) + "\n";
                    }
                    return text;
                }
            case 2:
            case 3:
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
                if (context.getSessionData(pref + CK.S_CONSUME_ITEMS) != null) {
                    List<ItemStack> items = (List<ItemStack>) context.getSessionData(pref + CK.S_CONSUME_ITEMS);
                    items.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(pref + CK.S_CONSUME_ITEMS, items);
                } else {
                    LinkedList<ItemStack> items = new LinkedList<ItemStack>();
                    items.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(pref + CK.S_CONSUME_ITEMS, items);
                }
                context.setSessionData("newItem", null);
                context.setSessionData("tempStack", null);
            }
            
            QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);

            String text = ChatColor.GOLD + "- " + getTitle(context) + " -\n";
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
                return new ItemStackPrompt(ItemsConsumeListPrompt.this);
            case 2:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorObjectiveCleared"));
                context.setSessionData(pref + CK.S_CONSUME_ITEMS, null);
                return new ItemsConsumeListPrompt(context);
            case 3:
                return new ItemsPrompt(stageNum, context);
            default:
                return new ItemsPrompt(stageNum, context);
            }
        }
    }
}
