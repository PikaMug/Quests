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

package me.blackvein.quests.convo.conditions.tasks;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.inventory.ItemStack;

import me.blackvein.quests.convo.conditions.main.ConditionMainPrompt;
import me.blackvein.quests.convo.generic.ItemStackPrompt;
import me.blackvein.quests.convo.quests.QuestsEditorNumericPrompt;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenNumericPromptEvent;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;

public class PlayerPrompt extends QuestsEditorNumericPrompt {
    
    public PlayerPrompt(ConversationContext context) {
        super(context);
    }
    
    private final int size = 2;
    
    public int getSize() {
        return size;
    }
    
    public String getTitle(ConversationContext context) {
        return Lang.get("eventEditorPlayer");
    }
    
    public ChatColor getNumberColor(ConversationContext context, int number) {
        switch (number) {
            case 1:
                return ChatColor.BLUE;
            case 2:
                return ChatColor.GREEN;
            default:
                return null;
        }
    }
    
    public String getSelectionText(ConversationContext context, int number) {
        switch(number) {
        case 1:
            return ChatColor.YELLOW + Lang.get("conditionEditorItemsInMainHand");
        case 2:
            return ChatColor.GREEN + Lang.get("done");
        default:
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    public String getAdditionalText(ConversationContext context, int number) {
        switch(number) {
        case 1:
            if (context.getSessionData(CK.C_WHILE_HOLDING_MAIN_HAND) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                String text = "\n";
                LinkedList<ItemStack> items = (LinkedList<ItemStack>) context.getSessionData(CK.C_WHILE_HOLDING_MAIN_HAND);
                for (int i = 0; i < items.size(); i++) {
                    text += ChatColor.GRAY + "     - " + ChatColor.BLUE + ItemUtil.getName(items.get(i)) 
                            + ChatColor.GRAY + " x " + ChatColor.AQUA + items.get(i).getAmount() + "\n";
                }
                return text;
            }
        case 2:
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
            if (context.getSessionData(CK.C_WHILE_HOLDING_MAIN_HAND) != null) {
                List<ItemStack> items = (List<ItemStack>) context.getSessionData(CK.C_WHILE_HOLDING_MAIN_HAND);
                items.add((ItemStack) context.getSessionData("tempStack"));
                context.setSessionData(CK.C_WHILE_HOLDING_MAIN_HAND, items);
            }
            context.setSessionData("newItem", null);
            context.setSessionData("tempStack", null);
        }
        
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
            return new ItemsInMainHandListPrompt(context);
        case 2:
            try {
                return new ConditionMainPrompt(context);
            } catch (Exception e) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateCriticalError"));
                return Prompt.END_OF_CONVERSATION;
            }
        default:
            return new PlayerPrompt(context);
        }
    }
    
    public class ItemsInMainHandListPrompt extends QuestsEditorNumericPrompt {
        
        public ItemsInMainHandListPrompt(ConversationContext context) {
            super(context);
        }

        private final int size = 3;
        
        public int getSize() {
            return size;
        }
        
        public String getTitle(ConversationContext context) {
            return Lang.get("conditionEditorItemsInMainHand");
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
                if (context.getSessionData(CK.C_WHILE_HOLDING_MAIN_HAND) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    String text = "\n";
                    for (ItemStack is : (List<ItemStack>) context.getSessionData(CK.C_WHILE_HOLDING_MAIN_HAND)) {
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
                if (context.getSessionData(CK.C_WHILE_HOLDING_MAIN_HAND) != null) {
                    List<ItemStack> items = (List<ItemStack>) context.getSessionData(CK.C_WHILE_HOLDING_MAIN_HAND);
                    items.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(CK.C_WHILE_HOLDING_MAIN_HAND, items);
                } else {
                    LinkedList<ItemStack> items = new LinkedList<ItemStack>();
                    items.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(CK.C_WHILE_HOLDING_MAIN_HAND, items);
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
                return new ItemStackPrompt(ItemsInMainHandListPrompt.this);
            case 2:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("conditionEditorConditionCleared"));
                context.setSessionData(CK.C_WHILE_HOLDING_MAIN_HAND, null);
                return new ItemsInMainHandListPrompt(context);
            case 3:
                return new PlayerPrompt(context);
            default:
                return new PlayerPrompt(context);
            }
        }
    }
}
