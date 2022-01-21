/*
 * Copyright (c) 2014 PikaMug and contributors. All rights reserved.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package me.blackvein.quests.convo.conditions.tasks;

import me.blackvein.quests.convo.conditions.main.ConditionMainPrompt;
import me.blackvein.quests.convo.generic.ItemStackPrompt;
import me.blackvein.quests.convo.quests.QuestsEditorNumericPrompt;
import me.blackvein.quests.convo.quests.QuestsEditorStringPrompt;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenNumericPromptEvent;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenStringPromptEvent;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class PlayerPrompt extends QuestsEditorNumericPrompt {
    
    public PlayerPrompt(final ConversationContext context) {
        super(context);
    }
    
    private final int size = 3;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle(final ConversationContext context) {
        return Lang.get("eventEditorPlayer");
    }
    
    @Override
    public ChatColor getNumberColor(final ConversationContext context, final int number) {
        switch (number) {
            case 1:
            case 2:
                return ChatColor.BLUE;
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
            return ChatColor.YELLOW + Lang.get("conditionEditorPermissions");
        case 2:
            return ChatColor.YELLOW + Lang.get("conditionEditorItemsInMainHand");
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
            if (context.getSessionData(CK.C_WHILE_PERMISSION) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final List<String> whilePermission = (List<String>) context.getSessionData(CK.C_WHILE_PERMISSION);
                if (whilePermission != null) {
                    for (final String s: whilePermission) {
                        // Replace standard period characters to prevent clickable links
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.BLUE)
                                .append(s.replace(".", "\uFF0E"));
                    }
                }
                return text.toString();
            }
        case 2:
            if (context.getSessionData(CK.C_WHILE_HOLDING_MAIN_HAND) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<ItemStack> whileHoldingMainHand
                        = (LinkedList<ItemStack>) context.getSessionData(CK.C_WHILE_HOLDING_MAIN_HAND);
                if (whileHoldingMainHand != null) {
                    for (final ItemStack item : whileHoldingMainHand) {
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.BLUE)
                                .append(ItemUtil.getName(item)).append(ChatColor.GRAY).append(" x ")
                                .append(ChatColor.AQUA).append(item.getAmount());
                    }
                }
                return text.toString();
            }
        case 3:
            return "";
        default:
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull String getBasicPromptText(final ConversationContext context) {
        // Check/add newly made item
        if (context.getSessionData("tempStack") != null) {
            if (context.getSessionData(CK.C_WHILE_HOLDING_MAIN_HAND) != null) {
                final List<ItemStack> items = (List<ItemStack>) context.getSessionData(CK.C_WHILE_HOLDING_MAIN_HAND);
                if (items != null) {
                    items.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(CK.C_WHILE_HOLDING_MAIN_HAND, items);
                }
            }
            ItemStackPrompt.clearSessionData(context);
        }

        if (context.getPlugin() != null) {
            final QuestsEditorPostOpenNumericPromptEvent event
                    = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
        }
        
        final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle(context) + " -");
        for (int i = 1; i <= size; i++) {
            text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                    .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i)).append(" ")
                    .append(getAdditionalText(context, i));
        }
        return text.toString();
    }
    
    @Override
    protected Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
        switch(input.intValue()) {
        case 1:
            return new PermissionsPrompt(context);
        case 2:
            return new ItemsInMainHandListPrompt(context);
        case 3:
            try {
                return new ConditionMainPrompt(context);
            } catch (final Exception e) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateCriticalError"));
                return Prompt.END_OF_CONVERSATION;
            }
        default:
            return new PlayerPrompt(context);
        }
    }
    
    public class PermissionsPrompt extends QuestsEditorStringPrompt {
        
        public PermissionsPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("conditionEditorPermissionsPrompt");
        }
        
        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                final LinkedList<String> permissions = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    permissions.add(s.trim());
                }
                context.setSessionData(CK.C_WHILE_PERMISSION, permissions);
            }
            return new PlayerPrompt(context);
        }
    }
    
    public class ItemsInMainHandListPrompt extends QuestsEditorNumericPrompt {
        
        public ItemsInMainHandListPrompt(final ConversationContext context) {
            super(context);
        }

        private final int size = 3;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("conditionEditorItemsInMainHand");
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
                if (context.getSessionData(CK.C_WHILE_HOLDING_MAIN_HAND) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<ItemStack> whileHoldingMainHand
                            = (List<ItemStack>) context.getSessionData(CK.C_WHILE_HOLDING_MAIN_HAND);
                    if (whileHoldingMainHand != null) {
                        for (final ItemStack is : whileHoldingMainHand) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ")
                                    .append(ItemUtil.getDisplayString(is));
                        }
                    }
                    return text.toString();
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
        public String getBasicPromptText(final ConversationContext context) {
            // Check/add newly made item
            if (context.getSessionData("tempStack") != null) {
                if (context.getSessionData(CK.C_WHILE_HOLDING_MAIN_HAND) != null) {
                    final List<ItemStack> items
                            = (List<ItemStack>) context.getSessionData(CK.C_WHILE_HOLDING_MAIN_HAND);
                    if (items != null) {
                        items.add((ItemStack) context.getSessionData("tempStack"));
                        context.setSessionData(CK.C_WHILE_HOLDING_MAIN_HAND, items);
                    }
                } else {
                    final LinkedList<ItemStack> items = new LinkedList<ItemStack>();
                    items.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(CK.C_WHILE_HOLDING_MAIN_HAND, items);
                }
                ItemStackPrompt.clearSessionData(context);
            }

            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenNumericPromptEvent event
                        = new QuestsEditorPostOpenNumericPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }

            final StringBuilder text = new StringBuilder(ChatColor.GOLD + "- " + getTitle(context) + " -");
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i)).append(" ")
                        .append(getAdditionalText(context, i));
            }
            return text.toString();
        }
        
        @Override
        protected Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
            switch(input.intValue()) {
            case 1:
                return new ItemStackPrompt(context, ItemsInMainHandListPrompt.this);
            case 2:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("conditionEditorConditionCleared"));
                context.setSessionData(CK.C_WHILE_HOLDING_MAIN_HAND, null);
                return new ItemsInMainHandListPrompt(context);
            case 3:
                return new PlayerPrompt(context);
            default:
                return new ItemsInMainHandListPrompt(context);
            }
        }
    }
}
