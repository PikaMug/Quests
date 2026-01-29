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
import me.pikamug.quests.convo.conditions.ConditionsEditorIntegerPrompt;
import me.pikamug.quests.convo.conditions.ConditionsEditorStringPrompt;
import me.pikamug.quests.convo.conditions.main.ConditionMainPrompt;
import me.pikamug.quests.convo.generic.ItemStackPrompt;
import me.pikamug.quests.events.editor.conditions.BukkitConditionsEditorPostOpenNumericPromptEvent;
import me.pikamug.quests.events.editor.conditions.BukkitConditionsEditorPostOpenStringPromptEvent;
import me.pikamug.quests.util.BukkitItemUtil;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.quests.util.BukkitMiscUtil;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.SessionData;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class ConditionPlayerPrompt extends ConditionsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final BukkitQuestsPlugin plugin;
    
    public ConditionPlayerPrompt(final @NotNull UUID uuid) {
        super(uuid);
        this.uuid = uuid;
        this.plugin = BukkitQuestsPlugin.getInstance();
    }
    
    private final int size = 4;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle() {
        return BukkitLang.get("eventEditorPlayer");
    }
    
    @Override
    public ChatColor getNumberColor(final int number) {
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
    public String getSelectionText(final int number) {
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
    public String getAdditionalText(final int number) {
        switch(number) {
        case 1:
            if (SessionData.get(uuid, Key.C_WHILE_PERMISSION) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final List<String> whilePermission = (List<String>) SessionData.get(uuid, Key.C_WHILE_PERMISSION);
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
            if (SessionData.get(uuid, Key.C_WHILE_HOLDING_MAIN_HAND) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<ItemStack> whileHoldingMainHand
                        = (LinkedList<ItemStack>) SessionData.get(uuid, Key.C_WHILE_HOLDING_MAIN_HAND);
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
            if (SessionData.get(uuid, Key.C_WHILE_WEARING) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<ItemStack> whileWearing
                        = (LinkedList<ItemStack>) SessionData.get(uuid, Key.C_WHILE_WEARING);
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

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull String getPromptText() {
        // Check/add newly made item
        if (SessionData.get(uuid, "tempStack") != null) {
            if (SessionData.get(uuid, Key.C_WHILE_HOLDING_MAIN_HAND) != null) {
                final List<ItemStack> items = (List<ItemStack>) SessionData.get(uuid, Key.C_WHILE_HOLDING_MAIN_HAND);
                if (items != null) {
                    items.add((ItemStack) SessionData.get(uuid, "tempStack"));
                    SessionData.set(uuid, Key.C_WHILE_HOLDING_MAIN_HAND, items);
                }
            }
            ItemStackPrompt.clearSessionData(uuid);
        }

        final BukkitConditionsEditorPostOpenNumericPromptEvent event
                = new BukkitConditionsEditorPostOpenNumericPromptEvent(uuid, this);
        plugin.getServer().getPluginManager().callEvent(event);
        
        final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle() + " -");
        for (int i = 1; i <= size; i++) {
            text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                    .append(ChatColor.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                    .append(getAdditionalText(i));
        }
        return sendClickableSelection(text.toString(), plugin.getQuester(uuid));
    }

    @Override
    public void acceptInput(final Number input) {
        final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
        switch(input.intValue()) {
        case 1:
            new ConditionPermissionsPrompt(uuid).start();
            break;
        case 2:
            new ConditionItemsInMainHandListPrompt(uuid).start();
            break;
        case 3:
            new ConditionItemsWearListPrompt(uuid).start();
            break;
        case 4:
            try {
                new ConditionMainPrompt(uuid).start();
            } catch (final Exception e) {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("itemCreateCriticalError"));
            }
            break;
        default:
            new ConditionPlayerPrompt(uuid).start();
            break;
        }
    }
    
    public class ConditionPermissionsPrompt extends ConditionsEditorStringPrompt {
        
        public ConditionPermissionsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("conditionEditorPermissionsPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitConditionsEditorPostOpenStringPromptEvent event
                    = new BukkitConditionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final LinkedList<String> permissions = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    permissions.add(s.trim());
                }
                SessionData.set(uuid, Key.C_WHILE_PERMISSION, permissions);
            }
            new ConditionPlayerPrompt(uuid).start();
        }
    }
    
    public class ConditionItemsInMainHandListPrompt extends ConditionsEditorIntegerPrompt {
        
        public ConditionItemsInMainHandListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 3;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle() {
            return BukkitLang.get("conditionEditorItemsInMainHand");
        }
        
        @Override
        public ChatColor getNumberColor(final int number) {
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
        public String getSelectionText(final int number) {
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
        public String getAdditionalText(final int number) {
            switch(number) {
            case 1:
                if (SessionData.get(uuid, Key.C_WHILE_HOLDING_MAIN_HAND) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<ItemStack> whileHoldingMainHand
                            = (List<ItemStack>) SessionData.get(uuid, Key.C_WHILE_HOLDING_MAIN_HAND);
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

        @Override
        @SuppressWarnings("unchecked")
        public String getPromptText() {
            // Check/add newly made item
            if (SessionData.get(uuid, "tempStack") != null) {
                if (SessionData.get(uuid, Key.C_WHILE_HOLDING_MAIN_HAND) != null) {
                    final List<ItemStack> items
                            = (List<ItemStack>) SessionData.get(uuid, Key.C_WHILE_HOLDING_MAIN_HAND);
                    if (items != null) {
                        items.add((ItemStack) SessionData.get(uuid, "tempStack"));
                        SessionData.set(uuid, Key.C_WHILE_HOLDING_MAIN_HAND, items);
                    }
                } else {
                    final LinkedList<ItemStack> items = new LinkedList<>();
                    items.add((ItemStack) SessionData.get(uuid, "tempStack"));
                    SessionData.set(uuid, Key.C_WHILE_HOLDING_MAIN_HAND, items);
                }
                ItemStackPrompt.clearSessionData(uuid);
            }

            final BukkitConditionsEditorPostOpenNumericPromptEvent event
                    = new BukkitConditionsEditorPostOpenNumericPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder text = new StringBuilder(ChatColor.GOLD + "- " + getTitle() + " -");
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                        .append(getAdditionalText(i));
            }
            return sendClickableSelection(text.toString(), plugin.getQuester(uuid));
        }

        @Override
        public void acceptInput(final Number input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            switch(input.intValue()) {
            case 1:
                new ItemStackPrompt(uuid, ConditionItemsInMainHandListPrompt.this).start();
                break;
            case 2:
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("conditionEditorConditionCleared"));
                SessionData.set(uuid, Key.C_WHILE_HOLDING_MAIN_HAND, null);
                new ConditionItemsInMainHandListPrompt(uuid).start();
                break;
            case 3:
                new ConditionPlayerPrompt(uuid).start();
                break;
            default:
                new ConditionItemsInMainHandListPrompt(uuid).start();
                break;
            }
        }
    }

    public class ConditionItemsWearListPrompt extends ConditionsEditorIntegerPrompt {

        public ConditionItemsWearListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 3;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return BukkitLang.get("conditionEditorItemsWear");
        }

        @Override
        public ChatColor getNumberColor(final int number) {
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
        public String getSelectionText(final int number) {
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
        public String getAdditionalText(final int number) {
            switch(number) {
                case 1:
                    if (SessionData.get(uuid, Key.C_WHILE_WEARING) == null) {
                        return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                    } else {
                        final StringBuilder text = new StringBuilder();
                        final List<ItemStack> whileWearing
                                = (List<ItemStack>) SessionData.get(uuid, Key.C_WHILE_WEARING);
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

        @Override
        @SuppressWarnings("unchecked")
        public String getPromptText() {
            // Check/add newly made item
            if (SessionData.get(uuid, "tempStack") != null) {
                if (SessionData.get(uuid, Key.C_WHILE_WEARING) != null) {
                    final List<ItemStack> items
                            = (List<ItemStack>) SessionData.get(uuid, Key.C_WHILE_WEARING);
                    if (items != null) {
                        items.add((ItemStack) SessionData.get(uuid, "tempStack"));
                        SessionData.set(uuid, Key.C_WHILE_WEARING, items);
                    }
                } else {
                    final LinkedList<ItemStack> items = new LinkedList<>();
                    items.add((ItemStack) SessionData.get(uuid, "tempStack"));
                    SessionData.set(uuid, Key.C_WHILE_WEARING, items);
                }
                ItemStackPrompt.clearSessionData(uuid);
            }

            final BukkitConditionsEditorPostOpenNumericPromptEvent event
                    = new BukkitConditionsEditorPostOpenNumericPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder text = new StringBuilder(ChatColor.GOLD + "- " + getTitle() + " -");
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                        .append(getAdditionalText(i));
            }
            return sendClickableSelection(text.toString(), plugin.getQuester(uuid));
        }

        @Override
        public void acceptInput(final Number input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            switch(input.intValue()) {
                case 1:
                    new ItemStackPrompt(uuid, ConditionItemsWearListPrompt.this).start();
                    break;
                case 2:
                    sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("conditionEditorConditionCleared"));
                    SessionData.set(uuid, Key.C_WHILE_WEARING, null);
                    new ConditionItemsWearListPrompt(uuid).start();
                    break;
                case 3:
                    new ConditionPlayerPrompt(uuid).start();
                    break;
                default:
                    new ConditionItemsWearListPrompt(uuid).start();
                    break;
            }
        }
    }
}
