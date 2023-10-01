/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.convo.conditions.tasks;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.convo.conditions.ConditionsEditorNumericPrompt;
import me.pikamug.quests.convo.conditions.ConditionsEditorStringPrompt;
import me.pikamug.quests.convo.conditions.main.ConditionMainPrompt;
import me.pikamug.quests.convo.generic.ItemStackPrompt;
import me.pikamug.quests.events.editor.conditions.ConditionsEditorPostOpenNumericPromptEvent;
import me.pikamug.quests.events.editor.conditions.ConditionsEditorPostOpenStringPromptEvent;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.BukkitItemUtil;
import me.pikamug.quests.util.BukkitLang;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class ConditionPlayerPrompt extends ConditionsEditorNumericPrompt {

    private final BukkitQuestsPlugin plugin;
    
    public ConditionPlayerPrompt(final ConversationContext context) {
        super(context);
        this.plugin = (BukkitQuestsPlugin)context.getPlugin();
    }
    
    private final int size = 4;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle(final ConversationContext context) {
        return BukkitLang.get("eventEditorPlayer");
    }
    
    @Override
    public ChatColor getNumberColor(final ConversationContext context, final int number) {
        switch (number) {
            case 1:
            case 2:
            case 3:
                return ChatColor.BLUE;
            case 4:
                return ChatColor.GREEN;
            default:
                return null;
        }
    }
    
    @Override
    public String getSelectionText(final ConversationContext context, final int number) {
        switch(number) {
        case 1:
            return ChatColor.YELLOW + BukkitLang.get("conditionEditorPermissions");
        case 2:
            return ChatColor.YELLOW + BukkitLang.get("conditionEditorItemsInMainHand");
        case 3:
            return ChatColor.YELLOW + BukkitLang.get("conditionEditorItemsWear");
        case 4:
            return ChatColor.GREEN + BukkitLang.get("done");
        default:
            return null;
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public String getAdditionalText(final ConversationContext context, final int number) {
        switch(number) {
        case 1:
            if (context.getSessionData(Key.C_WHILE_PERMISSION) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final List<String> whilePermission = (List<String>) context.getSessionData(Key.C_WHILE_PERMISSION);
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
            if (context.getSessionData(Key.C_WHILE_HOLDING_MAIN_HAND) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<ItemStack> whileHoldingMainHand
                        = (LinkedList<ItemStack>) context.getSessionData(Key.C_WHILE_HOLDING_MAIN_HAND);
                if (whileHoldingMainHand != null) {
                    for (final ItemStack item : whileHoldingMainHand) {
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.BLUE)
                                .append(BukkitItemUtil.getName(item)).append(ChatColor.GRAY).append(" x ")
                                .append(ChatColor.AQUA).append(item.getAmount());
                    }
                }
                return text.toString();
            }
        case 3:
            if (context.getSessionData(Key.C_WHILE_WEARING) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<ItemStack> whileWearing
                        = (LinkedList<ItemStack>) context.getSessionData(Key.C_WHILE_WEARING);
                if (whileWearing != null) {
                    for (final ItemStack item : whileWearing) {
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.BLUE)
                                .append(BukkitItemUtil.getName(item)).append(ChatColor.GRAY).append(" x ")
                                .append(ChatColor.AQUA).append(item.getAmount());
                    }
                }
                return text.toString();
            }
        case 4:
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
            if (context.getSessionData(Key.C_WHILE_HOLDING_MAIN_HAND) != null) {
                final List<ItemStack> items = (List<ItemStack>) context.getSessionData(Key.C_WHILE_HOLDING_MAIN_HAND);
                if (items != null) {
                    items.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(Key.C_WHILE_HOLDING_MAIN_HAND, items);
                }
            }
            ItemStackPrompt.clearSessionData(context);
        }

        final ConditionsEditorPostOpenNumericPromptEvent event
                = new ConditionsEditorPostOpenNumericPromptEvent(context, this);
        plugin.getServer().getPluginManager().callEvent(event);
        
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
            return new ConditionPermissionsPrompt(context);
        case 2:
            return new ConditionItemsInMainHandListPrompt(context);
        case 3:
            return new ConditionItemsWearListPrompt(context);
        case 4:
            try {
                return new ConditionMainPrompt(context);
            } catch (final Exception e) {
                context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("itemCreateCriticalError"));
                return Prompt.END_OF_CONVERSATION;
            }
        default:
            return new ConditionPlayerPrompt(context);
        }
    }
    
    public class ConditionPermissionsPrompt extends ConditionsEditorStringPrompt {
        
        public ConditionPermissionsPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("conditionEditorPermissionsPrompt");
        }
        
        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final ConditionsEditorPostOpenStringPromptEvent event
                    = new ConditionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final LinkedList<String> permissions = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    permissions.add(s.trim());
                }
                context.setSessionData(Key.C_WHILE_PERMISSION, permissions);
            }
            return new ConditionPlayerPrompt(context);
        }
    }
    
    public class ConditionItemsInMainHandListPrompt extends ConditionsEditorNumericPrompt {
        
        public ConditionItemsInMainHandListPrompt(final ConversationContext context) {
            super(context);
        }

        private final int size = 3;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("conditionEditorItemsInMainHand");
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
                return ChatColor.YELLOW + BukkitLang.get("stageEditorDeliveryAddItem");
            case 2:
                return ChatColor.RED + BukkitLang.get("clear");
            case 3:
                return ChatColor.GREEN + BukkitLang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                if (context.getSessionData(Key.C_WHILE_HOLDING_MAIN_HAND) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<ItemStack> whileHoldingMainHand
                            = (List<ItemStack>) context.getSessionData(Key.C_WHILE_HOLDING_MAIN_HAND);
                    if (whileHoldingMainHand != null) {
                        for (final ItemStack is : whileHoldingMainHand) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ")
                                    .append(BukkitItemUtil.getDisplayString(is));
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
                if (context.getSessionData(Key.C_WHILE_HOLDING_MAIN_HAND) != null) {
                    final List<ItemStack> items
                            = (List<ItemStack>) context.getSessionData(Key.C_WHILE_HOLDING_MAIN_HAND);
                    if (items != null) {
                        items.add((ItemStack) context.getSessionData("tempStack"));
                        context.setSessionData(Key.C_WHILE_HOLDING_MAIN_HAND, items);
                    }
                } else {
                    final LinkedList<ItemStack> items = new LinkedList<>();
                    items.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(Key.C_WHILE_HOLDING_MAIN_HAND, items);
                }
                ItemStackPrompt.clearSessionData(context);
            }

            final ConditionsEditorPostOpenNumericPromptEvent event
                    = new ConditionsEditorPostOpenNumericPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);

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
                return new ItemStackPrompt(context, ConditionItemsInMainHandListPrompt.this);
            case 2:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("conditionEditorConditionCleared"));
                context.setSessionData(Key.C_WHILE_HOLDING_MAIN_HAND, null);
                return new ConditionItemsInMainHandListPrompt(context);
            case 3:
                return new ConditionPlayerPrompt(context);
            default:
                return new ConditionItemsInMainHandListPrompt(context);
            }
        }
    }

    public class ConditionItemsWearListPrompt extends ConditionsEditorNumericPrompt {

        public ConditionItemsWearListPrompt(final ConversationContext context) {
            super(context);
        }

        private final int size = 3;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("conditionEditorItemsWear");
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
                    return ChatColor.YELLOW + BukkitLang.get("stageEditorDeliveryAddItem");
                case 2:
                    return ChatColor.RED + BukkitLang.get("clear");
                case 3:
                    return ChatColor.GREEN + BukkitLang.get("done");
                default:
                    return null;
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch(number) {
                case 1:
                    if (context.getSessionData(Key.C_WHILE_WEARING) == null) {
                        return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                    } else {
                        final StringBuilder text = new StringBuilder();
                        final List<ItemStack> whileWearing
                                = (List<ItemStack>) context.getSessionData(Key.C_WHILE_WEARING);
                        if (whileWearing != null) {
                            for (final ItemStack is : whileWearing) {
                                text.append("\n").append(ChatColor.GRAY).append("     - ")
                                        .append(BukkitItemUtil.getDisplayString(is));
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
                if (context.getSessionData(Key.C_WHILE_WEARING) != null) {
                    final List<ItemStack> items
                            = (List<ItemStack>) context.getSessionData(Key.C_WHILE_WEARING);
                    if (items != null) {
                        items.add((ItemStack) context.getSessionData("tempStack"));
                        context.setSessionData(Key.C_WHILE_WEARING, items);
                    }
                } else {
                    final LinkedList<ItemStack> items = new LinkedList<>();
                    items.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(Key.C_WHILE_WEARING, items);
                }
                ItemStackPrompt.clearSessionData(context);
            }

            final ConditionsEditorPostOpenNumericPromptEvent event
                    = new ConditionsEditorPostOpenNumericPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);

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
                    return new ItemStackPrompt(context, ConditionItemsWearListPrompt.this);
                case 2:
                    context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("conditionEditorConditionCleared"));
                    context.setSessionData(Key.C_WHILE_WEARING, null);
                    return new ConditionItemsWearListPrompt(context);
                case 3:
                    return new ConditionPlayerPrompt(context);
                default:
                    return new ConditionItemsWearListPrompt(context);
            }
        }
    }
}
