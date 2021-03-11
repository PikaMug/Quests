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

package me.blackvein.quests.convo.quests.objectives;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.inventory.ItemStack;

import me.blackvein.quests.convo.generic.ItemStackPrompt;
import me.blackvein.quests.convo.quests.QuestsEditorNumericPrompt;
import me.blackvein.quests.convo.quests.stages.StageMainPrompt;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenNumericPromptEvent;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;

public class ItemsPrompt extends QuestsEditorNumericPrompt {
    private final int stageNum;
    private final String pref;

    public ItemsPrompt(final int stageNum, final ConversationContext context) {
        super(context);
        this.stageNum = stageNum;
        this.pref = "stage" + stageNum;
    }
    
    private final int size = 6;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle(final ConversationContext context) {
        return Lang.get("stageEditorItems");
    }
    
    @Override
    public ChatColor getNumberColor(final ConversationContext context, final int number) {
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
    
    @Override
    public String getSelectionText(final ConversationContext context, final int number) {
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
    
    @Override
    @SuppressWarnings("unchecked")
    public String getAdditionalText(final ConversationContext context, final int number) {
        switch(number) {
        case 1:
            if (context.getSessionData(pref + CK.S_CRAFT_ITEMS) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                String text = "\n";
                final LinkedList<ItemStack> items = (LinkedList<ItemStack>) context.getSessionData(pref + CK.S_CRAFT_ITEMS);
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
                final LinkedList<ItemStack> items = (LinkedList<ItemStack>) context.getSessionData(pref + CK.S_SMELT_ITEMS);
                for (int i = 0; i < items.size(); i++) {
                    text += ChatColor.GRAY + "     - " + ChatColor.BLUE + ItemUtil.getName(items.get(i)) 
                            + ChatColor.GRAY + " x " + ChatColor.AQUA + items.get(i).getAmount() + "\n";
                }
                return text;
            }
        case 3:
            if (context.getSessionData(pref + CK.S_ENCHANT_ITEMS) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                String text = "\n";
                final LinkedList<ItemStack> items = (LinkedList<ItemStack>) context.getSessionData(pref + CK.S_ENCHANT_ITEMS);
                for (int i = 0; i < items.size(); i++) {
                    text += ChatColor.GRAY + "     - " + ChatColor.BLUE + ItemUtil.getName(items.get(i)) 
                            + ChatColor.GRAY + " x " + ChatColor.AQUA + items.get(i).getAmount() + "\n";
                }
                return text;
            }
        case 4:
            if (context.getSessionData(pref + CK.S_BREW_ITEMS) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                String text = "\n";
                final LinkedList<ItemStack> items = (LinkedList<ItemStack>) context.getSessionData(pref + CK.S_BREW_ITEMS);
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
                final LinkedList<ItemStack> items = (LinkedList<ItemStack>) context.getSessionData(pref + CK.S_CONSUME_ITEMS);
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
    public String getPromptText(final ConversationContext context) {
        // Check/add newly made item
        if (context.getSessionData("newItem") != null) {
            if (context.getSessionData(pref + CK.S_CRAFT_ITEMS) != null) {
                final List<ItemStack> items = (List<ItemStack>) context.getSessionData(pref + CK.S_CRAFT_ITEMS);
                items.add((ItemStack) context.getSessionData("tempStack"));
                context.setSessionData(pref + CK.S_CRAFT_ITEMS, items);
            } else if (context.getSessionData(pref + CK.S_SMELT_ITEMS) != null) {
                final List<ItemStack> items = (List<ItemStack>) context.getSessionData(pref + CK.S_SMELT_ITEMS);
                items.add((ItemStack) context.getSessionData("tempStack"));
                context.setSessionData(pref + CK.S_SMELT_ITEMS, items);
            }
            context.setSessionData("newItem", null);
            context.setSessionData("tempStack", null);
        }
        context.setSessionData(pref, Boolean.TRUE);
        
        final QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
        context.getPlugin().getServer().getPluginManager().callEvent(event);
        
        String text = ChatColor.AQUA + "- " + getTitle(context) + " -";
        for (int i = 1; i <= size; i++) {
            text += "\n" + getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                    + getSelectionText(context, i) + " " + getAdditionalText(context, i);
        }
        return text;
    }

    @Override
    protected Prompt acceptValidatedInput(final ConversationContext context, final Number input) {
        switch(input.intValue()) {
        case 1:
            return new ItemsCraftListPrompt(context);
        case 2:
            return new ItemsSmeltListPrompt(context);
        case 3:
            return new ItemsEnchantListPrompt(context);
        case 4:
            return new ItemsBrewListPrompt(context);
        case 5:
            return new ItemsConsumeListPrompt(context);
        case 6:
            try {
                return new StageMainPrompt(stageNum, context);
            } catch (final Exception e) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateCriticalError"));
                return Prompt.END_OF_CONVERSATION;
            }
        default:
            return new ItemsPrompt(stageNum, context);
        }
    }
    
    public class ItemsCraftListPrompt extends QuestsEditorNumericPrompt {
        
        public ItemsCraftListPrompt(final ConversationContext context) {
            super(context);
        }

        private final int size = 3;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("stageEditorCraftItems");
        }
        
        @Override
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
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
        
        @Override
        public String getSelectionText(final ConversationContext context, final int number) {
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
        
        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                if (context.getSessionData(pref + CK.S_CRAFT_ITEMS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    String text = "\n";
                    for (final ItemStack is : (List<ItemStack>) context.getSessionData(pref + CK.S_CRAFT_ITEMS)) {
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
        public String getPromptText(final ConversationContext context) {
            // Check/add newly made item
            if (context.getSessionData("newItem") != null) {
                if (context.getSessionData(pref + CK.S_CRAFT_ITEMS) != null) {
                    final List<ItemStack> items = (List<ItemStack>) context.getSessionData(pref + CK.S_CRAFT_ITEMS);
                    items.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(pref + CK.S_CRAFT_ITEMS, items);
                } else {
                    final LinkedList<ItemStack> items = new LinkedList<ItemStack>();
                    items.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(pref + CK.S_CRAFT_ITEMS, items);
                }
                context.setSessionData("newItem", null);
                context.setSessionData("tempStack", null);
            }
            
            final QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);

            String text = ChatColor.GOLD + "- " + getTitle(context) + " -";
            for (int i = 1; i <= size; i++) {
                text += "\n" + getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                        + getSelectionText(context, i) + " " + getAdditionalText(context, i);
            }
            return text;
        }
        
        @Override
        protected Prompt acceptValidatedInput(final ConversationContext context, final Number input) {
            switch(input.intValue()) {
            case 1:
                return new ItemStackPrompt(context, ItemsCraftListPrompt.this);
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
        
        public ItemsSmeltListPrompt(final ConversationContext context) {
            super(context);
        }
        
        private final int size = 3;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("stageEditorSmeltItems");
        }
        
        @Override
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
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
        
        @Override
        public String getSelectionText(final ConversationContext context, final int number) {
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
        
        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                if (context.getSessionData(pref + CK.S_SMELT_ITEMS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    String text = "\n";
                    for (final ItemStack is : (List<ItemStack>) context.getSessionData(pref + CK.S_SMELT_ITEMS)) {
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
        public String getPromptText(final ConversationContext context) {
            // Check/add newly made item
            if (context.getSessionData("newItem") != null) {
                if (context.getSessionData(pref + CK.S_SMELT_ITEMS) != null) {
                    final List<ItemStack> items = (List<ItemStack>) context.getSessionData(pref + CK.S_SMELT_ITEMS);
                    items.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(pref + CK.S_SMELT_ITEMS, items);
                } else {
                    final LinkedList<ItemStack> items = new LinkedList<ItemStack>();
                    items.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(pref + CK.S_SMELT_ITEMS, items);
                }
                context.setSessionData("newItem", null);
                context.setSessionData("tempStack", null);
            }
            
            final QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);

            String text = ChatColor.GOLD + "- " + getTitle(context) + " -";
            for (int i = 1; i <= size; i++) {
                text += "\n" + getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                        + getSelectionText(context, i) + " " + getAdditionalText(context, i);
            }
            return text;
        }
        
        @Override
        protected Prompt acceptValidatedInput(final ConversationContext context, final Number input) {
            switch(input.intValue()) {
            case 1:
                return new ItemStackPrompt(context, ItemsSmeltListPrompt.this);
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

    public class ItemsEnchantListPrompt extends QuestsEditorNumericPrompt {

        public ItemsEnchantListPrompt(final ConversationContext context) {
            super(context);
        }
        
        private final int size = 3;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("stageEditorEnchantItems");
        }
        
        @Override
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
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
        
        @Override
        public String getSelectionText(final ConversationContext context, final int number) {
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
        
        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                if (context.getSessionData(pref + CK.S_ENCHANT_ITEMS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    String text = "\n";
                    for (final ItemStack is : (List<ItemStack>) context.getSessionData(pref + CK.S_ENCHANT_ITEMS)) {
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
        public String getPromptText(final ConversationContext context) {
            // Check/add newly made item
            if (context.getSessionData("newItem") != null) {
                if (context.getSessionData(pref + CK.S_ENCHANT_ITEMS) != null) {
                    final List<ItemStack> items = (List<ItemStack>) context.getSessionData(pref + CK.S_ENCHANT_ITEMS);
                    items.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(pref + CK.S_ENCHANT_ITEMS, items);
                } else {
                    final LinkedList<ItemStack> items = new LinkedList<ItemStack>();
                    items.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(pref + CK.S_ENCHANT_ITEMS, items);
                }
                context.setSessionData("newItem", null);
                context.setSessionData("tempStack", null);
            }
            
            final QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);

            String text = ChatColor.GOLD + "- " + getTitle(context) + " -";
            for (int i = 1; i <= size; i++) {
                text += "\n" + getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                        + getSelectionText(context, i) + " " + getAdditionalText(context, i);
            }
            return text;
        }
        
        @Override
        protected Prompt acceptValidatedInput(final ConversationContext context, final Number input) {
            switch(input.intValue()) {
            case 1:
                return new ItemStackPrompt(context, ItemsEnchantListPrompt.this);
            case 2:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorObjectiveCleared"));
                context.setSessionData(pref + CK.S_ENCHANT_ITEMS, null);
                return new ItemsEnchantListPrompt(context);
            case 3:
                return new ItemsPrompt(stageNum, context);
            default:
                return new ItemsPrompt(stageNum, context);
            }
        }
    }
    
    public class ItemsBrewListPrompt extends QuestsEditorNumericPrompt {
        
        public ItemsBrewListPrompt(final ConversationContext context) {
            super(context);
        }
        
        private final int size = 3;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("stageEditorBrewPotions");
        }
        
        @Override
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
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
        
        @Override
        public String getSelectionText(final ConversationContext context, final int number) {
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
        
        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                if (context.getSessionData(pref + CK.S_BREW_ITEMS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    String text = "\n";
                    for (final ItemStack is : (List<ItemStack>) context.getSessionData(pref + CK.S_BREW_ITEMS)) {
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
        public String getPromptText(final ConversationContext context) {
            // Check/add newly made item
            if (context.getSessionData("newItem") != null) {
                if (context.getSessionData(pref + CK.S_BREW_ITEMS) != null) {
                    final List<ItemStack> items = (List<ItemStack>) context.getSessionData(pref + CK.S_BREW_ITEMS);
                    items.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(pref + CK.S_BREW_ITEMS, items);
                } else {
                    final LinkedList<ItemStack> items = new LinkedList<ItemStack>();
                    items.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(pref + CK.S_BREW_ITEMS, items);
                }
                context.setSessionData("newItem", null);
                context.setSessionData("tempStack", null);
            }
            
            final QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);

            String text = ChatColor.GOLD + "- " + getTitle(context) + " -";
            for (int i = 1; i <= size; i++) {
                text += "\n" + getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                        + getSelectionText(context, i) + " " + getAdditionalText(context, i);
            }
            return text;
        }
        
        @Override
        protected Prompt acceptValidatedInput(final ConversationContext context, final Number input) {
            switch(input.intValue()) {
            case 1:
                return new ItemStackPrompt(context, ItemsBrewListPrompt.this);
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
        
        public ItemsConsumeListPrompt(final ConversationContext context) {
            super(context);
        }
        
        private final int size = 3;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("stageEditorConsumeItems");
        }
        
        @Override
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
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
        
        @Override
        public String getSelectionText(final ConversationContext context, final int number) {
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
        
        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                if (context.getSessionData(pref + CK.S_CONSUME_ITEMS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    String text = "\n";
                    for (final ItemStack is : (List<ItemStack>) context.getSessionData(pref + CK.S_CONSUME_ITEMS)) {
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
        public String getPromptText(final ConversationContext context) {
            // Check/add newly made item
            if (context.getSessionData("newItem") != null) {
                if (context.getSessionData(pref + CK.S_CONSUME_ITEMS) != null) {
                    final List<ItemStack> items = (List<ItemStack>) context.getSessionData(pref + CK.S_CONSUME_ITEMS);
                    items.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(pref + CK.S_CONSUME_ITEMS, items);
                } else {
                    final LinkedList<ItemStack> items = new LinkedList<ItemStack>();
                    items.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(pref + CK.S_CONSUME_ITEMS, items);
                }
                context.setSessionData("newItem", null);
                context.setSessionData("tempStack", null);
            }
            
            final QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);

            String text = ChatColor.GOLD + "- " + getTitle(context) + " -";
            for (int i = 1; i <= size; i++) {
                text += "\n" + getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                        + getSelectionText(context, i) + " " + getAdditionalText(context, i);
            }
            return text;
        }
        
        @Override
        protected Prompt acceptValidatedInput(final ConversationContext context, final Number input) {
            switch(input.intValue()) {
            case 1:
                return new ItemStackPrompt(context, ItemsConsumeListPrompt.this);
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
