/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.convo.quests.objectives;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.convo.generic.ItemStackPrompt;
import me.pikamug.quests.convo.quests.QuestsEditorIntegerPrompt;
import me.pikamug.quests.convo.quests.stages.QuestStageMainPrompt;
import me.pikamug.quests.events.editor.quests.BukkitQuestsEditorPostOpenNumericPromptEvent;
import me.pikamug.quests.util.BukkitItemUtil;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.quests.util.BukkitMiscUtil;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.SessionData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class QuestItemsPrompt extends QuestsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final BukkitQuestsPlugin plugin;
    private final int stageNum;
    private final String pref;

    public QuestItemsPrompt(final int stageNum, final UUID uuid) {
        super(uuid);
        this.uuid = uuid;
        this.plugin = BukkitQuestsPlugin.getInstance();
        this.stageNum = stageNum;
        this.pref = "stage" + stageNum;
    }
    
    private final int size = 6;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle() {
        return BukkitLang.get("stageEditorItems");
    }
    
    @Override
    public ChatColor getNumberColor(final int number) {
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
    public String getSelectionText(final int number) {
        switch(number) {
        case 1:
            return ChatColor.YELLOW + BukkitLang.get("stageEditorCraftItems");
        case 2:
            return ChatColor.YELLOW + BukkitLang.get("stageEditorSmeltItems");
        case 3:
            return ChatColor.YELLOW + BukkitLang.get("stageEditorEnchantItems");
        case 4:
            return ChatColor.YELLOW + BukkitLang.get("stageEditorBrewPotions");
        case 5:
            return ChatColor.YELLOW + BukkitLang.get("stageEditorConsumeItems");
        case 6:
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
            if (SessionData.get(uuid, pref + Key.S_CRAFT_ITEMS) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<ItemStack> items
                        = (LinkedList<ItemStack>) SessionData.get(uuid, pref + Key.S_CRAFT_ITEMS);
                if (items != null) {
                    for (final ItemStack item : items) {
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.BLUE)
                                .append(BukkitItemUtil.getName(item)).append(ChatColor.GRAY).append(" x ")
                                .append(ChatColor.AQUA).append(item.getAmount());
                    }
                }
                return text.toString();
            }
        case 2:
            if (SessionData.get(uuid, pref + Key.S_SMELT_ITEMS) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<ItemStack> items
                        = (LinkedList<ItemStack>) SessionData.get(uuid, pref + Key.S_SMELT_ITEMS);
                if (items != null) {
                    for (final ItemStack item : items) {
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.BLUE)
                                .append(BukkitItemUtil.getName(item)).append(ChatColor.GRAY).append(" x ")
                                .append(ChatColor.AQUA).append(item.getAmount());
                    }
                }
                return text.toString();
            }
        case 3:
            if (SessionData.get(uuid, pref + Key.S_ENCHANT_ITEMS) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<ItemStack> items = (LinkedList<ItemStack>) SessionData.get(uuid, pref + Key.S_ENCHANT_ITEMS);
                if (items != null) {
                    for (final ItemStack item : items) {
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.BLUE)
                                .append(BukkitItemUtil.getName(item)).append(ChatColor.GRAY).append(" x ")
                                .append(ChatColor.AQUA).append(item.getAmount());
                    }
                }
                return text.toString();
            }
        case 4:
            if (SessionData.get(uuid, pref + Key.S_BREW_ITEMS) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<ItemStack> items
                        = (LinkedList<ItemStack>) SessionData.get(uuid, pref + Key.S_BREW_ITEMS);
                if (items != null) {
                    for (final ItemStack item : items) {
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.BLUE)
                                .append(BukkitItemUtil.getName(item)).append(ChatColor.GRAY).append(" x ")
                                .append(ChatColor.AQUA).append(item.getAmount());
                    }
                }
                return text.toString();
            }
        case 5:
            if (SessionData.get(uuid, pref + Key.S_CONSUME_ITEMS) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<ItemStack> items
                        = (LinkedList<ItemStack>) SessionData.get(uuid, pref + Key.S_CONSUME_ITEMS);
                if (items != null) {
                    for (final ItemStack item : items) {
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.BLUE)
                                .append(BukkitItemUtil.getName(item)).append(ChatColor.GRAY).append(" x ")
                                .append(ChatColor.AQUA).append(item.getAmount());
                    }
                }
                return text.toString();
            }
        case 6:
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
            if (SessionData.get(uuid, pref + Key.S_CRAFT_ITEMS) != null) {
                final List<ItemStack> items = (List<ItemStack>) SessionData.get(uuid, pref + Key.S_CRAFT_ITEMS);
                if (items != null) {
                    items.add((ItemStack) SessionData.get(uuid, "tempStack"));
                    SessionData.set(uuid, pref + Key.S_CRAFT_ITEMS, items);
                }
            } else if (SessionData.get(uuid, pref + Key.S_SMELT_ITEMS) != null) {
                final List<ItemStack> items = (List<ItemStack>) SessionData.get(uuid, pref + Key.S_SMELT_ITEMS);
                if (items != null) {
                    items.add((ItemStack) SessionData.get(uuid, "tempStack"));
                    SessionData.set(uuid, pref + Key.S_SMELT_ITEMS, items);
                }
            }
            ItemStackPrompt.clearSessionData(uuid);
        }

        final BukkitQuestsEditorPostOpenNumericPromptEvent event
                = new BukkitQuestsEditorPostOpenNumericPromptEvent(uuid, this);
        plugin.getServer().getPluginManager().callEvent(event);
        
        final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle() + " -");
        for (int i = 1; i <= size; i++) {
            text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                    .append(ChatColor.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                    .append(getAdditionalText(i));
        }
        return text.toString();
    }

    @Override
    public void acceptInput(final Number input) {
        final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
        switch(input.intValue()) {
        case 1:
            new QuestItemsCraftListPrompt(uuid).start();
            break;
        case 2:
            new QuestItemsSmeltListPrompt(uuid).start();
            break;
        case 3:
            new QuestItemsEnchantListPrompt(uuid).start();
            break;
        case 4:
            new QuestItemsBrewListPrompt(uuid).start();
            break;
        case 5:
            new QuestItemsConsumeListPrompt(uuid).start();
            break;
        case 6:
            try {
                new QuestStageMainPrompt(stageNum, uuid).start();
            } catch (final Exception e) {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("itemCreateCriticalError"));
            }
            break;
        default:
            new QuestItemsPrompt(stageNum, uuid).start();
            break;
        }
    }
    
    public class QuestItemsCraftListPrompt extends QuestsEditorIntegerPrompt {
        
        public QuestItemsCraftListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 3;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle() {
            return BukkitLang.get("stageEditorCraftItems");
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
                if (SessionData.get(uuid, pref + Key.S_CRAFT_ITEMS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<ItemStack> craftItems
                            = (List<ItemStack>) SessionData.get(uuid, pref + Key.S_CRAFT_ITEMS);
                    if (craftItems != null) {
                        for (final ItemStack is : craftItems) {
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
        public @NotNull String getPromptText() {
            // Check/add newly made item
            if (SessionData.get(uuid, "tempStack") != null) {
                if (SessionData.get(uuid, pref + Key.S_CRAFT_ITEMS) != null) {
                    final List<ItemStack> items = (List<ItemStack>) SessionData.get(uuid, pref + Key.S_CRAFT_ITEMS);
                    if (items != null) {
                        items.add((ItemStack) SessionData.get(uuid, "tempStack"));
                        SessionData.set(uuid, pref + Key.S_CRAFT_ITEMS, items);
                    }
                } else {
                    final LinkedList<ItemStack> items = new LinkedList<>();
                    items.add((ItemStack) SessionData.get(uuid, "tempStack"));
                    SessionData.set(uuid, pref + Key.S_CRAFT_ITEMS, items);
                }
                ItemStackPrompt.clearSessionData(uuid);
            }

            final BukkitQuestsEditorPostOpenNumericPromptEvent event
                    = new BukkitQuestsEditorPostOpenNumericPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder text = new StringBuilder(ChatColor.GOLD + "- " + getTitle() + " -");
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                        .append(getAdditionalText(i));
            }
            return text.toString();
        }

        @Override
        public void acceptInput(final Number input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            switch(input.intValue()) {
            case 1:
                new ItemStackPrompt(uuid, QuestItemsCraftListPrompt.this);
            case 2:
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorObjectiveCleared"));
                SessionData.set(uuid, pref + Key.S_CRAFT_ITEMS, null);
                new QuestItemsCraftListPrompt(uuid).start();
                break;
            default:
                new QuestItemsPrompt(stageNum, uuid).start();
                break;
            }
        }
    }
    
    public class QuestItemsSmeltListPrompt extends QuestsEditorIntegerPrompt {
        
        public QuestItemsSmeltListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        private final int size = 3;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle() {
            return BukkitLang.get("stageEditorSmeltItems");
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
                if (SessionData.get(uuid, pref + Key.S_SMELT_ITEMS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<ItemStack> smeltItems
                            = (List<ItemStack>) SessionData.get(uuid, pref + Key.S_SMELT_ITEMS);
                    if (smeltItems != null) {
                        for (final ItemStack is : smeltItems) {
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
        public @NotNull String getPromptText() {
            // Check/add newly made item
            if (SessionData.get(uuid, "tempStack") != null) {
                if (SessionData.get(uuid, pref + Key.S_SMELT_ITEMS) != null) {
                    final List<ItemStack> items = (List<ItemStack>) SessionData.get(uuid, pref + Key.S_SMELT_ITEMS);
                    if (items != null) {
                        items.add((ItemStack) SessionData.get(uuid, "tempStack"));
                        SessionData.set(uuid, pref + Key.S_SMELT_ITEMS, items);
                    }
                } else {
                    final LinkedList<ItemStack> items = new LinkedList<>();
                    items.add((ItemStack) SessionData.get(uuid, "tempStack"));
                    SessionData.set(uuid, pref + Key.S_SMELT_ITEMS, items);
                }
                ItemStackPrompt.clearSessionData(uuid);
            }

            final BukkitQuestsEditorPostOpenNumericPromptEvent event
                    = new BukkitQuestsEditorPostOpenNumericPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder text = new StringBuilder(ChatColor.GOLD + "- " + getTitle() + " -");
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                        .append(getAdditionalText(i));
            }
            return text.toString();
        }

        @Override
        public void acceptInput(final Number input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            switch(input.intValue()) {
            case 1:
                new ItemStackPrompt(uuid, QuestItemsSmeltListPrompt.this);
            case 2:
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorObjectiveCleared"));
                SessionData.set(uuid, pref + Key.S_SMELT_ITEMS, null);
                new QuestItemsSmeltListPrompt(uuid).start();
                break;
            default:
                new QuestItemsPrompt(stageNum, uuid).start();
                break;
            }
        }
    }

    public class QuestItemsEnchantListPrompt extends QuestsEditorIntegerPrompt {

        public QuestItemsEnchantListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        private final int size = 3;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle() {
            return BukkitLang.get("stageEditorEnchantItems");
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
                if (SessionData.get(uuid, pref + Key.S_ENCHANT_ITEMS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<ItemStack> enchantItems
                            = (List<ItemStack>) SessionData.get(uuid, pref + Key.S_ENCHANT_ITEMS);
                    if (enchantItems != null) {
                        for (final ItemStack is : enchantItems) {
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
        public @NotNull String getPromptText() {
            // Check/add newly made item
            if (SessionData.get(uuid, "tempStack") != null) {
                if (SessionData.get(uuid, pref + Key.S_ENCHANT_ITEMS) != null) {
                    final List<ItemStack> items = (List<ItemStack>) SessionData.get(uuid, pref + Key.S_ENCHANT_ITEMS);
                    if (items != null) {
                        items.add((ItemStack) SessionData.get(uuid, "tempStack"));
                        SessionData.set(uuid, pref + Key.S_ENCHANT_ITEMS, items);
                    }
                } else {
                    final LinkedList<ItemStack> items = new LinkedList<>();
                    items.add((ItemStack) SessionData.get(uuid, "tempStack"));
                    SessionData.set(uuid, pref + Key.S_ENCHANT_ITEMS, items);
                }
                ItemStackPrompt.clearSessionData(uuid);
            }

            final BukkitQuestsEditorPostOpenNumericPromptEvent event
                    = new BukkitQuestsEditorPostOpenNumericPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder text = new StringBuilder(ChatColor.GOLD + "- " + getTitle() + " -");
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                        .append(getAdditionalText(i));
            }
            return text.toString();
        }

        @Override
        public void acceptInput(final Number input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            switch(input.intValue()) {
            case 1:
                new ItemStackPrompt(uuid, QuestItemsEnchantListPrompt.this);
                break;
            case 2:
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorObjectiveCleared"));
                SessionData.set(uuid, pref + Key.S_ENCHANT_ITEMS, null);
                new QuestItemsEnchantListPrompt(uuid).start();
                break;
            default:
                new QuestItemsPrompt(stageNum, uuid).start();
                break;
            }
        }
    }
    
    public class QuestItemsBrewListPrompt extends QuestsEditorIntegerPrompt {
        
        public QuestItemsBrewListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        private final int size = 3;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle() {
            return BukkitLang.get("stageEditorBrewPotions");
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
                if (SessionData.get(uuid, pref + Key.S_BREW_ITEMS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<ItemStack> brewItems = (List<ItemStack>) SessionData.get(uuid, pref + Key.S_BREW_ITEMS);
                    if (brewItems != null) {
                        for (final ItemStack is : brewItems) {
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
        public @NotNull String getPromptText() {
            // Check/add newly made item
            if (SessionData.get(uuid, "tempStack") != null) {
                if (SessionData.get(uuid, pref + Key.S_BREW_ITEMS) != null) {
                    final List<ItemStack> items = (List<ItemStack>) SessionData.get(uuid, pref + Key.S_BREW_ITEMS);
                    if (items != null) {
                        items.add((ItemStack) SessionData.get(uuid, "tempStack"));
                        SessionData.set(uuid, pref + Key.S_BREW_ITEMS, items);
                    }
                } else {
                    final LinkedList<ItemStack> items = new LinkedList<>();
                    items.add((ItemStack) SessionData.get(uuid, "tempStack"));
                    SessionData.set(uuid, pref + Key.S_BREW_ITEMS, items);
                }
                ItemStackPrompt.clearSessionData(uuid);
            }

            final BukkitQuestsEditorPostOpenNumericPromptEvent event
                    = new BukkitQuestsEditorPostOpenNumericPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder text = new StringBuilder(ChatColor.GOLD + "- " + getTitle() + " -");
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                        .append(getAdditionalText(i));
            }
            return text.toString();
        }

        @Override
        public void acceptInput(final Number input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            switch(input.intValue()) {
            case 1:
                new ItemStackPrompt(uuid, QuestItemsBrewListPrompt.this);
            case 2:
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorObjectiveCleared"));
                SessionData.set(uuid, pref + Key.S_BREW_ITEMS, null);
                new QuestItemsBrewListPrompt(uuid).start();
                break;
            default:
                new QuestItemsPrompt(stageNum, uuid).start();
                break;
            }
        }
    }
    
    public class QuestItemsConsumeListPrompt extends QuestsEditorIntegerPrompt {
        
        public QuestItemsConsumeListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        private final int size = 3;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle() {
            return BukkitLang.get("stageEditorConsumeItems");
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
                if (SessionData.get(uuid, pref + Key.S_CONSUME_ITEMS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<ItemStack> consumeItems
                            = (List<ItemStack>) SessionData.get(uuid, pref + Key.S_CONSUME_ITEMS);
                    if (consumeItems != null) {
                        for (final ItemStack is : consumeItems) {
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
        public @NotNull String getPromptText() {
            // Check/add newly made item
            if (SessionData.get(uuid, "tempStack") != null) {
                if (SessionData.get(uuid, pref + Key.S_CONSUME_ITEMS) != null) {
                    final List<ItemStack> items = (List<ItemStack>) SessionData.get(uuid, pref + Key.S_CONSUME_ITEMS);
                    if (items != null) {
                        items.add((ItemStack) SessionData.get(uuid, "tempStack"));
                        SessionData.set(uuid, pref + Key.S_CONSUME_ITEMS, items);
                    }
                } else {
                    final LinkedList<ItemStack> items = new LinkedList<>();
                    items.add((ItemStack) SessionData.get(uuid, "tempStack"));
                    SessionData.set(uuid, pref + Key.S_CONSUME_ITEMS, items);
                }
                ItemStackPrompt.clearSessionData(uuid);
            }

            final BukkitQuestsEditorPostOpenNumericPromptEvent event
                    = new BukkitQuestsEditorPostOpenNumericPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder text = new StringBuilder(ChatColor.GOLD + "- " + getTitle() + " -");
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                        .append(getAdditionalText(i));
            }
            return text.toString();
        }

        @Override
        public void acceptInput(final Number input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            switch(input.intValue()) {
            case 1:
                new ItemStackPrompt(uuid, QuestItemsConsumeListPrompt.this);
            case 2:
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorObjectiveCleared"));
                SessionData.set(uuid, pref + Key.S_CONSUME_ITEMS, null);
                new QuestItemsConsumeListPrompt(uuid).start();
                break;
            default:
                new QuestItemsPrompt(stageNum, uuid).start();
                break;
            }
        }
    }
}
